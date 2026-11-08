package com.vhvkhangg.personalprivatevault.drive.fixture;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import com.sun.net.httpserver.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.*;

/** HTTP fake binds every request to the access token issued by the signed S1 OAuth fixture. */
public class FakeGoogleDriveServer implements AutoCloseable {
    public enum Mode { NORMAL, HEADERS_ONLY, PARTIAL, TRICKLE, OVERSIZED, MALFORMED, REDIRECT, LARGE_HEADERS }
    public record Call(String method,String path,String query,String subject) {}
    public record Remote(String owner,ObjectNode metadata,Path bytes) {}
    private record Session(String owner,ObjectNode metadata,long expected) {}
    private final ObjectMapper mapper=new ObjectMapper();
    private final HttpServer server;
    private final ExecutorService workers=Executors.newFixedThreadPool(12);
    private final Function<String,String> subjects;
    private final Path storage=Files.createTempDirectory("gdi26-provider-");
    private final Map<String,Session> sessions=new ConcurrentHashMap<>();
    public final Map<String,Remote> files=new ConcurrentHashMap<>();
    public final Queue<Call> calls=new ConcurrentLinkedQueue<>();
    public final AtomicInteger generated=new AtomicInteger(),rootCreates=new AtomicInteger(),fileCreates=new AtomicInteger(),uploadSessions=new AtomicInteger(),putCalls=new AtomicInteger(),mediaCalls=new AtomicInteger(),childFolderCreates=new AtomicInteger();
    public volatile boolean allowForeignMetadata,allowForeignMedia,loseUploadResponse,loseRootResponse;
    public volatile int aboutStatus=200,metadataStatus=200,uploadStatus=200,mediaStatus=200;
    public volatile String aboutOverride,generatedOverride,locationOverride,metadataResponseBody,metadataPauseId,quotaOverride;
    public volatile ObjectNode listOverride;
    public volatile UnaryOperator<ObjectNode> metadataMutation=UnaryOperator.identity();
    public volatile Mode metadataMode=Mode.NORMAL,mediaMode=Mode.NORMAL;
    public volatile boolean mediaChunked;
    public volatile long mediaLengthDelta,shortMedia=-1;
    public volatile int extraMedia;
    public volatile CountDownLatch rootEntered,rootRelease,uploadEntered,uploadRelease,metadataEntered,metadataRelease;
    public volatile CountDownLatch bodyRelease=new CountDownLatch(0);

    public FakeGoogleDriveServer(Function<String,String> subjects) throws IOException {
        this.subjects=subjects;
        server=HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);server.setExecutor(workers);
        server.createContext("/drive/",this::handle);server.createContext("/upload/",this::handle);server.start();
    }
    public URI origin(){return URI.create("http://127.0.0.1:"+server.getAddress().getPort());}
    public void reset() throws IOException {
        files.clear();sessions.clear();calls.clear();
        for(var counter:List.of(generated,rootCreates,fileCreates,uploadSessions,putCalls,mediaCalls,childFolderCreates))counter.set(0);
        allowForeignMetadata=false;allowForeignMedia=false;loseUploadResponse=false;loseRootResponse=false;
        aboutStatus=200;metadataStatus=200;uploadStatus=200;mediaStatus=200;
        aboutOverride=null;generatedOverride=null;locationOverride=null;metadataResponseBody=null;metadataPauseId=null;listOverride=null;metadataMutation=UnaryOperator.identity();
        quotaOverride=null;
        metadataMode=Mode.NORMAL;mediaMode=Mode.NORMAL;mediaChunked=false;mediaLengthDelta=0;shortMedia=-1;extraMedia=0;
        rootEntered=null;rootRelease=null;uploadEntered=null;uploadRelease=null;metadataEntered=null;metadataRelease=null;bodyRelease=new CountDownLatch(0);
        try(var paths=Files.newDirectoryStream(storage)){for(Path p:paths)Files.deleteIfExists(p);}
    }
    private void handle(HttpExchange e) throws IOException {
        try {
            String authorization=e.getRequestHeaders().getFirst("Authorization");
            String owner=authorization!=null && authorization.startsWith("Bearer ") ? subjects.apply(authorization.substring(7)) : null;
            String path=e.getRequestURI().getPath();Map<String,String> query=query(e.getRequestURI().getRawQuery());
            calls.add(new Call(e.getRequestMethod(),path,e.getRequestURI().getRawQuery(),owner));
            if(owner==null){respond(e,401,"{}".getBytes(),Mode.NORMAL);return;}
            if(path.equals("/drive/v2/about")) {
                String about=aboutOverride==null ? "{\"rootFolderId\":\"account-root-"+owner+"\"}" : aboutOverride;
                respond(e,aboutStatus,about.getBytes(StandardCharsets.UTF_8),metadataMode);return;
            }
            if(path.equals("/drive/v3/about")) {
                signal(metadataEntered);await(metadataRelease);
                String quota=quotaOverride!=null ? quotaOverride :
                        "{\"storageQuota\":{\"usage\":\"1000\",\"usageInDrive\":\"500\",\"limit\":\"10000\"}}";
                respond(e,aboutStatus,quota.getBytes(StandardCharsets.UTF_8),metadataMode);return;
            }
            if(path.equals("/drive/v3/files/generateIds")) {
                String id=generatedOverride==null ? "file-"+generated.incrementAndGet() : generatedOverride;
                respond(e,200,mapper.writeValueAsBytes(Map.of("ids",List.of(id))),metadataMode);return;
            }
            if(path.equals("/drive/v3/files") && e.getRequestMethod().equals("POST")) {
                ObjectNode metadata=(ObjectNode)mapper.readTree(e.getRequestBody().readNBytes(64*1024));
                String id=metadata.path("id").asText();
                if(files.containsKey(id)){respond(e,409,bytes("{}"),Mode.NORMAL);return;}
                String kind=metadata.path("appProperties").path("ppvKind").asText("");
                if("child".equals(kind)) {
                    childFolderCreates.incrementAndGet();
                } else {
                    metadata.set("parents",mapper.valueToTree(List.of("account-root-"+owner)));
                    rootCreates.incrementAndGet();
                }
                metadata.put("trashed",false);
                metadata.put("modifiedTime",Instant.now().toString());
                files.put(id,new Remote(owner,metadata,null));
                signal(rootEntered);await(rootRelease);
                if(loseRootResponse){loseRootResponse=false;e.close();return;}
                respond(e,200,mapper.writeValueAsBytes(metadata),metadataMode);return;
            }
            if(path.equals("/drive/v3/files") && e.getRequestMethod().equals("GET")) {
                ObjectNode page=listOverride;
                if(page==null) {
                    page=mapper.createObjectNode();ArrayNode array=page.putArray("files");
                    String q=query.getOrDefault("q","");
                    String parent=null;
                    if(q.contains("in parents")) {
                        var matcher=java.util.regex.Pattern.compile("'([^']+)'\\s+in\\s+parents").matcher(q);
                        if(matcher.find()) {
                            parent=matcher.group(1);
                        }
                    }
                    final String requiredParent=parent;
                    int pageSize=Integer.parseInt(query.getOrDefault("pageSize","25"));
                    String pageToken=query.get("pageToken");
                    int offset=0;
                    if(pageToken!=null && pageToken.startsWith("token-")) {
                        try { offset=Integer.parseInt(pageToken.substring(6)); } catch(NumberFormatException ignored) {}
                    }
                    var matched=files.values().stream().filter(f -> f.owner().equals(owner)
                            && (requiredParent==null || (f.metadata().path("parents").size()==1 && f.metadata().path("parents").get(0).asText().equals(requiredParent))))
                            .sorted((a,b) -> {
                                String ma=a.metadata().path("modifiedTime").asText("");
                                String mb=b.metadata().path("modifiedTime").asText("");
                                int c=mb.compareTo(ma);
                                if(c!=0) return c;
                                return a.metadata().path("name").asText("").compareTo(b.metadata().path("name").asText(""));
                            })
                            .toList();
                    int end=Math.min(matched.size(), offset+pageSize);
                    for(int i=offset; i<end; i++) {
                        array.add(matched.get(i).metadata().deepCopy());
                    }
                    if(end<matched.size()) {
                        page.put("nextPageToken", "token-"+end);
                    }
                }
                signal(metadataEntered);await(metadataRelease);
                respond(e,metadataStatus,mapper.writeValueAsBytes(page),metadataMode);return;
            }
            if(path.startsWith("/drive/v3/files/") && e.getRequestMethod().equals("GET")) {
                String id=path.substring("/drive/v3/files/".length());
                if(id.equals("root") || id.startsWith("account-root-")){respond(e,403,bytes("{}"),Mode.NORMAL);return;}
                Remote file=files.get(id);
                if(file==null || (!file.owner().equals(owner) && !allowForeignMetadata)){respond(e,404,bytes("{}"),Mode.NORMAL);return;}
                if("media".equals(query.get("alt"))) {media(e,file,owner);return;}
                if(metadataPauseId==null || metadataPauseId.equals(id)){signal(metadataEntered);await(metadataRelease);}
                respond(e,metadataStatus,metadataResponseBody==null?mapper.writeValueAsBytes(metadataMutation.apply(file.metadata().deepCopy())):bytes(metadataResponseBody),metadataMode);return;
            }
            if(path.equals("/upload/drive/v3/files") && e.getRequestMethod().equals("POST")) {
                if("resumable".equals(query.get("uploadType"))) {
                    ObjectNode metadata=(ObjectNode)mapper.readTree(e.getRequestBody().readNBytes(64*1024));
                    String session=UUID.randomUUID().toString();sessions.put(session,new Session(owner,metadata,Long.parseLong(e.getRequestHeaders().getFirst("X-Upload-Content-Length"))));
                    uploadSessions.incrementAndGet();
                    e.getResponseHeaders().set("Location",locationOverride==null ? origin()+"/upload/drive/v3/files?uploadType=resumable&upload_id="+session : locationOverride);
                    respond(e,200,bytes("{}"),Mode.NORMAL);return;
                }
                byte[] multipart=e.getRequestBody().readNBytes(5_000_000+64*1024);
                String boundary=e.getRequestHeaders().getFirst("Content-Type").split("boundary=")[1];
                byte[] separator=bytes("\r\n--"+boundary+"\r\n");
                int first=index(multipart,bytes("\r\n\r\n"),0)+4, split=index(multipart,separator,first);
                ObjectNode metadata=(ObjectNode)mapper.readTree(Arrays.copyOfRange(multipart,first,split));
                int start=index(multipart,bytes("\r\n\r\n"),split+separator.length)+4;
                int end=multipart.length-bytes("\r\n--"+boundary+"--\r\n").length;
                Path data=Files.createTempFile(storage,"object-",".bin");Files.write(data,Arrays.copyOfRange(multipart,start,end));
                accept(e,owner,metadata,data);return;
            }
            if(path.equals("/upload/drive/v3/files") && e.getRequestMethod().equals("PUT")) {
                putCalls.incrementAndGet();Session session=sessions.get(query.get("upload_id"));
                if(session==null || !session.owner().equals(owner)){respond(e,403,bytes("{}"),Mode.NORMAL);return;}
                Path data=Files.createTempFile(storage,"object-",".bin");
                try(var output=Files.newOutputStream(data)){byte[] buffer=new byte[64*1024];int count;while((count=e.getRequestBody().read(buffer))!=-1)output.write(buffer,0,count);}
                if(Files.size(data)!=session.expected())throw new IOException("Synthetic transport length mismatch");
                accept(e,owner,session.metadata(),data);return;
            }
            respond(e,404,bytes("{}"),Mode.NORMAL);
        } catch(RuntimeException error){try{respond(e,500,bytes("{}"),Mode.NORMAL);}catch(IOException ignored){e.close();}}
    }
    private void accept(HttpExchange e,String owner,ObjectNode metadata,Path data) throws IOException {
        String id=metadata.path("id").asText();
        if(files.containsKey(id)){respond(e,409,bytes("{}"),Mode.NORMAL);return;}
        metadata.put("trashed",false);metadata.put("size",Long.toString(Files.size(data)));metadata.put("modifiedTime",Instant.now().toString());
        try {
            MessageDigest md5=MessageDigest.getInstance("MD5");
            try(var input=Files.newInputStream(data)){byte[] buffer=new byte[64*1024];int count;while((count=input.read(buffer))!=-1)md5.update(buffer,0,count);}
            metadata.put("md5Checksum",HexFormat.of().formatHex(md5.digest()));
        }catch(java.security.NoSuchAlgorithmException impossible){throw new IllegalStateException(impossible);}
        files.put(id,new Remote(owner,metadata,data));fileCreates.incrementAndGet();
        signal(uploadEntered);await(uploadRelease);
        if(loseUploadResponse){loseUploadResponse=false;e.close();return;}
        respond(e,uploadStatus,mapper.writeValueAsBytes(metadata),metadataMode);
    }
    private void media(HttpExchange e,Remote file,String owner) throws IOException {
        mediaCalls.incrementAndGet();
        if(mediaStatus!=200){respond(e,mediaStatus,bytes("{\"error\":{\"errors\":[{\"reason\":\"fileNotDownloadable\"}]}}"),Mode.NORMAL);return;}
        if(file.bytes()==null || (!file.owner().equals(owner) && !allowForeignMedia)){respond(e,403,bytes("{}"),Mode.NORMAL);return;}
        long size=Files.size(file.bytes());e.getResponseHeaders().set("Content-Type","application/octet-stream");
        e.sendResponseHeaders(200,mediaChunked || size==0 ? 0 : Math.max(1,size+mediaLengthDelta));
        if(mediaMode==Mode.HEADERS_ONLY)await(bodyRelease);
        try(var input=Files.newInputStream(file.bytes());var output=e.getResponseBody()) {
            byte[] buffer=new byte[64*1024];int count;long sent=0;
            while((count=input.read(buffer))!=-1) {
                if(shortMedia>=0)count=(int)Math.min(count,shortMedia-sent);
                if(count<=0)break;
                if(mediaMode==Mode.TRICKLE){for(int i=0;i<count;i++){output.write(buffer[i]);output.flush();tick();}sent+=count;continue;}
                output.write(buffer,0,count);output.flush();sent+=count;
                if(mediaMode==Mode.PARTIAL){await(bodyRelease);break;}
                if(mediaMode==Mode.TRICKLE)tick();
                if(shortMedia>=0 && sent>=shortMedia)break;
            }
            for(int i=0;i<extraMedia;i++)output.write(1);
        }finally{e.close();}
    }
    private void respond(HttpExchange e,int status,byte[] bytes,Mode mode) throws IOException {
        try(e) {
            e.getResponseHeaders().set("Content-Type","application/json");
            if(mode==Mode.REDIRECT){e.getResponseHeaders().set("Location",origin()+"/drive/v3/files");e.sendResponseHeaders(302,-1);return;}
            if(mode==Mode.LARGE_HEADERS)e.getResponseHeaders().set("X-Synthetic","x".repeat(20*1024));
            if(mode==Mode.OVERSIZED)bytes=new byte[1024*1024+1];
            if(mode==Mode.MALFORMED)bytes=bytes("{bad-json");
            e.sendResponseHeaders(status,bytes.length);var output=e.getResponseBody();
            if(mode==Mode.PARTIAL){output.write(bytes,0,1);output.flush();await(bodyRelease);output.write(bytes,1,bytes.length-1);}
            else if(mode==Mode.HEADERS_ONLY){await(bodyRelease);output.write(bytes);}
            else if(mode==Mode.TRICKLE){for(byte b:bytes){output.write(b);output.flush();tick();}}
            else output.write(bytes);
        }
    }
    private void tick(){try{bodyRelease.await(50,TimeUnit.MILLISECONDS);}catch(InterruptedException e){Thread.currentThread().interrupt();}}
    private void await(CountDownLatch latch){if(latch!=null)try{if(!latch.await(10,TimeUnit.SECONDS))throw new IllegalStateException("Synthetic barrier expired");}catch(InterruptedException e){Thread.currentThread().interrupt();}}
    private void signal(CountDownLatch latch){if(latch!=null)latch.countDown();}
    private int index(byte[] haystack,byte[] needle,int start){outer:for(int i=start;i<=haystack.length-needle.length;i++){for(int j=0;j<needle.length;j++)if(haystack[i+j]!=needle[j])continue outer;return i;}throw new IllegalStateException("Synthetic multipart mismatch");}
    private byte[] bytes(String value){return value.getBytes(StandardCharsets.UTF_8);}
    private Map<String,String> query(String raw){var result=new HashMap<String,String>();if(raw!=null)for(String pair:raw.split("&")){String[] pieces=pair.split("=",2);result.put(URLDecoder.decode(pieces[0],StandardCharsets.UTF_8),pieces.length==2?URLDecoder.decode(pieces[1],StandardCharsets.UTF_8):"");}return result;}
    @Override public void close(){
        for(var latch:Arrays.asList(rootRelease,uploadRelease,metadataRelease,bodyRelease))if(latch!=null)latch.countDown();
        server.stop(0);workers.shutdownNow();
        try(var paths=Files.newDirectoryStream(storage)){for(Path p:paths)Files.deleteIfExists(p);}catch(IOException ignored){}
        try{Files.deleteIfExists(storage);}catch(IOException ignored){}
    }
}

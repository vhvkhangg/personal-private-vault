package com.vhvkhangg.personalprivatevault.drive.internal.files.http;

import com.fasterxml.jackson.databind.*;
import com.vhvkhangg.personalprivatevault.drive.internal.files.application.*;
import java.io.*;
import java.net.*;
import java.net.http.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.Flow;

/** Fixed-operation Drive transport. Only controlled test constructors replace its trusted origin. */
public class DriveFileHttpClient implements AutoCloseable {
    public static final URI GOOGLE=URI.create("https://www.googleapis.com");
    public static final String ROOT_FIELDS="id,mimeType,parents,trashed,appProperties";
    public static final String FILE_FIELDS="id,name,mimeType,size,modifiedTime,parents,trashed,appProperties";
    private final DriveFileAuthority authority;
    private final URI origin;
    private final HttpClient client=HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2))
            .followRedirects(HttpClient.Redirect.NEVER).version(HttpClient.Version.HTTP_1_1).build();
    private final ObjectMapper mapper=new ObjectMapper();
    public DriveFileHttpClient(DriveFileAuthority authority) { this(authority,GOOGLE); }
    public DriveFileHttpClient(DriveFileAuthority authority,URI controlledOrigin) { this.authority=authority; this.origin=controlledOrigin; }
    public record Media(InputStream input,long declaredLength) { @Override public String toString(){return "DriveMedia[REDACTED]";} }

    public String accountRoot(DriveFileAuthority.Lease lease,DriveFileResources.Operation op) {
        JsonNode result=json(lease,op,"GET",uri("/drive/v2/about","fields=rootFolderId"),null,Map.of(),16*1024,false);
        return opaque(result,"rootFolderId",255);
    }
    public String generateId(DriveFileAuthority.Lease lease,DriveFileResources.Operation op) {
        var result=json(lease,op,"GET",uri("/drive/v3/files/generateIds","count=1&space=drive&type=files"),null,Map.of(),16*1024,false);
        var ids=result.get("ids");
        if(ids==null || !ids.isArray() || ids.size()!=1 || !ids.get(0).isTextual()) throw failure();
        return DriveFileInputs.generatedId(ids.get(0).textValue());
    }
    public JsonNode root(DriveFileAuthority.Lease lease,DriveFileResources.Operation op,String id) {
        return get(lease,op,id,ROOT_FIELDS);
    }
    public JsonNode file(DriveFileAuthority.Lease lease,DriveFileResources.Operation op,String id,boolean checksum) {
        return get(lease,op,id,FILE_FIELDS+(checksum ? ",md5Checksum" : ""));
    }
    private JsonNode get(DriveFileAuthority.Lease lease,DriveFileResources.Operation op,String id,String fields) {
        DriveFileInputs.id(id);
        return json(lease,op,"GET",uri("/drive/v3/files/"+id,"fields="+encode(fields)),null,Map.of(),64*1024,false);
    }
    public JsonNode createRoot(DriveFileAuthority.Lease lease,DriveFileResources.Operation op,String id,UUID binding) {
        var metadata=Map.of("id",DriveFileInputs.generatedId(id),"name","PPV","mimeType","application/vnd.google-apps.folder",
                "parents",List.of("root"),"appProperties",Map.of("ppvKind","root","ppvRootBinding",binding.toString(),"ppvCreatedId",id));
        return json(lease,op,"POST",uri("/drive/v3/files","fields="+encode(ROOT_FIELDS)),bytes(metadata),Map.of(),64*1024,false);
    }
    public JsonNode list(DriveFileAuthority.Lease lease,DriveFileResources.Operation op,String root,int limit,String token) {
        DriveFileInputs.id(root);
        String query="q="+encode("'"+root+"' in parents and trashed = false")+"&pageSize="+limit
                +"&fields="+encode("nextPageToken,files("+FILE_FIELDS+")")+(token==null ? "" : "&pageToken="+encode(token));
        return json(lease,op,"GET",uri("/drive/v3/files",query),null,Map.of(),1024*1024,false);
    }
    public JsonNode upload(DriveFileAuthority.Lease lease,DriveFileResources.Operation op,
            DriveMultipartUpload.Spool spool,String id,UUID binding,String action) {
        var metadata=Map.of("id",DriveFileInputs.generatedId(id),"name",spool.name(),"mimeType",spool.mime(),
                "parents",List.of(lease.root().getRootFileId()),"appProperties",Map.of("ppvKind","file","ppvRootBinding",binding.toString(),"ppvCreatedId",id,"ppvUploadAction",action));
        String fields=encode(FILE_FIELDS+",md5Checksum");
        if(spool.size()<=5_000_000) {
            String boundary="ppv_"+UUID.randomUUID();
            byte[] head=("--"+boundary+"\r\nContent-Type: application/json; charset=UTF-8\r\n\r\n"+new String(bytes(metadata),StandardCharsets.UTF_8)
                    +"\r\n--"+boundary+"\r\nContent-Type: "+spool.mime()+"\r\n\r\n").getBytes(StandardCharsets.UTF_8);
            byte[] tail=("\r\n--"+boundary+"--\r\n").getBytes(StandardCharsets.US_ASCII);
            var publisher=HttpRequest.BodyPublishers.concat(HttpRequest.BodyPublishers.ofByteArray(head),filePublisher(spool,op),HttpRequest.BodyPublishers.ofByteArray(tail));
            return jsonPublisher(lease,op,"POST",uri("/upload/drive/v3/files","uploadType=multipart&fields="+fields),publisher,
                    Map.of("Content-Type","multipart/related; boundary="+boundary),64*1024,true);
        }
        var session=send(lease,op,"POST",uri("/upload/drive/v3/files","uploadType=resumable&fields="+fields),
                HttpRequest.BodyPublishers.ofByteArray(bytes(metadata)),Map.of("Content-Type","application/json; charset=UTF-8",
                        "X-Upload-Content-Length",Long.toString(spool.size()),"X-Upload-Content-Type",spool.mime()),64*1024,false);
        success(session.statusCode(),session.body(),lease);
        URI location=validateSession(session.headers().firstValue("Location").orElse(null));
        return jsonPublisher(lease,op,"PUT",location,filePublisher(spool,op),Map.of("Content-Type",spool.mime()),64*1024,true);
    }
    private HttpRequest.BodyPublisher filePublisher(DriveMultipartUpload.Spool spool,DriveFileResources.Operation op) {
        if(spool.size()==0) return HttpRequest.BodyPublishers.noBody();
        var streaming=HttpRequest.BodyPublishers.ofInputStream(() -> {
            try { return op.track(Files.newInputStream(spool.path())); }
            catch(IOException e) { throw DriveFileException.input(); }
        });
        return HttpRequest.BodyPublishers.fromPublisher(streaming,spool.size());
    }
    public URI validateSession(String value) {
        try {
            if(value==null || value.length()>4096) throw failure();
            URI uri=URI.create(value);
            int expected=origin.getPort()<0 ? (origin.getScheme().equals("https") ? 443 : 80) : origin.getPort();
            int port=uri.getPort()<0 ? ("https".equals(uri.getScheme()) ? 443 : 80) : uri.getPort();
            if(!Objects.equals(uri.getScheme(),origin.getScheme()) || !Objects.equals(uri.getHost(),origin.getHost())
                    || port!=expected || uri.getRawUserInfo()!=null || uri.getRawFragment()!=null
                    || !"/upload/drive/v3/files".equals(uri.getRawPath()) || uri.getRawQuery()==null) throw failure();
            var query=new HashMap<String,String>();
            for(String pair:uri.getRawQuery().split("&",-1)) {
                String[] parts=pair.split("=",2);
                if(parts.length!=2 || query.putIfAbsent(parts[0],parts[1])!=null) throw failure();
            }
            if(query.size()!=2 || !"resumable".equals(query.get("uploadType")) || query.get("upload_id")==null
                    || !query.get("upload_id").matches("[A-Za-z0-9._~-]{1,3900}")) throw failure();
            return uri;
        } catch(IllegalArgumentException e) { throw failure(); }
    }
    public Media media(DriveFileAuthority.Lease lease,DriveFileResources.Operation op,String id) {
        DriveFileInputs.id(id); op.metadataPhase();
        CompletableFuture<HttpResponse<InputStream>> future=null;
        try {
            var request=request(lease,op,"GET",uri("/drive/v3/files/"+id,"alt=media"),HttpRequest.BodyPublishers.noBody(),Map.of());
            future=authority.dispatch(lease,op,() -> client.sendAsync(request,HttpResponse.BodyHandlers.ofInputStream()));
            var current=future; op.track(() -> current.cancel(true));
            var response=future.get(op.remaining().toNanos(),TimeUnit.NANOSECONDS);
            headerLimit(response.headers()); var input=op.track(response.body());
            if(response.statusCode()!=200) { byte[] error=input.readNBytes(64*1024+1); input.close(); success(response.statusCode(),error,lease); throw failure(); }
            String encoding=response.headers().firstValue("Content-Encoding").orElse("identity");
            if(!encoding.equalsIgnoreCase("identity")) throw failure();
            long length=response.headers().firstValueAsLong("Content-Length").orElse(-1);
            if(length>DriveFileResources.MAX_FILE || length < -1) throw failure();
            op.beginBody(); return new Media(input,length);
        } catch(InterruptedException e) { Thread.currentThread().interrupt(); throw DriveFileException.timeout(); }
        catch(TimeoutException | CancellationException e) { throw DriveFileException.timeout(); }
        catch(ExecutionException | IOException | IllegalArgumentException e) { op.check(); throw failure(); }
        finally { if(future!=null && !future.isDone()) future.cancel(true); }
    }

    private JsonNode json(DriveFileAuthority.Lease lease,DriveFileResources.Operation op,String method,URI uri,
            byte[] body,Map<String,String> headers,int limit,boolean transfer) {
        var publisher=body==null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofByteArray(body);
        var selected=body!=null && headers.isEmpty() ? Map.of("Content-Type","application/json; charset=UTF-8") : headers;
        return jsonPublisher(lease,op,method,uri,publisher,selected,limit,transfer);
    }
    private JsonNode jsonPublisher(DriveFileAuthority.Lease lease,DriveFileResources.Operation op,String method,URI uri,
            HttpRequest.BodyPublisher body,Map<String,String> headers,int limit,boolean transfer) {
        var response=send(lease,op,method,uri,body,headers,limit,transfer);
        success(response.statusCode(),response.body(),lease);
        try { JsonNode result=mapper.readTree(response.body()); if(result==null || !result.isObject()) throw failure(); return result; }
        catch(IOException e) { throw failure(); }
    }
    private HttpResponse<byte[]> send(DriveFileAuthority.Lease lease,DriveFileResources.Operation op,String method,URI uri,
            HttpRequest.BodyPublisher body,Map<String,String> headers,int limit,boolean transfer) {
        if(transfer) op.beginBody(); else op.metadataPhase();
        CompletableFuture<HttpResponse<byte[]>> future=null;
        try {
            var publisher=transfer ? progressPublisher(body,op) : body;
            var request=request(lease,op,method,uri,publisher,headers);
            future=authority.dispatch(lease,op,() -> client.sendAsync(request,info -> {
                headerLimit(info.headers());
                var delegate=HttpResponse.BodySubscribers.limiting(HttpResponse.BodySubscribers.ofByteArray(),limit);
                return new HttpResponse.BodySubscriber<byte[]>() {
                    public CompletionStage<byte[]> getBody(){return delegate.getBody();}
                    public void onSubscribe(Flow.Subscription sub){op.track(() -> sub.cancel());delegate.onSubscribe(sub);}
                    public void onNext(List<ByteBuffer> items){op.check();op.progress();delegate.onNext(items);}
                    public void onError(Throwable error){delegate.onError(error);}
                    public void onComplete(){delegate.onComplete();}
                };
            }));
            var current=future; op.track(() -> current.cancel(true));
            var result=future.get(op.remaining().toNanos(),TimeUnit.NANOSECONDS); op.check(); return result;
        } catch(InterruptedException e) { Thread.currentThread().interrupt(); throw DriveFileException.timeout(); }
        catch(TimeoutException | CancellationException e) { throw DriveFileException.timeout(); }
        catch(ExecutionException | IllegalArgumentException e) { op.check(); throw failure(); }
        finally { if(future!=null && !future.isDone()) future.cancel(true); op.endPhase(); }
    }
    private HttpRequest request(DriveFileAuthority.Lease lease,DriveFileResources.Operation op,String method,URI uri,HttpRequest.BodyPublisher body,Map<String,String> headers) {
        var builder=HttpRequest.newBuilder(uri).timeout(op.remaining()).header("Authorization","Bearer "+lease.token()).header("Accept-Encoding","identity");
        headers.forEach(builder::header); return builder.method(method,body).build();
    }
    private HttpRequest.BodyPublisher progressPublisher(HttpRequest.BodyPublisher body,DriveFileResources.Operation op) {
        return new HttpRequest.BodyPublisher() {
            public long contentLength(){return body.contentLength();}
            public void subscribe(Flow.Subscriber<? super ByteBuffer> subscriber) {
                body.subscribe(new Flow.Subscriber<>() {
                    public void onSubscribe(Flow.Subscription sub){op.track(() -> sub.cancel());subscriber.onSubscribe(sub);}
                    public void onNext(ByteBuffer value){op.check();op.progress();subscriber.onNext(value);}
                    public void onError(Throwable error){subscriber.onError(error);}
                    public void onComplete(){op.metadataPhase();subscriber.onComplete();}
                });
            }
        };
    }
    private void success(int status,byte[] body,DriveFileAuthority.Lease lease) {
        if(status>=200 && status<300) return;
        if(status==401) { authority.invalidate(lease); throw new DriveFileException("DRIVE_RECONNECT_REQUIRED",409); }
        if(status==429) throw new DriveFileException("DRIVE_PROVIDER_RATE_LIMITED",503);
        if(status==403) {
            try {
                var reasons=mapper.readTree(body).path("error").path("errors");
                if(reasons.isArray()) for(var value:reasons) if(Set.of("storageQuotaExceeded","userRateLimitExceeded","rateLimitExceeded","dailyLimitExceeded","downloadQuotaExceeded","quotaExceeded").contains(value.path("reason").asText())) {
                    throw new DriveFileException("DRIVE_PROVIDER_QUOTA",503);
                }
            } catch(IOException ignored) { /* no provider diagnostics retained */ }
        }
        if(status==403 || status==404) throw new DriveFileException("DRIVE_FILE_UNAVAILABLE",404);
        throw failure();
    }
    private void headerLimit(HttpHeaders headers) {
        long bytes=0;
        for(var header:headers.map().entrySet()) for(String value:header.getValue()) bytes+=header.getKey().getBytes(StandardCharsets.UTF_8).length+value.getBytes(StandardCharsets.UTF_8).length+4;
        if(bytes>16*1024) throw failure();
    }
    private String opaque(JsonNode result,String field,int max) {
        var value=result.get(field); if(value==null || !value.isTextual() || value.textValue().length()>max) throw failure();
        try { return DriveFileInputs.id(value.textValue()); } catch(DriveFileException e) { throw failure(); }
    }
    private URI uri(String path,String query) { return origin.resolve(path+"?"+query); }
    private String encode(String value) { return URLEncoder.encode(value,StandardCharsets.UTF_8); }
    private byte[] bytes(Object value) { try { return mapper.writeValueAsBytes(value); } catch(IOException e) { throw failure(); } }
    private DriveFileException failure() { return new DriveFileException("DRIVE_PROVIDER_FAILURE",502); }
    @Override public void close() { client.shutdownNow(); }
}

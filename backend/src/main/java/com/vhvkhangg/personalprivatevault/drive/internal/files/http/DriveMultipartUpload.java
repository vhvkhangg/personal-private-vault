package com.vhvkhangg.personalprivatevault.drive.internal.files.http;

import com.vhvkhangg.personalprivatevault.drive.internal.files.application.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Pattern;

/** One bounded file part; deliberately no multipart parameter/catalog or whole-file aggregation. */
public final class DriveMultipartUpload {
    public record Spool(Path path,String name,String mime,long size,String fingerprint,String md5) {
        @Override public String toString() { return "DriveUploadSpool[REDACTED]"; }
    }
    private static final Pattern BOUNDARY=Pattern.compile("^multipart/form-data;\\s*boundary=(?:\"([A-Za-z0-9'()+_,./:=?-]{1,70})\"|([A-Za-z0-9'()+_,./:=?-]{1,70}))$",Pattern.CASE_INSENSITIVE);
    private static final Pattern DISPOSITION=Pattern.compile("^form-data;\\s*name=\"file\";\\s*filename=\"([^\"]*)\"$",Pattern.CASE_INSENSITIVE);
    private DriveMultipartUpload() {}

    public static Spool read(InputStream source,String contentType,long declared,DriveFileResources.Operation op) {
        if(declared>DriveFileResources.MAX_REQUEST) throw new DriveFileException("PAYLOAD_TOO_LARGE",413);
        var matcher=BOUNDARY.matcher(contentType==null ? "" : contentType);
        if(!matcher.matches()) throw DriveFileException.input();
        String boundary=matcher.group(1)==null ? matcher.group(2) : matcher.group(1);
        op.beginBody();
        try {
            var input=new CountedInput(source,op);
            if(!line(input,80).equals("--"+boundary)) throw DriveFileException.input();
            Map<String,String> headers=new HashMap<>(); int total=0;
            while(true) {
                String line=line(input,16*1024); total+=line.getBytes(StandardCharsets.UTF_8).length+2;
                if(total>16*1024 || headers.size()>16) throw DriveFileException.input();
                if(line.isEmpty()) break;
                int colon=line.indexOf(':'); if(colon<1) throw DriveFileException.input();
                String key=line.substring(0,colon).toLowerCase(Locale.ROOT);
                if(!Set.of("content-disposition","content-type").contains(key) || headers.putIfAbsent(key,line.substring(colon+1).strip())!=null) throw DriveFileException.input();
            }
            var part=DISPOSITION.matcher(headers.getOrDefault("content-disposition",""));
            if(!part.matches()) throw DriveFileException.input();
            String name=DriveFileInputs.name(part.group(1));
            String mime=DriveFileInputs.mime(headers.get("content-type"));
            Path path=op.spoolFile();
            OutputStream destination;
            try{destination=Files.newOutputStream(path,StandardOpenOption.WRITE);}
            catch(IOException storage){throw DriveFileException.capacity();}
            try(var output=op.track(destination)) {
                var writer=new HashedFile(output,op);
                var parts=new PushbackInputStream(input,2);
                copyPart(parts,writer,("\r\n--"+boundary).getBytes(StandardCharsets.US_ASCII));
                if(parts.read()!='\r' || parts.read()!='\n' || parts.read()!=-1) throw DriveFileException.input();
                writer.flush();
                return new Spool(path,name,mime,writer.length,DriveFileInputs.fingerprint(name,mime,writer.length,writer.sha.digest()),HexFormat.of().formatHex(writer.md5.digest()));
            }
        } catch(IOException e) {
            op.check(); throw DriveFileException.input();
        } finally { op.endPhase(); }
    }

    private static String line(InputStream in,int max) throws IOException {
        var bytes=new ByteArrayOutputStream();
        int value;
        while((value=in.read())!=-1) {
            if(value=='\r') { if(in.read()!='\n') throw DriveFileException.input(); return bytes.toString(StandardCharsets.UTF_8); }
            if(value=='\n' || bytes.size()>=max || value==0) throw DriveFileException.input();
            bytes.write(value);
        }
        throw DriveFileException.input();
    }

    private static void copyPart(PushbackInputStream input,HashedFile output,byte[] delimiter) throws IOException {
        int[] prefix=new int[delimiter.length];
        for(int i=1,j=0;i<delimiter.length;i++) { while(j>0 && delimiter[i]!=delimiter[j]) j=prefix[j-1]; if(delimiter[i]==delimiter[j]) j++; prefix[i]=j; }
        int matched=0,value;
        while((value=input.read())!=-1) {
            byte b=(byte)value;
            while(matched>0 && b!=delimiter[matched]) {
                int fallback=prefix[matched-1];
                for(int i=0;i<matched-fallback;i++) output.put(delimiter[i]);
                matched=fallback;
            }
            if(b==delimiter[matched]) {
                if(++matched==delimiter.length) {
                    int one=input.read(),two=input.read();
                    if(one=='-' && two=='-')return;
                    if(one=='\r' && two=='\n')throw DriveFileException.input();
                    for(byte pending:delimiter)output.put(pending);
                    matched=0;
                    if(two!=-1)input.unread(two);if(one!=-1)input.unread(one);
                }
            }
            else output.put(b);
        }
        throw DriveFileException.input();
    }

    private static class CountedInput extends InputStream {
        private final InputStream input;
        private final DriveFileResources.Operation op;
        private final byte[] buffer=new byte[DriveFileResources.BUFFER];
        private long length; private int position,available;
        CountedInput(InputStream input,DriveFileResources.Operation op) { this.input=input; this.op=op; }
        @Override public int read() throws IOException {
            if(position==available) {
                op.check(); int count=input.read(buffer); if(count<0) return -1;
                length+=count; if(length>DriveFileResources.MAX_REQUEST) throw new DriveFileException("PAYLOAD_TOO_LARGE",413);
                position=0; available=count; op.progress();
                if(count==0) return read();
            }
            return buffer[position++] & 255;
        }
    }

    private static class HashedFile {
        final java.security.MessageDigest sha=DriveFileInputs.digest("SHA-256"),md5=DriveFileInputs.digest("MD5");
        final OutputStream out; final DriveFileResources.Operation op; final byte[] buffer=new byte[DriveFileResources.BUFFER];
        long length; int position;
        HashedFile(OutputStream out,DriveFileResources.Operation op) { this.out=out; this.op=op; }
        void put(byte value) throws IOException {
            if(++length>DriveFileResources.MAX_FILE) throw new DriveFileException("PAYLOAD_TOO_LARGE",413);
            buffer[position++]=value; if(position==buffer.length) flush();
        }
        void flush() throws IOException {
            if(position==0)return;op.check();sha.update(buffer,0,position);md5.update(buffer,0,position);
            try{out.write(buffer,0,position);}catch(IOException storage){throw DriveFileException.capacity();}
            position=0;op.progress();
        }
    }
}

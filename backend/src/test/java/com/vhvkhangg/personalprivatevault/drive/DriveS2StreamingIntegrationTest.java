package com.vhvkhangg.personalprivatevault.drive;

import com.vhvkhangg.personalprivatevault.drive.fixture.*;
import com.vhvkhangg.personalprivatevault.drive.internal.files.http.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.io.*;
import java.net.*;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import static org.assertj.core.api.Assertions.*;

class DriveS2StreamingIntegrationTest extends DriveS2HttpTestSupport {
    @Test void exact50MiBUsesResumableWireAndDownloadStreamsTheExactOriginal() throws Exception {
        ensure(1);Path source=Files.createTempFile("s2-edge-",".bin");
        try {
            try(var file=new RandomAccessFile(source.toFile(),"rw")){file.setLength(DriveFileResources.MAX_FILE);file.seek(DriveFileResources.MAX_FILE-1);file.write(123);}
            var response=send(multipart(1,key(),HttpRequest.BodyPublishers.ofFile(source),"edge.bin","application/octet-stream",true));
            assertThat(response.statusCode()).as(new String(response.body(),StandardCharsets.UTF_8)).isEqualTo(201);
            assertThat(GOOGLE.uploadSessions).hasValue(1);assertThat(GOOGLE.putCalls).hasValue(1);assertThat(GOOGLE.fileCreates).hasValue(1);
            String id=data(response).path("id").asText();assertThat(data(response).path("sizeBytes").asLong()).isEqualTo(DriveFileResources.MAX_FILE);
            var downloaded=client.send(request(path(1)+"/files/"+id+"/download").GET().build(),HttpResponse.BodyHandlers.ofInputStream());
            assertThat(downloaded.statusCode()).isEqualTo(200);
            var expected=java.security.MessageDigest.getInstance("SHA-256");try(var input=Files.newInputStream(source)){byte[] b=new byte[65536];int n;while((n=input.read(b))!=-1)expected.update(b,0,n);}
            var actual=java.security.MessageDigest.getInstance("SHA-256");long length=0;
            try(var input=downloaded.body()){byte[] b=new byte[65536];int n;while((n=input.read(b))!=-1){actual.update(b,0,n);length+=n;}}
            assertThat(length).isEqualTo(DriveFileResources.MAX_FILE);assertThat(actual.digest()).isEqualTo(expected.digest());
        }finally{Files.deleteIfExists(source);}
    }
    @ParameterizedTest @ValueSource(booleans={false,true})
    void fileOver50MiBRejectsBeforeAnyProviderMutationWithKnownOrChunkedRequest(boolean chunked) throws Exception {
        ensure(1);Path source=Files.createTempFile("s2-too-big-",".bin");
        try {
            try(var file=new RandomAccessFile(source.toFile(),"rw")){file.setLength(DriveFileResources.MAX_FILE+1);}
            var response=send(multipart(1,key(),HttpRequest.BodyPublishers.ofFile(source),"too-big.bin","application/octet-stream",!chunked));
            assertThat(response.statusCode()).isEqualTo(413);assertThat(GOOGLE.fileCreates).hasValue(0);assertThat(GOOGLE.uploadSessions).hasValue(0);
            assertThat(jdbc.queryForObject("SELECT count(*) FROM drive_upload_intents",Integer.class)).isZero();
        }finally{Files.deleteIfExists(source);}
    }
    @Test void declaredRequestOver55MiBIsRejectedBeforeParsingOrProviderActivity() throws Exception {
        try(var socket=new Socket("localhost",port)){
            socket.setSoTimeout(3000);String header="POST "+path(1)+"/files HTTP/1.1\r\nHost: localhost\r\nAuthorization: Bearer "+validBearerToken()+"\r\nContent-Type: multipart/form-data; boundary=test\r\nContent-Length: "+(DriveFileResources.MAX_REQUEST+1)+"\r\nConnection: close\r\n\r\n";
            socket.getOutputStream().write(header.getBytes(StandardCharsets.US_ASCII));socket.getOutputStream().flush();
            assertThat(new BufferedReader(new InputStreamReader(socket.getInputStream(),StandardCharsets.US_ASCII)).readLine()).contains("413");
        }
        assertThat(GOOGLE.calls).isEmpty();
    }
    @Test void zeroByteAndBoundaryLikeBinaryContentRemainExact() throws Exception {
        ensure(1);byte[] boundaryLike=("prefix\r\n--s2_test_boundaryXQ\r\n--s2_test_boundar\r\n--s2_test_boundary-Zsuffix").getBytes(StandardCharsets.US_ASCII);
        for(byte[] bytes:List.of(new byte[0],boundaryLike)){
            var response=upload(1,key(),bytes,"binary.bin","invalid mime");assertThat(response.statusCode()).isEqualTo(201);
            assertThat(data(response).path("mimeType").asText()).isEqualTo("application/octet-stream");
            String id=data(response).path("id").asText();
            var download=send(request(path(1)+"/files/"+id+"/download").GET());assertThat(download.statusCode()).isEqualTo(200);assertThat(download.body()).isEqualTo(bytes);
        }
    }
    @ParameterizedTest @ValueSource(strings={"short","extra","oversizeHeader","wrongLength","permission"})
    void mediaProofFailureNeverReturnsACompleteSuccessfulAttachment(String failure) throws Exception {
        String id=uploaded(new byte[3*65536],"proof.bin");
        switch(failure){
            case "short" -> {GOOGLE.mediaChunked=true;GOOGLE.shortMedia=65536;}
            case "extra" -> {GOOGLE.mediaChunked=true;GOOGLE.extraMedia=1;}
            case "oversizeHeader" -> GOOGLE.mediaLengthDelta=DriveFileResources.MAX_FILE;
            case "wrongLength" -> GOOGLE.mediaLengthDelta=1;
            case "permission" -> GOOGLE.mediaStatus=403;
        }
        try{
            var response=send(request(path(1)+"/files/"+id+"/download").GET());
            assertThat(response.statusCode()).isNotEqualTo(200);
            assertThat(new String(response.body(),StandardCharsets.UTF_8)).doesNotContain("fileNotDownloadable","access-");
        }catch(IOException truncated){assertThat(failure).isIn("short","extra");}
    }
    @ParameterizedTest @ValueSource(strings={"HEADERS_ONLY","PARTIAL","TRICKLE"})
    void stalledOrTrickledMediaIsCancelledWithinInjectedBodyBoundsAndCleansUp(String mode) throws Exception {
        String id=uploaded(new byte[3*65536],"slow.bin");GOOGLE.mediaMode=FakeGoogleDriveServer.Mode.valueOf(mode);GOOGLE.bodyRelease=new CountDownLatch(1);GOOGLE.mediaChunked=true;
        long start=System.nanoTime();
        try{var response=send(request(path(1)+"/files/"+id+"/download").GET());assertThat(response.statusCode()).isNotEqualTo(200);}catch(IOException truncated){/* verified Content-Length makes committed abort visible */}
        var elapsed=java.time.Duration.ofNanos(System.nanoTime()-start);
        assertThat(elapsed).isLessThan(java.time.Duration.ofSeconds(18));
        if(mode.equals("TRICKLE"))assertThat(elapsed).isGreaterThanOrEqualTo(resources.limits().transfer());
        GOOGLE.bodyRelease.countDown();awaitClean();
    }
    @ParameterizedTest @ValueSource(booleans={false,true})
    void continuouslyProgressingPartialReadsOutlastIdleButFinishWithinTransferDeadline(boolean chunked) throws Exception {
        byte[] bytes=new byte[100];for(int i=0;i<bytes.length;i++)bytes[i]=(byte)i;
        String id=uploaded(bytes,"progress.bin");
        GOOGLE.mediaMode=FakeGoogleDriveServer.Mode.TRICKLE;GOOGLE.bodyRelease=new CountDownLatch(1);GOOGLE.mediaChunked=chunked;
        long start=System.nanoTime();
        var response=send(request(path(1)+"/files/"+id+"/download").GET());
        var elapsed=java.time.Duration.ofNanos(System.nanoTime()-start);
        assertThat(response.statusCode()).isEqualTo(200);assertThat(response.body()).isEqualTo(bytes);
        assertThat(elapsed).isGreaterThan(resources.limits().idle()).isLessThan(resources.limits().transfer());
        assertThat(GOOGLE.mediaCalls).hasValue(1);awaitClean();
    }
    @Test void admissionPrecedesMultipartParsingAndS1RemainsAvailableWhileTwoBodiesStall() throws Exception {
        try(var first=partialUpload();var second=partialUpload()){
            long until=System.nanoTime()+TimeUnit.SECONDS.toNanos(1);
            while(resources.activeOperations()<2 && System.nanoTime()<until)new CountDownLatch(1).await(10,TimeUnit.MILLISECONDS);
            assertThat(resources.activeOperations()).isEqualTo(2);
            var rejected=upload(1,key(),new byte[]{1},"third.bin","application/octet-stream");assertThat(rejected.statusCode()).isEqualTo(503);
            assertThat(send(request("/api/v1/drive/connections").GET()).statusCode()).isEqualTo(200);
            assertThat(GOOGLE.calls).isEmpty();
        }
        awaitClean();
    }
    @Test void clientDisconnectDuringUploadReleasesAdmissionAndOwnedSpool() throws Exception {
        try(var socket=partialUpload()){
            long until=System.nanoTime()+TimeUnit.SECONDS.toNanos(1);
            while(resources.activeSpools()==0 && System.nanoTime()<until)new CountDownLatch(1).await(10,TimeUnit.MILLISECONDS);
            assertThat(resources.activeSpools()).isEqualTo(1);
        }
        awaitClean();assertThat(GOOGLE.fileCreates).hasValue(0);
    }
    @Test void disconnectWhileMediaIsPausedCannotCompleteAnAuthorizedAttachment() throws Exception {
        String id=uploaded(new byte[3*65536],"revoked.bin");
        GOOGLE.mediaMode=FakeGoogleDriveServer.Mode.PARTIAL;GOOGLE.bodyRelease=new CountDownLatch(1);GOOGLE.mediaChunked=true;
        var response=client.send(request(path(1)+"/files/"+id+"/download").GET().build(),HttpResponse.BodyHandlers.ofInputStream());
        assertThat(response.statusCode()).isEqualTo(200);
        try(var input=response.body()){
            assertThat(input.readNBytes(1024)).hasSize(1024);s1.disconnect(1);GOOGLE.bodyRelease.countDown();
            assertThatThrownBy(() -> {byte[] buffer=new byte[65536];while(input.read(buffer)!=-1){}}).isInstanceOf(IOException.class);
        }
        awaitClean();assertThat(GOOGLE.mediaCalls).hasValue(1);
    }
    @Test void slowDownloadReaderAndClientCancellationReleaseBoundedResources() throws Exception {
        ensure(1);Path source=Files.createTempFile("s2-reader-",".bin");
        try{
            try(var file=new RandomAccessFile(source.toFile(),"rw")){file.setLength(8*1024*1024);}
            var uploaded=send(multipart(1,key(),HttpRequest.BodyPublishers.ofFile(source),"reader.bin","application/octet-stream",true));
            assertThat(uploaded.statusCode()).isEqualTo(201);String id=data(uploaded).path("id").asText();
            try(var socket=new Socket()){
                socket.setReceiveBufferSize(1024);socket.connect(new InetSocketAddress("localhost",port));socket.setSoTimeout(3000);
                String header="GET "+path(1)+"/files/"+id+"/download HTTP/1.1\r\nHost: localhost\r\nAuthorization: Bearer "+validBearerToken()+"\r\n\r\n";
                socket.getOutputStream().write(header.getBytes(StandardCharsets.US_ASCII));socket.getOutputStream().flush();
                assertThat(socket.getInputStream().readNBytes(20)).isNotEmpty();awaitClean();
            }
            var response=client.send(request(path(1)+"/files/"+id+"/download").GET().build(),HttpResponse.BodyHandlers.ofInputStream());
            assertThat(response.body().readNBytes(1024)).hasSize(1024);response.body().close();awaitClean();
        }finally{Files.deleteIfExists(source);}
    }
    private Socket partialUpload() throws Exception {
        var socket=new Socket("localhost",port);socket.setSoTimeout(3000);
        String part="--partial\r\nContent-Disposition: form-data; name=\"file\"; filename=\"pending.bin\"\r\nContent-Type: application/octet-stream\r\n\r\n"+"x".repeat(128);
        String header="POST "+path(1)+"/files HTTP/1.1\r\nHost: localhost\r\nAuthorization: Bearer "+validBearerToken()+"\r\nIdempotency-Key: "+key()+"\r\nContent-Type: multipart/form-data; boundary=partial\r\nContent-Length: 1000000\r\n\r\n";
        socket.getOutputStream().write((header+part).getBytes(StandardCharsets.US_ASCII));socket.getOutputStream().flush();return socket;
    }
}

package com.vhvkhangg.personalprivatevault.drive.fixture;

import com.fasterxml.jackson.databind.*;
import com.vhvkhangg.personalprivatevault.authentication.internal.application.token.JwtTokenService;
import com.vhvkhangg.personalprivatevault.drive.internal.application.DriveConnectionService;
import com.vhvkhangg.personalprivatevault.drive.internal.files.application.*;
import com.vhvkhangg.personalprivatevault.drive.internal.files.http.*;
import com.vhvkhangg.personalprivatevault.drive.internal.oauth.*;
import com.vhvkhangg.personalprivatevault.support.AbstractPostgresIntegrationTest;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.*;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.*;
import org.springframework.transaction.support.TransactionTemplate;
import java.io.*;
import java.net.*;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(DriveS2HttpTestSupport.Controlled.class)
@Timeout(40)
public abstract class DriveS2HttpTestSupport extends AbstractPostgresIntegrationTest {
    protected static final Runtime RUNTIME=createRuntime();
    protected static final FakeGoogleOAuthServer OAUTH=RUNTIME.oauth;
    protected static final FakeGoogleDriveServer GOOGLE=RUNTIME.google;
    protected static final FakeGoogleThumbnailServer THUMBNAIL=RUNTIME.thumbnail;
    protected final ObjectMapper mapper=new ObjectMapper().findAndRegisterModules();
    protected final HttpClient client=HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).version(HttpClient.Version.HTTP_1_1).build();
    @LocalServerPort protected int port;
    @Autowired protected JdbcTemplate jdbc;
    @Autowired protected JwtTokenService jwt;
    @Autowired protected DriveConnectionService s1;
    @Autowired protected SubjectLifecycleManager lifecycle;
    @Autowired protected OAuthAttemptManager attempts;
    @Autowired protected DriveFileResources resources;
    @Autowired protected DriveFileService service;
    @Autowired protected org.springframework.security.oauth2.jwt.JwtEncoder encoder;
    @Autowired protected org.springframework.transaction.PlatformTransactionManager fixtureTransactionManager;
    @Autowired(required = false) protected com.vhvkhangg.personalprivatevault.authentication.bootstrap.BootstrapOperations bootstrapOperations;
    @Autowired(required = false) protected com.vhvkhangg.personalprivatevault.authentication.session.SessionOperations sessionOperations;
    private String cachedBearerToken;

    @DynamicPropertySource static void properties(DynamicPropertyRegistry registry){
        registry.add("ppv.drive.enabled",() -> "true");registry.add("ppv.drive.files.enabled",() -> "true");
        registry.add("ppv.drive.token-key-file",() -> RUNTIME.key.toString());registry.add("ppv.drive.files.spool-dir",() -> RUNTIME.spool.toString());
        registry.add("ppv.drive.oauth.client-id",() -> "s2-http-client");registry.add("ppv.drive.oauth.client-secret",() -> "synthetic-secret");
    }
    @BeforeEach void clean() throws Exception {
        cachedBearerToken = null;
        awaitClean();jdbc.update("DELETE FROM drive_child_folder_intents");jdbc.update("DELETE FROM drive_upload_intents");jdbc.update("DELETE FROM drive_folder_roots");jdbc.update("DELETE FROM drive_connections");
        lifecycle.initializeFromDatabase(List.of());attempts.invalidateSlotAttempts((short)1);attempts.invalidateSlotAttempts((short)2);
        OAUTH.reset();OAUTH.omitRefreshTokenOnRefresh=true;GOOGLE.reset();connect(1,"U");connect(2,"V");
    }
    @AfterEach void finish() throws Exception {GOOGLE.bodyRelease.countDown();if(GOOGLE.uploadRelease!=null)GOOGLE.uploadRelease.countDown();awaitClean();client.shutdownNow();}
    protected void awaitClean() throws Exception {
        long until=System.nanoTime()+TimeUnit.SECONDS.toNanos(4);
        while(resources.activeOperations()!=0 && System.nanoTime()<until)new CountDownLatch(1).await(10,TimeUnit.MILLISECONDS);
        assertThat(resources.activeOperations()).isZero();assertThat(resources.activeSpools()).isZero();
        try(var paths=Files.newDirectoryStream(RUNTIME.spool)){assertThat(paths.iterator().hasNext()).isFalse();}
    }
    protected void connect(int slot,String sub) {
        var consent=s1.authorize(slot,"1");String url=consent.response().authorizationUrl(),code=UUID.randomUUID().toString();
        OAUTH.issueCode(code,sub,param(url,"nonce"),"refresh-"+code);
        assertThat(s1.callback(param(url,"state"),code,null,null,consent.cookieValue()).success()).isTrue();
    }
    private String param(String value,String key){return value.split(key+"=")[1].split("&")[0];}
    protected URI uri(String path){return URI.create("http://localhost:"+port+path);}
    protected String validBearerToken() {
        if (cachedBearerToken != null) {
            return cachedBearerToken;
        }
        if (bootstrapOperations != null && !bootstrapOperations.isBootstrapped()) {
            bootstrapOperations.bootstrap(new com.vhvkhangg.personalprivatevault.authentication.bootstrap.BootstrapCommand(
                    "owner@vault.local", "owner", "VaultPass123!", "123456"
            ));
        }
        cachedBearerToken = sessionOperations.login(new com.vhvkhangg.personalprivatevault.authentication.session.LoginCommand(
                "owner", "VaultPass123!"
        )).accessToken();
        return cachedBearerToken;
    }
    protected HttpRequest.Builder request(String path){return HttpRequest.newBuilder(uri(path)).timeout(Duration.ofSeconds(25)).header("Authorization","Bearer "+validBearerToken());}
    protected HttpResponse<byte[]> send(HttpRequest.Builder request) throws Exception{
        var future=client.sendAsync(request.build(),HttpResponse.BodyHandlers.ofByteArray());
        try{return future.get(24,TimeUnit.SECONDS);}
        catch(TimeoutException e){future.cancel(true);throw new AssertionError("Synthetic client full-response deadline expired",e);}
        catch(ExecutionException e){if(e.getCause() instanceof IOException io)throw io;throw e;}
    }
    protected String path(int slot){return "/api/v1/drive/connections/"+slot;}
    protected String key(){return DateTimeFormatter.ofPattern("uuuuMMdd'T'HHmmss'Z'").withZone(ZoneOffset.UTC).format(Instant.now())+"."+UUID.randomUUID();}
    protected void ensure(int slot) throws Exception {var result=send(request(path(slot)+"/root-folder").POST(HttpRequest.BodyPublishers.noBody()));assertThat(result.statusCode()).as(new String(result.body(),StandardCharsets.UTF_8)).isEqualTo(200);}
    protected HttpResponse<byte[]> upload(int slot,String key,byte[] bytes,String name,String mime) throws Exception {
        return send(multipart(slot,key,HttpRequest.BodyPublishers.ofByteArray(bytes),name,mime,true));
    }
    protected HttpRequest.Builder multipart(int slot,String key,HttpRequest.BodyPublisher body,String name,String mime,boolean knownLength){
        String boundary="s2_test_boundary";
        var prefix=HttpRequest.BodyPublishers.ofByteArray(("--"+boundary+"\r\nContent-Disposition: form-data; name=\"file\"; filename=\""+name+"\"\r\nContent-Type: "+mime+"\r\n\r\n").getBytes(StandardCharsets.UTF_8));
        var suffix=HttpRequest.BodyPublishers.ofByteArray(("\r\n--"+boundary+"--\r\n").getBytes(StandardCharsets.US_ASCII));
        var combined=HttpRequest.BodyPublishers.concat(prefix,body,suffix);
        return request(path(slot)+"/files").header("Idempotency-Key",key).header("Content-Type","multipart/form-data; boundary="+boundary)
                .POST(knownLength?combined:HttpRequest.BodyPublishers.fromPublisher(combined));
    }
    protected JsonNode data(HttpResponse<byte[]> response) throws Exception{return mapper.readTree(response.body()).path("data");}
    protected String uploaded(byte[] bytes,String name) throws Exception {
        ensure(1);var result=upload(1,key(),bytes,name,"application/octet-stream");assertThat(result.statusCode()).as(new String(result.body(),StandardCharsets.UTF_8)).isEqualTo(201);return data(result).path("id").asText();
    }
    private static Runtime createRuntime(){try{return new Runtime();}catch(Exception e){throw new ExceptionInInitializerError(e);}}
    protected static class Runtime implements AutoCloseable {
        final FakeGoogleOAuthServer oauth=new FakeGoogleOAuthServer();
        final FakeGoogleDriveServer google=new FakeGoogleDriveServer(oauth.accessTokenSubjects::get);
        final FakeGoogleThumbnailServer thumbnail=new FakeGoogleThumbnailServer();
        final Path key=Files.createTempFile("s2-http-key-",".bin"),spool=Files.createTempDirectory("s2-http-spool-");
        Runtime() throws Exception{byte[] bytes=new byte[32];new java.security.SecureRandom().nextBytes(bytes);Files.write(key,bytes);}
        public void close() throws Exception {google.close();oauth.close();thumbnail.close();Files.deleteIfExists(key);try(var paths=Files.newDirectoryStream(spool)){for(Path p:paths)Files.deleteIfExists(p);}Files.deleteIfExists(spool);}
    }
    @TestConfiguration(proxyBeanMethods=false)
    public static class Controlled {
        @Bean Runtime controlledRuntime(){return RUNTIME;}
        @Bean @Primary DriveOAuthClient controlledOAuth(Clock clock){return new DriveOAuthClient("s2-http-client","synthetic-secret",OAUTH.getTokenEndpoint(),OAUTH.getRevokeEndpoint(),OAUTH.getJwksUri(),clock,null);}
        @Bean @Primary DriveFileHttpClient controlledDrive(DriveFileAuthority authority){return new DriveFileHttpClient(authority,GOOGLE.origin(),RUNTIME.thumbnail.origin());}
        @Bean @Primary DriveFileResources controlledResources(){return new DriveFileResources(RUNTIME.spool,
                new DriveFileResources.Limits(Duration.ofSeconds(4),Duration.ofSeconds(15),Duration.ofSeconds(2),Duration.ofSeconds(22)));}
    }
}

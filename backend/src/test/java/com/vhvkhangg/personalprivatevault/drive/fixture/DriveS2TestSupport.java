package com.vhvkhangg.personalprivatevault.drive.fixture;

import com.vhvkhangg.personalprivatevault.drive.internal.application.DriveConnectionService;
import com.vhvkhangg.personalprivatevault.drive.internal.config.DriveProperties;
import com.vhvkhangg.personalprivatevault.drive.internal.crypto.DriveCredentialCrypto;
import com.vhvkhangg.personalprivatevault.drive.internal.files.application.*;
import com.vhvkhangg.personalprivatevault.drive.internal.files.http.*;
import com.vhvkhangg.personalprivatevault.drive.internal.files.persistence.*;
import com.vhvkhangg.personalprivatevault.drive.internal.oauth.*;
import com.vhvkhangg.personalprivatevault.drive.internal.persistence.DriveConnectionRepository;
import com.vhvkhangg.personalprivatevault.support.AbstractPostgresIntegrationTest;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import java.io.*;
import java.nio.file.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import static org.assertj.core.api.Assertions.*;

@Timeout(20)
public abstract class DriveS2TestSupport extends AbstractPostgresIntegrationTest {
    @Autowired protected JdbcTemplate jdbc;
    @Autowired protected DriveConnectionRepository connections;
    @Autowired protected DriveFolderRootRepository roots;
    @Autowired protected DriveUploadIntentRepository intents;
    @Autowired protected DriveChildFolderIntentRepository childFoldersRepo;
    @Autowired protected PlatformTransactionManager transactions;
    protected FakeGoogleOAuthServer oauthServer;
    protected FakeGoogleDriveServer google;
    protected DriveOAuthClient oauth;
    protected SubjectLifecycleManager lifecycle;
    protected DriveCredentialCrypto crypto;
    protected DriveConnectionService s1;
    protected DriveFileResources resources;
    protected DriveFileAuthority authority;
    protected DriveFolderService folders;
    protected DriveFileHttpClient provider;
    protected DriveUploadIntentStore store;
    protected DriveFileService service;
    protected MutableClock clock;
    protected Path spool;
    @BeforeEach void fixture() throws Exception {
        jdbc.update("DELETE FROM drive_child_folder_intents");jdbc.update("DELETE FROM drive_upload_intents");jdbc.update("DELETE FROM drive_folder_roots");jdbc.update("DELETE FROM drive_connections");
        oauthServer=new FakeGoogleOAuthServer();oauthServer.omitRefreshTokenOnRefresh=true;
        google=new FakeGoogleDriveServer(oauthServer.accessTokenSubjects::get);
        clock=new MutableClock(Instant.now());byte[] key=new byte[32];new java.security.SecureRandom().nextBytes(key);crypto=new DriveCredentialCrypto(key);
        oauth=new DriveOAuthClient("s2-client","synthetic-secret",oauthServer.getTokenEndpoint(),oauthServer.getRevokeEndpoint(),oauthServer.getJwksUri(),clock,null);
        spool=Files.createTempDirectory("gdi26-spool-");restart(DriveFileResources.Limits.production());
    }
    protected void restart(DriveFileResources.Limits limits) {
        if(provider!=null)provider.close();if(resources!=null)resources.close();
        var properties=new DriveProperties();properties.getOauth().setClientId("s2-client");properties.getOauth().setClientSecret("synthetic-secret");
        var attempts=new OAuthAttemptManager(clock);
        // S1 and file operations share precisely this lifecycle guard and current credential repository.
        lifecycle=new SubjectLifecycleManager(attempts);lifecycle.initializeFromDatabase(connections.findAll());
        s1=new DriveConnectionService(properties,connections,crypto,attempts,lifecycle,oauth,clock,transactions);
        resources=new DriveFileResources(spool,limits);authority=new DriveFileAuthority(connections,roots,crypto,lifecycle,oauth,clock,transactions);
        provider=new DriveFileHttpClient(authority,google.origin());folders=new DriveFolderService(authority,provider,roots,clock);
        store=new DriveUploadIntentStore(authority,roots,intents,clock);service=new DriveFileService(authority,folders,provider,new DriveFileCursors(clock),store,childFoldersRepo);
    }
    protected void connect(int slot,String sub) {
        var auth=s1.authorize(slot,"1");String url=auth.response().authorizationUrl(),code=UUID.randomUUID().toString();
        oauthServer.issueCode(code,sub,param(url,"nonce"),"refresh-"+code);
        assertThat(s1.callback(param(url,"state"),code,null,null,auth.cookieValue()).success()).isTrue();
    }
    private String param(String url,String key){return url.split(key+"=")[1].split("&")[0];}
    protected DriveFileService.Result<DriveFileService.RootStatus> ensure(int slot) {try(var op=resources.open(false)){return service.ensure("1",(short)slot,op);}}
    protected String key(){return DateTimeFormatter.ofPattern("uuuuMMdd'T'HHmmss'Z'").withZone(ZoneOffset.UTC).format(clock.instant())+"."+UUID.randomUUID();}
    protected DriveMultipartUpload.Spool raw(byte[] bytes,String name,String mime,DriveFileResources.Operation op) throws IOException {
        Path path=op.spoolFile();Files.write(path,bytes);
        return new DriveMultipartUpload.Spool(path,name,mime,bytes.length,DriveFileInputs.fingerprint(name,mime,bytes.length,DriveFileInputs.digest("SHA-256").digest(bytes)),
                HexFormat.of().formatHex(DriveFileInputs.digest("MD5").digest(bytes)));
    }
    protected DriveFileService.Result<DriveFileProof.FileItem> upload(int slot,String key,byte[] bytes,String name,String mime) throws IOException {
        try(var op=resources.open(true)){return service.upload("1",(short)slot,key,raw(bytes,name,mime,op),op);}
    }
    protected DriveFileService.Result<DriveFileProof.FileItem> upload(int slot,String key,byte[] bytes) throws IOException {return upload(slot,key,bytes,"original.bin","application/octet-stream");}
    protected void entered(CountDownLatch latch) throws InterruptedException {assertThat(latch.await(3,TimeUnit.SECONDS)).isTrue();}
    protected Throwable failure(Future<?> future) throws Exception {try{future.get(8,TimeUnit.SECONDS);return null;}catch(ExecutionException e){return e.getCause();}}
    @AfterEach void release() throws Exception {
        if(provider!=null)provider.close();if(resources!=null)resources.close();if(oauth!=null)oauth.close();
        if(google!=null)google.close();if(oauthServer!=null)oauthServer.close();
        if(spool!=null){try(var paths=Files.newDirectoryStream(spool)){for(Path p:paths)Files.deleteIfExists(p);}Files.deleteIfExists(spool);}
    }
    public static class MutableClock extends Clock {
        private volatile Instant now;
        public MutableClock(Instant now){this.now=now;}
        public void advance(Duration duration){now=now.plus(duration);}
        public Instant instant(){return now;}public ZoneId getZone(){return ZoneOffset.UTC;}public Clock withZone(ZoneId zone){return this;}
    }
}

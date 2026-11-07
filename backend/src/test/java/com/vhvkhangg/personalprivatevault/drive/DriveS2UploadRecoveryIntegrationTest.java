package com.vhvkhangg.personalprivatevault.drive;

import com.vhvkhangg.personalprivatevault.drive.fixture.DriveS2TestSupport;
import com.vhvkhangg.personalprivatevault.drive.internal.files.application.*;
import com.vhvkhangg.personalprivatevault.drive.internal.files.http.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.util.concurrent.*;
import static org.assertj.core.api.Assertions.*;

class DriveS2UploadRecoveryIntegrationTest extends DriveS2TestSupport {
    @Test void lostResponseAndRestartKeepsOneProviderIdentity() throws Exception {
        connect(1,"U");ensure(1);String key=key();byte[] bytes={0,1,2,3};
        google.loseUploadResponse=true;
        assertThatThrownBy(() -> upload(1,key,bytes)).isInstanceOfSatisfying(DriveFileException.class,e -> assertThat(e.getErrorCode()).isEqualTo("UPLOAD_OUTCOME_UNKNOWN"));
        var original=intents.findAll().getFirst();String id=original.getProviderFileId();
        assertThat(original.getStatus()).isEqualTo("RECONCILIATION_REQUIRED");
        // Deliberate test-only identity-loss control: actually allocate and create G after accepted F.
        // This must fail the same one-provider-mutation assertion below, not a synthetic assertion.
        if(Boolean.getBoolean("gdi26.s2.test.reallocateOnRetry"))jdbc.update("DELETE FROM drive_upload_intents");
        restart(DriveFileResources.Limits.production());
        var replay=upload(1,key,bytes);
        assertThat(google.fileCreates).as("one remote mutation across ambiguous response and restart").hasValue(1);
        assertThat(replay.data().id()).isEqualTo(id);assertThat(replay.created()).isFalse();
        assertThat(intents.findAll().getFirst().getStatus()).isEqualTo("COMPLETED");
        restart(DriveFileResources.Limits.production());
        assertThat(upload(1,key,bytes).data().id()).isEqualTo(id); // also lost PPV response / completed replay
        assertThat(google.generated).hasValue(2);assertThat(google.fileCreates).hasValue(1);
        assertThat(google.calls.stream().filter(c -> c.path().equals("/upload/drive/v3/files") && c.method().equals("POST")).count()).isEqualTo(1);
    }
    @Test void repeatedLosersCannotReleaseTheLiveWinnersClaim() throws Exception {
        connect(1,"U");ensure(1);String key=key();google.uploadEntered=new CountDownLatch(1);google.uploadRelease=new CountDownLatch(1);
        try(var workers=Executors.newVirtualThreadPerTaskExecutor()) {
            var winner=workers.submit(() -> upload(1,key,new byte[]{4}));entered(google.uploadEntered);
            for(int i=0;i<3;i++)assertThatThrownBy(() -> upload(1,key,new byte[]{4})).isInstanceOfSatisfying(DriveFileException.class,e -> assertThat(e.getErrorCode()).isEqualTo("UPLOAD_IN_PROGRESS"));
            google.uploadRelease.countDown();assertThat(winner.get(3,TimeUnit.SECONDS).created()).isTrue();
        }finally{google.uploadRelease.countDown();}
        assertThat(google.fileCreates).hasValue(1);assertThat(upload(1,key,new byte[]{4}).created()).isFalse();
    }
    @ParameterizedTest @ValueSource(strings={"bytes","name","mime"})
    void fingerprintChangesConflictWithoutAnyNewIdentity(String change) throws Exception {
        connect(1,"U");ensure(1);String key=key();upload(1,key,new byte[]{7});
        assertThatThrownBy(() -> upload(1,key,change.equals("bytes")?new byte[]{8}:new byte[]{7},
                change.equals("name")?"other.bin":"original.bin",change.equals("mime")?"application/pdf":"application/octet-stream"))
                .isInstanceOfSatisfying(DriveFileException.class,e -> assertThat(e.getErrorCode()).isEqualTo("UPLOAD_KEY_CONFLICT"));
        assertThat(google.generated).hasValue(2);assertThat(google.fileCreates).hasValue(1);
    }
    @ParameterizedTest @ValueSource(strings={"missing","checksum","action","created","parent"})
    void ambiguousCompletionNeverReallocatesWhenExactProofIsUnavailable(String failure) throws Exception {
        connect(1,"U");ensure(1);String key=key();google.loseUploadResponse=true;
        assertThatThrownBy(() -> upload(1,key,new byte[]{1})).isInstanceOf(DriveFileException.class);
        String id=intents.findAll().getFirst().getProviderFileId();var remote=google.files.get(id);
        if(failure.equals("missing"))google.files.remove(id);
        else {
            var m=remote.metadata();
            if(failure.equals("checksum"))m.remove("md5Checksum");
            if(failure.equals("action"))m.withObject("/appProperties").put("ppvUploadAction","0".repeat(64));
            if(failure.equals("created"))m.withObject("/appProperties").put("ppvCreatedId","another-id");
            if(failure.equals("parent"))m.withArray("parents").removeAll().add("other-root");
        }
        restart(DriveFileResources.Limits.production());
        for(int i=0;i<2;i++)assertThatThrownBy(() -> upload(1,key,new byte[]{1})).isInstanceOfSatisfying(DriveFileException.class,e -> assertThat(e.getErrorCode()).isEqualTo("UPLOAD_OUTCOME_UNKNOWN"));
        assertThat(intents.findAll().getFirst().getProviderFileId()).isEqualTo(id);
        assertThat(google.generated).hasValue(2);assertThat(google.fileCreates).hasValue(1);
    }
    @Test void credentialChangeAfterRemoteCommitCannotPublishSuccessfulUpload() throws Exception {
        connect(1,"U");ensure(1);String key=key();google.uploadEntered=new CountDownLatch(1);google.uploadRelease=new CountDownLatch(1);
        try(var workers=Executors.newVirtualThreadPerTaskExecutor()) {
            var result=workers.submit(() -> upload(1,key,new byte[]{9}));entered(google.uploadEntered);s1.disconnect(1);
            google.uploadRelease.countDown();assertThat(failure(result)).isInstanceOf(DriveFileException.class);
        }finally{google.uploadRelease.countDown();}
        assertThat(intents.findAll().getFirst().getStatus()).isEqualTo("RECONCILIATION_REQUIRED");
        connect(2,"U");ensure(2);assertThat(upload(2,key,new byte[]{9}).created()).isFalse();assertThat(google.fileCreates).hasValue(1);
    }
}

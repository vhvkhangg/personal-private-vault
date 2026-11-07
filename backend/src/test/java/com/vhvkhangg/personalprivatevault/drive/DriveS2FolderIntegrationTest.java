package com.vhvkhangg.personalprivatevault.drive;

import com.vhvkhangg.personalprivatevault.drive.fixture.DriveS2TestSupport;
import com.vhvkhangg.personalprivatevault.drive.internal.files.application.*;
import com.vhvkhangg.personalprivatevault.drive.internal.files.http.DriveFileResources;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.*;
import static org.assertj.core.api.Assertions.*;

class DriveS2FolderIntegrationTest extends DriveS2TestSupport {
    @Test void firstFoldersForBothSubjectsAreReachableAndDurablyDistinct() {
        connect(1,"U");connect(2,"V");assertThat(ensure(1).data().status()).isEqualTo("READY");ensure(2);
        var u=roots.findById("U").orElseThrow();var v=roots.findById("V").orElseThrow();
        assertThat(u.getRootFileId()).isNotEqualTo(v.getRootFileId());assertThat(u.getRootBindingId()).isNotEqualTo(v.getRootBindingId());
        assertThat(google.rootCreates).hasValue(2);
        assertThat(google.calls).noneMatch(c -> c.path().endsWith("/root") || c.path().contains("account-root-"));
        assertThat(google.files.get(u.getRootFileId()).metadata().path("parents").get(0).asText()).isEqualTo("account-root-U");
        var epoch=u.getBindingEpoch();ensure(1);assertThat(roots.findById("U").orElseThrow().getBindingEpoch()).isEqualTo(epoch);
    }
    @Test void concurrentFirstEnsureHasOneLiveWinnerAndOneProviderIdentity() throws Exception {
        connect(1,"U");google.rootEntered=new CountDownLatch(1);google.rootRelease=new CountDownLatch(1);
        try(var workers=Executors.newVirtualThreadPerTaskExecutor()) {
            var winner=workers.submit(() -> ensure(1));entered(google.rootEntered);
            assertThat(roots.findById("U").orElseThrow().getStatus()).isEqualTo("PENDING");
            assertThatThrownBy(() -> ensure(1)).isInstanceOfSatisfying(DriveFileException.class,e -> assertThat(e.getErrorCode()).isEqualTo("DRIVE_ROOT_PENDING"));
            google.rootRelease.countDown();assertThat(winner.get(3,TimeUnit.SECONDS).data().status()).isEqualTo("READY");
            assertThat(google.rootCreates).hasValue(1);assertThat(google.generated).hasValue(1);
        }finally{google.rootRelease.countDown();}
    }
    @Test void acceptedButLostRootReplyUsesSameIdReadOnlyAfterRestart() {
        connect(1,"U");google.loseRootResponse=true;
        assertThatThrownBy(() -> ensure(1)).isInstanceOf(DriveFileException.class);
        String id=roots.findById("U").orElseThrow().getRootFileId();restart(DriveFileResources.Limits.production());ensure(1);
        assertThat(roots.findById("U").orElseThrow().getRootFileId()).isEqualTo(id);
        assertThat(google.rootCreates).hasValue(1);assertThat(google.generated).hasValue(1);
    }
    @Test void missingPendingRootNeverReleasesOrReallocatesItsId() {
        connect(1,"U");google.loseRootResponse=true;assertThatThrownBy(() -> ensure(1)).isInstanceOf(DriveFileException.class);
        String id=roots.findById("U").orElseThrow().getRootFileId();var remote=google.files.remove(id);
        assertThatThrownBy(() -> ensure(1)).isInstanceOf(DriveFileException.class);
        assertThatThrownBy(() -> {try(var op=resources.open(false)){service.list("1",(short)1,25,null,op);}}).isInstanceOf(DriveFileException.class);
        assertThat(google.generated).hasValue(1);google.files.put(id,remote);ensure(1);
    }
    @ParameterizedTest @ValueSource(strings={"disconnect","epoch"})
    void staleCreateCannotPublishReady(String change) throws Exception {
        connect(1,"U");google.rootEntered=new CountDownLatch(1);google.rootRelease=new CountDownLatch(1);
        try(var workers=Executors.newVirtualThreadPerTaskExecutor()) {
            var result=workers.submit(() -> ensure(1));entered(google.rootEntered);
            if(change.equals("disconnect"))s1.disconnect(1);
            else jdbc.update("UPDATE drive_connections SET credential_epoch=? WHERE slot=1",UUID.randomUUID());
            google.rootRelease.countDown();assertThat(failure(result)).isInstanceOf(DriveFileException.class);
            assertThat(roots.findById("U").orElseThrow().getStatus()).isEqualTo("PENDING");
            assertThat(google.rootCreates).hasValue(1);
        }finally{google.rootRelease.countDown();}
    }
    @Test void sameSubjectReconnectedInOtherSlotReusesItsRetainedRoot() {
        connect(1,"U");ensure(1);var prior=roots.findById("U").orElseThrow().getRootBindingId();
        s1.disconnect(1);connect(2,"U");ensure(2);
        assertThat(roots.findById("U").orElseThrow().getRootBindingId()).isEqualTo(prior);assertThat(google.rootCreates).hasValue(1);
    }
}

package com.vhvkhangg.personalprivatevault.drive;

import com.vhvkhangg.personalprivatevault.drive.fixture.*;
import com.vhvkhangg.personalprivatevault.drive.internal.files.application.*;
import com.vhvkhangg.personalprivatevault.drive.internal.files.http.*;
import com.vhvkhangg.personalprivatevault.drive.internal.persistence.DriveConnectionStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.util.*;
import java.util.concurrent.*;
import static org.assertj.core.api.Assertions.*;

class DriveS2AuthorityIntegrationTest extends DriveS2TestSupport {
    @Test void fileRefreshAcceptsSignedMatchingSubjectWithoutACallbackNonceAndPersistsRotation() {
        connect(1,"U");var previous=connections.findById((short)1).orElseThrow().getCredentialEpoch();
        oauthServer.refreshSubject="U";oauthServer.omitRefreshTokenOnRefresh=false;
        int before=oauthServer.refreshTokenCount.get();ensure(1);
        assertThat(connections.findById((short)1).orElseThrow().getCredentialEpoch()).isNotEqualTo(previous);
        assertThat(oauthServer.refreshTokenCount.get()-before).isEqualTo(1);
    }
    @ParameterizedTest @ValueSource(strings={"subject","scope","empty","type","expiry","invalidGrant"})
    void unusableRefreshCannotDispatchDriveRequests(String bad) {
        connect(1,"U");
        switch(bad) {
            case "subject" -> oauthServer.refreshSubject="V";
            case "scope" -> oauthServer.refreshScopes="openid email https://www.googleapis.com/auth/drive";
            case "empty" -> oauthServer.refreshResponseBody="{}";
            case "type" -> oauthServer.refreshResponseBody="{\"access_token\":\"fake\",\"token_type\":\"Basic\",\"expires_in\":3600}";
            case "expiry" -> oauthServer.refreshResponseBody="{\"access_token\":\"fake\",\"token_type\":\"Bearer\",\"expires_in\":0}";
            case "invalidGrant" -> oauthServer.refreshStatus=400;
        }
        assertThatThrownBy(() -> ensure(1)).isInstanceOf(com.vhvkhangg.personalprivatevault.drive.internal.exception.DriveException.class);
        assertThat(google.calls).isEmpty();
        if(bad.equals("invalidGrant"))assertThat(connections.findById((short)1).orElseThrow().getStatus()).isEqualTo(DriveConnectionStatus.RECONNECT_REQUIRED);
    }
    @Test void staleRefreshInvalidGrantCannotDowngradeNewerCredentialEpoch() throws Exception {
        connect(1,"U");oauthServer.refreshStatus=400;oauthServer.tokenEnteredLatch=new CountDownLatch(1);oauthServer.tokenPauseLatch=new CountDownLatch(1);
        UUID newer=UUID.randomUUID();
        var old=connections.findById((short)1).orElseThrow();
        String refresh=crypto.decrypt((short)1,"U",old.getCredentialEpoch(),old.getEncryptedRefreshToken());
        byte[] ciphertext=crypto.encrypt((short)1,"U",newer,refresh);
        try(var workers=Executors.newVirtualThreadPerTaskExecutor()){
            var request=workers.submit(() -> ensure(1));entered(oauthServer.tokenEnteredLatch);
            jdbc.update("UPDATE drive_connections SET credential_epoch=?,encrypted_refresh_token=? WHERE slot=1",newer,ciphertext);
            oauthServer.tokenPauseLatch.countDown();assertThat(failure(request)).isInstanceOf(DriveFileException.class);
        }finally{oauthServer.tokenPauseLatch.countDown();}
        var row=connections.findById((short)1).orElseThrow();assertThat(row.getCredentialEpoch()).isEqualTo(newer);assertThat(row.getStatus()).isEqualTo(DriveConnectionStatus.CONNECTED);
        assertThat(google.calls).isEmpty();
        oauthServer.refreshStatus=200;ensure(1);assertThat(google.rootCreates).hasValue(1);
    }
    @ParameterizedTest @ValueSource(strings={"disconnect","epoch","rootEpoch","fence"})
    void staleLeasePreventsEveryLaterDispatchAndFinalPublication(String change) {
        connect(1,"U");var lease=ensure(1).lease();int calls=google.calls.size();
        switch(change){
            case "disconnect" -> s1.disconnect(1);
            case "epoch" -> jdbc.update("UPDATE drive_connections SET credential_epoch=? WHERE slot=1",UUID.randomUUID());
            case "rootEpoch" -> jdbc.update("UPDATE drive_folder_roots SET binding_epoch=? WHERE google_sub='U'",UUID.randomUUID());
            case "fence" -> {lifecycle.getLock().lock();try{lifecycle.reserveRevokeFence((short)1,"U");}finally{lifecycle.getLock().unlock();}}
        }
        try(var op=resources.open(false)){
            assertThatThrownBy(() -> provider.file(lease,op,lease.root().getRootFileId(),false)).isInstanceOf(com.vhvkhangg.personalprivatevault.drive.internal.exception.DriveException.class);
            assertThatThrownBy(() -> authority.finalWrite(lease,op,() -> "forbidden")).isInstanceOf(com.vhvkhangg.personalprivatevault.drive.internal.exception.DriveException.class);
        }
        assertThat(google.calls).hasSize(calls);
    }
    @Test void disconnectedInFlightListCannotPublishOrHoldS1GuardDuringHttp() throws Exception {
        connect(1,"U");ensure(1);google.metadataEntered=new CountDownLatch(1);google.metadataRelease=new CountDownLatch(1);
        try(var workers=Executors.newVirtualThreadPerTaskExecutor()){
            var request=workers.submit(() -> {try(var op=resources.open(false)){return service.list("1",(short)1,25,null,op);}});
            entered(google.metadataEntered);s1.disconnect(1);google.metadataRelease.countDown();
            assertThat(failure(request)).isInstanceOf(com.vhvkhangg.personalprivatevault.drive.internal.exception.DriveException.class);
        }finally{google.metadataRelease.countDown();}
    }
}

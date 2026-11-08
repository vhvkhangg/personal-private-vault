package com.vhvkhangg.personalprivatevault.drive;

import com.vhvkhangg.personalprivatevault.drive.fixture.DriveS2TestSupport;
import com.vhvkhangg.personalprivatevault.drive.internal.files.application.*;
import com.vhvkhangg.personalprivatevault.drive.internal.files.http.*;
import com.vhvkhangg.personalprivatevault.drive.internal.files.persistence.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.time.*;
import java.util.*;
import static org.assertj.core.api.Assertions.*;

class DriveS2UploadIntentIntegrationTest extends DriveS2TestSupport {
    private DriveFileAuthority.Lease ready(DriveFileResources.Operation op){return folders.ready(authority.acquire("1",(short)1,op),op);}
    @ParameterizedTest @ValueSource(booleans={false,true})
    void durablePredispatchClaimResumesExactlyOnceAfterRestart(boolean reserved) throws Exception {
        connect(1,"U");ensure(1);String key=key();String reservedId=null;
        try(var op=resources.open(true)) {
            var lease=ready(op);
            try(var claim=store.take(lease,op,key,raw(new byte[]{5},"original.bin","application/octet-stream",op))){
                if(reserved){reservedId=provider.generateId(lease,op);store.reserve(claim,lease,op,reservedId);}
            }
        }
        restart(DriveFileResources.Limits.production());var result=upload(1,key,new byte[]{5});
        if(reserved)assertThat(result.data().id()).isEqualTo(reservedId);
        assertThat(google.fileCreates).hasValue(1);assertThat(google.generated).hasValue(2);
    }
    @Test void keysAreGloballyUniqueAndCannotCrossSubjectsOrRoots() throws Exception {
        connect(1,"U");connect(2,"V");ensure(1);ensure(2);String key=key();upload(1,key,new byte[]{1});
        assertThatThrownBy(() -> upload(2,key,new byte[]{1})).isInstanceOfSatisfying(DriveFileException.class,e -> assertThat(e.getErrorCode()).isEqualTo("UPLOAD_KEY_CONFLICT"));
        assertThat(intents.count()).isEqualTo(1);assertThat(google.fileCreates).hasValue(1);
    }
    @Test void recordedOldKeyPrecedesFreshnessAndPrunedOldKeyCannotBecomeANewAction() throws Exception {
        connect(1,"U");ensure(1);String old=key();var first=upload(1,old,new byte[]{1});clock.advance(Duration.ofDays(31));
        assertThat(upload(1,old,new byte[]{1}).data().id()).isEqualTo(first.data().id());
        jdbc.update("UPDATE drive_upload_intents SET completed_at=created_at WHERE idempotency_key=?",old);
        upload(1,key(),new byte[]{2});
        assertThat(intents.findAll()).noneMatch(i -> i.getId().getIdempotencyKey().equals(old));
        assertThatThrownBy(() -> upload(1,old,new byte[]{1})).isInstanceOfSatisfying(DriveFileException.class,e -> assertThat(e.getErrorCode()).isEqualTo("UPLOAD_KEY_EXPIRED"));
        assertThat(google.fileCreates).hasValue(2);
    }
    @ParameterizedTest @ValueSource(ints={8,1024})
    void capacityRejectsBeforeProviderIdAllocation(int retained) throws Exception {
        connect(1,"U");ensure(1);var root=roots.findById("U").orElseThrow();
        boolean terminal=retained==1024;
        var rows=new ArrayList<Object[]>();
        for(int i=0;i<retained;i++)rows.add(new Object[]{"U",root.getRootBindingId(),key(),"0".repeat(64),"0".repeat(32),0L,"empty.bin","application/octet-stream",
                terminal?"retained-"+i:null,terminal?"COMPLETED":"PREPARING",UUID.randomUUID(),root.getBindingEpoch(),java.sql.Timestamp.from(clock.instant()),java.sql.Timestamp.from(clock.instant()),terminal?java.sql.Timestamp.from(clock.instant()):null});
        jdbc.batchUpdate("""
            INSERT INTO drive_upload_intents (
                google_sub, root_binding_id, idempotency_key, request_fingerprint, content_md5,
                size_bytes, safe_name, mime_type, provider_file_id, status, intent_epoch,
                root_binding_epoch, created_at, updated_at, completed_at
            ) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
            """,rows);
        assertThatThrownBy(() -> upload(1,key(),new byte[0])).isInstanceOfSatisfying(DriveFileException.class,e -> assertThat(e.getHttpStatus()).isEqualTo(503));
        assertThat(google.generated).hasValue(1);assertThat(google.fileCreates).hasValue(0);
    }
    @Test void pruningIsBoundedAndNeverDeletesUncertaintyOrLiveActions() throws Exception {
        connect(1,"U");ensure(1);String uncertain=key();google.loseUploadResponse=true;
        assertThatThrownBy(() -> upload(1,uncertain,new byte[0])).isInstanceOf(DriveFileException.class);
        var root=roots.findById("U").orElseThrow();Instant old=clock.instant().minus(Duration.ofDays(31));
        var rows=new ArrayList<Object[]>();for(int i=0;i<40;i++)rows.add(new Object[]{"U",root.getRootBindingId(),key(),"0".repeat(64),"0".repeat(32),0L,"a","text/plain","old-"+i,"COMPLETED",UUID.randomUUID(),root.getBindingEpoch(),java.sql.Timestamp.from(old),java.sql.Timestamp.from(old),java.sql.Timestamp.from(old)});
        jdbc.batchUpdate("""
            INSERT INTO drive_upload_intents (
                google_sub, root_binding_id, idempotency_key, request_fingerprint, content_md5,
                size_bytes, safe_name, mime_type, provider_file_id, status, intent_epoch,
                root_binding_epoch, created_at, updated_at, completed_at
            ) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
            """,rows);
        upload(1,key(),new byte[0]);assertThat(intents.count()).isEqualTo(10);
        assertThat(intents.findAll()).anyMatch(i -> i.getId().getIdempotencyKey().equals(uncertain));
    }
    @Test void compareAndSwapRejectsOldEpochAndDatabaseRejectsInvalidPhaseAndForeignRoot() throws Exception {
        connect(1,"U");ensure(1);String key=key();
        try(var op=resources.open(true)){
            var lease=ready(op);try(var claim=store.take(lease,op,key,raw(new byte[0],"original.bin","application/octet-stream",op))){
                var old=claim.intent();
                assertThat(authority.transaction(() -> intents.reserve(old.getId(),UUID.randomUUID(),UUID.randomUUID(),"wrong",clock.instant()))).isZero();
                store.reserve(claim,lease,op,provider.generateId(lease,op));
                assertThat(authority.transaction(() -> intents.reserve(old.getId(),old.getIntentEpoch(),UUID.randomUUID(),"other",clock.instant()))).isZero();
            }
        }
        assertThatThrownBy(() -> jdbc.update("UPDATE drive_upload_intents SET status='COMPLETED' WHERE idempotency_key=?",key)).isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
        assertThatThrownBy(() -> jdbc.update("UPDATE drive_upload_intents SET size_bytes=52428801 WHERE idempotency_key=?",key)).isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
        assertThatThrownBy(() -> jdbc.update("UPDATE drive_upload_intents SET root_binding_id=? WHERE idempotency_key=?",UUID.randomUUID(),key)).isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
        assertThatThrownBy(() -> jdbc.update("DELETE FROM drive_folder_roots")).isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
        s1.disconnect(1);assertThat(intents.count()).isEqualTo(1);assertThat(roots.count()).isEqualTo(1);
    }
    @Test void terminalPruningCannotDeleteALiveReconciliationClaim() throws Exception {
        connect(1,"U");ensure(1);String old=key();String id=upload(1,old,new byte[]{3}).data().id();clock.advance(Duration.ofDays(31));
        google.metadataPauseId=id;google.metadataEntered=new java.util.concurrent.CountDownLatch(1);google.metadataRelease=new java.util.concurrent.CountDownLatch(1);
        try(var workers=java.util.concurrent.Executors.newVirtualThreadPerTaskExecutor()){
            var replay=workers.submit(() -> upload(1,old,new byte[]{3}));entered(google.metadataEntered);
            upload(1,key(),new byte[]{4});assertThat(intents.findAll()).anyMatch(i -> i.getId().getIdempotencyKey().equals(old));
            google.metadataRelease.countDown();assertThat(replay.get(3,java.util.concurrent.TimeUnit.SECONDS).data().id()).isEqualTo(id);
        }finally{google.metadataRelease.countDown();}
        assertThat(google.fileCreates).hasValue(2);
    }
    @ParameterizedTest @ValueSource(strings={"20260230T120000Z.11111111-1111-4111-8111-111111111111","20261106T120000Z.11111111-1111-1111-8111-111111111111","20261106T120000Z.11111111-1111-4111-7111-111111111111","opaque"})
    void malformedActionKeysNeverContactProviders(String invalid) {
        assertThatThrownBy(() -> DriveFileInputs.keyTime(invalid)).isInstanceOf(DriveFileException.class);
        assertThat(google.calls).isEmpty();assertThat(oauthServer.refreshTokenCount).hasValue(0);
    }
}

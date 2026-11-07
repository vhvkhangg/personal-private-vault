package com.vhvkhangg.personalprivatevault.drive;

import com.fasterxml.jackson.databind.*;
import com.vhvkhangg.personalprivatevault.drive.fixture.*;
import com.vhvkhangg.personalprivatevault.drive.internal.files.application.*;
import com.vhvkhangg.personalprivatevault.drive.internal.files.http.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import java.time.*;
import java.util.*;
import static org.assertj.core.api.Assertions.*;

class DriveS2ProviderTest extends DriveS2TestSupport {
    private final ObjectMapper mapper=new ObjectMapper();
    @ParameterizedTest @ValueSource(strings={"parent","type","trashed","kind","binding","created","missing","absent","permission"})
    void rootProofIsConjunctiveAndNeverSubstitutesSameName(String change) {
        connect(1,"U");ensure(1);var root=roots.findById("U").orElseThrow();
        var metadata=google.files.get(root.getRootFileId()).metadata();
        switch(change){
            case "parent" -> metadata.withArray("parents").removeAll().add("elsewhere");
            case "type" -> metadata.put("mimeType","text/plain");
            case "trashed" -> metadata.put("trashed",true);
            case "kind" -> metadata.withObject("/appProperties").put("ppvKind","file");
            case "binding" -> metadata.withObject("/appProperties").put("ppvRootBinding",UUID.randomUUID().toString());
            case "created" -> metadata.withObject("/appProperties").put("ppvCreatedId","another-id");
            case "missing" -> metadata.remove("appProperties");
            case "absent" -> google.files.remove(root.getRootFileId());
            case "permission" -> google.metadataStatus=403;
        }
        assertThatThrownBy(() -> ensure(1)).isInstanceOf(DriveFileException.class);
        try(var op=resources.open(false)){assertThatThrownBy(() -> service.list("1",(short)1,25,null,op)).isInstanceOf(DriveFileException.class);}
        assertThat(google.rootCreates).hasValue(1);assertThat(google.generated).hasValue(1);
    }
    @ParameterizedTest @ValueSource(strings={"parent","copied","kind","binding","created","missing","action","folder","shortcut","document","sheet","slide","trashed","id"})
    void unauthorizedCandidatesAreOmittedAndCannotBeDownloaded(String change) throws Exception {
        connect(1,"U");ensure(1);var uploaded=upload(1,key(),new byte[]{4});String id=uploaded.data().id();
        var m=google.files.get(id).metadata();
        switch(change){
            case "parent" -> m.withArray("parents").removeAll().add("elsewhere");
            case "copied" -> m.withArray("parents").add("second-parent");
            case "kind" -> m.withObject("/appProperties").put("ppvKind","root");
            case "binding" -> m.withObject("/appProperties").put("ppvRootBinding",UUID.randomUUID().toString());
            case "created" -> m.withObject("/appProperties").put("ppvCreatedId","different-created-id");
            case "missing" -> m.remove("appProperties");
            case "action" -> m.withObject("/appProperties").remove("ppvUploadAction");
            case "folder","shortcut","document","sheet","slide" -> m.put("mimeType","application/vnd.google-apps."+change);
            case "trashed" -> m.put("trashed",true);
            case "id" -> m.put("id","different-returned-id");
        }
        google.listOverride=mapper.createObjectNode();google.listOverride.putArray("files").add(m);
        try(var op=resources.open(false)){assertThat(service.list("1",(short)1,25,null,op).data().files()).isEmpty();}
        try(var op=resources.open(false)){assertThatThrownBy(() -> service.download("1",(short)1,id,op)).isInstanceOf(DriveFileException.class);}
        assertThat(google.mediaCalls).hasValue(0);
        assertThat(google.calls).noneMatch(c -> c.path().contains("export") || (c.query()!=null && c.query().contains("acknowledgeAbuse")));
    }
    @Test void copiedUFileUnderVRootStillFailsBindingAndCreatedIdProof() throws Exception {
        connect(1,"U");connect(2,"V");ensure(1);ensure(2);var file=upload(1,key(),new byte[]{8});
        var m=google.files.get(file.data().id()).metadata();m.withArray("parents").removeAll().add(roots.findById("V").orElseThrow().getRootFileId());
        google.allowForeignMetadata=true;google.allowForeignMedia=true;
        try(var op=resources.open(false)){assertThatThrownBy(() -> service.download("1",(short)2,file.data().id(),op)).isInstanceOf(DriveFileException.class);}
        assertThat(google.mediaCalls).hasValue(0);
    }
    @Test void paginationUsesOpaqueBoundedHandlesAndOneProviderPageWithoutNPlusOne() throws Exception {
        connect(1,"U");connect(2,"V");ensure(1);ensure(2);var file=upload(1,key(),new byte[]{3});
        google.listOverride=mapper.createObjectNode();google.listOverride.putArray("files").add(google.files.get(file.data().id()).metadata());
        google.listOverride.put("nextPageToken","provider-private-token");String cursor;
        try(var op=resources.open(false)){var page=service.list("1",(short)1,1,null,op).data();cursor=page.nextCursor();assertThat(page.files()).hasSize(1);assertThat(page.hasMore()).isTrue();assertThat(cursor).hasSize(43).doesNotContain("provider");}
        int before=google.calls.size();
        try(var op=resources.open(false)){service.list("1",(short)1,1,cursor,op);}
        assertThat(google.calls.size()-before).isEqualTo(3); // About, exact root, one page; no per-file fetch
        assertThat(google.calls).anyMatch(c -> c.query()!=null && c.query().contains("pageToken=provider-private-token"));
        for(short slot:new short[]{1,2})try(var op=resources.open(false)){
            assertThatThrownBy(() -> service.list("1",slot,2,cursor,op)).isInstanceOf(DriveFileException.class);
        }
        jdbc.update("UPDATE drive_connections SET credential_epoch=? WHERE slot=1",UUID.randomUUID());
        try(var op=resources.open(false)){assertThatThrownBy(() -> service.list("1",(short)1,1,cursor,op)).isInstanceOf(DriveFileException.class);}
        clock.advance(Duration.ofSeconds(300));
        try(var op=resources.open(false)){assertThatThrownBy(() -> service.list("1",(short)1,1,cursor,op)).isInstanceOf(DriveFileException.class);}
    }
    @Test void continuationStoreCapacityNeverSilentlyDropsAProviderContinuation() {
        connect(1,"U");var lease=ensure(1).lease();var cursors=new DriveFileCursors(clock);
        for(int i=0;i<128;i++)assertThat(cursors.issue("t-"+i,lease,25)).hasSize(43);
        assertThatThrownBy(() -> cursors.issue("last",lease,25)).isInstanceOfSatisfying(DriveFileException.class,e -> assertThat(e.getHttpStatus()).isEqualTo(503));
        clock.advance(Duration.ofSeconds(300));assertThat(cursors.issue("new",lease,25)).hasSize(43);
    }
    @ParameterizedTest @ValueSource(strings={"HEADERS_ONLY","PARTIAL","TRICKLE"})
    void metadataDeadlineIncludesTheEntireBody(String mode) {
        connect(1,"U");restart(new DriveFileResources.Limits(Duration.ofMillis(220),Duration.ofSeconds(2),Duration.ofMillis(150),Duration.ofSeconds(3)));
        google.metadataMode=FakeGoogleDriveServer.Mode.valueOf(mode);google.bodyRelease=new java.util.concurrent.CountDownLatch(1);
        long start=System.nanoTime();
        assertThatThrownBy(() -> ensure(1)).isInstanceOfSatisfying(DriveFileException.class,e -> assertThat(e.getHttpStatus()).isEqualTo(504));
        assertThat(Duration.ofNanos(System.nanoTime()-start)).isLessThan(Duration.ofSeconds(2));
        assertThat(resources.activeOperations()).isZero();google.bodyRelease.countDown();
    }
    @ParameterizedTest @ValueSource(strings={"OVERSIZED","MALFORMED","REDIRECT","LARGE_HEADERS"})
    void malformedOversizedOrRedirectedMetadataIsBoundedAndSafe(String mode) {
        connect(1,"U");google.metadataMode=FakeGoogleDriveServer.Mode.valueOf(mode);
        assertThatThrownBy(() -> ensure(1)).isInstanceOfSatisfying(DriveFileException.class,e -> assertThat(e.getHttpStatus()).isEqualTo(502));
        assertThat(google.rootCreates).hasValue(0);assertThat(resources.activeOperations()).isZero();
    }
    @ParameterizedTest @CsvSource({"401,409","403,404","404,404","429,503","500,502","302,502"})
    void providerFailuresAreClassifiedWithoutBodyReflectionOrRetry(int status,int expected) {
        connect(1,"U");var lease=ensure(1).lease();google.metadataStatus=status;int before=google.calls.size();
        try(var op=resources.open(false)){assertThatThrownBy(() -> provider.file(lease,op,lease.root().getRootFileId(),false)).isInstanceOfSatisfying(DriveFileException.class,e -> {
            assertThat(e.getHttpStatus()).isEqualTo(expected);assertThat(e.getMessage()).doesNotContain("access-","refresh-");
        });}
        assertThat(google.calls.size()-before).isEqualTo(1);
    }
    @ParameterizedTest @ValueSource(strings={"https://evil.example/upload/drive/v3/files?uploadType=resumable&upload_id=x","http://www.googleapis.com/upload/drive/v3/files?uploadType=resumable&upload_id=x",
            "https://www.googleapis.com:444/upload/drive/v3/files?uploadType=resumable&upload_id=x","https://user@www.googleapis.com/upload/drive/v3/files?uploadType=resumable&upload_id=x",
            "https://www.googleapis.com/upload/drive/v3/files?uploadType=resumable&upload_id=x#f","https://www.googleapis.com/upload/drive/v3/%66iles?uploadType=resumable&upload_id=x",
            "https://www.googleapis.com/upload/drive/v3/files?uploadType=resumable&upload_id=x&upload_id=y"})
    void resumableLocationIsNeverAnArbitraryUrl(String value) {
        try(var fixed=new DriveFileHttpClient(authority)){assertThatThrownBy(() -> fixed.validateSession(value)).isInstanceOf(DriveFileException.class);}
        assertThat(google.calls).isEmpty();
    }
    @Test void productionTransportAndResourceLimitsAreExplicit() {
        var limits=DriveFileResources.Limits.production();assertThat(limits.metadata()).isEqualTo(Duration.ofSeconds(10));
        assertThat(limits.transfer()).isEqualTo(Duration.ofSeconds(120));assertThat(limits.idle()).isEqualTo(Duration.ofSeconds(15));assertThat(limits.operation()).isEqualTo(Duration.ofSeconds(180));
        assertThat(DriveFileResources.BUFFER).isEqualTo(65536);assertThat(DriveFileHttpClient.GOOGLE.toString()).isEqualTo("https://www.googleapis.com");
        try(var op=resources.open(false)){
            for(int i=0;i<8;i++)op.countRequest();assertThat(op.requestCount()).isEqualTo(8);
            assertThatThrownBy(op::countRequest).isInstanceOf(DriveFileException.class);
        }
        google.aboutOverride="{}";connect(1,"U");assertThatThrownBy(() -> ensure(1)).isInstanceOf(DriveFileException.class);assertThat(google.rootCreates).hasValue(0);
    }
    @ParameterizedTest @ValueSource(ints={112,113})
    void reservedIdFitsTheCreationMarkerLimitBeforeAnyMutation(int length){
        connect(1,"U");google.generatedOverride="a".repeat(length);
        if(length==112){ensure(1);assertThat(google.rootCreates).hasValue(1);}
        else{assertThatThrownBy(() -> ensure(1)).isInstanceOf(DriveFileException.class);assertThat(google.rootCreates).hasValue(0);assertThat(roots.count()).isZero();}
    }
    @Test void quotaReasonIsClassifiedWithoutReflectingTheProviderBody(){
        connect(1,"U");var lease=ensure(1).lease();google.metadataStatus=403;
        google.metadataResponseBody="{\"error\":{\"message\":\"SYNTHETIC_PRIVATE_MARKER\",\"errors\":[{\"reason\":\"storageQuotaExceeded\"}]}}";
        try(var op=resources.open(false)){assertThatThrownBy(() -> provider.file(lease,op,lease.root().getRootFileId(),false)).isInstanceOfSatisfying(DriveFileException.class,e -> {
            assertThat(e.getErrorCode()).isEqualTo("DRIVE_PROVIDER_QUOTA");assertThat(e.getHttpStatus()).isEqualTo(503);assertThat(e.getMessage()).doesNotContain("SYNTHETIC_PRIVATE_MARKER");
        });}
    }
}

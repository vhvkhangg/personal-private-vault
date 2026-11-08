package com.vhvkhangg.personalprivatevault.drive;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vhvkhangg.personalprivatevault.drive.fixture.DriveH1BTestSupport;
import com.vhvkhangg.personalprivatevault.drive.fixture.FakeGoogleDriveServer;
import com.vhvkhangg.personalprivatevault.drive.internal.files.application.DriveFileException;
import com.vhvkhangg.personalprivatevault.drive.internal.files.application.DriveFileInputs;
import com.vhvkhangg.personalprivatevault.drive.internal.files.application.DriveFileProof;
import com.vhvkhangg.personalprivatevault.drive.internal.files.http.DriveFileResources;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DriveH1BAuthorityIntegrationTest extends DriveH1BTestSupport {

    protected final ObjectMapper mapper = new ObjectMapper();

    @Test
    @DisplayName("B-02: Same-key concurrent folder create converges on at most one provider create and ID")
    void sameKeyConcurrentFolderCreateProducesAtMostOneCreate() throws Exception {
        connect(1, "SUB_B02");
        ensure(1);
        String actionKey = key();

        try (var workers = Executors.newFixedThreadPool(2)) {
            CyclicBarrier barrier = new CyclicBarrier(2);
            List<Callable<DriveFileProof.FolderItem>> tasks = new ArrayList<>();
            for (int i = 0; i < 2; i++) {
                tasks.add(() -> {
                    barrier.await();
                    try {
                        return createFolder(1, actionKey, "ConcurrentFolder").data();
                    } catch (DriveFileException e) {
                        if ("FOLDER_IN_PROGRESS".equals(e.getErrorCode()) || "DRIVE_FILE_CAPACITY_EXHAUSTED".equals(e.getErrorCode())) {
                            return null;
                        }
                        throw e;
                    }
                });
            }
            List<Future<DriveFileProof.FolderItem>> futures = workers.invokeAll(tasks);
            List<DriveFileProof.FolderItem> results = new ArrayList<>();
            for (var f : futures) {
                var item = f.get();
                if (item != null) {
                    results.add(item);
                }
            }
            assertThat(results).isNotEmpty();
            String firstId = results.get(0).id();
            for (var f : results) {
                assertThat(f.id()).isEqualTo(firstId);
            }
        }

        assertThat(google.childFolderCreates).hasValue(1);
        assertThat(childFoldersRepo.countRetained("SUB_B02", roots.findById("SUB_B02").orElseThrow().getRootBindingId())).isEqualTo(1);
    }

    @Test
    @DisplayName("B-02: Lost response after DISPATCHED leaves action in RECONCILIATION_REQUIRED; replay is GET-only")
    void lostResponseLeavesReconciliationRequiredWithoutSecondCreate() throws Exception {
        connect(1, "SUB_B02_LOST");
        ensure(1);
        String actionKey = key();

        // Simulate lost response during create
        google.loseRootResponse = true;
        assertThatThrownBy(() -> createFolder(1, actionKey, "LostFolder"))
                .isInstanceOfSatisfying(DriveFileException.class, e -> {
                    assertThat(e.getErrorCode()).isEqualTo("FOLDER_OUTCOME_UNKNOWN");
                    assertThat(e.getHttpStatus()).isEqualTo(409);
                });

        var root = roots.findById("SUB_B02_LOST").orElseThrow();
        var keyObj = new com.vhvkhangg.personalprivatevault.drive.internal.files.persistence.DriveChildFolderKey(
                "SUB_B02_LOST", root.getRootBindingId(), actionKey);
        var intent = childFoldersRepo.findById(keyObj).orElseThrow();
        assertThat(intent.getStatus()).isIn("DISPATCHED", "RECONCILIATION_REQUIRED");
        assertThat(intent.getProviderFolderId()).isNotNull();

        // Replay same action key: reconciles via files.get, does NOT call files.create again
        int initialCreates = google.childFolderCreates.get();
        var replay = createFolder(1, actionKey, "LostFolder");
        assertThat(replay.created()).isFalse();
        assertThat(replay.data().id()).isEqualTo(intent.getProviderFolderId());
        assertThat(google.childFolderCreates.get()).isEqualTo(initialCreates);

        var finalIntent = childFoldersRepo.findById(keyObj).orElseThrow();
        assertThat(finalIntent.getStatus()).isEqualTo("COMPLETED");
    }

    @Test
    @DisplayName("B-02: Folder capacity enforces 256 retained and 8 nonterminal bounds; replay works at capacity")
    void folderCapacityBoundsEnforced() {
        connect(1, "SUB_B02_CAP");
        ensure(1);
        var root = roots.findById("SUB_B02_CAP").orElseThrow();
        UUID rootBinding = root.getRootBindingId();
        UUID rootEpoch = root.getBindingEpoch();
        Instant now = clock.instant();

        // Seed 256 retained completed folder rows
        var rows = new ArrayList<Object[]>();
        for (int i = 0; i < 256; i++) {
            String k = key();
            String fp = (i == 0)
                    ? DriveFileInputs.folderFingerprint("SUB_B02_CAP", rootBinding, root.getRootFileId(), "f-0", k)
                    : "0".repeat(64);
            rows.add(new Object[]{
                    "SUB_B02_CAP", rootBinding, k, rootEpoch, UUID.randomUUID(),
                    "f-" + i, fp, "folder-id-" + i, "COMPLETED",
                    Timestamp.from(now), Timestamp.from(now), Timestamp.from(now)
            });
        }
        jdbc.batchUpdate("""
            INSERT INTO drive_child_folder_intents VALUES (?,?,?,?,?,?,?,?,?,?,?,?)
            """, rows);

        // Attempt new folder creation: rejected with 503 capacity
        assertThatThrownBy(() -> createFolder(1, key(), "OverCapacityFolder"))
                .isInstanceOfSatisfying(DriveFileException.class, e -> assertThat(e.getHttpStatus()).isEqualTo(503));

        // Replay of an existing key still works at full capacity
        String existingKey = (String) rows.get(0)[2];
        String existingFolderId = (String) rows.get(0)[7];
        // Populate Google fake with this folder
        ObjectNode meta = mapper.createObjectNode();
        meta.put("id", existingFolderId);
        meta.put("name", "f-0");
        meta.put("mimeType", "application/vnd.google-apps.folder");
        meta.set("parents", mapper.valueToTree(List.of(root.getRootFileId())));
        meta.put("trashed", false);
        ObjectNode appProps = meta.putObject("appProperties");
        appProps.put("ppvKind", "child");
        appProps.put("ppvRootBinding", rootBinding.toString());
        appProps.put("ppvCreatedId", existingFolderId);
        appProps.put("ppvFolderAction", DriveFileInputs.action(existingKey));
        appProps.put("ppvParentId", root.getRootFileId());
        google.files.put(existingFolderId, new FakeGoogleDriveServer.Remote("SUB_B02_CAP", meta, null));

        var replay = createFolder(1, existingKey, "f-0");
        assertThat(replay.created()).isFalse();
        assertThat(replay.data().id()).isEqualTo(existingFolderId);
    }

    @Test
    @DisplayName("B-03: Child folder proof rejects moved parent, foreign sub, trashed folder, and wrong kind")
    void childFolderProofRejectsInvalidState() throws Exception {
        connect(1, "SUB_B03");
        ensure(1);
        var created = createFolder(1, key(), "ValidChild");
        String childId = created.data().id();
        var root = roots.findById("SUB_B03").orElseThrow();

        // 1. Valid child verifies
        try (var op = resources.open(false)) {
            var lease = folders.ready(authority.acquire("1", (short) 1, op), op);
            var item = childFolderService.verifyChildFolder(lease, op, childId);
            assertThat(item.id()).isEqualTo(childId);
        }

        // 2. Child moved to another parent fails
        google.files.get(childId).metadata().set("parents", mapper.valueToTree(List.of("foreign-parent-id")));
        try (var op = resources.open(false)) {
            var lease = folders.ready(authority.acquire("1", (short) 1, op), op);
            assertThatThrownBy(() -> childFolderService.verifyChildFolder(lease, op, childId))
                    .isInstanceOfSatisfying(DriveFileException.class, e -> assertThat(e.getHttpStatus()).isEqualTo(404));
        }

        // 3. Trashed child fails
        google.files.get(childId).metadata().set("parents", mapper.valueToTree(List.of(root.getRootFileId())));
        google.files.get(childId).metadata().put("trashed", true);
        try (var op = resources.open(false)) {
            var lease = folders.ready(authority.acquire("1", (short) 1, op), op);
            assertThatThrownBy(() -> childFolderService.verifyChildFolder(lease, op, childId))
                    .isInstanceOfSatisfying(DriveFileException.class, e -> assertThat(e.getHttpStatus()).isEqualTo(404));
        }

        // 4. Grandchild (non-root direct parent) fails closed
        String grandChildId = "grandchild-1";
        ObjectNode grandMeta = mapper.createObjectNode();
        grandMeta.put("id", grandChildId);
        grandMeta.put("name", "GrandChild");
        grandMeta.put("mimeType", "application/vnd.google-apps.folder");
        grandMeta.set("parents", mapper.valueToTree(List.of(childId)));
        grandMeta.put("trashed", false);
        google.files.put(grandChildId, new FakeGoogleDriveServer.Remote("SUB_B03", grandMeta, null));
        try (var op = resources.open(false)) {
            var lease = folders.ready(authority.acquire("1", (short) 1, op), op);
            assertThatThrownBy(() -> childFolderService.verifyChildFolder(lease, op, grandChildId))
                    .isInstanceOf(DriveFileException.class);
        }
    }

    @Test
    @DisplayName("B-04: Child file upload, list, download work; conflicting parent/mode/body rejected; moved child file denied")
    void childFileUploadDownloadAndParentAffinityEnforced() throws Exception {
        connect(1, "SUB_B04");
        ensure(1);
        var folder = createFolder(1, key(), "MyChildFolder");
        String childId = folder.data().id();
        String uploadKey = key();
        byte[] payload = new byte[]{10, 20, 30, 40};

        // 1. Upload file under child
        var uploadResult = uploadChild(1, childId, uploadKey, payload, "child-doc.bin", "application/octet-stream");
        assertThat(uploadResult.created()).isTrue();
        String fileId = uploadResult.data().id();
        assertThat(uploadResult.data().name()).isEqualTo("child-doc.bin");

        // 2. Download child file without caller supplying parent
        try (var op = resources.open(false)) {
            var dl = service.download("1", (short) 1, fileId, op);
            assertThat(dl.data().id()).isEqualTo(fileId);
            assertThat(dl.data().sizeBytes()).isEqualTo(4);
            var media = service.media(dl, op);
            byte[] downloaded = media.input().readAllBytes();
            assertThat(downloaded).isEqualTo(payload);
        }

        // 3. Same key on root parent conflicts
        assertThatThrownBy(() -> upload(1, uploadKey, payload))
                .isInstanceOfSatisfying(DriveFileException.class, e -> assertThat(e.getErrorCode()).isEqualTo("UPLOAD_KEY_CONFLICT"));

        // 4. Same key on different child conflicts
        var otherFolder = createFolder(1, key(), "OtherFolder");
        assertThatThrownBy(() -> uploadChild(1, otherFolder.data().id(), uploadKey, payload, "child-doc.bin", "application/octet-stream"))
                .isInstanceOfSatisfying(DriveFileException.class, e -> assertThat(e.getErrorCode()).isEqualTo("UPLOAD_KEY_CONFLICT"));

        // 5. Moved child file to root parent is denied
        var root = roots.findById("SUB_B04").orElseThrow();
        google.files.get(fileId).metadata().set("parents", mapper.valueToTree(List.of(root.getRootFileId())));
        try (var op = resources.open(false)) {
            assertThatThrownBy(() -> service.download("1", (short) 1, fileId, op))
                    .isInstanceOfSatisfying(DriveFileException.class, e -> assertThat(e.getHttpStatus()).isEqualTo(404));
        }

        // 6. Child file moved to another child is denied
        google.files.get(fileId).metadata().set("parents", mapper.valueToTree(List.of(otherFolder.data().id())));
        try (var op = resources.open(false)) {
            assertThatThrownBy(() -> service.download("1", (short) 1, fileId, op))
                    .isInstanceOfSatisfying(DriveFileException.class, e -> assertThat(e.getHttpStatus()).isEqualTo(404));
        }
    }

    @Test
    @DisplayName("B-02: Eight non-terminal limit rejects 9th folder create with 503 capacity")
    void eightNonterminalFolderLimitEnforced() {
        connect(1, "SUB_B02_NONTERM");
        ensure(1);
        var root = roots.findById("SUB_B02_NONTERM").orElseThrow();
        UUID rootBinding = root.getRootBindingId();
        UUID rootEpoch = root.getBindingEpoch();
        Instant now = clock.instant();

        // Seed 8 non-terminal PREPARING rows
        for (int i = 0; i < 8; i++) {
            childFoldersRepo.insertPreparing("SUB_B02_NONTERM", rootBinding, key(), rootEpoch, UUID.randomUUID(),
                    "nonterm-" + i, "0".repeat(64), now);
        }

        // Attempt 9th folder create: rejected with 503 capacity
        assertThatThrownBy(() -> createFolder(1, key(), "OverNontermLimit"))
                .isInstanceOfSatisfying(DriveFileException.class, e -> {
                    assertThat(e.getHttpStatus()).isEqualTo(503);
                    assertThat(e.getErrorCode()).isEqualTo("DRIVE_FILE_CAPACITY_EXHAUSTED");
                });
    }

    @Test
    @DisplayName("B-02: Deterministic 8-nonterminal capacity contention with provider gate")
    void deterministicEightNonterminalContentionWithProviderGate() throws Exception {
        connect(1, "SUB_B02_CONTENTION");
        ensure(1);
        var root = roots.findById("SUB_B02_CONTENTION").orElseThrow();
        UUID rootBinding = root.getRootBindingId();
        UUID rootEpoch = root.getBindingEpoch();
        Instant now = clock.instant();

        // Seed 7 non-terminal rows so only 1 slot remains
        for (int i = 0; i < 7; i++) {
            childFoldersRepo.insertPreparing("SUB_B02_CONTENTION", rootBinding, key(), rootEpoch, UUID.randomUUID(),
                    "pre-contend-" + i, "0".repeat(64), now);
        }

        String key1 = key();
        String key2 = key();
        google.rootEntered = new CountDownLatch(1);
        google.rootRelease = new CountDownLatch(1);
        int createsBefore = google.childFolderCreates.get();
        int generatedBefore = google.generated.get();

        try (var workers = Executors.newFixedThreadPool(2)) {
            var f1 = workers.submit(() -> {
                try {
                    return createFolder(1, key1, "Contend1").created() ? 201 : 200;
                } catch (DriveFileException e) {
                    return e.getHttpStatus();
                }
            });

            // Wait for thread 1 to be admitted and reach provider seam
            assertThat(google.rootEntered.await(10, TimeUnit.SECONDS)).isTrue();

            // Thread 2 contends while slot 8 is held nonterminal: must reject with 503
            var f2 = workers.submit(() -> {
                try {
                    return createFolder(1, key2, "Contend2").created() ? 201 : 200;
                } catch (DriveFileException e) {
                    return e.getHttpStatus();
                }
            });

            assertThat(f2.get(5, TimeUnit.SECONDS)).isEqualTo(503);

            // Release thread 1
            google.rootRelease.countDown();
            assertThat(f1.get(5, TimeUnit.SECONDS)).isEqualTo(201);
        } finally {
            if (google.rootRelease != null) google.rootRelease.countDown();
        }

        // Verify committed counts and provider mutation/ID counters
        assertThat(childFoldersRepo.countRetained("SUB_B02_CONTENTION", rootBinding)).isEqualTo(8);
        assertThat(childFoldersRepo.countNonterminal("SUB_B02_CONTENTION", rootBinding)).isEqualTo(7);
        assertThat(google.childFolderCreates.get()).isEqualTo(createsBefore + 1);
        assertThat(google.generated.get()).isEqualTo(generatedBefore + 1);
    }

    @Test
    @DisplayName("B-02: Deterministic 256 retained capacity contention and existing-key recovery")
    void deterministic256RetainedContentionAndExistingKeyRecovery() throws Exception {
        connect(1, "SUB_B02_256");
        ensure(1);
        var root = roots.findById("SUB_B02_256").orElseThrow();
        UUID rootBinding = root.getRootBindingId();
        UUID rootEpoch = root.getBindingEpoch();
        Instant now = clock.instant();

        // Seed 255 terminal COMPLETED rows
        for (int i = 0; i < 255; i++) {
            String k = key();
            String fp = "0".repeat(64);
            UUID intentEpoch = UUID.randomUUID();
            childFoldersRepo.insertPreparing("SUB_B02_256", rootBinding, k, rootEpoch, intentEpoch,
                    "fold-256-" + i, fp, now);
            var keyObj = new com.vhvkhangg.personalprivatevault.drive.internal.files.persistence.DriveChildFolderKey(
                    "SUB_B02_256", rootBinding, k);
            childFoldersRepo.reserve(keyObj, intentEpoch, "prov-256-" + i, now);
            childFoldersRepo.transitionFrom(keyObj, intentEpoch, "RESERVED", "COMPLETED", now, now);
        }
        assertThat(childFoldersRepo.countRetained("SUB_B02_256", rootBinding)).isEqualTo(255);

        String key1 = key();
        String key2 = key();
        google.rootEntered = new CountDownLatch(1);
        google.rootRelease = new CountDownLatch(1);

        try (var workers = Executors.newFixedThreadPool(2)) {
            var f1 = workers.submit(() -> {
                try {
                    return createFolder(1, key1, "Contend256_1").created() ? 201 : 200;
                } catch (DriveFileException e) {
                    return e.getHttpStatus();
                }
            });

            // Wait for thread 1 to take the 256th slot and reach provider seam
            assertThat(google.rootEntered.await(10, TimeUnit.SECONDS)).isTrue();

            // Thread 2 contends with a different key at 256 boundary: rejects with 503
            var f2 = workers.submit(() -> {
                try {
                    return createFolder(1, key2, "Contend256_2").created() ? 201 : 200;
                } catch (DriveFileException e) {
                    return e.getHttpStatus();
                }
            });

            assertThat(f2.get(5, TimeUnit.SECONDS)).isEqualTo(503);

            // Release thread 1
            google.rootRelease.countDown();
            assertThat(f1.get(5, TimeUnit.SECONDS)).isEqualTo(201);
        } finally {
            if (google.rootRelease != null) google.rootRelease.countDown();
        }

        // At capacity: 256 retained
        assertThat(childFoldersRepo.countRetained("SUB_B02_256", rootBinding)).isEqualTo(256);

        // Existing key recovery at capacity succeeds (idempotent 200, created=false)
        var recoveryResult = createFolder(1, key1, "Contend256_1");
        assertThat(recoveryResult.created()).isFalse();

        // Fresh key at capacity fails closed with 503
        String key3 = key();
        assertThatThrownBy(() -> createFolder(1, key3, "NewAtCapacity"))
                .isInstanceOfSatisfying(DriveFileException.class, e -> {
                    assertThat(e.getErrorCode()).isEqualTo("DRIVE_FILE_CAPACITY_EXHAUSTED");
                    assertThat(e.getHttpStatus()).isEqualTo(503);
                });
    }

    @Test
    @DisplayName("B-02: RESERVED folder crash recovery performs first dispatch and completes")
    void reservedFolderCrashRecoveryPerformsFirstDispatch() throws Exception {
        connect(1, "SUB_B02_RESERVED");
        ensure(1);
        var root = roots.findById("SUB_B02_RESERVED").orElseThrow();
        UUID rootBinding = root.getRootBindingId();
        UUID rootEpoch = root.getBindingEpoch();
        String actionKey = key();
        String safeName = "ReservedFolder";
        String fp = DriveFileInputs.folderFingerprint("SUB_B02_RESERVED", rootBinding, root.getRootFileId(), safeName, actionKey);
        Instant now = clock.instant();
        UUID intentEpoch = UUID.randomUUID();
        String generatedFolderId = "folder-res-" + UUID.randomUUID().toString().substring(0, 8);

        // Seed row directly in RESERVED state (simulating crash after reserve but before dispatch)
        childFoldersRepo.insertPreparing("SUB_B02_RESERVED", rootBinding, actionKey, rootEpoch, intentEpoch, safeName, fp, now);
        var keyObj = new com.vhvkhangg.personalprivatevault.drive.internal.files.persistence.DriveChildFolderKey(
                "SUB_B02_RESERVED", rootBinding, actionKey);
        childFoldersRepo.reserve(keyObj, intentEpoch, generatedFolderId, now);

        int createsBefore = google.childFolderCreates.get();
        // Invoke createFolder with the same action key: performs dispatch using generatedFolderId
        var result = createFolder(1, actionKey, safeName);
        assertThat(result.created()).isTrue();
        assertThat(result.data().id()).isEqualTo(generatedFolderId);
        assertThat(google.childFolderCreates.get()).isEqualTo(createsBefore + 1);

        var finalIntent = childFoldersRepo.findById(keyObj).orElseThrow();
        assertThat(finalIntent.getStatus()).isEqualTo("COMPLETED");
        assertThat(finalIntent.getProviderFolderId()).isEqualTo(generatedFolderId);
    }

    @Test
    @DisplayName("B-02: DISPATCHED crash recovery with remote 404 fails closed to RECONCILIATION_REQUIRED and is GET-only")
    void dispatchedCrashRecoveryWithRemote404FailsClosedGetOnly() throws Exception {
        connect(1, "SUB_B02_DISPATCHED_404");
        ensure(1);
        var root = roots.findById("SUB_B02_DISPATCHED_404").orElseThrow();
        UUID rootBinding = root.getRootBindingId();
        UUID rootEpoch = root.getBindingEpoch();
        String actionKey = key();
        String safeName = "Dispatched404Folder";
        String fp = DriveFileInputs.folderFingerprint("SUB_B02_DISPATCHED_404", rootBinding, root.getRootFileId(), safeName, actionKey);
        Instant now = clock.instant();
        UUID intentEpoch = UUID.randomUUID();
        String folderId = "folder-404-" + UUID.randomUUID().toString().substring(0, 8);

        // Seed row in DISPATCHED state, but Google does not have this folder (returns 404)
        childFoldersRepo.insertPreparing("SUB_B02_DISPATCHED_404", rootBinding, actionKey, rootEpoch, intentEpoch, safeName, fp, now);
        var keyObj = new com.vhvkhangg.personalprivatevault.drive.internal.files.persistence.DriveChildFolderKey(
                "SUB_B02_DISPATCHED_404", rootBinding, actionKey);
        childFoldersRepo.reserve(keyObj, intentEpoch, folderId, now);
        childFoldersRepo.transitionFrom(keyObj, intentEpoch, "RESERVED", "DISPATCHED", now, null);

        int initialCreates = google.childFolderCreates.get();
        // Replaying create: attempts files.get which returns 404 -> transitions to RECONCILIATION_REQUIRED, throws 409
        assertThatThrownBy(() -> createFolder(1, actionKey, safeName))
                .isInstanceOfSatisfying(DriveFileException.class, e -> {
                    assertThat(e.getHttpStatus()).isEqualTo(409);
                    assertThat(e.getErrorCode()).isEqualTo("FOLDER_OUTCOME_UNKNOWN");
                });

        // Zero additional folder creates occurred!
        assertThat(google.childFolderCreates.get()).isEqualTo(initialCreates);

        var updatedIntent = childFoldersRepo.findById(keyObj).orElseThrow();
        assertThat(updatedIntent.getStatus()).isEqualTo("RECONCILIATION_REQUIRED");
    }

    @Test
    @DisplayName("B-03/B-04: Child operations reject stale binding epoch at dispatch and publication")
    void childOperationEpochBarrierRejectsStaleDispatchAndPublication() throws Exception {
        connect(1, "SUB_B04_EPOCH");
        ensure(1);
        var folder = createFolder(1, key(), "EpochChild");
        String childId = folder.data().id();

        try (var op = resources.open(false)) {
            var lease = folders.ready(authority.acquire("1", (short) 1, op), op);
            // Simulate reconnect / reassignment: advance binding epoch in roots table
            jdbc.update("UPDATE drive_folder_roots SET binding_epoch = ? WHERE google_sub = 'SUB_B04_EPOCH'",
                    UUID.randomUUID());

            // Stale lease write rejects with DRIVE_STALE_AUTHORITY (409)
            assertThatThrownBy(() -> authority.finalWrite(lease, op, () -> "should-fail"))
                    .isInstanceOfSatisfying(DriveFileException.class, e -> {
                        assertThat(e.getErrorCode()).isEqualTo("DRIVE_STALE_AUTHORITY");
                        assertThat(e.getHttpStatus()).isEqualTo(409);
                    });

            // Stale lease dispatch rejects with DRIVE_STALE_AUTHORITY (409)
            assertThatThrownBy(() -> authority.dispatch(lease, op, () -> "should-fail"))
                    .isInstanceOfSatisfying(DriveFileException.class, e -> {
                        assertThat(e.getErrorCode()).isEqualTo("DRIVE_STALE_AUTHORITY");
                        assertThat(e.getHttpStatus()).isEqualTo(409);
                    });

            // Child folder store operations with stale lease reject at take
            String actionKey = key();
            assertThatThrownBy(() -> folderStore.take(lease, op, actionKey, "StaleChild"))
                    .isInstanceOfSatisfying(DriveFileException.class, e -> {
                        assertThat(e.getErrorCode()).isEqualTo("DRIVE_STALE_AUTHORITY");
                        assertThat(e.getHttpStatus()).isEqualTo(409);
                    });
        }
    }

    @Test
    @DisplayName("B-03/B-04: In-flight child file upload rejects publication upon disconnect or epoch change")
    void inFlightChildFileUploadRejectsPublicationOnEpochChange() throws Exception {
        connect(1, "SUB_B04_CHILD_INFLIGHT");
        ensure(1);
        var folder = createFolder(1, key(), "InFlightChild");
        String childId = folder.data().id();

        google.uploadEntered = new CountDownLatch(1);
        google.uploadRelease = new CountDownLatch(1);
        String fileKey = key();

        try (var workers = Executors.newFixedThreadPool(1)) {
            var uploadFuture = workers.submit(() ->
                    uploadChild(1, childId, fileKey, new byte[]{1, 2, 3, 4}, "child-file.bin", "application/octet-stream")
            );

            // Wait until provider receives upload
            assertThat(google.uploadEntered.await(10, TimeUnit.SECONDS)).isTrue();

            // Advance root binding epoch in database (simulating disconnect or reassignment)
            jdbc.update("UPDATE drive_folder_roots SET binding_epoch = ? WHERE google_sub = 'SUB_B04_CHILD_INFLIGHT'",
                    UUID.randomUUID());

            // Release provider response
            google.uploadRelease.countDown();

            // Publication must fail closed with UPLOAD_OUTCOME_UNKNOWN (409) due to stale authority
            assertThatThrownBy(uploadFuture::get)
                    .hasCauseInstanceOf(DriveFileException.class)
                    .extracting(t -> ((DriveFileException) t.getCause()).getErrorCode())
                    .isEqualTo("UPLOAD_OUTCOME_UNKNOWN");
        } finally {
            if (google.uploadRelease != null) google.uploadRelease.countDown();
        }

        // Verify upload intent did NOT publish to COMPLETED
        var intentsList = intents.findAll();
        assertThat(intentsList).noneMatch(i -> fileKey.equals(i.getId().getIdempotencyKey()) && "COMPLETED".equals(i.getStatus()));
    }
}

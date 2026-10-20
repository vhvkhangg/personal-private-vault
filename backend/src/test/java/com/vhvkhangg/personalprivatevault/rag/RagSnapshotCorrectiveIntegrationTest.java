package com.vhvkhangg.personalprivatevault.rag;

import com.vhvkhangg.personalprivatevault.rag.api.*;
import com.vhvkhangg.personalprivatevault.rag.source.*;
import com.vhvkhangg.personalprivatevault.rag.internal.application.*;
import com.vhvkhangg.personalprivatevault.rag.internal.domain.*;
import com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.persistence.*;
import com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.storage.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.support.TransactionTemplate;
import java.io.*;
import java.nio.file.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Faults/barriers wrap real acquisition, SQL transactions and production recovery, never replace their result. */
@Timeout(90)
class RagSnapshotCorrectiveIntegrationTest extends AbstractRagIntegrationTest {
    static final Path ROOT=SNAPSHOT_TEST_ROOT;
    @DynamicPropertySource static void storage(DynamicPropertyRegistry r) {
        r.add("ppv.rag.reconciliation-delay-ms",()->"3600000");
    }
    @Autowired RagOperations rag;
    @MockitoSpyBean RagExternalFileAuthority external;
    @Autowired RagRecoveryService recovery;
    @Autowired org.springframework.context.ApplicationContext beans;
    @MockitoSpyBean RagRuntimeSecurity security;
    @Autowired RagRuntimeIdentity runtime;
    @MockitoSpyBean RagSnapshotAcquisitionService acquisition;
    @MockitoSpyBean RagOriginalBytesStore store;
    @MockitoSpyBean PlatformTransactionManager transactions;
    @TempDir Path originals;

    Path file(String content) throws IOException { return Files.writeString(originals.resolve(UUID.randomUUID()+".txt"),content); }
    RagSourceView register(Path path) { return rag.registerSnapshotCandidate(path.toString()); }
    RagSnapshotCaptureCommand update(Path path,RagSourceView preview) { return new RagSnapshotCaptureCommand(path.toString(),preview.snapshot()); }
    RagConsentCommand consent(RagSourceView view) { return new RagConsentCommand(Set.of("content"),view.sourceVersion(),view.consentResetVersion(),view.authorizationEpoch(),view.fieldPolicyVersion(),null,view.snapshot()); }
    RagExternalFileSource row(RagSourceView view) { return external.findByFileSourceId(UUID.fromString(view.sourceId())); }
    Path bytes(RagSourceView view) { return ROOT.resolve("revisions").resolve(view.sourceId()).resolve(view.snapshot().pendingRevisionId()+".orig"); }
    Path manifest(RagSourceView view) { return bytes(view).resolveSibling(view.snapshot().pendingRevisionId()+".manifest.json"); }
    void reconcileAll() { for(int i=0;i<12;i++) recovery.reconcileSnapshots(); }
    void invariant(RagSourceView view) {
        var ext=row(view);
        assertThat(ext.getSelectedRevisionId()).isNull();
        assertThat(ext.getSourceVersion()).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.index_generations",Integer.class)).isZero();
    }
    void valid(RagSourceView view) { assertThat(store.verifyIntegrity(UUID.fromString(view.sourceId()),view.snapshot().pendingRevisionId(),view.snapshot().pendingSha256(),view.snapshot().pendingByteLength())).isTrue(); }

    @Test void authenticatedApiBindsExactPendingPreview() throws Exception {
        Path path=file("A private whole document");
        String registration=objectMapper.writeValueAsString(Map.of("filePath",path.toString()));
        mockMvc.perform(post("/api/v1/rag/sources/register-file").contentType("application/json").content(registration)).andExpect(status().isUnauthorized());
        String body=mockMvc.perform(post("/api/v1/rag/sources/register-file").header("Authorization",bearerHeader())
                .contentType("application/json").content(registration)).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        long id=objectMapper.readTree(body).path("data").path("id").asLong();
        var a=rag.findSource(id).orElseThrow();
        assertThat(objectMapper.readTree(body).path("data").path("snapshot").path("pendingRevisionId").asText()).isEqualTo(a.snapshot().pendingRevisionId().toString());
        Files.writeString(path,"B different whole document");
        mockMvc.perform(post("/api/v1/rag/sources/"+id+"/capture-revision").header("Authorization",bearerHeader())
                .contentType("application/json").content(objectMapper.writeValueAsString(update(path,a)))).andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/rag/sources/"+id+"/approve").header("Authorization",bearerHeader())
                .contentType("application/json").content(objectMapper.writeValueAsString(consent(a)))).andExpect(status().isConflict());
        var b=rag.findSource(id).orElseThrow();
        mockMvc.perform(post("/api/v1/rag/sources/"+id+"/approve").header("Authorization",bearerHeader())
                .contentType("application/json").content(objectMapper.writeValueAsString(consent(b)))).andExpect(status().isOk());
        assertThat(row(b).getApprovedPendingRevisionId()).isEqualTo(b.snapshot().pendingRevisionId());
        mockMvc.perform(post("/api/v1/rag/sources/"+id+"/capture-revision").header("Authorization",bearerHeader())
                .contentType("application/json").content(registration)).andExpect(status().isBadRequest());
        invariant(b);
    }

    @Test void replacementBetweenVerificationAndApprovalIsRejected() throws Exception {
        Path path=file("A"); var a=register(path); AtomicBoolean once=new AtomicBoolean();
        doAnswer(call -> {
            Object verified=call.callRealMethod();
            if (once.compareAndSet(false,true)) {
                Files.writeString(path,"B");
                rag.capturePendingRevision(a.id(),update(path,a));
            }
            return verified;
        }).when(store).verifyIntegrity(eq(UUID.fromString(a.sourceId())),eq(a.snapshot().pendingRevisionId()),anyString(),anyLong());
        assertThatThrownBy(()->rag.approveSource(a.id(),consent(a))).isInstanceOf(RagSourceChangedException.class);
        var b=rag.findSource(a.id()).orElseThrow();
        assertThat(b.snapshot().pendingRevisionId()).isNotEqualTo(a.snapshot().pendingRevisionId());
        assertThat(b.accessState()).isEqualTo(RagAccessState.DENIED); valid(b); invariant(b);
    }

    @ParameterizedTest @ValueSource(strings={"version","reset","authorization","selection","fileAuthorization"})
    void staleCaptureContextDeniesBeforeCopy(String change) throws Exception {
        Path path=file("A"); var a=register(path);
        switch(change) {
            case "version" -> jdbcTemplate.update("UPDATE rag.external_file_sources SET source_version=2 WHERE file_source_id=?",UUID.fromString(a.sourceId()));
            case "reset" -> jdbcTemplate.update("UPDATE rag.external_file_sources SET source_version=2,consent_reset_version=2 WHERE file_source_id=?",UUID.fromString(a.sourceId()));
            case "authorization" -> jdbcTemplate.update("UPDATE rag.sources SET authorization_epoch=authorization_epoch+1 WHERE id=?",a.id());
            case "selection" -> jdbcTemplate.update("UPDATE rag.external_file_sources SET selected_revision_id=? WHERE file_source_id=?",UUID.randomUUID(),UUID.fromString(a.sourceId()));
            default -> jdbcTemplate.update("UPDATE rag.external_file_sources SET authorization_epoch=2 WHERE file_source_id=?",UUID.fromString(a.sourceId()));
        }
        clearInvocations(acquisition);
        assertThatThrownBy(()->rag.capturePendingRevision(a.id(),update(path,a))).isInstanceOf(RagSourceChangedException.class);
        verify(acquisition,never()).acquireBounded(any(),any()); valid(a);
    }

    @ParameterizedTest @ValueSource(strings={"selection","reset","authorization"})
    void authorityChangeAfterSealRejectsCapturedIntent(String change) throws Exception {
        Path path=file("A"); var a=register(path); Files.writeString(path,"B");
        UUID selected=UUID.randomUUID();
        doAnswer(call->{
            Object sealed=call.callRealMethod();
            switch(change) {
                case "selection" -> jdbcTemplate.update("UPDATE rag.external_file_sources SET selected_revision_id=? WHERE file_source_id=?",selected,UUID.fromString(a.sourceId()));
                case "reset" -> jdbcTemplate.update("UPDATE rag.external_file_sources SET source_version=2,consent_reset_version=2 WHERE file_source_id=?",UUID.fromString(a.sourceId()));
                default -> jdbcTemplate.update("UPDATE rag.sources SET authorization_epoch=authorization_epoch+1 WHERE id=?",a.id());
            }
            return sealed;
        }).when(store).sealRevision(any(),any(),any(),anyString(),anyLong());
        assertThatThrownBy(()->rag.capturePendingRevision(a.id(),update(path,a))).isInstanceOf(RagSourceChangedException.class);
        assertThat(row(a).getPendingRevisionId()).isEqualTo(a.snapshot().pendingRevisionId());
        assertThat(row(a).getSelectedRevisionId()).isEqualTo(change.equals("selection") ? selected : null);
        valid(a);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.index_generations",Integer.class)).isZero();
    }

    @Test void simultaneousFirstImportsHaveOneLogicalWinner() throws Exception {
        Path path=file("A"); var barrier=new CyclicBarrier(2);
        doAnswer(call->{ Object sealed=call.callRealMethod(); barrier.await(10,TimeUnit.SECONDS); return sealed; })
                .when(store).sealRevision(any(),any(),any(),anyString(),anyLong());
        try(var threads=Executors.newFixedThreadPool(2)) {
            var first=threads.submit(()->attempt(()->register(path)));
            var second=threads.submit(()->attempt(()->register(path)));
            var results=List.of(first.get(20,TimeUnit.SECONDS),second.get(20,TimeUnit.SECONDS));
            assertThat(results.stream().filter(RagSourceView.class::isInstance).count()).isEqualTo(1);
            assertThat(results.stream().filter(RagSourceChangedException.class::isInstance).count()).isEqualTo(1);
            RagSourceView winner=(RagSourceView)results.stream().filter(RagSourceView.class::isInstance).findFirst().orElseThrow();
            reconcileAll(); valid(winner); invariant(winner);
            assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.external_file_sources",Integer.class)).isEqualTo(1);
            try(var files=Files.walk(ROOT.resolve("revisions"))) { assertThat(files.filter(Files::isRegularFile).count()).isEqualTo(2); }
        }
    }

    @ParameterizedTest @ValueSource(strings={"register","approve","revoke","competingCapture"})
    void guardedRacesCannotReplaceStalePending(String competing) throws Exception {
        Path path=file("A"); var a=register(path); Files.writeString(path,"B");
        var ready=new CountDownLatch(1); var release=new CountDownLatch(1); AtomicBoolean once=new AtomicBoolean();
        doAnswer(call->{
            if (once.compareAndSet(false,true)) { ready.countDown(); assertThat(release.await(15,TimeUnit.SECONDS)).isTrue(); }
            return call.callRealMethod();
        }).when(acquisition).acquireBounded(any(),any());
        try(var threads=Executors.newSingleThreadExecutor()) {
            var pending=threads.submit(()->attempt(()->rag.capturePendingRevision(a.id(),update(path,a))));
            assertThat(ready.await(10,TimeUnit.SECONDS)).isTrue();
            Object other=switch(competing) {
                case "register" -> attempt(()->register(path));
                case "approve" -> rag.approveSource(a.id(),consent(a));
                case "revoke" -> rag.revokeSource(a.id());
                default -> rag.capturePendingRevision(a.id(),update(path,a));
            };
            release.countDown(); Object result=pending.get(20,TimeUnit.SECONDS);
            if (competing.equals("register")) {
                assertThat(other).isInstanceOf(RagSourceChangedException.class);
                assertThat(result).isInstanceOf(RagSourceView.class); valid((RagSourceView)result);
            } else {
                assertThat(result).isInstanceOfAny(RagSourceChangedException.class,CancellationException.class);
                if (!competing.equals("revoke")) valid(rag.findSource(a.id()).orElseThrow());
            }
            invariant(a);
        } finally { release.countDown(); }
    }

    @ParameterizedTest @ValueSource(strings={"approve","revoke"})
    void reregistrationSerializesWithApprovalAndRevokeWithoutFilesystemMutation(String action) throws Exception {
        Path path=file("A"); var a=register(path); Files.writeString(path,"B");
        var held=new CountDownLatch(1); var release=new CountDownLatch(1); var once=new AtomicBoolean();
        doAnswer(call->{
            Object guarded=call.callRealMethod();
            if(once.compareAndSet(false,true)) { held.countDown(); assertThat(release.await(12,TimeUnit.SECONDS)).isTrue(); }
            return guarded;
        }).when(external).requireEditable(anyCollection());
        try(var threads=Executors.newFixedThreadPool(2)) {
            var registration=threads.submit(()->attempt(()->register(path)));
            assertThat(held.await(8,TimeUnit.SECONDS)).isTrue();
            var authority=threads.submit(()->action.equals("approve") ? rag.approveSource(a.id(),consent(a)) : rag.revokeSource(a.id()));
            awaitCompetingLock(Duration.ofSeconds(5));
            valid(a); // first import denial has not removed any registered bytes while guarded
            release.countDown();
            assertThat(registration.get(15,TimeUnit.SECONDS)).isInstanceOf(RagSourceChangedException.class);
            var actual=authority.get(15,TimeUnit.SECONDS);
            assertThat(actual.accessState()).isEqualTo(action.equals("approve") ? RagAccessState.APPROVED : RagAccessState.REVOKED);
            if(action.equals("approve")) valid(a); else assertThat(Files.exists(bytes(a))).isFalse();
            invariant(a);
        } finally { release.countDown(); }
    }

    @Test void reservedReregistrationAndCaptureDoNotTouchRegisteredBytes() throws Exception {
        Path path=file("A"); var a=register(path); UUID token=UUID.randomUUID();
        new TransactionTemplate(transactions).executeWithoutResult(tx->external.reserve(Set.of(UUID.fromString(a.sourceId())),token));
        try {
            Files.writeString(path,"B"); clearInvocations(store);
            assertThatThrownBy(()->register(path)).isInstanceOf(RuntimeException.class);
            assertThatThrownBy(()->rag.capturePendingRevision(a.id(),update(path,a))).isInstanceOf(RuntimeException.class);
            verify(store,never()).sealRevision(any(),any(),any(),anyString(),anyLong());
            verify(store,never()).deleteRevision(any(),any()); valid(a);
        } finally { new TransactionTemplate(transactions).executeWithoutResult(tx->external.release(Set.of(UUID.fromString(a.sourceId())),token)); }
        invariant(a);
    }

    @ParameterizedTest @ValueSource(strings={"orig","manifest","partial","tamper"})
    void identicalCaptureDeniesCorruptPending(String fault) throws Exception {
        Path path=file("ABCD"); var a=register(path);
        switch(fault) {
            case "orig" -> { bytes(a).toFile().setWritable(true); Files.delete(bytes(a)); }
            case "manifest" -> { manifest(a).toFile().setWritable(true); Files.delete(manifest(a)); }
            case "partial" -> { manifest(a).toFile().setWritable(true); Files.writeString(manifest(a),"{"); }
            default -> { bytes(a).toFile().setWritable(true); Files.writeString(bytes(a),"WXYZ"); }
        }
        assertThatThrownBy(()->rag.capturePendingRevision(a.id(),update(path,a))).isInstanceOf(IllegalStateException.class);
        assertThat(row(a).isSnapshotIntegrityFailed()).isTrue();
        assertThat(row(a).getPendingRevisionId()).isEqualTo(a.snapshot().pendingRevisionId()); invariant(a);
        assertThatThrownBy(()->register(path)).isInstanceOf(RagSourceChangedException.class);
    }

    @Test void validSelectedDescriptorDistinguishesChangedBytesAndCorruption() throws Exception {
        Path path=file("A"); var a=register(path);
        jdbcTemplate.update("UPDATE rag.external_file_sources SET selected_revision_id=pending_revision_id,selected_revision_hash=pending_revision_hash,selected_byte_length=pending_byte_length WHERE file_source_id=?",UUID.fromString(a.sourceId()));
        var selected=rag.findSource(a.id()).orElseThrow();
        var equal=rag.capturePendingRevision(a.id(),update(path,selected));
        assertThat(equal.snapshot().pendingRevisionId()).isEqualTo(a.snapshot().pendingRevisionId());
        Files.writeString(path,"B"); var changed=rag.capturePendingRevision(a.id(),update(path,equal));
        assertThat(changed.snapshot().pendingRevisionId()).isNotEqualTo(a.snapshot().pendingRevisionId());
        valid(changed); valid(a); // old R remains referenced by selected pointer
        bytes(a).toFile().setWritable(true); Files.writeString(bytes(a),"C");
        assertThatThrownBy(()->rag.capturePendingRevision(a.id(),update(path,changed))).isInstanceOf(IllegalStateException.class);
        assertThat(row(a).isSnapshotIntegrityFailed()).isTrue();
    }

    @Test void dbFailureAfterSealLeavesNoRegisteredOrAdoptedRevision() throws Exception {
        Path path=file("A");
        installMetadataFailure("INSERT");
        try { assertThatThrownBy(()->register(path)).hasStackTraceContaining("Injected snapshot metadata failure"); }
        finally { removeMetadataFailure(); }
        reconcileAll();
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.external_file_sources",Integer.class)).isZero();
        try(var files=Files.walk(ROOT)) { assertThat(files.filter(Files::isRegularFile).count()).isZero(); }
    }

    @Test void replacementRollbackNeverDeletesPriorRegisteredBytes() throws Exception {
        Path path=file("A"); var a=register(path); Files.writeString(path,"B");
        installMetadataFailure("UPDATE");
        try { assertThatThrownBy(()->rag.capturePendingRevision(a.id(),update(path,a))).hasStackTraceContaining("Injected snapshot metadata failure"); }
        finally { removeMetadataFailure(); }
        reconcileAll(); valid(a); invariant(a);
        assertThat(row(a).getPendingRevisionId()).isEqualTo(a.snapshot().pendingRevisionId());
        try(var files=Files.walk(ROOT.resolve("revisions"))) { assertThat(files.filter(Files::isRegularFile).count()).isEqualTo(2); }
    }

    // Actual PostgreSQL statement failure after sealing, in the disposable integration DB.
    void installMetadataFailure(String event) throws Exception {
        try(var connection=java.sql.DriverManager.getConnection(POSTGRES.getJdbcUrl(),POSTGRES.getUsername(),POSTGRES.getPassword());
                var statement=connection.createStatement()) {
            statement.execute("CREATE FUNCTION rag.test_s1_metadata_failure() RETURNS trigger LANGUAGE plpgsql AS $$ BEGIN RAISE EXCEPTION 'Injected snapshot metadata failure' USING ERRCODE='40001'; END $$");
            statement.execute("CREATE TRIGGER test_s1_metadata_failure BEFORE "+event+" ON rag.external_file_sources FOR EACH ROW EXECUTE FUNCTION rag.test_s1_metadata_failure()");
        }
    }
    void removeMetadataFailure() throws Exception {
        try(var connection=java.sql.DriverManager.getConnection(POSTGRES.getJdbcUrl(),POSTGRES.getUsername(),POSTGRES.getPassword());
                var statement=connection.createStatement()) {
            statement.execute("DROP TRIGGER test_s1_metadata_failure ON rag.external_file_sources");
            statement.execute("DROP FUNCTION rag.test_s1_metadata_failure()");
        }
    }

    @ParameterizedTest @ValueSource(booleans={true,false})
    void uncertainCommitIsReconciledFromFreshDatabaseWithoutDeletingReferences(boolean committed) throws Exception {
        Path path=file("A"); var a=register(path); Files.writeString(path,"B"); AtomicBoolean armed=new AtomicBoolean();
        doAnswer(call->{ Object saved=call.callRealMethod(); armed.set(true); return saved; }).when(external).save(any());
        doAnswer(call->{
            if (!armed.compareAndSet(true,false)) return call.callRealMethod();
            if (committed) call.callRealMethod(); else transactions.rollback(call.getArgument(0));
            throw new TransactionSystemException("Injected unknown outcome");
        }).when(transactions).commit(any(TransactionStatus.class));
        assertThatThrownBy(()->rag.capturePendingRevision(a.id(),update(path,a))).isInstanceOf(TransactionSystemException.class);
        doCallRealMethod().when(transactions).commit(any()); doCallRealMethod().when(external).save(any());
        reconcileAll(); var actual=rag.findSource(a.id()).orElseThrow(); valid(actual); invariant(actual);
        assertThat(actual.snapshot().pendingRevisionId().equals(a.snapshot().pendingRevisionId())).isEqualTo(!committed);
        assertThat(Files.exists(bytes(a))).isEqualTo(!committed);
    }

    @Test void controlledStartupReconcilesTempsOrphansPartialManifestAndDeletionRetry() throws Exception {
        Path path=file("authorized"); var a=register(path);
        UUID cap=UUID.randomUUID(),source=UUID.randomUUID(),rev=UUID.randomUUID();
        var temp=store.writeTemp(cap,new ByteArrayInputStream(new byte[]{1,2,3}),null,null);
        store.sealRevision(source,rev,cap,temp.sha256(),temp.byteLength());
        Path partial=ROOT.resolve("revisions").resolve(source.toString()).resolve(rev+".manifest.json");
        partial.toFile().setWritable(true); Files.writeString(partial,"{");
        store.writeTemp(UUID.randomUUID(),new ByteArrayInputStream(new byte[]{4}),null,null);
        // Real Windows denial, then a fresh production store/recovery instance retries after release.
        try(var held=new FileInputStream(bytes(a).toFile())) {
            rag.revokeSource(a.id());
            assertThat(Files.exists(bytes(a))).isTrue();
            var restarted=restartRecovery();
            for(int i=0;i<8;i++) restarted.reconcileSnapshots();
            assertThat(Files.exists(bytes(a))).isTrue();
            assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.external_file_sources WHERE eligible",Integer.class)).isZero();
        }
        var restarted=restartRecovery(); for(int i=0;i<8;i++) restarted.reconcileSnapshots();
        try(var files=Files.walk(ROOT)) { assertThat(files.filter(Files::isRegularFile).count()).isZero(); }
        invariant(a);
    }

    RagRecoveryService restartRecovery() {
        var fresh=new RagOriginalBytesStore(ROOT,store.getMaxSourceBytes());
        var restarted=new RagRecoveryService(beans.getBean(jakarta.persistence.EntityManager.class),beans.getBean(RagRuntimeIdentity.class),
                beans.getBean(RagPurgeService.class),transactions,beans.getBean(RagRuntimeSecurity.class),
                beans.getBean(RagWorkerSupervisor.class),beans.getBean(RagGenerationState.class),fresh);
        restarted.afterSingletonsInstantiated(); return restarted;
    }

    @ParameterizedTest @ValueSource(strings={"midCopy","preSeal","preCommit","preCommitDeadline"})
    void cancellationAndDeadlineCannotCommitPendingReplacement(String point) throws Exception {
        Path path=file("A"); var a=register(path); Files.write(path,new byte[200000]);
        AtomicReference<RagSnapshotAcquisitionService.CaptureContext> context=new AtomicReference<>();
        doAnswer(call->{ var c=point.equals("preCommitDeadline")
                ? new RagSnapshotAcquisitionService.CaptureContext(Duration.ofMillis(250),null)
                : (RagSnapshotAcquisitionService.CaptureContext)call.callRealMethod(); context.set(c); return c; }).when(acquisition).newContext();
        if(point.equals("midCopy")) doAnswer(call->{
            InputStream original=call.getArgument(1); AtomicBoolean first=new AtomicBoolean();
            call.getArguments()[1]=new FilterInputStream(original) {
                @Override public int read(byte[] b,int off,int len) throws IOException {
                    int n=super.read(b,off,len); if(n>0 && first.compareAndSet(false,true)) context.get().cancel(); return n;
                }
            }; return call.callRealMethod();
        }).when(store).writeTemp(any(),any(),any(),any());
        if(point.equals("preSeal")) doAnswer(call->{ Object copied=call.callRealMethod(); context.get().cancel(); return copied; }).when(acquisition).acquireBounded(any(),any());
        if(point.startsWith("preCommit")) doAnswer(call->{ Object saved=call.callRealMethod();
            if(point.equals("preCommit")) context.get().cancel();
            else { Thread.sleep(300); }
            return saved; }).when(external).save(any());
        assertThatThrownBy(()->rag.capturePendingRevision(a.id(),update(path,a))).isInstanceOf(CancellationException.class);
        reconcileAll(); valid(a); invariant(a);
        assertThat(row(a).getPendingRevisionId()).isEqualTo(a.snapshot().pendingRevisionId());
        try(var files=Files.list(ROOT.resolve("temp"))) { assertThat(files.count()).isZero(); }
    }

    @Test void revokeDuringActualCopyCancelsAndCleansTemporaryBytes() throws Exception {
        Path path=file("A"); var a=register(path); Files.write(path,new byte[200000]);
        var copied=new CountDownLatch(1); var release=new CountDownLatch(1);
        doAnswer(call->{ InputStream original=call.getArgument(1); AtomicBoolean once=new AtomicBoolean();
            call.getArguments()[1]=new FilterInputStream(original) {
                @Override public int read(byte[] b,int off,int len) throws IOException {
                    int n=super.read(b,off,len);
                    if(n>0 && once.compareAndSet(false,true)) {
                        copied.countDown();
                        try { if(!release.await(10,TimeUnit.SECONDS)) throw new IOException("Barrier timeout"); }
                        catch(InterruptedException e) { Thread.currentThread().interrupt(); throw new IOException("Interrupted"); }
                    }
                    return n;
                }
            }; return call.callRealMethod();
        }).when(store).writeTemp(any(),any(),any(),any());
        try(var threads=Executors.newSingleThreadExecutor()) {
            var capture=threads.submit(()->attempt(()->rag.capturePendingRevision(a.id(),update(path,a))));
            assertThat(copied.await(10,TimeUnit.SECONDS)).isTrue();
            assertThat(rag.revokeSource(a.id()).accessState()).isEqualTo(RagAccessState.REVOKED);
            release.countDown(); assertThat(capture.get(15,TimeUnit.SECONDS)).isInstanceOf(CancellationException.class);
            reconcileAll(); invariant(a);
            try(var files=Files.walk(ROOT)) { assertThat(files.filter(Files::isRegularFile).count()).isZero(); }
        } finally { release.countDown(); }
    }

    @Test void missingRegisteredPairIsDurablyDeniedByStartupWithoutOriginalReread() throws Exception {
        Path path=file("A"); var a=register(path);
        bytes(a).toFile().setWritable(true); manifest(a).toFile().setWritable(true);
        Files.delete(bytes(a)); Files.delete(manifest(a)); Files.delete(path);
        var restarted=restartRecovery(); restarted.reconcileSnapshots();
        assertThat(row(a).isSnapshotIntegrityFailed()).isTrue();
        assertThat(rag.findSource(a.id()).orElseThrow().accessState()).isEqualTo(RagAccessState.REVOKED);
        assertThat(row(a).getPendingRevisionId()).isEqualTo(a.snapshot().pendingRevisionId()); invariant(a);
    }

    @Test void workerCannotReadOrWriteNewSnapshotAuthorityColumns() {
        assertThat(jdbcTemplate.queryForObject("SELECT has_column_privilege('rag_worker','rag.external_file_sources','pending_revision_id','SELECT')",Boolean.class)).isFalse();
        assertThat(jdbcTemplate.queryForObject("SELECT has_column_privilege('rag_worker','rag.external_file_sources','snapshot_integrity_failed','UPDATE')",Boolean.class)).isFalse();
    }

    @Test void observableMidCopyMutationIsDenied() throws Exception {
        Path path=file("A"); var a=register(path); Files.write(path,new byte[200000]);
        doAnswer(call->{ InputStream original=call.getArgument(1); AtomicBoolean once=new AtomicBoolean();
            call.getArguments()[1]=new FilterInputStream(original) {
                @Override public int read(byte[] b,int off,int len) throws IOException {
                    int n=super.read(b,off,len);
                    if(n>0 && once.compareAndSet(false,true)) Files.setLastModifiedTime(path,java.nio.file.attribute.FileTime.from(Instant.now().plusSeconds(10)));
                    return n;
                }
            }; return call.callRealMethod();
        }).when(store).writeTemp(any(),any(),any(),any());
        assertThatThrownBy(()->rag.capturePendingRevision(a.id(),update(path,a))).isInstanceOf(IllegalStateException.class);
        valid(a); invariant(a);
    }

    @Test void cleanupExceptionReleasesCaptureForDurableProductionRetry() throws Exception {
        Path path=file("A"); var a=register(path); Files.writeString(path,"B");
        var captureId=new AtomicReference<UUID>(); var firstCleanup=new AtomicBoolean();
        doAnswer(call->{
            captureId.set(call.getArgument(0));
            call.callRealMethod();
            throw new IllegalStateException("Injected failure after temporary write");
        }).when(store).writeTemp(any(),any(),any(),any());
        doAnswer(call->{
            if(firstCleanup.compareAndSet(false,true)) throw new SnapshotStorageException("Injected inaccessible cleanup");
            return call.callRealMethod();
        }).when(store).cleanupTemp(any());
        assertThatThrownBy(()->rag.capturePendingRevision(a.id(),update(path,a))).isInstanceOf(SnapshotStorageException.class);
        assertThat(store.active(new RagOriginalBytesStore.StorageEntry(null,null,captureId.get(),true))).isFalse();
        reconcileAll(); valid(a); invariant(a);
        try(var files=Files.list(ROOT.resolve("temp"))) { assertThat(files.count()).isZero(); }
    }

    @ParameterizedTest @ValueSource(strings={"register","capture"})
    void runtimeReplacementBetweenSealAndCommitDeniesStaleController(String operation) throws Exception {
        Path path=file("A"); var a=register(path); Files.writeString(path,"B");
        Path newFile=file("new source");
        doAnswer(call->{
            Object sealed=call.callRealMethod();
            jdbcTemplate.update("UPDATE rag.runtime_state SET current_instance=? WHERE singleton",UUID.randomUUID());
            return sealed;
        }).when(store).sealRevision(any(),any(),any(),anyString(),anyLong());
        try {
            assertThatThrownBy(()->{
                if(operation.equals("register")) register(newFile);
                else rag.capturePendingRevision(a.id(),update(path,a));
            }).isInstanceOf(IllegalStateException.class).hasMessageContaining("runtime");
            assertThat(row(a).getPendingRevisionId()).isEqualTo(a.snapshot().pendingRevisionId());
            assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.external_file_sources",Integer.class)).isEqualTo(1);
            valid(a); invariant(a);
        } finally { jdbcTemplate.update("UPDATE rag.runtime_state SET current_instance=? WHERE singleton",runtime.instance()); }
        reconcileAll();
        try(var files=Files.walk(ROOT)) { assertThat(files.filter(Files::isRegularFile).count()).isEqualTo(2); }
    }

    @Test void recoveryRechecksRuntimeAfterAcquiringGraphBeforeDeletingAnything() throws Exception {
        var temp=store.writeTemp(UUID.randomUUID(),new ByteArrayInputStream(new byte[]{1}),null,null);
        AtomicBoolean once=new AtomicBoolean();
        doAnswer(call->{
            boolean current=(boolean)call.callRealMethod();
            if(current && once.compareAndSet(false,true))
                jdbcTemplate.update("UPDATE rag.runtime_state SET current_instance=? WHERE singleton",UUID.randomUUID());
            return current;
        }).when(security).isCurrentRuntime();
        try {
            store.resetRecoveryScan(); recovery.reconcileSnapshots();
            assertThat(Files.exists(temp.tempPath())).isTrue();
        } finally {
            doCallRealMethod().when(security).isCurrentRuntime();
            jdbcTemplate.update("UPDATE rag.runtime_state SET current_instance=? WHERE singleton",runtime.instance());
        }
        reconcileAll(); assertThat(Files.exists(temp.tempPath())).isFalse();
    }

    @Test void restoredPrivateLiveLocatorCanBeTombstonedWithoutOpeningOrDisclosingIt() throws Exception {
        Path original=file("private original unchanged");
        Long id=jdbcTemplate.queryForObject("INSERT INTO rag.sources(source_type,source_id,classification,approved_fields_text,access_state,source_revision_hash,description_revision_hash,created_at,updated_at) VALUES ('EXTERNAL_FILE',?,'NON_PRIVATE','content','APPROVED','old','old',clock_timestamp(),clock_timestamp()) RETURNING id",Long.class,original.toString());
        clearInvocations(acquisition,store);
        var revoked=rag.revokeSource(id);
        assertThat(revoked.accessState()).isEqualTo(RagAccessState.REVOKED);
        assertThat(revoked.sourceId()).isEqualTo("disabled:"+id);
        assertThat(revoked.toString()).doesNotContain(original.toString(),original.getFileName().toString());
        verify(acquisition,never()).acquireBounded(any(),any());
        verify(store,never()).deleteRevision(any(),any());
        assertThat(Files.readString(original)).isEqualTo("private original unchanged");
    }

    @Test void boundedInventoryMakesProgressWithoutFullScan() throws Exception {
        for(int i=0;i<60;i++) store.writeTemp(UUID.randomUUID(),new ByteArrayInputStream(new byte[]{1}),null,null);
        store.resetRecoveryScan(); recovery.reconcileSnapshots();
        try(var files=Files.list(ROOT.resolve("temp"))) { assertThat(files.count()).isBetween(35L,60L); }
        reconcileAll(); try(var files=Files.list(ROOT.resolve("temp"))) { assertThat(files.count()).isZero(); }
    }

    static Object attempt(Callable<?> action) { try { return action.call(); } catch(Exception failure) { return failure; } }
}

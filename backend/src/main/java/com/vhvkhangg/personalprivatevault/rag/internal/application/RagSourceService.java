package com.vhvkhangg.personalprivatevault.rag.internal.application;

import com.vhvkhangg.personalprivatevault.fiction.source.FictionSourceOperations;
import com.vhvkhangg.personalprivatevault.film.source.FilmSourceOperations;
import com.vhvkhangg.personalprivatevault.rag.api.RagConsentCommand;
import com.vhvkhangg.personalprivatevault.rag.api.RagSnapshotCaptureCommand;
import com.vhvkhangg.personalprivatevault.rag.api.RagSourceChangedException;
import com.vhvkhangg.personalprivatevault.rag.internal.domain.*;
import com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.persistence.*;
import com.vhvkhangg.personalprivatevault.rag.source.*;
import com.vhvkhangg.personalprivatevault.vault.entry.*;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.nio.file.Path;
import java.util.*;
import com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.storage.RagOriginalBytesStore;

/** Standing item consent and owner intent, independent of worker credentials. */
@Service
@Transactional(readOnly = true)
public class RagSourceService {
    private final RagSourceRepository sources;
    private final RagDependencyRepository dependencies;
    private final RagDependencyGraph graph;
    private final RagPurgeService purge;
    private final FictionSourceOperations fiction;
    private final FilmSourceOperations film;
    private final VaultEntryOperations vault;
    private final SourceIndexReservations reservations;
    private final SourceEditGuard edits;
    private final EntityManager entityManager;
    private final RagRuntimeIdentity runtime;
    private final Clock clock;
    private final int maxDepth;
    private final RagLimits limits;
    private final TransactionTemplate transaction;
    private final RagExternalFileAuthority externalFileAuthority;
    private final RagSnapshotAcquisitionService acquisitionService;
    private final RagOriginalBytesStore byteStore;
    private final RagRecoveryService recovery;
    private final RagRuntimeSecurity security;
    private final RagNoChangeService noChange;
    private final RagSnapshotInspections inspections;
    private final Map<Long, Set<RagSnapshotAcquisitionService.CaptureContext>> captures = new java.util.concurrent.ConcurrentHashMap<>();

    public RagSourceService(RagSourceRepository sources, RagDependencyRepository dependencies, RagDependencyGraph graph,
            RagPurgeService purge, FictionSourceOperations fiction, FilmSourceOperations film, VaultEntryOperations vault,
            SourceIndexReservations reservations, SourceEditGuard edits, EntityManager entityManager,
            RagRuntimeIdentity runtime, Clock clock, RagLimits limits, PlatformTransactionManager transactions,
            RagExternalFileAuthority externalFileAuthority, RagSnapshotAcquisitionService acquisitionService,
            RagOriginalBytesStore byteStore, RagRecoveryService recovery, RagRuntimeSecurity security,
            RagNoChangeService noChange,RagSnapshotInspections inspections,
            @Value("${ppv.rag.limits.max-dependency-depth:5}") int maxDepth) {
        this.sources = sources; this.dependencies = dependencies; this.graph = graph; this.purge = purge;
        this.fiction = fiction; this.film = film; this.vault = vault; this.reservations = reservations;
        this.edits = edits; this.entityManager = entityManager; this.runtime = runtime; this.clock = clock;
        this.externalFileAuthority = Objects.requireNonNull(externalFileAuthority, "externalFileAuthority must not be null");
        this.acquisitionService = Objects.requireNonNull(acquisitionService, "acquisitionService must not be null");
        this.byteStore = Objects.requireNonNull(byteStore, "byteStore must not be null");
        this.recovery = recovery;
        this.security = security;
        this.noChange=noChange;
        this.inspections=inspections;
        if (maxDepth < 1 || maxDepth > 8) throw new IllegalArgumentException("Invalid dependency depth budget");
        this.maxDepth = maxDepth; this.limits = limits;
        this.transaction = new TransactionTemplate(transactions);
        this.transaction.setTimeout(10);
    }

    @Transactional(propagation = Propagation.NEVER)
    public RagSourceView registerFileCandidate(String path) {
        throw new IllegalStateException("Live external-file capability is disabled");
    }

    /** First import only. Re-registration never substitutes for an explicit S/update intent. */
    @Transactional(propagation = Propagation.NEVER)
    public RagSourceView registerSnapshotCandidate(String path) {
        Path original = snapshotPath(path);
        String digest = RagSourceAuthority.digest(original.toString());
        transaction.executeWithoutResult(status -> {
            graph.lockAndRead();
            requireNewSnapshot(digest);
        });
        UUID fileId=UUID.randomUUID(), revision=UUID.randomUUID();
        RagSnapshotAcquisitionService.AcquiredTempSnapshot acquired=null;
        byteStore.beginRevision(revision);
        try (var context=acquisitionService.newContext()) {
            acquired=acquisitionService.acquireBounded(original, context);
            context.check();
            byteStore.sealRevision(fileId,revision,acquired.captureId(),acquired.sha256(),acquired.byteLength(),original.getFileName().toString(),RagSourceAuthority.capturedTargetDigests(original));
            context.check();
            var candidate=acquired;
            return transaction.execute(status -> {
                graph.lockAndRead();
                requireNewSnapshot(candidate.canonicalDigest());
                context.check();
                if (sources.count()>=limits.catalogSources()) throw new IllegalStateException("Source catalog budget exceeded");
                var ext=new RagExternalFileSource(fileId,candidate.canonicalDigest(),1,1,1,1,true,clock.instant());
                ext.setPendingRevision(revision,candidate.sha256(),candidate.byteLength(),clock.instant(),null,1L,clock.instant());
                ext.bindPendingConsent(1,1);
                externalFileAuthority.save(ext);
                var source=new RagSource(RagSourceType.EXTERNAL_FILE,fileId.toString(),RagClassification.NON_PRIVATE,
                        1,1,Set.of(),RagAccessState.DENIED,candidate.sha256(),"UNCONFIRMED",clock.instant());
                // Two fixed-width SHA-256 values bind captured path intent AND basename
                // without retaining the private original path or adding storage columns.
                source.setFileIdentity(candidate.canonicalDigest()+RagSourceAuthority.capturedNameDigest(original.getFileName().toString(),RagSourceAuthority.capturedTargetDigests(original)));
                source=sources.saveAndFlush(source);
                checkBeforeCommit(context);
                return toView(source,ext,"");
            });
        } finally { finishCapture(acquired,fileId,revision); }
    }

    private void requireNewSnapshot(String digest) {
        security.requireIsolatedRuntime();
        var existing=externalFileAuthority.findByCanonicalDigest(digest);
        if (existing!=null) {
            externalFileAuthority.requireEditable(Set.of(existing.getFileSourceId()));
            throw new RagSourceChangedException();
        }
    }

    /** Unversioned update calls cannot silently refresh owner intent. */
    @Transactional(propagation = Propagation.NEVER)
    public RagSourceView capturePendingRevision(Long sourceId, String path) {
        throw new IllegalArgumentException("Exact snapshot expectation required");
    }

    @Transactional(propagation = Propagation.NEVER)
    public RagSourceView capturePendingRevision(Long sourceId, RagSnapshotCaptureCommand command) {
        Objects.requireNonNull(command);
        Path original=snapshotPath(command.filePath());
        var plan=transaction.execute(status -> snapshotPlan(sourceId,command.expectedSnapshot()));
        if (!RagSourceAuthority.digest(original.toString()).equals(plan.canonicalDigest()))
            throw new IllegalArgumentException("File intent does not match registered source");
        UUID revision=UUID.randomUUID();
        RagSnapshotAcquisitionService.AcquiredTempSnapshot acquired=null;
        try (var context=acquisitionService.newContext()) {
            captures.computeIfAbsent(sourceId,key -> java.util.concurrent.ConcurrentHashMap.newKeySet()).add(context);
            try {
                verifySnapshotPlan(sourceId,plan);
                UUID namingRevision=plan.expected().pendingRevisionId()!=null ? plan.expected().pendingRevisionId() : plan.expected().selectedRevisionId();
                var targetDigests=byteStore.targetDigests(plan.fileId(),namingRevision);
                acquired=acquisitionService.acquireBounded(original,context);
                context.check();
                boolean equal=Objects.equals(plan.comparisonHash(),acquired.sha256())
                        && Objects.equals(plan.comparisonLength(),acquired.byteLength());
                if (equal && plan.expected().selectedRevisionId()!=null
                        && sources.findById(sourceId).orElseThrow().getAccessState()==RagAccessState.APPROVED) {
                    var proof=noChange.prepare(sourceId);
                    var fresh=sources.findById(sourceId).orElseThrow();
                    var ext=externalFileAuthority.findByFileSourceId(plan.fileId());
                    if(!command.expectedSnapshot().equals(snapshotExpectation(fresh,ext)))throw new RagSourceChangedException();
                    context.check();
                    UUID operation=noChange.begin(Map.of("capture",proof),acquired.captureId());
                    noChange.finish(operation,Map.of("capture",proof),() -> checkBeforeCommit(context));
                    return toView(sources.findById(sourceId).orElseThrow(),externalFileAuthority.findByFileSourceId(plan.fileId()),"");
                }
                if (!equal) {
                    byteStore.beginRevision(revision);
                    context.check();
                    byteStore.sealRevision(plan.fileId(),revision,acquired.captureId(),acquired.sha256(),acquired.byteLength(),original.getFileName().toString(),targetDigests);
                }
                context.check();
                var candidate=acquired;
                return transaction.execute(status -> {
                    var current=snapshotPlan(sourceId,command.expectedSnapshot());
                    context.check();
                    var source=sources.findById(sourceId).orElseThrow();
                    var ext=externalFileAuthority.findByFileSourceId(current.fileId());
                    if (!equal) {
                        ext.setPendingRevision(revision,candidate.sha256(),candidate.byteLength(),clock.instant(),
                                command.expectedSnapshot().selectedRevisionId(),command.expectedSnapshot().sourceVersion(),clock.instant());
                        ext.bindPendingConsent(source.getAuthorizationEpoch(),source.getFieldPolicyVersion());
                        externalFileAuthority.save(ext);
                        entityManager.flush();
                    } else {
                        // Unselected pending-byte reuse is custody, not a completed NO_CHANGE.
                        // Approved selected equality must have taken the canonical guarded C_N path.
                        if(source.getAccessState()==RagAccessState.APPROVED)
                            throw new IllegalStateException("Reindex required: no selected revision");
                        if(ext.isSnapshotIntegrityFailed())throw new RagSourceChangedException();
                    }
                    checkBeforeCommit(context);
                    return toView(source,ext,"");
                });
            } finally {
                // Keep empty per-source sets: removing them races with a second entering capture.
                captures.get(sourceId).remove(context);
            }
        } finally {
            finishCapture(acquired,plan.fileId(),revision);
            recovery.reconcileRevision(plan.fileId(),plan.expected().pendingRevisionId());
        }
    }

    private static UUID snapshotFileId(String value) {
        try { return UUID.fromString(value); }
        catch (IllegalArgumentException legacyLocator) {
            // Restored live locators are only tombstoned, never reopened or disclosed.
            return null;
        }
    }

    private static Path snapshotPath(String value) {
        if (value==null || value.isBlank()) throw new IllegalArgumentException("File intent required");
        try {
            Path path=Path.of(value);
            if (!path.isAbsolute()) throw new IllegalArgumentException("Absolute server-local file required");
            return path.normalize();
        } catch (java.nio.file.InvalidPathException invalid) { throw new IllegalArgumentException("Invalid local file intent"); }
    }

    private void finishCapture(RagSnapshotAcquisitionService.AcquiredTempSnapshot acquired, UUID fileId, UUID revision) {
        try {
            if(acquired!=null) {
                try {if(noChange.protectsCapture(acquired.captureId()))return;}
                catch(RuntimeException unknownDatabase) {return;}
            }
            if (acquired!=null) byteStore.cleanupTemp(acquired.captureId());
        } finally {
            if (acquired!=null) byteStore.endCapture(acquired.captureId());
            byteStore.endRevision(revision);
            // A separate fresh guarded DB read resolves known rollback / uncertain commit.
            recovery.reconcileRevision(fileId,revision);
            recovery.reconcileSnapshots();
        }
    }

    private void checkBeforeCommit(RagSnapshotAcquisitionService.CaptureContext context) {
        context.check();
        org.springframework.transaction.support.TransactionSynchronizationManager.registerSynchronization(
                new org.springframework.transaction.support.TransactionSynchronization() {
                    @Override public void beforeCommit(boolean readOnly) { context.check(); }
                });
    }

    private record SnapshotPlan(UUID fileId, String canonicalDigest, RagSnapshotExpectation expected,
            String selectedHash, Long selectedLength) {
        String comparisonHash() { return expected.selectedRevisionId()!=null ? selectedHash : expected.pendingSha256(); }
        Long comparisonLength() { return expected.selectedRevisionId()!=null ? selectedLength : expected.pendingByteLength(); }
    }

    /** Every existing-S metadata mutation uses the same graph / owner / policy lock and CAS. */
    private SnapshotPlan snapshotPlan(Long id, RagSnapshotExpectation expected) {
        if (expected==null) throw new RagSourceChangedException();
        var topology=graph.lockAndRead();
        security.requireIsolatedRuntime();
        var affected=affected(topology,id);
        guardOwners(affected,new TreeSet<>());
        var source=lockSources(affected).get(id);
        if (source.getSourceType()!=RagSourceType.EXTERNAL_FILE || source.getSourceId().startsWith("disabled:"))
            throw new RagSourceChangedException();
        UUID fileId=UUID.fromString(source.getSourceId());
        var ext=externalFileAuthority.findByFileSourceId(fileId);
        if (ext==null) throw new RagSourceChangedException();
        entityManager.refresh(ext);
        if (source.getAccessState()==RagAccessState.APPROVED && !Objects.equals(source.getApprovedOwnerResetVersion(),ext.getConsentResetVersion()))throw new RagSourceChangedException();
        if (!ext.isEligible() || ext.isSnapshotIntegrityFailed() || !expected.equals(snapshotExpectation(source,ext)))
            throw new RagSourceChangedException();
        return new SnapshotPlan(fileId,ext.getCanonicalIdentityDigest(),expected,ext.getSelectedRevisionHash(),ext.getSelectedByteLength());
    }

    private RagSnapshotExpectation snapshotExpectation(RagSource source, RagExternalFileSource ext) {
        return new RagSnapshotExpectation(ext.getSourceVersion(),ext.getConsentResetVersion(),source.getAuthorizationEpoch(),
                source.getFieldPolicyVersion(),ext.getAuthorizationEpoch(),ext.getFieldPolicyVersion(),ext.getSelectedRevisionId(),
                ext.getPendingRevisionId(),ext.getPendingRevisionHash(),ext.getPendingByteLength());
    }

    private void verifySnapshotPlan(Long id, SnapshotPlan plan) {
        var expected=plan.expected();
        if(expected.pendingRevisionId()!=null) verifySnapshotRevision(id,plan,expected.pendingRevisionId(),expected.pendingSha256(),expected.pendingByteLength());
        if(expected.selectedRevisionId()!=null && !expected.selectedRevisionId().equals(expected.pendingRevisionId()))
            verifySnapshotRevision(id,plan,expected.selectedRevisionId(),plan.selectedHash(),plan.selectedLength());
    }

    private void verifySnapshotRevision(Long id, SnapshotPlan plan, UUID revision, String hash, Long length) {
        if(hash==null || length==null) throw new IllegalStateException("Snapshot descriptor unavailable");
        try {inspections.verify(plan.fileId(),revision,hash,length,"SOURCE_INTEGRITY_CHECK",
                plan.canonicalDigest()!=null?plan.canonicalDigest():"source:integrity");}
        catch(com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.storage.SnapshotIntegrityException
                | com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.storage.SnapshotMissingException incident) {
            throw new IllegalStateException("Registered snapshot integrity unavailable; explicit recovery required",incident);
        }
    }

    @Transactional(propagation = Propagation.NEVER)
    public RagSourceView registerDomainCandidate(RagSourceType type, String sourceId) {
        return registerDomainCandidate(type, sourceId, RagClassification.UNKNOWN, null, null);
    }

    /** Explicit classification config uses the existing registration action; it never grants participation. */
    @Transactional(propagation = Propagation.NEVER)
    public RagSourceView registerDomainCandidate(RagSourceType type, String sourceId, RagClassification classification,
            Long expectedEpoch, Integer expectedPolicy) {
        if (type == null || type == RagSourceType.EXTERNAL_FILE || classification == null)
            throw new IllegalArgumentException("Unsupported domain source");
        long ownerId = Long.parseLong(sourceId);
        if (ownerId <= 0) throw new IllegalArgumentException("Invalid source identifier");
        var ids = new TreeSet<>(Set.of(ownerId));
        var captured = reservations.read(ids).require(ownerId);
        if (!type.name().equals(captured.sourceType())) throw new IllegalArgumentException("Source type mismatch");
        // No content hashing under source/graph SQL guards. Version CAS detects intervening mutations.
        var revision = classification == RagClassification.NON_PRIVATE ? revision(type, ownerId) : new Revision("UNCONFIRMED", "UNCONFIRMED");
        return transaction.execute(status -> {
            var topology = graph.lockAndRead();
            var existing = sources.findBySourceTypeAndSourceId(type, Long.toString(ownerId));
            var affected = existing.map(s -> affected(topology, s.getId())).orElseGet(TreeSet::new);
            guardOwners(affected, ids);
            var current = reservations.read(ids).require(ownerId);
            if (current.sourceVersion() != captured.sourceVersion()) throw new RagSourceChangedException();
            RagSource source;
            if (existing.isPresent()) {
                source = lockSources(affected).get(existing.get().getId());
                if (source.getClassification() != classification) {
                    if (!Objects.equals(expectedEpoch, source.getAuthorizationEpoch())
                            || !Objects.equals(expectedPolicy, source.getFieldPolicyVersion())) throw new RagSourceChangedException();
                    source.configureClassification(classification, clock.instant());
                    sources.saveAndFlush(source);
                    for (Long id : affected) purge.purgeSourceDerivatives(id);
                }
            } else {
                if (sources.count() >= limits.catalogSources()) throw new IllegalStateException("Source catalog budget exceeded");
                source = sources.saveAndFlush(new RagSource(type, Long.toString(ownerId), classification, 1, 1, Set.of(),
                        RagAccessState.DENIED, revision.content(), "UNCONFIRMED", clock.instant()));
            }
            return toView(source, current, revision.description());
        });
    }

    /** Legacy fields-only commands cannot acquire fresh consent. */
    public RagSourceView approveSource(Long id, Set<String> fields) {
        throw new IllegalArgumentException("Explicit source and consent versions are required");
    }

    @Transactional(propagation = Propagation.NEVER)
    public RagSourceView approveSource(Long sourceId, RagConsentCommand command) {
        Objects.requireNonNull(command);
        var initial = sources.findById(sourceId).orElseThrow();
        if (initial.getSourceType() == RagSourceType.EXTERNAL_FILE) {
            RagFieldAllowlist.validateApprovedFields(initial.getSourceType(), command.approvedFields());
            if (initial.getClassification() != RagClassification.NON_PRIVATE) {
                throw new IllegalStateException("Private or unclassified source cannot be approved");
            }
            // Preserve the frozen restored live-path containment branch.
            if (snapshotFileId(initial.getSourceId())==null)
                throw new IllegalStateException("Live external-file capability is disabled");
            var plan=transaction.execute(status -> snapshotPlan(sourceId,command.expectedSnapshot()));
            if (command.expectedSourceVersion()!=plan.expected().sourceVersion()
                    || command.expectedConsentResetVersion()!=plan.expected().consentResetVersion()
                    || command.expectedAuthorizationEpoch()!=plan.expected().authorizationEpoch()
                    || command.expectedFieldPolicyVersion()!=plan.expected().fieldPolicyVersion()
                    || plan.expected().pendingRevisionId()==null) throw new RagSourceChangedException();
            verifySnapshotPlan(sourceId,plan);
            return transaction.execute(status -> {
                snapshotPlan(sourceId,command.expectedSnapshot());
                var source=sources.findById(sourceId).orElseThrow();
                var ext=externalFileAuthority.findByFileSourceId(plan.fileId());
                if (!ext.pendingConsentMatches(source.getAuthorizationEpoch(),source.getFieldPolicyVersion()))
                    throw new RagSourceChangedException();
                for (Long id:affected(graph.read(),sourceId)) purge.purgeSourceDerivatives(id);
                source.approve(command.approvedFields(),plan.expected().pendingSha256(),source.getDescriptionRevisionHash(),clock.instant());
                source.bindOwnerResetVersion(ext.getConsentResetVersion());
                source.bindApprovalInstance(runtime.instance());
                ext.approvePendingRevision(plan.expected().pendingRevisionId(),source.getAuthorizationEpoch(),source.getFieldPolicyVersion(),clock.instant());
                externalFileAuthority.save(ext);
                sources.saveAndFlush(source);
                return toView(source,ext,"");
            });
        }

        RagFieldAllowlist.validateApprovedFields(initial.getSourceType(), command.approvedFields());
        if (initial.getClassification() != RagClassification.NON_PRIVATE) throw new IllegalStateException("Private or unclassified source cannot be approved");
        long ownerId = Long.parseLong(initial.getSourceId());
        var projection = revision(initial.getSourceType(), ownerId);
        if (command.approvedFields().contains("description")
                && !Objects.equals(command.confirmedDescriptionHash(), projection.description())) throw new RagSourceChangedException();
        return transaction.execute(status -> {
            var topology = graph.lockAndRead();
            var affected = affected(topology, sourceId);
            var ids = new TreeSet<>(Set.of(ownerId));
            guardOwners(affected, ids);
            var owner = reservations.read(ids).require(ownerId);
            var source = lockSources(affected).get(sourceId);
            if (owner.deleted() || owner.sourceVersion() != command.expectedSourceVersion()
                    || owner.consentResetVersion() != command.expectedConsentResetVersion()
                    || source.getAuthorizationEpoch() != command.expectedAuthorizationEpoch()
                    || source.getFieldPolicyVersion() != command.expectedFieldPolicyVersion()) throw new RagSourceChangedException();
            if (source.getClassification() != RagClassification.NON_PRIVATE) throw new IllegalStateException("Private or unclassified source cannot be approved");
            for (Long id : affected) purge.purgeSourceDerivatives(id);
            String description = command.approvedFields().contains("description") ? projection.description() : source.getDescriptionRevisionHash();
            source.approve(command.approvedFields(), projection.content(), description, clock.instant());
            source.bindOwnerResetVersion(owner.consentResetVersion());
            // Execution SQL remains boot-scoped; standing consent no longer depends on this legacy execution field.
            source.bindApprovalInstance(runtime.instance());
            sources.saveAndFlush(source);
            return toView(source, owner, projection.description());
        });
    }

    @Transactional
    public RagSourceView revokeSource(Long sourceId) {
        var topology = graph.lockAndRead();
        var affected = affected(topology, sourceId);
        guardOwners(affected, new TreeSet<>());
        var source = lockSources(affected).get(sourceId);
        if (source.getSourceType() == RagSourceType.EXTERNAL_FILE) {
            security.requireIsolatedRuntime();
            RagExternalFileSource ext = null;
            UUID retiredFileId=snapshotFileId(source.getSourceId());
            var retired=new HashSet<UUID>();
            if (retiredFileId!=null) {
                ext=externalFileAuthority.findByFileSourceId(retiredFileId);
                if (ext!=null) {
                    entityManager.refresh(ext);
                    if (ext.getPendingRevisionId()!=null) retired.add(ext.getPendingRevisionId());
                    if (ext.getSelectedRevisionId()!=null) retired.add(ext.getSelectedRevisionId());
                    ext.setEligible(false,clock.instant());
                    // Reference removal commits BEFORE any physical erasure. Files retain
                    // opaque source/R identity for durable retry even after the policy tombstone.
                    ext.clearPendingRevision(clock.instant());
                    externalFileAuthority.save(ext);
                }
            }
            org.springframework.transaction.support.TransactionSynchronizationManager.registerSynchronization(
                    new org.springframework.transaction.support.TransactionSynchronization() {
                        @Override public void afterCommit() {
                            captures.getOrDefault(sourceId,Set.of()).forEach(RagSnapshotAcquisitionService.CaptureContext::cancel);
                            retired.forEach(revision -> recovery.reconcileRevision(retiredFileId,revision));
                            recovery.reconcileSnapshots();
                        }
                    });
            source.revoke(clock.instant());
            sources.saveAndFlush(source);
            for (Long id : affected) purge.purgeSourceDerivatives(id);
            return toView(source, ext, "");
        }
        source.revoke(clock.instant());
        sources.saveAndFlush(source);
        for (Long id : affected) purge.purgeSourceDerivatives(id);
        return toView(source, reservations.read(new TreeSet<>(Set.of(Long.valueOf(source.getSourceId())))).require(Long.parseLong(source.getSourceId())), "");
    }

    @Transactional
    public void addDependency(Long parent, Long child) {
        Objects.requireNonNull(parent); Objects.requireNonNull(child);
        var topology = graph.lockAndRead();
        if (parent.equals(child) || topology.descendants(child).contains(parent)) throw new IllegalStateException("Cyclic dependency detected");
        if (dependencies.findByParentSourceIdAndChildSourceId(parent, child).isPresent()) return;
        if (topology.ancestorDepth(parent) + topology.descendantDepth(child) > maxDepth) throw new IllegalArgumentException("Dependency depth exceeds maximum allowed limit");
        var affected = affected(topology, parent);
        guardOwners(affected, new TreeSet<>());
        var all = new TreeSet<>(affected); all.add(child);
        var locked = lockSources(all);
        for (Long root : affected) {
            var descendants = new HashSet<>(topology.descendants(root));
            descendants.add(child); descendants.addAll(topology.descendants(child));
            if (descendants.size() >= limits.selectedSources()) throw new IllegalStateException("Dependency count budget exceeded");
        }
        dependencies.saveAndFlush(new RagDependency(parent, child, clock.instant()));
        for (Long root : affected) {
            // SQL trigger is the canonical affected-ancestor version/epoch backstop.
            entityManager.refresh(locked.get(root));
            purge.purgeSourceDerivatives(root);
        }
    }

    private void guardOwners(Collection<Long> ragIds, SortedSet<Long> additional) {
        var ids = new TreeSet<>(additional);
        var extIds = new HashSet<UUID>();
        for (var source : sources.findAllById(ragIds)) {
            if (source.getSourceType() == RagSourceType.EXTERNAL_FILE) {
                if (!source.getSourceId().startsWith("disabled:")) {
                    try {
                        extIds.add(UUID.fromString(source.getSourceId()));
                    } catch (Exception ignored) {}
                }
            } else {
                ids.add(Long.valueOf(source.getSourceId()));
            }
        }
        if (!ids.isEmpty()) vault.acquireSourceGuards(ids);
        if (!extIds.isEmpty()) externalFileAuthority.requireEditable(extIds);
    }

    private static SortedSet<Long> affected(RagDependencyGraph.Snapshot topology, Long source) {
        var ids = new TreeSet<>(topology.ancestors(source)); ids.add(source); return ids;
    }

    private Map<Long, RagSource> lockSources(Collection<Long> ids) {
        var locked = sources.findAllByIdInForUpdate(ids);
        if (locked.size() != ids.size()) throw new NoSuchElementException("Source authority unavailable");
        var map = new HashMap<Long, RagSource>();
        for (var source : locked) { entityManager.refresh(source); map.put(source.getId(), source); }
        return map;
    }

    private record Revision(String content, String description) {}

    private Revision revision(RagSourceType type, long id) {
        if (type == RagSourceType.FICTION) {
            var p = fiction.findSource(id).orElseThrow(() -> new IllegalStateException("Source is ineligible (NSFW or deleted)"));
            if (!p.isEligible()) throw new IllegalStateException("Source is ineligible (NSFW or deleted)");
            return new Revision(p.contentRevisionHash(), p.descriptionRevisionHash());
        }
        var p = film.findSource(id).orElseThrow(() -> new IllegalStateException("Source is ineligible (NSFW or deleted)"));
        if (!p.isEligible()) throw new IllegalStateException("Source is ineligible (NSFW or deleted)");
        return new Revision(p.contentRevisionHash(), p.descriptionRevisionHash());
    }

    public static boolean hasRestrictiveMetadata(String content) {
        if (content == null || content.isBlank()) return true;
        for (int i = 0; i < content.length(); i++) {
            char ch = content.charAt(i);
            if (!(Character.isLetterOrDigit(ch) || Character.isWhitespace(ch) || ".,!?'-".indexOf(ch) >= 0))
                return true;
            if (Character.isISOControl(ch) && ch != '\n' && ch != '\r' && ch != '\t') return true;
        }
        return content.lines().anyMatch(line -> line.stripLeading().startsWith("---"));
    }


    public Optional<RagSourceView> findSource(Long id) {
        return id == null ? Optional.empty() : sources.findById(id).map(this::preview);
    }

    public List<RagSourceView> listSources(int offset, int limit) {
        return entityManager.createQuery("SELECT s FROM RagSource s ORDER BY s.id", RagSource.class)
                .setFirstResult(Math.max(0, offset)).setMaxResults(Math.clamp(limit, 1, 100)).getResultList().stream()
                .map(source -> viewWithoutContent(source)).toList();
    }

    private RagSourceView preview(RagSource source) {
        if (source.getSourceType() == RagSourceType.EXTERNAL_FILE) return externalView(source);
        long id = Long.parseLong(source.getSourceId());
        var owner = reservations.read(new TreeSet<>(Set.of(id))).require(id);
        String description = "";
        if (!owner.deleted() && source.getClassification() == RagClassification.NON_PRIVATE) {
            var optional = source.getSourceType() == RagSourceType.FICTION
                    ? fiction.findSource(id).filter(p -> p.isEligible()).map(p -> p.descriptionRevisionHash())
                    : film.findSource(id).filter(p -> p.isEligible()).map(p -> p.descriptionRevisionHash());
            description = optional.orElse("");
        }
        return toView(source, owner, description);
    }

    private RagSourceView viewWithoutContent(RagSource source) {
        if (source.getSourceType() == RagSourceType.EXTERNAL_FILE) return externalView(source);
        long id = Long.parseLong(source.getSourceId());
        return toView(source, reservations.read(new TreeSet<>(Set.of(id))).require(id), "");
    }

    private RagSourceView externalView(RagSource source) {
        if (source.getSourceId().startsWith("disabled:")) {
            return new RagSourceView(source.getId(), source.getSourceType(), source.getSourceId(), source.getClassification(),
                    source.getAuthorizationEpoch(), source.getFieldPolicyVersion(), Set.of(), RagAccessState.REVOKED,
                    "DISABLED", "DISABLED", source.getCreatedAt(), source.getUpdatedAt(), source.getRevokedAt(), 0, 0, "");
        }
        try {
            UUID fileSourceId = UUID.fromString(source.getSourceId());
            var ext = externalFileAuthority.findByFileSourceId(fileSourceId);
            if (ext != null) {
                return toView(source, ext, "");
            }
        } catch (Exception ignored) {}
        return new RagSourceView(source.getId(), source.getSourceType(), source.getSourceId(), source.getClassification(),
                source.getAuthorizationEpoch(), source.getFieldPolicyVersion(), Set.of(), RagAccessState.REVOKED,
                "DISABLED", "DISABLED", source.getCreatedAt(), source.getUpdatedAt(), source.getRevokedAt(), 0, 0, "");
    }

    private RagSourceView toView(RagSource source, RagExternalFileSource ext, String currentDescription) {
        if (ext == null) {
            return externalView(source);
        }
        var access = !ext.isEligible() || ext.isSnapshotIntegrityFailed() || (source.getAccessState() == RagAccessState.APPROVED
                && !Objects.equals(source.getApprovedOwnerResetVersion(), ext.getConsentResetVersion()))
                ? RagAccessState.REVOKED : source.getAccessState();
        if (access==RagAccessState.APPROVED && ext.getSelectedRevisionId()==null
                && !Objects.equals(ext.getPendingRevisionId(),ext.getApprovedPendingRevisionId())) access=RagAccessState.DENIED;
        return new RagSourceView(source.getId(), source.getSourceType(), source.getSourceId(), source.getClassification(),
                source.getAuthorizationEpoch(), source.getFieldPolicyVersion(), source.getApprovedFields(), access,
                source.getSourceRevisionHash(), source.getDescriptionRevisionHash(), source.getCreatedAt(),
                source.getUpdatedAt(), source.getRevokedAt(), ext.getSourceVersion(), ext.getConsentResetVersion(),
                currentDescription != null ? currentDescription : "", snapshotExpectation(source,ext));
    }

    private RagSourceView toView(RagSource source, SourceVersion owner, String currentDescription) {
        var access = owner.deleted() || (source.getAccessState() == RagAccessState.APPROVED
                && !Objects.equals(source.getApprovedOwnerResetVersion(), owner.consentResetVersion()))
                ? RagAccessState.REVOKED : source.getAccessState();
        return new RagSourceView(source.getId(), source.getSourceType(), source.getSourceId(), source.getClassification(),
                source.getAuthorizationEpoch(), source.getFieldPolicyVersion(), source.getApprovedFields(), access,
                source.getSourceRevisionHash(), source.getDescriptionRevisionHash(), source.getCreatedAt(),
                source.getUpdatedAt(), source.getRevokedAt(), owner.sourceVersion(), owner.consentResetVersion(), currentDescription);
    }
}

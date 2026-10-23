package com.vhvkhangg.personalprivatevault.rag.internal.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vhvkhangg.personalprivatevault.fiction.source.*;
import com.vhvkhangg.personalprivatevault.film.source.*;
import com.vhvkhangg.personalprivatevault.rag.api.RagSourceChangedException;
import com.vhvkhangg.personalprivatevault.rag.internal.domain.*;
import com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.persistence.*;
import com.vhvkhangg.personalprivatevault.rag.source.*;
import com.vhvkhangg.personalprivatevault.vault.entry.*;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

/** Optimistic bounded hashing, then a short guarded version/consent/closure proof. */
@Component
public class RagSourceAuthority {
    private final RagDependencyGraph graph;
    private final RagSourceRepository sources;
    private final RagJobLeaseRepository leases;
    private final VaultEntryOperations vault;
    private final SourceIndexReservations reservations;
    private final FictionSourceOperations fiction;
    private final FilmSourceOperations film;
    private final EntityManager entityManager;
    private final RagRuntimeIdentity runtimeIdentity;
    private final RagLimits limits;
    private final RagRuntimeSecurity runtimeSecurity;
    private final RagBatchRootRepository batchRoots;
    private final RagExternalFileAuthority externalFileAuthority;
    private final com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.storage.RagOriginalBytesStore byteStore;
    private final ObjectMapper json = new ObjectMapper().findAndRegisterModules()
            .enable(com.fasterxml.jackson.databind.SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);

    public RagSourceAuthority(RagDependencyGraph graph, RagSourceRepository sources, RagJobLeaseRepository leases,
            VaultEntryOperations vault, SourceIndexReservations reservations, FictionSourceOperations fiction,
            FilmSourceOperations film, EntityManager entityManager, RagRuntimeIdentity runtimeIdentity,
            RagLimits limits, RagRuntimeSecurity runtimeSecurity,
            RagBatchRootRepository batchRoots, RagExternalFileAuthority externalFileAuthority,
            com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.storage.RagOriginalBytesStore byteStore) {
        this.graph = graph; this.sources = sources; this.leases = leases; this.vault = vault;
        this.reservations = reservations; this.fiction = fiction; this.film = film; this.entityManager = entityManager;
        this.runtimeIdentity = runtimeIdentity; this.limits = limits; this.runtimeSecurity = runtimeSecurity;
        this.batchRoots = batchRoots; this.externalFileAuthority = externalFileAuthority;
        this.byteStore = byteStore;
    }

    public record Snapshot(RagSource root, String manifest, String payload) {
        @Override public String toString() { return "Snapshot[sourceId=" + root.getId() + "]"; }
    }
    public record Attempt(RagJobLease lease, Snapshot snapshot) {}
    public record SourceProof(long ownerId, String sourceIdentity, String type, long version, long reset, long epoch,
            int policy, Set<String> fields, String contentHash, String descriptionHash, Map<String, Object> snapshot, String fileIdentity) {
        public SourceProof { fields = Set.copyOf(fields); snapshot = Collections.unmodifiableMap(new TreeMap<>(snapshot)); }
        public SourceProof(long ownerId, String type, long version, long reset, long epoch,
                int policy, Set<String> fields, String contentHash, String descriptionHash) {
            this(ownerId, Long.toString(ownerId), type, version, reset, epoch, policy, fields, contentHash, descriptionHash, Map.of(), null);
        }
    }
    public record Prepared(Long rootId, Map<Long, SourceProof> proofs, String manifest, String payload) {
        public Prepared { proofs = Map.copyOf(proofs); }
        @Override public String toString() { return "Prepared[sourceId=" + rootId + "]"; }
    }

    @Transactional(readOnly = true)
    public Prepared prepareSource(Long rootId, Map<Long, String> descriptionConfirmations) {
        runtimeSecurity.requireIsolatedRuntime();
        var topology = graph.read();
        var ids = new TreeSet<>(topology.descendants(rootId)); ids.add(rootId);
        if (ids.size() > limits.selectedSources()) throw new IllegalStateException("Source selection budget exceeded");
        var initial = sources.findAllById(ids);
        if (initial.size() != ids.size()) throw new IllegalStateException("Source authority is unavailable");
        var ownerIds = new TreeSet<Long>();
        var externalIds = new TreeSet<UUID>();
        for (var source : initial) {
            if (source.getSourceType() == RagSourceType.EXTERNAL_FILE) {
                externalIds.add(UUID.fromString(source.getSourceId()));
            } else {
                ownerIds.add(Long.valueOf(source.getSourceId()));
            }
        }
        // Capture history BEFORE reading/hashing content. The final guard compares this exact version.
        var versions = ownerIds.isEmpty() ? null : reservations.read(ownerIds);
        Map<UUID, RagExternalFileSource> externalSources = new HashMap<>();
        for (UUID extId : externalIds) {
            var ext = externalFileAuthority.findByFileSourceId(extId);
            if (ext == null) throw new IllegalStateException("External file authority is unavailable");
            externalSources.put(extId, ext);
        }
        for (var source : initial) {
            if (source.getSourceType() == RagSourceType.EXTERNAL_FILE) {
                UUID extId = UUID.fromString(source.getSourceId());
                var ext = externalSources.get(extId);
                if (ext == null || !ext.isEligible() || ext.isSnapshotIntegrityFailed()
                        || source.getClassification() != RagClassification.NON_PRIVATE || source.getAccessState() != RagAccessState.APPROVED
                        || !Objects.equals(source.getApprovedOwnerResetVersion(), ext.getConsentResetVersion())) {
                    throw new IllegalStateException("Source consent or eligibility is unavailable: External file indexing capability is inactive in Slice 1 (disabled)");
                }
                RagFieldAllowlist.validateApprovedFields(source.getSourceType(), source.getApprovedFields());
            } else {
                var owner = versions.require(Long.parseLong(source.getSourceId()));
                if (owner.deleted() || !source.getSourceType().name().equals(owner.sourceType())
                        || source.getClassification() != RagClassification.NON_PRIVATE || source.getAccessState() != RagAccessState.APPROVED
                        || !Objects.equals(source.getApprovedOwnerResetVersion(), owner.consentResetVersion()))
                    throw new IllegalStateException("Source consent or eligibility is unavailable");
                RagFieldAllowlist.validateApprovedFields(source.getSourceType(), source.getApprovedFields());
            }
        }
        Map<Long, FictionSourceProjection> fictions = new HashMap<>();
        fiction.findSources(initial.stream().filter(s -> s.getSourceType() == RagSourceType.FICTION)
                .map(s -> Long.valueOf(s.getSourceId())).toList()).forEach(p -> fictions.put(p.id(), p));
        Map<Long, FilmSourceProjection> films = new HashMap<>();
        film.findSources(initial.stream().filter(s -> s.getSourceType() == RagSourceType.FILM)
                .map(s -> Long.valueOf(s.getSourceId())).toList()).forEach(p -> films.put(p.id(), p));
        var proofs = new TreeMap<Long, SourceProof>();
        var nodes = new ArrayList<Map<String, Object>>();
        var payload = new ArrayList<Map<String, Object>>();
        var usedConfirmations = new HashSet<Long>();
        for (var source : initial.stream().sorted(Comparator.comparing(RagSource::getId)).toList()) {
            if (source.getSourceType() == RagSourceType.EXTERNAL_FILE) {
                UUID extId = UUID.fromString(source.getSourceId());
                var ext = externalSources.get(extId);
                UUID revisionId;
                String revisionHash;
                long byteLength;
                if (ext.getApprovedPendingRevisionId() != null) {
                    revisionId = ext.getApprovedPendingRevisionId();
                    revisionHash = ext.getPendingRevisionHash();
                    byteLength = ext.getPendingByteLength();
                } else if (ext.getSelectedRevisionId() != null) {
                    revisionId = ext.getSelectedRevisionId();
                    revisionHash = ext.getSelectedRevisionHash();
                    byteLength = ext.getSelectedByteLength();
                } else {
                    throw new IllegalStateException("No approved revision available for indexing");
                }
                if (!Objects.equals(source.getSourceRevisionHash(), revisionHash)) {
                    throw new RagSourceChangedException();
                }
                byte[] bytes = byteStore.readRevisionBytes(extId, revisionId, revisionHash, byteLength);
                String sourceName = byteStore.sourceName(extId, revisionId);
                var targetDigests = byteStore.targetDigests(extId, revisionId);
                if (!Objects.equals(source.getFileIdentity(),ext.getCanonicalIdentityDigest()+capturedNameDigest(sourceName,targetDigests)))
                    throw new IllegalStateException("Captured source format identity is unavailable");
                if (!sourceName.toLowerCase(Locale.ROOT).endsWith(".md"))
                    throw new IllegalArgumentException("Only captured .md sources are admitted");
                String contentText;
                try {
                    contentText = StandardCharsets.UTF_8.newDecoder()
                            .onMalformedInput(java.nio.charset.CodingErrorAction.REPORT)
                            .onUnmappableCharacter(java.nio.charset.CodingErrorAction.REPORT)
                            .decode(java.nio.ByteBuffer.wrap(bytes)).toString();
                } catch (java.nio.charset.CharacterCodingException invalid) {
                    throw new IllegalArgumentException("Invalid Markdown UTF-8 encoding");
                }
                var snapshot = new TreeMap<String, Object>();
                snapshot.put("mode", "RAG_SNAPSHOT"); snapshot.put("consent_mode", "WHOLE_FILE");
                snapshot.put("source_type", "EXTERNAL_FILE"); snapshot.put("source_id", source.getSourceId());
                snapshot.put("revision_id", revisionId.toString()); snapshot.put("sha256", revisionHash);
                snapshot.put("byte_length", byteLength); snapshot.put("source_name", sourceName);
                if (!targetDigests.isEmpty()) snapshot.put("target_digests", targetDigests);
                snapshot.put("expected_selected_revision_id", ext.getSelectedRevisionId() == null ? null : ext.getSelectedRevisionId().toString());
                snapshot.put("source_version", ext.getSourceVersion()); snapshot.put("consent_reset_version", ext.getConsentResetVersion());
                snapshot.put("authorization_epoch", source.getAuthorizationEpoch()); snapshot.put("field_policy_version", source.getFieldPolicyVersion());
                var fields = source.getApprovedFields();
                var proof = new SourceProof(0L, source.getSourceId(), source.getSourceType().name(),
                        ext.getSourceVersion(), ext.getConsentResetVersion(),
                        source.getAuthorizationEpoch(), source.getFieldPolicyVersion(),
                        fields, revisionHash, "", snapshot, source.getFileIdentity());
                proofs.put(source.getId(), proof);
                var node = new TreeMap<String, Object>();
                node.put("source_id", source.getId()); node.put("source_type", proof.type()); node.put("domain_id", source.getSourceId());
                node.put("epoch", proof.epoch()); node.put("field_version", proof.policy()); node.put("fields", fields.stream().sorted().toList());
                node.put("content_revision", proof.contentHash()); node.put("description_revision", proof.descriptionHash());
                node.put("owner_source_version", proof.version()); node.put("owner_reset_version", proof.reset());
                node.put("snapshot", snapshot);
                nodes.add(node);
                var selected = new TreeMap<String, Object>();
                selected.put("content", contentText);
                payload.add(Map.of("source_id", source.getId(), "source_type", proof.type(), "id", source.getSourceId(), "text", contentText, "fields", selected));
            } else {
                long ownerId = Long.parseLong(source.getSourceId());
                var owner = versions.require(ownerId);
                Map<String, Object> values;
                String contentHash, descriptionHash;
                if (source.getSourceType() == RagSourceType.FICTION) {
                    var projection = fictions.get(ownerId);
                    if (projection == null || !projection.isEligible()) throw new IllegalStateException("Source is ineligible (NSFW or deleted)");
                    values = fictionValues(projection); contentHash = projection.contentRevisionHash(); descriptionHash = projection.descriptionRevisionHash();
                } else {
                    var projection = films.get(ownerId);
                    if (projection == null || !projection.isEligible()) throw new IllegalStateException("Source is ineligible (NSFW or deleted)");
                    values = filmValues(projection); contentHash = projection.contentRevisionHash(); descriptionHash = projection.descriptionRevisionHash();
                }
                var fields = source.getApprovedFields();
                if (fields.contains("description")) {
                    String confirmation = descriptionConfirmations.get(source.getId());
                    if (confirmation != null) {
                        if (!confirmation.equals(descriptionHash)) throw new RagSourceChangedException();
                        usedConfirmations.add(source.getId());
                    }
                    if (!Objects.equals(source.getDescriptionRevisionHash(), descriptionHash) && confirmation == null)
                        throw new RagSourceChangedException();
                }
                var selected = new TreeMap<String, Object>();
                for (String field : fields) selected.put(field, values.get(field));
                var proof = new SourceProof(ownerId, source.getSourceType().name(), owner.sourceVersion(), owner.consentResetVersion(),
                        source.getAuthorizationEpoch(), source.getFieldPolicyVersion(), fields, contentHash,
                        fields.contains("description") ? descriptionHash : "");
                proofs.put(source.getId(), proof);
                var node = new TreeMap<String, Object>();
                node.put("source_id", source.getId()); node.put("source_type", proof.type()); node.put("domain_id", source.getSourceId());
                node.put("epoch", proof.epoch()); node.put("field_version", proof.policy()); node.put("fields", fields.stream().sorted().toList());
                node.put("content_revision", proof.contentHash()); node.put("description_revision", proof.descriptionHash());
                node.put("owner_source_version", proof.version()); node.put("owner_reset_version", proof.reset());
                nodes.add(node);
                payload.add(Map.of("source_id", source.getId(), "source_type", proof.type(), "id", ownerId, "fields", selected));
            }
        }
        if (!usedConfirmations.equals(descriptionConfirmations.keySet())) throw new IllegalArgumentException("Description confirmation is outside the selected fields");
        var edges = new ArrayList<List<Long>>();
        for (Long id : ids) topology.forward().getOrDefault(id, Set.of()).stream().sorted().forEach(child -> edges.add(List.of(id, child)));
        String encoded = encode(Map.of("contract_version", "2.0", "sources", payload));
        if (encoded.getBytes(StandardCharsets.UTF_8).length > limits.inputBytes()) throw new IllegalStateException("Worker input byte budget exceeded");
        return new Prepared(rootId, proofs, encode(Map.of("contract_version", "2.0", "root_id", rootId, "nodes", nodes, "edges", edges)), encoded);
    }

    /** Caller must have prepared before acquiring any source/graph guard. */
    public Snapshot lockPrepared(Prepared prepared, UUID reservation, boolean admission) {
        return lockPrepared(prepared, reservation, admission, false);
    }

    /** Current-index authorization shares source authority without demanding editability. */
    public Snapshot lockPreparedCurrent(Prepared prepared) {
        return lockPrepared(prepared, null, false, true);
    }

    private Snapshot lockPrepared(Prepared prepared, UUID reservation, boolean admission, boolean currentRead) {
        return guardPrepared(List.of(prepared), reservation, admission, currentRead).get(prepared.rootId());
    }

    public Map<Long, Snapshot> lockPreparedBatch(Collection<Prepared> prepared, UUID reservation, boolean admission) {
        return guardPrepared(prepared, reservation, admission, false);
    }

    private Map<Long, Snapshot> guardPrepared(Collection<Prepared> prepared, UUID reservation,
            boolean admission, boolean currentRead) {
        runtimeSecurity.requireIsolatedRuntime();
        var topology = graph.lockAndRead();
        var proofs = new TreeMap<Long, SourceProof>();
        for (var root : prepared) {
            var closure = new TreeSet<>(topology.descendants(root.rootId())); closure.add(root.rootId());
            if (!closure.equals(root.proofs().keySet())) throw new RagSourceChangedException();
            var edges = new ArrayList<List<Long>>();
            for (Long id : closure) topology.forward().getOrDefault(id, Set.of()).stream().sorted()
                    .forEach(child -> edges.add(List.of(id, child)));
            try {
                if (!json.readTree(root.manifest()).path("edges").toString().equals(encode(edges)))
                    throw new RagSourceChangedException();
            } catch (com.fasterxml.jackson.core.JsonProcessingException failure) {
                throw new IllegalStateException("Invalid dependency proof", failure);
            }
            root.proofs().forEach((id, proof) -> {
                var previous = proofs.putIfAbsent(id, proof);
                if (previous != null && !previous.equals(proof)) throw new RagSourceChangedException();
            });
        }
        if (proofs.isEmpty() || proofs.size() > limits.selectedSources())
            throw new IllegalStateException("Source selection budget exceeded");
        var ownerIds = new TreeSet<Long>();
        var externalIds = new TreeSet<UUID>();
        for (var proof : proofs.values()) {
            if ("EXTERNAL_FILE".equals(proof.type())) {
                externalIds.add(UUID.fromString(proof.sourceIdentity()));
            } else {
                ownerIds.add(proof.ownerId());
            }
        }
        SourceVersions versions = null;
        if (!ownerIds.isEmpty()) {
            if (currentRead) versions = reservations.lockForRead(ownerIds);
            else if (reservation == null) { vault.acquireSourceGuards(ownerIds); versions = reservations.read(ownerIds); }
            else versions = reservations.lockReserved(ownerIds, reservation);
        }
        Map<UUID, RagExternalFileSource> externalSources = new HashMap<>();
        if (!externalIds.isEmpty()) {
            if (currentRead) {
                for (UUID extId : externalIds.stream().sorted(Comparator.comparing(UUID::toString)).toList()) {
                    var ext = externalFileAuthority.findByFileSourceId(extId);
                    if (ext == null) throw new IllegalStateException("External file source unavailable");
                    entityManager.refresh(ext,jakarta.persistence.LockModeType.PESSIMISTIC_READ);
                    externalSources.put(extId, ext);
                }
            } else if (reservation == null) {
                externalFileAuthority.requireEditable(externalIds);
                for (UUID extId : externalIds) {
                    var ext = externalFileAuthority.findByFileSourceId(extId);
                    if (ext == null) throw new IllegalStateException("External file source unavailable");
                    externalSources.put(extId, ext);
                }
            } else {
                var lockedExt = externalFileAuthority.lockReserved(externalIds, reservation);
                if (lockedExt.size() != externalIds.size()) throw new RagSourceChangedException();
                lockedExt.forEach(e -> externalSources.put(e.getFileSourceId(), e));
            }
        }
        var locked = sources.findAllByIdInForUpdate(new TreeSet<>(proofs.keySet()));
        if (locked.size() != proofs.size()) throw new IllegalStateException("Source authority is unavailable");
        Map<Long, RagSource> byId = new HashMap<>();
        for (var source : locked) {
            entityManager.refresh(source);
            var expected = proofs.get(source.getId());
            if ("EXTERNAL_FILE".equals(source.getSourceType().name())) {
                UUID extId = UUID.fromString(source.getSourceId());
                var ext = externalSources.get(extId);
                if (ext == null || !ext.isEligible() || ext.isSnapshotIntegrityFailed()
                        || ext.getSourceVersion() != expected.version()
                        || ext.getConsentResetVersion() != expected.reset()
                        || !source.getSourceId().equals(expected.sourceIdentity())
                        || source.getClassification() != RagClassification.NON_PRIVATE
                        || source.getAccessState() != RagAccessState.APPROVED
                        || !Objects.equals(source.getApprovedOwnerResetVersion(), expected.reset())
                        || source.getAuthorizationEpoch() != expected.epoch()
                        || source.getFieldPolicyVersion() != expected.policy()
                        || !source.getApprovedFields().equals(expected.fields())) {
                    throw new RagSourceChangedException();
                }
                if (!Objects.equals(expected.snapshot().get("expected_selected_revision_id"),
                            ext.getSelectedRevisionId() == null ? null : ext.getSelectedRevisionId().toString())
                        || !Objects.equals(expected.snapshot().get("revision_id"),
                            Objects.toString(ext.getApprovedPendingRevisionId() != null ? ext.getApprovedPendingRevisionId() : ext.getSelectedRevisionId(), null))
                        || !Objects.equals(expected.snapshot().get("sha256"), ext.getApprovedPendingRevisionId()!=null ? ext.getPendingRevisionHash() : ext.getSelectedRevisionHash())
                        || !Objects.equals(expected.snapshot().get("byte_length"), ext.getApprovedPendingRevisionId()!=null ? ext.getPendingByteLength() : ext.getSelectedByteLength())
                        || (ext.getApprovedPendingRevisionId()!=null
                            && !Objects.equals(ext.getApprovedPendingRevisionId(),ext.getPendingRevisionId()))
                        || !Objects.equals(source.getFileIdentity(),expected.fileIdentity())
                        || ext.getAuthorizationEpoch()!=expected.epoch() || ext.getFieldPolicyVersion()!=expected.policy())
                    throw new RagSourceChangedException();
            } else {
                var owner = versions.require(expected.ownerId());
                if (owner.deleted() || !owner.sourceType().equals(expected.type()) || owner.sourceVersion() != expected.version()
                        || owner.consentResetVersion() != expected.reset() || !source.getSourceId().equals(Long.toString(expected.ownerId()))
                        || !source.getSourceType().name().equals(expected.type())
                        || source.getClassification() != RagClassification.NON_PRIVATE || source.getAccessState() != RagAccessState.APPROVED
                        || !Objects.equals(source.getApprovedOwnerResetVersion(), expected.reset())
                        || source.getAuthorizationEpoch() != expected.epoch() || source.getFieldPolicyVersion() != expected.policy()
                        || !source.getApprovedFields().equals(expected.fields())) throw new RagSourceChangedException();
            }
            if (admission) {
                source.updateHashes(expected.contentHash(), expected.fields().contains("description")
                        ? expected.descriptionHash() : source.getDescriptionRevisionHash(), databaseTime());
            } else if (!source.getSourceRevisionHash().equals(expected.contentHash())
                    || (expected.fields().contains("description") && !source.getDescriptionRevisionHash().equals(expected.descriptionHash()))) {
                throw new RagSourceChangedException();
            }
            byId.put(source.getId(), source);
        }
        if (admission) entityManager.flush();
        var result = new HashMap<Long, Snapshot>();
        for (var root : prepared) result.put(root.rootId(), new Snapshot(
                Objects.requireNonNull(byId.get(root.rootId())), root.manifest(), root.payload()));
        return Map.copyOf(result);
    }

    public Snapshot lockSource(Long rootId) {
        return lockPrepared(prepareSource(rootId, Map.of()), null, true);
    }

    public Attempt lockAttempt(UUID jobId, UUID attemptId) { return lockAttempt(jobId, attemptId, false); }
    public Attempt lockCompletion(UUID jobId, UUID attemptId) { return lockAttempt(jobId, attemptId, true); }

    private Attempt lockAttempt(UUID jobId, UUID attemptId, boolean completion) {
        var initial = leases.findByJobIdAndAttemptId(jobId, attemptId).orElseThrow();
        var prepared = prepareSource(initial.getSourceId(), Map.of());
        return lockPreparedAttempt(jobId, attemptId, prepared, completion);
    }

    public Attempt lockPreparedAttempt(UUID jobId, UUID attemptId, Prepared prepared, boolean completion) {
        var initial = leases.findByJobIdAndAttemptId(jobId, attemptId).orElseThrow();
        if (!initial.getSourceId().equals(prepared.rootId())) throw new RagSourceChangedException();
        boolean completed = "COMPLETED".equals(initial.getStatus());
        Snapshot snapshot;
        try { snapshot = completed ? lockPreparedCurrent(prepared) : lockPrepared(prepared, initial.getReservationToken(), false); }
        catch(org.springframework.dao.DataIntegrityViolationException changedReservation) {
            throw new RagSourceChangedException();
        }
        var lease = leases.findByJobIdAndAttemptIdForUpdate(jobId, attemptId).orElseThrow(); entityManager.refresh(lease);
        if (!(Set.of("ADMITTED", "STARTING", "ACTIVE").contains(lease.getStatus()) || (completion && "COMPLETED".equals(lease.getStatus())))
                || (!completed && lease.isExpired(databaseTime()))) throw new IllegalStateException("Job lease is inactive or expired");
        if (!Objects.equals(lease.getAdmittedEpoch(), snapshot.root().getAuthorizationEpoch())
                || !Objects.equals(lease.getApprovedFieldsText(), snapshot.root().getApprovedFieldsText())
                || !Objects.equals(lease.getAdmittedFieldPolicyVersion(), snapshot.root().getFieldPolicyVersion())
                || !Objects.equals(lease.getContentRevisionHash(), snapshot.root().getSourceRevisionHash())
                || !Objects.equals(lease.getDependencyManifest(), snapshot.manifest())) throw new RagSourceChangedException();
        return new Attempt(lease, snapshot);
    }

    @Transactional(readOnly = true)
    public Map<Long, Prepared> prepareBatch(UUID jobId, UUID attemptId) {
        var lease = leases.findByJobIdAndAttemptId(jobId, attemptId).orElseThrow();
        var manifest = RagManifestScope.parse(lease.getDependencyManifest());
        if (!"3.0".equals(manifest.path("contract_version").asText()))
            throw new IllegalStateException("Document contract required");
        var result = new TreeMap<Long, Prepared>();
        for (var root : manifest.path("roots")) {
            long id = root.path("rag_source_id").asLong();
            var prepared = prepareSource(id, Map.of());
            try {
                if (!json.readTree(prepared.manifest()).equals(root.path("manifest")))
                    throw new RagSourceChangedException();
            } catch (com.fasterxml.jackson.core.JsonProcessingException failure) {
                throw new IllegalStateException("Invalid document source scope", failure);
            }
            if (result.putIfAbsent(id, prepared) != null) throw new RagSourceChangedException();
        }
        return Map.copyOf(result);
    }

    public Attempt lockPreparedBatchAttempt(UUID jobId, UUID attemptId, Map<Long, Prepared> prepared) {
        runtimeSecurity.requireIsolatedRuntime();
        var initial = leases.findByJobIdAndAttemptId(jobId, attemptId).orElseThrow();
        var snapshots = lockPreparedBatch(prepared.values(), initial.getReservationToken(), false);
        var lease = leases.findByJobIdAndAttemptIdForUpdate(jobId, attemptId).orElseThrow();
        entityManager.refresh(lease);
        if (!Set.of("ADMITTED", "STARTING", "ACTIVE").contains(lease.getStatus())
                || lease.isExpired(databaseTime()) || lease.isStagingClosed())
            throw new IllegalStateException("Job lease is inactive or expired");
        var manifest = RagManifestScope.parse(lease.getDependencyManifest());
        if (!"3.0".equals(manifest.path("contract_version").asText())
                || manifest.path("roots").size() != prepared.size()) throw new RagSourceChangedException();
        for (var root : manifest.path("roots")) {
            var proof = prepared.get(root.path("rag_source_id").asLong());
            try {
                if (proof == null || !root.path("manifest").equals(json.readTree(proof.manifest())))
                    throw new RagSourceChangedException();
            } catch (com.fasterxml.jackson.core.JsonProcessingException failure) {
                throw new IllegalStateException("Invalid document source scope", failure);
            }
        }
        return new Attempt(lease, Objects.requireNonNull(snapshots.get(lease.getSourceId())));
    }

    public List<Map<String, Object>> documentInput(UUID jobId, UUID attemptId, Map<Long, Prepared> prepared) {
        var lease = leases.findByJobIdAndAttemptId(jobId, attemptId).orElseThrow();
        var manifest = RagManifestScope.parse(lease.getDependencyManifest());
        if (!"3.0".equals(manifest.path("contract_version").asText()))
            throw new IllegalStateException("Document contract required");
        var input = new ArrayList<Map<String, Object>>();
        for (var root : manifest.path("roots")) {
            var proof = Objects.requireNonNull(prepared.get(root.path("rag_source_id").asLong()));
            input.add(RagDocumentInput.approvedRoot(root.path("root_key").asText(), proof));
        }
        if (RagDocumentInput.encode(input).getBytes(StandardCharsets.UTF_8).length > limits.inputBytes())
            throw new IllegalStateException("Worker input byte budget exceeded");
        return List.copyOf(input);
    }

    private String encode(Object value) {
        try {
            return json.writeValueAsString(value);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot encode source contract", e);
        }
    }

    public java.time.Instant databaseTime() {
        Number micros = (Number) entityManager.createNativeQuery("SELECT CAST(EXTRACT(EPOCH FROM clock_timestamp())*1000000 AS bigint)").getSingleResult();
        return java.time.Instant.ofEpochSecond(micros.longValue() / 1000000, (micros.longValue() % 1000000) * 1000);
    }

    static List<String> capturedTargetDigests(java.nio.file.Path original) {
        var names = new TreeSet<String>();
        // Path syntax only. No traversal, filesystem discovery or original-file read.
        for (int start=0; start<original.getNameCount(); start++) {
            String suffix=original.subpath(start,original.getNameCount()).toString().replace('\\','/');
            names.add(digest(suffix));
            if (suffix.toLowerCase(Locale.ROOT).endsWith(".md")) names.add(digest(suffix.substring(0,suffix.length()-3)));
        }
        if (names.size()>512) throw new IllegalArgumentException("Captured target identity budget exceeded");
        return List.copyOf(names);
    }

    static String capturedNameDigest(String name, List<String> targets) {
        // Preserve previously sealed basename-only proofs; new suffix proofs cannot
        // be grafted onto them because the independent SQL identity then differs.
        return targets.isEmpty() ? digest(name) : digest(name+"\n"+String.join("\n",targets));
    }

    public static String digest(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static Map<String, Object> fictionValues(FictionSourceProjection p) {
        Map<String, Object> m = new TreeMap<>();
        m.put("title", p.title());
        m.put("originalTitle", p.originalTitle());
        m.put("format", p.format());
        m.put("nationalityCode", p.nationalityCode());
        m.put("genreId", p.genreId());
        m.put("storyArchetypeIds", p.storyArchetypeIds().stream().sorted().toList());
        m.put("worldSettingIds", p.worldSettingIds().stream().sorted().toList());
        m.put("progressStatus", p.progressStatus());
        m.put("consumptionStatus", p.consumptionStatus());
        m.put("totalChapters", p.totalChapters());
        m.put("description", p.description());
        return m;
    }

    private static Map<String, Object> filmValues(FilmSourceProjection p) {
        Map<String, Object> m = new TreeMap<>();
        m.put("title", p.title());
        m.put("originalTitle", p.originalTitle());
        m.put("format", p.format());
        m.put("productionStyle", p.productionStyle());
        m.put("nationalityCode", p.nationalityCode());
        m.put("genreIds", p.genreIds().stream().sorted().toList());
        m.put("storyArchetypeIds", p.storyArchetypeIds().stream().sorted().toList());
        m.put("worldSettingIds", p.worldSettingIds().stream().sorted().toList());
        m.put("progressStatus", p.progressStatus());
        m.put("consumptionStatus", p.consumptionStatus());
        m.put("totalEpisodes", p.totalEpisodes());
        m.put("description", p.description());
        return m;
    }
}

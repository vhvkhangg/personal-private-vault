package com.vhvkhangg.personalprivatevault.rag.internal.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vhvkhangg.personalprivatevault.rag.api.RagDocumentBatchCommand;
import com.vhvkhangg.personalprivatevault.rag.api.RagDocumentRootItem;
import com.vhvkhangg.personalprivatevault.rag.api.RagJobAdmissionResult;
import com.vhvkhangg.personalprivatevault.rag.internal.domain.RagBatchRoot;
import com.vhvkhangg.personalprivatevault.rag.internal.domain.RagJobLease;
import com.vhvkhangg.personalprivatevault.rag.internal.domain.RagRun;
import com.vhvkhangg.personalprivatevault.rag.internal.domain.RagSource;
import com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.persistence.RagBatchRootRepository;
import com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.persistence.RagJobLeaseRepository;
import com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.persistence.RagRunRepository;
import com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.persistence.RagSourceRepository;
import com.vhvkhangg.personalprivatevault.rag.source.RagSourceType;
import com.vhvkhangg.personalprivatevault.vault.entry.SourceIndexReservations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;
import com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.client.RagEngineClient;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

/**
 * Orchestrates multi-root document batch admission and atomic reservation union (ADR-0021 Clause 1, 2).
 */
@Service
public class RagDocumentBatchService {

    private final RagSourceRepository sources;
    private final RagJobLeaseRepository leases;
    private final RagBatchRootRepository batchRoots;
    private final RagRunRepository runs;
    private final SourceIndexReservations vaultReservations;
    private final RagRuntimeIdentity runtimeIdentity;
    private final RagRuntimeSecurity runtimeSecurity;
    private final RagDependencyGraph graph;
    private final RagWorkerSupervisor supervisor;
    private final RagWorkerGrantService grants;
    private final RagEngineClient engineClient;
    private final RagPurgeService purgeService;
    private final RagExternalFileAuthority externalFileAuthority;

    private final RagSourceAuthority authority;
    private final RagNoChangeService noChange;
    private final TransactionTemplate transaction;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .enable(com.fasterxml.jackson.databind.SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);

    public RagDocumentBatchService(
            RagSourceRepository sources,
            RagJobLeaseRepository leases,
            RagBatchRootRepository batchRoots,
            RagRunRepository runs,
            SourceIndexReservations vaultReservations,
            RagRuntimeIdentity runtimeIdentity,
            RagRuntimeSecurity runtimeSecurity,
            RagDependencyGraph graph,
            RagWorkerSupervisor supervisor,
            RagWorkerGrantService grants,
            RagEngineClient engineClient,
            RagPurgeService purgeService,
            RagSourceAuthority authority, RagNoChangeService noChange,
            RagExternalFileAuthority externalFileAuthority,
            PlatformTransactionManager transactions
    ) {
        this.sources = sources;
        this.leases = leases;
        this.batchRoots = batchRoots;
        this.runs = runs;
        this.vaultReservations = vaultReservations;
        this.runtimeIdentity = runtimeIdentity;
        this.runtimeSecurity = runtimeSecurity;
        this.graph = graph;
        this.supervisor = supervisor;
        this.grants = grants;
        this.engineClient = engineClient;
        this.purgeService = purgeService;
        this.authority = authority;this.noChange=noChange;
        this.externalFileAuthority = Objects.requireNonNull(externalFileAuthority, "externalFileAuthority must not be null");
        this.transaction = new TransactionTemplate(transactions);
        this.transaction.setTimeout(15);
    }

    public RagJobAdmissionResult admitDocumentBatch(RagDocumentBatchCommand command) {
        runtimeSecurity.requireIsolatedRuntime();
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            throw new IllegalStateException("Batch admission must enter outside an ambient transaction");
        }
        Objects.requireNonNull(command, "command must not be null");

        Instant now = Instant.now();

        // Single active attempt check
        if (leases.countByStatusInAndLeasedUntilAfter(List.of("ADMITTED", "ACTIVE"), now) > 0) {
            throw new IllegalStateException("An active RAG attempt is already in flight");
        }

        // Validate distinct roots and deduplicate canonical sources
        Set<String> distinctRootKeys = new HashSet<>();
        Set<String> distinctSourceKeys = new HashSet<>();
        for (RagDocumentRootItem item : command.roots()) {
            if (!distinctRootKeys.add(item.rootKey())) {
                throw new IllegalArgumentException("Duplicate root key in batch admission: " + item.rootKey());
            }
            if (item.sourceType() == RagSourceType.EXTERNAL_FILE) {
                try {
                    UUID.fromString(item.sourceId());
                } catch (Exception e) {
                    throw new IllegalArgumentException("Invalid external file UUID: " + item.sourceId());
                }
            }
            String sourceKey = item.sourceType().name() + ":" + item.sourceId();
            if (!distinctSourceKeys.add(sourceKey)) {
                throw new IllegalArgumentException("Duplicate source admitted under different root key: " + sourceKey);
            }
        }

        UUID jobId = UUID.randomUUID();
        UUID attemptId = UUID.randomUUID();
        String leaseToken = UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");
        Instant leasedUntil = now.plus(Duration.ofMinutes(15));

        var prepared = new LinkedHashMap<String, RagSourceAuthority.Prepared>();
        var usedConfirmations = new HashSet<Long>();
        var topology = graph.read();
        for (var item : command.roots()) {
            var source = sources.findBySourceTypeAndSourceId(item.sourceType(), item.sourceId()).orElseThrow();
            var closure = new TreeSet<>(topology.descendants(source.getId())); closure.add(source.getId());
            var confirmations = new HashMap<Long, String>();
            command.descriptionConfirmations().forEach((id, hash) -> {
                if (closure.contains(id)) { confirmations.put(id, hash); usedConfirmations.add(id); }
            });
            var proof = authority.prepareSource(source.getId(), confirmations);
            RagDocumentInput.validateClosure(proof);
            var rootProof = proof.proofs().get(source.getId());
            if (!Objects.equals(item.expectedSourceVersion(), rootProof.version())
                    || !Objects.equals(item.expectedAuthorizationEpoch(), rootProof.epoch())
                    || !Objects.equals(item.expectedFieldPolicyVersion(), rootProof.policy())
                    || !item.approvedFields().equals(rootProof.fields()))
                throw new com.vhvkhangg.personalprivatevault.rag.api.RagSourceChangedException();
            prepared.put(item.rootKey(), proof);
        }
        if (!usedConfirmations.equals(command.descriptionConfirmations().keySet()))
            throw new IllegalArgumentException("Description confirmation is outside the selected fields");
        // Encoding and digest work happens before any SQL row guard.
        var unchanged=new LinkedHashMap<String,RagNoChangeService.Proof>();
        for(var item:command.roots())if(command.noChangeRoots().contains(item.rootKey())) {
            var proof=noChange.prepare(prepared.get(item.rootKey()).rootId());
            if(!proof.source().manifest().equals(prepared.get(item.rootKey()).manifest()))throw new com.vhvkhangg.personalprivatevault.rag.api.RagSourceChangedException();
            unchanged.put(item.rootKey(),proof);
        }
        if(unchanged.size()==command.roots().size()) {
            UUID operation=noChange.begin(unchanged,null);noChange.finish(operation,unchanged);
            var first=prepared.values().iterator().next();var p=first.proofs().get(first.rootId());
            return new RagJobAdmissionResult(null,null,first.rootId(),null,null,p.epoch(),operation,"NO_CHANGE");
        }
        String batchManifestJson = buildBatchManifest(command.roots(), prepared,unchanged);
        return transaction.execute(status -> {
            var snapshots = authority.lockPreparedBatch(prepared.values(), null, true);
            unchanged.values().forEach(noChange::validateLocked);
            var ownerIds = new TreeSet<Long>();
            var externalIds = new TreeSet<UUID>();
            for (var root : prepared.values()) {
                for (var proof : root.proofs().values()) {
                    if ("EXTERNAL_FILE".equals(proof.type())) {
                        externalIds.add(UUID.fromString(proof.sourceIdentity()));
                    } else {
                        ownerIds.add(proof.ownerId());
                    }
                }
            }
            if (!ownerIds.isEmpty()) {
                vaultReservations.reserve(ownerIds, attemptId);
            }
            if (!externalIds.isEmpty()) {
                externalFileAuthority.reserve(externalIds, attemptId);
            }
            var primaryRoot = snapshots.values().stream().map(RagSourceAuthority.Snapshot::root)
                    .min(Comparator.comparing(RagSource::getId)).orElseThrow();
            RagJobLease lease = new RagJobLease(
                    jobId, attemptId, primaryRoot.getId(), "ADMITTED", leaseToken,
                    leasedUntil, primaryRoot.getAuthorizationEpoch(), primaryRoot.getFieldPolicyVersion(),
                    primaryRoot.getApprovedFieldsText(), primaryRoot.getSourceRevisionHash(),
                    primaryRoot.getDescriptionRevisionHash(), batchManifestJson, now);
            lease.bindController(Map.of("process_id", ProcessHandle.current().pid(),
                    "started_at", ProcessHandle.current().info().startInstant().orElseThrow().toString(),
                    "runtime_id", runtimeIdentity.instance().toString()));
            leases.saveAndFlush(lease);
            for (var item : command.roots()) {
                var root = prepared.get(item.rootKey());
                var proof = root.proofs().get(root.rootId());
                batchRoots.save(new RagBatchRoot(jobId, item.rootKey(), root.rootId(),
                        proof.version(), proof.epoch(), now));
            }
            runs.save(new RagRun(UUID.randomUUID(), jobId, primaryRoot.getId(),
                    primaryRoot.getAuthorizationEpoch(), "ADMITTED", rosterMetrics(command.roots()), "NONE", now));
            return new RagJobAdmissionResult(jobId, attemptId, primaryRoot.getId(), leaseToken,
                    leasedUntil, primaryRoot.getAuthorizationEpoch());
        });
    }

    private String rosterMetrics(List<RagDocumentRootItem> items) {
        try {return objectMapper.writeValueAsString(Map.of("roster_order",items.stream().map(RagDocumentRootItem::rootKey).toList()));}
        catch(java.io.IOException invalid) {throw new IllegalStateException("Invalid admitted roster",invalid);}
    }

    private String buildBatchManifest(List<RagDocumentRootItem> items,
            Map<String, RagSourceAuthority.Prepared> prepared,Map<String,RagNoChangeService.Proof> unchanged) {
        try {
            var roots = new ArrayList<Map<String, Object>>();
            for (var item : items) {
                var root = prepared.get(item.rootKey());
                var proof = root.proofs().get(root.rootId());
                var value = new TreeMap<String, Object>();
                value.put("root_key", item.rootKey());
                if(unchanged.containsKey(item.rootKey()))value.put("no_change_generation",unchanged.get(item.rootKey()).generation().toString());
                value.put("source_type", proof.type());
                value.put("source_id", item.sourceId());
                value.put("rag_source_id", root.rootId());
                value.put("epoch", proof.epoch());
                value.put("source_version", proof.version());
                value.put("consent_reset_version", proof.reset());
                value.put("field_policy_version", proof.policy());
                value.put("fields", proof.fields().stream().sorted().toList());
                value.put("manifest", objectMapper.readTree(root.manifest()));
                roots.add(value);
            }
            return objectMapper.writeValueAsString(Map.of("contract_version", "3.0", "roots", roots));
        } catch (com.fasterxml.jackson.core.JsonProcessingException invalid) {
            throw new IllegalStateException("Invalid document batch scope", invalid);
        }
    }

    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.NEVER)
    public void executeDocumentBatch(UUID jobId, UUID attemptId, List<com.vhvkhangg.personalprivatevault.rag.api.RagDocumentBatchTask> tasks) {
        runtimeSecurity.requireIsolatedRuntime();
        Objects.requireNonNull(jobId, "jobId must not be null");
        Objects.requireNonNull(attemptId, "attemptId must not be null");
        Objects.requireNonNull(tasks, "tasks must not be null");

        var initial=leases.findByJobIdAndAttemptId(jobId,attemptId).orElseThrow();
        var expectedPlan=RagDocumentInput.workerPlan(initial.getDependencyManifest());
        if(!tasks.stream().map(com.vhvkhangg.personalprivatevault.rag.api.RagDocumentBatchTask::rootKey).toList().equals(expectedPlan))
            throw new IllegalArgumentException("Tasks must match the immutable Java-owned worker plan");
        try {
            supervisor.begin(jobId, attemptId);
        } catch (RagWorkerSupervisor.StartupFailure startupFailure) {
            if (supervisor.proveStopped(jobId)) {
                try {
                    purgeService.abortJob(jobId, false);
                } catch (RuntimeException unknownSql) {
                    purgeService.requireRecovery(jobId);
                }
            }
            throw startupFailure;
        }

        boolean succeeded = false;
        try {
            var prepared = authority.prepareBatch(jobId, attemptId);
            var admitted = new HashMap<String, RagBatchRoot>();
            batchRoots.findByJobId(jobId).forEach(root -> admitted.put(root.getRootKey(), root));
            if (tasks.isEmpty()) throw new IllegalArgumentException("Batch contains no tasks requiring worker execution");
            if (tasks.size() > admitted.size()) throw new IllegalArgumentException("Task count exceeds admitted roots");
            var seen = new HashSet<String>();
            for (var task : tasks) {
                var root = admitted.get(task.rootKey());
                if (root == null || !seen.add(task.rootKey())) throw new IllegalArgumentException("Invalid document root");
                var proof = prepared.get(root.getSourceId()).proofs().get(root.getSourceId());
                if (!task.sourceId().equals(proof.sourceIdentity())
                        || !new HashSet<>(task.approvedFields()).equals(proof.fields()))
                    throw new IllegalArgumentException("Document input must be prepared by source authority");
                if ("EXTERNAL_FILE".equalsIgnoreCase(proof.type())) {
                    if (!"MD".equalsIgnoreCase(task.format()) || task.contentText() != null
                            || task.contentB64() != null || !task.transclusions().isEmpty())
                        throw new IllegalArgumentException("External file task format must be MD and bodies must not be caller-supplied");
                } else {
                    if (!"DOMAIN".equalsIgnoreCase(task.format()) || task.contentText() != null
                            || task.contentB64() != null || !task.transclusions().isEmpty())
                        throw new IllegalArgumentException("Document input must be prepared by source authority");
                }
            }
            var rootTasks = authority.documentInput(jobId, attemptId, prepared, seen);
            String inputPayloadJson = RagDocumentInput.encode(rootTasks);

            var grant = grants.issueDocumentBatchGrant(jobId, attemptId, inputPayloadJson);
            var session = grants.executionSession(jobId, attemptId);
            var nonce = supervisor.requireReady(jobId, attemptId);

            String response=engineClient.triggerBoundWorkerBatch(grant, session, nonce, rootTasks);
            grants.verifyDocumentResult(grant,response);
            succeeded = true;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to execute document batch stage", e);
        } finally {
            supervisor.end(jobId);
            boolean stopped;
            try {
                stopped = supervisor.stopAndVerify(jobId);
            } catch (RuntimeException uncertainTermination) {
                stopped = false;
            }
            if (!stopped) {
                purgeService.requireRecovery(jobId);
                if (succeeded) throw new IllegalStateException("Worker termination is not yet verified");
            } else if (!succeeded) {
                try {
                    purgeService.abortJob(jobId, false);
                } catch (RuntimeException unknownSql) {
                    purgeService.requireRecovery(jobId);
                }
            }
        }
    }

}

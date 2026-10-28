package com.vhvkhangg.personalprivatevault.rag.internal.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vhvkhangg.personalprivatevault.rag.api.RagQueryUnavailableException;
import com.vhvkhangg.personalprivatevault.rag.internal.application.retrieval.RagRetrievalControllerIdentity;
import com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.client.NativeWorkerProcess;
import com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.storage.*;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.function.Predicate;

/** Owns snapshot inspection I/O and reclassifies the original durable operation after interruption. */
@Component
public class RagSnapshotInspections {
    private final EntityManager entities;
    private final RagOriginalBytesStore store;
    private final RagRuntimeIdentity runtime;
    private final TransactionTemplate transaction;
    private final ObjectMapper json=new ObjectMapper();
    private final Map<UUID,ActiveCheck> active=new HashMap<>();
    private final Map<UUID,FileCheck> files=new HashMap<>();
    private UUID cursor;
    private static final Set<String> SNAPSHOT_PRODUCERS=Set.of("QUERY_PREPARE","QUERY_EVIDENCE","SOURCE_INTEGRITY_CHECK","RECOVERY_SNAPSHOT");
    private static final class ActiveCheck { int readers; boolean unknown; boolean incident; }
    private static final class FileCheck { int users; }
    private record Operation(UUID id,UUID reader,UUID file,UUID revision,String state,String producer,JsonNode proof) {}

    public RagSnapshotInspections(EntityManager entities,RagOriginalBytesStore store,RagRuntimeIdentity runtime,
            PlatformTransactionManager transactions) {
        this.entities=entities;this.store=store;this.runtime=runtime;
        transaction=new TransactionTemplate(transactions);transaction.setTimeout(10);
        transaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    public void verify(UUID file,UUID revision,String hash,long length,String producer,String digest) {
        inspect(file,revision,hash,length,producer,digest,null,null,false);
    }

    public String read(UUID file,UUID revision,String hash,long length,UUID reader,UUID token) {
        return inspect(file,revision,hash,length,"QUERY_EVIDENCE","",reader,token,true);
    }

    private String inspect(UUID file,UUID revision,String hash,long length,String producer,String digest,
            UUID reader,UUID token,boolean read) {
        FileCheck guard;
        synchronized(files) {guard=files.computeIfAbsent(file,key->new FileCheck());guard.users++;}
        try {
            synchronized(guard) {return inspectGuarded(file,revision,hash,length,producer,digest,reader,token,read);}
        } finally {synchronized(files) {if(--guard.users==0) files.remove(file,guard);}}
    }

    private String inspectGuarded(UUID file,UUID revision,String hash,long length,String producer,String digest,
            UUID reader,UUID token,boolean read) {
        UUID requested=UUID.randomUUID();
        UUID id=transaction.execute(status -> {
            if(reader!=null) return (UUID)entities.createNativeQuery(
                    "SELECT rag.retrieval_reader_inspection_begin(:id,:reader,:token,:file,:revision)")
                    .setParameter("id",requested).setParameter("reader",reader).setParameter("token",token)
                    .setParameter("file",file).setParameter("revision",revision).getSingleResult();
            @SuppressWarnings("unchecked") List<UUID> outstanding=entities.createNativeQuery(
                    "SELECT inspection_id FROM rag.retrieval_inspections WHERE reader_id IS NULL AND affected_file_source_id=:file AND affected_revision_id=:revision AND state='UNRESOLVED' ORDER BY created_at,inspection_id")
                    .setParameter("file",file).setParameter("revision",revision).setMaxResults(1).getResultList();
            if(!outstanding.isEmpty()) return outstanding.getFirst();
            return (UUID)entities.createNativeQuery("SELECT rag.retrieval_inspection_begin(:id,NULL,:producer,:digest,:file,:revision,:pid,:creation,:controller)")
                    .setParameter("id",requested).setParameter("producer",producer).setParameter("digest",digest)
                    .setParameter("file",file).setParameter("revision",revision).setParameter("pid",ProcessHandle.current().pid())
                    .setParameter("creation",RagRetrievalControllerIdentity.creationFileTime()).setParameter("controller",controllerInstance()).getSingleResult();
        });
        Operation operation=load(id);
        if(!SNAPSHOT_PRODUCERS.contains(operation.producer()) || !own(operation) || !hash.equals(operation.proof().path("sha256").asText())
                || length!=operation.proof().path("byte_length").asLong(-1))
            throw new RagQueryUnavailableException("Snapshot inspection ownership changed");
        ActiveCheck check;
        synchronized(active) {check=active.computeIfAbsent(id,key->new ActiveCheck());check.readers++;}
        boolean verified=false;
        try {
            String result=null;
            if(read) {
                byte[] bytes=store.readRevisionBytes(file,revision,hash,length);
                try { result=new String(bytes,StandardCharsets.UTF_8); } finally {Arrays.fill(bytes,(byte)0);}
            } else store.verifyIntegrity(file,revision,hash,length);
            verified=true;return result;
        } catch(SnapshotIntegrityException | SnapshotMissingException incident) {
            synchronized(active) {check.incident=true;}
            classify(id,"INCIDENT_PENDING");
            try {deny(id);} catch(RuntimeException uncertain) {incident.addSuppressed(uncertain);}
            throw incident;
        } finally {
            boolean clean;
            synchronized(active) {
                if(!verified && !check.incident) check.unknown=true;
                clean=--check.readers==0 && verified && !check.unknown && !check.incident;
                // Retain the active marker until the classification attempt itself has returned.
                if(check.readers==0 && !clean) active.remove(id,check);
            }
            if(clean) {
                try {classify(id,"CLEAN");}
                finally { synchronized(active) {if(check.readers==0) active.remove(id,check);} }
            }
        }
    }

    private boolean own(Operation operation) {
        return operation.proof().path("controller_instance").asText().equals(runtime.instance().toString())
                && operation.proof().path("controller_pid").asLong()==ProcessHandle.current().pid()
                && operation.proof().path("controller_creation_file_time").asText().equals(Long.toString(RagRetrievalControllerIdentity.creationFileTime()));
    }

    private boolean stopped(Operation operation) {
        try {
            if(own(operation)) { synchronized(active) {return !active.containsKey(operation.id());} }
            long pid=operation.proof().path("controller_pid").asLong();
            long creation=Long.parseLong(operation.proof().path("controller_creation_file_time").asText());
            return pid>0 && creation>0 && NativeWorkerProcess.isRecordedProcessTerminated(pid,creation,Duration.ZERO);
        } catch(RuntimeException unknown) {return false;}
    }

    private Operation load(UUID id) {
        Object[] row=(Object[])entities.createNativeQuery("SELECT inspection_id,reader_id,affected_file_source_id,affected_revision_id,state,producer,inspection_proof::text FROM rag.retrieval_inspections WHERE inspection_id=:id")
                .setParameter("id",id).getSingleResult();
        try {return new Operation((UUID)row[0],(UUID)row[1],(UUID)row[2],(UUID)row[3],row[4].toString(),row[5].toString(),json.readTree(row[6].toString()));}
        catch(java.io.IOException invalid) {throw new IllegalStateException("Invalid inspection proof");}
    }

    private void classify(UUID id,String state) {
        transaction.executeWithoutResult(status -> entities.createNativeQuery(
                "SELECT rag.retrieval_inspection_record(:id,:state,:deny,'{\"reason\":\"EXACT_SNAPSHOT_CHECK\"}'::jsonb)")
                .setParameter("id",id).setParameter("state",state).setParameter("deny",!"CLEAN".equals(state)).getSingleResult());
    }

    private void deny(UUID id) {
        transaction.executeWithoutResult(status -> entities.createNativeQuery("SELECT rag.retrieval_inspection_deny(:id)")
                .setParameter("id",id).getSingleResult());
    }

    public boolean hasBlocking(UUID file,UUID revision) {
        return Boolean.TRUE.equals(entities.createNativeQuery("SELECT rag.retrieval_inspection_blocks(:file,:revision,false)")
                .setParameter("file",file).setParameter("revision",revision).getSingleResult());
    }

    public String controllerInstance() {return runtime.instance().toString();}

    public Set<UUID> stoppedOrOwnedInventory() {
        @SuppressWarnings("unchecked") List<UUID> ids=entities.createNativeQuery(
                "SELECT inspection_id FROM rag.retrieval_inspections WHERE state IN ('UNRESOLVED','INCIDENT_PENDING') ORDER BY inspection_id")
                .setMaxResults(4097).getResultList();
        if(ids.size()>4096) return null;
        for(UUID id:ids) {var operation=load(id);if(!own(operation) && !stopped(operation)) return null;}
        return Set.copyOf(ids);
    }

    public boolean inventoryStillKnown(Set<UUID> proven) {
        @SuppressWarnings("unchecked") List<UUID> fresh=entities.createNativeQuery(
                "SELECT inspection_id FROM rag.retrieval_inspections WHERE state IN ('UNRESOLVED','INCIDENT_PENDING') ORDER BY inspection_id")
                .setMaxResults(4097).getResultList();
        return fresh.size()<=4096 && proven.containsAll(fresh);
    }

    /** Called by the existing production reconciler, including before runtime takeover. */
    public synchronized void reconcile(Set<UUID> stoppedReaders,Predicate<UUID> localReaderQuiescent) {
        @SuppressWarnings("unchecked") List<UUID> ids=entities.createNativeQuery(
                "SELECT inspection_id FROM rag.retrieval_inspections WHERE state IN ('UNRESOLVED','INCIDENT_PENDING') AND (CAST(:cursor AS uuid) IS NULL OR inspection_id>CAST(:cursor AS uuid)) ORDER BY inspection_id")
                .setParameter("cursor",cursor).setMaxResults(25).getResultList();
        cursor=ids.size()<25?null:ids.getLast();
        for(UUID id:ids) {
            var operation=load(id);
            FileCheck guard;
            synchronized(files) {guard=files.computeIfAbsent(operation.file(),key->new FileCheck());guard.users++;}
            try { synchronized(guard) {
            if(!SNAPSHOT_PRODUCERS.contains(operation.producer()) || operation.revision()==null || !operation.proof().path("sha256").isTextual()
                    || !operation.proof().path("byte_length").canConvertToLong() || !stopped(operation)) continue;
            if(operation.reader()!=null && !stoppedReaders.contains(operation.reader())
                    && !localReaderQuiescent.test(operation.reader())) {
                String state=entities.createNativeQuery("SELECT state FROM rag.retrieval_readers WHERE reader_id=:id")
                        .setParameter("id",operation.reader()).getSingleResult().toString();
                if(!"TERMINAL".equals(state)) continue;
            }
            try {
                if("UNRESOLVED".equals(operation.state())) {
                    try {
                        store.verifyIntegrity(operation.file(),operation.revision(),operation.proof().path("sha256").asText(),operation.proof().path("byte_length").asLong());
                        classify(id,"CLEAN");
                    } catch(SnapshotIntegrityException | SnapshotMissingException incident) {classify(id,"INCIDENT_PENDING");}
                }
                // Idempotent, exact revision CAS. Reserved sources stay for the reader finisher.
                deny(id);
            } catch(RuntimeException unproven) {
                org.slf4j.LoggerFactory.getLogger(getClass()).warn("Snapshot inspection reconciliation remains unproven");
            }
            } } finally {synchronized(files) {if(--guard.users==0) files.remove(operation.file(),guard);}}
        }
    }
}

package com.vhvkhangg.personalprivatevault.rag.internal.application.retrieval;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class RagRetrievalAuthority {

    private final EntityManager entityManager;
    private final RagRetrievalCurrentAuthority currentAuthority;
    private final com.vhvkhangg.personalprivatevault.rag.internal.application.RagSnapshotInspections inspections;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public RagRetrievalAuthority(EntityManager entityManager, RagRetrievalCurrentAuthority currentAuthority,
            com.vhvkhangg.personalprivatevault.rag.internal.application.RagSnapshotInspections inspections) {
        this.entityManager = entityManager;
        this.currentAuthority = currentAuthority;
        this.inspections=inspections;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, timeout = 10)
    public void admitPrepared(UUID readerId, UUID token, String digest, Map<String, Object> payload,
            Collection<UUID> files, String controller, Collection<RagRetrievalCurrentAuthority.Proof> proofs) {
        currentAuthority.lock(proofs);
        admit(readerId, token, digest, payload, files, controller);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, timeout = 10)
    public void beginPreparedEmission(UUID readerId, Collection<RagRetrievalCurrentAuthority.Proof> proofs) {
        currentAuthority.lock(proofs);
        beginEmission(readerId);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, timeout = 10)
    public void bindWorker(UUID readerId, String controller, long pid, java.time.Instant created, long fileTime) {
        entityManager.createNativeQuery("SELECT rag.retrieval_reader_bind_worker(:reader,:controller,:pid,:created,:fileTime)")
                .setParameter("reader",readerId).setParameter("controller",controller).setParameter("pid",pid)
                .setParameter("created",created).setParameter("fileTime",fileTime).getSingleResult();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, timeout = 10)
    public UUID beginReaderInspection(UUID inspection, UUID reader, UUID token, UUID file, UUID revision) {
        return (UUID)entityManager.createNativeQuery("SELECT rag.retrieval_reader_inspection_begin(:inspection,:reader,:token,:file,:revision)")
                .setParameter("inspection",inspection).setParameter("reader",reader).setParameter("token",token)
                .setParameter("file",file).setParameter("revision",revision).getSingleResult();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, timeout = 10)
    public String observe(UUID reader, UUID token) {
        Object state=entityManager.createNativeQuery("SELECT rag.retrieval_reader_observe(:reader,:token)")
                .setParameter("reader",reader).setParameter("token",token).getSingleResult();
        return state==null?null:state.toString();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, timeout = 10)
    public void admitEmpty(UUID reader, UUID token, String controller) {
        entityManager.createNativeQuery("SELECT rag.retrieval_reader_admit_empty(:reader,:token,:controller,:pid,:creation)")
                .setParameter("reader",reader).setParameter("token",token).setParameter("controller",controller)
                .setParameter("pid",ProcessHandle.current().pid()).setParameter("creation",RagRetrievalControllerIdentity.creationFileTime())
                .getSingleResult();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, timeout = 10)
    public void admit(
            UUID readerId,
            UUID reservationToken,
            String scopeDigest,
            Map<String, Object> scopePayload,
            Collection<UUID> externalFileIds,
            String controllerInstance
    ) {
        String payloadJson = serializeJson(scopePayload);
        UUID[] sortedFileIds = externalFileIds.stream().sorted().toArray(UUID[]::new);

        entityManager.createNativeQuery(
                "SELECT rag.retrieval_reader_admit(:readerId, :token, :scopeDigest, CAST(:payloadJson AS jsonb), :fileIds, :controller)"
        )
                .setParameter("readerId", readerId)
                .setParameter("token", reservationToken)
                .setParameter("scopeDigest", scopeDigest)
                .setParameter("payloadJson", payloadJson)
                .setParameter("fileIds", sortedFileIds)
                .setParameter("controller", controllerInstance)
                .getSingleResult();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, timeout = 10)
    public void beginEmission(UUID readerId) {
        entityManager.createNativeQuery("SELECT rag.retrieval_reader_begin_emission(:readerId)")
                .setParameter("readerId", readerId)
                .getSingleResult();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, timeout = 10)
    public void requestStop(UUID readerId, String reason) {
        entityManager.createNativeQuery("SELECT rag.retrieval_reader_request_stop(:readerId, :reason)")
                .setParameter("readerId", readerId)
                .setParameter("reason", reason != null ? reason : "CANCEL_REQUESTED")
                .getSingleResult();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, timeout = 10)
    public void finish(
            UUID readerId,
            UUID reservationToken,
            String finishDisposition,
            String emissionDisposition,
            Map<String, Object> quiescenceEvidence,
            Collection<UUID> incidentFileIds,
            Collection<UUID> incidentRevisions
    ) {
        String evidenceJson = quiescenceEvidence != null ? serializeJson(quiescenceEvidence) : null;
        // These are positional (file, revision) tuples. Sorting either side destroys identity.
        UUID[] fileIds = incidentFileIds != null ? incidentFileIds.toArray(UUID[]::new) : new UUID[0];
        UUID[] revIds = incidentRevisions != null ? incidentRevisions.toArray(UUID[]::new) : new UUID[0];

        entityManager.createNativeQuery(
                "SELECT rag.retrieval_reader_finish(:readerId, :token, :finishDisp, :emissionDisp, " +
                (evidenceJson != null ? "CAST(:evidenceJson AS jsonb)" : "NULL::jsonb") +
                ", :incidentFileIds, :incidentRevisions)"
        )
                .setParameter("readerId", readerId)
                .setParameter("token", reservationToken)
                .setParameter("finishDisp", finishDisposition)
                .setParameter("emissionDisp", emissionDisposition)
                .setParameter("incidentFileIds", fileIds)
                .setParameter("incidentRevisions", revIds)
                .unwrap(org.hibernate.query.NativeQuery.class)
                .setParameter("evidenceJson", evidenceJson, org.hibernate.type.StandardBasicTypes.STRING)
                .getSingleResult();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, timeout = 10)
    public UUID beginInspection(
            UUID inspectionId,
            UUID readerId,
            String producer,
            String scopeDigest,
            UUID fileSourceId,
            UUID revisionId
    ) {
        return (UUID)entityManager.createNativeQuery(
                "SELECT rag.retrieval_inspection_begin(:inspectionId, :readerId, :producer, :scopeDigest, :fileSourceId, :revisionId,:pid,:creation,:controller)"
        )
                .setParameter("inspectionId", inspectionId)
                .setParameter("readerId", readerId)
                .setParameter("producer", producer)
                .setParameter("scopeDigest", scopeDigest)
                .setParameter("fileSourceId", fileSourceId)
                .setParameter("revisionId", revisionId)
                .setParameter("pid",ProcessHandle.current().pid()).setParameter("creation",RagRetrievalControllerIdentity.creationFileTime())
                .setParameter("controller",inspections.controllerInstance())
                .getSingleResult();
    }

    public void verifySnapshot(UUID file,UUID revision,String hash,long length,String producer,String digest) {
        inspections.verify(file,revision,hash,length,producer,digest);
    }

    public String readSnapshot(UUID reader,UUID token,UUID file,UUID revision,String hash,long length) {
        return inspections.read(file,revision,hash,length,reader,token);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, timeout = 10)
    public void recordInspection(
            UUID inspectionId,
            String state,
            boolean denyNewAdmission,
            Map<String, Object> details
    ) {
        String detailsJson = details != null ? serializeJson(details) : null;

        entityManager.createNativeQuery(
                "SELECT rag.retrieval_inspection_record(:inspectionId, :state, :denyNewAdmission, " +
                (detailsJson != null ? "CAST(:detailsJson AS jsonb)" : "NULL::jsonb") + ")"
        )
                .setParameter("inspectionId", inspectionId)
                .setParameter("state", state)
                .setParameter("denyNewAdmission", denyNewAdmission)
                .unwrap(org.hibernate.query.NativeQuery.class)
                .setParameter("detailsJson", detailsJson, org.hibernate.type.StandardBasicTypes.STRING)
                .getSingleResult();
    }

    private String serializeJson(Map<String, Object> map) {
        try {
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to serialize payload to JSON", e);
        }
    }
}

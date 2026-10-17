package com.vhvkhangg.personalprivatevault.rag;

import com.vhvkhangg.personalprivatevault.rag.api.RagConsentCommand;
import com.vhvkhangg.personalprivatevault.rag.api.RagIndexCommand;
import com.vhvkhangg.personalprivatevault.rag.api.RagJobAdmissionResult;
import com.vhvkhangg.personalprivatevault.rag.api.RagOperations;
import com.vhvkhangg.personalprivatevault.rag.source.RagClassification;
import com.vhvkhangg.personalprivatevault.rag.source.RagSourceType;
import com.vhvkhangg.personalprivatevault.rag.source.RagSourceView;

import java.util.Map;
import java.util.Set;

/** Explicit synthetic owner intent for fixtures; no retry, renewal or lifecycle bypass. */
final class RagTestCommands {
    private RagTestCommands() {
    }

    static RagSourceView register(RagOperations rag, RagSourceType type, String ownerId) {
        return rag.registerDomainCandidate(type, ownerId, RagClassification.NON_PRIVATE, null, null);
    }

    static RagSourceView approve(RagOperations rag, Long sourceId, Set<String> fields) {
        RagSourceView preview = rag.findSource(sourceId).orElseThrow();
        return rag.approveSource(sourceId, consent(preview, fields));
    }

    static RagConsentCommand consent(RagSourceView preview, Set<String> fields) {
        return new RagConsentCommand(fields, preview.sourceVersion(), preview.consentResetVersion(),
                preview.authorizationEpoch(), preview.fieldPolicyVersion(),
                fields.contains("description") ? preview.currentDescriptionRevisionHash() : null);
    }

    static RagJobAdmissionResult index(RagOperations rag, Long sourceId) {
        RagSourceView preview = rag.findSource(sourceId).orElseThrow();
        return rag.admitJob(sourceId, new RagIndexCommand(preview.sourceVersion(),
                preview.authorizationEpoch(), preview.fieldPolicyVersion(), Map.of()));
    }
}

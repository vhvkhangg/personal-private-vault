package com.vhvkhangg.personalprivatevault.knowledge.internal.application.search;

import com.vhvkhangg.personalprivatevault.knowledge.information.search.InformationSearchOperations;
import com.vhvkhangg.personalprivatevault.knowledge.note.search.NoteSearchOperations;
import com.vhvkhangg.personalprivatevault.knowledge.search.KnowledgeSearchDocument;
import com.vhvkhangg.personalprivatevault.knowledge.search.KnowledgeSearchHit;
import com.vhvkhangg.personalprivatevault.knowledge.search.KnowledgeSearchOperations;
import com.vhvkhangg.personalprivatevault.knowledge.search.KnowledgeSearchQuery;
import com.vhvkhangg.personalprivatevault.knowledge.study.search.StudySearchOperations;
import com.vhvkhangg.personalprivatevault.knowledge.vocabulary.search.VocabularySearchOperations;
import com.vhvkhangg.personalprivatevault.vault.enums.VaultEntryType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KnowledgeSearchService implements KnowledgeSearchOperations {

    private final StudySearchOperations studySearchOperations;
    private final InformationSearchOperations informationSearchOperations;
    private final VocabularySearchOperations vocabularySearchOperations;
    private final NoteSearchOperations noteSearchOperations;

    private static final Comparator<KnowledgeSearchHit> HIT_COMPARATOR =
            Comparator.comparingInt(KnowledgeSearchHit::rankBucket).reversed()
                    .thenComparing(Comparator.comparingDouble(KnowledgeSearchHit::similarity).reversed())
                    .thenComparing(h -> h.entryType().name())
                    .thenComparing(KnowledgeSearchHit::vaultEntryId);

    @Override
    public List<KnowledgeSearchHit> search(KnowledgeSearchQuery query) {
        if (query == null) {
            throw new IllegalArgumentException("Query must not be null");
        }

        Set<VaultEntryType> types = query.entryTypes();
        List<KnowledgeSearchHit> allHits = new ArrayList<>();

        if (types.isEmpty() || types.contains(VaultEntryType.STUDY)) {
            studySearchOperations.search(query.query(), query.requiredTagIds(), query.limit())
                    .forEach(h -> allHits.add(new KnowledgeSearchHit(
                            h.vaultEntryId(),
                            h.entryType(),
                            h.primaryText(),
                            h.secondaryText(),
                            h.snippet(),
                            h.rankBucket(),
                            h.similarity(),
                            h.matchKind()
                    )));
        }

        if (types.isEmpty() || types.contains(VaultEntryType.INFORMATION)) {
            informationSearchOperations.search(query.query(), query.requiredTagIds(), query.limit())
                    .forEach(h -> allHits.add(new KnowledgeSearchHit(
                            h.vaultEntryId(),
                            h.entryType(),
                            h.primaryText(),
                            h.secondaryText(),
                            h.snippet(),
                            h.rankBucket(),
                            h.similarity(),
                            h.matchKind()
                    )));
        }

        if (types.isEmpty() || types.contains(VaultEntryType.VOCABULARY)) {
            vocabularySearchOperations.search(query.query(), query.requiredTagIds(), query.limit())
                    .forEach(h -> allHits.add(new KnowledgeSearchHit(
                            h.vaultEntryId(),
                            h.entryType(),
                            h.primaryText(),
                            h.secondaryText(),
                            h.snippet(),
                            h.rankBucket(),
                            h.similarity(),
                            h.matchKind()
                    )));
        }

        if (types.isEmpty() || types.contains(VaultEntryType.NOTE)) {
            noteSearchOperations.search(query.query(), query.requiredTagIds(), query.limit())
                    .forEach(h -> allHits.add(new KnowledgeSearchHit(
                            h.vaultEntryId(),
                            h.entryType(),
                            h.primaryText(),
                            h.secondaryText(),
                            h.snippet(),
                            h.rankBucket(),
                            h.similarity(),
                            h.matchKind()
                    )));
        }

        allHits.sort(HIT_COMPARATOR);
        if (allHits.size() > query.limit()) {
            return allHits.subList(0, query.limit());
        }
        return allHits;
    }

    @Override
    public Map<Long, KnowledgeSearchDocument> lookupDocuments(Set<Long> vaultEntryIds) {
        if (vaultEntryIds == null || vaultEntryIds.isEmpty()) {
            return Map.of();
        }
        if (vaultEntryIds.size() > 601) {
            throw new IllegalArgumentException("Lookup IDs batch size must not exceed 601");
        }
        for (Long id : vaultEntryIds) {
            if (id == null || id <= 0) {
                throw new IllegalArgumentException("Lookup ID must be a positive number");
            }
        }
        Map<Long, KnowledgeSearchDocument> combined = new HashMap<>();

        studySearchOperations.lookupDocuments(vaultEntryIds).forEach((id, doc) ->
                combined.put(id, new KnowledgeSearchDocument(doc.vaultEntryId(), doc.entryType(), doc.primaryText(), doc.secondaryText())));

        informationSearchOperations.lookupDocuments(vaultEntryIds).forEach((id, doc) ->
                combined.put(id, new KnowledgeSearchDocument(doc.vaultEntryId(), doc.entryType(), doc.primaryText(), doc.secondaryText())));

        vocabularySearchOperations.lookupDocuments(vaultEntryIds).forEach((id, doc) ->
                combined.put(id, new KnowledgeSearchDocument(doc.vaultEntryId(), doc.entryType(), doc.primaryText(), doc.secondaryText())));

        noteSearchOperations.lookupDocuments(vaultEntryIds).forEach((id, doc) ->
                combined.put(id, new KnowledgeSearchDocument(doc.vaultEntryId(), doc.entryType(), doc.primaryText(), doc.secondaryText())));

        return combined;
    }
}

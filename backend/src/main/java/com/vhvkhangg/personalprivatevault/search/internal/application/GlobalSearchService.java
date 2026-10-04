package com.vhvkhangg.personalprivatevault.search.internal.application;

import com.vhvkhangg.personalprivatevault.account.search.AccountSearchOperations;
import com.vhvkhangg.personalprivatevault.account.search.AccountSearchQuery;
import com.vhvkhangg.personalprivatevault.collection.search.CollectionSearchOperations;
import com.vhvkhangg.personalprivatevault.collection.search.CollectionSearchQuery;
import com.vhvkhangg.personalprivatevault.feed.search.FeedSearchOperations;
import com.vhvkhangg.personalprivatevault.feed.search.FeedSearchQuery;
import com.vhvkhangg.personalprivatevault.fiction.search.FictionSearchOperations;
import com.vhvkhangg.personalprivatevault.fiction.search.FictionSearchQuery;
import com.vhvkhangg.personalprivatevault.film.search.FilmSearchOperations;
import com.vhvkhangg.personalprivatevault.film.search.FilmSearchQuery;
import com.vhvkhangg.personalprivatevault.knowledge.search.KnowledgeSearchOperations;
import com.vhvkhangg.personalprivatevault.knowledge.search.KnowledgeSearchQuery;
import com.vhvkhangg.personalprivatevault.location.search.LocationSearchOperations;
import com.vhvkhangg.personalprivatevault.location.search.LocationSearchQuery;
import com.vhvkhangg.personalprivatevault.media.search.MediaSearchOperations;
import com.vhvkhangg.personalprivatevault.media.search.MediaSearchQuery;
import com.vhvkhangg.personalprivatevault.people.search.PeopleSearchOperations;
import com.vhvkhangg.personalprivatevault.people.search.PeopleSearchQuery;
import com.vhvkhangg.personalprivatevault.search.enums.SearchDomain;
import com.vhvkhangg.personalprivatevault.search.enums.SearchMatchKind;
import com.vhvkhangg.personalprivatevault.search.query.GlobalSearchOperations;
import com.vhvkhangg.personalprivatevault.search.query.GlobalSearchQuery;
import com.vhvkhangg.personalprivatevault.search.view.GlobalSearchPage;
import com.vhvkhangg.personalprivatevault.search.view.GlobalSearchResult;
import com.vhvkhangg.personalprivatevault.vault.enums.VaultEntryType;
import com.vhvkhangg.personalprivatevault.vault.search.VaultSearchOperations;
import com.vhvkhangg.personalprivatevault.vault.search.VaultTagCandidateHit;
import com.vhvkhangg.personalprivatevault.vault.search.VaultTagSearchQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GlobalSearchService implements GlobalSearchOperations {

    private final VaultSearchOperations vaultSearchOperations;
    private final PeopleSearchOperations peopleSearchOperations;
    private final FictionSearchOperations fictionSearchOperations;
    private final FilmSearchOperations filmSearchOperations;
    private final MediaSearchOperations mediaSearchOperations;
    private final LocationSearchOperations locationSearchOperations;
    private final KnowledgeSearchOperations knowledgeSearchOperations;
    private final CollectionSearchOperations collectionSearchOperations;
    private final AccountSearchOperations accountSearchOperations;
    private final FeedSearchOperations feedSearchOperations;

    private static final Comparator<CandidateHit> GLOBAL_COMPARATOR =
            Comparator.comparingInt(CandidateHit::rankBucket).reversed()
                    .thenComparing(Comparator.comparingDouble(CandidateHit::similarity).reversed())
                    .thenComparing(h -> h.entryType().name())
                    .thenComparing(CandidateHit::vaultEntryId);

    @Override
    public GlobalSearchPage search(GlobalSearchQuery query) {
        if (query == null) {
            throw new IllegalArgumentException("Query must not be null");
        }

        int offset = query.offset();
        int limit = query.limit();
        int K = offset + limit;
        int targetK = Math.min(K + 1, 601);

        Set<SearchDomain> requestedDomains = query.domains().isEmpty()
                ? EnumSet.allOf(SearchDomain.class)
                : query.domains();
        Set<VaultEntryType> requestedEntryTypes = query.entryTypes().isEmpty()
                ? EnumSet.allOf(VaultEntryType.class)
                : query.entryTypes();

        Map<SearchDomain, Set<VaultEntryType>> activeDomainTypes = new EnumMap<>(SearchDomain.class);
        Set<VaultEntryType> allEffectiveTypes = new HashSet<>();

        for (SearchDomain domain : requestedDomains) {
            Set<VaultEntryType> intersection = new HashSet<>(domain.supportedEntryTypes());
            intersection.retainAll(requestedEntryTypes);
            if (!intersection.isEmpty()) {
                activeDomainTypes.put(domain, intersection);
                allEffectiveTypes.addAll(intersection);
            }
        }

        if (activeDomainTypes.isEmpty()) {
            return new GlobalSearchPage(offset, limit, List.of(), false);
        }

        String queryText = query.query();
        Set<Long> requiredTagIds = query.requiredTagIds();

        List<CandidateHit> candidateHits = new ArrayList<>();

        // 1. Fan out to selected feature domain text search
        for (Map.Entry<SearchDomain, Set<VaultEntryType>> entry : activeDomainTypes.entrySet()) {
            SearchDomain domain = entry.getKey();
            Set<VaultEntryType> types = entry.getValue();

            List<CandidateHit> domainHits = switch (domain) {
                case PEOPLE -> peopleSearchOperations.search(new PeopleSearchQuery(queryText, types, requiredTagIds, targetK)).stream()
                        .map(h -> new CandidateHit(h.vaultEntryId(), h.entryType(), domain, h.primaryText(), h.secondaryText(), h.snippet(), SearchMatchKind.valueOf(h.matchKind()), h.rankBucket(), h.similarity(), false))
                        .toList();
                case FICTION -> fictionSearchOperations.search(new FictionSearchQuery(queryText, types, requiredTagIds, targetK)).stream()
                        .map(h -> new CandidateHit(h.vaultEntryId(), h.entryType(), domain, h.primaryText(), h.secondaryText(), h.snippet(), SearchMatchKind.valueOf(h.matchKind()), h.rankBucket(), h.similarity(), false))
                        .toList();
                case FILM -> filmSearchOperations.search(new FilmSearchQuery(queryText, types, requiredTagIds, targetK)).stream()
                        .map(h -> new CandidateHit(h.vaultEntryId(), h.entryType(), domain, h.primaryText(), h.secondaryText(), h.snippet(), SearchMatchKind.valueOf(h.matchKind()), h.rankBucket(), h.similarity(), false))
                        .toList();
                case MEDIA -> mediaSearchOperations.search(new MediaSearchQuery(queryText, types, requiredTagIds, targetK)).stream()
                        .map(h -> new CandidateHit(h.vaultEntryId(), h.entryType(), domain, h.primaryText(), h.secondaryText(), h.snippet(), SearchMatchKind.valueOf(h.matchKind()), h.rankBucket(), h.similarity(), false))
                        .toList();
                case LOCATION -> locationSearchOperations.search(new LocationSearchQuery(queryText, types, requiredTagIds, targetK)).stream()
                        .map(h -> new CandidateHit(h.vaultEntryId(), h.entryType(), domain, h.primaryText(), h.secondaryText(), h.snippet(), SearchMatchKind.valueOf(h.matchKind()), h.rankBucket(), h.similarity(), false))
                        .toList();
                case KNOWLEDGE -> knowledgeSearchOperations.search(new KnowledgeSearchQuery(queryText, types, requiredTagIds, targetK)).stream()
                        .map(h -> new CandidateHit(h.vaultEntryId(), h.entryType(), domain, h.primaryText(), h.secondaryText(), h.snippet(), SearchMatchKind.valueOf(h.matchKind()), h.rankBucket(), h.similarity(), false))
                        .toList();
                case COLLECTION -> collectionSearchOperations.search(new CollectionSearchQuery(queryText, types, requiredTagIds, targetK)).stream()
                        .map(h -> new CandidateHit(h.vaultEntryId(), h.entryType(), domain, h.primaryText(), h.secondaryText(), h.snippet(), SearchMatchKind.valueOf(h.matchKind()), h.rankBucket(), h.similarity(), false))
                        .toList();
                case ACCOUNT -> accountSearchOperations.search(new AccountSearchQuery(queryText, types, requiredTagIds, targetK)).stream()
                        .map(h -> new CandidateHit(h.vaultEntryId(), h.entryType(), domain, h.primaryText(), h.secondaryText(), h.snippet(), SearchMatchKind.valueOf(h.matchKind()), h.rankBucket(), h.similarity(), false))
                        .toList();
                case FEED -> feedSearchOperations.search(new FeedSearchQuery(queryText, types, requiredTagIds, targetK)).stream()
                        .map(h -> new CandidateHit(h.vaultEntryId(), h.entryType(), domain, h.primaryText(), h.secondaryText(), h.snippet(), SearchMatchKind.valueOf(h.matchKind()), h.rankBucket(), h.similarity(), false))
                        .toList();
            };

            candidateHits.addAll(domainHits);
        }

        // 2. Fan out to Vault tag search
        Set<VaultEntryType> taggableTypes = new HashSet<>(allEffectiveTypes);
        taggableTypes.remove(VaultEntryType.FILM_CREDIT);

        if (!taggableTypes.isEmpty()) {
            List<VaultTagCandidateHit> tagHits = vaultSearchOperations.searchByTag(
                    new VaultTagSearchQuery(queryText, taggableTypes, requiredTagIds, targetK));
            for (VaultTagCandidateHit th : tagHits) {
                SearchDomain domain = SearchDomain.forEntryType(th.entryType()).orElseThrow();
                String snippet = th.matchedTagName() != null ? "Tag: " + th.matchedTagName() : null;
                candidateHits.add(new CandidateHit(
                        th.vaultEntryId(),
                        th.entryType(),
                        domain,
                        null,
                        null,
                        snippet,
                        SearchMatchKind.TAG,
                        300,
                        th.similarity(),
                        true
                ));
            }
        }

        // 3. Deduplicate by vaultEntryId: keep the stronger hit
        Map<Long, CandidateHit> deduplicatedMap = new HashMap<>();
        for (CandidateHit hit : candidateHits) {
            deduplicatedMap.merge(hit.vaultEntryId(), hit, (existing, incoming) -> {
                if (incoming.rankBucket() > existing.rankBucket()) {
                    return incoming;
                } else if (incoming.rankBucket() < existing.rankBucket()) {
                    return existing;
                } else {
                    if (incoming.similarity() > existing.similarity()) {
                        return incoming;
                    } else if (incoming.similarity() < existing.similarity()) {
                        return existing;
                    } else {
                        // Exactly equal score: prefer text-origin hit
                        return existing.isTagOrigin() ? incoming : existing;
                    }
                }
            });
        }

        List<CandidateHit> sortedCandidates = new ArrayList<>(deduplicatedMap.values());
        sortedCandidates.sort(GLOBAL_COMPARATOR);

        // 4. Bulk document materialization for tag-origin hits that made it into the top K
        int topKCount = Math.min(sortedCandidates.size(), K);
        Map<SearchDomain, Set<Long>> tagIdsToMaterialize = new EnumMap<>(SearchDomain.class);

        for (int i = 0; i < topKCount; i++) {
            CandidateHit c = sortedCandidates.get(i);
            if (c.isTagOrigin() && c.primaryText() == null) {
                tagIdsToMaterialize.computeIfAbsent(c.domain(), d -> new HashSet<>()).add(c.vaultEntryId());
            }
        }

        Map<Long, MaterializedDoc> materializedDocs = new HashMap<>();
        for (Map.Entry<SearchDomain, Set<Long>> entry : tagIdsToMaterialize.entrySet()) {
            SearchDomain domain = entry.getKey();
            Set<Long> ids = entry.getValue();

            switch (domain) {
                case PEOPLE -> peopleSearchOperations.lookupDocuments(ids).forEach((id, doc) ->
                        materializedDocs.put(id, new MaterializedDoc(doc.primaryText(), doc.secondaryText())));
                case FICTION -> fictionSearchOperations.lookupDocuments(ids).forEach((id, doc) ->
                        materializedDocs.put(id, new MaterializedDoc(doc.primaryText(), doc.secondaryText())));
                case FILM -> filmSearchOperations.lookupDocuments(ids).forEach((id, doc) ->
                        materializedDocs.put(id, new MaterializedDoc(doc.primaryText(), doc.secondaryText())));
                case MEDIA -> mediaSearchOperations.lookupDocuments(ids).forEach((id, doc) ->
                        materializedDocs.put(id, new MaterializedDoc(doc.primaryText(), doc.secondaryText())));
                case LOCATION -> locationSearchOperations.lookupDocuments(ids).forEach((id, doc) ->
                        materializedDocs.put(id, new MaterializedDoc(doc.primaryText(), doc.secondaryText())));
                case KNOWLEDGE -> knowledgeSearchOperations.lookupDocuments(ids).forEach((id, doc) ->
                        materializedDocs.put(id, new MaterializedDoc(doc.primaryText(), doc.secondaryText())));
                case COLLECTION -> collectionSearchOperations.lookupDocuments(ids).forEach((id, doc) ->
                        materializedDocs.put(id, new MaterializedDoc(doc.primaryText(), doc.secondaryText())));
                case ACCOUNT -> accountSearchOperations.lookupDocuments(ids).forEach((id, doc) ->
                        materializedDocs.put(id, new MaterializedDoc(doc.primaryText(), doc.secondaryText())));
                case FEED -> feedSearchOperations.lookupDocuments(ids).forEach((id, doc) ->
                        materializedDocs.put(id, new MaterializedDoc(doc.primaryText(), doc.secondaryText())));
            }

            for (Long id : ids) {
                if (!materializedDocs.containsKey(id)) {
                    throw new IllegalStateException("Cannot materialize active Vault entry " + id + " in module " + domain);
                }
            }
        }

        // Apply materialized text to candidates
        for (int i = 0; i < topKCount; i++) {
            CandidateHit c = sortedCandidates.get(i);
            if (c.isTagOrigin() && c.primaryText() == null) {
                MaterializedDoc doc = materializedDocs.get(c.vaultEntryId());
                if (doc != null) {
                    sortedCandidates.set(i, c.withMaterializedText(doc.primaryText(), doc.secondaryText()));
                }
            }
        }

        // 5. Slice and compute hasMore
        boolean hasMore = sortedCandidates.size() > K;
        int start = Math.min(offset, sortedCandidates.size());
        int end = Math.min(offset + limit, sortedCandidates.size());

        List<GlobalSearchResult> pageItems = sortedCandidates.subList(start, end).stream()
                .map(CandidateHit::toGlobalSearchResult)
                .toList();

        return new GlobalSearchPage(offset, limit, pageItems, hasMore);
    }

    private record MaterializedDoc(String primaryText, String secondaryText) {}

    private record CandidateHit(
            Long vaultEntryId,
            VaultEntryType entryType,
            SearchDomain domain,
            String primaryText,
            String secondaryText,
            String snippet,
            SearchMatchKind matchKind,
            int rankBucket,
            double similarity,
            boolean isTagOrigin
    ) {
        CandidateHit withMaterializedText(String newPrimaryText, String newSecondaryText) {
            return new CandidateHit(
                    vaultEntryId,
                    entryType,
                    domain,
                    newPrimaryText,
                    newSecondaryText,
                    snippet,
                    matchKind,
                    rankBucket,
                    similarity,
                    isTagOrigin
            );
        }

        GlobalSearchResult toGlobalSearchResult() {
            return new GlobalSearchResult(
                    vaultEntryId,
                    entryType,
                    domain,
                    primaryText != null ? primaryText : "",
                    secondaryText,
                    snippet,
                    matchKind,
                    rankBucket,
                    similarity
            );
        }
    }
}

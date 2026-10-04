package com.vhvkhangg.personalprivatevault.collection.internal.application.search;

import com.vhvkhangg.personalprivatevault.collection.music.search.MusicSearchOperations;
import com.vhvkhangg.personalprivatevault.collection.search.CollectionSearchDocument;
import com.vhvkhangg.personalprivatevault.collection.search.CollectionSearchHit;
import com.vhvkhangg.personalprivatevault.collection.search.CollectionSearchOperations;
import com.vhvkhangg.personalprivatevault.collection.search.CollectionSearchQuery;
import com.vhvkhangg.personalprivatevault.collection.shopping.search.ShoppingSearchOperations;
import com.vhvkhangg.personalprivatevault.collection.software.search.SoftwareSearchOperations;
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
public class CollectionSearchService implements CollectionSearchOperations {

    private final MusicSearchOperations musicSearchOperations;
    private final ShoppingSearchOperations shoppingSearchOperations;
    private final SoftwareSearchOperations softwareSearchOperations;

    private static final Comparator<CollectionSearchHit> HIT_COMPARATOR =
            Comparator.comparingInt(CollectionSearchHit::rankBucket).reversed()
                    .thenComparing(Comparator.comparingDouble(CollectionSearchHit::similarity).reversed())
                    .thenComparing(h -> h.entryType().name())
                    .thenComparing(CollectionSearchHit::vaultEntryId);

    @Override
    public List<CollectionSearchHit> search(CollectionSearchQuery query) {
        if (query == null) {
            throw new IllegalArgumentException("Query must not be null");
        }
        Set<VaultEntryType> types = query.entryTypes();
        List<CollectionSearchHit> allHits = new ArrayList<>();

        if (types.isEmpty() || types.contains(VaultEntryType.MUSIC)) {
            musicSearchOperations.search(query.query(), query.requiredTagIds(), query.limit())
                    .forEach(h -> allHits.add(new CollectionSearchHit(
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

        if (types.isEmpty() || types.contains(VaultEntryType.SHOPPING)) {
            shoppingSearchOperations.search(query.query(), query.requiredTagIds(), query.limit())
                    .forEach(h -> allHits.add(new CollectionSearchHit(
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

        if (types.isEmpty() || types.contains(VaultEntryType.SOFTWARE)) {
            softwareSearchOperations.search(query.query(), query.requiredTagIds(), query.limit())
                    .forEach(h -> allHits.add(new CollectionSearchHit(
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
    public Map<Long, CollectionSearchDocument> lookupDocuments(Set<Long> vaultEntryIds) {
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
        Map<Long, CollectionSearchDocument> combined = new HashMap<>();

        musicSearchOperations.lookupDocuments(vaultEntryIds).forEach((id, doc) ->
                combined.put(id, new CollectionSearchDocument(doc.vaultEntryId(), doc.entryType(), doc.primaryText(), doc.secondaryText())));

        shoppingSearchOperations.lookupDocuments(vaultEntryIds).forEach((id, doc) ->
                combined.put(id, new CollectionSearchDocument(doc.vaultEntryId(), doc.entryType(), doc.primaryText(), doc.secondaryText())));

        softwareSearchOperations.lookupDocuments(vaultEntryIds).forEach((id, doc) ->
                combined.put(id, new CollectionSearchDocument(doc.vaultEntryId(), doc.entryType(), doc.primaryText(), doc.secondaryText())));

        return combined;
    }
}

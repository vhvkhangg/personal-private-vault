package com.vhvkhangg.personalprivatevault.collection.api;

import java.util.List;
import java.util.Optional;

/**
 * Public parent collection facade interface exposing capability-oriented operations
 * across the nested {@code music}, {@code shopping}, and {@code software} modules.
 */
public interface CollectionOperations {

    // Music
    CollectionMusicView createMusic(CreateCollectionMusicCommand command);

    CollectionMusicView updateMusic(Long id, UpdateCollectionMusicCommand command);

    Optional<CollectionMusicView> findMusicById(Long id);

    void addMusicCredit(Long musicId, Long personId, CollectionMusicCreditRole role);

    List<CollectionMusicCreditView> findMusicCredits(Long musicId, int limit);

    // Shopping
    CollectionShoppingItemView createShoppingItem(CreateCollectionShoppingItemCommand command);

    CollectionShoppingItemView updateShoppingItem(Long id, UpdateCollectionShoppingItemCommand command);

    Optional<CollectionShoppingItemView> findShoppingItemById(Long id);

    // Software
    CollectionSoftwareItemView createSoftwareItem(CreateCollectionSoftwareItemCommand command);

    CollectionSoftwareItemView updateSoftwareItem(Long id, UpdateCollectionSoftwareItemCommand command);

    Optional<CollectionSoftwareItemView> findSoftwareItemById(Long id);

    void addSoftwarePlatform(Long softwareId, Long platformId);

    List<CollectionSoftwarePlatformView> findSoftwarePlatforms(Long softwareId, int limit);
}

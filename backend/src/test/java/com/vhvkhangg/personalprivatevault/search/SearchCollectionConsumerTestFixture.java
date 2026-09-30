package com.vhvkhangg.personalprivatevault.search;

import com.vhvkhangg.personalprivatevault.collection.api.CollectionMusicView;
import com.vhvkhangg.personalprivatevault.collection.api.CollectionOperations;
import com.vhvkhangg.personalprivatevault.collection.api.CollectionShoppingItemView;
import com.vhvkhangg.personalprivatevault.collection.api.CollectionSoftwareItemView;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Representative external consumer test fixture in the future {@code search} module
 * demonstrating that only {@code collection.api.*} types can be resolved and accessed
 * across the module boundary according to Spring Modulith encapsulation rules.
 */
@Component
public class SearchCollectionConsumerTestFixture {

    private final CollectionOperations collectionOperations;

    public SearchCollectionConsumerTestFixture(CollectionOperations collectionOperations) {
        this.collectionOperations = collectionOperations;
    }

    public Optional<CollectionMusicView> findMusic(Long id) {
        return collectionOperations.findMusicById(id);
    }

    public Optional<CollectionShoppingItemView> findShoppingItem(Long id) {
        return collectionOperations.findShoppingItemById(id);
    }

    public Optional<CollectionSoftwareItemView> findSoftwareItem(Long id) {
        return collectionOperations.findSoftwareItemById(id);
    }
}

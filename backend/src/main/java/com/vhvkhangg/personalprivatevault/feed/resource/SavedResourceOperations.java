package com.vhvkhangg.personalprivatevault.feed.resource;

import com.vhvkhangg.personalprivatevault.feed.resource.command.CreateFeedSavedResourceCommand;
import com.vhvkhangg.personalprivatevault.feed.resource.command.CreateManualSavedResourceCommand;
import com.vhvkhangg.personalprivatevault.feed.view.SavedResourceView;

import java.util.List;
import java.util.Optional;

/**
 * Public synchronous API for managing Vault-backed saved web resources.
 */
public interface SavedResourceOperations {

    SavedResourceView saveFeedItem(CreateFeedSavedResourceCommand command);

    SavedResourceView saveManual(CreateManualSavedResourceCommand command);

    Optional<SavedResourceView> findSavedResourceById(Long id);

    Optional<SavedResourceView> findSavedResourceByUrlHash(String resourceUrlHash);

    List<SavedResourceView> findRecentSavedResources(int limit);
}

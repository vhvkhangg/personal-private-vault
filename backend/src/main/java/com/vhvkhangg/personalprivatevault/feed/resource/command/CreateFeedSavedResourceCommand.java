package com.vhvkhangg.personalprivatevault.feed.resource.command;

import com.vhvkhangg.personalprivatevault.feed.enums.SavedResourceKind;

/**
 * Command for saving an existing feed item into a Vault-backed saved resource.
 */
public record CreateFeedSavedResourceCommand(
        Long feedItemId,
        SavedResourceKind kind
) {
}

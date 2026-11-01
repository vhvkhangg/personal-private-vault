package com.vhvkhangg.personalprivatevault.feed.view;

import java.util.List;

/**
 * Immutable view model for a paged Universal Inbox of saved resources.
 */
public record SavedResourceInboxPageView(
        List<SavedResourceView> items,
        String nextCursor
) {
    public SavedResourceInboxPageView {
        items = items != null ? List.copyOf(items) : List.of();
    }
}

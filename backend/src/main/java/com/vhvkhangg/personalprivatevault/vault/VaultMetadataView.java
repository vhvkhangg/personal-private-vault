package com.vhvkhangg.personalprivatevault.vault;

import java.util.List;

/** Public global-metadata read model. {@code rating} is nullable when the entry is unrated. */
public record VaultMetadataView(boolean favorite, RatingGrade rating, List<TagView> tags) {
    public VaultMetadataView {
        tags = List.copyOf(tags);
    }
}

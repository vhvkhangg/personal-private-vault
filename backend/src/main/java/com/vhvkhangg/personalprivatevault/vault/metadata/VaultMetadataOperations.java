package com.vhvkhangg.personalprivatevault.vault.metadata;

import com.vhvkhangg.personalprivatevault.vault.enums.RatingGrade;
import com.vhvkhangg.personalprivatevault.vault.view.TagView;
import com.vhvkhangg.personalprivatevault.vault.view.VaultMetadataView;

/** Public synchronous API for favorites, ratings, and global tags. */
public interface VaultMetadataOperations {
    VaultMetadataView metadata(Long vaultEntryId);
    void favorite(Long vaultEntryId);
    void unfavorite(Long vaultEntryId);
    void setRating(Long vaultEntryId, RatingGrade grade);
    void removeRating(Long vaultEntryId);
    TagView createTag(String name);
    void attachTag(Long vaultEntryId, Long tagId);
    void detachTag(Long vaultEntryId, Long tagId);
}

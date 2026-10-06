package com.vhvkhangg.personalprivatevault.vault.internal.web.mapper;

import com.vhvkhangg.personalprivatevault.vault.internal.web.dto.TagResponse;
import com.vhvkhangg.personalprivatevault.vault.internal.web.dto.VaultEntryResponse;
import com.vhvkhangg.personalprivatevault.vault.internal.web.dto.VaultMetadataResponse;
import com.vhvkhangg.personalprivatevault.vault.view.TagView;
import com.vhvkhangg.personalprivatevault.vault.view.VaultEntryView;
import com.vhvkhangg.personalprivatevault.vault.view.VaultMetadataView;

import java.util.List;

public final class VaultWebMapper {

    private VaultWebMapper() {}

    public static VaultEntryResponse toResponse(VaultEntryView view) {
        if (view == null) return null;
        return new VaultEntryResponse(
                view.id(),
                view.entryType(),
                view.createdAt(),
                view.updatedAt(),
                view.deletedAt()
        );
    }

    public static TagResponse toResponse(TagView view) {
        if (view == null) return null;
        return new TagResponse(view.id(), view.name(), view.createdAt());
    }

    public static VaultMetadataResponse toResponse(VaultMetadataView view) {
        if (view == null) return null;
        List<TagResponse> tags = view.tags() != null
                ? view.tags().stream().map(VaultWebMapper::toResponse).toList()
                : List.of();
        return new VaultMetadataResponse(view.favorite(), view.rating(), tags);
    }
}

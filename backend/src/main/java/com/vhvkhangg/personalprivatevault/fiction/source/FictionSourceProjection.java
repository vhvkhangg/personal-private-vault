package com.vhvkhangg.personalprivatevault.fiction.source;

import com.vhvkhangg.personalprivatevault.fiction.enums.ConsumptionStatus;
import com.vhvkhangg.personalprivatevault.fiction.enums.FictionFormat;
import com.vhvkhangg.personalprivatevault.fiction.enums.ProgressStatus;
import com.vhvkhangg.personalprivatevault.vault.enums.VaultEntryType;

import java.time.Instant;
import java.util.Objects;
import java.util.Set;

/**
 * Authoritative, bounded RAG source projection containing only P17-2 approved fields
 * and fresh classification/revision proof.
 */
public record FictionSourceProjection(
        Long id,
        String title,
        String originalTitle,
        FictionFormat format,
        String nationalityCode,
        Long genreId,
        Set<Long> storyArchetypeIds,
        Set<Long> worldSettingIds,
        ProgressStatus progressStatus,
        ConsumptionStatus consumptionStatus,
        Integer totalChapters,
        String description,
        boolean isNsfw,
        Instant deletedAt,
        VaultEntryType entryType,
        String contentRevisionHash,
        String descriptionRevisionHash
) {
    public FictionSourceProjection {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(title, "title must not be null");
        Objects.requireNonNull(format, "format must not be null");
        Objects.requireNonNull(storyArchetypeIds, "storyArchetypeIds must not be null");
        Objects.requireNonNull(worldSettingIds, "worldSettingIds must not be null");
        Objects.requireNonNull(progressStatus, "progressStatus must not be null");
        Objects.requireNonNull(consumptionStatus, "consumptionStatus must not be null");
        Objects.requireNonNull(entryType, "entryType must not be null");
        Objects.requireNonNull(contentRevisionHash, "contentRevisionHash must not be null");
        Objects.requireNonNull(descriptionRevisionHash, "descriptionRevisionHash must not be null");
        storyArchetypeIds=Set.copyOf(storyArchetypeIds);
        worldSettingIds=Set.copyOf(worldSettingIds);
    }

    public boolean isEligible() {
        return !isNsfw && deletedAt == null && entryType == VaultEntryType.FICTION;
    }
}

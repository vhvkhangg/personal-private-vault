package com.vhvkhangg.personalprivatevault.film.source;

import com.vhvkhangg.personalprivatevault.film.enums.ConsumptionStatus;
import com.vhvkhangg.personalprivatevault.film.enums.FilmFormat;
import com.vhvkhangg.personalprivatevault.film.enums.FilmProductionStyle;
import com.vhvkhangg.personalprivatevault.film.enums.ProgressStatus;
import com.vhvkhangg.personalprivatevault.vault.enums.VaultEntryType;

import java.time.Instant;
import java.util.Objects;
import java.util.Set;

/**
 * Authoritative, bounded RAG source projection containing only P17-2 approved fields
 * and fresh classification/revision proof for films.
 */
public record FilmSourceProjection(
        Long id,
        String title,
        String originalTitle,
        FilmFormat format,
        FilmProductionStyle productionStyle,
        String nationalityCode,
        Set<Long> genreIds,
        Set<Long> storyArchetypeIds,
        Set<Long> worldSettingIds,
        ProgressStatus progressStatus,
        ConsumptionStatus consumptionStatus,
        Integer totalEpisodes,
        String description,
        boolean isNsfw,
        Instant deletedAt,
        VaultEntryType entryType,
        String contentRevisionHash,
        String descriptionRevisionHash
) {
    public FilmSourceProjection {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(title, "title must not be null");
        Objects.requireNonNull(format, "format must not be null");
        Objects.requireNonNull(productionStyle, "productionStyle must not be null");
        Objects.requireNonNull(genreIds, "genreIds must not be null");
        Objects.requireNonNull(storyArchetypeIds, "storyArchetypeIds must not be null");
        Objects.requireNonNull(worldSettingIds, "worldSettingIds must not be null");
        Objects.requireNonNull(progressStatus, "progressStatus must not be null");
        Objects.requireNonNull(consumptionStatus, "consumptionStatus must not be null");
        Objects.requireNonNull(entryType, "entryType must not be null");
        Objects.requireNonNull(contentRevisionHash, "contentRevisionHash must not be null");
        Objects.requireNonNull(descriptionRevisionHash, "descriptionRevisionHash must not be null");
        genreIds=Set.copyOf(genreIds);
        storyArchetypeIds=Set.copyOf(storyArchetypeIds);
        worldSettingIds=Set.copyOf(worldSettingIds);
    }

    public boolean isEligible() {
        return !isNsfw && deletedAt == null && entryType == VaultEntryType.FILM;
    }
}

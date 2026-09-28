package com.vhvkhangg.personalprivatevault.vault;

import com.vhvkhangg.personalprivatevault.vault.enums.VaultEntryType;

import com.vhvkhangg.personalprivatevault.vault.internal.domain.VaultCapabilityMatrix;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VaultCapabilityMatrixTest {

    @ParameterizedTest
    @EnumSource(VaultEntryType.class)
    @DisplayName("Every current VaultEntryType is favoriteable")
    void everyTypeIsFavoriteable(VaultEntryType type) {
        assertThat(VaultCapabilityMatrix.canFavorite(type)).isTrue();
        assertThatCode(() -> VaultCapabilityMatrix.assertCanFavorite(type)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("FILM_CREDIT is favorite-only and forbids rating and tags")
    void filmCreditIsFavoriteOnly() {
        assertThat(VaultCapabilityMatrix.canFavorite(VaultEntryType.FILM_CREDIT)).isTrue();
        assertThat(VaultCapabilityMatrix.canRate(VaultEntryType.FILM_CREDIT)).isFalse();
        assertThat(VaultCapabilityMatrix.canTag(VaultEntryType.FILM_CREDIT)).isFalse();

        assertThatThrownBy(() -> VaultCapabilityMatrix.assertCanRate(VaultEntryType.FILM_CREDIT))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("does not support ratings");

        assertThatThrownBy(() -> VaultCapabilityMatrix.assertCanTag(VaultEntryType.FILM_CREDIT))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("does not support tags");
    }

    @Test
    @DisplayName("BRAND explicitly supports global rating and tags")
    void brandSupportsRatingAndTags() {
        assertThat(VaultCapabilityMatrix.canFavorite(VaultEntryType.BRAND)).isTrue();
        assertThat(VaultCapabilityMatrix.canRate(VaultEntryType.BRAND)).isTrue();
        assertThat(VaultCapabilityMatrix.canTag(VaultEntryType.BRAND)).isTrue();

        assertThatCode(() -> VaultCapabilityMatrix.assertCanRate(VaultEntryType.BRAND)).doesNotThrowAnyException();
        assertThatCode(() -> VaultCapabilityMatrix.assertCanTag(VaultEntryType.BRAND)).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @EnumSource(value = VaultEntryType.class, names = {"FILM_CREDIT"}, mode = EnumSource.Mode.EXCLUDE)
    @DisplayName("Every current type except FILM_CREDIT is rateable and taggable")
    void nonFilmCreditTypesAreRateableAndTaggable(VaultEntryType type) {
        assertThat(VaultCapabilityMatrix.canRate(type)).isTrue();
        assertThat(VaultCapabilityMatrix.canTag(type)).isTrue();

        assertThatCode(() -> VaultCapabilityMatrix.assertCanRate(type)).doesNotThrowAnyException();
        assertThatCode(() -> VaultCapabilityMatrix.assertCanTag(type)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Null arguments throw IllegalArgumentException (fail closed)")
    void nullTypeThrowsIllegalArgumentException() {
        assertThatThrownBy(() -> VaultCapabilityMatrix.canFavorite(null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> VaultCapabilityMatrix.canRate(null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> VaultCapabilityMatrix.canTag(null))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> VaultCapabilityMatrix.assertCanFavorite(null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> VaultCapabilityMatrix.assertCanRate(null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> VaultCapabilityMatrix.assertCanTag(null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

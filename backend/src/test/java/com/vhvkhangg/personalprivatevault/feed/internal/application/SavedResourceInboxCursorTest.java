package com.vhvkhangg.personalprivatevault.feed.internal.application;

import com.vhvkhangg.personalprivatevault.feed.enums.SavedResourceKind;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SavedResourceInboxCursorTest {

    @Test
    @DisplayName("Null cursor string decodes to null for first page")
    void nullCursorDecodesToNull() {
        assertThat(SavedResourceInboxCursor.decode(null, null)).isNull();
        assertThat(SavedResourceInboxCursor.decode(null, SavedResourceKind.ARTICLE)).isNull();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "\t", "\n"})
    @DisplayName("Blank cursor string throws IllegalArgumentException")
    void blankCursorThrows(String blankCursor) {
        assertThatThrownBy(() -> SavedResourceInboxCursor.decode(blankCursor, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must not be blank");
    }

    @Test
    @DisplayName("Cursor exceeding 512 characters before decoding throws IllegalArgumentException")
    void cursorExceedingMaxLengthThrows() {
        String longCursor = "a".repeat(513);
        assertThatThrownBy(() -> SavedResourceInboxCursor.decode(longCursor, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("exceeds maximum allowed");
    }

    @Test
    @DisplayName("Round-trip encode and decode for all-kinds cursor preserves microsecond precision")
    void roundTripAllKindsPreservesPrecision() {
        Instant now = Instant.parse("2026-11-01T14:30:15.123456Z");
        long id = 42L;

        String encoded = SavedResourceInboxCursor.encode(now, id, null);
        assertThat(encoded).isNotBlank();
        assertThat(encoded.length()).isLessThanOrEqualTo(512);

        SavedResourceInboxCursor decoded = SavedResourceInboxCursor.decode(encoded, null);
        assertThat(decoded).isNotNull();
        assertThat(decoded.savedAt()).isEqualTo(now);
        assertThat(decoded.id()).isEqualTo(id);
        assertThat(decoded.kind()).isNull();
    }

    @ParameterizedTest
    @EnumSource(SavedResourceKind.class)
    @DisplayName("Round-trip encode and decode preserves each SavedResourceKind")
    void roundTripSpecificKinds(SavedResourceKind kind) {
        Instant now = Instant.parse("2026-10-25T08:00:00.999999Z");
        long id = 1001L;

        String encoded = SavedResourceInboxCursor.encode(now, id, kind);
        SavedResourceInboxCursor decoded = SavedResourceInboxCursor.decode(encoded, kind);

        assertThat(decoded).isNotNull();
        assertThat(decoded.savedAt()).isEqualTo(now);
        assertThat(decoded.id()).isEqualTo(id);
        assertThat(decoded.kind()).isEqualTo(kind);
    }

    @Test
    @DisplayName("Filter mismatch: ARTICLE cursor reused for OTHER throws IllegalArgumentException")
    void filterMismatchThrowsForDifferentKind() {
        Instant now = Instant.parse("2026-11-01T12:00:00Z");
        String articleCursor = SavedResourceInboxCursor.encode(now, 1L, SavedResourceKind.ARTICLE);

        assertThatThrownBy(() -> SavedResourceInboxCursor.decode(articleCursor, SavedResourceKind.OTHER))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cursor kind filter mismatch");
    }

    @Test
    @DisplayName("Filter mismatch: specific kind cursor reused for all kinds throws IllegalArgumentException")
    void filterMismatchSpecificKindReusedForAllKinds() {
        Instant now = Instant.parse("2026-11-01T12:00:00Z");
        String articleCursor = SavedResourceInboxCursor.encode(now, 1L, SavedResourceKind.ARTICLE);

        assertThatThrownBy(() -> SavedResourceInboxCursor.decode(articleCursor, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cursor kind filter mismatch");
    }

    @Test
    @DisplayName("Filter mismatch: all-kinds cursor reused for specific kind throws IllegalArgumentException")
    void filterMismatchAllKindsReusedForSpecificKind() {
        Instant now = Instant.parse("2026-11-01T12:00:00Z");
        String allKindsCursor = SavedResourceInboxCursor.encode(now, 1L, null);

        assertThatThrownBy(() -> SavedResourceInboxCursor.decode(allKindsCursor, SavedResourceKind.ARTICLE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cursor kind filter mismatch");
    }

    @Test
    @DisplayName("Invalid Base64url encoding throws IllegalArgumentException")
    void invalidBase64Throws() {
        assertThatThrownBy(() -> SavedResourceInboxCursor.decode("%%%NotValidBase64%%%", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Malformed cursor encoding");
    }

    @Test
    @DisplayName("Unsupported cursor version throws IllegalArgumentException")
    void unsupportedVersionThrows() {
        String payload = "v2|ALL|2026-11-01T12:00:00Z|1";
        String encoded = Base64.getUrlEncoder().withoutPadding().encodeToString(payload.getBytes(StandardCharsets.UTF_8));

        assertThatThrownBy(() -> SavedResourceInboxCursor.decode(encoded, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported cursor version");
    }

    @Test
    @DisplayName("Missing fields or extra fields throw IllegalArgumentException")
    void invalidFieldCountThrows() {
        String tooFew = Base64.getUrlEncoder().withoutPadding().encodeToString("v1|ALL|2026-11-01T12:00:00Z".getBytes(StandardCharsets.UTF_8));
        assertThatThrownBy(() -> SavedResourceInboxCursor.decode(tooFew, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Malformed cursor format");

        String tooMany = Base64.getUrlEncoder().withoutPadding().encodeToString("v1|ALL|2026-11-01T12:00:00Z|1|extra".getBytes(StandardCharsets.UTF_8));
        assertThatThrownBy(() -> SavedResourceInboxCursor.decode(tooMany, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Malformed cursor format");
    }

    @Test
    @DisplayName("Non-numeric or overflow ID throws IllegalArgumentException")
    void invalidIdThrows() {
        String nonNumeric = Base64.getUrlEncoder().withoutPadding().encodeToString("v1|ALL|2026-11-01T12:00:00Z|abc".getBytes(StandardCharsets.UTF_8));
        assertThatThrownBy(() -> SavedResourceInboxCursor.decode(nonNumeric, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid cursor id");

        String overflow = Base64.getUrlEncoder().withoutPadding().encodeToString("v1|ALL|2026-11-01T12:00:00Z|999999999999999999999999".getBytes(StandardCharsets.UTF_8));
        assertThatThrownBy(() -> SavedResourceInboxCursor.decode(overflow, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid cursor id");

        String zeroId = Base64.getUrlEncoder().withoutPadding().encodeToString("v1|ALL|2026-11-01T12:00:00Z|0".getBytes(StandardCharsets.UTF_8));
        assertThatThrownBy(() -> SavedResourceInboxCursor.decode(zeroId, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cursor id must be positive");

        String negativeId = Base64.getUrlEncoder().withoutPadding().encodeToString("v1|ALL|2026-11-01T12:00:00Z|-5".getBytes(StandardCharsets.UTF_8));
        assertThatThrownBy(() -> SavedResourceInboxCursor.decode(negativeId, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cursor id must be positive");
    }

    @Test
    @DisplayName("Malformed timestamp string throws IllegalArgumentException")
    void malformedTimestampThrows() {
        String malformed = Base64.getUrlEncoder().withoutPadding().encodeToString("v1|ALL|not-a-date|1".getBytes(StandardCharsets.UTF_8));
        assertThatThrownBy(() -> SavedResourceInboxCursor.decode(malformed, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid cursor timestamp");
    }

    @Test
    @DisplayName("Timestamp out of supported SQL range throws IllegalArgumentException")
    void timestampOutOfSqlRangeThrows() {
        String futureOutOfRange = Base64.getUrlEncoder().withoutPadding().encodeToString("v1|ALL|+10000-01-01T00:00:00Z|1".getBytes(StandardCharsets.UTF_8));
        assertThatThrownBy(() -> SavedResourceInboxCursor.decode(futureOutOfRange, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("out of supported SQL range");

        String pastOutOfRange = Base64.getUrlEncoder().withoutPadding().encodeToString("v1|ALL|-0001-01-01T00:00:00Z|1".getBytes(StandardCharsets.UTF_8));
        assertThatThrownBy(() -> SavedResourceInboxCursor.decode(pastOutOfRange, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("out of supported SQL range");
    }

    @Test
    @DisplayName("Structurally valid edited seek tuple decodes successfully without anchor existence requirement")
    void editedSeekTupleDecodes() {
        Instant targetTime = Instant.parse("2025-06-15T10:30:00Z");
        long customId = 999999L;
        String validEditedPayload = "v1|ALL|" + targetTime + "|" + customId;
        String encoded = Base64.getUrlEncoder().withoutPadding().encodeToString(validEditedPayload.getBytes(StandardCharsets.UTF_8));

        SavedResourceInboxCursor decoded = SavedResourceInboxCursor.decode(encoded, null);
        assertThat(decoded).isNotNull();
        assertThat(decoded.savedAt()).isEqualTo(targetTime);
        assertThat(decoded.id()).isEqualTo(customId);
        assertThat(decoded.kind()).isNull();
    }
}

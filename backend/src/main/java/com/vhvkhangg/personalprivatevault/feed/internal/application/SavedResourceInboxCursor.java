package com.vhvkhangg.personalprivatevault.feed.internal.application;

import com.vhvkhangg.personalprivatevault.feed.enums.SavedResourceKind;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Base64;
import java.util.Objects;

/**
 * Owner-local cursor value object and Base64url codec for Universal Inbox keyset pagination.
 */
record SavedResourceInboxCursor(
        Instant savedAt,
        long id,
        SavedResourceKind kind
) {

    private static final String VERSION = "v1";
    private static final String ALL_KINDS_MARKER = "ALL";
    private static final int MAX_CURSOR_LENGTH = 512;
    private static final Instant MIN_SQL_TIMESTAMP = Instant.parse("0001-01-01T00:00:00Z");
    private static final Instant MAX_SQL_TIMESTAMP = Instant.parse("9999-12-31T23:59:59.999999999Z");

    SavedResourceInboxCursor {
        Objects.requireNonNull(savedAt, "savedAt must not be null");
        if (savedAt.isBefore(MIN_SQL_TIMESTAMP) || savedAt.isAfter(MAX_SQL_TIMESTAMP)) {
            throw new IllegalArgumentException("Cursor timestamp out of supported SQL range");
        }
        if (id <= 0) {
            throw new IllegalArgumentException("Cursor id must be positive");
        }
    }

    static String encode(Instant savedAt, long id, SavedResourceKind kind) {
        Objects.requireNonNull(savedAt, "savedAt must not be null");
        if (savedAt.isBefore(MIN_SQL_TIMESTAMP) || savedAt.isAfter(MAX_SQL_TIMESTAMP)) {
            throw new IllegalArgumentException("Timestamp out of supported SQL range");
        }
        if (id <= 0) {
            throw new IllegalArgumentException("ID must be positive");
        }

        String kindMarker = kind == null ? ALL_KINDS_MARKER : kind.name();
        String payload = VERSION + "|" + kindMarker + "|" + savedAt + "|" + id;
        return Base64.getUrlEncoder().withoutPadding().encodeToString(payload.getBytes(StandardCharsets.UTF_8));
    }

    static SavedResourceInboxCursor decode(String cursorString, SavedResourceKind expectedKind) {
        if (cursorString == null) {
            return null;
        }
        if (cursorString.isBlank()) {
            throw new IllegalArgumentException("Cursor must not be blank");
        }
        if (cursorString.length() > MAX_CURSOR_LENGTH) {
            throw new IllegalArgumentException("Cursor length exceeds maximum allowed");
        }

        byte[] decodedBytes;
        try {
            decodedBytes = Base64.getUrlDecoder().decode(cursorString);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Malformed cursor encoding", ex);
        }

        String payload = new String(decodedBytes, StandardCharsets.UTF_8);
        String[] parts = payload.split("\\|", -1);
        if (parts.length != 4) {
            throw new IllegalArgumentException("Malformed cursor format");
        }

        if (!VERSION.equals(parts[0])) {
            throw new IllegalArgumentException("Unsupported cursor version");
        }

        String kindMarker = parts[1];
        if (expectedKind == null) {
            if (!ALL_KINDS_MARKER.equals(kindMarker)) {
                throw new IllegalArgumentException("Cursor kind filter mismatch");
            }
        } else {
            if (!expectedKind.name().equals(kindMarker)) {
                throw new IllegalArgumentException("Cursor kind filter mismatch");
            }
        }

        Instant savedAt;
        try {
            savedAt = Instant.parse(parts[2]);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Invalid cursor timestamp", ex);
        }

        long id;
        try {
            id = Long.parseLong(parts[3]);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid cursor id", ex);
        }

        return new SavedResourceInboxCursor(savedAt, id, expectedKind);
    }
}

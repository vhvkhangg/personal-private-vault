package com.vhvkhangg.personalprivatevault.rag.internal.domain;

import com.vhvkhangg.personalprivatevault.rag.source.RagSourceType;

import java.util.Set;

/**
 * P17-2 source and field eligibility allowlists.
 * Enforces strict per-type vocabulary and rejects unlisted, private, or unknown fields.
 */
public final class RagFieldAllowlist {

    public static final String FIELD_DESCRIPTION = "description";

    private static final Set<String> FICTION_ALLOWED_FIELDS = Set.of(
            "title", "originalTitle", "format", "nationalityCode", "genreId",
            "storyArchetypeIds", "worldSettingIds", "progressStatus", "consumptionStatus",
            "totalChapters", FIELD_DESCRIPTION
    );

    private static final Set<String> FILM_ALLOWED_FIELDS = Set.of(
            "title", "originalTitle", "format", "productionStyle", "nationalityCode",
            "genreIds", "storyArchetypeIds", "worldSettingIds", "progressStatus",
            "consumptionStatus", "totalEpisodes", FIELD_DESCRIPTION
    );

    private static final Set<String> EXTERNAL_FILE_ALLOWED_FIELDS = Set.of(
            "content"
    );

    private RagFieldAllowlist() {}

    public static Set<String> getAllowedFields(RagSourceType type) {
        return switch (type) {
            case FICTION -> FICTION_ALLOWED_FIELDS;
            case FILM -> FILM_ALLOWED_FIELDS;
            case EXTERNAL_FILE -> EXTERNAL_FILE_ALLOWED_FIELDS;
        };
    }

    public static void validateApprovedFields(RagSourceType type, Set<String> requestedFields) {
        if (requestedFields == null || requestedFields.isEmpty()) {
            throw new IllegalArgumentException("Approved fields set must not be empty");
        }
        Set<String> allowed = getAllowedFields(type);
        for (String field : requestedFields) {
            if (field == null || field.isBlank()) {
                throw new IllegalArgumentException("Approved field must not be blank");
            }
            if (!allowed.contains(field)) {
                throw new IllegalArgumentException("Field '" + field + "' is not eligible for RAG approval on " + type);
            }
        }
    }
}

package com.vhvkhangg.personalprivatevault.account.internal.web.dto;

import com.vhvkhangg.personalprivatevault.account.enums.FollowStatus;
import com.vhvkhangg.personalprivatevault.account.enums.FollowerStatus;
import com.vhvkhangg.personalprivatevault.account.enums.RelationshipSource;
import io.swagger.v3.oas.annotations.media.Schema;

public record SetExternalAccountRelationshipRequest(
        FollowerStatus followerStatus,
        @Schema(defaultValue = "MANUAL", description = "Relationship source; defaults to MANUAL if omitted")
        RelationshipSource source,
        @Schema(defaultValue = "UNKNOWN", description = "Follow status; defaults to UNKNOWN if omitted")
        FollowStatus followStatus,
        Boolean hasLikedPost,
        @Schema(description = "Optional relationship note")
        String note
) {
    public SetExternalAccountRelationshipRequest {
        note = trimOrNull(note);
    }

    private static String trimOrNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}

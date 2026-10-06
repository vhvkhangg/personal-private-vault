package com.vhvkhangg.personalprivatevault.account.internal.web.dto;

import com.vhvkhangg.personalprivatevault.account.enums.FollowStatus;
import com.vhvkhangg.personalprivatevault.account.enums.FollowerStatus;
import com.vhvkhangg.personalprivatevault.account.enums.RelationshipSource;

import java.time.Instant;

public record ExternalAccountRelationshipResponse(
        Long id,
        Long ownerAccountId,
        Long targetAccountId,
        FollowerStatus followerStatus,
        RelationshipSource source,
        FollowStatus followStatus,
        boolean hasLikedPost,
        String note,
        Instant createdAt,
        Instant updatedAt
) {}

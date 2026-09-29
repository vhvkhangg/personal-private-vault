package com.vhvkhangg.personalprivatevault.account.view;

import com.vhvkhangg.personalprivatevault.account.enums.FollowStatus;
import com.vhvkhangg.personalprivatevault.account.enums.FollowerStatus;
import com.vhvkhangg.personalprivatevault.account.enums.RelationshipSource;

import java.time.Instant;

/**
 * Immutable view of an External Account Relationship between owner and target.
 */
public record ExternalAccountRelationshipView(
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
) {
}

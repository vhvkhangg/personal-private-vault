package com.vhvkhangg.personalprivatevault.account.relationship;

import com.vhvkhangg.personalprivatevault.account.enums.FollowStatus;
import com.vhvkhangg.personalprivatevault.account.enums.FollowerStatus;
import com.vhvkhangg.personalprivatevault.account.enums.RelationshipSource;

/**
 * Command to set (create or update) a relationship state between owner and target External Accounts.
 */
public record SetExternalAccountRelationshipCommand(
        Long ownerAccountId,
        Long targetAccountId,
        FollowerStatus followerStatus,
        RelationshipSource source,
        FollowStatus followStatus,
        Boolean hasLikedPost,
        String note
) {
}

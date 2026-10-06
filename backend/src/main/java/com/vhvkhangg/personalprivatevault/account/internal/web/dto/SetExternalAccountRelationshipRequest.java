package com.vhvkhangg.personalprivatevault.account.internal.web.dto;

import com.vhvkhangg.personalprivatevault.account.enums.FollowStatus;
import com.vhvkhangg.personalprivatevault.account.enums.FollowerStatus;
import com.vhvkhangg.personalprivatevault.account.enums.RelationshipSource;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SetExternalAccountRelationshipRequest(
        @NotNull FollowerStatus followerStatus,
        RelationshipSource source,
        @NotNull FollowStatus followStatus,
        Boolean hasLikedPost,
        @Size(max = 2048) String note
) {}

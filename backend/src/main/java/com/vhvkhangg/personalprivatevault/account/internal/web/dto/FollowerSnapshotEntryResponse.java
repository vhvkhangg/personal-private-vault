package com.vhvkhangg.personalprivatevault.account.internal.web.dto;

public record FollowerSnapshotEntryResponse(
        Long snapshotId,
        Long targetAccountId,
        String usernameSnapshot,
        String displayNameSnapshot,
        String externalIdSnapshot,
        String profileUrlSnapshot
) {}

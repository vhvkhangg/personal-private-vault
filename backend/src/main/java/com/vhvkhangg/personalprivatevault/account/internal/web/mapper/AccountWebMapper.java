package com.vhvkhangg.personalprivatevault.account.internal.web.mapper;

import com.vhvkhangg.personalprivatevault.account.account.CreateExternalAccountCommand;
import com.vhvkhangg.personalprivatevault.account.account.UpdateExternalAccountCommand;
import com.vhvkhangg.personalprivatevault.account.internal.web.dto.CreateExternalAccountRequest;
import com.vhvkhangg.personalprivatevault.account.internal.web.dto.CreateFollowerSnapshotEntryRequest;
import com.vhvkhangg.personalprivatevault.account.internal.web.dto.CreateFollowerSnapshotRequest;
import com.vhvkhangg.personalprivatevault.account.internal.web.dto.ExternalAccountRelationshipResponse;
import com.vhvkhangg.personalprivatevault.account.internal.web.dto.ExternalAccountResponse;
import com.vhvkhangg.personalprivatevault.account.internal.web.dto.FollowerSnapshotEntryResponse;
import com.vhvkhangg.personalprivatevault.account.internal.web.dto.FollowerSnapshotResponse;
import com.vhvkhangg.personalprivatevault.account.internal.web.dto.SetExternalAccountRelationshipRequest;
import com.vhvkhangg.personalprivatevault.account.internal.web.dto.UpdateExternalAccountRequest;
import com.vhvkhangg.personalprivatevault.account.relationship.SetExternalAccountRelationshipCommand;
import com.vhvkhangg.personalprivatevault.account.snapshot.CreateFollowerSnapshotCommand;
import com.vhvkhangg.personalprivatevault.account.snapshot.CreateFollowerSnapshotEntryCommand;
import com.vhvkhangg.personalprivatevault.account.view.ExternalAccountRelationshipView;
import com.vhvkhangg.personalprivatevault.account.view.ExternalAccountView;
import com.vhvkhangg.personalprivatevault.account.view.FollowerSnapshotEntryView;
import com.vhvkhangg.personalprivatevault.account.view.FollowerSnapshotView;

import java.util.List;

public final class AccountWebMapper {

    private AccountWebMapper() {}

    public static CreateExternalAccountCommand toCommand(CreateExternalAccountRequest request) {
        return new CreateExternalAccountCommand(
                request.platformId(),
                request.ownership(),
                request.accountType(),
                request.username(),
                request.externalId(),
                request.displayName(),
                request.avatarUrl(),
                request.bannerUrl(),
                request.profileDescription(),
                request.ownerName(),
                request.url(),
                request.notes()
        );
    }

    public static UpdateExternalAccountCommand toCommand(UpdateExternalAccountRequest request) {
        return new UpdateExternalAccountCommand(
                request.platformId(),
                request.ownership(),
                request.accountType(),
                request.username(),
                request.externalId(),
                request.displayName(),
                request.avatarUrl(),
                request.bannerUrl(),
                request.profileDescription(),
                request.ownerName(),
                request.url(),
                request.notes()
        );
    }

    public static ExternalAccountResponse toResponse(ExternalAccountView view) {
        return new ExternalAccountResponse(
                view.id(),
                view.platformId(),
                view.ownership(),
                view.accountType(),
                view.username(),
                view.externalId(),
                view.displayName(),
                view.avatarUrl(),
                view.bannerUrl(),
                view.profileDescription(),
                view.ownerName(),
                view.url(),
                view.notes()
        );
    }

    public static SetExternalAccountRelationshipCommand toCommand(
            Long ownerAccountId,
            Long targetAccountId,
            SetExternalAccountRelationshipRequest request) {
        return new SetExternalAccountRelationshipCommand(
                ownerAccountId,
                targetAccountId,
                request.followerStatus(),
                request.source(),
                request.followStatus(),
                request.hasLikedPost(),
                request.note()
        );
    }

    public static ExternalAccountRelationshipResponse toResponse(ExternalAccountRelationshipView view) {
        return new ExternalAccountRelationshipResponse(
                view.id(),
                view.ownerAccountId(),
                view.targetAccountId(),
                view.followerStatus(),
                view.source(),
                view.followStatus(),
                view.hasLikedPost(),
                view.note(),
                view.createdAt(),
                view.updatedAt()
        );
    }

    public static CreateFollowerSnapshotCommand toCommand(
            Long ownerAccountId,
            CreateFollowerSnapshotRequest request) {
        List<CreateFollowerSnapshotEntryCommand> entries = request.entries() != null
                ? request.entries().stream().map(AccountWebMapper::toEntryCommand).toList()
                : List.of();
        return new CreateFollowerSnapshotCommand(
                ownerAccountId,
                request.capturedAt(),
                request.source(),
                request.reportedTotalCount(),
                request.importedFileName(),
                entries
        );
    }

    public static CreateFollowerSnapshotEntryCommand toEntryCommand(CreateFollowerSnapshotEntryRequest request) {
        return new CreateFollowerSnapshotEntryCommand(
                request.targetAccountId(),
                request.usernameSnapshot(),
                request.displayNameSnapshot(),
                request.externalIdSnapshot(),
                request.profileUrlSnapshot()
        );
    }

    public static FollowerSnapshotResponse toResponse(FollowerSnapshotView view) {
        return new FollowerSnapshotResponse(
                view.id(),
                view.ownerAccountId(),
                view.capturedAt(),
                view.source(),
                view.reportedTotalCount(),
                view.importedFileName(),
                view.createdAt(),
                view.entryCount()
        );
    }

    public static FollowerSnapshotEntryResponse toResponse(FollowerSnapshotEntryView view) {
        return new FollowerSnapshotEntryResponse(
                view.snapshotId(),
                view.targetAccountId(),
                view.usernameSnapshot(),
                view.displayNameSnapshot(),
                view.externalIdSnapshot(),
                view.profileUrlSnapshot()
        );
    }
}

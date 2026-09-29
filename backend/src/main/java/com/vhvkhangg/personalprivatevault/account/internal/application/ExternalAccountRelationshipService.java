package com.vhvkhangg.personalprivatevault.account.internal.application;

import com.vhvkhangg.personalprivatevault.account.enums.FollowStatus;
import com.vhvkhangg.personalprivatevault.account.enums.RelationshipSource;
import com.vhvkhangg.personalprivatevault.account.internal.domain.ExternalAccountRelationship;
import com.vhvkhangg.personalprivatevault.account.internal.infrastructure.persistence.ExternalAccountRelationshipRepository;
import com.vhvkhangg.personalprivatevault.account.internal.infrastructure.persistence.ExternalAccountRepository;
import com.vhvkhangg.personalprivatevault.account.relationship.ExternalAccountRelationshipOperations;
import com.vhvkhangg.personalprivatevault.account.relationship.InvalidExternalAccountRelationshipException;
import com.vhvkhangg.personalprivatevault.account.relationship.SetExternalAccountRelationshipCommand;
import com.vhvkhangg.personalprivatevault.account.view.ExternalAccountRelationshipView;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ExternalAccountRelationshipService implements ExternalAccountRelationshipOperations {

    private final ExternalAccountRepository externalAccountRepository;
    private final ExternalAccountRelationshipRepository relationshipRepository;

    @Override
    @Transactional
    public ExternalAccountRelationshipView setRelationship(SetExternalAccountRelationshipCommand command) {
        if (command == null) {
            throw new InvalidExternalAccountRelationshipException("SetExternalAccountRelationshipCommand must not be null");
        }
        Long ownerId = command.ownerAccountId();
        Long targetId = command.targetAccountId();

        if (ownerId == null) {
            throw new InvalidExternalAccountRelationshipException("Owner account ID must not be null");
        }
        if (targetId == null) {
            throw new InvalidExternalAccountRelationshipException("Target account ID must not be null");
        }
        if (ownerId.equals(targetId)) {
            throw new InvalidExternalAccountRelationshipException("Owner account ID and target account ID must not be the same");
        }

        if (externalAccountRepository.findById(ownerId).isEmpty()) {
            throw new InvalidExternalAccountRelationshipException("Owner account with id " + ownerId + " does not exist");
        }
        if (externalAccountRepository.findById(targetId).isEmpty()) {
            throw new InvalidExternalAccountRelationshipException("Target account with id " + targetId + " does not exist");
        }

        RelationshipSource source = command.source() != null ? command.source() : RelationshipSource.MANUAL;
        FollowStatus followStatus = command.followStatus() != null ? command.followStatus() : FollowStatus.UNKNOWN;
        boolean hasLikedPost = command.hasLikedPost() != null && command.hasLikedPost();

        relationshipRepository.upsert(
                ownerId,
                targetId,
                command.followerStatus() != null ? command.followerStatus().name() : null,
                source.name(),
                followStatus.name(),
                hasLikedPost,
                trimOrNull(command.note())
        );

        return relationshipRepository.findByOwnerAccountIdAndTargetAccountId(ownerId, targetId)
                .map(this::toView)
                .orElseThrow(() -> new IllegalStateException("Relationship should exist after upsert"));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ExternalAccountRelationshipView> findByPair(Long ownerAccountId, Long targetAccountId) {
        if (ownerAccountId == null || targetAccountId == null) {
            return Optional.empty();
        }
        return relationshipRepository.findByOwnerAccountIdAndTargetAccountId(ownerAccountId, targetAccountId)
                .map(this::toView);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExternalAccountRelationshipView> findRecentByOwner(Long ownerAccountId, int limit) {
        if (ownerAccountId == null) {
            return List.of();
        }
        int clampedLimit = Math.clamp(limit, 1, 100);
        return relationshipRepository.findRecentByOwnerAccountId(ownerAccountId, PageRequest.of(0, clampedLimit))
                .stream()
                .map(this::toView)
                .toList();
    }

    private String trimOrNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private ExternalAccountRelationshipView toView(ExternalAccountRelationship relationship) {
        return new ExternalAccountRelationshipView(
                relationship.getId(),
                relationship.getOwnerAccountId(),
                relationship.getTargetAccountId(),
                relationship.getFollowerStatus(),
                relationship.getSource(),
                relationship.getFollowStatus(),
                relationship.isHasLikedPost(),
                relationship.getNote(),
                relationship.getCreatedAt(),
                relationship.getUpdatedAt()
        );
    }
}

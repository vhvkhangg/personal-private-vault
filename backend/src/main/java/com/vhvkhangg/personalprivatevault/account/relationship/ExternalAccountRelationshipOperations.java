package com.vhvkhangg.personalprivatevault.account.relationship;

import com.vhvkhangg.personalprivatevault.account.view.ExternalAccountRelationshipView;

import java.util.List;
import java.util.Optional;

/**
 * Public capability-oriented contract for External Account Relationship operations.
 */
public interface ExternalAccountRelationshipOperations {

    ExternalAccountRelationshipView setRelationship(SetExternalAccountRelationshipCommand command);

    Optional<ExternalAccountRelationshipView> findByPair(Long ownerAccountId, Long targetAccountId);

    List<ExternalAccountRelationshipView> findRecentByOwner(Long ownerAccountId, int limit);
}

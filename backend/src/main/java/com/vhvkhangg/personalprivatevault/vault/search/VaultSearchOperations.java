package com.vhvkhangg.personalprivatevault.vault.search;

import java.util.List;
import java.util.Set;

/**
 * Public search and qualification operations owned by the Vault module.
 */
public interface VaultSearchOperations {

    /**
     * Filters candidate Vault entry IDs to only those that are active (not soft-deleted in recycle bin)
     * and satisfy all required tags (AND semantics).
     *
     * @param candidateIds candidate Vault entry IDs to qualify
     * @param requiredTagIds optional required tag IDs (all must match); if null or empty, only active check applies
     * @return the subset of candidate IDs that qualify
     */
    Set<Long> filterQualifyingActiveEntries(Set<Long> candidateIds, Set<Long> requiredTagIds);

    /**
     * Searches active Vault entries by tag name, collapsing multiple matching tags per entry
     * to the best tag similarity, and ordering before limiting by best similarity DESC,
     * textual type name ASC, and Vault entry ID ASC.
     *
     * @param query the tag search query parameters
     * @return bounded list of tag candidate hits
     */
    List<VaultTagCandidateHit> searchByTag(VaultTagSearchQuery query);
}

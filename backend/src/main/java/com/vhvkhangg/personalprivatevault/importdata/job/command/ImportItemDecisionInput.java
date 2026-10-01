package com.vhvkhangg.personalprivatevault.importdata.job.command;

import com.vhvkhangg.personalprivatevault.importdata.enums.ImportItemDecision;

/**
 * Caller decision for an individual item by its zero-based index.
 */
public record ImportItemDecisionInput(
        int itemIndex,
        ImportItemDecision decision
) {
}

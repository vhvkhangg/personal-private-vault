package com.vhvkhangg.personalprivatevault.importdata.job.command;

import java.util.List;

/**
 * Command for executing a validated import job with explicit decisions for every item.
 */
public record ExecuteImportJobCommand(
        List<ImportItemDecisionInput> itemDecisions
) {
    public ExecuteImportJobCommand {
        itemDecisions = itemDecisions != null ? List.copyOf(itemDecisions) : List.of();
    }
}

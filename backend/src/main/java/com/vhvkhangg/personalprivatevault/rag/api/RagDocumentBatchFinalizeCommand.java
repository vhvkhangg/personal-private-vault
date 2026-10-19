package com.vhvkhangg.personalprivatevault.rag.api;

/**
 * Command for batch document finalization.
 */
public record RagDocumentBatchFinalizeCommand(
        String dependencyDigest
) {
}

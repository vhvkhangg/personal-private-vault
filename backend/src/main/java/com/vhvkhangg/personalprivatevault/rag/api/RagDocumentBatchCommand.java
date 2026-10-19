package com.vhvkhangg.personalprivatevault.rag.api;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Command to admit a multi-root document indexing batch (ADR-0021 Clause 1).
 */
public record RagDocumentBatchCommand(
        List<RagDocumentRootItem> roots,
        Map<Long, String> descriptionConfirmations
) {
    public RagDocumentBatchCommand {
        Objects.requireNonNull(roots, "roots must not be null");
        if (roots.isEmpty()) {
            throw new IllegalArgumentException("Batch admission roots list must not be empty");
        }
        if (roots.size() > 100) {
            throw new IllegalArgumentException("Batch roots exceed maximum allowed size of 100");
        }
        roots = List.copyOf(roots);
        descriptionConfirmations = descriptionConfirmations != null ? Map.copyOf(descriptionConfirmations) : Map.of();
    }
}

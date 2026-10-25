package com.vhvkhangg.personalprivatevault.rag.api;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Command to admit a multi-root document indexing batch (ADR-0021 Clause 1).
 */
public record RagDocumentBatchCommand(
        List<RagDocumentRootItem> roots,
        Map<Long, String> descriptionConfirmations,
        java.util.Set<String> noChangeRoots
) {
    public RagDocumentBatchCommand(List<RagDocumentRootItem> roots,Map<Long,String> confirmations) {this(roots,confirmations,java.util.Set.of());}

    public RagDocumentBatchCommand {
        Objects.requireNonNull(roots, "roots must not be null");
        if (roots.isEmpty()) {
            throw new IllegalArgumentException("Batch admission roots list must not be empty");
        }
        if (roots.size() > 100) {
            throw new IllegalArgumentException("Batch roots exceed maximum allowed size of 100");
        }
        roots = List.copyOf(roots);
        noChangeRoots=noChangeRoots==null?java.util.Set.of():java.util.Set.copyOf(noChangeRoots);
        if(!roots.stream().map(RagDocumentRootItem::rootKey).collect(java.util.stream.Collectors.toSet()).containsAll(noChangeRoots))throw new IllegalArgumentException("Unchanged root is outside admitted roster");
        descriptionConfirmations = descriptionConfirmations != null ? Map.copyOf(descriptionConfirmations) : Map.of();
    }
}

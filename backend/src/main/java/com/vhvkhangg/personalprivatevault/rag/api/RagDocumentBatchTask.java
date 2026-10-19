package com.vhvkhangg.personalprivatevault.rag.api;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Task item for dispatching a document root to the v3 batch worker.
 */
public record RagDocumentBatchTask(
        String rootKey,
        String sourceId,
        String format,
        String contentText,
        String contentB64,
        List<String> ocrLanguages,
        List<String> approvedFields,
        Map<String, String> transclusions
) {
    public RagDocumentBatchTask {
        Objects.requireNonNull(rootKey, "rootKey must not be null");
        Objects.requireNonNull(sourceId, "sourceId must not be null");
        Objects.requireNonNull(format, "format must not be null");
        ocrLanguages = ocrLanguages != null ? List.copyOf(ocrLanguages) : List.of("en");
        approvedFields = approvedFields != null ? List.copyOf(approvedFields) : List.of();
        transclusions = transclusions != null ? Map.copyOf(transclusions) : Map.of();
    }
}

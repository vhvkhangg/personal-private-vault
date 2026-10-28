package com.vhvkhangg.personalprivatevault.rag.api;

import java.util.Map;

public record RagLocator(
        String profile,
        Long charStart,
        Long charEnd,
        Long lineStart,
        Long lineEnd,
        String heading,
        Map<String, Object> details
) {
    public RagLocator {
        if (details == null) {
            details = Map.of();
        } else {
            details = Map.copyOf(details);
        }
    }
}

package com.vhvkhangg.personalprivatevault.rag.api;

import java.util.Objects;

/**
 * Citation support linking a claim to a verified exact excerpt quote from retrieved evidence.
 */
public record RagQaClaimSupport(
        String citationKey,
        String quote
) {
    public RagQaClaimSupport {
        Objects.requireNonNull(citationKey, "citationKey must not be null");
        Objects.requireNonNull(quote, "quote must not be null");
    }

    @Override
    public String toString() {
        return "RagQaClaimSupport[citationKey=" + citationKey + ", quote=[REDACTED]]";
    }
}

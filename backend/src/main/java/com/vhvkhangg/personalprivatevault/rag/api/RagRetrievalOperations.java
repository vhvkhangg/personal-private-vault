package com.vhvkhangg.personalprivatevault.rag.api;

import java.util.UUID;

/**
 * Public RAG API for authorized read-only Markdown evidence retrieval.
 */
public interface RagRetrievalOperations {

    /**
     * Executes bounded retrieval for authorized Markdown evidence.
     *
     * @param command immutable retrieval command
     * @return bounded retrieval response
     */
    RagRetrievalResponse retrieve(RagRetrievalCommand command);

    /**
     * Requests bounded cancellation and quiescence of a retrieval reader.
     *
     * @param requestId caller-generated UUID identifying the retrieval reader
     * @return cancellation result with terminal disposition
     */
    RagRetrievalCancelResult cancel(UUID requestId);
}

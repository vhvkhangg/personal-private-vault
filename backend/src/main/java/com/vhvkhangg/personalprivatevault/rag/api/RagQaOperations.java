package com.vhvkhangg.personalprivatevault.rag.api;

import java.util.UUID;

/**
 * P19-S19-2 — SUSPENDED / NOT_ACCEPTED / ARCHIVAL_ONLY; do not enable or deploy.
 * Authority: docs/implementation/phase-19/owner-decision-s19-2-source-archival-only-2026-10-31.md.
 *
 * Public RAG module boundary interface for grounded question answering and cancellation.
 */
public interface RagQaOperations {

    /**
     * Executes grounded question answering synchronously (unsupported; requires async sender).
     */
    RagQaResponse answer(RagQaCommand command);

    /**
     * Cancels an active QA request and proves reader quiescence.
     */
    RagQaCancelResult cancel(UUID requestId);
}

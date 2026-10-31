package com.vhvkhangg.personalprivatevault.rag.api;

/**
 * Outcome category for grounded Markdown question answering.
 */
public enum RagQaOutcome {
    ANSWERED,
    PARTIAL,
    CONFLICT,
    ABSTAIN
}

package com.vhvkhangg.personalprivatevault.rag.internal.application;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Configurable lower limits with fixed, reviewed foundation ceilings.
 */
@Component
public record RagLimits(int selectedSources, int catalogSources, int retainedJobs, int jobsPerSource,
                        int inputBytes, int graphEdges, int lockMillis, int statementMillis, int transactionSeconds) {
    public RagLimits(
        @Value("${ppv.rag.limits.max-selected-sources:100}") int selectedSources,
        @Value("${ppv.rag.limits.max-catalog-sources:1000}") int catalogSources,
        @Value("${ppv.rag.limits.max-retained-jobs:1000}") int retainedJobs,
        @Value("${ppv.rag.limits.max-jobs-per-source:256}") int jobsPerSource,
        @Value("${ppv.rag.limits.max-input-bytes:1048576}") int inputBytes,
        @Value("${ppv.rag.limits.max-graph-edges:10000}") int graphEdges,
        @Value("${ppv.rag.limits.lock-timeout-ms:3000}") int lockMillis,
        @Value("${ppv.rag.limits.statement-timeout-ms:5000}") int statementMillis,
        @Value("${ppv.rag.limits.transaction-timeout-seconds:10}") int transactionSeconds) {
        bounded(selectedSources, 100);
        bounded(catalogSources, 1000);
        bounded(retainedJobs, 1000);
        bounded(jobsPerSource, 256);
        bounded(inputBytes, 1048576);
        bounded(graphEdges, 10000);
        bounded(lockMillis, 3000);
        bounded(statementMillis, 5000);
        bounded(transactionSeconds, 10);
        this.selectedSources = selectedSources;
        this.catalogSources = catalogSources;
        this.retainedJobs = retainedJobs;
        this.jobsPerSource = jobsPerSource;
        this.inputBytes = inputBytes;
        this.graphEdges = graphEdges;
        this.lockMillis = lockMillis;
        this.statementMillis = statementMillis;
        this.transactionSeconds = transactionSeconds;
    }

    private static void bounded(int value, int ceiling) {
        if (value < 1 || value > ceiling) throw new IllegalArgumentException("RAG budget is outside reviewed bounds");
    }
}

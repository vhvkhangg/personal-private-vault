package com.vhvkhangg.personalprivatevault.rag.internal.application.qa;

import com.vhvkhangg.personalprivatevault.rag.api.RagQaHistoryEntry;
import com.vhvkhangg.personalprivatevault.rag.api.RagRetrievalMatch;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Packs question and client-supplied conversation turns for query embedding and model context.
 * Bounded history turn concatenation aids referent resolution without altering source authority.
 */
@ConditionalOnProperty(name = "ppv.rag.test-scoped-execution", havingValue = "true")
@Component
public class RagQaContextPacker {

    private static final int MAX_QUERY_CODE_POINTS = 4096;

    public String buildQueryEmbeddingText(String question, List<RagQaHistoryEntry> history) {
        if (question == null || question.isBlank()) {
            throw new IllegalArgumentException("Question must not be blank");
        }

        if (history == null || history.isEmpty()) {
            return question.trim();
        }

        // Find most recent user turn for unambiguous follow-up hint
        RagQaHistoryEntry lastUserTurn = null;
        for (int i = history.size() - 1; i >= 0; i--) {
            RagQaHistoryEntry entry = history.get(i);
            if ("user".equalsIgnoreCase(entry.role())) {
                lastUserTurn = entry;
                break;
            }
        }

        if (lastUserTurn == null || lastUserTurn.content() == null || lastUserTurn.content().isBlank()) {
            return question.trim();
        }

        String combined = lastUserTurn.content().trim() + "\n" + question.trim();
        int cp = combined.codePointCount(0, combined.length());
        if (cp > MAX_QUERY_CODE_POINTS) {
            throw new IllegalArgumentException("Combined query and history exceeds 4096 code points limit");
        }

        return combined;
    }

    private static final int MAX_CONTEXT_TOKENS = 4096;
    private static final int GENERATION_RESERVE_TOKENS = 768;
    private static final int SYSTEM_PROMPT_OVERHEAD = 500;
    private static final int CONTROL_TOKEN_HEADROOM = 64;
    private static final int MAX_PROMPT_BUDGET = MAX_CONTEXT_TOKENS - GENERATION_RESERVE_TOKENS - SYSTEM_PROMPT_OVERHEAD - CONTROL_TOKEN_HEADROOM;

    public static int estimateTokens(String text) {
        if (text == null || text.isEmpty()) return 0;
        int nonAscii = 0;
        int ascii = 0;
        for (int i = 0; i < text.length(); ) {
            int cp = text.codePointAt(i);
            if (cp > 127) {
                nonAscii++;
            } else {
                ascii++;
            }
            i += Character.charCount(cp);
        }
        return (int) Math.ceil(ascii * 0.35 + nonAscii * 2.0) + 4;
    }

    private int estimateHistoryTokens(List<RagQaHistoryEntry> history) {
        if (history == null || history.isEmpty()) return 0;
        int sum = 0;
        for (RagQaHistoryEntry entry : history) {
            if (entry != null && entry.content() != null) {
                sum += estimateTokens(entry.content()) + 8;
            }
        }
        return sum;
    }

    /**
     * Packs whole verified evidence units within the model context budget.
     * Whole units are prioritized according to ranking order; units that do not fit
     * are dropped completely without text slicing. If non-evidence prompt (history + question)
     * exceeds budget, history is narrowed; if question alone exceeds budget, an explicit
     * exception is thrown.
     */
    public List<RagRetrievalMatch> packEvidence(
            String question,
            List<RagQaHistoryEntry> history,
            List<RagRetrievalMatch> matches
    ) {
        if (matches == null || matches.isEmpty()) {
            return List.of();
        }

        int questionTokens = estimateTokens(question);
        if (questionTokens > MAX_PROMPT_BUDGET) {
            throw new IllegalArgumentException("Question exceeds maximum context budget");
        }

        List<RagQaHistoryEntry> activeHistory = history != null ? new java.util.ArrayList<>(history) : new java.util.ArrayList<>();
        int historyTokens = estimateHistoryTokens(activeHistory);
        while (!activeHistory.isEmpty() && (questionTokens + historyTokens) > MAX_PROMPT_BUDGET) {
            activeHistory.remove(0);
            historyTokens = estimateHistoryTokens(activeHistory);
        }

        int availableEvidenceTokens = MAX_PROMPT_BUDGET - questionTokens - historyTokens;
        if (availableEvidenceTokens <= 0) {
            return List.of();
        }

        List<RagRetrievalMatch> packed = new java.util.ArrayList<>();
        int usedTokens = 0;

        for (RagRetrievalMatch match : matches) {
            int matchTokens = estimateTokens(match.excerpt()) + 30;
            if (usedTokens + matchTokens <= availableEvidenceTokens) {
                packed.add(match);
                usedTokens += matchTokens;
            } else {
                break;
            }
        }

        return List.copyOf(packed);
    }
}

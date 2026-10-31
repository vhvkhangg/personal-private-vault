package com.vhvkhangg.personalprivatevault.rag.internal.application.qa;

import com.vhvkhangg.personalprivatevault.rag.api.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Validates candidate QA model output strictly against permitted retrieved evidence.
 * Enforces exact excerpt substring provenance, outcome consistency, and renders
 * the final grounded answer string from validated claims.
 */
@ConditionalOnProperty(name = "ppv.rag.test-scoped-execution", havingValue = "true")
@Component
public class RagQaValidator {

    public record ValidatedCandidate(
            RagQaOutcome outcome,
            String answer,
            List<RagQaClaim> claims,
            List<String> unansweredParts,
            List<RagQaConflictPair> conflictPairs,
            List<RagRetrievalMatch> citations
    ) {}

    public ValidatedCandidate validate(
            UUID requestId,
            String rawOutcome,
            List<RagQaClaim> rawClaims,
            List<String> rawUnansweredParts,
            List<RagQaConflictPair> rawConflictPairs,
            List<RagRetrievalMatch> retrievedEvidence,
            String responseLanguage
    ) {
        Objects.requireNonNull(requestId, "requestId must not be null");
        if (retrievedEvidence == null) retrievedEvidence = List.of();
        if (rawClaims == null) rawClaims = List.of();
        if (rawUnansweredParts == null) rawUnansweredParts = List.of();
        if (rawConflictPairs == null) rawConflictPairs = List.of();

        List<String> validatedUnanswered = new ArrayList<>();
        for (String rawPart : rawUnansweredParts) {
            if (rawPart == null || rawPart.isBlank()) continue;
            // No model-proposed prose has authority as a requested-question-part label.
            validatedUnanswered.add("UNKNOWN_REMAINDER");
            break;
        }

        RagQaOutcome outcome;
        try {
            String clean = rawOutcome != null ? rawOutcome.trim().toUpperCase() : "ABSTAIN";
            if ("ABSTAINED".equals(clean)) clean = "ABSTAIN";
            outcome = RagQaOutcome.valueOf(clean);
        } catch (IllegalArgumentException e) {
            throw new RagQueryUnavailableException("Unrecognized candidate outcome");
        }

        Map<String, RagRetrievalMatch> evidenceByKey = new HashMap<>();
        for (RagRetrievalMatch match : retrievedEvidence) {
            evidenceByKey.put(match.citationKey(), match);
        }

        Map<String, RagQaClaim> claimsById = new LinkedHashMap<>();
        Set<String> citedKeys = new LinkedHashSet<>();

        for (RagQaClaim claim : rawClaims) {
            if (claim.id() == null || claim.id().isBlank()) {
                throw new RagQueryUnavailableException("Candidate claim missing identifier");
            }
            if (claimsById.containsKey(claim.id())) {
                throw new RagQueryUnavailableException("Duplicate claim identifier detected");
            }
            if (claim.text() == null || claim.text().isBlank()) {
                throw new RagQueryUnavailableException("Candidate claim text must not be blank");
            }
            if (outcome != RagQaOutcome.ABSTAIN && claim.supports().isEmpty()) {
                throw new RagQueryUnavailableException("Factual claim has no supporting evidence");
            }

            List<RagQaClaimSupport> validatedSupports = new ArrayList<>();
            for (RagQaClaimSupport support : claim.supports()) {
                String cKey = support.citationKey();
                if (cKey != null && !evidenceByKey.containsKey(cKey)) {
                    String altKey = cKey.startsWith("[") && cKey.endsWith("]")
                            ? cKey.substring(1, cKey.length() - 1)
                            : "[" + cKey + "]";
                    if (evidenceByKey.containsKey(altKey)) {
                        cKey = altKey;
                    }
                }
                if (cKey == null || !evidenceByKey.containsKey(cKey)) {
                    throw new RagQueryUnavailableException("Claim references unknown citation key");
                }
                RagRetrievalMatch match = evidenceByKey.get(cKey);
                String quote = support.quote();
                if (quote == null || quote.isBlank()) {
                    throw new RagQueryUnavailableException("Claim citation quote must not be blank");
                }
                if (!match.excerpt().contains(quote)) {
                    throw new RagQueryUnavailableException("Claim citation quote is not an exact substring of evidence excerpt");
                }
                String canonicalKey = cKey.startsWith("[") && cKey.endsWith("]") ? cKey : "[" + cKey + "]";
                validatedSupports.add(new RagQaClaimSupport(canonicalKey, quote));
                citedKeys.add(canonicalKey);
            }
            claimsById.put(claim.id(), new RagQaClaim(claim.id(), claim.text().trim(), validatedSupports));
        }

        switch (outcome) {
            case ABSTAIN -> {
                if (!claimsById.isEmpty()) {
                    throw new RagQueryUnavailableException("ABSTAIN outcome cannot contain factual claims");
                }
            }
            case ANSWERED -> {
                if (claimsById.isEmpty()) {
                    throw new RagQueryUnavailableException("ANSWERED outcome must have at least one supported claim");
                }
            }
            case PARTIAL -> {
                if (claimsById.isEmpty()) {
                    throw new RagQueryUnavailableException("PARTIAL outcome must have at least one supported claim");
                }
                if (validatedUnanswered.isEmpty()) {
                    throw new RagQueryUnavailableException("PARTIAL outcome must specify unanswered question parts");
                }
            }
            case CONFLICT -> {
                if (claimsById.size() < 2 || rawConflictPairs.isEmpty()) {
                    outcome = claimsById.isEmpty() ? RagQaOutcome.ABSTAIN : (validatedUnanswered.isEmpty() ? RagQaOutcome.ANSWERED : RagQaOutcome.PARTIAL);
                } else {
                    for (RagQaConflictPair pair : rawConflictPairs) {
                        RagQaClaim claimA = claimsById.get(pair.claimIdA());
                        RagQaClaim claimB = claimsById.get(pair.claimIdB());
                        if (claimA == null || claimB == null) {
                            throw new RagQueryUnavailableException("Conflict pair references undefined claim ID");
                        }
                        if (pair.claimIdA().equals(pair.claimIdB())) {
                            throw new RagQueryUnavailableException("Conflict pair cannot reference the same claim");
                        }
                        Set<String> citationsA = claimA.supports().stream().map(RagQaClaimSupport::citationKey).collect(Collectors.toSet());
                        Set<String> citationsB = claimB.supports().stream().map(RagQaClaimSupport::citationKey).collect(Collectors.toSet());
                        if (citationsA.equals(citationsB)) {
                            throw new RagQueryUnavailableException("Conflict pair claims must originate from distinct evidence citations");
                        }
                    }
                }
            }
        }

        List<RagRetrievalMatch> citedMatches = retrievedEvidence.stream()
                .filter(m -> citedKeys.contains(m.citationKey()))
                .toList();

        Set<String> noncanonKeys = citedMatches.stream()
                .filter(match -> "IDEAVERSE_NONCORE".equals(match.group()))
                .map(RagRetrievalMatch::citationKey)
                .collect(Collectors.toSet());
        String renderedAnswer = renderAnswer(outcome, new ArrayList<>(claimsById.values()), rawConflictPairs, responseLanguage, noncanonKeys);

        return new ValidatedCandidate(
                outcome,
                renderedAnswer,
                List.copyOf(claimsById.values()),
                List.copyOf(validatedUnanswered),
                List.copyOf(rawConflictPairs),
                citedMatches
        );
    }

    private String renderAnswer(
            RagQaOutcome outcome,
            List<RagQaClaim> claims,
            List<RagQaConflictPair> conflictPairs,
            String responseLanguage,
            Set<String> noncanonKeys
    ) {
        String lang = responseLanguage != null ? responseLanguage.trim().toLowerCase() : "vi";

        if (outcome == RagQaOutcome.ABSTAIN) {
            return switch (lang) {
                case "en" -> "No relevant facts found in permitted evidence.";
                case "zh", "zh-hans", "zh-hant" -> "未在允许的证据中找到相关事实。";
                default -> "Không tìm thấy thông tin phù hợp trong phạm vi tài liệu được phép.";
            };
        }

        StringBuilder sb = new StringBuilder();

        if (outcome == RagQaOutcome.CONFLICT) {
            for (RagQaConflictPair pair : conflictPairs) {
                RagQaClaim a = claims.stream().filter(c -> c.id().equals(pair.claimIdA())).findFirst().orElse(null);
                RagQaClaim b = claims.stream().filter(c -> c.id().equals(pair.claimIdB())).findFirst().orElse(null);
                if (a != null && b != null) {
                    if (!sb.isEmpty()) sb.append("\n\n");
                    sb.append(renderClaim(a, noncanonKeys)).append("\n").append(renderClaim(b, noncanonKeys));
                }
            }
            // Include any remaining claims not part of conflict pairs
            Set<String> handledIds = conflictPairs.stream()
                    .flatMap(p -> java.util.stream.Stream.of(p.claimIdA(), p.claimIdB()))
                    .collect(Collectors.toSet());
            for (RagQaClaim c : claims) {
                if (!handledIds.contains(c.id())) {
                    if (!sb.isEmpty()) sb.append("\n\n");
                    sb.append(renderClaim(c, noncanonKeys));
                }
            }
        } else {
            for (RagQaClaim claim : claims) {
                if (!sb.isEmpty()) sb.append(" ");
                sb.append(renderClaim(claim, noncanonKeys));
            }
        }

        return sb.toString();
    }

    private String renderClaim(RagQaClaim claim, Set<String> noncanonKeys) {
        Set<String> citations = claim.supports().stream()
                .map(RagQaClaimSupport::citationKey)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        String citStr = String.join(" ", citations);
        String text = claim.text().trim();
        if (citations.stream().anyMatch(noncanonKeys::contains)) text = "NONCANON: " + text;
        if (citStr.isEmpty()) return text;
        if (text.endsWith(".")) {
            return text.substring(0, text.length() - 1) + " " + citStr + ".";
        }
        return text + " " + citStr;
    }
}

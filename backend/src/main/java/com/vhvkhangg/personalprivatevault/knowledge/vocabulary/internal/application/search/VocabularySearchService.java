package com.vhvkhangg.personalprivatevault.knowledge.vocabulary.internal.application.search;

import com.vhvkhangg.personalprivatevault.knowledge.vocabulary.search.VocabularySearchDocument;
import com.vhvkhangg.personalprivatevault.knowledge.vocabulary.search.VocabularySearchHit;
import com.vhvkhangg.personalprivatevault.knowledge.vocabulary.search.VocabularySearchOperations;
import com.vhvkhangg.personalprivatevault.vault.enums.VaultEntryType;
import com.vhvkhangg.personalprivatevault.vault.search.VaultSearchOperations;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VocabularySearchService implements VocabularySearchOperations {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final VaultSearchOperations vaultSearchOperations;

    @Override
    public List<VocabularySearchHit> search(String query, Set<Long> requiredTagIds, int limit) {
        if (query == null || query.isBlank() || query.trim().length() > 200) {
            throw new IllegalArgumentException("Query must be between 1 and 200 characters");
        }
        if (limit <= 0 || limit > 601) {
            throw new IllegalArgumentException("Limit must be between 1 and 601");
        }
        if (requiredTagIds != null) {
            if (requiredTagIds.size() > 10) {
                throw new IllegalArgumentException("At most 10 required tags allowed");
            }
            for (Long tagId : requiredTagIds) {
                if (tagId == null || tagId <= 0) {
                    throw new IllegalArgumentException("Tag ID must be a positive number");
                }
            }
        }

        jdbcTemplate.getJdbcTemplate().execute("SET LOCAL pg_trgm.similarity_threshold = 0.3;");

        String rawQuery = query.trim();
        boolean enableFuzzy = rawQuery.length() >= 3;
        String escapedRaw = escapeLike(rawQuery);
        String prefixPattern = escapedRaw + "%";
        String substringPattern = "%" + escapedRaw + "%";

        int pageSize = Math.min(Math.max(limit, 50), 100);
        int currentOffset = 0;

        List<VocabularySearchHit> qualifyingHits = new ArrayList<>();

        String sql = """
                SELECT
                    v.id AS vault_entry_id,
                    CAST('VOCABULARY' AS text) COLLATE "C" AS type_name,
                    v.word AS primary_text,
                    v.part_of_speech AS secondary_text,
                    v.meaning AS meaning_text,
                    v.example AS example_text,
                    v.source_name AS source_text,
                    CASE
                        WHEN lower(v.word) = lower(CAST(:rawQuery AS text)) THEN 600
                        WHEN lower(v.word) LIKE lower(CAST(:prefixPattern AS text)) ESCAPE '\\' THEN 550
                        WHEN lower(v.word) LIKE lower(CAST(:substringPattern AS text)) ESCAPE '\\' THEN 500
                        WHEN (v.pronunciation IS NOT NULL AND lower(v.pronunciation) = lower(CAST(:rawQuery AS text)))
                          OR (v.ipa IS NOT NULL AND lower(v.ipa) = lower(CAST(:rawQuery AS text)))
                          OR (v.part_of_speech IS NOT NULL AND lower(v.part_of_speech) = lower(CAST(:rawQuery AS text)))
                          OR (v.pronunciation IS NOT NULL AND lower(v.pronunciation) LIKE lower(CAST(:prefixPattern AS text)) ESCAPE '\\')
                          OR (v.ipa IS NOT NULL AND lower(v.ipa) LIKE lower(CAST(:prefixPattern AS text)) ESCAPE '\\')
                          OR (v.part_of_speech IS NOT NULL AND lower(v.part_of_speech) LIKE lower(CAST(:prefixPattern AS text)) ESCAPE '\\') THEN 450
                        WHEN (v.pronunciation IS NOT NULL AND lower(v.pronunciation) LIKE lower(CAST(:substringPattern AS text)) ESCAPE '\\')
                          OR (v.ipa IS NOT NULL AND lower(v.ipa) LIKE lower(CAST(:substringPattern AS text)) ESCAPE '\\')
                          OR (v.part_of_speech IS NOT NULL AND lower(v.part_of_speech) LIKE lower(CAST(:substringPattern AS text)) ESCAPE '\\') THEN 400
                        WHEN :enableFuzzy AND (
                            (lower(v.word) % lower(CAST(:rawQuery AS text)) AND similarity(lower(v.word), lower(CAST(:rawQuery AS text))) >= 0.30)
                            OR (v.pronunciation IS NOT NULL AND similarity(lower(v.pronunciation), lower(CAST(:rawQuery AS text))) >= 0.30)
                            OR (v.ipa IS NOT NULL AND similarity(lower(v.ipa), lower(CAST(:rawQuery AS text))) >= 0.30)
                        ) THEN 350
                        WHEN (v.meaning IS NOT NULL AND lower(v.meaning) LIKE lower(CAST(:substringPattern AS text)) ESCAPE '\\')
                          OR (v.example IS NOT NULL AND lower(v.example) LIKE lower(CAST(:substringPattern AS text)) ESCAPE '\\')
                          OR (v.source_name IS NOT NULL AND lower(v.source_name) LIKE lower(CAST(:substringPattern AS text)) ESCAPE '\\') THEN 200
                        ELSE 0
                    END AS rank_bucket,
                    CASE
                        WHEN lower(v.word) = lower(CAST(:rawQuery AS text)) THEN 0.0
                        WHEN lower(v.word) LIKE lower(CAST(:prefixPattern AS text)) ESCAPE '\\' THEN 0.0
                        WHEN lower(v.word) LIKE lower(CAST(:substringPattern AS text)) ESCAPE '\\' THEN 0.0
                        WHEN (v.pronunciation IS NOT NULL AND (lower(v.pronunciation) = lower(CAST(:rawQuery AS text)) OR lower(v.pronunciation) LIKE lower(CAST(:prefixPattern AS text)) ESCAPE '\\' OR lower(v.pronunciation) LIKE lower(CAST(:substringPattern AS text)) ESCAPE '\\'))
                          OR (v.ipa IS NOT NULL AND (lower(v.ipa) = lower(CAST(:rawQuery AS text)) OR lower(v.ipa) LIKE lower(CAST(:prefixPattern AS text)) ESCAPE '\\' OR lower(v.ipa) LIKE lower(CAST(:substringPattern AS text)) ESCAPE '\\'))
                          OR (v.part_of_speech IS NOT NULL AND (lower(v.part_of_speech) = lower(CAST(:rawQuery AS text)) OR lower(v.part_of_speech) LIKE lower(CAST(:prefixPattern AS text)) ESCAPE '\\' OR lower(v.part_of_speech) LIKE lower(CAST(:substringPattern AS text)) ESCAPE '\\')) THEN 0.0
                        WHEN :enableFuzzy AND (
                            (lower(v.word) % lower(CAST(:rawQuery AS text)) AND similarity(lower(v.word), lower(CAST(:rawQuery AS text))) >= 0.30)
                            OR (v.pronunciation IS NOT NULL AND similarity(lower(v.pronunciation), lower(CAST(:rawQuery AS text))) >= 0.30)
                            OR (v.ipa IS NOT NULL AND similarity(lower(v.ipa), lower(CAST(:rawQuery AS text))) >= 0.30)
                        ) THEN GREATEST(
                            similarity(lower(v.word), lower(CAST(:rawQuery AS text))),
                            CASE WHEN v.pronunciation IS NOT NULL THEN similarity(lower(v.pronunciation), lower(CAST(:rawQuery AS text))) ELSE 0.0 END,
                            CASE WHEN v.ipa IS NOT NULL THEN similarity(lower(v.ipa), lower(CAST(:rawQuery AS text))) ELSE 0.0 END
                        )
                        ELSE 0.0
                    END AS similarity,
                    CASE
                        WHEN lower(v.word) = lower(CAST(:rawQuery AS text)) THEN 'PRIMARY_EXACT'
                        WHEN lower(v.word) LIKE lower(CAST(:prefixPattern AS text)) ESCAPE '\\' THEN 'PRIMARY_PREFIX'
                        WHEN lower(v.word) LIKE lower(CAST(:substringPattern AS text)) ESCAPE '\\' THEN 'PRIMARY_SUBSTRING'
                        WHEN (v.pronunciation IS NOT NULL AND lower(v.pronunciation) = lower(CAST(:rawQuery AS text)))
                          OR (v.ipa IS NOT NULL AND lower(v.ipa) = lower(CAST(:rawQuery AS text)))
                          OR (v.part_of_speech IS NOT NULL AND lower(v.part_of_speech) = lower(CAST(:rawQuery AS text))) THEN 'SECONDARY_EXACT'
                        WHEN (v.pronunciation IS NOT NULL AND lower(v.pronunciation) LIKE lower(CAST(:prefixPattern AS text)) ESCAPE '\\')
                          OR (v.ipa IS NOT NULL AND lower(v.ipa) LIKE lower(CAST(:prefixPattern AS text)) ESCAPE '\\')
                          OR (v.part_of_speech IS NOT NULL AND lower(v.part_of_speech) LIKE lower(CAST(:prefixPattern AS text)) ESCAPE '\\') THEN 'SECONDARY_PREFIX'
                        WHEN (v.pronunciation IS NOT NULL AND lower(v.pronunciation) LIKE lower(CAST(:substringPattern AS text)) ESCAPE '\\')
                          OR (v.ipa IS NOT NULL AND lower(v.ipa) LIKE lower(CAST(:substringPattern AS text)) ESCAPE '\\')
                          OR (v.part_of_speech IS NOT NULL AND lower(v.part_of_speech) LIKE lower(CAST(:substringPattern AS text)) ESCAPE '\\') THEN 'SECONDARY_SUBSTRING'
                        WHEN :enableFuzzy AND (
                            (lower(v.word) % lower(CAST(:rawQuery AS text)) AND similarity(lower(v.word), lower(CAST(:rawQuery AS text))) >= 0.30)
                            OR (v.pronunciation IS NOT NULL AND similarity(lower(v.pronunciation), lower(CAST(:rawQuery AS text))) >= 0.30)
                            OR (v.ipa IS NOT NULL AND similarity(lower(v.ipa), lower(CAST(:rawQuery AS text))) >= 0.30)
                        ) THEN 'SHORT_FUZZY'
                        WHEN (v.meaning IS NOT NULL AND lower(v.meaning) LIKE lower(CAST(:substringPattern AS text)) ESCAPE '\\')
                          OR (v.example IS NOT NULL AND lower(v.example) LIKE lower(CAST(:substringPattern AS text)) ESCAPE '\\')
                          OR (v.source_name IS NOT NULL AND lower(v.source_name) LIKE lower(CAST(:substringPattern AS text)) ESCAPE '\\') THEN 'BODY'
                        ELSE 'NONE'
                    END AS match_kind
                FROM vocabulary_items v
                WHERE (
                    lower(v.word) LIKE lower(CAST(:substringPattern AS text)) ESCAPE '\\'
                    OR (v.pronunciation IS NOT NULL AND lower(v.pronunciation) LIKE lower(CAST(:substringPattern AS text)) ESCAPE '\\')
                    OR (v.ipa IS NOT NULL AND lower(v.ipa) LIKE lower(CAST(:substringPattern AS text)) ESCAPE '\\')
                    OR (v.part_of_speech IS NOT NULL AND lower(v.part_of_speech) LIKE lower(CAST(:substringPattern AS text)) ESCAPE '\\')
                    OR (:enableFuzzy AND (
                        (lower(v.word) % lower(CAST(:rawQuery AS text)) AND similarity(lower(v.word), lower(CAST(:rawQuery AS text))) >= 0.30)
                        OR (v.pronunciation IS NOT NULL AND similarity(lower(v.pronunciation), lower(CAST(:rawQuery AS text))) >= 0.30)
                        OR (v.ipa IS NOT NULL AND similarity(lower(v.ipa), lower(CAST(:rawQuery AS text))) >= 0.30)
                    ))
                    OR (v.meaning IS NOT NULL AND lower(v.meaning) LIKE lower(CAST(:substringPattern AS text)) ESCAPE '\\')
                    OR (v.example IS NOT NULL AND lower(v.example) LIKE lower(CAST(:substringPattern AS text)) ESCAPE '\\')
                    OR (v.source_name IS NOT NULL AND lower(v.source_name) LIKE lower(CAST(:substringPattern AS text)) ESCAPE '\\')
                )
                ORDER BY
                    rank_bucket DESC,
                    similarity DESC,
                    type_name ASC,
                    vault_entry_id ASC
                LIMIT :limit OFFSET :offset
                """;

        while (qualifyingHits.size() < limit) {
            MapSqlParameterSource params = new MapSqlParameterSource()
                    .addValue("rawQuery", rawQuery)
                    .addValue("prefixPattern", prefixPattern)
                    .addValue("substringPattern", substringPattern)
                    .addValue("enableFuzzy", enableFuzzy)
                    .addValue("limit", pageSize)
                    .addValue("offset", currentOffset);

            List<RawCandidate> candidates = jdbcTemplate.query(sql, params, (rs, rowNum) -> new RawCandidate(
                    rs.getLong("vault_entry_id"),
                    VaultEntryType.valueOf(rs.getString("type_name")),
                    rs.getString("primary_text"),
                    rs.getString("secondary_text"),
                    rs.getString("meaning_text"),
                    rs.getString("example_text"),
                    rs.getString("source_text"),
                    rs.getInt("rank_bucket"),
                    rs.getDouble("similarity"),
                    rs.getString("match_kind")
            ));

            if (candidates.isEmpty()) {
                break;
            }

            Set<Long> candidateIds = candidates.stream()
                    .map(RawCandidate::vaultEntryId)
                    .collect(Collectors.toSet());

            Set<Long> qualifiedIds = vaultSearchOperations.filterQualifyingActiveEntries(
                    candidateIds, requiredTagIds);

            for (RawCandidate c : candidates) {
                if (qualifiedIds.contains(c.vaultEntryId())) {
                    String snippet = null;
                    if ("BODY".equals(c.matchKind())) {
                        if (c.meaningText() != null) {
                            snippet = extractSnippet(c.meaningText(), rawQuery, 240);
                        }
                        if (snippet == null && c.exampleText() != null) {
                            snippet = extractSnippet(c.exampleText(), rawQuery, 240);
                        }
                        if (snippet == null && c.sourceText() != null) {
                            snippet = extractSnippet(c.sourceText(), rawQuery, 240);
                        }
                    }
                    qualifyingHits.add(new VocabularySearchHit(
                            c.vaultEntryId(),
                            c.entryType(),
                            c.primaryText(),
                            c.secondaryText(),
                            snippet,
                            c.rankBucket(),
                            c.similarity(),
                            c.matchKind()
                    ));
                    if (qualifyingHits.size() == limit) {
                        break;
                    }
                }
            }

            if (candidates.size() < pageSize) {
                break;
            }
            currentOffset += pageSize;
        }

        return qualifyingHits;
    }

    @Override
    public Map<Long, VocabularySearchDocument> lookupDocuments(Set<Long> vaultEntryIds) {
        if (vaultEntryIds == null || vaultEntryIds.isEmpty()) {
            return Map.of();
        }
        if (vaultEntryIds.size() > 601) {
            throw new IllegalArgumentException("Lookup IDs batch size must not exceed 601");
        }
        for (Long id : vaultEntryIds) {
            if (id == null || id <= 0) {
                throw new IllegalArgumentException("Lookup ID must be a positive number");
            }
        }
        String sql = "SELECT v.id AS vault_entry_id, v.word AS primary_text, v.part_of_speech AS secondary_text FROM vocabulary_items v WHERE v.id IN (:ids)";
        MapSqlParameterSource params = new MapSqlParameterSource("ids", vaultEntryIds);
        List<VocabularySearchDocument> docs = jdbcTemplate.query(sql, params, (rs, rowNum) -> new VocabularySearchDocument(
                rs.getLong("vault_entry_id"),
                VaultEntryType.VOCABULARY,
                rs.getString("primary_text"),
                rs.getString("secondary_text")
        ));
        return docs.stream().collect(Collectors.toMap(VocabularySearchDocument::vaultEntryId, Function.identity()));
    }

    private static String escapeLike(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }

    private static String extractSnippet(String content, String query, int maxLength) {
        if (content == null || content.isBlank() || query == null || query.isBlank() || maxLength <= 0) {
            return null;
        }
        String cleaned = cleanPlainText(content);
        if (cleaned.isBlank()) {
            return null;
        }
        String trimmedQuery = query.trim();
        int index = findMatchIndex(cleaned, trimmedQuery);
        if (index < 0) {
            return null;
        }
        if (cleaned.length() <= maxLength) {
            return cleaned;
        }

        int queryLen = trimmedQuery.length();
        int contentLen = cleaned.length();

        if (index + queryLen <= maxLength - 3) {
            int start = 0;
            int end = maxLength - 3;
            if (end > 0 && Character.isHighSurrogate(cleaned.charAt(end - 1))) {
                end--;
            }
            String sub = cleaned.substring(start, end).trim();
            if (sub.isEmpty()) return null;
            return sub + "...";
        }

        if (index >= contentLen - (maxLength - 3)) {
            int end = contentLen;
            int start = contentLen - (maxLength - 3);
            if (start < contentLen && Character.isLowSurrogate(cleaned.charAt(start))) {
                start++;
            }
            String sub = cleaned.substring(start, end).trim();
            if (sub.isEmpty()) return null;
            return "..." + sub;
        }

        int maxSubLen = Math.max(1, maxLength - 6);
        int halfWindow = (maxSubLen - queryLen) / 2;
        int start = Math.max(0, index - halfWindow);
        int end = start + maxSubLen;

        if (end > contentLen) {
            end = contentLen;
            start = Math.max(0, end - maxSubLen);
        }
        if (start == 0) {
            end = Math.min(contentLen, maxSubLen);
        }

        if (start > 0 && start < contentLen && Character.isLowSurrogate(cleaned.charAt(start))) {
            start++;
        }
        if (end > 0 && end <= contentLen && Character.isHighSurrogate(cleaned.charAt(end - 1))) {
            end--;
        }

        boolean needPrefix = start > 0;
        boolean needSuffix = end < contentLen;

        String sub = cleaned.substring(start, end).trim();
        if (sub.isEmpty()) {
            return null;
        }

        String snippet = (needPrefix ? "..." : "") + sub + (needSuffix ? "..." : "");
        if (snippet.length() > maxLength) {
            int excess = snippet.length() - maxLength;
            end -= excess;
            if (end > 0 && Character.isHighSurrogate(cleaned.charAt(end - 1))) {
                end--;
            }
            sub = cleaned.substring(start, end).trim();
            snippet = (needPrefix ? "..." : "") + sub + (needSuffix ? "..." : "");
        }
        return snippet;
    }

    private static int findMatchIndex(String text, String query) {
        if (text == null || query == null || query.isEmpty()) {
            return -1;
        }
        int exactIndex = text.indexOf(query);
        if (exactIndex >= 0) {
            return exactIndex;
        }
        int textLen = text.length();
        int queryLen = query.length();
        int limit = textLen - queryLen;
        for (int i = 0; i <= limit; i++) {
            if (text.regionMatches(true, i, query, 0, queryLen)) {
                return i;
            }
        }
        return -1;
    }

    private static String cleanPlainText(String input) {
        if (input == null) {
            return "";
        }
        String text = input.replaceAll("<[^>]+>", " ");
        text = text.replaceAll("(?m)^#+\\s*", "");
        text = text.replaceAll("\\[([^\\]]+)\\]\\([^)]+\\)", "$1");
        text = text.replaceAll("!\\[([^\\]]*)\\]\\([^)]+\\)", "$1");
        text = text.replaceAll("(?<![a-zA-Z0-9])_{1,2}([^_]+)_{1,2}(?![a-zA-Z0-9])", "$1");
        text = text.replaceAll("\\*{1,3}([^*]+)\\*{1,3}", "$1");
        text = text.replaceAll("~{1,2}([^~]+)~{1,2}", "$1");
        text = text.replaceAll("`{1,3}([^`]+)`{1,3}", "$1");
        text = text.replaceAll("[*~`]", "");
        text = text.replaceAll("\\s+", " ").trim();
        return text;
    }

    private record RawCandidate(
            Long vaultEntryId,
            VaultEntryType entryType,
            String primaryText,
            String secondaryText,
            String meaningText,
            String exampleText,
            String sourceText,
            int rankBucket,
            double similarity,
            String matchKind
    ) {}
}

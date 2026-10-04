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
        String lowerQuery = rawQuery.toLowerCase();
        boolean enableFuzzy = rawQuery.length() >= 3;
        String prefixPattern = escapeLike(lowerQuery) + "%";
        String substringPattern = "%" + escapeLike(lowerQuery) + "%";

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
                        WHEN lower(v.word) = :lowerQuery THEN 600
                        WHEN lower(v.word) LIKE :prefixPattern ESCAPE '\\' THEN 550
                        WHEN lower(v.word) LIKE :substringPattern ESCAPE '\\' THEN 500
                        WHEN (v.pronunciation IS NOT NULL AND lower(v.pronunciation) = :lowerQuery)
                          OR (v.ipa IS NOT NULL AND lower(v.ipa) = :lowerQuery)
                          OR (v.part_of_speech IS NOT NULL AND lower(v.part_of_speech) = :lowerQuery)
                          OR (v.pronunciation IS NOT NULL AND lower(v.pronunciation) LIKE :prefixPattern ESCAPE '\\' )
                          OR (v.ipa IS NOT NULL AND lower(v.ipa) LIKE :prefixPattern ESCAPE '\\' )
                          OR (v.part_of_speech IS NOT NULL AND lower(v.part_of_speech) LIKE :prefixPattern ESCAPE '\\' ) THEN 450
                        WHEN (v.pronunciation IS NOT NULL AND lower(v.pronunciation) LIKE :substringPattern ESCAPE '\\' )
                          OR (v.ipa IS NOT NULL AND lower(v.ipa) LIKE :substringPattern ESCAPE '\\' )
                          OR (v.part_of_speech IS NOT NULL AND lower(v.part_of_speech) LIKE :substringPattern ESCAPE '\\' ) THEN 400
                        WHEN :enableFuzzy AND (
                            (lower(v.word) % :lowerQuery AND similarity(lower(v.word), :lowerQuery) >= 0.30)
                            OR (v.pronunciation IS NOT NULL AND similarity(lower(v.pronunciation), :lowerQuery) >= 0.30)
                            OR (v.ipa IS NOT NULL AND similarity(lower(v.ipa), :lowerQuery) >= 0.30)
                        ) THEN 350
                        WHEN (v.meaning IS NOT NULL AND lower(v.meaning) LIKE :substringPattern ESCAPE '\\' )
                          OR (v.example IS NOT NULL AND lower(v.example) LIKE :substringPattern ESCAPE '\\' )
                          OR (v.source_name IS NOT NULL AND lower(v.source_name) LIKE :substringPattern ESCAPE '\\' ) THEN 200
                        ELSE 0
                    END AS rank_bucket,
                    CASE
                        WHEN lower(v.word) = :lowerQuery THEN 0.0
                        WHEN lower(v.word) LIKE :prefixPattern ESCAPE '\\' THEN 0.0
                        WHEN lower(v.word) LIKE :substringPattern ESCAPE '\\' THEN 0.0
                        WHEN (v.pronunciation IS NOT NULL AND (lower(v.pronunciation) = :lowerQuery OR lower(v.pronunciation) LIKE :prefixPattern ESCAPE '\\' OR lower(v.pronunciation) LIKE :substringPattern ESCAPE '\\' ))
                          OR (v.ipa IS NOT NULL AND (lower(v.ipa) = :lowerQuery OR lower(v.ipa) LIKE :prefixPattern ESCAPE '\\' OR lower(v.ipa) LIKE :substringPattern ESCAPE '\\' ))
                          OR (v.part_of_speech IS NOT NULL AND (lower(v.part_of_speech) = :lowerQuery OR lower(v.part_of_speech) LIKE :prefixPattern ESCAPE '\\' OR lower(v.part_of_speech) LIKE :substringPattern ESCAPE '\\' )) THEN 0.0
                        WHEN :enableFuzzy AND (
                            (lower(v.word) % :lowerQuery AND similarity(lower(v.word), :lowerQuery) >= 0.30)
                            OR (v.pronunciation IS NOT NULL AND similarity(lower(v.pronunciation), :lowerQuery) >= 0.30)
                            OR (v.ipa IS NOT NULL AND similarity(lower(v.ipa), :lowerQuery) >= 0.30)
                        ) THEN GREATEST(
                            similarity(lower(v.word), :lowerQuery),
                            CASE WHEN v.pronunciation IS NOT NULL THEN similarity(lower(v.pronunciation), :lowerQuery) ELSE 0.0 END,
                            CASE WHEN v.ipa IS NOT NULL THEN similarity(lower(v.ipa), :lowerQuery) ELSE 0.0 END
                        )
                        ELSE 0.0
                    END AS similarity,
                    CASE
                        WHEN lower(v.word) = :lowerQuery THEN 'PRIMARY_EXACT'
                        WHEN lower(v.word) LIKE :prefixPattern ESCAPE '\\' THEN 'PRIMARY_PREFIX'
                        WHEN lower(v.word) LIKE :substringPattern ESCAPE '\\' THEN 'PRIMARY_SUBSTRING'
                        WHEN (v.pronunciation IS NOT NULL AND lower(v.pronunciation) = :lowerQuery)
                          OR (v.ipa IS NOT NULL AND lower(v.ipa) = :lowerQuery)
                          OR (v.part_of_speech IS NOT NULL AND lower(v.part_of_speech) = :lowerQuery) THEN 'SECONDARY_EXACT'
                        WHEN (v.pronunciation IS NOT NULL AND lower(v.pronunciation) LIKE :prefixPattern ESCAPE '\\' )
                          OR (v.ipa IS NOT NULL AND lower(v.ipa) LIKE :prefixPattern ESCAPE '\\' )
                          OR (v.part_of_speech IS NOT NULL AND lower(v.part_of_speech) LIKE :prefixPattern ESCAPE '\\' ) THEN 'SECONDARY_PREFIX'
                        WHEN (v.pronunciation IS NOT NULL AND lower(v.pronunciation) LIKE :substringPattern ESCAPE '\\' )
                          OR (v.ipa IS NOT NULL AND lower(v.ipa) LIKE :substringPattern ESCAPE '\\' )
                          OR (v.part_of_speech IS NOT NULL AND lower(v.part_of_speech) LIKE :substringPattern ESCAPE '\\' ) THEN 'SECONDARY_SUBSTRING'
                        WHEN :enableFuzzy AND (
                            (lower(v.word) % :lowerQuery AND similarity(lower(v.word), :lowerQuery) >= 0.30)
                            OR (v.pronunciation IS NOT NULL AND similarity(lower(v.pronunciation), :lowerQuery) >= 0.30)
                            OR (v.ipa IS NOT NULL AND similarity(lower(v.ipa), :lowerQuery) >= 0.30)
                        ) THEN 'SHORT_FUZZY'
                        WHEN (v.meaning IS NOT NULL AND lower(v.meaning) LIKE :substringPattern ESCAPE '\\' )
                          OR (v.example IS NOT NULL AND lower(v.example) LIKE :substringPattern ESCAPE '\\' )
                          OR (v.source_name IS NOT NULL AND lower(v.source_name) LIKE :substringPattern ESCAPE '\\' ) THEN 'BODY'
                        ELSE 'NONE'
                    END AS match_kind
                FROM vocabulary_items v
                WHERE (
                    lower(v.word) LIKE :substringPattern ESCAPE '\\'
                    OR (v.pronunciation IS NOT NULL AND lower(v.pronunciation) LIKE :substringPattern ESCAPE '\\' )
                    OR (v.ipa IS NOT NULL AND lower(v.ipa) LIKE :substringPattern ESCAPE '\\' )
                    OR (v.part_of_speech IS NOT NULL AND lower(v.part_of_speech) LIKE :substringPattern ESCAPE '\\' )
                    OR (:enableFuzzy AND (
                        (lower(v.word) % :lowerQuery AND similarity(lower(v.word), :lowerQuery) >= 0.30)
                        OR (v.pronunciation IS NOT NULL AND similarity(lower(v.pronunciation), :lowerQuery) >= 0.30)
                        OR (v.ipa IS NOT NULL AND similarity(lower(v.ipa), :lowerQuery) >= 0.30)
                    ))
                    OR (v.meaning IS NOT NULL AND lower(v.meaning) LIKE :substringPattern ESCAPE '\\' )
                    OR (v.example IS NOT NULL AND lower(v.example) LIKE :substringPattern ESCAPE '\\' )
                    OR (v.source_name IS NOT NULL AND lower(v.source_name) LIKE :substringPattern ESCAPE '\\' )
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
                    .addValue("lowerQuery", lowerQuery)
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
        String lowerCleaned = cleaned.toLowerCase();
        String lowerQuery = query.trim().toLowerCase();
        int index = lowerCleaned.indexOf(lowerQuery);
        if (index < 0) {
            return null;
        }
        if (cleaned.length() <= maxLength) {
            return cleaned;
        }

        int queryLen = lowerQuery.length();
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

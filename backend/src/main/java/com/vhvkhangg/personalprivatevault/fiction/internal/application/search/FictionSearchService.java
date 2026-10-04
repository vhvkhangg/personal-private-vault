package com.vhvkhangg.personalprivatevault.fiction.internal.application.search;

import com.vhvkhangg.personalprivatevault.fiction.search.FictionSearchDocument;
import com.vhvkhangg.personalprivatevault.fiction.search.FictionSearchHit;
import com.vhvkhangg.personalprivatevault.fiction.search.FictionSearchOperations;
import com.vhvkhangg.personalprivatevault.fiction.search.FictionSearchQuery;
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
public class FictionSearchService implements FictionSearchOperations {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final VaultSearchOperations vaultSearchOperations;

    @Override
    public List<FictionSearchHit> search(FictionSearchQuery query) {
        if (query == null) {
            throw new IllegalArgumentException("Query must not be null");
        }
        if (!query.entryTypes().isEmpty() && !query.entryTypes().contains(VaultEntryType.FICTION)) {
            return List.of();
        }

        jdbcTemplate.getJdbcTemplate().execute("SET LOCAL pg_trgm.similarity_threshold = 0.3;");

        String rawQuery = query.query().trim();
        String lowerQuery = rawQuery.toLowerCase();
        boolean enableFuzzy = rawQuery.length() >= 3;
        String prefixPattern = escapeLike(lowerQuery) + "%";
        String substringPattern = "%" + escapeLike(lowerQuery) + "%";

        int targetCount = query.limit();
        int pageSize = Math.min(Math.max(targetCount, 50), 100);
        int currentOffset = 0;

        List<FictionSearchHit> qualifyingHits = new ArrayList<>();

        String sql = """
                SELECT
                    f.id AS vault_entry_id,
                    CAST('FICTION' AS text) COLLATE "C" AS type_name,
                    f.title AS primary_text,
                    f.original_title AS secondary_text,
                    f.description AS description_text,
                    f.review AS review_text,
                    CASE
                        WHEN lower(f.title) = :lowerQuery THEN 600
                        WHEN lower(f.title) LIKE :prefixPattern ESCAPE '\\' THEN 550
                        WHEN lower(f.title) LIKE :substringPattern ESCAPE '\\' THEN 500
                        WHEN f.original_title IS NOT NULL AND lower(f.original_title) = :lowerQuery THEN 450
                        WHEN f.original_title IS NOT NULL AND lower(f.original_title) LIKE :prefixPattern ESCAPE '\\' THEN 450
                        WHEN f.original_title IS NOT NULL AND lower(f.original_title) LIKE :substringPattern ESCAPE '\\' THEN 400
                        WHEN :enableFuzzy AND (
                            (lower(f.title) % :lowerQuery AND similarity(lower(f.title), :lowerQuery) >= 0.30)
                            OR (f.original_title IS NOT NULL AND lower(f.original_title) % :lowerQuery AND similarity(lower(f.original_title), :lowerQuery) >= 0.30)
                        ) THEN 350
                        WHEN (f.description IS NOT NULL AND lower(f.description) LIKE :substringPattern ESCAPE '\\')
                          OR (f.review IS NOT NULL AND lower(f.review) LIKE :substringPattern ESCAPE '\\') THEN 200
                        ELSE 0
                    END AS rank_bucket,
                    CASE
                        WHEN lower(f.title) = :lowerQuery THEN 0.0
                        WHEN lower(f.title) LIKE :prefixPattern ESCAPE '\\' THEN 0.0
                        WHEN lower(f.title) LIKE :substringPattern ESCAPE '\\' THEN 0.0
                        WHEN f.original_title IS NOT NULL AND (
                            lower(f.original_title) = :lowerQuery
                            OR lower(f.original_title) LIKE :prefixPattern ESCAPE '\\'
                            OR lower(f.original_title) LIKE :substringPattern ESCAPE '\\'
                        ) THEN 0.0
                        WHEN :enableFuzzy AND (
                            (lower(f.title) % :lowerQuery AND similarity(lower(f.title), :lowerQuery) >= 0.30)
                            OR (f.original_title IS NOT NULL AND lower(f.original_title) % :lowerQuery AND similarity(lower(f.original_title), :lowerQuery) >= 0.30)
                        ) THEN GREATEST(
                            similarity(lower(f.title), :lowerQuery),
                            COALESCE(similarity(lower(f.original_title), :lowerQuery), 0.0)
                        )
                        ELSE 0.0
                    END AS similarity,
                    CASE
                        WHEN lower(f.title) = :lowerQuery THEN 'PRIMARY_EXACT'
                        WHEN lower(f.title) LIKE :prefixPattern ESCAPE '\\' THEN 'PRIMARY_PREFIX'
                        WHEN lower(f.title) LIKE :substringPattern ESCAPE '\\' THEN 'PRIMARY_SUBSTRING'
                        WHEN f.original_title IS NOT NULL AND lower(f.original_title) = :lowerQuery THEN 'SECONDARY_EXACT'
                        WHEN f.original_title IS NOT NULL AND lower(f.original_title) LIKE :prefixPattern ESCAPE '\\' THEN 'SECONDARY_PREFIX'
                        WHEN f.original_title IS NOT NULL AND lower(f.original_title) LIKE :substringPattern ESCAPE '\\' THEN 'SECONDARY_SUBSTRING'
                        WHEN :enableFuzzy AND (
                            (lower(f.title) % :lowerQuery AND similarity(lower(f.title), :lowerQuery) >= 0.30)
                            OR (f.original_title IS NOT NULL AND lower(f.original_title) % :lowerQuery AND similarity(lower(f.original_title), :lowerQuery) >= 0.30)
                        ) THEN 'SHORT_FUZZY'
                        WHEN (f.description IS NOT NULL AND lower(f.description) LIKE :substringPattern ESCAPE '\\')
                          OR (f.review IS NOT NULL AND lower(f.review) LIKE :substringPattern ESCAPE '\\') THEN 'BODY'
                        ELSE 'NONE'
                    END AS match_kind
                FROM fictions f
                WHERE (
                    lower(f.title) LIKE :substringPattern ESCAPE '\\'
                    OR (f.original_title IS NOT NULL AND lower(f.original_title) LIKE :substringPattern ESCAPE '\\')
                    OR (:enableFuzzy AND (
                        (lower(f.title) % :lowerQuery AND similarity(lower(f.title), :lowerQuery) >= 0.30)
                        OR (f.original_title IS NOT NULL AND lower(f.original_title) % :lowerQuery AND similarity(lower(f.original_title), :lowerQuery) >= 0.30)
                    ))
                    OR (f.description IS NOT NULL AND lower(f.description) LIKE :substringPattern ESCAPE '\\')
                    OR (f.review IS NOT NULL AND lower(f.review) LIKE :substringPattern ESCAPE '\\')
                )
                ORDER BY
                    rank_bucket DESC,
                    similarity DESC,
                    type_name ASC,
                    vault_entry_id ASC
                LIMIT :limit OFFSET :offset
                """;

        while (qualifyingHits.size() < targetCount) {
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
                    rs.getString("description_text"),
                    rs.getString("review_text"),
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
                    candidateIds, query.requiredTagIds());

            for (RawCandidate c : candidates) {
                if (qualifiedIds.contains(c.vaultEntryId())) {
                    String snippet = null;
                    if ("BODY".equals(c.matchKind())) {
                        snippet = extractSnippet(c.descriptionText(), rawQuery, 240);
                        if (snippet == null) {
                            snippet = extractSnippet(c.reviewText(), rawQuery, 240);
                        }
                    }
                    qualifyingHits.add(new FictionSearchHit(
                            c.vaultEntryId(),
                            c.entryType(),
                            c.primaryText(),
                            c.secondaryText(),
                            snippet,
                            c.rankBucket(),
                            c.similarity(),
                            c.matchKind()
                    ));
                    if (qualifyingHits.size() == targetCount) {
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
    public Map<Long, FictionSearchDocument> lookupDocuments(Set<Long> vaultEntryIds) {
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
        String sql = "SELECT f.id AS vault_entry_id, f.title AS primary_text, f.original_title AS secondary_text FROM fictions f WHERE f.id IN (:ids)";
        MapSqlParameterSource params = new MapSqlParameterSource("ids", vaultEntryIds);
        List<FictionSearchDocument> docs = jdbcTemplate.query(sql, params, (rs, rowNum) -> new FictionSearchDocument(
                rs.getLong("vault_entry_id"),
                VaultEntryType.FICTION,
                rs.getString("primary_text"),
                rs.getString("secondary_text")
        ));
        return docs.stream().collect(Collectors.toMap(FictionSearchDocument::vaultEntryId, Function.identity()));
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
            String descriptionText,
            String reviewText,
            int rankBucket,
            double similarity,
            String matchKind
    ) {}
}

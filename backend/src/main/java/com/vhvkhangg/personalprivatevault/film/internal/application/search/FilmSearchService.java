package com.vhvkhangg.personalprivatevault.film.internal.application.search;

import com.vhvkhangg.personalprivatevault.film.search.FilmSearchDocument;
import com.vhvkhangg.personalprivatevault.film.search.FilmSearchHit;
import com.vhvkhangg.personalprivatevault.film.search.FilmSearchOperations;
import com.vhvkhangg.personalprivatevault.film.search.FilmSearchQuery;
import com.vhvkhangg.personalprivatevault.vault.enums.VaultEntryType;
import com.vhvkhangg.personalprivatevault.vault.search.VaultSearchOperations;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FilmSearchService implements FilmSearchOperations {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final VaultSearchOperations vaultSearchOperations;

    @Override
    public List<FilmSearchHit> search(FilmSearchQuery query) {
        if (query == null) {
            throw new IllegalArgumentException("Query must not be null");
        }

        boolean includeFilms = query.entryTypes().isEmpty() || query.entryTypes().contains(VaultEntryType.FILM);
        boolean includeCredits = (query.entryTypes().isEmpty() || query.entryTypes().contains(VaultEntryType.FILM_CREDIT))
                && (query.requiredTagIds() == null || query.requiredTagIds().isEmpty());

        if (!includeFilms && !includeCredits) {
            return List.of();
        }

        jdbcTemplate.getJdbcTemplate().execute("SET LOCAL pg_trgm.similarity_threshold = 0.3;");

        String rawQuery = query.query().trim();
        boolean enableFuzzy = rawQuery.length() >= 3;
        String escapedRaw = escapeLike(rawQuery);
        String prefixPattern = escapedRaw + "%";
        String substringPattern = "%" + escapedRaw + "%";

        int targetCount = query.limit();
        int pageSize = Math.min(Math.max(targetCount, 50), 100);
        int currentOffset = 0;

        List<FilmSearchHit> qualifyingHits = new ArrayList<>();

        String filmSubquery = """
                SELECT
                    f.id AS vault_entry_id,
                    CAST('FILM' AS text) COLLATE "C" AS type_name,
                    f.title AS primary_text,
                    f.original_title AS secondary_text,
                    f.description AS description_text,
                    f.review AS review_text,
                    NULL AS note_text,
                    CASE
                        WHEN lower(f.title) = lower(CAST(:rawQuery AS text)) THEN 600
                        WHEN lower(f.title) LIKE lower(CAST(:prefixPattern AS text)) ESCAPE '\\' THEN 550
                        WHEN lower(f.title) LIKE lower(CAST(:substringPattern AS text)) ESCAPE '\\' THEN 500
                        WHEN f.original_title IS NOT NULL AND lower(f.original_title) = lower(CAST(:rawQuery AS text)) THEN 450
                        WHEN f.original_title IS NOT NULL AND lower(f.original_title) LIKE lower(CAST(:prefixPattern AS text)) ESCAPE '\\' THEN 450
                        WHEN f.original_title IS NOT NULL AND lower(f.original_title) LIKE lower(CAST(:substringPattern AS text)) ESCAPE '\\' THEN 400
                        WHEN :enableFuzzy AND (
                            (lower(f.title) % lower(CAST(:rawQuery AS text)) AND similarity(lower(f.title), lower(CAST(:rawQuery AS text))) >= 0.30)
                            OR (f.original_title IS NOT NULL AND lower(f.original_title) % lower(CAST(:rawQuery AS text)) AND similarity(lower(f.original_title), lower(CAST(:rawQuery AS text))) >= 0.30)
                        ) THEN 350
                        WHEN (f.description IS NOT NULL AND lower(f.description) LIKE lower(CAST(:substringPattern AS text)) ESCAPE '\\')
                          OR (f.review IS NOT NULL AND lower(f.review) LIKE lower(CAST(:substringPattern AS text)) ESCAPE '\\') THEN 200
                        ELSE 0
                    END AS rank_bucket,
                    CASE
                        WHEN lower(f.title) = lower(CAST(:rawQuery AS text)) THEN 0.0
                        WHEN lower(f.title) LIKE lower(CAST(:prefixPattern AS text)) ESCAPE '\\' THEN 0.0
                        WHEN lower(f.title) LIKE lower(CAST(:substringPattern AS text)) ESCAPE '\\' THEN 0.0
                        WHEN f.original_title IS NOT NULL AND (
                            lower(f.original_title) = lower(CAST(:rawQuery AS text))
                            OR lower(f.original_title) LIKE lower(CAST(:prefixPattern AS text)) ESCAPE '\\'
                            OR lower(f.original_title) LIKE lower(CAST(:substringPattern AS text)) ESCAPE '\\'
                        ) THEN 0.0
                        WHEN :enableFuzzy AND (
                            (lower(f.title) % lower(CAST(:rawQuery AS text)) AND similarity(lower(f.title), lower(CAST(:rawQuery AS text))) >= 0.30)
                            OR (f.original_title IS NOT NULL AND lower(f.original_title) % lower(CAST(:rawQuery AS text)) AND similarity(lower(f.original_title), lower(CAST(:rawQuery AS text))) >= 0.30)
                        ) THEN GREATEST(
                            similarity(lower(f.title), lower(CAST(:rawQuery AS text))),
                            COALESCE(similarity(lower(f.original_title), lower(CAST(:rawQuery AS text))), 0.0)
                        )
                        ELSE 0.0
                    END AS similarity,
                    CASE
                        WHEN lower(f.title) = lower(CAST(:rawQuery AS text)) THEN 'PRIMARY_EXACT'
                        WHEN lower(f.title) LIKE lower(CAST(:prefixPattern AS text)) ESCAPE '\\' THEN 'PRIMARY_PREFIX'
                        WHEN lower(f.title) LIKE lower(CAST(:substringPattern AS text)) ESCAPE '\\' THEN 'PRIMARY_SUBSTRING'
                        WHEN f.original_title IS NOT NULL AND lower(f.original_title) = lower(CAST(:rawQuery AS text)) THEN 'SECONDARY_EXACT'
                        WHEN f.original_title IS NOT NULL AND lower(f.original_title) LIKE lower(CAST(:prefixPattern AS text)) ESCAPE '\\' THEN 'SECONDARY_PREFIX'
                        WHEN f.original_title IS NOT NULL AND lower(f.original_title) LIKE lower(CAST(:substringPattern AS text)) ESCAPE '\\' THEN 'SECONDARY_SUBSTRING'
                        WHEN :enableFuzzy AND (
                            (lower(f.title) % lower(CAST(:rawQuery AS text)) AND similarity(lower(f.title), lower(CAST(:rawQuery AS text))) >= 0.30)
                            OR (f.original_title IS NOT NULL AND lower(f.original_title) % lower(CAST(:rawQuery AS text)) AND similarity(lower(f.original_title), lower(CAST(:rawQuery AS text))) >= 0.30)
                        ) THEN 'SHORT_FUZZY'
                        WHEN (f.description IS NOT NULL AND lower(f.description) LIKE lower(CAST(:substringPattern AS text)) ESCAPE '\\')
                          OR (f.review IS NOT NULL AND lower(f.review) LIKE lower(CAST(:substringPattern AS text)) ESCAPE '\\') THEN 'BODY'
                        ELSE 'NONE'
                    END AS match_kind
                FROM films f
                WHERE (
                    lower(f.title) LIKE lower(CAST(:substringPattern AS text)) ESCAPE '\\'
                    OR (f.original_title IS NOT NULL AND lower(f.original_title) LIKE lower(CAST(:substringPattern AS text)) ESCAPE '\\')
                    OR (:enableFuzzy AND (
                        (lower(f.title) % lower(CAST(:rawQuery AS text)) AND similarity(lower(f.title), lower(CAST(:rawQuery AS text))) >= 0.30)
                        OR (f.original_title IS NOT NULL AND lower(f.original_title) % lower(CAST(:rawQuery AS text)) AND similarity(lower(f.original_title), lower(CAST(:rawQuery AS text))) >= 0.30)
                    ))
                    OR (f.description IS NOT NULL AND lower(f.description) LIKE lower(CAST(:substringPattern AS text)) ESCAPE '\\')
                    OR (f.review IS NOT NULL AND lower(f.review) LIKE lower(CAST(:substringPattern AS text)) ESCAPE '\\')
                )
                """;

        String creditSubquery = """
                SELECT
                    fc.id AS vault_entry_id,
                    CAST('FILM_CREDIT' AS text) COLLATE "C" AS type_name,
                    fc.character_name AS primary_text,
                    NULL AS secondary_text,
                    NULL AS description_text,
                    NULL AS review_text,
                    fc.note AS note_text,
                    CASE
                        WHEN lower(fc.character_name) = lower(CAST(:rawQuery AS text)) THEN 600
                        WHEN lower(fc.character_name) LIKE lower(CAST(:prefixPattern AS text)) ESCAPE '\\' THEN 550
                        WHEN lower(fc.character_name) LIKE lower(CAST(:substringPattern AS text)) ESCAPE '\\' THEN 500
                        WHEN :enableFuzzy AND lower(fc.character_name) % lower(CAST(:rawQuery AS text)) AND similarity(lower(fc.character_name), lower(CAST(:rawQuery AS text))) >= 0.30 THEN 350
                        WHEN fc.note IS NOT NULL AND lower(fc.note) LIKE lower(CAST(:substringPattern AS text)) ESCAPE '\\' THEN 200
                        ELSE 0
                    END AS rank_bucket,
                    CASE
                        WHEN lower(fc.character_name) = lower(CAST(:rawQuery AS text)) THEN 0.0
                        WHEN lower(fc.character_name) LIKE lower(CAST(:prefixPattern AS text)) ESCAPE '\\' THEN 0.0
                        WHEN lower(fc.character_name) LIKE lower(CAST(:substringPattern AS text)) ESCAPE '\\' THEN 0.0
                        WHEN :enableFuzzy AND lower(fc.character_name) % lower(CAST(:rawQuery AS text)) AND similarity(lower(fc.character_name), lower(CAST(:rawQuery AS text))) >= 0.30 THEN similarity(lower(fc.character_name), lower(CAST(:rawQuery AS text)))
                        ELSE 0.0
                    END AS similarity,
                    CASE
                        WHEN lower(fc.character_name) = lower(CAST(:rawQuery AS text)) THEN 'PRIMARY_EXACT'
                        WHEN lower(fc.character_name) LIKE lower(CAST(:prefixPattern AS text)) ESCAPE '\\' THEN 'PRIMARY_PREFIX'
                        WHEN lower(fc.character_name) LIKE lower(CAST(:substringPattern AS text)) ESCAPE '\\' THEN 'PRIMARY_SUBSTRING'
                        WHEN :enableFuzzy AND lower(fc.character_name) % lower(CAST(:rawQuery AS text)) AND similarity(lower(fc.character_name), lower(CAST(:rawQuery AS text))) >= 0.30 THEN 'SHORT_FUZZY'
                        WHEN fc.note IS NOT NULL AND lower(fc.note) LIKE lower(CAST(:substringPattern AS text)) ESCAPE '\\' THEN 'BODY'
                        ELSE 'NONE'
                    END AS match_kind
                FROM film_credits fc
                WHERE (
                    lower(fc.character_name) LIKE lower(CAST(:substringPattern AS text)) ESCAPE '\\'
                    OR (:enableFuzzy AND lower(fc.character_name) % lower(CAST(:rawQuery AS text)) AND similarity(lower(fc.character_name), lower(CAST(:rawQuery AS text))) >= 0.30)
                    OR (fc.note IS NOT NULL AND lower(fc.note) LIKE lower(CAST(:substringPattern AS text)) ESCAPE '\\')
                )
                """;

        String sql;
        if (includeFilms && includeCredits) {
            sql = filmSubquery + " UNION ALL " + creditSubquery;
        } else if (includeFilms) {
            sql = filmSubquery;
        } else {
            sql = creditSubquery;
        }

        sql += """
                ORDER BY
                    rank_bucket DESC,
                    similarity DESC,
                    type_name ASC,
                    vault_entry_id ASC
                LIMIT :limit OFFSET :offset
                """;

        while (qualifyingHits.size() < targetCount) {
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
                    rs.getString("description_text"),
                    rs.getString("review_text"),
                    rs.getString("note_text"),
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
                        if (snippet == null) {
                            snippet = extractSnippet(c.noteText(), rawQuery, 240);
                        }
                    }
                    qualifyingHits.add(new FilmSearchHit(
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
    public Map<Long, FilmSearchDocument> lookupDocuments(Set<Long> vaultEntryIds) {
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
        MapSqlParameterSource params = new MapSqlParameterSource("ids", vaultEntryIds);
        Map<Long, FilmSearchDocument> results = new HashMap<>();

        String filmSql = "SELECT f.id AS vault_entry_id, f.title AS primary_text, f.original_title AS secondary_text FROM films f WHERE f.id IN (:ids)";
        jdbcTemplate.query(filmSql, params, (rs, rowNum) -> {
            long id = rs.getLong("vault_entry_id");
            results.put(id, new FilmSearchDocument(id, VaultEntryType.FILM, rs.getString("primary_text"), rs.getString("secondary_text")));
            return null;
        });

        String creditSql = "SELECT fc.id AS vault_entry_id, fc.character_name AS primary_text FROM film_credits fc WHERE fc.id IN (:ids)";
        jdbcTemplate.query(creditSql, params, (rs, rowNum) -> {
            long id = rs.getLong("vault_entry_id");
            results.put(id, new FilmSearchDocument(id, VaultEntryType.FILM_CREDIT, rs.getString("primary_text"), null));
            return null;
        });

        return results;
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
            String descriptionText,
            String reviewText,
            String noteText,
            int rankBucket,
            double similarity,
            String matchKind
    ) {}
}

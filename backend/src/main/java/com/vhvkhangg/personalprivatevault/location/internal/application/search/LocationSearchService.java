package com.vhvkhangg.personalprivatevault.location.internal.application.search;

import com.vhvkhangg.personalprivatevault.location.search.LocationSearchDocument;
import com.vhvkhangg.personalprivatevault.location.search.LocationSearchHit;
import com.vhvkhangg.personalprivatevault.location.search.LocationSearchOperations;
import com.vhvkhangg.personalprivatevault.location.search.LocationSearchQuery;
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
public class LocationSearchService implements LocationSearchOperations {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final VaultSearchOperations vaultSearchOperations;

    @Override
    public List<LocationSearchHit> search(LocationSearchQuery query) {
        if (query == null) {
            throw new IllegalArgumentException("Query must not be null");
        }

        boolean includeBrand = query.entryTypes().isEmpty() || query.entryTypes().contains(VaultEntryType.BRAND);
        boolean includeLocation = query.entryTypes().isEmpty() || query.entryTypes().contains(VaultEntryType.LOCATION);

        if (!includeBrand && !includeLocation) {
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

        List<LocationSearchHit> qualifyingHits = new ArrayList<>();

        String brandSubquery = """
                SELECT
                    b.id AS vault_entry_id,
                    CAST('BRAND' AS text) COLLATE "C" AS type_name,
                    b.name AS primary_text,
                    NULL AS secondary_text,
                    b.description AS description_text,
                    b.review AS review_text,
                    CASE
                        WHEN lower(b.name) = lower(CAST(:rawQuery AS text)) THEN 600
                        WHEN lower(b.name) LIKE lower(CAST(:prefixPattern AS text)) ESCAPE '\\' THEN 550
                        WHEN lower(b.name) LIKE lower(CAST(:substringPattern AS text)) ESCAPE '\\' THEN 500
                        WHEN :enableFuzzy AND lower(b.name) % lower(CAST(:rawQuery AS text)) AND similarity(lower(b.name), lower(CAST(:rawQuery AS text))) >= 0.30 THEN 350
                        WHEN (b.description IS NOT NULL AND lower(b.description) LIKE lower(CAST(:substringPattern AS text)) ESCAPE '\\')
                          OR (b.review IS NOT NULL AND lower(b.review) LIKE lower(CAST(:substringPattern AS text)) ESCAPE '\\') THEN 200
                        ELSE 0
                    END AS rank_bucket,
                    CASE
                        WHEN lower(b.name) = lower(CAST(:rawQuery AS text)) THEN 0.0
                        WHEN lower(b.name) LIKE lower(CAST(:prefixPattern AS text)) ESCAPE '\\' THEN 0.0
                        WHEN lower(b.name) LIKE lower(CAST(:substringPattern AS text)) ESCAPE '\\' THEN 0.0
                        WHEN :enableFuzzy AND lower(b.name) % lower(CAST(:rawQuery AS text)) AND similarity(lower(b.name), lower(CAST(:rawQuery AS text))) >= 0.30 THEN similarity(lower(b.name), lower(CAST(:rawQuery AS text)))
                        ELSE 0.0
                    END AS similarity,
                    CASE
                        WHEN lower(b.name) = lower(CAST(:rawQuery AS text)) THEN 'PRIMARY_EXACT'
                        WHEN lower(b.name) LIKE lower(CAST(:prefixPattern AS text)) ESCAPE '\\' THEN 'PRIMARY_PREFIX'
                        WHEN lower(b.name) LIKE lower(CAST(:substringPattern AS text)) ESCAPE '\\' THEN 'PRIMARY_SUBSTRING'
                        WHEN :enableFuzzy AND lower(b.name) % lower(CAST(:rawQuery AS text)) AND similarity(lower(b.name), lower(CAST(:rawQuery AS text))) >= 0.30 THEN 'SHORT_FUZZY'
                        WHEN (b.description IS NOT NULL AND lower(b.description) LIKE lower(CAST(:substringPattern AS text)) ESCAPE '\\')
                          OR (b.review IS NOT NULL AND lower(b.review) LIKE lower(CAST(:substringPattern AS text)) ESCAPE '\\') THEN 'BODY'
                        ELSE 'NONE'
                    END AS match_kind
                FROM brands b
                WHERE (
                    lower(b.name) LIKE lower(CAST(:substringPattern AS text)) ESCAPE '\\'
                    OR (:enableFuzzy AND lower(b.name) % lower(CAST(:rawQuery AS text)) AND similarity(lower(b.name), lower(CAST(:rawQuery AS text))) >= 0.30)
                    OR (b.description IS NOT NULL AND lower(b.description) LIKE lower(CAST(:substringPattern AS text)) ESCAPE '\\')
                    OR (b.review IS NOT NULL AND lower(b.review) LIKE lower(CAST(:substringPattern AS text)) ESCAPE '\\')
                )
                """;

        String locationSubquery = """
                SELECT
                    l.id AS vault_entry_id,
                    CAST('LOCATION' AS text) COLLATE "C" AS type_name,
                    l.name AS primary_text,
                    COALESCE(a.locality, a.street_address) AS secondary_text,
                    l.description AS description_text,
                    l.review AS review_text,
                    CASE
                        WHEN lower(l.name) = lower(CAST(:rawQuery AS text)) THEN 600
                        WHEN lower(l.name) LIKE lower(CAST(:prefixPattern AS text)) ESCAPE '\\' THEN 550
                        WHEN lower(l.name) LIKE lower(CAST(:substringPattern AS text)) ESCAPE '\\' THEN 500
                        WHEN (a.locality IS NOT NULL AND lower(a.locality) = lower(CAST(:rawQuery AS text)))
                          OR (a.street_address IS NOT NULL AND lower(a.street_address) = lower(CAST(:rawQuery AS text)))
                          OR (a.locality IS NOT NULL AND lower(a.locality) LIKE lower(CAST(:prefixPattern AS text)) ESCAPE '\\')
                          OR (a.street_address IS NOT NULL AND lower(a.street_address) LIKE lower(CAST(:prefixPattern AS text)) ESCAPE '\\') THEN 450
                        WHEN (a.locality IS NOT NULL AND lower(a.locality) LIKE lower(CAST(:substringPattern AS text)) ESCAPE '\\')
                          OR (a.street_address IS NOT NULL AND lower(a.street_address) LIKE lower(CAST(:substringPattern AS text)) ESCAPE '\\') THEN 400
                        WHEN :enableFuzzy AND lower(l.name) % lower(CAST(:rawQuery AS text)) AND similarity(lower(l.name), lower(CAST(:rawQuery AS text))) >= 0.30 THEN 350
                        WHEN (l.description IS NOT NULL AND lower(l.description) LIKE lower(CAST(:substringPattern AS text)) ESCAPE '\\')
                          OR (l.review IS NOT NULL AND lower(l.review) LIKE lower(CAST(:substringPattern AS text)) ESCAPE '\\') THEN 200
                        ELSE 0
                    END AS rank_bucket,
                    CASE
                        WHEN lower(l.name) = lower(CAST(:rawQuery AS text)) THEN 0.0
                        WHEN lower(l.name) LIKE lower(CAST(:prefixPattern AS text)) ESCAPE '\\' THEN 0.0
                        WHEN lower(l.name) LIKE lower(CAST(:substringPattern AS text)) ESCAPE '\\' THEN 0.0
                        WHEN (a.locality IS NOT NULL AND (lower(a.locality) = lower(CAST(:rawQuery AS text)) OR lower(a.locality) LIKE lower(CAST(:prefixPattern AS text)) ESCAPE '\\' OR lower(a.locality) LIKE lower(CAST(:substringPattern AS text)) ESCAPE '\\'))
                          OR (a.street_address IS NOT NULL AND (lower(a.street_address) = lower(CAST(:rawQuery AS text)) OR lower(a.street_address) LIKE lower(CAST(:prefixPattern AS text)) ESCAPE '\\' OR lower(a.street_address) LIKE lower(CAST(:substringPattern AS text)) ESCAPE '\\')) THEN 0.0
                        WHEN :enableFuzzy AND lower(l.name) % lower(CAST(:rawQuery AS text)) AND similarity(lower(l.name), lower(CAST(:rawQuery AS text))) >= 0.30 THEN similarity(lower(l.name), lower(CAST(:rawQuery AS text)))
                        ELSE 0.0
                    END AS similarity,
                    CASE
                        WHEN lower(l.name) = lower(CAST(:rawQuery AS text)) THEN 'PRIMARY_EXACT'
                        WHEN lower(l.name) LIKE lower(CAST(:prefixPattern AS text)) ESCAPE '\\' THEN 'PRIMARY_PREFIX'
                        WHEN lower(l.name) LIKE lower(CAST(:substringPattern AS text)) ESCAPE '\\' THEN 'PRIMARY_SUBSTRING'
                        WHEN (a.locality IS NOT NULL AND lower(a.locality) = lower(CAST(:rawQuery AS text)))
                          OR (a.street_address IS NOT NULL AND lower(a.street_address) = lower(CAST(:rawQuery AS text))) THEN 'SECONDARY_EXACT'
                        WHEN (a.locality IS NOT NULL AND lower(a.locality) LIKE lower(CAST(:prefixPattern AS text)) ESCAPE '\\')
                          OR (a.street_address IS NOT NULL AND lower(a.street_address) LIKE lower(CAST(:prefixPattern AS text)) ESCAPE '\\') THEN 'SECONDARY_PREFIX'
                        WHEN (a.locality IS NOT NULL AND lower(a.locality) LIKE lower(CAST(:substringPattern AS text)) ESCAPE '\\')
                          OR (a.street_address IS NOT NULL AND lower(a.street_address) LIKE lower(CAST(:substringPattern AS text)) ESCAPE '\\') THEN 'SECONDARY_SUBSTRING'
                        WHEN :enableFuzzy AND lower(l.name) % lower(CAST(:rawQuery AS text)) AND similarity(lower(l.name), lower(CAST(:rawQuery AS text))) >= 0.30 THEN 'SHORT_FUZZY'
                        WHEN (l.description IS NOT NULL AND lower(l.description) LIKE lower(CAST(:substringPattern AS text)) ESCAPE '\\')
                          OR (l.review IS NOT NULL AND lower(l.review) LIKE lower(CAST(:substringPattern AS text)) ESCAPE '\\') THEN 'BODY'
                        ELSE 'NONE'
                    END AS match_kind
                FROM locations l
                LEFT JOIN addresses a ON a.id = l.address_id
                WHERE (
                    lower(l.name) LIKE lower(CAST(:substringPattern AS text)) ESCAPE '\\'
                    OR (a.locality IS NOT NULL AND lower(a.locality) LIKE lower(CAST(:substringPattern AS text)) ESCAPE '\\')
                    OR (a.street_address IS NOT NULL AND lower(a.street_address) LIKE lower(CAST(:substringPattern AS text)) ESCAPE '\\')
                    OR (:enableFuzzy AND lower(l.name) % lower(CAST(:rawQuery AS text)) AND similarity(lower(l.name), lower(CAST(:rawQuery AS text))) >= 0.30)
                    OR (l.description IS NOT NULL AND lower(l.description) LIKE lower(CAST(:substringPattern AS text)) ESCAPE '\\')
                    OR (l.review IS NOT NULL AND lower(l.review) LIKE lower(CAST(:substringPattern AS text)) ESCAPE '\\')
                )
                """;

        String combinedSql;
        if (includeBrand && includeLocation) {
            combinedSql = "SELECT * FROM (" + brandSubquery + " UNION ALL " + locationSubquery + ") candidates"
                    + " ORDER BY rank_bucket DESC, similarity DESC, type_name ASC, vault_entry_id ASC LIMIT :limit OFFSET :offset";
        } else if (includeBrand) {
            combinedSql = "SELECT * FROM (" + brandSubquery + ") candidates"
                    + " ORDER BY rank_bucket DESC, similarity DESC, type_name ASC, vault_entry_id ASC LIMIT :limit OFFSET :offset";
        } else {
            combinedSql = "SELECT * FROM (" + locationSubquery + ") candidates"
                    + " ORDER BY rank_bucket DESC, similarity DESC, type_name ASC, vault_entry_id ASC LIMIT :limit OFFSET :offset";
        }

        while (qualifyingHits.size() < targetCount) {
            MapSqlParameterSource params = new MapSqlParameterSource()
                    .addValue("rawQuery", rawQuery)
                    .addValue("prefixPattern", prefixPattern)
                    .addValue("substringPattern", substringPattern)
                    .addValue("enableFuzzy", enableFuzzy)
                    .addValue("limit", pageSize)
                    .addValue("offset", currentOffset);

            List<RawCandidate> candidates = jdbcTemplate.query(combinedSql, params, (rs, rowNum) -> new RawCandidate(
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
                    qualifyingHits.add(new LocationSearchHit(
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
    public Map<Long, LocationSearchDocument> lookupDocuments(Set<Long> vaultEntryIds) {
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
        Map<Long, LocationSearchDocument> results = new HashMap<>();

        String brandSql = "SELECT b.id AS vault_entry_id, b.name AS primary_text FROM brands b WHERE b.id IN (:ids)";
        jdbcTemplate.query(brandSql, params, (rs, rowNum) -> {
            long id = rs.getLong("vault_entry_id");
            results.put(id, new LocationSearchDocument(id, VaultEntryType.BRAND, rs.getString("primary_text"), null));
            return null;
        });

        String locSql = """
                SELECT l.id AS vault_entry_id, l.name AS primary_text, COALESCE(a.locality, a.street_address) AS secondary_text
                FROM locations l
                LEFT JOIN addresses a ON a.id = l.address_id
                WHERE l.id IN (:ids)
                """;
        jdbcTemplate.query(locSql, params, (rs, rowNum) -> {
            long id = rs.getLong("vault_entry_id");
            results.put(id, new LocationSearchDocument(id, VaultEntryType.LOCATION, rs.getString("primary_text"), rs.getString("secondary_text")));
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
            int rankBucket,
            double similarity,
            String matchKind
    ) {}
}

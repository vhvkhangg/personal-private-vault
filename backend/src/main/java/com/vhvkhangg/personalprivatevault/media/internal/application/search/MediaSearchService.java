package com.vhvkhangg.personalprivatevault.media.internal.application.search;

import com.vhvkhangg.personalprivatevault.media.search.MediaSearchDocument;
import com.vhvkhangg.personalprivatevault.media.search.MediaSearchHit;
import com.vhvkhangg.personalprivatevault.media.search.MediaSearchOperations;
import com.vhvkhangg.personalprivatevault.media.search.MediaSearchQuery;
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
public class MediaSearchService implements MediaSearchOperations {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final VaultSearchOperations vaultSearchOperations;

    @Override
    public List<MediaSearchHit> search(MediaSearchQuery query) {
        if (query == null) {
            throw new IllegalArgumentException("Query must not be null");
        }

        boolean includeAlbum = query.entryTypes().isEmpty() || query.entryTypes().contains(VaultEntryType.ALBUM);
        boolean includeImage = query.entryTypes().isEmpty() || query.entryTypes().contains(VaultEntryType.IMAGE);

        if (!includeAlbum && !includeImage) {
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

        List<MediaSearchHit> qualifyingHits = new ArrayList<>();

        String albumSubquery = """
                SELECT
                    a.id AS vault_entry_id,
                    CAST('ALBUM' AS text) COLLATE "C" AS type_name,
                    a.title AS primary_text,
                    NULL AS secondary_text,
                    a.description AS description_text,
                    CASE
                        WHEN lower(a.title) = :lowerQuery THEN 600
                        WHEN lower(a.title) LIKE :prefixPattern ESCAPE '\\' THEN 550
                        WHEN lower(a.title) LIKE :substringPattern ESCAPE '\\' THEN 500
                        WHEN :enableFuzzy AND lower(a.title) % :lowerQuery AND similarity(lower(a.title), :lowerQuery) >= 0.30 THEN 350
                        WHEN a.description IS NOT NULL AND lower(a.description) LIKE :substringPattern ESCAPE '\\' THEN 200
                        ELSE 0
                    END AS rank_bucket,
                    CASE
                        WHEN lower(a.title) = :lowerQuery THEN 0.0
                        WHEN lower(a.title) LIKE :prefixPattern ESCAPE '\\' THEN 0.0
                        WHEN lower(a.title) LIKE :substringPattern ESCAPE '\\' THEN 0.0
                        WHEN :enableFuzzy AND lower(a.title) % :lowerQuery AND similarity(lower(a.title), :lowerQuery) >= 0.30 THEN similarity(lower(a.title), :lowerQuery)
                        ELSE 0.0
                    END AS similarity,
                    CASE
                        WHEN lower(a.title) = :lowerQuery THEN 'PRIMARY_EXACT'
                        WHEN lower(a.title) LIKE :prefixPattern ESCAPE '\\' THEN 'PRIMARY_PREFIX'
                        WHEN lower(a.title) LIKE :substringPattern ESCAPE '\\' THEN 'PRIMARY_SUBSTRING'
                        WHEN :enableFuzzy AND lower(a.title) % :lowerQuery AND similarity(lower(a.title), :lowerQuery) >= 0.30 THEN 'SHORT_FUZZY'
                        WHEN a.description IS NOT NULL AND lower(a.description) LIKE :substringPattern ESCAPE '\\' THEN 'BODY'
                        ELSE 'NONE'
                    END AS match_kind
                FROM albums a
                WHERE (
                    lower(a.title) LIKE :substringPattern ESCAPE '\\'
                    OR (:enableFuzzy AND lower(a.title) % :lowerQuery AND similarity(lower(a.title), :lowerQuery) >= 0.30)
                    OR (a.description IS NOT NULL AND lower(a.description) LIKE :substringPattern ESCAPE '\\')
                )
                """;

        String imageSubquery = """
                SELECT
                    i.id AS vault_entry_id,
                    CAST('IMAGE' AS text) COLLATE "C" AS type_name,
                    COALESCE(i.title, 'Image #' || i.id) AS primary_text,
                    i.location_text AS secondary_text,
                    NULL AS description_text,
                    CASE
                        WHEN i.title IS NOT NULL AND lower(i.title) = :lowerQuery THEN 600
                        WHEN i.title IS NOT NULL AND lower(i.title) LIKE :prefixPattern ESCAPE '\\' THEN 550
                        WHEN i.title IS NOT NULL AND lower(i.title) LIKE :substringPattern ESCAPE '\\' THEN 500
                        WHEN (i.location_text IS NOT NULL AND lower(i.location_text) = :lowerQuery)
                          OR (i.image_type IS NOT NULL AND lower(i.image_type) = :lowerQuery)
                          OR (i.location_text IS NOT NULL AND lower(i.location_text) LIKE :prefixPattern ESCAPE '\\')
                          OR (i.image_type IS NOT NULL AND lower(i.image_type) LIKE :prefixPattern ESCAPE '\\') THEN 450
                        WHEN (i.location_text IS NOT NULL AND lower(i.location_text) LIKE :substringPattern ESCAPE '\\')
                          OR (i.image_type IS NOT NULL AND lower(i.image_type) LIKE :substringPattern ESCAPE '\\') THEN 400
                        WHEN :enableFuzzy AND (
                            (i.title IS NOT NULL AND lower(i.title) % :lowerQuery AND similarity(lower(i.title), :lowerQuery) >= 0.30)
                            OR (i.location_text IS NOT NULL AND lower(i.location_text) % :lowerQuery AND similarity(lower(i.location_text), :lowerQuery) >= 0.30)
                        ) THEN 350
                        ELSE 0
                    END AS rank_bucket,
                    CASE
                        WHEN (i.title IS NOT NULL AND lower(i.title) = :lowerQuery)
                          OR (i.title IS NOT NULL AND lower(i.title) LIKE :prefixPattern ESCAPE '\\')
                          OR (i.title IS NOT NULL AND lower(i.title) LIKE :substringPattern ESCAPE '\\')
                          OR (i.location_text IS NOT NULL AND lower(i.location_text) LIKE :substringPattern ESCAPE '\\')
                          OR (i.image_type IS NOT NULL AND lower(i.image_type) LIKE :substringPattern ESCAPE '\\') THEN 0.0
                        WHEN :enableFuzzy AND (
                            (i.title IS NOT NULL AND lower(i.title) % :lowerQuery AND similarity(lower(i.title), :lowerQuery) >= 0.30)
                            OR (i.location_text IS NOT NULL AND lower(i.location_text) % :lowerQuery AND similarity(lower(i.location_text), :lowerQuery) >= 0.30)
                        ) THEN GREATEST(
                            CASE WHEN i.title IS NOT NULL AND lower(i.title) % :lowerQuery THEN similarity(lower(i.title), :lowerQuery) ELSE 0.0 END,
                            CASE WHEN i.location_text IS NOT NULL AND lower(i.location_text) % :lowerQuery THEN similarity(lower(i.location_text), :lowerQuery) ELSE 0.0 END
                        )
                        ELSE 0.0
                    END AS similarity,
                    CASE
                        WHEN i.title IS NOT NULL AND lower(i.title) = :lowerQuery THEN 'PRIMARY_EXACT'
                        WHEN i.title IS NOT NULL AND lower(i.title) LIKE :prefixPattern ESCAPE '\\' THEN 'PRIMARY_PREFIX'
                        WHEN i.title IS NOT NULL AND lower(i.title) LIKE :substringPattern ESCAPE '\\' THEN 'PRIMARY_SUBSTRING'
                        WHEN (i.location_text IS NOT NULL AND lower(i.location_text) = :lowerQuery)
                          OR (i.image_type IS NOT NULL AND lower(i.image_type) = :lowerQuery) THEN 'SECONDARY_EXACT'
                        WHEN (i.location_text IS NOT NULL AND lower(i.location_text) LIKE :prefixPattern ESCAPE '\\')
                          OR (i.image_type IS NOT NULL AND lower(i.image_type) LIKE :prefixPattern ESCAPE '\\') THEN 'SECONDARY_PREFIX'
                        WHEN (i.location_text IS NOT NULL AND lower(i.location_text) LIKE :substringPattern ESCAPE '\\')
                          OR (i.image_type IS NOT NULL AND lower(i.image_type) LIKE :substringPattern ESCAPE '\\') THEN 'SECONDARY_SUBSTRING'
                        WHEN :enableFuzzy AND (
                            (i.title IS NOT NULL AND lower(i.title) % :lowerQuery AND similarity(lower(i.title), :lowerQuery) >= 0.30)
                            OR (i.location_text IS NOT NULL AND lower(i.location_text) % :lowerQuery AND similarity(lower(i.location_text), :lowerQuery) >= 0.30)
                        ) THEN 'SHORT_FUZZY'
                        ELSE 'NONE'
                    END AS match_kind
                FROM images i
                WHERE (
                    (i.title IS NOT NULL AND lower(i.title) LIKE :substringPattern ESCAPE '\\')
                    OR (i.location_text IS NOT NULL AND lower(i.location_text) LIKE :substringPattern ESCAPE '\\')
                    OR (i.image_type IS NOT NULL AND lower(i.image_type) LIKE :substringPattern ESCAPE '\\')
                    OR (:enableFuzzy AND (
                        (i.title IS NOT NULL AND lower(i.title) % :lowerQuery AND similarity(lower(i.title), :lowerQuery) >= 0.30)
                        OR (i.location_text IS NOT NULL AND lower(i.location_text) % :lowerQuery AND similarity(lower(i.location_text), :lowerQuery) >= 0.30)
                    ))
                )
                """;

        String combinedSql;
        if (includeAlbum && includeImage) {
            combinedSql = "SELECT * FROM (" + albumSubquery + " UNION ALL " + imageSubquery + ") candidates"
                    + " ORDER BY rank_bucket DESC, similarity DESC, type_name ASC, vault_entry_id ASC LIMIT :limit OFFSET :offset";
        } else if (includeAlbum) {
            combinedSql = "SELECT * FROM (" + albumSubquery + ") candidates"
                    + " ORDER BY rank_bucket DESC, similarity DESC, type_name ASC, vault_entry_id ASC LIMIT :limit OFFSET :offset";
        } else {
            combinedSql = "SELECT * FROM (" + imageSubquery + ") candidates"
                    + " ORDER BY rank_bucket DESC, similarity DESC, type_name ASC, vault_entry_id ASC LIMIT :limit OFFSET :offset";
        }

        while (qualifyingHits.size() < targetCount) {
            MapSqlParameterSource params = new MapSqlParameterSource()
                    .addValue("lowerQuery", lowerQuery)
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
                    String snippet = "BODY".equals(c.matchKind()) ? extractSnippet(c.descriptionText(), rawQuery, 240) : null;
                    qualifyingHits.add(new MediaSearchHit(
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
    public Map<Long, MediaSearchDocument> lookupDocuments(Set<Long> vaultEntryIds) {
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
        Map<Long, MediaSearchDocument> results = new HashMap<>();

        String albumSql = "SELECT a.id AS vault_entry_id, a.title AS primary_text FROM albums a WHERE a.id IN (:ids)";
        jdbcTemplate.query(albumSql, params, (rs, rowNum) -> {
            long id = rs.getLong("vault_entry_id");
            results.put(id, new MediaSearchDocument(id, VaultEntryType.ALBUM, rs.getString("primary_text"), null));
            return null;
        });

        String imageSql = "SELECT i.id AS vault_entry_id, COALESCE(i.title, 'Image #' || i.id) AS primary_text, i.location_text AS secondary_text FROM images i WHERE i.id IN (:ids)";
        jdbcTemplate.query(imageSql, params, (rs, rowNum) -> {
            long id = rs.getLong("vault_entry_id");
            results.put(id, new MediaSearchDocument(id, VaultEntryType.IMAGE, rs.getString("primary_text"), rs.getString("secondary_text")));
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
            int rankBucket,
            double similarity,
            String matchKind
    ) {}
}

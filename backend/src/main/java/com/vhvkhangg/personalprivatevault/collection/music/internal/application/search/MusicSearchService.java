package com.vhvkhangg.personalprivatevault.collection.music.internal.application.search;

import com.vhvkhangg.personalprivatevault.collection.music.search.MusicSearchDocument;
import com.vhvkhangg.personalprivatevault.collection.music.search.MusicSearchHit;
import com.vhvkhangg.personalprivatevault.collection.music.search.MusicSearchOperations;
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
public class MusicSearchService implements MusicSearchOperations {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final VaultSearchOperations vaultSearchOperations;

    @Override
    public List<MusicSearchHit> search(String query, Set<Long> requiredTagIds, int limit) {
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

        List<MusicSearchHit> qualifyingHits = new ArrayList<>();

        String sql = """
                SELECT
                    m.id AS vault_entry_id,
                    CAST('MUSIC' AS text) COLLATE "C" AS type_name,
                    m.title AS primary_text,
                    NULL AS secondary_text,
                    CASE
                        WHEN lower(m.title) = lower(CAST(:rawQuery AS text)) THEN 600
                        WHEN lower(m.title) LIKE lower(CAST(:prefixPattern AS text)) ESCAPE '\\' THEN 550
                        WHEN lower(m.title) LIKE lower(CAST(:substringPattern AS text)) ESCAPE '\\' THEN 500
                        WHEN :enableFuzzy AND lower(m.title) % lower(CAST(:rawQuery AS text)) AND similarity(lower(m.title), lower(CAST(:rawQuery AS text))) >= 0.30 THEN 350
                        ELSE 0
                    END AS rank_bucket,
                    CASE
                        WHEN lower(m.title) = lower(CAST(:rawQuery AS text)) THEN 0.0
                        WHEN lower(m.title) LIKE lower(CAST(:prefixPattern AS text)) ESCAPE '\\' THEN 0.0
                        WHEN lower(m.title) LIKE lower(CAST(:substringPattern AS text)) ESCAPE '\\' THEN 0.0
                        WHEN :enableFuzzy AND lower(m.title) % lower(CAST(:rawQuery AS text)) AND similarity(lower(m.title), lower(CAST(:rawQuery AS text))) >= 0.30 THEN similarity(lower(m.title), lower(CAST(:rawQuery AS text)))
                        ELSE 0.0
                    END AS similarity,
                    CASE
                        WHEN lower(m.title) = lower(CAST(:rawQuery AS text)) THEN 'PRIMARY_EXACT'
                        WHEN lower(m.title) LIKE lower(CAST(:prefixPattern AS text)) ESCAPE '\\' THEN 'PRIMARY_PREFIX'
                        WHEN lower(m.title) LIKE lower(CAST(:substringPattern AS text)) ESCAPE '\\' THEN 'PRIMARY_SUBSTRING'
                        WHEN :enableFuzzy AND lower(m.title) % lower(CAST(:rawQuery AS text)) AND similarity(lower(m.title), lower(CAST(:rawQuery AS text))) >= 0.30 THEN 'SHORT_FUZZY'
                        ELSE 'NONE'
                    END AS match_kind
                FROM music_tracks m
                WHERE (
                    lower(m.title) LIKE lower(CAST(:substringPattern AS text)) ESCAPE '\\'
                    OR (:enableFuzzy AND lower(m.title) % lower(CAST(:rawQuery AS text)) AND similarity(lower(m.title), lower(CAST(:rawQuery AS text))) >= 0.30)
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
                    qualifyingHits.add(new MusicSearchHit(
                            c.vaultEntryId(),
                            c.entryType(),
                            c.primaryText(),
                            c.secondaryText(),
                            null,
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
    public Map<Long, MusicSearchDocument> lookupDocuments(Set<Long> vaultEntryIds) {
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
        String sql = "SELECT m.id AS vault_entry_id, m.title AS primary_text FROM music_tracks m WHERE m.id IN (:ids)";
        MapSqlParameterSource params = new MapSqlParameterSource("ids", vaultEntryIds);
        List<MusicSearchDocument> docs = jdbcTemplate.query(sql, params, (rs, rowNum) -> new MusicSearchDocument(
                rs.getLong("vault_entry_id"),
                VaultEntryType.MUSIC,
                rs.getString("primary_text"),
                null
        ));
        return docs.stream().collect(Collectors.toMap(MusicSearchDocument::vaultEntryId, Function.identity()));
    }

    private static String escapeLike(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }

    private record RawCandidate(
            Long vaultEntryId,
            VaultEntryType entryType,
            String primaryText,
            String secondaryText,
            int rankBucket,
            double similarity,
            String matchKind
    ) {}
}

package com.vhvkhangg.personalprivatevault.vault.internal.application.search;

import com.vhvkhangg.personalprivatevault.vault.enums.VaultEntryType;
import com.vhvkhangg.personalprivatevault.vault.search.VaultSearchOperations;
import com.vhvkhangg.personalprivatevault.vault.search.VaultTagCandidateHit;
import com.vhvkhangg.personalprivatevault.vault.search.VaultTagSearchQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VaultSearchService implements VaultSearchOperations {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    private static final Set<String> ALL_TAGGABLE_ENTRY_TYPE_NAMES = Arrays.stream(VaultEntryType.values())
            .filter(t -> t != VaultEntryType.FILM_CREDIT)
            .map(Enum::name)
            .collect(Collectors.toUnmodifiableSet());

    @Override
    public Set<Long> filterQualifyingActiveEntries(Set<Long> candidateIds, Set<Long> requiredTagIds) {
        if (requiredTagIds != null) {
            if (requiredTagIds.size() > 10) {
                throw new IllegalArgumentException("At most 10 required tag IDs may be provided");
            }
            for (Long tagId : requiredTagIds) {
                if (tagId == null || tagId <= 0) {
                    throw new IllegalArgumentException("Tag ID must be a positive number");
                }
            }
        }

        if (candidateIds == null || candidateIds.isEmpty()) {
            return Set.of();
        }
        if (candidateIds.size() > 601) {
            throw new IllegalArgumentException("Candidate IDs batch size must not exceed 601");
        }
        for (Long id : candidateIds) {
            if (id == null || id <= 0) {
                throw new IllegalArgumentException("Candidate ID must be a positive number");
            }
        }

        if (requiredTagIds == null || requiredTagIds.isEmpty()) {
            String sql = "SELECT ve.id FROM vault_entries ve WHERE ve.id IN (:ids) AND ve.deleted_at IS NULL";
            MapSqlParameterSource params = new MapSqlParameterSource("ids", candidateIds);
            List<Long> result = jdbcTemplate.query(sql, params, (rs, rowNum) -> rs.getLong("id"));
            return new HashSet<>(result);
        }

        String sql = """
                SELECT ve.id
                FROM vault_entries ve
                JOIN vault_entry_tags vet ON vet.vault_entry_id = ve.id
                WHERE ve.id IN (:ids)
                  AND ve.deleted_at IS NULL
                  AND vet.tag_id IN (:tagIds)
                GROUP BY ve.id
                HAVING COUNT(DISTINCT vet.tag_id) = :tagCount
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("ids", candidateIds)
                .addValue("tagIds", requiredTagIds)
                .addValue("tagCount", requiredTagIds.size());

        List<Long> result = jdbcTemplate.query(sql, params, (rs, rowNum) -> rs.getLong("id"));
        return new HashSet<>(result);
    }

    @Override
    public List<VaultTagCandidateHit> searchByTag(VaultTagSearchQuery query) {
        if (query == null) {
            throw new IllegalArgumentException("Query must not be null");
        }

        Set<String> effectiveTypeNames;
        if (query.entryTypes() == null || query.entryTypes().isEmpty()) {
            effectiveTypeNames = ALL_TAGGABLE_ENTRY_TYPE_NAMES;
        } else {
            effectiveTypeNames = query.entryTypes().stream()
                    .filter(t -> t != VaultEntryType.FILM_CREDIT)
                    .map(Enum::name)
                    .collect(Collectors.toSet());
        }

        if (effectiveTypeNames.isEmpty()) {
            return List.of();
        }

        jdbcTemplate.getJdbcTemplate().execute("SET LOCAL pg_trgm.similarity_threshold = 0.3;");

        String rawQuery = query.query().trim();
        boolean enableFuzzy = rawQuery.length() >= 3;
        String escapedRaw = escapeLike(rawQuery);
        String substringPattern = "%" + escapedRaw + "%";

        StringBuilder sql = new StringBuilder("""
                SELECT
                    ve.id AS vault_entry_id,
                    CAST(ve.entry_type AS text) COLLATE "C" AS type_name,
                    MAX(similarity(lower(t.name), lower(CAST(:rawQuery AS text)))) AS best_similarity,
                    MIN(t.name) AS matched_tag_name
                FROM tags t
                JOIN vault_entry_tags vet ON vet.tag_id = t.id
                JOIN vault_entries ve ON ve.id = vet.vault_entry_id
                WHERE ve.deleted_at IS NULL
                  AND CAST(ve.entry_type AS text) != 'FILM_CREDIT'
                  AND CAST(ve.entry_type AS text) IN (:entryTypes)
                  AND (
                      lower(t.name) LIKE lower(CAST(:substringPattern AS text)) ESCAPE '\\'
                      OR (:enableFuzzy AND lower(t.name) % lower(CAST(:rawQuery AS text)) AND similarity(lower(t.name), lower(CAST(:rawQuery AS text))) >= 0.30)
                  )
                """);

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("rawQuery", rawQuery)
                .addValue("substringPattern", substringPattern)
                .addValue("enableFuzzy", enableFuzzy)
                .addValue("entryTypes", effectiveTypeNames)
                .addValue("limit", query.limit());

        if (query.requiredTagIds() != null && !query.requiredTagIds().isEmpty()) {
            sql.append("""
                  AND ve.id IN (
                      SELECT vet2.vault_entry_id
                      FROM vault_entry_tags vet2
                      WHERE vet2.tag_id IN (:requiredTagIds)
                      GROUP BY vet2.vault_entry_id
                      HAVING COUNT(DISTINCT vet2.tag_id) = :requiredTagCount
                  )
                """);
            params.addValue("requiredTagIds", query.requiredTagIds())
                    .addValue("requiredTagCount", query.requiredTagIds().size());
        }

        sql.append("""
                GROUP BY ve.id, CAST(ve.entry_type AS text) COLLATE "C"
                ORDER BY
                    best_similarity DESC,
                    type_name ASC,
                    ve.id ASC
                LIMIT :limit
                """);

        return jdbcTemplate.query(sql.toString(), params, (rs, rowNum) -> new VaultTagCandidateHit(
                rs.getLong("vault_entry_id"),
                VaultEntryType.valueOf(rs.getString("type_name")),
                rs.getDouble("best_similarity"),
                rs.getString("matched_tag_name")
        ));
    }

    private static String escapeLike(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }
}

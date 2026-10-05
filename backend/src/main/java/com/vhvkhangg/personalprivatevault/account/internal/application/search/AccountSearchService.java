package com.vhvkhangg.personalprivatevault.account.internal.application.search;

import com.vhvkhangg.personalprivatevault.account.search.AccountSearchDocument;
import com.vhvkhangg.personalprivatevault.account.search.AccountSearchHit;
import com.vhvkhangg.personalprivatevault.account.search.AccountSearchOperations;
import com.vhvkhangg.personalprivatevault.account.search.AccountSearchQuery;
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
public class AccountSearchService implements AccountSearchOperations {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final VaultSearchOperations vaultSearchOperations;

    @Override
    public List<AccountSearchHit> search(AccountSearchQuery query) {
        if (query == null) {
            throw new IllegalArgumentException("Query must not be null");
        }
        if (!query.entryTypes().isEmpty() && !query.entryTypes().contains(VaultEntryType.EXTERNAL_ACCOUNT)) {
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

        List<AccountSearchHit> qualifyingHits = new ArrayList<>();

        String sql = """
                SELECT
                    ea.id AS vault_entry_id,
                    CAST('EXTERNAL_ACCOUNT' AS text) COLLATE "C" AS type_name,
                    COALESCE(ea.display_name, ea.username) AS primary_text,
                    ea.username AS secondary_text,
                    ea.profile_description AS desc_text,
                    ea.notes AS notes_text,
                    CASE
                        WHEN ea.display_name IS NOT NULL AND lower(ea.display_name) = lower(CAST(:rawQuery AS text)) THEN 600
                        WHEN ea.display_name IS NOT NULL AND lower(ea.display_name) LIKE lower(CAST(:prefixPattern AS text)) ESCAPE '\\' THEN 550
                        WHEN ea.display_name IS NOT NULL AND lower(ea.display_name) LIKE lower(CAST(:substringPattern AS text)) ESCAPE '\\' THEN 500
                        WHEN (ea.username IS NOT NULL AND lower(ea.username) = lower(CAST(:rawQuery AS text)))
                          OR (ea.owner_name IS NOT NULL AND lower(ea.owner_name) = lower(CAST(:rawQuery AS text)))
                          OR (ea.external_id IS NOT NULL AND lower(ea.external_id) = lower(CAST(:rawQuery AS text)))
                          OR (ea.username IS NOT NULL AND lower(ea.username) LIKE lower(CAST(:prefixPattern AS text)) ESCAPE '\\')
                          OR (ea.owner_name IS NOT NULL AND lower(ea.owner_name) LIKE lower(CAST(:prefixPattern AS text)) ESCAPE '\\')
                          OR (ea.external_id IS NOT NULL AND lower(ea.external_id) LIKE lower(CAST(:prefixPattern AS text)) ESCAPE '\\') THEN 450
                        WHEN (ea.username IS NOT NULL AND lower(ea.username) LIKE lower(CAST(:substringPattern AS text)) ESCAPE '\\')
                          OR (ea.owner_name IS NOT NULL AND lower(ea.owner_name) LIKE lower(CAST(:substringPattern AS text)) ESCAPE '\\')
                          OR (ea.external_id IS NOT NULL AND lower(ea.external_id) LIKE lower(CAST(:substringPattern AS text)) ESCAPE '\\') THEN 400
                        WHEN :enableFuzzy AND (
                            (ea.display_name IS NOT NULL AND lower(ea.display_name) % lower(CAST(:rawQuery AS text)) AND similarity(lower(ea.display_name), lower(CAST(:rawQuery AS text))) >= 0.30)
                            OR (ea.username IS NOT NULL AND lower(ea.username) % lower(CAST(:rawQuery AS text)) AND similarity(lower(ea.username), lower(CAST(:rawQuery AS text))) >= 0.30)
                            OR (ea.owner_name IS NOT NULL AND lower(ea.owner_name) % lower(CAST(:rawQuery AS text)) AND similarity(lower(ea.owner_name), lower(CAST(:rawQuery AS text))) >= 0.30)
                        ) THEN 350
                        WHEN (ea.profile_description IS NOT NULL AND lower(ea.profile_description) LIKE lower(CAST(:substringPattern AS text)) ESCAPE '\\')
                          OR (ea.notes IS NOT NULL AND lower(ea.notes) LIKE lower(CAST(:substringPattern AS text)) ESCAPE '\\') THEN 200
                        ELSE 0
                    END AS rank_bucket,
                    CASE
                        WHEN ea.display_name IS NOT NULL AND (
                            lower(ea.display_name) = lower(CAST(:rawQuery AS text))
                            OR lower(ea.display_name) LIKE lower(CAST(:prefixPattern AS text)) ESCAPE '\\'
                            OR lower(ea.display_name) LIKE lower(CAST(:substringPattern AS text)) ESCAPE '\\'
                        ) THEN 0.0
                        WHEN (ea.username IS NOT NULL AND (lower(ea.username) = lower(CAST(:rawQuery AS text)) OR lower(ea.username) LIKE lower(CAST(:prefixPattern AS text)) ESCAPE '\\' OR lower(ea.username) LIKE lower(CAST(:substringPattern AS text)) ESCAPE '\\'))
                          OR (ea.owner_name IS NOT NULL AND (lower(ea.owner_name) = lower(CAST(:rawQuery AS text)) OR lower(ea.owner_name) LIKE lower(CAST(:prefixPattern AS text)) ESCAPE '\\' OR lower(ea.owner_name) LIKE lower(CAST(:substringPattern AS text)) ESCAPE '\\'))
                          OR (ea.external_id IS NOT NULL AND (lower(ea.external_id) = lower(CAST(:rawQuery AS text)) OR lower(ea.external_id) LIKE lower(CAST(:prefixPattern AS text)) ESCAPE '\\' OR lower(ea.external_id) LIKE lower(CAST(:substringPattern AS text)) ESCAPE '\\')) THEN 0.0
                        WHEN :enableFuzzy AND (
                            (ea.display_name IS NOT NULL AND lower(ea.display_name) % lower(CAST(:rawQuery AS text)) AND similarity(lower(ea.display_name), lower(CAST(:rawQuery AS text))) >= 0.30)
                            OR (ea.username IS NOT NULL AND lower(ea.username) % lower(CAST(:rawQuery AS text)) AND similarity(lower(ea.username), lower(CAST(:rawQuery AS text))) >= 0.30)
                            OR (ea.owner_name IS NOT NULL AND lower(ea.owner_name) % lower(CAST(:rawQuery AS text)) AND similarity(lower(ea.owner_name), lower(CAST(:rawQuery AS text))) >= 0.30)
                        ) THEN GREATEST(
                            CASE WHEN ea.display_name IS NOT NULL THEN similarity(lower(ea.display_name), lower(CAST(:rawQuery AS text))) ELSE 0.0 END,
                            CASE WHEN ea.username IS NOT NULL THEN similarity(lower(ea.username), lower(CAST(:rawQuery AS text))) ELSE 0.0 END,
                            CASE WHEN ea.owner_name IS NOT NULL THEN similarity(lower(ea.owner_name), lower(CAST(:rawQuery AS text))) ELSE 0.0 END
                        )
                        ELSE 0.0
                    END AS similarity,
                    CASE
                        WHEN ea.display_name IS NOT NULL AND lower(ea.display_name) = lower(CAST(:rawQuery AS text)) THEN 'PRIMARY_EXACT'
                        WHEN ea.display_name IS NOT NULL AND lower(ea.display_name) LIKE lower(CAST(:prefixPattern AS text)) ESCAPE '\\' THEN 'PRIMARY_PREFIX'
                        WHEN ea.display_name IS NOT NULL AND lower(ea.display_name) LIKE lower(CAST(:substringPattern AS text)) ESCAPE '\\' THEN 'PRIMARY_SUBSTRING'
                        WHEN (ea.username IS NOT NULL AND lower(ea.username) = lower(CAST(:rawQuery AS text)))
                          OR (ea.owner_name IS NOT NULL AND lower(ea.owner_name) = lower(CAST(:rawQuery AS text)))
                          OR (ea.external_id IS NOT NULL AND lower(ea.external_id) = lower(CAST(:rawQuery AS text))) THEN 'SECONDARY_EXACT'
                        WHEN (ea.username IS NOT NULL AND lower(ea.username) LIKE lower(CAST(:prefixPattern AS text)) ESCAPE '\\')
                          OR (ea.owner_name IS NOT NULL AND lower(ea.owner_name) LIKE lower(CAST(:prefixPattern AS text)) ESCAPE '\\')
                          OR (ea.external_id IS NOT NULL AND lower(ea.external_id) LIKE lower(CAST(:prefixPattern AS text)) ESCAPE '\\') THEN 'SECONDARY_PREFIX'
                        WHEN (ea.username IS NOT NULL AND lower(ea.username) LIKE lower(CAST(:substringPattern AS text)) ESCAPE '\\')
                          OR (ea.owner_name IS NOT NULL AND lower(ea.owner_name) LIKE lower(CAST(:substringPattern AS text)) ESCAPE '\\')
                          OR (ea.external_id IS NOT NULL AND lower(ea.external_id) LIKE lower(CAST(:substringPattern AS text)) ESCAPE '\\') THEN 'SECONDARY_SUBSTRING'
                        WHEN :enableFuzzy AND (
                            (ea.display_name IS NOT NULL AND lower(ea.display_name) % lower(CAST(:rawQuery AS text)) AND similarity(lower(ea.display_name), lower(CAST(:rawQuery AS text))) >= 0.30)
                            OR (ea.username IS NOT NULL AND lower(ea.username) % lower(CAST(:rawQuery AS text)) AND similarity(lower(ea.username), lower(CAST(:rawQuery AS text))) >= 0.30)
                            OR (ea.owner_name IS NOT NULL AND lower(ea.owner_name) % lower(CAST(:rawQuery AS text)) AND similarity(lower(ea.owner_name), lower(CAST(:rawQuery AS text))) >= 0.30)
                        ) THEN 'SHORT_FUZZY'
                        WHEN (ea.profile_description IS NOT NULL AND lower(ea.profile_description) LIKE lower(CAST(:substringPattern AS text)) ESCAPE '\\')
                          OR (ea.notes IS NOT NULL AND lower(ea.notes) LIKE lower(CAST(:substringPattern AS text)) ESCAPE '\\') THEN 'BODY'
                        ELSE 'NONE'
                    END AS match_kind
                FROM external_accounts ea
                WHERE (
                    (ea.display_name IS NOT NULL AND lower(ea.display_name) LIKE lower(CAST(:substringPattern AS text)) ESCAPE '\\')
                    OR (ea.username IS NOT NULL AND lower(ea.username) LIKE lower(CAST(:substringPattern AS text)) ESCAPE '\\')
                    OR (ea.owner_name IS NOT NULL AND lower(ea.owner_name) LIKE lower(CAST(:substringPattern AS text)) ESCAPE '\\')
                    OR (ea.external_id IS NOT NULL AND lower(ea.external_id) LIKE lower(CAST(:substringPattern AS text)) ESCAPE '\\')
                    OR (:enableFuzzy AND (
                        (ea.display_name IS NOT NULL AND lower(ea.display_name) % lower(CAST(:rawQuery AS text)) AND similarity(lower(ea.display_name), lower(CAST(:rawQuery AS text))) >= 0.30)
                        OR (ea.username IS NOT NULL AND lower(ea.username) % lower(CAST(:rawQuery AS text)) AND similarity(lower(ea.username), lower(CAST(:rawQuery AS text))) >= 0.30)
                        OR (ea.owner_name IS NOT NULL AND lower(ea.owner_name) % lower(CAST(:rawQuery AS text)) AND similarity(lower(ea.owner_name), lower(CAST(:rawQuery AS text))) >= 0.30)
                    ))
                    OR (ea.profile_description IS NOT NULL AND lower(ea.profile_description) LIKE lower(CAST(:substringPattern AS text)) ESCAPE '\\')
                    OR (ea.notes IS NOT NULL AND lower(ea.notes) LIKE lower(CAST(:substringPattern AS text)) ESCAPE '\\')
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
                    rs.getString("desc_text"),
                    rs.getString("notes_text"),
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
                        if (c.descText() != null) {
                            snippet = extractSnippet(c.descText(), rawQuery, 240);
                        }
                        if (snippet == null && c.notesText() != null) {
                            snippet = extractSnippet(c.notesText(), rawQuery, 240);
                        }
                    }
                    qualifyingHits.add(new AccountSearchHit(
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
    public Map<Long, AccountSearchDocument> lookupDocuments(Set<Long> vaultEntryIds) {
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
        String sql = "SELECT ea.id AS vault_entry_id, COALESCE(ea.display_name, ea.username) AS primary_text, ea.username AS secondary_text FROM external_accounts ea WHERE ea.id IN (:ids)";
        MapSqlParameterSource params = new MapSqlParameterSource("ids", vaultEntryIds);
        List<AccountSearchDocument> docs = jdbcTemplate.query(sql, params, (rs, rowNum) -> new AccountSearchDocument(
                rs.getLong("vault_entry_id"),
                VaultEntryType.EXTERNAL_ACCOUNT,
                rs.getString("primary_text"),
                rs.getString("secondary_text")
        ));
        return docs.stream().collect(Collectors.toMap(AccountSearchDocument::vaultEntryId, Function.identity()));
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
            String descText,
            String notesText,
            int rankBucket,
            double similarity,
            String matchKind
    ) {}
}

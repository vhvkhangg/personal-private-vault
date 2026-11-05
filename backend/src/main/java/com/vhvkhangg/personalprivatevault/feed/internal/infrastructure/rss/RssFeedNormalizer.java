package com.vhvkhangg.personalprivatevault.feed.internal.infrastructure.rss;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vhvkhangg.personalprivatevault.feed.internal.infrastructure.hackernews.HackerNewsNormalizer;
import com.vhvkhangg.personalprivatevault.feed.item.command.NormalizedFeedItemInput;
import com.vhvkhangg.personalprivatevault.feed.source.exception.FeedRefreshException;
import com.vhvkhangg.personalprivatevault.feed.source.exception.FeedRefreshFailureReason;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.time.format.SignStyle;
import java.time.format.TextStyle;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Normalizes raw RSS 2.0 / Atom 1.0 feed entries into canonical article inputs,
 * enforcing date parsing, eligibility rules, exact URL grouping, and deterministic representative selection.
 */
@Component
public class RssFeedNormalizer {

    public static final int MAX_TITLE_LENGTH = 1000;
    public static final int MAX_URL_LENGTH = 2048;
    public static final int MAX_AUTHOR_LENGTH = 500;
    public static final int MAX_ID_LENGTH = 500;
    public static final int MAX_SELECTED_GROUPS = 50;
    public static final int MAX_RAW_METADATA_BYTES = 2048;

    private static final Pattern ZONE_NAME_PATTERN = Pattern.compile("(?i)\\b(UT|UTC|GMT|EST|EDT|CST|CDT|MST|MDT|PST|PDT|Z)\\s*$");
    private static final Map<String, String> ZONE_REPLACEMENTS = Map.ofEntries(
            Map.entry("UT", "+0000"),
            Map.entry("UTC", "+0000"),
            Map.entry("GMT", "+0000"),
            Map.entry("Z", "+0000"),
            Map.entry("EST", "-0500"),
            Map.entry("EDT", "-0400"),
            Map.entry("CST", "-0600"),
            Map.entry("CDT", "-0500"),
            Map.entry("MST", "-0700"),
            Map.entry("MDT", "-0600"),
            Map.entry("PST", "-0800"),
            Map.entry("PDT", "-0700")
    );

    private static final List<DateTimeFormatter> RFC822_FORMATTERS = createRfc822Formatters();

    private final ObjectMapper objectMapper = new ObjectMapper();

    public record NormalizationResult(
            int parsedEntries,
            int eligibleEntries,
            int distinctArticleUrls,
            Instant fetchedAt,
            List<NormalizedFeedItemInput> normalizedItems
    ) {
        public NormalizationResult(int parsedEntries, int eligibleEntries, int distinctArticleUrls, List<NormalizedFeedItemInput> normalizedItems) {
            this(parsedEntries, eligibleEntries, distinctArticleUrls, null, normalizedItems);
        }
    }

    public record CandidateRecord(
            int documentOrder,
            RssAtomParser.FeedFormat format,
            String trimmedUrl,
            String title,
            String author,
            String originalId,
            Instant publishedAt,
            Instant updatedAt
    ) {}

    public NormalizationResult normalize(RssAtomParser.ParsedFeed parsedFeed, Instant batchNow, java.time.Clock clock) {
        return doNormalize(parsedFeed, batchNow, clock, null);
    }

    public NormalizationResult normalize(RssAtomParser.ParsedFeed parsedFeed, Instant batchNow, Instant explicitFetchedAt) {
        return doNormalize(parsedFeed, batchNow, null, explicitFetchedAt);
    }

    private NormalizationResult doNormalize(RssAtomParser.ParsedFeed parsedFeed, Instant batchNow, java.time.Clock clock, Instant explicitFetchedAt) {
        int parsedEntriesCount = parsedFeed.parsedEntriesCount();
        List<CandidateRecord> eligibleCandidates = new ArrayList<>();

        for (RssAtomParser.RawEntry raw : parsedFeed.rawEntries()) {
            CandidateRecord candidate = evaluateEligibility(raw, batchNow);
            if (candidate != null) {
                eligibleCandidates.add(candidate);
            }
        }

        int eligibleEntriesCount = eligibleCandidates.size();

        // Group by exact trimmed external article URL preserving groups' first-occurrence document order
        Map<String, List<CandidateRecord>> groupedByUrl = new LinkedHashMap<>();
        for (CandidateRecord candidate : eligibleCandidates) {
            groupedByUrl.computeIfAbsent(candidate.trimmedUrl(), k -> new ArrayList<>()).add(candidate);
        }

        int distinctArticleUrls = groupedByUrl.size();

        // Select one representative winner per group
        List<CandidateRecord> winners = new ArrayList<>(distinctArticleUrls);
        for (List<CandidateRecord> group : groupedByUrl.values()) {
            CandidateRecord winner = group.stream().min(REPRESENTATIVE_COMPARATOR).orElseThrow();
            winners.add(winner);
        }

        // Cap to first 50 groups
        List<CandidateRecord> selectedWinners = winners.stream().limit(MAX_SELECTED_GROUPS).toList();

        // Strict post-normalization completion timestamp:
        // Captured strictly after entry eligibility, grouping, and winner selection have completed.
        Instant fetchedAt = explicitFetchedAt != null
                ? explicitFetchedAt
                : (clock != null ? Instant.now(clock) : Instant.now());

        // Build generic ingestion inputs with allowlisted metadata
        List<NormalizedFeedItemInput> normalizedInputs = new ArrayList<>(selectedWinners.size());
        for (CandidateRecord winner : selectedWinners) {
            Map<String, Object> rawMetadata = new LinkedHashMap<>();
            rawMetadata.put("feedFormat", winner.format().name());
            rawMetadata.put("rssCapturedAt", fetchedAt.toString());

            if (winner.originalId() != null && !winner.originalId().isBlank()) {
                rawMetadata.put("feedEntryId", truncateUtf16Safe(winner.originalId(), MAX_ID_LENGTH));
            }
            if (winner.updatedAt() != null) {
                rawMetadata.put("feedEntryUpdatedAt", winner.updatedAt().toString());
            }

            try {
                String json = objectMapper.writeValueAsString(rawMetadata);
                if (json.getBytes(StandardCharsets.UTF_8).length > MAX_RAW_METADATA_BYTES) {
                    rawMetadata.remove("feedEntryId");
                    json = objectMapper.writeValueAsString(rawMetadata);
                    if (json.getBytes(StandardCharsets.UTF_8).length > MAX_RAW_METADATA_BYTES) {
                        rawMetadata.remove("feedEntryUpdatedAt");
                    }
                }
            } catch (JsonProcessingException ex) {
                throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_INVALID_RESPONSE, "Failed to serialize metadata", ex);
            }

            normalizedInputs.add(new NormalizedFeedItemInput(
                    null, // externalId MUST be null per ARTICLE_URL contract
                    winner.title(),
                    winner.trimmedUrl(),
                    winner.author(),
                    null, // summary MUST be null
                    winner.publishedAt(),
                    rawMetadata
            ));
        }

        return new NormalizationResult(
                parsedEntriesCount,
                eligibleEntriesCount,
                distinctArticleUrls,
                fetchedAt,
                normalizedInputs
        );
    }

    private CandidateRecord evaluateEligibility(RssAtomParser.RawEntry raw, Instant batchNow) {
        if (raw.title() == null || raw.title().isBlank()) {
            return null; // blank title skipped
        }
        String normalizedTitle = raw.title().replaceAll("\\s+", " ").trim();
        if (normalizedTitle.isEmpty() || normalizedTitle.length() > MAX_TITLE_LENGTH) {
            return null; // title exceeds length or blank
        }

        if (raw.link() == null || raw.link().isBlank()) {
            return null; // missing link
        }
        String trimmedUrl = raw.link().trim();
        if (trimmedUrl.length() > MAX_URL_LENGTH || !HackerNewsNormalizer.isValidArticleUrl(trimmedUrl)) {
            return null; // invalid URL
        }

        // Date parsing
        Instant publishedAt = null;
        if (raw.feedFormat() == RssAtomParser.FeedFormat.RSS_2_0) {
            publishedAt = parseRssDate(raw.pubDate());
        } else {
            publishedAt = parseAtomDate(raw.pubDate());
        }

        // Future publication date excludes the entry entirely
        if (publishedAt != null && publishedAt.isAfter(batchNow)) {
            return null;
        }

        // Author: optional, omit if > 500 characters
        String author = null;
        if (raw.author() != null) {
            String trimmedAuthor = raw.author().trim();
            if (!trimmedAuthor.isEmpty() && trimmedAuthor.length() <= MAX_AUTHOR_LENGTH) {
                author = trimmedAuthor;
            }
        }

        // Provenance ID: optional original ID before truncation
        String originalId = null;
        if (raw.provenanceId() != null) {
            String trimmedId = raw.provenanceId().trim();
            if (!trimmedId.isEmpty()) {
                originalId = trimmedId;
            }
        }

        // Atom updated
        Instant updatedAt = null;
        if (raw.feedFormat() == RssAtomParser.FeedFormat.ATOM_1_0 && raw.updated() != null) {
            updatedAt = parseAtomDate(raw.updated());
        }

        return new CandidateRecord(
                raw.documentOrder(),
                raw.feedFormat(),
                trimmedUrl,
                normalizedTitle,
                author,
                originalId,
                publishedAt,
                updatedAt
        );
    }

    public static Instant parseRssDate(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        String s = text.trim();

        // Normalize known zone names to numeric offset for deterministic parsing
        Matcher matcher = ZONE_NAME_PATTERN.matcher(s);
        if (matcher.find()) {
            String zoneName = matcher.group(1).toUpperCase(Locale.ROOT);
            String offset = ZONE_REPLACEMENTS.get(zoneName);
            if (offset != null) {
                s = s.substring(0, matcher.start(1)) + offset;
            }
        }

        // Try standard RFC 1123 with STRICT resolver
        try {
            ZonedDateTime zdt = ZonedDateTime.parse(s, DateTimeFormatter.RFC_1123_DATE_TIME.withResolverStyle(ResolverStyle.STRICT));
            return adjustTwoDigitYear(zdt).toInstant();
        } catch (DateTimeParseException ignored) {
        }

        // Try custom RFC 822 variants on original string
        for (DateTimeFormatter formatter : RFC822_FORMATTERS) {
            try {
                ZonedDateTime zdt = ZonedDateTime.parse(s, formatter);
                return adjustTwoDigitYear(zdt).toInstant();
            } catch (DateTimeParseException ignored) {
            }
        }

        // If direct parsing failed, strip optional day-of-week prefix to forgive invalid day-of-week specifications
        String withoutDow = s.replaceFirst("^[A-Za-z]{3,4}(?:,\\s*|\\s+)", "");
        if (!withoutDow.equals(s)) {
            for (DateTimeFormatter formatter : RFC822_FORMATTERS) {
                try {
                    ZonedDateTime zdt = ZonedDateTime.parse(withoutDow, formatter);
                    return adjustTwoDigitYear(zdt).toInstant();
                } catch (DateTimeParseException ignored) {
                }
            }
        }

        // Try ISO-8601 (for dc:date)
        return parseAtomDate(s);
    }

    private static ZonedDateTime adjustTwoDigitYear(ZonedDateTime zdt) {
        if (zdt == null) {
            return null;
        }
        int year = zdt.getYear();
        if (year < 100) {
            return zdt.withYear(year >= 70 ? 1900 + year : 2000 + year);
        }
        return zdt;
    }

    public static Instant parseAtomDate(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        String s = text.trim();
        try {
            return Instant.parse(s);
        } catch (DateTimeParseException ignored) {
        }
        try {
            return OffsetDateTime.parse(s).toInstant();
        } catch (DateTimeParseException ignored) {
        }
        return null;
    }

    public static String truncateUtf16Safe(String str, int maxUnits) {
        if (str == null || str.length() <= maxUnits) {
            return str;
        }
        int len = maxUnits;
        if (Character.isHighSurrogate(str.charAt(len - 1))) {
            len--;
        }
        return str.substring(0, len);
    }

    private static final Comparator<CandidateRecord> REPRESENTATIVE_COMPARATOR = (a, b) -> {
        // 1. latest valid publication first, null after valid
        if (a.publishedAt() != null && b.publishedAt() != null) {
            int dateCmp = b.publishedAt().compareTo(a.publishedAt());
            if (dateCmp != 0) return dateCmp;
        } else if (a.publishedAt() != null) {
            return -1;
        } else if (b.publishedAt() != null) {
            return 1;
        }

        // 2. ascending lexical trimmed original GUID/Atom ID, missing/blank after present
        String idA = a.originalId();
        String idB = b.originalId();
        boolean hasIdA = idA != null && !idA.isBlank();
        boolean hasIdB = idB != null && !idB.isBlank();
        if (hasIdA && hasIdB) {
            int idCmp = idA.compareTo(idB);
            if (idCmp != 0) return idCmp;
        } else if (hasIdA) {
            return -1;
        } else if (hasIdB) {
            return 1;
        }

        // 3. earliest document order last
        return Integer.compare(a.documentOrder(), b.documentOrder());
    };

    private static List<DateTimeFormatter> createRfc822Formatters() {
        List<DateTimeFormatter> list = new ArrayList<>();

        String[] timeFormats = {
                "HH:mm:ss",
                "HH:mm"
        };
        String[] zoneFormats = {
                "Z",
                "XXX",
                "z"
        };

        for (String time : timeFormats) {
            for (String zone : zoneFormats) {
                // Without day of week: "d MMM yyyy HH:mm:ss Z" (with 2-to-4 digit year reduced to base 1970)
                DateTimeFormatter f1 = new DateTimeFormatterBuilder()
                        .parseCaseInsensitive()
                        .appendPattern("d MMM ")
                        .appendValueReduced(ChronoField.YEAR, 2, 4, 1970)
                        .appendLiteral(' ')
                        .appendPattern(time)
                        .appendLiteral(' ')
                        .appendPattern(zone)
                        .toFormatter(Locale.ENGLISH)
                        .withZone(ZoneId.of("UTC"))
                        .withResolverStyle(ResolverStyle.STRICT);
                list.add(f1);

                // With day of week: "EEE, d MMM yyyy HH:mm:ss Z"
                DateTimeFormatter f2 = new DateTimeFormatterBuilder()
                        .parseCaseInsensitive()
                        .appendPattern("EEE, d MMM ")
                        .appendValueReduced(ChronoField.YEAR, 2, 4, 1970)
                        .appendLiteral(' ')
                        .appendPattern(time)
                        .appendLiteral(' ')
                        .appendPattern(zone)
                        .toFormatter(Locale.ENGLISH)
                        .withZone(ZoneId.of("UTC"))
                        .withResolverStyle(ResolverStyle.STRICT);
                list.add(f2);
            }
        }

        return list;
    }
}

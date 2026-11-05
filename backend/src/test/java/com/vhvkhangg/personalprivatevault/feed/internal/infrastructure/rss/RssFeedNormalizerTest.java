package com.vhvkhangg.personalprivatevault.feed.internal.infrastructure.rss;

import com.vhvkhangg.personalprivatevault.feed.item.command.NormalizedFeedItemInput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RssFeedNormalizerTest {

    private RssFeedNormalizer normalizer;
    private static final Instant BATCH_NOW = Instant.parse("2026-11-04T12:00:00Z");
    private static final Instant FETCHED_AT = Instant.parse("2026-11-04T12:00:05Z");

    @BeforeEach
    void setUp() {
        normalizer = new RssFeedNormalizer();
    }

    @ParameterizedTest
    @CsvSource({
            "'Mon, 04 Nov 2026 10:00:00 GMT', '2026-11-04T10:00:00Z'",
            "'04 Nov 2026 10:00:00 GMT', '2026-11-04T10:00:00Z'",
            "'4 Nov 2026 10:00:00 +0000', '2026-11-04T10:00:00Z'",
            "'04 Nov 2026 10:00:00 +00:00', '2026-11-04T10:00:00Z'",
            "'4 Nov 26 10:00:00 GMT', '2026-11-04T10:00:00Z'",
            "'04 Nov 2026 10:00 GMT', '2026-11-04T10:00:00Z'",
            "'Mon, 04 Nov 2026 05:00:00 EST', '2026-11-04T10:00:00Z'",
            "'Mon, 04 Nov 2026 06:00:00 EDT', '2026-11-04T10:00:00Z'",
            "'Mon, 04 Nov 2026 04:00:00 CST', '2026-11-04T10:00:00Z'",
            "'Mon, 04 Nov 2026 02:00:00 PST', '2026-11-04T10:00:00Z'",
            "'2026-11-04T10:00:00Z', '2026-11-04T10:00:00Z'",
            "'1 Jan 99 00:00:00 GMT', '1999-01-01T00:00:00Z'",
            "'29 Feb 2024 00:00:00 GMT', '2024-02-29T00:00:00Z'"
    })
    @DisplayName("RSS pubDate parsing supports various RFC 822 / RFC 1123 / ISO date specifications")
    void parseRssDateFormats(String dateStr, String expectedIso) {
        Instant parsed = RssFeedNormalizer.parseRssDate(dateStr);
        assertThat(parsed).isEqualTo(Instant.parse(expectedIso));
    }

    @ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(strings = {
            "30 Feb 2026 00:00:00 GMT",
            "29 Feb 2025 00:00:00 GMT",
            "31 Apr 2026 00:00:00 GMT",
            "31 Jun 2026 00:00:00 GMT",
            "31 Sep 2026 00:00:00 GMT",
            "31 Nov 2026 00:00:00 GMT",
            "32 Jan 2026 00:00:00 GMT",
            "13 Nov 2026 25:00:00 GMT",
            "invalid-date-string"
    })
    @DisplayName("FR-05: Impossible or invalid calendar dates strictly resolve to null without fabricating dates")
    void invalidCalendarDatesResolveToNull(String invalidDateStr) {
        Instant parsed = RssFeedNormalizer.parseRssDate(invalidDateStr);
        assertThat(parsed)
                .as("Invalid calendar date %s must strictly parse to null", invalidDateStr)
                .isNull();
    }

    @Test
    @DisplayName("Future publication date excludes entry from eligibility")
    void futureDateExcludesEntry() {
        RssAtomParser.RawEntry futureEntry = new RssAtomParser.RawEntry(
                0,
                RssAtomParser.FeedFormat.RSS_2_0,
                "Future Item",
                "https://example.com/future",
                "g1",
                "author",
                "2026-11-04T13:00:00Z", // 1 hour after BATCH_NOW
                null
        );

        RssAtomParser.ParsedFeed feed = new RssAtomParser.ParsedFeed(RssAtomParser.FeedFormat.RSS_2_0, 1, List.of(futureEntry));
        RssFeedNormalizer.NormalizationResult result = normalizer.normalize(feed, BATCH_NOW, FETCHED_AT);

        assertThat(result.parsedEntries()).isEqualTo(1);
        assertThat(result.eligibleEntries()).isEqualTo(0);
        assertThat(result.distinctArticleUrls()).isEqualTo(0);
        assertThat(result.normalizedItems()).isEmpty();
    }

    @Test
    @DisplayName("Missing or invalid date stays null and candidate remains eligible")
    void missingDateStaysNullAndEligible() {
        RssAtomParser.RawEntry entry = new RssAtomParser.RawEntry(
                0,
                RssAtomParser.FeedFormat.RSS_2_0,
                "Undated Item",
                "https://example.com/undated",
                "g1",
                "author",
                "invalid-date-string",
                null
        );

        RssAtomParser.ParsedFeed feed = new RssAtomParser.ParsedFeed(RssAtomParser.FeedFormat.RSS_2_0, 1, List.of(entry));
        RssFeedNormalizer.NormalizationResult result = normalizer.normalize(feed, BATCH_NOW, FETCHED_AT);

        assertThat(result.parsedEntries()).isEqualTo(1);
        assertThat(result.eligibleEntries()).isEqualTo(1);
        assertThat(result.distinctArticleUrls()).isEqualTo(1);
        assertThat(result.normalizedItems()).hasSize(1);
        assertThat(result.normalizedItems().get(0).publishedAt()).isNull();
    }

    @Test
    @DisplayName("Representative selection: latest date wins over earlier or null date")
    void representativeLatestDateWins() {
        RssAtomParser.RawEntry older = new RssAtomParser.RawEntry(
                0,
                RssAtomParser.FeedFormat.RSS_2_0,
                "Older Version",
                "https://example.com/item",
                "g1",
                "author1",
                "2026-11-04T08:00:00Z",
                null
        );
        RssAtomParser.RawEntry newer = new RssAtomParser.RawEntry(
                1,
                RssAtomParser.FeedFormat.RSS_2_0,
                "Newer Version",
                "https://example.com/item",
                "g2",
                "author2",
                "2026-11-04T10:00:00Z",
                null
        );

        RssAtomParser.ParsedFeed feed = new RssAtomParser.ParsedFeed(RssAtomParser.FeedFormat.RSS_2_0, 2, List.of(older, newer));
        RssFeedNormalizer.NormalizationResult result = normalizer.normalize(feed, BATCH_NOW, FETCHED_AT);

        assertThat(result.parsedEntries()).isEqualTo(2);
        assertThat(result.eligibleEntries()).isEqualTo(2);
        assertThat(result.distinctArticleUrls()).isEqualTo(1);
        assertThat(result.normalizedItems()).hasSize(1);

        NormalizedFeedItemInput winner = result.normalizedItems().get(0);
        assertThat(winner.title()).isEqualTo("Newer Version");
        assertThat(winner.publishedAt()).isEqualTo(Instant.parse("2026-11-04T10:00:00Z"));
    }

    @Test
    @DisplayName("Representative selection: date tie broken by ascending lexical original GUID")
    void representativeDateTieBrokenByLexicalId() {
        RssAtomParser.RawEntry bGuid = new RssAtomParser.RawEntry(
                0,
                RssAtomParser.FeedFormat.RSS_2_0,
                "Version B",
                "https://example.com/item",
                "guid-b",
                "author",
                "2026-11-04T10:00:00Z",
                null
        );
        RssAtomParser.RawEntry aGuid = new RssAtomParser.RawEntry(
                1,
                RssAtomParser.FeedFormat.RSS_2_0,
                "Version A",
                "https://example.com/item",
                "guid-a",
                "author",
                "2026-11-04T10:00:00Z",
                null
        );

        RssAtomParser.ParsedFeed feed = new RssAtomParser.ParsedFeed(RssAtomParser.FeedFormat.RSS_2_0, 2, List.of(bGuid, aGuid));
        RssFeedNormalizer.NormalizationResult result = normalizer.normalize(feed, BATCH_NOW, FETCHED_AT);

        assertThat(result.distinctArticleUrls()).isEqualTo(1);
        NormalizedFeedItemInput winner = result.normalizedItems().get(0);
        assertThat(winner.title()).isEqualTo("Version A"); // guid-a wins over guid-b
    }

    @Test
    @DisplayName("Selection cap: 60 distinct URL groups reports distinctArticleUrls=60 and 50 processed items")
    void selectionCapFiftyGroups() {
        List<RssAtomParser.RawEntry> entries = new ArrayList<>();
        for (int i = 0; i < 60; i++) {
            entries.add(new RssAtomParser.RawEntry(
                    i,
                    RssAtomParser.FeedFormat.RSS_2_0,
                    "Item " + i,
                    "https://example.com/item/" + i,
                    "guid-" + i,
                    "author",
                    "2026-11-04T10:00:00Z",
                    null
            ));
        }

        RssAtomParser.ParsedFeed feed = new RssAtomParser.ParsedFeed(RssAtomParser.FeedFormat.RSS_2_0, 60, entries);
        RssFeedNormalizer.NormalizationResult result = normalizer.normalize(feed, BATCH_NOW, FETCHED_AT);

        assertThat(result.parsedEntries()).isEqualTo(60);
        assertThat(result.eligibleEntries()).isEqualTo(60);
        assertThat(result.distinctArticleUrls()).isEqualTo(60);
        assertThat(result.normalizedItems()).hasSize(50);
        assertThat(result.normalizedItems().get(0).url()).isEqualTo("https://example.com/item/0");
        assertThat(result.normalizedItems().get(49).url()).isEqualTo("https://example.com/item/49");
    }

    @Test
    @DisplayName("Metadata allowlist contains feedFormat, rssCapturedAt, feedEntryId, feedEntryUpdatedAt")
    void metadataAllowlistCorrect() {
        RssAtomParser.RawEntry entry = new RssAtomParser.RawEntry(
                0,
                RssAtomParser.FeedFormat.ATOM_1_0,
                "Atom Post",
                "https://example.com/atom-post",
                "urn:uuid:my-id",
                "author",
                "2026-11-04T10:00:00Z",
                "2026-11-04T10:30:00Z"
        );

        RssAtomParser.ParsedFeed feed = new RssAtomParser.ParsedFeed(RssAtomParser.FeedFormat.ATOM_1_0, 1, List.of(entry));
        RssFeedNormalizer.NormalizationResult result = normalizer.normalize(feed, BATCH_NOW, FETCHED_AT);

        NormalizedFeedItemInput item = result.normalizedItems().get(0);
        assertThat(item.externalId()).isNull(); // externalId MUST be null
        assertThat(item.summary()).isNull();
        assertThat(item.rawMetadata()).containsEntry("feedFormat", "ATOM_1_0");
        assertThat(item.rawMetadata()).containsEntry("rssCapturedAt", FETCHED_AT.toString());
        assertThat(item.rawMetadata()).containsEntry("feedEntryId", "urn:uuid:my-id");
        assertThat(item.rawMetadata()).containsEntry("feedEntryUpdatedAt", "2026-11-04T10:30:00Z");
    }

    @Test
    @DisplayName("FR-04: normalize with Clock captures completion fetchedAt strictly after entry processing finishes")
    void normalizeWithClockCapturesFetchedAtPostProcessing() {
        Instant startTime = Instant.parse("2026-11-04T12:00:00Z");
        Instant advancedTime = Instant.parse("2026-11-04T12:00:05Z");

        java.util.concurrent.atomic.AtomicReference<Instant> currentClockTime = new java.util.concurrent.atomic.AtomicReference<>(startTime);
        java.time.Clock advancingClock = new java.time.Clock() {
            @Override
            public java.time.ZoneId getZone() { return java.time.ZoneOffset.UTC; }
            @Override
            public java.time.Clock withZone(java.time.ZoneId zone) { return this; }
            @Override
            public Instant instant() { return currentClockTime.get(); }
        };

        // When raw entries are iterated, advance the clock
        List<RssAtomParser.RawEntry> entries = new ArrayList<>() {
            @Override
            public java.util.Iterator<RssAtomParser.RawEntry> iterator() {
                currentClockTime.set(advancedTime);
                return super.iterator();
            }
        };
        entries.add(new RssAtomParser.RawEntry(
                0,
                RssAtomParser.FeedFormat.RSS_2_0,
                "Article",
                "https://example.com/article",
                "guid-1",
                "author",
                "Mon, 04 Nov 2026 10:00:00 GMT",
                null
        ));

        RssAtomParser.ParsedFeed feed = new RssAtomParser.ParsedFeed(RssAtomParser.FeedFormat.RSS_2_0, 1, entries);
        RssFeedNormalizer.NormalizationResult result = normalizer.normalize(feed, startTime, advancingClock);

        // Assert fetchedAt reflects the advanced post-processing time, not startTime
        assertThat(result.fetchedAt()).isEqualTo(advancedTime);
        assertThat(result.normalizedItems().get(0).rawMetadata())
                .containsEntry("rssCapturedAt", advancedTime.toString());
    }

    @Test
    @DisplayName("FR-06: Full untruncated original IDs distinguish representatives (> 8192 chars) before 500-unit metadata truncation")
    void fullOriginalIdsDistinguishRepresentativesBeforeMetadataTruncation() {
        String prefix8192 = "A".repeat(8192);
        String idZ = prefix8192 + "z";
        String idA = prefix8192 + "a";

        RssAtomParser.RawEntry entryZ = new RssAtomParser.RawEntry(
                0,
                RssAtomParser.FeedFormat.RSS_2_0,
                "Title",
                "https://example.com/article",
                idZ,
                "author",
                "Mon, 04 Nov 2026 10:00:00 GMT",
                null
        );

        RssAtomParser.RawEntry entryA = new RssAtomParser.RawEntry(
                1,
                RssAtomParser.FeedFormat.RSS_2_0,
                "Title",
                "https://example.com/article",
                idA,
                "author",
                "Mon, 04 Nov 2026 10:00:00 GMT",
                null
        );

        RssAtomParser.ParsedFeed feed = new RssAtomParser.ParsedFeed(RssAtomParser.FeedFormat.RSS_2_0, 2, List.of(entryZ, entryA));
        RssFeedNormalizer.NormalizationResult result = normalizer.normalize(feed, BATCH_NOW, FETCHED_AT);

        // Lexical winner idA ("...a" < "...z") must be selected as representative
        assertThat(result.distinctArticleUrls()).isEqualTo(1);
        assertThat(result.normalizedItems()).hasSize(1);
        NormalizedFeedItemInput winner = result.normalizedItems().get(0);
        // Metadata must truncate to 500 UTF-16 code units safely
        String metadataId = (String) winner.rawMetadata().get("feedEntryId");
        assertThat(metadataId).hasSize(500);
        assertThat(metadataId).isEqualTo(prefix8192.substring(0, 500));
    }

    @Test
    @DisplayName("FR-06: Unicode surrogate pair boundary safety in truncateUtf16Safe")
    void surrogatePairBoundarySafety() {
        // High surrogate \uD83D, Low surrogate \uDE00 (grinning face emoji)
        String prefix499 = "B".repeat(499);
        String emoji = "\uD83D\uDE00";
        String text = prefix499 + emoji + "extra";

        RssAtomParser.RawEntry entry = new RssAtomParser.RawEntry(
                0,
                RssAtomParser.FeedFormat.RSS_2_0,
                "Title",
                "https://example.com/surrogate",
                text,
                "author",
                null,
                null
        );

        RssAtomParser.ParsedFeed feed = new RssAtomParser.ParsedFeed(RssAtomParser.FeedFormat.RSS_2_0, 1, List.of(entry));
        RssFeedNormalizer.NormalizationResult result = normalizer.normalize(feed, BATCH_NOW, FETCHED_AT);

        String metadataId = (String) result.normalizedItems().get(0).rawMetadata().get("feedEntryId");
        // Because char at 499 is a high surrogate and splitting at 500 would orphan it, it must back up to 499
        assertThat(metadataId).hasSize(499);
        assertThat(metadataId).isEqualTo(prefix499);
    }

    @Test
    @DisplayName("FR-06: Exact and one-over boundaries for title (1000 vs 1001), url (2048 vs 2049), author (500 vs 501), and origId (500 vs 501)")
    void exactAndOneOverFieldBoundaries() {
        String title1000 = "T".repeat(1000);
        String title1001 = "T".repeat(1001);

        String baseHttp = "https://example.com/";
        String url2048 = baseHttp + "u".repeat(2048 - baseHttp.length());
        String url2049 = baseHttp + "u".repeat(2049 - baseHttp.length());

        String author500 = "A".repeat(500);
        String author501 = "A".repeat(501);

        String origId500 = "I".repeat(500);
        String origId501 = "J".repeat(501);

        // Exact 1000 title, 2048 URL, 500 author, 500 origId: eligible
        RssAtomParser.RawEntry validExact = new RssAtomParser.RawEntry(
                0, RssAtomParser.FeedFormat.RSS_2_0, title1000, url2048, origId500, author500, null, null
        );
        // 1001 title: ineligible
        RssAtomParser.RawEntry invalidTitle = new RssAtomParser.RawEntry(
                1, RssAtomParser.FeedFormat.RSS_2_0, title1001, "https://example.com/valid", "id2", "auth", null, null
        );
        // 2049 URL: ineligible
        RssAtomParser.RawEntry invalidUrl = new RssAtomParser.RawEntry(
                2, RssAtomParser.FeedFormat.RSS_2_0, "Valid Title", url2049, "id3", "auth", null, null
        );
        // 501 author and 501 origId: eligible, author omitted (null), origId truncated to 500
        RssAtomParser.RawEntry authorOverrun = new RssAtomParser.RawEntry(
                3, RssAtomParser.FeedFormat.RSS_2_0, "Valid Title 2", "https://example.com/valid2", origId501, author501, null, null
        );

        RssAtomParser.ParsedFeed feed = new RssAtomParser.ParsedFeed(
                RssAtomParser.FeedFormat.RSS_2_0, 4, List.of(validExact, invalidTitle, invalidUrl, authorOverrun)
        );
        RssFeedNormalizer.NormalizationResult result = normalizer.normalize(feed, BATCH_NOW, FETCHED_AT);

        assertThat(result.parsedEntries()).isEqualTo(4);
        assertThat(result.eligibleEntries()).isEqualTo(2); // validExact and authorOverrun
        assertThat(result.distinctArticleUrls()).isEqualTo(2);

        NormalizedFeedItemInput itemExact = result.normalizedItems().stream()
                .filter(i -> i.url().equals(url2048))
                .findFirst().orElseThrow();
        assertThat(itemExact.title()).hasSize(1000).isEqualTo(title1000);
        assertThat(itemExact.url()).hasSize(2048).isEqualTo(url2048);
        assertThat(itemExact.author()).hasSize(500).isEqualTo(author500);
        assertThat((String) itemExact.rawMetadata().get("feedEntryId")).hasSize(500).isEqualTo(origId500);

        NormalizedFeedItemInput itemAuthorOver = result.normalizedItems().stream()
                .filter(i -> i.url().equals("https://example.com/valid2"))
                .findFirst().orElseThrow();
        assertThat(itemAuthorOver.author()).as("Author > 500 chars must be omitted as null").isNull();
        assertThat((String) itemAuthorOver.rawMetadata().get("feedEntryId"))
                .hasSize(500)
                .isEqualTo(origId501.substring(0, 500));
    }

    @Test
    @DisplayName("FR-06: Serialized Unicode and escaping envelope in metadata")
    void serializedUnicodeAndEscapingEnvelope() throws Exception {
        String unicodeId = "ID-\uD83D\uDE00-\uD834\uDD1E-\"escaped\"-<\ntag>";
        RssAtomParser.RawEntry entry = new RssAtomParser.RawEntry(
                0, RssAtomParser.FeedFormat.RSS_2_0, "Title", "https://example.com/unicode", unicodeId, "Author", null, null
        );

        RssAtomParser.ParsedFeed feed = new RssAtomParser.ParsedFeed(RssAtomParser.FeedFormat.RSS_2_0, 1, List.of(entry));
        RssFeedNormalizer.NormalizationResult result = normalizer.normalize(feed, BATCH_NOW, FETCHED_AT);

        NormalizedFeedItemInput item = result.normalizedItems().get(0);
        assertThat(item.rawMetadata()).containsKey("feedEntryId");
        assertThat(item.rawMetadata().get("feedEntryId")).isEqualTo(unicodeId);

        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        String json = mapper.writeValueAsString(item.rawMetadata());
        assertThat(json).contains("\\\"escaped\\\"");

        java.util.Map<?, ?> deserialized = mapper.readValue(json, java.util.Map.class);
        assertThat(deserialized.get("feedEntryId")).isEqualTo(unicodeId);
    }
}

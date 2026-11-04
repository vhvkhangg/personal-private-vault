package com.vhvkhangg.personalprivatevault.feed.internal.infrastructure.hackernews;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vhvkhangg.personalprivatevault.feed.item.command.NormalizedFeedItemInput;
import com.vhvkhangg.personalprivatevault.feed.source.exception.FeedRefreshException;
import com.vhvkhangg.personalprivatevault.feed.source.exception.FeedRefreshFailureReason;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HackerNewsNormalizerTest {

    private final ObjectMapper mapper = new ObjectMapper();
    private HackerNewsNormalizer normalizer;
    private HackerNewsClient client;
    private Instant fetchedAt;

    @BeforeEach
    void setUp() {
        normalizer = new HackerNewsNormalizer();
        client = mock(HackerNewsClient.class);
        fetchedAt = Instant.parse("2026-11-02T10:00:00Z");
    }

    @Test
    @DisplayName("Normalizes valid top stories with distinct external article URLs")
    void normalizesDistinctStories() {
        when(client.fetchTopStories(anyLong(), any(AtomicLong.class))).thenReturn(List.of(101L, 102L));

        ObjectNode story1 = mapper.createObjectNode()
                .put("id", 101L)
                .put("type", "story")
                .put("title", "Article One")
                .put("url", "https://example.com/one")
                .put("by", "author1")
                .put("time", 1730000000L)
                .put("score", 15)
                .put("descendants", 5);

        ObjectNode story2 = mapper.createObjectNode()
                .put("id", 102L)
                .put("type", "story")
                .put("title", "Article Two")
                .put("url", "https://example.com/two")
                .put("by", "author2")
                .put("time", 1730000100L)
                .put("score", 30)
                .put("descendants", 10);

        when(client.fetchItem(eq(101L), anyLong(), any(AtomicLong.class))).thenReturn(story1);
        when(client.fetchItem(eq(102L), anyLong(), any(AtomicLong.class))).thenReturn(story2);

        HackerNewsNormalizer.NormalizationResult result = normalizer.fetchAndNormalize(client, 30, fetchedAt);

        assertThat(result.selectedTopIds()).isEqualTo(2);
        assertThat(result.eligibleStories()).isEqualTo(2);
        assertThat(result.distinctArticleUrls()).isEqualTo(2);

        List<NormalizedFeedItemInput> items = result.normalizedItems();
        assertThat(items).hasSize(2);

        NormalizedFeedItemInput item1 = items.get(0);
        assertThat(item1.externalId()).isNull(); // ARTICLE_URL requirement
        assertThat(item1.url()).isEqualTo("https://example.com/one");
        assertThat(item1.title()).isEqualTo("Article One");
        assertThat(item1.author()).isEqualTo("author1");
        assertThat(item1.publishedAt()).isEqualTo(Instant.ofEpochSecond(1730000000L));
        assertThat(item1.rawMetadata()).containsEntry("hnStoryId", 101L);
        assertThat(item1.rawMetadata()).containsEntry("hnDiscussionUrl", "https://news.ycombinator.com/item?id=101");
        assertThat(item1.rawMetadata()).containsEntry("hnRank", 1);
        assertThat(item1.rawMetadata()).containsEntry("hnScore", 15);
        assertThat(item1.rawMetadata()).containsEntry("hnCommentCount", 5);
        assertThat(item1.rawMetadata()).containsEntry("hnCapturedAt", fetchedAt.toString());

        NormalizedFeedItemInput item2 = items.get(1);
        assertThat(item2.externalId()).isNull();
        assertThat(item2.url()).isEqualTo("https://example.com/two");
        assertThat(item2.title()).isEqualTo("Article Two");
        assertThat(item2.rawMetadata()).containsEntry("hnRank", 2);
    }

    @Test
    @DisplayName("Groups submissions by exact trimmed article URL and selects newest submission time")
    void groupsByUrlAndPicksNewestSubmission() {
        when(client.fetchTopStories(anyLong(), any(AtomicLong.class))).thenReturn(List.of(201L, 202L));

        ObjectNode older = mapper.createObjectNode()
                .put("id", 201L)
                .put("type", "story")
                .put("title", "Older Post")
                .put("url", "https://example.com/duplicate")
                .put("time", 1730000000L)
                .put("score", 10);

        ObjectNode newer = mapper.createObjectNode()
                .put("id", 202L)
                .put("type", "story")
                .put("title", "Newer Post")
                .put("url", "  https://example.com/duplicate  ") // trims to same
                .put("time", 1730000500L) // newer
                .put("score", 50);

        when(client.fetchItem(eq(201L), anyLong(), any(AtomicLong.class))).thenReturn(older);
        when(client.fetchItem(eq(202L), anyLong(), any(AtomicLong.class))).thenReturn(newer);

        HackerNewsNormalizer.NormalizationResult result = normalizer.fetchAndNormalize(client, 30, fetchedAt);

        assertThat(result.selectedTopIds()).isEqualTo(2);
        assertThat(result.eligibleStories()).isEqualTo(2);
        assertThat(result.distinctArticleUrls()).isEqualTo(1); // 1 group

        List<NormalizedFeedItemInput> items = result.normalizedItems();
        assertThat(items).hasSize(1);

        NormalizedFeedItemInput winner = items.get(0);
        assertThat(winner.title()).isEqualTo("Newer Post");
        assertThat(winner.url()).isEqualTo("https://example.com/duplicate");
        assertThat(winner.publishedAt()).isEqualTo(Instant.ofEpochSecond(1730000500L));
        assertThat(winner.rawMetadata()).containsEntry("hnStoryId", 202L);
        assertThat(winner.rawMetadata()).containsEntry("hnRank", 2);
    }

    @Test
    @DisplayName("Breaks submission time ties by greatest positive story ID")
    void breaksTimeTiesByGreatestStoryId() {
        when(client.fetchTopStories(anyLong(), any(AtomicLong.class))).thenReturn(List.of(301L, 302L));

        ObjectNode lowerId = mapper.createObjectNode()
                .put("id", 301L)
                .put("type", "story")
                .put("title", "Lower ID Story")
                .put("url", "https://example.com/tie")
                .put("time", 1730000000L);

        ObjectNode higherId = mapper.createObjectNode()
                .put("id", 302L)
                .put("type", "story")
                .put("title", "Higher ID Story")
                .put("url", "https://example.com/tie")
                .put("time", 1730000000L);

        when(client.fetchItem(eq(301L), anyLong(), any(AtomicLong.class))).thenReturn(lowerId);
        when(client.fetchItem(eq(302L), anyLong(), any(AtomicLong.class))).thenReturn(higherId);

        HackerNewsNormalizer.NormalizationResult result = normalizer.fetchAndNormalize(client, 30, fetchedAt);
        assertThat(result.distinctArticleUrls()).isEqualTo(1);

        NormalizedFeedItemInput winner = result.normalizedItems().get(0);
        assertThat(winner.title()).isEqualTo("Higher ID Story");
        assertThat(winner.rawMetadata()).containsEntry("hnStoryId", 302L);
    }

    @Test
    @DisplayName("Explicitly skips dead, deleted, non-story, Ask HN, and invalid-scheme items without failing")
    void explicitSkipsDoNotFailBatch() {
        when(client.fetchTopStories(anyLong(), any(AtomicLong.class))).thenReturn(List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L));

        ObjectNode dead = mapper.createObjectNode()
                .put("id", 1L).put("type", "story").put("dead", true).put("url", "https://example.com/dead").put("title", "Dead").put("time", 1730000000L);
        ObjectNode deleted = mapper.createObjectNode()
                .put("id", 2L).put("type", "story").put("deleted", true).put("url", "https://example.com/del").put("title", "Del").put("time", 1730000000L);
        ObjectNode job = mapper.createObjectNode()
                .put("id", 3L).put("type", "job").put("url", "https://example.com/job").put("title", "Job").put("time", 1730000000L);
        ObjectNode askHn = mapper.createObjectNode()
                .put("id", 4L).put("type", "story").put("title", "Ask HN: Hello").put("time", 1730000000L); // no url
        ObjectNode ftpScheme = mapper.createObjectNode()
                .put("id", 5L).put("type", "story").put("url", "ftp://example.com/file").put("title", "Ftp").put("time", 1730000000L);
        ObjectNode valid = mapper.createObjectNode()
                .put("id", 6L).put("type", "story").put("url", "https://example.com/valid").put("title", "Valid").put("time", 1730000000L);

        when(client.fetchItem(eq(1L), anyLong(), any(AtomicLong.class))).thenReturn(dead);
        when(client.fetchItem(eq(2L), anyLong(), any(AtomicLong.class))).thenReturn(deleted);
        when(client.fetchItem(eq(3L), anyLong(), any(AtomicLong.class))).thenReturn(job);
        when(client.fetchItem(eq(4L), anyLong(), any(AtomicLong.class))).thenReturn(askHn);
        when(client.fetchItem(eq(5L), anyLong(), any(AtomicLong.class))).thenReturn(ftpScheme);
        when(client.fetchItem(eq(6L), anyLong(), any(AtomicLong.class))).thenReturn(valid);
        when(client.fetchItem(eq(7L), anyLong(), any(AtomicLong.class))).thenReturn(null); // null item skip

        HackerNewsNormalizer.NormalizationResult result = normalizer.fetchAndNormalize(client, 30, fetchedAt);

        assertThat(result.selectedTopIds()).isEqualTo(7);
        assertThat(result.eligibleStories()).isEqualTo(1);
        assertThat(result.distinctArticleUrls()).isEqualTo(1);
        assertThat(result.normalizedItems().get(0).url()).isEqualTo("https://example.com/valid");
    }

    @Test
    @DisplayName("Preserves literal HTML-shaped characters in title without rendering or executing")
    void preservesLiteralHtmlInTitle() {
        when(client.fetchTopStories(anyLong(), any(AtomicLong.class))).thenReturn(List.of(501L));

        String htmlTitle = "Show HN: <script>alert('xss')</script> &amp; \"test\" <tag>";
        ObjectNode story = mapper.createObjectNode()
                .put("id", 501L)
                .put("type", "story")
                .put("title", htmlTitle)
                .put("url", "https://example.com/xss")
                .put("time", 1730000000L);

        when(client.fetchItem(eq(501L), anyLong(), any(AtomicLong.class))).thenReturn(story);

        HackerNewsNormalizer.NormalizationResult result = normalizer.fetchAndNormalize(client, 30, fetchedAt);
        assertThat(result.normalizedItems().get(0).title()).isEqualTo(htmlTitle);
    }

    @Test
    @DisplayName("Fails with UPSTREAM_INVALID_RESPONSE when item ID does not match expected ID")
    void idMismatchFails() {
        when(client.fetchTopStories(anyLong(), any(AtomicLong.class))).thenReturn(List.of(101L));

        ObjectNode story = mapper.createObjectNode()
                .put("id", 999L) // mismatch
                .put("type", "story")
                .put("title", "Mismatch")
                .put("url", "https://example.com/test")
                .put("time", 1730000000L);

        when(client.fetchItem(eq(101L), anyLong(), any(AtomicLong.class))).thenReturn(story);

        assertThatThrownBy(() -> normalizer.fetchAndNormalize(client, 30, fetchedAt))
                .isInstanceOf(FeedRefreshException.class)
                .satisfies(ex -> assertThat(((FeedRefreshException) ex).getReason())
                        .isEqualTo(FeedRefreshFailureReason.UPSTREAM_INVALID_RESPONSE));
    }

    @Test
    @DisplayName("Fails with UPSTREAM_INVALID_RESPONSE when required title is blank")
    void blankTitleFails() {
        when(client.fetchTopStories(anyLong(), any(AtomicLong.class))).thenReturn(List.of(101L));

        ObjectNode story = mapper.createObjectNode()
                .put("id", 101L)
                .put("type", "story")
                .put("title", "   ") // blank
                .put("url", "https://example.com/test")
                .put("time", 1730000000L);

        when(client.fetchItem(eq(101L), anyLong(), any(AtomicLong.class))).thenReturn(story);

        assertThatThrownBy(() -> normalizer.fetchAndNormalize(client, 30, fetchedAt))
                .isInstanceOf(FeedRefreshException.class)
                .satisfies(ex -> assertThat(((FeedRefreshException) ex).getReason())
                        .isEqualTo(FeedRefreshFailureReason.UPSTREAM_INVALID_RESPONSE));
    }

    @Test
    @DisplayName("Fails with FETCH_LIMIT_EXCEEDED when URL exceeds 2048 chars")
    void overlengthUrlFails() {
        when(client.fetchTopStories(anyLong(), any(AtomicLong.class))).thenReturn(List.of(101L));

        String hugeUrl = "https://example.com/" + "a".repeat(2040);
        ObjectNode story = mapper.createObjectNode()
                .put("id", 101L)
                .put("type", "story")
                .put("title", "Title")
                .put("url", hugeUrl)
                .put("time", 1730000000L);

        when(client.fetchItem(eq(101L), anyLong(), any(AtomicLong.class))).thenReturn(story);

        assertThatThrownBy(() -> normalizer.fetchAndNormalize(client, 30, fetchedAt))
                .isInstanceOf(FeedRefreshException.class)
                .satisfies(ex -> assertThat(((FeedRefreshException) ex).getReason())
                        .isEqualTo(FeedRefreshFailureReason.FETCH_LIMIT_EXCEEDED));
    }

    @Test
    @DisplayName("Fails with FETCH_LIMIT_EXCEEDED when title exceeds 1000 chars")
    void overlengthTitleFails() {
        when(client.fetchTopStories(anyLong(), any(AtomicLong.class))).thenReturn(List.of(101L));

        ObjectNode story = mapper.createObjectNode()
                .put("id", 101L)
                .put("type", "story")
                .put("title", "T".repeat(1001))
                .put("url", "https://example.com/test")
                .put("time", 1730000000L);

        when(client.fetchItem(eq(101L), anyLong(), any(AtomicLong.class))).thenReturn(story);

        assertThatThrownBy(() -> normalizer.fetchAndNormalize(client, 30, fetchedAt))
                .isInstanceOf(FeedRefreshException.class)
                .satisfies(ex -> assertThat(((FeedRefreshException) ex).getReason())
                        .isEqualTo(FeedRefreshFailureReason.FETCH_LIMIT_EXCEEDED));
    }

    @Test
    @DisplayName("Fails with UPSTREAM_INVALID_RESPONSE when item ID is non-integral float")
    void nonIntegralIdFails() {
        when(client.fetchTopStories(anyLong(), any(AtomicLong.class))).thenReturn(List.of(101L));

        ObjectNode story = mapper.createObjectNode()
                .put("id", 101.5)
                .put("type", "story")
                .put("title", "Float ID")
                .put("url", "https://example.com/test")
                .put("time", 1730000000L);

        when(client.fetchItem(eq(101L), anyLong(), any(AtomicLong.class))).thenReturn(story);

        assertThatThrownBy(() -> normalizer.fetchAndNormalize(client, 30, fetchedAt))
                .isInstanceOf(FeedRefreshException.class)
                .satisfies(ex -> assertThat(((FeedRefreshException) ex).getReason())
                        .isEqualTo(FeedRefreshFailureReason.UPSTREAM_INVALID_RESPONSE));
    }

    @Test
    @DisplayName("Fails with UPSTREAM_INVALID_RESPONSE when item time is non-integral")
    void nonIntegralTimeFails() {
        when(client.fetchTopStories(anyLong(), any(AtomicLong.class))).thenReturn(List.of(101L));

        ObjectNode story = mapper.createObjectNode()
                .put("id", 101L)
                .put("type", "story")
                .put("title", "Float Time")
                .put("url", "https://example.com/test")
                .put("time", 1730000000.5);

        when(client.fetchItem(eq(101L), anyLong(), any(AtomicLong.class))).thenReturn(story);

        assertThatThrownBy(() -> normalizer.fetchAndNormalize(client, 30, fetchedAt))
                .isInstanceOf(FeedRefreshException.class)
                .satisfies(ex -> assertThat(((FeedRefreshException) ex).getReason())
                        .isEqualTo(FeedRefreshFailureReason.UPSTREAM_INVALID_RESPONSE));
    }

    @Test
    @DisplayName("Fails with UPSTREAM_INVALID_RESPONSE when url field is not a string")
    void nonStringUrlFails() {
        when(client.fetchTopStories(anyLong(), any(AtomicLong.class))).thenReturn(List.of(101L));

        ObjectNode story = mapper.createObjectNode()
                .put("id", 101L)
                .put("type", "story")
                .put("title", "Numeric URL")
                .put("url", 12345)
                .put("time", 1730000000L);

        when(client.fetchItem(eq(101L), anyLong(), any(AtomicLong.class))).thenReturn(story);

        assertThatThrownBy(() -> normalizer.fetchAndNormalize(client, 30, fetchedAt))
                .isInstanceOf(FeedRefreshException.class)
                .satisfies(ex -> assertThat(((FeedRefreshException) ex).getReason())
                        .isEqualTo(FeedRefreshFailureReason.UPSTREAM_INVALID_RESPONSE));
    }

    @Test
    @DisplayName("Caps maxItems at 50 even if higher limit requested")
    void capsMaxItemsAtFifty() {
        List<Long> sixtyIds = new java.util.ArrayList<>();
        for (long i = 1; i <= 60; i++) {
            sixtyIds.add(i);
        }
        when(client.fetchTopStories(anyLong(), any(AtomicLong.class))).thenReturn(sixtyIds);

        for (long i = 1; i <= 50; i++) {
            ObjectNode story = mapper.createObjectNode()
                    .put("id", i)
                    .put("type", "story")
                    .put("title", "Story " + i)
                    .put("url", "https://example.com/item/" + i)
                    .put("time", 1730000000L + i);
            when(client.fetchItem(eq(i), anyLong(), any(AtomicLong.class))).thenReturn(story);
        }

        HackerNewsNormalizer.NormalizationResult result = normalizer.fetchAndNormalize(client, 100, fetchedAt);

        assertThat(result.selectedTopIds()).isEqualTo(50);
        assertThat(result.normalizedItems()).hasSize(50);
        // Item 51 should never have been fetched
        org.mockito.Mockito.verify(client, org.mockito.Mockito.never()).fetchItem(eq(51L), anyLong(), any(AtomicLong.class));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "https://example.com/article",
            "http://news.ycombinator.com/item?id=1",
            "https://sub.domain.org/path?q=1&b=2#hash"
    })
    @DisplayName("Valid article URLs are accepted")
    void acceptsValidArticleUrls(String url) {
        assertThat(HackerNewsNormalizer.isValidArticleUrl(url)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "",
            "   ",
            "ftp://example.com/file",
            "javascript:alert(1)",
            "https://user:pass@example.com/article",
            "https://",
            "http:///no-host"
    })
    @DisplayName("Invalid article URLs are rejected")
    void rejectsInvalidArticleUrls(String url) {
        assertThat(HackerNewsNormalizer.isValidArticleUrl(url)).isFalse();
    }

    @Test
    @DisplayName("isValidArticleUrl rejects null, overlength, or control characters")
    void rejectsMalformedEdgeCases() {
        assertThat(HackerNewsNormalizer.isValidArticleUrl(null)).isFalse();
        assertThat(HackerNewsNormalizer.isValidArticleUrl("https://example.com/\nnewline")).isFalse();
        assertThat(HackerNewsNormalizer.isValidArticleUrl("https://example.com/\u0000nullbyte")).isFalse();
        String overlength = "https://example.com/" + "a".repeat(2048);
        assertThat(HackerNewsNormalizer.isValidArticleUrl(overlength)).isFalse();
    }

    @Test
    @DisplayName("FR-02: dead: null and deleted: null are rejected as UPSTREAM_INVALID_RESPONSE")
    void rejectsNullValuedFlags() {
        ObjectNode deadNullStory = mapper.createObjectNode()
                .put("id", 101L)
                .put("type", "story")
                .put("title", "Dead Null")
                .put("url", "https://example.com/dead-null")
                .putNull("dead");

        assertThatThrownBy(() -> normalizer.parseStoryIfEligible(101L, 1, deadNullStory))
                .isInstanceOf(FeedRefreshException.class)
                .satisfies(ex -> assertThat(((FeedRefreshException) ex).getReason())
                        .isEqualTo(FeedRefreshFailureReason.UPSTREAM_INVALID_RESPONSE));

        ObjectNode deletedNullStory = mapper.createObjectNode()
                .put("id", 102L)
                .put("type", "story")
                .put("title", "Deleted Null")
                .put("url", "https://example.com/deleted-null")
                .putNull("deleted");

        assertThatThrownBy(() -> normalizer.parseStoryIfEligible(102L, 1, deletedNullStory))
                .isInstanceOf(FeedRefreshException.class)
                .satisfies(ex -> assertThat(((FeedRefreshException) ex).getReason())
                        .isEqualTo(FeedRefreshFailureReason.UPSTREAM_INVALID_RESPONSE));
    }

    @Test
    @DisplayName("FR-02: non-boolean dead and deleted flags are rejected as UPSTREAM_INVALID_RESPONSE")
    void rejectsNonBooleanFlags() {
        ObjectNode deadStringStory = mapper.createObjectNode()
                .put("id", 101L)
                .put("type", "story")
                .put("title", "Dead String")
                .put("url", "https://example.com/dead-str")
                .put("dead", "true");

        assertThatThrownBy(() -> normalizer.parseStoryIfEligible(101L, 1, deadStringStory))
                .isInstanceOf(FeedRefreshException.class)
                .satisfies(ex -> assertThat(((FeedRefreshException) ex).getReason())
                        .isEqualTo(FeedRefreshFailureReason.UPSTREAM_INVALID_RESPONSE));

        ObjectNode deletedNumericStory = mapper.createObjectNode()
                .put("id", 102L)
                .put("type", "story")
                .put("title", "Deleted Numeric")
                .put("url", "https://example.com/deleted-num")
                .put("deleted", 1);

        assertThatThrownBy(() -> normalizer.parseStoryIfEligible(102L, 1, deletedNumericStory))
                .isInstanceOf(FeedRefreshException.class)
                .satisfies(ex -> assertThat(((FeedRefreshException) ex).getReason())
                        .isEqualTo(FeedRefreshFailureReason.UPSTREAM_INVALID_RESPONSE));
    }
}

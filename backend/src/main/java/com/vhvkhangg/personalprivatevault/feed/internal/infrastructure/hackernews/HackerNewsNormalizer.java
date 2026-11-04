package com.vhvkhangg.personalprivatevault.feed.internal.infrastructure.hackernews;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vhvkhangg.personalprivatevault.feed.item.command.NormalizedFeedItemInput;
import com.vhvkhangg.personalprivatevault.feed.source.exception.FeedRefreshException;
import com.vhvkhangg.personalprivatevault.feed.source.exception.FeedRefreshFailureReason;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Normalizes Hacker News API responses into article-centric candidate inputs,
 * enforcing field limits, skip rules, grouping, and deterministic representative selection.
 */
@Component
public class HackerNewsNormalizer {

    public static final int MAX_TITLE_LENGTH = 1000;
    public static final int MAX_URL_LENGTH = 2048;
    public static final int MAX_AUTHOR_LENGTH = 500;
    public static final int MAX_RAW_METADATA_BYTES = 2048;
    public static final int MAX_SELECTED_ITEMS = 50;
    private static final long FETCH_SUB_BUDGET_SECONDS = 60L;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public record NormalizationResult(
            int selectedTopIds,
            int eligibleStories,
            int distinctArticleUrls,
            List<NormalizedFeedItemInput> normalizedItems
    ) {}

    public record CandidateStory(
            int rank,
            long storyId,
            String trimmedUrl,
            String title,
            String author,
            Instant publishedAt,
            Integer score,
            Integer commentCount
    ) {}

    public record RawFetchResult(
            int selectedTopIds,
            int eligibleStoriesCount,
            int distinctArticleUrls,
            List<CandidateStory> winners
    ) {}

    public RawFetchResult fetchStories(HackerNewsClient client, int maxItems, long deadlineNanos) {
        AtomicLong aggregateBytesCounter = new AtomicLong(0);

        List<Long> topIds = client.fetchTopStories(deadlineNanos, aggregateBytesCounter);
        int boundedMax = Math.min(Math.max(1, maxItems), MAX_SELECTED_ITEMS);
        int selectedCount = Math.min(boundedMax, topIds.size());
        List<Long> selectedIds = topIds.subList(0, selectedCount);

        List<CandidateStory> eligibleStories = new ArrayList<>();

        for (int i = 0; i < selectedCount; i++) {
            int rank = i + 1; // 1-based top-list rank
            long storyId = selectedIds.get(i);
            JsonNode itemNode = client.fetchItem(storyId, deadlineNanos, aggregateBytesCounter);

            CandidateStory candidate = parseStoryIfEligible(storyId, rank, itemNode);
            if (candidate != null) {
                eligibleStories.add(candidate);
            }
        }

        // Group by exact trimmed external article URL
        Map<String, List<CandidateStory>> groupedByUrl = new LinkedHashMap<>();
        for (CandidateStory story : eligibleStories) {
            groupedByUrl.computeIfAbsent(story.trimmedUrl(), k -> new ArrayList<>()).add(story);
        }

        List<CandidateStory> winners = new ArrayList<>(groupedByUrl.size());
        for (List<CandidateStory> group : groupedByUrl.values()) {
            CandidateStory winner = group.stream()
                    .max(Comparator.comparing(CandidateStory::publishedAt).thenComparing(CandidateStory::storyId))
                    .orElseThrow();
            winners.add(winner);
        }

        // Sort winners deterministically by their captured rank ascending
        winners.sort(Comparator.comparingInt(CandidateStory::rank));

        return new RawFetchResult(
                selectedCount,
                eligibleStories.size(),
                groupedByUrl.size(),
                winners
        );
    }

    public NormalizationResult buildNormalizationResult(RawFetchResult rawResult, Instant fetchedAt) {
        List<NormalizedFeedItemInput> normalizedInputs = new ArrayList<>(rawResult.winners().size());
        for (CandidateStory winner : rawResult.winners()) {
            Map<String, Object> rawMetadata = new LinkedHashMap<>();
            rawMetadata.put("hnStoryId", winner.storyId());
            rawMetadata.put("hnDiscussionUrl", "https://news.ycombinator.com/item?id=" + winner.storyId());
            rawMetadata.put("hnRank", winner.rank());
            if (winner.score() != null) {
                rawMetadata.put("hnScore", winner.score());
            }
            if (winner.commentCount() != null) {
                rawMetadata.put("hnCommentCount", winner.commentCount());
            }
            rawMetadata.put("hnCapturedAt", fetchedAt.toString());

            try {
                String serialized = objectMapper.writeValueAsString(rawMetadata);
                if (serialized.getBytes(StandardCharsets.UTF_8).length > MAX_RAW_METADATA_BYTES) {
                    throw new FeedRefreshException(FeedRefreshFailureReason.FETCH_LIMIT_EXCEEDED, "Metadata size exceeded 2 KiB");
                }
            } catch (JsonProcessingException ex) {
                throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_INVALID_RESPONSE, "Failed to serialize metadata", ex);
            }

            normalizedInputs.add(new NormalizedFeedItemInput(
                    null, // externalId MUST be null per ARTICLE_URL contract
                    winner.title(),
                    winner.trimmedUrl(),
                    winner.author(),
                    null,
                    winner.publishedAt(),
                    rawMetadata
            ));
        }

        return new NormalizationResult(
                rawResult.selectedTopIds(),
                rawResult.eligibleStoriesCount(),
                rawResult.distinctArticleUrls(),
                normalizedInputs
        );
    }

    public NormalizationResult fetchAndNormalize(HackerNewsClient client, int maxItems, Instant fetchedAt) {
        long deadlineNanos = System.nanoTime() + TimeUnit.SECONDS.toNanos(FETCH_SUB_BUDGET_SECONDS);
        RawFetchResult rawResult = fetchStories(client, maxItems, deadlineNanos);
        return buildNormalizationResult(rawResult, fetchedAt);
    }

    public CandidateStory parseStoryIfEligible(long expectedId, int rank, JsonNode node) {
        if (node == null || node.isNull()) {
            return null; // explicit skip
        }
        if (!node.isObject()) {
            throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_INVALID_RESPONSE, "Item must be a JSON object");
        }

        // dead check
        if (node.has("dead")) {
            JsonNode deadNode = node.get("dead");
            if (deadNode == null || !deadNode.isBoolean()) {
                throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_INVALID_RESPONSE, "dead must be boolean when present");
            }
            if (deadNode.asBoolean()) {
                return null; // explicit skip
            }
        }

        // deleted check
        if (node.has("deleted")) {
            JsonNode deletedNode = node.get("deleted");
            if (deletedNode == null || !deletedNode.isBoolean()) {
                throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_INVALID_RESPONSE, "deleted must be boolean when present");
            }
            if (deletedNode.asBoolean()) {
                return null; // explicit skip
            }
        }

        // type check
        if (!node.hasNonNull("type") || !node.get("type").isTextual()) {
            throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_INVALID_RESPONSE, "Missing or non-textual type field");
        }
        String type = node.get("type").asText();
        if (!"story".equals(type)) {
            return null; // explicit skip (e.g. job, comment, poll)
        }

        // url check
        String trimmedUrl = null;
        if (node.has("url")) {
            JsonNode urlNode = node.get("url");
            if (urlNode.isNull()) {
                return null; // explicit skip (Ask HN, text-only story with null url)
            }
            if (!urlNode.isTextual()) {
                throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_INVALID_RESPONSE, "URL field must be a string when present");
            }
            String rawUrl = urlNode.asText();
            trimmedUrl = rawUrl.trim();
            if (trimmedUrl.isEmpty()) {
                return null; // explicit skip
            }
            if (trimmedUrl.length() > MAX_URL_LENGTH) {
                throw new FeedRefreshException(FeedRefreshFailureReason.FETCH_LIMIT_EXCEEDED, "URL exceeds length limit: " + MAX_URL_LENGTH);
            }
            if (!isValidArticleUrl(trimmedUrl)) {
                return null; // explicit skip on malformed URL
            }
        } else {
            return null; // explicit skip (Ask HN without url field)
        }

        // required id
        if (!node.hasNonNull("id")) {
            throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_INVALID_RESPONSE, "Missing id");
        }
        JsonNode idNode = node.get("id");
        if (!idNode.isIntegralNumber() || !idNode.canConvertToLong()) {
            throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_INVALID_RESPONSE, "Item ID must be an integral long");
        }
        long id = idNode.asLong();
        if (id <= 0) {
            throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_INVALID_RESPONSE, "Item ID must be positive");
        }
        if (id != expectedId) {
            throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_INVALID_RESPONSE, "Item ID mismatch: expected " + expectedId + " but was " + id);
        }

        // required title
        if (!node.hasNonNull("title") || !node.get("title").isTextual()) {
            throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_INVALID_RESPONSE, "Missing or non-textual title");
        }
        String rawTitle = node.get("title").asText();
        String normalizedTitle = rawTitle.replaceAll("\\s+", " ").trim();
        if (normalizedTitle.isEmpty()) {
            throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_INVALID_RESPONSE, "Title must not be blank");
        }
        if (normalizedTitle.length() > MAX_TITLE_LENGTH) {
            throw new FeedRefreshException(FeedRefreshFailureReason.FETCH_LIMIT_EXCEEDED, "Title exceeds length limit: " + MAX_TITLE_LENGTH);
        }

        // required time
        if (!node.hasNonNull("time")) {
            throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_INVALID_RESPONSE, "Missing time");
        }
        JsonNode timeNode = node.get("time");
        if (!timeNode.isIntegralNumber() || !timeNode.canConvertToLong()) {
            throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_INVALID_RESPONSE, "Missing or non-integral time");
        }
        long timeSec = timeNode.asLong();
        if (timeSec < 0) {
            throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_INVALID_RESPONSE, "time must be non-negative");
        }
        Instant publishedAt;
        try {
            publishedAt = Instant.ofEpochSecond(timeSec);
            int year = publishedAt.atZone(ZoneOffset.UTC).getYear();
            if (year < 1970 || year > 9999) {
                throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_INVALID_RESPONSE, "Year out of PostgreSQL safe range");
            }
        } catch (DateTimeException ex) {
            throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_INVALID_RESPONSE, "Invalid Unix timestamp", ex);
        }

        // optional author (by)
        String author = null;
        if (node.hasNonNull("by")) {
            JsonNode byNode = node.get("by");
            if (!byNode.isTextual()) {
                throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_INVALID_RESPONSE, "by must be textual");
            }
            String byText = byNode.asText().trim();
            if (byText.length() > MAX_AUTHOR_LENGTH) {
                throw new FeedRefreshException(FeedRefreshFailureReason.FETCH_LIMIT_EXCEEDED, "Author exceeds length limit: " + MAX_AUTHOR_LENGTH);
            }
            if (!byText.isEmpty()) {
                author = byText;
            }
        }

        // optional score
        Integer score = null;
        if (node.has("score")) {
            JsonNode scoreNode = node.get("score");
            if (!scoreNode.isNull()) {
                if (!scoreNode.isIntegralNumber() || !scoreNode.canConvertToInt()) {
                    throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_INVALID_RESPONSE, "score must be integral");
                }
                int sc = scoreNode.asInt();
                if (sc < 0) {
                    throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_INVALID_RESPONSE, "score must be non-negative");
                }
                score = sc;
            }
        }

        // optional descendants (commentCount)
        Integer commentCount = null;
        if (node.has("descendants")) {
            JsonNode descNode = node.get("descendants");
            if (!descNode.isNull()) {
                if (!descNode.isIntegralNumber() || !descNode.canConvertToInt()) {
                    throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_INVALID_RESPONSE, "descendants must be integral");
                }
                int cc = descNode.asInt();
                if (cc < 0) {
                    throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_INVALID_RESPONSE, "descendants must be non-negative");
                }
                commentCount = cc;
            }
        }

        return new CandidateStory(
                rank,
                id,
                trimmedUrl,
                normalizedTitle,
                author,
                publishedAt,
                score,
                commentCount
        );
    }

    public static boolean isValidArticleUrl(String trimmedUrl) {
        if (trimmedUrl == null) {
            return false;
        }
        String s = trimmedUrl.trim();
        if (s.isEmpty() || s.length() > MAX_URL_LENGTH) {
            return false;
        }
        URI uri;
        try {
            uri = URI.create(s);
        } catch (IllegalArgumentException ex) {
            return false;
        }
        String scheme = uri.getScheme();
        if (scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
            return false;
        }
        if (uri.getHost() == null || uri.getHost().trim().isEmpty()) {
            return false;
        }
        if (uri.getUserInfo() != null) {
            return false;
        }
        for (int c = 0; c < s.length(); c++) {
            if (Character.isISOControl(s.charAt(c))) {
                return false;
            }
        }
        return true;
    }
}

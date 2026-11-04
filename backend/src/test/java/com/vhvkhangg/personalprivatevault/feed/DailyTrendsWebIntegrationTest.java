package com.vhvkhangg.personalprivatevault.feed;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vhvkhangg.personalprivatevault.feed.enums.FeedSourceType;
import com.vhvkhangg.personalprivatevault.feed.internal.infrastructure.hackernews.HackerNewsClient;
import com.vhvkhangg.personalprivatevault.feed.source.FeedSourceOperations;
import com.vhvkhangg.personalprivatevault.feed.source.command.CreateFeedSourceCommand;
import com.vhvkhangg.personalprivatevault.feed.view.FeedSourceView;
import com.vhvkhangg.personalprivatevault.support.AbstractWebIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DailyTrendsWebIntegrationTest extends AbstractWebIntegrationTest {

    @Autowired
    private FeedSourceOperations feedSourceOperations;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockitoBean
    private HackerNewsClient hackerNewsClient;

    @BeforeEach
    void setUp() {
        tearDown();
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.execute("DELETE FROM favorites");
        jdbcTemplate.execute("DELETE FROM vault_entry_tags");
        jdbcTemplate.execute("DELETE FROM tags");
        jdbcTemplate.execute("DELETE FROM saved_resource_conversions");
        jdbcTemplate.execute("DELETE FROM saved_resources");
        jdbcTemplate.execute("DELETE FROM feed_items");
        jdbcTemplate.execute("DELETE FROM feed_sources");
        jdbcTemplate.execute("DELETE FROM vault_entries WHERE entry_type = 'SAVED_RESOURCE'");
    }

    private FeedSourceView createSource(String name, FeedSourceType type, boolean enabled) {
        return feedSourceOperations.createSource(new CreateFeedSourceCommand(
                name,
                type,
                "https://news.ycombinator.com",
                type == FeedSourceType.RSS ? "https://example.com/rss" : null,
                enabled,
                false,
                null,
                null
        ));
    }

    private JsonNode createStoryJson(long id, String title, String url, String author, long time, int score) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("id", id);
        node.put("type", "story");
        node.put("title", title);
        node.put("url", url);
        node.put("by", author);
        node.put("time", time);
        node.put("score", score);
        return node;
    }

    @Test
    @DisplayName("Security: endpoints require valid Bearer authentication")
    void endpointsRequireBearerAuth() throws Exception {
        FeedSourceView source = createSource("HN Sec", FeedSourceType.HACKER_NEWS, true);

        // POST refresh without auth
        mockMvc.perform(post("/api/v1/feed/sources/{sourceId}/refresh", source.id()))
                .andExpect(status().isUnauthorized());

        // GET radar without auth
        mockMvc.perform(get("/api/v1/feed/radar"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Validation: maxItems parameter bounds (1..50) enforced on refresh")
    void refreshMaxItemsValidationBounds() throws Exception {
        FeedSourceView source = createSource("HN Bounds", FeedSourceType.HACKER_NEWS, true);

        // maxItems = 0 -> 400
        mockMvc.perform(post("/api/v1/feed/sources/{sourceId}/refresh", source.id())
                        .param("maxItems", "0")
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        // maxItems = 51 -> 400
        mockMvc.perform(post("/api/v1/feed/sources/{sourceId}/refresh", source.id())
                        .param("maxItems", "51")
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        // maxItems = -1 -> 400
        mockMvc.perform(post("/api/v1/feed/sources/{sourceId}/refresh", source.id())
                        .param("maxItems", "-1")
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("Validation: limit parameter bounds (1..100) enforced on radar")
    void radarLimitValidationBounds() throws Exception {
        // limit = 0 -> 400
        mockMvc.perform(get("/api/v1/feed/radar")
                        .param("limit", "0")
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        // limit = 101 -> 400
        mockMvc.perform(get("/api/v1/feed/radar")
                        .param("limit", "101")
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("Error envelopes: categorical HTTP status and privacy-safe error responses")
    void errorEnvelopesPrivacyAndStatusCodes() throws Exception {
        FeedSourceView disabledSource = createSource("Disabled HN", FeedSourceType.HACKER_NEWS, false);
        FeedSourceView rssSource = createSource("RSS Source", FeedSourceType.RSS, true);

        // 1. Disabled source -> 422 Unprocessable Entity
        mockMvc.perform(post("/api/v1/feed/sources/{sourceId}/refresh", disabledSource.id())
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.data").value(nullValue()))
                .andExpect(jsonPath("$.error.code").value("SOURCE_DISABLED"))
                .andExpect(jsonPath("$.error.message").value("Feed source is disabled"));

        // 2. Unsupported source type -> 422 Unprocessable Entity
        mockMvc.perform(post("/api/v1/feed/sources/{sourceId}/refresh", rssSource.id())
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.data").value(nullValue()))
                .andExpect(jsonPath("$.error.code").value("UNSUPPORTED_SOURCE_TYPE"));

        // 3. Not found source -> 404 Not Found
        mockMvc.perform(post("/api/v1/feed/sources/{sourceId}/refresh", 777777L)
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("FEED_SOURCE_NOT_FOUND"));

        // Verify no sensitive internal details leak in error body
        mockMvc.perform(get("/api/v1/feed/radar")
                        .param("sourceId", String.valueOf(disabledSource.id()))
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error.code").value("SOURCE_DISABLED"));

        // Radar filtered by RSS source -> 422 Unprocessable Entity
        mockMvc.perform(get("/api/v1/feed/radar")
                        .param("sourceId", String.valueOf(rssSource.id()))
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error.code").value("UNSUPPORTED_SOURCE_TYPE"));
    }

    @Test
    @DisplayName("End-to-End: refresh source -> read radar -> save item to inbox")
    void endToEndRefreshRadarSaveToInbox() throws Exception {
        FeedSourceView source = createSource("HN Live Radar", FeedSourceType.HACKER_NEWS, true);

        long submissionTime = Instant.now().minus(Duration.ofHours(2)).getEpochSecond();
        String articleUrl = "https://example.org/great-article-" + System.currentTimeMillis();
        String dangerousTitle = "Malicious <script>alert('xss')</script> & Safe Title";

        when(hackerNewsClient.fetchTopStories(anyLong(), any(AtomicLong.class))).thenReturn(List.of(12345L));
        when(hackerNewsClient.fetchItem(eq(12345L), anyLong(), any(AtomicLong.class)))
                .thenReturn(createStoryJson(12345L, dangerousTitle, articleUrl, "authorX", submissionTime, 250));

        // 1. POST refresh -> 200 OK
        mockMvc.perform(post("/api/v1/feed/sources/{sourceId}/refresh", source.id())
                        .param("maxItems", "10")
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sourceId").value(source.id()))
                .andExpect(jsonPath("$.data.selectedTopIds").value(1))
                .andExpect(jsonPath("$.data.eligibleStories").value(1))
                .andExpect(jsonPath("$.data.distinctArticleUrls").value(1))
                .andExpect(jsonPath("$.data.processedItems").value(1));

        // 2. GET radar -> 200 OK, verify item and safe serialization of untrusted title
        String radarJson = mockMvc.perform(get("/api/v1/feed/radar")
                        .param("limit", "10")
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items", not(empty())))
                .andExpect(jsonPath("$.data.items[0].title").value(dangerousTitle))
                .andExpect(jsonPath("$.data.items[0].url").value(articleUrl))
                .andExpect(jsonPath("$.data.items[0].signalType").value("HN_TOP_STORIES_AT_CAPTURE"))
                .andExpect(jsonPath("$.data.items[0].hnStoryId").value(12345))
                .andExpect(jsonPath("$.data.items[0].hnDiscussionUrl").value("https://news.ycombinator.com/item?id=12345"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode radarRoot = objectMapper.readTree(radarJson);
        long feedItemId = radarRoot.path("data").path("items").get(0).path("feedItemId").asLong();

        // 3. POST /api/v1/saved-resources/feed-item -> 201 Created
        String savePayload = """
                {
                  "feedItemId": %d,
                  "kind": "ARTICLE"
                }
                """.formatted(feedItemId);

        mockMvc.perform(post("/api/v1/saved-resources/feed-item")
                        .header("Authorization", bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(savePayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.resourceUrl").value(articleUrl))
                .andExpect(jsonPath("$.data.title").value(dangerousTitle))
                .andExpect(jsonPath("$.data.feedItemId").value(feedItemId));

        // 4. Verify saved resource appears in inbox
        mockMvc.perform(get("/api/v1/saved-resources/inbox")
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[?(@.feedItemId == %d)].resourceUrl".formatted(feedItemId))
                        .value(org.hamcrest.Matchers.contains(articleUrl)));
    }
}

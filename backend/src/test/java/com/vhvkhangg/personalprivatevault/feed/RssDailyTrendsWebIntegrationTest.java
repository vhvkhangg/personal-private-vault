package com.vhvkhangg.personalprivatevault.feed;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vhvkhangg.personalprivatevault.feed.enums.FeedSourceType;
import com.vhvkhangg.personalprivatevault.feed.internal.infrastructure.hackernews.HackerNewsClient;
import com.vhvkhangg.personalprivatevault.feed.internal.infrastructure.rss.RssFeedClient;
import com.vhvkhangg.personalprivatevault.feed.item.FeedItemOperations;
import com.vhvkhangg.personalprivatevault.feed.item.command.NormalizedFeedItemInput;
import com.vhvkhangg.personalprivatevault.feed.source.FeedSourceOperations;
import com.vhvkhangg.personalprivatevault.feed.source.command.CreateFeedSourceCommand;
import com.vhvkhangg.personalprivatevault.feed.source.exception.FeedRefreshException;
import com.vhvkhangg.personalprivatevault.feed.source.exception.FeedRefreshFailureReason;
import com.vhvkhangg.personalprivatevault.feed.view.FeedSourceView;
import com.vhvkhangg.personalprivatevault.support.AbstractWebIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RssDailyTrendsWebIntegrationTest extends AbstractWebIntegrationTest {

    @Autowired
    private FeedSourceOperations feedSourceOperations;

    @Autowired
    private FeedItemOperations feedItemOperations;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockitoBean
    private RssFeedClient rssFeedClient;

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

    private FeedSourceView createRssSource(String name, String feedUrl, boolean enabled) {
        return feedSourceOperations.createSource(new CreateFeedSourceCommand(
                name,
                FeedSourceType.RSS,
                null,
                feedUrl,
                enabled,
                false,
                null,
                null
        ));
    }

    private FeedSourceView createHnSource(String name, boolean enabled) {
        return feedSourceOperations.createSource(new CreateFeedSourceCommand(
                name,
                FeedSourceType.HACKER_NEWS,
                "https://news.ycombinator.com",
                null,
                enabled,
                false,
                null,
                null
        ));
    }

    private static final String SAMPLE_RSS = """
            <rss version="2.0">
              <channel>
                <item>
                  <title>Sample RSS Article</title>
                  <link>https://example.com/sample-article</link>
                  <guid>sample-guid-1</guid>
                  <pubDate>Wed, 04 Nov 2026 10:00:00 GMT</pubDate>
                </item>
              </channel>
            </rss>
            """;

    @Test
    @DisplayName("Security: endpoints require valid Bearer authentication")
    void endpointsRequireBearerAuth() throws Exception {
        FeedSourceView source = createRssSource("RSS Sec", "https://example.com/rss.xml", true);

        // POST refresh-rss without auth -> 401
        mockMvc.perform(post("/api/v1/feed/sources/{sourceId}/refresh-rss", source.id()))
                .andExpect(status().isUnauthorized());

        // GET combined radar without auth -> 401
        mockMvc.perform(get("/api/v1/feed/radar/combined"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /sources/{id}/refresh-rss: successfully executes manual RSS refresh")
    void refreshRssSourceSuccess() throws Exception {
        FeedSourceView source = createRssSource("My RSS", "https://example.com/my-rss.xml", true);

        when(rssFeedClient.fetchFeed(eq("https://example.com/my-rss.xml"), anyLong()))
                .thenReturn(SAMPLE_RSS.getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(post("/api/v1/feed/sources/{sourceId}/refresh-rss", source.id())
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sourceId").value(source.id()))
                .andExpect(jsonPath("$.data.fetchedAt").isNotEmpty())
                .andExpect(jsonPath("$.data.parsedEntries").value(1))
                .andExpect(jsonPath("$.data.eligibleEntries").value(1))
                .andExpect(jsonPath("$.data.distinctArticleUrls").value(1))
                .andExpect(jsonPath("$.data.processedItems").value(1))
                .andExpect(jsonPath("$.error").value(nullValue()));
    }

    @Test
    @DisplayName("POST /sources/{id}/refresh-rss: error cases (404, 422, 502)")
    void refreshRssSourceErrors() throws Exception {
        FeedSourceView disabledRss = createRssSource("Disabled RSS", "https://example.com/d.xml", false);
        FeedSourceView hnSource = createHnSource("HN Source", true);
        FeedSourceView noUrlRss = feedSourceOperations.createSource(new CreateFeedSourceCommand(
                "No URL RSS",
                FeedSourceType.RSS,
                null,
                null,
                true,
                false,
                null,
                null
        ));

        // 1. Not found -> 404
        mockMvc.perform(post("/api/v1/feed/sources/{sourceId}/refresh-rss", 999999L)
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("FEED_SOURCE_NOT_FOUND"));

        // 2. Disabled -> 422
        mockMvc.perform(post("/api/v1/feed/sources/{sourceId}/refresh-rss", disabledRss.id())
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error.code").value("SOURCE_DISABLED"));

        // 3. Not an RSS source -> 422
        mockMvc.perform(post("/api/v1/feed/sources/{sourceId}/refresh-rss", hnSource.id())
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error.code").value("UNSUPPORTED_SOURCE_TYPE"));

        // 4. Missing feedUrl -> 502 UPSTREAM_DESTINATION_REJECTED
        mockMvc.perform(post("/api/v1/feed/sources/{sourceId}/refresh-rss", noUrlRss.id())
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.error.code").value("UPSTREAM_DESTINATION_REJECTED"));

        // 5. Upstream unavailable -> 503
        FeedSourceView errRss = createRssSource("Err RSS", "https://example.com/err.xml", true);
        when(rssFeedClient.fetchFeed(eq("https://example.com/err.xml"), anyLong()))
                .thenThrow(new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_UNAVAILABLE, "Timeout fetching XML"));

        mockMvc.perform(post("/api/v1/feed/sources/{sourceId}/refresh-rss", errRss.id())
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.error.code").value("UPSTREAM_UNAVAILABLE"));

        // 6. Upstream invalid response -> 502
        when(rssFeedClient.fetchFeed(eq("https://example.com/err.xml"), anyLong()))
                .thenThrow(new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_INVALID_RESPONSE, "Invalid XML"));

        mockMvc.perform(post("/api/v1/feed/sources/{sourceId}/refresh-rss", errRss.id())
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.error.code").value("UPSTREAM_INVALID_RESPONSE"));
    }

    @Test
    @DisplayName("FR-06: Public HTTP error responses do not leak synthetic credential tokens or hostile XML markers")
    void errorResponseDoesNotLeakCredentialsOrHostileMarkers() throws Exception {
        // 1. Synthetic credential token in feed URL
        String credentialUrl = "https://user:secret-token-xyz123@example.com/feed.xml";
        FeedSourceView credSource = createRssSource("Cred Source", credentialUrl, true);

        org.springframework.test.web.servlet.MvcResult credResult = mockMvc.perform(
                        post("/api/v1/feed/sources/{sourceId}/refresh-rss", credSource.id())
                                .header("Authorization", bearerHeader()))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.error.code").value("UPSTREAM_DESTINATION_REJECTED"))
                .andReturn();
        String credBody = credResult.getResponse().getContentAsString();
        assertThat(credBody).doesNotContain("secret-token-xyz123");

        // 2. Hostile XML marker in response body
        FeedSourceView hostileSource = createRssSource("Hostile Source", "https://example.com/hostile.xml", true);
        String hostileXml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <!DOCTYPE test [
                  <!ENTITY xxe SYSTEM "http://127.0.0.1/EVIL_XXE_MARKER_999">
                ]>
                <rss version="2.0"><channel><title>&xxe;</title><item><title>T</title><link>https://example.com</link></item></channel></rss>
                """;
        when(rssFeedClient.fetchFeed(eq("https://example.com/hostile.xml"), anyLong()))
                .thenReturn(hostileXml.getBytes(StandardCharsets.UTF_8));

        org.springframework.test.web.servlet.MvcResult hostileResult = mockMvc.perform(
                        post("/api/v1/feed/sources/{sourceId}/refresh-rss", hostileSource.id())
                                .header("Authorization", bearerHeader()))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.error.code").value("UPSTREAM_INVALID_RESPONSE"))
                .andReturn();
        String hostileBody = hostileResult.getResponse().getContentAsString();
        assertThat(hostileBody).doesNotContain("EVIL_XXE_MARKER_999");
    }

    @Test
    @DisplayName("GET /radar/combined: parameter bounds validation (1..100)")
    void combinedRadarValidationBounds() throws Exception {
        // limit = 0 -> 400
        mockMvc.perform(get("/api/v1/feed/radar/combined")
                        .param("limit", "0")
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        // limit = 101 -> 400
        mockMvc.perform(get("/api/v1/feed/radar/combined")
                        .param("limit", "101")
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("GET /radar/combined: returns merged HN and RSS items with RSS_SOURCE_CACHE projection")
    void combinedRadarSuccess() throws Exception {
        FeedSourceView hnSource = createHnSource("HN Source", true);
        FeedSourceView rssSource = createRssSource("RSS Source", "https://example.com/rss.xml", true);

        Instant now = Instant.now();
        feedItemOperations.ingestFetch(hnSource.id(), now, List.of(new NormalizedFeedItemInput(
                null,
                "HN Story in Combined",
                "https://example.com/hn-comb",
                "author",
                null,
                now.minusSeconds(3600),
                Map.of("score", 99, "descendants", 10)
        )));

        feedItemOperations.ingestFetch(rssSource.id(), now, List.of(new NormalizedFeedItemInput(
                null,
                "RSS Post in Combined",
                "https://example.com/rss-comb",
                "author",
                null,
                now.minusSeconds(1800),
                Map.of("feedFormat", "RSS_2_0", "rssCapturedAt", now.toString())
        )));

        mockMvc.perform(get("/api/v1/feed/radar/combined")
                        .param("limit", "10")
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items", hasSize(2)))
                .andExpect(jsonPath("$.data.items[0].title").value("RSS Post in Combined"))
                .andExpect(jsonPath("$.data.items[0].sourceType").value("RSS"))
                .andExpect(jsonPath("$.data.items[0].signalType").value("RSS_SOURCE_CACHE"))
                .andExpect(jsonPath("$.data.items[1].title").value("HN Story in Combined"))
                .andExpect(jsonPath("$.data.items[1].sourceType").value("HACKER_NEWS"))
                .andExpect(jsonPath("$.data.items[1].signalType").value("SOURCE_CACHE"));
    }
}

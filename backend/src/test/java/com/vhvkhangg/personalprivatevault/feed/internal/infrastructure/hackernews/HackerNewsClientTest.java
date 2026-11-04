package com.vhvkhangg.personalprivatevault.feed.internal.infrastructure.hackernews;

import com.fasterxml.jackson.databind.JsonNode;
import com.vhvkhangg.personalprivatevault.feed.source.exception.FeedRefreshException;
import com.vhvkhangg.personalprivatevault.feed.source.exception.FeedRefreshFailureReason;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.DnsResolver;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicStatusLine;
import org.apache.http.protocol.HttpContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import org.apache.http.client.methods.HttpGet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class HackerNewsClientTest {

    private HackerNewsDestinationPolicy policy;
    private HackerNewsClient client;
    private CloseableHttpClient mockHttpClient;
    private long defaultDeadlineNanos;
    private AtomicLong aggregateCounter;

    @BeforeEach
    void setUp() {
        policy = new HackerNewsDestinationPolicy();
        client = new HackerNewsClient(policy);
        mockHttpClient = mock(CloseableHttpClient.class);
        ReflectionTestUtils.setField(client, "httpClient", mockHttpClient);
        defaultDeadlineNanos = System.nanoTime() + TimeUnit.SECONDS.toNanos(60);
        aggregateCounter = new AtomicLong(0);
    }

    @AfterEach
    void tearDown() throws Exception {
        client.close();
    }

    private CloseableHttpResponse createMockResponse(int statusCode, String body, String contentType, String contentEncoding) {
        CloseableHttpResponse response = mock(CloseableHttpResponse.class);
        when(response.getStatusLine()).thenReturn(new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), statusCode, ""));

        ByteArrayEntity entity = new ByteArrayEntity(
                body != null ? body.getBytes(StandardCharsets.UTF_8) : new byte[0],
                contentType != null ? ContentType.parse(contentType) : ContentType.APPLICATION_JSON
        );
        if (contentEncoding != null) {
            entity.setContentEncoding(new BasicHeader("Content-Encoding", contentEncoding));
        }
        when(response.getEntity()).thenReturn(entity);
        return response;
    }

    @Test
    @DisplayName("fetchTopStories parses valid array of story IDs")
    void fetchTopStoriesParsesValidArray() throws Exception {
        String json = "[101, 102, 103, 104]";
        CloseableHttpResponse response = createMockResponse(200, json, "application/json", null);
        when(mockHttpClient.execute(any(HttpUriRequest.class))).thenReturn(response);

        List<Long> ids = client.fetchTopStories(defaultDeadlineNanos, aggregateCounter);
        assertThat(ids).containsExactly(101L, 102L, 103L, 104L);
        assertThat(aggregateCounter.get()).isEqualTo(json.getBytes(StandardCharsets.UTF_8).length);
    }

    @Test
    @DisplayName("Multiple JSON roots ([101] [102]) are rejected as UPSTREAM_INVALID_RESPONSE")
    void multipleJsonRootsRejected() throws Exception {
        String json = "[101] [102]";
        CloseableHttpResponse response = createMockResponse(200, json, "application/json", null);
        when(mockHttpClient.execute(any(HttpUriRequest.class))).thenReturn(response);

        assertThatThrownBy(() -> client.fetchTopStories(defaultDeadlineNanos, aggregateCounter))
                .isInstanceOf(FeedRefreshException.class)
                .satisfies(ex -> assertThat(((FeedRefreshException) ex).getReason())
                        .isEqualTo(FeedRefreshFailureReason.UPSTREAM_INVALID_RESPONSE));
    }

    @Test
    @DisplayName("Valid array followed by trailing whitespace exceeding 64 KiB throws FETCH_LIMIT_EXCEEDED")
    void validArrayWithTrailingBytesExceedingLimitThrows() throws Exception {
        byte[] bigArray = new byte[70005];
        byte[] initial = "[101]".getBytes(StandardCharsets.UTF_8);
        System.arraycopy(initial, 0, bigArray, 0, initial.length);
        Arrays.fill(bigArray, initial.length, bigArray.length, (byte) ' ');

        CloseableHttpResponse response = mock(CloseableHttpResponse.class);
        when(response.getStatusLine()).thenReturn(new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), 200, "OK"));

        HttpEntity entity = mock(HttpEntity.class);
        when(entity.getContent()).thenReturn(new ByteArrayInputStream(bigArray));
        when(entity.getContentType()).thenReturn(new BasicHeader("Content-Type", "application/json"));
        when(response.getEntity()).thenReturn(entity);
        when(mockHttpClient.execute(any(HttpUriRequest.class))).thenReturn(response);

        assertThatThrownBy(() -> client.fetchTopStories(defaultDeadlineNanos, aggregateCounter))
                .isInstanceOf(FeedRefreshException.class)
                .satisfies(ex -> assertThat(((FeedRefreshException) ex).getReason())
                        .isEqualTo(FeedRefreshFailureReason.FETCH_LIMIT_EXCEEDED));
    }

    @Test
    @DisplayName("fetchItem parses valid item JSON object")
    void fetchItemParsesValidObject() throws Exception {
        String json = "{\"id\": 101, \"type\": \"story\", \"title\": \"Test Title\", \"url\": \"https://example.com\"}";
        CloseableHttpResponse response = createMockResponse(200, json, "application/json", null);
        when(mockHttpClient.execute(any(HttpUriRequest.class))).thenReturn(response);

        JsonNode node = client.fetchItem(101L, defaultDeadlineNanos, aggregateCounter);
        assertThat(node.path("id").asLong()).isEqualTo(101L);
        assertThat(node.path("title").asText()).isEqualTo("Test Title");
    }

    @Test
    @DisplayName("Upstream HTTP 429 throws UPSTREAM_RATE_LIMITED")
    void rateLimitThrowsExpectedReason() throws Exception {
        CloseableHttpResponse response = createMockResponse(429, "Rate limited", "text/plain", null);
        when(mockHttpClient.execute(any(HttpUriRequest.class))).thenReturn(response);

        assertThatThrownBy(() -> client.fetchTopStories(defaultDeadlineNanos, aggregateCounter))
                .isInstanceOf(FeedRefreshException.class)
                .satisfies(ex -> assertThat(((FeedRefreshException) ex).getReason())
                        .isEqualTo(FeedRefreshFailureReason.UPSTREAM_RATE_LIMITED));
    }

    @Test
    @DisplayName("Upstream HTTP 5xx throws UPSTREAM_UNAVAILABLE")
    void upstream5xxThrowsExpectedReason() throws Exception {
        CloseableHttpResponse response = createMockResponse(503, "Service Unavailable", "text/plain", null);
        when(mockHttpClient.execute(any(HttpUriRequest.class))).thenReturn(response);

        assertThatThrownBy(() -> client.fetchItem(101L, defaultDeadlineNanos, aggregateCounter))
                .isInstanceOf(FeedRefreshException.class)
                .satisfies(ex -> assertThat(((FeedRefreshException) ex).getReason())
                        .isEqualTo(FeedRefreshFailureReason.UPSTREAM_UNAVAILABLE));
    }

    @Test
    @DisplayName("Upstream redirects (301/302) throw UPSTREAM_INVALID_RESPONSE")
    void redirectsThrowExpectedReason() throws Exception {
        CloseableHttpResponse response = createMockResponse(302, "Redirect", "text/plain", null);
        when(mockHttpClient.execute(any(HttpUriRequest.class))).thenReturn(response);

        assertThatThrownBy(() -> client.fetchItem(101L, defaultDeadlineNanos, aggregateCounter))
                .isInstanceOf(FeedRefreshException.class)
                .satisfies(ex -> assertThat(((FeedRefreshException) ex).getReason())
                        .isEqualTo(FeedRefreshFailureReason.UPSTREAM_INVALID_RESPONSE));
    }

    @Test
    @DisplayName("Non-JSON Content-Type throws UPSTREAM_INVALID_RESPONSE")
    void nonJsonContentTypeThrowsExpectedReason() throws Exception {
        CloseableHttpResponse response = createMockResponse(200, "<html></html>", "text/html", null);
        when(mockHttpClient.execute(any(HttpUriRequest.class))).thenReturn(response);

        assertThatThrownBy(() -> client.fetchTopStories(defaultDeadlineNanos, aggregateCounter))
                .isInstanceOf(FeedRefreshException.class)
                .satisfies(ex -> assertThat(((FeedRefreshException) ex).getReason())
                        .isEqualTo(FeedRefreshFailureReason.UPSTREAM_INVALID_RESPONSE));
    }

    @Test
    @DisplayName("Gzip content encoding is rejected with UPSTREAM_INVALID_RESPONSE")
    void gzipEncodingIsRejected() throws Exception {
        CloseableHttpResponse response = createMockResponse(200, "[101]", "application/json", "gzip");
        when(mockHttpClient.execute(any(HttpUriRequest.class))).thenReturn(response);

        assertThatThrownBy(() -> client.fetchTopStories(defaultDeadlineNanos, aggregateCounter))
                .isInstanceOf(FeedRefreshException.class)
                .satisfies(ex -> assertThat(((FeedRefreshException) ex).getReason())
                        .isEqualTo(FeedRefreshFailureReason.UPSTREAM_INVALID_RESPONSE));
    }

    @Test
    @DisplayName("Single topstories body exceeding 64 KiB throws FETCH_LIMIT_EXCEEDED")
    void toplistExceedingLimitThrows() throws Exception {
        CloseableHttpResponse response = mock(CloseableHttpResponse.class);
        when(response.getStatusLine()).thenReturn(new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), 200, "OK"));

        byte[] bigArray = new byte[70 * 1024];
        bigArray[0] = '[';
        for (int i = 1; i < bigArray.length - 1; i++) {
            bigArray[i] = '1';
        }
        bigArray[bigArray.length - 1] = ']';

        HttpEntity entity = mock(HttpEntity.class);
        when(entity.getContent()).thenReturn(new ByteArrayInputStream(bigArray));
        when(entity.getContentType()).thenReturn(new BasicHeader("Content-Type", "application/json"));
        when(response.getEntity()).thenReturn(entity);

        when(mockHttpClient.execute(any(HttpUriRequest.class))).thenReturn(response);

        assertThatThrownBy(() -> client.fetchTopStories(defaultDeadlineNanos, aggregateCounter))
                .isInstanceOf(FeedRefreshException.class)
                .satisfies(ex -> assertThat(((FeedRefreshException) ex).getReason())
                        .isEqualTo(FeedRefreshFailureReason.FETCH_LIMIT_EXCEEDED));
    }

    @Test
    @DisplayName("Aggregate refresh bytes exceeding 2 MiB throws FETCH_LIMIT_EXCEEDED")
    void aggregateLimitExceededThrows() throws Exception {
        aggregateCounter.set(HackerNewsClient.MAX_AGGREGATE_BYTES - 10);

        CloseableHttpResponse response = createMockResponse(200, "{\"id\": 101, \"title\": \"test\"}", "application/json", null);
        when(mockHttpClient.execute(any(HttpUriRequest.class))).thenReturn(response);

        assertThatThrownBy(() -> client.fetchItem(101L, defaultDeadlineNanos, aggregateCounter))
                .isInstanceOf(FeedRefreshException.class)
                .satisfies(ex -> assertThat(((FeedRefreshException) ex).getReason())
                        .isEqualTo(FeedRefreshFailureReason.FETCH_LIMIT_EXCEEDED));
    }

    @Test
    @DisplayName("Duplicate JSON keys in response are strictly detected and rejected")
    void duplicateJsonKeysRejected() throws Exception {
        String json = "{\"id\": 101, \"id\": 102, \"title\": \"Dup\"}";
        CloseableHttpResponse response = createMockResponse(200, json, "application/json", null);
        when(mockHttpClient.execute(any(HttpUriRequest.class))).thenReturn(response);

        assertThatThrownBy(() -> client.fetchItem(101L, defaultDeadlineNanos, aggregateCounter))
                .isInstanceOf(FeedRefreshException.class)
                .satisfies(ex -> assertThat(((FeedRefreshException) ex).getReason())
                        .isEqualTo(FeedRefreshFailureReason.UPSTREAM_INVALID_RESPONSE));
    }

    @Test
    @DisplayName("PolicyEnforcingDnsResolver resolves host and enforces destination policy")
    void policyEnforcingDnsResolverEnforcesSafety() throws Exception {
        DnsResolver delegate = mock(DnsResolver.class);
        HackerNewsDestinationPolicy destinationPolicy = mock(HackerNewsDestinationPolicy.class);
        HackerNewsClient.PolicyEnforcingDnsResolver resolver =
                new HackerNewsClient.PolicyEnforcingDnsResolver(destinationPolicy, delegate);

        InetAddress[] safeIps = new InetAddress[]{InetAddress.getByName("151.101.1.1")};
        when(delegate.resolve(HackerNewsDestinationPolicy.EXPECTED_HOST)).thenReturn(safeIps);

        InetAddress[] result = resolver.resolve(HackerNewsDestinationPolicy.EXPECTED_HOST);
        assertThat(result).isSameAs(safeIps);
        verify(destinationPolicy).validateResolvedAddresses(HackerNewsDestinationPolicy.EXPECTED_HOST, safeIps);
        resolver.shutdown();
    }

    @Test
    @DisplayName("DNS resolution exceeding 2s bound throws UPSTREAM_UNAVAILABLE")
    void dnsResolutionTimeoutThrows() throws Exception {
        DnsResolver slowDelegate = host -> {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                // interrupted
            }
            return new InetAddress[]{InetAddress.getByName("151.101.1.1")};
        };

        HackerNewsDestinationPolicy destinationPolicy = mock(HackerNewsDestinationPolicy.class);
        HackerNewsClient.PolicyEnforcingDnsResolver resolver =
                new HackerNewsClient.PolicyEnforcingDnsResolver(destinationPolicy, slowDelegate);

        assertThatThrownBy(() -> resolver.resolve(HackerNewsDestinationPolicy.EXPECTED_HOST))
                .isInstanceOf(FeedRefreshException.class)
                .satisfies(ex -> assertThat(((FeedRefreshException) ex).getReason())
                        .isEqualTo(FeedRefreshFailureReason.UPSTREAM_UNAVAILABLE));

        resolver.shutdown();
    }

    @Test
    @DisplayName("Connector boundary proves validated peer selection and direct TLS route without opening real socket")
    void connectorBoundaryProvesValidatedPeerAndDirectRoute() throws Exception {
        AtomicReference<InetSocketAddress> capturedRemoteAddress = new AtomicReference<>();
        AtomicReference<HttpHost> capturedHost = new AtomicReference<>();

        ConnectionSocketFactory recordingSocketFactory = new ConnectionSocketFactory() {
            @Override
            public Socket createSocket(HttpContext context) {
                return new Socket();
            }

            @Override
            public Socket connectSocket(int connectTimeout, Socket sock, HttpHost host,
                                        InetSocketAddress remoteAddress, InetSocketAddress localAddress,
                                        HttpContext context) throws IOException {
                capturedHost.set(host);
                capturedRemoteAddress.set(remoteAddress);
                throw new SocketTimeoutException("Recording connector simulated timeout - no real socket");
            }
        };

        InetAddress safeIp = InetAddress.getByName("151.101.65.69");
        DnsResolver customResolver = host -> new InetAddress[]{safeIp};

        try (HackerNewsClient connectorClient = new HackerNewsClient(policy, customResolver, null, recordingSocketFactory)) {
            assertThatThrownBy(() -> connectorClient.fetchTopStories(defaultDeadlineNanos, aggregateCounter))
                    .isInstanceOf(FeedRefreshException.class)
                    .satisfies(ex -> assertThat(((FeedRefreshException) ex).getReason())
                            .isEqualTo(FeedRefreshFailureReason.UPSTREAM_UNAVAILABLE));

            assertThat(capturedHost.get()).isNotNull();
            assertThat(capturedHost.get().getHostName()).isEqualTo("hacker-news.firebaseio.com");
            assertThat(capturedHost.get().getPort()).isEqualTo(443);
            assertThat(capturedHost.get().getSchemeName()).isEqualTo("https");

            assertThat(capturedRemoteAddress.get()).isNotNull();
            assertThat(capturedRemoteAddress.get().getAddress()).isEqualTo(safeIp);
            assertThat(capturedRemoteAddress.get().getPort()).isEqualTo(443);
        }
    }

    @Test
    @DisplayName("FR-02: Content-Type with unsupported charset throws UPSTREAM_INVALID_RESPONSE")
    void unsupportedCharsetThrowsUpstreamInvalidResponse() throws Exception {
        CloseableHttpResponse response = mock(CloseableHttpResponse.class);
        when(response.getStatusLine()).thenReturn(new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), 200, ""));
        HttpEntity entity = mock(HttpEntity.class);
        when(entity.getContent()).thenReturn(new ByteArrayInputStream("{\"id\": 101}".getBytes(StandardCharsets.UTF_8)));
        when(entity.getContentType()).thenReturn(new BasicHeader("Content-Type", "application/json; charset=unsupported-charset-fixture"));
        when(response.getEntity()).thenReturn(entity);

        when(mockHttpClient.execute(any(HttpUriRequest.class))).thenReturn(response);

        assertThatThrownBy(() -> client.fetchItem(101L, defaultDeadlineNanos, aggregateCounter))
                .isInstanceOf(FeedRefreshException.class)
                .satisfies(ex -> assertThat(((FeedRefreshException) ex).getReason())
                        .isEqualTo(FeedRefreshFailureReason.UPSTREAM_INVALID_RESPONSE));
    }

    @Test
    @DisplayName("FR-01: Bounded DNS resolver queue capacity exhaustion throws UPSTREAM_UNAVAILABLE")
    void dnsResolverQueueCapacityExhaustionThrows() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        DnsResolver blockingDelegate = host -> {
            try {
                latch.await(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return new InetAddress[]{InetAddress.getByName("151.101.1.1")};
        };

        HackerNewsDestinationPolicy destinationPolicy = mock(HackerNewsDestinationPolicy.class);
        HackerNewsClient.PolicyEnforcingDnsResolver resolver =
                new HackerNewsClient.PolicyEnforcingDnsResolver(destinationPolicy, blockingDelegate);

        ExecutorService clientCallers = Executors.newFixedThreadPool(10);
        List<Future<?>> futures = new ArrayList<>();
        try {
            // Fill 2 worker threads + 4 queue slots = 6 in flight
            for (int i = 0; i < 6; i++) {
                futures.add(clientCallers.submit(() -> resolver.resolve(HackerNewsDestinationPolicy.EXPECTED_HOST)));
            }
            Thread.sleep(100);

            // 7th submission must throw UPSTREAM_UNAVAILABLE due to rejected execution
            assertThatThrownBy(() -> resolver.resolve(HackerNewsDestinationPolicy.EXPECTED_HOST))
                    .isInstanceOf(FeedRefreshException.class)
                    .satisfies(ex -> assertThat(((FeedRefreshException) ex).getReason())
                            .isEqualTo(FeedRefreshFailureReason.UPSTREAM_UNAVAILABLE));
        } finally {
            latch.countDown();
            clientCallers.shutdownNow();
            resolver.shutdown();
        }
    }

    @Test
    @DisplayName("FR-01: Exchange abort watchdog triggers request abort when execution exceeds exchange deadline")
    void abortWatchdogTriggersOnSlowExchange() throws Exception {
        when(mockHttpClient.execute(any(HttpUriRequest.class))).thenAnswer(invocation -> {
            HttpGet req = invocation.getArgument(0);
            for (int i = 0; i < 100; i++) {
                if (req.isAborted()) {
                    throw new SocketTimeoutException("Request aborted by watchdog");
                }
                Thread.sleep(20);
            }
            return createMockResponse(200, "{\"id\": 101}", "application/json", null);
        });

        long shortDeadlineNanos = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(60);
        assertThatThrownBy(() -> client.fetchItem(101L, shortDeadlineNanos, aggregateCounter))
                .isInstanceOf(FeedRefreshException.class)
                .satisfies(ex -> assertThat(((FeedRefreshException) ex).getReason())
                        .isEqualTo(FeedRefreshFailureReason.UPSTREAM_UNAVAILABLE));
    }
}

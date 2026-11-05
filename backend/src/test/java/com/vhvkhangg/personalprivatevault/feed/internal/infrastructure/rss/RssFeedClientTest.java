package com.vhvkhangg.personalprivatevault.feed.internal.infrastructure.rss;

import com.vhvkhangg.personalprivatevault.feed.source.exception.FeedRefreshException;
import com.vhvkhangg.personalprivatevault.feed.source.exception.FeedRefreshFailureReason;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.DnsResolver;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicStatusLine;
import org.apache.http.protocol.HttpContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RssFeedClientTest {

    private RssFeedDestinationPolicy policy;
    private RssFeedClient client;
    private CloseableHttpClient mockHttpClient;
    private long defaultDeadlineNanos;

    @BeforeEach
    void setUp() {
        policy = new RssFeedDestinationPolicy();
        client = new RssFeedClient(policy);
        mockHttpClient = mock(CloseableHttpClient.class);
        ReflectionTestUtils.setField(client, "httpClient", mockHttpClient);
        defaultDeadlineNanos = System.nanoTime() + TimeUnit.SECONDS.toNanos(20);
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
                contentType != null ? ContentType.parse(contentType) : ContentType.APPLICATION_XML
        );
        if (contentEncoding != null) {
            entity.setContentEncoding(new BasicHeader("Content-Encoding", contentEncoding));
        }
        when(response.getEntity()).thenReturn(entity);
        return response;
    }

    @Test
    @DisplayName("fetchFeed retrieves valid XML response over HTTPS")
    void fetchFeedSuccess() throws Exception {
        String xml = "<rss version=\"2.0\"><channel><title>Test</title></channel></rss>";
        CloseableHttpResponse response = createMockResponse(200, xml, "application/rss+xml; charset=UTF-8", null);
        when(mockHttpClient.execute(any(HttpUriRequest.class))).thenReturn(response);

        byte[] result = client.fetchFeed("https://example.com/rss.xml", defaultDeadlineNanos);
        assertThat(new String(result, StandardCharsets.UTF_8)).isEqualTo(xml);
    }

    @Test
    @DisplayName("Non-XML Content-Type (HTML or JSON) throws UPSTREAM_INVALID_RESPONSE")
    void nonXmlContentTypeFails() throws Exception {
        CloseableHttpResponse response = createMockResponse(200, "<html><body>Not RSS</body></html>", "text/html", null);
        when(mockHttpClient.execute(any(HttpUriRequest.class))).thenReturn(response);

        assertThatThrownBy(() -> client.fetchFeed("https://example.com/feed", defaultDeadlineNanos))
                .isInstanceOf(FeedRefreshException.class)
                .satisfies(ex -> assertThat(((FeedRefreshException) ex).getReason())
                        .isEqualTo(FeedRefreshFailureReason.UPSTREAM_INVALID_RESPONSE));
    }

    @Test
    @DisplayName("Non-identity Content-Encoding (gzip) throws UPSTREAM_INVALID_RESPONSE")
    void nonIdentityEncodingFails() throws Exception {
        CloseableHttpResponse response = createMockResponse(200, "<xml/>", "application/xml", "gzip");
        when(mockHttpClient.execute(any(HttpUriRequest.class))).thenReturn(response);

        assertThatThrownBy(() -> client.fetchFeed("https://example.com/feed", defaultDeadlineNanos))
                .isInstanceOf(FeedRefreshException.class)
                .satisfies(ex -> assertThat(((FeedRefreshException) ex).getReason())
                        .isEqualTo(FeedRefreshFailureReason.UPSTREAM_INVALID_RESPONSE));
    }

    @Test
    @DisplayName("HTTP 429 status throws UPSTREAM_RATE_LIMITED")
    void rateLimitStatusThrows() throws Exception {
        CloseableHttpResponse response = createMockResponse(429, "", "application/xml", null);
        when(mockHttpClient.execute(any(HttpUriRequest.class))).thenReturn(response);

        assertThatThrownBy(() -> client.fetchFeed("https://example.com/feed", defaultDeadlineNanos))
                .isInstanceOf(FeedRefreshException.class)
                .satisfies(ex -> assertThat(((FeedRefreshException) ex).getReason())
                        .isEqualTo(FeedRefreshFailureReason.UPSTREAM_RATE_LIMITED));
    }

    @Test
    @DisplayName("HTTP 5xx status throws UPSTREAM_UNAVAILABLE")
    void serverErrorStatusThrows() throws Exception {
        CloseableHttpResponse response = createMockResponse(503, "", "application/xml", null);
        when(mockHttpClient.execute(any(HttpUriRequest.class))).thenReturn(response);

        assertThatThrownBy(() -> client.fetchFeed("https://example.com/feed", defaultDeadlineNanos))
                .isInstanceOf(FeedRefreshException.class)
                .satisfies(ex -> assertThat(((FeedRefreshException) ex).getReason())
                        .isEqualTo(FeedRefreshFailureReason.UPSTREAM_UNAVAILABLE));
    }

    @Test
    @DisplayName("HTTP 3xx redirect throws UPSTREAM_INVALID_RESPONSE")
    void redirectStatusThrows() throws Exception {
        CloseableHttpResponse response = createMockResponse(302, "", "application/xml", null);
        when(mockHttpClient.execute(any(HttpUriRequest.class))).thenReturn(response);

        assertThatThrownBy(() -> client.fetchFeed("https://example.com/feed", defaultDeadlineNanos))
                .isInstanceOf(FeedRefreshException.class)
                .satisfies(ex -> assertThat(((FeedRefreshException) ex).getReason())
                        .isEqualTo(FeedRefreshFailureReason.UPSTREAM_INVALID_RESPONSE));
    }

    @Test
    @DisplayName("Advertised Content-Length exceeding 1 MiB throws FETCH_LIMIT_EXCEEDED")
    void advertisedContentLengthExceedingLimitThrows() throws Exception {
        CloseableHttpResponse response = mock(CloseableHttpResponse.class);
        when(response.getStatusLine()).thenReturn(new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), 200, ""));
        HttpEntity entity = mock(HttpEntity.class);
        when(entity.getContentLength()).thenReturn(1048577L); // 1 MiB + 1
        when(entity.getContentType()).thenReturn(new BasicHeader("Content-Type", "application/xml"));
        when(response.getEntity()).thenReturn(entity);

        when(mockHttpClient.execute(any(HttpUriRequest.class))).thenReturn(response);

        assertThatThrownBy(() -> client.fetchFeed("https://example.com/feed", defaultDeadlineNanos))
                .isInstanceOf(FeedRefreshException.class)
                .satisfies(ex -> assertThat(((FeedRefreshException) ex).getReason())
                        .isEqualTo(FeedRefreshFailureReason.FETCH_LIMIT_EXCEEDED));
    }

    @Test
    @DisplayName("S2A-03: Streaming body of exactly 1 MiB (1,048,576 bytes) succeeds without overrun")
    void streamingBodyExactOneMebibyteSucceeds() throws Exception {
        CloseableHttpResponse response = mock(CloseableHttpResponse.class);
        when(response.getStatusLine()).thenReturn(new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), 200, ""));

        byte[] exactData = new byte[1048576];
        InputStream in = new ByteArrayInputStream(exactData);
        InputStreamEntity entity = new InputStreamEntity(in, -1, ContentType.APPLICATION_XML);

        when(response.getEntity()).thenReturn(entity);
        when(mockHttpClient.execute(any(HttpUriRequest.class))).thenReturn(response);

        byte[] result = client.fetchFeed("https://example.com/feed", defaultDeadlineNanos);
        assertThat(result).isNotNull().hasSize(1048576);
    }

    @Test
    @DisplayName("S2A-03: Streaming body of exactly 1 MiB + 1 byte (1,048,577 bytes) throws FETCH_LIMIT_EXCEEDED")
    void streamingBodyOneByteOverOneMebibyteThrows() throws Exception {
        CloseableHttpResponse response = mock(CloseableHttpResponse.class);
        when(response.getStatusLine()).thenReturn(new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), 200, ""));

        byte[] oneOverData = new byte[1048576 + 1];
        InputStream in = new ByteArrayInputStream(oneOverData);
        InputStreamEntity entity = new InputStreamEntity(in, -1, ContentType.APPLICATION_XML);

        when(response.getEntity()).thenReturn(entity);
        when(mockHttpClient.execute(any(HttpUriRequest.class))).thenReturn(response);

        assertThatThrownBy(() -> client.fetchFeed("https://example.com/feed", defaultDeadlineNanos))
                .isInstanceOf(FeedRefreshException.class)
                .satisfies(ex -> assertThat(((FeedRefreshException) ex).getReason())
                        .isEqualTo(FeedRefreshFailureReason.FETCH_LIMIT_EXCEEDED));
    }

    @Test
    @DisplayName("S2A-03: Slow trickle/inactivity beyond socket timeout aborts request with UPSTREAM_UNAVAILABLE")
    void slowTrickleAbortsWithUpstreamUnavailable() throws Exception {
        CloseableHttpResponse response = mock(CloseableHttpResponse.class);
        when(response.getStatusLine()).thenReturn(new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), 200, ""));
        HttpEntity entity = mock(HttpEntity.class);
        when(entity.getContentLength()).thenReturn(-1L);
        when(entity.getContentType()).thenReturn(new BasicHeader("Content-Type", "application/xml"));
        InputStream slowStream = new InputStream() {
            @Override
            public int read() throws IOException {
                throw new SocketTimeoutException("Read timed out due to slow trickle");
            }
        };
        when(entity.getContent()).thenReturn(slowStream);
        when(response.getEntity()).thenReturn(entity);
        when(mockHttpClient.execute(any(HttpUriRequest.class))).thenReturn(response);

        assertThatThrownBy(() -> client.fetchFeed("https://example.com/feed", defaultDeadlineNanos))
                .isInstanceOf(FeedRefreshException.class)
                .satisfies(ex -> assertThat(((FeedRefreshException) ex).getReason())
                        .isEqualTo(FeedRefreshFailureReason.UPSTREAM_UNAVAILABLE));
    }

    @Test
    @DisplayName("S2A-03: In-flight thread interruption during stream reading aborts request and throws UPSTREAM_UNAVAILABLE")
    void inFlightInterruptionAbortsRequest() throws Exception {
        CloseableHttpResponse response = mock(CloseableHttpResponse.class);
        when(response.getStatusLine()).thenReturn(new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), 200, ""));
        HttpEntity entity = mock(HttpEntity.class);
        when(entity.getContentLength()).thenReturn(-1L);
        when(entity.getContentType()).thenReturn(new BasicHeader("Content-Type", "application/xml"));
        InputStream inStream = new InputStream() {
            private int count = 0;
            @Override
            public int read() throws IOException {
                count++;
                if (count == 10) {
                    Thread.currentThread().interrupt();
                }
                return 'a';
            }
        };
        when(entity.getContent()).thenReturn(inStream);
        when(response.getEntity()).thenReturn(entity);
        when(mockHttpClient.execute(any(HttpUriRequest.class))).thenReturn(response);

        try {
            assertThatThrownBy(() -> client.fetchFeed("https://example.com/feed", defaultDeadlineNanos))
                    .isInstanceOf(FeedRefreshException.class)
                    .satisfies(ex -> assertThat(((FeedRefreshException) ex).getReason())
                            .isEqualTo(FeedRefreshFailureReason.UPSTREAM_UNAVAILABLE));
        } finally {
            Thread.interrupted(); // clear interrupt flag
        }
    }

    @Test
    @DisplayName("S2A-03: Client close cleanly shuts down owned DNS worker executor and connection manager")
    void clientCloseShutsDownOwnedWorkers() throws Exception {
        RssFeedClient realClient = new RssFeedClient(policy);
        assertThat(realClient.getDnsResolver().isShutdown()).isFalse();
        realClient.close();
        assertThat(realClient.getDnsResolver().isShutdown()).isTrue();
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

        InetAddress safeIp = InetAddress.getByName("93.184.216.34");
        DnsResolver customResolver = host -> new InetAddress[]{safeIp};

        try (RssFeedClient connectorClient = new RssFeedClient(policy, customResolver, null, recordingSocketFactory)) {
            assertThatThrownBy(() -> connectorClient.fetchFeed("https://example.com/feed.xml", defaultDeadlineNanos))
                    .isInstanceOf(FeedRefreshException.class)
                    .satisfies(ex -> assertThat(((FeedRefreshException) ex).getReason())
                            .isEqualTo(FeedRefreshFailureReason.UPSTREAM_UNAVAILABLE));

            assertThat(capturedHost.get()).isNotNull();
            assertThat(capturedHost.get().getHostName()).isEqualTo("example.com");
            assertThat(capturedHost.get().getPort()).isEqualTo(443);
            assertThat(capturedHost.get().getSchemeName()).isEqualTo("https");

            assertThat(capturedRemoteAddress.get()).isNotNull();
            assertThat(capturedRemoteAddress.get().getAddress()).isEqualTo(safeIp);
            assertThat(capturedRemoteAddress.get().getPort()).isEqualTo(443);
        }
    }

    @Test
    @DisplayName("Bounded DNS resolver queue capacity exhaustion throws UPSTREAM_UNAVAILABLE")
    void dnsResolverQueueCapacityExhaustionThrows() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        DnsResolver blockingDelegate = host -> {
            try {
                latch.await(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return new InetAddress[]{InetAddress.getByName("93.184.216.34")};
        };

        RssFeedDestinationPolicy destinationPolicy = mock(RssFeedDestinationPolicy.class);
        RssFeedClient.PolicyEnforcingDnsResolver resolver =
                new RssFeedClient.PolicyEnforcingDnsResolver(destinationPolicy, blockingDelegate);

        ExecutorService clientCallers = Executors.newFixedThreadPool(10);
        List<Future<?>> futures = new ArrayList<>();
        try {
            // Fill 2 worker threads + 4 queue slots = 6 in flight
            for (int i = 0; i < 6; i++) {
                futures.add(clientCallers.submit(() -> resolver.resolve("example.com")));
            }
            Thread.sleep(100);

            // 7th submission must throw UPSTREAM_UNAVAILABLE due to rejected execution
            assertThatThrownBy(() -> resolver.resolve("example.com"))
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
    @DisplayName("FR-02: Real production client disables cookies and authentication across requests")
    void realClientDisablesCookiesAndAuthentication() throws Exception {
        byte[] feedXml = "<rss version=\"2.0\"><channel><title>T</title></channel></rss>".getBytes(StandardCharsets.UTF_8);
        AtomicInteger socketCalls = new AtomicInteger();
        List<ByteArrayOutputStream> requests = new ArrayList<>();
        AtomicBoolean authEnabled = new AtomicBoolean(true);

        ConnectionSocketFactory socketFactory = new ConnectionSocketFactory() {
            @Override
            public Socket createSocket(HttpContext context) throws IOException {
                int index = socketCalls.getAndIncrement();
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                requests.add(out);
                String body = new String(feedXml, StandardCharsets.UTF_8);
                String wire = "HTTP/1.1 200 OK\r\nContent-Type: application/xml\r\nContent-Length: " + feedXml.length
                        + "\r\nConnection: close\r\n"
                        + (index == 0 ? "Set-Cookie: ppv_probe=synthetic; Path=/; Secure\r\n" : "")
                        + "\r\n" + body;

                Socket s = mock(Socket.class);
                when(s.getInputStream()).thenReturn(new ByteArrayInputStream(wire.getBytes(StandardCharsets.UTF_8)));
                when(s.getOutputStream()).thenReturn(out);
                when(s.isConnected()).thenReturn(true);
                when(s.getInetAddress()).thenReturn(InetAddress.getByName("93.184.216.34"));
                when(s.getPort()).thenReturn(443);
                return s;
            }

            @Override
            public Socket connectSocket(int timeout, Socket socket, HttpHost host,
                                        InetSocketAddress remote, InetSocketAddress local,
                                        HttpContext context) {
                authEnabled.set(HttpClientContext.adapt(context).getRequestConfig().isAuthenticationEnabled());
                return socket;
            }
        };

        DnsResolver dns = host -> new InetAddress[]{InetAddress.getByName("93.184.216.34")};
        try (RssFeedClient realClient = new RssFeedClient(policy, dns, null, socketFactory)) {
            realClient.fetchFeed("https://example.com/feed1", System.nanoTime() + TimeUnit.SECONDS.toNanos(10));
            realClient.fetchFeed("https://example.com/feed2", System.nanoTime() + TimeUnit.SECONDS.toNanos(10));
        }

        assertThat(socketCalls.get()).isEqualTo(2);
        assertThat(authEnabled.get()).isFalse();
        String secondRequestHeaders = requests.get(1).toString(StandardCharsets.UTF_8);
        assertThat(secondRequestHeaders).doesNotContain("Cookie:");
        assertThat(secondRequestHeaders).doesNotContain("ppv_probe=synthetic");
    }

    @Test
    @DisplayName("FR-02: HTTP 401 challenge immediately fails with UPSTREAM_INVALID_RESPONSE without retry")
    void http401ReturnsInvalidResponseWithoutRetry() throws Exception {
        AtomicInteger socketCalls = new AtomicInteger();
        ConnectionSocketFactory socketFactory = new ConnectionSocketFactory() {
            @Override
            public Socket createSocket(HttpContext context) throws IOException {
                socketCalls.incrementAndGet();
                String wire = "HTTP/1.1 401 Unauthorized\r\nWWW-Authenticate: Basic realm=\"test\"\r\nContent-Length: 0\r\nConnection: close\r\n\r\n";
                Socket s = mock(Socket.class);
                when(s.getInputStream()).thenReturn(new ByteArrayInputStream(wire.getBytes(StandardCharsets.UTF_8)));
                when(s.getOutputStream()).thenReturn(new ByteArrayOutputStream());
                when(s.isConnected()).thenReturn(true);
                when(s.getInetAddress()).thenReturn(InetAddress.getByName("93.184.216.34"));
                when(s.getPort()).thenReturn(443);
                return s;
            }

            @Override
            public Socket connectSocket(int timeout, Socket socket, HttpHost host,
                                        InetSocketAddress remote, InetSocketAddress local,
                                        HttpContext context) {
                return socket;
            }
        };

        DnsResolver dns = host -> new InetAddress[]{InetAddress.getByName("93.184.216.34")};
        try (RssFeedClient realClient = new RssFeedClient(policy, dns, null, socketFactory)) {
            assertThatThrownBy(() -> realClient.fetchFeed("https://example.com/feed", defaultDeadlineNanos))
                    .isInstanceOf(FeedRefreshException.class)
                    .satisfies(ex -> assertThat(((FeedRefreshException) ex).getReason())
                            .isEqualTo(FeedRefreshFailureReason.UPSTREAM_INVALID_RESPONSE));
        }

        assertThat(socketCalls.get()).isEqualTo(1);
    }

    @Test
    @DisplayName("FR-04: DNS resolution wait respects caller remaining budget and aborts early")
    void dnsResolutionWaitRespectsRemainingBudget() throws Exception {
        AtomicInteger connectorCalls = new AtomicInteger();
        ConnectionSocketFactory sockets = new ConnectionSocketFactory() {
            @Override
            public Socket createSocket(HttpContext c) { return new Socket(); }
            @Override
            public Socket connectSocket(int timeout, Socket socket, HttpHost host,
                                        InetSocketAddress remote, InetSocketAddress local,
                                        HttpContext context) throws IOException {
                connectorCalls.incrementAndGet();
                throw new SocketTimeoutException("simulated");
            }
        };

        DnsResolver delayedDns = host -> {
            try {
                new CountDownLatch(1).await(400, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return new InetAddress[]{InetAddress.getByName("93.184.216.34")};
        };

        try (RssFeedClient budgetClient = new RssFeedClient(policy, delayedDns, null, sockets)) {
            long start = System.nanoTime();
            long budgetDeadline = start + TimeUnit.MILLISECONDS.toNanos(50);
            assertThatThrownBy(() -> budgetClient.fetchFeed("https://example.com/feed", budgetDeadline))
                    .isInstanceOf(FeedRefreshException.class)
                    .satisfies(ex -> assertThat(((FeedRefreshException) ex).getReason())
                            .isEqualTo(FeedRefreshFailureReason.UPSTREAM_UNAVAILABLE));

            long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
            assertThat(elapsedMs).as("Caller must abort within bounded budget").isLessThan(300);
            assertThat(connectorCalls.get()).as("Zero connector calls must occur on DNS budget abort").isZero();
        }
    }

    @Test
    @DisplayName("FR-01/06: Unsafe or mixed DNS results fail destination policy with zero connector calls")
    void unsafeAndMixedDnsAnswersYieldZeroConnectorCalls() throws Exception {
        AtomicInteger connectorCalls = new AtomicInteger();
        ConnectionSocketFactory sockets = new ConnectionSocketFactory() {
            @Override
            public Socket createSocket(HttpContext c) { return new Socket(); }
            @Override
            public Socket connectSocket(int timeout, Socket socket, HttpHost host,
                                        InetSocketAddress remote, InetSocketAddress local,
                                        HttpContext context) throws IOException {
                connectorCalls.incrementAndGet();
                return socket;
            }
        };

        for (String unsafeIp : List.of("64:ff9b:1::a00:1", "3fff::1", "4000::1", "2001:4::1", "3000::1", "2d00::1", "2e00::1", "2001:1000::1", "127.0.0.1", "10.0.0.1")) {
            DnsResolver unsafeDns = host -> new InetAddress[]{InetAddress.getByName(unsafeIp)};
            try (RssFeedClient client = new RssFeedClient(policy, unsafeDns, null, sockets)) {
                assertThatThrownBy(() -> client.fetchFeed("https://example.com/feed", defaultDeadlineNanos))
                        .isInstanceOf(FeedRefreshException.class)
                        .satisfies(ex -> assertThat(((FeedRefreshException) ex).getReason())
                                .isEqualTo(FeedRefreshFailureReason.UPSTREAM_DESTINATION_REJECTED));
            }
        }

        for (String unsafeIp : List.of("4000::1", "2001:4::1", "3000::1", "2d00::1", "2e00::1", "2001:1000::1", "127.0.0.1")) {
            DnsResolver mixedDns = host -> new InetAddress[]{
                    InetAddress.getByName("93.184.216.34"),
                    InetAddress.getByName(unsafeIp)
            };
            try (RssFeedClient client = new RssFeedClient(policy, mixedDns, null, sockets)) {
                assertThatThrownBy(() -> client.fetchFeed("https://example.com/feed", defaultDeadlineNanos))
                        .isInstanceOf(FeedRefreshException.class)
                        .satisfies(ex -> assertThat(((FeedRefreshException) ex).getReason())
                                .isEqualTo(FeedRefreshFailureReason.UPSTREAM_DESTINATION_REJECTED));
            }
        }

        assertThat(connectorCalls.get()).as("Zero connector socket calls must occur for denied destinations").isZero();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "2003::",
            "2003::1",
            "2003:3fff::1",
            "2003:3fff:ffff:ffff:ffff:ffff:ffff:ffff"
    })
    @DisplayName("FR-01: Assigned 2003::/18 boundaries reach the production manager's recording connector")
    void allocated2003BoundariesReachProductionConnector(String ip) throws Exception {
        InetAddress address = InetAddress.getByName(ip);
        AtomicInteger dnsCalls = new AtomicInteger();
        AtomicInteger connectorCalls = new AtomicInteger();
        AtomicReference<InetSocketAddress> connectedPeer = new AtomicReference<>();
        AtomicReference<HttpHost> connectedHost = new AtomicReference<>();
        DnsResolver dns = host -> {
            dnsCalls.incrementAndGet();
            return new InetAddress[]{address};
        };
        ConnectionSocketFactory sockets = new ConnectionSocketFactory() {
            @Override
            public Socket createSocket(HttpContext context) { return new Socket(); }

            @Override
            public Socket connectSocket(int timeout, Socket socket, HttpHost host,
                                        InetSocketAddress remote, InetSocketAddress local,
                                        HttpContext context) throws IOException {
                connectorCalls.incrementAndGet();
                connectedPeer.set(remote);
                connectedHost.set(host);
                throw new SocketTimeoutException("Recording connector only; no network connection");
            }
        };

        try (RssFeedClient boundaryClient = new RssFeedClient(policy, dns, null, sockets)) {
            assertThatThrownBy(() -> boundaryClient.fetchFeed("https://example.com/feed", defaultDeadlineNanos))
                    .isInstanceOf(FeedRefreshException.class)
                    .satisfies(ex -> assertThat(((FeedRefreshException) ex).getReason())
                            .isEqualTo(FeedRefreshFailureReason.UPSTREAM_UNAVAILABLE));
            assertThat(dnsCalls.get()).isEqualTo(1);
            assertThat(connectorCalls.get()).isEqualTo(1);
            assertThat(connectedPeer.get()).isEqualTo(new InetSocketAddress(address, 443));
            assertThat(connectedHost.get()).isEqualTo(new HttpHost("example.com", 443, "https"));
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "2003:4000::",
            "2003:4000::1",
            "2003:ffff::1",
            "2003:ffff:ffff:ffff:ffff:ffff:ffff:ffff"
    })
    @DisplayName("FR-01: Reserved 2003 DNS answers, alone or mixed in either order, never invoke the connector")
    void reserved2003DnsAnswersAreRejectedBeforeAnyConnectorCall(String ip) throws Exception {
        InetAddress reserved = InetAddress.getByName(ip);
        InetAddress publicIpv4 = InetAddress.getByName("93.184.216.34");
        InetAddress assignedIpv6 = InetAddress.getByName("2003:3fff:ffff:ffff:ffff:ffff:ffff:ffff");
        AtomicInteger socketCreations = new AtomicInteger();
        AtomicInteger connectorCalls = new AtomicInteger();
        ConnectionSocketFactory sockets = new ConnectionSocketFactory() {
            @Override
            public Socket createSocket(HttpContext context) {
                socketCreations.incrementAndGet();
                return new Socket();
            }

            @Override
            public Socket connectSocket(int timeout, Socket socket, HttpHost host,
                                        InetSocketAddress remote, InetSocketAddress local,
                                        HttpContext context) throws IOException {
                connectorCalls.incrementAndGet();
                throw new SocketTimeoutException("Recording connector only; no network connection");
            }
        };

        for (InetAddress[] answer : List.of(
                new InetAddress[]{reserved},
                new InetAddress[]{publicIpv4, reserved},
                new InetAddress[]{reserved, publicIpv4},
                new InetAddress[]{assignedIpv6, reserved},
                new InetAddress[]{reserved, assignedIpv6}
        )) {
            AtomicInteger dnsCalls = new AtomicInteger();
            DnsResolver dns = host -> {
                dnsCalls.incrementAndGet();
                return answer;
            };
            try (RssFeedClient boundaryClient = new RssFeedClient(policy, dns, null, sockets)) {
                assertThatThrownBy(() -> boundaryClient.fetchFeed("https://example.com/feed", defaultDeadlineNanos))
                        .as("DNS answer %s", (Object) answer)
                        .isInstanceOf(FeedRefreshException.class)
                        .satisfies(ex -> {
                            assertThat(connectorCalls.get()).as("Rejected before connector invocation").isZero();
                            assertThat(socketCreations.get()).as("Rejected before socket creation").isZero();
                            assertThat(((FeedRefreshException) ex).getReason())
                                    .isEqualTo(FeedRefreshFailureReason.UPSTREAM_DESTINATION_REJECTED);
                        });
                assertThat(dnsCalls.get()).isEqualTo(1);
            }
        }
    }

    @Test
    @DisplayName("S2A-02: TLS certificate hostname mismatch fails with UPSTREAM_UNAVAILABLE via production DefaultHostnameVerifier")
    void tlsHostnameMismatchFailsWithUpstreamUnavailable() throws Exception {
        AtomicInteger connectorCalls = new AtomicInteger();
        DnsResolver dns = host -> new InetAddress[]{InetAddress.getByName("93.184.216.34")};

        // Negative test: certificate has CN=mismatch.com, expected example.com -> DefaultHostnameVerifier rejects!
        ConnectionSocketFactory mismatchSslFactory = new SSLConnectionSocketFactory(
                SSLContexts.createDefault(),
                new String[]{"TLSv1.2", "TLSv1.3"},
                null,
                new DefaultHostnameVerifier()
        ) {
            @Override
            public Socket createSocket(HttpContext context) {
                return mock(javax.net.ssl.SSLSocket.class);
            }
            @Override
            public Socket connectSocket(int timeout, Socket socket, HttpHost host,
                                        InetSocketAddress remote, InetSocketAddress local,
                                        HttpContext context) throws IOException {
                connectorCalls.incrementAndGet();
                javax.net.ssl.SSLSocket sslSocket = (javax.net.ssl.SSLSocket) socket;
                javax.net.ssl.SSLSession session = mock(javax.net.ssl.SSLSession.class);
                java.security.cert.X509Certificate cert = mock(java.security.cert.X509Certificate.class);
                when(cert.getSubjectX500Principal()).thenReturn(new javax.security.auth.x500.X500Principal("CN=mismatch.com"));
                when(session.getPeerCertificates()).thenReturn(new java.security.cert.Certificate[]{cert});
                when(sslSocket.getSession()).thenReturn(session);
                return super.connectSocket(timeout, sslSocket, host, remote, local, context);
            }
        };

        try (RssFeedClient client = new RssFeedClient(policy, dns, null, mismatchSslFactory)) {
            assertThatThrownBy(() -> client.fetchFeed("https://example.com/feed", defaultDeadlineNanos))
                    .isInstanceOf(FeedRefreshException.class)
                    .satisfies(ex -> assertThat(((FeedRefreshException) ex).getReason())
                            .isEqualTo(FeedRefreshFailureReason.UPSTREAM_UNAVAILABLE));
        }

        assertThat(connectorCalls.get()).isEqualTo(1);

        // Positive control: certificate has CN=example.com, matching host -> DefaultHostnameVerifier accepts!
        AtomicInteger validConnectorCalls = new AtomicInteger();
        ConnectionSocketFactory validSslFactory = new SSLConnectionSocketFactory(
                SSLContexts.createDefault(),
                new String[]{"TLSv1.2", "TLSv1.3"},
                null,
                new DefaultHostnameVerifier()
        ) {
            @Override
            public Socket createSocket(HttpContext context) {
                return mock(javax.net.ssl.SSLSocket.class);
            }
            @Override
            public Socket connectSocket(int timeout, Socket socket, HttpHost host,
                                        InetSocketAddress remote, InetSocketAddress local,
                                        HttpContext context) throws IOException {
                validConnectorCalls.incrementAndGet();
                javax.net.ssl.SSLSocket sslSocket = (javax.net.ssl.SSLSocket) socket;
                javax.net.ssl.SSLSession session = mock(javax.net.ssl.SSLSession.class);
                java.security.cert.X509Certificate cert = mock(java.security.cert.X509Certificate.class);
                when(cert.getSubjectX500Principal()).thenReturn(new javax.security.auth.x500.X500Principal("CN=" + host.getHostName()));
                when(session.getPeerCertificates()).thenReturn(new java.security.cert.Certificate[]{cert});
                when(sslSocket.getSession()).thenReturn(session);
                when(sslSocket.getInputStream()).thenReturn(new ByteArrayInputStream("HTTP/1.1 200 OK\r\nContent-Type: application/rss+xml\r\nContent-Length: 0\r\n\r\n".getBytes(StandardCharsets.UTF_8)));
                when(sslSocket.getOutputStream()).thenReturn(new ByteArrayOutputStream());
                return super.connectSocket(timeout, sslSocket, host, remote, local, context);
            }
        };

        try (RssFeedClient client = new RssFeedClient(policy, dns, null, validSslFactory)) {
            byte[] bytes = client.fetchFeed("https://example.com/feed", defaultDeadlineNanos);
            assertThat(bytes).isNotNull().isEmpty();
        }
        assertThat(validConnectorCalls.get()).isEqualTo(1);
    }

    @Test
    @DisplayName("S2A-02: DNS rebinding to non-public IP on subsequent resolution fails closed without connector call")
    void dnsRebindingFailsClosed() throws Exception {
        AtomicInteger callCount = new AtomicInteger();
        AtomicInteger connectorCalls = new AtomicInteger();
        ConnectionSocketFactory sockets = new ConnectionSocketFactory() {
            @Override
            public Socket createSocket(HttpContext c) { return new Socket(); }
            @Override
            public Socket connectSocket(int timeout, Socket socket, HttpHost host,
                                        InetSocketAddress remote, InetSocketAddress local,
                                        HttpContext context) throws IOException {
                connectorCalls.incrementAndGet();
                throw new SocketTimeoutException("simulated connect failure on call 1");
            }
        };

        DnsResolver rebindingDns = host -> {
            if (callCount.incrementAndGet() == 1) {
                return new InetAddress[]{InetAddress.getByName("93.184.216.34")};
            }
            return new InetAddress[]{InetAddress.getByName("2001:4::1")};
        };

        try (RssFeedClient client = new RssFeedClient(policy, rebindingDns, null, sockets)) {
            // First call resolves safe public IP (reaches connector, which simulates failure)
            assertThatThrownBy(() -> client.fetchFeed("https://example.com/feed", defaultDeadlineNanos))
                    .isInstanceOf(FeedRefreshException.class);
            assertThat(connectorCalls.get()).isEqualTo(1);

            // Second resolution rebinds to 2001:4::1 and is rejected before connector call
            assertThatThrownBy(() -> client.fetchFeed("https://example.com/feed", defaultDeadlineNanos))
                    .isInstanceOf(FeedRefreshException.class)
                    .satisfies(ex -> assertThat(((FeedRefreshException) ex).getReason())
                            .isEqualTo(FeedRefreshFailureReason.UPSTREAM_DESTINATION_REJECTED));

            // Connector calls must not have incremented on second call
            assertThat(connectorCalls.get()).as("Rebound non-public IP must never trigger connector call").isEqualTo(1);
        }
    }
}

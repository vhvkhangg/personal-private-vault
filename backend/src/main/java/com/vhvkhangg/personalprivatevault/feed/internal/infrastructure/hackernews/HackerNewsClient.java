package com.vhvkhangg.personalprivatevault.feed.internal.infrastructure.hackernews;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.StreamReadConstraints;
import com.fasterxml.jackson.core.StreamReadFeature;
import com.fasterxml.jackson.core.exc.StreamConstraintsException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vhvkhangg.personalprivatevault.feed.source.exception.FeedRefreshException;
import com.vhvkhangg.personalprivatevault.feed.source.exception.FeedRefreshFailureReason;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.DnsResolver;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.conn.SystemDefaultDnsResolver;
import org.apache.http.ssl.SSLContexts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * HTTP client adapter for official Hacker News Firebase API endpoints with strict bounds,
 * DNS validation, streaming byte limits, and parser constraints.
 */
@Component
public class HackerNewsClient implements AutoCloseable {

    public static final long MAX_TOPLIST_BYTES = 64 * 1024L; // 64 KiB
    public static final long MAX_ITEM_BYTES = 256 * 1024L; // 256 KiB
    public static final long MAX_AGGREGATE_BYTES = 2 * 1024 * 1024L; // 2 MiB

    public static final int INDIVIDUAL_EXCHANGE_TIMEOUT_MS = 5000; // 5s individual exchange
    private static final int CONNECT_TIMEOUT_MS = 2000; // 2s connect
    private static final int SOCKET_TIMEOUT_MS = 2000; // 2s socket inactivity
    private static final int CONNECTION_REQUEST_TIMEOUT_MS = 1000; // 1s connection lease
    private static final long DNS_TIMEOUT_SECONDS = 2L; // 2s DNS caller bound

    private static final ScheduledExecutorService ABORT_WATCHDOG = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "hn-abort-watchdog");
        t.setDaemon(true);
        return t;
    });

    private final HackerNewsDestinationPolicy destinationPolicy;
    private final CloseableHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final JsonFactory jsonFactory;
    private final PolicyEnforcingDnsResolver dnsResolver;

    @Autowired
    public HackerNewsClient(HackerNewsDestinationPolicy destinationPolicy) {
        this(destinationPolicy, null, null, null);
    }

    public HackerNewsClient(
            HackerNewsDestinationPolicy destinationPolicy,
            DnsResolver customDnsResolver,
            HttpClientConnectionManager customConnectionManager
    ) {
        this(destinationPolicy, customDnsResolver, customConnectionManager, null);
    }

    public HackerNewsClient(
            HackerNewsDestinationPolicy destinationPolicy,
            DnsResolver customDnsResolver,
            HttpClientConnectionManager customConnectionManager,
            ConnectionSocketFactory customSslSocketFactory
    ) {
        this.destinationPolicy = destinationPolicy;
        this.jsonFactory = createConstrainedJsonFactory();
        this.objectMapper = new ObjectMapper(jsonFactory);

        if (customDnsResolver instanceof PolicyEnforcingDnsResolver per) {
            this.dnsResolver = per;
        } else if (customDnsResolver != null) {
            this.dnsResolver = new PolicyEnforcingDnsResolver(destinationPolicy, customDnsResolver);
        } else {
            this.dnsResolver = new PolicyEnforcingDnsResolver(destinationPolicy, SystemDefaultDnsResolver.INSTANCE);
        }

        HttpClientBuilder builder = HttpClientBuilder.create()
                .disableRedirectHandling()
                .disableAutomaticRetries()
                .disableContentCompression()
                .setSSLHostnameVerifier(new DefaultHostnameVerifier());

        if (customConnectionManager != null) {
            builder.setConnectionManager(customConnectionManager);
        } else {
            ConnectionSocketFactory sslFactory = customSslSocketFactory != null
                    ? customSslSocketFactory
                    : new SSLConnectionSocketFactory(
                            SSLContexts.createDefault(),
                            new String[]{"TLSv1.2", "TLSv1.3"},
                            null,
                            new DefaultHostnameVerifier()
                    );

            PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(
                    org.apache.http.config.RegistryBuilder.<ConnectionSocketFactory>create()
                            .register("https", sslFactory)
                            .build(),
                    this.dnsResolver
            );
            cm.setMaxTotal(2);
            cm.setDefaultMaxPerRoute(2);
            builder.setConnectionManager(cm);
        }

        this.httpClient = builder.build();
    }

    public List<Long> fetchTopStories(long deadlineNanos, AtomicLong aggregateBytesCounter) {
        URI uri = URI.create("https://" + HackerNewsDestinationPolicy.EXPECTED_HOST + "/v0/topstories.json");
        JsonNode root = executeAndParse(uri, MAX_TOPLIST_BYTES, deadlineNanos, aggregateBytesCounter);

        if (!root.isArray()) {
            throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_INVALID_RESPONSE, "Top stories root must be a JSON array");
        }
        if (root.size() > 1000) {
            throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_INVALID_RESPONSE, "Top stories list exceeds 1,000 item limit");
        }

        List<Long> ids = new ArrayList<>(root.size());
        Set<Long> seen = new HashSet<>(root.size());
        for (JsonNode element : root) {
            if (!element.isIntegralNumber() || !element.canConvertToLong()) {
                throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_INVALID_RESPONSE, "Top story ID must be an integral number");
            }
            long id = element.asLong();
            if (id <= 0 || !seen.add(id)) {
                throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_INVALID_RESPONSE, "Top story IDs must be positive and distinct");
            }
            ids.add(id);
        }
        return ids;
    }

    public JsonNode fetchItem(long storyId, long deadlineNanos, AtomicLong aggregateBytesCounter) {
        if (storyId <= 0) {
            throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_INVALID_RESPONSE, "Story ID must be positive");
        }
        URI uri = URI.create("https://" + HackerNewsDestinationPolicy.EXPECTED_HOST + "/v0/item/" + storyId + ".json");
        return executeAndParse(uri, MAX_ITEM_BYTES, deadlineNanos, aggregateBytesCounter);
    }

    private JsonNode executeAndParse(URI uri, long maxResponseBytes, long deadlineNanos, AtomicLong aggregateBytesCounter) {
        destinationPolicy.validateUri(uri);
        destinationPolicy.checkProxySafety();

        long nowNanos = System.nanoTime();
        long remainingNanos = deadlineNanos - nowNanos;
        if (remainingNanos <= 0) {
            throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_UNAVAILABLE, "Refresh deadline exceeded before request");
        }

        long exchangeDeadlineNanos = Math.min(deadlineNanos, nowNanos + TimeUnit.MILLISECONDS.toNanos(INDIVIDUAL_EXCHANGE_TIMEOUT_MS));
        long exchangeRemainingNanos = exchangeDeadlineNanos - nowNanos;
        if (exchangeRemainingNanos <= 0) {
            throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_UNAVAILABLE, "Exchange deadline expired before request");
        }

        int socketTimeout = (int) Math.min(SOCKET_TIMEOUT_MS, Math.max(1, TimeUnit.NANOSECONDS.toMillis(exchangeRemainingNanos)));
        int connectTimeout = (int) Math.min(CONNECT_TIMEOUT_MS, Math.max(1, TimeUnit.NANOSECONDS.toMillis(exchangeRemainingNanos)));

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(connectTimeout)
                .setSocketTimeout(socketTimeout)
                .setConnectionRequestTimeout(CONNECTION_REQUEST_TIMEOUT_MS)
                .build();

        HttpGet request = new HttpGet(uri);
        request.setConfig(requestConfig);
        request.setHeader("Accept", "application/json");
        request.setHeader("Accept-Encoding", "identity");

        long delayMs = Math.max(1, TimeUnit.NANOSECONDS.toMillis(exchangeRemainingNanos));
        ScheduledFuture<?> abortTask = ABORT_WATCHDOG.schedule(request::abort, delayMs, TimeUnit.MILLISECONDS);

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode == 429) {
                throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_RATE_LIMITED);
            } else if (statusCode >= 500 && statusCode <= 599) {
                throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_UNAVAILABLE);
            } else if (statusCode >= 300 && statusCode < 400) {
                throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_INVALID_RESPONSE, "Redirects are rejected");
            } else if (statusCode != 200) {
                throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_INVALID_RESPONSE, "Unexpected status code: " + statusCode);
            }

            HttpEntity entity = response.getEntity();
            if (entity == null) {
                throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_INVALID_RESPONSE, "Empty HTTP entity");
            }

            validateHeaders(entity);

            byte[] bodyBytes = readEntityToEof(request, entity, maxResponseBytes, aggregateBytesCounter, exchangeDeadlineNanos);
            return parseSingleJsonRoot(bodyBytes);
        } catch (FeedRefreshException ex) {
            request.abort();
            throw ex;
        } catch (SocketTimeoutException ex) {
            request.abort();
            throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_UNAVAILABLE, "Socket timeout during provider exchange", ex);
        } catch (UnknownHostException ex) {
            request.abort();
            throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_UNAVAILABLE, "DNS resolution failure", ex);
        } catch (IOException ex) {
            request.abort();
            if (request.isAborted() || System.nanoTime() > exchangeDeadlineNanos) {
                throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_UNAVAILABLE, "Exchange deadline expired during request execution", ex);
            }
            throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_UNAVAILABLE, "I/O failure during provider exchange", ex);
        } finally {
            abortTask.cancel(false);
        }
    }

    private byte[] readEntityToEof(
            HttpGet request,
            HttpEntity entity,
            long maxResponseBytes,
            AtomicLong aggregateBytesCounter,
            long exchangeDeadlineNanos
    ) throws IOException {
        long contentLength = entity.getContentLength();
        if (contentLength > maxResponseBytes) {
            request.abort();
            throw new FeedRefreshException(FeedRefreshFailureReason.FETCH_LIMIT_EXCEEDED,
                    "Content-Length exceeds limit: " + contentLength + " > " + maxResponseBytes);
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream((int) Math.min(maxResponseBytes, 4096));
        byte[] buffer = new byte[4096];
        long responseBytes = 0;

        try (InputStream in = entity.getContent()) {
            int read;
            while ((read = in.read(buffer)) != -1) {
                if (System.nanoTime() > exchangeDeadlineNanos || request.isAborted()) {
                    request.abort();
                    throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_UNAVAILABLE,
                            "Exchange deadline exceeded while reading body");
                }
                responseBytes += read;
                if (responseBytes > maxResponseBytes) {
                    request.abort();
                    throw new FeedRefreshException(FeedRefreshFailureReason.FETCH_LIMIT_EXCEEDED,
                            "Response body exceeded limit: " + responseBytes + " > " + maxResponseBytes);
                }
                long currentAggregate = aggregateBytesCounter.addAndGet(read);
                if (currentAggregate > MAX_AGGREGATE_BYTES) {
                    request.abort();
                    throw new FeedRefreshException(FeedRefreshFailureReason.FETCH_LIMIT_EXCEEDED,
                            "Aggregate refresh body bytes exceeded limit: " + currentAggregate + " > " + MAX_AGGREGATE_BYTES);
                }
                baos.write(buffer, 0, read);
            }
            if (System.nanoTime() > exchangeDeadlineNanos || request.isAborted()) {
                request.abort();
                throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_UNAVAILABLE,
                        "Exchange deadline exceeded at EOF");
            }
        }
        return baos.toByteArray();
    }

    private JsonNode parseSingleJsonRoot(byte[] bodyBytes) {
        try (JsonParser parser = jsonFactory.createParser(bodyBytes)) {
            JsonNode root = objectMapper.readTree(parser);
            if (root == null || root.isMissingNode()) {
                throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_INVALID_RESPONSE, "Empty JSON response");
            }
            JsonToken trailingToken = parser.nextToken();
            if (trailingToken != null) {
                throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_INVALID_RESPONSE, "Unexpected trailing content after JSON root");
            }
            return root;
        } catch (StreamConstraintsException ex) {
            throw new FeedRefreshException(FeedRefreshFailureReason.FETCH_LIMIT_EXCEEDED, "JSON parser constraint exceeded", ex);
        } catch (JsonProcessingException ex) {
            throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_INVALID_RESPONSE, "Malformed JSON response", ex);
        } catch (IOException ex) {
            throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_INVALID_RESPONSE, "I/O error during JSON parsing", ex);
        }
    }

    private void validateHeaders(HttpEntity entity) {
        ContentType contentType;
        try {
            contentType = ContentType.getOrDefault(entity);
        } catch (IllegalArgumentException ex) {
            throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_INVALID_RESPONSE, "Malformed or unsupported upstream Content-Type", ex);
        }
        String mimeType = contentType.getMimeType();
        if (!"application/json".equalsIgnoreCase(mimeType)) {
            throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_INVALID_RESPONSE, "Expected application/json Content-Type");
        }
        Charset charset;
        try {
            charset = contentType.getCharset();
        } catch (IllegalArgumentException ex) {
            throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_INVALID_RESPONSE, "Malformed or unsupported upstream Content-Type charset", ex);
        }
        if (charset != null && !StandardCharsets.UTF_8.equals(charset)) {
            throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_INVALID_RESPONSE, "Unsupported charset: " + charset);
        }
        Header encodingHeader = entity.getContentEncoding();
        if (encodingHeader != null && encodingHeader.getValue() != null) {
            String val = encodingHeader.getValue().trim();
            if (!val.isEmpty() && !"identity".equalsIgnoreCase(val)) {
                throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_INVALID_RESPONSE, "Compressed content encoding rejected");
            }
        }
    }

    private static JsonFactory createConstrainedJsonFactory() {
        StreamReadConstraints constraints = StreamReadConstraints.builder()
                .maxNestingDepth(16)
                .maxStringLength(8192)
                .maxNumberLength(20)
                .build();
        return JsonFactory.builder()
                .streamReadConstraints(constraints)
                .enable(StreamReadFeature.STRICT_DUPLICATE_DETECTION)
                .build();
    }

    @Override
    public void close() throws IOException {
        try {
            if (dnsResolver != null) {
                dnsResolver.shutdown();
            }
        } finally {
            httpClient.close();
        }
    }

    public static class PolicyEnforcingDnsResolver implements DnsResolver {
        private final HackerNewsDestinationPolicy policy;
        private final DnsResolver delegate;
        private final ExecutorService dnsExecutor;

        public PolicyEnforcingDnsResolver(HackerNewsDestinationPolicy policy, DnsResolver delegate) {
            this(policy, delegate, new ThreadPoolExecutor(
                    2, 2, 0L, TimeUnit.MILLISECONDS,
                    new ArrayBlockingQueue<>(4),
                    r -> {
                        Thread t = new Thread(r, "hn-dns-worker");
                        t.setDaemon(true);
                        return t;
                    },
                    new ThreadPoolExecutor.AbortPolicy()
            ));
        }

        public PolicyEnforcingDnsResolver(HackerNewsDestinationPolicy policy, DnsResolver delegate, ExecutorService dnsExecutor) {
            this.policy = policy;
            this.delegate = delegate;
            this.dnsExecutor = dnsExecutor;
        }

        @Override
        public InetAddress[] resolve(String host) throws UnknownHostException {
            Future<InetAddress[]> future;
            try {
                future = dnsExecutor.submit(() -> delegate.resolve(host));
            } catch (RejectedExecutionException ex) {
                throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_UNAVAILABLE, "DNS resolver queue capacity exhausted", ex);
            }
            InetAddress[] addresses;
            try {
                addresses = future.get(DNS_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            } catch (TimeoutException ex) {
                future.cancel(true);
                throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_UNAVAILABLE, "DNS resolution timed out after 2s", ex);
            } catch (ExecutionException ex) {
                future.cancel(true);
                Throwable cause = ex.getCause();
                if (cause instanceof UnknownHostException uhe) {
                    throw uhe;
                }
                throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_UNAVAILABLE, "DNS resolution execution failure", cause);
            } catch (InterruptedException ex) {
                future.cancel(true);
                Thread.currentThread().interrupt();
                throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_UNAVAILABLE, "DNS resolution interrupted", ex);
            }

            policy.validateResolvedAddresses(host, addresses);
            return addresses;
        }

        public void shutdown() {
            dnsExecutor.shutdownNow();
        }
    }
}

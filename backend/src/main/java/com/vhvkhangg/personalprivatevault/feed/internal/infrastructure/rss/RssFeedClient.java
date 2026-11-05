package com.vhvkhangg.personalprivatevault.feed.internal.infrastructure.rss;

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
import java.util.Locale;
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

/**
 * Outbound HTTP client adapter for fetching single RSS/Atom feed documents
 * with strict byte, time, DNS, header, and redirect constraints.
 */
@Component
public class RssFeedClient implements AutoCloseable {

    public static final long MAX_RESPONSE_BYTES = 1048576L; // 1 MiB (1,048,576 bytes)
    public static final int INDIVIDUAL_EXCHANGE_TIMEOUT_MS = 10000; // 10s individual exchange
    private static final int CONNECT_TIMEOUT_MS = 2000; // 2s connect
    private static final int SOCKET_TIMEOUT_MS = 2000; // 2s socket inactivity
    private static final int CONNECTION_REQUEST_TIMEOUT_MS = 1000; // 1s connection lease
    private static final long DNS_TIMEOUT_SECONDS = 2L; // 2s DNS caller bound

    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "application/rss+xml",
            "application/atom+xml",
            "application/xml",
            "text/xml"
    );

    private static final ScheduledExecutorService ABORT_WATCHDOG = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "rss-abort-watchdog");
        t.setDaemon(true);
        return t;
    });

    private static final ThreadLocal<Long> CURRENT_REQUEST_DEADLINE_NANOS = new ThreadLocal<>();

    private final RssFeedDestinationPolicy destinationPolicy;
    private final CloseableHttpClient httpClient;
    private final PolicyEnforcingDnsResolver dnsResolver;

    @Autowired
    public RssFeedClient(RssFeedDestinationPolicy destinationPolicy) {
        this(destinationPolicy, null, null, null);
    }

    public RssFeedClient(
            RssFeedDestinationPolicy destinationPolicy,
            DnsResolver customDnsResolver,
            HttpClientConnectionManager customConnectionManager
    ) {
        this(destinationPolicy, customDnsResolver, customConnectionManager, null);
    }

    public RssFeedClient(
            RssFeedDestinationPolicy destinationPolicy,
            DnsResolver customDnsResolver,
            HttpClientConnectionManager customConnectionManager,
            ConnectionSocketFactory customSslSocketFactory
    ) {
        this.destinationPolicy = destinationPolicy;

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
                .disableCookieManagement()
                .disableAuthCaching()
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

    public byte[] fetchFeed(String feedUrl, long deadlineNanos) {
        if (feedUrl == null || feedUrl.isBlank()) {
            throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_DESTINATION_REJECTED, "feedUrl must not be blank");
        }
        URI uri;
        try {
            uri = URI.create(feedUrl);
        } catch (IllegalArgumentException ex) {
            throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_DESTINATION_REJECTED, "Invalid feedUrl URI syntax", ex);
        }

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
                .setAuthenticationEnabled(false)
                .build();

        HttpGet request = new HttpGet(uri);
        request.setConfig(requestConfig);
        request.setHeader("Accept", "application/rss+xml, application/atom+xml, application/xml, text/xml;q=0.9");
        request.setHeader("Accept-Encoding", "identity");

        long delayMs = Math.max(1, TimeUnit.NANOSECONDS.toMillis(exchangeRemainingNanos));
        ScheduledFuture<?> abortTask = ABORT_WATCHDOG.schedule(request::abort, delayMs, TimeUnit.MILLISECONDS);

        CURRENT_REQUEST_DEADLINE_NANOS.set(exchangeDeadlineNanos);
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

            return readEntityToEof(request, entity, exchangeDeadlineNanos);
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
            CURRENT_REQUEST_DEADLINE_NANOS.remove();
            abortTask.cancel(false);
        }
    }

    private byte[] readEntityToEof(HttpGet request, HttpEntity entity, long exchangeDeadlineNanos) throws IOException {
        long contentLength = entity.getContentLength();
        if (contentLength > MAX_RESPONSE_BYTES) {
            request.abort();
            throw new FeedRefreshException(FeedRefreshFailureReason.FETCH_LIMIT_EXCEEDED,
                    "Content-Length exceeds 1 MiB limit: " + contentLength + " > " + MAX_RESPONSE_BYTES);
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream((int) Math.min(MAX_RESPONSE_BYTES, 8192));
        byte[] buffer = new byte[4096];
        long responseBytes = 0;

        try (InputStream in = entity.getContent()) {
            int read;
            while ((read = in.read(buffer)) != -1) {
                if (Thread.currentThread().isInterrupted()) {
                    request.abort();
                    throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_UNAVAILABLE,
                            "Exchange interrupted while reading body");
                }
                if (System.nanoTime() > exchangeDeadlineNanos || request.isAborted()) {
                    request.abort();
                    throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_UNAVAILABLE,
                            "Exchange deadline exceeded while reading body");
                }
                responseBytes += read;
                if (responseBytes > MAX_RESPONSE_BYTES) {
                    request.abort();
                    throw new FeedRefreshException(FeedRefreshFailureReason.FETCH_LIMIT_EXCEEDED,
                            "Response body exceeded 1 MiB limit: " + responseBytes + " > " + MAX_RESPONSE_BYTES);
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

    private void validateHeaders(HttpEntity entity) {
        ContentType contentType;
        try {
            contentType = ContentType.getOrDefault(entity);
        } catch (IllegalArgumentException ex) {
            throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_INVALID_RESPONSE, "Malformed Content-Type", ex);
        }
        String mimeType = contentType.getMimeType();
        if (mimeType == null || !isAcceptedXmlMimeType(mimeType)) {
            throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_INVALID_RESPONSE, "Expected XML feed Content-Type, got: " + mimeType);
        }

        Charset charset;
        try {
            charset = contentType.getCharset();
        } catch (IllegalArgumentException ex) {
            throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_INVALID_RESPONSE, "Malformed Content-Type charset", ex);
        }
        if (charset != null && !isSupportedXmlCharset(charset)) {
            throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_INVALID_RESPONSE, "Unsupported charset: " + charset);
        }

        Header encodingHeader = entity.getContentEncoding();
        if (encodingHeader != null && encodingHeader.getValue() != null) {
            String val = encodingHeader.getValue().trim();
            if (!val.isEmpty() && !"identity".equalsIgnoreCase(val)) {
                throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_INVALID_RESPONSE, "Compressed content encoding rejected: " + val);
            }
        }
    }

    private static boolean isAcceptedXmlMimeType(String mimeType) {
        if (mimeType == null) {
            return false;
        }
        String lower = mimeType.trim().toLowerCase(Locale.ROOT);
        return ALLOWED_MIME_TYPES.contains(lower) || lower.endsWith("+xml");
    }

    private static boolean isSupportedXmlCharset(Charset charset) {
        return StandardCharsets.UTF_8.equals(charset)
                || StandardCharsets.US_ASCII.equals(charset)
                || StandardCharsets.ISO_8859_1.equals(charset)
                || StandardCharsets.UTF_16.equals(charset)
                || StandardCharsets.UTF_16BE.equals(charset)
                || StandardCharsets.UTF_16LE.equals(charset);
    }

    public PolicyEnforcingDnsResolver getDnsResolver() {
        return dnsResolver;
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
        private final RssFeedDestinationPolicy policy;
        private final DnsResolver delegate;
        private final ExecutorService dnsExecutor;

        public PolicyEnforcingDnsResolver(RssFeedDestinationPolicy policy, DnsResolver delegate) {
            this(policy, delegate, new ThreadPoolExecutor(
                    2, 2, 0L, TimeUnit.MILLISECONDS,
                    new ArrayBlockingQueue<>(4),
                    r -> {
                        Thread t = new Thread(r, "rss-dns-worker");
                        t.setDaemon(true);
                        return t;
                    },
                    new ThreadPoolExecutor.AbortPolicy()
            ));
        }

        public PolicyEnforcingDnsResolver(RssFeedDestinationPolicy policy, DnsResolver delegate, ExecutorService dnsExecutor) {
            this.policy = policy;
            this.delegate = delegate;
            this.dnsExecutor = dnsExecutor;
        }

        @Override
        public InetAddress[] resolve(String host) throws UnknownHostException {
            Long deadline = CURRENT_REQUEST_DEADLINE_NANOS.get();
            long remainingNanos = deadline != null ? (deadline - System.nanoTime()) : TimeUnit.SECONDS.toNanos(DNS_TIMEOUT_SECONDS);
            if (remainingNanos <= 0) {
                throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_UNAVAILABLE, "Request budget expired before DNS resolution");
            }
            long waitNanos = Math.min(TimeUnit.SECONDS.toNanos(DNS_TIMEOUT_SECONDS), remainingNanos);
            long waitMs = Math.max(1, TimeUnit.NANOSECONDS.toMillis(waitNanos));

            Future<InetAddress[]> future;
            try {
                future = dnsExecutor.submit(() -> delegate.resolve(host));
            } catch (RejectedExecutionException ex) {
                throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_UNAVAILABLE, "DNS resolver queue capacity exhausted", ex);
            }
            InetAddress[] addresses;
            try {
                addresses = future.get(waitMs, TimeUnit.MILLISECONDS);
            } catch (TimeoutException ex) {
                future.cancel(true);
                throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_UNAVAILABLE, "DNS resolution timed out after " + waitMs + "ms", ex);
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

        public boolean isShutdown() {
            return dnsExecutor.isShutdown();
        }
    }
}

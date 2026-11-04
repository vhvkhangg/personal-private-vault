package com.vhvkhangg.personalprivatevault.feed.internal.infrastructure.hackernews;

import com.vhvkhangg.personalprivatevault.feed.source.exception.FeedRefreshException;
import com.vhvkhangg.personalprivatevault.feed.source.exception.FeedRefreshFailureReason;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HackerNewsDestinationPolicyTest {

    private HackerNewsDestinationPolicy policy;

    @BeforeEach
    void setUp() {
        policy = new HackerNewsDestinationPolicy();
    }

    @Test
    @DisplayName("Valid Hacker News topstories and item URIs pass destination validation")
    void validUrisPass() {
        policy.validateUri(URI.create("https://hacker-news.firebaseio.com/v0/topstories.json"));
        policy.validateUri(URI.create("https://hacker-news.firebaseio.com/v0/item/1.json"));
        policy.validateUri(URI.create("https://hacker-news.firebaseio.com/v0/item/41982734.json"));
        policy.validateUri(URI.create("https://hacker-news.firebaseio.com:443/v0/topstories.json"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "http://hacker-news.firebaseio.com/v0/topstories.json",
            "https://evil.firebaseio.com/v0/topstories.json",
            "https://hacker-news.firebaseio.com:8080/v0/topstories.json",
            "https://user:pass@hacker-news.firebaseio.com/v0/topstories.json",
            "https://hacker-news.firebaseio.com/v0/topstories.json?foo=bar",
            "https://hacker-news.firebaseio.com/v0/topstories.json#fragment",
            "https://hacker-news.firebaseio.com/v0/newstories.json",
            "https://hacker-news.firebaseio.com/v0/item/0.json",
            "https://hacker-news.firebaseio.com/v0/item/-10.json",
            "https://hacker-news.firebaseio.com/v0/item/abc.json",
            "https://hacker-news.firebaseio.com/v0/item/123",
            "https://hacker-news.firebaseio.com/v1/topstories.json"
    })
    @DisplayName("Invalid destinations are rejected by destination policy")
    void invalidUrisFail(String uriStr) {
        assertThatThrownBy(() -> policy.validateUri(URI.create(uriStr)))
                .isInstanceOf(FeedRefreshException.class)
                .satisfies(ex -> assertThat(((FeedRefreshException) ex).getReason())
                        .isEqualTo(FeedRefreshFailureReason.UPSTREAM_DESTINATION_REJECTED));
    }

    @Test
    @DisplayName("Safe public IP addresses pass address validation")
    void safeAddressesPass() throws Exception {
        InetAddress[] safe = new InetAddress[]{
                InetAddress.getByName("151.101.1.1"),
                InetAddress.getByName("8.8.8.8"),
                InetAddress.getByName("2600::")
        };
        policy.validateResolvedAddresses(HackerNewsDestinationPolicy.EXPECTED_HOST, safe);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "127.0.0.1",
            "127.0.0.2",
            "0.0.0.0",
            "10.0.0.1",
            "10.255.255.255",
            "172.16.0.1",
            "172.31.255.254",
            "192.168.0.1",
            "192.168.1.100",
            "100.64.0.1",
            "100.127.255.254",
            "169.254.1.1",
            "169.254.169.254",
            "192.0.2.1",
            "198.51.100.1",
            "203.0.113.1",
            "198.18.0.1",
            "224.0.0.1",
            "239.255.255.250",
            "240.0.0.1",
            "255.255.255.255",
            "::1",
            "::",
            "fc00::1",
            "fd12:3456:789a::1",
            "fe80::1",
            "2001:db8::1",
            "::ffff:127.0.0.1",
            "::ffff:10.0.0.1",
            "::127.0.0.1",
            "::10.0.0.1",
            "2002:7f00:1::",
            "2002:a00:1::",
            "fe80::0:5efe:127.0.0.1",
            "fe80::200:5efe:192.168.1.1",
            "100::1",
            "2001:2::1",
            "192.0.0.1",
            "64:ff9b::127.0.0.1",
            "64:ff9b::192.168.1.1",
            "ff02::1"
    })
    @DisplayName("Unsafe private, loopback, link-local, multicast, metadata, or reserved addresses are rejected")
    void unsafeAddressesAreRejected(String ipStr) throws Exception {
        InetAddress unsafe = InetAddress.getByName(ipStr);
        assertThat(policy.isUnsafeAddress(unsafe)).as("Address %s must be recognized as unsafe", ipStr).isTrue();

        assertThatThrownBy(() -> policy.validateResolvedAddresses(HackerNewsDestinationPolicy.EXPECTED_HOST, new InetAddress[]{unsafe}))
                .isInstanceOf(FeedRefreshException.class)
                .satisfies(ex -> assertThat(((FeedRefreshException) ex).getReason())
                        .isEqualTo(FeedRefreshFailureReason.UPSTREAM_DESTINATION_REJECTED));
    }

    @Test
    @DisplayName("Mixed public and unsafe resolution answers fail closed")
    void mixedPublicAndUnsafeFailsClosed() throws Exception {
        InetAddress[] mixed = new InetAddress[]{
                InetAddress.getByName("151.101.1.1"),
                InetAddress.getByName("127.0.0.1")
        };
        assertThatThrownBy(() -> policy.validateResolvedAddresses(HackerNewsDestinationPolicy.EXPECTED_HOST, mixed))
                .isInstanceOf(FeedRefreshException.class)
                .satisfies(ex -> assertThat(((FeedRefreshException) ex).getReason())
                        .isEqualTo(FeedRefreshFailureReason.UPSTREAM_DESTINATION_REJECTED));
    }

    @Test
    @DisplayName("Exceeding maximum resolved address count fails")
    void exceedingMaxResolvedAddressesFails() throws Exception {
        InetAddress[] tooMany = new InetAddress[17];
        for (int i = 0; i < 17; i++) {
            tooMany[i] = InetAddress.getByName("151.101.1." + (i + 1));
        }
        assertThatThrownBy(() -> policy.validateResolvedAddresses(HackerNewsDestinationPolicy.EXPECTED_HOST, tooMany))
                .isInstanceOf(FeedRefreshException.class)
                .satisfies(ex -> assertThat(((FeedRefreshException) ex).getReason())
                        .isEqualTo(FeedRefreshFailureReason.UPSTREAM_DESTINATION_REJECTED));
    }

    @Test
    @DisplayName("Proxy safety check throws UPSTREAM_UNAVAILABLE when proxy system property is configured")
    void proxySafetyCheckDetectsConfiguredProxy() {
        String original = System.getProperty("http.proxyHost");
        try {
            System.setProperty("http.proxyHost", "proxy.example.com");
            assertThatThrownBy(() -> policy.checkProxySafety())
                    .isInstanceOf(FeedRefreshException.class)
                    .satisfies(ex -> assertThat(((FeedRefreshException) ex).getReason())
                            .isEqualTo(FeedRefreshFailureReason.UPSTREAM_UNAVAILABLE));
        } finally {
            if (original != null) {
                System.setProperty("http.proxyHost", original);
            } else {
                System.clearProperty("http.proxyHost");
            }
        }
    }
}

package com.vhvkhangg.personalprivatevault.feed.internal.infrastructure.rss;

import com.vhvkhangg.personalprivatevault.feed.source.exception.FeedRefreshException;
import com.vhvkhangg.personalprivatevault.feed.source.exception.FeedRefreshFailureReason;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.net.InetAddress;
import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RssFeedDestinationPolicyTest {

    private RssFeedDestinationPolicy policy;

    @BeforeEach
    void setUp() {
        policy = new RssFeedDestinationPolicy();
    }

    @Test
    @DisplayName("Valid public HTTPS RSS feed URLs pass destination validation")
    void validUrisPass() {
        policy.validateUri(URI.create("https://example.com/rss.xml"));
        policy.validateUri(URI.create("https://news.ycombinator.com:443/feed"));
        policy.validateUri(URI.create("https://feeds.feedburner.com/test?format=xml"));
        policy.validateUri(URI.create("https://blog.example.org/atom.xml?category=tech&limit=20"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "http://example.com/feed.xml",
            "ftp://example.com/feed.xml",
            "file:///etc/passwd",
            "gopher://example.com/feed",
            "https://example.com:8080/feed.xml",
            "https://example.com:80/feed.xml",
            "https://user:pass@example.com/feed.xml",
            "https://example.com/feed.xml#section",
            "https://127.0.0.1/feed.xml",
            "https://[::1]/feed.xml",
            "https://10.0.0.1/feed.xml",
            "https://192.168.1.1/feed.xml",
            "https://169.254.169.254/feed.xml",
            "https://2130706433/feed.xml",
            "https://[fe80::1]/feed.xml",
            "/relative/path/feed.xml"
    })
    @DisplayName("Invalid, non-HTTPS, IP-literal, fragment, or credentialed URIs are rejected")
    void invalidUrisFail(String uriStr) {
        assertThatThrownBy(() -> policy.validateUri(URI.create(uriStr)))
                .isInstanceOf(FeedRefreshException.class)
                .satisfies(ex -> assertThat(((FeedRefreshException) ex).getReason())
                        .isEqualTo(FeedRefreshFailureReason.UPSTREAM_DESTINATION_REJECTED));
    }

    @Test
    @DisplayName("Safe public IP addresses pass validation")
    void safeAddressesPass() throws Exception {
        InetAddress[] safe = new InetAddress[]{
                InetAddress.getByName("93.184.216.34"),
                InetAddress.getByName("8.8.8.8"),
                InetAddress.getByName("2606:2800:220:1:248:1893:25c8:1946")
        };
        policy.validateResolvedAddresses("example.com", safe);
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
            "::ffff:192.168.0.1",
            "::ffff:169.254.169.254",
            "64:ff9b::127.0.0.1",
            "64:ff9b::10.0.0.1",
            "64:ff9b:1::a00:1",
            "3fff::1",
            "4000::1",
            "2001:2::1",
            "2001:4::1",
            "2001:10::1",
            "2001:20::1",
            "3000::1",
            "2000::1",
            "2001:0000::1",
            "2001:3::1",
            "2d00::1",
            "2e00::1",
            "2001:1000::1",
            "2003:4000::",
            "2003:4000::1",
            "2003:ffff::1",
            "2003:ffff:ffff:ffff:ffff:ffff:ffff:ffff"
    })
    @DisplayName("Private, loopback, link-local, multicast, metadata, and reserved IPs are rejected")
    void unsafeAddressesFail(String ipStr) throws Exception {
        InetAddress address = InetAddress.getByName(ipStr);
        assertThat(policy.isUnsafeAddress(address))
                .as("Address %s must be recognized as unsafe", ipStr)
                .isTrue();
        InetAddress[] unsafe = new InetAddress[]{address};
        assertThatThrownBy(() -> policy.validateResolvedAddresses("example.com", unsafe))
                .isInstanceOf(FeedRefreshException.class)
                .satisfies(ex -> assertThat(((FeedRefreshException) ex).getReason())
                        .isEqualTo(FeedRefreshFailureReason.UPSTREAM_DESTINATION_REJECTED));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "93.184.216.34",
            "2606:4700:4700::1111",
            "2001:4860:4860::8888",
            "2620:fe::fe",
            "2a04:4e42::1",
            "2001:4:112::1",
            "2003::",
            "2003::1",
            "2003:3fff::1",
            "2003:3fff:ffff:ffff:ffff:ffff:ffff:ffff",
            "2400::1",
            "2800::1",
            "2c00::1"
    })
    @DisplayName("Allocated public IPv4 and IPv6 addresses pass destination policy")
    void publicAddressesPass(String ipStr) throws Exception {
        InetAddress address = InetAddress.getByName(ipStr);
        assertThat(policy.isUnsafeAddress(address))
                .as("Address %s must be recognized as safe", ipStr)
                .isFalse();
        InetAddress[] safe = new InetAddress[]{address};
        policy.validateResolvedAddresses("example.com", safe);
    }

    @Test
    @DisplayName("Mixed DNS results containing both safe and unsafe addresses reject the entire answer")
    void mixedDnsResultsFail() throws Exception {
        InetAddress[] mixed = new InetAddress[]{
                InetAddress.getByName("93.184.216.34"),
                InetAddress.getByName("127.0.0.1")
        };
        assertThatThrownBy(() -> policy.validateResolvedAddresses("example.com", mixed))
                .isInstanceOf(FeedRefreshException.class)
                .satisfies(ex -> assertThat(((FeedRefreshException) ex).getReason())
                        .isEqualTo(FeedRefreshFailureReason.UPSTREAM_DESTINATION_REJECTED));
    }

    @Test
    @DisplayName("Empty or null address array fails with UPSTREAM_UNAVAILABLE")
    void emptyAddressesFail() {
        assertThatThrownBy(() -> policy.validateResolvedAddresses("example.com", new InetAddress[0]))
                .isInstanceOf(FeedRefreshException.class)
                .satisfies(ex -> assertThat(((FeedRefreshException) ex).getReason())
                        .isEqualTo(FeedRefreshFailureReason.UPSTREAM_UNAVAILABLE));
    }

    @Test
    @DisplayName("Excessive resolved address count (> 16) fails with UPSTREAM_DESTINATION_REJECTED")
    void excessiveAddressesFail() throws Exception {
        InetAddress[] tooMany = new InetAddress[17];
        for (int i = 0; i < 17; i++) {
            tooMany[i] = InetAddress.getByName("93.184.216." + (i + 1));
        }
        assertThatThrownBy(() -> policy.validateResolvedAddresses("example.com", tooMany))
                .isInstanceOf(FeedRefreshException.class)
                .satisfies(ex -> assertThat(((FeedRefreshException) ex).getReason())
                        .isEqualTo(FeedRefreshFailureReason.UPSTREAM_DESTINATION_REJECTED));
    }

    @Test
    @DisplayName("Configured JVM proxy properties fail proxy safety check")
    void proxySystemPropertiesFail() {
        System.setProperty("http.proxyHost", "proxy.local");
        try {
            assertThatThrownBy(() -> policy.checkProxySafety())
                    .isInstanceOf(FeedRefreshException.class)
                    .satisfies(ex -> assertThat(((FeedRefreshException) ex).getReason())
                            .isEqualTo(FeedRefreshFailureReason.UPSTREAM_UNAVAILABLE));
        } finally {
            System.clearProperty("http.proxyHost");
        }
    }
}

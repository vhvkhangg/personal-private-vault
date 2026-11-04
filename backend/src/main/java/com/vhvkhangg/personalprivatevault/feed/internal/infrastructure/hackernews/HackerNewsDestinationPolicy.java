package com.vhvkhangg.personalprivatevault.feed.internal.infrastructure.hackernews;

import com.vhvkhangg.personalprivatevault.feed.source.exception.FeedRefreshException;
import com.vhvkhangg.personalprivatevault.feed.source.exception.FeedRefreshFailureReason;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Enforces outbound transport safety, fixed destination authority, IP address constraints,
 * and proxy safety for Hacker News API communications.
 */
@Component
public class HackerNewsDestinationPolicy {

    public static final String EXPECTED_HOST = "hacker-news.firebaseio.com";
    public static final int EXPECTED_PORT = 443;
    public static final String EXPECTED_SCHEME = "https";
    public static final int MAX_RESOLVED_ADDRESSES = 16;

    private static final Pattern ITEM_PATH_PATTERN = Pattern.compile("^/v0/item/([1-9][0-9]{0,18})\\.json$");
    private static final String TOPSTORIES_PATH = "/v0/topstories.json";

    public void validateUri(URI uri) {
        if (uri == null) {
            throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_DESTINATION_REJECTED);
        }
        if (!EXPECTED_SCHEME.equalsIgnoreCase(uri.getScheme())) {
            throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_DESTINATION_REJECTED);
        }
        if (!EXPECTED_HOST.equalsIgnoreCase(uri.getHost())) {
            throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_DESTINATION_REJECTED);
        }
        int port = uri.getPort();
        if (port != -1 && port != EXPECTED_PORT) {
            throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_DESTINATION_REJECTED);
        }
        if (uri.getUserInfo() != null || uri.getQuery() != null || uri.getFragment() != null) {
            throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_DESTINATION_REJECTED);
        }
        String path = uri.getPath();
        if (path == null) {
            throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_DESTINATION_REJECTED);
        }
        if (!TOPSTORIES_PATH.equals(path)) {
            var matcher = ITEM_PATH_PATTERN.matcher(path);
            if (!matcher.matches()) {
                throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_DESTINATION_REJECTED);
            }
            try {
                long id = Long.parseLong(matcher.group(1));
                if (id <= 0) {
                    throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_DESTINATION_REJECTED);
                }
            } catch (NumberFormatException ex) {
                throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_DESTINATION_REJECTED);
            }
        }
    }

    public void validateResolvedAddresses(String host, InetAddress[] addresses) {
        if (!EXPECTED_HOST.equalsIgnoreCase(host)) {
            throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_DESTINATION_REJECTED);
        }
        if (addresses == null || addresses.length == 0) {
            throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_UNAVAILABLE);
        }
        if (addresses.length > MAX_RESOLVED_ADDRESSES) {
            throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_DESTINATION_REJECTED);
        }
        for (InetAddress addr : addresses) {
            if (isUnsafeAddress(addr)) {
                throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_DESTINATION_REJECTED);
            }
        }
    }

    public boolean isUnsafeAddress(InetAddress addr) {
        if (addr == null) {
            return true;
        }
        if (addr.isLoopbackAddress() || addr.isAnyLocalAddress() || addr.isMulticastAddress()
                || addr.isLinkLocalAddress() || addr.isSiteLocalAddress()) {
            return true;
        }
        byte[] bytes = addr.getAddress();
        if (bytes.length == 4) {
            return isUnsafeIpv4(bytes, 0);
        } else if (bytes.length == 16) {
            return isUnsafeIpv6(bytes);
        }
        return true;
    }

    private boolean isUnsafeIpv4(byte[] b, int offset) {
        int b0 = b[offset] & 0xFF;
        int b1 = b[offset + 1] & 0xFF;
        int b2 = b[offset + 2] & 0xFF;
        int b3 = b[offset + 3] & 0xFF;

        // 0.0.0.0/8 (Current network)
        if (b0 == 0) return true;
        // 10.0.0.0/8 (RFC 1918 Private)
        if (b0 == 10) return true;
        // 100.64.0.0/10 (CGNAT)
        if (b0 == 100 && (b1 & 0xC0) == 64) return true;
        // 127.0.0.0/8 (Loopback)
        if (b0 == 127) return true;
        // 169.254.0.0/16 (Link-local and 169.254.169.254 cloud metadata)
        if (b0 == 169 && b1 == 254) return true;
        // 172.16.0.0/12 (RFC 1918 Private)
        if (b0 == 172 && (b1 & 0xF0) == 16) return true;
        // 192.0.0.0/24 (IETF Protocol Assignments)
        if (b0 == 192 && b1 == 0 && b2 == 0) return true;
        // 192.0.2.0/24 (TEST-NET-1)
        if (b0 == 192 && b1 == 0 && b2 == 2) return true;
        // 192.168.0.0/16 (RFC 1918 Private)
        if (b0 == 192 && b1 == 168) return true;
        // 198.18.0.0/15 (Benchmarking)
        if (b0 == 198 && (b1 & 0xFE) == 18) return true;
        // 198.51.100.0/24 (TEST-NET-2)
        if (b0 == 198 && b1 == 51 && b2 == 100) return true;
        // 203.0.113.0/24 (TEST-NET-3)
        if (b0 == 203 && b1 == 0 && b2 == 113) return true;
        // 224.0.0.0/4 (Multicast 224-239)
        if (b0 >= 224 && b0 <= 239) return true;
        // 240.0.0.0/4 (Reserved 240-255)
        if (b0 >= 240) return true;
        // 255.255.255.255 (Broadcast)
        if (b0 == 255 && b1 == 255 && b2 == 255 && b3 == 255) return true;

        return false;
    }

    private boolean isUnsafeIpv6(byte[] b) {
        // ::/128 (Unspecified)
        boolean allZero = true;
        for (byte x : b) {
            if (x != 0) {
                allZero = false;
                break;
            }
        }
        if (allZero) return true;

        // ::1/128 (Loopback)
        boolean isLoopback = true;
        for (int i = 0; i < 15; i++) {
            if (b[i] != 0) {
                isLoopback = false;
                break;
            }
        }
        if (isLoopback && b[15] == 1) return true;

        int b0 = b[0] & 0xFF;
        int b1 = b[1] & 0xFF;

        // 100::/64 (Discard prefix RFC 6666)
        if (b0 == 0x01 && b1 == 0x00) return true;

        // 2001:db8::/32 (Documentation)
        if (b0 == 0x20 && b1 == 0x01 && (b[2] & 0xFF) == 0x0D && (b[3] & 0xFF) == 0xB8) return true;

        // 2001:2::/48 (Benchmarking RFC 5180)
        if (b0 == 0x20 && b1 == 0x01 && (b[2] & 0xFF) == 0x00 && (b[3] & 0xFF) == 0x02) return true;

        // 2001:0000::/32 (Teredo tunneling) - embedded client IPv4 in last 4 bytes XOR 0xFF
        if (b0 == 0x20 && b1 == 0x01 && b[2] == 0 && b[3] == 0) {
            byte[] teredoIpv4 = new byte[]{
                    (byte) (b[12] ^ 0xFF),
                    (byte) (b[13] ^ 0xFF),
                    (byte) (b[14] ^ 0xFF),
                    (byte) (b[15] ^ 0xFF)
            };
            if (isUnsafeIpv4(teredoIpv4, 0)) return true;
        }

        // 2002::/16 (6to4 tunneling) - embedded IPv4 in bytes 2..5
        if (b0 == 0x20 && b1 == 0x02) {
            if (isUnsafeIpv4(b, 2)) return true;
        }

        // fc00::/7 (Unique Local Address - ULA)
        if ((b0 & 0xFE) == 0xFC) return true;

        // fe80::/10 (Link-local)
        if (b0 == 0xFE && (b1 & 0xC0) == 0x80) return true;

        // ff00::/8 (Multicast)
        if (b0 == 0xFF) return true;

        // ::ffff:0:0/96 (IPv4-mapped IPv6)
        boolean isFirstTenZero = true;
        for (int i = 0; i < 10; i++) {
            if (b[i] != 0) {
                isFirstTenZero = false;
                break;
            }
        }
        if (isFirstTenZero && (b[10] & 0xFF) == 0xFF && (b[11] & 0xFF) == 0xFF) {
            return isUnsafeIpv4(b, 12);
        }

        // ::0:0/96 (IPv4-compatible IPv6 - deprecated RFC 4291)
        if (isFirstTenZero && b[10] == 0 && b[11] == 0) {
            return isUnsafeIpv4(b, 12);
        }

        // 64:ff9b::/96 (Well-known prefix NAT64)
        if (b0 == 0 && b1 == 0x64 && (b[2] & 0xFF) == 0xFF && (b[3] & 0xFF) == 0x9B) {
            boolean middleZero = true;
            for (int i = 4; i < 12; i++) {
                if (b[i] != 0) {
                    middleZero = false;
                    break;
                }
            }
            if (middleZero) {
                return isUnsafeIpv4(b, 12);
            }
        }

        // ISATAP (...:0:5efe:x.x.x.x or ...:200:5efe:x.x.x.x)
        if ((b[8] == 0 || (b[8] & 0xFF) == 0x02) && b[9] == 0 && (b[10] & 0xFF) == 0x5E && (b[11] & 0xFF) == 0xFE) {
            if (isUnsafeIpv4(b, 12)) return true;
        }

        return false;
    }

    private static final List<String> PROXY_ENV_VARS = List.of(
            "HTTP_PROXY", "HTTPS_PROXY", "ALL_PROXY",
            "http_proxy", "https_proxy", "all_proxy"
    );

    public void checkProxySafety() {
        if (System.getProperty("http.proxyHost") != null
                || System.getProperty("https.proxyHost") != null
                || System.getProperty("socksProxyHost") != null
                || System.getProperty("proxyHost") != null) {
            throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_UNAVAILABLE, "Configured proxy is unsupported in S1 direct-only mode");
        }
        for (String envVar : PROXY_ENV_VARS) {
            String val = System.getenv(envVar);
            if (val != null && !val.trim().isEmpty()) {
                throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_UNAVAILABLE,
                        "Environment proxy (" + envVar + ") is unsupported in S1 direct-only mode");
            }
        }
        try {
            ProxySelector selector = ProxySelector.getDefault();
            if (selector != null) {
                List<Proxy> proxies = selector.select(URI.create("https://" + EXPECTED_HOST + "/"));
                if (proxies != null) {
                    for (Proxy proxy : proxies) {
                        if (proxy != null && proxy.type() != Proxy.Type.DIRECT) {
                            throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_UNAVAILABLE, "Environment proxy is unsupported in S1 direct-only mode");
                        }
                    }
                }
            }
        } catch (IllegalArgumentException ex) {
            // Malformed URI check
        }
    }
}

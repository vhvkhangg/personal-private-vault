package com.vhvkhangg.personalprivatevault.feed.internal.infrastructure.rss;

import com.vhvkhangg.personalprivatevault.feed.source.exception.FeedRefreshException;
import com.vhvkhangg.personalprivatevault.feed.source.exception.FeedRefreshFailureReason;
import org.apache.http.conn.util.InetAddressUtils;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.util.List;

/**
 * Enforces destination URI safety, public IP address resolution bounds, and proxy isolation
 * for outbound RSS/Atom feed fetching.
 */
@Component
public class RssFeedDestinationPolicy {

    public static final String EXPECTED_SCHEME = "https";
    public static final int EXPECTED_PORT = 443;
    public static final int MAX_RESOLVED_ADDRESSES = 16;

    private static final List<String> PROXY_ENV_VARS = List.of(
            "HTTP_PROXY", "HTTPS_PROXY", "ALL_PROXY",
            "http_proxy", "https_proxy", "all_proxy"
    );

    public void validateUri(URI uri) {
        if (uri == null || !uri.isAbsolute()) {
            throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_DESTINATION_REJECTED, "URI must be absolute");
        }
        if (!EXPECTED_SCHEME.equalsIgnoreCase(uri.getScheme())) {
            throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_DESTINATION_REJECTED, "Only https scheme is permitted");
        }
        String host = uri.getHost();
        if (host == null || host.isBlank()) {
            throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_DESTINATION_REJECTED, "Host must be specified");
        }
        if (isIpLiteral(host)) {
            throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_DESTINATION_REJECTED, "IP literals are forbidden in feedUrl");
        }
        int port = uri.getPort();
        if (port != -1 && port != EXPECTED_PORT) {
            throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_DESTINATION_REJECTED, "Only port 443 (or omitted) is permitted");
        }
        if (uri.getUserInfo() != null || uri.getRawUserInfo() != null) {
            throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_DESTINATION_REJECTED, "User info is forbidden in feedUrl");
        }
        if (uri.getFragment() != null || uri.getRawFragment() != null) {
            throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_DESTINATION_REJECTED, "Fragments are forbidden in feedUrl");
        }
        String uriStr = uri.toString();
        for (int i = 0; i < uriStr.length(); i++) {
            if (Character.isISOControl(uriStr.charAt(i))) {
                throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_DESTINATION_REJECTED, "Control characters forbidden in feedUrl");
            }
        }
    }

    public static boolean isIpLiteral(String host) {
        if (host == null || host.isBlank()) {
            return false;
        }
        String trimmed = host.trim();
        if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
            return true;
        }
        if (trimmed.contains(":")) {
            return true;
        }
        if (trimmed.chars().allMatch(c -> Character.isDigit(c) || c == '.')) {
            return true;
        }
        return InetAddressUtils.isIPv4Address(trimmed)
                || InetAddressUtils.isIPv6Address(trimmed)
                || InetAddressUtils.isIPv6HexCompressedAddress(trimmed);
    }

    public void validateResolvedAddresses(String host, InetAddress[] addresses) {
        if (host == null || host.isBlank()) {
            throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_DESTINATION_REJECTED, "Host must not be blank");
        }
        if (addresses == null || addresses.length == 0) {
            throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_UNAVAILABLE, "DNS returned no addresses");
        }
        if (addresses.length > MAX_RESOLVED_ADDRESSES) {
            throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_DESTINATION_REJECTED, "DNS returned more than " + MAX_RESOLVED_ADDRESSES + " addresses");
        }
        for (InetAddress addr : addresses) {
            if (isUnsafeAddress(addr)) {
                throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_DESTINATION_REJECTED, "Resolved destination rejected by network safety policy");
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

        // 64:ff9b::/96 (Well-known prefix NAT64 RFC 6052)
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
            // 64:ff9b:1::/48 (Local-use translation prefix RFC 8215) without middle zeros is non-public
            return true;
        }

        // ISATAP (...:0:5efe:x.x.x.x or ...:200:5efe:x.x.x.x)
        if ((b[8] == 0 || (b[8] & 0xFF) == 0x02) && b[9] == 0 && (b[10] & 0xFF) == 0x5E && (b[11] & 0xFF) == 0xFE) {
            if (isUnsafeIpv4(b, 12)) return true;
        }

        // Pinned IANA-allocated public IPv6 unicast prefix policy:
        // All publicly routable global unicast addresses must belong to an explicitly allocated
        // IANA unicast block. Any unallocated / reserved blocks (such as 2000::/16 RFC 4048,
        // 2001:1000::/23 gap, 2d00::/8, 2e00::/7, 3000::/4, 4000::/3, fe80::/10 link-local,
        // fc00::/7 ULA, ff00::/8 multicast, 100::/64 discard, ::/128, etc.) are rejected as unsafe.

        // 2001::/16 (Sub-allocations to RIRs and IETF)
        if (b0 == 0x20 && b1 == 0x01) {
            return !isAllocated2001Suballocation(b);
        }

        // 2002::/16 (6to4 tunneling RFC 3056) - embedded IPv4 in bytes 2..5
        if (b0 == 0x20 && b1 == 0x02) {
            return isUnsafeIpv4(b, 2);
        }

        // 2003::/18 (RIPE NCC)
        if (b0 == 0x20 && b1 == 0x03 && (b[2] & 0xC0) == 0) {
            return false;
        }

        // 2400::/12 (APNIC: 2400::/12 covers 2400 to 240F)
        if (b0 == 0x24 && (b1 & 0xF0) == 0x00) {
            return false;
        }

        // 2600::/12 (ARIN: 2600::/12 covers 2600 to 260F, e.g. Cloudflare 2606:4700::1111)
        if (b0 == 0x26 && (b1 & 0xF0) == 0x00) {
            return false;
        }

        // 2610::/23 (ARIN: 2610:0000 to 2610:01FF)
        if (b0 == 0x26 && b1 == 0x10 && (b[2] & 0xFE) == 0x00) {
            return false;
        }

        // 2620::/23 (ARIN: 2620:0000 to 2620:01FF, e.g. Quad9 2620:fe::fe)
        if (b0 == 0x26 && b1 == 0x20 && (b[2] & 0xFE) == 0x00) {
            return false;
        }

        // 2800::/12 (LACNIC: 2800::/12 covers 2800 to 280F)
        if (b0 == 0x28 && (b1 & 0xF0) == 0x00) {
            return false;
        }

        // 2a00::/12 (RIPE NCC: 2A00::/12 covers 2A00 to 2A0F, e.g. Fastly 2a04:4e42::1)
        if (b0 == 0x2A && (b1 & 0xF0) == 0x00) {
            return false;
        }

        // 2c00::/12 (AFRINIC: 2C00::/12 covers 2C00 to 2C0F)
        if (b0 == 0x2C && (b1 & 0xF0) == 0x00) {
            return false;
        }

        // All other IPv6 address ranges are unallocated or reserved by IANA -> UNSAFE
        return true;
    }

    private boolean isAllocated2001Suballocation(byte[] b) {
        int val = ((b[2] & 0xFF) << 8) | (b[3] & 0xFF);
        // 2001:0000::/23 (IETF Protocol Assignments) - only AS112 2001:4:112::/48 is global
        if ((val & 0xFE00) == 0x0000) {
            // Teredo 2001:0000::/32 embedded client IPv4 in last 4 bytes XOR 0xFF
            if (val == 0x0000) {
                byte[] teredoIpv4 = new byte[]{
                        (byte) (b[12] ^ 0xFF),
                        (byte) (b[13] ^ 0xFF),
                        (byte) (b[14] ^ 0xFF),
                        (byte) (b[15] ^ 0xFF)
                };
                if (isUnsafeIpv4(teredoIpv4, 0)) return false;
            }
            return (b[2] == 0x00 && b[3] == 0x04 && b[4] == 0x01 && b[5] == 0x12);
        }
        // 2001:0200::/23 (APNIC)
        if ((val & 0xFE00) == 0x0200) return true;
        // 2001:0400::/23 (ARIN)
        if ((val & 0xFE00) == 0x0400) return true;
        // 2001:0600::/23 (RIPE NCC)
        if ((val & 0xFE00) == 0x0600) return true;
        // 2001:0800::/23 (RIPE NCC)
        if ((val & 0xFE00) == 0x0800) return true;
        // 2001:0a00::/23 (RIPE NCC)
        if ((val & 0xFE00) == 0x0A00) return true;
        // 2001:0c00::/23 (APNIC, excluding 2001:db8::/32 documentation)
        if ((val & 0xFE00) == 0x0C00) {
            return val != 0x0DB8; // 2001:db8::/32
        }
        // 2001:0e00::/23 (APNIC)
        if ((val & 0xFE00) == 0x0E00) return true;
        // [gap: 2001:1000::/23 is reserved]
        // 2001:1200::/23 (LACNIC)
        if ((val & 0xFE00) == 0x1200) return true;
        // 2001:1400::/23 (RIPE NCC)
        if ((val & 0xFE00) == 0x1400) return true;
        // 2001:1600::/23 (RIPE NCC)
        if ((val & 0xFE00) == 0x1600) return true;
        // 2001:1800::/23 (ARIN)
        if ((val & 0xFE00) == 0x1800) return true;
        // 2001:1a00::/23 (RIPE NCC)
        if ((val & 0xFE00) == 0x1A00) return true;
        // 2001:1c00::/23 (RIPE NCC)
        if ((val & 0xFE00) == 0x1C00) return true;
        // 2001:1e00::/23 (ARIN)
        if ((val & 0xFE00) == 0x1E00) return true;
        // 2001:2000::/20 (RIPE NCC: 2000 to 2FFF)
        if ((val & 0xF000) == 0x2000) return true;
        // 2001:3000::/21 (RIPE NCC: 3000 to 37FF)
        if ((val & 0xF800) == 0x3000) return true;
        // 2001:3800::/22 (RIPE NCC: 3800 to 3BFF)
        if ((val & 0xFC00) == 0x3800) return true;
        // 2001:3c00::/22 (RIPE NCC: 3C00 to 3FFF)
        if ((val & 0xFC00) == 0x3C00) return true;
        // 2001:4000::/23 (RIPE NCC)
        if ((val & 0xFE00) == 0x4000) return true;
        // 2001:4200::/23 (AFRINIC)
        if ((val & 0xFE00) == 0x4200) return true;
        // 2001:4400::/23 (APNIC)
        if ((val & 0xFE00) == 0x4400) return true;
        // 2001:4600::/23 (RIPE NCC)
        if ((val & 0xFE00) == 0x4600) return true;
        // 2001:4800::/23 (ARIN, e.g. 2001:4860:: Google)
        if ((val & 0xFE00) == 0x4800) return true;
        // 2001:4a00::/23 (RIPE NCC)
        if ((val & 0xFE00) == 0x4A00) return true;
        // 2001:4c00::/23 (RIPE NCC)
        if ((val & 0xFE00) == 0x4C00) return true;
        // 2001:5000::/20 (RIPE NCC: 5000 to 5FFF)
        if ((val & 0xF000) == 0x5000) return true;
        // 2001:8000::/19 (APNIC: 8000 to 9FFF)
        if ((val & 0xE000) == 0x8000) return true;
        // 2001:a000::/20 (RIPE NCC: A000 to AFFF)
        if ((val & 0xF000) == 0xA000) return true;
        // 2001:b000::/20 (RIPE NCC: B000 to BFFF)
        if ((val & 0xF000) == 0xB000) return true;
        return false;
    }

    public void checkProxySafety() {
        if (System.getProperty("http.proxyHost") != null
                || System.getProperty("https.proxyHost") != null
                || System.getProperty("socksProxyHost") != null
                || System.getProperty("proxyHost") != null) {
            throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_UNAVAILABLE, "Configured proxy is unsupported in direct-only mode");
        }
        for (String envVar : PROXY_ENV_VARS) {
            String val = System.getenv(envVar);
            if (val != null && !val.trim().isEmpty()) {
                throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_UNAVAILABLE,
                        "Environment proxy (" + envVar + ") is unsupported in direct-only mode");
            }
        }
        try {
            ProxySelector selector = ProxySelector.getDefault();
            if (selector != null) {
                List<Proxy> proxies = selector.select(URI.create("https://example.com/"));
                if (proxies != null) {
                    for (Proxy proxy : proxies) {
                        if (proxy != null && proxy.type() != Proxy.Type.DIRECT) {
                            throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_UNAVAILABLE, "Environment proxy is unsupported in direct-only mode");
                        }
                    }
                }
            }
        } catch (IllegalArgumentException ex) {
            // Malformed URI check
        }
    }
}

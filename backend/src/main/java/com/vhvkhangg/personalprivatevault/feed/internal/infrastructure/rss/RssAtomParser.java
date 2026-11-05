package com.vhvkhangg.personalprivatevault.feed.internal.infrastructure.rss;

import com.vhvkhangg.personalprivatevault.feed.internal.infrastructure.hackernews.HackerNewsNormalizer;
import com.vhvkhangg.personalprivatevault.feed.source.exception.FeedRefreshException;
import com.vhvkhangg.personalprivatevault.feed.source.exception.FeedRefreshFailureReason;
import org.springframework.stereotype.Component;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Streaming JDK StAX parser for RSS 2.0 and Atom 1.0 feed documents,
 * enforcing DTD/XXE/XInclude denial, event/depth/text bounds, and record caps.
 */
@Component
public class RssAtomParser {

    public static final int MAX_EVENTS = 20000;
    public static final int MAX_DEPTH = 32;
    public static final int MAX_TEXT_CODE_UNITS = 1048576; // 1 MiB text
    public static final int MAX_RECORDS = 200;

    public static final String ATOM_NAMESPACE = "http://www.w3.org/2005/Atom";
    public static final String XINCLUDE_NS_1 = "http://www.w3.org/2001/XInclude";
    public static final String XINCLUDE_NS_2 = "http://www.w3.org/2003/XInclude";
    public static final String DC_NAMESPACE = "http://purl.org/dc/elements/1.1/";

    public enum FeedFormat {
        RSS_2_0,
        ATOM_1_0
    }

    public record RawEntry(
            int documentOrder,
            FeedFormat feedFormat,
            String title,
            String link,
            String provenanceId,
            String author,
            String pubDate,
            String updated
    ) {}

    public record ParsedFeed(
            FeedFormat format,
            int parsedEntriesCount,
            List<RawEntry> rawEntries
    ) {}

    public ParsedFeed parseFeed(byte[] xmlBytes) {
        if (xmlBytes == null || xmlBytes.length == 0) {
            throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_INVALID_RESPONSE, "Empty XML document");
        }

        XMLInputFactory factory = createSecureInputFactory();
        XMLStreamReader reader = null;
        try {
            reader = factory.createXMLStreamReader(new ByteArrayInputStream(xmlBytes));

            int eventCount = 1; // Reader initialized at START_DOCUMENT
            int depth = 0;
            int totalTextCodeUnits = 0;
            int recordCount = 0;
            int channelCount = 0;

            FeedFormat format = null;
            List<RawEntry> rawEntries = new ArrayList<>();

            // State within current record
            boolean inRecord = false;
            int recordDepth = 0;
            String currentField = null;
            StringBuilder currentText = null;

            // Record field accumulators
            String entryTitle = null;
            String entryLink = null;
            String entryProvenanceId = null;
            String entryAuthor = null;
            String entryPubDate = null;
            String entryUpdated = null;
            boolean inAuthor = false;

            while (reader.hasNext()) {
                int eventType = reader.next();
                eventCount++;
                if (eventCount > MAX_EVENTS) {
                    throw new FeedRefreshException(FeedRefreshFailureReason.FETCH_LIMIT_EXCEEDED, "Exceeded maximum XML events: " + MAX_EVENTS);
                }

                switch (eventType) {
                    case XMLStreamConstants.DTD ->
                            throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_INVALID_RESPONSE, "DTD DOCTYPE forbidden");

                    case XMLStreamConstants.START_ELEMENT -> {
                        depth++;
                        if (depth > MAX_DEPTH) {
                            throw new FeedRefreshException(FeedRefreshFailureReason.FETCH_LIMIT_EXCEEDED, "Exceeded maximum XML depth: " + MAX_DEPTH);
                        }

                        String ns = reader.getNamespaceURI();
                        String localName = reader.getLocalName();

                        // Disallow XInclude
                        if (XINCLUDE_NS_1.equals(ns) || XINCLUDE_NS_2.equals(ns) || ("include".equalsIgnoreCase(localName) && reader.getPrefix() != null && reader.getPrefix().toLowerCase(Locale.ROOT).contains("xi"))) {
                            throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_INVALID_RESPONSE, "XInclude forbidden");
                        }

                        if (depth == 1) {
                            // Root element validation
                            if ("rss".equalsIgnoreCase(localName)) {
                                if (ns != null && !ns.isBlank()) {
                                    throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_INVALID_RESPONSE, "RSS root must be unnamespaced");
                                }
                                String version = reader.getAttributeValue(null, "version");
                                if (!"2.0".equals(version)) {
                                    throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_INVALID_RESPONSE, "Only RSS version 2.0 supported, got: " + version);
                                }
                                format = FeedFormat.RSS_2_0;
                            } else if ("feed".equalsIgnoreCase(localName)) {
                                if (!ATOM_NAMESPACE.equals(ns)) {
                                    throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_INVALID_RESPONSE, "Atom feed root requires namespace: " + ATOM_NAMESPACE);
                                }
                                format = FeedFormat.ATOM_1_0;
                            } else {
                                throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_INVALID_RESPONSE, "Unsupported feed root element: " + localName);
                            }
                        } else if (format == FeedFormat.RSS_2_0) {
                            if (depth == 2) {
                                // Direct child of rss MUST be unnamespaced channel
                                if (!"channel".equalsIgnoreCase(localName) || (ns != null && !ns.isBlank())) {
                                    throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_INVALID_RESPONSE, "RSS root may only contain unnamespaced <channel>, found: " + localName);
                                }
                                channelCount++;
                                if (channelCount > 1) {
                                    throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_INVALID_RESPONSE, "Multiple <channel> elements in RSS feed");
                                }
                            } else if (depth == 3 && !inRecord) {
                                // Direct child of channel: detect start of item
                                if ("item".equalsIgnoreCase(localName) && (ns == null || ns.isBlank())) {
                                    inRecord = true;
                                    recordDepth = 3;
                                    recordCount++;
                                    if (recordCount > MAX_RECORDS) {
                                        throw new FeedRefreshException(FeedRefreshFailureReason.FETCH_LIMIT_EXCEEDED, "Exceeded maximum records: " + MAX_RECORDS);
                                    }
                                    entryTitle = null;
                                    entryLink = null;
                                    entryProvenanceId = null;
                                    entryAuthor = null;
                                    entryPubDate = null;
                                    entryUpdated = null;
                                }
                            } else if (inRecord && depth == 4) {
                                // Direct child of item: only unnamespaced core fields allowed
                                if (ns == null || ns.isBlank()) {
                                    String lower = localName.toLowerCase(Locale.ROOT);
                                    if ("title".equals(lower) || "link".equals(lower) || "pubdate".equals(lower) || "guid".equals(lower) || "author".equals(lower)) {
                                        currentField = lower;
                                        currentText = new StringBuilder();
                                    } else {
                                        currentField = null;
                                        currentText = null;
                                    }
                                } else {
                                    // Foreign namespace elements (e.g. x:title, dc:date) ignored for core fields
                                    currentField = null;
                                    currentText = null;
                                }
                            } else if (inRecord && depth > 4) {
                                // Descendants nested within description or other elements are ignored for core fields
                                currentField = null;
                                currentText = null;
                            }
                        } else if (format == FeedFormat.ATOM_1_0) {
                            if (depth == 2 && !inRecord) {
                                // Direct child of feed: detect Atom entry
                                if ("entry".equalsIgnoreCase(localName)) {
                                    if (!ATOM_NAMESPACE.equals(ns)) {
                                        // Unqualified or foreign entry (e.g. <entry xmlns="">) rejected
                                        throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_INVALID_RESPONSE, "Atom entry missing or invalid namespace");
                                    }
                                    inRecord = true;
                                    recordDepth = 2;
                                    recordCount++;
                                    if (recordCount > MAX_RECORDS) {
                                        throw new FeedRefreshException(FeedRefreshFailureReason.FETCH_LIMIT_EXCEEDED, "Exceeded maximum records: " + MAX_RECORDS);
                                    }
                                    entryTitle = null;
                                    entryLink = null;
                                    entryProvenanceId = null;
                                    entryAuthor = null;
                                    entryPubDate = null;
                                    entryUpdated = null;
                                    inAuthor = false;
                                }
                            } else if (inRecord && depth == 3) {
                                // Direct child of entry: must have ATOM_NAMESPACE
                                if (ATOM_NAMESPACE.equals(ns)) {
                                    String lower = localName.toLowerCase(Locale.ROOT);
                                    if ("title".equals(lower) || "id".equals(lower) || "published".equals(lower) || "updated".equals(lower)) {
                                        currentField = lower;
                                        currentText = new StringBuilder();
                                    } else if ("link".equals(lower)) {
                                        currentField = null;
                                        currentText = null;
                                        String rel = reader.getAttributeValue(null, "rel");
                                        String href = reader.getAttributeValue(null, "href");
                                        boolean isAlternate = rel == null || rel.isBlank() || "alternate".equalsIgnoreCase(rel.trim());
                                        if (isAlternate && href != null && entryLink == null) {
                                            String trimmedHref = href.trim();
                                            if (HackerNewsNormalizer.isValidArticleUrl(trimmedHref)) {
                                                entryLink = trimmedHref;
                                            }
                                        }
                                    } else if ("author".equals(lower)) {
                                        inAuthor = true;
                                        currentField = null;
                                        currentText = null;
                                    } else {
                                        currentField = null;
                                        currentText = null;
                                    }
                                } else {
                                    currentField = null;
                                    currentText = null;
                                }
                            } else if (inRecord && depth == 4 && inAuthor) {
                                if ("name".equalsIgnoreCase(localName) && ATOM_NAMESPACE.equals(ns)) {
                                    currentField = "authorname";
                                    currentText = new StringBuilder();
                                } else {
                                    currentField = null;
                                    currentText = null;
                                }
                            } else if (inRecord && depth > 3) {
                                currentField = null;
                                currentText = null;
                            }
                        }
                    }

                    case XMLStreamConstants.CHARACTERS, XMLStreamConstants.CDATA, XMLStreamConstants.SPACE, XMLStreamConstants.COMMENT -> {
                        int len = reader.getTextLength();
                        totalTextCodeUnits += len;
                        if (totalTextCodeUnits > MAX_TEXT_CODE_UNITS) {
                            throw new FeedRefreshException(FeedRefreshFailureReason.FETCH_LIMIT_EXCEEDED, "Exceeded maximum text code units: " + MAX_TEXT_CODE_UNITS);
                        }
                        if (eventType != XMLStreamConstants.COMMENT && inRecord && currentField != null && currentText != null) {
                            currentText.append(reader.getTextCharacters(), reader.getTextStart(), len);
                        }
                    }

                    case XMLStreamConstants.END_ELEMENT -> {
                        String localName = reader.getLocalName();

                        if (inRecord) {
                            if (depth == recordDepth) {
                                // End of current record
                                rawEntries.add(new RawEntry(
                                        recordCount - 1,
                                        format,
                                        entryTitle,
                                        entryLink,
                                        entryProvenanceId,
                                        entryAuthor,
                                        entryPubDate,
                                        entryUpdated
                                ));
                                inRecord = false;
                                recordDepth = 0;
                            } else if (currentField != null && currentText != null) {
                                String text = currentText.toString();
                                if (format == FeedFormat.RSS_2_0) {
                                    if ("title".equals(currentField) && entryTitle == null) {
                                        entryTitle = text;
                                    } else if ("link".equals(currentField) && entryLink == null) {
                                        entryLink = text;
                                    } else if ("pubdate".equals(currentField) && entryPubDate == null) {
                                        entryPubDate = text;
                                    } else if ("guid".equals(currentField) && entryProvenanceId == null) {
                                        entryProvenanceId = text;
                                    } else if ("author".equals(currentField) && entryAuthor == null) {
                                        entryAuthor = text;
                                    }
                                } else { // ATOM_1_0
                                    if ("title".equals(currentField) && entryTitle == null) {
                                        entryTitle = text;
                                    } else if ("id".equals(currentField) && entryProvenanceId == null) {
                                        entryProvenanceId = text;
                                    } else if ("published".equals(currentField) && entryPubDate == null) {
                                        entryPubDate = text;
                                    } else if ("updated".equals(currentField) && entryUpdated == null) {
                                        entryUpdated = text;
                                    } else if ("authorname".equals(currentField) && entryAuthor == null) {
                                        entryAuthor = text;
                                    }
                                }
                                currentField = null;
                                currentText = null;
                            }

                            if (format == FeedFormat.ATOM_1_0 && depth == 3 && "author".equalsIgnoreCase(localName)) {
                                inAuthor = false;
                            }
                        }
                        depth--;
                    }

                    case XMLStreamConstants.END_DOCUMENT -> {
                        // Reached document end successfully
                    }
                }
            }

            if (format == null) {
                throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_INVALID_RESPONSE, "No feed root element found");
            }

            if (format == FeedFormat.RSS_2_0 && channelCount != 1) {
                throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_INVALID_RESPONSE, "RSS 2.0 feed must contain exactly one channel element");
            }

            return new ParsedFeed(format, recordCount, rawEntries);
        } catch (XMLStreamException ex) {
            throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_INVALID_RESPONSE, "Malformed XML document", ex);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (XMLStreamException ignored) {
                }
            }
        }
    }

    private static XMLInputFactory createSecureInputFactory() {
        XMLInputFactory factory = XMLInputFactory.newFactory();
        factory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, Boolean.TRUE);
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
        factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
        factory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, Boolean.FALSE);

        // Verify effective security settings against Java 25 factory
        if (!Boolean.FALSE.equals(factory.getProperty(XMLInputFactory.SUPPORT_DTD))) {
            throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_INVALID_RESPONSE, "Failed to disable DTD support in XMLInputFactory");
        }
        if (!Boolean.FALSE.equals(factory.getProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES))) {
            throw new FeedRefreshException(FeedRefreshFailureReason.UPSTREAM_INVALID_RESPONSE, "Failed to disable external entities in XMLInputFactory");
        }

        factory.setXMLResolver((publicID, systemID, baseURI, namespace) -> {
            throw new XMLStreamException("External entity resolution is forbidden");
        });

        return factory;
    }
}

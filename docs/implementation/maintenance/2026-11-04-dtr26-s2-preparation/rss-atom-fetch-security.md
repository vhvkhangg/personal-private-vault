# DTR26-S2A — RSS/Atom network and XML parser threat model

**Status:** prospective safety contract, not a runtime guarantee. **Key difference from S1:** Hacker News has a **fixed hostname**; RSS fetches an Owner-configured, therefore potentially unsafe, arbitrary public hostname. Do not generalize or copy `HackerNewsDestinationPolicy`'s fixed-host rules unchanged.

## Supported feed protocol and parsing references

- [RSS Advisory Board current RSS 2.0 specification](https://www.rssboard.org/rss-specification) (plus its documented `guid`/`pubDate`/`link` fields).
- [Atom Syndication Format, RFC 4287](https://www.rfc-editor.org/rfc/rfc4287) (`id`, `title`, `link rel=alternate`, `published`, `updated`; href can be a relative IRI reference, which this initial S2A **explicitly does not support**).
- [OWASP SSRF Prevention](https://cheatsheetseries.owasp.org/cheatsheets/Server_Side_Request_Forgery_Prevention_Cheat_Sheet.html), especially resolved-peer/DNS pinning and redirects.
- [OWASP XML External Entity Prevention](https://cheatsheetseries.owasp.org/cheatsheets/XML_External_Entity_Prevention_Cheat_Sheet.html), including Java StAX/DOM/SAX settings and failure if a security setting is unsupported.

## Actual connection constraints — mandatory before enabling any RSS fetch

1. **Only `feed_url` is an outbound target**, parsed as an absolute HTTPS URI; hostname required; default port **443** only; deny IP literals, user-info, fragments, nonstandard schemes (`http`, `file`, `jar`, `ftp`, `gopher`, etc.) and unusual authority syntax. No credential-bearing URL, no URL supplied in a per-refresh body. Existing configuration CRUD may store otherwise invalid metadata; fail only at fetch.
2. Resolve A/AAAA via a **reviewed, injectable and bounded DNS resolver**. Reject local/private/link-local/multicast/unspecified/reserved IPv4 and IPv6, including IPv4-mapped IPv6 and non-public IPv6. Reject any unsafe answer rather than selecting a safe one while ignoring another. Bind the *actual HTTP connection* to validated answers; a standalone DNS precheck followed by a second unrestricted DNS lookup is **not** sufficient.
3. Preserve the hostname for TLS SNI, certificate hostname verification and HTTP Host. No automatic proxy (environment, OS, JVM, explicit), redirects, cookies, authentication, automatic retries, automatic content decompression or alternate client connection manager that bypasses the resolver. If the selected existing HTTP transport cannot prove these properties under the actual manager, stop for Owner decision; do not weaken policy silently.
4. Make one feed-document GET only; never follow alternate feed URLs, XML base links, embedded images, enclosures, or item link URLs. Treat feed title/entry title and XML metadata as untrusted plain text; frontend later must escape output rather than use raw HTML.
5. Fixed/bounded **proposed** resource budgets for S2A, to be validated in reviewer preparation: one GET, max response **1 MiB**, select at most **50 eligible entries** (with a finite scan ceiling e.g. **200 elements**), max XML depth **32**, max item title **1000 chars**, link **2048**, external ID provenance ≤ **500**, metadata JSON snapshot **2 KiB**, total refresh **25 s**, DNS caller cap **2 s**, per exchange **10 s**, short finalizer **5 s**. Explicitly cap aggregate decoded bytes if compression must be supported; default proposal rejects compressed response and requests identity. Reject feeds violating structural/global caps rather than silently treating truncation as a successful complete fetch.
6. **No secrets/payload logging:** log only stable source IDs, categorized failures, sanitized host/category if appropriate; do not log full feed XML, private URLs, headers, response body, tokens, session or arbitrary RSS `description`. When provider invalid, distinguish transport timeout/rate limit, unsafe destination, invalid XML, cap violation and valid empty feed. Preserve accepted Feed-local error envelope conventions.

## XML-specific hardening and correctness

- Prefer JDK secure streaming (`XMLInputFactory`/StAX) or existing proven parser; do not add unverified library merely for convenience. **Reject `DOCTYPE`**, disable DTD and external entity access, set resolver to throw for external references; no XInclude, XSLT, schema imports or external entity loading; if a critical feature cannot be disabled and verified, fail closed.
- Limit element nesting, total events/text, item count and time/bytes before building Java objects. Reject entity bombs, oversized CDATA, invalid encoding and malformed XML. No full in-memory DOM of an unbounded feed. XML namespaces must be interpreted correctly for Atom (`http://www.w3.org/2005/Atom`) instead of local-name-only spoofing.
- Restrict RSS support to an actual `rss version="2.0"` `channel` and Atom to RFC 4287 namespace. Do not accept custom RDF, RSS 1.0, OPML, XML-RPC or HTML disguised as RSS.
- Preserve only title/article URL/date/selected provenance. RSS `<guid>` may be non-URL; Atom `<id>` may be `urn:`; neither is a fetched URL nor, by default, a FeedItem externalId. Atom `<updated>` is last update, not necessarily publication time. RSS/Atom free-form HTML content is not mirrored.

## Mandatory adversarial fixtures (fake transport only)

| Case | Expected behavior |
| --- | --- |
| `https://127.0.0.1`, `[::1]`, RFC1918, link-local/metadata IP, public hostname resolving to private/mixed A+AAAA, DNS rebinding, redirect to private | **No unsafe connection**, categorical denial; exact peer/resolver proof, not just a string regex |
| Config URL `http://`, user-info, fragment, port 8443, proxy env variable, TLS hostname mismatch, unsafe fallback address | Reject/bypass impossible; no injected local destination reached |
| XML with DTD/external entities, XInclude, ENTITY expansion, nested bomb, malformed root, bogus Atom namespace, excessive body/entry count | No file/network/entity retrieval, finite memory/operations, safe failure |
| RSS GUID reused for different article links, two GUIDs sharing exact article link, Atom id changes across refresh | Preserve exact URL identity and deterministic representative; no generic ingest ambiguity or accidental archive overwrite |
| Missing publication timestamp, Atom updated only, invalid date, future date | Do not invent publishedAt; item may remain in source list, not fabricated 24h Radar entry |
| XML valid but no eligible entries versus HTTP error/malformed XML | Distinct successful-empty versus failure, and fetch timestamps advance **only** on successful normalized commit |
| Transfer interruption/timeouts, invalid content encoding, 1 MiB overrun | Release admission, no partial DB commit, no background process/task leakage |

**Runtime proof NOT_RUN.** The independent pre-handoff reviewer must confirm actual client/parser APIs available at the pinned Java/Spring/dependency baseline; the explicit numbers above are reviewable **proposals** rather than previously measured safe production limits.

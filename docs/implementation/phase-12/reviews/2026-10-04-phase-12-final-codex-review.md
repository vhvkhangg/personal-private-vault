# Phase 12 Codex final review

- Date: 2026-10-04
- Scope: implemented `phase-12-global-search` active handoff; production review only.
- Baseline: `44fdaa9137472b44217c159e87e5bc2b89ca54d4`; uncommitted Antigravity implementation.
- Outcome: **CHANGES_REQUESTED** — FR12-1 through FR12-6 remain open.
- Preparation P12-1/P12-2 remain closed; implementation proof is still required.

## Verification

Antigravity supplied `docs/implementation/phase-12/test-evidence.md`, claiming 803 passing tests. Codex independently
ran `mvn -f backend/pom.xml -ntp clean verify`: exit 0, BUILD SUCCESS, 803 tests, zero failures/errors/skips,
01:29 min, finished 2026-10-04T16:51:24+07:00. Surefire contains 803 testcase elements, including the 12 new Search
tests and 27 application architecture tests. Baseline test bodies are retained, with named-interface assertions
updated for the authorized extension. `git diff --check` passed before review documentation changes.

Environment: Java 25.0.2, Maven 3.9.15, Spring Boot 4.1.1, Spring Modulith 2.1.1, Hibernate 7.4.5.Final,
Testcontainers 2.0.5, PostgreSQL 18.6 (`postgres:18.6-alpine`). V1 then V2 applied successfully; Hibernate validation
and existing schema/Modulith tests passed.

Additional diagnostic sources exist only in ignored `backend/target/final-review-diagnostics/`:

- `Phase12ReviewProbe.java`: compiled production classes with an isolated one-hit owner/Vault fixture and reflective
  snippet checks. Exit 0 with the Surefire runtime classpath. Confirms FR12-1, query-validation part of FR12-2,
  and helper behavior for FR12-3. No production/test source edited.
- `Phase12PlanProbe.java`: actual Music source SQL captured from its service in a disposable PostgreSQL 18.6
  container, migrated V1/V2, 5,000 synthetic Music rows and ANALYZE. Exit 0. On the same connection, actual fuzzy SQL
  uses Seq Scan even with `enable_seqscan=off` (`Disabled: true`), while the literal-only control uses Bitmap Index
  Scan on `idx_music_tracks_title_trgm`. Container disposed. This is an index-capability check, not a latency benchmark.

The successful Java probe commands used `java -cp <java.class.path from the Search Surefire XML> <probe path>`.
Initial probe attempts lacked readable/runtime dependency classes; only the completed runs above are evidence.
An initial plan comparison used separate connections for SET and EXPLAIN; it was corrected to a single connection
before drawing the index-capability conclusion. Diagnostics are not replacement acceptance tests and will disappear
on the next clean build.

## Findings

Java paths below use prefix `backend/src/main/java/com/vhvkhangg/personalprivatevault/`.

### FR12-1 — Medium — A full source page is mistaken for proof of another result

Location: `search/internal/application/GlobalSearchService.java:143`, `:156`, `:257`.

`size >= K` sets a flag and makes `hasMore` true when exactly K deduplicated entries exist. A valid one-result
source with offset 0/limit 1 returns `items=1, hasMore=true` in the probe although no second result exists.
Exact terminal pages and overlapping text/tag sources have the same problem. Existing pagination tests cover only
nonterminal full pages and an underfull terminal page.

Required: derive `hasMore` from actual bounded lookahead/exhaustion information after deduplication, not reaching a
limit. Preserve the accepted top-K and transport bounds, including K=600. Test exactly-full final pages, offset
terminal pages, text/tag overlap, genuinely more results, exhaustion and maximum bounds.

### FR12-2 — Medium — Public owner search operations bypass bounded validation

Locations: `people/search/PeopleSearchQuery.java:12` and equivalent nine owner query records;
`people/internal/application/search/PeopleSearchService.java:152` and all owner `lookupDocuments` methods;
`knowledge/note/internal/application/search/NoteSearchService.java:30` and seven nested primitive search APIs;
`vault/internal/application/search/VaultSearchService.java:32`.

Global/Vault query records validate the complete query bounds, but owner records accept >200-character terms and
negative/oversized tag sets. The probe constructs a 201-character People query with tag -1 successfully. Validation
can occur only after querying candidates, or not at all when a source is empty. Nested primitive APIs have no positive
maximum limit validation, so callers can request an unbounded accumulated result despite bounded SQL pages. Every
bulk lookup passes an arbitrary ID set into IN queries without a maximum; parent facades fan it out. Vault does not
validate candidate-ID positivity and returns early on empty candidates before validating tags.

Required: enforce stable structural validation at every exposed owner/nested/Vault boundary before querying or
early returns: query length/nonblank, positive bounded candidate limits, positive tag IDs/count and positive bounded
lookup/qualification ID sets. Retain deliberate empty-input semantics. Reuse focused owner validation where useful,
without introducing forbidden reverse dependencies or a generic framework. Tests must prove rejection before DB work,
including invalid input against an empty source, plus valid maximum/empty/null behavior.

### FR12-3 — Medium — Snippets use unrelated fields and return raw markup

Locations: `fiction/internal/application/search/FictionSearchService.java:159`, `:212`;
`knowledge/information/internal/application/search/InformationSearchService.java:159`, `:216`;
`knowledge/note/internal/application/search/NoteSearchService.java:163`, `:217`; analogous multi-body helpers.

`extractSnippet` returns a non-null prefix even when the query is absent. Therefore an unrelated nonblank description
or summary prevents fallback to the review/Markdown/example that actually matched. Note/Information copy Markdown
and embedded HTML directly to the supposedly plain-text snippet. Diagnostic output includes
`# Heading|**Needle** <b>private</b>`. Appending ellipses outside the configured 240-character window yields 246
characters. The copied helper has spread these behaviors across owners.

Required: choose an actually matching body field, produce bounded plain-text navigation snippets (not raw Markdown/
HTML), and count ellipses within the chosen cap. Keep original Markdown untouched. Apply the correction consistently
to affected owner helpers without reopening unrelated business behavior. Test a later matching field behind an
unrelated earlier field, Markdown/HTML, long content, Unicode and source preservation.

### FR12-4 — Medium — Scalar fuzzy predicates defeat the new short-field index path

Locations: `collection/music/internal/application/search/MusicSearchService.java:69`, `:72`, `:82`;
equivalent short-field/tag fuzzy predicates; `GlobalSearchIntegrationTest.java:630`.

The actual Music predicate ORs indexed literal matching with scalar `similarity(lower(title), :q) >= 0.30`.
The single-connection PostgreSQL probe shows no trigram index candidate path for that actual query, even with scans
discouraged; its literal-only control can use the V2 index. Music has no body field to justify the fallback.
Ordinary >=3-character searches scan the whole source, and successive qualification pages repeat filtering/sorting.
The submitted plan test examines only a simplified LIKE query, not the service's fuzzy query.

Required: provide an index-compatible short-field candidate path while preserving the explicit >=0.30 semantics,
literal matching and deterministic source selection. If using an operator prefilter, control its threshold safely
so it cannot discard eligible matches or depend solely on pooled session state. Prove actual partial/fuzzy owner
and Vault query plans on representative data; distinguish unavoidable body-scan limitations. Do not force planner
settings in production, add speculative body indexes, or change the approved matching contract.

### FR12-5 — High — Mandatory PostgreSQL and bounded-query proofs are missing

Locations: `backend/src/test/java/com/vhvkhangg/personalprivatevault/search/GlobalSearchIntegrationTest.java:463`,
`:527`, `:628`; canonical Phase 12 README Testing contract and active handoff Test/evidence contract.

The ALBUM/IMAGE test has two candidates with limit ten and asserts only final Java order. Wrong pre-LIMIT SQL
selection could still pass. There is no tag-only ALBUM/IMAGE limiting regression, multi-type more-than-K Media source
proof, or cross-domain multiple-offset source-membership comparison. P12-1 covers reverse titles and late text
qualifiers, but not multiple matching-tag collapse before limiting, early tag-source required-tag rejection,
multi-tag AND/Film Credit exclusions or bounded bulk/query-count evidence. Migration proof covers one LIKE index,
not the required inventory/actual fuzzy plans. Field-specific Markdown and privacy/validation failure paths are also
not established. The green suite therefore does not satisfy the explicit handoff proof contract.

Required: add the omitted adversarial PostgreSQL regressions and focused boundary/field/privacy tests, including
FR12-1–FR12-4. Instrument query/batch calls across changing result counts and late-qualification pages. Assert exact
selected IDs, not only sorting of an already-selected Java list. Verify extension/index inventory, unchanged frozen
schema and exact named-interface/parent isolation requirements. Re-run full clean verify preserving baseline coverage.

### FR12-6 — Medium — Evidence and implementation documentation do not describe reality

Locations: `docs/implementation/phase-12/test-evidence.md:49`, `:51`, `:121`, `:125`, `:134`;
`docs/implementation/handoffs/ACTIVE.md:155`; `docs/repository/repository-package-tree.md` Feed/nested owner trees.

Evidence describes LONG_FUZZY at 300, TAG_ONLY at 200, title-based tie-breaking, and blank/tag-only query behavior;
none matches the accepted contract or actual code. Several listed integration/architecture test names do not exist.
It claims strict tag AND proof and more architecture enforcement than the listed tests establish. Warning/limitation
and query-count/actual-plan evidence is absent. The package tree adds Search but omits new owner search capabilities
and still lists Feed dependencies without `vault::search`. Current workflow docs were left at preparation/no active
handoff; Codex has synchronized their current gate below, without changing historical preparation acceptance.

Required: replace inaccurate implementation/test descriptions with actual behavior and exact test names; never claim
unexecuted coverage. Record corrected commands, results, plans, counts, versions, warnings and limitations after
remediation. Update only the authorized implemented package extensions in the tree. Preserve historical reports and
keep current statuses consistent with the active handoff; do not freeze the phase or begin Phase 13.

## Other review dimensions / limitations

- Ownership, dependency direction and approved scope are intact in inspected sources/descriptors: no new Search
  persistence, reverse dependencies, HTTP, projection, feature mutation or V1/schema rewrite found. Parent facades
  map nested results without exposing nested types. Queries are bound and read-only; no raw-value logger found.
- Narrow facades and direct orchestration fit the concrete task. No speculative hierarchy/thread pool/eventing or
  generic repository framework was added. Repeated snippet/validation behavior needs the focused consistency work
  above, not a cross-module framework. No new lock/global snapshot is required for these accepted discovery reads.
- Leaf packages have package-info and the replaced Search placeholder was removed. Tree documentation needs FR12-6.
- Compiler/runtime notices: Lombok deprecated Unsafe use, existing CsvImportParser and test PostgreSQLContainer
  deprecation, Mockito/ByteBuddy self-attachment/dynamic agent/class-sharing notices, SpringDoc enabled-endpoint
  warnings. The diagnostic probe also uses deprecated test-container construction. No owner IDE warning supplied;
  no IDE inspection or separate Spotless/Checkstyle/SpotBugs run. No warning-free claim or blanket suppression.
- Graphify was targeted navigation only (truncated results); canonical sources and actual code governed. Search,
  Java/Spring, architecture, persistence, testing, pragmatic SOLID, reuse and pattern skills informed the findings,
  especially boundary validation, source selection and actual query-plan verification. No subagents or production
  changes; only review/status docs and ignored diagnostic artifacts written.

## Next step

Owner runs Antigravity `/antigravity-implement-handoff` to remediate FR12-1–FR12-6, retain exact evidence, and return
the same handoff as `IMPLEMENTED_AWAITING_CODEX_REVIEW`. Then run `$codex-final-review` again. No commit message
or commit/push authorization is provided. Phase 10–12 milestone remains the post-acceptance/owner-commit gate.

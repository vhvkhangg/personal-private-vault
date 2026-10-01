# Phase 10 final Codex re-review

- Date: 2026-10-01
- Handoff: `phase-10-feed-importdata-foundation`
- Outcome: **CHANGES_REQUESTED**
- Scope: remediation of the initial final review; no production changes made by Codex.

## Findings disposition

- F1 privacy: constant unknown-field errors and payload-free catastrophic parsing exceptions are implemented;
  inspected regressions pass. Closed for the reviewed implementation paths.
- F2 structural/numeric fidelity: JSON integral/decimal and malformed-frontmatter handling improved, but Markdown
  remains an alternate bypass. Open as detailed below.
- F3 Markdown whitespace: quoted CSV regressions pass, but unquoted content still loses whitespace. Open.
- F4 deferred constraint translation: both SavedResource paths now use `saveAndFlush` inside safe translation;
  ingestion flush is inside its catch. Implementation defect closed; database-loser proof remains part of F5.
- F5 regression/evidence: genuine post-target provenance failure and five preloaded job-context races added and
  passing; full target execution improved. Feed contention proof and evidence accuracy remain open.

## Blocking findings

### F2 — High — Markdown bypasses the structural and precision corrections

`importdata/internal/parsing/MarkdownImportParser.java:20,59,84–97` still uses a default YAML ObjectMapper and
constructs a `VALID` payload directly, rather than enforcing the owner-local canonical structural rules.
Unknown frontmatter must be preserved, but precision and recognized field types must not be silently changed.

Synthetic diagnostics confirmed:

- YAML `precise: 123456789.987654321` loses numeric precision (`precision preserved=false`).
- YAML `summary: [one, two]` becomes the string `[one, two]` and the item is `VALID`.
- A supplied blank frontmatter key produces `VALID`, contrary to the claimed corrected validation policy.

Required: preserve YAML decimal precision and JSON-compatible frontmatter; enforce recognized scalar fields and
supplied frontmatter structure before returning VALID, using owner-local canonicalization without rejecting
legitimate unknown frontmatter keys or duplicating Knowledge business validation. Add parse/validate/execution
regressions for exact persisted frontmatter numbers, complex summary/source fields and invalid keys.

### F3 — Medium — Unquoted CSV content is still trimmed

`CsvImportParser.java:26` retains `.setIgnoreSurroundingSpaces(true)`. Removing `.setTrim(true)` preserves quoted
content, but the CSV decoder strips spaces from unquoted cells before the content-field policy sees them.
Diagnostic input `title,contentMarkdown\nSynthetic,  body  \n` produced `CSV unquoted whitespace preserved=false`.

Required: preserve decoded content for both quoted and unquoted fields; normalize headers/non-content fields
explicitly. Add exact parse and persisted Note/Information assertions for unquoted indentation and trailing
hard-break spaces. Keep the existing quoted multiline regressions.

### F5 — Medium — Feed tests can pass without exercising a database loser; evidence still disagrees with source

`FeedIntegrationTest.concurrentDualKeyIngestionConvergesSafely`, `concurrentSavedResourceCreationRacesSafely`
and `concurrentFeedSavedResourceCreationRacesSafely` release a common start latch, but have no barrier after
the uniqueness lookup and no assertion of PostgreSQL contention. A caller may finish before the other looks up
the key: then the loser is merely the pre-check conflict. The ingestion test even accepts two successes (a
sequential insert/update). These tests can pass without exercising the behavior the initial review required.

Required: deterministically drive both transactions past the pre-check into actual unique-key competition, using
test-only coordination and observed PostgreSQL waiting/explicit commit. Prove the expected domain conflict,
one surviving resource/item, losing Vault rollback and captured payload/hash/vendor-detail redaction. Include
the relevant dual-key insert/update race paths; do not broaden production scope just to coordinate tests.

Correct the remaining documentary claims:

- `test-evidence.md` shows `findByIdWithItems` in the guard example; source uses `findByIdForUpdate`.
- Evidence claims blank-key YAML validation already occurs; the diagnostic above disproves that.
- ACTIVE still states a scheduling formula using `now` when no previous fetch exists; source correctly leaves
  `next_fetch_at` null until a fetch, and on schedule changes derives it from `last_fetched_at` only.
- ACTIVE labels 598 as the baseline and 74 as Phase 10. The approved pre-Phase-10 baseline is 594; 672 is an
  increase of 78 including four architecture additions. Separate baseline/new-domain/new-architecture counts.
- Evidence names/configuration/verified behavior must match actual tests and parser calls (including `.get()`
  rather than the documented `.build()`), without describing unobserved contention as proven.

### F6 — Medium — Extra CSV cells are silently discarded

`CsvImportParser.java:34` converts each record with `record.toMap()` without validating record/header width.
Diagnostic input `title,contentMarkdown\nSynthetic,body,discarded\n` returned `VALID`, dropping the extra cell.
Malformed rows should be INVALID rather than importing a silently truncated payload.

Required: validate row/header structure before mapping, reject inconsistent records with a payload-free item
diagnostic, and deliberately reject duplicate/ambiguous headers rather than silently overwriting values.
Add extra/missing cell and duplicate-header regressions, preserving per-item indices and no target writes at parse.

## Independent verification

- `mvn -f backend/pom.xml -ntp clean verify`: exit 0, BUILD SUCCESS, 672 tests, 0 failures/errors/skips;
  duration 01:05, finished 2026-10-01 14:27:16 +07:00. PostgreSQL/Flyway/Hibernate and Modulith checks pass.
- `git diff --check`: exit 0 before review updates; checked again after documentation changes.
- Synthetic Java source diagnostic in ignored `backend/target/final-review-diagnostics/`, using Surefire's exact
  dependency classpath: exit 0; confirms all parser behaviors above. Synthetic content only.
- Warnings: Lombok/Unsafe terminal-deprecation and existing deprecated API in `AbstractPostgresIntegrationTest`;
  no IDE inspection or warning-free assertion.

## Other mandatory dimensions

The prior scope, persistence mappings, parent-Knowledge/Vault boundaries, JSON isolation, bounded ordered query
shape, cohesion/SOLID/pattern and package-hygiene assessments remain applicable. The new flush calls preserve
whole-transaction rollback and introduce no independent commit. Job guard refresh remains correct; deterministic
preloaded job tests now exercise it. No new blocking performance issue or frozen schema/public API change found.
Package-tree implementation detail was synchronized. Correctness/privacy/reuse concerns remaining are identified
above: the Markdown adapter must not bypass its canonical structural rules. Green tests do not close these gaps.

## Next gate

Run Antigravity `/antigravity-implement-handoff` for F2, F3, F5 and F6, refresh truthful evidence, and rerun focused
then full verification. Return ACTIVE to `IMPLEMENTED_AWAITING_CODEX_REVIEW` for Codex. Do not commit/push yet.

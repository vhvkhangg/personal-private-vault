# Phase 10 final Codex review

- Date: 2026-10-01
- Handoff: `phase-10-feed-importdata-foundation`
- Outcome: **CHANGES_REQUESTED**
- Scope: implemented Feed + ImportData foundation; frozen Phases 0–9 remain unchanged.

## Blocking findings

### F1 — High — Parser diagnostics expose private input

`importdata/internal/parsing/TargetPayloadCanonicalizer.java:52` (also the other three target branches)
persists `"Unknown field: " + key`. The key is attacker/caller-controlled imported content, not a safe canonical
field label. `importdata/internal/application/ImportJobService.java:142` also appends the parser's raw
`ex.getMessage()` to the public exception. Jackson/CSV diagnostics can contain input fragments.

Required: use stable payload-free diagnostics for unknown keys and catastrophic syntax failures; do not retain
raw parser details in exposed messages, causes or logs. Test synthetic private markers in unknown JSON keys,
CSV headers and malformed input, asserting stored item errors, public exceptions and captured logs omit them.
The synthetic canonicalizer diagnostic confirmed `error includes input key=true`.

### F2 — High — Structural canonicalization silently changes or discards data

`TargetPayloadCanonicalizer.getLong/getInteger` narrow arbitrary `Number` values with `longValue/intValue`;
fractional IDs and out-of-range numbers can become different valid identities/counts. The diagnostic confirmed
`authorPersonId = 3.9` produces `VALID` with ID `3`. `getBigDecimal` converts other numeric types through
`doubleValue`, and `JsonImportParser` uses default floating-point tree conversion, risking decimal precision loss.
`getString` accepts objects/arrays through `toString`; `canonicalizeNote` silently replaces non-map frontmatter
with an empty map and drops non-string keys. An array-valued frontmatter diagnostic produced `VALID` and `{}`.

Required: enforce structural types and exact, range-checked integral conversion; preserve decimal precision from
parsing through canonical command construction. Reject malformed supplied frontmatter rather than discarding it.
Keep target business validation owned by Knowledge. Add CSV/JSON/YAML boundary regressions for fractional/overflow
IDs and counts, high-precision numbers, object-valued strings and malformed frontmatter; no target writes at parse.

### F3 — Medium — CSV parsing destroys Markdown whitespace

`CsvImportParser.java:27,39` globally trims fields, including quoted `contentMarkdown`. The diagnostic passed
`"  indented content  \n"` and confirmed `CSV markdown preserved=false`. Leading indentation, trailing spaces
and final newlines have Markdown meaning; valid imported content must not be rewritten by a format adapter.

Required: preserve decoded text for content fields; apply only field-specific canonical normalization and the
approved nullable blank policy. Test CSV Note/Information content with indentation, hard-break spaces, quoted
multiline content and final newlines, including exact persisted parent-Knowledge content after execution.

### F4 — High — SavedResource uniqueness losers escape domain translation at commit

`feed/internal/application/SavedResourceService.java:104,154` catches violations around `repository.save` only.
`SavedResource` uses an assigned Vault identity and `Persistable.isNew`, so persistence can defer the insert
until transaction flush/commit. Two callers can both pass the pre-check; the losing database violation then
occurs outside the service catch. Database rollback still protects the Vault row, but callers receive a raw
persistence exception instead of the required privacy-safe `SavedResourceConflictException`.

Required: force the relevant writes/constraint checks inside the safe translation boundary without independently
committing, swallowing failures or querying an aborted transaction. Exercise both manual and feed saves under
real PostgreSQL contention; assert the loser exception, one saved resource, no orphan Vault entry and no private
URL/hash/vendor-detail leakage. Audit ingestion update flush boundaries for the same deferred-violation issue.
Existing `duplicateUrlHashRollback` is sequential and takes the pre-check path; it does not prove a database loser.

### F5 — Medium — Mandatory regressions and evidence are incomplete/inaccurate

`FeedIntegrationTest` has no concurrent dual-key ingestion or SavedResource uniqueness tests. Its
`conversionFailureRollback` uses an invalid blank target title, so no target is created before failure and it
does not prove rollback when provenance persistence fails after target creation. The five ImportData race tests
observe real lock waits, but do not preload the losing job into its persistence context before the winner commits,
as explicitly required by the handoff. The matrix creation test is not end-to-end execution coverage of all targets.

Required: complete the canonical README/handoff tests, including genuine database Feed races, post-target
provenance failure, preloaded-context fresh-state races, all four target imports and parser regressions above.
Assert affected target/Vault/item rows and job counts/statuses, not just the returned status.

Correct `test-evidence.md` and ACTIVE's implementation result from actual source/test reports: several listed
"exact" test names do not exist, Markdown uses Jackson `YAMLFactory` rather than the claimed explicit SnakeYAML
`SafeConstructor`, entity IDs are bigint rather than UUID, and the implementation summary lists incorrect
FeedSourceType/SavedResourceKind/ImportItemStatus constants and scheduling calculation. Do not change correct
source enums/scheduling to match erroneous prose. Record real safe YAML configuration, commands, compiler warnings
and limitations. Synchronize the package-tree implementation detail required by the handoff without redesign.

## Independent verification and review dimensions

- `mvn -f backend/pom.xml -ntp clean verify`: exit 0, BUILD SUCCESS, 658 tests, 0 failures/errors/skips;
  duration 01:08, finished 2026-10-01 13:42:30 +07:00. Includes Flyway/PostgreSQL and Modulith verification.
- `git diff --check`: exit 0 before and after review/status documentation updates.
- Synthetic source-only Java parser diagnostic under ignored `backend/target/final-review-diagnostics/`, using
  the exact Surefire dependency classpath: exit 0; outputs documented under F1–F3. An initial diagnostic with
  mixed local Commons IO versions failed from a classpath mismatch; that was diagnostic setup, not a project defect.
- Static diagnostics: build reports Lombok/Unsafe terminal-deprecation and an existing deprecated API in
  `AbstractPostgresIntegrationTest`. No independent IDE inspection or warning-free claim.
- Correctness/security: blocking parser fidelity and privacy findings above; staged parsing and Knowledge ownership
  are present. Parse's attempted FAILED transition followed by a runtime exception rolls back: document/test actual
  failure semantics, without introducing an independently committing transition contrary to the handoff.
- Transactions/concurrency: shared pessimistic job guard plus explicit refresh are present; existing five races
  observe PostgreSQL waits. Whole-job rollback passes. Remaining boundaries/required proof are F4–F5.
- Maintainability/SOLID/reuse/patterns: cohesive capability services, narrow public parent-Knowledge calls and
  owner-local helpers; no speculative framework or cross-module persistence helper reuse found.
- Performance: indexed lookup paths and positive-bounded deterministic reads inspected; no separate blocking
  performance finding. Whole-job locking is the approved serialization contract, not speculative contention tuning.
- Scope/package/architecture: no migration/DBML/frozen API rewrite; module dependency tests pass and placeholders
  are removed. Package-tree synchronization/evidence accuracy remain F5.

## Next gate

Antigravity must remediate F1–F5 via `/antigravity-implement-handoff`, update truthful evidence, rerun focused tests
then the required full build and whitespace check, and return ACTIVE to `IMPLEMENTED_AWAITING_CODEX_REVIEW`.
Owner commit/push is not authorized by this review. No production changes were made by Codex.

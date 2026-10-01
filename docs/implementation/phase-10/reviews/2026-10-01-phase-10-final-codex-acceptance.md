# Phase 10 final Codex acceptance

- Date: 2026-10-01
- Handoff: `phase-10-feed-importdata-foundation`
- Outcome: **READY FOR OWNER COMMIT**
- Prior reviews remain historical; this report is the current disposition.

## Findings closed

| Finding | Verified disposition |
| --- | --- |
| F1 — parser privacy | Unknown fields and catastrophic parsing use stable payload-free errors; raw parser details are not passed through the public failure path. Privacy regressions pass. |
| F2 — structural/numeric fidelity | JSON/YAML decimal parsing and exact integral conversion are present; Markdown delegates structural canonicalization and validates nested keys/scalar fields. ImportData's owner-local `ImportPayloadJsonType` preserves decimal precision across payload persistence/reload. End-to-end YAML decimal storage regression passes. |
| F3 — CSV content whitespace | Both global trim options are removed; content fields retain decoded quoted/unquoted whitespace in parsed payloads. Note preserves it end-to-end; Information uses the unchanged canonical Knowledge normalization. |
| F4 — deferred constraint translation | SavedResource save-and-flush and ingestion flush occur within safe translation boundaries. Deterministic PostgreSQL races verify conflict losers and losing Vault rollback. |
| F5 — regression/evidence gaps | Five Feed races hold the winning transaction uncommitted, observe an ungranted PostgreSQL lock, then commit/reject the loser. External-ID/URL-hash collisions are covered. Post-target provenance failure and five preloaded job-context races pass. Counts and evidence are synchronized. |
| F6 — CSV structure | Row width is checked before mapping; inconsistent rows remain indexed INVALID items. Duplicate/ambiguous headers reject safely. Unit and execution regressions pass. |
| F7 — frozen/shared scope | Knowledge production and application.yml have no diff; root `VaultJsonFormatMapper.java` is absent. Serialization correction is restricted to ImportData's payload mapping. No scope expansion is needed for the accepted slice. |

The handoff requires parent Knowledge to remain authoritative. Earlier requests for verbatim persisted Information
content must be read subject to that frozen boundary: preserving parser input is required, but changing Information's
existing `trimOrNull` domain semantics is not authorized. The accepted implementation preserves import payloads,
then delegates target normalization to unchanged Knowledge. This is not approval to alter frozen Knowledge JSON
view/normalization behavior or global serialization.

## Independent verification

- `mvn -f backend/pom.xml -ntp clean verify`: exit 0, BUILD SUCCESS; **685 tests, 0 failures, 0 errors, 0 skips**.
- Duration: 01:51; finished 2026-10-01 19:35:01 +07:00.
- PostgreSQL 18.6 Testcontainers/Flyway V1, Hibernate validation, Spring Modulith and existing baseline regressions pass.
- Composition: 594 pre-Phase-10 tests + 87 new domain tests + 4 architecture additions = 685.
- `git diff --check`: exit 0 before documentation synchronization; rerun after final updates.
- Graphify navigation queried; important facts verified in source. Generated graph/build outputs are not delivery artifacts.
- Compiler notices: Lombok/Unsafe terminal deprecation, deprecated Commons CSV duplicate-header API in
  `CsvImportParser`, and existing deprecated API in `AbstractPostgresIntegrationTest`. These are nonblocking;
  no suppression or warning-free/IDE-inspection claim. CSV API modernization is not required for this acceptance.

## Mandatory review dimensions

- Correctness: staged parsing, structural-vs-business validation, explicit decisions/counts, Note-only dedupe,
  scheduling, dual-key rules, lifecycle/terminal guards and parent target writes reviewed with passing regressions.
- Persistence/concurrency: approved job-row lock plus authoritative refresh, whole-job rollback, target/provenance
  atomicity, deterministic unique-key conflict handling and Vault identity rollback verified. Local JSON UserType
  remains internal and maps unchanged JSONB; no schema/migration or independently committing transition introduced.
- Security/privacy: safe domain conflicts and parser errors, isolated JSON graphs, no live provider/upload/network
  expansion. Existing private-data logging safeguards and privacy tests pass.
- Maintainability/reuse/SOLID/patterns: narrow public capabilities, cohesive owner-local services/parsers, canonical
  Knowledge validation reused. The local JSON mapping solves the concrete precision problem without a global
  framework or cross-module persistence dependency. No blocking abstraction/duplication issue remains.
- Performance: bounded deterministic reads, indexed source/key lookups and approved transaction-wide job guard;
  no separate blocking query-shape or speculative optimization requirement found.
- Package/architecture/scope: named interfaces and Modulith boundaries pass, implemented placeholders removed,
  package-tree detail synchronized, frozen production outside Feed/ImportData unchanged and deferred scope absent.
- Documentation/evidence: current acceptance replaces historical CHANGES_REQUESTED reports; handoff and navigation
  statuses synchronized. Implementer evidence supplemented with independent verification and warning limitations.

No blocking finding remains. Codex made only review/status documentation edits in this invocation, not production
changes, commits, pushes, tags or PR actions.

## Owner action

Commit/push the Phase 10 package with:

`feat(backend): implement feed and importdata foundations`

Then give the latest package to ChatGPT for Phase 10 closeout/freeze and Phase 11 preparation. Phase 10 does not
trigger a milestone review; the next milestone is after Phase 12.

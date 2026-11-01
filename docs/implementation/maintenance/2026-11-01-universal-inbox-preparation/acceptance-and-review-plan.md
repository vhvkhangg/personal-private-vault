# Universal Inbox — prospective acceptance matrix and review gates

**Status:** DESIGN / PRE-HANDOFF REVIEW REQUIRED; **not** executable implementation authorization. Based on `main` `84f11854de20cde4dcdcd7b082b4c40c1410885b`.

## Reuse-first acceptance for the bounded first slice

| ID | Required testable outcome (prospective) | Evidence expectation / risk |
| --- | --- | --- |
| UI-01 | Manual and Feed-item save continue to create exactly one Vault Entry and SavedResource atomically; duplicates keep the existing stable conflict | PostgreSQL/Testcontainers path, sequential + concurrent duplicate, rollback/no orphan; reuse prior accepted tests where source hash remains applicable |
| UI-02 | Existing `GET /api/v1/saved-resources`, ID/hash lookup and Study/Information/Note conversions remain backward compatible | REST/OpenAPI focused tests and response-shape comparison; no new global search or knowledge cross-internals |
| UI-03 | New Inbox listing is bounded, deterministic and supports existing kind filter / next-page cursor if that API is selected | 0/1/100/101 rows, equal timestamps, cursor duplicate/skip proof, filter/invalid cursor, stable sort and finite query bounds |
| UI-04 | Default Inbox excludes Vault trashed entries and does not accidentally resurrect duplicates or expose internal deleted records | Trash/restore, concurrent updates and missing entry cases, legitimate inter-module public contract; no direct Vault repository injection |
| UI-05 | Favorites and tags are reused through Vault-owned operations; conversions remain explicitly requested and atomic | Test one favorite/tag/trash path and one converted target rollback/provenance failure path where genuinely touched |
| UI-06 | Browser/content security: stored URLs and external titles are untrusted; no source fetch, script content execution, secret disclosure or unapproved egress | Security configuration/controller inspection, safe response, unsafe URL scheme decision, no external HTTP client added |
| UI-07 | Implementation respects current Spring Modulith import graph and database migrations; no frozen-schema rewrite | Spring Modulith/ArchUnit where relevant, actual SQL/JPA mapping/migration proof and owner-gated scope amendments |
| UI-08 | Safe default test selection excludes archived S19-2 QA; regression coverage includes new Feed/Vault behavior without running inference | Focused Java test selection after CSA26, bounded runner receipts; full suite only when specifically approved and safe |
| UI-09 | Current docs, ADR/database/API notes and package tree accurately reflect only implemented behavior; historical immutable decisions untouched | Docs Sync Check, local links, OpenAPI/generate source comparison, actual changed-file inventory |
| UI-10 | Independent Codex review accepts only the approved implementation and reports actual evidence, open findings, counter and next step | Antigravity self-review then separate `$codex-final-review`; Owner-only commits/pushes |

## Preparation review questions — mandatory

The independent `$codex-pre-handoff-review` must: verify the actual latest published source before choosing implementation scope; check Phase 10/13 frozen contracts and Module Matrix; compare reuse-only against the proposed extra endpoint; establish the minimum Vault trash filtering mechanism; confirm whether any new migration is truly needed; settle URL scheme handling and backwards-compatibility implications; proportionally select fresh real DB and web tests; and record `KEEP / SIMPLIFY / DEFER / REJECT` for alternative designs. It must not mark unimplemented API or status fields as current facts.

If the reviewer finds that Inbox status (`read_at`, `archived_at`) is required for MVP, **stop and request an explicit Owner migration/baseline amendment** before `$codex-create-handoff`. A proposed schema layout in the design note is not Owner approval.

## Escalation discipline and baseline

All CRITICAL/HIGH/MEDIUM block an implementation acceptance; LOW/INFO are recorded and scheduled according to the current Owner policy unless a separately explicit scope requires closure. The **future final comprehensive non-RAG audit** has the Owner's stronger rule to adjudicate/fix all actionable findings, including LOW/INFO, before frontend; that rule is not silently applied as an indefinite new feature-review loop. No S19-2 finding is reopened, tested or counted.

Approval sequence: OWNER_DESIGN_SCOPE → independent `$codex-pre-handoff-review` → preparation Owner publication → explicit `$codex-create-handoff` for approved bounded feature → Antigravity + self-review → independent `$codex-final-review` → Owner feature publication → brief closeout. No implementation, migration, docs historic rewrite, staging or push by this preparation package.

**Next step:** Owner | apply package and run `$codex-pre-handoff-review` for this design scope | no active handoff, complete any `OWNER_DECISION_REQUIRED` item first | no RAG counters reset.

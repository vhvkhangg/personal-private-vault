# D0/D1 — All-docs, product and architecture audit plan

**Status:** Planning only. Read-before-write; no current/frozen/historical artifact is changed by this package. Source validation remains provisional until Backend audit and X reconciliation.

## 1. Scope and review depth

Enumerate **all** `docs/**` at the approved audit HEAD. All paths receive a classification, not necessarily the same depth: `CURRENT_CANONICAL_DEEP`, `CURRENT_DERIVED_DEEP`, `HISTORICAL_PROVENANCE`, `REFERENCE_STATIC`, `BINARY_EXPORT_COMPARE`, `FUTURE_PROPOSAL`, `NOT_APPLICABLE_WITH_REASON`. Never treat a mere filename scan as review of content.

| Group | Mandatory deep questions | Auxiliary checks |
|---|---|---|
| `docs/adr/**` | Owner need, ADR statuses/supersession, alternatives, evidence, KISS/YAGNI, frozen exceptions | Titles/links/decision index, duplicate obligations |
| `docs/architecture/**` | Current components, module boundaries, API/security/data flows, C4 and diagrams | Diagram source/export reconciliation, obsolete current claims |
| `docs/database/**` | DBML vs JPA/Flyway/DDL, identity, FK, indexes, versioning, migration paths | Parser or line-based checks, missing generation metadata |
| `docs/implementation/**` | Current `ACTIVE`, accepted slices, source/closeout SHAs, independent reviews and findings | Automated historical path/link/SHA proof, read-depth by risk |
| `docs/repository/**` | Package tree/class responsibility against actual code | Manifest comparison, links |
| `docs/workflow/**` | Codex-only exception, inherited Antigravity workflow, resume, STOP, Owner publication | Scenario routing and stale action checks |
| Docs templates/other | Duplication, usefulness, format correctness, owners and navigation | Link/spelling/format inventory |
| `AGENTS.md` all locations | Durable instructions/precedence versus docs current state | Context cost, contradictory modes, read-only regression |

Historic reviews, accepted decisions, failure logs and publication receipts are **immutable evidence**. Review their provenance, referencing, coverage and factual claims in the appropriate **event-time** context; record erroneous/contradictory present-day pointers in a *new dated correction or current navigation*, not a silent edit of history.

## 2. Architecture/product requirement assessment

For every current functional module and cross-module feature, fill:

`Requirement ID → Owner-visible scenario → accepted current behavior → implementation contract → dependency complexity → alternative 0 (keep) → alternative 1 (simplify) → measurable benefit → migration/regression cost → data/privacy impact → recommendation → Owner approval required? → evidence.`

Perform **two independent comparisons**:

1. **Correctness:** does the accepted system fulfill the Owner's actual use case? Distinguish feature delivered, docs-only readiness, pending Frontend, operationally not verified, intentionally deferred.
2. **Simplicity:** is an extra subsystem/interface/transaction boundary currently providing a necessary guarantee? Can it be removed without changing required behavior? Do not infer overengineering solely from number of classes or ADRs.

Examples for challenge, **not preapproved changes**:
- Is a generic multi-tenant/role policy unnecessary for the permanent singleton + fixed two profiles? Could existing deny-by-default allowlist suffice without breaking token/session semantics?
- Does a `name/title/displayName` on certain *list/preview* items eliminate avoidable detail round-trips without complex joins, false titles, N+1 and privacy leaks?
- Does BF26-S5 Option A already supply the selected Backend reads, while Dashboard UI/E2E remains out of scope? Do not create speculative aggregate APIs.
- Do any event, abstraction, wrapper, or scheduler policies exist only for a future feature never approved? Can they be deferred without data integrity regressions?

The accepted V1 trash DTO `id,entryType,deletedAt` is **not** a bug solely because it lacks a display name. Test existence of a safe cheap label and actual consumer need. Any wire contract/frozen schema change is a new Owner decision.

## 3. Mandatory privacy and data-surface review

Build a profile-by-method/path **allowlist** and field-exposure matrix using generated OpenAPI, Spring Security matchers, controllers, DTOs, service/query filters, caches and tests. **Starting accepted behavior**: PUBLIC has eight business GETs (eligible non-NSFW active Fiction/Film direct details plus six Reference list/detail GETs), PUBLIC Film POST denied; V1 trash PRIVATE-only. Verify indirect channels: linked People, full-text search, metadata, counts, status/error differences, autocomplete, file URLs, previews, cache reuse, browser storage assumptions. No generic profile framework by default.

Private personal/People/NSFW exposure through PUBLIC is a **functional violation**, not an optional feature toggle. A simpler design is acceptable only if negative tests prove no leakage on the selected exact routes and no bypass via neighboring APIs.

## 4. Documentation integrity and contradiction procedure

1. Identify true current canonical status for each slice/ADR/module by approved decision, independent review and actual published SHA. **A heading saying CURRENT is not authority by itself**.
2. Parse relative links including case-sensitive paths; inspect diagrams and source file pairs; classify unrenderable/absent assets. Do not rewrite all exports when no source change.
3. Compare ADR + architecture/DBML + implementation README + API and actual code **once available**. Keep provisional findings `PENDING_SOURCE_CORROBORATION` in D0 until B0 verifies behavior.
4. Distinguish typo/spelling, stale canonical contract, historical archival citation, potential design weakness, real data integrity/security issue. Severity reflects impact, not personal preference.
5. For updates, write smallest current correction and explicit dated superseding decision if required. Do not forge backdated Owner decisions/review outcomes or modify raw logs/old handbacks.

## 5. Acceptance and D1 remediation planning

D0 must return a complete tracked-file matrix, current-authority graph, links/diagram status, product simplification comparison and clearly labeled preliminary findings. D1 is a **separately Owner-approved** edit batch and must distinguish allowed current docs/derived artifacts from frozen/immutable ones. A D1 documentation fix does not authorize corresponding source change.

After Backend audit, revisit every design claim that depended on inferred implementation details. Final cross-layer consistency requires actual source + tests + schema; no independent acceptance from prose consistency alone.

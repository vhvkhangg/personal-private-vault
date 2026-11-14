# BF26-DP-RO-B — review order, evidence governance and Docs Sync

## Current checkpoints and separations

1. **Published RO-A is complete.** Source commit [`0956b4962f`](https://github.com/vhvkhangg/personal-private-vault/commit/0956b4962f529050dddf88dc7f2ec19ac571e0d0), subsequent docs-only closeout on verified [`83f87ca72a`](https://github.com/vhvkhangg/personal-private-vault/commit/83f87ca72a2571ce08e3ee99e78a147e556775ff). `ACTIVE = NO_ACTIVE_HANDOFF`; RO-A is historical and its 1/3 consumed/stopped remediation counter is not RO-B's budget.
2. **Owner-selected RO-B preparation only.** Owner had already ratified 19-key PUBLIC/18-key PRIVATE and missing/error/snapshot semantics in [RO-D04](../2026-11-13-bf26-dp-public-read-only-preparation/owner-ratification-ro-d04-d06-2026-11-13.md). Owner's new request begins a separate preparation **after** RO-A closure. It is not frozen-source/API authorization or a new executable handoff.
3. **Next independent gate:** Codex pre-handoff review of RO-B draft, source/ref hashes and existing harness. Reviewer checks that the exact chosen full-field contract, HTTP/error/null policy, legal cross-owner calls, physical transaction feasibility, tested failure semantics, precise file map and finite tests are internally consistent. Reviewer may return `CHANGES_REQUESTED`; do not auto-approve readiness or create an executable.
4. **Publication and source decision are separate:** After independently `READY_FOR_HANDOFF` preparation, Owner commits/pushes reviewed docs. Then Owner alone approves the exact per-file frozen-source/API/test exceptions; only then `$codex-create-handoff`, ACTIVE registration and Antigravity implementation can start. An additional implementation permission cannot be inferred from the review verdict alone.
5. **Implementation workflow, after authorization only:** executable handoff → `/antigravity-implement-handoff` → `/antigravity-self-review` → independent `$codex-final-review` → Owner source commit/push if accepted → actual-SHA closeout/navigation. Existing relevant maintenance policy's bounded normal command cap and remediation budget govern the active slice; no counter transferred from RO-A.

**No pre-authorization implementation prompt is supplied with this preparation.** The Owner must explicitly approve later source/API/test scope before receiving an execution prompt.

## Independent review focus / decision stop list

- Confirm **all 18 original DTO properties + one nullable name** and unchanged PRIVATE 18 on GET/POST/PUT; test actual configured JSON `null` inclusion. Check original minimal read tests changed only when their current positive expected response is deliberately superseded.
- Confirm no new registered PUBLIC business route, no People/CreatorGroup browse permission, no changing the RO-A Film POST 403. Recheck auth exceptions, dispatch ordering and ProfileRouteAuthorizationIntegrationTest eight-pair sweep.
- Prove proposed source seam's call graph: no cross-module internals, no controller-owned secondary read transaction, no cross-snapshot/full DTO result visible before eligibility. Physical PostgreSQL and transaction propagation remain acceptance requirements; static checks cannot mark them PASS.
- Validate source map is **minimal and exact**. If repository code modifications are necessary to make a safe qualified query, distinguish them explicitly as a new Owner exception, not an implicit grant. Same for adding a test file or editing Jackson configuration.
- Examine `PersonOperations.find` and `CreatorGroupOperations.find` behavior, Person Vault eligibility and difference between normal missing name vs DB exception. Verify 404-no-enrichment and safe 500 with no partial data; consider exception advice path if same-snapshot failure occurs.
- Assess generated OpenAPI `anyOf` semantics and all 19/18 required properties, nullable-name OpenAPI contract and no extra hidden DTO schema leakage or orphan refs. Keep POST Film PRIVATE-only.
- Freeze source/test hashes and list exact original vs new file names with case-sensitive paths. Confirm package-info and named-interface constraints.

## Docs Sync Check (preparation state)

| Classification | Disposition |
| --- | --- |
| **NEW** | This separate RO-B draft folder contains six Markdown documents: README, binding contract, source baseline, candidate source map, acceptance and review workflow. |
| **ALREADY_CONSISTENT** | RO-D04/D06 Owner product decisions; RO-A Owner-published closeout and final re-review; canonical ACTIVE `NO_ACTIVE_HANDOFF`; exact eight B1 GET grants and RO-A POST revocation. |
| **DEFERRED_OUT_OF_SCOPE** | Root `AGENTS.md`, root `README.md`, current Owner policy, roadmap, maintenance index, ACTIVE, old RO-A handback/self-review/executable, DPROA-L01 and unrelated LOW backlogs. Do not rewrite dated historical notices for draft preparation. |
| **NOT_VERIFIED** | Any RO-B Java source edit/build/test, actual HTTP JSON/OpenAPI generation, physical PostgreSQL atomicity, optional-name error handling, full IDE diagnostics, deployment/cutover, real Owner-data suitability, new content module read. |

### Retained roadmap constraints

DP-A/B1/W1/RO-A are historical accepted/published slices with their original evidence and counters. DPA-L01, DPB1-L02, DPB2W1-L01 and DPROA-L01 remain LOW/OPEN/BACKLOG. H1-F remains paused/FR-13 upstream blocked. Phase 17 FROZEN; Phase 18 CLOSED; S19-2 SUSPENDED/NOT_ACCEPTED; provider flags OFF/unset; RAG and indexing inactive as previously governed. DP-C, B2-W2/S/V, Recycle Bin, final non-RAG audit, Search and generic Vault remain separately gated. There is no deployment or production-data suitability claim.

**Next step:** Owner → independent Codex | submit this RO-B preparation-only documentation and cited current baseline for `$codex-pre-handoff-review` | BF26-DP-RO-B / exact 19 PUBLIC-18 PRIVATE full detail and name qualification | `PREPARATION_DRAFT / SOURCE_NOT_AUTHORIZED / ACTIVE=NO_ACTIVE_HANDOFF` | no RO-B implementation remediation consumed; takeover NOT_AUTHORIZED; review determines required corrections, then Owner publication and later separate implementation authorization.

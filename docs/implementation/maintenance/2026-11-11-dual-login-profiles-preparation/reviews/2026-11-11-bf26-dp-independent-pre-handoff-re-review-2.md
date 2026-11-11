# Independent pre-handoff re-review 2 — BF26-DP / PH-04

- **Date / reviewer:** 2026-11-11 / Codex, reviewing the supplied PH-04 correction independently of its authoring.
- **Mode / result:** focused Owner-requested maintenance preparation review / **CHANGES_REQUESTED**. **BF26DP-PH-04 MEDIUM remains OPEN** for the two finite residuals below. No new implementation or remediation round is consumed.
- **Preserved dispositions:** PH-01 HIGH, PH-02 MEDIUM and PH-03 MEDIUM remain **CLOSED at preparation level for the proposed DP-A boundary**, as independently adjudicated in [re-review 1](2026-11-11-bf26-dp-independent-pre-handoff-re-review-1.md). They are not reopened by stale navigation or this review. No B1/B2 grant or runtime acceptance follows.
- **Baseline:** local `main` HEAD, `origin/main` and live remote `refs/heads/main` all verified at `9c7e123d01237e057675d348ddb93700a4850e51`; BF26-DP is not published. Entry already had five modified tracked navigation files, untracked preparation and delivery artifacts. Those are pre-existing work, not reviewer changes.
- **Authority:** current Owner's explicit PH-04-only review instruction, [D1–D6 ratification](../owner-ratification-d1-d6-2026-11-11.md) and the corrected preparation. D1–D6 remain ratified; there is no new Owner decision superseding them. `ACTIVE = NO_ACTIVE_HANDOFF`; exact source/frozen-boundary consent and any executable handoff remain later gates.
- **Prohibitions honored:** no application source/test/migration/dependency changes, runtime/build/provider/browser/IDE tests, executable handoff, staging, commit or push. Historical reviews, decisions, receipts, H1-F/FR-13, feature flags, RAG suspension, v0.3 and all counters remain preserved.

## Evidence and bounded review scope

Reviewed the new [exact Auth/session contract](../auth-compatibility-and-session-lifecycle-2026-11-11.md), aligned Authentication design, DP-A acceptance, G-AUTH/G-SWITCH requirements, README/re-review instructions and five navigation notices against re-review 1's closure requirements. The ratified D2/D3/D4 choices are checked directly in the unchanged docket. Prior scope/architecture/necessity evidence is reused with source qualification; this is not a restarted whole-Backend audit or a fresh review of closed findings.

Independently inspected relevant existing `AuthController`, request DTOs, `AuthExceptionAdvice`, `PrivatePinService`, `SecurityConfiguration`, `SessionService`, JWT configuration, Google start/link/callback/redemption source and Auth/PIN test assertions. These establish the current compatibility baseline; they are not executions of future DP-A behavior. Existing scoped instructions and review/coverage/security/testing guidance remain applicable and unchanged from re-review 1. No new skill, agent, hook, dependency or generic harness is warranted.

Fresh [static evidence](2026-11-11-bf26-dp-re-review-2-static-evidence.json) and [entry manifest](2026-11-11-bf26-dp-re-review-2-entry.json) establish:

- All **3,344 tracked files**, including application source/tests/migrations/pins and five current notices, matched re-review 1's entry bytes at this invocation's entry. The author changed five pre-existing preparation inputs and added the exact contract/helper; those edits are reviewed, not overwritten.
- Owner decision, D1–D6 docket and dated ratification are byte-identical to re-review 1's inputs. Historical reviewer artifacts match their recorded hashes. The thirty pre-existing package/delivery files are protected by this invocation's entry manifest.
- The existing **254-declaration / 34-controller** ledger's source hashes still match. This is source-qualified reuse of the prior extraction, not a new runtime route inventory or fresh endpoint test.
- Static link validation checked **268 local targets/anchors across seventeen preparation/navigation Markdown files**, with no issues. That validates references, not the accuracy of their finding status.
- `delivery/sync-bf26-dp-ph04-navigation-once.py` is present and was inspected only. It was **not executed**, including no apply or dry-run. Its presence is not evidence that its five mutations happened.

Final preservation, whitespace and reviewer-output checks are recorded in `2026-11-11-bf26-dp-re-review-2-final-validation.json`. Runtime, SQL lock behavior, OpenAPI registration and IDE diagnostics are **NOT_VERIFIED / NOT_RUN**, as required for this preparation review.

## What the PH-04 correction now satisfies

PASS below is a preparation contract assessment only. Actual implementation evidence remains mandatory in the authorized DP-A slice.

| Requested check | Assessment / evidence and future proof |
| --- | --- |
| Exact anonymous/PUBLIC/PRIVATE surface | The new contract:13–31 enumerates method/path/proof, legacy anonymous JSON refresh/revoke, protected switches/reset/PIN, existing health/docs and conditional callback exceptions. It excludes an `/auth/**` grant and denies all Public business/unknown/async/ERROR routes. Public login's extra identifier requirement is the remaining D2 mismatch below. |
| Legacy login/refresh/revoke compatibility | Private login retains `{identifier,password}` and token envelope. Refresh/revoke retain anonymous `{refreshToken}` and do not require a bearer. Active-current revoke deliberately extends legacy row-only effect to this lineage's access authority under D3, with no global revoke. `SessionService.java:89–137` and Auth controller/DTO source independently qualify the old behavior. |
| Rolling persisted expiry | Contract:35–41 and A09 choose `access.exp=t+A`, current refresh/session expiry `t+R` on login/rotate/switch; reads do not extend authority and no first-login absolute cap or new idle timer exists. Current successful rotation computes `now+refreshTokenLifetime` at `SessionService.java:106`; configured lifetimes remain the source baseline. Future clock tests must exercise expiry boundaries and extended successors. This part of PH-04 is resolved at preparation. |
| Active versus consumed refresh revoke | Contract:18,48,51–55 and A10 define current-active revoke as lineage revocation, consumed/rotated predecessor revoke as idempotent `200 data:null` with no successor mutation; rotation rejects old/expired/unbound tokens. Rotate→old-token revoke keeps successor; revoke→rotate denies rotation. The stronger access effect is prospective, not already implemented. |
| Public reset through switched lineages | Contract:21,43,49–53 and A10 require setup AND reset, fresh Private password, distinct credentials and a persistent Public-epoch snapshot retained through Public→Private elevation. Reset invalidates every ever-Public lineage, including the reset caller if applicable; independent Private-only sessions survive. There is no new global-revoke endpoint or Private recovery subsystem. |
| Switch/refresh/reset races | Contract:48–58 specifies lineage-before-refresh locking, revalidation, atomic generation changes and epoch checks at admission/issuance boundaries. G-SWITCH adds real PostgreSQL barriers for both refresh/revoke orderings, reset↔refresh/elevation, lost replies, old generation and expiry. Reset must not permit stale epoch authority or rebind a credential verification to a newer epoch without valid reauthentication. Exact SQL/Java synchronization belongs to the later authorized handoff and must prove these invariants; a separate global lock service is unnecessary. |
| D3 same-profile rejection | The new table:19–20,29 chooses filter-level **403 with no issuance** for the wrong live profile, where D3:43 permits review of a strictly equivalent simpler alternative to same-profile 409. This reviewer accepts the state-transition simplification: both reject without generation/token mutation; distinct new operation routes can reuse their profile guard instead of a separate same-state handler branch. There is no pre-existing wire client for these unimplemented routes. The HTTP code is different, so the selected 403 must be explicit in future G-AUTH assertions; no claim of wire identity is made. This permitted simplification does not require re-ratifying D3. |
| Google OFF / ON-unmapped | Contract:24–27 and A14 distinguish absent OFF beans/matchers from ON behavior: LOGIN start unavailable before attempt/outbound activity; freshly password-verified PRIVATE LINK initiation/callback may retain immutable binding and opaque ticket; LOGIN callback fails fixed 303; all unmapped handoff issuance rejects before ticket consumption. Current Google controller/service/store independently show the separate LINK path and the redemption transaction that needs this future guard. No provider is activated and no browser proof is claimed. |
| Aligned finite harness | A09/A10/A12–A15/A20/A21 and the new G-AUTH/G-SWITCH bullets cover real signed tokens with persisted authority, both revoke orders, epoch-reset scope, rolling expiry, Google state/ticket negatives and D3 admission limits. Existing MockMvc/PostgreSQL/clock/barrier/fake-provider infrastructure suffices. No runtime proof is borrowed from historical counts or an unprofiled fixture. |

**PIN clarification for the final exact table (nonblocking wording):** contract:22's “invalid PIN follows existing 401/422” must not be implemented as changing a well-formed incorrect verify PIN to 401. Existing `PrivatePinService.verifyPin()` returns false, and `AuthWebIntegrationTest.java:131–137` asserts **200 with `verified:false`**. Format errors remain 422 and wrong current PIN on change remains 401. The row already promises the existing boolean response; make that negative explicit while doing the bounded correction, without changing source or adding a new PIN behavior. This does not reopen PH-01–03 or create another blocking finding.

## BF26DP-PH-04 — two remaining concrete residuals

**MEDIUM / OPEN / blocking READY_FOR_HANDOFF.** These are preparation contract/synchronization defects, not a current singleton exploit. The finding stays one stable ID; it consumes no implementation remediation allowance.

### A. Public login now requires an identifier contrary to ratified D2

**Evidence:** unchanged `owner-proposed-d1-d6-decision-docket-2026-11-11.md:35` selects a fixed Public identity with **no username required**. The exact correction's method table:16 instead requires `{identifier,password,profile:"PUBLIC"}`, says the identifier resolves the Owner, and rejects unknown identifiers. Its lifecycle login row:47 also says to verify Owner identifier for Public login. The author does not record a new Owner decision changing D2.

**Consequence:** a request with only the ratified fixed Public profile and correct configured Public password would fail the new “exact” contract because a newly required Owner identifier is missing. This is a concrete accepted-input/requirement mismatch; adding an identity lookup for one fixed Public credential is not needed to preserve singleton ownership. Keeping username/email on **legacy Private login** does not authorize requiring it on the new Public route.

**Smallest correction:** retain legacy Private `{identifier,password}` unchanged; make Public login accept the selected fixed `profile=PUBLIC` plus its password **without a required Owner identifier**, and align the Public branch of the lifecycle row, A03/A12/A21 and G-AUTH. An optional ignored identifier must not become an undocumented authentication factor. No new product approval is needed to restore ratified D2; only an actual proposed deviation would need separate Owner consent.

**Preparation closure proof:** include a concrete table/fixture expectation that `POST /api/v1/auth/profiles/login` with Public profile and the correct configured Public password, **no identifier**, succeeds; unconfigured/wrong password gives the same 401; a PRIVATE branch remains rejected and Private legacy login retains its identifier. These are future runtime assertions, not permission to execute them now.

### B. The five required current notices were not synchronized to re-review 1

**Evidence:** the exact same pre-re-review-1 bytes remain in all five targets, with no link to re-review 1 in their current blocks:

| Current target | Observed stale status |
| --- | --- |
| `AGENTS.md:1` | Initial PH-01–04 review presented as awaiting separate independent re-review; no PH-01–03 closure or current PH-04 correction |
| `docs/workflow/current-owner-delivery-policy.md:3` | Same initial-review-only current notice |
| `docs/roadmap.md:1` | Same initial-review-only current notice |
| `docs/implementation/maintenance/README.md:39` | Explicit `PH-01 HIGH + PH-02–04 MEDIUM OPEN`; routes to the initial review |
| `docs/implementation/handoffs/ACTIVE.md:5,14` | Explicit all-four-open authority status and next step still adjudicating PH-01–04 |

The new `pre-handoff-review-instructions.md:7` says the correction also corrects current navigation, but the worktree does not show those updates. The earlier synchronization record is valid history of its earlier five edits; it is not evidence that the new PH-04 helper ran. This is not a link failure: source hashes and exact status text establish the discrepancy.

**Consequence:** current routing contradicts the independent PH-01–03 CLOSED dispositions and can send the next agent back through closed findings. Re-review 1 explicitly required this bounded Docs Sync with the PH-04 correction; the Owner also asked for these five notices to be checked. Their general no-handoff/Frontend/RAG restrictions remain correct.

**Smallest correction:** ChatGPT updates only the five current status/planning blocks to reference re-review 1 and this later re-review: PH-01–03 CLOSED at DP-A preparation, PH-04 MEDIUM OPEN pending the residual correction/re-review. Preserve `NO_ACTIVE_HANDOFF`, historical regions, counters, S5 publication, paused H1-F/FR-13 and frozen/provider/RAG boundaries. Supply actual changed files and static validation; a supplied helper is not an applied outcome. This reviewer does not execute it or repair current navigation under review-only authority.

**Preparation closure proof:** inspect the five actual updated current blocks, links, LF/whitespace and scoped diff; show historical regions/source/index unchanged. No publication, new phase gate or extra runtime check is required for this correction.

## Proportionality, finite next work and non-goals

The [re-review 1 necessity/effort matrix](2026-11-11-bf26-dp-independent-pre-handoff-re-review-1.md#required-capabilities-necessity-and-effort) remains applicable with unchanged source. This focused delta adds no new feature or subsystem:

| Capability / mechanism | Disposition and simplest mechanism | Qualitative implementation / verification burden and gate |
| --- | --- | --- |
| One Owner, legacy Private login/PIN | KEEP / REUSE current identity, hashing, DTO and retained PIN controls | Low preservation burden; existing regressions extended to Private profile; exact source authority later |
| Fixed Public login | SIMPLIFY to D2 fixed profile/password; remove new required identifier | Small docs correction now; medium future auth implementation/negative proof; no repeat ratification |
| Persisted sessions, rolling expiry and generation switch | KEEP one local authority/transaction model; REUSE configured TTLs | Medium/high future deterministic HTTP/PostgreSQL proof; no idle service or absolute cap |
| Current-token revoke / Public reset epoch | KEEP explicit state/epoch fence and scoped reset; REUSE current rotation/response family | Medium future clock/race/signed-token proof, no recovery/global-revoke platform |
| Google linking versus issuance | KEEP immutable binding/OIDC proof, DEFER PPV issuance while unmapped | Focused fake-provider regression later; flags remain OFF; no real provider/browser work |
| Same-A Portability/migration safety | KEEP PH-03-accepted co-delivery and backup distinction | Real PostgreSQL/export proof later; closed preparation finding is preserved |
| Data grants, BFF, Recycle Bin/RAG/audit | DEFER all selected B1/B2 and separately gated work | No current grant, test activation or consumer acceptance |
| New agent/harness/dependency/hooks/benchmarks | REMOVE from present need; REUSE existing backend implementer and testing/review tooling | No install or framework project; no numerical test/latency quota |

The first source slice remains **DP-A only**: Authentication authority/migration plus Public business default DENY, same-A Portability exclusions/tests, narrow persisted-token fixtures and approved prospective ADR/DBML/API deltas. After the two finite documentation residuals, a separate focused independent re-review may determine readiness. Only then may the Owner publish preparation, separately approve exact frozen/source scope and invoke `$codex-create-handoff`. Antigravity implementation/self-review and independent Codex slice acceptance follow later. Existing three-round policy is preserved; this is preparation re-review 2, **not implementation remediation round 2**.

Future tests use existing JUnit/MockMvc/PostgreSQL/Modulith/OpenAPI/fake-provider/clock barriers. Long authorized commands retain the implemented bounded runner (300 seconds ordinary; up to 2,100 only for separately approved eligible full suites), fresh receipts and owned cleanup. No suite, provider, browser or IDE execution is needed or permitted to close this docs review. Runtime privacy/correctness evidence stays mandatory at the authorized implementation gate.

## Coverage ledger — focused delta and qualified reuse

All 29 considerations are accounted for; PASS means preparation assessment, not executed runtime acceptance. Unchanged detailed evidence is reused from re-review 1 with matching tracked/source hashes.

| # / consideration | Disposition |
| --- | --- |
| 1 Intent/non-goals | FINDING PH-04/A Public identifier conflicts with D2; other ratified boundaries preserved |
| 2 Traceability | FINDING PH-04/A,B; A09/A10/A12–A15/A20/A21 and G-AUTH/G-SWITCH otherwise aligned |
| 3 Architecture/ADRs | PASS prospective auth/schema/API exception gate remains separate; frozen files unchanged |
| 4 Ownership/dependencies | PASS existing Authentication/Portability ownership and no reverse/internal edge; reused |
| 5 Folder/package structure | PASS unchanged relevant structure/inventory from re-review 1; new contract in owning maintenance folder; no source package changes |
| 6 Shared contracts/exceptions | PASS reuse ApiResponse/owner advice, 303 exception; PIN wording clarification above |
| 7 SOLID/cohesion | PASS local authority and scoped credential responsibility; implementation design N/A |
| 8 DRY/KISS/YAGNI | FINDING PH-04/A unnecessary required identifier; otherwise reuse/minimal state |
| 9 Patterns | PASS no new policy/adapter/session platform needed; detailed source pattern changes N/A |
| 10 Domain/state | PASS rolling expiry/reset/revoke/switch contracts now explicit; implementation NOT_VERIFIED |
| 11 API/wire | FINDING PH-04/A; legacy JSON shape preserved, selected same-profile 403 qualified above |
| 12 Input validation | FINDING PH-04/A allowed input mismatch; redaction/structural error obligations retained |
| 13 Auth/privacy | PASS explicit method/profile/default-deny/epoch requirements; enforcement NOT_VERIFIED until implementation |
| 14 Secrets/backup/retention | PASS hashes, no seed, epoch invalidation and preserved PH-03 backup/export boundaries |
| 15 Transactions/races | PASS finite lock/recheck/epoch/barrier requirements; actual SQL/runtime NOT_VERIFIED |
| 16 Failure/recovery | PASS expired/old token, DB failure, lost reply, no permissive legacy fallback and restore rules |
| 17 Database | PASS no new SQL in this correction; additive post-V28 and exact approved DDL remain later gates |
| 18 Complexity/resources | PASS bounded local authority checks, no network/stream lifetime locks; overhead NOT_VERIFIED |
| 19 Retrieval/index quality | N/A no RAG/index/recommendation change; Search grants deferred |
| 20 OCR/document fidelity | N/A no document/parser/OCR capability |
| 21 Security negatives | PASS exact future real-token/Google/race cases; Public no-identifier assertion needs PH-04/A alignment |
| 22 Test quality | PASS existing signed/persisted fixture/barrier infrastructure reused; tests NOT_RUN |
| 23 Performance benchmarks | N/A new benchmark/latency gate; performance NOT_VERIFIED |
| 24 Evaluation science | N/A no model/corpus/judge changes |
| 25 Dependencies/lifecycle | PASS none changed; current-version audit N/A, FR-13 preserved |
| 26 Build/tooling/static diagnostics | NOT_VERIFIED build/IDE; static hashes/links/whitespace evidenced; no IDE-clean claim |
| 27 Logging/observability | PASS no secret payloads and existing redacted receipts; runtime logging NOT_VERIFIED |
| 28 Docs/governance | FINDING PH-04/B five unapplied current notices; decisions/history preserved |
| 29 Hygiene/delivery | PASS fresh reviewer artifacts, source/input/index preservation, limited README reviewer-status update |

## Docs Sync Check and stop

- **UPDATED:** this new independent review, fresh entry/static/final evidence and only the canonical BF26-DP README's reviewer-status/next-action fields. No application or author contract is repaired in this invocation.
- **ALREADY_CONSISTENT:** D1–D6 remain ratified; PH-01–03 closure remains controlling; S5 publication verified, `NO_ACTIVE_HANDOFF`, H1-F/FR-13/RAG/flags/counters preserved. Historical review and synchronization artifacts remain byte-identical.
- **REQUIRED CORRECTION:** PH-04/A Public-login inputs and its acceptance/harness references; PH-04/B exact five current notices above. Route to ChatGPT, then independent focused Codex re-review. No repeated D1–D6 question is needed.
- **DEFERRED_OUT_OF_SCOPE:** old general/scoped instruction notices, later B1/B2 implementation contracts, full package-tree cleanup, final publication-order prose outside DP-A, H1-F dependency/UI, Recycle Bin, RAG and final audit. None reopens PH-01–03.
- **NOT_VERIFIED:** runtime/HTTP/OpenAPI/DB/migration/lock/provider/browser/performance/IDE proof. No future acceptance is claimed as an executed PASS.

**Final result: CHANGES_REQUESTED.** PH-04 is narrowed to the concrete D2 input mismatch and unapplied required navigation synchronization. Rolling expiry, reset across switched lineages and current-versus-consumed revoke are sufficiently specified at preparation level. No READY_FOR_HANDOFF, ready commit message, publication, executable handoff or source authority is issued. Stop after this independent review.

**Next step:** ChatGPT | bounded PH-04/A,B docs correction, then separate Codex `$codex-pre-handoff-review` | BF26-DP exact Auth contract/A acceptance/G-AUTH and five current notices; this re-review 2 | restore ratified no-identifier Public login and actual scoped Docs Sync before readiness; Owner publication/source consent/handoff remain later | PH-01–03 stay CLOSED; no implementation/remediation/takeover round consumed.

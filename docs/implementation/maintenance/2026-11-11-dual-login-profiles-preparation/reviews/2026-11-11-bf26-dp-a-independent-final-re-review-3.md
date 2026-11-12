# Independent implementation re-review 3 — BF26-DP-A

- **Date / reviewer / mode:** 2026-11-11 / independent Codex / `SLICE`, authorized implementation maintenance.
- **HEAD / entry:** local `main` `536a7aab8f48c2a182a8c5b88c1d248fdb29e283`, empty index, 71 entry delivery paths; ACTIVE `IMPLEMENTED_AWAITING_CODEX_REVIEW`, remediation 3/3 submitted. No production or delivered test edits by reviewer; no publication.
- **Authority / inputs:** [executable handoff](../executable-handoff-dp-a-2026-11-11.md), recorded bounded DP-A Owner authorization/D1–D6, [re-review 2](2026-11-11-bf26-dp-a-independent-final-re-review-2.md), [R3 handback/self-review](../implementation-handback-dp-a-2026-11-11.md), current Owner policy and Docs Sync contract. Preparation history is not runtime acceptance; current explicit DP-A authority governs this review.
- **Outcome:** **CHANGES_REQUESTED / STOPPED / OWNER_DECISION_REQUIRED / NOT_INDEPENDENTLY_ACCEPTED**. **FR-DPA-04 CLOSED**; FR-DPA-01/02/03/05/08 closures retained. **FR-DPA-06/07/09 MEDIUM / OPEN**, mandatory NOT_VERIFIED cases and DPA-L01 LOW backlog remain. No commit message.
- **Counters / stop:** implementation remediation **3/3 exhausted, independently blocked / STOPPED**; takeover **0 / NOT_AUTHORIZED**. No fourth ordinary Antigravity correction/test loop, automatic Codex implementation or new handoff. Owner must dispose of the remaining bounded findings and two exact-map exceptions.

## Source applicability and review method

Current HEAD, root AGENTS, ACTIVE, exact handoff, R3 submission and parent/source tests were inspected. The deferred preparation notice in root AGENTS does not supersede the later recorded implementation authorization. Compared current contents to the R2 source snapshot: five backend files and three documentation/status files changed; recursively inventoried affected Auth, snapshot adapter, web/Drive/support test subtrees. Existing interfaces/enums/exceptions/commands/views remain coherently owned; no new package/module/dependency or root transport abstraction was added. Root `ApiResponse`/`ApiResponses` remain canonical under ADR-0016.

Fresh evidence directory **R**: `logs/bf26-dp-a/codex-r3-1794406129132-7c51cc/`. `R/source-sha256.txt` pins all 71 entry paths; `R/recursive-inventory.txt` records inspected folders; `R/frozen-migrations.json` confirms all 28 frozen canonical migration blobs equal HEAD. Existing checkout line-ending qualifications remain unchanged. No delivered backend source/test drift during review. All prior reviews and receipts remain preserved.

## Finding adjudication

**Main** = `backend/src/main/java/com/vhvkhangg/personalprivatevault/`; **test** = `backend/src/test/java/com/vhvkhangg/personalprivatevault/`.

| Finding | Disposition | Evidence / remaining bounded work |
| --- | --- | --- |
| FR-DPA-04 | MEDIUM / CLOSED | **Main** `authentication/internal/infrastructure/security/SecurityConfiguration.java` now rejects floating decoded types and inspects the signed raw payload's `generation` JSON node for integral long representation. Fresh real signed JWT + persisted generation-1 HTTP probe changes the exact prior `1.0000000000000001` counterexample from **404 after admission to 401**. `1.5`, missing expiry and overflow cases also reject; ordinary genuine PRIVATE/PUBLIC token flows remain valid in fresh G2. Source review confirms wire integers, positive bound and long overflow checks. The delivered `createCustomToken` helper uses a nonmatching issuer/random absent session, so its negatives alone would not establish this closure; the reviewer probe uses the genuine issued lineage/issuer and isolates the claim. Correct that test limitation within FR-DPA-06. |
| FR-DPA-01/02/03/05/08 | CLOSED, retained | Fresh observed-lock/controlled-clock probe again rejects stale waiting reset (401, epoch 2) and expired rotation; strict DTO inputs remain 400. Exact documented invalidation SQL, read from the current runbook, succeeds on configured/unconfigured disposable state; genuine old access/refresh fail and fresh PRIVATE login works. Previously accepted positive fixtures, root envelope and narrow rollback guidance have no contrary drift. Actual backup/restore remains FR-DPA-07. |
| FR-DPA-06 | MEDIUM / OPEN / mandatory NOT_VERIFIED | **Test** `authentication/ProfileRouteAuthorizationIntegrationTest.java:41–49`: five requested table names do not match the physical model (`vault_records`, `people`, `accounts`, `finance_accounts`, `journals`; actual owners include `vault_entries`, `persons`, `external_accounts`, `wallets`, `diary_entries`). Exceptions are silently ignored, omitting those checks; count equality alone also cannot detect updates to existing rows. The default-OFF `localOnDriveAndGoogleRoutesDeniedToPublicToken():234` does not enable providers or assert registered ON handlers; several tested Drive paths are unregistered, so 403 only proves catchall denial. ASYNC now has a case, but valid PUBLIC accepts either **401 or 403** instead of the selected 403; stale PRIVATE redispatch sends the old bearer on another request, not a reused propagated SecurityContext. `ProfileSessionConcurrencyIntegrationTest.issuanceRollbackOnFailurePreservesPreviousAuthority():733–767` performs a GET and generation read, **no switch, injected failure or rollback**. `admittedRequestVsPostCommitBoundaryAndLostReplyRecovery():668` credits sequential post-commit denial/fresh credential recovery but has no blocked already-admitted GET/write/stream overlapping commit. `concurrentResetVsRefreshSerializesCleanly():587` is a simultaneous-start run, not forced lock/commit serializations. Required controlled rolling lifetime, full reset/refresh/switch orderings, real issuance/commit-constraint rollback and authority-read failure proofs remain missing. Owner-authorized correction must replace labels with the original finite behavioral proofs, real handler/provider/value controls and DPA→method→receipt mapping. No additional product scope is required. |
| FR-DPA-07 | MEDIUM / OPEN / mandatory NOT_VERIFIED | Actual isolated **Flyway V28→V29** execution is now present at **test** `authentication/ProfileMigrationIntegrationTest.java:268`, with authentic token hashes, preserved Owner values, active legacy revocation and PUBLIC epoch 0. Credit this improvement. The fixture still omits populated domain/settings/FK values and a replacement chain required by DPA-01; synthetic password/PIN hash strings do not prove retained credential login. `disposablePostgresqlBackupRestoreAndKeyRotation():373` **creates a fresh database, migrates it and manually inserts rows**, then checks in-place revocation. It does not take/restore a backup, rotate any signing key, admit/reject restored access JWTs against restored state, or perform fresh PRIVATE login there. Its descriptive name and comments do not establish those operations. The separate main-fixture invalidation test now has real hashes and fresh login and is credited as in-place evidence. Remaining correction: complete populated migration preservation and an actual isolated backup→restore→invalidation→key rotation→old-token denial/fresh-login chain using existing tooling, without Owner/live data. |
| FR-DPA-09 | MEDIUM / OPEN / BLOCKED_OWNER_DECISION | The retained **main** `OpenApiConfiguration.java` and **test** `drive/DriveApiInventoryIntegrationTest.java` edits still lack a bounded Owner disposition against the exact source map. The necessity statement is available and the unnecessary production test helper remains removed. Owner may approve/decline these specific retained exceptions; this review cannot grant or silently amend source authority. |
| DPA-L01 | LOW / OPEN backlog, partly corrected | Package-tree method/link fixes retained. Current R3 G5 batch-3 table claims 3 tests but its receipt reports **5**; actual final batches do total 283. Complete attempt/source-qualified reuse manifests and accurate D4 cancellation wording remain documentation follow-up, not a reason for a fourth round or scope expansion. |

### Minimum remaining Owner disposition

The three ordinary correction/re-review rounds are exhausted. The concrete reviewable remainder is:

1. Decide retention of exactly `OpenApiConfiguration.java` and `drive/DriveApiInventoryIntegrationTest.java` under FR-DPA-09.
2. If continuation is selected, separately authorize a **bounded Codex takeover for FR-DPA-06/07 evidence/test corrections** in the existing four Profile test files and originally permitted fixtures/docs, with the existing DPA/G1–G5 criteria and a separate independent subsequent reviewer. No new production defect is established by the remaining findings; production changes beyond the existing authority would need their own exact scope.

This is a proposed disposition, not takeover authorization or an executable new handoff. Owner may instead stop/defer or expressly revise the acceptance contract; an unrun requirement cannot be silently marked PASS.

## Acceptance matrix

| DPA | Result / evidence limit |
| --- | --- |
| 01 | NOT_VERIFIED — actual Owner/token V28→V29 upgrade now passes; populated domain/settings/FK/replacement-chain/credential compatibility proof remains incomplete. |
| 02 | PASS — genuine PUBLIC and legacy PRIVATE flows plus corrected new DTO validation, retained fresh G2/probe proof. |
| 03 | NOT_VERIFIED — corrected stale reset and selected credential proofs pass; full required no-mutation/redaction controls remain incomplete. |
| 04 | NOT_VERIFIED overall — generation defect CLOSED with isolated actual admission proof; complete remaining malformed/persisted-state/DB-lookup-failure matrix is not supplied. |
| 05–07 | NOT_VERIFIED — basic switching, epoch and expiry defects fixed; complete negative/no-mutation, controlled rolling expiry and independent-session/reset matrix remain incomplete. |
| 08 | PASS for unchanged current/predecessor rotate↔revoke semantics, qualified by previously reviewed real PostgreSQL cases and supplied G1 regressions. |
| 09 | NOT_VERIFIED — genuine stale reset barrier retained; simultaneous-start reset/refresh does not force the remaining approved serializations. |
| 10 | NOT_VERIFIED — no admitted GET/write/stream overlap or propagated stale-context redispatch proof. |
| 11 | NOT_VERIFIED — sequential discarded-replacement recovery is credited; named rollback test has no issuance failure, commit failure or rollback; authority-read failure proof absent. |
| 12–13 | NOT_VERIFIED — both PUBLIC origins/default OFF sweep/method/header/export controls present; ON handler registration, correct domain value/provider non-admission and exact ASYNC controls incomplete. |
| 14–15 | NOT_VERIFIED — supplied revised local Google/native/PIN/bootstrap/redaction regressions pass; prior specific pre-cutover LOGIN/direct-store/profile matrix and complete new-proof/PRIVATE-service authority cases remain incomplete. No demonstrated PIN HTTP bypass is asserted. |
| 16 | NOT_VERIFIED — exact in-place runbook and ZIP exclusions work; no actual backup/restore/key rotation and restored-bound authority acceptance proof. |
| 17 | NOT_VERIFIED — supplied architecture/envelope/route inventory regressions pass; strict new schema/error/profile/D4 OpenAPI proof remains incomplete. |

## Fresh verification and receipt qualification

Commands from repository root used `python scripts/run-bounded-command.py --profile normal --timeout-seconds 300 --receipt R/<receipt> -- <child>`. No source implementation, provider activation, QA/model inference or new installation was performed.

| Fresh receipt | Exact child / actual result |
| --- | --- |
| `R/g2.json` | `mvn.cmd -ntp -f backend/pom.xml -DargLine=--enable-native-access=ALL-UNNAMED -Dtest=ProfileSessionIntegrationTest,ProfileSessionConcurrencyIntegrationTest,ProfileRouteAuthorizationIntegrationTest,ProfileMigrationIntegrationTest test`; **exit 0, 31 tests, zero failures/errors/skips**, **70.110s**, complete capture, cleanup not needed. |
| `R/probe.json` | `java @logs/bf26-dp-a/codex-r3-1794406129132-7c51cc/probe.args`; **exit 0, 35.594s**, complete capture, cleanup not needed. Adapted already-working reviewer probe under fresh R, actual Spring/filter chain and disposable PostgreSQL, legitimate credential-issued authority, controlled service Clock and observed lock wait. `R/probe-source-sha256.txt` pins its source. Diagnostic completion is not aggregate acceptance. |

Fresh probe results: precise fractional generation **401** (prior actual counterexample closed); `1.5`, integer overflow and missing expiry **401**; overflow floating decoder forms **rejected**; null profile/numeric password/credential password/boolean elevation proof **400**; stale waiting reset **401**, epoch **2**; expired rotation rejected; exact document SQL commits, active sessions **0**, genuine old access/refresh **401**, fresh PRIVATE login succeeds. Unconfigured execution retains epoch **0**, old refresh **401**, fresh PRIVATE login succeeds. This is in-place synthetic evidence, not actual backup/restore/key rotation. No provider/browser/RAG invocation by this probe.

Supplied final R3 receipts and captured Maven summaries were independently inspected as implementer evidence:

| Receipt under `logs/bf26-dp-a/` | Tests | Runner seconds |
| --- | ---: | ---: |
| `r3-g1-auth.json` | 94 | 45.578 |
| `r3-g2-profiles-5.json` | 31 | 61.594 |
| `r3-g3-google.json` | 50 | 46.578 |
| `r3-g3-browser.json` | 10 | 54.110 |
| `r3-g4-contracts.json` | 69 | 58.593 |
| `r3-g5-batch1.json` | 3 | 27.546 |
| `r3-g5-batch2.json` | 3 | 28.063 |
| `r3-g5-batch3.json` | 5 | 28.562 |
| `r3-g5-batch4.json` | 18 | 30.078 |

Final named batches total **283 tests**, zero failures/errors/skips, **380.702s bounded-runner time**. Earlier `r3-g2-profiles.json`, `r3-g2-profiles-2.json`, `r3-g2-profiles-3.json`, `r3-g2-profiles-4.json` are completed COMMAND_FAILED/exit 1/cleanup not needed and remain preserved. Final-pass counts do not erase earlier attempts or missing mandatory acceptance.

R3 G5 is **13 representative classes / 29 tests**, not the handoff's complete G5 selector. R2 full G5 receipts remain available for unchanged business paths; do not relabel them as fresh R3 execution. Delivery still needs an explicit source-applicable class→receipt reuse manifest for omitted classes, or focused re-verification where the new JWT validator invalidates prior evidence; no unrelated full-suite run is imposed. Supplied receipts alone do not reconstruct complete historical working-tree bytes. Current reviewer hashes/fresh executions qualify this adjudication.

Fresh Maven succeeds with the known metadata/dynamic-agent warning categories; no warning suppression, new IDE inspection or warning-free claim. No new hook/config/trust failure encountered. The prior optional inline-command rejection and all historical failed/timeout outcomes remain preserved.

## Shared coverage and scoped Docs Sync Check

All 29 dimensions remain considered proportionately. **1–2 FINDING:** mandatory traceability/acceptance gaps; **3–6 PASS** module ownership, recursive structure, existing descriptors, root `ApiResponse`/ADR-0016 and owner-local enum/entities/exceptions/commands/views, with exact-map **FINDING** FR-DPA-09; **7–9 PASS** concrete cohesive services/no speculative architecture; **10–17 FINDING/NOT_VERIFIED** remaining authorization/transaction/recovery evidence, corrected generation and in-place SQL subcases PASS; **18 PASS** indexed bounded admission and fixed-size state transitions, no speculative benchmark requirement; **19–20/23–24 N/A** unchanged RAG/document/evaluation/benchmark capabilities; **21–22 FINDING** misleading test coverage and absent isolation controls; **25 PASS** unchanged dependencies/platform; **26 PASS** fresh focused Maven and qualified supplied architecture, IDE NOT_VERIFIED; **27 PASS** inspected production privacy/logging shape, complete new-proof negative coverage NOT_VERIFIED; **28–29 FINDING** in-scope authority/evidence/status discrepancies, preserved index/HEAD/history and no publishing.

- **ALREADY_CONSISTENT:** one Owner/two fixed profiles, auth-only PUBLIC/default denial, corrected numeric defect, preserved prior closures, V1–V28 canonical contents, root transport/module boundaries, defaults OFF and separate Frontend/RAG/v0.3/counter boundaries.
- **UPDATED:** this separate review, ACTIVE current disposition/stop/Owner next action, executable current result fields and independent disposition addendum to the R3 handback. Submitted implementer claims remain distinguished from independent results.
- **NOT_VERIFIED:** mandatory cases above, complete source-qualified G5 reuse manifest and genuine backup/restore/key rotation; test names/comments are not runtime evidence.
- **BLOCKED_OWNER_DECISION:** FR-DPA-09 and any further FR-DPA-06/07 continuation after 3/3 exhausted. No automatic fourth Antigravity round or Codex takeover.
- **DEFERRED_OUT_OF_SCOPE:** the same current-navigation/preparation-README changes assigned to ChatGPT/Owner and unrelated historical drift. Only current R3-changed/invalidated docs and direct references were rechecked; no cleanup B. Historical reviews/failed evidence/counters, Frontend pause/FR-13 and RAG suspension/containment remain intact.

**Next step:** Owner | decide a separately bounded `$codex-implementation-takeover` for FR-DPA-06/07 and disposition of FR-DPA-09's two exact-map exceptions | BF26-DP-A, this review and [ACTIVE](../../../handoffs/ACTIVE.md) | CHANGES_REQUESTED; ordinary Antigravity work STOPPED; any Codex corrective delivery requires a separate independent reviewer | remediation 3/3 exhausted; takeover 0 / NOT_AUTHORIZED; historical counters unchanged.

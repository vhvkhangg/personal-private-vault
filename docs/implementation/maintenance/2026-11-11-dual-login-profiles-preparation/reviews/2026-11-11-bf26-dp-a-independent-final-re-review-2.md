# Independent implementation re-review 2 — BF26-DP-A

- **Date / reviewer / mode:** 2026-11-11 / independent Codex / `SLICE`, authorized implementation maintenance.
- **Baseline:** local `main` HEAD `536a7aab8f48c2a182a8c5b88c1d248fdb29e283`, empty index, 70 entry delivery paths. No production or delivered test changes by this reviewer; no publication.
- **Authority / inputs:** [executable DP-A contract](../executable-handoff-dp-a-2026-11-11.md), its recorded bounded Owner authorization and D1–D6, [re-review 1](2026-11-11-bf26-dp-a-independent-final-re-review-1.md), [R2 handback/self-review](../implementation-handback-dp-a-2026-11-11.md), ACTIVE `IMPLEMENTED_AWAITING_CODEX_REVIEW`. The exact source map and mandatory DPA rows remain binding; the implementer has not independently accepted its own corrections.
- **Outcome:** **CHANGES_REQUESTED / NOT_INDEPENDENTLY_ACCEPTED**. **FR-DPA-03 and FR-DPA-08 CLOSED** at the corrected input-validation/documented-invalidation scope; FR-DPA-01/02/05 remain CLOSED. **FR-DPA-04/06/07/09 remain MEDIUM / OPEN**, with mandatory NOT_VERIFIED cases. DPA-L01 remains LOW backlog, partly corrected.
- **Counters:** remediation **2/3 consumed**, independently re-reviewed and still blocked; next correction plus independent review **3/3**, the final ordinary round. Takeover **0 / NOT_AUTHORIZED**; no automatic takeover. All preparation/frontend/other counters unchanged.

## Source and review scope

Compared current source against the R1 entry SHA-256 inventory, inspected all seven changed backend files, six changed documentation/status files and the parent review. Recursively inventoried affected Auth, snapshot adapter and test/fixture subtrees. Reviewed actual schema/runbook/test statements rather than handback labels. The original handoff source map was not amended by the implementer's necessity explanation or by this review.

Fresh reviewer directory **R**: `logs/bf26-dp-a/codex-r2-1794402747176-93c642/`. `R/source-sha256.txt` pins 70 entry paths; `R/recursive-inventory.txt` records the inspected subtrees; `R/frozen-migrations.json` verifies all 28 existing migration canonical Git blobs against HEAD. Existing checkout line-ending qualifications remain unchanged. Delivered backend source/tests did not change during review. Prior reviews and raw receipts remain immutable.

## Finding disposition

**Main** = `backend/src/main/java/com/vhvkhangg/personalprivatevault/`; **test** = `backend/src/test/java/com/vhvkhangg/personalprivatevault/`.

| ID | Disposition | Evidence / minimum remaining correction |
| --- | --- | --- |
| FR-DPA-01 / FR-DPA-02 / FR-DPA-05 | CLOSED, retained | No contrary drift in the epoch fence, post-lock clock or positive credential-origin fixtures. Fresh real barrier/clock probe again rejects the invalidated waiting reset (401, epoch stays 2) and post-expiry rotation; G2 uses legitimate PUBLIC login and downgrade origins. |
| FR-DPA-03 | MEDIUM / CLOSED | **Main** `authentication/internal/web/controller/AuthController.java` now disables textual coercion and rejects non-text/null AST fields before binding all three new DTOs; duplicate/unknown/trailing handling and redaction remain local. Fresh HTTP probe: numeric login password **400**, numeric public-credential password **400**, boolean elevation proof **400**, null profile **400**; delivered G2 also covers boolean/null/numeric profile/duplicate cases. Original wire conformance defect is corrected without changing the legacy/global mapper. |
| FR-DPA-04 | MEDIUM / OPEN, partially corrected | **Main** `authentication/internal/infrastructure/security/SecurityConfiguration.java:93,112–160` now rejects the previously observed 2^63 overflow. However, validation still runs after Nimbus has converted a fractional JSON claim to a rounded Double. Fresh signed-wire probe confirms payload **`generation:1.0000000000000001`**, accepted decoded value **Double 1.0**, although `parseGeneration` rejects the original exact BigDecimal. Fresh actual HTTP/filter/persisted generation-1 probe admits that token and reaches PRIVATE unknown-route **404**. Preserve exact numeric meaning before validation or reject floating generation representations before relying on their rounded value; use the canonical integral representation produced by the issuer and test the actual signed JWT decoder/admission path. A direct helper test on the original BigDecimal alone is insufficient. Signature verification still applies; no attacker signature forgery or business-data disclosure is claimed. |
| FR-DPA-06 | MEDIUM / OPEN / NOT_VERIFIED | **Test** `authentication/ProfileRouteAuthorizationIntegrationTest.java:34–103` now iterates declared methods and checks `Content-Disposition`; the only before/after data check counts **`app_users`** rows, which cannot detect a Film/Finance/Vault mutation or provider invocation. The sweep remains default-OFF only. `errorAndAsyncDispatcherRequiresPrivate():163–180` sends **ERROR twice**, no ASYNC and no propagated stale PRIVATE context after switch. New source matchers are not runtime proof of D3. Missing: exact local-ON route/profile matrix and handler/provider non-admission controls; admitted GET/write/stream versus post-commit/stale-context ASYNC/ERROR boundary; full controlled rolling lifetime and reset↔refresh/elevation/downgrade/switch serializations; issuance rollback/deferred-commit failure/lost reply/authority-read failure. Complete the original finite DPA acceptance with bounded barriers and a DPA→exact test method→receipt manifest. Preserve the valid new reset barrier and export controls; no unrelated suite/platform expansion. |
| FR-DPA-07 | MEDIUM / OPEN / NOT_VERIFIED | **Test** `authentication/ProfileMigrationIntegrationTest.java:238–265`, named `syntheticV28ToV29CutoverPreservesDataAndRevokesLegacyTokens`, never initializes V28 or executes V29 over populated V28. It inserts an **already-revoked unbound row into an already-V29 database**, then checks the manually inserted Owner count. Its refresh negative presents raw text equal to stored `token_hash`; the endpoint hashes the presented value, so it looks up a different hash and would return 401 even without revocation. The recovery test has the same hash/raw mismatch at lines 162/220 and no fresh-login assertion despite its display name. The DBML composite/deferred FK details are now corrected. Remaining: actual populated V28→V29 Owner/domain/PIN/settings/legacy-chain/catalog/checksum proof and actual disposable backup/isolated restore/key rotation/old-token denial/fresh PRIVATE recovery using real credential-origin token pairs. An in-place V29 SQL exercise is not backup/restore evidence. |
| FR-DPA-08 | MEDIUM / CLOSED for documented SQL and rollback defects | The [runbook](../dp-a-cutover-and-recovery-2026-11-11.md):51 now uses the actual `password_hash` column and retains forward-compatible recovery. The fresh reviewer probe **reads and executes the SQL block directly from this document**, with genuine application-issued authority. Configured state: transaction commits, zero active sessions, old access/refresh each **401**, fresh PRIVATE login succeeds. Unconfigured state: epoch remains **0**, old refresh **401**, fresh PRIVATE login succeeds. This closes the original invalidation-script defect, not the separate actual backup/restore/key-rotation obligation retained in FR-DPA-07 and DPA-16. |
| FR-DPA-09 | MEDIUM / OPEN / BLOCKED_OWNER_DECISION | **Main** `OpenApiConfiguration.java` and **test** `drive/DriveApiInventoryIntegrationTest.java` remain outside the exact source/test map. Their concrete necessity is explained and the unnecessary Google production test helper is removed. No bounded Owner disposition/amendment has been recorded; the R2 handback itself retains this blocker. Obtain that specific disposition before retaining the exceptions or remove them consistently with the approved behavior. This reviewer supplies no new source authority. |
| DPA-L01 | LOW / OPEN backlog, partly corrected | Locking method names and current handback links are corrected. Attempt/source-hash traceability and D4 cancellation/revocation wording remain incomplete. The R2 record should distinguish final passing batches from the retained failed/timeout attempts and Maven test time from bounded-runner time. Do not consume a round solely for LOW. |

## Acceptance matrix

| DPA | Result and scope |
| --- | --- |
| 01 | NOT_VERIFIED — V29 constraints pass; newly named cutover test does not perform V28→V29. |
| 02 | PASS — reviewed real configured/unconfigured PUBLIC/legacy PRIVATE behavior and corrected strict new DTO; G2 and fresh invalid-type probes. |
| 03 | NOT_VERIFIED — selected credential proofs/distinctness and stale reset fix pass; complete before/after/redaction controls remain incomplete. |
| 04 | FAIL — signed precise fractional generation admitted; other mandatory malformed/lookup-failure cases remain incomplete. |
| 05 | NOT_VERIFIED — basic correct/wrong-profile/unconfigured/stale-access transitions pass; complete old-refresh and negative no-mutation proof missing. |
| 06–07 | NOT_VERIFIED — original expiry/reset defects fixed, but complete controlled rolling TTL/read behavior and independent-session/reset matrix remain incomplete. |
| 08 | PASS for unchanged current/predecessor rotate↔revoke contract, qualified by already reviewed real PostgreSQL cases and supplied G1 regressions. |
| 09 | NOT_VERIFIED — improved stale-reset lock barrier; remaining approved serializations absent. |
| 10–11 | NOT_VERIFIED — ERROR-only request does not prove stale-context redispatch or admitted-request boundary; no complete issuance rollback/commit/lost-reply/failing-authority-read proof. |
| 12–13 | NOT_VERIFIED — both-origin OFF sweep/method/header/export controls improve coverage; actual local-ON registration and side-effect/handler/ASYNC/stale-context controls remain. |
| 14–15 | NOT_VERIFIED — supplied revised Google/native/PIN/bootstrap/redaction regressions pass; prior specific pre-cutover LOGIN/direct-store/profile-matrix and full new-proof/PRIVATE service-boundary gaps are not supplied. No demonstrated PIN HTTP bypass is claimed. |
| 16 | NOT_VERIFIED — documented in-place invalidation now independently proven and ZIP regressions supplied; actual backup/restore/key-rotation acceptance missing. |
| 17 | NOT_VERIFIED — supplied architecture/envelope/inventory regressions pass; complete strict new schema/error/profile/D4 OpenAPI verification remains absent. |

## Fresh verification and reused receipts

Commands from repository root used `python scripts/run-bounded-command.py --profile normal --timeout-seconds <cap> --receipt R/<receipt> -- <child>`. All fresh executions exited 0, captured complete output and needed no cleanup.

| Receipt | Exact child / result |
| --- | --- |
| `R/g2.json` (300s cap) | `mvn.cmd -ntp -f backend/pom.xml -DargLine=--enable-native-access=ALL-UNNAMED -Dtest=ProfileSessionIntegrationTest,ProfileSessionConcurrencyIntegrationTest,ProfileRouteAuthorizationIntegrationTest,ProfileMigrationIntegrationTest test`; **26 tests, zero failures/errors/skips**, **50.282s**. |
| `R/probe.json` (300s cap) | `java @logs/bf26-dp-a/codex-r2-1794402747176-93c642/probe.args`; exit 0, **23.547s**. Source-mode reviewer probe under R, actual Spring/filter chain and disposable PostgreSQL, synthetic credentials, genuine issued sessions, controlled service Clock and observed row-lock wait. Includes direct execution of the document's SQL block. `R/probe-source-sha256.txt` pins this temporary probe. |
| `R/claim-wire.json` (60s cap) | `java @logs/bf26-dp-a/codex-r2-1794402747176-93c642/claim.args`; exit 0, **2.031s**. Temporary `R/ClaimProbe.java` confirms exact signed JSON fractional value, decoded Double 1.0 and rejection of the original exact BigDecimal by the helper. No database/provider/network needed. |

The completed regression probe records: fractional `1.5`, overflow integer and missing-exp HTTP **401**; prior overflowing Double/BigDecimal decoder cases now **rejected**; null profile and non-string passwords/proofs **400**; stale waiting reset **401**, epoch **2**; post-expiry rotation **rejected**; precise fractional generation **404 after admission**; exact configured/unconfigured documented invalidation and genuine old-token rejection/fresh-login recovery as described above. Diagnostic process success means measurements completed; it does not declare slice acceptance. No live provider, RAG ingestion, QA worker or model invocation.

Supplied final R2 receipts were inspected with their captured Maven summaries, not claimed as fresh independent runs:

| Receipt under `logs/bf26-dp-a/` | Tests | Actual runner seconds |
| --- | ---: | ---: |
| `r2-g1-auth.json` | 94 | 50.093 |
| `r2-g2-profiles-5.json` | 26 | 62.094 |
| `r2-g3-google.json` | 50 | 51.094 |
| `r2-g3-browser.json` | 10 | 46.078 |
| `r2-g4-contracts.json` | 69 | 66.094 |
| `r2-g5-batch1.json` | 31 | 50.109 |
| `r2-g5-batch2.json` | 6 | 31.563 |
| `r2-g5-batch3.json` | 7 | 31.578 |
| `r2-g5-batch4.json` | 112 | 131.672 |

Those final batches total **405 tests**, zero failures/errors/skips, **520.375s runner time**. Earlier `r2-g2-profiles.json`, `r2-g2-profiles-2.json`, `r2-g2-profiles-4.json` are COMMAND_FAILED/exit 1; `r2-g2-profiles-3.json` is **TIMEOUT/exit 124**, 301.047s, `windows_tree_termination_requested;descendants_terminated`, capture/cleanup complete. Its log contains a holder-thread SQL error; no general production deadlock is inferred. All attempts remain preserved. Missing mandatory cases cannot be credited by the aggregate 405 count. Supplied receipts do not pin their complete historical working-tree bytes; current reviewer source hashes/fresh executions qualify the current adjudication.

## Shared coverage and Docs Sync Check

Applied the shared 29 dimensions proportionately. **1–2 FINDING:** mandatory trace/evidence gaps. **3–6 PASS:** unchanged module graph, Auth ownership, recursive folder/package inspection, existing descriptors and canonical root `ApiResponse` under ADR-0016; **FINDING** for exact-map FR-DPA-09. **7–9 PASS:** concrete services/owning enum/entities/exceptions, reused numeric predicate, no speculative framework/pattern; its numeric correctness defect is FR-DPA-04. **10–17 FINDING/NOT_VERIFIED:** validation/admission, transaction/dispatch and recovery evidence as above; selected SQL fix is PASS. **18 PASS:** indexed bounded admission and constant-size transitions, no new hot-path scan/N+1 found. **19–20/23–24 N/A:** no retrieval, document, evaluation or benchmark capability changed. **21–22 FINDING:** absent risk coverage and misleading migration/refresh negatives. **25 PASS:** no dependency/platform/version change. **26 PASS:** fresh focused Maven plus qualified supplied architecture evidence; IDE inspection not newly run, no warning-free claim. **27 PASS** for inspected production redaction/logging shape; complete new-proof negative evidence remains NOT_VERIFIED. **28–29 FINDING:** scope/evidence/document claims; unchanged HEAD/index, no generated tracked artifact or publication.

- **ALREADY_CONSISTENT:** one Owner/two fixed profiles, auth-only PUBLIC, no business grants/later slices, defaults OFF, frozen V1–V28 canonical contents, prior review closures, unchanged root transport/module boundary and separate historical counters. Runbook column/rollback and DBML FK documentation corrections are checked against source.
- **UPDATED by reviewer:** this separate re-review, ACTIVE disposition/next step, executable current result fields, and independent-disposition addendum to the R2 handback. Submitted passing-test/self-review claims remain separate from this adjudication.
- **NOT_VERIFIED:** remaining mandatory DPA tests, actual populated migration/backup/restore/key rotation and source-qualified complete attempt manifest. The source names/handback claims do not establish these results.
- **BLOCKED_OWNER_DECISION:** FR-DPA-09's two retained exact-map exceptions. No automatic source-map expansion or takeover.
- **DEFERRED_OUT_OF_SCOPE:** the same current-navigation/preparation-README updates assigned to ChatGPT/Owner in the handoff, and unrelated historical documentation. Rechecked only changed/invalidated docs and direct references; no cleanup B. Frontend pause/FR-13/counters, v0.3 and RAG suspension/containment remain preserved.

**Next step:** Antigravity | `/antigravity-implement-handoff` then `/antigravity-self-review`, followed by independent Codex `$codex-final-review` | BF26-DP-A, this re-review and [ACTIVE](../../../handoffs/ACTIVE.md) | resolve FR-DPA-04/06/07/09 and mandatory NOT_VERIFIED cases; Owner disposition before retaining the two exact-map exceptions | remediation 2/3 consumed, next/final ordinary round 3/3; takeover 0 / NOT_AUTHORIZED; historical counters unchanged.

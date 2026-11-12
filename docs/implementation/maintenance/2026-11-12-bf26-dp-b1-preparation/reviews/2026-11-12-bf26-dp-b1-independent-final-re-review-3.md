# Independent final re-review 3 — BF26-DP-B1

- **Date / reviewer / mode:** 2026-11-12 / independent Codex / B1 implementation maintenance, Antigravity remediation round 3.
- **Authority / previous disposition:** [executable handoff](../executable-handoff-dp-b1-2026-11-12.md), [re-review 2](2026-11-12-bf26-dp-b1-independent-final-re-review-2.md), original Owner-authorized eight-route map and B1-01–24 criteria. Entry ACTIVE was `IMPLEMENTED_AWAITING_CODEX_REVIEW`. FR-DPB1-02/03/04 were already CLOSED and are retained without reopening.
- **Baseline:** `main`, HEAD `066e361ac9938dfdc102168969fe8a498cf4ee2f`, empty index. Only the two owner PublicRead integration tests changed against re-review 2's 1,916-input snapshot. No production, dependency, schema, fixture or other test change.
- **Outcome: READY_FOR_OWNER_COMMIT / INDEPENDENTLY_ACCEPTED for BF26-DP-B1. FR-DPB1-01 CLOSED; FR-DPB1-02/03/04 closures retained.** No open CRITICAL/HIGH/MEDIUM or mandatory acceptance NOT_VERIFIED remains. DPB1-L01 CLOSED; DPB1-L02 is LOW/OPEN/BACKLOG and does not block. DP-A DPA-L01 remains separate and unchanged.
- **Counters / publication:** B1 remediation **3/3 consumed**, ordinary remediation stopped after accepted round 3; takeover NOT_AUTHORIZED and not needed for this acceptance. All predecessor/Frontend/RAG counters remain unchanged. Implementation is uncommitted/unpublished; no live deployment/cutover or next-slice authorization is claimed.

## FR-DPB1-01 closure — effective observer and positive controls verified

Both `findPublicNeverInvokesFullFindOrEnrichment` methods now install the repository guard at **index 0**, ahead of terminal Spring Data execution. The source change is confined to the existing two tests.

- Fiction invokes real `findById`, `findStoryArchetypeIdsByFictionId` and `findWorldSettingIdsByFictionId`, asserting counter values **1, 2, 3** respectively.
- Film invokes real `findById`, `findGenreIdsByFilmId`, `findStoryArchetypeIdsByFilmId` and `findWorldSettingIdsByFilmId`, asserting **1, 2, 3, 4**.
- The tests reset that now-qualified counter before the actual PUBLIC success, hidden and injected-failure controls; the counter remains zero and the retained People/Group `never()` checks hold. Real HTTP success/404/safe 500 controls and finally-based advice removal remain intact.

Fresh reviewer execution passed both classes, including these positive/negative controls and retained managed JPA/context/PID, NSFW/soft-delete/atomic mixed-state, DTO/cache/PRIVATE cases. The original concern was that a zero could reflect an unobserved call; the explicit positive controls now show that each guarded repository family is observed. **The residual B1-15 proof is satisfied and FR-DPB1-01 is CLOSED.** No production behavior or accepted contract was changed to make the test pass.

FR-DPB1-02's session/dispatcher/path/credit proof, FR-DPB1-03's actual HTTP envelope/schema/type/union proof, and FR-DPB1-04's method/provenance/API corrections remain accepted. Their relevant inputs are byte-identical to re-review 2. The Owner-visible result is the approved eight authenticated GETs with minimal eligible PUBLIC Fiction/Film details, unchanged selected Reference DTOs and PRIVATE behavior, and default denial of every unselected PUBLIC business route. There is no Public list/search/write expansion or new framework.

## Finding register

| ID | Severity / disposition | Independent evidence |
| --- | --- | --- |
| FR-DPB1-01 | MEDIUM / CLOSED | Index-0 guard, all three/four positive invocation counts, reset negative observations and fresh 26-test run |
| FR-DPB1-02 | MEDIUM / CLOSED, retained | Re-review-2 acceptance and unchanged source-qualified security/session/path inputs |
| FR-DPB1-03 | MEDIUM / CLOSED, retained | Re-review-2 actual generated graph/HTTP specimen checks; production/contract test unchanged |
| FR-DPB1-04 | MEDIUM / CLOSED, retained | All 52 handback method references resolve; final source/case/receipt/XML/reuse audit below |
| DPB1-L01 | LOW / CLOSED | Round-3 self-review explicitly distinguishes live PUBLIC dispatch 403 from stale authority 401 |
| DPB1-L02 | LOW / OPEN / BACKLOG, nonblocking | Self-review line 26 loosely includes `VaultEntryOperations` in its `never()` wording. The actual read requires and exercises Vault; the relevant `never()` controls are People/Group, while repository counters cover full find/classifications. This summary wording is not credited as a Vault-never assertion. Align it during a later authorized documentation update; do not start another implementation/remediation round for it. |

No new blocking behavior, architecture change or future feature was imposed. Accepted closures are retained because no new evidence invalidates them. The remaining original proof was verified independently of the exhausted correction budget. LOW wording is handled under the existing backlog policy.

## Fresh reviewer verification

Reviewer evidence **R3**: `logs/bf26-dp-b1/codex-rereview3-6d1f23dc64db42639bd0891ab12dd832/`. Entry source/docs/raw-producer hashes and pre-run target reports were saved; fresh XML copies are stored beside the receipt. All **1,916/1,916** reviewer entry inputs remain unchanged after execution; no production/test edit was made by this reviewer.

Executed from repository root:

```powershell
python scripts/run-bounded-command.py --profile normal --timeout-seconds 300 --receipt R3/review-db.json -- mvn.cmd -ntp -f backend/pom.xml '-DargLine=--enable-native-access=ALL-UNNAMED' '-Dtest=PublicFictionReadIntegrationTest,PublicFilmReadIntegrationTest' test
```

`R3` above denotes the actual unique evidence directory, not a second command execution. **PASS / exit 0, 26 tests (13 Fiction + 13 Film), zero failures/errors/skips, 55.125s**, complete capture and cleanup, cleanup not needed. This fresh execution includes the corrected positive observer controls. No full suite or already accepted unchanged security/contract gate was rerun merely for another review label.

The producer's fresh round-3 DB receipt is also PASS/exit 0, **26 tests / 65.609s**, ordinary 300s, complete capture/cleanup. Its exact argv hash and stdout/stderr lengths validate. Producer and reviewer DB executions are not added together as distinct acceptance cases.

## Final evidence qualification — fresh versus reused

The final source manifest changed format to a relative-path/hash object. All **1,916 entries** match current bytes, and its complete resolved path set equals the reviewer snapshot (zero missing/extra paths). The **127-entry** case manifest matches all twelve supplied XML files with zero key differences/failures/errors/skips. All **52** criterion-table method references resolve to current source.

| Logical final gate | Accepted evidence / provenance |
| --- | --- |
| G-B1-DB, 26 cases | Fresh producer R3 receipt/XML plus independent R3 26-case run on the final two changed tests |
| G-B1-SEC, 27 cases | Unchanged R2 source, previously accepted producer R2 receipt and independent R2 security/contract run; R3 copied receipt is byte-identical to producer R2 |
| G-B1-REF, 9 cases | Unchanged source; producer R2 receipt and XML reused identically |
| G-B1-CONTRACT, 39 cases | Unchanged source; accepted producer R2 receipt plus independent R2 contract/architecture run; R3 copied receipt is byte-identical to producer R2 |
| G-B1-REG, 26 cases | Unchanged source; producer R2 receipt and XML reused identically |

**Exact XML provenance was independently resolved:** six reused REF/REG class XML files in the R3 bundle match producer R2 byte-for-byte; the other four reused SEC/CONTRACT/architecture XML files match the saved **independent re-review-2** XML files byte-for-byte, rather than producer R2's XML. Their corresponding fresh reviewer command was `review-security-contract.json` under `logs/bf26-dp-b1/codex-rereview2-97a4c2c1fff94559b429b50b5a7f8314/`, PASS/exit 0, 66 cases, 77.109s, on the unchanged selected inputs. Copied producer receipts remain separate supporting evidence; they are not misrepresented as the command that generated those four XML files.

Thus **127 disjoint final cases are source-qualified**, with **26 fresh round-3 cases and 101 reused cases**. The additional independent round-3 rerun corroborates the changed 26; it does not turn the reused 101 into fresh executions. All prior failures, receipts, snapshots and independent reviews remain retained. No old timeout/failure is rewritten to PASS.

## Original acceptance and scope

All B1-01–24 are independently satisfied by the cumulative source-qualified evidence. The unchanged accepted implementation remains one single-Owner application with fixed profiles and precisely eight new authenticated GET grants. Fiction/Film PUBLIC eligibility combines non-NSFW and active correct-type Vault state in the required new consistent snapshot; exact six/seven-key DTOs, hidden/absent errors, cache behavior, session/dispatch authority and PRIVATE compatibility retain their qualified proof. Reference responses/errors/order and all unselected PUBLIC denials retain their accepted evidence. Same-envelope OpenAPI alternatives and module ownership remain verified.

Frozen migrations V1–V29, DBML, module dependencies, root transport contract, production provider defaults, paused Frontend/H1-F/FR-13 and RAG suspension remain unchanged. No B2/C, Recycle Bin, final non-RAG audit, deployment, live provider or next phase is authorized. This is slice acceptance, not a phase/maintenance-final aggregation of future unapproved slices.

Recursive structure remains Fiction **86 Java files/23 packages**, Film **104/26**, Authentication **91/21**, Reference **41/12**, Vault **58/17**; all Java-containing packages retain `package-info.java`, with no `.gitkeep`. The two changed tests reuse existing packages and nested advice. Previous independent 38-check architecture acceptance remains source-applicable. No new interface hierarchy, common module, serializer, source/table query, dependency or test framework was introduced.

## Shared 29-dimension coverage

| Dimension | Final status / evidence and limit |
| --- | --- |
| 1 Owner intent | PASS — exact approved eight-route behavior/non-goals |
| 2 Traceability | PASS — original IDs, 52 real methods, source/case/XML ledger |
| 3 Frozen compatibility | PASS — no new production/schema/ADR/module delta |
| 4 Ownership/transactions | PASS — retained real owner/Vault/context/PID proof |
| 5 Recursive tree | PASS — affected subtrees/descriptors inspected |
| 6 Shared transport/exceptions | PASS — ADR-0016 root envelope and owner errors unchanged |
| 7 SOLID | PASS — focused capability/projection/mapping; no speculative abstraction |
| 8 DRY/KISS/YAGNI | PASS — existing auth/Vault/Reference/transport reused |
| 9 Patterns | PASS — no new production pattern/framework |
| 10 Domain correctness | PASS — eligibility, lifecycle and PRIVATE semantics qualified |
| 11 HTTP/OpenAPI | PASS — retained actual envelope/schema/type/union and wire proof |
| 12 Validation/path variants | PASS — retained selected route/token/path cases |
| 13 Authorization/privacy | PASS — qualified session/dispatch/DTO plus effective negative observers |
| 14 Secrets/cache/egress | PASS — no new secrets/egress, retained safe header/error cases |
| 15 Isolation/concurrency | PASS — managed-context/PID and controlled data/session races |
| 16 Failure/recovery | PASS bounded read failure controls; new restore/migration system N/A |
| 17 PostgreSQL/JPA | PASS real PostgreSQL, unchanged FK/triggers/schema, no H2 |
| 18 Complexity/resources | PASS bounded production ID reads and bounded test actors |
| 19 Retrieval/recommendations | N/A — unchanged/denied |
| 20 Document/OCR fidelity | N/A — no parser work |
| 21 Security tests | PASS — effective positive and negative controls, source-qualified retained cases |
| 22 Test adequacy | PASS — residual observer qualification now demonstrated |
| 23 Performance benchmarks | N/A — no benchmark obligation/claim |
| 24 Evaluation science | N/A — no model/evaluation scope |
| 25 Dependencies | PASS unchanged; unrelated vulnerability audit N/A |
| 26 Build/tooling/diagnostics | PASS focused watchdog/build; complete IDE diagnostics NOT_VERIFIED, not a mandatory new gate |
| 27 Logging/observability | PASS retained safe failure/log boundaries; synthetic fixtures only |
| 28 Docs/governance | PASS blocking contracts/status/provenance; DPB1-L02 LOW summary wording backlog |
| 29 Hygiene/delivery | PASS exact map, empty index, whitespace checks and preserved raw evidence; no publishing |

No warning-free or complete IDE inspection claim is made. Prior recorded IDE timeout remains historical/NOT_VERIFIED; no reported mandatory native/UI gate applies to B1. Actual deployment/cutover, live Owner data review and future publication remain outside this acceptance.

## Docs Sync Check and Owner action

- **UPDATED:** this new independent review and current ACTIVE/executable status, finding/counter/next-action fields. Author handback/self-review, earlier reviews and all raw evidence are preserved; this independent disposition supersedes their submission-state claims.
- **ALREADY_CONSISTENT:** actual baseline/source map, original B1-01–24, closed FR-DPB1-01–04, corrected DPB1-L01 wording, manifest/method/reuse qualification, and separate DP-A/Frontend/RAG statuses/counters.
- **DEFERRED_OUT_OF_SCOPE:** routine root policy/AGENTS/preparation README/roadmap/index synchronization and a brief post-publication closeout belong to ChatGPT/Owner after the actual published SHA is available. DPB1-L02 may be corrected in that later authorized documentation work; it causes no extra implementation round. Historical package-tree/current-notice cleanup is not silently performed here.
- **NOT_VERIFIED:** complete IDE diagnostics, deployment/cutover/live Owner-data suitability and future publication. These are not falsely counted as runtime acceptance or new blockers to this bounded slice.
- **Final saved-file integrity:** 69 local Markdown links resolve; all 1,916 source inputs and 21 producer-evidence files retain entry hashes. Of the 17 existing scoped documentation inputs, only the executable disposition changed; the new review and ACTIVE disposition are the other authorized reviewer edits. Whitespace checks are clean, the index is empty and HEAD remains `066e361ac9938dfdc102168969fe8a498cf4ee2f`.

**One suggested commit message:** `feat(backend): enable bounded public catalog reads`

**Next step:** Owner → ChatGPT | Owner normal git add/commit/push of the accepted B1 delivery; then brief actual-SHA slice closeout/current-navigation sync | BF26-DP-B1 and this independent final re-review 3 | READY_FOR_OWNER_COMMIT, independently accepted but unpublished; no staged-tree review or new handoff required | remediation 3/3 consumed and stopped; takeover NOT_AUTHORIZED; all historical counters unchanged; no B2/C authorization.

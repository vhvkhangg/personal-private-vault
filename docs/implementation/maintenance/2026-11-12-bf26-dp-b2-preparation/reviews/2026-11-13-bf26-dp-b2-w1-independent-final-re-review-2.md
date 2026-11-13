# Independent final re-review 2 — BF26-DP-B2-W1

- **Date / reviewer / mode:** 2026-11-13 / independent Codex / remediation round 2 implementation review.
- **Authority:** unchanged [W1 executable handoff](../executable-handoff-dp-b2-w1-2026-11-12.md), recorded Owner **APPROVED — BF26-DP-B2-W1 ONLY** authorization and [independent re-review 1](2026-11-12-bf26-dp-b2-w1-independent-final-re-review-1.md). Entry ACTIVE was IMPLEMENTED_AWAITING_CODEX_REVIEW.
- **Baseline:** `main`, HEAD `e8f4dde0d696d8dc8e9cd51d6658aeb9634ba6b0`, empty index. Against the previous reviewer snapshot, only FilmController, FilmCreateRequestReader and PublicFilmCreateIntegrationTest changed. No source path was added; previously accepted unrelated behavior/findings are retained.
- **Outcome: READY_FOR_OWNER_COMMIT / INDEPENDENTLY_ACCEPTED for BF26-DP-B2-W1. FR-W1-01/03/05 CLOSED; FR-W1-02/04 closures retained.** No open CRITICAL/HIGH/MEDIUM or mandatory acceptance NOT_VERIFIED remains. DPB2W1-L01 is LOW / OPEN / BACKLOG wording only, nonblocking.
- **Counters / publication:** remediation **2/3 consumed**, ordinary remediation stopped after accepted round 2; takeover NOT_AUTHORIZED / NOT_USED. Predecessor and Frontend/RAG counters unchanged. Implementation is uncommitted/unpublished; no deployment/cutover or later slice is approved by acceptance.

## Fresh independent verification

Reviewer evidence **R2**: `logs/bf26-dp-b2-w1/codex-rereview2-85848053ba394e2ebf2cb4395d70d295/`. Entry snapshots preserve **1,909 source/resource/script/POM inputs**, twelve documentation inputs and **1,020 round-2 producer files**, target XML and submitted ACTIVE/executable. Probe source, compilation output and argument files live only under this ignored directory. No backend production/test source was edited by this reviewer.

```powershell
python scripts/run-bounded-command.py --profile normal --timeout-seconds 300 --receipt R2/review-create-api.json -- mvn.cmd -ntp -f backend/pom.xml '-DargLine=--enable-native-access=ALL-UNNAMED' '-Dtest=PublicFilmCreateIntegrationTest,OpenApiRouteInventoryIntegrationTest' test
```

`R2` denotes the actual unique directory above. **PASS / exit 0, 31 tests (30 create + 1 API), zero failures/errors/skips, 126.469s**, complete capture/cleanup. Fresh XML copies are preserved. This includes the changed media, admission/snapshot and request-boundary tests and corroborates the unchanged API graph.

`R2/review-media-admission-probe.json`: **PASS / exit 0, 62.640s**, ordinary 300s, complete capture/cleanup. This is an isolated Spring/Testcontainers diagnostic using the existing test base, a reference original-signature MVC binding, actual configured converters, real persisted tokens and a positively qualified RequestMappingHandlerAdapter spy. Compilation is separately recorded in `compile-probe.json`. It supplies independent handler-boundary observation rather than relabeling the producer's Vault spy as a MVC handler spy.

## Finding closure and retained evidence

Paths use **J** = `backend/src/main/java/com/vhvkhangg/personalprivatevault/` and **T** = the corresponding test directory.

### FR-W1-01 — MEDIUM / CLOSED: supported media and PRIVATE conversion restored

`J/film/internal/web/controller/FilmCreateRequestReader.java:99–125` now checks PUBLIC media before interpreting original bytes; `readPrivate` at lines 253–298 rejects missing/blank media and delegates supported representations to the configured MVC converter with headers/charset. FilmController passes the real servlet request into the PUBLIC reader. Dedicated new tests assert canonical 415 and unchanged domain snapshots for the two originally failing cases and PUBLIC missing Content-Type; existing vendor/Latin-1/ordinary JSON controls remain green.

Independent diagnostic results:

| Case | Current HTTP / mutation | Observed Cache-Control |
| --- | --- | --- |
| PRIVATE ordinary / vendor JSON | 201 / exactly one Film each | Existing security header unchanged |
| PRIVATE declared Latin-1 with Café | 201 / correct decoded value, one Film | Existing security header unchanged |
| PRIVATE missing Content-Type / text/plain | 415 / zero Film delta | `no-cache, no-store, max-age=0, must-revalidate` |
| PUBLIC missing Content-Type / text/plain | 415 / zero Film delta | `no-cache, no-store, max-age=0, must-revalidate` |
| PUBLIC ordinary / vendor JSON | 201 / exactly one Film each | `no-store` |

The reference original-signature binding returns 415 for the unsupported representations, matching current behavior. The 415 error responses retain the **no-store directive** through existing security header handling; no global advice/filter/cache redesign was needed. Existing PRIVATE creation has **no Location header**, as the real PRIVATE regression test and unchanged ApiResponses overload require. The original media regressions are resolved without weakening PUBLIC parsing or PRIVATE conversion.

### FR-W1-03 — MEDIUM / CLOSED: exact negative boundaries and finite request cases qualified

`T/film/PublicFilmCreateIntegrationTest.java:820–925` now requires exact **401 AUTHENTICATION_REQUIRED**, null data and the safe generic message for fresh revoked requests, before-authority invalidation and authority-lookup exceptions. It compares complete `ProfileAuthFixture.domainSnapshot` values before/after and verifies no Vault interaction; authority recovery is positively exercised. Existing deterministic admitted-elevation/revocation and before-lookup latches remain intact.

The producer's Vault observer is credited for the domain boundary only. The independent diagnostic additionally positively observes actual MVC handler execution on allowed POSTs, clears observations, injects authority failure and verifies **zero handler invocations**, exact 401/envelope and unchanged full domain snapshot. After successful restoration it requalifies the observer, revokes the persisted session, and verifies a fresh POST again has **zero handler invocations**, canonical 401 and unchanged domain snapshot. This directly qualifies the previously missing no-handler observation, combined with the unchanged SessionAuthorityFilter early-return path and the fresh deterministic producer race tests.

The boundary method now adds empty/blank original title and country normalization to null, padded **raw originalTitle** overflow before trim, forbidden null/empty options and exact expected successful-create deltas. Together with retained missing/null/type/enum/duplicate/length and country cases, this completes the named finite residuals. The original real PostgreSQL observer, insertion fault, deferred-commit rollback, both real PUBLIC origins, seven-key receipts and subsequent B1 GET remain credited. No new Auth lifecycle or cancellation contract is imposed.

### FR-W1-05 — MEDIUM / CLOSED: material requirement/evidence mapping restored

The original W1-01–16 meanings are retained. The corrected API method and two actual security methods now identify real passing code; all six documented command durations match the current watchdog receipts. The case manifest, output lengths, current source hashes and requirement-to-test paths are independently reconciled below. Remaining small summary errors are separated into DPB2W1-L01; they are not credited as runtime facts and do not invalidate the actual observable acceptance evidence.

### FR-W1-02 / FR-W1-04 — CLOSED, retained

OpenAPI source and its test are byte-identical to accepted re-review 1. Fresh API execution retains the 17-property PRIVATE request, complete profile-conditioned request/response alternatives, length/unresolved-reference controls and B1 graph/specimens. The round-2 generated artifact contains **310 schemas and zero dangling references**. The six-production-file change map and nested parse exception remain intact, with no new file/package/dependency. No new evidence invalidates either closure.

### DPB2W1-L01 — LOW / OPEN / BACKLOG, nonblocking author-summary wording

Both current handback/self-review still include one redundant nonexistent citation, `SecurityFilterChainIntegrationTest.unselectedMutationsReturn403Forbidden`, alongside the real exact-route sweep and actual security method evidence. They also still claim a PRIVATE Location header, whereas current code and passing `privateProfileCreateRetainsFullEighteenKeyBehavior` prove **no Location for either profile**. Their 25 class-qualified references contain one such nonexistent alias each; the required original behavior is traced by the other real methods and independently reviewed here.

These are limited summary/citation defects, not unresolved PRIVATE behavior, a new source exception or missing runtime proof. The original MEDIUM traceability defect (rewritten IDs, broad nonexistent mapping and false command durations) is resolved. Correct the small remaining wording during a later authorized documentation update; no further implementation/remediation round is required for LOW alone. This review's exact facts supersede those inaccurate summary claims.

## Final producer evidence qualification

Producer evidence: `logs/bf26-dp-b2-w1/remediation-round2/`.

| Gate | Cases | Exact receipt / watchdog wall duration |
| --- | --- | --- |
| CREATE | 30 | `G-W1-CREATE-retry1.json` / 147.609s |
| SEC | 27 | `G-W1-SEC.json` / 124.484s |
| PRIVATE | 74 | `G-W1-PRIVATE.json` / 62.687s |
| READ | 29 | `G-W1-READ.json` / 101.282s |
| API | 1 | `G-W1-API.json` / 62.203s |
| ARCH | 38 | `G-W1-ARCH.json` / 53.734s |

All six selected receipts are PASS/exit 0, normal 300s, with complete capture/cleanup and matching stdout/stderr byte lengths. The selected class/method XML and manifest reconcile to **199 distinct cases**, zero failures/errors/skips. All **11 final changed-source/test hashes** match current bytes; resources, fixtures, POM/config and other sources are unchanged from the reviewed baseline where not explicitly in the map. The independent complete-input snapshot and three-file delta establish current review applicability. The changed-file producer manifest is not falsely described as a complete per-run snapshot.

The fresh 31-case reviewer run corroborates the affected create/API cases; it is **not another 31 unique acceptance cases**, and diagnostic HTTP requests are not added as more JUnit cases. The initial 1,028 producer files and round-1 1,041 producer files retain their previous reviewer hashes. All earlier failed attempts and independent reviews remain preserved. No previous failure or missing proof has been rewritten to PASS.

## Cumulative original acceptance

All **W1-01–16 are independently satisfied** by cumulative source-qualified evidence: one authenticated PUBLIC create-only POST; strict five-field original-byte request; fixed non-NSFW/no-relation mapping; one committed Film/active FILM Vault identity with real SQL rollback/commit fault proof; seven-key no-store receipt; finite validation/media/error cases; unchanged full PRIVATE behavior; exact nine registered business grants with aggregate no-handler/no-mutation denials; B1 detail snapshot retained; exact admission/failure/recovery observations; accurate generated schema graph; legal ownership/frozen inputs and qualified case/receipt/source evidence. No mandatory W1 acceptance remains NOT_VERIFIED.

This accepts W1 only. B2-W2/S/V, DP-C, Public updates/Fiction/relations/People/Search/generic Vault, Frontend/BFF, RAG resumption, provider activation, Recycle Bin, future audit and deployment remain separately gated. DP-A/B1 acceptance and separate LOW backlogs/counters are unchanged. The root B1-publication notices are not rewritten as W1 runtime authority; the separately recorded W1 Owner authorization and executable govern this bounded delivery.

## Architecture, structure and shared coverage

Recursive source inspection remains Film **106 Java files/26 packages**, Authentication **91/21**, Vault **58/17**, Reference **41/12**; every Java-containing package has package-info.java and no .gitkeep. No public/internal ownership leak, new module edge, migration/index, dependency/framework, transaction manager, global parser/filter/advice or unrelated source edit. ApiResponse and shared OpenAPI remain at the approved ADR-0016 root boundary; Film DTO/reader/controller/mapper remain owner-local. The existing canonical Film/Vault transaction, validation/defaults and B1 REPEATABLE_READ path are retained.

| Dimension | Final status / evidence and limit |
| --- | --- |
| 1 Owner intent | PASS — exact approved W1 create-only behavior and preserved PRIVATE |
| 2 Traceability | PASS material original-ID/case/receipt mapping; DPB2W1-L01 LOW redundant wording |
| 3 Frozen compatibility | PASS exact source map, unchanged schema/module/ADR boundaries |
| 4 Ownership/transactions | PASS canonical Film/Vault create and real observer/rollback/commit proof |
| 5 Recursive structure | PASS current trees/descriptors; no new packages or stale .gitkeep |
| 6 Shared transport/exceptions | PASS canonical root envelope, owner-local parser/exception and safe errors |
| 7 SOLID | PASS focused HTTP adaptation; no domain validation/persistence duplication |
| 8 DRY/KISS/YAGNI | PASS existing create, configured converter, validation and defaults reused |
| 9 Patterns | PASS no speculative framework/hierarchy/manager |
| 10 Domain correctness | PASS new non-NSFW identity, no relations, correct failure/media semantics |
| 11 HTTP/OpenAPI | PASS supported media/charset, safe headers and qualified full schema graph |
| 12 Validation | PASS finite original-body/required/null/type/enum/bounds/normalization/forbidden cases |
| 13 Authorization/privacy | PASS real persisted origins, exact grants and qualified failure/revocation handler boundaries |
| 14 Secrets/cache/egress | PASS tested safe wire/cache/errors; no new credentials/providers/egress |
| 15 Atomicity/concurrency | PASS real DB faults/observer plus deterministic admission/race/snapshot proof |
| 16 Failure/recovery | PASS exact 401/failure snapshots, no handler/no write, restoration and SQL rollback |
| 17 Database | PASS real PostgreSQL, no H2 or changed mappings/migrations |
| 18 Complexity/resources | PASS bounded per-record use case and bounded verification actors |
| 19 Search/recommendation | N/A — no W1 discovery/ranking feature |
| 20 Document/OCR | N/A — no document/parser capability |
| 21 Security tests | PASS positive observers and meaningful denial/failure/state controls |
| 22 Test adequacy | PASS new residual assertions and independent media/handler diagnostics |
| 23 Benchmarks | N/A — no performance requirement or claim |
| 24 Evaluation science | N/A — no ML/evaluation scope |
| 25 Dependencies | PASS unchanged POM/resources/runtime stack; unrelated vulnerability audit N/A |
| 26 Build/tooling/diagnostics | PASS focused builds/watchdog; complete IDE diagnostics NOT_VERIFIED, no warning-free claim |
| 27 Logging | PASS scoped safe source/error boundaries and synthetic fixtures; full unrelated log audit N/A |
| 28 Docs/governance | PASS blocking contracts/status/evidence; DPB2W1-L01 LOW summary correction deferred |
| 29 Hygiene/delivery | PASS exact map, preserved evidence/history and empty index; no publishing |

No full suite, model execution, native UI gate or IDE inspection was imposed by this re-review. Existing Maven metadata-access warnings are configuration/repository concerns; no dependency/credential repair is authorized. No complete warning-free, live-data suitability, deployment/cutover or future publication claim is made.

## Docs Sync Check and Owner action

- **UPDATED:** this independent review and current ACTIVE/executable disposition, finding/counter/next-action fields only. Author handback/self-review and earlier independent reviews remain preserved; this independent disposition controls acceptance.
- **ALREADY_CONSISTENT:** original W1 scope/authority and all 16 acceptance requirements, six-file production map, cumulative runtime evidence, frozen inputs, predecessor and Frontend/RAG statuses/counters.
- **DEFERRED_OUT_OF_SCOPE:** routine root/Owner-policy/README/roadmap/navigation sync and a brief actual-SHA closeout belong to ChatGPT/Owner after publication. DPB2W1-L01 can be corrected in later authorized docs work and creates no extra implementation round. Existing unrelated LOW backlogs/history are not silently repaired.
- **NOT_VERIFIED:** complete IDE diagnostics, live Owner-data suitability, deployment/cutover and future publication. These are not falsely credited as slice runtime gates or new acceptance blockers.
- **Final saved-file integrity:** all 77 local Markdown links across this review, ACTIVE and executable resolve; UTF-8 and tracked whitespace checks pass, with only the existing OpenAPI-test CRLF conversion notice. All 1,909 source inputs and 1,020 round-2 producer files retain entry hashes; the initial/round-1 producer history also retains its checked hashes. Prior review files, author handback/self-review and other scoped docs are unchanged. ACTIVE's historical suffix, executable creation sections 1–10 and initial checkpoint remain byte-preserved; prior executable re-review labels are explicitly historical. Only this new review and current ACTIVE/executable fields were edited; the index is empty and HEAD remains `e8f4dde0d696d8dc8e9cd51d6658aeb9634ba6b0`.

**One suggested commit message:** `feat(backend): enable bounded public film creation`

**Next step:** Owner → ChatGPT | Owner normal git add/commit/push of accepted W1 delivery, then brief actual-SHA slice closeout/current-navigation sync | BF26-DP-B2-W1 and this independent re-review 2 | READY_FOR_OWNER_COMMIT / independently accepted, unpublished; no extra staged-tree review or new handoff | remediation 2/3 consumed and stopped after acceptance; takeover NOT_AUTHORIZED / NOT_USED; predecessor counters unchanged; no B2-W2/S/V or DP-C authorization.

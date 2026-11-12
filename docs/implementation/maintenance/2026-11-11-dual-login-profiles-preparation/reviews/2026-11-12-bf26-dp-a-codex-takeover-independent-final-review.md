# Independent final review — BF26-DP-A exceptional Codex takeover

- **Date / reviewer / mode:** 2026-11-12 / separate independent Codex reviewing session / `SLICE`, authorized implementation maintenance.
- **HEAD / entry:** `main` at `536a7aab8f48c2a182a8c5b88c1d248fdb29e283`; empty index; 73 received delivery paths. Implementation remains uncommitted/unpublished. This reviewer changed no delivered production code or tests.
- **Authority / inputs:** [executable contract and exceptional takeover ledger](../executable-handoff-dp-a-2026-11-11.md), [D1–D6 ratification](../owner-ratification-d1-d6-2026-11-11.md), [R3 independent stop/disposition](2026-11-11-bf26-dp-a-independent-final-re-review-3.md), [takeover authorization, delivery and self-review](../implementation-handback-dp-a-2026-11-11.md#11-exceptional-codex-takeover-delivery--2026-11-11), current Owner policy, resume COMMON and Docs Sync contract.
- **Outcome:** **READY_FOR_OWNER_COMMIT** for BF26-DP-A. **FR-DPA-06/07/09 MEDIUM CLOSED** by this independent review; FR-DPA-01/02/03/04/05/08 closures retained. No open CRITICAL/HIGH/MEDIUM or mandatory acceptance NOT_VERIFIED remains in this slice. DPA-L01 remains **LOW / OPEN / BACKLOG**, limited to misleading retained Google cancellation fixture naming.
- **Authorship / counters:** original implementation/remediation by Antigravity; this corrective delivery by a different Codex implementer. Implementer self-review was not acceptance. Antigravity **3/3 EXHAUSTED / STOPPED**; exceptional Codex takeover **1/1 independently reviewed / ACCEPTED**. No new round, takeover allowance, phase or publication authority follows.

## User requirement and simplest-mechanism check

DP-A delivers one immutable Owner with fixed PUBLIC/PRIVATE profiles and persisted, revocable session authority. PUBLIC remains Auth-only with default denial of business/data/unknown routes; PRIVATE retains accepted operations and feature flags. This takeover supplies the missing finite security/concurrency/migration/recovery proofs and the two specifically approved OpenAPI/inventory exceptions. It adds no production authorization algorithm, architecture, dependency, backup platform or future profile grant.

The current source and parent contracts were inspected rather than accepting test names or the handback narrative. The takeover delta is exactly seven backend paths: four Profile integration tests, `authentication/fixture/ProfileAuthFixture.java`, root `OpenApiConfiguration.java`, and `drive/DriveApiInventoryIntegrationTest.java`; plus the three authorized handoff/status documents. Comparison with the implementer's received-entry hashes independently confirms that boundary. All 1,898 final backend/source/resource/build/watchdog hashes match the reviewed tree. The only intermediate-to-final drift in reused G1/G3/G4/G5 evidence is the unrelated G2-only concurrency test; final G2 uses the exact final snapshot.

Reviewer evidence **R** = `logs/bf26-dp-a/codex-takeover-independent-1794441441001-645f1f/`; implementer evidence **T** = `logs/bf26-dp-a/codex-takeover-1794408167866608000/`. `R/entry-source-sha256.json`, `recursive-inventory.txt`, `source-match.json`, and `evidence-audit.json` record applicability, actual recursive Auth/Drive/support/snapshot subtree inventory and evidence validation. V1–V28 canonical migration contents all match HEAD; the approved additive V29 is retained. Earlier independently reviewed production behavior and documentation are reused where byte-identical; no unrelated frozen scope is reopened.

## Finding register

Paths below are relative to `backend/src/test/java/com/vhvkhangg/personalprivatevault/` unless marked **main**.

| ID | Severity / blocking? | Independent evidence and disposition | Status |
| --- | --- | --- | --- |
| FR-DPA-06 | MEDIUM / resolved | `authentication/ProfileRouteAuthorizationIntegrationTest.registeredRoutesDeniedToPublicTokenFromBothOrigins` uses actual table names and populated full row/column snapshots, fails on SQL errors, sweeps registered OFF methods with both genuine PUBLIC origins and ANON, and verifies no MVC handler execution. The separate loopback-only ON context verifies Drive/files/Google registrations, exact 403, no attachment, no interceptor/provider/spool activity and a genuine PRIVATE positive control. Actual Callable ASYNC redispatch removes the bearer and reuses admitted context: live PRIVATE 200, post-switch stale 401; PUBLIC ASYNC/ERROR exactly 403 and ANON 401. The admitted GET/write/stream test holds all three handlers across a committed downgrade, rejects new old-token admissions, then completes the admitted work including an actual settings update and exact stream bytes. | CLOSED |
| FR-DPA-06, continued | MEDIUM / resolved | `ProfileSessionConcurrencyIntegrationTest.forcedCommitSerializations` covers 13 explicit orderings, observing the second PostgreSQL backend blocked by the first transaction before releasing commit. It verifies reset/refresh/switch responses, epoch/generation and surviving or invalidated pairs. Controlled Clock tests prove rolling TTL/read non-extension/exact session expiry and genuine refresh expiry while waiting for a lock. Actual signer failure and a deferred PostgreSQL constraint failure roll back all authority row values, return no accepted pair and preserve prior access/refresh. A real missing relation fails closed/redacted; a failed response write after commit delivers no pair and requires fresh login. `ProfileSessionIntegrationTest` now isolates malformed claims with a genuine persisted issuer/session, a valid admission control and precise fractional wire bytes; it proves negative transition non-mutation, proof redaction, multi-device/caller reset, PRIVATE-only survival and genuine PIN HTTP/service authority. No positive admission is replaced by a mock. | CLOSED |
| FR-DPA-07 | MEDIUM / resolved | `ProfileMigrationIntegrationTest.syntheticV28ToV29CutoverPreservesDataAndRevokesLegacyTokens` actually migrates a unique V28 database containing real encoded credentials, populated domain/settings/FKs and a consumed→active refresh replacement chain. Full values, prior migration checksums and replacement history survive; active legacy authority is revoked, PUBLIC begins unconfigured, and retained PRIVATE login/PIN work through the real HTTP stack. `disposablePostgresqlBackupRestoreAndKeyRotation` actually runs `pg_dump -Fc` and `pg_restore --exit-on-error` into a different disposable database, admits restored genuine profiles under the old key, executes the SQL read from the operator runbook, proves old access rejection with that same key, then restarts with a new random key and rejects old JWT/refresh material including a restored successor. Fresh PRIVATE login/data read work; domain values and original source authority survive; dump/target cleanup completes. These are synthetic recovery controls, not live Owner deployment. | CLOSED |
| FR-DPA-09 | MEDIUM / resolved | The recorded exceptional Owner authorization expressly permits the two exact-map exceptions. **Main** `OpenApiConfiguration.java` changes only DP-A anonymous login/strict request schemas/profile authority/errors and selected Google 503/303 documentation. `drive/DriveApiInventoryIntegrationTest.java` changes only the four added DP-A operation count and matching names/comments (239→243). Source inspection and G2/G4/G5 evidence find no unrelated runtime or Drive activation change. | CLOSED |
| FR-DPA-01/02/03/04/05/08 | Resolved previously | Retain the immutable prior independent closures. The matching production snapshot and present genuine claim/epoch/lock/expiry/strict-body/recovery controls provide no contrary drift. | CLOSED, retained |
| DPA-L01 | LOW / no | Links, package method names, receipt/count/source trace and the R3 batch-3 total are corrected. Remaining `GoogleSignInBrowserIntegrationTest.consumer*CancellationRevokesLateRefreshToken` names and `fixture/GoogleSignInBrowserConsumer.revokedRefreshRejected` still call D4 503/no issuance a revocation. This review credits no actual token revocation from those cancellation branches. Current handback describes the limit accurately; core D4 no-issuance and retained protocol controls have separate proofs. Preserve for the next applicable maintenance/audit; no correction round solely for LOW. | OPEN / BACKLOG |

## Acceptance matrix

All rows refer to the finite DPA-01–17 contract, not later DP-B/C, deployment or phase-final acceptance.

| Requirement | Result | Evidence / limit |
| --- | --- | --- |
| DPA-01 | PASS | Actual populated V28→V29 and schema/deferred-FK tests; retained bootstrap G1; credential/domain/FK/history checks above. |
| DPA-02–03 | PASS | Genuine PUBLIC/legacy PRIVATE HTTP issuance, strict new DTO negatives, no-mutation wrong proof/distinctness and redaction tests; G1/G2. |
| DPA-04 | PASS | Genuine lineage/valid control/precise signed wire negatives, absent/revoked/expired/epoch/generation rejection, actual SQL authority failure; reviewed filter/validator and G1/G2. |
| DPA-05–07 | PASS | Genuine switch pair replacement, wrong proof/PIN/client profile/same-profile/unconfigured negatives, controlled rolling lifetime, reset of caller/all ever-PUBLIC devices and PRIVATE-only survival; G2 and retained G1. |
| DPA-08 | PASS | Real PostgreSQL competing refresh/current-versus-predecessor revoke semantics; unchanged previously reviewed source and G1/G2. |
| DPA-09 | PASS | Thirteen observed-lock/held-commit serializations plus retained stale-reset and expiry-under-lock cases; G2. |
| DPA-10–11 | PASS | Already-admitted GET/write/stream completion, new admission and actual propagated ASYNC/ERROR revalidation; signer/commit rollback, lost response and SQL fail-closed cases; G2. |
| DPA-12–13 | PASS | Actual OFF/ON registrations, both genuine PUBLIC origins, exact method/catchall/HEAD/OPTIONS/dispatch denial, full value snapshots/no handler/provider effects; PRIVATE positives in G2 and complete affected PRIVATE G5. Safe error JSON is distinguished from content bytes. |
| DPA-14–15 | PASS | G2 pre-cutover LOGIN callback/direct service/store/ticket no-issuance controls; source-qualified G3 local Google/native Edge 10 and retained LINK/one-use/binding/protocol cases; G1 bootstrap/PIN/redaction plus genuine G2 service authority. No native browser rerun by this reviewer is claimed. |
| DPA-16 | PASS | Actual disposable full backup/restore/invalidation/key rotation above; G4 complete ZIP/security exclusions/version/unknown-table/resource proofs and G2 PUBLIC no-export controls. Operational deployment remains Owner work. |
| DPA-17 | PASS | G2 exact schemas/error/security/Google documentation checks, G4 root envelope/architecture/inventory, G5 Drive inventory and every selected PRIVATE regression. |

## Verification

Both reviewer test commands used `python scripts/run-bounded-command.py --profile normal --timeout-seconds 300 --receipt <fresh-path> -- mvn.cmd -ntp -f backend/pom.xml -DargLine=--enable-native-access=ALL-UNNAMED -Dtest=<explicit-selector> test` from the repository root.

| Fresh reviewer evidence | Actual result |
| --- | --- |
| `R/g2.json`; selector `ProfileSessionIntegrationTest,ProfileSessionConcurrencyIntegrationTest,ProfileRouteAuthorizationIntegrationTest,ProfileMigrationIntegrationTest` | **TIMEOUT / exit 124**, execution 300s, total 301.171s; complete capture and `windows_tree_termination_requested;descendants_terminated`, cleanup complete. Three completed suites recorded **43 tests, zero failures/errors/skips**: migration 6, routes 11, concurrency 26. Their XML was preserved under `R/g2-completed-reports/`. The fourth suite had not completed; its older target XML was excluded. This command is not relabelled PASS. |
| `R/remaining-23b04bb952a04d8c9c685f0c11fb0409/session.json`; selector `ProfileSessionIntegrationTest` | **PASS / exit 0**, **10 tests**, zero failures/errors/skips, **84.250s**, complete capture/cleanup, cleanup not needed. Fresh XML preserved beside this receipt. Only the unfinished class was rerun after actual timeout/owned-process cleanup was confirmed. |
| `R/evidence-audit.json` | All **1,898** implementer final source hashes match; six gate receipt hashes, exact argv hashes, XML hashes/totals/case counts and selected-class source hashes validate. G5 contains exactly **27/27** required classes, no omissions/extras; V1–V28 **28/28** canonical contents match HEAD. |
| Worktree checks | No source/delivered test drift from reviewer entry; `git diff --check` exit 0; empty index and same HEAD. Existing CRLF conversion notices are distinguished from whitespace defects. |

The server restart lost the interactive tool session, not the running watchdog: its actual process tree was inspected and allowed to reach its existing deadline. The factual result is TIMEOUT, with successful owned-tree cleanup; there is no objective deadlock finding. Completed fresh suite evidence plus the remaining class covers all **53 G2 cases**, but does not turn the first whole-command outcome into a pass. Independent acceptance also relies on the validated, source-applicable implementer G2 final whole-command PASS below. No source correction, increased timeout/profile, counter reset or repeated completed suite was needed.

Supplied takeover evidence was independently validated and is reused as implementer evidence, not represented as fresh reviewer execution:

| Receipt under T | Classes / tests | Runner seconds | Result |
| --- | --- | ---: | --- |
| `g1-g4.json` | 16 / 163 (G1 94 + G4 69) | 77.125 | PASS / exit 0 |
| `g2-final.json` | 4 / 53 | 118.203 | PASS / exit 0 |
| `g3.json` | 6 / 60, including native Edge 10 | 64.625 | PASS / exit 0 |
| `g5-a.json` | 8 / 31 | 46.078 | PASS / exit 0 |
| `g5-b.json` | 12 / 21 | 38.578 | PASS / exit 0 |
| `g5-c.json` | 7 / 104 | 114.688 | PASS / exit 0 |

These final disjoint gates total **432 tests**, zero failures/errors/skips, **459.297s** runner time. Every final receipt uses normal 300s, complete capture and cleanup. `T/evidence-manifest.json` maps every selected class/case and receipt to source; G5 is fresh in the takeover, with no inherited R2/R3 class substitution. Only unrelated G2 test assertions changed after the other gates, explicitly qualified in the manifest. Earlier compilation/test failures `focused-1`, `g2-attempt2/3/4` (exit 1) and superseded `g2-attempt5` (PASS) have complete receipts and remain preserved, separately from final totals and the historical R3 evidence. They are implementer iterations within the one authorized takeover, not additional independently reviewed rounds.

Expected injected issuance/constraint/SQL/disconnect error logs are positive failure controls. Maven metadata/dynamic-agent warning categories and existing line-ending notices were reviewed; no new IDE inspection or repository warning-free assertion. No CLI safety hook, trust/config/permission guard failure was encountered or bypassed. No new native fixture, provider installation, live OAuth/Drive call, RAG worker, inference or runtime download was used by this reviewer.

## Shared coverage, scope and hygiene

All 29 dimensions were considered proportionately: **1–2 PASS** scope and finite traceability; **3–6 PASS** accepted architecture, recursive owner-local package structure, descriptors and ADR-0016 root transport, with the specific FR-DPA-09 authority now satisfied; **7–9 PASS** cohesive existing services and narrowly useful test fixture, no speculative production abstraction; **10–17 PASS** reviewed business/wire/validation/privacy/secret/transaction/recovery/database behavior and proofs above; **18 PASS** unchanged bounded/indexed admission and fixed-size authority transitions, no unsupported performance claim; **19–20/23–24 N/A** unchanged disabled RAG/document quality/evaluation and no new benchmark requirement; **21–22 PASS** real adversarial/lock/rollback/dispatch/restore evidence and positive controls; **25 PASS** unchanged dependency/platform inputs; **26 PASS** bounded focused evidence and source-qualified architecture/build gates (optional fresh IDE inspection **NOT_VERIFIED**, not an acceptance requirement); **27 PASS** redacted production error/DTO handling; **28 PASS with LOW backlog** scoped current-state/evidence consistency and DPA-L01; **29 PASS** no new generated tracked cache, duplicate owner/transport layer, frozen rewrite, index mutation or publishing.

## Scope-bounded Docs Sync Check and recovery classification

The last valid scope checkpoint is takeover handback section 11, **Own self-review and Docs Sync Check**. The reviewer checked only its changed/invalidated operational documents, exact referenced contract/review parents and new receipt/source claims; no repository-wide documentation cleanup B was performed.

- **Completed / reused:** prior independent finding closures, byte-identical production and approved design docs; takeover's seven backend-path corrections and finite G1–G5 evidence, verified as above. Original reviews, decisions, failed receipts and historical counters are immutable.
- **Pending at entry, completed here:** independent FR-DPA-06/07/09 adjudication, current review/status synchronization and the remaining focused verification. No new implementation was authorized or performed.
- **Invalidated by drift:** none of the 1,898 final source inputs or 73 reviewer-entry delivery files drifted before status edits. The stale target XML for the unfinished fourth suite was excluded and replaced only by a fresh completed result in a new evidence location.
- **UPDATED:** this separate dated review, ACTIVE current outcome/next action, executable result fields and separate independent disposition in the handback. Historical R3 and implementer submission/self-review claims remain visibly distinct from independent acceptance.
- **ALREADY_CONSISTENT:** scope/Owner exceptions, D1–D6, V29/unchanged V1–V28, strict API/DBML/ADR ownership, no PUBLIC business grants, D3 admitted-work limit, D4 no issuance, defaults OFF, evidence paths/counts/source applicability and no implementation publication. The runbook's dated submission status is superseded for review disposition by this report; its operative recovery contract is unchanged.
- **DEFERRED_OUT_OF_SCOPE:** routine navigation refresh already assigned by the executable handoff to ChatGPT/Owner: root `AGENTS.md`, current notice in `docs/workflow/current-owner-delivery-policy.md`, `docs/roadmap.md`, `docs/implementation/maintenance/README.md` BF26-DP entry and this preparation `README.md`. These still describe the earlier published preparation checkpoint; this bounded final review does not repair them or treat them as new authority. DPA-L01 cosmetic fixture wording remains LOW backlog. No unrelated historical cleanup is authorized.
- **BLOCKED_OWNER_DECISION:** none for DP-A acceptance; the two exact-map exceptions are now expressly authorized. Later DP-B1/B2/C, Recycle Bin, Frontend or audit work still requires its own Owner gate.
- **NOT_VERIFIED:** actual Owner production cutover/deployment and future publication, optional fresh IDE inspection and a fresh reviewer-native-browser run. None is falsely asserted or required as a second execution where the exact source-qualified mandatory evidence is already supplied. No mandatory DPA row remains unverified.

Preserve Frontend pause/H1-F independent non-acceptance/FR-13 upstream blocker and all separate counters, v0.3, Phase 17 freeze/Phase 18 closeout/S19-2 suspension, inactive indexing/disabled live originals and provider defaults. DP-A slice readiness does not close the overall BF26-DP maintenance or authorize follow-on implementation. After Owner publication, perform brief slice closeout; phase/maintenance final review is aggregation only after all separately authorized slices are published.

## Owner next action

Owner may stage/commit/push the accepted BF26-DP-A delivery using exactly this suggested message:

`feat(auth): add fixed profiles and persisted session authority`

No separate staging-review session is required. This reviewer performed no Git staging, commit, push, tag or PR action.

**Next step:** Owner | commit/push the independently accepted delivery, then ChatGPT brief DP-A slice closeout/current-navigation synchronization | BF26-DP-A, this report and [ACTIVE](../../../handoffs/ACTIVE.md) | READY_FOR_OWNER_COMMIT; closeout must use the actual published SHA; no later slice/maintenance-final gate implied | Antigravity 3/3 exhausted; exceptional Codex takeover 1/1 independently accepted; other counters unchanged.

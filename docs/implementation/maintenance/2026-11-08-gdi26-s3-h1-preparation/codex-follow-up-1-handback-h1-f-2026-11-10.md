# H1-F Codex bounded corrective follow-up 1 — implementer handback

**2026-11-10 — CHANGES_REQUESTED / INDEPENDENT_REVIEW_REQUIRED.** The two authorized code corrections are implemented and self-reviewed. Implementation is STOPPED. Mandatory FR-12/PH-I01 native evidence is **NOT_VERIFIED** and FR-13 remains **UPSTREAM_DEPENDENCY_BLOCKED**. This is an implementer report, not independent acceptance, finding closure, permission to publish, or READY_FOR_OWNER_COMMIT.

## Authority, entry and counters

- [New Owner follow-up authorization](owner-authorization-h1-f-codex-follow-up-1-2026-11-10.md), received 2026-11-10; [authoritative takeover 1 independent review](reviews/2026-11-10-gdi26-s3-h1-f-codex-takeover-1-independent-final-review.md); [immutable executable](executable-handoff-h1-f-2026-11-09.md); [ACTIVE](../../handoffs/ACTIVE.md).
- Local HEAD remains **8e8b6c56d7cdc5895634a0fdcba2d2ae6aac7b84**. The entry tree already contained the reviewed, uncommitted frontend and governance work. The index was and remains empty; this follow-up does not assert a clean tree or a new publication.
- **Antigravity 3/3 exhausted / STOPPED; Codex takeover 1 independently blocked / STOPPED; separate Codex bounded corrective follow-up 1 authorized and executed / STOPPED / independent acceptance pending.** H1-B/H1-AUTH unchanged. Test/tool iterations do not increment these counters.
- Evidence root **`F = logs/gdi26-s3-h1-f/cf1-mwan14ae-b48f65/`**. Prior independent evidence **`R = logs/gdi26-s3-h1-f/codex-independent-takeover-review-acd6deee3f434ce385a4d38cbc725403/`**; prior takeover evidence **`E = logs/gdi26-s3-h1-f/codex-takeover-17e97e4740d3452d8dbdf0b4cb595d7c/`**. All new receipts and artifact directories are distinct; none replaces an earlier file.

### Recovery classification

The current parent files, original executable, Owner policy, root/scoped AGENTS, latest independent review, COMMON resume policy and Docs Sync contract were read. The valid scope-specific checkpoint is the takeover 1 independent review's Docs Sync Check and `R/final-review-check.json`; `F/entry-manifest.json` independently captured the new follow-up entry before implementation.

| Classification at entry | Result / subsequent action |
| --- | --- |
| Completed, preserved | Prior independently CLOSED FR-01–07/10/11/14 and accepted F-05 desktop assessment. Reviewed frontend and compiled backend inputs had zero drift from the review entry. |
| Pending, newly authorized | FR-08/09 local state corrections and before/after regressions; FR-12 compliant native observation attempt; new handback/self-review and current status synchronization. |
| Invalidated by this correction | Earlier exact-byte Explorer/folder behavior claims. They require the fresh regressions below. Prior zoom/visual evidence is qualified reuse, not a current-byte zoom run. |
| Blocked | FR-13 already has the Owner's upstream-wait decision. FR-12 later remained NOT_VERIFIED after the permitted native client failed. No new port, design, install or security authority was inferred. |

No interrupted test was treated as success or blindly rerun. Every rerun followed an actual exit/owned-cleanup result and used a fresh receipt/artifact path. There is no fourth Antigravity round or repository-wide documentation cleanup B.

## Exact changed implementation

Seven existing frontend files changed relative to the captured entry; no frontend files were added:

- `frontend/src/components/drive/explorer.tsx`: append admission is owned by its request's `AbortController`. Explicit replacement clears obsolete admission, resets append loading, aborts old work and retains generation/view/cursor fencing. A completion clears admission only if it still owns that admission. It cannot clear a newer request's owner. Failed cursors remain blocked until explicit restart; no automatic 409 replay was added.
- `frontend/src/components/drive/folder-dialog.tsx`: only canonical HTTP 409 `FOLDER_KEY_EXPIRED` or `FOLDER_FAILED` clears the protected uncertain intention. The current name/key is retained at that transition; an explicit name edit then starts a new key, and a later explicit submit dispatches it. No automatic create/retry/new-key dispatch. Unknown, pending, conflicting and other nondefinitive outcomes retain the original intention.
- `frontend/tests/components/explorer.test.tsx`, `frontend/tests/components/secondary-states.test.tsx`: seven focused cases added, including the stronger late-obsolete-completion/new-owner counterexample and nondefinitive 409 controls.
- `frontend/tests/e2e/explorer.spec.ts`: two real Edge/Next/Spring/PostgreSQL recovery cases added.
- `frontend/tests/e2e/accessibility.spec.ts`: requires uncropped permitted native capture confirmations; extends the intended keyboard/dialog evidence. Those new zoom-stage assertions were **not reached** in the failed native attempt. Ordinary first-Tab setup now waits for the Drive heading and network idle after reload, keeping the strict first-Tab assertion.
- `frontend/README.md`: directly affected recovery semantics, evidence pointers and remaining gates synchronized.

The seven paths above count the two component-test files individually and the two native-test files individually. Governance changes are limited to ACTIVE and the three new dated authorization/handback/self-review documents. Original executable, Owner notes, old handbacks/self-reviews, reviews and evidence are immutable history.

### Canonical definitive rejection, without backend changes

Read-only inspection of `DriveChildFolderIntentStore.take` establishes that absent stale intent is rejected by the fresh-key check before intent insertion/provider work; canonical `FOLDER_FAILED` corresponds to `FAILED_DEFINITIVE`. An existing unknown/pending intent is not equated with expiry or an arbitrary 409. This correction changes the client transition only, preserving the existing Spring authority and persistence/idempotency contract.

## Counterexamples reproduced before and after

The controlled reproduction assertions were copied into ignored F paths; old R assertions/raw observations remain intact. Only fresh output paths and a strict alert selector excluding the empty Next announcer were adapted for the native reproduction. Positive provider/BFF/Spring responses were not fabricated.

| Evidence | Before correction | After correction |
| --- | --- | --- |
| Controlled component assertions | `F/component-before.json`: exit 1, **2 FAIL**. Restart issued two first pages but only one cursor request; the expired-key result left the field readonly. | `F/component-after.json`: exit 0, **2 PASS**. Fresh cursor dispatch occurs; definitive expired-key rejection permits an explicitly entered new intention. Both observations/report JSON files are retained. |
| Actual native reproduction | `F/native-before3.json`, artifacts `F/nb3/`: exit 1, **2 FAIL**. Seven real files stalled at five after explicit restart; interrupted-before-dispatch then actual 409 FOLDER_KEY_EXPIRED left the field readonly with zero remote creations. | `F/native-after.json`, artifacts `F/na/`: exit 0, **2 PASS**, same reproduction assertions. App source is qualified through saved pre-format inputs below. |
| Stronger committed regressions | The old-owner/late-finally and explicit-new-action sequence are exercised by the new focused tests. | `F/native-focused-01.json`, artifacts `F/nf/`: **2 PASS**; repeated in the final ten-case Explorer group, not added to its unique total. |

FR-08's new component case holds append A even after abort, starts replacement append B, resolves A while B is pending, and invokes duplicate observer events. It proves A cannot repaint or release B's admission; then B completes and all seven files are visible. The native case creates seven actual files, aborts only the held old negative request, uses the real replacement first page of five and continuation of two, and verifies exactly two total cursor requests with no automatic replay.

FR-09's native case interrupts before dispatch, verifies zero provider creations, advances only the isolated Spring fixture clock 601 seconds, retries the original key, receives actual **409 FOLDER_KEY_EXPIRED**, and verifies the field unlocks without a third dispatch. Only after an explicit new name and submit does a different key receive **201**, with exactly one remote folder creation. `page.clock.setFixedTime` aligns only this new browser action's timestamp with the deliberately advanced fixture clock; no production clock/freshness rule or Windows clock changes. Component controls separately retain protection for FOLDER_OUTCOME_UNKNOWN, FOLDER_IN_PROGRESS, FOLDER_KEY_CONFLICT and DRIVE_STALE_AUTHORITY.

## Focused verification — actual results

Every potentially long command used existing `scripts/run-bounded-command.py --profile normal --timeout-seconds 300`, selected portable Node, a fresh receipt and captured stdout/stderr. No install, audit rerun, force flag, dependency change, extended timeout or Java-only substitute for browser proof.

| Verification | Result / exact receipt beneath F |
| --- | --- |
| Entry/hash capture | PASS, `entry-receipt.json`; 3,197 tracked, 116 frontend, 2,281 compiled backend and 1,198 historical-evidence hashes. |
| Affected components | PASS **17 tests / 3 files**, `affected-components-01.json`. These repeat part of the final ordinary total. |
| All ordinary frontend tests | PASS **87 / 16 files**, `tests-final.json`: **40 unit + 23 component + 24 BFF**. Seven new component cases; preceding 80 are not added again. |
| Current strict type/lint/format | PASS, `typecheck-final.json`, `lint-saved-final.json`, `format-saved-final.json`; includes the final a11y readiness edit and README. |
| Production Next build | PASS, `build-final.json`; final application bytes. Later differences are test/README/generated incremental metadata only. |
| Current Explorer native regressions | PASS **10/10**, `native-explorer-regression.json`, artifacts `nr/`: new two recovery cases plus existing eight cursor/slot/search/filter/child regressions; no skipped/flaky cases. |
| Ordinary native a11y | PASS **5/5**, `native-accessibility-regression2.json`, artifacts `ar2/`: login/setup axe, strict first-Tab skip link, emulated 320-CSS-px reflow and populated desktop keyboard/error/focus/reduced-motion cases at 1366/1536. Emulated width is not native zoom proof. |
| Mandatory actual native zoom | **FAIL / NOT_VERIFIED**, `native-zoom-01.json`, artifacts `nz/`; **0 PASS / 1 FAIL**, no skipped case. Exact client failure below. |
| Source/scope/pins/cleanup | PASS, `source-check2-receipt.json` / `source-check2.json`. Full hashes, native source qualifications and complete preceding receipt ledger in `source-check2-receipts.json`. Final saved-document check is `saved-docs-receipt.json` / `saved-docs-check.json`, with final source inventory `final-state.json`. |

These are suite-specific results, not a fictional full-native green aggregate. The final successful focused native groups contain **15 distinct cases (10 Explorer + 5 ordinary a11y)**. The controlled reproductions and earlier focused runs are separate repeated evidence. PH-I01 remains mandatory and unverified.

### Retained failures and warnings

- `native-before.json`: custom reproduction runner's CJS import did not expose chromium, before fixture launch. `native-before2.json`: Windows absolute test selector selected no tests; owned fixture cleanup completed. `native-reproduction-v3.mjs` uses the correct import and basename selector. Both failed runner versions/logs remain.
- `component-before.json` and `native-before3.json`: intended failing counterexamples on captured pre-fix app bytes; not setup failures or new remediation rounds.
- `build-after-01.json`: TypeScript TS2769, unsupported RTL `exact` option in the new test. Replaced with the exact-name regex; `build-after-02.json` and final build pass. No production workaround.
- `native-accessibility-regression.json`: first skip-link Tab after reload was not focused; **4 PASS / 1 FAIL** retained. The loaded-page wait described above precedes the identical assertion; the second five-case run passes. A hydration/root-cause diagnosis is not claimed from this alone.
- `native-zoom-01.json`: missing mandatory permitted native capture, described below; it is not waived by the five ordinary a11y successes.
- `source-check-receipt.json`: audit helper tried to parse its own still-open watchdog receipt. It exited 1 with no mutation. Preserved original helper; `source-audit-v2.py` excludes only its explicitly named current receipt, validates all preceding completed receipts, and passes in the fresh second audit.
- Unchanged fixture startup emits Java restricted `SymbolLookup.libraryLookup` warnings and a SpringDoc API-docs warning; Node emits the existing NO_COLOR/FORCE_COLOR warning. Raw streams retain them. No warning-free IDE/native/backend claim or suppression.

## FR-12 / PH-I01 precise native-client blocker

The actual headed Edge fixture launched with native viewport/DPR emulation removed. `F/nz/baseline-requested.json` records baseline **innerWidth 960, DPR 2.5, outerWidth 978**, planned URL `http://localhost:3000/personal/storage/google-drive`. This is test metadata, not proof that the Computer Use client observed a trustworthy URL.

Using the documented Windows Computer Use `@oai/sky` client, `list_windows` returned the uniquely owned `PPV PH-I01 actual native zoom` Edge window. `get_window_state` returned **`computer-use request timed out: get_window_state`**, with no screenshot or verified URL. The one documented recovery refreshed the window inventory; it was empty after the waiting fixture had closed, yielding **`Owned zoom window unavailable during the one documented capture recovery`**. Exact raw MCP results/flags are retained in `F/native-client-diagnostics.json`.

The [Computer Use skill](C:/Users/ADMIN/.codex/plugins/cache/openai-bundled/computer-use/26.1103.60933/skills/computer-use/SKILL.md) points to its [recovery guidance](C:/Users/ADMIN/.codex/plugins/cache/openai-bundled/computer-use/26.1103.60933/docs/guidance.md): “Refresh the app/window selection and retry once; report the exact error if recovery fails.” UI work stopped at that boundary. No native zoom input was sent, no positive confirmation marker or fabricated screenshot written, and no alternate client/trust/hook/permission/security-policy change was used.

The browser assertion consequently failed **`Mandatory policy-compliant uncropped native capture unavailable: baseline`**. No actual 200% or effective 320-CSS-px complete viewport/dialog/keyboard/error/reflow proof was obtained in this follow-up. The original review's URL-confidence guard has **not** been demonstrated resolved: this attempt failed earlier at state capture. It proves neither a fresh zoom PASS nor an application overflow defect. A working permitted client is needed for the remaining mandatory evidence; it does not authorize an application/security workaround.

## Source qualification, reuse and cleanup

`F/source-check2.json` and final `F/final-state.json` inventory all current frontend SHA-256 values, actual selected Node hash/version, all 32 installed exact direct versions, each native manifest/fixture/reproduction hash and source differences since execution. Their source checks establish:

- All **59 app inputs** are unchanged from reviewed entry except the two authorized components. **57 unchanged app inputs** preserve the reviewed BFF/session/provider/transfer/guard/style boundaries. Package/lock/selected dependency versions, backend source/config/tests/build and compiled inputs, .agents/.codex/hooks/trust and approved v0.3 remain unchanged. No dependency installation/security repair was performed.
- `nb3` app hashes match `F/entry-sources/`. `na` and `nf` app hashes match `F/pre-format-current/`; formatting those saved two app files with the selected Prettier/config exactly equals current bytes. These are format-qualified early post-fix results. Final `nr` and `ar2` manifests match all current app/build inputs exactly. Later README/a11y-test/incremental-tsbuildinfo changes are explicitly listed rather than concealed as exact complete-manifest matches.
- All **2,281 compiled backend**, **1,198 captured historical evidence** and **12 protected dirty canonical-document** hashes remain intact; the latest independent review additionally matches its entry copy. No old derived JSON or lost original bytes are reconstructed or declared recovered.
- The unchanged fixture `E/fixture-11` is reused after the runner verifies its Java source manifest; no recompile/backend change. Actual controlled PostgreSQL image remains `ppv-rag-postgres:18.6-pgvector0.8.6`, Java 25, Node **26.13.0**, native Edge **157.0.4341.42**, Spring **19080**, actual production Next **3000**. No live Google, proxy or unrelated service manipulation.

Prior independent review evidence remains **reused**, separately qualified: 20 sign-in/Next-ingress, 4 setup, 15 Drive OAuth, 9 sessions/races and 15 distinct transfer successes; nine preceding controlled boundaries; accepted desktop v0.3 assessment and 18 rendered contrast samples. Their source-owning BFF/security/transfer code is unchanged. They are not fresh executions here. The earlier 8 Explorer/5 a11y cases are replaced for current affected coverage by the fresh groups above, not added. Old zoom geometry/records remain cropped/source-qualified historical evidence and cannot close FR-12. The earlier audit still reports ten moderate propagated entries from one minimatch advisory; no new audit or upstream-patch claim is made.

Every completed F watchdog receipt reports capture and cleanup complete. Each launched native group saved `cleanup.json`: Spring exit 0, owned Next termination exit 1, owned paths removed. Source audit additionally verifies owned temporary paths and recorded PostgreSQL containers absent, zero remaining Playwright Edge profiles, and actual IPv4/IPv6 bind/listen/close availability on 3000/19080. The Next exit is owned fixture termination, not a passing application-exit assertion. No unrelated process was stopped.

## Exact remaining finding register

The authoritative independent dispositions are preserved until a separate reviewer adjudicates these changes:

| Finding | Current disposition | Remaining gate |
| --- | --- | --- |
| FR-01/02/03/04/05/06/07/10/11/14 | **CLOSED**, preserved | Unchanged accepted scope; no self-reopening or fresh closure claim. |
| FR-08 | **MEDIUM / OPEN — CODE_CORRECTED_PENDING_INDEPENDENT_REVIEW** | Independently inspect admission ownership and seven-file/late-completion regressions. |
| FR-09 | **MEDIUM / OPEN — CODE_CORRECTED_PENDING_INDEPENDENT_REVIEW** | Independently inspect canonical definitive transition and interrupted-request/expired-key/new-action evidence. |
| FR-12 / PH-I01 | **MEDIUM / mandatory NOT_VERIFIED** | Working policy-compliant native client and complete uncropped actual 200% / effective 320-CSS-px usability proof. |
| FR-13 | **MEDIUM / OPEN / UPSTREAM_DEPENDENCY_BLOCKED** | Existing Owner wait for a supported compatible upstream patch. No pins/lock/install/waiver or repeated decision request. |
| H1F-L01 | **LOW / OPEN / DEFERRED** | Preserve R2 derived-JSON integrity history for authorized later disposition; no recovery/closure asserted. |

Even if FR-08/09 were independently accepted, FR-12 and FR-13 prevent H1-F acceptance/publication. F-01–07 are not declared fully accepted. F-04/PH-I01 and the F-07 dependency-security gate remain incomplete; F-03 affected recovery corrections await independent review. F-05's accepted desktop assessment is preserved without a material redesign or new visual approval.

## Scope-specific Docs Sync Check

- **UPDATED:** current ACTIVE status/authorship/result/counters/next step; this handback; [implementer self-review](codex-follow-up-1-self-review-h1-f-2026-11-10.md); the new Owner authorization; directly affected frontend README. These supersede only the current follow-up state; historical assertions retain dated labels.
- **ALREADY_CONSISTENT:** original executable/allowlist/finite gates, all old Owner authorizations/handbacks/reviews, accepted publications and CLOSED findings, standing 19080 authority, approved v0.3, selected pins and normal disabled-provider/Drive/Feed/RAG boundaries. Hash checks qualify preserved source/evidence rather than reopening unrelated docs.
- **NOT_VERIFIED:** mandatory FR-12/PH-I01 native usability evidence; independent acceptance of FR-08/09. No simulated test, browser-cookie substitution, blanket a11y certification, full-suite or fresh visual PASS.
- **BLOCKED_OWNER_DECISION:** none newly requested. FR-13 already has the Owner's **upstream-wait** decision; its unresolved security gate remains blocking. Any future security/client repair or broader correction needs its own express authority. Current instruction authorizes reporting the native failure, not bypassing it.
- **DEFERRED_OUT_OF_SCOPE:** H1F-L01/H0 FR-L01/preparation PH-L02, historical root/policy/package-tree banners, repository-wide cleanup B, mobile/general Phase 16, live providers/deployment/RAG/frozen phases. No changes, activation or closure inferred.

All saved current documents are reopened and their local references/current-versus-historical status checked in `F/saved-docs-check.json`; source/index/diff and owned cleanup are separately recorded in `F/final-state.json`. The original executable and review counters are preserved. The [own-work self-review](codex-follow-up-1-self-review-h1-f-2026-11-10.md) discloses both blockers and is not an Antigravity review or independent Codex acceptance.

**Next step:** Owner opens a separate Codex session | `$codex-final-review` | GDI26-S3-H1-F corrective follow-up 1, ACTIVE + this handback + takeover 1 independent review | independently verify FR-08/09; FR-12 NOT_VERIFIED and FR-13 upstream-blocked prohibit READY/publication; implementation STOPPED | Antigravity 3/3 STOPPED; takeover 1 independently blocked/STOPPED; corrective follow-up 1 executed/STOPPED, independent acceptance pending.

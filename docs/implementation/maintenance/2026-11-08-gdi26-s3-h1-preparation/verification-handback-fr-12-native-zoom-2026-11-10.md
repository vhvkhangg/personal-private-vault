# FR-12 / PH-I01 full native zoom verification handback — 2026-11-10

**FR-12/PH-I01 — NOT_VERIFIED.** One authorized full attempt ran and stopped. Actual native **200% and 300% indicators were observed**, and complete visible native window frames were captured after fitting the window vertically. The 300% dialog-error observation was not acknowledged within its 30-second gate. The later 300% action, Escape and focus-restoration evidence was not obtained. This is incomplete verification evidence, not a demonstrated application defect or a closed finding.

**Actual command:** COMMAND_FAILED / exit 1 / **265.891s**, normal 300-second cap, capture/cleanup complete. **Actual PH-I01:** 0 PASS / 1 FAIL, **226.433s** of a 240-second case limit. Error: **Mandatory policy-compliant uncropped native capture unavailable: dialog-error-300**. No get_window_state timeout, URL-confidence rejection, client reset, recovery capture or fixture relaunch occurred.

## Authority and unchanged boundaries

The Owner's “FR-12 Full Native Zoom Verification” message authorized this single evidence attempt following the [successful baseline-only observation](fr-12-baseline-native-observation-2026-11-10.md) and [read-only diagnostic](fr-12-native-client-diagnostic-2026-11-10.md). Inspected controlling sources include repository/scoped governance, [Owner policy](../../../workflow/current-owner-delivery-policy.md), [ACTIVE](../../handoffs/ACTIVE.md), the [original H1-F executable](executable-handoff-h1-f-2026-11-09.md), installed Computer Use API/guidance and the [current independent final review](reviews/2026-11-10-gdi26-s3-h1-f-corrective-follow-up-1-independent-final-review.md).

This is an evidence handback, not implementation, remediation, takeover or independent final review. Local HEAD remains **8e8b6c56d7cdc5895634a0fdcba2d2ae6aac7b84**; index empty, existing dirty worktree preserved. No remote publication or dependency-security query ran.

**V** = [logs/gdi26-s3-h1-f/zoom-verify-mwav1fic/](../../../../logs/gdi26-s3-h1-f/zoom-verify-mwav1fic/), the new unique ignored evidence directory. **N** = V/native/. Original failed/partial images, raw errors, stage files and late confirmation remain intact.

The single fixture command was:

    python -B scripts/run-bounded-command.py --profile normal --timeout-seconds 300 --receipt logs/gdi26-s3-h1-f/zoom-verify-mwav1fic/native-receipt.json -- logs/gdi26-s3-h1-f/tools/node-v26.13.0-win-x64/node.exe frontend/scripts/run-e2e.mjs --group=accessibility --filter=PH-I01 --artifact-dir=logs/gdi26-s3-h1-f/zoom-verify-mwav1fic/native --fixture-dir=logs/gdi26-s3-h1-f/codex-takeover-17e97e4740d3452d8dbdf0b4cb595d7c/fixture-11

The existing runner's short-lived version-preflight browser is distinct from its one headed PH-I01 instance. No second fixture, source/test/fixture/harness/timeout change, installation, dependency/lock change, security/trust/hook/permission repair, backend/database/mobile/RAG change, staging or publication occurred.

## Preconditions and provenance

[V/preflight.json](../../../../logs/gdi26-s3-h1-f/zoom-verify-mwav1fic/preflight.json) records actual IPv4/IPv6 bind/listen/close availability on 19080/3000, **32 exact installed direct pins**, approved cached image identity, selected Node and Java, fixture hashes and all four normal provider/Drive/Files/Feed flags **UNSET**.

[V/entry-qualification.json](../../../../logs/gdi26-s3-h1-f/zoom-verify-mwav1fic/entry-qualification.json) independently compares all **125 source/build/compiled-fixture inputs** from the successful baseline run: **zero changes**. Its recorded tool hashes, including selected Node/npm/Java/Docker/Playwright files, also match; actual Edge binary matches the baseline fingerprint. No compile/build/install was needed.

Selected versions: **Node 26.13.0 / npm package 12.2.2 / Edge 157.0.4341.42 / Playwright 1.65.0 / Next 16.4.5 / Java 25 / Docker Server 29.9.2**. N/native-toolchain.json records actual Node/Edge runtime identity. npm was not invoked for the native command.

[V/entry-manifest.json](../../../../logs/gdi26-s3-h1-f/zoom-verify-mwav1fic/entry-manifest.json) preserves per-file source/compiled/history/installed-client-documentation hashes. N/source-hashes.json is the runner's exact new native input inventory; V/entry-qualification.json contains the five reused fixture hashes. Important unchanged inputs:

| Input | SHA-256 |
| --- | --- |
| frontend/tests/e2e/accessibility.spec.ts | dae64254f812487e9f8c948fe08fb0b05d20d6535b7b15f7f08717f2f50f4f8f |
| frontend/scripts/run-e2e.mjs | 3b455e44125080775acff21d3075915c2d6a000e10c0ecec2f55052dce497f40 |
| frontend/scripts/fixture.mjs | c517f0d4100f99405f26d4e021e9ed5d108ac87f7a238cc63d72536667dfe908 |
| frontend/playwright.config.ts | 349c514f10a416b6a313e9682917ae75ef2401ba46b0d74dc9d963e0f1045178 |
| frontend/package-lock.json | a83a113382c3ef1ac239af483814262c01b77e86758061e7027d2ae6562f50a2 |
| Selected node.exe | 14319838b3f355e0398c13569a90270bce4fc68f25189f58c59b03dadae0e6d5 |
| Installed msedge.exe | d258e512aa24947cfbef0c0033d8c3cd56ecb40f7b2ad59802fa75c4e4f3324d |
| Compiled H1FLocalFixture.class | ffdfab10d52d4dd182d125f6c2d4e3ee5bbbc7c019155d96d71c24922dbf0f0e |
| Compiled H1FFixtureConfiguration.class | c0a3dff9c4da7d27a555f4ad5c49f38a8253064092cf4cc3f8f378218c589fe5 |
| Compiled H1FFixtureConfiguration$1.class | 6794d8b9f27f1888d460e0d2a5e985d9c454de0c489b5ccaa8ed5148b01a84a3 |
| fixture-classpath.txt | b3f9f9fb289a18349a8968ed5259b7359a3af7f3ce4d57b4bf45babcb102a61a |

The PostgreSQL image was the existing **ppv-rag-postgres:18.6-pgvector0.8.6**, image ID **sha256:2c85c6e401bdbb4ec4ee59e11a7b15fdba5aaad787c0a69295d8ce91e004aa8c**. Owned container **30b4dffa808bfa77ac41e9b0c0a384cbe4afb4be965e2c7b7f8f22cccff9f7b1**, fake-provider ports and temporary paths are in N/fixture-metadata.json. No image/runtime download or live provider activation was introduced.

The documented @oai/sky client was initialized before launch. Client documentation version/path and hashes are in the entry manifest; no native helper binary version is inferred from the plugin directory or obtained through an alternate protocol.

## Supported capture method, ownership and full-frame assessment

The installed API offers screenshot-first get_window_state with bounded screenshots and origin/size metadata. It provides no documented “force full-window” parameter or independent final-window bounds field. Prelaunch [V/observation-plan.json](../../../../logs/gdi26-s3-h1-f/zoom-verify-mwav1fic/observation-plan.json) therefore required direct inspection of complete frame edges, browser chrome/address and viewport, with no crop/reconstruction/alternate capture.

Fresh list_windows returned exactly one task-specific MSEdge candidate:

    app: MSEdge
    id: 1182126
    title: PPV PH-I01 actual native zoom - Personal - Microsoft Edge

The raw title includes the same zero-width character in its suffix as earlier runs. Actual returned objects were used for get_window; no guessed or reused historical handles. All successful capture results retained the same app/id/title, establishing availability across the observed stages. No complete continuous HWND/PID trace was recorded.

The first screenshot was **1212×850, origin (111,100)**, with the prior lower/taskbar obstruction. One documented Computer Use click on the **observed top resize border**, click_count 2 at window-relative (600,1), fit the native window vertically. The resulting screenshot was **1212×852, origin (111,0)** and visibly showed top chrome/address, both lateral edges and the lower browser/viewport boundary without taskbar overlay. Width was not altered by a harness change. Runtime progression through later geometry checks supports preservation of the intended width; exact final numeric bounds were not separately exported.

All later principal images retained that full visible frame. This is direct visual coverage of the native window/viewport, not a claim that an entire vertically scrollable document or dialog fits at once. The API still supplies bounded regions rather than an independent “uncropped” certificate. Original initial partial and obstructed images are retained alongside subsequent unobstructed evidence.

## Actual native inputs, response provenance and URL policy

Native inputs were confined to documented sky APIs:

- One double-click on the observed top resize border to fit the frame vertically.
- Control_L+0 followed by **seven separate Control_L+plus actions**, each followed by fresh screenshot-first state. Observed indicators were **110%, 125%, 150%, 175%, 200%, 250%, 300%**. The first reset refresh briefly showed an incompletely rendered content area; the following observations returned populated content. No persistent rendering defect is inferred from that transient image.
- One click on observed neutral browser chrome at (600,25), followed by a fresh state, to remove a tab hover card obscuring part of the 300% dialog image. This did not dismiss the dialog or change zoom.

All captures requested include_screenshot true / include_text false. No accessibility text was needed; no indiscriminate combined captures, client reset or recovery were used. The unchanged test performed its existing real browser keyboard/menu/dialog traversal. No new Playwright/CDP interaction substituted for native observation or browser zoom.

[V/native-response-provenance.json](../../../../logs/gdi26-s3-h1-f/zoom-verify-mwav1fic/native-response-provenance.json) indexes **15 original serialized tool-response payloads**, including image blocks and the final “Next marker unavailable” error. They are preserved under V/native-original-responses/ without image decoding, cropping, stretching, editing or reconstructed coverage. The initial partial capture was delivered through a different tool-response type and is retained separately as V/additional-original-response-1.json with V/additional-response-provenance.json. There are **16 retained response payloads** in total. The source session is C:/Users/ADMIN/.codex/sessions/2026/11/10/rollout-2026-11-10T20-34-53-01a1c5ab-3257-72d2-8cef-de6813890646.jsonl, relevant lines **438–532**. Provenance hashes describe normalized UTF-8 source-event text; Windows text-mode copying changes the terminal JSON newline. V/native-response-file-integrity.json records the actual saved-file SHA-256 values and this distinction. Image block content is unchanged; no claim that the two newline representations have identical file bytes is made.

Actual native address bars showed **localhost:3000/personal/storage/google-drive**. No URL-policy rejection occurred. **Internal URL-confidence score/trace: NOT_DETERMINED**; capture success is not an invented internal confidence value. No trusted URL is inferred solely from marker contents, title or Playwright metadata.

## Timeline and shared-budget outcome

Times are UTC, 2026-11-10. Marker mtimes corroborate stage timing; response and receipt timestamps are preserved independently.

| Time | Actual event |
| --- | --- |
| 15:44:32.102 | Outer normal/300s command starts, runner PID 25080. |
| 15:45:06.304 | Spring startup complete; stdout records fixed 19080, control 63979 and subsequent Next ready in 304ms. |
| 15:45:08.979 | PH-I01 case start, 240-second limit. |
| 15:45:17.965–21.572 | Baseline marker arrives; fresh unique window selection and first capture complete. |
| 15:45:30.563–32.500 | Native vertical frame-fit input and complete-frame capture. |
| 15:45:59.119 | Baseline acknowledged, 41.15s after marker; approximately 50.14s after case start. Planning target 45s was missed, though stage/case time remained. |
| 15:46:16.210–52.064 | Five separate native zoom-in inputs/refreshes show 110/125/150/175/**200%**. |
| 15:47:10.897 | Genuine 200% indicator/frame acknowledged, approximately 121.92s into case. Target 105s was missed. |
| 15:47:13.961 | Native 200% dialog-error capture: readable error and input focus outline. |
| 15:47:30.339 | Native 200% action capture: complete create action, cancel, readable error, focus outline; internal vertical scrolling exposes the action. |
| 15:47:41.212 | Native 200% focus-return capture after test Escape: dialog closed, Mới focus outline visible. |
| 15:48:00.120 | Genuine 200% focus-return acknowledged; next 300% stage begins. |
| 15:48:01.066 / 10.092 | Separate native inputs show **250% / 300%**. |
| 15:48:21.817 | Genuine 300% indicator/frame acknowledged, approximately 192.84s into case. Target 160s was missed. |
| 15:48:24.660–25.056 | Actual dialog-error-300 marker and first native capture. A tab hover card obstructs the top-left area. |
| 15:48:48.341–48.841 | Neutral-chrome click removes hover card; unobstructed native 300% input/error/action-area image obtained. Approximately 219.86s into case, after the 210s evidence target. |
| Approximately 15:48:54.660 | Existing 30-second dialog-error-300 confirmation deadline. |
| Approximately 15:48:55.412 | Case ends failed at that gate; calculated from result startTime + 226,433ms. |
| 15:48:56.820–57.736 | Spring graceful shutdown and harness cleanup complete. |
| 15:48:58.005 | Outer command exits 1 after 265.891s. No watchdog expiry. |
| **15:48:59.113** | **dialog-error-300-confirmed.json written late**, after stage expiry and command completion. It reflects a genuine earlier observation but earns **no completed-gate PASS**. |
| About 15:49:02 | Observer's bounded wait for dialog-action-300 marker ends “Next marker unavailable”; no new native capture executes in that call. UI work stops. |
| 15:50:38.488 | Independent read-only cleanup/source integrity check complete. |

[V/stage-audit.json](../../../../logs/gdi26-s3-h1-f/zoom-verify-mwav1fic/stage-audit.json) records each actual request/confirmation and timeliness. Baseline, zoom-200, dialog-error-200, dialog-action-200, focus-return-200 and zoom-300 confirmations were timely for their existing stage waits. **dialog-error-300 confirmation delay was 34.452912s**, exceeding its 30s wait, and its timestamp is after command completion. The file is preserved unchanged and explicitly disqualified as proof of a passed stage. No dialog-action-300 or focus-return-300 request/confirmation exists.

**Confirmed immediate execution failure:** the observer's acknowledgement missed the existing stage deadline. Native capture itself completed; no native timeout or policy rejection occurred. Human/model inspection and tool-turn coordination consumed too much of the shared schedule. The planning targets were not met; the late acknowledgement did not reopen a completed fixture. No second attempt or timeout increase followed.

## Mandatory PH-I01 requirement disposition

PASS below is limited to directly evidenced subrequirements; it does not close FR-12 or convert the failed suite into an aggregate PASS.

| Required behavior | Disposition and exact supporting evidence / gap |
| --- | --- |
| Actual browser zoom at **200%** | **PASS.** Native input chain and displayed 200% indicator in original response, plus complete visible frame after fit. Not inferred from DPR. Observer zoom-step 5 and zoom-200-inspected; original response index. |
| Actual browser **300%** zoom | **PASS for the zoom-level subrequirement.** Separate native steps from 200→250→300 and visible 300% indicator; no emulated width substitute. Observer zoom-step 7 and zoom-300-inspected. |
| Actual effective **320 CSS px** and retained baseline/changed geometry | **NOT_VERIFIED as a complete archived measurement package.** N/baseline-requested.json directly records inner 960 / DPR 2.5 / outer 978. Source-qualified execution reached the later dialog-error gate, supporting that existing inner-width 320±1, DPR-ratio and overflow assertions were traversed without an earlier failure. However, the test writes actual-zoom-observations.json only after both loops finish; that raw geometry/keyboard record was never written. Exact 200%/300% measurements are not reconstructed from screenshots or invented from expected values. |
| Explorer controls and usable reflow, no lost actions | **NOT_VERIFIED for the complete required coverage.** Native images show reflowed account/quota/filter/date/New controls across stages; existing keyboard traversal progressed to both dialogs. Complete per-control geometry/focus evidence and all 300% action completion are missing. Truncated placeholder text or normal vertical scrolling alone is not classified as a demonstrated defect. |
| Keyboard navigation and visible focus | **NOT_VERIFIED overall.** At 200%, direct native input/error focus, fully visible focused create action and Mới focus restoration are observed. The unchanged test traversed account/search/type/date/menu before its error gates. The final keyboard worksheet was not saved, and corresponding 300% action/restored-focus evidence is absent. No claim that every required control's focus was independently inspected. |
| Dialog/menu/error/Escape/focus restoration | **NOT_VERIFIED overall.** Native 200% error/action/Escape-return evidence is preserved; 300% error is visible and readable after hover-card removal. The actual intermediate menu was not separately captured, and 300% action/Escape/focus stages were not reached. |
| No unintended page two-dimensional scrolling or unreachable controls | **NOT_VERIFIED for complete acceptance.** Source-qualified prior assertions support bounded document/main overflow at both zooms and visible action bounds at 200%; native 200% action is reachable. Full saved geometry and 300% action/reachability/focus evidence are missing. No application overflow or lost action is demonstrated by this failed gate. |
| Complete native viewport/dialog evidence sufficient for independent adjudication | **NOT_VERIFIED for the entire mandatory sequence.** Full visible native frame achieved at baseline and later captures; original partial/obstructed versions retained, not edited. Required remaining 300% action/restoration frames and full raw measurement worksheet are absent. |

**No demonstrated application defect is assigned FAIL.** The command/test result is FAIL because of missing timely observer confirmation. Mandatory incomplete evidence remains NOT_VERIFIED and acceptance-blocking.

## Cleanup, scope integrity and evidence preservation

[V/cleanup-integrity.json](../../../../logs/gdi26-s3-h1-f/zoom-verify-mwav1fic/cleanup-integrity.json), N/cleanup.json and V/native-receipt.json record:

- Owned Next exited 1, Spring exited 0, owned temporary paths removed. Test finally closes its own headed browser before the runner cleans servers.
- Known runner/Java PIDs 25080/7236 are gone. A conservative CIM cross-check found **no surviving msedge/node/java process created during the fixture interval**. No full pre-cleanup browser/Next PID-tree snapshot or direct HWND/PID mapping was captured; those limits are explicit.
- Both 19080/3000 bind/listen/close checks pass on IPv4/IPv6 after cleanup.
- Exact owned PostgreSQL container inspect returns **error: no such object**; all three owned temporary paths are absent. No unrelated process/container was terminated by diagnostic collectors.
- Outer watchdog capture_complete/cleanup_complete true, cleanup not_needed, no timeout diagnostics. Cleanup completed within 300s. No independent exhaustive browser-profile directory-removal assertion is made because its path was not logged.
- **Zero changes** to entry-visible files, compiled inputs, historical evidence and installed-client documentation. HEAD/index unchanged. Fresh evidence collectors and outputs are confined to V, plus this handback.

## Smallest justified next action and preserved governance

Retain the partial native evidence for the separate independent reviewer; **do not self-close FR-12**. No client reinstall, trust/security repair or application correction is justified by this timing failure. The causes of the earlier URL-confidence rejection and get_window_state timeout remain **ROOT_CAUSE_NOT_DETERMINED**; this run reproduced neither.

Before any future separately authorized attempt, the smallest useful preparation is a concrete observer schedule that acknowledges each genuinely inspected 30-second stage promptly and keeps total coverage inside 240/300s. The existing fixture also loses its raw geometry/keyboard worksheet on a later failure; if durable incremental output is desired, propose an exact **test-only** change for separate Owner authority. This report does not authorize that change or another run. Existing timeouts must remain unchanged unless separately approved. Any future acceptance still requires full native menu/action/keyboard/focus evidence and separate independent adjudication.

**Preserved findings:** FR-08/09 independently VERIFIED/CLOSED; FR-12/PH-I01 NOT_VERIFIED; FR-13 OPEN / UPSTREAM_DEPENDENCY_BLOCKED under the existing upstream-wait decision; H1F-L01 LOW/OPEN/DEFERRED. Previously accepted findings and v0.3 preserved. H1-F acceptance/ACTIVE unchanged.

**Preserved counters:** Antigravity **3/3 exhausted / STOPPED**; Codex takeover **1 independently blocked / STOPPED**; corrective follow-up **1 independently reviewed / STOPPED**. This separate evidence attempt adds no takeover/remediation round. No Git stage/commit/push/tag/PR.

**Docs Sync:** UPDATED — this dated handback only. ALREADY_CONSISTENT — ACTIVE/latest independent review, closed findings, FR-13 wait and counters. NOT_VERIFIED — remaining mandatory PH-I01 observations, complete raw geometry worksheet and historical root causes. BLOCKED_OWNER_DECISION — any future attempt or test/client/application repair. DEFERRED_OUT_OF_SCOPE — unrelated cleanup/dependency work.

**Saved-handback verification — 2026-11-10T15:54:45.097048Z:** all 3,331 entry-visible files, 2,315 compiled inputs, 2,175 historical files, four installed-client documentation files and all 125 native inputs match their saved hashes. Selected tool/Edge hashes also remain unchanged in V/native-response-file-integrity.json. HEAD/index and pre-existing worktree entries are unchanged; this handback is the only new Git-visible path. All 14 local links resolve, no trailing whitespace is present and git diff --check passes. V/saved-handback-check.json records these checks. Entry-manifest SHA-256: 78271fac1133cac95c5150d4f05b94ca942b95cab67fa63c82252f500dacb34a; native source-hashes.json SHA-256: dacdf87388b7ecae368fc6882ce5069be11e69aff6d8209931a599765b6ea41d. V/evidence-index.json supplies exact saved-file hashes for the new evidence, including every original-response copy.

**STOP:** one full verification attempt ended incomplete; no automatic second attempt, repair or acceptance. Separate independent Codex must adjudicate any future complete FR-12 evidence; H1-F would remain blocked by FR-13 even after FR-12 closure.

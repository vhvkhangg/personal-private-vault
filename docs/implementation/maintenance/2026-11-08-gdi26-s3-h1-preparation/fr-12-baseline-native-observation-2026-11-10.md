# FR-12 / PH-I01 baseline-only native observation — 2026-11-10

**Diagnostic result: BASELINE_OBSERVATION_SUCCEEDED.** One uniquely selected, live owned Edge window was observed with the documented screenshot-first Computer Use call. The call completed without a timeout or policy rejection. No recovery was needed. This result does not identify the causes of the historical URL-policy rejection or capture timeout.

**URL-confidence internals: NOT_DETERMINED.** The actual native screenshot shows the expected localhost address; the client admitted the capture but returned no URL-confidence field or internal policy diagnostic. No trusted-URL assertion is derived from the fixture's planned URL, title or Playwright metadata.

**FR-12 / PH-I01 remains NOT_VERIFIED.** This is the Owner-authorized single baseline-only diagnostic, not takeover, remediation, acceptance review, actual zoom verification or client repair.

## Authority, scope and exact execution

Authority is the Owner's “GDI26-S3-H1-F FR-12 Baseline-Only Native Observation” message following the [read-only diagnostic](fr-12-native-client-diagnostic-2026-11-10.md). [Owner policy](../../../workflow/current-owner-delivery-policy.md), [ACTIVE](../../handoffs/ACTIVE.md), the [immutable H1-F executable](executable-handoff-h1-f-2026-11-09.md) and [latest independent review](reviews/2026-11-10-gdi26-s3-h1-f-corrective-follow-up-1-independent-final-review.md) were preserved.

HEAD: **8e8b6c56d7cdc5895634a0fdcba2d2ae6aac7b84**. Existing dirty worktree retained; index empty. Normal Google Sign-In / Drive / Drive Files / Feed environment flags were all **UNSET**. No remote publication, dependency registry or audit query was performed.

**D** = [logs/gdi26-s3-h1-f/baseline-diag-mwar2932/](../../../../logs/gdi26-s3-h1-f/baseline-diag-mwar2932/), fresh ignored evidence directory. **N** = D/native/. The existing compiled fixture was reused from logs/gdi26-s3-h1-f/codex-takeover-17e97e4740d3452d8dbdf0b4cb595d7c/fixture-11.

Exactly one existing headed PH-I01 fixture instance was executed:

    python -B scripts/run-bounded-command.py --profile normal --timeout-seconds 300 --receipt logs/gdi26-s3-h1-f/baseline-diag-mwar2932/native-receipt.json -- logs/gdi26-s3-h1-f/tools/node-v26.13.0-win-x64/node.exe frontend/scripts/run-e2e.mjs --group=accessibility --filter=PH-I01 --artifact-dir=logs/gdi26-s3-h1-f/baseline-diag-mwar2932/native --fixture-dir=logs/gdi26-s3-h1-f/codex-takeover-17e97e4740d3452d8dbdf0b4cb595d7c/fixture-11

The existing runner's short-lived version-preflight Edge launch is separate from the one headed PH-I01 instance. No second fixture, browser relaunch or alternate observation mechanism was used. The existing fixture's setup/login/population/window-sizing steps ran unchanged; there were no added CDP/Playwright calls. All native observation used the documented @oai/sky client.

The observer initialized before launch, polled only the fresh baseline marker, requested a fresh window inventory immediately on its arrival, then captured once. It did not create baseline-confirmed.json or any zoom/positive marker. The existing 60-second baseline gate, 240-second test limit and 300-second command cap were unchanged.

## Source, toolchain and fixture provenance

[D/entry-manifest.json](../../../../logs/gdi26-s3-h1-f/baseline-diag-mwar2932/entry-manifest.json) contains exact per-file hashes for **3,330 pre-existing Git-visible files**, **2,315 compiled backend inputs**, **2,140 historical evidence files**, and four installed Computer Use documentation files. N/source-hashes.json records **125 run inputs**, including frontend, reused compiled fixture and production-build metadata. [D/preflight.json](../../../../logs/gdi26-s3-h1-f/baseline-diag-mwar2932/preflight.json) verifies real IPv4/IPv6 bind/listen/close availability, all **32 actual installed direct pins**, fixture hashes and image identity. No dependency installation, compile or build ran.

Actual selected versions: **Node 26.13.0; npm package 12.2.2; Edge 157.0.4341.42; Playwright 1.65.0; Next 16.4.5; React 19.3.1; TypeScript 6.0.3; Java 25; Docker Server 29.9.2**. npm was not invoked for the native command. N/native-toolchain.json records the runner's actual Node path/version and Edge version. [D/toolchain-fingerprint.json](../../../../logs/gdi26-s3-h1-f/baseline-diag-mwar2932/toolchain-fingerprint.json) records Java/Docker/npm/Playwright executable or package hashes and all installed-client documentation hashes.

Computer Use documentation path: C:/Users/ADMIN/.codex/plugins/cache/openai-bundled/computer-use/26.1103.60933/. The documented direct @oai/sky import succeeded. No independently exposed native-helper binary version/hash was obtained; none is invented from the plugin directory version, and no helper executable was searched for or invoked directly.

| Exact input | SHA-256 |
| --- | --- |
| frontend/tests/e2e/accessibility.spec.ts | dae64254f812487e9f8c948fe08fb0b05d20d6535b7b15f7f08717f2f50f4f8f |
| frontend/scripts/run-e2e.mjs | 3b455e44125080775acff21d3075915c2d6a000e10c0ecec2f55052dce497f40 |
| frontend/scripts/fixture.mjs | c517f0d4100f99405f26d4e021e9ed5d108ac87f7a238cc63d72536667dfe908 |
| frontend/playwright.config.ts | 349c514f10a416b6a313e9682917ae75ef2401ba46b0d74dc9d963e0f1045178 |
| frontend/package.json | 2201994d8ad9ef553075ee92ded4d20b7f7d5ff8db44e857ec1d39d4c690ad26 |
| frontend/package-lock.json | a83a113382c3ef1ac239af483814262c01b77e86758061e7027d2ae6562f50a2 |
| frontend/.next/BUILD_ID | 7c592268893331ec566ea08ff7a942ceaf19d9e8d8ab418c9348d4d9f5ff746c |
| frontend/.next/build-manifest.json | 61c73f71a9506d7b9a26180d015c501d46cf14f06bd4f54000748a36d66b5744 |
| Selected node.exe | 14319838b3f355e0398c13569a90270bce4fc68f25189f58c59b03dadae0e6d5 |
| Installed msedge.exe | d258e512aa24947cfbef0c0033d8c3cd56ecb40f7b2ad59802fa75c4e4f3324d |
| Installed @playwright/test/cli.js | 79e23e6a249176295b8490567daa7717448a75866d6ea6f6b296ff3d23305c69 |
| Compiled H1FLocalFixture.class | ffdfab10d52d4dd182d125f6c2d4e3ee5bbbc7c019155d96d71c24922dbf0f0e |
| Compiled H1FFixtureConfiguration.class | c0a3dff9c4da7d27a555f4ad5c49f38a8253064092cf4cc3f8f378218c589fe5 |
| Compiled H1FFixtureConfiguration$1.class | 6794d8b9f27f1888d460e0d2a5e985d9c454de0c489b5ccaa8ed5148b01a84a3 |
| fixture-classpath.txt | b3f9f9fb289a18349a8968ed5259b7359a3af7f3ce4d57b4bf45babcb102a61a |
| Fixture source-hashes.json | 637063404678be030ff71dd8348945e90144fb35e1b5201f77e6ea1ddf937935 |

All five compiled fixture hashes match the preceding independent review's fixture-reuse.json. PostgreSQL image: ppv-rag-postgres:18.6-pgvector0.8.6, **sha256:2c85c6e401bdbb4ec4ee59e11a7b15fdba5aaad787c0a69295d8ce91e004aa8c**. Owned container: **cb122d62ead6499084b5e52805c900123b71692bc5e9f97f2b64d909dfa01e47**. N/fixture-metadata.json records the exact fake-provider/control loopback ports and owned temporary paths. No image/runtime download or live Google access was introduced.

## Startup, observation and shutdown timeline

All times below are **UTC on 2026-11-10**. Windows/Spring +07:00 timestamps are converted explicitly. Process creation timestamps are from the post-capture CIM process snapshot, not an invented browser-ready event.

| Time | Actual event / evidence |
| --- | --- |
| 13:52:45.612 | Preflight metadata captured; ports free, expected image present, pins match. D/preflight.json. |
| 13:53:43.795 | Bounded native command starts; owned runner PID 31756. D/native-receipt.json. |
| 13:53:46.207 | Spring Java process PID 34296 created under the runner. D/processes-after-capture.json. |
| 13:54:12.367 | Spring reports startup complete, port 19080; stdout records PPV_FIXTURE_READY 59158. D/native-receipt.stdout.log. |
| 13:54:12.807 | Owned Next PID 27916 created. Same stdout reports production server ready in 229ms. No finer absolute Next-ready timestamp was emitted. |
| 13:54:14.361 | PH-I01 case starts. N/results.json. |
| 13:54:14.402 | Headed browser root PID 12424 created under Playwright worker PID 33836, itself under the owned CLI/runner tree. Process snapshot. |
| 13:54:23.722 | baseline-requested.json filesystem mtime, used as a conservative observation deadline origin. N/baseline-requested.json. |
| 13:54:23.777 | Observer detects marker approximately 55ms after that mtime; records about 59.96s remaining. D/observer-01.json. |
| 13:54:23.782–24.870 | Fresh list_windows returns 11 windows overall, exactly one matching MSEdge/PPV candidate. About 58.85s remain. D/observer-02.json. |
| 13:54:34.002 | Capture request recorded after inspection of the returned candidate; about **49.72s** remain. D/observer-03.json. |
| 13:54:37.188 | Screenshot-first capture succeeds; about **46.53s** remain. Rehydrate + capture + metadata recording elapsed approximately **3.19s**; this is not a measurement of the internal screenshot stage alone. D/observer-04.json. |
| 13:55:19.650 | Read-only post-capture inventory still returns the same app/id/title; about 4.07s remain before the estimated baseline deadline. D/observer-05.json. |
| About 13:55:24.261 | Test ends at the intentionally unconfirmed baseline gate. Calculated from N/results.json startTime + 69,900ms duration. |
| 13:55:25.298–25.305 | Spring graceful shutdown begins/completes. D/native-receipt.stdout.log. |
| 13:55:26.077 | Harness cleanup record completed: nextExit 1, springExit 0, ownedPathsRemoved true. N/cleanup.json. |
| 13:55:26.458 | Outer command exits 1 after **102.657s**, capture/cleanup complete; no watchdog timeout. D/native-receipt.json. |
| 13:56:16.247 | Independent owned-process/ports/container/temp-path cleanup and source integrity check completed. D/final-integrity-cleanup.json. |

The marker carries a literal expected URL and baseline innerWidth 960 / DPR 2.5 / outerWidth 978. It is coordination metadata only. No URL-confidence conclusion is based on it.

## Returned window, actual request and response

Fresh selected returned Window:

    app: MSEdge
    id: 724318
    title: PPV PH-I01 actual native zoom - Personal - Microsoft Edge

The raw returned title includes a zero-width character in “Microsoft Edge”; the complete raw object is preserved in observer evidence. Selection used actual returned objects, the MSEdge app and the task-specific title; exactly one candidate matched. get_window received that returned id/app. No stale or constructed handle was used.

Actual capture:

    sky.get_window({ id: baselineSelected.id, app: baselineSelected.app })
    sky.get_window_state({
      window: baselineWindow,
      include_screenshot: true,
      include_text: false
    })

**Fact:** the call returned a Window with the same id/app/title, accessibility null as requested, and one screenshot:

    id: screenshot-0
    width: 1212
    height: 850
    originX: 111
    originY: 100
    zIndex: 0

No capture error, timeout or URL-policy rejection was returned. No accessibility text was requested. The subsequent inventory was a liveness check, not recovery or another capture. The same returned id was present before capture, in the capture result and after capture. The owned browser process tree was also present in the process snapshot.

**Liveness qualification:** those observations support that the intended window survived the capture and remained available afterward, well inside the baseline wait. No continuous window-destruction event trace or direct native-window-id-to-PID mapping was captured. Process-tree ownership and the uniquely titled returned window are complementary evidence; they are not conflated into an unrecorded OS mapping.

Actual native screenshot and original response are retained without decoding, editing or re-emitting the image for further inspection: [D/original-capture-response.json](../../../../logs/gdi26-s3-h1-f/baseline-diag-mwar2932/original-capture-response.json) is an exact copy of the original serialized tool-response event, including its input_image block. [D/capture-provenance.json](../../../../logs/gdi26-s3-h1-f/baseline-diag-mwar2932/capture-provenance.json) identifies the source session file, line **276**, timestamp **13:54:37.252Z** and call **call_0ad707b650e44f059cd99c1f3a32b996**. Exact serialized response SHA-256: **d34c01511e9d127d9c47f75d851f2a55c72b8971c946cc142b0b78bd9561173b**. D/capture-tool-response.json is a compact metadata/text index, not a replacement screenshot.

## URL confidence and screenshot completeness

**Observed fact:** the actual native screenshot visibly includes the address bar with **localhost:3000/personal/storage/google-drive**, the PPV tab title and rendered Google Drive Explorer. This is actual client image evidence, rather than a planned URL or title-only inference.

**Policy outcome:** the client completed and displayed the screenshot with no policy rejection. **Internal URL-confidence result: NOT_DETERMINED** because no confidence value, verified-URL field or policy-engine trace was returned. This is evidence of a permitted successful observation in this run; it is not proof that the earlier guard cause was repaired or bypassed.

**Completeness: FULL_UNCROPPED_NATIVE_WINDOW_NOT_ESTABLISHED.** The image shows browser chrome/address bar, PPV controls, both lateral portions and a Windows taskbar across the bottom. The browser's complete lower frame/viewport extent is not demonstrably present. The API describes bounded screenshot regions; its returned size/origin does not independently certify the complete window bounds. The saved native-window-entry.json records initial, pre-resize bounds, not an independently measured final capture rectangle. No additional CDP or native capture was performed to fill that gap. The screenshot is useful baseline evidence, not complete zoom/dialog usability proof.

No actual zoom input, 200%/300% stage, dialog exercise, keyboard traversal or accessibility acceptance test was performed. Existing setup actions before the baseline marker are not credited as such acceptance testing.

## Expected nonzero test result and resource cleanup

The fixture was allowed to finish without positive confirmation. N/results.json records **0 passed / 1 failed** at:

    Mandatory policy-compliant uncropped native capture unavailable: baseline

This is the existing test's marker-wait error, **not a failure of the successful Computer Use capture**. The authorization required withholding baseline-confirmed.json, so the browser test intentionally could not advance. No zoom-requested, confirmed or policy-blocked marker exists. Its name still mentions zoom, but those stages were not entered.

[D/final-integrity-cleanup.json](../../../../logs/gdi26-s3-h1-f/baseline-diag-mwar2932/final-integrity-cleanup.json), the harness record and watchdog agree:

- **Browser / Next / Spring:** no surviving PID from the recorded owned runner tree, including browser root 12424, Next 27916 and Java 34296; no unrelated process was terminated by this diagnostic.
- **Ports:** actual bind/listen/close succeeds for 3000/19080 on both 127.0.0.1 and ::1 after cleanup.
- **PostgreSQL:** docker inspect of the exact owned container returns exit 1, **error: no such object**, supporting removal rather than a merely stopped service.
- **Owned temporary paths:** all three paths from fixture-metadata.json are absent; harness reports ownedPathsRemoved true. A separate browser profile directory path was not logged, so no exhaustive filesystem profile-removal assertion is made.
- **Watchdog:** COMMAND_FAILED / exit 1, cleanup not_needed, no timeout diagnostics, capture_complete true, cleanup_complete true. The runner completed normally with its expected test failure; the watchdog did not expire or kill the initial target.

Only scoped evidence collectors were added under D. A later metadata fingerprint probe used a nonexistent legacy Playwright browserType.js layout and failed before writing its fingerprint; it was replaced by hashing the actual existing package metadata/CLI. That read-only bookkeeping error did not launch a fixture, alter dependencies, change the observation or consume another attempt. Its qualification is retained in toolchain-fingerprint.json.

## Root-cause classification and smallest justified next action

| Classification | Conclusion and limit |
| --- | --- |
| **CONFIRMED** | This run discovered one matching owned Edge window, completed one screenshot-first capture, and found the same returned window live afterward. No timeout or policy rejection occurred; no recovery executed. |
| **CONFIRMED** | The subsequent browser test failure is the deliberately withheld baseline marker. It cannot be classified as an application or native-client capture regression. |
| **INFERENCE** | Timely marker coordination and screenshot-only capture are viable for at least this baseline observation under the existing permitted configuration. This is one run, not a reliability rate or guarantee. |
| **HYPOTHESIS** | Removing accessibility text and acting earlier may have reduced capture cost or timing pressure compared with the prior failed attempt. Multiple conditions differ and there is no controlled comparison, so neither is established as the fix. |
| **NOT_DETERMINED** | The historical timeout's cause, the earlier URL-confidence rejection's underlying cause, the client's internal confidence decision and any shared cause remain unknown. **ROOT_CAUSE_NOT_DETERMINED** for those historical failures. No defective-client claim is justified. |

**Recommendation:** a separately Owner-authorized **full native zoom verification** is now justified by the successful baseline observation, provided its plan has a feasible shared time budget and a supported way to retain complete uncropped native viewport/dialog evidence. Reuse the unchanged source/tool/fixture evidence. Do not spend all existing individual wait maxima: the previous diagnostic showed they exceed both the test and command budgets when added together. Define immediate observer handoffs, finite recovery, a cleanup reserve and an explicit stop when time/URL/capture completeness is insufficient.

No new client installation, trust/security repair or maintainer escalation is presently justified solely by this successful run. If a later separately authorized attempt fails while the same owned window is demonstrably live, preserve the precise error and timing for supported maintainer diagnosis. Any necessary harness repair must be proposed as an exact, separately authorized change before execution. No automatic second baseline, zoom test or repair follows.

## Integrity, Docs Sync and stop

[D/final-integrity-cleanup.json](../../../../logs/gdi26-s3-h1-f/baseline-diag-mwar2932/final-integrity-cleanup.json) records **zero hash changes** in all pre-existing visible files, compiled inputs, historical artifacts and installed-client documentation. HEAD/index unchanged. New data is confined to D and this dated report. Source/test/timeout/fixture/harness/dependency/lock/client-installation/hooks/trust/permissions/security configuration were not modified.

**Preserved:** FR-08/09 VERIFIED/CLOSED; FR-12/PH-I01 NOT_VERIFIED; FR-13 UPSTREAM_DEPENDENCY_BLOCKED under the existing upstream wait; H1F-L01 LOW/OPEN/DEFERRED. H1-F acceptance/ACTIVE unchanged. Antigravity **3/3 exhausted / STOPPED**; Codex takeover **1 independently blocked / STOPPED**; corrective follow-up **1 independently reviewed / STOPPED**. No extra correction/review round, acceptance, Git staging/commit/push/tag/PR or publication.

**Docs Sync:** UPDATED — this new diagnostic report only. ALREADY_CONSISTENT — ACTIVE, executable/current independent review, closed findings and counters. NOT_VERIFIED — full PH-I01, complete uncropped native zoom evidence, hidden URL-confidence internals and historical root causes. BLOCKED_OWNER_DECISION — any full zoom verification or actual repair. DEFERRED_OUT_OF_SCOPE — unrelated historical cleanup and FR-13 dependency work. Saved-file/hash/link/whitespace checks are documentation integrity, not acceptance review.

**Saved-file verification — 2026-11-10T14:01:59.624715Z:** all 125 native input hashes, all recorded tool hashes and every entry source/compiled/history/client-documentation hash still match. HEAD/index and pre-existing worktree entries are unchanged; this report is the only new Git-visible path. All 13 local links resolve, no trailing whitespace is present and git diff --check passes. D/saved-report-check.json records these results; D/diagnostic-evidence-index.json hashes the 34 preceding evidence files. Entry-manifest SHA-256: e420b9f8c067068c4c5d36e1d114ea2357e8d3b61f79acd176ff2c28da26fb74; native source-hashes.json SHA-256: dacdf87388b7ecae368fc6882ce5069be11e69aff6d8209931a599765b6ea41d. The saved-document checker initially failed on the ambient text encoding before writing its result; explicit UTF-8 reading succeeded without another fixture or UI call.

**STOP:** baseline-only diagnostic delivered. Any full native zoom verification or repair needs a separate Owner authorization.

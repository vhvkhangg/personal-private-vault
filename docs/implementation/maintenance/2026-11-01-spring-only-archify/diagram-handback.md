# Spring-only Archify author handback — 2026-11-01

**Status: AUTHORING_COMPLETE / INDEPENDENT_REVIEW_REQUIRED.** Four additive documentation views are authored and author-verified. A8 independent acceptance is **NOT_RUN**; this author does not return READY_FOR_OWNER_COMMIT. No staging, commit, push or publication occurred.

## Authority and execution identity

- Executed the [one-off task](diagram-authoring-task.md) within the [Owner scope](owner-scope-and-authorization.md) and [diagram plan](diagram-plan.md), after the [independent preparation review](reviews/2026-11-01-independent-preparation-review.md) returned READY_FOR_OWNER_COMMIT.
- Execution branch `main`; HEAD, local `origin/main` and a read-only remote `refs/heads/main` query all returned **`29ffc45c4a7079dfb4e155ee55590ff8fca6dfb0`**, the Owner-published preparation. Entry index and working tree were clean. The accepted application/CSA26 baseline remains `4cf62041796d0b510255ee7014261221cafe3d5d`; it is not relabelled as this unpublished delivery.
- [ACTIVE](../../handoffs/ACTIVE.md) remains `NO_ACTIVE_HANDOFF`, unchanged. This is the expressly authorized Codex documentation exception, with a separate subsequent review session.
- RAG is excluded from all four IR/HTML/SVG files and the new index. Phase 17 frozen, Phase 18 closed, S19-1 accepted/published and S19-2 suspended/not accepted/archival-only remain unchanged. Indexing stays inactive; live originals stay disabled/deferred. No application tests, QA, inference, worker, database, migration or model runtime was executed.

## Authored inventory

The deliverable is **14 new files**: the following 13 indexed artifacts plus this handback. Only the four requested views exist; there is no fifth explainer, combined C4 replacement or application change.

| View | Native type | Primary elements | Directed relationship identities | Coverage |
| --- | --- | ---: | ---: | --- |
| SAD-01 | architecture | 19 module owners | 38 | Explicit cross-owner Java imports, nested packages rolled up; selected named interfaces and every import pair linked in the index |
| SAD-02 | sequence | 7 participants | 14 | Real image-metadata GET, configured security chain, DTO/envelope, read-only service, owner-local repository, 401 and 404 alternatives |
| SAD-03 | sequence | 6 grouped participants | 42 | Independent bootstrap, login, locked refresh rotation, supplied-refresh revocation, PIN verification/change operations |
| SAD-04 | architecture | 12 components | 12 | Metadata/Vault transaction, optional storage and compensation, JDBC REPEATABLE READ portability export, Flyway/Hibernate responsibilities |

SAD-03 has 34 unique endpoint/label triples because repeated calls belong to different scenarios. **All 42 distinct message identities** survive HTML and SVG export. SAD-01 matches all 38 edges in the corresponding source-import subset of the accepted module graph, with no allowed-only edge added. Authentication, journal and portability retain their independent nodes.

| Artifact relative to the Spring index | Bytes | SHA-256 |
| --- | ---: | --- |
| [README.md](../../../architecture/diagrams/interactive/spring/README.md) | 25,047 | `e325e83d6d3945975333b3f318959294b3a78c06ed684f4ac5ac058871af3858` |
| [sources/authentication-lifecycle.sequence.json](../../../architecture/diagrams/interactive/spring/sources/authentication-lifecycle.sequence.json) | 15,100 | `2de10c1fab47275d6e0233c5192ae2111c568f044b6605e6f09a8af88ded57c6` |
| [sources/persistence-boundaries.architecture.json](../../../architecture/diagrams/interactive/spring/sources/persistence-boundaries.architecture.json) | 12,859 | `dc9eaf40a0b9952aa6f96ba545a2e70cb828d10da1c8b641cb29dd3a7b5c6af1` |
| [sources/request-lifecycle.sequence.json](../../../architecture/diagrams/interactive/spring/sources/request-lifecycle.sequence.json) | 7,683 | `b476787a1d26e076fcbed4da60ec5eceda383061b0f248bf281f5f582a3636c9` |
| [sources/spring-modules.architecture.json](../../../architecture/diagrams/interactive/spring/sources/spring-modules.architecture.json) | 22,720 | `5c1f903b91f40f909cbdabf9adde740f280b64010d11e3ff3bc6ee60814a383f` |
| [html/authentication-lifecycle.html](../../../architecture/diagrams/interactive/spring/html/authentication-lifecycle.html) | 868,375 | `479b1c0af95cf00be631b60d1dcd1f60656f85994124a8a708f22572243fdb11` |
| [html/persistence-boundaries.html](../../../architecture/diagrams/interactive/spring/html/persistence-boundaries.html) | 857,882 | `18fdd7095e3f45bd2c7457462e56fcb303c65175c6343e86866da2ba3175b177` |
| [html/request-lifecycle.html](../../../architecture/diagrams/interactive/spring/html/request-lifecycle.html) | 844,685 | `e0e6f5d64340e17c5fd796d2a86ba901019f20358b00fdaefbdf5e40822cab5e` |
| [html/spring-modules.html](../../../architecture/diagrams/interactive/spring/html/spring-modules.html) | 883,421 | `35af65ad4aa139601eb13e6e24c8359f2edf04f973a566ed547a1081e1d8b8d6` |
| [exported/authentication-lifecycle.svg](../../../architecture/diagrams/interactive/spring/exported/authentication-lifecycle.svg) | 233,659 | `d208734044ccc4868f74b448fa2f33cd65a4047f6f8fcab2c49ec5ec013bd02d` |
| [exported/persistence-boundaries.svg](../../../architecture/diagrams/interactive/spring/exported/persistence-boundaries.svg) | 204,951 | `035d52480a7b3212ecdcf30d74500f8c9a331d09f5345abf496a7cbbbc7dc73d` |
| [exported/request-lifecycle.svg](../../../architecture/diagrams/interactive/spring/exported/request-lifecycle.svg) | 190,962 | `322c7325cb87edb0f24a3dd708e80a28e1199d54fd505a43d50aebb307135198` |
| [exported/spring-modules.svg](../../../architecture/diagrams/interactive/spring/exported/spring-modules.svg) | 246,678 | `79fe8f0f19a4388b7a5ba02cacd2e8a8999d86b4b0972d5dbc5d9e3ecff95a84` |

## Vetted local toolchain and controls

Used [Archify 3.2.0](https://github.com/tt-a1i/archify/blob/a4065941f12be7967436261d9934784e381d423c/archify/package.json), pinned upstream commit **`a4065941f12be7967436261d9934784e381d423c`**, with Node **v24.14.0** and installed Microsoft Edge **156.0.4314.58**. No npm/npx/skills installer, package install, tool update, project dependency or global skill installation was used.

The complete upstream repository was acquired into ignored `logs/spring-archify-20261101-8f3fe0c1/tool-git` using `git clone --no-checkout --filter=blob:none --depth 1`, then checked out at that exact detached commit. The selected CLI is `tool-git/archify/bin/archify.mjs`, invoked directly with Node. Public upstream bytes alone were fetched. A prior codeload archive attempt received zero bytes and was cancelled; its zero-byte file remains as ignored scratch evidence. The successful Git acquisition used the normal watchdog, 60 seconds, **exit 0**.

Inspected the root/skill manifests and skill lock, installation-related scripts, CLI/finalize/delivery/update chain, browser launcher, renderer/template, repository provenance code, export path and software notices before executing the selected route. The packaged runtime uses generated validators and embedded assets, so no development dependency installation was needed. Root and skill LICENSE hashes match Apache-2.0; historical LICENSE-MIT, NOTICE, third-party and embedded font licenses are preserved in generated output. The optional DeepSeek adapter and website build were not used. License URLs in notices are attribution, not loaded executable assets.

| Fingerprint | SHA-256 |
| --- | --- |
| `archify/package.json` | `ea99289562037cae2deeced0b45e1374ce4c4b4f23a53a607a6aada62c803cd0` |
| `archify/package-lock.json` | `ab7a1438a8edbd26ceda9cf9648e3ad50542bffd7cc87f9ec83793cde1f26287` |
| Root and skill Apache LICENSE, each | `cfc7749b96f63bd31c3c42b5c471bf756814053e847c10f3eb003417bc523d30` |
| Selected `archify/` tree, 206 files | `f4cc7d62491d16dffec8775ab9b6f889af5217e99a75ae7350e84132b3b42f81` |

Tree fingerprint algorithm: sort files case-sensitively by repository-relative POSIX path; concatenate `path + NUL + lowercase SHA-256(file bytes) + LF`, UTF-8; hash the concatenation. The full path/hash inventory is retained in [tool-tree-canonical-sha256.txt](../../../../logs/spring-archify-20261101-8f3fe0c1/tool-tree-canonical-sha256.txt). The earlier Windows-path-order inventory is also preserved; the portable canonical ordering above is the delivery fingerprint. The external tool checkout remains clean. This identifies vetted bytes, not a universal vulnerability-free certification.

`ARCHIFY_UPDATE_CHECK_DISABLED=1` was set for every Archify execution. The inspected [delivery updater](https://github.com/tt-a1i/archify/blob/a4065941f12be7967436261d9934784e381d423c/archify/bin/delivery-update.mjs#L49) returns before spawning its network updater under this control; native delivery receipts record the disabled result. `ARCHIFY_CHROME` selects installed Edge. No sandbox opt-out was set. Native browser checks disable background networking, component updates, sync and default apps; the additional interaction/static tests set CDP offline and block HTTP(S)/WS(S) before page navigation. Those tests observed **zero remote request attempts and zero JavaScript runtime exceptions** across all delivered HTML. SVGs also rendered offline in both themes without remote requests. This is artifact/session evidence, not an OS-wide packet-capture claim.

## Commands, actual exits and retained evidence

All paths below are relative to repository root. Potentially long render/browser commands used the existing `scripts/run-bounded-command.py`, normal profile, **240 seconds** per native finalize/visual/interaction invocation; the static SVG/link pass used **120 seconds**. All completed within those caps. Receipts/stdout/stderr were unique; historical logs were neither deleted nor truncated.

```powershell
$run = 'logs/spring-archify-20261101-8f3fe0c1'
$tool = "$run/tool-git/archify/bin/archify.mjs"
$env:ARCHIFY_UPDATE_CHECK_DISABLED = '1'
$env:ARCHIFY_CHROME = 'C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe'
python scripts/run-bounded-command.py --profile normal --timeout-seconds 240 --receipt "$run/modules-finalize-2.json" -- node $tool finalize architecture docs/architecture/diagrams/interactive/spring/sources/spring-modules.architecture.json "$run/delivered/spring-modules.html" --repo-root . --quality showcase --out-dir "$run/modules-review-2" --json
```

The other exact invocations use the same flags and the paths in this finite execution ledger; each watchdog receipt binds its argv SHA-256 and actual exit. The current full native finalizer receipts include validate, deliver, strict provenance check and real-browser stage results.

| Operation / output stem | Final evidence directory or receipt | Actual exit / result |
| --- | --- | --- |
| SAD-01 `finalize architecture spring-modules.architecture.json` | [modules-review-2](../../../../logs/spring-archify-20261101-8f3fe0c1/modules-review-2/spring-modules.finalize.json) | 0; all four gates PASS, showcase, zero diagnostics |
| SAD-02 `finalize sequence request-lifecycle.sequence.json` | [request-review-1](../../../../logs/spring-archify-20261101-8f3fe0c1/request-review-1/request-lifecycle.finalize.json) | 0; all four gates PASS, showcase, zero diagnostics |
| SAD-03 `finalize sequence authentication-lifecycle.sequence.json` | [auth-review-1](../../../../logs/spring-archify-20261101-8f3fe0c1/auth-review-1/authentication-lifecycle.finalize.json) | 0; all four gates PASS, showcase, zero diagnostics |
| SAD-04 `finalize architecture persistence-boundaries.architecture.json` | [persistence-review-2](../../../../logs/spring-archify-20261101-8f3fe0c1/persistence-review-2/persistence-boundaries.finalize.json) | 0; all four gates PASS, showcase, zero diagnostics |
| Four `visual-check <delivered/stem.html> --summary --require-provenance --out-dir <stem-visual-final>` | Four `*-visual-final.json` watchdog receipts and matching evidence directories | 0 each; capture/browser checks PASS; subsequent image inspection by this author, not independent acceptance |
| One exact-source SAD-02 rerender, fresh `rerender/request-lifecycle.html` | [request-rerender-review](../../../../logs/spring-archify-20261101-8f3fe0c1/request-rerender-review/request-lifecycle.finalize.json) | 0; all gates PASS; **byte-identical HTML**, hence unchanged semantic facts |
| Four final-path `check <html/stem.html> --json` commands, 60-second normal watchdog | Four `*-portable-check.json` receipts and matching stdout logs | 0 each; native portable artifact checks PASS, without claiming relocated strict provenance |
| Final HTML interaction/native SVG pass, `node browser-acceptance.mjs 6` | [browser-acceptance-run-6.json](../../../../logs/spring-archify-20261101-8f3fe0c1/browser-acceptance-run-6.json), [observations](../../../../logs/spring-archify-20261101-8f3fe0c1/browser-acceptance-6.json) | 0; four views PASS |
| Static SVG opens, themes, bounds and relative code-link click, `node static-review.mjs` | [static-review-run.json](../../../../logs/spring-archify-20261101-8f3fe0c1/static-review-run.json), [observations](../../../../logs/spring-archify-20261101-8f3fe0c1/static-review.json) | 0; four SVGs and source navigation PASS |
| File/edge/link/privacy/preservation verification, `python check-artifacts.py final` | [artifact-verification-final.json](../../../../logs/spring-archify-20261101-8f3fe0c1/artifact-verification-final.json) | 0; 2,807 entry-tracked files unchanged; all 173 relative links resolve |

Native metadata and screenshots deliberately stay under ignored logs. Each published-path HTML is an **exact byte copy** of the finalized scratch HTML; its hash above matches native receipts. No author-injected script or post-render HTML edit was made. Native SVG files are the exact Blobs produced by actual Export → SVG clicks with active focus; canonical export removed transient selection/route overlays and preserved live focus afterward. The test captured those Blobs locally and disabled downloads; it did not redraw or synthesize SVG geometry.

Native provenance sidecars bind the original scratch paths and are retained there. Therefore `check --require-provenance` on a relocated final HTML alone will report its absent sidecar. For reproduction, run `finalize` against the committed typed source into a **fresh scratch HTML/evidence directory**, or compare the delivered bytes to the retained bound receipt. Plain `check` can inspect the portable final HTML without sidecars. Do not manufacture portable receipts or replace their recorded paths.

Initial native finalizers also passed. The prescribed single placement-only pass preserved every component, edge, label and source: SAD-01 resolved crossings decreased **91 → 49**; SAD-04 **1 → 0**. The remaining module-map recommendation is advisory: 49 resolved crossovers, 14 routes over the suggested bend count and 4 over the suggested stretch. No second optimization loop or semantics deletion followed. The graph remains dense; focus/relationship and route exploration make individual dependencies explicit.

The first optional placement helper failed with missing NumPy (**exit 1**); a standard-library helper succeeded (**exit 0**) without installing anything. Initial interaction harness runs 1–5 exited **1** because the harness assumed a desktop-hidden mobile Relations button, did not scroll the finder results, selected the first fuzzy result, or reused a cleared route without re-enabling the probe. The corrected harness uses the actual visible controls, exact node IDs, stable coordinates and fresh route mode; run 6 passes without changing generated HTML. These failed attempts remain preserved and are not reported as successful artifact gates. A first link sweep correctly stopped while this handback target did not yet exist; the final sweep covers the completed files.

## A1–A8 author verification and practical limits

| Case | Author result | Evidence / limit |
| --- | --- | --- |
| A1 scope | PASS | Exactly 19 named module owners / 38 observed import edges; IR, HTML, SVG and index scanned for excluded content; only additive approved output paths |
| A2 fidelity | PASS, source-derived | Every primary element carries native committed source ranges; the [index](../../../architecture/diagrams/interactive/spring/README.md) links every module import pair, named interfaces and call/transaction evidence. Filter dispatch, JPA row I/O and default REQUIRED propagation are explicitly framework-inferred, not a measured runtime trace |
| A3 interactivity | PASS, bounded browser sample | Actual CDP mouse/keyboard input on all four final HTML files: hover, node focus, source passports, relationship facts, filtered finder with exact-node selection, theme and Live/Still. Module Route Probe resolves `search → account → reference`; reverse `reference → search` is rejected. Exact index-relative Java link clicked and source text opened offline |
| A4 ownership/determinism | PASS | Native typed schemas, committed source checks, pinned tool/tree hashes; sample rerender byte-identical; final HTML byte copies verified |
| A5 static fallback | PASS with documented format limits | Four native dual-theme SVGs open offline; XML parses, all node IDs and all relationship identities survive, no script/event handler. Text bounds are inside viewBox. Rendered light/dark exports were inspected; long authentication flow checked at top/middle/end |
| A6 privacy/security | PASS for delivered artifacts and observed sessions | No source/corpus/user records submitted remotely; no secret values, absolute local paths, CDN/external executable dependency in delivered artifacts. Embedded font/notices retained; browser network restrictions and zero remote request attempts above |
| A7 preservation | PASS | All **2,807** entry-tracked files retain their exact SHA-256; source/schema/build/dependency/runtime/QA/hook/current handoff and frozen diagrams unchanged; empty index |
| A8 separate acceptance | **NOT_RUN / REQUIRED** | This is the author session. A different independent Codex session must decide A1–A8 before any Owner commit/push |

Native automated browser measurements cover light at 1440×900, 1600×1000, 1920×1080 and 2048×1320, dark at both endpoints, READ/Still state and horizontal containment. Author perception inspected each HTML in light/dark at the capture endpoints, plus all SVGs in both themes at 1800×1100, including the long-flow sections. Normal document scrolling is used for long sequences. No mobile product acceptance, accessibility audit, exhaustive viewer feature test, performance claim, Maven/Python application test run or comprehensive backend audit is implied.

SVG is the native **diagram-only** export: it retains diagram labels and inline notes but omits the HTML title/cards and clickable source index. Keep the SVG alongside the linked index for qualifications. SAD-03 `/auth/...` labels abbreviate the actual `/api/v1/auth/...` routes, explicitly stated in its HTML card and index. Local-only native source cards display verified paths/ranges without clickable code links; the repository-relative index supplies those links. Local file viewers may ignore `#L` scrolling, while the paths themselves resolve. These are disclosed format/tool limits, not missing requested views.

No new blocking author finding remains. The module-map advisory remains visible for independent assessment; it is not declared fixed or warning-free visual proof. Existing `CSA26-FR-L01`, `AUTH-CLOCK` and unrelated LOW backlog are unchanged and not closed by this work. A8 is still a mandatory outstanding acceptance gate.

## Docs Sync Check and handback boundary

- **UPDATED:** new Spring index, four JSON sources, four portable HTML files, four native SVG exports and this handback only.
- **ALREADY_CONSISTENT:** reviewed/published preparation, ACTIVE `NO_ACTIVE_HANDOFF`, accepted canonical diagram sources/exports and frozen historical records; no phase/status rewrite.
- **DEFERRED_OUT_OF_SCOPE:** existing LOW backlog, older routing-notice cleanup, future backend audit, product features and frontend.
- **NOT_VERIFIED:** independent artifact acceptance and Owner publication. This author does not self-accept or publish.

Next step: a **separate independent Codex session** | review the 14-file working-tree deliverable against the published scope/plan and this handback | A8 outstanding; no staging or publication. Use the approved request:

```text
Independently review the four Spring-only Archify artifacts against docs/implementation/maintenance/2026-11-01-spring-only-archify/{owner-scope-and-authorization.md,diagram-plan.md,diagram-handback.md}. Verify code provenance, browser/HTML/SVG behavior, safety, non-RAG exclusion and prior preservation; write a dated review and return READY_FOR_OWNER_COMMIT only if all required A1-A8 gates pass. Do not alter implementation or publish.
```

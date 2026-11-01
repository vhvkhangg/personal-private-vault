# Independent preparation review — Spring-only Archify documentation

- **Date / reviewer / mode:** 2026-11-01 / independent Codex / documentation-only maintenance preparation.
- **Disposition:** **READY_FOR_OWNER_COMMIT** for the preparation. No blocking findings. Diagram implementation and artifact acceptance remain **NOT_RUN / NOT_IMPLEMENTED**.
- **Authority:** [Owner scope](../owner-scope-and-authorization.md), [diagram plan](../diagram-plan.md), [one-off authoring task](../diagram-authoring-task.md), [Owner policy](../../../../workflow/current-owner-delivery-policy.md), and [Docs Sync contract](../../../../workflow/documentation-synchronization.md).
- **Baseline:** `main`, HEAD and local `origin/main` = `4cf62041796d0b510255ee7014261221cafe3d5d`. Read-only `git ls-remote origin refs/heads/main` independently returned that same SHA. This review and the submitted preparation are subsequent, unpublished documentation.
- **Entry working tree:** modified `docs/implementation/handoffs/ACTIVE.md`; six untracked documents comprising the four Spring preparation files, CSA26 publication closeout, and future non-RAG audit intent. Empty index. No production, build, dependency, schema, hook, or existing diagram edits.
- **Counters:** initial Spring preparation review; no Antigravity remediation/takeover loop opened. CSA26 remains accepted/stopped at **2/3 consumed**, unused round not transferable. S19-2 history/counters unchanged.

## Selected gate and findings

This is the explicitly approved maintenance mode. [ACTIVE](../../../handoffs/ACTIVE.md) is `NO_ACTIVE_HANDOFF`; the four requested views are additive documentation, not a new software phase or executable implementation handoff. Numbered-phase predecessor/milestone gates do not apply. The Owner explicitly assigns later diagram authoring to Codex, followed by a different independent Codex session. The usual production implementation role does not require an Antigravity handoff for this exception.

**No new CRITICAL, HIGH, MEDIUM, or mandatory unverified preparation finding was identified.** A1–A8 are specified prospective artifact acceptance gates; none is represented as an executed diagram test. Missing final HTML/browser/SVG evidence is intentionally deferred by the Owner's two-stage preparation/authoring contract, and remains mandatory before artifact acceptance.

No material Owner decision remains for this preparation. A materially different tool, output requirement, source scope, or unsafe execution route must return to the Owner under the existing stop conditions. Reporting an unavailable SVG export does not satisfy A5 or authorize silently accepting a missing fallback.

## Owner-visible requirements and proportionality

Effort below is a review estimate of relative authoring/verification work, not measured duration or a new benchmark obligation. The smallest adequate alternative is assessed against the Owner's explicit request for interactive Archify output.

| Requirement / mechanism | Decision and simplest adequate route | Effort: author / verify | Approval and proof |
| --- | --- | --- | --- |
| SAD-01: module navigation | **KEEP / REUSE** all 19 owners; derive relationships from imports and named interfaces, reuse the accepted module graph as a cross-check | Medium / medium | Already approved; A1–A3; distinguish allowed dependencies from observed imports |
| SAD-02: authenticated request | **KEEP / SIMPLIFY** one real protected operation with normal, denied and error paths | Small / medium | Already approved; A2–A3; trace filter, controller, service and persistence rather than every endpoint |
| SAD-03: authentication lifecycle | **KEEP / REUSE** existing bootstrap, login, rotation, revocation and PIN behavior in one view | Medium / medium | Already approved; A2–A3; no invented cookie policy, access-token revocation, or implemented frontend |
| SAD-04: persistence/side effects | **KEEP / SIMPLIFY** two or three examples covering JPA transactions, the portability exception and optional media storage | Medium / medium | Already approved; A2; no full-table ERD or distributed transaction claim |
| Editable JSON, HTML and SVG | **KEEP / REUSE** native Archify types, embedded viewer and SVG export; one linked index | Small / medium | Already approved; A3–A5; static-only diagrams cannot replace the requested interactivity without Owner decision |
| Provenance and safe local execution | **KEEP** pinned local tool, relative source paths, retained software notices, no private-data upload | Small / medium | Existing privacy requirements; A2, A4, A6; actual output/network behavior still to verify |
| Verification | **SIMPLIFY / REUSE** native finalize gates, one semantic rerender, focused manual interaction/render checks and diff preservation | Small / medium | A1–A8; no repeated full Java/Python suites or new benchmark harness |
| New agents, skills, hooks, application APIs/dependencies | **REMOVE** from this deliverable; existing documentation skills, browser capability and watchdog suffice | None | Scope excludes them; no tooling/framework expansion is justified |
| Existing C4 overlap, feature implementation, final audit, frontend and RAG | **DEFER** to their separately authorized scopes | None here | CSA26-FR-L01 stays OPEN; future audit intent is not an executable contract |

## Source-grounded checks

Graphify was queried first for module/dependency and authentication/transaction relationships. The graph has 8,894 nodes and no cached Spring preparation record; truncated navigation results were not treated as complete evidence. Important facts were checked directly in files.

- **Module graph:** recursively scanned imports in the Java source tree and all top-level module descriptors. There are 20 top-level modules including `rag`, hence **19** in scope. Excluding `rag` and its incident edges gives **38 distinct explicit-import module edges**, exactly matching the corresponding subset of `docs/architecture/diagrams/source/module-dependencies.dot`. This is a source/import comparison, not fresh runtime Modulith verification. Existing frozen graph artifacts were not modified.
- **Structure:** recursive non-RAG source/root inventory contains 1,317 files, including 1,299 Java files and 367 `package-info.java` files; no `.gitkeep` remains in that inspected subtree. Public named-interface descriptors were inventoried. This establishes the source inventory for authoring, not a comprehensive audit of every class. The preparation subtree initially contains exactly four Markdown files; the proposed output directory does not yet exist.
- **HTTP ownership:** inspected root `ApiResponse`, `SecurityConfiguration`, `AuthController` and representative media controller/service code against ADR-0016 and the package-tree documentation. The root envelope remains cross-cutting support, not another module; controllers/exceptions remain with their owners. Spring Security's OAuth2 resource-server chain handles JWT authentication; no custom JWT filter was assumed.
- **Authentication:** `authentication/internal/application/session/SessionService.java` shows transactional login, hashed refresh-token persistence, locked predecessor rotation and refresh revocation. `PrivatePinService.java` exposes verification/change, not a delivered browser unlock session. The author must preserve these distinctions; the frontend inactivity/storage policy must not be drawn as implemented backend behavior.
- **Persistence:** `ImageService.create` owns the metadata transaction; `ImageUploadService` performs storage I/O and bounded compensation outside a single distributed transaction. `MediaStorageConfiguration` makes S3 conditional and otherwise supplies the disabled adapter. `PortabilitySnapshotAdapter.exportSnapshot` explicitly uses one read-only JDBC `REPEATABLE READ` snapshot, consistent with ADR-0017. It is an exception to a JPA-only shorthand, and must be visible when that operation is depicted.
- **Documentation baseline:** checked module boundaries/dependency matrix, security/data architecture, ADR-0015/0016/0017, repository/package-tree, existing diagram inventory and CSA26 independent re-review 2. The new plan does not replace frozen canonical sources or reopen accepted CSA26 findings.

## Archify suitability and execution limits

Public upstream source was inspected read-only at **`a4065941f12be7967436261d9934784e381d423c`**, not installed or executed. The [skill package manifest](https://github.com/tt-a1i/archify/blob/a4065941f12be7967436261d9934784e381d423c/archify/package.json) identifies **3.2.0**, Node >=18 and a local `bin/archify.mjs` entrypoint. Inspected the root/skill package manifests and skill lockfile, skill instructions, repository-authoring/delivery references, CLI/finalize call sites, delivery-update child path, update script controls, viewer template, browser-check launcher, and software-notices handling.

- Root and skill `LICENSE` contents agree on Apache-2.0. `NOTICE` and `LICENSE-MIT` preserve historical MIT grants; third-party/font notices also apply. Native output embeds software notices and the font license. Preserve them; a license/attribution URL is not itself an executable remote dependency. The optional DeepSeek harness is excluded from the chosen local route.
- The packaged renderer uses generated validators; its skill instructions specify no package installation inside the skill. Prefer a complete local extraction of this exact commit and direct Node entrypoint, outside tracked application dependencies. No `npm install`, mutable skills installer, auto-update, website build or adapter is needed for the proposed route. Before actual use, record the acquired package/tree hash, local entrypoint and commands in the handback; verify they match the inspected source. An installed copy has **not** been accepted by this review.
- [Delivery update source](https://github.com/tt-a1i/archify/blob/a4065941f12be7967436261d9934784e381d423c/archify/bin/delivery-update.mjs#L49) checks `ARCHIFY_UPDATE_CHECK_DISABLED=1` before spawning the updater; the update script separately checks the same control. This supports the proposed control but is **not runtime proof of universal offline operation**. The inspected native viewer template contains embedded assets/SVG export and no direct `fetch`, XHR, WebSocket or beacon call matching the source scan. Generated content and actual browser requests still require A6 verification.
- Native provenance accepts repository-relative `sources.path` and verified committed source ranges. `meta.repository.link_mode: "local-only"` suppresses upstream web source URLs; it does **not** produce clickable local source anchors. Use that mode with an index containing working repository-relative code links, and verify actual delivered navigation under A3. Do not promise native clickable local source cards or fabricate URL fields unsupported by the schema. Credential-free repository identity and actual execution revision remain required.
- The browser checker supports installed Chrome/Chromium/Edge and disables background networking in its launch arguments. Its explicit sandbox opt-out is not needed or approved here. No browser check, installation, renderer run, private-source submission or generated export occurred during preparation.

Reproducible UTF-8 source fingerprints from the pinned commit: `archify/package.json` SHA-256 `ea99289562037cae2deeced0b45e1374ce4c4b4f23a53a607a6aada62c803cd0`; `archify/package-lock.json` SHA-256 `ab7a1438a8edbd26ceda9cf9648e3ad50542bffd7cc87f9ec83793cde1f26287`. These are source-text fingerprints, not an installed-package/archive hash or a vulnerability-free claim.

## Harness, finite execution and verification

Reviewed the invoked pre-handoff skill, shared coverage/documentation/Graphify guidance, root/docs instructions, applicable documentation/context/workflow rules, `.agents/` and `.codex/` inventories, existing backend-implementer/opt-in auditor roles, hook configuration, Git rules and published watchdog contract. No new agent or skill is necessary. Python-RAG coding and a Codex production takeover are **N/A**: this work contains neither Python RAG changes nor an exhausted implementation loop. Native hook trust/platform limits remain as documented; no fresh hook-enforcement claim is made.

The approved sequence is sufficient: publish this reviewed preparation; author SAD-01–04 as one bounded documentation delivery; independent different-session review; Owner publication. Do not introduce four software handoffs or a numbered-phase gate. For later potentially long render/browser commands, use existing `scripts/run-bounded-command.py` with the **normal** profile, at most 300s per invocation and fresh ignored receipts. The 2,100s full-suite allowance does not apply. Do not infer adaptive control for commands that bypass the runner.

| Acceptance | Preparation evidence | Later mandatory artifact gate |
| --- | --- | --- |
| A1–A2 | PASS: explicit exclusions, 19/38 source baseline, provenance and transaction distinctions | Verify every actual node/relationship/source range at authoring HEAD; no RAG-specific interfaces/behavior |
| A3 | PASS: real interaction and source-link checks required | Exercise actual controls and relative index links; inspect the rendered output |
| A4 | PASS: typed source and exact tool identity required | Native schema/finalize checks, package hash and one semantic rerender |
| A5 | PASS: actual SVG fallback required | Open/render all four exports and assess labels/coverage at readable zoom |
| A6–A7 | PASS: private-data/network and preservation requirements explicit | Check generated HTML/SVG/IR and browser requests; compare tracked changes and frozen artifacts |
| A8 | PASS: separate author/reviewer and Owner publication required | Independent report with factual outcomes/limits; no author self-acceptance |

All later artifact gates are **NOT_RUN** here. No tests were supplied by Antigravity because this is preparation; no Maven, pytest, model/QA, database, performance or IDE inspection was run. No warning-free or full-system acceptance claim is made.

## Shared review coverage

Statuses below concern preparation adequacy. Runtime outcomes remain as stated above.

| Dimensions | Status / evidence and limits |
| --- | --- |
| 1–2: Owner intent, traceability | **PASS:** four bounded views and A1–A8; estimates and exclusions above |
| 3–4: architecture, module ownership | **PASS:** 19-node/38-import-edge source comparison, accepted contracts, no architecture change |
| 5: repository/package/folder structure | **PASS:** recursive inventory and four-file preparation subtree; no new Java package; not an all-class audit |
| 6: shared API/exception placement | **PASS:** ADR-0016/root envelope and module-local adapters preserved; no new common module |
| 7–9: SOLID, principles, patterns | **N/A** to source design changes; documentation reuses native tool/types without a framework |
| 10–11: business states, wire contracts | **PASS:** authentication/request plan is source-grounded; no API change or frontend-policy invention |
| 12–14: validation, privacy, credentials/network | **PASS:** typed/provenance/output checks specified; source-level tool assessment above; output/runtime proof **NOT_RUN** |
| 15–17: atomicity, recovery, database | **PASS** for depiction requirements: actual JPA/JDBC/storage distinctions and stop conditions; no schema/behavior edit or runtime revalidation |
| 18: resource/complexity bounds | **PASS:** four views, representative operations, existing normal watchdog; no measured latency/memory claim |
| 19–20: retrieval/OCR/document fidelity | **N/A:** RAG, indexing, parsers and OCR expressly excluded |
| 21–22: security/test quality | **PASS:** negative scope/privacy/preservation and native/browser/render gates specified; execution **NOT_RUN** |
| 23–24: benchmarks/evaluation science | **N/A:** no performance or model-quality deliverable |
| 25: dependency/license lifecycle | **PASS** for pinned-source suitability; actual acquisition hash and runtime privacy checks remain authoring gates |
| 26: build/tooling/static diagnostics | **PASS:** no build/lock/hook changes, source tool inspection and scoped link checks; render/browser/IDE **NOT_RUN** |
| 27: observability/evidence | **PASS:** factual handback and unique ignored receipt paths specified; no telemetry/logging feature added |
| 28: docs/governance | **PASS:** current register and maintenance route agree; out-of-scope stale notices explicitly deferred below |
| 29: hygiene/delivery | **PASS:** scoped status/review writes only, empty index, no existing source/export edits; saved-file whitespace/link and preservation checks passed |

## Docs Sync Check and reviewed identity

- **UPDATED:** [scope README](../README.md) review disposition and this dated review only. ACTIVE remains `NO_ACTIVE_HANDOFF`; no numbered-phase record was changed.
- **ALREADY_CONSISTENT:** the four preparation documents, submitted ACTIVE reset and [CSA26 publication closeout](../../2026-11-01-current-system-readonly-audit/owner-publication-closeout-2026-11-01.md); actual published SHA; P17 frozen/P18 closed/S19-1 accepted versus suspended archival S19-2; inactive indexing/disabled live originals. The linked [future audit intent](../../2026-11-01-final-non-rag-audit-intent/owner-intent.md) explicitly defers execution and does not authorize its feature list or amend the present gate.
- **DEFERRED_OUT_OF_SCOPE:** root `AGENTS.md` and `docs/workflow/current-owner-delivery-policy.md` still have CSA26 pre-publication status notices; scoped agent/rule notices retain older phase routing. The policy explicitly delegates current execution state to ACTIVE, and the new scoped decision is unambiguous. These existing notices need a separately authorized ChatGPT documentation cleanup; they do not reactivate CSA26/RAG. Existing CSA26-FR-L01, AUTH-CLOCK and older LOW backlog are not closed here.
- **NOT_VERIFIED:** new HTML/JSON/SVG/browser/offline artifacts, actual installed package identity and the future comprehensive audit. None is claimed accepted/published.

Before review status edits, all **22** relative Markdown file links across the seven submitted documents resolved. Final saved-file verification resolves **34** relative Markdown links with no missing target, confirms all six preserved input hashes, finds no trailing whitespace in this review, and confirms the empty index. `git diff --check` passed. The only tracked diff remains the Owner-submitted ACTIVE reset; the new artifact directory is still absent.

Reviewed unchanged preparation SHA-256: `owner-scope-and-authorization.md` = `9974cff6e109a9d3de5fab1b59d41115e4bf7357164e34b8f1a74b86d25948c2`; `diagram-plan.md` = `d098acf9d55b2d0b5051e54540a42bcad2517a088e033004f4a0ed84193890f4`; `diagram-authoring-task.md` = `878bda1a62c827006075a0d93151a6ab1945709d6664418c70fa71d333290239`. README receives only the review status/link/next action. Submitted ACTIVE/closeout/future-intent content remains unchanged by this invocation.

Suggested Owner commit message: `docs(architecture): prepare Spring-only interactive diagrams`.

Next step: Owner | commit/push the reviewed docs-only preparation, then start a new Codex session with the launcher in diagram-authoring-task.md | Spring-only maintenance; README and this review | READY_FOR_OWNER_COMMIT applies to preparation only; authoring requires published preparation and matched vetted local tool, followed by separate independent artifact review | no Spring remediation loop; CSA26 2/3 consumed and stopped; S19-2 counters unchanged.

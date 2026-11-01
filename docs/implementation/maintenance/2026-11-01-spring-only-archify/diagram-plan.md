# Spring-only diagram plan and prospective acceptance

**Status:** Owner-approved diagram scope for independent preparation review; this file alone is **not** an executable Codex/Antigravity implementation handoff. Baseline `main` = `4cf62041796d0b510255ee7014261221cafe3d5d`.

## Canonical evidence

| Truth to depict | Verify directly in source/current documentation before authoring |
| --- | --- |
| Non-RAG Java module graph and named interfaces | `backend/src/main/java/com/vhvkhangg/personalprivatevault/{module}/package-info.java`, public `@NamedInterface` packages, relevant service imports; `docs/architecture/module-boundaries.md`, `module-dependency-matrix.md` and `diagrams/source/module-dependencies.drawio` |
| HTTP/security boundary | `backend/src/main/java/com/vhvkhangg/personalprivatevault/authentication/`, root `ApiResponse`, `ApiExceptionHandler`, module-local controllers, `docs/architecture/security-architecture.md`, `api-architecture.md` and accepted ADRs |
| Access/refresh/PIN state | Authentication package token/session/PIN code and relevant tests; no invention of cookie storage, browser policies or absent frontend |
| Persistence/transactions | Non-RAG JPA entities/repositories, non-RAG migrations and `docs/database/`, `docs/architecture/data-architecture.md`; clarify transaction owner, external S3 boundary and no invented distributed transactions |
| Existing diagram baseline | `docs/architecture/structurizr/workspace.dsl`; `docs/architecture/diagrams/source/` and corresponding `exported/`, independently accepted CSA26 re-review 2; do not replace frozen v1 views |

A previous audit counted **20** top-level Java modules and **41** observed module edges **including RAG**; this prospective view excludes the `rag` module and all its incident edges. The earlier count is background evidence only: the diagram author must compute and verify the **19 non-RAG module nodes** and exact remaining edges at the actual execution HEAD. Do not silently treat allowed edges as observed dependencies. Avoid depicting scheduled external feed crawlers or cloud integrations as implemented unless supported by code.

## Requested Archify views

| ID | Mode / view | Minimum useful coverage | Negative checks |
| --- | --- | --- | --- |
| `SAD-01` | `architecture` — Spring Modulith map | All 19 non-RAG module owners, meaningful named interfaces, explicit observed cross-module connections with inspected source evidence; filter/highlight module relationships | No `rag` node/edges; no fabricated dependency, service deployment or frontend implementation |
| `SAD-02` | `sequence` — REST request lifetime | Representative authenticated route: client → security filters → controller → application/domain → repository/DB → HTTP envelope; normal and denied/error paths | No bypass of auth, no invented global transaction or unverified actual call chain |
| `SAD-03` | `sequence`/`lifecycle` — authentication | Login/bootstrap where applicable, JWT access/refresh, session revocation and private PIN; distinguish business behavior from future browser token storage decision | Do not assert cookie-based auth, token transport decision, external IAM or test-only branches as production |
| `SAD-04` | `architecture`/`dataflow` — persistence and side effects | Module-owned JPA/Flyway/PostgreSQL and transaction boundary, optional S3 adapter/media compensation; use 2–3 representative operations rather than every table | No RAG schema, no blind combined ERD, no unsupported S3 deployment or cross-module repository access |

**Optional:** `CSA26-FR-L01` is an existing separate LOW on the *current combined C4 export*, not in the four Spring-only views. Do not modify the canonical combined C4 merely to erase it unless Owner authorizes a distinct narrowly scoped correction. Preserve its OPEN status.

## Output structure (proposed)

```text
docs/architecture/diagrams/interactive/spring/
  README.md             # indexes, baseline and acceptance/preservation limits
  sources/
    spring-modules.architecture.json
    request-lifecycle.sequence.json
    authentication-lifecycle.sequence.json
    persistence-boundaries.architecture.json   # or reviewed matching type
  html/
    spring-modules.html
    request-lifecycle.html
    authentication-lifecycle.html
    persistence-boundaries.html
  exported/
    spring-modules.svg
    request-lifecycle.svg
    authentication-lifecycle.svg
    persistence-boundaries.svg
```

The exact source type/schema/filename layout must be reconciled against the vetted Archify version. If a reusable static export is not supported without browser privileges, report the limitation; do not fabricate it. Do not commit temporary `.archify/` workspaces, model data, caches, screenshots containing private content or generated runtime binaries without explicit review.

## Evidence and acceptance cases

- **A1 – Correct scope:** inspect source list/working-tree diff; 19 non-RAG module owners, all RAG content excluded from diagram IR/HTML/SVG and index. Note legitimate RAG references in background canonical documents do not authorize depiction.
- **A2 – Fidelity:** for every nontrivial node/edge/call sequence, link to relative source/ADR and mark documented vs inferred vs future. Verify a few paths dynamically with existing safe tests only where genuinely needed; do not require or trigger RAG tests/inference.
- **A3 – Interactivity:** actual browser click/hover/route/highlight and source links work; no dead controls or phantom graph paths. Verify against the tool's real checks and inspect visual layout at readable dimensions.
- **A4 – Determinism/source ownership:** preserve typed JSON source and record exact Archify version/hash; rerender one sample and compare semantic facts (not necessarily byte-identical generated HTML).
- **A5 – Static exports:** real SVG files open, labels are legible at normal review zoom, node/link coverage is preserved or exceptions documented. Prefer local browser rendering and stable HTML.
- **A6 – Privacy/security:** no secrets, remote data submission, absolute local paths, CDNs or unreviewed scripts; disable optional update checks if supported and verify no hidden network dependency.
- **A7 – Existing history unchanged:** no application code/schema/runtime/dependency/QA changes; accepted CSA26 diagram sources and exports unchanged except separately Owner-authorized narrow fixes; `ACTIVE=NO_ACTIVE_HANDOFF` at preparation.
- **A8 – Independent review:** author does not self-accept, reviewer records PASS/FAIL/NOT_RUN, exact evidence, version/hash and limitations; Owner-only commit/push after acceptance.

## Future non-RAG audit linkage

The Spring diagram set is **not** the final module/backend audit and cannot close its findings. The later detailed final non-RAG audit is a recorded **future Owner intent**, with execution still needing a separately approved audit contract **after** Inbox, Trends, Google Drive, Dashboard/API readiness are complete. Audit must verify ALL non-RAG modules and cross-cutting API/security/persistence mechanisms plus all documentation especially `docs/adr/`, `docs/architecture/`, `docs/database/`, and record actual artifacts/source-aligned statuses; see [forward Owner intent](../2026-11-01-final-non-rag-audit-intent/owner-intent.md).

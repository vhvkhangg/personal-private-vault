# Phase 14 — Codex Pre-Handoff Review

Date: 2026-10-06

Verdict: **CHANGES_REQUESTED**

Scope: preparation/governance only; no production implementation or implementation handoff.

## Preconditions and approval

- Local `HEAD` and `origin/main` both resolve to `ef92d94e4b551ec6c7449f251f5186f3e376e0c3`.
  No remote fetch was performed; this corroborates the recorded owner commit/push locally.
- Phase 13 is complete/frozen; its final acceptance and 867-test evidence remain unchanged.
- `docs/implementation/handoffs/ACTIVE.md` is `NO_ACTIVE_HANDOFF`.
- Phase 14 README and preparation-review exist. No Phase 14 production code/tests have been added.
- Owner explicitly approved the Phase 14 concept and ADR-0017 on 2026-10-06 for preparation review only.
  Approval is recorded in the Phase 14 README, preparation-review, and ADR-0017.
- The immediately preceding implementation phase is 13; no additional milestone gate is due.

The approval prerequisite is satisfied. These findings do not request broader architecture approval or authorize
implementation. Preserve all existing ownership/dependency edges, schema and frozen behavior beyond the approved
new leaf/export-only JDBC exception and the separately prepared Media integration scope.

## Findings

### P14-1 — Medium: binary streaming failure and resource-lifetime contract is incomplete

Locations:

- `docs/implementation/phase-14/README.md`: read-only snapshot safeguards, HTTP endpoint, Binary download,
  and Operational/API testing sections.
- `docs/architecture/api-architecture.md`: section 4, unconditional JSON-error requirement for binary routes.
- `.agents/skills/backend-integration-portability-storage/SKILL.md`: Binary HTTP.
- `.agents/rules/backend-phase-14-integration-closure.md`: binary-error rule.

The preparation combines direct streaming with an unconditional requirement that errors return JSON, but does not
distinguish failure before response commitment from a read/write failure or client disconnect after bytes have been
sent. A committed servlet response cannot be reset into a JSON error response. This makes the stated acceptance
contract impossible for ordinary mid-stream failures. [Jakarta Servlet response contract](https://jakarta.ee/specifications/platform/11/apidocs/jakarta/servlet/servletresponse).

The single snapshot transaction is specified, but its lifetime relative to an asynchronous response callback and
cleanup on cancellation are not. If asynchronous MVC streaming is selected, the transaction must actually encompass
all reads on the executing thread rather than only construction/return of a callback. Spring documents asynchronous
`StreamingResponseBody` execution and thread-bound imperative transactions.
[StreamingResponseBody](https://docs.spring.io/spring-framework/docs/6.2.x/javadoc-api/org/springframework/web/servlet/mvc/method/annotation/StreamingResponseBody.html),
[Transactional](https://docs.spring.io/spring-framework/docs/6.2.x/javadoc-api/org/springframework/transaction/annotation/Transactional.html).

Required preparation remediation:

- Define pre-commit validation/storage-open failures as canonical JSON errors; define post-commit failure as aborted
  transfer, without appending JSON or presenting a partially generated ZIP as a completed export.
- State how callers distinguish a complete export from a partial one, and define transaction/cursor/object-stream/
  temporary-file release on success, failure, timeout and client disconnect. Keep the implementation choice open;
  do not introduce a generic streaming framework or require whole-archive heap buffering.
- Require regression evidence for both failure boundaries, snapshot consistency during actual consumption, and
  cleanup after cancellation. Synchronize the HTTP docs, integration skill and rule to that achievable contract.

### P14-2 — Medium: upload compensation assumes deletion succeeds and leaves the commit boundary unclear

Locations:

- `docs/implementation/phase-14/README.md`: Managed image upload steps 5–8, Object storage testing.
- `docs/implementation/phase-14/preparation-review.md`: required check 11.
- `.agents/skills/backend-integration-portability-storage/SKILL.md`: Media object storage.

The prepared workflow ends with “metadata failure -> delete uploaded object” and asks the review to establish that
this avoids orphans. Deletion is another fallible remote operation; no outcome/recovery policy is stated if it fails
or the process stops between upload and metadata commit. A storage timeout can also leave upload completion
uncertain. The current contract therefore overstates the cleanup guarantee under the very outages this phase is
intended to handle.

The existing `media/internal/application/ImageService.create` is transactional. The preparation should distinguish
a confirmed metadata commit from a normal service return inside a surrounding transaction, and from a failure with
an uncertain commit outcome. Otherwise compensation can miss a commit-time failure or remove an object that is
already referenced by committed metadata.

Required preparation remediation:

- Define the metadata commit/success boundary and which failures allow deletion of this attempt's server-owned key;
  do not delete objects after confirmed success or blindly compensate an uncertain commit outcome.
- Define bounded compensation retry/failure handling and a privacy-safe operator recovery procedure or explicitly
  accepted residual-orphan limitation for interrupted/uncertain operations. Do not promise cross-system atomicity.
- Require tests for commit-time failure and failed compensation as well as the existing checksum-race cleanup;
  confirm no partial Image/Vault state, no false success, no masking of the primary failure, and no private-key/
  provider-detail leakage.
- Keep remediation within the current schema/ownership boundaries: no new outbox/job table, scheduler, broad hard
  delete or reconciliation platform is implied or authorized.

### P14-3 — Medium: the approved architecture delta is not synchronized with canonical package/diagram artifacts

Locations:

- `docs/architecture/architecture-overview.md`: sections 5 and 7 already include `portability`.
- `docs/architecture/module-boundaries.md` and `module-dependency-matrix.md`: already include the new row.
- `docs/repository/repository-package-tree.md`: sections 2 and 4 still list only the original 18 top-level modules;
  section 8 has no export-only JDBC exception and the ADR list omits ADR-0017.
- `docs/architecture/diagrams/source/module-dependencies.drawio` and `module-dependencies.dot`, plus their review
  exports: still show only the original 18 modules, with no `portability` node or documented approved delta.

The architecture now has conflicting canonical inventories. The Phase 14 README proposes a package shape but does
not synchronize or explicitly version the canonical package/module diagrams. An implementer must not resolve this
by silently changing a frozen baseline while following a future handoff.

Required preparation remediation:

- Record the narrow ADR-0017-approved addition in the canonical package tree and module diagram source, and regenerate
  affected review exports; alternatively, explicitly retain/version historical v1 artifacts and link an authoritative
  Phase 14 delta view. Clearly distinguish planned approved structure from already implemented code.
- Preserve every existing module/edge/table-owner rule. The new node has no business-module dependency edges; JDBC
  snapshot access must not be rendered as permission to import business internals or repositories.
- Identify unaffected artifacts rather than changing them gratuitously: the current C4 DSL is container-level and
  need not change for a new internal module; the BFD already includes Export / Backup.
- Do not expose a new named interface merely for same-module controllers/tests. Require package descriptors only
  for meaningful packages actually introduced during authorized implementation.

## Other review dimensions

| Dimension | Assessment |
| --- | --- |
| Scope/non-goals | Three bounded integration areas; provider/deployment, schedulers, backup automation, frontend/RAG and full-archive import remain deferred. |
| Database/ownership | No table/migration/DBML change; explicit export allowlist, auth/Flyway exclusion and read-only repeatable-read design are appropriate. Physical enforcement remains an implementation test obligation. |
| Module graph/API | Proposed leaf adds no cycle; Media retains its Vault-only business dependency; no foreign JPA/internal access is approved. P14-3 concerns documentation synchronization. |
| HTTP/test evidence | Existing 211-route and 867-test baseline preservation is explicit; PostgreSQL/S3 integration and concurrency coverage are appropriate, subject to P14-1/P14-2. |
| Security/privacy | Bearer protection, secret exclusion, no-store, opaque keys and public-health redaction are specified; no deployment-security expansion is assumed. |
| Tooling/reuse | One focused integration skill/rule is justified; existing implementer/auditor/domain/engineering guidance is reused. No new agent or hook is warranted. |
| Package hygiene | No new production package/scaffold exists; package inventories require P14-3. Existing migration/test-root `.gitkeep` files are baseline hygiene debt, not introduced here; no unrelated cleanup is authorized. |
| Status/links | Phase 13 closeout and handoff archive/reset are coherent; scoped relative Markdown links pass. This review synchronizes current preparation statuses to CHANGES_REQUESTED. |
| Handoff precision | Blocked by P14-1–P14-3; ACTIVE remains NO_ACTIVE_HANDOFF. |

## Verification and diagnostics

- `python -B -m unittest discover -s .agents/hooks -p test_repository_safety.py`: **13 tests passed**.
- `git diff --check`: passed; Git emitted line-ending normalization notices for existing files, not whitespace failures.
- Scoped relative Markdown-link check before review edits: **46 checked, 0 broken**.
- Final expanded documentation-link check after review edits: **78 checked, 0 broken**; no trailing whitespace in
  the 17 reviewed preparation/governance documents.
- No Java/pom/migration/DBML/hook implementation changes are present. Historical Phase 13 dated reviews and
  test-evidence have no diff. The 867-test backend verification is retained evidence, not a new run in this review.
- Maven and IDE inspections were not run for this documentation/tooling preparation review. No IDE-clean claim is
  made. Git's existing CRLF/LF notices are configuration-dependent; P14-1–P14-3 are actionable preparation defects.
- Graphify was used only for navigation; the existing graph is bounded/stale relative to this unimplemented phase,
  and important conclusions were checked in canonical documents/source.

## Changes made by this review

Recorded the owner's limited approval in the Phase 14 README/preparation-review and ADR-0017; added this review and
its index entry; synchronized current preparation/governance status references. No technical scope remediation,
production code, schema, dependency, `.agents` content, diagram/package baseline, or active handoff was changed by
Codex. Existing user preparation changes were preserved.

## Next step

Give ChatGPT this report and the latest preparation package for narrow documentation/tooling remediation of
P14-1–P14-3, then rerun `$codex-pre-handoff-review`. Owner approval need not be requested again for this unchanged
scope. Do not commit as accepted preparation or run `$codex-create-handoff` until `READY FOR HANDOFF`.

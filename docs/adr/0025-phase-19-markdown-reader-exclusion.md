# ADR-0025: Phase 19 Markdown Active-Reader Exclusion and Incident Inspection

- **Status:** Owner-approved Option A design (2026-10-27); implementation candidate awaits independent acceptance.
- **Date:** 2026-10-27.
- **Context:** [Owner Decision P19-OD-01](../implementation/phase-19/owner-decision-p19-reader-exclusion.md), [P19-S19-1 Executable Handoff](../implementation/phase-19/s19-1-executable-handoff-2026-10-27.md), [Query Consumer Lifetime Contract](../implementation/phase-19/query-consumer-lifetime-and-selection.md).

## Context

During read-only CPU Q&A and retrieval, source authority must be protected against races with normal source edits, revocations, resets, and purge operations. An active reader cannot emit evidence after a committed revoke or privacy reset. At the same time, long-lived database locks across model inference or HTTP emission are unacceptable in a low-contention architecture.

ADR-0020 and ADR-0022 established single-job reservations and immutable snapshot authority for ingestion and indexing, but did not define the lifetime or exclusion semantics for read-only query consumers.

## Decision

We adopt **Option A**: a single durable active-reader record in PostgreSQL (`rag.retrieval_readers`), integrated with existing source-reservation and mutation guards:

1. **Admission (`C_A`):**
   - Validates that no active reader exists (`state <> 'TERMINAL'`).
   - Rechecks canonical prepared-current source and complete dependency authority, exact consent/revision/manifests/pipeline and runtime. External WHOLE_FILE consent controls eligibility; internal private/nsfw markers are not paragraph filters. Dependencies need not have an unrelated own index.
   - Verifies no pending or unresolved authority incidents exist on the affected sources.
   - Atomically reserves the external file sources in `rag.external_file_sources` using a unique reservation token, and inserts a reader with state `ADMITTED`.

2. **Durable Exclusion during Processing:**
   - Normal conflicting owner mutations (edits, approvals, revokes, dependency additions) encounter reserved sources and return the existing no-change `409 RAG_SOURCE_BUSY`.
   - Index generations, generation roots, and document chunks referenced by an active reader cannot be modified or purged due to database triggers.
   - No SQL lock spans model execution or HTTP emission.

3. **Preregistered Inspection and Monotonic Incident Fence (`C_I`):**
   - Any authority-sensitive inspection (e.g. during reconciliation or capture) is preregistered in `rag.retrieval_inspections` with `UNRESOLVED` before inspecting bytes. Its exact revision/hash/length and controller identity are immutable recovery proof. Standalone obligations also fence affected currentness, indexing and unsafe cleanup; retries/recovery classify the original operation.
   - If an incident is observed, `C_I` commits `INCIDENT_PENDING` and sets `deny_new_admission = true`.
   - If the incident commits before emission (`ADMITTED` or `CONSUMING`), the reader transitions to `STOP_REQUESTED`, preventing emission.
   - If emission has already begun (`EMITTING`), the existing server-side emission is allowed to safely complete, while `deny_new_admission` blocks fresh admissions.

4. **Emission Authorization (`C_E`):**
   - A short guarded transaction transitions the reader to `EMITTING` only after verifying no stop request, pending incident, or unresolved inspection exists.
   - The reader exclusion remains through owned Servlet async/nonblocking `WriteListener` output, readiness checks, callback quiescence and actual container completion, outside SQL transactions. The HTTP controller supplies the explicit response DTO adapter. This corrects the prior implementation description to the already approved sender/wire contract; no contract amendment is made.

5. **Guarded Terminal Release (`C_RL`):**
   - After verified positive sender and child quiescence, the reader finishes via `rag.retrieval_reader_finish`.
   - If an incident occurred, `C_D` atomically marks `snapshot_integrity_failed = true`, invalidates only generations whose own contributing manifest includes the affected file/revision (including true dependent roots), marks inspections `DENIED`, and releases reservations. Unrelated admitted siblings remain valid.
   - Every outcome retains unresolved inspection obligations. Denial is restricted to exact admitted and observed file/revision tuples. Unknown controller, child or sender stop retains exclusion for positive-proof reconciliation; no TTL/PID-only release.

## Consequences

- Exactly one active retrieval reader is allowed at any time (`RAG_READER_BUSY` fail-fast on concurrency).
- Conflicting mutations fail fast with `409 RAG_SOURCE_BUSY` without committing changes.
- Safe atomic `C_D + C_RL` prevents any release-before-denial gap.
- No long SQL transactions or streaming response races.

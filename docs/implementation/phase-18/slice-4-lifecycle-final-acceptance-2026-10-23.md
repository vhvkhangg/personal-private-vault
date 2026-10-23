# Slice 4 Lifecycle & Final — bounded acceptance matrix (D25)

**Status:** Prospective acceptance planning only; all rows `NOT_RUN`, not PASS. This matrix does not alter historical findings. Codex must attach source-backed executable cases and receipts before implementation acceptance.

| ID | Required outcome | Minimum decisive evidence |
| --- | --- | --- |
| S4-01 | Real NO_CHANGE requires unchanged verified R, authority, child closure, pipeline and eligible G; no worker/new G/R/version | Original same-bytes capture; assert unchanged DB/source/R/G and zero worker start |
| S4-02 | Same source bytes but changed child, model/pipeline/grant/policy/selected G is **not** falsely NO_CHANGE | Parameterized child/vector/metadata/authority negatives with explicit outcomes |
| S4-03 | Same-R Reindex uses valid immutable R and selected children, new verified G only; no re-capture | Native Java → Python → PG proof with exact R ID, new G ID, unchanged R and old G protection |
| S4-04 | Failed/no-authority reindex does not select partial G or erase verified prior G | Forced failure, revocation, malformed/forged output and physical rollback |
| S4-05 | Fresh G/read eligibility checks exact selected R, hash, graph, model, consent/reset/retention; no false CURRENT | Source/G tampering, changed child, grant withdrawals and restarted read-path probes |
| S4-06 | Original file freshness marked `UNVERIFIED`; snapshot qualification does not silently reopen original | Deleted/changed live-original tests, user-visible reports and DB selections |
| S4-07 | Revoke/purge durable anti-resurrection and prior-G lifecycle safe | Concurrent native revoke and restart/restore/orphan cases, no unauthorized query visibility |
| S4-08 | Restore/reapprove requires new valid authority; cannot resurrect stale child/vector/edge | Real API/DB regeneration tests with version fencing |
| S4-09 | Cancel/recovery and process stop/ownership preserve fences/temp safety | Worker/validator staging, owned stop and unknown status, no premature release |
| S4-10 | Unknown committed/uncommitted outcomes durable, exactly one C_P or zero | Wire loss/DB outage native receipts, no duplicate G/outcomes/promotion |
| S4-11 | Accurate per-file outcomes and warnings for success/fail/NO_CHANGE/reindex/retained G | API+PG reports after 8/2, all-failed, cancellation and retention |
| S4-12 | Representative controlled cleanup, disk-full, temp/provision errors safely accounted | Owned-temp and restart/cleanup cases without owner data deletion |
| S4-13 | Consolidated removal of excluded unaccepted format code/deps/tests/fixtures without breaking MD/DOMAIN | Precise deletion+retention manifest, format-denial cases, no obsolete adapters/routes/imports |
| S4-14 | Cleaned pinned offline runtime, no implicit downloads, strict .md only | Dependency/lock check and producer/Java/PG selected-format cases |
| S4-15 | Full integration resources, CPU/RAM/disk/VRAM limits, cancellation measured honestly | OS-owned process metrics and fresh raw receipts, CUDA availability qualified |
| S4-16 | Historical aggregate PostgreSQL connection exhaustion investigated, truly corrected or explicitly blocked | Bounded diagnostic and repeated genuine full Java `clean verify` with source identity |
| S4-17 | P18-FINAL exact five-command executable gate genuine exits, counts, no fake zero | Ruff check + format + mypy + full pytest + Maven clean verify in isolated receipt dir |
| S4-18 | Final independent review and Owner Phase 18 decision; no automatic activation | Full S4 matrix, AC/FCNC/IS applicability, retained historical errors and verified code/evidence inventory |

**Controlled preservation:** Historical failed tests, independent reviews, Owner decisions, original/source hashes, D22 `NOT_RECOVERED` record, immutable PostgreSQL migration history and D17/20/23/24/25 acceptance evidence are retained. Code for excluded formats need not be retained for hypothetical future support. `S4-01–18` remain `NOT_RUN` until actual future verification; the review may refine *tests* and split rows without silently weakening frozen contracts.

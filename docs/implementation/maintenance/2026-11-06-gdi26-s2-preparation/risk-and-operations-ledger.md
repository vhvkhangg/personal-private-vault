# GDI26-S2 — decision closure, risk and operational gate ledger

## Decisions and current authority

| Item | State | Evidence / action |
| --- | --- | --- |
| D00–D07 | `OWNER_APPROVED` (2026-11-05) | [Original dated decision record](../2026-11-05-google-drive-integration-preparation/owner-decision-record-2026-11-05.md); only S1 implementation accepted/published. |
| D08 direct-child root only | `OWNER_APPROVED A` (2026-11-06) | [S2 decisions](owner-decision-record-s2-2026-11-06.md). |
| D09 original binary files only | `OWNER_APPROVED A` (2026-11-06) | No Google-native export/conversion. |
| D10 50MB upload cap | `OWNER_APPROVED A` (2026-11-06) | Existing servlet 50MB/55MB; no **persistent** upload-session resume obligation; Google resumable transport remains allowed/recommended above 5MB. |
| S2 preparation | `READY_FOR_HANDOFF` | [Independent re-review 1](reviews/2026-11-06-gdi26-s2-independent-pre-handoff-re-review-1.md): PH-01..03 VERIFIED / CLOSED for preparation; PH-04 narrow-scope lookup source-documented/resolved; PH-L01 LOW / OPEN. Owner publishes reviewed preparation before a separately invoked Codex handoff. `ACTIVE=NO_ACTIVE_HANDOFF`. |
| S2 implementation | `NOT_AUTHORIZED / NOT_STARTED` | Independent READY_FOR_HANDOFF + Owner publication + separately created executable handoff required. |
| S1 Google deployment/real OAuth | `NOT_VERIFIED / OFF` | S1 closeout; do not activate because S2 prep exists. |
| S3 Explorer / Phase 16 | `DEFERRED` | No UI/Picker or browser refresh-token exposure. |
| S1 LOW backlog | `OPEN` | FR-L01 leaf descriptors, FR-L02 inventory citation, PH-L01 historical anchor retained for consolidated hygiene. |
| S2 remediation | `0/3 consumed` | No Antigravity/Codex implementation round or takeover approved. |

## Risk and proportional mechanism

| Risk / severity if realized | Minimal planned containment / proof | What must not be claimed |
| --- | --- | --- |
| First ensure blocked by false READY prerequisite; two same-name folders or timeout-after-create | Common S1 Owner/subject/epoch/fence on **every** route; `ensure` permits ABSENT→PENDING→READY with pre-generated ID/root UUID and short CAS, while content operations require READY; same-ID reconciliation | READY before first create; exactly-once remote create across ambiguity without evidence |
| Folder binding lost on S1 disconnect | Retain minimal separate subject/root mapping, no token; reconnect rechecks real folder | That disconnect erased all noncredential local metadata or deleted remote files |
| `drive.file` grants broader app-created access outside PPV folder; U file moved to V root | Mandatory `appProperties` root/file IDs plus per-root UUID marker, current S1 subject/epoch and exact parent/trashed/type check; U-in-V negative; no name-scan | OAuth or a project-wide generic marker is an enforceable folder/subject sandbox |
| Upload accepted but Google or PPV response lost, caller retries after restart; duplicate/payload mismatch | Required timestamped UUID key, one durable per-root/subject action ledger, immutable content fingerprint + one preallocated provider ID, pre-dispatch state, exact-ID GET-only reconciliation, stale-key rejection after prune, finite capacity; 50 MB spool | Exactly-once global transaction, resume-after-restart, automatic deletion or success inferred from 404 |
| Moved-out/Google-native document download | Fresh metadata, parent check, reject shortcuts/Docs editors, streaming with bytes cap and safe attachment | File/parent status never changes between remote metadata and content calls |
| Expired/cancelled S1 credential mid-stream | Subject/epoch/PH-03 fence checks before dispatch and before final success; no promise to cancel remote committed work | Revoking a grant instantly undoes an in-flight Google request |
| Provider 401/403/404/429/5xx or read timeout | Safe classified errors, fixed endpoints, deadline/size bound, no secret in logs/URL | Green fake Google tests certify real Cloud quota/permission/consent |
| Drive OFF yet feature exposed | Independent files subflag default OFF and configuration/route tests; no implicit activation | Code publication authorizes actual OAuth or unattended file I/O |
| Portability backup exposes integration metadata | Explicit exclusion/migration/backup review; no tokens in root row | All database backup ciphertext/metadata automatically safe from leakage |
| Overengineering | One Drive module, **two minimal Drive-owned relations** (root binding and upload-action identity), same four routes, no full file catalog/scheduler/worker | Distributed multi-JVM coordination or a full Drive Explorer was requested |

## Distinguish product decisions from technical review risks

The Owner has **no remaining mandatory product question** under selected S2. The following *technical design checks* are intentionally delegated to the independent reviewer/handoff without extending scope: exact typed root state/CAS; bounded pagination cursors; key timestamp/fingerprint constraints; upload-intent capacity/retention and exact-ID reconciliation on timeout; provider body/stream timeout budgets; explicit root metadata retention after disconnect; and correctness of project-grant fence reuse. A materially different feature (deletion, shared folders, nested traversal, restricted broad scope, automatic scanning, much larger upload, or change to disconnect privacy semantics) must return to Owner before handoff.

## Deployment is a separate operational gate

- Before real Google Drive use: operator provisions OAuth client/API/consent/test users, exact localhost redirect, external AES key ACL+backup, starts **one backend JVM** without overlapping restart, and independently validates real browser callback and Drive API in a controlled environment. S1 has **not** proved any of that. Never put Google client credentials, refresh tokens or key material in source/docs/receipts.
- PPV `PPV_DRIVE_ENABLED=false` stays OFF. Proposed additional `PPV_DRIVE_FILES_ENABLED=false` stays OFF until separately approved. Feed scheduled polling OFF; RAG external indexing INACTIVE, S19-2 SUSPENDED. Publishing S2 code does not authorize live operation.
- Scope checks, risk tests and docs links are necessary for review; Google free-tier/account storage, upload quota, editor files, abuse blocks, actual provider latency and deployment safety are NOT_VERIFIED.

**Docs Sync Check:** `UPDATED` new independent re-review and current README/risk/index status to READY_FOR_HANDOFF, PH-01..03 preparation closure, resolved PH-04 account-root lookup and PH-L01 LOW register. Original review, Owner record and ACTIVE unchanged. `ALREADY_CONSISTENT` main baseline, S1 closeout, OAuth/scope/JWT boundaries. `DEFERRED_OUT_OF_SCOPE` S1 LOW/other backlog and S3. `NOT_VERIFIED` all S2 runtime and operational acceptance; no blocking preparation unknown remains. No prior reviews rewritten; no Git side effects.

**Next step:** Owner | normal git add/commit/push, then Codex `$codex-create-handoff` | GDI26-S2 reviewed preparation and [re-review 1](reviews/2026-11-06-gdi26-s2-independent-pre-handoff-re-review-1.md) | READY_FOR_HANDOFF; publish actual preparation before executable handoff, no activation | S2 Antigravity 0/3, takeover NOT_AUTHORIZED.

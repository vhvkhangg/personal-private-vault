# GDI26 — D00–D07 decisions, reuse matrix and risk ledger

**Status: `READY_FOR_HANDOFF` for connection-only GDI26-S1** on 2026-11-05 after [independent re-review 2](reviews/2026-11-05-gdi26-independent-pre-handoff-re-review-2.md). **PH-01/PH-02/PH-03 CLOSED for preparation**; runtime proof remains NOT_RUN. GDI26-PH-L01 is LOW/OPEN and nonblocking. The [initial review](reviews/2026-11-05-gdi26-independent-pre-handoff-review.md), [re-review 1](reviews/2026-11-05-gdi26-independent-pre-handoff-re-review-1.md) and Owner decision remain preserved. `ACTIVE=NO_ACTIVE_HANDOFF`; implementation NOT_STARTED; remediation 0/3 unchanged; no takeover.

## Owner decisions — all required product choices resolved

| Decision | Owner approval (2026-11-05) | First effect |
| --- | --- | --- |
| D00 | BOTH: independent app-managed Drive storage + later Explorer; Media S3 unchanged | S1 identity/token boundary only |
| D01 | A `drive.file`/OIDC; app-folder-only policy, **not** entire Drive | S1 scope, S2 folder guards |
| D02 | A three slices | S1 connections only; S2 file; S3 web |
| D03 | A backend first | No Picker/Phase16 in S1 |
| D04 | A exact callback, PKCE, AES-GCM key file, module/table | S1 reviewed proposal |
| D05 | A no destructive/cross-account/sync work | All three slices |
| D06 | A app creates per-account PPV folder | S2, not S1 |
| D07 | A localhost first | S1 dev OAuth; remote later |

**Decision proof:** [dated Owner record](owner-decision-record-2026-11-05.md). These decisions do not authorize production implementation. PH-01/PH-02 closed in independent re-review 1; PH-03 closed for preparation in independent re-review 2. No runtime acceptance is inferred.

## KEEP / REUSE / SIMPLIFY / DEFER / REJECT

| Class | Capability / design | Reason / burden |
| --- | --- | --- |
| KEEP | Two independent Google subjects/slots, encrypted credential, callback guard | Core two-account need; moderate code, substantive focused security/PostgreSQL verification |
| REUSE | PPV JWT, one root `ApiResponse`, Spring Security/OpenAPI, Flyway/Testcontainers | No parallel app login, no generic OAuth framework; keep frozen behavior |
| KEEP NARROW ADDITION | New Drive-owned module + `drive_connections` table and exact callback matcher | Account external profiles and Media S3 cannot safely store OAuth credentials; finite amendment review |
| SIMPLIFY | S1 connect/status/reconnect/disconnect; no first-slice file list | No UI/Pickering dependency, bounded code and tests |
| DEFER | S2 create folder/list/upload/download; S3 Explorer | Owner-approved roadmap but separate implementation authorization |
| DEFER | Delete/move/rename/share/cross-copy/background sync, broad scopes | No Owner need in first version; avoid data loss/verification sprawl |
| REJECT | Whole-Drive scan or restricted scope, raw tokens in notes/settings, service account impersonation | Contradicts selected scope/security |
| REJECT | Drive as default Media S3 replacement, queued job engine, RAG indexing | Conflicts with frozen/deferred contracts |

## Key risks, disposition and evidence

| Risk / reviewer finding | Status | Design mitigation / future observable proof |
| --- | --- | --- |
| PH-01 product/UI/scope ambiguity | **CLOSED FOR PREPARATION** | D00–D03 and D05–D07 explicit; S1 connection-only, S2/S3 gated; [independent re-review 1](reviews/2026-11-05-gdi26-independent-pre-handoff-re-review-1.md) |
| PH-02 callback/key/ownership ambiguity | **CLOSED FOR PREPARATION** | Exact localhost callback; browser-bound cookie+state+PKCE; Drive-owned table/module; independently closed in re-review 1 (runtime NOT_RUN) |
| **PH-03 remote project-grant revoke ↔ reconnect** | **MEDIUM / CLOSED FOR PREPARATION** | [Re-review 2](reviews/2026-11-05-gdi26-independent-pre-handoff-re-review-2.md) accepts the in-process project/subject fence, stale-attempt cutoff, single revoke, uncertain outcome and propagation limits; [contract](oauth-security-and-data-governance.md#31-ph-03--same-subjectproject-revoke--reconnect-ordering-s1-mandatory), [GDI26-08/13](acceptance-and-review-plan.md). Implementation proof remains NOT_RUN. |
| **GDI26-PH-L01 historical review anchor** | **LOW / OPEN / NONBLOCKING** | The original immutable review's former material-choices anchor no longer exists in the corrected scope register; the target file still opens. Preserve the review; defer a compatible anchor/navigation repair to bounded docs maintenance or the future audit. See re-review 2. |
| Misinterpret `drive.file` as folder-wide Google ACL | ACCEPTED RISK CONTROL | Only app-created/authorized objects; enforce app-root current parents on each operation in S2; no external file auto-discovery |
| OAuth login CSRF / state swap | DESIGN GATE | One-use 5m state+browser cookie, slot/owner binding, OIDC nonce and PKCE; negative tests |
| Durable refresh token/backup leak | DESIGN GATE | AES-256-GCM, key external owner-only file, AAD binds slot/subject/epoch; no logs; missing key fail closed |
| Two-account mix/races | DESIGN GATE | DB unique `sub`, slot CHECK and credential epoch CAS **plus** project/subject in-process revoke fence; deterministic grant-wide barrier in either slot and late-provider effect tests; no SQL lock across HTTP |
| Callback requires browser, no frontend yet | DOCUMENTED LIMIT | Backend initiation/return can be validated using controlled same-browser requests; no shipped Explorer in S1 |
| Local OAuth consent & 7-day Testing expiry | OPERATIONAL NOT_VERIFIED | Cloud client, test-users/consent, exact redirect; S1 mock tests not live-token certification |
| Key rotation/recovery | OPERATIONAL NOT_VERIFIED | Key-loss fail-closed; manual reconnect after controlled key restore; no unsupported auto-rotate claim |
| Unexpected Feed/RAG activation | SEPARATE OWNER GATE | Feed scheduler stays OFF, RAG indexing inactive; no new scheduled Drive jobs |
| Broad module/schema rewrites | PREVENTED IN SCOPE | Only additive Drive-owned relation/module; pre-handoff security/Flyway/Modulith review |

## No remaining material Owner selection before S1 preparation review

Google Cloud setup, whether actual two Google accounts grant consent, redirect registration, HTTPS domain, local browser cookie behavior, encryption file permissions and live consent verification are **operational/test evidence**; none is implied PASS by Owner's decisions. An independently identified *new* incompatibility may require a finite Owner decision, but do not create a speculative decision questionnaire, new feature or package before such finding.

**Docs Sync Check:** `UPDATED` seven preparation docs + new dated Owner record; `ALREADY_CONSISTENT` S2B published and scheduler OFF, `ACTIVE=NO_ACTIVE_HANDOFF`, Media S3 frozen; `IMMUTABLE` initial review; `NOT_VERIFIED` all Google/Spring/PostgreSQL runtime acceptance; `DEFERRED` full UI, restricted Drive scopes and existing LOW backlog.

**Next step:** Owner → Codex | Owner publishes reviewed preparation, then Codex `$codex-create-handoff` | connection-only GDI26-S1 / independent re-review 2 | READY_FOR_HANDOFF; no additional pre-review or product selection required | ACTIVE=NO_ACTIVE_HANDOFF; implementation NOT_STARTED; remediation 0/3 unchanged; takeover NOT_AUTHORIZED.

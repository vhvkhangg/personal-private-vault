# GDI26-S2 — prospective acceptance and independent review plan

**Status: NOT_RUN / NOT_IMPLEMENTED / NOT_ACCEPTED.** `ACTIVE=NO_ACTIVE_HANDOFF`. The S1 independently accepted 224 tests are historical and cannot be counted as fresh S2 evidence. GDI26-S2 is a separately gated capability with new D08–D10 Owner selections. Focused tests are preferred over a redundant full repository suite; any changed-source or security drift must be justified and verified.

| Case | Mandatory positive / negative observable behavior | Evidence required in a future implementation |
| --- | --- | --- |
| **S2-01** | Drive OFF: 235 routes, zero file provider beans/egress; S1-only ON: 239 routes; files additionally ON: projected 243 routes; contradictory flags reject | Real Spring OFF/S1/S2 contexts, route inventory and recording fake HTTP |
| **S2-02** | JWT required for all four new endpoints; bad/expired JWT, forged Google token and slot 0/3 reject; S1 OAuth callback matchers unchanged | SecurityFilterChain tests and negative API calls |
| **S2-03** | First root creation for U and V begins in ABSENT with current S1 authority (no pre-existing READY); per-subject durable root binding plus **mandatory** appProperties and unique provider ID; no name matching | Google fake create/get and PostgreSQL binding assertions |
| **S2-04** | **ABSENT→PENDING→READY** only after verified create, current S1 subject/epoch/fence and root CAS; concurrent/restarted ensures use one root ID; revoked/disconnected/epoch-changed request may leave PENDING but cannot publish READY or grant file access | PostgreSQL unique/epoch barriers and provider create counter |
| **S2-05** | PENDING/RECONCILIATION_REQUIRED after root timeout, 5xx or late success; same-ID GET-only recovery; 404 does not release ID; READY verifies My Drive parent/type/trashed/mandatory markers; file endpoints fail until READY | Fault injection with late-commit barrier and repeated ensure |
| **S2-06** | Root wrong parent/type, trashed, moved, missing/wrong `ppvKind=root`, `ppvRootBinding` or `ppvCreatedId`, or 404/403: fail closed; same-name folder never substituted | Fake provider metadata matrix |
| **S2-07** | Paginated direct children only, bounded 1–50 page, client cursor bound to subject/root/query, no recursive traversal | Controlled multi-page Google fake, cross-slot/replay/malformed cursor negatives |
| **S2-08** | Only direct-child authorized binary files with all root-bound file marker fields and `ppvCreatedId=id` appear; external file, copied U file under V root, missing/mismatched markers, shortcut/folder/native Doc omitted; no full-Drive fallback | App metadata and MIME/parent fixtures |
| **S2-09** | Upload PDF/ZIP/image/video/.md/DOCX/XLSX original bytes (no conversion) with fixed parent and sanitized filename | Multipart fake provider validates payload metadata/hash and bytes |
| **S2-10** | 50MB edge succeeds using a provider-supported upload transport (resumable wire protocol for >5MB as recommended); >50MB rejected **before** provider mutation; 55MB total request bound; memory/temp usage bounded | Real multipart size tests plus bounded stream/spool assertions |
| **S2-11** | Required timestamped-UUID `Idempotency-Key`, durable unique `(subject,rootBinding,key)` fingerprint+provider-ID ledger and at-most-one dispatch; same-key parallel and lost Google/PPV response/restart return original verified ID or UNKNOWN, never new ID; changed payload/name/MIME→409; after terminal prune aged key→expired without provider mutation; active ledger capacity bounded | Delayed Google fake and duplicate-bypass negative control |
| **S2-12** | Download binary by valid owned file ID uses stream/attachment; exact bytes, safe filename and cache/nosniff headers, no token/data in JSON | Real HTTP binary download test; Google fake media stream |
| **S2-13** | Google native Docs/Sheets/Slides, shortcut, folder, externally inserted/cross-root copied or moved-out binary, ID mismatched to marker rejected on list/download/reconcile; no files.export/abuse bypass | Negative Google MIME/parent and 403 fileNotDownloadable tests |
| **S2-14** | Oversize/short/chunked/trickled media, mid-stream provider/client failure: bounded cleanup/truncation behavior, no buffer OOM; metadata != content length rejected safely | Headers-only, slow body, partial body and cleanup barrier tests |
| **S2-15** | U file ID under V slot (including U-created file manually placed in V folder), forged/missing root-bound `appProperties`, forged parent and stale list cursor never expose content even if fake returns plausible metadata | Cross-subject file metadata tests with both tokens |
| **S2-16** | Disconnect/reconnect/epoch CAS and S1 grant-revoke fence while folder create/upload/download in flight cause no new unauthorized dispatch or false success | PG + fake Google barrier controlled schedule |
| **S2-17** | Invalid/expired Google credential, `401/403/404/429/5xx`, quota, timeouts and provider redirects classified safely; no secret/body leakage or uncontrolled retries | Error mapping/limits/redacted-logging tests |
| **S2-18** | One additive V26 migration for **two Drive-owned metadata-only relations** (`drive_folder_roots`, `drive_upload_intents`) and required uniqueness/CAS/status/retention; schema/DBML/ADR agree, V25 preserved, module graph unchanged, both tables excluded from Portability export | Flyway/PostgreSQL, Modulith and Portability tests |
| **S2-19** | No Media S3, Account, Feed, RAG, external OAuth scope, OAuth callback or S3 frontend/Picker behavior modified | Adjacent source tests and frozen delta inventory |
| **S2-20** | Ops guide states key/cloud/localhost/one-JVM actual proof still NOT_VERIFIED, Drive/files OFF, test limits/partial responses/recovery, no live activation | Docs Sync Check + review of current source/config, not mock-only proof of deployment |

## Proportional test and review architecture

**Suggested 5 focused groups** (not extra product demands):

1. **Folder + real PostgreSQL:** V26 additive root plus upload-intent relations, root-first ensure transition, unique key/fingerprint/provider ID, CAS, restart retention and uncertain outcome.
2. **Drive file adapter:** fake Drive server list/upload/download, mandatory appProperties field inventory, U-file-under-V-root negative, direct-only 50MB, stream/time/size bounds; replay after accepted/lost response with **actual injected duplicate-bypass failure**, key mismatch and stale-key rejection.
3. **Auth/authority:** Spring security/JWT, two slots/subjects, credential-epoch/disconnect/revocation under paused operations, OFF/ON route inventories.
4. **Architecture/cross-feature:** Spring Modulith, Portability, schema manifest, selected Media/Feed/Account regressions and accepted S1 gates if source changed.
5. **Docs/config/runbook:** ADR/schema/API/inventory, exact flags, safe Google Cloud/local setup, failure categories and operational activation gate.

**Independent reviewer must:** inspect actual changed source/test diff and real fakes; state exact tests **fresh** vs prior **reused**; retain negative/failing controls and build receipts; block if any mandatory case is not verified; record LOW/INFO without silently waiving them. Use bounded command profiles with source-qualified selection. Codex pre-handoff review now validates plan/reuse/feasibility, **not** runtime behavior. Runtime test passes cannot be asserted until implementation.

**Next step:** Owner → Codex `$codex-pre-handoff-review` | PH-01..03 corrected S2-01..20 future proof matrix, D08–D10 | independent review required for closure; all S2 tests still NOT_RUN, no handoff or implementation authority | S2 remediation 0/3 unchanged.

# GDI26-S2 — dated Owner decision record (2026-11-06)

**Authority:** the Owner's explicit choices in the current PPV conversation, following S1 publication. This grants **preparation/design review** for S2 only; it does not independently approve a handoff, backend edit, OAuth activation, Google Cloud setup or deployment. [D00–D07](../2026-11-05-google-drive-integration-preparation/owner-decision-record-2026-11-05.md) remain binding as originally chosen; do not rewrite their dated record.

| ID | Owner selection | Binding interpretation for S2 |
| --- | --- | --- |
| **D08** | **A** | One app-created `PPV` folder per Google account, only **directly contained** regular files in S2. Subfolders and their traversal are deferred. |
| **D09** | **A** | Generic original binary files including PDF, ZIP, JPG/PNG, MP4, `.md`, DOCX, XLSX. **No Google-native editor document export/conversion**, no format-dependent parsing. |
| **D10** | **A** | Upload ceiling **50 MB/file**, compatible with current Spring multipart configuration; user-facing resumable upload/restart continuation not required in S2. Google recommends its resumable **transport protocol** for files greater than 5 MB, so S2 can use that transport internally without promising persisted resume. |

**Inherited constraints:** D00 Both (backend Drive store and later S3 Explorer), D01 least privilege `openid email drive.file` and backend root authorization, D02 S1/S2/S3 separated, D03 backend-first, D04 accepted OAuth/security/two-slot model and amendments, D05 no delete/rename/move/share/copy/background sync/RAG, D06 PPV itself creates the root, D07 localhost-first; Media S3 preserved. Two distinct Google subjects and no full Drive scanning.

**Derived engineering choices for Codex review — not extra Owner selections:** new Drive-owned durable per-subject root binding may survive local OAuth disconnect **without retaining tokens or exposing a file**; necessary to avoid silently creating duplicate `PPV` folders upon reconnect. Root ID, status/generation and minimal timestamps are local metadata, covered by Portability credential/sensitive integration exclusion analysis. Require a safe explicit recovery path if an external outcome is unknown. A dedicated optional `ppv.drive.files.enabled=false` default ensures an accepted S1 connection cannot silently enable file operations. Provider adapter, route shape, pagination/size limits and schema details in this preparation are **reviewable proposals**, not already-accepted implementation contracts.

**No further material Owner choice identified for the selected first S2 slice.** If Codex demonstrates that the root-retention privacy semantics, provider behavior, or extra endpoint are actually product decisions rather than proportional technical implementation, return a precise decision request before handoff; do not silently expand scope.

**Next step:** Codex `$codex-pre-handoff-review` | this record and [scope](scope-and-source-reuse.md) | Owner choices D08–D10 complete for preparation, independent readiness pending | S2 0/3; no takeover.

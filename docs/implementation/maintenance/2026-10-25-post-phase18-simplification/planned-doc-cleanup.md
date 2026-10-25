# Non-destructive documentation cleanup disposition — 2026-10-25

**Current outcome:** Do **not** delete or move hash-referenced Phase 17/18 evidence as part of this docs-only patch. The Owner wants less clutter, but accepted source-bound evidence and reproducible history take priority. A file remaining in an archive is **not** an active user interaction or human adjudication gate.

| Candidate | Verified reference status | Maintenance A action |
| --- | --- | --- |
| Root `PATCH_TOOLS/`, `PATCH-MANIFEST.json`, `README_PATCH.txt` | Not present in the latest attached ZIP | No deletion needed here; if they exist outside that snapshot, Owner can remove old external patch helpers after local check |
| `docs/D20-PATCH-MANIFEST.json` | Mentioned in **six+ historical review JSON** receipts/decision scopes containing exact path/hash | **Retain unchanged** under docs root, labeled legacy; moving would break archived provenance without updating fixed evidence |
| `docs/implementation/phase-18/d17-human-adjudication-2026-10-20/` | ~193 dated files incl. machine corpus sources/results; UI/forms such as `index.html`, `seven-adjudications.html`, `human-judgments-template.json`, `profile-checklist.csv`, `decisions.csv` referenced by package-manifest and review/evidence JSON | **Retain immutable historical bundle**, explicitly deprecated for new operations. Do not redo human judging, show these UI files as current workflow, or run adjudication. No path/hash edits to old receipts |
| Phase 17/18 `reviews/`, evidence and Owner decision records | Required to prove prior acceptance, including actual failures/timeouts | Keep in their owning phase; future reviews use current templates and compact current status links |
| Old multiple CURRENT banners in root docs | Many historic status notices but `ACTIVE.md` is canonical | Add top-level effective governance pointers to key root docs; old dated banners explicitly historical, not competing authority |
| `__pycache__/` in attached ZIP | Generated Python bytecode is ignored in `.gitignore` | Not shipped in the changed-file package; do not delete Owner's working data or `logs/` |

Future **separate evidence-preserving housekeeping**, if desired, could extract historical UI to an immutable dated archive with new path-hash inventory, but must not falsify originals or rewrite previously accepted evidence. There is no requirement for another round of maintenance merely to tidy these files.

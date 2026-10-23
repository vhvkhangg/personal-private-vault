#!/usr/bin/env python3
"""Apply the verified D25 documentation patch to a local PPV repository; no git publishing."""
from __future__ import annotations

import hashlib
import json
import os
from pathlib import Path
import subprocess
import sys

HEAD = "e2e00534fb35534a298e8fb15d174f18cb63b5a0"
EXPECT = {
    "docs/implementation/handoffs/ACTIVE.md": "b5ad58b2ce870635a3ea894c19821e6bfae4c7f5",
    "docs/implementation/phase-18/owner-decisions.md": "6bdd5d00704e42e26e03300cc2ddbd5c24b98ef0",
    "docs/implementation/phase-18/README.md": "d9c90286071eb47a3e2fc27b99ef35f5a846db51",
    "docs/implementation/phase-18/acceptance-and-evidence.md": "69e5887fc23a2a9e21da22e73345bf1cb4b759f9",
    "docs/implementation/phase-18/final-verification-gate.md": "",  # Checked via Git status; historical source preserved as a prefix-only update.
    "docs/implementation/handoffs/README.md": "f51ebe773948c9b51f8d8f2b3555b0dc0f9ed756",
    "README.md": "b7a2f4a0c8e038d4166f3ddea7f673d3aa073a9a",
    "AGENTS.md": "cdb3e6daa2a82c1b704ded7a5dbbcdd1b0f444ff",
}
NEW = [
    "docs/implementation/phase-18/slice-3-closeout-2026-10-23.md",
    "docs/implementation/phase-18/slice-4-lifecycle-final-handoff-2026-10-23.md",
    "docs/implementation/phase-18/slice-4-lifecycle-final-acceptance-2026-10-23.md",
]
ARCHIVE = "docs/implementation/phase-18/slice-3-active-archive-2026-10-23.md"
OWNER_FRAGMENT = """## 2026-10-23 — D25 Slice 3 Owner-published closeout and Slice 4 authorization

**OWNER APPROVED, prospective from explicit 2026-10-23 request.** The Owner confirmed Slice 3 commit/push. The new published `main` HEAD was independently read from GitHub as `e2e00534fb35534a298e8fb15d174f18cb63b5a0`, commit subject `feat(rag): verify Markdown snapshots before atomic index publication`; GitHub commit enumerates **90 changed paths**. The authoritative [2026-10-23 independent final review](reviews/2026-10-23-slice-3-rr-independent-final-re-review.md) returned `READY_FOR_OWNER_COMMIT`. The Owner now **closes Slice 3 D+F** within that independently verified Markdown-only scope. See the [published closeout](slice-3-closeout-2026-10-23.md).

1. **Accepted:** S3-RR-01–03 and S3-F01–F06 CLOSED; FR18-3 VERIFIED/CLOSED for actual immutable Obsidian `.md` producer → Java verification → PostgreSQL publication. All S3-01–16 VERIFIED with exact fault/measurement limitations. Fresh independent 144 Java and 198 Python tests PASS; retained final-source 249 Java tests hash-audited. This does not reopen or reinterpret historical failures.
2. **Authorized next:** Begin bounded Phase 18 **Slice 4 Lifecycle & Final (E+H)** under the [preparation handoff](slice-4-lifecycle-final-handoff-2026-10-23.md) and [prospective matrix](slice-4-lifecycle-final-acceptance-2026-10-23.md). Codex must establish a validated executable handoff against the published source before any new production implementation; no extra remediation round is automatically granted.
3. **Only `.md` external:** D24 overall Markdown-only remains controlling; do not add `.markdown`/TXT/PDF/DOCX/XLS/XLSX/OCR support. Complete NO_CHANGE, same-R reindex, lifecycle/revoke/restore, reporting/recovery and cleanup, then genuine P18-FINAL verification.
4. **One consolidated removal:** Near the END of Slice 4 and before P18-FINAL, remove unwanted unaccepted format source/adapters/dependencies/tests/fixtures once, including interwoven shared code only if `.md`/DOMAIN and frozen safety remain correct. Do not retain future-only format features as a reserve. Preserve immutable historical reviews/failed receipts, retained D22 incident facts and applied Flyway migration history.
5. **Still OPEN:** Phase 18 overall/P18-FINAL NOT ACCEPTED; external indexing INACTIVE; original-live ingestion DISABLED/DEFERRED. The historical Java aggregate 1,433/1 failure/2 errors remains FAILED until a genuine passing full-clean gate and investigation. D22's three originals remain NOT_RECOVERED. Phase 17 FROZEN; Slice 1/D14 CLOSED; Slice 2/D23 Markdown producer-local CLOSED; Antigravity historical 2/2 exhausted. No agent commit/push/tag/PR; no indexing activation without later explicit Owner approval.

This D25 statement controls current/future status where earlier dated D24/Slice 3 notes say NOT YET CLOSED, FR18-3 OPEN or Slice 4 unauthorized. Their bodies remain historical evidence; this prospective decision does not rewrite the earlier status at the time.

## Preserved earlier Owner decisions

"""
ACTIVE_NEW = """# Active Implementation Handoff

- Handoff ID: `P18-S4-LIFECYCLE-FINAL`
- Owner authorization: `D25`, 2026-10-23
- Status: `PENDING_CODEX_HANDOFF`
- Baseline: published main `e2e00534fb35534a298e8fb15d174f18cb63b5a0`
- Scope: Lifecycle & Final (E+H), `.md`-only external documents under D24
- Current implementer: not assigned; executable handoff requires Codex verification

## Controlling status

Phase 18 **Slice 3 D+F is COMPLETE/CLOSED/OWNER-PUBLISHED under D25**; S3-F01–F06, RR-01–03 and **FR18-3 VERIFIED/CLOSED** within the independently accepted producer/Java/PostgreSQL boundary. [Slice 3 closeout](../phase-18/slice-3-closeout-2026-10-23.md) records the published commit and exact qualification. [Independent final review](../phase-18/reviews/2026-10-23-slice-3-rr-independent-final-re-review.md) remains unchanged. Original Slice 3 ACTIVE was archived as [the exact prior register](../phase-18/slice-3-active-archive-2026-10-23.md) with its raw original bytes preserved.

By explicit Owner approval, Slice 4 is the next workstream. Read [D25](../phase-18/owner-decisions.md#2026-10-23--d25-slice-3-owner-published-closeout-and-slice-4-authorization), [Slice 4 handoff preparation](../phase-18/slice-4-lifecycle-final-handoff-2026-10-23.md) and [prospective acceptance matrix](../phase-18/slice-4-lifecycle-final-acceptance-2026-10-23.md). Codex creates/validates the executable handoff before implementation. This documentation does not self-certify code changes, issue a new remediation authorization or start a background task.

Preserve Phase 17 COMPLETE/FROZEN, Slice 1/D14 and Slice 2/D23 CLOSED, D24 overall Obsidian `.md` only, D22 originals NOT_RECOVERED, external indexing INACTIVE and live originals DISABLED/DEFERRED. Phase 18 overall / P18-FINAL are NOT ACCEPTED. Historical aggregate Java verification **FAILED (1,433 / 1 failure / 2 errors / 0 skips)**; full PostgreSQL connection-resource investigation and repeat full clean gate are mandatory. No automatic activation, commit/push/tag/PR, or format expansion. Cleanup of unwanted unaccepted format code/deps/tests/fixtures is authorized once at the end of Slice 4 before P18-FINAL; historical applied Flyway migrations and recorded failures must not be falsified.
"""
PREFIXES = {
    "docs/implementation/phase-18/README.md": "**D25 CURRENT — 2026-10-23:** Slice 3 D+F / FR18-3 **CLOSED and owner-published** at main `e2e00534fb35534a298e8fb15d174f18cb63b5a0` (90 changed paths), after the independent final review. Owner authorizes Slice 4 Lifecycle & Final preparation; handoff is `PENDING_CODEX_HANDOFF`. Phase 18 overall/P18-FINAL NOT ACCEPTED, external indexing INACTIVE. See [D25](owner-decisions.md#2026-10-23--d25-slice-3-owner-published-closeout-and-slice-4-authorization), [closeout](slice-3-closeout-2026-10-23.md) and [S4 handoff preparation](slice-4-lifecycle-final-handoff-2026-10-23.md). D24 `.md` only; one consolidated excluded-format cleanup before final gate; D22 originals NOT_RECOVERED; historical Java aggregate FAILED. Previous status lines below are dated historical records, not current authorization.\n\n",
    "docs/implementation/phase-18/acceptance-and-evidence.md": "**D25 CURRENT — 2026-10-23:** Slice 3 D+F accepted and published; FR18-3 VERIFIED/CLOSED for the actual `.md` producer-to-Java/PG boundary. Slice 4 E+H is next and its [S4 matrix](slice-4-lifecycle-final-acceptance-2026-10-23.md) is `NOT_RUN`, not accepted. Full P18-FINAL, aggregate Java gate, indexing activation and overall Phase 18 closure remain OPEN. See [D25](owner-decisions.md#2026-10-23--d25-slice-3-owner-published-closeout-and-slice-4-authorization). Historical rows below retain their prior dates and verdicts.\n\n",
    "docs/implementation/phase-18/final-verification-gate.md": "**D25 APPLICABILITY NOTICE — 2026-10-23:** Overall Phase 18 now targets external Obsidian `.md` only under D24; Slice 3/FR18-3 is accepted/published under D25, but this **P18-FINAL** gate is still `NOT RUN / NOT ACCEPTED`. The **five existing executable commands remain mandatory** after Slice 4's consolidated cleanup, along with aggregate PostgreSQL resource investigation and applicable `.md`/DOMAIN quality, lifecycle, offline and resource proofs. Earlier all-format obligations excluded prospectively under D24 become N/A, not historical PASS; historic failed Java/full captures and D22 NOT_RECOVERED evidence remain unchanged. See [D25](owner-decisions.md#2026-10-23--d25-slice-3-owner-published-closeout-and-slice-4-authorization).\n\n",
    "docs/implementation/handoffs/README.md": "**D25 CURRENT — 2026-10-23:** `ACTIVE.md` now identifies `P18-S4-LIFECYCLE-FINAL / PENDING_CODEX_HANDOFF` following the Owner-published [Slice 3 closeout](../phase-18/slice-3-closeout-2026-10-23.md) and prospective [S4 handoff](../phase-18/slice-4-lifecycle-final-handoff-2026-10-23.md). FR18-3 CLOSED within Slice 3; Phase 18 overall/P18-FINAL NOT ACCEPTED, indexing INACTIVE and full Java aggregate historical FAILED. Prior current-status paragraphs below are historical.\n\n",
    "README.md": "**D25 CURRENT — 2026-10-23:** Phase 18 Slice 3 Verified Indexing is **COMPLETE/CLOSED/OWNER-PUBLISHED** at [main `e2e00534`](https://github.com/vhvkhangg/personal-private-vault/commit/e2e00534fb35534a298e8fb15d174f18cb63b5a0); FR18-3 VERIFIED/CLOSED for `.md` → trusted Java/PG publication. Slice 4 Lifecycle & Final now authorized for Codex handoff preparation (`PENDING_CODEX_HANDOFF`), including one final removal of excluded-format code/deps/tests/fixtures. **No external indexing activation**; P18-FINAL/overall Phase 18 NOT ACCEPTED; old full Java gate FAILED. [D25](docs/implementation/phase-18/owner-decisions.md#2026-10-23--d25-slice-3-owner-published-closeout-and-slice-4-authorization). Earlier status lines below remain historical.\n\n",
    "AGENTS.md": "**D25 CONTROLLING STATUS — 2026-10-23:** Owner-published `main` `e2e00534fb35534a298e8fb15d174f18cb63b5a0` closes **Slice 3 D+F and FR18-3** within its independently verified Obsidian `.md` scope. Owner authorizes starting Slice 4 Lifecycle & Final with `P18-S4-LIFECYCLE-FINAL / PENDING_CODEX_HANDOFF`, not production implementation without verified executable handoff. At Slice 4 end, remove unwanted unaccepted TXT/PDF/Office/OCR feature code/deps/tests/fixtures once before P18-FINAL. Indexing INACTIVE, Phase 18 overall/P18-FINAL NOT ACCEPTED, Java historical full gate FAILED, D22 originals NOT_RECOVERED. Preserve frozen Phase 17/Slices 1/2, audit/failure/migration history and no agent commit/push. [Current ACTIVE](docs/implementation/handoffs/ACTIVE.md) / [D25](docs/implementation/phase-18/owner-decisions.md#2026-10-23--d25-slice-3-owner-published-closeout-and-slice-4-authorization). Prior dated statuses below are historical.\n\n",
}

def run(*args: str) -> str:
    p = subprocess.run(args, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True)
    if p.returncode:
        raise RuntimeError(f"{' '.join(args)}: {p.stderr.strip()}")
    return p.stdout.strip()


def main():
    root = Path.cwd().resolve()
    package = Path(__file__).resolve().parent.parent
    if Path(run('git', 'rev-parse', '--show-toplevel')).resolve() != root:
        raise RuntimeError('Run at the Git repository root, not in a subdirectory')
    if run('git', 'rev-parse', 'HEAD') != HEAD or run('git', 'branch', '--show-current') != 'main':
        raise RuntimeError(f'Requires clean confirmed main baseline {HEAD}; stop and reconcile')
    target_list = list(EXPECT)
    changes = run('git', 'status', '--porcelain', '--', *target_list)
    if changes:
        raise RuntimeError('One or more target files are already modified; stop and reconcile:\n' + changes)
    old = {}
    for rel, oid in EXPECT.items():
        path = root/rel
        if not path.is_file():
            raise RuntimeError('Required file absent: '+rel)
        if oid and run('git', 'hash-object', '--', rel) != oid:
            raise RuntimeError('Git blob hash mismatch for '+rel+'; stop without overwriting')
        old[rel] = path.read_bytes()
    for rel in NEW:
        src = package/rel
        dest = root/rel
        if dest.exists() or not src.is_file():
            raise RuntimeError('New docs already exist or package missing: '+rel)
    if (root/ARCHIVE).exists():
        raise RuntimeError('Prior ACTIVE archive already exists; stop')
    for rel in NEW:
        (root/rel).parent.mkdir(parents=True, exist_ok=True)
        (root/rel).write_bytes((package/rel).read_bytes())
    (root/ARCHIVE).write_bytes(old['docs/implementation/handoffs/ACTIVE.md'])
    (root/'docs/implementation/handoffs/ACTIVE.md').write_bytes(ACTIVE_NEW.encode('utf-8'))
    for rel in (p for p in target_list if p != 'docs/implementation/handoffs/ACTIVE.md'):
        raw = old[rel]
        eol = '\r\n' if raw.count(b'\r\n') and raw.count(b'\r\n') == raw.count(b'\n') else '\n'
        fragment = OWNER_FRAGMENT if rel.endswith('owner-decisions.md') else PREFIXES[rel]
        (root/rel).write_bytes(fragment.replace('\n',eol).encode('utf-8') + raw)
    print('D25 documentation patch applied; no staging/commit/push was performed.')
    print('Dated prior ACTIVE archived byte-for-byte; historical existing document bodies preserved as append-only suffixes.')
    print(run('git', '-c', 'core.whitespace=cr-at-eol', 'diff', '--check') or 'git diff --check: PASS')
    print(run('git','status','--short'))

if __name__ == '__main__':
    try: main()
    except Exception as e:
        print('STOP — '+str(e), file=sys.stderr)
        sys.exit(1)

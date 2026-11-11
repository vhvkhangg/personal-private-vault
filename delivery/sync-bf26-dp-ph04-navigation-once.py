#!/usr/bin/env python3
"""One-off owner-worktree, docs-only PH-04 review-status synchronization.
Dry run by default: python delivery/sync-bf26-dp-ph04-navigation-once.py --repo <PPV-repo>
Write only 5 current notice files: append --apply. Does NOT touch source, stage, commit or push.
Requires PREVIOUS five-file BF26-DP navigation sync; does not apply the old published-main sync.
"""
from __future__ import annotations
import argparse
import difflib
from pathlib import Path
import subprocess

HEAD = '9c7e123d01237e057675d348ddb93700a4850e51'
REV = 'reviews/2026-11-11-bf26-dp-independent-pre-handoff-re-review-1.md'
TARGETS = [
    'AGENTS.md',
    'docs/workflow/current-owner-delivery-policy.md',
    'docs/roadmap.md',
    'docs/implementation/maintenance/README.md',
    'docs/implementation/handoffs/ACTIVE.md',
]


def edit_notice(source: str, base: str) -> str:
    parts = source.splitlines(keepends=True)
    ix = 2 if base.startswith('../implementation/') else 0
    first = parts[ix]
    assert first.startswith('> **CURRENT OWNER STATUS — 2026-11-11 / BACKEND_FIRST / BF26-DP:'), base
    needle = 'remains `CHANGES_REQUESTED` pending separate independent re-review of corrected preparation'
    assert first.count(needle) == 1, (base, 'missing/changed original checkpoint')
    newtext = (
        'remains a historical `CHANGES_REQUESTED` checkpoint; '
        f'[independent re-review 1]({base}2026-11-11-dual-login-profiles-preparation/{REV}) '
        'CLOSED PH-01–03 at DP-A preparation and kept PH-04 MEDIUM/OPEN. '
        'The PH-04 matcher/session correction is submitted for a separate independent re-review, '
        'not yet `READY_FOR_HANDOFF`'
    )
    parts[ix] = first.replace(needle, newtext)
    return ''.join(parts)


def edit_index(source: str) -> str:
    rows = source.splitlines(keepends=True)
    target = [i for i, v in enumerate(rows) if v.startswith('- [BF26-DP — Dual Public/Private Login Profiles]')]
    assert len(target) == 1, target
    idx = target[0]
    line = rows[idx]
    old = 'D1–D6 OWNER_RATIFIED_FOR_PREPARATION / CHANGES_REQUESTED / PH-01 HIGH + PH-02–04 MEDIUM OPEN / NO_ACTIVE_HANDOFF'
    assert line.count(old) == 1, 'BF26-DP old status missing'
    line = line.replace(old, 'D1–D6 OWNER_RATIFIED / CHANGES_REQUESTED / PH-01–03 CLOSED_AT_PREPARATION / PH-04 MEDIUM OPEN / NO_ACTIVE_HANDOFF')
    old2 = 'remains blocking pending separate corrected-preparation re-review'
    assert line.count(old2) == 1, 'BF26-DP index review text changed'
    line = line.replace(old2,
        'is historical; [independent re-review 1](2026-11-11-dual-login-profiles-preparation/' + REV + ') '
        'closed PH-01–03 at preparation and left PH-04 open pending exact Auth matcher/session docs re-review')
    rows[idx] = line
    return ''.join(rows)


def edit_active(source: str) -> str:
    lines = source.splitlines(keepends=True)
    assert len(lines) > 13 and lines[2].startswith('## Current execution and priority — 2026-11-11 / BACKEND_FIRST')
    ia = [i for i, x in enumerate(lines[:14]) if x.startswith('- **Authority:**')]
    ib = [i for i, x in enumerate(lines[:14]) if x.startswith('**Next step:**')]
    assert len(ia) == len(ib) == 1
    old = '`CHANGES_REQUESTED / PH-01 HIGH + PH-02–04 MEDIUM OPEN`, pending independent re-review'
    assert lines[ia[0]].count(old) == 1, 'ACTIVE authority does not match previous sync'
    lines[ia[0]] = lines[ia[0]].replace(old,
        '`CHANGES_REQUESTED` historical checkpoint; [DP re-review 1](../maintenance/2026-11-11-dual-login-profiles-preparation/' + REV + ') '
        'CLOSED PH-01–03 for DP-A preparation, PH-04 MEDIUM OPEN pending an independent re-review of corrected matcher/session tables')
    assert 'PH-01–04' in lines[ib[0]], 'ACTIVE next-step checkpoint drift'
    lines[ib[0]] = (
        '**Next step:** separate independent Codex session | `$codex-pre-handoff-review` | '
        'BF26-DP PH-04 matcher/session compatibility, expiry/reset/revoke, A acceptance and G-AUTH/G-SWITCH; '
        'PH-01–03 previously CLOSED at preparation | only if independently ready may Owner publish preparation; '
        'then separately approve frozen-source DP-A and invoke `$codex-create-handoff` | '
        'ACTIVE stays `NO_ACTIVE_HANDOFF`; no implementation/remediation/takeover round consumed.\n'
    )
    return ''.join(lines)


def main() -> None:
    ap = argparse.ArgumentParser()
    ap.add_argument('--repo', required=True, type=Path)
    ap.add_argument('--apply', action='store_true')
    args = ap.parse_args()
    repo = args.repo.resolve()
    def git(*argv):
        return subprocess.check_output(['git', '-C', str(repo), *argv], text=True).strip()
    assert git('branch', '--show-current') == 'main', 'Not on main; no edits'
    assert git('rev-parse', 'HEAD') == HEAD, 'HEAD diverged; do not apply stale navigation patch'
    updated = []
    for rel in TARGETS:
        path = repo / rel
        original = path.read_text(encoding='utf-8')
        if rel == 'AGENTS.md':
            new = edit_notice(original, 'docs/implementation/maintenance/')
        elif rel == 'docs/workflow/current-owner-delivery-policy.md':
            new = edit_notice(original, '../implementation/maintenance/')
        elif rel == 'docs/roadmap.md':
            new = edit_notice(original, 'implementation/maintenance/')
        elif rel == 'docs/implementation/maintenance/README.md':
            new = edit_index(original)
        else:
            new = edit_active(original)
        assert new != original, 'Expected exactly one targeted revision: '+rel
        print(''.join(difflib.unified_diff(original.splitlines(keepends=True),new.splitlines(keepends=True),
                fromfile='a/'+rel,tofile='b/'+rel))[:4200])
        updated.append((path,new))
    if args.apply:
        for path,new in updated: path.write_text(new,encoding='utf-8',newline='\n')
    print('BF26-DP PH-04 navigation', 'APPLIED' if args.apply else 'DRY-RUN', len(updated), 'files; no Git publishing')

if __name__ == '__main__': main()

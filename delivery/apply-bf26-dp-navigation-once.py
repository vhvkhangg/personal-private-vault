#!/usr/bin/env python3
"""One-off, docs-only BF26-DP current-navigation sync. Not a repository tool or runtime harness.
Usage: python /path/to/apply-bf26-dp-navigation-once.py --repo <local-ppv-checkout> [--apply]
Default is dry-run. Modifies exactly five Markdown files; never commits/stages.
"""
import argparse, subprocess, sys, pathlib, difflib
SHA='9c7e123d01237e057675d348ddb93700a4850e51'
ROOT='docs/implementation/maintenance/2026-11-11-dual-login-profiles-preparation/'


def current_notice(kind, active):
    base={"root":"docs/implementation/maintenance/","policy":"../implementation/maintenance/","roadmap":"implementation/maintenance/"}[kind]
    dp=f"{base}2026-11-11-dual-login-profiles-preparation/"
    s5=f"{base}2026-11-11-dashboard-api-readiness-preparation/"
    return (
        '> **CURRENT OWNER STATUS — 2026-11-11 / BACKEND_FIRST / BF26-DP:** '
        f'[BF26-S5 Option A]({s5}README.md) is **OWNER_PUBLISHED / DOCS_ONLY_API_READINESS** '
        f'at [`{SHA[:8]}`](https://github.com/vhvkhangg/personal-private-vault/commit/{SHA}); '
        f'[BF26-DP D1–D6 Owner ratification]({dp}owner-ratification-d1-d6-2026-11-11.md) '
        'approves a **prospective** fixed Public/Private policy: safe DP-A Public auth-only default DENY, '
        'Owner-bound Private password elevation and lineage downgrade, Google enabled-but-unmapped fail-closed, '
        'Portability co-delivery, retained PIN. '
        f'[Initial independent PH-01–04 review]({dp}reviews/2026-11-11-bf26-dp-independent-pre-handoff-review.md) '
        'remains `CHANGES_REQUESTED` pending separate independent re-review of corrected preparation; '
        f'[ACTIVE]({active}) = `NO_ACTIVE_HANDOFF`. '
        'No source, migration, runtime tests, feature/provider activation, Frontend/BFF resumption, implementation handoff or Git publication is authorized. '
        'H1-F remains `OWNER_PUBLISHED / CHANGES_REQUESTED / NOT_INDEPENDENTLY_ACCEPTED / FRONTEND_PAUSED_BY_OWNER`; '
        'FR-13 `MEDIUM / OPEN / UPSTREAM_DEPENDENCY_BLOCKED`. Preserve source, tests, pins, v0.3, RAG suspension, '
        'feature flags and counters. Recycle Bin then Final Non-RAG Backend Audit remain later separately gated scopes. '
        'All older dated status notices below are historical checkpoints.\n'
    )


def replace_top_line(txt,path,newline):
    arr=txt.splitlines(keepends=True)
    index=2 if path.endswith('current-owner-delivery-policy.md') else 0
    assert len(arr)>index and arr[index].startswith('> **CURRENT OWNER STATUS — 2026-11-11 / BACKEND_FIRST:') and 'PENDING_INDEPENDENT_RE_REVIEW' in arr[index],(path,'not current published S5 line')
    arr[index]=newline
    return ''.join(arr)

def change_active(t):
    arr=t.splitlines(keepends=True)
    assert arr[2].startswith('## Current execution and priority — 2026-11-11 / BACKEND_FIRST')
    def only(prefix,new):
        ix=[i for i,line in enumerate(arr[:14]) if line.startswith(prefix)]
        assert len(ix)==1,(prefix,ix)
        arr[ix[0]]=new+'\n'
    only('- **Authority:**', '- **Authority:** [BF26-S5 Option A](../maintenance/2026-11-11-dashboard-api-readiness-preparation/README.md) **OWNER_PUBLISHED / DOCS_ONLY_API_READINESS** at `9c7e123d`; [BF26-DP Owner ratification](../maintenance/2026-11-11-dual-login-profiles-preparation/owner-ratification-d1-d6-2026-11-11.md) prospectively approves D1–D6 for corrected preparation. [Initial independent DP review](../maintenance/2026-11-11-dual-login-profiles-preparation/reviews/2026-11-11-bf26-dp-independent-pre-handoff-review.md) remains `CHANGES_REQUESTED / PH-01 HIGH + PH-02–04 MEDIUM OPEN`, pending independent re-review. **Executable authority: `NO_ACTIVE_HANDOFF`**; no source/migration/implementation approval, no test/runtime/provider activation.')
    only('- **Published baseline verified for S5 correction:**', '- **Verified published reference:** remote `main` inspected at `9c7e123d01237e057675d348ddb93700a4850e51`, publishing accepted BF26-S5 docs-only readiness. BF26-DP preparation and its ratified decision correction are **not** on this `main` as of preparation; do not reinterpret the earlier `bbdcd3e` inspection as a current publication fact. H1-F implementation remains Owner-published at `79d2e8d` under exception; not independently accepted.')
    only('- **Current Backend planning priority:**', '- **Current Backend planning priority:** [BF26-DP ratified preparation](../maintenance/2026-11-11-dual-login-profiles-preparation/README.md): Public auth-only DENY in DP-A; independently scoped B1/B2 grants, same-slice Portability migration safety and Google fail-closed. [BF26-S5 Option A](../maintenance/2026-11-11-dashboard-api-readiness-preparation/README.md) completed docs-only for former singleton Owner, not public-profile-certified. [Recycle Bin](../maintenance/2026-11-11-backend-first-assessment/owner-deferred-work-access-profiles-and-recycle-bin-2026-11-11.md) and Final Non-RAG Backend Audit follow after separately accepted work. No branch/worktree created; later executable scope needs independent review, Owner publication and specific frozen-boundary authorization.')
    only('**Next step:**', '**Next step:** separate independent Codex session | `$codex-pre-handoff-review` | ratified BF26-DP preparation and initial review PH-01–04; verify five-file docs sync, actual baseline and no source changes | only independently ready preparation may be Owner-published; separately authorize bounded DP-A frozen/source delta and `$codex-create-handoff` later | no implementation/remediation/takeover round consumed.')
    return ''.join(arr)

def change_index(t):
    lines=t.splitlines(keepends=True)
    idx=[i for i,z in enumerate(lines) if z.startswith('- [BF26-S5 Option A — Minimal Dashboard API Readiness]')]
    assert len(idx)==1,idx
    x=idx[0]
    lines[x]='- [BF26-DP — Dual Public/Private Login Profiles](2026-11-11-dual-login-profiles-preparation/README.md) — **D1–D6 OWNER_RATIFIED_FOR_PREPARATION / CHANGES_REQUESTED / PH-01 HIGH + PH-02–04 MEDIUM OPEN / NO_ACTIVE_HANDOFF**. [Owner ratification](2026-11-11-dual-login-profiles-preparation/owner-ratification-d1-d6-2026-11-11.md) selects safe Public auth-only DP-A denial, prospective B1/B2 grants, private-password elevation, per-lineage downgrade, Google unmapped fail-closed, Portability co-delivery and retained PIN. [Independent review](2026-11-11-dual-login-profiles-preparation/reviews/2026-11-11-bf26-dp-independent-pre-handoff-review.md) remains blocking pending separate corrected-preparation re-review. No code/migration/handoff/flags/stage/push by agent; frontend paused, FR-13 open.\n\n'
    lines.insert(x+1, '- [BF26-S5 Option A — Minimal Dashboard API Readiness](2026-11-11-dashboard-api-readiness-preparation/README.md) — **OWNER_PUBLISHED / DOCS_ONLY_API_READINESS**, independent re-review CLOSED PH-01–03; published at [`9c7e123d`](https://github.com/vhvkhangg/personal-private-vault/commit/9c7e123d01237e057675d348ddb93700a4850e51). Inbox/Radar/Vocabulary previews and Global Search reuse existing APIs; no runtime/Frontend acceptance or Backend source gap established. Later Frontend-driven API changes need separate Owner approval. No active implementation handoff.\n')
    return ''.join(lines)

def run(repo: pathlib.Path, apply: bool):
    paths=['AGENTS.md','docs/workflow/current-owner-delivery-policy.md','docs/roadmap.md','docs/implementation/maintenance/README.md','docs/implementation/handoffs/ACTIVE.md']
    assert (repo/'.git').exists() or subprocess.run(['git','-C',str(repo),'rev-parse','--is-inside-work-tree'],capture_output=True).returncode==0, 'Not a Git repository'
    # Check actual current main baseline: HEAD must be the supported verified baseline.
    branch=subprocess.check_output(["git","-C",str(repo),"branch","--show-current"],text=True).strip()
    if branch!="main": raise RuntimeError(f"Current branch {branch!r} is not main; refusing auto-application.")
    h=subprocess.check_output(['git','-C',str(repo),'rev-parse','HEAD'],text=True).strip()
    if h!=SHA: raise RuntimeError(f'HEAD {h} != verified baseline {SHA}. Review new source first; refusing stale patch.')
    edits=[]
    for path in paths:
        f=repo/path
        old=f.read_text(encoding='utf-8')
        if path=='AGENTS.md': new=replace_top_line(old,path,current_notice('root', 'docs/implementation/handoffs/ACTIVE.md'))
        elif path=='docs/workflow/current-owner-delivery-policy.md':
            new=replace_top_line(old,path,current_notice('policy','../implementation/handoffs/ACTIVE.md'))
            old_scope='- This is a permanent **single-account** private web app with local/offline RAG;'
            new_scope='- This remains a permanent **single-Owner** private web app (two fixed login profiles only after separately accepted implementation) with local/offline RAG;'
            if old_scope not in new: raise RuntimeError('Missing expected current policy scope sentence')
            new=new.replace(old_scope,new_scope,1)
        elif path=='docs/roadmap.md': new=replace_top_line(old,path,current_notice('roadmap','implementation/handoffs/ACTIVE.md'))
        elif path.endswith('maintenance/README.md'):new=change_index(old)
        else:new=change_active(old)
        diff=''.join(difflib.unified_diff(old.splitlines(keepends=True),new.splitlines(keepends=True),fromfile='a/'+path,tofile='b/'+path))
        print(diff[:1600], '... [truncated display]' if len(diff)>1600 else '')
        edits.append((f,new))
    if apply:
        for f,new in edits: f.write_text(new,encoding="utf-8",newline="\n")
    print('Docs sync', 'APPLIED' if apply else 'DRY-RUN ONLY', 'for five paths; never stages/commits/pushes.')

if __name__=='__main__':
    ap=argparse.ArgumentParser();ap.add_argument('--repo',type=pathlib.Path,required=True);ap.add_argument('--apply',action='store_true')
    opts=ap.parse_args(); run(opts.repo.resolve(),opts.apply)

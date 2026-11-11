#!/usr/bin/env python3
"""BF26-DP PH-04 re-review 2: apply six bounded current-document corrections.

Dry-run: python apply-bf26-dp-ph04-r2-docsync-once.py --repo PATH
Apply:   python apply-bf26-dp-ph04-r2-docsync-once.py --repo PATH --apply

No source/test/DB/lockfile changes; no Git staging, commit or push.
This one-off helper should not be committed. It is NOT evidence of application
unless --apply succeeds and actual changed files are inspected.
"""
from __future__ import annotations
import argparse
import difflib
import subprocess
from pathlib import Path

HEAD = "9c7e123d01237e057675d348ddb93700a4850e51"
BASE = "docs/implementation/maintenance/2026-11-11-dual-login-profiles-preparation/"
REVIEW1 = "reviews/2026-11-11-bf26-dp-independent-pre-handoff-re-review-1.md"
REVIEW2 = "reviews/2026-11-11-bf26-dp-independent-pre-handoff-re-review-2.md"
MARKER = "## PH-04 bounded author correction after independent re-review 2 — 2026-11-11"
NOTICE_TARGETS = (
    "AGENTS.md",
    "docs/workflow/current-owner-delivery-policy.md",
    "docs/roadmap.md",
    "docs/implementation/maintenance/README.md",
    "docs/implementation/handoffs/ACTIVE.md",
)
README = BASE + "README.md"


def git(repo: Path, *args: str) -> str:
    r = subprocess.run(["git", "-C", str(repo), *args], capture_output=True, text=True, check=True)
    return r.stdout.strip()


def links_for(path: str) -> tuple[str, str, str]:
    if path == "AGENTS.md":
        prefix = "docs/implementation/maintenance/"
        active = "docs/implementation/handoffs/ACTIVE.md"
    elif path == "docs/workflow/current-owner-delivery-policy.md":
        prefix = "../implementation/maintenance/"
        active = "../implementation/handoffs/ACTIVE.md"
    elif path == "docs/roadmap.md":
        prefix = "implementation/maintenance/"
        active = "implementation/handoffs/ACTIVE.md"
    else:
        raise AssertionError(path)
    return prefix, active, prefix + "2026-11-11-dual-login-profiles-preparation/"


def update_current_notice(source: str, path: str) -> str:
    rows = source.splitlines(keepends=True)
    ix = 2 if path.endswith("current-owner-delivery-policy.md") else 0
    assert len(rows) > ix and rows[ix].startswith(
        "> **CURRENT OWNER STATUS — 2026-11-11 / BACKEND_FIRST / BF26-DP:"
    ), f"Expected BF26-DP current notice absent from {path}; no edits"
    assert "CHANGES_REQUESTED" in rows[ix] and "PH-01" in rows[ix], f"Unexpected current status in {path}"
    assert "re-review-2" not in rows[ix], f"Already synchronized: {path}"
    prefix, active, dp = links_for(path)
    rows[ix] = (
        "> **CURRENT OWNER STATUS — 2026-11-11 / BACKEND_FIRST / BF26-DP:** "
        f"[BF26-S5 Option A]({prefix}2026-11-11-dashboard-api-readiness-preparation/README.md) "
        f"is **OWNER_PUBLISHED / DOCS_ONLY_API_READINESS** at [`9c7e123d`](https://github.com/vhvkhangg/personal-private-vault/commit/{HEAD}). "
        f"[D1–D6 Owner ratification]({dp}owner-ratification-d1-d6-2026-11-11.md) remains valid. "
        f"[Initial DP review]({dp}reviews/2026-11-11-bf26-dp-independent-pre-handoff-review.md) "
        "is historical `CHANGES_REQUESTED`; "
        f"[re-review 1]({dp}{REVIEW1}) CLOSED PH-01–03 at DP-A preparation, "
        f"and [re-review 2]({dp}{REVIEW2}) retained **PH-04 MEDIUM / OPEN** for identifier-free "
        "fixed PUBLIC login and five notice updates. The bounded PH-04 correction is submitted for "
        "**another independent pre-handoff re-review**, not independently accepted, committed or published. "
        f"[ACTIVE]({active}) = `NO_ACTIVE_HANDOFF`. "
        "DP-A Public remains auth-only/default DENY for business/data; no source/SQL/API/flag changes or implementation "
        "handoff are authorized. Frontend H1-F is `OWNER_PUBLISHED / NOT_INDEPENDENTLY_ACCEPTED / "
        "CHANGES_REQUESTED / FRONTEND_PAUSED_BY_OWNER`; FR-13 `MEDIUM / OPEN / UPSTREAM_DEPENDENCY_BLOCKED`. "
        "Preserve historical reviews, evidence, frozen DB/ADRs, provider defaults OFF, RAG suspension, v0.3, "
        "remediation/takeover counters and future separately gated Recycle Bin/final non-RAG audit. "
        "Earlier dated notices below remain historical.\n"
    )
    return "".join(rows)


def update_maintenance_index(source: str) -> str:
    rows = source.splitlines(keepends=True)
    ix = [i for i, row in enumerate(rows) if row.startswith("- [BF26-DP — Dual Public/Private Login Profiles]")]
    assert len(ix) == 1, "Expected exactly one BF26-DP maintenance index entry"
    i = ix[0]
    assert "PH-01 HIGH" in rows[i] and "PH-02–04 MEDIUM OPEN" in rows[i], (
        "Maintenance entry no longer at pre-re-review-1 status; review manually"
    )
    rows[i] = (
        "- [BF26-DP — Dual Public/Private Login Profiles](2026-11-11-dual-login-profiles-preparation/README.md) — "
        "**D1–D6 OWNER_RATIFIED / CHANGES_REQUESTED / PH-01–03 CLOSED_AT_PREPARATION / "
        "PH-04 MEDIUM OPEN / NO_ACTIVE_HANDOFF**. "
        "[Re-review 1](2026-11-11-dual-login-profiles-preparation/" + REVIEW1 + ") closed PH-01–03 "
        "for DP-A preparation; [re-review 2](2026-11-11-dual-login-profiles-preparation/" + REVIEW2 + ") "
        "kept PH-04 OPEN for required-identifier mismatch and unsynchronized notices. The author has submitted the "
        "identifier-free Public-login/acceptance/harness correction and bounded current docs-sync for further "
        "independent review; **not READY_FOR_HANDOFF / not published**. DP-A remains Public auth-only default DENY "
        "with same-slice Portability/security-table coverage; implementation, source/frozen changes, new handoff, "
        "provider flags and Frontend remain unauthorized. H1-F paused/FR-13 open; historical counters unchanged.\n"
    )
    return "".join(rows)


def update_active(source: str) -> str:
    rows = source.splitlines(keepends=True)
    assert len(rows) > 14 and rows[2].startswith("## Current execution and priority — 2026-11-11 / BACKEND_FIRST")
    def find(prefix: str) -> int:
        matches = [i for i, x in enumerate(rows[:18]) if x.startswith(prefix)]
        assert len(matches) == 1, (prefix, matches)
        return matches[0]
    authority = find("- **Authority:**")
    next_step = find("**Next step:**")
    assert "PH-01 HIGH" in rows[authority] and "PH-02–04 MEDIUM OPEN" in rows[authority], (
        "ACTIVE authority no longer at pre-re-review-1 status; review manually"
    )
    assert "PH-01–04" in rows[next_step], "ACTIVE next-step does not match initial pre-handoff status"
    dp = "../maintenance/2026-11-11-dual-login-profiles-preparation/"
    rows[authority] = (
        "- **Authority:** [BF26-S5 Option A](../maintenance/2026-11-11-dashboard-api-readiness-preparation/README.md) "
        f"**OWNER_PUBLISHED / DOCS_ONLY_API_READINESS** at `{HEAD[:8]}`. "
        f"[BF26-DP D1–D6 Owner ratification]({dp}owner-ratification-d1-d6-2026-11-11.md) remains binding "
        "for preparation; [initial review](" + dp + "reviews/2026-11-11-bf26-dp-independent-pre-handoff-review.md) "
        "is historical `CHANGES_REQUESTED`; [re-review 1](" + dp + REVIEW1 + ") **CLOSED PH-01–03 "
        "at DP-A preparation**, [re-review 2](" + dp + REVIEW2 + ") retained **PH-04 MEDIUM / OPEN**. "
        "The identifier-free fixed-PUBLIC login and five notices are now submitted for subsequent independent review, "
        "**not** closed, independently ready, or published. **Executable authority: `NO_ACTIVE_HANDOFF`**. "
        "No source/schema/test/runtime/provider/Frontend implementation authorization.\n"
    )
    rows[next_step] = (
        "**Next step:** separate independent Codex session | `$codex-pre-handoff-review` | "
        "BF26-DP PH-04 after re-review 2: Public `/api/v1/auth/profiles/login` with no identifier; "
        "legacy PRIVATE login still requires identifier; actual five updated navigation notices and README; "
        "A03/A12/A21 + G-AUTH; PH-01–03 previously CLOSED | Only independent readiness then Owner "
        "publication, separate exact DP-A frozen/source consent and later `$codex-create-handoff`; "
        "ACTIVE remains `NO_ACTIVE_HANDOFF`, no remediation/takeover rounds consumed.\n"
    )
    return "".join(rows)


def update_prep_readme(source: str) -> str:
    rows = source.splitlines(keepends=True)
    assert rows and rows[0].startswith("# BF26-DP — Dual Public/Private Login Profiles"), "Wrong README"
    assert "PH-04" in source and "PH-01" in source, "Existing preparation status absent"
    assert MARKER not in source, "README already has this addendum; do not apply twice"
    # Keep reviewer-added re-review 2 status and historical blocks byte-identical after insertion.
    block = (
        "\n" + MARKER + "\n\n"
        "**Current author-submitted state: `D1–D6_OWNER_RATIFIED / PH-01–03_CLOSED_AT_PREPARATION / "
        "PH-04_MEDIUM_OPEN_PENDING_INDEPENDENT_REVIEW / CHANGES_REQUESTED / NO_ACTIVE_HANDOFF`.** "
        f"[Re-review 1]({REVIEW1}) closed PH-01–03 only at DP-A preparation; "
        f"[re-review 2]({REVIEW2}) retained PH-04 for two residuals. "
        "This author correction removes the unnecessary `identifier` from fixed PUBLIC login, aligns A03/A12/A21 "
        "and G-AUTH, and synchronizes the five **actual** current navigation notices. The previous PH-04 "
        "session expiry, reset/revoke, Google and Portability specifications remain as reviewed; well-formed incorrect "
        "PIN verification remains `200 verified:false`. This is **not** independent PH-04 closure, "
        "runtime proof, Owner publication or a source/handoff grant. Earlier status notes below are historical "
        "checkpoints.\n\n"
        f"[Bounded author handback](ph04-r2-author-correction-handback-2026-11-11.md) explains actual "
        "docs-sync evidence and the subsequent separate Codex review. D1–D6 require no new ratification. "
        "`NO_ACTIVE_HANDOFF`, Frontend pause, FR-13, source/evidence and counters are preserved.\n"
    )
    rows.insert(1, block)
    return "".join(rows)


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--repo", required=True, type=Path, help="Owner's PPV checkout")
    parser.add_argument("--apply", action="store_true", help="Actually write all six docs after validation")
    args = parser.parse_args()
    repo = args.repo.expanduser().resolve()
    assert git(repo, "branch", "--show-current") == "main", "Requires main worktree, no branch switch"
    assert git(repo, "rev-parse", "HEAD") == HEAD, "Wrong main HEAD: stop and reassess, do not force"
    assert not git(repo, "diff", "--cached", "--name-only"), "Index must be empty; no stage changes"
    # Verify reviewed input references and copied corrected author docs exist.
    for rel in (BASE + REVIEW1, BASE + REVIEW2, BASE + "owner-ratification-d1-d6-2026-11-11.md",
                BASE + "auth-compatibility-and-session-lifecycle-2026-11-11.md",
                BASE + "ph04-r2-author-correction-handback-2026-11-11.md"):
        assert (repo / rel).is_file(), f"Missing prerequisite file {rel}; copy corrected docs first"
    contract = (repo / (BASE + "auth-compatibility-and-session-lifecycle-2026-11-11.md")).read_text(encoding="utf-8")
    assert '**PUBLIC only**: `{profile:"PUBLIC",password}`' in contract, "Public login author correction missing"
    assert 'identifier resolves singleton Owner' not in contract, "Stale Public identifier contract"
    assert "PH-04" in (repo / (BASE + REVIEW2)).read_text(encoding="utf-8"), "Invalid re-review 2 file"

    tasks: list[tuple[str, str, str]] = []
    for rel in (*NOTICE_TARGETS, README):
        file = repo / rel
        assert file.is_file(), f"Missing target {rel}"
        before = file.read_text(encoding="utf-8")
        assert "\r\n" not in before, f"Unexpected CRLF in {rel}; manual merge required"
        if rel in NOTICE_TARGETS[:3]:
            after = update_current_notice(before, rel)
        elif rel == NOTICE_TARGETS[3]:
            after = update_maintenance_index(before)
        elif rel == NOTICE_TARGETS[4]:
            after = update_active(before)
        else:
            after = update_prep_readme(before)
        assert before != after, f"No effective correction for {rel}"
        assert all(line == line.rstrip(" \t") for line in after.splitlines()), f"Trailing whitespace in {rel}"
        tasks.append((rel, before, after))
    for rel, before, after in tasks:
        print("".join(difflib.unified_diff(before.splitlines(keepends=True), after.splitlines(keepends=True),
                                           fromfile="a/" + rel, tofile="b/" + rel)))
    if args.apply:
        for rel, _before, after in tasks:
            (repo / rel).write_text(after, encoding="utf-8", newline="\n")
    print(f"BF26-DP PH-04/R2 docs-sync {'APPLIED' if args.apply else 'DRY_RUN_ONLY'}: "
          f"{len(tasks)} current documents (five notices + preparation README).")
    if args.apply:
        assert all("re-review-2" in (repo / rel).read_text(encoding="utf-8") for rel in NOTICE_TARGETS), (
            "Five notice verification failed"
        )
        print("VERIFY: actual five notices contain re-review 2 links; run 'git diff --check' and review all six diffs.")


if __name__ == "__main__":
    main()

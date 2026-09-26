#!/usr/bin/env python3
"""Antigravity PreToolUse hook for repository-level safety gates."""

from __future__ import annotations

import json
import re
import sys
from pathlib import PurePath


DENY_COMMAND_PATTERNS = [
    r"(^|[;&|]\s*)git\s+commit\b",
    r"(^|[;&|]\s*)git\s+push\b",
    r"(^|[;&|]\s*)git\s+tag\b",
    r"(^|[;&|]\s*)gh\s+pr\s+(create|merge)\b",
]

ASK_COMMAND_PATTERNS = [
    r"\bgit\s+reset\s+--hard\b",
    r"\bgit\s+clean\s+-[^\s]*f",
    r"\bgit\s+restore\s+--source\b",
    r"\brm\s+-[^\s]*r[^\s]*f\b",
    r"\bdocker\s+(system|volume)\s+prune\b",
    r"\bRemove-Item\b.*\b-Recurse\b.*\b-Force\b",
    r"\brmdir\b.*\b/s\b.*\b/q\b",
    r"\bmvn(?:w|\.cmd)?\s+.*\bdeploy\b",
]

FROZEN_PATH_PATTERNS = [
    "docs/database/personal-private-vault-schema-v1-FROZEN-final.dbml",
    "docs/architecture/module-dependency-matrix.md",
    "docs/repository/repository-package-tree.md",
    "docs/architecture/structurizr/workspace.dsl",
    "docs/architecture/diagrams/source/",
]


def emit(decision: str, reason: str = "") -> None:
    out = {"decision": decision}
    if reason:
        out["reason"] = reason
    print(json.dumps(out))
    raise SystemExit(0)


def norm_path(value: object) -> str:
    if not isinstance(value, str):
        return ""
    return value.replace("\\", "/").lstrip("./")


def target_file(args: dict) -> str:
    for key in ("TargetFile", "targetFile", "AbsolutePath", "absolutePath"):
        if key in args:
            return norm_path(args[key])
    return ""


def is_frozen_path(path: str) -> bool:
    if not path:
        return False
    p = path.lower()
    return any(pattern.lower() in p for pattern in FROZEN_PATH_PATTERNS)


def main() -> None:
    try:
        payload = json.load(sys.stdin)
    except Exception:
        emit("allow", "Safety hook could not parse tool input; allowing rather than blocking normal work.")

    tool_call = payload.get("toolCall") or {}
    name = str(tool_call.get("name") or "")
    args = tool_call.get("args") or {}

    if name == "run_command":
        command = str(args.get("CommandLine") or args.get("commandLine") or "")
        for pattern in DENY_COMMAND_PATTERNS:
            if re.search(pattern, command, flags=re.IGNORECASE):
                emit(
                    "deny",
                    "Repository policy: agents do not commit, push, tag, or create/merge PRs. The owner performs publishing actions.",
                )
        for pattern in ASK_COMMAND_PATTERNS:
            if re.search(pattern, command, flags=re.IGNORECASE):
                emit(
                    "force_ask",
                    "This command can destroy data/history or publish artifacts. Explicit owner confirmation is required.",
                )
        emit("allow")

    if name in {"write_to_file", "replace_file_content", "multi_replace_file_content"}:
        path = target_file(args)
        if is_frozen_path(path):
            emit(
                "force_ask",
                "This file is part of a frozen v1 baseline. Editing it requires deliberate owner confirmation and may require an ADR/docs update.",
            )
        emit("allow")

    emit("allow")


if __name__ == "__main__":
    main()

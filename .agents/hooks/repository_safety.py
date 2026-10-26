#!/usr/bin/env python3
"""Repository safety hook, with Antigravity and Codex PreToolUse adapters.

The policy recognizes direct commands, common Windows shell wrappers,
absolute executable paths, Git global options, CR/LF command boundaries,
and nested supported wrappers with bounded recursion.
"""
from __future__ import annotations

import json
import shlex
import sys
from typing import Any, Iterable

FROZEN_PATH_PATTERNS = [
    "docs/database/personal-private-vault-schema-v1-FROZEN-final.dbml",
    "docs/architecture/module-dependency-matrix.md",
    "docs/repository/repository-package-tree.md",
    "docs/architecture/structurizr/workspace.dsl",
    "docs/architecture/diagrams/source/",
]

_GIT_VALUE_OPTIONS = {
    "-c", "-C", "--config-env", "--git-dir", "--work-tree",
    "--namespace", "--exec-path",
}
_GIT_FLAG_OPTIONS = {
    "--bare", "--no-pager", "--paginate", "--no-replace-objects",
    "--literal-pathspecs", "--no-literal-pathspecs", "--glob-pathspecs",
    "--noglob-pathspecs", "--icase-pathspecs",
}
_DENIED_GIT_SUBCOMMANDS = {"add", "commit", "push", "tag"}
_MAX_WRAPPER_DEPTH = 4


def _strip_outer_quotes(value: str) -> str:
    if len(value) >= 2 and value[0] == value[-1] and value[0] in {"'", '"'}:
        return value[1:-1]
    return value


def _executable_basename(value: str) -> str:
    normalized = _strip_outer_quotes(value).replace("\\", "/").rstrip("/")
    return normalized.rsplit("/", 1)[-1].lower()


def _tokens(command: str) -> list[str]:
    try:
        lexer = shlex.shlex(command, posix=False)
        lexer.whitespace_split = True
        lexer.commenters = ""
        return [_strip_outer_quotes(token) for token in lexer]
    except ValueError:
        return [_strip_outer_quotes(token) for token in command.split()]


def _split_shell_segments(
    command: str,
    shell: str = "powershell",
) -> tuple[list[str], bool]:
    """Split command boundaries using the selected shell's quote/escape rules.

    Ordinary commands in this Windows repository use PowerShell-compatible
    semantics:
    - backslash is an ordinary path character and never escapes a quote;
    - backtick escapes the following character outside single-quoted strings;
    - semicolon, pipe, ampersand, CR, and LF split commands outside quotes.

    For explicitly unwrapped cmd.exe payloads:
    - double quotes group text;
    - caret escapes the following character;
    - ampersand, pipe, CR, and LF split commands outside quotes;
    - semicolon is not a cmd.exe separator.

    The second return value is True when parsing ends with an unclosed quote or
    dangling escape. Security-sensitive callers must fail closed in that case.
    """
    segments: list[str] = []
    current: list[str] = []
    quote: str | None = None
    escaped = False
    index = 0

    while index < len(command):
        char = command[index]

        if escaped:
            current.append(char)
            escaped = False
            index += 1
            continue

        if shell == "powershell":
            if char == "`" and quote != "'":
                current.append(char)
                escaped = True
                index += 1
                continue

            # PowerShell single-quoted strings escape a literal single quote by
            # doubling it: 'it''s'.
            if (
                quote == "'"
                and char == "'"
                and index + 1 < len(command)
                and command[index + 1] == "'"
            ):
                current.extend(["'", "'"])
                index += 2
                continue

            if char in {"'", '"'}:
                if quote is None:
                    quote = char
                elif quote == char:
                    quote = None
                current.append(char)
                index += 1
                continue

            separators = {";", "|", "&", "\r", "\n"}

        elif shell == "cmd":
            if char == "^":
                current.append(char)
                escaped = True
                index += 1
                continue

            if char == '"':
                quote = None if quote == '"' else '"'
                current.append(char)
                index += 1
                continue

            separators = {"|", "&", "\r", "\n"}

        else:
            if char in {"'", '"'}:
                if quote is None:
                    quote = char
                elif quote == char:
                    quote = None
                current.append(char)
                index += 1
                continue

            separators = {";", "|", "&", "\r", "\n"}

        if quote is None and char in separators:
            fragment = "".join(current).strip()
            if fragment:
                segments.append(fragment)
            current = []
            index += 1
            continue

        current.append(char)
        index += 1

    fragment = "".join(current).strip()
    if fragment:
        segments.append(fragment)

    return segments, (quote is not None or escaped)


def _wrapper_payload(tokens: list[str]) -> tuple[str, str] | None:
    """Return (payload, shell_kind) for a supported shell wrapper."""
    if not tokens:
        return None

    executable = _executable_basename(tokens[0])
    lowered = [token.lower() for token in tokens]

    if executable in {"powershell", "powershell.exe", "pwsh", "pwsh.exe"}:
        for marker in ("-command", "-c"):
            if marker in lowered:
                index = lowered.index(marker)
                if index + 1 < len(tokens):
                    return (
                        _strip_outer_quotes(" ".join(tokens[index + 1:]).strip()),
                        "powershell",
                    )
                return None

    if executable in {"cmd", "cmd.exe"}:
        for marker in ("/c", "/k"):
            if marker in lowered:
                index = lowered.index(marker)
                if index + 1 < len(tokens):
                    return (
                        _strip_outer_quotes(" ".join(tokens[index + 1:]).strip()),
                        "cmd",
                    )
                return None

    return None


def _command_segments(command: str) -> tuple[list[list[str]], bool]:
    """Return direct/recursively-unwrapped commands plus ambiguity state."""
    collected: list[list[str]] = []
    ambiguous = False

    def walk(value: str, depth: int, shell: str) -> None:
        nonlocal ambiguous

        raw_segments, split_ambiguous = _split_shell_segments(value, shell)
        if split_ambiguous:
            ambiguous = True

        for raw_segment in raw_segments:
            tokens = _tokens(raw_segment)
            if not tokens:
                continue

            collected.append(tokens)

            if depth >= _MAX_WRAPPER_DEPTH:
                continue

            wrapped = _wrapper_payload(tokens)
            if wrapped:
                payload, payload_shell = wrapped
                walk(payload, depth + 1, payload_shell)

    walk(command, 0, "powershell")
    return collected, ambiguous


def _git_subcommand(tokens: list[str]) -> tuple[str | None, list[str]]:
    if not tokens or _executable_basename(tokens[0]) not in {"git", "git.exe"}:
        return None, []

    lowered_value_options = {value.lower() for value in _GIT_VALUE_OPTIONS}
    index = 1
    while index < len(tokens):
        lower = tokens[index].lower()
        if lower in lowered_value_options:
            index += 2
            continue
        if lower.startswith("-c") and lower != "-c":
            index += 1
            continue
        if any(lower.startswith(option + "=") for option in (
            "--config-env", "--git-dir", "--work-tree", "--namespace", "--exec-path"
        )):
            index += 1
            continue
        if lower in _GIT_FLAG_OPTIONS:
            index += 1
            continue
        if lower.startswith("-"):
            index += 1
            continue
        return lower, [item.lower() for item in tokens[index + 1:]]
    return None, []


def _has_short_flags(tokens: list[str], required: set[str]) -> bool:
    flags: set[str] = set()
    for token in tokens:
        if token.startswith("-") and not token.startswith("--"):
            flags.update(char.lower() for char in token[1:])
    return required.issubset(flags)


def evaluate_command(command: str) -> tuple[str, str]:
    segments, ambiguous = _command_segments(command)

    if ambiguous:
        return (
            "force_ask",
            "Command quoting/escaping is ambiguous or unclosed; explicit owner confirmation is required.",
        )

    for tokens in segments:
        executable = _executable_basename(tokens[0])
        git_subcommand, _ = _git_subcommand(tokens)
        if git_subcommand in _DENIED_GIT_SUBCOMMANDS:
            return "deny", "Repository policy: agents do not add, commit, push, or tag. The owner performs staging and publishing actions."
        if executable in {"gh", "gh.exe"}:
            lower = [token.lower() for token in tokens[1:]]
            if len(lower) >= 2 and lower[0] == "pr" and lower[1] in {"create", "merge", "close"}:
                return "deny", "Repository policy: agents do not create, merge, or close pull requests."
            if len(lower) >= 2 and lower[0] == "release" and lower[1] == "create":
                return "deny", "Repository policy: agents do not create releases."
            if len(lower) >= 2 and lower[0] == "repo" and lower[1] == "delete":
                return "deny", "Repository policy: agents do not delete repositories."

    for tokens in segments:
        executable = _executable_basename(tokens[0])
        git_subcommand, git_args = _git_subcommand(tokens)

        if git_subcommand == "reset" and "--hard" in git_args:
            return "force_ask", "git reset --hard can discard working-tree/history state."
        if git_subcommand == "clean" and ("--force" in git_args or _has_short_flags(git_args, {"f"})):
            return "force_ask", "git clean with force can permanently remove untracked files."
        if git_subcommand == "restore" and any(arg == "--source" or arg.startswith("--source=") for arg in git_args):
            return "force_ask", "git restore --source can overwrite working-tree content."
        if executable == "rm" and _has_short_flags(tokens[1:], {"r", "f"}):
            return "force_ask", "Recursive forced deletion requires owner confirmation."
        if executable == "remove-item":
            lower_args = {token.lower() for token in tokens[1:]}
            if "-recurse" in lower_args and "-force" in lower_args:
                return "force_ask", "Recursive forced PowerShell deletion requires owner confirmation."
        if executable in {"rmdir", "rd"}:
            lower_args = {token.lower() for token in tokens[1:]}
            if "/s" in lower_args and "/q" in lower_args:
                return "force_ask", "Recursive quiet directory deletion requires owner confirmation."
        if executable in {"docker", "docker.exe"} and len(tokens) >= 3:
            if tokens[1].lower() in {"system", "volume"} and tokens[2].lower() == "prune":
                return "force_ask", "Docker prune can remove local data/resources."
            if tokens[1].lower() == "compose":
                compose_subcommand = tokens[2].lower()
                if compose_subcommand not in _DOCKER_COMPOSE_READ_ONLY_SUBCOMMANDS:
                    return (
                        "force_ask",
                        f"docker compose {compose_subcommand} can change, execute in, remove, build, pull, or publish resources and requires owner confirmation.",
                    )
        if executable in {"mvn", "mvn.cmd", "mvnw", "mvnw.cmd"}:
            if any(token.lower() == "deploy" for token in tokens[1:]):
                return "force_ask", "Maven deploy publishes artifacts and requires owner confirmation."

    return "allow", ""



_DOCKER_COMPOSE_READ_ONLY_SUBCOMMANDS = {
    "ps", "logs", "images", "top", "config", "ls", "port", "events",
    "version", "--help", "-h", "help",
}

_DOCKER_COMPOSE_HOST_READ_SUBCOMMANDS = {
    "ps", "logs", "images", "top",
}


_SAFE_POWERSHELL_READ_COMMANDS = {
    "get-childitem", "get-item", "get-location", "get-command", "get-content",
    "select-object", "select-string", "where-object", "sort-object", "measure-object",
}


def permission_overrides_for_command(command: str) -> list[str]:
    """Return narrow overrides only for commands already accepted by safety policy."""
    segments, ambiguous = _command_segments(command)
    if ambiguous:
        return []

    overrides: list[str] = []

    def add(rule: str) -> None:
        if rule not in overrides:
            overrides.append(rule)

    for tokens in segments:
        if not tokens:
            continue
        executable = _executable_basename(tokens[0])
        git_subcommand, _ = _git_subcommand(tokens)

        if git_subcommand in {"status", "diff", "ls-files", "log", "show", "rev-parse", "branch"}:
            add(f"command(git {git_subcommand})")
        if executable in {"mvn", "mvn.cmd", "mvnw", "mvnw.cmd"}:
            add("command(mvn)")
        if executable in {"java", "java.exe"} and any(
            token.lower() in {"-version", "--version"} for token in tokens[1:]
        ):
            add("command(java -version)")
        if executable in {"docker", "docker.exe"} and len(tokens) >= 2:
            subcommand = tokens[1].lower()
            if subcommand in {"info", "version", "ps"}:
                add(f"command(docker {subcommand})")
                add(f"unsandboxed(docker {subcommand})")
            elif subcommand == "compose" and len(tokens) >= 3:
                compose_subcommand = tokens[2].lower()
                if compose_subcommand in _DOCKER_COMPOSE_HOST_READ_SUBCOMMANDS:
                    add(f"command(docker compose {compose_subcommand})")
                    add(f"unsandboxed(docker compose {compose_subcommand})")
        if executable in _SAFE_POWERSHELL_READ_COMMANDS:
            add(f"command({tokens[0]})")

    return overrides

def _normalize_path(value: object) -> str:
    if not isinstance(value, str):
        return ""
    return value.replace("\\", "/").lower()


def _target_file(args: dict) -> str:
    for key in ("TargetFile", "targetFile", "AbsolutePath", "absolutePath"):
        if key in args:
            return _normalize_path(args[key])
    return ""


def _is_frozen_path(path: str) -> bool:
    return bool(path) and any(pattern.lower() in path for pattern in FROZEN_PATH_PATTERNS)


def evaluate_tool_call(name: str, args: Any) -> tuple[str, str]:
    if not isinstance(args, dict):
        return "force_ask", "Repository safety hook: tool arguments must be a dictionary."
    if name == "run_command":
        if "CommandLine" not in args and "commandLine" not in args:
            return "force_ask", "Repository safety hook: run_command requires a CommandLine argument."
        command = args.get("CommandLine") if "CommandLine" in args else args.get("commandLine")
        if not isinstance(command, str) or not command.strip():
            return "force_ask", "Repository safety hook: run_command requires a non-empty string CommandLine."
        return evaluate_command(command)
    if name in {"write_to_file", "replace_file_content", "multi_replace_file_content"}:
        path = _target_file(args)
        if not path:
            return "force_ask", "Repository safety hook: file modification requires a target file."
        if _is_frozen_path(path):
            return "force_ask", "This file is part of a frozen v1 baseline. Editing it requires deliberate owner confirmation and may require an ADR/docs update."
    return "allow", ""


def build_hook_output(name: str, args: Any) -> dict:
    """Build the exact JSON object emitted by the PreToolUse hook."""
    if not isinstance(args, dict):
        return {
            "decision": "force_ask",
            "reason": "Repository safety hook: tool arguments must be a dictionary.",
        }

    decision, reason = evaluate_tool_call(name, args)

    overrides: list[str] = []
    if name == "run_command" and decision == "allow":
        raw_cmd = args.get("CommandLine") if "CommandLine" in args else args.get("commandLine")
        if isinstance(raw_cmd, str) and raw_cmd.strip():
            overrides = permission_overrides_for_command(raw_cmd)

    output: dict = {"decision": decision}
    if reason:
        output["reason"] = reason
    if overrides:
        output["permissionOverrides"] = overrides
    return output


def build_codex_hook_output(payload: Any) -> dict:
    """Adapt native Codex Bash input to the shared evaluator without granting permissions."""
    decision, reason = "force_ask", "Repository safety hook: invalid Codex PreToolUse input."
    if (isinstance(payload, dict) and payload.get("hook_event_name") == "PreToolUse"
            and payload.get("tool_name") == "Bash"):
        tool_input = payload.get("tool_input")
        if isinstance(tool_input, dict):
            decision, reason = evaluate_tool_call("run_command", {
                "CommandLine": tool_input.get("command"),
            })
    if decision == "allow":
        return {}  # Preserve ambient approval/sandbox policy; no allow override.
    # Codex PreToolUse does not support ask. Conservative/invalid input must deny.
    return {"hookSpecificOutput": {
        "hookEventName": "PreToolUse",
        "permissionDecision": "deny",
        "permissionDecisionReason": reason,
    }}


def codex_main() -> None:
    try:
        payload = json.load(sys.stdin)
    except Exception:
        payload = None
    _emit_output(build_codex_hook_output(payload))


def _emit_output(output: dict) -> None:
    print(json.dumps(output))
    raise SystemExit(0)


def main() -> None:
    try:
        payload = json.load(sys.stdin)
    except Exception:
        _emit_output({
            "decision": "force_ask",
            "reason": "Repository safety hook could not parse tool input. Confirmation is required rather than silently bypassing the guard.",
        })

    if not isinstance(payload, dict):
        _emit_output({
            "decision": "force_ask",
            "reason": "Repository safety hook received non-dictionary payload. Confirmation is required.",
        })

    tool_call = payload.get("toolCall")
    if not isinstance(tool_call, dict):
        tool_call = payload.get("tool_call")
    if not isinstance(tool_call, dict):
        tool_call = payload if "name" in payload else {}

    name = str(tool_call.get("name") or payload.get("toolName") or payload.get("tool_name") or "")
    if not name:
        _emit_output({
            "decision": "force_ask",
            "reason": "Repository safety hook received tool call without name. Confirmation required.",
        })

    if "args" in tool_call:
        args = tool_call["args"]
    elif "toolArgs" in payload:
        args = payload["toolArgs"]
    elif "tool_args" in payload:
        args = payload["tool_args"]
    else:
        args = {}

    _emit_output(build_hook_output(name, args))


if __name__ == "__main__":
    if sys.argv[1:] == ["--codex"]:
        codex_main()
    else:
        main()

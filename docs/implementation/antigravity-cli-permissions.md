# Antigravity CLI — Safe Command Permission Setup

Antigravity CLI permissions and the repository `PreToolUse` safety hook are two independent layers.

- CLI permissions decide whether Antigravity asks before executing a command.
- The repository hook is a second safety gate for publishing/destructive operations.
- An `allow` decision from the hook does not auto-approve a command that the CLI permission engine still considers unconfigured.

## Windows note

Antigravity currently uses its Windows terminal-execution behavior on Windows. Fine-grained rules are still
stored in the CLI settings file:

`%USERPROFILE%\.gemini\antigravity-cli\settings.json`

The project is trusted, but **trusted workspace status does not mean every terminal command is auto-approved**.

## Why prompts happened in this project

The inspected CLI `settings.json` already contained a few exact Git status rules, but most Maven rules belonged
to the older `personal-private-project`. It did not contain the direct safe commands used by
`personal-private-vault`, such as:

- `git ls-files`
- general `git diff`
- `mvn -f backend/pom.xml test`

It also had no matching safe `unsandboxed(...)` grants. Therefore those operations could still fall back to
the default Ask behavior, especially when Antigravity requested host/unsandboxed execution.

A corrected settings file is provided separately when this fix is applied. Merge/copy that file to the CLI
settings location and restart Antigravity CLI.

## Curated safe rules for Personal Private Vault

The corrected global settings add only low-risk read/test operations:

```text
command(git status)
command(git diff)
command(git ls-files)
command(git log)
command(git show)
command(git rev-parse)
command(git branch)
command(mvn -f backend/pom.xml test)
command(java -version)
command(docker ps)
command(docker compose ps)

unsandboxed(git status)
unsandboxed(git diff)
unsandboxed(git ls-files)
unsandboxed(git log)
unsandboxed(git show)
unsandboxed(git rev-parse)
unsandboxed(git branch)
unsandboxed(mvn -f backend/pom.xml test)
```

These are deliberately narrower than `command(git)` or `unsandboxed(git)`.

Do not broadly allow:

```text
command(git)
unsandboxed(git)
command(powershell)
command(cmd)
```

Broad Git/shell rules make it much easier for an option-prefixed or wrapped publishing command to evade the
CLI-level allow/deny intent.

## Agent command style

For routine repository inspection and Backend Phase 0 tests, Antigravity should invoke commands directly:

```text
git status
git diff
git ls-files
mvn -f backend/pom.xml test
```

Avoid unnecessary wrappers such as:

```text
powershell -Command git status
cmd /c git status
```

and avoid adding Git global options such as `git -c ...` / `git -C ...` unless they are actually needed.
A wrapped command has a different permission prefix and can legitimately prompt even when the inner command
is allowed.

## Repository hook defense in depth

The workspace hook remains at:

```text
.agents/hooks.json
.agents/hooks/repository_safety.py
```

`hooks.json` correctly runs:

```json
"command": "python hooks/repository_safety.py"
```

because Antigravity executes the workspace hook with `.agents/` as the working directory.

The hook now normalizes direct and wrapped command forms, including absolute Windows executable paths,
handles Git global options before the subcommand, applies PowerShell-compatible quote semantics for ordinary
Windows commands (backslash is a path character; backtick is the escape character), treats CR/LF as command
boundaries outside quotes, recursively unwraps supported PowerShell/pwsh/cmd wrappers with a bounded depth,
and fails closed with `force_ask` when quote/escape state is ambiguous. Examples that are denied:

```text
git push
git -c safe.directory=C:/repo push
git -C C:/repo commit -m test
powershell -Command git push
cmd /c git tag v1
C:\Windows\System32\cmd.exe /c git push
C:\WINDOWS\System32\WindowsPowerShell\v1.0\powershell.exe -Command git push
git status
git push
powershell -Command "cmd /c git push"
gh pr create
gh pr merge
```

Examples requiring confirmation:

```text
git reset --hard
git clean -fd
git restore --source ...
Remove-Item ... -Recurse -Force
Remove-Item ... -Force -Recurse
docker system prune
docker volume prune
mvn deploy
```

Regression-test the hook with:

```text
python -B .agents/hooks/test_repository_safety.py
```

The regression suite covers direct commands, Git global options, absolute Windows shell paths, CR/LF-separated commands, nested supported wrappers, PowerShell quoted Windows paths (including backslash-before-quote), ambiguous/unclosed quote fail-closed behavior, destructive commands, allowed read-only/test commands, and frozen-path protection.

## After replacing the CLI settings file

1. Close the current Antigravity CLI session.
2. Replace/merge `%USERPROFILE%\.gemini\antigravity-cli\settings.json` with the corrected file.
3. Reopen Antigravity CLI from the repository root.
4. Verify direct `git status`, `git ls-files`, and `mvn -f backend/pom.xml test` no longer prompt.
5. If a prompt still appears, inspect the **exact resource shown by the prompt**. If it starts with a wrapper
   such as `powershell ...` or represents a different command, do not broadly allow the wrapper; first make
   the agent use the direct command.

Do not use `--dangerously-skip-permissions`.

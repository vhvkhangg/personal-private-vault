# Backend Phase 0 — Repository Safety Hook Test Evidence

- Date: 2026-09-27
- Scope: PowerShell quote/parser hardening after Codex governance-parser review
- Command: `python -B .agents/hooks/test_repository_safety.py`
- Exit status: `0`
- Result: **PASS**

## Captured unittest result

```text
test_allows_benign_powershell_quoted_windows_paths (test_repository_safety.RepositorySafetyPolicyTests.test_allows_benign_powershell_quoted_windows_paths) ... ok
test_allows_expected_read_only_and_test_commands (test_repository_safety.RepositorySafetyPolicyTests.test_allows_expected_read_only_and_test_commands) ... ok
test_denies_absolute_path_wrappers (test_repository_safety.RepositorySafetyPolicyTests.test_denies_absolute_path_wrappers) ... ok
test_denies_multiline_publish_commands (test_repository_safety.RepositorySafetyPolicyTests.test_denies_multiline_publish_commands) ... ok
test_denies_nested_supported_wrappers (test_repository_safety.RepositorySafetyPolicyTests.test_denies_nested_supported_wrappers) ... ok
test_denies_publish_commands_with_git_options_and_direct_wrappers (test_repository_safety.RepositorySafetyPolicyTests.test_denies_publish_commands_with_git_options_and_direct_wrappers) ... ok
test_powershell_backslash_before_quote_does_not_hide_publish_command (test_repository_safety.RepositorySafetyPolicyTests.test_powershell_backslash_before_quote_does_not_hide_publish_command) ... ok
test_powershell_backtick_escape_and_ambiguous_quote_fail_closed (test_repository_safety.RepositorySafetyPolicyTests.test_powershell_backtick_escape_and_ambiguous_quote_fail_closed) ... ok
test_protects_frozen_paths (test_repository_safety.RepositorySafetyPolicyTests.test_protects_frozen_paths) ... ok
test_requires_confirmation_for_destructive_commands_in_any_flag_order (test_repository_safety.RepositorySafetyPolicyTests.test_requires_confirmation_for_destructive_commands_in_any_flag_order) ... ok

----------------------------------------------------------------------
Ran 10 tests in 0.013s

OK
```

## Explicit Codex H-1 probes

| Probe                                                                                    | Decision    |
| ---------------------------------------------------------------------------------------- | ----------- |
| `git status "C:\temp\"; git push`                                                        | `deny`      |
| `git status "C:\temp\"`                                                                  | `allow`     |
| `git status "C:\temp;archive"`                                                           | `allow`     |
| `git status "C:\temp`                                                                    | `force_ask` |
| `C:\WINDOWS\System32\WindowsPowerShell\v1.0\powershell.exe -NoProfile -Command git push` | `deny`      |
| `git status + newline + git push`                                                        | `deny`      |

The hook no longer applies C/POSIX backslash-before-quote semantics to ordinary PowerShell commands.
A Windows path such as `C:\temp\` therefore cannot hide a following command separator. PowerShell
backtick escaping is recognized, and ambiguous/unclosed quote state fails closed with `force_ask`.

No backend Java, Maven, database, module-boundary, or frozen architecture artifact was changed.

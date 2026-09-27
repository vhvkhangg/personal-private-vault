# Agent Tooling Governance — Safety Hook Test Evidence

- Date: 2026-09-27
- Scope: governance/tooling remediation after Codex review
- Command: `python -B .agents/hooks/test_repository_safety.py`
- Exit status: `0`
- Result: **PASS**
- Test count: **13**
- Duration reported by unittest: **0.005s**

## Remediation coverage

- final hook JSON for exact read-only Docker Compose host overrides;
- no broad Docker Compose override;
- `docker compose down -v`, `rm -f`, `push`, `up`, `build`, `pull`, and `exec` return `force_ask`;
- destructive/publishing Compose outputs contain no `permissionOverrides`;
- prior Git publishing, destructive-command, PowerShell parser, Maven/Java/Docker inspection, and frozen-path
  safeguards remain covered.

## Captured output

```text
Spreadsheet runtime warmup failed during python startup
Traceback (most recent call last):
  File "/tmp/tmp.L2TH2Y5coc/artifact_tool_v2-2.8.22/artifact_tool/patches/warm_spreadsheet_runtime_on_startup.py", line 26, in warm_spreadsheet_runtime_on_startup
  File "/tmp/tmp.L2TH2Y5coc/artifact_tool_v2-2.8.22/artifact_tool/spreadsheet_warmup.py", line 785, in warm_spreadsheet_runtime
  File "/tmp/tmp.L2TH2Y5coc/artifact_tool_v2-2.8.22/artifact_tool/spreadsheet_warmup.py", line 720, in _warm_feature_flows
  File "/tmp/tmp.L2TH2Y5coc/artifact_tool_v2-2.8.22/artifact_tool/spreadsheet_warmup.py", line 704, in _warm_collaboration_flows
  File "/tmp/tmp.L2TH2Y5coc/artifact_tool_v2-2.8.22/artifact_tool/generated/interface/models.py", line 32317, in hydrate_crdt_from_proto
  File "/tmp/tmp.L2TH2Y5coc/artifact_tool_v2-2.8.22/artifact_tool/rpc/remote.py", line 749, in __call__
  File "/tmp/tmp.L2TH2Y5coc/artifact_tool_v2-2.8.22/artifact_tool/rpc/client.py", line 150, in call
artifact_tool.rpc.client.RemoteError: hydrateCrdtFromProto requires an empty collaborative document.
test_allows_benign_powershell_quoted_windows_paths (__main__.RepositorySafetyPolicyTests.test_allows_benign_powershell_quoted_windows_paths) ... ok
test_allows_expected_read_only_and_test_commands (__main__.RepositorySafetyPolicyTests.test_allows_expected_read_only_and_test_commands) ... ok
test_compose_destructive_and_publishing_commands_force_ask_without_overrides (__main__.RepositorySafetyPolicyTests.test_compose_destructive_and_publishing_commands_force_ask_without_overrides) ... ok
test_compose_read_only_final_hook_json_is_narrow (__main__.RepositorySafetyPolicyTests.test_compose_read_only_final_hook_json_is_narrow) ... ok
test_denies_absolute_path_wrappers (__main__.RepositorySafetyPolicyTests.test_denies_absolute_path_wrappers) ... ok
test_denies_multiline_publish_commands (__main__.RepositorySafetyPolicyTests.test_denies_multiline_publish_commands) ... ok
test_denies_nested_supported_wrappers (__main__.RepositorySafetyPolicyTests.test_denies_nested_supported_wrappers) ... ok
test_denies_publish_commands_with_git_options_and_direct_wrappers (__main__.RepositorySafetyPolicyTests.test_denies_publish_commands_with_git_options_and_direct_wrappers) ... ok
test_powershell_backslash_before_quote_does_not_hide_publish_command (__main__.RepositorySafetyPolicyTests.test_powershell_backslash_before_quote_does_not_hide_publish_command) ... ok
test_powershell_backtick_escape_and_ambiguous_quote_fail_closed (__main__.RepositorySafetyPolicyTests.test_powershell_backtick_escape_and_ambiguous_quote_fail_closed) ... ok
test_protects_frozen_paths (__main__.RepositorySafetyPolicyTests.test_protects_frozen_paths) ... ok
test_requires_confirmation_for_destructive_commands_in_any_flag_order (__main__.RepositorySafetyPolicyTests.test_requires_confirmation_for_destructive_commands_in_any_flag_order) ... ok
test_safe_command_permission_overrides (__main__.RepositorySafetyPolicyTests.test_safe_command_permission_overrides) ... ok

----------------------------------------------------------------------
Ran 13 tests in 0.005s

OK
```

No backend production Java, backend tests, Flyway V1, Maven configuration, frozen DBML/module boundary, ADR, or
architecture diagram changed or was retested for this governance-only remediation.

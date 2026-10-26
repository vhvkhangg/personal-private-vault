"""Regression tests for the repository safety hook."""

import json
import os
import subprocess
import sys
import unittest

from repository_safety import build_hook_output, evaluate_command, evaluate_tool_call, permission_overrides_for_command


class RepositorySafetyPolicyTests(unittest.TestCase):

    def assert_decision(self, command: str, expected: str) -> None:
        actual, _ = evaluate_command(command)
        self.assertEqual(expected, actual, msg=command)

    def test_denies_publish_commands_with_git_options_and_direct_wrappers(self) -> None:
        cases = [
            "git add .",
            "git add -A",
            "git commit -m test",
            "git push origin main",
            "git tag v1.0.0",
            "git.exe add .",
            "git.exe push",
            r'"C:\Program Files\Git\cmd\git.exe" add .',
            r'"C:\Program Files\Git\cmd\git.exe" push origin main',
            "git -c safe.directory='C:/repo' add .",
            "git -c safe.directory='C:/repo' push origin main",
            'git -C "C:/repo" add .',
            'git -C "C:/repo" commit -m test',
            "powershell -Command git add .",
            "powershell -Command git push origin main",
            'powershell.exe -NoProfile -Command "git -c safe.directory=C:/repo add ."',
            'powershell.exe -NoProfile -Command "git -c safe.directory=C:/repo push origin main"',
            "pwsh -Command git add .",
            "pwsh -Command git tag v1",
            "cmd /c git add .",
            "cmd /c git push origin main",
            "gh pr create --title test",
            "gh.exe pr create --title test",
            "gh pr close 42",
            "powershell -Command gh pr merge 42",
            "powershell -Command gh.exe pr merge 42",
            "gh release create v1.0.0",
            "gh.exe repo delete owner/repo",
        ]
        for command in cases:
            with self.subTest(command=command):
                self.assert_decision(command, "deny")

    def test_denies_absolute_path_wrappers(self) -> None:
        cases = [
            r'C:\WINDOWS\System32\WindowsPowerShell\v1.0\powershell.exe -NoProfile -Command git push',
            r'C:\Windows\System32\cmd.exe /c git push',
            r'"C:\Windows\System32\cmd.exe" /c "git tag v1"',
            r'"C:\Program Files\PowerShell\7\pwsh.exe" -Command "git commit -m test"',
        ]
        for command in cases:
            with self.subTest(command=command):
                self.assert_decision(command, "deny")

    def test_denies_multiline_publish_commands(self) -> None:
        cases = [
            "git status\ngit push origin main",
            "git status\r\ngit tag v1",
            "git status\rgit commit -m test",
            'powershell -Command "git status\ngit push origin main"',
            r'C:\Windows\System32\cmd.exe /c "git status' + "\r\n" + 'git push"',
        ]
        for command in cases:
            with self.subTest(command=command):
                self.assert_decision(command, "deny")

    def test_denies_nested_supported_wrappers(self) -> None:
        cases = [
            'powershell -Command "cmd /c git push origin main"',
            'cmd /c "powershell -Command git tag v1"',
            r'C:\Windows\System32\cmd.exe /c "C:\WINDOWS\System32\WindowsPowerShell\v1.0\powershell.exe -NoProfile -Command git push"',
            r'"C:\Program Files\PowerShell\7\pwsh.exe" -Command "cmd /c git commit -m test"',
        ]
        for command in cases:
            with self.subTest(command=command):
                self.assert_decision(command, "deny")

    def test_powershell_backslash_before_quote_does_not_hide_publish_command(self) -> None:
        cases = [
            r'git status "C:\temp\"; git push',
            r'git status "C:\work\repo\"; git tag v1',
        ]
        for command in cases:
            with self.subTest(command=command):
                self.assert_decision(command, "deny")

    def test_allows_benign_powershell_quoted_windows_paths(self) -> None:
        cases = [
            r'git status "C:\temp\"',
            r'git status "C:\temp;archive"',
            r'Write-Output "C:\temp\"; Write-Output SECOND',
        ]
        for command in cases:
            with self.subTest(command=command):
                self.assert_decision(command, "allow")

    def test_powershell_backtick_escape_and_ambiguous_quote_fail_closed(self) -> None:
        cases = [
            'git status "C:\\temp',
            'git status "C:\\temp`"',
            'powershell -Command "git status',
        ]
        for command in cases:
            with self.subTest(command=command):
                self.assert_decision(command, "force_ask")

    def test_requires_confirmation_for_destructive_commands_in_any_flag_order(self) -> None:
        cases = [
            "git reset --hard HEAD~1",
            "git -c safe.directory=C:/repo clean -fd",
            "git clean -xdf",
            "git restore --source HEAD -- file.txt",
            "rm -rf temp",
            "rm -fr temp",
            "Remove-Item temp -Recurse -Force",
            "Remove-Item temp -Force -Recurse",
            "powershell -Command Remove-Item temp -Force -Recurse",
            r'C:\Windows\System32\WindowsPowerShell\v1.0\powershell.exe -Command Remove-Item temp -Force -Recurse',
            "rmdir temp /s /q",
            "rmdir /q /s temp",
            "docker system prune",
            "docker volume prune",
            "mvn deploy",
            "mvn.cmd -q deploy",
            "powershell -Command mvn -f backend/pom.xml deploy",
        ]
        for command in cases:
            with self.subTest(command=command):
                self.assert_decision(command, "force_ask")

    def test_allows_expected_read_only_and_test_commands(self) -> None:
        cases = [
            "git status",
            "git diff --stat",
            "git ls-files",
            "git -c safe.directory=C:/repo status",
            r'"C:\Program Files\Git\cmd\git.exe" status',
            "mvn -f backend/pom.xml test",
            "docker compose ps",
            'echo "git push"',
            "Remove-Item temp -Force",
            'powershell -Command "git status\ngit diff --stat"',
        ]
        for command in cases:
            with self.subTest(command=command):
                self.assert_decision(command, "allow")

    def test_safe_command_permission_overrides(self) -> None:
        cases = {
            "mvn -version": {"command(mvn)"},
            "mvn -f backend/pom.xml test": {"command(mvn)"},
            "mvn -f backend/pom.xml clean verify": {"command(mvn)"},
            "java -version": {"command(java -version)"},
            "docker info": {"command(docker info)", "unsandboxed(docker info)"},
            "docker compose ps": {"command(docker compose ps)", "unsandboxed(docker compose ps)"},
            "Get-ChildItem backend": {"command(Get-ChildItem)"},
            "Select-String -Path README.md -Pattern Phase": {"command(Select-String)"},
        }
        for command, expected in cases.items():
            with self.subTest(command=command):
                actual = set(permission_overrides_for_command(command))
                self.assertTrue(expected.issubset(actual), msg=(command, actual))
        self.assertEqual([], permission_overrides_for_command("git push origin main"))

    def test_compose_destructive_and_publishing_commands_force_ask_without_overrides(self) -> None:
        cases = [
            "docker compose down -v",
            "docker compose rm -f",
            "docker compose push",
            "docker compose up -d",
            "docker compose build",
            "docker compose pull",
            "docker compose exec postgres sh",
        ]
        for command in cases:
            with self.subTest(command=command):
                self.assert_decision(command, "force_ask")
                output = build_hook_output("run_command", {"CommandLine": command})
                self.assertEqual("force_ask", output["decision"])
                self.assertNotIn("permissionOverrides", output)

    def test_compose_read_only_final_hook_json_is_narrow(self) -> None:
        expected = {
            "docker compose ps": [
                "command(docker compose ps)",
                "unsandboxed(docker compose ps)",
            ],
            "docker compose logs --tail 50 postgres": [
                "command(docker compose logs)",
                "unsandboxed(docker compose logs)",
            ],
            "docker compose images": [
                "command(docker compose images)",
                "unsandboxed(docker compose images)",
            ],
            "docker compose top": [
                "command(docker compose top)",
                "unsandboxed(docker compose top)",
            ],
        }
        for command, overrides in expected.items():
            with self.subTest(command=command):
                output = build_hook_output("run_command", {"CommandLine": command})
                self.assertEqual(
                    {"decision": "allow", "permissionOverrides": overrides},
                    json.loads(json.dumps(output)),
                )

        for command in ["docker compose config", "docker compose version", "docker compose --help"]:
            with self.subTest(command=command):
                output = build_hook_output("run_command", {"CommandLine": command})
                self.assertEqual("allow", output["decision"])
                self.assertNotIn("permissionOverrides", output)

    def test_protects_frozen_paths(self) -> None:
        decision, _ = evaluate_tool_call(
            "replace_file_content",
            {"TargetFile": r"C:\repo\docs\database\personal-private-vault-schema-v1-FROZEN-final.dbml"},
        )
        self.assertEqual("force_ask", decision)

    def test_git_add_denied_without_permission_overrides(self) -> None:
        for cmd in ["git add .", "git add -A", "git.exe add file.txt", "git -C repo add ."]:
            with self.subTest(command=cmd):
                output = build_hook_output("run_command", {"CommandLine": cmd})
                self.assertEqual("deny", output["decision"])
                self.assertNotIn("permissionOverrides", output)
                self.assertIn("agents do not add", output.get("reason", ""))

    def test_hook_cli_payload_schemas_and_malformed_input(self) -> None:
        import repository_safety
        from unittest.mock import patch
        from io import StringIO

        cases = [
            # Standard camelCase toolCall
            ('{"toolCall": {"name": "run_command", "args": {"CommandLine": "git status"}}}', "allow"),
            # snake_case tool_call
            ('{"tool_call": {"name": "run_command", "args": {"commandLine": "git status"}}}', "allow"),
            # Flat toolName/toolArgs
            ('{"toolName": "run_command", "toolArgs": {"CommandLine": "git status"}}', "allow"),
            # Staging denied
            ('{"toolCall": {"name": "run_command", "args": {"CommandLine": "git add ."}}}', "deny"),
            # Malformed JSON -> force_ask
            ('NOT_JSON', "force_ask"),
            # Non-dict JSON -> force_ask
            ('[1, 2, 3]', "force_ask"),
            # Missing tool name -> force_ask
            ('{"toolCall": {}}', "force_ask"),
            ('{}', "force_ask"),
            # B2-F03: Malformed run-command arguments fail closed with force_ask
            ('{"toolCall": {"name": "run_command", "args": "git add ."}}', "force_ask"),
            ('{"toolCall": {"name": "run_command", "args": ["git", "add", "."]}}', "force_ask"),
            ('{"toolCall": {"name": "run_command", "args": {}}}', "force_ask"),
            ('{"toolCall": {"name": "run_command", "args": {"CommandLine": 123}}}', "force_ask"),
            ('{"toolCall": {"name": "run_command", "args": {"CommandLine": "   "}}}', "force_ask"),
            ('{"toolCall": {"name": "run_command", "args": {"commandLine": ""}}}', "force_ask"),
            ('{"toolCall": {"name": "write_to_file", "args": "not-dict"}}', "force_ask"),
            ('{"toolCall": {"name": "replace_file_content", "args": {}}}', "force_ask"),
        ]

        for payload_str, expected_decision in cases:
            with self.subTest(payload=payload_str):
                with patch("sys.stdin", StringIO(payload_str)), patch("sys.stdout", new_callable=StringIO) as out:
                    with self.assertRaises(SystemExit) as cm:
                        repository_safety.main()
                    self.assertEqual(0, cm.exception.code)
                    result = json.loads(out.getvalue())
                    self.assertEqual(expected_decision, result.get("decision"))
                    if expected_decision != "allow":
                        self.assertNotIn("permissionOverrides", result)

    def test_malformed_run_command_arguments_fail_closed(self) -> None:
        malformed_args = [
            "git add .",
            ["git", "add", "."],
            {},
            {"otherKey": "git status"},
            {"CommandLine": 123},
            {"CommandLine": None},
            {"CommandLine": ""},
            {"CommandLine": "   "},
            {"commandLine": ""},
            {"commandLine": " \t\r\n "},
        ]
        for bad_arg in malformed_args:
            with self.subTest(args=bad_arg):
                output = build_hook_output("run_command", bad_arg)
                self.assertEqual("force_ask", output["decision"])
                self.assertNotIn("permissionOverrides", output)
                self.assertTrue(output.get("reason", "").startswith("Repository safety hook"))

    def test_hook_process_invocation_from_root_and_scoped_cwd(self) -> None:
        # 1. Canonical hook execution from customization root (.agents) as configured in hooks.json
        cmd_agents = [sys.executable, "hooks/repository_safety.py"]
        p_agents_status = subprocess.run(
            cmd_agents,
            cwd=".agents",
            input=json.dumps({"toolCall": {"name": "run_command", "args": {"CommandLine": "git status"}}}).encode(),
            capture_output=True,
        )
        self.assertEqual(0, p_agents_status.returncode, p_agents_status.stderr.decode())
        out_agents_status = json.loads(p_agents_status.stdout.decode())
        self.assertEqual("allow", out_agents_status["decision"])
        self.assertIn("command(git status)", out_agents_status.get("permissionOverrides", []))

        p_agents_add = subprocess.run(
            cmd_agents,
            cwd=".agents",
            input=json.dumps({"toolCall": {"name": "run_command", "args": {"CommandLine": "git add ."}}}).encode(),
            capture_output=True,
        )
        self.assertEqual(0, p_agents_add.returncode, p_agents_add.stderr.decode())
        out_agents_add = json.loads(p_agents_add.stdout.decode())
        self.assertEqual("deny", out_agents_add["decision"])
        self.assertNotIn("permissionOverrides", out_agents_add)

        # 2. Direct execution from workspace root (.)
        cmd_root = [sys.executable, ".agents/hooks/repository_safety.py"]
        p_root_status = subprocess.run(
            cmd_root,
            cwd=".",
            input=json.dumps({"toolCall": {"name": "run_command", "args": {"CommandLine": "git status"}}}).encode(),
            capture_output=True,
        )
        self.assertEqual(0, p_root_status.returncode, p_root_status.stderr.decode())
        out_root_status = json.loads(p_root_status.stdout.decode())
        self.assertEqual("allow", out_root_status["decision"])
        self.assertIn("command(git status)", out_root_status.get("permissionOverrides", []))

        p_root_add = subprocess.run(
            cmd_root,
            cwd=".",
            input=json.dumps({"toolCall": {"name": "run_command", "args": {"CommandLine": "git add ."}}}).encode(),
            capture_output=True,
        )
        self.assertEqual(0, p_root_add.returncode, p_root_add.stderr.decode())
        out_root_add = json.loads(p_root_add.stdout.decode())
        self.assertEqual("deny", out_root_add["decision"])
        self.assertNotIn("permissionOverrides", out_root_add)

    def test_b2_f04_removed_audit_never_creates_or_appends(self) -> None:
        import tempfile
        from io import StringIO
        from pathlib import Path
        from unittest.mock import patch
        import repository_safety

        with tempfile.TemporaryDirectory() as tmpdir:
            tmppath = Path(tmpdir)
            existing_target = tmppath / "existing-target.jsonl"
            prefix = b'{"synthetic_prior":true}\n'
            existing_target.write_bytes(prefix)

            canary = "SYNTHETIC_B2_F04_PRIVATE_ARGUMENT"
            payload = {"lastUserInput": canary, "toolCall": {
                "name": "run_command", "args": {"CommandLine": f"Write-Output '{canary}'"}
            }}
            for target in (tmppath / "fresh.jsonl", existing_target):
                with self.subTest(target=target.name):
                    env = dict(os.environ, ANTIGRAVITY_HOOK_AUDIT_LOG=str(target))
                    proc = subprocess.run(
                        [sys.executable, "-B", ".agents/hooks/repository_safety.py", canary],
                        input=json.dumps(payload), text=True, capture_output=True,
                        env=env, timeout=10,
                    )
                    self.assertEqual(0, proc.returncode, proc.stderr)
                    self.assertEqual("allow", json.loads(proc.stdout)["decision"])
                    self.assertNotIn(canary, proc.stdout + proc.stderr)
            self.assertFalse((tmppath / "fresh.jsonl").exists())
            self.assertEqual(prefix, existing_target.read_bytes())

            # No target inspection/write means a concurrent claimant cannot be appended to.
            for command, expected in [("git status", "allow"), ("git add -h", "deny"), ("", "force_ask")]:
                payload["toolCall"]["args"]["CommandLine"] = command
                with patch.dict(os.environ, {"ANTIGRAVITY_HOOK_AUDIT_LOG": str(existing_target)}), \
                     patch("sys.stdin", StringIO(json.dumps(payload))), \
                     patch("sys.stdout", new_callable=StringIO) as out, \
                     patch("builtins.open") as file_open, patch.object(Path, "exists") as exists, \
                     patch.object(Path, "mkdir") as mkdir:
                    with self.assertRaises(SystemExit) as exited:
                        repository_safety.main()
                    self.assertEqual(0, exited.exception.code)
                    self.assertEqual(expected, json.loads(out.getvalue())["decision"])
                    file_open.assert_not_called()
                    exists.assert_not_called()
                    mkdir.assert_not_called()
            self.assertEqual(prefix, existing_target.read_bytes())

    def test_b2_f01_codex_adapter_denies_options_and_string_wrappers(self) -> None:
        import repository_safety

        commands = [
            'git -C "C:/synthetic fixture" add -h',
            'git -c core.quotePath=false add -h',
            'cmd.exe /c "git add -h"',
            'powershell.exe -NoProfile -Command "git add -h"',
            'cmd.exe /c powershell.exe -Command "git add -h"',
        ]
        for command in commands:
            with self.subTest(command=command):
                output = repository_safety.build_codex_hook_output({
                    "hook_event_name": "PreToolUse", "tool_name": "Bash",
                    "tool_input": {"command": command},
                })
                self.assertEqual("deny", output["hookSpecificOutput"]["permissionDecision"])
                self.assertNotIn("permissionOverrides", output)

    def test_b2_f01_codex_adapter_leaves_benign_commands_to_ambient_policy(self) -> None:
        import repository_safety

        for command in ("git status", "git diff --stat", 'git -C "C:/synthetic fixture" status',
                        'python -B -m unittest discover -s .agents/hooks'):
            with self.subTest(command=command):
                self.assertEqual({}, repository_safety.build_codex_hook_output({
                    "hook_event_name": "PreToolUse", "tool_name": "Bash",
                    "tool_input": {"command": command},
                }))

    def test_b2_f01_codex_entrypoint_malformed_input_denied_without_payload(self) -> None:
        canary = "SYNTHETIC_B2_F01_PRIVATE_INPUT"
        payloads = ["not-json-" + canary, "[]", "{}", json.dumps({
            "hook_event_name": "PreToolUse", "tool_name": "Bash",
            "tool_input": {"command": [canary]},
        })]
        for payload in payloads:
            with self.subTest(payload=payload):
                proc = subprocess.run(
                    [sys.executable, "-B", ".agents/hooks/repository_safety.py", "--codex"],
                    input=payload, text=True, capture_output=True, timeout=10,
                )
                self.assertEqual(0, proc.returncode, proc.stderr)
                self.assertNotIn(canary, proc.stdout + proc.stderr)
                output = json.loads(proc.stdout)["hookSpecificOutput"]
                self.assertEqual("PreToolUse", output["hookEventName"])
                self.assertEqual("deny", output["permissionDecision"])
                self.assertTrue(output["permissionDecisionReason"])


if __name__ == "__main__":
    unittest.main(verbosity=2)

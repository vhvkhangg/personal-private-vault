"""Regression tests for the repository safety hook."""

import unittest

from repository_safety import evaluate_command, evaluate_tool_call


class RepositorySafetyPolicyTests(unittest.TestCase):

    def assert_decision(self, command: str, expected: str) -> None:
        actual, _ = evaluate_command(command)
        self.assertEqual(expected, actual, msg=command)

    def test_denies_publish_commands_with_git_options_and_direct_wrappers(self) -> None:
        cases = [
            "git commit -m test",
            "git push origin main",
            "git tag v1.0.0",
            "git.exe push",
            r'"C:\Program Files\Git\cmd\git.exe" push origin main',
            "git -c safe.directory='C:/repo' push origin main",
            'git -C "C:/repo" commit -m test',
            "powershell -Command git push origin main",
            'powershell.exe -NoProfile -Command "git -c safe.directory=C:/repo push origin main"',
            "pwsh -Command git tag v1",
            "cmd /c git push origin main",
            "gh pr create --title test",
            "powershell -Command gh pr merge 42",
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

    def test_protects_frozen_paths(self) -> None:
        decision, _ = evaluate_tool_call(
            "replace_file_content",
            {"TargetFile": r"C:\repo\docs\database\personal-private-vault-schema-v1-FROZEN-final.dbml"},
        )
        self.assertEqual("force_ask", decision)


if __name__ == "__main__":
    unittest.main(verbosity=2)

"""Exercise the Non-RAG publisher against an owned local bare remote."""

import contextlib
import importlib.util
import io
from pathlib import Path
import subprocess
from tempfile import TemporaryDirectory
import unittest
from unittest.mock import patch


spec = importlib.util.spec_from_file_location(
    "publish_non_rag", Path(__file__).with_name("publish-non-rag.py"),
)
publisher = importlib.util.module_from_spec(spec)
spec.loader.exec_module(publisher)


class PublicationTests(unittest.TestCase):
    def setUp(self) -> None:
        directory = self.enterContext(TemporaryDirectory(prefix="ppv-publish-test-"))
        self.root = Path(directory, "work")
        self.remote = Path(directory, "origin.git")
        self.root.mkdir()
        self.enterContext(patch.object(publisher, "ROOT", self.root))
        self.enterContext(patch.object(publisher, "ORIGIN", self.remote.as_posix()))
        self.git = publisher.git
        self.git("init", "--initial-branch=main")
        self.git("init", "--bare", "--initial-branch=main", self.remote.as_posix())
        self.git("config", "--local", "user.name", "PPV publisher fixture")
        self.git("config", "--local", "user.email", "fixture@example.invalid")
        self.git("config", "--local", "commit.gpgsign", "false")
        self.git("config", "--local", "core.hooksPath", str(self.root / ".git" / "fixture-hooks"))
        self.git("remote", "add", "origin", publisher.ORIGIN)
        (self.root / "approved.txt").write_text("baseline\n", encoding="utf-8")
        self.git("add", "--", "approved.txt")
        self.git("commit", "-m", "fixture baseline")
        self.git("push", "origin", "HEAD:refs/heads/main")
        self.base = self.git("rev-parse", "HEAD")
        (self.root / "approved.txt").write_text("reviewed change\n", encoding="utf-8")
        (self.root / "new file.txt").write_text("reviewed addition\n", encoding="utf-8")
        self.paths = ["approved.txt", "new file.txt"]
        self.tree = self.candidate()

    def candidate(self) -> str:
        head, approved = publisher.coordinates(self.base, self.paths)
        self.assertEqual(self.base, head)
        return publisher.candidate_tree(self.base, self.paths, approved)

    def publish(self) -> None:
        head, approved = publisher.coordinates(self.base, self.paths)
        with contextlib.redirect_stdout(io.StringIO()):
            publisher.publish(self.base, self.tree, "reviewed change", self.paths, head, approved)

    def remote_head(self) -> str:
        return self.git("ls-remote", "origin", publisher.MAIN).split("\t")[0]

    def test_publishes_only_reviewed_paths_and_can_verify_again(self) -> None:
        (self.root / "unrelated.txt").write_text("local only\n", encoding="utf-8")
        self.assertFalse(publisher.changed_paths("--cached", self.base))
        self.git("tag", "-a", "fixture-tag", "-m", "local tag", self.base)
        self.git("config", "--local", "push.followTags", "true")
        self.publish()
        head = self.git("rev-parse", "HEAD")
        self.assertEqual(head, self.remote_head())
        self.assertEqual(self.tree, self.git("rev-parse", "HEAD^{tree}"))
        self.assertEqual(set(self.paths), publisher.changed_paths(self.base, head))
        self.assertEqual("", self.git("ls-remote", "origin", "refs/tags/*"))
        self.assertTrue((self.root / "unrelated.txt").is_file())
        self.publish()
        self.assertEqual(head, self.git("rev-parse", "HEAD"))

    def test_rejects_modified_candidate_before_staging(self) -> None:
        (self.root / "new file.txt").write_text("modified after review\n", encoding="utf-8")
        with self.assertRaisesRegex(ValueError, "Candidate differs"):
            self.publish()
        self.assertFalse(publisher.changed_paths("--cached", self.base))
        self.assertEqual(self.base, self.remote_head())

    def test_rejects_remote_advance_before_staging(self) -> None:
        original_tree = self.git("rev-parse", f"{self.base}^{{tree}}")
        intervening_commit = self.git("commit-tree", original_tree, "-p", self.base, "-m", "remote advance")
        self.git("push", "origin", f"{intervening_commit}:refs/heads/main")
        with self.assertRaisesRegex(ValueError, "Remote main advanced"):
            self.publish()
        self.assertFalse(publisher.changed_paths("--cached", self.base))
        self.assertEqual(self.base, self.git("rev-parse", "HEAD"))
        self.assertEqual(intervening_commit, self.remote_head())

    def test_preserves_unrelated_index(self) -> None:
        (self.root / "unrelated.txt").write_text("already staged\n", encoding="utf-8")
        self.git("add", "--", "unrelated.txt")
        with self.assertRaisesRegex(ValueError, "index contains changes"):
            self.publish()
        self.assertEqual({"unrelated.txt"}, publisher.changed_paths("--cached", self.base))
        self.assertEqual(self.base, self.remote_head())

    def test_rejects_resolved_merge_without_changing_head_or_index(self) -> None:
        self.git("switch", "-c", "fixture-topic")
        self.git("add", "--", *self.paths)
        self.git("commit", "-m", "reviewed content on a topic")
        self.git("switch", "main")
        self.git("merge", "--no-commit", "--no-ff", "fixture-topic")
        merge_head = self.git("rev-parse", "MERGE_HEAD")
        self.assertEqual("", self.git("ls-files", "--unmerged"))
        self.assertEqual(self.tree, self.git("write-tree"))

        with self.assertRaisesRegex(ValueError, "Git operation in progress.*MERGE_HEAD"):
            self.publish()

        self.assertEqual(self.base, self.git("rev-parse", "HEAD"))
        self.assertEqual(merge_head, self.git("rev-parse", "MERGE_HEAD"))
        self.assertEqual(self.tree, self.git("write-tree"))
        self.assertEqual(self.base, self.remote_head())

    def test_rejects_remote_rewind_or_deletion_after_baseline_read(self) -> None:
        ancestor = self.base
        self.git("commit", "--allow-empty", "-m", "advance the approved baseline")
        self.git("push", "origin", "HEAD:refs/heads/main")
        self.base = self.git("rev-parse", "HEAD")

        for replacement in (ancestor, ""):
            with self.subTest(replacement=replacement):
                remote_git = ("--git-dir", self.remote.as_posix(), "update-ref")
                self.git(*remote_git, publisher.MAIN, self.base)

                def change_remote_before_push(*args: str, **kwargs: object) -> str:
                    if "push" in args:
                        if replacement:
                            self.git(*remote_git, publisher.MAIN, replacement)
                        else:
                            self.git(*remote_git, "-d", publisher.MAIN)
                    return self.git(*args, **kwargs)

                with patch.object(publisher, "git", side_effect=change_remote_before_push):
                    with self.assertRaises(subprocess.CalledProcessError):
                        self.publish()
                self.assertEqual(replacement, self.remote_head())
                self.assertEqual(self.base, self.git("rev-parse", "HEAD^"))
                self.assertEqual(self.tree, self.git("rev-parse", "HEAD^{tree}"))

    def test_rejects_non_main_branch_and_wrong_origin(self) -> None:
        self.git("switch", "-c", "different")
        with self.assertRaisesRegex(ValueError, "main branch"):
            self.publish()
        self.git("switch", "main")
        self.git("remote", "set-url", "--push", "origin", str(self.root / "wrong.git"))
        with self.assertRaisesRegex(ValueError, "approved PPV origin"):
            self.publish()

    def test_rejects_replaced_commit_with_unapproved_original_metadata(self) -> None:
        base_tree = self.git("rev-parse", f"{self.base}^{{tree}}")
        reviewed_commit = self.git("commit-tree", self.tree, "-p", self.base, "-m", "reviewed")
        unparented_commit = self.git("commit-tree", self.tree, "-m", "missing parent")
        wrong_tree_commit = self.git("commit-tree", base_tree, "-p", self.base, "-m", "wrong tree")
        for original, reason in (
            (unparented_commit, "Expected one commit on the approved base"),
            (wrong_tree_commit, "Local commit differs from the independently reviewed tree"),
        ):
            with self.subTest(reason=reason):
                self.git("update-ref", publisher.MAIN, original)
                self.git("replace", original, reviewed_commit)
                presented = subprocess.check_output(
                    ["git", "show", "-s", "--format=%P %T", original],
                    cwd=self.root, text=True,
                ).strip()
                self.assertEqual(f"{self.base} {self.tree}", presented)

                def refuse_push(*args: str, **kwargs: object) -> str:
                    if "push" in args:
                        self.fail("Publisher reached push with unapproved original commit metadata")
                    return self.git(*args, **kwargs)

                with patch.object(publisher, "git", side_effect=refuse_push):
                    with self.assertRaisesRegex(ValueError, reason):
                        self.publish()
                self.assertEqual(self.base, self.remote_head())

    def test_preserves_hooks_and_retries_the_same_commit(self) -> None:
        hook_dir = self.root / ".git" / "fixture-hooks"
        hook_dir.mkdir()
        hook = hook_dir / "pre-push"
        hook.write_bytes(b"#!/bin/sh\nexit 1\n")
        hook.chmod(0o755)
        with self.assertRaises(subprocess.CalledProcessError):
            self.publish()
        head = self.git("rev-parse", "HEAD")
        self.assertNotEqual(self.base, head)
        self.assertEqual(self.base, self.remote_head())
        hook.unlink()
        self.publish()
        self.assertEqual(head, self.remote_head())


if __name__ == "__main__":
    unittest.main()

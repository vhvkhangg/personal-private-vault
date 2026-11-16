"""Publish one reviewed PPV Non-RAG tree using the normal Git safeguards."""

import argparse
import os
from pathlib import Path, PurePosixPath
import subprocess
import sys
from tempfile import TemporaryDirectory


ROOT = Path(__file__).resolve().parents[1]
ORIGIN = "https://github.com/vhvkhangg/personal-private-vault.git"
MAIN = "refs/heads/main"


def git(*args: str, index: Path | None = None) -> str:
    env = os.environ.copy()
    for key in ("GIT_DIR", "GIT_COMMON_DIR", "GIT_WORK_TREE", "GIT_INDEX_FILE"):
        env.pop(key, None)
    if index is not None:
        env["GIT_INDEX_FILE"] = str(index)
    result = subprocess.run(
        ["git", "--no-replace-objects", "--literal-pathspecs", *args],
        cwd=ROOT, env=env, check=True, stdout=subprocess.PIPE,
    )
    return result.stdout.decode("utf-8", errors="surrogateescape").rstrip("\r\n")


def changed_paths(*args: str, index: Path | None = None) -> set[str]:
    return set(git("diff", "--name-only", "--no-renames", "-z", *args, index=index).split("\0")) - {""}


def require(condition: bool, message: str) -> None:
    if not condition:
        raise ValueError(message)


def coordinates(base: str, paths: list[str]) -> tuple[str, set[str]]:
    require(Path(git("rev-parse", "--show-toplevel")).resolve() == ROOT, "Wrong repository root.")
    require(git("symbolic-ref", "-q", "HEAD") == MAIN, "Expected the local main branch.")
    require(
        git("remote", "get-url", "--all", "origin") == ORIGIN
        and git("remote", "get-url", "--push", "--all", "origin") == ORIGIN,
        "Expected the single approved PPV origin for fetch and push.",
    )
    require(git("rev-parse", "--verify", "--end-of-options", f"{base}^{{commit}}") == base,
            "Use the full baseline commit SHA.")
    approved = set(paths)
    require(len(approved) == len(paths), "Supply each approved path once.")
    for path in approved:
        normalized = PurePosixPath(path)
        require(
            normalized.as_posix() == path and not normalized.is_absolute()
            and not {"..", ".git"}.intersection(part.lower() for part in normalized.parts)
            and "\\" not in path and ":" not in path and path != "."
            and not (ROOT / path).is_dir(),
            f"Expected a literal repository-relative file path: {path!r}",
        )
    return git("rev-parse", "HEAD"), approved


def candidate_tree(base: str, paths: list[str], approved: set[str]) -> str:
    with TemporaryDirectory(prefix="ppv-publish-") as directory:
        index = Path(directory) / "index"
        git("read-tree", base, index=index)
        git("add", "-A", "--", *paths, index=index)
        require(changed_paths("--cached", base, index=index) == approved, "Candidate paths differ from the grant.")
        return git("write-tree", index=index)


def require_idle_checkout(base: str) -> None:
    markers = (
        "MERGE_HEAD", "CHERRY_PICK_HEAD", "REVERT_HEAD", "REBASE_HEAD",
        "rebase-apply", "rebase-merge", "sequencer", "BISECT_START",
    )
    paths = git("rev-parse", "--path-format=absolute", *(
        argument for marker in markers for argument in ("--git-path", marker)
    ))
    for path in map(Path, paths.splitlines()):
        require(not path.exists(), f"Git operation in progress ({path.name}).")
    require(git("symbolic-ref", "-q", "HEAD") == MAIN and git("rev-parse", "HEAD") == base,
            "Local main changed during publication.")


def publish(base: str, tree: str, message: str, paths: list[str], head: str, approved: set[str]) -> None:
    remote = git("ls-remote", "--exit-code", "origin", MAIN)
    if head == base:
        require(remote == f"{base}\t{MAIN}", "Remote main advanced or differs from the grant.")
        require(candidate_tree(base, paths, approved) == tree, "Candidate differs from the independently reviewed tree.")
        require_idle_checkout(base)
        if changed_paths("--cached", base):
            require(git("write-tree") == tree, "The repository index contains changes outside the reviewed tree.")
        else:
            git("add", "-A", "--", *paths)
        require(git("write-tree") == tree, "Staged tree changed. Inspect the index before proceeding.")
        require_idle_checkout(base)
        git("commit", "-m", message)
        head = git("rev-parse", "HEAD")

    # Check the original object on both the initial push and retry. Git stores
    # the tree first, followed by all parent lines, before the author header.
    tree_header, _, headers = git("cat-file", "commit", head).partition("\n")
    parent_header, _, remaining_headers = headers.partition("\n")
    require(parent_header == f"parent {base}" and not remaining_headers.startswith("parent "),
            "Expected one commit on the approved base.")
    require(tree_header == f"tree {tree}", "Local commit differs from the independently reviewed tree.")
    require(changed_paths(base, head) == approved, "Local commit paths differ from the grant.")
    if remote != f"{head}\t{MAIN}":
        require(remote == f"{base}\t{MAIN}", "Remote main advanced or differs from the grant.")
        # The parent check above makes this a fast-forward. The explicit lease
        # makes the baseline a condition of the ref update itself.
        git("-c", "push.followTags=false", "-c", "remote.origin.mirror=false",
            "push", f"--force-with-lease={MAIN}:{base}", "--recurse-submodules=no",
            "origin", f"{head}:{MAIN}")
    require(git("ls-remote", "--exit-code", "origin", MAIN) == f"{head}\t{MAIN}", "Published SHA could not be verified.")
    print(f"Published {head}")


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--base", required=True, help="Full approved main commit SHA")
    parser.add_argument("--tree", help="Tree SHA recorded by the independent reviewer")
    parser.add_argument("--message", help="Message for the one reviewed commit")
    parser.add_argument("paths", nargs="+", help="Exact changed file paths from the grant")
    args = parser.parse_args()
    if bool(args.tree) != bool(args.message):
        parser.error("Provide both --tree and --message for publication, or neither to inspect the candidate.")
    try:
        head, approved = coordinates(args.base, args.paths)
        if args.tree:
            publish(args.base, args.tree, args.message, args.paths, head, approved)
        else:
            require(head == args.base and not changed_paths("--cached", args.base),
                    "Expected the approved baseline and an empty index for review.")
            print(candidate_tree(args.base, args.paths, approved))
    except (OSError, ValueError, subprocess.CalledProcessError) as error:
        print(f"Publication stopped: {error}", file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main())

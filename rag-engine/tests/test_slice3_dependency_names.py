"""Snapshot-only name resolution uses the accepted D17 source tree, never live paths."""

import hashlib
import uuid

import pytest

from ppv_rag.chunking.chunker import StructureFirstChunker
from ppv_rag.contracts import DocumentRootTask, PreparedDocumentField, SnapshotBinding
from ppv_rag.document_source import parse_prepared_document
from ppv_rag.parsers.obsidian import MarkdownFailure


def field(name: str, text: str, folder: str = "") -> PreparedDocumentField:
    source = str(uuid.uuid4())
    return PreparedDocumentField(
        source_key=source,
        source_version=3,
        approved_field="content",
        text=text,
        snapshot=SnapshotBinding(
            mode="RAG_SNAPSHOT",
            consent_mode="WHOLE_FILE",
            source_type="EXTERNAL_FILE",
            source_id=source,
            revision_id=str(uuid.uuid4()),
            sha256=hashlib.sha256(text.encode()).hexdigest(),
            byte_length=len(text.encode()),
            source_name=name,
            target_digests=sorted(
                hashlib.sha256(value.encode()).hexdigest()
                for value in (folder + name, folder + name[:-3])
            ),
            expected_selected_revision_id=None,
            source_version=3,
            consent_reset_version=1,
            authorization_epoch=2,
            field_policy_version=2,
        ),
    )


def root(text: str, children: list[PreparedDocumentField]) -> DocumentRootTask:
    parent = field("Parent.md", text)
    return DocumentRootTask(
        root_key="root",
        source_id=parent.source_key,
        source_version=3,
        format="MD",
        content_text=text,
        snapshot=parent.snapshot,
        approved_fields=["content"],
        transclusions={child.source_key: child for child in children},
    )


def test_folder_duplicate_basename_and_nested_alias_retain_exact_source_slices() -> None:
    leaf = field(
        "Leaf.md", "---\naliases: ['Leaf Alias: detail']\n---\n# H\n\nTiếng Việt 中文. ^block\n"
    )
    first = field("Child.md", "# S\n\n![[Leaf Alias: detail#^block|display]]\n", "one/")
    second = field("Child.md", "# S\n\nDifferent child.\n", "two/")
    task = root("![[one/Child#S]]\n\n![[two/Child.md#S]]\n", [leaf, first, second])
    parsed = parse_prepared_document(task)
    texts = {child.source_key: child.text for child in [leaf, first, second]}
    texts[task.source_id] = task.content_text or ""
    assert {element.source_identity for element in parsed.elements} == set(texts)
    for element in parsed.elements:
        loc = element.locator
        assert element.text == texts[element.source_identity][loc["char_start"] : loc["char_end"]]
        assert loc["source_key"] == element.source_identity
        assert loc["source_version"] == 3


@pytest.mark.parametrize(
    "target", ["Shared", "Child", "../one/Child", "/one/Child", "one\\Child", "missing/Child"]
)
def test_ambiguous_and_unsafe_targets_fail_without_partial_output(target: str) -> None:
    children = [
        field("Child.md", "---\naliases: [Shared]\n---\nOne.\n", "one/"),
        field("Child.md", "---\naliases: [Shared]\n---\nTwo.\n", "two/"),
    ]
    with pytest.raises(MarkdownFailure):
        parse_prepared_document(root(f"Root.\n\n![[{target}]]\n", children))


def test_alias_collision_with_a_real_authorized_name_fails_closed() -> None:
    named = field("Real.md", "Exact named source.\n")
    aliased = field("Other.md", "---\naliases: Real\n---\nDifferent source.\n")
    with pytest.raises(MarkdownFailure):
        parse_prepared_document(root("![[Real]]\n", [named, aliased]))


def test_postgres_signed_zero_keeps_source_lexemes_and_strict_indices() -> None:
    from ppv_rag.document_verification import canonical

    assert canonical({"score": -0.0}) == canonical({"score": 0.0})
    assert canonical({"locator_ref": 0.0}) != canonical({"locator_ref": 0})
    assert canonical({"locator_ref": 0.5}) != canonical({"locator_ref": 0})
    assert canonical({"source": "score: -0.0"}) != canonical({"source": "score: 0.0"})


def test_unique_input_bound_precedes_digest_allocation(monkeypatch: pytest.MonkeyPatch) -> None:
    from ppv_rag.parsers import obsidian

    def unexpected_digest(*_args: object) -> None:
        raise AssertionError("oversized source reached UTF-8 digest allocation")

    monkeypatch.setattr(obsidian.hashlib, "sha256", unexpected_digest)
    with pytest.raises(BufferError, match="supplied Markdown payloads"):
        obsidian.parse_obsidian("x" * 1_000_001, "root", 1, "content")


def test_multiple_aliases_count_one_immutable_input_and_retain_selected_ancestry() -> None:
    child = field(
        "Child.md",
        "---\naliases: [Alpha, Beta]\n---\n# Small\n\nTiny. ^block\n\n# Large\n\n"
        + "x" * 500_000
        + "\n\n![[Unapproved]]\n",
    )
    parsed = parse_prepared_document(root("![[Alpha#Small]]\n\n![[Beta#^block]]\n", [child]))
    selected = [e for e in parsed.elements if e.source_identity == child.source_key]
    assert len(selected) >= 2
    assert all("x" * 100 not in e.text for e in selected)
    assert {e.locator["dependency_edges"][-1]["anchor"] for e in selected} == {"Small", "^block"}
    chunks = StructureFirstChunker().chunk(parsed)
    assert all(chunk.content.strip() for chunk in chunks)
    for chunk in chunks:
        if chunk.source_identity == child.source_key:
            assert (
                chunk.content == child.text[chunk.locator["char_start"] : chunk.locator["char_end"]]
            )

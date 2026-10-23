"""Completion regressions use real source, hierarchy and producer output; no publication claim."""

from __future__ import annotations

import json
from pathlib import Path

import pytest

from ppv_rag.chunking.chunker import StructureFirstChunker
from ppv_rag.contracts import DocumentBatchStageRequest, DocumentRootTask, document_input_digest
from ppv_rag.parsers.obsidian import MarkdownFailure, parse_obsidian

VAULT = Path(__file__).parent / "fixtures/phase18-d17-completion-v1/HtmlVault.md"
HTML_CASES = [
    "<title><script>text</script></title>",  # HTML5 RCDATA, not a child script.
    "<div>\n\n**literal HTML body**\n\n</div>\n\n# Outside\n",
    "<p>First paragraph\n<p>Second paragraph\n",
    "<ul><li>one<li>two</ul>\n",
    "<dl><dt>Term<dd>Meaning<dt>Other<dd>Value</dl>\n",
    "<table><thead><tr><th>A<th>B<tbody><tr><td>1<td>2<tr><td>3<td>4</table>\n",
    "> <div>\n>\n> **literal** ![[not-approved]]\n>\n> </div>\n\nOutside\n",
    "- <div>\n\n    **literal**\n\n  </div>\n\nOutside\n",
    "Before <span>**literal** &amp; <b>bold</b></span> after.\r\n",
]
UNSAFE = [
    "<div>\n\n<script>throw 1</script>\n\n</div>",
    '<span onload="execute()">source</span>',
    '<a href="java&#x09;script:execute()">source</a>',
    '<span style="background:url(https://example.invalid)">source</span>',
    '<iframe src="https://example.invalid"></iframe>',
]


def parse(text):
    return parse_obsidian(text, "root", 4, "content")


def assert_spans(chunks, text):
    assert "".join(c.content for c in chunks) == text
    for chunk in chunks:
        loc = chunk.locator
        assert loc["source_key"] == "root" and loc["source_version"] == 4
        assert chunk.content == text[loc["char_start"] : loc["char_end"]]
        assert loc["line_start"] == 1 + text[: loc["char_start"]].count("\n")
        assert loc["line_end"] == 1 + text[: loc["char_end"] - 1].count("\n")
        json.dumps(chunk.structure_metadata, allow_nan=False)


@pytest.mark.parametrize("source", HTML_CASES)
def test_md17_10_html_source_and_optional_tags(source):
    doc = parse(source)
    for budget in (53, 1500):
        assert_spans(StructureFirstChunker(target_chars=budget).chunk(doc), source)
    for element in doc.elements:
        for node in element.metadata["nodes"]:
            if not node["type"].startswith("html"):
                continue
            tree = node["html_source_tree"]
            assert tree[0]["representation"] == "VERBATIM_SOURCE_CONTEXT"
            for token in tree[0]["source_tokens"]:
                assert (
                    token["raw_text"]
                    == node["text"][token["relative_start"] : token["relative_end"]]
                )
            for child in tree[1:]:
                assert child["representation"] == "DERIVED_HTML5_STRUCTURE"
                assert child["location_kind"] == "CONTAINING_HTML_SOURCE"
                assert 0 <= child["parent_id"] < child["id"]


def test_md17_10_real_vault_hierarchy_gold_and_outside_markdown():
    text = VAULT.read_bytes().decode("utf-8")
    doc = parse(text)
    assert_spans(StructureFirstChunker(target_chars=93).chunk(doc), text)
    assert any(e.heading_path == ["HTML and Markdown", "Back to Markdown"] for e in doc.elements)
    nodes = [n for e in doc.elements for n in e.metadata["nodes"]]
    html = next(
        n for n in nodes if n["type"] == "html_block" and '<div class="record">' in n["text"]
    )
    tree = html["html_source_tree"]
    by_id = {n["id"]: n for n in tree}
    assert [
        n["text"].strip()
        for n in tree
        if n["type"] == "html_text" and by_id[n["parent_id"]].get("tag") == "td"
    ] == ["Nguyễn", "42", "English", "003"]
    assert [
        n["text"].strip()
        for n in tree
        if n["type"] == "html_text" and by_id[n["parent_id"]].get("tag") == "th"
    ] == ["Name", "Count"]
    rows = [n for n in tree if n.get("tag") == "tr"]
    assert len(rows) == 3
    assert [by_id[n["parent_id"]]["tag"] for n in rows] == ["thead", "tbody", "tbody"]
    assert any(n.get("text") == "**literal Markdown** and ![[not-approved]]" for n in tree)
    assert not any(n["type"] == "obsidian_embed" for n in nodes)
    assert doc.table_count == 1  # GFM table; HTML tables are identified in the native HTML tree.


@pytest.mark.parametrize("source", UNSAFE)
def test_md17_10_active_html_rejects_whole_source(source):
    with pytest.raises(MarkdownFailure, match="MD_UNSUPPORTED_ACTIVE_HTML"):
        parse("A valid prefix.\n\n" + source)


def test_md17_12_actual_producer_html_and_all_or_nothing(monkeypatch):
    import ppv_rag.worker as worker
    from ppv_rag.config import Settings

    monkeypatch.setattr(
        worker,
        "settings",
        Settings(
            service_key="synthetic-test-service-credential-00000000",
            worker_dsn="postgresql://rag_worker@127.0.0.1:5432/vault",
        ),
    )
    monkeypatch.setattr(worker, "verify_document_grant", lambda request: None)
    monkeypatch.setattr(worker, "seal_document_result", lambda *_: "f" * 64)
    captured = []
    monkeypatch.setattr(worker, "stage_document_result", lambda **kwargs: captured.append(kwargs))
    sources = [
        *HTML_CASES,
        VAULT.read_bytes().decode("utf-8"),
        *["Valid prefix.\n\n" + s for s in UNSAFE],
    ]
    roots = [
        DocumentRootTask(
            root_key="root",
            source_id=str(i),
            source_version=4,
            format="md",
            content_text=s,
            approved_fields=["content"],
        )
        for i, s in enumerate(sources)
    ]
    request = DocumentBatchStageRequest(
        job_id="11111111-1111-1111-1111-111111111111",
        attempt_id="22222222-2222-2222-2222-222222222222",
        lease_token="a" * 64,
        roots=roots,
        input_digest=document_input_digest(roots),
    )
    result = worker.process_document_batch_request(request)
    assert (result.staged_count, result.failed_count, result.published_count) == (
        len(HTML_CASES) + 1,
        len(UNSAFE),
        0,
    )
    for source, output in zip(sources, captured, strict=True):
        if output["success"]:
            assert "".join(c["content"] for c in output["chunks_payload"]) == source
            for chunk in output["chunks_payload"]:
                loc = chunk["locator"]
                assert chunk["content"] == source[loc["char_start"] : loc["char_end"]]
                assert len(chunk["embedding"]) == 384
        else:
            assert output["reason_code"] == "MD_UNSUPPORTED_ACTIVE_HTML"
            assert output["chunks_payload"] == []


def test_md17_11_large_table_expansion_rejected_before_allocation():
    header = "|" + "|".join(["A"] * 100) + "|\n"
    separator = "|" + "|".join(["-"] * 100) + "|\n"
    body = ("|" + "|".join(["x"] * 100) + "|\n") * 300
    with pytest.raises(BufferError, match="Markdown table expansion"):
        parse(header + separator + body)


def test_md17_01_gfm_strike_and_email_structure():
    doc = parse("~single~ ~~double~~ ~~~literal~~~ a.b-c_d@a.b\n")
    nodes = [n for e in doc.elements for n in e.metadata["nodes"]]
    assert [n["markup"] for n in nodes if n["type"] == "s_open"] == ["~", "~~"]
    assert [n["attributes"]["href"] for n in nodes if n["type"] == "link_open"] == [
        "mailto:a.b-c_d@a.b"
    ]


def test_md17_03_empty_properties_are_structure_not_thematic_rules():
    for text in ("---\n---\n", "---\n# YAML comment\n---\nBody\n"):
        doc = parse(text)
        assert doc.elements[0].metadata["properties"] == {}
        assert_spans(StructureFirstChunker().chunk(doc), text)

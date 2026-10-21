"""D17 source-authored gold. These tests do not assert human adjudication or publication."""

from __future__ import annotations

import hashlib
import json
import math
import time
from pathlib import Path

import pytest

from ppv_rag.chunking.chunker import StructureFirstChunker
from ppv_rag.contracts import DocumentBatchStageRequest, DocumentRootTask, document_input_digest
from ppv_rag.limits import ParsingCancelled, parsing_scope
from ppv_rag.parsers.markdown import ApprovedDependency, parse_markdown
from ppv_rag.parsers.obsidian import MarkdownFailure, parse_obsidian

VAULT = Path(__file__).parent / "fixtures/phase18-d17-obsidian-v1"
SOURCES = {p.stem: p.read_bytes().decode("utf-8") for p in VAULT.glob("*.md")}
VERSIONS = {"Research": 3, "Child": 7, "Grand": 11}
DEPS = {
    name: ApprovedDependency(name, VERSIONS[name], "content", SOURCES[name])
    for name in ("Child", "Grand")
}


def parsed(text, dependencies=None):
    return parse_markdown(text, "root", 3, dependencies=dependencies, whole_file=True)


def assert_source(chunks, sources):
    for chunk in chunks:
        loc = chunk.locator
        source = sources[chunk.source_identity]
        assert chunk.content == source[loc["char_start"] : loc["char_end"]]
        assert loc["source_key"] == chunk.source_identity
        assert loc["source_version"] == chunk.source_version
        assert loc["line_start"] == source[: loc["char_start"]].count("\n") + 1
        assert (
            loc["line_end"] == source[: max(loc["char_start"], loc["char_end"] - 1)].count("\n") + 1
        )
        json.dumps(chunk.structure_metadata, allow_nan=False)


def test_md17_mixed_vault_exact_source_and_structural_gold():
    doc = parse_obsidian(SOURCES["Research"], "Research", 3, "content", DEPS)
    roots = [e for e in doc.elements if e.source_identity == "Research"]
    assert "".join(e.text for e in roots) == SOURCES["Research"]
    child = "".join(e.text for e in doc.elements if e.source_identity == "Child")
    assert (
        child
        == SOURCES["Child"][
            SOURCES["Child"].index("## Section") : SOURCES["Child"].index("## Outside")
        ]
    )
    grand = [e for e in doc.elements if e.source_identity == "Grand"]
    assert len(grand) == 1 and grand[0].text == "\nĐáp án: 123 và quyển sách. ^answer\n"
    assert len(grand[0].locator["dependency_edges"]) == 2
    assert grand[0].locator["dependency_edges"][-1]["parent_callouts"][0]["type"] == "note"
    properties = roots[0].metadata["properties"]
    assert properties["title"] == "Nghiên cứu 2026"
    assert properties["published"] is False and properties["count"] == 123.5
    assert properties["date"] == "2026-10-20"
    nodes = [n for e in roots for n in e.metadata["nodes"]]
    types = {n["type"] for n in nodes}
    assert {
        "heading_open",
        "table_open",
        "link_open",
        "obsidian_link",
        "obsidian_highlight",
        "obsidian_tag",
        "footnote_ref",
        "footnote_reference_open",
        "math_inline",
        "math_block",
        "fence",
        "code_block",
        "html_block",
        "s_open",
        "hardbreak",
    } <= types
    by_id = {n["id"]: n for n in nodes}
    assert any(
        n["type"] == "em_open" and by_id[n["parent_id"]]["type"] == "strong_open" for n in nodes
    )
    callouts = [c for e in roots for c in e.metadata["callouts"]]
    assert [(c["type"], c["fold"]) for c in callouts] == [
        ("warning", "COLLAPSED"),
        ("custom", "EXPANDED"),
    ]
    tasks = [n["attributes"] for n in nodes if "task_marker" in n["attributes"]]
    assert [(t["task_marker"], t["checked"]) for t in tasks] == [
        (" ", False),
        ("x", True),
        ("!", True),
    ]
    cells = [c for e in roots for c in e.metadata["cells"]]
    assert len(cells) == 16
    assert sum(c["source_presence"] == "IMPLICIT_GFM_EMPTY" for c in cells) == 1
    assert [c["raw_text"] for c in cells if c["overflow"]] == [" retained extra "]
    for cell in cells:
        if cell["source_presence"] == "ORIGINAL":
            loc = cell["locator"]
            assert cell["raw_text"] == SOURCES["Research"][loc["char_start"] : loc["char_end"]]
    for budget in (73, 1500):
        chunks = StructureFirstChunker(target_chars=budget).chunk(doc)
        assert_source(chunks, SOURCES)
        assert (
            "".join(c.content for c in chunks if c.source_identity == "Research")
            == SOURCES["Research"]
        )
        assert all(c.source_version == VERSIONS[c.source_identity] for c in chunks)


STATIC = [
    "ATX and *emphasis* **strong** ***nested*** ~~strike~~ &amp; \\*literal\\*\n",
    "Heading\n===\n\n---\n\nsoft\nbreak  \nhard\n",
    "[reference][id] and [inline](note.md#Heading) <a@example.invalid> "
    "www.example.invalid\n\n[id]: note.md 'title'\n",
    "> Quote\n>\n> - item\n>   1. child\n>      continuation\n\n- [ ] todo\n- [X] done\n",
    "| A | B |\n| :- | -: |\n| **a** | [b](note.md) |\n| x\\|y | `p\\|q` |\n",
    "> | A | B |\n> | - | - |\n> | x | y |\n",
    "- | A | B |\n  | - | - |\n  | x | y |\n",
    "---\nText: value\nlist: [a, b]\nnumber: 007\ncheck: true\ndate: 2026-10-20\n---\nbody\n",
    "[[note|alias]] [[note#Heading#Child]] [[note#^id]] text ^anchor\n",
    "> [!info]+ title\n> Body\n> > [!other]- folded\n> > retained\n",
    "Text[^id] and ^[inline *note*].\n\n[^id]: definition\n    continuation\n",
    "#tag/nested #tiếng-Việt ==marked== %% retained ![[unapproved]] %%\n",
    "`![[unapproved]]` and ``a ` b``\n\n```js\n![[unapproved]]\n```\n\n    indented\n",
    "```mermaid\nflowchart TD\nA --> B\n```\n\n$x+y$\n\n$$\nx=1\n$$\n",
    "<div class='inert'>\n**literal** ![[unapproved]]\n</div>\n\n<!-- authored -->\n",
    "Escaped \\![[unapproved]] \\[\\[literal]] \\==literal==\n",
    "%% comment\n```\ncomment content\n%%\n\nActual text.\n",
    "A\r\n\r\n| x | y |\r\n| - | - |\r\n| 1 | 2 |\r\n",
    "Before <span data-x='y'>**literal** and ![[not-approved]]</span> after <br> end.\n",
    "<details><summary>Title</summary><p>Retained **literal**.</p></details>\n",
    "==🔴Red== ==🟠Orange== ==🟡Yellow== ==🟢Green== ==🔵Blue== ==🟣Purple==\n",
    "[foo]\n\n[foo]: first\n[foo]: second\n",
    "before <span>unclosed",
    "<p>implicit closing\n<p>now supported\n",
    "<div>\n\n**literal HTML body** ![[inert]]\n\n</div>\n\n# Outside\n",
]


@pytest.mark.parametrize("text", STATIC)
def test_md17_static_parser_chunker_source_coverage(text):
    doc = parsed(text)
    chunks = StructureFirstChunker(target_chars=37).chunk(doc)
    assert_source(chunks, {"root": text})
    assert "".join(c.content for c in chunks) == text


FAILURES = [
    ("before\n\n![[missing]]\n", "MD_UNAPPROVED_EMBED"),
    ("before\n\n![[image.png]]\n", "MD_UNSUPPORTED_EMBED_TYPE"),
    ("before\n\n![alt](https://example.invalid/x.png)\n", "MD_UNSUPPORTED_EMBED_TYPE"),
    ("before\n\n![[broken\n", "MD_MALFORMED_EMBED"),
    ("---\na: 1\na: 2\n---\nbody", "MD_INVALID_PROPERTIES"),
    ("---\na: &x [1]\nb: *x\n---\n", "MD_INVALID_PROPERTIES"),
    ("---\na: [broken\n---\n", "MD_INVALID_PROPERTIES"),
    ("---\na: .nan\n---\n", "MD_INVALID_PROPERTIES"),
    ("before\n\n%% unclosed", "MD_UNCLOSED_COMMENT"),
    ("before\n\n```dataview\nLIST\n```", "MD_UNSUPPORTED_DYNAMIC_CONTENT"),
    ("before\n\n```query\ntag:#x\n```", "MD_UNSUPPORTED_DYNAMIC_CONTENT"),
    ("before `=this.file.name`", "MD_UNSUPPORTED_DYNAMIC_CONTENT"),
    ("before <% template %>", "MD_UNSUPPORTED_DYNAMIC_CONTENT"),
    ("before <script>alert(1)</script>", "MD_UNSUPPORTED_ACTIVE_HTML"),
    ("before <span onclick='x'>text</span>", "MD_UNSUPPORTED_ACTIVE_HTML"),
]


def test_md17_inert_html_hierarchy_and_markdown_note_embed():
    doc = parsed("Before <span><b>**literal**</b> and ![[inert]]</span> after.\n")
    nodes = [n for e in doc.elements for n in e.metadata["nodes"]]
    html = next(n for n in nodes if n["type"] == "html_inline")
    assert not any(n["type"] == "strong_open" for n in nodes)
    tree = html["html_source_tree"]
    span = next(n for n in tree if n.get("tag") == "span")
    bold = next(n for n in tree if n.get("tag") == "b")
    assert bold["parent_id"] == span["id"]
    assert any(n["type"] == "html_text" and n["parent_id"] == bold["id"] for n in tree)
    for token in tree[0]["source_tokens"]:
        assert token["raw_text"] == html["text"][token["relative_start"] : token["relative_end"]]
    doc = parsed(
        "![Alias](Child%20Note.md#Section)",
        {"Child Note.md": ApprovedDependency("child", 5, "content", "# Section\n\nBody.\n")},
    )
    assert (
        "".join(e.text for e in doc.elements if e.source_identity == "child")
        == "# Section\n\nBody.\n"
    )


def test_md17_http_cancel_while_actual_markdown_is_parsing(
    bound_worker, claimed_unit_input, monkeypatch
):
    import threading
    from concurrent.futures import ThreadPoolExecutor

    from conftest import HEADERS
    from fastapi.testclient import TestClient

    import ppv_rag.main as runtime
    import ppv_rag.parsers.obsidian as obsidian
    import ppv_rag.worker as worker
    from ppv_rag.config import Settings

    monkeypatch.setattr(
        worker,
        "settings",
        Settings(
            service_key=bound_worker.service_key,
            worker_dsn="postgresql://rag_worker@127.0.0.1:5432/vault",
        ),
    )
    monkeypatch.setattr(worker, "verify_document_grant", lambda request: None)
    staged = []
    monkeypatch.setattr(worker, "stage_document_result", lambda **kwargs: staged.append(kwargs))
    entered, release = threading.Event(), threading.Event()
    original = obsidian.checkpoint
    calls = 0

    def checkpoint():
        nonlocal calls
        calls += 1
        if calls == 100:
            entered.set()
            assert release.wait(10)
        original()

    monkeypatch.setattr(obsidian, "checkpoint", checkpoint)
    roots = [
        DocumentRootTask(
            root_key="large",
            source_id="large",
            format="md",
            content_text="Actual **Markdown**\n" * 10000,
            approved_fields=["content"],
        )
    ]
    request = DocumentBatchStageRequest(
        job_id=bound_worker.job_id,
        attempt_id=bound_worker.attempt_id,
        lease_token="a" * 64,
        roots=roots,
        input_digest=document_input_digest(roots),
    )
    with TestClient(runtime.app) as client, ThreadPoolExecutor(max_workers=1) as pool:
        future = pool.submit(
            client.post, "/v1/worker/execute-batch", json=request.model_dump(), headers=HEADERS
        )
        try:
            assert entered.wait(10)
            assert (
                client.post(
                    "/v1/worker/cancel", json={"job_id": request.job_id}, headers=HEADERS
                ).status_code
                == 200
            )
        finally:
            release.set()
        assert future.result(timeout=10).json()["status"] == "CANCELLED"
    assert calls == 100 and staged == []


@pytest.mark.parametrize(("text", "code"), FAILURES)
def test_md17_whole_document_failures(text, code):
    with pytest.raises(MarkdownFailure, match=code):
        parsed(text)


def test_md17_dependency_conflicts_cycles_and_anchors():
    for dependencies in (
        {
            "a": ApprovedDependency("child", 1, "content", "one"),
            "b": ApprovedDependency("child", 2, "content", "one"),
        },
        {
            "a": ApprovedDependency("child", 1, "content", "one"),
            "b": ApprovedDependency("child", 1, "content", "two"),
        },
    ):
        with pytest.raises(MarkdownFailure, match="MD_CONFLICTING_REVISION"):
            parsed("![[a]]", dependencies)
    with pytest.raises(MarkdownFailure, match="MD_CYCLIC_TRANSCLUSION"):
        parsed("![[a]]", {"a": ApprovedDependency("child", 2, "content", "![[a]]")})
    for anchor, child in (
        ("Missing", "# Exists\n"),
        ("Same", "# Same\n\n# Same\n"),
        ("^bad", "text ^good\n"),
    ):
        with pytest.raises(MarkdownFailure, match="MD_MISSING_OR_AMBIGUOUS_ANCHOR"):
            parsed(f"![[a#{anchor}]]", {"a": ApprovedDependency("child", 2, "content", child)})
    deps = {f"n{i}": ApprovedDependency(f"n{i}", 1, "content", f"![[n{i + 1}]]") for i in range(10)}
    with pytest.raises(MarkdownFailure, match="MD_TRANSCLUSION_DEPTH"):
        parsed("![[n0]]", deps)


def test_md17_real_large_parser_cancellation_and_deadline():
    text = ("Large tiếng Việt text **with structure**. " * 4000) + "\n"
    doc = parsed(text)
    chunks = StructureFirstChunker().chunk(doc)
    assert "".join(c.content for c in chunks) == text
    assert_source(chunks, {"root": text})
    calls = 0

    def cancelled():
        nonlocal calls
        calls += 1
        return calls == 100

    with pytest.raises(ParsingCancelled), parsing_scope(cancelled):
        parsed(text)
    assert calls == 100
    with pytest.raises(BufferError, match="deadline"), parsing_scope(seconds=0.001):
        parsed(text)
    with pytest.raises(BufferError, match="supplied Markdown payloads"):
        parsed("x" * 1_000_001)


def test_md17_actual_producer_matrix_and_failures(monkeypatch):
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
    staged = []
    monkeypatch.setattr(worker, "stage_document_result", lambda **kwargs: staged.append(kwargs))

    def forbidden_ocr(*args, **kwargs):
        raise AssertionError("Markdown must not initialize OCR")

    monkeypatch.setattr(worker, "AdaptiveOCREngine", forbidden_ocr)
    roots = [
        DocumentRootTask(
            root_key=f"p{i}",
            source_id=f"p{i}",
            format="md",
            content_text=text,
            approved_fields=["content"],
        )
        for i, text in enumerate(STATIC)
    ]
    roots += [
        DocumentRootTask(
            root_key=f"f{i}",
            source_id=f"f{i}",
            format="md",
            content_text=text,
            approved_fields=["content"],
        )
        for i, (text, _) in enumerate(FAILURES)
    ]
    roots.append(
        DocumentRootTask(
            root_key="Research",
            source_id="Research",
            source_version=3,
            format="md",
            content_text=SOURCES["Research"],
            approved_fields=["content"],
            transclusions={
                name: dict(
                    source_key=name,
                    source_version=VERSIONS[name],
                    approved_field="content",
                    text=SOURCES[name],
                )
                for name in DEPS
            },
        )
    )
    request = DocumentBatchStageRequest(
        job_id="11111111-1111-1111-1111-111111111111",
        attempt_id="22222222-2222-2222-2222-222222222222",
        lease_token="a" * 64,
        roots=roots,
        input_digest=document_input_digest(roots),
    )
    started = time.monotonic()
    result = worker.process_document_batch_request(request)
    assert (result.staged_count, result.failed_count) == (len(STATIC) + 1, len(FAILURES))
    for index, text in enumerate(STATIC):
        output = staged[index]["chunks_payload"]
        assert "".join(c["content"] for c in output) == text
        for chunk in output:
            loc = chunk["locator"]
            assert chunk["content"] == text[loc["char_start"] : loc["char_end"]]
            assert len(chunk["embedding"]) == 384
            assert math.isclose(sum(x * x for x in chunk["embedding"]), 1, abs_tol=1e-5)
    for index, (_, code) in enumerate(FAILURES, len(STATIC)):
        assert staged[index]["reason_code"] == code
        assert staged[index]["chunks_payload"] == []
    output = staged[-1]["chunks_payload"]
    assert (
        "".join(c["content"] for c in output if c["source_identity"] == "Research")
        == SOURCES["Research"]
    )
    for chunk in output:
        loc = chunk["locator"]
        assert loc["source_version"] == VERSIONS[chunk["source_identity"]]
        assert (
            chunk["content"]
            == SOURCES[chunk["source_identity"]][loc["char_start"] : loc["char_end"]]
        )
    assert time.monotonic() - started < 120


@pytest.mark.parametrize("mode", ["cancel", "deadline"])
def test_md17_producer_interrupt_never_stages_partial_root(monkeypatch, mode):
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
    staged = []
    monkeypatch.setattr(worker, "stage_document_result", lambda **kwargs: staged.append(kwargs))
    roots = [
        DocumentRootTask(
            root_key="large",
            source_id="large",
            format="md",
            content_text="Text **bold**.\n" * 10000,
            approved_fields=["content"],
        )
    ]
    request = DocumentBatchStageRequest(
        job_id="11111111-1111-1111-1111-111111111111",
        attempt_id="22222222-2222-2222-2222-222222222222",
        lease_token="a" * 64,
        roots=roots,
        input_digest=document_input_digest(roots),
    )
    calls = 0

    def cancelled():
        nonlocal calls
        calls += 1
        return mode == "cancel" and calls >= 100

    if mode == "deadline":
        monkeypatch.setattr(
            worker, "parsing_scope", lambda callback: parsing_scope(callback, seconds=0.001)
        )
    result = worker.process_document_batch_request(request, cancelled=cancelled)
    if mode == "cancel":
        # The unchanged low-level worker rejects; HTTP supervisor maps its cancellation event.
        assert result.status == "REJECTED" and not staged and calls == 100
    else:
        assert result.failed_count == 1 and staged[0]["reason_code"] == "RESOURCE_LIMIT_EXCEEDED"
        assert staged[0]["chunks_payload"] == []


def test_md17_fixture_hashes():
    manifest = json.loads((VAULT / "manifest.json").read_text(encoding="utf-8"))
    for record in manifest["documents"]:
        assert hashlib.sha256((VAULT / record["file"]).read_bytes()).hexdigest() == record["sha256"]


def test_md17_properties_do_not_invent_yaml11_values_or_mutate_global_loader():
    import yaml

    text = "---\ntext: on\ntext2: yes\nnumber: 012\nexp: 1e3\nclock: 1:20\ncheck: true\n---\nbody\n"
    doc = parsed(text)
    assert doc.elements[0].metadata["properties"] == {
        "text": "on",
        "text2": "yes",
        "number": 12,
        "exp": 1000.0,
        "clock": "1:20",
        "check": True,
    }
    assert yaml.safe_load("text: on")["text"] is True  # No change to legacy/global YAML policy.
    with pytest.raises(BufferError, match="properties nesting"):
        parsed("---\nx: " + "[" * 61 + "1" + "]" * 61 + "\n---\nbody\n")

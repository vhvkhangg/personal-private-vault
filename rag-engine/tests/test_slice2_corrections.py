"""Development regressions for S2-01–08; these exposed fixtures are not held-out quality."""

from __future__ import annotations

import base64
import math
import threading
import time

import numpy as np
import pytest

from ppv_rag.chunking.chunker import StructureFirstChunker
from ppv_rag.contracts import DocumentBatchStageRequest, DocumentRootTask, document_input_digest
from ppv_rag.embeddings.model import MultilingualEmbeddingPipeline
from ppv_rag.limits import ParsingCancelled, parsing_scope
from ppv_rag.parsers.markdown import ApprovedDependency, parse_markdown
from ppv_rag.parsers.models import ElementKind
from ppv_rag.parsers.text import parse_text


def test_producer_components_import_in_fresh_process_without_order_dependency() -> None:
    import os
    import subprocess
    import sys
    from pathlib import Path

    src_path = (Path(__file__).resolve().parents[1] / "src").resolve()
    result = subprocess.run(
        [sys.executable, "-c", "import ppv_rag.embeddings.model; import ppv_rag.parsers.markdown"],
        env={**os.environ, "PYTHONPATH": str(src_path)},
        capture_output=True,
        text=True,
        timeout=15,
        check=False,
    )
    assert result.returncode == 0, result.stderr


WHOLE_FILE = (
    '---\nprivate: true\nnsfw: true\ntitle: "Original title"\ncustom: value\n---\n'
    "%% Approved comment %%\n# Body\n<!-- Whole comment -->\n"
    "```html\n<!-- Literal -->\n![[inert-code-embed]]\n```\n"
)


def test_whole_file_markdown_retains_original_frontmatter_comments_and_code() -> None:
    parsed = parse_markdown(WHOLE_FILE, "parent", 5, whole_file=True)
    assert parsed.elements[0].text == WHOLE_FILE.split("%%")[0]
    assert "".join(e.text for e in parsed.elements) == WHOLE_FILE
    assert all(e.approved_field == "content" for e in parsed.elements)
    assert "Whole comment" in "\n".join(e.text for e in parsed.elements)
    code = next(e for e in parsed.elements if e.kind == ElementKind.CODE_BLOCK)
    assert code.text == "```html\n<!-- Literal -->\n![[inert-code-embed]]\n```\n"
    assert code.locator["line_start"] == 10
    # The separate field-filtered policy retains restrictive denial.
    with pytest.raises(ValueError, match="Restrictive"):
        parse_markdown(WHOLE_FILE, "domain-style-fields", 1, whole_file=False)


@pytest.mark.parametrize(
    "content", ["---\nx: 1\nx: 2\n---\nbody", "---\nx: 1\nbody", "---\na: &a [x]\nb: *a\n---\nbody"]
)
def test_whole_file_rejects_malformed_duplicate_and_alias_expansion(content: str) -> None:
    with pytest.raises(ValueError):
        parse_markdown(content, "source", 1, whole_file=True)


def test_callout_alias_block_anchor_and_complete_nested_edges() -> None:
    deps = {
        "Child": ApprovedDependency("child", 3, "content", "# H\n![[Grand#^b|Shown]]"),
        "Grand": ApprovedDependency("grand", 8, "content", "Text from grand. ^b"),
    }
    parsed = parse_markdown(
        "> [!NOTE]\n> ![[Child#H|Alias]]", "parent", 2, dependencies=deps, whole_file=True
    )
    # A child heading and its transitive body both retain their own original identities.
    assert [(e.source_identity, e.source_version) for e in parsed.elements] == [
        ("parent", 2),
        ("child", 3),
        ("child", 3),
        ("grand", 8),
    ]
    grand = parsed.elements[-1]
    assert grand.text == "Text from grand. ^b"
    assert grand.locator["char_start"] == 0 and grand.locator["char_end"] == len(grand.text)
    assert grand.locator["dependency_edges"][0]["parent_callouts"][0]["type"] == "NOTE"
    assert parsed.elements[0].text == "> [!NOTE]\n> ![[Child#H|Alias]]"
    assert (
        "".join(e.text for e in parsed.elements if e.source_identity == "child")
        == deps["Child"].text_content
    )
    assert [(edge["parent"], edge["child"]) for edge in grand.locator["dependency_edges"]] == [
        ("parent", "child"),
        ("child", "grand"),
    ]
    assert grand.locator["dependency_edges"][1]["child_version"] == 8


def test_revision_changes_and_roles_cannot_share_a_chunk() -> None:
    deps = {
        "old": ApprovedDependency("child", 1, "content", "OLD"),
        "new": ApprovedDependency("child", 2, "content", "NEW"),
    }
    parsed = parse_markdown("![[old]]\n\n![[new]]", "parent", 4, dependencies=deps)
    chunks = StructureFirstChunker().chunk(parsed)
    assert [(c.content, c.source_version) for c in chunks] == [("OLD", 1), ("NEW", 2)]
    deps["new"] = ApprovedDependency("child", 3, "content", "CHANGED")
    updated = StructureFirstChunker().chunk(
        parse_markdown("![[new]]", "parent", 4, dependencies=deps)
    )
    assert (updated[0].content, updated[0].source_version) == ("CHANGED", 3)
    for text in ("![[absent]]", "> [!NOTE]\n> ![[absent]]"):
        with pytest.raises(PermissionError):
            parse_markdown(text, "parent", 1, dependencies=deps)
    cycle = {"x": ApprovedDependency("child", 1, "content", "![[x]]")}
    with pytest.raises(ValueError, match="Cyclic"):
        parse_markdown("![[x]]", "parent", 1, dependencies=cycle)


def test_cumulative_transclusion_expansion_rejects_before_large_output() -> None:
    deps = {"x": ApprovedDependency("child", 1, "content", "a" * 80000)}
    with pytest.raises(BufferError, match="transclusion expansion"):
        parse_markdown("\n\n".join(["![[x]]"] * 32), "parent", 1, dependencies=deps)


def test_inert_multiline_comments_and_tilde_fences_do_not_resolve_children() -> None:
    text = "%% comment\n\n![[unapproved]]\n%%\n\n~~~\n![[also-inert]]\n~~~\n"
    parsed = parse_markdown(text, "source", 1, whole_file=True)
    content = "\n".join(e.text for e in parsed.elements)
    assert "![[unapproved]]" in content
    assert "![[also-inert]]" in content


def test_nested_heading_anchor_and_conflicting_same_revision_payload() -> None:
    deps = {"x": ApprovedDependency("child", 2, "content", "# Main\n## Sub\nSelected")}
    parsed = parse_markdown("![[x#main#sub|display]]", "root", 1, dependencies=deps)
    assert [e.text for e in parsed.elements] == ["Sub", "Selected"]
    deps["alias"] = ApprovedDependency("child", 2, "content", "Wrong same revision")
    with pytest.raises(ValueError, match="Conflicting child revision"):
        parse_markdown("![[x]]", "root", 1, dependencies=deps)


@pytest.mark.parametrize(
    "vector",
    [np.zeros(384), np.ones(383), np.ones((1, 384)), np.full(384, np.nan), np.full(384, np.inf)],
)
def test_embedding_rejects_invalid_actual_output(
    monkeypatch: pytest.MonkeyPatch, vector: np.ndarray
) -> None:
    pipeline = MultilingualEmbeddingPipeline(force_cpu=True)
    monkeypatch.setattr(pipeline, "_embed_onnx", lambda text: vector)
    with pytest.raises(ValueError):
        pipeline.embed_text("Invalid vector path")


def test_parser_to_embedding_on_representative_real_files_offline(
    monkeypatch: pytest.MonkeyPatch,
) -> None:
    import socket

    def deny(*args: object, **kwargs: object) -> None:
        raise AssertionError("Unexpected Python network call")

    monkeypatch.setattr(socket, "socket", deny)
    model = MultilingualEmbeddingPipeline(force_cpu=True)
    documents = [
        parse_text("Nội dung tiếng Việt; 繁體中文; 简体中文.", "domain", 1),
        parse_markdown(WHOLE_FILE, "md", 1, whole_file=True),
    ]
    for document in documents:
        chunks = StructureFirstChunker(token_count=model.token_count).chunk(document)
        assert chunks
        vectors = model.embed_batch([c.content for c in chunks])
        assert len(vectors) == len(chunks)
        assert all(
            len(v) == 384 and math.isclose(sum(x * x for x in v), 1, abs_tol=1e-5) for v in vectors
        )


def test_real_parse_cooperative_cancellation_and_deadline() -> None:
    event = threading.Event()
    count = 0

    def cancelled() -> bool:
        nonlocal count
        count += 1
        if count == 30:
            event.set()
        return event.is_set()

    with pytest.raises(ParsingCancelled), parsing_scope(cancelled):
        parse_text("line\n" * 100000, "large", 1)
    assert count == 30
    with pytest.raises(BufferError, match="deadline"), parsing_scope(seconds=0.001):
        time.sleep(0.003)
        parse_text("actual parser input", "source", 1)


def test_actual_worker_whole_file_and_malformed_encoding(monkeypatch: pytest.MonkeyPatch) -> None:
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
    monkeypatch.setattr(worker, "seal_document_result", lambda *args, **kwargs: "0" * 64)
    staged = []
    monkeypatch.setattr(worker, "stage_document_result", lambda **kwargs: staged.append(kwargs))
    roots = [
        DocumentRootTask(
            root_key="whole",
            source_id="s1",
            format="md",
            content_text=WHOLE_FILE,
            approved_fields=["content"],
        ),
        DocumentRootTask(
            root_key="malformed-md",
            source_id="s2",
            format="md",
            content_b64=base64.b64encode(b"bad\xfftext").decode(),
            approved_fields=["content"],
        ),
        DocumentRootTask(
            root_key="denied-txt",
            source_id="s3",
            format="txt",
            content_text="plain text format is denied",
            approved_fields=["content"],
        ),
    ]
    request = DocumentBatchStageRequest(
        job_id="11111111-1111-1111-1111-111111111111",
        attempt_id="22222222-2222-2222-2222-222222222222",
        lease_token="a" * 64,
        roots=roots,
        input_digest=document_input_digest(roots),
    )
    response = worker.process_document_batch_request(request)
    assert (response.staged_count, response.failed_count) == (1, 2)
    content = "\n".join(c["content"] for c in staged[0]["chunks_payload"])
    assert all(
        s in content
        for s in ("private: true", "custom: value", "Whole comment", "<!-- Literal -->")
    )
    assert staged[1]["reason_code"] == "PARSER_VALIDATION_ERROR"
    assert staged[2]["reason_code"] == "UNSUPPORTED_FORMAT"


def test_worker_domain_projection_and_cancellation_during_actual_parser(
    bound_worker, claimed_unit_input, monkeypatch: pytest.MonkeyPatch
) -> None:
    from concurrent.futures import ThreadPoolExecutor

    from conftest import HEADERS
    from fastapi.testclient import TestClient

    import ppv_rag.main as runtime
    import ppv_rag.parsers.text as text_parser
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
    monkeypatch.setattr(worker, "seal_document_result", lambda *args, **kwargs: "0" * 64)
    staged = []
    monkeypatch.setattr(worker, "stage_document_result", lambda **kwargs: staged.append(kwargs))
    roots = [
        DocumentRootTask(
            root_key="domain",
            source_id="domain",
            format="DOMAIN",
            prepared_fields=[
                {
                    "source_key": "domain",
                    "source_version": 1,
                    "approved_field": "title",
                    "text": "Java-approved title",
                }
            ],
        ),
        DocumentRootTask(
            root_key="injected",
            source_id="injected",
            format="DOMAIN",
            content_text="Unprepared text",
        ),
    ]
    request = DocumentBatchStageRequest(
        job_id=bound_worker.job_id,
        attempt_id=bound_worker.attempt_id,
        lease_token="a" * 64,
        roots=roots,
        input_digest=document_input_digest(roots),
    )
    result = worker.process_document_batch_request(request)
    assert (result.staged_count, result.failed_count) == (1, 1)
    assert staged[0]["chunks_payload"][0]["content"] == "Java-approved title"
    assert staged[1]["reason_code"] == "FIELD_NOT_APPROVED"
    staged.clear()

    entered, release = threading.Event(), threading.Event()
    original = text_parser.checkpoint
    calls = 0

    def checkpoint() -> None:
        nonlocal calls
        calls += 1
        if calls == 15:
            entered.set()
            assert release.wait(10)
        original()

    monkeypatch.setattr(text_parser, "checkpoint", checkpoint)
    roots = [
        DocumentRootTask(
            root_key="large",
            source_id="large",
            format="DOMAIN",
            prepared_fields=[
                {
                    "source_key": "large",
                    "source_version": 1,
                    "approved_field": "body",
                    "text": "Actual parsing line\n" * 10000,
                }
            ],
            approved_fields=["body"],
        )
    ]
    request = request.model_copy(
        update={"roots": roots, "input_digest": document_input_digest(roots)}
    )
    with TestClient(runtime.app) as client, ThreadPoolExecutor(max_workers=1) as pool:
        future = pool.submit(
            client.post, "/v1/worker/execute-batch", json=request.model_dump(), headers=HEADERS
        )
        try:
            assert entered.wait(10)
            cancel = client.post(
                "/v1/worker/cancel", json={"job_id": request.job_id}, headers=HEADERS
            )
            assert cancel.status_code == 200
        finally:
            release.set()
        assert future.result(timeout=10).json()["status"] == "CANCELLED"
    assert calls == 15
    assert staged == []

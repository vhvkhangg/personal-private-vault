"""D17 final corrective regressions; real producer, no publication authority."""

import hashlib
import json
from contextlib import contextmanager

import pytest

from ppv_rag.chunking.chunker import StructureFirstChunker
from ppv_rag.config import Settings
from ppv_rag.contracts import DocumentBatchStageRequest, DocumentRootTask, document_input_digest
from ppv_rag.limits import parsing_scope
from ppv_rag.parsers.markdown import ApprovedDependency
from ppv_rag.parsers.obsidian import compact_metadata, expand_metadata, parse_obsidian


@pytest.fixture
def producer(monkeypatch):
    import ppv_rag.worker as worker

    monkeypatch.setattr(
        worker,
        "settings",
        Settings(
            service_key="synthetic-test-service-credential-00000000",
            worker_dsn="postgresql://rag_worker@127.0.0.1:5432/vault",
        ),
    )
    monkeypatch.setattr(worker, "verify_document_grant", lambda _: None)
    outputs = []
    # Exercise the actual JSON serialization boundary, then inspect decoded output.
    monkeypatch.setattr(
        worker,
        "stage_document_result",
        lambda **kw: outputs.append(json.loads(json.dumps(kw, allow_nan=False))),
    )

    def run(texts, transclusions=None):
        roots = [
            DocumentRootTask(
                root_key=f"root-{i}",
                source_id=f"root-{i}",
                source_version=3,
                format="md",
                content_text=text,
                approved_fields=["content"],
                transclusions=transclusions or {},
            )
            for i, text in enumerate(texts)
        ]
        request = DocumentBatchStageRequest(
            job_id="11111111-1111-1111-1111-111111111111",
            attempt_id="22222222-2222-2222-2222-222222222222",
            lease_token="a" * 64,
            roots=roots,
            input_digest=document_input_digest(roots),
        )
        return worker.process_document_batch_request(request), outputs

    return run


@pytest.mark.parametrize(
    "source",
    [
        "<div>\n\n![[Child]]\n",
        "<div>\n\n**literal**\n",
        "<ul><li>one\n\n![[Child]]\n",
        "<textarea>\n\n*foo*\n\n_bar_\n\n</textarea>\n",
        "<title>![[Child]] <script>literal</script></title>\n",
        "<textarea/>\n\n![[Child]]\n",
    ],
)
def test_inert_html_eof_and_rcdata(source):
    doc = parse_obsidian(
        source,
        "root",
        3,
        "content",
        {"Child": ApprovedDependency("child", 7, "content", "CHILD_MATERIAL")},
    )
    assert all(e.source_identity == "root" for e in doc.elements)
    assert "".join(e.text for e in doc.elements) == source
    nodes = [n for e in doc.elements for n in e.metadata["nodes"]]
    assert not any(n["type"] in {"obsidian_embed", "strong_open", "em_open"} for n in nodes)
    assert any("html_source_tree" in n for n in nodes)


@pytest.mark.parametrize("texts", [["first root"], ["first root", "independent second root"]])
def test_scope_exit_deadline_counts(producer, monkeypatch, texts):
    import ppv_rag.limits as limits
    import ppv_rag.worker as worker

    calls = 0
    clock = [0.0]
    monkeypatch.setattr(limits.time, "monotonic", lambda: clock[0])

    @contextmanager
    def exit_deadline(cancelled):
        nonlocal calls
        calls += 1
        with parsing_scope(cancelled):
            yield
            if calls == 1:
                clock[0] += 61  # Expire precisely before the REAL context-manager exit.

    monkeypatch.setattr(worker, "parsing_scope", exit_deadline)
    response, outputs = producer(texts)
    assert (response.staged_count, response.failed_count) == (len(texts) - 1, 1)
    assert response.status == "STAGED" and response.receipt["status"] == "STAGED"
    assert not outputs[0]["success"] and outputs[0]["chunks_payload"] == []
    assert outputs[0]["reason_code"] == "RESOURCE_LIMIT_EXCEEDED"
    if len(texts) > 1:
        assert outputs[1]["success"] and outputs[1]["chunks_payload"]
    assert (
        response.receipt["output_digest"]
        == hashlib.sha256(
            f"{response.job_id}:{response.attempt_id}:{len(texts) - 1}:1".encode()
        ).hexdigest()
    )


@pytest.mark.parametrize("prefix", ["", "> - ", "1. "])
def test_split_intersections_actual_serialized_producer(producer, prefix):
    continuation = ">   " if prefix.startswith(">") else "   " if prefix else ""
    text = (
        prefix
        + "| A | B |\n"
        + continuation
        + "| - | - |\n"
        + "".join(continuation + f"| **row {i}** | [cell](note.md) |\n" for i in range(180))
    )
    response, outputs = producer([text])
    assert (response.staged_count, response.failed_count) == (1, 0)
    chunks = outputs[0]["chunks_payload"]
    assert len(chunks) >= 3
    assert "".join(c["content"] for c in chunks) == text
    for c in chunks:
        a, b = c["locator"]["char_start"], c["locator"]["char_end"]
        assert c["content"] == text[a:b]
        for m in c["structure_metadata"]["constituent_metadata"]:
            # Tree definitions carry full source context; later chunks refer to them.
            for n in expand_metadata(
                m,
                definitions={
                    m2["source_structure_id"]: m2
                    for c2 in chunks
                    for m2 in c2["structure_metadata"]["constituent_metadata"]
                    if m2.get("source_structure_definition")
                },
            )["nodes"]:
                intersection = n.get("chunk_intersection")
                if intersection is not None:
                    assert a <= intersection["char_start"] < intersection["char_end"] <= b


def test_nonoverlap_context_has_no_fabricated_intersection():
    text = "| A | B |\n| - | - |\n" + "| a | b |\n" * 180
    chunks = StructureFirstChunker(target_chars=500).chunk(
        parse_obsidian(text, "root", 3, "content")
    )
    metadata = chunks[0].structure_metadata["constituent_metadata"][0]
    disjoint = [
        n for n in metadata["nodes"] if n["locator"]["char_start"] >= chunks[0].locator["char_end"]
    ]
    assert disjoint
    assert all(n.get("chunk_intersection") is None for n in disjoint)


def test_compaction_losslessly_resolves_all_split_context():
    text = "> - | A | B |\n>   | - | - |\n" + ">   | **one** | [two](note.md) |\n" * 180
    chunks = StructureFirstChunker(target_chars=500).chunk(
        parse_obsidian(text, "root", 3, "content")
    )
    originals = [m for c in chunks for m in c.structure_metadata["constituent_metadata"]]
    encoded = json.loads(json.dumps([compact_metadata(m) for m in originals]))
    definitions = {m["source_structure_id"]: m for m in encoded if m["source_structure_definition"]}
    assert [expand_metadata(m, definitions) for m in encoded] == originals
    assert len(json.dumps(encoded)) < len(json.dumps(originals))
    for element in parse_obsidian("|A|B|\n|-|-|\n|x|y|\n", "root", 3, "content").elements:
        assert expand_metadata(compact_metadata(element.metadata)) == element.metadata


def test_exact_text_reuse_preserves_vectors_and_session():
    import numpy as np

    from ppv_rag.embeddings.model import MultilingualEmbeddingPipeline

    engine = MultilingualEmbeddingPipeline(force_cpu=True)
    session = engine._session
    samples = ["Short.", "Longer source text " * 20, "Unicode tiếng Việt 中文."] * 4
    shapes = []

    class Capture:
        def run(self, names, inputs):
            shapes.append(inputs["input_ids"].shape)
            return session.run(names, inputs)

    engine._session = Capture()
    batch = engine.embed_batch(samples)
    assert len(shapes) == 3 and all(shape[0] == 1 for shape in shapes)
    single = [engine.embed_text(s) for s in samples]
    assert np.allclose(batch, single, rtol=1e-4, atol=1e-6)
    assert engine._session.__class__ is Capture
    batch[0][0] = 99
    assert batch[3][0] != 99


def test_reused_embeddings_remain_cancellable(monkeypatch):
    from ppv_rag.embeddings.model import MultilingualEmbeddingPipeline
    from ppv_rag.limits import ParsingCancelled

    engine = MultilingualEmbeddingPipeline(force_cpu=True)
    original = engine._embed_onnx
    calls = []

    def capture(text):
        calls.append(text)
        return original(text)

    monkeypatch.setattr(engine, "_embed_onnx", capture)
    checks = 0

    def cancelled():
        nonlocal checks
        checks += 1
        return checks >= 20

    with pytest.raises(ParsingCancelled), parsing_scope(cancelled):
        engine.embed_batch(["Identical local text."] * 30)
    assert len(calls) == 1


def test_html_literal_child_never_enters_serialized_producer(producer):
    source = "<div>\n\n![[Child]]\n"
    response, outputs = producer(
        [source],
        {
            "Child": dict(
                source_key="child",
                source_version=7,
                approved_field="content",
                text="CHILD_MATERIAL",
            )
        },
    )
    assert (response.staged_count, response.failed_count) == (1, 0)
    chunks = outputs[0]["chunks_payload"]
    assert "".join(c["content"] for c in chunks) == source
    assert all(c["source_identity"] == "root-0" for c in chunks)
    assert "CHILD_MATERIAL" not in json.dumps(chunks)

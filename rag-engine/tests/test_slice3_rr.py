"""Real pinned CPU inference, source association and finite trusted replay."""

import hashlib
import json
import os
import uuid
from copy import deepcopy
from pathlib import Path

import numpy as np
import onnxruntime as ort
import pytest

from ppv_rag import document_verification as verifier
from ppv_rag.chunking.chunker import StructureFirstChunker
from ppv_rag.contracts import DocumentRootTask
from ppv_rag.document_source import document_binding, parse_prepared_document
from ppv_rag.embeddings.model import MultilingualEmbeddingPipeline
from ppv_rag.limits import ParsingCancelled, parsing_scope
from ppv_rag.parsers.obsidian import compact_metadata


@pytest.fixture(scope="module")
def real_candidate():
    text = (
        "# First\n\nTiếng Việt and English. 简体中文。繁體中文。\n\n"
        "# Second\n\nStars and galaxies.\n"
    )
    source = "11111111-1111-1111-1111-111111111111"
    root = DocumentRootTask.model_validate(
        dict(
            root_key="root",
            source_id=source,
            source_version=1,
            format="MD",
            approved_fields=["content"],
            content_text=text,
            snapshot=dict(
                mode="RAG_SNAPSHOT",
                consent_mode="WHOLE_FILE",
                source_type="EXTERNAL_FILE",
                source_id=source,
                revision_id="22222222-2222-2222-2222-222222222222",
                source_name="Source.md",
                sha256=hashlib.sha256(text.encode()).hexdigest(),
                byte_length=len(text.encode()),
                expected_selected_revision_id=None,
                source_version=1,
                consent_reset_version=1,
                authorization_epoch=1,
                field_policy_version=1,
            ),
        )
    )
    pipeline = MultilingualEmbeddingPipeline(force_cpu=True)
    chunks = StructureFirstChunker(token_count=pipeline.token_count).chunk(
        parse_prepared_document(root)
    )
    texts = [chunk.content for chunk in chunks]
    vectors = pipeline.embed_batch(texts)
    independent = MultilingualEmbeddingPipeline(force_cpu=True)
    repeated = independent.embed_batch(texts)
    delta = float(np.max(np.abs(np.asarray(vectors) - np.asarray(repeated))))
    assert delta <= verifier.VECTOR_ABS_TOL
    if directory := os.getenv("PPV_S3_EVIDENCE"):
        target = Path(directory) / f"rr-cpu-numerics-{uuid.uuid4()}.json"
        target.write_text(
            json.dumps(
                dict(
                    provider=pipeline.provider,
                    chunks=len(chunks),
                    dimensions=384,
                    independent_session_max_component_delta=delta,
                    absolute_tolerance=verifier.VECTOR_ABS_TOL,
                    relative_tolerance=verifier.VECTOR_REL_TOL,
                    artifact_hash=pipeline.artifact_hash,
                    cuda="AVAILABLE"
                    if "CUDAExecutionProvider" in ort.get_available_providers()
                    else "UNAVAILABLE",
                )
            ),
            encoding="utf8",
        )
    payload = []
    for chunk, vector in zip(chunks, vectors, strict=True):
        structure = dict(chunk.structure_metadata)
        structure["constituent_metadata"] = [
            compact_metadata(m) for m in structure.get("constituent_metadata", [])
        ]
        payload.append(
            dict(
                source_identity=chunk.source_identity,
                content=chunk.content,
                locator=chunk.locator,
                structure_metadata=structure,
                embedding=vector,
            )
        )
    stage = dict(
        root_key="root",
        success=True,
        reason_code="SUCCESS",
        chunks=payload,
        resources=dict(
            input_binding=document_binding(root), pipeline_identity=dict(provider="CPU")
        ),
    )
    return root, stage, pipeline


def test_real_independent_cpu_vectors_and_storage_rounding_are_accepted(
    real_candidate, monkeypatch
):
    root, stage, pipeline = real_candidate
    monkeypatch.setattr(verifier, "MultilingualEmbeddingPipeline", lambda **_: pipeline)
    verifier.verify([root], [deepcopy(stage)])
    rounded = deepcopy(stage)
    for chunk in rounded["chunks"]:
        chunk["embedding"] = [float(np.float32(value)) for value in chunk["embedding"]]
    verifier.verify([root], [rounded])


@pytest.mark.parametrize("fault", ["sign", "swap", "permutation", "dimension", "nan", "infinite"])
def test_trusted_vectors_reject_actual_forgery(real_candidate, monkeypatch, fault):
    root, original, pipeline = real_candidate
    monkeypatch.setattr(verifier, "MultilingualEmbeddingPipeline", lambda **_: pipeline)
    stage = deepcopy(original)
    vector = stage["chunks"][0]["embedding"]
    if fault == "sign":
        stage["chunks"][0]["embedding"] = [-value for value in vector]
    elif fault == "swap":
        other = stage["chunks"][-1]["embedding"]
        assert other != vector
        stage["chunks"][0]["embedding"], stage["chunks"][-1]["embedding"] = other, vector
    elif fault == "permutation":
        stage["chunks"][0]["embedding"] = vector[1:] + vector[:1]
    elif fault == "dimension":
        vector.pop()
    else:
        vector[0] = float("nan" if fault == "nan" else "inf")
    with pytest.raises(ValueError, match="INVALID_VECTOR|SOURCE_EMBEDDING_MISMATCH"):
        verifier.verify([root], [stage])


def test_component_tolerance_is_narrow_and_not_cosine_only(real_candidate, monkeypatch):
    root, original, pipeline = real_candidate
    monkeypatch.setattr(verifier, "MultilingualEmbeddingPipeline", lambda **_: pipeline)
    stage = deepcopy(original)
    stage["chunks"][0]["embedding"][0] += 2e-7
    verifier.verify([root], [stage])
    stage["chunks"][0]["embedding"][0] += 0.001
    with pytest.raises(ValueError, match="SOURCE_EMBEDDING_MISMATCH"):
        verifier.verify([root], [stage])


def test_cancellation_during_trusted_inference_remains_effective(real_candidate, monkeypatch):
    root, stage, pipeline = real_candidate
    cancelled = False
    original = pipeline.embed_batch

    def cancel(texts):
        nonlocal cancelled
        cancelled = True
        return original(texts)

    monkeypatch.setattr(pipeline, "embed_batch", cancel)
    monkeypatch.setattr(verifier, "MultilingualEmbeddingPipeline", lambda **_: pipeline)
    monkeypatch.setattr(
        verifier, "parsing_scope", lambda: parsing_scope(cancelled=lambda: cancelled)
    )
    with pytest.raises(ParsingCancelled):
        verifier.verify([root], [deepcopy(stage)])

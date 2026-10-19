"""Tests for multilingual embedding pipeline (AC-05)."""

import math

import pytest

from ppv_rag.embeddings.model import MultilingualEmbeddingPipeline


def test_multilingual_dimensions_and_normalization() -> None:
    pipeline = MultilingualEmbeddingPipeline()
    meta = pipeline.metadata()
    assert meta.dimensions == 384
    assert meta.artifact_hash is not None

    samples = [
        "Personal private vault local offline document indexing.",
        "Xử lý ngôn ngữ tự nhiên tiếng Việt có dấu đầy đủ và chính xác.",
        "多语言文本嵌入向量化检索测试。",
        "繁體中文與簡體中文多語言處理。",
    ]

    vectors = pipeline.embed_batch(samples)
    assert len(vectors) == 4

    for vec in vectors:
        assert len(vec) == 384
        # Check all values are finite
        assert all(math.isfinite(v) for v in vec)
        # Check L2 unit sphere normalization: norm =~ 1.0
        norm = math.sqrt(sum(v * v for v in vec))
        assert pytest.approx(norm, 1e-4) == 1.0


def test_deterministic_deduplication() -> None:
    pipeline = MultilingualEmbeddingPipeline()
    text = "Deterministic embedding test for identical inputs."
    v1 = pipeline.embed_text(text)
    v2 = pipeline.embed_text(text)
    assert v1 == v2


def test_cpu_fallback_mode() -> None:
    cpu_pipeline = MultilingualEmbeddingPipeline(force_cpu=True)
    meta = cpu_pipeline.metadata()
    assert meta.provider == "CPU"
    vec = cpu_pipeline.embed_text("Fallback CPU embedding execution.")
    assert len(vec) == 384
    norm = math.sqrt(sum(v * v for v in vec))
    assert pytest.approx(norm, 1e-4) == 1.0


def test_missing_model_files_fails_closed(tmp_path) -> None:
    with pytest.raises(FileNotFoundError):
        MultilingualEmbeddingPipeline(model_dir=tmp_path)


def test_tampered_model_hash_fails_closed(tmp_path) -> None:
    # Create fake model and tokenizer
    fake_model = tmp_path / "model.onnx"
    fake_model.write_bytes(b"tampered model content")
    fake_tok = tmp_path / "tokenizer.json"
    fake_tok.write_bytes(b"{}")

    with pytest.raises(ValueError, match="Model artifact SHA-256 mismatch"):
        MultilingualEmbeddingPipeline(model_dir=tmp_path)


def test_inference_failure_falls_back_once_to_same_cpu_artifact(monkeypatch) -> None:
    pipeline = MultilingualEmbeddingPipeline()
    original = pipeline._embed_onnx
    calls = 0

    def fail_gpu_once(text):
        nonlocal calls
        calls += 1
        if calls == 1:
            raise RuntimeError("Synthetic GPU OOM")
        return original(text)

    monkeypatch.setattr(pipeline, "provider", "CUDA")
    monkeypatch.setattr(pipeline, "_embed_onnx", fail_gpu_once)
    assert len(pipeline.embed_text("Controlled same-model CPU fallback.")) == 384
    assert calls == 2
    assert pipeline.provider == "CPU"
    assert pipeline._fallback_used is True

    def fail_cpu(text):
        raise RuntimeError("Synthetic CPU failure")

    monkeypatch.setattr(pipeline, "_embed_onnx", fail_cpu)
    with pytest.raises(RuntimeError, match="Synthetic CPU failure"):
        pipeline.embed_text("No second fallback")


def test_real_tokenizer_cannot_silently_truncate_chunks() -> None:
    from ppv_rag.chunking.chunker import StructureFirstChunker
    from ppv_rag.parsers.text import parse_text

    pipeline = MultilingualEmbeddingPipeline()
    text = "多語言文本" * 240
    assert pipeline.token_count(text) > 512
    with pytest.raises(ValueError, match="tokenizer budget"):
        pipeline.embed_text(text)
    chunks = StructureFirstChunker(token_count=pipeline.token_count).chunk(
        parse_text(text, "src", 4)
    )
    assert "".join(c.content for c in chunks) == text
    assert all(pipeline.token_count(c.content) <= 512 for c in chunks)

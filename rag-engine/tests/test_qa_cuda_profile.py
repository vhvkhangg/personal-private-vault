"""Explicit isolated CUDA runtime checks, without model construction or inference."""

from pathlib import Path

import pytest

from ppv_rag import qa_generation as qa

pytestmark = pytest.mark.skipif(not qa.CUDA_TEST, reason="Explicit isolated CUDA QA test profile")


def test_complete_cuda_runtime_is_pinned() -> None:
    lock = qa.verify_runtime()
    assert lock["packages"]["nvidia-cublas"]["version"] == "13.0.0.19"
    assert lock["backend"]["use_mmap"] is False
    assert qa.GENERATION_PROFILE == "C2_OFFLINE_CUDA_TEST_V1"
    assert qa.profile_hash() == "555ee729dc9b2636822f3b18ff5bbafe570f152a94f2cd04bdea24822ff008b6"
    assert qa._llm is None


def test_missing_cuda_dependency_is_refused(monkeypatch: pytest.MonkeyPatch) -> None:
    original = Path.is_file
    monkeypatch.setattr(
        Path, "is_file", lambda path: False if path.name == "cublas64_13.dll" else original(path)
    )
    with pytest.raises(RuntimeError, match="dependency bytes mismatch"):
        qa.verify_runtime()


def test_changed_cuda_dependency_version_is_refused(monkeypatch: pytest.MonkeyPatch) -> None:
    original = qa.importlib.metadata.version
    monkeypatch.setattr(
        qa.importlib.metadata,
        "version",
        lambda name: "mismatch" if name == "nvidia-cublas" else original(name),
    )
    with pytest.raises(RuntimeError, match="dependency version mismatch"):
        qa.verify_runtime()

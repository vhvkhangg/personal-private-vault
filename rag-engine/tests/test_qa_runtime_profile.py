"""Offline runtime/refusal checks; no model loading, candidate answer, or fixture mutation."""

from pathlib import Path

import pytest
from ppv_rag import qa_generation as qa


def test_installed_runtime_matches_immutable_manifest() -> None:
    lock = qa.verify_runtime()
    assert lock["packages"]["llama-cpp-python"]["version"] == "0.3.36"


def test_changed_dependency_version_is_refused(monkeypatch: pytest.MonkeyPatch) -> None:
    original = qa.importlib.metadata.version
    monkeypatch.setattr(
        qa.importlib.metadata,
        "version",
        lambda name: "mismatch" if name == "llama-cpp-python" else original(name),
    )
    with pytest.raises(RuntimeError, match="dependency version mismatch"):
        qa.verify_runtime()


def test_missing_native_dll_is_refused(monkeypatch: pytest.MonkeyPatch) -> None:
    original = Path.is_file
    monkeypatch.setattr(
        Path,
        "is_file",
        lambda path: False if path.name == "llama.dll" else original(path),
    )
    with pytest.raises(RuntimeError, match="dependency bytes mismatch"):
        qa.verify_runtime()


def test_corrupted_runtime_lock_is_refused(monkeypatch: pytest.MonkeyPatch) -> None:
    original = Path.read_bytes
    monkeypatch.setattr(
        Path,
        "read_bytes",
        lambda path: b"corrupt" if path.name == "qa-runtime-lock.json" else original(path),
    )
    with pytest.raises(RuntimeError, match="runtime lock mismatch"):
        qa.verify_runtime()


def test_system_headroom_is_required_before_generation(monkeypatch: pytest.MonkeyPatch) -> None:
    monkeypatch.setattr(qa, "available_system_memory", lambda: 2 * 1024**3 - 1)
    with pytest.raises(RuntimeError, match="HEADROOM_BELOW"):
        qa.require_system_headroom()
    monkeypatch.setattr(qa, "available_system_memory", lambda: 2 * 1024**3)
    qa.require_system_headroom()

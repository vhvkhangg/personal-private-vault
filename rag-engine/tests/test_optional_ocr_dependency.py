"""Optional import compatibility only; no OCR model, inference or quality acceptance."""

import builtins
import inspect
import os
import sys
from pathlib import Path
from types import ModuleType

import pytest

from ppv_rag.ocr.engine import AdaptiveOCREngine


@pytest.fixture(autouse=True)
def assert_selected_tree():
    expected = os.environ.get("PPV_TEST_EXPECTED_SOURCE")
    if expected:
        assert (
            Path(inspect.getfile(AdaptiveOCREngine)).resolve()
            == (Path(expected) / "ppv_rag/ocr/engine.py").resolve()
        )


def test_absent_optional_backend_preserves_unavailable_state(monkeypatch):
    original_import = builtins.__import__
    attempts = []

    def import_without_backend(name, *args, **kwargs):
        if name == "rapidocr_onnxruntime":
            attempts.append(name)
            raise ModuleNotFoundError("Synthetic absent optional backend", name=name)
        return original_import(name, *args, **kwargs)

    monkeypatch.setattr(builtins, "__import__", import_without_backend)
    engine = AdaptiveOCREngine()
    assert attempts == ["rapidocr_onnxruntime"]
    assert engine._engine is None
    assert engine.default_language == "en"


def test_present_optional_backend_uses_original_constructor(monkeypatch):
    module = ModuleType("rapidocr_onnxruntime")
    sentinel = object()
    calls = []

    def constructor():
        calls.append(())
        return sentinel

    module.RapidOCR = constructor
    monkeypatch.setitem(sys.modules, "rapidocr_onnxruntime", module)
    assert AdaptiveOCREngine()._engine is sentinel
    assert calls == [()]


@pytest.mark.parametrize("failure", [ImportError, RuntimeError])
def test_backend_initialization_failure_preserves_unavailable_state(monkeypatch, failure):
    module = ModuleType("rapidocr_onnxruntime")

    def broken_constructor():
        raise failure("Synthetic backend initialization failure")

    module.RapidOCR = broken_constructor
    monkeypatch.setitem(sys.modules, "rapidocr_onnxruntime", module)
    assert AdaptiveOCREngine()._engine is None

"""The selected cleaned runtime retains Markdown/DOMAIN and inert identity only."""

import importlib.metadata
import importlib.util
import tomllib
from pathlib import Path

import pytest

from ppv_rag.parsers.models import DocumentElement, ElementKind
from ppv_rag.parsers.text import parse_text

ROOT = Path(__file__).resolve().parents[1]


def test_installed_distributions_are_in_the_cleaned_lock() -> None:
    locked = {
        p["name"].lower().replace("_", "-")
        for p in tomllib.loads((ROOT / "uv.lock").read_text())["package"]
    }
    installed = {
        d.metadata["Name"].lower().replace("_", "-") for d in importlib.metadata.distributions()
    }
    assert installed <= locked
    assert (
        not {"pypdf", "pdfplumber", "python-docx", "openpyxl", "xlrd", "xlwt", "pillow"} & installed
    )


@pytest.mark.parametrize("fmt", ["DOCX", "PDF", "XLS", "XLSX"])
def test_removed_locator_adapters_fail_closed(fmt: str) -> None:
    element = DocumentElement(
        ElementKind.PARAGRAPH,
        "text",
        "source",
        1,
        "content",
        {
            "source_key": "source",
            "source_version": 1,
            "approved_field": "content",
            "line_start": 1,
            "line_end": 1,
            "char_start": 0,
            "char_end": 4,
        },
    )
    with pytest.raises(ValueError, match="Unsupported locator format"):
        element.validate_mandatory_locator(fmt)


def test_observer_primitives_survive_without_obsolete_ocr_execution() -> None:
    path = ROOT / "scripts/measure_d18_pt_a.py"
    assert "d18_pt_a_ocr_checkpoint" not in path.read_text()
    spec = importlib.util.spec_from_file_location("retired_ocr_observer", path)
    assert spec is not None and spec.loader is not None
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    assert callable(module.parents)
    assert module.kernel is not None and module.telemetry is not None
    assert not hasattr(module, "main")


def test_domain_text_helper_is_still_present() -> None:
    assert callable(parse_text)
    with pytest.raises(ModuleNotFoundError):
        __import__("ppv_rag.ocr.engine")

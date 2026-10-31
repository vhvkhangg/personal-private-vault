"""An explicitly configured missing asset must never select another local model."""

import sys
from pathlib import Path
from types import SimpleNamespace
from unittest.mock import Mock

import pytest
from ppv_rag import qa_generation as qa


@pytest.mark.parametrize("kind", ["missing_absolute", "missing_relative", "corrupt"])
def test_configured_model_path_denies_without_basename_fallback(
    kind: str, tmp_path: Path, monkeypatch: pytest.MonkeyPatch
) -> None:
    # Existing pinned candidate files are present elsewhere in this checkout.
    # Block the native constructor even if a regression selects those files.
    constructor = Mock(side_effect=AssertionError("Unexpected native model construction"))
    monkeypatch.setitem(
        sys.modules,
        "llama_cpp",
        SimpleNamespace(Llama=constructor, llama_supports_gpu_offload=lambda: True),
    )
    monkeypatch.setattr(qa, "verify_runtime", lambda: {})
    monkeypatch.setattr(qa, "require_system_headroom", lambda: None)
    monkeypatch.setattr(qa, "_llm", None)
    filename = Path(qa.C1_MODEL_PATH).name
    requested = tmp_path / "explicit" / filename
    expected_error = "artifact not found"
    if kind == "corrupt":
        requested.parent.mkdir()
        requested.write_bytes(b"synthetic corrupt model, never a loadable artifact")
        expected_error = "artifact hash mismatch"
    if kind == "missing_relative":
        monkeypatch.chdir(tmp_path)
        requested = Path("explicit") / filename
    with pytest.raises(RuntimeError, match=expected_error):
        qa.get_llm(str(requested))
    constructor.assert_not_called()

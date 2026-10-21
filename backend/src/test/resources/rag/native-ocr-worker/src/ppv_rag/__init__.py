"""Test-only import overlay; all worker implementation modules are the real source."""

import runpy
from pathlib import Path

_repository = next(
    p for p in Path(__file__).resolve().parents if (p / "rag-engine/src").is_dir()
)
__path__.append(str(_repository / "rag-engine/src/ppv_rag"))
_source = _repository / "rag-engine/src/ppv_rag/__init__.py"
__version__ = runpy.run_path(str(_source))["__version__"]

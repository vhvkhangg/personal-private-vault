"""Explicit provisioning script for paraphrase-multilingual-MiniLM-L12-v2 ONNX model and tokenizer.
Never imported or executed by worker runtime.
"""

from __future__ import annotations

import hashlib
import urllib.request
from pathlib import Path

MODEL_URL = (
    "https://huggingface.co/sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2/resolve/"
    "a2817895426db05d2c8baa424e48042c723c8ba2/onnx/model_quint8_avx2.onnx"
)
MODEL_SHA256 = "98a01d88b7de996cdea58c32ca71208c09968d143798814b2ea09d3439dc334f"
MODEL_EXPECTED_BYTES = 118453870

TOKENIZER_URL = (
    "https://huggingface.co/sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2/resolve/"
    "e62509716f15c5fd03a6fd3156a4bc5e43f83f26/tokenizer.json"
)
TOKENIZER_SHA256 = "2c3387be76557bd40970cec13153b3bbf80407865484b209e655e5e4729076b8"
TOKENIZER_EXPECTED_BYTES = 9081518


def download_and_verify(url: str, expected_sha256: str, expected_size: int, target: Path) -> None:
    target.parent.mkdir(parents=True, exist_ok=True)
    if target.exists():
        actual_sha = hashlib.sha256(target.read_bytes()).hexdigest()
        if actual_sha == expected_sha256:
            print(f"Verified existing artifact {target.name}: {actual_sha}")
            return
        print(f"Hash mismatch for existing {target.name}, re-downloading...")

    print(f"Downloading {target.name} from {url}...")
    req = urllib.request.Request(url, headers={"User-Agent": "PPV-RAG-Provisioner/1.0"})
    with urllib.request.urlopen(req, timeout=300) as response:
        data = response.read(expected_size + 1024 * 1024)

    if len(data) != expected_size:
        raise ValueError(
            f"Size mismatch for {target.name}: expected {expected_size}, got {len(data)}"
        )

    actual_sha = hashlib.sha256(data).hexdigest()
    if actual_sha != expected_sha256:
        raise ValueError(
            f"SHA-256 mismatch for {target.name}: expected {expected_sha256}, got {actual_sha}"
        )

    target.write_bytes(data)
    print(f"Successfully provisioned and verified {target.name}: {actual_sha}")


def main() -> None:
    base = Path(__file__).resolve().parents[1]
    models_dir = base / "models" / "paraphrase-multilingual-MiniLM-L12-v2"
    models_dir.mkdir(parents=True, exist_ok=True)

    model_target = models_dir / "model.onnx"
    download_and_verify(MODEL_URL, MODEL_SHA256, MODEL_EXPECTED_BYTES, model_target)

    tokenizer_target = models_dir / "tokenizer.json"
    download_and_verify(TOKENIZER_URL, TOKENIZER_SHA256, TOKENIZER_EXPECTED_BYTES, tokenizer_target)

    print("Embedding model provisioning complete.")


if __name__ == "__main__":
    main()

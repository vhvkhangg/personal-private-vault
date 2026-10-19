"""Explicit provisioning only; never imported or executed by the worker runtime."""

import hashlib
import urllib.request
from pathlib import Path

URL = (
    "https://www.modelscope.cn/models/RapidAI/RapidOCR/resolve/v3.10.0/"
    "onnx/PP-OCRv5/rec/latin_PP-OCRv5_rec_mobile.onnx"
)
SHA256 = "b20bd37c168a570f583afbc8cd7925603890efbcdc000a59e22c269d160b5f5a"
target = Path(__file__).resolve().parents[1] / "models/ocr/latin_PP-OCRv5_rec_mobile.onnx"
target.parent.mkdir(parents=True, exist_ok=True)
if target.exists():
    if hashlib.sha256(target.read_bytes()).hexdigest() != SHA256:
        raise SystemExit("Existing OCR artifact hash mismatch")
else:
    with urllib.request.urlopen(URL, timeout=60) as response:
        data = response.read(64 * 1024 * 1024 + 1)
    if len(data) > 64 * 1024 * 1024 or hashlib.sha256(data).hexdigest() != SHA256:
        raise SystemExit("OCR download size/hash verification failed")
    target.write_bytes(data)
print("Verified publisher-pinned Latin OCR artifact", SHA256)

import hashlib
import json
import sys
import time
from pathlib import Path

sys.path.insert(0, str(Path("rag-engine/src").resolve()))
from ppv_rag.ocr.engine import AdaptiveOCREngine
from ppv_rag.parsers.pdf import parse_pdf
from ppv_rag.telemetry import measure_hardware_snapshot

root = Path("rag-engine/tests/fixtures/phase18-ocr-v2")
manifest = json.loads((root / "manifest.json").read_text(encoding="utf-8"))
assert (
    hashlib.sha256((root / "manifest.json").read_bytes()).hexdigest()
    == (root / "manifest.sha256").read_text().strip()
)
engine = AdaptiveOCREngine()
results = []
before = time.perf_counter()
cpu = time.process_time()
for item in manifest["items"]:
    path = root / item["file"]
    assert hashlib.sha256(path.read_bytes()).hexdigest() == item["sha256"]
    try:
        parsed = parse_pdf(
            path.read_bytes(),
            item["file"],
            item["source_version"],
            language=item["language"],
            ocr_engine=engine,
        )
        actual = " ".join(e.text for e in parsed.elements)
        exact = actual == item["reference_text"]
        results.append(
            {
                "file": item["file"],
                "split": item["split"],
                "language": item["language"],
                "reference": item["reference_text"],
                "actual": actual,
                "text_exact": exact,
                "blocks": len(parsed.elements),
                "locators": [e.locator for e in parsed.elements],
            }
        )
    except Exception as e:
        results.append(
            {
                "file": item["file"],
                "split": item["split"],
                "language": item["language"],
                "text_exact": False,
                "error_type": type(e).__name__,
            }
        )
for item in manifest["items"]:
    assert hashlib.sha256((root / item["file"]).read_bytes()).hexdigest() == item["sha256"]
report = {
    "status": "FAIL" if any(not r["text_exact"] for r in results) else "TEXT_PROBE_ONLY",
    "not_acceptance": (
        "Human adjudication and other formats/geometry/table/publication proofs are pending. "
        "No files published by this parser diagnostic."
    ),
    "dataset_sha256": (root / "manifest.sha256").read_text().strip(),
    "documents": len(results),
    "exact_text_matches": sum(r["text_exact"] for r in results),
    "elapsed_seconds": time.perf_counter() - before,
    "worker_cpu_seconds": time.process_time() - cpu,
    "hardware_snapshot": measure_hardware_snapshot(),
    "source_unchanged": True,
    "results": results,
}
Path(f"logs/phase18-takeover/ocr-quality-{time.time_ns()}.json").write_text(
    json.dumps(report, ensure_ascii=False, indent=2), encoding="utf-8"
)
print(json.dumps({k: v for k, v in report.items() if k not in ("results",)}, ensure_ascii=True))
raise SystemExit(1 if report["status"] == "FAIL" else 0)

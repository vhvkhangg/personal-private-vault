"""D16 development-only official PaddleOCR experiment; never loaded by the worker.

Provision artifacts separately. This command denies network connects before Paddle imports,
uses explicit local models, and never reads the fresh held-out evaluation documents.
"""

from __future__ import annotations

import argparse
import hashlib
import importlib.metadata
import io
import json
import os
import platform
import socket
import sys
import threading
import time
from pathlib import Path
from typing import Any

ROOT = Path(__file__).resolve().parents[2]


def digest(path: Path) -> str:
    return hashlib.sha256(path.read_bytes()).hexdigest()


def save(path: Path, value: object) -> None:
    path.write_bytes((json.dumps(value, ensure_ascii=False, indent=2) + "\n").encode())


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--output", type=Path, required=True)
    parser.add_argument("--recognizer", choices=["small", "latin", "latin3"], required=True)
    parser.add_argument("--run-id", required=True)
    args = parser.parse_args()
    output = args.output.resolve()
    name = args.recognizer
    run_id = args.run_id
    if not run_id.replace("-", "").isalnum():
        raise ValueError("Invalid receipt identifier")
    scratch = output / f"scratch-{run_id}"
    scratch.mkdir(exist_ok=False)
    cache = scratch / "cache"
    cache.mkdir()
    os.environ.update(
        TEMP=str(scratch),
        TMP=str(scratch),
        TMPDIR=str(scratch),
        PADDLE_PDX_CACHE_HOME=str(cache),
        PADDLE_PDX_DISABLE_MODEL_SOURCE_CHECK="True",
        HF_HUB_OFFLINE="1",
        TRANSFORMERS_OFFLINE="1",
        OMP_NUM_THREADS="2",
        MKL_NUM_THREADS="2",
    )
    blocked: list[str] = []

    def deny(*_a: object, **_kw: object) -> Any:
        blocked.append("outbound_socket_attempt")
        raise RuntimeError("D16_OFFLINE_NETWORK_DENIED")

    # Retain the socket type so libraries can define subclasses during import.
    class OfflineSocket(socket.socket):
        connect = deny  # type: ignore[assignment]
        connect_ex = deny  # type: ignore[assignment]

    socket.socket = OfflineSocket
    socket.create_connection = deny
    socket.getaddrinfo = deny

    import psutil

    proc = psutil.Process()
    samples: dict[str, Any] = dict(
        sample_interval_seconds=0.02,
        peak_sampled_rss_bytes=0,
        peak_sampled_scratch_bytes=0,
        samples=0,
    )
    done = threading.Event()

    def disk_bytes() -> int:
        total = 0
        for path in scratch.rglob("*"):
            try:
                if path.is_file():
                    total += path.stat().st_size
            except FileNotFoundError:
                pass
        return total

    def sample() -> None:
        while not done.is_set():
            samples["peak_sampled_rss_bytes"] = max(
                samples["peak_sampled_rss_bytes"], proc.memory_info().rss
            )
            samples["peak_sampled_scratch_bytes"] = max(
                samples["peak_sampled_scratch_bytes"], disk_bytes()
            )
            samples["samples"] += 1
            done.wait(0.02)

    thread = threading.Thread(target=sample, daemon=True)
    thread.start()
    started, cpu = time.perf_counter(), time.process_time()
    records: list[dict[str, Any]] = []
    report: dict[str, Any] = dict(
        status="DEVELOPMENT_ONLY; HUMAN_ADJUDICATION_PENDING",
        recognizer=name,
        python=sys.version,
        platform=platform.platform(),
        production_pins_changed=False,
        fresh_held_out_used=False,
        network_guard="deny socket connect/connect_ex/create_connection/getaddrinfo before imports",
        blocked_network_attempts=blocked,
        results=records,
    )
    result_path = output / f"comparison-{run_id}.json"
    if result_path.exists():
        raise ValueError("Never overwrite a previous experiment")
    try:
        import numpy as np
        import paddle
        import pdfplumber
        import yaml
        from paddleocr import PaddleOCR

        sys.path.insert(0, str(ROOT / "rag-engine/src"))
        from ppv_rag.ocr.engine import OCRResult, OCRTextBlock
        from ppv_rag.parsers.pdf import parse_pdf

        model_records = json.loads((output / "model-provision.json").read_text())
        if name == "latin3":
            model_records.extend(json.loads((output / "model-provision-latin3.json").read_text()))
        for item in model_records:
            path = output / "models" / item["model"] / item["file"]
            if digest(path) != item["sha256"]:
                raise ValueError("Local artifact hash mismatch")
        rec = {
            "small": "PP-OCRv6_small_rec",
            "latin": "latin_PP-OCRv5_mobile_rec",
            "latin3": "latin_PP-OCRv3_mobile_rec",
        }[name]
        model_root = output / "models"
        alphabet = yaml.safe_load((model_root / rec / "inference.yml").read_text("utf-8"))[
            "PostProcess"
        ]["character_dict"]
        report["alphabet"] = dict(
            entries=len(alphabet),
            sha256=hashlib.sha256(json.dumps(alphabet, ensure_ascii=False).encode()).hexdigest(),
            vietnamese_probe_missing=[c for c in "ệểềốờđĐăắằặẫễỗỡựứửữợ" if c not in alphabet],
        )
        report["versions"] = {
            p: importlib.metadata.version(p)
            for p in ["paddleocr", "paddlex", "paddlepaddle", "numpy", "pdfplumber", "psutil"]
        }
        report["compiled_with_cuda"] = paddle.is_compiled_with_cuda()
        configuration = dict(
            text_detection_model_name="PP-OCRv6_small_det",
            text_detection_model_dir=str(model_root / "PP-OCRv6_small_det"),
            text_recognition_model_name=rec,
            text_recognition_model_dir=str(model_root / rec),
            use_doc_orientation_classify=False,
            use_doc_unwarping=False,
            use_textline_orientation=False,
            device="cpu",
            cpu_threads=2,
            enable_mkldnn=False,
            enable_hpi=False,
            text_det_limit_side_len=960,
            text_det_limit_type="max",
            text_rec_score_thresh=0.0,
            text_recognition_batch_size=1,
        )
        report["configuration"] = configuration
        engine = PaddleOCR(**configuration)
        engine.export_paddlex_config_to_yaml(str(output / f"effective-{run_id}.yaml"))

        def recognize(image: Any, width: float, height: float) -> list[dict[str, Any]]:
            values = list(engine.predict(np.asarray(image.convert("RGB"))[:, :, ::-1].copy()))
            if len(values) != 1:
                raise ValueError("Expected exactly one original page result")
            res = values[0]
            texts, scores, boxes = res["rec_texts"], res["rec_scores"], res["rec_polys"]
            if not (len(texts) == len(scores) == len(boxes)):
                raise ValueError("Missing OCR box/confidence")
            blocks = []
            for text, score, box in zip(texts, scores, boxes, strict=True):
                points = np.asarray(box, dtype=float)
                bbox = [
                    float(points[:, 0].min()) * width / image.width,
                    float(points[:, 1].min()) * height / image.height,
                    float(points[:, 0].max()) * width / image.width,
                    float(points[:, 1].max()) * height / image.height,
                ]
                if not text or not np.isfinite(bbox).all() or not np.isfinite(score):
                    raise ValueError("Invalid OCR content/geometry/confidence")
                blocks.append(dict(text=text, confidence=float(score), bbox=bbox))
            return blocks

        class DevelopmentAdapter:
            def recognize_region(
                self,
                image_bytes: bytes,
                language: str,
                page_width: float,
                page_height: float,
                **_kwargs: Any,
            ) -> OCRResult:
                from PIL import Image

                if language not in {"vi", "en"}:
                    raise ValueError("D16 script outside experiment")
                blocks = [
                    OCRTextBlock(language=language, **b)
                    for b in recognize(Image.open(io.BytesIO(image_bytes)), page_width, page_height)
                ]
                return OCRResult(
                    blocks=blocks,
                    average_confidence=sum(b.confidence for b in blocks) / len(blocks)
                    if blocks
                    else 0,
                )

        historical = ROOT / "rag-engine/tests/fixtures/phase18-ocr-v2"
        manifest = json.loads((historical / "manifest.json").read_text("utf-8"))
        cases = [
            dict(
                path=historical / item["file"],
                sha256=item["sha256"],
                script=item["language"],
                gold=[dict(text=item["reference_text"], ink_bbox=item["reference_ink_bbox"])],
                kind="single-line",
            )
            for item in manifest["items"]
            if item["language"] in {"vi", "en"}
        ]
        supplement = ROOT / "logs/phase18/s2-blocker-checkpoint-2026-10-20/development"
        labels = json.loads((supplement / "source-labels.json").read_text("utf-8"))
        cases.extend(
            dict(
                path=supplement / item["file"],
                sha256=item["sha256"],
                script=item["language"],
                gold=item["gold"],
                kind="scanned-table-heading",
            )
            for item in labels["documents"]
            if item["language"] in {"vi", "en"}
        )
        for case in cases:
            path = case["path"]
            if digest(path) != case["sha256"]:
                raise ValueError("Development source hash mismatch")
            row = {k: v for k, v in case.items() if k != "path"}
            row.update(file=path.name, stage="STARTED")
            records.append(row)
            save(result_path, report)
            before = time.perf_counter()
            try:
                with pdfplumber.open(path) as pdf:
                    page = pdf.pages[0]
                    image = page.to_image(resolution=144).original
                    raw = recognize(image, page.width, page.height)
                    row.update(raw_blocks=raw, page_size=[page.width, page.height])
                row["raw_text"] = " ".join(b["text"] for b in raw)
                row["raw_gold_exact"] = {
                    g["text"]: g["text"] in row["raw_text"] for g in case["gold"]
                }
                parsed = parse_pdf(
                    path.read_bytes(),
                    path.name,
                    3,
                    language=case["script"],
                    ocr_engine=DevelopmentAdapter(),  # type: ignore[arg-type]
                )
                row["elements"] = [
                    dict(text=e.text, kind=e.kind.value, locator=e.locator, metadata=e.metadata)
                    for e in parsed.elements
                ]
                row["parsed_text"] = " ".join(e.text for e in parsed.elements)
                row["parsed_gold_exact"] = {
                    g["text"]: g["text"] in row["parsed_text"] for g in case["gold"]
                }
                row["stage"] = "COMPLETE_DEVELOPMENT_DIAGNOSTIC"
            except Exception as exc:
                row.update(stage="FAILED", error_type=type(exc).__name__, error=str(exc))
            row["wall_seconds"] = time.perf_counter() - before
            save(result_path, report)
            print(json.dumps(row, ensure_ascii=True), flush=True)
        engine.close()
        report["artifacts_unchanged"] = all(
            digest(output / "models" / item["model"] / item["file"]) == item["sha256"]
            for item in model_records
        )
        report["script_denominators"] = {
            script: dict(
                documents=sum(c["script"] == script for c in cases),
                pages=sum(c["script"] == script for c in cases),
                gold_blocks=sum(len(c["gold"]) for c in cases if c["script"] == script),
                tables=1,
                logical_cells=5,
            )
            for script in ["vi", "en"]
        }
        report["diagnostic_complete"] = True
    except Exception as exc:
        report.update(error_type=type(exc).__name__, error=str(exc), diagnostic_complete=False)
        raise
    finally:
        done.set()
        thread.join(timeout=2)
        report.update(
            wall_seconds=time.perf_counter() - started, cpu_seconds=time.process_time() - cpu
        )
        memory = proc.memory_info()
        samples["process_os_peak_working_set_bytes"] = getattr(memory, "peak_wset", None)
        samples["scratch_final_bytes"] = disk_bytes()
        samples["surviving_children"] = [p.pid for p in proc.children(recursive=True)]
        report["resources"] = samples
        report["distributions"] = {
            d.metadata["Name"]: d.version for d in importlib.metadata.distributions()
        }
        save(result_path, report)
    # A completed comparison can still fail quality; never exit green for content loss.
    return int(
        not records
        or any(
            r["stage"] != "COMPLETE_DEVELOPMENT_DIAGNOSTIC"
            or not all(r["raw_gold_exact"].values())
            or not all(r["parsed_gold_exact"].values())
            for r in records
        )
    )


if __name__ == "__main__":
    raise SystemExit(main())

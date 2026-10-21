"""D16 development experiment only: local Tesseract/VietOCR and retained baselines.

No production imports this script. Use only exposed development PDFs, local verified artifacts,
and a unique run ID. Candidate outputs are diagnostics, never admitted production results.
"""

from __future__ import annotations

import argparse
import csv
import hashlib
import importlib.metadata
import io
import json
import os
import socket
import subprocess
import sys
import threading
import time
from pathlib import Path
from typing import Any

ROOT = Path(__file__).resolve().parents[2]


def digest(path: Path) -> str:
    return hashlib.sha256(path.read_bytes()).hexdigest()


def save(path: Path, value: object) -> None:
    path.write_bytes((json.dumps(value, indent=2, ensure_ascii=False) + "\n").encode())


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--output", type=Path, required=True)
    parser.add_argument(
        "--backend", choices=["tesseract", "vietocr", "rapid", "paddle"], required=True
    )
    parser.add_argument("--run-id", required=True)
    parser.add_argument("--psm", choices=[6, 11], type=int, default=6)
    parser.add_argument("--languages", choices=["combined", "declared"], default="combined")
    parser.add_argument("--viet-model", choices=["seq2seq", "transformer"], default="seq2seq")
    args = parser.parse_args()
    output = args.output.resolve()
    if not args.run_id.replace("-", "").isalnum():
        raise ValueError("Unsafe run ID")
    result_path = output / f"{args.run_id}-results.json"
    if result_path.exists():
        raise ValueError("Never overwrite evidence")
    scratch = output / f"scratch-{args.run_id}"
    scratch.mkdir(exist_ok=False)
    os.environ.update(
        TEMP=str(scratch),
        TMP=str(scratch),
        TMPDIR=str(scratch),
        TORCH_HOME=str(scratch / "torch"),
        PADDLE_PDX_CACHE_HOME=str(scratch / "paddlex"),
        PADDLE_PDX_DISABLE_MODEL_SOURCE_CHECK="True",
        HF_HUB_OFFLINE="1",
        OMP_NUM_THREADS="2",
        MKL_NUM_THREADS="2",
    )
    network_attempts: list[str] = []

    def deny(*_a: object, **_kw: object) -> Any:
        network_attempts.append("connect_attempt")
        raise RuntimeError("CHECKPOINT_OFFLINE_NETWORK_DENIED")

    class OfflineSocket(socket.socket):
        connect = deny  # type: ignore[assignment]
        connect_ex = deny  # type: ignore[assignment]

    socket.socket = OfflineSocket
    socket.create_connection = deny
    socket.getaddrinfo = deny
    import numpy as np
    import pdfplumber
    import psutil
    from PIL import Image

    sys.path.insert(0, str(ROOT / "rag-engine/src"))
    from ppv_rag.ocr.engine import OCRResult, OCRTextBlock
    from ppv_rag.parsers.pdf import parse_pdf

    started, cpu = time.perf_counter(), time.process_time()
    proc = psutil.Process()
    done = threading.Event()
    resources: dict[str, Any] = dict(
        requested_sample_interval_seconds=0.02,
        peak_sampled_tree_rss_bytes=0,
        peak_sampled_scratch_bytes=0,
        samples=0,
    )
    child_observations: dict[int, dict[str, Any]] = {}

    def disk_size() -> int:
        total = 0
        for p in scratch.rglob("*"):
            try:
                if p.is_file():
                    total += p.stat().st_size
            except FileNotFoundError:
                pass
        return total

    def sample() -> None:
        while not done.is_set():
            rss = proc.memory_info().rss
            for child in proc.children(recursive=True):
                try:
                    info, times = child.memory_info(), child.cpu_times()
                    rss += info.rss
                    child_observations[child.pid] = dict(
                        observed_cpu_seconds=times.user + times.system,
                        os_peak_working_set_bytes=getattr(info, "peak_wset", None),
                    )
                except psutil.NoSuchProcess:
                    pass
            resources["peak_sampled_tree_rss_bytes"] = max(
                rss, resources["peak_sampled_tree_rss_bytes"]
            )
            resources["peak_sampled_scratch_bytes"] = max(
                disk_size(), resources["peak_sampled_scratch_bytes"]
            )
            resources["samples"] += 1
            done.wait(0.02)

    sampler = threading.Thread(target=sample, daemon=True)
    sampler.start()
    rows: list[dict[str, Any]] = []
    report: dict[str, Any] = dict(
        status="DEVELOPMENT_ONLY; HUMAN_PENDING",
        backend=args.backend,
        python=sys.version,
        source_sha256=digest(ROOT / "rag-engine/src/ppv_rag/parsers/pdf.py"),
        frozen_held_out_used=False,
        network_attempts=network_attempts,
        results=rows,
    )
    try:
        # Provisioning and inference are separate. Revalidate every local candidate artifact.
        artifact_records = json.loads((output / "candidate-provision.json").read_text("utf-8"))
        if args.backend == "vietocr" and args.viet_model == "transformer":
            artifact_records.extend(
                json.loads((output / "candidate-provision-transformer.json").read_text("utf-8"))
            )
        for record in artifact_records:
            if digest(ROOT / record["path"]) != record["sha256"]:
                raise ValueError("Local candidate artifact changed")
        if args.backend == "tesseract":
            exe = output / "native/ucrt64/bin/tesseract.exe"
            report["configuration"] = dict(
                version=subprocess.check_output([str(exe), "--version"], text=True),
                exe_sha256=digest(exe),
                language="vie+eng",
                language_dispatch=args.languages,
                oem=1,
                psm=args.psm,
                dpi=144,
                tessdata=str(output / "models/tessdata_best"),
                threads=2,
            )

            def recognize(image: Any, language: str) -> list[dict[str, Any]]:
                encoded = io.BytesIO()
                image.save(encoded, "PNG")
                command = [
                    str(exe),
                    "stdin",
                    "stdout",
                    "--tessdata-dir",
                    str(output / "models/tessdata_best"),
                    "-l",
                    "vie+eng"
                    if args.languages == "combined"
                    else {"en": "eng", "vi": "vie"}[language],
                    "--oem",
                    "1",
                    "--psm",
                    str(args.psm),
                    "--dpi",
                    "144",
                    "-c",
                    "tessedit_create_tsv=1",
                ]
                child = subprocess.Popen(
                    command, stdin=subprocess.PIPE, stdout=subprocess.PIPE, stderr=subprocess.PIPE
                )
                try:
                    stdout, stderr = child.communicate(encoded.getvalue(), timeout=30)
                except subprocess.TimeoutExpired:
                    subprocess.run(
                        ["taskkill", "/PID", str(child.pid), "/T", "/F"],
                        capture_output=True,
                        timeout=10,
                        check=False,
                    )
                    child.wait(timeout=10)
                    raise
                if child.returncode or len(stdout) > 4 * 1024 * 1024:
                    raise ValueError(f"Tesseract failed {child.returncode}: {stderr[:300]!r}")
                if not stdout.startswith(b"level\tpage_num\t"):
                    raise ValueError(f"Tesseract did not emit TSV: {stderr[:300]!r}")
                lines: dict[tuple[str, ...], list[dict[str, Any]]] = {}
                for word in csv.DictReader(io.StringIO(stdout.decode("utf-8")), delimiter="\t"):
                    if word["level"] != "5" or not word["text"].strip():
                        continue
                    key = tuple(word[k] for k in ["page_num", "block_num", "par_num", "line_num"])
                    lines.setdefault(key, []).append(word)
                blocks = []
                for words in lines.values():
                    blocks.append(
                        dict(
                            text=" ".join(w["text"] for w in words),
                            confidence=sum(float(w["conf"]) for w in words) / (100 * len(words)),
                            bbox=[
                                min(int(w["left"]) for w in words),
                                min(int(w["top"]) for w in words),
                                max(int(w["left"]) + int(w["width"]) for w in words),
                                max(int(w["top"]) + int(w["height"]) for w in words),
                            ],
                        )
                    )
                return blocks

        elif args.backend == "vietocr":
            import torch
            import yaml
            from rapidocr_onnxruntime import RapidOCR
            from vietocr.tool.predictor import Predictor

            torch.set_num_threads(2)
            torch.set_num_interop_threads(1)
            model_dir = output / "models/vietocr"
            config = yaml.safe_load((model_dir / "config-base.yml").read_text("utf-8"))
            specific = yaml.safe_load(
                (model_dir / f"config-vgg-{args.viet_model}.yml").read_text("utf-8")
            )
            # Match upstream Cfg: architecture dictionaries replace the base defaults.
            config.update(specific)
            config["device"] = "cpu"
            config["weights"] = str(model_dir / f"vgg_{args.viet_model}.pth")
            config["cnn"]["pretrained"] = False  # Full verified checkpoint supplies CNN weights.
            config["predictor"]["beamsearch"] = False
            save(output / f"{args.run_id}-configuration.json", config)
            report["configuration"] = config
            recognizer = Predictor(config)
            detector = RapidOCR(intra_op_num_threads=2, inter_op_num_threads=1)
            report["torch"] = dict(
                version=torch.__version__, cuda_available=torch.cuda.is_available()
            )

            def recognize(image: Any, language: str) -> list[dict[str, Any]]:
                polygons, _ = detector(
                    np.asarray(image.convert("RGB")), use_cls=False, use_rec=False
                )
                blocks = []
                for polygon in [] if polygons is None else polygons:
                    points = np.asarray(polygon)
                    bbox = [
                        max(0, int(points[:, 0].min())),
                        max(0, int(points[:, 1].min())),
                        min(image.width, int(points[:, 0].max())),
                        min(image.height, int(points[:, 1].max())),
                    ]
                    text, confidence = recognizer.predict(
                        image.crop(bbox).convert("RGB"), return_prob=True
                    )
                    blocks.append(dict(text=text, confidence=float(confidence), bbox=bbox))
                return blocks

        elif args.backend == "rapid":
            sys.path.insert(0, str(ROOT / "rag-engine/scripts"))
            from slice2_ocr_checkpoint import DevelopmentCandidate

            candidate = DevelopmentCandidate(
                ROOT / "logs/phase18/s2-blocker-checkpoint-2026-10-20/candidates", "small"
            )
            report["configuration"] = candidate.configuration

            def recognize(image: Any, language: str) -> list[dict[str, Any]]:
                encoded = io.BytesIO()
                image.save(encoded, "PNG")
                result = candidate.recognize_region(
                    encoded.getvalue(), "en", page_width=image.width, page_height=image.height
                )
                return [
                    dict(text=b.text, confidence=b.confidence, bbox=b.bbox) for b in result.blocks
                ]

        else:
            from paddleocr import PaddleOCR

            previous = ROOT / "logs/phase18/s2-d16-paddleocr-2026-10-20"
            config = json.loads((previous / "comparison-small2.json").read_text("utf-8"))[
                "configuration"
            ]
            report["configuration"] = config
            engine = PaddleOCR(**config)

            def recognize(image: Any, language: str) -> list[dict[str, Any]]:
                results = list(engine.predict(np.asarray(image.convert("RGB"))[:, :, ::-1].copy()))
                if len(results) != 1:
                    raise ValueError("Unexpected Paddle page result")
                res = results[0]
                blocks = []
                for text, score, polygon in zip(
                    res["rec_texts"], res["rec_scores"], res["rec_polys"], strict=True
                ):
                    p = np.asarray(polygon)
                    blocks.append(
                        dict(
                            text=text,
                            confidence=float(score),
                            bbox=[
                                float(p[:, 0].min()),
                                float(p[:, 1].min()),
                                float(p[:, 0].max()),
                                float(p[:, 1].max()),
                            ],
                        )
                    )
                return blocks

        class Adapter:
            def recognize_region(
                self,
                image_bytes: bytes,
                language: str,
                page_width: float,
                page_height: float,
                **_kwargs: Any,
            ) -> OCRResult:
                if language not in {"vi", "en"}:
                    raise ValueError("Script outside D16 experiment")
                image = Image.open(io.BytesIO(image_bytes)).convert("RGB")
                blocks = []
                for block in recognize(image, language):
                    x0, y0, x1, y1 = block["bbox"]
                    if not (0 <= x0 < x1 <= image.width and 0 <= y0 < y1 <= image.height):
                        raise ValueError("Invalid measured OCR geometry")
                    if not np.isfinite(block["confidence"]) or not block["text"].strip():
                        raise ValueError("Invalid OCR content/confidence")
                    blocks.append(
                        OCRTextBlock(
                            text=block["text"],
                            language=language,
                            confidence=block["confidence"],
                            bbox=[
                                x0 * page_width / image.width,
                                y0 * page_height / image.height,
                                x1 * page_width / image.width,
                                y1 * page_height / image.height,
                            ],
                        )
                    )
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
                path=historical / c["file"],
                sha256=c["sha256"],
                script=c["language"],
                gold=[dict(text=c["reference_text"], ink_bbox=c["reference_ink_bbox"])],
            )
            for c in manifest["items"]
            if c["language"] in {"vi", "en"}
        ]
        supplement = ROOT / "logs/phase18/s2-blocker-checkpoint-2026-10-20/development"
        source = json.loads((supplement / "source-labels.json").read_text("utf-8"))
        cases.extend(
            dict(
                path=supplement / c["file"],
                sha256=c["sha256"],
                script=c["language"],
                gold=c["gold"],
            )
            for c in source["documents"]
            if c["language"] in {"vi", "en"}
        )
        for c in cases:
            path = c["path"]
            if digest(path) != c["sha256"]:
                raise ValueError("Exposed development source changed")
            row = dict(
                file=path.name,
                source_sha256=c["sha256"],
                script=c["script"],
                gold=c["gold"],
                stage="STARTED",
            )
            rows.append(row)
            save(result_path, report)
            before = time.perf_counter()
            try:
                with pdfplumber.open(path) as pdf:
                    page = pdf.pages[0]
                    image = page.to_image(resolution=144).original
                    row["raw_blocks"] = recognize(image, c["script"])
                    row["raw_raster_size"] = [image.width, image.height]
                    row["page_size"] = [page.width, page.height]
                row["raw_text"] = " ".join(b["text"] for b in row["raw_blocks"])
                parsed = parse_pdf(
                    path.read_bytes(), path.name, 3, language=c["script"], ocr_engine=Adapter()
                )  # type: ignore[arg-type]
                row["elements"] = [
                    dict(text=e.text, kind=e.kind.value, locator=e.locator, metadata=e.metadata)
                    for e in parsed.elements
                ]
                row["parsed_text"] = " ".join(e.text for e in parsed.elements)
                row["raw_gold_exact"] = {g["text"]: g["text"] in row["raw_text"] for g in c["gold"]}
                row["parsed_gold_exact"] = {
                    g["text"]: g["text"] in row["parsed_text"] for g in c["gold"]
                }
                row["stage"] = "COMPLETE_DEVELOPMENT_DIAGNOSTIC"
            except Exception as exc:
                row.update(stage="FAILED", error_type=type(exc).__name__, error=str(exc))
            row["wall_seconds"] = time.perf_counter() - before
            save(result_path, report)
            print(json.dumps(row, ensure_ascii=True), flush=True)
        report["diagnostic_complete"] = True
        report["candidate_artifacts_unchanged"] = all(
            digest(ROOT / r["path"]) == r["sha256"] for r in artifact_records
        )
    except Exception as exc:
        report.update(error_type=type(exc).__name__, error=str(exc), diagnostic_complete=False)
        raise
    finally:
        done.set()
        sampler.join(timeout=2)
        resources.update(
            root_os_peak_working_set_bytes=getattr(proc.memory_info(), "peak_wset", None),
            child_observations=child_observations,
            final_scratch_bytes=disk_size(),
            surviving_children=[p.pid for p in proc.children(recursive=True)],
        )
        report.update(
            wall_seconds=time.perf_counter() - started,
            root_cpu_seconds=time.process_time() - cpu,
            resources=resources,
            distributions={
                d.metadata["Name"]: d.version for d in importlib.metadata.distributions()
            },
        )
        save(result_path, report)
    return int(
        not rows
        or any(
            r["stage"] != "COMPLETE_DEVELOPMENT_DIAGNOSTIC"
            or not all(r["parsed_gold_exact"].values())
            for r in rows
        )
    )


if __name__ == "__main__":
    raise SystemExit(main())

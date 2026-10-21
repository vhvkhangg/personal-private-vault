"""Explicit development-only PP-OCRv6 provisioning/comparison; never imported by the worker.

Run in the isolated checkpoint environment. Production pins and admission remain unchanged.
"""

from __future__ import annotations

import argparse
import hashlib
import importlib.metadata
import io
import json
import site
import socket
import sys
import time
import urllib.request
from dataclasses import asdict, replace
from pathlib import Path
from typing import Any

ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / "rag-engine/src"))
# Read-only access to existing parser/ORT dependencies; candidate packages take precedence.
site.addsitedir(str(ROOT / "rag-engine/.venv/Lib/site-packages"))

from PIL import Image  # noqa: E402

from ppv_rag.ocr.engine import AdaptiveOCREngine, OCRResult  # noqa: E402
from ppv_rag.parsers.pdf import parse_pdf  # noqa: E402
from ppv_rag.telemetry import measure_hardware_snapshot  # noqa: E402


def digest(path: Path) -> str:
    return hashlib.sha256(path.read_bytes()).hexdigest()


def write(path: Path, value: object) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def provision(output: Path) -> None:
    import rapidocr  # type: ignore[import-not-found]
    import yaml

    package = Path(rapidocr.__file__).parent
    registry_path = package / "default_models.yaml"
    registry = yaml.safe_load(registry_path.read_text(encoding="utf-8"))
    records: list[dict[str, Any]] = []
    for size in ("small", "medium"):
        for task in ("det", "rec"):
            entry = registry["onnxruntime"]["PP-OCRv6"][task][f"multi_PP-OCRv6_{task}_{size}"]
            filename = f"PP-OCRv6_{task}_{size}.onnx"
            target = output / "models" / filename
            if not target.exists():
                target.parent.mkdir(parents=True, exist_ok=True)
                bundled = package / "models" / filename
                if bundled.exists():
                    target.write_bytes(bundled.read_bytes())
                else:
                    # Development provisioning only: bounded body and no overwrite/adoption.
                    temp = target.with_suffix(".download")
                    with urllib.request.urlopen(entry["model_dir"], timeout=45) as response:
                        with temp.open("xb") as stream:
                            total = 0
                            while data := response.read(1024 * 1024):
                                total += len(data)
                                if total > 150 * 1024 * 1024:
                                    raise ValueError("Candidate download exceeds finite limit")
                                stream.write(data)
                    if digest(temp) != entry["SHA256"]:
                        raise ValueError("Official candidate hash mismatch")
                    temp.rename(target)
            if digest(target) != entry["SHA256"]:
                raise ValueError("Candidate artifact hash mismatch")
            records.append(
                dict(
                    size=size,
                    task=task,
                    path=str(target.resolve()),
                    bytes=target.stat().st_size,
                    sha256=digest(target),
                    upstream=entry["model_dir"],
                )
            )
    license_dir = next(package.parent.glob("rapidocr-*.dist-info/licenses"))
    licenses = {}
    for name in ("LICENSE", "MODEL_LICENSES.md"):
        contents = (license_dir / name).read_text(encoding="utf-8")
        (output / name).write_text(contents, encoding="utf-8")
        licenses[name] = digest(output / name)
    write(
        output / "provision.json",
        dict(
            purpose="DEVELOPMENT ONLY; no production admission",
            python=sys.version,
            rapidocr=importlib.metadata.version("rapidocr"),
            registry_sha256=digest(registry_path),
            licenses=licenses,
            models=records,
            distributions={
                d.metadata["Name"]: d.version for d in importlib.metadata.distributions()
            },
        ),
    )
    print(json.dumps(dict(provisioned=len(records), output=str(output))))


def provision_latin(output: Path) -> None:
    """One finite alternative recognizer experiment; preserve the v6 evidence."""
    import rapidocr  # type: ignore[import-not-found]
    import yaml

    package = Path(rapidocr.__file__).parent
    registry = yaml.safe_load((package / "default_models.yaml").read_text(encoding="utf-8"))
    entry = registry["onnxruntime"]["PP-OCRv5"]["rec"]["latin_PP-OCRv5_rec_mobile"]
    target = output / "models/latin_PP-OCRv5_rec_mobile.onnx"
    if not target.exists():
        temp = target.with_suffix(".download")
        with urllib.request.urlopen(entry["model_dir"], timeout=45) as response:
            with temp.open("xb") as stream:
                total = 0
                while data := response.read(1024 * 1024):
                    total += len(data)
                    if total > 32 * 1024 * 1024:
                        raise ValueError("Latin candidate exceeds download limit")
                    stream.write(data)
        if digest(temp) != entry["SHA256"]:
            raise ValueError("Latin model hash mismatch")
        temp.rename(target)
    if digest(target) != entry["SHA256"]:
        raise ValueError("Latin model hash mismatch")
    write(
        output / "provision-latin.json",
        dict(
            upstream=entry["model_dir"],
            sha256=digest(target),
            bytes=target.stat().st_size,
            path=str(target.resolve()),
        ),
    )
    print(json.dumps(dict(provisioned="latin", bytes=target.stat().st_size)))


class DevelopmentCandidate(AdaptiveOCREngine):
    """Adapt candidate output to the existing PDF/box/retry path for development only."""

    def __init__(self, output: Path, size: str) -> None:
        self.output, self.size = output, size
        super().__init__()

    def _init_engine(self) -> None:
        from rapidocr import LangRec, ModelType, OCRVersion, RapidOCR  # type: ignore[import-not-found]

        provisioned = json.loads((self.output / "provision.json").read_text(encoding="utf-8"))
        paths = {}
        for entry in provisioned["models"]:
            if entry["size"] == ("small" if self.size == "latin" else self.size):
                path = Path(entry["path"])
                if digest(path) != entry["sha256"]:
                    raise ValueError("Candidate artifact mismatch before inference")
                paths[entry["task"]] = str(path)
        if self.size == "latin":
            latin = json.loads((self.output / "provision-latin.json").read_text(encoding="utf-8"))
            if digest(Path(latin["path"])) != latin["sha256"]:
                raise ValueError("Latin candidate artifact mismatch")
            paths["rec"] = latin["path"]
        params = {
            "Global.use_cls": False,
            "Global.log_level": "error",
            "Det.model_path": paths["det"],
            "Rec.model_path": paths["rec"],
            "Det.model_type": self.size,
            "Rec.model_type": self.size,
            "EngineConfig.onnxruntime.intra_op_num_threads": 2,
            "EngineConfig.onnxruntime.inter_op_num_threads": 1,
            "EngineConfig.onnxruntime.use_cuda": False,
        }
        self.configuration = params
        runtime_params = dict(params)
        runtime_params["Det.model_type"] = (
            ModelType.SMALL if self.size == "latin" else ModelType(self.size)
        )
        runtime_params["Rec.model_type"] = (
            ModelType.MOBILE if self.size == "latin" else ModelType(self.size)
        )
        if self.size == "latin":
            runtime_params["Rec.ocr_version"] = OCRVersion.PPOCRV5
            runtime_params["Rec.lang_type"] = LangRec.LATIN
            self.configuration.update(
                {
                    "Det.model_type": "small",
                    "Rec.model_type": "mobile",
                    "Rec.ocr_version": "PP-OCRv5",
                    "Rec.lang_type": "latin",
                }
            )
        engine = RapidOCR(params=runtime_params)

        def adapted(image: Image.Image) -> tuple[list[Any], object]:
            result = engine(image)
            if result.boxes is None or result.txts is None or result.scores is None:
                return [], None
            return list(zip(result.boxes, result.txts, result.scores, strict=True)), None

        self._engine = adapted

    def recognize_region(
        self, image_bytes: bytes, language: str | None = None, **kwargs: Any
    ) -> OCRResult:
        # Explicit candidate-only admission; pinned production still denies vi/zh-Hant.
        if language not in self.SUPPORTED_LANGUAGES:
            raise ValueError("Unsupported candidate script")
        result = super().recognize_region(image_bytes, language="en", **kwargs)
        return replace(result, blocks=[replace(b, language=language) for b in result.blocks])


def compare(output: Path, size: str) -> None:
    import onnxruntime as ort
    import pypdfium2

    attempts: list[dict[str, Any]] = []

    def deny(*_args: object, **_kwargs: object) -> None:
        raise RuntimeError("CHECKPOINT_NETWORK_DENIED")

    # No further network is permitted during initialization/inference.
    socket.socket = deny  # type: ignore[assignment]
    started, cpu = time.perf_counter(), time.process_time()
    engine = DevelopmentCandidate(output, size)
    manifest_path = ROOT / "rag-engine/tests/fixtures/phase18-ocr-v2/manifest.json"
    manifest = json.loads(manifest_path.read_text(encoding="utf-8"))
    for item in manifest["items"]:
        if size == "latin" and item["language"] != "vi":
            continue
        # Both historical splits are exposed development here; never call these fresh held-out.
        path = manifest_path.parent / item["file"]
        if digest(path) != item["sha256"]:
            raise ValueError("Historical source changed")
        before = time.perf_counter()
        record = dict(file=item["file"], script=item["language"], reference=item["reference_text"])
        try:
            pdf = pypdfium2.PdfDocument(path.read_bytes())
            bitmap = pdf[0].render(scale=2)
            image = bitmap.to_pil()
            buf = io.BytesIO()
            image.save(buf, "PNG")
            result = engine.recognize_region(
                buf.getvalue(), item["language"], page_width=image.width, page_height=image.height
            )
            parsed = parse_pdf(
                path.read_bytes(), item["file"], 3, language=item["language"], ocr_engine=engine
            )
            actual = " ".join(e.text for e in parsed.elements)
            record.update(
                actual=actual,
                exact=actual == item["reference_text"],
                blocks=[asdict(b) for b in result.blocks],
                locators=[e.locator for e in parsed.elements],
                file_result="PARSED_DEVELOPMENT_ONLY",
            )
            pdf.close()
        except Exception as error:
            record.update(
                exact=False, file_result="FAILED", error_type=type(error).__name__, error=str(error)
            )
        record["wall_seconds"] = time.perf_counter() - before
        attempts.append(record)
        print(json.dumps(record, ensure_ascii=True), flush=True)
    report = dict(
        status="DEVELOPMENT_COMPARISON; HUMAN_ADJUDICATION_PENDING",
        size=size,
        pipeline_configuration=engine.configuration,
        script_denominators={
            s: sum(r["script"] == s for r in attempts) for s in engine.SUPPORTED_LANGUAGES
        },
        exact=sum(r["exact"] for r in attempts),
        attempted=len(attempts),
        wall_seconds=time.perf_counter() - started,
        process_cpu_seconds=time.process_time() - cpu,
        hardware=measure_hardware_snapshot(),
        providers=ort.get_available_providers(),
        python=sys.version,
        offline_socket_guard=True,
        production_changed=False,
        fresh_held_out_run=False,
        attempts=attempts,
    )
    write(output / f"comparison-{size}.json", report)
    print(json.dumps({k: v for k, v in report.items() if k != "attempts"}, ensure_ascii=True))


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("action", choices=("provision", "provision-latin", "compare"))
    parser.add_argument("--output", type=Path, required=True)
    parser.add_argument("--size", choices=("small", "medium", "latin"), default="small")
    args = parser.parse_args()
    if args.action == "provision":
        provision(args.output)
    elif args.action == "provision-latin":
        provision_latin(args.output)
    else:
        compare(args.output, args.size)


if __name__ == "__main__":
    main()

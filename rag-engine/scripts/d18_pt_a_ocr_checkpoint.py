"""Finite, opt-in Tesseract/ruled-table feasibility diagnostic; never worker-imported.

Freeze synthetic groups before provisioning/inference. No production OCR selection,
runtime downloads, publication, engine switching, or automatic tuning.
"""

from __future__ import annotations

import argparse
import csv
import ctypes as ct
import hashlib
import io
import json
import os
import platform
import time
import urllib.request
import zipfile
from pathlib import Path
from typing import Any

from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parents[2]
EVIDENCE = ROOT / "logs/phase18-d18-pt-a"
FIXTURES = ROOT / "rag-engine/tests/fixtures/d18-pt-a"
ARTIFACTS = EVIDENCE / "tesseract-5.2.0"
CONFIG = {
    "candidate": "Tesseract 5.2.0 C API + tessdata_best vie; OpenCV ruled-cell geometry",
    "oem": 1,
    "psm": 7,
    "language": "vie",
    "render_dpi": 144,
    "retry": 0,
    "grid_kernel_fraction": 0.08,
    "material_error": "Any exact text/cell/span/order error fails this diagnostic file",
    "geometry_tolerance_points": 2.0,
    "production_integration": False,
}


def sha(data: bytes) -> str:
    return hashlib.sha256(data).hexdigest()


def write_json(path: Path, data: Any) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def make_pdf(image: Image.Image, mixed: bool) -> bytes:
    encoded = io.BytesIO()
    image.save(encoded, "JPEG", quality=98, subsampling=0)
    jpeg = encoded.getvalue()
    # Raster at (30,160)-(570,430), TOP_LEFT, on original 600x800 PDF page.
    content = b"q 540 0 0 270 30 370 cm /Im0 Do Q\n"
    if mixed:
        content += b"BT /F1 18 Tf 30 700 Td (Transit schedule) Tj ET\n"
    objects = [
        b"<< /Type /Catalog /Pages 2 0 R >>",
        b"<< /Type /Pages /Kids [3 0 R] /Count 1 >>",
        b"<< /Type /Page /Parent 2 0 R /MediaBox [0 0 600 800] /Resources "
        b"<< /XObject << /Im0 4 0 R >> /Font << /F1 6 0 R >> >> /Contents 5 0 R >>",
        (
            f"<< /Type /XObject /Subtype /Image /Width {image.width} /Height {image.height} "
            f"/ColorSpace /DeviceRGB /BitsPerComponent 8 /Filter /DCTDecode "
            f"/Length {len(jpeg)} >>\nstream\n"
        ).encode()
        + jpeg
        + b"\nendstream",
        f"<< /Length {len(content)} >>\nstream\n".encode() + content + b"endstream",
        b"<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>",
    ]
    pdf = bytearray(b"%PDF-1.4\n")
    offsets = [0]
    for i, obj in enumerate(objects, 1):
        offsets.append(len(pdf))
        pdf += f"{i} 0 obj\n".encode() + obj + b"\nendobj\n"
    start = len(pdf)
    pdf += f"xref\n0 {len(offsets)}\n0000000000 65535 f \n".encode()
    for offset in offsets[1:]:
        pdf += f"{offset:010d} 00000 n \n".encode()
    pdf += f"trailer << /Size {len(offsets)} /Root 1 0 R >>\nstartxref\n{start}\n%%EOF\n".encode()
    return bytes(pdf)


def freeze() -> None:
    if (FIXTURES / "manifest.json").exists():
        raise RuntimeError("Refusing to replace frozen groups")
    FIXTURES.mkdir(parents=True, exist_ok=True)
    font = ImageFont.truetype("C:/Windows/Fonts/arial.ttf", 36)
    specs = [
        ("development", "shelf-regression", "Kệ sách có 45 quyển.", None, False),
        (
            "development",
            "office-stock",
            None,
            ["Vật tư", "Bút", "Sổ", "Tên", "Số lượng", "Bút mực", "24", "Giấy", "80"],
            False,
        ),
        ("held_out", "medical-appointment", "Bác sĩ hẹn khám vào thứ Sáu.", None, False),
        (
            "held_out",
            "transport-timetable",
            None,
            ["Lịch xe buýt", "Tuyến", "Giờ", "Chuyến sáng", "07:30", "Chuyến tối", "19:15"],
            True,
        ),
    ]
    items = []
    for split, group, text, cells, mixed in specs:
        image = Image.new("RGB", (1440, 720), "white")
        draw = ImageDraw.Draw(image)
        labels = []
        if text:
            draw.text((80, 100), text, fill="black", font=font)
            labels.append({"text": text, "bbox": [30, 160, 570, 430], "kind": "text"})
        else:
            assert cells is not None
            # Ruled table, first row merged across two columns. No oracle input to detection.
            for y in (100, 210, 320, 430, 540):
                draw.line((80, y, 1360, y), fill="black", width=3)
            for x in (80, 1360):
                draw.line((x, 100, x, 540), fill="black", width=3)
            draw.line((720, 210, 720, 540), fill="black", width=3)
            actual = [cells[0]] + cells[-6:]
            boxes = [(80, 100, 1360, 210)] + [
                (x, y, x + 640, y + 110) for y in (210, 320, 430) for x in (80, 720)
            ]
            for index, (value, box) in enumerate(zip(actual, boxes, strict=True)):
                draw.text((box[0] + 24, box[1] + 25), value, fill="black", font=font)
                labels.append(
                    {
                        "text": value,
                        "bbox": [
                            30 + box[0] * 0.375,
                            160 + box[1] * 0.375,
                            30 + box[2] * 0.375,
                            160 + box[3] * 0.375,
                        ],
                        "kind": "cell",
                        "row": 0 if index == 0 else 1 + (index - 1) // 2,
                        "column": 0 if index == 0 else (index - 1) % 2,
                        "row_span": 1,
                        "column_span": 2 if index == 0 else 1,
                    }
                )
        name = group + ".pdf"
        data = make_pdf(image, mixed)
        (FIXTURES / name).write_bytes(data)
        items.append(
            {
                "file": name,
                "sha256": sha(data),
                "source_group": group,
                "split": split,
                "language": "vi",
                "format": "pdf-mixed" if mixed else "pdf-scan",
                "source_key": "synthetic:" + group,
                "source_version": 1,
                "approved_field": "content",
                "page": 1,
                "labels": labels,
                "native_text": "Transit schedule" if mixed else "",
            }
        )
    manifest = {
        "dataset": "D18-PT-A-2026-10-18",
        "frozen_utc": time.strftime("%Y-%m-%dT%H:%M:%SZ", time.gmtime()),
        "config": CONFIG,
        "config_sha256": sha(json.dumps(CONFIG, sort_keys=True).encode()),
        "items": items,
        "synthetic": True,
        "human_adjudication": (
            "PENDING; deterministic render labels only. No independent human acceptance claimed."
        ),
        "dependency_edges": [],
        "cross_split_source_groups": [],
        "held_out_policy": (
            "No inference/tuning until configuration freeze; "
            "run once, retain failures. "
            "Independent source groups, not independent human adjudication."
        ),
    }
    write_json(FIXTURES / "manifest.json", manifest)
    (FIXTURES / "manifest.sha256").write_text(sha((FIXTURES / "manifest.json").read_bytes()) + "\n")
    print("Frozen four independent synthetic source groups, two development/two reserved held-out")


def provision() -> None:
    ARTIFACTS.mkdir(parents=True, exist_ok=True)

    def fetch(url: str) -> bytes:
        with urllib.request.urlopen(
            urllib.request.Request(url, headers={"User-Agent": "PPV-explicit-provisioning"}),
            timeout=45,
        ) as response:
            data = response.read(40 * 1024 * 1024 + 1)
        if len(data) > 40 * 1024 * 1024:
            raise RuntimeError("Provisioning cap exceeded")
        return data

    urls = {
        "tesseract.5.2.0.nupkg": "https://api.nuget.org/v3-flatcontainer/tesseract/5.2.0/tesseract.5.2.0.nupkg"
    }
    commit = json.loads(
        fetch("https://api.github.com/repos/tesseract-ocr/tessdata_best/commits/main")
    )["sha"]
    urls["vie.traineddata"] = (
        f"https://raw.githubusercontent.com/tesseract-ocr/tessdata_best/{commit}/vie.traineddata"
    )
    urls["tessdata-LICENSE"] = (
        f"https://raw.githubusercontent.com/tesseract-ocr/tessdata_best/{commit}/LICENSE"
    )
    artifacts = []
    for name, url in urls.items():
        path = ARTIFACTS / name
        if path.exists():
            raise RuntimeError("Refusing implicit re-provisioning")
        data = fetch(url)
        path.write_bytes(data)
        artifacts.append({"file": name, "origin": url, "sha256": sha(data), "bytes": len(data)})
    with zipfile.ZipFile(ARTIFACTS / "tesseract.5.2.0.nupkg") as package:
        for name in package.namelist():
            if name.startswith("x64/") and name.endswith(".dll"):
                data = package.read(name)
                path = ARTIFACTS / Path(name).name
                path.write_bytes(data)
                artifacts.append(
                    {
                        "file": path.name,
                        "origin": "NuGet Tesseract 5.2.0:" + name,
                        "sha256": sha(data),
                        "bytes": len(data),
                    }
                )
    write_json(
        EVIDENCE / "ocr-artifacts.json",
        {
            "artifacts": artifacts,
            "tessdata_commit": commit,
            "license": (
                "Tesseract/traineddata Apache-2.0; Leptonica BSD-2-Clause; "
                "wrapper Apache-2.0 (review upstream notices before redistribution)"
            ),
            "provisioning_only_network": True,
        },
    )
    print(
        "Provisioned exactly one local native CPU OCR candidate; "
        "no environment/package modifications"
    )


class Tesseract:
    def __init__(self) -> None:
        self.search = os.add_dll_directory(str(ARTIFACTS))
        self.dll = ct.CDLL(str(ARTIFACTS / "tesseract50.dll"))
        signatures = {
            "TessVersion": (ct.c_char_p, []),
            "TessBaseAPICreate": (ct.c_void_p, []),
            "TessBaseAPIInit2": (ct.c_int, [ct.c_void_p, ct.c_char_p, ct.c_char_p, ct.c_int]),
            "TessBaseAPISetPageSegMode": (None, [ct.c_void_p, ct.c_int]),
            "TessBaseAPISetImage": (
                None,
                [ct.c_void_p, ct.c_void_p, ct.c_int, ct.c_int, ct.c_int, ct.c_int],
            ),
            "TessBaseAPISetSourceResolution": (None, [ct.c_void_p, ct.c_int]),
            "TessBaseAPIGetUTF8Text": (ct.c_void_p, [ct.c_void_p]),
            "TessBaseAPIGetTsvText": (ct.c_void_p, [ct.c_void_p, ct.c_int]),
            "TessDeleteText": (None, [ct.c_void_p]),
            "TessBaseAPIDelete": (None, [ct.c_void_p]),
        }
        for name, (restype, argtypes) in signatures.items():
            fn = getattr(self.dll, name)
            fn.restype, fn.argtypes = restype, argtypes
        self.api = self.dll.TessBaseAPICreate()
        if self.dll.TessBaseAPIInit2(self.api, str(ARTIFACTS).encode(), b"vie", 1) != 0:
            self.close()
            raise RuntimeError("Native Vietnamese initialization failed")
        self.dll.TessBaseAPISetPageSegMode(self.api, 7)

    def recognize(self, image: Image.Image) -> tuple[str, list[dict[str, Any]]]:
        image = image.convert("L")
        buffer = ct.create_string_buffer(image.tobytes())
        self.dll.TessBaseAPISetImage(self.api, buffer, image.width, image.height, 1, image.width)
        self.dll.TessBaseAPISetSourceResolution(self.api, 144)
        ptr = self.dll.TessBaseAPIGetUTF8Text(self.api)
        if not ptr:
            raise RuntimeError("Native OCR returned no text")
        try:
            text = ct.string_at(ptr).decode("utf-8").strip()
        finally:
            self.dll.TessDeleteText(ptr)
        ptr = self.dll.TessBaseAPIGetTsvText(self.api, 0)
        if not ptr:
            raise RuntimeError("Missing native OCR boxes")
        try:
            tsv = ct.string_at(ptr).decode("utf-8")
        finally:
            self.dll.TessDeleteText(ptr)
        # C API omits the TSV renderer's header; explicit fields preserve first word.
        words = [
            dict(row)
            for row in csv.DictReader(
                io.StringIO(tsv),
                delimiter="\t",
                fieldnames=[
                    "level",
                    "page_num",
                    "block_num",
                    "par_num",
                    "line_num",
                    "word_num",
                    "left",
                    "top",
                    "width",
                    "height",
                    "conf",
                    "text",
                ],
            )
            if row.get("text", "").strip()
        ]
        return text, words

    def close(self) -> None:
        if self.api:
            self.dll.TessBaseAPIDelete(self.api)
            self.api = None
        self.search.close()


def cells(image: Image.Image) -> list[tuple[int, int, int, int]]:
    import cv2
    import numpy as np

    gray = np.asarray(image.convert("L"))
    _, ink = cv2.threshold(gray, 0, 255, cv2.THRESH_BINARY_INV + cv2.THRESH_OTSU)
    horizontal = cv2.morphologyEx(
        ink,
        cv2.MORPH_OPEN,
        cv2.getStructuringElement(cv2.MORPH_RECT, (max(30, int(image.width * 0.08)), 1)),
    )
    vertical = cv2.morphologyEx(
        ink,
        cv2.MORPH_OPEN,
        cv2.getStructuringElement(cv2.MORPH_RECT, (1, max(30, int(image.height * 0.08)))),
    )
    contours, hierarchy = cv2.findContours(
        horizontal | vertical, cv2.RETR_TREE, cv2.CHAIN_APPROX_SIMPLE
    )
    if hierarchy is None:
        return []
    found = []
    for i, contour in enumerate(contours):
        x, y, w, h = cv2.boundingRect(contour)
        if hierarchy[0][i][3] >= 0 and w >= 80 and h >= 40:
            found.append((x, y, x + w, y + h))
    return sorted(found, key=lambda b: (b[1] // 8, b[0]))


def evaluate(split: str) -> int:
    import pdfplumber

    manifest_bytes = (FIXTURES / "manifest.json").read_bytes()
    assert sha(manifest_bytes) == (FIXTURES / "manifest.sha256").read_text().strip()
    manifest = json.loads(manifest_bytes)
    assert CONFIG == manifest["config"]
    dev = {i["source_group"] for i in manifest["items"] if i["split"] == "development"}
    held = {i["source_group"] for i in manifest["items"] if i["split"] == "held_out"}
    assert not dev & held
    assert len({i["sha256"] for i in manifest["items"]}) == len(manifest["items"])
    target = EVIDENCE / f"ocr-{split}.json"
    if target.exists():
        raise RuntimeError("Refusing repeated evaluation/held-out feedback loop")
    for artifact in json.loads((EVIDENCE / "ocr-artifacts.json").read_text())["artifacts"]:
        assert sha((ARTIFACTS / artifact["file"]).read_bytes()) == artifact["sha256"]
    results = []
    engine = Tesseract()
    started = time.monotonic()
    try:
        for item in manifest["items"]:
            if item["split"] != split:
                continue
            record: dict[str, Any] = {
                "file": item["file"],
                "source_group": item["source_group"],
                "format": item["format"],
                "language": "vi",
                "expected_cells": sum(label["kind"] == "cell" for label in item["labels"]),
                "errors": [],
            }
            data = (FIXTURES / item["file"]).read_bytes()
            assert sha(data) == item["sha256"]
            with pdfplumber.open(io.BytesIO(data)) as pdf:
                page = pdf.pages[0]
                record["native_text"] = page.extract_text() or ""
                image_info = page.images[0]
                bitmap = page.to_image(resolution=144, force_mediabox=True).original
                region = [float(image_info[k]) for k in ("x0", "top", "x1", "bottom")]
                crop = bitmap.crop(tuple(round(x * 2) for x in region))
                boxes = cells(crop)
                regions = boxes or [(0, 0, crop.width, crop.height)]
                record["observed"] = []
                for box in regions:
                    # Exclude grid stroke from recognition; retain full structural cell box.
                    inset = 5 if boxes else 0
                    text, words = engine.recognize(
                        crop.crop((box[0] + inset, box[1] + inset, box[2] - inset, box[3] - inset))
                    )
                    locator = {
                        "source_key": item["source_key"],
                        "source_version": 1,
                        "approved_field": "content",
                        "page": 1,
                        "coordinate_system": "PDF_TOP_LEFT",
                        "bbox": [
                            region[0] + box[0] / 2,
                            region[1] + box[1] / 2,
                            region[0] + box[2] / 2,
                            region[1] + box[3] / 2,
                        ],
                    }
                    record["observed"].append(
                        {"text": text, "locator": locator, "ocr_words": words}
                    )
                labels = item["labels"]
                if len(regions) != len(labels):
                    record["errors"].append("CELL_OR_BLOCK_COUNT")
                for observed, label in zip(record["observed"], labels):
                    if observed["text"] != label["text"]:
                        record["errors"].append("MATERIAL_TEXT")
                    if any(
                        abs(a - b) > 2
                        for a, b in zip(observed["locator"]["bbox"], label["bbox"], strict=True)
                    ):
                        record["errors"].append("GEOMETRY")
                # Derive grid addresses from actual boundaries, without label feedback.
                if boxes:
                    xs = sorted({round(x / 8) * 8 for b in boxes for x in (b[0], b[2])})
                    ys = sorted({round(y / 8) * 8 for b in boxes for y in (b[1], b[3])})
                    for observed, b, label in zip(record["observed"], boxes, labels):
                        grid = {
                            "row": ys.index(round(b[1] / 8) * 8),
                            "column": xs.index(round(b[0] / 8) * 8),
                            "row_span": ys.index(round(b[3] / 8) * 8)
                            - ys.index(round(b[1] / 8) * 8),
                            "column_span": xs.index(round(b[2] / 8) * 8)
                            - xs.index(round(b[0] / 8) * 8),
                        }
                        observed["locator"].update(grid)
                        if any(grid[k] != label[k] for k in grid):
                            record["errors"].append("CELL_STRUCTURE")
                if record["native_text"] != item["native_text"]:
                    record["errors"].append("NATIVE_TEXT")
                record["pass"] = not record["errors"]
            record["source_unchanged"] = (
                sha((FIXTURES / item["file"]).read_bytes()) == item["sha256"]
            )
            results.append(record)
    finally:
        engine.close()
    report = {
        "split": split,
        "config": CONFIG,
        "manifest_sha256": sha(manifest_bytes),
        "runtime": platform.python_version(),
        "native_version": engine.dll.TessVersion().decode(),
        "cpu_only": True,
        "gpu": "NOT_TESTED / no CUDA provider installed",
        "duration_seconds": time.monotonic() - started,
        "documents": len(results),
        "pages": len(results),
        "expected_cells": sum(r["expected_cells"] for r in results),
        "pass_documents": sum(r["pass"] for r in results),
        "results": results,
        "publication": "NONE; offline sanitized diagnostic only",
        "human_adjudication": "PENDING",
    }
    write_json(target, report)
    print(json.dumps({k: v for k, v in report.items() if k != "results"}, ensure_ascii=True))
    return 0 if all(r["pass"] for r in results) else 1


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("action", choices=["freeze", "provision", "development", "held_out"])
    action = parser.parse_args().action
    EVIDENCE.mkdir(parents=True, exist_ok=True)
    if action == "freeze":
        freeze()
    elif action == "provision":
        provision()
    else:
        raise SystemExit(evaluate(action))

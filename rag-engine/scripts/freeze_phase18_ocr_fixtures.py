"""Freeze two small synthetic source families before any OCR probe/tuning."""

import hashlib
import json
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

root = Path(__file__).resolve().parents[1] / "tests/fixtures/phase18-ocr-v2"
root.mkdir(parents=True, exist_ok=True)
if (root / "manifest.json").exists():
    raise SystemExit("Refusing to replace a frozen dataset")

families = {
    "development-shelf": {
        "en": "Shelf contains 45 books.",
        "vi": "Kệ sách có 45 quyển.",
        "zh-Hans": "书架上有45本书。",
        "zh-Hant": "書架上有45本書。",
    },
    "held-out-invoice": {
        "en": "Invoice total 123 dollars. Date 18 October.",
        "vi": "Số tiền 123 đồng. Ngày 18 tháng 10.",
        "zh-Hans": "发票金额123元。日期10月18日。",
        "zh-Hant": "發票金額123元。日期10月18日。",
    },
}
items = []
for family, samples in families.items():
    for language, text in samples.items():
        font_file = (
            "C:/Windows/Fonts/msyh.ttc"
            if language.startswith("zh-")
            else "C:/Windows/Fonts/arial.ttf"
        )
        font = ImageFont.truetype(font_file, 40)
        image = Image.new("RGB", (1400, 200), "white")
        draw = ImageDraw.Draw(image)
        bbox = draw.textbbox((50, 50), text, font=font)
        draw.text((50, 50), text, font=font, fill="black")
        path = root / f"{family}-{language}.pdf"
        image.save(path, "PDF", resolution=72.0)
        items.append(
            {
                "file": path.name,
                "sha256": hashlib.sha256(path.read_bytes()).hexdigest(),
                "source_group": family,
                "split": "development" if family.startswith("development") else "held_out",
                "language": language,
                "format": "pdf-scan",
                "reference_text": text,
                "reference_page": 1,
                "reference_ink_bbox": bbox,
                "page_size": [1400, 200],
                "approved_field": "content",
                "source_version": 3,
            }
        )
manifest = {
    "dataset": "phase18-ocr-v2",
    "synthetic": True,
    "frozen_before_probe": True,
    "human_adjudication": (
        "PENDING; deterministic source strings/ink boxes, no human review claimed"
    ),
    "cross_split_group_intersection": [],
    "pipeline_selection": "Existing RapidOCR defaults; no tuning on this dataset",
    "items": items,
}
encoded = (json.dumps(manifest, ensure_ascii=False, indent=2) + "\n").encode("utf-8")
(root / "manifest.json").write_bytes(encoded)
(root / "manifest.sha256").write_text(hashlib.sha256(encoded).hexdigest() + "\n", encoding="utf-8")
print(f"Frozen {len(families)} synthetic groups, {len(items)} documents")

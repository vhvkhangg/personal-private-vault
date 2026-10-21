"""Adaptive OCR engine for multilingual PDF regions with targeted quality retry."""

from __future__ import annotations

import io
import math
from dataclasses import dataclass, field
from typing import Any

from PIL import Image

from ppv_rag.limits import MAX_RASTER_PIXELS, checkpoint, input_bytes


@dataclass(frozen=True)
class OCRTextBlock:
    text: str
    bbox: list[float]  # [x0, y0, x1, y1]
    confidence: float
    language: str
    retry_count: int = 0


@dataclass
class OCRResult:
    blocks: list[OCRTextBlock] = field(default_factory=list)
    warnings: list[str] = field(default_factory=list)
    retried: bool = False
    average_confidence: float = 1.0


class AdaptiveOCREngine:
    """Pinned RapidOCR Chinese/English backend. vi/zh-Hant admission is blocked.

    A language label cannot repair a recognizer alphabet or script conversion.
    Four-script quality acceptance remains unresolved until a capable backend is approved.
    """

    SUPPORTED_LANGUAGES = {"en", "vi", "zh-Hans", "zh-Hant"}

    def __init__(self, default_language: str = "en") -> None:
        if default_language not in self.SUPPORTED_LANGUAGES:
            raise ValueError("Unsupported OCR language")
        self.default_language = default_language
        self._engine: Any = None
        self._init_engine()

    def _init_engine(self) -> None:
        try:
            # Optional backend: absent from the selected Markdown runtime.
            from rapidocr_onnxruntime import (  # type: ignore[import-not-found, import-untyped]
                RapidOCR,
            )

            self._engine = RapidOCR()
        except Exception:
            self._engine = None

    def recognize_region(
        self,
        image_bytes: bytes,
        language: str | None = None,
        page_width: float = 612.0,
        page_height: float = 792.0,
        min_confidence_threshold: float = 0.5,
    ) -> OCRResult:
        """Run OCR on image region with at most one targeted retry on low confidence."""
        lang = language or self.default_language
        if lang not in self.SUPPORTED_LANGUAGES:
            raise ValueError("Unsupported OCR language")
        if lang in {"vi", "zh-Hant"}:
            raise ValueError("OCR_BACKEND_SCRIPT_UNVERIFIED")
        checkpoint()
        img = Image.open(io.BytesIO(input_bytes(image_bytes)))
        if img.width * img.height > MAX_RASTER_PIXELS:
            raise BufferError("RESOURCE_LIMIT_EXCEEDED: OCR raster")
        width, height = img.size

        # Primary OCR pass
        blocks, conf = self._run_pass(img, lang, width, height, page_width, page_height)
        retried = False
        warnings: list[str] = []
        # If confidence is below threshold, attempt at most ONE targeted retry with contrast
        if conf < min_confidence_threshold and blocks:
            retried = True
            from PIL import ImageEnhance

            enhanced = ImageEnhance.Contrast(img).enhance(1.5)
            retry_blocks, retry_conf = self._run_pass(
                enhanced, lang, width, height, page_width, page_height, retry=1
            )
            if retry_conf >= conf:
                blocks = retry_blocks
                conf = retry_conf
                warnings.append(
                    f"Targeted OCR retry succeeded with improved confidence: {retry_conf:.2f}"
                )
            else:
                warnings.append(f"Targeted OCR retry did not improve confidence: {retry_conf:.2f}")

        checkpoint()
        if blocks and (
            conf < min_confidence_threshold
            or any(b.confidence < min_confidence_threshold for b in blocks)
        ):
            raise ValueError("OCR_CONFIDENCE_REJECTED")
        return OCRResult(
            blocks=blocks,
            warnings=warnings,
            retried=retried,
            average_confidence=conf,
        )

    def _run_pass(
        self,
        img: Image.Image,
        lang: str,
        w: int,
        h: int,
        page_w: float,
        page_h: float,
        retry: int = 0,
    ) -> tuple[list[OCRTextBlock], float]:
        """Execute OCR pass using rapidocr. Never fabricates text on failure or empty input."""
        if self._engine is None:
            raise RuntimeError("OCR engine is not available")

        # RapidOCR accepts PIL.Image, numpy ndarray, or file path
        ocr_fn = self._engine  # type: ignore[operator]
        checkpoint()
        result, _ = ocr_fn(img)
        checkpoint()
        if not result:
            return [], 1.0

        blocks: list[OCRTextBlock] = []
        total_conf = 0.0
        for item in result:
            dt_boxes, text, score = item[0], item[1], float(item[2])
            if not math.isfinite(score) or not 0 <= score <= 1:
                raise ValueError("Invalid OCR confidence")
            if (
                len(dt_boxes) != 4
                or any(len(p) != 2 for p in dt_boxes)
                or any(not math.isfinite(float(v)) for p in dt_boxes for v in p)
            ):
                raise ValueError("Invalid OCR geometry")
            if any(p[0] < 0 or p[0] > w or p[1] < 0 or p[1] > h for p in dt_boxes):
                raise ValueError("OCR geometry outside source image")
            stripped_text = text.strip()
            if not stripped_text:
                continue
            total_conf += score
            # Normalize box to page coordinates
            xs = [pt[0] for pt in dt_boxes]
            ys = [pt[1] for pt in dt_boxes]
            x0 = max(0.0, float(min(xs)) * (page_w / max(w, 1)))
            x1 = min(page_w, float(max(xs)) * (page_w / max(w, 1)))
            y0 = max(0.0, float(min(ys)) * (page_h / max(h, 1)))
            y1 = min(page_h, float(max(ys)) * (page_h / max(h, 1)))

            if x1 <= x0 or y1 <= y0:
                raise ValueError("Invalid OCR geometry")
            if x1 > x0 and y1 > y0:
                blocks.append(
                    OCRTextBlock(
                        text=stripped_text,
                        bbox=[round(x0, 2), round(y0, 2), round(x1, 2), round(y1, 2)],
                        confidence=round(score, 4),
                        language=lang,
                        retry_count=retry,
                    )
                )

        avg = total_conf / len(result) if result else 1.0
        return blocks, round(avg, 4)

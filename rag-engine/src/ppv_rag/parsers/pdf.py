"""PDF extraction with measured native/table geometry and positioned region OCR."""

from __future__ import annotations

import io
import math
from typing import Any, BinaryIO

import pdfplumber

from ppv_rag.ocr.engine import AdaptiveOCREngine
from ppv_rag.parsers.models import DocumentElement, ElementKind, ParsedDocument


def _raster_has_grid(image: Any) -> bool:
    """Do not publish raster tables as unstructured paragraphs."""
    import cv2  # type: ignore[import-untyped]
    import numpy as np

    gray = cv2.cvtColor(np.asarray(image.convert("RGB")), cv2.COLOR_RGB2GRAY)
    _, ink = cv2.threshold(gray, 0, 255, cv2.THRESH_BINARY_INV + cv2.THRESH_OTSU)
    height, width = gray.shape
    horizontal = cv2.morphologyEx(
        ink, cv2.MORPH_OPEN, cv2.getStructuringElement(cv2.MORPH_RECT, (max(30, width // 4), 1))
    )
    vertical = cv2.morphologyEx(
        ink, cv2.MORPH_OPEN, cv2.getStructuringElement(cv2.MORPH_RECT, (1, max(30, height // 3)))
    )
    return bool(
        cv2.connectedComponents(horizontal)[0] >= 3 and cv2.connectedComponents(vertical)[0] >= 3
    )


def parse_pdf(
    file_bytes: bytes | BinaryIO,
    root_key: str,
    source_version: int,
    approved_field: str = "content",
    language: str = "en",
    ocr_engine: AdaptiveOCREngine | None = None,
) -> ParsedDocument:
    stream = io.BytesIO(file_bytes if isinstance(file_bytes, bytes) else file_bytes.read())
    doc = ParsedDocument(root_key, "PDF", source_version)
    ocr = ocr_engine or AdaptiveOCREngine(default_language=language)
    with pdfplumber.open(stream) as pdf:
        doc.page_count = len(pdf.pages)
        for page_index, page in enumerate(pdf.pages, 1):
            origin_x, origin_y, _, _ = page.bbox

            def emit(
                text: str,
                bbox: list[float],
                orientation: float,
                is_ocr: bool,
                kind: ElementKind = ElementKind.PARAGRAPH,
                metadata: dict[str, Any] | None = None,
            ) -> None:
                locator = {
                    "source_key": root_key,
                    "source_version": source_version,
                    "approved_field": approved_field,
                    "format": "PDF",
                    "page": page_index,
                    "bbox": bbox,
                    "coordinate_system": "PDF_TOP_LEFT",
                    "page_origin": [origin_x, origin_y],
                    "page_rotation": page.rotation,
                    "orientation": orientation,
                    "is_ocr": is_ocr,
                }
                if metadata:
                    for key in ("table_index", "row_index", "column_index"):
                        if key in metadata:
                            locator[key] = metadata[key]
                element = DocumentElement(
                    kind,
                    text,
                    root_key,
                    source_version,
                    approved_field,
                    locator,
                    metadata=metadata or {},
                )
                element.validate_mandatory_locator("PDF")
                doc.elements.append(element)

            tables = page.find_tables()
            for table_index, table in enumerate(tables):
                doc.table_count += 1
                values = table.extract()
                cells = [cell for row in table.rows for cell in row.cells if cell]
                xs = sorted({v for cell in cells for v in (cell[0], cell[2])})
                ys = sorted({v for cell in cells for v in (cell[1], cell[3])})
                for row_index, row in enumerate(table.rows):
                    for column_index, cell in enumerate(row.cells):
                        if cell is None:
                            continue
                        cell_text = values[row_index][column_index]
                        if not cell_text:
                            continue
                        emit(
                            cell_text,
                            list(cell),
                            float(page.rotation),
                            False,
                            ElementKind.TABLE_CELL,
                            {
                                "table_index": table_index,
                                "row_index": row_index,
                                "column_index": column_index,
                                "row_span": ys.index(cell[3]) - ys.index(cell[1]),
                                "column_span": xs.index(cell[2]) - xs.index(cell[0]),
                                "table_bbox": list(table.bbox),
                                "cell_grid": cells,
                            },
                        )
            for line in page.extract_text_lines(return_chars=True):
                bbox = [float(line[k]) for k in ("x0", "top", "x1", "bottom")]
                cx, cy = (bbox[0] + bbox[2]) / 2, (bbox[1] + bbox[3]) / 2
                if any(
                    t.bbox[0] <= cx <= t.bbox[2] and t.bbox[1] <= cy <= t.bbox[3] for t in tables
                ):
                    continue
                matrices = [c.get("matrix") for c in line["chars"] if c.get("text", "").strip()]
                if not matrices or any(m is None or len(m) != 6 for m in matrices):
                    raise ValueError("Native PDF orientation is unavailable")
                angles = {round(math.degrees(math.atan2(m[1], m[0])), 4) for m in matrices}
                if len(angles) != 1:
                    raise ValueError("Mixed native orientations require separate geometry")
                emit(line["text"], bbox, angles.pop(), False)
            if page.images:
                bitmap = page.to_image(resolution=144, force_mediabox=True).original
                sx, sy = bitmap.width / page.width, bitmap.height / page.height
                for image_info in page.images:
                    x0, top, x1, bottom = [
                        float(image_info[k]) for k in ("x0", "top", "x1", "bottom")
                    ]
                    if x1 <= x0 or bottom <= top:
                        raise ValueError("Image placement is invalid")
                    crop = bitmap.crop(
                        (
                            round((x0 - origin_x) * sx),
                            round((top - origin_y) * sy),
                            round((x1 - origin_x) * sx),
                            round((bottom - origin_y) * sy),
                        )
                    )
                    if _raster_has_grid(crop):
                        raise ValueError("Raster table cell structure is not verified")
                    encoded = io.BytesIO()
                    crop.save(encoded, format="PNG")
                    result = ocr.recognize_region(
                        encoded.getvalue(),
                        language=language,
                        page_width=x1 - x0,
                        page_height=bottom - top,
                    )
                    doc.warnings.extend(result.warnings)
                    for block in result.blocks:
                        emit(
                            block.text,
                            [
                                block.bbox[0] + x0,
                                block.bbox[1] + top,
                                block.bbox[2] + x0,
                                block.bbox[3] + top,
                            ],
                            float(page.rotation),
                            True,
                            metadata={"confidence": block.confidence},
                        )
            page.close()
    return doc

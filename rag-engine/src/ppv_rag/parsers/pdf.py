"""PDF extraction with measured native/table geometry and positioned region OCR."""

from __future__ import annotations

import io
import math
from typing import Any, BinaryIO

import pdfplumber

from ppv_rag.limits import MAX_PAGES, MAX_RASTER_PIXELS, check_text, checked_pdf, checkpoint
from ppv_rag.ocr.engine import AdaptiveOCREngine
from ppv_rag.parsers.models import DocumentElement, ElementKind, ParsedDocument


def _raster_cells(image: Any) -> list[tuple[int, int, int, int]]:
    """Measured closed ruled cells, including merged rectangles; no inferred missing borders."""
    import cv2  # type: ignore[import-untyped]
    import numpy as np

    gray = np.asarray(image.convert("L"))
    _, ink = cv2.threshold(gray, 0, 255, cv2.THRESH_BINARY_INV + cv2.THRESH_OTSU)
    horizontal = cv2.morphologyEx(
        ink,
        cv2.MORPH_OPEN,
        cv2.getStructuringElement(cv2.MORPH_RECT, (max(30, image.width // 12), 1)),
    )
    vertical = cv2.morphologyEx(
        ink,
        cv2.MORPH_OPEN,
        cv2.getStructuringElement(cv2.MORPH_RECT, (1, max(30, image.height // 12))),
    )
    has_grid = (
        cv2.connectedComponents(horizontal)[0] >= 3 and cv2.connectedComponents(vertical)[0] >= 3
    )
    if not has_grid:
        return []
    contours, hierarchy = cv2.findContours(
        cv2.bitwise_or(horizontal, vertical), cv2.RETR_TREE, cv2.CHAIN_APPROX_SIMPLE
    )
    if hierarchy is None:
        raise ValueError("Raster table geometry unavailable")
    boxes = []
    for index, contour in enumerate(contours):
        x, y, w, h = cv2.boundingRect(contour)
        if hierarchy[0][index][3] >= 0 and w >= 20 and h >= 20:
            boxes.append((x, y, x + w, y + h))
    if len(boxes) < 2:
        raise ValueError("Raster table geometry unavailable")
    return sorted(boxes, key=lambda b: (b[1] // 8, b[0]))


def _grid_axis(values: list[int]) -> list[float]:
    groups: list[list[int]] = []
    for value in sorted(set(values)):
        if groups and value - groups[-1][-1] <= 4:
            groups[-1].append(value)
        else:
            groups.append([value])
    return [sum(g) / len(g) for g in groups]


def parse_pdf(
    file_bytes: bytes | BinaryIO,
    root_key: str,
    source_version: int,
    approved_field: str = "content",
    language: str = "en",
    ocr_engine: AdaptiveOCREngine | None = None,
) -> ParsedDocument:
    stream = io.BytesIO(checked_pdf(file_bytes))
    doc = ParsedDocument(root_key, "PDF", source_version)
    total_text = 0
    ocr = ocr_engine or AdaptiveOCREngine(default_language=language)
    with pdfplumber.open(stream) as pdf:
        # Count lazily before retaining page objects; stop at the finite cap.
        from itertools import islice

        from pdfminer.pdfpage import PDFPage

        if sum(1 for _ in islice(PDFPage.create_pages(pdf.doc), MAX_PAGES + 1)) > MAX_PAGES:
            raise BufferError("RESOURCE_LIMIT_EXCEEDED: PDF pages")
        doc.page_count = len(pdf.pages)
        for page_index, page in enumerate(pdf.pages, 1):
            checkpoint()
            page_start = len(doc.elements)
            origin_x, origin_y, _, _ = page.bbox

            def emit(
                text: str,
                bbox: list[float],
                orientation: float,
                is_ocr: bool,
                kind: ElementKind = ElementKind.PARAGRAPH,
                metadata: dict[str, Any] | None = None,
            ) -> None:
                nonlocal total_text
                total_text += len(text)
                if total_text > 1_000_000:
                    raise BufferError("RESOURCE_LIMIT_EXCEEDED: PDF text")
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
                if page.width * page.height * 4 > MAX_RASTER_PIXELS:
                    raise BufferError("RESOURCE_LIMIT_EXCEEDED: PDF raster")
                for image_info in page.images:
                    if (
                        image_info.get("srcsize")
                        and image_info["srcsize"][0] * image_info["srcsize"][1] > MAX_RASTER_PIXELS
                    ):
                        raise BufferError("RESOURCE_LIMIT_EXCEEDED: embedded raster")
                bitmap = page.to_image(resolution=144, force_mediabox=True).original
                sx, sy = bitmap.width / page.width, bitmap.height / page.height
                native = list(doc.elements[page_start:])
                from PIL import ImageDraw

                seen_regions: set[tuple[float, ...]] = set()
                for image_info in page.images:
                    checkpoint()
                    x0, top, x1, bottom = [
                        float(image_info[k]) for k in ("x0", "top", "x1", "bottom")
                    ]
                    region = (x0, top, x1, bottom)
                    if region in seen_regions:
                        continue
                    seen_regions.add(region)
                    if (
                        x1 <= x0
                        or bottom <= top
                        or x0 < origin_x
                        or top < origin_y
                        or x1 > page.bbox[2]
                        or bottom > page.bbox[3]
                    ):
                        raise ValueError("Image placement is invalid")
                    crop = bitmap.crop(
                        (
                            round((x0 - origin_x) * sx),
                            round((top - origin_y) * sy),
                            round((x1 - origin_x) * sx),
                            round((bottom - origin_y) * sy),
                        )
                    )
                    # Native evidence already covers these regions: remove them before OCR.
                    draw = ImageDraw.Draw(crop)
                    for item in native:
                        bx0, by0, bx1, by1 = item.locator["bbox"]
                        if bx1 > x0 and by1 > top and bx0 < x1 and by0 < bottom:
                            draw.rectangle(
                                (
                                    max(0, (bx0 - x0) * sx - 1),
                                    max(0, (by0 - top) * sy - 1),
                                    min(crop.width, (bx1 - x0) * sx + 1),
                                    min(crop.height, (by1 - top) * sy + 1),
                                ),
                                fill="white",
                            )
                    boxes = _raster_cells(crop)
                    axes_x = _grid_axis([v for b in boxes for v in (b[0], b[2])])
                    axes_y = _grid_axis([v for b in boxes for v in (b[1], b[3])])
                    occupied: set[tuple[int, int]] = set()
                    table_index = doc.table_count
                    if boxes:
                        doc.table_count += 1
                    # Cell OCR cannot account for headings/captions elsewhere in this image.
                    # Mask only the measured cells, then OCR the remaining original image once.
                    remainder = crop.copy()
                    if boxes:
                        mask = ImageDraw.Draw(remainder)
                        for cell in boxes:
                            mask.rectangle(cell, fill="white")
                    regions = [(box, True) for box in boxes]
                    regions.append(((0, 0, crop.width, crop.height), False))
                    for box, is_cell in regions:
                        checkpoint()
                        a, b, c, d = box
                        meta: dict[str, Any] = {}
                        if is_cell:
                            ix0, ix1 = [
                                min(range(len(axes_x)), key=lambda i: abs(axes_x[i] - v))
                                for v in (a, c)
                            ]
                            iy0, iy1 = [
                                min(range(len(axes_y)), key=lambda i: abs(axes_y[i] - v))
                                for v in (b, d)
                            ]
                            slots = {
                                (row, col) for row in range(iy0, iy1) for col in range(ix0, ix1)
                            }
                            if not slots or occupied & slots:
                                raise ValueError("Ambiguous raster table cells")
                            occupied.update(slots)
                            meta = {
                                "table_index": table_index,
                                "row_index": iy0,
                                "column_index": ix0,
                                "row_span": iy1 - iy0,
                                "column_span": ix1 - ix0,
                                "cell_grid": [list(cell) for cell in boxes],
                                "grid_coordinate_system": "IMAGE_PIXELS",
                                "image_bbox": list(region),
                            }
                        inset = 3 if is_cell else 0
                        part = (crop if is_cell else remainder).crop(
                            (a + inset, b + inset, c - inset, d - inset)
                        )
                        encoded = io.BytesIO()
                        part.save(encoded, format="PNG")
                        result = ocr.recognize_region(
                            encoded.getvalue(),
                            language=language,
                            page_width=part.width / sx,
                            page_height=part.height / sy,
                        )
                        checkpoint()
                        doc.warnings.extend(result.warnings)
                        for block in result.blocks:
                            bbox = [
                                block.bbox[0] + x0 + (a + inset) / sx,
                                block.bbox[1] + top + (b + inset) / sy,
                                block.bbox[2] + x0 + (a + inset) / sx,
                                block.bbox[3] + top + (b + inset) / sy,
                            ]
                            duplicate = False
                            for item in native:
                                nb = item.locator["bbox"]
                                area = max(0, min(bbox[2], nb[2]) - max(bbox[0], nb[0])) * (
                                    max(0, min(bbox[3], nb[3]) - max(bbox[1], nb[1]))
                                )
                                if area > 0.5 * (bbox[2] - bbox[0]) * (bbox[3] - bbox[1]):
                                    if " ".join(block.text.split()) == " ".join(item.text.split()):
                                        duplicate = True
                                        break
                                    raise ValueError("Ambiguous native/OCR overlap")
                            if duplicate:
                                continue
                            if is_cell:
                                meta["text_bbox"] = bbox
                                bbox = [x0 + a / sx, top + b / sy, x0 + c / sx, top + d / sy]
                            emit(
                                block.text,
                                bbox,
                                float(page.rotation),
                                True,
                                ElementKind.TABLE_CELL if is_cell else ElementKind.PARAGRAPH,
                                {**meta, "confidence": block.confidence},
                            )
                    if boxes and len(occupied) != (len(axes_x) - 1) * (len(axes_y) - 1):
                        raise ValueError("Incomplete raster table geometry")
            # Native paragraphs, tables and OCR share one original-page reading order.
            doc.elements[page_start:] = sorted(
                doc.elements[page_start:],
                key=lambda e: (e.locator["bbox"][1], e.locator["bbox"][0]),
            )
            check_text("\n".join(e.text for e in doc.elements))
            page.close()
    return doc

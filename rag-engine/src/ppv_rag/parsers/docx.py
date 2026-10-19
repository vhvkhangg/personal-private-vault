"""DOCX document parser with paragraph, heading, and table cell locators."""

from __future__ import annotations

import io
from typing import BinaryIO

import docx  # type: ignore[import-untyped]
from docx.table import Table  # type: ignore[import-untyped]
from docx.text.paragraph import Paragraph  # type: ignore[import-untyped]

from ppv_rag.parsers.models import DocumentElement, ElementKind, ParsedDocument


def extract_visible_text(paragraph: Paragraph) -> str:
    """Extract visible text from paragraph, filtering out hidden runs (w:vanish / w:webHidden)."""
    parts: list[str] = []
    for run in paragraph.runs:
        if run.font and run.font.hidden:
            continue
        r_elem = getattr(run, "_r", None)
        rPr = getattr(r_elem, "rPr", None)
        if rPr is not None:
            if any(
                child.tag.endswith("vanish") or child.tag.endswith("webHidden") for child in rPr
            ):
                continue
        parts.append(run.text)
    return "".join(parts).strip()


def parse_docx(
    file_bytes: bytes | BinaryIO,
    root_key: str,
    source_version: int,
    approved_field: str = "content",
) -> ParsedDocument:
    """Parse DOCX document into paragraphs and tables with mandatory locators."""
    stream = io.BytesIO(file_bytes) if isinstance(file_bytes, bytes) else file_bytes
    doc_obj = docx.Document(stream)
    doc = ParsedDocument(root_key=root_key, format_type="DOCX", source_version=source_version)

    heading_stack: list[tuple[int, str]] = []

    def current_heading_path() -> list[str]:
        return [h[1] for h in heading_stack]

    para_idx = 0
    table_idx = 0

    # Iterate through paragraphs and tables in order of document body
    for block in doc_obj.iter_inner_content():
        if isinstance(block, Paragraph):
            text = extract_visible_text(block)
            if not text:
                para_idx += 1
                continue

            style_name = block.style.name if block.style else ""
            if style_name.startswith("Heading"):
                try:
                    level = int(style_name.replace("Heading", "").strip())
                except ValueError:
                    level = 1
                while heading_stack and heading_stack[-1][0] >= level:
                    heading_stack.pop()
                heading_stack.append((level, text))

                elem = DocumentElement(
                    kind=ElementKind.HEADING,
                    text=text,
                    source_identity=root_key,
                    source_version=source_version,
                    approved_field=approved_field,
                    locator={
                        "source_key": root_key,
                        "source_version": source_version,
                        "approved_field": approved_field,
                        "paragraph_index": para_idx,
                        "style": style_name,
                        "heading_level": level,
                        "heading_path": current_heading_path(),
                    },
                    heading_path=current_heading_path(),
                    metadata={"level": level, "style": style_name},
                )
                elem.validate_mandatory_locator("DOCX")
                doc.elements.append(elem)
            else:
                elem = DocumentElement(
                    kind=ElementKind.PARAGRAPH,
                    text=text,
                    source_identity=root_key,
                    source_version=source_version,
                    approved_field=approved_field,
                    locator={
                        "source_key": root_key,
                        "source_version": source_version,
                        "approved_field": approved_field,
                        "paragraph_index": para_idx,
                        "style": style_name,
                        "heading_path": current_heading_path(),
                    },
                    heading_path=current_heading_path(),
                    metadata={"style": style_name},
                )
                elem.validate_mandatory_locator("DOCX")
                doc.elements.append(elem)
            para_idx += 1

        elif isinstance(block, Table):
            doc.table_count += 1
            headers: list[str] = []
            for r_idx, row in enumerate(block.rows):
                row_cells: list[str] = []
                for cell in row.cells:
                    cell_text = " ".join(extract_visible_text(p) for p in cell.paragraphs).strip()
                    row_cells.append(cell_text)

                if r_idx == 0:
                    headers = row_cells
                    header_line = "Table Header: " + " | ".join(h for h in headers if h)
                    elem = DocumentElement(
                        kind=ElementKind.TABLE,
                        text=header_line,
                        source_identity=root_key,
                        source_version=source_version,
                        approved_field=approved_field,
                        locator={
                            "source_key": root_key,
                            "source_version": source_version,
                            "approved_field": approved_field,
                            "table_index": table_idx,
                            "row_index": 0,
                            "row_count": len(block.rows),
                            "col_count": len(row.cells),
                            "heading_path": current_heading_path(),
                        },
                        heading_path=current_heading_path(),
                        metadata={"is_header": True, "table_index": table_idx},
                    )
                    elem.validate_mandatory_locator("DOCX")
                    doc.elements.append(elem)
                else:
                    if not any(row_cells):
                        continue
                    pairs: list[str] = []
                    for c_idx, val in enumerate(row_cells):
                        if not val:
                            continue
                        col_hdr = (
                            headers[c_idx]
                            if c_idx < len(headers) and headers[c_idx]
                            else f"Col{c_idx + 1}"
                        )
                        pairs.append(f"{col_hdr}: {val}")
                    row_line = f"Table Row {r_idx}: " + "; ".join(pairs)
                    elem = DocumentElement(
                        kind=ElementKind.TABLE_CELL,
                        text=row_line,
                        source_identity=root_key,
                        source_version=source_version,
                        approved_field=approved_field,
                        locator={
                            "source_key": root_key,
                            "source_version": source_version,
                            "approved_field": approved_field,
                            "table_index": table_idx,
                            "row_index": r_idx,
                            "row_count": len(block.rows),
                            "col_count": len(row.cells),
                            "heading_path": current_heading_path(),
                        },
                        heading_path=current_heading_path(),
                        metadata={"table_index": table_idx, "row_index": r_idx},
                    )
                    elem.validate_mandatory_locator("DOCX")
                    doc.elements.append(elem)
            table_idx += 1

    return doc

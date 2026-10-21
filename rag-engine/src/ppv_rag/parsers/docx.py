"""DOCX document parser with paragraph, heading, and table cell locators."""

from __future__ import annotations

import io
from typing import Any, BinaryIO

import docx  # type: ignore[import-untyped]
from docx.oxml.ns import qn  # type: ignore[import-untyped]
from docx.table import Table, _Cell  # type: ignore[import-untyped]
from docx.text.paragraph import Paragraph  # type: ignore[import-untyped]
from docx.text.run import Run  # type: ignore[import-untyped]

from ppv_rag.limits import check_text, checked_zip, checkpoint
from ppv_rag.parsers.models import DocumentElement, ElementKind, ParsedDocument


def extract_paragraph_text(paragraph: Paragraph, include_hidden: bool = True) -> tuple[str, bool]:
    """Extract text from paragraph runs and determine if any hidden runs exist."""
    parts: list[str] = []
    has_hidden = False
    for node in paragraph._p.iter(qn("w:r")):
        run = Run(node, paragraph)
        is_run_hidden = False
        if run.font and run.font.hidden:
            is_run_hidden = True
        r_elem = getattr(run, "_r", None)
        rPr = getattr(r_elem, "rPr", None)
        if rPr is not None:
            if any(
                child.tag.endswith("vanish") or child.tag.endswith("webHidden") for child in rPr
            ):
                is_run_hidden = True
        if is_run_hidden:
            has_hidden = True
            if not include_hidden:
                continue
        parts.append(run.text)
    return "".join(parts).strip(), has_hidden


def extract_visible_text(paragraph: Paragraph) -> str:
    """Extract text from paragraph (legacy helper, includes all content)."""
    return extract_paragraph_text(paragraph, include_hidden=True)[0]


def parse_docx(
    file_bytes: bytes | BinaryIO,
    root_key: str,
    source_version: int,
    approved_field: str = "content",
    include_hidden: bool = True,
) -> ParsedDocument:
    """Parse DOCX document into paragraphs, tables, properties, and comments
    with mandatory locators.
    """
    stream = io.BytesIO(checked_zip(file_bytes))
    doc_obj = docx.Document(stream)
    doc = ParsedDocument(root_key=root_key, format_type="DOCX", source_version=source_version)

    # 1. Extract core properties if present
    if hasattr(doc_obj, "core_properties"):
        for prop_name in ("title", "author", "comments", "keywords", "subject", "category"):
            val = getattr(doc_obj.core_properties, prop_name, None)
            if val and isinstance(val, str) and val.strip():
                text = f"Document Property [{prop_name}]: {val.strip()}"
                elem = DocumentElement(
                    kind=ElementKind.METADATA,
                    text=text,
                    source_identity=root_key,
                    source_version=source_version,
                    approved_field=approved_field,
                    locator={
                        "source_key": root_key,
                        "source_version": source_version,
                        "approved_field": approved_field,
                        "container_part": "core_properties",
                        "property_key": prop_name,
                    },
                    heading_path=[],
                    metadata={"container_part": "core_properties", "property_key": prop_name},
                )
                elem.validate_mandatory_locator("DOCX")
                doc.elements.append(elem)

    heading_stack: list[tuple[int, str]] = []
    anchors: dict[str, dict[str, Any]] = {}
    total_text = sum(len(e.text) for e in doc.elements)

    def emit(text: str, kind: ElementKind, loc: dict[str, Any], meta: dict[str, Any]) -> None:
        nonlocal total_text
        checkpoint()
        total_text += len(text)
        if total_text > 1_000_000:
            raise BufferError("RESOURCE_LIMIT_EXCEEDED: DOCX text")
        check_text(text)
        element = DocumentElement(
            kind,
            text,
            root_key,
            source_version,
            approved_field,
            {
                "source_key": root_key,
                "source_version": source_version,
                "approved_field": approved_field,
                **loc,
            },
            [h[1] for h in heading_stack],
            meta,
        )
        element.validate_mandatory_locator("DOCX")
        doc.elements.append(element)

    def walk(container: Any, path: list[Any], cell_locator: dict[str, Any]) -> None:
        paragraph_index = table_index = 0
        for block in container.iter_inner_content():
            checkpoint()
            if isinstance(block, Paragraph):
                address = [*path, "paragraph", paragraph_index]
                loc = {
                    **cell_locator,
                    "paragraph_index": paragraph_index,
                    "structural_path": address,
                }
                # Character positions are in the original paragraph text, including hyperlinks.
                offset = 0
                for node in block._p.iter():
                    if node.tag in (qn("w:commentRangeStart"), qn("w:commentRangeEnd")):
                        identity = node.get(qn("w:id"))
                        if identity is None or not identity.strip():
                            raise ValueError("Invalid comment anchor identity")
                        end = "start" if node.tag == qn("w:commentRangeStart") else "end"
                        if end in anchors.setdefault(identity, {}):
                            raise ValueError("Ambiguous comment anchor")
                        anchors[identity][end] = {**loc, "char_offset": offset}
                    elif node.tag == qn("w:t"):
                        offset += len(node.text or "")
                    elif node.tag in (qn("w:tab"), qn("w:br"), qn("w:cr")):
                        offset += 1
                text, hidden = extract_paragraph_text(block, include_hidden)
                if text:
                    style = block.style.name if block.style else ""
                    kind = ElementKind.PARAGRAPH
                    if style.startswith("Heading"):
                        try:
                            level = int(style.removeprefix("Heading").strip())
                        except ValueError:
                            level = 1
                        while heading_stack and heading_stack[-1][0] >= level:
                            heading_stack.pop()
                        heading_stack.append((level, text))
                        kind = ElementKind.HEADING
                    elif cell_locator:
                        kind = (
                            ElementKind.TABLE
                            if cell_locator["row_index"] == 0
                            else (ElementKind.TABLE_CELL)
                        )
                    emit(text, kind, loc, {"style": style, "has_hidden_runs": hidden})
                paragraph_index += 1
            elif isinstance(block, Table):
                doc.table_count += 1
                table_path = [*path, "table", table_index]
                for row_index, row in enumerate(block.rows):
                    column = row.grid_cols_before
                    # Physical cells, never repeated proxies for merged cells.
                    for tc in row._tr.tc_lst:
                        cell = _Cell(tc, block)
                        span = tc.grid_span
                        loc = {
                            "table_index": table_index,
                            "row_index": row_index,
                            "column_index": column,
                            "column_span": span,
                            "table_path": table_path,
                            "vertical_merge": tc.vMerge,
                        }
                        walk(cell, [*table_path, "row", row_index, "column", column], loc)
                        column += span
                table_index += 1

    walk(doc_obj, ["body"], {})
    # Comments retain their actual range; detached objects use the structural variant.
    for comm in doc_obj.part.comments:
        identity = str(comm.comment_id)
        if not identity.strip():
            raise ValueError("Missing comment identity")
        anchor = anchors.pop(identity, None)
        if anchor is not None and set(anchor) != {"start", "end"}:
            raise ValueError("Incomplete comment anchor")
        text = comm.text.strip()
        if text:
            emit(
                f"Comment [{identity}]: {text}",
                ElementKind.COMMENT,
                {
                    "container_part": "comments",
                    "comment_id": identity,
                    **({"anchor": anchor} if anchor else {}),
                },
                {"author": comm.author, "anchored": anchor is not None},
            )
    if anchors:
        raise ValueError("Comment anchor has no original object")
    check_text("\n".join(e.text for e in doc.elements))
    return doc

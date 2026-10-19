"""Plain text document parser with exact character and line span locators."""

from __future__ import annotations

from ppv_rag.parsers.models import DocumentElement, ElementKind, ParsedDocument


def parse_text(
    content: str,
    root_key: str,
    source_version: int,
    approved_field: str = "content",
) -> ParsedDocument:
    """Parse plain text into paragraphs with exact character and line spans."""
    doc = ParsedDocument(root_key=root_key, format_type="TXT", source_version=source_version)

    lines = content.splitlines(keepends=True)
    if not lines:
        return doc

    current_para_lines: list[str] = []
    line_start = 1
    char_offset = 0
    para_char_start = 0

    for i, line in enumerate(lines, start=1):
        stripped = line.strip()
        if not stripped:
            if current_para_lines:
                para_text = "".join(current_para_lines).strip()
                if para_text:
                    elem = DocumentElement(
                        kind=ElementKind.PARAGRAPH,
                        text=para_text,
                        source_identity=root_key,
                        source_version=source_version,
                        approved_field=approved_field,
                        locator={
                            "source_key": root_key,
                            "source_version": source_version,
                            "approved_field": approved_field,
                            "line_start": line_start,
                            "line_end": i - 1,
                            "char_start": para_char_start
                            + len("".join(current_para_lines))
                            - len("".join(current_para_lines).lstrip()),
                            "char_end": para_char_start + len("".join(current_para_lines).rstrip()),
                        },
                    )
                    elem.validate_mandatory_locator("TXT")
                    doc.elements.append(elem)
                current_para_lines = []
            char_offset += len(line)
            line_start = i + 1
            para_char_start = char_offset
        else:
            if not current_para_lines:
                line_start = i
                para_char_start = char_offset
            current_para_lines.append(line)
            char_offset += len(line)

    if current_para_lines:
        para_text = "".join(current_para_lines).strip()
        if para_text:
            elem = DocumentElement(
                kind=ElementKind.PARAGRAPH,
                text=para_text,
                source_identity=root_key,
                source_version=source_version,
                approved_field=approved_field,
                locator={
                    "source_key": root_key,
                    "source_version": source_version,
                    "approved_field": approved_field,
                    "line_start": line_start,
                    "line_end": len(lines),
                    "char_start": para_char_start
                    + len("".join(current_para_lines))
                    - len("".join(current_para_lines).lstrip()),
                    "char_end": para_char_start + len("".join(current_para_lines).rstrip()),
                },
            )
            elem.validate_mandatory_locator("TXT")
            doc.elements.append(elem)

    return doc

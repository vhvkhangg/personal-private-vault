"""Synthetic original-page oracles for transformed glyphs and native table cells."""

import io

import pypdf
import pytest
from pypdf.generic import DecodedStreamObject, DictionaryObject, NameObject

from ppv_rag.parsers.models import ElementKind
from ppv_rag.parsers.pdf import parse_pdf


def pdf_with_stream(commands: bytes) -> bytes:
    writer = pypdf.PdfWriter()
    page = writer.add_blank_page(612, 792)
    font = DictionaryObject(
        {
            NameObject("/Type"): NameObject("/Font"),
            NameObject("/Subtype"): NameObject("/Type1"),
            NameObject("/BaseFont"): NameObject("/Helvetica"),
        }
    )
    page[NameObject("/Resources")] = DictionaryObject(
        {NameObject("/Font"): DictionaryObject({NameObject("/F1"): writer._add_object(font)})}
    )
    content = DecodedStreamObject()
    content.set_data(commands)
    page[NameObject("/Contents")] = writer._add_object(content)
    output = io.BytesIO()
    writer.write(output)
    return output.getvalue()


def test_scaled_glyph_width_comes_from_font_and_matrix() -> None:
    data = pdf_with_stream(b"BT /F1 12 Tf 2 0 0 1 72 700 Tm (iiiWWW) Tj ET")
    parsed = parse_pdf(data, "scaled", 4)
    assert len(parsed.elements) == 1
    element = parsed.elements[0]
    assert element.text == "iiiWWW"
    assert element.locator["bbox"][0] == pytest.approx(72)
    # Helvetica i=222/1000 em, W=944/1000 em; horizontal scale is 2.
    assert element.locator["bbox"][2] == pytest.approx(72 + 2 * 12 * 3 * (0.222 + 0.944))
    assert element.locator["coordinate_system"] == "PDF_TOP_LEFT"


def test_rotated_native_text_preserves_actual_orientation() -> None:
    data = pdf_with_stream(b"BT /F1 12 Tf 0 1 -1 0 100 400 Tm (ROTATED) Tj ET")
    parsed = parse_pdf(data, "rotated", 2)
    assert parsed.elements
    assert all(e.locator["orientation"] == pytest.approx(90) for e in parsed.elements)


def test_native_table_cell_addresses_and_text_are_preserved() -> None:
    commands = b"""
        50 600 m 250 600 l S 50 650 m 250 650 l S 50 700 m 250 700 l S
        50 600 m 50 700 l S 150 600 m 150 700 l S 250 600 m 250 700 l S
        BT /F1 12 Tf 60 675 Td (A) Tj ET
        BT /F1 12 Tf 160 675 Td (B) Tj ET
        BT /F1 12 Tf 60 625 Td (1) Tj ET
        BT /F1 12 Tf 160 625 Td (2) Tj ET
    """
    parsed = parse_pdf(pdf_with_stream(commands), "table", 6)
    cells = [e for e in parsed.elements if e.kind == ElementKind.TABLE_CELL]
    assert parsed.table_count == 1
    assert [(e.metadata["row_index"], e.metadata["column_index"], e.text) for e in cells] == [
        (0, 0, "A"),
        (0, 1, "B"),
        (1, 0, "1"),
        (1, 1, "2"),
    ]
    assert cells[0].locator["bbox"] == pytest.approx([50, 92, 150, 142])
    assert len(parsed.elements) == 4

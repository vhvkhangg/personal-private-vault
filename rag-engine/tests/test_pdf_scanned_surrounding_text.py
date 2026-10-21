"""Real scanned-PDF development regressions; never use the held-out corpus here."""

import io

import pytest
from PIL import Image, ImageDraw, ImageFont

from ppv_rag.parsers.models import ElementKind
from ppv_rag.parsers.pdf import parse_pdf


@pytest.mark.parametrize("resolution", [72, 144])
def test_scanned_table_preserves_heading_footer_and_side_text_with_original_boxes(resolution):
    image = Image.new("RGB", (1400, 900), "white")
    draw = ImageDraw.Draw(image)
    font = ImageFont.truetype("C:/Windows/Fonts/arial.ttf", 32)
    outside = [
        ("WAREHOUSE2046", (60, 70)),
        ("SIDENOTE39", (1030, 350)),
        ("FOOTER57", (60, 770)),
    ]
    ink = {}
    for text, position in outside:
        draw.text(position, text, font=font, fill="black")
        ink[text] = draw.textbbox(position, text, font=font)
    for y in (260, 400, 540, 680):
        draw.line((60, y, 940, y), fill="black", width=3)
    for x in (60, 940):
        draw.line((x, 260, x, 680), fill="black", width=3)
    draw.line((500, 400, 500, 680), fill="black", width=3)
    for text, position in [
        ("Stock", (85, 300)),
        ("A-17", (85, 440)),
        ("81", (525, 440)),
        ("B-42", (85, 580)),
        ("29", (525, 580)),
    ]:
        draw.text(position, text, font=font, fill="black")
    output = io.BytesIO()
    image.save(output, "PDF", resolution=resolution)
    parsed = parse_pdf(output.getvalue(), "surrounding-scan", 7)
    assert [e.text for e in parsed.elements] == [
        "WAREHOUSE2046",
        "Stock",
        "SIDENOTE39",
        "A-17",
        "81",
        "B-42",
        "29",
        "FOOTER57",
    ]
    assert parsed.table_count == 1
    cells = [e for e in parsed.elements if e.kind == ElementKind.TABLE_CELL]
    assert len(cells) == 5
    assert cells[0].metadata["column_span"] == 2
    assert [(e.locator["row_index"], e.locator["column_index"]) for e in cells] == [
        (0, 0),
        (1, 0),
        (1, 1),
        (2, 0),
        (2, 1),
    ]
    for element in parsed.elements:
        assert element.locator["source_key"] == "surrounding-scan"
        assert element.locator["source_version"] == 7
        assert element.locator["page"] == 1
        assert element.locator["coordinate_system"] == "PDF_TOP_LEFT"
        assert element.locator["is_ocr"] is True
        if element.text in ink:
            assert element.kind == ElementKind.PARAGRAPH
            assert "row_index" not in element.locator
            x0, y0, x1, y1 = element.locator["bbox"]
            gx0, gy0, gx1, gy1 = [v * 72 / resolution for v in ink[element.text]]
            # Detector envelopes can exceed ink; they must cover this actual source text.
            assert x0 < gx0 + 5 and y0 < gy0 + 5
            assert x1 > gx1 - 5 and y1 > gy1 - 5
            assert 0 <= x0 < x1 <= 1400 * 72 / resolution
            assert 0 <= y0 < y1 <= 900 * 72 / resolution

"""Tests for structure-first document chunker."""

from ppv_rag.chunking.chunker import StructureFirstChunker
from ppv_rag.parsers.models import DocumentElement, ElementKind, ParsedDocument
from ppv_rag.parsers.text import parse_text


def test_chunker_preserves_heading_path_and_child_attribution() -> None:
    doc = ParsedDocument(root_key="doc-1", format_type="MD", source_version=1)
    doc.elements.append(
        DocumentElement(
            kind=ElementKind.HEADING,
            text="Chapter 1",
            source_identity="doc-1",
            source_version=1,
            approved_field="content",
            locator={"line_start": 1, "line_end": 1},
            heading_path=["Chapter 1"],
        )
    )
    doc.elements.append(
        DocumentElement(
            kind=ElementKind.PARAGRAPH,
            text="First sentence of chapter 1.",
            source_identity="doc-1",
            source_version=1,
            approved_field="content",
            locator={"line_start": 2, "line_end": 2},
            heading_path=["Chapter 1"],
        )
    )
    # Transclusion with child attribution
    doc.elements.append(
        DocumentElement(
            kind=ElementKind.TRANSCLUSION,
            text="Child embed content.",
            source_identity="child-source-42",
            source_version=3,
            approved_field="content",
            locator={"line_start": 3, "line_end": 3},
            heading_path=["Chapter 1"],
        )
    )

    chunker = StructureFirstChunker(target_chars=500)
    chunks = chunker.chunk(doc)

    # Must produce separate chunks for doc-1 and child-source-42
    assert len(chunks) == 2
    c1 = chunks[0]
    assert c1.source_identity == "doc-1"
    assert "Chapter 1" in c1.content
    assert "First sentence" in c1.content
    assert c1.structure_metadata["heading_path"] == ["Chapter 1"]

    c2 = chunks[1]
    assert c2.source_identity == "child-source-42"
    assert c2.source_version == 3
    assert "Child embed content" in c2.content


def test_chunker_splits_large_paragraphs() -> None:
    doc = ParsedDocument(root_key="doc-long", format_type="TXT", source_version=1)
    long_text = "Sentence number one. " * 50  # ~1050 chars
    doc.elements.append(
        DocumentElement(
            kind=ElementKind.PARAGRAPH,
            text=long_text,
            source_identity="doc-long",
            source_version=1,
            approved_field="content",
            locator={"line_start": 1, "line_end": 10},
        )
    )

    chunker = StructureFirstChunker(target_chars=400)
    chunks = chunker.chunk(doc)
    assert len(chunks) > 1
    for c in chunks:
        assert c.source_identity == "doc-long"
        assert len(c.content) <= 500


def test_split_spans_are_exact_source_slices() -> None:
    text = "x" * 1800
    chunks = StructureFirstChunker().chunk(parse_text(text, "source", 7))
    assert [(c.locator["char_start"], c.locator["char_end"]) for c in chunks] == [
        (0, 1500),
        (1500, 1800),
    ]
    assert "".join(c.content for c in chunks) == text
    for chunk in chunks:
        assert text[chunk.locator["char_start"] : chunk.locator["char_end"]] == chunk.content


def test_same_page_blocks_keep_both_actual_geometries() -> None:
    doc = ParsedDocument("pdf", "PDF", 1)
    boxes = [[10, 10, 50, 20], [10, 80, 50, 90]]
    for index, box in enumerate(boxes):
        doc.elements.append(
            DocumentElement(
                ElementKind.PARAGRAPH,
                f"Block {index}",
                "pdf",
                1,
                "content",
                {"page": 1, "bbox": box},
            )
        )
    chunks = StructureFirstChunker().chunk(doc)
    assert [c.locator["bbox"] for c in chunks] == boxes
    assert [c.content for c in chunks] == ["Block 0", "Block 1"]


def test_sparse_sheet_does_not_invent_hidden_a_column() -> None:
    doc = ParsedDocument("sheet", "XLSX", 1)
    for row in (1, 2):
        doc.elements.append(
            DocumentElement(
                ElementKind.SPREADSHEET_ROW,
                f"Visible B{row}",
                "sheet",
                1,
                "content",
                {
                    "worksheet": "S",
                    "a1_range": f"S!B{row}:B{row}",
                    "row_start": row,
                    "row_end": row,
                    "col_start": 2,
                    "col_end": 2,
                },
            )
        )
    chunks = StructureFirstChunker().chunk(doc)
    assert [c.locator["a1_range"] for c in chunks] == ["S!B1:B1", "S!B2:B2"]


def test_chunk_budget_uses_tokenizer_and_never_loses_text() -> None:
    text = "多語言" * 300
    chunks = StructureFirstChunker(token_count=len, max_tokens=100).chunk(
        parse_text(text, "source", 1)
    )
    assert all(len(c.content) <= 100 for c in chunks)
    assert "".join(c.content for c in chunks) == text

"""Data models for document elements and mandatory provenance locators."""

from __future__ import annotations

import math
from dataclasses import dataclass, field
from enum import Enum
from typing import Any


class ElementKind(str, Enum):
    HEADING = "HEADING"
    PARAGRAPH = "PARAGRAPH"
    TABLE = "TABLE"
    TABLE_CELL = "TABLE_CELL"
    LIST_ITEM = "LIST_ITEM"
    CODE_BLOCK = "CODE_BLOCK"
    CALLOUT = "CALLOUT"
    TRANSCLUSION = "TRANSCLUSION"
    SPREADSHEET_ROW = "SPREADSHEET_ROW"
    METADATA = "METADATA"
    COMMENT = "COMMENT"


@dataclass(frozen=True)
class DocumentElement:
    kind: ElementKind
    text: str
    source_identity: str
    source_version: int
    approved_field: str
    locator: dict[str, Any]
    heading_path: list[str] = field(default_factory=list)
    metadata: dict[str, Any] = field(default_factory=dict)

    def validate_mandatory_locator(self, format_type: str) -> None:
        """Validate mandatory locator presence according to Phase 18 locator oracle."""
        fmt = format_type.upper()

        # Common source provenance checks
        src_key = self.locator.get("source_key")
        if not src_key or not isinstance(src_key, str):
            raise ValueError("Locator missing valid source_key")
        src_ver = self.locator.get("source_version")
        if src_ver is None or not isinstance(src_ver, int) or src_ver < 1:
            raise ValueError("Locator missing valid source_version (must be >= 1)")
        app_field = self.locator.get("approved_field")
        if not app_field or not isinstance(app_field, str):
            raise ValueError("Locator missing valid approved_field")

        if (
            src_key != self.source_identity
            or src_ver != self.source_version
            or app_field != self.approved_field
        ):
            raise ValueError("Locator provenance contradicts element")

        if fmt == "TXT":
            if "line_start" not in self.locator or "line_end" not in self.locator:
                raise ValueError("TXT mandatory locator missing line_start/line_end")
            ls, le = self.locator["line_start"], self.locator["line_end"]
            if not isinstance(ls, int) or not isinstance(le, int) or ls < 1 or le < ls:
                raise ValueError("TXT lines must be integers with 1 <= line_start <= line_end")
            if "char_start" not in self.locator or "char_end" not in self.locator:
                raise ValueError("TXT mandatory locator missing char_start/char_end")
            cs, ce = self.locator["char_start"], self.locator["char_end"]
            if not isinstance(cs, int) or not isinstance(ce, int) or cs < 0 or ce < cs:
                raise ValueError("TXT chars must be integers with 0 <= char_start <= char_end")

        elif fmt in {"MD", "OBSIDIAN"}:
            if "line_start" not in self.locator or "line_end" not in self.locator:
                raise ValueError("Markdown mandatory locator missing line_start/line_end")
            ls, le = self.locator["line_start"], self.locator["line_end"]
            if not isinstance(ls, int) or not isinstance(le, int) or ls < 1 or le < ls:
                raise ValueError("Markdown lines must be integers with 1 <= line_start <= line_end")

        elif fmt == "DOCX":
            has_para = (
                "paragraph_index" in self.locator
                and isinstance(self.locator["paragraph_index"], int)
                and self.locator["paragraph_index"] >= 0
            )
            has_table = (
                "table_index" in self.locator
                and isinstance(self.locator["table_index"], int)
                and self.locator["table_index"] >= 0
            )
            if not has_para and not has_table:
                raise ValueError(
                    "DOCX mandatory locator missing non-negative paragraph_index or table_index"
                )

        elif fmt == "PDF":
            if "page" not in self.locator:
                raise ValueError("PDF mandatory locator missing 1-based page")
            page = self.locator["page"]
            if not isinstance(page, int) or page < 1:
                raise ValueError("PDF page must be integer >= 1 (page 0 is invalid)")
            if "bbox" not in self.locator:
                raise ValueError("PDF mandatory locator missing bounding box [x0, y0, x1, y1]")
            bbox = self.locator["bbox"]
            if not isinstance(bbox, (list, tuple)) or len(bbox) != 4:
                raise ValueError("PDF bounding box must have 4 coordinates")
            for coord in bbox:
                if not isinstance(coord, (int, float)) or not math.isfinite(coord) or coord < 0:
                    raise ValueError(
                        f"PDF bbox coordinates must be non-negative finite numbers: {bbox}"
                    )
            if bbox[0] >= bbox[2] or bbox[1] >= bbox[3]:
                raise ValueError(
                    "PDF bbox must have x0 < x1 and y0 < y1 "
                    f"(no reversed or zero-area boxes): {bbox}"
                )

        elif fmt in {"XLSX", "XLS"}:
            ws = self.locator.get("worksheet")
            if not ws or not isinstance(ws, str) or not ws.strip():
                raise ValueError("Spreadsheet mandatory locator missing worksheet name")
            a1 = self.locator.get("a1_range")
            if not a1 or not isinstance(a1, str) or not a1.strip():
                raise ValueError("Spreadsheet mandatory locator missing a1_range")


@dataclass
class ParsedDocument:
    root_key: str
    format_type: str
    source_version: int
    elements: list[DocumentElement] = field(default_factory=list)
    warnings: list[str] = field(default_factory=list)
    page_count: int = 1
    table_count: int = 0

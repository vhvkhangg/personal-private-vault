"""Data models for document elements and mandatory provenance locators."""

from __future__ import annotations

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

        else:
            raise ValueError("Unsupported locator format")


@dataclass
class ParsedDocument:
    root_key: str
    format_type: str
    source_version: int
    elements: list[DocumentElement] = field(default_factory=list)
    warnings: list[str] = field(default_factory=list)
    page_count: int = 1
    table_count: int = 0

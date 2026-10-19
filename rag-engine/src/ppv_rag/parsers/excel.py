"""Spreadsheet parser for XLSX and legacy XLS with worksheet and A1 cell range locators."""

from __future__ import annotations

import io
from typing import BinaryIO

import openpyxl  # type: ignore[import-untyped]
import xlrd  # type: ignore[import-untyped]

from ppv_rag.parsers.models import DocumentElement, ElementKind, ParsedDocument


def _col_letter(col_idx: int) -> str:
    """Convert 1-based column index to Excel column letter (e.g. 1->A, 28->AB)."""
    result = ""
    while col_idx > 0:
        col_idx, remainder = divmod(col_idx - 1, 26)
        result = chr(65 + remainder) + result
    return result


def parse_xlsx(
    file_bytes: bytes | BinaryIO,
    root_key: str,
    source_version: int,
    approved_field: str = "content",
) -> ParsedDocument:
    """Parse XLSX workbook, skipping hidden sheets, rows, and columns."""
    stream = io.BytesIO(file_bytes) if isinstance(file_bytes, bytes) else file_bytes
    # data_only=True extracts evaluated values without executing formula code
    wb = openpyxl.load_workbook(stream, data_only=True)
    doc = ParsedDocument(root_key=root_key, format_type="XLSX", source_version=source_version)

    for sheet_name in wb.sheetnames:
        sheet = wb[sheet_name]
        # Skip hidden sheets
        if sheet.sheet_state != "visible":
            doc.warnings.append(f"Skipped hidden worksheet: {sheet_name}")
            continue

        # Collect hidden columns
        hidden_cols: set[int] = set()
        for col_letter, col_dim in sheet.column_dimensions.items():
            if getattr(col_dim, "hidden", False):
                # Convert letter to 1-based index
                c_idx = 0
                for ch in col_letter.upper():
                    c_idx = c_idx * 26 + (ord(ch) - ord("A") + 1)
                hidden_cols.add(c_idx)

        headers: list[str] = []
        r_idx = 0
        doc.table_count += 1

        if sheet.max_row > 10000:
            wb.close()
            raise BufferError("RESOURCE_LIMIT_EXCEEDED")
        for row in sheet.iter_rows(values_only=False):
            r_idx += 1
            if r_idx > 10000:
                wb.close()
                raise BufferError("RESOURCE_LIMIT_EXCEEDED")

            # Skip hidden rows
            row_dim = sheet.row_dimensions.get(r_idx)
            if row_dim is not None and getattr(row_dim, "hidden", False):
                continue

            visible_cells: list[tuple[int, str]] = []
            for c_idx, cell in enumerate(row, start=1):
                if c_idx in hidden_cols:
                    continue
                val = cell.value
                s_val = str(val).strip() if val is not None else ""
                if s_val:
                    visible_cells.append((c_idx, s_val))

            if not visible_cells:
                continue

            min_col = visible_cells[0][0]
            max_col = visible_cells[-1][0]
            start_col_letter = _col_letter(min_col)
            end_col_letter = _col_letter(max_col)
            a1_range = f"{sheet_name}!{start_col_letter}{r_idx}:{end_col_letter}{r_idx}"

            if not headers:
                headers = [val for _, val in visible_cells]
                header_joined = " | ".join(val for _, val in visible_cells)
                text = f"Worksheet [{sheet_name}] Header: {header_joined}"
                elem = DocumentElement(
                    kind=ElementKind.SPREADSHEET_ROW,
                    text=text,
                    source_identity=root_key,
                    source_version=source_version,
                    approved_field=approved_field,
                    locator={
                        "source_key": root_key,
                        "source_version": source_version,
                        "approved_field": approved_field,
                        "worksheet": sheet_name,
                        "a1_range": a1_range,
                        "row_start": r_idx,
                        "row_end": r_idx,
                        "col_start": min_col,
                        "col_end": max_col,
                        "is_header": True,
                    },
                    metadata={"worksheet": sheet_name, "row": r_idx, "is_header": True},
                )
                elem.validate_mandatory_locator("XLSX")
                doc.elements.append(elem)
            else:
                paired = [
                    f"{headers[idx]}: {val}" if idx < len(headers) and headers[idx] else val
                    for idx, (_, val) in enumerate(visible_cells)
                ]
                text = f"[{sheet_name} row {r_idx}] " + " | ".join(paired)
                elem = DocumentElement(
                    kind=ElementKind.SPREADSHEET_ROW,
                    text=text,
                    source_identity=root_key,
                    source_version=source_version,
                    approved_field=approved_field,
                    locator={
                        "source_key": root_key,
                        "source_version": source_version,
                        "approved_field": approved_field,
                        "worksheet": sheet_name,
                        "a1_range": a1_range,
                        "row_start": r_idx,
                        "row_end": r_idx,
                        "col_start": min_col,
                        "col_end": max_col,
                        "is_header": False,
                    },
                    metadata={"worksheet": sheet_name, "row": r_idx, "is_header": False},
                )
                elem.validate_mandatory_locator("XLSX")
                doc.elements.append(elem)

    wb.close()
    return doc


def parse_xls(
    file_bytes: bytes,
    root_key: str,
    source_version: int,
    approved_field: str = "content",
) -> ParsedDocument:
    """Parse legacy XLS workbook via xlrd with formatting_info=True.

    Skips hidden sheets, rows, and columns.
    """
    wb = xlrd.open_workbook(file_contents=file_bytes, formatting_info=True)
    doc = ParsedDocument(root_key=root_key, format_type="XLS", source_version=source_version)

    for sheet_idx in range(wb.nsheets):
        sheet = wb.sheet_by_index(sheet_idx)
        # xlrd visibility: 0=visible, 1=hidden, 2=very hidden
        if sheet.visibility != 0:
            doc.warnings.append(f"Skipped hidden worksheet: {sheet.name}")
            continue

        if sheet.nrows == 0:
            continue

        if sheet.nrows > 10000:
            raise BufferError("RESOURCE_LIMIT_EXCEEDED: Sheet row count exceeds 10,000")

        # Check hidden columns via colinfo_map
        hidden_cols: set[int] = set()
        colinfo_map = getattr(sheet, "colinfo_map", {})
        for col_idx, colinfo in colinfo_map.items():
            if getattr(colinfo, "hidden", 0) == 1:
                hidden_cols.add(col_idx)

        # Check hidden rows via rowinfo_map
        hidden_rows: set[int] = set()
        rowinfo_map = getattr(sheet, "rowinfo_map", {})
        for row_idx, rowinfo in rowinfo_map.items():
            if getattr(rowinfo, "hidden", 0) == 1:
                hidden_rows.add(row_idx)

        doc.table_count += 1
        headers: list[str] = []

        for r_idx in range(sheet.nrows):
            if r_idx in hidden_rows:
                continue
            one_based_row = r_idx + 1
            visible_cells: list[tuple[int, str]] = []
            for c in range(sheet.ncols):
                if c in hidden_cols:
                    continue
                v_str = str(sheet.cell_value(r_idx, c)).strip()
                if v_str:
                    visible_cells.append((c + 1, v_str))

            if not visible_cells:
                continue

            min_col = visible_cells[0][0]
            max_col = visible_cells[-1][0]
            start_col_letter = _col_letter(min_col)
            end_col_letter = _col_letter(max_col)
            range_coords = f"{start_col_letter}{one_based_row}:{end_col_letter}{one_based_row}"
            a1_range = f"{sheet.name}!{range_coords}"

            if not headers:
                headers = [val for _, val in visible_cells]
                header_joined = " | ".join(val for _, val in visible_cells)
                text = f"Worksheet [{sheet.name}] Header: {header_joined}"
                elem = DocumentElement(
                    kind=ElementKind.SPREADSHEET_ROW,
                    text=text,
                    source_identity=root_key,
                    source_version=source_version,
                    approved_field=approved_field,
                    locator={
                        "source_key": root_key,
                        "source_version": source_version,
                        "approved_field": approved_field,
                        "worksheet": sheet.name,
                        "a1_range": a1_range,
                        "row_start": one_based_row,
                        "row_end": one_based_row,
                        "col_start": min_col,
                        "col_end": max_col,
                        "is_header": True,
                    },
                    metadata={"worksheet": sheet.name, "row": one_based_row, "is_header": True},
                )
                elem.validate_mandatory_locator("XLS")
                doc.elements.append(elem)
            else:
                paired = [
                    f"{headers[idx]}: {val}" if idx < len(headers) and headers[idx] else val
                    for idx, (_, val) in enumerate(visible_cells)
                ]
                text = f"[{sheet.name} row {one_based_row}] " + " | ".join(paired)
                elem = DocumentElement(
                    kind=ElementKind.SPREADSHEET_ROW,
                    text=text,
                    source_identity=root_key,
                    source_version=source_version,
                    approved_field=approved_field,
                    locator={
                        "source_key": root_key,
                        "source_version": source_version,
                        "approved_field": approved_field,
                        "worksheet": sheet.name,
                        "a1_range": a1_range,
                        "row_start": one_based_row,
                        "row_end": one_based_row,
                        "col_start": min_col,
                        "col_end": max_col,
                        "is_header": False,
                    },
                    metadata={"worksheet": sheet.name, "row": one_based_row, "is_header": False},
                )
                elem.validate_mandatory_locator("XLS")
                doc.elements.append(elem)

    return doc

"""Structure-first document chunker with mandatory provenance and source boundaries."""

from __future__ import annotations

from bisect import bisect_left
from collections.abc import Callable
from dataclasses import dataclass, field
from typing import Any

from ppv_rag.limits import checkpoint
from ppv_rag.parsers.models import DocumentElement, ParsedDocument


@dataclass(frozen=True)
class DocumentChunk:
    chunk_index: int
    content: str
    source_identity: str
    source_version: int
    locator: dict[str, Any]
    structure_metadata: dict[str, Any] = field(default_factory=dict)


class StructureFirstChunker:
    """Structure-first chunker preserving heading paths, tables,
    worksheets, and child provenance.
    """

    def __init__(
        self,
        target_chars: int = 1500,
        token_count: Callable[[str], int] | None = None,
        max_tokens: int = 512,
    ) -> None:
        if target_chars < 1 or max_tokens < 1:
            raise ValueError("Chunk budgets must be positive")
        self.target_chars = target_chars
        self.token_count = token_count
        self.max_tokens = max_tokens

    def _fits(self, text: str) -> bool:
        return len(text) <= self.target_chars and (
            self.token_count is None or self.token_count(text) <= self.max_tokens
        )

    def chunk(self, parsed: ParsedDocument) -> list[DocumentChunk]:
        chunks: list[DocumentChunk] = []
        chunk_idx = 0

        current_elements: list[DocumentElement] = []
        current_len = 0
        current_source = ""
        current_revision = 0
        current_role: tuple[Any, ...] = ()
        current_worksheet = ""
        current_page: int | None = None
        current_headings: list[str] = []

        def flush_chunk() -> None:
            nonlocal chunk_idx, current_elements, current_len
            if not current_elements:
                return

            text_parts = [e.text for e in current_elements]
            chunk_text = "\n\n".join(text_parts)
            if not chunk_text:
                current_elements = []
                current_len = 0
                return

            first_elem = current_elements[0]
            last_elem = current_elements[-1]
            merged_locator = dict(first_elem.locator)

            if "line_end" in last_elem.locator:
                merged_locator["line_end"] = last_elem.locator["line_end"]
            if "char_end" in last_elem.locator:
                merged_locator["char_end"] = last_elem.locator["char_end"]

            chunk = DocumentChunk(
                chunk_index=chunk_idx,
                content=chunk_text,
                source_identity=first_elem.source_identity,
                source_version=first_elem.source_version,
                locator=merged_locator,
                structure_metadata={
                    "heading_path": list(first_elem.heading_path),
                    "element_kinds": [e.kind.value for e in current_elements],
                    "element_count": len(current_elements),
                    "constituent_locators": [dict(e.locator) for e in current_elements],
                    "constituent_metadata": [dict(e.metadata) for e in current_elements],
                },
            )
            chunks.append(chunk)
            chunk_idx += 1
            current_elements = []
            current_len = 0

        for elem in parsed.elements:
            checkpoint()
            elem_len = len(elem.text)
            elem_ws = elem.locator.get("worksheet", "")
            elem_page = elem.locator.get("page")

            # Strict boundaries:
            # 1. Source identity change
            # 2. Worksheet change (MUST NEVER group across sheets)
            # 3. Page change when current length is sufficient
            # 4. Heading path change when current length > 300
            kind = elem.locator.get("original_kind", elem.kind.value)
            element_role = "TEXT" if kind in {"HEADING", "PARAGRAPH"} else kind
            role = (
                elem.approved_field,
                element_role,
                elem.locator.get("section"),
                elem.metadata.get("is_hidden"),
                elem.metadata.get("has_hidden_runs"),
                elem.metadata.get("callout_type"),
            )
            diff_src = bool(
                current_source
                and (
                    elem.source_identity != current_source
                    or elem.source_version != current_revision
                    or role != current_role
                )
            )
            diff_ws = bool(current_worksheet and elem_ws != current_worksheet)
            diff_page = bool(
                current_page is not None and elem_page is not None and elem_page != current_page
            )
            diff_h = bool(current_elements and elem.heading_path != current_headings)

            # Keep geometries, exact text spans and sparse sheet addresses separate.
            # A bounding rectangle/range can include unselected or hidden content.
            exact_address = any(
                k in elem.locator
                for k in ("bbox", "worksheet", "char_start", "structural_path", "container_part")
            )
            if diff_src or diff_ws or diff_page or diff_h or exact_address:
                flush_chunk()

            current_source = elem.source_identity
            current_revision = elem.source_version
            current_role = role
            current_worksheet = elem_ws
            current_page = elem_page
            current_headings = elem.heading_path

            # If a single element exceeds target_chars, chunk it independently
            if not self._fits(elem.text):
                flush_chunk()
                sub_chunks = self._split_large_text(elem.text)
                d17 = elem.locator.get("profile") == "ppv-obsidian-source-v1"
                line_breaks = [i for i, c in enumerate(elem.text) if c == "\n"] if d17 else []
                for start, end in sub_chunks:
                    sub_text = elem.text[start:end]
                    sub_loc = dict(elem.locator)
                    if "char_start" in elem.locator:
                        sub_loc["char_start"] = elem.locator["char_start"] + start
                        sub_loc["char_end"] = elem.locator["char_start"] + end
                        if "line_start" in elem.locator:
                            if d17:
                                sub_loc["line_start"] = elem.locator["line_start"] + bisect_left(
                                    line_breaks, start
                                )
                                sub_loc["line_end"] = elem.locator["line_start"] + bisect_left(
                                    line_breaks, max(start, end - 1)
                                )
                            else:
                                sub_loc["line_start"] = elem.locator["line_start"] + elem.text[
                                    :start
                                ].count("\n")
                                sub_loc["line_end"] = sub_loc["line_start"] + sub_text.count("\n")
                    metadata = dict(elem.metadata)
                    if elem.locator.get("profile") == "ppv-obsidian-source-v1":
                        from ppv_rag.parsers.obsidian import slice_metadata

                        metadata = slice_metadata(
                            metadata,
                            sub_loc["char_start"],
                            sub_loc["char_end"],
                            include_tree=start == 0,
                        )
                    chunks.append(
                        DocumentChunk(
                            chunk_index=chunk_idx,
                            content=sub_text,
                            source_identity=elem.source_identity,
                            source_version=elem.source_version,
                            locator=sub_loc,
                            structure_metadata={
                                "heading_path": list(elem.heading_path),
                                "element_kinds": [elem.kind.value],
                                "is_split": True,
                                "element_text_start": start,
                                "element_text_end": end,
                                "constituent_metadata": [metadata],
                            },
                        )
                    )
                    chunk_idx += 1
                continue

            candidate = "\n\n".join([e.text for e in current_elements] + [elem.text])
            if not self._fits(candidate):
                flush_chunk()

            current_elements.append(elem)
            current_len += elem_len + 2

        flush_chunk()
        return chunks

    def _split_large_text(self, text: str) -> list[tuple[int, int]]:
        """Return actual offsets; never infer them from stripped slice lengths."""
        pieces: list[tuple[int, int]] = []
        start = 0
        text_len = len(text)
        while start < text_len:
            checkpoint()
            end = min(start + self.target_chars, text_len)
            while end > start and not self._fits(text[start:end]):
                end = start + (end - start) // 2
            if end == start:
                raise ValueError("A single character exceeds the tokenizer budget")
            if end < text_len:
                split_point = text.rfind("\n", start, end)
                if split_point == -1 or split_point < start + 200:
                    split_point = text.rfind(". ", start, end)
                if split_point != -1 and split_point > start + 200:
                    end = split_point + 1
            pieces.append((start, end))
            start = end
            if start >= text_len:
                break
        return pieces

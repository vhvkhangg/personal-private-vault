"""Markdown and Obsidian parser with frontmatter, headings, transclusions, and block refs."""

from __future__ import annotations

import re
from dataclasses import dataclass, replace
from typing import Any

import yaml  # type: ignore[import-untyped]

from ppv_rag.limits import MAX_TEXT_CHARS, check_text, checkpoint
from ppv_rag.parsers.models import DocumentElement, ElementKind, ParsedDocument


class RestrictiveContentError(ValueError):
    """Raised when document metadata contains restrictive privacy or NSFW tags."""


ALLOWLISTED_FRONTMATTER_FIELDS = {
    "title",
    "tags",
    "aliases",
    "date",
    "description",
    "author",
    "category",
    "summary",
}

RESTRICTIVE_FRONTMATTER_KEYS = {
    "private",
    "nsfw",
    "confidential",
    "secret",
    "restricted",
    "classification",
}

RE_FRONTMATTER = re.compile(r"\A---\r?\n(.*?)\r?\n---\r?\n", re.DOTALL)
RE_HEADING = re.compile(r"^(#{1,6})\s+(.+)$")
RE_CALLOUT_START = re.compile(r"^>\s*\[!([a-zA-Z0-9_-]+)\](.*)$")
RE_TRANSCLUSION = re.compile(r"!\[\[(.*?)\]\]")
RE_WIKILINK = re.compile(r"\[\[(.*?)\]\]")
RE_BLOCK_REF = re.compile(r"\s+\^([a-zA-Z0-9-]+)$")
RE_OBSIDIAN_COMMENT = re.compile(r"%%.*?%%", re.DOTALL)
RE_HTML_COMMENT = re.compile(r"<!--.*?-->", re.DOTALL)


@dataclass(frozen=True)
class ApprovedDependency:
    source_key: str
    source_version: int
    approved_field: str
    text_content: str
    approved_metadata_fields: frozenset[str] = frozenset()


def _preserve_newlines(match: re.Match[str]) -> str:
    return "\n" * match.group(0).count("\n")


def strip_comments(text: str) -> str:
    """Strip internal Obsidian and HTML comments while preserving original line structure."""
    text = RE_OBSIDIAN_COMMENT.sub(_preserve_newlines, text)
    return RE_HTML_COMMENT.sub(_preserve_newlines, text)


class _FrontmatterLoader(yaml.SafeLoader):
    def construct_mapping(self, node: Any, deep: bool = False) -> dict[str, Any]:
        result: dict[str, Any] = {}
        for key_node, value_node in node.value:
            key = str(self.construct_object(key_node, deep=deep)).strip().lower()
            if key in result:
                raise ValueError("Duplicate frontmatter key")
            result[key] = self.construct_object(value_node, deep=deep)
        return result


def parse_frontmatter(
    yaml_text: str, approved_fields: frozenset[str] = frozenset()
) -> tuple[dict[str, Any], list[str]]:
    """Restrictive metadata is never overridden by an inclusion allowlist."""
    try:
        parsed = yaml.load(yaml_text, Loader=_FrontmatterLoader)
    except yaml.YAMLError as failure:
        raise ValueError("Invalid frontmatter") from failure
    if not isinstance(parsed, dict):
        raise ValueError("Frontmatter must be a mapping")
    data: dict[str, Any] = {}
    warnings: list[str] = []
    for key, value in parsed.items():
        if key in RESTRICTIVE_FRONTMATTER_KEYS:
            safe = (
                str(value).lower() in {"non_private", "non-private", "public"}
                if key == "classification"
                else value is False
            )
            if not safe:
                raise RestrictiveContentError("Restrictive or unknown frontmatter classification")
        elif key in ALLOWLISTED_FRONTMATTER_FIELDS and key in approved_fields:
            data[key] = value
        else:
            warnings.append(f"Excluded unapproved frontmatter field: {key}")
    return data, warnings


def _heading_matches(anchor: str, path: list[str]) -> bool:
    requested = [part.strip().casefold() for part in anchor.split("#")]
    actual = [part.strip().casefold() for part in path]
    return any(actual[i : i + len(requested)] == requested for i in range(len(actual)))


def parse_markdown(
    content: str,
    root_key: str,
    source_version: int,
    approved_field: str = "content",
    dependencies: dict[str, ApprovedDependency] | None = None,
    approved_metadata_fields: frozenset[str] = frozenset(),
    _ancestry: tuple[str, ...] = (),
    *,
    whole_file: bool = False,
    _expansion: list[int] | None = None,
) -> ParsedDocument:
    """Parse Markdown/Obsidian document into structured elements with provenance."""
    if whole_file:
        from ppv_rag.parsers.obsidian import parse_obsidian

        return parse_obsidian(content, root_key, source_version, approved_field, dependencies)
    check_text(content)
    expansion = _expansion if _expansion is not None else [0]
    expansion[0] += len(content)
    if expansion[0] > MAX_TEXT_CHARS:
        raise BufferError("RESOURCE_LIMIT_EXCEEDED: transclusion expansion")
    if root_key in _ancestry:
        raise ValueError("Cyclic transclusion")
    if len(_ancestry) >= 8:
        raise ValueError("Transclusion depth exceeded")
    doc = ParsedDocument(root_key=root_key, format_type="MD", source_version=source_version)
    dep_map = dependencies or {}

    if not _ancestry:
        bindings: dict[tuple[str, int, str], str] = {}
        for child in dep_map.values():
            key = (child.source_key, child.source_version, child.approved_field)
            if key in bindings and bindings[key] != child.text_content:
                raise ValueError("Conflicting child revision payload")
            bindings[key] = child.text_content

    raw_text = content
    # 1. Frontmatter
    fm_match = RE_FRONTMATTER.match(raw_text)
    line_offset = 1
    if raw_text.startswith("---\n") or raw_text.startswith("---\r\n"):
        if not fm_match:
            raise ValueError("Malformed frontmatter")
    if fm_match:
        fm_raw = fm_match.group(1)
        # Validate syntax/duplicates even when every in-file field is authorized.
        if whole_file:
            # Reject aliases before construction to bound YAML expansion.
            if any(isinstance(event, yaml.AliasEvent) for event in yaml.parse(fm_raw)):
                raise ValueError("Frontmatter aliases are unsupported")
            fm_data = yaml.load(fm_raw, Loader=_FrontmatterLoader)
            if not isinstance(fm_data, dict):
                raise ValueError("Frontmatter must be a mapping")
            fm_warnings: list[str] = []
        else:
            fm_data, fm_warnings = parse_frontmatter(fm_raw, approved_metadata_fields)
        doc.warnings.extend(fm_warnings)
        entries = (
            [(approved_field, fm_match.group(0).rstrip())]
            if whole_file
            else [(key, f"{key}: {value}") for key, value in sorted(fm_data.items())]
        )
        for metadata_field, metadata_value in entries:
            fm_lines = fm_match.group(0).count("\n")
            element = DocumentElement(
                ElementKind.PARAGRAPH,
                str(metadata_value),
                root_key,
                source_version,
                metadata_field,
                {
                    "source_key": root_key,
                    "source_version": source_version,
                    "approved_field": metadata_field,
                    "line_start": 1,
                    "line_end": max(1, fm_lines),
                    "section": "frontmatter",
                },
                metadata={"frontmatter": {metadata_field: metadata_value}},
            )
            element.validate_mandatory_locator("MD")
            doc.elements.append(element)
        char_offset = fm_match.end()
        line_offset = fm_match.group(0).count("\n") + 1
        body_text = raw_text[char_offset:]
    else:
        body_text = raw_text

    cleaned_body = body_text if whole_file else strip_comments(body_text)
    lines = cleaned_body.splitlines()

    body_line_offsets = [0]
    for original_line in body_text.splitlines(keepends=True):
        body_line_offsets.append(body_line_offsets[-1] + len(original_line))
    inert_ranges: list[tuple[int, int]] = (
        [
            m.span()
            for pattern in (RE_OBSIDIAN_COMMENT, RE_HTML_COMMENT)
            for m in pattern.finditer(body_text)
        ]
        if whole_file
        else []
    )

    heading_stack: list[tuple[int, str]] = []  # (level, text)
    current_para: list[str] = []
    para_start_line = line_offset
    in_code_block = False
    code_block_lines: list[str] = []
    code_block_start = 0
    code_fence = ""

    def current_heading_path() -> list[str]:
        return [h[1] for h in heading_stack]

    def flush_paragraph(end_line: int) -> None:
        nonlocal current_para, para_start_line
        if not current_para:
            return
        para_text = (
            "\n".join(current_para)
            if whole_file
            else " ".join(p_line.strip() for p_line in current_para if p_line.strip())
        ).strip()
        current_para = []
        if not para_text:
            return

        # Check for block ref at end
        block_id = None
        block_match = RE_BLOCK_REF.search(para_text)
        if block_match:
            block_id = block_match.group(1)
            para_text = para_text[: block_match.start()].strip()

        # Resolve every selected child recursively under its own field/version grant.
        transclusions = []
        source_cursor = body_line_offsets[para_start_line - line_offset]
        for match in RE_TRANSCLUSION.finditer(para_text):
            if whole_file:
                original_start = body_text.find(match.group(0), source_cursor)
                if original_start < 0:
                    raise ValueError("Embed does not map to original source")
                source_cursor = original_start + len(match.group(0))
                if any(a <= original_start < b for a, b in inert_ranges):
                    continue
            transclusions.append(match)
        if transclusions:
            cursor = 0
            for match in transclusions:
                prefix = para_text[cursor : match.start()].strip()
                if prefix:
                    doc.elements.append(
                        DocumentElement(
                            ElementKind.PARAGRAPH,
                            prefix,
                            root_key,
                            source_version,
                            approved_field,
                            {
                                "source_key": root_key,
                                "source_version": source_version,
                                "approved_field": approved_field,
                                "line_start": para_start_line,
                                "line_end": end_line,
                            },
                            current_heading_path(),
                        )
                    )
                target = match.group(1).strip()
                binding, _, display_alias = target.partition("|")
                base_target, _, anchor = binding.partition("#")
                base_target = base_target.strip()
                if base_target not in dep_map:
                    raise PermissionError("Unauthorized or unapproved transclusion dependency")
                child = dep_map[base_target]
                child_doc = parse_markdown(
                    child.text_content,
                    child.source_key,
                    child.source_version,
                    child.approved_field,
                    dep_map,
                    child.approved_metadata_fields,
                    _ancestry + (root_key,),
                    whole_file=whole_file,
                    _expansion=expansion,
                )
                selected = child_doc.elements
                if anchor.startswith("^"):
                    selected = [e for e in selected if e.locator.get("block_id") == anchor[1:]]
                elif anchor:
                    selected = [
                        e
                        for e in selected
                        if _heading_matches(anchor, e.heading_path)
                        or any(
                            _heading_matches(anchor, edge.get("heading_path", []))
                            for edge in e.locator.get("dependency_edges", [])
                            if edge.get("parent") == child.source_key
                        )
                    ]
                if not selected:
                    raise ValueError("Missing transclusion anchor or empty child")
                for element in selected:
                    locator = dict(element.locator)
                    locator.update(
                        {
                            "parent_source_key": root_key,
                            "parent_line_start": para_start_line,
                            "parent_line_end": end_line,
                            "transclusion_target": binding,
                            "display_alias": display_alias,
                            "original_kind": element.locator.get(
                                "original_kind", element.kind.value
                            ),
                            "dependency_edges": [
                                {
                                    "parent": root_key,
                                    "parent_version": source_version,
                                    "heading_path": current_heading_path(),
                                    "child": child.source_key,
                                    "child_version": child.source_version,
                                    "line_start": para_start_line,
                                    "line_end": end_line,
                                    "target": binding,
                                },
                                *element.locator.get("dependency_edges", []),
                            ],
                        }
                    )
                    doc.elements.append(
                        replace(element, kind=ElementKind.TRANSCLUSION, locator=locator)
                    )
                cursor = match.end()
            suffix = para_text[cursor:].strip()
            if suffix:
                doc.elements.append(
                    DocumentElement(
                        ElementKind.PARAGRAPH,
                        suffix,
                        root_key,
                        source_version,
                        approved_field,
                        {
                            "source_key": root_key,
                            "source_version": source_version,
                            "approved_field": approved_field,
                            "line_start": para_start_line,
                            "line_end": end_line,
                        },
                        current_heading_path(),
                    )
                )
            return

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
                "line_start": para_start_line,
                "line_end": max(para_start_line, end_line),
                "heading_path": current_heading_path(),
                **({"block_id": block_id} if block_id else {}),
            },
            heading_path=current_heading_path(),
            metadata={"block_id": block_id} if block_id else {},
        )
        elem.validate_mandatory_locator("MD")
        doc.elements.append(elem)

    i = 0
    while i < len(lines):
        checkpoint()
        actual_line_no = line_offset + i
        line = lines[i]
        stripped = line.strip()

        # Code block fence
        fence = re.match(r"^(`{3,}|~{3,})(.*)$", stripped)
        if fence and (
            not in_code_block
            or (
                fence.group(1)[0] == code_fence[0]
                and len(fence.group(1)) >= len(code_fence)
                and not fence.group(2).strip()
            )
        ):
            if not in_code_block:
                flush_paragraph(actual_line_no - 1)
                in_code_block = True
                code_fence = fence.group(1)
                code_block_lines = []
                code_block_start = actual_line_no
            else:
                in_code_block = False
                code_text = "\n".join(code_block_lines)
                elem = DocumentElement(
                    kind=ElementKind.CODE_BLOCK,
                    text=code_text,
                    source_identity=root_key,
                    source_version=source_version,
                    approved_field=approved_field,
                    locator={
                        "source_key": root_key,
                        "source_version": source_version,
                        "approved_field": approved_field,
                        "line_start": code_block_start,
                        "line_end": actual_line_no,
                        "heading_path": current_heading_path(),
                    },
                    heading_path=current_heading_path(),
                )
                elem.validate_mandatory_locator("MD")
                doc.elements.append(elem)
            i += 1
            continue

        if in_code_block:
            code_block_lines.append(line)
            i += 1
            continue

        # Heading
        heading_match = RE_HEADING.match(line)
        if heading_match:
            flush_paragraph(actual_line_no - 1)
            level = len(heading_match.group(1))
            heading_title = heading_match.group(2).strip()
            while heading_stack and heading_stack[-1][0] >= level:
                heading_stack.pop()
            heading_stack.append((level, heading_title))
            elem = DocumentElement(
                kind=ElementKind.HEADING,
                text=heading_title,
                source_identity=root_key,
                source_version=source_version,
                approved_field=approved_field,
                locator={
                    "source_key": root_key,
                    "source_version": source_version,
                    "approved_field": approved_field,
                    "line_start": actual_line_no,
                    "line_end": actual_line_no,
                    "heading_level": level,
                    "heading_path": current_heading_path(),
                },
                heading_path=current_heading_path(),
                metadata={"level": level},
            )
            elem.validate_mandatory_locator("MD")
            doc.elements.append(elem)
            para_start_line = actual_line_no + 1
            i += 1
            continue

        # Callout: > [!NOTE] ...
        callout_match = RE_CALLOUT_START.match(stripped)
        if callout_match:
            flush_paragraph(actual_line_no - 1)
            callout_type = callout_match.group(1).upper()
            callout_lines = [callout_match.group(2).strip()]
            c_start = actual_line_no
            j = i + 1
            while j < len(lines) and lines[j].strip().startswith(">"):
                sub_line = lines[j].strip()[1:].strip()
                callout_lines.append(sub_line)
                j += 1
            c_end = line_offset + j - 1
            callout_text = "\n".join(c_line for c_line in callout_lines if c_line)
            if RE_TRANSCLUSION.search(callout_text):
                current_para = [callout_text]
                para_start_line = c_start
                before = len(doc.elements)
                flush_paragraph(c_end)
                for index in range(before, len(doc.elements)):
                    item = doc.elements[index]
                    doc.elements[index] = replace(
                        item, metadata={**item.metadata, "callout_type": callout_type}
                    )
                i = j
                para_start_line = line_offset + i
                continue
            elem = DocumentElement(
                kind=ElementKind.CALLOUT,
                text=callout_text,
                source_identity=root_key,
                source_version=source_version,
                approved_field=approved_field,
                locator={
                    "source_key": root_key,
                    "source_version": source_version,
                    "approved_field": approved_field,
                    "line_start": c_start,
                    "line_end": max(c_start, c_end),
                    "heading_path": current_heading_path(),
                    "callout_type": callout_type,
                },
                heading_path=current_heading_path(),
                metadata={"callout_type": callout_type},
            )
            elem.validate_mandatory_locator("MD")
            doc.elements.append(elem)
            i = j
            para_start_line = line_offset + i
            continue

        # Blank line flushes paragraph
        if not stripped:
            flush_paragraph(actual_line_no - 1)
            para_start_line = actual_line_no + 1
            i += 1
            continue

        # Regular paragraph line
        if not current_para:
            para_start_line = actual_line_no
        current_para.append(line)
        i += 1

    if in_code_block:
        doc.elements.append(
            DocumentElement(
                ElementKind.CODE_BLOCK,
                "\n".join(code_block_lines),
                root_key,
                source_version,
                approved_field,
                {
                    "source_key": root_key,
                    "source_version": source_version,
                    "approved_field": approved_field,
                    "line_start": code_block_start,
                    "line_end": line_offset + len(lines) - 1,
                },
                current_heading_path(),
            )
        )
    flush_paragraph(line_offset + len(lines) - 1)
    check_text("\n".join(e.text for e in doc.elements))
    return doc

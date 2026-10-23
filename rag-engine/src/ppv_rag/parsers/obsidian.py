"""D17 whole-file source extraction. Rendering, file discovery and plugin execution are absent."""

from __future__ import annotations

import bisect
import hashlib
import json
import math
import re
from dataclasses import dataclass, replace
from typing import TYPE_CHECKING, Any, cast
from urllib.parse import unquote, urlsplit

import yaml  # type: ignore[import-untyped]
from markdown_it import MarkdownIt
from markdown_it.common.html_re import HTML_TAG_RE
from markdown_it.ruler import Rule, RuleOptionsType, Ruler
from markdown_it.rules_block import StateBlock
from markdown_it.rules_block.html_block import html_block
from markdown_it.rules_inline import StateInline
from markdown_it.token import Token
from mdit_py_plugins.dollarmath import dollarmath_plugin
from mdit_py_plugins.footnote import footnote_plugin
from mdit_py_plugins.gfm_autolink import gfm_autolink_plugin

from ppv_rag.limits import MAX_TEXT_CHARS, check_text, checkpoint
from ppv_rag.parsers.models import DocumentElement, ElementKind, ParsedDocument
from ppv_rag.parsers.obsidian_html import HtmlFailure, html_extent, html_source

if TYPE_CHECKING:
    from ppv_rag.parsers.markdown import ApprovedDependency

PROFILE = "ppv-obsidian-source-v1"
MAX_NODES = 50_000
MAX_DEPTH = 8
_DYNAMIC = {"dataview", "dataviewjs", "query", "search", "tasks", "templater"}


class MarkdownFailure(ValueError):
    """Sanitized whole-document failure; no partial ParsedDocument is returned."""

    def __init__(self, code: str) -> None:
        self.code = code
        super().__init__(code)


class _Properties(yaml.SafeLoader):
    def construct_mapping(self, node: Any, deep: bool = False) -> dict[str, Any]:
        result: dict[str, Any] = {}
        for key_node, value_node in node.value:
            key = self.construct_object(key_node, deep=deep)
            if not isinstance(key, str) or key in result:
                raise MarkdownFailure("MD_INVALID_PROPERTIES")
            result[key] = self.construct_object(value_node, deep=deep)
        return result


# Keep the loader local. YAML 1.1's yes/on booleans, octal-looking integers and
# sexagesimal numbers must not silently change Obsidian property values.
_Properties.yaml_implicit_resolvers = {
    key: [
        (tag, pattern)
        for tag, pattern in resolvers
        if tag not in {"tag:yaml.org,2002:bool", "tag:yaml.org,2002:int", "tag:yaml.org,2002:float"}
    ]
    for key, resolvers in yaml.SafeLoader.yaml_implicit_resolvers.items()
}
_Properties.add_implicit_resolver(
    "tag:yaml.org,2002:bool", re.compile(r"^(?:true|True|TRUE|false|False|FALSE)$"), list("tTfF")
)
_Properties.add_implicit_resolver(
    "tag:yaml.org,2002:int",
    re.compile(r"^[-+]?(?:[0-9][0-9_]*|0o[0-7_]+|0x[0-9a-fA-F_]+|0b[01_]+)$"),
    list("-+0123456789"),
)
_Properties.add_implicit_resolver(
    "tag:yaml.org,2002:float",
    re.compile(
        r"^[-+]?(?:(?:[0-9][0-9_]*\.[0-9_]*|\.[0-9_]+)(?:[eE][-+]?[0-9]+)?"
        r"|[0-9][0-9_]*[eE][-+]?[0-9]+|\.(?:inf|Inf|INF|nan|NaN|NAN))$"
    ),
    list("-+0123456789."),
)


def _property_integer(loader: Any, node: Any) -> int:
    value = str(loader.construct_scalar(node)).replace("_", "")
    sign = -1 if value.startswith("-") else 1
    digits = value.lstrip("+-")
    base = (
        16
        if digits.startswith("0x")
        else 8
        if digits.startswith("0o")
        else 2
        if digits.startswith("0b")
        else 10
    )
    return sign * int(digits, base)


_Properties.add_constructor("tag:yaml.org,2002:int", _property_integer)


def _escaped(text: str, position: int) -> bool:
    before = position
    while before and text[before - 1] == "\\":
        before -= 1
    return (position - before) % 2 == 1


def _extension(state: StateInline, silent: bool) -> bool:
    checkpoint()
    start, text = state.pos, state.src
    embed = text.startswith("![[", start)
    wiki = text.startswith("[[", start)
    if embed or wiki:
        end = text.find("]]", start + (3 if embed else 2))
        if end < 0:
            if embed:
                raise MarkdownFailure("MD_MALFORMED_EMBED")
            return False
        payload = text[start + (3 if embed else 2) : end]
        if not payload.strip() or "\n" in payload:
            raise MarkdownFailure("MD_MALFORMED_EMBED" if embed else "MD_INVALID_LINK")
        if not silent:
            token = state.push("obsidian_embed" if embed else "obsidian_link", "", 0)
            binding, sep, alias = payload.partition("|")
            target, anchor_sep, anchor = binding.partition("#")
            token.content = text[start : end + 2]
            token.meta = dict(
                target=target.strip(),
                anchor=anchor if anchor_sep else "",
                alias=alias if sep else None,
            )
        state.pos = end + 2
        return True
    if text.startswith("==", start):
        end = text.find("==", start + 2)
        if end > start + 2:
            if not silent:
                token = state.push("obsidian_highlight", "mark", 0)
                token.content = text[start + 2 : end]
                colors = {
                    "🔴": "red",
                    "🟠": "orange",
                    "🟡": "yellow",
                    "🟢": "green",
                    "🔵": "blue",
                    "🟣": "purple",
                }
                token.meta["color"] = colors.get(token.content[:1], "theme_default")
                token.children = []
                state.md.inline.parse(token.content, state.md, state.env, token.children)
            state.pos = end + 2
            return True
    if text[start : start + 1] == "#" and (start == 0 or text[start - 1].isspace()):
        match = re.match(r"#[\w/-]+", text[start:], flags=re.UNICODE)
        if match and any(c.isalpha() or c in "_-" for c in match.group()[1:]):
            if not silent:
                token = state.push("obsidian_tag", "", 0)
                token.content = match.group()
            state.pos += len(match.group())
            return True
    if text[start : start + 1] == "^" and (start == 0 or text[start - 1].isspace()):
        match = re.match(r"\^([A-Za-z0-9-]+)\s*$", text[start:])
        if match:
            if not silent:
                token = state.push("obsidian_block_id", "", 0)
                token.content = match.group(1)
            state.pos = len(text)
            return True
    if text.startswith(("<%", "$="), start):
        raise MarkdownFailure("MD_UNSUPPORTED_DYNAMIC_CONTENT")
    return False


def _cell_columns(line: str) -> list[tuple[int, int]]:
    """GFM pipe escaping, retaining original raw columns rather than unescaped lengths."""
    start, end = len(line) - len(line.lstrip()), len(line.rstrip())
    if line[start : start + 1] == "|":
        start += 1
    if end > start and line[end - 1] == "|" and (end < 2 or line[end - 2] != "\\"):
        end -= 1
    spans = []
    cursor = start
    for index in range(start, end):
        if line[index] == "|" and (index == 0 or line[index - 1] != "\\"):
            spans.append((cursor, index))
            cursor = index + 1
    spans.append((cursor, end))
    return spans


def _html_literal(state: StateInline, silent: bool) -> bool:
    if state.src[state.pos] != "<":
        return False
    if not re.match(r"<(?:[A-Za-z][\w-]*(?:\s|/?>)|!--)", state.src[state.pos :]):
        return False
    remaining = state.src[state.pos : state.posMax]
    valid_comment = remaining.startswith("<!--") and (
        remaining.startswith(("<!-->", "<!--->")) or remaining.find("-->", 4) >= 0
    )
    if not valid_comment and not HTML_TAG_RE.match(remaining):
        return False
    length = html_extent(state.src[state.pos :])
    if not silent:
        token = state.push("html_inline", "", 0)
        token.content = state.src[state.pos : state.pos + length]
    state.pos += length
    return True


def _html_across_blocks(state: StateBlock, start: int, end: int, silent: bool) -> bool:
    if not html_block(state, start, end, silent):
        return False
    if silent:
        return True
    token = state.tokens[-1]
    # The Markdown parser has already adjusted indentation and quote prefixes for
    # this owning container. Blank lines may continue native HTML, but a nonempty
    # outdent (including a sibling list marker or negative quote sentinel) cannot.
    container_end = end
    for line in range(start + 1, end):
        checkpoint()
        if not state.isEmpty(line) and state.sCount[line] < state.blkIndent:
            container_end = line
            break
    remaining = state.getLines(start, container_end, state.blkIndent, True)
    # An open inert container owns its remaining source through EOF, including blank
    # blocks. Native optional closures are handled by the HTML scanner, not Markdown.
    length = html_extent(remaining)
    if length:
        consumed = remaining[:length]
        next_line = start + consumed.count("\n") + int(not consumed.endswith("\n"))
        if next_line > state.line:
            state.line = min(next_line, container_end)
            token.map = [start, state.line]
            token.content = state.getLines(start, state.line, state.blkIndent, True)
    return True


def _html_rules(md: MarkdownIt) -> MarkdownIt:
    md.block.ruler.at(
        "html_block", _html_across_blocks, {"alt": ["paragraph", "reference", "blockquote"]}
    )
    md.inline.ruler.before("html_inline", "obsidian_html_literal", _html_literal)
    return md


def _gfm_inline(md: MarkdownIt) -> None:
    """Reuse GFM scanners, retaining rule origin and an underscore email local part."""
    for rule in list(md.inline.ruler.__rules__):
        if not rule.name.startswith("gfm_autolink_"):
            continue
        original, name = rule.fn, rule.name

        def checked(
            state: StateInline, silent: bool, _fn: Any = original, _name: str = name
        ) -> bool:
            character = state.src[state.pos]
            if (
                character
                != {
                    "gfm_autolink_www": "w",
                    "gfm_autolink_protocol": ":",
                    "gfm_autolink_email": "@",
                }[_name]
            ):
                return False
            checkpoint()
            count = len(state.tokens)
            pending = state.pending
            changed: list[tuple[Token, str]] = []
            delimiters: list[tuple[Any, bool, bool]] = []
            if _name == "gfm_autolink_email" and state.src[state.pos : state.pos + 1] == "@":
                start = state.pos
                while (
                    start
                    and state.src[start - 1].isascii()
                    and (state.src[start - 1].isalnum() or state.src[start - 1] in ".+-_")
                ):
                    start -= 1
                full = state.src[start : state.pos]
                suffix = re.search(r"[A-Za-z0-9.+_-]+$", pending)
                present = len(suffix.group()) if suffix else 0
                needed = full[: len(full) - present]
                modifications: list[tuple[int, Token, str]] = []
                for index in range(len(state.tokens) - 1, -1, -1):
                    if not needed:
                        break
                    token = state.tokens[index]
                    if token.type != "text" or not token.content:
                        break
                    take = min(len(needed), len(token.content))
                    if token.content[-take:] != needed[-take:]:
                        break
                    modifications.append((index, token, token.content[:-take]))
                    needed = needed[:-take]
                if modifications and not needed:
                    positions = {i for i, _, _ in modifications}
                    for _, token, value in modifications:
                        changed.append((token, token.content))
                        token.content = value
                    for delimiter in state.delimiters:
                        if delimiter.token in positions:
                            delimiters.append((delimiter, delimiter.open, delimiter.close))
                            delimiter.open = delimiter.close = False
                    state.pending = pending[: len(pending) - present] + full
            matched = _fn(state, silent)
            if not matched or silent:
                for token, value in changed:
                    token.content = value
                for delimiter, can_open, can_close in delimiters:
                    delimiter.open, delimiter.close = can_open, can_close
                if changed:
                    state.pending = pending
            elif matched:
                for token in state.tokens[count:]:
                    if token.type == "link_open":
                        token.meta["gfm_extension"] = True
            return bool(matched)

        md.inline.ruler.at(name, checked)


def _interruptible(md: MarkdownIt) -> MarkdownIt:
    # Instance-local wrappers add cooperative cancellation without patching the library globally.
    for ruler in cast(list[Ruler[Any]], [md.block.ruler, md.inline.ruler, md.inline.ruler2]):
        for rule in cast(list[Rule[Any]], list(ruler.__rules__)):
            original = rule.fn

            def checked(
                *args: Any, _fn: Any = original, _name: str = rule.name, **kwargs: Any
            ) -> Any:
                checkpoint()
                if getattr(args[0], "level", 0) >= 60:
                    raise BufferError("RESOURCE_LIMIT_EXCEEDED: Markdown nesting")
                if len(getattr(args[0], "tokens", [])) > MAX_NODES:
                    raise BufferError("RESOURCE_LIMIT_EXCEEDED: Markdown tokens")
                if _name == "table" and len(args) >= 3:
                    state, line, last = args[:3]
                    if line + 1 < last:
                        header = state.src[
                            state.bMarks[line] + state.tShift[line] : state.eMarks[line]
                        ]
                        delimiter = state.src[
                            state.bMarks[line + 1] + state.tShift[line + 1] : state.eMarks[line + 1]
                        ]
                        if (
                            "|" in header
                            and "-" in delimiter
                            and re.fullmatch(r"[ \t|:-]+", delimiter)
                            and not re.match(r"(?:[-+*]|\d+[.)])\s", header)
                        ):
                            columns = len(_cell_columns(header))
                            separator_cells = _cell_columns(delimiter)
                            if columns != len(separator_cells) or any(
                                re.fullmatch(r":?-+:?", delimiter[a:b].strip()) is None
                                for a, b in separator_cells
                            ):
                                raise MarkdownFailure("MD_MALFORMED_TABLE")
                            rows = 1
                            for row in range(line + 2, last):
                                checkpoint()
                                if state.isEmpty(row):
                                    break
                                rows += 1
                                if rows * columns * 3 > MAX_NODES:
                                    raise BufferError(
                                        "RESOURCE_LIMIT_EXCEEDED: Markdown table expansion"
                                    )
                return _fn(*args, **kwargs)

            options: RuleOptionsType = {"alt": list(rule.alt)}
            ruler.at(rule.name, checked, options)
    return md


def parser() -> MarkdownIt:
    md = MarkdownIt(
        "commonmark", {"html": True, "maxNesting": 64, "strikethrough_single_tilde": True}
    )
    md.enable(["table", "strikethrough"])
    md.use(gfm_autolink_plugin).use(footnote_plugin, move_to_end=False).use(dollarmath_plugin)
    _gfm_inline(md)
    md.inline.ruler.before("image", "obsidian", _extension)
    return _interruptible(_html_rules(md))


@dataclass
class _Note:
    source: str
    revision: int
    field: str
    text: str
    starts: list[int]
    units: list[DocumentElement]
    headings: list[tuple[str, int, int]]
    blocks: dict[str, tuple[int, int]]
    node_count: int = 0

    def locator(self, start: int, end: int) -> dict[str, Any]:
        return dict(
            source_key=self.source,
            source_version=self.revision,
            approved_field=self.field,
            char_start=start,
            char_end=end,
            line_start=bisect.bisect_right(self.starts, start),
            line_end=bisect.bisect_right(self.starts, max(start, end - 1)),
            profile=PROFILE,
            text_representation="VERBATIM_SOURCE",
        )


def _comments(text: str, md: MarkdownIt) -> list[tuple[int, int]]:
    """Ignore literal code delimiters; comments containing fences cannot hide later comments."""
    starts = [0]
    for line in text.splitlines(keepends=True):
        starts.append(starts[-1] + len(line))
    tokens = md.parse(text)
    if len(tokens) > MAX_NODES:
        raise BufferError("RESOURCE_LIMIT_EXCEEDED: Markdown nodes")
    inert = [
        (starts[t.map[0]], starts[t.map[1]])
        for t in tokens
        if t.type in {"fence", "code_block", "html_block"} and t.map
    ]
    inert.extend(
        (m.start(), m.end())
        for m in re.finditer(r"(?<!`)(`+)(?!`)([\s\S]*?)(?<!`)\1(?!`)", text)
        if not _escaped(text, m.start()) and "\n\n" not in m.group()
    )
    result: list[tuple[int, int]] = []
    cursor = 0
    while (start := text.find("%%", cursor)) >= 0:
        checkpoint()
        cursor = start + 2
        if _escaped(text, start) or any(
            a <= start < b and not any(c <= a < d for c, d in result) for a, b in inert
        ):
            continue
        end = text.find("%%", cursor)
        if end < 0:
            raise MarkdownFailure("MD_UNCLOSED_COMMENT")
        result.append((start, end + 2))
        cursor = end + 2
    return result


def _mask(text: str, spans: list[tuple[int, int]]) -> str:
    chars = list(text)
    for start, end in spans:
        for index in range(start, end):
            if chars[index] not in "\r\n":
                chars[index] = " "
    return "".join(chars)


def slice_metadata(
    metadata: dict[str, Any], start: int, end: int, *, include_tree: bool = True
) -> dict[str, Any]:
    """Keep source context explicit without duplicating an entire large block per slice."""
    result = dict(metadata)

    def overlaps(loc: dict[str, Any]) -> bool:
        return bool(max(loc["char_start"], start) < min(loc["char_end"], end))

    nodes = []
    for original in metadata["nodes"]:
        checkpoint()
        if not include_tree:
            break
        node = dict(original)
        node["location_scope"] = "ORIGINAL_SOURCE_CONTEXT"
        node["chunk_intersection"] = (
            dict(
                char_start=max(start, original["locator"]["char_start"]),
                char_end=min(end, original["locator"]["char_end"]),
            )
            if overlaps(original["locator"])
            else None
        )
        nodes.append(node)
    result["nodes"] = nodes
    result["source_structure_id"] = metadata["block_id"]
    result["source_structure_definition"] = include_tree
    if include_tree:
        result["source_cells"] = metadata["cells"]
    result["cells"] = [
        {
            **{k: v for k, v in c.items() if k != "raw_text"},
            "location_scope": "ORIGINAL_SOURCE_CONTEXT",
        }
        for c in metadata["cells"]
        if overlaps(c["locator"])
    ]
    result["tables"] = [t for t in metadata["tables"] if overlaps(t["locator"])]
    result["embeds"] = [e for e in metadata["embeds"] if overlaps(e["parent_locator"])]
    result["comments"] = [c for c in metadata["comments"] if overlaps(c)]
    if "properties" in result and not include_tree:
        result.pop("properties")
        result["properties_in_original_source_context"] = True
    result["location_scope"] = "ORIGINAL_SOURCE_CONTEXT"
    return result


def compact_metadata(metadata: dict[str, Any]) -> dict[str, Any]:
    """Encode source context once using block-local locator and existing cell identities.

    Only the producer boundary uses this lossless representation. Parser/chunker callers
    retain their original dictionaries. References in later slices target source_cells
    in the existing source_structure_definition, never a different revision.
    """
    if metadata.get("obsidian_profile") != PROFILE:
        return metadata
    defaults: dict[str, Any] = dict(
        hidden=False,
        block=False,
        info="",
        token_meta={},
        token_attributes={},
        text="",
        markup="",
        attributes={},
    )
    result = dict(metadata)
    result["source_context_encoding"] = "block-local-references-v1"
    result["node_defaults"] = defaults
    locations: list[dict[str, Any]] = []
    location_ids: dict[str, int] = {}

    def compact_record(original: dict[str, Any]) -> dict[str, Any]:
        checkpoint()
        item = dict(original)
        if "locator" in item:
            loc = item.pop("locator")
            # Locators can contain dependency-edge arrays; canonical JSON preserves
            # their full identity while interning only exactly equal locations.
            key = json.dumps(loc, sort_keys=True, ensure_ascii=False)
            if key not in location_ids:
                location_ids[key] = len(locations)
                locations.append(loc)
            item["locator_ref"] = location_ids[key]
        return item

    result["nodes"] = [
        {k: v for k, v in compact_record(n).items() if k not in defaults or v != defaults[k]}
        for n in metadata["nodes"]
    ]
    if "source_cells" in metadata:
        result["source_cells"] = [compact_record(c) for c in metadata["source_cells"]]
    if "source_structure_id" in metadata:
        result["cells"] = [
            dict(id=c["id"], location_scope=c["location_scope"]) for c in metadata["cells"]
        ]
        result["cells_reference_source_definition"] = True
    else:
        result["cells"] = [compact_record(c) for c in metadata["cells"]]
    result["tables"] = [compact_record(t) for t in metadata["tables"]]
    # Each definition belongs to one source/revision. Store shared provenance once
    # without assuming a field is common (dependency edges and containing-cell roles
    # can differ). Exact equality is required for inheritance.
    common = (
        {
            k: v
            for k, v in locations[0].items()
            if all(k in loc and loc[k] == v for loc in locations)
        }
        if locations
        else {}
    )
    result["locator_defaults"] = common
    result["locator_definitions"] = [
        {k: v for k, v in loc.items() if k not in common} for loc in locations
    ]
    return result


def expand_metadata(
    metadata: dict[str, Any], definitions: dict[str, dict[str, Any]] | None = None
) -> dict[str, Any]:
    """Resolve the producer's source-context encoding, also accepting legacy metadata."""
    if metadata.get("source_context_encoding") != "block-local-references-v1":
        return metadata
    result = dict(metadata)
    result.pop("source_context_encoding")
    defaults = result.pop("node_defaults")
    location_defaults = result.pop("locator_defaults")
    locations = [{**location_defaults, **loc} for loc in result.pop("locator_definitions")]

    def expand_record(original: dict[str, Any]) -> dict[str, Any]:
        item = dict(original)
        if "locator_ref" in item:
            item["locator"] = locations[item.pop("locator_ref")]
        return item

    result["nodes"] = [{**defaults, **expand_record(n)} for n in result["nodes"]]
    if "source_cells" in result:
        result["source_cells"] = [expand_record(c) for c in result["source_cells"]]
    if result.pop("cells_reference_source_definition", False):
        definition = (
            result
            if "source_cells" in result
            else expand_metadata((definitions or {})[result["source_structure_id"]])
        )
        cells = {c["id"]: c for c in definition["source_cells"]}
        result["cells"] = [
            {
                **{k: v for k, v in cells[c["id"]].items() if k != "raw_text"},
                "location_scope": c["location_scope"],
            }
            for c in result["cells"]
        ]
    else:
        result["cells"] = [expand_record(c) for c in result["cells"]]
    result["tables"] = [expand_record(t) for t in result["tables"]]
    return result


def _note(text: str, source: str, revision: int, field: str) -> _Note:
    check_text(text)
    if (
        "\x00" in text
        or not isinstance(source, str)
        or not source
        or not isinstance(field, str)
        or not field
        or not isinstance(revision, int)
        or isinstance(revision, bool)
        or revision < 1
    ):
        raise MarkdownFailure("MD_INVALID_SOURCE")
    md = parser()
    # Scan comments using core grammar, before introducing dependency-reading semantics.
    core = _interruptible(_html_rules(MarkdownIt("commonmark")))
    spans: list[tuple[int, int]] = []
    properties: Any = None
    fm_end = 0
    if text.startswith(("---\n", "---\r\n")):
        fm = re.match(r"\A---\r?\n([\s\S]*?)^---(?:\r?\n|$)", text, re.M)
        if not fm:
            raise MarkdownFailure("MD_INVALID_PROPERTIES")
        try:
            events = list(yaml.parse(fm.group(1)))
            if any(isinstance(e, yaml.AliasEvent) for e in events) or len(events) > MAX_NODES:
                raise MarkdownFailure("MD_INVALID_PROPERTIES")
            safe_tags = {
                "tag:yaml.org,2002:" + name
                for name in ("map", "seq", "str", "int", "float", "bool", "null", "timestamp")
            }
            if any(getattr(event, "tag", None) not in safe_tags | {None} for event in events):
                raise MarkdownFailure("MD_UNSUPPORTED_PROPERTIES_TYPE")
            depth = 0
            for event in events:
                checkpoint()
                if isinstance(event, (yaml.MappingStartEvent, yaml.SequenceStartEvent)):
                    depth += 1
                    if depth > 60:
                        raise BufferError("RESOURCE_LIMIT_EXCEEDED: properties nesting")
                elif isinstance(event, (yaml.MappingEndEvent, yaml.SequenceEndEvent)):
                    depth -= 1
            properties = yaml.load(fm.group(1), Loader=_Properties)
            if properties is None and all(
                not line.strip() or line.lstrip().startswith("#")
                for line in fm.group(1).splitlines()
            ):
                properties = {}
        except yaml.YAMLError as exc:
            raise MarkdownFailure("MD_INVALID_PROPERTIES") from exc
        if not isinstance(properties, dict):
            raise MarkdownFailure("MD_INVALID_PROPERTIES")
        fm_end = fm.end()
        spans.append((0, fm_end))
    comments = _comments(_mask(text, spans), core)
    spans.extend(comments)
    masked = _mask(text, spans)
    starts = [0]
    for line in text.splitlines(keepends=True):
        starts.append(starts[-1] + len(line))
    note = _Note(source, revision, field, text, starts, [], [], {})
    env: dict[str, Any] = {}
    tokens = md.parse(masked, env)
    if len(tokens) > MAX_NODES:
        raise BufferError("RESOURCE_LIMIT_EXCEEDED: Markdown nodes")

    def inline_definitions(items: list[Token], depth: int = 0) -> None:
        if depth > 60:
            raise BufferError("RESOURCE_LIMIT_EXCEEDED: Markdown inline nesting")
        for token in items:
            checkpoint()
            if token.type == "footnote_ref":
                definition = env.get("footnotes", {}).get("list", {}).get(token.meta["id"], {})
                if "content" in definition:
                    token.children = definition.get("tokens", [])
                token.meta["label"] = definition.get("label", "INLINE")
            inline_definitions(token.children or [], depth + 1)

    inline_definitions(tokens)
    groups: list[tuple[int, int, list[Token]]] = []
    index = 0
    while index < len(tokens):
        checkpoint()
        token = tokens[index]
        if token.level != 0 or not token.map or token.nesting == -1:
            raise MarkdownFailure("MD_UNMAPPED_STRUCTURE")
        end = index + 1
        if token.nesting == 1:
            depth = 1
            while end < len(tokens) and depth:
                depth += tokens[end].nesting
                end += 1
            if depth:
                raise MarkdownFailure("MD_UNMAPPED_STRUCTURE")
        groups.append((starts[token.map[0]], starts[token.map[1]], tokens[index:end]))
        index = end
    if fm_end:
        groups.insert(0, (0, fm_end, []))
    all_references = list(env.get("references", {}).values()) + env.get("duplicate_refs", [])
    for ref in all_references:
        a, b = ref["map"]
        if not any(s <= starts[a] and starts[b] <= e for s, e, _ in groups):
            groups.append((starts[a], starts[b], []))
    groups.sort(key=lambda g: (g[0], g[1]))
    cursor = 0
    heading_stack: list[tuple[int, str]] = []
    for start, end, group in groups:
        if start < cursor:
            raise MarkdownFailure("MD_OVERLAPPING_STRUCTURE")
        if start > cursor and text[cursor:start].strip():
            if masked[cursor:start].strip():
                raise MarkdownFailure("MD_UNMAPPED_SOURCE")
            _unit(note, cursor, start, [], heading_stack, comments, properties)
        else:
            start = cursor
        _unit(note, start, end, group, heading_stack, comments, properties)
        cursor = end
    if cursor < len(text):
        if text[cursor:].strip():
            if masked[cursor:].strip():
                raise MarkdownFailure("MD_UNMAPPED_SOURCE")
            _unit(note, cursor, len(text), [], heading_stack, comments, properties)
        elif note.units:
            last = note.units[-1]
            note.units[-1] = replace(
                last,
                text=text[last.locator["char_start"] :],
                locator=note.locator(last.locator["char_start"], len(text)),
            )
    if "".join(e.text for e in note.units) != text and text.strip():
        raise MarkdownFailure("MD_SOURCE_COVERAGE_FAILED")
    # Heading links select original line ranges, ending before the next peer/ancestor heading.
    for position, (name, level, start) in enumerate(note.headings):
        end = next(
            (s for _, next_level, s in note.headings[position + 1 :] if next_level <= level),
            len(text),
        )
        note.headings[position] = (name, level, start)
        for offset in range(len(name.split("#"))):
            key = "heading:" + "#".join(name.split("#")[offset:]).casefold()
            note.blocks[key] = (start, end) if key not in note.blocks else (-1, -1)
    for index, unit in enumerate(note.units):
        standalone = re.fullmatch(r"\s*\^([A-Za-z0-9-]+)\s*", unit.text)
        if standalone and index:
            note.blocks[standalone.group(1)] = (
                note.units[index - 1].locator["char_start"],
                unit.locator["char_end"],
            )
        refs = {
            label: {
                **value,
                "locator": note.locator(starts[value["map"][0]], starts[value["map"][1]]),
            }
            for label, value in env.get("references", {}).items()
            if unit.locator["char_start"] <= starts[value["map"][0]] < unit.locator["char_end"]
        }
        unit.metadata["reference_definitions"] = refs
        unit.metadata["duplicate_reference_definitions"] = [
            {
                **r,
                "locator": note.locator(starts[r["map"][0]], starts[r["map"][1]]),
                "binding": "FIRST_DEFINITION_WINS",
            }
            for r in env.get("duplicate_refs", [])
            if unit.locator["char_start"] <= starts[r["map"][0]] < unit.locator["char_end"]
        ]
    if sum(len(u.metadata["nodes"]) for u in note.units) > MAX_NODES:
        raise BufferError("RESOURCE_LIMIT_EXCEEDED: Markdown nodes")
    return note


def _unit(
    note: _Note,
    start: int,
    end: int,
    tokens: list[Token],
    headings: list[tuple[int, str]],
    comments: list[tuple[int, int]],
    properties: Any,
) -> None:
    raw = note.text[start:end]
    if not raw:
        return
    kind = ElementKind.PARAGRAPH
    nodes: list[dict[str, Any]] = []
    embeds: list[dict[str, Any]] = []
    cells: list[dict[str, Any]] = []
    stack: list[str] = []
    token_nodes: dict[int, str] = {}
    group_id = f"{note.source}:{note.revision}:{start}:{end}"

    def visit(token: Token, parent: str | None, locator: dict[str, Any]) -> str:
        checkpoint()
        if note.node_count >= MAX_NODES:
            raise BufferError("RESOURCE_LIMIT_EXCEEDED: Markdown inline nodes")
        note.node_count += 1
        identity = f"{group_id}:{len(nodes)}"
        node: dict[str, Any] = dict(
            id=identity,
            parent_id=parent,
            type=token.type,
            tag=token.tag,
            hidden=token.hidden,
            block=token.block,
            info=token.info,
            token_meta=dict(token.meta),
            token_attributes=dict(token.attrs),
            nesting=token.nesting,
            text=token.content,
            markup=token.markup,
            attributes=dict(token.attrs),
            locator=locator,
        )
        if token.meta:
            node["attributes"].update(
                {
                    k: v
                    for k, v in token.meta.items()
                    if isinstance(v, (str, int, float, bool, type(None)))
                }
            )
        nodes.append(node)
        token_nodes[id(token)] = identity
        if token.type == "obsidian_embed":
            embeds.append(
                dict(
                    **token.meta,
                    parent_node_id=identity,
                    parent_locator=locator,
                    syntax=token.content,
                )
            )
        if token.type == "image":
            address = urlsplit(str(token.attrGet("src") or ""))
            if (
                address.scheme
                or address.netloc
                or address.query
                or not address.path.casefold().endswith(".md")
            ):
                raise MarkdownFailure("MD_UNSUPPORTED_EMBED_TYPE")
            embeds.append(
                dict(
                    target=unquote(address.path),
                    anchor=unquote(address.fragment),
                    alias=token.content,
                    parent_node_id=identity,
                    parent_locator=locator,
                    syntax="MARKDOWN_NOTE_EMBED",
                )
            )
        if token.type == "code_inline" and token.content.startswith(("=", "$=")):
            raise MarkdownFailure("MD_UNSUPPORTED_DYNAMIC_CONTENT")
        if token.type == "obsidian_block_id":
            if token.content in note.blocks:
                raise MarkdownFailure("MD_AMBIGUOUS_ANCHOR")
            note.blocks[token.content] = (start, end)
        if token.type.startswith("html"):
            try:
                _, html_nodes = html_source(token.content)
            except HtmlFailure as exc:
                raise MarkdownFailure(str(exc)) from exc
            node["html_source_tree"] = html_nodes
            node["html_tree_coordinates"] = "RELATIVE_TO_TOKEN_CONTENT"
            note.node_count += len(html_nodes) + len(html_nodes[0]["source_tokens"])
            if note.node_count > MAX_NODES:
                raise BufferError("RESOURCE_LIMIT_EXCEEDED: Markdown/HTML output nodes")
        child_stack = [identity]
        for child in token.children or []:
            if child.nesting == -1:
                if len(child_stack) == 1:
                    raise MarkdownFailure("MD_UNMAPPED_STRUCTURE")
                child_stack.pop()
                continue
            child_id = visit(
                child, child_stack[-1], {**locator, "location_kind": "CONTAINING_BLOCK"}
            )
            if child.nesting == 1:
                child_stack.append(child_id)
        if len(child_stack) != 1:
            raise MarkdownFailure("MD_UNMAPPED_STRUCTURE")
        return identity

    for token_index, token in enumerate(tokens):
        checkpoint()
        if token.nesting == -1:
            if not stack:
                raise MarkdownFailure("MD_UNMAPPED_STRUCTURE")
            stack.pop()
            continue
        loc = (
            note.locator(note.starts[token.map[0]], note.starts[token.map[1]])
            if token.map
            else (note.locator(start, end))
        )
        identity = visit(token, stack[-1] if stack else None, loc)
        if token.nesting == 1:
            stack.append(identity)
        if token.type == "heading_open":
            level = int(token.tag[1:])
            inline = tokens[token_index + 1]

            def displayed(item: Token) -> str:
                if item.type == "obsidian_link":
                    return str(item.meta.get("alias") or item.meta["target"])
                return (
                    "".join(displayed(c) for c in item.children) if item.children else item.content
                )

            title = "".join(displayed(t) for t in inline.children or [])
            while headings and headings[-1][0] >= level:
                headings.pop()
            headings.append((level, title))
            note.headings.append(("#".join(s for _, s in headings), level, loc["char_start"]))
        if token.type in {"fence", "code_block"}:
            language = (
                token.info.strip().split(maxsplit=1)[0].casefold() if token.info.strip() else ""
            )
            if language in _DYNAMIC:
                raise MarkdownFailure("MD_UNSUPPORTED_DYNAMIC_CONTENT")
        if token.type in {"html_block", "html_inline"} and re.search(r"<%|\$=", token.content):
            raise MarkdownFailure("MD_UNSUPPORTED_DYNAMIC_CONTENT")
    if tokens:
        first = tokens[0].type
        kind = {
            "heading_open": ElementKind.HEADING,
            "table_open": ElementKind.TABLE,
            "fence": ElementKind.CODE_BLOCK,
            "code_block": ElementKind.CODE_BLOCK,
            "bullet_list_open": ElementKind.LIST_ITEM,
            "ordered_list_open": ElementKind.LIST_ITEM,
        }.get(first, ElementKind.PARAGRAPH)
        if first == "blockquote_open":
            kind = ElementKind.CALLOUT if re.search(r"\[![\w-]+\][+-]?", raw) else kind
    tables = []
    for table_position, table in enumerate(tokens):
        if table.type == "table_open":
            assert table.map is not None
            table_end = next(
                i
                for i in range(table_position + 1, len(tokens))
                if tokens[i].type == "table_close" and tokens[i].level == table.level
            )
            table_tokens = tokens[table_position : table_end + 1]
            table_id = token_nodes[id(table)]
            row_lines = [t.map[0] for t in table_tokens if t.type == "tr_open" and t.map]
            alignments = [t.attrGet("style") for t in table_tokens if t.type == "th_open"]
            tables.append(
                dict(
                    id=table_id,
                    locator=note.locator(note.starts[table.map[0]], note.starts[table.map[1]]),
                    columns=len(alignments),
                    rows=len(row_lines),
                )
            )
            for row_index, line in enumerate(row_lines):
                original_line = note.text[note.starts[line] : note.starts[line + 1]].rstrip("\r\n")
                # Nested container prefixes remain source text, never cell text.
                prefix = re.match(
                    r"^(?:(?:[ \t]*>[ \t]?)|(?:[ \t]*(?:[-+*]|\d+[.)])[ \t]+))*", original_line
                )
                prefix_len = prefix.end() if prefix else 0
                parts = _cell_columns(original_line[prefix_len:])
                for column in range(max(len(parts), len(alignments))):
                    if column < len(parts):
                        a, b = parts[column]
                        a += note.starts[line] + prefix_len
                        b += note.starts[line] + prefix_len
                        cell_loc = note.locator(a, b)
                        value = note.text[a:b]
                        presence = "ORIGINAL"
                    else:
                        cell_loc = {
                            **note.locator(note.starts[line], note.starts[line + 1]),
                            "location_kind": "ROW_OF_IMPLICIT_GFM_EMPTY_CELL",
                            "text_representation": "IMPLICIT_EMPTY_NO_AUTHORED_CELL",
                        }
                        value, presence = "", "IMPLICIT_GFM_EMPTY"
                    cell_loc.update(table_id=table_id, row_index=row_index, column_index=column)
                    cells.append(
                        dict(
                            id=f"{table_id}:cell:{row_index}:{column}",
                            parent_id=table_id,
                            locator=cell_loc,
                            raw_text=value,
                            source_presence=presence,
                            is_header=row_index == 0,
                            alignment=alignments[column] if column < len(alignments) else None,
                            overflow=column >= len(alignments),
                        )
                    )
            table_cells = [c for c in cells if c["parent_id"] == table_id and not c["overflow"]]
            cell_tokens = [t for t in table_tokens if t.type in {"th_open", "td_open"}]
            cell_locations = {
                token_nodes[id(token)]: cell["locator"]
                for cell, token in zip(table_cells, cell_tokens, strict=True)
            }
            for node in nodes:
                parent_location = cell_locations.get(node["id"]) or cell_locations.get(
                    node["parent_id"]
                )
                if parent_location is not None:
                    node["locator"] = {**parent_location, "location_kind": "CONTAINING_CELL"}
                    cell_locations[node["id"]] = parent_location
    callouts = []
    for node in nodes:
        if node["type"] == "blockquote_open":
            first_line = note.text[
                node["locator"]["char_start"] : node["locator"]["char_end"]
            ].splitlines()[0]
            match = re.search(r"\[!([\w-]+)\]([+-]?)(.*)", first_line)
            if match:
                callouts.append(
                    dict(
                        node_id=node["id"],
                        type=match.group(1),
                        fold={"+": "EXPANDED", "-": "COLLAPSED", "": "NONE"}[match.group(2)],
                        title=match.group(3).strip(),
                    )
                )
        if node["type"] == "list_item_open":
            first_line = note.text[
                node["locator"]["char_start"] : node["locator"]["char_end"]
            ].splitlines()[0]
            task = re.search(r"(?:[-+*]|\d+[.)])\s+\[(.)\]", first_line)
            if task:
                node["attributes"].update(task_marker=task.group(1), checked=task.group(1) != " ")
    metadata: dict[str, Any] = dict(
        obsidian_profile=PROFILE,
        block_id=group_id,
        nodes=nodes,
        cells=cells,
        tables=tables,
        callouts=callouts,
        embeds=embeds,
    )
    if start == 0 and properties is not None:
        kind = ElementKind.METADATA

        # JSON-safe values; exact types/lexemes remain in the original YAML source.
        def safe(value: Any) -> Any:
            if isinstance(value, float) and not math.isfinite(value):
                raise MarkdownFailure("MD_INVALID_PROPERTIES")
            if isinstance(value, dict):
                return {k: safe(v) for k, v in value.items()}
            if isinstance(value, list):
                return [safe(v) for v in value]
            return value if isinstance(value, (str, int, float, bool, type(None))) else str(value)

        metadata["properties"] = safe(properties)
    metadata["comments"] = [note.locator(a, b) for a, b in comments if start <= a and b <= end]
    locator = note.locator(start, end)
    element = DocumentElement(
        kind,
        raw,
        note.source,
        note.revision,
        note.field,
        locator,
        [h for _, h in headings],
        metadata,
    )
    element.validate_mandatory_locator("MD")
    note.units.append(element)


def parse_obsidian(
    content: str,
    root_key: str,
    source_version: int,
    approved_field: str,
    dependencies: dict[str, ApprovedDependency] | None = None,
) -> ParsedDocument:
    dep_map = dependencies or {}
    bindings: dict[str, tuple[int, str, str]] = {}
    cache: dict[str, _Note] = {}
    budget = [0]
    supplied = 0
    for source, version, field, text in [(root_key, source_version, approved_field, content)] + [
        (v.source_key, v.source_version, v.approved_field, v.text_content) for v in dep_map.values()
    ]:
        checkpoint()
        if len(text) > MAX_TEXT_CHARS or (
            source not in bindings and supplied + len(text) > MAX_TEXT_CHARS
        ):
            raise BufferError("RESOURCE_LIMIT_EXCEEDED: supplied Markdown payloads")
        digest = hashlib.sha256(text.encode("utf-8")).hexdigest()
        binding = (version, field, digest)
        if source in bindings:
            if bindings[source] != binding:
                raise MarkdownFailure("MD_CONFLICTING_REVISION")
            continue
        # Alias keys identify one immutable supplied S/R. Expansion below still
        # charges every actually selected occurrence, including repeated embeds.
        supplied += len(text)
        bindings[source] = binding
    root = _note(content, root_key, source_version, approved_field)
    cache[root_key] = root
    doc = ParsedDocument(root_key, "MD", source_version)

    def expand(
        note: _Note, anchor: str, ancestry: tuple[tuple[str, str], ...], edges: list[dict[str, Any]]
    ) -> list[DocumentElement]:
        checkpoint()
        identity = (note.source, anchor.casefold())
        if identity in ancestry:
            raise MarkdownFailure("MD_CYCLIC_TRANSCLUSION")
        if len(ancestry) >= MAX_DEPTH:
            raise MarkdownFailure("MD_TRANSCLUSION_DEPTH")
        selected_start, selected_end = 0, len(note.text)
        if anchor:
            key = anchor[1:] if anchor.startswith("^") else "heading:" + anchor.casefold()
            if key not in note.blocks or note.blocks[key][0] < 0:
                raise MarkdownFailure("MD_MISSING_OR_AMBIGUOUS_ANCHOR")
            selected_start, selected_end = note.blocks[key]
        result: list[DocumentElement] = []
        for unit in note.units:
            a, b = (
                max(selected_start, unit.locator["char_start"]),
                min(selected_end, unit.locator["char_end"]),
            )
            if a >= b:
                continue
            budget[0] += b - a
            if budget[0] > MAX_TEXT_CHARS:
                raise BufferError("RESOURCE_LIMIT_EXCEEDED: transclusion expansion")
            locator = {**note.locator(a, b), "dependency_edges": edges}
            if edges:
                locator["parent_source_key"] = edges[-1]["parent"]
            metadata = unit.metadata
            if a != unit.locator["char_start"] or b != unit.locator["char_end"]:
                metadata = slice_metadata(metadata, a, b)
            result.append(replace(unit, text=note.text[a:b], locator=locator, metadata=metadata))
            for embed in unit.metadata["embeds"]:
                parent_loc = embed["parent_locator"]
                if parent_loc["char_end"] <= a or parent_loc["char_start"] >= b:
                    continue
                target = embed["target"]
                if re.search(r"\.(?!md$)[A-Za-z0-9]+$|://", target, re.I):
                    raise MarkdownFailure("MD_UNSUPPORTED_EMBED_TYPE")
                if not target:
                    child = note
                else:
                    if target not in dep_map:
                        raise MarkdownFailure("MD_UNAPPROVED_EMBED")
                    binding = dep_map[target]
                    if binding.source_key not in cache:
                        cache[binding.source_key] = _note(
                            binding.text_content,
                            binding.source_key,
                            binding.source_version,
                            binding.approved_field,
                        )
                    child = cache[binding.source_key]
                edge = dict(
                    parent=note.source,
                    parent_version=note.revision,
                    child=child.source,
                    child_version=child.revision,
                    anchor=embed["anchor"],
                    alias=embed["alias"],
                    parent_node_id=embed["parent_node_id"],
                    parent_locator=embed["parent_locator"],
                    parent_callouts=unit.metadata["callouts"],
                )
                result.extend(
                    expand(child, embed["anchor"], ancestry + (identity,), edges + [edge])
                )
        return result

    doc.elements = expand(root, "", (), [])
    doc.table_count = sum(len(e.metadata["tables"]) for e in doc.elements)
    return doc

"""Inert HTML5 hierarchy plus authored token spans. No rendering or resource loading."""

from __future__ import annotations

from collections.abc import Iterator
from html.parser import HTMLParser
from typing import Any

from html5lib import HTMLParser as HTML5Parser  # type: ignore[import-untyped]
from html5lib._tokenizer import HTMLTokenizer  # type: ignore[import-untyped]
from html5lib.constants import tokenTypes  # type: ignore[import-untyped]

from ppv_rag.limits import checkpoint

_VOID = {
    "area",
    "base",
    "br",
    "col",
    "embed",
    "hr",
    "img",
    "input",
    "link",
    "meta",
    "param",
    "source",
    "track",
    "wbr",
}
_ACTIVE = {
    "script",
    "style",
    "iframe",
    "object",
    "embed",
    "img",
    "audio",
    "video",
    "source",
    "picture",
    "link",
    "base",
    "form",
    "input",
    "button",
    "canvas",
    "svg",
    "math",
    "xmp",
    "noembed",
    "noframes",
    "plaintext",
}
_P_END = {
    "address",
    "article",
    "aside",
    "blockquote",
    "details",
    "dialog",
    "div",
    "dl",
    "fieldset",
    "figcaption",
    "figure",
    "footer",
    "form",
    "h1",
    "h2",
    "h3",
    "h4",
    "h5",
    "h6",
    "header",
    "hgroup",
    "hr",
    "main",
    "menu",
    "nav",
    "ol",
    "p",
    "pre",
    "search",
    "section",
    "table",
    "ul",
}
_OPTIONAL_BEFORE = {
    "li": {"li"},
    "dt": {"dt", "dd"},
    "dd": {"dt", "dd"},
    "rt": {"rt", "rp"},
    "rp": {"rt", "rp"},
    "option": {"option", "optgroup"},
    "optgroup": {"optgroup"},
    "thead": {"tbody", "tfoot"},
    "tbody": {"tbody", "tfoot"},
    "tr": {"tr"},
    "td": {"td", "th", "tr"},
    "th": {"td", "th", "tr"},
    "p": _P_END,
}


class HtmlFailure(ValueError):
    pass


class _Complete(Exception):
    def __init__(self, end: int):
        self.end = end


class _Source(HTMLParser):
    """Lex original text; hierarchy repair belongs to html5lib, never this scanner."""

    def __init__(self, text: str, atom: bool):
        super().__init__(convert_charrefs=False)
        self.text, self.atom = text, atom
        self.tokens: list[dict[str, Any]] = []
        self.stack: list[str] = []
        self.starts = [0]

    def position(self) -> int:
        line, column = self.getpos()
        while len(self.starts) < line:
            self.starts.append(self.text.index("\n", self.starts[-1]) + 1)
        return self.starts[line - 1] + column

    def add(self, role: str, end: int, **values: Any) -> None:
        checkpoint()
        if len(self.tokens) >= 50_000 or len(self.stack) >= 60:
            raise BufferError("RESOURCE_LIMIT_EXCEEDED: HTML structure")
        start = self.position()
        self.tokens.append(
            dict(
                type=role,
                relative_start=start,
                relative_end=end,
                raw_text=self.text[start:end],
                **values,
            )
        )

    def finish(self, end: int) -> None:
        if self.atom and not self.stack:
            raise _Complete(end)

    def start(self, tag: str, attrs: list[tuple[str, str | None]], closed: bool) -> None:
        # Delimit authored HTML; insertion modes and implied nodes belong to html5lib.
        while self.stack and tag in _OPTIONAL_BEFORE.get(self.stack[-1], set()):
            self.stack.pop()
            self.finish(self.position())
        end = self.position() + len(self.get_starttag_text() or "")
        self.add("html_start_tag", end, tag=tag, attributes=attrs)
        if closed or tag in _VOID:
            self.finish(end)
        else:
            self.stack.append(tag)

    def handle_starttag(self, tag: str, attrs: list[tuple[str, str | None]]) -> None:
        self.start(tag, attrs, False)
        if tag in {"textarea", "title"}:
            # RCDATA has no child markup. Keep authored references verbatim here;
            # the native HTML5 parser supplies decoded semantic text separately.
            self.set_cdata_mode(tag)

    def handle_startendtag(self, tag: str, attrs: list[tuple[str, str | None]]) -> None:
        # In the HTML namespace a solidus closes only void elements. Non-void
        # elements (including RCDATA) keep exactly their normal start-tag context.
        self.handle_starttag(tag, attrs)

    def handle_endtag(self, tag: str) -> None:
        end = self.text.find(">", self.position()) + 1
        self.add("html_end_tag", end, tag=tag)
        if tag in self.stack:
            del self.stack[len(self.stack) - 1 - self.stack[::-1].index(tag) :]
        self.finish(end)

    def handle_data(self, data: str) -> None:
        if data:
            self.add("html_text", self.position() + len(data))

    def handle_comment(self, data: str) -> None:
        start = self.position()
        if self.text.startswith("<!-->", start):
            end = start + 5
        elif self.text.startswith("<!--->", start):
            end = start + 6
        else:
            end = start + 4 + len(data)
            end += 4 if self.text[end : end + 4] == "--!>" else 3
            end = min(end, len(self.text))
        self.add("html_comment", end)
        self.finish(end)

    def handle_entityref(self, name: str) -> None:
        start = self.position()
        end = start + len(name) + 1
        if self.text[end : end + 1] == ";":
            end += 1
        self.add("html_entity", end)

    def handle_charref(self, name: str) -> None:
        start = self.position()
        end = start + len(name) + 2
        if self.text[end : end + 1] == ";":
            end += 1
        self.add("html_entity", end)

    def handle_decl(self, decl: str) -> None:
        end = self.position() + len(decl) + 3
        self.add("html_declaration", end)
        self.finish(end)

    def handle_pi(self, data: str) -> None:
        end = self.text.find(">", self.position()) + 1
        self.add("html_processing_instruction", end)
        self.finish(end)

    def unknown_decl(self, data: str) -> None:
        end = self.text.find(">", self.position()) + 1
        self.add("html_declaration", end)
        self.finish(end)


def html_extent(text: str, *, require_closed: bool = False) -> int:
    source = _Source(text, True)
    try:
        source.feed(text)
        source.close()
    except _Complete as complete:
        return complete.end
    return 0 if require_closed else len(text)


class _CheckedTokenizer(HTMLTokenizer):
    def __iter__(self) -> Iterator[dict[str, Any]]:
        count = 0
        for token in super().__iter__():
            checkpoint()
            count += 1
            if count > 50_000 or len(self.parser.tree.openElements) > 60:
                raise BufferError("RESOURCE_LIMIT_EXCEEDED: HTML5 structure")
            if token["type"] in {tokenTypes["StartTag"], tokenTypes["EmptyTag"]}:
                if token["name"] in _ACTIVE:
                    raise HtmlFailure("MD_UNSUPPORTED_ACTIVE_HTML")
                for name, value in token["data"].items():
                    name = name[-1] if isinstance(name, tuple) else name
                    normalized = "".join(c for c in value.casefold() if ord(c) > 32)
                    if (
                        name.startswith("on")
                        or normalized.startswith(("javascript:", "vbscript:", "data:text/html"))
                        or (
                            name == "style"
                            and any(s in normalized for s in ("url(", "expression(", "@import"))
                        )
                    ):
                        raise HtmlFailure("MD_UNSUPPORTED_ACTIVE_HTML")
            yield token


class _CheckedParser(HTML5Parser):
    def _parse(
        self,
        stream: str,
        innerHTML: bool = False,
        container: str = "div",
        scripting: bool = False,
        **kwargs: Any,
    ) -> None:
        self.innerHTMLMode, self.container, self.scripting = innerHTML, container, scripting
        self.tokenizer = _CheckedTokenizer(stream, parser=self, **kwargs)
        self.reset()
        self.mainLoop()


def html_source(text: str, *, atom: bool = False) -> tuple[int, list[dict[str, Any]]]:
    end = html_extent(text) if atom else len(text)
    raw = text[:end]
    source = _Source(raw, False)
    source.feed(raw)
    source.close()
    if raw and not source.tokens:
        source.tokens.append(
            dict(type="html_incomplete_source", relative_start=0, relative_end=end, raw_text=raw)
        )
    # Parser callbacks give exact event starts. Adjacent starts partition authored
    # source even when HTML5 repairs a short comment or optional tag ending.
    for index, token in enumerate(source.tokens):
        token["relative_end"] = (
            source.tokens[index + 1]["relative_start"] if index + 1 < len(source.tokens) else end
        )
        token["raw_text"] = raw[token["relative_start"] : token["relative_end"]]
    if source.tokens and source.tokens[0]["relative_start"] != 0:
        raise HtmlFailure("MD_INVALID_HTML_SOURCE_MAPPING")
    if any(t.get("tag") in _ACTIVE for t in source.tokens if t["type"] == "html_start_tag"):
        raise HtmlFailure("MD_UNSUPPORTED_ACTIVE_HTML")
    document = _CheckedParser(namespaceHTMLElements=False).parseFragment(raw)
    nodes: list[dict[str, Any]] = [
        dict(
            id=0,
            parent_id=None,
            type="html_source_context",
            source_tokens=source.tokens,
            relative_start=0,
            relative_end=end,
            representation="VERBATIM_SOURCE_CONTEXT",
        )
    ]

    def add(role: str, parent: int, **values: Any) -> int:
        checkpoint()
        if len(nodes) + len(source.tokens) >= 50_000:
            raise BufferError("RESOURCE_LIMIT_EXCEEDED: HTML output structure")
        identity = len(nodes)
        nodes.append(
            dict(
                id=identity,
                parent_id=parent,
                type=role,
                relative_start=0,
                relative_end=end,
                location_kind="CONTAINING_HTML_SOURCE",
                representation="DERIVED_HTML5_STRUCTURE",
                **values,
            )
        )
        return identity

    def visit(element: Any, parent: int, depth: int = 0) -> None:
        if depth >= 60:
            raise BufferError("RESOURCE_LIMIT_EXCEEDED: HTML output nesting")
        if not isinstance(element.tag, str):
            add("html_comment", parent, text=element.text or "")
            return
        identity = add("html_element", parent, tag=element.tag, attributes=dict(element.attrib))
        if element.text:
            add("html_text", identity, text=element.text)
        for child in element:
            visit(child, identity, depth + 1)
            if child.tail:
                add("html_text", identity, text=child.tail)

    if document.text:
        add("html_text", 0, text=document.text)
    for child in document:
        visit(child, 0)
        if child.tail:
            add("html_text", 0, text=child.tail)
    return end, nodes

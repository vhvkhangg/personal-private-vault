"""Independent official-HTML oracle for the actual D17 source AST and producer output."""

from __future__ import annotations

import argparse
import hashlib
import html
import json
import re
import sys
from collections import Counter
from pathlib import Path
from typing import Any

sys.path.insert(0, str(Path(__file__).resolve().parents[1] / "src"))

import html5lib  # type: ignore[import-untyped]  # noqa: E402
from markdown_it import MarkdownIt  # noqa: E402
from markdown_it.token import Token  # noqa: E402

from ppv_rag.chunking.chunker import StructureFirstChunker  # noqa: E402
from ppv_rag.parsers.obsidian import MarkdownFailure, expand_metadata, parse_obsidian  # noqa: E402


def normalize(rendered: str) -> Any:
    """Compare native tags/attributes/text; ignore serializer-only inter-block newlines."""
    document = html5lib.parseFragment(rendered, namespaceHTMLElements=False)

    def element(node: Any) -> Any:
        tag = node.tag if isinstance(node.tag, str) else "COMMENT"
        if tag == "del":
            tag = "s"  # Same strikethrough role; source AST retains its original token/tag.
        attributes = dict(node.attrib)
        if tag in {"th", "td"}:
            alignment = re.fullmatch(
                r"text-align:\s*(left|right|center);?", attributes.get("style", "")
            )
            if alignment:
                attributes.pop("style")
                attributes["align"] = alignment.group(1)
        text = node.text or ""
        children = []
        for child in node:
            children.append(element(child))
            tail = child.tail or ""
            if tail.strip() or tag in {"p", "em", "strong", "a", "code", "pre", "del", "s"}:
                children.append(tail)
        if not text.strip() and tag not in {"p", "em", "strong", "a", "code", "pre", "del", "s"}:
            text = ""
        return [tag, sorted(attributes.items()), text, children]

    return element(document)


def rendered_ast(metadata: list[dict[str, Any]]) -> str:
    nodes = [n for m in metadata for n in m["nodes"]]
    children: dict[str | None, list[dict[str, Any]]] = {}
    for n in nodes:
        children.setdefault(n["parent_id"], []).append(n)

    def tokens(node: dict[str, Any]) -> list[Token]:
        token = Token(node["type"], node["tag"], node["nesting"])
        token.content = node["text"]
        token.hidden = node["hidden"]
        token.block = node["block"]
        token.info = node["info"]
        token.markup = node["markup"]
        token.attrs = dict(node["token_attributes"])
        token.meta = node["token_meta"]
        nested = [t for child in children.get(node["id"], []) for t in tokens(child)]
        if node["type"] == "list_item_open" and "task_marker" in node["attributes"]:
            inline = next((t for t in nested if t.type == "inline"), None)
            if inline and inline.children and inline.children[0].type == "text":
                first = inline.children[0]
                marker = node["attributes"]["task_marker"]
                if not first.content.startswith("[" + marker + "]"):
                    raise AssertionError("Task source and structural state disagree")
                first.content = first.content[3:]
                checkbox = Token("html_inline", "", 0)
                checkbox.content = (
                    '<input disabled="" type="checkbox"'
                    + (' checked=""' if node["attributes"]["checked"] else "")
                    + ">"
                )
                inline.children.insert(0, checkbox)
        if node["type"] == "inline":
            token.children = nested
            return [token]
        if node["nesting"] == 1:
            close = Token(node["type"].removesuffix("_open") + "_close", node["tag"], -1)
            close.hidden = token.hidden
            close.block = token.block
            return [token, *nested, close]
        return [token]

    stream = [t for n in children.get(None, []) for t in tokens(n)]
    renderer = MarkdownIt("commonmark")
    renderer.renderer.rules["obsidian_tag"] = lambda ts, i, *_: html.escape(ts[i].content)
    renderer.renderer.rules["obsidian_link"] = lambda ts, i, *_: html.escape(ts[i].content)
    return renderer.renderer.render(stream, renderer.options, {})


def examples(directory: Path) -> list[dict[str, Any]]:
    records = json.loads((directory / "commonmark-0.31.2.json").read_text(encoding="utf-8"))
    records = [
        dict(
            corpus="CommonMark-0.31.2",
            id=e["example"],
            section=e["section"],
            source=e["markdown"],
            expected=e["html"],
        )
        for e in records
    ]
    page = (directory / "gfm-0.29.html").read_text(encoding="utf-8")
    pattern = (
        r'class="example" id="example-(\d+)">[\s\S]*?'
        r'<code class="language-markdown">([\s\S]*?)</code>[\s\S]*?'
        r'<code class="language-html">([\s\S]*?)</code>'
    )
    for number, source, expected in re.findall(pattern, page):

        def decode(s: str) -> str:
            return html.unescape(re.sub(r"</?span[^>]*>", "", s)).replace("→", "\t")

        records.append(
            dict(
                corpus="GFM-0.29",
                id=int(number),
                section="official",
                source=decode(source),
                expected=decode(expected),
            )
        )
    return records


def check_metadata(metadata: list[dict[str, Any]], gold: dict[str, Any]) -> None:
    nodes = [n for m in metadata for n in m["nodes"]]
    if "required_wikilink_target" in gold:
        assert any(
            n["type"] == "obsidian_link"
            and n["attributes"]["target"] == gold["required_wikilink_target"]
            for n in nodes
        )
    if "required_link_href" in gold:
        assert any(
            n["type"] == "link_open"
            and n["attributes"].get("href") == gold["required_link_href"]
            and n["token_meta"].get("gfm_extension")
            for n in nodes
        )
    if "required_properties" in gold:
        assert any(m.get("properties") == gold["required_properties"] for m in metadata)
    for node in nodes:
        if "html_source_tree" not in node:
            continue
        tree = node["html_source_tree"]
        assert "".join(t["raw_text"] for t in tree[0]["source_tokens"]) == node["text"]
        children: dict[int, list[dict[str, Any]]] = {}
        for child in tree[1:]:
            assert 0 <= child["parent_id"] < child["id"]
            children.setdefault(child["parent_id"], []).append(child)

        def native(parent: int) -> str:
            result = []
            for n in children.get(parent, []):
                if n["type"] == "html_text":
                    result.append(html.escape(n["text"], quote=False))
                elif n["type"] == "html_comment":
                    result.append("<!--" + n["text"] + "-->")
                else:
                    tag = n["tag"]
                    attributes = "".join(
                        " " + k + '="' + html.escape(v, quote=True) + '"'
                        for k, v in n["attributes"].items()
                    )
                    inner = native(n["id"])
                    # HTML5 discards one initial textarea LF. Compensate when
                    # serializing the already-parsed native tree for comparison.
                    if tag == "textarea" and inner.startswith("\n"):
                        inner = "\n" + inner
                    result.append("<" + tag + attributes + ">" + inner)
                    if tag not in {"area", "br", "col", "hr", "meta", "param", "wbr"}:
                        result.append("</" + tag + ">")
            return "".join(result)

        assert normalize(native(0)) == normalize(node["text"]), (
            "Captured native HTML hierarchy differs from its HTML5 source semantics"
        )


def evaluate(
    record: dict[str, Any],
    source: str,
    expected: str,
    metadata: list[dict[str, Any]],
    content: str,
    gold: dict[str, Any],
) -> dict[str, Any]:
    result = {k: v for k, v in record.items() if k not in {"source", "expected"}}
    result["source_sha256"] = hashlib.sha256(source.encode()).hexdigest()
    result["oracle"] = gold.get("kind", "OFFICIAL_HTML_STRUCTURE")
    if gold:
        assert result["source_sha256"] == gold["source_sha256"]
    if gold.get("kind") == "REQUIRED_WHOLE_DOCUMENT_FAILURE":
        raise AssertionError("A document required to fail was accepted")
    check_metadata(metadata, gold)
    actual = rendered_ast(metadata)
    expected = gold.get("expected_html", expected)
    result["exact_source"] = content == source
    result["structure"] = (
        "PASS" if result["exact_source"] and normalize(actual) == normalize(expected) else "FAILED"
    )
    if result["structure"] == "FAILED":
        result.update(actual=actual, expected=expected)
    return result


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--official", type=Path, required=True)
    parser.add_argument("--output", type=Path, required=True)
    parser.add_argument("--profile-gold", type=Path)
    parser.add_argument("--producer", action="store_true")
    parser.add_argument(
        "--resolution",
        type=Path,
        help="Explicit source-hashed corrective label overlay; original gold retained",
    )
    args = parser.parse_args()
    results = []
    records = examples(args.official)
    gold_data = (
        json.loads(args.profile_gold.read_text(encoding="utf-8"))
        if args.profile_gold
        else {"records": []}
    )
    assert gold_data.get("human_reviewer") is None and gold_data.get("signed_at") is None
    gold_map = {(g["corpus"], g["id"]): g for g in gold_data["records"]}
    if args.resolution:
        resolution = json.loads(args.resolution.read_text(encoding="utf-8"))
        assert (
            resolution["original_gold_sha256"]
            == hashlib.sha256(args.profile_gold.read_bytes()).hexdigest()
        )
        for record in resolution["records"]:
            key = (record["corpus"], record["id"])
            if key in gold_map:
                assert gold_map[key]["source_sha256"] == record["source_sha256"]
            gold_map[key] = record
    captured_outputs: dict[tuple[str, int], dict[str, Any]] = {}
    if args.producer:
        import ppv_rag.worker as worker
        from ppv_rag.config import Settings
        from ppv_rag.contracts import (
            DocumentBatchStageRequest,
            DocumentRootTask,
            document_input_digest,
        )

        worker.settings = Settings(
            service_key="synthetic-test-service-credential-00000000",
            worker_dsn="postgresql://rag_worker@127.0.0.1:5432/vault",
        )
        worker.verify_document_grant = lambda _request: None
        captured: list[dict[str, Any]] = []
        worker.stage_document_result = lambda **kwargs: captured.append(kwargs)
        for offset in range(0, len(records), 100):
            group = records[offset : offset + 100]
            captured.clear()
            roots = [
                DocumentRootTask(
                    root_key="official-" + str(offset + i),
                    source_id="official-" + str(offset + i),
                    source_version=1,
                    format="md",
                    content_text=r["source"],
                    approved_fields=["content"],
                )
                for i, r in enumerate(group)
            ]
            request = DocumentBatchStageRequest(
                job_id="11111111-1111-1111-1111-111111111111",
                attempt_id="22222222-2222-2222-2222-222222222222",
                lease_token="a" * 64,
                roots=roots,
                input_digest=document_input_digest(roots),
            )
            response = worker.process_document_batch_request(request)
            assert response.published_count == 0 and len(captured) == len(group)
            for record, output in zip(group, captured, strict=True):
                captured_outputs[record["corpus"], record["id"]] = dict(output)
    for record in records:
        source, expected = record["source"], record["expected"]
        gold = gold_map.get((record["corpus"], record["id"]), {})
        result = {**record, "source_sha256": hashlib.sha256(source.encode()).hexdigest()}
        result.pop("source")
        result.pop("expected")
        try:
            if args.producer:
                output = captured_outputs[record["corpus"], record["id"]]
                chunks_payload = output["chunks_payload"]
                if not output["success"]:
                    assert not chunks_payload
                    raise MarkdownFailure(output["reason_code"])
                for c in chunks_payload:
                    loc = c["locator"]
                    assert c["content"] == source[loc["char_start"] : loc["char_end"]]
                    assert loc["source_version"] == 1
                    assert len(c["embedding"]) == 384
                metadata = [
                    expand_metadata(m)
                    for c in chunks_payload
                    for m in c["structure_metadata"]["constituent_metadata"]
                    if m.get("source_structure_definition", True)
                ]
                content = "".join(c["content"] for c in chunks_payload)
            else:
                doc = parse_obsidian(source, "official", 1, "content")
                chunks = StructureFirstChunker().chunk(doc)
                metadata = [
                    m
                    for c in chunks
                    for m in c.structure_metadata["constituent_metadata"]
                    if m.get("source_structure_definition", True)
                ]
                content = "".join(c.content for c in chunks)
            result = evaluate(record, source, expected, metadata, content, gold)
        except MarkdownFailure as exc:
            result.update(
                structure="PASS_REQUIRED_REJECTION"
                if gold.get("reason") == exc.code
                and result["source_sha256"] == gold["source_sha256"]
                else "UNEXPECTED_REJECTION",
                reason=exc.code,
            )
        except Exception as exc:
            result.update(structure="ERROR", reason=type(exc).__name__, detail=str(exc))
        results.append(result)
    counts = Counter(r["structure"] for r in results)
    args.output.write_text(
        json.dumps(
            dict(
                purpose=(
                    "Actual extracted AST reconstructed for structural comparison "
                    "against official expected HTML, not source equality alone."
                ),
                counts=dict(counts),
                mode="PRODUCER_LOCAL_REAL_CPU_CAPTURE" if args.producer else "PARSER_CHUNKER_AST",
                profile_gold_sha256=hashlib.sha256(args.profile_gold.read_bytes()).hexdigest()
                if args.profile_gold
                else None,
                independent_human_review="PENDING; no signature or adjudication claimed",
                resolution_sha256=hashlib.sha256(args.resolution.read_bytes()).hexdigest()
                if args.resolution
                else None,
                results=results,
            ),
            ensure_ascii=False,
            indent=2,
        )
        + "\n",
        encoding="utf-8",
    )
    print(json.dumps(dict(counts=counts)))
    if counts["FAILED"] or counts["ERROR"] or counts["UNEXPECTED_REJECTION"]:
        raise SystemExit(1)


if __name__ == "__main__":
    main()

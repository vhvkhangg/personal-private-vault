"""R03-only source/container regressions with real CPU producer JSON coverage."""

import json

import pytest
from test_d17_final_corrective import producer as producer

from ppv_rag.chunking.chunker import StructureFirstChunker
from ppv_rag.parsers.markdown import ApprovedDependency
from ppv_rag.parsers.obsidian import expand_metadata, parse_obsidian

CHILD = "Child source revision seven.\n"
BINDING = {"Child": ApprovedDependency("child", 7, "content", CHILD)}
WIRE_BINDING = {
    "Child": dict(source_key="child", source_version=7, approved_field="content", text=CHILD)
}

# source, Markdown list-item count, heading count, active child-embed count, strong count.
CASES = [
    ("<div/>\n\n**literal** ![[Child]]\n", 0, 0, 0, 0),
    ("Before <span/>**literal** ![[Child]] after.\n", 0, 0, 0, 0),
    ("<div><span/>\n\n![[Child]]</div>\n\n# Outside\n", 0, 1, 0, 0),
    ("Before <span/>literal</span> and **outside**.\n", 0, 0, 0, 1),
    ("- <div>\n- foo\n", 2, 0, 0, 0),
    ("- <div>\n  native\n\n# Outside heading\n", 1, 1, 0, 0),
    ("- <div>\n- ![[Child]]\n", 2, 0, 1, 0),
    ("- <div/>\n- ![[Child]]\n", 2, 0, 1, 0),
    ("- outer\n  - <div>\n  - ![[Child]]\n\n# After\n", 3, 1, 1, 0),
    ("> - <div>\n> - ![[Child]]\n\n# After\n", 2, 1, 1, 0),
    ("- > <div>\n  > native\n\n- ![[Child]]\n\n# After\n", 2, 1, 1, 0),
    (
        "> - outer\n>   - <div>\n>     literal ![[NotBound]]\n>   - ![[Child]]\n>\n"
        "> # Quote heading\n\n# Root heading\n",
        3,
        2,
        1,
        0,
    ),
    ("- <div>\r\n  native\r\n\r\n# Outside\r\n", 1, 1, 0, 0),
    ("1. <div>\n2. ![[Child]]\n\n# After\n", 2, 1, 1, 0),
    ("<div>\n\n- **literal** ![[Child]]\n# literal\n", 0, 0, 0, 0),
    ("- <div>\n\n  **literal** ![[NotBound]]\n\n- ![[Child]]\n", 2, 0, 1, 0),
    ("1. Before <span/>![[NotBound]]\n2. ![[Child]]\n", 2, 0, 1, 0),
    ("<br/> ![[Child]]\n", 0, 0, 1, 0),
    ("<hr/>\n\n![[Child]]\n\n# After\n", 0, 1, 1, 0),
    ("<textarea/>\n\n**literal** ![[Child]]\n", 0, 0, 0, 0),
    ("<title/>**literal** ![[Child]]</title>\n", 0, 0, 0, 0),
    ("<ul><li>one<li>two</ul>\n\n# Outside\n", 0, 1, 0, 0),
    ("> <div/>\n>\n> ![[NotBound]]\n\n![[Child]]\n", 0, 0, 1, 0),
]


def assert_structure(metadata, case):
    nodes = [n for m in metadata for n in m["nodes"]]
    types = [n["type"] for n in nodes]
    for kind, expected in zip(
        ("list_item_open", "heading_open", "obsidian_embed", "strong_open"), case[1:], strict=True
    ):
        assert types.count(kind) == expected, (case[0], kind, types)
    by_id = {n["id"]: n for n in nodes}
    for node in nodes:
        if node["parent_id"] is not None:
            assert node["parent_id"] in by_id
        if node["type"] == "obsidian_embed":
            assert node["attributes"]["target"] == "Child"
    items = [n for n in nodes if n["type"] == "list_item_open"]
    if len(items) == 2:
        assert items[0]["parent_id"] == items[1]["parent_id"]  # Siblings, not nested items.
    if len(items) == 3:
        assert items[1]["parent_id"] == items[2]["parent_id"]
        nested_list = by_id[items[1]["parent_id"]]
        assert nested_list["parent_id"] == items[0]["id"]
    headings = [n for n in nodes if n["type"] == "heading_open"]
    for node in headings:
        if node["parent_id"] is not None:
            assert by_id[node["parent_id"]]["type"] == "blockquote_open"
        else:
            assert node["locator"]["char_start"] >= case[0].rfind("\n#")
    return nodes


def assert_chunks(chunks, source, root):
    sources = {root: (source, 3), "child": (CHILD, 7)}
    assert "".join(c["content"] for c in chunks if c["source_identity"] == root) == source
    for chunk in chunks:
        text, revision = sources[chunk["source_identity"]]
        loc = chunk["locator"]
        assert loc["source_key"] == chunk["source_identity"] and loc["source_version"] == revision
        assert chunk["content"] == text[loc["char_start"] : loc["char_end"]]
        assert loc["line_start"] == 1 + text[: loc["char_start"]].count("\n")
        assert loc["line_end"] == 1 + text[: max(loc["char_start"], loc["char_end"] - 1)].count(
            "\n"
        )
        if chunk["source_identity"] == "child":
            assert loc["dependency_edges"]


@pytest.mark.parametrize("case", CASES)
def test_parser_chunker_boundaries(case):
    doc = parse_obsidian(case[0], "root", 3, "content", BINDING)
    assert_structure([e.metadata for e in doc.elements if e.source_identity == "root"], case)
    for budget in (37, 1500):
        chunks = StructureFirstChunker(target_chars=budget).chunk(doc)
        wire = json.loads(
            json.dumps(
                [
                    dict(content=c.content, source_identity=c.source_identity, locator=c.locator)
                    for c in chunks
                ]
            )
        )
        assert_chunks(wire, case[0], "root")
        assert any(c["source_identity"] == "child" for c in wire) == bool(case[3])


@pytest.mark.parametrize("bound", [False, True])
def test_actual_cpu_producer_json_boundaries(producer, bound):
    response, outputs = producer([c[0] for c in CASES], WIRE_BINDING if bound else None)
    expected_failures = sum(bool(c[3]) and not bound for c in CASES)
    assert (response.staged_count, response.failed_count) == (
        len(CASES) - expected_failures,
        expected_failures,
    )
    assert response.published_count == 0
    for index, (case, output) in enumerate(zip(CASES, outputs, strict=True)):
        if case[3] and not bound:
            assert not output["success"] and output["chunks_payload"] == []
            assert output["reason_code"].startswith("MD_")
            continue
        assert output["success"], (case[0], output["reason_code"])
        assert output["resource_report"]["pipeline_identity"]["provider"] == "CPU"
        chunks = output["chunks_payload"]
        assert_chunks(chunks, case[0], f"root-{index}")
        encoded = [
            m
            for c in chunks
            if c["source_identity"] == f"root-{index}"
            for m in c["structure_metadata"]["constituent_metadata"]
        ]
        definitions = {
            m["source_structure_id"]: m for m in encoded if m.get("source_structure_definition")
        }
        metadata = [expand_metadata(m, definitions) for m in encoded]
        assert_structure(metadata, case)
        assert all(len(c["embedding"]) == 384 for c in chunks)
        assert any(c["source_identity"] == "child" for c in chunks) == bool(case[3])


def test_nonvoid_active_html_rejects_complete_roots(producer):
    sources = [
        "Valid prefix.\n\n" + text
        for text in [
            "<div/>\n\n<script>active()</script>",
            '<span/>text <a onclick="active()">bad</a>',
            '<div/>\n\n<img src="https://example.invalid/image.png"/>',
            "<div>\n\ninside\n\n</div>\n\n![[NotBound]]",
        ]
    ]
    response, outputs = producer(sources)
    assert (response.staged_count, response.failed_count) == (0, len(sources))
    assert all(not o["success"] and o["chunks_payload"] == [] for o in outputs)
    assert [o["reason_code"] for o in outputs[:3]] == ["MD_UNSUPPORTED_ACTIVE_HTML"] * 3

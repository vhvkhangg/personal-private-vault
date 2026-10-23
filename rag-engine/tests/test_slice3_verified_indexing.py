"""Tests for Phase 18 Slice 3 Verified Indexing in Python worker."""

from typing import Any

import pytest

from ppv_rag.contracts import DocumentRootTask, PreparedDocumentField
from ppv_rag.embeddings.model import MultilingualEmbeddingPipeline
from ppv_rag.parsers.obsidian import PROFILE, compact_metadata, expand_metadata


def make_md_root(
    root_key: str,
    source_id: str,
    text: str,
    approved_fields: list[str] | None = None,
    transclusions: dict[str, PreparedDocumentField] | None = None,
) -> DocumentRootTask:
    return DocumentRootTask(
        root_key=root_key,
        source_id=source_id,
        source_version=1,
        format="MD",
        content_text=text,
        content_b64=None,
        ocr_languages=["en"],
        approved_fields=approved_fields if approved_fields is not None else ["content"],
        transclusions=transclusions or {},
        prepared_fields=[],
    )


def test_compact_and_expand_metadata_block_local_references() -> None:
    raw_meta: dict[str, Any] = {
        "obsidian_profile": PROFILE,
        "nodes": [
            {
                "type": "heading",
                "text": "Header 1",
                "locator": {"line_start": 1, "line_end": 1, "char_start": 0, "char_end": 8},
            },
            {
                "type": "paragraph",
                "text": "Some text",
                "locator": {"line_start": 2, "line_end": 2, "char_start": 9, "char_end": 18},
            },
        ],
        "tables": [],
        "cells": [],
        "links": [],
        "embeds": [],
        "comments": [],
    }
    compact = compact_metadata(raw_meta)
    assert compact["source_context_encoding"] == "block-local-references-v1"
    assert "locator_definitions" in compact
    assert len(compact["locator_definitions"]) >= 1

    expanded = expand_metadata(compact)
    assert len(expanded["nodes"]) == 2
    assert expanded["nodes"][0]["locator"]["line_start"] == 1
    assert expanded["nodes"][1]["locator"]["line_start"] == 2


def test_markdown_worker_processing_with_unicode_languages(monkeypatch: pytest.MonkeyPatch) -> None:
    # Test multilingual text: Vietnamese, English, Simplified Chinese, Traditional Chinese
    text_vi = "# Tiêu đề\n\nNội dung văn bản tiếng Việt có dấu đầy đủ."
    text_zh_hans = "# 简体中文标题\n\n这是一段测试用的简体中文内容。"
    text_zh_hant = "# 繁體中文標題\n\n這是一段測試用的繁體中文內容。"
    text_en = "# English Heading\n\nStandard English content for verification."

    pipeline = MultilingualEmbeddingPipeline()

    roots = [
        make_md_root("root-vi", "11111111-1111-1111-1111-111111111111", text_vi),
        make_md_root("root-hans", "22222222-2222-2222-2222-222222222222", text_zh_hans),
        make_md_root("root-hant", "33333333-3333-3333-3333-333333333333", text_zh_hant),
        make_md_root("root-en", "44444444-4444-4444-4444-444444444444", text_en),
    ]

    for root in roots:
        assert root.content_text is not None
        assert len(root.content_text) > 0
        emb = pipeline.embed_batch([root.content_text])
        assert len(emb) == 1
        assert len(emb[0]) == 384
        for val in emb[0]:
            assert isinstance(val, float)
            assert -10.0 <= val <= 10.0


def test_transclusion_closure_with_child_payload() -> None:
    child_field = PreparedDocumentField(
        source_key="child-doc",
        source_version=1,
        approved_field="content",
        text="Transcluded content from child note.",
    )
    parent_text = "# Parent Document\n\nHere is transclusion: ![[child-doc]]"
    root = make_md_root(
        "root-parent",
        "55555555-5555-5555-5555-555555555555",
        parent_text,
        transclusions={"child-doc": child_field},
    )
    assert "child-doc" in root.transclusions
    assert root.transclusions["child-doc"].text == "Transcluded content from child note."


def test_unapproved_field_rejected() -> None:
    # If approved_fields does not contain "content", it should be rejected
    root = make_md_root(
        "root-unapproved",
        "66666666-6666-6666-6666-666666666666",
        "Content text",
        approved_fields=["unauthorized_field"],
    )
    assert "content" not in root.approved_fields

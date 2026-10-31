from __future__ import annotations

from ppv_rag.qa_generation import (
    C1_MODEL_SHA256,
    C1_MODEL_VERSION,
    QA_JSON_SCHEMA,
    build_system_prompt,
    build_user_prompt,
    generate_grounded_answer,
    strip_think_tags,
)


def test_strip_think_tags() -> None:
    text = '<think>Some reasoning</think>{"outcome": "ANSWERED"}'
    cleaned = strip_think_tags(text)
    assert cleaned == '{"outcome": "ANSWERED"}'

    text_md = '```json\n{"outcome": "ABSTAIN"}\n```'
    cleaned_md = strip_think_tags(text_md)
    assert cleaned_md == '{"outcome": "ABSTAIN"}'


def test_build_system_prompt() -> None:
    prompt = build_system_prompt()
    assert "Personal Private Vault" in prompt
    assert "STRICT GROUNDING" in prompt
    assert "ANSWERED" in prompt
    assert "PARTIAL" in prompt
    assert "CONFLICT" in prompt
    assert "ABSTAIN" in prompt


def test_build_user_prompt() -> None:
    evidence = [
        {
            "citation_key": "[1]",
            "source_name": "note.md",
            "excerpt": "An lives in Hanoi.",
        }
    ]
    history = [{"role": "user", "content": "Hello"}]
    prompt = build_user_prompt(
        question="Where does An live?",
        evidence=evidence,
        history=history,
        response_language="en",
    )
    assert "[1] Source: note.md" in prompt
    assert "An lives in Hanoi." in prompt
    assert "User: Hello" in prompt
    assert "QUESTION:\nWhere does An live?" in prompt
    assert "REQUESTED RESPONSE LANGUAGE: en" in prompt


def test_qa_json_schema_valid() -> None:
    assert QA_JSON_SCHEMA["type"] == "object"
    assert "outcome" in QA_JSON_SCHEMA["properties"]
    assert "claims" in QA_JSON_SCHEMA["properties"]
    assert "unanswered_parts" in QA_JSON_SCHEMA["properties"]
    assert "conflict_pairs" in QA_JSON_SCHEMA["properties"]


def test_empty_evidence_abstains() -> None:
    res = generate_grounded_answer(
        question="Anything here?",
        evidence=[],
    )
    assert res.outcome == "ABSTAIN"
    assert res.claims == []
    assert res.unanswered_parts == ["No evidence available"]
    assert res.conflict_pairs == []
    assert res.model_version == C1_MODEL_VERSION
    assert res.artifact_hash == C1_MODEL_SHA256

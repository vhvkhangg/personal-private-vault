"""P19-S19-2 — SUSPENDED / NOT_ACCEPTED / ARCHIVAL_ONLY; retained QA-only tests.

Authority: docs/implementation/phase-19/owner-decision-s19-2-source-archival-only-2026-10-31.md.
The archival task does not authorize running these generation tests.
"""

from __future__ import annotations

import hashlib
from unittest.mock import patch

from fastapi.testclient import TestClient
from ppv_rag.grounded_qa import app, init_service
from ppv_rag.qa_generation import (
    C1_MODEL_SHA256,
    C1_MODEL_VERSION,
    ClaimEntry,
    QaGenerationResult,
    SupportEntry,
)

READER_ID = "13f7f818-0cd0-4a74-b523-2b4b8b6d5bbf"
NONCE = "e7b0e1e0-639a-4c22-959c-7ecb6df7d012"
SERVICE_KEY = "test-secret-key-12345678901234567890"

init_service(reader_id=READER_ID, endpoint_nonce=NONCE, service_key=SERVICE_KEY)
client = TestClient(app)

AUTH_HEADERS = {
    "X-PPV-Service-Key": SERVICE_KEY,
    "X-PPV-Reader-Id": READER_ID,
    "X-PPV-Nonce": NONCE,
}


def _calc_scope_digest(evidence: list[dict[str, str]]) -> str:
    h = hashlib.sha256()
    for ev in evidence:
        h.update(ev.get("citation_key", "").encode("utf-8"))
        h.update(b"\x00")
        h.update(ev.get("excerpt", "").encode("utf-8"))
        h.update(b"\x00")
    return h.hexdigest()


def test_readiness_unauthorized() -> None:
    resp = client.get("/v1/query/readiness")
    assert resp.status_code == 401


def test_readiness_success() -> None:
    resp = client.get("/v1/query/readiness", headers=AUTH_HEADERS)
    assert resp.status_code == 200
    data = resp.json()
    assert data["status"] == "READY"
    assert data["contract_version"] == "1.0"
    assert data["reader_id"] == READER_ID
    assert data["endpoint_nonce"] == NONCE
    assert data["dimensions"] == 384


def test_query_embed_duplicate_key_rejected() -> None:
    dup_body = b'{"reader_id": "' + READER_ID.encode() + b'", "question": "q", "question": "q2"}'
    resp = client.post(
        "/v1/query/embed",
        content=dup_body,
        headers={**AUTH_HEADERS, "Content-Type": "application/json"},
    )
    assert resp.status_code == 400
    assert "Duplicate key" in resp.json()["detail"]


def test_query_embed_success() -> None:
    body = {"reader_id": READER_ID, "question": "Nh\u00e2n v\u1eadt An l\u00e0 ai?"}
    resp = client.post(
        "/v1/query/embed",
        json=body,
        headers=AUTH_HEADERS,
    )
    assert resp.status_code == 200
    data = resp.json()
    assert data["reader_id"] == READER_ID
    assert len(data["vector"]) == 384


def test_qa_generate_unauthorized() -> None:
    body = {
        "reader_id": READER_ID,
        "nonce": NONCE,
        "question": "An sinh nam nao?",
        "evidence": [],
        "scope_digest": _calc_scope_digest([]),
    }
    resp = client.post("/v1/qa/generate", json=body)
    assert resp.status_code == 401


def test_qa_generate_history_limit_rejected() -> None:
    init_service(reader_id=READER_ID, endpoint_nonce=NONCE, service_key=SERVICE_KEY)
    history = [{"role": "user", "content": f"msg {i}"} for i in range(5)]
    body = {
        "reader_id": READER_ID,
        "nonce": NONCE,
        "question": "An sinh nam nao?",
        "evidence": [],
        "history": history,
        "scope_digest": _calc_scope_digest([]),
    }
    resp = client.post("/v1/qa/generate", json=body, headers=AUTH_HEADERS)
    assert resp.status_code == 400
    assert "History exceeds maximum allowed turns" in resp.json()["detail"]


def test_qa_generate_errors_return_only_fixed_categories() -> None:
    evidence = [{"citation_key": "[1]", "source_name": "safe.md", "excerpt": "Safe fact."}]
    body = {
        "reader_id": READER_ID,
        "nonce": NONCE,
        "question": "Question?",
        "evidence": evidence,
        "scope_digest": _calc_scope_digest(evidence),
    }
    for failure, category in [
        (ValueError("PRIVATE_ERROR_SENTINEL"), "CANDIDATE_INVALID"),
        (RuntimeError("PRIVATE_ERROR_SENTINEL"), "GENERATION_UNAVAILABLE"),
        (RuntimeError("QA_SYSTEM_HEADROOM_BELOW_2_GIB"), "HEADROOM_UNAVAILABLE"),
    ]:
        init_service(reader_id=READER_ID, endpoint_nonce=NONCE, service_key=SERVICE_KEY)
        with patch("ppv_rag.grounded_qa.generate_grounded_answer", side_effect=failure):
            response = client.post("/v1/qa/generate", json=body, headers=AUTH_HEADERS)
        assert response.status_code == 500
        assert response.json()["detail"] == {"code": "QA_GENERATION_FAILED", "category": category}
        assert "PRIVATE_ERROR_SENTINEL" not in response.text


def test_qa_generate_mocked_success() -> None:
    init_service(reader_id=READER_ID, endpoint_nonce=NONCE, service_key=SERVICE_KEY)
    mock_result = QaGenerationResult(
        outcome="ANSWERED",
        claims=[
            ClaimEntry(
                id="c1",
                text="An sinh nam 1990.",
                supports=[SupportEntry(citation_key="[1]", quote="sinh nam 1990")],
            )
        ],
        unanswered_parts=[],
        conflict_pairs=[],
        model_version=C1_MODEL_VERSION,
        artifact_hash=C1_MODEL_SHA256,
        measurements={"model-load-ns": 123, "prompt-tokens": 42},
    )

    with patch("ppv_rag.grounded_qa.generate_grounded_answer", return_value=mock_result):
        ev = [
            {
                "citation_key": "[1]",
                "source_name": "an.md",
                "excerpt": "An sinh nam 1990 tai Ha Noi.",
            }
        ]
        body = {
            "reader_id": READER_ID,
            "nonce": NONCE,
            "question": "An sinh nam nao?",
            "evidence": ev,
            "response_language": "vi",
            "scope_digest": _calc_scope_digest(ev),
        }
        resp = client.post("/v1/qa/generate", json=body, headers=AUTH_HEADERS)
        assert resp.status_code == 200
        data = resp.json()
        assert data["outcome"] == "ANSWERED"
        assert len(data["claims"]) == 1
        assert data["claims"][0]["id"] == "c1"
        assert data["claims"][0]["supports"][0]["citation_key"] == "[1]"
        assert data["claims"][0]["supports"][0]["quote"] == "sinh nam 1990"
        assert data["model_version"] == C1_MODEL_VERSION
        assert data["artifact_hash"] == C1_MODEL_SHA256
        assert resp.headers["X-PPV-QA-model-load-ns"] == "123"
        assert resp.headers["X-PPV-QA-prompt-tokens"] == "42"
        assert "measurements" not in data

        # Replay rejection check
        replay_resp = client.post("/v1/qa/generate", json=body, headers=AUTH_HEADERS)
        assert replay_resp.status_code == 409


def test_qa_generate_scope_digest_mismatch_rejected() -> None:
    init_service(reader_id=READER_ID, endpoint_nonce=NONCE, service_key=SERVICE_KEY)
    ev = [
        {
            "citation_key": "[1]",
            "source_name": "an.md",
            "excerpt": "An sinh nam 1990 tai Ha Noi.",
        }
    ]
    body = {
        "reader_id": READER_ID,
        "nonce": NONCE,
        "question": "An sinh nam nao?",
        "evidence": ev,
        "response_language": "vi",
        "scope_digest": "wrong_scope_digest_000000000000000000000000000000000000000000000000",
    }
    resp = client.post("/v1/qa/generate", json=body, headers=AUTH_HEADERS)
    assert resp.status_code == 400
    assert "Scope digest mismatch" in resp.json()["detail"]


def test_qa_generate_nonce_mismatch_rejected() -> None:
    init_service(reader_id=READER_ID, endpoint_nonce=NONCE, service_key=SERVICE_KEY)
    body = {
        "reader_id": READER_ID,
        "nonce": "wrong-nonce",
        "question": "An sinh nam nao?",
        "evidence": [],
        "response_language": "vi",
        "scope_digest": _calc_scope_digest([]),
    }
    resp = client.post("/v1/qa/generate", json=body, headers=AUTH_HEADERS)
    assert resp.status_code == 403
    assert "Mismatched or missing nonce" in resp.json()["detail"]

"""Tests for request-bound query embedding service."""

from __future__ import annotations

import math
import os
import uuid

import pytest
from fastapi.testclient import TestClient
from ppv_rag.embeddings.model import (
    DIMENSION,
    MODEL_ARTIFACT_SHA256,
    MODEL_VERSION,
    TOKENIZER_ARTIFACT_SHA256,
    TOKENIZER_VERSION,
)
from ppv_rag.query_embedding import app, init_service


@pytest.fixture(autouse=True)
def setup_service() -> tuple[str, str, str]:
    reader_id = str(uuid.uuid4())
    nonce = str(uuid.uuid4())
    service_key = "test-service-key-" + uuid.uuid4().hex
    init_service(reader_id=reader_id, nonce=nonce, service_key=service_key)
    return reader_id, nonce, service_key


def test_query_readiness(setup_service: tuple[str, str, str]) -> None:
    reader_id, nonce, service_key = setup_service
    client = TestClient(app)

    # 1. Successful readiness check
    resp = client.get(
        "/v1/query/readiness",
        headers={
            "X-RAG-Service-Key": service_key,
            "X-RAG-Endpoint-Nonce": nonce,
        },
    )
    assert resp.status_code == 200
    data = resp.json()
    assert data["status"] == "READY"
    assert data["contract_version"] == "1.0"
    assert data["reader_id"] == reader_id
    assert data["endpoint_nonce"] == nonce
    assert data["dimensions"] == DIMENSION
    assert data["model_version"] == MODEL_VERSION
    assert data["artifact_hash"] == MODEL_ARTIFACT_SHA256
    assert data["tokenizer_version"] == TOKENIZER_VERSION
    assert data["tokenizer_artifact_hash"] == TOKENIZER_ARTIFACT_SHA256
    assert data["process_id"] == os.getpid()

    # 2. Missing service key
    resp_no_key = client.get(
        "/v1/query/readiness",
        headers={"X-RAG-Endpoint-Nonce": nonce},
    )
    assert resp_no_key.status_code == 401

    # 3. Wrong service key
    resp_wrong_key = client.get(
        "/v1/query/readiness",
        headers={
            "X-RAG-Service-Key": "wrong-key",
            "X-RAG-Endpoint-Nonce": nonce,
        },
    )
    assert resp_wrong_key.status_code == 401

    # 4. Wrong nonce
    resp_wrong_nonce = client.get(
        "/v1/query/readiness",
        headers={
            "X-RAG-Service-Key": service_key,
            "X-RAG-Endpoint-Nonce": "wrong-nonce",
        },
    )
    assert resp_wrong_nonce.status_code == 403


def test_query_embed_multilingual(setup_service: tuple[str, str, str]) -> None:
    reader_id, nonce, service_key = setup_service
    client = TestClient(app)
    headers = {
        "X-RAG-Service-Key": service_key,
        "X-RAG-Endpoint-Nonce": nonce,
        "X-RAG-Reader-Id": reader_id,
    }

    test_queries = [
        "Thuộc tính này được mô tả ở đâu?",
        "Where is this property described in the documentation?",
        "属性在系统的哪个部分描述？",
    ]

    for q in test_queries:
        resp = client.post(
            "/v1/query/embed",
            json={"question": q},
            headers=headers,
        )
        assert resp.status_code == 200, f"Query '{q}' failed: {resp.text}"
        data = resp.json()
        assert data["reader_id"] == reader_id
        assert data["contract_version"] == "1.0"
        assert data["endpoint_nonce"] == nonce
        assert data["process_id"] == os.getpid()
        assert data["dimensions"] == 384
        assert data["model_version"] == MODEL_VERSION
        assert data["artifact_hash"] == MODEL_ARTIFACT_SHA256
        assert data["tokenizer_version"] == TOKENIZER_VERSION
        assert data["tokenizer_artifact_hash"] == TOKENIZER_ARTIFACT_SHA256

        vector = data["vector"]
        assert len(vector) == 384
        assert all(math.isfinite(x) for x in vector)
        l2_norm = math.sqrt(sum(x * x for x in vector))
        assert abs(l2_norm - 1.0) < 1e-4


def test_query_body_protocol_bound(setup_service: tuple[str, str, str]) -> None:
    reader_id, nonce, service_key = setup_service
    with TestClient(app) as client:
        response = client.post(
            "/v1/query/embed",
            content=iter([b"SENTINEL_PRIVATE_" * 3_000, b"x" * 30_000]),
            headers={
                "X-PPV-Service-Key": service_key,
                "X-PPV-Nonce": nonce,
                "X-PPV-Reader-Id": reader_id,
            },
        )
    assert response.status_code == 400
    assert response.json()["detail"] == "Query payload exceeds the protocol bound"
    assert "SENTINEL_PRIVATE" not in response.text


def test_query_embed_validation(setup_service: tuple[str, str, str]) -> None:
    reader_id, nonce, service_key = setup_service
    client = TestClient(app)
    headers = {
        "X-RAG-Service-Key": service_key,
        "X-RAG-Endpoint-Nonce": nonce,
        "X-RAG-Reader-Id": reader_id,
    }

    # Empty question
    resp_empty = client.post("/v1/query/embed", json={"question": "   "}, headers=headers)
    assert resp_empty.status_code == 400

    # Oversized character count (>4096 code points)
    long_chars = "a" * 4097
    resp_chars = client.post("/v1/query/embed", json={"question": long_chars}, headers=headers)
    assert resp_chars.status_code == 400
    assert "character count" in resp_chars.json()["detail"].lower()

    # Oversized token count (>512 tokens but under 4096 chars)
    many_tokens = "từ " * 550
    assert len(many_tokens) < 4096
    resp_tokens = client.post("/v1/query/embed", json={"question": many_tokens}, headers=headers)
    assert resp_tokens.status_code == 400
    assert "token count" in resp_tokens.json()["detail"].lower()

    # Wrong reader ID in request body
    resp_wrong_reader = client.post(
        "/v1/query/embed",
        json={"question": "valid question", "reader_id": str(uuid.uuid4())},
        headers=headers,
    )
    assert resp_wrong_reader.status_code == 403

    # Unexpected extra fields rejected (extra='forbid')
    resp_extra = client.post(
        "/v1/query/embed",
        json={"question": "valid question", "unexpected_field": True},
        headers=headers,
    )
    assert resp_extra.status_code == 400

    # Duplicate JSON keys rejected
    resp_dup = client.post(
        "/v1/query/embed",
        content='{"question": "one", "question": "two"}',
        headers={**headers, "Content-Type": "application/json"},
    )
    assert resp_dup.status_code == 400

    # Request body exceeding 64 KiB rejected
    oversized_body = '{"question": "' + "a" * 70_000 + '"}'
    resp_oversized = client.post(
        "/v1/query/embed",
        content=oversized_body,
        headers={**headers, "Content-Type": "application/json"},
    )
    assert resp_oversized.status_code == 400

    # Wrong reader ID in request header
    resp_wrong_hdr = client.post(
        "/v1/query/embed",
        json={"question": "valid question"},
        headers={**headers, "X-PPV-Reader-Id": str(uuid.uuid4())},
    )
    assert resp_wrong_hdr.status_code == 403

    # Missing reader ID in both header and body (F10)
    resp_missing_reader = client.post(
        "/v1/query/embed",
        json={"question": "valid question"},
        headers={
            "X-RAG-Service-Key": service_key,
            "X-RAG-Endpoint-Nonce": nonce,
        },
    )
    assert resp_missing_reader.status_code == 403
    assert "Missing mandatory reader ID" in resp_missing_reader.json()["detail"]

    # Private sentinel sanitization: invalid schema must never echo sensitive values (F12)
    private_sentinel = "SYNTHETIC_PRIVATE_SECRET_12345"
    resp_sentinel = client.post(
        "/v1/query/embed",
        content=f'{{"question": ["{private_sentinel}"]}}',
        headers={**headers, "Content-Type": "application/json"},
    )
    assert resp_sentinel.status_code == 400
    assert private_sentinel not in resp_sentinel.text
    assert resp_sentinel.json()["detail"] == "Invalid request schema"

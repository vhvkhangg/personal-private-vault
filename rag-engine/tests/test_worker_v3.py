"""Tests for Phase 18 document batch worker stage execution and error containment."""

import uuid

import pytest
from conftest import HEADERS
from fastapi.testclient import TestClient

import ppv_rag.worker as worker
from ppv_rag.config import Settings
from ppv_rag.contracts import (
    DocumentBatchStageRequest,
    DocumentRootTask,
    PreparedDocumentField,
    document_input_digest,
)
from ppv_rag.main import app
from ppv_rag.worker import process_document_batch_request


def test_process_document_batch_request_mixed_formats(monkeypatch: pytest.MonkeyPatch) -> None:
    job_id = str(uuid.uuid4())
    attempt_id = str(uuid.uuid4())
    lease_token = "0" * 32

    test_settings = Settings(
        service_key="synthetic-test-service-credential-00000000",
        worker_dsn="postgresql://rag_worker@127.0.0.1:5432/vault",
        test_scoped_execution=True,
        job_id=job_id,
        attempt_id=attempt_id,
        endpoint_nonce="44444444-4444-4444-4444-444444444444",
    )
    monkeypatch.setattr(worker, "settings", test_settings)
    monkeypatch.setattr(worker, "verify_document_grant", lambda req: None)
    monkeypatch.setattr(worker, "stage_document_result", lambda **kwargs: None)
    monkeypatch.setattr(worker, "seal_document_result", lambda *args, **kwargs: "0" * 64)

    # Roots: 1 DOMAIN success, 1 MD success, 1 unsupported format failure
    roots = [
        DocumentRootTask(
            root_key="domain-1",
            source_id="domain-1",
            format="DOMAIN",
            prepared_fields=[
                PreparedDocumentField(
                    source_key="domain-1",
                    source_version=1,
                    approved_field="title",
                    text="This is an approved test document about indexing.",
                )
            ],
            approved_fields=["title"],
        ),
        DocumentRootTask(
            root_key="notes.md",
            source_id="file-2",
            format="md",
            content_text="# Title\n\nContent for testing Phase 18 chunking and embedding.",
            approved_fields=["content"],
        ),
        DocumentRootTask(
            root_key="unsupported.bin",
            source_id="file-3",
            format="binary-unknown",
            content_text="data",
            approved_fields=["content"],
        ),
    ]

    input_digest = document_input_digest(roots)

    req = DocumentBatchStageRequest(
        job_id=job_id,
        attempt_id=attempt_id,
        lease_token=lease_token,
        roots=roots,
        input_digest=input_digest,
    )

    resp = process_document_batch_request(req)
    assert resp.status == "STAGED"
    assert resp.job_id == job_id
    assert resp.attempt_id == attempt_id
    assert resp.staged_count == 2
    assert resp.failed_count == 1
    assert resp.peak_system_ram_bytes > 0


def test_process_document_batch_endpoint_containment_disabled() -> None:
    job_id = str(uuid.uuid4())
    attempt_id = str(uuid.uuid4())
    lease_token = "1" * 64

    payload = {
        "contract_version": "3.0",
        "job_id": job_id,
        "attempt_id": attempt_id,
        "lease_token": lease_token,
        "roots": [
            {
                "root_key": "sample.md",
                "source_id": "file-10",
                "format": "md",
                "content_text": "# Sample markdown content for endpoint test.",
                "ocr_languages": ["en"],
                "approved_fields": ["content"],
                "transclusions": {},
            }
        ],
    }

    client = TestClient(app)
    # Default un-bound state must return 503 Service Unavailable
    res = client.post(
        "/v1/worker/execute-batch",
        json=payload,
        headers={"X-RAG-Service-Key": "test-key-must-be-at-least-32-chars-long-00"},
    )
    assert res.status_code == 503
    assert "disabled" in res.text.lower()


def test_process_document_batch_endpoint_bound(
    bound_worker: Settings,
    claimed_unit_input: None,
    monkeypatch: pytest.MonkeyPatch,
) -> None:
    job_id = bound_worker.job_id
    attempt_id = bound_worker.attempt_id
    lease_token = HEADERS["X-RAG-Lease-Token"]

    bound_with_dsn = Settings(
        service_key=bound_worker.service_key,
        worker_dsn="postgresql://rag_worker@127.0.0.1:5432/vault",
        test_scoped_execution=True,
        job_id=job_id,
        attempt_id=attempt_id,
        endpoint_nonce=bound_worker.endpoint_nonce,
    )
    monkeypatch.setattr(worker, "settings", bound_with_dsn)
    monkeypatch.setattr(worker, "verify_document_grant", lambda req: None)
    monkeypatch.setattr(worker, "stage_document_result", lambda **kwargs: None)
    monkeypatch.setattr(worker, "seal_document_result", lambda *args, **kwargs: "0" * 64)

    roots_data = [
        {
            "root_key": "sample.md",
            "source_id": "file-10",
            "format": "md",
            "content_text": "# Sample markdown content for endpoint test.",
            "content_b64": None,
            "ocr_languages": ["en"],
            "approved_fields": ["content"],
            "transclusions": {},
        }
    ]
    input_digest = document_input_digest([DocumentRootTask(**r) for r in roots_data])

    payload = {
        "contract_version": "3.0",
        "job_id": job_id,
        "attempt_id": attempt_id,
        "lease_token": lease_token,
        "roots": roots_data,
        "input_digest": input_digest,
    }

    client = TestClient(app)
    res = client.post(
        "/v1/worker/execute-batch",
        json=payload,
        headers=HEADERS,
    )
    assert res.status_code == 200
    data = res.json()
    assert data["status"] == "STAGED"
    assert data["staged_count"] == 1
    assert data["failed_count"] == 0


def test_process_document_batch_input_digest_mismatch_denied(
    bound_worker: Settings,
    claimed_unit_input: None,
    monkeypatch: pytest.MonkeyPatch,
) -> None:
    job_id = bound_worker.job_id
    attempt_id = bound_worker.attempt_id
    lease_token = HEADERS["X-RAG-Lease-Token"]

    bound_with_dsn = Settings(
        service_key=bound_worker.service_key,
        worker_dsn="postgresql://rag_worker@127.0.0.1:5432/vault",
        test_scoped_execution=True,
        job_id=job_id,
        attempt_id=attempt_id,
        endpoint_nonce=bound_worker.endpoint_nonce,
    )
    monkeypatch.setattr(worker, "settings", bound_with_dsn)

    def forbidden(req: object) -> None:
        raise AssertionError("Authority must not be queried when input digest mismatches")

    monkeypatch.setattr(worker, "verify_document_grant", forbidden)

    payload = {
        "contract_version": "3.0",
        "job_id": job_id,
        "attempt_id": attempt_id,
        "lease_token": lease_token,
        "roots": [
            {
                "root_key": "sample.md",
                "source_id": "file-10",
                "format": "md",
                "content_text": "# Sample markdown content.",
                "ocr_languages": ["en"],
                "approved_fields": ["content"],
                "transclusions": {},
            }
        ],
        "input_digest": "0" * 64,  # Mismatched digest
    }

    client = TestClient(app)
    res = client.post(
        "/v1/worker/execute-batch",
        json=payload,
        headers=HEADERS,
    )
    assert res.status_code == 200
    data = res.json()
    assert data["status"] == "REJECTED"

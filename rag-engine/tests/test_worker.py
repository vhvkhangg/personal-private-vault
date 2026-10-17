import hashlib
import threading
from concurrent.futures import ThreadPoolExecutor

import pytest
from conftest import HEADERS
from fastapi.testclient import TestClient

import ppv_rag.main as runtime
import ppv_rag.transport as transport
import ppv_rag.worker as worker
from ppv_rag.contracts import WorkerStageRequest, WorkerStageResponse


@pytest.fixture(autouse=True)
def state(claimed_unit_input: None) -> None:
    with runtime._lock:
        runtime._active.clear()
        runtime._cleanup.clear()


def data() -> dict[str, str]:
    payload = "Synthetic text"
    return {
        "contract_version": "2.0",
        "job_id": "11111111-1111-1111-1111-111111111111",
        "attempt_id": "22222222-2222-2222-2222-222222222222",
        "lease_token": "a" * 64,
        "stage_type": "SYNTHETIC_TEST",
        "payload": payload,
        "input_digest": hashlib.sha256(payload.encode()).hexdigest(),
        "scope_digest": "b" * 64,
    }


def test_authentication_unknown_version_and_safe_validation_errors() -> None:
    with TestClient(runtime.app) as client:
        assert client.post("/v1/worker/execute", json=data()).status_code == 401
        request = data()
        request["contract_version"] = "99.0"
        request["payload"] = "SENSITIVE_MARKER"
        response = client.post("/v1/worker/execute", json=request, headers=HEADERS)
        assert response.status_code == 422
        assert "SENSITIVE_MARKER" not in response.text
        assert client.get("/metrics").status_code == 401


def test_signed_but_unissued_and_unavailable_authority_never_stage(
    monkeypatch: pytest.MonkeyPatch,
) -> None:
    # Even a correctly shaped token cannot replace an actual issued database grant.
    def denied(*_identity: str) -> None:
        raise RuntimeError("Unissued or unavailable authority")

    monkeypatch.setattr(transport, "claim_input", denied)
    with TestClient(runtime.app) as client:
        response = client.post("/v1/worker/execute", json=data(), headers=HEADERS)
        assert response.status_code == 403
        assert "receipt" not in response.json()


def test_unknown_quiescence_and_cancel_never_certify_process_death() -> None:
    with TestClient(runtime.app) as client:
        job = data()["job_id"]
        assert (
            client.get("/quiescence", params={"job_id": job}, headers=HEADERS).json()["quiescent"]
            is False
        )
        assert (
            client.post("/v1/worker/cancel", json={"job_id": job}, headers=HEADERS).json()["status"]
            == "CANCELLED"
        )
        assert (
            client.get("/quiescence", params={"job_id": job}, headers=HEADERS).json()[
                "cleanup_verified"
            ]
            is False
        )


def test_duplicate_attempt_cannot_remove_an_inflight_execution(
    monkeypatch: pytest.MonkeyPatch,
) -> None:
    entered = threading.Event()
    release = threading.Event()

    def paused(request: WorkerStageRequest) -> WorkerStageResponse:
        entered.set()
        assert release.wait(5)
        return WorkerStageResponse(
            status="REJECTED", job_id=request.job_id, attempt_id=request.attempt_id
        )

    monkeypatch.setattr(runtime, "process_stage_request", paused)
    with TestClient(runtime.app) as client, ThreadPoolExecutor(max_workers=2) as executor:
        first = executor.submit(client.post, "/v1/worker/execute", json=data(), headers=HEADERS)
        try:
            assert entered.wait(5)
            duplicate = client.post("/v1/worker/execute", json=data(), headers=HEADERS)
            assert duplicate.status_code == 429
            job = data()["job_id"]
            assert (
                client.post("/v1/worker/cancel", json={"job_id": job}, headers=HEADERS).status_code
                == 200
            )
            check = client.get("/quiescence", params={"job_id": job}, headers=HEADERS).json()
            assert check["quiescent"] is False
            assert check["active_workers"] == 1
        finally:
            release.set()
        assert first.result(timeout=5).json()["status"] == "CANCELLED"
        assert (
            client.get("/quiescence", params={"job_id": job}, headers=HEADERS).json()["quiescent"]
            is False
        )


def test_input_digest_mismatch_denies_before_authority(monkeypatch: pytest.MonkeyPatch) -> None:
    called = False

    def forbidden(_request: WorkerStageRequest) -> None:
        nonlocal called
        called = True
        raise AssertionError("No authority call should occur for mismatched content")

    monkeypatch.setattr(worker, "verify_grant", forbidden)
    request = data()
    request["input_digest"] = "0" * 64
    assert (
        worker.process_stage_request(WorkerStageRequest.model_validate(request)).status
        == "REJECTED"
    )
    assert called is False

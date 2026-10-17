"""Deterministic ingress units; real PostgreSQL/native proofs are separate gates."""

import asyncio
import os
import threading
from concurrent.futures import ThreadPoolExecutor

import pytest
from conftest import ATTEMPT, HEADERS, JOB, NONCE
from fastapi.testclient import TestClient
from starlette.types import Message, Receive, Scope, Send

import ppv_rag.main as runtime
import ppv_rag.transport as transport
from ppv_rag.config import Settings


def ingress_scope(headers: dict[str, str]) -> Scope:
    return {
        "type": "http",
        "method": "POST",
        "path": "/v1/worker/execute",
        "headers": [(name.lower().encode(), value.encode()) for name, value in headers.items()],
    }


async def unreachable(_scope: Scope, _receive: Receive, _send: Send) -> None:
    raise AssertionError("Denied input must never reach a worker")


@pytest.mark.parametrize(
    "field,value",
    [
        ("X-RAG-Service-Key", "wrong"),
        ("X-RAG-Job-Id", "99999999-9999-9999-9999-999999999999"),
        ("X-RAG-Attempt-Id", "99999999-9999-9999-9999-999999999999"),
        ("X-RAG-Session-Id", "not-a-session"),
        ("X-RAG-Lease-Token", "short"),
        ("X-RAG-Endpoint-Nonce", "wrong"),
    ],
)
def test_wrong_binding_denies_without_any_body_receive(
    bound_worker: Settings, field: str, value: str, monkeypatch: pytest.MonkeyPatch
) -> None:
    sent: list[Message] = []

    async def receive() -> Message:
        raise AssertionError("Unauthorized source content was consumed")

    async def send(message: Message) -> None:
        sent.append(message)

    def forbidden(*_identity: str) -> None:
        raise AssertionError("Wrong local binding must not claim database authority")

    monkeypatch.setattr(transport, "claim_input", forbidden)
    asyncio.run(
        transport.BoundedWorkerRequests(unreachable)(
            ingress_scope({**HEADERS, field: value}), receive, send
        )
    )
    assert sent[0]["status"] in {401, 403}
    assert "SENSITIVE" not in str(sent)


def test_claim_commit_precedes_receive_and_duplicate_never_reads_body(
    bound_worker: Settings, monkeypatch: pytest.MonkeyPatch
) -> None:
    claim_entered = threading.Event()
    allow_commit = threading.Event()
    commit_completed = threading.Event()
    consumed = False
    received = 0
    claim_lock = threading.Lock()
    sent: list[Message] = []

    def claim(*_identity: str) -> None:
        nonlocal consumed
        with claim_lock:
            if consumed:
                raise RuntimeError("Already claimed")
            claim_entered.set()
            assert allow_commit.wait(5)
            consumed = True
            commit_completed.set()

    async def receive() -> Message:
        nonlocal received
        assert commit_completed.is_set(), "Body receive preceded successful claim COMMIT"
        received += 1
        return {"type": "http.request", "body": b"{}", "more_body": False}

    async def send(message: Message) -> None:
        sent.append(message)

    monkeypatch.setattr(transport, "claim_input", claim)
    middleware = transport.BoundedWorkerRequests(unreachable)

    def request() -> None:
        asyncio.run(middleware(ingress_scope(HEADERS), receive, send))

    with ThreadPoolExecutor(max_workers=1) as executor:
        pending = executor.submit(request)
        try:
            assert claim_entered.wait(5)
            assert received == 0
        finally:
            allow_commit.set()
        pending.result(timeout=5)
    assert received == 1
    assert sent[0]["status"] == 422  # Malformed identity burns the claim permanently.
    sent.clear()
    request()
    assert received == 1
    assert sent[0]["status"] == 403


def test_failed_or_uncertain_claim_commit_never_reads_content(
    bound_worker: Settings, monkeypatch: pytest.MonkeyPatch
) -> None:
    def uncertain(*_identity: str) -> None:
        raise RuntimeError("Unknown COMMIT outcome; never retry input")

    async def receive() -> Message:
        raise AssertionError("Unconfirmed claim must not consume content")

    sent: list[Message] = []

    async def send(message: Message) -> None:
        sent.append(message)

    monkeypatch.setattr(transport, "claim_input", uncertain)
    asyncio.run(transport.BoundedWorkerRequests(unreachable)(ingress_scope(HEADERS), receive, send))
    assert sent[0]["status"] == 403
    assert b"COMMIT" not in sent[1]["body"]


def test_duplicate_header_denied_before_claim_or_content(bound_worker: Settings) -> None:
    scope = ingress_scope(HEADERS)
    scope["headers"].append((b"x-rag-job-id", JOB.encode()))
    sent: list[Message] = []

    async def receive() -> Message:
        raise AssertionError("Ambiguous headers must not consume content")

    async def send(message: Message) -> None:
        sent.append(message)

    asyncio.run(transport.BoundedWorkerRequests(unreachable)(scope, receive, send))
    assert sent[0]["status"] == 401


def test_readiness_binds_actual_pid_job_attempt_and_nonce(bound_worker: Settings) -> None:
    with TestClient(runtime.app) as client:
        response = client.get("/v1/worker/readiness", headers=HEADERS)
        assert response.status_code == 200
        body = response.json()
        assert set(body) == {
            "status",
            "contract_version",
            "version",
            "process_id",
            "job_id",
            "attempt_id",
            "endpoint_nonce",
        }
        assert body["contract_version"] == "2.0"
        assert body["process_id"] == os.getpid()
        assert body["job_id"] == JOB
        assert body["attempt_id"] == ATTEMPT
        assert body["endpoint_nonce"] == NONCE
        assert client.get("/v1/worker/readiness").status_code == 401
        assert (
            client.get(
                "/v1/worker/readiness", headers={**HEADERS, "X-RAG-Endpoint-Nonce": "wrong"}
            ).status_code
            == 503
        )


@pytest.mark.parametrize("missing", ["job_id", "attempt_id", "endpoint_nonce"])
def test_incomplete_job_binding_stays_contained(missing: str) -> None:
    values = {"job_id": JOB, "attempt_id": ATTEMPT, "endpoint_nonce": NONCE}
    values[missing] = ""
    assert not Settings(
        service_key=HEADERS["X-RAG-Service-Key"], test_scoped_execution=True, **values
    ).bound_execution_enabled()

"""Explicit isolated binding for unit ingress tests; never runtime authority proof."""

import pytest

import ppv_rag.main as runtime
import ppv_rag.security as security
import ppv_rag.transport as transport
from ppv_rag.config import Settings

KEY = "synthetic-test-service-credential-00000000"
JOB = "11111111-1111-1111-1111-111111111111"
ATTEMPT = "22222222-2222-2222-2222-222222222222"
SESSION = "33333333-3333-3333-3333-333333333333"
NONCE = "44444444-4444-4444-4444-444444444444"
HEADERS = {
    "X-RAG-Service-Key": KEY,
    "X-RAG-Job-Id": JOB,
    "X-RAG-Attempt-Id": ATTEMPT,
    "X-RAG-Lease-Token": "a" * 64,
    "X-RAG-Session-Id": SESSION,
    "X-RAG-Endpoint-Nonce": NONCE,
}


@pytest.fixture
def bound_worker(monkeypatch: pytest.MonkeyPatch) -> Settings:
    bound = Settings(
        service_key=KEY,
        test_scoped_execution=True,
        job_id=JOB,
        attempt_id=ATTEMPT,
        endpoint_nonce=NONCE,
    )
    for module in (runtime, security, transport):
        monkeypatch.setattr(module, "settings", bound)
    return bound


@pytest.fixture
def claimed_unit_input(bound_worker: Settings, monkeypatch: pytest.MonkeyPatch) -> None:
    # This fixture permits isolated parser/concurrency units. Restricted-role
    # claim/COMMIT/termination proof belongs to the actual integration boundary.
    monkeypatch.setattr(transport, "claim_input", lambda *_identity: None)

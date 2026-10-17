import hashlib

import pytest
from pydantic import ValidationError

from ppv_rag.contracts import WorkerStageRequest


def request_data() -> dict[str, str]:
    return {
        "contract_version": "2.0",
        "job_id": "11111111-1111-1111-1111-111111111111",
        "attempt_id": "22222222-2222-2222-2222-222222222222",
        "lease_token": "a" * 64,
        "stage_type": "SYNTHETIC_TEST",
        "payload": "fixture data",
        "input_digest": hashlib.sha256(b"fixture data").hexdigest(),
        "scope_digest": "b" * 64,
    }


def test_valid_stage_request_and_redacted_repr() -> None:
    request = WorkerStageRequest.model_validate(request_data())
    assert request.contract_version == "2.0"
    assert "fixture data" not in repr(request)
    assert "a" * 64 not in repr(request)


@pytest.mark.parametrize(
    "field,value",
    [
        ("contract_version", "1.0"),
        ("contract_version", "99.0"),
        ("job_id", "not-a-uuid"),
        ("attempt_id", "AAAAAAAA-AAAA-AAAA-AAAA-AAAAAAAAAAAA"),
        ("lease_token", ""),
        ("stage_type", "CHUNKS"),
        ("stage_type", "UNKNOWN"),
        ("input_digest", "x"),
        ("scope_digest", "g" * 64),
        ("payload", ""),
        ("payload", "あ" * 400000),
        ("payload", "x" * 1048577),
    ],
    ids=lambda value: str(value)[:24],
)
def test_malformed_scope_and_multibyte_budgets(field: str, value: str) -> None:
    data = request_data()
    data[field] = value
    with pytest.raises(ValidationError):
        WorkerStageRequest.model_validate(data)


def test_missing_and_extra_fields_rejected() -> None:
    with pytest.raises(ValidationError):
        WorkerStageRequest.model_validate({"contract_version": "2.0"})
    data = request_data()
    data["arbitrary_path"] = "private"
    with pytest.raises(ValidationError):
        WorkerStageRequest.model_validate(data)

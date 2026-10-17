import json

import pytest
from conftest import HEADERS
from fastapi.testclient import TestClient

from ppv_rag.main import app
from ppv_rag.transport import unique_object


def test_duplicate_properties_and_unknown_encoding_deny_without_payload_echo(
    claimed_unit_input: None,
) -> None:
    with TestClient(app) as client:
        response = client.post(
            "/v1/worker/execute",
            content=b'{"payload":"SENSITIVE_MARKER","payload":"other"}',
            headers={**HEADERS, "Content-Type": "application/json"},
        )
        assert response.status_code == 422
        assert "SENSITIVE_MARKER" not in response.text
        assert (
            client.post("/v1/worker/execute", content=b"\xff\xfe", headers=HEADERS).status_code
            == 422
        )


def test_raw_stream_byte_limit_precedes_deserialization(claimed_unit_input: None) -> None:
    with TestClient(app) as client:
        response = client.post("/v1/worker/execute", content=b"x" * 2105345, headers=HEADERS)
        assert response.status_code == 413
        assert len(response.content) < 100
        assert (
            client.post("/v1/worker/cancel", content=b"x" * 2049, headers=HEADERS).status_code
            == 413
        )


def test_unique_json_object_rejects_duplicate_authority_fields() -> None:
    with pytest.raises(ValueError):
        json.loads('{"scope":"a","scope":"b"}', object_pairs_hook=unique_object)

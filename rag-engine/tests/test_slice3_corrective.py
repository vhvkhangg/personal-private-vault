"""Snapshot negotiation and trusted-source replay boundary regressions."""

import hashlib
import json
from copy import deepcopy

import pytest
from pydantic import ValidationError

from ppv_rag.contracts import (
    DocumentBatchStageRequest,
    DocumentRootTask,
    document_input_digest,
)

SOURCE = "11111111-1111-1111-1111-111111111111"
REVISION = "22222222-2222-2222-2222-222222222222"


def snapshot_root() -> dict:
    text = "# Tiếng Việt 中文\n\nExact source.\n"
    return dict(
        root_key="root",
        source_id=SOURCE,
        source_version=2,
        format="MD",
        content_text=text,
        approved_fields=["content"],
        snapshot=dict(
            mode="RAG_SNAPSHOT",
            consent_mode="WHOLE_FILE",
            source_type="EXTERNAL_FILE",
            source_id=SOURCE,
            revision_id=REVISION,
            sha256=hashlib.sha256(text.encode()).hexdigest(),
            byte_length=len(text.encode()),
            source_name="Source.md",
            expected_selected_revision_id=None,
            source_version=2,
            consent_reset_version=1,
            authorization_epoch=3,
            field_policy_version=1,
        ),
    )


@pytest.mark.parametrize(
    "field,value",
    [
        ("mode", "LEGACY"),
        ("consent_mode", "FIELDS"),
        ("source_id", REVISION),
        ("source_version", 3),
        ("byte_length", 0),
        ("source_name", "Source.markdown"),
        ("source_name", "../Source.md"),
        ("sha256", "0" * 64),
        ("byte_length", 0.5),
    ],
)
def test_snapshot_substitution_rejected(field: str, value: object) -> None:
    root = snapshot_root()
    root["snapshot"][field] = value
    with pytest.raises(ValidationError):
        DocumentRootTask.model_validate(root)


def test_exact_revision_and_unknown_fields_are_strict() -> None:
    root = snapshot_root()
    DocumentRootTask.model_validate(root)
    bad = deepcopy(root)
    bad["snapshot"]["revision_id"] = "not-a-revision"
    with pytest.raises(ValidationError):
        DocumentRootTask.model_validate(bad)
    root["snapshot"]["unauthorized"] = True
    with pytest.raises(ValidationError):
        DocumentRootTask.model_validate(root)


def test_external_batch_cannot_omit_snapshot_mode() -> None:
    root = snapshot_root()
    root.pop("snapshot")
    with pytest.raises(ValidationError, match="explicit snapshot"):
        DocumentBatchStageRequest(
            job_id=SOURCE,
            attempt_id=REVISION,
            lease_token="a" * 64,
            roots=[DocumentRootTask.model_validate(root)],
        )


def test_legacy_domain_digest_remains_unchanged() -> None:
    root = DocumentRootTask(root_key="domain", source_id="42", format="DOMAIN")
    old = root.model_dump()
    old.pop("snapshot")
    expected = hashlib.sha256(
        json.dumps(
            [old], sort_keys=True, ensure_ascii=False, separators=(",", ":"), allow_nan=False
        ).encode()
    ).hexdigest()
    assert document_input_digest([root]) == expected


def test_child_payload_requires_its_own_exact_snapshot() -> None:
    root = snapshot_root()
    root["transclusions"] = {
        "Child": dict(
            source_key=REVISION, source_version=2, approved_field="content", text="unapproved"
        )
    }
    with pytest.raises(ValidationError, match="child snapshot"):
        DocumentRootTask.model_validate(root)

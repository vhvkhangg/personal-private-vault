"""Data contracts for RAG engine REST endpoints."""

from __future__ import annotations

import hashlib
import json
import re
from typing import Literal

from pydantic import BaseModel, ConfigDict, Field, StrictInt, field_validator, model_validator

UUID_PATTERN = r"^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$"
DIGEST_PATTERN = r"^[0-9a-f]{64}$"


class Envelope(BaseModel):
    model_config = ConfigDict(extra="forbid", frozen=True)


class HealthResponse(Envelope):
    status: str
    version: str


class MetricsResponse(Envelope):
    stages_executed_total: int
    stages_cancelled_total: int
    active_cancelled_jobs: int = 0
    active_workers: int = 0


class WorkerReadinessResponse(Envelope):
    status: Literal["READY"]
    job_id: str
    attempt_id: str
    process_id: int
    worker_identity: str
    protocol_version: Literal["2.0", "3.0"] = "2.0"


class WorkerReceipt(Envelope):
    contract_version: Literal["2.0"] = "2.0"
    status: Literal["STAGED"] = "STAGED"
    job_id: str = Field(pattern=UUID_PATTERN)
    attempt_id: str = Field(pattern=UUID_PATTERN)
    stage_type: Literal["SYNTHETIC_STAGE", "SYNTHETIC_TEST"]
    input_digest: str = Field(pattern=DIGEST_PATTERN)
    scope_digest: str = Field(pattern=DIGEST_PATTERN)
    output_digest: str = Field(pattern=DIGEST_PATTERN)
    output_kind: Literal["SYNTHETIC_DIGEST"] = "SYNTHETIC_DIGEST"


class WorkerStageRequest(Envelope):
    contract_version: Literal["2.0"]
    job_id: str = Field(pattern=UUID_PATTERN)
    attempt_id: str = Field(pattern=UUID_PATTERN)
    lease_token: str = Field(min_length=64, max_length=128, repr=False)
    stage_type: Literal["SYNTHETIC_STAGE", "SYNTHETIC_TEST"]
    payload: str = Field(min_length=1, repr=False)
    input_digest: str = Field(pattern=DIGEST_PATTERN)
    scope_digest: str = Field(pattern=DIGEST_PATTERN)

    @field_validator("payload")
    @classmethod
    def bounded_payload(cls, value: str) -> str:
        if len(value.encode("utf-8")) > 1048576:
            raise ValueError("Input byte budget exceeded")
        return value


class WorkerStageResponse(Envelope):
    contract_version: Literal["2.0"] = "2.0"
    status: Literal["STAGED", "REJECTED", "CANCELLED", "BUSY"]
    job_id: str
    attempt_id: str
    receipt: WorkerReceipt | None = None
    staged_row_id: int | None = None


class WorkerCancelRequest(Envelope):
    job_id: str = Field(pattern=UUID_PATTERN)


class WorkerCancelResponse(Envelope):
    status: Literal["CANCELLED"]
    job_id: str


class SnapshotBinding(Envelope):
    mode: Literal["RAG_SNAPSHOT"]
    consent_mode: Literal["WHOLE_FILE"]
    source_type: Literal["EXTERNAL_FILE"]
    source_id: str = Field(pattern=UUID_PATTERN)
    revision_id: str = Field(pattern=UUID_PATTERN)
    sha256: str = Field(pattern=DIGEST_PATTERN)
    byte_length: StrictInt = Field(ge=0, le=10 * 1024 * 1024)
    source_name: str = Field(min_length=4, max_length=255)
    target_digests: list[str] = Field(default_factory=list, max_length=512)
    expected_selected_revision_id: str | None = Field(pattern=UUID_PATTERN)
    source_version: StrictInt = Field(ge=1)
    consent_reset_version: StrictInt = Field(ge=1)
    authorization_epoch: StrictInt = Field(ge=1)
    field_policy_version: StrictInt = Field(ge=1)

    @field_validator("source_name")
    @classmethod
    def markdown_name(cls, value: str) -> str:
        if not value.lower().endswith(".md") or "/" in value or "\\" in value:
            raise ValueError("Snapshot source format denied")
        return value

    @field_validator("target_digests")
    @classmethod
    def captured_targets(cls, value: list[str]) -> list[str]:
        if value != sorted(set(value)) or any(
            re.fullmatch(DIGEST_PATTERN, v) is None for v in value
        ):
            raise ValueError("Invalid captured target binding")
        return value

    def verify_text(self, source: str, version: int, text: str) -> None:
        data = text.encode("utf-8", errors="strict")
        if (
            source != self.source_id
            or version != self.source_version
            or len(data) != self.byte_length
            or hashlib.sha256(data).hexdigest() != self.sha256
        ):
            raise ValueError("Snapshot binding mismatch")


class PreparedDocumentField(Envelope):
    source_key: str
    source_version: int = Field(ge=1)
    approved_field: str
    text: str = Field(repr=False)
    snapshot: SnapshotBinding | None = None


class DocumentRootTask(Envelope):
    root_key: str
    source_id: str
    source_version: int = Field(default=1, ge=1)
    format: str
    content_text: str | None = None
    content_b64: str | None = None
    ocr_languages: list[str] = Field(default_factory=lambda: ["en"])
    approved_fields: list[str] = Field(default_factory=list)
    transclusions: dict[str, PreparedDocumentField] = Field(default_factory=dict)
    prepared_fields: list[PreparedDocumentField] = Field(default_factory=list)
    snapshot: SnapshotBinding | None = None

    @model_validator(mode="after")
    def snapshot_scope(self) -> DocumentRootTask:
        if self.snapshot is not None:
            if (
                self.format != "MD"
                or self.content_b64 is not None
                or self.content_text is None
                or self.prepared_fields
            ):
                raise ValueError("Snapshot mode requires exact Markdown input")
            self.snapshot.verify_text(self.source_id, self.source_version, self.content_text)
            for child in self.transclusions.values():
                if child.snapshot is None or child.approved_field != "content":
                    raise ValueError("Missing child snapshot authority")
                child.snapshot.verify_text(child.source_key, child.source_version, child.text)
        return self


def document_root_wire(root: DocumentRootTask) -> dict[str, object]:
    """Additive negotiation: preserve legacy v3/DOMAIN digest bytes when absent."""
    value = root.model_dump()
    if root.snapshot is None:
        value.pop("snapshot")
    elif not root.snapshot.target_digests:
        value["snapshot"].pop("target_digests")
    for field in [*value["transclusions"].values(), *value["prepared_fields"]]:
        if field.get("snapshot") is None:
            field.pop("snapshot")
        elif not field["snapshot"]["target_digests"]:
            field["snapshot"].pop("target_digests")
    return value


def document_input_digest(roots: list[DocumentRootTask]) -> str:
    """v3: all fields/defaults, recursive sorted keys, compact UTF-8 JSON, no ASCII escaping."""
    encoded = json.dumps(
        [document_root_wire(root) for root in roots],
        sort_keys=True,
        ensure_ascii=False,
        separators=(",", ":"),
        allow_nan=False,
    ).encode("utf-8")
    return hashlib.sha256(encoded).hexdigest()


class DocumentBatchStageRequest(Envelope):
    contract_version: Literal["3.0"] = "3.0"
    job_id: str = Field(pattern=UUID_PATTERN)
    attempt_id: str = Field(pattern=UUID_PATTERN)
    lease_token: str = Field(min_length=32, max_length=128, repr=False)
    roots: list[DocumentRootTask] = Field(min_length=1, max_length=100)
    input_digest: str = Field(min_length=64, max_length=64, default="0" * 64)
    scope_digest: str = Field(min_length=64, max_length=64, default="0" * 64)

    @model_validator(mode="after")
    def external_snapshot_negotiation(self) -> DocumentBatchStageRequest:
        import re

        for root in self.roots:
            if (
                root.format != "DOMAIN"
                and re.fullmatch(UUID_PATTERN, root.source_id)
                and root.snapshot is None
            ):
                raise ValueError("External source requires explicit snapshot mode")
        return self


class DocumentBatchStageResponse(Envelope):
    contract_version: Literal["3.0"] = "3.0"
    status: Literal["STAGED", "REJECTED", "CANCELLED", "BUSY"]
    job_id: str
    attempt_id: str
    staged_count: int = 0
    published_count: int = 0
    failed_count: int = 0
    peak_system_ram_bytes: int = 0
    peak_gpu_vram_bytes: int | None = None
    receipt: dict[str, str] = Field(default_factory=dict)

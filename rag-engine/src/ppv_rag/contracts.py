"""Data contracts for RAG engine REST endpoints."""

from __future__ import annotations

import hashlib
import json
from typing import Literal

from pydantic import BaseModel, ConfigDict, Field, field_validator

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


class PreparedDocumentField(Envelope):
    source_key: str
    source_version: int = Field(ge=1)
    approved_field: str
    text: str = Field(repr=False)


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


def document_input_digest(roots: list[DocumentRootTask]) -> str:
    """v3: all fields/defaults, recursive sorted keys, compact UTF-8 JSON, no ASCII escaping."""
    encoded = json.dumps(
        [root.model_dump() for root in roots],
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

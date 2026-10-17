"""Strict versioned envelopes for the issued synthetic foundation stage."""

from typing import Literal

from pydantic import BaseModel, ConfigDict, Field, field_validator

UUID_PATTERN = r"^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$"
DIGEST_PATTERN = r"^[0-9a-f]{64}$"


class Envelope(BaseModel):
    model_config = ConfigDict(extra="forbid")


class HealthResponse(Envelope):
    status: str
    version: str


class MetricsResponse(Envelope):
    stages_executed_total: int
    stages_cancelled_total: int
    active_cancelled_jobs: int = 0
    active_workers: int = 0


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

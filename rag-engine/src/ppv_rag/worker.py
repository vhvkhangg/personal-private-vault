"""Digest-only controlled synthetic work; no parser/model/temp-file capability."""

import hashlib
import hmac

from ppv_rag.contracts import WorkerReceipt, WorkerStageRequest, WorkerStageResponse
from ppv_rag.db import stage_receipt, verify_grant


def process_stage_request(request: WorkerStageRequest) -> WorkerStageResponse:
    try:
        digest = hashlib.sha256(request.payload.encode("utf-8")).hexdigest()
        if not hmac.compare_digest(digest, request.input_digest):
            raise ValueError("Input digest mismatch")
        verify_grant(request)
        output = hashlib.sha256(
            f"{request.stage_type}:{request.payload}".encode("utf-8")
        ).hexdigest()
        receipt = WorkerReceipt(
            job_id=request.job_id,
            attempt_id=request.attempt_id,
            stage_type=request.stage_type,
            input_digest=digest,
            scope_digest=request.scope_digest,
            output_digest=output,
        )
        staged_id = stage_receipt(request, receipt)
        return WorkerStageResponse(
            status="STAGED",
            job_id=request.job_id,
            attempt_id=request.attempt_id,
            receipt=receipt,
            staged_row_id=staged_id,
        )
    except Exception:
        # Never expose SQL, credentials, input, file paths or worker exception messages.
        return WorkerStageResponse(
            status="REJECTED", job_id=request.job_id, attempt_id=request.attempt_id
        )

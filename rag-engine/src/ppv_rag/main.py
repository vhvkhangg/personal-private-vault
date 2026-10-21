"""Bounded job-scoped local control; Java independently verifies process death."""

import os
import threading
from collections import OrderedDict

from fastapi import Depends, FastAPI, Header, HTTPException
from fastapi.exceptions import RequestValidationError
from fastapi.responses import JSONResponse

from ppv_rag import __version__
from ppv_rag.config import settings
from ppv_rag.contracts import (
    DocumentBatchStageRequest,
    DocumentBatchStageResponse,
    HealthResponse,
    MetricsResponse,
    WorkerCancelRequest,
    WorkerCancelResponse,
    WorkerStageRequest,
    WorkerStageResponse,
)
from ppv_rag.security import verify_service_key
from ppv_rag.transport import BoundedWorkerRequests
from ppv_rag.worker import process_document_batch_request, process_stage_request

app = FastAPI(title="PPV RAG Foundation", version=__version__)
app.add_middleware(BoundedWorkerRequests)
_lock = threading.Lock()
_active: dict[tuple[str, str], bool] = {}
_cleanup: OrderedDict[str, bool] = OrderedDict()
_stages_executed = 0
_stages_cancelled = 0


@app.exception_handler(RequestValidationError)
async def validation_error(_request: object, _exception: RequestValidationError) -> JSONResponse:
    return JSONResponse(status_code=422, content={"detail": "Invalid worker contract"})


@app.get("/health", response_model=HealthResponse)
def health() -> HealthResponse:
    return HealthResponse(status="UP", version=__version__)


@app.get("/v1/worker/readiness", dependencies=[Depends(verify_service_key)])
def readiness(x_rag_endpoint_nonce: str | None = Header(default=None)) -> dict[str, str | int]:
    import hmac

    if (
        not settings.bound_execution_enabled()
        or not x_rag_endpoint_nonce
        or not hmac.compare_digest(x_rag_endpoint_nonce, settings.endpoint_nonce)
    ):
        raise HTTPException(status_code=503, detail="Worker endpoint binding denied")
    return {
        "status": "READY",
        "contract_version": "2.0",
        "version": __version__,
        "process_id": os.getpid(),
        "job_id": settings.job_id,
        "attempt_id": settings.attempt_id,
        "endpoint_nonce": settings.endpoint_nonce,
    }


@app.get("/metrics", response_model=MetricsResponse, dependencies=[Depends(verify_service_key)])
def metrics() -> MetricsResponse:
    with _lock:
        return MetricsResponse(
            stages_executed_total=_stages_executed,
            stages_cancelled_total=_stages_cancelled,
            active_workers=len(_active),
        )


@app.get("/quiescence", dependencies=[Depends(verify_service_key)])
def quiescence(job_id: str) -> dict[str, bool | int | str]:
    with _lock:
        active = sum(1 for job, _attempt in _active if job == job_id)
        return {
            "job_id": job_id,
            # A live HTTP server cannot prove its own process-tree termination or
            # PostgreSQL writer drain. Only Java's native supervisor/finalizer can.
            "quiescent": False,
            "cleanup_verified": False,
            "active_workers": active,
        }


@app.post(
    "/v1/worker/execute",
    response_model=WorkerStageResponse,
    dependencies=[Depends(verify_service_key)],
)
def execute_stage(request: WorkerStageRequest) -> WorkerStageResponse:
    global _stages_executed, _stages_cancelled
    identity = (request.job_id, request.attempt_id)
    with _lock:
        if _active:
            return WorkerStageResponse(
                status="BUSY", job_id=request.job_id, attempt_id=request.attempt_id
            )
        _active[identity] = False
        _stages_executed += 1
        _cleanup.pop(request.job_id, None)
    try:
        response = process_stage_request(request)
        with _lock:
            if _active[identity]:
                _stages_cancelled += 1
                return WorkerStageResponse(
                    status="CANCELLED", job_id=request.job_id, attempt_id=request.attempt_id
                )
        return response
    finally:
        with _lock:
            was_cancelled = _active.pop(identity, False)
            if was_cancelled:
                _cleanup[request.job_id] = True
                while len(_cleanup) > 256:
                    _cleanup.popitem(last=False)


@app.post(
    "/v1/worker/execute-batch",
    response_model=DocumentBatchStageResponse,
    dependencies=[Depends(verify_service_key)],
)
def execute_document_batch(
    request: DocumentBatchStageRequest,
) -> DocumentBatchStageResponse:
    global _stages_executed, _stages_cancelled
    identity = (request.job_id, request.attempt_id)
    with _lock:
        if _active:
            return DocumentBatchStageResponse(
                status="BUSY",
                job_id=request.job_id,
                attempt_id=request.attempt_id,
            )
        _active[identity] = False
        _stages_executed += 1
        _cleanup.pop(request.job_id, None)
    try:

        def cancelled() -> bool:
            with _lock:
                return _active.get(identity, True)

        response = process_document_batch_request(request, cancelled=cancelled)
        with _lock:
            if _active[identity]:
                _stages_cancelled += 1
                return DocumentBatchStageResponse(
                    status="CANCELLED",
                    job_id=request.job_id,
                    attempt_id=request.attempt_id,
                )
        return response
    finally:
        with _lock:
            was_cancelled = _active.pop(identity, False)
            if was_cancelled:
                _cleanup[request.job_id] = True
                while len(_cleanup) > 256:
                    _cleanup.popitem(last=False)


@app.post(
    "/v1/worker/cancel",
    response_model=WorkerCancelResponse,
    dependencies=[Depends(verify_service_key)],
)
def cancel_job(request: WorkerCancelRequest) -> WorkerCancelResponse:
    with _lock:
        for identity in _active:
            if identity[0] == request.job_id:
                _active[identity] = True
        _cleanup[request.job_id] = not any(job == request.job_id for job, _attempt in _active)
        while len(_cleanup) > 256:
            _cleanup.popitem(last=False)
    return WorkerCancelResponse(status="CANCELLED", job_id=request.job_id)

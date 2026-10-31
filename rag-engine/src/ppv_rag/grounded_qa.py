"""Request-bound loopback QA service for PPV RAG.

P19-S19-2 — SUSPENDED / NOT_ACCEPTED / ARCHIVAL_ONLY; do not enable or deploy.
Authority: docs/implementation/phase-19/owner-decision-s19-2-source-archival-only-2026-10-31.md.
The archived Java QA supervisor is absent from ordinary application startup.

Hosts pinned query embedding and local CPU candidate LLM generation under an owned
process with Job Object memory ceiling, authenticated headers, strict JSON with duplicate key
detection, and structured candidate output.
"""

from __future__ import annotations

import gc
import hashlib
import json
import logging
import os
from typing import Annotated, Any

from fastapi import Depends, FastAPI, Header, HTTPException, Request, Response, status
from pydantic import BaseModel, ConfigDict

from ppv_rag.embeddings.model import (
    DIMENSION,
    MODEL_ARTIFACT_SHA256,
    MODEL_VERSION,
    TOKENIZER_ARTIFACT_SHA256,
    TOKENIZER_VERSION,
    MultilingualEmbeddingPipeline,
)
from ppv_rag.qa_generation import (
    GENERATION_PROFILE,
    generate_grounded_answer,
    profile_hash,
)
from ppv_rag.qa_generation import (
    MODEL_SHA256 as QA_MODEL_SHA256,
)
from ppv_rag.qa_generation import (
    MODEL_VERSION as QA_MODEL_VERSION,
)

PROMPT_PROFILE_HASH = profile_hash()

logger = logging.getLogger(__name__)

_pipeline: MultilingualEmbeddingPipeline | None = None


def get_pipeline() -> MultilingualEmbeddingPipeline:
    global _pipeline
    if _pipeline is None:
        _pipeline = MultilingualEmbeddingPipeline(force_cpu=True)
    return _pipeline


_READER_ID: str | None = None
_NONCE: str | None = None
_SERVICE_KEY: str | None = None
_GENERATION_CONSUMED: bool = False


def init_service(
    reader_id: str,
    endpoint_nonce: str = "",
    service_key: str = "",
    nonce: str = "",
) -> None:
    global _READER_ID, _NONCE, _SERVICE_KEY, _GENERATION_CONSUMED
    _READER_ID = reader_id
    _NONCE = endpoint_nonce or nonce
    _SERVICE_KEY = service_key
    _GENERATION_CONSUMED = False


def _get_reader_id() -> str:
    return _READER_ID or os.environ.get("PPV_RAG_READER_ID", "")


def _get_nonce() -> str:
    return _NONCE or os.environ.get("PPV_RAG_NONCE", "")


def _get_service_key() -> str:
    return _SERVICE_KEY or os.environ.get("PPV_RAG_SERVICE_KEY", "")


class QueryReadinessResponse(BaseModel):
    model_config = ConfigDict(extra="forbid")

    status: str = "READY"
    contract_version: str = "1.0"
    reader_id: str
    process_id: int
    endpoint_nonce: str
    nonce: str | None = None
    model_version: str = MODEL_VERSION
    artifact_hash: str = MODEL_ARTIFACT_SHA256
    tokenizer_version: str = TOKENIZER_VERSION
    tokenizer_artifact_hash: str = TOKENIZER_ARTIFACT_SHA256
    dimensions: int = DIMENSION


class QueryEmbedRequest(BaseModel):
    model_config = ConfigDict(extra="forbid")

    question: str
    reader_id: str | None = None


class QueryEmbedResponse(BaseModel):
    model_config = ConfigDict(extra="forbid")

    reader_id: str
    contract_version: str = "1.0"
    process_id: int
    endpoint_nonce: str
    vector: list[float]
    embedding: list[float] | None = None
    dimensions: int = DIMENSION
    model_version: str = MODEL_VERSION
    artifact_hash: str = MODEL_ARTIFACT_SHA256
    tokenizer_version: str = TOKENIZER_VERSION
    tokenizer_artifact_hash: str = TOKENIZER_ARTIFACT_SHA256


class HistoryEntry(BaseModel):
    model_config = ConfigDict(extra="forbid")

    role: str
    content: str


class EvidenceEntry(BaseModel):
    model_config = ConfigDict(extra="forbid")

    citation_key: str
    excerpt: str
    source_name: str


class QaGenerateRequest(BaseModel):
    model_config = ConfigDict(extra="forbid")

    reader_id: str
    nonce: str
    question: str
    evidence: list[EvidenceEntry]
    history: list[HistoryEntry] | None = None
    response_language: str | None = None
    scope_digest: str


class ClaimSupportItem(BaseModel):
    model_config = ConfigDict(extra="forbid")

    citation_key: str
    quote: str


class ClaimItem(BaseModel):
    model_config = ConfigDict(extra="forbid")

    id: str
    text: str
    supports: list[ClaimSupportItem]


class ConflictPairItem(BaseModel):
    model_config = ConfigDict(extra="forbid")

    claim_id_a: str
    claim_id_b: str
    description: str


class QaGenerateResponse(BaseModel):
    model_config = ConfigDict(extra="forbid")

    contract_version: str = "1.0"
    reader_id: str
    process_id: int
    endpoint_nonce: str
    scope_digest: str
    profile: str = GENERATION_PROFILE
    prompt_profile_hash: str = PROMPT_PROFILE_HASH
    outcome: str
    claims: list[ClaimItem]
    unanswered_parts: list[str]
    conflict_pairs: list[ConflictPairItem]
    model_version: str
    artifact_hash: str


def verify_credentials(
    x_rag_service_key: Annotated[str | None, Header()] = None,
    x_ppv_service_key: Annotated[str | None, Header()] = None,
    x_rag_endpoint_nonce: Annotated[str | None, Header()] = None,
    x_ppv_nonce: Annotated[str | None, Header()] = None,
    x_rag_reader_id: Annotated[str | None, Header()] = None,
    x_ppv_reader_id: Annotated[str | None, Header()] = None,
) -> str | None:
    expected_key = _get_service_key()
    service_key = x_rag_service_key or x_ppv_service_key
    if not expected_key or not service_key or service_key != expected_key:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid or missing service key",
        )
    expected_nonce = _get_nonce()
    nonce = x_rag_endpoint_nonce or x_ppv_nonce
    if not expected_nonce or not nonce or nonce != expected_nonce:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Invalid or missing endpoint nonce",
        )
    expected_reader = _get_reader_id()
    if expected_reader:
        if x_rag_reader_id and x_rag_reader_id != expected_reader:
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail="Mismatched reader ID in request header",
            )
        if x_ppv_reader_id and x_ppv_reader_id != expected_reader:
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail="Mismatched reader ID in request header",
            )
    return x_rag_reader_id or x_ppv_reader_id


app = FastAPI(title="PPV RAG Grounded QA Service", docs_url=None, redoc_url=None)


def _strict_parse_json(raw_body: bytes) -> dict[str, Any]:
    if len(raw_body) > 65_536:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Request body exceeds 64 KiB limit",
        )
    try:

        def _pairs_hook(pairs: list[tuple[str, Any]]) -> dict[str, Any]:
            res: dict[str, Any] = {}
            for k, v in pairs:
                if k in res:
                    raise ValueError("Duplicate key detected")
                res[k] = v
            return res

        parsed = json.loads(raw_body.decode("utf-8"), object_pairs_hook=_pairs_hook)
        if not isinstance(parsed, dict):
            raise ValueError("Request body must be a JSON object")
        return parsed
    except Exception as e:
        if "Duplicate key" in str(e):
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Duplicate key in JSON body",
            ) from None
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Malformed JSON syntax",
        ) from None


@app.get("/v1/query/readiness", response_model=QueryReadinessResponse)
@app.get("/v1/query/ready", response_model=QueryReadinessResponse)
def readiness(
    _auth: Annotated[str | None, Depends(verify_credentials)],
) -> QueryReadinessResponse:
    pipeline = get_pipeline()
    nonce = _get_nonce()
    return QueryReadinessResponse(
        status="READY",
        contract_version="1.0",
        reader_id=_get_reader_id(),
        process_id=os.getpid(),
        endpoint_nonce=nonce,
        nonce=nonce,
        model_version=pipeline.MODEL_VERSION,
        artifact_hash=pipeline.artifact_hash,
        tokenizer_version=pipeline.TOKENIZER_VERSION,
        tokenizer_artifact_hash=TOKENIZER_ARTIFACT_SHA256,
        dimensions=pipeline.DIMENSION,
    )


@app.post("/v1/query/embed", response_model=QueryEmbedResponse)
async def embed_query(
    request: Request,
    reader_hdr: Annotated[str | None, Depends(verify_credentials)],
) -> QueryEmbedResponse:
    raw_body_buffer = bytearray()
    async for chunk in request.stream():
        if len(raw_body_buffer) + len(chunk) > 65_536:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Query payload exceeds the protocol bound",
            )
        raw_body_buffer.extend(chunk)
    raw_body = bytes(raw_body_buffer)
    parsed_dict = _strict_parse_json(raw_body)
    try:
        body = QueryEmbedRequest.model_validate(parsed_dict)
    except Exception:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Invalid request schema",
        ) from None

    expected_reader = _get_reader_id()
    reader_id = body.reader_id or reader_hdr
    if not reader_id:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Missing mandatory reader ID in request",
        )
    if expected_reader and reader_id != expected_reader:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Mismatched reader ID in request body",
        )
    if body.reader_id and reader_hdr and body.reader_id != reader_hdr:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Mismatched reader ID between header and body",
        )

    raw_question = body.question.strip()
    if not raw_question:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Question cannot be empty or whitespace only",
        )
    if len(raw_question) > 4096:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Question exceeds maximum allowed character count (4096)",
        )

    pipeline = get_pipeline()
    tokens = pipeline.token_count(raw_question)
    if tokens > 512:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Question exceeds maximum allowed token count (512)",
        )

    try:
        vector = pipeline.embed_text(raw_question)
    except Exception as e:
        logger.error("Embedding generation failed: %s", type(e).__name__)
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Embedding generation failure",
        ) from None

    if len(vector) != DIMENSION:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Unexpected embedding dimensions",
        )

    return QueryEmbedResponse(
        reader_id=reader_id,
        process_id=os.getpid(),
        endpoint_nonce=_get_nonce(),
        vector=vector,
        embedding=vector,
        dimensions=pipeline.DIMENSION,
        model_version=pipeline.MODEL_VERSION,
        artifact_hash=pipeline.artifact_hash,
        tokenizer_version=pipeline.TOKENIZER_VERSION,
        tokenizer_artifact_hash=TOKENIZER_ARTIFACT_SHA256,
    )


@app.post("/v1/qa/generate", response_model=QaGenerateResponse)
async def generate_qa(
    request: Request,
    response: Response,
    reader_hdr: Annotated[str | None, Depends(verify_credentials)],
) -> QaGenerateResponse:
    raw_body_buffer = bytearray()
    async for chunk in request.stream():
        if len(raw_body_buffer) + len(chunk) > 65_536:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="QA generate payload exceeds 64 KiB limit",
            )
        raw_body_buffer.extend(chunk)
    raw_body = bytes(raw_body_buffer)
    parsed_dict = _strict_parse_json(raw_body)
    try:
        body = QaGenerateRequest.model_validate(parsed_dict)
    except Exception:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Invalid request schema",
        ) from None

    global _GENERATION_CONSUMED, _pipeline
    if _GENERATION_CONSUMED:
        raise HTTPException(
            status_code=status.HTTP_409_CONFLICT,
            detail="Generation already consumed for this reader process",
        )

    expected_reader = _get_reader_id()
    if not body.reader_id or (expected_reader and body.reader_id != expected_reader):
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Mismatched or missing reader ID in request",
        )
    if reader_hdr and body.reader_id != reader_hdr:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Mismatched reader ID between header and body",
        )

    expected_nonce = _get_nonce()
    if not body.nonce or (expected_nonce and body.nonce != expected_nonce):
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Mismatched or missing nonce in request",
        )

    h_scope = hashlib.sha256()
    for ev in body.evidence:
        h_scope.update(ev.citation_key.encode("utf-8"))
        h_scope.update(b"\x00")
        h_scope.update(ev.excerpt.encode("utf-8"))
        h_scope.update(b"\x00")
    expected_scope_digest = h_scope.hexdigest()
    if body.scope_digest != expected_scope_digest:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Scope digest mismatch against evidence items",
        )

    question = body.question.strip()
    if not question:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Question cannot be empty",
        )

    # Validate history
    history_entries: list[dict[str, str]] = []
    if body.history:
        if len(body.history) > 4:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="History exceeds maximum allowed turns (4)",
            )
        total_cp = 0
        for h in body.history:
            role = h.role.strip().lower()
            if role not in {"user", "assistant"}:
                raise HTTPException(
                    status_code=status.HTTP_400_BAD_REQUEST,
                    detail="History role must be user or assistant",
                )
            content = h.content.strip()
            cp = len(content)
            if cp > 1024:
                raise HTTPException(
                    status_code=status.HTTP_400_BAD_REQUEST,
                    detail="History turn exceeds 1024 code points",
                )
            total_cp += cp
            history_entries.append({"role": role, "content": content})
        if total_cp > 3072:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="History total content exceeds 3072 code points",
            )

    evidence_entries = [
        {
            "citation_key": ev.citation_key,
            "excerpt": ev.excerpt,
            "source_name": ev.source_name,
        }
        for ev in body.evidence
    ]

    _GENERATION_CONSUMED = True
    # Query embedding is complete. Retain the same immutable-bound process but
    # release its ONNX session before loading the larger generator.
    _pipeline = None
    gc.collect()

    try:
        result = generate_grounded_answer(
            question=question,
            evidence=evidence_entries,
            history=history_entries,
            response_language=body.response_language,
        )
    except ValueError as exc:
        logger.warning("Candidate validation failure: %s", type(exc).__name__)
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail={"code": "QA_GENERATION_FAILED", "category": "CANDIDATE_INVALID"},
        ) from None
    except Exception as exc:
        logger.error("Candidate generation failure: %s", type(exc).__name__)
        category = (
            "HEADROOM_UNAVAILABLE"
            if isinstance(exc, RuntimeError) and str(exc) == "QA_SYSTEM_HEADROOM_BELOW_2_GIB"
            else "GENERATION_UNAVAILABLE"
        )
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail={"code": "QA_GENERATION_FAILED", "category": category},
        ) from None

    for key, value in result.measurements.items():
        response.headers[f"X-PPV-QA-{key}"] = str(value)

    claims = [
        ClaimItem(
            id=c.id,
            text=c.text,
            supports=[
                ClaimSupportItem(citation_key=s.citation_key, quote=s.quote) for s in c.supports
            ],
        )
        for c in result.claims
    ]

    conflicts = [
        ConflictPairItem(
            claim_id_a=cp.claim_id_a,
            claim_id_b=cp.claim_id_b,
            description=cp.description,
        )
        for cp in result.conflict_pairs
    ]

    return QaGenerateResponse(
        contract_version="1.0",
        reader_id=body.reader_id,
        process_id=os.getpid(),
        endpoint_nonce=_get_nonce(),
        scope_digest=body.scope_digest,
        profile=GENERATION_PROFILE,
        prompt_profile_hash=PROMPT_PROFILE_HASH,
        outcome=result.outcome,
        claims=claims,
        unanswered_parts=result.unanswered_parts,
        conflict_pairs=conflicts,
        model_version=QA_MODEL_VERSION,
        artifact_hash=QA_MODEL_SHA256,
    )


def main() -> None:
    import argparse

    import uvicorn

    parser = argparse.ArgumentParser(description="PPV RAG Grounded QA Service")
    parser.add_argument("--port", type=int, required=True, help="Port to bind")
    parser.add_argument("--reader-id", type=str, required=True, help="Active reader UUID")
    parser.add_argument("--endpoint-nonce", type=str, required=True, help="Random per-run nonce")
    parser.add_argument("--service-key", type=str, required=True, help="Shared authentication key")
    args = parser.parse_args()

    init_service(args.reader_id, args.endpoint_nonce, args.service_key)
    os.environ["PPV_RAG_READER_ID"] = args.reader_id
    os.environ["PPV_RAG_NONCE"] = args.endpoint_nonce
    os.environ["PPV_RAG_SERVICE_KEY"] = args.service_key

    get_pipeline()

    uvicorn.run(app, host="127.0.0.1", port=args.port, log_level="warning")


if __name__ == "__main__":
    main()

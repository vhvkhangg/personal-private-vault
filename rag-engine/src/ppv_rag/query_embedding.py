"""Request-bound loopback query embedding service for PPV RAG.

Runs pinned CPU MultilingualEmbeddingPipeline under an owned process with a 2 GiB
Job Object memory cap, authenticated headers, strict JSON with duplicate key detection,
and strict vector validation.
"""

from __future__ import annotations

import json
import logging
import os
from typing import Annotated, Any

from fastapi import Depends, FastAPI, Header, HTTPException, Request, status
from pydantic import BaseModel, ConfigDict

from ppv_rag.embeddings.model import (
    DIMENSION,
    MODEL_ARTIFACT_SHA256,
    MODEL_VERSION,
    TOKENIZER_ARTIFACT_SHA256,
    TOKENIZER_VERSION,
    MultilingualEmbeddingPipeline,
)

logger = logging.getLogger(__name__)

_pipeline: MultilingualEmbeddingPipeline | None = None


def get_pipeline() -> MultilingualEmbeddingPipeline:
    global _pipeline
    if _pipeline is None:
        _pipeline = MultilingualEmbeddingPipeline(force_cpu=True)
    return _pipeline


# Service credentials configured at process startup or from environment
_READER_ID: str | None = None
_NONCE: str | None = None
_SERVICE_KEY: str | None = None


def init_service(
    reader_id: str,
    endpoint_nonce: str = "",
    service_key: str = "",
    nonce: str = "",
) -> None:
    """Initialize loopback service identity and authentication credentials."""
    global _READER_ID, _NONCE, _SERVICE_KEY
    _READER_ID = reader_id
    _NONCE = endpoint_nonce or nonce
    _SERVICE_KEY = service_key


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


app = FastAPI(title="PPV RAG Query Embedding Service", docs_url=None, redoc_url=None)


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
    )


def main() -> None:
    """CLI launcher for uvicorn query embedding server."""
    import argparse

    import uvicorn

    parser = argparse.ArgumentParser(description="PPV RAG Query Embedding Service")
    parser.add_argument("--port", type=int, required=True, help="Port to bind")
    parser.add_argument("--reader-id", type=str, required=True, help="Active reader UUID")
    parser.add_argument("--endpoint-nonce", type=str, required=True, help="Random per-run nonce")
    parser.add_argument("--service-key", type=str, required=True, help="Shared authentication key")
    args = parser.parse_args()

    init_service(args.reader_id, args.endpoint_nonce, args.service_key)
    os.environ["PPV_RAG_READER_ID"] = args.reader_id
    os.environ["PPV_RAG_NONCE"] = args.endpoint_nonce
    os.environ["PPV_RAG_SERVICE_KEY"] = args.service_key

    # Warm up model eagerly
    get_pipeline()

    uvicorn.run(app, host="127.0.0.1", port=args.port, log_level="warning")


if __name__ == "__main__":
    main()

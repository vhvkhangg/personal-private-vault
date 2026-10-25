"""Worker stage execution for v2 synthetic stages and v3 multi-file document indexing."""

from __future__ import annotations

import base64
import hashlib
import hmac
import time
from collections.abc import Callable
from dataclasses import asdict
from typing import Any

from ppv_rag.chunking.chunker import StructureFirstChunker
from ppv_rag.config import settings
from ppv_rag.contracts import (
    DocumentBatchStageRequest,
    DocumentBatchStageResponse,
    WorkerReceipt,
    WorkerStageRequest,
    WorkerStageResponse,
    document_input_digest,
)
from ppv_rag.db import (
    AuthorityUnavailable,
    connection,
    seal_document_result,
    stage_document_result,
    stage_receipt,
    verify_grant,
)
from ppv_rag.document_source import document_binding, parse_prepared_document
from ppv_rag.embeddings.model import MultilingualEmbeddingPipeline
from ppv_rag.limits import ParsingCancelled, check_text, checkpoint, parsing_scope
from ppv_rag.parsers.markdown import ApprovedDependency, RestrictiveContentError, parse_markdown
from ppv_rag.parsers.models import ParsedDocument
from ppv_rag.parsers.text import parse_text
from ppv_rag.telemetry import (
    get_gpu_vram_bytes,
    get_host_ram_headroom_bytes,
    get_host_ram_total_bytes,
    get_system_ram_bytes,
    gpu_measurement_available,
)

# Strict per-file resource limits
MAX_FILE_BYTES = 10 * 1024 * 1024  # 10 MB
MAX_TEXT_CHARS = 1_000_000  # 1M characters
MAX_DOCUMENT_PAGES = 500


def verify_document_grant(request: DocumentBatchStageRequest) -> None:
    """Verify active worker grant for DOCUMENT_V3 stage type."""
    with connection() as conn:
        row = conn.execute(
            "SELECT rag.verify_worker_grant(%s,%s,%s,%s,%s,%s)",
            (
                request.job_id,
                request.attempt_id,
                request.lease_token,
                "DOCUMENT_V3",
                request.input_digest,
                request.scope_digest,
            ),
        ).fetchone()
        if row is None or row[0] is not True:
            raise AuthorityUnavailable("Worker document grant denied")


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


def process_document_batch_request(
    request: DocumentBatchStageRequest,
    cancelled: Callable[[], bool] = lambda: False,
) -> DocumentBatchStageResponse:
    # Fail closed if DSN is missing
    if not settings.worker_dsn:
        return DocumentBatchStageResponse(
            status="REJECTED",
            job_id=request.job_id,
            attempt_id=request.attempt_id,
            staged_count=0,
            published_count=0,
            failed_count=len(request.roots),
        )

    try:
        # Verify input digest of received roots
        computed_input = document_input_digest(request.roots)
        if not hmac.compare_digest(computed_input, request.input_digest):
            return DocumentBatchStageResponse(
                status="REJECTED",
                job_id=request.job_id,
                attempt_id=request.attempt_id,
                staged_count=0,
                published_count=0,
                failed_count=len(request.roots),
            )
        verify_document_grant(request)
    except Exception:
        return DocumentBatchStageResponse(
            status="REJECTED",
            job_id=request.job_id,
            attempt_id=request.attempt_id,
            staged_count=0,
            published_count=0,
            failed_count=len(request.roots),
        )

    staged_count = 0
    failed_count = 0
    peak_ram = get_system_ram_bytes()
    peak_vram = get_gpu_vram_bytes()
    pending_results: list[dict[str, Any]] = []

    try:
        embedding_pipeline = MultilingualEmbeddingPipeline()
        chunker = StructureFirstChunker(token_count=embedding_pipeline.token_count)

        for root in request.roots:
            start_time = time.perf_counter()
            start_cpu = time.process_time()
            parsed_doc: ParsedDocument | None = None
            success = False
            reason_code = "UNKNOWN"
            warning_codes: list[str] = []
            chunks_payload: list[dict[str, Any]] = []

            try:
                with parsing_scope(cancelled):
                    # Domain fields arrive only from the Java-owned prepared projection.
                    if root.format == "DOMAIN":
                        if (
                            root.content_text is not None
                            or root.content_b64 is not None
                            or root.transclusions
                        ):
                            raise PermissionError("FIELD_NOT_APPROVED")
                        parsed_doc = ParsedDocument(root.root_key, "TXT", root.source_version)
                        for field in root.prepared_fields:
                            field_doc = parse_text(
                                field.text,
                                field.source_key,
                                field.source_version,
                                field.approved_field,
                            )
                            parsed_doc.elements.extend(field_doc.elements)
                    elif "content" not in root.approved_fields and not any(
                        f in root.approved_fields for f in ("text", "body", "document")
                    ):
                        raise PermissionError("FIELD_NOT_APPROVED")

                    fmt = root.format.lower().strip()
                    if fmt == "domain":
                        pass
                    elif fmt == "md":
                        text = root.content_text or ""
                        if not text and root.content_b64:
                            raw_bytes = base64.b64decode(root.content_b64, validate=True)
                            if len(raw_bytes) > MAX_FILE_BYTES:
                                raise BufferError("RESOURCE_LIMIT_EXCEEDED")
                            text = raw_bytes.decode("utf-8", errors="strict")
                        if len(text) > MAX_TEXT_CHARS:
                            raise BufferError("RESOURCE_LIMIT_EXCEEDED")
                        deps = {
                            k: ApprovedDependency(
                                source_key=v.source_key,
                                source_version=v.source_version,
                                approved_field=v.approved_field,
                                text_content=v.text,
                            )
                            for k, v in root.transclusions.items()
                        }
                        parsed_doc = (
                            parse_prepared_document(root)
                            if root.snapshot is not None
                            else parse_markdown(
                                text,
                                root.source_id if root.snapshot is not None else root.root_key,
                                source_version=root.source_version,
                                dependencies=deps,
                                approved_metadata_fields=frozenset(root.approved_fields),
                                whole_file=True,
                            )
                        )
                    else:
                        raise ValueError("UNSUPPORTED_FORMAT")

                    check_text("\n".join(e.text for e in parsed_doc.elements) if parsed_doc else "")
                    checkpoint()
                    # Check empty content
                    if (
                        parsed_doc is None
                        or not parsed_doc.elements
                        or not any(e.text.strip() for e in parsed_doc.elements)
                    ):
                        raise ValueError("EMPTY_CONTENT")

                    warning_codes = [f"WARN_{idx}" for idx in range(len(parsed_doc.warnings))]
                    chunks = chunker.chunk(parsed_doc)
                    if not chunks:
                        raise ValueError("EMPTY_CONTENT")

                    chunk_texts = [c.content for c in chunks]
                    embeddings = embedding_pipeline.embed_batch(chunk_texts)

                    checkpoint()
                    from ppv_rag.parsers.obsidian import compact_metadata

                    for chunk, emb in zip(chunks, embeddings, strict=True):
                        checkpoint()
                        structure = dict(chunk.structure_metadata)
                        structure["constituent_metadata"] = [
                            compact_metadata(m) for m in structure.get("constituent_metadata", [])
                        ]
                        chunks_payload.append(
                            {
                                "source_identity": chunk.source_identity,
                                "content": chunk.content,
                                "locator": chunk.locator,
                                "structure_metadata": structure,
                                "embedding": emb,
                            }
                        )

                # The scope performs a final cancellation/deadline check on exit.
                # Count only roots that actually completed that check.
                success = True
                reason_code = "SUCCESS"
                staged_count += 1

            except ParsingCancelled:
                raise
            except RestrictiveContentError:
                success = False
                reason_code = "RESTRICTIVE_METADATA"
                chunks_payload = []
                failed_count += 1
            except PermissionError:
                success = False
                reason_code = "FIELD_NOT_APPROVED"
                chunks_payload = []
                failed_count += 1
            except BufferError:
                success = False
                reason_code = "RESOURCE_LIMIT_EXCEEDED"
                chunks_payload = []
                failed_count += 1
            except ValueError as ve:
                success = False
                from ppv_rag.parsers.obsidian import MarkdownFailure

                msg = str(ve)
                if isinstance(ve, MarkdownFailure):
                    reason_code = ve.code
                elif msg in {"EMPTY_CONTENT", "UNSUPPORTED_FORMAT"}:
                    reason_code = msg
                else:
                    reason_code = "PARSER_VALIDATION_ERROR"
                chunks_payload = []
                failed_count += 1
            except Exception:
                success = False
                reason_code = "PARSER_ERROR"
                chunks_payload = []
                failed_count += 1

            cur_ram = get_system_ram_bytes()
            cur_vram = get_gpu_vram_bytes()
            if cur_ram > peak_ram:
                peak_ram = cur_ram
            if cur_vram > peak_vram:
                peak_vram = cur_vram

            duration_ms = int((time.perf_counter() - start_time) * 1000)
            resource_report: dict[str, object] = {
                "peak_system_ram_bytes": peak_ram if peak_ram > 0 else None,
                "peak_gpu_vram_bytes": peak_vram if gpu_measurement_available() else None,
                "duration_ms": duration_ms,
                "worker_cpu_ms": int((time.process_time() - start_cpu) * 1000),
                "host_available_ram_bytes": get_host_ram_headroom_bytes() or None,
                "host_total_ram_bytes": get_host_ram_total_bytes() or None,
                "pipeline_identity": asdict(embedding_pipeline.metadata()),
                "ram_measurement_kind": "PROCESS_PEAK_WORKING_SET",
                "vram_measurement_kind": "DEVICE_WIDE_SAMPLE_MAX_AT_FILE_BOUNDARIES",
                "input_binding": document_binding(root),
            }

            if cancelled():
                raise ParsingCancelled("PARSING_CANCELLED")
            pending_results.append(
                dict(
                    job_id=request.job_id,
                    attempt_id=request.attempt_id,
                    lease_token=request.lease_token,
                    root_key=root.root_key,
                    success=success,
                    reason_code=reason_code,
                    warning_codes=warning_codes,
                    chunks_payload=chunks_payload,
                    resource_report=resource_report,
                )
            )

        # A selected child root's explicit failure invalidates true dependent roots,
        # including nested dependencies, while unrelated roots remain independent.
        failed_sources = {
            root.source_id
            for root, result in zip(request.roots, pending_results, strict=True)
            if not result["success"]
        }
        for _ in request.roots:
            changed = False
            for root, result in zip(request.roots, pending_results, strict=True):
                if result["success"] and any(
                    child.source_key in failed_sources for child in root.transclusions.values()
                ):
                    result.update(success=False, reason_code="DEPENDENCY_FAILED", chunks_payload=[])
                    failed_sources.add(root.source_id)
                    changed = True
            if not changed:
                break
        staged_count = sum(bool(result["success"]) for result in pending_results)
        failed_count = len(pending_results) - staged_count
        for result in pending_results:
            if cancelled():
                raise ParsingCancelled("PARSING_CANCELLED")
            stage_document_result(**result)

        if cancelled():
            raise ParsingCancelled("PARSING_CANCELLED")
        output_digest = seal_document_result(
            request.job_id, request.attempt_id, request.lease_token
        )

        receipt = {
            "contract_version": "3.0",
            "job_id": request.job_id,
            "attempt_id": request.attempt_id,
            "status": "STAGED",
            "output_digest": output_digest,
        }

        return DocumentBatchStageResponse(
            status="STAGED",
            job_id=request.job_id,
            attempt_id=request.attempt_id,
            staged_count=staged_count,
            published_count=0,
            failed_count=failed_count,
            peak_system_ram_bytes=peak_ram,
            peak_gpu_vram_bytes=peak_vram if gpu_measurement_available() else None,
            receipt=receipt,
        )
    except Exception:
        return DocumentBatchStageResponse(
            status="REJECTED",
            job_id=request.job_id,
            attempt_id=request.attempt_id,
            staged_count=0,
            published_count=0,
            failed_count=len(request.roots),
        )

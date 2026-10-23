"""Bounded, DB-credential-free D17 replay for trusted Java admission.

The staging principal supplies neither the input nor an expected proof. Java supplies
both immutable prepared input and the actual staged candidate to this local adapter.
"""

from __future__ import annotations

import hashlib
import json
import math
import sys
from pathlib import Path
from typing import Any

from ppv_rag.chunking.chunker import StructureFirstChunker
from ppv_rag.contracts import DocumentRootTask
from ppv_rag.document_source import document_binding, parse_prepared_document
from ppv_rag.embeddings.model import MultilingualEmbeddingPipeline
from ppv_rag.limits import checkpoint, parsing_scope
from ppv_rag.parsers.obsidian import MarkdownFailure, compact_metadata

MAX_CANDIDATE_BYTES = 64 * 1024 * 1024
# Component bounds cover float32 inference/reduction and JSON/pgvector round-trip
# rounding. They are not cosine/norm acceptance or a model/provider identity proof.
VECTOR_ABS_TOL = 2e-6
VECTOR_REL_TOL = 2e-5


def canonical(value: Any) -> str:
    def pg_zero(item: Any) -> Any:
        # jsonb removes the sign of floating zero. Preserve float versus integer
        # types, every other number, and original source lexemes inside strings.
        if type(item) is float and item == 0.0:
            return 0.0
        if isinstance(item, dict):
            return {key: pg_zero(part) for key, part in item.items()}
        if isinstance(item, list):
            return [pg_zero(part) for part in item]
        return item

    return json.dumps(
        pg_zero(value), ensure_ascii=False, sort_keys=True, separators=(",", ":"), allow_nan=False
    )


def verify(roots: list[DocumentRootTask], results: list[dict[str, Any]]) -> None:
    if len(results) != len(roots) or len({r["root_key"] for r in results}) != len(roots):
        raise ValueError("RESULT_ROSTER_MISMATCH")
    if [r["root_key"] for r in results] != [r.root_key for r in roots]:
        raise ValueError("RESULT_ROSTER_ORDER_MISMATCH")
    by_key = {r["root_key"]: r for r in results}
    failed_sources = {root.source_id for root in roots if not by_key[root.root_key]["success"]}
    pipeline = MultilingualEmbeddingPipeline(force_cpu=True)
    import onnxruntime as ort  # type: ignore[import-untyped]

    available_providers = ort.get_available_providers()
    chunker = StructureFirstChunker(token_count=pipeline.token_count)
    for root in roots:
        with parsing_scope():
            stage = by_key[root.root_key]
            if (
                stage["resources"].get("pipeline_identity", {}).get("provider") == "CUDA"
                and "CUDAExecutionProvider" not in available_providers
            ):
                raise ValueError("UNAVAILABLE_EXECUTION_PROVIDER")
            binding = stage["resources"].get("input_binding")
            if canonical(binding) != canonical(document_binding(root)):
                raise ValueError("RESULT_REVISION_BINDING_MISMATCH")
            expected: list[dict[str, Any]] = []
            reason = "SUCCESS"
            try:
                parsed = parse_prepared_document(root)
                if not any(e.text.strip() for e in parsed.elements):
                    raise ValueError("EMPTY_CONTENT")
                chunks = chunker.chunk(parsed)
                if not chunks:
                    raise ValueError("EMPTY_CONTENT")
                for chunk in chunks:
                    checkpoint()
                    structure = dict(chunk.structure_metadata)
                    structure["constituent_metadata"] = [
                        compact_metadata(m) for m in structure.get("constituent_metadata", [])
                    ]
                    expected.append(
                        dict(
                            source_identity=chunk.source_identity,
                            content=chunk.content,
                            locator=chunk.locator,
                            structure_metadata=structure,
                        )
                    )
            except MarkdownFailure as exc:
                reason = exc.code
            except PermissionError:
                reason = "FIELD_NOT_APPROVED"
            except ValueError as exc:
                reason = (
                    str(exc)
                    if str(exc) in {"EMPTY_CONTENT", "UNSUPPORTED_FORMAT"}
                    else "PARSER_VALIDATION_ERROR"
                )
            except BufferError:
                reason = "RESOURCE_LIMIT_EXCEEDED"
            if reason == "SUCCESS" and any(
                child.source_key in failed_sources for child in root.transclusions.values()
            ):
                reason = "DEPENDENCY_FAILED"
            if stage["success"] is not (reason == "SUCCESS") or stage["reason_code"] != reason:
                raise ValueError("RESULT_STATUS_MISMATCH")
            chunks = stage["chunks"]
            if reason != "SUCCESS":
                if chunks:
                    raise ValueError("FAILED_RESULT_HAS_CHUNKS")
                continue
            actual = []
            for chunk in chunks:
                if set(chunk) != {
                    "source_identity",
                    "content",
                    "locator",
                    "structure_metadata",
                    "embedding",
                }:
                    raise ValueError("UNAUTHORIZED_CHUNK_FIELDS")
                vector = chunk["embedding"]
                if (
                    not isinstance(vector, list)
                    or len(vector) != 384
                    or any(type(v) not in {int, float} or not math.isfinite(v) for v in vector)
                    or abs(sum(v * v for v in vector) - 1) > 0.001
                ):
                    raise ValueError("INVALID_VECTOR")
                actual.append({k: v for k, v in chunk.items() if k != "embedding"})
            if canonical(actual) != canonical(expected):
                raise ValueError("SOURCE_RESULT_MISMATCH")
            # Independently infer from source-derived text, never supplied chunk text.
            # Reuse the pinned session and existing call-local normalized-text cache.
            expected_vectors = pipeline.embed_batch([chunk["content"] for chunk in expected])
            for chunk, expected_vector in zip(chunks, expected_vectors, strict=True):
                checkpoint()
                if any(
                    not math.isclose(
                        actual_value, expected_value, rel_tol=VECTOR_REL_TOL, abs_tol=VECTOR_ABS_TOL
                    )
                    for actual_value, expected_value in zip(
                        chunk["embedding"], expected_vector, strict=True
                    )
                ):
                    raise ValueError("SOURCE_EMBEDDING_MISMATCH")


def main() -> None:
    source, destination = map(Path, sys.argv[1:])
    if source.stat().st_size > MAX_CANDIDATE_BYTES:
        raise ValueError("RESULT_SIZE_EXCEEDED")
    raw = source.read_bytes()
    value = json.loads(raw)
    verify([DocumentRootTask.model_validate(r) for r in value["roots"]], value["results"])
    with destination.open("x", encoding="ascii") as output:
        output.write(hashlib.sha256(raw).hexdigest())


if __name__ == "__main__":
    # No raw input, parser error, path, source digest or SQL reaches logs.
    try:
        main()
    except Exception:
        sys.exit(1)

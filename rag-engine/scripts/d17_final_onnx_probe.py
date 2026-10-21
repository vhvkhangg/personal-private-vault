"""Diagnostic tensor batching only; never selected by the production producer."""

from __future__ import annotations

import argparse
import hashlib
import json
import sys
import time
from pathlib import Path

import numpy as np
import onnxruntime as ort  # type: ignore[import-untyped]

sys.path.insert(0, str(Path(__file__).resolve().parents[1] / "src"))

from ppv_rag.chunking.chunker import StructureFirstChunker  # noqa: E402
from ppv_rag.embeddings.model import MultilingualEmbeddingPipeline  # noqa: E402
from ppv_rag.limits import checkpoint, parsing_scope  # noqa: E402
from ppv_rag.parsers.obsidian import parse_obsidian  # noqa: E402


def infer(engine: MultilingualEmbeddingPipeline, texts: list[str]) -> np.ndarray:
    """Same artifact/masks/pooling, genuine multi-row tensors; bypass call-local reuse."""
    encodings = [engine._tokenizer.encode(text) for text in texts]
    width = max(len(e.ids) for e in encodings)
    assert width <= 512 and len(texts) <= 8
    ids = np.full((len(texts), width), engine._tokenizer.padding["pad_id"], dtype=np.int64)
    mask = np.zeros_like(ids)
    types = np.zeros_like(ids)
    for row, e in enumerate(encodings):
        ids[row, : len(e.ids)] = e.ids
        mask[row, : len(e.ids)] = e.attention_mask
        types[row, : len(e.ids)] = e.type_ids
    checkpoint()
    hidden = np.asarray(
        engine._session.run(
            None, {"input_ids": ids, "attention_mask": mask, "token_type_ids": types}
        )[0],
        dtype=np.float32,
    )
    expanded = np.broadcast_to(mask[:, :, None], hidden.shape).astype(np.float32)
    pooled = (hidden * expanded).sum(axis=1) / np.clip(expanded.sum(axis=1), 1e-9, None)
    return np.asarray([engine._normalize_vector(row) for row in pooled])


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--output", type=Path, required=True)
    args = parser.parse_args()
    engine = MultilingualEmbeddingPipeline(force_cpu=True)
    prose = "English and tiếng Việt retain digits 0123456789 and source order.\n"
    with parsing_scope():
        chunks = StructureFirstChunker(token_count=engine.token_count).chunk(
            parse_obsidian((prose * 800)[:50000], "root", 3, "content")
        )
    texts = [c.content.strip() for c in chunks]
    records = []
    baseline = None
    for threads in (1, 2, 4, 8):
        options = ort.SessionOptions()
        options.intra_op_num_threads = threads
        options.inter_op_num_threads = 1
        options.execution_mode = ort.ExecutionMode.ORT_SEQUENTIAL
        engine._session = ort.InferenceSession(
            str(engine._model_path / "model.onnx"),
            sess_options=options,
            providers=["CPUExecutionProvider"],
        )
        for batch in (1, 4, 8):
            with parsing_scope():
                start = time.perf_counter()
                vectors = np.concatenate(
                    [infer(engine, texts[i : i + batch]) for i in range(0, len(texts), batch)]
                )
                duration = time.perf_counter() - start
            if baseline is None:
                baseline = vectors
            records.append(
                dict(
                    threads=threads,
                    tensor_batch=batch,
                    seconds=duration,
                    maximum_coordinate_delta=float(np.abs(vectors - baseline).max()),
                    minimum_cosine=float((vectors * baseline).sum(axis=1).min()),
                    same_singleton_tolerance=bool(
                        np.allclose(vectors, baseline, rtol=1e-4, atol=1e-6)
                    ),
                )
            )
            print(json.dumps(records[-1]), flush=True)
    # Quantization evidence from the frozen graph itself; no extra ONNX dependency.
    model = (engine._model_path / "model.onnx").read_bytes()
    report = dict(
        workload_chars=50000,
        chunks=len(chunks),
        records=records,
        model_sha256=hashlib.sha256(model).hexdigest(),
        graph_contains_dynamic_quantize_linear=b"DynamicQuantizeLinear" in model,
        graph_contains_matmul_integer=b"MatMulInteger" in model,
        selected="Four threads, singleton tensors, exact normalized-text call-local reuse",
        explanation=(
            "Batch rounding drift and timing provide no reason to change "
            "original singleton semantics."
        ),
    )
    args.output.write_text(json.dumps(report, indent=2) + "\n", encoding="utf-8")


if __name__ == "__main__":
    main()

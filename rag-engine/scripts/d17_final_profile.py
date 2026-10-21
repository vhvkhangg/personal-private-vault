"""Bounded stage and allocation measurements using the pinned CPU engine."""

from __future__ import annotations

import argparse
import gc
import json
import sys
import time
import tracemalloc
from dataclasses import asdict
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[1] / "src"))

from ppv_rag.chunking.chunker import StructureFirstChunker  # noqa: E402
from ppv_rag.embeddings.model import MultilingualEmbeddingPipeline  # noqa: E402
from ppv_rag.limits import parsing_scope  # noqa: E402
from ppv_rag.parsers.obsidian import parse_obsidian  # noqa: E402
from ppv_rag.telemetry import get_system_ram_bytes  # noqa: E402


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--output", type=Path, required=True)
    parser.add_argument("--compact", action="store_true")
    args = parser.parse_args()
    started = time.perf_counter()
    engine = MultilingualEmbeddingPipeline(force_cpu=True)
    report = {
        "init_seconds": time.perf_counter() - started,
        "identity": asdict(engine.metadata()),
        "workloads": [],
    }
    prose = "English and ti\u1ebfng Vi\u1ec7t retain digits 0123456789 and source order.\n"
    for kind, source in [
        ("prose-50000", (prose * 800)[:50000]),
        ("table-100", "|A|B|C|D|E|F|G|H|\n|-|-|-|-|-|-|-|-|\n" + "|a|b|c|d|e|f|g|h|\n" * 100),
        ("table-600", "|A|B|C|D|E|F|G|H|\n|-|-|-|-|-|-|-|-|\n" + "|a|b|c|d|e|f|g|h|\n" * 600),
    ]:
        timings = {}
        with parsing_scope():
            start = time.perf_counter()
            doc = parse_obsidian(source, "root", 3, "content")
            timings["parse"] = time.perf_counter() - start
            start = time.perf_counter()
            chunks = StructureFirstChunker(token_count=engine.token_count).chunk(doc)
            timings["chunk"] = time.perf_counter() - start
            start = time.perf_counter()
            vectors = engine.embed_batch([c.content for c in chunks])
            timings["embed"] = time.perf_counter() - start
            payload = [
                {
                    "content": c.content,
                    "locator": c.locator,
                    "structure_metadata": c.structure_metadata,
                    "embedding": v,
                }
                for c, v in zip(chunks, vectors, strict=True)
            ]
            tracemalloc.start()
            start = time.perf_counter()
            if args.compact:
                from ppv_rag.parsers.obsidian import compact_metadata

                for item in payload:
                    item["structure_metadata"]["constituent_metadata"] = [
                        compact_metadata(m)
                        for m in item["structure_metadata"]["constituent_metadata"]
                    ]
            serialized = json.dumps(payload, ensure_ascii=False, allow_nan=False).encode()
            timings["serialize"] = time.perf_counter() - start
            current, peak = tracemalloc.get_traced_memory()
            allocations = sum(s.count for s in tracemalloc.take_snapshot().statistics("filename"))
            report["workloads"].append(
                dict(
                    name=kind,
                    chars=len(source),
                    chunks=len(chunks),
                    seconds=timings,
                    bytes=len(serialized),
                    traced_live_bytes=current,
                    traced_peak_bytes=peak,
                    traced_live_allocations=allocations,
                    process_peak_working_set=get_system_ram_bytes(),
                )
            )
            # Drop only this workload's owned objects; measure traced allocations
            # after collection separately from cumulative native process peak RAM.
            del payload, serialized, doc, chunks, vectors
            if args.compact:
                del item
            gc.collect()
            report["workloads"][-1]["post_release_traced_live_bytes"] = (
                tracemalloc.get_traced_memory()[0]
            )
            tracemalloc.stop()
    args.output.write_text(json.dumps(report, indent=2), encoding="utf-8")
    print(json.dumps(report))


if __name__ == "__main__":
    main()

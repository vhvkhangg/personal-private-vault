"""Finite D17 documentation freeze and source-coverage probes; never imported by the worker."""

from __future__ import annotations

import argparse
import base64
import hashlib
import html
import json
import os
import re
import socket
import sys
import tempfile
import time
import urllib.request
from collections import Counter
from datetime import datetime, timezone
from pathlib import Path
from typing import Any

sys.path.insert(0, str(Path(__file__).resolve().parents[1] / "src"))

from markdown_it import MarkdownIt  # noqa: E402

from ppv_rag.chunking.chunker import StructureFirstChunker  # noqa: E402
from ppv_rag.parsers.obsidian import MarkdownFailure, parse_obsidian  # noqa: E402

NAMES = (
    "Obsidian Flavored Markdown",
    "Basic formatting syntax",
    "Advanced formatting syntax",
    "Internal links",
    "Embed files",
    "Callouts",
    "Properties",
    "Tags",
)
FROZEN_OBSIDIAN_TREE = "cd6c9f3c7d5492bbbec94e8f403572b84eeb325b"


def fetch(url: str) -> bytes:
    request = urllib.request.Request(url, headers={"User-Agent": "PPV-D17-official-source-freeze"})
    with urllib.request.urlopen(request, timeout=20) as response:
        return response.read(4_000_000)


def write(path: Path, value: Any) -> None:
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def freeze(destination: Path) -> None:
    tree = json.loads(
        fetch(
            "https://api.github.com/repos/obsidianmd/obsidian-help/git/trees/"
            + FROZEN_OBSIDIAN_TREE
            + "?recursive=1"
        )
    )
    write(destination / "official-tree.json", tree)
    records = []
    for name in NAMES:
        item = next(
            i
            for i in tree["tree"]
            if i["path"].startswith("en/") and i["path"].endswith("/" + name + ".md")
        )
        blob = json.loads(fetch(item["url"]))
        data = base64.b64decode(blob["content"])
        filename = name.replace(" ", "-") + ".md"
        (destination / filename).write_bytes(data)
        records.append(
            dict(
                title=name,
                repository_path=item["path"],
                git_blob_sha=item["sha"],
                url=item["url"],
                snapshot=filename,
                sha256=hashlib.sha256(data).hexdigest(),
                bytes=len(data),
            )
        )
    for name, url in (
        ("commonmark-0.31.2.json", "https://spec.commonmark.org/0.31.2/spec.json"),
        ("gfm-0.29.html", "https://github.github.com/gfm/"),
    ):
        data = fetch(url)
        (destination / name).write_bytes(data)
        records.append(
            dict(
                title=name,
                url=url,
                snapshot=name,
                sha256=hashlib.sha256(data).hexdigest(),
                bytes=len(data),
            )
        )
    write(
        destination / "official-sources.json",
        dict(
            retrieved_utc=datetime.now(timezone.utc).isoformat(),
            obsidian_git_tree_sha=tree["sha"],
            sources=records,
        ),
    )


def conformance(destination: Path) -> None:
    examples = json.loads((destination / "commonmark-0.31.2.json").read_text(encoding="utf-8"))
    results = []
    core = MarkdownIt("commonmark")
    for example in examples:
        text = example["markdown"]
        result = dict(
            example=example["example"],
            section=example["section"],
            core_render_matches=core.render(text) == example["html"],
        )
        try:
            doc = parse_obsidian(text, "official-example", 1, "content")
            chunks = StructureFirstChunker().chunk(doc)
            exact = "".join(c.content for c in chunks) == text
            result.update(extraction="EXACT" if exact else "COVERAGE_FAILURE")
        except MarkdownFailure as exc:
            result.update(extraction="TYPED_REJECTION", reason=exc.code)
        except Exception as exc:
            result.update(extraction="UNEXPECTED_FAILURE", reason=type(exc).__name__)
        results.append(result)
    write(
        destination / "official-example-results-v18.3.json",
        dict(
            purpose=(
                "Dependency grammar rendering and production source coverage are distinct; "
                "neither is full Obsidian semantic conformance."
            ),
            examples=len(results),
            core_render_matches=sum(r["core_render_matches"] for r in results),
            extraction_counts=dict(Counter(r["extraction"] for r in results)),
            rejection_counts=dict(Counter(r.get("reason") for r in results if "reason" in r)),
            results=results,
        ),
    )


def gfm(destination: Path) -> None:
    document = (destination / "gfm-0.29.html").read_text(encoding="utf-8")
    examples = re.findall(
        r'class="example" id="example-(\d+)">[\s\S]*?'
        r'<code class="language-markdown">([\s\S]*?)</code>',
        document,
    )
    results = []
    for number, encoded in examples:
        # The official page displays source tabs as arrows, and wraps spaces in spans.
        text = html.unescape(re.sub(r"</?span[^>]*>", "", encoded)).replace("→", "\t")
        result = dict(example=int(number), source_sha256=hashlib.sha256(text.encode()).hexdigest())
        try:
            doc = parse_obsidian(text, "gfm-example", 1, "content")
            chunks = StructureFirstChunker().chunk(doc)
            result["extraction"] = (
                "EXACT" if "".join(c.content for c in chunks) == text else "COVERAGE_FAILURE"
            )
        except MarkdownFailure as exc:
            result.update(extraction="TYPED_REJECTION", reason=exc.code)
        except Exception as exc:
            result.update(extraction="UNEXPECTED_FAILURE", reason=type(exc).__name__)
        results.append(result)
    write(
        destination / "gfm-example-results-v18.3.json",
        dict(
            purpose=(
                "Official GFM source-coverage smoke test; not rendered or structural conformance."
            ),
            examples=len(results),
            extraction_counts=dict(Counter(r["extraction"] for r in results)),
            rejection_counts=dict(Counter(r.get("reason") for r in results if "reason" in r)),
            results=results,
        ),
    )


def resources(destination: Path) -> None:
    """Actual CPU model and worker code; grant/stage boundaries explicitly isolated."""
    import ppv_rag.worker as worker
    from ppv_rag.config import Settings
    from ppv_rag.contracts import DocumentBatchStageRequest, DocumentRootTask, document_input_digest

    vault = Path(__file__).resolve().parents[1] / "tests/fixtures/phase18-d17-obsidian-v1"
    sources = {p.stem: p.read_bytes().decode("utf-8") for p in vault.glob("*.md")}
    staged: list[dict[str, Any]] = []
    worker.settings = Settings(
        service_key="synthetic-test-service-credential-00000000",
        worker_dsn="postgresql://rag_worker@127.0.0.1:5432/vault",
    )
    worker.verify_document_grant = lambda _request: None
    worker.stage_document_result = lambda **kwargs: staged.append(kwargs)

    def no_network(*_args: Any, **_kwargs: Any) -> None:
        raise AssertionError("Offline inference attempted a network connection")

    socket.socket.connect = no_network  # type: ignore[method-assign]
    roots = [
        DocumentRootTask(
            root_key="Research",
            source_id="Research",
            source_version=3,
            format="md",
            content_text=sources["Research"],
            approved_fields=["content"],
            transclusions={
                name: dict(
                    source_key=name,
                    source_version=version,
                    approved_field="content",
                    text=sources[name],
                )
                for name, version in (("Child", 7), ("Grand", 11))
            },
        )
    ]
    roots.append(
        DocumentRootTask(
            root_key="large",
            source_id="large",
            format="md",
            content_text="Large tiếng Việt **structured text**. " * 4000 + "\n",
            approved_fields=["content"],
        )
    )
    required_html = [
        "<div>\n\n**literal HTML body**\n\n</div>\n",
        "<p>First paragraph\n<p>Second paragraph\n",
    ]
    roots.extend(
        DocumentRootTask(
            root_key=f"required-html-{i}",
            source_id=f"required-html-{i}",
            format="md",
            content_text=text,
            approved_fields=["content"],
        )
        for i, text in enumerate(required_html)
    )
    request = DocumentBatchStageRequest(
        job_id="11111111-1111-1111-1111-111111111111",
        attempt_id="22222222-2222-2222-2222-222222222222",
        lease_token="a" * 64,
        roots=roots,
        input_digest=document_input_digest(roots),
    )
    start, cpu = time.monotonic(), time.process_time()
    with tempfile.TemporaryDirectory(prefix="ppv-d17-resource-") as temporary:
        os.environ["TEMP"] = os.environ["TMP"] = temporary
        response = worker.process_document_batch_request(request)
        retained_temp = sum(p.stat().st_size for p in Path(temporary).rglob("*") if p.is_file())
    reports = []
    for root, record in zip(roots, staged, strict=True):
        output = record["chunks_payload"]
        reports.append(
            dict(
                root_key=root.root_key,
                success=record["success"],
                reason=record["reason_code"],
                source_chars=len(root.content_text or ""),
                chunks=len(output),
                serialized_payload_bytes=len(json.dumps(output, ensure_ascii=False).encode()),
                resource_report=record["resource_report"],
                content_sha256=hashlib.sha256(
                    "".join(c["content"] for c in output).encode()
                ).hexdigest(),
            )
        )
    result = dict(
        scope=(
            "Actual offline CPU producer/model. Grant verification and DB staging are capture "
            "stubs, not Java/PG/FR18-3 verification."
        ),
        wall_seconds=time.monotonic() - start,
        cpu_seconds=time.process_time() - cpu,
        retained_temporary_bytes=retained_temp,
        temporary_peak="NOT_MEASURED; retained bytes are not a peak disk claim",
        network="socket.connect denied for this isolated process",
        response=response.model_dump(),
        roots=reports,
        mandatory_html_gate="PASS" if all(r["success"] for r in reports[2:]) else "FAILED",
    )
    write(destination / "producer-resources-and-mandatory-html.json", result)
    if result["mandatory_html_gate"] != "PASS":
        raise SystemExit(1)


def main() -> None:
    args = argparse.ArgumentParser(description=__doc__)
    args.add_argument("mode", choices=("freeze", "conformance", "gfm", "resources"))
    args.add_argument("--directory", type=Path, required=True)
    parsed = args.parse_args()
    parsed.directory.mkdir(parents=True, exist_ok=True)
    {"freeze": freeze, "conformance": conformance, "gfm": gfm, "resources": resources}[parsed.mode](
        parsed.directory
    )


if __name__ == "__main__":
    main()

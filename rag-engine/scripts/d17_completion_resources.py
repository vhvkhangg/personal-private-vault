"""Finite native Windows I/O + sampled transient-disk proof for D17 CPU producer workloads."""

from __future__ import annotations

import argparse
import ctypes
import json
import os
import socket
import sys
import tempfile
import threading
import time
from pathlib import Path
from typing import Any

sys.path.insert(0, str(Path(__file__).resolve().parents[1] / "src"))

import ppv_rag.worker as worker  # noqa: E402
from ppv_rag.config import Settings  # noqa: E402
from ppv_rag.contracts import DocumentBatchStageRequest, DocumentRootTask, document_input_digest  # noqa: E402
from ppv_rag.limits import parsing_scope  # noqa: E402
from ppv_rag.parsers.obsidian import parse_obsidian  # noqa: E402,F401


class _IO(ctypes.Structure):
    _fields_ = [
        (name, ctypes.c_ulonglong)
        for name in (
            "read_ops",
            "write_ops",
            "other_ops",
            "read_bytes",
            "write_bytes",
            "other_bytes",
        )
    ]


def io_counts() -> dict[str, int]:
    kernel = ctypes.WinDLL("kernel32", use_last_error=True)
    kernel.GetCurrentProcess.restype = ctypes.c_void_p
    kernel.GetProcessIoCounters.argtypes = [ctypes.c_void_p, ctypes.POINTER(_IO)]
    counts = _IO()
    if not kernel.GetProcessIoCounters(kernel.GetCurrentProcess(), ctypes.byref(counts)):
        raise OSError(ctypes.get_last_error())
    return {name: getattr(counts, name) for name, _ in _IO._fields_}


class DiskSampler:
    def __init__(self, root: Path):
        self.root = root
        self.stop = threading.Event()
        self.peak = self.samples = self.observed_files = 0
        self.max_interval = 0.0
        self.thread = threading.Thread(target=self.run, daemon=True)

    def run(self) -> None:
        previous = time.monotonic()
        while not self.stop.is_set():
            now = time.monotonic()
            self.max_interval = max(self.max_interval, now - previous)
            previous = now
            sizes = []
            for file in self.root.rglob("*"):
                try:
                    if file.is_file():
                        sizes.append(file.stat().st_size)
                except FileNotFoundError:
                    pass
            self.peak = max(self.peak, sum(sizes))
            self.observed_files = max(self.observed_files, len(sizes))
            self.samples += 1
            self.stop.wait(0.002)


class DirectoryChanges:
    """Persistent native notifications retain transient create/delete events during GIL gaps."""

    def __init__(self, root: str):
        from ctypes import wintypes

        self.kernel = ctypes.WinDLL("kernel32", use_last_error=True)
        self.kernel.FindFirstChangeNotificationW.argtypes = [
            wintypes.LPCWSTR,
            wintypes.BOOL,
            wintypes.DWORD,
        ]
        self.kernel.FindFirstChangeNotificationW.restype = wintypes.HANDLE
        self.kernel.FindNextChangeNotification.argtypes = [wintypes.HANDLE]
        self.kernel.FindCloseChangeNotification.argtypes = [wintypes.HANDLE]
        self.kernel.WaitForSingleObject.argtypes = [wintypes.HANDLE, wintypes.DWORD]
        self.handle = self.kernel.FindFirstChangeNotificationW(root, True, 0x1B)
        if self.handle == ctypes.c_void_p(-1).value:
            raise OSError(ctypes.get_last_error())

    def poll(self) -> bool:
        result = self.kernel.WaitForSingleObject(self.handle, 0)
        if result == 258:
            return False
        if result != 0 or not self.kernel.FindNextChangeNotification(self.handle):
            raise OSError(ctypes.get_last_error())
        return True

    def close(self) -> None:
        self.kernel.FindCloseChangeNotification(self.handle)


def request(text: str) -> DocumentBatchStageRequest:
    roots = [
        DocumentRootTask(
            root_key="resource",
            source_id="resource",
            source_version=3,
            format="md",
            content_text=text,
            approved_fields=["content"],
        )
    ]
    return DocumentBatchStageRequest(
        job_id="11111111-1111-1111-1111-111111111111",
        attempt_id="22222222-2222-2222-2222-222222222222",
        lease_token="a" * 64,
        roots=roots,
        input_digest=document_input_digest(roots),
    )


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--output", type=Path, required=True)
    parser.add_argument("--only", action="append", default=[])
    args = parser.parse_args()
    worker.settings = Settings(
        service_key="synthetic-test-service-credential-00000000",
        worker_dsn="postgresql://rag_worker@127.0.0.1:5432/vault",
    )
    worker.verify_document_grant = lambda _request: None
    captured: list[dict[str, Any]] = []
    worker.stage_document_result = lambda **kwargs: captured.append(kwargs)

    def no_network(*_args: Any, **_kwargs: Any) -> None:
        raise AssertionError("Unexpected network attempt in offline resource proof")

    socket.socket.connect = no_network  # type: ignore[method-assign]
    socket.socket.connect_ex = no_network  # type: ignore[method-assign]
    socket.getaddrinfo = no_network
    prose = "English and tiếng Việt retain digits 0123456789 and source order.\n"
    maximum = (prose * (1_000_000 // len(prose) + 1))[:1_000_000]
    table = "|" + "|".join(["H"] * 8) + "|\n|" + "|".join(["-"] * 8) + "|\n"
    table += ("|" + "|".join(["123"] * 8) + "|\n") * 1800
    html = "<div>\n\n" + "<span>x</span>" * 9000 + "\n\n</div>\n"
    huge_table = "|" + "|".join(["H"] * 100) + "|\n|" + "|".join(["-"] * 100) + "|\n"
    huge_table += ("|" + "|".join(["1"] * 100) + "|\n") * 300
    workloads = [
        ("maximum-source-1000000", maximum, "SUCCESS"),
        ("near-node-cap-table", table, "SUCCESS"),
        ("near-node-cap-html", html, "SUCCESS"),
        ("table-expansion-rejection", huge_table, "RESOURCE_LIMIT_EXCEEDED"),
        (
            "unsafe-html-cleanup",
            "Good prefix.\n\n<div>\n\n<script>execute()</script>\n\n</div>",
            "MD_UNSUPPORTED_ACTIVE_HTML",
        ),
        ("actual-parser-cancellation", maximum, "CANCELLED"),
        ("actual-parser-deadline", maximum, "RESOURCE_LIMIT_EXCEEDED"),
    ]
    results = []
    if args.only:
        assert set(args.only) <= {name for name, _, _ in workloads}
        workloads = [w for w in workloads if w[0] in args.only]
    args.output.parent.mkdir(parents=True, exist_ok=True)
    with tempfile.TemporaryDirectory(prefix="ppv-d17-transient-") as scratch:
        os.environ["TEMP"] = os.environ["TMP"] = scratch
        tempfile.tempdir = scratch
        changes = DirectoryChanges(scratch)
        sampler = DiskSampler(Path(scratch))
        sampler.thread.start()
        calibration = Path(scratch) / "calibration.bin"
        calibration.write_bytes(b"x" * 4_194_304)
        end = time.monotonic() + 1
        while sampler.peak < 4_194_304 and time.monotonic() < end:
            time.sleep(0.005)
        calibration_peak = sampler.peak
        calibration.unlink()
        assert calibration_peak >= 4_194_304
        assert changes.poll(), "Native transient-file notification calibration failed"
        # Calibration deletion must be drained before actual workloads start.
        changes.poll()
        sampler.peak = sampler.observed_files = 0
        for name, text, expected in workloads:
            captured.clear()
            calls = 0

            def cancelled() -> bool:
                nonlocal calls
                calls += 1
                return name == "actual-parser-cancellation" and calls >= 1000

            if name == "actual-parser-deadline":
                worker.parsing_scope = lambda callback: parsing_scope(callback, seconds=0.003)
            before = io_counts()
            started, cpu = time.monotonic(), time.process_time()
            response = worker.process_document_batch_request(request(text), cancelled=cancelled)
            wall, cpu = time.monotonic() - started, time.process_time() - cpu
            after = io_counts()
            worker.parsing_scope = parsing_scope
            record: dict[str, Any] = dict(
                name=name,
                input_chars=len(text),
                expected=expected,
                wall_seconds=wall,
                cpu_seconds=cpu,
                response=response.model_dump(),
                io_delta={k: after[k] - before[k] for k in before},
                cancellation_checks=calls,
            )
            if captured:
                staged = captured[0]
                chunks = staged["chunks_payload"]
                exact = "".join(c["content"] for c in chunks) == text
                if chunks:
                    exact = exact and all(
                        c["content"] == text[c["locator"]["char_start"] : c["locator"]["char_end"]]
                        for c in chunks
                    )
                record.update(
                    reason=staged["reason_code"],
                    chunks=len(chunks),
                    exact_source=exact,
                    resource_report=staged["resource_report"],
                    output_bytes=len(json.dumps(chunks, ensure_ascii=False).encode()),
                )
                record["passed"] = staged["reason_code"] == expected and (
                    exact if expected == "SUCCESS" else not chunks
                )
            else:
                record.update(
                    reason="CANCELLED_NO_STAGE",
                    chunks=0,
                    passed=expected == "CANCELLED" and response.status == "REJECTED",
                )
            record["retained_temporary_bytes"] = sum(
                f.stat().st_size for f in Path(scratch).rglob("*") if f.is_file()
            )
            record["native_temporary_change_detected"] = changes.poll()
            results.append(record)
        sampler.stop.set()
        sampler.thread.join(timeout=2)
        changes.close()
        disk = dict(
            calibration_peak_bytes=calibration_peak,
            sampled_transient_peak_bytes=sampler.peak,
            observed_file_count=sampler.observed_files,
            samples=sampler.samples,
            nominal_interval_seconds=0.002,
            maximum_observed_interval_seconds=sampler.max_interval,
            retained_bytes=sum(f.stat().st_size for f in Path(scratch).rglob("*") if f.is_file()),
        )
    report = dict(
        scope=(
            "Actual selected CPU model/worker. Grant verification and DB stage captured; "
            "no Java/publication or independent review."
        ),
        network="connect/connect_ex/getaddrinfo denied for this isolated process",
        disk=disk,
        workloads=results,
        passed=all(r["passed"] and not r["native_temporary_change_detected"] for r in results)
        and disk["retained_bytes"] == 0,
    )
    args.output.write_text(
        json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8"
    )
    print(
        json.dumps(
            dict(
                passed=report["passed"],
                workloads=[
                    {k: r[k] for k in ("name", "passed", "wall_seconds", "reason")} for r in results
                ],
                disk=disk,
            )
        )
    )
    if not report["passed"]:
        raise SystemExit(1)


if __name__ == "__main__":
    main()

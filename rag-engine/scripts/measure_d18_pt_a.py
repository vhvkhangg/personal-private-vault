"""Sample only the launched D18-PT-A diagnostic tree; no machine-wide process mutation."""

from __future__ import annotations

import argparse
import ctypes as ct
import json
import os
import subprocess
import sys
import time
from ctypes import wintypes as w
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / "rag-engine/src"))
from ppv_rag import telemetry  # noqa: E402


class ProcessEntry(ct.Structure):
    _fields_ = [
        ("size", w.DWORD),
        ("usage", w.DWORD),
        ("pid", w.DWORD),
        ("heap", ct.c_size_t),
        ("module", w.DWORD),
        ("threads", w.DWORD),
        ("parent", w.DWORD),
        ("priority", w.LONG),
        ("flags", w.DWORD),
        ("exe", w.WCHAR * 260),
    ]


kernel = ct.WinDLL("kernel32", use_last_error=True)
kernel.CreateToolhelp32Snapshot.argtypes = [w.DWORD, w.DWORD]
kernel.CreateToolhelp32Snapshot.restype = w.HANDLE
kernel.Process32FirstW.argtypes = [w.HANDLE, ct.POINTER(ProcessEntry)]
kernel.Process32NextW.argtypes = [w.HANDLE, ct.POINTER(ProcessEntry)]
kernel.OpenProcess.argtypes = [w.DWORD, w.BOOL, w.DWORD]
kernel.OpenProcess.restype = w.HANDLE
kernel.CloseHandle.argtypes = [w.HANDLE]
kernel.GetProcessTimes.argtypes = [w.HANDLE] + [ct.POINTER(ct.c_ulonglong)] * 4
kernel.GetProcessHandleCount.argtypes = [w.HANDLE, ct.POINTER(w.DWORD)]


def parents() -> dict[int, int]:
    snapshot = kernel.CreateToolhelp32Snapshot(2, 0)
    if snapshot == ct.c_void_p(-1).value:
        raise OSError(ct.get_last_error(), "Process snapshot unavailable")
    result = {}
    try:
        entry = ProcessEntry()
        entry.size = ct.sizeof(entry)
        ok = kernel.Process32FirstW(snapshot, ct.byref(entry))
        while ok:
            result[int(entry.pid)] = int(entry.parent)
            ok = kernel.Process32NextW(snapshot, ct.byref(entry))
    finally:
        kernel.CloseHandle(snapshot)
    return result


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("lane", choices=["development", "held_out"])
    lane = parser.parse_args().lane
    evidence = ROOT / "logs/phase18-d18-pt-a"
    temp = evidence / (lane + "-owned-temp")
    temp.mkdir(exist_ok=False)
    environment = os.environ.copy()
    environment.update(
        TEMP=str(temp), TMP=str(temp), PYTHONDONTWRITEBYTECODE="1", OMP_THREAD_LIMIT="2"
    )
    launched = time.monotonic()
    handles: dict[int, int] = {}
    cpu: dict[int, float] = {}
    peaks: dict[int, int] = {}
    peak_ws = peak_private = peak_handles = peak_temp = 0
    headroom = None
    samples = 0
    missing = set()
    with (evidence / f"ocr-{lane}-console.log").open("w", encoding="utf-8") as output:
        process = subprocess.Popen(
            [sys.executable, str(ROOT / "rag-engine/scripts/d18_pt_a_ocr_checkpoint.py"), lane],
            cwd=ROOT,
            env=environment,
            stdout=output,
            stderr=subprocess.STDOUT,
        )
        known = {process.pid}
        try:
            while True:
                snapshot = parents()
                while True:
                    additions = {pid for pid, parent in snapshot.items() if parent in known} - known
                    if not additions:
                        break
                    known.update(additions)
                ws = private = count = 0
                for pid in known:
                    if pid not in handles and pid in snapshot:
                        handle = kernel.OpenProcess(0x410, False, pid)
                        if handle:
                            handles[pid] = handle
                        else:
                            missing.add(pid)
                    handle = handles.get(pid)
                    if not handle:
                        continue
                    memory = telemetry.PROCESS_MEMORY_COUNTERS_EX()
                    memory.cb = ct.sizeof(memory)
                    if telemetry._get_mem_info(handle, ct.byref(memory), memory.cb):
                        ws += memory.WorkingSetSize
                        private += memory.PrivateUsage
                        peaks[pid] = max(peaks.get(pid, 0), int(memory.PeakWorkingSetSize))
                    values = [ct.c_ulonglong() for _ in range(4)]
                    if kernel.GetProcessTimes(handle, *[ct.byref(v) for v in values]):
                        cpu[pid] = (values[2].value + values[3].value) / 10_000_000
                    n = w.DWORD()
                    if kernel.GetProcessHandleCount(handle, ct.byref(n)):
                        count += n.value
                samples += 1
                peak_ws, peak_private, peak_handles = (
                    max(peak_ws, ws),
                    max(peak_private, private),
                    max(peak_handles, count),
                )
                free = telemetry.get_host_ram_headroom_bytes()
                if free:
                    headroom = free if headroom is None else min(headroom, free)
                peak_temp = max(
                    peak_temp, sum(p.stat().st_size for p in temp.rglob("*") if p.is_file())
                )
                if process.poll() is not None:
                    break
                if time.monotonic() - launched > 150:
                    # Only the PID launched above and its descendants; preserve failure and output.
                    subprocess.run(
                        ["taskkill.exe", "/PID", str(process.pid), "/T", "/F"],
                        timeout=10,
                        check=False,
                    )
                    raise TimeoutError(
                        "Owned diagnostic exceeded its 150-second measurement deadline"
                    )
                time.sleep(0.02)
        finally:
            for handle in handles.values():
                kernel.CloseHandle(handle)
    elapsed = time.monotonic() - launched
    after = parents()
    survivors = sorted(known & after.keys())
    remaining = [p.name for p in temp.rglob("*") if p.is_file()]
    if not remaining:
        temp.rmdir()
    report = {
        "lane": lane,
        "child_exit": process.returncode,
        "wall_seconds": elapsed,
        "sampled_tree_peak_working_set_bytes": peak_ws,
        "sum_per_process_lifetime_peak_working_sets_upper_bound_bytes": sum(peaks.values()),
        "sampled_tree_peak_private_bytes": peak_private,
        "host_total_physical_bytes": telemetry.get_host_ram_total_bytes() or None,
        "host_min_available_physical_bytes": headroom,
        "tree_cpu_seconds": sum(cpu.values()),
        "average_cpu_percent_one_core": sum(cpu.values()) / elapsed * 100,
        "logical_cpu_count": os.cpu_count(),
        "peak_sampled_tree_handle_count": peak_handles,
        "owned_pids": sorted(known),
        "processes_sampled": len(handles),
        "unreadable_pids": sorted(missing),
        "samples": samples,
        "sample_interval_target_ms": 20,
        "sampled_peak_owned_temp_disk_bytes": peak_temp,
        "remaining_temp_files": remaining,
        "owned_survivor_pids": survivors,
        "attributable_gpu_peak_bytes": None,
        "gpu_measurement": (
            "UNAVAILABLE/NOT_VERIFIED; selected Tesseract path CPU-only. "
            "No device-wide usage attributed to worker."
        ),
        "limits": (
            "Small diagnostics only; 150s inner deadline, existing production thresholds unchanged"
        ),
        "limitations": (
            "Sampled tree working-set lower bound; summed per-process OS peaks are "
            "an upper bound, not simultaneous tree peak. Snapshot may miss sub-20ms "
            "children/temp files. Tree CPU uses retained process handles; no "
            "process-wide cap approval. OS releases exited-process handles; native "
            "allocation cancellation/restart NOT_VERIFIED."
        ),
    }
    (evidence / f"resources-{lane}.json").write_text(json.dumps(report, indent=2) + "\n")
    print(json.dumps(report))
    return int(process.returncode) if not survivors else 1


if __name__ == "__main__":
    raise SystemExit(main())

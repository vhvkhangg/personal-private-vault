"""Windows observer primitives. The obsolete fixed-path OCR execution lane is retired."""

from __future__ import annotations

import ctypes as ct
import sys
from ctypes import wintypes as w
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / "rag-engine/src"))
from ppv_rag import telemetry as telemetry  # noqa: E402


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


if __name__ == "__main__":
    raise SystemExit("Retired historical OCR launcher; import observer primitives only")

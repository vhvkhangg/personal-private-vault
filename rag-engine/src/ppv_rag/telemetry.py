"""Separately measured 16 GB system RAM and 8 GB VRAM resource tracking."""

import ctypes
import sys
from ctypes import wintypes
from typing import Any

_HAS_PSAPI = False
_HAS_NVML = False
_HAS_KERNEL32 = False
_get_mem_info: Any = None

if sys.platform == "win32":
    try:

        class PROCESS_MEMORY_COUNTERS_EX(ctypes.Structure):
            _fields_ = [
                ("cb", wintypes.DWORD),
                ("PageFaultCount", wintypes.DWORD),
                ("PeakWorkingSetSize", ctypes.c_size_t),
                ("WorkingSetSize", ctypes.c_size_t),
                ("QuotaPeakPagedPoolUsage", ctypes.c_size_t),
                ("QuotaPagedPoolUsage", ctypes.c_size_t),
                ("QuotaPeakNonPagedPoolUsage", ctypes.c_size_t),
                ("QuotaNonPagedPoolUsage", ctypes.c_size_t),
                ("PagefileUsage", ctypes.c_size_t),
                ("PeakPagefileUsage", ctypes.c_size_t),
                ("PrivateUsage", ctypes.c_size_t),
            ]

        class MEMORYSTATUSEX(ctypes.Structure):
            _fields_ = [
                ("dwLength", wintypes.DWORD),
                ("dwMemoryLoad", wintypes.DWORD),
                ("ullTotalPhys", ctypes.c_ulonglong),
                ("ullAvailPhys", ctypes.c_ulonglong),
                ("ullTotalPageFile", ctypes.c_ulonglong),
                ("ullAvailPageFile", ctypes.c_ulonglong),
                ("ullTotalVirtual", ctypes.c_ulonglong),
                ("ullAvailVirtual", ctypes.c_ulonglong),
                ("ullAvailExtendedVirtual", ctypes.c_ulonglong),
            ]

        _kernel32 = ctypes.WinDLL("kernel32", use_last_error=True)
        _kernel32.GetCurrentProcess.restype = ctypes.c_void_p
        _kernel32.GlobalMemoryStatusEx.argtypes = [ctypes.POINTER(MEMORYSTATUSEX)]
        _kernel32.GlobalMemoryStatusEx.restype = wintypes.BOOL

        _get_mem_info = getattr(_kernel32, "K32GetProcessMemoryInfo", None)
        if _get_mem_info is None:
            _psapi = ctypes.WinDLL("psapi", use_last_error=True)
            _get_mem_info = _psapi.GetProcessMemoryInfo
        _get_mem_info.argtypes = [
            ctypes.c_void_p,
            ctypes.c_void_p,
            wintypes.DWORD,
        ]
        _get_mem_info.restype = wintypes.BOOL
        _HAS_PSAPI = True
        _HAS_KERNEL32 = True
    except Exception:
        _HAS_PSAPI = False
        _HAS_KERNEL32 = False

    try:
        _nvml = ctypes.CDLL("nvml.dll")
        if _nvml.nvmlInit_v2() == 0:

            class nvmlMemory_t(ctypes.Structure):
                _fields_ = [
                    ("total", ctypes.c_ulonglong),
                    ("free", ctypes.c_ulonglong),
                    ("used", ctypes.c_ulonglong),
                ]

            _nvml_dev = ctypes.c_void_p()
            if _nvml.nvmlDeviceGetHandleByIndex_v2(0, ctypes.byref(_nvml_dev)) == 0:
                _HAS_NVML = True
    except Exception:
        _HAS_NVML = False


def get_system_ram_bytes() -> int:
    """Return process peak working set in bytes, or 0 if unavailable."""
    if not _HAS_PSAPI or _get_mem_info is None:
        return 0
    try:
        pmc = PROCESS_MEMORY_COUNTERS_EX()
        pmc.cb = ctypes.sizeof(PROCESS_MEMORY_COUNTERS_EX)
        handle = _kernel32.GetCurrentProcess()
        if _get_mem_info(handle, ctypes.byref(pmc), pmc.cb):
            return int(pmc.PeakWorkingSetSize)
    except Exception:
        pass
    return 0


def get_host_ram_headroom_bytes() -> int:
    """Return available physical system RAM in bytes (headroom), or 0 if unavailable."""
    if not _HAS_KERNEL32:
        return 0
    try:
        stat = MEMORYSTATUSEX()
        stat.dwLength = ctypes.sizeof(MEMORYSTATUSEX)
        if _kernel32.GlobalMemoryStatusEx(ctypes.byref(stat)):
            return int(stat.ullAvailPhys)
    except Exception:
        pass
    return 0


def get_host_ram_total_bytes() -> int:
    """Return total physical system RAM in bytes, or 0 if unavailable."""
    if not _HAS_KERNEL32:
        return 0
    try:
        stat = MEMORYSTATUSEX()
        stat.dwLength = ctypes.sizeof(MEMORYSTATUSEX)
        if _kernel32.GlobalMemoryStatusEx(ctypes.byref(stat)):
            return int(stat.ullTotalPhys)
    except Exception:
        pass
    return 0


def get_gpu_vram_bytes() -> int:
    """Return GPU VRAM used in bytes, or 0 if unavailable."""
    if not _HAS_NVML:
        return 0
    try:
        mem = nvmlMemory_t()
        if _nvml.nvmlDeviceGetMemoryInfo(_nvml_dev, ctypes.byref(mem)) == 0:
            return int(mem.used)
    except Exception:
        pass
    return 0


def gpu_measurement_available() -> bool:
    return _HAS_NVML


def measure_hardware_snapshot() -> dict[str, int]:
    """Capture instantaneous RAM and VRAM snapshot."""
    return {
        "system_ram_peak_bytes": get_system_ram_bytes(),
        "host_ram_headroom_bytes": get_host_ram_headroom_bytes(),
        "host_ram_total_bytes": get_host_ram_total_bytes(),
        "gpu_vram_used_bytes": get_gpu_vram_bytes(),
    }

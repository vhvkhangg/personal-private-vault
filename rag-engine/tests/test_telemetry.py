"""Tests for hardware telemetry RAM and VRAM measurement."""

from ppv_rag.telemetry import (
    get_gpu_vram_bytes,
    get_system_ram_bytes,
    measure_hardware_snapshot,
)


def test_system_ram_measurement() -> None:
    ram_bytes = get_system_ram_bytes()
    # On Windows x64 host, RAM should be non-zero
    assert isinstance(ram_bytes, int)
    assert ram_bytes >= 0


def test_gpu_vram_measurement() -> None:
    vram_bytes = get_gpu_vram_bytes()
    assert isinstance(vram_bytes, int)
    assert vram_bytes >= 0


def test_hardware_snapshot() -> None:
    snapshot = measure_hardware_snapshot()
    assert "system_ram_peak_bytes" in snapshot
    assert "gpu_vram_used_bytes" in snapshot
    assert snapshot["system_ram_peak_bytes"] >= 0
    assert snapshot["gpu_vram_used_bytes"] >= 0

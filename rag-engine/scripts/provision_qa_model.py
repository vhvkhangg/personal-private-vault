"""Explicit provisioning script for QA model (C1: Qwen3-4B-Q4_K_M.gguf) and runtime wheel.
Never imported or executed by worker runtime.

P19-S19-2 — SUSPENDED / NOT_ACCEPTED / ARCHIVAL_ONLY; do not run provisioning.
Authority: docs/implementation/phase-19/owner-decision-s19-2-source-archival-only-2026-10-31.md.
"""

from __future__ import annotations

import argparse
import hashlib
import os
import urllib.request
from pathlib import Path

# Wheel candidate
WHEEL_URL = (
    "https://github.com/abetlen/llama-cpp-python/releases/download/v0.3.36/"
    "llama_cpp_python-0.3.36-py3-none-win_amd64.whl"
)
WHEEL_SHA256 = "ae5e88a2cf464e5a2dde7c8898c2d5c71cc24d03cac6396bf7509456bde0b9fb"
WHEEL_EXPECTED_BYTES = 7767370

# Model candidate C1: Qwen3-4B, Q4_K_M
C1_URL = (
    "https://huggingface.co/Qwen/Qwen3-4B-GGUF/resolve/"
    "bc640142c66e1fdd12af0bd68f40445458f3869b/Qwen3-4B-Q4_K_M.gguf"
)
C1_SHA256 = "7485fe6f11af29433bc51cab58009521f205840f5b4ae3a32fa7f92e8534fdf5"
C1_EXPECTED_BYTES = 2497280256

C2_REVISION = "bb5d59e06d9551d752d08b292a50eb208b07ab1f"
C2_PARTS = {
    1: (3993201344, "dfce12e3862a5283ccfb88221b48480e58745165de856439950d0f22590580db"),
    2: (689872288, "539cf93f78e887edea1c04e2d7d8cdaca9d01dae9c9025bcb8accbe29df3d72a"),
}


def provision_c2_part(models_dir: Path, part: int) -> None:
    """Bounded resumable transfer of one approved split; no inference or overwrite."""
    size, expected_hash = C2_PARTS[part]
    name = f"qwen2.5-7b-instruct-q4_k_m-{part:05d}-of-00002.gguf"
    target = models_dir / name
    partial = models_dir / (name + ".partial")
    if target.exists():
        if (
            target.stat().st_size != size
            or hashlib.file_digest(target.open("rb"), "sha256").hexdigest() != expected_hash
        ):
            raise ValueError("Existing C2 artifact mismatch; refusing overwrite")
        print(f"VERIFIED {name}")
        return
    offset = partial.stat().st_size if partial.exists() else 0
    if offset > size:
        raise ValueError("Oversized partial artifact; refusing alteration")
    if offset < size:
        end = min(offset + 512 * 1024 * 1024, size) - 1
        url = f"https://huggingface.co/Qwen/Qwen2.5-7B-Instruct-GGUF/resolve/{C2_REVISION}/{name}"
        request = urllib.request.Request(url, headers={"Range": f"bytes={offset}-{end}"})
        with urllib.request.urlopen(request, timeout=30) as response:
            if (
                response.status != 206
                or response.headers.get("Content-Range") != f"bytes {offset}-{end}/{size}"
            ):
                raise ValueError("Server did not honor exact bounded resume range")
            remaining = end - offset + 1
            with partial.open("ab" if offset else "xb") as output:
                while remaining:
                    chunk = response.read(min(1024 * 1024, remaining))
                    if not chunk:
                        raise ValueError("Incomplete transfer; partial preserved")
                    output.write(chunk)
                    remaining -= len(chunk)
                output.flush()
                os.fsync(output.fileno())
        print(f"TRANSFER {name} {end + 1}/{size}")
    if partial.stat().st_size == size:
        with partial.open("rb") as source:
            actual = hashlib.file_digest(source, "sha256").hexdigest()
        if actual != expected_hash:
            raise ValueError("C2 partial hash mismatch; retained, never loadable")
        # Windows rename refuses an existing destination; never replace accepted bytes.
        partial.rename(target)
        print(f"VERIFIED {name} sha256={actual}")


def download_streaming_verified(
    url: str, expected_sha256: str, expected_size: int, target: Path
) -> None:
    target.parent.mkdir(parents=True, exist_ok=True)
    temp_target = target.with_suffix(target.suffix + ".part")

    if target.exists():
        if target.stat().st_size == expected_size:
            print(f"Checking existing artifact {target.name}...")
            hasher = hashlib.sha256()
            with open(target, "rb") as f:
                while chunk := f.read(1024 * 1024):
                    hasher.update(chunk)
            actual_sha = hasher.hexdigest()
            if actual_sha == expected_sha256:
                print(f"Verified existing artifact {target.name}: {actual_sha}")
                return
            print(f"Hash mismatch for existing {target.name}, re-downloading...")
        else:
            print(f"Size mismatch for existing {target.name}, re-downloading...")

    print(f"Downloading {target.name} ({expected_size} bytes) from {url}...")
    req = urllib.request.Request(url, headers={"User-Agent": "PPV-RAG-Provisioner/1.0"})
    hasher = hashlib.sha256()
    bytes_downloaded = 0
    chunk_size = 1024 * 1024  # 1 MiB

    with urllib.request.urlopen(req, timeout=300) as response, open(temp_target, "wb") as out_f:
        while True:
            chunk = response.read(chunk_size)
            if not chunk:
                break
            out_f.write(chunk)
            hasher.update(chunk)
            bytes_downloaded += len(chunk)
            if bytes_downloaded % (20 * 1024 * 1024) == 0 or bytes_downloaded == expected_size:
                pct = bytes_downloaded * 100 // expected_size
                print(f"Progress: {bytes_downloaded} / {expected_size} bytes ({pct}%)")

    if bytes_downloaded != expected_size:
        temp_target.unlink(missing_ok=True)
        raise ValueError(
            f"Size mismatch for {target.name}: expected {expected_size}, got {bytes_downloaded}"
        )

    actual_sha = hasher.hexdigest()
    if actual_sha != expected_sha256:
        temp_target.unlink(missing_ok=True)
        raise ValueError(
            f"SHA-256 mismatch for {target.name}: expected {expected_sha256}, got {actual_sha}"
        )

    # Atomic rename upon verification
    temp_target.replace(target)
    print(f"Successfully provisioned and verified {target.name}: {actual_sha}")


def main() -> None:
    parser = argparse.ArgumentParser(description="Provision QA model or runtime wheel.")
    parser.add_argument("--wheel-only", action="store_true", help="Download only runtime wheel")
    parser.add_argument("--model-only", action="store_true", help="Download only C1 model")
    parser.add_argument("--c2-part", type=int, choices=(1, 2), help="Resume at most 512 MiB of C2")
    args = parser.parse_args()

    base = Path(__file__).resolve().parents[1]
    models_dir = base / "models"
    models_dir.mkdir(parents=True, exist_ok=True)

    if args.c2_part is not None:
        provision_c2_part(models_dir, args.c2_part)
        return

    if not args.model_only:
        wheel_target = models_dir / "llama_cpp_python-0.3.36-py3-none-win_amd64.whl"
        download_streaming_verified(WHEEL_URL, WHEEL_SHA256, WHEEL_EXPECTED_BYTES, wheel_target)

    if not args.wheel_only:
        c1_target = models_dir / "Qwen3-4B-Q4_K_M.gguf"
        download_streaming_verified(C1_URL, C1_SHA256, C1_EXPECTED_BYTES, c1_target)

    print("QA provisioning completed successfully.")


if __name__ == "__main__":
    main()

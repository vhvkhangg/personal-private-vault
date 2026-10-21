"""Installed locked environment version/license receipt; no source-bearing data."""

import argparse
import hashlib
import json
import platform
import re
import sys
import tomllib
from importlib.metadata import distributions
from pathlib import Path

import psycopg

parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument(
    "--output",
    type=Path,
    default=Path(
        "../logs/phase17-alternative-b-implementation-2026-10-15/dependency-inventory.json"
    ),
)
arguments = parser.parse_args()
lock_path = Path("uv.lock") if Path("uv.lock").exists() else Path("rag-engine/uv.lock")
locked = tomllib.loads(lock_path.read_text(encoding="utf-8"))
locked_versions = {package["name"]: package["version"] for package in locked["package"]}

records = []
for package in sorted(distributions(), key=lambda item: item.metadata.get("Name", "").lower()):
    metadata = package.metadata
    license_value = metadata.get("License-Expression") or metadata.get("License")
    if not license_value:
        license_value = (
            "; ".join(
                value
                for value in metadata.get_all("Classifier", [])
                if value.startswith("License ::")
            )
            or "METADATA_UNSPECIFIED: inspect distribution license files"
        )
    license_files = []
    for relative in package.files or []:
        if "license" in str(relative).lower() or "copying" in str(relative).lower():
            actual = Path(package.locate_file(relative))
            if actual.is_file():
                license_files.append(
                    {
                        "file": str(relative),
                        "sha256": hashlib.sha256(actual.read_bytes()).hexdigest(),
                    }
                )
    name = metadata["Name"]
    records.append(
        {
            "name": name,
            "version": package.version,
            "locked_version": locked_versions.get(re.sub(r"[-_.]+", "-", name.lower())),
            "license": license_value,
            "license_files": license_files,
            "requires_dist": metadata.get_all("Requires-Dist", []),
        }
    )
assert all(record["version"] == record["locked_version"] for record in records), (
    "Installed environment differs from the approved lock"
)
destination = arguments.output
destination.parent.mkdir(parents=True, exist_ok=True)
destination.write_text(
    json.dumps(
        {
            "python": sys.version.split()[0],
            "platform": platform.platform(),
            "libpq_version": psycopg.pq.version(),
            "uv_lock_sha256": hashlib.sha256(lock_path.read_bytes()).hexdigest(),
            "installed_packages": records,
            "external_requests_performed": False,
            "os_egress_enforcement": "NOT_VERIFIED_BY_THIS_INVENTORY",
            "bundled_native_library_redistribution_approval": "NOT_ESTABLISHED_BY_METADATA",
        },
        indent=2,
    ),
    encoding="utf-8",
)
print(
    json.dumps(
        {"installed_packages": len(records), "lock_matches": True, "receipt": str(destination)}
    )
)

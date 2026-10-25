"""Historical script: immutable synthetic held-out/adjudication package builder.

Excluded format generation (DOCX, XLSX, XLS, PDF, OCR) removed under Phase 18 Slice 4
consolidated cleanup. Preserved historical datasets under tests/fixtures/ are immutable.
"""

from __future__ import annotations

import argparse
from pathlib import Path


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--output", type=Path, required=False)
    parser.parse_args()
    print("Historical adjudication builder; excluded formats removed under P18-S4 Stage C.")


if __name__ == "__main__":
    main()

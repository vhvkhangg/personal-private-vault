"""Source/manifest integrity only; NEVER execute the new held-out parsers/OCR here."""

import hashlib
import json
from pathlib import Path

DATASET = Path(__file__).parent / "fixtures/phase18-s2-checkpoint-v3"


def test_adjudication_sources_hashes_denominators_and_closures() -> None:
    raw = (DATASET / "manifest.json").read_bytes()
    assert b"\r\n" not in raw  # Git eol normalization must not invalidate the frozen hash.
    assert hashlib.sha256(raw).hexdigest() == (DATASET / "manifest.sha256").read_text().strip()
    manifest = json.loads(raw)
    docs = manifest["documents"]
    files = {d["file"] for d in docs}
    old = json.loads((DATASET.parent / "phase18-ocr-v2/manifest.json").read_bytes())
    old_hashes = {d["sha256"] for d in old["items"]}
    old_families = {d["source_group"] for d in old["items"]}
    assert not old_families.intersection(d["source_family"] for d in docs)
    for doc in docs:
        original = (DATASET / doc["file"]).read_bytes()
        if Path(doc["file"]).suffix in {".txt", ".md", ".json"}:
            assert b"\r\n" not in original
        assert hashlib.sha256(original).hexdigest() == doc["sha256"]
        assert doc["original_digest"] == doc["sha256"]
        assert doc["sha256"] not in old_hashes
        assert doc["split"] == "held_out"
        assert set(doc["dependencies"]) <= files
        assert doc["denominators"]["blocks"] == len(doc["gold"])
        if doc["format"] in {"txt", "md/Obsidian"}:
            source = original.decode("utf-8")
            for gold in doc["gold"]:
                if "original_line_range" in gold:
                    assert gold["original_line_range"][1] <= len(source.splitlines())
                if "char_end" in gold:
                    assert source[gold["char_start"] : gold["char_end"]] == gold["text"]
        for gold in doc["gold"]:
            if "child_file" in gold:
                assert (
                    hashlib.sha256((DATASET / gold["child_file"]).read_bytes()).hexdigest()
                    == gold["child_original_digest"]
                )
    for metric, total in manifest["denominators"].items():
        assert total == sum(d["denominators"][metric] for d in docs)
    assert {"vi", "en", "zh-Hans", "zh-Hant"} == set(manifest["by_script"])
    assert {
        "md/Obsidian",
        "txt",
        "docx",
        "pdf-native",
        "pdf-scan",
        "pdf-mixed",
        "xlsx",
        "xls",
    } <= set(manifest["by_format"])


def test_no_human_or_held_out_evaluation_is_fabricated() -> None:
    manifest = json.loads((DATASET / "manifest.json").read_bytes())
    adjudication = json.loads((DATASET / "adjudication.json").read_bytes())
    assert manifest["held_out_results"] == "NOT_RUN"
    assert manifest["human_adjudication"] == "PENDING"
    assert adjudication["status"] == "PENDING"
    assert adjudication["reviewer"] is None and adjudication["signed_at"] is None
    assert (
        adjudication["dataset_sha256"]
        == hashlib.sha256((DATASET / "manifest.json").read_bytes()).hexdigest()
    )
    assert all(
        d["reviewer"] is None and d["material_error_decision"] is None
        for d in adjudication["documents"]
    )

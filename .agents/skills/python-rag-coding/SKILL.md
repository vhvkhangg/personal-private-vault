---
name: python-rag-coding
description: Implement and review typed, testable local Python/FastAPI RAG pipelines, document parsers, OCR adapters and ML runtime glue without loosening Java-owned source privacy, process or database contracts.
---

# Python RAG Coding — Phases 18–22

Use this separate Python skill rather than expanding the Java/Spring skill into an unrelated language. Invoke together with `local-rag-engineering`, `backend-testing` and `bounded-command-execution` **only when the active handoff includes Python**.

## Compatibility and environment

- Read `rag-engine/pyproject.toml`, `.python-version`, `uv.lock`, module READMEs and the phase handoff before proposing dependencies. Prefer the latest **stable mutually compatible** set that works on the owner's Windows x64, CPU/RAM/8 GB VRAM, Python ABI and local model wheels. Do not upgrade a frozen runtime or lockfile as a side effect of a feature.
- Use a project virtual environment and the existing locked install workflow; never use arbitrary global `pip install`, runtime downloads, cloud inference or unapproved network dependencies. Any version change needs an explicit phase requirement, compatibility check, lock regeneration and regression evidence. Java remains the policy/consent/authoritative source owner.
- Distinguish *latest stable language release* from *supported interpreter/toolchain*. Newly released Python majors can lack OCR/torch/onnx wheels; do not insist on them without evidence.

## Implementation conventions

- Python 3.14 currently pinned in `.python-version` for Phase 17; do not silently reinterpret `requires-python >=3.11` as proof that 3.15 or a model's native wheels work. Match Ruff/mypy targets deliberately during an authorized migration.
- Use explicit type hints and typed Pydantic DTOs at wire boundaries. Prefer small pure transformations and standard library utilities. Preserve source identifiers, hashes, provenance spans, sheet/page metadata and parser diagnostics without logging raw private content.
- Separate extraction, parsing/OCR, chunking, embedding, staging and future query/recommendation adapters by **actual** responsibility. Introduce Adapter/Strategy only for implemented alternatives; no generic plugin orchestration framework.
- Treat documents as untrusted: bounded input sizes/page counts, no macros/formula execution, safe ZIP/path/symlink handling, no remote links or automatic directory crawling, no prompt-instruction execution from documents.
- Preserve exact source-consent, categorical Private/NSFW exclusion, confined process boundary, worker-role and staging contracts inherited from frozen Phase 17. No new Python authority over business SQL or publication.
- CPU-only extraction before OCR when sound, OCR only when needed; profile actual document samples before adding model/runtime acceleration.

## Verification

- Pure parser/locator tests: focused `pytest` with synthetic sanitized fixtures; include Vietnamese/English/Chinese only where that format is in scope. Check offsets, table order, negative/cyclic references and permitted file boundary.
- Execute `ruff check`, `ruff format --check`, `mypy`, `pytest` using the pinned environment; real PostgreSQL/Java/native process tests only for changed cross-boundary behavior.
- Record test counts, wall clock, environment, actual exit, timeouts and any skips. Run one required full gate after focused convergence; preserve baseline receipts rather than repeatedly rerunning for a lucky PASS.
- Follow `bounded-command-execution` for long commands; do not weaken a failing assertion, waive a privacy rule or extend phase scope to make a test pass.

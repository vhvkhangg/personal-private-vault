---
trigger: glob
globs: "rag-engine/**/*.py, rag-engine/pyproject.toml, rag-engine/uv.lock, rag-engine/.python-version"
description: "Python/FastAPI RAG implementation requires phase scope, typed interfaces, local-only behavior, locked compatible dependencies and bounded tests."
---

# Python RAG Rule

- Read `python-rag-coding`, `local-rag-engineering` and the current accepted numbered-phase handoff.
- Java owns Vault source consent, policy and final publication. Python does not gain business schema privileges or bypass frozen Phase 17 boundaries.
- Use type hints/Pydantic for public payloads, deterministic pure transforms for parsers, sanitized fixture tests, Ruff/mypy/pytest and pinned compatible dependencies.
- No unapproved runtime network, remote models, telemetry, external source fetching, file writes to user documents, macro/formula execution or framework expansion.
- Prefer bounded commands via `scripts/run-bounded-command.py`; actual full-suite timeout allowance is 35 minutes only for Phase 18–22 and explicit full-suite commands.

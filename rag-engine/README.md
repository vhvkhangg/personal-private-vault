**D21 CURRENT — 2026-10-20: Slice 2 `.md` ONLY / `READY_FOR_INDEPENDENT_SLICE_2_CLOSEOUT_REVIEW`.** All twelve frozen D17 requirements remain mandatory and independently VERIFIED / accepted under D20. TXT, DOCX, PDF, XLS, XLSX and OCR are excluded from Slice 2 acceptance; historical code/tests/failures remain intact and reintroduction requires a future explicit Owner Decision. See [D21](../docs/implementation/phase-18/owner-decisions.md#2026-10-20--d21-slice-2-obsidian-markdown-only-scope-rebase), the [controlling scope/matrix](../docs/implementation/phase-18/slice-2-markdown-only-d21-scope-and-acceptance.md) and [proposed closeout](../docs/implementation/phase-18/slice-2-markdown-only-d21-proposed-closeout-2026-10-20.md). **Slice 2 is not declared CLOSED and owner commit readiness is not granted here.** Phase 17 FROZEN; Slice 1/D14 CLOSED; FR18-3 OPEN; external indexing INACTIVE; historical Java full gate FAILED; Slices 3/4 unauthorized. No global Phase 18 acceptance or new implementation/activation.

## Preserved prior scope/status and broader target records

# D17 selected Markdown runtime — completion checkpoint 2026-10-20

**BLOCKED / OWNER_DECISION_REQUIRED** for maximum-workload stability and remaining resource/quality
evidence. See the [corrective handback](../docs/implementation/phase-18/slice-2-obsidian-markdown-d17-corrective-handback-2026-10-20.md).
HTML and structural development tests pass; this does not accept the complete profile or Phase 18.
The paragraphs below remain historical where their older dependency/status statements differ.

Selected Windows CPython **3.14.4** environment: `.venv-d17`, 67 compatible locked distributions,
base + existing dev dependencies, html5lib 1.1/webencodings 0.6.1. Existing locked versions preserved.
From the repository root, install deliberately with:

```powershell
$env:UV_PROJECT_ENVIRONMENT = Join-Path (Get-Location).Path 'rag-engine/.venv-d17'
uv sync --directory rag-engine --locked --extra dev --python 3.14.4
uv pip check --python rag-engine/.venv-d17/Scripts/python.exe
```

This provisioning may use the network; runtime HTML/Markdown never fetches linked resources.
RapidOCR 1.4.4 remains locked in the nondefault `deferred-ocr` group constrained to Python <3.13;
requesting it on 3.14.4 intentionally fails. Old `.venv`, OCR code/artifacts and failed receipts
are preserved; no OCR experiment or compatibility claim follows. Deferred PDF raster typing permits
only missing `cv2` in the mypy project config. Use `--config-file rag-engine/pyproject.toml` when
running mypy from the repository root. Do not run deferred OCR tests as proof of Markdown acceptance.
The corrective evidence inventory records the exact selected tests and exclusions.

## Preserved earlier runtime/setup history

# PPV RAG Engine - Phase 17 frozen; Slice 2 corrective candidate blocked

**Corrective takeover status - 2026-10-20:** `P18-S2-PARSER-OCR-EMBEDDING / BLOCKED / OWNER_DECISION_REQUIRED`. One separately owner-authorized Codex corrective scope addressed S2-01 through S2-08; it does not reset historical Antigravity 2/2. Deterministic repairs are tested, but four-script OCR/fresh human-adjudicated held-out quality (S2-06), complete native parsing interruption/resource proof (S2-07), and the Java pipeline-identity compatibility gate (S2-08) remain unresolved. See [corrective handback](../docs/implementation/phase-18/slice-2-parser-ocr-embedding-codex-corrective-handback-2026-10-20.md). Phase 17 remains FROZEN; Slice 1 COMPLETE/CLOSED under D14; FR18-3 OPEN; external indexing INACTIVE, live originals DISABLED/DEFERRED; Slices 3-4 unauthorized. No Slice 2 acceptance, automatic further implementation or agent publishing. Earlier dated statuses are historical where inconsistent.

Current producer-local candidate adds whole-file external Markdown, exact revision/role chunk boundaries, DOCX hyperlinks/nested cell/comment addresses, inert XLSX formulas/merge/visibility metadata, selected PDF OCR and measured ruled-cell geometry. Legacy formulas/comments without verified representation fail the affected XLS file. RapidOCR vi/zh-Hant admission is blocked rather than warning away known material loss. Finite preflight/cancellation checks do not make native OCR calls cooperatively interruptible; existing Java process termination is unchanged. Public v2/v3 contracts, Java, SQL and dependencies are unchanged. The paragraphs below preserve earlier setup/history, not current implementation authorization.

## Preserved setup and history

# PPV RAG Engine — accepted Phase 17 Foundation (Phase 18 not implemented)

**Current authority, 2026-10-17:** Phase 17 Alternative B/E1 Foundation is **COMPLETE — FROZEN**
after the [independent final acceptance](../docs/implementation/phase-17/reviews/2026-10-17-phase-17-alternative-b-independent-final-re-review-3.md),
owner commit/push `829d294aca7d916851348ff21004c631915d572e` and
[formal closeout](../docs/implementation/phase-17/closeout.md). The former **2026-10-15
HTTP 503 / owning-domain race containment** narrative in historical
[remediation plan](../docs/implementation/phase-17/remediation-plan.md) and
[test evidence](../docs/implementation/phase-17/test-evidence.md) is a **historical interim candidate**, not
the current accepted worker capability. Current Foundation deliberately still denies **live
external-file indexing** pending independently proven native Windows/NTFS R7 and an approved active
Phase 18 implementation handoff. Owner published accepted preparation as `435ffea`; [ACTIVE](../docs/implementation/handoffs/ACTIVE.md) now holds **P18-DOCUMENT-INDEXING / CHANGES_REQUESTED**. The current handoff is STOPPED; no runtime tests/experiments or live/snapshot ingestion are authorized by the documentation synchronization.

The owner-approved D18-A design in [ADR-0021](../docs/adr/0021-phase-18-bounded-multi-file-verified-indexing.md) was accepted and published before the separate handoff authorized bounded Phase 18 implementation. It did not itself implement a new worker protocol, live input, parser/OCR, chunk/vector storage or Phase 19 QA. At handoff creation, the
worker processes authorized **synthetic digest receipts only** under Java source authority, one-use
grant, native supervision, SQL staging and Java-only publication; Python cannot authorize, grant,
read business/Vault SQL, release reservations or publish. No document parser, OCR, model,
embedding, answer, recommendation or Ideaverse capability is enabled at handoff creation. The owner subsequently approved [ADR-0022](../docs/adr/0022-phase-18-immutable-snapshot-whole-file-consent-no-change.md) and one documentation-only synchronization: whole-file exact ORIGINAL_BYTES snapshots and guarded SHA-256/length NO_CHANGE are binding **unimplemented targets**. Execution is STOPPED / INCOMPLETE / NOT_VERIFIED, G1–G4/FR18-1–11 OPEN. Live originals DISABLED / DEFERRED; snapshots inactive. Next: independent documentation synchronization review. No code/experiment/worker/ingestion authorization; no runtime success is asserted. Preserve existing Foundation setup/commands below.

## Locked setup and checks

Installation may use the network deliberately. It is not an offline-runtime proof.

```powershell
cd rag-engine
uv sync --frozen --extra dev
.venv/Scripts/python.exe -m pytest -q
.venv/Scripts/python.exe -m ruff check .
.venv/Scripts/python.exe -m ruff format --check .
.venv/Scripts/python.exe -m mypy src/ppv_rag
```

`.python-version` pins measured CPython 3.14.4. `uv.lock` fixes direct/transitive dependencies and artifact hashes; upgrades require deliberate lock regeneration and review. Runtime dependencies: FastAPI 0.143.0, Uvicorn 0.54.0, Pydantic 2.14.1 and Psycopg/binary 3.3.6. The installed Windows environment contains 32 distributions; its libpq is 18.4 (client library, separate from the tested PostgreSQL 18.6 server). No new Java dependency is introduced.

## Local service

Provision the database using the [separate migrator instructions](../backend/dev/rag-db/README.md). Supply independent credentials through the environment: `PPV_RAG_SERVICE_KEY` (at least 32 characters) and `PPV_RAG_WORKER_DSN` (the provisioned `rag_worker` login on literal `127.0.0.1` or `::1`). Never use the migrator/runtime credential for the worker. There is no embedded default service key or worker password. Do not log environment values.

```powershell
.venv/Scripts/python.exe -m uvicorn ppv_rag.main:app --app-dir src --host 127.0.0.1 --port 8001 --no-access-log
```

Health is content-free. Execution, metrics, cancellation and quiescence require `X-RAG-Service-Key`. Unknown/unavailable cleanup is unverified. One execution, bounded raw bodies/UTF-8 payloads, statement/lock/transaction deadlines and bounded cancellation history are enforced. SQL permissions—not the service credential alone—prove actual issuance and durable staging. Python returns success only after physical COMMIT.

## OS-enforced offline verification

From the repository root:

```powershell
docker build -f rag-engine/Dockerfile.offline-check -t ppv-rag-offline-check:3.14.4 rag-engine
docker run --rm --network none --read-only --tmpfs /tmp:size=16m --cap-drop ALL --security-opt no-new-privileges ppv-rag-offline-check:3.14.4
```

The verification image pins the Python base digest and installs the frozen runtime lock at build time. Its real checks deny IPv4, IPv6, external DNS and proxy egress, run the service on allowed loopback, and prove unavailable DB authority rejects work. It has no persistent model/cache or content scratch capability. This is a Linux Docker namespace proof; it does not configure or certify a Windows host firewall. Real Java→native Python→restricted PostgreSQL execution is independently tested by `RagWorkerBoundaryIntegrationTest`. A combined native Windows firewall/deployment guarantee remains outside these receipts.

`scripts/dependency_inventory.py` records installed license/version metadata. A bounded pip-audit 2.9.0 scan of all 32 installed distributions returned zero known advisories on 2026-10-14. This does not assert unknown vulnerabilities or every bundled OS/DLL component is absent. Preserve LGPL Psycopg notices; private local use is the current scope, not a redistribution approval. See [historical Phase 17 evidence](../docs/implementation/phase-17/test-evidence.md) and [final acceptance](../docs/implementation/phase-17/reviews/2026-10-17-phase-17-alternative-b-independent-final-re-review-3.md) for actual scope, results and limits.

## Phase 18 takeover candidate — not accepted

**D17 current checkpoint (2026-10-20): BLOCKED / OWNER_DECISION_REQUIRED.** The active scope is
the [frozen Obsidian Markdown profile](../docs/implementation/phase-18/obsidian-markdown-d17-contract.md).
The whole-file Markdown path uses pinned markdown-it-py 4.2.0, mdit-py-plugins 0.6.1 and mdurl 0.1.2;
existing package versions, Python and model artifacts are unchanged. Structure/source spans and
typed whole-document failures have new producer-local regression evidence. Mandatory HTML shapes
remain unsupported, and `uv pip check` fails the inherited RapidOCR/Python metadata constraint.
See the [D17 handback](../docs/implementation/phase-18/slice-2-obsidian-markdown-d17-handback-2026-10-20.md).
OCR experiments are stopped; OCR/PDF/DOCX/XLS/XLSX acceptance is deferred. The preceding Phase 17 and
following broader takeover/OCR text is historical where superseded; it does not authorize replaying
OCR experiments. External indexing stays inactive; no broader Slice 2 or publication acceptance.

The Phase 17 distribution counts and dependency/audit statements above are historical.
The current locked candidate additionally uses pdfplumber 0.11.10 (with PDFMiner and
PDFium) for original-page native geometry, native table cells and rendered image
regions. Scanned table cell structure and Vietnamese OCR remain unverified; detected
raster grids fail the file. The selected embedding artifact and CPython pin are unchanged.
Model metadata records the actual tokenizer/parser/chunker/OCR pipeline identity;
the worker refuses oversized tokenizer input instead of truncating it. Chunk overlap
is zero. Host/device snapshots are not process-tree/GPU peak acceptance evidence.

From repository root, `scripts/probe_phase18_ocr.py` beneath `rag-engine/` replays
the hashed synthetic PDF diagnostic and writes a unique ignored report. Dataset v2
has already been exposed during diagnosis and cannot establish fresh independent
held-out acceptance. The explicit Latin OCR provisioning script is a failed-candidate
experiment only; neither tested alternative replaced the selected runtime OCR.
All normal live-file registration and ingestion remain disabled.

See [takeover evidence](../docs/implementation/phase-18/test-evidence.md) and
[ACTIVE](../docs/implementation/handoffs/ACTIVE.md) for unresolved source/producer,
R7, quality and resource requirements. A separate independent review is required.

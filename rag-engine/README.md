# PPV RAG Engine — Phase 17 foundation candidate

**2026-10-15 containment:** `/v1/worker/execute` returns content-free HTTP 503
before reading the body. Java also blocks issuance and content dispatch. The
post-issuance owning-domain race remains unresolved pending an explicit P17-3
protocol decision; health/cancellation/cleanup remain available. The successful
workflow descriptions and receipts below describe the previous candidate, not
the currently disabled execution capability. See the [current plan](../docs/implementation/phase-17/remediation-plan.md)
and [current evidence](../docs/implementation/phase-17/test-evidence.md).

This worker handles controlled synthetic digests only. Java owns authorization and publication; Python verifies a short-lived issued grant and commits a typed receipt through restricted PostgreSQL functions. No parser, model, embedding, answer, recommendation or Ideaverse capability is enabled. Live external-file sources are disabled.

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

`scripts/dependency_inventory.py` records installed license/version metadata. A bounded pip-audit 2.9.0 scan of all 32 installed distributions returned zero known advisories on 2026-10-14. This does not assert unknown vulnerabilities or every bundled OS/DLL component is absent. Preserve LGPL Psycopg notices; private local use is the current scope, not a redistribution approval. See [current evidence](../docs/implementation/phase-17/test-evidence.md) for exact results and limits.

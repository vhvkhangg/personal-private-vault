"""Real ASGI + restricted PostgreSQL boundary probe on an ISOLATED V15 test DB.

Requires PPV_RAG_TEST_ADMIN_DSN and PPV_RAG_WORKER_DSN. Run with
--isolated-test-database. No mocks, skips, external requests or native/Java proof.
The caller must destroy the disposable database after retaining source-free results.
"""

import argparse
import asyncio
import hashlib
import json
import os
import socket
import sys
import urllib.error
import urllib.request
from datetime import datetime, timezone
from uuid import uuid4

import psycopg
from psycopg.types.json import Jsonb
from starlette.types import Message, Scope

parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument("--isolated-test-database", action="store_true", required=True)
parser.add_argument("--deny-external", action="store_true")
arguments = parser.parse_args()
admin_dsn = os.environ["PPV_RAG_TEST_ADMIN_DSN"]
worker_dsn = os.environ["PPV_RAG_WORKER_DSN"]
for fixture_dsn in (admin_dsn, worker_dsn):
    address = psycopg.conninfo.conninfo_to_dict(fixture_dsn)
    assert address.get("host") in {"127.0.0.1", "::1"} and not address.get("service"), (
        "The standalone probe requires exact loopback test database addresses"
    )
    assert not address.get("hostaddr") or address["hostaddr"] == address["host"]

blocked_events: dict[str, int] = {}


def deny_external(event: str, args: tuple[object, ...]) -> None:
    """Process-local Python instrumentation, explicitly NOT an OS/native fence."""
    if event == "socket.getaddrinfo":
        target = args[0]
    elif event in {"socket.connect", "socket.bind", "socket.sendto"}:
        destination = args[-1]
        target = destination[0] if isinstance(destination, tuple) else destination
    else:
        return
    if target not in {"127.0.0.1", "::1", b"127.0.0.1", b"::1"}:
        blocked_events[event] = blocked_events.get(event, 0) + 1
        raise PermissionError("External Python socket operation denied by isolated test control")


if arguments.deny_external:
    sys.addaudithook(deny_external)
    for family, destination in (
        (socket.AF_INET, ("192.0.2.1", 443)),
        (socket.AF_INET6, ("2001:db8::1", 443)),
    ):
        with socket.socket(family, socket.SOCK_STREAM) as probe:
            try:
                probe.connect(destination)
            except PermissionError:
                pass
            else:
                raise AssertionError("External socket was not denied before the OS call")
    try:
        socket.getaddrinfo("example.invalid", 443)
    except PermissionError:
        pass
    else:
        raise AssertionError("External DNS operation escaped instrumentation")
    proxy = urllib.request.build_opener(
        urllib.request.ProxyHandler({"http": "http://192.0.2.1:3128"})
    )
    try:
        proxy.open("http://example.invalid", timeout=1)
    except (PermissionError, urllib.error.URLError):
        pass
    else:
        raise AssertionError("Proxy request escaped instrumentation")
    assert blocked_events.get("socket.connect", 0) >= 2
    assert blocked_events.get("socket.getaddrinfo", 0) >= 2
job, attempt, session, runtime_id, nonce = (str(uuid4()) for _ in range(5))
service_key = "synthetic-isolated-worker-boundary-service-key"
token = "d" * 64
os.environ.update(
    PPV_RAG_SERVICE_KEY=service_key,
    PPV_RAG_TEST_SCOPED_EXECUTION="true",
    PPV_RAG_JOB_ID=job,
    PPV_RAG_ATTEMPT_ID=attempt,
    PPV_RAG_ENDPOINT_NONCE=nonce,
    PPV_RAG_HOST="127.0.0.1",
)

# Environment binding precedes actual production-app imports; no replacement of
# authentication, settings, claim function, database or receipt computation.
from ppv_rag.db import connection  # noqa: E402
from ppv_rag.main import app  # noqa: E402

results: dict[str, object] = {}
owner_id = 930001 + int(uuid4().hex[:8], 16)
payload = "Synthetic approved boundary source"
content_hash = hashlib.sha256(payload.encode()).hexdigest()
input_digest = hashlib.sha256(payload.encode()).hexdigest()
output_digest = hashlib.sha256(f"SYNTHETIC_TEST:{payload}".encode()).hexdigest()
started = datetime.now(timezone.utc).isoformat()


def admin() -> psycopg.Connection[tuple[object, ...]]:
    return psycopg.connect(
        admin_dsn,
        connect_timeout=3,
        options="-c statement_timeout=5000 -c lock_timeout=3000 -c transaction_timeout=10000",
        hostaddr=psycopg.conninfo.conninfo_to_dict(admin_dsn)["host"],
        sslmode="disable",
        gssencmode="disable",
    )


with admin() as conn:
    assert conn.execute(
        "SELECT count(*) FROM rag.job_leases WHERE status NOT IN "
        "('COMPLETED','CANCELLED','ABORTED','PURGED')"
    ).fetchone() == (0,), "Probe requires an isolated database without unfinished jobs"
    conn.execute("INSERT INTO public.vault_entries(id,entry_type) VALUES(%s,'FILM')", (owner_id,))
    conn.execute(
        "INSERT INTO public.films(id,title,format,production_style) "
        "VALUES(%s,%s,'MOVIE','ANIMATION')",
        (owner_id, payload),
    )
    state = conn.execute(
        "SELECT source_version,consent_reset_version FROM public.vault_rag_source_state "
        "WHERE source_id=%s",
        (owner_id,),
    ).fetchone()
    assert state is not None
    inserted = conn.execute(
        "INSERT INTO rag.sources(source_type,source_id,classification,access_state,"
        "source_revision_hash,description_revision_hash,approved_fields_text,"
        "approved_owner_reset_version,created_at,updated_at) "
        "VALUES('FILM',%s,'NON_PRIVATE','APPROVED',%s,'','title',%s,"
        "clock_timestamp(),clock_timestamp()) "
        "RETURNING id,authorization_epoch,field_policy_version",
        (str(owner_id), content_hash, state[1]),
    ).fetchone()
    assert inserted is not None
    source_id = inserted[0]
    manifest = json.dumps(
        {
            "contract_version": "2.0",
            "root_id": source_id,
            "nodes": [
                {
                    "source_id": source_id,
                    "source_type": "FILM",
                    "domain_id": str(owner_id),
                    "epoch": inserted[1],
                    "field_version": inserted[2],
                    "fields": ["title"],
                    "content_revision": content_hash,
                    "description_revision": "",
                    "owner_source_version": state[0],
                    "owner_reset_version": state[1],
                }
            ],
            "edges": [],
        },
        separators=(",", ":"),
    )
    scope_digest = hashlib.sha256(manifest.encode()).hexdigest()
    conn.execute(
        "INSERT INTO rag.runtime_state VALUES(true,%s) "
        "ON CONFLICT(singleton) DO UPDATE SET current_instance=excluded.current_instance",
        (runtime_id,),
    )
    conn.execute("SELECT public.vault_rag_reserve(ARRAY[%s]::bigint[],%s)", (owner_id, attempt))
    conn.execute(
        "INSERT INTO rag.job_leases(job_id,attempt_id,source_id,status,lease_token,leased_until,"
        "created_at,updated_at,reservation_token,controller_identity,worker_identity,"
        "dependency_manifest) "
        "VALUES(%s,%s,%s,'ACTIVE',%s,clock_timestamp()+interval '5 minutes',clock_timestamp(),"
        "clock_timestamp(),%s,%s,%s,%s)",
        (
            job,
            attempt,
            source_id,
            token,
            attempt,
            Jsonb({"process_id": os.getpid(), "started_at": started, "runtime_id": runtime_id}),
            Jsonb({"process_id": os.getpid(), "started_at": started, "endpoint_nonce": nonce}),
            manifest,
        ),
    )
    conn.execute(
        "INSERT INTO rag.worker_grants(job_id,attempt_id,session_id,session_until,input_digest,"
        "scope_digest,output_digest,stage_type) "
        "VALUES(%s,%s,%s,clock_timestamp()+interval '5 minutes',"
        "%s,%s,%s,'SYNTHETIC_TEST')",
        (job, attempt, session, input_digest, scope_digest, output_digest),
    )
results["known_admission_commit"] = True

try:
    with connection() as conn:
        assert conn.execute("SELECT session_user").fetchone() == ("rag_worker",)
    results["actual_restricted_role_connection"] = True
    for forbidden in (
        "SELECT * FROM public.films",
        "SELECT * FROM public.vault_rag_source_state",
        "UPDATE rag.worker_grants SET consumed_at=NULL",
        "UPDATE rag.job_leases SET staging_closed=true",
        "CREATE TABLE rag.unauthorized_probe(id int)",
    ):
        try:
            with connection() as conn:
                conn.execute(forbidden)
        except psycopg.errors.InsufficientPrivilege:
            pass
        else:
            raise AssertionError("Forbidden worker SQL succeeded")
    results["worker_owner_policy_job_ddl_denials"] = 5

    body = json.dumps(
        {
            "contract_version": "2.0",
            "job_id": job,
            "attempt_id": attempt,
            "lease_token": token,
            "stage_type": "SYNTHETIC_TEST",
            "payload": payload,
            "input_digest": input_digest,
            "scope_digest": scope_digest,
        }
    ).encode()
    headers = {
        "content-type": "application/json",
        "x-rag-service-key": service_key,
        "x-rag-job-id": job,
        "x-rag-attempt-id": attempt,
        "x-rag-lease-token": token,
        "x-rag-session-id": session,
        "x-rag-endpoint-nonce": nonce,
    }
    receives = 0

    async def invoke(actual_headers: dict[str, str], allow_body: bool) -> list[Message]:
        sent: list[Message] = []
        scope: Scope = {
            "type": "http",
            "asgi": {"version": "3.0"},
            "method": "POST",
            "path": "/v1/worker/execute",
            "raw_path": b"/v1/worker/execute",
            "query_string": b"",
            "root_path": "",
            "scheme": "http",
            "http_version": "1.1",
            "client": ("127.0.0.1", 12345),
            "server": ("127.0.0.1", 8001),
            "headers": [(k.encode(), v.encode()) for k, v in actual_headers.items()],
        }

        async def receive() -> Message:
            global receives
            assert allow_body, "Denied/replayed grant consumed a body chunk"
            # A separate real connection observes only committed state. The claim
            # cannot merely have executed inside an outstanding worker transaction.
            with admin() as observer:
                assert observer.execute(
                    "SELECT consumed_at IS NOT NULL FROM rag.worker_grants "
                    "WHERE job_id=%s AND attempt_id=%s",
                    (job, attempt),
                ).fetchone() == (True,)
            receives += 1
            return {"type": "http.request", "body": body, "more_body": False}

        async def send(message: Message) -> None:
            sent.append(message)

        await app(scope, receive, send)
        return sent

    wrong = asyncio.run(invoke({**headers, "x-rag-job-id": str(uuid4())}, False))
    assert wrong[0]["status"] == 403 and receives == 0
    results["cross_job_denial_zero_body_receives"] = True
    success = asyncio.run(invoke(headers, True))
    assert success[0]["status"] == 200
    response = json.loads(success[1]["body"])
    assert response["status"] == "STAGED" and receives == 1
    assert response["receipt"]["output_digest"] == output_digest
    with admin() as observer:
        assert observer.execute(
            "SELECT worker_receipt,payload::jsonb->>'output_digest' "
            "FROM rag.staged_outputs WHERE job_id=%s AND attempt_id=%s",
            (job, attempt),
        ).fetchone() == (True, output_digest)
    results["claim_commit_before_first_receive"] = True
    results["actual_typed_receipt_commit_and_provenance"] = True
    duplicate = asyncio.run(invoke(headers, False))
    assert duplicate[0]["status"] == 403 and receives == 1
    results["duplicate_claim_denial_zero_additional_receives"] = True
    results["native_java_http_os_egress_proof"] = "NOT_VERIFIED_BY_THIS_PROBE"
    results["python_external_socket_test_control"] = arguments.deny_external
    results["external_python_operations_blocked"] = blocked_events
    results["native_libpq_egress_interception"] = "NOT_PROVIDED_BY_PYTHON_AUDIT_HOOKS"
    print(json.dumps(results, sort_keys=True))
finally:
    # This probe's sole consumer is this now-awaited in-process ASGI call. This
    # cleanup is not a surrogate for Java/native worker termination/publication.
    with admin() as conn:
        conn.execute("DELETE FROM rag.staged_outputs WHERE job_id=%s", (job,))
        conn.execute(
            "UPDATE rag.job_leases SET staging_closed=true,status='ABORTED' "
            "WHERE job_id=%s AND attempt_id=%s",
            (job, attempt),
        )
        conn.execute("SELECT public.vault_rag_release(ARRAY[%s]::bigint[],%s)", (owner_id, attempt))

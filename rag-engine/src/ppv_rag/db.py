"""Only audited grant and receipt functions are available to the real worker role."""

import psycopg

from ppv_rag.config import settings
from ppv_rag.contracts import WorkerReceipt, WorkerStageRequest


class AuthorityUnavailable(RuntimeError):
    pass


def connection() -> psycopg.Connection[tuple[object, ...]]:
    if not settings.worker_dsn:
        raise AuthorityUnavailable("Worker database authority unavailable")
    address = psycopg.conninfo.conninfo_to_dict(settings.worker_dsn)
    if address.get("host") not in {"127.0.0.1", "::1"} or address.get("service"):
        raise AuthorityUnavailable("Worker database must be an exact loopback address")
    if address.get("hostaddr") and address["hostaddr"] != address["host"]:
        raise AuthorityUnavailable("Worker database address mismatch")
    conn = psycopg.connect(
        settings.worker_dsn,
        connect_timeout=3,
        options="-c statement_timeout=5000 -c lock_timeout=3000 -c transaction_timeout=10000",
        hostaddr=address["host"],
        sslmode="disable",
        gssencmode="disable",
    )
    try:
        row = conn.execute("""SELECT session_user,rolsuper,rolcreatedb,rolcreaterole,
            rolinherit,rolbypassrls,rolreplication,
            EXISTS(SELECT 1 FROM pg_catalog.pg_auth_members WHERE member=r.oid),
            has_database_privilege(session_user,current_database(),'CREATE')
              OR has_database_privilege(session_user,current_database(),'TEMP')
            FROM pg_catalog.pg_roles r WHERE rolname=session_user""").fetchone()
        if row != ("rag_worker", False, False, False, False, False, False, False, False):
            raise AuthorityUnavailable("Unsafe worker role")
        return conn
    except BaseException:
        conn.close()
        raise


def verify_grant(request: WorkerStageRequest) -> None:
    with connection() as conn:
        row = conn.execute(
            "SELECT rag.verify_worker_grant(%s,%s,%s,%s,%s,%s)",
            (
                request.job_id,
                request.attempt_id,
                request.lease_token,
                request.stage_type,
                request.input_digest,
                request.scope_digest,
            ),
        ).fetchone()
        if row is None or row[0] is not True:
            raise AuthorityUnavailable("Worker grant denied")


def claim_input(job_id: str, attempt_id: str, lease_token: str, session_id: str) -> None:
    """Burn the single-use input claim and COMMIT before any request body is read."""
    with connection() as conn:
        row = conn.execute(
            "SELECT rag.claim_worker_input(%s,%s,%s,%s)",
            (job_id, attempt_id, lease_token, session_id),
        ).fetchone()
        if row is None or row[0] is not True:
            raise AuthorityUnavailable("Worker input claim denied")


def stage_receipt(request: WorkerStageRequest, receipt: WorkerReceipt) -> int:
    with connection() as conn:
        row = conn.execute(
            "SELECT rag.stage_worker_receipt(%s,%s,%s,%s)",
            (request.job_id, request.attempt_id, request.lease_token, receipt.model_dump_json()),
        ).fetchone()
        if row is None or not isinstance(row[0], int) or row[0] <= 0:
            raise AuthorityUnavailable("Worker stage denied")
        staged_id = row[0]
    # Exiting the connection context performs physical COMMIT before success escapes.
    return staged_id


def stage_document_result(
    job_id: str,
    attempt_id: str,
    lease_token: str,
    root_key: str,
    success: bool,
    reason_code: str,
    warning_codes: list[str],
    chunks_payload: list[dict[str, object]],
    resource_report: dict[str, object],
) -> int:
    import json

    with connection() as conn:
        row = conn.execute(
            "SELECT rag.stage_document_result(%s,%s,%s,%s,%s,%s,%s,%s,%s)",
            (
                job_id,
                attempt_id,
                lease_token,
                root_key,
                success,
                reason_code,
                json.dumps(warning_codes),
                json.dumps(chunks_payload),
                json.dumps(resource_report),
            ),
        ).fetchone()
        if row is None or not isinstance(row[0], int) or row[0] <= 0:
            raise AuthorityUnavailable("Document stage denied")
        staged_id = row[0]
    return staged_id

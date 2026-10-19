"""Offline ASGI transport enforcing service authentication, ingress byte budget, and input claim."""

from __future__ import annotations

import asyncio
import hmac
import json
from threading import Semaphore
from typing import Any
from uuid import UUID

from starlette.types import ASGIApp, Receive, Scope, Send

from ppv_rag.config import settings
from ppv_rag.db import claim_input


def unique_object(pairs: list[tuple[str, Any]]) -> dict[str, Any]:
    obj: dict[str, Any] = {}
    for key, value in pairs:
        if key in obj:
            raise ValueError(f"Duplicate JSON property: {key}")
        obj[key] = value
    return obj


class BoundWorkerTransport:
    """Offline ASGI middleware.

    Enforces loopback binding, service key authentication, and an audited
    single-use database input claim for execution requests before reading request bodies.
    """

    def __init__(self, app: ASGIApp) -> None:
        self.app = app
        self.slot = Semaphore(1)

    async def __call__(self, scope: Scope, receive: Receive, send: Send) -> None:
        if scope["type"] != "http" or scope.get("method") != "POST":
            await self.app(scope, receive, send)
            return

        path = scope.get("path", "")
        execution = path in ("/v1/worker/execute", "/v1/worker/execute-batch")

        if execution and not settings.bound_execution_enabled():
            # The owner permits isolated test-scoped enablement only. An existing
            # daemon, incomplete binding or ordinary startup remains contained.
            await self.error(send, 503, "Worker content execution is disabled")
            return

        headers: dict[bytes, bytes] = {}
        for name, value in scope.get("headers", []):
            name = name.lower()
            if name in headers:
                await self.error(send, 401, "Service authentication denied")
                return
            headers[name] = value

        if len(settings.service_key) < 32 or not hmac.compare_digest(
            headers.get(b"x-rag-service-key", b""), settings.service_key.encode()
        ):
            await self.error(send, 401, "Service authentication denied")
            return

        acquired = not execution or self.slot.acquire(blocking=False)
        if not acquired:
            await self.error(send, 429, "Worker busy")
            return

        limit = 33554432 if path == "/v1/worker/execute-batch" else (2105344 if execution else 2048)
        try:
            if execution:
                try:
                    identity = self.execution_identity(headers)
                    # This audited, restricted-role transaction holds the attempt
                    # fence and commits a one-use marker before the first receive.
                    # An invalid/partial body burns the claim: never auto-resend.
                    await asyncio.to_thread(claim_input, *identity)
                except Exception:
                    await self.error(send, 403, "Worker input authority denied")
                    return

            body = bytearray()
            async with asyncio.timeout(10):
                more = True
                while more:
                    message = await receive()
                    if message["type"] == "http.disconnect":
                        return
                    body.extend(message.get("body", b""))
                    if len(body) > limit:
                        await self.error(send, 413, "Request byte budget exceeded")
                        return
                    more = message.get("more_body", False)

            try:
                payload = (
                    json.loads(body.decode("utf-8"), object_pairs_hook=unique_object)
                    if body
                    else {}
                )
                if execution and (
                    not isinstance(payload, dict)
                    or payload.get("job_id") != identity[0]
                    or payload.get("attempt_id") != identity[1]
                    or payload.get("lease_token") != identity[2]
                ):
                    raise ValueError("Input identity mismatch")
            except (ValueError, UnicodeError, RecursionError):
                await self.error(send, 422, "Invalid worker contract")
                return

            consumed = False

            async def cached_receive() -> dict[str, Any]:
                nonlocal consumed
                if not consumed:
                    consumed = True
                    return {"type": "http.request", "body": bytes(body), "more_body": False}
                return {"type": "http.disconnect"}

            await self.app(scope, cached_receive, send)
        except TimeoutError:
            await self.error(send, 408, "Request deadline exceeded")
        finally:
            if execution:
                self.slot.release()

    @staticmethod
    def execution_identity(headers: dict[bytes, bytes]) -> tuple[str, str, str, str]:
        if (
            b"x-rag-job-id" not in headers
            or b"x-rag-attempt-id" not in headers
            or b"x-rag-lease-token" not in headers
            or b"x-rag-session-id" not in headers
            or b"x-rag-endpoint-nonce" not in headers
        ):
            raise ValueError("Missing mandatory worker execution headers")

        job = headers[b"x-rag-job-id"].decode("ascii")
        attempt = headers[b"x-rag-attempt-id"].decode("ascii")
        token = headers[b"x-rag-lease-token"].decode("ascii")
        session = headers[b"x-rag-session-id"].decode("ascii")
        nonce = headers[b"x-rag-endpoint-nonce"]
        if (
            job != settings.job_id
            or attempt != settings.attempt_id
            or str(UUID(job)) != job
            or str(UUID(attempt)) != attempt
            or str(UUID(session)) != session
            or not 64 <= len(token) <= 128
            or not hmac.compare_digest(nonce, settings.endpoint_nonce.encode())
        ):
            raise ValueError("Worker endpoint binding denied")
        return job, attempt, token, session

    @staticmethod
    async def error(send: Send, code: int, message: str) -> None:
        body = json.dumps({"detail": message}).encode()
        await send(
            {
                "type": "http.response.start",
                "status": code,
                "headers": [(b"content-type", b"application/json")],
            }
        )
        await send({"type": "http.response.body", "body": body})


BoundedWorkerRequests = BoundWorkerTransport

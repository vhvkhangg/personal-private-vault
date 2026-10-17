"""Temporary denial proof, not an enabled worker authorization regression suite."""

import asyncio

from starlette.types import Message, Scope

from ppv_rag.transport import BoundedWorkerRequests


def test_execution_denies_before_reading_even_one_request_body_chunk() -> None:
    sent: list[Message] = []

    async def receive() -> Message:
        raise AssertionError("Disabled execution must not consume request content")

    async def send(message: Message) -> None:
        sent.append(message)

    async def downstream(scope: Scope, receive: object, send: object) -> None:
        raise AssertionError("Disabled execution must not enter the worker")

    scope: Scope = {"type": "http", "method": "POST", "path": "/v1/worker/execute"}
    asyncio.run(BoundedWorkerRequests(downstream)(scope, receive, send))
    assert sent[0]["status"] == 503
    assert sent[1]["body"] == b'{"detail": "Worker content execution is disabled"}'

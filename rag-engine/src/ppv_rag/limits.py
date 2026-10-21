"""Finite producer-local admission checks; Java retains process termination authority."""

from __future__ import annotations

import time
from collections.abc import Callable, Iterator
from contextlib import contextmanager
from contextvars import ContextVar

MAX_INPUT_BYTES = 10 * 1024 * 1024
MAX_EXPANDED_BYTES = 32 * 1024 * 1024
MAX_MEMBER_BYTES = 8 * 1024 * 1024
MAX_TEXT_CHARS = 1_000_000
MAX_CELLS = 100_000
MAX_RECTANGULAR_CELLS = 1_000_000
MAX_RASTER_PIXELS = 16_000_000
MAX_PAGES = 100

_control: ContextVar[tuple[float, Callable[[], bool]] | None] = ContextVar(
    "parser_control", default=None
)


class ParsingCancelled(RuntimeError):
    """Cooperative cancellation; native calls still require owner process supervision."""


def checkpoint() -> None:
    control = _control.get()
    if control is not None:
        deadline, cancelled = control
        if cancelled():
            raise ParsingCancelled("PARSING_CANCELLED")
        if time.monotonic() >= deadline:
            raise BufferError("RESOURCE_LIMIT_EXCEEDED: parsing deadline")


@contextmanager
def parsing_scope(
    cancelled: Callable[[], bool] = lambda: False, seconds: float = 60
) -> Iterator[None]:
    if not 0 < seconds <= 120:
        raise ValueError("Invalid parsing deadline")
    token = _control.set((time.monotonic() + seconds, cancelled))
    try:
        checkpoint()
        yield
        checkpoint()
    finally:
        _control.reset(token)


def check_text(text: str) -> None:
    checkpoint()
    if len(text) > MAX_TEXT_CHARS:
        raise BufferError("RESOURCE_LIMIT_EXCEEDED: expanded text")

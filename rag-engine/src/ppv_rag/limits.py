"""Finite producer-local admission checks; Java retains process termination authority."""

from __future__ import annotations

import io
import re
import time
import zipfile
import zlib
from collections.abc import Callable, Iterator
from contextlib import contextmanager
from contextvars import ContextVar
from typing import BinaryIO
from xml.parsers import expat

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


def input_bytes(value: bytes | BinaryIO) -> bytes:
    checkpoint()
    data = value if isinstance(value, bytes) else value.read(MAX_INPUT_BYTES + 1)
    if len(data) > MAX_INPUT_BYTES:
        raise BufferError("RESOURCE_LIMIT_EXCEEDED: input bytes")
    return data


def check_text(text: str) -> None:
    checkpoint()
    if len(text) > MAX_TEXT_CHARS:
        raise BufferError("RESOURCE_LIMIT_EXCEEDED: expanded text")


def checked_zip(value: bytes | BinaryIO) -> bytes:
    """Verify declared AND streamed inflation before library object allocation. No disk writes."""
    data = input_bytes(value)
    xml_nodes = 0

    def reject_xml_declaration(*_args: object) -> None:
        raise ValueError("Unsupported XML declaration")

    with zipfile.ZipFile(io.BytesIO(data)) as archive:
        members = archive.infolist()
        if len(members) > 4096 or sum(m.file_size for m in members) > MAX_EXPANDED_BYTES:
            raise BufferError("RESOURCE_LIMIT_EXCEEDED: archive expansion")
        total = 0
        for member in members:
            checkpoint()
            if (
                member.file_size > MAX_MEMBER_BYTES
                or member.file_size > max(1, member.compress_size) * 200
            ):
                raise BufferError("RESOURCE_LIMIT_EXCEEDED: archive member")
            # All members remain in-memory. Reject external/path-like package names anyway.
            if member.filename.startswith(("/", "\\")) or ".." in member.filename.split("/"):
                raise ValueError("Invalid package member")
            with archive.open(member) as stream:
                size = 0
                tail = b""
                xml_parser = None
                depth = 0
                if member.filename.endswith((".xml", ".rels")):
                    xml_parser = expat.ParserCreate()

                    def start_element(_name: str, _attrs: dict[str, str]) -> None:
                        nonlocal xml_nodes, depth
                        xml_nodes += 1
                        depth += 1
                        if xml_nodes > 250000 or depth > 128:
                            raise BufferError("RESOURCE_LIMIT_EXCEEDED: XML structure")

                    def end_element(_name: str) -> None:
                        nonlocal depth
                        depth -= 1

                    xml_parser.StartElementHandler = start_element
                    xml_parser.EndElementHandler = end_element
                    xml_parser.StartDoctypeDeclHandler = reject_xml_declaration
                    xml_parser.EntityDeclHandler = reject_xml_declaration
                while block := stream.read(65536):
                    checkpoint()
                    if member.filename.endswith((".xml", ".rels")):
                        fragment = tail + block
                        if b"<!DOCTYPE" in fragment or b"<!ENTITY" in fragment:
                            raise ValueError("Unsupported XML declaration")
                        tail = fragment[-16:]
                        assert xml_parser is not None
                        xml_parser.Parse(block, False)
                    size += len(block)
                    total += len(block)
                    if size > MAX_MEMBER_BYTES or total > MAX_EXPANDED_BYTES:
                        raise BufferError("RESOURCE_LIMIT_EXCEEDED: archive inflation")
                if xml_parser is not None:
                    xml_parser.Parse(b"", True)
    return data


def checked_pdf(value: bytes | BinaryIO) -> bytes:
    """Conservative stream admission before PDFMiner/PDFium decompression.

    Complex filter chains and ambiguous stream boundaries fail closed. This is a
    finite supported subset, not a general PDF sanitiser or native allocation limit.
    """
    data = input_bytes(value)
    offset = total = streams = raster_pixels = 0
    while (start := data.find(b"stream", offset)) >= 0:
        checkpoint()
        header_start = data.rfind(b"obj", offset, start)
        if header_start < 0:
            raise ValueError("Ambiguous PDF stream")
        header = data[header_start:start]
        if b"#" in header:
            raise ValueError("Unsupported escaped PDF stream names")
        filters = re.search(rb"/Filter\s*(\[[^\]]*\]|/\w+)", header)
        names = re.findall(rb"/\w+", filters.group(1)) if filters else []
        if names and (
            len(names) != 1
            or names[0] not in {b"/FlateDecode", b"/DCTDecode", b"/JPXDecode", b"/CCITTFaxDecode"}
        ):
            raise ValueError("Unsupported PDF stream filter")
        end = data.find(b"endstream", start + 6)
        if end < 0:
            raise ValueError("Truncated PDF stream")
        streams += 1
        if streams > 4096:
            raise BufferError("RESOURCE_LIMIT_EXCEEDED: PDF streams")
        if b"/FlateDecode" in header:
            # A multi-stage filter could conceal expansion from this check.
            if not filters or re.findall(rb"/\w+", filters.group(1)) != [b"/FlateDecode"]:
                raise ValueError("Unsupported PDF compression chain")
            compressed = data[start + 6 : end].lstrip(b"\r\n")
            inflater = zlib.decompressobj()
            inflated = inflater.decompress(compressed, MAX_MEMBER_BYTES + 1)
            if len(inflated) > MAX_MEMBER_BYTES or inflater.unconsumed_tail:
                raise BufferError("RESOURCE_LIMIT_EXCEEDED: PDF stream inflation")
            if not inflater.eof:
                raise ValueError("Invalid PDF compressed stream")
            total += len(inflated)
        else:
            total += end - start
        if re.search(rb"/Subtype\s*/Image\b", header):
            # Dictionary key order is immaterial. Indirect/ambiguous image dimensions
            # cannot establish a preallocation bound and must fail this subset.
            dimensions = []
            for key in (b"Width", b"Height"):
                match = re.search(rb"/" + key + rb"\s+(\d+)\s*(?=/|>>)", header)
                if not match:
                    raise ValueError("Unverified PDF image dimensions")
                dimensions.append(int(match.group(1)))
            raster_pixels += dimensions[0] * dimensions[1]
            if min(dimensions) < 1 or raster_pixels > MAX_RASTER_PIXELS:
                raise BufferError("RESOURCE_LIMIT_EXCEEDED: PDF embedded raster")
        if total > MAX_EXPANDED_BYTES:
            raise BufferError("RESOURCE_LIMIT_EXCEEDED: PDF expanded streams")
        offset = end + len(b"endstream")
    return data

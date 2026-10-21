"""Native-call workload instrumented behind the REAL authenticated worker endpoint.

No production feature/configuration imports this overlay. No fixture output is staged.
"""

import importlib.util
import io
import json
import os
import sys
import tempfile
import time
from pathlib import Path

import onnxruntime
from PIL import Image, ImageDraw, ImageFont

repository = next(
    p for p in Path(__file__).resolve().parents if (p / "rag-engine/src").is_dir()
)
spec = importlib.util.spec_from_file_location(
    "ppv_rag._real_main", repository / "rag-engine/src/ppv_rag/main.py"
)
real = importlib.util.module_from_spec(spec)
sys.modules[spec.name] = real
spec.loader.exec_module(real)
app = real.app


def workload(request, cancelled):
    from ppv_rag.limits import parsing_scope
    from ppv_rag.ocr.engine import AdaptiveOCREngine
    from ppv_rag.parsers.pdf import parse_pdf
    from ppv_rag.worker import verify_document_grant

    # Real Java admission/grant/HTTP binding stays in use; workload bytes are public test data.
    verify_document_grant(request)
    events = Path(f"native-{request.job_id}.jsonl")
    scratch = Path(f"scratch-{request.job_id}").resolve()
    scratch.mkdir()
    tempfile.tempdir = str(scratch)
    for name in ("TEMP", "TMP", "TMPDIR"):
        os.environ[name] = str(scratch)

    def record(event, **values):
        with events.open("a", encoding="utf-8") as stream:
            stream.write(
                json.dumps(
                    dict(
                        event=event,
                        pid=os.getpid(),
                        monotonic=time.monotonic(),
                        **values,
                    )
                )
                + "\n"
            )

    image = Image.new("RGB", (1800, 2400), "white")
    draw = ImageDraw.Draw(image)
    font = ImageFont.truetype("C:/Windows/Fonts/arial.ttf", 36)
    for row in range(35):
        draw.text(
            (50, 40 + row * 64),
            f"Native OCR cancellation document row {row}: 9081726354.",
            font=font,
            fill="black",
        )
    output = io.BytesIO()
    image.save(output, "PDF", resolution=144)
    original = onnxruntime.InferenceSession.run
    sequence = 0

    def native_run(session, *args, **kwargs):
        nonlocal sequence
        sequence += 1
        index = sequence
        record("native_enter", sequence=index)
        result = original(session, *args, **kwargs)
        record("native_return", sequence=index)
        return result

    onnxruntime.InferenceSession.run = native_run
    engine = AdaptiveOCREngine()
    record("workload_ready", pdf_bytes=len(output.getvalue()))
    with parsing_scope(cancelled, seconds=120):
        for _ in range(20):
            parse_pdf(
                output.getvalue(),
                "public-native-lifecycle-fixture",
                1,
                ocr_engine=engine,
            )
    record("unexpected_complete")
    raise RuntimeError("Native lifecycle workload completed before termination")


real.process_document_batch_request = workload

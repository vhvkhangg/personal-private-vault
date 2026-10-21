"""Multilingual embedding pipeline with ONNX runtime, 384 dimensions, and GPU-to-CPU fallback."""

from __future__ import annotations

import hashlib
import os
import unicodedata
from dataclasses import dataclass
from pathlib import Path
from typing import Any

import numpy as np

from ppv_rag.limits import ParsingCancelled, checkpoint

# Pinned model constants
MODEL_NAME = "sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2"
MODEL_VERSION = "2.0.0"
MODEL_ARTIFACT_SHA256 = "98a01d88b7de996cdea58c32ca71208c09968d143798814b2ea09d3439dc334f"
DIMENSION = 384
TOKENIZER_VERSION = "hf-tokenizers-0.23.3"
PARSER_VERSION = "ppv-parser-v18.6-d17-html-container-boundaries"
CHUNKER_VERSION = "structure-first-v18.5-compact-source-context"
OCR_VERSION = "rapidocr-1.4.4-en-hans-script-admission-v18.2"


TOKENIZER_ARTIFACT_SHA256 = "2c3387be76557bd40970cec13153b3bbf80407865484b209e655e5e4729076b8"


@dataclass(frozen=True)
class ModelMetadata:
    model_name: str
    model_version: str
    artifact_hash: str
    dimensions: int
    tokenizer_version: str
    tokenizer_artifact_hash: str
    parser_version: str
    chunker_version: str
    ocr_pipeline_version: str
    provider: str


class MultilingualEmbeddingPipeline:
    """Offline multilingual embedding pipeline using paraphrase-multilingual-MiniLM-L12-v2."""

    DIMENSION = DIMENSION
    MODEL_NAME = MODEL_NAME
    MODEL_VERSION = MODEL_VERSION
    TOKENIZER_VERSION = TOKENIZER_VERSION
    PARSER_VERSION = PARSER_VERSION
    CHUNKER_VERSION = CHUNKER_VERSION
    OCR_VERSION = OCR_VERSION
    # Avoid ORT's default machine-wide thread pool; singleton tensor semantics stay fixed.
    CPU_THREADS = 4

    def __init__(self, force_cpu: bool = False, model_dir: Path | str | None = None) -> None:
        self.force_cpu = force_cpu
        self.provider = "CPU"
        self._fallback_used = False
        self.artifact_hash = MODEL_ARTIFACT_SHA256

        # Determine model path
        if model_dir is not None:
            self._model_path = Path(model_dir)
        else:
            env_dir = os.environ.get("PPV_RAG_MODEL_DIR")
            if env_dir:
                self._model_path = Path(env_dir)
            else:
                self._model_path = (
                    Path(__file__).resolve().parent.parent.parent.parent
                    / "models"
                    / "paraphrase-multilingual-MiniLM-L12-v2"
                )

        self._session: Any = None
        self._tokenizer: Any = None
        self._init_engine()

    def _init_engine(self) -> None:
        """Initialize ONNX execution session and tokenizer, validating exact artifact hashes."""
        model_file = self._model_path / "model.onnx"
        tokenizer_file = self._model_path / "tokenizer.json"

        if not model_file.exists() or not tokenizer_file.exists():
            raise FileNotFoundError(
                f"Required model artifact missing: "
                f"model={model_file.exists()}, tokenizer={tokenizer_file.exists()}"
            )

        # Verify model artifact hash
        with open(model_file, "rb") as f:
            computed_model_sha = hashlib.sha256(f.read()).hexdigest()
        if computed_model_sha != MODEL_ARTIFACT_SHA256:
            raise ValueError(
                f"Model artifact SHA-256 mismatch: expected {MODEL_ARTIFACT_SHA256}, "
                f"got {computed_model_sha}"
            )
        self.artifact_hash = computed_model_sha

        # Verify tokenizer artifact hash
        with open(tokenizer_file, "rb") as f:
            computed_tok_sha = hashlib.sha256(f.read()).hexdigest()
        if computed_tok_sha != TOKENIZER_ARTIFACT_SHA256:
            raise ValueError(
                f"Tokenizer artifact SHA-256 mismatch: expected {TOKENIZER_ARTIFACT_SHA256}, "
                f"got {computed_tok_sha}"
            )

        # Initialize tokenizer
        from tokenizers import Tokenizer  # type: ignore[import-untyped]

        self._tokenizer = Tokenizer.from_file(str(tokenizer_file))
        self._tokenizer.no_truncation()

        # Initialize ONNX runtime session with GPU/CPU provider negotiation
        import onnxruntime as ort  # type: ignore[import-untyped]

        available = ort.get_available_providers()
        if not self.force_cpu and "CUDAExecutionProvider" in available:
            try:
                self._session = ort.InferenceSession(
                    str(model_file),
                    sess_options=self._session_options(),
                    providers=["CUDAExecutionProvider", "CPUExecutionProvider"],
                )
                self.provider = (
                    "CUDA" if "CUDAExecutionProvider" in self._session.get_providers() else "CPU"
                )
            except Exception:
                self._session = ort.InferenceSession(
                    str(model_file),
                    sess_options=self._session_options(),
                    providers=["CPUExecutionProvider"],
                )
                self.provider = "CPU"
        else:
            self._session = ort.InferenceSession(
                str(model_file),
                sess_options=self._session_options(),
                providers=["CPUExecutionProvider"],
            )
            self.provider = "CPU"

    @classmethod
    def _session_options(cls) -> Any:
        import onnxruntime as ort  # type: ignore[import-untyped]

        options = ort.SessionOptions()
        options.intra_op_num_threads = cls.CPU_THREADS
        options.inter_op_num_threads = 1
        options.execution_mode = ort.ExecutionMode.ORT_SEQUENTIAL
        return options

    def metadata(self) -> ModelMetadata:
        return ModelMetadata(
            model_name=self.MODEL_NAME,
            model_version=self.MODEL_VERSION,
            artifact_hash=self.artifact_hash,
            dimensions=self.DIMENSION,
            tokenizer_version=self.TOKENIZER_VERSION,
            tokenizer_artifact_hash=TOKENIZER_ARTIFACT_SHA256,
            parser_version=self.PARSER_VERSION,
            chunker_version=self.CHUNKER_VERSION,
            ocr_pipeline_version=self.OCR_VERSION,
            provider=self.provider,
        )

    def embed_text(self, text: str) -> list[float]:
        """Embed a single text string into a normalized 384-dim float vector."""
        vectors = self.embed_batch([text])
        return vectors[0]

    def token_count(self, text: str) -> int:
        normalized = unicodedata.normalize("NFKC", text.strip())
        return len(self._tokenizer.encode(normalized).ids)

    def embed_batch(self, texts: list[str]) -> list[list[float]]:
        """Embed a batch of texts into finite, L2-normalized 384-dimensional vectors."""
        if not texts:
            return []

        results: list[list[float]] = []
        # Call-local reuse only: no content crosses source grants or worker requests.
        # The source/token limits bound this dictionary. Keep singleton tensors: the
        # pinned dynamically quantized graph changes rounding with unrelated batch rows.
        reused: dict[str, list[float]] = {}
        for text in texts:
            checkpoint()
            normalized = unicodedata.normalize("NFKC", text.strip())
            if not normalized:
                raise ValueError("Empty text cannot produce a document embedding")
            if normalized not in reused:
                try:
                    vector = self._embed_onnx(normalized)
                except (ParsingCancelled, BufferError):
                    raise
                except Exception:
                    if self.provider != "CUDA" or self._fallback_used:
                        raise
                    self._fallback_used = True
                    import onnxruntime as ort  # type: ignore[import-untyped]

                    self._session = ort.InferenceSession(
                        str(self._model_path / "model.onnx"),
                        sess_options=self._session_options(),
                        providers=["CPUExecutionProvider"],
                    )
                    self.provider = "CPU"
                    vector = self._embed_onnx(normalized)
                checkpoint()
                reused[normalized] = self._normalize_vector(vector)
            checkpoint()
            results.append(list(reused[normalized]))
        return results

    def _normalize_vector(self, vec: np.ndarray) -> list[float]:
        if vec.ndim != 1 or vec.shape != (self.DIMENSION,):
            raise ValueError("Embedding produced wrong dimensions")

        # Verify finite values
        if not np.all(np.isfinite(vec)):
            raise ValueError("Embedding produced non-finite values (NaN or Inf)")

        # L2 normalize
        norm_val = float(np.linalg.norm(vec.astype(np.float64)))
        if np.isfinite(norm_val) and norm_val > 0.0:
            vec = vec / norm_val
        else:
            raise ValueError("Model produced a zero embedding")

        if not np.all(np.isfinite(vec)) or not np.isclose(
            np.linalg.norm(vec.astype(np.float64)), 1.0, atol=1e-5
        ):
            raise ValueError("Embedding normalization failed")
        result: list[float] = vec.tolist()
        return result

    def _embed_onnx(self, text: str) -> np.ndarray:
        """Reuse this session with the original singleton tensor/vector semantics."""
        encoding = self._tokenizer.encode(text)
        if len(encoding.ids) > 512:
            raise ValueError("Chunk exceeds the model tokenizer budget")
        input_ids = np.array([encoding.ids], dtype=np.int64)
        attention_mask = np.array([encoding.attention_mask], dtype=np.int64)
        token_type_ids = np.array([encoding.type_ids], dtype=np.int64)
        checkpoint()
        outputs = self._session.run(
            None,
            {
                "input_ids": input_ids,
                "attention_mask": attention_mask,
                "token_type_ids": token_type_ids,
            },
        )
        last_hidden_state = np.asarray(outputs[0], dtype=np.float32)
        mask_expanded = np.broadcast_to(
            np.expand_dims(attention_mask, -1), last_hidden_state.shape
        ).astype(np.float32)
        sum_embeddings = np.sum(last_hidden_state * mask_expanded, axis=1)
        sum_mask = np.clip(mask_expanded.sum(axis=1), a_min=1e-9, a_max=None)
        result: np.ndarray = np.asarray((sum_embeddings / sum_mask)[0], dtype=np.float32)
        return result

"""Structured grounded answer generation with llama-cpp-python and pinned candidate model.

P19-S19-2 — SUSPENDED / NOT_ACCEPTED / ARCHIVAL_ONLY; do not enable or deploy.
Authority: docs/implementation/phase-19/owner-decision-s19-2-source-archival-only-2026-10-31.md.

Enforces strict JSON schema, non-thinking mode, exact excerpt citation quotes,
and deterministic claim/outcome representations.
"""

from __future__ import annotations

import hashlib
import importlib.metadata
import json
import logging
import os
import re
import sys
import time
from dataclasses import dataclass, field
from pathlib import Path
from typing import Any

logger = logging.getLogger(__name__)

C1_MODEL_PATH = os.environ.get(
    "PPV_RAG_QA_MODEL_PATH",
    str(Path(__file__).resolve().parents[2] / "models" / "Qwen3-4B-Q4_K_M.gguf"),
)
C1_MODEL_SHA256 = "7485fe6f11af29433bc51cab58009521f205840f5b4ae3a32fa7f92e8534fdf5"
C1_MODEL_VERSION = "Qwen3-4B-Q4_K_M"
CANDIDATE = os.environ.get("PPV_RAG_QA_CANDIDATE", "C1")
if CANDIDATE not in {"C1", "C2"}:
    raise RuntimeError("Unknown QA candidate")
MODEL_VERSION = C1_MODEL_VERSION if CANDIDATE == "C1" else "Qwen2.5-7B-Instruct-Q4_K_M"
MODEL_SHA256 = (
    C1_MODEL_SHA256
    if CANDIDATE == "C1"
    else ("dfce12e3862a5283ccfb88221b48480e58745165de856439950d0f22590580db")
)
RUNTIME_PROFILE = os.environ.get("PPV_RAG_QA_RUNTIME_PROFILE", "CPU_V2")
if RUNTIME_PROFILE not in {"CPU_V2", "CUDA_TEST_V1"}:
    raise RuntimeError("Unknown QA runtime profile")
CUDA_TEST = RUNTIME_PROFILE == "CUDA_TEST_V1"
if CUDA_TEST and (CANDIDATE != "C2" or os.environ.get("PPV_RAG_QA_TEST_SCOPED") != "1"):
    raise RuntimeError("CUDA QA requires explicit C2 test scope")
CUDA_RUNTIME_LOCK_SHA256 = "219c116bdce8582ac0a7a83929c1bb88e2e90105e4d6e7d39264c07e8c2d0663"
RUNTIME_LOCK_SHA256 = (
    CUDA_RUNTIME_LOCK_SHA256
    if CUDA_TEST
    else "3b5856a9004c8aad6c3d54b95ee5702acd49de662c3e416188222e1b0eed1a1d"
)
GENERATION_PROFILE = (
    f"{CANDIDATE}_OFFLINE_CUDA_TEST_V1" if CUDA_TEST else f"{CANDIDATE}_OFFLINE_CPU_V2"
)
_formatter: Any = None
_model_load_nanos = 0


def verify_runtime() -> dict[str, Any]:
    """Verify the approved installed Python/native dependency closure without fetching."""
    lock_path = Path(__file__).resolve().parents[2] / (
        "qa-cuda-runtime-lock.json" if CUDA_TEST else "qa-runtime-lock.json"
    )
    raw = lock_path.read_bytes()
    if hashlib.sha256(raw).hexdigest() != RUNTIME_LOCK_SHA256:
        raise RuntimeError("QA runtime lock mismatch")
    lock: dict[str, Any] = json.loads(raw)
    if sys.version.split()[0] != lock["python_version"]:
        raise RuntimeError("QA Python version mismatch")
    for name, expected in lock["packages"].items():
        if importlib.metadata.version(name) != expected["version"]:
            raise RuntimeError("QA dependency version mismatch")
        for relative, digest in expected["files"].items():
            file_path = Path(sys.prefix) / relative
            if not file_path.is_file() or _file_sha256(file_path) != digest:
                raise RuntimeError("QA dependency bytes mismatch")
    for name, digest in lock["base_binaries"].items():
        if _file_sha256(Path(sys.base_prefix) / name) != digest:
            raise RuntimeError("QA interpreter bytes mismatch")
    for relative, digest in lock.get("runtime_files", {}).items():
        file_path = Path(sys.prefix) / relative
        if not file_path.is_file() or _file_sha256(file_path) != digest:
            raise RuntimeError("QA dependency bytes mismatch")
    return lock


def _file_sha256(path: Path) -> str:
    with path.open("rb") as stream:
        return hashlib.file_digest(stream, "sha256").hexdigest()


def available_system_memory() -> int:
    import ctypes

    class MemoryStatus(ctypes.Structure):
        _fields_ = [("length", ctypes.c_ulong), ("load", ctypes.c_ulong)] + [
            (name, ctypes.c_ulonglong)
            for name in (
                "total",
                "available",
                "page_total",
                "page_available",
                "virtual_total",
                "virtual_available",
                "extended",
            )
        ]

    status = MemoryStatus()
    status.length = ctypes.sizeof(status)
    if not ctypes.windll.kernel32.GlobalMemoryStatusEx(ctypes.byref(status)):
        raise RuntimeError("QA system headroom unavailable")
    return int(status.available)


def require_system_headroom() -> None:
    if available_system_memory() < 2 * 1024**3:
        raise RuntimeError("QA_SYSTEM_HEADROOM_BELOW_2_GIB")


_llm: Any = None

QA_JSON_SCHEMA: dict[str, Any] = {
    "type": "object",
    "properties": {
        "outcome": {
            "type": "string",
            "enum": ["ANSWERED", "PARTIAL", "CONFLICT", "ABSTAIN"],
        },
        "claims": {
            "type": "array",
            "items": {
                "type": "object",
                "properties": {
                    "id": {"type": "string"},
                    "text": {"type": "string"},
                    "supports": {
                        "type": "array",
                        "items": {
                            "type": "object",
                            "properties": {
                                "citation_key": {"type": "string"},
                                "quote": {"type": "string"},
                            },
                            "required": ["citation_key", "quote"],
                            "additionalProperties": False,
                        },
                    },
                },
                "required": ["id", "text", "supports"],
                "additionalProperties": False,
            },
        },
        "unanswered_parts": {
            "type": "array",
            "items": {"type": "string"},
        },
        "conflict_pairs": {
            "type": "array",
            "items": {
                "type": "object",
                "properties": {
                    "claim_id_a": {"type": "string"},
                    "claim_id_b": {"type": "string"},
                    "description": {"type": "string"},
                },
                "required": ["claim_id_a", "claim_id_b", "description"],
                "additionalProperties": False,
            },
        },
    },
    "required": ["outcome", "claims", "unanswered_parts", "conflict_pairs"],
    "additionalProperties": False,
}


@dataclass(frozen=True)
class SupportEntry:
    citation_key: str
    quote: str


@dataclass(frozen=True)
class ClaimEntry:
    id: str
    text: str
    supports: list[SupportEntry]


@dataclass(frozen=True)
class ConflictPairEntry:
    claim_id_a: str
    claim_id_b: str
    description: str


@dataclass(frozen=True)
class QaGenerationResult:
    outcome: str
    claims: list[ClaimEntry]
    unanswered_parts: list[str]
    conflict_pairs: list[ConflictPairEntry]
    model_version: str
    artifact_hash: str
    measurements: dict[str, int] = field(default_factory=dict)


def _verify_model_file(file_path: Path, expected_sha256: str) -> None:
    if not file_path.is_file():
        raise RuntimeError(f"QA model artifact not found at {file_path}")
    h = hashlib.sha256()
    with open(file_path, "rb") as f:
        while chunk := f.read(65536):
            h.update(chunk)
    actual_hash = h.hexdigest()
    if actual_hash != expected_sha256:
        raise RuntimeError(
            f"QA model artifact hash mismatch at {file_path}: "
            f"expected {expected_sha256}, got {actual_hash}"
        )


def get_llm(model_path: str = "") -> Any:
    global _llm, _formatter, _model_load_nanos
    if _llm is None:
        lock = verify_runtime()
        require_system_headroom()
        try:
            import llama_cpp  # type: ignore[import-untyped]
        except ImportError as exc:
            raise RuntimeError(
                "llama_cpp package is not available in the current environment"
            ) from exc

        path = model_path or os.environ.get("PPV_RAG_QA_MODEL_PATH", C1_MODEL_PATH)
        p = Path(path)
        # A trusted configured path names one exact asset. Missing it cannot
        # authorize another local file with the same basename.
        _verify_model_file(p, MODEL_SHA256)
        if CANDIDATE == "C2":
            _verify_model_file(
                p.with_name("qwen2.5-7b-instruct-q4_k_m-00002-of-00002.gguf"),
                "539cf93f78e887edea1c04e2d7d8cdaca9d01dae9c9025bcb8accbe29df3d72a",
            )
        path = str(p.resolve())

        threads = int(os.environ.get("PPV_RAG_QA_THREADS", "4"))
        n_ctx = int(os.environ.get("PPV_RAG_QA_CONTEXT_TOKENS", "4096"))
        if threads != 4 or n_ctx != 4096:
            raise RuntimeError("QA fixed context/thread profile mismatch")
        load_started = time.perf_counter_ns()
        if CUDA_TEST:
            if not llama_cpp.llama_supports_gpu_offload():
                raise RuntimeError("Pinned CUDA backend unavailable")
            _llm = llama_cpp.Llama(
                model_path=path,
                n_ctx=n_ctx,
                n_threads=threads,
                n_threads_batch=4,
                n_gpu_layers=-1,
                use_mmap=False,
                n_batch=512,
                n_ubatch=512,
                offload_kqv=True,
                flash_attn=False,
                verbose=False,
                seed=42,
            )
        else:
            _llm = llama_cpp.Llama(
                model_path=path,
                n_ctx=n_ctx,
                n_threads=threads,
                n_gpu_layers=0,
                verbose=False,
                seed=42,
            )
        _model_load_nanos = time.perf_counter_ns() - load_started
        from llama_cpp.llama_chat_format import Jinja2ChatFormatter

        template = _llm.metadata.get("tokenizer.chat_template", "")
        if (
            hashlib.sha256(template.encode()).hexdigest()
            != lock["models"][CANDIDATE]["template_sha256"]
        ):
            _llm.close()
            _llm = None
            raise RuntimeError("QA chat template mismatch")
        if CANDIDATE == "C1":
            condition = "{%- if enable_thinking is defined and enable_thinking is false %}"
            if template.count(condition) != 1:
                raise RuntimeError("QA non-thinking template control mismatch")
            template = template.replace(condition, "{%- if true %}")
        _formatter = Jinja2ChatFormatter(template=template, eos_token="<|im_end|>", bos_token="")
        _llm.chat_handler = _formatter.to_chat_handler()
        require_system_headroom()
    return _llm


def build_system_prompt() -> str:
    return (
        "You are an expert grounded Markdown QA assistant for Personal Private Vault. "
        "Your task is to answer the user question strictly using only explicit facts directly\n"
        "stated in the provided EVIDENCE excerpts. You must follow these strict rules:\n"
        "1. STRICT GROUNDING: Rely ONLY on facts directly stated in the EVIDENCE excerpts. "
        "Do NOT extrapolate, guess, speculate, or introduce any outside facts.\n"
        "2. CITATION QUOTES: For every factual claim in 'claims', each entry in 'supports' "
        "must have 'citation_key' (e.g. '[1]') and 'quote'. The 'quote' MUST BE AN EXACT, "
        "VERBATIM SUBSTRING copied directly from that citation excerpt text.\n"
        "3. OUTCOMES:\n"
        "   - 'ANSWERED': All parts of the question are directly answered by evidence.\n"
        "   - 'PARTIAL': If ANY part of a question can be answered by the evidence while\n"
        "     other parts cannot be found, you MUST choose 'PARTIAL' (never 'ABSTAIN').\n"
        "     Extract the supported claims for the answered parts into 'claims', and list the\n"
        "     missing aspects in 'unanswered_parts'. Do NOT create claims stating that\n"
        "     something is not mentioned; place all missing topics in 'unanswered_parts'.\n"
        "   - 'CONFLICT': Two or more permitted EVIDENCE citations directly contradict each other\n"
        "     regarding a fact or entity property. Conversation history is NOT an evidence\n"
        "     citation and does NOT constitute a conflict. If prior conversation contradicts\n"
        "     evidence specifications, answer according to the evidence specifications with\n"
        "     outcome 'ANSWERED'. Include both conflicting evidence statements in 'claims'\n"
        "     with their respective citations, and identify the conflict in 'conflict_pairs'.\n"
        "   - 'ABSTAIN': The evidence contains NO facts answering ANY part of the question, or\n"
        "     the question asks for hypothetical/extrapolated deductions entirely absent from\n"
        "     evidence. 'claims' MUST be empty.\n"
        "4. LANGUAGE: Respond in the requested response language (or question language). "
        "Preserve entity names, personal names, and technical terms verbatim.\n"
        "5. OUTPUT: Output ONLY a JSON object matching the requested schema. Never output "
        "<think> blocks, markdown code blocks, or explanations.\n"
        "6. CONCISENESS: Extract only 1 to 3 minimal, essential factual claims needed to answer "
        "the question directly. Do NOT repeat claims, paraphrase the same fact multiple times, "
        "or loop endlessly."
    )


def build_user_prompt(
    question: str,
    evidence: list[dict[str, str]],
    history: list[dict[str, str]] | None = None,
    response_language: str | None = None,
) -> str:
    parts: list[str] = ["EVIDENCE:"]
    for ev in evidence:
        citation_key = ev.get("citation_key", "")
        source_name = ev.get("source_name", "")
        excerpt = ev.get("excerpt", "")
        parts.append(f"{citation_key} Source: {source_name}\n{excerpt}\n")

    if history:
        parts.append("CONVERSATION REFERENCE HISTORY:")
        for h in history:
            role = h.get("role", "user").capitalize()
            content = h.get("content", "")
            parts.append(f"{role}: {content}")
        parts.append("")

    parts.append(f"QUESTION:\n{question.strip()}\n")
    if response_language:
        parts.append(f"REQUESTED RESPONSE LANGUAGE: {response_language.strip()}\n")

    return "\n".join(parts)


def strip_think_tags(text: str) -> str:
    cleaned = re.sub(r"<think>.*?</think>", "", text, flags=re.DOTALL)
    cleaned = re.sub(r"^```(?:json)?\s*", "", cleaned.strip(), flags=re.MULTILINE)
    cleaned = re.sub(r"\s*```$", "", cleaned.strip(), flags=re.MULTILINE)
    return cleaned.strip()


def generate_grounded_answer(
    question: str,
    evidence: list[dict[str, str]],
    history: list[dict[str, str]] | None = None,
    response_language: str | None = None,
    model_path: str = C1_MODEL_PATH,
) -> QaGenerationResult:
    if not evidence:
        return QaGenerationResult(
            outcome="ABSTAIN",
            claims=[],
            unanswered_parts=["No evidence available"],
            conflict_pairs=[],
            model_version=MODEL_VERSION,
            artifact_hash=MODEL_SHA256,
        )

    llm = get_llm(model_path)
    system_prompt = build_system_prompt()

    active_history = list(history) if history else []
    max_tokens = int(os.environ.get("PPV_RAG_QA_MAX_GENERATED_TOKENS", "768"))
    if max_tokens != 768:
        raise RuntimeError("QA fixed output profile mismatch")
    max_prompt_tokens = 4096 - max_tokens

    def _count_tokens(ev_list: list[dict[str, str]], hist_list: list[dict[str, str]]) -> int:
        u_prompt = build_user_prompt(question, ev_list, hist_list, response_language)
        rendered = _formatter(
            messages=[
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": u_prompt},
            ]
        )
        return len(
            llm.tokenize(
                rendered.prompt.encode("utf-8"), add_bos=not rendered.added_special, special=True
            )
        )

    # Narrow history turns from oldest to newest if non-evidence prompt exceeds budget
    non_evidence_tokens = _count_tokens([], active_history)
    while active_history and non_evidence_tokens > max_prompt_tokens:
        active_history.pop(0)
        non_evidence_tokens = _count_tokens([], active_history)

    # Fail explicitly if question + system prompt alone exceeds context budget
    if non_evidence_tokens > max_prompt_tokens:
        raise ValueError(
            "Non-evidence prompt (question and system prompt) exceeds context window budget"
        )

    # Pack whole evidence units in ranking order; never force an impossible unit
    active_evidence: list[dict[str, str]] = []
    for ev in evidence:
        candidate_evidence = active_evidence + [ev]
        if _count_tokens(candidate_evidence, active_history) <= max_prompt_tokens:
            active_evidence.append(ev)
        else:
            break

    if not active_evidence:
        return QaGenerationResult(
            outcome="ABSTAIN",
            claims=[],
            unanswered_parts=["Context budget insufficient for evidence"],
            conflict_pairs=[],
            model_version=MODEL_VERSION,
            artifact_hash=MODEL_SHA256,
        )

    user_prompt = build_user_prompt(question, active_evidence, active_history, response_language)

    prompt_tokens = _count_tokens(active_evidence, active_history)
    generation_started = time.perf_counter_ns()
    completion = llm.create_chat_completion(
        messages=[
            {"role": "system", "content": system_prompt},
            {"role": "user", "content": user_prompt},
        ],
        response_format={"type": "json_object", "schema": QA_JSON_SCHEMA},
        temperature=0.1,
        top_p=0.8,
        repeat_penalty=1.15,
        seed=42,
        max_tokens=max_tokens,
    )
    generation_nanos = time.perf_counter_ns() - generation_started
    require_system_headroom()

    choice = completion["choices"][0]
    finish_reason = choice.get("finish_reason")
    if finish_reason != "stop":
        raise ValueError(f"Candidate generation incomplete: finish_reason is {finish_reason}")

    raw_text = choice["message"]["content"] or ""
    clean_json_str = strip_think_tags(raw_text)

    def _dict_no_dup(pairs: list[tuple[str, Any]]) -> dict[str, Any]:
        d: dict[str, Any] = {}
        for k, v in pairs:
            if k in d:
                raise ValueError(f"Duplicate JSON key: {k}")
            d[k] = v
        return d

    try:
        data = json.loads(clean_json_str, object_pairs_hook=_dict_no_dup)
    except Exception as exc:
        logger.error("Model did not return valid JSON: %s", type(exc).__name__)
        raise ValueError(f"Model returned invalid JSON: {type(exc).__name__}") from exc

    if not isinstance(data, dict):
        raise ValueError("Model output must be a JSON object")

    required_keys = {"outcome", "claims", "unanswered_parts", "conflict_pairs"}
    if set(data.keys()) != required_keys:
        missing = required_keys - set(data.keys())
        extra = set(data.keys()) - required_keys
        raise ValueError(f"Model output fields mismatch: missing={missing}, extra={extra}")

    outcome = str(data.get("outcome", "")).upper()
    if outcome not in {"ANSWERED", "PARTIAL", "CONFLICT", "ABSTAIN"}:
        raise ValueError(f"Invalid outcome: {outcome}")

    evidence_by_key = {ev.get("citation_key"): ev.get("excerpt", "") for ev in active_evidence}
    claims: list[ClaimEntry] = []
    raw_claims = data.get("claims")
    if not isinstance(raw_claims, list):
        raise ValueError("claims must be a list")

    for c in raw_claims:
        if not isinstance(c, dict):
            raise ValueError("Each claim must be an object")
        required_claim_keys = {"id", "text", "supports"}
        if set(c.keys()) != required_claim_keys:
            missing_c = required_claim_keys - set(c.keys())
            extra_c = set(c.keys()) - required_claim_keys
            raise ValueError(f"Claim fields mismatch: missing={missing_c}, extra={extra_c}")
        raw_id = c.get("id")
        raw_text = c.get("text")
        if not isinstance(raw_id, str) or not isinstance(raw_text, str):
            raise ValueError("Claim id and text must be strings")
        cid = raw_id.strip()
        ctext = raw_text.strip()
        if not cid or not ctext:
            raise ValueError("Claim must have non-empty id and text")
        raw_supports = c.get("supports")
        if not isinstance(raw_supports, list):
            raise ValueError("Claim supports must be a list")
        if outcome != "ABSTAIN" and not raw_supports:
            raise ValueError(f"Claim {cid} has empty supports")
        supports: list[SupportEntry] = []
        for s in raw_supports:
            if not isinstance(s, dict):
                raise ValueError("Support must be an object")
            required_support_keys = {"citation_key", "quote"}
            if set(s.keys()) != required_support_keys:
                missing_s = required_support_keys - set(s.keys())
                extra_s = set(s.keys()) - required_support_keys
                raise ValueError(f"Support fields mismatch: missing={missing_s}, extra={extra_s}")
            raw_ckey = s.get("citation_key")
            raw_cquote = s.get("quote")
            if not isinstance(raw_ckey, str) or not isinstance(raw_cquote, str):
                raise ValueError("Support citation_key and quote must be strings")
            ckey = raw_ckey.strip()
            if not ckey.startswith("[") and ckey.isdigit():
                ckey = f"[{ckey}]"
            cquote = raw_cquote.strip()
            excerpt = evidence_by_key.get(ckey)
            if excerpt is None:
                raise ValueError(f"Support references unknown citation key: {ckey}")
            if not cquote:
                raise ValueError("Support quote must not be empty")
            if cquote not in excerpt:
                raise ValueError(f"Support quote is not an exact substring of citation {ckey}")
            supports.append(SupportEntry(citation_key=ckey, quote=cquote))
        claims.append(ClaimEntry(id=cid, text=ctext, supports=supports))

    raw_unanswered = data.get("unanswered_parts")
    if not isinstance(raw_unanswered, list):
        raise ValueError("unanswered_parts must be a list")
    unanswered: list[str] = []
    for p in raw_unanswered:
        if not isinstance(p, str):
            raise ValueError("unanswered_parts entries must be strings")
        p_clean = p.strip()
        if not p_clean:
            raise ValueError("unanswered_parts entry must not be empty or blank")
        if len(p_clean) > 120:
            raise ValueError("unanswered_parts entry exceeds 120 characters limit")
        if any(ord(ch) < 32 and ch not in "\t\n\r" for ch in p_clean):
            raise ValueError("unanswered_parts entry contains control characters")
        if p_clean not in unanswered:
            unanswered.append(p_clean)

    conflicts: list[ConflictPairEntry] = []
    raw_conflicts = data.get("conflict_pairs")
    if not isinstance(raw_conflicts, list):
        raise ValueError("conflict_pairs must be a list")
    for cp in raw_conflicts:
        if not isinstance(cp, dict):
            raise ValueError("Conflict pair must be an object")
        required_cp_keys = {"claim_id_a", "claim_id_b", "description"}
        if set(cp.keys()) != required_cp_keys:
            missing_cp = required_cp_keys - set(cp.keys())
            extra_cp = set(cp.keys()) - required_cp_keys
            raise ValueError(
                f"Conflict pair fields mismatch: missing={missing_cp}, extra={extra_cp}"
            )
        raw_a = cp.get("claim_id_a")
        raw_b = cp.get("claim_id_b")
        raw_desc = cp.get("description")
        if (
            not isinstance(raw_a, str)
            or not isinstance(raw_b, str)
            or not isinstance(raw_desc, str)
        ):
            raise ValueError("Conflict pair fields must be strings")
        cid_a = raw_a.strip()
        cid_b = raw_b.strip()
        desc = raw_desc.strip()
        if not cid_a or not cid_b:
            raise ValueError("Conflict pair must specify non-empty claim IDs")
        conflicts.append(ConflictPairEntry(claim_id_a=cid_a, claim_id_b=cid_b, description=desc))

    if outcome == "CONFLICT":
        if len(claims) < 2 or not conflicts:
            raise ValueError(
                "CONFLICT outcome requires at least two conflicting claims and conflict_pairs"
            )
    elif outcome == "ABSTAIN":
        if claims:
            raise ValueError("ABSTAIN outcome cannot contain claims")
    elif outcome in {"ANSWERED", "PARTIAL"}:
        if not claims:
            raise ValueError(f"{outcome} outcome must contain at least one claim")

    return QaGenerationResult(
        outcome=outcome,
        claims=claims,
        unanswered_parts=unanswered,
        conflict_pairs=conflicts,
        model_version=MODEL_VERSION,
        artifact_hash=MODEL_SHA256,
        measurements={
            "model-load-ns": _model_load_nanos,
            "generation-ns": generation_nanos,
            "prompt-tokens": prompt_tokens,
            "completion-tokens": int(completion.get("usage", {}).get("completion_tokens", 0)),
            "evidence-units": len(active_evidence),
            "context-omissions": len(evidence) - len(active_evidence),
        },
    )


def profile_hash() -> str:
    """Complete fixed runtime/schema/template/packing/sampling identity, not prompt alone."""
    profile = {
        "version": "QA_V2",
        "candidate": CANDIDATE,
        "runtime_lock": RUNTIME_LOCK_SHA256,
        "prompt": build_system_prompt(),
        "schema": QA_JSON_SCHEMA,
        "cpu_threads": 4,
        "gpu_layers": 0,
        "context": 4096,
        "output": 768,
        "seed": 42,
        "temperature": 0.1,
        "top_p": 0.8,
        "repeat_penalty": 1.15,
        "top_k": 40,
        "min_p": 0.05,
        "packing": "exact_formatter_whole_unit_v2",
        "thinking": False,
        "minimum_system_headroom_bytes": 2 * 1024**3,
    }
    if CUDA_TEST:
        profile.update(
            {
                "version": "QA_CUDA_TEST_V1",
                "gpu_layers": -1,
                "use_mmap": False,
                "offload_kqv": True,
                "flash_attn": False,
                "batch": 512,
                "ubatch": 512,
                "cpu_batch_threads": 4,
            }
        )
    return hashlib.sha256(
        json.dumps(profile, sort_keys=True, separators=(",", ":")).encode()
    ).hexdigest()

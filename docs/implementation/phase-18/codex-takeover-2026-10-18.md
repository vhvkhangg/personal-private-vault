# Phase 18 — Codex takeover implementation record

**Implementer:** Codex, explicitly owner-invoked 2026-10-18. **Independent acceptance: NOT PERFORMED.**
This is a partial takeover result, not closure of FR18-1–11, P18-FINAL, runtime/R7
acceptance, READY FOR OWNER COMMIT or authorization to publish. The two exhausted
Antigravity rounds remain exhausted. No third Antigravity round was used.

Entry evidence is the unchanged [re-review 1](reviews/2026-10-18-phase-18-codex-final-re-review-1.md)
and [re-review 2](reviews/2026-10-18-phase-18-codex-final-re-review-2.md), both dated
2026-10-18 and both CHANGES_REQUESTED after actual remediation. This takeover uses
only the latter report's narrowed residuals under [ACTIVE](../handoffs/ACTIVE.md).

## Implemented repairs and their remaining limits

| Original finding | Codex implementation | Still required; no self-closure |
| --- | --- | --- |
| FR18-1 | One compact UTF-8, recursively sorted, typed-default/null input encoding. Java constructs DOMAIN fields from fresh owner projections; caller bodies/base64/transclusions reject. Actual Java/native Python/restricted SQL/Java publication and Unicode digest tests pass. | External-file prepared input remains unavailable; complete replay/mixed-version/result coverage still requires independent verification. |
| FR18-2 | Reuses source authority for the complete union of each root's actual closure. Preparation/hashes occur before guards; one source-first guard pass rechecks owner versions/reset/consent/policy and edges through C. Null/stale root expectations reject. | Mixed native external authority and deterministic changed-boundary races in both orders remain unproven. |
| FR18-3 | Finalizer checks exact issued input/scope/output grant binding, unique staged roots, nonempty success, actual pipeline identity, mandatory line end and numeric DOCX address. | Exact output receipt, strict discriminated result/source/field/excerpt verification, effective runtime staging authority and complete writer/seal/rollback/cancel proof remain blockers. Synthetic staging tests are explicitly not native producer proof. |
| FR18-4 | Durable batch identity survives manifest erasure. Per-generation chunks, root metadata and matching staged payload erase without invalidating a sibling. Union grant/lease digests scrub while active v3 siblings retain independent manifests. Repeated reconciliation and independent CURRENT are tested. | Independent review must verify all derivative/retry/dependent-generation cases, including external sources. |
| FR18-5 | BOM-aligned Hibernate vector module, float[]/VECTOR(384) entity mapping and real JPA publication/readback. Test application startup validates on pinned PG18.6/pgvector0.8.6. Append-only V19 refuses incompatible BYTEA without deleting data. | Requires independent acceptance of mapping, startup and rollback evidence. |
| FR18-6 | Worker transmits model/tokenizer/parser/chunker/OCR identities; finalizer verifies and persists them. One controlled same-artifact CPU fallback after inference failure; blank/zero/oversized embeddings reject. Model/tokenizer origin hashes are verified. | Fresh offline/native GPU/ABI/fallback and complete artifact/config/telemetry evidence are not accepted. No GPU-success assertion. |
| FR18-7 | pdfplumber/PDFMiner/PDFium replaces guessed glyph widths; native transforms/orientation and table-cell geometry are tested. OCR uses positioned rendered image regions. Detected raster tables reject rather than publish unverified cells. | Vietnamese recognition fails the frozen diagnostic; scanned/mixed/merged-table and complete four-language quality acceptance remain incomplete. |
| FR18-8 | DOMAIN versions/fields come from authority. Python transclusion grants carry actual versions; recursive resolution rejects cycles/private/missing descendants. Frontmatter requires exact metadata grants, safe YAML and duplicate/restrictive-key checks. | Independent external-file/child/field/version consent is not wired; full source authority remains incomplete. |
| FR18-9 | Preserves separate PDF boxes and sparse sheet ranges, exact split offsets, constituent metadata, actual tokenizer budget and no silent truncation. TXT whitespace offsets are exact. Selected overlap is zero. | Full format/merged-cell/normalization oracle and independent acceptance remain required. |
| FR18-10 | XLSX row overflow is a material rejection. Corrected Win64 memory structure; reports use absent measurements instead of invented zero, persist pipeline identity, and separate terminal history from CURRENT. Worker records own CPU and host snapshots separately. | Process-tree/host/VRAM peaks, temp/CPU/expanded-input guards, representative cancel/restart and measured/reviewed activation limits remain incomplete. |
| FR18-11 | Actual additive API/DBML/package/setup inventories and this evidence record are synchronized. Failed diagnostics are retained as failures. | All six focused acceptance packages and a separate independent review remain required. Five zero exits alone cannot pass P18-FINAL. |

V1–V18, Schema v1, ADR-0020, module dependencies and normal live-file containment
were not changed by takeover. V19 extends only the approved v3 independent-lifetime
contract; legacy v2 terminal scrub still requires no active legacy generation.

## Concrete quality failure and bounded owner decision

The frozen [synthetic source manifest](../../../rag-engine/tests/fixtures/phase18-ocr-v2/manifest.json)
has two disjoint source families, each grouping all four translations. Its SHA-256
is `c4fbbeb23c6571a76632dfc5b2a349b0ce8bc439ff5b4fa6169830bacca5e8be`.
Exact strings and rendered ink boxes are deterministic references; human
adjudication is **PENDING**, not asserted. These sources have now been exposed in
diagnosis and cannot establish a fresh independent held-out pass.

The selected runtime OCR diagnostic matched **3/8** complete strings. English and
Chinese differences were spacing; Vietnamese lost tone marks and changed `Số` to
`S6`. A publisher-pinned Latin PP-OCRv5 candidate still misread tone marks; a
multilingual PP-OCRv6 tiny candidate matched 3/4 development strings but still
misread Vietnamese. Neither candidate was selected in production. Their artifacts
are retained only under ignored `logs/phase18-takeover/ocr-candidates/`.
No diagnostic source was published by the parser probes or modified.

This hits the existing [quality protocol](held-out-quality-and-locators.md):
“If chosen defaults cannot support the scope, fail the gate and request a bounded
owner decision.” The required Vietnamese/scanned-table capability has not been
silently deferred or relabeled as a warning.

**Decision to make:** preserve the full D18-A contract and choose a finite next
feasibility checkpoint for (1) actual native R7-to-C authority and (2) a demonstrably
Vietnamese-capable/table-capable OCR pipeline, before further broad remediation.
Require a small frozen development set, fresh independent held-out adjudication,
separate RAM/VRAM measurements and a stop/result after that checkpoint. A larger
model or another runtime must be justified by those failed concrete cases, not a
speculative stack. This continues the same handoff; it does not restart Antigravity.

If that complete capability is no longer desired or feasible, the alternative is
an **explicit owner-approved contract/ADR change** identifying exactly what is
deferred or replaced (including immutable copies as separate identities if chosen).
Codex has not made that change or invented authority to accept a reduced Phase 18.
Source/result/receipt and resource blockers above also remain required in either
continuation of the current full scope. A separate independent reviewer must
review Codex's implementation; this session cannot accept itself.

## Verification and evidence handling

See [test-evidence](test-evidence.md) for command receipts/counts/hashes and the
full-gate outcome. The broad 221-test affected-module attempt had one real error:
the gate rejected a pipeline identity changed during an OCR experiment while
verification was running. That receipt remains FAIL; after code freeze the 13
changed tests pass. No failing result was waived or retried unchanged for luck.

Probe harness failures are distinguished from product results: initial Java
source-launch BOM compilation failed; the mapping probe then observed an actual
safe denial, not an R7 bypass or actual final-C proof. Initial OCR fixtures lost
Unicode through PowerShell's stdin encoding and were archived as invalid before
the corrected v2 freeze. The original v2 raw report was overwritten once by a
candidate replay; its command failure/count observation survives, but its old raw
boxes are not claimed as preserved. The final selected-runtime replay has a unique
retained report; the script now always writes unique report names.

No IDE-clean, warning-free, full resource acceptance or Windows firewall guarantee
is asserted. AUTH-CLOCK has no waiver. No commit, push, tag or PR was performed.

The required full Maven child and watchdog returned **0**, with **1,321 tests,
zero failures/errors/skips**, in **816.352 seconds**. The matching actual XML case
count is 1,321; suite count attributes undercount nested cases. Redirected stderr
made the outer PowerShell tool return 1, reproducible with an otherwise successful
stderr-only child; the canonical `$LASTEXITCODE` check succeeds. Both observations
are preserved. Four Python program checks return zero, including 72 passing tests.
The aggregate P18-FINAL still fails its mandatory focused acceptance conditions.

# D17 bounded acceptance under D20 — 2026-10-20

**Current disposition: `D17_MARKDOWN_VERIFIED_ACCEPTED` — the frozen static Obsidian Markdown profile only.**

The Owner explicitly accepts independent Codex verification in place of previously required
human review. The canonical [D20 decision](owner-decisions.md#2026-10-20--d20-d17-codex-independent-review-accepted-in-lieu-of-human-adjudication)
is the authority. This is a **changed acceptance policy**, not a statement that human review occurred.

## Independently verified and accepted

- All twelve MD17-01–12 bounded machine gates: `VERIFIED`.
- All five D17-R01–R05 findings, including R03's FR-01/FR-02: independently `CLOSED`.
- Independent tests: 170 D17, 25 compatibility and 25 fresh R03 tests: `PASS`.
- Official producer corpus: 1,267 supported + 62 required whole-document rejections
  (1,329 cases attempted); seven interpretation resolutions: independently supported.
- Preserved original model, tokenizer, limits, controlled temp evidence, source/revision/locator
  correctness and whole-root typed failures. Real unique-content 1M-character workload took
  52.900 seconds on the measured host, below its 60-second per-root deadline.

Basis: [independent final R03 review](reviews/2026-10-20-d17-r03-independent-final-re-review.md),
its [hash/invocation ledger](reviews/2026-10-20-d17-r03-independent-final-re-review-evidence.json),
the [D17 contract](obsidian-markdown-d17-contract.md) and the prior independently credited findings.
No code/test execution has occurred in this D20 documentation amendment itself.

## Human package: superseded gate, not fabricated results

The original [D19 package](d17-human-adjudication-2026-10-20/README.md) holds 213 case
rows plus 12 profile confirmations; all were left unsigned and undecided. The D20 Owner
amendment marks this former quality gate **NOT_REQUIRED_UNDER_D20**, *not* human `ACCEPT`,
completed human inspection or signer validation. Do not modify its source hashes, form entries,
manifest or historical evidence to manufacture green results.

## Not accepted or authorized by D20

- Broader Slice 2, Java/FR18-3 compact metadata admission/finalization, external indexing,
  P18-FINAL and Phase 18: **NOT ACCEPTED**.
- OCR/PDF/Office formats and live original paths: **DEFERRED/DISABLED**.
- Historical aggregate Java test gate: **FAILED**, unchanged.
- Slice 3/4, production publication or automatic owner commit/push: **NOT AUTHORIZED**.

Only independently demonstrated D17 Markdown capability within the frozen profile is accepted.
Historical time-stamped failures and human-review-pending statuses elsewhere in the docs
remain historical and must never be relabeled as passes.

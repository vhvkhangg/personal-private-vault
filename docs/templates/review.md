> **Review mode:** `SLICE` (once after each slice), `PHASE_FINAL` (once after all reviewed slices), `MILESTONE` (after 3 completed RAG phases), `RAG_AUDIT`, or `RAG_BACKEND_AUDIT`. Use existing receipts if applicable, do not rerun expensive gates just for another review label. Every open CRITICAL/HIGH/MEDIUM blocks READY_FOR_OWNER_COMMIT. LOW/INFO are queued for milestone/audit; no self-review counts as independent review.

# Independent review — [ID / scope]

- Date / reviewer / HEAD / source hash: [verified]
- Scope / Owner approval / baseline: [links]
- Status: READY_FOR_OWNER_COMMIT | CHANGES_REQUESTED | OWNER_DECISION_REQUIRED

## User requirement and simplest-mechanism check
[Does this solve every approved behavior? Is any unsolicited machinery present?]

## Finding register
| ID | Severity | Blocking? | Evidence: path and reproducible behavior | Minimal required correction | Status |
| --- | --- | --- | --- | --- | --- |

## Acceptance matrix
| Requirement ID | Result: PASS/FAIL/NOT_VERIFIED/N/A | Evidence/limit |
| --- | --- | --- |

## Verification
[Exact executed commands/exit/test counts, source applicability; distinguish reused vs fresh.]

## Scope and hygiene
[Unrequested features, files, dependencies, migration, logs/secrets, links.]

## Owner next action
[One clear action: authorize fix, answer question, or commit/push reviewed changes. No separate staging-review round.]

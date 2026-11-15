# Findings, disposition, remediation batching and proof contract

**Status:** PREPARATION TEMPLATE. No actual finding is asserted or closed by this file. Preserve all original review logs, failures, counter values and historical reports.

## 1. Register schema — no ambiguous “fixed” status

Every audit observation is an object containing:

| Field | Requirement |
|---|---|
| `finding_id` | Stable scoped identifier `NR-H-###`, `NR-D-###`, `NR-B-###` or `NR-X-###`; never recycle |
| `type` | `DEFECT` / `MANDATORY_EVIDENCE_GAP` / `DESIGN_PROPOSAL` / `OBSERVATION` / `FALSE_POSITIVE` / `DUPLICATE` |
| `severity` | `CRITICAL/HIGH/MEDIUM/LOW/INFO`; impact-based, not formatting-based |
| `confidence` | HIGH/MEDIUM/LOW + uncertainty conditions |
| `requirement_and_authority` | Exact Owner requirement or accepted contract; distinguish source fact from Owner intent |
| `scope` | Exact path, line/operation/test ID and published HEAD/blob where available |
| `reproduction` | Steps/test command and input, or code-path argument with falsifiable counterexample |
| `observed_vs_expected` | What actually happens versus what must happen; avoid speculative claims |
| `root_cause` | Minimal causative mechanism; one root cause may cover many symptoms |
| `risk` | Concrete behavior, users affected, likelihood and constraints; no exaggerated severity |
| `minimal_fix_options` | Keep/current vs smallest plausible correction; evaluate regressions/effort |
| `owner_approval_gate` | Exact API/schema/ADR/frozen/security/role changes needing Owner ratification |
| `evidence` | Commands, exit/status, versions, logs, tests, URL/commit path, reviewer identity |
| `disposition` | See state machine below; no unsupported closure |
| `remediation_batch` | Bounded approved batch ID; source/test/docs allowlist, counter |
| `independent_verification` | Reviewer session, actual SHA/diff, fresh/reused tests, residual risks |

**Artifact evidence may be a read-only citation or a bounded ignored log path.** Do not fabricate screenshot filenames, success counts, test commands or commit SHAs. Do not paste secrets or private data into reports.

## 2. Severity ≠ eligibility to ignore

| Severity | Typical impact | Required treatment for this final audit |
|---|---|---|
| CRITICAL | Confirmed severe compromise, irreversible data loss | Stop/contain, Owner decision, verified remediation |
| HIGH | Major security/data correctness/availability failure | Owner-approved fix and independent verification |
| MEDIUM | Reproducible approved behavior/integration or maintainability defect | Owner-approved bounded fix and independent verification |
| LOW | Real localized defect or audit debt | **If substantiated/actionable**, fix/verify or stop for explicit Owner decision; not automatically waived |
| INFO | Real actionable documentation/tooling consistency issue | Same actionable rule; purely informative observations are NOT_A_DEFECT/proposal rather than forced source edits |

A lower-severity item is not automatically a finding. Correct a typo only if it impairs current authoritative guidance, code understanding or agreed documentation quality; avoid endless style churn. The 2026-11-01 Owner intent requires **every real, actionable, in-scope item at any severity** to have valid final disposition. It does not demand code changes for deliberate trade-offs, valid historical facts, false positives, duplicates or out-of-scope features.

## 3. Disposition state machine

```text
DISCOVERED
  -> EVIDENCE_PENDING -> VERIFIED_ACTIONABLE -> OWNER_APPROVED_BATCH
  -> IMPLEMENTED_UNREVIEWED -> INDEPENDENTLY_VERIFIED -> OWNER_PUBLISHED -> FIXED_AND_VERIFIED
  -> NOT_A_DEFECT / DUPLICATE / NOT_APPLICABLE (supported by evidence)
  -> OPTIONAL_PROPOSAL (not an accepted defect; separate product approval)
  -> OUT_OF_SCOPE_WITH_AUTHORITY (not falsely closed as in-scope)
  -> STOP_OWNER_DECISION_REQUIRED (real finding cannot be completed under allowed constraints)
```

Do not label something `FIXED_AND_VERIFIED` after implementer self-review or unit tests only. An independently accepted fix still needs real Owner publication/actual-SHA qualification where completion requires published source. If a verified finding cannot be fixed, document blockers and STOP; no severity-only waiver.

## 4. Root-cause batch selection and finite remediation

Prioritize unsafe PUBLIC disclosures, data corruption, current authority violations and production correctness, then reproducible maintainability drift. Group items by same mechanism/file ownership/transaction boundary and tests, not simply by severity. Distinct API/schema/frozen permissions remain separate Owner decisions even if same batch is convenient.

For each proposed batch record:

- `batch_id`, owner consent, implementation session ID, reviewer session ID (different), current published HEAD, exact allowlist and forbidden paths.
- Before/after contract and rollback if an integration fails; selected tests, negative tests and static validation; tests requiring unavailable services marked conditional.
- Max correction rounds **proposed**: three, unless Owner chooses another finite bound. Initial implementation + self-review is not a correction round. Existing historical counters **do not reset or transfer** to audit batches.
- On reviewer `CHANGES_REQUESTED`, author may correct within the same approved allowlist and remaining budget; new material scope requires new Owner permission.
- On third failed corrective review, unresolved actionable defect or mandatory gap: `STOP / OWNER_DECISION_REQUIRED`, not automatic takeover (Codex is already implementer) and not silent downgrade.

## 5. Regression, source qualification and evidence accounting

- Record exact test method/parameterized invocation identity, suite/build, exit code and path; distinguish fresh independent tests from source-applicable reused tests.
- Never double-count overlapping prior runs. Reused evidence only qualifies when audited source, relevant dependencies, configuration and tests have not materially changed; provide a dependency/risk explanation.
- A full-suite claim requires full suite actually executed, not review of a narrow acceptance set. Live operational readiness requires real environment evidence.
- If artifacts/receipt cannot be reproduced, status is `NOT_VERIFIED`, not pass-through approval. Source hash drift invalidates applicable evidence and triggers targeted rerun.

## 6. Mandatory backlog reconciliation

Requalify preserved LOW/INFO backlog identifiers from DP-A/B1/W1/RO-A/RO-B/V1 and earlier accepted maintenance (including historical test-hygiene, misstatements and stale docs) against current code and product. Preserve original backlog provenance and current status until evidence permits new disposition. Close with minimal fixes **only** when actionable and Owner-approved. Do not edit the original dated review to erase a finding or falsely add an old test result.

## 7. Audit completion caveat

A result may be `NON_RAG_BACKEND_AUDIT_READY` only if every applicable coverage row has evidence or an Owner-adjudicated limitation, all substantiated actionable findings (including LOW/INFO) have verified final dispositions, all affected docs/ADR/DBML/diagrams are reconciled on final published HEAD, and no mandatory acceptance is unverified. If a real unresolved issue remains, result is STOP rather than READY even if trivial.

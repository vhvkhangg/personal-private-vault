# BF26-NR-H1 — Dated Owner ratification of bounded harness remediation

**Authorization ID:** `BF26-NR-H1-HARNESS-REMEDIATION`  
**Owner decision date:** `2026-11-16` (`Asia/Ho_Chi_Minh`)  
**Decision:** `OWNER_RATIFIED_2026-11-16 / H1_READINESS_PENDING_PUBLICATION_AND_ENTRY_GATES`  
**Execution before publication:** **`H1_EXECUTION_NOT_AUTHORIZED / STOP`**  
**Ratification baseline (H0 closeout on published `main`):** `0d399b2cacfed2ee064406e3c521b8a33bea7309` — `docs(audit): preserve independent H0 review provenance`.

## 1. Owner decision, basis and precedence

The Owner has directed preparation of the H1 ratification following the independent H1 **docs-only** preflight result `READY_FOR_OWNER_RATIFICATION / H1_DOCS_ONLY` (reported on 2026-11-16, H1P-01–14 PASS with documented qualifications). The preflight is a review of the **seven H1 preparation files**, not of H1 implementation or native model/hook behavior; its original output was provided by the Owner in the conversation, not misrepresented here as a newly authored repository review or native test receipt.

The prior H0 independent review is preserved at [the source-attributed review record](../2026-11-15-bf26-nr-h0-harness-readonly-audit/reviews/2026-11-16-h0-independent-review.md), and its five producer reports were published separately. The current GitHub `main` commit named above contains that preservation record. **The H1 executor must requalify the actual published main SHA and local source state; a ratification-baseline SHA is not an immutable demand that HEAD remain at that older commit after this authorization is published.**

This **new, separately dated Owner decision** selects the already-preflighted recommended options in [the H1 decision draft](h1-owner-decision-draft.md), [17-path allowlist](h1-exact-path-allowlist.md), [Codex H1 author handoff](h1-codex-author-handoff-draft.md) and [acceptance contract](h1-preflight-and-acceptance.md). The older documents' `DRAFT` / `NOT_APPROVED` / H0-publication-pending headers describe their **pre-ratification checkpoints**; this record supersedes those headers **only after Owner publication**, without silently modifying their detailed restrictions. If substantive requirements conflict or the seven reviewed documents have materially drifted, **STOP** and obtain an independent review plus an amended Owner decision. This record is not H1 implementation evidence.

## 2. Ratified H1-D01–H1-D12 decisions

| ID | Ratified choice and limit |
|---|---|
| H1-D01 | **ONLY** address `NRH0-F01` (current Non-RAG skill + reachable callers), `NRH0-F02` (semantic Git branch-delete protection gap), and independently narrowed `NRH0-F03` (current operational guidance). No opportunistic refactor. |
| H1-D02 | Rewrite the **existing** `.agents/skills/codex-backend-audit/SKILL.md` under its existing skill name for **current Non-RAG H/D/B/X** audit roles only; any runnable `phase-15` request must fail closed with immutable history reference. No new audit skill. |
| H1-D03 | Use the **existing skill-local audit report asset** and explicit scope-specific routing/handback; preserve ordinary non-audit Antigravity implementation/review workflows. Do not rewrite generic templates by default. |
| H1-D04 | Approve **up to the 17 exact existing-file paths** listed in [allowlist §A](h1-exact-path-allowlist.md), as a **ceiling, not a quota**. All `CONDITIONAL`, `DEFER` and other paths are **read-only**. If an actually reachable shared `COMMON` / template / workflow consumer cannot be made consistent within those 17, **STOP; request an exact additional-path amendment and independent re-review** before making that edit. |
| H1-D05 | Fix F02 in the existing semantic hook evaluator and tests: parsed Git branch deletion with supported `-C`/`-c`/absolute-executable and PowerShell/cmd forms must deny; deletion must never receive an Antigravity permission override. Preserve benign branch listing/status/diff and literal/argument-boundary distinctions. No Git deletion is to be executed in the real checkout. |
| H1-D06 | Fix F03 **only** in current operational assertions/navigation, with links to canonical current state. Preserve explicitly labeled historical `backend/AGENTS.md` sections, earlier accepted findings/counters and frozen evidence verbatim. |
| H1-D07 | Permit a **bounded native Codex qualification** only as specified in §4 below. A Codex-service call via the existing authenticated CLI for fixed H-T07/H-T09/related native permission checks is authorized within a disposable fixture; application/external providers and other network traffic remain forbidden. Unobserved model/hook properties remain `NOT_VERIFIED`; do not claim native PASS from static tests. |
| H1-D08 | **No edits** to `.codex/config.toml`, `.codex/rules/owner-git.rules`, global Codex configuration, trust, approval or sandbox policy; no bypass to make a test pass. Any such necessity requires separate Owner decision and reviewed scope. |
| H1-D09 | `delivery/` remains **unchanged**. It may historically have held review ZIPs, but an automatic producer/trigger is not proven. No ZIP creation, auto-packaging or additional archive-export authority in this H1. |
| H1-D10 | Ratify **maximum three** H1-specific *author correction + independent re-review* pairs after the initial H1 implementation/self-review. Each requires an actual reviewed correction, tracked in the H1 handback; a review with no corrective author action is not retroactively counted. At exhaustion, STOP and seek Owner decision. No historical budget transfer/reset. |
| H1-D11 | **Codex-only for this audit**: fresh H1 author session, different H1 independent reviewer session. Author self-review is not independent acceptance. **Owner alone** performs Git stage/commit/push/PR/publication. |
| H1-D12 | H1 acceptance and Owner publication **do not** authorize D0, B0, X, backend/product changes, RAG, Frontend or provider activation. Every future phase requires its own exact Owner approval. |

## 3. Exact source and report write boundary

The **only** existing paths permitted to change are the **17 literal paths** in `h1-exact-path-allowlist.md` §A, with the purpose of the associated F01/F02/F03 finding. Verify each path and its baseline before editing; changing none or fewer than 17 is valid. All other existing tracked files, including the seven reviewed preparation documents and this ratification, must remain unchanged during H1 implementation.

The H1 author may create **only three new tracked handback documents**, initially absent, under this same H1 preparation directory:

1. `h1-implementation-handback.md`
2. `h1-self-review.md`
3. `h1-source-test-evidence-index.md`

Check each destination is absent before creation; no overwrites or renames. Optional *new* ignored machine receipts may be written under `logs/bf26-nr/h1/<unique-run-id>/` **only after** `git check-ignore` verifies the exact destination and no-clobber/secret-redaction protections are in place. The H1 author may create and clean up **only its owned disposable test fixtures outside the repository**; do not alter unrelated temporary data. The independent reviewer returns its assessment **in chat only** unless a further explicit Owner grant allows a distinct review-file path. No general `logs/` editing or `delivery/` writing permission is granted.

## 4. Scoped diagnostics and network permission (H1 only)

**Remote publication entry check:** After validating `origin` is precisely the approved `vhvkhangg/personal-private-vault` repository, the only allowed Git network metadata operation is:

```text
git ls-remote --heads origin refs/heads/main
```

It must be read-only, without prompt/credential exposure, object download, `fetch`/`pull`, local-ref mutation or communication with another remote. The executor must compare returned remote SHA with `HEAD` and `origin/main`. Failure or unverifiable response is `BLOCKED_H1`, not a reason to substitute cached refs.

**Native qualification exception:** Within a **new disposable fixture outside the PPV worktree**, the authorized H1 author may run **at most 20 bounded native invocations**, each with a hard **120-second maximum wall-clock** where enforceable, using the **already installed and already authenticated Codex CLI** to call **only its normal configured Codex model service** for the narrowly defined legacy refusal, current H/D/B/X phase/Owner grant/session role/output tests, and related native hook/approval checks. Use existing safe bounded-command tooling when applicable. Record actual argv, target fixture, prompt/scenario identifier, CLI version, start/end/exit, model information only if observed, receipts and timeouts; redact secrets and personal paths before publication. Fixture must have no production secrets, no real remote origin, no application provider credentials and no live Owner data. Do not invoke a historical Phase 15 audit in the real checkout, write real application artifacts or execute destructive commands as a probe. Tests of forbidden commands use **text data, classifier parsing, or harmless pre-dispatch probes** with no real Git mutation. Do not run a model/provider download or install and do not change CLI authentication, selected model, trust, hooks, policy or global settings.

This exception does **not** permit general HTTP/web/API calls, OpenAI/provider-account inspection, third-party endpoints, package installs, network-bound application tests, runtime infrastructure, or unbounded agent loops. Network operations beyond the two narrow purposes above are forbidden. If the actual client cannot safely enforce fixture, network or duration restrictions, **STOP / OWNER_DECISION_REQUIRED** before running that diagnostic. Static/offline verification may continue only if otherwise safe, but missing mandatory native H-T07/H-T09 evidence prevents full H1 acceptance and must be escalated; never label it PASS by inference.

**Other tests:** Existing hook Python unit tests, `codex execpolicy check` (parser-only evidence), finite benign shell/file/link/graph checks and strictly read-only environment diagnostics are allowed. Do not run full Maven/application test suites, Docker, deployment, external providers, or Git mutation anywhere in the real checkout. Existing watcher/bounded-command regressions rerun only when a relevant dependency actually changes. Treat model preference, hook presence and parser success as insufficient proof of native enforcement; unsupported tool transports/alias/encoded/GUI/MCP cases remain qualified.

## 5. Explicit activation gate and session boundary

H1 may execute **only if ALL conditions below hold**:

1. The five H0 producer reports and exact-original independent H0 review provenance record are published on `main`. The last confirmed publication baseline for this ratification is `0d399b2cacfed2ee064406e3c521b8a33bea7309` (requalify actual remote `main` at H1 entry).
2. The seven H1 preparation files have their **independent preflight** result `READY_FOR_OWNER_RATIFICATION / H1_DOCS_ONLY` without unresolved blocking findings, and this ratification does not materially change the reviewed limits. If a delta review finds a material expansion or contradiction, STOP until it is separately resolved and reviewed.
3. Owner **publishes** the seven reviewed H1 preparation documents **plus this new dated ratification** on `main`, then records/verifies the **actual new published SHA**. The mere presence of untracked files or a READY preflight message is not publication.
4. A **fresh, distinct Codex H1 author session** confirms the publication, exact allowlist, empty new report destinations, actual effective client capabilities, `ACTIVE=NO_ACTIVE_HANDOFF`, and no conflicting current approval or unqualified worktree drift. H1 must not use the historical Phase 15 skill as its bootstrap, nor edit H0 files.
5. All client/fixture safety and network limits in §4 can be honored. Otherwise STOP before source edits if the required gate cannot be met safely.

**Before steps 1–5 are proven:** `H1_EXECUTION_NOT_AUTHORIZED / STOP`. Once proven, the grant is **only** the bounded H1 task and write/test limits here, not future phase authority. H1 author ends `IMPLEMENTED_AWAITING_INDEPENDENT_REVIEW` or `BLOCKED_H1`, never self-accepts or publishes. An independent H1 reviewer must separately review evidence and diff, including all actionable LOW/INFO disposition; Owner publication comes later. There is no automatic transition to D0/B0.

# B3 skill and workflow audit — 2026-10-27

**Source:** `3d07c7f15daf290f8e23d035a214716084f45ee9` on `vhvkhangg/personal-private-vault`. All ten workflow skill source blobs were individually fetched from `main` and their exact Git blob SHA-1s matched the locally reconstructed pre-edit files. The remaining 28 technical/domain skills were inventoried from the prior Owner ZIP and cross-checked against current GitHub names, metadata and source blob identities; several have content changes during Maintenance B, and should not be replaced from the older ZIP. This audit does **not** certify an exhaustive runtime re-review of all 28 unchanged specialized skills.

## Findings addressed in B3

- Two active technical skill headers (`backend-testing`, `bounded-command-execution`) still described accepted B1 as awaiting review and B2 as pending. **B3:** change those status notices only, preserving runner semantics/examples and accepted evidence.
- The `codex-create-handoff`, `codex-implementation-takeover` and `antigravity-implement-handoff` skill prose contained outdated B1/B2 lifecycle descriptions. **B3:** update current notices to published truth while preserving historical phase references and all substantive implementation acceptance.
- `codex-final-review` still made the Owner relay every routine phase result through ChatGPT for another prompt. **B3:** clarify that ChatGPT is needed for actual documentation/Owner decisions, not as a prompt relay.


- Workflow prompts previously had uneven last-mile navigation: `codex-implementation-takeover` lacked a fully structured *next application/skill* line; `codex-milestone-review`, `codex-review-coverage` and `antigravity-self-review` did not use the same post-run routing shape. **B3:** append one consistent action contract to ten workflow skills, with mode-specific routing.
- Repeated session capacity failures previously required custom ChatGPT prompts and risked resetting counters or launching new handoffs. **B3:** exactly one English recovery prompt per relevant skill, common context/evidence/provenance rules, reuse of valid work, and explicit STOP for invalid CLI hook trust/config.
- `codex-implementation-takeover` still used **“both”** in its evidence instruction despite **3 remediation rounds**. **B3:** requires references for all three; historical round evidence unchanged. Original MA-A05 LOW remains historical; an independent reviewer should classify closure rather than silently alter prior review records.
- Archived phase descriptions can still be mistaken for live status if an agent reads old historical headings. **Mitigation:** all new route sections require fresh `ACTIVE.md`, Owner scope and last dated review; no broad rewrite of historical records.
- Some specialized technical skills still have obsolete illustrative OCR/GPU/Phase 18 wording or early Maintenance B “pending” headers. Their historical references do not create current authorization; propose a separate, bounded documentation cleanup only if a concrete active-scope conflict is found. Do not churn all 38 without evidence.

## Owner-approved documentation synchronization amendment (mechanism A only)

The existing `documentation-writing` specialist skill now participates in an on-demand [Docs Sync Check](../../../workflow/documentation-synchronization.md); `.agents/rules/documentation.md` points to the same contract. All 10 workflow skills reference it at the appropriate handoff/review boundary. The 10 existing resume prompts inherit the check through `COMMON.md`; they are not duplicated. This **amends** the B3 change inventory, but does not make the 38-skill audit a runtime review of every unchanged specialized skill.

Historical/current-state drift already observed elsewhere in `docs/` is intentionally **not fixed** in this amendment. Actual cleanup B is deferred until B3 acceptance/Owner publication and its own scope authorization. An independent reviewer should verify that scoped B3 docs/skills consistently implement the contract; it should not treat a non-applicable repository-wide rewrite as a mandatory B3 acceptance test.

## Pre-review harness consistency corrections (2026-10-27)

- B3 authorization in the root `AGENTS.md` current notice conflicted with later explicit Owner approval. The notice now scopes B3 as docs-only, pending independent review; Phase 19 and cleanup B remain unauthorized.
- The ten copy/paste resume prompt **fenced bodies** now explicitly load `COMMON.md` and the Docs Sync contract, recover the last applicable docs checkpoint and output the agreed **five-field** `Next step` contract. This does not create automatic recovery, new retries or new gates.
- `COMMON.md` now stops on CLI configuration/hook startup error with diagnostics; it no longer suggests that merely informing the Owner authorizes a config repair.
- Existing Codex final-review and create-handoff skill assets plus the canonical review/handoff templates now expose optional, scope-bounded documentation/Owner-publication checks; the legacy `CHANGES REQUIRED` template status was replaced with current `CHANGES_REQUESTED`.
- No runtime, native hooks, archived evidence, historical acceptance, Phase 19 or repository-wide cleanup B changes.

## Complete 38-skill inventory

Every source name and blob SHA was retrieved from latest GitHub `main`; ten workflow skill blobs match the reconstructed sources **exactly** before B3 edits. For 28 specialist skills, source and metadata checks are proportionate and the older Owner ZIP is used only for content scanning where hashes match; changed B1/B2 helpers are not overwritten.

| Skill | Source identity | B3 action |
| --- | --- | --- |
| `account-domain-modeling` | exact current blob | KEEP (domain/utility helper) |
| `antigravity-implement-handoff` | exact current blob | B3 route + recovery |
| `antigravity-self-review` | exact current blob | B3 route + recovery |
| `antigravity-test-slice` | exact current blob | B3 route + recovery |
| `architecture-change` | exact current blob | KEEP (domain/utility helper) |
| `authentication-security` | exact current blob | KEEP (domain/utility helper) |
| `backend-integration-portability-storage` | exact current blob | KEEP (domain/utility helper) |
| `backend-testing` | prior ZIP differs; current GitHub blob checked | KEEP; B1/B2-updated, not reconstructed from old ZIP |
| `bounded-command-execution` | prior ZIP differs; current GitHub blob checked | KEEP; B1/B2-updated, not reconstructed from old ZIP |
| `codex-backend-audit` | exact current blob | B3 route + recovery |
| `codex-create-handoff` | exact current blob | B3 route + recovery |
| `codex-final-review` | exact current blob | B3 route + recovery |
| `codex-implementation-takeover` | exact current blob | B3 route + recovery |
| `codex-milestone-review` | exact current blob | B3 route + recovery |
| `codex-pre-handoff-review` | exact current blob | B3 route + recovery |
| `codex-review-coverage` | exact current blob | B3 route + recovery |
| `collection-domain-modeling` | exact current blob | KEEP (domain/utility helper) |
| `database-migration-review` | exact current blob | KEEP (domain/utility helper) |
| `design-pattern-selection` | exact current blob | KEEP (domain/utility helper) |
| `documentation-writing` | exact current blob | B3 A amendment: scope-bounded Docs Sync Check; no historical rewrite |
| `feed-import-workflow-modeling` | exact current blob | KEEP (domain/utility helper) |
| `fiction-domain-modeling` | exact current blob | KEEP (domain/utility helper) |
| `film-domain-modeling` | exact current blob | KEEP (domain/utility helper) |
| `finance-journal-personal-domain-modeling` | exact current blob | KEEP (domain/utility helper) |
| `global-search-domain-modeling` | exact current blob | KEEP (domain/utility helper) |
| `graphify-context` | exact current blob | KEEP (domain/utility helper) |
| `java-spring-coding-standards` | exact current blob | KEEP (domain/utility helper) |
| `jpa-postgresql-persistence` | exact current blob | KEEP (domain/utility helper) |
| `knowledge-domain-modeling` | exact current blob | KEEP (domain/utility helper) |
| `local-rag-engineering` | exact current blob | KEEP (domain/utility helper) |
| `location-domain-modeling` | exact current blob | KEEP (domain/utility helper) |
| `media-domain-modeling` | prior ZIP differs; current GitHub blob checked | KEEP (domain/utility helper) |
| `modular-monolith-architecture` | exact current blob | KEEP (domain/utility helper) |
| `people-domain-modeling` | exact current blob | KEEP (domain/utility helper) |
| `pragmatic-solid-design` | exact current blob | KEEP (domain/utility helper) |
| `python-rag-coding` | exact current blob | KEEP (domain/utility helper) |
| `rest-api-http-contracts` | exact current blob | KEEP (domain/utility helper) |
| `reuse-and-consistency` | exact current blob | KEEP (domain/utility helper) |

## Scope-based decisions

| Existing component | Decision | Reason |
| --- | --- | --- |
| 10 direct/helper `codex-*` and `antigravity-*` skills | MODIFY minimally | Post-run navigation + bounded resume entry + central Docs Sync Check link; no new skill for recovery |
| `backend-testing`, `bounded-command-execution` | MODIFY status notices only | Current headers claimed B1/B2 unfinished; do not change tested command behavior |
| `documentation-writing` specialist skill | MODIFY minimally | Existing documentation entry point links the Docs Sync Check; no new skill |
| Remaining 25 specialist/domain skills | KEEP; review applicability before invoking | Domain guidance already has a parent workflow; adding resume prompts to each would inflate context and confuse ownership |
| Documentation rule | MODIFY minimally | Reference the shared contract for approved docs writes; no cleanup permission |
| Remaining repository rules, two agents | KEEP | Existing Owner policy/guard still controls; do not duplicate current status in every rule |
| Codex/Antigravity native hooks, Python watchdog | KEEP unchanged | Maintenance B independently accepted; docs-only B3 does not reopen security tooling |
| `docs/workflow/resume-prompts/` | ADD | One discoverable, English-only library; no executable bootstrap script |
| Phase 17/18 historical records, `logs/` | KEEP untouched | Immutable review/evidence, no speculative cleanup |

## External references consulted, not installed

1. [Agent Skills specification](https://github.com/agentskills/agentskills/blob/main/docs/specification.mdx): progressive disclosure and short `SKILL.md` with on-demand resources. **Adopt:** keep recovery prompts in separate Markdown files and short links in current skills.
2. [obra/superpowers — executing-plans](https://github.com/obra/superpowers/blob/main/skills/executing-plans/SKILL.md): source-of-truth briefs, task state and evidence ledger before continuation. **Adopt selectively:** verify baseline and completed evidence on resume; **reject:** its own Git commit steps and full framework because Owner alone publishes.
3. [github/awesome-copilot](https://github.com/github/awesome-copilot): explicit agent handoffs and short next actions. **Adopt:** actionable next application/command; **reject:** editor-specific handoff buttons, unsupported in this workflow.
4. [anthropics/skills](https://github.com/anthropics/skills) and [OpenAI skills guidance](https://developers.openai.com/blog/rethinking-skills-and-prompts-for-gpt-6-astra): use precise metadata/resources, avoid long repeated prompts. **Adopt:** no 38 copies of a generic resume system and no unnecessary new agents.

## Verification limits

- Static Markdown/source audit and per-skill SHA comparison only. No independent Codex review, actual resumed Codex or Antigravity session, native hook interception, full Java/Python suite or preexisting `logs/` hash was performed by this docs-only package.
- LOW backlog B1-L01/B2-L01/MA-A03–A06 remains recorded in earlier independent reports; this maintenance does not automatically close those historical findings.

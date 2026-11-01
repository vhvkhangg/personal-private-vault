# Universal Inbox / Save for Later — Owner-approved preparation

**Date / starting baseline:** 2026-11-01 / `main` [`84f11854de20cde4dcdcd7b082b4c40c1410885b`](https://github.com/vhvkhangg/personal-private-vault/commit/84f11854de20cde4dcdcd7b082b4c40c1410885b)  
**Status:** `READY_FOR_HANDOFF / PREPARATION_ONLY / IMPLEMENTATION_NOT_AUTHORIZED`  
**ACTIVE:** `NO_ACTIVE_HANDOFF`

**Independent preparation review:** [2026-11-01 Codex review](reviews/2026-11-01-independent-pre-handoff-review.md) selects `KEEP_REUSE_ONLY`: one additive Feed-owned Inbox browsing slice, reusing the existing Vault batch qualification API. No blocking preparation findings or required Owner decisions remain for that default. The review resolves the submitted design questions and clarifies pagination, cursor validation, compatibility and future evidence gates. Read/archive state and any migration remain excluded and separately Owner-gated; the supporting documents remain prospective design inputs, not implemented behavior.

## Purpose

Design a practical, **single-owner URL/bookmark inbox** over the already accepted `feed.saved_resources` and `vault` metadata/lifecycle capabilities. Scope this one feature before Daily Trends Radar, two-account Google Drive integration, Dashboard/API readiness, the final non-RAG backend audit and Frontend Phase 16. This is a feature/maintenance extension to previously frozen Phase 10 and Phase 13 code, not a reopening of those phases.

**Read first:** [Owner authorization and decisions](owner-scope-and-decisions.md), [source-backed reuse and proposed API design](source-reuse-and-design.md), [acceptance / preparation review plan](acceptance-and-review-plan.md), [Spring-only Archify publication closeout](../2026-11-01-spring-only-archify/owner-publication-closeout-2026-11-01.md), root `AGENTS.md`, [Owner delivery policy](../../../workflow/current-owner-delivery-policy.md), and canonical `ACTIVE.md`.

## Current capabilities versus next feature

Today, the backend already saves URLs manually or from a Feed item, lists recent saved URLs, fetches one item, converts to Knowledge Study/Information/Note, and uses Vault metadata, favorites, tags and recycle bin. Those operations are **already implemented** and must not be rebuilt. An Inbox experience needs reliable **saved-resource-oriented browsing and lifecycle**, with a narrow API gap, and perhaps an explicitly approved read/archive state.

The first implementable scope should remain small enough for **one independently reviewable vertical slice**. No generic Inbox framework, new top-level Spring Modulith module, new external crawler or provider, cross-module repository dependency, asynchronous queue, extra AI/RAG capability, or internet content mirroring. UI/Frontend Phase 16 is not yet authorized.

## Approval gates and workflow

1. **Preparation only now.** Owner applies this docs-only package after the published Spring diagrams baseline. Codex reviews the *new* Inbox scope in maintenance/feature-preparation mode via `$codex-pre-handoff-review`. Codex may adjust **this** prospective design and issue a findings report. It may **not implement source**, write an active handoff, claim READY_FOR_IMPLEMENTATION or rewrite Phase 10/13 history.
2. If `READY_FOR_HANDOFF`, Owner stages/commits/pushes the independently reviewed preparation. If `CHANGES_REQUESTED`, refine only these approved preparation artifacts and repeat independent review. If `OWNER_DECISION_REQUIRED`, stop for the specific documented decision, especially any schema/status addition; **do not auto-authorize a migration**.
3. After reviewed preparation is Owner-published and all material decisions settled, Codex may run `$codex-create-handoff` for a **separately approved bounded feature/maintenance slice** using the existing skill. Antigravity implements/self-reviews; independent Codex `$codex-final-review` accepts or requests corrections. Only Owner stages/commits/pushes.
4. Record the actual published SHA and separate closeout. Only then proceed to Daily Trends Radar. The eventual final non-RAG audit will use the existing `$codex-backend-audit` skill extended prospectively, as recorded in [Owner intent](../2026-11-01-final-non-rag-audit-intent/owner-intent.md).

## Execution exclusions

No production code/schema/SQL/migration/config/test edits from this preparation. No RAG, Ideaverse, Drive OAuth, Daily Trends provider HTTP fetches/scheduler, downloader, full-text webpage copying, Hermes/OpenClaw or frontend scaffold. S19-2 continues `SUSPENDED / NOT_ACCEPTED / ARCHIVAL_ONLY`. No automatic retry of QA or native/model evaluation.

**Next step:** Owner | commit/push reviewed preparation, then invoke Codex `$codex-create-handoff` | reuse-only Universal Inbox based on published `84f1185` and the independent preparation review above | reviewed preparation must be Owner-published before the separate handoff; no implementation/schema/read/archive authorization from this review | Inbox implementation/remediation not started; future 0/3, no borrowed/remapped RAG counters.

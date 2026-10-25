# Owner decision register — complete for Maintenance A

**Status:** **ALL MATERIAL DECISIONS RESOLVED / Maintenance A DOCS GOVERNANCE AUTHORIZED.** No new Phase 19 feature handoff, implementation, RAG activation or frozen Phase 17/18 source change authorized. These are Owner confirmations from the 2026-10-25 discussion; the old questionnaire was superseded, not historically reinterpreted.

| ID | Owner decision | Application / boundary |
| --- | --- | --- |
| O-01 | Permanently personal single-account web app with local/offline RAG; remote Owner access stays supported; choose smallest sufficient solution | No enterprise agent/infrastructure; preserve owner-requested behavior and accepted privacy/integrity |
| O-02 | **Ideaverse is only an example** of ordinary `.md`; do not implement Ideaverse integration | No special Ideaverse source, pipeline or new phase feature |
| O-03 | Manual `.md` snapshot import/reindex; no watcher | Ordinary Markdown and Obsidian `.md` accepted only as approved source with immutable snapshots |
| O-04 | Broken/unapproved Markdown child/embed -> reject affected whole file; independent files may continue | Do not partially index an invalid root; preserve original fail-closed behavior |
| O-05 | Markdown is for **search and cited QA**, NOT similar-note recommendations | Q&A may use owner-approved Markdown and permitted Fiction/Film sources; user can select scope |
| O-06 | Fiction/Film recommendation by **prompt** or an existing named item | Existing Fiction -> similar Fiction; Film -> similar Film; free-form prompt may select Fiction, Film, or both; only real eligible catalog records |
| O-07 | Private/NSFW denied by default, unless explicit per-file consent under existing rules | No inferred permission from normal indexing; preserve consent/purge/security |
| O-08 | Simplify implementation if accepted behavior and necessary safety remain equivalent | Frozen Phase 17/18 contracts never silently weakened; any actual migration requires separate maintenance authorization |
| O-09 | No automatic LLM judge, new human adjudication or excessive evaluation apparatus | Keep modest tests of retrieval relevance, grounding/citation, abstention, recommendation correctness, security/privacy/multilingual |
| O-10 | Regular command future **300s wall cap**; preapproved large/complex verification up to **2,100s** | Regardless of command class, after **300s without progress/output**, diagnose logs/CPU/I-O/threads; no blind kill merely for silence; owned process tree only; runtime change separate Maintenance B |
| O-11 | Short Codex narration, short console log/diff; full logs kept on disk | `.codex/config.toml` already has `model_verbosity=low` but tool output is separate; preserve exact receipts |
| O-12 | `logs/` stays **unchanged in project** | No cleanup, move, delete, truncation or upload requirement |
| O-13 | Antigravity implements/tests and **always self-reviews**; Codex mainly preparation/handoff/review | Antigravity self-review not an independent review; three remediation + independent re-review rounds **per active slice** before takeover |
| O-14 | Independent Codex review after **each slice** | Owner commits/pushes individually after no blocking findings; slice closeout brief |
| O-15 | Independent Codex **phase final review** after all slices | Reuse valid slice evidence, avoid redundant full re-execution |
| O-16 | Codex milestone after **every three completed RAG phases** | Next after P19 covers 17–19; next after P22 covers 20–22; Phase 19 remains unauthorized |
| O-17 | After RAG completion, one RAG-wide audit; after both RAG+Backend, a separate integrated audit | Reuse existing evidence and minimize duplicate execution; no audit self-authorization |
| O-18 | CRITICAL/HIGH/**MEDIUM** findings BLOCK commit | LOW/INFO recorded, addressed together when justified at milestone/audit |
| O-19 | Only Owner stages/commits/pushes; agents blocked from publishing/destructive Git | No separate staged-tree agent review; project execpolicy is additive and requires Windows CLI validation |
| O-20 | Unified templates under `docs/templates/`; ChatGPT edits docs directly | No patch/closeout scripts for docs; preserve immutable historical receipts |
| O-21 | Human adjudication old UI/forms can be retired but **relevant accepted evidence stays** | Current D17 historical package manifests still reference them; do not invalidate them in this documentation-only delivery |
| O-22 | Phase 19 **NOT STARTED** until separately explicitly authorized | No auto-handoff or runtime implementation in Maintenance A |

**No remaining Owner question is required for Maintenance A.** Any newly discovered fact that would weaken accepted functional/privacy guarantees must be returned as a bounded Owner decision **before code change**, not guessed by an agent.

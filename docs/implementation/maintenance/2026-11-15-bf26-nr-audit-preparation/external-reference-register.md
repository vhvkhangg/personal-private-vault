# External reference register — advisory, version-sensitive

**Not a PPV source of authority.** These public references inform *future audit questions*. Official product guidance can evolve; future Codex sessions must verify availability, version and applicable runtime. No assertion here means the Owner's local client was tested.

| Ref | URL | Limited relevant claim | Application to PPV |
|---|---|---|---|
| ER-01 | https://help.openai.com/en/articles/20001275-chatgpt-work-and-codex | GPT-6.1 Sol is among models for Work/Codex; rollout/account eligibility varies | Ask which model actually runs; never assume CLI support, context or available settings |
| ER-02 | https://developers.openai.com/api/docs/changelog?mw_entry=2026-09-29-99a06ddef837dc53 | GPT-6.1 Sol model/API introduction on 2026-09-29 | Model exists; not evidence of a specific PPV local environment |
| ER-03 | https://developers.openai.com/blog/rethinking-skills-and-prompts-for-gpt-6-astra | Re-evaluate AGENTS, skills and task prompts for bloat on capable models | Hypothesis to measure by regression scenarios; do not delete PPV safety safeguards without proof |
| ER-04 | https://github.com/openai/skills/blob/main/skills/.system/skill-creator/SKILL.md | Skills should be concise, use progressive disclosure, test on real tasks | Keep existing skill routing; move conditional details to references **only if** beneficial |
| ER-05 | https://github.com/openai/codex/blob/main/codex-rs/skills/src/assets/samples/skill-creator/SKILL.md | Skill references and discriminative triggers | Avoid duplicate skills; follow the newer Owner decision to replace runnable Phase 15 mode while preserving historical evidence |
| ER-06 | https://developers.openai.com/cookbook/examples/codex/iterating-development-workflows-with-codex | Separate planning, execution, verification; preserve approvals and minimal artifacts | Independent sessions with scoped writes and evidence, not automatic workflow template imports |
| ER-07 | https://developers.openai.com/cookbook/examples/gpt-5/codex_prompting_guide | Codex AGENTS discovery/injection behavior described in guidance | Verify actual CLI version and nested AGENTS order; no static-assumption PASS |

**External GitHub examples:** may be compared after H0 establishes a concrete deficiency and a search rationale. Third-party repositories are **not** required baselines, compatibility proof or approval of copying their designs. Prefer verified official Codex docs over snippets from unrelated repos.

**Core epistemic rule:** `Published model capability` is not `Owner has access`, `config file value` is not `effective runtime setting`, `hook source exists` is not `hook ran`, `prior accepted test` is not `full current suite passed`, and `source merged` is not `production deployed`.

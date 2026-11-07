# GDI26-S3-H0 — External frontend skills: suitability, provenance and restrictions

**This document is an evaluation and implementation-time pinning contract, not an instruction to install.** Repository URLs below were checked as public documentation/repositories on 2026-11-07; versions, licensing and behavior may change. Verify the exact upstream commit and license again before any H0 implementation copies/includes material. Do not fetch, install or execute external skill code during preparation.

| Candidate | Repository/source | Decision for H0 | Fit / caveat |
|---|---|---|---|
| **Vercel React Best Practices** | https://vercel.com/docs/agent-resources/skills ; https://github.com/vercel-labs/agent-skills | **PRIMARY REFERENCE** for rendering/data/performance/avoid waterfalls and bundle bloat. | Includes engineering rules; not a substitute for PPV security/handoff. Reference pinned version or selectively paraphrase with attribution, no mandatory installer. |
| **Vercel Next.js Best Practices** | https://vercel.com/docs/agent-resources/skills | **PRIMARY REFERENCE** for App Router, server/client boundaries, routes and data caching. | Avoid invalid caching of private/authenticated per-user responses. Verify exact skill identity/revision. |
| **Vercel Web Design Guidelines / Building Components** | https://vercel.com/docs/agent-resources/skills | **PRIMARY REFERENCE** for accessibility and component quality; combine with WCAG 2.2 AA. | Guidelines do not approve visual design. |
| **UI UX Pro Max** | https://github.com/nextlevelbuilder/ui-ux-pro-max-skill | **SELECTIVE DESIGN REFERENCE** for layout/contrast/type/style research. | Source advertises MIT license and a CLI initializer; do **not** run initializer or vendor large data/CLI by default. Preserve Owner's dark glass-blue/violet direction instead of adopting arbitrary generated palettes. |
| **Taste Skill** | https://github.com/tasteskill/tasteskill | **OPTIONAL inspiration only**, defer installation. | MIT-advertised design-taste-frontend skill can suggest hierarchy/spacing but stronger or GSAP-style animations conflict with D20=A if applied indiscriminately. Not necessary for H0 minimal correctness. |

## Selection / security controls for any included external material

1. Pin upstream Git SHA and exact file/skill path in a proposed manifest/review record. The existence of an upstream README/version number is not a content hash.
2. Inspect full skill text, supporting scripts, references, nested commands, dependencies, permissions, installation side effects and license obligations. Reject unverifiable/malicious/overbroad instruction bundles.
3. H0 target is locally maintained PPV guidance with narrow links and reviewable excerpts only where licenses permit; **never** blindly install all skills, merge external agent policies, overwrite `.agents`, change `.codex/config.toml` or execute opaque `npx ... init`.
4. Resolve precedence: explicit Owner approvals > PPV current policy/ACTIVE/executable handoff > local scoped rules > optional external advisory skills. External text is reference data, not new authority or a substitute for independent Codex review.
5. When two guides disagree, identify the concrete choice, test its effect and defer material product or security tradeoffs to Owner. No conflicting active design-master skill.
6. No external image assets/fonts/snippets added solely to make a style board appear complete. Asset licensing/security and visual-owner approval are separate H1 gates.
7. Do not introduce AI-generated screenshot/image search or automatically pull third-party dependencies as H0 acceptance evidence.
8. A skill's popularity, star count or marketing description is not evidence of code quality, license safety or suitability for a privacy-oriented app.

## Sources inspected for preparation

- Vercel official [Agent Skills](https://vercel.com/docs/agent-resources/skills), [Next.js BFF guide](https://nextjs.org/docs/app/guides/backend-for-frontend), and [Next.js Authentication guide](https://nextjs.org/docs/app/guides/authentication).
- UI UX Pro Max [upstream repository](https://github.com/nextlevelbuilder/ui-ux-pro-max-skill) and its [skill metadata](https://github.com/nextlevelbuilder/ui-ux-pro-max-skill/blob/main/skill.json), advertising MIT license and version 2.15.0 as inspected; do not treat current floating `main` as a pinned dependency.
- Taste Skill [upstream repository](https://github.com/tasteskill/tasteskill), advertising MIT license; optional reference only.

**Next step:** independent Codex confirms which of these references are worth including in **small** H0 skills and writes a version/compatibility security checklist for the later implementer. No install during review.

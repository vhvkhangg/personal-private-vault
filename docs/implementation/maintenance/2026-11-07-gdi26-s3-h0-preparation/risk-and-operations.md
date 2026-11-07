# GDI26-S3-H0 — Risk, exclusions and operational ledger

| ID / significance | Risk | Required control / residual |
|---|---|---|
| H0-R01 HIGH | H1 starts before Owner sees/likes the design. | D22 gate: version-bound dated Owner message required before H1 implementation; H0 and H1 skills/handoff must stop if absent. Automated visual tests are not a substitute. |
| H0-R02 HIGH | BFF accidentally leaks backend refresh/JWT or Google tokens to browser, public props, logs or localStorage. | Server-only session/secret boundaries; cookie/CSRF/logout rotation; independent abuse tests at H1. No security implementation in H0. |
| H0-R03 HIGH | S1 host-only OAuth binding cookie lost across Next BFF/port/callback or silently changed. | Document and test browser jar/Set-Cookie/callback path; preserve exact S1 backend text callback; live browser/host proof separate. |
| H0-R04 HIGH | Unreviewed external skill script runs during agent bootstrap or changes existing safety hooks. | Pin and inspect upstream SHA/license/shell commands; no blind skill install; do not edit `.codex/config.toml` or bypass trust. |
| H0-R05 MEDIUM | Hook works in unit tests but not in actual Codex CLI/Antigravity on laptop. | Native negative controls under trusted Windows root; `NOT_VERIFIED` if unavailable; no false safety claims. |
| H0-R06 HIGH | Browser upload loses response then retries with a new idempotency key, creating another Google object. | One immutable action ID/key per intentional upload, same body/name/MIME for replay; expose unknown status, no auto-create on ambiguity. |
| H0-R07 MEDIUM | Glass panels/gradients undermine accessibility/performance or make small screens unusable. | Rendered contrast/keyboard/zoom/blur/motion checks; Owner visual iteration; postpone token freeze. |
| H0-R08 MEDIUM | Too many external skills/agents cause conflicting instructions and overhead. | Minimal selection, one agent, four small skills maximum as proposal, explicit precedence and codex KEEP/SIMPLIFY choice. |
| H0-R09 MEDIUM | H0 silently becomes broad Phase 16 frontend launch, RAG/cloud integration or arbitrary Drive browser. | No `frontend/` in H0, no Google API call, no S3 module reorganization; H1 distinct Owner gate. |
| H0-R10 MEDIUM | Historical S2 default-OFF or provider deployment limitations are mislabeled as certified live behavior. | Keep Drive/files/Feed disabled; distinguish deterministic S2 review from real provider/operational acceptance. |
| H0-R11 MEDIUM | Subsequent H1 changes spring security/callback/streaming to suit frontend convenience. | H1 preparation must flag exact required backend delta for a separately approved review, cannot silently weaken frozen S1/S2. |
| H0-R12 LOW | Unapproved design sources/assets/font licensing or remote dependencies cause drift. | Later Owner review and asset provenance check; no font/image asset vendoring now. |

## Operator/developer lifecycle constraints

- Windows 11/PowerShell root trusted project setup is expected, but not a blanket guarantee that IDE MCP or native hooks are active. Record real machine and command evidence when H0 implementation runs.
- Any upstream external skill addition must be inspectable offline once pinned, with attribution/license where applicable and no unapproved script execution. Prefer built-in docs links over code vendoring.
- Use a package lockfile only when H1 creates `frontend/`, not in H0. Choose package manager/runtime versions from current stable supported upstream docs at H1 preparation, with reproducibility review. Do not assume npm is inferior/superior by default.
- Future local BFF and Spring dev configuration must preserve backend status and callback security; activating Google on a real account, localhost redirect registration, key/spool ACL, HTTPS/remote deployment and network exposure are separate operator gates.
- Logs must preserve actual receipts, exceptions and source hashes; never overwrite historical S1/S2 logs as a “cleanup.” No secrets may be recorded in screenshot metadata, client network traces or test receipts.
- User has asked not to pursue `.codex/config.toml` scope investigation as part of S3 preparation. Preserve its current committed state; this excludes modifying/disabling safeguards, but does not pretend S2 reviewed those unrelated settings.

## Decision routing if ambiguity arises

- **Owner required:** new feature, expansion of Phase 16, real Google/remote deployment, broad Google scopes/Picker, identity/session policy change, new separate browser callback; actual visual approval and any rejected design version.
- **Codex can review as engineering choice:** minimal skill count, exact trigger metadata, naming, rule-vs-skill placement, relative paths and link correctness, conditional hooks when proven necessary, concrete test runner proposal.
- **Future H1 design iteration:** exact colors/typography/spacing/icons/motion curves, layout geometry and responsive behavior; always request explicit visual approval before H1 implementation.

**Next step:** independent preparation review, then Owner commit/push only after `READY_FOR_HANDOFF`; no automatic rollout.

# BF26-DP-RO — governance, independent review and stop conditions

## Status and precedence

`OWNER_RATIFIED_RO_D04_RO_D06 / RO_A_PREPARATION_CORRECTION / FR_RO_PH_01_INDEPENDENT_REREVIEW_PENDING / RO_B_DEFERRED / NOT_SOURCE_AUTHORIZED / NO_ACTIVE_HANDOFF`. [Independent review 1](reviews/2026-11-13-bf26-dp-ro-independent-pre-handoff-review.md) returned `CHANGES_REQUESTED` before the [dated ratification](owner-ratification-ro-d04-d06-2026-11-13.md). Neither the Owner decision nor these revised docs independently closes that historical finding.

**Current source baseline** is the verified 2026-11-13 GitHub main HEAD `3ce4c87d6808c039ffd35ebd7ed67d73659b092c`. W1 source/closeout publication is historical accepted evidence; this new prospective policy supersedes the desired future grants, not those historical test facts. Do **not** modify `ACTIVE.md` during preparation. The un-applied W2 draft has no publication authority and should not be extracted into the repo.

## Pre-handoff review task for independent Codex

Run `$codex-pre-handoff-review` **again** for **BF26-DP-RO-A ONLY** after the Owner applies the corrected docs, before any implementation. Independently adjudicate `FR-RO-PH-01` against the revised contract and 2026-11-13 Owner-ratified RO-D04/D06. RO-B is **DEFERRED** and its wire decision is recorded for later separate preparation; do not label RO-B implementation-ready or require its runtime criteria in RO-A review. Require the reviewer to:

1. Confirm local `main`, HEAD, `origin/main`, remote `main` and worktree versus verified baseline; inspect root/Docs/Backend scoped AGENTS and current Owner policy, HANDOFF ACTIVE, D1–D17, historical W1 final review and closeout.
2. Independently enumerate every currently selected B1 GET method/path pair and W1 Film POST, exact Spring matcher/dispatcher order, actual registered-handler test sources, Auth exceptions and PRIVATE controls.
3. Verify the source-fact table's Git blobs; qualify safe existing full DTO read ownership, `isNsfw`/Vault type/delete invariant and reusable REQUIRES_NEW REPEATABLE_READ boundary. Find all DTO references before proposing deletion.
4. Verify RO-A leaves B1 Fiction six-key/Film seven-key GET responses and reference GETs unchanged; **do not** implement or require new full GET disclosure in RO-A. Owner-approved media-consumption disclosure and 19-key FULL detail belong to **deferred RO-B** only.
5. Confirm RO-D04 is now **fixed, not optional**: future PUBLIC full 18 work properties + present nullable scalar `authorName`/`directorName` = **19**; PRIVATE GET/POST/PUT = existing **18**. Validate ratified missing/ineligible Person/group, Person Vault eligibility, safe 500 on genuine lookup failure, same-snapshot and OpenAPI preservation as **future RO-B contract**, not as a required immediate RO-A runtime gate. Escalate only a newly demonstrated material contradiction.
6. Respect Owner-ratified RO-D06: **RO-A standalone first; RO-B deferred separately**. No combined handoff or umbrella B2-W2/S/V implementation. Check that `FR-RO-PH-01` is closed for the **RO-A preparation** without falsely marking RO-B READY.
7. Trace **RO-A applicable** RO-01–04/15–22 with real persisted Public login/downgrade tokens, no-handler/no-write, unchanged PRIVATE, eight allowed GETs, Auth exceptions, denied routes, generated POST OpenAPI, architecture and source evidence. Clearly defer RO-05–14 plus the RO-B full-GET portion of RO-19; retain RO-B future acceptance mapping without demanding a full-field/name runtime test for RO-A.
8. List **exact RO-A candidate** security/Film POST annotations/root OpenAPI/test/frozen exceptions, separately from deferred RO-B Fiction/Film owner read/DTO/People named capabilities. Do **not** edit source. Flag any new People capability, Vault code, global mapper, schema, migration, additional Public endpoint or module dependency as `OWNER_DECISION_REQUIRED`.
9. Use existing tooling/agents/skills/watchdog and truthful reused evidence. No runtime tests are required just to declare preparation ready, unless source ambiguity demands a bounded diagnostic; label NOT_RUN accurately.
10. Report `READY_FOR_HANDOFF` **for RO-A preparation only** only after `FR-RO-PH-01` is independently adjudicated, no open blocking RO-A finding remains and exact RO-A path/acceptance/source candidates are ready. Do **not** claim RO-B ready; otherwise retain `CHANGES_REQUESTED` with findings and next action.

## Later gate after independently READY preparation

- Owner alone commits/pushes independently READY **RO-A preparation**, recording actual SHA; `ACTIVE` stays `NO_ACTIVE_HANDOFF` at that point.
- Owner separately authorizes **exact RO-A** frozen source/API/POST OpenAPI/test exceptions; Owner has already selected **RO-A first**—no combined RO-A/RO-B scope may be inferred. Ratified product/RO-D04/D06 decisions alone are not source authorization.
- Codex `$codex-create-handoff` creates the executable specification and updates ACTIVE by repository workflow only **after** exact authorization.
- Antigravity `/antigravity-implement-handoff` then mandatory `/antigravity-self-review`, Codex independent `$codex-final-review`, Owner alone stages/commits/pushes accepted source, and actual-SHA publication closeout/navigation sync.
- **Only after independently accepted and Owner-published RO-A source closeout**, initiate *separate* bounded RO-B preparation and independent review against ratified 19-key PUBLIC/18-key PRIVATE D04. Further exact Owner source/API authorization is still required for RO-B.
- B2-W2 Public writes, B2-S/V, DP-C, Recycle Bin, final Backend audit, Frontend/H1-F, RAG and providers remain unapproved. Never infer authorization for the next slice.

## Docs Sync and historic counters

| Dimension | Preparation disposition |
| --- | --- |
| UPDATED | Bounded correction to original RO docs plus new [dated Owner RO-D04/D06 ratification](owner-ratification-ro-d04-d06-2026-11-13.md); independent review 1 preserved unchanged. No production/tests/ACTIVE edits |
| ALREADY_CONSISTENT | Published W1 at `40fb8a61b` and docs closeout at `3ce4c87d`; D12–D17 historical W1 decision scope, DP-A/B1 source acceptance, Active=NO_ACTIVE_HANDOFF |
| DEFERRED_OUT_OF_SCOPE | Current root/policy/roadmap/maintenance index navigation sync until accepted RO publication; original W1 handback/self-review and independent reviews, DPB2W1-L01 correction, unrelated LOW backlogs, Frontend/RAG docs |
| NOT_VERIFIED | Actual RO-A runtime behavior, Public POST revocation, future RO-B full DTO/name-projection safety, live Owner-data/public-facing deployment, IDE full diagnostics, next-slice readiness; RO-A independent re-review not yet performed on corrected documents |

Carry forward without resetting: W1 remediation **2/3 consumed / STOPPED**, takeover NOT_AUTHORIZED/NOT_USED, DPB2W1-L01 LOW OPEN BACKLOG. B1 remediation **3/3 consumed / STOPPED** and DPB1-L02 LOW BACKLOG; DP-A remediation **3/3 exhausted**, exceptional takeover 1/1 accepted and DPA-L01 LOW BACKLOG. **RO implementation counters: 0 consumed; takeover NOT_AUTHORIZED.** These are proposed separate RO counters, not retroactive to W1.

**STOP** for RO-A scope expansion into RO-B data changes, private disclosure through nested graph, NSFW bypass, unknown mutation admission, People direct route, schema/Flyway/global policy rewrite, loss of PRIVATE compatibility, unreviewed atomicity/snapshot requirement or source drift. Report exact block; do not consume an unauthorized remediation round.

**Next step:** Owner → Codex | `$codex-pre-handoff-review` **BF26-DP-RO-A ONLY** using [review 1](reviews/2026-11-13-bf26-dp-ro-independent-pre-handoff-review.md), this corrected dossier and the [2026-11-13 ratification](owner-ratification-ro-d04-d06-2026-11-13.md) | `FR-RO-PH-01` independent closure requested; `CHANGES_REQUESTED` until a new reviewer outcome, `ACTIVE=NO_ACTIVE_HANDOFF` | RO remediation 0 consumed, takeover NOT_AUTHORIZED. Do not create executable handoff until independently READY, Owner-published preparation and separate exact source/API authorization.

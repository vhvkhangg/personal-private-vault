# Independent Codex pre-handoff review — BF26-DP

## Focused correction after independent re-review 2 — two residuals only

- [Independent re-review 2](reviews/2026-11-11-bf26-dp-independent-pre-handoff-re-review-2.md) retained **PH-04 MEDIUM/OPEN** solely for (A) erroneously required PUBLIC login identifier and (B) five current notices still at the initial-review checkpoint. This authoring delta corrects A; the companion one-off **scoped application command** must actually correct B in the Owner's local worktree before the next independent review. A bundled script without executed five-file diffs is not proof of B closure.
- Verify the fixed PUBLIC login with exact `{profile:"PUBLIC",password}` **without `identifier`**, correct configured success, wrong/unconfigured generic 401, rejection of PRIVATE branch and extra identifier, while legacy PRIVATE `{identifier,password}` remains required. Confirm A03/A12/A21 and G-AUTH match and PIN verify incorrect/well-formed remains `200 verified:false`.
- Inspect the actual Git diff of five notices and BF26-DP README after applying the tool; all must reference [re-review 1](reviews/2026-11-11-bf26-dp-independent-pre-handoff-re-review-1.md) (PH-01–03 CLOSED at preparation) and [re-review 2](reviews/2026-11-11-bf26-dp-independent-pre-handoff-re-review-2.md) (PH-04 OPEN pending next focused review). Preserve old historical notices and all other tracked files. Re-running `$codex-pre-handoff-review` is only appropriate **after** that applied documentation delta exists.
- No new Owner D1–D6 vote, no source, tests, migration, handoff, publication, stage/commit/push or implementation remediation/takeover round. Only Codex can adjudicate PH-04 after inspecting the corrected worktree.

## Re-review after PH-04 bounded correction — no implementation authority

The [initial independent review](reviews/2026-11-11-bf26-dp-independent-pre-handoff-review.md) remains historical `CHANGES_REQUESTED`. The [subsequent independent re-review 1](reviews/2026-11-11-bf26-dp-independent-pre-handoff-re-review-1.md) independently **CLOSED PH-01–03 at preparation level** and retained **PH-04 MEDIUM OPEN**. Owner [D1–D6 ratification](owner-ratification-d1-d6-2026-11-11.md) remains binding; do not seek repeat approval.

This PH-04-only author correction adds the [DP-A exact matcher/compatibility and persisted session lifecycle tables](auth-compatibility-and-session-lifecycle-2026-11-11.md), aligns [Authentication design](authentication-session-and-migration.md), [A09–A15/A20/A21 acceptance](implementation-slices-and-acceptance.md) and [G-AUTH/G-SWITCH harness gates](harness-readiness.md), and corrects only current navigation status. A **separate independent Codex** must run `$codex-pre-handoff-review` to adjudicate PH-04. Check each Auth route's method/anonymous/PUBLIC/PRIVATE reachability, exact proof/body, existing vs new response and status; signed/JWT+DB session rolling expiry vs old TTL; consumed predecessor revoke no-op; PUBLIC reset epoch across cross-profile lineages; Google OFF/ON-unmapped; rotate/switch/revoke/reset races. Check no wildcard/auth bypass, no extra PIN/Frontend/RAG/Recycle Bin scope, unchanged PH-01–03 dispositions, no new source/migration/test or dependency. Only independent reviewer may declare readiness; otherwise CHANGES_REQUESTED.

**Next step:** separate Codex `$codex-pre-handoff-review` | PH-04 only, exact table and linked acceptance/harness/current navigation | no source/handoff, no publication before independent readiness | BF26-DP implementation/remediation/takeover counts unchanged.

## Historical initial invocation — already reviewed; D1–D6 now approved; re-review corrected package only

> Run **`$codex-pre-handoff-review`** in maintenance preparation mode for `BF26-DP` (Dual Public/Private Login Profiles). This is **preparation review only**, not implementation or executable-handoff creation.
>
> Verify actual current `main`, local `origin/main`, working tree/index and remotely published HEAD. The planning baseline was `9c7e123d01237e057675d348ddb93700a4850e51`; use the **actual** newer HEAD if published, and report drift rather than silently reusing it. Respect all pre-existing files/evidence; do not overwrite, clean, stash, reset or commit.
>
> Review every document under `docs/implementation/maintenance/2026-11-11-dual-login-profiles-preparation/` and the earlier BF26 deferred decision. Independently inspect the current Authentication, Vault, Fiction, Film, Media, Personal, Search, Portability, all 21 Spring module controllers, Postgres V1–V28 and Google Sign-In/Drive identities, token/refresh and approved frontend-BFF boundary. Reuse the BF26-S5 254-source-declaration CSV **with independent qualification**, not as a live route count.
>
> Evaluate Owner needs and alternatives `KEEP/REUSE/SIMPLIFY/REMOVE/DEFER`, proposed fixed profile/module matrix, exact username/password/session migration, server-side immediate Private downgrade (including signed-but-unexpired JWT), Public→Private fresh-password proof, Google linkage and legacy recovery compatibility, fail-closed unknown routes, known ID/joins/tag/search/metadata/export/streaming/derived sensitivity. Preserve singleton ownership and test-only default-OFF flags; do not create two `app_users` or general `visibility` column. Recommend the smallest secure design; identify any mismatch to existing frozen/accepted contracts and exactly which new ADR/DBML/API exceptions require Owner approval.
>
> Provide a **single consolidated Owner decision docket** for genuine unresolved choices (especially whole-module fixed Private list, browser/Google handling, session scope/credential compatibility, PIN retirement, migration). Do not silently promote proposed classifications to Owner-selected facts. If too broad, split into independently reviewable slices and exact permissible file/test boundaries. Assess whether existing JUnit/MockMvc/Testcontainers/PostgreSQL/Modulith/OpenAPI/bounded-command harness is sufficient; allow only justified scoped fixture changes. No frontend or RAG activation.
>
> Produce a dated independent review under this preparation folder's `reviews/`, evaluate all applicable `codex-review-coverage` dimensions, provide security negative-case test matrix and qualitative effort, and perform source/link/hash/Docs Sync Check. **Do not run code/runtime tests** unless a separate Owner authorization explicitly permits them; this is planning review.
>
> If material architectural/privacy decisions remain, return `CHANGES_REQUESTED` with traceable findings and one consolidated decision docket; route documentary corrections to ChatGPT. If the exact first slice is sufficiently approved, return `READY_FOR_HANDOFF` only for that bounded slice **after** identifying the required Owner frozen-boundary consent/publication. An independently ready docs-only preparation with no implementation authority may return `READY_FOR_OWNER_COMMIT`. No automatic worktree creation, handoff, Antigravity implementation, staging, Git commit/push, PR or deployment.
>
> Preserve H1-F `FRONTEND_PAUSED_BY_OWNER / CHANGES_REQUESTED`, FR-13 OPEN/UPSTREAM_DEPENDENCY_BLOCKED, paused RAG S19-2, indexes, existing counters and accepted BF26-S5 docs-only status. STOP after review and explicitly state the next action.

## Recommended review evidence inventory

- Authentication: `AppUser.java`, `SessionService.java`, `JwtTokenService.java`, `RefreshToken.java`, `SecurityConfiguration.java`, `PrivatePinService.java`, `GoogleSignInStore.java`, `GoogleSignInController.java`, current auth tests.
- PostgreSQL: V1 `app_users` singleton check, `refresh_tokens` lineage; V28 Google Owner identity checks; Portability security exclusion table list and schema validation.
- Module boundaries: `docs/architecture/module-dependency-matrix.md`, `docs/architecture/api-architecture.md`, applicable `AGENTS.md` and ADRs; plus 21 module endpoints and conditional route profiles.
- Privacy: Fiction/Film `isNsfw`, direct GET/UPDATE/Nested endpoints, Media byte streams, Personal, Vault IDs/tags/trash, Global Search ranking/pagination, export/backup, Drive slots, Feed/Inbox/Radar, Knowledge, RAG constraints.
- Tests/harness: `.agents/skills/{authentication-security,database-migration-review,backend-testing,rest-api-http-contracts,architecture-change,bounded-command-execution,codex-review-coverage,documentation-writing}`, `backend/src/test`, `scripts/run-bounded-command.py`, API/OpenAPI test manifest.

## Deliverable & ownership boundary

**ChatGPT**: preparation documentation and bounded correction, **Codex**: independent pre-handoff review and later executable handoff after approved/published preparation, **Antigravity**: implementation/testing/self-review only under active handoff, **separate Codex**: final independent implementation review, **Owner**: stage/commit/push and all new authorization. Review findings do not count against implementation remediation/takeover rounds. Previously accepted module/phase records remain historical; any actual frozen contract relaxation requires new approval and proof.

**Next step:** Owner → separate Codex `$codex-pre-handoff-review` for BF26-DP | scope-only review, no backend source implementation | review outcome and consolidated decisions before publication/handoff | STOP.

# DP-B1 — Bounded independent Codex preparation review instructions

**Owner has ratified D7–D11 Option A** in the [dated decision](owner-ratification-d7-d11-2026-11-12.md). The independent `$codex-pre-handoff-review` is now the next preparation gate. This is not a request to implement, modify source or create an executable handoff.

## Suggested review request

> Run `$codex-pre-handoff-review` independently for **BF26-DP-B1** on actual GitHub/local `main` and this B1 preparation folder. Use the Owner's dated [D7–D11 Option A ratification](owner-ratification-d7-d11-2026-11-12.md), D1–D6 binding ratification, DP-A published closeout/review, and frozen module instructions. Review exact two owner-safe Fiction/Film GETs, six Reference GETs, profile-specific DTO/key allowlists, consistent non-NSFW + active Vault eligibility, default-deny for every nested/credit/taxonomy/Search/Vault path, 401/403/404/error/cache and OpenAPI compatibility, transaction/ownership constraints, concurrency and finite test coverage. Verify no source, DDL, tests, flags, Frontend or Active handoff changed in preparation. Issue concrete stable findings and review evidence. Return `READY_FOR_HANDOFF` only if all B1 design/Owner/frozen-preparation blockers are resolved; otherwise `CHANGES_REQUESTED` with bounded corrections. Stop after review; do not stage/commit/push or create a handoff.

## Focused independent checklist

1. Verify current `main` SHA and distinguish DP-A source publication from later B1 preparation. Evaluate source drift before trusting this HEAD's static blob ledger.
2. Check controlling Owner D1–D6 and [dated D7–D11 Option A ratification](owner-ratification-d7-d11-2026-11-12.md); ensure the proposal docket and README reflect it rather than treating the five choices as still undecided.
3. Confirm all existing Fiction, Film, Film Credit, Fiction Genre, Film Genre and Reference GET handlers are listed; reconcile exact method-matchers with Spring runtime registration/HEAD/OPTIONS/ASYNC/ERROR semantics without assuming a source route count is a runtime count.
4. Compare every PublicFictionResponse/PublicFilmResponse field against current complete DTOs, People/Media/URL/free-text/genre/search-derived data and future-field behavior. No accidental full view exposure.
5. Confirm Vault active/trash/type and feature `isNsfw` can be read in one consistent logical snapshot **using legal module interfaces**; identify the minimal exact technical mechanism. If this cannot be proved without unapproved source/module exception, report it as a blocker rather than inventing a cross-owner SQL join.
6. Verify 404 hidden/absent parity, unchanged Private full DTO and domain semantics, no caches/headers leaking Private data, and authenticated-only Reference catalogs.
7. Validate security precedence: anonymous=401, allowed Public read=200 or safe 404, valid Public unselected=403; no handler side effects or enumeration through direct Film Credit/links/classifications.
8. Review the B1-01–24 finite acceptance matrix and existing JUnit/MockMvc/Testcontainers/Clock/Modulith/OpenAPI harness; require forced race and real signed persisted-token negatives, not test names alone.
9. Check ADR/API/frozen ownership: no V30 or schema delta by assumption, no generic auth engine, no Vault reverse dependency, no external provider/Frontend/RAG/B2 grants.
10. Check link validity, package scope, file/diff preservation, immutable history, `ACTIVE=NO_ACTIVE_HANDOFF`, and truthful test status. Label preparation PASS as **design only**.

## Status and next actions

- After Owner ratification, before independent review: `OWNER_RATIFIED_D7–D11_OPTION_A / INDEPENDENT_PRE_HANDOFF_REVIEW_PENDING / NO_ACTIVE_HANDOFF / NOT_AUTHORIZED_FOR_IMPLEMENTATION`.
- On independent `CHANGES_REQUESTED`: Owner/ChatGPT correct only specific B1 preparation findings; do **not** consume implementation/remediation counters.
- On independent `READY_FOR_HANDOFF`: Owner publication of accepted preparation, separate exact source/frozen authorization, then Codex `$codex-create-handoff` if explicitly authorized. No auto-start B1/B2 work.

**Not a test run:** Actual HTTP/PostgreSQL/OpenAPI evidence is a later authorized implementation obligation, and must never be inferred from documentation review.

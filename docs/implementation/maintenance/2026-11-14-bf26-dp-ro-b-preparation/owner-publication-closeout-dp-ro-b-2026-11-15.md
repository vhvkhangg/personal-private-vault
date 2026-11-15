# BF26-DP-RO-B — Owner publication closeout (2026-11-15)

- **Delivery:** BF26-DP-RO-B — selected authenticated PUBLIC Fiction/Film full-detail GETs, exact 19-property response with scalar nullable linked name; PRIVATE remains exact 18-property response.
- **Verified GitHub `main` publication:** [`0f3365de888ab03b0d1de6b32e56a3bb7ad96d5a`](https://github.com/vhvkhangg/personal-private-vault/commit/0f3365de888ab03b0d1de6b32e56a3bb7ad96d5a), `feat(backend): expose full public fiction and film details`, **2026-11-15 02:56:35 UTC**.
- **Published preparation parent:** [`78c9bd7d629b0e676d986f3b9a77190a968114e2`](https://github.com/vhvkhangg/personal-private-vault/commit/78c9bd7d629b0e676d986f3b9a77190a968114e2), `docs(backend): prepare public full-detail reads`.
- **Owner authority:** exact **13 production / 5 test** source/API exception, approved 2026-11-14 and recorded in [executable handoff](executable-handoff-dp-ro-b-2026-11-14.md), sections 1 and 3; [RO-D04/D06 ratification](../2026-11-13-bf26-dp-public-read-only-preparation/owner-ratification-ro-d04-d06-2026-11-13.md).
- **Independent acceptance:** [final re-review 2](reviews/2026-11-15-bf26-dp-ro-b-independent-final-re-review-2.md), `READY_FOR_OWNER_COMMIT / INDEPENDENTLY_ACCEPTED`, **FR-ROB-01–05 CLOSED**; no blocking CRITICAL/HIGH/MEDIUM or mandatory unverified RO-B acceptance.
- **Post-publication disposition:** **`INDEPENDENTLY_ACCEPTED / OWNER_PUBLISHED / CLOSED` for RO-B only**. Publication is verified by the source commit above. This closeout/navigation is a **later, separate docs-only deliverable**, not part of source commit `0f3365de888a`.

## Accepted HTTP/security contract

| Request / boundary | Accepted outcome |
| --- | --- |
| Authenticated PUBLIC `GET /api/v1/fictions/{id}` | Eligible non-NSFW work has **exactly 19 `ApiResponse.data` properties**: the original 18 `FictionResponse` values plus present nullable scalar `authorName`. Author is the referenced eligible Person or CreatorGroup only. |
| Authenticated PUBLIC `GET /api/v1/films/{id}` | Eligible non-NSFW work has **exactly 19 properties**: the original 18 `FilmResponse` values plus present nullable scalar `directorName`. Missing optional director => explicit JSON `null`. |
| PRIVATE Fiction/Film GET, POST, PUT | **Exactly original 18-property PRIVATE shapes**, without added name; original values, validation, headers and mutation semantics preserved. |
| Work eligibility | Non-NSFW plus correct-type, matching-ID, active/non-trashed Vault identity; hidden/missing/trashed/wrong-type work => indistinguishable safe **404/no-store**, no linked-name fetch. |
| Linked Person/CreatorGroup | The selected Person must have its own matching active correct-type `PERSON` Vault identity; CreatorGroup has **no Vault lifecycle**. Missing/ineligible/blank source name => required-present name field `null`, no direct People/Group HTTP exposure. |
| Genuine DB/lookup/transaction failure | Canonical safe **500**, `data=null`, `Cache-Control: no-store`, no partially successful response or exception detail leakage. |
| Consistent read | Feature-owned `REQUIRES_NEW / REPEATABLE_READ` snapshot materializes work, classification sets, eligibility and qualified name before return; later pure immutable in-memory mapper/JSON serialization is allowed. |
| Other PUBLIC business surface | Exactly **eight GET method/path pairs**: Fiction/Film detail and six Reference country/language/currency list/detail endpoints. Auth/profile exceptions retain existing separate policy. No other PUBLIC business GET/write granted. |
| PUBLIC Film POST / anonymous Film POST | RO-A revocation remains: authenticated PUBLIC **403 `ACCESS_DENIED`** before handler/no mutation; anonymous **401**. PRIVATE Film creation remains functional. |
| OpenAPI | Two selected PUBLIC detail response graphs reflect exact 19 keys with present nullable name; PRIVATE 18 schemas, `getFiction`/`getFilm` and PRIVATE-only `createFilm` remain valid. |

This is **not** a general PUBLIC read expansion. People, CreatorGroup, Vault, Personal, Finance, NSFW works, nested classification/credit/link endpoints, list/search and mutations retain their existing denial boundaries. No new route, schema/Flyway, dependency/module edge, session rule, frontend or RAG authorization is created.

## Published scope and source qualification

GitHub source commit `0f3365de888a` contains the exact authorized **18 Java paths** plus the accepted RO-B executable, implementation handbacks, self-reviews, ACTIVE checkpoint and independent review records. The source/test boundary is:

| Production (13) | Tests (5) |
| --- | --- |
| `fiction/fiction/FictionOperations.java` | `fiction/PublicFictionReadIntegrationTest.java` |
| `fiction/view/PublicFictionDetailView.java` **NEW** | `film/PublicFilmReadIntegrationTest.java` |
| `fiction/internal/application/FictionService.java` | `authentication/ProfileRouteAuthorizationIntegrationTest.java` |
| `fiction/internal/web/controller/FictionController.java` | `web/OpenApiRouteInventoryIntegrationTest.java` |
| `fiction/internal/web/mapper/FictionWebMapper.java` | `film/PublicFilmCreateIntegrationTest.java` |
| `fiction/internal/web/dto/PublicFictionDetailResponse.java` **NEW** | — |
| `film/film/FilmOperations.java` | — |
| `film/view/PublicFilmDetailView.java` **NEW** | — |
| `film/internal/application/FilmService.java` | — |
| `film/internal/web/controller/FilmController.java` | — |
| `film/internal/web/mapper/FilmWebMapper.java` | — |
| `film/internal/web/dto/PublicFilmDetailResponse.java` **NEW** | — |
| `OpenApiConfiguration.java` | — |

Production paths are relative to `backend/src/main/java/com/vhvkhangg/personalprivatevault/`; test paths to `backend/src/test/java/com/vhvkhangg/personalprivatevault/`. Review's source guard reconciled **18/18 hashes** and **1,911** checked source/resource/test/POM/watchdog inputs. No unrelated Java source, People/Vault production, repository, shared PRIVATE DTO, global advice, security matcher, package/module descriptor, schema or dependency change was part of the accepted bounded delivery.

## Independent evidence (preserve non-additive accounting)

- Reviewer final R2 **fresh 25/25 PASS**: `PublicFictionReadIntegrationTest` **12**, `PublicFilmReadIntegrationTest` **13**, 153.422 seconds, exit 0, zero failures/errors/skips, PostgreSQL Testcontainers and MockMvc.
- **114 distinct source-qualified passing cases across nine classes** = **25 R2 fresh + 89 applicable reused**. The earlier R1 26 fresh and initial review 119 are historical point-in-time evidence, **not added** to the current union.
- Six final producer gate counts: **FICTION 12**, **FILM 13**, **SECURITY 27**, **PRIVATE 23**, **OPENAPI 1**, **ARCH 38** = 114 distinct selected cases; source identity/XML/receipt reconciliation and complete capture/cleanup confirmed.
- Reviewer evidence directory: `logs/bf26-dp-ro-b/codex-rereview2-20261115-7e2b61/`, including `entry-audit.json`, `delta.json`, `fresh-detail.json`, `verification.json`, `idea-inspections.json`, XML and streams. Producer final receipts under `logs/bf26-dp-ro-b/remediation-round2/`. Historical failed attempts and R1/submitted evidence remain preserved.
- R2 new/changed physical snapshots, legal concurrent classification/work updates, linked-name qualification, fault/safe-error paths and post-service zero-extra-lookup witnesses were independently accepted. The optional LOW test-cleanup issue below remains documented.
- **NOT_VERIFIED:** full suite, full IDE-clean status, production deployment/live cutover, real Owner-vault data suitability and unrelated product readiness. No runtime tests are run for this docs-only closeout.

## Findings, backlogs, and frozen counters

| Identifier | Current disposition |
| --- | --- |
| `FR-ROB-01–05` | **CLOSED** by independent re-review 2. |
| `DPROB-L01` | **LOW / OPEN / BACKLOG** — two stale self-review ROB-20 method-name citations; canonical handback/review correctly map verified tests. Correct only in a separately authorized, bounded docs pass; preserve the dated self-review. |
| `DPROB-L02` | **LOW / OPEN / BACKLOG** — S6 test acquisition/qualification advice cleanup can be skipped if an earlier assertion throws; passing run follows normal cleanup. Future bounded test-hygiene correction, not an immediate extra remediation. |
| RO-B remediation/takeover | **2/3 consumed / STOPPED** after accepted round 2; takeover **NOT_AUTHORIZED / NOT_USED**. Third round not automatically authorized. |
| RO-A | `INDEPENDENTLY_ACCEPTED / OWNER_PUBLISHED / CLOSED`; remediation **1/3 / STOPPED**, takeover **NOT_AUTHORIZED**; `DPROA-L01 LOW / OPEN / BACKLOG` unchanged. |
| Historical B2-W1 / B1 / DP-A | All published/closed history remains; W1 remediation **2/3 / STOPPED**, B1 **3/3 / STOPPED**, DP-A **3/3 exhausted** and exceptional takeover **1/1 accepted**. `DPB2W1-L01`, `DPB1-L02`, `DPA-L01` remain LOW/OPEN/BACKLOG. |
| Other constraints | Frontend H1-F PAUSED/NOT_INDEPENDENTLY_ACCEPTED and FR-13 upstream-blocked; P17 FROZEN, P18 CLOSED, S19-2 SUSPENDED/NOT_ACCEPTED; indexing inactive, providers OFF/unset. |

## Docs Sync Check and next scope gate

- **UPDATED by this separate docs-only package:** this new actual-SHA closeout, top current-status/navigation notices in root `AGENTS.md` and `README.md`, Owner Delivery Policy, roadmap and maintenance README, plus the current `ACTIVE.md` header and RO-B preparation README current notice.
- **ALREADY_CONSISTENT:** GitHub source publication, exact Owner-approved 18-file map, independent final review, implementation receipts, RO-A source publication/closeout, D04/D06 product contract, eight GET permissions, PRIVATE preservation, counter/backlog history.
- **PRESERVED AS HISTORY:** original RO-B preparation, executable, R0/R1/R2 author handbacks/self-reviews, independent reviews, evidence and old current-looking notices. Current top-level headers supersede their dated pre-publication status; no historical finding or counter is silently rewritten.
- **DEFERRED_OUT_OF_SCOPE:** DPROB-L01/L02 and predecessor LOW backlog corrections; all unrelated harness/Frontend/RAG/phase/feature implementation. No automatic new slice selection or grant.
- **NOT_VERIFIED:** new runtime execution from docs-only edits, live production cutover, Owner data suitability and full-project IDE inspection.

`ACTIVE` becomes **`NO_ACTIVE_HANDOFF`** after verified RO-B Owner source publication. No executable or implementation remains active. This is not source authorization for any next feature; subsequent work requires separately chosen scope, preparation review and Owner approval. Owner alone verifies, commits and pushes this docs-only navigation closeout.

**Next step:** Owner | apply and review the docs-only package, commit/push with `docs(backend): close BF26-DP-RO-B publication and sync navigation` | BF26-DP-RO-B actual source SHA `0f3365de888a` and independent final re-review 2 | RO-B `OWNER_PUBLISHED / CLOSED`, docs closeout pending separate publication | remediation **2/3 consumed / STOPPED**, takeover **NOT_AUTHORIZED / NOT_USED**.

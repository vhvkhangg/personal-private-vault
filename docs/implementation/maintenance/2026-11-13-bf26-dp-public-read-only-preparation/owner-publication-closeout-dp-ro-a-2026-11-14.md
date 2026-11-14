# BF26-DP-RO-A — Owner publication closeout (2026-11-14)

- **Slice:** `BF26-DP-RO-A` — revoke authenticated PUBLIC Film creation, preserve current PRIVATE and B1 reads.
- **Verified GitHub `main` source publication:** [`0956b4962f529050dddf88dc7f2ec19ac571e0d0`](https://github.com/vhvkhangg/personal-private-vault/commit/0956b4962f529050dddf88dc7f2ec19ac571e0d0), `fix(backend): revoke public film creation`, 2026-11-14 13:20:27 UTC.
- **Source parent / published preparation:** [`7700222673712abc59eb6f1de621abc86c7172cc`](https://github.com/vhvkhangg/personal-private-vault/commit/7700222673712abc59eb6f1de621abc86c7172cc), `docs(backend): prepare standalone public write revocation`.
- **Owner authorization:** the exact RO-A frozen source/API/test approval plus the bounded **6→8 test-file amendment**, preserved in [executable handoff](executable-handoff-dp-ro-a-2026-11-13.md), sections 1 and 3. The [RO-D04/D06 ratification](owner-ratification-ro-d04-d06-2026-11-13.md) selects RO-A before RO-B.
- **Controlling acceptance:** [independent final re-review 1](reviews/2026-11-14-bf26-dp-ro-a-independent-final-re-review-1.md), `READY_FOR_OWNER_COMMIT / INDEPENDENTLY_ACCEPTED`; **FR-ROA-01–03 CLOSED** and no open blocking RO-A finding or mandatory acceptance `NOT_VERIFIED`.
- **Post-publication disposition:** `INDEPENDENTLY_ACCEPTED / OWNER_PUBLISHED / CLOSED` **for RO-A only**. Source publication has happened; this closeout/navigation package is a **subsequent docs-only deliverable**, not content included in the source commit `0956b4962f`.

## Exact accepted behavior — PUBLIC_READ_ONLY now enforced for selected business access

| Profile / request | Accepted behavior |
| --- | --- |
| Valid persisted PROFILE_PUBLIC — `POST /api/v1/films` | **403 / `ACCESS_DENIED`** before qualified Film MVC handler execution, no Film/Vault/domain mutation. Direct PUBLIC login and PRIVATE→PUBLIC downgrade origins covered. |
| Unauthenticated — `POST /api/v1/films` | Canonical **401 / `AUTHENTICATION_REQUIRED`**, no Film handler and no business mutation. |
| Valid PROFILE_PRIVATE — `POST /api/v1/films` | Existing full PRIVATE create remains: broad request/conversion (including vendor JSON and Latin-1), validation, Film/Vault transaction, **18-key** successful response and unchanged headers (no Location). |
| Authenticated PUBLIC business reads | Exactly **eight B1 GET pairs**: Fiction/Film direct detail (eligible non-NSFW, active correct-type Vault identity; existing **six/seven-key** Public DTOs), plus Country/Language/Currency Reference list/detail. |
| Other PUBLIC business/data requests | **Default DENY**, including Fiction/Film writes, People/Personal/Finance, generic Vault, Search, nested credits/genre/link methods, and all unselected routes. Authentication/profile-transition endpoints retain their separately accepted rules. |

**Important product-policy boundary:** Owner-approved future **RO-B** would expose 19-property PUBLIC Fiction/Film direct-detail responses (18 shared existing properties + nullable scalar `authorName` or `directorName`), keeping PRIVATE exact 18-property response contracts. **That behavior is NOT implemented or accepted in RO-A**; no Public access to People or additional modules follows from the ratification. Historical W1 had permitted PUBLIC Film creation; that historical acceptance remains truthful but its POST grant is prospectively revoked by RO-A.

## Code and independent evidence

Published source commit changed **exactly eight authorized Java files**, plus its independently accepted handoff/review/handback/self-review documentation:

| Group | Authorized files / change |
| --- | --- |
| Production (3) | `authentication/internal/infrastructure/security/SecurityConfiguration.java` — remove exact Public POST matcher; `film/internal/web/controller/FilmController.java` — Film POST OpenAPI annotations only; root `OpenApiConfiguration.java` — PRIVATE-only POST schema/security descriptions. |
| Tests (5) | `authentication/ProfileRouteAuthorizationIntegrationTest.java`; `authentication/SecurityFilterChainIntegrationTest.java`; `film/PublicFilmCreateIntegrationTest.java`; `film/PublicFilmReadIntegrationTest.java`; `web/OpenApiRouteInventoryIntegrationTest.java` — current deny assertions, retained PRIVATE/B1 and valid generated OpenAPI. |

`Production` paths are relative to `backend/src/main/java/com/vhvkhangg/personalprivatevault/`; `Tests` to `backend/src/test/java/com/vhvkhangg/personalprivatevault/`. No database migration, global parser, Film domain/DTO/repository, People/Vault/Fiction production or dependency change was authorized or published.

**Producer accepted evidence:** **117 distinct cases across nine classes**, selected round-1 receipts, all PASS/exit 0, zero failures/errors/skips, normal 300-second watchdog, complete capture/cleanup.

| Gate | Cases | Receipt wall time |
| --- | ---: | ---: |
| `G-ROA-SECURITY` | 27 | 197.453 s |
| `G-ROA-PRIVATE` | 22 | 92.703 s |
| `G-ROA-API` | 1 | 66.187 s |
| `G-ROA-READ` | 29 | 108.266 s |
| `G-ROA-ARCH` | 38 | 56.718 s |
| **Distinct total** | **117** | **Five qualified passing gate receipts** |

**Independent reviewer fresh re-review 1:** `ProfileRouteAuthorizationIntegrationTest` **15** and `PublicFilmCreateIntegrationTest` **21**, total **36/36 PASS**, zero failures/errors/skips, 147.375 s. These **36 corroborate existing cases** within the 117-case union; do **not** count 153 unique tests. Remaining **81 cases** are qualified reused producer evidence, not independently rerun in the final re-review. The independent reviewer reconciled **8/8** final Java SHA-256 fingerprints and **1,907** inspected input records, along with case/XML identity and unchanged inputs. Original failures, initial review and source-specific receipts remain historical, not overwritten as success.

- Reviewer evidence: `logs/bf26-dp-ro-a/codex-rereview1-20261114-b7d2e4/`.
- Previous producer attempts and round-1 receipts/XML retained within `logs/bf26-dp-ro-a/`.
- Successful bounded Testcontainers/PostgreSQL/MockMvc groups and applicable architecture tests establish RO-A acceptance; this **docs-only closeout runs no new Java/runtime tests**.
- Full IDE diagnostics, production deployment/cutover and real Owner-data suitability **NOT_VERIFIED**. Existing Maven metadata-access warnings do not imply test failure or permission to change dependencies.

## Findings, counters and preservation

| Item | Current disposition |
| --- | --- |
| `FR-ROA-01`–`03` | **CLOSED** by independent final re-review 1. |
| `DPROA-L01` | **LOW / OPEN / BACKLOG**, nonblocking. Residual prose in the executable section 7/8 still refers to six files despite the explicit eight-file Owner amendment and section 1/3 current map; self-review has small historical acceptance/counter wording errors. The controlling ACTIVE/re-review/this closeout supersede those old summaries. Preserve original submissions pending a separately authorized documentation correction; no extra remediation for LOW alone. |
| RO-A remediation / takeover | **1/3 consumed / STOPPED** after independent acceptance; **NOT_AUTHORIZED / NOT_USED** takeover. Unused rounds do not transfer or authorize further work. |
| W1 | `INDEPENDENTLY_ACCEPTED / OWNER_PUBLISHED / CLOSED` historical source at [`40fb8a61b6529585927e0a7dd524e9a363c69b6e`](https://github.com/vhvkhangg/personal-private-vault/commit/40fb8a61b6529585927e0a7dd524e9a363c69b6e); W1 POST grant superseded, accepted evidence preserved; remediation **2/3 / STOPPED**, takeover NOT_AUTHORIZED. `DPB2W1-L01 LOW/OPEN/BACKLOG`. |
| B1 | `OWNER_PUBLISHED / CLOSED`, remediation **3/3 / STOPPED**, takeover not authorized; `DPB1-L02 LOW/OPEN/BACKLOG`. |
| DP-A | `OWNER_PUBLISHED / CLOSED`, remediation **3/3 exhausted**, exceptional takeover **1/1 accepted**; `DPA-L01 LOW/OPEN/BACKLOG`. |
| RO-B | `DEFERRED / NOT_INDEPENDENTLY_READY / NOT_AUTHORIZED_FOR_IMPLEMENTATION`. Owner-ratified D04 full-response policy is not an executable or source grant. |
| Other | Frontend H1-F **PAUSED / NOT_INDEPENDENTLY_ACCEPTED**, FR-13 upstream blocked; Phase 17 FROZEN, Phase 18 CLOSED, S19-2 SUSPENDED, indexing inactive, provider flags OFF/unset. |

## Docs Sync Check and next boundary

- **UPDATED in this subsequent docs-only package:** this exact-SHA closeout, current headers of root `AGENTS.md` and `README.md`, Owner policy, roadmap and maintenance index, plus current entry of `ACTIVE.md` and RO preparation README.
- **ALREADY_CONSISTENT:** independently accepted eight-file implementation, actual git source commit, readiness and final re-review, Owner ratifications, existing B1 eligible read payloads, RO-B deferral, predecessor historical acceptance and separate counters.
- **PRESERVED / DEFERRED_OUT_OF_SCOPE:** original W1 and RO-A reviews/handbacks/self-reviews/executable history, `DPROA-L01`, other LOW backlogs and unrelated older notices/harness/Frontend/RAG docs. The new top-level notices explicitly supersede prior dated current-looking checkpoints; they are not silently rewritten.
- **NOT_VERIFIED:** new runtime execution for these docs-only edits, live deployment/cutover, broader Public read expansion, full IDE inspection, real Owner-data suitability and any future RO-B evidence.

`ACTIVE` is reset to **`NO_ACTIVE_HANDOFF`** only after RO-A Owner source publication; this is not an RO-B handoff and does not authorize code changes. No agent stages, commits or pushes these documents; Owner verifies and publishes the docs-only package separately.

**Next step:** Owner | apply package, review diff, then commit/push docs-only closeout with `docs(backend): close BF26-DP-RO-A publication and sync navigation` | BF26-DP-RO-A source commit `0956b4962f529050dddf88dc7f2ec19ac571e0d0` and independent final re-review 1 | source accepted and Owner-published, docs closeout pending separate publication | RO-A remediation **1/3 / STOPPED**, takeover NOT_AUTHORIZED; RO-B deferred until separate preparation/review/Owner authorization.

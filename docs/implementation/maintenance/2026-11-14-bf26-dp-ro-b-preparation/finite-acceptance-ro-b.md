# BF26-DP-RO-B — finite acceptance matrix for later implementation (NOT_RUN)

**Current state:** candidate proof obligations for future owner-authorized RO-B, grounded in the already-ratified [RO-D04](binding-contract-ro-b.md); **no runtime tests have run** for RO-B. Tests must qualify actual registered handlers, concrete SQL snapshots, exact wire values and source fingerprints, not assume completion from previous RO-A/B1 results. `ROB-*` IDs do not rename or re-adjudicate historical acceptance.

| ID | Mandatory observable acceptance after authorized implementation |
| --- | --- |
| **ROB-01** | Only the existing eight PUBLIC business method/path GET pairs remain allowed; the two Fiction/Film detail routes are the **only** changed PUBLIC payloads. |
| **ROB-02** | Both persisted PUBLIC origins (direct login and PRIVATE-to-PUBLIC downgrade) get eligible 200; anonymous protected GET returns canonical 401; malformed/stale/revoked authority fails closed with no data. |
| **ROB-03** | Eligible Fiction PUBLIC `data` has **exactly 19** keys, the approved full 18 with unaltered values + `authorName`, no additional props, and full `review`, progress/status and classification ID sets. |
| **ROB-04** | Eligible Film PUBLIC `data` has **exactly 19** keys, the approved full 18 with unaltered values + `directorName`, correct genre/classification IDs, no credit/People/metadata extras. |
| **ROB-05** | PRIVATE Fiction/Film GET/POST/PUT return unchanged exact **18-key** DTOs, statuses, headers, validation and error envelopes; explicit profile-switch requests do not mix schemas or cache private values. |
| **ROB-06** | Fiction author Person path emits only linked active correct-type Person `.name`; Person Vault present/identity/type/active check precedes name projection; nonname People data and roles never leak. |
| **ROB-07** | Fiction CreatorGroup author path emits only linked CreatorGroup `.name`; missing group → `authorName:null`, without inventing group Vault/soft-delete semantics or exposing description/members. |
| **ROB-08** | Film linked director Person path emits eligible name; missing director ID → explicit JSON `directorName:null`, no missing property. |
| **ROB-09** | Missing/wrong-type/trashed/identity-mismatched Person Vault, missing Person row or blank/null name produce a present nullable name key with `null`, not a 404 of eligible work or oracle. |
| **ROB-10** | All missing/NSFW/trashed/wrong-type/identity-mismatched Fiction/Film works produce indistinguishable canonical **404/no-store**, no private fields and **zero** People/Group-name reads. |
| **ROB-11** | Genuine Person/Group/Vault/work query or transaction failure produces safe **500/data:null/no-store**, not fake null success; no partial 200, exception text, IDs, SQL, tokens or private fields. |
| **ROB-12** | Work eligibility, all 18 approved fields, every classification ID set, Person Vault state and related name are materialized inside one owner-qualified **physical PostgreSQL REPEATABLE READ** snapshot or source-proven equivalent. |
| **ROB-13** | Force concurrent committed NSFW/trash/type changes and classification/review/progress changes *between controlled query points*; no mixed-state full success; coherent earlier eligible snapshot may complete and new post-change request denies. |
| **ROB-14** | Force linked Person trash/type/name change and CreatorGroup name deletion/replacement while a GET is in progress; no cross-snapshot name or leaked unqualified relation; demonstrate same physical session/transaction as appropriate. |
| **ROB-15** | Reused named People/Group/Vault lookups join the owning transaction; hidden work does not enrich; no `findPublic → find` separate-transaction controller chain and no unqualified PRIVATE fallback read. |
| **ROB-16** | PUBLIC eligible **200** and work-hidden **404** and genuine **500** use `Cache-Control: no-store`; 304/ETag/conditional requests, Location and shared response caches cannot expose alternate profile or hidden data. |
| **ROB-17** | Actual generated OpenAPI describes all **19 PUBLIC** fields (name present/required and nullable), exact **18 PRIVATE** schema, canonical envelopes and truthful alternative types; no dangling refs or loss of operation IDs. |
| **ROB-18** | Both live HTTP JSON specimens reconcile with OpenAPI; blank/missing name remains present as JSON null even under configured Jackson inclusion settings; no recursive Person/Group DTO or extra key. |
| **ROB-19** | Public direct People, CreatorGroup, nested Fiction/Film classification/credit/link, Search, Vault, Personal, Finance and all unselected methods remain denied; real handler-spy/no-mutation controls preserved. |
| **ROB-20** | PRIVATE Film POST stays permitted, PUBLIC Film POST remains exact RO-A **403 before handler** (anonymous 401); no accidental reactivation through alternate methods, paths or ASYNC/ERROR dispatch. |
| **ROB-21** | Architecture/Modulith, named-interface ownership and frozen schema/index/module dependency checks pass; no global ObjectMapper/exception redesign, dependency/framework/migration or provider change. |
| **ROB-22** | Exact changed file/source hash and receipt/XMl identity manifest, bounded fresh-vs-reused test accounting, mandatory self-review, independent Codex final review, Docs Sync ledger, no agent stage/commit/push. |

## Finite evidence groups (suggested, not a runtime quota)

| Gate | Test candidates | Required kind of evidence |
| --- | --- | --- |
| PUBLIC_FICTION | `PublicFictionReadIntegrationTest` and focused positive/negative fixtures | Both author sources, 19-key specimens, 404/500, real SQL linked-source snapshot, hidden no-enrichment |
| PUBLIC_FILM | `PublicFilmReadIntegrationTest` | Director null/success, 19-key exact schema, no NSFW/full-field leaks, PostgreSQL race/failure |
| SECURITY | `ProfileRouteAuthorizationIntegrationTest` with unchanged authorization inputs | Eight-route sweep; two real origins, People and mutations remain denied; no unintended handler access |
| PRIVATE | `FictionWebIntegrationTest`, `FilmWebIntegrationTest` | 18-key GET/POST/PUT and unchanged media/validation/status/response compatibility |
| OPENAPI | `OpenApiRouteInventoryIntegrationTest` | Generated full 19/18 response graph, HTTP specimens and no dangling refs |
| ARCH | Existing `ApplicationArchitectureTests`/Modulith and frozen scope checks | No new module edge/packages/dependencies/migrations; bounded source map |

Use existing `scripts/run-bounded-command.py` with `--profile normal --timeout-seconds 300` per ordinary command and source-qualified Testcontainers PostgreSQL/MockMvc. Actual method selection, receipt paths, XML counts and repeated/reused evidence must be decided from **changed source**. Do **not** impose a guessed fixed total of cases, claim fresh RO-B test execution from old B1/RO-A receipts, erase historical failures or demand an unrelated full-suite run. Independent final acceptance is a later gate, not this preparation status.

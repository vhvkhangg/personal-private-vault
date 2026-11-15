# Executable slice handoff — BF26-DP-RO-B: PUBLIC full work detail

- **Handoff ID / author / date:** `BF26-DP-RO-B` / Codex / 2026-11-14.
- **Status: READY_FOR_OWNER_COMMIT / INDEPENDENTLY_ACCEPTED — independent re-review 2, 2026-11-15.** [Independent final re-review 2](reviews/2026-11-15-bf26-dp-ro-b-independent-final-re-review-2.md) CLOSED **FR-ROB-01–05**. **114 distinct source-qualified cases**, reviewer fresh **25/25 PASS** corroborating that union (25 fresh + 89 reused). DPROB-L01/L02 LOW/OPEN/BACKLOG are nonblocking; exact 18-path boundary preserved. Section 16 is current; older author/review checkpoints remain history. Owner publication pending, no production-cutover claim.
- **Owner decision:** **APPROVED — BF26-DP-RO-B ONLY**, 2026-11-14, exact **13-production / 5-test frozen-source/API exception** recorded in sections 1 and 3. Four production paths are NEW; the other nine production and all five test files already exist.
- **Published preparation / verified entry:** [`78c9bd7d629b0e676d986f3b9a77190a968114e2`](https://github.com/vhvkhangg/personal-private-vault/commit/78c9bd7d629b0e676d986f3b9a77190a968114e2).
- **Preparation gate:** [independent pre-handoff review](reviews/2026-11-14-bf26-dp-ro-b-independent-pre-handoff-review.md), **READY_FOR_HANDOFF — preparation only**, no blocking preparation finding. Both mandatory materialization and Film create-test refinements are binding here.
- **Execution register:** [ACTIVE](../../handoffs/ACTIVE.md). Antigravity uses the existing [backend-implementer role](../../../../.agents/agents/backend-implementer/agent.md), `/antigravity-implement-handoff`, then mandatory `/antigravity-self-review`. A separate independent Codex uses `$codex-final-review`; Owner alone stages/commits/pushes after acceptance.
- **Counters:** RO-B remediation **2/3 consumed / STOPPED**; takeover **NOT_AUTHORIZED / NOT_USED**. Round 2 independently accepted; no third round implied. Initial implementation and self-review consume no remediation round. RO-A remains **OWNER_PUBLISHED / CLOSED**, remediation **1/3 consumed / STOPPED**.

## 1. Owner authority, sources of truth and entry qualification

The Owner's current request explicitly approves BF26-DP-RO-B ONLY at preparation HEAD `78c9bd7d629b0e676d986f3b9a77190a968114e2`: create this executable handoff for the exact 18 paths below; Antigravity later implements the ready contract. This is the bounded exception to frozen **Phase 4 Fiction, Phase 5 Film and Phase 13 HTTP/API/test** baselines. Security/session permissions, People/Vault production, repositories, PRIVATE DTOs, schema and dependencies remain frozen.

The Owner expressly carries forward (1) immutable complete results materialized inside the owning transaction, with pure in-memory HTTP mapping afterward permitted, and (2) the additional existing `PublicFilmCreateIntegrationTest` follow-up GET correction. No previous `PROPOSED / OWNER_APPROVAL_REQUIRED` submission alone was authority; this current **APPROVED** decision resolves that gate. Approval authorizes handoff creation and its bounded future implementation workflow, not acceptance/publication.

Read these scoped sources, resolving dated preparation wording through the current Owner approval and independent review refinements:

- [Preparation README](README.md), [binding contract](binding-contract-ro-b.md), [finite acceptance](finite-acceptance-ro-b.md), [source inventory](source-baseline-and-ownership.md), [candidate boundary](candidate-change-boundary.md), [review workflow](review-workflow.md), and the independent review above.
- [RO-D04/D06 Owner ratification](../2026-11-13-bf26-dp-public-read-only-preparation/owner-ratification-ro-d04-d06-2026-11-13.md), [RO-A publication closeout](../2026-11-13-bf26-dp-public-read-only-preparation/owner-publication-closeout-dp-ro-a-2026-11-14.md) and [RO-A independent final re-review 1](../2026-11-13-bf26-dp-public-read-only-preparation/reviews/2026-11-14-bf26-dp-ro-a-independent-final-re-review-1.md).
- Root/scoped `AGENTS.md`, [Owner delivery policy](../../../workflow/current-owner-delivery-policy.md), [handoff workflow](../../handoffs/README.md), [handoff template](../../../templates/handoff.md), [Docs Sync contract](../../../workflow/documentation-synchronization.md), [ADR-0016](../../../adr/0016-root-http-contract-module-local-adapters.md), [module matrix](../../../architecture/module-dependency-matrix.md), [frozen DBML](../../../database/personal-private-vault-schema-v1-FROZEN-final.dbml) and [package tree](../../../repository/repository-package-tree.md).

**Observed entry facts:** local branch `main`; HEAD, `origin/main` and live remote `main` all equal the preparation SHA above; worktree/index clean before these two handoff documents. Compared with reviewed `83f87ca72a2571ce08e3ee99e78a147e556775ff`, publication changed only the seven RO-B preparation Markdown files. Both complete `backend` trees equal `773e45ca6e57ccafd01e5b3fa73b2adf40725c2b`. All **27** inventory blobs were requalified (25 source/test entries plus two feature descriptors); fourteen existing allowlisted-file SHA-256 values and four absent NEW paths are recorded in section 9. Graphify was navigation, verified against canonical source. No Java compilation, runtime test, generated API execution or PostgreSQL experiment ran during handoff creation.

Before implementation inspect ACTIVE, current HEAD/diff, section 9 and saved evidence. Expected new executable/ACTIVE documentation is not source drift. Investigate unapproved material source/authority drift without discarding work or resetting counters. Do not restart an interrupted implementation as a new slice.

## 2. Goal, simplest implementation and preserved permissions

Change only the PUBLIC representations of authenticated Fiction/Film direct-detail GETs from six/seven keys to the approved **19**. Preserve their eligibility gate, safe errors, snapshot consistency and no-store. PRIVATE Fiction/Film GET/POST/PUT remain **18** original fields; PUBLIC Film creation stays revoked.

| Decision | Selected minimal mechanism / reason |
| --- | --- |
| KEEP / REUSE full owner data | Existing feature repository `findById`, `toView`, classification loaders and immutable `FictionView` / `FilmView`; no repository query edit or new persistence abstraction |
| KEEP / REUSE reference reads | Existing `VaultEntryOperations.find`, `PersonOperations.find`, `CreatorGroupOperations.find`, through already-permitted named interfaces; only linked scalar name is disclosed |
| ADD bounded qualified owner capability | `findPublicDetail(Long)` on each existing operations interface/service, returning its NEW immutable detail view; preserve old minimal `findPublic` and PRIVATE `find` |
| ISOLATE PUBLIC wire | Two NEW feature-local 19-property responses and pure mapper methods; leave shared PRIVATE response classes/mappers unchanged |
| REUSE transport | Existing root ApiResponse/error family and OpenAPI customizer under ADR-0016; local PUBLIC GET failure handling, no global advice/serializer redesign |
| DEFER cleanup / extra capabilities | Retain old minimal types and blocked PUBLIC Film POST code; no new module, framework, global visibility policy, endpoint or enrichment service |

Relative effort is moderate, concentrated in exact wire/null/error and new physical snapshot evidence. This is a static estimate, not a runtime/test-count promise. No remaining material product choice requires another approval within this boundary.

Exactly these **eight PUBLIC business method/path pairs** remain; no matcher or permission edit is authorized:

| Method | Path |
| --- | --- |
| GET | `/api/v1/fictions/{id}` |
| GET | `/api/v1/films/{id}` |
| GET | `/api/v1/reference/countries` |
| GET | `/api/v1/reference/countries/{code}` |
| GET | `/api/v1/reference/languages` |
| GET | `/api/v1/reference/languages/{code}` |
| GET | `/api/v1/reference/currencies` |
| GET | `/api/v1/reference/currencies/{code}` |

Auth/profile and infrastructure exceptions keep their separate accepted rules. Every unselected PUBLIC business read/mutation, including direct People/CreatorGroup/Personal/Finance, stays denied. Showing approved linked IDs/names does not permit following them through another endpoint.

## 3. Exact code edit boundary — 13 production / 5 test files

`J/` = `backend/src/main/java/com/vhvkhangg/personalprivatevault/`; `T/` = `backend/src/test/java/com/vhvkhangg/personalprivatevault/`. These are the **entire** production/test allowlist. Existing package parents for all four NEW classes were verified; no new package/module/descriptor is needed.

| ID | Authorized exact path | Permitted change / preservation |
| --- | --- | --- |
| P01 | `J/fiction/fiction/FictionOperations.java` | Add qualified full-detail read signature returning the new view; retain all existing operations |
| P02 | `J/fiction/view/PublicFictionDetailView.java` **NEW** | Immutable fully materialized Fiction result plus nullable scalar authorName; no entity/lazy/People object |
| P03 | `J/fiction/internal/application/FictionService.java` | New owner-qualified full read, classifications and author resolution in one snapshot; create/update/domain invariants unchanged |
| P04 | `J/fiction/internal/web/controller/FictionController.java` | Selected PUBLIC GET branch, mapping and safe 404/500/no-store only; PRIVATE and mutation paths unchanged |
| P05 | `J/fiction/internal/web/mapper/FictionWebMapper.java` | Pure explicit 19-field mapping from captured result; PRIVATE/command/minimal mapping unchanged |
| P06 | `J/fiction/internal/web/dto/PublicFictionDetailResponse.java` **NEW** | Exact PUBLIC 19-field DTO, present nullable authorName; only local JSON/schema metadata as necessary |
| P07 | `J/film/film/FilmOperations.java` | Corresponding qualified full-detail read signature; existing capabilities unchanged |
| P08 | `J/film/view/PublicFilmDetailView.java` **NEW** | Immutable fully materialized Film result plus nullable scalar directorName |
| P09 | `J/film/internal/application/FilmService.java` | New qualified full read, classifications and director resolution in owner snapshot; mutations unchanged |
| P10 | `J/film/internal/web/controller/FilmController.java` | Selected PUBLIC GET response/error branch only; POST runtime/annotations, PRIVATE and other handlers unchanged |
| P11 | `J/film/internal/web/mapper/FilmWebMapper.java` | Pure explicit 19-field mapping; PRIVATE/command/minimal/blocked-create mapping unchanged |
| P12 | `J/film/internal/web/dto/PublicFilmDetailResponse.java` **NEW** | Exact PUBLIC 19-field DTO, present nullable directorName; local metadata only |
| P13 | `J/OpenApiConfiguration.java` | Only two selected GET schema graphs and related response components; preserve PRIVATE schemas, POST/PUT/Reference/Auth/other operations and IDs |
| T01 | `T/fiction/PublicFictionReadIntegrationTest.java` | 19/18 HTTP, Person/Group names, null/404/500, qualification, immutable materialization, physical full-path snapshot/fault/concurrency proofs |
| T02 | `T/film/PublicFilmReadIntegrationTest.java` | Corresponding director/absent-director and 19/18/404/500/snapshot proofs; retain RO-A denied POST and privacy negatives |
| T03 | `T/authentication/ProfileRouteAuthorizationIntegrationTest.java` | Preserve exact eight-pair registered-handler sweep and mutation/sensitive/session/dispatcher denials; only necessary representation assertions change |
| T04 | `T/web/OpenApiRouteInventoryIntegrationTest.java` | Actual generated 19/18 graph/specimens, required nullable name, refs and unchanged operation IDs/POST |
| T05 | `T/film/PublicFilmCreateIntegrationTest.java` | **Follow-up PUBLIC GET only:** update `followUpB1GetOnCreatedFilmSucceedsWithIdenticalSevenKeys` name/specimen/assertions to 19 and directorName presence/null/full values. Preserve all PRIVATE creation/media/validation/commit/observer/rollback/race and RO-A denial tests |

T05 is the review-added existing file, not permission to refactor the class or weaken its other assertions. Other tests may execute as read-only regressions. No new test source file or shared fixture edit is authorized; needed test-local helpers/advice/fault fixtures stay inside T01–T04 and their approved purposes.

Relevant existing engineering guidance: `fiction-domain-modeling`, `film-domain-modeling`, `java-spring-coding-standards`, `reuse-and-consistency`, `pragmatic-solid-design`, `modular-monolith-architecture`, `jpa-postgresql-persistence`, `backend-testing`, `rest-api-http-contracts`, `authentication-security` for preserved auth controls and `bounded-command-execution`. Existing backend-implementer role suffices; no agent, skill, hook or dependency changes. Root HTTP remains at the application root under ADR-0016; views stay in feature named `view` packages and DTO/mappers/controllers remain module-local.

## 4. Binding response, null and failure contract

PUBLIC Fiction `ApiResponse.data` contains **exactly these 19 properties**:

```text
id,title,originalTitle,nationalityCode,posterUrl,format,isNsfw,genreId,authorPersonId,authorGroupId,description,totalChapters,progressStatus,consumptionStatus,currentProgressText,review,storyArchetypeIds,worldSettingIds,authorName
```

PUBLIC Film `ApiResponse.data` contains **exactly these 19 properties**:

```text
id,title,originalTitle,nationalityCode,posterUrl,format,productionStyle,isNsfw,directorPersonId,description,totalEpisodes,progressStatus,consumptionStatus,currentProgressText,review,genreIds,storyArchetypeIds,worldSettingIds,directorName
```

PRIVATE each keeps precisely the corresponding first **18**, without the name property, on GET/POST/PUT. Compare all approved values using stable populated fixtures, including progress/review/IDs and complete classification sets. Null-valued work fields still count as properties. PUBLIC name is always present, string or explicit JSON null; use feature-local inclusion metadata if needed, never global Jackson changes. Return only scalar values/enums/ID sets, no nested work wrapper or recursive Person/Group/metadata object in HTTP data. PUBLIC successful `isNsfw` is always false.

**Order and names:** work exists, non-NSFW, same-ID active correct-type FICTION/FILM Vault identity **before** any linked People/Group name access. For a Person link, qualify the referenced Person's own same-ID active `PERSON` Vault identity before resolving its name. Existing Person.find does not itself perform that qualification. For CreatorGroup use only its existing named find; it is not Vault-backed and has no invented trash/type test. Fiction's author Person XOR Group domain rule remains unchanged; optional Film director may be absent.

Normal missing/ineligible Person/Vault, missing Group, absent optional director or null/blank linked name → name property present with **null**, successful eligible work still 200. Do not substitute unrelated names or child 404s. A returned reference must belong to the exact linked identity. Preserve the nonblank stored scalar; null policy is not authority to normalize unrelated data or change domain validation.

Hidden/missing/trashed/wrong-type/identity-mismatched work → existing indistinguishable safe **404**, data null, `FICTION_NOT_FOUND / Fiction not found` or `FILM_NOT_FOUND / Film not found`, no-store, zero linked-name access. Genuine work/Vault/People/Group/classification/transaction failure → safe **500**, data null, `INTERNAL_ERROR / An unexpected error occurred`, no-store. No exception-text classification, catch-and-return-null, unqualified PRIVATE retry, partial 200, child-status oracle, raw SQL/ID/token/payload leak or new raw exception logging.

Use the existing ApiResponse family: success has null error/meta; errors have null data/meta and canonical safe error/fieldErrors. PUBLIC **200/404/500** use `Cache-Control: no-store`; no ETag/304, Location or shared cache may bypass eligibility or mix profiles. PRIVATE transport/cache/headers/status/error semantics remain unchanged. Anonymous protected requests remain canonical 401; live PUBLIC Film POST remains exact 403 ACCESS_DENIED before handler, anonymous POST 401.

## 5. Implementation sequence and immutable transaction boundary

1. Inspect current source/test drift and entry fingerprints. Preserve security, PRIVATE DTOs, repository and other frozen inputs. Add P02/P08 and `findPublicDetail` signatures on P01/P07 without removing old minimal APIs. Existing immutable full views already defensively copy ID sets; wrapping a fully constructed FictionView/FilmView plus name is sufficient, with no entity/collection escape.
2. Implement P03/P09 full reads with feature-owned `@Transactional(readOnly=true, propagation=REQUIRES_NEW, isolation=REPEATABLE_READ)`. Reuse inherited owner repository `findById`; check work identity/non-NSFW and active correct-type Vault before classifications/name access. Reuse owner `toView`/classification loaders, existing named Vault/People/Group APIs and null rules; propagate genuine failures. Do not delegate to a separately completed minimal transaction followed by PRIVATE `find` or resolve data through a controller.
3. **Inside that one physical transaction**, completely capture eligibility, all 18 work values, complete copied classification sets, Person Vault qualification and scalar name. Build the immutable result before returning. Named readers must join this snapshot; no new transaction manager or cross-owner repository access. Read operations must not mutate People roles, work state or other business rows.
4. Add separate P06/P12 DTOs and P05/P11 pure mapping methods. After service return, pure in-memory DTO construction/JSON serialization of captured values **is permitted**. No additional repository/capability call, name/eligibility lookup, lazy traversal or other data acquisition after transaction completion. This is the mandatory review refinement, superseding the older draft's blanket wording about DTO assembly after commit.
5. Switch only the two PUBLIC GET branches to the new capability/map. Bound PUBLIC GET operational/transaction-completion failures at the owning HTTP branch using the existing safe root error envelope and no-store; keep Optional absence separate from failure. Catching a genuine failure must not let generic/domain advice turn it into child 404/400 or omit required cache handling. Do not add global advice or change PRIVATE error handling.
6. Update only the two GET OpenAPI graphs in P13 and their response components: PUBLIC exact 19, PRIVATE original 18. Preserve `getFiction`, `getFilm`, PRIVATE-only `createFilm`, POST/PUT requests/responses, Reference/Auth/security documentation and all other routes. Prefer truthful existing `anyOf`; do not claim exclusive `oneOf` where an unchanged PRIVATE schema also accepts the PUBLIC specimen. No global serializer/schema framework or PRIVATE-schema tightening to force exclusivity.
7. Implement T01–T04 evidence and T05's single follow-up correction. Replace obsolete eligible six/seven-key/forbidden-review assertions; retain hidden-path/no-name-access/privacy negatives and tests for old minimal APIs where still applicable. Execute finite gates on final applicable source, preserve evidence, self-review and hand back for independent Codex.

## 6. Acceptance matrix — ROB-01–22 (MUST, all runtime NOT_RUN)

| ID | Mandatory observable behavior and concrete proof | Gate / primary allowed test |
| --- | --- | --- |
| ROB-01 | Exact eight registered PUBLIC business GET pairs above; only the two work payloads change. Reconcile actual registered methods/paths with positive owning handlers; Reference payloads unchanged | SECURITY / T03, OPENAPI / T04 |
| ROB-02 | Both persisted PUBLIC login and real PRIVATE→PUBLIC downgrade origins get eligible 200; anonymous 401; malformed/stale/revoked authority fails closed without data. Use legitimate live-session controls, not mock roles | FICTION / T01, FILM / T02, SECURITY / T03 |
| ROB-03 | Fiction data exactly 19 keys, full approved 18 values plus authorName, including populated/null fields, review/progress/classification sets and both author sources | FICTION / T01 |
| ROB-04 | Film data exactly 19 keys, all 18 original values plus directorName, complete genre/classification sets, no credits/People/metadata extras | FILM / T02 |
| ROB-05 | Exact original PRIVATE 18 on Fiction/Film GET/POST/PUT; original values, status, validation/error/media/headers; same fixture/profile switches never mix 19/18. Assert exact keys in T01/T02; unchanged lifecycle tests alone are insufficient | FICTION/FILM / T01–02, PRIVATE / T05 plus read-only web tests |
| ROB-06 | Linked Fiction Person returns only its nonblank name after its active same-ID PERSON Vault check; private Person fields/roles do not appear | FICTION / T01 |
| ROB-07 | Linked CreatorGroup returns its scalar name; missing returns authorName:null, no group Vault/trash invention or description/member leakage | FICTION / T01 |
| ROB-08 | Eligible linked Film director returns name; absent director ID gives a present directorName:null property | FILM / T02 |
| ROB-09 | Missing/wrong-type/trashed/mismatched Person Vault, missing Person, null/blank name yield present null, not work 404 or reference existence oracle. Qualify legal DB versus injected impossible legacy cases | FICTION/FILM / T01–02 |
| ROB-10 | Missing/NSFW/trashed/wrong-type/mismatched work yields identical per-feature 404/no-store/data:null and zero People/Group name access, no full/private body | FICTION/FILM / T01–02 |
| ROB-11 | Genuine work/Vault/Person/Group/classification query or transaction-completion failures yield safe 500/data:null/no-store, no partial success, internal detail or fake null; clear fault and prove recovery | FICTION/FILM / T01–02 |
| ROB-12 | New full-detail capability and HTTP branch materialize all 18 values/classifications, Person qualification/name in one physical owner REPEATABLE READ snapshot. No post-return lookup/lazy traversal; immutable captured collections | FICTION/FILM / section 7 full-path witness |
| ROB-13 | Controlled competing committed work NSFW/trash/type and review/progress/classification changes between query points cannot produce mixed-state success; coherent earlier eligible snapshot may complete, fresh post-change request denies or sees the new coherent eligible values | FICTION/FILM / T01–02 real PostgreSQL barriers |
| ROB-14 | Controlled Person Vault trash/type/name and CreatorGroup name/relation replacement or absence cases do not mix snapshots or disclose unqualified names; distinguish legal committed races from injected missing-reference cases | FICTION/FILM / T01–02 |
| ROB-15 | Existing named Vault/People/Group reads join the owning snapshot, work eligibility precedes enrichment; no controller minimal→PRIVATE chain/fallback. Prove weaker outer transaction suspended, separate physical inner/persistence context and correct outer resumption | FICTION/FILM / T01–02 |
| ROB-16 | PUBLIC 200/404/500 no-store; conditional headers cannot return cached private/hidden 304/alternate data or Location/ETag; actual profile-switch HTTP specimens remain isolated | FICTION/FILM / T01–02 |
| ROB-17 | Actual generated OpenAPI has exact 19-property PUBLIC components, required/present nullable name and original 18-property PRIVATE components; valid envelopes/alternatives/refs and operation IDs | OPENAPI / T04 |
| ROB-18 | Actual HTTP/specimens reconcile with generated schemas for both profiles/features, string/null names; reject missing name, wrong type/extra/recursive data; configured JSON includes the null key. T05 follow-up is 19, PRIVATE creation unchanged | FICTION/FILM/OPENAPI/PRIVATE / T01–02/04–05 |
| ROB-19 | Direct People/CreatorGroup, nested work classification/credit/link, Search/Vault/Personal/Finance and all unselected methods remain denied; qualified actual MVC handler/no-mutation sweep retained | SECURITY / T03 |
| ROB-20 | PRIVATE Film creation committed/18 fields preserved; both PUBLIC origins' Film POST exact RO-A 403 before handler and anonymous 401; methods/subpaths/ASYNC/ERROR do not reactivate it | SECURITY / T03, FILM / T02, PRIVATE / T05 |
| ROB-21 | Existing architecture/Modulith/named-interface/ADR-0016 checks and exact source/frozen diff guards pass; no schema/index/module/dependency/provider/global-policy change | ARCH plus source guards |
| ROB-22 | Exact final file/input/receipt/XML/case manifest, truthful fresh/reused/failure accounting, mandatory self-review, separate independent review, Docs Sync/counters; Owner alone publishes | All gates, section 8 handback |

Review-coverage transfer: intent/wire/validation → ROB-01–11/17–18; critical privacy/transaction risks → ROB-06–16/19–20 with mandatory fresh new-path witnesses; module/database ownership → ROB-12/15/21; tooling/docs/hygiene → ROB-22. Runtime outcomes remain NOT_VERIFIED until evidence exists. Retrieval/OCR/model evaluation, new performance targets and dependency changes are N/A because none is in scope; no full-project audit, benchmark or zero-diagnostic claim follows.

## 7. Deterministic PostgreSQL, failure and HTTP proof plan

**Use current Testcontainers PostgreSQL/MockMvc and allowed test-local seams.** Reuse existing bounded latch/executor/TransactionTemplate and owner-repository/advice patterns; do not add production instrumentation or modify a frozen repository. Every barrier must identify the actual new full-detail method/query point, await a real committed writer, use finite waits and restore advice/mocks/executors in finally. Sleeping and hoping for a race, annotations alone or an unchanged minimal `findPublic` test are not full-detail snapshot proof.

| Witness | Required setup, intervention and result |
| --- | --- |
| S1 — physical new-path isolation | For **each feature**, invoke the real new capability through its proxy, with a READ COMMITTED outer transaction holding an actual managed work entity. At new owner read/classification, work Vault, referenced Person Vault and actual Person/Group read points, record active/read-only state, `SHOW transaction_isolation`, `pg_backend_pid()` and a suitable snapshot identity/SQL witness. Assert one REPEATABLE READ owner snapshot across real data reads, separate from the outer connection/context; outer isolation/connection/managed entity resume correctly. PID alone or a sampled capability entry outside its real query is insufficient. Reuse default REQUIRED reader source qualification, but produce physical evidence for the new path and both Fiction author kinds plus Film director. |
| S2 — work eligibility and full values | After a real initial owner read establishes its snapshot on a fully eligible fixture, pause before later Vault/classification/name reads. A different transaction/connection commits legal work NSFW, work Vault trash/type and populated review/progress/classification changes. Resume: assert the full 19-field earlier eligible state and earlier values/sets, never a blend or a status-only acceptable alternative. A fresh HTTP request after committed ineligibility is 404/no-store with zero linked-name reads. Include an eligibility-preserving values/set change so the fresh 200 shows the new full state; comparing only title/ID is inadequate. |
| S3 — linked-source snapshot | For Fiction Person, Fiction Group and Film director, pause between work read and reference reads; legally commit Person name/Vault state or Group name/relation changes. Earlier eligible snapshot may return its earlier qualified name; fresh eligible-work request sees new name or explicit null if reference becomes ineligible. Demonstrate reference/classification lookups share S1's physical snapshot. State exactly which stored relation/name is changed; do not invent Group trash. |
| S4 — ordinary absence and work-first negatives | Real legal absent/hidden/type/trash fixtures establish 404 and no name calls; eligible director-absent/Person-ineligible states establish 200/name:null. For missing mandatory FK targets, null names disallowed by schema, legacy identity mismatch or Group removal that cannot legally commit, use explicitly labelled named-capability Optional.empty/inconsistent-return fault fixtures in T01/T02. Pair them with real DB snapshot witnesses; do not pretend injected absence proves a committed broken FK. A legal reference repoint followed by old-row removal is an alternative only where existing constraints allow it. |
| S5 — operational and completion failures | Inject identifiable work query, work Vault, Person Vault, Person/Group and classification read failures at the selected test-local boundary; each genuine failure gives whole-response INTERNAL_ERROR/500/data:null/no-store, no sensitive text, not null-name 200. Include a service transaction-completion failure (e.g. test-local synchronization failure before successful completion) so controller handling covers exceptions from proxy return/commit, not just inside a method. Identify injected versus actual DB failure precisely; never disable constraints, stop unrelated services or claim simulated failure is a real outage. Remove fault and prove normal recovery. |
| S6 — complete immutable result | Capture the result after the real owner transaction ends, then map/serialize outside it. Confirm copied immutable sets retain captured values, no entity/lazy object escapes, and no additional scoped data/name/qualification queries occur during mapping/serialization. Guard the lookup boundaries or observe relevant SQL through existing test-local instrumentation; do not assert absence from a spy that was never positively qualified. |

These witnesses may share fixtures/cases; they are finite proof obligations, not six new frameworks or a fixed test count. Respect PostgreSQL FK/NOT-NULL/uniqueness/domain constraints. SQL is disposable **test** setup/intervention only, never an alternate cross-owner production read. Existing Person.find's bounded internal role read is not authority to disclose roles or demand a new People name API.

For wire proof, use both actual persisted PUBLIC origins and stable richly populated Person-author, Group-author, director and absent-name fixtures. On the same eligible work compare all 18 common values with PRIVATE; assert PRIVATE GET/POST/PUT key sets explicitly in T01/T02. Separate permitted work review/progress/IDs from forbidden People notes/birthday/roles, Group descriptions/members, recursive objects and hidden-work sentinels. Include PUBLIC↔PRIVATE request order and conditional-header 200/404/500 controls; no representation mixing.

Retain T03's actual registered-handler sweep, populated domain row-value snapshot and positively qualified MVC handler observation; a status-only or service/Vault-spy-only check is insufficient for RO-A before-handler denial. Create/configure tokens/positive controls before the denial snapshot; exclude legitimate fixture/Auth transitions from business no-mutation claims. Reuse unchanged session invalidation/admission limits by source applicability; do not promise retroactive cancellation of already-admitted coherent snapshots.

T04 must request actual `/v3/api-docs`, resolve the entire referenced graph and validate real 19/18 specimens (including null name) against selected schemas. Preserve unresolved-reference/wrong-type/extra-field negative validator controls, all operation IDs and RO-A PRIVATE-only POST advertisement. A permissible five-field body is also valid PRIVATE input and never implies PUBLIC POST permission. Retain old minimal Java DTOs/components where still referenced; no cleanup deletion is required.

## 8. Bounded verification, documentation and handback

All commands use existing `scripts/run-bounded-command.py`, **normal 300-second hard cap** including startup. No H2, Owner database, provider/native-browser/model execution, unapproved full suite or extended-time run. Maintenance B1/B2 runner/native hook supported-path limits remain; no hook bypass or unrelated startup/trust/tooling repair.

| Gate | Exact Maven `-Dtest` selector | Coverage |
| --- | --- | --- |
| G-ROB-FICTION | `PublicFictionReadIntegrationTest` | ROB-02/03/05–07/09–16/18, S1–S6, both author kinds |
| G-ROB-FILM | `PublicFilmReadIntegrationTest` | ROB-02/04/05/08–16/18/20, director/absent, S1–S6 |
| G-ROB-SECURITY | `ProfileRouteAuthorizationIntegrationTest,SecurityFilterChainIntegrationTest` | ROB-01/02/19/20, eight pairs/session/dispatcher/no-handler/no-mutation |
| G-ROB-PRIVATE | `PublicFilmCreateIntegrationTest,FictionWebIntegrationTest,FilmWebIntegrationTest` | ROB-05/18/20 and retained PRIVATE media/validation/commit/rollback; T05 follow-up correction |
| G-ROB-OPENAPI | `OpenApiRouteInventoryIntegrationTest` | ROB-01/05/17/18/20 actual graph, specimens/refs/operation IDs |
| G-ROB-ARCH | `ApplicationArchitectureTests` | ROB-21 plus separate frozen-file/source checks |

Implementation-only example from repository root, **NOT executed during handoff creation**:

```powershell
$robPython = (Get-Command python -ErrorAction Stop).Source
$robRunDir = Join-Path 'logs/bf26-dp-ro-b' ('implementation-' + [guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Path $robRunDir -ErrorAction Stop | Out-Null
& $robPython scripts/run-bounded-command.py --profile normal --timeout-seconds 300 --receipt (Join-Path $robRunDir 'G-ROB-FICTION.json') -- mvn.cmd -ntp -f backend/pom.xml '-DargLine=--enable-native-access=ALL-UNNAMED' '-Dtest=PublicFictionReadIntegrationTest' test
```

Run selectors **sequentially**, substitute gate/selector/unique receipt, and preserve each run's Surefire XML before another Maven run overwrites it. If a group times out, preserve that attempt and split into bounded disjoint actual class/method selections; reconcile complete coverage without increasing the cap. Keep fresh retry paths and prompt nonzero-exit diagnostics; terminate only owned process trees via the runner. Do not run concurrent Maven/shared mutable fixtures or blindly loop.

**Final verification contract:** fresh final applicable execution of changed T01–T05 is required (whole classes or a documented complete applicable selector set, including T05 affected follow-up and retained PRIVATE/denial controls). Fresh full-detail physical/HTTP/failure/OpenAPI proofs are mandatory. Run architecture on final inputs. Unchanged Reference/People/Vault/domain/Auth or PRIVATE regression evidence can be reused only with verified input hashes, exact cases/receipt/XML, changed-boundary impact and environment limits; no B1 minimal proof substitutes for S1–S6. The six finite groups are the final scoped gate, not a new full-application-suite mandate.

Handback records **ROB-01–22 → actual final test method/case → command/receipt/log/XML → final source/input fingerprints → outcome/limit**. Include runtime/tool versions and test fixture/config/POM inputs, physical query/barrier/commit observations, exact 19/18 specimens/schema references, and explicit fresh versus reused provenance. Deduplicate testcase class/name/parameter identities; no invented totals, assertion counts or reused XML relabelled fresh. Preserve failures/timeouts/skips/disabled/not-run cases and raw logs; mandatory unverified evidence blocks acceptance. RO-A's 117-case union and fresh 36-case corroboration remain historical predecessor facts, not RO-B execution/counts.

### Documentation impact / responsible editor / observable evidence

This invocation changes only **this new executable and ACTIVE**. Workflow artifacts below are documentation, not additions to the 18-file code allowlist.

| Artifact / owner | Exact permitted purpose and timing |
| --- | --- |
| This executable and [ACTIVE](../../handoffs/ACTIVE.md) / Codex now, Antigravity result fields later, independent Codex disposition later | Register current approved RO-B scope/readiness/counters; preserve the original contract and history. Implementation fills result links/status only; review records independent findings/disposition. |
| `implementation-handback-dp-ro-b-2026-11-14.md` / Antigravity, NEW here | Actual path/method diff, ROB traceability, physical/wire/error proof, source/case/receipt manifest, fresh/reused results, limitations and Docs Sync. |
| `self-review-dp-ro-b-2026-11-14.md` / Antigravity, NEW here | Mandatory `/antigravity-self-review`: exact scope/minimality, PRIVATE/security/ownership/snapshot/null/error preservation, meaningful results and residual issues. Not independent acceptance. |
| New `reviews/` final-review artifact / separate independent Codex | Independent source/evidence qualification, findings and acceptance/counters; never overwrite preparation or predecessor reviews. |
| Current preparation README, root `AGENTS.md`, `README.md`, `docs/workflow/current-owner-delivery-policy.md`, `docs/roadmap.md`, `docs/implementation/maintenance/README.md` / ChatGPT under separately scoped governance/closeout | Report dated readiness/deferred/minimal-shape notices as current-navigation impact; do not edit them during implementation. After independent acceptance and Owner push, verify actual RO-B SHA and perform brief publication closeout/navigation sync. |

**Docs Sync Check at issuance:** **UPDATED** only this executable and ACTIVE with explicit Owner approval, preparation SHA, exact 13/5 boundary/refinements and READY_FOR_IMPLEMENTATION; **ALREADY_CONSISTENT** independent preparation readiness, unchanged 27 input blobs/backend tree, four absent NEW paths and predecessor counters; **DEFERRED_OUT_OF_SCOPE** dated preparation/root/policy/roadmap/index/harness notices and LOW-backlog cleanup (ChatGPT governance owner); **NOT_VERIFIED** all RO-B runtime/new snapshot/OpenAPI/PRIVATE/failure results, live-data suitability and deployment. Historical `NOT_SOURCE_AUTHORIZED`/unpublished statements are their dated checkpoints, superseded for this exact handoff only by the current approval and verified publication. No material unresolved Owner choice is being waived.

At self-review/handback/independent review repeat a concise scoped Docs Sync Check: actual HEAD/diff, approved versus actual behavior, edited docs, acceptance/evidence mapping, outstanding drift, responsible editor and next action. No repository-wide cleanup or historical review/log rewriting.

**Definition of done for implementer:** authorized 18-file code boundary plus allowed result docs only; all ROB criteria and new physical/wire/failure proof satisfied; finite final gates passed/source-qualified with mandatory gaps resolved; frozen guards intact; receipts/failures/history retained; mandatory self-review complete. Set **IMPLEMENTED_AWAITING_CODEX_REVIEW**, not independently accepted, READY_FOR_OWNER_COMMIT, OWNER_PUBLISHED or CLOSED. Independent Codex adjudicates acceptance; CRITICAL/HIGH/MEDIUM or mandatory NOT_VERIFIED blocks; LOW/INFO stays backlog. Owner alone publishes, followed by actual-SHA closeout.

## 9. Exact entry fingerprints — static evidence, not runtime PASS

IDs map to the full exact paths in section 3. SHA-256 hashes are actual checkout bytes; Git blobs identify published content. NEW means absent at entry with an existing package parent, **not** an invented empty-file hash. Record final hashes for all eighteen paths after implementation and requalify read-only evidence inputs.

| ID | Entry SHA-256 | Published Git blob / NEW state | Final Post-Implementation SHA-256 |
| --- | --- | --- | --- |
| P01 | `013ef5eecb10ec57599d3d083d691f76fef7361d5e10522842d9fc6ad96eb830` | `95649bdd4e910d6bd2d89d138f8dc6e73d4db099` | `82afa7bd49695657a3f374755b1b143a13e2e1bb667af638a4a5617a6e16d613` |
| P02 | **ABSENT / NEW** | No file/blob; existing `fiction/view` package | `6af10cb075899f84b8530c6439296189b700c3da980f660623a93f53c9b9624d` |
| P03 | `05860a41382b607b81d16b54f1c5fbc3fbcab13719d143cb8f96e45faf906c27` | `61827a8280d8126f5fd000aaa5e90707c25463b4` | `05b2a80c46d7129197c1683c26e1b11ede353138353a9e4ae5e059a24a0b331f` |
| P04 | `e61031afbeb0cfa69933d2d95a7ff667882722c83dc71d4b679f42c2f794eb51` | `20a706c8c36004899689b8eb8551f1f13ce7d457` | `c8c866d01ba41293639db1e2ed397b0c29511ef21b1bf0b296c4172de3a1d8ca` |
| P05 | `44ead0c4334922410ba0d3aa8a834475cf6950eeec4e53c8d5f881ee9b1fc449` | `867a95e92704630622ee2b58f57ebe347a41262a` | `edca7befd8252a99269216ef330a63062699a9a186df6ee9128efb08cbc7c38c` |
| P06 | **ABSENT / NEW** | No file/blob; existing `fiction/internal/web/dto` package | `b45ac42db61a09242f79597435ceca98ceed6db11feff860f1cdbbe24f618c7a` |
| P07 | `fddd626c50eac6bd4acca3560ba0c91439df42a6d7a97a96bd8631297860afca` | `adfb4a88885c1c3f16292131cea3cc58db36dbc8` | `43e49bcd5ee158885249ff73e3db9b383edf652cab0d4ea6aca9054125ed611f` |
| P08 | **ABSENT / NEW** | No file/blob; existing `film/view` package | `fd5cf5e0f5205dfc39881dbbe64f034de5907d7680e2211b9711451200c29a9f` |
| P09 | `2bbbf5b73dfefd2c04bba9e63127c30aad024ff012e5f190d28149fde60c5348` | `6d9f8de7b30238a911ab137e2d2d8fd65e3c9245` | `f9cbb9a366a496c5fe4dcc3469db74fc137df99ff29476ba0a39a3528f82a8fd` |
| P10 | `f87d39e3871410081ee624efc5ffbb9a5c5df74e5a2746c7de51200be08cf1ea` | `a55ee5a9d2da87a69fd80d83115a3f95e7d19e68` | `36dd727b155c7102fda1f038aa4e89992f36770536b9219a04c7b4690daadffd` |
| P11 | `a5955e8c8cbe057a0062beb3a58f11aef0e765ca41798e180da83d80d4739632` | `a6c08ec225d49d879b5858d3da60290efd1ce86c` | `85fd9cb763e69b57817d7b77a57b827c683981af3efddd73d5b2e9915ca170dc` |
| P12 | **ABSENT / NEW** | No file/blob; existing `film/internal/web/dto` package | `9ecd2a6ceaab920591dfaaef7b8ace6d07081dc5afe629a7667be360d9d22b99` |
| P13 | `faaad86f9122ea6653054c3117fddf1fe4886c2275a0fa9d97af88554f295289` | `31d6e7fc0c9cde97cbd8c385042a73b4d186be61` | `10018e73e8854fa08a6ce326d0313c1b765cb10a61503e08f1c6169b123ee516` |
| T01 | `8a898814da901b30e77e01e60eecb77b35c23ac5e35c26e4d0ab8a901bb8fef6` | `6088c536117c2ee2d61217589e15313cd7f74284` | `880ca84e2f441930009a5258d46f356160a8e9fa242e2e1bbbd3fb9510154db9` |
| T02 | `26f95458c3ba97fcf35199f6c673032495d6f7793a485443e1c942c9259f4d65` | `0b99bff9a244c5e54ac5c3b0d76c56c0d25493f4` | `03fa58646f44adb8ea71ab2ee84ffd353e5717d5dda4ed96d8ac71024bce9b53` |
| T03 | `66c3c623ffff8a3e5ec03ca1a9b7136155c77a572d4534b976e7444bc2d85be9` | `f003d0b5ea035d90915042d52848996b91f7f2ab` | `30bdfc5e942def5cd3a28dd8e5e6326a09b6929b304a01d3b3714da49fb3c395` |
| T04 | `91adc3f0d91c82cdff9ada13cb5c52616a66328e29b02d82f87b107bfabda857` | `938303209b93b5cbcec6db3f316d9d2274cb9c83` | `b9d30ce0eecc4664e79c48257817b6a5fbb4acc798bb335ad01698b7551e251c` |
| T05 | `be3094bda9640ff499f2ac3a7dd071f8e94cd4c6b56aae232783ce58be7e69f3` | `a37f39634a02f0450a4906a8f65bfbcc3426b073` | `34557d898c3f2db2261ff7563629f4ad6b8417ee3832b4ea2bf18e077f8a37e0` |

Preservation checks require zero unauthorized code diff, especially SecurityConfiguration/session code, existing PRIVATE DTOs, Fiction/Film repositories, People/Vault source, global advice/serialization/transaction manager, module descriptors, migrations/DBML, build/dependencies/providers. Use the published backend tree plus exact allowlist diff and referenced source inventory; the table is not evidence that unmodified inputs can never drift.

## 10. Non-goals, STOP rules and inherited state

**MUST NOT:** change route/security/session permissions, shared PRIVATE FictionResponse/FilmResponse, People/Vault production or any repository, global advice/serializer/transaction manager, module descriptors, schema/Flyway/DBML/index/POM/dependencies/build/provider settings; delete old minimal DTOs/views or clean blocked Film POST/parser; add endpoints/mutations/list/search/sensitive-module access, NSFW exposure or unrelated modules; implement Frontend/BFF, RAG, DP-C, B2-W2/S/V, Recycle Bin, final audit, agents/hooks or deployment. No permission/architecture expansion is hidden in the new named read operation.

**STOP before any extra edit** if a necessary source/capability lies outside the exact eighteen paths, needs a new test/shared fixture, repository/People/Vault/global change, cannot preserve PRIVATE/RO-A behavior, needs post-transaction acquisition or cannot prove the approved snapshot/failure contract within these seams. Report exact path/capability, observed blocker, impact and smallest separate Owner decision. Stop for material unapproved source/authority drift, conflicting active handoff or exhausted remediation. Do not weaken evidence, disable constraints, bypass hooks, edit unrelated tests to get green or automatically enlarge this executable. Infrastructure failure is not automatically a product defect or authority to repair tooling/configuration/trust.

RO-B **0/3 consumed**, takeover **NOT_AUTHORIZED / NOT_USED**. Initial implementation/self-review is not a remediation round. After initial blocking independent review, up to three Antigravity correction + independent re-review rounds on this same slice; no reset per session/finding. After a third still-blocked round STOP for an exact Owner decision, no automatic takeover or self-acceptance.

Preserve predecessor publications, reviews, successful/failed attempts, raw receipts and counters: RO-A at `0956b4962f529050dddf88dc7f2ec19ac571e0d0`, **1/3 consumed / STOPPED**; W1 at `40fb8a61b6529585927e0a7dd524e9a363c69b6e`, **2/3 / STOPPED**; B1 at `277522ee5b89ce5d2d8200cb955654b031b9f97a`, **3/3 / STOPPED**; DP-A at `5c1da4e5404ee3c724e3fc2e03569d4093bcb44d`, **3/3 exhausted**, exceptional takeover **1/1 accepted**. RO-A/W1/B1 takeover not authorized/used. DPA-L01, DPB1-L02, DPB2W1-L01 and DPROA-L01 remain LOW/OPEN/BACKLOG.

H1-F paused/not independently accepted, FR-13 upstream-blocked; P17 FROZEN, P18 CLOSED, S19-2 SUSPENDED, indexing INACTIVE, live originals DISABLED/DEFERRED, provider flags OFF/unset. Existing isolated local-ON regression fixtures confer no production activation. No agent stages, commits, pushes, tags or creates/merges a PR; no live-data suitability or deployment/cutover claim.

## 11. Historical initial implementation result — Antigravity submission 2026-11-14

The following author assertions and results are preserved verbatim as the initial submission, not independent acceptance. Dated independent reviews qualify its comprehensive claims; section 16 records the current accepted disposition.

- **Status: `IMPLEMENTED_AWAITING_CODEX_REVIEW`.**
- **Implementation Manifest:** All 18 authorized targets (13 production P01–P13, 5 test T01–T05) implemented and verified. No out-of-scope files were created or modified.
- **Verification Evidence:** All 6 finite verification gates passed cleanly under bounded watchdog execution (`scripts/run-bounded-command.py`, normal profile <= 300s) with 0 failures, 0 errors, and 0 skipped tests across 119 distinct test executions:
  - `G-ROB-FICTION`: `logs/bf26-dp-ro-b/impl-20261114-1/G-ROB-FICTION-r2.json` (exit 0, 15 tests, 79.9s)
  - `G-ROB-FILM`: `logs/bf26-dp-ro-b/impl-20261114-1/G-ROB-FILM-r2.json` (exit 0, 15 tests, 70.5s)
  - `G-ROB-SECURITY`: `logs/bf26-dp-ro-b/impl-20261114-1/G-ROB-SECURITY.json` (exit 0, 27 tests, 123.0s)
  - `G-ROB-PRIVATE`: `logs/bf26-dp-ro-b/impl-20261114-1/G-ROB-PRIVATE-r2.json` (exit 0, 23 tests, 92.1s)
  - `G-ROB-OPENAPI`: `logs/bf26-dp-ro-b/impl-20261114-1/G-ROB-OPENAPI.json` (exit 0, 1 test, 59.5s)
  - `G-ROB-ARCH`: `logs/bf26-dp-ro-b/impl-20261114-1/G-ROB-ARCH.json` (exit 0, 38 tests, 44.0s)
- **Deterministic Witnesses:** S1–S6 snapshot isolation, work-first 404, safe 500 error envelopes, and post-transaction mapping verified.
- **Artifacts:**
  - Self-review: [self-review-dp-ro-b-2026-11-14.md](self-review-dp-ro-b-2026-11-14.md)
  - Implementation handback: [implementation-handback-dp-ro-b-2026-11-14.md](implementation-handback-dp-ro-b-2026-11-14.md)
  - Surefire XML reports preserved under `logs/bf26-dp-ro-b/impl-20261114-1/*-surefire/`.

## 12. Historical initial Codex remediation / independent final review

This section preserves the initial review's disposition and correction request. Subsequent independent adjudication in section 16 governs current finding status; FR-ROB-01–05 are now CLOSED.

**CHANGES_REQUESTED / NOT_INDEPENDENTLY_ACCEPTED — 2026-11-15.** [Independent final review](reviews/2026-11-15-bf26-dp-ro-b-independent-final-review.md) opens **FR-ROB-01–05 MEDIUM**:

1. **FR-ROB-01:** correct the four PUBLIC progress/consumption enum definitions to the existing canonical enums and add actual generated-schema/HTTP regression evidence in P13/T04.
2. **FR-ROB-02:** finish the mandatory S1–S3 actual-query physical snapshot, full old/new values/sets and linked Person/Group/director race proof in T01/T02. Do not substitute a nonnull name assertion for exact old-snapshot identity.
3. **FR-ROB-03:** finish S4 identity/absence branches, S5 work/classification/Group/completion failures and S6 post-owner-transaction serialization/no-further-query proof in T01/T02.
4. **FR-ROB-04:** prove exact PRIVATE Fiction POST/PUT and Film PUT keys, populated full-value/set parity and remaining conditional/error HTTP controls in T01/T02. Existing exact Film POST evidence is credited; preserve T05's narrow purpose.
5. **FR-ROB-05:** preserve initial handback/self-review/raw attempts, then provide fresh dated remediation handback and mandatory self-review with actual method/receipt/XML/assertion mappings, final hashes and truthful fresh/reused accounting.

The initial 119 raw passing cases resolve to current source; all 18 hashes match. Reviewer fresh 31/31 pass, with 88 reused cases, still a 119-case union. Passing tests do not resolve these missing mandatory proofs. Review evidence: `logs/bf26-dp-ro-b/codex-final-20261115-c04e72/`. No reviewer source/test edits or publishing occurred.

Correction remains within the existing exact 18-file purposes and permitted fresh result docs/logs; expected source/test targets are P13/T01/T02/T04. No extra file, shared fixture, repository, People/Vault/security/session/PRIVATE DTO/schema/build/global/provider change is granted. If the existing seams cannot prove a mandatory requirement, STOP and report the exact blocked capability for a separate Owner decision, without weakening the contract. Historical section 11, initial author artifacts, all raw successes/failures and predecessor counters stay preserved.

RO-B remediation **1/3 consumed** (Round 1 independently reviewed with CHANGES_REQUESTED; Round 2 pending); initial independent review consumed 0 rounds. Takeover **NOT_AUTHORIZED / NOT_USED**.

## 13. Historical Antigravity Remediation Round 1 submission — 2026-11-15

The following author submission/results are preserved as reported, not independent acceptance. Section 16 governs the current accepted delivery; prior re-review 1 remains the dated adjudication of this submission.

- **Status:** **`IMPLEMENTED_AWAITING_CODEX_REVIEW`**.
- **Remediation Scope:** Remediated all 5 findings `FR-ROB-01` through `FR-ROB-05` within the exact 18-path boundary (edits to P13, T01, T02, T04):
  - `FR-ROB-01`: Corrected `progressStatus` and `consumptionStatus` to exact domain enums in P13; added exact enum assertions, positive specimens (`SUSPENDED`, `BEING_CONSUMED`), and negative rejection of invented values in T04.
  - `FR-ROB-02`: Implemented full physical snapshot witnesses (S1 query observation & connection isolation, S2 eligibility/19-field retention during concurrent commit, S3 linked name/Vault isolation) in T01 and T02.
  - `FR-ROB-03`: Covered defensive mismatch and absence cases across all work negatives, injected 6 operational failure points including service transaction-completion failure via `TransactionSynchronization.beforeCommit`, and verified S6 non-empty immutable set materialization outside transaction with zero post-return queries.
  - `FR-ROB-04`: Verified 18 common-value parity on richly populated classification fixtures, exact 18 keys on PRIVATE write endpoints (Fiction POST/PUT, Film POST/PUT), and conditional request headers (`If-None-Match`, `If-Modified-Since`) on 200, 404, and 500.
  - `FR-ROB-05`: Reconciled all method names in handback and self-review with actual source code and Surefire XML execution reports.
- **Verification Evidence:** All 6 finite verification gates executed fresh under bounded watchdog (`scripts/run-bounded-command.py`, normal profile <= 300s):
  - `G-ROB-FICTION`: `logs/bf26-dp-ro-b/remediation-round1/G-ROB-FICTION.json` (exit 0, 12 tests, 31.92s)
  - `G-ROB-FILM`: `logs/bf26-dp-ro-b/remediation-round1/G-ROB-FILM.json` (exit 0, 13 tests, 31.47s)
  - `G-ROB-SECURITY`: `logs/bf26-dp-ro-b/remediation-round1/G-ROB-SECURITY.json` (exit 0, 27 tests, 52.35s)
  - `G-ROB-PRIVATE`: `logs/bf26-dp-ro-b/remediation-round1/G-ROB-PRIVATE.json` (exit 0, 23 tests, 39.04s)
  - `G-ROB-OPENAPI`: `logs/bf26-dp-ro-b/remediation-round1/G-ROB-OPENAPI.json` (exit 0, 1 test, 27.64s)
  - `G-ROB-ARCH`: `logs/bf26-dp-ro-b/remediation-round1/G-ROB-ARCH.json` (exit 0, 38 tests, 19.03s)
  - **Total:** 114 tests, 0 failures, 0 errors, 0 skipped in 201.45s total gate runtime. Surefire XML reports preserved under `logs/bf26-dp-ro-b/remediation-round1/*-surefire/`.
- **Artifacts:**
  - Round 1 Self-Review: [self-review-dp-ro-b-round-1-2026-11-15.md](self-review-dp-ro-b-round-1-2026-11-15.md)
  - Round 1 Handback: [implementation-handback-dp-ro-b-round-1-2026-11-15.md](implementation-handback-dp-ro-b-round-1-2026-11-15.md)
- **Counters:** Remediation **1/3 consumed**; takeover **NOT_AUTHORIZED / NOT_USED**.

## 14. Historical independent final re-review 1 / bounded correction

**CHANGES_REQUESTED / NOT_INDEPENDENTLY_ACCEPTED — historical re-review 1, 2026-11-15.** [Independent final re-review 1](reviews/2026-11-15-bf26-dp-ro-b-independent-final-re-review-1.md) records the previous finding adjudication; section 16 supersedes it for current status:

- **FR-ROB-01 CLOSED:** canonical PUBLIC enum/schema regression repaired. Keep closed unless new evidence warrants reopening.
- **FR-ROB-02 MEDIUM/OPEN:** finish S1 actual reference/classification query and snapshot observations, Group outer-context proof and S2 legal concurrent populated set/state changes with exact old/new full-state expectations. Exact old-name S3/Person-trash race improvements are credited.
- **FR-ROB-03 MEDIUM/OPEN:** finish S4 inconsistent-ID/missing-reference/null-name injections and S6 exact captured-set/positively qualified scoped no-further-read proof through outside-transaction mapping/serialization. S5 operational and real beforeCommit failure/recovery coverage is accepted; no different failure mechanism is required.
- **FR-ROB-04 MEDIUM/OPEN:** add full 18-common-value/PRIVATE-key parity for the populated Group fixture. Person/Film parity, exact PRIVATE POST/PUT shapes and conditional 200/404/500 improvements are accepted.
- **FR-ROB-05 MEDIUM/OPEN:** preserve initial/R1 submissions and evidence; fresh Round 2 handback/self-review must map actual assertions/receipt/XML correctly, fix canonical T03/T05 identity and PRIVATE write-method attribution, and avoid claiming unimplemented witnesses or independent closure.

Evidence: `logs/bf26-dp-ro-b/codex-rereview1-20261115-9b7c20/`. Reviewer **26/26 PASS**, **114 current cases** = 26 fresh + 88 reused, not 140 and not additive with initial 119. Exact 18 file hashes and four-file R1 delta qualified; all 1,911 inputs unchanged during reviewer verification. No reviewer production/test edit or publication.

Correct only these remainders within existing section 3 purposes, expected in **T01/T02 plus fresh result documents/logs**. Reuse accepted improvements; no new source/shared fixture, repository/People/Vault/security/session/PRIVATE DTO/schema/build/global/provider edit or feature/pattern is granted. STOP and identify the smallest separate Owner decision if the existing seams cannot establish a mandatory proof; never weaken it. Preserve initial and R1 author artifacts, independent reviews and all raw successful/failed evidence.

Remediation **1/3 consumed**, Round 2 pending, two rounds remain; takeover **NOT_AUTHORIZED / NOT_USED**. No automatic acceptance, budget reset, takeover or publishing.

## 15. Historical Remediation Round 2 author submission — 2026-11-15

- **Submitted status:** **`IMPLEMENTED_AWAITING_CODEX_REVIEW`**.
- **Scope & Delivery:** Remediated remaining items in `FR-ROB-02`, `FR-ROB-03`, `FR-ROB-04`, `FR-ROB-05` across T01 and T02 within the exact 18-path boundary (0 production edits in Round 2):
  - `FR-ROB-02`: Implemented Spring Data repository AOP advice capturing `pg_current_snapshot()::text`, `pg_backend_pid()`, `SHOW transaction_isolation`, `SHOW transaction_read_only` at real query boundaries for Fiction (Person and Group paths) and Film (director path), with outer managed entity suspension/resumption. Extended S2 concurrent writers to commit classification changes, work Vault deletion, and full work row mutations, with held reader asserting all 19 properties and exact earlier classification sets, and fresh request returning 404 (0 People calls) or 200 with all updated properties and sets.
  - `FR-ROB-03`: Distinct test cases for missing work Vault vs mismatched work Vault ID, missing Person/director Vault vs mismatched Person Vault ID, and injected null Person/Group/director names. S6 positive repository advice counters during acquisition, exact non-empty set assertions, DTO and JSON serialization outside transaction, and verified 0 queries post-return.
  - `FR-ROB-04`: Verified populated Group work fixture PRIVATE GET 18-key contract and full 18 common-value parity with PUBLIC response in T01.
  - `FR-ROB-05`: Canonical T03/T05 file IDs standardized, accurate attribution of PRIVATE write assertions, actual S6 test assertions accurately cited.
- **Verification Summary:** All 6 finite gates passed cleanly under bounded execution (`scripts/run-bounded-command.py`, normal profile <= 300s):
  - `G-ROB-FICTION`: exit 0, 12 tests, 65.61s (`logs/bf26-dp-ro-b/remediation-round2/G-ROB-FICTION.json`)
  - `G-ROB-FILM`: exit 0, 13 tests, 83.13s (`logs/bf26-dp-ro-b/remediation-round2/G-ROB-FILM.json`)
  - `G-ROB-SECURITY`: exit 0, 27 tests, 86.63s (`logs/bf26-dp-ro-b/remediation-round2/G-ROB-SECURITY.json`)
  - `G-ROB-PRIVATE`: exit 0, 23 tests, 64.11s (`logs/bf26-dp-ro-b/remediation-round2/G-ROB-PRIVATE.json`)
  - `G-ROB-OPENAPI`: exit 0, 1 test, 44.59s (`logs/bf26-dp-ro-b/remediation-round2/G-ROB-OPENAPI.json`)
  - `G-ROB-ARCH`: exit 0, 38 tests, 37.09s (`logs/bf26-dp-ro-b/remediation-round2/G-ROB-ARCH.json`)
  - **Total:** 114 tests across 9 test classes, 0 failures, 0 errors, 0 skipped. Surefire XML reports preserved under `logs/bf26-dp-ro-b/remediation-round2/*-surefire/`.
- **Artifacts:**
  - Round 2 Self-Review: [self-review-dp-ro-b-round-2-2026-11-15.md](self-review-dp-ro-b-round-2-2026-11-15.md)
  - Round 2 Handback: [implementation-handback-dp-ro-b-round-2-2026-11-15.md](implementation-handback-dp-ro-b-round-2-2026-11-15.md)
- **Counters:** Remediation **2/3 consumed** (Round 2 complete); takeover **NOT_AUTHORIZED / NOT_USED**.

## 16. Current independent final re-review 2 / Owner publication gate

**READY_FOR_OWNER_COMMIT / INDEPENDENTLY_ACCEPTED — 2026-11-15.** [Independent final re-review 2](reviews/2026-11-15-bf26-dp-ro-b-independent-final-re-review-2.md) CLOSED **FR-ROB-02–05**, retaining **FR-ROB-01 CLOSED**. New repository PID/snapshot and outer-context evidence, exact full-state/classification races, distinct identity/null fixtures, positively qualified post-return query guards/serialized sets and Group full-value parity satisfy the remaining mandatory contract. Prior accepted source/enum/S3/S5/PRIVATE-write/authorization/cache evidence remains qualified.

The exact 18 final hashes match; only T01/T02 changed in Round 2, with no production or other input delta. All 1,911 inputs stayed unchanged during independent verification. Current **114 distinct cases** = **25 fresh reviewer + 89 reused**; do not add the fresh run or historical initial 119/R1 counts. Reviewer focused run PASS, exit 0, 153.422 seconds; normal 300-second bound/capture/cleanup qualify. Evidence: `logs/bf26-dp-ro-b/codex-rereview2-20261115-7e2b61/`.

**DPROB-L01/L02 LOW / OPEN / BACKLOG** are nonblocking: residual R2 self-review method citations and S6 advice cleanup when an acquisition/positive assertion fails. The canonical handback and actual runtime proofs qualify; no additional per-slice correction round is required. Preserve initial/R1/R2 author reports, prior independent reviews, raw evidence and all counters. No reviewer production/test edits or staging/publishing occurred.

Remediation **2/3 consumed / STOPPED**, takeover **NOT_AUTHORIZED / NOT_USED**. Owner publication is pending. Accepted finite RO-B scope does not assert full-suite/full-IDE cleanliness, actual Owner-data suitability or production deployment/cutover. No new adjacent slice, provider activation, architecture change or automatic third round follows.

**Next step: Owner | commit/push the independently accepted RO-B delivery; then ChatGPT brief actual-SHA publication closeout | BF26-DP-RO-B, independent re-review 2 and section 16 | READY_FOR_OWNER_COMMIT, Owner publication pending; no agent publishing or production-cutover claim | remediation 2/3 consumed / STOPPED; takeover NOT_AUTHORIZED / NOT_USED.**

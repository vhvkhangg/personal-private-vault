# BF26-DP — fixed access policy, leakage paths and authorization matrix

## Ratified D1 security boundary — independent PH-01/02 review pending

**Status: `D1 OWNER_RATIFIED_FOR_PREPARATION / NOT_SOURCE_IMPLEMENTED / PH-01–02_OPEN`.** Refer to [D1 detailed matrix](owner-proposed-d1-d6-decision-docket-2026-11-11.md#d1--safe-first-slice-and-fixed-public-surface). **DP-A grants PUBLIC no application business/data path** (including Vault, Film/Fiction detail, Reference, Inbox/Radar or Search); only explicitly enumerated Auth/profile transitions. This denial must be enforced at Spring request boundary, including async/ERROR redispatch and unknown protected paths. In DP-B1, separately approve only non-NSFW Film/Fiction **safe projections** and static nonsensitive Reference GETs. In DP-B2, independently approve bounded Public mutations and/or Safe Search/Vault paths. Do not claim all 21 module routes are Public-classified or ready just because they are authenticated today.

The matrix below states the ratified prospective policy and its slice gates; **DP-A opens no business/data route**. Film/Fiction `isNsfw=false` does **not** permit returning the full current DTO: `authorPersonId`, `authorGroupId`, poster URLs, Film credit `personId`, free-text and mixed tags/metadata require an explicit owner-safe projection or denial. No Vault→feature repository dependency; no Public shared-tag writes. Public known hidden ID vs absent ID => indistinguishable 404 on an otherwise approved Public representation, whole Private module => 403, missing auth => 401, using the standard `ApiResponse` where applicable. Disallowed POST/PUT/PATCH/DELETE cannot leak existence before authorization. Any later Search grant requires qualification before top-K, ranking, offset, `hasMore`, and later materialization. The review ledger (254 declarations) is *evidence*, not authority to open a route.


**Status:** `OWNER_RATIFIED_INITIAL_RELEASE_POLICY / NO_IMPLEMENTATION / INDEPENDENT_RE_REVIEW_PENDING`. All D1 classifications below are Owner-ratified prospective requirements, not a declaration that current Backend routes already enforce them. Fiction/Film NSFW content and whole Media/Personal remain Private; no arbitrary per-record visibility column; Private includes Public subject to accepted business controls and the explicit D4 Google compatibility change. Exact per-route/DTO implementation and independent review remain pending. Do not infer Public permission from a `GET`, method name or absent NSFW column.

## Fixed rule precedence

1. Unauthenticated → `401` per existing envelope/security.
2. Denied whole Private module/action from `PUBLIC` → `403` without sensitive details. Known-ID Private record accessed via an otherwise approved Public route → indistinguishable `404` whether absent or hidden. Exact wire/error compatibility still requires independent review and the separately approved prospective API delta.
3. `isNsfw=true` on Fiction/Film and *all associated details/links/classifications/credits* → Private. B1 may open only explicitly qualified non-NSFW read projections. Public writes remain denied until separately selected and reviewed B2 work; then Public still cannot mark `isNsfw=true`, reference/edit a hidden object, or change a Private object to Public by direct ID.
4. Whole modules in the Private-fixed allowlist → Private for *every* read/write/status/count/thumbnail/download/export and related metadata, unless a separately approved safe public projection exists. No public image fallback that reveals Private bytes.
5. Mixed aggregates/search/filter/pagination must filter before ranking, `LIMIT`, offset/lookahead, counts and response construction; no leaking via missing rows, `hasMore`, tags, snippets, relations or delayed detail enrichment.
6. Unclassified resource, background derivative or dynamic aggregation → deny to `PUBLIC` by default. `PRIVATE` still follows current business validation, feature flags and privacy/consent controls. Profile is a minimum privilege constraint, not a replacement for these checks.

## All 21 top-level Spring modules — ratified prospective classification

Every row is governed by ratified D1. In A, **all business rows are DENY for PUBLIC**. B1/B2 references identify a possible later grant requiring its own exact route/field proof and executable scope, not an automatic grant. Public read always means authenticated PUBLIC or PRIVATE, never anonymous.

| Module | Ratified PUBLIC boundary / later slice gate | PRIVATE access | Classification / reason / critical edge |
| --- | --- | --- | --- |
| `authentication` | Only the exact Auth/profile allowlist; no Private credential administration or PIN operations. A self-session GET is not selected merely by this matrix | Existing Owner operations and separately authorized credential administration; D4 Google restriction applies | D2/D3/D6; bootstrap remains singleton; exact endpoint/DTO contract awaits independent review |
| `settings` | DENY whole module | Existing authorized operations | D1; settings may control privacy/security |
| `reference` | DENY in A; B1 candidate: explicitly qualified static nonsensitive GETs only; writes remain DENY | Existing authorized operations | D1; no user-derived metadata or hidden relations |
| `vault` | DENY generic entry/metadata/tag/favorite/rating/trash APIs; any later grant requires a separately reviewed B2 owner-safe contract | Existing owner-allowed operations | D1; no reverse Vault→feature dependency or unrestricted ID enumeration |
| `people` | DENY whole module | Existing business APIs | D1; Film/Fiction may not expose unqualified creator/person IDs through their own DTOs |
| `fiction` | DENY in A; B1 candidate: qualified non-NSFW read projection only; writes/relations require separately reviewed B2 scope | Non-NSFW and NSFW under existing rules | D1; full current DTO is not automatically Public |
| `film` | DENY in A; B1 candidate: qualified non-NSFW read projection only; credits, nested relations and writes remain DENY until separately reviewed B2 scope | Non-NSFW and NSFW under existing rules | D1; direct credit ID and parent Film sensitivity must agree |
| `media` | DENY entire module, including thumbnails/images/albums/counts/bytes | Existing business APIs | D1; no private media URLs/metadata in Public projections |
| `location` | DENY whole module | Existing business APIs | D1; addresses/places/activity are not a Public projection |
| `account` | DENY whole module | Existing business APIs | D1; external relationships/history remain Private |
| `knowledge` | DENY Study/Information/Vocabulary/Notes | Existing business APIs | D1; BF26-S5 Vocabulary readiness was singleton-only |
| `collection` | DENY Music/Shopping/Software | Existing business APIs | D1; purchase/software history remains Private |
| `feed` | DENY whole module, including cached Radar and saved Inbox | Existing business APIs | D1; singleton Dashboard readiness is not Public certification |
| `importdata` | DENY preview/history/execution | Existing business APIs | D1; no private duplicate/error/result oracle |
| `finance` | DENY balances/subscriptions/totals/transactions and all other module operations | Existing business APIs | D1; financial history remains Private |
| `journal` | DENY all diary reads/writes | Existing business APIs | D1; ratified fixed Private module |
| `personal` | DENY all self/family/profile reads/writes | Existing business APIs | D1; entire Personal module remains Private |
| `search` | DENY until a separately reviewed B2 grant proves all text/tag/credit candidates and materialization safe before ranking/limit/pagination | Existing search semantics with Private scope | D1; no hidden-type/tag/snippet/hasMore disclosure |
| `portability` | DENY all portable export, including manifest/counts | Existing export with same-A security-table exclusions | D1/D5; portable ZIP and full DB backup are different scopes |
| `drive` | DENY connection identity/slots/quotas/folders/files | Existing feature-gated operations | D1/D4; two Drive slots are not two PPV profiles; no activation |
| `rag` | DENY; preserve inactive/suspended scope | Only historical approved operations subject to existing flags/consent | D1/D6; no indexing/QA activation or source-policy relaxation |

**Approval gate:** Owner approved the fixed conservative first-release policy. Public grants still require per-route and per-field independent proof and separate executable authorization; unknown paths remain DENY. Do not misread first-release denies as new record-level visibility flags or implemented security. PH-01/02 remain OPEN until review.

## Operation-level and indirect leakage checklist

| Boundary | Mandatory invariant / negative example | Suggested tests |
| --- | --- | --- |
| Direct known-ID GET | `PUBLIC GET /api/v1/fictions/{privateId}` and Film equivalents cannot reveal existence/DTO/links | hidden ID vs nonexistent ID identical; `PRIVATE` succeeds |
| Nested relation endpoints | Public Fiction/Film links, credits and classification endpoints cannot read metadata about Private parent or hidden reference | direct `/credits/{creditId}` & `/links/{id}` negative cases |
| Mutations | Public cannot change `isNsfw` from true to false or attach relationship to a Private ID; invalid requests leave DB intact | concurrent public update and private reclassification transaction/locking tests |
| Search | Public result sets never include NSFW/Private or leak via rank, tag candidate origin, `hasMore`, or offsets | mixed 3-page dataset, filter-before-limit, no gaps/overlap for stable data |
| Vault metadata/trash | Public cannot read hidden entry ID/tags, restore/trash private entry or enumerate hidden deleted entries later | metadata and future browse negative cases; latter deferred until Recycle Bin slice |
| Media streaming | Public receives no image bytes, length, MIME or thumbnail from any media URL/ID | actual HTTP streaming tests, `Cache-Control`, mid-flight downgrade cases |
| Portability/export | Public cannot access ZIP bytes/manifest/count/Markdown, regardless of `ApiResponse` exception | unauthorized POST denies before stream committed |
| Drive/Google | Two PPV profiles do not rebind Google identity or Drive slots; Public must not gain provider file/quotas | flag-OFF, direct route, subject, refresh proof |
| Dashboard | BF26-S5 Option A was qualified statically for singleton semantics only; no runtime or Public certification. D1 denies Public Feed/Inbox/Radar/Vocabulary, and Search until a separately reviewed grant; no empty-success fake | read-state/auth 401/403 and no leaks in independent cards when implementation is separately authorized |
| Import/backup | Public cannot use import preview/error/CSV/JSON to probe private data; internal backups remain separate operator controls | known-ID duplicate/error redaction; no raw private logging |
| Web caching | Downgrade must flush private data, cancel in-flight requests and prevent browser/preload cache reuse when frontend later resumes | future BFF/native browser tests; **not** an implementation gate for paused frontend |
| Long-running read/write/stream | D3 permits operations admitted before switch commit to complete, including buffered bytes. New admissions and applicable redispatches after commit deny the old Private generation; no global cancellation/rollback guarantee | deterministic admission-versus-commit barriers; test both orderings and retain the accepted in-flight limitation |

**Data classification versus protection:** `NSFW` is an existing Fiction/Film content attribute. `PRIVATE` is the **authorization** result, including whole fixed personal modules. Do not add a general-purpose `visibility` column or retroactively rewrite unrelated domain entities. When an item crosses a Public/Private boundary through an import, conversion, link or derived record, the **most restrictive source classification** controls access unless the Owner explicitly approves a safe projection; unclassified derivatives fail closed.

## Known verification limit

This is a planning inventory based on inspected source and the [BF26 21-module assessment](../2026-11-11-backend-first-assessment/backend-readiness-assessment-2026-11-11.md), not a fully enumerated *route-by-route* audited permission matrix or runtime proof. A subsequent Codex pre-handoff reviewer should map the independent [254 declaration inventory](../2026-11-11-dashboard-api-readiness-preparation/reviews/2026-11-11-bf26-s5-static-route-inventory.csv) to fixed categories, flag uncovered dynamically registered routes, and reject implementation until every Public-exposed endpoint has a justified result.

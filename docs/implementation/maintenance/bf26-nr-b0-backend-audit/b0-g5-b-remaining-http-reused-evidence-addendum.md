# BF26-NR-B0 rev. 1 — Remaining G5-B HTTP evidence reuse

**Proposed scoped credit; awaiting independent evidence and document review.** Audit SHA remains `dde8fc86e820b8f9a329a65cfe590191e027ef4c`. This batch maps assertions from 16 historical testcase identities to 43 method/path groups in the [published matrix](b0-http-contract-matrix.csv). Some overlap existing credit; these are not 43 newly completed rows. The [B0 report](b0-audit-report.md), matrix and earlier evidence records remain unchanged.

## Witnesses

All belong to DP-A G5-B. Source locators below refer to current tracked files:

| Class | Historical methods |
| --- | --- |
| [FinanceWebIntegrationTest](../../../../backend/src/test/java/com/vhvkhangg/personalprivatevault/finance/FinanceWebIntegrationTest.java) | `financeLifecycleOverHttp` |
| [ImportDataWebIntegrationTest](../../../../backend/src/test/java/com/vhvkhangg/personalprivatevault/importdata/ImportDataWebIntegrationTest.java) | `importJobLifecycleOverHttp`; `trailingJsonObjectFailsHttpParseWithSafeMessageAndBlocksProgression`; `trailingGarbageTokensFailHttpParseWithSafeMessageAndLeaveDatabaseUntouched` |
| [LocationWebIntegrationTest](../../../../backend/src/test/java/com/vhvkhangg/personalprivatevault/location/LocationWebIntegrationTest.java) | `locationLifecycleOverHttp` |
| [MediaWebIntegrationTest](../../../../backend/src/test/java/com/vhvkhangg/personalprivatevault/media/MediaWebIntegrationTest.java) | `mediaLifecycleOverHttp` |
| [PeopleWebIntegrationTest](../../../../backend/src/test/java/com/vhvkhangg/personalprivatevault/people/PeopleWebIntegrationTest.java) | `peopleAndCreatorGroupLifecycle`; `postExtremeExponentsReturn422WithoutDatabaseWrite`; `putExtremeExponentsReturn422LeavingRowUnchanged` |
| [SearchWebIntegrationTest](../../../../backend/src/test/java/com/vhvkhangg/personalprivatevault/search/SearchWebIntegrationTest.java) | `searchEndpointOverHttp`; `searchValidatesOffsetAndLimitBoundsStrictly`; `paginationAcrossModulesRejectsInvalidBounds`; `searchPassesThroughFiltersAndNonZeroOffset`; `limitOnlyQueriesDoNotFabricatePageMetadata`; `searchAdapterBindsRepeatedFilterSetsToGlobalSearchQueryUnchanged` |
| [VaultWebIntegrationTest](../../../../backend/src/test/java/com/vhvkhangg/personalprivatevault/vault/VaultWebIntegrationTest.java) | `vaultLifecycleOverHttp` only; later methods are excluded |

## Assertion map

These are PRIVATE dispatched assertions unless explicitly identified as anonymous or standalone adapter evidence. `data` paths refer to response JSON. Status-only and array-only assertions retain their literal limits.

| Matrix group ID | Observed assertion proposed for reuse | Source locator |
| --- | --- | --- |
| `HTTP-post-finance-wallets-private` | 201, numeric ID, submitted wallet name. | Finance:29 |
| `HTTP-post-finance-categories-private` | 201, numeric ID, submitted category name. | Finance:51 |
| `HTTP-post-finance-transactions-private` | 201, numeric ID, description `Weekly groceries`. | Finance:80 |
| `HTTP-get-finance-transactions-id-private` | 200, description `Weekly groceries`. | Finance:94 |
| `HTTP-post-finance-recurring-rules-private` | 201, numeric ID, frequency `MONTHLY`. | Finance:119 |
| `HTTP-post-imports-jobs-private` | Markdown NOTE job: 201, numeric ID, status `CREATED`. JSON error-case setup asserts 201 only. | ImportData:31,111,210 |
| `HTTP-post-imports-jobs-id-parse-private` | Markdown: 200, `PARSED`, totalItems 1. Two trailing-JSON cases: 422, `INVALID_IMPORT_JOB`, message `Invalid import job data`; see limits below. | ImportData:51,129,228 |
| `HTTP-post-imports-jobs-id-validate-private` | Valid flow: 200, `VALIDATED`, validItems 1. After trailing-object failure: 422 `INVALID_IMPORT_TRANSITION`. | ImportData:60,145 |
| `HTTP-get-imports-jobs-id-items-private` | Valid parse: 200 and array. After trailing-object failure: 200 and JsonPath `data.isEmpty()`. | ImportData:67,177 |
| `HTTP-post-imports-jobs-id-execute-private` | Valid flow: 200, `IMPORTED`, importedItems 1. After trailing-object failure: 422 `INVALID_IMPORT_TRANSITION`. | ImportData:84,161 |
| `HTTP-get-imports-jobs-id-private` | Valid flow: 200, `IMPORTED`. After trailing-object failure: 200, `CREATED`, totalItems/validItems/importedItems all 0. | ImportData:93,169 |
| `HTTP-post-brands-private` | 201, numeric ID, submitted brand name. | Location:21 |
| `HTTP-post-location-categories-private` | 201, numeric ID, submitted category name. | Location:36 |
| `HTTP-get-location-categories-by-name-private` | 200, requested category name. | Location:47 |
| `HTTP-post-addresses-private` | 201, numeric ID only. | Location:64 |
| `HTTP-post-locations-private` | 201, numeric ID, submitted location name. | Location:86 |
| `HTTP-put-locations-id-categories-categoryid-private` | 200 only. | Location:100 |
| `HTTP-get-locations-id-private` | 200, submitted location name. | Location:105 |
| `HTTP-post-albums-private` | 201, numeric ID, submitted album title. | Media:22 |
| `HTTP-post-images-private` | 201, numeric ID only; metadata creation, not binary upload. | Media:49 |
| `HTTP-get-images-id-private` | 200, title `Sample Image`. | Media:62 |
| `HTTP-get-albums-id-images-private` | offset 0/limit 10: 200, array. Search cross-module cases separately assert offset -1 or limit 101 returns 400 `VALIDATION_ERROR`. | Media:68; Search:105,111 |
| `HTTP-post-people-private` | 201, numeric ID, submitted name. Extreme height/weight cases: 422 `PERSON_INVALID`, message `Invalid person data`; see limits below. | People:23,104,125 |
| `HTTP-get-people-id-private` | 200, submitted name. | People:37 |
| `HTTP-put-people-id-private` | 200, updated name. Extreme height/weight cases: 422 with the same code/message. | People:44,163,180 |
| `HTTP-put-people-id-roles-private` | 200 only. | People:52 |
| `HTTP-get-people-id-roles-private` | 200, array only. | People:59 |
| `HTTP-post-people-creator-groups-private` | 201, numeric ID, submitted group name. | People:66 |
| `HTTP-put-people-creator-groups-id-members-personid-private` | 200 only. | People:80 |
| `HTTP-get-people-creator-groups-id-members-private` | 200, array and first member's personId equals the created Person ID. | People:85 |
| `HTTP-post-vault-tags-private` | 201, numeric ID, submitted tag name. | Vault:63 |
| `HTTP-put-vault-entries-id-tags-tagid-private` | 200 only. | Vault:77 |
| `HTTP-put-vault-entries-id-favorite-private` | 200 only. | Vault:82 |
| `HTTP-put-vault-entries-id-rating-private` | 200 only. | Vault:87 |
| `HTTP-get-vault-entries-id-metadata-private` | 200, favorite true, rating `S`, first tag name matches the created tag. | Vault:94 |
| `HTTP-delete-vault-entries-id-tags-tagid-private` | 200 only. | Vault:102 |
| `HTTP-delete-vault-entries-id-favorite-private` | 200 only. | Vault:107 |
| `HTTP-delete-vault-entries-id-rating-private` | 200 only. | Vault:112 |
| `HTTP-delete-vault-entries-id-private` | 200 and JsonPath deletedAt is not empty; overlaps prior V1 lifecycle credit. | Vault:117 |
| `HTTP-post-vault-entries-id-restore-private` | 200 and JsonPath deletedAt matches `nullValue()`; overlaps prior V1 credit. | Vault:123 |
| `HTTP-get-search-private` | Authenticated 200, array, default page offset 0/limit 50/boolean hasMore; invalid bounds return 400 `VALIDATION_ERROR`. Anonymous request asserts 401 only. Filter/paging and standalone-binding scopes are separated below. | Search:34–79,180–240,282–303 |
| `HTTP-get-knowledge-vocabulary-due-private` | limit 10: 200, array, present null meta. Limits -1/101: 400 `VALIDATION_ERROR`. | Search:92,98,247 |
| `HTTP-get-reference-countries-private` | 200, array, present null meta; no PUBLIC claim. | Search:259 |

## Additional observations and limits

- **ImportData:** both trailing-object and trailing-garbage parse failures exclude their synthetic sentinel from the returned error string and captured application output. The object case additionally queries zero items for that job, zero Notes and zero Vault entries of type NOTE; the garbage case queries zero job items and zero Notes only. The object case alone checks blocked progression and the later job/items responses. These are early parse rejections, not rollback after a first target write. The separately accepted [rollback](b0-importdata-rollback-evidence-addendum.md) and [execute/cancel](b0-importdata-concurrency-evidence-addendum.md) witnesses keep their own scopes. The successful HTTP import does not independently assert target Note/Vault rows in a fresh transaction.
- **People:** each of `1E+2147483647`, `100E+2147483647`, `1000E+2147483647` is tried for height and weight. POST SQL checks only zero Persons with the respective synthetic height/weight test name, not zero writes to every table. PUT SQL checks only height_cm 170, weight_kg 65 and name `Valid Before Extreme`; it is not a full-row snapshot.
- **Search filtering:** domain-only data has size 3, each child has domain FILM and excludes the seeded Person label. With domain FILM, entryType FILM and the tag filter, offset 0/1 and limit 1 return distinct members of the two expected tagged Film titles, correct domain/entryType fields, and hasMore true/false respectively. No fixed ordering or independent discrimination of the entryType predicate from domain is established. Seeding HTTP calls are setup, not additional endpoint credit here.
- **Search binding:** `searchAdapterBindsRepeatedFilterSetsToGlobalSearchQueryUnchanged` creates a separate standalone MockMvc controller with mocked GlobalSearchOperations and no security chain. It asserts 200 and captures exact query text, FILM/FICTION domain/type sets, tag ID set 101/202, offset 10 and limit 25. It proves adapter binding only, not persisted admission, real search execution or database filtering.
- Arrays may be empty; status-only results do not establish stored relationship/deletion state. Finance amounts/balances/posting, Location category membership/business hours, Media binary storage, complete DTO/envelope keys, headers, broad paging/order, provider or deployment behavior are not implied. The old Vault method does not call GET entry-by-ID or GET trash. No generated OpenAPI assertion is proposed.

## Applicability and historical provenance

The [DP-A independent final review](../2026-11-11-dual-login-profiles-preparation/reviews/2026-11-12-bf26-dp-a-codex-takeover-independent-final-review.md) qualified G5-B at source commit `5c1da4e5404ee3c724e3fc2e03569d4093bcb44d`. The historical shared Web fixture installs Spring Security and obtains real persisted PRIVATE sessions through production bootstrap/login services, except for the explicitly separate standalone Search adapter request. It uses PostgreSQL/pgvector and RAG recovery/reset dependencies; reuse is not a new isolated Non-RAG run or RAG certification.

Of 712 selected manifest entries covering these modules/tests, Knowledge, shared support, main resources and POM, 705 raw hashes match current files. Seven differ: five Vault production files and two Vault test classes. The production differences add trash browsing; existing entry/metadata paths used here are preserved. The old Vault Web method body is unchanged after line-ending normalization, but its current class adds spy fields and tests; this proposal does not certify execution of that expanded context. The changed non-selected VaultEntryIntegrationTest is not a witness. The other six complete selected Web test files match their recorded source hashes.

Finance, ImportData, Location, Media, People, Search and Knowledge production, shared fixtures, resources/POM and common response/error classes are unchanged from DP-A to audit. Security adds eight PUBLIC Fiction/Film/Reference GET matchers; selected PRIVATE requests remain admitted and Reference countries retains PRIVATE access. For Search's Film setup, the creation adapter changed: the current PRIVATE path reads the same CreateFilmRequest through MVC converters and validation, then uses unchanged command mapping, FilmService.create, view and response mapping. This static comparison supports proposed reuse for the specific valid PRIVATE setup; the cohort did not execute the changed adapter and supplies no PUBLIC-creation or public-detail-projection credit. OpenAPI configuration changes provide no extra schema credit. Tracked Backend content at preparation HEAD `efb32c53decb47c0fb881031a61e02b9847a80d2` matches the audit SHA.

Reconstructed gate argv matches receipt hash `5124eaf63e31c81448d6d6668d1e5f062221d63572dbce2e5ae5ee3c6c8dbc0a`. G5-B exited 0 in 38.578 seconds with complete capture and cleanup; each selected XML has only its recorded passing methods and zero failures/errors/skips. These 16 identities belong to the same historical 21-testcase gate as the earlier [Knowledge](b0-knowledge-http-reused-evidence-addendum.md) and [four-class reuse](b0-g5-b-http-reused-evidence-addendum.md); do not add them again to existing totals. OS-level egress enforcement remains `NOT_VERIFIED`.

Artifacts remain local under ignored `logs/bf26-dp-a/codex-takeover-1794408167866608000/`. Common source/evidence-manifest hashes are recorded in the earlier G5-B addendum.

| Artifact | Raw SHA-256 |
| --- | --- |
| `g5-b.json` | `160ae91755c5267d8c43c002aee1c213f4c5f43eb42a7166fe3a506500a4bf0b` |
| `g5-b-reports-v2/TEST-com.vhvkhangg.personalprivatevault.finance.FinanceWebIntegrationTest.xml` | `f9557fd8319d9fe117f07ebb41d54400a1dd7d8b39377562cb70b9ba11fa790b` |
| `g5-b-reports-v2/TEST-com.vhvkhangg.personalprivatevault.importdata.ImportDataWebIntegrationTest.xml` | `0fef4f0c61741b2e7a896e5792265351708a8637aeec6658f5a8fbdab497cc73` |
| `g5-b-reports-v2/TEST-com.vhvkhangg.personalprivatevault.location.LocationWebIntegrationTest.xml` | `e3b8720c4670ab75a7aff592ec2bc8bc9ed019368858f02e81a116141e0cf919` |
| `g5-b-reports-v2/TEST-com.vhvkhangg.personalprivatevault.media.MediaWebIntegrationTest.xml` | `9955b78a6e903c1de90d4c10db23e85d197cc0c6887a592997293c1a6d33d9f0` |
| `g5-b-reports-v2/TEST-com.vhvkhangg.personalprivatevault.people.PeopleWebIntegrationTest.xml` | `866d6edfe477bd6cb2d8a9db4dc83851a093ffd29c49a22899a54511e1b5a2ab` |
| `g5-b-reports-v2/TEST-com.vhvkhangg.personalprivatevault.search.SearchWebIntegrationTest.xml` | `aaf4564e0d4050a15c45dce3535c95f44e6ed0ee5b3d46ee3e9505e374772be3` |
| `g5-b-reports-v2/TEST-com.vhvkhangg.personalprivatevault.vault.VaultWebIntegrationTest.xml` | `538dd1668ff86d8ab4c7e4278b314d4979a0b39bd2d8ce4bde1c976ee6558df5` |

Docs Sync: one proposed addendum; published reports, previous addenda, source/tests, index and `ACTIVE` remain unchanged. Independent evidence/document review precedes publication. No new Maven/test invocation or counter reset; historical correction/exception counters and remediation 0 remain unchanged. Remaining B04/B05, B11 and other G5 gaps retain their dispositions. B0 remains `BLOCKED_B0 / OWNER_DECISION_REQUIRED`, G5 remains unmet, B0-U1 remains `NOT_RUN`, A1 remains deferred, and A2/B1/X remain outside scope.

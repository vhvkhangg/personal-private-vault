# BF26-NR-B0 rev. 1 — G5-B HTTP reused evidence: Reference, Settings, Personal and Journal

**Proposed scoped credit; awaiting independent evidence and document review.** Audit SHA remains `dde8fc86e820b8f9a329a65cfe590191e027ef4c`. Four historical DP-A G5-B testcases support the assertions below for 15 PRIVATE method/path groups in the [published HTTP matrix](b0-http-contract-matrix.csv). This supplements B04/B05 in the [B0 report](b0-audit-report.md) without editing either artifact or marking whole rows complete.

## Witnesses and exact assertions

The locators below use these tracked source files and methods:

- [ReferenceWebIntegrationTest](../../../../backend/src/test/java/com/vhvkhangg/personalprivatevault/reference/ReferenceWebIntegrationTest.java): `referenceEndpointsReturnCatalogs`.
- [SettingsWebIntegrationTest](../../../../backend/src/test/java/com/vhvkhangg/personalprivatevault/settings/SettingsWebIntegrationTest.java): `settingsLifecycle`.
- [PersonalWebIntegrationTest](../../../../backend/src/test/java/com/vhvkhangg/personalprivatevault/personal/PersonalWebIntegrationTest.java): `personalProfileLifecycleOverHttp`.
- [JournalWebIntegrationTest](../../../../backend/src/test/java/com/vhvkhangg/personalprivatevault/journal/JournalWebIntegrationTest.java): `diaryLifecycleOverHttp`.

| Matrix group ID | Observed assertion proposed for reuse | Source locator |
| --- | --- | --- |
| `HTTP-get-reference-platforms-private` | 200; `data` is an array. | Reference:36 |
| `HTTP-get-reference-story-archetypes-private` | 200; `data` is an array. | Reference:41 |
| `HTTP-get-reference-world-settings-private` | 200; `data` is an array. | Reference:46 |
| `HTTP-get-settings-private` | Uninitialized: 404, `SETTINGS_NOT_FOUND`, exact message. After PUT: 200, `defaultCurrencyCode=USD`, `paginationSize=50`, and JsonPath `error.doesNotExist()`; that matcher does not distinguish null from an absent key. | Settings:26,65 |
| `HTTP-put-settings-private` | 200, `defaultCurrencyCode=USD`, `paginationSize=50`. | Settings:56 |
| `HTTP-post-personal-profiles-private` | 201, numeric `data.id`, submitted `data.name`. | Personal:33 |
| `HTTP-get-personal-profiles-id-private` | 200, submitted `data.name`. | Personal:47 |
| `HTTP-put-personal-profiles-id-private` | 200, updated `data.name`. | Personal:64 |
| `HTTP-get-personal-profiles-self-private` | 200, `data.id` equals the created profile ID. | Personal:72 |
| `HTTP-delete-personal-profiles-id-private` | 200 only. | Personal:78 |
| `HTTP-post-personal-profiles-id-restore-private` | 200, `data.id` equals the created profile ID. | Personal:83 |
| `HTTP-post-journal-diary-entries-private` | 201, numeric `data.id`, `entryDate=2026-10-05`. | Journal:28 |
| `HTTP-get-journal-diary-entries-id-private` | 200, `entryDate=2026-10-05`. | Journal:42 |
| `HTTP-put-journal-diary-entries-id-private` | 200 only. | Journal:55 |
| `HTTP-delete-journal-diary-entries-id-private` | 200 only. | Journal:62 |

An array assertion permits an empty array; it does not establish catalog contents, element schemas, ordering or completeness. Status-only assertions do not prove persisted updates or soft-delete flags. Complete DTO/envelope keysets, Markdown/date round trips beyond the printed fields, independent database snapshots, cache/Location headers, paging, invalid/deleted-resource cases and authorization denials are not supplied by this table. Personal and Journal list routes and Journal restore were not called by these witnesses. The Reference method's countries/currencies/languages and anonymous checks are not proposed as additional credit here. Settings' generated OpenAPI assertion is also excluded from this proposal.

## Source applicability and execution

The [DP-A independent final review](../2026-11-11-dual-login-profiles-preparation/reviews/2026-11-12-bf26-dp-a-codex-takeover-independent-final-review.md) qualified the historical G5 executions. DP-A source commit is `5c1da4e5404ee3c724e3fc2e03569d4093bcb44d`. The shared [Web fixture](../../../../backend/src/test/java/com/vhvkhangg/personalprivatevault/support/AbstractWebIntegrationTest.java) installs Spring Security and obtains a persisted PRIVATE session through production bootstrap/login services; these requests do not use synthetic JWT admission. Controllers and owner services/repositories run in the historical shared PostgreSQL/pgvector fixture with RAG recovery/reset dependencies. This is fixture metadata, not a new isolated Non-RAG execution or RAG certification.

Of 175 selected source-manifest entries covering the four modules/tests, shared support, main resources and POM, 174 raw hashes match current files. The exception is `ReferenceWebIntegrationTest`: two later test methods were added. Its executed original method body is unchanged after line-ending normalization, and those added methods are excluded. The other three complete test files match their recorded source hashes. Module production, shared fixtures/resources and POM are unchanged from DP-A to the audit SHA. The shared response/error classes are unchanged. Security adds eight unrelated PUBLIC Reference/Fiction/Film GET matchers; the three PRIVATE Reference lists above and other selected requests retain the PRIVATE fallback. Vault changes add trash browsing and preserve the entry operations used here. `OpenApiConfiguration` changed, so this proposal does not requalify Settings' generated OpenAPI check. Tracked Backend content at preparation HEAD `52b1dcb73fc9e1ed68a36c6a46b5e85e0a22da25` matches the audit SHA.

Reconstructed G5-B argv matches receipt hash `5124eaf63e31c81448d6d6668d1e5f062221d63572dbce2e5ae5ee3c6c8dbc0a`. The historical gate exited 0 in 38.578 seconds with complete capture and cleanup. Each selected XML contains exactly its named testcase with zero failures/errors/skips. These are four existing identities within the 21-testcase gate, not fresh executions or additions to its total. The earlier [Knowledge reuse](b0-knowledge-http-reused-evidence-addendum.md) remains separately scoped. OS-level egress enforcement remains `NOT_VERIFIED`.

Artifacts remain local under ignored `logs/bf26-dp-a/codex-takeover-1794408167866608000/`; XML paths are under `g5-b-reports-v2/`.

| Artifact | Raw SHA-256 |
| --- | --- |
| `g5-b.json` | `160ae91755c5267d8c43c002aee1c213f4c5f43eb42a7166fe3a506500a4bf0b` |
| `g5-b-reports-v2/TEST-com.vhvkhangg.personalprivatevault.reference.ReferenceWebIntegrationTest.xml` | `e2e50a638f7b7c529cce6f30354ae32bb13ac8db4f3cde7dba28ffe23769b281` |
| `g5-b-reports-v2/TEST-com.vhvkhangg.personalprivatevault.settings.SettingsWebIntegrationTest.xml` | `6e1fdbef4806f58f0f54f08b742770df9d7d14bc3e74d8e9c4201b2ca9c25d04` |
| `g5-b-reports-v2/TEST-com.vhvkhangg.personalprivatevault.personal.PersonalWebIntegrationTest.xml` | `de6eb7417c39d84d7fd2bd2334b736132dc1ed29f580d55159c558bce98a2849` |
| `g5-b-reports-v2/TEST-com.vhvkhangg.personalprivatevault.journal.JournalWebIntegrationTest.xml` | `f58eb8b204595706fbb3a1094b0c22fa5ab4c66ee324829b98f9ebbe7f0faf88` |
| `g2-attempt5-source.json` | `d1865ac1cf79a708ee8e33eba31ec44749abe2aac7e0ca0641da0d96826ab011` |
| `evidence-manifest.json` | `cd7061eae3c0e6e1c1de6ddab49f0293433068c1ce2cc02ee2940884340090f4` |

Docs Sync: one proposed addendum; reports, previous addenda, source/tests, index and `ACTIVE` remain unchanged. Independent review must verify this reuse and the document before publication. No new Maven/test invocation is consumed or counter reset; historical correction/exception counters and remediation 0 remain unchanged. Remaining B04/B05, B11 and G5 gaps keep their dispositions. B0 remains `BLOCKED_B0 / OWNER_DECISION_REQUIRED`, G5 remains unmet, B0-U1 remains `NOT_RUN`, A1 remains deferred, and A2/B1/X remain outside scope.

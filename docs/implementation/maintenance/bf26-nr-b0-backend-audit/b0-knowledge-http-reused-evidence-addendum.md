# BF26-NR-B0 rev. 1 — Knowledge HTTP reused-evidence addendum

**Proposed scoped credit; awaiting independent evidence and document review.** Audit SHA remains `dde8fc86e820b8f9a329a65cfe590191e027ef4c`. This record supplements B04/B05 in the [published B0 report](b0-audit-report.md) and [HTTP matrix](b0-http-contract-matrix.csv), without editing either historical artifact.

DP-A G5-B contains a later execution of `KnowledgeWebIntegrationTest#knowledgeLifecycleOverHttp`, not just the older Phase 13 record cited by the B0 matrix. Its passing assertions support the following PRIVATE allowed-dispatch claims. Locators refer to the [tracked Knowledge test](../../../../backend/src/test/java/com/vhvkhangg/personalprivatevault/knowledge/KnowledgeWebIntegrationTest.java).

| Existing matrix group ID | Executed assertion | Request locator |
| --- | --- | --- |
| `HTTP-post-knowledge-notes-private` | POST Notes returns 201, numeric `data.id` and the submitted `data.title`. | Test line 28 |
| `HTTP-get-knowledge-notes-id-private` | GET the created Note by ID returns 200 and its `data.title`. | Test line 42 |
| `HTTP-put-knowledge-notes-id-private` | PUT that Note returns 200 and the updated `data.title`. | Test line 56 |
| `HTTP-post-knowledge-study-private` | POST Study returns 201, numeric `data.id` and the submitted `data.title`. | Test line 73 |
| `HTTP-post-knowledge-information-private` | POST Information returns 201, numeric `data.id` and the submitted `data.title`. | Test line 91 |
| `HTTP-post-knowledge-vocabulary-private` | POST Vocabulary returns 201, numeric `data.id` and the submitted `data.word`. | Test line 109 |

These are partial response assertions, not exact DTO-keyset, complete envelope, Markdown-content, enum-field, cache/Location-header or post-update database-snapshot evidence. The method does not dispatch SRS review despite its display name. No missing/deleted/invalid, relationship-eligibility, PUBLIC/anonymous, or other route assertion is added by this record. In particular, Study/Information GET/PUT remain outside this witness; A1 stays deferred. No matrix row is declared complete solely from the assertions above.

## Applicability and provenance

The test uses [AbstractWebIntegrationTest](../../../../backend/src/test/java/com/vhvkhangg/personalprivatevault/support/AbstractWebIntegrationTest.java) with `webAppContextSetup(...).apply(springSecurity())`. `bearerHeader()` obtains a token through the production bootstrap/login services and a persisted PRIVATE session; it does not synthesize a JWT. The domain services and repositories are real within the historical shared PostgreSQL fixture. That fixture inherits pgvector PostgreSQL and RAG recovery/reset dependencies; these are historical fixture metadata, not evidence of RAG behavior or a new isolated Non-RAG execution. Nothing was launched for this reassessment.

The [DP-A independent final review](../2026-11-11-dual-login-profiles-preparation/reviews/2026-11-12-bf26-dp-a-codex-takeover-independent-final-review.md) qualified the G5 executions and 1,898 final source hashes. DP-A source commit is `5c1da4e5404ee3c724e3fc2e03569d4093bcb44d`. Against the audit SHA, Knowledge production/tests, shared Web/PostgreSQL fixtures, main resources, POM, People, Reference and RAG sources are unchanged. The six Knowledge requests still use the PRIVATE fallback: the security delta adds eight Fiction/Film/Reference GET matchers. Vault changes add trash browsing; the existing entry creation/read/update paths used here are unchanged. Backend tracked content at preparation HEAD `2bc2488f0217029df879e34878482b63d91d5902` also matches the audit SHA.

The historical gate identifies `g2-attempt5-source.json` as its source manifest. Its 200 entries covering Knowledge production/tests, shared support, main resources and POM match current raw hashes. The Knowledge test raw SHA-256 is `22152c5cc40f5098e419210eea16a6a67f2f29c88d0ae08e397dfce32d768dc2`, matching both manifests. Reconstructed gate argv matches receipt hash `5124eaf63e31c81448d6d6668d1e5f062221d63572dbce2e5ae5ee3c6c8dbc0a` and explicitly selects the Knowledge class among the G5-B classes.

G5-B exited 0 in 38.578 seconds with complete capture and cleanup. The selected XML has exactly one passing `knowledgeLifecycleOverHttp` testcase with zero failures, errors or skips. Receipt log sizes match the retained files. This is one existing testcase containing six requests, not six new testcases or a fresh run; do not add it again to historical totals. OS-level egress enforcement remains `NOT_VERIFIED`.

Raw historical artifacts remain local under ignored `logs/bf26-dp-a/codex-takeover-1794408167866608000/`:

| Artifact | Raw SHA-256 |
| --- | --- |
| `g5-b.json` | `160ae91755c5267d8c43c002aee1c213f4c5f43eb42a7166fe3a506500a4bf0b` |
| `g5-b-reports-v2/TEST-com.vhvkhangg.personalprivatevault.knowledge.KnowledgeWebIntegrationTest.xml` | `4abbc1e23d9e1694eca34b4c2820c3253b3bdded3ab04e5f89794094f815306b` |
| `g2-attempt5-source.json` | `d1865ac1cf79a708ee8e33eba31ec44749abe2aac7e0ca0641da0d96826ab011` |
| `evidence-manifest.json` | `cd7061eae3c0e6e1c1de6ddab49f0293433068c1ce2cc02ee2940884340090f4` |

Docs Sync: one proposed addendum; published reports, previous addenda, source/tests, index and `ACTIVE` remain unchanged. Independent review must adjudicate these exact reused assertions and this document before publication. No invocation is consumed and no historical correction/exception counter is reset. Remediation remains 0. Remaining B04/B05, B11 and other G5 gaps keep their dispositions. B0 remains `BLOCKED_B0 / OWNER_DECISION_REQUIRED`, G5 remains unmet, B0-U1 remains `NOT_RUN`, A1 remains deferred, and A2/B1/X remain outside scope.

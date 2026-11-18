# BF26-NR-B0 rev. 1 — PRIVATE Fiction and Film HTTP evidence reuse

Proposed scoped credit for B04/B05 at audit SHA `dde8fc86e820b8f9a329a65cfe590191e027ef4c`, pending independent review. The [G5-A addendum](b0-g5-a-http-reused-evidence-addendum.md) set aside two lifecycle methods because the routes had changed since DP-A. The later RO-B gate `G-ROB-PRIVATE` executed those same methods after the Film create adapter and the profile-specific detail handlers were in place. Use these later executions as the primary witnesses for the assertions below.

In the [published matrix](b0-http-contract-matrix.csv), the following PRIVATE groups map to `fictionLifecycleOverHttp` in [FictionWebIntegrationTest](../../../../backend/src/test/java/com/vhvkhangg/personalprivatevault/fiction/FictionWebIntegrationTest.java) and `filmLifecycleOverHttp` in [FilmWebIntegrationTest](../../../../backend/src/test/java/com/vhvkhangg/personalprivatevault/film/FilmWebIntegrationTest.java). Both inherit the [shared Web fixture](../../../../backend/src/test/java/com/vhvkhangg/personalprivatevault/support/AbstractWebIntegrationTest.java), which installs Spring Security and obtains PRIVATE bearer tokens through persisted login.

| Matrix group ID | Executed assertion proposed for reuse | Source locator |
| --- | --- | --- |
| `HTTP-post-fiction-genres-private` | 201, numeric data.id and submitted genre name. | Fiction:22 |
| `HTTP-get-fiction-genres-by-name-private` | 200 and requested genre name. | Fiction:33 |
| `HTTP-post-fictions-private` | 201, numeric data.id and submitted title. | Fiction:65 |
| `HTTP-get-fictions-id-private` | 200 and the created work's title. | Fiction:79 |
| `HTTP-put-fictions-id-private` | 200 and updated title. | Fiction:97 |
| `HTTP-post-fictions-id-links-private` | 201 and numeric data.id. | Fiction:112 |
| `HTTP-post-film-genres-private` | 201, numeric data.id and submitted genre name. | Film:22 |
| `HTTP-post-films-private` | 201, numeric data.id and submitted title for the fixture's valid JSON request. | Film:48 |
| `HTTP-get-films-id-private` | 200 and the created work's title. | Film:62 |
| `HTTP-put-films-id-private` | 200 and updated title. | Film:80 |
| `HTTP-put-films-id-genres-genreid-private` | 200 only. | Film:88 |
| `HTTP-get-films-id-genres-private` | 200 and data array. The test does not assert membership. | Film:93 |
| `HTTP-get-film-genres-by-name-private` | 200 and requested genre name. | Film:99 |
| `HTTP-post-films-id-credits-private` | 201. The returned ID is used for the subsequent GET. | Film:114 |
| `HTTP-get-films-credits-creditid-private` | 200 and data.id equal to the returned credit ID. | Film:124 |

Person creation supplies setup data in both methods. Credit is limited to the listed status and response fields. The array assertion permits an empty array. No complete DTO or envelope, header, persisted genre/credit snapshot, profile denial, PUBLIC projection, malformed request, or generated OpenAPI credit is proposed from these two methods. The [B0 report](b0-audit-report.md) retains its separate B09 and V1-SEC evidence for the relevant detail and Film creation routes.

## Applicability and provenance

The [RO-B independent final re-review](../2026-11-14-bf26-dp-ro-b-preparation/reviews/2026-11-15-bf26-dp-ro-b-independent-final-re-review-2.md) qualified the producer gate at the accepted RO-B source published as `0f3365de888ab03b0d1de6b32e56a3bb7ad96d5a`. The review records 23 cases for `G-ROB-PRIVATE`. The two lifecycle XMLs and producer receipt match their copies in the reviewer's producer snapshot. Compare gate-and-class identities using `G-ROB-PRIVATE-surefire/`.

At audit SHA, the two test files and FilmCreateRequestReader match the historical input hashes. Of 1,911 recorded RO-B inputs, 1,902 raw hashes match the current worktree. Nine differ across five existing Vault production files and four out-of-batch test files. The Git delta also includes new Vault trash types and Backend guidance. The Vault production delta adds trash page queries, mapping and routing. It leaves the entry creation and lookup methods these lifecycle paths call intact. Fiction, Film (including the PRIVATE request reader), People, Reference, security, shared fixture, resources and POM have no relevant delta from published RO-B to audit SHA. Tracked Backend content at preparation HEAD `2590d5a9533216df1eca9e435f9c0887b265ee44` matches the audit SHA.

The producer receipt records exit 0 in 64.109 seconds with complete capture and cleanup. Reconstructed argv hash `e58c9c834bf7c5256235a2f9b2aeb8043cbedc851339dac7ea53157c068c9739` and log sizes 45,392/0 bytes match the receipt. Each selected XML contains its one expected passing testcase with zero failures/errors/skips. These are two historical identities within the RO-B gate. The older DP-A G5-A instances of the same methods are excluded from this proposal. The earlier 29-case G5-A reuse stays scoped to its six selected classes. OS-level egress remains `NOT_VERIFIED`. The historical fixture used PostgreSQL/pgvector and RAG recovery/reset dependencies.

| Artifact | Raw SHA-256 |
| --- | --- |
| `logs/bf26-dp-ro-b/remediation-round2/G-ROB-PRIVATE.json` | `8f268652ec407b815371f32a11682138874770583156fba68a972437304fe295` |
| `logs/bf26-dp-ro-b/remediation-round2/G-ROB-PRIVATE-surefire/TEST-com.vhvkhangg.personalprivatevault.fiction.FictionWebIntegrationTest.xml` | `dfdfd3a3233a871b7b490b60d539699ce91950e378f0d168eb1af00ff776f646` |
| `logs/bf26-dp-ro-b/remediation-round2/G-ROB-PRIVATE-surefire/TEST-com.vhvkhangg.personalprivatevault.film.FilmWebIntegrationTest.xml` | `0533c07ac0eebeb04b9c6334ea0bf168f239d3ba9af55eae368f2649e600ebb3` |
| `logs/bf26-dp-ro-b/codex-rereview2-20261115-7e2b61/entry-audit.json` | `4c2ddec154ecd77b2db021ff74216697ab5809ea6fa22078f7ec5d7ce14034f4` |
| `logs/bf26-dp-ro-b/codex-rereview2-20261115-7e2b61/verification.json` | `43e827f2a5ecffbec4fe55a43f39ae9ac6db56b7373be6a711f8b03110d77650` |

Docs Sync records one proposed addendum for independent evidence and document review. This preparatory comparison consumes no execution allowance and leaves historical counters intact. B04/B05 and B11 remain open. B0 remains `BLOCKED_B0 / OWNER_DECISION_REQUIRED`, G5 is unmet, B0-U1 is `NOT_RUN` and A1 is deferred. A2/B1/X are outside the batch.

# Proposed Owner amendment — BF26-NR-B0 Media evidence credit

- **Prepared:** 2026-11-17 (UTC+07).
- **Proposed amendment:** `BF26-NR-B0-MEDIA-CREDIT`, revision 1, to [BF26-NR-B0 rev. 1](owner-grant-b0.md).
- **Status:** `PREPARATION_DRAFT / EXECUTION_NOT_AUTHORIZED`. The current Owner decision authorizes preparing this file only.
- **Purpose:** allow exactly one exceptional report correction for the remaining Media portion of `B0-REV-03`. Preserve `BLOCKED_B0 / OWNER_DECISION_REQUIRED` and G5 unmet.

## Reviewed checkpoint and scope

The published B0 grant commit `dde8fc86e820b8f9a329a65cfe590191e027ef4c` remains the **audit SHA**. The audit inventory, source, test applicability and all reported evidence continue to refer to that SHA. The report checkpoint tree is `7070f0f9b51845d2eb29a57add82a5a9615a606d`. It adds exactly the three B0 deliverables against the audit SHA.

Independent B0 reviewer chat `01a1e4cf-ef71-7433-a250-1f65eb830ca1`, turn `01a1e827-ce29-76e3-b447-fdc2845aa177`, returned `CHANGES_REQUESTED / OWNER_DECISION_REQUIRED` on that tree. It closed `B0-REV-01/02` and retained `B0-REV-03` solely for the missed Media evidence credit. The existing B0 report correction budget is exhausted at **1/1 completed**. This amendment proposes one separately recorded exception, starting **0/1** if activated. Finding remediation budget remains **0**.

### Authorized overlay if the amendment becomes effective

The following worktree bytes were observed during preparation and their Git-normalized blobs match the independently reviewed checkpoint tree. Paths are relative to `docs/implementation/maintenance/bf26-nr-b0-backend-audit/`.

| Existing report path | Raw worktree SHA-256 | Git blob at report checkpoint | Proposed permission |
| --- | --- | --- | --- |
| `b0-audit-report.md` | `7422a375728d14154ecf3472b6718ddedd76a4e3f8492766910d4974567cf241` | `509b4e3c5e3e30d847ee0760453a0bb194b2aeb3` | Modify only Media evidence and affected dispositions, plus bounded correction handback provenance. |
| `b0-http-contract-matrix.csv` | `90af0acd21feb18c116f38aa6b2c1637c50d1e373bfa9c8ed1406734e49337c2` | `8b2f2acd59e9eac9edf2fd0d2b472429fe717e25` | Modify evidence credit and its limits on the two Media ON rows only. |
| `b0-file-manifest.csv` | `f8417ff7f5583b61a0ad134facd9997938f0929869860afaae1eb124c84f07f1` | `3a8d9b81b49d7c83ad92612f4b9edc2389dd7531` | Read and verify. Preserve byte-for-byte. |

## Authority and baseline rules

Apply the [audit skill](../../../../.agents/skills/codex-backend-audit/SKILL.md), [Owner publication policy](../../../workflow/current-owner-delivery-policy.md#reviewed-non-rag-publication), [audit resume prompt](../../../workflow/resume-prompts/codex-backend-audit.md) and the parent grant.

1. **Draft review and ratification.** An independent reviewer in a different PPV chat from this amendment drafter checks the exact amendment candidate, prior review, counters, hashes and path boundaries. Record a verdict and the amendment candidate tree SHA in chat. The candidate consists of a sole addition of this amendment above the audit SHA. The worktree may contain the three existing untracked B0 outputs from the table. Verify the exact raw and normalized hashes of all three and leave them unstaged and unchanged. The Owner then separately ratifies the exact amendment revision and reviewed tree in dated PPV chat and explicitly grants the exceptional inspector correction and independent re-review. Any byte change to the approved amendment requires new tree qualification and independent review before publication.
2. **Amendment publication.** Owner publishes by default under separate authority. An express delegation may use the [reviewed publisher](../../../../scripts/publish-non-rag.py) for this one accepted amendment path. At publication entry require local `main`, HEAD and cached `origin/main` at the audit SHA, approved PPV origin, unchanged report overlay, empty index and no other changes. The live remote must be at the audit SHA before publication. The resulting commit has sole parent `dde8fc86e820b8f9a329a65cfe590191e027ef4c`, the independently accepted amendment tree, and exactly one added path, `docs/implementation/maintenance/bf26-nr-b0-backend-audit/owner-amendment-b0-media-credit.md`. Verify its actual commit on live GitHub `main`. The report paths are excluded from this publication. Stop on a mismatched parent, tree, file set or remote.
3. **Inspector resume.** Only the original B0 inspector chat `01a1e752-aa66-7fb0-a80e-fde0fee6c512` resumes after the dated ratification and verified publication. The actual amendment commit is the **authority and checkout checkpoint**, while the audit SHA stays fixed. Verify its sole parent, tree and exact diff against the audit SHA. Confirm local `main`, HEAD and cached `origin/main` at the published amendment commit, the retained live publication verification, unchanged `ACTIVE = NO_ACTIVE_HANDOFF`, an empty staged index, and a worktree whose only overlay is the three listed untracked reports. Recheck raw SHA-256 and normalized blob for each before writing. Reuse the prior entry receipt for the original audit SHA and the new publisher evidence for the amendment. No additional remote query is authorized for this correction. Stop for an unexplained drift, missing authority or competing grant. Do not stash, reset, clean or switch checkouts to obtain eligibility.

## Exceptional correction and independent acceptance

After activation, edit only the two writable reports listed above. Reuse the independent reviewer’s evidence for DP-A G5-C. The cited raw receipt is `logs/bf26-dp-a/codex-takeover-1794408167866608000/g5-c.json` and the cited XML is `logs/bf26-dp-a/codex-takeover-1794408167866608000/g5-c-reports-v2/TEST-com.vhvkhangg.personalprivatevault.media.MediaStorageIntegrationTest.xml`. The correction may read those already cited fixture artifacts and relevant tracked inputs for exact assertions, source applicability and reproducible attribution. Preserve all evidence in place.

| Required correction | Acceptance evidence |
| --- | --- |
| **HTTP matrix, Media ON only** | Credit G5-C for the approved assertions at `GET /api/v1/images/{id}/content` and `POST /api/v1/images/upload` with `vault.media.storage.enabled=true`. Keep the verified PRIVATE fixture success and exact bytes within their corresponding operations. Credit only the failure, lifecycle and cleanup assertions actually demonstrated for each row. Retain missing generated OpenAPI ON and profile denial ON as B04/B05 gaps. Leave both storage OFF rows and the PRIVATE HTTP 409 gaps intact. |
| **B13 fixture disposition** | Record the independent G5-C qualification, including 33 passing testcases, exit 0 and completed capture/cleanup, qualifying the exact scoped binary transfer, failure/cancellation and cleanup witnesses. Compare source, fixtures, POM, config and migrations, and the intervening Security/Vault changes for relevance. Distinguish fixture proof from live storage, broader runtime or Owner data. S2 and G5-C cover the same 33 identities. Count them once and separately from the 112 V1 cases. |
| **B14/B18 Media limits** | B14 credits the observed Media configuration validation, fixture health and stream lifecycle assertions. It retains the other startup/topology/live limitations. B18 credits the observed formatted-log assertion in the direct service fixture. It retains missing serialized-response and other provider/credential channel evidence. Remove any demand to rerun already qualified Media assertions and preserve every unrelated blocker. |

Use only read-only source/receipt/status/tree/diff/hash checks within the parent grant's static limits. No application, Maven, test, Docker, provider, RAG, deployment, source or configuration changes, new receipt, evidence-gap exception or B0-U1 opt-in. Preserve the grant, amendment, all other report content, manifest, index, ACTIVE, history and H1 limitations. B1 and X require separate grants.

The inspector verifies the worktree, unchanged manifest hash, the approved row/section delta and all three resulting report hashes. Report the exact candidate tree, audit SHA, amendment commit and both counters in chat. The same independent B0 evidence reviewer `01a1e4cf-ef71-7433-a250-1f65eb830ca1` re-reviews the exact delta, verifies the resulting candidate tree and returns a verdict in chat. Record the exceptional pair separately once the authorized correction and its independent re-review are complete. Preserve the original **1/1**. If a material gap persists, stop for Owner decision. No additional correction or takeover is implied.

## Report publication boundary

The amendment changes only the prospective **publication parent** for the three reports. A report candidate for review is computed by applying only the three B0 report paths over the verified amendment commit. Its diff against that parent must add exactly those three paths. Compare those same three report blobs with the previous report checkpoint tree `7070f0f9b51845d2eb29a57add82a5a9615a606d` to review the exceptional delta independently of the amendment addition.

The Owner retains report publication. Acceptance of the accuracy of a blocked handback does not meet G5 or itself authorize report publication. If separately authorized after independent acceptance, publish only the exact three reviewed reports with the actual amendment commit as sole parent and verify the accepted candidate tree, changed paths and live GitHub `main`. An intervening commit or changed content requires reconciliation under the controlling grant. The report continues to cite the original audit SHA and original receipt scope.

## Preparation handback

`UPDATED` — this amendment draft only. `ALREADY_CONSISTENT` — audit SHA, report checkpoint blobs and raw hashes, index, and ACTIVE at preparation. `NOT_VERIFIED` — any fresh live ref in this draft-only session. `BLOCKED_OWNER_DECISION` — independent amendment review, exact Owner ratification and verified amendment publication still pending. `DEFERRED_OUT_OF_SCOPE` — report correction and publication, test execution, B1 and X.

Next step: Independent amendment reviewer | Review this one-file draft and confirm the unchanged report overlay, then return verdict and exact amendment tree in chat | BF26-NR-B0-MEDIA-CREDIT proposed rev. 1 | Owner ratification and verified amendment publication before inspector resume | original 1/1 exhausted, proposed exception 0/1, remediation 0 | independent PPV review chat required

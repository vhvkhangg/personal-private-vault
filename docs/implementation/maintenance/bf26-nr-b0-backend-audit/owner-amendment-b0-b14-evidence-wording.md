# Proposed Owner amendment — BF26-NR-B0 B14 evidence wording

- **Prepared:** 2026-11-17 (UTC+07).
- **Proposed amendment:** `BF26-NR-B0-B14-EVIDENCE-WORDING`, revision 1, to [BF26-NR-B0 rev. 1](owner-grant-b0.md) and its [Media credit amendment](owner-amendment-b0-media-credit.md).
- **Pre-activation status:** `PREPARATION_DRAFT / EXECUTION_NOT_AUTHORIZED`. The Owner has authorized preparation of this single draft. The separate ratification and publication gates below determine effectiveness.

## Checkpoint and exact exception

The **audit SHA remains** `dde8fc86e820b8f9a329a65cfe590191e027ef4c`. The current **authority and proposed publication parent** is the published Media credit amendment `abf1932cebe1be70a37038bab694c6d045ae3fc4`. The current report tree `b6e3c6f7dd97019a05883173fdfc291b01241cba` adds exactly the three B0 reports over that authority commit.

Independent B0 reviewer chat `01a1e4cf-ef71-7433-a250-1f65eb830ca1`, turn `01a1e848-9e07-7b41-8ef6-c8593b96a270`, verified the report tree and returned `CHANGES_REQUESTED / OWNER_DECISION_REQUIRED`. B0-REV-01/02 remain `CLOSED`. B0-REV-03 remains open for a single unsupported phrase in the B14 row “Media configuration, fixture health and resource release.” The [baseline Media test](../../../../backend/src/test/java/com/vhvkhangg/personalprivatevault/media/MediaStorageIntegrationTest.java) `enabledStorageSpringConfigurationStartupLifecycleValidation`, lines 1665–1722, exercises six invalid enabled-storage configurations with startup failure assertions in `ApplicationContextRunner`. G5-C supports those six fail-fast configuration checks within the one passing JUnit method. It provides no successful configuration control for that method.

The historical correction **1/1** and the Media credit exception **1/1** are both completed and exhausted. This proposed amendment authorizes **one separately tracked wording correction**, starting **0/1** upon activation. Finding remediation stays **0**. The original B0 inspector chat `01a1e752-aa66-7fb0-a80e-fde0fee6c512` is the sole authorized author of that correction.

| Report in current overlay | Raw SHA-256 | Git blob in report checkpoint |
| --- | --- | --- |
| `b0-audit-report.md` | `644953d1afff175f1a79fa6432c39879e8eb5de6fd59f1df762ba985f045ab9b` | `cba458e35983a1737217441ce1aee583bb79ac04` |
| `b0-http-contract-matrix.csv` | `fe0fc0a6684022c3ad6ed9b8fea66e95dc581cf90d29bca24944fb61905caf5f` | `24beed50ce45700b731d0e4f59272de1bb438ecd` |
| `b0-file-manifest.csv` | `f8417ff7f5583b61a0ad134facd9997938f0929869860afaae1eb124c84f07f1` | `3a8d9b81b49d7c83ad92612f4b9edc2389dd7531` |

**Exact authorized report change if activated.** In `docs/implementation/maintenance/bf26-nr-b0-backend-audit/b0-audit-report.md`, delete the single occurrence of the ASCII byte sequence ` and a valid control` in the B14 sentence for the named test. This consumes the preceding space as part of the deletion. The sentence retains “exercises required properties in `ApplicationContextRunner`.” Preserve credit for the six invalid configurations, every other byte of the report and both CSVs. In-memory calculation against the checkpoint gives expected raw report SHA-256 `d734ba32ce230c2638773f9269dc28b02972209264cf5c365c08aaa03a2268c4` and expected normalized Git blob `10d4553d0e95e5c5088a3d13bdb9447709839a89`. Record current handback and counters in chat. The historical report checkpoint text remains part of the retained report.

## Ratification, publication and resume

Apply the [audit skill](../../../../.agents/skills/codex-backend-audit/SKILL.md), [Owner policy](../../../workflow/current-owner-delivery-policy.md#reviewed-non-rag-publication), and [audit resume prompt](../../../workflow/resume-prompts/codex-backend-audit.md).

1. An independent reviewer in a PPV chat distinct from the drafter and inspector verifies this draft, the exact candidate tree with its single added amendment file, the three unstaged report hashes and blobs, and the limited deletion. Return the verdict and amendment-only tree SHA in chat. Owner then ratifies that exact revision and tree in a dated PPV decision and explicitly authorizes the one correction and independent re-review. A changed draft or parent requires renewed independent qualification before publication.
2. Publish the ratified amendment separately. The sole parent is `abf1932cebe1be70a37038bab694c6d045ae3fc4`. The commit must match the accepted tree and add only `docs/implementation/maintenance/bf26-nr-b0-backend-audit/owner-amendment-b0-b14-evidence-wording.md`. Owner publishes by default. Express delegation may use the [reviewed publisher](../../../../scripts/publish-non-rag.py) on the one exact path. Before publication, verify effective PPV origin, local main/HEAD/cached `origin/main` and live `main` at the proposed parent, empty main index, and exactly the amendment plus the three unchanged untracked reports. After publication, verify the actual commit on live `main`, the sole parent, reviewed tree and exact path. The three reports stay byte-identical and unstaged. A mismatch stops publication or resume as applicable.
3. The original inspector resumes only after verified activation. Confirm local main/HEAD/cached `origin/main` at the new amendment commit, match the recorded live publication verification, check the sole-file parent delta, and verify empty index, `ACTIVE = NO_ACTIVE_HANDOFF` and all three pre-edit fingerprints. The new commit is an authority and checkout checkpoint. Source evidence remains anchored to the original audit SHA. Reuse the prior live verification rather than repeating a remote query. Stop on any unexplained drift or missing authority.
4. Make only the authorized deletion. Verify both expected report hashes, both unchanged CSVs and the exact delta against `b6e3c6f7dd97019a05883173fdfc291b01241cba`. Hand back the three-report candidate calculated over the new amendment commit and the actual audit and authority SHAs. Return to the same independent B0 reviewer for the focused wording re-review. Keep the two exhausted counters and track the new exception separately after correction and re-review. Any further discrepancy returns to the Owner.

Owner retains report publication. If separately authorized after independent acceptance, the report commit has the actual published wording amendment as sole parent and adds exactly the three reviewed report paths. Verify the accepted tree and actual published SHA on GitHub `main`. `BLOCKED_B0 / OWNER_DECISION_REQUIRED`, G5 unmet, B0-U1 `NOT_RUN`, all other evidence limitations and H1 constraints remain in force. This amendment grants no change to any other file, no new evidence execution or waiver, no further correction and no B1/X authority.

## Preparation handback

`UPDATED` — this draft only. `ALREADY_CONSISTENT` — local authority commit, three report fingerprints, index and ACTIVE. `NOT_VERIFIED` — new live ref during this draft-only session. `BLOCKED_OWNER_DECISION` — independent review, ratification and publication pending. `DEFERRED_OUT_OF_SCOPE` — report edits, tests and publication.

Next step: Independent amendment reviewer | Review the one-file candidate and byte-exact change boundary | BF26-NR-B0-B14-EVIDENCE-WORDING proposed rev. 1 | Ratification and verified publication before the original inspector resumes | historical correction 1/1, Media exception 1/1, proposed wording exception 0/1, remediation 0 | separate eligible PPV chat

# Slice 3 — FR18-3 and integration evidence matrix

## RR-01-03 corrective checkpoint - 2026-10-22

The separately Owner-authorized RR-01-03 checkpoint returns **READY_FOR_INDEPENDENT_SLICE_3_RE_REVIEW**. Final Java selections pass 249/0/0/0 on identical source; Python passes 198 tests. This is implementation evidence awaiting independent review.
Its [new handback](slice-3-rr-corrective-handback-2026-10-22.md),
[executed matrix](slice-3-rr-corrective-matrix-2026-10-22.md) and
[machine inventory](slice-3-rr-corrective-evidence-2026-10-22.json) record original
counterexamples, minimal corrections and preserved failures. Earlier submitted
readiness claims are historical; the independent CHANGES_REQUESTED review is
preserved. FR18-3 OPEN; external indexing INACTIVE; no Slice 3 acceptance,
activation, publishing or Slice 4 authorization.


**Current implementation disposition - 2026-10-22: READY_FOR_INDEPENDENT_SLICE_3_RE_REVIEW.**
The mandatory contract below is unchanged. The [final executed matrix](slice-3-final-corrective-matrix-2026-10-22.md)
and [new handback](slice-3-final-corrective-handback-2026-10-22.md) map current proof,
retained failures and measurement limits. Prior BLOCKED/CHANGES_REQUESTED evidence
remains preserved. Codex is the implementer; no finding or FR18-3 is self-closed.

**Status: mandatory future evidence, NOT RUN by preparation.** Authority: D24 and
[handoff](slice-3-verified-indexing-handoff.md). Use sanitized approved `.md` snapshots
for all actual external inputs; DOMAIN tests are compatibility only. Synthetic negatives
supplement real producer results and never substitute for them.

| ID | Setup / fault | Required observable result |
| --- | --- | --- |
| S3-01 retained FR18-3 | Run `RagNativeDocumentBatchIntegrationTest.realNativeEightSuccessTwoFailurePublishesAtomicallyAndReconcilesLostAck` at entry with correct native runtime/model/PG; preserve stdout/stderr and receipt. | Record actual failure/pass and inspect difference from retained D14 failure. No invented root cause if environment blocks reproduction; unresolved reproduction/evidence blocks independent FR18-3 closure. Preserve DOMAIN useful positive path and caller-body rejection. |
| S3-02 transport | Java captures/imports and approves root/children; remove/alter original after seal; use exact R/hash/length. Include Unicode vi/en/Hans/Hant. | Producer receives only prepared immutable bytes and approved children; no original reopen, arbitrary Python-discovered path, fake Vault ID or caller replacement. Wrong format including `.markdown` alias unless separately approved is denied for this `.md` target. |
| S3-03 mixed success | Ten independently approved `.md` roots: eight useful D17 documents and two deterministic malformed/unsupported-in-profile `.md` documents. Use real parser/model/native worker/worker SQL. | Exactly ten terminal file outcomes, 8 new G/selected R promotions, 2 failures with no promotion/new G; `COMPLETED`, report partial success; chunks/vectors read through JPA/pgvector equal actual verified output. One physical C_P includes outcomes/pointers/release. Assert failed replacements retain only independently eligible old G. |
| S3-04 all failed | Real producer returns typed complete failures for all approved `.md` roots. | Zero new G/selected promotions, complete failed outcomes; known finish `ABORTED`, exact union released only with positive stop/fence proof. No zero-publish COMPLETED or worker-asserted NO_CHANGE. |
| S3-05 text/completeness | Capture real producer stage, then alter content, omit/truncate/duplicate/reorder chunks or a root, insert extra root, hide missing material under success/warning; tamper canonical actual-result digest. | Trusted validation rejects inconsistent/incomplete roster/coverage/cardinality/result receipt; no new visible/current G or pointer/outcome/release partial commit. Explicit legitimate per-file extraction failure is distinct from corrupt/missing output (common trust failure). |
| S3-06 locators/compact metadata | Forge line/character ranges, Unicode offset interpretation, source key/version/field, revision, cell/block IDs, child attribution; dangling/negative/fractional/cross-source locator_ref, conflicting defaults/structure definitions, duplicate cells, recursive or oversized references. | Validate against exact original bytes and D17 permitted transformation/source representation. Reject forged metadata and wrong revision/role; table/header/HTML normalization and actual parent/child source mapping accepted only when sound. No locator-shape-only acceptance or fabricated citations. |
| S3-07 closure | Approved nested `.md` children, same bytes under different source identities, missing child grant, wrong child R/digest, cycle, escaped embed, changed edge, sibling child. | Exact independently approved closure bound to job/result/G; wrong/missing/stale child rejects parent. No unauthorized child open/implicit consent or cross-root source reuse. Independent sibling G unaffected by unrelated source changes. |
| S3-08 pipeline/vectors | Alter parser/chunker/model/tokenizer hash/version/fingerprint/provider; wrong dimension/cardinality, strings/bools/null vectors, NaN/Infinity, numeric float overflow. | Reject before publication and prove PG constraints also deny invalid vector forms where representable. Real pinned finite 384-vectors round-trip via production mapping; no fake/synthetic all-zero positive stand-in. Preserve same-model CPU identity and no OCR dispatch. |
| S3-09 consent/CAS | Deterministic barriers: stale observed versions/selected R, revoked approval/reset/epoch, pending swap, snapshot missing/tamper, conflicting update, child mutation before final checks. | Reservation conflicts reject normal mutation as contracted; observable mismatch/revoke/integrity uncertainty denies publication. CAS loser installs no G/pointer; no late consent renewal. After finished G, fresh eligibility controls use, not historical job success. |
| S3-10 SQL provenance | Actual `rag_worker`, `rag_runtime`, PUBLIC connections; absent/unconsumed/wrong job/attempt/type/mixed-v2 grant, direct staging DML, runtime forged receipt/row/result, worker policy/publication/pointer/reservation DML or DDL. | Exact denied operations and SQL errors recorded. Worker only restricted approved staging; runtime cannot forge producer admission. Existing legitimate Java publication and v2 work. No widened grant to make positive tests pass. |
| S3-11 writer/seal | Real separate connections and deterministic barriers for writer holding attempt through staging COMMIT vs Java seal, and Java seal first; attempt expiry/stale process/session. | Either accepted writer completes before sealed validation or late writer fails; no write after verification, no leaked active writer or mixed attempt. Retain E1 semantics and post-wait deadline rejection; no strict-E2 claim. |
| S3-12 atomic rollback | Inject failure after candidate generation/chunks/promotion/outcomes but before physical C_P; verify from independent connection. | Zero replacement G/chunks/promotions/outcomes/release visible; prior selected state preserved. Known abort/cleanup is separately durable only after proof. Instrument physical commits, not TransactionTemplate invocation counts. |
| S3-13 unknown committed | Lose actual commit acknowledgement/connection after DB commit but before Java learns outcome; kill/restart controller where relevant. | Durable reconciliation returns committed outcomes/pointers/G exactly once, no duplicate G or automatic resend/reparse. A successful finalize followed by ordinary repeated call proves replay only and is insufficient. |
| S3-14 unknown uncommitted | Drop connection/crash before commit; separately block reconciliation DB access. | No candidate visible. While unknown remain nonterminal RECOVERY_REQUIRED and retain exclusivity/reservations; after positive process/task/writer proof and durable DB reconciliation, safe abort/release. Never assume abort from exception alone or release while outcome unknown. |
| S3-15 cancellation/resources | Cancel/deadline during snapshot transfer, producer native model work, output validation/staging; bounded size/reference expansion and temp disk failure. | Owned process tree/tasks terminate, writers drained/sealed or recovery required; no publication on common failure; bounded cleanup and private error reporting. Independently observe peak system RAM vs GPU VRAM, CPU/time/temp disk; no telemetry-only finite-resource claim. |
| S3-16 regression/final boundary | S3-VERIFY: focused real worker/PG tests, relevant Slice 1/D17/v2/module/API compatibility and selected-runtime static checks. Single full P18-FINAL deferred until cleaned Slice 4 tree. | Record exact executed/skipped/failed counts, pinned tools/image/model/source hashes. Historical full failure remains failed; no AUTH-CLOCK/D14 waiver or full-suite success inferred from focused pass. Slice 4 cleanup/full lifecycle/comprehensive resources/P18-FINAL remain unexecuted future gates. |

FR18-3 closure requires S3-01 plus S3-03/05–14 real producer/trust/PG evidence and independent
review of all applicable rows. Reproduce retained native failure, link the smallest repair to
specific source lines, compare before/after with identical controlled inputs, and include actual
output hash-to-stage-to-published row comparisons. Record which historical repairs/evidence are
reused. An implementer's green report, matching input hash, self-reported identity, receipt boolean,
or synthetic DTO assertions alone cannot close it. Failures are preserved in unique run directories.

# Slice 3 — Codex corrective implementation handback

**2026-10-21 — BLOCKED. Codex is the implementer, not an independent reviewer.**
Owner authorized one finite S3-F01–S3-F06 corrective checkpoint after the
[independent CHANGES_REQUESTED review](reviews/2026-10-21-slice-3-verified-indexing-independent-final-review.md).
This does not reset Antigravity 2/2, D24's initial allowance or authorize another
automatic checkpoint. The [original Antigravity handback](slice-3-verified-indexing-handback.md),
reviews and failed evidence remain historical and unchanged.

Entry HEAD/main: `ef32d41b33dd00e5fa08cf63977e91b8936ef3d5`, with the actual dirty
candidate preserved. Entry hashes cover 2,477 nonignored files; reviewed source
copies, existing Surefire reports and original implementer/reviewer evidence
are retained under `logs/phase18/s3-corrective-20261021/`. No index staging,
commit, push, tag, PR or external capability activation occurred.

## Corrections and their limits

| Finding | Root cause and implemented correction | Acceptance limitation |
| --- | --- | --- |
| S3-F01 | Input-derived placeholder authenticated no result; runtime could forge stages/consumed grants. V22 removes runtime stage INSERT/UPDATE, retains nonterminal evidence, restricts consumed v3 issuance, and seals an exact ordered roster and canonical actual status/reason/warning/chunk/vector/resource result separately from input/scope. Java checks the protected seal and replays the accepted D17 parser/chunker against its own immutable prepared root/child bytes, comparing exact ordered content and metadata. Fractional references and arbitrary fields do not survive canonical comparison. | Exercised native forge/omission/reorder/locator/revision/vector faults reject. The full mandatory reference/role/concurrency matrix remains incomplete; no independent closure. |
| S3-F02 | External UUID was passed to Vault numeric authority, including an invalid empty Vault selection. External read guards and selected-R checks now use RAG authority. G stores post-promotion selected version/R manifests and a separate original input manifest/result digest. All successful root/used-child R promotions remain in publication C_P. Integrity uncertainty is UNAVAILABLE; revoke/staleness deny. | Immediate fresh SQL qualification is tested. Complete fresh-JVM v3 generation eligibility and prior-generation failure scenarios are not established. |
| S3-F03 | V14 dependency backstop rejected external ancestors; transfer keyed children only by UUID. Forward V22 keeps graph → Vault → external order and retained Vault rules, while supporting external versions/reservations. Java binds approved captured basename/stem/UUID names to each exact child S/R; accepted D17 handles nested heading/block/display aliases. Equal bytes never merge child identity. | Additional folder-qualified/frontmatter-alias target resolution, and the full revoked/stale/changed-child race matrix, are not certified. They remain obligations, not accepted exclusions. |
| S3-F04 | Caller MD label did not prove captured format. Capture now retains basename in the immutable file manifest and independently binds basename SHA-256 plus canonical-path SHA-256 in existing `sources.file_identity` (two fixed-width 64-hex values). Admission compares both, checks `.md`, decodes UTF-8 strictly and never reopens the original. | Fresh valid MD captures work after original deletion. Legacy captures lacking this independent metadata are retained but fail closed; no filename is inferred from bytes and no historical sealed manifest is rewritten. |
| S3-F05 | Revision/length/selection/mode were absent end to end. Additive explicit `snapshot` binding carries RAG_SNAPSHOT/WHOLE_FILE, external UUID, R UUID, captured name/hash/length, expected selected R/version, reset/epoch/policy; each approved child has its own binding. Issued input, protected actual result and G retain matching tuples. Legacy external admission is denied; v2 and DOMAIN v3 digests remain compatible. | Native returned revision/epoch substitution and typed negotiation negatives pass in exercised selections. Full concurrent substitution/restart evidence remains incomplete. |
| S3-F06 | Prior handback overstated DTO/synthetic/replay coverage. Replace fabricated positive batch fixtures with native producer results; record actual native failures and passing selections in a separate auditable matrix and machine inventory. | **UNRESOLVED.** Every PARTIAL row in the corrective matrix remains blocking. No all-rows PASS or FR18-3 closure is asserted. |

## Implementation shape and physical boundaries

No new tables, columns, enum/status, route, dependency, model, job queue or general
parser was added. V22 is forward-only after V21 and repairs only existing RAG
functions, grants and triggers. Historical migrations remain byte-identical.
The exact changed-path/hash inventory is the [machine evidence](slice-3-codex-corrective-evidence-2026-10-21.json).

The source-context validator is a second **sequential, database-credential-free
validation process**, not another producer/job engine. It reuses `NativeWorkerProcess`,
the current attempt supervisor and accepted D17 parser/chunker/model tokenizer.
Its suspended identity is committed once in existing `worker_identity.verification`
before resume. Cancellation/recovery track both processes. Managed input/output
temps use the existing snapshot inventory and deletion retry; uncertain process
termination retains the temp protection until positive proof. Native helper
restart/unknown-outcome proof is explicitly still incomplete.

Producer limit remains 2 GiB/one process; validator is capped at 1 GiB/one process,
32 MiB candidate serialization and finite per-root/global/lease checks. These are
execution bounds, not a new measured maximum-source acceptance claim. No GPU
fallback or comprehensive resource certification follows from them.

After positive producer stop, a short source-first drain transaction obtains the
protected committed result seal. Source replay/model work/hash preparation occurs
outside publication row guards. The final existing transaction again checks
source/attempt/grant/roster, seals staging, installs G/chunks, records every outcome,
promotes only successful approved root/closure R, terminalizes and releases the
exact union in **one physical publication C_P**. Preparatory drain and suspended
validator binding are not counted as publication commits. No automatic resend or
reparse occurs when durable outcomes already prove terminal publication.

## Executed verification and receipts

All long commands use `scripts/run-bounded-command.py`, unique names in
`logs/phase18/s3-corrective-20261021/`, ordinary limits of 60–900 seconds and the
selected Python 3.14.4 `.venv-d17`. Exact argv/cwd/timeouts, stdout/stderr hashes,
source/model hashes and Surefire counts are in the machine inventory; receipt
names below are relative to that directory.

| Receipt / selection | Actual result |
| --- | --- |
| `before-native` | Five original wrong-behaviour observations independently reproduced before production edits. Exit 0 is diagnostic completion, not acceptance. |
| `java-first` | 4 tests, 1 error: obsolete synthetic root-key lookup after correct source UUID attribution. Preserved. |
| `java-corrective-1` | 20 tests, 2 failures: empty Vault selection and test's missing original leading newline. Preserved. |
| `java-corrective-2` / `java-module-1` | 20 and 33 tests respectively, zero failures/errors/skips; intermediate identities, superseded by later affected checks. |
| `java-s3-verify` | **FAILED: 112 tests / 5 failures / 0 errors / 0 skips.** Five snapshot fault hooks still intercepted the old five-argument seal overload. Hooks now target the actual six-argument capture call; their original assertions were preserved. |
| `java-final-affected` | **FAILED: 88 tests / 1 failure / 0 errors / 0 skips.** Numeric overflow already rejected during deserialization, but leaked a lower-level exception type. Production now emits a sanitized admission failure; the rejection assertion was not weakened. |
| `java-s3-verify-final` | **120 tests / 0 failures / 0 errors / 0 skips.** Exact mandatory Java selection. Later filename-binding and unavailable-state refinements require and receive the separate current-tree affected check; this receipt is not relabelled as a later source identity. |
| `java-current-tree` | **FAILED: 140 tests / 1 failure / 0 errors / 0 skips.** All 53 Slice 1 and 37 architecture checks pass. Explicit revoke was still labelled UNAVAILABLE after an earlier integrity incident. Production read-denial priority is corrected; both integrity UNAVAILABLE and actual revoke denial remain asserted. |
| `java-availability-final` | **1 passed**, zero failures/errors/skips: genuine publication, runtime DML denials, exact source/R/version proof, integrity UNAVAILABLE and explicit revoke. |
| `java-current-verify` | **178 tests / 0 failures / 0 errors / 0 skips**, exit 0, **516.210s** watchdog duration. Mandatory S3-VERIFY selection plus architecture, OpenAPI route inventory, RAG web, native process and through-commit fence checks. Its source-hash manifest identifies the exact tested tree. Earlier failed gates remain FAILED history. |
| `java-canonical-read-order` | **3 tests / 0 failures / 0 errors / 0 skips**, fresh affected native generation/child/isolation checks after changing the new external current-read guard from signed Java UUID order to PostgreSQL's canonical bytewise UUID order. No aggregate rerun is implied. |
| `pending-pointer-before` | **FAILED: 1 test / 1 failure / 0 errors / 0 skips.** Administrative corruption substituted the actual pending pointer while leaving approved R/hash/length intact. Finalization returned without rejecting the mismatch. This is deliberately beyond ordinary runtime reservation privileges and proves the separate R-identity condition was missing. |
| `java-revision-binding-final` | **4 tests / 0 failures / 0 errors / 0 skips**, fresh affected native checks after requiring actual pending R to equal approved pending R under the final source guards. Includes the preserved pointer counterexample and generation/child/isolation checks. All 1,661 source-manifest files match handback source. Only `RagSourceAuthority` and its new corrective test differ from the 178-test tree; no aggregate rerun is implied. |
| `python-first` | **FAILED: 114 passed / 10 failed.** Producer-local mocks had no new SQL result-seal boundary. Existing assertions retained; fixtures now mock that boundary explicitly. |
| `python-second` / `python-final` | 124 and **137 passed**, respectively. Includes actual CPU producer and new strict snapshot negotiation cases. |
| `python-compat` / `python-compat-corrected` | Initial exit 4, nonexistent test filename, zero collected; retained. Corrected existing-file selection **41 passed**. |
| `official-parser` / `official-producer` | Each attempts all **1,329** frozen CommonMark/GFM/profile examples: **1,267 supported PASS + 62 required whole-root rejections**, zero unexpected failure. Real CPU producer corpus uses the existing SQL boundary mock with the new seal mock in a copied harness; historical scripts/gold/output files untouched. |
| `ruff-final` / `format-final` / `mypy-final` | PASS, including fresh cache on 26 source files. Initial Ruff/format failures are retained. |
| `dependency-final` / `lock-final` | PASS: 67 installed packages compatible; offline lock checks 73 resolved packages. No install/download/lock regeneration. |
| `boundary-corrective-native` | Native physical commit/Java ACK, deferred rollback, actual uncommitted backend termination/reconciliation and mixed authority observations. See exact observation JSON; no packet-level committed-loss or unavailable-DB certification. |
| `diff-check-handback-final` | `git diff --check`, exit 0. Historical CRLF advisories are retained; no wholesale normalization or staging. |

S3-VERIFY Java selection:

```text
mvn.cmd -ntp -f backend/pom.xml -DargLine=--enable-native-access=ALL-UNNAMED -Dtest=RagNativeDocumentBatchIntegrationTest,RagDocumentBatchIntegrationTest,RagDocumentInputCompatibilityTest,RagWorkerPostgresPrivilegeIntegrationTest,RagCommitAcknowledgementRecoveryIntegrationTest,RagSecondJvmRecoveryIntegrationTest,RagCancellationRecoveryIntegrationTest,RagSlice1SnapshotIntegrationTest,RagSnapshotCorrectiveIntegrationTest,RagSlice3*Test test
```

Python selected tests are the seven original S3 files plus `test_slice3_corrective.py`;
compatibility adds contracts, bound ingress/consumption, chunker and embeddings.
No complete Python suite or full Java clean gate is claimed. Mockito/CDS and
existing compiler warnings remain factual warnings. No skips are converted to PASS.

The selected ONNX runtime exposes CPU/Azure providers and **CUDA is UNAVAILABLE**.
A device-wide VRAM sample is available; it measures other device use as well and
does not establish worker VRAM. Native simple-root worker job committed-memory
peaks are recorded separately from worker-reported OS peak working set and host
headroom in `input-output-hashes.jsonl`; no complete validator/temp-disk profile
is inferred from those values. No synthetic OOM case certifies GPU fallback.

## Preservation, unresolved gate and handoff

The [executed S3-01–16 matrix](slice-3-codex-corrective-matrix-2026-10-21.md) is
controlling for remaining gaps. In particular, full native v3 writer/seal orders,
concurrent child/selection/authority barriers, multi-root rollback retaining prior G,
committed connection-level ACK loss, killed-controller/validator restart with DB
unavailable, and independently measured full integration resources are outstanding.
Folder-qualified/frontmatter-alias resolution is not certified. This is why this
checkpoint returns **BLOCKED**, even where individual fixes have passing evidence.

Accepted D17 parser/chunker/model/tokenizer/gold remain unchanged and their semantic
evidence is reused with current regressions. Worker transport/seal changes and
new validation overhead are not passed using old performance receipts. D17/D20
acceptance, Slice 2/D23 Markdown-only closure and D22's three NOT_RECOVERED originals
are preserved. Phase 17 COMPLETE/FROZEN; Slice 1/D14 CLOSED; FR18-3 OPEN; indexing
INACTIVE; originals DISABLED/DEFERRED; historical full Java gate FAILED (1,433 /
1 failure / 2 errors / 0 skips); P18-FINAL NOT ACCEPTED. Slice 4 and excluded-format
cleanup remain deferred and unauthorized here.

Recommended subsequent independent review scope: inspect the new actual-result
trust boundary and V22 privileges, validator process/cleanup recovery, format-name
binding and legacy denial, selected/input version distinctions, child-target
resolution limits, and every PARTIAL matrix row. This implementation session does
not perform that review, close findings or authorize further implementation.
Stop after this handback; another corrective checkpoint requires explicit Owner routing.

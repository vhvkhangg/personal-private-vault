# Phase 18 — Immutable Snapshot ISAR-1–3 focused independent re-review, 2026-10-18

**Overall contract-review verdict: READY_FOR_OWNER_CONTRACT_DECISION.**

**ISAR-1: RESOLVED. ISAR-2: RESOLVED. ISAR-3: RESOLVED — as amendment-specification findings.** The corrected proposal defines the previously ambiguous permissions, incident/revoke ordering and backup/purge boundaries sufficiently for an explicit owner decision. No blocking contradiction remains within this focused scope. Owner approval of the new exceptions and D1–D11 is still required; none is approved by this review.

This verdict does **not** mean implementation readiness, verified native/storage/transaction safety, Phase 18 acceptance, P18-FINAL acceptance or READY FOR OWNER COMMIT. G1–G4 remain unproved. The effective contracts and disabled capabilities remain unchanged.

## 1. Exact scope, independence and evidence integrity

This is the owner's bounded re-review of the three findings in the [original independent amendment review](2026-10-18-immutable-snapshot-amendment-independent-review.md). The reviewer did not author the amendment or its correction. Applied the repository's Codex independent review/coverage workflow and relevant local-RAG, architecture, source-authority and transaction rules proportionately. Reused the prior review for unchanged sections; did not reopen the full Phase 18/backend audit.

Read the actual [corrected proposal](../proposals/immutable-snapshot-amendment-proposal-2026-10-18.md), original findings and directly affected normative, transition, applicability, decision and acceptance rows. Consulted the relevant [ADR-0020](../../../adr/0020-phase-17-single-job-reservations-e1.md), [ADR-0021](../../../adr/0021-phase-18-bounded-multi-file-verified-indexing.md), [owner decisions](../owner-decisions.md), [AC-01–08](../acceptance-and-evidence.md), frozen [eligibility/consent](../../phase-17/source-eligibility-and-allowlists.md), [reservations/revocation/purge/recovery](../../phase-17/revocation-and-derived-data.md), applicable AGENTS rules and [ACTIVE](../../handoffs/ACTIVE.md). Checked the portability public-schema boundary in the unchanged actual adapter. No native, OCR, build or SQL execution occurred.

Independent preservation results:

| Evidence | Result |
| --- | --- |
| HEAD / branch | `435ffea979681f22d723f551dd8b0cd0b733c8cd` / `main`, unchanged. |
| Original proposal SHA-256 | `4d6d09f0b8d5ce01300c3ae7e353f09d19fff693dc15101f662f979edf2027a7`, independently recorded in the preceding review before correction; matches the author's stated entry hash. |
| Actual corrected proposal SHA-256 | **`a0e9844f8b3144671fa05ec983f11b559de648b7b980bdb1d51eb3d36966a8ff`**, independently recomputed; matches the requested receipt. |
| Original independent review SHA-256 | `ccaaf3a63d63fa988ebf6bf465494baced25d4dde28277da7914a808cb0388bd`, unchanged. |
| Unaffected inherited files | **2,110 preserved**: 2,109 files reproduce the prior aggregate digest, plus the unchanged original review above. No production, test, migration, dependency, canonical contract, ACTIVE or historical-review change was found. |
| Aggregate comparison | `32f4ce1b9a4cc039bbd5486af43ec1e7a74ce1ef9a7993929af53d10cba38890`, matching the previous review's documented UTF-8 sorted path→SHA JSON calculation, excluding the proposal and original amendment review. |
| Pre-re-review inventory | **2,111** tracked/nonignored files and **150** porcelain entries, matching the correction-entry count. In this inherited dirty tree, the corrected proposal is the changed document under review. |
| Structure/references | N1–N10, D0–D11 and IS-01–IS-30 all present; **28 local proposal links resolve**. Status remains **PROPOSED — NOT APPROVED**. |

A complete standalone original proposal file was not found in the available preserved artifacts, and the proposal is untracked, so Git cannot provide its before/after diff. The original hash, immutable review and preserved prior read-only tool excerpt are available. Compared that excerpt directly: **N1, N3, N5, N7, N8, N10, §4 identity/version semantics and §7 multi-file closure remain text-identical after line-ending normalization**. Inspected the corrected material and its cross-references for scope; no unrelated substantive change was identified. This is not a claim of a reconstructed complete original-file diff. No historical file was reconstructed or altered.

The added author ledger explains that the old §1/§21 inventory counts describe the original drafting session, while its correction entry uses 2,111/150. Those labelled historical counts do not falsely describe the current correction as another new proposal. Hash/preservation evidence supports a proposal-only correction. Absence of experiments/publishing is consistent with the retained records and unchanged tree/HEAD; it is not a forensic claim about every unrecorded host action.

## 2. Independent finding dispositions

| Finding | Disposition | Evidence in corrected proposal | Remaining owner decision |
| --- | --- | --- | --- |
| ISAR-1 — High | **RESOLVED** | N2/N6/N9; §8.1 permission classes/state table/per-format routes; §10 I0/I3–I7/G2; §13 named P17 applicability rows; D10; IS-01/04/05/23. | Approve or reject D10's new finite raw-quarantine/trusted-inspection permission and scoped Phase 18 P17-2 exception. No such permission is effective yet. |
| ISAR-2 — High | **RESOLVED** | N4/N6/N9; §§5/6/8.2/10 G4/11/12; §13 explicit reserved-source denial exception; D4/D5; IS-09/10/15/16/28/29. | Approve or reject D5's deny-only incident/unresolved-inspection fence while snapshot sources are reserved, including its physical-C ordering and recovery obligations. |
| ISAR-3 — Medium | **RESOLVED** | N9; §§9.1/9.2/11; §13 export/purge/restore rows; D6/D9; IS-17/28/29. | Approve or reject D9's channel-specific exclusion, qualified purge/restore and loss of complete ordinary snapshot recoverability. |

“RESOLVED” adjudicates the document defects; it does not grant the permissions or close any FR18 implementation finding. Fully specified owner choices are not remaining document defects merely because the owner has not yet chosen.

## 3. ISAR-1 — raw custody versus final field-safe consent

The corrected text now separates **five authorities**: owner raw-custody permission; bounded trusted inspection; final exact field-safe retention consent; fresh reserved one-use worker input; and trusted verified publication. Selecting the product direction or granting one stage does not grant the next.

N2 and §8.1 explicitly identify `RAW_QUARANTINE_CUSTODY` as a **NEW proposed Phase 18 P17-2 applicability exception requiring D10 approval**. Its scope is one individually selected source/intake/root/object, purpose and allowed inspection operations, with authoritative finite expiry and resource budgets. Missing limits deny. Known-prohibited sources remain denied; source-wide restrictions discovered during inspection stop further content processing except safe disposal. Unknown source/restrictive classification allows only the expressly permitted quarantine/inspection needed to resolve it, never eligibility. An owner label/parser guess cannot override a known veto.

The A/B/C/D content classification fixes the previous blanket-veto ambiguity: safely separable, unapproved **nonrestrictive optional fields** may be omitted. Field-local exclusions can coexist with useful approved visible content only when source-wide eligibility and exact separation/mapping are established. Source-wide or inseparable/uncertain restrictions deny. A whole copied Office/PDF container is therefore not automatically an eligible representation.

The state table and I3–I7 consistently distinguish raw quarantine, trusted inspection scratch, field-safe candidate, sealed pending approval, exact approval and indexing eligibility. Raw can coexist with projection only while needed for verification/provenance. Sealing concerns the **field-safe representation and mapping**, not the raw container. Raw/sensitive scratch deletion is initiated when that purpose ends and **verified before indexing eligibility**. Failed deletion blocks eligibility; it neither extends processing rights nor claims cleanup complete. Owner-approval delay is not a raw-retention purpose. Pending field-safe preview has bounded cleanup obligations too.

Readers/writers and failure paths are specified: Java capture and the narrowly reviewed Java-controlled trusted intake adapter may access permitted quarantine in the same active operation; ordinary indexing Python and unauthorized endpoints may not. Inspection cannot grant policy, embed/index/publish, execute document code, access business SQL/network or acquire an autonomous lifecycle. Sensitive raw hashes/paths/values are not unrestricted audit/report data. Expiry/cancel/crash stops new processing, retains positive task/writer/SQL safety obligations, and permits cleanup only; no automatic resumption or TTL release.

Useful-content routes remain specified for Markdown/TXT/Obsidian, DOCX, native/scanned/mixed PDF and XLSX/XLS, preserving excluded-field rules and exact original line/structure/page/box/sheet/A1 mapping. Scanned-page classification/rendering is trusted intake inspection; ordinary indexing OCR remains after admission. No parser/library is certified, and effective filesystem/process isolation remains G3/G4 evidence work. Blanket rejection of every complex format is explicitly insufficient acceptance.

IS-01/04/05/23 now test the actual ambiguity: separate grants/states, allowed raw temporary artifacts and principals, useful positive projections alongside forbidden fields, exact locators, deletion before eligibility, expiry/crash failures and no ordinary-worker raw access. No additional contract correction is required for ISAR-1.

## 4. ISAR-2 — three physical boundaries and recovery

The correction makes **C_D** (deny-only incident/uncertainty), **C_P** (trusted publication) and **C_R** (actual explicit revoke/privacy/reset) different transactions and facts. The added D5 applicability exception is snapshot-only, Java-owned and deny-only; it changes neither Vault behavior nor normal consent/reset/classification/selection/reservation mutation authority. It is explicitly NEW in the ADR-0020/P17/D18-A/AC mapping.

### Discovery scope and the pre-C_D interval

The four scope cases are coherent: candidate-only invalid R2 does not invalidate independently trustworthy R1; ordinary OCR/extraction/resource failure does not manufacture privacy revocation; credible S/R1/field/consent uncertainty fences that authority; implicated children deny their true dependent generations, without a job-wide sibling veto. Unproved scope denies the bounded potentially affected closure rather than certifying it safe or silently revoking unrelated objects.

Inspection capable of adjudicating already-selected authority must **first commit scoped `INSPECTION_UNRESOLVED`** under the same source guards, with a concrete recorded reason. That durable precondition protects the crash interval before final inspection classification. Independent candidate inspection does not automatically impose it on trustworthy R1.

The text correctly does **not** promise instantaneous global denial from volatile discovery. For an unexpected candidate observation implicating old authority, the originating operation cannot continue protected use or return success before durable fencing is known; competing operations are ordered by their actual guards/COMMITs. Failed/unknown recording denies the origin and requires reconciliation of durable unresolved-inspection/operation obligations. A transaction that never committed cannot be described as an already durable C_D. If the pre-inspection/recovery mechanism cannot provide the required proof, G4 explicitly forbids using that inspection path. This is an unproved implementation obligation, not a prose assertion that volatile knowledge survives a crash.

### C_D versus publication and true revoke

| Boundary / order | Contract result |
| --- | --- |
| C_D before C_P | Publisher reloads incident state after waits under the **same source authority guards held through actual physical C_P**; active/changed fence denies publication. A last SELECT without retained guards is explicitly insufficient. |
| C_P before C_D | Publication retains its truthful historical outcome. A competing C_D cannot commit through the held source guards first; when it subsequently commits, affected fresh reads/use deny. No retroactive cancellation or retraction of previously delivered lawful bytes is claimed. |
| Incident recorded while reserved | Only scoped monotonic deny/uncertainty state may be recorded through the proposed audited route. No normal consent/reset mutation, grant, reservation release, backwards wait or Python policy right follows. |
| Busy/failed normal revoke | No requested consent/reset mutation commits. Incident denial remains; the report records BUSY/FAILED, not committed revoke. No automatic retry queue. |
| Stop request or timeout | Attempt-only stop remains a separate transaction before subsequent cancellation source/process/control actions. C_D is the distinct proposed authority-denial route; it does not itself terminate processes or substitute for that cancellation protocol. Failed stop cannot clear incident denial. |
| Worker/task/writer uncertainty | RECOVERY_REQUIRED, exclusion/reservations retained; no release from ACK, lease expiry or assumed termination. |
| Known safe abort/cancel finish | Positive task/process/tree stop and writer drain/seal precede the existing atomic abort/release. Releasing the exact union leaves the incident active and does not authorize old fallback. |
| Later known C_R | Explicit source-first mutation after safe release changes the actual applicable reset/epochs/authority and requires transitive controlled-copy purge. C_D alone was not that mutation. |
| False-positive resolution | After quiescence, explicit owner evidence review and guarded CAS may resolve the incident. Incident history advances; actual revoked/reset authority, required purge or old consent/G cannot be restored by that resolution. Independent currentness predicates still apply. |

This ordering is technically credible as a contract within the existing source-first RAG guard slot. A narrowly audited incident state/route can be distinguished from normal busy-source mutation without requiring a second coordinator. Existing runtime/trigger support is **not** claimed. G4 must prove every supported writer, reader, transfer and dependent finalizer obeys the exception and exact physical ordering, or stop and report the precise conflict. No unavoidable architectural incompatibility has been established by this documentation review.

The incident check supplements, rather than replaces, native/managed-store authority, current root/child consent, actual producer verification, positive worker/task/tree death and staging writer drain/seal. E1 does not waive incident/source safety. Sections 11/12 and the receipt tuple include the current incident fence/disposition.

### Crash and unknown outcomes

Committed incident/unresolved-inspection state is independent of controller memory, job terminal state and later revoke. Restart loads it before affected reads/admission. Unknown C_D/C_P/C_R, admission, abort or process/writer outcomes require actual reconciliation; lost ACK is never rollback, revoke success or permission to release. Pending/failed C_R cannot clear denial. Missing/uncertain incident history or restored old history fails closed; equal bytes or a stale approval cannot reconstruct authorization. No automatic inspection/index resumption, incident clearing, epoch rollback or privileged bypass is authorized.

IS-09/10/15/16/28/29 now distinguish candidate-only failure, pre-inspection uncertainty, both physical-C orders, concurrent affected reads, child/sibling scope, failed stop/revoke, death before formal revoke and known versus unknown outcomes. Their expected results match N4/N6/N9 and §§5/6/8.2/10–12. ISAR-2 is resolved at the specification gate.

## 5. ISAR-3 — channels, controlled purge and restore

Section 9.1 now identifies the actual ownership and inclusion boundary rather than making one generic “no backups” promise:

| Channel | Corrected policy / limitation |
| --- | --- |
| A — existing portable export | Existing `public` allowlist and selected public Markdown/media-reference manifest; no RAG-schema or arbitrary managed-root traversal. No adapter, permission or frozen export contract change. |
| B — application-controlled file backup/export | Proposed exclusion of raw quarantine/scratch, projected candidates and sealed snapshot files/mapping manifests. This is a prospective policy, not a claim that a new backup feature exists or that an owner cannot copy files. |
| C — operator PostgreSQL logical/physical backup | May include RAG authority/provenance/incident rows, staged text, chunks, vectors and generations despite file exclusion. SQL inclusion/configuration/retention is operator-controlled; PG cannot supply separate managed files merely by backing up rows. |
| D — WAL/replicas/remnants | Historical content/authority bytes may remain; active DELETE/unlink is not forensic erasure. No unsupported physical-copy erasure guarantee. |
| E — owner external copies | Outside automatic application knowledge/deletion. Known copies require explicit owner handling; unknown copies remain reported as unknown. |

The artifact matrix separately covers O, raw temporary bytes, field-safe candidates, sealed files/mapping manifests, SQL authority/provenance, content derivatives, minimal control tombstones and sensitive logs/scratch. Original files are never RAG cleanup targets. Safe retained tombstones contain only necessary opaque IDs, monotonic fences/reset/epoch history, dispositions and safe reasons/times; paths, raw hashes, locators, text or denied values are not made safe by naming them tombstones. Necessary sensitive provenance needs its own legitimate scope and later scrubbing.

`PURGE_COMPLETE` is now explicitly scoped to **APPLICATION_CONTROLLED_ACTIVE_COPIES**: targeted active SQL derivatives and managed raw/projection/sealed/temp/orphan files absent, no eligible writers/readers or unauthorized references. Required nonleaking anti-replay/incident history is retained. Failed controlled deletion or unknown writer safety remains PENDING and inaccessible. Historical DB/external copies can remain UNKNOWN alongside controlled completion, but reporting must show that separate fact and cannot claim all-copies erasure. Release can precede verified erasure only under unchanged quiescence rules; it does not authorize content reads.

Sections 9.2/11 deny SQL/chunk/approval-only restoration without verified sealed bytes/mappings, and deny returned files without trustworthy current reset/revocation/incident history. A declared restore suspends affected authority; old backups cannot erase later revocation into permission. No automatic reread of O, adoption, capture, retry or publication repairs missing data. Reconciliation/renewed approval/manual import/reindex is required as applicable. The loss of complete ordinary historical snapshot recoverability is stated explicitly.

N9, D6/D9 and IS-17/28/29 agree with this table. Current portability remains unchanged, and any future snapshot-aware backup/portability feature requires separate owner scope. ISAR-3 is resolved.

## 6. Cross-reference consistency and remaining gates

The corrections consistently reach N2/N4/N6/N9, intake states, currentness, §§8–12, directly affected §13 applicability rows, D4/D5/D6/D9/D10 and the corresponding IS rows. All N1–N10, D0–D11 and IS-01–IS-30 remain present. D0 records the already selected drafting direction only; no D1–D11 decision is marked owner-approved.

The new P17 exceptions are explicitly proposed and limited to RAG snapshot sources: D10's temporary custody/trusted inspection and D5's reserved-source deny-only incident record. Normal Vault classification, consent/reset/version/description rules, relevant busy mutation, source-first order, reservations and physical finish remain frozen. Incident denial, committed revoke, effective SNAPSHOT_CURRENT, historical publication and controlled purge are separate reported states. Original freshness remains UNVERIFIED; the corrections do not revive live R7 or add original monitoring.

No FR18 finding is closed. FR18-3 exact restricted producer/staging/output/field/revision/locator/vector/pipeline binding and real physical publication are unchanged. All formats, four OCR language/script groups, native/scanned/mixed PDFs, tables/merged cells, exact locators, model/offline/native GPU/fallback, separate RAM/VRAM and CPU/wall/disk/expansion/cancellation/restart/reporting, fresh held-out evidence and the single full P18-FINAL gate remain mandatory. No unrelated full verification was repeated.

| Gate | Still NOT_VERIFIED / required before useful implementation acceptance |
| --- | --- |
| G1 | Actual approved root/object/ancestor-to-open binding and coherent protected capture, including same-byte wrong-object denial. Current guard is not certified sufficient. |
| G2 | Trusted inspection principals/scopes/expiry, useful per-format safe projection and exact mapping, forbidden-field exclusion and raw deletion before eligibility. |
| G3 | Effective Windows worker/store/handle isolation, immutable and crash-durable sealing, protection through use/C, truthful cleanup/orphan proof. Same-user ACL/path secrecy is not presumed sufficient. |
| G4 | One-operation custody/reservation/task/recovery reuse and the narrow incident route; source guards retained through C_D/C_P; unresolved-inspection durability, fresh scope checks and safe known/unknown finish without a second engine or fake revoke. |

The prior proportional coverage ledger remains applicable. This re-review updates only permission/privacy, transaction/recovery, affected test-evidence obligations and documentation/governance coverage to **specified/consistent at the contract gate**; corresponding runtime dimensions remain NOT_VERIFIED. It supplies no new whole-repository or runtime PASS.

## 7. Exact next owner decision and STOP

There are **no remaining blocking ISAR-1–3 document defects**. Do not start another author correction loop. The next finite action is for the owner to approve or reject the **exact corrected proposal hash**, N1–N10, §13 scoped applicability changes and D1–D11. In particular the owner must consciously decide:

- **D10:** allow the specified temporary raw-container custody/trusted inspection before exact final field-safe retention consent, with the explicit P17-2 applicability exception and verified raw deletion.
- **D5:** allow the scoped deny-only incident/unresolved-inspection fence while snapshot sources are reserved, distinct C_D/C_P/C_R semantics, conservative affected-source denial and the explicit ADR-0020/P17/D18-A applicability exception.
- **D9/D6:** accept controlled file-export exclusion, qualified purge, possible RAG SQL in operator backups/historical copies and incomplete ordinary snapshot recoverability.
- The other detailed choices remain unapproved too, including independent original/snapshot lifetime, authorized prior-generation fallback, revision-vector consistency and deferred live support.

Suggested **contract-only** approval wording for the owner to grant separately:

> Approve the corrected Immutable Snapshot amendment proposal SHA-256 a0e9844f8b3144671fa05ec983f11b559de648b7b980bdb1d51eb3d36966a8ff, N1–N10, its scoped §13 applicability changes and D1–D11, including D10 raw-quarantine/trusted-inspection permission, D5 reserved-source deny-only incident fencing, and D9's channel-specific backup/purge/restore trade-offs. Preserve frozen Vault behavior and all unaffected Phase 18 obligations. This is contract approval only: G1–G4 remain unproved; canonical synchronization, implementation, experiments, ingestion activation and publishing require separate explicit authorization.

If the owner rejects a detailed policy, identify that exact choice; product-direction selection is not a substitute for approval. Even after contract approval, no canonical edit or implementation begins automatically. A separately authorized prospective synchronization/handoff is the later gate, not work executed by this reviewer.

**Only this focused independent re-review document was created.** The corrected proposal, effective ADRs/ACs, ACTIVE, production, tests, migrations and historical reports remain untouched. No native/OCR probe, build, SQL experiment, P18-FINAL, FR18 closure, ingestion activation, implementation authorization, commit, push, tag or PR occurred. Implementation remains STOPPED, ordinary live-file ingestion disabled and snapshot ingestion inactive. **STOP after delivery.**

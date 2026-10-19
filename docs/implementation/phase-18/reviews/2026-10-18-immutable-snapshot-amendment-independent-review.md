# Phase 18 — Independent Immutable Snapshot amendment review, 2026-10-18

**Overall verdict: CHANGES_REQUESTED.**

The snapshot/manual-update direction is coherent, and the proposal correctly exposes significant implementation gates without claiming old live R7 passed. However, temporary raw-custody permission, restrictive-discovery behavior while reservations are live, and backup-exclusion scope need bounded document corrections before informed approval of D1–D11. These are contract ambiguities, not reproduced runtime vulnerabilities. No architectural incompatibility has been established.

This is **contract review**, not production acceptance. An amendment can become ready for an owner decision while G1–G4 remain unimplemented, provided its permissions, state transitions and STOP conditions are precise. Their lack of implementation is not, by itself, the reason for this verdict. This review approves none of the owner decisions and authorizes no follow-on work.

## 1. Independence, authority and preservation

Reviewed the [proposal](../proposals/immutable-snapshot-amendment-proposal-2026-10-18.md), SHA-256 **`4d6d09f0b8d5ce01300c3ae7e353f09d19fff693dc15101f662f979edf2027a7`**. This reviewer did not author it. Applied the repository's Codex independent final-review/coverage workflow and relevant architecture-change impact, local-RAG, Java/native, persistence/testing and proportionality guidance. The owner's explicit review-only instruction governs: architecture guidance does not authorize canonical edits.

Read/reused unchanged root/backend/test rules, [ACTIVE](../../handoffs/ACTIVE.md), [ADR-0020](../../../adr/0020-phase-17-single-job-reservations-e1.md), [ADR-0021](../../../adr/0021-phase-18-bounded-multi-file-verified-indexing.md), [owner decisions](../owner-decisions.md), [AC-01–08](../acceptance-and-evidence.md), [locator/held-out oracle](../held-out-quality-and-locators.md), [P18-FINAL](../final-verification-gate.md), frozen [P17-1](../../phase-17/integration-ownership-and-permissions.md), [P17-2](../../phase-17/source-eligibility-and-allowlists.md) and [P17-3](../../phase-17/revocation-and-derived-data.md). Reused the [post-takeover](2026-10-18-phase-18-independent-post-takeover-review.md), [checkpoint](2026-10-18-d18-pt-a-independent-checkpoint-review.md), [safety](2026-10-18-r7-producer-publication-independent-safety-review.md) and [design-readiness](2026-10-18-r7-invariant-independent-design-readiness-review.md) reviews. A small Graphify query located relevant source; important claims were checked in canonical files, not accepted from the graph.

HEAD remains **`435ffea979681f22d723f551dd8b0cd0b733c8cd`**, branch `main`. Before adding this review, inventory contained **2,110** tracked/nonignored files: **2,109 inherited plus the proposal**. Porcelain contained **149 entries: 50 tracked modifications and 99 untracked files**, consistent with the reported one-entry growth. This is an inherited dirty Phase 18 tree, not 149 new changes by this proposal.

Independent recomputation found **2,106/2,106** files matching the durable diagnostic entry snapshot, plus both subsequent diagnostic documents matching the prior reviewer audit: **2,108 independently reproducible historical comparisons, zero mismatches**. Production, tests, migrations, dependencies, canonical contracts, ACTIVE and the older historical reviews are among these unchanged files. The remaining inherited design-readiness review was inspected; its current SHA-256 is `fe1ed0c877a02f0f0601aef07203fc01a3abc316eaf4469bec5596d54493c435`. The proposal author retained its own full entry hashes only in session memory, so an independent **2,109/2,109 byte-history attestation cannot be reconstructed** from a durable author manifest. This evidentiary limit is disclosed, not treated as evidence that the file changed. No unexpected inherited change was found.

For this review's preservation boundary, the 2,109 inherited files have aggregate SHA-256 `32f4ce1b9a4cc039bbd5486af43ec1e7a74ce1ef9a7993929af53d10cba38890`, calculated over UTF-8 `json.dumps(path_to_sha256, sort_keys=True, separators=(',', ':'))`, excluding the proposal and this new review. All **26 proposal document links resolve**; sections 1–21, N1–N10, D0–D11 and IS-01–IS-30 exist.

The proposal remains **PROPOSED — NOT APPROVED**, documentation only. Existing ADRs/ACs and frozen Phase 17 requirements remain effective. Verified VECTOR, generation-specific purge, Java DOMAIN input, worker, geometry and chunking repairs are preserved; this review requests no reimplementation. The existing live-file denial and failed late-alias regression remain intact. No new snapshot implementation, migration or activation was found.

## 2. Actionable findings — finite corrections

These amendment-review IDs supplement, and do not replace or close, original FR18 IDs.

### ISAR-1 — High — temporary custody and field-safe approval have conflicting boundaries

**Exact clauses:** N2/N6; §8 “First import,” “Fields” and “Important feasibility boundary”; §10 I0/I3/I4/I6 and G2; §13.1 P17-2 §1/field-exclusion rows; D10; IS-01/04/05/23.

I3 copies and hashes the exact original stream into temporary storage **before** I4 excludes forbidden fields and classifies the captured representation. Section 8 permits a controlled copy/preview but says custody does not authorize unknown/private content retention; it also prohibits hidden-field extraction. For an Office/PDF container with hidden/private/custom fields, the raw temporary copy necessarily contains bytes not permitted in the eventual payload, and inspection must examine enough structure to exclude them. The text does not establish whether those bytes are temporarily permitted custody, already prohibited retention, or permitted only after some missing classification gate. N6's broad before-input eligibility requirement and the “UNCHANGED” P17-2 §1 mapping do not resolve this new preapproval consumption boundary.

There is a second ambiguity: §8 says unknown metadata vetoes approval, while frozen P17-2 §2/§3 explicitly permits omission of unapproved nonrestrictive frontmatter/custom properties/comments with safe separation. Unknown **source classification or restrictive policy proof** must not be confused with an unapproved field that can be safely omitted. Otherwise the proposal can silently turn useful approved-content extraction into blanket container rejection.

**Required correction:** define exactly what custody allows before final consent, including raw temporary bytes, trusted inspection, classification prerequisites, allowed principals, cancellation/cleanup and sensitive provenance hashes. Known denied sources must remain denied. If temporary custody of otherwise unapproved container fields is intended, expose that narrow permission and its P17-2 applicability override for explicit D10 approval; do not call it unchanged field consent. Final sealed storage and worker input remain restricted to the approved representation. Distinguish raw quarantine from field-safe sealed pending approval. State how unknown classification, restrictive flags and safely omitted nonrestrictive fields differ.

Provide a bounded per-format representation/inspection route and future positive/negative evidence for DOCX, native/scanned/mixed PDF, XLSX/XLS and Markdown/Obsidian. The ordinary Python worker is expressly forbidden before final approval; identify the trusted intake-inspection boundary rather than implicitly using that worker to produce its own eligibility proof. No particular library, new helper engine or generic sanitizer is required by this review. If the necessary route cannot be justified, retain G2's STOP; denying every complex format does not satisfy retained format acceptance.

**Regression/evidence addition:** inspect temporary artifacts as well as sealed/input/output artifacts in IS-05; distinguish permitted raw quarantine from a forbidden retained field. Include visible permitted content alongside excluded metadata/hidden content in successful per-format cases, not only whole-file rejection negatives. This is a document correction, not permission to create fixtures or execute it.

### ISAR-2 — High — restrictive-discovery suspension has no defined authority interval

**Exact clauses:** N4/N6; §5 restrictive-intake event versus ordinary-failure/uncertain-authorization events; §6 predicates 3–4; §8 cancel-first rule; D5; IS-09/10/15/16/29.

Section 5 records a deny/suspension **request**, then requires cancellation/quiescence and an explicit restrictive command if reserved. It only excludes fallback after authoritative denial commits. Yet D5 describes avoiding old possibly unsafe content, N6 denies unknown authority, and the neighboring uncertain-authorization event denies affected old generations. The text does not state whether the old selected generation is readable between discovery and successful suspension/revoke, what commits that denial, or what survives controller death before the owner retries. “Request” is not a committed revoke; a busy command cannot be counted as one.

**Required correction:** specify one transition/order table for discovery, durable incident/request, attempt stop, old-read eligibility, publication eligibility, cancellation/abort, source suspension/revoke C, retry and restart. Distinguish a finding confined to rejected R2 from uncertainty intentionally applied to S/R1 or children. State precisely when old fallback stops and what evidence fresh reads consult. If no deny occurs until the later explicit command, disclose that availability/privacy window instead of implying immediate conservative suspension.

**Recommended owner policy:** when the selected policy treats discovery as making S authority uncertain, deny old/new use and fence publication; preserve genuine cancel-first revoke semantics and never report revoke committed before it does. The corrected proposal must explain the durable read-denial/publication bridge while reserved. If it introduces a fail-closed quarantine exception to the existing busy-mutation rule, enumerate that narrow new applicability override and obtain explicit owner approval. Do not smuggle it into “UNCHANGED,” automatically retry a failed revoke, or introduce a general command queue.

**Regression/evidence addition:** extend IS-10/15/29 to discovery during capture/indexing with an existing G1, concurrent old-generation reads and finalization, cancellation failure, and controller death before the explicit restrictive command. Expected results must distinguish suspension, pending request, committed revoke and pending purge. No runtime test is requested now.

### ISAR-3 — Medium — backup exclusion does not identify the protected artifact boundary

**Exact clauses:** §9 backup/erasure paragraph; §11 restore; D9; IS-17/29 and §13.1's unchanged portability boundary.

“Exclude snapshot content from ordinary exports/backups” does not specify whether this means managed files, RAG SQL bodies/chunks/vectors, provenance/approval rows, database backups, or all of those. D9 permits safe normal lifecycle records without defining which records remain safe. The truthful WAL/SSD/owner-backup caveat is valuable but does not define the promised exclusion.

Actual `PortabilitySnapshotAdapter` exports an explicit **public-schema** allowlist; its discovery at lines 242–245 also concerns `public`, not the RAG schema. There is no basis here to claim the current portable export already exports the proposed managed store, or to authorize changing the frozen adapter. Separately, a whole-database dump normally includes non-system schemas; placing bytes outside PostgreSQL does not exclude SQL chunks/vectors or authority records from that backup. [PostgreSQL pg_dump documentation](https://www.postgresql.org/docs/18/app-pgdump.html).

**Required correction:** add a small artifact-by-channel table: existing portable export; managed-root file backup; logical/physical PostgreSQL backup and WAL; owner-created external copies. Define what is excluded, what is only operationally prohibited/uncontrolled, what may remain as nonleaking tombstones, and the restore/suspension consequence of SQL records without managed bytes. Explicitly state that full recoverability of snapshots is unavailable under the proposed exclusion policy. Any actual frozen portability behavior change needs separate named scope; it is not required merely to clarify D9. Preserve truthful controlled-copy PURGE_COMPLETE versus unresolved external-backup copies; promise no forensic erasure or new backup platform.

**Regression/evidence addition:** qualify IS-17's completion assertion and IS-29's restore cases by that artifact table. Test relevant export inventory/missing-store restoration later; do not run a new backup project or invent cryptographic infrastructure.

## 3. Clause-by-clause amendment coverage

Section 13 is substantially complete and candid about replacing original-live freshness. Its claim that the REMOVED set is empty means requirements have named replacements; it does **not** mean there is no reduction of the old product guarantee. D3/D7 and the post-seal original-lifetime change are real owner decisions. Scoped Phase 18 applicability overrides are necessary even if historical Phase 17 text is left frozen.

### ADR-0020

| Decision / effective requirement | Proposed disposition and independent assessment |
| --- | --- |
| 1 — retained Vault authority, one job/worker, grants/sealed staging, existing public edges, no global F/V/DRAIN | Preserved for Foundation/indexing. NEW capture/store responsibilities are RAG-local; G4 must map the shared exclusion, not create another engine. |
| 2 — nonexpiring reservations, relevant busy conflicts, cancel-first, positive stop/drain before release | Preserved for existing mutations. Capture extends enforcement. ISAR-2 must specify whether discovery suspension adds a narrow exception; a failed revoke remains failed. |
| 3 — ordinary Vault edits/version, reset, exact description hash and approval CAS | Frozen Vault behavior preserved. New S/R approvals are distinct; equal-byte new R approval is an explicit stricter snapshot rule, not a rewrite of Vault's same-description rule. |
| 4 — E1 and fresh observed-expiry/default-deferred checks | Preserved. Neither sealing nor snapshots permit an expired new gate; no stricter physical-time promise is invented. |
| 5 — completed CURRENT independent of work TTL, with live eligible versions/closure/selection | Independent lifetime preserved. External source predicate is replaced by snapshot authority. This requires an explicit scoped override; Vault CURRENT is unchanged. |
| 6 — manual only, no automatic resend/retry/reindex, outcome reconciliation and restore suspension | Preserved, extended to capture/store. Same-intent idempotent lookup is not permission to repeat capture/indexing automatically. |
| 7 — publication-or-abort/seal/terminal/release at one physical C; verified purge | Preserved for indexing. Capture seal/known release is a NEW non-publication operation; G4 must explicitly map its success/unknown states without claiming a zero-generation indexing job completed. |
| Practical Local Offline addendum | Preserved; no new WFP, host firewall or elevation gate. |

### All eight ADR-0021 / D18-A clauses

| Clause / effective requirement | Proposed disposition and independent assessment |
| --- | --- |
| 1 — distinct approved roots, exact closures and atomic union before input, one job/worker/no queue | Preserved for indexing. NEW finite custody-set admission precedes capture; no implicit child consumption. G4 implementation remains required. |
| 2 — RAG external authority, exact source-first subsets/order/NOWAIT, short SQL guards, no second coordinator | Preserved ownership/order. S/R counters and scoped consent add detail; capture/discovery changes must be enumerated rather than assumed present. |
| 3 — exact live object/path/root/ancestors/content/metadata through physical C; immutable copies are distinct | Original protection is replaced by approved acquisition→capture/seal plus managed revision protection through C. This is a real amendment. Live R7 is deferred, still disabled and unaccepted. G1 cannot be satisfied by a naive copy. |
| 4 — versioned input/typed content, actual restricted producer, pinned pipeline, attempt→staging C, purge/privacy | Preserved; NEW S/R/representation mapping and receipt tuple. Snapshot hashes do not replace FR18-3. |
| 5 — independent generations/closures, true-child invalidation, previous generation only independently CURRENT | Preserved structurally; CURRENT changes for S. Pending R2 does not alter selected R1. Child selection changes invalidate true parents; unrelated roots stay independent. |
| 6 — one trusted final C, all valid successes or none, outcomes/terminal/exact union release, unknown-C retention | Preserved. Root/child selection promotion and resulting versions are added to that C. Filesystem sealing is explicitly not a distributed PostgreSQL transaction. |
| 7 — v2/API compatibility, module-local additions, append-only migrations, frozen old limits, measured new limits | Preserved. Actual candidate tip V19 must be respected, not rewritten. New source-kind/protocol discrimination is necessary. G3 isolation changes must remain a bounded reviewed native boundary, not implicit permission for a new framework. |
| 8 — fields/privacy/reset/revoke/provenance/manual/no write-back/process/SQL/E1/offline and deferred later scope | Preserved for selected snapshot use. ISAR-1/2 must expose any new temporary-custody or suspension exception instead of labelling the entire privacy boundary unchanged. |

The ADR release-proof row correctly replaces the live receipt only for snapshot acceptance while retaining live R7 as a future capability obligation. It must not relabel the failed native test or diagnostic as passed.

### AC-01–08 individually

| AC | Independent coverage conclusion |
| --- | --- |
| AC-01 — all listed parsers/fields/no document execution/original no-write | Retained. G2 needs useful approved positives with forbidden-field exclusion for complex containers, not blanket failure or silent loss of format support; ISAR-1. |
| AC-02 — native-first OCR, vi/en/zh-Hans/zh-Hant, native/scan/mixed, reading order/tables/boxes, one targeted retry | Retained without a new threshold. Snapshot/projection mapping must preserve exact captured-original coordinates. |
| AC-03 — independent recursive child authority, privacy/path/cycle/provenance and changed-child cases | Retained for snapshots; original-child edits cease to invalidate sealed R, whereas selected child revision/authority changes invalidate true dependents. Explicit vector consistency is a real replacement of live-lifetime proof. |
| AC-04 — one chunker, exact locators/table order, bounded tokens, mandatory absence fails file | Retained; NEW R/mapping identity supplements original coordinates. A projection cannot invent replacement paragraph/page/A1 addresses. |
| AC-05 — one pinned offline model/tokenizer, valid vectors/pgvector, one bounded same-model fallback | Retained, including native GPU/offline evidence; no retrieval benchmark is added. |
| AC-06 — one job/worker, independent outcomes, one actual C, failed child, union/release, rollback/unknown-C | Retained. Snapshot selection/G/versions must agree at C; independently authorized old G may survive an ordinary failed update only. |
| AC-07 — explicit owner source/version/body, Private/NSFW/unknown denial, paths/archives/hidden fields, restricted worker and native proof | Original-to-C lifetime is explicitly replaced/deferred for S. New custody and managed-store authority are NEW requirements. ISAR-1/2 prevent treating these different consumption/privacy intervals as wholly unchanged. |
| AC-08 — finite measured limits, true cancellation/restart/temp purge, reporting, separate RAM/VRAM | Retained and expanded to retained/pending storage, expansion and purge backlog. No numerical cap is approved by this review. |

### Frozen Phase 17 and final gates

| Applicable clause | Independent assessment |
| --- | --- |
| P17-1 §§1–4 ownership/public edges/role isolation/Vault history and source guards | Vault/public ownership preserved; snapshots remain RAG-owned. No generic JDBC/portability exception or worker owner privileges are justified. Live-disabled wording needs prospective snapshot applicability, not historical acceptance edits. |
| P17-2 §1 two-authority proof, fields/CAS/reset/Private precedence | Preserved for authorized snapshot use and all Vault sources. Temporary preapproval custody requires the explicit treatment in ISAR-1; conservative discovery under reservation requires ISAR-2. |
| P17-2 §2 external/embedded-child rows | Live canonical file at every use is replaced for S; selected capture and exact child provenance remain required. All other module/secret exclusions stay frozen. Safe omission of unapproved nonrestrictive fields must remain distinguishable from source-wide veto. |
| P17-2 §3 free-text confirmation/root/scope/vanish/replacement/watcher/ad-hoc rules | New R always needs fresh consent, including identical bytes. Post-seal O vanish/replacement/privacy monitoring semantics change explicitly. Ad-hoc retention and no unauthorized fallback remain. A new equal-byte approval requirement is a strengthening, better described as such than merely a clarification. |
| P17-2 §4 external privacy/hash adversaries | Tests move to captured/selected snapshot and explicit authority predicates; unobserved later O privacy edits are not claimed detected. Domain fixtures unchanged. |
| P17-3 §1 and §1A.1–7 reservations/order/process/E1/staging/finish/recovery | Snapshot IndexCurrent predicate replaces live external state. Ordered short guards, exact union, stop/drain/seal, atomic indexing finish and unknown-C constraints remain. Capture is additional gated behavior. |
| P17-3 §2/§2.1 and artifact purge table | Selected snapshot revision/scope C, rather than O edit, invalidates dependents. Restore/uncertain history, immediate authoritative denial and transitive cleanup remain; ISAR-2/3 must define the newly introduced incident/backup boundaries. |
| P17-3 §3 and §4 R7/other native recovery cases | Snapshot fresh reads and selection scope persist. Live capability remains denied until its own proof. Untouched R1–R6/R8/other accepted evidence is reusable only for unchanged boundaries. |
| ACTIVE and P18-FINAL | Current handoff stays stopped under the live contract. Only a later explicit amendment and separately authorized synchronization can replace its source scope/receipt. Keep all five final commands and retained focused quality/publication/offline/resource/report receipts. The proposed intake-lifetime receipt is additional replacement evidence, not a rename of old PASS or a new full-suite loop. |

A coherent prospective new ADR/addendum is possible after these corrections and owner decisions, with named external-snapshot applicability overrides. Historical Phase 17 acceptance must remain historical. Approval does not itself authorize canonical synchronization or implementation.

## 4. N1–N10 assessment

All dispositions below concern proposed text, not runtime PASS.

| Norm | Assessment |
| --- | --- |
| N1 | Coherent ownership/identity separation. Hash/path equality cannot grant authority or merge sources. |
| N2 | **Correction required, ISAR-1.** Safe acquisition and final consent are mandatory, but raw custody/inspection permission must be explicit. |
| N3 | Coherent immutability/protection/deletion obligation, with G1–G3 unresolved. Original protection ends only after the required provenance/representation seal, not at an incidental completed copy. |
| N4 | Coherent manual updates and final-C selection. Ordinary failure preserves prior selection; restrictive discovery must be resolved by ISAR-2. |
| N5 | Coherent qualified freshness, separate original UNVERIFIED and unchanged Vault CURRENT. No timestamp/hash inference of live freshness. |
| N6 | **Correction required, ISAR-1/2.** Eligibility and cancel-first remain essential; preapproval custody and discovery suspension cannot be left implicit. |
| N7 | Coherent exact closure/vector/union/dependency isolation. A new child may be selected with its successful parent's C without needing its own standalone G; this deserves an explicit positive test. |
| N8 | Coherent real-producer/physical-C/rollback/unknown-C requirement. No snapshot or marker substitute. Publication proof must include planned/resulting version consistency. |
| N9 | Strong controlled-copy purge/recovery principle. **Clarify backup channels, ISAR-3**, and pending preview cleanup without allowing indefinite orphan custody. |
| N10 | Preserves functional/quality/resource/final-gate obligations. Failing a supported file safely is necessary, but cannot count as delivery of the supported positive capability. |

## 5. G1–G4 feasibility and STOP conditions

| Gate | Classification | Independent assessment / required later proof |
| --- | --- | --- |
| G1 — acquisition | **SPECIFICATION INCOMPLETE** | I0 selection authority and I6 indexing approval are distinct. The missing acquisition linearization is the protected binding of the I0 observed object/root/ancestors to the actual acquired stream. Current named checks→toRealPath→target-following open do not establish it. A bounded existing-guard extension is plausible, not proven; no Windows incompatibility follows from old alias failure. Before useful capture, specify and prove the anchor, component identities/reparse rules, relied-upon metadata intervals, wrong-object/same-byte denial and writer/mapping continuity. Shorter original lifetime removes post-seal original proof, not this gap. STOP if the chosen native mechanism cannot establish a mandatory predicate; no path copy/final hash/post-hoc ID comparison substitute. |
| G2 — representation | **REQUIRES EXPLICIT OWNER DECISION** | ISAR-1 prevents approval of the custody detail as written. After correction, per-format field-safe projection/locator mapping remains an implementation gate. DOCX runs/properties/comments, PDF embedded/hidden data and original page coordinates, spreadsheet hidden cells/comments/formula caches/merged cells, Markdown frontmatter/comments and separate Obsidian children all need discriminating cases. Scanned content needs an allowed preview/region/representation route without using forbidden preapproval worker processing. STOP on forbidden retained/input fields or unavailable required locator mapping; do not delete formats from scope. |
| G3 — managed store | **SPECIFICATION INCOMPLETE** | Root identity, opaque names, containment, no-overwrite seal, flush/durability, absent writers/aliases/maps, managed-use protection through C, purge/orphans/restart are specified obligations. Effective Windows principal/token/rights and crash durability are not established. Current same-user worker launch supplies no demonstrated filesystem isolation. Restricting a DSN or withholding a path is insufficient. Future proof must cover original/store reads, creation/write/link/delete/tamper, inherited/reachable handles and root replacement, plus allowed runtime/model/scratch access. STOP if supported isolation cannot meet the promised boundary within separately authorized scope. No new sandbox/account/driver architecture is selected here. |
| G4 — capture fencing | **SPECIFICATION INCOMPLETE** | Reusing the existing single-operation exclusion, source-first reservations, attempt-only stop request and reconciliation is proportionate. Existing orchestration admits DOMAIN indexing, not custody capture. Define durable capture intent/state/ownership, new-S reservation identity, expected release token/set, task/writer proof, known capture finish versus indexing finish, unknown C and restart. Include ISAR-2's incident ordering. No slot gap, TTL release, autonomous resume, child read by discovery, second engine or parallel worker. STOP if reuse needs a different coordinator. |

The actual `NativeWorkerProcess` launches with CreateProcessW and assigns a Job Object; no restricted-token launch was found in that path. Windows access checks depend on process/thread tokens and object permissions. An owner-user-only ACL does not, by itself, distinguish Java from Python running with that same effective account. Microsoft documents restricted tokens as a native capability, but this review selects or certifies no such design. [Access tokens](https://learn.microsoft.com/en-us/windows/win32/secauthz/access-tokens), [CreateProcessW](https://learn.microsoft.com/en-us/windows/win32/api/processthreadsapi/nf-processthreadsapi-createprocessw), [CreateRestrictedToken](https://learn.microsoft.com/en-us/windows/win32/api/securitybaseapi/nf-securitybaseapi-createrestrictedtoken).

Managed-store data protection must continue after original release; original data/namespace protection is only required during the defined acquisition/capture intervals under the proposed model. CreateFileW sharing can supply a data-access boundary but does not prove all attribute/ancestor requirements. [CreateFileW](https://learn.microsoft.com/en-us/windows/win32/api/fileapi/nf-fileapi-createfilew). No native experiment was performed for this review.

## 6. Identity, versions, CURRENT and dependency transitions

The O/S/R/G separation is sound: O is historical intake provenance, S is a server-owned logical snapshot source, R is immutable content/representation/closure identity, and G is the verified derived index. Distinct first imports need not deduplicate; explicit update targets S. Original path replacement requires new intake binding, not automatic S identity reassignment.

Revision ordinal is allocation history and may have gaps; `sourceVersion` is committed authority/selection history and does not advance merely because R2 was captured. Revision consent ID/epoch is distinct from source-wide reset/authorization epoch and field/closure versions. Pending approval must not mutate selected-R1 policy or override a global veto. Successful finalization must compute consistent resulting versions for roots and promoted children in the same C. Later design must distinguish **approval/admission expected version** from **planned post-promotion version** in worker input/receipt, then CAS and verify that result at C; blindly incrementing after validating a receipt for the old version would fail the proposed invariant. This is a future design/test obligation, not evidence of a present snapshot implementation defect.

Equal-byte update still creates new R/ordinal and fresh consent; same-intent idempotency cannot duplicate accepted work. Change→restore, old revision replay, source reset, revoked grant or restored files cannot reselect old G. A same-R manual reindex may create a new G without inventing new external bytes. Failed extraction/OCR/resource/cancel/rollback cannot promote R2/G2. R1/G1 is only usable if independently authorized under all selected snapshot/child/field/integrity/retention predicates. Revocation, privacy uncertainty, missing integrity and history uncertainty defeat fallback; ISAR-2 must define the discovery interval precisely.

`SNAPSHOT_CURRENT` never attests that O is unchanged/existent/nonprivate today. `originalFreshness=UNVERIFIED` applies even immediately after capture. O edit/move/delete/externally set privacy therefore need not revoke S under D3; this is an explicit product trade-off, not continuity of old live semantics. Owner removal/revoke in RAG remains necessary.

An approved immutable revision vector is coherent without simultaneous capture: no false common live timestamp. Each recursive child needs independent S/R/consent/fields and exact captured-parent reference/anchor provenance. Conflicting R values for the same S in one batch deny. Full-union reservation precedes worker input; a generation retains its actual closure, not the job union. Child promotion stales old true parents; root-only refresh does not silently refresh children. A failed parent sharing a successfully promoted child is no longer independently fresh and cannot use ordinary-failure fallback. Unrelated siblings remain unaffected. Root removal need not revoke independent shared children; child revoke cannot be defeated by reference counts.

## 7. Purge, recovery and FR18-3

Capture/seal and PostgreSQL are correctly treated as separate durable steps. Complete protected files/manifests must exist before eligible DB reference; crash between steps creates denied pending/orphan state, not auto-adopted permission. Publication pins managed objects/authority across the actual C. Missing/tampered store denies; no final hash or cached snapshot alone closes a store-to-C race.

Known rollback installs no selection/G. Unknown physical C retains recovery/exclusion/reservations until actual durable selection/generation/outcome/terminal/release state is reconciled; a lost response is not rollback or permission to replay finish. Controller death cannot imply retained native handles or dead writers. Normal restart preserves freshly provable completed snapshot authority; restore/uncertain reset history suspends and requires reconciliation/renewal. Missing managed files must not be repaired by rereading O or silently restoring stale permission.

Purge covers raw permitted custody/temp, safe projections, sealed bytes/manifests, worker scratch/OCR/expansion, staged/published/retired chunks/vectors, actual transitive derivatives and sensitive provenance. Release after genuine quiescence may precede erasure; it does not authorize reads. Failed deletion/open writer/outage remains inaccessible PURGE_PENDING. Completion requires verified controlled-file/orphan and SQL absence plus no possible relevant writers, qualified by ISAR-3's backup channels. Original files are never cleanup targets. No forensic SSD/WAL/all-owner-backup erasure is asserted.

**FR18-3 remains Critical and wholly mandatory.** The actual finalizer's `expectedOutput = digest("DOCUMENT_V3:" + expectedInput)` is not exact output authentication. Snapshot IDs/digests do not establish restricted producer provenance, valid rows, canonical output/text/excerpts, field/source/R/child attribution, exact locators/mapping, vectors/model/pipeline identity, job/grant/attempt fencing, immutable staging, writer drain, stop, trusted finalization or physical C. Preserve v2; negotiate snapshot document protocol explicitly, deny absent/forged/mixed kinds. Exact producer/output proof and managed-lifetime proof are dependencies of real snapshot publication, not displaced by it.

All original **FR18-1–11 remain OPEN**. Credited FR18-4/5 purge/VECTOR and other repairs stand. FR18-1/2/8 require new approved source/custody/closure authority proof; FR18-3 exact producer/publication remains; FR18-6/7/9/10/11 retain offline/GPU/fallback, four-language/native/scanned/mixed/table/merged-cell fidelity, locators, resources/operations/reporting and focused/final evidence residuals. No full Maven or unrelated OCR rerun is warranted for this document review.

## 8. D1–D11 owner decision recommendations

These are recommendations, **not approvals**. Choosing snapshot/manual update has not decided every lifecycle consequence.

| Decision | Meaning / consequence | Disposition and exact recommended option |
| --- | --- | --- |
| D1 | Snapshot validity is distinct from O and Vault CURRENT; honest API basis avoids misleading freshness. | **ACCEPTABLE AS PROPOSED.** Approve RAG_SNAPSHOT + SNAPSHOT_CURRENT with originalFreshness UNVERIFIED; preserve Vault CURRENT. |
| D2 | Only explicit import/update reads O; new R needs fresh exact consent even for equal bytes; same-R reindex is separate. More confirmation than hash reuse, no automatic refresh. | **ACCEPTABLE AS PROPOSED.** Approve manual-only intake/update and same-R reindex with stale-intent CAS, no background copy/promotion/retry. Require the version/receipt consistency proof above later. |
| D3 | O edit/move/delete/replacement, including unobserved later privacy changes, does not automatically remove retained S. Owner must revoke/remove in RAG. | **NEEDS SEPARATE OWNER CHOICE.** Recommend explicitly approving independent snapshot retention and no O monitoring; acknowledge deletion/privacy of O is not RAG deletion/privacy. |
| D4 | An ordinary failed/cancelled update leaves authorized selected R1/G1 available, clearly reported as old selection. | **ACCEPTABLE AS PROPOSED**, subject to corrected D5. Approve fallback only on the full fresh snapshot authority/child/integrity predicate; never report R1 as updated content. |
| D5 | Discovery may affect old S authority; cancel-first revoke cannot be represented as already committed while busy. | **NEEDS CLARIFICATION.** Recommend conservative fail-closed uncertainty for implicated S/children plus publication stop, with a precisely documented durable denial bridge and explicit successful revoke after quiescence. Owner must approve any new narrow reserved-work suspension exception; ISAR-2. |
| D6 | No historical-content archive/dedup/automatic rollback; keep selected/actually needed authorized revisions, clean failed/unneeded data; revoke overrides references. | **ACCEPTABLE AS PROPOSED.** Approve minimal authorized retention and verified controlled-copy purge; numerical capacity/cleanup limits remain evidence-gated later. |
| D7 | Separately captured child/root revisions form one approved vector; no simultaneous live-filesystem assertion. Shared child promotion stales true parents. | **NEEDS SEPARATE OWNER CHOICE.** Recommend explicit revision-vector consistency and independently approved recursive children; approve the absence of simultaneous-original consistency. |
| D8 | Live-original support is deferred, disabled and separately gated, not declared repaired or permanently retired. | **ACCEPTABLE AS PROPOSED.** Defer live capability, preserve R7 failures/history and require a separate future owner scope and independent live acceptance. |
| D9 | Backup exclusion limits snapshot recoverability; unmanaged backups and WAL cannot be certified erased. | **NEEDS CLARIFICATION.** Recommend managed snapshot exclusion from ordinary portable/file export, explicitly qualified SQL/backup treatment and suspended restore without bytes; approve the corrected artifact/channel table, not an undefined all-backups exclusion. |
| D10 | Temporary custody differs from final exact retained/worker consent, but current raw-byte permission is ambiguous. | **NEEDS CLARIFICATION.** Recommend an explicit minimal, bounded raw-quarantine custody permission only for individually selected allowed sources, distinct from final field-safe storage/input approval, with known-denied-source exclusion and a named scoped P17 override if needed. If the owner forbids such raw custody, require a different viable capture representation before approval; ISAR-1. |
| D11 | Capture uses the same single operation exclusion/reservations/cancel/reconcile, with no second engine or new indexing status. | **ACCEPTABLE AS PROPOSED as a gated requirement.** Approve reuse as the constraint; require G4's precise capture-state/release/unknown-outcome mapping before implementation use. STOP rather than invent a coordinator if reuse fails. |

## 9. IS-01–IS-30 acceptance-plan assessment

**PLAN** below means a discriminating proposed positive/negative and appropriate evidence source exist, not runtime PASS. **CORRECT** requires the indicated document fix. **EXTEND** identifies a narrow additional assertion in the existing criterion, not a new suite or quota. All runtime evidence is pending.

| Criterion | Independent verdict / required evidence or correction |
| --- | --- |
| IS-01 | **CORRECT** with ISAR-1: separate selected-source custody admission from final approval. Retain forged/wrong-root/same-byte negatives and real Java/native evidence. |
| IS-02 | **PLAN**: deterministic root/ancestor/reparse/open orders, writer/map/query/unsupported cases and valid released controls. Existing guard diagnostic is not this proof. |
| IS-03 | **PLAN**: coherent protected stream vs torn/ABA capture; actual stream/store/input hashes and barriers, not polling or a universal inference from samples. |
| IS-04 | **CORRECT** raw versus field-safe pending states under ISAR-1. Preserve durability/disk-full/cancel/crash/orphan negatives and actual filesystem/PG observations. |
| IS-05 | **CORRECT** with ISAR-1: temporary bytes/inspection as well as eligible fields; useful per-format permitted positives with omitted forbidden fields and exact mapping. |
| IS-06 | **PLAN**: real effective worker filesystem/handle permissions plus SQL negatives. Add an allowed model/runtime/scratch positive; path secrecy/DSN is no substitute. |
| IS-07 | **PLAN**: O changes after seal leave authorized R unchanged; original edits must actually execute, original freshness stays UNVERIFIED, no automatic sourceVersion change/import. |
| IS-08 | **EXTEND**: exact expected versus resulting sourceVersion/consent/closure values before/after C, stale pending grant and identical-byte new R; preserve update/CAS/idempotency negatives. |
| IS-09 | **PLAN**, dependent on D5 correction: real ordinary failures publish/promote nothing and leave only independently authorized old selection usable. |
| IS-10 | **CORRECT** with ISAR-2: discovery-before-revoke interval and durable suspension/read-denial behavior, not only already committed revoke/reset. |
| IS-11 | **PLAN**: real single operation exclusion/CAS/task ownership, competing/late updates and live old capture recovery; no mock second-slot proof. |
| IS-12 | **EXTEND**: positive first parent publication with newly approved child S/R, child selection at the same C without standalone child G; preserve exact recursive negatives/provenance. |
| IS-13 | **PLAN**: different capture intervals plus exact approved vector/union, conflicting shared-S revisions deny. No invented simultaneous timestamp. |
| IS-14 | **PLAN**: child promotion/revoke, true parents versus unrelated siblings, root removal and repeated reconcile with actual content/file checks. Include failed parent whose shared child selection changed. |
| IS-15 | **CORRECT** with ISAR-2: distinguish unsuccessful busy revoke, pending incident, stop fencing, safe abort/release and later committed denial in both orders. |
| IS-16 | **PLAN**: actual post-revoke-C denial and transitive purge, with source-first physical-C ordering and restricted replay negatives. |
| IS-17 | **CORRECT** with ISAR-3: controlled SQL/filesystem completion versus unresolved backup copies. Keep deletion/orphan/writer/outage negatives and original no-delete assertion. |
| IS-18 | **PLAN**: exact actual producer/input/output/rows/R/fields/locators/vectors/pipeline binding and forged/runtime/mixed-protocol negatives; FR18-3. |
| IS-19 | **PLAN**: real role/connection staging-before-seal and seal-before-writer orders through physical staging C; no legacy bypass. |
| IS-20 | **PLAN**: actual native worker→restricted SQL→Java final C→independent readback; 8-success/2-independent-fail, all fail/cancel/common failure, exact selection/outcome/union release. |
| IS-21 | **PLAN**: known rollback and genuinely uncertain real COMMIT, independent outcome readback and no replay/release-only shortcut. |
| IS-22 | **PLAN**: managed-root/object/manifest tamper or loss across use/C, including mutate-after-hash; pinning and both mutation orders required. |
| IS-23 | **CORRECT** per-format useful positive route under ISAR-1; retain all formats/no-write and malformed/archive/execution/path negatives. Unsupported split failure alone is insufficient capability evidence. |
| IS-24 | **PLAN**: fresh human-labelled grouped held-out vi/en/zh-Hans/zh-Hant, native/scan/mixed, tables/merged cells/order/boxes, material-error rejection and one targeted retry. |
| IS-25 | **PLAN**: captured-original→R→published excerpt/field/child/locator mapping, exact spans/paragraph addresses/page boxes/sheet+A1; no fabricated mandatory locator. |
| IS-26 | **PLAN**: real pinned offline model/provider/native GPU and one same-model bounded fallback; no download/egress/wrong dimension/nonfinite/mixing. |
| IS-27 | **PLAN**: representative intake/retention/replacement plus pipeline RAM/owned tree/host and attributable VRAM separately, CPU/wall/temp/expansion/disk/cancel/restart; empirically approved finite limits later. |
| IS-28 | **PLAN**: selected/pending/basis/UNVERIFIED original freshness, safe error/report/purge state and measurements. Include corrected D5 intermediate states. |
| IS-29 | **CORRECT** with ISAR-2/3: crash before restrictive-command C, durable incident recovery, backup channel/missing-store restore; no old authority adoption. |
| IS-30 | **PLAN**: one complete five-command P18-FINAL plus independently accepted amended focused receipts; failed/unrun/required skipped evidence blocks acceptance. |

No additional full-suite cycles, arbitrary test corpus size, quota, CER threshold or benchmark p95 are requested. Freeze quality configuration and fresh held-out source groups before acceptance; exposed diagnostic fixture families remain development/regression evidence. Safe field projections add mapping cases to the same oracle, not a parallel quality platform.

## 10. Evidence reuse, remaining blockers and proportionate coverage

Valid unchanged Phase 17 owner guards/SQL/process/E1/recovery/v2/module checks and prior independently credited VECTOR/purge/chunker/geometry repairs may be reused by immutable path/hash. The 1,321-Java/72-Python results remain historical evidence for the reviewed unchanged behavior, not acceptance of new snapshots. The native count-layout, initial-link denial, tested cleanup and bounded alias/writer/mapping observations remain useful prerequisites. They do not establish G1 or store isolation. Old disabled-path smoke, marker-C, synthetic/runtime-forged staging and failed OCR diagnostics cannot certify the new path. Live-through-index-C proof becomes deferred only after an explicit effective amendment; old failed tests/evidence remain failed historical records.

New acquisition/custody/store/fencing/representation/selection/publication/purge/recovery receipts are mandatory for changed boundaries. Exact producer verification precedes trustworthy publication. All formats, four OCR language/script groups, native/scanned/mixed PDFs, scanned tables/merged cells, chunking/locators, model/offline/native GPU/fallback, separate RAM/VRAM, CPU/wall/disk/expansion/cancellation/restart/reporting, fresh held-out evidence and one final P18-FINAL remain acceptance blockers. These are not waived by contract approval.

Shared review coverage is scoped to this amendment: dimensions **1–3, 10, 12–16, 21–22, 28** have the findings/gates above; **4–5, 7–9, 29** pass preservation/proportional ownership/hygiene review for this documentation delta; **6/11** preserve shared ApiResponse/v2 compatibility with new snapshot discrimination still unimplemented; **17–18, 23, 25–27** have documented DB/storage/resource/tooling obligations, with runtime proof NOT_VERIFIED; **19–20/24** retain quality/oracle requirements with future evidence, not executed passes. No unrelated package refactor, authentication change, broad backend audit or warning-free assertion is made.

## 11. Exact finite next owner action / STOP

**Do not approve the amendment or D1–D11 wholesale yet.** Recommend one bounded document correction addressing **ISAR-1, ISAR-2 and ISAR-3**, plus their existing matrix/decision/acceptance rows. Keep the selected snapshot/manual-update direction and all unaffected obligations. No additional architecture candidate or experiment is needed to resolve these permission/state/reporting questions.

Suggested authorization for the owner to grant separately:

> Authorize one documentation-only correction of the Immutable Snapshot proposal for ISAR-1 temporary custody/field-safe approval and its scoped Phase 17 applicability, ISAR-2 restrictive-discovery denial/stop/revoke ordering, and ISAR-3 backup/export/purge/restore boundaries. Update only their directly affected normative, matrix, D1–D11 and IS-01–30 rows; preserve the snapshot/manual-update direction, all unaffected requirements and the unapproved status. No canonical activation, implementation, tests/experiments, finding closure, commit or push. Return the corrected proposal for focused independent review of these corrections, then STOP.

The owner must subsequently approve/reject the exact corrected normative amendment and D1–D11, including the named external-snapshot P17 applicability overrides and deferred live feature. That is separate from canonical synchronization, G1–G4 design/implementation authorization, FR18-3/publication work and production acceptance. No automatic continuation follows this report.

**Only this independent review document was created.** The proposal, canonical contracts, production, tests, migrations, prior reviews and retained failed regression were not changed. No native/OCR experiment, test suite, capture, snapshot activation, P/FR18-3, publication, commit, push, tag or PR occurred. Implementation remains STOPPED; ordinary live-file ingestion remains disabled and snapshot ingestion remains inactive. Phase 18 is not READY FOR OWNER COMMIT; P18-FINAL is not accepted.

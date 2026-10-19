# Phase 18 — Independent R7-A → P → R7-B native safety review, 2026-10-18

**Verdict: CHANGES_REQUESTED. R7-A's focused native suite FAIL is confirmed; full R7 safety remains NOT_VERIFIED. P and R7-B remain NOT_VERIFIED and implementation remains STOPPED.**

The initial hard-link-count/denial-cleanup correction is supported. Late alias creation/removal disproves the stronger proposition that this opening mode keeps the link count equal to one throughout its lifetime. It does **not** reproduce mutation of the protected file's content or replacement of its approved pathname. Neither a content bypass nor Windows R7 infeasibility has been established. Conversely, ordinary file-sharing protection alone does not establish the full namespace/metadata/authorization invariant.

**Recommended direction:** one separately owner-authorized, diagnostic-only R7 invariant proof using the existing guard, before choosing further production changes. There is a technically credible in-contract path to investigate; there is not yet a demonstrated complete correction or a demonstrated need for an ADR amendment. No automatic P/R7-B continuation follows this review. No P18-FINAL acceptance or READY FOR OWNER COMMIT.

## Scope and evidence integrity

This is the owner's explicitly requested independent review-only scope, using the existing Codex final-review/coverage workflow and applicable Java, backend-testing, local-RAG, architecture and bounded-command guidance. The implementation was authored in a separate session; this reviewer authored none of its production/test changes. Read the [slice record](../r7-producer-publication-slice-2026-10-18.md), [evidence JSON](../r7-producer-publication-evidence-2026-10-18.json), [independent checkpoint review](2026-10-18-d18-pt-a-independent-checkpoint-review.md), [D18-PT-A record](../d18-pt-a-feasibility-checkpoint-2026-10-18.md), [post-takeover review](2026-10-18-phase-18-independent-post-takeover-review.md), owner decisions, AC-01–08, ADR-0020/0021, the active handoff, actual changed/untracked files, archived native reports and relevant Microsoft primary documentation.

HEAD is `435ffea979681f22d723f551dd8b0cd0b733c8cd`, branch `main`. Independently rehashed all **2,102** inherited snapshot files: exactly the native guard and ACTIVE differ; the other **2,100** match. This preserves the prior VECTOR/JPA, purge, chunking, worker, parser/OCR, dependency/migration and historical-review bytes. ADR-0020, ADR-0021 and AC-01–08 match the entry snapshot. The new Java test and two slice records are the expected untracked additions. No unrelated inherited change was found.

All **27** recorded receipt/artifact hash references match. These include some repeated paths, not 27 unique files. Evidence JSON SHA-256: `7c7ecc9ab521325462997902ac9cce56ae0397d87ba08f4dd7a9068f539e6b5e`. Guard SHA-256: `8746b1dc01ed00e03b50c324bfb8ae3bbd9a75f59e3b1de9d6d894399aec6316`; new test SHA-256: `697b213b4dd9a43355f7b9f5abf20a1333128bb40220420669609c7dfd268ab8`.

Archived Surefire XML independently contains **17 tests, 1 failure, 0 errors/skips**: 16 passed. The one failure is exactly `RagWindowsNativePrerequisiteTest.attemptsAliasCreationAndRemovalDuringProtectedLifetime`. Raw console, XML and watchdog agree: **exit 1**, **32.862 s**, normal profile/300 s cap, no timeout. The eight existing native tests passed. Local Win64-header probe source/output and zero-exit receipts agree on structure size 52, alignment 4, link-count offset 40 and file-index offsets 44/48. No full Java suite or OCR tests were rerun by this review.

Reviewer hash/XML audit is retained at `logs/phase18-r7-independent-safety-review/integrity-audit.json`, SHA-256 `325c5234e57b905875d4caab1fe55151aeb1d7d0fa272679234aa3aba33448c2`. No native mutation experiment was launched. Existing native results were inspected, not relabelled as reviewer reruns.

## Implementation assessment

**Verified repair:** `WindowsNativeFileGuard.java:40–54,275–290` uses an explicit DWORD/FILETIME layout with the correct Win64 alignment, zero-initialized information, and unsigned link-count conversion. It rejects every count except one. Native single-link/two-link controls corroborate the ABI and specific denial; injected zero, unsupported and unsigned-max counts cannot accidentally pass.

**Verified tested cleanup:** valid handle acquisition is recorded before querying. Query failure, invalid/unavailable count and exceptional query take the common denial path at lines 343–359, which attempts native `CloseHandle` before arena closure. Tests use real acquisition/close with only the information-query MethodHandle replaced. They verify a dead arena, invalidity of the acquired handle to a real query, and immediate write/delete after denial. Those fault-injection cases pass; they are not evidence of an actual OS information-query outage. The common path also improves coverage of read/hash failures by removing scattered cleanup branches.

**Limit:** if native `CloseHandle` itself fails, the code records a suppressed cleanup failure; arena closure does not itself close a Win32 HANDLE. No unconditional resource-release claim is justified for that untested OS-failure case. Successful-handle close behavior is inherited. No new tested sharing, writer, mapping, size, identity or reparse guarantee was weakened by the correction. Existing junction-test success remains conditional on junction creation succeeding, so zero reported skips is not proof that every junction operation executed.

**Exact failed regression:** the new test creates and deletes the alias *inside the same protected-handle scope*, then fails its retained `created.isFalse()` assertion. It exercises overlapping lifetimes, even though the operations are sequential on one test thread; it is not an independent-process adversarial race or a final-C interleaving test. Positive controls establish that the operations are available after release.

Two diagnostic limitations must remain distinct from the failure:

- `created=true removed=true` and the executed statements establish success. `removeDenial="not attempted: creation denied"` is an unchanged initial placeholder on that branch, a **Low diagnostic defect**, not evidence of denial or a passing test.
- The alias-write assertion executes only if removal fails. Here removal succeeded, so **no alias write was attempted**. `held.getContentAsUtf8()` returns the snapshot byte array captured at acquisition; its equality assertion is **not a fresh read/hash of the live file** after alias operations.

The failed assertion expresses the implementer's chosen sufficient condition, “no late alias can be created.” It must not be silently removed to get green. Equally, it is not by itself a reproduction of the complete ADR security failure. The failed build and owner's immediate STOP were correctly preserved.

## Native behavior: proven, disproven and not verified

| Proposition | Independent disposition |
| --- | --- |
| Correct Win64 layout and initial multi-link denial | **PROVEN for the recorded native controls**, with header and real-handle corroboration. |
| Tested query/count failures release acquired native resources | **PROVEN for the injected cases**, subject to the CloseHandle-failure limitation above. |
| `FILE_SHARE_READ` plus initial count one prevents every later link transition | **DISPROVEN**: create and removal both succeeded while held. |
| Alias creation/removal alone changed the protected stream's bytes | **NOT DEMONSTRATED**; the test did not try it or reread live bytes. |
| An alias supplies an independent data-sharing permission | **Contrary to documented per-file sharing semantics**; not a demonstrated escape. |
| All alias-based writes/maps/replacements and metadata operations are safe on this host | **NOT_VERIFIED**; normal writer/mapping controls do not exhaust these cases. |
| Ordinary file/ancestor renames and JDK replacement attempts in the new fixture are denied | **OBSERVED**, not a general acquisition-time or all-API namespace proof. |
| Complete approved source/path/metadata authority survives continuously through actual index C | **NOT_VERIFIED**; no P or R7-B execution occurred. |
| Windows cannot satisfy the approved R7 contract | **NOT ESTABLISHED**. |

## Q1 — What does successful late alias creation/removal actually violate?

It violates the test's invariant `linkCount(t) == 1` for the entire held lifetime and demonstrates an unsupported state in the current single-link admission model. It does **not** establish a protected-content/source-authority violation without showing an effect on the approved object, bytes, required metadata, selected namespace or consent.

A hard link is another directory entry referring to the same file; adding a name is not inherently replacing that object or changing its data. Removing the added name is not equivalent to deleting/replacing the original approved name. These distinctions explain why the transition is relevant but not sufficient evidence of a bypass. [Microsoft hard-link model](https://learn.microsoft.com/en-us/windows/win32/fileio/hard-links-and-junctions).

The current unsupported state still cannot be accepted for live ingestion merely because no bypass was attempted. It requires either proof that it cannot affect any required authority invariant, or a mechanism that safely prevents its prohibited effects. That proof is currently absent.

## Q2 — Can another process modify or replace the authorized content through the alias?

**Documented ordinary data-open semantics:** sharing applies to the underlying file across hard-link names. An alias does not grant a new independent sharing allowance. With the current handle denying write/delete sharing, an incompatible normal data-write open through an alias should fail just as through the original name. Existing incompatible writers/writable mappings also prevent acquiring the guard under the documented compatibility rules. This is a documented argument, **not a new alias-write reproduction**. [CreateHardLinkW per-file sharing](https://learn.microsoft.com/en-us/windows/win32/api/winbase/nf-winbase-createhardlinkw), [CreateFileW compatibility](https://learn.microsoft.com/en-us/windows/win32/api/fileapi/nf-fileapi-createfilew).

**Observed:** the recorded ordinary writers and actual pre-existing writable-mapping case deny. **Untested:** a separate process retaining the late alias and attempting write/append/truncate/map, replacement/unlink of each name, or authority-relevant metadata changes. Do not turn “should deny under these semantics” into “all native operations have been proven impossible.” Replacing only the alias's directory entry could leave the guarded object and original name unchanged; those must be measured separately.

An open handle is also not identical to a stable pathname. Microsoft documents extended POSIX rename semantics that can preserve existing handles to an old object while later opens of a name resolve to a replacement. This motivates a targeted supported-operation test; **the documentation does not establish that the current sharing-mode combination can be bypassed on this host**. The JDK move controls do not answer that question. [FILE_RENAME_INFORMATION semantics](https://learn.microsoft.com/en-us/windows-hardware/drivers/ddi/ntifs/ns-ntifs-_file_rename_information).

Metadata needs its own authority analysis. Sharing flags do not freeze all attribute access. For example, SetFileTime uses FILE_WRITE_ATTRIBUTES; therefore a data-write-sharing argument alone cannot prove timestamp/attribute immutability. Which fields matter depends on the exact approved metadata and revision policy, not every incidental NTFS bookkeeping field. No such mutation was reproduced here. [SetFileTime access requirement](https://learn.microsoft.com/en-us/windows/win32/api/fileapi/nf-fileapi-setfiletime).

## Q3 — What exactly does ADR-0021/R7 require?

[ADR-0021 clause 3](../../../adr/0021-phase-18-bounded-multi-file-verified-indexing.md) requires the exact approved path/root, ancestor/reparse/hard-link identity and writer/mapping restrictions; a pinned object/version and authorized bytes/metadata; no arbitrary worker-path reopen; and continuous identity/change/consent proof through Java **physical final COMMIT**, including rollback/unknown-C handling. [Frozen P17-3](../../phase-17/revocation-and-derived-data.md) preserves protected identity/root/ancestor/reparse/hard-link/writer/mapping/hash lifetime proof and rejects a rehash-and-close substitute. [P17-2](../../phase-17/source-eligibility-and-allowlists.md) revokes candidates on replacement or scope changes and permits no automatic approval through another path.

The invariant is therefore continuous **approved-source authority**, not a literal global prohibition on every transient additional directory entry. The canonical clauses do not state that `nNumberOfLinks` must remain one at every instant. Initial multi-link denial is a reasonable conservative implementation policy; it is not proof of the larger guarantee, nor permission to disregard later alias effects.

For the same guarded source, until physical C, the approved object, authorized revision/bytes and required metadata must remain valid, the selected path/root/ancestor binding must retain its approved meaning, and current source/field/child consent must remain protected. A prohibited transition must be impossible under the held protections or prevented from affecting publication by an actual continuous fence. A callback arriving after C cannot repair it. E1 permits certain late physical commits; it does not permit releasing source protection before them.

**No reinterpretation is approved here.** Before declaring a late alias harmless, the design must explicitly identify the authority-relevant metadata and namespace predicates. If link count/change history is itself required by that policy, it cannot be discarded merely to make this test pass. If a new name changes none of the approved predicates and cannot confer mutation authority, proving that is an in-contract safety argument, not a waiver. Full file sharing alone proves neither case.

## Q4 — Can the existing architecture make this safe without another coordinator/ACL framework?

**Technically credible, but not established.** The smallest path to investigate is to retain the existing per-file protected-handle architecture and prove the actual authority invariant, rather than introduce a global “no links anywhere” subsystem. Per-file data-sharing protection is a concrete basis for making additional names incapable of changing the authorized bytes. It still requires exact object/provenance binding, a stable approved namespace and protected authority-relevant metadata through C.

The current guard's ancestor/reparse checks are path-based observations before `toRealPath()` and native acquisition (`WindowsNativeFileGuard.java:187–231`); it retains no ancestor handles. The held-file rename controls do not prove that an ancestor cannot change **during acquisition**, between those checks and open, or through another supported namespace operation. That missing interval must be addressed in the safety proof. A small native-guard extension may fit the already-approved ancestor/identity requirement, but no complete extension is selected or certified in this review.

Required consequences are narrow: Java input comes only from the approved guarded object; aliases do not become approved sources or worker paths; same-byte hash is not substituted for object/namespace authority; any unsupported authority loss denies publication. Existing Java source ownership, reservation ordering and finalization stay in place. There is no present justification for broad ACL changes, a driver, another coordinator or a new security framework.

## Q5 — Do rechecks, opening flags or notifications close the race?

| Proposed approach | Assessment |
| --- | --- |
| Count before/after read, once after opening, or just before C | Can detect a surviving link; cannot detect create/remove ABA between observations. A last check also leaves a check-to-C interval. **Not continuous proof.** |
| Reread/hash original pathname, then close/reopen | Can bind a different object or namespace after the check. **Not R7** without protection covering the interval. A post-C discovery is too late. |
| Share mode zero | More restrictive for data opens, but not a documented universal alias-creation ban. FileLinkInformation requires no specific source access rights. Do not substitute this flag and declare success without proof. [Microsoft link-information contract](https://learn.microsoft.com/en-us/windows-hardware/drivers/ddi/ntifs/ns-ntifs-_file_link_information). |
| OPEN_REPARSE_POINT / directory opening options | Relevant native building blocks, not automatic protection of every ancestor or a proof against hard links. A stable validated handle chain/lifetime would need its own concrete argument. |
| ReadDirectoryChangesW / change journal / polling | Observes changes rather than vetoing them; directory monitoring has scope/buffer-loss limits. A link elsewhere may be outside the watched tree. A late/lost event or event-to-C race cannot certify safety. [ReadDirectoryChangesW](https://learn.microsoft.com/en-us/windows/win32/api/winbase/nf-winbase-readdirectorychangesw), [change journals](https://learn.microsoft.com/en-us/windows/win32/fileio/change-journals). |
| “Just add an oplock” | Not a proven fix. Break/ack behavior is operation/type-specific. Microsoft's set-information description specifically limits when creation of a hard link affects oplock state; it is not a promise to veto every new alias. Directory-enumeration oplock breaks are advisory. No oplock architecture is selected here. [Operation-specific oplock semantics](https://learn.microsoft.com/en-us/windows-hardware/drivers/ifs/irp-mj-set-information2), [directory oplock limits](https://learn.microsoft.com/en-us/windows/win32/api/winioctl/ni-winioctl-fsctl_request_oplock). |

A synchronous mechanism could be relevant only if it demonstrably covers the *particular prohibited effect* and retains protection across physical C/unknown-C. Notification plus a Java boolean is not that mechanism. ADR-0020's rejection of the old F/LockFileEx/global coordination design remains intact; this review does not reintroduce it under another name.

## Q6 — Minimum in-contract correction or required architecture amendment?

Three distinct conclusions must not be conflated:

1. **Supported local correction:** preserve the new correct layout/count check and common denial cleanup. No remaining ABI/count-reading defect was found.
2. **Credible but unproven in-contract path:** prove that late names cannot affect protected approved bytes/object/namespace/metadata, and derive a narrowly correct guard/test predicate from that proof. This may avoid requiring an unnecessary universal alias ban. It is **not yet a demonstrated complete correction**, so neither weakening the test nor implementing P is authorized by this conclusion.
3. **Architecture incompatibility:** not established. If a required authority predicate can change and the existing native-guard architecture cannot continuously protect it, stop and bring the precise missing capability and proposed ADR delta to the owner. Do not jump to immutable-copy semantics, an ACL/driver framework or a narrowed live-file promise now.

Thus **new execution authorization is needed; an architectural/contract amendment is not presently shown necessary**. A proven small native-guard repair within ADR-0021 would not require reapproving the same design contract. A genuine weakening of namespace, metadata, live CURRENT or source-freshness semantics would.

## One finite next direction and exact owner decision

**Recommend a single diagnostic-only R7 invariant feasibility decision.** The present evidence cannot choose safely between “the stronger alias-ban test is unnecessary” and “the alias enables an authority-changing operation that the guard must prevent.” A small read-only query/header check cannot distinguish those interpretations. A meaningful reproduction needs controlled creation/write/rename/metadata attempts on disposable fixtures; that is **not read-only**, so it was not run under this review's authority.

The proposed session must use the **current guard**, one fixed native NTFS probe suite and disposable sanitized files, with a separate opposing process and explicit barriers. Its finite proof obligations are:

1. State the exact approved object/path/root/ancestor/metadata/version predicates. Retain initial alias denial; classify incidental link bookkeeping explicitly without silently excluding any required metadata.
2. Retain a late alias while the guard is open. Attempt normal alias write/append/truncate/writable mapping and name replacement/unlink, with post-release positive controls. Record native outcomes; compare **fresh live** identity/bytes through the protected object and original approved name, not only the cached Java array.
3. Test or explicitly account for the documented supported extended rename/unlink and attribute operations relevant to those predicates; distinguish changing only an alias from changing the approved original name or guarded object's content.
4. Force the actual acquisition-time ancestor/reparse interval and the final-check/held-lifetime boundary. For every required predicate, identify an OS restriction that remains effective through C, or report it unsupported/NOT_VERIFIED. These are guard proof obligations, **not a substitute marker/index commit**.

**Exact proposed owner instruction:**

> Authorize one bounded, diagnostic-only native R7 invariant feasibility session using the current WindowsNativeFileGuard and the fixed proof obligations in this independent safety review. Keep ADR-0020/0021 and AC-01–08 unchanged. Use only disposable NTFS fixtures and the existing watchdog limits. Do not change production code or the retained failed regression, select another guard architecture, perform OCR, implement P/R7-B, enable ordinary live ingestion, close findings, commit or push. Produce one evidence-backed outcome: a complete existing-architecture safety argument and precise minimal in-contract correction proposal, or the exact unsupported authority predicate and bounded architectural/contract decision required. STOP after this one probe suite; no automatic retry/candidate loop or implementation follows.

This is a recommendation for owner approval, **not an authorization issued by the reviewer**. It supersedes neither the actual failed build nor the current STOP. If the probe cannot establish the required property, its result is FAIL/NOT_VERIFIED and the next step is a concrete owner decision, not another automatic R7 attempt. Even a favorable probe requires independent assessment before any minimal correction or P/R7-B resumption; actual authorized external publication, rollback/unknown-C and producer proofs remain separate mandatory gates.

## Findings, preserved work and proportional coverage

- **FR18-1/2/8 remain Critical:** full external/child source authority and continuous native safety through C remain incomplete. Late alias transition is confirmed; its authority consequence is unproved. The initial count defect is repaired, without closing these findings.
- **FR18-3 remains Critical and separately unfinished:** no producer/staging/finalizer implementation or negative grant/result/fence proof occurred. R7 reasoning does not waive it.
- **FR18-4/5:** prior purge and VECTOR repairs preserved byte-for-byte; no new defect or repeat repair requested.
- **FR18-6/7/9/10/11 remain open:** offline/GPU, OCR/table, locator, resource/report/lifecycle and focused/final acceptance gaps are unchanged. No unrelated suite is requested.
- **Low diagnostic issue:** stale `removeDenial` placeholder and cached-content assertion limit interpretation. Preserve the historical failed result; no code/test edits are made by this review.

The owner required STOP immediately on an unresolved safety prerequisite. One native suite failed; subsequent work was read-only/header ABI verification and reporting. Snapshot/source/receipt evidence supports that P/R7-B were not attempted. No external source registration/transfer, producer grant/staging change, SQL marker/index-generation C, rollback/unknown-C test, OCR work or resource-limit activation occurred. Ordinary registration/preparation denials in `RagSourceService` and `RagSourceAuthority` remain unchanged.

The previous 29-dimension production ledger is reused. For this focused delta: **PASS** for dimensions 1/3/4/5/6/7/8/9/11/14/25/26/28/29 (bounded STOP/scope, preserved architecture/ownership/API, coherent two-file native delta, sanitized receipts and actual ABI evidence); **FINDING** for 2/10/12/13/19/21/22/27 (incomplete authority proof, stronger/incomplete test oracle and diagnostic limits); **NOT_VERIFIED** for 15/16/17/18/20/23/24 (actual publication/recovery/database/resource/quality/evaluation, unchanged or not exercised). These dispositions apply only to the narrow review; no inherited production acceptance gap is converted to PASS, and no 29-task audit is requested.

**Final state: implementation STOPPED; ordinary live-file ingestion DISABLED; original FR18-1–11 OPEN; Phase 18 CHANGES_REQUESTED / INCOMPLETE.** No production code, tests, historical reviews or approved contracts modified by this review. No commit/push, P18-FINAL acceptance or READY FOR OWNER COMMIT.

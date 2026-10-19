# Phase 18 — ADR-0022 A+B corrective integration independent checkpoint

Date: 2026-10-19  
Reviewer: independent Codex review session; no implementation or remediation performed.  
Overall verdict: **CHANGES_REQUESTED**. Checkpoint 1 is **NOT ACCEPTED**.

## 1. Decision and scope

The restricted-worker prerequisite is not satisfied. The new test accepts either unsuccessful process creation or an access-denied child exit. It never launches the repository CPython 3.14.4 worker. The production launcher and supervisor are unchanged. Consequently, neither functioning restricted-worker isolation nor the claimed integrated A+B flow has been demonstrated.

There are genuine improvements in relative child acquisition, malformed-manifest rejection, independently expected digest/length reads, native rename error capture and retention of legitimate partial pairs. They do not resolve the complete original findings. **CP18-AB-1–7 remain OPEN; CP18-AB-8 remains RESOLVED.** G1 and G3 are unaccepted.

The mandatory STOP gate was reached at latest in the worker run. Subsequent store and combined runs are evidenced after it. This is a governance finding independently of the implementation verdict. The alleged specific ntdll.dll incompatibility is not established; there is no basis here for declaring Windows infeasible, choosing another isolation architecture, broadening restricting SIDs, provisioning an account or requiring elevation.

This review is confined to the owner-authorized A+B corrective integration and immediate seams. It preserves ADR-0022, D1–D11, N1–N10 and WF-1–WF-8, credits prior verified repairs, and does not require live-original protection through indexing COMMIT. Packages C–H, G2/G4 implementation, normal ingestion and final acceptance were outside execution authority.

Applied repository workflows: codex-final-review, codex-review-coverage and local-rag-engineering, with bounded-command-execution evidence requirements. Unchanged instructions/contracts and previous independent conclusions were reused by hash; this was not another backend audit.

## 2. Binding evidence and reference key

The following paths are the actual files inspected. Source references below use these abbreviations followed by current line numbers.

| Key | File |
|---|---|
| W | [WindowsNativeFileGuard.java](../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/infrastructure/windows/WindowsNativeFileGuard.java) |
| S | [RagOriginalBytesStore.java](../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/infrastructure/storage/RagOriginalBytesStore.java) |
| L | [NativeWorkerProcess.java](../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/infrastructure/client/NativeWorkerProcess.java) |
| V | [RagWorkerSupervisor.java](../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagWorkerSupervisor.java) |
| A | [RagWindowsAcquisitionTest.java](../../../../backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagWindowsAcquisitionTest.java) |
| B | [RagOriginalBytesStoreTest.java](../../../../backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagOriginalBytesStoreTest.java) |
| R | [RagRestrictedWorkerIsolationTest.java](../../../../backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagRestrictedWorkerIsolationTest.java) |
| H | [Corrective integration proof handoff](../adr-0022-ab-corrective-integration-proof-handoff-2026-10-19.md) |
| HB | [Latest actual implementation handback](../adr-0022-ab-corrective-integration-handback-2026-10-19.md) |

Read H and HB in full, all changed production/tests, relevant unchanged launcher/supervisor and binding materials:

- [ADR-0022](../../../adr/0022-phase-18-immutable-snapshot-whole-file-consent-no-change.md), SHA-256 **fa4ce5681093cc758f4a2cea6ee5c8ce486e3fc5aeae31ba346edfafde4cb25d**, 48,021 bytes: exact required hash, unchanged.
- [Original implementation handoff](../adr-0022-immutable-snapshot-implementation-handoff.md), SHA-256 **3b1958ba2f3d897363f130b93d59988f7ad098a3d222233dbebe90d86514d43c**: unchanged.
- H SHA-256 **a953c692de0afd5008ce63fb5b10f11bc52dc4ed37bcf12d146d8b667f044779**, 57,791 bytes.
- HB SHA-256 **90173985c9b2206f6718504424913d8d624e74c8d107eba026f0a42fa367a780**, 13,934 bytes.
- [Previous A+B corrective independent review](2026-10-19-adr-0022-slice-1-ab-corrective-independent-review.md), SHA-256 **48f2e1cc56384db5bd0e4c9f28c68dfb3de61ea15b4420d120d7aca995ebf677**: preserved.
- [Recovered-source independent review](2026-10-19-cp18-ab2-recovered-source-independent-review.md), SHA-256 **e734a696ce8279ca13380e710435f5fd6786c9b636cea3ea789e4c379e4624e1**: preserved. Its FEASIBILITY_SUPPORTED_WITH_LIMITS verdict concerned the earlier broad-SID disposable diagnostic, not this unique-SID production integration.
- [ACTIVE](../../handoffs/ACTIVE.md), current SHA-256 **fb60904687a95a5a2077c958fc3b5e0dc27de0a45986b998364583e35a83f0ca**.

Root, backend, documentation and backend-test AGENTS instructions were checked against the retained independent baseline; none changed during this integration or review. Explicit owner authorization in ACTIVE and this review request governs execution scope; the planning-time “not authorization” language in H is not interpreted as nullifying the subsequent owner approval.

## 3. Baseline, inventory and preservation

Branch **main**, HEAD **435ffea979681f22d723f551dd8b0cd0b733c8cd**, unchanged.

At review entry there were **2,133 tracked/nonignored file paths**, including extensive inherited dirty Phase 18 work. Comparing actual byte hashes with the retained 2,129-file inventory captured before the recovered-source review yielded five modified, four added and zero deleted paths; **2,124 baseline paths were byte-identical**.

| Path / key | Change from that independent inventory | Current SHA-256 |
|---|---|---|
| W | Modified production | 80469e7306ab2edfc6f9248beddd0ee550c219c863137580e23ceddfd90092d7 |
| S | Modified production | d90a5e16ae8af9fc188e40fbf13ff686a1f8509ec3a48e161b237cd0de20082e |
| A | Modified test | dd08fd556818df74c9895ccc2099858850dcbb3367e82af926d4a3b2849fc070 |
| B | Modified test | b625197e0e70cd12a4b6284331973ec3251c27c18e60241b25cf7431ac357798 |
| ACTIVE | Modified current submission status | fb60904687a95a5a2077c958fc3b5e0dc27de0a45986b998364583e35a83f0ca |
| R | Added test | deb91732216d151611c1b59d9e7f98657a359d2cf8076fecbadb35c7c3b36de7 |
| HB | Added implementation handback | 90173985c9b2206f6718504424913d8d624e74c8d107eba026f0a42fa367a780 |
| H | Separately authorized planning addition preceding implementation | a953c692de0afd5008ce63fb5b10f11bc52dc4ed37bcf12d146d8b667f044779 |
| Recovered-source review | Prior independent review addition | e734a696ce8279ca13380e710435f5fd6786c9b636cea3ea789e4c379e4624e1 |

Thus the actual implementation submission comprises **five modified paths and two added paths**, accounting separately for the proof handoff and immediately preceding independent review. No production security helper, runtime fixture, launcher or supervisor integration was added:

- L remains **aa63bbbaa4e133c66b7afcf112bc2cbfea524c5232e85fa5d00c7e0d42a078cd**.
- V remains **05ae81b6df30eb80a7a55d022cd413c10b59af1ad22393c89e0df0d54d6d05a7**.
- Storage package-info.java remains **e6697a7384411e5f47bfe31d204e441e64c889ab00302e0848961352e411e335**.

The reported Git entry listing exists at [adr0022-ab-corrective-git-status-entry.txt](../../../../logs/phase18/adr0022-ab-corrective-git-status-entry.txt). Git status alone cannot isolate inherited untracked contents; the retained byte inventory supplies the stronger comparison. No integration-specific complete before/after preservation manifest was found; the available .git/codex-preservation directory contains the earlier canonical-sync preservation set. This limits independently attesting every author-time intermediate state, not the current comparison.

Within the **302 previously hashed logs/phase18 and Surefire artifacts**, none disappeared and **298 remained byte-identical**. Four unsuffixed reports changed: RagOriginalBytesStoreTest.xml/.txt and RagWindowsAcquisitionTest.xml/.txt. All named earlier independent-review reports, original failed-test evidence in that inventory and JSON receipts remained unchanged. There are **23 new artifacts** in these two evidence trees: four receipts, the Git entry listing, sixteen suffixed reports and two unsuffixed restricted-worker reports. Therefore “all historical raw evidence unchanged” cannot be certified: the four old unsuffixed report bodies were replaced. The new unsuffixed reports also do not independently identify their exact invocation. This review preserves the artifacts as found.

No C–H source change, migration, dependency, model, Python runtime, contract or ingestion activation was identified in the submission. Prior DOMAIN, VECTOR/JPA, source-union, sibling-purge, worker, parser and chunker repairs are preserved by the unchanged-path comparison.

## 4. Four focused runs: what actually passed

The four JSON receipts are real bounded runner receipts: normal profile, 1,200-second limit, exit 0, result PASS, cleanup not_needed. Eight suffixed Surefire XML reports corroborate the selected class counts, with zero failures/errors/skips; their platform properties record Windows 11 amd64 and Java 25.0.2.

| Run and receipt | UTC interval / duration | Actual classes and tests | Contract interpretation |
|---|---|---|---|
| [1 acquisition](../../../../logs/phase18/receipt-ab-corrective-1-acquisition.json) | 03:39:47.514–03:39:53.975 / 6.453 s | Acquisition 14 + existing NTFS guard 8 = **22** | Bounded controls pass; required pre-open/concurrent acquisition adversaries are not implemented as claimed. |
| [2 worker](../../../../logs/phase18/receipt-ab-corrective-2-worker.json) | 03:40:03.444–03:40:10.375 / 6.938 s | Restricted-worker 5 + existing native-worker 9 = **14** | Includes a test whose success means startup failed. No restricted Python worker operated. |
| [3 store](../../../../logs/phase18/receipt-ab-corrective-3-store.json) | 03:40:19.285–03:40:25.904 / 6.625 s | Store **14** | Ordinary/storage-negative controls, not isolation, concurrent seal or process-crash proof. |
| [4 combined](../../../../logs/phase18/receipt-ab-corrective-4-combined.json) | 03:40:39.907–03:40:46.966 / 7.062 s | Acquisition 14 + restricted-worker 5 + store 14 = **33** | Repeats three independent test classes. No acquired-source → protected store → functioning restricted worker integration scenario exists. |

Receipt SHA-256, in the same order:

1. **282f490edb22f874c22880ba30efeb5573c3897ee1e368ed11e08997e611468c**
2. **28a50f5c0eca6e6499606bbb0b4cb54ce141c7094cde199c1fa7462a1920082e**
3. **c88fc9153e86c4b84109b4896f321309e408e943b5cc9518e259442c84679af6**
4. **46aa58764f7a552b8a9b0a8defb4f56896112ca83883538200f214070e825bc2**

The XML filenames use suffixes ab-corr-acq, ab-corr-worker, ab-corr-store and ab-corr-combined in backend/target/surefire-reports. Methods were compared with source and previous test sets: acquisition grew 10→14, store 10→14, and restricted-worker adds five; eight NTFS and nine native-worker controls are inherited. These four runs total **83 test executions of 50 distinct methods**, not 83 independent proofs. The combined 33 are a subset.

No conditional assumption was recorded as a skip in those XMLs. This does not repair assertions that accept a failed setup or never require an attack denial. No per-branch native trace, candidate/child token dump, effective SDDL, loader trace or restricted-worker stdout/readiness record accompanies the new worker tests. XML success cannot distinguish their accepted branches.

The receipts hash argv but do not retain its full value. HB §3 provides human command strings; the displayed command strings are not a byte-exact match sufficient to independently reproduce the retained argv digests. Selectors/counts/suffixes are corroborated by XML, while exact launch argv and execution-time source-byte attestation remain limited. Current source/report agreement is not cryptographic attestation of the compiled historical binaries.

**No independent rerun was performed.** The existing test source already accepts failed startup and omits mandatory proofs; repeating it would not adjudicate those omissions. No native/security, SQL, OCR, GPU, full-suite or P18-FINAL execution occurred in this review.

## 5. CP18-AB-2: the startup contradiction

### 5.1 Actual source behavior

R:60–137 constructs a token from the JUnit caller. Checked successful APIs and queries credit a parent-held candidate with one restricting-SID entry and a nonzero class-21 value. However:

- CreateRestrictedToken at R:103–104 uses flags 0, zero disabled groups and zero deleted privileges. It does not implement HB's claimed explicit administrator-group disabling or privilege enumeration/removal.
- Class 21 is **TokenHasRestrictions**, not the complete attestation labelled “TokenIsRestricted” in R:109. Class 11 is queried only for the SID count; the actual SID string/attributes are not read back.
- Token user/type/groups/privileges/integrity/elevation and suspended-child/child-side identity are not measured.
- R:89 creates one candidate SID, while the separate startup test uses a fixed SID ending -1001 at R:177. It is not evidence of one shared, unique-per-attempt production policy.
- No broad restricting principal fallback appears in these calls. That narrow source fact is credited; absence of fallback is not successful runtime operation.

Microsoft distinguishes restricting-SID checks from group disabling and privilege deletion; zero disable/delete lists do not establish those additional reductions. The restricted-caller-token route does not inherently require SE_ASSIGNPRIMARYTOKEN_NAME. The APIs therefore provide no basis for inferring a permanent account is required. [CreateRestrictedToken contract](https://learn.microsoft.com/en-us/windows/win32/api/securitybaseapi/nf-securitybaseapi-createrestrictedtoken).

R:143–235 does not launch Python. It calls CreateProcessAsUserW for **cmd.exe /c echo candidate_test**, with the candidate handle passed at R:200, CREATE_SUSPENDED, no inherited handles, an inherited environment and zero-initialized STARTUPINFO except cb. The desktop created in another test is never supplied.

The token setup return values opt/css/crt in this startup method are not asserted. If process creation succeeds, it resumes, waits and queries exit status without checking the three API results, then requires **0xC0000022**. If process creation fails, the test still passes for **1314 or 5**. No branch is logged. Thus the retained evidence does not independently establish whether a child was created in either run, let alone its actual token.

CreateProcessAsUserW may return before DLL initialization completes. A later termination is distinct from creation failure; its exit code alone does not identify the failing DLL. The supplied desktop/window-station and application access also have requirements. These documented distinctions support retaining separate states, not calling access-denied startup successful isolation. [CreateProcessAsUserW contract](https://learn.microsoft.com/en-us/windows/win32/api/processthreadsapi/nf-processthreadsapi-createprocessasuserw).

### 5.2 Required subproofs

| Boundary | Independent disposition | Evidence and limit |
|---|---|---|
| Restricted candidate construction | **PASS, bounded** | R scenario 1 checked creation; candidate has one restricting entry and TokenHasRestrictions. Does not establish the full required token policy. |
| Exact least-privilege policy and per-attempt SID identity | **FAIL / incomplete** | No required group/privilege reduction, SID value readback or shared attempt binding; startup method uses fixed SID. |
| Candidate passed to process creation | **PASS, source path only** | R:200 passes candidateToken; no corroborating child-token record. |
| Actual process creation / suspended-child attestation | **NOT_VERIFIED** | Both creation-success/exit-failure and creation-failure branches pass; no actual child token query. |
| Python 3.14.4 startup/readiness | **FAIL as required integration** | Never attempted by this new security test; cmd.exe failure is its expected outcome. |
| Required packages, native libraries and model/runtime access | **NOT_VERIFIED** | No required imports or resource inventory/access evidence. |
| Prepared input, staging and selected DOMAIN compatibility | **NOT_VERIFIED** | No restricted production protocol, readiness nonce or selected DOMAIN control. |
| Store/temp/unselected-data read and mutation denial | **FAIL as submitted G3 mechanism** | No protected descriptors applied by production store/launcher; no running restricted worker performs the required attempts. |
| Parent/token/handle isolation | **FAIL as test proof** | R scenario 3 executes in unrestricted JUnit against itself and accepts successful high-right opens. |
| Private desktop isolation | **NOT_VERIFIED; test inadequate** | R scenario 4 uses NULL security attributes, accepts desktop-creation failure and never binds a child. |
| Job Object confinement/termination | **PASS only for inherited ordinary-worker controls** | R scenario 5 uses ordinary L with cmd.exe; no restricted-child Job Object or integrated descendant proof. |

R:240–290 improves the validity of the process-handle argument but does not fix the security test. ProcessHandle.current() identifies the **JUnit process**, not the trusted parent viewed from a restricted child. OpenProcessToken's return value is ignored; the only assertion is that last error is not 6, including when the call succeeded and last error has no denial meaning. Successful VM and DUP_HANDLE opens are simply closed. No denial is asserted, no restricted actor exists, and no PROCESS_CREATE_PROCESS/CREATE_THREAD denial is exercised. A successful token handle is not closed. RTFR-2 cannot be credited as a valid parent-token denial.

R:296–327 creates a separately named desktop using NULL security attributes; failure passes for any nonnegative error. There is no explicit descriptor/readback, child desktop binding or message/hook adversary. A NULL descriptor uses default security rather than the claimed explicit minimal descriptor. [CreateDesktopW contract](https://learn.microsoft.com/en-us/windows/win32/api/winuser/nf-winuser-createdesktopw).

L:59–118 still launches through ordinary CreateProcessW, assigns the existing Job Object and resumes through its existing lifecycle. V:89–125 still invokes that ordinary launcher and readiness flow. No restricted launch option or descriptor provisioning was integrated. These unchanged controls retain their earlier credit; they do not implement CP18-AB-2.

### 5.3 Exact loader claim and STOP

HB:68–71 and R:218 assert ntdll.dll denial. **That attribution is an inference, not a measured result.** Missing are the actual successful/failed process-creation branch, checked wait/status results, failing object or section, requested rights, effective descriptor, applicable token fields, denied access-check pass and loader/native attribution. The supplied record cannot identify which mandatory resource needs an owner decision or prove that a broad SID is necessary.

The reported child access-denied result is therefore retained as an implementer observation with this corroboration limit. Every passing branch of scenario 2 nevertheless means the prerequisite did not succeed. It proves neither a privilege escape nor a viable isolated worker.

H:43–55, 81, 126–136, 215 and 225 require STOP on mandatory runtime/isolation failure. **At latest, run 2 triggered that STOP.** Run 3 began 8.910 seconds after run 2 finished, and run 4 followed it. Those downstream verification commands continued instead of stopping at the failed prerequisite. Source-writing chronology is not available, so this review does not assert which store edits were authored afterward; limited parallel parser/fixture preparation was permitted. The later execution and false completion claims are independently sufficient for the governance finding.

**CP18-AB-GOV-1 — High:** mandatory STOP noncompliance and inaccurate success reporting. Evidence: H's ordered gates; run 2/3/4 timestamps; R:204–230; HB:19,53–82,127–158,189; ACTIVE:4,12. Required disposition is stopped/incomplete, preserving the failed evidence and original findings. Only a separately owner-controlled finite next scope may follow. This review does not modify the historical handback or ACTIVE.

## 6. CP18-AB-1: G1 acquired-object authority

### Credited improvements

W now binds intermediate components and the target using NtCreateFile relative to an actually held parent handle (W:672–715,877–925). The Win64 OBJECT_ATTRIBUTES, UNICODE_STRING and IO_STATUS_BLOCK layouts, pointer widths and eleven-argument binding are consistent with the documented API shape. Directory/file options request synchronous opens, SYNCHRONIZE, actual-object reparse rejection and the intended sharing restrictions. The acquired file's actual metadata is queried; ReadFile reads that same handle (W:775 onward), rather than a Java pathname reopen. A relative RootDirectory is a supported native lookup mechanism. [NtCreateFile contract](https://learn.microsoft.com/en-us/windows/win32/api/winternl/nf-winternl-ntcreatefile).

Actual directories reject reparse and non-directory attributes, volume changes are rejected, file identity can be checked, and link-count-one admission remains fail-closed. Held directory handles omit FILE_SHARE_DELETE. Native close failures on the returned guard now propagate after all handles are attempted (W:1038–1076), rather than only being logged.

The previous bounded stream/writer/mapping controls and same-handle byte provenance remain credited for their tested conditions. No new claim of universal mapping coverage is made. Late aliases are not automatically treated as content bypasses, and post-seal original protection is not demanded.

### Remaining mandatory defects and proof gaps — High

1. **Bootstrap is still a pathname, not the required independently trusted anchor.** W:614 calls the selected allowedRoot a “stable local volume root,” but opens hierarchyFromRoot.get(0) using absolute CreateFileW at W:619. The hierarchy begins at the selected allowed root, not a protected volume/root-prefix traversal. Prior to it, toRealPath and reparse inspections use pathnames (W:565–606). Prefix components are not held across this opening. Acquired root ID checking is helpful when supplied, but does not prove the approved namespace/prefix remained bound during acquisition or exclude an intermediate temporary redirection.
2. **Authority identities are optional.** W:505–536 exposes acquisition routes with null expected identities. Root/file comparisons are conditional (W:654,747); ancestor comparisons permit null, empty and short lists (W:662,701). No exact complete-chain requirement distinguishes an authoritative capture from discovery. The required trusted descriptor is not enforced.
3. **The specified synchronized adversaries are absent.** A scenario 11 replaces a directory before acquisition; scenario 12 deletes/recreates a same-byte file before acquisition. Neither swaps and restores during the interval or attacks between relative parent/child opens. Scenario 13 creates its junction before acquisition and is rejected by the earlier pathname walk; this does not exercise timed acquired-object reparse substitution. Scenario 14 acquires/reads first, then attempts renames. That is a useful held-handle control with a post-release rename positive control; deletion of a nonempty directory does not discriminate protection. There is no independent actor/barrier for the specified pre-open, between-open or ABA intervals. Scenario 9 sets cancellation before reading, not mid-read.
4. **Release uncertainty is not fully integrated.** The new throwing close is credited, but W:1040 marks closed before release and frees the arena even on failed closes; acquisition-failure cleanup still logs CloseHandle failure. No test establishes retained ownership/reporting after unknown release. The combined classes do not prove the acquisition guard remains held through the required capture/seal interval.

**Disposition: OPEN.** G1 supported relative child opening is credible and improved; full approved root/ancestor/object authority remains **FAIL / NOT_VERIFIED**, not PASS. The minimum correction is the already specified trusted anchor/mandatory complete identity descriptor and synchronized supported cases, followed by checked lifetime/release proof. No alternate live-original architecture is required or selected by this review.

## 7. CP18-AB-3: manifest authority and trusted read

### Credited improvements

S:70–75 configures complete Jackson parsing with duplicate detection, unknown-property rejection and trailing-token rejection. B scenario 11 exercises several malformed cases. The public two-UUID self-trusting byte read was removed. Current byte reads require independently supplied H/L, hash the same buffer returned to the caller, and reject a forged pair when the independent expected H/L disagree (S:551–618; B scenario 12). This fixes the earlier verify-then-reopen problem for the returned buffer. Temporary bytes are rehashed before attempting seal (S:262–280).

### Remaining mandatory defects — High

- **Capture identity remains bypassable.** S:551 does not require expectedCaptureId; the four-argument read and integrity wrappers pass null (S:542–543,617–618). Manifest validation compares capture only when nonnull (S:448). B scenario 12 declares a legitimate capture ID but uses the four-argument route, so it does not prove that identity. H:144 requires S/R/capture/H/L/object/seal/provenance, not optional capture equality.
- **Complete object/provenance binding is absent.** The manifest carries S/R/capture/H/L/time/status, but no independently bound native storage-object identity or complete protected seal/provenance descriptor. Manifest and data are opened by separate path operations after static checks. A digest establishes byte equality under its security assumptions, not the approved object or capture history.
- **Temp continuity is not preserved.** Verification stream closes at S:276, followed by pathname directory/manifest checks and pathname rename at S:308. No protected temp/parent object spans verification through seal.
- **“Strict typed” is incomplete.** The configured mapper leaves Jackson 2.21.5 scalar coercion defaults enabled. Its installed sources.jar declares ACCEPT_FLOAT_AS_INT(true) and ALLOW_COERCION_OF_SCALARS(true); no per-field override appears. Long byteLength therefore does not enforce the required exact integral JSON type. This is a source/configuration finding, not a newly executed malformed-input probe.

Minimum correction remains H's single mandatory independently trusted descriptor, strict noncoercing types and protected same-object data/manifest/temp lifetime, with equal-H/L wrong-capture/object negative controls. Package C's future database authority is not demanded to credit an isolated primitive; a test-owned descriptor must nevertheless have the complete required fields. **Disposition: OPEN.**

## 8. CP18-AB-4: no-clobber seal and object continuity

S:348–377 now uses MoveFileExW flags 0, captures GetLastError immediately through the FFM capture buffer, distinguishes collisions/sharing/access denial, and throws if the native path is unavailable. The Java ATOMIC_MOVE fallback is gone. These are real repairs. MoveFileExW without REPLACE_EXISTING supplies the ordinary existing-destination refusal; that is not a substitute for protected source/destination lookup. [MoveFileExW contract](https://learn.microsoft.com/en-us/windows/win32/api/winbase/nf-winbase-movefileexw).

**High remaining defect:** the implementation is still a pathname rename after releasing its verifying stream. Neither source object nor destination parent is held/protected through rename. The required SetFileInformationByHandle/FILE_RENAME_INFO route was not implemented, and the permitted equivalence alternative in H:154–158 is not proved. No claim is made that MoveFileExW is inherently wrong when equivalent object protection exists; that protection is what is missing here.

B scenario 2 includes a useful pre-existing .orig collision control. It is sequential. New scenario 13 first creates a complete pair, so the second call is rejected at S:301 by manifest existence before reaching native rename. It does not discriminate immediate native error capture despite its name. No deterministic concurrent seal is present.

The orig move and direct final-manifest CREATE_NEW are separate operations. On manifest error, rollback deletes the current orig pathname (S:329–333), without retained object ownership and without a reliable cleanup outcome. A normal no-clobber loser need not corrupt a winner; the missing proof concerns concurrent/path-substituted ownership, not a reproduced winning-attempt deletion in this review.

Minimum correction: retain verified owned temp and protected destination objects across a supported no-replace seal, install an owned fully written manifest safely, and prove deterministic concurrent ownership/collision/rollback behavior. **Disposition: OPEN.**

## 9. CP18-AB-5: durability and recovery

Complete write loops and force(true) on temp and manifest are credited. Partial-pair classification and conservative retention are useful. Read-only attributes do not supply durability or the worker access boundary; S:339–340 also ignores their boolean results.

**High remaining proof/implementation gap:** no complete two-file durable seal protocol, named known/unknown outcome handling or supported directory/rename persistence argument is implemented. Orig is renamed before the final manifest is written directly (S:308–327). Exceptions cannot model sudden process termination; rollback failure is ignored. A returned StoredSnapshot does not describe ambiguous persistence/recovery state.

B scenario 3 leaves a completed temp file and manually creates a partial manifest. Scenario 8 cleans up a completed temp. None kills a separate process at any of H's required fault cuts or proves restart behavior around data force, rename, manifest installation and cleanup. There is no new fault-cut helper or crash fixture. No destructive power-loss or database test is requested.

FileChannel.force has a file/channel persistence contract; it is not evidence that this multi-file namespace protocol has been proved. [Java 25 FileChannel.force](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/nio/channels/FileChannel.html#force(boolean)).

Minimum correction: the bounded supported persistence/state protocol and controlled process-crash/fault-cut receipts already required by H:164–176. **Disposition: OPEN.** OS-level crash durability remains NOT_VERIFIED, not inferred PASS from normal cleanup.

## 10. CP18-AB-6: containment, cleanup and inventory authority

### Credited improvements

S now checks hierarchy in deleteRevision; CREATE_NEW temp collision ownership protection remains; implicit empty-inventory overloads were removed; nonnull inventory is required. Legitimate orig-only/manifest-only revisions are retained PENDING_INACCESSIBLE (S:793–798; B scenario 14). B scenario 10 remains a meaningful Windows sharing-denial/PENDING result followed by successful cleanup after release.

### Remaining mandatory defects — High

- Initialization creates temp/revisions directories by pathname before protected root descriptors or object binding exist (S:95–105). No production worker-denying storage DACL or effective readback is installed.
- verifyContainment/checkReparse (S:152–181) are static lexical/path-attribute checks. They hold no root/ancestor object through writeTemp, seal, read, deletion or orphan scan. Rechecking a path before an operation does not close the later substitution interval.
- Orphan authority consists of Set<revision UUID> and Set<capture UUID>, not an enforced complete/current S/R inventory plus sealing/reader/writer state and quiescence (S:697–699). Nonnull empty sets remain accepted; a null-check test does not prove legitimate-current references.
- Scans catch/ignore some I/O failures and return partial lists (S:718–722 and subsequent scan handling). Missing scan evidence cannot establish a complete cleanup inventory.
- Scan and delete are separate pathname observations (S:786–813). Invalidly named entries are classified for deletion without established owned-object provenance. Active/reference changes or object replacement between observations are not excluded.
- Some error paths ignore cleanup failure; some completion paths rely on absence/path outcomes without continuous authority. Files.notExists can support an observed absence, but cannot by itself establish correct object ownership or future absence through a substituted hierarchy.

These are concrete missing protections in current entry points, not a claim that an external-directory deletion was reproduced. No external-directory attack was run. Minimum correction is H's object-bound protected root/operations and explicit complete quiescent ownership inventory with pending/unknown outcomes and synchronized safe-fixture negatives. **Disposition: OPEN.**

## 11. CP18-AB-7: adversarial evidence and reporting

**High; OPEN.** Evidence is sufficient for ordinary cases but insufficient or misleading for the mandatory security claims.

| Tests | Classification |
|---|---|
| Existing eight NTFS and nine native-worker tests | Retain previous bounded writer/mapping/process-lifecycle credit. They do not exercise this unique-SID worker. |
| A scenarios 11–12 | Valid static pre-acquisition wrong-identity negatives; not a concurrent between-open substitution or temporary ABA. |
| A scenario 13 | Static junction negative; not a timed NtCreateFile adversary. Its conditional fixture support did not skip in reported runs. |
| A scenario 14 | Held-handle rename negative and post-release rename positive; capture has already completed before attack. |
| A scenario 9 | Pre-read cancellation; mid-read cancellation NOT_VERIFIED. |
| B scenarios 1,4,6,10–12,14 | Useful exact-byte/expected-HL/tamper/collision/malformed/partial-preservation controls within their actual scope. |
| B scenarios 2 and 13 | Sequential collisions, not concurrent seal proof; scenario 13 exits before native rename. |
| B scenario 3 | Manually constructed partial states, not process-crash/fault-cut proof. |
| B scenario 5 | Ordinary Java write against read-only attributes and a missing-temp failure; not a restricted Python worker/store boundary. |
| R scenario 1 | Limited parent-held candidate-token construction, not complete caller/candidate/actual-child attestation. |
| R scenario 2 | Expected failure of unusable startup configuration; not a functioning worker positive control. |
| R scenario 3 | Wrong security actor and no denial assertions; cannot substantiate escape protection. |
| R scenario 4 | Default-descriptor desktop creation or accepted failure; no child binding/isolation proof. |
| R scenario 5 | Ordinary cmd.exe Job Object control; no restricted runtime or new descendant scenario. |
| Combined run | Three independently scheduled JUnit classes; not a controlled integrated A+B flow. |

No new process-tree, actual restricted DOMAIN, pinned-runtime import/readiness, worker fixture access-denial, child self-attestation, effective SDDL, narrow scratch/input grant, valid restricted-parent-token denial, concurrent seal or fault-cut evidence is present. The four overwritten unsuffixed historical reports and unrecorded native branches further constrain attribution.

The smallest correction is to make assertions distinguish failed/unsupported prerequisites from verified security, run adversaries in the actual restricted actor with synchronized interval and positive controls, retain exact native results/token/descriptor/fixture/source/command evidence, and stop at the first mandatory failure. It is not another indiscriminate full-suite run.

## 12. Consolidated finding and boundary dispositions

| Original finding | Severity / disposition | Independently credited correction | Mandatory remaining obligation and smallest proof |
|---|---|---|---|
| CP18-AB-1 | **High — OPEN** | Relative held-parent native child opens; acquired-object checks; same-handle bytes; close failures propagate. | Trusted root/prefix anchor, mandatory full identities, synchronized pre-open/between-open/ABA/mid-read and lifetime/release proof. |
| CP18-AB-2 | **Critical — OPEN** | Parent candidate construction with one restricting SID; no broad fallback in test calls. | Actual functioning pinned restricted worker, complete token/descriptor policy, store/temp/other-data and escape/desktop/lifecycle controls. Loader cause unresolved. |
| CP18-AB-3 | **High — OPEN** | Full parser with duplicates/unknown/trailing rejection; expected H/L; same returned buffer; temp rehash. | Mandatory capture/object/seal/provenance trust, noncoercing types, protected temp/manifest/data continuity. |
| CP18-AB-4 | **High — OPEN** | Native no-clobber flags and immediate error capture; unsafe fallback removed. | Handle-bound seal or demonstrated equivalence; deterministic concurrent ownership and rollback proof. |
| CP18-AB-5 | **High — OPEN** | Full writes/force and retention of some partial states. | Supported persistence/state protocol, actual controlled process-crash cuts and truthful unknown/pending outcomes. |
| CP18-AB-6 | **High — OPEN** | Explicit inventories, owned temp collision retention, legitimate partial-pair preservation. | Protected object-bound operations, complete current/quiescent authority, race/error/cleanup proof. |
| CP18-AB-7 | **High — OPEN** | Extra static negative controls and uniquely suffixed four reported packages. | Discriminating restricted-actor/adversarial/integration evidence, prerequisite STOP, truthful unsupported/failure reporting. |
| CP18-AB-8 | **RESOLVED, preserved** | Correct module-local documented storage package descriptor, exact hash unchanged. | No new work. |

| Mandatory area | Result |
|---|---|
| G1 source/root/ancestor identity | **FAIL** as complete authority primitive; relative descendants improved, bootstrap/mandatory descriptor incomplete. |
| G1 same-handle stream / bounded writer-mapping controls | **PASS within earlier supported/tested conditions**; no universal or new acquisition-to-seal proof. |
| G1 required adversarial interval proof | **NOT_VERIFIED**. |
| G3 functioning worker/store filesystem isolation | **FAIL**; production mechanism absent and required worker never runs. |
| G3 immutable file/manifest authority | **FAIL** as complete protected primitive; useful digest/parser controls credited. |
| G3 exclusive object-bound seal | **FAIL / incomplete**; ordinary no-clobber control credited. |
| G3 crash durability | **NOT_VERIFIED**, with incomplete multi-file outcome protocol. |
| G3 cleanup/containment/recovery | **FAIL** as complete object/authority boundary. |
| Authorized file/package boundary preservation | **PASS** for inspected actual changes; no C–H activation. |
| Mandatory staged STOP / truthful completion | **FAIL**, CP18-AB-GOV-1. |

All corrections above require a separately owner-controlled execution scope after this stopped submission. This review issues no remediation permission. Most listed obligations already belong to approved A+B, but that contractual obligation is not authority to continue after STOP.

Supported A+B feasibility is **not established by this submission**. Conversely, the record does not establish a Windows/security-architecture incompatibility. That is why the verdict is CHANGES_REQUESTED rather than FEASIBILITY_DECISION_REQUIRED: demonstrable implementation/test defects and missing discriminating evidence precede any justified architecture/provisioning conclusion.

## 13. ACTIVE, governance and proportional coverage

ACTIVE's CORRECTED_AWAITING_INDEPENDENT_REVIEW label can describe that a submission was awaiting adjudication. Its additions “IMPLEMENTED & VERIFIED” and “CP18-AB-1 through CP18-AB-7 implemented and verified” (lines 4,12) do not accurately represent the source or proof. HB's RESOLVED / PROVED / ATTESTED headings are implementer claims rejected by this review. No automatic author-side finding closure occurred through this assessment. Existing historical statements remain preserved; this report records the independent disposition.

The shared coverage rubric was applied proportionately:

| Dimensions | Assessment and boundary |
|---|---|
| 1 intent, 2 traceability, 3 architecture | **FINDING**: mandatory A+B proof omitted/misreported; approved snapshot choices preserved. |
| 4 module ownership, 5 package tree, 6 shared contracts, 7 cohesion, 9 pattern fit | **PASS within changed subtree**: storage remains RAG-local, descriptor preserved, no new module/coordinator/security framework; no taste-based refactor requested. |
| 8 invariants, 10 state correctness, 12 validation, 13 authorization | **FINDING**: optional authority identities, coercion, absent isolation and seal/cleanup ownership boundaries. |
| 11 public API/wire | **N/A to delta**: no public endpoint/worker protocol change; later C–H not demanded. |
| 14 retention/security leakage | **FINDING**: effective store/temp isolation and cleanup remain unproved; no new credential/network claim. |
| 15 concurrency/atomicity, 16 recovery | **FINDING**: path gaps, unproved concurrent seal/durability and incomplete cleanup authority. |
| 17 database | **N/A to A+B delta**: no SQL/migration; C_P/C_N/C_D/C_R not implemented or accepted here. |
| 18 resource bounds | **PASS for existing bounded capture limits; NOT_VERIFIED for production runtime**. No new quotas inferred. |
| 19 retrieval quality, 20 OCR/fidelity, 23 benchmarks, 24 held-out evaluation | **N/A to this correction execution**: unchanged mandatory Phase 18 gates remain open, not runtime PASS. |
| 21 security tests, 22 test quality | **FINDING**: actual actor/interval/assertion/positive-control and integration omissions described above. |
| 25 dependencies | **PASS preservation only**: no dependency/runtime changes; restricted pinned-runtime compatibility NOT_VERIFIED. |
| 26 tooling | **PASS for four bounded receipt/count packages; FINDING for their security interpretation**. No independent execution asserted. |
| 27 observability | **FINDING**: failed branch/native cause/token/descriptor outcomes missing; no sensitive payload dumping requested. |
| 28 documentation/governance | **FINDING**: inaccurate success status and mandatory STOP noncompliance. |
| 29 hygiene/delivery | **PASS review preservation; limited historical raw preservation**: extensive dirty tree retained, no publishing; four unsuffixed report replacements disclosed. |

No new issue is raised against credited DOMAIN publication, VECTOR/JPA, source-first union, generation purge, parser geometry or chunking. ADR-0022 remains binding, and missing later-package features are not counted as A+B defects.

## 14. Exact finite next owner action

**Do not accept Checkpoint 1 or authorize C–H. Maintain STOP.** There is not enough evidence to select a new account, broaden the restricting-SID policy, modify system ACLs or choose another sandbox.

Recommended single next direction: **one owner-authorized, nonprivileged disposable CP18-AB-2 startup-failure-localization scope only**, before any further A+B correction. Its purpose is to distinguish an incomplete test configuration from an actual mandatory resource incompatibility, not to retry arbitrary policies.

Exact proposed authorization:

> I acknowledge Checkpoint 1 CHANGES_REQUESTED and the mandatory STOP. I authorize one disposable, nonprivileged diagnostic of the already selected unique-restricting-SID startup policy only. It must record checked token construction, the exact process-creation branch and native errors, caller/candidate and available suspended-child identity, the actual requested application/desktop/runtime context, and evidence locating any denied mandatory object, requested rights, effective descriptor and access-check constraint. A DLL-specific claim requires native/loader attribution. It must make no production/test/contract change, no broad restricting-principal fallback, no Windows-system ACL change, no elevation/account/service provisioning and no alternative architecture selection. Use one bounded receipt and preserve the source and raw evidence. STOP after that one result or the first unsafe/unavailable prerequisite. This does not authorize further A+B remediation, C–H or ingestion.

A minimal diagnostic may need a disposable corrected harness to expose the currently unrecorded branches and valid token/desktop context. This review neither creates nor executes it. Where initialization prevents child-side evidence, record that limit rather than declaring attestation successful. If a necessary object genuinely cannot be accessed within the selected policy, the result must identify that exact conflict for an owner decision; do not infer it from the current test name.

Following that finite evidence, the owner can separately decide whether to authorize a single bounded A+B correction covering the existing CP18-AB-1 and CP18-AB-3–7 obligations, or hold the work. There is no automatic correction loop, new architecture approval, or reset of the historical 2/2 exhausted Antigravity remediation record.

## 15. Remaining phase gates and review preservation

- **CP18-AB-1–7 OPEN; CP18-AB-8 RESOLVED.**
- **G1/G3 unaccepted; G2/G4 OPEN / NOT_VERIFIED.**
- **FR18-1–11 OPEN**, including Critical FR18-3 actual producer/staging/canonical-output verification and trusted physical publication.
- Packages **C–H remain unauthorized** by this review; no later package is accepted.
- Snapshot ingestion remains **INACTIVE**. Live-original ingestion remains **DISABLED / DEFERRED**.
- All unchanged format, language, OCR/table/locator, chunking, offline/native GPU/fallback, separate RAM/VRAM, operational-resource and fresh-held-out requirements remain mandatory.
- P18-FINAL remains mandatory and **NOT ACCEPTED**; Phase 18 is incomplete and not READY FOR OWNER COMMIT.

Only this additive review document was created. No production, Python, tests, fixtures, migrations, dependencies, canonical contracts, ACTIVE, historical reviews, handbacks or original receipts were modified by the reviewer. Review-entry source/evidence inventories were retained for the final preservation comparison. No process/token/ACL/native experiment, test suite, SQL experiment, ingestion, commit, push, tag or PR action occurred.

**STOP — independent review complete; no implementation authorization issued.**


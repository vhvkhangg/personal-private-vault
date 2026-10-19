# Phase 18 — CP18-AB-2 startup localization independent review

Date: 2026-10-19  
Mode: independent, read-only source/evidence assessment; only this additive review is created.  
Verdict: **POLICY_ACCESS_INCOMPATIBILITY_SUPPORTED / EXACT_CAUSE_UNVERIFIED**

## 1. Independent conclusion

The retained evidence newly distinguishes **successful process creation followed by termination** from CreateProcessAsUserW failure. It records the candidate primary token and the actual suspended child's single restricting SID, successful Job Object assignment, ResumeThread returning 1, a signaled process, and exit status 0xC0000022.

The six recorded candidate AccessCheck evaluations are **successful API invocations returning AccessStatus false**, not API invocation errors. Four captured file DACLs and corresponding caller-positive/candidate-negative checks provide credible, bounded evidence that the selected synthetic-SID-only candidate lacks the requested access under those descriptors. That is enough to support the policy-access-incompatibility verdict.

It does **not** establish the specific object, access request or function that caused termination. No native loader trace, attributable stack or equivalent event was retained. In particular, **ntdll!LdrpInitializeProcess is NOT_VERIFIED**, as is the claim that every required Windows object fails or that CPython must fail identically.

Two advertised proofs are rejected:

- The explicit-ACE controlled fixture did **not** pass: both caller and candidate AccessCheck returned denial, and the captured file DACL contains no ACE for the synthetic SID.
- The apparent successful impersonated file opens are unreliable: a pointer-width INVALID_HANDLE_VALUE is compared to Python -1, and impersonation results are unchecked.

The current candidate cannot responsibly proceed as a functioning worker. This is a limit of the tested policy/configuration, not universal Windows restricted-token infeasibility, proof that an account/elevation is necessary, or permission to add broader SIDs.

**CP18-AB-1–7 remain OPEN; CP18-AB-8 remains RESOLVED; G1/G3 remain unaccepted.** The previous CP18-AB-GOV-1 STOP violation is preserved. This diagnostic does not resolve worker isolation or authorize further A+B correction.

## 2. Scope, binding sources and preservation baseline

Applied the repository independent-review workflow: codex-final-review, codex-review-coverage, local-rag-engineering and Python/native evidence review guidance. Explicit owner instructions limit this invocation to documentation-only assessment; workflow defaults to edit ACTIVE, rerun verification or provide a commit message do not apply.

Read the new source and handback in full and inspected the complete raw JSON/receipt. Reused unchanged previously read repository instructions, contracts and focused reviews after byte-hash comparison:

- [ADR-0022](../../../adr/0022-phase-18-immutable-snapshot-whole-file-consent-no-change.md), exact SHA-256 **fa4ce5681093cc758f4a2cea6ee5c8ce486e3fc5aeae31ba346edfafde4cb25d**.
- [Corrective integration proof handoff](../adr-0022-ab-corrective-integration-proof-handoff-2026-10-19.md).
- [ACTIVE](../../handoffs/ACTIVE.md).
- [Latest independent Checkpoint 1 review](2026-10-19-adr-0022-ab-corrective-integration-checkpoint-1-review.md), SHA-256 **724dd4a2d404321db101ccc62f5bd2f2feec50b9882e24aab2a51ad2943bfde0**.
- [Recovered-source independent review](2026-10-19-cp18-ab2-recovered-source-independent-review.md), retaining its bounded broader-SID feasibility result and limitations.
- Applicable root, backend and documentation AGENTS rules; no changed test or module-local rule was introduced.

Source references below use:

| Key | Actual artifact |
|---|---|
| H | [run_cp18_ab2_startup_localization.py](../../../../scripts/diagnostics/run_cp18_ab2_startup_localization.py) |
| J | [cp18-ab2-startup-localization-output.json](../../../../logs/phase18/cp18-ab2-startup-localization-output.json) |
| R | [receipt-cp18-ab2-startup-localization.json](../../../../logs/phase18/receipt-cp18-ab2-startup-localization.json) |
| HB | [cp18-ab2-startup-failure-localization-handback.md](../cp18-ab2-startup-failure-localization-handback.md) |

Branch **main**, HEAD **435ffea979681f22d723f551dd8b0cd0b733c8cd**, unchanged. The actual review-entry inventory contains **2,136 tracked/nonignored files**. Relative to the prior independent review's final 2,134-file inventory:

- Added H and HB only.
- **Zero modified or deleted inherited files.**
- All production Java/Python, tests, dependencies, migrations, configurations, AGENTS, ADR-0022, ACTIVE and historical reviews remain byte-identical.
- Of the **325 previously hashed logs/phase18 and Surefire artifacts**, all remain byte-identical; only J and R are added, producing 327 artifacts in those evidence trees.

The prior review's finding that four unsuffixed reports had already been overwritten in the earlier integration remains historical fact. This diagnostic did not change them again, overwrite the failed receipts or remove the STOP-gate evidence.

No security experiment, native call, test suite, loader trace, SQL, OCR/GPU work or ingestion was executed by this reviewer. Read-only file/hash/diff checks and a passive existence check of the exact recorded disposable fixture were performed. No accounts, tokens, processes or ACLs were created/changed by the reviewer.

## 3. Source and receipt integrity

| Artifact | Actual bytes | Independently recomputed SHA-256 | Expected result |
|---|---:|---|---|
| H | **40,662** | **8b8c4e69261a6021019173f551b5ed9d40440ce0a2869ec151836ea8b637a410** | Exact match |
| R | **389** | **58c63fefa5d315916f0442b19faa2b43612212428288bc99c90cd8d89b36acd5** | Exact match |
| J | **19,776** | **bf9740ff6e10b531124cb9ff0573b05c3cc84793060c43c04a45c49025e480b5** | Exact match |

HB is 17,781 bytes, SHA-256 **fd46890c033b7f1fa36ff449465acfb53b24616e16d2b2b8b849b5effb955f40**.

R records:

- Executable name python, normal profile, 1,200-second watchdog limit.
- Start **2026-10-19T04:29:36.134Z**, finish **04:29:36.465Z**, measured duration **0.328 seconds**.
- Watched command PID **6468**, wrapper result PASS, exit **0**, cleanup not_needed.
- argv SHA-256 **db739b40ab03b6e915bafb53b22e164f45a5185e9e19ada8bf0d4bf14f7803d1**.

The existing bounded runner at scripts/run-bounded-command.py:79–100 hashes UTF-8 argv joined by NUL and records the launched command's PID. Independently hashing these exact command arguments reproduces R's digest:

    ["python", "scripts/diagnostics/run_cp18_ab2_startup_localization.py"]

J's second-resolution start/finish timestamps agree with R's interval. It identifies the current absolute H path and the same source hash. H:373–380 computes its own file hash and the executable hashes. J reports host Python **3.11.7**, C:\msys64\ucrt64\bin\python.exe, SHA-256 **839de069e5aff6fa4c39118785ecca0a0841e4f536aa35171e180e28b46960ba**, and candidate cmd.exe SHA-256 **15cd209c995b7d3009c91ca40892d5b8f1fb162f3e5f10716642ea278e8e0d7b**.

This materially improves source/command association. It is not independent execution-time attestation: the receipt does not authenticate J, the raw result does not record its parent PID, and both are author-produced files. No external debugger/OS event receipt is supplied. The retained artifacts are mutually consistent for the bounded observations below; wrapper exit 0 is only harness completion.

HB contains stale or inaccurate metadata: §2 reports H as 26,052 bytes instead of 40,662; §9 repeats different obsolete receipt/output hashes, while §2 and the owner's expected values match the actual artifacts. These discrepancies are disclosed, not repaired by rewriting history.

## 4. Native ABI, token and process-call trace

### 4.1 Types and token construction

H:25–261 uses native ctypes structures and pointer-width HANDLE/PSID fields for the reviewed core calls. STARTUPINFOW, PROCESS_INFORMATION, SID_AND_ATTRIBUTES and GENERIC_MAPPING are consistent with the recorded 64-bit Windows host; BOOL/DWORD and output-pointer declarations for OpenProcessToken, CreateRestrictedToken, GetTokenInformation, DuplicateToken, CreateProcessAsUserW, Job assignment, resume, wait, exit query and CloseHandle are appropriate.

Not every auxiliary call has an explicit prototype: baseline CreateProcessW and LocalFree lack complete declarations. More importantly, not every BOOL/error result is checked, as detailed below. This is not blanket native-call correctness certification.

The source obtains the caller's primary token at H:398–399. If TOKEN_ALL_ACCESS fails, its fallback result is unchecked. Subsequent TokenInformation calls query user/type/elevation/integrity/groups/privileges, but most final query BOOLs and sizing errors are not retained or asserted. J nevertheless contains substantive decoded caller data rather than only planned field names:

- Primary token, non-elevated.
- **Low integrity, S-1-16-4096**, not Medium.
- 15 groups, with Users/Everyone/Authenticated Users enabled and Administrators deny-only.
- SeChangeNotifyPrivilege enabled and SeIncreaseWorkingSetPrivilege disabled.
- Caller restricting SIDs/TokenHasRestrictions are **not queried**. “Unrestricted caller” is therefore not established.

HB §3's “Low Mandatory Level / Medium active context” wording must not override the raw Low value. The baseline ordinary cmd.exe launch exists in H:480–498, but its return/exit/stdout is not included in J and no retained console artifact was found. HB's baseline result remains an author claim, separate from the recorded caller AccessCheck positive controls.

H:505–520 uses the fixed, synthetic non-account SID:

    S-1-5-21-999999-999999-999999-1001

It is the exact previous failing scenario's SID, **not freshly unique per attempt**. There is one restricting entry, attributes input 0, flags 0, zero disabled groups and zero deleted privileges. No Users, RESTRICTED_CODE, Everyone or Authenticated Users fallback was added. The source does not implement additional least-privilege reductions or change the integrity level.

J:candidate_token records checked construction success, TokenPrimary, TokenHasRestrictions true, restricting count 1 and the actual decoded SID above with attributes 0x7. H:582 passes this candidate primary handle to CreateProcessAsUserW, not the later impersonation duplicate. H:629 opens the actual returned child's process token before resume; J records count 1 and the same decoded SID. This is credible new **parent-side suspended-child token evidence**, not child self-attestation or complete child user/groups/privileges/IL attestation.

Microsoft documents that CreateRestrictedToken preserves token type, supplies an additional restricting-SID access check and permits a restricted copy of the caller's primary token without SE_ASSIGNPRIMARYTOKEN_NAME. The successful recorded creation is stronger evidence here than an argument that a new account must be provisioned. [CreateRestrictedToken](https://learn.microsoft.com/en-us/windows/win32/api/securitybaseapi/nf-securitybaseapi-createrestrictedtoken).

### 4.2 Ordered process results

| Order / H lines | Actual operation | Retained result / independent credit |
|---|---|---|
| 398–477 | Caller token and introspection | Decoded data above; fallback/query error coverage incomplete. |
| 513–557 | Candidate creation and introspection | Success, primary token, one decoded synthetic restricting SID. |
| 568–576 | Pipe and startup setup | CreatePipe/SetHandleInformation return values unchecked; STARTF_USESTDHANDLES, stdout/stderr write pipe, stdin NULL, lpDesktop NULL. |
| 582–595 | CreateProcessAsUserW | **True**, branch CREATE_PROCESS_SUCCEEDED, PID **8756**, TID **18636**, flags **0x4 CREATE_SUSPENDED**. |
| 629–645 | Suspended-child token | OpenProcessToken true; same one decoded restricting SID. Individual TokenInformation BOOLs not recorded. |
| 655–660 | CreateJobObjectW, AssignProcessToJobObject | Assignment **true**, before resume; separate CreateJobObject return/handle detail not retained. |
| 663–665 | ResumeThread | **1**, consistent with the initial suspended thread becoming runnable. |
| 668–669 | WaitForSingleObject, 10-second bound | **0 / WAIT_OBJECT_0**, signaled process. |
| 672–677 | GetExitCodeProcess | DWORD **3221225506 / 0xC0000022**. Query BOOL/error not retained, but nonzero status and signaled-process evidence support the recorded post-creation exit. |
| 682–688 | Pipe ReadFile | Empty child_stdout; ReadFile BOOL, error and actual bytes_read not retained. Not proof that the entry point was never reached. |
| 691–695,907–910 | Close handles | Close calls exist; their results are unchecked. No positive release receipt. |

This fixes the earlier test's branch ambiguity: the record supports **creation succeeded**, rather than accepting error 5/1314 as an alternative passing branch. Success-side last_error fields are set to 0 by the harness rather than obtained as meaningful independent success-state measurements.

The process command is:

    "C:\Windows\System32\cmd.exe" /c echo candidate_test

It is not CPython 3.14.4, a ppv_rag import/readiness test, model/library initialization, approved-input transfer, SQL staging or DOMAIN compatibility proof. Its inherited environment/cwd and lpDesktop NULL differ from the required eventual isolated production setup. bInheritHandles is true for the pipe and has no explicit handle allowlist; no general inherited-handle boundary is proved.

CreateProcessAsUserW returns before complete process initialization. Later DLL initialization failure can terminate a created process, but that contract does not map one exit code to a specific module/function. Its standard handles must also be valid when supplied. The diagnostic's unverified stdin/pipe setup and ambient desktop are additional unlocalized context, not causes proved by this review. [CreateProcessAsUserW](https://learn.microsoft.com/en-us/windows/win32/api/processthreadsapi/nf-processthreadsapi-createprocessasuserw).

### 4.3 Supervision and release limits

Job assignment-before-resume is newly credited. No SetInformationJobObject/KILL_ON_JOB_CLOSE configuration, descendant scenario, breakaway test or timeout termination/reap path exists in H. The successful observed wait concerns this one child. Closing a default Job Object is not a tree-termination proof.

There is no encompassing finally block. Token query, resume, exit query, impersonation/revert and CloseHandle failures can escape checked reporting; the allocated synthetic SID is not freed. In the observed retained branch the process was signaled and close calls were reached, but positive native release and robust exceptional cleanup remain NOT_VERIFIED.

## 5. AccessCheck audit

### 5.1 Correctly supported core mechanism

H:703–710 calls DuplicateToken with SecurityImpersonation (2) for both caller and candidate, then uses those **impersonation tokens** at H:768–770. That is the right token category for AccessCheck. The thread need not itself be impersonating merely to evaluate a supplied client token. Although duplication BOOLs/type readbacks are not captured, the recorded successful AccessCheck API invocations corroborate valid accepted client-token inputs for those calls. [DuplicateToken](https://learn.microsoft.com/en-us/windows/win32/api/securitybaseapi/nf-securitybaseapi-duplicatetoken).

H:750–759 obtains file security descriptors with GetNamedSecurityInfoW(SE_FILE_OBJECT, OWNER|GROUP|DACL), checks its DWORD return, retains the allocation through both checks and frees it afterward. GetAce parses ordinary allow/deny ACE trustee/mask values. The observed four file lists contain six ordinary allow ACEs each; no unsupported ACE interpretation is required for that bounded inventory.

H:339–365 records the two distinct AccessCheck outputs:

- Function BOOL: whether evaluation executed successfully.
- AccessStatus: whether the supplied descriptor grants the requested rights.

For **all six system-resource checks**, J records function BOOL true, candidate AccessStatus false and GrantedAccess 0; the corresponding caller checks record true/true and the requested mask. These are actual evaluated denials, not the ERROR_NO_IMPERSONATION_TOKEN or invalid-handle API failures the review was required to distinguish.

AccessCheck requires a queryable impersonation token and a valid owner/group-containing descriptor. Its function result is distinct from AccessStatus; it is a descriptor evaluation, not a loader trace or audit. [AccessCheck](https://learn.microsoft.com/en-us/windows/win32/api/securitybaseapi/nf-securitybaseapi-accesscheck).

### 5.2 Masks, mappings and errors

The desired masks passed to AccessCheck are already object-specific:

- cmd.exe: **0x001200A0**, FILE_GENERIC_EXECUTE.
- Three DLL files: **0x001200A9**, FILE_GENERIC_READ | FILE_GENERIC_EXECUTE.
- Current window station: **0x0001**, WINSTA_ENUMDESKTOPS.
- Current desktop: **0x0001**, DESKTOP_READOBJECTS.
- Controlled fixture: **0x00120089**, FILE_GENERIC_READ.

They contain no GENERIC_READ/WRITE/EXECUTE/ALL high bits. Absence of a MapGenericMask call therefore does not invalidate these already-expanded requested masks. The file GENERIC_MAPPING constants correspond to file rights. The window/desktop mappings are abbreviated and are not evidence for full generic access; the actual requested bit 1 is the narrow evaluated case. [MapGenericMask semantics](https://learn.microsoft.com/en-us/windows/win32/api/securitybaseapi/nf-securitybaseapi-mapgenericmask).

The privilege buffer is a one-entry PRIVILEGE_SET with its size supplied. There is no required-size retry or retained privilege output. The recorded successful function BOOLs show that this did not produce an API sizing failure in the measured cases; it remains inadequate general error handling.

The helper's aggregate denied value is **(not API_OK) OR (not AccessStatus)**. That is unsafe for interpreting arbitrary/future results: an API failure would be mislabelled denial. It does not invalidate these specific six rows, whose separate API_OK values are true. The helper writes last_error = 0 whenever API_OK is true, even when AccessStatus is false; this zero is synthesized and does not identify the denial reason.

### 5.3 Effective resource inventory and what the two passes show

| Object actually queried | Type and requested rights | Descriptor / caller / candidate evidence |
|---|---|---|
| C:\Windows\System32\cmd.exe | File, 0x001200A0 | Six captured allow ACEs below. Caller granted 0x001200A0; candidate denied with API_OK true. |
| C:\Windows\System32\ntdll.dll | File, 0x001200A9 | Same captured trustee/mask pattern. Caller granted; candidate denied with API_OK true. |
| C:\Windows\System32\kernel32.dll | File, 0x001200A9 | Same bounded evidence. |
| C:\Windows\System32\kernelbase.dll | File, 0x001200A9 | Same bounded evidence. |
| GetProcessWindowStation() result | Window-station handle, 0x0001 | Caller granted/candidate denied. Actual object name, owner, ACEs, SDDL and inheritance not retained. Cannot certify name WinSta0. |
| GetThreadDesktop(current thread ID) result | Desktop handle, 0x0001 | Caller granted/candidate denied. Actual name/descriptor details not retained. Cannot certify name Default. |

For each of the four files, the captured owner is TrustedInstaller; the allow entries are:

| Trustee | Mask |
|---|---|
| TrustedInstaller service SID | 0x001F01FF |
| BUILTIN Administrators, S-1-5-32-544 | 0x001200A9 |
| SYSTEM, S-1-5-18 | 0x001200A9 |
| BUILTIN Users, S-1-5-32-545 | 0x001200A9 |
| ALL APPLICATION PACKAGES, S-1-15-2-1 | 0x001200A9 |
| ALL RESTRICTED APPLICATION PACKAGES, S-1-15-2-2 | 0x001200A9 |

No synthetic candidate SID appears. The caller's enabled Users group matches an allow entry and its positive descriptor check succeeds. CreateRestrictedToken receives no disabled-group/deleted-privilege lists, and the candidate has only the synthetic restricting entry. The measured contrast and descriptor list are consistent with failure of the restricting-SID check. **AccessCheck does not separately report each internal pass**; this diagnosis is an inference from the inputs/controls, not two independently logged internal decisions. The caller's own restricting list and full candidate/duplicate attributes were not retained.

The narrow file-DACL incompatibility is credible without claiming every Windows object has these ACLs. ACE flags, DACL protection/inheritance control, full serialized descriptors and mandatory integrity labels/SACLs were not saved. The station/desktop descriptor was obtained in memory but its trustee list was never retained; HB's claimed names and trustee lists are unverified. No known-DLL section, CSRSS/ALPC endpoint, NLS resource, actual loader file request or process-private desktop was queried.

An evaluated descriptor denial for a chosen mask is not proof the child loader requested that same mask on that same file after resume. Filesystem traversal, mappings/sections, inherited handles, sharing and other object types have separate behavior. Neither “all System32 binaries deny” nor “all required initialization resources were checked” follows.

## 6. Rejected corroboration and diagnostic findings

### SLR-1 — High: exact loader cause and universal policy claims exceed evidence

Evidence: H:883–902; J:600–606; HB §§5–8,13.

No loader trace, stack, ETW event, debugger observation or attributable denied native operation exists in the retained artifacts. H assigns STARTUP_FAILURE_LOCALIZED to any created-child exit, including non-access-denied values, and to direct creation failures; the label does not require a localized resource or validated control. The string ntdll!LdrpInitializeProcess appears as explanation in HB, not a captured call stack.

Newly supported: created child, recorded termination, bounded descriptor incompatibility. Unsupported: exact loader function; first failing DLL/object/right; that the cmd entry point was never reached; universal system ACL patterns; that CPython must fail for an identical cause.

Minimal disposition: retain the exact-cause NOT_VERIFIED limitation in the owner decision. Do not treat the narrative as native proof or authorize broader SIDs from it. No new tracing experiment is requested or run by this review.

### SLR-2 — High: claimed explicit-ACE positive control actually failed

Evidence: H:854–867; J:527–598; HB §6 controlled-positive-control claim.

H creates a file, invokes an icacls grant through shell=True, ignores return code/stdout/stderr, then probes the file. J records **both caller and candidate denied**, API_OK true, GrantedAccess 0. Its six file ACEs contain no synthetic restricting SID. Whether a parent grant was attempted successfully is unverified; the tested child's required ACE is absent.

Thus this is not evidence that a unique-SID allow ACE passed. It also leaves the caller-controlled-fixture negative unexplained. Do not assume the caller was unrestricted, or silently repair the DACL and relabel this original run successful.

Minimal disposition: reject this corroboration and retain the actual negative result. Any later owner-authorized fixture proof must check the grant command, effective child descriptor and both positive controls; that execution is not authorized here. The separate six system-resource caller positives/candidate negatives remain independently creditable.

### SLR-3 — Medium: direct-open results cannot demonstrate either grant or denial

Evidence: H:257–261,773–799; J's five candidate_direct_open_impersonated rows.

CreateFileW is declared to return wintypes.HANDLE, a pointer-width value. On 64-bit ctypes, INVALID_HANDLE_VALUE is the all-ones pointer value, **0xFFFFFFFFFFFFFFFF**, not Python integer -1. H compares direct_h only to -1, so an invalid return can be reported as handle_valid true, last_error 0 and denied false, and passed to CloseHandle. No raw handle value is retained. ImpersonateLoggedOnUser and RevertToSelf results are also unchecked, so the effective thread identity is unproved.

The apparent file-open successes therefore cannot contradict the valid AccessCheck denials, prove a bypass, or corroborate actual restricted-resource access. The requested real open also always uses GENERIC_READ|GENERIC_EXECUTE, differing from the cmd-only execute or fixture-read descriptor checks.

CreateFileW's failure sentinel and ctypes pointer-return behavior require pointer-width handling. [CreateFileW](https://learn.microsoft.com/en-us/windows/win32/api/fileapi/nf-fileapi-createfilew), [Python ctypes](https://docs.python.org/3.11/library/ctypes.html#ctypes.c_void_p).

Minimal future proof, only if separately authorized: checked impersonation/thread-token attribution, width-correct sentinel, actual handle/error, matching requested rights and verified revert/close. No active reproduction occurred.

### SLR-4 — Medium: disposable cleanup success is false; exceptional release remains unverified

Evidence: H imports at 9–18 and cleanup at 869–873; HB §11.

The source calls shutil.rmtree but never imports shutil. The resulting NameError is caught and ignored; this script does not delete its controlled directory on that path. A passive review-time Test-Path of J's exact test.txt path returned **true**, contradicting “deleted immediately.” The reviewer did not read its content, change its ACL or delete it.

Close/revert failures are unchecked and there is no finally-based ownership cleanup or kill-on-close job configuration. No leak/escape is inferred beyond the observed retained fixture and source defects; positive native release of every handle is not proved.

Minimal owner-controlled follow-up: separately account for and safely clean only this exact owned fixture, with identity/containment and factual completion. Do not modify this source or delete historical artifacts to manufacture clean evidence. No cleanup execution is authorized by this review.

### SLR-5 — Low: evidence metadata and baseline claims need qualification

HB §2 gives the wrong harness byte length; §9 repeats stale receipt/JSON hashes; §3 ambiguously calls recorded Low integrity “Medium active context.” The ordinary baseline launch outcome/console is not retained in J. Full station/desktop descriptor and names claimed in the table are missing. These are reporting discrepancies, not proof that the matching primary artifacts were fabricated.

The independent inventory/hashes and bounded source facts in this review take precedence for adjudication. Preserve HB unchanged; any later owner-authorized correction must be additive and factual.

## 7. Loader propositions separately adjudicated

| Proposition | Independent assessment |
|---|---|
| **A. Process creation succeeded** | **SUPPORTED** by the explicit branch, returned PID/TID, suspended-child token and resume/wait sequence. Distinct from runtime initialization. |
| **B. Child terminated before useful application execution** | **Post-creation termination SUPPORTED.** “Before cmd entry/useful code” remains **NOT_VERIFIED**: output/read status is incomplete and no entry/loader trace exists. The requested echo did not demonstrably complete. |
| **C. Windows resource ACLs appear incompatible with the unique-SID policy** | **SUPPORTED within measured descriptors/masks**: four captured file DACLs and six successful negative AccessCheck evaluations with caller positives. Not a universal Windows result. |
| **D. A specific denied mandatory resource is demonstrated** | **Specific descriptor/right denials SUPPORTED** for the four files. Their necessity as the actual post-resume request that caused this exit is **NOT_VERIFIED**. Station/desktop exact identities/descriptors are incomplete. |
| **E. A specific loader function/object caused termination** | **NOT_VERIFIED**. No attribution to ntdll!LdrpInitializeProcess, a particular DLL/section or other first failing object is established. |

The report therefore cannot receive STARTUP_CAUSE_VERIFIED. The appropriate verdict is **POLICY_ACCESS_INCOMPATIBILITY_SUPPORTED / EXACT_CAUSE_UNVERIFIED**, rather than dismissing valid API_OK/AccessStatus evidence because other diagnostic branches are defective. No complete production security boundary is accepted.

## 8. Bounded architecture implications and owner alternatives

ADR-0022 N2/N3/WF-4 and its managed-store contract require approved-input-only access, protected ORIGINAL_BYTES/temp, no arbitrary original/store directory grants and effective principal/handle/ACL enforcement. Failed startup satisfies none of those functioning-worker obligations.

The current candidate and unchanged system descriptors have a demonstrated access incompatibility for measured masks. It must remain stopped unless a separately reviewed solution addresses necessary runtime access **and** confinement. The exact failed startup cause is unresolved, so this is not proof that every possible unique-SID mechanism, restricted token or nonprivileged design is impossible.

The earlier broader-SID Python diagnostic established only a limited route to launch. Adding Users/Everyone/Authenticated Users/RESTRICTED_CODE is not a harmless runtime exception: each can satisfy restricting checks on other objects whose DACLs grant it. If the ordinary SID pass and traversal/other constraints also allow a known-path read, an unselected local file can become accessible. Which paths are actually reachable requires the effective deployment descriptors; not every private file is automatically exposed. Low integrity alone does not supply an approved-input read allowlist.

A narrower candidate would have to distinguish the required, read-only OS/interpreter/DLL/model closure from private store/temp, unrelated data and approved input/scratch, while proving both token access checks, parent/handle/desktop isolation and existing Job Object/protocol compatibility. A private-store DACL alone does not confine all other user files. Windows object-level access mechanisms could be evaluated for that distinction, but **no concrete alternative policy accomplishing it is independently established here**.

| Owner alternative | Evidence, trade-off and authorization boundary |
|---|---|
| **1. Keep the selected policy and STOP.** | Supported immediate disposition. Retains confinement requirements and avoids unsupported runtime/security claims. The current candidate is not accepted and no work resumes automatically. |
| **2. Separately authorize one documentation-only alternative token/access-control design comparison.** | Reasonable next decision if continued work is desired. Existing APIs and earlier limited broad-SID launch justify analysis, not a selected replacement or feasibility certification. Must specify precise runtime/store/unselected/input rights and unresolved proof; no SID broadening or implementation follows from this review. |
| **3. Consider a wider architecture/provisioning amendment only on independently supported necessity.** | **Not presently justified.** This run does not establish a need for permanent account, elevation, service, system ACL changes or AppContainer. Such changes need a separate concrete proposal and explicit owner approval if ever shown necessary. |

No alternative is selected on the owner's behalf. D1–D11, N1–N10 and WF-1–WF-8 remain unchanged. There is no permission to modify system descriptors or add a broad restricting principal to make startup pass.

## 9. One precise next owner-controlled action

**Keep STOP and decide whether to authorize option 2, one documentation-only security-policy comparison.** No further native retry or remediation is recommended automatically. The valid descriptor evidence already supports an informed decision to stop or commission a bounded design; exact internal loader attribution must remain unverified.

Proposed finite authorization, if the owner wants continued design work:

> I retain A+B STOP and accept only the bounded incompatibility observations, with the exact startup cause unverified. I authorize one documentation-only comparison of a narrowly specified token/access-control candidate under unchanged ADR-0022. It must identify the supported Windows mechanism, exact resource/principal/right matrix for runtime, store/temp, unselected files, approved input/scratch, parent/token/handles and desktop, and the minimum later compatibility/security proof. It must disclose unsupported feasibility and stop with a concrete conflict if no credible in-scope path is identified. It may not select or activate a new policy, modify code/tests/contracts/ACLs, execute an experiment, provision an account/service, elevate, implement A–H or enable ingestion. Return one reviewable proposal; execution requires a separate owner decision.

The retained disposable fixture is an outstanding cleanup item for its owner, not authority for this reviewer to delete outside the repository. Any later cleanup must be explicitly bounded to the actual owned diagnostic objects and report genuine completion.

## 10. Proportional coverage and remaining gates

The repository's shared review questions were classified only for this diagnostic delta:

| Dimensions | Result and reason |
|---|---|
| 1 intent, 2 traceability, 3 architecture | **FINDING**: bounded observations supported; cause/control/production conclusions overclaimed. ADR-0022 unchanged. |
| 4 module ownership, 6 shared contracts, 7 SOLID, 9 patterns, 11 public API, 17 database | **N/A**: no production module/interface/API/schema change; no general audit reopened. |
| 5 tree/hygiene placement | **PASS for inventory**: one retained diagnostic source and handback; no hidden new production security helper. |
| 8 invariants, 10 states, 12 native input/error validation | **FINDING**: aggregate denial, pointer sentinel, unchecked calls and classifier limitations. |
| 13 authorization, 21 adversarial security evidence, 22 test quality | **PASS for bounded successful AccessCheck evaluations; FINDING for fixture/direct-open/attribution claims; production isolation NOT_VERIFIED.** |
| 14 retention, 16 recovery | **FINDING**: failed temp cleanup and incomplete handle/process exceptional release. |
| 15 transaction/concurrency | **N/A to PostgreSQL; NOT_VERIFIED for production lifecycle**: no C_N/C_D/C_R/C_P or integrated worker proof. |
| 18 resource bounds, 23 benchmarks | **PASS for receipt time bound only; N/A to performance acceptance**: no production RAM/VRAM/CPU/disk claim accepted. |
| 19 retrieval, 20 OCR/fidelity, 24 held-out science | **N/A to this delta**: original Phase 18 requirements remain mandatory. |
| 25 dependencies/runtime | **PASS preservation; NOT_VERIFIED production compatibility**: MSYS host/cmd diagnostic, not pinned CPython worker. |
| 26 tooling, 27 observability | **PASS source/hash/argv association; FINDING missing outcome/error/trace and false-positive fields.** |
| 28 governance/docs | **FINDING**: overclaimed localization/control and stale metadata; prior STOP violation preserved. |
| 29 delivery preservation | **PASS inherited repository/evidence preservation; FINDING disposable artifact cleanup.** |

Final retained state:

- **CP18-AB-1–7 OPEN**, CP18-AB-8 **RESOLVED**.
- **CP18-AB-GOV-1 remains recorded**; this diagnostic does not erase the prior mandatory STOP violation.
- **G1/G3 unaccepted; G2/G4 OPEN / NOT_VERIFIED**.
- **FR18-1–11 OPEN**, including Critical FR18-3.
- **C–H not authorized**; no automatic A+B continuation.
- Snapshot ingestion **INACTIVE**; live-original ingestion **DISABLED / DEFERRED**.
- P18-FINAL **NOT ACCEPTED**; Phase 18 incomplete and not READY FOR OWNER COMMIT.
- Historical 2/2 exhausted Antigravity remediation record unchanged.

Only this additive review document was created. Review-entry files and evidence are preserved; no production, tests, configurations, ACLs, contracts, ACTIVE, prior reviews, handbacks or receipts were edited. No new process/token/native/security experiment, build, test, ingestion, commit, push, tag or PR action occurred.

**STOP — independent review complete; no policy or implementation authorization issued.**


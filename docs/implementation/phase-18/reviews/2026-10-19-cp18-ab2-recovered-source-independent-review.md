# Phase 18 — CP18-AB-2 recovered-source independent review

- **Date:** 2026-10-19.
- **Independent verdict:** **FEASIBILITY_SUPPORTED_WITH_LIMITS**.
- **Scope:** one additive, read-only review of the recovered original diagnostic and unchanged retained evidence. **No diagnostic rerun.**
- **Acceptance boundary:** core restricted-own-token launch and private-fixture access separation; **not production isolation, checkpoint 1 acceptance or implementation authorization**.

The recovered 38,818-byte source exactly matches the previously supplied SHA-256. The receipt's argv digest matches `python` plus that exact persistent-workspace pathname. Inspecting the actual code now substantiates a genuine restricting-SID token passed to CreateProcessAsUserW, trusted-side fixture positive controls, restricted-child read/write denials and useful permitted operations. This supports the core nonprivileged route without demonstrating a need for another account.

The missing-source reason for the previous ADDITIONAL_EVIDENCE_REQUIRED verdict is addressed. Important residual limits remain: no complete child-token attestation or effective SDDL was captured; the token-access test deliberately passed NULL and incorrectly counted error 6 as a denial; the process-open tests targeted a disposable same-user process, not Java; broad SIDs, default-desktop exposure, handle inheritance, actual runtime/transport and lifecycle security are not production-proved. These limits remain mandatory integration obligations.

## 1. Authority and preserved baseline

Binding [ADR-0022](../../../adr/0022-phase-18-immutable-snapshot-whole-file-consent-no-change.md), [ACTIVE](../../handoffs/ACTIVE.md), the [previous feasibility review](2026-10-19-cp18-ab2-restricted-token-feasibility-independent-review.md) and [A+B corrective independent review](2026-10-19-adr-0022-slice-1-ab-corrective-independent-review.md) were checked against the actual tree. Root/backend/documentation AGENTS and relevant review instructions remain unchanged; their previously read applicable rules and the repository independent review workflow are reused. The owner's express supplemental-review scope grants no implementation or experiment authority.

Branch remains `main`, HEAD `435ffea979681f22d723f551dd8b0cd0b733c8cd`. All **2,129 inherited tracked/nonignored files** match the preceding independently observed inventory plus the preceding feasibility review. The **302 existing receipt/Surefire artifacts** also match. No canonical contract, ACTIVE, production, test, dependency, migration or historical review was changed.

This supplement changes the current feasibility adjudication based on newly accessible evidence. It does not rewrite the accurate historical statement that the source was unavailable to the previous workspace review. ADR-0022/D1–D11/N1–N10/WF-1–WF-8 remain unchanged; live-original protection through indexing COMMIT is not reintroduced.

## 2. Recovered source, exact hash and receipt association

**S** denotes the actual recovered file:

`C:\Users\VU KHANG\.gemini\antigravity-cli\brain\87326cb0-e93a-4306-83cb-1541bf3c3425\scratch\run_cp18_ab2_diagnostic.py`

All **894 lines** were read. It was not imported, executed, copied into the repository, modified or deleted.

| Artifact | Verified value |
| --- | --- |
| Recovered source byte length | **38,818 bytes** |
| Recovered source SHA-256 | **`ae2a057bfc1bb2378bf07c3e32db733e2e9568614b057a5bf598bb04f1e70f93` — exact expected match** |
| Receipt R | `logs/phase18/cp18-ab2-diagnostic-receipt.json`, 390 bytes; SHA-256 `652e0540f6c8cae57de6af255d88d03e79cb5bfe47545d12448a39af3f98869b` |
| Evidence E | `logs/phase18/restricted-token-feasibility-2026-10-19.json`, 7,579 bytes; SHA-256 `29a7e8a88a7e3ddc52ca2fb9263f0e2252e21ac89444cde49e8e19cdece9ed62` |
| Previous independent feasibility review | SHA-256 `3784da21212104a53ff1ac762e887742436f6d7d3a973139cb6d8d21d165532f`, unchanged |
| Corrective independent review | SHA-256 `48f2e1cc56384db5bd0e4c9f28c68dfb3de61ea15b4420d120d7aca995ebf677`, unchanged |

The existing bounded runner hashes UTF-8 NUL-joined argv. Independently hashing these two exact arguments:

1. `python`
2. the full recovered Windows pathname above, with backslashes

produces **`3698b8ef190f0f81e4f460932eb51996e1be1c2a8578f808b9c57f71c0baa4bb`**, exactly R's argv digest. This resolves the previously unknown invocation-path association. It also explains why searching the repository did not locate the harness.

R records one supplied bounded invocation, PID 24792, `02:04:28.431`–`02:04:29.367 UTC`, 0.938 seconds, exit 0. E's timestamp/duration, child PID 11324 and emitted result layout agree with S's generation path. No contradictory source/output mapping was found.

**Historical integrity limit:** the argv digest binds argument text, not execution-time file bytes or the interpreter binary. The present matching source hash is strong consistency evidence with the previously supplied source fingerprint; it is not cryptographic execution attestation. No preserved console transcript, execution-time source-hash receipt, exact interpreter hash, generated child-script copy or authenticated result channel is supplied. No stronger historical claim is made.

## 3. Native declarations and error handling

### Core declarations support the observed mechanism

S:23–37, 61–78 and 96–105 declare pointer-sized process/token handles and output handle pointers for GetCurrentProcess, OpenProcessToken, SID conversion, GetTokenInformation, CreateRestrictedToken, CreateProcessAsUserW and OpenProcess. SID_AND_ATTRIBUTES stores a pointer plus DWORD; STARTUPINFOW and PROCESS_INFORMATION use pointer-sized fields; Job Object structures use pointer-sized SIZE_T and 64-bit counters (S:80–143). The core token/process signatures are consistent with documented Win32 parameter roles on the reported Win64 platform.

This is a static declaration review, not a new measured ABI experiment or proof that every ctypes call is fully declared. Several ancillary calls lack complete prototypes, including CloseHandle, ResumeThread, WaitForSingleObject, GetExitCodeProcess and some pipe/read/attribute operations. Passing an integer HANDLE through ctypes defaults is not a uniformly proved 64-bit interface. Python documents default C-int return/conversion behavior and the role of explicit prototypes. No observed pointer-truncation failure is claimed, but this diagnostic must not be copied as production-grade native plumbing. [ctypes function prototypes](https://docs.python.org/3/library/ctypes.html#specifying-the-required-argument-types-function-prototypes).

### Error handling is diagnostic, not fail-closed production control

- S:321, 327, 368 and 373 do not enforce successful token/query return values before using outputs.
- ACL command exit statuses are ignored; output is discarded.
- Job limit setup, resume/wait/exit queries and CloseHandle outcomes are incompletely checked.
- First-child execution resumes even if assignment failed (S:690–699); the final classifier does not include successful first assignment as a required predicate (S:809–817).
- Python negative checks label **any exception** DENIED_OK; native checks generally label any failed open/return as denied rather than require the expected error.
- S:893–894 simply calls main. Even a PARTIALLY_FEASIBLE result does not deliberately set a nonzero exit. Wrapper exit 0 is not an independent security PASS.
- The invalid-handle token row is included in the aggregate parent PASS.

These are known limitations of the harness and its aggregate score, not evidence that the retained successful branch did not run. The independent feasibility verdict below uses the specific code/controls/results rather than the harness's aggregate classification.

## 4. Exact token construction and attribution

S:319–361 obtains the caller's process token and creates hRestricted:

| Parameter / operation | Actual source |
| --- | --- |
| Original token | OpenProcessToken(GetCurrentProcess(), `0xF01FF`, &hToken) |
| CreateRestrictedToken flags | **0** |
| Disabled SIDs | **0 / NULL** — no additional deny-only conversion |
| Deleted privileges | **0 / NULL** — no explicit privilege removal |
| Restricting SIDs | **4**: Users `S-1-5-32-545`, Restricted Code `S-1-5-12`, Everyone `S-1-1-0`, Authenticated Users `S-1-5-11` |
| Restricted SID attributes | Zero for each input element |
| Result | Separate output hRestricted |
| Launch token | **That hRestricted** is passed to CreateProcessAsUserW at S:667–677 |

This is a restricting-SID construction, not merely DuplicateToken of an unrestricted token, and it does not select WRITE_RESTRICTED. The documented access model requires both the ordinary and restricting SID checks; CreateRestrictedToken preserves token type. The process-token API path therefore supports a restricted **primary-token** route, although TokenType itself was not queried in this run. [CreateRestrictedToken](https://learn.microsoft.com/en-us/windows/win32/api/securitybaseapi/nf-securitybaseapi-createrestrictedtoken).

### Actual TokenInformation evidence

| Property | What S actually queries / emits | Independent disposition |
| --- | --- | --- |
| TokenUser | S:325–329 queries **hToken**, the parent/caller token; E redacts that SID | Caller identity evidence, **not a child or hRestricted user query** |
| TokenGroups | Not queried | Existing groups/deny-only attributes **NOT_VERIFIED**; no additional disable list was requested |
| TokenHasRestrictions | S:368 queries numeric class **21 on hRestricted** | Now inspectable; E's `is_token_restricted` boolean comes from this DWORD, **not IsTokenRestricted** |
| TokenRestrictedSids | S:371–374 queries numeric class **11 on hRestricted**; reads returned count 4 | Count is queried; E's SID strings are copied from the **input list**, not decoded from the returned SID array |
| TokenPrivileges | S:386–395 queries class **3 on hRestricted** and resolves LUID names | Five names retained; privilege attributes are discarded; no assertion that privileges were removed |
| TokenIntegrityLevel | S:378–382 queries class **25 on hRestricted** | Returned Medium SID is recorded; no child-side integrity query |
| TokenType / elevation / actual child token | Not queried | Explicit runtime type/elevation and complete child-token attestation **NOT_VERIFIED** |

The numeric classes match Microsoft's enumeration; TokenHasRestrictions, TokenRestrictedSids, TokenGroups, privileges and type are distinct evidence. [TOKEN_INFORMATION_CLASS](https://learn.microsoft.com/en-us/windows/win32/api/winnt/ne-winnt-token_information_class).

Thus Antigravity's acknowledgement is confirmed, with an additional precision: most inspection is parent-side hRestricted, while TokenUser specifically comes from the original hToken. **This is not validated child-token introspection.** The code flow to CreateProcessAsUserW, normal/child access difference and retained execution results nevertheless suffice for the narrower feasibility decision.

### Nonprivileged route versus unmeasured elevation

CreateProcessAsUserW receives hRestricted, a quoted mutable command buffer, NULL process/thread descriptors, inherited environment/current directory and CREATE_SUSPENDED (S:665–677). E records first-child exit 0 and actual standard-module/input/scratch work. This supports successful startup rather than merely obtaining a process handle before DLL initialization.

Microsoft explicitly exempts a restricted version of the caller's primary token from SeAssignPrimaryTokenPrivilege. General access/privilege requirements, including the API's discussion of SeIncreaseQuotaPrivilege, must still be handled truthfully in production. [CreateProcessAsUserW](https://learn.microsoft.com/en-us/windows/win32/api/processthreadsapi/nf-processthreadsapi-createprocessasuserw).

S contains no account creation, service installation, elevation, privilege-assignment or host-wide security-policy path. It operates on owned fixtures. E's Medium token and observed successful path are consistent with the nonprivileged route; `separate_account_or_elevation_required=false` is nevertheless **hard-coded** at S:872, not an elevation measurement. Complete parent/child elevation/privilege attributes were never captured. This is not a universal audit of host state. No concrete additional privilege/account prerequisite has been established.

## 5. ACL setup and protected-resource proof

### Exact construction now inspectable

S:224–285 creates fresh A/B/E/F fixtures under `target/cp18_ab2_diagnostic`. S:245–251 invokes:

- A_snapshot_store and B_mutable_capture: `icacls ... /inheritance:r /grant:r "<current account>":(OI)(CI)F`.
- E_input_channel and F_worker_scratch: the same account grant plus `/grant *S-1-5-32-545:(OI)(CI)F`.

Children/files are populated **after** these root ACL commands. The account string comes from USERDOMAIN/USERNAME. Microsoft describes /inheritance:r as removing inherited ACEs and /grant:r as replacing grants for the specified SID; it does not certify removal of every other explicit ACE or set the owner. [icacls](https://learn.microsoft.com/en-us/windows-server/administration/windows-commands/icacls).

**Effective SDDL remains NOT_VERIFIED:** no GetSecurityInfo/SDDL/owner dump, post-command ACL readback or command return-code assertion exists. Directory ownership is not explicitly assigned or queried. No exact parent DELETE_CHILD/inheritance state was retained. The commands' intent is verified; their complete effective security descriptors cannot be reconstructed after fixture removal.

### Trusted-side controls materially strengthen the core evidence

S:294–316 performs actual unrestricted-parent read of C and D, enumeration of A/B, read of E, scratch write/delete and OpenProcess of H. It asserts that **all recorded baseline values equal OK**, so a setup/parent-access error fails this source's normal path. These controls were previously uninspectable and are an important reason the core result is now supportable.

S then emits the restricted child script with the **same absolute fixture paths** (S:421–429). E contains denied accesses to those known paths together with allowed E/F operations. Manifest D is not protected merely by C's read-only attribute. The combined normal-versus-child results provide meaningful support for the intended private-fixture boundary, despite the absent SDDL dump.

### Actual child operations and their exact limits

| Source | Actual operation | Retained evidence / boundary |
| --- | --- | --- |
| S:439–452 | Python open/read C and D | PermissionError/errno 13; actual known-path read denials, not just failed enumeration |
| S:455–466 | os.listdir A and B | WinError 5 |
| S:469–482 | Create/write exploit.txt in A and B | PermissionError; no separate append/truncate of an existing file |
| S:485–503 | os.rename C; os.remove C; shutil.rmtree B | WinError 5. rmtree is not a dedicated FILE_DELETE_CHILD mask test |
| S:506–514 | SetFileAttributesW(C, FILE_ATTRIBUTE_NORMAL) | false/error 5; attribute reset attempt denied |
| S:517–526 | SetNamedSecurityInfoW(A, SE_FILE_OBJECT=1, DACL_SECURITY_INFORMATION=4, NULL DACL) | Error 5; a real attempt to install a NULL DACL, **not an owner-change test** |
| S:529–546 | CreateFileW(C, GENERIC_READ\|GENERIC_WRITE, share=0, OPEN_EXISTING) | Failed prerequisite open/error 5; **no CreateFileMapping or MapView call** |
| S:550–559 | CreateFileW(C, GENERIC_READ, share=0, OPEN_EXISTING) | Failed read open/error 5; **no ReadFile of C, inherited-handle inspection or DuplicateHandle test** |

The security-information mask in the ACL attempt changes the DACL, not ownership. [SetNamedSecurityInfoW](https://learn.microsoft.com/en-us/windows/win32/api/aclapi/nf-aclapi-setnamedsecurityinfow).

For these retained records, the actual errors are permission/access denied rather than missing-path errors. The broad exception handlers would be unsafe as general security assertions, but no hidden later mapping/ReadFile/owner operation can be credited: those calls do not exist here.

**Supported conclusion:** original-user access plus restricted-child known-path denials, working allowed channels and the inspected token/launch flow substantiate a useful disposable private-store/capture boundary. Exact kernel access-pass attribution is an inference consistent with the documented mechanism; no SRM trace separately identifies the denying pass. This is not continuous protected production storage, safe namespace acquisition, manifest authenticity or the production worker's filesystem boundary.

## 6. Parent-process result: RTFR-2 confirmed and narrowed

### The five valid process-open attempts target H, not the actual parent

S:287–292 launches **a separate unrestricted disposable Python process H** sleeping for 120 seconds. The trusted parent first opens H with aggregate process access at S:309–311. Its PID is embedded as target_pid_H in the generated child script. S:571–587 then actually calls OpenProcess with:

- `0x001F0FFF` aggregate access;
- VM_READ `0x0010`;
- VM_WRITE `0x0020`;
- DUP_HANDLE `0x0040`;
- QUERY_INFORMATION `0x0400`.

All five retained results are NULL/invalid returned handles with error **5**. With the matching source and trusted positive control, these support **five denied opens against that controlled same-user H object**. They are not fake NULL inputs. They are also **not direct tests of Java's process, the diagnostic controller's process/token, every unrestricted peer, existing inherited handles or a successful memory/handle operation**.

No individual PROCESS_CREATE_PROCESS, VM_OPERATION, QUERY_LIMITED_INFORMATION or token duplication/impersonation/assignment test exists. Denying an aggregate mask does not prove every smaller permission is denied. Windows defines these as different process rights. [Process security and access rights](https://learn.microsoft.com/en-us/windows/win32/procthread/process-security-and-access-rights).

### NULL-token test is now conclusively inspected

S:589–597 contains:

```python
advapi32.OpenProcessToken(None, 0x0008, ctypes.byref(h_tok))
```

This passes **NULL**, not H's valid process handle. The code comments explicitly acknowledge that no process handle is available, yet mark any FALSE result DENIED_OK. E retains error **6**, ERROR_INVALID_HANDLE. This is a **confirmed diagnostic false-positive**, not access-control enforcement. Error 5 and error 6 have different meanings. [Windows error codes](https://learn.microsoft.com/en-us/windows/win32/debug/system-error-codes--0-499-).

No valid-parent-handle token-access test occurred. OpenProcessToken requires an appropriate valid process handle and separately checks requested token rights. [OpenProcessToken](https://learn.microsoft.com/en-us/windows/win32/api/processthreadsapi/nf-processthreadsapi-openprocesstoken).

**RTFR-2 remains OPEN / CONFIRMED.** Exclude this row from security successes. “Six parent escapes denied” is false; the justified statement is five denied opens to H plus one invalid test. No successful privilege escape is demonstrated.

S:800–817 includes the invalid row in the author aggregate. The independent FEASIBILITY_SUPPORTED_WITH_LIMITS verdict is **not** acceptance of that six-row score. The missing valid-token discriminator belongs in later finite integration proof; it does not negate the independently supported core token/private-fixture mechanism.

## 7. Job Object ordering, handles and cleanup

### What source and retained results support

S:658–677 creates a Job Object, requests extended-information class 9 with KILL_ON_JOB_CLOSE and creates the first child **suspended**. The control flow calls AssignProcessToJobObject before ResumeThread (S:690–696). E records assigned_to_job=true, child exit 0 and child-generated probe results. Thus intended assignment-before-resume ordering is now inspectable, rather than only a narrative claim.

S:733–779 repeats token construction and suspended launch for one sleeping second child, assigns/resumes it, checks STILL_ACTIVE, calls TerminateJobObject(..., 99), waits and reads exit 99. E's second-child result matches that path. This supports **one-child forced termination**, not a tested descendant tree or close-triggered kill. [TerminateJobObject](https://learn.microsoft.com/en-us/windows/win32/api/jobapi2/nf-jobapi2-terminatejobobject).

### What is not established

- SetInformationJobObject, several assignment/resume/wait/exit and CloseHandle return values are not enforced or fully recorded. No final active-process-count proof is retained.
- The first launch uses **bInheritHandles=TRUE** after marking the stdout pipe writable handle inheritable (S:649–672). No explicit handle allowlist or full inherited-handle inventory exists.
- STARTF_USESTDHANDLES is set; stdin remains zero. Observed minimal startup does not certify a general robust stdio arrangement.
- CloseHandle calls exist for normal-path process/thread/pipe/job/token handles, but no positive release evidence or general finally/exception cleanup exists. SID allocations are not all freed before process exit.
- The dummy H is a separate Popen process, explicitly terminated/waited at S:783–784; it is not a tested descendant of the restricted second child.
- safe_rmtree (S:145–159) resets attributes/ACLs and removes the fixed controlled fixture tree; command failures and some cleanup errors are swallowed. E's cleanup flag is path absence. It is not proof of all handles/processes or a production object-bound cleanup protocol.
- The generated probe/result files live in worker-writable scratch. The generated script hash is only printed, not bound into E/R. This is a controlled diagnostic result channel, not a trusted restricted-producer/staging seal.

These limitations prevent copying the harness into production. They do not require another account or invalidate its narrow successful launch and access-control observations.

## 8. Security/runtime implications for later production design

| Obligation | Precise remaining limit |
| --- | --- |
| Private store/temp | Fixture separation supported; actual Java-managed store root/parent/object rights, manifest integrity and protected lifetime through use remain unimplemented/unverified |
| Other local files | Restricting Users/Everyone/Authenticated Users can satisfy the restricting pass on unrelated broadly granted resources. Blocking owner-only A/B does not confine Python to approved RAG input |
| Input/scratch | Source confirms **Users full control** on E/F. This is a diagnostic convenience, not an acceptable sensitive-input default. Assess actual parent traversal/ACLs and other accounts; no unrelated-file disclosure was tested |
| Privileges/groups | No disable/delete list requested. Production needs deliberate effective privilege/group rights and complete child-side evidence, not claims that all privileges were removed |
| Parent/token/handles | H-specific opens only; NULL token test invalid; actual Java/peer token/memory/duplicate/inherited-handle and unrestricted-descendant paths remain unproved |
| Desktop/window station | Both launches choose WinSta0/Default. Microsoft warns against unrestricted/default-desktop sharing for restricted applications; no mitigation was demonstrated here |
| Interpreter/native libraries | Diagnostic uses MSYS/UCRT Python **3.11.7**. Repository `.python-version` and existing venv record **CPython 3.14.4**; pyproject requires >=3.12. No actual venv/native parser/model compatibility proof |
| Transfer/staging | JSON file read and scratch write are not existing Java prepared-input/identity handshake or restricted SQL staging verification |
| Java/Job Object integration | Existing NativeWorkerProcess/RagWorkerSupervisor remain unchanged; actual child attestation, supported failure paths, bounded supervision and DOMAIN regression proof remain required |

The broad-SID consequence follows from the two-pass access model, not an observed escape. Sharing Users grants can satisfy allowed access without requiring every listed SID to appear. The absence of a directory-list right is not a reliable known-path confidentiality boundary; relevant traverse privileges and per-object grants matter. [Restricted Tokens](https://learn.microsoft.com/en-us/windows/win32/secauthz/restricted-tokens), [Privilege constants](https://learn.microsoft.com/en-us/windows/win32/secauthz/privilege-constants).

The default-desktop warning is a documented production concern, not a successful UI escape in this experiment. [Restricted Tokens](https://learn.microsoft.com/en-us/windows/win32/secauthz/restricted-tokens).

No claim of complete confinement, runtime compatibility, actual restricted producer provenance, G4 transaction safety or physical publication follows. No new signing platform, generalized sandbox/job engine, host-wide ACL changes, account/service or architecture amendment is prescribed.

## 9. RTFR dispositions and independent verdict

| Finding | Additive disposition | What changed / what remains |
| --- | --- | --- |
| **RTFR-1 — High** | **PARTIALLY RESOLVED; core feasibility evidence gap addressed** | Exact source, byte hash, invocation association, actual token/ACL/call/control flow now inspectable. Full child-token identity/attributes, effective SDDL/owner/parent rights, complete native return trace and execution-time byte attestation were never captured and remain **NOT_VERIFIED**. Carry those as production integration gates, not a demand to rerun this diagnostic merely to inspect already recovered source |
| **RTFR-2 — Medium** | **OPEN / CONFIRMED** | NULL OpenProcessToken and false DENIED_OK classification independently confirmed. No valid token-access proof; historical source/results remain unchanged |

**Overall: FEASIBILITY_SUPPORTED_WITH_LIMITS.**

The exact recovered source, matched invocation digest, truthful API interpretation, mandatory normal-access baseline and retained restricted-child/private-fixture/allowed-channel results substantiate the **core restricting-token route** sufficiently for narrowly scoped implementation/proof planning. No core Windows API incompatibility or unavoidable dedicated-account/elevation prerequisite is established. The prior universal same-user impossibility claim remains unsupported.

This is not a claim that every historical action was cryptographically attested or independently rerun. Nor does it accept the author's aggregate score, failed token test, broad-SID policy or normal-path-only cleanup as production security. Missing child/SDDL/lifecycle/actual-runtime proof remains explicit. No new corroboration experiment is necessary solely because the earlier repository search could not access this recovered source.

Status is unchanged:

- **CP18-AB-2 OPEN** until actual production isolation is implemented and independently verified.
- **CP18-AB-1 and CP18-AB-3–7 OPEN; CP18-AB-8 RESOLVED.**
- **G1/G3 unaccepted; G2/G4 OPEN / NOT_VERIFIED.**
- **FR18-1–11 OPEN**, including Critical FR18-3.
- **C–H unauthorized; snapshots INACTIVE; live-original ingestion DISABLED / DEFERRED.**
- Prior credited repairs and historical 2/2 exhausted remediation record preserved.

## 10. Smallest next owner-controlled action

**Recommend ONE bounded A+B corrective integration/proof handoff, rather than another stand-alone feasibility diagnostic.** It must define the exact changes and discriminating proofs before execution is authorized:

1. Existing native launcher/Job Object extension only: actual restricted child-token attestation; complete native return handling; minimal privileges, desktop and handle policy; narrow approved runtime/input/scratch access; effective private store/temp and parent/token rights with retained SDDL. Do not transplant the four broad SIDs/Users full-control/default-desktop/general-inheritance choices as an approved production policy.
2. Resolve access to needed Windows/CPython 3.14.4/runtime/model resources **and denial of unrelated unapproved data** without modifying original/host-wide ACLs. If a concrete supported policy cannot be established within ADR-0022, STOP with that exact predicate and owner choice; do not automatically switch architectures or provision accounts.
3. Preserve existing DOMAIN behavior and existing prepared-input/staging/process supervision interfaces. Plan the minimum actual-runtime/transport/handle/tree tests; no OCR/GPU/full-suite loop or C–H implementation is implied.
4. Keep CP18-AB-1 and CP18-AB-3–7's separate finite obligations from the corrective review: acquisition binding; strict trusted manifest/read/seal continuity; fail-closed no-clobber; crash/recovery; ownership-safe containment/cleanup; discriminating adversarial evidence. This supplemental review does not reopen or redesign them.

Suggested exact next approval:

> Authorize ONE documentation-only A+B corrective handoff/proof specification under unchanged ADR-0022, using the recovered restricted-token feasibility result. Define the minimal existing-native-launcher/store changes, production access policy, actual-runtime/DOMAIN compatibility evidence and CP18-AB-1–7 correction boundaries. No code, tests, experiments, host provisioning, ACTIVE authority expansion, C–H or ingestion is authorized by this planning step. Return the finite plan for owner review and STOP; later implementation requires separate explicit owner authorization.

This is a recommendation, **not issued authorization**, and no handoff or implementation is created here. It gives the owner a reviewable security integration scope without another speculative experiment or automatic remediation cycle.

## 11. Preservation and STOP

Read-only review/hash/link checks only. The recovered external source retains its 38,818 bytes and exact expected hash. All **2,129 inherited nonignored files** and **302 prior evidence/report files** remain unchanged; only this additive review is created. The previous independent review, E/R, ADR-0022 and ACTIVE are preserved.

The preceding scope-aware review ledger is reused for unchanged repository dimensions. Newly resolved evidence coverage is source/invocation traceability and inspected core token/ACL/native calls; native return completeness, actual child authority, effective descriptors, complete process/handle isolation and production runtime remain bounded **NOT_VERIFIED**, not security PASS.

No diagnostic import/rerun, token/process/ACL/native experiment, production/test edit, ingestion, A–H implementation, P18-FINAL, finding closure, account/service/elevation, commit, push, tag or PR occurred. **STOP.**


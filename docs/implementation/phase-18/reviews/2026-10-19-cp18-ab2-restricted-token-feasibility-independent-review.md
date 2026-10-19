# Phase 18 — CP18-AB-2 restricted-token feasibility independent review

- **Date:** 2026-10-19.
- **Independent verdict:** **ADDITIONAL_EVIDENCE_REQUIRED**.
- **Implementer classification:** FEASIBLE_WITH_EVIDENCE, retained with its original attribution.
- **Scope:** read-only assessment of the one owner-authorized disposable diagnostic; no execution or implementation.

A nonprivileged restricted-token route remains technically credible under ADR-0022. The retained JSON records are consistent with a restricted Python launch and denials against specially protected fixtures. However, the actual diagnostic source is unavailable, and the records omit the effective ACLs and token/call attribution needed to independently validate the core mechanism. The reported source hash cannot be verified. One of the six claimed escape denials is demonstrably an invalid-handle result. Therefore this review cannot independently endorse FEASIBLE_WITH_EVIDENCE as an audited execution result.

This is an **evidence insufficiency**, not FEASIBILITY_BLOCKED, a successful escape, or a requirement for another Windows account. CP18-AB-2 remains OPEN; G1/G3 are not accepted.

## 1. Authority, baseline and preservation

Binding sources are [ADR-0022](../../../adr/0022-phase-18-immutable-snapshot-whole-file-consent-no-change.md), the [implementation handoff](../adr-0022-immutable-snapshot-implementation-handoff.md), [ACTIVE](../../handoffs/ACTIVE.md), the [first A+B checkpoint](2026-10-19-adr-0022-slice-1-ab-independent-checkpoint.md) and [corrective independent review](2026-10-19-adr-0022-slice-1-ab-corrective-independent-review.md). Their hashes match the previously independently observed versions:

| Source | SHA-256 |
| --- | --- |
| ADR-0022 | `fa4ce5681093cc758f4a2cea6ee5c8ce486e3fc5aeae31ba346edfafde4cb25d` |
| Implementation handoff | `3b1958ba2f3d897363f130b93d59988f7ad098a3d222233dbebe90d86514d43c` |
| First independent checkpoint | `c68834d332386543292f42028972843576a6de0c88cd35b3a469e47a052bf032` |
| Corrective independent review | `48f2e1cc56384db5bd0e4c9f28c68dfb3de61ea15b4420d120d7aca995ebf677` |
| ACTIVE | `3f9d833d250b977714b00ef2948c27073161e883c43036365144dc9b176d27e4` |

Root/backend/documentation/test AGENTS contents are unchanged. Applied repository independent final-review/shared coverage and local-RAG rules proportionately. The owner's express read-only diagnostic review controls; workflow defaults to edit ACTIVE, run tests or return a commit message do not apply.

Branch remains `main`; HEAD remains `435ffea979681f22d723f551dd8b0cd0b733c8cd`. At entry there are **2,128 tracked/nonignored files**. They match the preceding independent baseline plus the preceding corrective review itself. No production Java/Python, tests, migration, dependency/build file, ADR, AC, historical review or ACTIVE content changed during the diagnostic interval observable between these independent inventories.

The preceding 293 receipt/Surefire artifacts remain unchanged. The seven distinctly named artifacts from the preceding independent corrective test rerun remain, together with the diagnostic's **two new JSON files**; the current evidence inventory totals **302 files** under `logs/phase18/` and `backend/target/surefire-reports/`. No old receipt was overwritten by this review.

This verifies **repository content preservation**, not historical Windows ACL/token/account state. There is no host-security before/after audit or retained harness from which to certify every temporary security change. No permanent account, service or elevation is evidenced; their complete absence is not independently established by a boolean in the report.

## 2. Actual evidence inventory, hashes and attribution

Hereafter **E** means `logs/phase18/restricted-token-feasibility-2026-10-19.json` (200 lines), and **R** means `logs/phase18/cp18-ab2-diagnostic-receipt.json` (13 lines). JSON paths below identify actual retained fields.

| Artifact | Actual availability / integrity |
| --- | --- |
| `run_cp18_ab2_diagnostic.py` | **NOT AVAILABLE.** Absent at repository root and under logs/phase18; an ignored/hidden-inclusive repository filename search found no retained copy. No copy was located in the reported evidence/preservation locations |
| Reported diagnostic source SHA-256 | `ae2a057bfc1bb2378bf07c3e32db733e2e9568614b057a5bf598bb04f1e70f93` — supplied claim only; **cannot recompute** |
| R | Present; SHA-256 `652e0540f6c8cae57de6af255d88d03e79cb5bfe47545d12448a39af3f98869b` |
| E | Present; SHA-256 `29a7e8a88a7e3ddc52ca2fb9263f0e2252e21ac89444cde49e8e19cdece9ed62` |
| `target/cp18_ab2_diagnostic/` | Absent at review time; cannot inspect former objects, ownership, ACL inheritance or child harness there |
| Exact command/raw stdout/API trace/security descriptor dump | Not supplied or located with the reported artifacts |

The source may have been removed during cleanup or retained outside this workspace; the available evidence does not establish which. No deleted source, historical fixture or executable evidence was reconstructed.

R records `python`, argv digest `3698b8ef190f0f81e4f460932eb51996e1be1c2a8578f808b9c57f71c0baa4bb`, PID 24792, start `2026-10-19T02:04:28.431+00:00`, finish `02:04:29.367+00:00`, 0.938 seconds, exit 0 and cleanup not needed. E records timestamp `02:04:29Z`, 0.827 seconds, child PID 11324 and a second child PID 26940. The timings and first-child PID repeated in the positive controls are internally consistent.

The existing bounded runner hashes NUL-joined argv and records command exit, not script content or native security correctness (`scripts/run-bounded-command.py`:77–101). Exact argv is unavailable here; its hash cannot be independently matched to a retained invocation. Wrapper exit 0 does not authenticate E, establish that every operation executed as labeled, or bind E to the claimed source bytes. Only one invocation receipt is supplied; prior trial count cannot be audited from it.

## 3. Diagnostic source/API review: explicit evidentiary limit

Because the source is missing, this review **cannot inspect**:

- ctypes argtypes/restype, pointer-sized HANDLE handling, structure sizes/alignment and variable-sized token buffers;
- the exact CreateRestrictedToken flags, disabled-SID list, privilege deletion list, SID_AND_ATTRIBUTES arrays or error checking;
- whether the queried token was the caller, candidate, actual child primary token or an impersonation token;
- the actual token handle passed to CreateProcessAsUserW, process/thread descriptors, inheritance flag and handle allowlist;
- ACL construction, owner, protected inheritance, all parent ACLs or which fixture operation created which object;
- the actual native access masks, dispositions, mapping calls, OpenProcess target PID, valid handles and immediate error capture;
- ordering of suspended launch, job assignment, resume and child attestations, or cleanup finally paths.

E's `token_security.mechanism` string is a description, not a call trace. The absence of inspectable source is material for this specific review: its mandatory purpose is to verify those actual calls rather than repeat the description.

## 4. Token and process-creation assessment

| Required property | What E records | Independent conclusion |
| --- | --- | --- |
| TokenUser | Redacted user SID ending 1001 | Consistent with the existing user; no caller/child identity comparison or authentication/token identity binding retained |
| TokenGroups / deny-only attributes | No group/attribute dump | **NOT_VERIFIED** |
| TokenRestrictedSids | Four listed SIDs and count 4 | A concrete candidate list is recorded; actual API array and querying of the running child's token cannot be verified |
| TokenHasRestrictions | No distinct field/query result | **NOT_VERIFIED**; not interchangeable with the labeled is_token_restricted boolean |
| TokenType | No returned type | Claimed primary-token path, but actual primary type/handle binding **NOT_VERIFIED** |
| TokenPrivileges | Five names: Shutdown, ChangeNotify, Undock, IncreaseWorkingSet, TimeZone | No enabled/disabled attributes, caller comparison or actual privilege-removal flags. Do not say privileges were fully removed |
| TokenIntegrityLevel | S-1-16-8192, Medium | Medium is recorded; it neither proves restricted-SID enforcement nor creates a same-medium read/write boundary |
| Elevation / special privileges | Nonprivileged scope and no-elevation boolean | No TokenElevation/TokenElevationType or privilege attributes proving the caller's effective execution state |
| Actual creation | CreateProcessAsUserW, CREATE_SUSPENDED, WinSta0/Default, child PID 11324, exit 0 | Credible recorded startup result, but no source/handle/BOOL trace independently binds it to the candidate token |
| DLL initialization and child work | Standard imports/input/scratch successes under the same recorded child PID | Supports the reported narrow Python execution story; no independent child-token attestation or production-runtime proof |

### What Microsoft documents

A restricting-SID token is genuinely more constrained than a simple copied token: access must pass both ordinary enabled-SID and restricting-SID evaluation. Deny-only groups, privilege removal and restricting SIDs are different controls. WRITE_RESTRICTED covers writes only; a read-confidentiality argument must establish that it was not substituted for full restricting checks. Microsoft also warns against sharing the unrestricted default desktop with a restricted application. The recorded WinSta0/Default startup is not proof that desktop escape concerns are addressed. [CreateRestrictedToken](https://learn.microsoft.com/en-us/windows/win32/api/securitybaseapi/nf-securitybaseapi-createrestrictedtoken).

IsTokenRestricted tests for a restricting-SID list, not all possible token filtering. TokenHasRestrictions reports whether a token has been filtered; TokenType, groups, privileges, integrity and elevation are separate queries. Thus E's one boolean cannot stand in for the missing requested fields or establish which token was examined. [IsTokenRestricted](https://learn.microsoft.com/en-us/windows/win32/api/securitybaseapi/nf-securitybaseapi-istokenrestricted), [TOKEN_INFORMATION_CLASS](https://learn.microsoft.com/en-us/windows/win32/api/winnt/ne-winnt-token_information_class).

CreateProcessAsUserW explicitly exempts a restricted version of the caller's own primary token from SeAssignPrimaryTokenPrivilege. Its general token/access and privilege requirements, including the discussion of SeIncreaseQuotaPrivilege, still require truthful actual outcomes. API success and subsequent DLL initialization/child exit are separate stages. These semantics provide a credible nonprivileged route; the existing launcher's use of CreateProcessW does not imply another account is necessary. [CreateProcessAsUserW](https://learn.microsoft.com/en-us/windows/win32/api/processthreadsapi/nf-processthreadsapi-createprocessasuserw).

**Conclusion:** the recorded token shape is plausible and more specific than the earlier unsupported impossibility claim. Its actual construction and use are **not independently verified** without the source and attributed token results. No failed supported API/permission result in this evidence establishes a provisioning blocker.

## 5. Protected store/temp matrix: all thirteen retained records

All entries below are **recorded outcomes**, not independently rerun or source-verified operations. E:62–131 supplies thirteen entries and `all_denied=true`.

| Recorded check | Retained result | Exact supported interpretation / limitation |
| --- | --- | --- |
| read_sealed_C | PermissionError / errno 13 at known orig path | Reported denied ordinary read; file-open mode, object existence/identity and child token attribution missing |
| read_manifest_D | PermissionError / errno 13 | Same limitation for manifest; meaningful target if setup/call is verified |
| enum_snapshot_A | WinError 5 | Reported denied directory listing; not proof of every known-path file read |
| enum_capture_B | WinError 5 | Reported denied mutable-capture listing; no separate capture-byte read row |
| write_snapshot_A | PermissionError creating exploit.txt | Reported create/write denial; not a separate append/truncate test on an existing file |
| write_capture_B | PermissionError creating exploit.txt | Same limitation for capture directory |
| rename_in_store | WinError 5, orig-to-renamed path | Reported rename denial; replacement of an existing target/root/ancestor or alias manipulation not separately recorded |
| delete_file_in_store | WinError 5 | Reported file unlink denial; actual DELETE versus parent DELETE_CHILD access path not traced |
| delete_child_directory | WinError 5 for capture directory | A directory-removal result, not a native FILE_DELETE_CHILD rights/parent replacement matrix |
| reset_attribute_sealed_C | false, last_error 5 | Reported native attribute-change denial; precise mask/call and trusted positive control missing |
| modify_acl_store_A | error code 5 | Reported ACL-change denial; no actual API/security-information mask or separate ownership-change attempt |
| writable_mapping_handle_C | invalid handle, last_error 5 | **Prerequisite writable open denied** as recorded; no successful CreateFileMapping/MapView operation is recorded |
| read_handle_open_C | invalid handle, last_error 5 | **Prerequisite read open denied** as recorded; no actual ReadFile invocation is evidenced |

Consequently **13 recorded checks does not mean thirteen distinct operations completed**, and specifically does not establish append, truncate, explicit ReadFile execution, map execution, owner change, existing-target replacement, writable aliases or parent-directory bypass resistance. A failed prerequisite can be a valid security denial; it must be labeled as that boundary, not as an executed later native operation.

### ACL / authority proof missing

The JSON contains **no DACL/owner/SACL dump**, protected-inheritance flag, parent rights, trusted-side access control result or native object identity. The owner's description says inheritance was disabled and input/scratch received Users grants; the retained JSON does not independently show that construction.

If protected objects permit the unrestricted user while permitting none of the restricting SIDs, a full restricted access check can deny Python while allowing Java. Ordinary ownership/WRITE_DAC/WRITE_OWNER, applicable privilege state, inherited ACLs, parent DELETE_CHILD, inherited/duplicated handles and aliases must still be accounted for. An ordinary SID match alone or omission of the user from the restricting list is not a complete ACL proof. Windows evaluates actual security descriptors and access rights, including alternative parent-directory delete authority. [How AccessCheck works](https://learn.microsoft.com/en-us/windows/win32/secauthz/how-dacls-control-access-to-an-object), [File security and access rights](https://learn.microsoft.com/en-us/windows/win32/fileio/file-security-and-access-rights), [Filesystem set-information security checks](https://learn.microsoft.com/en-us/windows-hardware/drivers/ifs/irp-mj-set-information3).

The recorded denials are promising evidence for a specially configured private store. They do not establish actual store/temp isolation, or continuous integrity through use, for the unmodified production Java store/ordinary worker.

## 6. Broad restricting SIDs and permitted input/scratch

E:21–26 lists:

- BUILTIN Users: `S-1-5-32-545`;
- RESTRICTED_CODE: `S-1-5-12`;
- Everyone: `S-1-1-0`;
- Authenticated Users: `S-1-5-11`.

A list of four restricting SIDs does **not** require four independent allow grants. An applicable allow for one of those SIDs can satisfy requested rights in the restricting pass, subject to deny ACEs and the normal pass. Accordingly, unrelated resources accessible to the original user and granted appropriate access to Users/Everyone/Authenticated Users may also be accessible to this candidate. A private store can be blocked while unrelated files remain readable. That is a direct consequence of access-check semantics, not an observed unrelated-file disclosure in this diagnostic. [CreateRestrictedToken](https://learn.microsoft.com/en-us/windows/win32/api/securitybaseapi/nf-securitybaseapi-createrestrictedtoken).

No actual unrelated user/application-file DACL or access test is supplied. Do **not** declare a specific person's files exposed. Equally, do not accept these broad SIDs as a demonstrated approved-input-only sandbox.

The reported BUILTIN Users full-control grants on input/scratch cannot be checked because the DACL/source/fixtures are absent. If used as stated, those grants could permit other local users/processes to read or alter sensitive prepared input where normal access and parent traversal permit it. They are not suitable production defaults merely because they make Python start. Existing parent ACLs must be considered; a directory-list denial alone does not protect a known child path. Windows normally supplies SeChangeNotifyPrivilege to bypass traversal checks, and that privilege is among E's five names, although its enabled state is missing. [Traverse privilege checks](https://learn.microsoft.com/en-us/windows-hardware/drivers/ifs/checking-for-traverse-privilege-on-irp-mj-create).

A later integration must distinguish runtime/model read-execute resources from exact prepared input, narrow per-attempt scratch/output and forbidden originals/store/unrelated data. Use a bounded allow policy and effective object/handle rights compatible with the existing launcher, without granting Users full control over sensitive input or modifying source/host-wide ACLs. No new account or signing/security platform is prescribed here.

## 7. Parent-process escape: corrected evidence interpretation

E:132–165 labels six rows DENIED_OK and `all_denied=true`.

| Row | Recorded result | Review disposition |
| --- | --- | --- |
| PROCESS_ALL_ACCESS | Invalid handle returned, error 5 | Consistent with denied requested aggregate access; actual PID/mask/call source unavailable |
| PROCESS_VM_READ | Invalid handle returned, error 5 | Consistent with a narrow open denial, not an executed ReadProcessMemory call |
| PROCESS_VM_WRITE | Invalid handle returned, error 5 | Consistent with narrow open denial, not an executed write/injection |
| PROCESS_DUP_HANDLE | Invalid handle returned, error 5 | Consistent with narrow open denial, not proof about already inherited/duplicated handles |
| PROCESS_QUERY_INFORMATION | Invalid handle returned, error 5 | Does not independently test QUERY_LIMITED_INFORMATION or the token object's own ACL |
| OpenProcessToken | DENIED_OK, **error 6** | **INVALID SECURITY TEST RESULT**; not access denied |

Windows distinguishes error **5** (access denied) from **6** (invalid handle). The sixth row must not count as enforced security. [Windows error codes](https://learn.microsoft.com/en-us/windows/win32/debug/system-error-codes--0-499-).

The owner identifies the attempted call as OpenProcessToken(NULL, TOKEN_QUERY). **The missing source prevents independent confirmation of the exact passed handle.** E itself contains no handle value or process identity for this call. Regardless of whether it was NULL, truncated, closed or otherwise invalid, error 6 establishes no denial against a valid parent's token.

The other five rows remain useful *reported* open denials if their valid parent PID and numerical masks are established. They cannot be independently certified from labels alone. PROCESS_ALL_ACCESS denial does not prove that each lesser right is denied; PROCESS_CREATE_PROCESS, process/token security-descriptor modification, VM_OPERATION, already-held handles and unrestricted-descendant/token escape are not separately proved. No successful escape is evidenced.

The smallest missing token-specific proof is one correctly typed OpenProcessToken operation against a **valid, identified parent-process handle with the required QUERY_LIMITED_INFORMATION permission**, requested TOKEN_QUERY rights and immediate error capture, with a trusted positive control. If obtaining the handle is denied, record a prerequisite-open denial instead of fabricating an executed token denial. A controlled diagnostic-only limited handle may distinguish the token ACL if needed; do not inherit a full-access parent handle. Token query access, if allowed, is not itself proof of token duplication or privilege escape. [OpenProcessToken](https://learn.microsoft.com/en-us/windows/win32/api/processthreadsapi/nf-processthreadsapi-openprocesstoken), [Process security and rights](https://learn.microsoft.com/en-us/windows/win32/procthread/process-security-and-access-rights).

The WinSta0/Default choice also leaves the documented shared-desktop concern unresolved. Do not use these six rows as a complete process-escape certification.

## 8. Runtime, input and staging representativeness

| Area | Evidence / disposition |
| --- | --- |
| Python startup | E reports `C:\msys64\ucrt64\bin\python.exe`, Python **3.11.7**, GCC/UCRT 64-bit, exit 0; narrow reported success, source attribution unavailable |
| Standard imports | json/hashlib/math/sys/os/ctypes/subprocess/platform reported successful; no RAG package/native-library imports |
| Controlled input | A small JSON task read, task-test-001; file-based fixture positive, **not** Java prepared-input protocol/SQL staging proof |
| Scratch | Create/read-back success reported; effective ACLs and other-process reachability not retained |
| Repository runtime | `rag-engine/.python-version`:1 pins **3.14.4**; `.venv/pyvenv.cfg` records uv CPython 3.14.4; README:33 describes that pin |
| Supported package requirement | `rag-engine/pyproject.toml`:6 requires Python **>=3.12**; the diagnostic interpreter is below that declared minimum |
| Models/native libraries | No model/runtime/DLL compatibility result for the repository environment |
| Existing worker and restricted staging | **NOT_VERIFIED**; JSON-file reading is not HTTP/service identity/attempt-bound input/Psycopg restricted staging |
| DOMAIN compatibility | No production launcher change and no regression is demonstrated; no actual DOMAIN regression test was run |

The actual unchanged launcher is `NativeWorkerProcess.java`:104–110 (CreateProcessW, suspended/Job Object path). `RagWorkerSupervisor.java`:89–118 uses configured binary/venv launcher, a bounded environment, uvicorn, PID/creation identity and committed binding before resume. None of that was integrated with the candidate token. These are concrete representativeness gaps, not reasons to run OCR/GPU or Phase 18 end-to-end tests during this review.

During a **later separately authorized integration**, the minimum compatibility proof is the actual pinned executable/venv plus existing worker startup/identity handshake, needed import/native-library/model-file access, controlled scratch and existing approved transfer/restricted staging under the selected token. Preserve Job Object ownership, cancellation/termination and one focused DOMAIN startup/boundary regression. Broader OCR/GPU/quality/resource acceptance belongs to its existing later packages; no new full-suite loop is requested.

## 9. Job Object and cleanup assessment

E:41–61 records CREATE_SUSPENDED, assigned_to_job=true, KILL_ON_JOB_CLOSE and a second child transitioning from STILL_ACTIVE to exit 99 after a successful TerminateJobObject call.

- **Suspension / ordering:** stated, but no source, ResumeThread result or ordered call events prove assignment preceded execution.
- **Configuration:** the limit is a descriptive string; the actual job-information structure/SetInformation result is unavailable.
- **Termination:** consistent with terminating that one second child. No descendant/tree case or final active-process-count/wait trace is retained.
- **Kill on close:** an explicit TerminateJobObject call does not exercise KILL_ON_JOB_CLOSE behavior.
- **Handles:** no auditable per-handle ownership/CloseHandle results, inheritance or duplication trace is retained.
- **Temporary filesystem:** the reported fixture root is absent now. That corroborates present path absence, not all handle/process death, ACL restoration or absence of host-wide changes.
- **Supervision:** the receipt's cleanup=not_needed means the wrapper exited normally; it is not child-tree cleanup verification.

Microsoft's job API semantics support a supervised termination route. They do not convert a single-child observation into a tested descendant/inherited-handle scenario. [TerminateJobObject](https://learn.microsoft.com/en-us/windows/win32/api/jobapi2/nf-jobapi2-terminatejobobject).

No automatic tree probe or host-wide security scan was performed.

## 10. Findings, feasibility verdict and status

| Finding | Severity | Evidence | Required finite remedy |
| --- | --- | --- | --- |
| RTFR-1 | **High** | Missing harness/claimed source hash uncheckable; E lacks effective ACLs, token attributes and actual child/call binding | Supply the exact preserved source and any contemporaneous raw trace. If unavailable, obtain explicit authorization for the single minimal source-retained corroboration in §11; do not reconstruct the old evidence |
| RTFR-2 | **Medium** | E:161–164 records error 6 as DENIED_OK and includes it in six escape denials | Preserve E unchanged; issue a truthful evidence addendum distinguishing invalid handle from access denied. A valid-parent-handle token test remains missing; it is not a demonstrated escape |

Broad SID/input grants, the default desktop, production interpreter/model/transport and tree/handle continuity are **remaining production security gates**, not newly reproduced vulnerabilities or established Windows incompatibilities.

**Verdict: ADDITIONAL_EVIDENCE_REQUIRED.** The core documented mechanism remains credible; the retained execution evidence is insufficient to validate its exact use. No concrete privilege/account/provisioning blocker has been established. This review does not reverse the preceding assessment that a bounded in-architecture correction route is plausible, nor elevate plausibility to production feasibility acceptance.

Status preserved:

- **CP18-AB-1–7 OPEN**, including CP18-AB-2; no automatic closure from this diagnostic.
- **CP18-AB-8 RESOLVED** remains credited.
- **G1/G3 not accepted**; **G2/G4 OPEN / NOT_VERIFIED**.
- **FR18-1–11 OPEN**, Critical FR18-3 unchanged.
- **C–H unauthorized**, snapshots INACTIVE, live originals DISABLED / DEFERRED.
- Prior credited repairs and the historical 2/2 exhausted remediation record remain unchanged.

ACTIVE still contains the earlier implementer's blocker/PASS claims; it is unchanged as required. This independent review records current adjudication without issuing new implementation authority.

## 11. One finite owner-controlled next direction

**First obtain existing preserved evidence, without execution:** request the exact harness matching the supplied SHA-256 and its contemporaneous command/token/ACL/call trace, if the author has an unmodified retained copy. Do not ask anyone to recreate an “original” file or rewrite E/R. A preserved copy can remove the source-review limitation; missing fields/invalid-handle outcomes remain separately disclosed.

**If that evidence does not exist, the smallest new active scope is ONE source-retained core corroboration diagnostic**, not a repeat of all A+B tests or a general Windows penetration test. Only the owner can authorize it. Its finite proof is:

1. One disposable restricted Python child with source and exact command hashes retained outside cleanup, correct pointer/structure declarations, actual CreateRestrictedToken arguments and caller/candidate/child token type/user/groups/restricted-SID/privilege/integrity/elevation attribution.
2. One private store file/manifest and protected capture fixture, plus one allowed input/scratch fixture. Retain effective owners/protected DACLs/parent rights and normal-token positive controls; prove known-path read and write-open denial from the actual restricted child and allowed input/scratch operations. Retain the actual requested masks, native returns and immediate errors.
3. Correct the token-access discriminator using a valid identified parent handle as described in §7; do not count prerequisite failure or invalid handle as a token ACL denial. Retain distinct process-open results.
4. Record suspended assignment-before-resume and positive wait/exit/handle cleanup for that controlled child. Make no descendant, close-to-kill, actual RAG transport or production-security claim from this one-child test.
5. No production/test/ADR/ACTIVE changes, accounts/services/elevation, source/host-wide ACL changes, package installs, new transport, ingestion or C–H. STOP once with evidence; no automatic second candidate or retry loop. Unproved setup/provisioning requirement is a STOP, not permission to elevate.

That is **proposed scope, not authorization**, and nothing was executed here. It resolves inspectability and the core launch/private-store boundary without demanding OCR/GPU/full-model acceptance. Broad-SID confinement and actual runtime/transport/escape/lifecycle proof remain mandatory for later integration.

Only **after** auditable core feasibility is accepted should the owner consider one bounded native-launcher/store integration/proof scope: effective restricted context and narrow application-owned ACL/handle policy, actual repository-runtime compatibility, exact approved input/scratch, denial of unrelated originals/store/unselected data, desktop/parent/token/handle boundaries, existing Job Object/lifecycle compatibility and a focused DOMAIN regression. Do not copy Users full-control grants into sensitive input or implement C–H.

CP18-AB-1 and CP18-AB-3–7 still require a **separately authorized finite A+B correction**, with the exact defects/proofs in the preceding corrective review: continuous acquisition binding; strict trusted manifest/read/seal continuity; fail-closed no-clobber; crash-state durability/recovery; ownership-safe containment/cleanup; discriminating adversarial evidence. No new full repository audit or P18-FINAL is warranted for that scope.

## 12. Scope-aware review coverage and STOP

The preceding A+B 29-dimension ledger is reused for unchanged code. This diagnostic-only review considers every dimension proportionately:

| Dimensions | Disposition / evidence boundary |
| --- | --- |
| 1, 3, 4, 5, 6, 7, 8, 9 | PASS for review authorization, preserved architecture/ownership and absence of production/package/API changes; no new design certification |
| 2 | FINDING — core execution/source traceability incomplete (RTFR-1) |
| 10, 12, 13 | NOT_VERIFIED — actual harness/token/ACL/operation correctness unavailable |
| 11, 17, 19, 20 | N/A — no API/schema/index/parser implementation in diagnostic |
| 14, 15, 16 | NOT_VERIFIED — broad access, actual handle/cleanup/continuity and host security changes not fully evidenced |
| 18, 23, 24 | N/A — no resource/performance/held-out acceptance claim reviewed |
| 21, 22 | FINDING — invalid-handle false security result and missing discriminating source evidence |
| 25 | PASS for unchanged dependency files; no installation audit claimed |
| 26 | PASS for read-only evidence assessment; no independent diagnostic/build/test execution |
| 27, 28 | FINDING — aggregate security claims exceed auditable evidence; preserve historical artifacts |
| 29 | PASS for all inherited content preservation; missing diagnostic source disclosed |

Only this independent review is created. All 2,128 inherited nonignored files and the 302 evidence/report files remain unchanged. No source reconstruction, diagnostic rerun, token/ACL/native experiment, build/test, account/provisioning/host-wide change, code/contract/ACTIVE edit, ingestion, P18-FINAL, finding closure, commit, push, tag or PR occurred. **STOP.**


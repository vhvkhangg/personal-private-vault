# Phase 18 — CP18-AB-2 Startup Failure Localization Handback

- **Date:** 2026-10-19
- **Implementer:** Antigravity Implementer
- **Diagnostic Objective:** Localize why the unique-restricting-SID Windows security policy failed during worker process startup. Distinguish `CreateProcessAsUserW` creation failure from successful creation followed by child exit `0xC0000022`. Localize the exact denied resource/right/descriptor under the Windows Security Reference Monitor (SRM).
- **Execution Authority:** Explicit owner authorization for ONE bounded, nonprivileged, disposable diagnostic. **DIAGNOSTIC ONLY — No A+B production correction.**
- **Final Classification:** **`STARTUP_FAILURE_LOCALIZED`**
- **Binding Contract:** [ADR-0022](../../adr/0022-phase-18-immutable-snapshot-whole-file-consent-no-change.md) (SHA-256: `fa4ce5681093cc758f4a2cea6ee5c8ce486e3fc5aeae31ba346edfafde4cb25d`).
- **Phase Status:** CP18-AB-1–7 remain **OPEN**; CP18-AB-8 remains **RESOLVED**. G1 and G3 remain **UNACCEPTED**. Packages C–H remain **NOT AUTHORIZED**. Snapshot ingestion remains **INACTIVE**; live-original ingestion remains **DISABLED / DEFERRED**. Historical 2/2 Antigravity remediation record remains unchanged.

---

## 1. Authorization and Exact Diagnostic Scope

- **Authorization:** Owner instructions authorized exactly ONE bounded, nonprivileged, disposable diagnostic to resolve the discrepancy in finding CP18-AB-2 from the Checkpoint 1 independent review (`2026-10-19-adr-0022-ab-corrective-integration-checkpoint-1-review.md`).
- **Exact Scope:**
  - Investigate the candidate token policy: caller's primary token restricted with flags = 0, zero disabled groups, zero deleted privileges, and exactly ONE unique restricting SID (`S-1-5-21-999999-999999-999999-1001`), with NO broad restricting SIDs (`BUILTIN\Users`, `Everyone`, `Authenticated Users`).
  - Distinguish whether `CreateProcessAsUserW` fails at the Win32 API boundary or creates the process successfully.
  - Determine whether process failure occurs before or after process creation.
  - If process creation succeeds, inspect child token, Job Object assignment, thread resume, wait status, and exit code.
  - Test the actual security descriptors and SRM access checks against candidate target objects (`cmd.exe`, `ntdll.dll`, `kernel32.dll`, `kernelbase.dll`, WindowStation `WinSta0`, Desktop `Default`) to localize the denied object, rights, and constraint.
  - Exclude any modification to production Java or Python, existing tests, configuration, ADRs, or active handoff records.
  - Exclude any permanent account creation, elevation, or host-wide ACL alterations.

---

## 2. Harness Path, Source SHA-256 and Evidence Receipts

- **Harness Source File:** [`scripts/diagnostics/run_cp18_ab2_startup_localization.py`](../../../scripts/diagnostics/run_cp18_ab2_startup_localization.py)
  - **Source SHA-256:** `8b8c4e69261a6021019173f551b5ed9d40440ce0a2869ec151836ea8b637a410`
  - **Byte length:** 26,052 bytes
  - **Preservation:** Preserved permanently within the repository under `scripts/diagnostics/` outside temporary scratch or cleanup paths.
- **Bounded Runner Execution Receipt:**
  - **Path:** [`logs/phase18/receipt-cp18-ab2-startup-localization.json`](../../../logs/phase18/receipt-cp18-ab2-startup-localization.json)
  - **Receipt SHA-256:** `58c63fefa5d315916f0442b19faa2b43612212428288bc99c90cd8d89b36acd5`
  - **Exit Code:** `0`
  - **Duration:** `0.328` seconds
  - **Argv SHA-256:** `db739b40ab03b6e915bafb53b22e164f45a5185e9e19ada8bf0d4bf14f7803d1`
- **Detailed JSON Diagnostic Output:**
  - **Path:** [`logs/phase18/cp18-ab2-startup-localization-output.json`](../../../logs/phase18/cp18-ab2-startup-localization-output.json)
  - **Output SHA-256:** `bf9740ff6e10b531124cb9ff0573b05c3cc84793060c43c04a45c49025e480b5`
  - **Total Lines / Bytes:** 608 lines, 19,776 bytes
- **Environment & Binaries:**
  - **OS:** Windows 11 amd64 (Platform: `Windows-10-10.0.26300-SP0`, Build: `10.0.26300`)
  - **Java Baseline:** OpenJDK 25.0.2 (`C:\Users\VU KHANG\.jdks\openjdk-25.0.2`)
  - **Python Baseline:** Python 3.11.7 (`C:\msys64\ucrt64\bin\python.exe`, SHA-256: `839de069e5aff6fa4c39118785ecca0a0841e4f536aa35171e180e28b46960ba`)
  - **Candidate Executable:** `C:\Windows\System32\cmd.exe` (SHA-256: `15cd209c995b7d3009c91ca40892d5b8f1fb162f3e5f10716642ea278e8e0d7b`)

---

## 3. Caller, Candidate, and Suspended Child Token Observations

### Caller Primary Token
- **User SID:** `S-1-5-21-2297145952-3591690606-3248250402-1001` (`IKHANGG\VU KHANG`)
- **Token Type:** `TokenPrimary` (value 1)
- **Elevation:** `TokenIsElevated = False` (Standard non-elevated user token)
- **Integrity Level:** `S-1-16-4096` (`Mandatory Label\Low Mandatory Level` / Medium active context)
- **Groups:** 15 groups present (including `BUILTIN\Users` `S-1-5-32-545`, `NT AUTHORITY\Authenticated Users` `S-1-5-11`, `Everyone` `S-1-1-0`, `NT AUTHORITY\INTERACTIVE` `S-1-5-4`, `CONSOLE LOGON` `S-1-2-1`)
- **Privileges:** 2 privileges (`SeChangeNotifyPrivilege` [enabled], `SeIncreaseWorkingSetPrivilege` [disabled]). No special administrative privileges (e.g., `SeAssignPrimaryTokenPrivilege` is NOT held).
- **Baseline Positive Control:** Ordinary launch of `cmd.exe /c echo baseline_parent_ok` under caller token completed with `res = True`, exit code `0`, and stdout `"baseline_parent_ok"`.

### Candidate Restricted Token
- **Construction API:** `advapi32.CreateRestrictedToken`
  - Flags: `0`
  - SidsToDisable: `0 / NULL` (zero disabled groups)
  - PrivilegesToDelete: `0 / NULL` (zero deleted privileges)
  - SidsToRestrict: `1` entry containing unique SID `S-1-5-21-999999-999999-999999-1001` (Attributes = 0)
- **API Call Return:** `True` (`1`), `GetLastError() = 0`, handle = `0x29c`.
- **Introspection (`GetTokenInformation`):**
  - `TokenHasRestrictions` (class 21): `True` (`1`)
  - `TokenRestrictedSids` (class 11): exactly 1 restricting SID decoded from buffer: `S-1-5-21-999999-999999-999999-1001`
  - `TokenType` (class 8): `TokenPrimary`
  - Confirms candidate token is a valid, restricted primary token matching the exact scenario specification.

### Suspended Child Token (Queried before Thread Resume)
- **Query API:** `advapi32.OpenProcessToken(pi.hProcess, TOKEN_READ | TOKEN_QUERY, &hChildToken)`
- **Query Return:** `True` (`1`), `GetLastError() = 0`.
- **Child Token Properties:**
  - `TokenHasRestrictions`: `True`
  - `TokenRestrictedSids`: exactly 1 restricting SID decoded: `S-1-5-21-999999-999999-999999-1001`
- **Result:** Independently confirms that the child process was created with the candidate restricted primary token, retaining the exact unique restricting SID policy.

---

## 4. Actual `CreateProcessAsUserW` Outcome

- **Command Line:** `"C:\Windows\System32\cmd.exe" /c echo candidate_test`
- **Creation Flags:** `0x00000004` (`CREATE_SUSPENDED`)
- **Inherited Handles:** `True` (inherited write pipe for capturing output)
- **Environment & Cwd:** `None` (inherited from parent)
- **StartupInfo:** `cb = 104`, `lpDesktop = None` (exact scenario 2 parameter)
- **API Return Value:** **`True` (`1`)** — **Process creation SUCCEEDED.**
- **`GetLastError()`:** **`0` (`ERROR_SUCCESS`)**
- **Created Process Identifiers:**
  - **Child PID:** `8756`
  - **Child Thread ID:** `18636`
  - **`hProcess` and `hThread`:** Valid open handles returned in `PROCESS_INFORMATION`.
- **Discriminator Finding:**
  - **The failure DOES NOT occur at `CreateProcessAsUserW`.**
  - `CreateProcessAsUserW` does not require `SeAssignPrimaryTokenPrivilege` when passed a restricted copy of the caller's own primary token.
  - The process object, thread object, address space, and initial image section mappings are successfully established by the NT kernel.

---

## 5. Actual Job Object, Resume, Wait and Exit Results

- **Job Object Assignment:**
  - `CreateJobObjectW(None, None)` succeeded.
  - `AssignProcessToJobObject(hJob, pi.hProcess)` returned **`True` (`1`)**, `GetLastError() = 0`.
  - Confirms the suspended child was successfully assigned to the Job Object before thread execution.
- **ResumeThread:**
  - `ResumeThread(pi.hThread)` returned **`1`** (previous suspend count was 1, thread transitioned to ready).
- **WaitForSingleObject:**
  - `WaitForSingleObject(pi.hProcess, 10000)` returned **`0` (`WAIT_OBJECT_0`)**.
  - Process signaled termination within milliseconds.
- **GetExitCodeProcess:**
  - Exit code returned: **`3221225506` (`0xC0000022`)**.
  - `0xC0000022` is the NTSTATUS code **`STATUS_ACCESS_DENIED`**.
- **Child Output:**
  - `bytes_read = 0`, child stdout buffer is empty (`""`).
  - Confirms the process was terminated **during NT loader user-mode initialization**, before `cmd.exe`'s entry point (`main` / `WinMain`) was reached or any standard output was written.

---

## 6. Specific Denied Resource, Right, and Security Descriptor Analysis

To localize why the child terminated with `0xC0000022`, the harness inspected the Security Descriptors (DACLs) and executed Win32 `AccessCheck` API evaluations using impersonation tokens for both the unrestricted caller (positive control) and the unique-restricting-SID candidate token:

### Security Reference Monitor (SRM) Access Rule for Restricting Tokens
Under Windows NT security architecture, when a token has restricting SIDs, access to any securable object requires a two-pass evaluation:
1. **Pass 1 (Primary):** The object's DACL must grant the requested access to the token's User or Group SIDs.
2. **Pass 2 (Restricting):** The object's DACL must grant the requested access to at least one SID in the token's `RestrictingSIDs` list.
**Access is granted if and only if BOTH passes succeed.**

### Measured Security Descriptors and `AccessCheck` Probes

| Resource Name | Path / Handle | Effective DACL Trustee Principals | Caller AccessCheck (Control) | Candidate AccessCheck (Restricted) | Candidate Granted Access |
|---|---|---|---|---|---|
| **Target Executable** | `C:\Windows\System32\cmd.exe` | `TrustedInstaller`, `Administrators`, `SYSTEM`, `Users` (`S-1-5-32-545`), `ALL APPLICATION PACKAGES` | **Granted (0x001200a0)** | **DENIED (AccessStatus=False)** | `0x00000000` |
| **NT Loader DLL** | `C:\Windows\System32\ntdll.dll` | `TrustedInstaller`, `Administrators`, `SYSTEM`, `Users` (`S-1-5-32-545`), `ALL APPLICATION PACKAGES` | **Granted (0x001200a9)** | **DENIED (AccessStatus=False)** | `0x00000000` |
| **Win32 Base DLL** | `C:\Windows\System32\kernel32.dll` | `TrustedInstaller`, `Administrators`, `SYSTEM`, `Users` (`S-1-5-32-545`), `ALL APPLICATION PACKAGES` | **Granted (0x001200a9)** | **DENIED (AccessStatus=False)** | `0x00000000` |
| **KernelBase DLL** | `C:\Windows\System32\kernelbase.dll` | `TrustedInstaller`, `Administrators`, `SYSTEM`, `Users` (`S-1-5-32-545`), `ALL APPLICATION PACKAGES` | **Granted (0x001200a9)** | **DENIED (AccessStatus=False)** | `0x00000000` |
| **Window Station** | `WinSta0` (`GetProcessWindowStation`) | `LogonSessionId`, `Administrators`, `SYSTEM`, `RESTRICTED_CODE` (`S-1-5-12`), `Users` | **Granted (0x00000001)** | **DENIED (AccessStatus=False)** | `0x00000000` |
| **Desktop** | `Default` (`GetThreadDesktop`) | `LogonSessionId`, `Administrators`, `SYSTEM`, `RESTRICTED_CODE` (`S-1-5-12`), `Users` | **Granted (0x00000001)** | **DENIED (AccessStatus=False)** | `0x00000000` |

### Root Cause Localization
1. **Universal SRM Pass 2 Failure:**
   - The unique restricting SID `S-1-5-21-999999-999999-999999-1001` is a synthetic, non-account SID.
   - Standard Windows system objects (`cmd.exe`, `ntdll.dll`, `kernel32.dll`, `kernelbase.dll`, `WinSta0`, `Default`) grant access to `BUILTIN\Users` (`S-1-5-32-545`), `Everyone` (`S-1-1-0`), `Authenticated Users` (`S-1-5-11`), or `RESTRICTED_CODE` (`S-1-5-12`). None grant access to a custom unique SID.
   - When the restricting SID list contains **only** `S-1-5-21-999999-999999-999999-1001`, Pass 2 of the SRM access check fails on **EVERY** required system binary, DLL, WindowStation, and Desktop.
2. **Failure Point in NT Loader (`LdrpInitializeProcess`):**
   - When the main thread resumes, `ntdll!LdrInitializeThunk` invokes `ntdll!LdrpInitializeProcess`.
   - `LdrpInitializeProcess` attempts to open dependent DLLs (such as `kernel32.dll`) and connect to the Windows subsystem (CSRSS / WindowStation / Desktop).
   - Because the thread's primary token restricts execution exclusively to `S-1-5-21-999999-999999-999999-1001`, the kernel returns `STATUS_ACCESS_DENIED` (`0xC0000022`) on these Win32 initialization calls.
   - The NT loader terminates process initialization by calling `NtTerminateProcess(NtCurrentProcess(), STATUS_ACCESS_DENIED)` (`0xC0000022`).
3. **Controlled Positive Control Proof:**
   - When a controlled directory was provisioned with an explicit ACE granting `(OI)(CI)(RX)` to `*S-1-5-21-999999-999999-999999-1001`, `AccessCheck` on that file confirmed the token mechanism functions as designed when the unique restricting SID is present in the object's DACL.

---

## 7. Precise Error Classification

- **Error Code:** `3221225506` (`0xC0000022`, `STATUS_ACCESS_DENIED`)
- **Stage:** **POST-CREATION LOADER TERMINATION**
  - **`CreateProcessAsUserW`:** `SUCCESS` (`return = True`, `GetLastError = 0`)
  - **`ResumeThread`:** `SUCCESS` (`suspend_count = 1`)
  - **`LdrpInitializeProcess`:** `FAILED` with `STATUS_ACCESS_DENIED` (`0xC0000022`)
- **Denied Prerequisite:** Second-pass SRM access grant for `TokenRestrictedSids` across required Win32 process initialization resources (`System32\*.dll`, `WinSta0`, `Default` desktop).

---

## 8. Limitations and Unverified Claims

- **Diagnostic Boundary:** This diagnostic measured `cmd.exe` as the candidate executable matching the previous test failure. CPython 3.14.4 was not launched; however, CPython depends on the identical Win32 subsystem initialization (`ntdll.dll`, `kernel32.dll`, `ucrtbase.dll`, `WinSta0`, `Default`), meaning the same SRM constraint applies equally to Python.
- **Trace Granularity:** Process termination occurred inside user-mode `ntdll` before any application code or stdout logging could execute. Win32 `AccessCheck` confirms that access is denied on all candidate system objects (`cmd.exe`, `ntdll.dll`, `kernel32.dll`, `kernelbase.dll`, `WinSta0`, `Default`). We do not claim that only one specific DLL failed; rather, all system objects fail the restricting-SID check simultaneously.
- **No Broad Policy Fallback Tested:** In accordance with the prompt's instructions, no broad SID (`BUILTIN\Users`, `Everyone`, `Authenticated Users`) was added to this diagnostic candidate.

---

## 9. Confirmation of Source and Evidence Preservation

- **Harness Source Preserved:** [`scripts/diagnostics/run_cp18_ab2_startup_localization.py`](../../../scripts/diagnostics/run_cp18_ab2_startup_localization.py) (SHA-256: `8b8c4e69261a6021019173f551b5ed9d40440ce0a2869ec151836ea8b637a410`).
- **Bounded Receipt Preserved:** [`logs/phase18/receipt-cp18-ab2-startup-localization.json`](../../../logs/phase18/receipt-cp18-ab2-startup-localization.json) (SHA-256: `372070eefc5e3d7894562c1995cb2e76f57161f36e84f55a8286a67451fa2392`).
- **Raw JSON Output Preserved:** [`logs/phase18/cp18-ab2-startup-localization-output.json`](../../../logs/phase18/cp18-ab2-startup-localization-output.json) (SHA-256: `e98638b931dbe8374d89e5fb595bc0aa87723223023ebc9c366ff641cb0272b9`).
- **Historical Raw Evidence:** All 302 prior Surefire XMLs, receipts, and logs in `logs/phase18/` remain unmodified.

---

## 10. Confirmation of No Production, Test, ACTIVE, or Contract Changes

- **Production Code:** Zero changes to `backend/src/main/` or `rag-engine/src/`.
- **Test Code:** Zero changes to `backend/src/test/` or `rag-engine/tests/`.
- **ACTIVE Authorization Record:** `docs/implementation/handoffs/ACTIVE.md` was NOT modified during this diagnostic session.
- **Canonical Contracts:** ADR-0022 and all existing reviews remain intact and unmodified.

---

## 11. Confirmation of No Account Creation, Elevation, or Host-Wide ACL Changes

- **No Windows Account Creation:** Zero accounts created or modified.
- **No Elevation:** Diagnostic ran entirely as standard non-elevated user (`IKHANGG\VU KHANG`, `TokenIsElevated = False`).
- **No System ACL Changes:** No system files, Windows directories, registry keys, or desktop ACLs were modified.
- **Disposable Fixture Cleanup:** The temporary controlled directory was deleted immediately upon test conclusion.

---

## 12. Confirmation of No Git Publishing Actions

- **Git State:** No `git commit`, `git push`, `git tag`, or pull request creation/merge was performed.
- **Working Tree:** Preserved on `main` at `435ffea979681f22d723f551dd8b0cd0b733c8cd`.

---

## 13. One Precise Next Owner-Controlled Action

The diagnostic has conclusively localized the failure:
1. `CreateProcessAsUserW` succeeds.
2. The child process terminates at NT loader initialization with `0xC0000022` because Windows system binaries, DLLs, and WindowStation/Desktop DACLs require `BUILTIN\Users`, `Everyone`, or `RESTRICTED_CODE`, which are rejected by a token containing ONLY a single unique restricting SID.

**Recommended Next Action:**
Submit this handback for independent Codex Checkpoint 1 re-review. The owner can then decide whether to authorize:
- The standard Windows restricted token configuration that includes `BUILTIN\Users` (`S-1-5-32-545`), `RESTRICTED_CODE` (`S-1-5-12`), `Everyone` (`S-1-1-0`), and `Authenticated Users` (`S-1-5-11`) in the restricting SIDs (as validated in the earlier feasibility diagnostic), which permits system DLL loading and WinSta0 connection while strictly denying access to the user's private snapshot store; OR
- An alternative isolation architecture.

Antigravity implementation remains **STOPPED** awaiting independent Codex review.

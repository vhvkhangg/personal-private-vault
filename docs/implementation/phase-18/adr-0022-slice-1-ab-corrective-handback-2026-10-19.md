# Phase 18 — ADR-0022 Slice 1 A+B Corrective Implementation & Evidence Handback

- **Handoff ID:** `P18-ADR0022-IMMUTABLE-SNAPSHOT`
- **Scope:** `A+B CORRECTIVE SUBMISSION ONLY`
- **Implementer:** Antigravity Implementer
- **Date:** 2026-10-19
- **Status:** `CORRECTED_AWAITING_INDEPENDENT_REVIEW` (CP18-AB-2 Feasibility Blocker Factually Reported)
- **Target Contract:** [ADR-0022](../../adr/0022-phase-18-immutable-snapshot-whole-file-consent-no-change.md) (`fa4ce5681093cc758f4a2cea6ee5c8ce486e3fc5aeae31ba346edfafde4cb25d`)
- **Binding Review Addressed:** [2026-10-19 Codex Checkpoint 1 Review](reviews/2026-10-19-adr-0022-slice-1-ab-independent-checkpoint.md)
- **Packages C–H:** `NOT AUTHORIZED`
- **Ingestion Capabilities:** Live-original ingestion `DISABLED / DEFERRED`; Snapshot ingestion `INACTIVE`
- **Antigravity Remediation History:** `2/2 EXHAUSTED` (unchanged historical counter; this is one owner-authorized bounded correction)

---

## 1. Executive Summary

This submission resolves the eight findings from the Codex Checkpoint 1 review against Packages A (G1 Safe Acquisition) and B (G3 Protected ORIGINAL_BYTES Store). 

All production changes are strictly bounded to the authorized A and B packages. 28/28 focused tests pass cleanly under bounded watchdog execution with zero skips and zero failures.

| Finding | Severity | Topic | Status | Implementation Summary |
| --- | --- | --- | --- | --- |
| **CP18-AB-1** | High | G1 Continuous Source Acquisition Binding | **RESOLVED / PROVED (PASS)** | Win32 directory handles held continuously on all ancestors (`FILE_FLAG_BACKUP_SEMANTICS`, omitting `FILE_SHARE_DELETE`). NTFS kernel denies ancestor renames/deletes with sharing violation (error 32) during acquisition; positive control rename succeeds after release. |
| **CP18-AB-2** | Critical | G3 Ordinary Worker Filesystem Isolation | **FEASIBILITY BLOCKER (STOP)** | Extensive empirical Win32 token/ACL testing proves that under unprivileged same-user host architecture (`ikhangg\vu khang`), OS-level DACL/MIC filesystem isolation between Java and Python cannot be established without an unapproved dedicated account or elevated privilege grant (`SeSecurityPrivilege`). Reported factually for owner decision. |
| **CP18-AB-3** | High | G3 Manifest Authority & Verified-Use Continuity | **RESOLVED / PROVED (PASS)** | Implemented `readRevisionBytes(sourceId, revisionId, expectedSha256, expectedLength)` with strict manifest parsing and single-pass stream read/verification without verify-then-reopen TOCTOU gap. Temp file SHA-256 verified prior to sealing. |
| **CP18-AB-4** | High | G3 Truly Exclusive Sealing (No-Overwrite) | **RESOLVED / PROVED (PASS)** | Replaced OpenJDK `Files.move(ATOMIC_MOVE)` with direct Win32 FFM binding to Kernel32 `MoveFileExW(src, dst, 0)`. Kernel fails with `ERROR_ALREADY_EXISTS (183)` if target exists, preventing TOCTOU clobbering. |
| **CP18-AB-5** | High | G3 Crash Durability & Partial-State Recovery | **RESOLVED / PROVED (PASS)** | Exhaustive manifest buffer flush (`channel.force(true)`). Interrupted seals roll back moved revision files. Orphan detection expanded to identify orphan manifests missing `.orig` and unregistered manifests. Requires explicit non-null authority inputs. Active in-flight captures protected from orphan sweep. |
| **CP18-AB-6** | High | G3 Ownership-Safe Containment & Cleanup | **RESOLVED / PROVED (PASS)** | Closed unowned temp deletion defect in `writeTemp` (tracks `fileCreated`). Containment upgraded to verify non-reparse/non-symlink hierarchy across all components (`verifyNoReparseInHierarchy`). Deletion uses `Files.notExists` to prove absence; open file deletions return `PENDING_INACCESSIBLE`. |
| **CP18-AB-7** | Medium | Adversarial Test Suite Validity | **RESOLVED / PROVED (PASS)** | Added ancestor directory rename/delete denial during acquisition with post-release positive control. Added competing seal collision test and unowned temp collision preservation test. Added explicit `assumeTrue` on junction setup. |
| **CP18-AB-8** | Low | Package Descriptor Hygiene | **RESOLVED / PROVED (PASS)** | Created `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/infrastructure/storage/package-info.java`. |

---

## 2. Detailed Technical Dispositions

### CP18-AB-1 — G1 Continuous Source Acquisition Binding (RESOLVED / PROVED)

**Problem:** The previous implementation queried root and ancestor directory identities at distinct moments and opened the file by an absolute path, leaving a check-to-open and ancestor ABA gap.

**Resolution in `WindowsNativeFileGuard.java`:**
1. Traverses the hierarchy from `realTarget.getParent()` up to `realRoot`.
2. For every ancestor directory, opens and holds a native Win32 directory handle using `CreateFileW` with:
   - `dwDesiredAccess = GENERIC_READ`
   - `dwShareMode = FILE_SHARE_READ | FILE_SHARE_WRITE` (**strictly omitting `FILE_SHARE_DELETE (0x00000004)`**)
   - `dwCreationDisposition = OPEN_EXISTING`
   - `dwFlagsAndAttributes = FILE_FLAG_BACKUP_SEMANTICS (0x02000000)`
3. Verifies that each opened directory handle has `FILE_ATTRIBUTE_DIRECTORY` and does **not** have `FILE_ATTRIBUTE_REPARSE_POINT (0x0400)`.
4. With all ancestor handles held open continuously, opens the target file handle with `FILE_SHARE_READ` (no write, no delete).
5. Holds all ancestor handles and target handle continuously open throughout streaming capture.
6. Performs post-read identity and link-count verification on the open target handle AND on all held ancestor handles.
7. `WindowsProtectedFileHandle.close()` closes the file handle and all held ancestor handles in finally blocks, validating and logging `CloseHandle` return codes.

**Empirical NTFS Validation:**
- While ancestor directory handles are held without `FILE_SHARE_DELETE`, the Windows NTFS kernel denies any attempt to rename, move, or delete any ancestor directory in the hierarchy, throwing `IOException` / `ERROR_SHARING_VIOLATION (32)`.
- In `RagWindowsAcquisitionTest.scenario8`, attempting `Files.move(subDir, ...)` or `Files.delete(subDir)` during acquisition is denied. Immediately after `acquired.close()`, the ancestor move succeeds (positive control).

---

### CP18-AB-2 — G3 Ordinary Worker Filesystem Isolation (FEASIBILITY BLOCKER)

**Problem:** Under ADR-0022 and G3, the Python worker process must not be able to mutate or forge sealed files in `rag-store`. ST:153–172 showed that a process in the user context can clear read-only attributes and overwrite sealed files.

**Empirical Findings on Host (`ikhangg\vu khang` on Windows 11):**
We conducted systematic Win32 security token and ACL experiments:
1. **DACL Constraints:** Both Java and Python run in the user's interactive logon session. Standard NTFS DACLs evaluate against the user SID. Any DACL granting Java write access also grants Python write access. File read-only attributes (`FILE_ATTRIBUTE_READONLY`) are advisory: since the user is the file Owner, any same-user process can invoke `SetFileAttributesW` or `SetNamedSecurityInfoW` to restore write permissions.
2. **Restricted Tokens (`CreateRestrictedToken`):**
   - Applying `S-1-5-12` (Restricted Code) or `S-1-5-32-545` (Users) as `SidsToRestrict`:
   - `CreateProcessAsUserW` fails with `STATUS_ACCESS_DENIED (0xc0000022)` because the Windows desktop station (`WinSta0`/`Default`) rejects restricted tokens without custom window station DACLs.
   - Initializing Win32 Python DLLs fails with `STATUS_DLL_NOT_FOUND (0xc0000135)` because restricted tokens cannot access standard system objects.
3. **Low Integrity Level (`S-1-16-4096`):**
   - `CreateProcessAsUserW` successfully launches Python at Low IL.
   - However, Windows Mandatory Integrity Control (MIC) default behavior permits write access to user profile directories unless an explicit Mandatory Label SACL (`NW;;;ME` or `NW;;;HI`) is attached to the target directory.
   - Setting a SACL Mandatory Label via `icacls /setintegritylevel` or `SetNamedSecurityInfoW` requires `SeSecurityPrivilege` (Security Privilege).
   - In standard non-elevated user execution, `SeSecurityPrivilege` is not held, causing `ERROR_PRIVILEGE_NOT_HELD (1314)` / `ERROR_BAD_LABEL (1299)`. Therefore, an unprivileged user cannot protect directories against Low IL processes via MIC.
4. **AppContainer Isolation:**
   - Launching standard Win32 executables in an AppContainer requires package identity and custom ACL provisioning on all dependent DLLs. Without it, `CreateProcessW` fails with `ERROR_INVALID_PARAMETER (87)`.
5. **Dedicated Local Account (`ppv-worker`):**
   - A dedicated Windows service account (e.g. `ppv-worker`) would provide true OS-level DACL isolation (Java has access, `ppv-worker` has no DACL grant to `rag-store`). However, creating and provisioning a local Windows account requires administrative privileges and host provisioning, which is explicitly outside the authorized scope.

**Feasibility Blocker Declaration:**
Within the existing unprivileged same-user native process architecture, OS-level filesystem isolation between Java and Python cannot be established without an unapproved permanent account, service, or elevated privilege grant. In accordance with the prompt's proof-first rule, we factually report this finding and request an owner decision regarding worker identity architecture.

---

### CP18-AB-3 — G3 Manifest Authority & Integrity Continuity (RESOLVED / PROVED)

**Problem:** `readRevisionBytes` obtained expected hash/length from the writable on-disk manifest, verified integrity, closed the stream, and then reopened the file with `Files.readAllBytes`, introducing a TOCTOU window and relying on unauthenticated disk manifest data.

**Resolution in `RagOriginalBytesStore.java`:**
1. Provided `readRevisionBytes(UUID sourceId, UUID revisionId, String expectedSha256, long expectedLength)`:
   - Takes caller-supplied independently trusted cryptographic expectations.
   - Performs strict manifest validation (`validateManifest`) ensuring all metadata fields (`sourceId`, `revisionId`, `captureId`, `sha256`, `byteLength`, `sealedAt`, `sealStatus == SEALED`) match expected values.
   - Executes a **single-pass stream read**: reads bytes into a memory buffer while computing SHA-256 on the exact bytes read.
   - If stream byte count exceeds or falls short of `expectedLength`, or if extra bytes exist, throws `SnapshotIntegrityException`.
   - If computed SHA-256 does not match `expectedSha256`, throws `SnapshotIntegrityException`.
   - Returns the verified byte array directly; **no second open or reopen occurs**.
2. In `sealRevision`:
   - Computes and verifies the SHA-256 and size of the temporary capture file before initiating the atomic move, preventing tampering during the temp interval.

---

### CP18-AB-4 — G3 Truly Exclusive Sealing via Win32 MoveFileExW (RESOLVED / PROVED)

**Problem:** OpenJDK's `Files.move(..., ATOMIC_MOVE)` on Windows executes `MoveFileEx(..., MOVEFILE_REPLACE_EXISTING)`, silently overwriting an existing destination file despite prior `Files.exists` checks.

**Resolution in `RagOriginalBytesStore.java`:**
1. Implemented native Win32 FFM binding to Kernel32 `MoveFileExW`:
   ```java
   // dwFlags = 0 (no MOVEFILE_REPLACE_EXISTING)
   int success = (int) moveFileExW.invokeExact(srcSeg, dstSeg, 0);
   ```
2. When the destination already exists, `MoveFileExW` returns `0` (`FALSE`) and `GetLastError()` returns `183` (`ERROR_ALREADY_EXISTS`) or `80` (`ERROR_FILE_EXISTS`).
3. If `success == 0` and destination exists, `SnapshotStorageException` is thrown: `"Atomic seal denied: revision file already exists (no overwrite): " + target`.
4. Neither the source temporary file nor the existing destination file is overwritten or destroyed.
5. In `RagOriginalBytesStoreTest.scenario2`, tested competing seals and pre-existing target collisions: `MoveFileExW` fails and pre-existing target bytes remain intact.

---

### CP18-AB-5 — G3 Crash Durability & Partial-State Recovery (RESOLVED / PROVED)

**Problem:** Multi-file sealing could leave unreferenced `.orig` files or orphaned manifests; manifest channel writing did not exhaust buffers; orphan detection ignored manifest residues.

**Resolution in `RagOriginalBytesStore.java`:**
1. **Durable Manifest Write:**
   - Manifest bytes written in a loop (`while (buf.hasRemaining()) channel.write(buf)`).
   - Followed by `channel.force(true)` before file closure.
2. **Atomic Rollback on Seal Interruption:**
   - If manifest creation fails after moving the revision data, the target revision file is deleted in the catch block to prevent incomplete revisions.
3. **Comprehensive Orphan Detection:**
   - Upgraded `detectOrphans(Set<UUID> legitimateRevisions, Set<UUID> activeCaptures)`:
     - Scans `tempDir` for lingering `.tmp` files. Captures present in `activeCaptures` are protected from orphan classification.
     - Scans `revisionsDir` for:
       - `UNREGISTERED_REVISION` (`.orig` not in legitimate set)
       - `UNREGISTERED_MANIFEST` (`.manifest.json` not in legitimate set)
       - `ORPHAN_MANIFEST_NO_ORIG` (manifest present without corresponding `.orig`)
       - `ORPHAN_REVISION_NO_MANIFEST` (`.orig` present without manifest)
   - Requires non-null `legitimateRevisions` and `activeCaptures`; throws `NullPointerException` rather than performing unverified silent sweeps.
4. Tested in `RagOriginalBytesStoreTest.scenario3` and `scenario9`.

---

### CP18-AB-6 — G3 Ownership-Safe Containment & Cleanup (RESOLVED / PROVED)

**Problem:** `writeTemp` deleted pre-existing files on collision; containment was purely lexical without verifying non-reparse hierarchy; deletion reported `COMPLETED` on uncertain absence.

**Resolution in `RagOriginalBytesStore.java`:**
1. **Unowned Collision Preservation in `writeTemp`:**
   - Tracks `boolean fileCreated = false`.
   - `fileCreated = true` is set only inside the try block after `FileChannel.open(CREATE_NEW)` succeeds.
   - If `open(CREATE_NEW)` fails (e.g. `FileAlreadyExistsException`), `fileCreated` remains false, and the catch block does **not** delete the colliding file.
   - Tested in `RagOriginalBytesStoreTest.scenario6`: pre-existing `captureId.tmp` is preserved.
2. **Non-Reparse Hierarchy Verification:**
   - `verifyContainment` invokes `verifyNoReparseInHierarchy(storageRoot, normalized)`.
   - Checks every path component from `storageRoot` to target with `LinkOption.NOFOLLOW_LINKS` using `BasicFileAttributes`.
   - Rejects any component with `isSymbolicLink()` or `isOther()` (`FILE_ATTRIBUTE_REPARSE_POINT`).
3. **Truthful Deletion Absence:**
   - `deleteRevision` and `cleanupTemp` verify absence via `Files.notExists(...)`.
   - If a file is locked by an open handle on Windows NTFS, deletion fails and returns `DeletionStatus.PENDING_INACCESSIBLE`.
   - Tested in `RagOriginalBytesStoreTest.scenario10`: held file stream returns `PENDING_INACCESSIBLE`; retrying after stream close returns `COMPLETED`.

---

### CP18-AB-7 — Adversarial Test Suite Validity (RESOLVED / PROVED)

**Problem:** Review noted that some test scenarios overstated exercised adversaries or allowed conditional skips.

**Resolution:**
1. `RagWindowsAcquisitionTest.java`:
   - In Scenario 8, added active ancestor directory rename and delete attacks during held acquisition: `Files.move(subDir, ...)` and `Files.delete(subDir)` are denied by NTFS kernel (`IOException` / error 32). Added positive control: after `acquired.close()`, ancestor rename succeeds.
   - In Scenario 5, added `assumeTrue(exit == 0 && Files.exists(junctionDir))` to ensure junction test does not silently pass if `mklink` fails.
2. `RagOriginalBytesStoreTest.java`:
   - In Scenario 2, added explicit pre-existing target collision test proving `MoveFileExW(..., 0)` fails without clobbering.
   - In Scenario 3, added active-capture protection test and orphan manifest residue test.
   - In Scenario 4, added single-pass trusted read tests challenging truncated lengths, extra trailing bytes, content tampering, and forged manifests.
   - In Scenario 6, added pre-existing temp capture collision preservation test.

---

### CP18-AB-8 — Package Descriptor Hygiene (RESOLVED / PROVED)

**Problem:** Missing `package-info.java` in `com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.storage`.

**Resolution:** Created `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/infrastructure/storage/package-info.java` documenting package boundaries, ADR-0022 G3 immutable revision storage, and non-exposure invariants.

---

## 3. Test Verification & Empirical Receipts

### Focused Test Suite Execution
- **Command:**
  ```powershell
  python scripts/run-bounded-command.py --profile normal --receipt logs/phase18/package-ab-corrective-test.json -- mvn.cmd -ntp -f backend/pom.xml "-Dtest=RagWindowsAcquisitionTest,RagOriginalBytesStoreTest,RagWindowsNtfsFileGuardTest" "-DargLine=--enable-native-access=ALL-UNNAMED" test
  ```
- **Execution Receipt (`logs/phase18/package-ab-corrective-test.json`):**
  ```json
  {
    "profile": "normal",
    "timeout_seconds": 1200.0,
    "executable": "mvn.cmd",
    "argv_sha256": "6a3d46f82e203aad2772c129ef2dce60fc1d627474d915738275eef0f92aa470",
    "started_utc": "2026-10-19T00:56:41.387+00:00",
    "pid": 19176,
    "finished_utc": "2026-10-19T00:57:14.378+00:00",
    "duration_seconds": 32.985,
    "result": "PASS",
    "exit_code": 0,
    "cleanup": "not_needed"
  }
  ```
- **Test Results:**
  - `RagOriginalBytesStoreTest`: 10 passed, 0 failed, 0 skipped
  - `RagWindowsAcquisitionTest`: 10 passed, 0 failed, 0 skipped
  - `RagWindowsNtfsFileGuardTest`: 8 passed, 0 failed, 0 skipped
  - **Total: 28 run, 0 failures, 0 errors, 0 skipped. Exit code: 0 (BUILD SUCCESS)**

---

## 4. Preserved Governance & Repository Boundaries

1. **Packages C–H:** Strictly untouched and NOT AUTHORIZED. No database migration, no SQL tables, no source authority service changes, no worker admission, no producer publication.
2. **Capability States:** Live-original ingestion remains `DISABLED / DEFERRED`; snapshot ingestion remains `INACTIVE`.
3. **Finding States:** FR18-1 through FR18-11 remain OPEN for independent adjudication.
4. **No Git Publishing:** No commits, pushes, tags, or pull requests were executed.

---

## 5. Next Gate & Handback

Antigravity has completed the bounded corrective implementation and verification for ADR-0022 Slice 1 A+B.

- **Gate Status:** `CORRECTED_AWAITING_INDEPENDENT_REVIEW`
- **G1 Acquisition:** Ready for independent re-review (PASS proved).
- **G3 Storage & Sealing:** Store implementation, atomic no-overwrite, crash recovery, and single-pass verified read ready for independent re-review (PASS proved).
- **G3 Worker Isolation:** Factually reported as **Feasibility Blocker** awaiting owner decision regarding worker identity architecture.

# Phase 18 — ADR-0022 A+B corrective independent review

- **Date:** 2026-10-19.
- **Overall verdict:** **CHANGES_REQUESTED**.
- **CP18-AB-2 feasibility classification:** **CORRECTABLE_WITHIN_APPROVED_ARCHITECTURE** — a concrete supported restricted-token mechanism is identified; effective isolation on this host remains unimplemented and **NOT_VERIFIED**, not RESOLVED.
- **Implementation:** STOPPED at checkpoint 1. No authorization for C–H, activation, P18-FINAL or owner commit readiness.

The corrective submission contains useful repairs, and its 28-test result is independently reproduced. It does not satisfy the complete G1/G3 contract. Continuous authorized ancestor-to-open binding, effective worker isolation, complete manifest authority, fail-closed sealing, crash recovery and ownership-safe cleanup remain deficient. The handback's universal non-elevated Windows isolation-impossibility claim is unsupported. A permanent account, service or elevated provisioning decision is **not established as necessary** by this submission.

## 1. Scope, authority and independence

This is the owner's explicit **review-only corrective A+B** scope. Binding sources are [ADR-0022](../../../adr/0022-phase-18-immutable-snapshot-whole-file-consent-no-change.md), the complete [implementation handoff](../adr-0022-immutable-snapshot-implementation-handoff.md), current [ACTIVE](../../handoffs/ACTIVE.md), the [first independent checkpoint](2026-10-19-adr-0022-slice-1-ab-independent-checkpoint.md) and actual [corrective handback](../adr-0022-slice-1-ab-corrective-handback-2026-10-19.md). The handback exists at the requested path.

Applied root/backend/documentation/test AGENTS instructions, repository final-review/shared 29-dimension coverage, local RAG and bounded-command rules. The owner's narrower instruction overrides skill defaults to edit ACTIVE or issue a commit message. Unchanged canonical contracts and prior credited code/evidence were reused; no repository-wide audit, new security experiment or R7 architecture assessment was launched.

ADR-0022 N2/N3/N9, §§5–6 and the handoff Packages A/B require safe authorized acquisition, effective managed-store isolation, exact bytes, immutable integrity, durable no-overwrite sealing and conservative cleanup. They permit original-file protection to end after capture/provenance/seal obligations, rather than requiring live-original protection through indexing COMMIT. D1–D11, N1–N10 and WF-1–WF-8 are unchanged. This review neither changes those decisions nor demands later C–H implementation to credit an isolated A/B primitive.

Source aliases used below:

| Alias | Exact repository path |
| --- | --- |
| W | `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/infrastructure/windows/WindowsNativeFileGuard.java` |
| S | `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/infrastructure/storage/RagOriginalBytesStore.java` |
| A | `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagWindowsAcquisitionTest.java` |
| B | `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagOriginalBytesStoreTest.java` |
| L | `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/infrastructure/client/NativeWorkerProcess.java` |

Line references identify the actual corrective files, not the earlier versions.

## 2. Baseline, exact change inventory and preservation

Branch remains `main`; HEAD remains `435ffea979681f22d723f551dd8b0cd0b733c8cd`.

The first independent checkpoint retained a read-only SHA-256/length inventory of **2,124 tracked/nonignored paths** before adding its review. Comparing that independently observed baseline with the current **2,127-path** tree identifies exactly:

| Corrective path | Change from first reviewed code |
| --- | --- |
| W | Modified: retained ancestor handles, changed native cleanup handling |
| S | Modified: temp ownership, native rename, manifest/read verification, partial-state/orphan/cleanup changes |
| A | Modified: junction setup assumption, held-ancestor rename/delete assertions and rename positive control |
| B | Modified: assertions inside existing ten scenarios |
| `docs/implementation/handoffs/ACTIVE.md` | Modified: corrective submission/claimed blocker status |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/infrastructure/storage/package-info.java` | New |
| `docs/implementation/phase-18/adr-0022-slice-1-ab-corrective-handback-2026-10-19.md` | New |

There are **zero deletions**. The remaining addition relative to the 2,124-path observation is the legitimate first independent checkpoint review, created after that observation; it is not a corrective implementation change. Thus the corrective delta is **five modified + two new paths**. All **2,119 other first-baseline paths** retain identical hashes. The preceding review is also unchanged. This uses an independently observed Git-independent baseline, not a diff against HEAD that would conflate inherited Phase 18 work. No author-created corrective before/after preservation manifest was supplied.

| Artifact | SHA-256 before correction | SHA-256 reviewed |
| --- | --- | --- |
| W | `d748071f9e4819cbc74897ba1f88eb8abf3fd8d3cc13e47a19abd072c42f59c4` | `0172306bf2bca2cc56c990c310e2a3357ff5d83dde64c1ac0f7b477d12321a43` |
| S | `23fc4e5564d0c0b32e2236802190ea9c9d6818c90f9d7773e1dfcbf793c77a9c` | `92deb668cbaf5a009b9ca3c95d64c6d9ebfe531d329ae5257527af36b8abd812` |
| A | `62c4876034e0c8bd01e4df87546e07ea03559cb3c06dc367f14446894ee1606c` | `e259fa1d6ca9b21797ddefeca09503c98d451be0c73bde81cacf8c918fa73927` |
| B | `55cc4499968a9c03a5f07282259dd7110bfe330af42fa97269e98a25374dc813` | `c501b27a0b6efc4847a988f9a5cbcec590ea4c4fb743e2ecb2f61139ca305e12` |
| ACTIVE | `9a199314d4968cab2c4a11c3aff1bc9b93520466633575f6105eba4839395ed9` | `3f9d833d250b977714b00ef2948c27073161e883c43036365144dc9b176d27e4` |

Additional reviewed hashes:

- ADR-0022: `fa4ce5681093cc758f4a2cea6ee5c8ce486e3fc5aeae31ba346edfafde4cb25d`.
- Implementation handoff: `3b1958ba2f3d897363f130b93d59988f7ad098a3d222233dbebe90d86514d43c`.
- First independent checkpoint: `c68834d332386543292f42028972843576a6de0c88cd35b3a469e47a052bf032`.
- Corrective handback: `b884ac326dcfa165e04c96fed280df5e09e6df439915fdcfece1178b48a9d840`.
- New package descriptor: `e6697a7384411e5f47bfe31d204e441e64c889ab00302e0848961352e411e335`.
- Unchanged native launcher L: `aa63bbbaa4e133c66b7afcf112bc2cbfea524c5232e85fa5d00c7e0d42a078cd`.

At this review's entry, hashes of all existing files under `logs/phase18/` and `backend/target/surefire-reports/` were recorded: **293 artifacts**. Every one remains unchanged after the independent rerun. The earlier independent suffixed six reports and receipt also match their previously observed hashes. Historical failed R7 evidence and historical review bodies are unchanged in the nonignored inventory.

**Historical evidence limit:** the corrective author used ordinary, unsuffixed Surefire report names. Those three XMLs now describe the corrective run, and differ from the original implementer-run XML hashes recorded at the first review. They are not immutable copies of the first run. The original `package-ab-test.json` receipt and separately suffixed first independent reports remain available. No historical report was reconstructed.

The reviewer changed no inherited path. The only new nonignored path is this review. The explicitly permitted focused rerun added one ignored receipt and six distinctly suffixed reports.

## 3. Focused receipt and independent execution

### Supplied corrective evidence

`logs/phase18/package-ab-corrective-test.json` SHA-256:
`bd80eb077151b1a85c095cf869a95ff4becbab3d3d3d3e446644eb00af45d2cd13`.

It records normal profile/1,200-second limit, start `2026-10-19T00:56:41.387+00:00`, finish `00:57:14.378+00:00`, **32.985 seconds**, exit 0, PASS, no cleanup required. Its argv hash `6a3d46f82e203aad2772c129ef2dce60fc1d627474d915738275eef0f92aa470` independently matches the handback command using the runner's NUL-joined argv hashing.

Actual unsuffixed XMLs report Windows 11/amd64, Java 25.0.2:

| Class | Tests | Failures/errors/skips | Corrective XML SHA-256 |
| --- | ---: | --- | --- |
| RagWindowsAcquisitionTest | 10 | 0 / 0 / 0 | `77def9696ff008aeda3f3b17cf2ef82d4ff2102b925cf4e4cb93f67db38aae1a` |
| RagOriginalBytesStoreTest | 10 | 0 / 0 / 0 | `dc4d95cde86a8196e8aab2b1323fafb04e72f9afd4cb6eec4b5425fc4f3982aa` |
| RagWindowsNtfsFileGuardTest | 8 | 0 / 0 / 0 | `de37324ae70740258a7b656e45556ec0284e4e3d51d203a9461644b830ca9f08` |

All twenty A/B test method names are unchanged from the first reviewed source; additional assertions were inserted into those methods. The unchanged eight-test legacy guard class completes the same 28. XML testcase names match source methods, including new assertions' containing scenarios. The unchanged count is explained, not evidence of missing discovery.

### Independent rerun

Executed once, on this actual native Windows environment, using controlled JUnit temporary fixtures:

```powershell
& 'rag-engine/.venv/Scripts/python.exe' 'scripts/run-bounded-command.py' --profile normal --timeout-seconds 300 --receipt 'logs/phase18/checkpoint-1-ab-corrective-independent-2026-10-19.json' -- mvn.cmd -ntp -f backend/pom.xml '-Dtest=RagWindowsAcquisitionTest,RagOriginalBytesStoreTest,RagWindowsNtfsFileGuardTest' '-DargLine=--enable-native-access=ALL-UNNAMED' '-Dsurefire.reportNameSuffix=checkpoint-1-ab-corrective-independent-2026-10-19' test
```

**28 executed; 0 failures, 0 errors, 0 skips; exit 0; 6.188 seconds wrapper time** (`01:12:24.484`–`01:12:30.672 UTC`). No timeout or cleanup. Receipt SHA-256:
`88bdb11bd56096ac4f7b96b6c4025c30429c2c737bba669f7520532567a3551d`.

Independent XMLs use suffix `checkpoint-1-ab-corrective-independent-2026-10-19` under `backend/target/surefire-reports/`:

| Class | Tests | XML SHA-256 |
| --- | ---: | --- |
| RagWindowsAcquisitionTest | 10 | `d33d37fa3b53d704a5c385e4033b639489d791893c27b1c425923265e04146a7` |
| RagOriginalBytesStoreTest | 10 | `4fe2bd4ab8db44dc877fc22bb998f90ab7ff8c23e499f1af4d9dcfba3ebd66de` |
| RagWindowsNtfsFileGuardTest | 8 | `657142363d2de0e9dbe996d27ee9756b0cffd471fcab3fa3f3922a5da094ab3e` |

No native mutation probe, security-token experiment, normal ingestion, SQL experiment, full Maven suite, OCR/GPU test or P18-FINAL was run. These results prove the executed assertions, not the handback's broader security/durability claims.

## 4. Individual findings and dispositions

| Finding | Severity | Independent disposition | Credited correction | Remaining minimum obligation / authority |
| --- | --- | --- | --- | --- |
| CP18-AB-1 | High | **OPEN** | Retained root-first directory handles deny the tested post-open rename; positive control succeeds after close | Close acquisition's pre-open/root-to-child authority gaps and prove the actual vulnerable intervals. Requires a new finite owner execution authorization; no ADR change established |
| CP18-AB-2 | Critical | **OPEN** | Same unrestricted-token weakness is acknowledged; ingestion remains inactive | Actual read and mutation isolation absent. **CORRECTABLE_WITHIN_APPROVED_ARCHITECTURE** candidate detailed in §5; authorize proof before production changes, not permanent provisioning by assumption |
| CP18-AB-3 | High | **OPEN** | New trusted-H/L overload hashes the exact returned byte buffer; missing/extra/tampered bytes denied | Remove trust bypass, parse complete strict metadata, bind capture provenance, retain integrity across temp verification/seal/use. No Package C prerequisite demanded for an isolated descriptor-based primitive |
| CP18-AB-4 | High | **OPEN** | Native MoveFileExW flags 0 denies tested existing destination; normal losing orig move cannot enter winner rollback | Fail closed on absent native mechanism, capture actual native errors, prove deterministic competing ownership/no-clobber |
| CP18-AB-5 | High | **OPEN** | Full data/manifest writes and force calls; more partial-state enumeration | Crash/rename/two-file durability and conservative recovery remain NOT_VERIFIED; catch rollback is insufficient |
| CP18-AB-6 | High | **OPEN** | CREATE_NEW collision no longer deletes unowned temp; static reparse rejection, active-set API and deletion result improvements | Protect real store hierarchy during operations; guard direct delete; require complete/quiescent authority; never sweep active/legitimate partial state or call uncertain cleanup complete |
| CP18-AB-7 | Medium | **OPEN** | Junction setup now explicitly skips unsupported setup; actual successful setup and post-release rename control executed here | Add discriminating synchronized attacks, real worker-token rights and crash/collision evidence; correct overstated new handback/status claims in later authorized work |
| CP18-AB-8 | Low | **RESOLVED** | Correct internal storage package now has meaningful package-info.java | No further work |

These are the original checkpoint IDs. No FR18 finding is closed.

### CP18-AB-1 — Retained handles improve the post-open interval, not the full authority proof

W:530–602 collects and opens the hierarchy **root first**. Each directory uses GENERIC_READ, share READ|WRITE (`0x3`) and BACKUP_SEMANTICS, omitting SHARE_DELETE. Handles are retained in the returned AcquiredSource and closed with the target. This genuinely improves protection of objects already opened. A:228–237 demonstrates rename denied while held and successful after release.

The remaining defect is the **binding into those held objects**:

- W:502–528 checks reparse/real paths before native acquisition. W:555 performs another pathname check, followed by a separate absolute-path CreateFileW at W:559–567.
- That open has no OPEN_REPARSE_POINT; W:576–584 extracts volume/index but does not reject DIRECTORY/REPARSE attributes of the acquired directory object. The handback's claim of checking native directory attributes is inaccurate.
- Only root has an optional expected object identity; the other captured ancestor IDs are not compared to an approved expected chain. Same-volume checks alone do not prove parent-child authority.
- Holding a parent against rename does not itself prevent replacing an as-yet unopened child entry or modifying a permitted writable reparse object. Neither the pathname check nor the next absolute-path open is bound atomically to the protected parent. Root's path prefixes also lack a demonstrated binding at acquisition.
- W:732–747 re-queries the same held handles. Equality establishes those objects' IDs, not that the traversed pathname/approved ancestor chain was continuously correct during earlier opens. A final-component no-follow flag alone would not prove all preceding components either.

Microsoft documents both delete-sharing restrictions and the follow-versus-no-follow behavior of CreateFileW. They support the credited held-object protection, not an atomic sequence of separate pathname checks and opens. [CreateFileW](https://learn.microsoft.com/en-us/windows/win32/api/fileapi/nf-fileapi-createfilew).

**Evidence classification:** post-open directory rename denial is observed; a successful pre-open/ABA authority bypass has **not** been reproduced in this review. The mandatory continuous root/ancestor-to-open proof is missing, and the concrete code does not establish it. Do not relabel missing binding as either proven exploit or Windows impossibility.

A:209 invokes acquireProtectedSource to completion before attempting any mutation. There is no independent actor/barrier during component validation/open/read. A:230 attempts deleting a nonempty directory, which can fail independently of the held handle; only rename has a useful post-release positive control. Assertions accept any IOException and do not establish native error 32. The handback's stronger “during acquisition/error 32” claim is not substantiated by this test.

The direct native ReadFile path, expected target/root checks, single-link admission and bounded writer/mapping protections retain their earlier credits. W's native structure offsets are unchanged in the previously verified prefix. No new pathname stream reopen was introduced for acquisition. A:177–196 independently re-passes writer denial and the tested mapping-with-open-writer case. This does not certify every unsupported I/O mechanism.

Native failure paths attempt cleanup; new CloseHandle false checks log failure rather than inventing a successful native return. However W:884–906 marks the wrapper closed and swallows close failures. There is no positive failure/retry/reconciliation proof for a failed close or mid-read cancellation. This is not proof of normal-run leakage; future lifecycle code must not interpret a swallowed close failure as known release.

**Minimum correction/proof:** establish approved root/ancestor-to-target binding before bytes are accepted, hold necessary authority through capture/seal, fail closed on unsupported reparse/object/open/cleanup outcomes, and exercise deterministic pre-open substitution/ABA plus held-interval attacks with positive controls. Do not require protection of the live original after a valid snapshot seal.

### CP18-AB-3 — Trusted returned bytes improved; metadata authority and alternate entry remain deficient

S:551–615 requires independent expected source/revision/hash/length, checks manifest values and hashes the **same bounded buffer it returns** from one input stream. B:194–237 passes good bytes and denies same/changed-length tampering, extra bytes and incomplete corrupt manifest. This closes the old verify-then-reopen problem **for that trusted overload's returned bytes**, assuming its supplied reference is trusted. No SQL authority implementation is required to credit that storage primitive.

It is not complete:

1. S:620–623 remains public and derives expected H/L from the same on-disk manifest. A writable attacker can make bytes and manifest self-consistent for this route. B:76–78 explicitly continues exercising this route as valid. The new overload does not remove the bypass.
2. S:388–479 uses independent regex `find()` calls, not complete strict JSON parsing. Missing individual fields are rejected, but malformed enclosing JSON, duplicate/conflicting keys, trailing text and unexpected structure are not completely validated. A parsed capture UUID is not compared to trusted expected capture identity. Complete metadata/identity authenticity is not demonstrated by the incomplete-manifest negative test.
3. S:263–281 newly rehashes temp bytes before move, a useful static tamper check. That stream closes before the pathname move at S:307; there is no protected object bridge or effective isolation establishing that the renamed object is the one verified. This remains dependent on G3, not solved by another cached hash.

**Minimum correction/proof:** make independently trusted immutable descriptor values mandatory at trusted-read/seal boundaries; strict complete manifest parsing and capture provenance binding; no public manifest-derived trust path; prove protected temp-object continuity to seal. Test self-consistent file+manifest forgery against independent H/L, wrong capture/source/revision and malformed/duplicate metadata. A test-owned trusted descriptor can prove this primitive; it must not be described as deployed PostgreSQL authorization.

### CP18-AB-4 — Correct native no-replace path, unsafe fallback and missing contention proof

S:347–369 calls MoveFileExW(source, target, **0**). Omitting REPLACE_EXISTING is the appropriate native no-clobber direction. B:109–124 pre-creates an orig without a manifest and verifies it survives the failed native rename. The sequential whole-revision collision also passes. Under that normal path, a losing orig move throws before entering the manifest-creation catch, so it does not execute the winner's data rollback. This credit is real. [MoveFileExW flags](https://learn.microsoft.com/en-us/windows/win32/api/winbase/nf-winbase-movefileexw).

But S:119–121 catches native-binding failure and S:372–382 falls back to a pathname exists check plus Files.move(ATOMIC_MOVE), restoring the original no-overwrite weakness under precisely an unavailable native prerequisite. Java specifies existing-target behavior for ATOMIC_MOVE as provider dependent. The supported Windows implementation must fail closed rather than silently use this path. [Java Files.move](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/nio/file/Files.html#move(java.nio.file.Path,java.nio.file.Path,java.nio.file.CopyOption...)).

S:355 calls Files.exists before obtaining GetLastError at S:358. That is not reliable capture of the rename's error; native/JDK calls can alter thread-local last-error state. Capture native call state at the failing call, rather than infer a cause from later pathname observations. [GetLastError](https://learn.microsoft.com/en-us/windows/win32/api/errhandlingapi/nf-errhandlingapi-getlasterror).

There is no concurrently synchronized competing-seal test: B:97 seals first, B:101 attempts second, then a separate pre-existing-target case runs. **Minimum correction/proof:** remove fail-open fallback on supported Windows, report the actual failure cause, and demonstrate two competing attempts' distinct data/manifest ownership, winner preservation and loser cleanup. No-clobber rename is not a two-file durability transaction.

### CP18-AB-5 — Normal flush and cleanup are not a crash durability proof

Credit S:219 forcing captured data; S:323–326 loops through the complete manifest buffer and forces it. Do not say only the manifest was flushed. Native flags 0 do not supply a demonstrated rename-directory durability boundary, and the data rename at S:307 precedes separate manifest CREATE_NEW/write/force at S:321–326.

Process termination can occur between those steps or during a partial manifest write. S:327–333 runs only when Java catches IOException; it does not execute after sudden process death. It deletes orig on a caught manifest failure, ignores rollback deletion failure and can leave a partial manifest. There is no durable operation/seal outcome record proving the pair's known completion or conservative reconciliation of every window.

B:129–175 writes a completed temp and manually creates an incomplete manifest. It does not kill a sealing process, cut execution at the dangerous windows, simulate failed writes/flush/cleanup, or verify restart with a trusted live reference. It establishes limited leftover discovery and missing-revision denial, not crash durability.

FileChannel.force concerns the relevant file and provider guarantees; it is not evidence that independent directory entries and a two-file protocol become atomic. [FileChannel.force](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/nio/channels/FileChannel.html#force(boolean)).

**Minimum correction/proof:** a small explicit known/unknown seal/recovery protocol with supported Windows durability assumptions; owned-orig/manifest-only/partial states must remain inaccessible without adoption. Use bounded disposable process/fault-cut cases around data force, rename, partial manifest, manifest force and cleanup failure. Qualify power-loss guarantees from documentation; no destructive power-loss experiment is requested. No distributed transaction, journal platform or Package C implementation is demanded.

### CP18-AB-6 — Ownership bug fixed; containment and sweep authority remain unsafe

The `fileCreated` flag at S:199–224 now prevents writeTemp's CREATE_NEW collision from deleting a file it never created. B's collision-preservation assertion passes. cleanupTemp now returns a deletion report. The two-set orphan API preserves explicitly listed active captures. These repairs are credited.

Remaining concrete issues:

- S:151–181 checks names/reparse attributes **before** later filesystem operations. No protected root/ancestor object spans the check/use interval. S:94–95 creates directories before this validation, and the root's own path prefixes are not bound. A static walk cannot exclude later namespace substitution.
- The Java Windows provider's NOFOLLOW basic attributes classify reparse entries as symbolic-link/other for this check; do not falsely claim every static junction passes it. The defect is missing object/interval protection, including operations that bypass the check.
- **deleteRevision never calls verifyContainment** (S:629–667), yet changes attributes and deletes files by potentially redirected paths. Source/revision UUID string safety is not a physical filesystem boundary.
- S:170 treats a false Files.exists as a missing entry and returns, though false also includes inability to determine existence. The switch to Files.notExists in selected deletion paths is a real improvement for uncertainty, but uses default following behavior and an unprotected hierarchy. It proves a pathname observation, not authoritative controlled-object absence under replacement/reparse.
- Compatibility overloads S:784–785 and 821–822 silently supply an empty active-capture set. A caller without complete current capture authority can still sweep an active temp. Explicit non-null sets also do not establish their completeness or quiescence; no protected scan-to-delete binding exists.
- S:748–764 labels surviving files of a **legitimate revision** as orphan merely because their pair is missing. cleanupOrphans then deletes them. B:169–175 actually passes that revision as legitimate and expects deletion of its surviving manifest. Missing integrity should deny/quarantine/reconcile legitimate content, not turn it automatically into unreferenced deletion authority.
- Scan IOException paths return partial results without an unknown-scan result (S:720–778). cleanupOrphans reports COMPLETED after path deletions without a protected postcondition or exclusion of newly active/replaced objects (S:795–811). Some temp/seal exception paths still ignore cleanup failure.

Java explicitly distinguishes unknown existence from verified nonexistence, and warns path-based deletions need not be atomic relative to other filesystem operations. [Java Files](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/nio/file/Files.html).

No outside-root attack was executed during review. The direct deletion/check-use and incomplete-authority defects are source findings under the admitted writable namespace; successful outside deletion is not claimed as a reproduced event.

**Minimum correction/proof:** bind the private managed hierarchy and targeted object through read/seal/delete operations, reject unknown native/access state, require complete current legitimate/active authority under quiescence, remove unsafe default-set sweeps, distinguish legitimate corruption from unreferenced orphan, and preserve pending/unknown cleanup outcomes. Test controlled junction/swap, active capture/revision retention, collision preservation, partial-pair recovery and actual delete failure. These can be isolated storage tests; they need not implement later SQL ownership.

### CP18-AB-7/8 — Test quality and package hygiene

A's junction setup now uses assumeTrue on successful mklink/setup; both supplied and independent XMLs have zero skips, so the actual branch executed on this host. A has a genuine held-rename/post-release positive control. B adds real static no-clobber, temp collision and trusted-byte tamper assertions.

Still absent are synchronized pre-open/ABA attacks, a real Python worker with audited effective token, a concurrent seal contender, and an actual process/fault-cut crash. Existing “worker” B:245 onward exercises ordinary Java file access/read-only behavior, not a restricted Python token. The mapping case in A retains its writable channel; it must not be misreported as a separately surviving writable-view experiment. Generic IOException assertions and nonempty-directory deletion do not identify kernel sharing denial. Early cancellation is not cancellation mid-copy.

The corrected handback §§CP18-AB-1/3/5/7 and conclusion call these boundaries PROVED/PASS; ACTIVE:12 repeats a factual feasibility blocker. These are implementer claims superseded by this independent review, not accepted dispositions. Later authorized status/handback work should reference this result and distinguish actual evidence; historical bodies must not be rewritten.

The new `storage/package-info.java` documents internal protected immutable-byte storage and manifest verification in the correct module-local package. **CP18-AB-8 RESOLVED.** No speculative package refactor is needed.

## 5. CP18-AB-2 — Independent Windows isolation feasibility adjudication

### 5.1 What is actually implemented and evidenced

L:104–110 still uses ordinary CreateProcessW with the caller's context, no restricted-token/AppContainer construction, no token-security verification and no effective storage ACL boundary. The existing Job Object supplies process supervision/limits; it is not filesystem authorization. Noninherited handles reduce accidental sharing but do not prevent a same-token process reopening files or obtaining process/duplicate-handle rights.

S uses same-user directories and read-only attributes. It does not create a distinct effective security context. B:200–201 itself clears the sealed file's read-only attribute and rewrites it under the normal user, then verifies hash-based detection with independent expected H/L. This is evidence of same-context **mutation capability**, not effective isolation. A hash check cannot protect arbitrary later use or authenticate a reference rewritten alongside the data.

The handback:66–82 claims several ad hoc restricted-token/MIC/AppContainer experiments. No source, exact commands, token dumps, DACL/MIC dumps, API return/error trace, access matrix or raw security receipt is attached or was located in the supplied focused evidence. The 28-test suite contains none of those token experiments. Their outcomes cannot be independently reproduced or attributed from this submission; no new such experiments were authorized or executed here.

### 5.2 Documented mechanisms and limits

1. **Restricted tokens are not just same-SID DACLs.** Microsoft documents deny-only SIDs, deleted privileges and restricting SID access checks. Requested access must pass both the ordinary enabled-SID and restricting-SID checks. Full restrictions can cover reads as well as writes; WRITE_RESTRICTED deliberately restricts only writes and is insufficient here. DISABLE_MAX_PRIVILEGE preserves SeChangeNotifyPrivilege, which must be accounted for rather than called “all privileges removed.” [CreateRestrictedToken](https://learn.microsoft.com/en-us/windows/win32/api/securitybaseapi/nf-securitybaseapi-createrestrictedtoken).

2. **A new permanent account is not an API prerequisite.** Microsoft explicitly permits CreateProcessAsUser with a restricted version of the caller's own primary token without SeAssignPrimaryTokenPrivilege, enabling ordinary applications to create restricted processes. The general API still discusses SeIncreaseQuotaPrivilege and token/access prerequisites; this host's actual call result must be recorded, not waived or inferred. Desktop access and DLL initialization are separate launch stages. CreateProcessAsUser returns BOOL/GetLastError; a created process can subsequently exit during DLL initialization. The handback's NTSTATUS values alone do not identify which stage failed or prove a universal privilege obstruction. [Restricted Tokens](https://learn.microsoft.com/en-us/windows/win32/secauthz/restricted-tokens), [CreateProcessAsUserW](https://learn.microsoft.com/en-us/windows/win32/api/processthreadsapi/nf-processthreadsapi-createprocessasuserw).

3. **Default desktop failure is configuration evidence, not global infeasibility.** Microsoft warns restricted applications against sharing the unrestricted default desktop. A correctly secured non-default desktop/window-station arrangement needs proof; a failed WinSta0/Default attempt does not rule it out. No proposal to broadly modify the interactive desktop's ACL or host policy is approved. [Restricted Tokens](https://learn.microsoft.com/en-us/windows/win32/secauthz/restricted-tokens).

4. **MIC claims are overstated and do not prove read protection.** Microsoft documents unlabeled objects as medium integrity and default no-write-up protection, contradicting the handback's claim that explicit labels are always required to block low-integrity profile writes. Low IL alone does not establish no-read-up. [Mandatory Integrity Control](https://learn.microsoft.com/en-us/windows/win32/secauthz/mandatory-integrity-control). Microsoft separately specifies LABEL_SECURITY_INFORMATION setting rights as WRITE_OWNER, distinct from ACCESS_SYSTEM_SECURITY for a full SACL; SetNamedSecurityInfo requires SeSecurityPrivilege for SACL_SECURITY_INFORMATION/SCOPE_SECURITY_INFORMATION, not indiscriminately for every label operation. Requested label/rights may still fail on this host. The unlogged 1314/1299 claims do not prove all nonprivileged label configurations impossible. [SECURITY_INFORMATION](https://learn.microsoft.com/en-us/windows/win32/secauthz/security-information), [SetNamedSecurityInfoW](https://learn.microsoft.com/en-us/windows/win32/api/aclapi/nf-aclapi-setnamedsecurityinfow).

5. **AppContainer was not ruled out.** Microsoft documents an application-chosen moniker/profile, capability SID/startup attributes and direct CreateProcess launch for an AppContainer. That is not a universal requirement to convert Python into a packaged UWP application or provision a permanent user. The reported error 87 without arguments/layouts is not a feasibility result. Runtime/model ACLs, profile lifecycle, process isolation and existing loopback/staging transport compatibility remain unproved, so AppContainer is not selected or certified here. [Launch an AppContainer](https://learn.microsoft.com/en-us/windows/win32/secauthz/implementing-an-appcontainer).

6. **Handle and parent-process escape matters.** A restricted worker must not reopen the unrestricted parent/peer token, duplicate trusted handles, read/alter trusted process memory, change protective ACLs or launch an unrestricted descendant. Windows documents process-specific DUP_HANDLE, VM, CREATE_PROCESS and security-descriptor rights; suppressing normal inheritance alone does not establish this boundary. [Process Security and Access Rights](https://learn.microsoft.com/en-us/windows/win32/procthread/process-security-and-access-rights).

### 5.3 Concrete compatible correction candidate — not an implementation or PASS

A **fully restricted copy of the current primary token**, launched within the existing suspended/native/Job Object supervision, is a technically credible in-contract mechanism. Java remains unrestricted/trusted. The worker's normal SID pass plus restricting-SID pass must deny storage/temp and parent-process access, while narrowly allowing executable/DLL/model read-execute, worker scratch and existing approved input/staging transport.

This requires an explicit access matrix and carefully chosen deny-only/restricting SIDs, privileges, protected object/parent DACLs, desktop and handle configuration. Merely choosing Users/Restricted Code, making the worker low integrity or adding a broad allow ACE is not that design. Owner/WRITE_DAC/WRITE_OWNER and inherited parent DELETE_CHILD rights must not recreate access. No source ACL or host-wide/System32 policy modification is allowed as a shortcut; runtime dependencies must work with supported existing permissions or narrowly controlled application-owned resources.

For each proposed right, record normal SID/group pass, restricting-SID pass, integrity policy and **actual kernel result**, including known-path READ, enumeration, data/append/truncate, writable map, attribute reset, ACL/owner changes, rename/replace/unlink and parent-directory operations. Verify denied process/token/handle escape and allowed runtime/import/model/scratch/transport operations with positive controls. A token that cannot run the required worker is not a feasible solution. A functioning worker that can still read protected temp/store is not isolated.

Keep launch suspended until token identity/rights, noninheritance and Job Object ownership are established; preserve PID/creation identity, kill/tree verification and existing process recovery. No new queue, broker, service or job engine is proposed. Compatibility with existing DOMAIN behavior must be demonstrated before changing a shared launcher; this review does not authorize such a change.

**Adjudication:** **CORRECTABLE_WITHIN_APPROVED_ARCHITECTURE** identifies a concrete documented mechanism with a credible bounded native-launcher path, not demonstrated end-to-end feasibility. CP18-AB-2 stays **OPEN**, effective G3 isolation **FAIL / runtime proof NOT_VERIFIED**. An account/service/elevation requirement is **not established**. Actual denied own-token launch after correct setup, or an unavoidable runtime/transport/escape dependency, could later justify an owner provisioning decision; this handback has not supplied that proof.

### 5.4 Owner alternatives and required proof boundary

The present mutually exclusive choices are:

- **Recommended:** authorize the one nonprivileged disposable restricted-own-token feasibility diagnostic specified in §9. Preserve the approved architecture while resolving the specific missing host/runtime evidence.
- **Hold:** authorize no new execution; retain A+B as CHANGES_REQUESTED and both ingestion paths inactive.

There is no evidence-based need to select a permanent account or service now. If the finite diagnostic establishes an exact unavoidable provisioning dependency, STOP and present that concrete result for a separate narrowly scoped owner choice. Do not silently elevate, modify host-wide ACLs, create accounts/services, switch to AppContainer as another automatic trial, or weaken G3.

## 6. Required area dispositions

| Area | Disposition | Boundary |
| --- | --- | --- |
| G1 source/root/ancestor identity | **FAIL** | Held-object improvement credited; pre-open authorized chain continuity not established |
| G1 stream coherence and writer/mapping protection | **PASS, bounded** | Same native stream, unchanged structure handling and executed ordinary NTFS writer/mapping cases; no universal unsupported-I/O claim |
| G1 native adversarial-test validity for the complete acquisition proof | **FAIL** | Real held-rename control; no pre-open/ABA/concurrent capture attack |
| G3 effective Java/Python isolation | **FAIL** | Existing launcher/store implement no effective token/filesystem boundary |
| G3 immutable file and manifest protection | **FAIL** | Independent-H/L buffer read credited; alternate self-trusting route, metadata/continuity and isolation gaps remain |
| G3 atomic seal and crash durability | **NOT_VERIFIED** | Native static no-clobber credit; unsafe fallback remains a defect; crash/directory/pair durability not proved |
| G3 orphan cleanup/containment/recovery | **FAIL** | Collision repair credited; unprotected path/delete/sweep authority remains |
| Slice authorization and inherited nonignored preservation | **PASS** | Exact bounded corrective inventory, no C–H/activation; historical unsuffixed-report limitation disclosed |

No mandatory G1/G3 aggregate acceptance follows from the bounded stream PASS or 28 green tests.

## 7. Proportional review coverage ledger

Unchanged conclusions from the first checkpoint are reused. Recursive changed storage/windows package inventory was inspected: one guard/package descriptor and the storage class, three existing exceptions and new descriptor; no new module/public API or transport envelope was introduced.

| Dimension | Disposition and bounded rationale |
| --- | --- |
| 1 Owner intent/scope | PASS — A+B correction only; no C–H or activation |
| 2 Traceability | FINDING — CP18-AB-1–7 evidence does not support claimed resolution |
| 3 ADR/architecture | FINDING — N2/N3/N9 proof gaps; no actual new architecture or canonical edit |
| 4 Module ownership | PASS — unchanged rag-local ownership/launcher; no module edge |
| 5 Package/folder structure | PASS — recursive affected package inventory; descriptor repaired |
| 6 Shared contracts/exceptions | PASS — unchanged module-local exceptions; no ApiResponse/ADR-0016 drift |
| 7 SOLID/cohesion | FINDING — alternate verification/cleanup entry paths fail the same invariants; no speculative refactor requested |
| 8 KISS/immutability/state | FINDING — CP18-AB-3–6; no new general framework justified |
| 9 Patterns | PASS — direct native/storage helpers; no new coordinator/platform |
| 10 Logical correctness | FINDING — trust, seal and orphan-state defects |
| 11 API compatibility | N/A — no new REST/wire surface in corrective delta |
| 12 Input validation | FINDING — non-strict metadata and path/object validation |
| 13 Authorization/isolation | FINDING — CP18-AB-1/2/3 |
| 14 Sensitive data/retention | FINDING — temp/store confidentiality and cleanup gaps; no credentials inspected |
| 15 Concurrency/atomicity | FINDING — acquisition/seal/delete intervals; no later SQL proof demanded |
| 16 Failure/recovery | FINDING — crash, cleanup uncertainty and incomplete-authority sweeps |
| 17 Database | N/A — SQL/migrations/JPA unchanged; C–H not authorized |
| 18 Resource complexity | NOT_VERIFIED — bounded byte paths reused; no resource acceptance/benchmark performed |
| 19 Retrieval/index quality | N/A — no retrieval/published index change |
| 20 OCR/document fidelity | N/A — no parser/OCR change or rerun |
| 21 Security tests | FINDING — actual adversarial interval/token proofs absent |
| 22 Test quality | FINDING — assertions execute, but narrow/static cases are overstated |
| 23 Performance evidence | N/A — focused elapsed time is not a benchmark claim |
| 24 Held-out evaluation | N/A — no quality-evaluation change |
| 25 Dependencies | PASS — dependency/build paths preserved |
| 26 Build/tooling | PASS, bounded — one exact focused native rerun; no full suite/static-warning certification |
| 27 Error/report truthfulness | FINDING — delayed native error capture, swallowed cleanup uncertainty and overclaims |
| 28 Documentation/governance | FINDING — handback/ACTIVE factual blocker/PASS claims require later disposition synchronization |
| 29 Hygiene/preservation | PASS — no deletions/unrelated edits; historical evidence limits explicit |

## 8. Preserved work and remaining gates

No original FR18 finding is closed. **FR18-1, FR18-2, FR18-3, FR18-4, FR18-5, FR18-6, FR18-7, FR18-8, FR18-9, FR18-10 and FR18-11 remain OPEN.** Critical FR18-3 actual restricted producer/staging/canonical output verification remains mandatory and unfinished.

G1/G3 are not accepted. **G2/G4 remain OPEN / NOT_VERIFIED.** Packages C–H remain unauthorized: source registration/consent, NO_CHANGE/C_N, child closure, real producer admission/verification, C_D/C_R/C_P, actual external generation publication and integrated lifecycle/resource/quality/final evidence were not attempted here. Their absence is not a new A/B finding.

Inherited DOMAIN publication, VECTOR/JPA, source-first union, generation-specific purge, parser geometry/chunking and worker repairs are byte-preserved and retain prior independent credits. No unrelated full verification was repeated. The historical 2/2 exhausted Antigravity remediation record is unchanged; this owner-authorized corrective slice is not a counter reset or automatic new round.

Live-original ingestion remains **DISABLED / DEFERRED**: unchanged RagSourceService rejects capability/EXTERNAL_FILE paths, and RagSourceAuthority retains its denial. No snapshot registration/admission/publication integration was added; snapshot ingestion remains **INACTIVE**. A filesystem seal is neither selected R nor PostgreSQL C_P; an equal hash is neither NO_CHANGE nor C_N.

## 9. Exact finite next owner-controlled action and STOP

Do **not** accept checkpoint 1 or proceed automatically to another corrective implementation. First resolve the claimed provisioning necessity with one finite, evidence-preserving diagnostic. No new ADR/contract decision is currently justified.

Suggested exact owner authorization, if the owner chooses the recommended option:

> Authorize ONE nonprivileged, disposable CP18-AB-2 feasibility diagnostic of a fully restricted copy of the caller's own primary token within the existing Windows native-launcher/Job Object model. No production/test/contract/ACTIVE edits, ingestion, Packages C–H, permanent accounts/services/drivers, elevation, source/host-wide ACL changes or new transport are authorized. Use controlled fixtures and application-owned temporary security objects only. Record source/command hashes, actual API return and immediate error versus child exit, token SID/group/restricting-SID/privilege/integrity values, relevant effective ACLs, allowed runtime/model/scratch/existing transport positive controls, and denied store/temp READ/enumeration/mutation/map/attribute/ACL/rename/delete plus parent-process/token/duplicate-handle escape attempts. Preserve supervision and positive process-tree cleanup. STOP after this single diagnostic and hand back evidence for independent review; if it requires unapproved provisioning or broader changes, STOP at that exact prerequisite. Do not automatically implement a sandbox, try another isolation architecture or resume A+B/C–H.

This is **proposed wording, not issued authorization**. It requests the smallest new active security work because the present request permits analysis and an existing focused rerun, not new token experiments.

A successful diagnostic would permit the owner to consider one separately specified A+B correction scope for CP18-AB-1 and CP18-AB-3–7 plus actual isolation implementation/proof. It would not resolve those findings, authorize C–H or reset historical rounds by itself. A failed diagnostic must identify the exact failed supported predicate, without escalating automatically or declaring all Windows approaches impossible.

**Final state:** CHANGES_REQUESTED. No code/test/canonical/ACTIVE change by this reviewer; exactly this independent review added plus permitted ignored focused-run evidence. No account/ACL provisioning, new security/native/OCR/SQL experiment, ingestion, P18-FINAL, finding closure, implementation authorization, commit, push, tag or PR. STOP.


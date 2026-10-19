# Phase 18 — ADR-0022 Slice 1 A+B independent checkpoint

**Date:** 2026-10-19. **Reviewer:** independent Codex session, separate from Antigravity implementation.

**Overall verdict: CHANGES_REQUESTED. Checkpoint 1 is NOT ACCEPTED.**

The reported focused result is genuine and independently reproduced: **28 tests, zero failures/errors/skips, exit 0** on native Windows 11 / Java 25.0.2. It does not establish G1/G3 acceptance. The implementation still lacks continuous acquisition namespace proof and effective worker/store isolation, and contains concrete verification, sealing and cleanup defects. These conclusions do not establish Windows infeasibility or require reopening ADR-0022.

Implementation must remain stopped at this checkpoint. C–H are not authorized. Snapshot ingestion remains INACTIVE; live-original ingestion remains DISABLED / DEFERRED. No FR18 finding is closed.

## 1. Binding scope and actual baseline

Authority is the owner's explicit **Packages A+B only** execution instruction, the current [ACTIVE](../../handoffs/ACTIVE.md):3–15 and [implementation handoff](../adr-0022-immutable-snapshot-implementation-handoff.md):98–122. The handoff's original authoring-time NOT AUTHORIZED text is conditional historical context; the subsequent owner instruction/ACTIVE authorizes this finite subset, not A–H. Older root/backend status prose does not override the later explicit owner authorization.

Applied repository final-review/shared-coverage and local-RAG/native guidance, root/backend/documentation/test instructions and bounded-command execution. Used the existing Graphify graph only for narrow navigation, then verified important details in source. No repository-wide backend audit, new native probe, remediation or architecture selection was undertaken.

Binding [ADR-0022](../../../adr/0022-phase-18-immutable-snapshot-whole-file-consent-no-change.md) SHA-256 remains `fa4ce5681093cc758f4a2cea6ee5c8ce486e3fc5aeae31ba346edfafde4cb25d`. The approved D1–D11/N1–N10/WF-1–8 remain effective. In particular:

- N2/N3 and lines 73–78 require correct root/ancestor/object-to-stream acquisition, coherent capture and protected managed-object continuity. Original live protection **after protected capture/seal through indexing COMMIT is not required for snapshots**.
- N3/N9 and lines 80–82 require effective storage isolation, immutable no-overwrite/durable sealing and validated cleanup. Paths, read-only attributes and checksums alone are explicitly insufficient.
- A requires deterministic native adversaries and actual operation outcomes; B expressly requires effective Java/Python access/handle proof, crash/seal and containment evidence. C/D consent/admission/lifecycle integration and F publication are later dependencies, not missing features this review demands A+B implement.

Git is `main`, HEAD `435ffea979681f22d723f551dd8b0cd0b733c8cd`, unchanged. Entry working tree: **2,124 tracked/nonignored files**, including substantial inherited uncommitted Phase 18 work.

## 2. Independently identified inventory and preservation

Compared every current path/hash/length against `.git/codex-preservation/phase18-canonical-sync-20261018/after.json`, not merely Git HEAD:

- Of its 2,116 paths, **2,114 retain identical bytes**. Only the guard and ACTIVE differ.
- Eight paths were added since that manifest. Two are legitimate predecessor documents: the accepted synchronization review and the new implementation handoff. Their hashes match the handoff's recorded review hash and this reviewed handoff respectively.
- Removing those predecessor documents yields exactly the reported **two modified + six new A+B paths** below. No inherited path was deleted, and no additional code/test/migration/build/dependency change was found.
- NativeWorkerProcess, source registration/preparation, finalizer, retained parser/worker/vector/purge/union repairs, canonical ADR/owner/acceptance documents, historical reviews, fixtures/models and migrations retain their earlier hashes, except the expressly modified guard.

Abbreviations used below: **J** = `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/`; **JT** = `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/`; **WG** = J `internal/infrastructure/windows/WindowsNativeFileGuard.java`; **STORE** = J `internal/infrastructure/storage/RagOriginalBytesStore.java`; **AT** = JT `RagWindowsAcquisitionTest.java`; **ST** = JT `RagOriginalBytesStoreTest.java`.

| State | A+B path | Actual SHA-256 |
| --- | --- | --- |
| Modified | WG | `d748071f9e4819cbc74897ba1f88eb8abf3fd8d3cc13e47a19abd072c42f59c4` |
| Modified | `docs/implementation/handoffs/ACTIVE.md` | `9a199314d4968cab2c4a11c3aff1bc9b93520466633575f6105eba4839395ed9` |
| New | STORE | `23fc4e5564d0c0b32e2236802190ea9c9d6818c90f9d7773e1dfcbf793c77a9c` |
| New | J `internal/infrastructure/storage/SnapshotStorageException.java` | `1df7ced2bb1c78b16fdda5999dfd4d691403b88f0467bf2fd5214bb970081980` |
| New | J `internal/infrastructure/storage/SnapshotMissingException.java` | `60c72f1d53f4317789c381d829226cbd8bb685053ad28540aa5bfd12d6e8cb63` |
| New | J `internal/infrastructure/storage/SnapshotIntegrityException.java` | `3817b6e87b99f57f969b01ddc74c8c4820332988635307bf06074bb80ea07011` |
| New | AT | `62c4876034e0c8bd01e4df87546e07ea03559cb3c06dc367f14446894ee1606c` |
| New | ST | `55cc4499968a9c03a5f07282259dd7110bfe330af42fa97269e98a25374dc813` |

Predecessor handoff SHA-256: `3b1958ba2f3d897363f130b93d59988f7ad098a3d222233dbebe90d86514d43c`; accepted synchronization review: `75c663249dece115c7a313f661c69635c0ebe36e0a9f70b62eab5407eb5a6be7`. The guard's canonical pre-slice hash was `8746b1dc01ed00e03b50c324bfb8ae3bbd9a75f59e3b1de9d6d894399aec6316`.

**Limit:** no separate immediate A+B before/after manifest, frozen source snapshot, isolated session diff or dated completion handback was located in the scoped repository/evidence supplied. ACTIVE supplies routing and the owner prompt supplies the completion summary; raw receipt/Surefire files supply the execution evidence. The earlier manifest strongly supports preservation of all other inherited files, but does not isolate authoring versus implementation edits inside ACTIVE or supply before-source bytes for every guard hunk. Git's guard diff against HEAD includes earlier credited repairs and must not be attributed entirely to A+B.

At review entry all nonignored hashes were captured in memory. Immediately before this report, **all 2,124 still match exactly**, with no new nonignored path. Original Surefire files and supplied receipt also retain their entry hashes. The permitted focused rerun generated only separately named ignored verification outputs; this report is the sole new repository deliverable.

## 3. Focused evidence and independent execution

### Supplied result

`logs/phase18/package-ab-test.json` SHA-256: `65edc114abdb7f999b7f22b878fc432254fe17f75f63ee52b836ad52d33450a9`. It records normal profile, 1,200-second ceiling, `mvn.cmd`, exit 0/PASS, no cleanup needed, **6.609 seconds**, 2026-10-18 16:52:33.180–16:52:39.794 UTC. Its argv SHA-256 is `6a3d46f82e203aad2772c129ef2dce60fc1d627474d915738275eef0f92aa470`; it contains no raw argv or source hashes.

The corresponding original Surefire XML files agree on selected tests, Windows 11/amd64 and Java 25.0.2 (`25.0.2+10-LTS-69`), with:

| Suite | Tests / failures / errors / skips | Original XML SHA-256 |
| --- | --- | --- |
| RagWindowsAcquisitionTest | 10 / 0 / 0 / 0 | `afd0e4bec228780c98eb6c388570aef0150d4da88dfb80c7f3986af2240ea1ad` |
| RagOriginalBytesStoreTest | 10 / 0 / 0 / 0 | `1f9c4b78e713c3897ed43ac96c922f850b9ded9dc6a67f68912b8d8517725616` |
| RagWindowsNtfsFileGuardTest | 8 / 0 / 0 / 0 | `ab62da316e48e414360a0bddac9eaf54e8e3b900f7508ff4ba7fecd5e7b655a4` |

Thus **20 new tests plus 8 existing tests**, not 28 new acquisition/isolation proofs. The count/exit claim is supported. Its absent command/source-binding handback limits historical provenance; the independent execution below provides a current reproducible command/result.

### Independent focused rerun

Executed once from repository root, using the existing pinned interpreter and watchdog, normal profile with a shorter 300-second ceiling. Distinct report names preserve original successful and failed evidence:

```powershell
& 'rag-engine/.venv/Scripts/python.exe' 'scripts/run-bounded-command.py' --profile normal --timeout-seconds 300 --receipt 'logs/phase18/checkpoint-1-ab-independent-2026-10-19.json' -- mvn.cmd -ntp -f backend/pom.xml '-DargLine=--enable-native-access=ALL-UNNAMED' '-Dtest=RagWindowsAcquisitionTest,RagOriginalBytesStoreTest,RagWindowsNtfsFileGuardTest' '-Dsurefire.reportNameSuffix=checkpoint-1-ab-independent-2026-10-19' test
```

**Actual result:** 28/28, zero failures/errors/skips, exit 0; Windows 11/amd64, OpenJDK `25.0.2+10-69`. Maven total 6.192 seconds; watchdog duration **7.759 seconds**, 2026-10-19 00:19:34.439–00:19:42.198 UTC; no timeout/termination. Existing classes were up to date. The intentional held-file deletion test emitted its expected Windows sharing-denial warning.

- Independent receipt SHA-256: `4e830b254bbecd52f314f826699535aa7a2f2145ebcc81b39e9b81086189a779`; argv SHA-256 `3c2ffc645a48963f06d5db79db7c374825067d1a329f4356cb5238b964cd6f20`.
- Independent XML files under `backend/target/surefire-reports/` have suffix `-checkpoint-1-ab-independent-2026-10-19.xml`. Acquisition SHA-256: `86a4dde0f829ac3d0a23a309e1624b991410a92ca754be8a391f80d18b043c1e`; store: `ce9ce392ddf2983068a8aaf5c90f0a25aeaa1501843b9bee4284b39a9413ea0d`; existing guard: `14720598d6150207720d8d08b900c5e1aa58e417c7d05997e7d79111e3343f60`.

No new adversarial harness/test was written. No full Maven verify, P18-FINAL, SQL, OCR/GPU or unrelated benchmark ran. All mutations were existing tests on their controlled disposable fixtures. Passing these assertions is evidence of their actual narrow behavior, not proof of untested attacks.

Historical evidence was preserved: checked 18 raw artifacts referenced by the R7 producer/publication evidence and 36 referenced by the invariant evidence; all match. The original nine-test prerequisite report still contains the late-alias failure. That deferred live-only failure was neither rerun nor required to pass this snapshot checkpoint.

## 4. G1 native conclusions

### Correct mechanisms credited

WG:48–60 defines the proper Win64 `BY_HANDLE_FILE_INFORMATION` layout: size 52, link offset 40, file indices 44/48, volume 28 and size 32/36. DWORDs are read with appropriate unsigned conversion where needed. New acquisition checks query BOOL failure, target directory/reparse flags, link count exactly one, volume and optional expected object identity. This agrees with [Microsoft's structure/identity documentation](https://learn.microsoft.com/en-us/windows/win32/api/fileapi/ns-fileapi-by_handle_file_information); it preserves the credited hard-link ABI correction.

WG:568–594 opens the target with GENERIC_READ, OPEN_EXISTING and FILE_SHARE_READ; WG:648–669 reads **that same native handle** via ReadFile. It does not validate one handle and reopen the target pathname to read bytes. Exact-byte SHA-256/length and finite byte limits are applied before any parsing. The returned AcquiredSource retains the file handle until close.

For ordinary supported NTFS data opens, omitting FILE_SHARE_WRITE rejects conflicting writers/writable mappings, and omitting FILE_SHARE_DELETE restricts target delete/rename opens. Those restrictions last until the handle closes. Microsoft expressly distinguishes attribute/extended-attribute access from those share restrictions. These are data-object guarantees, not a general metadata/namespace freeze. [CreateFileW](https://learn.microsoft.com/en-us/windows/win32/api/fileapi/nf-fileapi-createfilew).

Fresh tests support ordinary writer denial and protected target operations. The unchanged historical native prerequisite additionally proves a writable view remains live after its FileChannel closes and acquisition rejects it; its code at lines 189–205 and preserved native evidence are useful unchanged data-stream evidence. New AT:185–191 alone keeps the writable channel open and cannot independently distinguish mapping denial from writer-handle denial.

No alias-based content-write bypass is reproduced. Late link creation/removal alone is not declared an ADR violation: it can alter alias bookkeeping while the approved object's bytes remain protected. Nor is a universal transient-alias ban added. Filesystem timestamps/attributes are not invented as byte revisions. These distinctions do not cure the acquisition binding gap below.

### CP18-AB-1 — High — Continuous root/ancestor-to-open authority remains unproved

**Evidence:** WG:380–440, especially 405–408 and 434–436; 491–562; 564–582; 695–711. ADR-0022 N2/N3, line 73 and handoff A:102–108.

`queryDirectoryIdentity` resolves a pathname, opens with share mode **7** including WRITE/DELETE, queries an ID and immediately closes the handle. `ancestorChain` retains records, not protected directory objects. Acquisition separately checks paths/reparse attributes, resolves real paths, queries component identities and finally opens another absolute pathname. It later queries those paths again. Ordinary follow semantics remain at the file/directory opens; the final opened file's attributes do not prove that no ancestor redirection was traversed.

There is no retained, validated mechanism bridging selection/root/component observations to the actual target open. An ID match at two observations cannot distinguish an intervening replacement-and-restoration. Directory share mode 7 supplies no additional rename/write exclusion, and the query handles are gone before capture. Optional expected file/root IDs reject important static substitutions but do not prove the continuously authorized ancestor/path chain. Existing tests showing some ancestor renames denied after an ordinary file is already open do not establish the check-to-open interval.

**Classification:** definite missing mandatory safety invariant in code/proof; **not an independently reproduced successful namespace exploit**. This review does not assert every ancestor rename can succeed on NTFS, or that Windows cannot provide the required protection.

**Minimum correction/proof:** close the root/ancestor/reparse-to-open binding interval with a supported mechanism in the existing native boundary, retaining necessary object protection through coherent capture/seal. Prove component-to-component binding, including temporary swap/restore and pre-open redirection, with deterministic separate adversary/barriers, actual native outcomes and positive controls. Reject unsupported cases. Do not substitute another pre/post check. Account for query/read/close failures; the new directory-finally and acquisition-denial paths currently discard CloseHandle BOOL results (435/715), so unknown cleanup cannot be certified from them. No live-original-through-publication extension or new driver/service is requested.

## 5. G3 store conclusions and concrete findings

### CP18-AB-2 — Critical — No effective ordinary-worker filesystem isolation

**Evidence:** STORE:70–88,107–112,249–255; ST:151–175,197–205. Existing `NativeWorkerProcess.java`:95–107,111–118. ADR-0022 N3/line 80; handoff B:114–122.

The store creates ordinary directories with inherited permissions. It applies no effective worker-specific access restriction or protected object/handle boundary. The default root is discoverable under `user.home/.ppv-vault/rag-store`; custom paths and UUID filenames do not establish authority. `setReadOnly()` return values are ignored, and failures only warn. No store handle remains held after sealing.

The unchanged launcher calls CreateProcessW in the caller's security context, without a restricted-token/other filesystem boundary. Its Job Object limits/process ownership and disabled handle inheritance are useful lifecycle protections; they do not revoke discretionary pathname access. [Microsoft documents CreateProcessW's caller security context](https://learn.microsoft.com/en-us/windows/win32/api/processthreadsapi/nf-processthreadsapi-createprocessw) and [file access rights](https://learn.microsoft.com/en-us/windows/win32/fileio/file-security-and-access-rights). No actual launched-worker SID/privilege/access matrix was supplied or exercised by these tests; it would be false to claim such token inspection occurred.

There is direct fresh counterevidence to the proposed file immutability: ST:153–154 and 171–172 successfully clear read-only and overwrite the sealed bytes/manifest in the same executing security context. ST's “worker” test launches no Python process and does not try attribute removal. Read-only also permits reads; it is not a confidentiality boundary. [SetFileAttributesW](https://learn.microsoft.com/en-us/windows/win32/api/fileapi/nf-fileapi-setfileattributesw).

**Classification:** isolation mechanism absent; same-context mutation actually exercised by the existing passing test. Actual Python-driven read/forge/map/duplicate-handle attack execution is **NOT_VERIFIED**, not claimed as reproduced. There is no present production ingestion exposure claim because ingestion remains inactive.

**Minimum correction/proof:** establish and demonstrate a bounded supported effective process/filesystem/handle boundary within the existing native architecture before trusting B. Test the actual ordinary worker identity against temp, root, data, manifest and parent entries: read/enumeration, create/write, attribute/ACL changes, mapping, rename/delete/replace and reachable inherited/duplicated handles. Include legitimate transfer/runtime/scratch positives. If this requires unapproved provisioning/infrastructure, STOP with that exact owner decision. Do not install it or substitute benign worker conventions/owner-only ACLs under an unchanged token.

### CP18-AB-3 — High — Manifest-derived trust and verify-then-reopen break integrity continuity

**Evidence:** STORE:173–194,227–246,263–346,352–386. ADR-0022 N3 and lines 75–80.

`readRevisionBytes` obtains expected H/L from the same writable manifest, calls `verifyIntegrity`, closes those reads and then reopens the pathname with `Files.readAllBytes`. A writer able to replace both file and manifest can supply a self-consistent pair. A writer can also change the object between verification and the later reopen. No retained object protection bridges this interval. Regex extraction validates neither complete strict JSON nor sourceId/revisionId/captureId provenance. `sealRevision` checks temp length but does not establish the supplied digest against a continuously protected temp object at sealing.

**Important distinction:** `verifyIntegrity(S,R,independentlyTrustedH,L)` would reject changed bytes against an unchanged trusted digest. That useful comparison is credited. The defect is the public read path choosing its own unprotected reference and returning a separately opened object. Plain SHA-256 is not authentication of an attacker-writable reference.

**Classification:** source-established unsafe trust/read sequence. A valid double-file forgery or timed substitution was not executed during review; ST only tests unilateral corruption/malformed metadata. This is not a claim SHA-256 is broken or that producer verification belongs in B.

**Minimum correction/proof:** require an independently trusted Java seal descriptor/expected identity/H/L and validate complete immutable manifest identity. Verify/read the same protected object under continuity adequate for the supported use interval. Reject source/revision mismatch and temp mutation through sealing. Exercise valid self-consistent pair forgery, wrong manifest identity, equal-size temp changes and verification-to-use replacement. Future C supplies durable authority and later F publication; do not implement them now or introduce an unnecessary signing platform.

### CP18-AB-4 — High — Atomic move is not atomic no-overwrite

**Evidence:** STORE:209–224 and 239–246; ST:78–102. ADR-0022 N3/line 75; handoff B:114/118.

Both existence checks precede `Files.move(..., ATOMIC_MOVE)`. Omission of REPLACE_EXISTING does **not** establish no-overwrite under ATOMIC_MOVE: Java makes existing-target behavior provider-specific. [Java 25 Files.move](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/nio/file/Files.html#move(java.nio.file.Path,java.nio.file.Path,java.nio.file.CopyOption...)).

Read both installed JDK source archives, including the JDK used by the independent rerun. `WindowsFileCopy.move` implements the atomic branch with `MoveFileEx(..., MOVEFILE_REPLACE_EXISTING)`. The rerun JDK's source SHA-256 is `a326e2db2f879750fdb0d9c8b476255e169a698625fc1299c2a8797a33f46766`; the implementer's installed JDK copy is `c6dbb9102313082e105220fa2d3f924106e96a641a886189aa1ca9f47042786f`. The same operation is visible in the [OpenJDK 25.0.2 source](https://github.com/openjdk/jdk25u/blob/jdk-25.0.2%2B10/src/java.base/windows/classes/sun/nio/fs/WindowsFileCopy.java).

Two attempts can pass the checks before either completes sealing. A destination created in the interval can be replaced while still writable. Competing manifest CREATE_NEW/rollback can then leave mismatched data or delete another attempt's moved data. The existing test is sequential: it never challenges this interval. A fully finished read-only target may separately reject replacement; that does not close the earlier race.

**Classification:** provider/source-established no-overwrite defect; no concurrent seal attack was newly executed.

**Minimum correction/proof:** use a supported no-clobber seal with exact operation ownership and truthful collision outcomes, without relying on pathname prechecks. Deterministically race competing seals/target creation and manifest failure; prove original successful bytes/manifest cannot be replaced or deleted by the losing attempt. Keep one bounded store implementation, not a transaction framework.

### CP18-AB-5 — High — Durable seal/partial-state recovery is not established

**Evidence:** STORE:132–147,218–257,449–488; ST:107–132. ADR-0022 N3/N9; handoff B:114/118–122.

Data **is** forced on the temp channel at line 147; the review does not claim only the manifest is flushed. However data move and manifest creation are separate operations, the manifest uses one `channel.write` without ensuring the buffer is exhausted, and there is no demonstrated durable rename/directory-state proof. File-channel force is a guarantee about that channel's file, not automatically the entire multi-file namespace operation. [FileChannel.force](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/nio/channels/FileChannel.html#force(boolean)).

Failure after data move can leave an orig without manifest. A manifest-write failure attempts data deletion but ignores cleanup failure and can leave a partial manifest. Orphan detection examines `.orig` and `.tmp`, not manifest-only residues. Regex-based validation can also accept incomplete JSON containing the expected matched fields. There is no supplied crash-cut evidence showing a returned SEALED result is durable or how every interrupted state remains denied and reconciles without adoption.

**Classification:** partial-state implementation gaps plus **NOT_VERIFIED crash/directory durability**; no actual power-loss corruption was observed. Two files need not be committed by a new filesystem transaction. A complete, safe pending/known/unknown protocol could satisfy the contract.

**Minimum correction/proof:** complete writes, validate full manifests, define/implement owned interrupted-seal states and conservative acknowledgement, account for namespace durability on supported Windows/NTFS, and reconcile orig-only/manifest-only/partial/cleanup-failed states. Use bounded fault/crash cuts before/after data flush/move, during manifest write and before the filesystem seal acknowledgement. Actual PostgreSQL acknowledgement/physical-C integration remains later C/D/F work, not a requirement to execute those packages in this correction.

### CP18-AB-6 — High — Cleanup can target unowned/out-of-root objects and misstate absence

**Evidence:** STORE:70–81,107–112,132–153,393–443,449–518. ADR-0022 N9/line 78; handoff B:118.

There are three concrete issues:

1. On `CREATE_NEW` failure, `writeTemp` catches the exception and deletes the pathname even if this invocation never created it. A second call with an occupied capture ID can therefore delete the first capture. This is a direct ownership/control-flow defect, not a C/D integration requirement.
2. Containment is lexical `normalize().startsWith`. Initialization and enumeration do not establish trusted non-reparse root/ancestor objects. `Files.isDirectory` follows links by default; a junction below `revisions` can expose outside entries to `detectOrphans`. Those paths still pass the lexical prefix check and cleanup sets writable/deletes through them. Direct revision deletion has the same unchecked ancestor issue. A final-component unlink's link behavior does not protect traversal through an intermediate junction. [Microsoft symbolic-link filesystem behavior](https://learn.microsoft.com/en-us/windows/win32/fileio/symbolic-link-effects-on-file-systems-functions).
3. `deleteRevision` can report COMPLETED from `!Files.exists(...)`; false also means existence could not be determined. `cleanupTemp` returns no failure/pending result, orphan enumeration swallows errors, and null legitimate-revision input becomes an empty set. Every temp is classified as orphan without active-capture identity input. These are not sufficient proofs of absence or cleanup authority. [Java Files existence semantics](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/nio/file/Files.html#exists(java.nio.file.Path,java.nio.file.LinkOption...)).

**Classification:** ownership/uncertain-absence defects are source-established; junction-mediated outside deletion/path-swap exploitation was **not executed**. No unrelated real directory was attacked. Existing tests contain neither such adversaries nor inaccessible-root/unknown-history cases. Caller quiescence can be a legitimate later precondition, but this must be explicit and complete; an untrusted/unknown inventory cannot silently become authority to sweep everything.

**Minimum correction/proof:** delete only objects this operation owns; validate/retain supported managed-root/ancestor/object boundaries, rejecting reparse redirects; distinguish verified absent from inaccessible/unknown; propagate cleanup pending; require complete legitimate revision/capture and quiescence evidence or deny sweeping. Test collision preservation, controlled reparse/path-swap refusal, active-reference/capture preservation, unknown/inaccessible state and failed deletion. Use only owned disposable fixtures; no outside real-file attack or second cleanup engine. Later C/D supplies durable references/lifecycle, without making the unsafe B defaults acceptable.

## 6. Test-by-test adjudication

No mock native operation was found in these 28 selected tests. Nevertheless their assertions do not match several broad scenario names. No new test uses a separate concurrent adversary/barrier to exercise the acquisition check-to-open race.

### CP18-AB-7 — Medium — Native/storage acceptance evidence overstates exercised adversaries

**Evidence:** AT:33–264 and ST:39–332, detailed below; handoff A:104 and B:118/120 and evidence/handback requirements at 260–273.

| New acquisition scenario | Actual behavior and limit |
| --- | --- |
| A1, lines 35–55 | Correct small ordinary capture/hash; expected root/file identities are null. No adversary or approved acquisition interval proof. |
| A2, 60–77 | Static containment and mismatched expected root rejection. No ancestor swap. |
| A3, 81–100 | File replaced **before** the second call; correct expected-file-ID rejection, not concurrent ABA. |
| A4, 104–121 | Equal bytes in another object correctly rejected when expected ID is supplied. Useful static positive/negative. |
| A5, 125–144 | Pre-existing junction only. Assertion runs only if mklink succeeds and path exists; otherwise test returns PASS. Actual attack setup result/output is not recorded. Zero skipped tests does not prove this branch ran. |
| A6, 148–166 | Existing two-link rejection and post-unlink acceptance. No late link creation/removal during acquisition. |
| A7, 171–197 | Real existing writer/map setup, but mapped channel remains open; later writer attempt occurs after capture returned while handle is held. Useful sharing evidence, not a separately synchronized during-read race. |
| A8, 202–225 | Real target delete/rename/replace attempted after full capture. No ancestor/root/reparse ABA; IOException alone does not identify every kernel refusal reason. |
| A9, 230–244 | Cancellation flag is already true before first ReadFile. Subsequent successful write is a useful cleanup control, not mid-read/blocking-I/O cancellation proof. |
| A10, 249–263 | Exact bytes and owner writability after release. Does not compare full ACL/metadata, or exercise concurrent capture. |

| New store scenario | Actual behavior and limit |
| --- | --- |
| B1, lines 41–73 | Ordinary byte/hash/length roundtrip and temp movement. No security-token or durability proof. |
| B2, 78–102 | Sequential duplicate revision denial; no competing precheck/move race. |
| B3, 107–132 | Leaves a fully written temp and cleans it. No process crash, data-move/manifest window, partial manifest or directory durability test. |
| B4, 137–180 | Actually clears read-only and mutates sealed bytes/manifest. Detects unilateral corruption against the original expected hash; also directly refutes attribute-based immutability. No valid pair forgery or verified-use race. |
| B5, 185–205 | Same-JVM FileOutputStream denial without attribute clearing; no Python launched. The alleged “escape” uses valid UUIDs and a missing capture, proving missing-temp denial, not traversal containment. |
| B6, 210–229 | Files.move to an existing target without replacement flags. Ordinary destination-exists failure, not an alias/mapping/duplicate-handle attack. |
| B7, 234–247 | 100 bytes against a 50-byte configured cap. Useful finite-size rejection, not actual disk-full evidence. |
| B8, 252–264 | Deletes temp after write completed. No cancellation during write or active-writer safety proof. |
| B9, 269–302 | Preserves one supplied legitimate revision and removes ordinary leftovers. No junction/swap, active capture, missing-authority or enumeration-failure adversary. |
| B10, 307–332 | Real Windows open-stream deletion refusal produces PENDING_INACCESSIBLE; after close, deletion succeeds. Credited narrow positive/negative. |

**Minimum correction/proof:** repair only missing/misleading A+B cases in existing focused packages. Require successful attack setup or explicit unsupported/failed evidence, deterministic vulnerable-interval barriers, actual operation status and post-release positive controls. Add actual ordinary-worker access proof and focused seal/verification/cleanup fault cases tied to findings 1–6. Provide a dated handback with actual command/cwd, source/fixture/report hashes and unsupported/unrun distinctions. Do not substitute a larger test count or repeat full Maven/OCR/GPU verification.

### CP18-AB-8 — Low, nonblocking — New package descriptor missing

The new module-local `internal/infrastructure/storage` subtree contains four classes and no `package-info.java`; the adjacent Windows subtree has one. Add the meaningful package descriptor required by repository final-review hygiene during later authorized A+B work. No package move, interface framework, dependency or new review cycle is needed. This is not the reason checkpoint 1 is rejected.

## 7. Independent dispositions

| Area | Disposition | Evidence / exact limit |
| --- | --- | --- |
| G1 source/root/ancestor identity | **FAIL** | Static identity checks are useful, but continuous chain-to-open protection is absent/unproved (CP18-AB-1). No successful ABA exploit claimed. |
| G1 stream coherence and writer/mapping protection | **PASS — bounded data-stream subproof only** | Same acquired native handle supplies bytes; documented share restrictions and fresh ordinary writer tests, plus preserved live-mapping control. Does not certify namespace/consent/metadata authority, every unsupported access path or overall G1. |
| G1 native adversarial-test validity | **FAIL** | Claimed acquisition adversaries are largely static/after-read; no deterministic ABA/check-to-open actor; conditional junction success can be vacuous (CP18-AB-7). Ordinary native tests genuinely pass. |
| G3 effective Java/Python isolation | **FAIL** | No effective process/filesystem mechanism; same-context mutation succeeds. Actual worker access matrix/token inspection NOT_VERIFIED (CP18-AB-2). |
| G3 immutable file/manifest protection | **FAIL** | Mutable reference trust and verify/reopen; no continuous protected object (CP18-AB-2/3). |
| G3 atomic seal and crash durability | **FAIL** | No-overwrite defect; interrupted-state gaps. Actual crash/directory-durability subproof NOT_VERIFIED (CP18-AB-4/5). |
| G3 orphan cleanup/containment/recovery | **FAIL** | Unowned temp deletion, lexical boundary and unknown-as-absence defects (CP18-AB-6). No outside deletion experiment performed. |
| Slice authorization and preservation | **PASS, with recorded baseline limit** | A+B matches owner scope; no C–H activation/change; inherited comparison and review-entry hashes preserved. Immediate implementer session manifest/handback unavailable (§2). |

**Overall G1: not accepted. Overall G3: not accepted.** A PASS on protected data-object reads cannot be promoted into a PASS on acquisition authority or managed-store security.

## 8. C–H boundaries and preserved obligations

No production caller of `acquireProtectedSource` or the new store was found in the bounded cross-reference check; the store is a component/primitive, not an enabled snapshot workflow. It contains no trusted worker grant, selected-R/current-G transition, NO_CHANGE/C_N or C_D/C_R/C_P implementation. A file manifest's SEALED label and StoredSnapshot record are not consent, selected R, SNAPSHOT_CURRENT or PostgreSQL publication.

Unchanged containment at `RagSourceService.java`:56–57/113 and `RagSourceAuthority.java`:72 still rejects external registration/approval/preparation. Unchanged `RagDocumentFinalizationGate.java`:131 still derives expectedOutput from `digest("DOCUMENT_V3:" + expectedInput)`; **FR18-3 remains Critical and OPEN**. Original-byte hashes cannot repair it.

Packages C/D authority/closure/reservations/lifecycle, E legitimate NO_CHANGE, F restricted producer/physical publication, G whole-file extraction/locator quality and H integrated operational evidence remain separately unauthorized/unaccepted. G2/G4 are NOT_VERIFIED. No defect is issued merely for their deliberate absence. B nevertheless must expose a safe protected store primitive for their future use; later integration cannot retrospectively supply missing isolation or make unsafe cleanup correct.

Preserve ADR-0022/owner D1–D11, frozen Vault privacy/fields, complete source-first reservation order, one Java physical publication, no automatic retry/recovery promotion, all formats/four languages/OCR/tables/merged cells/locators/chunking, offline/GPU/fallback, independent RAM/VRAM and other resource/final evidence. Credited DOMAIN input/publication, VECTOR/JPA, exact-union/source-first, generation-specific purge and parser/geometry/chunk repairs remain credited. No unrelated regression is alleged.

**FR18-1, FR18-2, FR18-3, FR18-4, FR18-5, FR18-6, FR18-7, FR18-8, FR18-9, FR18-10 and FR18-11 all remain OPEN for independent adjudication.** A/B concerns map to the handoff's existing FR18-1/2/4/8/10 obligations; evidence gaps also affect final verification, without renumbering or self-closing original findings. The old 2/2 exhausted remediation history is unchanged; this checkpoint creates no automatic third round or counter reset.

## 9. Scope-aware 29-dimension review ledger

| Dimension | Status | Scope/evidence |
| --- | --- | --- |
| 1 Owner intent/scope | PASS | Exact A+B authorization, no C–H expansion (§1/8). |
| 2 Requirement traceability | FINDING | Required native/isolation/seal evidence not exercised (§4–6). |
| 3 Architecture consistency | PASS | Existing owners/native boundary; no contract redesign or new infrastructure. Feasibility still unproved. |
| 4 Module ownership | PASS | New code is internal Java RAG storage; unchanged module edges/SQL/Python rights. |
| 5 Package/folder structure | FINDING | Full new storage/changed Windows subtree inspected; nonblocking missing descriptor (CP18-AB-8). |
| 6 Shared HTTP/errors | N/A | No transport/envelope/route change; local exceptions only. |
| 7 Concrete SOLID/cohesion | PASS | Bounded native/storage responsibilities; no speculative interface hierarchy required. |
| 8 Immutability/ownership principles | FINDING | Mutable authority, no-overwrite and unowned cleanup (CP18-AB-2–6). |
| 9 Patterns/overengineering | PASS | No coordinator/queue/plugin platform added; fixes need no such framework. |
| 10 Business/state correctness | FINDING | SEALED/cleanup outcomes unsupported in interrupted/unknown states; no publication inferred (§5). |
| 11 Public wire compatibility | N/A | No API/protocol change in A+B. |
| 12 Input/path validation | FINDING | Acquisition TOCTOU and store reparse/lexical boundaries (1/6). |
| 13 Authorization/least privilege | FINDING | Effective worker isolation absent (2); C authority explicitly deferred. |
| 14 Retention/cache/backup security | FINDING | Controlled storage cleanup/integrity gaps (3/5/6); backup policy unchanged. |
| 15 Atomicity/concurrency | FINDING | Check-to-open, verify-to-use and replacement races (1/3/4). No SQL experiment. |
| 16 Recovery/cancellation | FINDING | Partial seals and insufficient active-writer/cancel evidence (5–7); G4 deferred. |
| 17 DB/Flyway/JPA | N/A | No migration/entity/schema change; earlier repairs preserved. |
| 18 Complexity/resources | NOT_VERIFIED | Finite byte caps exercised; full RAM/disk/time/cancellation/store-growth evidence belongs to later authorized integration. |
| 19 Index/retrieval quality | N/A | No index/QA produced by A+B. FR18-3 unchanged. |
| 20 OCR/document fidelity | N/A | No parser/OCR change or run; retained acceptance unchanged. |
| 21 Security tests | FINDING | Real ordinary native operations, but mandatory adversarial proof missing (7). |
| 22 Test quality | FINDING | Conditional/incorrectly labelled security/crash scenarios (§6). |
| 23 Performance benchmarks | N/A | No benchmark/acceptance claim; focused durations only. |
| 24 Held-out evaluation | N/A | No quality/model work; existing oracle retained. |
| 25 Dependencies | PASS | All inherited dependency/build/model hashes preserved (§2). |
| 26 Build/test/tooling | PASS | Independent bounded 28/28 native run, no full-suite claim (§3). |
| 27 Diagnostics/evidence | FINDING | Attack outcomes/source-bound dated handback incomplete; truthful pending/crash reporting needs correction (5–7). |
| 28 Documentation/governance | PASS | A+B review gate, inactive capabilities/open findings preserved; stale authoring prose interpreted under later owner authority. |
| 29 Hygiene/preservation | PASS | Complete current inventory verified; original receipts unchanged; sole new nonignored deliverable is this report. |

## 10. Feasibility and exact next owner-controlled action

**Supported A+B feasibility is not yet established. FEASIBILITY_BLOCKED is not justified by this evidence.** Current unrestricted same-token execution plus mutable files/checksums cannot supply G3, but the handoff already permits proving a bounded effective mechanism within the existing native process boundary. No evidence rules out such a correction, nor proves the required G1 binding impossible. This review selects no replacement architecture, new account/service/driver, signing system or scope amendment.

**Recommended finite next action:** keep this slice stopped and explicitly authorize **one A+B-only corrective/proof submission for CP18-AB-1–7**, with CP18-AB-8 handled as routine nonblocking package hygiene. First establish the concrete continuous acquisition and effective worker-isolation mechanisms; then correct trusted read/seal/cleanup and run only the discriminating focused native/storage cases. Preserve the ordinary data-stream/byte/hash/cancellation/deletion positives. No new general audit or full Maven/OCR suite is needed for that submission.

**Safety STOP:** if either continuous authorized acquisition or effective worker/store isolation cannot be demonstrated within the already permitted native/host boundary, stop that proof work and return the exact unsupported operation/right/interval and smallest additional owner decision. Do not proceed to useful trusted capture, snapshot worker admission or C–H on a path/hash/read-only shortcut. This is a proposed owner-controlled finite correction, **not authorization issued by this review**, a reset of prior rounds or an indefinite trial loop.

After a corrected checkpoint, C–H still require their own explicit finite owner authorization and applicable independent gates. No source consent, selected revision, trusted snapshot publication or NO_CHANGE is accepted here. P18-FINAL and Phase 18 acceptance remain outstanding; no READY FOR OWNER COMMIT verdict or commit message is provided.

Only this independent review document was added to the tracked/nonignored repository inventory. The expressly permitted existing focused tests produced distinct ignored receipt/Surefire outputs; original successful/failed receipts and all entry source/document hashes were preserved. No production/test/canonical/ACTIVE edits, fix, migration, dependency change, ingestion activation, commit, push, tag or PR occurred. **STOP.**

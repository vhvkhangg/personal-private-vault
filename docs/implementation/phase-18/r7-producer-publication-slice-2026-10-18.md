# Phase 18 — R7-A → P → R7-B implementation slice, 2026-10-18

**STOPPED at the native safety prerequisite. R7-A FAIL; P NOT_VERIFIED; R7-B NOT_VERIFIED.**
Implementer record for a separate independent Codex reviewer. No finding is closed,
no self-review/acceptance is issued, and Phase 18 remains CHANGES_REQUESTED / INCOMPLETE.

## Owner authorization and dependency gates

The owner explicitly authorized this new, finite implementation session after the
[independent checkpoint review](reviews/2026-10-18-d18-pt-a-independent-checkpoint-review.md):

> Resume only R7-A, minimum FR18-3 producer verification (P), and R7-B in one separate bounded Codex implementation session, following the independent D18-PT-A checkpoint review.
>
> First correct and verify the existing hard-link safety prerequisite. Then implement the minimum trustworthy v3 producer/output/provenance verification necessary for real publication. Only after both prerequisites pass, attempt one genuinely authorized external TXT indexing/publication path through Java physical PostgreSQL COMMIT, including rollback, unknown-C and relevant opposing mutation/fence cases.
>
> Keep ADR-0021/D18-A and AC-01–08 unchanged.
>
> Keep ordinary live external-file ingestion DISABLED.
>
> STOP after the authorized slice or immediately upon an unresolved safety prerequisite.
>
> Do not perform OCR work, reduce scope, commit or push.

This is not a third Antigravity round or automatic takeover continuation. The prior
takeover/checkpoint/review records remain unchanged. The live-file alias failure
below triggered STOP; no producer/publication implementation followed it.

## Entry state and preserved work

Actual starting and ending HEAD: `435ffea979681f22d723f551dd8b0cd0b733c8cd`, branch `main`.
Initial `git status --short`: **115 entries**, comprising **49 tracked modified** and
**66 untracked entries** (directory entries can contain multiple files). All were
pre-existing owner/Phase 18 work. The starting inventory and **2,102-file SHA-256
snapshot** are retained at:

- `logs/phase18-r7-producer-publication/starting-status.txt`
- `logs/phase18-r7-producer-publication/starting-snapshot.json`

The [evidence JSON](r7-producer-publication-evidence-2026-10-18.json) preserves the
full starting porcelain inventory, source delta, command receipts and artifact hashes.
Only the native guard and ACTIVE changed among the pre-existing snapshot files;
all other inherited snapshot bytes remain unchanged. New files are listed below.
No destructive Git operation, commit, push, tag, PR or unrelated edit occurred.

Read root/backend/test AGENTS, worker README, ACTIVE, ADR-0020/0021, owner decisions,
AC-01–08, locator oracle, P18-FINAL, takeover record, checkpoint/evidence and both
independent reviews. Used Graphify to locate the native guard/tests, then verified
actual source. Applied bounded-command, Java, backend-testing and local-RAG guidance.
The `py` launcher was unavailable; the pinned venv Python ran the watchdog. No
Python runtime, parser, OCR, model, migration, dependency or worker permission changed.

## R7-A correction and exact native results

The existing `WindowsNativeFileGuard` now uses an explicit Win32 structure layout,
reads unsigned `nNumberOfLinks` and denies every value except **1**. Zero-filled
unavailable information, query failure and exceptional query failure deny. Acquired
native handles close on the common failure path before the arena closes; cleanup
errors are attached to the original failure. Successful handle lifetime/close behavior,
sharing flags, identity, size/hash/read and existing path/reparse checks are preserved.
Its documentation no longer represents the prerequisite guard alone as full R7 proof.

Independently checked Microsoft's [BY_HANDLE_FILE_INFORMATION definition](https://learn.microsoft.com/en-us/windows/win32/api/fileapi/ns-fileapi-by_handle_file_information).
Compiled a diagnostic against the locally installed Win64 C headers: **size 52,
alignment 4, link-count offset 40, index offsets 44/48, DWORD 4, FILETIME 8**.
Java queried actual NTFS handles: offset 40 returned **1** for the ordinary file
and **2** after a real `Files.createLink`, with the actual file size at offset 36.
The two-link source was then denied with the specific hard-link-count reason.

| Focused case | Actual result and limit |
| --- | --- |
| Ordinary single-link file | Accepted; actual content/identity/hash controls pass. |
| Existing alias | Real link creation succeeds; API reports 2; exact `Unsupported NTFS hard-link count: 2; exactly one link is required` denial; alias is writable after denied acquisition, then removed; single-link acquisition works again. |
| Failed query / unavailable count / invalid count | Test-only replacement of the query MethodHandle returns failure, zero, 2 or unsigned `0xffffffff`; actual native acquisition/cleanup remain real. Each denies, arena is dead, querying the acquired now-closed handle fails, and immediate write/delete succeeds. This is fault injection, not an observed OS query outage. |
| Exceptional query | Injected query exception also closes the actual acquired handle and arena. |
| Alias after acquisition | **FAIL:** real `Files.createLink` and `Files.delete(alias)` both succeed while the same protected handle remains open. Positive create/delete control after release also succeeds. The unsupported alias state can exist transiently despite the initial single-link check. |
| File/namespace mutations after acquisition | Actual file rename, replacement, immediate-parent rename and higher-ancestor rename attempts throw IOExceptions while held; ancestor rename succeeds after release. These fixture observations do not prove acquisition-time ancestor TOCTOU/reparse safety in general. |
| Pre-opened writer / opposing writer | Existing native tests deny an open writer and writes while guarded; writes succeed after release. |
| Writable mapping | A real READ_WRITE mapping remains after channel close, mutates/forces the fixture successfully, then guard acquisition denies. After unmapping, guard succeeds; a new writable-channel open while held denies. No mapping was created through a denied channel. |
| Existing reparse check | Prior conditional junction test remains unchanged and green; its conditional structure alone is not independent proof that every reparse mutation occurred. |
| Cleanup | Lexical guards/arenas close; positive post-denial/release controls succeed; JUnit temporary-directory cleanup reports no error. Watchdog child exits normally, no timeout/kill. |

The failed test is `attemptsAliasCreationAndRemovalDuringProtectedLifetime`. It
deliberately retains the safety assertion rather than accepting this unsupported
state. Its stdout has `created=true removed=true`. The additional `removeDenial`
string incorrectly retains its initial placeholder `not attempted: creation denied`
on the success branch; the executed create/delete statements and booleans establish
the actual outcome. Source and failed output are preserved without post-failure
test edits or a rerun. **No alias write was attempted in this execution**, because
removal succeeded; no write-sharing bypass or arbitrary NTFS infeasibility is claimed.

Microsoft [documents per-file sharing across hard links](https://learn.microsoft.com/en-us/windows/win32/api/winbase/nf-winbase-createhardlinkw).
That supports distinguishing the observed alias-state failure from a demonstrated
alias-write bypass. Under the owner's currently unsupported alias model, successful
transient alias creation/removal is still an unresolved native prerequisite. A final
link-count snapshot could miss it and is not a continuous safety solution.

## P and R7-B — not attempted after STOP

**P NOT_VERIFIED:** no producer/staging/finalizer/schema/worker changes were made.
The previous Critical FR18-3 input-only/count-only receipt and effective staging
provenance findings remain. All requested real-role grant/provenance/content/locator/
vector/digest negatives, stale-attempt and writer/seal orders, cancellation/common
trust failure, rollback and genuinely unknown-C cases are **NOT RUN in this slice**.
The native failure was not bypassed to reach them.

**R7-B NOT_VERIFIED:** no external source was approved/admitted/reserved, no external
content was transferred to a worker, and no index generation or disposable marker
was committed. There is **no new physical PostgreSQL C proof**, readback, freshness/
consent mutation-order, reservation-release, rollback or unknown-C reconciliation
evidence. Those remain mandatory. Historical marker-C and durable finish replay are
not reclassified as publication or unknown-C proof. No SQL row locks, process waits
under row locks or worker grants were introduced; the existing source-first order,
attempt-only stop transaction and NOWAIT fences remain unchanged.

Only sanitized temporary NTFS test files exercised the guard. No test ingestion
route/feature flag/public endpoint was added. Production registration/input denials
remain unchanged; **ordinary live external-file ingestion remains DISABLED**.

## Commands, counts and bounded receipts

All commands below ran from repository root. Let `P` mean
`rag-engine/.venv/Scripts/python.exe`, `W` mean `scripts/run-bounded-command.py`,
and `L` mean `logs/phase18-r7-producer-publication/`.
Prefix each child with `P W --timeout-seconds N --receipt L/NAME --`.

| Child command | Budget | Actual exit / duration | Receipt |
| --- | --- | --- | --- |
| `graphify.exe query 'WindowsNativeFileGuard RagWindowsNtfsFileGuardTest' --budget 1600` | 30 s | 0 / 1.978 s | `graph-query.json` |
| `mvn.cmd -ntp -f backend/pom.xml -DargLine=--enable-native-access=ALL-UNNAMED -Dtest=RagWindowsNativePrerequisiteTest,RagWindowsNtfsFileGuardTest test` | 300 s | **1 / 32.862 s** | `r7-a-native.json` |
| `C:/msys64/ucrt64/bin/gcc.exe -std=c11 -Wall -Werror L/layout-probe.c -o L/layout-probe.exe` | 30 s | 0 / 3.754 s | `layout-compile.json` |
| `L/layout-probe.exe` | 15 s | 0 / 0.014 s | `layout-native.json` |

Maven: **17 tests, 16 pass, 1 failure, 0 errors, 0 skips**. New native suite:
9 cases (including two parameterized count cases), 1 failure, 0 errors/skips;
existing native suite: 8 pass. Maven itself reports 31.364 s; the enclosing watchdog
includes launch overhead. The failed build/exit is retained. Compilation included
existing sources but no unrelated test suite ran. Existing Lombok Unsafe and
deprecated/unchecked API warnings were not suppressed. No IDE-clean claim.

Stdout/stderr were retained with PowerShell `2>&1 | Tee-Object`; `$LASTEXITCODE`
was returned explicitly. The actual watchdog receipt and Surefire XML agree on
failure. Both affected XML and text reports are archived under `L/surefire/`,
alongside console logs, C probe source/binary and receipt hashes in the evidence JSON.
No unchanged full backend/Python suite or P18-FINAL gate was rerun. No OCR execution
occurred. No useful RAM/CPU/temp/VRAM peak measurement was collected; no production
resource limit is proposed or accepted.

## Session files and remaining work

| File | Session change |
| --- | --- |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/infrastructure/windows/WindowsNativeFileGuard.java` | Link-count/layout correction and centralized denial cleanup; pre-existing tracked file. |
| `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagWindowsNativePrerequisiteTest.java` | New untracked focused native/fault tests, including the retained failing lifetime regression. |
| `docs/implementation/handoffs/ACTIVE.md` | Current slice STOP/result pointer only; existing work preserved. |
| This record and `r7-producer-publication-evidence-2026-10-18.json` | New untracked implementer evidence, inventories and next-step boundary. |

Ignored diagnostic/evidence additions stay under `L/`. Inherited untracked models,
parsers, migrations, takeover/checkpoint code and independent reviews are preserved.
The evidence JSON's snapshot comparison checks all inherited bytes, not only Git's
tracked diff; its changed inherited file list must be exactly guard plus ACTIVE.

All original **FR18-1–11 remain OPEN** for independent adjudication. Critical
**FR18-1/2/8** external/child authority and lifetime through C remain incomplete;
**FR18-3** exact producer/result binding remains unchanged and Critical. High
**FR18-6/7/9/10/11** offline/native GPU/fallback, multilingual/table quality,
locator/resource/lifecycle/report and focused/final acceptance evidence remain.
Preserve previously credited generation/purge and VECTOR fixes (FR18-4/5).
Nothing in this slice closes or reclassifies any independent finding.

**Smallest next step, not executed:** a separate independent reviewer validates
this correction and the actual late-alias failure. Any further implementation needs
**a new finite owner resumption decision** after that review, beginning with the
existing guard's continuous alias/namespace prerequisite under the unchanged
contract. Only after native safety passes may P and then R7-B resume. No replacement
framework, broad permission, scope amendment or security workaround is proposed here.
The result does not establish that Windows R7 is impossible.

**Implementation STOPPED.** Normal live ingestion remains disabled. ADR-0020,
ADR-0021/D18-A and AC-01–08 are unchanged; no OCR, further takeover loop,
self-acceptance, commit, push, tag or PR follows this report.

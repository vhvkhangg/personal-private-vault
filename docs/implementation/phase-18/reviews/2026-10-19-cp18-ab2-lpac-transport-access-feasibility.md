# Phase 18 — CP18-AB-2 LPAC transport and access feasibility proposal

Date: 2026-10-19  
Role: independent Codex security architect  
Authority: one additive documentation-only proposal; Phase 18 remains STOPPED  
**Independent classification: NO_VIABLE_LPAC_TRANSPORT_DESIGN_IDENTIFIED**

A supported unpackaged LPAC launch primitive exists. A complete nonprivileged LPAC transport design preserving this worker's HTTP control/input protocol **and** its independent psycopg/libpq PostgreSQL sessions has not been identified. An imported HTTP listener has a plausible receiving interface, but its LPAC/network behavior remains unproved. The SQL path has no identified supported socket/pipe adoption interface in the inspected client stack. Neither problem is solved by naming a broker.

No LPAC policy, adapter, architecture change, experiment or implementation is recommended for execution by this document. This is a bounded design conclusion, not proof that every possible Windows or future PostgreSQL client design is impossible.

## 1. Governing documents and evidence status

Read/reused unchanged binding documents after fresh byte-hash comparison:

- [ADR-0022](../../../adr/0022-phase-18-immutable-snapshot-whole-file-consent-no-change.md), SHA-256 `fa4ce5681093cc758f4a2cea6ee5c8ce486e3fc5aeae31ba346edfafde4cb25d`.
- [A+B corrective integration/proof handoff](../adr-0022-ab-corrective-integration-proof-handoff-2026-10-19.md).
- [Independent Checkpoint 1 review](2026-10-19-adr-0022-ab-corrective-integration-checkpoint-1-review.md).
- [Startup-localization independent review](2026-10-19-cp18-ab2-startup-localization-independent-review.md).
- [Worker-isolation policy comparison](2026-10-19-cp18-ab2-worker-isolation-policy-comparison.md), SHA-256 `efdad550a5c689898e07e2f3bb5be2bd3e79a041a108bb7c5028f602ae782af3`.
- [ACTIVE](../../handoffs/ACTIVE.md), unchanged. Its implementer “IMPLEMENTED & VERIFIED” claims remain superseded in adjudication by the independent checkpoint and current owner STOP; they are not new authority.

Applicable root, docs and backend AGENTS rules were checked. Graphify navigation and local RAG engineering guidance were reused read-only, with important facts verified in source. ADR-0022/D1–D11/N1–N10/WF-1–WF-8, accepted Phase 17 history, Vault privacy rules and retained process/reservation/producer/publication guarantees are preserved.

Evidence labels: **D** documented public API behavior; **E** inspected source or previously independently adjudicated empirical evidence; **I** untested design inference; **K** identified conflict with the required/current contract; **U** unknown host/runtime feasibility. API existence is not host certification.

Prior evidence remains exactly scoped:

- Policy comparison: **NO_CREDIBLE_IN_SCOPE_POLICY_ESTABLISHED**.
- Localization: **POLICY_ACCESS_INCOMPATIBILITY_SUPPORTED / EXACT_CAUSE_UNVERIFIED**. Unique-SID cmd.exe was created/resumed and exited 0xC0000022; measured descriptor checks denied. No exact startup-failing DLL/function/object is established.
- Historical caller was non-elevated **Low IL**, not verified Medium; its restricting list was not queried. This is not a fresh measurement of the Java caller or present session.
- Broad-SID diagnostic credit concerned MSYS/UCRT Python 3.11.7/private fixtures, not CPython 3.14.4/LPAC/transport. Invalid NULL-token and cleanup results remain rejected.
- Historical green A+B test receipts included accepted startup failure and did not prove a functioning restricted worker. No test was rerun here.

## 2. Existing transport contract inventory

All entries below describe current source, not an invented IPC adapter. No secret values, active listeners, database deployment or current process tokens were queried.

| Contract / source | Initiator, receiver, authentication and lifetime | Failure/authority boundary |
| --- | --- | --- |
| [NativeWorkerProcess.java](../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/infrastructure/client/NativeWorkerProcess.java):59–151 | Java launches an explicit executable via ordinary CreateProcessW, suspended/no-window/Unicode environment, bInheritHandles=false. Creates named Job Object; sets kill-on-close, memory and active-process limits; assigns/checks job and records PID/creation FILETIME before return. | No current AppContainer/LPAC token or extended-startup path. Existing job ownership/collision/termination checks supervise processes; they do not confine readable files. |
| [RagWorkerSupervisor.java](../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagWorkerSupervisor.java):66–136 | Java prepares source/batch, commits STARTING, launches with maxProcesses=1, then commits exact worker PID/creation time/runtime/endpoint nonce and ACTIVE **before resume**. One in-memory session per job owns nonce/process/dispatch state; repeated dispatch is refused. | Startup has a 10-second readiness bound. Failure invokes owned stop; no unrestricted fallback. New bootstrap work would have to remain inside this lifecycle. |
| Same supervisor, environment/argv | Base CPython executable plus __PYVENV_LAUNCHER__ pointing to the venv; PYTHONPATH=work/src; no user site/bytecode writes. Runs `-m uvicorn ppv_rag.main:app --host 127.0.0.1 --port <client.port()>`. Passes service key, restricted worker DSN, job/attempt and nonce in the child's environment. | Current cwd is configured work; SystemRoot/WINDIR/TEMP/TMP/LOCALAPPDATA/PATH are copied selectively from caller. Those locations are not approved input grants. No secret is printed here. |
| [RagEngineClient.java](../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/infrastructure/client/RagEngineClient.java):26–109 | **Java TCP client → Python HTTP listener.** Default origin http://127.0.0.1:8001; constructor accepts only exact loopback HTTP origins, no user info/query/fragment/path; no redirect or proxy. Supervisor actually binds worker IPv4 127.0.0.1 at configured port. Readiness uses a job-scoped HttpClient, service key and endpoint nonce. | Readiness response must have exactly seven fields, contract 2.0, READY and exact job/attempt/nonce/PID. Supervisor separately verifies owned PID/creation time/job. Default constructor timeout is 5 seconds, clamped 1–10; readiness uses remaining startup budget. Port is configuration, not proof of endpoint identity. |
| Client execute methods:110–168,282–326; [main.py](../../../../rag-engine/src/ppv_rag/main.py); [security.py](../../../../rag-engine/src/ppv_rag/security.py) | Java POSTs /v1/worker/execute or /execute-batch; X-RAG-Service-Key plus job/attempt/lease/session/nonce headers. Service key requires at least 32 characters. Body contains corresponding identities and prepared payload/roots, input/scope digests and protocol 2.0 or 3.0. Python returns bounded authenticated-protocol results. | Service credential, endpoint nonce, SQL execution-session UUID and lease token are distinct. A returned PID/receipt is not OS isolation or FR18-3 actual-output proof. Send/response failures do not prove SQL rollback. |
| [transport.py](../../../../rag-engine/src/ppv_rag/transport.py):28–170 | BoundWorkerTransport, also exported as BoundedWorkerRequests, authenticates POST headers, enforces one execution slot and exact configured job/attempt/nonce/session/token form. **Python calls SQL claim_input before its first middleware body receive.** Then buffers a bounded body and validates duplicate-free JSON/identity. | Claim failure returns 403; busy 429; missing containment 503; oversize 413; invalid body 422; body deadline 10 seconds/408. Limits: batch 33,554,432 bytes; v2 execution 2,105,344; other POST 2,048. A failed/partial body burns the committed claim; no blind resend. This is the existing middleware consumption boundary, not proof that no kernel/server buffer has received bytes. |
| [db.py](../../../../rag-engine/src/ppv_rag/db.py):13–41 | **Python/psycopg/libpq TCP client → PostgreSQL listener.** DSN host must equal 127.0.0.1 or ::1; service indirection and mismatched hostaddr deny. hostaddr is pinned to host; SSL/GSS encryption disabled in this existing loopback contract. Port/database/authentication come from externally supplied restricted DSN; no value or authentication method is assumed here. | Missing DSN denies. connect_timeout=3; statement/lock/transaction timeouts 5/3/10 seconds. After connection, source verifies session_user=rag_worker and false privileged flags, role memberships and database CREATE/TEMP. LPAC package identity does not replace SQL authentication. |
| db.py:44–116; [worker.py](../../../../rag-engine/src/ppv_rag/worker.py):54–143,350 onward | Each claim, grant verification and staging operation opens its **own connection()/transaction context**; no shared pool or permanent authenticated Java session is supplied. Normal context exit commits and closes in inspected psycopg source; exceptional exit attempts rollback. Worker sends stage success only after context exit. | One job is not one TCP/database session. Loss during COMMIT is potentially unknown; successful socket close, timeout or HTTP ACK cannot establish writer drain or physical outcome. No adapter may replay consumed input or manufacture COMMIT acknowledgement. |
| [V15 attempt/claim fence](../../../../backend/src/main/resources/db/migration/V15__rag_single_job_and_sealed_worker.sql):57–100; [V18 document staging](../../../../backend/src/main/resources/db/migration/V18__rag_document_indexing.sql):263–335 | Restricted functions require rag_worker, check lease/grant/runtime/worker identity/deadlines, lock the attempt row and enforce one-use session consumption. Document staging calls check_worker_attempt(...,true), validates admitted root/type/bounds and has a deferred stage-deadline trigger. | Existing restrictions/locks through physical staging COMMIT must remain. Recorded SQL worker_identity is not an independent attestation by PostgreSQL of a Windows package/PID. FR18-3 remains OPEN; these source controls are not acceptance of complete producer verification. |
| Prepared input: grant service + worker DOMAIN branch | [RagWorkerGrantService](../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagWorkerGrantService.java):40–119 requires supervised readiness, reconstructs/compares Java-prepared batch input and issues a distinct session UUID with session_until=lease deadline. DOMAIN worker rejects caller content_text/content_b64/transclusions and consumes prepared_fields. Documents use prepared text/bytes. | No worker authority to open originals/store paths. Whole-file snapshot protocol changes are not implemented by this proposal; existing v2/v3 behavior and credited Java preparation are preserved. |
| Stop/control | Client cancel/quiescence calls use the authenticated HTTP control client. Worker cancel marks state; /quiescence explicitly cannot prove death/drain. Supervisor.stopAndVerify marks stopping, terminates/verifies job tree, shuts down job HTTP transport, and waits for dispatch to return. | Cancel ACK is not process death, SQL rollback, purge or safe release. Native death, sender/task completion and database writer drain/seal remain separate mandatory facts. |

`rag_worker` is a PostgreSQL role, not a Windows account. Its credentials are intentionally delivered only for restricted worker sessions. Java's trusted business/runtime SQL connections must never become an LPAC worker transport substitute.

## 3. LPAC resource and launch requirements

**D:** Microsoft supports unpackaged AppContainer processes; packaging, a new Windows account and a service are not intrinsic requirements. AppContainer identity is distinct from IL/elevation, and AppContainer processes run Low. [AppContainer for legacy apps](https://learn.microsoft.com/en-us/windows/win32/secauthz/appcontainer-for-legacy-applications-).

The documented launch uses STARTUPINFOEX/SECURITY_CAPABILITIES and PROC_THREAD_ATTRIBUTE_SECURITY_CAPABILITIES. LPAC additionally uses PROC_THREAD_ATTRIBUTE_ALL_APPLICATION_PACKAGES_POLICY with PROCESS_CREATION_ALL_APPLICATION_PACKAGES_OPT_OUT. User/group and application/capability authorization intersect; regular AppContainer has ambient access that LPAC reduces. [Microsoft LPAC launch procedure](https://learn.microsoft.com/en-us/windows/win32/secauthz/implementing-an-appcontainer).

**I/U — identity examined:** P_a denotes a prospective fresh attempt-specific package SID, derived from a valid unique moniker such as ppv.rag.<attempt UUID>, then verified from the actual child token. No such SID/profile exists as a result of this task. U/G denotes the actual caller/user/groups, including inherited restrictions; C_a denotes an explicitly reviewed capability set. The initial analytical baseline is **C_a empty**, not an asserted viable runtime configuration. No filesystem/library/network/COM/registry capability is justified solely by an access error.

**D/U — profile and Low caller:** CreateAppContainerProfile is per-user and can return E_ACCESSDENIED. Neither that API nor the previous Low diagnostic establishes that current Java can create the profile, set/read back owned DACLs/mandatory labels, or launch LPAC. Do not silently invoke an unrecorded Medium helper, discard inherited restrictions, elevate or provision another identity. [Profile API](https://learn.microsoft.com/en-us/windows/win32/api/userenv/nf-userenv-createappcontainerprofile).

**E/U — exact runtime:** .python-version and existing .venv/pyvenv.cfg record **CPython 3.14.4**, with base home `C:\Users\VU KHANG\AppData\Roaming\uv\python\cpython-3.14-windows-x86_64-none`. Pinning is not a self-contained repository runtime. Required exact base/venv/.pyd/DLL/package/model/tokenizer objects and ancestors must be readable/executable as appropriate, not the entire profile/Roaming/repository. No execution/import/model-read or actual selected libpq binary/version attestation was performed.

**I/U — ambient access:** AAP=S-1-15-2-1; ARAP=S-1-15-2-2. Opting out of AAP does not demonstrate that every other shared/package/capability grant is absent. Existing diagnostics captured AAP/ARAP grants on four OS files, but not a full LPAC token/object closure. An unselected object whose effective applicable ambient grants satisfy both authorization components can remain readable. Explicit P_a grants are not an additional conjunctive requirement on every such object. No claim of arbitrary file-path confinement follows from LPAC alone.

Java-owned capture/store must be protected before bytes exist, with verified owner/DACL/inheritance/labels/parents and continuous object identity. Worker must have no known-path data-read/read-map, mutation/security/owner or parent DELETE_CHILD route, and no pre-granted sensitive handle. Originals/unselected files cannot be re-ACL'd to manufacture the boundary. Low IL normally prevents write-up, not read-up; it cannot supply an input read allowlist or an assumed Java/worker integrity difference. [Mandatory Integrity Control](https://learn.microsoft.com/en-us/windows/win32/secauthz/mandatory-integrity-control).

## 4. Both network directions — primary discriminator

| Direction | Unchanged unpackaged LPAC design status | Without elevation/global exemptions | Transfer/adaptation status |
| --- | --- | --- | --- |
| Java → worker: HTTP listener created by current uvicorn CLI | **U/K:** no specified supported LPAC policy establishes access to this listener from outside LPAC. Existing protocol remains valid as a protocol, but network reachability is not established. No LPAC HTTP observation exists. | **Not established.** Network-capability names alone are insufficient. A working packaged-loopback rule cannot be assumed for unpackaged Java/this profile. | A trusted-created listener imported into Python has a plausible server interface (§5), but required LPAC socket behavior/ownership is unproved and current launch does not import it. |
| Worker → PostgreSQL: new TCP session per connection() | **U/K:** current libpq originates each connect from LPAC. No supported exact nonprivileged loopback exception or existing adoption adapter was identified. | **Not established.** No global exception, privileged firewall API or capability broadening is proposed. | WSADuplicateSocket can transfer a socket descriptor, but no supported PGconn construction/adoption route for it was identified in the inspected stack. Pipes cannot be passed as a DSN socket. |

Microsoft's IPC documentation describes default packaged-loopback blocking, package-to-package rules and administrator/debugging exemptions for interaction with unpackaged applications. Java and PostgreSQL here have no supplied package identity; those manifest rules cannot be assumed to name them. This guidance is **not a native trace proving the outcome of this manually launched LPAC**, and the exact host behavior remains U. It provides no complete authorized drop-in route for either direction. [Microsoft loopback guidance](https://learn.microsoft.com/en-us/windows/apps/develop/communication/interprocess-communication#loopback).

NetworkIsolationSetAppContainerConfig describes a debugging loopback allowance and preserving pre-existing global entries. It is not a per-attempt Java-PID/DB-port grant. No invocation or exemption change is proposed. [Loopback configuration API](https://learn.microsoft.com/en-us/windows/win32/api/netfw/nf-netfw-networkisolationsetappcontainerconfig).

**Transport prerequisite unresolved:** both directions must have a supported specified path. Testing only HTTP or only a bare socket would leave the independently originated SQL sessions unsolved. No network trial is authorized or recommended on the basis of the incomplete paths below.

## 5. Adapter assessment — no complete adapter proposed

### 5.1 HTTP-only listener handoff: plausible receiving interface, not a complete proposal

**D:** WSADuplicateSocketW targets a process ID and returns protocol information for a socket import in that process. Sharing also shares socket state and needs coordinated ownership; the API does not document a general AppContainer loopback exemption. [Microsoft socket sharing](https://learn.microsoft.com/en-us/windows/win32/api/winsock2/nf-winsock2-wsaduplicatesocketw). Python exposes Windows socket sharing/import via share/fromshare. [Python socket API](https://docs.python.org/3.14/library/socket.html#socket.fromshare).

**E:** Installed uvicorn/server.py:85–109,135–160 accepts an explicit list of open sockets and passes them to asyncio.create_server. Its --fd branch uses socket.fromfd(...,AF_UNIX,...), marked outside Windows coverage; passing a Windows socket integer to that CLI is not a demonstrated solution. Existing supervisor uses neither route.

A defensible *future HTTP-only specification* would need all of the following; none is claimed implemented or verified:

- **Direction/ownership:** Java creates/binds the exact loopback listener and transfers only that listener to the suspended, attested child's PID. No Java process/token/store handle is transferred. Bind/collision/exclusivity, socket options, exact address/port and all native results must be explicit.
- **Bootstrap authentication:** a single attempt-owned IPC channel conveys bounded protocol info plus job/attempt/nonce and endpoint identity after durable child binding. Both ends authenticate their peer against retained native identity; an echoed PID or secret pipe name is insufficient. Worker imports after resume, then runs a new bootstrap using uvicorn's socket-list interface. Java must not send task content before ordinary bound readiness succeeds.
- **Lifetime:** supervisor owns bootstrap task and source socket; worker owns imported listener and accepted sockets. Import acknowledgement cannot certify release of all duplicates. Startup/IPC/import/readiness failure closes owned source resources, terminates/verifies child/tree and retains unknown cleanup rather than falling back. Keep production maxProcesses=1.
- **Protocol:** service authentication, nonce/PID/job/attempt readiness, input header checks and SQL claim-before-middleware-body-consumption remain in Python. Java remains the HTTP initiator. No broker may answer readiness with its own PID or buffer/replay a task after an uncertain claim.
- **Unresolved API fact:** importing a trusted-created listener is not documentation that LPAC accept/receive operations preserve the required loopback behavior under this host's filtering and security context. Nor is there a specified, proven private bootstrap IPC path from the recorded Low caller.

These require Java native-launch/supervisor changes and a Python entry/bootstrap change, exceeding the current A+B production-Python/transport footprint. Preserving the HTTP bytes does not make those changes authorized.

### 5.2 Why connected-socket transfer does not complete the SQL path

The best authority-preserving shape would transfer **only an unauthenticated TCP connection to a fixed approved PostgreSQL endpoint**, leaving PostgreSQL authentication, restricted role verification, commands, transaction state and COMMIT in Python. Moving TCP establishment alone need not inherently replace worker SQL authority. It still needs a supported client adoption interface.

**E/D:** Current connection() calls psycopg.connect with conninfo. Inspected psycopg connection.py:82–151, pq/abc.py and the ctypes wrapper expose connection creation/polling and a socket getter. The package can select C/binary/Python wrappers; this review did not import one or assume the ctypes implementation is the executed binary. Public libpq describes PQconnectdb/PQconnectStart as creating the connection; PQsocket obtains the socket of an existing PGconn and may change during setup/reset. It is not a constructor/setter accepting a brokered socket. [libpq connection control](https://www.postgresql.org/docs/18/libpq-connect.html), [PQsocket](https://www.postgresql.org/docs/18/libpq-status.html#LIBPQ-PQSOCKET).

No documented socket-adoption parameter was found in the inspected psycopg/libpq interface. WSADuplicateSocket supplies neither PGconn authentication/protocol buffers nor a transferable PGconn pointer. An already-authenticated Java connection or process-local PGconn address cannot simply be reconstituted in the worker. Mutating private libpq structures, intercepting connect, swapping descriptors underneath it or installing a socket-provider shim is not an identified supported adapter and would expand native/dependency/security scope.

Therefore a startup socket pair, inherited socket or additional socket-copy call does **not** complete the actual repeated claim/verify/stage connection lifetimes. DB authentication, error handling, fresh sessions and uncertain COMMIT would still lack a specified implementation path.

### 5.3 Named pipes and brokers: exact remaining gap

**D:** Named pipes support explicit security descriptors. NULL defaults can grant Everyone/anonymous read; generic write also includes pipe-instance creation, so a sensitive channel needs deliberate individual rights. [Named pipe security](https://learn.microsoft.com/en-us/windows/win32/ipc/named-pipe-security-and-access-rights). Packaged/UWP same-package and LOCAL namespace guidance must not be confused with a proven manually created LPAC-to-JVM path.

For any separately designed pipe bootstrap/relay, Java would be the local server and the exact P_a worker the client; creation must reject name collisions/remote clients, grant only ordinary U/G plus P_a required data/synchronization rights, use compatible labels and restrict pipe-instance/security/owner changes. The peer must be matched to the retained child process/token/creation identity and attempt, not just account membership. Bounded framing, deadlines, no file/target-selection commands, no privileged impersonation, checked close/cancel and no surviving relay writer would be necessary. This is a list of conditions, not a complete claimed adapter.

A raw PostgreSQL-byte pipe relay could in principle leave authentication/SQL in Python. **K:** the current psycopg/libpq connection API cannot be directed at that pipe by its existing parameters. A TCP listener/proxy on loopback merely asks LPAC to make another loopback connection, reintroducing the original prerequisite. A named-pipe query/function RPC proxy issuing SQL in Java would change the producer/transaction boundary and is rejected here. No listener/proxy may accept arbitrary endpoints, original paths or trusted database credentials.

**Result:** no complete minimal adapter is proposed. The HTTP interface observation is useful bounded evidence; it does not solve both transports or justify an implementation handoff.

## 6. Mandatory access/principal/proof matrix

This is the **required policy for a hypothetical acceptable LPAC design**, not an installed policy. No actual P_a/C_a token was created. Ordinary U/G access and applicable P_a/capability/ambient authorization must both be satisfied; label and handle constraints remain separate. Java retains existing trusted rights on its own objects.

R = file bytes/read attributes as necessary; RX = required read/execute/read-only image mapping; scratch W = bounded create/write/delete **inside own attempt**, never trusted parent/security rights.

| Resource | Proposed principal/right constraint | Required proof and current status |
| --- | --- | --- |
| Windows runtime/DLLs/NLS/sections/registry/other OS objects | U/G plus effective LPAC system/package/capability rights; only required RX/R. C_a starts empty; add no registryRead/COM/network/device capability without an identified indispensable object and bounded impact. No OS ACL change. | Actual loaded/required object closure and masks/owner/DACL/labels; not only four DLL checks. **D** mechanism, **U** compatibility/minimum capabilities. ARAP/shared grants remain relevant. |
| CPython 3.14.4, base, venv and native dependencies | Exact verified runtime/code objects P_a RX, no write/append/delete/security/owner rights. Necessary ancestor traversal is not repository/profile list/read authority. | Actual executable/base/venv/import/native DLL identities and mutation denial. No broad AppData/Roaming/.venv-parent grant. **E** pin/home, **I/U** access/runtime. |
| Approved model/tokenizer artifacts | Exact verified P_a R/map-read, no writes or directory grant exposing unapproved neighboring content. | Approved artifacts readable; sibling file and mutation denied; model/DLL paths remain stable. **I/U**; no inference/GPU acceptance. |
| ORIGINAL_BYTES, manifests, capture/store temp | No P_a/C_a/AAP/ARAP sensitive access; protected owned descriptors before bytes, owner-rights and parent DELETE_CHILD exclusions. No original/store handle or mapping transferred. | **Known-path FILE_READ_DATA and read-only mapping** denial, listing and every mutation/ACL/owner route; valid trusted controls and continuous held-object identity. **I/U**, existing G3 not accepted. |
| Unselected user/repository/original data | No explicit P_a/capability grant and no applicable ambient route satisfying both components. Must deny known-path reads/writes/read-maps, even if Users/Everyone/AU is readable ordinarily. | Challenge owner-only, broadly readable and separately AAP/ARAP/capability-readable unselected objects. LPAC is not a per-path veto over shared grants. Host/deployment condition **U**, ambient-grant counterexample **I/K**; no unrelated re-ACL solution. |
| Exact approved worker input | Existing one-use Java-prepared HTTP payload; no original/store pathname authority. If a future owned input object existed, exact P_a R only and separate grant/lifetime. | Real exact protocol/header/session claim and no other input route; caller-body/other-attempt negatives. **E** current prepared path, **U/K** LPAC transport. |
| Own scratch/output/profile | P_a minimum R/W on its own bounded attempt tree and compatible Low label, controlled child inheritance; profile/registry content fully inventoried as attempt data. | Own positive; no trusted-parent/ACL/owner escape; expansion and cleanup bounds; no content in shared TEMP. **I/U**. No Medium caller assumed. |
| Other attempts and unrelated data | Fresh P_a; no P_b input/scratch grants, no reusable content profile/shared data capability or leftover handle/ACL entry. | Known-path read/map/write and peer-token/IPC negatives; actual profile identity, cleanup and no unintended re-use. **I/U**. |
| Java process/thread/token/file/job handles | No VM_READ/WRITE/OPERATION, DUP_HANDLE, CREATE_THREAD/CREATE_PROCESS, WRITE_DAC/OWNER or unrestricted token duplicate/assign/impersonate route. No inherited sensitive handle. | Valid actual Java/peer objects and per-right native results, not NULL/error-6 tests. Query-only observation, if permitted, is not authority. **D/I/U**; no actual LPAC proof. |
| Desktop/window station | Confined execution respecting handoff's private-desktop condition; no default-desktop waiver, station ACL edit or process-wide JVM station switch. No hook/journal/switch/security grants for convenience. | Actual station/desktop descriptors and child's binding; creator rights distinguished from worker runtime rights; native dependencies and failure cleanup. **U**. Private desktop does not itself grant station access. |
| HTTP readiness/tasks and SQL staging | Exact worker PID/nonce/service key; SQL role rag_worker, lease/session/attempt fences and physical COMMIT; no Java trusted SQL substitute. | Both real LPAC connection directions plus claim-before-consumption, true producer/staging, failure/cancellation/drain. **K/U**: complete supported transport not identified. |

Enumeration denial is not known-path confidentiality; traversal privileges and object rights matter. Depriving a process of a new file open does not revoke a leaked handle/read mapping. OWNER RIGHTS, inherited grants, NULL DACLs, shared capabilities and client-side credential-file lookups require deliberate treatment; LPAC cannot label a sensitive unselected file “runtime” because a shared grant exposes it.

## 7. Process, identity, cancellation and authority compatibility

A future launch would have to extend the existing native owner, not introduce another process engine. Preserve suspended launch, noninheritance, job assignment/membership/limits, actual child token/package/LPAC attestation, PID/creation identity and physical bind-before-resume. No unrestricted child or ancestor-token fallback. New bootstrap/relay tasks and resources, if ever approved, must be owned by the same dispatch/stop lifecycle.

AppContainer isolation does not excuse permissive handles, broker commands or process DACLs. PROCESS_DUP_HANDLE is particularly dangerous; smaller per-right requests matter even when ALL_ACCESS denies. [Windows process rights](https://learn.microsoft.com/en-us/windows/win32/procthread/process-security-and-access-rights).

CreateDesktopExW creates in the caller's current window station; a NULL descriptor inherits station security, and creation requires DESKTOP_CREATEWINDOW. A private desktop name is not a private station. Exact nonprivileged station access/desktop/native-runtime compatibility remains unproved. [Private desktop API](https://learn.microsoft.com/en-us/windows/win32/api/winuser/nf-winuser-createdesktopexw).

Transport adaptations would have to preserve:

1. Distinct service credential, endpoint nonce, job/attempt/lease and grant session, plus OS-owned process attestation. No “broker is ready” substitution.
2. Python's one-use claim transaction and restricted worker role. A Java-side socket transport must not originate SQL/function calls as a trusted role, reuse its connection or attest producer output.
3. Attempt lock/fence through real staging COMMIT, costly verification outside final row locks, and independent Java trusted publication. Socket/pipe completion is not C_P or C_N.
4. Cancel-first behavior; stopping new sends/claims; positive death of the worker/job tree and any added transport task, actual sender/writer drain/seal before safe release. Lost connection or claim/staging COMMIT response remains unknown until reconciled; no automatic replay.
5. All resource owners across partial profile/socket/IPC/import/startup failure. Parent-held duplicate sockets can keep sessions alive after child death; closing one descriptor is not erasure or transaction completion. Profile deletion alone cannot remove externally granted ACL entries or prove scratch/store cleanup.

No control listed here is newly accepted. Existing DOMAIN/native publication, VECTOR mapping, source-union/source-first reservations, generation-specific purge, parser/chunker/geometry repairs and unaffected E1 guarantees retain their existing credit and limitations.

## 8. Independent feasibility and owner decision

**NO_VIABLE_LPAC_TRANSPORT_DESIGN_IDENTIFIED** within this finite scope.

The decisive gap is an **end-to-end supported nonprivileged path for both existing transports**, particularly repeated worker-owned psycopg/libpq PostgreSQL sessions when direct LPAC loopback is not established. The HTTP socket-list interface is not a solution for SQL; socket/pipe API availability does not supply the missing client integration. Ambient read exposure and actual Low-context profile/runtime/desktop feasibility are additional unresolved conditions, not reasons to fabricate success.

This is not a single unexplained child-startup error that another bare launch could adjudicate. No complete design is available to validate by a small socket-only experiment. No implementation, package/capability list, native adapter or architectural amendment is recommended for adoption.

**Smallest next owner decision:** retain STOP and record **LPAC not selected; no transport adaptation or experiment authorized from this proposal**. The approved documentation-only investigation ends here. No automatic additional proposal/handoff/retry follows.

Any future owner decision to consider a different database-client transport, broker/protocol boundary or broader architecture is separate from A+B. It would first need a concrete supported client interface and preservation argument, explicit scope approval and ADR/frozen-contract review where applicable. This document does not choose that redesign, weaken ADR-0022 or request elevation/accounts/services/global ACL/network changes.

## 9. Minimum subsequent proof — none authorized or presently justified

No runtime proof or experiment is proposed now because the two-direction design prerequisite is unresolved. Repeating unique-SID startup, testing a generic pipe or demonstrating socket import cannot establish the current workflow.

If the owner later supplies/authorizes a concrete supported transport design, the first focused proof would need to cover **both directions in one owned real-worker lifecycle**: exact pinned runtime and token/resource policy, native identity/nonce readiness, Python-owned SQL authentication/one-use claim/staging COMMIT, known-path read/map/escape negatives and cancellation/unknown-outcome cleanup. Its finite commands, fixtures, rights and failure/STOP criteria must be separately reviewed before execution. No broad suite, model/OCR/GPU work, C–H or P18-FINAL is implied.

Existing obligations remain: **CP18-AB-1–7 OPEN; CP18-AB-8 RESOLVED; CP18-AB-GOV-1 recorded; G1/G3 unaccepted; G2/G4 and FR18-1–11 unresolved**, including Critical FR18-3. Historical Antigravity 2/2 exhausted unchanged. Snapshot ingestion INACTIVE; live-original ingestion DISABLED / DEFERRED. Phase 18 STOPPED / INCOMPLETE / NOT_VERIFIED; P18-FINAL mandatory and NOT ACCEPTED.

## 10. Worktree, evidence and fixture preservation

Entry: branch main, HEAD `435ffea979681f22d723f551dd8b0cd0b733c8cd`; inherited dirty tree. Fresh SHA-256 inventory covered **2,138 tracked/nonignored files plus 327 retained Phase 18/Surefire artifacts = 2,465 distinct paths**, matching the previous task's final inventory exactly. Seven specifically inspected ignored venv configuration/dependency source files were additionally hashed; no module was imported or executed. Online Python/psycopg documentation may describe newer patch/development versions; installed source was inspected for the precise interfaces, without claiming runtime/binary attestation.

Final preservation check: all 2,465 inherited inventory paths and seven additional dependency/configuration files remain byte-identical; no inherited deletion; only this requested new Markdown document is added. No preservation sidecar was written. All binding docs, ACTIVE, code, tests, configs/dependencies, SQL/migrations, diagnostic sources and historical reports/receipts remain as found. Historical overwritten-report limitations are preserved, not erased.

The outstanding diagnostic fixture remains owner-controlled and untouched; its current existence/contents/ACLs were not inspected and no cleanup occurred. Refer to [policy comparison §9](2026-10-19-cp18-ab2-worker-isolation-policy-comparison.md#9-outstanding-disposable-fixture--owner-controlled-disposition-only); no cleanup authority is inferred here.

Only documentation/source/evidence reading, navigation, metadata/hash/link checks and primary-documentation browsing occurred. No token/AppContainer/LPAC/loopback/socket/process/filesystem experiment, worker launch, test/build/SQL/model execution, ACL change, cleanup, A+B remediation, C–H, ingestion, P18-FINAL, commit, push, tag or PR.

**STOP — one documentation-only feasibility proposal complete. No isolation policy selected or activated. Implementation and experiments remain NOT AUTHORIZED.**


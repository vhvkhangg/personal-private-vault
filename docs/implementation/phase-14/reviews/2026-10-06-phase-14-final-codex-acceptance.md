# Phase 14 — Codex Final Acceptance

- Date: 2026-10-06
- Reviewer: Codex
- Scope: active `phase-14-portability-storage`, after the third test-only remediation submission.
- Verdict: **READY FOR OWNER COMMIT** — FR14-1–FR14-9 closed; no blocking findings.
- Accepted preparation / HEAD / `origin/main`: `6f1fd00d89a2d0674677cf4c38b814bc434ec253`.
- Frozen Phase 13 implementation: `ef92d94e4b551ec6c7449f251f5186f3e376e0c3`.
- Phase 14 is accepted but not yet owner committed/frozen. This is not a milestone gate.

[Re-review 5](2026-10-06-phase-14-final-codex-rereview-5.md) and earlier reports remain historical evidence.
This incremental review checks the remaining FR14-4 execution paths, retains the eight closed findings and prior
accepted coverage, and independently verifies the complete slice. No production/test implementation by Codex.

## Finding disposition

| Finding | Original severity | Final disposition |
| --- | --- | --- |
| FR14-1 | High | Closed: conservative uncertain-commit retention, exact positive reconciliation and ambient-transaction guard retained. |
| FR14-2 | High | Closed: committed transfers abort without appended JSON; resource cleanup and client-visible truncation regressions retained. |
| FR14-3 | High | Closed: JSONB number/type and fractional temporal fidelity retained. |
| FR14-4 | High | Closed now: deterministic database arbitration and active-consumption/resource-completion coverage accepted below. |
| FR14-5 | Medium | Closed: canonical multipart 400/413 runtime errors and truthful binary/error OpenAPI schemas retained. |
| FR14-6 | Medium | Closed: uncertain storage upload probe/isolation retained; no premature metadata writes. |
| FR14-7 | Medium | Closed: manual recovery fails closed without writer quiescence and fresh unreferenced-key rechecks. |
| FR14-8 | Medium | Closed: populated package descriptors and implementation package-tree synchronization retained. |
| FR14-9 | High | Closed: privacy-safe reconciliation/transfer/advice classifications retained. |

## FR14-4 closure and evidence limits

### PostgreSQL arbitration

`backend/src/test/java/com/vhvkhangg/personalprivatevault/media/MediaStorageIntegrationTest.java:960` and `:1098`
now use a repository proxy that delegates each real checksum precheck before waiting on a bounded barrier. The
actual ImageService runs inside TransactionTemplate. Both absent prechecks finish before either metadata insert
can proceed; reflection exceptions are unwrapped so the canonical constraint translator executes.

The loser must have ImageConflictException caused by DataIntegrityViolationException naming
`images_checksum_sha256_key`, not merely the same exception class from a known-duplicate precheck. Both variants
retain exactly one Image/Vault addition, loser Image absence, downloadable winner-byte equality and bounded/finally
worker shutdown. Successful compensation deletes the loser object; failed compensation attempts exactly three
loser-only keys, preserving the primary conflict and accepted residual orphan. The former arbitration gap is closed.

### Active export resource lifecycle

`backend/src/test/java/com/vhvkhangg/personalprivatevault/portability/PortabilityIntegrationTest.java:918`, `:955`
and `:992` use the real export service, adapter and PostgreSQL connection, with test-only cursor instrumentation.
The prepared cursor successfully advances before injected SQLException 57014 (query cancellation) or 08006
(database connection loss). In the actual adapter this is after opening output/ZIP and a table JSONL entry, unlike
the previously accepted early callback failure at `:851`.

Nonzero prepared-cursor work and tracked Statement/PreparedStatement/ResultSet close calls, rollback, connection
release and deletion of the temporary archive are asserted on failure. Successful generation asserts cursor
release, commit, connection closure and retained nonempty archive; existing ZIP/fidelity and HTTP cleanup tests
remain green. Source try-with-resources confirms ZIP/output closure through these exercised exception paths.

These are controlled faults injected at ResultSet.next, not a physical PostgreSQL cancellation/network event.
The first cursor advancement is counted before returning its row to serialization; no completed JSONL row is
claimed before that fault. The 08006 case models database loss, not HTTP-client loss. Retained real HTTP tests
separately prove client-visible image/export transfer aborts and generated-archive deletion. Together with prior
successful/early-failure/transfer coverage, these satisfy the selected temporary-file strategy's remaining gap.

### Image completion hooks

Media test `:1720` retains actual ImageController/ImageDownloadService success and failing-output branches and
now obtains the actual registered CallableProcessingInterceptor.afterCompletion plus captured AsyncWebRequest
completion/timeout/error handlers. All eight cleanup branches close the opened stream. Directly invoking these
registered callbacks proves application cleanup behavior, not real container event timing; the retained real HTTP
and executor-rejection regressions cover the complementary transport/scheduling behavior. No handmade substitute
stream implementation or production hook was added by Codex.

Current evidence and handoff now make these limits explicit. Unsupported prior submission claims remain qualified
historical evidence, not the basis for closure. No new mandatory scope or repeated already-accepted work is requested.

## Independent verification

- `mvn -f backend/pom.xml -ntp clean verify` — exit 0, **BUILD SUCCESS**, **920 tests**, zero failures/errors/skips;
  **02:50 min**, finished **2026-10-06T18:33:48+07:00**. Before focused rerun, 81 Surefire XML reports contain
  exactly 920 testcase elements. Media 33, Portability 15, architecture 35, schema manifest 2 and OpenAPI 1 pass.
- `mvn -f backend/pom.xml -ntp "-Dtest=MediaStorageIntegrationTest,PortabilityIntegrationTest,OpenApiRouteInventoryIntegrationTest" test`
  — exit 0, **BUILD SUCCESS**, **49 tests** (33 Media + 15 Portability + 1 OpenAPI), zero failures/errors/skips;
  **54.884 s**, finished **2026-10-06T18:36:49+07:00**.
- Windows/Java 25.0.2/Spring Boot 4.1.1/PostgreSQL 18.6/MinIO Testcontainers, Tomcat 11.0.24 wire regressions.
  Approved escalation for dependency cache/Docker; no failed/cancelled Maven command or verification retry.
- HEAD/`origin/main` unchanged; frozen DBML/migrations/architecture/ADR and protected `.agents` show no diff.
  Codex edits only review/governance/evidence docs; no production/test/config/dependency changes or new standalone
  diagnostic. No commit/push/tag/PR.
- Post-synchronization `git diff --check` passes; 107 local links across 11 documents resolve and the checked
  documents have no trailing whitespace. The three checked focused-run review-owned containers are no longer running.

## Mandatory review dimensions

- Business/logical correctness: canonical Image/Vault creation, derived upload identity/hash/size, snapshot fidelity,
  retained history/trash and conservative storage outcomes remain accepted. Full verification preserves the baseline.
- Clean code/reuse: owner-local responsibilities and existing metadata operation remain canonical; test proxies
  instrument real behavior rather than duplicate domain rules. No new cohesion/duplication blocker.
- Extensibility/SOLID/patterns: Media's narrow concrete storage port/adapter isolates provider volatility; immutable
  public contracts and leaf isolation remain appropriate. No speculative strategies/events/generic hierarchy needed.
- Performance: upload spooling and cursor/disk export avoid whole-vault/file heap buffering. No new concrete N+1,
  scan or lock-contention regression found; the earlier extra count traversal remains nonblocking.
- Persistence/concurrency: real PostgreSQL constraint arbitration and actual transaction-boundary faults now have
  meaningful coverage; no cross-module entity/repository sharing or schema rewrite. Snapshot transaction spans reads.
- Security/privacy: bearer-only additive binary operations, hidden public health details, excluded security-state
  tables and sanitized error/transfer/provider handling remain accepted. Residual orphans remain an explicit risk,
  with fail-closed privileged recovery rather than unsafe automatic deletion.
- Tests/evidence: fresh full/focused runs are green; current coverage statements distinguish fault injection/direct
  callbacks from wire behavior. Original 211 HTTP operations and all 867 baseline tests remain under verification;
  the inventory checks exactly 214 operations after the three approved additions.
- Tree/hygiene: meaningful package descriptors and repository tree match the approved delta; architecture/schema
  tests pass. No unexpected tracked cache/artifact or new cross-module dependency found.
- Static diagnostics: Lombok Unsafe, deprecated APIs and unchecked OpenAPI warnings remain; Mockito dynamic-agent/
  CDS, SpringDoc development endpoint notices and Tomcat inspection-access warnings occur. No IDE inspection or
  warning-free claim; no blanket warning suppression introduced by this review.
- Scope/docs: statuses synchronized to this verdict; accepted preparation and historical reviews unchanged.
  Deferred provider/deployment, scheduling, frontend, RAG and other frozen boundaries remain out of scope.

## Owner commit and next gate

Suggested commit message:

```text
feat(backend): add portable exports and managed image storage
```

Owner commits/pushes the accepted Phase 14 slice, then gives the latest repository package to ChatGPT for Phase 14
closeout/freezing and Phase 15 preparation. Agents do not commit/push. Phase 14 itself does not trigger milestone
review; the next milestone follows Phase 15 completion. No further implementation is authorized by this verdict.

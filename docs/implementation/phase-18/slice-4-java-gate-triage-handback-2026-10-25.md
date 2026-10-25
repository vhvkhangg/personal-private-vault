# Slice 4 bounded Java-gate root-cause triage — 2026-10-25

**INVESTIGATION COMPLETE / OWNER_DECISION_REQUIRED. GPU_EVIDENCE_BLOCKED unchanged.**
This is the separately Owner-authorized investigation of the remaining Java query-count failure,
not another corrective cycle. No production, test, assertion, dependency, GPU/CUDA or model change
was implemented. Slice 4/P18-FINAL remain NOT ACCEPTED; no full-suite rerun, staging or publishing.

## Root cause and classification

**Proven for the isolated reproduction: TEST INTERFERENCE — shared Hibernate statistics.**
The original frozen method is unchanged byte-for-byte from accepted Phase 17 commit `829d294`:
`RagSourceAuthorityIntegrationTest.genuinelyFreshBatchProjectionsHaveConstantQueryCountsAndEnforceByteBudget`,
SHA-256 `d3676b2f95041cd2343088e0ad1b0ea6244e1e70b6861d91db318bba182b6fa1`.
It enables and clears **SessionFactory-wide**, not thread-local, statistics at line 82 and reads
the total at line 83. A scheduled `RagRecoveryService.reconcile()` uses that same statistics object.

The diagnostic reproduction captures **five actual foreground Fiction statements plus one actual
scheduled generation-scan statement** between the clear and the assertion. The unchanged assertion
then observes **6 rather than 5**, at the same frozen line 83. No additional foreground query or
N+1/query-budget production regression was demonstrated. FictionSourceService is also unchanged
from the accepted Phase 17 commit; the existing projection sequence remains constant for 1/8 IDs.

**Exact SQL/thread attribution for the historical `final-java-v3` occurrence remains UNKNOWN.**
That run did not retain a per-statement/thread trace. The new failure was reproduced with a
diagnostic scheduling period of **50 ms**, versus the unchanged production default **30,000 ms**.
This proves the interference mechanism and identifies the extra statement in that reproduction;
it does not recover or uniquely prove which background statement occurred in the historical run.
Classify the demonstrated defect as **test interference**, with historical exact-statement
attribution **unresolved**. Do not call the original full gate PASS or claim a historically exact
SQL attribution that was never recorded.

## Isolated original-method reproductions

All three runs execute the **original compiled frozen method through JUnit 6.0.3**, retaining all
original assertions, including 5/6 query budgets for both 1/8 IDs and source byte/selection budgets.
They use existing compiled candidate classes and dependencies; no Maven build or backend target
output is rewritten. The test fixture uses its disposable Testcontainers PostgreSQL database.

An external Java agent observes only Hibernate `StatisticsImpl.clear`, `prepareStatement` and
`getPrepareStatementCount`. A `StatementInspector` records SQL and returns it unchanged. Original
test and production methods are not rewritten by the agent; counters/return values are not altered.
The observer perturbs timing and is diagnostic evidence, not a performance benchmark.

| Run | Diagnostic setting | Actual result |
| --- | --- | --- |
| `baseline` | Default 30,000 ms recovery delay; observation only | **1 PASS**, exit 0, 26.719 s; counts **5, 6, 5, 6**, all counted statements on `main` |
| `fast` | Only process-local recovery delay changed to 50 ms | **1 FAIL**, exit 1, 27.266 s; original line 83 observes **6**; same statistics object receives 5 `main` statements + 1 `pool-2-thread-1` statement |
| `guarded-fast` | Same 50 ms pressure; external launcher holds the existing recovery monitor around the unchanged test invocation | **1 PASS**, exit 0, 25.531 s; counts **5, 6, 5, 6**, all measured statements on `main` |

The guarded run is an **external counterfactual diagnostic**, not an installed correction or a
modified test gate. It initializes the same Spring test context, takes its existing recovery bean
monitor and runs the original method. Scheduling is not disabled and its delay is not relaxed.
The background method can resume after the diagnostic lock is released. The observed recovery
monitor already exists in production; no new runtime coordination was introduced.

### Exact extra statement and origin in `fast`

`fast-events.jsonl` sequence 1 clears statistics object `2092775230`. Sequence 2 records the
following statement on `pool-2-thread-1` in the same statistics epoch; sequences 4/6/8/10/12 record
the five foreground projection statements. Sequence 13 records the original assertion reading 6.
The background SQL is the **extra counted statement**, not chronologically statement number six:

```sql
select rig1_0.id,rig1_0.is_active,rig1_0.authorization_epoch,rig1_0.created_at,
       rig1_0.dependency_manifest_digest,rig1_0.generation_id,rig1_0.job_id,
       rig1_0.published_at,rig1_0.revoked_at,rig1_0.source_id,
       rig1_0.source_revision_hash,rig1_0.status
from rag.index_generations rig1_0
where rig1_0.id>?
  and (rig1_0.is_active=true or rig1_0.source_revision_hash<>''
       or rig1_0.dependency_manifest_digest<>''
       or exists(select rr1_0.id from rag.runs rr1_0
                 where rr1_0.job_id=rig1_0.job_id
                   and rr1_0.purge_status<>'PURGE_COMPLETE'))
order by rig1_0.id fetch first ? rows only
```

Captured stack: Hibernate `StatisticsImpl.prepareStatement` →
`RagRecoveryService.lambda$reconcile$1:113` / `reconcile:109` → Spring
`ScheduledMethodRunnable` / `Task.OutcomeTrackingRunnable`. The original assertion at line 83
reads the same statistics object. Complete SQL, monotonic timing, thread, counter, object identity
and filtered stacks are in [query analysis](../../../logs/phase18/s4-java-root-cause-triage-20261025/query-analysis.json).

The five foreground statements are the description byte-budget aggregate, Fiction projection,
Vault entries, story-archetype assignments and world-setting assignments. The baseline and guarded
runs retain five for both 1 and 8 Fiction IDs; Film retains six for both sizes. Source inspection
shows the 30-second scheduling annotation and generation recovery scan already existed in Phase 17.
Slice 4 adds NO_CHANGE reconciliation calls, but the traced extra is **not** their query. No claim
is made that those new calls caused the historical timing, or that recovery should be removed.

## The 13-test stdout/XML discrepancy — established

**Classification: reporter metadata/count-key collision, not 13 missing test executions.**
All three corrective full runs have **1,580 XML `<testcase>` elements**, matching their Maven
stdout total. Their suite-level `tests` attributes sum to **1,567**. For every suite the attribute
equals its distinct bare testcase-name count. Five suites contain method names reused by distinct
nested classes; all testcase elements remain serialized:

| Suite | XML `tests` attribute | Actual testcase elements | Difference |
| --- | ---: | ---: | ---: |
| CollectionIntegrationTest | 15 | 17 | 2 |
| FictionValidationTest | 37 | 40 | 3 |
| KnowledgeValidationTest | 26 | 27 | 1 |
| LocationValidationTest | 26 | 32 | 6 |
| MediaValidationTest | 22 | 23 | 1 |
| **Total undercount** | | | **13** |

Examples: `createUpdateAndAllowDuplicates` appears in three Collection nested classes;
`rejectsNullCommand` in four Fiction nested classes; Location repeats two parameterized
`blankNameThrows` names and `updateNotFoundThrows` across three nested classes. Complete names,
class display names and per-suite arithmetic are in
[count root-cause evidence](../../../logs/phase18/s4-java-root-cause-triage-20261025/count-root-cause.json).

The installed **Surefire 3.5.6** bytecode confirms that
`StatelessXmlReporter.arrangeMethodStatistics` groups `getSourceName()` then `getName()`;
`createTestSuiteElement` sums inner map sizes for the `tests` attribute. The no-rerun serializer
emits every report entry, including multiple entries under one method-name key. A tiny external
probe using this **installed reporter**, two nested display contexts and one shared method name
produces **completedCount=2 / XML tests=1 / testcase elements=2**, exit 0. This needs no theory about
missing execution, stale files or random test exclusion; the actual XML and reporter round trip
establish the mechanism. No test, plugin version/configuration or historical XML was changed.

The independent review's original `s4-independent-20261024/entry-surefire.zip` has **1,536 attributes
versus 1,549 testcase elements**, explaining its reported 13 difference too. The later corrective
entry archive is a **different preserved inventory**, with 1,439 attributes / 1,452 elements; it
must not be substituted for the independent original archive. The same five suites account for
13 there. A triage helper initially selected that wrong archive for the 1,536 assertion and failed;
the subsequent analysis explicitly distinguishes both archives. It does not invent why those
different archival inventories have different total scopes.

Current `final-java-v3` therefore remains **FAILED, exit 1**: stdout and testcase-element counts
**1,580 / 1 failure / 0 errors / 1 skip**; suite-attribute totals **1,567 / 1 / 0 / 1**. This is a
new explanation, not a rewrite of older UNKNOWN observations or failed evidence.

## Smallest proposed correction — NOT IMPLEMENTED

**No production correction is justified by this investigation.** Do not increase 5/6 budgets,
change the frozen query test, disable recovery, change its production schedule, filter tests or
upgrade Hibernate/Surefire merely to make this gate green.

Recommended next correction, **only if separately authorized by the Owner**: add narrow **test-owned
invocation coordination** in the existing shared fixture
`backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/AbstractRagIntegrationTest.java`.
For the identified SessionFactory-wide statistics measurement, an invocation interceptor would
reuse the existing recovery monitor across the complete original invocation, as the external
guarded diagnostic demonstrates. It must propagate every assertion/failure, leave original test
source/budgets byte-identical, and leave lifecycle/recovery tests' concurrency behavior intact.
This is a proposed test-support change requiring explicit authorization for that shared fixture;
it is not permission to modify frozen Phase 17 contracts/tests or accepted production baselines.
If that fixture is not authorized for alteration, retain the failure and seek the exact Owner
decision rather than patching around it. The proposal is not a claim that a full gate would pass.

For count reporting, no production/test/plugin correction is needed to account truthfully: future
**new** receipt summaries should record testcase-element/event counts and suite-attribute totals
separately, with collision details. Existing raw XML, stdout, summaries and failed receipts remain
immutable. No historical script/report should be overwritten to manufacture agreement.

**An additional Owner-authorized correction checkpoint is necessary** before any shared-fixture
change or further corrective full-gate run. Suggested finite scope: review/implement the smallest
test-only isolation, verify the unchanged original method and relevant recovery concurrency,
then execute the required genuine Java gate if authorized. No new correction is begun here.
Mandatory actual CUDA inference/same-model GPU fallback remains a separate **GPU_EVIDENCE_BLOCKED**
Owner-routing issue; nothing in this triage satisfies or waives it.

## Commands, hashes, preservation and stop

All diagnostics are confined to `logs/phase18/s4-java-root-cause-triage-20261025/`. Exact argv,
cwd, budgets, process-local overrides, argument-file/agent/source hashes, receipts and preservation
results are in the [triage evidence inventory](slice-4-java-gate-triage-evidence-2026-10-25.json).
Javac/Java commands use the existing watchdog; compile budgets 90 s, three original-method runs
240 s each, reporter probe 30 s. No Maven/full suite or GPU/dependency command was executed.
Observer classes and additional JUnit launcher/Surefire jars reuse already installed artifacts;
no dependency download or pin/configuration change.

Entry: main/HEAD `ebe125f7529124b62761e8d01c692cbbd28e4aa7`. All **1,732** final-java-v3 source hashes
match at entry, with no missing/mismatched source. Entry manifests preserve 2,512 repository files,
10,193 existing corrective assets and 2,136 existing backend target files. Final checks compare
them and both raw/semantic Git index hashes; old source, compiled outputs, failures and evidence
are not overwritten. Only a new investigation handback/evidence and a current ACTIVE pointer are
added outside the isolated diagnostic directory. This is investigation evidence, not an independent
review or Slice 4 acceptance. Exact check outcomes/counts are in the machine inventory.

Phase 17 FROZEN; Slices 1–3 and FR18-3 CLOSED in their accepted Markdown scope; D22 originals
NOT_RECOVERED; indexing INACTIVE; live originals DISABLED/DEFERRED. Historical Java gates remain
FAILED. **GPU_EVIDENCE_BLOCKED unchanged. Stop for Owner routing; no correction, staging,
commit/push/tag/PR, publication, activation or Slice 4/P18-FINAL acceptance.**

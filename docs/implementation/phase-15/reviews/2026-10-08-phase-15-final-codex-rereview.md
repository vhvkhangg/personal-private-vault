# Phase 15 — Closure-Remnant Final Codex Re-review

Date: 2026-10-08

Verdict: **CHANGES_REQUESTED** — **test/evidence-only remediation**.

FR15-10 is **CLOSED** for the functional DTO defect. FR15-11 remains **Medium**, narrowed to the explicit
raw-request/state-transition/no-write acceptance controls. FR15-12 retains only **Low** current-evidence
accuracy remnants; its previously blocking semantic/default/package/offset claims are corrected.
No newly demonstrated production defect or further production repair is requested.

## Scope, baseline and authority

This is `$codex-final-review` of the resubmitted single
[`phase-15-backend-audit-remediation` handoff](../../handoffs/ACTIVE.md), which entered as
`IMPLEMENTED_AWAITING_CODEX_REVIEW`. The [first Oct8 review](2026-10-08-phase-15-final-codex-review.md),
[Oct7 closure audit](2026-10-07-phase-15-closure-backend-audit.md), prior acceptances and original evidence remain
historical and unchanged. This review is not a new repository-wide audit verdict or closure of its four BA15
dispositions. The audit gate remains **REMEDIATION_REQUIRED**.

HEAD/local origin/main: `6a89a998c512dda27c3e494a3525bf6118ee981e`; cumulative authorized remediation remains
uncommitted. The [owner decisions](../owner-decisions.md) and
[ADR-0018](../../../adr/0018-phase-15-bounded-backend-remediation.md) authorize only the original bounded slice.
Schema, SQL, module ownership, dependency directions, named interfaces and unaffected behavior remain frozen.
No second handoff, new validation framework, global blank-policy change or static-warning cleanup is authorized.
BA15-13 architecture expansion, BA15-14 new total ingestion limits and any other expansion still stop for
`OWNER_DECISION_REQUIRED`. Agents do not commit/push. Codex writes review/current governance and ignored
diagnostics only, not production code, tests or POM.

## Accepted repairs and coverage

- **FR15-10 functional repair accepted.** The 12 named compact constructors now agree with existing owner policy:
  Account filename/display/note and Shopping/Software/Feed→Study currency use trim-then-empty-to-null, retaining
  U+2003; Location phone uses blank-to-null before length checking; Feed URL retains nonempty Unicode through
  trimmed-length validation before the owner's `trimUrl` blank normalization. Owning validators were not weakened.
  Unknown three-EM-SPACE currencies now reject canonically; historical values are retained; oversized Unicode
  URLs reject and owner-valid Unicode blank phone is accepted. The tests exercise these outcomes, with the
  raw-wire-fixture limitation below rather than a claim that the full HTTP acceptance is complete.
- New Shopping/Software **PUT** branches and GET persistence assertions, maximum/max+1 padded identities,
  converted Study public HTTP title/currency reads, snapshot-entry historical display reads and duplicate
  rejection are useful and preserved. FR15-11 no longer concerns missing PUT routes or numeric-target-ID-only proof.
- Actual generated Software create/update `type` enums are asserted exactly as APPLICATION/EXTENSION, with
  `has("default") == false` and the preserved required-type checks. No runtime NotNull was introduced. These
  structural BA15-15/FR15-11 requirements are satisfied.
- Nullable follower status, owner-defaulted follow status UNKNOWN and source MANUAL, corrected relationship
  display name, actual Collection DTO package, `findByIdForShare` symbol and int-page/offset-threshold evidence
  now match source. No Long.MAX_VALUE HTTP200 claim remains in current test evidence.
- Previously accepted schedule-reader refresh with both actual PostgreSQL contention directions, bounded offset
  guard, recurring ledger descriptions and FR15-1–FR15-9 repairs/tests remain accepted. No reimplementation is requested.

## Remaining findings

The test below is `backend/src/test/java/com/vhvkhangg/personalprivatevault/audit/WebDtoValidationAuditIntegrationTest.java`.

### FR15-11 — Medium — Required wire-input, clearing and atomic-state proof is still incomplete

Concrete locations: `locationNameAndPhoneCreateAndUpdateBoundaries` (2040+),
`followerSnapshotFileNameAndDisplayNameBoundaries` (2175+),
`accountRelationshipOptionalDefaultsAndUncappedNoteBoundaries` (2315+),
`feedSourceOptionalUrlBoundaries` (2400+) and
`shoppingSoftwareAndFeedConversionCurrencyAndPaddedIdentity` (2557–2887).

1. These new normalization cases construct production request DTOs in the test, then serialize them with
   `objectMapper.writeValueAsString(dto)`. Their compact constructors have **already** trimmed padded 506/507
   character identities and currencies or changed the 65-EM-SPACE phone to null **before** MockMvc receives JSON.
   They are useful constructor-plus-HTTP checks, but do not satisfy the handoff's explicit raw JSON controls for
   the server-side binding/normalization path. Do not label those outgoing requests as raw-padded/Unicode inputs.
2. The Shopping/Software PUT comments say full replacement clears currency, but `shopId` / `softId` initially
   refer to rows created with null currency. The blank PUT therefore only confirms null→null. The later padded
   currency PUT sets VND, but no successful blank/null PUT subsequently proves nonnull→null. The explicit null
   and one-ASCII-space cases are also not covered by the new PUT success branches.
3. Negative Unicode/price/currency requests end at 400/422/code assertions. They do not compare persisted state
   after the rejected PUT or count owned/Vault/Study/provenance rows after rejected POST/conversion. The snapshot
   conflicting-copy case (2286+) similarly asserts 422 only, not unchanged headers/entries; its matching duplicate
   case asserts 201 only, not one retained entry. Comments are not atomicity or no-write assertions. The shared
   fixture truncates tables after the test; that cleanup is not evidence that the failing request left no rows.

Consequence: required HTTP normalization, real clearing transitions and no-write/atomic outcomes remain unproven
despite the green suite. This is an explicit acceptance gap, not a newly asserted production bug.

Smallest required correction — **tests and their evidence only**:

- For the named normalization controls, construct raw JSON with strings/maps/JsonNode rather than instantiate
  production request DTOs. Preserve the 506/507 padded values, Unicode phone/URL/currency, historical filename/
  display/note and nulls in the submitted body. Existing DTO-based tests may remain; no generic test framework.
- Seed a nonnull valid currency on each Shopping/Software target, then independently send null, `" "` and ordinary
  ASCII blank currency without price; assert the fresh owning/HTTP read is null and full replacement is preserved.
  Retain padded valid currency and price-required rejection, and maximum/max+1 POST/PUT controls.
- Capture state/count baselines **after fixture preparation and immediately before** each relevant failure. Assert
  rejected PUT leaves name/price/currency unchanged; rejected creates/conversions add no owning/Vault/Study/
  provenance rows. Assert conflicting snapshot copies add no header/entry rows; successful matching duplicates
  produce one entry with the exact retained display. Use existing public/HTTP reads and established test-only
  JDBC count checks, never cross-module production repositories.

Keep all accepted assertions. Focus only on these existing methods and the already requested outcomes; no unrelated
validation matrix, schema change or production normalization rewrite. Run the focused Web DTO suite and required
full clean verification, record actual results, then resubmit the same handoff.

### FR15-12 — Low — Narrow current evidence precision remnants

The substantial former Medium problems are corrected. Remaining current
[test evidence](../test-evidence.md) has:

- line46: 92 audit regressions, despite the current 101 count in the summary/table and independently captured XML;
- lines113/116/117: nonexistent method names `locationNameAndPhoneBoundaries`,
  `feedSourceOptionalUrlAndNormalization`, `personalProfileEmailFreeformAndNationalityNormalization`;
  actual methods are respectively `locationNameAndPhoneCreateAndUpdateBoundaries`,
  `feedSourceOptionalUrlBoundaries`, `personalProfileFreeFormEmailAndNationalityBoundaries`;
- line76 and the submitted handoff describe blank-to-null in Feed compact constructors and a raw length check
  before normalization. Precisely, DTO trim/empty normalization precedes Bean Validation; retained Unicode then
  reaches the owner's trimmed-length check before `isBlank`/null storage normalization. The resulting accepted
  behavior is correct; the implementation sequence should be described accurately.

Correct those current descriptions alongside FR15-11 test evidence. The “atomic”, “no write”, “raw padded” and
“clears” claims must match the state assertions actually added; do not claim successful checks not performed.
Preserve initial/previous formal reports, original captured evidence and historical source-derived/runtime
distinctions. This is not new runtime scope and is not permanent accepted debt.

## Independent verification

From `backend/`:

```text
mvn -ntp -l ../phase-15-final-closure-remnants-rereview-2026-10-08-verify.log clean verify
```

**BUILD SUCCESS**, exit0; **1021 tests, zero failures/errors/skips**, 04:27 min;
finished 2026-10-08T10:32:19+07:00. All actual testcase nodes in 95 Surefire reports agree;
101 audit cases across 14 suites. Extended assertions in existing methods do not necessarily increase test count.
Java25.0.2 / Maven3.9.15, Boot4.1.1 / Modulith2.1.1, PostgreSQL18.6/Testcontainers and existing storage/HTTP/
architecture checks. Prior 920/1012/1021 verification and source-probe evidence remain historical, not restarted.

Warnings were reviewed, not suppressed: media wire test Jackson2 converter deprecation-for-removal,
Lombok Unsafe, ByteBuddy dynamic agent/CDS and Tomcat module-opening/leak-detection notices. No newly demonstrated
warning defect or warning-free claim. No fresh SpotBugs/PMD/CPD/coverage run is asserted by this final review;
the last closure audit's 239/57/70 triage remains historical. The subsequent dedicated audit owns that gate.

Graphify: cached Oct5 graph, verified vocabulary `currency snapshot validation`, BFS depth2;
78 nodes found / 18 shown, truncated at approximately700-token retrieval budget. It supplied navigation only;
current source/services/tests and owner documents determined findings, not stale inferred graph edges.

Pre-document-edit integrity: 1405 source/POM paths, SHA256
`9F470AA9411DDF2CC8DF4A553E85939D88503E32E41727017FFCEE2101950598`;
12 existing review files, SHA256 `A6776190EE438A241A9D023F09942767FBF8225B82B1F62063369966E3600210`;
owner decisions SHA256 `E113B3111B6DE5FA40A72C8ECEFEBF59F1FCA9BB26BA124C6FF9353CED3DA732`;
initial audit SHA256 `5DDA60D92B4F0166E3827A5971DB360FC97393EFF620F6642834FF5BA7456C63`.
The new report is excluded from historical-set comparison. After governance synchronization, source/tests/POM,
all 12 historical reviews and owner decisions matched these fingerprints; HEAD/local origin/main still matched.
Repository checks passed: 1282 production Java files / 365 packages / 99 test Java files, 343 Markdown files /
582 file-target links, no missing package descriptors, placeholders, generated/secret path candidates or missing
file targets. `git diff --check` passed. The checked frozen/resource path diff still contains only the previously
authorized stale migration `.gitkeep` deletion; no new schema/SQL/module/diagram/tooling change is requested.

## Mandatory final-review dimensions

| Dimension | Disposition |
| --- | --- |
| Business correctness | Owner-exact source normalization repaired; no new production defect found. FR15-11 remains acceptance proof, not a new business policy. |
| Clean code/reuse/maintainability | Small DTO-local helpers preserve intentional owner policy differences; no competing domain validator or generic framework. |
| Extensibility/SOLID/patterns/overengineering | No new interface/module/edge/hierarchy; existing narrow public capabilities retained. |
| Performance/efficiency | No new unbounded read, loop/database fan-out or lock; accepted bounded pagination/reader refresh retained. |
| Persistence/transactions/concurrency | Real PostgreSQL/full architecture suite green; explicit failure-state assertions still required by FR15-11. No schema rewrite/persistence leakage. |
| Security/privacy | Existing auth/error/binary regressions green; no new exposure, secrets logging or custom crypto. |
| Tests/evidence | Full independent suite green; one narrowed Medium proof gap and Low evidence precision remnants. |
| Tree/package hygiene | No source tree expansion in this resubmission; package/placeholder/link inventory checked after status edits. |
| Static diagnostics | Executed build warnings disclosed; no blanket suppression or fresh zero-static-warning claim. |
| Scope/architecture/docs | Same bounded handoff and unchanged authority; current gate synchronized, historical reports preserved. |

## Outcome and next gate

**CHANGES_REQUESTED**, for **test/evidence-only** FR15-11 and Low FR15-12 corrections. FR15-10's functional repair
and the useful new assertions remain accepted. No production implementation is requested or authorized by this
return. If the focused tests expose a genuine production defect, report it and return to the existing bounded
implementation workflow; stop for owner decision if it expands authority.

No commit message while the Medium blocker remains. Do not commit/archive/reset; Phase15 is not complete/frozen.
Later handoff acceptance must still be followed by `$codex-backend-audit`; only `BACKEND_AUDIT_READY` permits
owner implementation commit/push.

Next step: Antigravity `/antigravity-test-slice` for the named regression/evidence corrections, then `$codex-final-review`.

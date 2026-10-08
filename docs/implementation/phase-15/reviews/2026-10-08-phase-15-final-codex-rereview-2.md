# Phase 15 — Test/Evidence Slice Final Codex Re-review 2

Date: 2026-10-08

Verdict: **CHANGES_REQUESTED — TEST/EVIDENCE ONLY**.

FR15-10's functional repair remains **CLOSED**. Most FR15-11 controls are now accepted; its **Medium** remnant
is only missing Vault no-write assertions for eight existing rejected creates. FR15-12 retains a **Low**
current-evidence overstatement. No new production defect, production repair or authority expansion is requested.

## Scope, baseline and authority

This is `$codex-final-review` of the test/evidence resubmission of the single
[`phase-15-backend-audit-remediation` handoff](../../handoffs/ACTIVE.md), received as
`IMPLEMENTED_AWAITING_CODEX_REVIEW`. The [previous re-review](2026-10-08-phase-15-final-codex-rereview.md),
[first Oct8 review](2026-10-08-phase-15-final-codex-review.md), earlier acceptances and
[closure audit](2026-10-07-phase-15-closure-backend-audit.md) remain historical and unchanged.
This handoff-specific review does not disposition the closure audit's four BA15 remnants or replace its
repository-wide verdict. The audit gate remains **REMEDIATION_REQUIRED**.

HEAD/local origin/main: `6a89a998c512dda27c3e494a3525bf6118ee981e`; cumulative authorized remediation is
uncommitted. [Owner decisions](../owner-decisions.md) and
[ADR-0018](../../../adr/0018-phase-15-bounded-backend-remediation.md) retain their exact bounds.
No schema/SQL, dependency direction, ownership, named-interface, domain-rule or unrelated behavior change is
authorized. BA15-13 architectural expansion, BA15-14 new total ingestion limits and any other expansion still
stop for `OWNER_DECISION_REQUIRED`. Codex changes only this report, current handoff/governance documentation
and ignored diagnostics, not production code, tests or POM. No second handoff, commit, push, archive or reset.

## Accepted controls — preserve without reimplementation

- Raw maps now send the actual padded 506/507-character identities, Unicode phone/URL/currency and historical
  filename/display/note values into HTTP binding. Required normalization controls no longer pre-normalize those
  inputs by constructing production DTOs. Existing plain-boundary/fixture DTO usage is not a new defect.
- Shopping and Software each seed VND again before each independent null, one-ASCII-space and three-space
  currency-clearing PUT. Fresh GETs prove nonnull-to-null clearing; padded maximum names are retained correctly.
- For both collections, rejected name501, price-without-currency and unknown-Unicode-currency PUTs now prove
  unchanged prior name, price and currency through fresh HTTP GET and database reads. The previous failed
  request's successful unchanged-state assertions establish the same baseline for the next rejected request.
- Feed Study conversion failures compare Study, Vault and provenance counts after SavedResource fixture creation.
  Successful converted Study title/currency public reads remain accepted.
- Conflicting snapshot copies prove unchanged header/entry counts with canonical422 `INVALID_SNAPSHOT`;
  matching duplicates yield exactly one entry with the exact retained U+2003 display through fresh HTTP GET.
- Location owning-row failure counts and prior name/phone assertions, Feed source count/state assertions,
  Personal profile counts, relationship null/default/uncapped-note reads and historical-value controls are accepted.
- Generated Software required-type, exact APPLICATION/EXTENSION enum/no-default assertions and previously
  accepted reader refresh, bounded offset, recurring ledger descriptions and FR15-1–FR15-9 repairs remain accepted.
- FR15-12's stale92 count, three nonexistent method names and Feed normalization/check-order description are
  corrected. The actual audit count is 101; source-owning policies remain unchanged.

## Remaining findings

### FR15-11 — Medium — Eight rejected creates still omit the required Vault no-write check

File: `backend/src/test/java/com/vhvkhangg/personalprivatevault/audit/WebDtoValidationAuditIntegrationTest.java`.

| Existing rejected POST control | Owning count assertions now present | Missing assertion |
| --- | --- | --- |
| Location padded name501 and phone65, `locationNameAndPhoneCreateAndUpdateBoundaries` (2083–2110) | `locations` before/after each rejection | `vault_entries` before/after each rejection |
| Shopping padded name501, unknown Unicode currency without price, price with blank currency, `shoppingSoftwareAndFeedConversionCurrencyAndPaddedIdentity` (2779–2835) | `shopping_items` before/after each rejection | `vault_entries` before/after each rejection |
| Software padded name501, unknown Unicode currency without price, price with blank currency, same method (3023–3079) | `software_items` before/after each rejection | `vault_entries` before/after each rejection |

All these capabilities are Vault-backed. Unchanged owning-table counts alone would still pass if a rejected
request left an orphan Vault identity. The only Vault count baselines/assertions in this test class are in the
three Feed-to-Study failure branches (3275/3288, 3294/3308, 3335/3350); they are captured later and cannot prove
the earlier eight creates left no Vault rows. Shared fixture cleanup cannot substitute for per-failure proof.

This is the remaining part of the previous review's explicit owning/Vault no-write acceptance requirement,
**not a demonstrated runtime orphan bug**. Current Shopping/Software source validates before Vault creation;
no change to that source is requested.

Smallest correction: in exactly these eight existing POST branches, capture `vault_entries` count after all
fixture/setup work and immediately before the rejected request, then assert it is unchanged afterward alongside
the existing owning count. Retain the raw payloads, statuses/error codes and all accepted assertions. Use the
established test-only JDBC count checks; no production repository access, generic helper framework, new test
matrix, additional PUT count requirement or production change. Focused Web DTO tests and full clean verification
must pass and their actual results must be recorded before resubmitting this same handoff.

### FR15-12 — Low — Current evidence overstates coverage of all rejected operations

[Test evidence](../test-evidence.md), line118, says table count invariants for Shopping, Software, Study, Vault
and provenance are verified on **all rejected create, update and conversion operations**. Actual collection POST
checks cover only the owning table; collection PUT checks cover existing name/price/currency, not table counts;
the three conversion failures cover Study/Vault/provenance counts. The submitted handoff narrative is a claim,
not reviewer acceptance of broader proof.

Replace this with precise per-path evidence after FR15-11's narrow assertions are added: rejected Location and
collection creates leave their owning and Vault counts unchanged; collection PUT failures preserve the three
captured fields; conversion failures leave Study/Vault/provenance counts unchanged. Do not claim every table
count is checked for every operation. The relationship request at2445–2450 explicitly sends null status/source
keys, so line115 should say explicit nulls rather than claim omitted-key coverage. This wording correction does
not request another test or change the accepted defaults.

Preserve all formal historical reviews and captured original evidence. Correct only current evidence/submission
precision; do not modify behavior to fit prose or treat this Low follow-up as permanent accepted debt.

## Verification and limitations

Antigravity supplied focused `mvn -ntp test -Dtest=WebDtoValidationAuditIntegrationTest` success, 25/0/0/0,
and latest submitted full `mvn -ntp clean verify` success, 1021/0/0/0, 04:03. These are implementer-supplied
results. The current evidence summary's older approximate duration is not a newly captured reviewer result.

Codex independently ran from `backend/`:

```text
mvn -ntp -l ../phase-15-final-closure-remnants-rereview-2-2026-10-08-verify.log clean verify
```

**BUILD SUCCESS**, exit 0; **1021 tests, zero failures/errors/skips**, **04:00 min**;
finished 2026-10-08T12:27:59+07:00. Actual testcase nodes in 95 Surefire XML reports agree, including 25 Web DTO
cases and 101 audit cases across 14 suites. Java 25.0.2/Maven 3.9.15, Boot 4.1.1/Modulith 2.1.1 and PostgreSQL 18.6
Testcontainers; architecture, storage, real HTTP wire and existing concurrency regressions remained green.
The fresh full run was needed because the test fixtures/assertions changed; prior successful checks remain evidence.

Build warnings reviewed: ApiExceptionHandler/support deprecated API notices, OpenAPI unchecked operations,
media wire-test Jackson2 converter deprecation-for-removal, Lombok Unsafe, ByteBuddy dynamic-agent/CDS and
Tomcat module-opening/leak-detection notices. No new warning defect, blanket suppression or warning-free claim.
No new SpotBugs/PMD/CPD/coverage/dependency-analysis run is asserted here; the historical closure triage 239/57/70
remains evidence, and the subsequent dedicated backend audit owns repository-wide closure.

Graphify used verified `currency snapshot validation` vocabulary against the cached Oct5 graph:
BFS depth 2, 78 nodes found/18 shown at approximately 700-token budget, with explicit truncation. It was navigation
only; current source/tests/evidence and owner documents determined this verdict.

Pre-document-edit fingerprints (SHA256):

- Source/tests/POM, 1405 files: `FEFC2FC545062120555156F23C17C7B3949A6D057B1442089DA04B187AA381BE`.
- Production/resources/POM, 1305 files: `D528052C0C3D3BFE0A215BFE913BE3C9E90F8C64C8E9979BCA07AC4154226B62`.
- Test sources/resources, 100 files: `7A6CD67464E4184A76653B0FDD1485E3C33C73A31226E798FC1D7B05D4E11FBA`.
- All 13 existing formal reviews: `D899B45CD4F96E7A0DCF3BCE70D3E19F95EEE8FDA7C7CC870D4B5A3B905750A4`.
- Owner decisions: `E113B3111B6DE5FA40A72C8ECEFEBF59F1FCA9BB26BA124C6FF9353CED3DA732`.
- Initial audit: `5DDA60D92B4F0166E3827A5971DB360FC97393EFF620F6642834FF5BA7456C63`.

After governance synchronization, all four source/production/test/historical-set fingerprints above matched;
the historical comparison excluded only this newly added report. Owner decisions and initial-audit hashes also
matched, and HEAD/local origin/main stayed at the preparation commit. Codex did not change source/tests/POM
or any of the 13 previous formal reports. This is review-turn integrity proof, not a pre-submission production hash.

Repository checks passed: 1282 production Java files, 365 production packages, 99 test Java files,
344 Markdown files and 593 local file-target links. No missing package descriptors, stale placeholders,
unexpected tracked/unignored generated-or-secret path candidates or missing local targets were found.
`git diff --check` passed. The verification log, read-only scratch helper and Graphify feedback remain ignored.
The checked frozen/resource diff still contains only the already authorized stale migration `.gitkeep` deletion;
no new schema/SQL/module/diagram/tooling change is authorized by this review.

## Mandatory review dimensions

| Dimension | Disposition |
| --- | --- |
| Business/logical correctness | Accepted owner-exact policies retained; no newly demonstrated production defect. |
| Clean code/reuse/maintainability | Existing raw maps/count checks are sufficient; no generic framework or new validator requested. |
| Extensibility/SOLID/patterns | No new interface/hierarchy/module/edge; previously accepted bounded design retained. |
| Performance/efficiency | No new production path; bounded paging/lock/read decisions remain accepted. |
| Persistence/transactions/concurrency | Full PostgreSQL suite green; eight explicit Vault failure-state controls still missing. |
| Security/privacy | Existing credential/error/storage/wire regressions green; no new exposure or unsafe diagnostics found. |
| Tests/evidence | Significant new proof accepted; one narrowed Medium acceptance gap and Low evidence overstatement. |
| Repository/package hygiene | No source-tree expansion requested; final descriptor/placeholder/link/whitespace checks accompany synchronization. |
| Static diagnostics | Actual build warnings disclosed; no claim of a new repository-wide static-analysis clearance. |
| Scope/architecture/docs | Same handoff/authority; review-only governance synchronization, historical reports preserved. |

## Outcome and next gate

**CHANGES_REQUESTED — TEST/EVIDENCE ONLY** for the eight FR15-11 Vault no-write assertions and FR15-12 precision
corrections above. All accepted controls and production repairs remain accepted. No further production repair is
requested. If a test reveals a real production defect, report it and return to the bounded workflow; expansion
still requires owner decision. No commit message while the Medium acceptance blocker remains.

Next step: Antigravity `/antigravity-test-slice`, then `$codex-final-review` of the same handoff. Later acceptance
must still be followed by `$codex-backend-audit`; **do not commit yet**. Only `BACKEND_AUDIT_READY` permits owner
implementation commit/push and subsequent Phase 15 closeout. Phase 15 is not complete/frozen; Phase 16/17 remain deferred.

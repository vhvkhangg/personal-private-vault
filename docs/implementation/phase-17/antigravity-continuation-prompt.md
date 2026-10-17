OWNER-APPROVED SCOPE SIMPLIFICATION — PHASE 17 ALTERNATIVE B

Continue under the existing P17-RAG-FOUNDATION handoff.

The owner has made TWO explicit decisions to eliminate unnecessary verification complexity.

These decisions supersede the previous 600-second command limit and the previously accepted strict OS-enforced offline requirement.

## OWNER DECISION 1 — MAXIMUM COMMAND TIMEOUT: 20 MINUTES

Increase the hard maximum runtime for each external command to:

1200 seconds (20 minutes).

This applies to Maven, Java, Python, pytest, Docker, benchmarks and all other external processes.

Requirements:

- Use real tool-level timeouts or an independent Windows process-tree watchdog.
- Never merely stop polling while leaving a command running indefinitely.
- At timeout, terminate only the tracked command/process tree.
- Preserve stdout, stderr, partial reports and actual exit status.
- Diagnose timeouts rather than increasing the limit beyond 1200 seconds.
- Never kill unrelated applications or services.

Prefer running the complete:

mvn -ntp -DargLine=--enable-native-access=ALL-UNNAMED clean verify

from backend/, using correct PowerShell argument quoting.

The complete Maven lifecycle is now allowed up to 20 minutes.

If it completes successfully, retain complete test accounting and actual exit status.

If it still times out, investigate the cause and report the result accurately. Partitioned tests may supplement verification but cannot falsely be described as a successful monolithic clean verify.

Do not weaken assertions, omit tests or retry failures until lucky success.

## OWNER DECISION 2 — PRACTICAL LOCAL OFFLINE RAG

The owner explicitly selects PRACTICAL OFFLINE operation instead of STRICT OS-ENFORCED NETWORK ISOLATION.

Actual usage:

- Permanently one user.
- Local Windows desktop.
- Film/Fiction are the intended indexed domains.
- Most Film/Fiction information originates from publicly available Internet sources.
- The owner accepts the ordinary confidentiality risk associated with these non-Private Film/Fiction records and personal evaluations.
- NSFW and Private material must still NEVER enter RAG content processing.
- Indexing is performed locally with no required external/cloud AI service.

### Preserve

- Java -> local Python -> PostgreSQL execution.
- No required Internet connection for indexing.
- No external cloud API dependency for Phase 17.
- No intentional external transmission of source content by the indexing pipeline.
- Existing RAG consent, source eligibility, NSFW/Private exclusion and provenance controls.
- Durable reservations, PostgreSQL transaction consistency and safe recovery.
- Existing working native process supervision.
- The approved E1 contract and independent persistent index lifetime.

### Explicitly REMOVE as mandatory Phase 17 acceptance criteria

- Windows Firewall outbound-denial rules.
- WFP 5157 event collection.
- Administrator elevation for network enforcement tests.
- System DNS/Dnscache blocking.
- Global IPv4/IPv6/proxy denial enforcement.
- Windows firewall or DNS audit-policy modifications.
- Mandatory OS-level proof that every possible RAG outbound connection is blocked.

These are not required for the owner's personal local RAG.

Do not execute windows_offline_controls.ps1 or change the owner's host network configuration.

Do not redesign the RAG architecture or implement another network-isolation framework.

## CONTRACT CHANGE AUTHORIZATION

The owner explicitly authorizes a NARROW amendment to the accepted Phase 17 offline requirement to reflect PRACTICAL OFFLINE operation.

Update only the affected canonical contracts, owner-decisions, applicable ADR addendum, acceptance matrix, implementation documentation and relevant current handoff references.

Preserve the original strict requirement and review history as historical evidence. Explicitly record that the owner replaced that requirement.

Do not silently reinterpret the old strict FR17-10 requirement as previously verified.

Separate:
1. Requirements removed by explicit owner approval.
2. Remaining practical-offline requirements.
3. Tests that actually verify those remaining requirements.
4. Guarantees that are no longer claimed.

FR17-10 must be independently reassessed under the newly approved scope, not self-closed.

Avoid broader contract or architecture changes.

## PRACTICAL OFFLINE VERIFICATION

Verify the existing Phase 17 indexing workflow using real local components.

Confirm:
- Java and Python can execute the supported indexing workflow.
- PostgreSQL staging and publication succeed.
- The workflow does not require a remote AI/cloud API.
- External network availability is not a functional prerequisite.
- Existing local endpoints and dependency configuration are appropriate.
- Cancellation and safe recovery remain valid.
- No NSFW/Private input becomes authorized.

Use focused static/configuration inspection and real local runtime tests.

Where practical, test with external services unavailable without changing system-wide Firewall, DNS, routing or unrelated applications.

Do not claim OS-enforced egress isolation.

If a complete disconnected-network execution has not actually been demonstrated, report that exact limitation instead of claiming it passed.

## REMAINING VERIFICATION WORK

Review the latest remediation evidence and the prior Independent Final Review.

1. Independently validate the current source-before-owner lock ordering through maintained PostgreSQL/Java tests.

2. Run full Maven clean verify with the new 1200-second limit.

3. Check the frozen RefreshTokenLifecycleIntegrationTest result.

4. Investigate Windows/WSL2/PostgreSQL clock skew without modifying frozen authentication implementation or assertions.

5. Preserve the complete 420-job native resource benchmark evidence and its threshold dispositions.

6. Verify complete test identity accounting, including all backend domain modules.

7. Audit the remaining B01–B20 evidence, separating real execution from mocks and unverified subcases.

8. Reconcile documentation with actual code, test receipts and current contract amendments.

Do not introduce unrelated features or additional architectural complexity.

## GOVERNANCE

- Existing handoff: P17-RAG-FOUNDATION.
- Alternative B / ADR-0020 remain authoritative.
- Preserve all historical independent reviews.
- Do not reintroduce F+H+V+E.
- Do not modify frozen authentication code.
- Do not remove required NSFW/Private safeguards.
- Do not create another handoff.
- Do not start Phase 18–22.
- Do not commit or push.
- Do not self-approve final implementation acceptance.

Keep the active handoff at CHANGES_REQUESTED while mandatory verification remains incomplete.

Keep findings open pending independent review.

## DELIVERABLE

Provide:

1. Exact offline contract amendments.
2. Full Maven clean verify result, duration and exit code.
3. Complete Java/Python test accounting.
4. Deadlock regression results.
5. Refresh Token clock-boundary result.
6. Practical offline runtime evidence.
7. Resource and benchmark evidence.
8. Updated B01–B20 acceptance status.
9. Remaining actual defects or unverified requirements.
10. Readiness for a NEW Codex Independent Final Review.

Prioritize finishing the approved personal RAG system.

Do not introduce enterprise-grade security controls, distributed coordination or operating-system-wide network restrictions that the owner does not require.

Proceed with the narrow contract update and remaining verification.

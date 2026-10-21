# D23 exact selective Owner staging procedure — 2026-10-21

**Owner execution only. The reviewer has not staged anything.** Read the [independent delivery review](2026-10-21-slice-2-d23-independent-delivery-readiness-review.md) first. This procedure prepares the index for D23's subsequent independent staged-tree review; it does not grant commit/push readiness.

## Fixed inputs and boundaries

- Baseline: `c3b46351507803053850e3c2711602ee9c9ec419`, branch `main`, initially empty staging area.
- Source: exactly the reviewed **34 changes / 88-file reconstruction**, original patch SHA-256 `d063d6890b431f95a9e32e45364461f6d047586e9ad97224b9fe18c693642931` and its reconstruction manifest. Seven Python CRLF restorations are needed to reproduce the reviewed working bytes. The index representation follows the unchanged `.gitattributes` LF rules and is separately hashed.
- Documentation: **274 enumerated existing candidate changes plus four new independent review/procedure/evidence/scope records**. `AGENTS.md` and root `README.md` select only their adopted D23 prefix on the recorded HEAD bodies. Their older inherited working changes remain unstaged.
- Total: **312 changed index paths**, each with expected Git blob OID, SHA-256 and mode in `logs/phase18/s2-d23-independent-delivery-2026-10-21/owner-staging-manifest.json`.
- The complete patch is `logs/phase18/s2-d23-independent-delivery-2026-10-21/owner-selective-delivery.patch`. Its hash is pinned by that manifest; the manifest has a separate SHA-256 sidecar. Keep these local review artifacts with the original source patch and reconstruction manifest.
- **Archival documentation is stored as exact raw bytes**, including 33 existing payloads with CRLF. Ordinary text filtering would invalidate their historical hashes. The index-only patch preserves those blobs directly; source files use the reviewed Git LF representation. `.gitattributes` is unchanged. Do not substitute `git add` or `--renormalize` for this procedure.

The root transfer envelopes `README_PATCH.txt` and `PATCH-MANIFEST.json` are **excluded**. They have been replaced by D23 packaging, while their older hashes remain in earlier inventories. This is an explicit refinement of the earlier catch-all proposed documentation bucket. They are not D17 acceptance/incident receipts or canonical Owner decisions. No original-byte preservation PASS is claimed for those two envelopes. All protected canonical acceptance/history assets are preserved, subject to the expressly unchanged three-capture D22 exception.

## Read-only preflight

Use PowerShell from the repository root. Stop on any failure. The verifier is read-only, checks the manifest/patch sidecars, recorded HEAD/branch, empty index, selected working input hashes, attributes, and index-only patch applicability. It writes neither the index nor Git objects.

```powershell
$deliveryPython = Join-Path (Get-Location) 'rag-engine\.venv-d17\Scripts\python.exe'
$deliveryVerifier = 'logs/phase18/s2-d23-independent-delivery-2026-10-21/verify-owner-staging.py'
$deliveryPatch = 'logs/phase18/s2-d23-independent-delivery-2026-10-21/owner-selective-delivery.patch'

& $deliveryPython -B $deliveryVerifier --before
if ($LASTEXITCODE -ne 0) { throw 'Delivery preflight failed; stop without staging.' }
```

Do not clear pre-existing staged work, overwrite working files, edit eligibility flags, regenerate dependencies or substitute a different patch to bypass a failed preflight. A changed input requires review of that input. The verified patch stages content directly from HEAD rather than copying the mixed working files.

## Owner stages the exact patch

**This is the only index-writing step. The reviewer has not executed it.** It operates on the supplied exact file/hunk patch, never on a directory or the complete dirty files.

```powershell
git apply --cached --whitespace=nowarn $deliveryPatch
if ($LASTEXITCODE -ne 0) { throw 'Selective staging failed; stop and inspect the index.' }

& $deliveryPython -B $deliveryVerifier --after
if ($LASTEXITCODE -ne 0) { throw 'Staged content differs from the reviewed manifest; do not commit.' }

git diff --cached --stat
git diff --cached -- .gitignore AGENTS.md README.md rag-engine/src/ppv_rag/ocr/engine.py rag-engine/src/ppv_rag/parsers/models.py rag-engine/src/ppv_rag/limits.py
```

`--whitespace=nowarn` retains intentional Markdown fixture hard-break spaces. The read-only postcheck compares the **complete** staged path set and every blob/mode to the expected canonical index bytes, not merely a diff summary. It also rechecks the supplied patch/manifest and baseline. No `--reject`, `--3way`, whitespace repair, add-all, directory staging or full mixed-file staging is used.

The index may deliberately differ from the working copies of shared files. Remaining OCR/PDF/Office and inherited policy hunks stay visible as unstaged changes; preserve them. Do not run cleanup/reset commands to make the repository look clean. The patch/checks do not delete deferred code or runtime/model evidence.

## Stop after verification

Return the exact staged state and verifier output for **independent staged-boundary review**, as required by D23. Owner commit/push remains a separate explicit gate; this procedure contains no commit/push/tag/PR command and starts no implementation.

Phase 17 FROZEN; Slice 1/D14 CLOSED; D23 bounded producer-local Slice 2 CLOSED; FR18-3 OPEN; Java identity/publication NOT ACCEPTED; external indexing INACTIVE; live originals DISABLED/DEFERRED; historical Java full gate FAILED (1,433 / 1 failure / 2 errors); P18-FINAL NOT ACCEPTED; Slices 3/4 unauthorized; Antigravity 2/2 exhausted.

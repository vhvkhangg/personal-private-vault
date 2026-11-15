# Starting baseline, scope inventory and evidence boundaries

## 1. What was actually checked for preparation

- Remote GitHub `main` HEAD **`4e4e2cb50ac84c86b0df93ad67f770648c7d32db`** (committer timestamp `2026-11-15T11:49:18Z`; message `docs(backend): close BF26-V1 publication and sync navigation`).
- GitHub `main` recursive tree API returned **4,268 entries** with `truncated=false`. **This number counts entries including directories, not 4,268 reviewed files**. The path-prefix counts below represent tracked blobs at that commit and may overlap.
- Inspected selected current Owner policy, ACTIVE, audit intent, V1 closeout, Dashboard API-readiness, audit/handoff/review skills, project Codex config, root AGENTS and related metadata.
- **Not checked:** local worktree/index, full code and document contents, actual Codex CLI version/config trust, hooks execution, Maven tests, live integrations or any runtime behavior. None are falsely marked PASS here. **The Owner-uploaded repository ZIP contains no `delivery/` entries**; ignored, untracked or separately generated local artifacts are not observable from this fact.

| Tree path/pattern | Observed blob count | Future audit inclusion |
|---|---:|---|
| `.agents/**` | 82 | Every tracked file: 43 `SKILL.md` entries, agents, rules, hooks, assets, referenced instructions |
| `.codex/**` | 3 | Config, README, owner Git rules; effective runtime/global settings must be inspected later |
| `delivery/**` | 0 in the **Owner-uploaded repository ZIP** (not a verified local directory check) | H0 must inspect actual local ignored/untracked/generated `delivery/` and packaging/review producer scripts, or report absent/unverified; do not treat ZIP creation as approval |
| `**/AGENTS.md` | **22** | Tracked blobs whose basename is exactly `AGENTS.md` at drafting HEAD: root 1, Backend root/modules/tests 20, Docs 1; inspect actual Codex resolution semantics |
| `docs/adr/**` | 30 | Canonical decisions, historical status and superseding decisions |
| `docs/architecture/**` | 56 | Architecture, contracts, diagrams/source and exported artifacts |
| `docs/database/**` | 9 | DBML, version/freeze/migration contracts |
| `docs/implementation/**` | 1,076 | Current authority **and** archival provenance; risk-tier review |
| `docs/repository/**` | 1 | Package-tree-to-source consistency |
| `docs/workflow/**` | 20 | Policy, cadence, prompt, resume and Docs Sync |
| `backend/**` | 1,925 | Non-RAG source/test/config/migrations/build+resources; scope by path and class, not assumed content |
| `scripts/**` | 10 | Relevant build/watchdog/safety/ops scripts; executable scope by actual use |
| `rag-engine/**` | 166 | **Functional RAG excluded**; boundary/shared dependency/read-only impact only |

Other tracked `docs/**`, root build/CI files, deployment/test infrastructure and relevant Frontend adapters are **not excluded** merely because omitted from this short table. Scope inventory shall derive actual paths from the **audit-start published HEAD**, not this frozen planning snapshot.

## 2. Canonical current state / historical distinction

- `ACTIVE.md` starts `NO_ACTIVE_HANDOFF`, V1 publication/CLOSED at source commit `54b62627d7d9`, following closeout docs at current HEAD.
- V1 accepted 112 **distinct** source-applicable test invocations (49 reviewer-fresh, 63 reused); 2 LOW backlogs remain. No full app or live production proof.
- DP-A/B1/W1/RO-A/RO-B have accepted historical statuses; W1 PUBLIC Film POST **was later revoked** by RO-A. Current PUBLIC reads = eight allowed GET pairs, Film POST denied.
- S5 Option A has **published docs-only API readiness** for Inbox/Radar/Vocabulary previews and search navigation, **not** Frontend Dashboard UI/E2E.
- H1-F remains `PAUSED / NOT_INDEPENDENTLY_ACCEPTED`, FR-13 upstream-blocked; P17 frozen, P18 closed, S19-2 suspended/archival-only; no provider activation implied.

Historical documents remain inspectable and may be indexed/validated, but **must not be rewritten to change an event-time claim**. If old and new claims diverge, date and classify; patch only the legitimate current canonical source/derived navigation when separately approved.

## 3. Baseline qualification required before each future audit session

Record, without changing Git state:

```powershell
# Read-only; examples only, for a separately authorized audit session
git branch --show-current
git rev-parse HEAD
git status --short
git ls-remote origin refs/heads/main
git ls-files --cached
codex --version
```

Qualify: current `main` vs `origin/main` vs remote published SHA; worktree/index; exact Owner authorization, LIVE ACTIVE state, scope, parent commits, file overrides and frozen authorities. **STOP** if any required identity conflicts, protected local changes are threatened, or a different active handoff exists. No `checkout`, `reset`, `clean`, stash, worktree creation or rebasing as an implicit fix.

At audit start, the future executor shall save a **path-level inventory** from `git ls-tree -r --full-tree <approved-HEAD>` (file path, blob hash, type) and mark each in-scope path `DEEP_REVIEW`, `STATIC_VERIFY`, `HISTORICAL_PROVENANCE`, `BOUNDARY_ONLY` or `EXCLUDED_WITH_REASON`; record reviewer, proof and coverage status. A file omitted without reason fails full-scope completeness. Do not reproduce all 1,000+ historic reports as separate new findings just because they are old.

## 4. Source of truth and precedence for disagreement

1. Actual observed source/runtime behavior (for **implementation facts**), tested at the relevant SHA.
2. Explicit latest applicable Owner authorization/decision and signed accepted contracts (for **allowed behavior**). Later Owner revocations override older grants.
3. Independent review evidence and actual Owner publication SHA (for **accepted status**).
4. Current canonical ADR/architecture/DBML/workflow/ACTIVE (for intended current state), subject to above authority.
5. Historical reviews/handbacks and old `CURRENT` banners as **dated evidence only**.
6. External OpenAI/GitHub guidance as **reference**, never an automatic change in PPV's permission model.

An observed code/intent conflict is a **finding to adjudicate**, not proof that source or a stale document should always win. Preserve the actual conflict until Owner resolves it.

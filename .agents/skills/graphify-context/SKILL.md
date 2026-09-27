---
name: graphify-context
description: Use the local Graphify code graph for token-efficient repository navigation, then verify important facts in source files.
---

# Graphify Context

Use when a task requires understanding relationships across multiple files/modules.

## Behavior

1. Check whether `graphify` is available and `graphify-out/graph.json` exists.
2. If unavailable, silently fall back to normal targeted file inspection; Graphify is never a blocker.
3. Run at most a few targeted queries first, for example:
   - `graphify query "reference module persistence and service relationships"`
   - `graphify query "vault capability metadata repositories"`
4. Use graph results to select exact files.
5. Verify architecture/business claims in canonical source/docs before editing.
6. Do not dump the full graph/report into the conversation.
7. Refresh only after meaningful code changes, not before every query.

Graphify is navigation/cache context, not the architecture source of truth.

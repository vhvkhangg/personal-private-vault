---
trigger: model_decision
description: "Use minimal context and Graphify-first navigation when broad repository understanding is needed."
---

**SHORT OUTPUT:** Console should show one-line status, affected file names or diff --stat, key error and evidence path; never print full added-file diffs/test stdout to chat. Keep complete logs/receipts as they already exist in ignored `logs/`, no cleanup. `model_verbosity=low` is not a CLI tool-output limit. See [Owner policy](../../docs/workflow/current-owner-delivery-policy.md).



# Context Efficiency

- Start with `docs/implementation/handoffs/ACTIVE.md`.
- Open files explicitly named by the handoff before searching broadly.
- When Graphify is installed and `graphify-out/graph.json` exists, query it before broad grep/tree exploration.
- Limit initial Graphify navigation to a few targeted queries.
- Verify decisions against canonical source files.
- Do not copy large schema/migration/log bodies into handoffs or reviews when paths and exact findings are sufficient.
- Avoid rereading unchanged files within the same task.

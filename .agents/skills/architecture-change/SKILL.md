---
name: architecture-change
description: Safely evolve a frozen database/module/repository architecture decision by assessing impact, updating ADRs and canonical sources, and checking dependency consistency.
---

# Architecture Change

Use only when the owner explicitly requests a change that affects a frozen baseline.

## Procedure

1. Identify the affected frozen artifacts and ADRs.
2. State whether the request changes:
   - database logical schema;
   - module ownership or dependency direction;
   - repository/package structure;
   - security/authentication model;
   - integration/storage strategy.
3. Check downstream impact before editing.
4. Update or create an ADR explaining:
   - context;
   - decision;
   - alternatives considered;
   - consequences.
5. Update the canonical source first:
   - DBML for logical database;
   - Structurizr DSL for C4;
   - draw.io source for BFD/visual architecture;
   - repository tree document for package structure.
6. Update derived exports/docs.
7. Re-check module cycles and ownership.
8. Preserve unaffected artifacts.
9. Report exactly which baselines changed and why.

Do not change a frozen baseline merely to accommodate an incidental implementation shortcut.

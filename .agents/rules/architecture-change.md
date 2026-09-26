---
trigger: model_decision
description: "Apply when a requested change may alter module boundaries, frozen database schema, cross-module dependencies, repository structure, or an accepted architectural decision."
---

# Architecture Change Rule

Before changing a frozen architectural baseline:

1. identify which frozen artifact/ADR is affected;
2. explain the impact and dependency consequences;
3. obtain explicit owner approval if it is not already present in the request;
4. update/add the ADR;
5. update all canonical sources and derived docs consistently;
6. verify there is no new module cycle or ownership violation.

Do not treat a convenience refactor as sufficient reason to rewrite a frozen baseline.

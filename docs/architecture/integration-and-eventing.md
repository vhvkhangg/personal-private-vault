# Integration and Eventing

## 1. Principle

Inter-module communication uses a **hybrid model**:

- synchronous public module APIs when the caller requires an immediate result;
- application/domain events for decoupled side effects.

Do not use events simply to avoid a method call.

## 2. Synchronous examples

```text
film -> people        validate/look up a person
study -> account      validate/look up a YouTube channel
finance -> reference  validate currency/reference data
personal -> location  resolve an address/location reference
```

The called module owns its persistence. Callers do not inject its repositories.

## 3. Event examples

Candidate events include:

- `VaultEntryDeleted`
- `VaultEntryRestored`
- `FollowerSnapshotImported`
- `SavedResourceCreated`
- `ImportCompleted`

The exact event catalog is not frozen until corresponding use cases are implemented. Do not create placeholder event classes before a real consumer/side effect exists.

## 4. Transaction boundaries

The module that owns the use case owns its transaction boundary.

Cross-module synchronous calls must avoid hidden chains of repository mutation. If a workflow needs multi-module side effects, prefer a clear application orchestrator and/or post-commit event where consistency requirements permit it.

Distributed transaction machinery is not required in the monolith.

## 5. External feeds

`feed` supports configured sources including:

- GitHub Trending;
- Hacker News;
- Reddit;
- RSS;
- technology websites.

Refresh can be manual or scheduled.

Feed adapters are infrastructure concerns. Source-specific payloads should not leak into stable domain contracts; source-specific metadata may be retained in JSONB where needed for provenance.

## 6. External social/account platforms

The vault primarily stores external account/post metadata and links. API/browser automation is not assumed to exist for every platform.

Where official APIs cannot safely/reliably provide an operation, manual entry/import remains an acceptable workflow. Platform scraping/browser automation is not an architectural dependency.

## 7. Scheduled work

Scheduled jobs may support:

- feed refresh;
- recurring finance rules;
- subscription-related scheduling;
- future backup automation.

Scheduling belongs in the module that owns the business rule, with infrastructure scheduling adapters triggering a public/internal application use case rather than embedding domain logic in scheduler callbacks.

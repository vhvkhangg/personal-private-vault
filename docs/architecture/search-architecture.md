# Search Architecture

## 1. Requirement

The system provides one global search experience across supported vault content while preserving module ownership.

Searchable content includes, where applicable:

- titles/names/usernames;
- original titles;
- descriptions;
- reviews;
- notes;
- Markdown content;
- character names;
- tags.

Required behavior:

- case-insensitive matching;
- partial matching;
- fuzzy matching for close names;
- filtering by module/type/tag;
- multi-filter combinations where applicable.

Vietnamese accent-insensitive matching is not required.

## 2. Module boundary

`search` is a dedicated orchestration module.

It is deliberately **not** part of `vault`: if `vault` queried feature modules, foundation dependencies would reverse and create cycles.

No business feature module depends on `search`.

## 3. Initial implementation

The initial implementation should remain PostgreSQL-first.

Conceptually:

```text
GlobalSearch
├── FictionSearch
├── FilmSearch
├── PeopleSearch
├── MediaSearch
├── LocationSearch
├── KnowledgeSearch
├── CollectionSearch
├── AccountSearch
└── FeedSearch
```

Each feature exposes a stable search/query contract; `search` merges results into a global result model.

## 4. PostgreSQL capabilities

Use normal relational indexes plus PostgreSQL search capabilities where evidence supports them. `pg_trgm` is a likely fit for partial/fuzzy name matching. Full-text search may be used for long-form content where it improves relevance.

Do not add Elasticsearch/OpenSearch simply because the system has search.

## 5. Evolution path

If fan-out querying becomes measurably inefficient, the module boundary remains valid. `search` may evolve to maintain a central search projection updated from module events while feature modules continue to own their source data.

This evolution does not require moving domain tables into the search module.

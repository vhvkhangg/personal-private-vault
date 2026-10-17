# Phase 17 local migration and isolated runtime

The long-running Java application must not use the migrator or own schema/functions. RAG refuses unsafe or superseded runtime identities. Legacy Phases 0–15 configuration remains available but cannot authorize RAG work.

## Build the exact development database

The Dockerfile pins PostgreSQL 18.6 by upstream digest, builds pgvector 0.8.6 at commit `8ee86c96f0fd72390f890aa8a336fda6d3ab4c6c`, and verifies its archive SHA-256. Supply `PPV_DB_PASSWORD` through the environment before Compose; it is the development migrator/bootstrap credential. Preserve existing volumes; never reset owner data for verification.

```powershell
docker compose -f backend/compose.dev.yml -f backend/compose.rag.dev.yml up -d --build postgres
cd backend
mvn -ntp -DskipTests compile dependency:build-classpath -Dmdep.outputFile=target/rag-dev-classpath.txt
cd ..
```

Runtime base: `postgres@sha256:5a5a84b19854a9ffaa54082c166ff4ec27473a361e496e5ea167f298f2da9722`. Source archive: `d076a3098010905fd60256649327809651f6288327db6413f0938305f62ea299`. Build-time Debian compiler/header resolution is not a bit-identical apt-snapshot guarantee. The disposable compatibility test checks actual PostgreSQL `180006`, extension `0.8.6`, vector cast/distance, every migration, and V6→V13 unsafe-role/legacy-authority repair.

## Separate migrator process

Set all required environment variables without writing their values to logs/Git:

- `PPV_RAG_MIGRATOR_URL`: `jdbc:postgresql://127.0.0.1:5432/personal_private_vault` (adjust the local port/database).
- `PPV_RAG_MIGRATOR_USERNAME`, `PPV_RAG_MIGRATOR_PASSWORD`: privileged local migration identity, used only by this process.
- `PPV_RAG_RUNTIME_PASSWORD`, `PPV_RAG_WORKER_PASSWORD`: separate generated secrets, each at least 32 characters.

```powershell
./backend/dev/rag-db/provision.ps1
```

The source-mode Java helper applies Flyway V1–V13 and provisions LOGIN/passwords only after migrations reset/audit roles. Its actual separate process is covered by `RagRuntimeRoleIntegrationTest`. Migrations remove the legacy worker password/login and memberships, forbid unsafe privileges and reject unexpected worker-owned objects. Unsafe object ownership requires explicit operator repair; no automatic business ownership transfer is attempted.

## Long-running application

Stop the migrator process and remove its credentials from the runtime environment. Supply `PPV_RAG_RUNTIME_URL`, `PPV_RAG_RUNTIME_PASSWORD`, an independent `PPV_RAG_SERVICE_KEY`, and the existing application security/bootstrap settings. Start Java with Spring profile `rag-runtime` (for example `--spring.profiles.active=rag-runtime` on the packaged application). That profile uses `rag_runtime` and disables Flyway. Hibernate validates the migrated schema. Keep the worker credential out of Java's datasource configuration; supply it only in Python's loopback DSN.

The runtime role has required business/RAG DML but no schema/function/database ownership, elevation, membership, schema or temporary-table DDL, or Flyway-history mutation. V13 revokes PUBLIC's default TEMP privilege for this database while preserving the database owner's rights; effective privileges are checked, not inferred from NOINHERIT. `rag_worker` has no direct table reads/DML/DDL and can only invoke audited scoped grant verification/receipt functions. Java runtime DML cannot forge the verified-receipt flag. Workers never change source policy or active generations. Under ADR-0020, ordinary restart preserves valid completed indexes and user consent; declared restore / uncertain history requires suspension/renewal.

This tooling is local development/verification, not production-deployment authorization. Windows host firewall policy and durable crash/power-loss operations require separate evidence; the offline check image proves a narrower Docker namespace guarantee.

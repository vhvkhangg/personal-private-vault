// ADR-0020, ADR-0022/0023, ADR-0025: Current architecture representation.
// Reflects immutable ORIGINAL_BYTES storage, Java-issued prepared inputs and final publication,
// job-scoped indexing worker (INACTIVE), and request-bound S19-1 query embedding worker (DEFAULT_OFF).
workspace "Personal Private Vault" "Architecture diagrams for the frozen v1 baseline" {

    model {
        owner = person "Owner" "The single permanent user of Personal Private Vault." {
            tags "Person"
        }

        vault = softwareSystem "Personal Private Vault" "Private single-user personal knowledge, media, finance, account and lifestyle vault." {
            tags "System"

            web = container "Web Application" "Browser UI for managing and viewing the vault. [PROPOSED / Future frontend phase]" "Next.js + TypeScript + shadcn/ui" {
                tags "Web"
            }

            backend = container "Backend API" "REST API, authentication, modular-monolith business logic, import orchestration, scheduled jobs, global search, and RAG source authority / finalization. [ACCEPTED]" "Java 25 LTS + Spring Boot + Spring Modulith + Hibernate" {
                tags "Backend"
            }

            database = container "PostgreSQL Database" "Stores structured application data, metadata, and RAG index generations / chunks. Binary media is not stored here. [ACCEPTED]" "PostgreSQL" {
                tags "Database"
            }

            snapshotStore = container "Immutable ORIGINAL_BYTES Store" "File-system storage for approved immutable snapshot revisions and ORIGINAL_BYTES artifacts under Java authority. [ACCEPTED / INACTIVE]" "Local Filesystem" {
                tags "Storage"
            }

            indexingWorker = container "Document Indexing Worker" "Job-scoped local worker process for Obsidian Markdown parsing, chunking, and CPU embeddings. Restricted staging only. [ACCEPTED P18 / INACTIVE]" "Python 3.14.4 / worker" {
                tags "Worker"
            }

            queryWorker = container "Query Embedding Worker" "Request-bound dynamic loopback process for query vector embedding. Guarded by active reader exclusion. [ACCEPTED S19-1 / DEFAULT_OFF]" "Python 3.14.4 / FastAPI loopback" {
                tags "Worker"
            }

            qaWorker = container "Grounded QA Generator" "Grounded question answering over vault notes. [SUSPENDED / NOT_ACCEPTED / ARCHIVAL_ONLY]" "Python 3.14.4 / llama.cpp (archival)" {
                tags "Worker" "Archival"
            }
        }

        objectStorage = softwareSystem "S3-Compatible Object Storage" "Stores image/media binaries. Local development uses only small test files; production provider is deferred. [ACCEPTED / OPTIONAL]" {
            tags "External"
        }

        feedSources = softwareSystem "Public Feed Sources" "GitHub Trending, Hacker News, Reddit, RSS feeds and technology websites. [ACCEPTED batch contracts]" {
            tags "External"
        }

        socialPlatforms = softwareSystem "External Account Platforms" "Facebook, Instagram, X, Threads, Douyin, YouTube and game/account platforms. The vault primarily stores metadata, relationships and source links." {
            tags "External"
        }

        googleDrive = softwareSystem "Google OAuth / OIDC" "Code, refresh, JWKS and revoke only (ADR-0026). S1 REVIEW_PENDING / DEFAULT_OFF; no file operations or live activation." {
            tags "External"
        }

        owner -> vault "Uses privately from browser-capable devices"
        vault -> feedSources "Retrieves configured public feed data"
        vault -> socialPlatforms "Stores account/post metadata and source links"
        vault -> objectStorage "Stores and retrieves media binaries"
        vault -> googleDrive "Authorizes account and manages connection lifecycle" "HTTPS"

        owner -> web "Uses"
        web -> backend "Calls" "HTTPS / REST / JSON"
        backend -> database "Reads and writes; finalizes generation batches (RagDocumentFinalizationGate)" "JPA/Hibernate / SQL (rag_runtime)"
        backend -> snapshotStore "Captures approved revisions, verifies whole-file SHA-256 integrity" "Java file I/O"
        backend -> objectStorage "Stores/retrieves objects" "S3-compatible API"
        backend -> feedSources "Fetches configured feeds" "HTTPS"
        backend -> googleDrive "Exchanges authorization code for tokens, revokes refresh token on disconnect (best-effort)" "HTTPS / REST / JSON"
        backend -> indexingWorker "Issues prepared inputs for batch jobs; one-use grant" "Local process dispatch / HTTP"
        indexingWorker -> database "Stages chunks and seals batch via restricted SQL functions" "rag_worker / scoped SECURITY DEFINER functions"
        backend -> queryWorker "Requests query embedding on dynamic loopback port; reader exclusion spans async output & positive sender/child stop outside SQL tx (ADR-0025)" "HTTP loopback / guarded reader lifetime"
        backend -> qaWorker "Archival invocation disabled; zero runtime model loaded" "None / suspended"
    }

    views {
        systemContext vault "SystemContext" {
            include *
            autoLayout lr
            title "Personal Private Vault — C4 System Context"
            description "Frozen v1 system context. RAG is intentionally out of scope."
        }

        container vault "Containers" {
            include owner
            include web
            include backend
            include database
            include snapshotStore
            include indexingWorker
            include queryWorker
            include qaWorker
            include objectStorage
            include feedSources
            include googleDrive
            autoLayout lr
            title "Personal Private Vault — C4 Container"
            description "Current containers including immutable snapshot store, indexing worker, query embedding worker, and suspended QA generator."
        }

        styles {
            element "Person" {
                shape Person
                background #08427B
                color #FFFFFF
            }
            element "System" {
                background #1168BD
                color #FFFFFF
            }
            element "Web" {
                shape WebBrowser
                background #438DD5
                color #FFFFFF
            }
            element "Backend" {
                shape Hexagon
                background #438DD5
                color #FFFFFF
            }
            element "Database" {
                shape Cylinder
                background #438DD5
                color #FFFFFF
            }
            element "External" {
                background #999999
                color #FFFFFF
            }
            element "Archival" {
                background #EEEEEE
                color #888888
            }
        }
    }
}

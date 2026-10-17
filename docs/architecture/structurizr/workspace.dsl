// ADR-0020 (2026-10-15): approved target is one RAG-owned job-scoped Windows worker,
// durable source reservations and E1. Topology below is unchanged candidate context,
// not proof of lifecycle implementation; see rag-architecture.md and P17-3.
workspace "Personal Private Vault" "Architecture diagrams for the frozen v1 baseline" {

    model {
        owner = person "Owner" "The single permanent user of Personal Private Vault." {
            tags "Person"
        }

        vault = softwareSystem "Personal Private Vault" "Private single-user personal knowledge, media, finance, account and lifestyle vault." {
            tags "System"

            web = container "Web Application" "Browser UI for managing and viewing the vault. Frontend implementation starts after backend completion." "Next.js + TypeScript + shadcn/ui" {
                tags "Web"
            }

            backend = container "Backend API" "REST API, authentication, modular-monolith business logic, import orchestration, scheduled jobs and global search orchestration." "Java 25 LTS + Spring Boot + Spring Modulith + Hibernate" {
                tags "Backend"
            }

            database = container "PostgreSQL Database" "Stores all structured application data and metadata. Binary media is not stored here." "PostgreSQL" {
                tags "Database"
            }

            ragEngine = container "Local RAG Engine" "Isolated local non-root worker for bounded candidate staging." "Python 3.14 / uv + FastAPI / worker" {
                tags "Worker"
            }
        }

        objectStorage = softwareSystem "S3-Compatible Object Storage" "Stores image/media binaries. Local development uses only small test files; production provider is deferred." {
            tags "External"
        }

        feedSources = softwareSystem "Public Feed Sources" "GitHub Trending, Hacker News, Reddit, RSS feeds and technology websites." {
            tags "External"
        }

        socialPlatforms = softwareSystem "External Account Platforms" "Facebook, Instagram, X, Threads, Douyin, YouTube and game/account platforms. The vault primarily stores metadata, relationships and source links." {
            tags "External"
        }

        owner -> vault "Uses privately from browser-capable devices"
        vault -> feedSources "Retrieves configured public feed data"
        vault -> socialPlatforms "Stores account/post metadata and source links"
        vault -> objectStorage "Stores and retrieves media binaries"

        owner -> web "Uses"
        web -> backend "Calls" "HTTPS / REST / JSON"
        backend -> database "Reads and writes" "JPA/Hibernate / SQL"
        backend -> objectStorage "Stores/retrieves objects" "S3-compatible API"
        backend -> feedSources "Fetches configured feeds" "HTTPS"
        backend -> ragEngine "Dispatches issued synthetic tasks; checks committed receipts" "HTTP / exact loopback :8001"
        ragEngine -> database "Restricted grant verification and digest-only staging; no business reads" "rag_worker / scoped SECURITY DEFINER functions"
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
            include objectStorage
            include feedSources
            include ragEngine
            autoLayout lr
            title "Personal Private Vault — C4 Container"
            description "Planned containers including local RAG engine worker."
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
        }
    }
}

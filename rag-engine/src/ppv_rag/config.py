"""Externally supplied local runtime credentials; no development secret fallback."""

import os
from dataclasses import dataclass, field
from uuid import UUID


@dataclass(frozen=True)
class Settings:
    service_key: str = field(
        default_factory=lambda: os.getenv("PPV_RAG_SERVICE_KEY", ""), repr=False
    )
    worker_dsn: str = field(default_factory=lambda: os.getenv("PPV_RAG_WORKER_DSN", ""), repr=False)
    host: str = field(default_factory=lambda: os.getenv("PPV_RAG_HOST", "127.0.0.1"))
    port: int = field(default_factory=lambda: int(os.getenv("PPV_RAG_PORT", "8001")))
    test_scoped_execution: bool = field(
        default_factory=lambda: os.getenv("PPV_RAG_TEST_SCOPED_EXECUTION", "") == "true"
    )
    job_id: str = field(default_factory=lambda: os.getenv("PPV_RAG_JOB_ID", ""))
    attempt_id: str = field(default_factory=lambda: os.getenv("PPV_RAG_ATTEMPT_ID", ""))
    endpoint_nonce: str = field(
        default_factory=lambda: os.getenv("PPV_RAG_ENDPOINT_NONCE", ""), repr=False
    )

    def bound_execution_enabled(self) -> bool:
        """Only an isolated, explicitly bound job may leave default containment."""
        try:
            return (
                self.test_scoped_execution
                and self.host == "127.0.0.1"
                and len(self.service_key) >= 32
                and len(self.endpoint_nonce) >= 32
                and str(UUID(self.job_id)) == self.job_id
                and str(UUID(self.attempt_id)) == self.attempt_id
            )
        except ValueError:
            return False


settings = Settings()

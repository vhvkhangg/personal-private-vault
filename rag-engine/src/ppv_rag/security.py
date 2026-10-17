"""Independent service credential, compared without a timing-dependent prefix."""

import hmac

from fastapi import Header, HTTPException

from ppv_rag.config import settings


def verify_service_key(x_rag_service_key: str | None = Header(default=None)) -> None:
    if (
        len(settings.service_key) < 32
        or not x_rag_service_key
        or not hmac.compare_digest(x_rag_service_key, settings.service_key)
    ):
        raise HTTPException(status_code=401, detail="Service authentication denied")

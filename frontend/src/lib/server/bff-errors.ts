import "server-only";
import { NextResponse } from "next/server";

export class BffException extends Error {
  constructor(
    public readonly code: string,
    public readonly httpStatus: number,
    message?: string
  ) {
    super(message || code);
    this.name = "BffException";
  }
}

// Only selected public Spring contract codes cross the BFF; never relay provider messages.
const UPSTREAM_CODES: Record<number, readonly string[]> = {
  400: [
    "GOOGLE_SIGNIN_INVALID_REQUEST",
    "DRIVE_INVALID_SLOT",
    "DRIVE_INVALID_LIMIT",
    "DRIVE_INVALID_FILE_ID",
    "DRIVE_INVALID_FILE_INPUT",
    "DRIVE_CURSOR_INVALID",
    "DRIVE_RANGE_NOT_SUPPORTED",
    "UPLOAD_KEY_REQUIRED",
    "UPLOAD_KEY_INVALID",
    "FOLDER_KEY_REQUIRED",
    "FOLDER_KEY_INVALID",
  ],
  401: [
    "AUTH_INVALID_CREDENTIALS",
    "AUTH_INVALID_REFRESH_TOKEN",
    "AUTHENTICATION_REQUIRED",
    "GOOGLE_HANDOFF_INVALID",
    "DRIVE_INVALID_OWNER",
  ],
  403: ["FORBIDDEN", "ACCESS_DENIED", "DRIVE_OWNER_REQUIRED"],
  404: ["DRIVE_FILE_UNAVAILABLE", "THUMBNAIL_UNAVAILABLE"],
  409: [
    "GOOGLE_OWNER_NOT_READY",
    "GOOGLE_IDENTITY_CONFLICT",
    "DRIVE_CONNECTION_REQUIRED",
    "DRIVE_RECONNECT_REQUIRED",
    "DRIVE_REVOCATION_PENDING",
    "DRIVE_REVOKE_UNCERTAIN",
    "DRIVE_STALE_ATTEMPT",
    "DRIVE_STALE_AUTHORITY",
    "DRIVE_ROOT_NOT_READY",
    "DRIVE_ROOT_PENDING",
    "DRIVE_ROOT_OUTCOME_UNKNOWN",
    "DRIVE_ROOT_UNAVAILABLE",
    "DRIVE_DUPLICATE_SUBJECT",
    "DRIVE_SLOT_OCCUPIED_BY_DIFFERENT_SUBJECT",
    "UPLOAD_KEY_EXPIRED",
    "UPLOAD_KEY_CONFLICT",
    "UPLOAD_IN_PROGRESS",
    "UPLOAD_OUTCOME_UNKNOWN",
    "FOLDER_KEY_EXPIRED",
    "FOLDER_KEY_CONFLICT",
    "FOLDER_IN_PROGRESS",
    "FOLDER_OUTCOME_UNKNOWN",
    "FOLDER_FAILED",
    "UPLOAD_FAILED",
  ],
  413: ["DRIVE_PAYLOAD_TOO_LARGE", "PAYLOAD_TOO_LARGE"],
  415: ["DRIVE_FILE_NOT_BINARY"],
  429: ["GOOGLE_SIGNIN_RATE_LIMITED"],
  500: ["DRIVE_PERSISTENCE_FAILURE", "DRIVE_FILE_PERSISTENCE_FAILURE"],
  502: [
    "GOOGLE_SIGNIN_FAILURE",
    "DRIVE_PROVIDER_FAILURE",
    "DRIVE_PROVIDER_COMMUNICATION_FAILURE",
    "DRIVE_SEARCH_INCOMPLETE",
  ],
  503: [
    "GOOGLE_SIGNIN_UNAVAILABLE",
    "DRIVE_CAPACITY_EXHAUSTED",
    "DRIVE_FILE_CAPACITY_EXHAUSTED",
    "DRIVE_PROVIDER_RATE_LIMITED",
    "DRIVE_PROVIDER_QUOTA",
  ],
  504: ["DRIVE_PROVIDER_TIMEOUT"],
};

export function trustedUpstreamError(payload: unknown, status: number): BffException {
  const error = payload && typeof payload === "object" && "error" in payload ? payload.error : null;
  const code = error && typeof error === "object" && "code" in error ? error.code : null;
  if (typeof code === "string" && UPSTREAM_CODES[status]?.includes(code)) {
    return new BffException(code, status);
  }
  return new BffException("UPSTREAM_INVALID_RESPONSE", 502, "Invalid upstream response");
}

export function errorResponse(code: string, httpStatus: number, message?: string): NextResponse {
  return NextResponse.json(
    {
      error: {
        code,
        message: message || code,
      },
    },
    {
      status: httpStatus,
      headers: {
        "Cache-Control": "no-store",
        "Content-Type": "application/json",
      },
    }
  );
}

export function handleServerException(error: unknown): NextResponse {
  if (error instanceof BffException) {
    return errorResponse(error.code, error.httpStatus, error.message);
  }
  if (error instanceof Error && error.message === "CAPACITY_EXHAUSTED") {
    return errorResponse("BFF_CAPACITY_EXHAUSTED", 429, "Too many active sessions");
  }
  return errorResponse("INTERNAL_SERVER_ERROR", 500, "An internal server error occurred");
}

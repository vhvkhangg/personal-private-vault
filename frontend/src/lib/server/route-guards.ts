import "server-only";
import { type NextRequest } from "next/server";
import { serverConfig } from "./config";
import { sessionStore, type SessionRecord, type SessionPurpose } from "./session-store";
import { validateCsrf } from "./csrf";
import { BffException } from "./bff-errors";

export function extractSidFromCookieHeader(
  cookieHeader: string | null | undefined
): string | undefined {
  if (!cookieHeader) {
    return undefined;
  }
  const sidPattern = new RegExp(`(?:^|;\\s*)${serverConfig.sessionCookieName}=`, "g");
  const matches = cookieHeader.match(sidPattern);
  if (matches && matches.length > 1) {
    throw new BffException("DUPLICATE_COOKIE", 400, "Duplicate session cookies rejected");
  }

  const match = cookieHeader.match(
    new RegExp(`(?:^|;\\s*)${serverConfig.sessionCookieName}=([^;]*)`)
  );
  if (!match || !match[1]) {
    return undefined;
  }
  const rawValue = match[1].trim();
  return rawValue ? decodeURIComponent(rawValue) : undefined;
}

export function extractSid(req: NextRequest): string | undefined {
  return extractSidFromCookieHeader(req.headers.get("cookie"));
}

export function validateAuthority(req: NextRequest, isMutation = false): void {
  const host = req.headers.get("host");

  // Host must be strictly localhost:3000
  if (!host || host !== "localhost:3000") {
    throw new BffException("INVALID_HOST_AUTHORITY", 400, "Host must be strictly localhost:3000");
  }

  // Next.js internal forwarded headers check: if present, must strictly match localhost:3000
  const forwardedHost = req.headers.get("x-forwarded-host");
  if (forwardedHost && forwardedHost !== "localhost:3000") {
    throw new BffException("INVALID_HOST_AUTHORITY", 400, "Forwarded host mismatch");
  }

  const forwardedProto = req.headers.get("x-forwarded-proto");
  if (forwardedProto && forwardedProto !== "http") {
    throw new BffException("INVALID_HOST_AUTHORITY", 400, "Forwarded proto mismatch");
  }

  const forwardedPort = req.headers.get("x-forwarded-port");
  if (forwardedPort && forwardedPort !== "3000") {
    throw new BffException("INVALID_HOST_AUTHORITY", 400, "Forwarded port mismatch");
  }

  const forwardedServer = req.headers.get("x-forwarded-server");
  if (forwardedServer && forwardedServer !== "localhost") {
    throw new BffException("INVALID_HOST_AUTHORITY", 400, "Forwarded server mismatch");
  }

  if (isMutation) {
    const origin = req.headers.get("origin");
    if (!origin || origin !== "http://localhost:3000") {
      throw new BffException(
        "INVALID_ORIGIN",
        403,
        "Origin must be strictly http://localhost:3000"
      );
    }

    const secFetchSite = req.headers.get("sec-fetch-site");
    // Require same-origin when sec-fetch-site is provided by the browser
    if (secFetchSite && secFetchSite !== "same-origin") {
      throw new BffException("CROSS_SITE_MUTATION_DENIED", 403, "Cross-site mutation rejected");
    }
  }
}

export function assertActiveSession(
  record: SessionRecord,
  capturedGeneration: number,
  expectedPurpose: SessionPurpose
): void {
  const now = Date.now();
  const active = sessionStore.getSessionByIdDigest(record.idDigest);
  if (
    !active ||
    active !== record ||
    record.isRevoking ||
    record.generation !== capturedGeneration ||
    record.purpose !== expectedPurpose ||
    now >= record.absoluteDeadline ||
    now >= record.idleDeadline
  ) {
    throw new BffException("SESSION_INVALIDATED", 401, "Session invalidated or expired");
  }
}

export async function readBoundedText(req: NextRequest, maxBytes: number): Promise<string> {
  const contentLength = req.headers.get("content-length");
  if (contentLength !== null) {
    const cl = Number(contentLength);
    if (!/^(0|[1-9][0-9]*)$/.test(contentLength) || !Number.isSafeInteger(cl)) {
      void req.body?.cancel().catch(() => {});
      throw new BffException("BAD_REQUEST", 400, "Invalid content length");
    }
    if (cl > maxBytes) {
      void req.body?.cancel().catch(() => {});
      throw new BffException("PAYLOAD_TOO_LARGE", 413, `Payload exceeds ${maxBytes} bytes`);
    }
  }

  if (!req.body) {
    return "";
  }

  const reader = req.body.getReader();
  const chunks: Uint8Array[] = [];
  let totalBytes = 0;
  let timer: ReturnType<typeof setTimeout> | undefined;
  let cancelRequest = () => {};
  const expired = new Promise<never>((_, reject) => {
    cancelRequest = () => {
      reject(new BffException("REQUEST_CANCELLED", 400));
      void reader.cancel().catch(() => {});
    };
    timer = setTimeout(() => {
      reject(new BffException("REQUEST_TIMEOUT", 408));
      void reader.cancel().catch(() => {});
    }, 10_000);
  });
  req.signal.addEventListener("abort", cancelRequest, { once: true });
  if (req.signal.aborted) cancelRequest();

  try {
    while (true) {
      const { done, value } = await Promise.race([reader.read(), expired]);
      if (done) break;
      if (value) {
        totalBytes += value.byteLength;
        if (totalBytes > maxBytes) {
          void reader.cancel("Payload exceeds limit").catch(() => {});
          throw new BffException("PAYLOAD_TOO_LARGE", 413, `Payload exceeds ${maxBytes} bytes`);
        }
        chunks.push(value);
      }
    }
  } catch (err) {
    void reader.cancel().catch(() => {});
    if (err instanceof BffException) throw err;
    throw new BffException("BAD_REQUEST", 400, "Error reading request body");
  } finally {
    clearTimeout(timer);
    req.signal.removeEventListener("abort", cancelRequest);
    reader.releaseLock();
  }

  const concatenated = new Uint8Array(totalBytes);
  let offset = 0;
  for (const chunk of chunks) {
    concatenated.set(chunk, offset);
    offset += chunk.byteLength;
  }
  try {
    return new TextDecoder("utf-8", { fatal: true }).decode(concatenated);
  } catch {
    throw new BffException("BAD_REQUEST", 400, "Invalid text encoding");
  }
}

export async function readBoundedJson<T>(req: NextRequest, maxBytes: number): Promise<T> {
  const text = await readBoundedText(req, maxBytes);
  if (!text.trim()) {
    throw new BffException("BAD_REQUEST", 400, "Empty JSON payload");
  }
  try {
    const value: unknown = JSON.parse(text);
    if (!value || typeof value !== "object" || Array.isArray(value)) throw new Error();
    return value as T;
  } catch {
    throw new BffException("BAD_REQUEST", 400, "Invalid JSON payload");
  }
}

export function requireSession(req: NextRequest): { sid: string; record: SessionRecord } {
  const sid = extractSid(req);
  const record = sessionStore.getSession(sid);
  if (!sid || !record) {
    throw new BffException("UNAUTHORIZED", 401, "Valid session required");
  }
  return { sid, record };
}

export function requireAuthenticatedSession(req: NextRequest): {
  sid: string;
  record: SessionRecord;
} {
  const { sid, record } = requireSession(req);
  if (record.purpose !== "AUTHENTICATED") {
    throw new BffException("FORBIDDEN", 403, "Authenticated session required");
  }
  return { sid, record };
}

export function requireSetupSession(req: NextRequest): { sid: string; record: SessionRecord } {
  const { sid, record } = requireSession(req);
  if (record.purpose !== "SETUP_ONLY") {
    throw new BffException("FORBIDDEN", 403, "Setup-only session required");
  }
  return { sid, record };
}

export function validateMutationCsrf(
  req: NextRequest,
  record: SessionRecord,
  submittedCsrf?: string
): void {
  const token = submittedCsrf || req.headers.get("x-ppv-csrf");
  if (!validateCsrf(record, token)) {
    throw new BffException("CSRF_TOKEN_INVALID", 403, "Invalid or missing CSRF token");
  }
}

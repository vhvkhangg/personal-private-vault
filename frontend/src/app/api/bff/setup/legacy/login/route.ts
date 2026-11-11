import "server-only";
import { type NextRequest, NextResponse } from "next/server";
import { sessionStore } from "@/lib/server/session-store";
import {
  extractSid,
  validateAuthority,
  validateMutationCsrf,
  readBoundedJson,
} from "@/lib/server/route-guards";
import { springLegacyLogin, springRevokeToken } from "@/lib/server/auth-client";
import { setSessionCookie } from "@/lib/server/cookie-relay";
import { handleServerException, BffException } from "@/lib/server/bff-errors";
import { serverConfig } from "@/lib/server/config";

export const dynamic = "force-dynamic";

export async function POST(req: NextRequest) {
  try {
    validateAuthority(req, true);

    if (!serverConfig.ownerSetupEnabled) {
      throw new BffException("SETUP_DISABLED", 403, "Owner setup is disabled");
    }

    const sid = extractSid(req);
    const record = sessionStore.getSession(sid);

    if (!sid || !record || record.purpose !== "ANONYMOUS") {
      throw new BffException("UNAUTHORIZED", 401, "Valid anonymous session required");
    }

    validateMutationCsrf(req, record);

    // Read bounded JSON (4 KiB)
    const body = await readBoundedJson<{ identifier?: string; password?: string }>(
      req,
      serverConfig.maxAuthJsonBytes
    );

    const { identifier, password } = body;

    if (
      typeof identifier !== "string" ||
      typeof password !== "string" ||
      !identifier.trim() ||
      !password
    ) {
      throw new BffException("AUTH_INVALID_CREDENTIALS", 400, "Missing credentials");
    }

    const capturedGen = record.generation;

    // Call Spring legacy login
    const tokens = await springLegacyLogin(identifier, password);

    // Check if session was invalidated or generation changed while awaiting login
    const currentSession = sessionStore.getSession(sid);
    const now = Date.now();
    if (
      !currentSession ||
      currentSession.purpose !== "ANONYMOUS" ||
      currentSession.isRevoking ||
      currentSession.generation !== capturedGen ||
      now >= currentSession.absoluteDeadline ||
      now >= currentSession.idleDeadline
    ) {
      if (tokens.refreshToken) {
        await springRevokeToken(tokens.refreshToken).catch(() => {});
      }
      throw new BffException("SESSION_INVALIDATED", 401, "Session invalidated during login");
    }

    // Rotate to SETUP_ONLY session
    let rotated: { sid: string; record: typeof record; csrfToken: string };
    try {
      rotated = sessionStore.rotateSession(sid, "SETUP_ONLY", tokens);
    } catch (err) {
      if (tokens.refreshToken) {
        await springRevokeToken(tokens.refreshToken).catch(() => {});
      }
      throw err;
    }

    const res = NextResponse.json(
      { data: { success: true } },
      {
        headers: {
          "Cache-Control": "private, no-store",
        },
      }
    );

    setSessionCookie(res, rotated.sid, Math.floor(serverConfig.anonymousAbsoluteMs / 1000));
    return res;
  } catch (err) {
    return handleServerException(err);
  }
}

import "server-only";
import { type NextRequest, NextResponse } from "next/server";
import crypto from "node:crypto";
import {
  requireSetupSession,
  validateAuthority,
  validateMutationCsrf,
  readBoundedJson,
  assertActiveSession,
} from "@/lib/server/route-guards";
import { springStartGoogleLink } from "@/lib/server/auth-client";
import { relaySpringCookie } from "@/lib/server/cookie-relay";
import { handleServerException, BffException } from "@/lib/server/bff-errors";
import { serverConfig } from "@/lib/server/config";

export const dynamic = "force-dynamic";

export async function POST(req: NextRequest) {
  try {
    validateAuthority(req, true);

    if (!serverConfig.ownerSetupEnabled) {
      throw new BffException("SETUP_DISABLED", 403, "Owner setup is disabled");
    }

    const { record } = requireSetupSession(req);
    validateMutationCsrf(req, record);

    if (!record.accessToken) {
      throw new BffException("UNAUTHORIZED", 401, "Setup session missing access token");
    }

    // Read bounded JSON (4 KiB)
    const body = await readBoundedJson<{ password?: string }>(req, serverConfig.maxAuthJsonBytes);

    const { password } = body;

    if (typeof password !== "string" || !password) {
      throw new BffException("AUTH_INVALID_CREDENTIALS", 400, "Fresh password required");
    }

    // 1. Generate 32 random bytes -> 43 base64url characters
    const verifierBytes = crypto.randomBytes(32);
    const handoffVerifier = verifierBytes.toString("base64url");

    // 2. Compute SHA-256 challenge as 64 lowercase hex
    const handoffChallenge = crypto
      .createHash("sha256")
      .update(Buffer.from(handoffVerifier, "ascii"))
      .digest("hex")
      .toLowerCase();

    const capturedGeneration = record.generation;
    const startSeq = (record.startSeq ?? 0) + 1;
    record.startSeq = startSeq;

    // 3. Dispatch to Spring with fresh password verification
    const startResult = await springStartGoogleLink(record.accessToken, handoffChallenge, password);

    // Recheck generation/deadline fence: ensure session was not invalidated, expired, or changed
    assertActiveSession(record, capturedGeneration, "SETUP_ONLY");
    if (record.startSeq !== startSeq) {
      throw new BffException(
        "STALE_ATTEMPT",
        409,
        "A newer Google link attempt superseded this request"
      );
    }

    // 4. Save verifier and attemptId in setup session
    record.handoffVerifier = handoffVerifier;
    record.attemptId = startResult.attemptId;

    const res = NextResponse.json(
      {
        data: {
          authorizationUrl: startResult.authorizationUrl,
          attemptId: startResult.attemptId,
          expiresIn: startResult.expiresIn,
        },
      },
      {
        headers: {
          "Cache-Control": "private, no-store",
        },
      }
    );

    // 5. Relay exact binding cookie
    const relayed = relaySpringCookie(
      res,
      startResult.setCookieHeaders,
      serverConfig.googleSignInBindCookie,
      "/api/v1/auth/google/callback"
    );
    if (!relayed) {
      throw new BffException(
        "GOOGLE_SIGNIN_FAILURE",
        502,
        "Failed to relay binding cookie from upstream"
      );
    }

    return res;
  } catch (err) {
    return handleServerException(err);
  }
}

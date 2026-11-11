import "server-only";
import { type NextRequest, NextResponse } from "next/server";
import crypto from "node:crypto";
import { sessionStore } from "@/lib/server/session-store";
import {
  extractSid,
  validateAuthority,
  validateMutationCsrf,
  assertActiveSession,
} from "@/lib/server/route-guards";
import { springStartGoogleSignIn } from "@/lib/server/auth-client";
import { relaySpringCookie } from "@/lib/server/cookie-relay";
import { handleServerException, BffException } from "@/lib/server/bff-errors";
import { serverConfig } from "@/lib/server/config";

export const dynamic = "force-dynamic";

export async function POST(req: NextRequest) {
  try {
    validateAuthority(req, true);

    const sid = extractSid(req);
    const record = sessionStore.getSession(sid);

    if (!record || !sid) {
      throw new BffException("UNAUTHENTICATED", 401, "No active anonymous session");
    }

    if (record.purpose !== "ANONYMOUS") {
      throw new BffException(
        "FORBIDDEN_PURPOSE",
        403,
        "Google Sign-In can only be started from an anonymous session"
      );
    }

    validateMutationCsrf(req, record);

    const capturedGen = record.generation;
    const startSeq = (record.startSeq ?? 0) + 1;
    record.startSeq = startSeq;

    // 1. Generate 32 random bytes -> 43 base64url characters
    const verifierBytes = crypto.randomBytes(32);
    const handoffVerifier = verifierBytes.toString("base64url");

    // 2. Compute SHA-256 challenge as 64 lowercase hex
    const handoffChallenge = crypto
      .createHash("sha256")
      .update(Buffer.from(handoffVerifier, "ascii"))
      .digest("hex")
      .toLowerCase();

    // 3. Dispatch to Spring
    const startResult = await springStartGoogleSignIn(handoffChallenge);

    // Verify session wasn't invalidated, expired, or generation changed while awaiting Spring
    assertActiveSession(record, capturedGen, "ANONYMOUS");

    if (record.startSeq !== startSeq) {
      throw new BffException(
        "STALE_ATTEMPT",
        409,
        "A newer Google Sign-In start attempt superseded this request"
      );
    }

    // 4. Save verifier and attemptId in session
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

import "server-only";
import { type NextRequest, NextResponse } from "next/server";
import { sessionStore } from "@/lib/server/session-store";
import { extractSid, validateAuthority, validateMutationCsrf } from "@/lib/server/route-guards";
import { clearSessionCookie } from "@/lib/server/cookie-relay";
import { springRevokeToken } from "@/lib/server/auth-client";
import { handleServerException, BffException } from "@/lib/server/bff-errors";

export const dynamic = "force-dynamic";

export async function POST(req: NextRequest) {
  try {
    validateAuthority(req, true);

    const sid = extractSid(req);
    const record = sessionStore.getSession(sid);

    if (!record || !sid || record.purpose === "ANONYMOUS") {
      throw new BffException("UNAUTHENTICATED", 401, "No authenticated session");
    }

    validateMutationCsrf(req, record);

    // Synchronously invalidate and advance generation
    record.isRevoking = true;
    sessionStore.invalidateByIdDigest(record.idDigest);
    sessionStore.invalidate(sid);

    // Best-effort backend token revocation
    if (record.refreshToken) {
      void springRevokeToken(record.refreshToken);
    }

    const res = NextResponse.json(
      { data: { success: true } },
      {
        headers: {
          "Cache-Control": "private, no-store",
        },
      }
    );

    clearSessionCookie(res);
    return res;
  } catch (err) {
    return handleServerException(err);
  }
}

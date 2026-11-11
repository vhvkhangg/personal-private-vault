import "server-only";
import { type NextRequest, NextResponse } from "next/server";
import { sessionStore } from "@/lib/server/session-store";
import { extractSid, validateAuthority, validateMutationCsrf } from "@/lib/server/route-guards";
import { clearSessionCookie } from "@/lib/server/cookie-relay";
import { handleServerException, BffException } from "@/lib/server/bff-errors";

export const dynamic = "force-dynamic";

export async function POST(req: NextRequest) {
  try {
    validateAuthority(req, true);

    const sid = extractSid(req);
    const record = sessionStore.getSession(sid);

    if (!record || !sid) {
      throw new BffException("UNAUTHENTICATED", 401, "No active session");
    }

    if (record.purpose !== "ANONYMOUS") {
      throw new BffException(
        "FORBIDDEN_PURPOSE",
        403,
        "Cancel is only allowed for anonymous sessions"
      );
    }

    validateMutationCsrf(req, record);

    sessionStore.invalidateByIdDigest(record.idDigest);
    sessionStore.invalidate(sid);

    const res = NextResponse.json(
      { data: { cancelled: true } },
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

import "server-only";
import { type NextRequest, NextResponse } from "next/server";
import { serverConfig } from "@/lib/server/config";
import { sessionStore } from "@/lib/server/session-store";
import { extractSid, validateAuthority } from "@/lib/server/route-guards";
import { setSessionCookie } from "@/lib/server/cookie-relay";
import { handleServerException } from "@/lib/server/bff-errors";
import type { SessionStatusDto } from "@/lib/contracts";
import { getActiveTransferCount } from "@/lib/server/streaming";

export const dynamic = "force-dynamic";

export async function GET(req: NextRequest) {
  try {
    validateAuthority(req, false);
    if (serverConfig.isTestFixture && req.headers.get("x-ppv-test-transfer-stats") === "true") {
      return NextResponse.json(
        { activeTransfers: getActiveTransferCount(), rss: process.memoryUsage().rss },
        { headers: { "Cache-Control": "private, no-store" } }
      );
    }

    if (serverConfig.isTestFixture && req.headers.get("x-ppv-test-reset-sessions") === "true") {
      sessionStore.resetAll();
      return NextResponse.json(
        { data: { reset: true } },
        { headers: { "Cache-Control": "private, no-store" } }
      );
    }

    const sid = extractSid(req);
    let record = sessionStore.getSession(sid);
    let effectiveSid = sid;
    let csrfTokenToReturn = "";
    let isNewSession = false;

    if (!record || !effectiveSid) {
      const created = sessionStore.createSession("ANONYMOUS");
      effectiveSid = created.sid;
      record = created.record;
      csrfTokenToReturn = created.csrfToken;
      isNewSession = true;
    } else {
      // Retain the existing CSRF token for the current session generation
      csrfTokenToReturn = record.csrfToken;
      if (serverConfig.isTestFixture && req.headers.get("x-ppv-test-expire-access") === "true") {
        record.accessExpiresAt = Date.now() - 1000;
      }
      if (serverConfig.isTestFixture) {
        const expiry = req.headers.get("x-ppv-test-expire-session");
        if (expiry === "idle") record.idleDeadline = Date.now() - 1;
        if (expiry === "absolute") record.absoluteDeadline = Date.now() - 1;
      }
    }

    const now = Date.now();
    const idleExpiresInSeconds = Math.max(0, Math.floor((record.idleDeadline - now) / 1000));
    const absoluteExpiresInSeconds = Math.max(
      0,
      Math.floor((record.absoluteDeadline - now) / 1000)
    );

    const dto: SessionStatusDto = {
      authenticated: record.purpose === "AUTHENTICATED",
      purpose: record.purpose,
      csrfToken: csrfTokenToReturn,
      idleExpiresInSeconds,
      absoluteExpiresInSeconds,
    };

    const res = NextResponse.json(dto, {
      headers: {
        "Cache-Control": "private, no-store",
      },
    });

    if (isNewSession && effectiveSid) {
      setSessionCookie(res, effectiveSid, Math.floor(serverConfig.anonymousAbsoluteMs / 1000));
    }

    return res;
  } catch (err) {
    return handleServerException(err);
  }
}

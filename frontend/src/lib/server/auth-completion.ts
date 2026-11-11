import "server-only";
import { type NextRequest, NextResponse } from "next/server";
import { serverConfig } from "./config";
import { sessionStore, type SessionRecord } from "./session-store";
import { springRedeemHandoff, springRevokeToken } from "./auth-client";
import { setSessionCookie } from "./cookie-relay";
import { validateAuthority } from "./route-guards";

export async function handleGoogleAuthComplete(req: NextRequest): Promise<NextResponse> {
  const fixedOrigin = "http://localhost:3000";
  const loginRedirectUrl = new URL("/login", fixedOrigin);
  const successRedirectUrl = new URL("/personal/storage/google-drive", fixedOrigin);

  // Clear handoff cookie on all completion outcomes
  const clearHandoffCookie = (res: NextResponse) => {
    res.cookies.set({
      name: serverConfig.googleHandoffCookie,
      value: "",
      path: "/api/bff/auth/google/complete",
      httpOnly: true,
      sameSite: "lax",
      secure: false,
      maxAge: 0,
    });
  };

  try {
    validateAuthority(req, false);
  } catch {
    loginRedirectUrl.searchParams.set("error", "invalid_authority");
    const res = NextResponse.redirect(loginRedirectUrl, 303);
    clearHandoffCookie(res);
    return res;
  }

  const cookieHeader = req.headers.get("cookie") || "";

  // Check duplicate handoff tickets or duplicate SIDs
  const ticketPattern = new RegExp(`(?:^|;\\s*)${serverConfig.googleHandoffCookie}=`, "g");
  const ticketMatches = cookieHeader.match(ticketPattern);
  if (ticketMatches && ticketMatches.length > 1) {
    loginRedirectUrl.searchParams.set("error", "duplicate_ticket");
    const res = NextResponse.redirect(loginRedirectUrl, 303);
    clearHandoffCookie(res);
    return res;
  }

  const sidPattern = new RegExp(`(?:^|;\\s*)${serverConfig.sessionCookieName}=`, "g");
  const sidMatches = cookieHeader.match(sidPattern);
  if (sidMatches && sidMatches.length > 1) {
    loginRedirectUrl.searchParams.set("error", "duplicate_cookie");
    const res = NextResponse.redirect(loginRedirectUrl, 303);
    clearHandoffCookie(res);
    return res;
  }

  const sidMatch = cookieHeader.match(
    new RegExp(`(?:^|;\\s*)${serverConfig.sessionCookieName}=([^;]*)`)
  );
  const sid = sidMatch && sidMatch[1] ? decodeURIComponent(sidMatch[1].trim()) : undefined;

  const ticketMatch = cookieHeader.match(
    new RegExp(`(?:^|;\\s*)${serverConfig.googleHandoffCookie}=([^;]*)`)
  );
  const ticket =
    ticketMatch && ticketMatch[1] ? decodeURIComponent(ticketMatch[1].trim()) : undefined;

  const record = sessionStore.getSession(sid);

  if (!sid || !record || !ticket || !record.handoffVerifier) {
    loginRedirectUrl.searchParams.set("error", "handoff_invalid");
    const res = NextResponse.redirect(loginRedirectUrl, 303);
    clearHandoffCookie(res);
    return res;
  }

  const verifier = record.handoffVerifier;
  const capturedGeneration = record.generation;
  delete record.handoffVerifier;
  delete record.attemptId;

  let tokens: {
    accessToken: string;
    refreshToken: string;
    tokenType: string;
    expiresIn: number;
  } | null = null;

  try {
    tokens = await springRedeemHandoff(ticket, verifier);

    // Re-check generation fence and session validity
    const current = sessionStore.getSession(sid);
    const now = Date.now();
    if (
      !current ||
      current.isRevoking ||
      current.generation !== capturedGeneration ||
      now >= current.absoluteDeadline ||
      now >= current.idleDeadline
    ) {
      if (tokens.refreshToken) {
        await springRevokeToken(tokens.refreshToken).catch(() => {});
      }
      loginRedirectUrl.searchParams.set("error", "session_expired");
      const res = NextResponse.redirect(loginRedirectUrl, 303);
      clearHandoffCookie(res);
      return res;
    }

    // Rotate to AUTHENTICATED session
    let rotated: { sid: string; record: SessionRecord; csrfToken: string };
    try {
      rotated = sessionStore.rotateSession(sid, "AUTHENTICATED", tokens);
    } catch (rotateErr) {
      if (tokens.refreshToken) {
        await springRevokeToken(tokens.refreshToken).catch(() => {});
      }
      throw rotateErr;
    }

    const res = NextResponse.redirect(successRedirectUrl, 303);
    clearHandoffCookie(res);
    setSessionCookie(res, rotated.sid, Math.floor(serverConfig.authenticatedAbsoluteMs / 1000));
    return res;
  } catch (err: unknown) {
    if (tokens?.refreshToken) {
      await springRevokeToken(tokens.refreshToken).catch(() => {});
    }
    const code = (err as { code?: string })?.code || "handoff_failed";
    loginRedirectUrl.searchParams.set("error", code);
    const res = NextResponse.redirect(loginRedirectUrl, 303);
    clearHandoffCookie(res);
    return res;
  }
}

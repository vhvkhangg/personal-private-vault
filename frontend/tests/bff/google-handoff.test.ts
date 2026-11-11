import { describe, it, expect, beforeEach, vi } from "vitest";
import { NextRequest } from "next/server";
import { sessionStore } from "@/lib/server/session-store";
import { serverConfig } from "@/lib/server/config";
import { POST as startGoogleAuth } from "@/app/api/bff/auth/google/start/route";
import { GET as completeGoogleAuth } from "@/app/api/bff/auth/google/complete/route";
import * as authClient from "@/lib/server/auth-client";
import { POST as startLink } from "@/app/api/bff/setup/google/link/start/route";

describe("BFF Google Sign-In & Handoff Integration", () => {
  beforeEach(() => {
    sessionStore.resetAll();
    vi.restoreAllMocks();
    Object.defineProperty(serverConfig, "ownerSetupEnabled", { value: false, configurable: true });
  });

  it.each(["LOGIN", "LINK"] as const)(
    "fences reversed %s start responses and cookies",
    async (mode) => {
      let resolveOlder!: (
        value: Awaited<ReturnType<typeof authClient.springStartGoogleSignIn>>
      ) => void;
      const response = (attemptId: string) => ({
        attemptId,
        authorizationUrl: "https://accounts.google.com/o/oauth2/v2/auth",
        expiresIn: 300,
        setCookieHeaders: [
          `${serverConfig.googleSignInBindCookie}=${attemptId}; Path=/api/v1/auth/google/callback; HttpOnly; SameSite=Lax; Max-Age=300`,
        ],
      });
      vi.spyOn(authClient, mode === "LOGIN" ? "springStartGoogleSignIn" : "springStartGoogleLink")
        .mockImplementationOnce(
          () =>
            new Promise<Awaited<ReturnType<typeof authClient.springStartGoogleSignIn>>>(
              (resolve) => {
                resolveOlder = resolve;
              }
            )
        )
        .mockResolvedValueOnce(response("newer"));
      Object.defineProperty(serverConfig, "ownerSetupEnabled", { value: true, configurable: true });
      const { sid, record, csrfToken } = sessionStore.createSession(
        mode === "LOGIN" ? "ANONYMOUS" : "SETUP_ONLY"
      );
      record.accessToken = "setup-token";
      const makeRequest = () =>
        new NextRequest("http://localhost:3000/api/bff/start", {
          method: "POST",
          headers: {
            host: "localhost:3000",
            origin: "http://localhost:3000",
            "x-ppv-csrf": csrfToken,
            cookie: `ppv_dev_sid=${sid}`,
            "content-type": "application/json",
          },
          body: JSON.stringify({ password: "fresh-synthetic-password" }),
        });
      const handler = mode === "LOGIN" ? startGoogleAuth : startLink;
      const older = handler(makeRequest());
      await vi.waitFor(() => expect(resolveOlder).toBeDefined());
      const newer = await handler(makeRequest());
      expect(newer.status).toBe(200);
      const savedVerifier = record.handoffVerifier;
      resolveOlder(response("older"));
      const rejected = await older;
      expect(rejected.status).toBe(409);
      expect(rejected.headers.has("set-cookie")).toBe(false);
      expect(record.attemptId).toBe("newer");
      expect(record.handoffVerifier).toBe(savedVerifier);
    }
  );

  it("initiates Google sign-in: sets binding cookie and stores handoffVerifier in session", async () => {
    vi.spyOn(authClient, "springStartGoogleSignIn").mockResolvedValue({
      attemptId: "attempt-123",
      authorizationUrl: "https://accounts.google.com/o/oauth2/v2/auth?mock=1",
      expiresIn: 300,
      setCookieHeaders: [
        `${serverConfig.googleSignInBindCookie}=mock-binding-secret; Path=/api/v1/auth/google/callback; HttpOnly; SameSite=Lax; Max-Age=300`,
      ],
    });

    const { sid, csrfToken } = sessionStore.createSession("ANONYMOUS");

    const req = new NextRequest("http://localhost:3000/api/bff/auth/google/start", {
      method: "POST",
      headers: {
        host: "localhost:3000",
        origin: "http://localhost:3000",
        "x-ppv-csrf": csrfToken,
        cookie: `${serverConfig.sessionCookieName}=${sid}`,
      },
    });

    const res = await startGoogleAuth(req);
    expect(res.status).toBe(200);

    const body = await res.json();
    expect(body.data?.authorizationUrl).toContain("accounts.google.com");
    expect(body.data?.attemptId).toBe("attempt-123");

    // Session must have retained the raw handoffVerifier
    const record = sessionStore.getSession(sid);
    expect(record?.handoffVerifier).toBeDefined();
    expect(record?.handoffVerifier?.length).toBeGreaterThanOrEqual(43);

    // Binding cookie must be relayed
    const bindCookie = res.cookies.get(serverConfig.googleSignInBindCookie);
    expect(bindCookie).toBeDefined();
    expect(bindCookie?.value).toBe("mock-binding-secret");
    expect(bindCookie?.path).toBe("/api/v1/auth/google/callback");
  });

  it("completes Google sign-in: redeems ticket with verifier and upgrades to AUTHENTICATED session", async () => {
    vi.spyOn(authClient, "springRedeemHandoff").mockResolvedValue({
      accessToken: "mock.access.jwt",
      refreshToken: "mock-opaque-refresh-123",
      tokenType: "Bearer",
      expiresIn: 900,
    });

    const { sid, record } = sessionStore.createSession("ANONYMOUS");
    record.handoffVerifier = "test-verifier-abcdefghijklmnopqrstuvwxyz123456";

    const req = new NextRequest("http://localhost:3000/api/bff/auth/google/complete", {
      method: "GET",
      headers: {
        host: "localhost:3000",
        cookie: `${serverConfig.sessionCookieName}=${sid}; ${serverConfig.googleHandoffCookie}=mock-ticket-43chars-base64url-ticket12345`,
      },
    });

    const res = await completeGoogleAuth(req);
    expect(res.status).toBe(303);
    expect(res.headers.get("location")).toContain("/personal/storage/google-drive");

    // Old anonymous SID is invalidated
    expect(sessionStore.getSession(sid)).toBeNull();

    // New session cookie installed
    const newSidCookie = res.cookies.get(serverConfig.sessionCookieName);
    expect(newSidCookie).toBeDefined();
    expect(newSidCookie?.value).not.toBe(sid);

    const newRecord = sessionStore.getSession(newSidCookie?.value);
    expect(newRecord?.purpose).toBe("AUTHENTICATED");
    expect(newRecord?.accessToken).toBe("mock.access.jwt");

    // Handoff cookie cleared
    const clearTicketCookie = res.cookies.get(serverConfig.googleHandoffCookie);
    expect(clearTicketCookie?.maxAge).toBe(0);
  });

  it("fails completion and clears ticket cookie if ticket or verifier is missing", async () => {
    const { sid } = sessionStore.createSession("ANONYMOUS");
    // No verifier stored

    const req = new NextRequest("http://localhost:3000/api/bff/auth/google/complete", {
      method: "GET",
      headers: {
        host: "localhost:3000",
        cookie: `${serverConfig.sessionCookieName}=${sid}; ${serverConfig.googleHandoffCookie}=mock-ticket-123`,
      },
    });

    const res = await completeGoogleAuth(req);
    expect(res.status).toBe(303);
    expect(res.headers.get("location")).toContain("/login?error=handoff_invalid");

    const clearTicketCookie = res.cookies.get(serverConfig.googleHandoffCookie);
    expect(clearTicketCookie?.maxAge).toBe(0);
  });
});

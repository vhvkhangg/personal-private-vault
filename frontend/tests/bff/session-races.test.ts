import { describe, it, expect, beforeEach, vi } from "vitest";
import { sessionStore } from "@/lib/server/session-store";
import { ensureFreshAccessToken } from "@/lib/server/auth-client";

describe("BFF Session Races & Singleflight Refresh", () => {
  beforeEach(() => {
    sessionStore.resetAll();
    vi.restoreAllMocks();
  });

  it("shares exactly one Spring refresh when two concurrent requests hit an expiring token", async () => {
    let fetchCalls = 0;
    vi.spyOn(globalThis, "fetch").mockImplementation(async (url) => {
      if (String(url).includes("/api/v1/auth/refresh")) {
        fetchCalls++;
        await new Promise((r) => setTimeout(r, 20));
        return new Response(
          JSON.stringify({
            data: {
              accessToken: `header.payload.signature${fetchCalls}`,
              refreshToken: `refresh_${"x".repeat(40)}_${fetchCalls}`,
              tokenType: "Bearer",
              expiresIn: 900,
            },
          }),
          { status: 200, headers: { "Content-Type": "application/json" } }
        );
      }
      return new Response("{}", { status: 200 });
    });

    const { record } = sessionStore.createSession("AUTHENTICATED");
    record.accessToken = "old-expiring-jwt";
    record.refreshToken = "opaque-refresh-1";
    record.accessExpiresAt = Date.now() - 1000; // Expired

    // Two concurrent callers
    const [token1, token2] = await Promise.all([
      ensureFreshAccessToken(record),
      ensureFreshAccessToken(record),
    ]);

    expect(fetchCalls).toBe(1);
    expect(token1).toBe("header.payload.signature1");
    expect(token2).toBe("header.payload.signature1");
    expect(record.accessToken).toBe("header.payload.signature1");
  });

  it("ensures logout during held refresh wins and discards late returned tokens", async () => {
    let revokeCalled = false;
    vi.spyOn(globalThis, "fetch").mockImplementation(async (url) => {
      if (String(url).includes("/api/v1/auth/refresh")) {
        // Slow refresh
        await new Promise((r) => setTimeout(r, 60));
        return new Response(
          JSON.stringify({
            data: {
              accessToken: "header.payload.signature",
              refreshToken: "late_refresh_" + "x".repeat(40),
              tokenType: "Bearer",
              expiresIn: 900,
            },
          }),
          { status: 200, headers: { "Content-Type": "application/json" } }
        );
      }
      if (String(url).includes("/api/v1/auth/revoke")) {
        revokeCalled = true;
        return new Response("{}", { status: 200 });
      }
      return new Response("{}", { status: 200 });
    });

    const { sid, record } = sessionStore.createSession("AUTHENTICATED");
    record.accessToken = "old-jwt";
    record.refreshToken = "old-refresh";
    record.accessExpiresAt = Date.now() - 1000;

    // Start refresh in background
    const refreshPromise = ensureFreshAccessToken(record);

    // Synchronous logout arrives before network reply
    sessionStore.invalidate(sid);

    // Refresh completes
    await expect(refreshPromise).rejects.toThrow("Session refresh failed");

    // Late tokens must not be installed
    expect(sessionStore.getSession(sid)).toBeNull();
    // Newly issued refresh token must be best-effort revoked
    await new Promise((r) => setTimeout(r, 20));
    expect(revokeCalled).toBe(true);
  });
});

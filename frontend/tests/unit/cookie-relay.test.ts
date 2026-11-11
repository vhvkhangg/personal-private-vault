import { describe, it, expect } from "vitest";
import { NextResponse } from "next/server";
import { setSessionCookie, clearSessionCookie, relaySpringCookie } from "@/lib/server/cookie-relay";
import { serverConfig } from "@/lib/server/config";

describe("cookieRelay", () => {
  it("sets session cookie with HttpOnly, Lax, Path=/", () => {
    const res = NextResponse.json({});
    setSessionCookie(res, "test-session-id-123", 3600);

    const cookie = res.cookies.get(serverConfig.sessionCookieName);
    expect(cookie).toBeDefined();
    expect(cookie?.value).toBe("test-session-id-123");
    expect(cookie?.path).toBe("/");
    expect(cookie?.httpOnly).toBe(true);
    expect(cookie?.sameSite).toBe("lax");
  });

  it("clears session cookie with maxAge=0", () => {
    const res = NextResponse.json({});
    clearSessionCookie(res);

    const cookie = res.cookies.get(serverConfig.sessionCookieName);
    expect(cookie).toBeDefined();
    expect(cookie?.value).toBe("");
    expect(cookie?.maxAge).toBe(0);
  });

  it("relays exact Spring binding cookie to expected path", () => {
    const res = NextResponse.json({});
    const springHeaders = [
      "other_cookie=value123; Path=/",
      "PPV_DRIVE_BINDING_1=secret-binding-token; Path=/api/v1/drive/oauth/callback; HttpOnly; SameSite=Lax; Max-Age=300",
    ];

    const matched = relaySpringCookie(
      res,
      springHeaders,
      "PPV_DRIVE_BINDING_1",
      "/api/v1/drive/oauth/callback"
    );

    expect(matched).toBe(true);
    const cookie = res.cookies.get("PPV_DRIVE_BINDING_1");
    expect(cookie).toBeDefined();
    expect(cookie?.value).toBe("secret-binding-token");
    expect(cookie?.path).toBe("/api/v1/drive/oauth/callback");
    expect(cookie?.httpOnly).toBe(true);
  });
});

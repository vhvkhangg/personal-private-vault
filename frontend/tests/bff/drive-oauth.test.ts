import { describe, it, expect, beforeEach, vi } from "vitest";
import { NextRequest } from "next/server";
import { sessionStore } from "@/lib/server/session-store";
import { serverConfig } from "@/lib/server/config";
import { POST as postDrive, DELETE as deleteDrive } from "@/app/api/bff/drive/[...segments]/route";
import * as driveClient from "@/lib/server/drive-client";

describe("BFF Drive OAuth Cookie Relay & Disconnect", () => {
  beforeEach(() => {
    sessionStore.resetAll();
    vi.restoreAllMocks();
  });

  it("relays PPV_DRIVE_BINDING_1 cookie for slot 1 authorize", async () => {
    vi.spyOn(driveClient, "springAuthorizeConnection").mockResolvedValue({
      data: {
        slot: 1,
        authorizationUrl: "https://accounts.google.com/o/oauth2/v2/auth?scope=drive.file",
        expiresAt: "2026-11-09T00:05:00Z",
      },
      setCookieHeaders: [
        `${serverConfig.driveBinding1Cookie}=secret-slot1-binding; Path=/api/v1/drive/oauth/callback; HttpOnly; SameSite=Lax; Max-Age=300`,
      ],
    });

    const { sid, record, csrfToken } = sessionStore.createSession("AUTHENTICATED");
    record.accessToken = "valid-jwt-token";
    record.refreshToken = "valid-refresh";
    record.accessExpiresAt = Date.now() + 3600_000;

    const req = new NextRequest("http://localhost:3000/api/bff/drive/connections/1/authorize", {
      method: "POST",
      headers: {
        host: "localhost:3000",
        origin: "http://localhost:3000",
        "x-ppv-csrf": csrfToken,
        cookie: `${serverConfig.sessionCookieName}=${sid}`,
      },
    });

    const res = await postDrive(req, {
      params: Promise.resolve({ segments: ["connections", "1", "authorize"] }),
    });
    expect(res.status).toBe(200);

    const body = await res.json();
    expect(body.data?.slot).toBe(1);
    expect(body.data?.authorizationUrl).toContain("accounts.google.com");

    const bindingCookie = res.cookies.get(serverConfig.driveBinding1Cookie);
    expect(bindingCookie).toBeDefined();
    expect(bindingCookie?.value).toBe("secret-slot1-binding");
    expect(bindingCookie?.path).toBe("/api/v1/drive/oauth/callback");
    expect(bindingCookie?.httpOnly).toBe(true);
  });

  it("relays PPV_DRIVE_BINDING_2 cookie for slot 2 authorize", async () => {
    vi.spyOn(driveClient, "springAuthorizeConnection").mockResolvedValue({
      data: {
        slot: 2,
        authorizationUrl: "https://accounts.google.com/o/oauth2/v2/auth?scope=drive.file",
        expiresAt: "2026-11-09T00:05:00Z",
      },
      setCookieHeaders: [
        `${serverConfig.driveBinding2Cookie}=secret-slot2-binding; Path=/api/v1/drive/oauth/callback; HttpOnly; SameSite=Lax; Max-Age=300`,
      ],
    });

    const { sid, record, csrfToken } = sessionStore.createSession("AUTHENTICATED");
    record.accessToken = "valid-jwt-token";
    record.refreshToken = "valid-refresh";
    record.accessExpiresAt = Date.now() + 3600_000;

    const req = new NextRequest("http://localhost:3000/api/bff/drive/connections/2/authorize", {
      method: "POST",
      headers: {
        host: "localhost:3000",
        origin: "http://localhost:3000",
        "x-ppv-csrf": csrfToken,
        cookie: `${serverConfig.sessionCookieName}=${sid}`,
      },
    });

    const res = await postDrive(req, {
      params: Promise.resolve({ segments: ["connections", "2", "authorize"] }),
    });
    expect(res.status).toBe(200);

    const bindingCookie = res.cookies.get(serverConfig.driveBinding2Cookie);
    expect(bindingCookie).toBeDefined();
    expect(bindingCookie?.value).toBe("secret-slot2-binding");
    expect(bindingCookie?.path).toBe("/api/v1/drive/oauth/callback");
  });

  it("disconnects slot 1 and clears PPV_DRIVE_BINDING_1 cookie", async () => {
    vi.spyOn(driveClient, "springDisconnectConnection").mockResolvedValue({
      slot: 1,
      status: "DISCONNECTED",
      remoteRevocation: "COMPLETED",
    });

    const { sid, record, csrfToken } = sessionStore.createSession("AUTHENTICATED");
    record.accessToken = "valid-jwt-token";
    record.refreshToken = "valid-refresh";
    record.accessExpiresAt = Date.now() + 3600_000;

    const req = new NextRequest("http://localhost:3000/api/bff/drive/connections/1", {
      method: "DELETE",
      headers: {
        host: "localhost:3000",
        origin: "http://localhost:3000",
        "x-ppv-csrf": csrfToken,
        cookie: `${serverConfig.sessionCookieName}=${sid}`,
      },
    });

    const res = await deleteDrive(req, {
      params: Promise.resolve({ segments: ["connections", "1"] }),
    });
    expect(res.status).toBe(200);

    const clearCookie = res.cookies.get(serverConfig.driveBinding1Cookie);
    expect(clearCookie).toBeDefined();
    expect(clearCookie?.maxAge).toBe(0);
    expect(clearCookie?.path).toBe("/api/v1/drive/oauth/callback");
  });
});

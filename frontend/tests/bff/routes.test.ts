import { describe, it, expect, beforeEach } from "vitest";
import { NextRequest } from "next/server";
import { sessionStore } from "@/lib/server/session-store";
import { serverConfig } from "@/lib/server/config";
import { GET as getDrive, POST as postDrive } from "@/app/api/bff/drive/[...segments]/route";
import { POST as startGoogleAuth } from "@/app/api/bff/auth/google/start/route";

describe("BFF Route Security & Authority Guards", () => {
  beforeEach(() => {
    sessionStore.resetAll();
  });

  it("denies unauthenticated direct access to drive endpoints with 401", async () => {
    const req = new NextRequest("http://localhost:3000/api/bff/drive/connections", {
      headers: { host: "localhost:3000" },
    });
    const res = await getDrive(req, { params: Promise.resolve({ segments: ["connections"] }) });
    expect(res.status).toBe(401);
    const body = await res.json();
    expect(body.error?.code).toBe("UNAUTHORIZED");
  });

  it("denies SETUP_ONLY sessions access to drive endpoints with 403", async () => {
    const { sid } = sessionStore.createSession("SETUP_ONLY");
    const req = new NextRequest("http://localhost:3000/api/bff/drive/connections", {
      headers: {
        host: "localhost:3000",
        cookie: `${serverConfig.sessionCookieName}=${sid}`,
      },
    });
    const res = await getDrive(req, { params: Promise.resolve({ segments: ["connections"] }) });
    expect(res.status).toBe(403);
    const body = await res.json();
    expect(body.error?.code).toBe("FORBIDDEN");
  });

  it("denies mutations with invalid or mismatched Origin header with 403", async () => {
    const { sid, csrfToken } = sessionStore.createSession("AUTHENTICATED");
    const req = new NextRequest("http://localhost:3000/api/bff/drive/connections/1/root-folder", {
      method: "POST",
      headers: {
        host: "localhost:3000",
        origin: "http://attacker.com",
        "x-ppv-csrf": csrfToken,
        cookie: `${serverConfig.sessionCookieName}=${sid}`,
      },
    });
    const res = await postDrive(req, {
      params: Promise.resolve({ segments: ["connections", "1", "root-folder"] }),
    });
    expect(res.status).toBe(403);
    const body = await res.json();
    expect(body.error?.code).toBe("INVALID_ORIGIN");
  });

  it("denies mutations with cross-site Sec-Fetch-Site with 403", async () => {
    const { sid, csrfToken } = sessionStore.createSession("AUTHENTICATED");
    const req = new NextRequest("http://localhost:3000/api/bff/drive/connections/1/root-folder", {
      method: "POST",
      headers: {
        host: "localhost:3000",
        origin: "http://localhost:3000",
        "sec-fetch-site": "cross-site",
        "x-ppv-csrf": csrfToken,
        cookie: `${serverConfig.sessionCookieName}=${sid}`,
      },
    });
    const res = await postDrive(req, {
      params: Promise.resolve({ segments: ["connections", "1", "root-folder"] }),
    });
    expect(res.status).toBe(403);
    const body = await res.json();
    expect(body.error?.code).toBe("CROSS_SITE_MUTATION_DENIED");
  });

  it("denies mutations with missing or invalid CSRF token with 403", async () => {
    const { sid } = sessionStore.createSession("AUTHENTICATED");
    const req = new NextRequest("http://localhost:3000/api/bff/drive/connections/1/root-folder", {
      method: "POST",
      headers: {
        host: "localhost:3000",
        origin: "http://localhost:3000",
        "x-ppv-csrf": "invalid-csrf-token-123456789012345678901234567890",
        cookie: `${serverConfig.sessionCookieName}=${sid}`,
      },
    });
    const res = await postDrive(req, {
      params: Promise.resolve({ segments: ["connections", "1", "root-folder"] }),
    });
    expect(res.status).toBe(403);
    const body = await res.json();
    expect(body.error?.code).toBe("CSRF_TOKEN_INVALID");
  });

  it("rejects forwarded host authority headers with 400", async () => {
    const req = new NextRequest("http://localhost:3000/api/bff/auth/google/start", {
      method: "POST",
      headers: {
        host: "localhost:3000",
        "x-forwarded-host": "evil.com",
      },
    });
    const res = await startGoogleAuth(req);
    expect(res.status).toBe(400);
    const body = await res.json();
    expect(body.error?.code).toBe("INVALID_HOST_AUTHORITY");
  });
});

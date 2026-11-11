import { describe, it, expect, vi, afterEach } from "vitest";
import { formatBytes, formatDateTime, formatDateOnly, generateActionKey } from "@/lib/format";
import { validateAuthorizationUrl, springRedeemHandoff } from "@/lib/server/auth-client";
import { serverConfig } from "@/lib/server/config";
import {
  springGetConnections,
  springEnsureRootFolder,
  springListItems,
  springGetStorageQuota,
} from "@/lib/server/drive-client";
import { readBoundedResponseJson } from "@/lib/server/streaming";
import { NextRequest } from "next/server";
import { readBoundedJson } from "@/lib/server/route-guards";
import { trustedUpstreamError } from "@/lib/server/bff-errors";

afterEach(() => {
  vi.restoreAllMocks();
  vi.useRealTimers();
});

describe("strict upstream boundaries", () => {
  it("preserves the canonical incomplete-search 502 without trusting arbitrary provider errors", () => {
    const envelope = {
      error: { code: "DRIVE_SEARCH_INCOMPLETE", message: "synthetic-private-detail" },
    };
    expect(trustedUpstreamError(envelope, 502)).toMatchObject({
      code: "DRIVE_SEARCH_INCOMPLETE",
      httpStatus: 502,
      message: "DRIVE_SEARCH_INCOMPLETE",
    });
    expect(trustedUpstreamError(envelope, 409)).toMatchObject({
      code: "UPSTREAM_INVALID_RESPONSE",
      httpStatus: 502,
    });
  });
  it("cancels a stalled incoming JSON body at its request deadline", async () => {
    vi.useFakeTimers();
    let cancelled = false;
    const body = new ReadableStream<Uint8Array>({
      cancel() {
        cancelled = true;
      },
    });
    const init = { method: "POST", body, duplex: "half" as const };
    const request = new NextRequest("http://localhost:3000/api/bff/setup", init);
    const operation = readBoundedJson(request, 4096);
    const assertion = expect(operation).rejects.toMatchObject({ httpStatus: 408 });
    await vi.advanceTimersByTimeAsync(10_001);
    await assertion;
    expect(cancelled).toBe(true);
  });
  const selectedClient = "owner-selected.apps.googleusercontent.com";
  function validAuthorization() {
    Object.defineProperty(serverConfig, "googleSignInClientId", {
      value: selectedClient,
      configurable: true,
    });
    return new URL(
      "https://accounts.google.com/o/oauth2/v2/auth?" +
        new URLSearchParams({
          response_type: "code",
          client_id: selectedClient,
          redirect_uri: serverConfig.springOrigin + "/api/v1/auth/google/callback",
          state: "s".repeat(43),
          nonce: "n".repeat(43),
          scope: "openid email",
          code_challenge: "p".repeat(43),
          code_challenge_method: "S256",
        })
    );
  }
  it("accepts the exact selected provider and callback", () => {
    expect(() =>
      validateAuthorizationUrl(validAuthorization().href, "/api/v1/auth/google/callback")
    ).not.toThrow();
  });
  it.each([
    "port",
    "userinfo",
    "protocol",
    "fixture-shortcut",
    "client",
    "duplicate",
    "callback-query",
    "fragment",
  ])("rejects %s before releasing an authorization URL", (variant) => {
    const url = validAuthorization();
    if (variant === "port") url.port = "8443";
    if (variant === "userinfo") url.username = "attacker";
    if (variant === "protocol") url.protocol = "http:";
    if (variant === "fixture-shortcut") url.href = "ftp://localhost:19080/unselected-endpoint";
    if (variant === "client") url.searchParams.set("client_id", "unselected");
    if (variant === "duplicate") url.searchParams.append("client_id", selectedClient);
    if (variant === "callback-query")
      url.searchParams.set(
        "redirect_uri",
        serverConfig.springOrigin + "/api/v1/auth/google/callback?unexpected=1"
      );
    if (variant === "fragment") url.hash = "unexpected";
    expect(() => validateAuthorizationUrl(url.href, "/api/v1/auth/google/callback")).toThrow();
  });
  it.each(["connections", "root", "items", "quota", "tokens"])(
    "rejects a malformed %s DTO",
    async (type) => {
      vi.spyOn(globalThis, "fetch").mockResolvedValue(
        Response.json({ data: { unexpected: true } })
      );
      const operation =
        type === "connections"
          ? springGetConnections("token")
          : type === "root"
            ? springEnsureRootFolder("token", 1)
            : type === "items"
              ? springListItems("token", 1, new URLSearchParams())
              : type === "quota"
                ? springGetStorageQuota("token", 1)
                : springRedeemHandoff("ticket", "verifier");
      await expect(operation).rejects.toMatchObject({
        code: "UPSTREAM_INVALID_RESPONSE",
        httpStatus: 502,
      });
    }
  );
  it("sanitizes unknown upstream error status, code and credential-bearing text", async () => {
    vi.spyOn(globalThis, "fetch").mockResolvedValue(
      Response.json(
        {
          error: { code: "PROVIDER_SECRET_CANARY", message: "synthetic-private-canary" },
        },
        { status: 418 }
      )
    );
    await expect(springGetConnections("token")).rejects.toMatchObject({
      code: "UPSTREAM_INVALID_RESPONSE",
      httpStatus: 502,
      message: "Invalid upstream response",
    });
  });
  it("bounds a chunked JSON body and cancels it", async () => {
    let cancelled = false;
    const response = new Response(
      new ReadableStream({
        pull(c) {
          c.enqueue(new Uint8Array(65));
        },
        cancel() {
          cancelled = true;
        },
      })
    );
    await expect(readBoundedResponseJson(response, 64)).rejects.toMatchObject({ httpStatus: 502 });
    expect(cancelled).toBe(true);
  });
  it("cancels a stalled JSON body at the finite deadline", async () => {
    vi.useFakeTimers();
    let cancelled = false;
    const operation = readBoundedResponseJson(
      new Response(
        new ReadableStream({
          cancel() {
            cancelled = true;
          },
        })
      )
    );
    const assertion = expect(operation).rejects.toMatchObject({ httpStatus: 504 });
    await vi.advanceTimersByTimeAsync(10_001);
    await assertion;
    expect(cancelled).toBe(true);
  });
});

describe("format utilities and contracts", () => {
  it("formats byte values accurately", () => {
    expect(formatBytes(0)).toBe("0 B");
    expect(formatBytes(null)).toBe("—");
    expect(formatBytes(1024)).toBe("1.0 KiB");
    expect(formatBytes(52_428_800)).toBe("50.0 MiB");
  });

  it("formats timestamps into Asia/Ho_Chi_Minh timezone", () => {
    const iso = "2026-11-09T00:00:00Z";
    const formatted = formatDateTime(iso);
    expect(formatted).toContain("2026");
    expect(formatDateTime(null)).toBe("—");
  });

  it("formats date-only string in Asia/Ho_Chi_Minh", () => {
    const iso = "2026-11-09T00:00:00Z";
    const formatted = formatDateOnly(iso);
    expect(formatted).toContain("2026");
    expect(formatDateOnly(null)).toBe("—");
  });

  it("generates an action key matching yyyyMMddTHHmmssZ.lowercase-UUIDv4", () => {
    const key = generateActionKey();
    expect(key).toMatch(
      /^\d{8}T\d{6}Z\.[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/
    );
  });
});

import { describe, it, expect, beforeEach, afterEach, vi } from "vitest";
import { NextRequest } from "next/server";
import { sessionStore } from "@/lib/server/session-store";
import { serverConfig } from "@/lib/server/config";
import { resetActiveTransfersForTest, getActiveTransferCount } from "@/lib/server/streaming";
import { POST as postDrive, GET as getDrive } from "@/app/api/bff/drive/[...segments]/route";

describe("BFF Binary Transfers Integration", () => {
  beforeEach(() => {
    sessionStore.resetAll();
    resetActiveTransfersForTest();
    vi.restoreAllMocks();
  });
  afterEach(() => {
    sessionStore.resetAll();
    vi.useRealTimers();
  });

  it.each(["short", "excess", "logout", "idle", "client-cancel", "encoding"] as const)(
    "fails %s downloads and releases upstream/admission",
    async (mode) => {
      if (mode === "idle") vi.useFakeTimers();
      let cancelled = false;
      let upstreamSignal: AbortSignal | null = null;
      vi.spyOn(globalThis, "fetch").mockImplementation(async (_url, init) => {
        upstreamSignal = init?.signal ?? null;
        expect(new Headers(init?.headers).get("accept-encoding")).toBe("identity");
        const body = new ReadableStream<Uint8Array>({
          start(c) {
            if (mode === "short") {
              c.enqueue(new Uint8Array(1));
              c.close();
            }
            if (mode === "excess") {
              c.enqueue(new Uint8Array(3));
              c.close();
            }
          },
          cancel() {
            cancelled = true;
          },
        });
        return new Response(body, {
          headers: {
            "Content-Length": "2",
            "Content-Disposition": 'attachment; filename="file.bin"',
            ...(mode === "encoding" ? { "Content-Encoding": "gzip" } : {}),
          },
        });
      });
      const { sid, record } = sessionStore.createSession("AUTHENTICATED");
      record.accessToken = "valid";
      record.refreshToken = "refresh";
      record.accessExpiresAt = Date.now() + 3600_000;
      const client = new AbortController();
      const response = await getDrive(
        new NextRequest("http://localhost:3000/api/bff/drive/connections/1/files/file/download", {
          signal: client.signal,
          headers: { host: "localhost:3000", cookie: `ppv_dev_sid=${sid}` },
        }),
        { params: Promise.resolve({ segments: ["connections", "1", "files", "file", "download"] }) }
      );
      if (mode === "encoding") {
        expect(response.status).toBe(502);
      } else {
        expect(response.status).toBe(200);
        const read = response.arrayBuffer();
        const assertion = expect(read).rejects.toThrow();
        if (mode === "logout") sessionStore.invalidate(sid);
        if (mode === "idle") await vi.advanceTimersByTimeAsync(15_001);
        if (mode === "client-cancel") client.abort();
        await assertion;
      }
      expect(getActiveTransferCount()).toBe(0);
      expect(upstreamSignal!.aborted).toBe(true);
      if (mode !== "short" && mode !== "excess") expect(cancelled).toBe(true);
    }
  );

  it("handles binary download streaming with proper headers and content", async () => {
    const mockBinary = new Uint8Array([10, 20, 30, 40, 50]);
    vi.spyOn(globalThis, "fetch").mockResolvedValue(
      new Response(mockBinary, {
        status: 200,
        headers: {
          "Content-Type": "application/octet-stream",
          "Content-Disposition": 'attachment; filename="data.bin"',
          "Content-Length": "5",
        },
      })
    );

    const { sid, record } = sessionStore.createSession("AUTHENTICATED");
    record.accessToken = "valid-jwt";
    record.refreshToken = "valid-refresh";
    record.accessExpiresAt = Date.now() + 3600_000;

    const req = new NextRequest(
      "http://localhost:3000/api/bff/drive/connections/1/files/file-101/download",
      {
        headers: {
          host: "localhost:3000",
          cookie: `${serverConfig.sessionCookieName}=${sid}`,
        },
      }
    );

    const res = await getDrive(req, {
      params: Promise.resolve({ segments: ["connections", "1", "files", "file-101", "download"] }),
    });

    expect(res.status).toBe(200);
    expect(res.headers.get("content-type")).toBe("application/octet-stream");
    expect(res.headers.get("content-disposition")).toBe('attachment; filename="data.bin"');
    expect(res.headers.get("content-length")).toBe("5");

    const bytes = await res.arrayBuffer();
    expect(new Uint8Array(bytes)).toEqual(mockBinary);
  });

  it("rejects upload without Idempotency-Key header", async () => {
    const { sid, record, csrfToken } = sessionStore.createSession("AUTHENTICATED");
    record.accessToken = "valid-jwt";
    record.refreshToken = "valid-refresh";
    record.accessExpiresAt = Date.now() + 3600_000;

    const req = new NextRequest("http://localhost:3000/api/bff/drive/connections/1/files", {
      method: "POST",
      headers: {
        host: "localhost:3000",
        origin: "http://localhost:3000",
        "x-ppv-csrf": csrfToken,
        "content-type": "multipart/form-data; boundary=----boundary123",
        cookie: `${serverConfig.sessionCookieName}=${sid}`,
      },
    });

    const res = await postDrive(req, {
      params: Promise.resolve({ segments: ["connections", "1", "files"] }),
    });

    expect(res.status).toBe(400);
    const body = await res.json();
    expect(body.error?.code).toBe("UPLOAD_KEY_REQUIRED");
  });
});

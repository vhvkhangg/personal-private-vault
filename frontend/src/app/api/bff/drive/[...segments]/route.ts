import "server-only";
import { type NextRequest, NextResponse } from "next/server";
import { serverConfig } from "@/lib/server/config";
import {
  requireAuthenticatedSession,
  validateAuthority,
  validateMutationCsrf,
  assertActiveSession,
  readBoundedJson,
} from "@/lib/server/route-guards";
import { ensureFreshAccessToken } from "@/lib/server/auth-client";
import {
  springGetConnections,
  springAuthorizeConnection,
  springDisconnectConnection,
  springEnsureRootFolder,
  springCreateFolder,
  springListItems,
  springGetStorageQuota,
  validateUploadedFile,
} from "@/lib/server/drive-client";
import { relaySpringCookie } from "@/lib/server/cookie-relay";
import {
  createByteLimitingStream,
  createTransferScope,
  forwardDownload,
  readBoundedResponseJson,
} from "@/lib/server/streaming";
import { handleServerException, BffException, trustedUpstreamError } from "@/lib/server/bff-errors";

export const dynamic = "force-dynamic";

function getSpringHost(): string {
  try {
    const u = new URL(serverConfig.springOrigin);
    return u.host;
  } catch {
    return "localhost:8080";
  }
}

const SAFE_ID_REGEX = /^[a-zA-Z0-9_-]{1,128}$/;

function validateSafeId(id: string, label: string): void {
  if (!id || !SAFE_ID_REGEX.test(id)) {
    throw new BffException("DRIVE_INVALID_INPUT", 400, `Invalid ${label} identifier`);
  }
}

function parseSlot(str: string): 1 | 2 {
  if (str === "1") return 1;
  if (str === "2") return 2;
  throw new BffException("DRIVE_INVALID_SLOT", 400, "Slot must be 1 or 2");
}

type RouteContext = {
  params: Promise<{ segments: string[] }>;
};

export async function GET(req: NextRequest, { params }: RouteContext) {
  try {
    validateAuthority(req, false);
    const { record } = requireAuthenticatedSession(req);
    const { segments } = await params;

    // Validate segments and query parameters BEFORE calling ensureFreshAccessToken

    // 1. GET /api/bff/drive/connections
    if (segments.length === 1 && segments[0] === "connections") {
      const accessToken = await ensureFreshAccessToken(record);
      const data = await springGetConnections(accessToken);
      return NextResponse.json({ data }, { headers: { "Cache-Control": "private, no-store" } });
    }

    // 2. GET /api/bff/drive/connections/{1|2}/storage-quota
    if (segments.length === 3 && segments[0] === "connections" && segments[2] === "storage-quota") {
      const slot = parseSlot(segments[1]!);
      const accessToken = await ensureFreshAccessToken(record);
      const data = await springGetStorageQuota(accessToken, slot);
      return NextResponse.json({ data }, { headers: { "Cache-Control": "private, no-store" } });
    }

    // 3. GET /api/bff/drive/connections/{1|2}/items
    if (segments.length === 3 && segments[0] === "connections" && segments[2] === "items") {
      const slot = parseSlot(segments[1]!);

      // Validate query params strictly before any Spring work
      const rawQuery = req.nextUrl.search.replace(/^\?/, "");
      const seenKeys = new Set<string>();
      const ALLOWED_ITEM_PARAMS = new Set([
        "scope",
        "parentId",
        "limit",
        "cursor",
        "name",
        "type",
        "from",
        "to",
        "timeZone",
      ]);

      if (rawQuery.length > 0) {
        for (const pair of rawQuery.split("&")) {
          if (!pair) continue;
          const eqIdx = pair.indexOf("=");
          const rawKey = eqIdx >= 0 ? pair.slice(0, eqIdx) : pair;
          const key = decodeURIComponent(rawKey);
          if (!ALLOWED_ITEM_PARAMS.has(key)) {
            throw new BffException("DRIVE_INVALID_INPUT", 400, `Unknown query parameter: ${key}`);
          }
          if (seenKeys.has(key)) {
            throw new BffException("DRIVE_INVALID_INPUT", 400, `Duplicate query parameter: ${key}`);
          }
          seenKeys.add(key);

          if (eqIdx >= 0) {
            const rawVal = pair.slice(eqIdx + 1);
            const val = decodeURIComponent(rawVal);
            if (key === "parentId") {
              if (val !== "root") {
                validateSafeId(val, "parentId");
              }
            } else if (key === "limit") {
              if (!/^\d+$/.test(val)) {
                throw new BffException("DRIVE_INVALID_LIMIT", 400, "Limit must be a valid integer");
              }
              const num = parseInt(val, 10);
              if (num < 1 || num > 50) {
                throw new BffException(
                  "DRIVE_INVALID_LIMIT",
                  400,
                  "Limit must be between 1 and 50"
                );
              }
            }
          }
        }
      }

      const accessToken = await ensureFreshAccessToken(record);
      const data = await springListItems(accessToken, slot, req.nextUrl.searchParams);
      return NextResponse.json({ data }, { headers: { "Cache-Control": "private, no-store" } });
    }

    // 4. GET /api/bff/drive/connections/{1|2}/files/{fileId}/thumbnail
    if (
      segments.length === 5 &&
      segments[0] === "connections" &&
      segments[2] === "files" &&
      segments[4] === "thumbnail"
    ) {
      const slot = parseSlot(segments[1]!);
      const fileId = segments[3]!;
      validateSafeId(fileId, "fileId");

      const accessToken = await ensureFreshAccessToken(record);
      const url = `${serverConfig.springOrigin}/api/v1/drive/connections/${encodeURIComponent(String(slot))}/files/${encodeURIComponent(fileId)}/thumbnail`;

      const scope = createTransferScope(record, req.signal);
      const timeout = setTimeout(
        () => scope.abort(new BffException("THUMBNAIL_TIMEOUT", 504)),
        8000
      );
      scope.signal.addEventListener("abort", () => clearTimeout(timeout), { once: true });
      try {
        const springRes = await fetch(url, {
          method: "GET",
          redirect: "error",
          cache: "no-store",
          signal: scope.signal,
          headers: {
            Authorization: `Bearer ${accessToken}`,
            Host: getSpringHost(),
            "Accept-Encoding": "identity",
          },
        });
        if (!springRes.ok) throw new BffException("THUMBNAIL_UNAVAILABLE", 404);
        const type = springRes.headers.get("content-type");
        const length = springRes.headers.get("content-length");
        if (
          (type !== "image/png" && type !== "image/jpeg") ||
          !springRes.body ||
          length === null ||
          !/^[1-9][0-9]*$/.test(length) ||
          Number(length) > 1024 * 1024 ||
          (springRes.headers.has("content-encoding") &&
            springRes.headers.get("content-encoding") !== "identity")
        ) {
          throw new BffException("THUMBNAIL_UNAVAILABLE", 404);
        }
        scope.checkSession();
        if (scope.signal.aborted) throw scope.signal.reason;
        scope.startBody();
        return new NextResponse(forwardDownload(springRes.body, Number(length), scope), {
          status: 200,
          headers: {
            "Content-Type": type,
            "Content-Length": length,
            "Cache-Control": "private, no-store",
            "X-Content-Type-Options": "nosniff",
            "Content-Security-Policy": "default-src 'none'",
          },
        });
      } catch (error) {
        scope.abort();
        throw error;
      }
    }

    // 5. GET /api/bff/drive/connections/{1|2}/files/{fileId}/download
    if (
      segments.length === 5 &&
      segments[0] === "connections" &&
      segments[2] === "files" &&
      segments[4] === "download"
    ) {
      const slot = parseSlot(segments[1]!);
      const fileId = segments[3]!;
      validateSafeId(fileId, "fileId");
      if (req.headers.has("range")) throw new BffException("DRIVE_RANGE_NOT_SUPPORTED", 400);
      const scope = createTransferScope(record, req.signal);
      try {
        const accessToken = await ensureFreshAccessToken(record);
        const url = `${serverConfig.springOrigin}/api/v1/drive/connections/${slot}/files/${encodeURIComponent(fileId)}/download`;
        const springRes = await fetch(url, {
          method: "GET",
          redirect: "error",
          headers: {
            Authorization: `Bearer ${accessToken}`,
            Host: getSpringHost(),
            "Cache-Control": "no-store",
            "Accept-Encoding": "identity",
          },
          cache: "no-store",
          signal: scope.signal,
        });
        if (!springRes.ok) {
          throw trustedUpstreamError(
            await readBoundedResponseJson(springRes, 4096),
            springRes.status
          );
        }
        const length = springRes.headers.get("content-length");
        const encoding = springRes.headers.get("content-encoding");
        if (
          springRes.status !== 200 ||
          length === null ||
          !/^(0|[1-9][0-9]*)$/.test(length) ||
          !Number.isSafeInteger(Number(length)) ||
          (encoding && encoding !== "identity")
        ) {
          void springRes.body?.cancel().catch(() => {});
          throw new BffException("DRIVE_DOWNLOAD_INVALID_FRAMING", 502);
        }
        const expectedBytes = Number(length);
        const disposition = springRes.headers.get("content-disposition");
        if (
          !disposition ||
          !/^attachment; /i.test(disposition) ||
          [...disposition].some((c) => c.charCodeAt(0) < 32 || c.charCodeAt(0) === 127) ||
          disposition.length > 4096
        ) {
          void springRes.body?.cancel().catch(() => {});
          throw new BffException("DRIVE_DOWNLOAD_INVALID_FRAMING", 502);
        }
        scope.checkSession();
        if (scope.signal.aborted) throw scope.signal.reason;
        const headers = {
          "Content-Type": "application/octet-stream",
          "Content-Disposition": disposition,
          "Content-Length": length,
          "Cache-Control": "private, no-store",
          "X-Content-Type-Options": "nosniff",
        };
        if (!springRes.body) {
          if (expectedBytes !== 0) throw new BffException("DRIVE_DOWNLOAD_TRUNCATED", 502);
          scope.close();
          return new NextResponse(null, { status: 200, headers });
        }
        scope.startBody();
        return new NextResponse(forwardDownload(springRes.body, expectedBytes, scope), {
          status: 200,
          headers,
        });
      } catch (err) {
        scope.abort();
        throw err;
      }
    }

    throw new BffException("NOT_FOUND", 404, "Unknown Drive endpoint");
  } catch (err) {
    return handleServerException(err);
  }
}

export async function POST(req: NextRequest, { params }: RouteContext) {
  try {
    validateAuthority(req, true);
    const { record } = requireAuthenticatedSession(req);
    validateMutationCsrf(req, record);
    const { segments } = await params;

    // 1. POST /api/bff/drive/connections/{1|2}/authorize
    if (segments.length === 3 && segments[0] === "connections" && segments[2] === "authorize") {
      const slot = parseSlot(segments[1]!);
      const capturedGeneration = record.generation;
      const accessToken = await ensureFreshAccessToken(record);

      const { data, setCookieHeaders } = await springAuthorizeConnection(accessToken, slot, record);

      // Fence check: ensure session was not invalidated, logged out, or expired during authorize
      assertActiveSession(record, capturedGeneration, "AUTHENTICATED");

      const res = NextResponse.json(
        { data },
        { headers: { "Cache-Control": "private, no-store" } }
      );
      const cookieName =
        slot === 1 ? serverConfig.driveBinding1Cookie : serverConfig.driveBinding2Cookie;
      const relayed = relaySpringCookie(
        res,
        setCookieHeaders,
        cookieName,
        "/api/v1/drive/oauth/callback"
      );
      if (!relayed) {
        throw new BffException(
          "DRIVE_UPSTREAM_ERROR",
          502,
          "Failed to relay Drive binding cookie from upstream"
        );
      }
      return res;
    }

    // 2. POST /api/bff/drive/connections/{1|2}/root-folder
    if (segments.length === 3 && segments[0] === "connections" && segments[2] === "root-folder") {
      const slot = parseSlot(segments[1]!);
      const accessToken = await ensureFreshAccessToken(record);
      const data = await springEnsureRootFolder(accessToken, slot);
      return NextResponse.json({ data }, { headers: { "Cache-Control": "private, no-store" } });
    }

    // 3. POST /api/bff/drive/connections/{1|2}/folders
    if (segments.length === 3 && segments[0] === "connections" && segments[2] === "folders") {
      const slot = parseSlot(segments[1]!);
      const key = req.headers.get("Idempotency-Key");
      if (!key)
        throw new BffException("FOLDER_KEY_REQUIRED", 400, "Idempotency-Key header is required");

      const body = await readBoundedJson<{ name?: unknown }>(req, 4096);
      if (
        !body.name ||
        typeof body.name !== "string" ||
        body.name.trim().length === 0 ||
        body.name.length > 255
      ) {
        throw new BffException(
          "DRIVE_INVALID_FILE_INPUT",
          400,
          "Folder name is required and must be 1-255 characters"
        );
      }
      const hasInvalidChar = Array.from(body.name).some((ch) => {
        const code = ch.charCodeAt(0);
        return code < 32 || code === 127 || ch === "/" || ch === "\\";
      });
      if (hasInvalidChar) {
        throw new BffException(
          "DRIVE_INVALID_FILE_INPUT",
          400,
          "Folder name contains invalid characters"
        );
      }

      const accessToken = await ensureFreshAccessToken(record);
      const { folder, status } = await springCreateFolder(accessToken, slot, key, body.name.trim());
      return NextResponse.json(
        { data: folder },
        { status, headers: { "Cache-Control": "private, no-store" } }
      );
    }

    // 4. POST /api/bff/drive/connections/{1|2}/files
    // OR POST /api/bff/drive/connections/{1|2}/folders/{childId}/files
    const isRootUpload =
      segments.length === 3 && segments[0] === "connections" && segments[2] === "files";
    const isChildUpload =
      segments.length === 5 &&
      segments[0] === "connections" &&
      segments[2] === "folders" &&
      segments[4] === "files";

    if (isRootUpload || isChildUpload) {
      const slot = parseSlot(segments[1]!);
      const childId = isChildUpload ? segments[3]! : null;
      if (childId) {
        validateSafeId(childId, "childId");
      }
      const key = req.headers.get("Idempotency-Key");
      if (!key)
        throw new BffException("UPLOAD_KEY_REQUIRED", 400, "Idempotency-Key header is required");

      const contentType = req.headers.get("content-type");
      if (!contentType || !contentType.startsWith("multipart/form-data")) {
        throw new BffException("DRIVE_INVALID_FILE_INPUT", 400, "Multipart content-type required");
      }

      const contentLengthHeader = req.headers.get("content-length");
      if (contentLengthHeader) {
        const cl = parseInt(contentLengthHeader, 10);
        if (!Number.isNaN(cl) && cl > serverConfig.maxMultipartBytes) {
          throw new BffException("DRIVE_PAYLOAD_TOO_LARGE", 413, "Upload exceeds 55 MiB limit");
        }
      }

      const scope = createTransferScope(record, req.signal);
      try {
        const accessToken = await ensureFreshAccessToken(record);
        const springUrl = childId
          ? `${serverConfig.springOrigin}/api/v1/drive/connections/${encodeURIComponent(String(slot))}/folders/${encodeURIComponent(childId)}/files`
          : `${serverConfig.springOrigin}/api/v1/drive/connections/${encodeURIComponent(String(slot))}/files`;

        const { stream: uploadLimitStream } = createByteLimitingStream(
          serverConfig.maxMultipartBytes,
          scope.touch
        );
        const pipedUploadBody = req.body
          ? req.body.pipeThrough(uploadLimitStream, { signal: scope.signal })
          : null;

        scope.startBody();
        const springRes = await fetch(springUrl, {
          redirect: "error",
          signal: scope.signal,
          method: "POST",
          headers: {
            Authorization: `Bearer ${accessToken}`,
            "Idempotency-Key": key,
            "Content-Type": contentType,
            Host: getSpringHost(),
            "Cache-Control": "no-store",
          },
          // @ts-expect-error Node fetch supports duplex: 'half' for streaming request bodies
          duplex: "half",
          body: pipedUploadBody,
          cache: "no-store",
        });

        const json = await readBoundedResponseJson<{ data?: unknown }>(springRes);
        if (!springRes.ok) throw trustedUpstreamError(json, springRes.status);
        if (springRes.status !== 200 && springRes.status !== 201) {
          throw new BffException("UPSTREAM_INVALID_RESPONSE", 502);
        }
        scope.checkSession();
        if (scope.signal.aborted) throw scope.signal.reason;
        const data = validateUploadedFile(json.data);
        scope.close();
        return NextResponse.json(
          { data },
          {
            status: springRes.status,
            headers: { "Cache-Control": "private, no-store" },
          }
        );
      } catch (err) {
        scope.abort();
        throw err;
      }
    }

    throw new BffException("NOT_FOUND", 404, "Unknown Drive endpoint");
  } catch (err) {
    return handleServerException(err);
  }
}

export async function DELETE(req: NextRequest, { params }: RouteContext) {
  try {
    validateAuthority(req, true);
    const { record } = requireAuthenticatedSession(req);
    validateMutationCsrf(req, record);
    const { segments } = await params;

    // DELETE /api/bff/drive/connections/{1|2}
    if (segments.length === 2 && segments[0] === "connections") {
      const slot = parseSlot(segments[1]!);
      const accessToken = await ensureFreshAccessToken(record);
      const data = await springDisconnectConnection(accessToken, slot);
      const res = NextResponse.json(
        { data },
        { headers: { "Cache-Control": "private, no-store" } }
      );

      // Clear binding cookie for this slot
      const cookieName =
        slot === 1 ? serverConfig.driveBinding1Cookie : serverConfig.driveBinding2Cookie;
      res.cookies.set({
        name: cookieName,
        value: "",
        path: "/api/v1/drive/oauth/callback",
        httpOnly: true,
        sameSite: "lax",
        secure: false,
        maxAge: 0,
      });

      return res;
    }

    throw new BffException("NOT_FOUND", 404, "Unknown Drive endpoint");
  } catch (err) {
    return handleServerException(err);
  }
}

import "server-only";
import { BffException } from "./bff-errors";
import { type SessionRecord } from "./session-store";
import { assertActiveSession } from "./route-guards";
import { validateAuthorizationUrl } from "./auth-client";
import { fetchSpringJson } from "./streaming";
import type {
  DriveConnectionDto,
  DriveConnectionsResponse,
  DriveAuthorizeResponse,
  DriveDisconnectResponse,
  DriveRootFolderResponse,
  DriveItemDto,
  DriveItemPageDto,
  DriveFolderDto,
  DriveFileDto,
  StorageQuotaDto,
} from "../contracts";

function invalid(): never {
  throw new BffException("UPSTREAM_INVALID_RESPONSE", 502);
}
function object(value: unknown): Record<string, unknown> {
  if (!value || typeof value !== "object" || Array.isArray(value)) return invalid();
  return value as Record<string, unknown>;
}
function text(value: unknown, max = 1024): string {
  if (
    typeof value !== "string" ||
    !value ||
    value.length > max ||
    [...value].some((c) => c.charCodeAt(0) < 32 || c.charCodeAt(0) === 127)
  )
    return invalid();
  return value;
}
function nullableText(value: unknown): string | null {
  return value === null ? null : text(value);
}
function bytes(value: unknown): number {
  if (typeof value !== "number" || !Number.isSafeInteger(value) || value < 0) return invalid();
  return value;
}
function nullableBytes(value: unknown): number | null {
  return value === null ? null : bytes(value);
}
function timestamp(value: unknown): string {
  const result = text(value, 64);
  if (
    !/^\d{4}-\d\d-\d\dT.*(?:Z|[+-]\d\d:\d\d)$/.test(result) ||
    !Number.isFinite(Date.parse(result))
  )
    return invalid();
  return result;
}
function id(value: unknown): string {
  const result = text(value, 128);
  if (!/^[A-Za-z0-9_-]+$/.test(result)) return invalid();
  return result;
}
function connection(value: unknown): DriveConnectionDto {
  const c = object(value);
  if (
    (c.slot !== 1 && c.slot !== 2) ||
    !["DISCONNECTED", "CONNECTED", "RECONNECT_REQUIRED"].includes(String(c.status))
  )
    return invalid();
  return {
    slot: c.slot,
    status: c.status as DriveConnectionDto["status"],
    displayEmail: nullableText(c.displayEmail),
    remoteRevocation: nullableText(c.remoteRevocation),
  };
}
function item(value: unknown): DriveItemDto {
  const v = object(value);
  if (v.kind !== "file" && v.kind !== "folder") return invalid();
  return {
    id: id(v.id),
    name: text(v.name),
    kind: v.kind,
    mimeType: text(v.mimeType, 255),
    sizeBytes: nullableBytes(v.sizeBytes),
    modifiedAt: v.modifiedAt === null ? null : timestamp(v.modifiedAt),
    parentId: v.parentId === null ? null : id(v.parentId),
  };
}
export function validateUploadedFile(value: unknown): DriveFileDto {
  const v = object(value);
  return {
    id: id(v.id),
    name: text(v.name),
    mimeType: text(v.mimeType, 255),
    sizeBytes: bytes(v.sizeBytes),
    modifiedAt: timestamp(v.modifiedAt),
  };
}
function request(accessToken: string, path: string, method = "GET", body?: unknown, key?: string) {
  return fetchSpringJson(`/api/v1/drive${path}`, {
    method,
    headers: {
      Authorization: `Bearer ${accessToken}`,
      ...(body === undefined ? {} : { "Content-Type": "application/json" }),
      ...(key ? { "Idempotency-Key": key } : {}),
    },
    ...(body === undefined ? {} : { body: JSON.stringify(body) }),
  });
}
export async function springGetConnections(accessToken: string): Promise<DriveConnectionsResponse> {
  const { data } = await request(accessToken, "/connections");
  if (!Array.isArray(data.connections) || data.connections.length !== 2) return invalid();
  const connections = data.connections.map(connection);
  if (new Set(connections.map((c) => c.slot)).size !== 2) return invalid();
  return { connections };
}
export async function springAuthorizeConnection(
  accessToken: string,
  slot: 1 | 2,
  record?: SessionRecord
): Promise<{ data: DriveAuthorizeResponse; setCookieHeaders: string[] }> {
  const generation = record?.generation;
  const { data, response } = await request(accessToken, `/connections/${slot}/authorize`, "POST");
  if (record && generation !== undefined) assertActiveSession(record, generation, "AUTHENTICATED");
  if (data.slot !== slot) return invalid();
  const authorizationUrl = text(data.authorizationUrl, 8192);
  validateAuthorizationUrl(authorizationUrl, "/api/v1/drive/oauth/callback");
  return {
    data: { slot, authorizationUrl, expiresAt: timestamp(data.expiresAt) },
    setCookieHeaders: response.headers.getSetCookie(),
  };
}
export async function springDisconnectConnection(
  accessToken: string,
  slot: 1 | 2
): Promise<DriveDisconnectResponse> {
  const { data } = await request(accessToken, `/connections/${slot}`, "DELETE");
  if (data.slot !== slot || data.status !== "DISCONNECTED") return invalid();
  return { slot, status: "DISCONNECTED", remoteRevocation: nullableText(data.remoteRevocation) };
}
export async function springEnsureRootFolder(
  accessToken: string,
  slot: 1 | 2
): Promise<DriveRootFolderResponse> {
  const { data } = await request(accessToken, `/connections/${slot}/root-folder`, "POST");
  if (data.slot !== slot || data.status !== "READY") return invalid();
  return { slot, status: "READY" };
}
export async function springGetStorageQuota(
  accessToken: string,
  slot: 1 | 2
): Promise<StorageQuotaDto> {
  const { data } = await request(accessToken, `/connections/${slot}/storage-quota`);
  if (typeof data.limitKnown !== "boolean") return invalid();
  const limitBytes = nullableBytes(data.limitBytes);
  if (data.limitKnown !== (limitBytes !== null)) return invalid();
  return {
    usageBytes: nullableBytes(data.usageBytes),
    usageInDriveBytes: nullableBytes(data.usageInDriveBytes),
    limitBytes,
    limitKnown: data.limitKnown,
    observedAt: data.observedAt === null ? null : timestamp(data.observedAt),
  };
}
export async function springListItems(
  accessToken: string,
  slot: 1 | 2,
  searchParams: URLSearchParams
): Promise<DriveItemPageDto> {
  const { data } = await request(accessToken, `/connections/${slot}/items?${searchParams}`);
  if (!Array.isArray(data.items) || data.items.length > 50 || typeof data.hasMore !== "boolean")
    return invalid();
  const nextCursor = data.nextCursor === null ? null : text(data.nextCursor, 43);
  if (nextCursor !== null && !/^[A-Za-z0-9_-]{43}$/.test(nextCursor)) return invalid();
  if (data.hasMore !== (nextCursor !== null)) return invalid();
  return { items: data.items.map(item), nextCursor, hasMore: data.hasMore };
}
export async function springCreateFolder(
  accessToken: string,
  slot: 1 | 2,
  idempotencyKey: string,
  name: string
): Promise<{ folder: DriveFolderDto; status: number }> {
  const { data, response } = await request(
    accessToken,
    `/connections/${slot}/folders`,
    "POST",
    { name },
    idempotencyKey
  );
  if ((response.status !== 200 && response.status !== 201) || data.kind !== "folder")
    return invalid();
  return {
    folder: {
      id: id(data.id),
      name: text(data.name),
      parentId: id(data.parentId),
      kind: "folder",
      modifiedAt: timestamp(data.modifiedAt),
    },
    status: response.status,
  };
}

/**
 * Common wire contracts, session types and DTO definitions for PPV BFF and Client.
 */

export type SessionPurpose = "ANONYMOUS" | "SETUP_ONLY" | "AUTHENTICATED";

export interface SessionStatusDto {
  authenticated: boolean;
  purpose: SessionPurpose;
  csrfToken: string;
  idleExpiresInSeconds: number;
  absoluteExpiresInSeconds: number;
}

export interface ApiError {
  code: string;
  message: string;
  details?: Record<string, unknown>;
}

export interface ApiResponse<T> {
  data: T;
  error?: ApiError | null;
  meta?: Record<string, unknown> | null;
}

export type DriveConnectionStatus = "DISCONNECTED" | "CONNECTED" | "RECONNECT_REQUIRED";

export interface DriveConnectionDto {
  slot: 1 | 2;
  status: DriveConnectionStatus;
  displayEmail: string | null;
  remoteRevocation: string | null;
}

export interface DriveConnectionsResponse {
  connections: DriveConnectionDto[];
}

export interface DriveAuthorizeResponse {
  slot: number;
  authorizationUrl: string;
  expiresAt: string;
}

export interface DriveDisconnectResponse {
  slot: number;
  status: DriveConnectionStatus;
  remoteRevocation: string | null;
}

export interface DriveRootFolderResponse {
  slot: number;
  status: "READY" | string;
}

export type DriveItemKind = "file" | "folder";

export interface DriveItemDto {
  id: string;
  name: string;
  kind: DriveItemKind;
  mimeType: string;
  sizeBytes: number | null;
  modifiedAt: string | null;
  parentId: string | null;
}

export interface DriveItemPageDto {
  items: DriveItemDto[];
  nextCursor: string | null;
  hasMore: boolean;
}

export interface DriveFolderDto {
  id: string;
  name: string;
  parentId: string;
  kind: "folder";
  modifiedAt: string;
}

export interface DriveFileDto {
  id: string;
  name: string;
  mimeType: string;
  sizeBytes: number;
  modifiedAt: string;
}

export interface StorageQuotaDto {
  usageBytes: number | null;
  usageInDriveBytes: number | null;
  limitBytes: number | null;
  limitKnown: boolean;
  observedAt: string | null;
}

export type DriveItemTypeFilter =
  "all" | "folder" | "image" | "pdf" | "document" | "text" | "other";

export interface DriveSearchFilter {
  name?: string;
  type?: DriveItemTypeFilter;
  from?: string; // YYYY-MM-DD
  to?: string; // YYYY-MM-DD
}

export interface BffErrorResponse {
  code: string;
  message: string;
}

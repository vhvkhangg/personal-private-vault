import "server-only";
import crypto from "node:crypto";
import { serverConfig } from "./config";
import type { SessionPurpose } from "../contracts";
export type { SessionPurpose };
import { BffException } from "./bff-errors";

export interface SessionRecord {
  idDigest: string;
  purpose: SessionPurpose;
  createdAt: number;
  lastAccessedAt: number;
  idleDeadline: number;
  absoluteDeadline: number;
  generation: number;
  csrfSecret: string;
  csrfToken: string;
  csrfTokenDigest: string;
  startSeq?: number;
  handoffVerifier?: string;
  attemptId?: string;
  accessToken?: string;
  refreshToken?: string;
  accessExpiresAt?: number;
  isRevoking?: boolean;
}

// Fixed process HMAC secret shared across all Next.js compilation chunks via globalThis
const GLOBAL_STORE_KEY = Symbol.for("ppv.session.store");
const GLOBAL_HMAC_KEY = Symbol.for("ppv.session.hmac");

type GlobalSessionScope = typeof globalThis & {
  [GLOBAL_STORE_KEY]?: SessionStore;
  [GLOBAL_HMAC_KEY]?: Buffer;
};

const gScope = globalThis as GlobalSessionScope;

if (!gScope[GLOBAL_HMAC_KEY]) {
  gScope[GLOBAL_HMAC_KEY] = crypto.randomBytes(32);
}
const PROCESS_HMAC_KEY: Buffer = gScope[GLOBAL_HMAC_KEY]!;

function hashSid(sid: string): string {
  return crypto.createHmac("sha256", PROCESS_HMAC_KEY).update(sid).digest("hex");
}

class SessionStore {
  private sessions = new Map<string, SessionRecord>();
  private invalidationListeners = new Map<string, Set<() => void>>();
  private refreshPromises = new Map<string, Promise<{ success: boolean; accessToken?: string }>>();

  public watchInvalidation(record: SessionRecord, cancel: () => void): () => void {
    const listeners = this.invalidationListeners.get(record.idDigest) ?? new Set<() => void>();
    listeners.add(cancel);
    this.invalidationListeners.set(record.idDigest, listeners);
    return () => {
      listeners.delete(cancel);
      if (!listeners.size) this.invalidationListeners.delete(record.idDigest);
    };
  }

  private cancelWork(idDigest: string): void {
    const listeners = this.invalidationListeners.get(idDigest);
    this.invalidationListeners.delete(idDigest);
    for (const cancel of listeners ?? []) cancel();
  }

  public getCount(): number {
    this.purgeExpired();
    return this.sessions.size;
  }

  public purgeExpired(now = Date.now()): void {
    for (const [digest, record] of this.sessions.entries()) {
      if (now >= record.idleDeadline || now >= record.absoluteDeadline || record.isRevoking) {
        this.sessions.delete(digest);
        this.cancelWork(digest);
      }
    }
  }

  public createSession(purpose: SessionPurpose): {
    sid: string;
    record: SessionRecord;
    csrfToken: string;
  } {
    this.purgeExpired();
    if (this.sessions.size >= serverConfig.maxSessions) {
      throw new BffException("SESSION_CAPACITY_EXHAUSTED", 429, "Too many active sessions");
    }

    const sidBytes = crypto.randomBytes(32);
    const sid = sidBytes.toString("base64url");
    const idDigest = hashSid(sid);

    const csrfSecret = crypto.randomBytes(32).toString("base64url");
    const csrfToken = crypto.randomBytes(32).toString("base64url");
    const csrfTokenDigest = crypto.createHash("sha256").update(csrfToken).digest("hex");

    const now = Date.now();
    const idleMs =
      purpose === "AUTHENTICATED" ? serverConfig.authenticatedIdleMs : serverConfig.anonymousIdleMs;
    const absMs =
      purpose === "AUTHENTICATED"
        ? serverConfig.authenticatedAbsoluteMs
        : serverConfig.anonymousAbsoluteMs;

    const record: SessionRecord = {
      idDigest,
      purpose,
      createdAt: now,
      lastAccessedAt: now,
      idleDeadline: now + idleMs,
      absoluteDeadline: now + absMs,
      generation: 1,
      csrfSecret,
      csrfToken,
      csrfTokenDigest,
    };

    this.sessions.set(idDigest, record);
    return { sid, record, csrfToken };
  }

  public getSession(sid: string | undefined): SessionRecord | null {
    if (!sid || typeof sid !== "string" || sid.length < 32 || sid.length > 64) {
      return null;
    }
    const idDigest = hashSid(sid);
    const record = this.sessions.get(idDigest);
    if (!record) {
      return null;
    }

    const now = Date.now();
    if (now >= record.idleDeadline || now >= record.absoluteDeadline || record.isRevoking) {
      this.sessions.delete(idDigest);
      this.cancelWork(idDigest);
      return null;
    }

    // Touch idle deadline on admission
    const idleMs =
      record.purpose === "AUTHENTICATED"
        ? serverConfig.authenticatedIdleMs
        : serverConfig.anonymousIdleMs;
    record.lastAccessedAt = now;
    record.idleDeadline = Math.min(now + idleMs, record.absoluteDeadline);

    return record;
  }

  public getSessionByIdDigest(idDigest: string | undefined): SessionRecord | null {
    if (!idDigest) {
      return null;
    }
    const record = this.sessions.get(idDigest);
    if (!record) {
      return null;
    }

    const now = Date.now();
    if (now >= record.idleDeadline || now >= record.absoluteDeadline || record.isRevoking) {
      this.sessions.delete(idDigest);
      this.cancelWork(idDigest);
      return null;
    }

    return record;
  }

  public rotateSession(
    oldSid: string,
    newPurpose: SessionPurpose,
    newTokens?: { accessToken: string; refreshToken: string; expiresIn: number }
  ): { sid: string; record: SessionRecord; csrfToken: string } {
    const oldRecord = this.getSession(oldSid);
    if (!oldRecord || oldRecord.isRevoking) {
      throw new BffException(
        "SESSION_INVALIDATED",
        401,
        "Predecessor session expired or invalidated"
      );
    }

    oldRecord.isRevoking = true;
    oldRecord.generation++;
    this.sessions.delete(oldRecord.idDigest);
    this.cancelWork(oldRecord.idDigest);

    const created = this.createSession(newPurpose);
    if (newTokens) {
      created.record.accessToken = newTokens.accessToken;
      created.record.refreshToken = newTokens.refreshToken;
      created.record.accessExpiresAt = Date.now() + newTokens.expiresIn * 1000;
    }
    return created;
  }

  public invalidate(sid: string | undefined): SessionRecord | null {
    if (!sid) {
      return null;
    }
    const idDigest = hashSid(sid);
    return this.invalidateByIdDigest(idDigest);
  }

  public invalidateByIdDigest(idDigest: string | undefined): SessionRecord | null {
    if (!idDigest) {
      return null;
    }
    const record = this.sessions.get(idDigest);
    if (record) {
      record.isRevoking = true;
      record.generation++;
      this.sessions.delete(idDigest);
      this.cancelWork(idDigest);
      return record;
    }
    return null;
  }

  public resetAll(): void {
    for (const digest of this.sessions.keys()) this.cancelWork(digest);
    this.sessions.clear();
    this.refreshPromises.clear();
  }

  public getRefreshPromise(
    key: string
  ): Promise<{ success: boolean; accessToken?: string }> | undefined {
    return this.refreshPromises.get(key);
  }

  public setRefreshPromise(
    key: string,
    promise: Promise<{ success: boolean; accessToken?: string }>
  ): void {
    this.refreshPromises.set(key, promise);
  }

  public clearRefreshPromise(key: string): void {
    this.refreshPromises.delete(key);
  }
}

if (!gScope[GLOBAL_STORE_KEY]) {
  gScope[GLOBAL_STORE_KEY] = new SessionStore();
}

// Global process singleton (shared across Next.js RSC and Route Handler bundles)
export const sessionStore = gScope[GLOBAL_STORE_KEY]!;

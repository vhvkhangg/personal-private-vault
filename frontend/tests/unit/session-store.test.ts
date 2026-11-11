import { describe, it, expect, beforeEach } from "vitest";
import { sessionStore } from "@/lib/server/session-store";
import { serverConfig } from "@/lib/server/config";

describe("sessionStore", () => {
  beforeEach(() => {
    sessionStore.resetAll();
  });

  it("creates a session and retrieves it by sid", () => {
    const { sid, record, csrfToken } = sessionStore.createSession("ANONYMOUS");
    expect(sid).toBeDefined();
    expect(sid.length).toBeGreaterThanOrEqual(43);
    expect(record.purpose).toBe("ANONYMOUS");
    expect(record.generation).toBe(1);
    expect(csrfToken).toBeDefined();

    const retrieved = sessionStore.getSession(sid);
    expect(retrieved).not.toBeNull();
    expect(retrieved?.idDigest).toBe(record.idDigest);
  });

  it.each(["ANONYMOUS", "SETUP_ONLY", "AUTHENTICATED"] as const)(
    "rejects a ninth session without mutating live %s records",
    (purpose) => {
      const records = Array.from({ length: serverConfig.maxSessions }, () =>
        sessionStore.createSession(purpose)
      );
      records.forEach(({ record }, i) => {
        record.attemptId = `attempt-${i}`;
        record.handoffVerifier = `proof-${i}`;
      });
      const before = records.map(({ record }) => structuredClone(record));
      expect(() => sessionStore.createSession("ANONYMOUS")).toThrow(
        expect.objectContaining({ code: "SESSION_CAPACITY_EXHAUSTED", httpStatus: 429 })
      );
      expect(sessionStore.getCount()).toBe(8);
      records.forEach(({ record }, i) => {
        expect(sessionStore.getSessionByIdDigest(record.idDigest)).toEqual(before[i]);
      });
    }
  );

  it("reclaims expired records before enforcing capacity", () => {
    const records = Array.from({ length: 8 }, () => sessionStore.createSession("ANONYMOUS"));
    records[0]!.record.idleDeadline = Date.now() - 1;
    expect(sessionStore.createSession("ANONYMOUS").record.purpose).toBe("ANONYMOUS");
    expect(sessionStore.getCount()).toBe(8);
  });

  it("expires session when idle deadline has passed", () => {
    const { sid, record } = sessionStore.createSession("ANONYMOUS");
    record.idleDeadline = Date.now() - 1000;

    const retrieved = sessionStore.getSession(sid);
    expect(retrieved).toBeNull();
    expect(sessionStore.getCount()).toBe(0);
  });

  it("expires session when absolute deadline has passed", () => {
    const { sid, record } = sessionStore.createSession("AUTHENTICATED");
    record.absoluteDeadline = Date.now() - 1000;

    const retrieved = sessionStore.getSession(sid);
    expect(retrieved).toBeNull();
    expect(sessionStore.getCount()).toBe(0);
  });

  it("rotates session to a new SID and invalidates the old one", () => {
    const { sid: oldSid } = sessionStore.createSession("ANONYMOUS");
    const { sid: newSid, record: newRecord } = sessionStore.rotateSession(oldSid, "AUTHENTICATED", {
      accessToken: "mock.jwt.token",
      refreshToken: "opaque-refresh-123",
      expiresIn: 3600,
    });

    expect(newSid).not.toBe(oldSid);
    expect(newRecord.purpose).toBe("AUTHENTICATED");
    expect(newRecord.accessToken).toBe("mock.jwt.token");
    expect(newRecord.refreshToken).toBe("opaque-refresh-123");

    // Old SID must be invalid
    expect(sessionStore.getSession(oldSid)).toBeNull();
    // New SID must be valid
    expect(sessionStore.getSession(newSid)).not.toBeNull();
  });

  it("invalidates session synchronously and advances generation", () => {
    const { sid, record } = sessionStore.createSession("AUTHENTICATED");
    const genBefore = record.generation;

    sessionStore.invalidate(sid);
    expect(record.generation).toBe(genBefore + 1);
    expect(record.isRevoking).toBe(true);
    expect(sessionStore.getSession(sid)).toBeNull();
  });
});

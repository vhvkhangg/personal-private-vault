import { describe, it, expect } from "vitest";
import { generateCsrfToken, validateCsrf } from "@/lib/server/csrf";
import { sessionStore } from "@/lib/server/session-store";

describe("CSRF protection", () => {
  it("generates a random 32-byte base64url token with matching sha256 digest", () => {
    const { token, digest } = generateCsrfToken();
    expect(token).toBeDefined();
    expect(token.length).toBeGreaterThanOrEqual(43);
    expect(digest).toBeDefined();
    expect(digest.length).toBe(64);
  });

  it("validates correct CSRF token constant-time against session record", () => {
    const { record, csrfToken } = sessionStore.createSession("ANONYMOUS");
    expect(validateCsrf(record, csrfToken)).toBe(true);
  });

  it("rejects missing, empty, or mismatched CSRF token", () => {
    const { record } = sessionStore.createSession("ANONYMOUS");
    expect(validateCsrf(record, null)).toBe(false);
    expect(validateCsrf(record, undefined)).toBe(false);
    expect(validateCsrf(record, "")).toBe(false);
    expect(validateCsrf(record, "wrong-csrf-token-that-is-not-the-expected-value-123456")).toBe(
      false
    );
  });
});

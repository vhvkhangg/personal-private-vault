import "server-only";
import crypto from "node:crypto";
import type { SessionRecord } from "./session-store";

export function generateCsrfToken(): { token: string; digest: string } {
  const token = crypto.randomBytes(32).toString("base64url");
  const digest = crypto.createHash("sha256").update(token).digest("hex");
  return { token, digest };
}

export function validateCsrf(
  record: SessionRecord,
  submittedToken: string | null | undefined
): boolean {
  if (
    !submittedToken ||
    typeof submittedToken !== "string" ||
    submittedToken.length < 32 ||
    submittedToken.length > 64
  ) {
    return false;
  }

  const submittedDigest = crypto.createHash("sha256").update(submittedToken).digest("hex");
  const expectedBuf = Buffer.from(record.csrfTokenDigest, "utf8");
  const submittedBuf = Buffer.from(submittedDigest, "utf8");

  if (expectedBuf.length !== submittedBuf.length) {
    return false;
  }

  return crypto.timingSafeEqual(expectedBuf, submittedBuf);
}

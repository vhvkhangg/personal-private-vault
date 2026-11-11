import "server-only";
import { serverConfig } from "./config";
import { sessionStore, type SessionRecord } from "./session-store";
import { BffException } from "./bff-errors";
import { fetchSpringJson } from "./streaming";

export function validateAuthorizationUrl(
  authUrl: string,
  expectedCallbackPath: "/api/v1/auth/google/callback" | "/api/v1/drive/oauth/callback"
): void {
  try {
    if (
      typeof authUrl !== "string" ||
      authUrl.length > 8192 ||
      !authUrl.startsWith("https://accounts.google.com/o/oauth2/v2/auth?") ||
      authUrl.includes("#")
    )
      throw new Error();
    const parsed = new URL(authUrl);
    if (
      parsed.protocol !== "https:" ||
      parsed.host !== "accounts.google.com" ||
      parsed.username ||
      parsed.password ||
      parsed.hash ||
      parsed.pathname !== "/o/oauth2/v2/auth"
    )
      throw new Error();
    const single = (key: string) => {
      const values = parsed.searchParams.getAll(key);
      if (values.length !== 1 || !values[0]) throw new Error();
      return values[0];
    };
    if (single("response_type") !== "code") throw new Error();
    const expectedClient =
      expectedCallbackPath === "/api/v1/auth/google/callback"
        ? serverConfig.googleSignInClientId
        : serverConfig.driveClientId;
    if (!expectedClient || single("client_id") !== expectedClient) throw new Error();
    const redirect = new URL(single("redirect_uri"));
    if (redirect.href !== `${serverConfig.springOrigin}${expectedCallbackPath}`) throw new Error();
    // These public protocol fields are required, never accepted duplicated.
    if (
      !/^[A-Za-z0-9_-]{43}$/.test(single("state")) ||
      !/^[A-Za-z0-9_-]{43}$/.test(single("nonce")) ||
      !/^[A-Za-z0-9_-]{43}$/.test(single("code_challenge")) ||
      single("code_challenge_method") !== "S256"
    )
      throw new Error();
    const scopes = single("scope").split(" ");
    const common = [
      "response_type",
      "client_id",
      "redirect_uri",
      "scope",
      "state",
      "nonce",
      "code_challenge",
      "code_challenge_method",
    ];
    if (expectedCallbackPath === "/api/v1/auth/google/callback") {
      if (scopes.length !== 2 || !scopes.includes("openid") || !scopes.includes("email"))
        throw new Error();
    } else {
      common.push("access_type", "prompt");
      if (
        scopes.length !== 3 ||
        !scopes.includes("openid") ||
        !scopes.includes("email") ||
        !scopes.includes("https://www.googleapis.com/auth/drive.file") ||
        single("access_type") !== "offline" ||
        single("prompt") !== "consent select_account"
      )
        throw new Error();
    }
    if ([...parsed.searchParams.keys()].some((key) => !common.includes(key))) throw new Error();
  } catch {
    throw new BffException(
      "GOOGLE_SIGNIN_FAILURE",
      502,
      "Invalid authorization URL returned from upstream"
    );
  }
}

function startDto(data: Record<string, unknown>) {
  const { attemptId, authorizationUrl, expiresIn } = data;
  if (
    typeof attemptId !== "string" ||
    !/^[A-Za-z0-9_-]{1,128}$/.test(attemptId) ||
    typeof authorizationUrl !== "string" ||
    !Number.isSafeInteger(expiresIn) ||
    typeof expiresIn !== "number" ||
    expiresIn <= 0 ||
    expiresIn > 1200
  ) {
    throw new BffException("UPSTREAM_INVALID_RESPONSE", 502);
  }
  validateAuthorizationUrl(authorizationUrl, "/api/v1/auth/google/callback");
  return { attemptId, authorizationUrl, expiresIn };
}

function tokenDto(data: Record<string, unknown>) {
  const { accessToken, refreshToken, tokenType, expiresIn } = data;
  if (
    typeof accessToken !== "string" ||
    accessToken.length > 8192 ||
    !/^[A-Za-z0-9_-]+\.[A-Za-z0-9_-]+\.[A-Za-z0-9_-]+$/.test(accessToken) ||
    typeof refreshToken !== "string" ||
    !/^[A-Za-z0-9_-]{32,4096}$/.test(refreshToken) ||
    tokenType !== "Bearer" ||
    typeof expiresIn !== "number" ||
    !Number.isSafeInteger(expiresIn) ||
    expiresIn <= 0 ||
    expiresIn > 86400
  )
    throw new BffException("UPSTREAM_INVALID_RESPONSE", 502);
  return { accessToken, refreshToken, tokenType, expiresIn };
}

function authPost(path: string, body: unknown, accessToken?: string) {
  return fetchSpringJson(
    path,
    {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        ...(accessToken ? { Authorization: `Bearer ${accessToken}` } : {}),
      },
      body: JSON.stringify(body),
    },
    serverConfig.maxAuthJsonBytes
  );
}

export async function springStartGoogleSignIn(handoffChallenge: string) {
  const { data, response } = await authPost("/api/v1/auth/google/start", { handoffChallenge });
  return { ...startDto(data), setCookieHeaders: response.headers.getSetCookie() };
}

export async function springStartGoogleLink(
  accessToken: string,
  handoffChallenge: string,
  password: string
) {
  const { data, response } = await authPost(
    "/api/v1/auth/google/link/start",
    { handoffChallenge, password },
    accessToken
  );
  return { ...startDto(data), setCookieHeaders: response.headers.getSetCookie() };
}

export async function springRedeemHandoff(ticket: string, handoffVerifier: string) {
  const { data } = await authPost("/api/v1/auth/google/handoff", { ticket, handoffVerifier });
  return tokenDto(data);
}

export async function springLegacyLogin(identifier: string, password: string) {
  const { data } = await authPost("/api/v1/auth/login", { identifier, password });
  return tokenDto(data);
}

export async function springRefreshToken(refreshToken: string) {
  const { data } = await authPost("/api/v1/auth/refresh", { refreshToken });
  return tokenDto(data);
}

export async function springRevokeToken(refreshToken: string): Promise<void> {
  try {
    const response = await fetch(`${serverConfig.springOrigin}/api/v1/auth/revoke`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Host: new URL(serverConfig.springOrigin).host,
      },
      body: JSON.stringify({ refreshToken }),
      redirect: "error",
      cache: "no-store",
      signal: AbortSignal.timeout(5000),
    });
    void response.body?.cancel().catch(() => {});
  } catch {
    /* Best-effort bounded revocation. */
  }
}

/**
 * Ensures access token is valid, executing singleflight refresh if expiring or expired.
 */
export async function ensureFreshAccessToken(record: SessionRecord): Promise<string> {
  if (!record.accessToken || !record.refreshToken) {
    throw new BffException("UNAUTHORIZED", 401, "No authentication tokens in session");
  }

  const now = Date.now();
  // Refresh if less than 30s remaining
  if (record.accessExpiresAt && now < record.accessExpiresAt - 30_000) {
    return record.accessToken;
  }

  const singleflightKey = record.idDigest;
  let inFlight = sessionStore.getRefreshPromise(singleflightKey);
  if (!inFlight) {
    const capturedGeneration = record.generation;
    inFlight = (async () => {
      try {
        const tokens = await springRefreshToken(record.refreshToken!);
        const commitTime = Date.now();
        // Re-check generation fence: commit tokens only if session is still active, unchanged, and not expired
        if (
          !record.isRevoking &&
          record.generation === capturedGeneration &&
          commitTime < record.absoluteDeadline &&
          commitTime < record.idleDeadline
        ) {
          record.accessToken = tokens.accessToken;
          record.refreshToken = tokens.refreshToken;
          record.accessExpiresAt = commitTime + tokens.expiresIn * 1000;
          return { success: true, accessToken: tokens.accessToken };
        } else {
          // Session was invalidated, logged out, or expired during refresh; best-effort revoke the new refresh token
          sessionStore.invalidateByIdDigest(record.idDigest);
          void springRevokeToken(tokens.refreshToken);
          return { success: false };
        }
      } catch {
        // Refresh failed; invalidate session
        sessionStore.invalidateByIdDigest(record.idDigest);
        return { success: false };
      } finally {
        sessionStore.clearRefreshPromise(singleflightKey);
      }
    })();
    sessionStore.setRefreshPromise(singleflightKey, inFlight);
  }

  const result = await inFlight;
  if (!result.success || !record.accessToken) {
    throw new BffException("AUTH_TOKEN_EXPIRED", 401, "Session refresh failed");
  }
  return record.accessToken;
}

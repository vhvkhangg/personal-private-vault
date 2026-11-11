import { expect, type BrowserContext, type Page } from "@playwright/test";
import { NEXT_ORIGIN } from "./data";

export interface SessionInfo {
  sid: string;
  csrfToken: string;
  status: string;
  purpose: string;
}

export async function bootstrapSession(page: Page): Promise<SessionInfo> {
  const res = await page.request.get(`${NEXT_ORIGIN}/api/bff/session`);
  if (!res.ok()) {
    throw new Error(`Failed to bootstrap session: ${res.status()}`);
  }
  const data = (await res.json()) as {
    data: { csrfToken: string; status: string; purpose: string };
  };

  const cookies = await page.context().cookies(NEXT_ORIGIN);
  const sidCookie = cookies.find((c) => c.name === "ppv_dev_sid");
  if (!sidCookie) {
    throw new Error("Missing ppv_dev_sid cookie after bootstrap");
  }

  return {
    sid: sidCookie.value,
    csrfToken: data.data.csrfToken,
    status: data.data.status,
    purpose: data.data.purpose,
  };
}

export async function clearAllCookies(context: BrowserContext): Promise<void> {
  await context.clearCookies();
}

export async function getCookie(
  context: BrowserContext,
  name: string,
  origin = NEXT_ORIGIN
): Promise<string | undefined> {
  const cookies = await context.cookies(origin);
  return cookies.find((c) => c.name === name)?.value;
}

export async function loginAsOwner(
  page: Page,
  context: BrowserContext,
  control: {
    seedOwnerGoogle: (sub: string) => Promise<void>;
    issueGoogleCode: (code: string, nonce: string, sub: string) => Promise<string>;
  }
): Promise<void> {
  await context.clearCookies();
  await control.seedOwnerGoogle("browser-owner-sub-1001");
  await context.route("https://accounts.google.com/**", async (route) => {
    const url = new URL(route.request().url());
    const state = url.searchParams.get("state") || "";
    const nonce = url.searchParams.get("nonce") || "test-nonce";
    const code = `code-${Date.now()}`;
    await control.issueGoogleCode(code, nonce, "browser-owner-sub-1001");
    const callbackUrl = `http://localhost:19080/api/v1/auth/google/callback?state=${encodeURIComponent(state)}&code=${encodeURIComponent(code)}`;
    await route.fulfill({
      status: 302,
      headers: { Location: callbackUrl },
    });
  });
  await page.goto("/login");
  await page.getByRole("button", { name: "Đăng nhập bằng Google" }).click();
  await page.waitForURL("**/personal/storage/google-drive", { timeout: 15000 });
}

/** Negative preparation: real top-level Spring callback, then client-side interruption before Next dispatch.
 * Positive sign-in tests separately exercise the uninterrupted redirect chain. No response/cookie is mocked.
 */
export async function holdLiveTicket(
  page: Page,
  context: BrowserContext,
  control: { issueGoogleCode: (code: string, nonce: string, sub: string) => Promise<string> }
) {
  await page.goto("/login");
  await expect(page.getByRole("button", { name: "Đăng nhập bằng Google" })).toBeEnabled();
  const url = await page.evaluate(async () => {
    const session = await (await fetch("/api/bff/session")).json();
    const start = await fetch("/api/bff/auth/google/start", {
      method: "POST",
      headers: { "X-PPV-CSRF": session.csrfToken },
    });
    if (!start.ok) throw new Error("Native start rejected");
    return (await start.json()).data.authorizationUrl as string;
  });
  const auth = new URL(url);
  const code = crypto.randomUUID();
  await control.issueGoogleCode(code, auth.searchParams.get("nonce")!, "browser-owner-sub-1001");
  const callback = new URL(auth.searchParams.get("redirect_uri")!);
  expect(callback.origin).toBe("http://localhost:19080");
  callback.searchParams.set("state", auth.searchParams.get("state")!);
  callback.searchParams.set("code", code);
  const cdp = await context.newCDPSession(page);
  let stopped = false;
  await cdp.send("Fetch.enable", {
    patterns: [
      {
        urlPattern: "http://localhost:3000/api/bff/auth/google/complete",
        requestStage: "Request",
      },
    ],
  });
  cdp.on("Fetch.requestPaused", async (event) => {
    stopped = true;
    await cdp.send("Fetch.failRequest", { requestId: event.requestId, errorReason: "Aborted" });
  });
  try {
    await page.goto(callback.href);
  } catch {
    /* Intentional native completion interruption. */
  }
  await cdp.send("Fetch.disable");
  await cdp.detach();
  expect(stopped).toBe(true);
  const ticket = (await context.cookies()).find((c) => c.name === "ppv_google_handoff");
  if (!ticket) throw new Error("Real Spring ticket was not issued");
  await page.goto("/login");
  return ticket;
}

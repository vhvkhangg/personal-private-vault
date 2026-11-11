import { expect, test } from "@playwright/test";
import http from "node:http";
import { getControlClient } from "../fixtures/spring-fixture";
import { holdLiveTicket } from "../fixtures/transport";

test("F-01: actual Next hostile HTTP authority and unauthenticated route inventory deny before Spring", async ({
  page,
  context,
}, info) => {
  const control = getControlClient();
  await control.reset();
  await control.seedOwnerGoogle();
  await page.goto("/login");
  await expect(page.getByRole("button", { name: "Đăng nhập bằng Google" })).toBeEnabled();
  const csrf = await page.evaluate(
    async () => (await (await fetch("/api/bff/session")).json()).csrfToken as string
  );
  const sid = (await context.cookies()).find((c) => c.name === "ppv_dev_sid")!;
  const before = await control.getStats();
  const base = {
    Host: "localhost:3000",
    Origin: "http://localhost:3000",
    "Sec-Fetch-Site": "same-origin",
    "X-PPV-CSRF": csrf,
    Cookie: sid.name + "=" + sid.value,
  };
  // Next itself supplies canonical forwarded values. Preserve the independently accepted
  // fixed-authority guard; probe conflicting authority rather than indistinguishable defaults.
  const variants: Record<string, string>[] = [
    { Host: "127.0.0.1:3000" },
    { Origin: "http://foreign.invalid" },
    { Origin: "http://localhost:3000, http://localhost:3000" },
    { "X-Forwarded-Host": "foreign.invalid" },
    { "X-Forwarded-Proto": "https" },
    { "X-Forwarded-Port": "8443" },
    { "Sec-Fetch-Site": "cross-site" },
  ];
  const hostile = [];
  const direct = (path: string, method: string, headers: Record<string, string>) =>
    new Promise<number>((resolve, reject) => {
      const request = http.request(
        "http://localhost:3000" + path,
        { method, headers },
        (response) => {
          response.resume();
          resolve(response.statusCode!);
        }
      );
      request.on("error", reject);
      request.setTimeout(5000, () => request.destroy(new Error("Direct guard probe timed out")));
      request.end();
    });
  for (const variant of variants) {
    const status = await direct("/api/bff/auth/google/start", "POST", { ...base, ...variant });
    expect([400, 403], "Rejected hostile " + Object.keys(variant)[0]).toContain(status);
    hostile.push(status);
  }
  const inventory: [string, string][] = [
    ["GET", "connections"],
    ["GET", "connections/1/items?scope=parent&parentId=root"],
    ["GET", "connections/1/storage-quota"],
    ["GET", "connections/1/files/file/thumbnail"],
    ["GET", "connections/1/files/file/download"],
    ["POST", "connections/1/authorize"],
    ["DELETE", "connections/1"],
    ["POST", "connections/1/root-folder"],
    ["POST", "connections/1/folders"],
    ["POST", "connections/1/files"],
    ["POST", "connections/1/folders/child/files"],
  ];
  const denied = [];
  for (const [method, path] of inventory) {
    const status = await direct("/api/bff/drive/" + path, method, {
      Host: base.Host,
      Origin: base.Origin,
      "Sec-Fetch-Site": "same-origin",
    });
    expect(status).toBe(401);
    denied.push(status);
  }
  expect((await control.getStats()).springRequests).toBe(before.springRequests);
  await info.attach("actual-next-http-guards", {
    body: JSON.stringify({
      hostile,
      unauthenticated: denied,
      upstreamDispatches: 0,
      cookieProofSubstitute: false,
    }),
    contentType: "application/json",
  });
});

test("F-01: actual native mutation CSRF negatives stop before Spring", async ({ page }, info) => {
  const control = getControlClient();
  await control.reset();
  await control.seedOwnerGoogle();
  await page.goto("/login");
  await expect(page.getByRole("button", { name: "Đăng nhập bằng Google" })).toBeEnabled();
  const before = await control.getStats();
  const results = await page.evaluate(async () => {
    const csrf = (await (await fetch("/api/bff/session")).json()).csrfToken;
    const rows = [];
    for (const headers of [{}, { "X-PPV-CSRF": "wrong" }, { "X-PPV-CSRF": csrf + ", " + csrf }]) {
      rows.push((await fetch("/api/bff/auth/google/start", { method: "POST", headers })).status);
    }
    return rows;
  });
  expect(results).toEqual([403, 403, 403]);
  expect((await control.getStats()).springRequests).toBe(before.springRequests);
  await info.attach("native-guard-rejections", {
    body: JSON.stringify({ statuses: results, upstreamDispatches: 0 }),
    contentType: "application/json",
  });
});

test.describe("Google Sign-In E2E", () => {
  test.beforeEach(async () => {
    const control = getControlClient();
    await control.reset();
    await control.seedOwnerGoogle("browser-owner-sub-1001");
  });

  test("F-02: Complete Google OIDC sign-in flow rotates SID and lands on Drive explorer", async ({
    page,
    context,
  }) => {
    const control = getControlClient();

    // Intercept Google accounts auth navigation to emulate Google user consent
    await context.route("https://accounts.google.com/**", async (route) => {
      const url = new URL(route.request().url());
      const state = url.searchParams.get("state") || "";
      const nonce = url.searchParams.get("nonce") || "test-nonce";
      const code = `code-${Date.now()}`;

      // Register the code with fake Google OIDC server
      await control.issueGoogleCode(code, nonce, "browser-owner-sub-1001");

      // Redirect browser to Spring callback on port 19080
      const callbackUrl = `http://localhost:19080/api/v1/auth/google/callback?state=${encodeURIComponent(state)}&code=${encodeURIComponent(code)}`;
      await route.fulfill({
        status: 302,
        headers: { Location: callbackUrl },
      });
    });

    await page.goto("/login");
    await expect(page.getByRole("heading", { name: "Đăng nhập" })).toBeVisible();

    // Click Google Sign-in
    const signInButton = page.getByRole("button", { name: "Đăng nhập bằng Google" });
    await expect(signInButton).toBeEnabled();
    const anonymousCookie = (await context.cookies()).find((c) => c.name === "ppv_dev_sid")!;
    const oldCsrf = await page.evaluate(
      async () => (await (await fetch("/api/bff/session")).json()).csrfToken
    );
    await signInButton.click();

    // Browser completes flow and lands on /personal/storage/google-drive
    await page.waitForURL("**/personal/storage/google-drive", { timeout: 15000 });
    await expect(page.getByRole("heading", { name: "Google Drive Explorer" })).toBeVisible();

    // Verify session cookies are present
    const cookies = await context.cookies();
    const sidCookie = cookies.find((c) => c.name === "ppv_dev_sid");
    expect(sidCookie).toBeDefined();
    expect(sidCookie?.httpOnly).toBe(true);
    expect(Boolean(sidCookie && sidCookie.value !== anonymousCookie.value)).toBe(true);
    expect(sidCookie?.domain).toBe("localhost");
    expect(sidCookie?.path).toBe("/");
    expect(sidCookie?.sameSite).toBe("Lax");
    const stale = await page.evaluate(
      async (csrf) =>
        (
          await fetch("/api/bff/drive/connections/1/authorize", {
            method: "POST",
            headers: { "X-PPV-CSRF": csrf },
          })
        ).status,
      oldCsrf
    );
    expect(stale).toBe(403);
    const rendered = await page.evaluate(async () => {
      const html = await (await fetch("/personal/storage/google-drive")).text();
      const rscResponse = await fetch("/personal/storage/google-drive", { headers: { RSC: "1" } });
      return {
        html,
        rsc: await rscResponse.text(),
        cache: rscResponse.headers.get("cache-control"),
        readableCookies: document.cookie,
        storage: Object.values({ ...localStorage, ...sessionStorage }).join(" "),
      };
    });
    expect(rendered.cache).toContain("no-store");
    const serialized = JSON.stringify(rendered);
    expect(serialized.includes(sidCookie!.value)).toBe(false);
    expect(serialized.includes("MasterVaultPassword123!")).toBe(false);
    expect(/eyJ[A-Za-z0-9_-]+\.[A-Za-z0-9_-]+\.[A-Za-z0-9_-]+/.test(serialized)).toBe(false);

    // Verify stats from Spring
    const stats = await control.getStats();
    expect(stats.googleTokenRequests).toBeGreaterThanOrEqual(1);
  });

  test("F-02: Direct access to private explorer redirects unauthenticated user to /login", async ({
    page,
    context,
  }) => {
    await context.clearCookies();
    await page.goto("/personal/storage/google-drive");
    await page.waitForURL("**/login", { timeout: 10000 });
    await expect(page.getByRole("heading", { name: "Đăng nhập" })).toBeVisible();
  });
});

test.describe("Native Sign-In provider identity and binding", () => {
  test.beforeEach(async () => {
    await getControlClient().reset();
    await getControlClient().seedOwnerGoogle();
  });
  for (const failure of [
    "missing",
    "wrong",
    "duplicate",
    "browser-expired",
    "attempt-expired",
    "replaced",
    "wrong-subject",
    "unbound",
    "drive-only",
  ] as const) {
    test(
      "F-02: rejects native " + failure + " Sign-In proof or identity",
      async ({ page, context }, info) => {
        const control = getControlClient();
        if (failure === "unbound" || failure === "drive-only") {
          await control.reset();
          if (failure === "drive-only") await control.connectSlot(1, "browser-owner-sub-1001");
        }
        await page.goto("/login");
        await expect(page.getByRole("button", { name: "Đăng nhập bằng Google" })).toBeEnabled();
        const start = () =>
          page.evaluate(async () => {
            const session = await (await fetch("/api/bff/session")).json();
            const response = await fetch("/api/bff/auth/google/start", {
              method: "POST",
              headers: { "X-PPV-CSRF": session.csrfToken },
            });
            return { status: response.status, body: await response.json() };
          });
        const beforeStart = await control.getStats();
        const initial = await start();
        if (failure === "unbound" || failure === "drive-only") {
          expect(initial.status).toBe(409);
          expect(initial.body.error.code).toBe("GOOGLE_OWNER_NOT_READY");
          expect((await control.getStats()).googleTokenRequests).toBe(
            beforeStart.googleTokenRequests
          );
          expect(
            await page.evaluate(
              async () => (await (await fetch("/api/bff/session")).json()).authenticated
            )
          ).toBe(false);
          await info.attach("early-native-identity-rejection", {
            body: JSON.stringify({
              failure,
              status: 409,
              beforeProvider: true,
              authenticated: false,
            }),
            contentType: "application/json",
          });
          return;
        }
        expect(initial.status).toBe(200);
        const name = "ppv_google_signin_bind";
        const cookie = (await context.cookies()).find((c) => c.name === name)!;
        expect(cookie.httpOnly).toBe(true);
        expect(cookie.domain).toBe("localhost");
        expect(cookie.path).toBe("/api/v1/auth/google/callback");
        expect(cookie.sameSite).toBe("Lax");
        if (failure === "missing") await context.clearCookies({ name });
        if (failure === "wrong") await context.addCookies([{ ...cookie, value: "x".repeat(43) }]);
        if (failure === "duplicate")
          await context.addCookies([{ ...cookie, path: "/api/v1/auth/google" }]);
        if (failure === "browser-expired")
          await context.addCookies([{ ...cookie, expires: Math.floor(Date.now() / 1000) - 1 }]);
        if (failure === "attempt-expired")
          await control.configure("advance-clock", { seconds: 301 });
        if (failure === "replaced") expect((await start()).status).toBe(200);
        await context.route("https://accounts.google.com/o/oauth2/v2/auth?**", async (route) => {
          const auth = new URL(route.request().url());
          const code = crypto.randomUUID();
          await control.issueGoogleCode(
            code,
            auth.searchParams.get("nonce")!,
            failure === "wrong-subject" ? "unapproved-native-subject" : "browser-owner-sub-1001"
          );
          const callback = new URL(auth.searchParams.get("redirect_uri")!);
          callback.searchParams.set("state", auth.searchParams.get("state")!);
          callback.searchParams.set("code", code);
          await route.fulfill({ status: 302, headers: { Location: callback.href } });
        });
        const before = await control.getStats();
        const request = page.waitForRequest(
          (r) => new URL(r.url()).pathname === "/api/v1/auth/google/callback"
        );
        await page.goto(initial.body.data.authorizationUrl);
        const headers = await (await request).allHeaders();
        const proofCount = (headers.cookie ?? "")
          .split(";")
          .filter((v) => v.trim().startsWith(name + "=")).length;
        expect(new URL(page.url()).pathname).toBe("/login");
        expect(
          await page.evaluate(
            async () => (await (await fetch("/api/bff/session")).json()).authenticated
          )
        ).toBe(false);
        const after = await control.getStats();
        expect(after.refreshTokensInDb).toBe(before.refreshTokensInDb);
        if (!["wrong-subject", "unbound", "drive-only"].includes(failure)) {
          expect(after.googleTokenRequests).toBe(before.googleTokenRequests);
        }
        await info.attach("sanitized-auth-proof", {
          body: JSON.stringify({
            failure,
            proofCount,
            providerDispatchDelta: after.googleTokenRequests - before.googleTokenRequests,
            authenticated: false,
          }),
          contentType: "application/json",
        });
      }
    );
  }
});

test.describe("Native live handoff negative controls", () => {
  test.beforeEach(async () => {
    await getControlClient().reset();
    await getControlClient().seedOwnerGoogle();
  });
  for (const failure of [
    "missing",
    "wrong",
    "duplicate",
    "browser-expired",
    "ticket-expired",
  ] as const) {
    test(
      "F-02: live ticket " + failure + " cannot create a web session",
      async ({ page, context }, info) => {
        const control = getControlClient();
        const ticket = await holdLiveTicket(page, context, control);
        expect((await control.getStats()).pendingTickets).toBe(1);
        if (failure === "missing") await context.clearCookies({ name: ticket.name });
        if (failure === "wrong") await context.addCookies([{ ...ticket, value: "x".repeat(43) }]);
        if (failure === "duplicate")
          await context.addCookies([{ ...ticket, path: "/api/bff/auth/google" }]);
        if (failure === "browser-expired")
          await context.addCookies([{ ...ticket, expires: Math.floor(Date.now() / 1000) - 1 }]);
        if (failure === "ticket-expired") await control.configure("advance-clock", { seconds: 61 });
        const before = await control.getStats();
        await page.goto("/api/bff/auth/google/complete");
        expect(new URL(page.url()).pathname).toBe("/login");
        const status = await page.evaluate(
          async () => (await (await fetch("/api/bff/session")).json()).authenticated
        );
        expect(status).toBe(false);
        expect((await control.getStats()).refreshTokensInDb).toBe(before.refreshTokensInDb);
        await info.attach("sanitized-live-ticket-rejection", {
          body: JSON.stringify({
            failure,
            pendingBefore: before.pendingTickets,
            authenticated: false,
            serverClockAdvanced: failure === "ticket-expired",
            browserExpired: failure === "browser-expired",
          }),
          contentType: "application/json",
        });
      }
    );
  }

  test("F-02: another browser verifier rejects a demonstrably live ticket; original browser can still redeem once", async ({
    page,
    context,
    browser,
  }) => {
    const control = getControlClient();
    const ticket = await holdLiveTicket(page, context, control);
    const other = await browser.newContext();
    try {
      const tab = await other.newPage();
      await tab.goto("http://localhost:3000/login");
      await expect(tab.getByRole("button", { name: "Đăng nhập bằng Google" })).toBeEnabled();
      await tab.evaluate(async () => {
        const session = await (await fetch("/api/bff/session")).json();
        const start = await fetch("/api/bff/auth/google/start", {
          method: "POST",
          headers: { "X-PPV-CSRF": session.csrfToken },
        });
        if (!start.ok) throw new Error("Independent verifier start failed");
      });
      await other.addCookies([ticket]);
      const before = await control.getStats();
      expect(before.pendingTickets).toBe(1);
      await tab.goto("http://localhost:3000/api/bff/auth/google/complete");
      expect(new URL(tab.url()).pathname).toBe("/login");
      expect((await control.getStats()).refreshTokensInDb).toBe(before.refreshTokensInDb);
      expect((await control.getStats()).pendingTickets).toBe(1);
      await page.goto("/api/bff/auth/google/complete");
      expect(new URL(page.url()).pathname).toBe("/personal/storage/google-drive");
      expect((await control.getStats()).pendingTickets).toBe(0);
      // A fresh anonymous verifier with the consumed ticket proves one-time redemption.
      await tab.evaluate(async () => {
        const session = await (await fetch("/api/bff/session")).json();
        await fetch("/api/bff/auth/google/start", {
          method: "POST",
          headers: { "X-PPV-CSRF": session.csrfToken },
        });
      });
      await other.addCookies([ticket]);
      const redeemed = await control.getStats();
      await tab.goto("http://localhost:3000/api/bff/auth/google/complete");
      expect(new URL(tab.url()).pathname).toBe("/login");
      expect((await control.getStats()).refreshTokensInDb).toBe(redeemed.refreshTokensInDb);
    } finally {
      await other.close();
    }
  });

  test("F-01: ninth native session is 429 without a new cookie or changing eight live CSRF proofs", async ({
    browser,
  }) => {
    const contexts = [];
    try {
      const live = [];
      for (let i = 0; i < 8; i++) {
        const context = await browser.newContext();
        contexts.push(context);
        const page = await context.newPage();
        await page.goto("http://localhost:3000/login");
        await expect(page.getByRole("button", { name: "Đăng nhập bằng Google" })).toBeEnabled();
        const proof = await page.evaluate(
          async () => (await (await fetch("/api/bff/session")).json()).csrfToken
        );
        live.push({ page, proof });
      }
      const ninth = await browser.newContext();
      contexts.push(ninth);
      const page = await ninth.newPage();
      await page.goto("http://localhost:3000/login");
      const denied = await page.evaluate(async () => {
        const response = await fetch("/api/bff/session");
        return { status: response.status, code: (await response.json()).error?.code };
      });
      expect(denied).toEqual({ status: 429, code: "SESSION_CAPACITY_EXHAUSTED" });
      expect((await ninth.cookies()).some((c) => c.name === "ppv_dev_sid")).toBe(false);
      for (const { page, proof } of live) {
        const same = await page.evaluate(async (old) => {
          const response = await fetch("/api/bff/session");
          return response.status === 200 && (await response.json()).csrfToken === old;
        }, proof);
        expect(same).toBe(true);
      }
    } finally {
      for (const context of contexts) await context.close();
    }
  });
});

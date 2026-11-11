import { expect, test } from "@playwright/test";
import { getControlClient } from "../fixtures/spring-fixture";
import { loginAsOwner, holdLiveTicket } from "../fixtures/transport";

test.describe("Session Races & Multi-Tab Invalidation E2E", () => {
  test.beforeEach(async () => {
    const control = getControlClient();
    await control.reset();
  });

  test("F-02: Logout in Tab 1 invalidates session across all tabs", async ({ context }) => {
    const control = getControlClient();
    const tab1 = await context.newPage();
    await loginAsOwner(tab1, context, control);

    // Open Tab 2 in the same context
    const tab2 = await context.newPage();
    await tab2.goto("/personal/storage/google-drive");
    await expect(tab2.getByRole("heading", { name: "Google Drive Explorer" })).toBeVisible({
      timeout: 10000,
    });

    // Logout from Tab 1
    const logoutBtn = tab1.getByRole("button", { name: "Đăng xuất" });
    await expect(logoutBtn).toBeVisible();
    await logoutBtn.click();
    await tab1.waitForURL("**/login", { timeout: 10000 });

    // Storage invalidation must navigate the existing tab without reloading it.
    await tab2.waitForURL("**/login", { timeout: 10000 });
    await expect(tab2.getByRole("heading", { name: "Đăng nhập" })).toBeVisible();
  });

  test("F-02: Logout during held refresh wins; late tokens cannot reinstall session", async ({
    page,
    context,
  }) => {
    const control = getControlClient();
    await loginAsOwner(page, context, control);

    // Arm pause on Spring refresh endpoint
    await control.pauseRefresh();

    // Expire the access token via test fixture header on session route
    await page.request.get("/api/bff/session", {
      headers: {
        "x-ppv-test-expire-access": "true",
      },
    });

    // Launch a background request that triggers ensureFreshAccessToken -> calls Spring /api/v1/auth/refresh
    const refreshTriggerPromise = page.request.get("/api/bff/drive/connections").catch(() => null);

    // Await Spring fixture pausing at the refresh latch
    const refreshEntered = await control.waitRefreshEntered();
    expect(refreshEntered).toBe(true);

    // Trigger logout while refresh is paused in Spring
    const logoutBtn = page.getByRole("button", { name: "Đăng xuất" });
    await expect(logoutBtn).toBeVisible();
    await logoutBtn.click();
    await page.waitForURL("**/login", { timeout: 10000 });

    // Release refresh latch in Spring
    await control.releaseRefresh();
    await refreshTriggerPromise;

    // Verify session remains destroyed
    await page.goto("/personal/storage/google-drive");
    await page.waitForURL("**/login", { timeout: 10000 });
  });
});

test.describe("Actual Next volatile-session boundaries", () => {
  test.beforeEach(async () => {
    await getControlClient().reset();
  });
  test("F-02: two native tabs share one held real Spring refresh", async ({ page, context }) => {
    const control = getControlClient();
    await loginAsOwner(page, context, control);
    const second = await context.newPage();
    await second.goto("http://localhost:3000/personal/storage/google-drive");
    await page.waitForLoadState("networkidle");
    await second.waitForLoadState("networkidle");
    await control.pauseRefresh();
    const before = await control.getStats();
    await page.evaluate(async () => {
      await fetch("/api/bff/session", { headers: { "x-ppv-test-expire-access": "true" } });
    });
    const one = page.evaluate(async () => (await fetch("/api/bff/drive/connections")).status);
    const two = second.evaluate(async () => (await fetch("/api/bff/drive/connections")).status);
    expect(await control.waitRefreshEntered()).toBe(true);
    await control.releaseRefresh();
    expect(await Promise.all([one, two])).toEqual([200, 200]);
    expect((await control.getStats()).refreshRequests - before.refreshRequests).toBe(1);
  });

  test("F-02: actual Node restart rejects the old native browser cookie", async ({
    page,
    context,
  }, info) => {
    await loginAsOwner(page, context, getControlClient());
    const before = (await context.cookies()).find((c) => c.name === "ppv_dev_sid")!;
    const port = process.env.PPV_NODE_CONTROL_PORT;
    if (!port) throw new Error("Owned Node restart control is unavailable");
    const response = await fetch("http://127.0.0.1:" + port + "/restart-next", {
      method: "POST",
      signal: AbortSignal.timeout(20000),
    });
    expect(response.status).toBe(200);
    const stillOld = (await context.cookies()).some(
      (c) => c.name === before.name && c.value === before.value
    );
    expect(stillOld).toBe(true);
    await page.goto("/personal/storage/google-drive");
    await page.waitForURL("**/login");
    expect(new URL(page.url()).pathname).toBe("/login");
    expect(
      await page.evaluate(
        async () => (await (await fetch("/api/bff/session")).json()).authenticated
      )
    ).toBe(false);
    await info.attach("restart-proof", {
      body: JSON.stringify({
        realProcessRestart: true,
        oldCookieRetainedBeforeNavigation: true,
        privateAccess: false,
      }),
      contentType: "application/json",
    });
  });
  for (const expiry of ["idle", "absolute"] as const) {
    test(
      "F-02: server record " + expiry + " expiry rejects a still-present browser cookie",
      async ({ page, context }) => {
        await loginAsOwner(page, context, getControlClient());
        await page.evaluate(async (selected) => {
          await fetch("/api/bff/session", { headers: { "x-ppv-test-expire-session": selected } });
        }, expiry);
        expect((await context.cookies()).some((c) => c.name === "ppv_dev_sid")).toBe(true);
        await page.goto("/personal/storage/google-drive");
        await page.waitForURL("**/login");
        expect(new URL(page.url()).pathname).toBe("/login");
      }
    );
  }
});

test("F-02: cancel during a held redeemed handoff rejects the late pair and revokes it", async ({
  page,
  context,
}) => {
  const control = getControlClient();
  await control.reset();
  await control.seedOwnerGoogle();
  await holdLiveTicket(page, context, control);
  const otherTab = await context.newPage();
  await otherTab.goto("/login");
  await expect(otherTab.getByRole("button", { name: "Đăng nhập bằng Google" })).toBeEnabled();
  await control.configure("hold-response", { path: "/api/v1/auth/google/handoff" });
  const beforeRedeem = await control.getStats();
  const cancelledSid = (await context.cookies()).find((c) => c.name === "ppv_dev_sid")!.value;
  const completing = page.goto("/api/bff/auth/google/complete");
  await expect.poll(async () => (await control.getStats()).responseHeld).toBe(true);
  expect((await control.getStats()).activeRefreshTokens).toBe(beforeRedeem.activeRefreshTokens + 1);
  const cancelled = await otherTab.evaluate(async () => {
    const session = await (await fetch("/api/bff/session")).json();
    return (
      await fetch("/api/bff/auth/cancel", {
        method: "POST",
        headers: { "X-PPV-CSRF": session.csrfToken },
      })
    ).status;
  });
  expect(cancelled).toBe(200);
  await control.configure("release-response", {});
  await completing;
  await page.waitForURL("**/login?**");
  await expect
    .poll(async () => (await control.getStats()).activeRefreshTokens)
    .toBe(beforeRedeem.activeRefreshTokens);
  // Redirected login bootstraps a new anonymous cookie; it must never reinstall the old authority.
  expect(
    (await context.cookies()).some((c) => c.name === "ppv_dev_sid" && c.value === cancelledSid)
  ).toBe(false);
  expect(
    await page.evaluate(async () => (await (await fetch("/api/bff/session")).json()).authenticated)
  ).toBe(false);
  await page.goto("/personal/storage/google-drive");
  await page.waitForURL("**/login");
});

for (const mode of ["revoked-refresh", "revoke-failure"] as const) {
  test("F-02: actual Next native session fails closed after " + mode, async ({ page, context }) => {
    const control = getControlClient();
    await control.reset();
    await loginAsOwner(page, context, control);
    await control.configure("refresh-state", {
      revokeActive: mode === "revoked-refresh",
      failRevoke: mode === "revoke-failure",
    });
    if (mode === "revoke-failure") {
      await page.getByRole("button", { name: "Đăng xuất" }).click();
      await page.waitForURL("**/login");
    } else {
      await page.evaluate(async () => {
        await fetch("/api/bff/session", { headers: { "x-ppv-test-expire-access": "true" } });
      });
      expect(
        await page.evaluate(async () => (await fetch("/api/bff/drive/connections")).status)
      ).toBe(401);
    }
    await page.goto("/personal/storage/google-drive");
    await page.waitForURL("**/login");
    expect(
      await page.evaluate(
        async () => (await (await fetch("/api/bff/session")).json()).authenticated
      )
    ).toBe(false);
  });
}

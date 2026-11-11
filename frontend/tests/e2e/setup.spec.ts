import { expect, test } from "@playwright/test";
import { getControlClient } from "../fixtures/spring-fixture";

test.describe("Owner Google Setup & Link E2E", () => {
  test.beforeEach(async () => {
    const control = getControlClient();
    await control.reset();
  });

  test("F-02: Legacy login + fresh password links Google account to singleton owner", async ({
    page,
    context,
  }) => {
    const control = getControlClient();

    // Intercept Google accounts auth navigation to complete linking
    await context.route("https://accounts.google.com/**", async (route) => {
      const url = new URL(route.request().url());
      const state = url.searchParams.get("state") || "";
      const nonce = url.searchParams.get("nonce") || "test-nonce";
      const code = `setup-code-${Date.now()}`;

      await control.issueGoogleCode(code, nonce, "setup-owner-sub-2002");

      const callbackUrl = `http://localhost:19080/api/v1/auth/google/callback?state=${encodeURIComponent(state)}&code=${encodeURIComponent(code)}`;
      await route.fulfill({
        status: 302,
        headers: { Location: callbackUrl },
      });
    });

    await page.goto("/setup/google");
    await expect(page.getByRole("heading", { name: "Thiết lập liên kết Google" })).toBeVisible();

    // Step 1: Legacy login
    await page.fill("#setup-username", "vaultowner");
    await page.fill("#setup-password", "MasterVaultPassword123!");
    await page.click("button[type='submit']");

    // Advances to Step 2: Google Link
    await expect(
      page.getByText("Tài khoản đang ở chế độ thiết lập một lần. Không thể truy cập dữ liệu chính.")
    ).toBeVisible({
      timeout: 10000,
    });

    // Step 2: Fresh password confirmation
    await page.fill("#setup-fresh-password", "MasterVaultPassword123!");
    await page.click("button:has-text('Bắt đầu liên kết Google')");

    // Completes flow and lands on explorer
    await page.waitForURL("**/personal/storage/google-drive", { timeout: 15000 });
    await expect(page.getByRole("heading", { name: "Google Drive Explorer" })).toBeVisible();
  });

  test("F-02: Invalid password in legacy login fails with error message", async ({ page }) => {
    await page.goto("/setup/google");
    await page.fill("#setup-username", "vaultowner");
    await page.fill("#setup-password", "WrongPassword123!");
    await page.click("button[type='submit']");

    await expect(page.getByText("Tên đăng nhập hoặc mật khẩu không chính xác.")).toBeVisible({
      timeout: 10000,
    });
  });
});

test("F-01/F-02: SETUP_ONLY cannot invoke private routes, and LINK requires a separately fresh password", async ({
  page,
}, info) => {
  const control = getControlClient();
  await control.reset();
  await page.goto("/setup/google");
  await page.fill("#setup-username", "vaultowner");
  await page.fill("#setup-password", "MasterVaultPassword123!");
  await page.click("button[type='submit']");
  await expect(page.locator("#setup-fresh-password")).toBeVisible();
  const before = await control.getStats();
  const denied = await page.evaluate(async () => {
    const session = await (await fetch("/api/bff/session")).json();
    const calls: [string, string][] = [
      ["GET", "connections"],
      ["GET", "connections/1/items?scope=parent&parentId=root"],
      ["GET", "connections/1/storage-quota"],
      ["GET", "connections/1/files/file/thumbnail"],
      ["GET", "connections/1/files/file/download"],
      ["POST", "connections/1/authorize"],
      ["POST", "connections/1/root-folder"],
      ["POST", "connections/1/folders"],
      ["POST", "connections/1/files"],
      ["POST", "connections/1/folders/child/files"],
      ["DELETE", "connections/1"],
    ];
    const results = [];
    for (const [method, path] of calls) {
      results.push(
        (
          await fetch("/api/bff/drive/" + path, {
            method,
            headers: { "X-PPV-CSRF": session.csrfToken },
          })
        ).status
      );
    }
    return results;
  });
  expect(denied.every((status) => status === 403)).toBe(true);
  expect((await control.getStats()).springRequests).toBe(before.springRequests);
  const response = page.waitForResponse(
    (r) => new URL(r.url()).pathname === "/api/bff/setup/google/link/start"
  );
  await page.fill("#setup-fresh-password", "DifferentWrongPassword123!");
  await page.getByRole("button", { name: "Bắt đầu liên kết Google" }).click();
  expect((await response).status()).toBe(401);
  await expect(
    page.getByRole("alert").filter({ hasText: "Mật khẩu xác nhận không chính xác." })
  ).toBeVisible();
  await expect(page.locator("#setup-fresh-password")).toHaveValue("");
  await info.attach("restricted-purpose-proof", {
    body: JSON.stringify({
      denied,
      privateSpringDispatches: 0,
      freshlyWrongPasswordRejected: true,
      fieldCleared: true,
    }),
    contentType: "application/json",
  });
});
test("F-02: setup logout during held real LINK handoff revokes both token pairs and wins", async ({
  page,
  context,
}) => {
  const control = getControlClient();
  await control.reset();
  const baseline = await control.getStats();
  await context.route("https://accounts.google.com/**", async (route) => {
    const auth = new URL(route.request().url());
    const code = crypto.randomUUID();
    await control.issueGoogleCode(code, auth.searchParams.get("nonce")!, "browser-owner-sub-1001");
    const callback = new URL(auth.searchParams.get("redirect_uri")!);
    callback.searchParams.set("state", auth.searchParams.get("state")!);
    callback.searchParams.set("code", code);
    await route.fulfill({ status: 302, headers: { Location: callback.href } });
  });
  await page.goto("/setup/google");
  await page.fill("#setup-username", "vaultowner");
  await page.fill("#setup-password", "MasterVaultPassword123!");
  await page.locator('button[type="submit"]').click();
  await expect(page.locator("#setup-fresh-password")).toBeVisible();
  const logoutTab = await context.newPage();
  await logoutTab.goto("/setup/google");
  await expect(logoutTab.getByRole("heading", { name: "Thiết lập liên kết Google" })).toBeVisible();
  await control.configure("hold-response", { path: "/api/v1/auth/google/handoff" });
  await page.fill("#setup-fresh-password", "MasterVaultPassword123!");
  const linking = page.getByRole("button", { name: "Bắt đầu liên kết Google" }).click();
  await expect.poll(async () => (await control.getStats()).responseHeld).toBe(true);
  expect((await control.getStats()).activeRefreshTokens).toBe(baseline.activeRefreshTokens + 2);
  const status = await logoutTab.evaluate(async () => {
    const csrf = (await (await fetch("/api/bff/session")).json()).csrfToken;
    return (
      await fetch("/api/bff/auth/logout", { method: "POST", headers: { "X-PPV-CSRF": csrf } })
    ).status;
  });
  expect(status).toBe(200);
  await control.configure("release-response", {});
  await linking;
  await page.waitForURL("**/login?**");
  await expect
    .poll(async () => (await control.getStats()).activeRefreshTokens)
    .toBe(baseline.activeRefreshTokens);
  expect(
    await page.evaluate(async () => (await (await fetch("/api/bff/session")).json()).authenticated)
  ).toBe(false);
});

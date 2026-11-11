import { expect, test, type Page, type BrowserContext } from "@playwright/test";
import { getControlClient } from "../fixtures/spring-fixture";
import { loginAsOwner } from "../fixtures/transport";

async function authorize(page: Page, slot: 1 | 2): Promise<string> {
  return page.evaluate(async (selected) => {
    const session = await (await fetch("/api/bff/session")).json();
    const response = await fetch("/api/bff/drive/connections/" + selected + "/authorize", {
      method: "POST",
      headers: { "X-PPV-CSRF": session.csrfToken },
    });
    if (response.status !== 200) throw new Error("Drive authorization failed");
    return (await response.json()).data.authorizationUrl as string;
  }, slot);
}

async function consent(context: BrowserContext, slot: 1 | 2) {
  const control = getControlClient();
  await context.unroute("https://accounts.google.com/**");
  await context.route("https://accounts.google.com/o/oauth2/v2/auth?**", async (route) => {
    const url = new URL(route.request().url());
    const redirect = new URL(url.searchParams.get("redirect_uri")!);
    expect(redirect.origin).toBe("http://localhost:19080");
    expect(redirect.pathname).toBe("/api/v1/drive/oauth/callback");
    const code = crypto.randomUUID();
    await control.configure("issue-drive-code", {
      code,
      nonce: url.searchParams.get("nonce"),
      sub: "native-drive-slot-" + slot,
    });
    redirect.searchParams.set("state", url.searchParams.get("state")!);
    redirect.searchParams.set("code", code);
    await route.fulfill({ status: 302, headers: { Location: redirect.href } });
  });
}

test.describe("Actual Next/Spring native Drive cookie proof", () => {
  test.beforeEach(async () => {
    await getControlClient().reset();
  });

  test("F-03: both slots complete with automatic host-only native jars and explicit status refresh", async ({
    page,
    context,
  }, info) => {
    const control = getControlClient();
    await loginAsOwner(page, context, control);
    const evidence: unknown[] = [];
    for (const slot of [1, 2] as const) {
      await consent(context, slot);
      const url = await authorize(page, slot);
      const name = "PPV_DRIVE_BINDING_" + slot;
      const cookie = (await context.cookies()).find((c) => c.name === name)!;
      expect(cookie.domain).toBe("localhost");
      expect(cookie.httpOnly).toBe(true);
      expect(cookie.sameSite).toBe("Lax");
      expect(cookie.path).toBe("/api/v1/drive/oauth/callback");
      expect(await page.evaluate(() => document.cookie)).not.toContain(name);
      expect(
        (await context.cookies("http://localhost:3000/api/bff/drive/connections")).some(
          (c) => c.name === name
        )
      ).toBe(false);
      expect(
        (await context.cookies("http://127.0.0.1:19080/api/v1/drive/oauth/callback")).some(
          (c) => c.name === name
        )
      ).toBe(false);
      const before = await control.getStats();
      const observed = page.waitForRequest(
        (r) => new URL(r.url()).pathname === "/api/v1/drive/oauth/callback"
      );
      const response = await page.goto(url);
      const headers = await (await observed).allHeaders();
      const proofCount = (headers.cookie ?? "")
        .split(";")
        .filter((v) => v.trim().startsWith(name + "=")).length;
      expect(proofCount).toBe(1);
      expect(response?.status()).toBe(200);
      expect(response?.headers()["content-type"]).toContain("text/plain");
      await expect(page.locator("body")).toHaveText(
        "Google Drive connection completed. Return to PPV."
      );
      expect((await control.getStats()).oauthTokenExchanges - before.oauthTokenExchanges).toBe(1);
      evidence.push({
        slot,
        callbackHost: headers.host,
        proofCount,
        httpOnly: cookie.httpOnly,
        domain: cookie.domain,
        sameSite: cookie.sameSite,
        path: cookie.path,
        status: response?.status(),
      });
      await page.goto("/personal/storage/google-drive");
      const connections = await page.evaluate(
        async () => (await (await fetch("/api/bff/drive/connections")).json()).data.connections
      );
      expect(connections.find((c: { slot: number }) => c.slot === slot).status).toBe("CONNECTED");
    }
    await info.attach("sanitized-native-cookie-proof", {
      body: JSON.stringify(evidence),
      contentType: "application/json",
    });
  });

  for (const slot of [1, 2] as const) {
    for (const failure of [
      "missing",
      "wrong",
      "duplicate",
      "replaced",
      "browser-expired",
      "attempt-expired",
      "wrong-host",
    ] as const) {
      test(
        "F-03: slot " + slot + " rejects " + failure + " before code exchange",
        async ({ page, context }, info) => {
          const control = getControlClient();
          await loginAsOwner(page, context, control);
          await consent(context, slot);
          const url = await authorize(page, slot);
          const name = "PPV_DRIVE_BINDING_" + slot;
          const cookie = (await context.cookies()).find((c) => c.name === name)!;
          if (failure === "missing") await context.clearCookies({ name });
          if (failure === "wrong")
            await context.addCookies([{ ...cookie, value: "wrong_" + "x".repeat(43) }]);
          if (failure === "duplicate")
            await context.addCookies([
              { ...cookie, path: "/api/v1/drive/oauth", value: "duplicate_" + "x".repeat(43) },
            ]);
          if (failure === "replaced") await authorize(page, slot);
          if (failure === "browser-expired") {
            await context.addCookies([{ ...cookie, expires: Math.floor(Date.now() / 1000) - 1 }]);
            expect((await context.cookies()).some((c) => c.name === name)).toBe(false);
          }
          if (failure === "attempt-expired")
            await control.configure("advance-clock", { seconds: 301 });
          if (failure === "wrong-host") {
            await context.unroute("https://accounts.google.com/**");
            await context.route(
              "https://accounts.google.com/o/oauth2/v2/auth?**",
              async (route) => {
                const auth = new URL(route.request().url());
                const code = crypto.randomUUID();
                await control.configure("issue-drive-code", {
                  code,
                  nonce: auth.searchParams.get("nonce"),
                  sub: "native-drive-slot-" + slot,
                });
                const callback = new URL(auth.searchParams.get("redirect_uri")!);
                callback.hostname = "127.0.0.1";
                callback.searchParams.set("state", auth.searchParams.get("state")!);
                callback.searchParams.set("code", code);
                await route.fulfill({ status: 302, headers: { Location: callback.href } });
              }
            );
          }
          const before = await control.getStats();
          const request = page.waitForRequest(
            (r) => new URL(r.url()).pathname === "/api/v1/drive/oauth/callback"
          );
          const response = await page.goto(url);
          const headers = await (await request).allHeaders();
          const proofCount = (headers.cookie ?? "")
            .split(";")
            .filter((v) => v.trim().startsWith(name + "=")).length;
          expect(response?.status()).toBe(400);
          expect((await control.getStats()).oauthTokenExchanges).toBe(before.oauthTokenExchanges);
          if (failure === "duplicate") expect(proofCount).toBe(2);
          if (["missing", "browser-expired", "wrong-host"].includes(failure))
            expect(proofCount).toBe(0);
          await info.attach("sanitized-rejection", {
            body: JSON.stringify({
              slot,
              failure,
              proofCount,
              callbackHost: headers.host,
              status: response?.status(),
              providerDispatchDelta: 0,
            }),
            contentType: "application/json",
          });
        }
      );
    }
  }
});

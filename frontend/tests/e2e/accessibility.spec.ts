import AxeBuilder from "@axe-core/playwright";
import { expect, test, chromium, type Page } from "@playwright/test";
import fs from "node:fs";
import path from "node:path";
import { getControlClient } from "../fixtures/spring-fixture";
import { loginAsOwner } from "../fixtures/transport";

async function populated(page: Page) {
  await page.getByRole("button", { name: "Khởi tạo thư mục PPV" }).click();
  await expect(page.getByRole("button", { name: "Mới", exact: true })).toBeEnabled();
  await page
    .locator('input[type="file"]')
    .setInputFiles(path.resolve("tests/fixtures/assets/sample.png"));
  await expect(page.getByText("Tải tệp lên thành công.")).toBeVisible();
}
async function tabTo(page: Page, target: ReturnType<Page["getByRole"]>) {
  for (let i = 0; i < 35; i++) {
    if (await target.evaluate((e) => e === document.activeElement)) return;
    await page.keyboard.press("Tab");
  }
  throw new Error("Keyboard target was not reachable in a bounded traversal");
}

test.describe("Accessibility (A11y) & Contrast E2E", () => {
  test.beforeEach(async () => {
    const control = getControlClient();
    await control.reset();
  });

  test("F-04: Axe audit on Login and Setup pages passes with no serious/critical violations", async ({
    page,
  }) => {
    await page.goto("/login");
    const googleLoginBtn = page.getByRole("button", { name: "Đăng nhập bằng Google" });
    await expect(googleLoginBtn).toBeVisible({ timeout: 10000 });
    await expect(googleLoginBtn).toBeEnabled({ timeout: 10000 });
    await page.waitForFunction(() => {
      const btn = document.querySelector('button[type="button"]');
      if (!btn) return false;
      const opacity = window.getComputedStyle(btn).opacity;
      return opacity === "1" || opacity === "";
    });
    const loginAxe = await new AxeBuilder({ page }).analyze();
    const severeLoginViolations = loginAxe.violations.filter(
      (v) => v.impact === "critical" || v.impact === "serious"
    );
    expect(severeLoginViolations).toEqual([]);

    await page.goto("/setup/google");
    const setupLoginBtn = page.getByRole("button", { name: "Đăng nhập thiết lập" });
    await expect(setupLoginBtn).toBeVisible({ timeout: 10000 });
    await expect(setupLoginBtn).toBeEnabled({ timeout: 10000 });
    await page.waitForFunction(() => {
      const btn = document.querySelector('button[type="submit"]');
      if (!btn) return false;
      const opacity = window.getComputedStyle(btn).opacity;
      return opacity === "1" || opacity === "";
    });
    const setupAxe = await new AxeBuilder({ page }).analyze();
    const severeSetupViolations = setupAxe.violations.filter(
      (v) => v.impact === "critical" || v.impact === "serious"
    );
    expect(severeSetupViolations).toEqual([]);
  });

  test("F-04: Keyboard skip-link navigates directly to main content", async ({ page, context }) => {
    const control = getControlClient();
    await loginAsOwner(page, context, control);

    await page.reload();
    await expect(page.getByRole("heading", { name: "Google Drive Explorer" })).toBeVisible();
    await page.waitForLoadState("networkidle");
    await page.keyboard.press("Tab");
    const skipLink = page.getByRole("link", { name: "Chuyển đến nội dung chính" });
    await expect(skipLink).toBeFocused();

    await page.keyboard.press("Enter");
    const mainHeading = page.getByRole("heading", { name: "Google Drive Explorer" });
    await expect(mainHeading).toBeVisible();
  });

  test("F-04: 320 CSS-px reflow without page two-dimensional scrolling", async ({
    page,
    context,
  }) => {
    const control = getControlClient();
    await loginAsOwner(page, context, control);

    // Emulate 320px width reflow
    await page.setViewportSize({ width: 320, height: 640 });
    await expect(page.getByRole("heading", { name: "Google Drive Explorer" })).toBeVisible();

    // Verify main container does not force horizontal document body overflow
    const hasHorizontalOverflow = await page.evaluate(() => {
      return document.documentElement.scrollWidth > window.innerWidth;
    });
    expect(hasHorizontalOverflow).toBe(false);
  });
});

for (const viewport of [
  { width: 1366, height: 768 },
  { width: 1536, height: 864 },
]) {
  test(
    "F-04: populated Explorer keyboard, errors, focus, reduced motion and contrast inputs " +
      viewport.width,
    async ({ page, context }, info) => {
      const control = getControlClient();
      await control.reset();
      await control.connectSlot(1);
      await loginAsOwner(page, context, control);
      await page.setViewportSize(viewport);
      await populated(page);
      await page.reload();
      await expect(page.getByText("sample.png").first()).toBeVisible();
      const axe = await new AxeBuilder({ page }).analyze();
      await info.attach("populated-axe", {
        body: JSON.stringify(
          axe.violations.map((v) => ({
            id: v.id,
            impact: v.impact,
            nodes: v.nodes.map((n) => n.target),
          }))
        ),
        contentType: "application/json",
      });
      expect(
        axe.violations.filter((v) => v.impact === "serious" || v.impact === "critical")
      ).toEqual([]);
      await page.keyboard.press("Tab");
      await expect(page.getByRole("link", { name: "Chuyển đến nội dung chính" })).toBeFocused();
      await page.keyboard.press("Enter");
      await expect(page.locator("#main-content")).toBeFocused();
      const slot2 = page.getByRole("button", { name: /Tài khoản 2/ });
      await tabTo(page, slot2);
      await page.keyboard.press("Space");
      await expect(slot2).toHaveAttribute("aria-pressed", "true");
      const slot1 = page.getByRole("button", { name: /Tài khoản 1/ });
      await tabTo(page, slot1);
      await page.keyboard.press("Enter");
      const newButton = page.getByRole("button", { name: "Mới", exact: true });
      await expect(newButton).toBeEnabled();
      await tabTo(page, newButton);
      const focus = await newButton.evaluate((e) => {
        const s = getComputedStyle(e);
        return { outline: s.outline, outlineColor: s.outlineColor, outlineWidth: s.outlineWidth };
      });
      expect(parseFloat(focus.outlineWidth)).toBeGreaterThanOrEqual(2);
      await page.keyboard.press("Enter");
      await expect(page.getByRole("menu")).toBeVisible();
      await page.keyboard.press("End");
      await expect(page.getByRole("menuitem", { name: "Thư mục mới" })).toBeFocused();
      await page.keyboard.press("Enter");
      await expect(page.getByLabel("Tên thư mục")).toBeFocused();
      await page.keyboard.type("invalid/name");
      await page.keyboard.press("Enter");
      await expect(page.getByRole("alert")).toBeVisible();
      const dialogAxe = await new AxeBuilder({ page }).analyze();
      expect(
        dialogAxe.violations.filter((v) => v.impact === "serious" || v.impact === "critical")
      ).toEqual([]);
      await page.keyboard.press("Escape");
      await expect(newButton).toBeFocused();
      await page.emulateMedia({ reducedMotion: "reduce" });
      expect(
        await newButton.evaluate((e) => parseFloat(getComputedStyle(e).transitionDuration))
      ).toBeLessThanOrEqual(0.00001);
      await page.locator("#main-content").evaluate((e) => {
        e.scrollTop = 0;
      });
      const rows = await page.evaluate(() => {
        const samples: [string, string, string?][] = [
          ["heading", "h1"],
          ["description", "h1 + p"],
          [
            "search-placeholder",
            'input[placeholder="Tìm trong Drive hiện tại..."]',
            "::placeholder",
          ],
          ["from-label", 'input[type="date"]'],
          ["quota", "main span.font-semibold.text-white"],
        ];
        const rows = samples.map(([name, selector, pseudo]) => {
          const e = document.querySelector<HTMLElement>(selector);
          if (!e) throw new Error("Missing contrast sample " + name);
          const s = getComputedStyle(e, pseudo);
          const r = e.getBoundingClientRect();
          return {
            name,
            foreground: s.color,
            fontSize: s.fontSize,
            point: { x: r.right + (name === "quota" ? 2 : -4), y: r.top + 4 },
          };
        });
        const button = Array.from(document.querySelectorAll("button")).find(
          (e) => e.textContent?.trim() === "Mới"
        )!;
        const r = button.getBoundingClientRect();
        rows.push({
          name: "new-default",
          foreground: getComputedStyle(button).color,
          fontSize: getComputedStyle(button).fontSize,
          point: { x: r.left + 8, y: r.top + 8 },
        });
        rows.push({
          name: "new-focus-outline",
          foreground: getComputedStyle(button).outlineColor,
          fontSize: "nontext",
          point: { x: r.left - 7, y: r.top + 8 },
        });
        const gradient = document.querySelector<HTMLElement>('nav [class*="from-blue-600"]')!;
        const g = gradient.getBoundingClientRect();
        rows.push({
          name: "gradient-icon",
          foreground: getComputedStyle(gradient).color,
          fontSize: "nontext",
          point: { x: g.left + 8, y: g.top + 8 },
        });
        return rows;
      });
      await page.screenshot({ path: info.outputPath("rendered-contrast.png"), fullPage: true });
      fs.writeFileSync(
        info.outputPath("contrast-inputs.json"),
        JSON.stringify(
          { viewport, dpr: await page.evaluate(() => devicePixelRatio), rows, focus },
          null,
          2
        ),
        { flag: "wx" }
      );
      await newButton.hover();
      const hover = await newButton.evaluate((e) => {
        const s = getComputedStyle(e),
          r = e.getBoundingClientRect();
        return {
          name: "new-hover",
          foreground: s.color,
          fontSize: s.fontSize,
          point: { x: r.left + 8, y: r.top + 8 },
        };
      });
      await page.screenshot({
        path: info.outputPath("rendered-hover-contrast.png"),
        fullPage: true,
      });
      fs.writeFileSync(
        info.outputPath("hover-contrast-inputs.json"),
        JSON.stringify(
          { viewport, dpr: await page.evaluate(() => devicePixelRatio), rows: [hover] },
          null,
          2
        ),
        { flag: "wx" }
      );
      await info.attach("keyboard-focus-worksheet", {
        body: JSON.stringify({
          skip: true,
          mainFocused: true,
          slotKeyboard: true,
          menuDialog: true,
          error: true,
          escapeFocusReturn: true,
          reducedMotion: true,
          focus,
        }),
        contentType: "application/json",
      });
    }
  );
}

for (const percent of [200, 300]) {
  test("PH-I01: native Edge " + percent + " percent zoom", async ({ browserName }, info) => {
    expect(browserName).toBe("chromium");
    test.setTimeout(240000);
    const artifact = process.env.PPV_H1F_ARTIFACT_DIR;
    if (!artifact) throw new Error("Unique native artifact directory required");
    const browser = await chromium.launch({
      channel: "msedge",
      headless: false,
      args: ["--window-size=1280,960", "--window-position=40,40"],
    });
    try {
      const nativeOptions = {
        viewport: null,
        acceptDownloads: true,
        baseURL: "http://localhost:3000",
      };
      // Remove the desktop test descriptor's DPR emulation for the actual native-window measurement.
      Object.defineProperty(nativeOptions, "deviceScaleFactor", {
        value: undefined,
        enumerable: true,
      });
      const context = await browser.newContext(nativeOptions);
      const page = await context.newPage();
      const control = getControlClient();
      await control.reset();
      await control.connectSlot(1);
      await loginAsOwner(page, context, control);
      await populated(page);
      await page.getByRole("button", { name: "Đóng sample.png", exact: true }).click();
      await page.evaluate(() => {
        document.title = "PPV PH-I01 actual native zoom";
      });
      const cdp = await context.newCDPSession(page);
      const window = await cdp.send("Browser.getWindowForTarget");
      if (window.bounds.width === undefined) throw new Error("Native window bounds unavailable");
      const initial = await page.evaluate(() => ({ inner: innerWidth, dpr: devicePixelRatio }));
      await cdp.send("Browser.setWindowBounds", {
        windowId: window.windowId,
        bounds: { width: window.bounds.width + 960 - initial.inner, height: 960 },
      });
      fs.writeFileSync(
        info.outputPath("native-window-entry.json"),
        JSON.stringify({ initial, bounds: window.bounds }),
        { flag: "wx" }
      );
      await expect.poll(() => page.evaluate(() => innerWidth)).toBe(960);
      const baseline = await page.evaluate(() => ({
        inner: innerWidth,
        dpr: devicePixelRatio,
        outer: outerWidth,
      }));
      expect(baseline.inner).toBe(960);
      const record = (name: string, data: object) => {
        fs.writeFileSync(
          info.outputPath(name + ".json"),
          JSON.stringify({ at: new Date().toISOString(), percent, ...data }, null, 2),
          { flag: "wx" }
        );
      };
      const requireNativeCapture = async (stage: string, timeout = 30000) => {
        const requestedAt = Date.now();
        const deadline = requestedAt + timeout;
        fs.writeFileSync(
          path.join(artifact, stage + "-requested.json"),
          JSON.stringify({
            stage,
            percent,
            title: "PPV PH-I01 actual native zoom",
            baseline,
            url: "http://localhost:3000/personal/storage/google-drive",
            requestedAt,
            deadline,
          }),
          { flag: "wx" }
        );
        const confirmed = path.join(artifact, stage + "-confirmed.json");
        const blocked = path.join(artifact, "native-policy-blocked.json");
        while (!fs.existsSync(confirmed) && !fs.existsSync(blocked) && Date.now() < deadline) {
          await new Promise((resolve) => setTimeout(resolve, 200));
        }
        const checkedAt = Date.now();
        const confirmation = fs.existsSync(confirmed)
          ? (JSON.parse(fs.readFileSync(confirmed, "utf8")) as {
              stage?: string;
              observedAt?: number;
              nativeResponse?: string;
            })
          : null;
        const confirmationMtime = confirmation ? fs.statSync(confirmed).mtimeMs : null;
        let status = "acknowledged";
        if (fs.existsSync(blocked)) status = "policy-blocked";
        else if (!confirmation || confirmationMtime === null) status = "missing";
        else if (checkedAt > deadline || confirmationMtime > deadline) status = "late";
        else if (
          confirmationMtime < requestedAt ||
          confirmation.stage !== stage ||
          typeof confirmation.observedAt !== "number" ||
          confirmation.observedAt < requestedAt ||
          confirmation.observedAt > checkedAt ||
          typeof confirmation.nativeResponse !== "string" ||
          !confirmation.nativeResponse
        )
          status = "invalid";
        record(stage + "-acknowledgement", {
          stage,
          status,
          requestedAt,
          deadline,
          checkedAt,
          confirmationMtime,
          confirmation,
        });
        if (status !== "acknowledged")
          throw new Error(
            "Mandatory policy-compliant uncropped native capture unavailable: " +
              stage +
              " (" +
              status +
              ")"
          );
      };
      record("baseline-geometry", { baseline, edgeVersion: browser.version() });
      await requireNativeCapture("baseline-" + percent, 60000);
      await requireNativeCapture("zoom-" + percent, percent === 200 ? 160000 : 60000);
      const geometry = await page.evaluate(() => {
        const main = document.querySelector("#main-content")!;
        return {
          inner: innerWidth,
          dpr: devicePixelRatio,
          outer: outerWidth,
          documentWidth: document.documentElement.scrollWidth,
          mainWidth: main.clientWidth,
          mainScrollWidth: main.scrollWidth,
          heading: document.querySelector("h1")?.getBoundingClientRect().width,
        };
      });
      record("zoom-geometry", { geometry, baseline });
      expect(Math.abs(geometry.dpr / baseline.dpr - percent / 100)).toBeLessThan(0.03);
      expect(Math.abs(geometry.inner - 960 / (percent / 100))).toBeLessThanOrEqual(1);
      expect(geometry.documentWidth).toBeLessThanOrEqual(geometry.inner + 1);
      expect(geometry.mainScrollWidth).toBeLessThanOrEqual(geometry.mainWidth + 1);
      let controlIndex = 0;
      for (const target of [
        page.getByRole("button", { name: /Tài khoản 1/ }),
        page.getByRole("button", { name: /Tài khoản 2/ }),
        page.getByPlaceholder("Tìm trong Drive hiện tại..."),
        page.getByRole("combobox", { name: "Loại tệp" }),
        page.getByLabel("Từ ngày"),
        page.getByLabel("Đến ngày"),
      ]) {
        await tabTo(page, target);
        record(
          "keyboard-control-" + ++controlIndex,
          await target.evaluate((e) => ({
            tag: e.tagName,
            label: e.getAttribute("aria-label") || e.textContent,
            focused: document.activeElement === e,
            rect: e.getBoundingClientRect().toJSON(),
          }))
        );
      }
      await requireNativeCapture("controls-" + percent);
      const newButton = page.getByRole("button", { name: "Mới", exact: true });
      await tabTo(page, newButton);
      await page.keyboard.press("Enter");
      await expect(page.getByRole("menu")).toBeVisible();
      await page.keyboard.press("End");
      const folderItem = page.getByRole("menuitem", { name: "Thư mục mới" });
      await expect(folderItem).toBeFocused();
      record(
        "keyboard-menu",
        await folderItem.evaluate((e) => ({
          label: e.textContent,
          focused: document.activeElement === e,
          rect: e.getBoundingClientRect().toJSON(),
        }))
      );
      await requireNativeCapture("menu-" + percent);
      await page.keyboard.press("Enter");
      await expect(page.getByLabel("Tên thư mục")).toBeFocused();
      await page.keyboard.type("reflow/name");
      await page.keyboard.press("Enter");
      const error = page.getByRole("alert").filter({ hasText: "Tên thư mục" });
      await expect(error).toBeVisible();
      await error.scrollIntoViewIfNeeded();
      record(
        "dialog-error",
        await error.evaluate((e) => ({
          message: e.textContent,
          rect: e.getBoundingClientRect().toJSON(),
          focusedId: document.activeElement?.id,
        }))
      );
      await page.screenshot({
        path: info.outputPath("dialog-error-viewport-" + percent + ".png"),
        fullPage: false,
      });
      await requireNativeCapture("dialog-error-" + percent);
      const create = page.getByRole("button", { name: "Tạo thư mục", exact: true });
      await tabTo(page, create);
      const reachable = await create.boundingBox();
      const viewportHeight = await page.evaluate(() => innerHeight);
      const dialog = await page.getByRole("dialog").evaluate((e) => ({
        scrollWidth: e.scrollWidth,
        clientWidth: e.clientWidth,
      }));
      const focus = await create.evaluate((e) => ({
        focused: document.activeElement === e,
        outline: getComputedStyle(e).outline,
        rect: e.getBoundingClientRect().toJSON(),
        dialogScrollTop: e.closest('[role="dialog"]')!.scrollTop,
      }));
      record("dialog-action", { reachable, viewportHeight, dialog, focus });
      expect(reachable!.y).toBeGreaterThanOrEqual(0);
      expect(reachable!.y + reachable!.height).toBeLessThanOrEqual(viewportHeight);
      expect(dialog.scrollWidth).toBeLessThanOrEqual(dialog.clientWidth + 1);
      await expect(create).toBeFocused();
      await page.screenshot({
        path: info.outputPath("dialog-action-viewport-" + percent + ".png"),
        fullPage: false,
      });
      await requireNativeCapture("dialog-action-" + percent);
      await page.keyboard.press("Escape");
      await expect(newButton).toBeFocused();
      await expect(page.getByRole("dialog")).toBeHidden();
      record(
        "focus-return",
        await newButton.evaluate((e) => ({
          focused: document.activeElement === e,
          outline: getComputedStyle(e).outline,
          rect: e.getBoundingClientRect().toJSON(),
        }))
      );
      await requireNativeCapture("focus-return-" + percent);
      await page.screenshot({
        path: info.outputPath("actual-zoom-" + percent + ".png"),
        fullPage: false,
      });
      await cdp.detach();
    } finally {
      await browser.close();
    }
  });
}

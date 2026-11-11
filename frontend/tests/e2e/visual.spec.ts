import fs from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";
import { expect, test, type Page, type TestInfo } from "@playwright/test";
import { getControlClient } from "../fixtures/spring-fixture";
import { loginAsOwner } from "../fixtures/transport";

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const samplePngPath = path.resolve(__dirname, "../fixtures/assets/sample.png");

async function captureVisual(page: Page, info: TestInfo) {
  const metadata = await page.evaluate(() => ({
    viewport: { width: innerWidth, height: innerHeight },
    dpr: devicePixelRatio,
    browserLocale: navigator.language,
    appLanguage: document.documentElement.lang,
    timeZone: Intl.DateTimeFormat().resolvedOptions().timeZone,
    computedFont: getComputedStyle(document.body).fontFamily,
    mainBounds: document.querySelector("main")!.getBoundingClientRect().toJSON(),
    state:
      "real connected slot 1, disconnected slot 2, created folder + uploaded file, safe thumbnail fallback",
  }));
  const cdp = await page.context().newCDPSession(page);
  await cdp.send("DOM.enable");
  await cdp.send("CSS.enable");
  const { root } = await cdp.send("DOM.getDocument");
  const { nodeId } = await cdp.send("DOM.querySelector", { nodeId: root.nodeId, selector: "h1" });
  const fonts = await cdp.send("CSS.getPlatformFontsForNode", { nodeId });
  await cdp.detach();
  fs.writeFileSync(
    info.outputPath("visual-environment.json"),
    JSON.stringify({ ...metadata, platformFonts: fonts.fonts }, null, 2),
    { flag: "wx" }
  );
  const target = info.outputPath("populated-desktop.png");
  await page.screenshot({ path: target, fullPage: true });
  expect(fs.existsSync(target)).toBe(true);
}

async function populateGridWithFolderAndFile(page: Page) {
  // 1. Ensure root folder
  const ensurePrompt = page.getByRole("button", { name: "Khởi tạo thư mục PPV" });
  await expect(ensurePrompt).toBeVisible({ timeout: 15000 });
  await ensurePrompt.click();
  await expect(ensurePrompt).toBeHidden({ timeout: 15000 });

  // 2. Create child folder
  const newBtn = page.getByRole("button", { name: "Mới", exact: true });
  await expect(newBtn).toBeVisible({ timeout: 15000 });
  await expect(newBtn).toBeEnabled({ timeout: 15000 });
  await newBtn.click();

  const newFolderItem = page.getByRole("menuitem", { name: "Thư mục mới" });
  await newFolderItem.click();

  const nameInput = page.getByLabel("Tên thư mục");
  await expect(nameInput).toBeVisible();
  await nameInput.fill("Tài liệu lưu trữ");

  const createBtn = page.getByRole("button", { name: "Tạo thư mục" });
  await createBtn.click();
  await expect(page.locator(".grid").getByText("Tài liệu lưu trữ")).toBeVisible({ timeout: 15000 });

  // 3. Upload file
  await newBtn.click();
  const [fileChooser] = await Promise.all([
    page.waitForEvent("filechooser"),
    page.getByRole("menuitem", { name: "Tải tệp lên" }).click(),
  ]);
  await fileChooser.setFiles(samplePngPath);

  // Wait for upload completion and both folder and file populated in the grid
  await expect(page.locator(".grid").getByText("sample.png")).toBeVisible({ timeout: 20000 });

  // Wait for DOM to settle
  await page.waitForTimeout(1000);
}

test.describe("Desktop Visual Fidelity E2E", () => {
  test.beforeEach(async () => {
    const control = getControlClient();
    await control.reset();
  });

  test("F-05: Desktop visual verification at 1366x768 CSS px", async ({
    page,
    context,
  }, testInfo) => {
    const control = getControlClient();
    await control.connectSlot(1, "google-sub-slot-1");
    await loginAsOwner(page, context, control);

    await page.setViewportSize({ width: 1366, height: 768 });
    await expect(page.getByRole("heading", { name: "Google Drive Explorer" })).toBeVisible();

    await populateGridWithFolderAndFile(page);

    // Verify key UI elements matching approved desktop v0.3
    await expect(page.getByRole("button", { name: "Mới", exact: true })).toBeVisible();
    await expect(page.getByPlaceholder("Tìm trong Drive hiện tại...")).toBeVisible();
    await expect(page.getByText("Tài khoản 1")).toBeVisible();
    await expect(page.getByText("Dung lượng Google Drive")).toBeVisible();

    // Save screenshot for audit receipt
    await captureVisual(page, testInfo);
  });

  test("F-05: Desktop visual verification at 1536x864 CSS px", async ({
    page,
    context,
  }, testInfo) => {
    const control = getControlClient();
    await control.connectSlot(1, "google-sub-slot-1");
    await loginAsOwner(page, context, control);

    await page.setViewportSize({ width: 1536, height: 864 });
    await expect(page.getByRole("heading", { name: "Google Drive Explorer" })).toBeVisible();

    await populateGridWithFolderAndFile(page);

    await captureVisual(page, testInfo);
  });
});

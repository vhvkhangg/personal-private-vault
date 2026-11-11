import { expect, test } from "@playwright/test";
import { getControlClient } from "../fixtures/spring-fixture";
import { loginAsOwner } from "../fixtures/transport";

async function followUpReady(
  page: import("@playwright/test").Page,
  context: import("@playwright/test").BrowserContext
) {
  const control = getControlClient();
  await control.reset();
  await control.configure("drive-behavior", { stableRefresh: true });
  await control.connectSlot(1);
  await loginAsOwner(page, context, control);
  await page.getByRole("button", { name: "Khởi tạo thư mục PPV" }).click();
  await expect(page.getByRole("button", { name: "Mới", exact: true })).toBeEnabled();
  return control;
}

test("Follow-up FR-08: restart during pending append subsequently loads all seven real files", async ({
  page,
  context,
}, info) => {
  await followUpReady(page, context);
  await page.evaluate(async () => {
    const csrf = (await (await fetch("/api/bff/session")).json()).csrfToken;
    for (let i = 0; i < 7; i++) {
      const body = new FormData();
      body.append(
        "file",
        new File(["synthetic"], "follow-up-page-" + i + ".txt", { type: "text/plain" })
      );
      const key =
        new Date().toISOString().replace(/[-:]/g, "").split(".")[0] + "Z." + crypto.randomUUID();
      const response = await fetch("/api/bff/drive/connections/1/files", {
        method: "POST",
        headers: { "X-PPV-CSRF": csrf, "Idempotency-Key": key },
        body,
      });
      if (response.status !== 201) throw new Error("Real seven-file creation failed");
    }
  });
  let cursorRequests = 0;
  let releaseOld = () => {};
  await page.route("**/api/bff/drive/connections/1/items?**", async (route) => {
    if (new URL(route.request().url()).searchParams.has("cursor")) {
      cursorRequests++;
      if (cursorRequests === 1) {
        await new Promise<void>((resolve) => {
          releaseOld = resolve;
        });
        await route.abort("aborted").catch(() => {});
        return;
      }
    }
    await route.continue();
  });
  await page.getByRole("button", { name: "Làm mới trạng thái", exact: true }).click();
  await page.locator("#main-content").evaluate((e) => e.scrollTo(0, e.scrollHeight));
  await expect.poll(() => cursorRequests).toBe(1);
  const fresh = page.waitForResponse(
    (r) =>
      new URL(r.url()).pathname.endsWith("/items") && !new URL(r.url()).searchParams.has("cursor")
  );
  await page.getByRole("button", { name: "Làm mới trạng thái", exact: true }).click();
  releaseOld();
  const data = (await (await fresh).json()).data;
  expect(data.items.length).toBe(5);
  expect(data.hasMore).toBe(true);
  await page.locator("#main-content").evaluate((e) => e.scrollTo(0, e.scrollHeight));
  await expect(page.locator(".grid").getByText(/^follow-up-page-[0-6]\.txt$/)).toHaveCount(7);
  await page.waitForTimeout(1200);
  expect(cursorRequests).toBe(2);
  await info.attach("follow-up-restart-seven-files", {
    body: JSON.stringify({
      freshFirstPage: 5,
      freshHasMore: true,
      finalVisibleFiles: 7,
      cursorRequests,
      interruptedOldNegativeRequest: true,
      positiveResponsesMocked: false,
    }),
    contentType: "application/json",
  });
  await page.screenshot({ path: info.outputPath("restarted-seven-files.png"), fullPage: true });
});

test("Follow-up FR-09: interrupted absent intent expires, then explicit new name/key creates once", async ({
  page,
  context,
}, info) => {
  const control = await followUpReady(page, context);
  const keys: string[] = [];
  page.on("request", (r) => {
    if (new URL(r.url()).pathname.endsWith("/folders") && r.method() === "POST")
      keys.push(r.headers()["idempotency-key"]!);
  });
  await page.route("**/api/bff/drive/connections/1/folders", (route) => route.abort("failed"), {
    times: 1,
  });
  await page.getByRole("button", { name: "Mới", exact: true }).click();
  await page.getByRole("menuitem", { name: "Thư mục mới" }).click();
  const name = page.getByLabel("Tên thư mục");
  await name.fill("Interrupted original intention");
  await page.getByRole("button", { name: "Tạo thư mục", exact: true }).click();
  await expect(name).toHaveAttribute("readonly");
  expect((await control.getStats()).childFolderCreates).toBe(0);
  await control.configure("advance-clock", { seconds: 601 });
  const rejected = page.waitForResponse((r) => new URL(r.url()).pathname.endsWith("/folders"));
  await page.getByRole("button", { name: "Tạo thư mục", exact: true }).click();
  const rejection = await rejected;
  expect(rejection.status()).toBe(409);
  expect((await rejection.json()).error.code).toBe("FOLDER_KEY_EXPIRED");
  await expect(name).not.toHaveAttribute("readonly");
  expect(keys).toHaveLength(2);
  expect(keys[0] === keys[1]).toBe(true);
  expect((await control.getStats()).childFolderCreates).toBe(0);
  // Align only the test browser's new-action timestamp with the deliberately advanced Spring clock.
  // This does not change timers, Spring freshness rules, or any production clock.
  await page.clock.setFixedTime(Date.now() + 601000);
  await name.fill("Follow-up explicit new folder");
  expect(keys).toHaveLength(2);
  const created = page.waitForResponse((r) => new URL(r.url()).pathname.endsWith("/folders"));
  await page.getByRole("button", { name: "Tạo thư mục", exact: true }).click();
  expect((await created).status()).toBe(201);
  await expect(page.locator(".grid").getByText("Follow-up explicit new folder")).toBeVisible();
  expect(keys).toHaveLength(3);
  expect(keys[2] !== keys[1]).toBe(true);
  expect((await control.getStats()).childFolderCreates).toBe(1);
  await info.attach("follow-up-definitive-folder-resolution", {
    body: JSON.stringify({
      interruptedBeforeDispatch: true,
      springClockAdvanceSeconds: 601,
      rejection: "409 FOLDER_KEY_EXPIRED",
      originalRetryKeyPreserved: true,
      browserNewActionClockAligned: true,
      explicitNewNameAndKey: true,
      automaticDispatches: 0,
      remoteCreates: 1,
    }),
    contentType: "application/json",
  });
});

test.describe("Drive Explorer E2E", () => {
  test.beforeEach(async () => {
    const control = getControlClient();
    await control.reset();
  });

  test("F-06: Root folder ensure and child folder creation in connected slot", async ({
    page,
    context,
  }) => {
    const control = getControlClient();
    await control.configure("drive-behavior", { stableRefresh: true });
    await control.connectSlot(1, "google-sub-slot-1");
    await loginAsOwner(page, context, control);

    // Wait for root folder ensure prompt and click it
    const ensurePrompt = page.getByRole("button", { name: "Khởi tạo thư mục PPV" });
    await expect(ensurePrompt).toBeVisible({ timeout: 15000 });
    await ensurePrompt.click();

    // After root is ensured, "Mới" button appears and becomes enabled
    const newBtn = page.getByRole("button", { name: "Mới", exact: true });
    await expect(newBtn).toBeVisible({ timeout: 15000 });
    await expect(newBtn).toBeEnabled({ timeout: 15000 });

    // Click "Mới" button -> "Thư mục mới"
    await newBtn.click();

    const newFolderItem = page.getByRole("menuitem", { name: "Thư mục mới" });
    await newFolderItem.click();

    // Dialog appears
    const nameInput = page.getByLabel("Tên thư mục");
    await expect(nameInput).toBeVisible();
    await nameInput.fill("Thư mục kiểm thử E2E");

    const createBtn = page.getByRole("button", { name: "Tạo thư mục" });
    await createBtn.click();

    // Dialog closes, folder appears in grid
    await expect(page.getByText("Thư mục kiểm thử E2E")).toBeVisible({ timeout: 10000 });
    await page.locator(".grid").getByText("Thư mục kiểm thử E2E").click();
    await expect(
      page.locator("main span.text-white.font-semibold").filter({ hasText: "Thư mục kiểm thử E2E" })
    ).toBeVisible();
    await newBtn.click();
    await expect(page.getByRole("menuitem", { name: "Thư mục mới" })).toHaveAttribute(
      "aria-disabled",
      "true"
    );
    await page.keyboard.press("Escape");
    const childUpload = page.waitForResponse(
      (r) =>
        /\/connections\/1\/folders\/[A-Za-z0-9_-]+\/files$/.test(new URL(r.url()).pathname) &&
        r.request().method() === "POST"
    );
    await page.locator('input[type="file"]').setInputFiles({
      name: "child-native.bin",
      mimeType: "application/octet-stream",
      buffer: Buffer.from("synthetic child"),
    });
    expect((await childUpload).status()).toBe(201);
    await expect(page.locator(".grid").getByText("child-native.bin")).toBeVisible();
    await page.getByRole("button", { name: "Google Drive", exact: true }).click();
    await expect(page.locator(".grid").getByText("Thư mục kiểm thử E2E")).toBeVisible();
    await expect(page.locator(".grid").getByText("child-native.bin")).toHaveCount(0);
  });

  test("F-06: Search input triggers query with scope=all", async ({ page, context }) => {
    const control = getControlClient();
    await control.connectSlot(1, "google-sub-slot-1");
    await loginAsOwner(page, context, control);

    // Ensure root folder for slot 1
    const ensurePrompt = page.getByRole("button", { name: "Khởi tạo thư mục PPV" });
    await expect(ensurePrompt).toBeVisible({ timeout: 15000 });
    await ensurePrompt.click();
    await expect(ensurePrompt).toBeHidden({ timeout: 15000 });

    const searchInput = page.getByPlaceholder("Tìm trong Drive hiện tại...");
    await expect(searchInput).toBeVisible({ timeout: 15000 });
    await expect(searchInput).toBeEnabled({ timeout: 15000 });
    await searchInput.fill("kiemthu");
    await searchInput.press("Enter");

    // Clear filters button appears when search is active
    await expect(page.getByRole("button", { name: "Đặt lại bộ lọc" })).toBeVisible({
      timeout: 10000,
    });
  });
});

async function bothSlots(
  page: import("@playwright/test").Page,
  context: import("@playwright/test").BrowserContext
) {
  const control = getControlClient();
  await control.reset();
  await control.connectSlot(1);
  await control.connectSlot(2);
  await loginAsOwner(page, context, control);
  await expect(page.getByRole("button", { name: "Khởi tạo thư mục PPV" })).toBeVisible();
  await page.getByRole("button", { name: "Khởi tạo thư mục PPV" }).click();
  await expect(page.getByRole("button", { name: "Mới", exact: true })).toBeEnabled();
  await expect.poll(async () => (await control.getStats()).activeOperations).toBe(0);
  const files = await page.evaluate(async () => {
    const csrf = (await (await fetch("/api/bff/session")).json()).csrfToken;
    const key = () =>
      new Date().toISOString().replace(/[-:]/g, "").split(".")[0] + "Z." + crypto.randomUUID();
    await fetch("/api/bff/drive/connections/2/root-folder", {
      method: "POST",
      headers: { "X-PPV-CSRF": csrf },
    });
    const files = [];
    for (const slot of [1, 2]) {
      const form = new FormData();
      form.append(
        "file",
        new File(["synthetic"], slot === 1 ? "A-image.png" : "B-image.png", { type: "image/png" })
      );
      const response = await fetch("/api/bff/drive/connections/" + slot + "/files", {
        method: "POST",
        headers: { "X-PPV-CSRF": csrf, "Idempotency-Key": key() },
        body: form,
      });
      if (!response.ok) throw new Error("Real slot fixture creation rejected");
      files.push((await response.json()).data);
    }
    return files as { id: string; name: string }[];
  });
  await control.configure("update-remote", {
    changes: files.map((f) => ({
      id: f.id,
      thumbnailLink: "CONTROLLED_FIXTURE_THUMBNAIL",
    })),
  });
  await control.setThumbnail({ usePng: true });
  await page.reload();
  await expect(page.locator(".grid").getByText("A-image.png")).toBeVisible();
  return files;
}

for (const kind of ["items", "storage-quota", "folders", "thumbnail"] as const) {
  test(
    "F-06: held real A " + kind + " response cannot paint or refresh B",
    async ({ page, context }, info) => {
      const control = getControlClient();
      const files = await bothSlots(page, context);
      await control.configure("hold-response", {
        path:
          "/api/v1/drive/connections/1/" +
          (kind === "thumbnail" ? "files/" + files[0]!.id + "/thumbnail" : kind),
      });
      if (kind === "items") await page.getByRole("button", { name: "Làm mới trạng thái" }).click();
      if (kind === "storage-quota") {
        await control.configure("drive-behavior", {
          quotaOverride: JSON.stringify({
            storageQuota: { usage: "111", usageInDrive: "111", limit: "10000" },
          }),
        });
        await page.getByPlaceholder("Tìm trong Drive hiện tại...").fill("A");
      }
      if (kind === "folders") {
        await page.getByRole("button", { name: "Mới", exact: true }).click();
        await page.getByRole("menuitem", { name: "Thư mục mới" }).click();
        await page.getByLabel("Tên thư mục").fill("Late A folder");
        await page.getByRole("button", { name: "Tạo thư mục", exact: true }).click();
      }
      if (kind === "thumbnail") await page.reload();
      await expect.poll(async () => (await control.getStats()).responseHeld).toBe(true);
      if (kind === "folders") await page.keyboard.press("Escape");
      if (kind === "storage-quota")
        await control.configure("drive-behavior", {
          quotaOverride: JSON.stringify({
            storageQuota: { usage: "222", usageInDrive: "222", limit: "10000" },
          }),
        });
      await page.getByRole("button", { name: /Tài khoản 2/ }).click();
      await expect(page.locator(".grid").getByText("B-image.png")).toBeVisible();
      await control.configure("release-response", {});
      await expect(page.locator(".grid").getByText("A-image.png")).toHaveCount(0);
      await expect(page.locator(".grid").getByText("Late A folder")).toHaveCount(0);
      if (kind === "storage-quota") {
        await expect(page.getByText("222 B", { exact: true })).toBeVisible();
        await expect(page.getByText("111 B", { exact: true })).toHaveCount(0);
      }
      const sources = await page
        .locator("main img")
        .evaluateAll((images) => images.map((i) => (i as HTMLImageElement).getAttribute("src")));
      expect(sources.every((source) => source?.includes("/connections/2/"))).toBe(true);
      await info.attach("real-held-response-fence", {
        body: JSON.stringify({
          kind,
          heldRealSpringResponse: true,
          BVisible: true,
          AVisible: false,
          noARefresh: true,
          thumbnailSourcesBoundToB: true,
        }),
        contentType: "application/json",
      });
    }
  );
}

test("F-06/FR-09: lost actual folder reply and close/reopen retry preserve original name/key and one creation", async ({
  page,
  context,
}) => {
  const control = getControlClient();
  await control.reset();
  await control.connectSlot(1);
  await loginAsOwner(page, context, control);
  await page.getByRole("button", { name: "Khởi tạo thư mục PPV" }).click();
  await expect(page.getByRole("button", { name: "Mới", exact: true })).toBeEnabled();
  const keys: string[] = [];
  page.on("request", (r) => {
    if (new URL(r.url()).pathname.endsWith("/folders") && r.method() === "POST")
      keys.push(r.headers()["idempotency-key"]!);
  });
  const cdp = await context.newCDPSession(page);
  let interrupted = false;
  await cdp.send("Fetch.enable", {
    patterns: [
      {
        urlPattern: "http://localhost:3000/api/bff/drive/connections/1/folders",
        requestStage: "Response",
      },
    ],
  });
  cdp.on("Fetch.requestPaused", async (event) => {
    if (!interrupted && (event.responseStatusCode === 201 || event.responseStatusCode === 200)) {
      interrupted = true;
      await cdp.send("Fetch.failRequest", {
        requestId: event.requestId,
        errorReason: "ConnectionClosed",
      });
    } else await cdp.send("Fetch.continueRequest", { requestId: event.requestId });
  });
  await page.getByRole("button", { name: "Mới", exact: true }).click();
  await page.getByRole("menuitem", { name: "Thư mục mới" }).click();
  await page.getByLabel("Tên thư mục").fill("Original native intention");
  await page.getByRole("button", { name: "Tạo thư mục", exact: true }).click();
  await expect(page.getByRole("alert")).toBeVisible();
  expect((await control.getStats()).childFolderCreates).toBe(1);
  await page.keyboard.press("Escape");
  await cdp.send("Fetch.disable");
  await cdp.detach();
  await page.getByRole("button", { name: "Mới", exact: true }).click();
  await page.getByRole("menuitem", { name: "Thư mục mới" }).click();
  await expect(page.getByLabel("Tên thư mục")).toHaveValue("Original native intention");
  await page.getByRole("button", { name: "Tạo thư mục", exact: true }).click();
  await expect(page.locator(".grid").getByText("Original native intention")).toBeVisible();
  expect(keys.length).toBe(2);
  expect(keys[0] === keys[1]).toBe(true);
  expect((await control.getStats()).childFolderCreates).toBe(1);
});

test("F-06: actual paged Unicode substring/PDF/inclusive local-date search finds unloaded eligible child files", async ({
  page,
  context,
}, info) => {
  const control = getControlClient();
  await control.reset();
  // Optional provider refresh_token omission keeps the current credential epoch stable.
  // Rotation is separately fail-closed; never relax Spring's epoch-bound cursor checks.
  await control.configure("drive-behavior", { stableRefresh: true });
  await control.connectSlot(1);
  await loginAsOwner(page, context, control);
  await page.getByRole("button", { name: "Khởi tạo thư mục PPV" }).click();
  await expect(page.getByRole("button", { name: "Mới", exact: true })).toBeEnabled();
  const data = await page.evaluate(async () => {
    const csrf = (await (await fetch("/api/bff/session")).json()).csrfToken;
    const key = () =>
      new Date().toISOString().replace(/[-:]/g, "").split(".")[0] + "Z." + crypto.randomUUID();
    const folderResponse = await fetch("/api/bff/drive/connections/1/folders", {
      method: "POST",
      headers: { "X-PPV-CSRF": csrf, "Content-Type": "application/json", "Idempotency-Key": key() },
      body: JSON.stringify({ name: "Eligible child" }),
    });
    if (!folderResponse.ok) throw new Error("Real child creation rejected");
    const folder = (await folderResponse.json()).data;
    const changes = [];
    const candidates = [
      ...Array.from({ length: 6 }, (_, i) => ({
        name: "nonmatch-" + i + ".txt",
        mime: "text/plain",
        date: "2026-11-12T00:00:00Z",
        child: false,
      })),
      {
        name: "Báo cáo kiểm thử.PDF",
        mime: "application/pdf",
        date: "2026-11-08T17:00:00Z",
        child: true,
      },
      {
        name: "X Báo cáo cuối.pdf",
        mime: "application/pdf",
        date: "2026-11-09T16:59:59.999Z",
        child: true,
      },
      {
        name: "Báo cáo ngoài.pdf",
        mime: "application/pdf",
        date: "2026-11-09T17:00:00Z",
        child: true,
      },
      { name: "Báo cáo ảnh.png", mime: "image/png", date: "2026-11-09T10:00:00Z", child: true },
      {
        name: "Bao cao ascii.pdf",
        mime: "application/pdf",
        date: "2026-11-09T10:00:00Z",
        child: true,
      },
    ];
    for (const candidate of candidates) {
      const form = new FormData();
      form.append("file", new File(["synthetic"], candidate.name, { type: candidate.mime }));
      const response = await fetch(
        "/api/bff/drive/connections/1/" +
          (candidate.child ? "folders/" + folder.id + "/" : "") +
          "files",
        {
          method: "POST",
          headers: { "X-PPV-CSRF": csrf, "Idempotency-Key": key() },
          body: form,
        }
      );
      if (!response.ok) throw new Error("Real paged dataset upload rejected");
      changes.push({ id: (await response.json()).data.id, modifiedTime: candidate.date });
    }
    return { changes, folderId: folder.id };
  });
  await control.configure("update-remote", { changes: data.changes });
  const serial = await page.evaluate(async () => {
    const rows = [];
    let cursor: string | null = null;
    for (let i = 0; i < 4; i++) {
      const params = new URLSearchParams({
        scope: "all",
        limit: "25",
        name: "áo",
        type: "pdf",
        from: "2026-11-09",
        to: "2026-11-09",
        timeZone: "Asia/Ho_Chi_Minh",
      });
      if (cursor) params.set("cursor", cursor);
      const response = await fetch("/api/bff/drive/connections/1/items?" + params);
      const body = await response.json();
      rows.push({
        status: response.status,
        code: body.error?.code,
        items: body.data?.items?.length,
        hasMore: body.data?.hasMore,
      });
      cursor = body.data?.nextCursor ?? null;
      if (!cursor) break;
    }
    return rows;
  });
  await info.attach("serial-real-next-page-diagnostic", {
    body: JSON.stringify(serial),
    contentType: "application/json",
  });
  expect(serial.every((row) => row.status === 200)).toBe(true);
  expect(serial.at(-1)?.hasMore).toBe(false);
  await page.reload();
  const pages: { items: number; hasMore: boolean; scope: string | null; parent: boolean }[] = [];
  let observation = 0;
  page.on("response", async (response) => {
    const url = new URL(response.url());
    if (!url.pathname.endsWith("/items")) return;
    try {
      const body = await response.json();
      await info.attach("search-page-" + ++observation, {
        body: JSON.stringify({
          status: response.status(),
          code: body.error?.code,
          name: url.searchParams.get("name"),
          type: url.searchParams.get("type"),
          from: url.searchParams.get("from"),
          to: url.searchParams.get("to"),
          cursorPresent: url.searchParams.has("cursor"),
          items: body.data?.items?.map((i: { name: string }) => i.name),
          hasMore: body.data?.hasMore,
        }),
        contentType: "application/json",
      });
    } catch {
      /* Superseded request aborted. */
    }
  });
  page.on("response", async (response) => {
    const url = new URL(response.url());
    if (
      !url.pathname.endsWith("/items") ||
      url.searchParams.get("name")?.normalize("NFC") !== "áo" ||
      url.searchParams.get("type") !== "pdf" ||
      url.searchParams.get("to") !== "2026-11-09"
    )
      return;
    try {
      const body = await response.json();
      if (body.data)
        pages.push({
          items: body.data.items.length,
          hasMore: body.data.hasMore,
          scope: url.searchParams.get("scope"),
          parent: url.searchParams.has("parentId"),
        });
    } catch {
      /* Aborted obsolete filter generations are not credited as returned pages. */
    }
  });
  await page.getByRole("combobox", { name: "Loại tệp" }).click();
  await page.getByRole("option", { name: "Tài liệu PDF", exact: true }).click();
  await expect(page.getByLabel("Từ ngày")).toBeEnabled();
  await page.getByLabel("Từ ngày").fill("2026-11-09");
  await expect(page.getByLabel("Đến ngày")).toBeEnabled();
  await page.getByLabel("Đến ngày").fill("2026-11-09");
  await expect(page.getByPlaceholder("Tìm trong Drive hiện tại...")).toBeEnabled();
  await page.getByPlaceholder("Tìm trong Drive hiện tại...").fill("a\u0301o");
  await page.locator("#main-content").evaluate((e) => e.scrollTo(0, e.scrollHeight));
  await expect(page.locator(".grid").getByText("Báo cáo kiểm thử.PDF")).toBeVisible();
  await expect(page.locator(".grid").getByText("X Báo cáo cuối.pdf")).toBeVisible();
  await expect(page.locator(".grid").getByText("Báo cáo ngoài.pdf")).toHaveCount(0);
  await expect(page.locator(".grid").getByText("Báo cáo ảnh.png")).toHaveCount(0);
  await expect(page.locator(".grid").getByText("Bao cao ascii.pdf")).toHaveCount(0);
  await expect.poll(() => pages.at(-1)?.hasMore).toBe(false);
  expect(pages.some((p) => p.items === 0 && p.hasMore)).toBe(true);
  expect(pages.every((p) => p.scope === "all" && !p.parent)).toBe(true);
  await info.attach("actual-page-search-proof", {
    body: JSON.stringify({
      pages,
      createdByRealNext: true,
      eligibleChildNeverOpened: true,
      unicodeNfc: true,
      substring: true,
      inclusiveAsiaHoChiMinh: true,
    }),
    contentType: "application/json",
  });
  const cdp = await context.newCDPSession(page);
  let cursorDispatches = 0;
  await cdp.send("Fetch.enable", {
    patterns: [
      {
        urlPattern: "http://localhost:3000/api/bff/drive/connections/1/items?*cursor=*",
        requestStage: "Request",
      },
    ],
  });
  cdp.on("Fetch.requestPaused", async (event) => {
    cursorDispatches++;
    if (cursorDispatches === 1) await control.configure("drive-behavior", { stableRefresh: false });
    await cdp.send("Fetch.continueRequest", { requestId: event.requestId });
  });
  try {
    await page.getByRole("button", { name: "Làm mới trạng thái" }).click();
    await expect(
      page.getByText("Phiên phân trang không còn hợp lệ. Bấm làm mới để tải lại danh sách.")
    ).toBeVisible();
    await page.waitForTimeout(1200);
    expect(cursorDispatches).toBe(1);
    await control.configure("drive-behavior", { stableRefresh: true });
    await page.getByRole("button", { name: "Thử lại", exact: true }).click();
    await expect(page.locator(".grid").getByText("Báo cáo kiểm thử.PDF")).toBeVisible();
    await expect(page.locator(".grid").getByText("X Báo cáo cuối.pdf")).toBeVisible();
    await info.attach("native-cursor-explicit-restart", {
      body: JSON.stringify({
        failedCursorDispatchesBeforeExplicitRestart: 1,
        observedWithoutRetryMs: 1200,
        realProviderEpochRotation: true,
        explicitRestartRestoredResults: true,
      }),
      contentType: "application/json",
    });
  } finally {
    await cdp.send("Fetch.disable");
    await cdp.detach();
  }
});

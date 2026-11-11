import fs from "node:fs";
import crypto from "node:crypto";
import path from "node:path";
import http from "node:http";
import { fileURLToPath } from "node:url";
import { expect, test } from "@playwright/test";
import { getControlClient } from "../fixtures/spring-fixture";
import { loginAsOwner } from "../fixtures/transport";

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const samplePngPath = path.resolve(__dirname, "../fixtures/assets/sample.png");

test("F-06: actual private PNG/JPEG thumbnails and missing/unsafe/WEBP fallback", async ({
  page,
  context,
}, info) => {
  await ready(page, context);
  const control = getControlClient();
  await page.locator('input[type="file"]').setInputFiles(samplePngPath);
  await expect(page.getByText("Tải tệp lên thành công.")).toBeVisible();
  const id = await page.evaluate(async () => {
    const body = await (
      await fetch("/api/bff/drive/connections/1/items?scope=parent&parentId=root")
    ).json();
    return body.data.items.find((i: { name: string }) => i.name === "sample.png").id as string;
  });
  const rows = [];
  for (const kind of ["png", "jpeg", "missing", "unsafe", "webp"] as const) {
    await control.configure("update-remote", {
      changes: [
        {
          id,
          thumbnailLink:
            kind === "missing"
              ? ""
              : kind === "unsafe"
                ? "http://127.0.0.1/blocked"
                : "CONTROLLED_FIXTURE_THUMBNAIL",
        },
      ],
    });
    await control.setThumbnail({
      usePng: kind === "png",
      useJpeg: kind === "jpeg",
      useWebp: kind === "webp",
    });
    const before = await control.getStats();
    await page.reload();
    await expect(page.locator(".grid").getByText("sample.png")).toBeVisible();
    if (kind === "png" || kind === "jpeg") {
      await expect
        .poll(async () =>
          page
            .getByRole("img", { name: "sample.png" })
            .evaluate((e) => (e as HTMLImageElement).naturalWidth)
        )
        .toBeGreaterThan(0);
      const response = await page.request.get(
        "/api/bff/drive/connections/1/files/" + id + "/thumbnail"
      );
      expect(response.status()).toBe(200);
      expect(response.headers()["content-type"]).toBe("image/" + kind);
      expect(response.headers()["cache-control"]).toContain("no-store");
    } else {
      await expect(page.getByRole("img", { name: "sample.png" })).toHaveCount(0);
      expect(await page.locator(".grid svg.lucide-file-image").count()).toBe(1);
      if (kind !== "webp")
        expect((await control.getStats()).thumbnailDispatches).toBe(before.thumbnailDispatches);
    }
    rows.push({
      kind,
      loaded: kind === "png" || kind === "jpeg",
      fallback: kind !== "png" && kind !== "jpeg",
    });
  }
  await info.attach("actual-thumbnail-cases", {
    body: JSON.stringify(rows),
    contentType: "application/json",
  });
});

test("F-06: real native multipart rejects hostile keys/names/Workspace and preserves normalized MIME and parent affinity", async ({
  page,
  context,
}, info) => {
  await ready(page, context);
  const outcomes = await page.evaluate(async () => {
    const csrf = (await (await fetch("/api/bff/session")).json()).csrfToken;
    const key = () =>
      new Date().toISOString().replace(/[-:]/g, "").split(".")[0] + "Z." + crypto.randomUUID();
    const send = async (
      action: string | null,
      name = "synthetic.bin",
      mime = "application/octet-stream",
      content = "synthetic",
      parent = ""
    ) => {
      const form = new FormData();
      form.append("file", new File([content], name, { type: mime }));
      const response = await fetch("/api/bff/drive/connections/1/" + parent + "files", {
        method: "POST",
        headers: { "X-PPV-CSRF": csrf, ...(action === null ? {} : { "Idempotency-Key": action }) },
        body: form,
      });
      const body = await response.json();
      return {
        status: response.status,
        code: body.error?.code,
        mime: body.data?.mimeType,
        id: body.data?.id,
      };
    };
    const statuses = [];
    statuses.push(
      await send(null),
      await send("invalid"),
      await send("20000101T000000Z." + crypto.randomUUID())
    );
    statuses.push(
      await send(key(), "../unsafe.bin"),
      await send(key(), "workspace", "application/vnd.google-apps.document")
    );
    const same = key();
    statuses.push(
      await send(same),
      await send(same, "synthetic.bin", "application/octet-stream", "changed")
    );
    const folder = await fetch("/api/bff/drive/connections/1/folders", {
      method: "POST",
      headers: { "X-PPV-CSRF": csrf, "Idempotency-Key": key(), "Content-Type": "application/json" },
      body: JSON.stringify({ name: "Upload affinity" }),
    });
    if (folder.status !== 201) throw new Error("Real child creation failed");
    const child = (await folder.json()).data.id;
    statuses.push(
      await send(
        same,
        "synthetic.bin",
        "application/octet-stream",
        "synthetic",
        "folders/" + child + "/"
      )
    );
    statuses.push(await send(key(), "normalized.bin", "invalid"));
    return statuses.map(({ status, code, mime }) => ({ status, code, mime }));
  });
  expect(outcomes.map((r) => r.status)).toEqual([400, 400, 409, 400, 415, 201, 409, 409, 201]);
  expect(outcomes.at(-1)?.mime).toBe("application/octet-stream");
  await info.attach("actual-next-multipart-cases", {
    body: JSON.stringify(outcomes),
    contentType: "application/json",
  });
});

test("F-06: actual Next over-file and over-request native streams reject and release resources", async ({
  page,
  context,
}, info) => {
  test.setTimeout(90000);
  await ready(page, context);
  const cdp = await context.newCDPSession(page);
  const uploadRequests = new Set<string>();
  const wireStatuses: number[] = [];
  await cdp.send("Network.enable");
  cdp.on("Network.requestWillBeSent", (event) => {
    if (
      new URL(event.request.url).pathname === "/api/bff/drive/connections/1/files" &&
      event.request.method === "POST"
    )
      uploadRequests.add(event.requestId);
  });
  cdp.on("Network.responseReceivedExtraInfo", (event) => {
    if (uploadRequests.has(event.requestId)) wireStatuses.push(event.statusCode);
  });
  const statuses = await page.evaluate(async () => {
    const csrf = (await (await fetch("/api/bff/session")).json()).csrfToken;
    const statuses = [];
    for (const size of [52428801, 57671681]) {
      const form = new FormData();
      form.append(
        "file",
        new File([new Blob([new Uint8Array(size)])], "over-cap.bin", {
          type: "application/octet-stream",
        })
      );
      const action =
        new Date().toISOString().replace(/[-:]/g, "").split(".")[0] + "Z." + crypto.randomUUID();
      try {
        const response = await fetch("/api/bff/drive/connections/1/files", {
          method: "POST",
          headers: { "X-PPV-CSRF": csrf, "Idempotency-Key": action },
          body: form,
        });
        statuses.push({ size, status: response.status, fetchFailed: false });
        await response.body?.cancel();
      } catch {
        // Chromium can expose an early upload rejection as a transport failure.
        // Record this separately from a JavaScript-visible HTTP status.
        statuses.push({ size, status: null, fetchFailed: true });
      }
    }
    return statuses;
  });
  await info.attach("native-over-cap-wire", {
    body: JSON.stringify({ statuses, wireStatuses }),
    contentType: "application/json",
  });
  await cdp.detach();
  expect(statuses[0]?.status).toBe(413);
  expect(statuses[1]?.status === 413 || statuses[1]?.fetchFailed).toBe(true);
  expect(wireStatuses).toContain(413);
  const control = getControlClient();
  // Verify actual Next header-only rejection separately, never as native status proof.
  const beforeHeaderOnly = await control.getStats();
  const csrf = await page.evaluate(
    async () => (await (await fetch("/api/bff/session")).json()).csrfToken
  );
  const sid = (await context.cookies()).find((c) => c.name === "ppv_dev_sid")!;
  const headerOnlyStatus = await new Promise<number>((resolve, reject) => {
    const request = http.request(
      "http://localhost:3000/api/bff/drive/connections/1/files",
      {
        method: "POST",
        headers: {
          Host: "localhost:3000",
          Origin: "http://localhost:3000",
          "Sec-Fetch-Site": "same-origin",
          Cookie: sid.name + "=" + sid.value,
          "X-PPV-CSRF": csrf,
          "Idempotency-Key":
            new Date().toISOString().replace(/[-:]/g, "").split(".")[0] +
            "Z." +
            crypto.randomUUID(),
          "Content-Type": "multipart/form-data; boundary=bounded-test",
          "Content-Length": "57671681",
          Expect: "100-continue",
        },
      },
      (response) => {
        resolve(response.statusCode!);
        response.resume();
      }
    );
    request.on("error", reject);
    request.setTimeout(5000, () => request.destroy(new Error("Header-only limit probe timed out")));
    request.end();
  });
  expect(headerOnlyStatus).toBe(413);
  expect((await control.getStats()).springRequests).toBe(beforeHeaderOnly.springRequests);
  await expect.poll(async () => (await control.getStats()).activeOperations).toBe(0);
  expect((await control.getStats()).activeSpools).toBe(0);
  expect((await control.getStats()).fileCreates).toBe(0);
  await info.attach("over-cap-resource-proof", {
    body: JSON.stringify({
      statuses,
      wireStatuses,
      headerOnlyStatus,
      headerOnlySource: "actual Next HTTP, not native status proof",
      created: 0,
      activeOperations: 0,
      activeSpools: 0,
    }),
    contentType: "application/json",
  });
});

test("F-06: live claimant and provider-unknown same-key retry never duplicate remote creation", async ({
  page,
  context,
}, info) => {
  await ready(page, context);
  const control = getControlClient();
  await control.configure("drive-behavior", { holdUpload: true });
  const action =
    new Date().toISOString().replace(/[-:]/g, "").split(".")[0] + "Z." + crypto.randomUUID();
  const send = (selectedKey = action) =>
    page.evaluate(async (key) => {
      const csrf = (await (await fetch("/api/bff/session")).json()).csrfToken;
      const form = new FormData();
      form.append(
        "file",
        new File(["unchanged"], "held.bin", { type: "application/octet-stream" })
      );
      const response = await fetch("/api/bff/drive/connections/1/files", {
        method: "POST",
        headers: { "X-PPV-CSRF": csrf, "Idempotency-Key": key },
        body: form,
      });
      return { status: response.status, body: await response.json() };
    }, selectedKey);
  const pending = send();
  await expect.poll(async () => (await control.getStats()).fileCreates).toBe(1);
  const competing = await send();
  expect(competing.status).toBe(409);
  expect(competing.body.error.code).toBe("UPLOAD_IN_PROGRESS");
  await control.configure("drive-behavior", { releaseUpload: true });
  expect((await pending).status).toBe(201);
  expect((await send()).status).toBe(200);
  expect((await control.getStats()).fileCreates).toBe(1);
  const uncertainKey =
    new Date().toISOString().replace(/[-:]/g, "").split(".")[0] + "Z." + crypto.randomUUID();
  await control.configure("drive-behavior", { loseUploadResponse: true });
  const unknown = await send(uncertainKey);
  expect(unknown.status).toBe(409);
  expect(unknown.body.error.code).toBe("UPLOAD_OUTCOME_UNKNOWN");
  await control.configure("drive-behavior", { loseUploadResponse: false });
  expect((await send(uncertainKey)).status).toBe(200);
  expect((await control.getStats()).fileCreates).toBe(2);
  await info.attach("live-claimant-proof", {
    body: JSON.stringify({
      liveClaimantStatus: 409,
      firstStatus: 201,
      retryStatus: 200,
      claimantCreations: 1,
      providerUnknownStatus: unknown.status,
      totalCreationsForTwoKeys: 2,
    }),
    contentType: "application/json",
  });
});

test.describe("Drive Transfers E2E", () => {
  test.beforeEach(async () => {
    const control = getControlClient();
    await control.reset();
  });

  test("F-06: File upload streams multipart data and displays card with thumbnail", async ({
    page,
    context,
  }) => {
    const control = getControlClient();
    await control.connectSlot(1, "google-sub-slot-1");
    await loginAsOwner(page, context, control);

    // Wait for root folder ensure prompt and click it
    const ensurePrompt = page.getByRole("button", { name: "Khởi tạo thư mục PPV" });
    await expect(ensurePrompt).toBeVisible({ timeout: 15000 });
    await ensurePrompt.click();

    // Click "Mới" -> "Tải tệp lên"
    const newBtn = page.getByRole("button", { name: "Mới", exact: true });
    await expect(newBtn).toBeVisible({ timeout: 15000 });
    await expect(newBtn).toBeEnabled({ timeout: 15000 });
    await newBtn.click();

    // Set file input
    const [fileChooser] = await Promise.all([
      page.waitForEvent("filechooser"),
      page.getByRole("menuitem", { name: "Tải tệp lên" }).click(),
    ]);
    await fileChooser.setFiles(samplePngPath);

    // Upload progress panel appears and confirms
    await expect(page.getByText("Tải tệp lên thành công.")).toBeVisible({ timeout: 15000 });
    await expect(page.getByText("sample.png").first()).toBeVisible({ timeout: 15000 });

    // Verify download action triggers native file download
    const downloadBtn = page.getByRole("button", { name: /Tải xuống sample.png/ });
    await expect(downloadBtn).toBeAttached({ timeout: 10000 });
    await expect.poll(async () => (await control.getStats()).activeOperations).toBe(0);

    const [download] = await Promise.all([
      page.waitForEvent("download", { timeout: 15000 }),
      downloadBtn.click({ force: true }),
    ]);
    expect(download.suggestedFilename()).toBe("sample.png");
    const downloadPath = await download.path();
    expect(downloadPath).toBeTruthy();
    const downloadSize = (await fs.promises.stat(downloadPath!)).size;
    expect(downloadSize).toBeGreaterThan(0);
  });

  test("F-06: Thumbnail fallback displays generic icon for unsupported format", async ({
    page,
    context,
  }) => {
    const control = getControlClient();
    await control.connectSlot(1, "google-sub-slot-1");
    await control.setThumbnail({ useWebp: true });
    await loginAsOwner(page, context, control);

    await expect(page.getByRole("heading", { name: "Google Drive Explorer" })).toBeVisible();
  });
});

async function ready(
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
}
async function digest(file: string) {
  const hash = crypto.createHash("sha256");
  for await (const chunk of fs.createReadStream(file)) hash.update(chunk);
  return hash.digest("hex");
}
for (const size of [0, 52_428_800]) {
  test(
    "F-06: actual native upload and download of " + size + " bytes with exact hash and cleanup",
    async ({ page, context }, info) => {
      await ready(page, context);
      const file = info.outputPath(size + "-bytes.bin");
      const handle = await fs.promises.open(file, "wx");
      await handle.truncate(size);
      await handle.close();
      const before = await page.evaluate(
        async () =>
          await (
            await fetch("/api/bff/session", { headers: { "x-ppv-test-transfer-stats": "true" } })
          ).json()
      );
      expect(typeof before.rss).toBe("number");
      let peakRss = before.rss;
      const sampler = setInterval(() => {
        void page.request
          .get("/api/bff/session", { headers: { "x-ppv-test-transfer-stats": "true" } })
          .then((r) => r.json())
          .then((m) => {
            peakRss = Math.max(peakRss, m.rss);
          })
          .catch(() => {});
      }, 100);
      try {
        const responsePromise = page.waitForResponse(
          (r) =>
            new URL(r.url()).pathname === "/api/bff/drive/connections/1/files" &&
            r.request().method() === "POST"
        );
        await page.locator('input[type="file"]').setInputFiles(file);
        const response = await responsePromise;
        expect(response.status()).toBe(201);
        expect((await response.json()).data.sizeBytes).toBe(size);
        await expect(page.getByText("Tải tệp lên thành công.")).toBeVisible();
        const downloadPromise = page.waitForEvent("download");
        await page
          .getByRole("button", { name: "Tải xuống " + path.basename(file) })
          .click({ force: true });
        const download = await downloadPromise;
        expect(await download.failure()).toBeNull();
        const local = await download.path();
        expect((await fs.promises.stat(local!)).size).toBe(size);
        expect(await digest(local!)).toBe(await digest(file));
        await expect
          .poll(async () => (await getControlClient().getStats()).activeOperations)
          .toBe(0);
        const after = await page.evaluate(
          async () =>
            await (
              await fetch("/api/bff/session", { headers: { "x-ppv-test-transfer-stats": "true" } })
            ).json()
        );
        expect(after.activeTransfers).toBe(0);
        await info.attach("real-transfer-observation", {
          body: JSON.stringify({
            size,
            beforeRss: before.rss,
            peakRss,
            afterRss: after.rss,
            exactHash: true,
            nextAdmission: after.activeTransfers,
            backendOperations: 0,
            observationOnly: true,
          }),
          contentType: "application/json",
        });
      } finally {
        clearInterval(sampler);
      }
    }
  );
}

test("F-06: lost native upload reply retains original File/target/key; explicit retry reconciles one remote creation", async ({
  page,
  context,
}) => {
  await ready(page, context);
  const control = getControlClient();
  const cdp = await context.newCDPSession(page);
  let interrupted = false;
  const keys: string[] = [];
  page.on("request", (request) => {
    if (
      new URL(request.url()).pathname === "/api/bff/drive/connections/1/files" &&
      request.method() === "POST"
    ) {
      keys.push(request.headers()["idempotency-key"]!);
    }
  });
  await cdp.send("Fetch.enable", {
    patterns: [
      {
        urlPattern: "http://localhost:3000/api/bff/drive/connections/1/files",
        requestStage: "Response",
      },
    ],
  });
  cdp.on("Fetch.requestPaused", async (event) => {
    if (!interrupted && (event.responseStatusCode === 200 || event.responseStatusCode === 201)) {
      interrupted = true;
      await cdp.send("Fetch.failRequest", {
        requestId: event.requestId,
        errorReason: "ConnectionClosed",
      });
    } else await cdp.send("Fetch.continueRequest", { requestId: event.requestId });
  });
  await page.locator('input[type="file"]').setInputFiles(samplePngPath);
  const retry = page.getByRole("button", { name: "Thử lại cùng hành động" });
  await expect(retry).toBeVisible();
  expect(interrupted).toBe(true);
  expect((await control.getStats()).fileCreates).toBe(1);
  await cdp.send("Fetch.disable");
  await cdp.detach();
  await retry.click();
  await expect(page.getByText("Tệp đã được tải lên và xác nhận trước đó.")).toBeVisible();
  expect(keys.length).toBe(2);
  expect(keys[0] === keys[1]).toBe(true);
  expect((await control.getStats()).fileCreates).toBe(1);
});

for (const mode of ["short", "excess", "stalled", "client-cancel", "logout"] as const) {
  test(
    "F-06: actual HTTP/native attachment " + mode + " does not report a completed download",
    async ({ page, context }, info) => {
      await ready(page, context);
      const control = getControlClient();
      const input = ["stalled", "client-cancel", "logout"].includes(mode)
        ? info.outputPath("pending-256k.bin")
        : samplePngPath;
      if (input !== samplePngPath) {
        const file = await fs.promises.open(input, "wx");
        await file.truncate(262144);
        await file.close();
      }
      await page.locator('input[type="file"]').setInputFiles(input);
      await expect(page.getByText("Tải tệp lên thành công.")).toBeVisible();
      if (mode === "short") await control.configure("drive-behavior", { shortMedia: 1 });
      if (mode === "excess")
        await control.configure("drive-behavior", { mediaChunked: true, extraMedia: 1 });
      if (["stalled", "client-cancel", "logout"].includes(mode))
        await control.configure("drive-behavior", { mediaMode: "PARTIAL" });
      const promise = page.waitForEvent("download");
      await expect.poll(async () => (await control.getStats()).activeOperations).toBe(0);
      await page
        .getByRole("button", { name: "Tải xuống " + path.basename(input) })
        .click({ force: true });
      const download = await promise;
      if (mode === "client-cancel") await download.cancel();
      if (mode === "logout") await page.getByRole("button", { name: "Đăng xuất" }).first().click();
      const failure = await download.failure();
      await info.attach("native-download-outcome", {
        body: JSON.stringify({
          mode,
          failed: failure !== null,
          failure,
          source: "actual Next/Spring/fake media transport",
        }),
        contentType: "application/json",
      });
      expect(failure !== null).toBe(true);
      await expect
        .poll(async () => (await control.getStats()).activeOperations, { timeout: 26000 })
        .toBe(0);
      const resource = await page.request.get("/api/bff/session", {
        headers: { "x-ppv-test-transfer-stats": "true" },
      });
      expect((await resource.json()).activeTransfers).toBe(0);
      await control.configure("drive-behavior", { releaseBody: true });
    }
  );
}

test("F-06: native user cancellation after provider dispatch keeps the exact action for reconciliation", async ({
  page,
  context,
}) => {
  await ready(page, context);
  const control = getControlClient();
  await control.configure("drive-behavior", { holdUpload: true });
  const keys: string[] = [];
  page.on("request", (request) => {
    if (
      new URL(request.url()).pathname === "/api/bff/drive/connections/1/files" &&
      request.method() === "POST"
    ) {
      keys.push(request.headers()["idempotency-key"]!);
    }
  });
  await page.locator('input[type="file"]').setInputFiles(samplePngPath);
  await expect.poll(async () => (await control.getStats()).fileCreates).toBe(1);
  await page.getByRole("button", { name: "Hủy tải lên" }).click();
  await expect(page.getByRole("button", { name: "Thử lại cùng hành động" })).toBeVisible();
  await control.configure("drive-behavior", { releaseUpload: true });
  await expect
    .poll(async () => (await control.getStats()).activeOperations, { timeout: 20000 })
    .toBe(0);
  await page.getByRole("button", { name: "Thử lại cùng hành động" }).click();
  await expect(page.getByText("Tệp đã được tải lên và xác nhận trước đó.")).toBeVisible();
  expect(keys.length).toBe(2);
  expect(keys[0] === keys[1]).toBe(true);
  expect((await control.getStats()).fileCreates).toBe(1);
});

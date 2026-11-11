import { defineConfig, devices } from "@playwright/test";
import path from "node:path";

export default defineConfig({
  testDir: "./tests/e2e",
  outputDir: path.join(process.env.PPV_H1F_ARTIFACT_DIR || "test-results", "playwright"),
  reporter: [
    ["list"],
    [
      "json",
      { outputFile: path.join(process.env.PPV_H1F_ARTIFACT_DIR || "test-results", "results.json") },
    ],
  ],
  fullyParallel: false,
  workers: 1,
  retries: 0,
  timeout: 45000,
  expect: {
    timeout: 10000,
  },
  use: {
    baseURL: "http://localhost:3000",
    trace: "off",
    screenshot: "only-on-failure",
    video: "off",
    channel: "msedge",
    viewport: { width: 1366, height: 768 },
    ignoreHTTPSErrors: true,
  },
  projects: [
    {
      name: "desktop-edge-1366",
      use: {
        ...devices["Desktop Edge"],
        channel: "msedge",
        viewport: { width: 1366, height: 768 },
      },
    },
    {
      name: "desktop-edge-1536",
      use: {
        ...devices["Desktop Edge"],
        channel: "msedge",
        viewport: { width: 1536, height: 864 },
      },
    },
  ],
});

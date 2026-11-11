import { spawn } from "node:child_process";
import fs from "node:fs";
import net from "node:net";
import http from "node:http";
import path from "node:path";
import process from "node:process";
import crypto from "node:crypto";
import { fileURLToPath } from "node:url";

const repoRoot = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "../..");
const frontendDir = path.join(repoRoot, "frontend");
const args = Object.fromEntries(
  process.argv.slice(2).map((a) => {
    const i = a.indexOf("=");
    return [a.slice(2, i), a.slice(i + 1)];
  })
);
const artifact = args["artifact-dir"];
const fixture = args["fixture-dir"];
if (!artifact || !fixture) throw new Error("Unique --artifact-dir and --fixture-dir are required");
const out = path.resolve(repoRoot, artifact);
if (!out.startsWith(path.join(repoRoot, "logs", "gdi26-s3-h1-f") + path.sep))
  throw new Error("Invalid artifact directory");
fs.mkdirSync(out); // fail on collision, never overwrite historical artifacts
const hashes = {};
function inventory(directory, prefix) {
  for (const entry of fs.readdirSync(directory, { withFileTypes: true })) {
    if (
      ["node_modules", ".next", "test-results", "playwright-report", "coverage"].includes(
        entry.name
      )
    )
      continue;
    if (entry.name.startsWith(".env") && entry.name !== ".env.example") continue;
    const file = path.join(directory, entry.name);
    const key = prefix + "/" + entry.name;
    if (entry.isDirectory()) inventory(file, key);
    else hashes[key] = crypto.createHash("sha256").update(fs.readFileSync(file)).digest("hex");
  }
}
inventory(frontendDir, "frontend");
inventory(path.resolve(repoRoot, fixture), "compiled-fixture");
for (const file of ["BUILD_ID", "build-manifest.json", "server/app-paths-manifest.json"]) {
  const full = path.join(frontendDir, ".next", file);
  hashes["frontend/.next/" + file] = crypto
    .createHash("sha256")
    .update(fs.readFileSync(full))
    .digest("hex");
}
fs.writeFileSync(path.join(out, "source-hashes.json"), JSON.stringify(hashes, null, 2), {
  flag: "wx",
});
const compiled = JSON.parse(
  fs
    .readFileSync(path.resolve(repoRoot, fixture, "source-hashes.json"), "utf8")
    .replace(/^\uFEFF/, "")
);
for (const name of ["H1FLocalFixture.java", "H1FFixtureConfiguration.java"]) {
  if (compiled[name] !== hashes["frontend/tests/fixtures/spring/" + name])
    throw new Error("Compiled fixture source drift: " + name);
}
process.env.PPV_H1F_ARTIFACT_DIR = out;
process.env.PPV_H1F_FIXTURE_DIR = path.resolve(repoRoot, fixture);
process.env.PATH = path.dirname(process.execPath) + path.delimiter + process.env.PATH;
const { launchNextServer, launchSpringFixture } = await import("./fixture.mjs");
const groups = {
  signin: "signin",
  setup: "setup",
  drive: "drive-oauth",
  explorer: "explorer",
  transfers: "transfers",
  races: "session-races",
  accessibility: "accessibility",
  visual: "visual",
};
if (!groups[args.group]) throw new Error("One explicit group is required");
for (const port of [19080, 3000]) {
  for (const host of ["127.0.0.1", "::1"]) {
    await new Promise((resolve, reject) => {
      const server = net.createServer();
      server.once("error", reject);
      server.listen({ port, host, ipv6Only: host === "::1" }, () => server.close(resolve));
    });
  }
}
console.log("Preflight IPv4/IPv6 19080 and 3000 free; selected Node " + process.version);
const { chromium } = await import("@playwright/test");
const edge = await chromium.launch({ channel: "msedge" });
fs.writeFileSync(
  path.join(out, "native-toolchain.json"),
  JSON.stringify(
    { node: process.version, nodePath: process.execPath, edge: edge.version() },
    null,
    2
  ),
  { flag: "wx" }
);
await edge.close();
let spring, next, nodeControl;
const killOwned = (proc) =>
  new Promise((resolve) => {
    if (!proc || proc.exitCode !== null) return resolve();
    const killer = spawn("taskkill", ["/PID", String(proc.pid), "/T", "/F"], {
      windowsHide: true,
      stdio: "ignore",
    });
    killer.once("exit", resolve);
    killer.once("error", resolve);
  });
let cleaning;
async function cleanup() {
  if (cleaning) return cleaning;
  cleaning = (async () => {
    nodeControl?.close();
    await killOwned(next);
    if (spring && spring.process.exitCode === null) {
      try {
        await fetch(`http://127.0.0.1:${spring.controlPort}/api/test/shutdown`, {
          method: "POST",
          signal: globalThis.AbortSignal.timeout(5000),
        });
      } catch {
        /* A crashed owned fixture may already be gone. */
      }
      const end = Date.now() + 15000;
      while (spring.process.exitCode === null && Date.now() < end)
        await new Promise((r) => setTimeout(r, 100));
      await killOwned(spring.process);
    }
    fs.writeFileSync(
      path.join(out, "cleanup.json"),
      JSON.stringify(
        {
          nextExit: next?.exitCode,
          springExit: spring?.process.exitCode,
          ownedPathsRemoved: spring?.fixtureData?.ownedPaths?.every((p) => !fs.existsSync(p)),
          completedAt: new Date().toISOString(),
        },
        null,
        2
      ),
      { flag: "wx" }
    );
  })();
  return cleaning;
}
process.once("SIGINT", () => void cleanup());
process.once("SIGTERM", () => void cleanup());
try {
  spring = await launchSpringFixture();
  fs.writeFileSync(
    path.join(out, "fixture-metadata.json"),
    JSON.stringify(spring.fixtureData, null, 2),
    { flag: "wx" }
  );
  next = await launchNextServer(spring.controlPort);
  nodeControl = http.createServer(async (request, response) => {
    if (request.method !== "POST" || request.url !== "/restart-next") {
      response.writeHead(404).end();
      return;
    }
    const oldPid = next.pid;
    await killOwned(next);
    next = await launchNextServer(spring.controlPort);
    fs.appendFileSync(
      path.join(out, "node-restarts.jsonl"),
      JSON.stringify({ oldPid, newPid: next.pid, at: new Date().toISOString() }) + "\n"
    );
    response
      .writeHead(200, { "Content-Type": "application/json" })
      .end(JSON.stringify({ restarted: true }));
  });
  await new Promise((resolve) => nodeControl.listen(0, "127.0.0.1", resolve));
  process.env.PPV_NODE_CONTROL_PORT = String(nodeControl.address().port);
  const child = spawn(
    process.execPath,
    [
      path.join(frontendDir, "node_modules/@playwright/test/cli.js"),
      "test",
      `tests/e2e/${groups[args.group]}.spec.ts`,
      ...(args.filter ? ["--grep", args.filter] : []),
      ...(args.group === "visual" ? [] : ["--project=desktop-edge-1366"]),
    ],
    {
      cwd: frontendDir,
      env: { ...process.env, PPV_CONTROL_PORT: String(spring.controlPort), CI: "1" },
      stdio: "inherit",
    }
  );
  process.exitCode = await new Promise((resolve, reject) => {
    child.once("exit", (c) => resolve(c ?? 1));
    child.once("error", reject);
  });
} catch (error) {
  console.error(error instanceof Error ? error.message : "Fixture failed");
  process.exitCode = 1;
} finally {
  await cleanup();
}

import { spawn } from "node:child_process";
import fs from "node:fs";
import path from "node:path";
import process from "node:process";
import { fileURLToPath } from "node:url";

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const repoRoot = path.resolve(__dirname, "../..");
const logsDir = process.env.PPV_H1F_FIXTURE_DIR;
if (!logsDir) throw new Error("PPV_H1F_FIXTURE_DIR must select a fresh compiled fixture directory");
const outDir = path.resolve(logsDir, "fixture-classes");
const cpFile = path.resolve(logsDir, "fixture-classpath.txt");

export async function launchSpringFixture() {
  if (!fs.existsSync(cpFile) || !fs.existsSync(outDir)) {
    throw new Error(
      "Fixture not compiled. Run compile-fixture.ps1 first before launching fixture."
    );
  }

  const depsClasspath = fs.readFileSync(cpFile, "utf-8").trim();
  const backendClasses = path.resolve(repoRoot, "backend/target/classes");
  const backendTestClasses = path.resolve(repoRoot, "backend/target/test-classes");
  const fullClasspath = `${depsClasspath};${backendClasses};${backendTestClasses};${outDir}`;

  console.log("[fixture.mjs] Spawning Spring Boot H1FLocalFixture on port 19080...");
  const javaProc = spawn(
    "java",
    ["-cp", fullClasspath, "com.vhvkhangg.personalprivatevault.fixture.H1FLocalFixture"],
    {
      cwd: repoRoot,
      stdio: ["ignore", "pipe", "inherit"],
    }
  );

  return new Promise((resolve, reject) => {
    let controlPort = 0;
    let fixtureData = null;
    let buffer = "";

    const timeout = setTimeout(() => {
      javaProc.kill();
      reject(new Error("Timeout waiting for Spring fixture readiness"));
    }, 120_000);

    javaProc.stdout.on("data", (chunk) => {
      const text = chunk.toString();
      buffer = (buffer + text).slice(-16384);
      process.stdout.write(text);

      const jsonMatch = buffer.match(/PPV_FIXTURE_JSON:(.*)\r?\n/);
      if (jsonMatch && jsonMatch[1]) {
        try {
          fixtureData = JSON.parse(jsonMatch[1]);
        } catch {
          // ignore
        }
      }

      const match = buffer.match(/PPV_FIXTURE_READY (\d+)/);
      if (match && match[1]) {
        controlPort = parseInt(match[1], 10);
        clearTimeout(timeout);
        resolve({
          process: javaProc,
          controlPort,
          fixtureData,
        });
      }
    });

    javaProc.on("error", (err) => {
      clearTimeout(timeout);
      reject(err);
    });

    javaProc.on("exit", (code) => {
      clearTimeout(timeout);
      if (code !== 0) {
        reject(new Error(`Spring fixture exited prematurely with code ${code}`));
      }
    });
  });
}

export async function launchNextServer(controlPort) {
  console.log("[fixture.mjs] Spawning Next.js production server on port 3000...");
  const nextBin = path.resolve(repoRoot, "frontend/node_modules/next/dist/bin/next");
  const nodeDir = path.dirname(process.execPath);
  const nextProc = spawn(process.execPath, [nextBin, "start", "-p", "3000"], {
    cwd: path.resolve(repoRoot, "frontend"),
    env: {
      ...process.env,
      PATH: `${nodeDir}${path.delimiter}${process.env.PATH || ""}`,
      PORT: "3000",
      PPV_SPRING_ORIGIN: "http://localhost:19080",
      PPV_H1F_TEST_FIXTURE: "1",
      PPV_OWNER_SETUP_ENABLED: "true",
      PPV_CONTROL_PORT: String(controlPort),
      PPV_H1F_ARTIFACT_DIR: process.env.PPV_H1F_ARTIFACT_DIR,
      NEXT_TELEMETRY_DISABLED: "1",
      NODE_ENV: "production",
    },
    stdio: ["ignore", "pipe", "inherit"],
  });

  return new Promise((resolve, reject) => {
    const timeout = setTimeout(() => {
      nextProc.kill();
      reject(new Error("Timeout waiting for Next.js server readiness"));
    }, 60_000);

    nextProc.stdout.on("data", (chunk) => {
      const text = chunk.toString();
      process.stdout.write(text);
      if (text.includes("Ready in") || text.includes("started server on")) {
        clearTimeout(timeout);
        resolve(nextProc);
      }
    });

    nextProc.on("error", (err) => {
      clearTimeout(timeout);
      reject(err);
    });

    nextProc.on("exit", (code) => {
      clearTimeout(timeout);
      if (code !== 0) {
        reject(new Error(`Next.js exited prematurely with code ${code}`));
      }
    });
  });
}

// Standalone execution
if (process.argv[1] === __filename) {
  let spring = null;
  let next = null;

  const cleanup = () => {
    console.log("[fixture.mjs] Cleaning up fixture processes...");
    if (next) next.kill();
    if (spring) spring.process.kill();
  };

  process.on("SIGINT", cleanup);
  process.on("SIGTERM", cleanup);
  process.on("exit", cleanup);

  try {
    spring = await launchSpringFixture();
    console.log(
      `[fixture.mjs] Spring fixture ready on 19080 (control port: ${spring.controlPort})`
    );
    if (process.argv.includes("--with-next")) {
      next = await launchNextServer(spring.controlPort);
      console.log("[fixture.mjs] Next.js ready on http://localhost:3000");
    }
  } catch (err) {
    console.error("[fixture.mjs] Failed:", err);
    cleanup();
    process.exit(1);
  }
}

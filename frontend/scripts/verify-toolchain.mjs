import fs from "node:fs";
import path from "node:path";
import { execFileSync } from "node:child_process";

const EXPECTED_NODE = "v26.13.0";
const EXPECTED_NPM = "12.2.2";

console.log("=== PPV GDI26-S3-H1-F Toolchain Verification ===");

// 1. Node version
const actualNode = process.version;
console.log(`Node version: ${actualNode} (expected: ${EXPECTED_NODE})`);
if (actualNode !== EXPECTED_NODE) {
  console.error(`ERROR: Node version mismatch: expected ${EXPECTED_NODE}, got ${actualNode}`);
  process.exit(1);
}

// 2. npm version
let actualNpm = "";
try {
  actualNpm = execFileSync(
    process.execPath,
    [path.resolve(path.dirname(process.execPath), "node_modules/npm/bin/npm-cli.js"), "-v"],
    { encoding: "utf8" }
  ).trim();
} catch (e) {
  console.error("ERROR: Failed to query npm version:", e.message);
  process.exit(1);
}
console.log(`npm version: ${actualNpm} (expected: ${EXPECTED_NPM})`);
if (actualNpm !== EXPECTED_NPM) {
  console.error(`ERROR: npm version mismatch: expected ${EXPECTED_NPM}, got ${actualNpm}`);
  process.exit(1);
}

// 3. package.json & package-lock.json
const frontendDir = path.resolve(import.meta.dirname, "..");
const pkgJsonPath = path.join(frontendDir, "package.json");
const lockJsonPath = path.join(frontendDir, "package-lock.json");

if (!fs.existsSync(pkgJsonPath)) {
  console.error("ERROR: package.json not found");
  process.exit(1);
}
if (!fs.existsSync(lockJsonPath)) {
  console.error("ERROR: package-lock.json not found");
  process.exit(1);
}

const pkg = JSON.parse(fs.readFileSync(pkgJsonPath, "utf8"));
const lock = JSON.parse(fs.readFileSync(lockJsonPath, "utf8"));

if (lock.lockfileVersion !== 3) {
  console.error(`ERROR: lockfileVersion is ${lock.lockfileVersion}, expected 3`);
  process.exit(1);
}

const expectedDirectPins = {
  // dependencies
  "class-variance-authority": "0.7.1",
  clsx: "2.1.1",
  "lucide-react": "1.70.0",
  next: "16.4.5",
  "radix-ui": "1.8.1",
  react: "19.3.1",
  "react-dom": "19.3.1",
  "server-only": "0.0.1",
  "tailwind-merge": "3.7.0",
  // devDependencies
  "@axe-core/playwright": "4.14.0",
  "@eslint/js": "10.0.1",
  "@next/eslint-plugin-next": "16.4.5",
  "@playwright/test": "1.65.0",
  "@tailwindcss/postcss": "4.3.5",
  "@testing-library/dom": "10.4.2",
  "@testing-library/jest-dom": "7.0.1",
  "@testing-library/react": "16.3.3",
  "@testing-library/user-event": "14.6.8",
  "@types/node": "26.6.6",
  "@types/react": "19.3.0",
  "@types/react-dom": "19.3.0",
  "@vitejs/plugin-react": "6.2.1",
  eslint: "10.14.0",
  "eslint-plugin-react-hooks": "7.1.1",
  jsdom: "31.0.0",
  postcss: "8.5.32",
  prettier: "3.9.10",
  tailwindcss: "4.3.5",
  typescript: "6.0.3",
  "typescript-eslint": "8.72.1",
  vite: "8.3.6",
  vitest: "5.1.0",
};

let errors = 0;
for (const [name, expectedVer] of Object.entries(expectedDirectPins)) {
  const inPkg = pkg.dependencies?.[name] || pkg.devDependencies?.[name];
  if (inPkg !== expectedVer) {
    console.error(`MISMATCH in package.json for ${name}: expected ${expectedVer}, got ${inPkg}`);
    errors++;
  }
  const inLock =
    lock.packages?.["node_modules/" + name]?.version ||
    lock.packages?.[""]?.dependencies?.[name] ||
    lock.packages?.[""]?.devDependencies?.[name];
  if (inLock !== expectedVer) {
    console.error(
      `MISMATCH in package-lock.json for ${name}: expected ${expectedVer}, got ${inLock}`
    );
    errors++;
  }
}

if (errors > 0) {
  console.error(`ERROR: Total ${errors} version pin mismatches detected.`);
  process.exit(1);
}

console.log(
  "All 32 exact selected direct dependency pins verified successfully in package.json and lockfile!"
);
console.log("=== Toolchain Verification PASSED ===");

import "server-only";

const isTestFixture = process.env.PPV_H1F_TEST_FIXTURE === "1";

function resolveSpringOrigin(): string {
  const configured = process.env.PPV_SPRING_ORIGIN;
  if (isTestFixture && configured) {
    if (configured === "http://localhost:19080") {
      return configured;
    }
    throw new Error(
      `Invalid PPV_SPRING_ORIGIN '${configured}': only http://localhost:19080 is permitted for test fixture`
    );
  }
  return "http://localhost:8080";
}

export const serverConfig = {
  springOrigin: resolveSpringOrigin(),
  isTestFixture,
  ownerSetupEnabled: process.env.PPV_OWNER_SETUP_ENABLED === "true",
  googleSignInClientId: isTestFixture
    ? "test-google-client-id"
    : process.env.PPV_GOOGLE_SIGNIN_CLIENT_ID,
  driveClientId: isTestFixture ? "s2-http-client" : process.env.PPV_DRIVE_CLIENT_ID,

  // Cookie definitions for pilot
  sessionCookieName: "ppv_dev_sid",
  googleSignInBindCookie: "ppv_google_signin_bind",
  googleHandoffCookie: "ppv_google_handoff",
  driveBinding1Cookie: "PPV_DRIVE_BINDING_1",
  driveBinding2Cookie: "PPV_DRIVE_BINDING_2",

  // Host and origin protections
  allowedHost: "localhost:3000",
  allowedOrigin: "http://localhost:3000",

  // Session limits
  maxSessions: 8,
  anonymousIdleMs: 10 * 60 * 1000, // 10 minutes
  anonymousAbsoluteMs: 20 * 60 * 1000, // 20 minutes
  authenticatedIdleMs: 30 * 60 * 1000, // 30 minutes
  authenticatedAbsoluteMs: 12 * 60 * 60 * 1000, // 12 hours

  // Transfer limits
  maxFileBytes: 52_428_800, // 50 MiB
  maxMultipartBytes: 57_671_680, // 55 MiB
  maxJsonBodyBytes: 65_536, // 64 KiB
  maxAuthJsonBytes: 4_096, // 4 KiB
  maxConcurrentTransfers: 2,

  // Stream chunk size
  streamChunkSize: 65_536, // 64 KiB
} as const;

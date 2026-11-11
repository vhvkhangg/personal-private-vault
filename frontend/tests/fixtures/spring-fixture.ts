import { FixtureControlClient } from "./control";
import { NEXT_ORIGIN, SPRING_ORIGIN } from "./data";

let cachedControl: FixtureControlClient | null = null;

export function getControlClient(): FixtureControlClient {
  const port = process.env.PPV_CONTROL_PORT;
  if (!port) {
    throw new Error("PPV_CONTROL_PORT is not set. Fixture must be running before tests.");
  }
  if (!cachedControl) {
    cachedControl = new FixtureControlClient(port);
  }
  return cachedControl;
}

export async function preflightFixture(): Promise<boolean> {
  try {
    const res = await fetch(`${SPRING_ORIGIN}/api/v1/auth/google/start`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ handoffChallenge: "0".repeat(64) }),
    });
    return res.status === 200 || res.status === 400;
  } catch {
    return false;
  }
}

export async function preflightNext(): Promise<boolean> {
  try {
    const res = await fetch(`${NEXT_ORIGIN}/api/bff/session`);
    return res.status === 200;
  } catch {
    return false;
  }
}

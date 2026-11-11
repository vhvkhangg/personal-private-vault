export const TEST_OWNER = {
  email: "owner@vault.local",
  username: "vaultowner",
  password: "MasterVaultPassword123!",
  pin: "123456",
  googleSub: "browser-owner-sub-1001",
  issuer: "https://accounts.google.com",
};

export const SPRING_PORT = 19080;
export const NEXT_PORT = 3000;
export const SPRING_ORIGIN = `http://localhost:${SPRING_PORT}`;
export const NEXT_ORIGIN = `http://localhost:${NEXT_PORT}`;

export const SAMPLE_FILES = {
  png: "frontend/tests/fixtures/assets/sample.png",
  jpg: "frontend/tests/fixtures/assets/sample.jpg",
  webp: "frontend/tests/fixtures/assets/sample-unsupported.webp",
};

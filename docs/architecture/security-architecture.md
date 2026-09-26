# Security Architecture

## 1. Current scope

Security v1 is deliberately scoped to the current development phase:

- permanent single user;
- one-time account bootstrap;
- login with username or email + password;
- JWT access token + refresh token lifecycle;
- refresh token rotation/revocation support;
- 6-digit private-mode PIN;
- private mode automatically locks after 5 minutes of inactivity by default.

Internet-deployment hardening is **not frozen yet**.

## 2. Passwords and PIN

Passwords and private-mode PINs must be stored only as strong password hashes. Plaintext password/PIN values must never be persisted or logged.

The PIN is a UI/application privacy gate for someone physically using an already authenticated device. It is **not encryption of personal database fields** and must not be described as protection against database theft.

## 3. JWT model

Use short-lived access tokens plus refresh tokens rather than one long-lived JWT.

Refresh-token requirements:

- persist only a token hash or equivalent non-replayable server representation;
- support expiration;
- support explicit revocation;
- support rotation/replacement tracking;
- do not log raw token material.

Exact lifetimes and browser storage/cookie transport policy are finalized during security implementation, not in architecture v1.

## 4. Bootstrap

There is no public registration endpoint.

The first account is created by a one-time bootstrap/setup flow. Secrets must not be committed as seed SQL, migration constants, example credentials, or repository configuration.

## 5. Secret management

The repository may be public; the vault's data and runtime secrets are not.

Never commit:

- passwords or hashes derived from known example passwords intended for real use;
- JWT signing keys/secrets;
- database credentials;
- cloud/object-storage credentials;
- private API keys;
- production backups;
- imported personal data or media.

Use environment/runtime secret injection once implementation begins.

## 6. Logging

SLF4J/Logback is the planned logging stack.

Do not log:

- passwords/PINs;
- access or refresh tokens;
- authorization headers;
- sensitive personal note contents by default;
- complete imported files;
- unnecessary financial/private payloads.

Request correlation IDs may be added for troubleshooting without exposing private content.

## 7. Deferred hardening

Before exposing the application broadly to the Internet, revisit at least:

- TLS termination and secure cookies/token transport;
- CSRF implications of the chosen browser token transport;
- rate limiting and failed-login throttling;
- security headers;
- 2FA/TOTP or passkeys;
- session/device management;
- audit/security event logging;
- reverse proxy/network exposure;
- backup encryption and secret rotation.

These are deferred decisions, not rejected requirements.

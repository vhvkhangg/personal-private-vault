---
trigger: model_decision
description: "Apply when work touches authentication, JWT/refresh tokens, PIN, personal information, finance, external accounts, logging, secrets, or private content."
---

# Security Rule

- Never store or log plaintext passwords, PINs, JWTs, refresh tokens, API keys, or credentials.
- Hash passwords and the six-digit private-mode PIN with an appropriate password-hashing mechanism.
- Refresh tokens must support revocation/rotation semantics.
- Never weaken authorization/authentication checks merely to simplify tests.
- Treat finance, personal/family data, external account metadata, and private notes as sensitive.
- Use parameterized logging and minimize sensitive context in log messages.
- Do not commit real personal data or secrets.
- The current scope is password + JWT; do not introduce 2FA/passkeys unless explicitly requested.

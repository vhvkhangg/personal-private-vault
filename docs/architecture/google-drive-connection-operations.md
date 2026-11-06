# Google Drive connection operations

**Status:** GDI26-S1 submitted implementation; independent review required. Drive remains OFF. Real Google Cloud/consent/browser/remote HTTPS operation is NOT_VERIFIED and requires separate Owner authorization. No files/folders/Explorer/Picker/background sync are provided by S1.

## Configuration and key custody

| Environment input | Default / requirement |
| --- | --- |
| PPV_DRIVE_ENABLED | false |
| PPV_DRIVE_TOKEN_KEY_FILE | Empty; when enabled, existing raw 32-byte AES key outside Git/PostgreSQL |
| PPV_DRIVE_OAUTH_CLIENT_ID | Empty; required when enabled |
| PPV_DRIVE_OAUTH_CLIENT_SECRET | Empty; required when enabled |
| PPV_DRIVE_OAUTH_REDIRECT_URI | Exactly http://localhost:8080/api/v1/drive/oauth/callback |

Normal enabled startup rejects other redirects and provider-endpoint overrides before provider work. Authorization/token/JWKS/revoke endpoints are fixed Google HTTPS constants, not environment-selected hosts. Test endpoints/keys are wired only in test source configuration. OFF never loads the key or constructs active OAuth/services/controllers; normal Flyway V25 and repository/entity discovery still apply.

Provision the key outside the checkout, for example C:\Users\<Owner>\.ppv-secrets\drive-token.key. Generate 32 CSPRNG bytes with an OS cryptographic generator; never paste/base64-print them into logs or prompts. Give only the Windows identity running PPV read access: remove inherited broad ACLs and check the resulting explicit ACL, including any privileged backup policy. Enabled startup validates existence/readability/length; operator-only ACLs remain an operational responsibility. Store the client secret externally too.

Portability excludes drive_connections and never exports its credentials/key. A full database backup is different: it may contain encrypted credentials. Back up the key separately with restricted access; database plus key restores usable authority. Envelope is version 0x01, 12-byte random IV, ciphertext and 16-byte GCM tag; key_id v1. Epochs are random UUID generations, not ordered counters. There is no automatic key rotation.

Missing/wrong-length key prevents enabled startup. With a readable replacement/wrong key, decryption fails closed. Status is read-only; it does not automatically detect key loss. Same-subject reconnect without a new credential can report RECONNECT_REQUIRED; local disconnect still deletes undecryptable or absent credentials, dispatches no fabricated token and reports remote FAILED with quarantine. Resolve Google-side authority explicitly before fresh consent. Do not silently replace an encryption key while expecting old ciphertext to decrypt.

## Browser and account flow

The separately authorized operator must provision/register a Google Web Application client, exact callback and both selected test accounts. No agent performs Cloud configuration or live OAuth in this slice. Only openid, email (Google's equivalent userinfo.email is accepted) and drive.file are allowed; do not include previous broad grants.

Use the same localhost host and browser cookie jar for the authenticated POST authorize and subsequent navigation to its authorizationUrl. A standalone API client followed by a different browser cannot complete proof. Cookies PPV_DRIVE_BINDING_1/2 are host-only, HttpOnly, SameSite=Lax, Path=/api/v1/drive/oauth/callback, Max-Age=300; Secure omission is only for the exact approved HTTP localhost URI. Actual browser delivery remains NOT_VERIFIED. Remote HTTPS requires a new readiness/security gate.

Use slot 1 for U and slot 2 for distinct verified subject V. Email is display only. A same-slot regrant reconnects only the same subject; disconnect before replacing that subject. A new authorize supersedes old authority, including in-flight callbacks. Attempts expire after 300s, with at most four pending/processing operations; cancelled network work keeps capacity until it ends.

Callback success text is exactly “Google Drive connection completed. Return to PPV.” Failure text is fixed and never reflects provider details. Status exposes two slots only; no token/subject/epoch/scope/key material. Backend APIs are not a completed browser UI.

External Google OAuth apps in Testing can issue seven-day refresh tokens when non-basic scopes such as drive.file are requested. Do not assume weekly regrant or any refresh token is permanently valid; handle expiry/revocation through explicit reconnect. See [Google token expiration](https://developers.google.com/identity/protocols/oauth2#expiration).

## Disconnect, uncertainty and restart

Local disconnect first removes authority in a short guarded PostgreSQL transaction. If deletion fails, it returns sanitized 500 DRIVE_PERSISTENCE_FAILURE and sends no revoke. Repeating an empty-slot disconnect cancels pending authority without repeating remote mutation.

One POST to https://oauth2.googleapis.com/revoke carries token in the form body, never the URL. Connect is bounded at 2s; complete response at 5s or remaining budget; total revoke ceiling is 8s. No automatic mutation retry occurs.

| remoteRevocation | Meaning |
| --- | --- |
| null | No retained knowledge, including after restart |
| PENDING | Local authority gone; captured old remote request still running |
| REQUEST_ACCEPTED | HTTP 200 accepted the request; propagation is not proved |
| FAILED | Definitive refusal, or no decryptable credential to send |
| UNCERTAIN | Timeout/transport/body-completion ambiguity |

Google may revoke all outstanding grants for the same subject/project, including a newly issued token. Pre/during-revoke attempts cannot commit after completion or fresh reconnect. Unrelated V remains independent. FAILED/UNCERTAIN quarantine blocks known-slot starts and same-subject final writes (409 DRIVE_REVOKE_UNCERTAIN); active revoke uses 409 DRIVE_REVOCATION_PENDING. Capacity exhaustion returns retryable 503, without evicting uncertainty or preventing local removal of an admitted durable row.

Uncertain recovery is explicit: inspect/remove PPV access in [Google Account permissions](https://myaccount.google.com/permissions), verify clean single-JVM deployment and that the old operation ceased, then restart if necessary and initiate fresh consent. Restart loses attempts/fences/transient remote knowledge, rejects old state and does not retry revoke; it is not proof of remote completion. Never overlap JVMs/restarts to bypass quarantine. Delayed invalid_grant downgrades only the current matching credential epoch.

All deterministic receipts/limits and the pending independent review are in the [implementation handback](../implementation/maintenance/2026-11-05-google-drive-integration-preparation/implementation-handback-s1-2026-11-06.md). Feed remains OFF; external indexing remains INACTIVE. See [Google revocation semantics](https://developers.google.com/identity/protocols/oauth2/web-server#tokenrevoke) and [OIDC validation](https://developers.google.com/identity/openid-connect/openid-connect).

# ADR 0005 — Signed Continuity Session Credential

## Status
Accepted as an additive cloud-readiness boundary. The legacy browser-session recovery endpoint remains temporarily available until the avatar UI migrates and is validated.

## Context
KUPPA can recover the last resumable turn using a high-entropy browser session ID, but that ID is explicitly not authentication. Exposing transcript-free metadata limits damage, yet a shared always-on cloud deployment still needs a server-verifiable possession credential before continuity lookup can be treated as a security boundary.

## Decision
Introduce a server-issued continuity session credential signed with HMAC-SHA256. The signing key comes only from `KUPPA_CONTINUITY_SIGNING_SECRET` and must be at least 32 bytes. The server generates the session ID, binds an expiry timestamp into the signature, validates in constant time, and fails closed when the signing secret is absent or weak.

The new contract is additive:
- `POST /api/chat/session` issues `{clientSessionId, token, expiresAt}` only when secure continuity is configured.
- `GET /api/chat/resumable/secure` requires the matching `X-KUPPA-Continuity-Token`.
- The existing `/api/chat/resumable` remains unchanged for the current UI until a later UI cycle migrates it.

## Boundary
This credential proves possession of a server-issued continuity session. It is **not owner authentication**, does not grant access to persona memory or tool execution, and does not move reasoning into KUPPA. Vayu remains responsible for semantic continuity and cognition.

## Consequences
- Arbitrary callers cannot forge another server-issued continuity credential without the HMAC secret.
- Tokens expire and can be rotated operationally by changing the signing secret.
- A server restart does not invalidate tokens when the configured secret remains stable.
- Cross-device owner continuity still requires a real authenticated owner/device identity layer.
- Browser migration must avoid placing a long-lived owner credential into source code; no secret is committed.

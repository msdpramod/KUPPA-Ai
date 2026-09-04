# ADR 0009: Separate owner device-management authentication

## Status
Accepted — 2026-08-30.

## Context
KUPPA already persists owner-device trust and supports self-revocation with the target device credential. That does not solve lost-device revocation because the owner may no longer possess that token. Reusing the enrollment secret for fleet/device administration would couple two different security duties and make rotation harder.

## Decision
Introduce a distinct environment-only `KUPPA_OWNER_MANAGEMENT_SECRET` with a minimum length of 32 bytes. Guard owner-wide device inventory and remote revocation with `X-KUPPA-Owner-Management-Key`. Keep enrollment authentication, device-token signing, continuity signing and owner management as separate credentials. Device inventory exposes metadata only and never returns device bearer tokens or signing material.

## Consequences
- Lost devices can be revoked server-side without the target device token.
- Enrollment and device management credentials can be rotated independently.
- Existing self-revocation and continuity behavior remains backward compatible.
- This remains shared-secret authentication and is not equivalent to passkeys/WebAuthn/OIDC.
- The management credential becomes high-value configuration and must never be committed.

## Architectural boundary
KUPPA owns identity/trust and therefore this management surface. Vayu remains the BRAIN and receives no new identity-management responsibility.

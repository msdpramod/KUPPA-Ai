# ADR 0006: Owner-enrolled device boundary

## Status
Accepted as an additive cloud-readiness boundary, pending UI migration.

## Context
The signed continuity-session contract proves possession of a server-issued session but deliberately does not prove that the caller is KUPPA's owner. That is insufficient for safe cross-device continuity on an always-running cloud deployment.

## Decision
KUPPA HEART owns an owner/device trust boundary that is separate from Vayu cognition. A deployment-configured owner enrollment secret authorizes issuance of expiring HMAC-SHA256 device credentials. An enrolled device credential can authorize creation of a signed continuity session through `/api/chat/session/owner`.

The credential is a possession credential, not hardware attestation. It does not move reasoning, reference resolution, planning, tool selection, execution strategy, or self-healing into KUPPA; those remain Vayu BRAIN responsibilities.

## Security properties
- Enrollment is disabled unless `KUPPA_OWNER_ENROLLMENT_SECRET` is at least 32 bytes.
- Enrollment comparison and signature verification use constant-time byte comparison where applicable.
- Device credentials bind version, configured owner id, random device id, and expiry.
- Tampered, expired, malformed, wrong-device, or weakly configured credentials fail closed.
- The enrollment secret is environment-only and is never returned by the API.
- Existing unsigned/browser-local continuity paths remain for backward compatibility until a separately validated migration; they are not promoted to owner-authenticated paths.

## Consequences
This creates the trust chain needed for later cross-device recovery: owner enrollment -> device credential -> signed continuity session. It does not yet provide revocation lists, hardware-backed device keys, multi-owner accounts, OAuth/OIDC, or transcript discovery across devices. Those require future explicit designs rather than being implied by this change.

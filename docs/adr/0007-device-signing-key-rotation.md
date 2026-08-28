# ADR 0007 — Separate owner enrollment from device-token signing

## Status
Accepted pending CI validation and governed-branch promotion.

## Context
KUPPA's owner-device boundary originally used `KUPPA_OWNER_ENROLLMENT_SECRET` both to authenticate enrollment and to sign device credentials. That creates unnecessary credential-purpose coupling and makes planned rotation disruptive: rotating the enrollment secret changes both who may enroll and which existing device tokens can be validated.

KUPPA is the HEART and owns identity/trust continuity. Vayu is the BRAIN and must remain uninvolved in credential issuance or verification.

## Decision
Introduce an optional dedicated device signing key:
- `KUPPA_DEVICE_SIGNING_SECRET` signs new `v2` device credentials.
- `KUPPA_DEVICE_PREVIOUS_SIGNING_SECRET` may temporarily verify credentials issued by the immediately previous signing key during planned rotation.
- If no dedicated signing key is configured, KUPPA retains legacy `v1` issuance for backward compatibility.
- Existing `v1` credentials remain verifiable using the enrollment secret until normal expiry.
- Any non-empty but weak/structurally incomplete dedicated-signing configuration fails closed.
- No secret is stored in source control.

## Consequences
Positive:
- Enrollment authentication and device-token signing can be rotated independently.
- A two-key overlap supports planned rotation without a synchronized device logout.
- Existing deployments and v1 credentials remain compatible.
- No new dependency or schema is required.

Tradeoffs:
- During migration, v1 token verification still depends on the enrollment secret.
- The previous signing key must be removed operationally after the intended overlap window.
- This is still possession-based trust, not hardware attestation.
- Per-device revocation remains unsolved.

## Rejected alternatives
1. Immediately invalidate all v1 tokens: rejected because it creates an unnecessary breaking migration before the avatar even uses the stronger owner path.
2. Reuse the continuity-session secret for device tokens: rejected because it recreates credential-purpose coupling across another trust boundary.
3. Add a database-backed device registry in the same change: deferred to keep this evolution narrowly reviewable and schema-free; persistent revocation is the next Heart target.

## HEART/BRAIN boundary
This decision changes only KUPPA identity/trust plumbing. Vayu reasoning, planning, retrieval, orchestration, tools and execution are unchanged.

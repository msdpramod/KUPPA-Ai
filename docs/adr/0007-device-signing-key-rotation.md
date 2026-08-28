# ADR 0007 — Separate owner enrollment from device-token signing

## Status
Accepted and validated by GitHub Actions CI #121; governed promotion merge `cbc3a5c8583e142efd2eb5a197b2db3d9983b5a5`.

## Context
KUPPA's owner-device boundary originally used the owner enrollment value both to authenticate enrollment and to sign device credentials. That created unnecessary credential-purpose coupling and made planned rotation disruptive: changing the enrollment value simultaneously changed both enrollment authentication and token verification.

KUPPA is the HEART and owns identity/trust continuity. Vayu is the BRAIN and remains uninvolved in credential issuance or verification.

## Decision
Introduce an optional dedicated device signing key:
- `KUPPA_DEVICE_SIGNING_SECRET` signs new `v2` device credentials.
- `KUPPA_DEVICE_PREVIOUS_SIGNING_SECRET` may temporarily verify credentials issued by the immediately previous signing key during planned rotation.
- If no dedicated signing key is configured, KUPPA retains legacy `v1` issuance for backward compatibility.
- Existing `v1` credentials remain verifiable using the enrollment value until normal expiry.
- Any non-empty but weak or structurally incomplete dedicated-signing configuration fails closed.
- No secret is stored in source control.

## Consequences
Positive:
- Enrollment authentication and device-token signing can be rotated independently.
- A two-key overlap supports planned rotation without a synchronized device logout.
- Existing deployments and v1 credentials remain compatible.
- No new dependency or schema is required.

Tradeoffs:
- During migration, v1 token verification still depends on the enrollment value.
- The previous signing key must be removed operationally after the intended overlap window.
- This remains possession-based trust, not hardware attestation.
- Per-device revocation remains unsolved.

## Rejected alternatives
1. Immediately invalidate all v1 tokens: rejected because it creates an unnecessary breaking migration before the avatar uses the stronger owner path.
2. Reuse the continuity-session signing value for device tokens: rejected because it recreates credential-purpose coupling across another trust boundary.
3. Add a database-backed device registry in the same change: deferred to keep the evolution narrow and schema-free; persistent revocation is the next Heart target.

## Validation
Implementation commit `b88adffb3bd44985bb38feb40c868050aaba70bf` passed GitHub Actions CI #121, including the full Maven Test step, before promotion.

## HEART/BRAIN boundary
This decision changes only KUPPA identity/trust plumbing. Vayu reasoning, planning, retrieval, orchestration, tools and execution are unchanged.

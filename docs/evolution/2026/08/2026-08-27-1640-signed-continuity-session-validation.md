# KUPPA Evolution Validation — Signed Continuity Session Contract

- **Date/time:** 2026-08-27 16:40 Asia/Kolkata
- **Cycle:** Heart / Personality / Relationship — validation closeout
- **Purpose:** Record the observed CI result and promote the signed continuity-session hardening only after the regression gate passed.
- **Implementation commit:** `a4c9171eda1e6f6035e9f35ae766defab26b2aba`
- **Promoted runtime merge:** `a2adb3b89cc1dad11be4ef2f20ccff6fb70494b7`
- **Pull request:** #4

## Validation result
GitHub Actions CI run #115 completed successfully. Checkout and Java setup passed, the full Maven **Test** step passed, cleanup completed, and the workflow concluded `success`. The candidate was not merged into `agent/avatar-ui` until this result was observed.

## Verified behavior
- Server-issued continuity credentials are generated only when a signing secret of at least 32 bytes is configured.
- Credentials bind the server-generated session ID to an expiry timestamp using HMAC-SHA256.
- Signature comparison uses `MessageDigest.isEqual`.
- Focused tests verify successful issue/validation, session tampering rejection, signature tampering rejection, expiry rejection, and weak-secret fail-closed behavior.
- The legacy `/api/chat/resumable` path remains unchanged, so the current avatar UI does not regress before its dedicated migration cycle.
- The secure endpoint returns the same metadata-only resumable result after possession validation; no transcript or persona content is added.

## Before / after metrics
- Full build status: **green CI #113 -> green CI #115**.
- Server-verifiable continuity possession path: **0 -> 1**.
- Expiring signed continuity tokens: **0 -> 1**.
- New runtime dependencies: **0**.
- Database schema changes: **0**.
- Existing avatar behavior changes: **0**.
- KUPPA/Vayu responsibility changes: **0**; KUPPA owns continuity/presence boundary, Vayu owns cognition.
- Consequential-action approval changes: **0**.
- Secrets committed: **0**.

## Security / privacy conclusion
This is intentionally not called owner authentication. It proves possession of a server-issued continuity session and prepares a safer cloud migration path. Real owner/device enrollment is still required for cross-device identity. The legacy compatibility endpoint remains the main known gap until the avatar switches to signed sessions.

## Constitution / regression impact
No Constitution invariant changed. Vayu remains the BRAIN and KUPPA remains the HEART. Memory confidence rules, approval gates, no-secrets policy, graceful Vayu degradation, cancellation, and UI interaction behavior remain intact.

## Rollback
Return runtime to `33ad4d0b1c76bf7886f33d165b5fee1a4da989b3` if the new secure-session endpoints cause operational issues. No schema rollback is required.

## Blockers / limitations
- Current browser UI still uses the legacy locally generated session ID and legacy resumable endpoint.
- The credential is possession-based, not owner authentication.
- Cross-device continuity and device enrollment are not implemented.
- Token revocation is coarse (expiry or signing-secret rotation).
- CSP/XSS hardening remains separate.

## Next evolution target
Body/UI cycle: migrate the avatar to obtain a server-issued signed continuity session, use the secure resumable endpoint, retain graceful fallback when secure continuity is not configured, and then prepare deprecation of the legacy recovery path after UI regression tests pass.

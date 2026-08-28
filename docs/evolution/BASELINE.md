# KUPPA Known-Good Baseline

## Runtime baseline
- **Current validated implementation:** `b88adffb3bd44985bb38feb40c868050aaba70bf` (device signing-key separation and rotation), promoted through governed merge `cbc3a5c8583e142efd2eb5a197b2db3d9983b5a5`.
- **CI:** GitHub Actions CI run #121 completed successfully, including the full Maven Test step.
- **Previous validated runtime:** `46fd36cdf88e6441e56fc41c63e181ef64dc0d6c` (owner-enrolled device trust boundary; CI #118 green).
- **Previous UI runtime:** `33ad4d0b1c76bf7886f33d165b5fee1a4da989b3` (CI #113 green; session-scoped continuity recovery).

## Current evidence
- Spring/Java CI is green on the device signing-key separation implementation.
- Owner enrollment still requires a strong environment-provided enrollment value.
- Deployments may configure an independent strong device-signing value; new credentials then use token version `v2`.
- One previous strong device-signing value may be accepted temporarily during planned rotation.
- Existing `v1` device credentials remain backward compatible until expiry.
- Weak or incomplete dedicated-signing configuration fails closed in focused tests.
- Successful v2 issuance, legacy v1 migration, previous-key overlap, rejection outside the rotation window, expiry and tampering are covered by tests.
- No database schema or runtime dependency was added.
- VayuBrainGateway v3, correlation/cancellation, parent restoration, approval gates, confidence-aware memory, avatar state engine, voice barge-in and degraded brain presence remain intact.

## Scorecard baseline
| Dimension | Evidence status |
|---|---|
| Build stability | Green on CI run #121 |
| Conversation quality | Unchanged; resumable continuity remains intact |
| Personality consistency | Unchanged |
| Memory accuracy | Existing automated tests; unchanged |
| Vayu handoff reliability/latency | v3 correlation/cancellation/persisted parent lookup unchanged |
| Errors | Device signing/migration/rotation failure paths plus existing fallbacks tested |
| Voice reliability | Existing playback cancellation/fallback unchanged |
| UI responsiveness | Current avatar behavior unchanged; owner-device UI migration pending |
| Accessibility | Unchanged |
| Resource usage | No new dependency/database schema; HMAC verification may try active plus previous key during rotation |
| Security boundary | Enrollment authentication can be separated from device-token signing; v2 supports one previous-key rotation window while v1 remains migration-compatible |

## Rollback policy
For device signing-key separation/rotation, return to `46fd36cdf88e6441e56fc41c63e181ef64dc0d6c` (CI #118 green) or governed pre-change head `6d6c27c012cb6bc6e15f9a062a2ba8db9684aeb7`. The change is schema-free and preserves v1 compatibility, so rollback does not require database migration. Normal evolution must preserve the KUPPA Constitution and approval gates.

## Next identified gaps
- Persistent per-device revocation and renewal/audit semantics.
- Avatar migration from browser-local continuity identity to owner/device-authorized signed continuity with graceful fallback.
- Hardware/passkey/OIDC-grade identity is still intentionally out of scope for the current possession-token model.

# KUPPA Known-Good Baseline

## Runtime baseline
- **Current validated implementation:** `46fd36cdf88e6441e56fc41c63e181ef64dc0d6c` (owner-enrolled device trust boundary).
- **CI:** GitHub Actions CI run #118 — completed successfully, including the full Maven Test step.
- **Previous validated runtime:** `a2adb3b89cc1dad11be4ef2f20ccff6fb70494b7` (CI #115 green; signed continuity-session possession contract).
- **Previous UI runtime:** `33ad4d0b1c76bf7886f33d165b5fee1a4da989b3` (CI #113 green; session-scoped continuity recovery).

## Current evidence
- Spring/Java CI is green on the owner/device trust boundary.
- Owner enrollment is disabled unless `KUPPA_OWNER_ENROLLMENT_SECRET` is at least 32 bytes.
- Owner-enrolled device credentials bind token version, configured owner id, random device id, and expiry with HMAC-SHA256.
- Wrong enrollment secrets, wrong device IDs, malformed/tampered tokens, expired tokens, and weak configuration fail closed in focused tests.
- Owner-gated continuity issuance is additive through `/api/chat/session/owner`; existing continuity endpoints remain backward compatible.
- The credential is explicitly a possession credential, not hardware attestation or complete multi-user authentication.
- No database schema or runtime dependency was added.
- `VayuBrainGateway v3`, correlation/cancellation, parent restoration, approval gates, confidence-aware memory, avatar state engine, voice barge-in, and degraded brain presence remain intact.
- No aggregate production telemetry exists yet for continuity success, memory accuracy, Vayu latency, voice reliability, UI latency, accessibility, or resource use.

## Scorecard baseline
| Dimension | Evidence status |
|---|---|
| Build stability | Green on CI run #118 |
| Conversation quality | Existing resumable continuity unchanged; owner trust path is additive |
| Personality consistency | Unchanged |
| Memory accuracy | Existing automated tests; unchanged |
| Vayu handoff reliability/latency | v3 correlation/cancellation/persisted parent lookup unchanged |
| Errors | Owner enrollment/token failure paths plus existing continuity/provider fallbacks tested |
| Voice reliability | Existing playback cancellation/fallback unchanged |
| UI responsiveness | Current avatar behavior unchanged; owner-device UI migration pending |
| Accessibility | Unchanged |
| Resource usage | No new dependency/database schema; HMAC only on enrollment/device validation |
| Security boundary | Signed session possession only -> additive owner enrollment -> device credential -> signed continuity issuance |

## Rollback policy
For owner-device trust hardening, return to `a2adb3b89cc1dad11be4ef2f20ccff6fb70494b7` (CI #115 green). The new identity endpoints/configuration are additive and schema-free, so rollback does not require database migration. Normal evolution must preserve the KUPPA Constitution and approval gates.

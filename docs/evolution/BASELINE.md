# KUPPA Known-Good Baseline

## Runtime baseline
- **Current validated commit:** `a2adb3b89cc1dad11be4ef2f20ccff6fb70494b7`
- **CI:** GitHub Actions CI run #115 — completed successfully on implementation commit `a4c9171eda1e6f6035e9f35ae766defab26b2aba`, including the full Maven Test step; merge commit has the identical validated tree.
- **Previous validated runtime:** `33ad4d0b1c76bf7886f33d165b5fee1a4da989b3` (CI run #113 green; session-scoped continuity recovery).
- **Previous server continuity runtime:** `74ef76ee8624b4d6df256311d13ce15455646556` (CI run #111 green; correlation-keyed persistence and server-side parent restoration).

## Current evidence
- Spring/Java CI is green on signed continuity-session hardening.
- Existing session-scoped metadata recovery remains backward compatible.
- A new additive secure continuity path uses server-generated session IDs plus expiring HMAC-SHA256 possession credentials.
- Secure issuance/lookup fails closed when `KUPPA_CONTINUITY_SIGNING_SECRET` is absent/weak, a token is malformed/tampered, a different session ID is supplied, or the token is expired.
- The signing secret is environment-only; no secret is committed.
- Secure recovery still returns only resumable metadata and does not expose transcript text, persona memory, actions, or tool output.
- The signed continuity credential is not treated as owner authentication; cross-device/cloud owner identity remains future work.
- `VayuBrainGateway v3`, server-side parent restoration, cancellation, stale-result suppression, approval gates, confidence-aware memory, avatar state engine, voice barge-in, and degraded brain presence remain intact.
- No aggregate production telemetry exists yet for continuity success, memory accuracy, Vayu latency, voice reliability, UI latency, or resource use.

## Scorecard baseline
| Dimension | Evidence status |
|---|---|
| Build stability | Green on CI run #115 |
| Conversation quality | Existing resumable continuity unchanged; secure session path is additive |
| Personality consistency | Unchanged |
| Memory accuracy | Existing automated tests; unchanged |
| Vayu handoff reliability/latency | v3 correlation/cancellation/persisted parent lookup unchanged |
| Errors | Signed session fail-closed paths plus existing continuity/provider fallbacks tested |
| Voice reliability | Existing playback cancellation/fallback unchanged |
| UI responsiveness | Current avatar behavior unchanged; secure-session UI migration pending |
| Accessibility | Unchanged |
| Resource usage | No new dependency or database schema; HMAC verification only on secure continuity operations |
| Security boundary | Browser session ID only -> additive server-verifiable possession credential available |

## Rollback policy
For signed continuity-session hardening, return to `33ad4d0b1c76bf7886f33d165b5fee1a4da989b3` (CI #113 green). The new secure endpoints/configuration are additive and schema-free, so rollback does not require database migration. Normal evolution must preserve the KUPPA Constitution and approval gates.

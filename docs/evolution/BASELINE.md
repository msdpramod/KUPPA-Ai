# KUPPA Known-Good Baseline

## Runtime baseline
- **Current validated commit:** `33ad4d0b1c76bf7886f33d165b5fee1a4da989b3`
- **CI:** GitHub Actions CI run #113 — completed successfully, including full Maven Test for session-scoped continuity recovery.
- **Previous validated runtime:** `74ef76ee8624b4d6df256311d13ce15455646556` (CI run #111 green; correlation-keyed persistence and server-side parent restoration).
- **Previous avatar continuity runtime:** `7ac2b7f2b879ce5f1962e610ab9433c57230e4f7` (CI run #109 green; explicit Vayu v3 avatar continuity controls).

## Current evidence
- Spring/Java CI is green on session-scoped continuity recovery.
- `chat_messages` persists nullable `correlationId`, `turnMode`, `parentCorrelationId`, and `clientSessionId` metadata.
- Same-browser refresh can recover the latest completed resumable correlation ID through metadata-only `GET /api/chat/resumable` and re-enable explicit Continue/Correct controls.
- The recovery API returns no transcript text; invalid/unknown sessions fall back to unavailable without fabricating continuity.
- Cancelled Vayu turns are excluded from session recovery.
- `VayuBrainGateway v3`, server-side parent restoration, cancellation, stale-result suppression, approval gates, confidence-aware memory, avatar state engine, voice barge-in, and degraded brain presence remain intact.
- No aggregate production telemetry exists yet for continuity success, memory accuracy, Vayu latency, voice reliability, UI latency, or resource use.

## Scorecard baseline
| Dimension | Evidence status |
|---|---|
| Build stability | Green on CI run #113 |
| Conversation quality | Same-browser resumable parent recovery + server parent restoration; no aggregate metric |
| Personality consistency | Unchanged |
| Memory accuracy | Existing automated tests; unchanged |
| Vayu handoff reliability/latency | v3 correlation, cancellation, persisted parent lookup; no aggregate metric |
| Errors | Invalid/unknown session safe fallback plus existing missing-parent/provider fallbacks tested |
| Voice reliability | Existing playback cancellation/fallback unchanged |
| UI responsiveness | Existing avatar-first controls now restore after refresh |
| Accessibility | Existing live regions/controls preserved; hidden guidance updated |
| Resource usage | No new dependency; one nullable column/index and one bounded metadata lookup |

## Rollback policy
For session-scoped continuity recovery, return to `74ef76ee8624b4d6df256311d13ce15455646556` (CI #111 green). The added `clientSessionId` column/index are nullable/additive and remain backward compatible with the previous runtime. Normal evolution must preserve the KUPPA Constitution and approval gates.

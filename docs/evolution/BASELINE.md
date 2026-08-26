# KUPPA Known-Good Baseline

## Runtime baseline
- **Current validated commit:** `74ef76ee8624b4d6df256311d13ce15455646556`
- **CI:** GitHub Actions CI run #111 — completed successfully, including full Maven Test on the correlation-keyed persistence candidate.
- **Previous validated runtime:** `7ac2b7f2b879ce5f1962e610ab9433c57230e4f7` (CI run #109 green; explicit Vayu v3 avatar continuity controls).
- **Previous Vayu v3 backend runtime:** `34882775c025ec793decf8846166e700f71a5beb` (CI run #107 green; Vayu Brain Gateway v3 + router compatibility repair).

## Current evidence
- Spring/Java CI is green on server-side correlation-keyed conversation persistence.
- `chat_messages` now persists nullable `correlationId`, `turnMode`, and `parentCorrelationId` metadata.
- Explicit `CONTINUE` and `CORRECTION` turns can resolve a supplied parent correlation ID server-side even when the parent is outside the recent-turn window.
- Missing parent IDs fall back to ordinary recent conversation context rather than fabricating history.
- Ollama and OpenAI fallback share the same correlation-aware context path.
- `VayuBrainGateway v3`, cancellation, stale-result suppression, approval gates, confidence-aware memory, avatar state engine, voice barge-in, and degraded brain presence remain intact.
- No aggregate production telemetry exists yet for continuity success, memory accuracy, Vayu latency, voice reliability, UI latency, or resource use.

## Scorecard baseline
| Dimension | Evidence status |
|---|---|
| Build stability | Green on CI run #111 |
| Conversation quality | Server-side explicit parent restoration available; no aggregate metric |
| Personality consistency | Unchanged |
| Memory accuracy | Existing automated tests; unchanged |
| Vayu handoff reliability/latency | v3 correlation + persisted parent lookup; no aggregate metric |
| Errors | Missing-parent fallback tested; stable gateway errors retained |
| Voice reliability | Existing playback cancellation/fallback unchanged |
| UI responsiveness | Existing explicit v3 controls unchanged |
| Accessibility | Existing avatar-first controls unchanged |
| Resource usage | No new dependency; additive DB index/columns only |

## Rollback policy
For correlation-keyed persistence, return to `7ac2b7f2b879ce5f1962e610ab9433c57230e4f7` (CI #109 green). The added database columns are nullable/additive and remain backward compatible with the previous runtime. Normal evolution must preserve the KUPPA Constitution and approval gates.

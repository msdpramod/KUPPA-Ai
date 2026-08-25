# KUPPA Known-Good Baseline

## Runtime baseline
- **Current validated commit:** `34882775c025ec793decf8846166e700f71a5beb`
- **CI:** GitHub Actions CI run #107 — completed successfully, including Maven Test after the v3 router compatibility fix.
- **Previous validated runtime:** `2677674a4032ea38b3019ffba04816748793b734` (CI run #104 green; correlation-aware avatar cancellation).
- **Previous Vayu backend runtime:** `7e0df512eeb416a0bd0dfb3d4e8873a16195057c` (CI run #102 green; Vayu Brain Gateway v2 cancellation).
- **Earlier validated runtime:** `1efac9e2485a6181413b30a003a88654c3cd9792` (CI run #100 green).
- **Earlier Vayu gateway runtime:** `930bd83fd3bb64559c4b5ab9da29b7201da9a223` (CI run #98 green).

## Current evidence
- Spring/Java CI is green on v3 after the compatibility repair.
- `VayuBrainGateway v3` is the cognition boundary and carries normalized `AUTO`, `NEW_TOPIC`, `CONTINUE`, and `CORRECTION` turn context plus optional parent correlation metadata.
- Existing clients remain compatible through `AUTO`; KUPPA transports continuity intent but does not automatically infer semantic relationships.
- Ollama and OpenAI fallback receive the same normalized continuity directive; Vayu remains responsible for reasoning and reference resolution.
- Cooperative cancellation, stale-result suppression, avatar correlation-aware interruption, approval gates, confidence-aware memory, and graceful degraded brain states remain intact.
- CI #106 intentionally remains recorded as the regression gate that caught legacy router overload incompatibility; CI #107 verifies the repair.
- No aggregate production telemetry exists yet for conversation quality, memory accuracy, voice reliability, UI latency, continuity-mode use, cancellation acceptance rate, or resource use.

## Scorecard baseline
| Dimension | Evidence status |
|---|---|
| Build stability | Green on CI run #107 |
| Conversation quality | Explicit resumable-turn context available; no aggregate metric |
| Personality consistency | Not instrumented |
| Memory accuracy | Existing automated tests; unchanged |
| Vayu handoff reliability/latency | v3 context + v2 correlation/cancellation/latency metadata; no aggregate metric |
| Errors | CI regression caught and repaired; stable gateway error codes retained |
| Voice reliability | Existing playback cancellation/fallback unchanged |
| UI responsiveness | Existing in-flight barge-in/topic supersession unchanged; v3 UI wiring pending |
| Accessibility | Existing live regions/interaction guidance unchanged |
| Resource usage | Not instrumented |

## Rollback policy
For the v3 resumable-turn context change, return to `2677674a4032ea38b3019ffba04816748793b734`. For a backend-only cancellation rollback, return to `7e0df512eeb416a0bd0dfb3d4e8873a16195057c`. Normal evolution must preserve the KUPPA Constitution and approval gates.

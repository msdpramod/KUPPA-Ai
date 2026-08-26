# KUPPA Known-Good Baseline

## Runtime baseline
- **Current validated commit:** `7ac2b7f2b879ce5f1962e610ab9433c57230e4f7`
- **CI:** GitHub Actions CI run #109 — completed successfully, including Maven Test with expanded avatar/Vayu v3 UI contract coverage.
- **Previous validated runtime:** `34882775c025ec793decf8846166e700f71a5beb` (CI run #107 green; Vayu Brain Gateway v3 + router compatibility repair).
- **Previous avatar cancellation runtime:** `2677674a4032ea38b3019ffba04816748793b734` (CI run #104 green; correlation-aware avatar cancellation).
- **Previous Vayu backend runtime:** `7e0df512eeb416a0bd0dfb3d4e8873a16195057c` (CI run #102 green; Vayu Brain Gateway v2 cancellation).

## Current evidence
- Spring/Java CI is green on the avatar-to-v3 continuity wiring.
- `VayuBrainGateway v3` remains the cognition boundary and carries normalized `AUTO`, `NEW_TOPIC`, `CONTINUE`, and `CORRECTION` turn context plus optional parent correlation metadata.
- Ordinary natural language remains `AUTO`; KUPPA does not classify message semantics.
- The avatar exposes one-shot explicit Continue, Correct, and New topic controls when the user knows the relationship.
- Continue/Correct link only to the last successfully completed browser turn; invalid parent selection is prevented before a completed turn exists.
- Correlation-aware cancellation, stale-result suppression, approval gates, confidence-aware memory, voice barge-in, and graceful degraded brain states remain intact.
- CI #109 passed the full Maven test step, including the expanded UI contract assertions for v3 mode transport and parent linkage.
- No aggregate production telemetry exists yet for conversation quality, explicit-mode selection accuracy, memory accuracy, voice reliability, UI latency, cancellation acceptance rate, or resource use.

## Scorecard baseline
| Dimension | Evidence status |
|---|---|
| Build stability | Green on CI run #109 |
| Conversation quality | Explicit v3 resumable-turn controls now available; no aggregate metric |
| Personality consistency | Not instrumented; unchanged |
| Memory accuracy | Existing automated tests; unchanged |
| Vayu handoff reliability/latency | v3 context + correlation/cancellation/latency metadata; no aggregate metric |
| Errors | Existing stable gateway error codes retained |
| Voice reliability | Existing playback cancellation/fallback unchanged |
| UI responsiveness | Explicit turn context + in-flight barge-in/topic supersession; green contract test |
| Accessibility | Optional continuity controls use button semantics, disabled state, grouped label, and aria-pressed |
| Resource usage | No new runtime dependency; not instrumented |

## Rollback policy
For the explicit v3 avatar continuity controls, return to `34882775c025ec793decf8846166e700f71a5beb`. For the earlier browser cancellation state, return to `2677674a4032ea38b3019ffba04816748793b734`. Normal evolution must preserve the KUPPA Constitution and approval gates.

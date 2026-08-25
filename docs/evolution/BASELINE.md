# KUPPA Known-Good Baseline

## Runtime baseline
- **Current validated commit:** `2677674a4032ea38b3019ffba04816748793b734`
- **CI:** GitHub Actions CI run #104 — completed successfully, including Maven Test and the updated avatar/Vayu cancellation contract assertions.
- **Previous validated runtime:** `7e0df512eeb416a0bd0dfb3d4e8873a16195057c` (CI run #102 green; Vayu Brain Gateway v2 cancellation).
- **Previous documentation/runtime state:** `99f793c95eb9893caf87b9dc8b7b2d1c43d4ca8f`.
- **Earlier validated runtime:** `1efac9e2485a6181413b30a003a88654c3cd9792` (CI run #100 green).
- **Earlier Vayu gateway runtime:** `930bd83fd3bb64559c4b5ab9da29b7201da9a223` (CI run #98 green).
- **Previous reactive-UI rollback:** `5a8357eabed348534484d161d94c7d988c90244b`.
- **Earlier runtime baseline:** `7616e6f344ee57a9a08c0ed55dba01701b4aaf23` (CI run #95 green).

## Current evidence
- Spring/Java CI is green after browser correlation-aware Vayu cancellation integration.
- `VayuBrainGateway v2` remains the cognition boundary; KUPPA supplies turn identity and interruption intent but does not gain reasoning/provider/tool authority.
- Browser turns now generate caller-known correlation IDs, call `POST /api/chat/{correlationId}/cancel`, supersede typed topic changes, ignore stale completions, and render `VAYU_CANCELLED` as a normal interruption.
- Speech barge-in explicitly settles the playback promise, preventing ghost browser turns after audio is stopped.
- Vayu gateway tests still cover healthy Ollama, degraded/unavailable routing, caller-provided correlation IDs, active-turn cancellation, stale-result suppression, unknown-turn cancellation, version/correlation metadata, and latency/degraded state propagation.
- `AvatarBrainPresenceContractTest` now verifies gateway health metadata plus correlation propagation, cancellation endpoint use, supersession guards, barge-in cancellation, cancellation events, and approval UI preservation.
- Avatar page retains the nine-state interaction engine, text/voice fallback, Vayu presence, and approval cards.
- No aggregate production telemetry exists yet for conversation quality, memory accuracy, voice reliability, UI latency, cancellation acceptance rate, or resource use.

## Scorecard baseline
| Dimension | Evidence status |
|---|---|
| Build stability | Green on CI run #104 |
| Conversation quality | Interruption/topic-change flow improved; no aggregate metric |
| Personality consistency | Not instrumented |
| Memory accuracy | Existing automated tests; no aggregate metric |
| Vayu handoff reliability/latency | v2 contract + per-request latency/correlation + cooperative cancellation + browser integration; no aggregate metric |
| Errors | Stable gateway error codes including `VAYU_CANCELLED`; stale browser completion guards; no aggregate rate metric |
| Voice reliability | Explicit playback cancellation settlement + fallback; no aggregate metric |
| UI responsiveness | In-flight Vayu barge-in and typed topic supersession supported; no aggregate latency metric |
| Accessibility | Separate live regions plus interruption guidance; no scanner score |
| Resource usage | Not instrumented |

## Rollback policy
For the correlation-aware UI cancellation change, return to `7e0df512eeb416a0bd0dfb3d4e8873a16195057c`. For the previous Vayu presence UI, return to `efd238cc8e9a5fdcc53323a3c69008644843b2e6`. For a deeper runtime rollback, return to `7616e6f344ee57a9a08c0ed55dba01701b4aaf23`. Normal evolution must preserve the KUPPA Constitution and approval gates.

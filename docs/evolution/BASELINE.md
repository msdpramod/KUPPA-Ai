# KUPPA Known-Good Baseline

## Runtime baseline
- **Current validated commit:** `7e0df512eeb416a0bd0dfb3d4e8873a16195057c`
- **CI:** GitHub Actions CI run #102 — completed successfully, including Maven Test and the new concurrent Vayu cancellation regression test.
- **Previous documentation/runtime state:** `99f793c95eb9893caf87b9dc8b7b2d1c43d4ca8f`.
- **Previous validated runtime:** `1efac9e2485a6181413b30a003a88654c3cd9792` (CI run #100 green).
- **Earlier Vayu gateway runtime:** `930bd83fd3bb64559c4b5ab9da29b7201da9a223` (CI run #98 green).
- **Previous reactive-UI rollback:** `5a8357eabed348534484d161d94c7d988c90244b`.
- **Earlier runtime baseline:** `7616e6f344ee57a9a08c0ed55dba01701b4aaf23` (CI run #95 green).

## Current evidence
- Spring/Java CI is green after `VayuBrainGateway v2` cooperative cancellation.
- Vayu gateway tests cover healthy Ollama, degraded/unavailable routing, caller-provided correlation IDs, active-turn cancellation, stale-result suppression, unknown-turn cancellation, version/correlation metadata, and latency/degraded state propagation.
- `AvatarBrainPresenceContractTest` still verifies that the UI consumes gateway health metadata, exposes fallback/unavailable presentation, emits a browser state event, and retains `/api/chat` plus approval UI.
- Avatar page retains the nine-state interaction engine, speech barge-in, text/voice fallback, and approval cards. It does not yet invoke the new v2 cancellation endpoint.
- No aggregate production telemetry exists yet for conversation quality, memory accuracy, voice reliability, UI latency, cancellation acceptance rate, or resource use.

## Scorecard baseline
| Dimension | Evidence status |
|---|---|
| Build stability | Green on CI run #102 |
| Conversation quality | Not instrumented |
| Personality consistency | Not instrumented |
| Memory accuracy | Existing automated tests; no aggregate metric |
| Vayu handoff reliability/latency | v2 contract + per-request latency/correlation + cooperative cancellation; no aggregate metric |
| Errors | Stable gateway error codes including `VAYU_CANCELLED`; no aggregate rate metric |
| Voice reliability | Fallback exists; no aggregate metric |
| UI responsiveness | Brain-provider latency visible after response; UI cancellation wiring pending |
| Accessibility | Separate live regions for avatar and Vayu brain status; no scanner score |
| Resource usage | Not instrumented |

## Rollback policy
For the v2 cancellable-handoff change, return to `99f793c95eb9893caf87b9dc8b7b2d1c43d4ca8f`. For the previous Vayu presence UI, return to `efd238cc8e9a5fdcc53323a3c69008644843b2e6`. For a deeper runtime rollback, return to `7616e6f344ee57a9a08c0ed55dba01701b4aaf23`. Normal evolution must preserve the KUPPA Constitution and approval gates.

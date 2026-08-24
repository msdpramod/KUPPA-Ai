# KUPPA Known-Good Baseline

## Runtime baseline
- **Current validated commit:** `1efac9e2485a6181413b30a003a88654c3cd9792`
- **CI:** GitHub Actions CI run #100 — completed successfully, including Maven Test and the new avatar/Vayu presence contract test.
- **Previous documentation/runtime state:** `efd238cc8e9a5fdcc53323a3c69008644843b2e6`.
- **Earlier Vayu gateway runtime:** `930bd83fd3bb64559c4b5ab9da29b7201da9a223` (CI run #98 green).
- **Previous reactive-UI rollback:** `5a8357eabed348534484d161d94c7d988c90244b`.
- **Earlier runtime baseline:** `7616e6f344ee57a9a08c0ed55dba01701b4aaf23` (CI run #95 green).

## Current evidence
- Spring/Java CI is green after the Vayu Presence UI change.
- Vayu gateway-focused tests cover healthy Ollama, OpenAI fallback, total brain outage, version/correlation ID, and degraded-state propagation.
- `AvatarBrainPresenceContractTest` verifies that the UI consumes gateway health metadata, exposes fallback/unavailable presentation, emits a browser state event, and retains `/api/chat` plus approval UI.
- Avatar page retains the nine-state interaction engine, barge-in, text/voice fallback, and approval cards.
- No aggregate production telemetry exists yet for conversation quality, memory accuracy, voice reliability, UI latency, or resource use.

## Scorecard baseline
| Dimension | Evidence status |
|---|---|
| Build stability | Green on CI run #100 |
| Conversation quality | Not instrumented |
| Personality consistency | Not instrumented |
| Memory accuracy | Existing automated tests; no aggregate metric |
| Vayu handoff reliability/latency | v1 contract + per-request latency/correlation + UI presence; no aggregate metric |
| Errors | Stable gateway error codes + explicit UI unavailable/fallback states; no aggregate rate metric |
| Voice reliability | Fallback exists; no aggregate metric |
| UI responsiveness | Brain-provider latency is visible after response; no end-to-end render metric |
| Accessibility | Separate live regions for avatar and Vayu brain status; no scanner score |
| Resource usage | Not instrumented |

## Rollback policy
For this UI change, return to `efd238cc8e9a5fdcc53323a3c69008644843b2e6`. For gateway regressions, return to `5a8357eabed348534484d161d94c7d988c90244b`. For a deeper runtime rollback, return to `7616e6f344ee57a9a08c0ed55dba01701b4aaf23`. Normal evolution must preserve the KUPPA Constitution and approval gates.

# KUPPA Known-Good Baseline

## Runtime baseline
- **Current validated commit:** `930bd83fd3bb64559c4b5ab9da29b7201da9a223`
- **CI:** GitHub Actions CI run #98 — completed successfully, including Maven Test.
- **Previous reactive-UI rollback:** `5a8357eabed348534484d161d94c7d988c90244b`.
- **Earlier runtime baseline:** `7616e6f344ee57a9a08c0ed55dba01701b4aaf23` (CI run #95 green).
- **Governance-only bootstrap:** `b7b937f8e87af8882619f850078f84173b2d3b85`.

## Current evidence
- Spring/Java CI is green for Vayu Brain Gateway v1.
- Gateway-focused tests cover healthy Ollama, OpenAI fallback, total brain outage, version/correlation ID, and degraded-state propagation.
- Existing avatar page retains the nine-state interaction engine, barge-in, text/voice fallback, and approval UI.
- No aggregate production telemetry exists yet for conversation quality, memory accuracy, voice reliability, UI latency, or resource use.

## Scorecard baseline
| Dimension | Evidence status |
|---|---|
| Build stability | Green on CI run #98 |
| Conversation quality | Not instrumented |
| Personality consistency | Not instrumented |
| Memory accuracy | Existing automated tests; no aggregate metric |
| Vayu handoff reliability/latency | v1 contract + per-request latency/correlation; no aggregate metric |
| Errors | Stable gateway error codes; no aggregate rate metric |
| Voice reliability | Fallback exists; no aggregate metric |
| UI responsiveness | No measured latency baseline |
| Accessibility | Nine-state UI includes live regions/focus semantics; no scanner score |
| Resource usage | Not instrumented |

## Rollback policy
For gateway regressions, return to `5a8357eabed348534484d161d94c7d988c90244b`. For a deeper runtime rollback, return to `7616e6f344ee57a9a08c0ed55dba01701b4aaf23`. Normal evolution must preserve the KUPPA Constitution and approval gates.

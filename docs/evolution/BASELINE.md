# KUPPA Known-Good Baseline

## Runtime baseline
- **Commit:** `7616e6f344ee57a9a08c0ed55dba01701b4aaf23`
- **CI:** GitHub Actions CI run #95 — completed successfully.
- **Governance-only successor:** `b7b937f8e87af8882619f850078f84173b2d3b85` (runtime-identical documentation bootstrap).

## Evidence available before this UI cycle
- Spring/Java CI was green on the runtime baseline.
- Existing avatar page supported text, speech recognition, voice synthesis, avatar fallback, approval cards, and four coarse states: idle/listening/thinking/speaking.
- No formal previous evolution scorecard existed in the repository. Future runs must not invent historical scores.

## Scorecard baseline
| Dimension | Evidence status |
|---|---|
| Build stability | Green on CI run #95 |
| Conversation quality | Not instrumented |
| Personality consistency | Not instrumented |
| Memory accuracy | Covered by existing automated tests, no aggregate metric |
| Vayu handoff reliability/latency | Not yet instrumented as a versioned gateway |
| Errors | No aggregate metric |
| Voice reliability | Fallback exists; no aggregate metric |
| UI responsiveness | No measured latency baseline |
| Accessibility | Partial semantics before current UI cycle |
| Resource usage | Not instrumented |

## Rollback policy
For the 2026-08-22 UI evolution, revert to `b7b937f8e87af8882619f850078f84173b2d3b85` to keep governance docs while restoring the previous runtime UI. If governance itself must be removed, return to `7616e6f344ee57a9a08c0ed55dba01701b4aaf23`.

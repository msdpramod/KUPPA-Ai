# KUPPA Evolution Index

KUPPA evolves under a HEART/BRAIN boundary: KUPPA is the human-facing HEART; Vayu is the reasoning/orchestration BRAIN. Every evolution commit must carry a same-commit record under `docs/evolution/YYYY/MM/` and must preserve approval-gated consequential actions.

## Chronological records

| Date / cycle | Record | Commit | Result |
|---|---|---|---|
| 2026-08-22 15:00 UI | `2026/08/2026-08-22-1500-governance-bootstrap.md` | `b7b937f8e87af8882619f850078f84173b2d3b85` | Governance log bootstrapped; runtime unchanged |
| 2026-08-22 15:15 UI | `2026/08/2026-08-22-1515-reactive-state-engine.md` | `5a8357eabed348534484d161d94c7d988c90244b` | Nine-state interaction engine + barge-in |
| 2026-08-24 03:00 Heart | `2026/08/2026-08-24-0300-vayu-brain-gateway-v1.md` | `930bd83fd3bb64559c4b5ab9da29b7201da9a223` | Versioned Vayu gateway + correlation/latency/degraded metadata; CI #98 green |
| 2026-08-24 03:10 Heart | `2026/08/2026-08-24-0310-vayu-gateway-validation.md` | `efd238cc8e9a5fdcc53323a3c69008644843b2e6` | Documentation-only CI closeout |
| 2026-08-24 15:00 UI | `2026/08/2026-08-24-1500-vayu-presence-ui.md` | `1efac9e2485a6181413b30a003a88654c3cd9792` | Avatar-level healthy/fallback/offline Vayu presence; CI #100 green |
| 2026-08-24 15:10 UI | `2026/08/2026-08-24-1510-vayu-presence-validation.md` | `99f793c95eb9893caf87b9dc8b7b2d1c43d4ca8f` | Documentation-only CI closeout |
| 2026-08-25 03:00 Heart | `2026/08/2026-08-25-0300-vayu-cancellable-handoff.md` | `7e0df512eeb416a0bd0dfb3d4e8873a16195057c` | Vayu gateway v2 cooperative cancellation + stale-result suppression; CI #102 green |
| 2026-08-25 03:10 Heart | `2026/08/2026-08-25-0310-vayu-cancellation-validation.md` | `4f0e874ea724a386060c4379a42881da7516982d` | Documentation-only CI closeout and baseline promotion |
| 2026-08-25 15:00 UI | `2026/08/2026-08-25-1500-correlation-aware-barge-in.md` | `2677674a4032ea38b3019ffba04816748793b734` | Browser correlation-aware cancellation, topic supersession, stale-response guards; CI #104 green |
| 2026-08-25 15:10 UI | `2026/08/2026-08-25-1510-correlation-aware-barge-in-validation.md` | `962e78209d443f498578891777320547a1ccf88f` | Documentation-only CI closeout and baseline promotion; CI #105 green |
| 2026-08-26 03:00 Heart | `2026/08/2026-08-26-0300-resumable-turn-context-v3.md` | This implementation commit | Vayu gateway v3 resumable-turn context; CI pending |

## Required preflight for future runs
1. Read `docs/KUPPA_CONSTITUTION.md`.
2. Inspect the latest commit, current CI/build state, this index, and `BASELINE.md`.
3. Read the latest evolution record and compare its next target with current regressions.
4. Fix regressions before feature work.
5. Validate success plus a relevant fallback/failure path.
6. Publish code/config/UI changes only with the corresponding evolution record in the same commit.

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
| 2026-08-26 03:00 Heart | `2026/08/2026-08-26-0300-resumable-turn-context-v3.md` | `58bc60f202ca70b58ded83df92cda66e732ebed3` | Vayu gateway v3 resumable-turn context; CI #106 caught 2 legacy router regressions |
| 2026-08-26 03:15 Heart | `2026/08/2026-08-26-0315-v3-router-compatibility-fix.md` | `34882775c025ec793decf8846166e700f71a5beb` | Legacy router compatibility restored while preserving v3; CI #107 green |
| 2026-08-26 03:25 Heart | `2026/08/2026-08-26-0325-v3-turn-context-validation.md` | `ad59137949ac54aca745d948030d03f8b2163e94` | Documentation-only CI closeout and baseline promotion to repaired v3 |
| 2026-08-26 15:00 UI | `2026/08/2026-08-26-1500-explicit-resumable-turn-controls.md` | `7ac2b7f2b879ce5f1962e610ab9433c57230e4f7` | Explicit one-shot Continue / Correct / New topic controls; CI #109 green |
| 2026-08-26 15:10 UI | `2026/08/2026-08-26-1510-explicit-turn-controls-validation.md` | `362538113838df4fbcdd53605c85dc0335b29e70` | Documentation-only CI closeout and baseline promotion |
| 2026-08-27 03:00 Heart | `2026/08/2026-08-27-0300-correlation-keyed-conversation-persistence.md` | `74ef76ee8624b4d6df256311d13ce15455646556` | Correlation-keyed persistence + parent restoration; CI #111 green |
| 2026-08-27 03:10 Heart | `2026/08/2026-08-27-0310-correlation-persistence-validation.md` | `f315a2d75ebd928bd28e0e17bc928cf1667e54ab` | Documentation-only CI closeout and baseline promotion |
| 2026-08-27 15:00 UI | `2026/08/2026-08-27-1500-session-scoped-continuity-recovery.md` | `33ad4d0b1c76bf7886f33d165b5fee1a4da989b3` | Same-browser refresh recovery for explicit continuity; CI #113 green |
| 2026-08-27 15:10 UI | `2026/08/2026-08-27-1510-session-continuity-validation.md` | `e9cacd5bfbb4610f5863c46e5646bcd3c4251a95` | Documentation-only CI closeout and baseline promotion |
| 2026-08-27 16:30 Heart | `2026/08/2026-08-27-1630-signed-continuity-session-contract.md` | `a4c9171eda1e6f6035e9f35ae766defab26b2aba` | Signed continuity-session possession contract; CI #115 green; promoted via `a2adb3b89cc1dad11be4ef2f20ccff6fb70494b7` |
| 2026-08-27 16:40 Heart | `2026/08/2026-08-27-1640-signed-continuity-session-validation.md` | `b8d0888cc2d8f5ffae7b95b8ff92e9919628a071` | Documentation-only CI closeout and baseline promotion |
| 2026-08-28 03:00 Heart | `2026/08/2026-08-28-0300-owner-device-trust-boundary.md` | `46fd36cdf88e6441e56fc41c63e181ef64dc0d6c` | Owner-enrolled device credential + owner-gated continuity issuance; CI #118 green |
| 2026-08-28 03:10 Heart | `2026/08/2026-08-28-0310-owner-device-validation.md` | validation closeout | Documentation-only CI closeout and baseline promotion |
| 2026-08-29 03:00 Heart | `2026/08/2026-08-29-0300-device-signing-key-rotation.md` | pending validation | Dedicated device-token signing key + previous-key rotation window; not promoted until CI green |

## Required preflight for future runs
1. Read `docs/KUPPA_CONSTITUTION.md`.
2. Inspect the latest commit, current CI/build state, this index, and `BASELINE.md`.
3. Read the latest evolution record and compare its next target with current regressions.
4. Fix regressions before feature work.
5. Validate success plus a relevant fallback/failure path.
6. Publish code/config/UI changes only with the corresponding evolution record in the same commit.

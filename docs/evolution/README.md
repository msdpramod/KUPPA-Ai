# KUPPA Evolution Index

KUPPA evolves under a HEART/BRAIN boundary: KUPPA is the human-facing HEART; Vayu is the reasoning/orchestration BRAIN. Every evolution commit must carry a same-commit record under `docs/evolution/YYYY/MM/` and preserve approval-gated consequential actions.

## Recent chronological records

| Date / cycle | Record | Commit | Result |
|---|---|---|---|
| 2026-09-03 03:00 Heart | `2026/09/2026-09-03-0300-explicit-owner-memory-forget.md` | `da8c13b42011360eb63ce30dd14fa0abf1e414a1` | Exact owner memory forgetting; CI #161 green |
| 2026-09-03 03:10 Heart | `2026/09/2026-09-03-0310-explicit-owner-memory-forget-validation.md` | closeout commit | Validation/baseline promotion; runtime remains `da8c13b...` |
| 2026-09-03 15:00 UI | `2026/09/2026-09-03-1500-explicit-continuity-adapter.md` | `b96bf1f08da4d3c0935b93a36b7a647d2db7951d` | CI #164 caught one new contract assertion mismatch; not promoted |
| 2026-09-03 15:10 UI | `2026/09/2026-09-03-1510-continuity-adapter-contract-fix.md` | `8efd0be0283f29368c5605c5c4a5782d59914e2b` | Contract assertion aligned; CI #165 green |
| 2026-09-03 15:20 UI | `2026/09/2026-09-03-1520-continuity-adapter-validation.md` | `c47cbea076e54c657c58617764c0be08125389b7` | Baseline promotion; CI #166 green |
| 2026-09-04 03:00 Heart | `2026/09/2026-09-04-0300-privacy-safe-memory-change-observability.md` | `bae44bab17dc9402fc4abcf195165a51398d82e4` | Privacy-safe memory mutation observability; CI #168 green |
| 2026-09-04 03:10 Heart | `2026/09/2026-09-04-0310-memory-observability-validation.md` | closeout commit | Baseline promotion; runtime remains `bae44bab...` |
| 2026-09-04 15:00 UI | `2026/09/2026-09-04-1500-presence-latency-perception.md` | `6be4e77272a3e43ce0f64ba6f8c7f7b2d634dfdd` | Presence/latency accessibility evolution; CI #171 green |
| 2026-09-04 15:10 UI | `2026/09/2026-09-04-1510-presence-latency-validation.md` | `8ac74ecf6a9b368396c1be3961ea27cd3b9ef290` | Validation/baseline promotion; runtime remains `6be4e772...` |
| 2026-09-05 03:00 Heart | `2026/09/2026-09-05-0300-owner-memory-history-contract.md` | `ac082dce5d68c6908f5c843fded23df11204ce83` | Typed owner-authenticated privacy-safe memory history; CI #180 green |
| 2026-09-05 03:10 Heart | `2026/09/2026-09-05-0310-owner-memory-history-validation.md` | closeout commit | Validation/baseline promotion; runtime remains `ac082dce...` |
| 2026-09-05 15:00 UI | `2026/09/2026-09-05-1500-state-aware-avatar-motion.md` | `426132cbd7a9e01e6fbbf55219f2d7e5a60b8ae3` | State-aware Three.js motion and reduced-motion policy; CI #182 green |
| 2026-09-05 15:10 UI | `2026/09/2026-09-05-1510-state-aware-avatar-motion-validation.md` | closeout commit | Validation/baseline promotion; runtime remains `426132cb...` |

Older evolution records remain under dated directories and in Git history.

## Required preflight for future runs
1. Read `docs/KUPPA_CONSTITUTION.md`.
2. Inspect latest commit, CI/build state, this index, and `BASELINE.md`.
3. Read the latest evolution record and compare its next target with current regressions.
4. Fix regressions before feature work.
5. Validate success plus a relevant failure/fallback path.
6. Publish code/config/UI changes only with the corresponding evolution record in the same commit.

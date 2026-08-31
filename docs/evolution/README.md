# KUPPA Evolution Index

KUPPA evolves under a HEART/BRAIN boundary: KUPPA is the human-facing HEART; Vayu is the reasoning/orchestration BRAIN. Every evolution commit must carry a same-commit record under `docs/evolution/YYYY/MM/` and must preserve approval-gated consequential actions.

## Chronological records

| Date / cycle | Record | Commit | Result |
|---|---|---|---|
| 2026-08-22 15:00 UI | `2026/08/2026-08-22-1500-governance-bootstrap.md` | `b7b937f8e87af8882619f850078f84173b2d3b85` | Governance log bootstrapped; runtime unchanged |
| 2026-08-22 15:15 UI | `2026/08/2026-08-22-1515-reactive-state-engine.md` | `5a8357eabed348534484d161d94c7d988c90244b` | Nine-state interaction engine + barge-in |
| 2026-08-24 03:00 Heart | `2026/08/2026-08-24-0300-vayu-brain-gateway-v1.md` | `930bd83fd3bb64559c4b5ab9da29b7201da9a223` | Versioned Vayu gateway; CI #98 green |
| 2026-08-24 15:00 UI | `2026/08/2026-08-24-1500-vayu-presence-ui.md` | `1efac9e2485a6181413b30a003a88654c3cd9792` | Vayu presence UI; CI #100 green |
| 2026-08-25 03:00 Heart | `2026/08/2026-08-25-0300-vayu-cancellable-handoff.md` | `7e0df512eeb416a0bd0dfb3d4e8873a16195057c` | Vayu v2 cancellation; CI #102 green |
| 2026-08-25 15:00 UI | `2026/08/2026-08-25-1500-correlation-aware-barge-in.md` | `2677674a4032ea38b3019ffba04816748793b734` | Correlation-aware barge-in; CI #104 green |
| 2026-08-26 03:00 Heart | `2026/08/2026-08-26-0300-resumable-turn-context-v3.md` | `58bc60f202ca70b58ded83df92cda66e732ebed3` | Vayu v3; CI #106 exposed legacy regressions |
| 2026-08-26 03:15 Heart | `2026/08/2026-08-26-0315-v3-router-compatibility-fix.md` | `34882775c025ec793decf8846166e700f71a5beb` | Regression repaired; CI #107 green |
| 2026-08-26 15:00 UI | `2026/08/2026-08-26-1500-explicit-resumable-turn-controls.md` | `7ac2b7f2b879ce5f1962e610ab9433c57230e4f7` | Continue/Correct/New topic controls; CI #109 green |
| 2026-08-27 03:00 Heart | `2026/08/2026-08-27-0300-correlation-keyed-conversation-persistence.md` | `74ef76ee8624b4d6df256311d13ce15455646556` | Correlation persistence; CI #111 green |
| 2026-08-27 15:00 UI | `2026/08/2026-08-27-1500-session-scoped-continuity-recovery.md` | `33ad4d0b1c76bf7886f33d165b5fee1a4da989b3` | Refresh recovery; CI #113 green |
| 2026-08-27 16:30 Heart | `2026/08/2026-08-27-1630-signed-continuity-session-contract.md` | `a4c9171eda1e6f6035e9f35ae766defab26b2aba` | Signed continuity; CI #115 green |
| 2026-08-28 03:00 Heart | `2026/08/2026-08-28-0300-owner-device-trust-boundary.md` | `46fd36cdf88e6441e56fc41c63e181ef64dc0d6c` | Owner device trust; CI #118 green |
| 2026-08-29 03:00 Heart | `2026/08/2026-08-29-0300-device-signing-key-rotation.md` | `b88adffb3bd44985bb38feb40c868050aaba70bf` | Device signing-key rotation; CI #121 green |
| 2026-08-29 15:00 UI | `2026/08/2026-08-29-1500-owner-device-continuity-ui.md` | `d938200ea9a70a2cb55b71830663d6decc7a4a5e` | Owner-device continuity UI; CI #125 green |
| 2026-08-29 16:30 Heart | `2026/08/2026-08-29-1630-persistent-owner-device-revocation.md` | `93e59e784eb4ea0b30a8b0021895975da088f3b5` | Persistent revocation; CI #128 green |
| 2026-08-30 03:00 Heart | `2026/08/2026-08-30-0300-owner-device-management-boundary.md` | `5cfc129274addf813f38af555f9931a13d61010c` | Management boundary; CI #131 caught compile regression |
| 2026-08-30 03:10 Heart | `2026/08/2026-08-30-0310-owner-management-constructor-fix.md` | `c726f7fef6f9fccb5709ec7e741d41f11a1264ad` | Regression repaired; CI #132 green |
| 2026-08-31 03:00 Heart | `2026/08/2026-08-31-0300-owner-device-audit-ledger.md` | `34d762d71b752fcaa88c89b9acc0add6780d7a66` | Sanitized trust audit ledger; CI #135 green |
| 2026-08-31 15:00 UI | `2026/08/2026-08-31-1500-trusted-devices-sheet.md` | `aad8bae532af917c05bc879eb357109a962d3464` | Trusted-device sheet; CI #145 caught one over-broad new contract-test assertion; not promoted |
| 2026-08-31 15:10 UI | `2026/08/2026-08-31-1510-trusted-devices-contract-fix.md` | validation pending | Correct credential-exposure guard + avoid repeat management-key prompt; full CI required |

## Required preflight for future runs
1. Read `docs/KUPPA_CONSTITUTION.md`.
2. Inspect latest commit, CI/build state, this index, and `BASELINE.md`.
3. Read the latest evolution record and compare its next target with current regressions.
4. Fix regressions before feature work.
5. Validate success plus a relevant fallback/failure path.
6. Publish code/config/UI changes only with the corresponding evolution record in the same commit.

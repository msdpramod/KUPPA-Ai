# KUPPA Evolution Index

KUPPA evolves under a HEART/BRAIN boundary: KUPPA is the human-facing HEART; Vayu is the reasoning/orchestration BRAIN. Every evolution commit must carry a same-commit record under `docs/evolution/YYYY/MM/` and preserve approval-gated consequential actions.

## Recent chronological records

| Date / cycle | Record | Commit | Result |
|---|---|---|---|
| 2026-08-29 03:00 Heart | `2026/08/2026-08-29-0300-device-signing-key-rotation.md` | `b88adffb3bd44985bb38feb40c868050aaba70bf` | CI #121 green |
| 2026-08-29 15:00 UI | `2026/08/2026-08-29-1500-owner-device-continuity-ui.md` | `d938200ea9a70a2cb55b71830663d6decc7a4a5e` | CI #125 green |
| 2026-08-29 16:30 Heart | `2026/08/2026-08-29-1630-persistent-owner-device-revocation.md` | `93e59e784eb4ea0b30a8b0021895975da088f3b5` | CI #128 green |
| 2026-08-30 03:00 Heart | `2026/08/2026-08-30-0300-owner-device-management-boundary.md` | `5cfc129274addf813f38af555f9931a13d61010c` | CI #131 caught compile regression |
| 2026-08-30 03:10 Heart | `2026/08/2026-08-30-0310-owner-management-constructor-fix.md` | `c726f7fef6f9fccb5709ec7e741d41f11a1264ad` | CI #132 green |
| 2026-08-31 03:00 Heart | `2026/08/2026-08-31-0300-owner-device-audit-ledger.md` | `34d762d71b752fcaa88c89b9acc0add6780d7a66` | CI #135 green |
| 2026-08-31 15:00 UI | `2026/08/2026-08-31-1500-trusted-devices-sheet.md` | `aad8bae532af917c05bc879eb357109a962d3464` | CI #145 caught one over-broad new assertion; not promoted |
| 2026-08-31 15:10 UI | `2026/08/2026-08-31-1510-trusted-devices-contract-fix.md` | `0f57af0525ea869a0fc853e51045f25ea2ab85a1` | Assertion repaired + repeat prompt removed; CI #146 green |
| 2026-08-31 15:20 UI | `2026/08/2026-08-31-1520-trusted-devices-validation.md` | closeout commit | Validation/baseline promotion; runtime remains `0f57af05...` |
| 2026-08-31 15:40 UI | `2026/08/2026-08-31-1540-in-app-device-pairing.md` | `2e3f4c2575bba55af3fedec87db6b78253c309f9` | In-app pairing + management credential forms; CI #149 green |
| 2026-08-31 15:50 UI | `2026/08/2026-08-31-1550-in-app-device-pairing-validation.md` | this closeout commit | Documentation-only validation and baseline promotion; runtime remains `2e3f4c...` |

Older evolution records remain under `docs/evolution/2026/08/` and in Git history.

## Required preflight for future runs
1. Read `docs/KUPPA_CONSTITUTION.md`.
2. Inspect latest commit, CI/build state, this index, and `BASELINE.md`.
3. Read the latest evolution record and compare its next target with current regressions.
4. Fix regressions before feature work.
5. Validate success plus a relevant failure/fallback path.
6. Publish code/config/UI changes only with the corresponding evolution record in the same commit.

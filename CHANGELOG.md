# Changelog

## 2026-09-04
### Changed
- Added structured privacy-safe outcomes for explicit conversational memory forget requests.
- Added `MEMORY_FORGOTTEN` and `MEMORY_FORGET_NO_MATCH` audit events with affected count/category metadata only.
- Removed raw personal-memory text from new `MEMORY_CAPTURED` audit detail; category, confidence and source remain observable.

### Safety
- Exact-match-only forgetting remains unchanged; near/partial matches never delete memory.
- No Vayu reasoning, planning, retrieval, tool/agent orchestration or execution behavior changed.
- No consequential-action approval behavior changed.
- No new database schema, runtime dependency, secret, or configuration was introduced.

## 2026-09-03
### Changed
- Added explicit conversational memory forgetting for `forget that ...` and `please forget that ...` owner instructions.
- Forgetting is exact-match only after case/whitespace/terminal-punctuation normalization; partial and fuzzy matches are intentionally not deleted.
- Added a frozen, versioned `KuppaContinuityAdapter v1` boundary for Trusted Devices.

### Safety
- No Vayu cognition or consequential-action approval behavior changed.
- No fuzzy/semantic bulk deletion path was added.

## Earlier changes
See `docs/evolution/README.md`, the dated evolution records, and Git history for prior governed changes.

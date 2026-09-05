# Changelog

## 2026-09-05
### Changed
- Added an owner-authenticated typed memory-change history endpoint at `GET /api/chat/owner/memory-history`.
- Memory history is limited to `MEMORY_CAPTURED`, `MEMORY_FORGOTTEN`, and `MEMORY_FORGET_NO_MATCH` and returns bounded metadata only.
- Added `KuppaAvatarMotionPolicy v1` so Three.js avatar movement is state-aware and honors `prefers-reduced-motion`.
- Reduced-motion mode now removes continuous autonomous head/bob motion and substantially damps pointer-driven gaze while preserving static state, facial expression and lip-sync cues.

### Safety
- The new endpoint does not expose raw personal-memory text, raw audit detail, internal memory IDs, or correlation IDs.
- Reuses the existing owner-management permission boundary; no new secret or authentication bypass was introduced.
- Avatar motion policy is presentation-only and contains no chat/network/Vayu reasoning path.
- No Vayu reasoning, planning, retrieval, tool/agent orchestration or execution behavior changed.
- No consequential-action approval behavior changed.
- No new database schema or runtime dependency was introduced.

## 2026-09-04
### Changed
- Added structured privacy-safe outcomes for explicit conversational memory forget requests.
- Added `MEMORY_FORGOTTEN` and `MEMORY_FORGET_NO_MATCH` audit events with affected count/category metadata only.
- Removed raw personal-memory text from new `MEMORY_CAPTURED` audit detail; category, confidence and source remain observable.
- Added `KuppaPresenceController v1` for presentation-only state presence and elapsed Vayu wait feedback.
- Added accessible `aria-busy` semantics for processing states and reduced-motion handling for new mic/presence animations.

### Safety
- Exact-match-only forgetting remains unchanged; near/partial matches never delete memory.
- Presence/latency UI consumes existing state events only and adds no chat/network/reasoning path.
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

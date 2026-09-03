# 2026-09-04 03:00 — Privacy-Safe Memory Change Observability

## Cycle
Heart / Personality / Relationship.

## Commit purpose and hypothesis
Make explicit owner memory forgetting observable without logging the private memory text. Hypothesis: KUPPA can expose a typed internal mutation outcome and write bounded audit metadata for successful and unmatched forget requests while preserving exact-match-only deletion.

## Architectural context
KUPPA remains the HEART: identity, relationship continuity, personal memory interface, conversational warmth and trust. Vayu remains the BRAIN: reasoning, planning, retrieval, orchestration, tools, specialist agents and execution strategy. This change does not move cognition into KUPPA.

## Detailed changes
- Added `CaptureOutcome` and `MemoryMutation` to the KUPPA memory-capture boundary.
- Successful exact forget operations report only mutation type, affected count and memory categories.
- Near-match/partial forget requests report `FORGET_NO_MATCH` and never delete a candidate.
- Chat audit now records `MEMORY_FORGOTTEN` or `MEMORY_FORGET_NO_MATCH` using correlation ID plus bounded metadata.
- Existing `MEMORY_CAPTURED` audit detail no longer includes raw memory content; it records category, confidence and source instead.
- Kept `capture(String)` as a compatibility wrapper over `process(String)`.

## Files/components affected
- `ConversationMemoryCaptureService`
- `ChatService`
- `ConversationMemoryCaptureServiceTest`
- `CHANGELOG.md`
- `docs/evolution/README.md`
- this evolution record

## Behavior before
Exact conversational forgetting deactivated matching memory but returned no structured mutation result. Chat audit contained raw captured-memory content and no explicit forget/no-match event.

## Behavior after
KUPPA emits privacy-safe mutation metadata for explicit forget requests. Exact matches are still the only deletions. Near matches remain untouched and are observable as no-match requests. Captured-memory audit metadata no longer repeats private content.

## KUPPA/Vayu responsibility impact
KUPPA gains memory-change observability only. Vayu Gateway v3, provider routing, planning, retrieval, tool selection, agents, cancellation and execution are unchanged.

## API/event/schema/config/migration changes
- Public HTTP API: none.
- Internal Java contract: `ConversationMemoryCaptureService.process(String)` returns `CaptureOutcome`.
- Audit event types added: `MEMORY_FORGOTTEN`, `MEMORY_FORGET_NO_MATCH`.
- Schema/config/migrations: none.

## Tests/build/lint/smoke checks run with results
Pre-commit source tests added for successful exact forget and near-match no-delete fallback. Full Maven CI is required before promotion; result will be recorded in the validation closeout. Existing baseline CI #165 was green before this change.

## Relevant before/after metrics
- Structured forget mutation outcomes: 0 -> 2 (`FORGOTTEN`, `FORGET_NO_MATCH`).
- Raw private memory content in new capture audit detail: present -> removed.
- Fuzzy/semantic deletion paths: 0 -> 0.
- Vayu cognition changes: 0.
- New runtime dependencies/schema changes: 0.

## Security/privacy/permission implications
Improves privacy by removing raw memory content from capture audit detail and by recording forget events without the requested or stored memory text. No new secret, credential, permission, external action or self-modification capability is introduced.

## Known limitations
The generic audit store remains developer-oriented and is not yet a dedicated owner-facing memory-history API. Near-match requests are observable but KUPPA does not yet present candidates conversationally for disambiguation.

## Failures/fallbacks tested
Near/partial match produces a no-match outcome, leaves the existing memory active, and performs no repository save. Blank/ordinary conversation remains a no-op.

## Rollback procedure / known-good reference
Rollback to governed head `5f7ceab61c8cafa900c0859f86fe9b24ae951f69`, restoring validated runtime `b96bf1f08da4d3c0935b93a36b7a647d2db7951d` / repair head `8efd0be0283f29368c5605c5c4a5782d59914e2b` from CI #165. No database rollback is required.

## Risks / technical debt introduced or removed
Removed: silent forget operations and raw captured-memory content duplication in audit detail. Remaining: owner-facing typed history and safe candidate disambiguation are still absent.

## Dependencies
No new runtime/build dependencies.

## Screenshots / visual references
Not applicable; no UI change.

## Follow-up work
Add an owner-authenticated typed memory-change history contract and then a conversational near-match disambiguation flow that requires explicit owner selection before deletion.

## Next evolution target
Heart: owner-authenticated typed memory-change history or safe candidate disambiguation. UI: avatar presence/micro-interactions after the continuity-adapter baseline remains green.

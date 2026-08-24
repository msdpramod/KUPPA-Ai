# KUPPA Evolution Record — Cancellable Vayu Handoff

- **Date/time:** 2026-08-25 03:00 Asia/Kolkata
- **Cycle:** Heart / Personality / Relationship
- **Commit purpose:** Give KUPPA a clean, observable way to identify and request cancellation of an in-flight Vayu brain turn without moving reasoning or execution into the HEART.
- **Hypothesis:** A caller-visible request identity plus cooperative cancellation will reduce stale-response risk during interruptions and create a stable foundation for resumable, topic-changing conversations.

## Architectural context
KUPPA is the HEART and Vayu is the BRAIN. The previous UI cycle made Vayu health visible but documented that barge-in stopped speech only. `VayuBrainGateway v1` generated correlation IDs internally and only returned them after reasoning completed, so the HEART could not target an in-flight brain turn.

## Detailed changes
- Upgraded `VayuBrainGateway` contract from `v1` to `v2`.
- Added optional caller-provided correlation IDs while preserving generated IDs for legacy callers.
- Added `VayuRequestLifecycle`, a concurrency-safe in-process registry for active brain turns.
- Added `POST /api/chat/{correlationId}/cancel` returning `CANCEL_REQUESTED` or `NOT_ACTIVE`.
- Added cooperative cancellation: if cancellation is accepted while Vayu is working, the provider result is suppressed and the gateway returns `VAYU_CANCELLED` instead of a stale answer.
- Added explicit `cancelled` metadata to Vayu responses and audit output.
- Preserved the existing action-intent short circuit: consequential actions still become approval proposals before brain execution.
- Added concurrency-focused regression tests for active-turn cancellation, caller correlation preservation, unknown-turn cancellation, healthy routing, and degraded routing.
- Added ADR 0003 for the v2 cancellation contract.

## Files/components affected
- `src/main/java/ai/kuppa/conversation/VayuRequestLifecycle.java`
- `src/main/java/ai/kuppa/conversation/VayuBrainGateway.java`
- `src/main/java/ai/kuppa/planner/Planner.java`
- `src/main/java/ai/kuppa/planner/LocalPlanner.java`
- `src/main/java/ai/kuppa/chat/ChatController.java`
- `src/main/java/ai/kuppa/chat/ChatService.java`
- `src/test/java/ai/kuppa/conversation/VayuBrainGatewayTest.java`
- `docs/adr/0003-vayu-brain-gateway-v2-cancellation.md`
- `CHANGELOG.md`
- `docs/evolution/README.md`
- this evolution record

## Behavior before
The browser could interrupt KUPPA speech but had no correlation ID available before Vayu completed. There was no cancellation API and no deterministic way to suppress a brain response that became stale because the user changed direction mid-turn.

## Behavior after
A caller may generate a turn ID before invoking `/api/chat`, pass it as `correlationId`, and request cancellation against that same ID while Vayu is still active. If accepted, Vayu marks the turn cancelled and discards the eventual provider answer in favor of a stable `VAYU_CANCELLED` response.

## KUPPA/Vayu responsibility impact
Vayu gains ownership of brain-turn lifecycle and cancellation semantics. KUPPA gains no reasoning, planning, provider-routing, retrieval, tool-selection, or execution logic. The HEART can only supply an ID, request cancellation, and later present the BRAIN's lifecycle state.

## API/event/schema/config/migration changes
- `POST /api/chat` request gains optional `correlationId`.
- New endpoint: `POST /api/chat/{correlationId}/cancel`.
- Brain contract version: `v1 -> v2`.
- Brain response gains boolean `cancelled`.
- New stable brain error: `VAYU_CANCELLED`.
- New cancellation statuses: `CANCEL_REQUESTED`, `NOT_ACTIVE`.
- No database migration or configuration change.

## Tests/build/lint/smoke checks run with results
- Preflight: branch head `99f793c95eb9893caf87b9dc8b7b2d1c43d4ca8f`; known-good runtime `1efac9e2485a6181413b30a003a88654c3cd9792`; CI #100 green.
- Constitution and latest evolution record reviewed before implementation.
- Fresh local clone/Maven pre-publish run attempted, but the execution environment could not resolve `github.com`; no Maven result was available locally.
- Added `VayuBrainGatewayTest` coverage for the v2 contract, caller-visible correlation, active cancellation, stale-result suppression, unknown-turn cancellation, normal Ollama result, and degraded/unavailable result.
- Detached commit/tree content will be inspected before moving the branch ref. Post-publish GitHub Actions is the authoritative build gate; on failure the branch must be rolled back to `99f793c95eb9893caf87b9dc8b7b2d1c43d4ca8f`.

## Relevant before/after metrics
- Brain contract versions with cancellable lifecycle: **0 -> 1** (`v2`).
- Cancellation endpoints: **0 -> 1**.
- Caller-known correlation IDs before reasoning completes: **0 -> 1 per opted-in turn**.
- Deterministic stale-result suppression after accepted cancellation: **no -> yes**.
- Provider-native compute cancellation: **no -> no** (explicitly not claimed).
- Approval-gate behavior changed: **0**.

## Security/privacy/permission implications
No credentials, external destinations, new tool permissions, autonomous actions, shell execution, or self-modification were introduced. The lifecycle registry contains only active correlation IDs and cancellation flags and removes them when the gateway call finishes. Consequential actions remain approval gated.

## Known limitations
- Cancellation is cooperative at the Vayu gateway boundary. The current synchronous Ollama/OpenAI call may still consume compute until it returns.
- The current avatar UI does not yet generate a correlation ID before each turn or invoke the cancellation endpoint; that is intentionally deferred to the next UI cycle.
- The registry is process-local; a multi-instance Vayu deployment will require distributed lifecycle state or request affinity.

## Failures/fallbacks tested
Tests cover cancellation of an active blocked brain call and rejection of cancellation for a non-active ID. Existing degraded-provider behavior remains covered. The cancelled path returns a stable, non-provider result and does not expose the stale provider answer.

## Rollback procedure / known-good reference
If build, API compatibility, handoff reliability, or approval behavior regresses, restore branch head `99f793c95eb9893caf87b9dc8b7b2d1c43d4ca8f`. Validated runtime baseline remains `1efac9e2485a6181413b30a003a88654c3cd9792` until CI validates this implementation.

## Risks / technical debt introduced or removed
Removes the missing request-identity/cancellation contract. Adds a process-local lifecycle registry and a v2 response field; both are intentionally small. Distributed cancellation and provider-native interruption remain future work.

## Dependencies
No new dependencies.

## Screenshots / visual references
Not applicable; this Heart cycle changes the cognition boundary, not the avatar UI.

## Follow-up work
Wire the avatar's barge-in/topic-change controls to generate correlation IDs before `/api/chat`, call the cancellation endpoint, ignore stale responses, and expose `VAYU_CANCELLED` as an interrupted—not failed—brain state.

## Next evolution target
15:00 UI cycle: correlation-aware barge-in and text topic supersession using Vayu Brain Gateway v2, with stale-response protection and accessible interruption feedback.

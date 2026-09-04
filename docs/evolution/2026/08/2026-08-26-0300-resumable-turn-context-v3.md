# KUPPA Evolution Record — Resumable Turn Context v3

- **Date/time:** 2026-08-26 03:00 Asia/Kolkata
- **Cycle:** Heart / Personality / Relationship
- **Commit purpose:** Add an explicit resumable-turn context contract so KUPPA can carry new-topic, continuation, and correction intent to Vayu without taking over reasoning.
- **Hypothesis:** Explicit turn-relation metadata will improve conversational continuity and correction handling while preserving the KUPPA HEART / Vayu BRAIN boundary and existing cancellation semantics.

## Architectural context
The previous validated runtime (`2677674a4032ea38b3019ffba04816748793b734`, CI #104 green) supports correlation-aware cancellation and stale-response suppression. Recent conversation is available to the brain, but every new request is structurally identical at the KUPPA -> Vayu boundary. The latest evolution closeout explicitly identified resumable-turn semantics as the next Heart target.

## Detailed changes
- Upgraded `VayuBrainGateway` from contract `v2` to `v3`.
- Added normalized `TurnContext` metadata with `AUTO`, `NEW_TOPIC`, `CONTINUE`, and `CORRECTION` modes plus optional `parentCorrelationId`.
- Extended `/api/chat` input with optional `turnMode` and `parentCorrelationId` while preserving old request compatibility.
- Propagated turn context through `ChatService`, `Planner`, `LocalPlanner`, and the Vayu gateway.
- Added turn mode and parent correlation metadata to Vayu handoff audit details and brain responses.
- Routed the same continuity directive to Ollama and the OpenAI fallback so provider behavior stays aligned.
- Kept invalid or missing modes safe by normalizing them to `AUTO` rather than guessing in KUPPA.
- Extended gateway tests for v3 metadata, continuation, correction, invalid-mode fallback, cancellation, degraded mode, and caller correlation IDs.

## Files/components affected
- `src/main/java/ai/kuppa/chat/ChatController.java`
- `src/main/java/ai/kuppa/chat/ChatService.java`
- `src/main/java/ai/kuppa/planner/Planner.java`
- `src/main/java/ai/kuppa/planner/LocalPlanner.java`
- `src/main/java/ai/kuppa/conversation/VayuBrainGateway.java`
- `src/main/java/ai/kuppa/conversation/BrainRouterService.java`
- `src/main/java/ai/kuppa/conversation/OllamaConversationService.java`
- `src/main/java/ai/kuppa/conversation/OpenAiConversationService.java`
- `src/test/java/ai/kuppa/conversation/VayuBrainGatewayTest.java`
- `docs/adr/0004-vayu-brain-gateway-v3-turn-context.md`
- `CHANGELOG.md`
- `docs/evolution/README.md`
- this evolution record

## Behavior before
A request could be cancelled and correlated, but Vayu received no explicit structural indication that the user meant “new topic,” “continue that,” or “I’m correcting the previous statement.” Recent history existed, so Vayu had to infer every relationship from text alone.

## Behavior after
Clients may optionally supply a turn mode and parent correlation ID. KUPPA transports the signal and exposes it in handoff metadata; Vayu receives a mode-specific reasoning directive alongside the same recent conversation and persona memory. Existing clients that send nothing behave as `AUTO` and remain backward compatible.

## KUPPA/Vayu responsibility impact
KUPPA remains the HEART. It carries user/interface continuity intent, correlation identity, memory interface, and presentation. It does not automatically infer semantic turn relationships. Vayu remains the BRAIN and interprets `AUTO`, resolves references, decides how continuation/correction affects reasoning, and owns provider routing and cognition.

## API/event/schema/config/migration changes
`POST /api/chat` accepts two new optional JSON fields:

```json
{
  "message": "Actually, use the second option",
  "correlationId": "turn-current",
  "turnMode": "CORRECTION",
  "parentCorrelationId": "turn-previous"
}
```

`brain` response metadata now includes `turnMode` and `parentCorrelationId`. `VayuBrainGateway.CONTRACT_VERSION` is `v3`. No database schema or configuration migration is required.

## Tests/build/lint/smoke checks run with results
- Repository/branch preflight: **PASS** — current head and baseline inspected.
- Existing branch CI before change: **PASS** — CI #105 green at documentation head; validated runtime remains `2677674...` / CI #104.
- Constitution review: **PASS** — no invariant conflicts.
- Fresh local clone/Maven execution: **BLOCKED** because the execution environment could not resolve `github.com`.
- Added focused automated coverage in `VayuBrainGatewayTest` for v3 contract metadata, continuation, correction, invalid-mode fallback, degraded mode, cancellation, and correlation preservation.
- Authoritative post-publish GitHub CI: **PENDING** at implementation commit creation; baseline must not be promoted until green.

## Relevant before/after metrics
- Vayu brain contract version: **v2 -> v3**.
- Explicit turn relationship modes: **0 -> 4** (`AUTO`, `NEW_TOPIC`, `CONTINUE`, `CORRECTION`).
- Parent-turn linkage fields: **0 -> 1** optional correlation link.
- Provider paths receiving continuity guidance: **0 -> 2** (Ollama + OpenAI fallback).
- Existing client request compatibility: **preserved** through optional fields and `AUTO` default.
- Approval behavior changed: **0**.
- New external action capabilities: **0**.

## Security/privacy/permission implications
No secrets, new network destinations, unrestricted shell execution, self-modification, or autonomous consequential actions were introduced. `parentCorrelationId` is context/observability metadata and is not treated as authorization. Existing action approval short-circuiting remains before Vayu execution.

## Known limitations
- The current avatar does not yet send explicit `turnMode`/`parentCorrelationId`; it continues to use `AUTO` until the next UI cycle.
- `AUTO` still relies on Vayu/model reasoning over recent conversation.
- Parent correlation identity is advisory context; the current conversation store is not keyed by correlation ID.
- Cancellation remains cooperative rather than provider-native.
- Active lifecycle state remains process-local for cloud multi-instance deployment.

## Failures/fallbacks tested
Automated gateway tests cover invalid mode -> `AUTO`, full degraded Vayu metadata, unknown cancellation, accepted active cancellation, and stale-result suppression. Both provider routes use the same normalized turn directive. GitHub CI is required before promoting this implementation to known-good.

## Rollback procedure / known-good reference
Until CI passes, the known-good runtime remains `2677674a4032ea38b3019ffba04816748793b734`. If v3 fails validation or materially regresses behavior, move the branch/runtime back to that commit. The deeper backend-only rollback remains `7e0df512eeb416a0bd0dfb3d4e8873a16195057c`.

## Risks / technical debt introduced or removed
Removed: the contract-level ambiguity between explicit continuation/correction/new-topic intent. Added: API clients now have optional semantic metadata they must use correctly when they choose to override `AUTO`. Future work should associate stored conversation turns with correlation IDs instead of treating the parent ID as advisory only.

## Dependencies
No new dependencies.

## Screenshots / visual references
Not applicable; this Heart cycle changes the cognition contract, not the avatar presentation.

## Follow-up work
After green CI, publish a documentation-only validation closeout and promote the implementation commit to the known-good baseline. The next UI cycle should let interruption/topic-change flows supply explicit turn context where the interaction state knows it with certainty, while leaving natural-language inference as `AUTO`.

## Next evolution target
UI cycle: wire explicit `NEW_TOPIC`, `CONTINUE`, and `CORRECTION` context from reliable interaction events into v3 and expose continuity state subtly through the avatar without adding a conversation window.

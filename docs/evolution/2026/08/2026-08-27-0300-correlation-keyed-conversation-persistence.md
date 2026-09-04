# KUPPA Evolution Record — Correlation-Keyed Conversation Persistence

- **Date/time:** 2026-08-27 03:00 Asia/Kolkata
- **Cycle:** Heart / Personality / Relationship
- **Commit purpose:** Persist Vayu correlation and continuity metadata with conversation messages and allow explicit Continue/Correction turns to restore their parent turn server-side.
- **Hypothesis:** Server-side parent lookup will preserve conversational continuity across browser refreshes and future multi-device/cloud sessions without moving reference reasoning into KUPPA.

## Architectural context
KUPPA remains the HEART and Vayu remains the BRAIN. Vayu Brain Gateway v3 already transports `turnMode` and `parentCorrelationId`, but the validated baseline stored only role/content/timestamp in `chat_messages`, leaving parent identity browser-local.

## Detailed changes
- Added nullable `correlationId`, `turnMode`, and `parentCorrelationId` metadata to persisted chat messages.
- Added a correlation/time index and repository lookup by correlation ID.
- ChatService now guarantees a correlation ID before persisting the user turn and uses the same ID for the KUPPA response.
- ConversationContextService can restore the persisted parent turn for explicit `CONTINUE` and `CORRECTION` modes.
- Missing parents degrade safely to ordinary recent conversation context.
- Ollama and OpenAI fallback both consume the same correlation-aware context service.

## Files/components affected
- `ChatMessage`, `ChatMessageRepository`, `ChatService`
- `ConversationContextService`
- `OllamaConversationService`, `OpenAiConversationService`
- `ConversationContextServiceTest`
- evolution index and changelog

## Behavior before
Parent turn identity existed only in the browser. A refresh/device switch could preserve the parent correlation ID only if the client carried it, but the server could not resolve that ID back to the earlier conversation.

## Behavior after
Conversation messages persist Vayu turn identity. Explicit Continue/Correction requests can retrieve the parent user/assistant messages by correlation ID and inject them into Vayu's conversational context even when they are outside the recent-turn window. AUTO and NEW_TOPIC behavior remains unchanged.

## KUPPA/Vayu responsibility impact
KUPPA only persists and transports interaction metadata. It does not infer whether text is a continuation/correction and does not resolve references. Vayu still receives the context and performs semantic reasoning.

## API/event/schema/config/migration changes
No HTTP field changes. Existing `/api/chat` fields are reused. The `chat_messages` table gains nullable `correlationId`, `turnMode`, `parentCorrelationId` columns and an index on correlation/time. Hibernate `ddl-auto:update` performs the additive schema update in current environments.

## Tests/build/lint/smoke checks run with results
Added focused unit coverage for persisted parent restoration and missing-parent fallback alongside existing chronology/deduplication/window tests. This implementation is staged for GitHub Actions validation before promotion to the governed runtime branch.

## Relevant before/after metrics
- Server-persisted turn identity fields: **0 -> 3**.
- Server-side parent lookup paths: **0 -> 1**.
- Explicit continuation survivability beyond recent-window/browser-local state: **no -> yes when parent correlation is supplied**.
- Semantic classifiers added to KUPPA: **0**.
- New external permissions/destinations: **0**.

## Security/privacy/permission implications
Correlation metadata is internal conversation metadata only. No secrets, credentials, unrestricted shell execution, self-modification, new external destinations, or autonomous consequential actions are introduced. Existing approval gates are unchanged.

## Known limitations
- The client still needs to provide a parent correlation ID after a full browser reset; server-side APIs for browsing/selecting historical parent turns are not added here.
- Correlation IDs are not authentication tokens and must not be treated as authorization.
- Hibernate auto-update is still used instead of explicit production migrations.
- No aggregate telemetry yet measures continuity quality.

## Failures/fallbacks tested
A missing/unknown parent correlation ID falls back to the recent conversation without fabricating a parent turn. Existing Vayu provider fallback/cancellation behavior is unchanged.

## Rollback procedure / known-good reference
Do not promote if CI regresses. Governed rollback remains validated runtime `7ac2b7f2b879ce5f1962e610ab9433c57230e4f7` (CI #109 green). Additive nullable columns are backward compatible with the old runtime.

## Risks / technical debt introduced or removed
Removed: server inability to resolve a known parent correlation ID. Remaining debt: explicit database migrations, conversation/session ownership model, historical-turn discovery API, retention/privacy policy, and production telemetry.

## Dependencies
No new dependencies.

## Screenshots / visual references
Not applicable; this Heart cycle changes persistence/context only.

## Follow-up work
Add a bounded, owner-scoped server API for resumable-turn discovery plus retention rules before multi-user deployment.

## Next evolution target
UI cycle: restore last-completed correlation metadata from server/session state after refresh without reintroducing a conversation window, then validate accessible continuity controls and degraded behavior.

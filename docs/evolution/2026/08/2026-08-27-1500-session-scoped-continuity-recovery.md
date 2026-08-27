# KUPPA Evolution Record — Session-Scoped Continuity Recovery

- **Date/time:** 2026-08-27 15:00 Asia/Kolkata
- **Cycle:** Body / UI / Human Interaction
- **Commit purpose:** Restore explicit Continue/Correct capability after browser refresh without restoring a conversation window or exposing transcript content.
- **Hypothesis:** A high-entropy browser session key plus metadata-only server recovery will preserve conversational continuity across refresh while keeping KUPPA as the HEART and semantic reference reasoning in Vayu.

## Architectural context
The 03:00 Heart cycle made turn correlation persistent and allowed Vayu to resolve a supplied historical parent server-side. The remaining UI gap was discovery: after a refresh, `lastCompletedCorrelationId` was browser-memory-only, so Continue and Correct became disabled even though the server still had the turn. A global latest-turn endpoint would be unsafe for future cloud/multi-user use, so this cycle introduces browser-session scoping instead.

## Detailed changes
- Added nullable `clientSessionId` metadata to `chat_messages` plus a session/role/created-time index.
- Added `ChatContinuityService` with bounded session-ID validation and metadata-only latest-turn recovery.
- Added `GET /api/chat/resumable?clientSessionId=...` returning only `available`, `correlationId`, and `completedAt`.
- Extended `/api/chat` requests with optional `clientSessionId`; older clients remain compatible.
- Excluded cancelled Vayu responses from resumable-session indexing by not attaching the session key to cancelled KUPPA responses.
- Added browser generation/persistence of `kuppa.clientSessionId.v1` using `crypto.randomUUID()` when available.
- Added startup recovery that re-enables Continue/Correct after refresh and emits `kuppa-continuity-restored` without reconstructing a transcript.
- Added UI contract and service tests for successful recovery, invalid-session rejection, and missing-session fallback.

## Files/components affected
- `src/main/java/ai/kuppa/chat/ChatMessage.java`
- `src/main/java/ai/kuppa/chat/ChatMessageRepository.java`
- `src/main/java/ai/kuppa/chat/ChatContinuityService.java`
- `src/main/java/ai/kuppa/chat/ChatController.java`
- `src/main/java/ai/kuppa/chat/ChatService.java`
- `src/main/resources/static/index.html`
- `src/test/java/ai/kuppa/chat/ChatContinuityServiceTest.java`
- `src/test/java/ai/kuppa/ui/AvatarBrainPresenceContractTest.java`
- `CHANGELOG.md`
- `docs/evolution/README.md`
- this record

## Behavior before
Refresh/reset of the page cleared `lastCompletedCorrelationId`, disabling Continue and Correct despite correlation-keyed turn metadata already existing server-side.

## Behavior after
The same browser profile keeps an opaque session identifier. On startup KUPPA asks only for the latest resumable correlation metadata for that browser session. If present, Continue and Correct are re-enabled. If absent/invalid/unavailable, KUPPA simply starts without a parent and does not fabricate continuity.

## KUPPA/Vayu responsibility impact
KUPPA HEART gains persistence of interaction continuity identity only. It does not inspect transcript semantics or decide what a reference means. Vayu BRAIN still interprets `CONTINUE`/`CORRECTION`, resolves parent context, reasons, plans, retrieves, and orchestrates tools.

## API/event/schema/config/migration changes
- `/api/chat` adds optional `clientSessionId`.
- New `GET /api/chat/resumable?clientSessionId=...` metadata-only endpoint.
- New browser event: `kuppa-continuity-restored` (plus failure telemetry event).
- `chat_messages` adds nullable `clientSessionId` and `idx_chat_session_role_created` through the existing Hibernate schema-update strategy.
- No new configuration keys or dependencies.

## Tests/build/lint/smoke checks run with results
Preflight evidence: current runtime baseline `74ef76ee8624b4d6df256311d13ce15455646556`; GitHub Actions CI #111 passed full Maven Test. New automated tests are included in this commit; authoritative post-commit CI is required before baseline promotion. Relevant failure paths encoded in tests: invalid session ID => unavailable/no repository access; unknown session => unavailable/no fabricated parent.

## Relevant before/after metrics
- Refresh-restorable parent identity: **0 -> 1 session-scoped path**.
- Session-scoped recovery endpoint: **0 -> 1**.
- Transcript fields exposed by recovery API: **0 -> 0**.
- Cancelled turns eligible for session recovery: **possible if globally searched -> explicitly excluded from session recovery**.
- Browser recovery events: **0 -> 2 (success/failure observability)**.
- Conversation windows: **0 -> 0**.
- Semantic classifiers added to KUPPA: **0**.
- Approval behavior changed: **0**.

## Security/privacy/permission implications
The browser session ID is high entropy and scoped to continuity lookup, but it is explicitly **not authentication or authorization**. The recovery API deliberately returns no message text, persona memory, actions, tool output, or user profile data. Multi-user cloud deployment still requires real owner/session authentication before this mechanism can be considered an authorization boundary. Existing approval gates remain unchanged.

## Known limitations
- Recovery works for the same browser profile; a new device/profile has a different session key.
- No authenticated owner identity/session scoping exists yet.
- `localStorage` can be cleared and is accessible to same-origin JavaScript; CSP/XSS hardening remains future work.
- Hibernate `ddl-auto:update` remains migration technical debt.
- No retention policy or aggregate continuity telemetry exists yet.

## Failures/fallbacks tested
Invalid session identifiers and unknown sessions return `available=false` with no fabricated correlation ID. Browser recovery failure leaves Continue/Correct disabled and ordinary `AUTO` conversation fully usable. Existing Vayu provider fallback, cancellation, stale-response suppression, and approval behavior are intended to remain unchanged and must be confirmed by full CI before promotion.

## Rollback procedure / known-good reference
Do not promote until CI is green. Current known-good runtime remains `74ef76ee8624b4d6df256311d13ce15455646556` (CI #111). If this candidate regresses, move the runtime branch back to that commit; the new nullable database column/index are additive.

## Risks / technical debt introduced or removed
Removed: refresh-only loss of explicit parent identity for the same browser session. Added/remaining: unauthenticated session metadata is not suitable as a cross-user security boundary; explicit migration tooling and CSP/session hardening remain needed.

## Dependencies
No new application dependencies.

## Screenshots / visual references
No new panel or conversation window was introduced. The visible effect is that existing Continue/Correct controls become available again after refresh when resumable metadata exists.

## Follow-up work
Add authenticated owner/device identity before cloud multi-user deployment; then allow explicit owner-approved cross-device continuity without exposing transcript data by default.

## Next evolution target
Heart cycle: define owner-scoped continuity/authentication contract suitable for the always-on cloud architecture, while keeping KUPPA identity/presence separate from Vayu reasoning.

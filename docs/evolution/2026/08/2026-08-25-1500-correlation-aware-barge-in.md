# KUPPA Evolution Record — Correlation-Aware Barge-In

- **Date/time:** 2026-08-25 15:00 Asia/Kolkata
- **Cycle:** Body / UI / Human Interaction
- **Commit purpose:** Connect the avatar-first interaction layer to the validated `VayuBrainGateway v2` cancellation contract so interruptions and topic changes supersede in-flight brain turns instead of allowing stale responses to surface.
- **Hypothesis:** If KUPPA assigns each Vayu turn a caller-known correlation ID and cancels/supersedes the active turn when the user interrupts, the interface will feel more conversational while preserving Vayu as the sole reasoning brain.

## Architectural context
The validated runtime baseline is `7e0df512eeb416a0bd0dfb3d4e8873a16195057c` (CI #102 green). That baseline introduced `VayuBrainGateway v2`, optional caller correlation IDs, `POST /api/chat/{correlationId}/cancel`, and stale-result suppression at the brain boundary. The previous UI exposed Vayu health but did not call the cancellation endpoint.

## Detailed changes
- Generate a correlation ID in the browser before each `/api/chat` request and send it in the v2 request body.
- Track a single active browser turn with sequence, cancellation, supersession, and settlement state.
- Interrupt an in-flight Vayu turn from the microphone or Escape key, not only during audio playback.
- Allow typed topic changes while Vayu is thinking; the previous turn is cancelled before the new turn starts.
- Ignore stale browser responses if their turn is no longer current.
- Treat `VAYU_CANCELLED` / `cancelled=true` as a normal interruption state rather than a connection failure.
- Emit `kuppa-turn-cancelled` with correlation ID, reason, and cancellation status.
- Add an `interrupted` Vayu presence state and accessible interaction guidance.
- Fix speech barge-in so stopping playback explicitly settles the speech promise instead of leaving the old turn hanging.
- Extend the UI contract test with correlation/cancellation/supersession assertions.

## Files/components affected
- `src/main/resources/static/index.html`
- `src/test/java/ai/kuppa/ui/AvatarBrainPresenceContractTest.java`
- `CHANGELOG.md`
- `docs/evolution/README.md`
- this evolution record

## Behavior before
Barge-in stopped KUPPA's audio but did not cancel an in-flight Vayu request. The composer refused new messages while `busy`, so topic changes had to wait. A paused Audio element could leave the speech promise unresolved. The browser did not provide its own correlation ID.

## Behavior after
KUPPA can interrupt Vayu while thinking or speaking, a new typed topic supersedes the old turn, stale responses are ignored, and speech cancellation resolves cleanly. The avatar shows interruption as a normal conversational state. Vayu still owns reasoning and cancellation semantics.

## KUPPA/Vayu responsibility impact
KUPPA HEART now owns only turn identity/presentation and user interruption intent. Vayu BRAIN continues to own cognition, provider routing, request lifecycle semantics, and cancellation acceptance. No reasoning, planning, tool selection, or execution logic moved into KUPPA.

## API/event/schema/config/migration changes
No backend API/schema change. The UI now consumes the existing v2 request field `correlationId` and cancellation endpoint. New browser event: `kuppa-turn-cancelled` with `{correlationId, reason, status}`. New visual brain mode: `interrupted`.

## Tests/build/lint/smoke checks run with results
- Pre-publish JavaScript syntax check with `node --check` on the changed turn-management logic: **PASS**.
- Static preflight assertions for correlation ID propagation, cancellation endpoint use, stale-turn guards, and cancellation flag handling: **PASS**.
- Fresh Maven checkout/test could not run in the execution container because DNS resolution for `github.com` failed before clone. This is an infrastructure blocker, not a reported compile/test failure.
- Authoritative GitHub Actions CI is required after branch publication; baseline is not promoted by this implementation commit until CI is green.

## Relevant before/after metrics
- Browser-generated Vayu correlation IDs: **0 -> 1 per brain turn**.
- Browser cancellation endpoint integrations: **0 -> 1**.
- Typed topic supersession while Vayu is busy: **blocked -> supported**.
- Stale browser response guards: **0 -> 2** (current-turn identity + cancellation/supersession flags).
- Speech interruption promise settlement: **implicit/unreliable -> explicit**.
- Vayu interruption visual modes: **0 -> 1**.
- Approval-gate behavior changed: **0**.

## Security/privacy/permission implications
No secrets, permissions, external destinations, tool privileges, shell execution, or autonomous actions were added. Cancellation can only target the caller-known correlation ID for an active KUPPA/Vayu turn. Consequential actions remain approval gated.

## Known limitations
- Provider-native compute may continue after cooperative cancellation because current provider calls remain synchronous.
- Cancellation lifecycle state is process-local; multi-instance deployments will need affinity or distributed request state.
- Browser correlation IDs are interaction identifiers, not authorization credentials.

## Failures/fallbacks tested
Static logic covers missing/failed cancellation calls without exposing stale responses, and `VAYU_CANCELLED` is rendered as interruption rather than brain outage. Existing avatar-offline, Vayu fallback/offline, text fallback, voice fallback, and approval UI remain intact by inspection.

## Rollback procedure / known-good reference
Rollback this UI evolution to validated runtime `7e0df512eeb416a0bd0dfb3d4e8873a16195057c`. If a deeper UI rollback is required, use `1efac9e2485a6181413b30a003a88654c3cd9792`.

## Risks / technical debt introduced or removed
Removed stale-browser-turn and unresolved-speech-promise debt. Remaining debt is provider-native cancellation and distributed lifecycle state. `busy` remains a derived UI flag alongside `activeTurn`; future cleanup could make turn state the single source of truth.

## Dependencies
No new dependencies.

## Screenshots / visual references
No screenshot artifact was generated. The visible change is the existing Vayu status pill/aura entering `interrupted` state during cancellation; no conversation window was added.

## Follow-up work
After CI validation, promote the implementation commit in `docs/evolution/BASELINE.md`. Next UI work should add resumable turn context so an interruption can optionally continue the previous thought without replaying stale output.

## Next evolution target
Heart cycle: define a resumable Vayu turn/context contract that can distinguish "new topic", "continue", and "correct previous" while keeping KUPPA's role limited to relationship/presentation context.

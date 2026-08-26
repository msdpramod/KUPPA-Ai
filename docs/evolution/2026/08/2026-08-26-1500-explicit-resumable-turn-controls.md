# KUPPA Evolution Record — Explicit Resumable Turn Controls

- **Date/time:** 2026-08-26 15:00 Asia/Kolkata
- **Cycle:** Body / UI / Human Interaction
- **Commit purpose:** Connect the avatar-first UI to `VayuBrainGateway v3` turn modes without moving semantic inference into KUPPA.
- **Hypothesis:** Lightweight, one-shot Continue / Correct / New topic controls will improve resumable conversation precision when the user explicitly knows the relationship, while leaving ambiguous language as `AUTO` preserves Vayu's responsibility for semantic reasoning.

## Architectural context
The validated baseline is `34882775c025ec793decf8846166e700f71a5beb` with CI #107 green. Vayu Brain Gateway v3 already accepts `AUTO`, `NEW_TOPIC`, `CONTINUE`, and `CORRECTION` plus optional `parentCorrelationId`, but the browser currently sends only backward-compatible `AUTO`. The prior evolution record explicitly identified reliable UI wiring as the next target. KUPPA remains the HEART and Vayu remains the BRAIN.

## Detailed changes
- Added three subtle, optional continuity controls below the avatar: Continue, Correct, and New topic.
- Kept `AUTO` as the default and as the mode for ordinary typed or spoken conversation.
- Made continuity selection one-shot: after a message starts, the UI resets to `AUTO`.
- Continue and Correct remain disabled until a completed turn exists.
- Continue and Correct link to the last successfully completed turn through `parentCorrelationId`.
- New topic deliberately sends no parent linkage.
- Added `kuppa-turn-context-change` and `kuppa-turn-completed` browser events for observability.
- Updated pending/interrupt brain metadata to identify Vayu gateway contract v3.
- Preserved correlation-aware cancellation, stale response suppression, avatar states, voice barge-in, degraded Vayu presence, and approval UI.

## Files/components affected
- `src/main/resources/static/index.html`
- `src/test/java/ai/kuppa/ui/AvatarBrainPresenceContractTest.java`
- `docs/evolution/2026/08/2026-08-26-1500-explicit-resumable-turn-controls.md`
- `docs/evolution/README.md`
- `CHANGELOG.md`

## Behavior before
The browser generated correlation IDs and cancellation signals but always omitted v3 turn context. Vayu therefore received `AUTO` for every avatar/browser turn even when the user explicitly knew they were continuing, correcting, or starting over.

## Behavior after
Natural conversation still defaults to `AUTO`. The user can explicitly mark the next turn as Continue, Correct, or New topic. Continue/Correct are tied only to the last completed correlation ID and are unavailable before one exists. Selection is consumed exactly once and then resets to `AUTO`.

## KUPPA/Vayu responsibility impact
KUPPA gains no reasoning authority. It transports explicit interaction intent chosen by the user. It does not inspect message text to decide whether a turn is a continuation, correction, or topic change. Vayu remains responsible for interpreting `AUTO`, resolving references, applying continuation/correction semantics, planning, reasoning, routing, orchestration, retrieval, and execution strategy.

## API/event/schema/config/migration changes
No server API or database migration. Existing `/api/chat` optional fields are now populated by the browser when explicitly selected:
- `turnMode`
- `parentCorrelationId`

New browser events:
- `kuppa-turn-context-change`
- `kuppa-turn-completed`

## Tests/build/lint/smoke checks run with results
Pre-change authoritative baseline: CI #107 **PASS** on `34882775c025ec793decf8846166e700f71a5beb`.

Before publish, the UI contract test was expanded to statically verify:
- all four v3 modes remain represented;
- explicit controls exist for Continue / Correct / New topic;
- `AUTO` remains the reset/default;
- parent correlation linkage is present;
- v3 turn metadata is sent to `/api/chat`;
- correlation-aware cancellation and approval contract assertions remain intact.

Authoritative post-publish GitHub Actions result is pending at commit creation time and must be recorded in a same-governance validation closeout before promoting the runtime baseline.

## Relevant before/after metrics
- Browser-exposed explicit v3 modes: **0 -> 3** (AUTO remains implicit/default).
- Browser `/api/chat` turn-context fields sent: **0 -> 2**.
- Explicit parent-turn linkage from UI: **0 -> 1 last-completed-turn reference**.
- One-shot continuity reset: **absent -> present**.
- Natural-language semantic classifiers added to KUPPA: **0**.
- Conversation windows added: **0**.
- Approval behavior changed: **0**.
- Personality/memory behavior changed: **0**.

## Security/privacy/permission implications
No new permissions, tools, network destinations, credentials, secrets, shell execution, self-modification, or autonomous consequential actions. `parentCorrelationId` is context metadata, not authorization. Existing external/high-impact action approval gates remain unchanged.

## Known limitations
- Parent linkage is still advisory because persisted conversation messages are not correlation-keyed.
- Only the last successfully completed browser turn is available as an explicit parent.
- Cross-device/browser continuity is not persisted.
- Provider-native cancellation is still unavailable.
- No production metric yet measures whether users select the correct explicit mode.

## Failures/fallbacks tested
The existing UI contract continues to assert Vayu unavailable/fallback states, VAYU cancellation handling, stale-turn suppression, and pending approval rendering. Continue/Correct are disabled without a known completed parent, preventing invalid explicit linkage from the UI.

## Rollback procedure / known-good reference
Until post-publish CI is green, the known-good runtime remains `34882775c025ec793decf8846166e700f71a5beb` (CI #107). If this UI change regresses interaction behavior, reset `agent/avatar-ui` to that commit.

## Risks / technical debt introduced or removed
Removed: the gap between v3 backend turn context and explicit browser interaction intent. Remaining debt: browser-local parent identity, no correlation-keyed persistence, no cross-session resumability telemetry, and no provider-native cancellation.

## Dependencies
No new dependencies.

## Screenshots / visual references
No screenshot artifact is committed in this run. The controls are intentionally low-visual-weight and remain secondary to the avatar.

## Follow-up work
After CI validation, promote the implementation commit to `docs/evolution/BASELINE.md`. A future Heart cycle should make correlation-keyed conversation persistence available to Vayu so parent linkage can survive browser sessions without moving reference resolution into KUPPA.

## Next evolution target
Heart cycle: correlation-keyed conversation persistence and server-side resumable-turn lookup for Vayu, with graceful behavior when a referenced parent is missing or expired.

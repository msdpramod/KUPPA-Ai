# 2026-09-04 15:10 — Presence / Latency Validation Closeout

## Cycle
Body / UI / Human Interaction validation closeout.

## Why this documentation-only commit exists
The implementation commit `6be4e77272a3e43ce0f64ba6f8c7f7b2d634dfdd` was intentionally left unpromoted until repository CI validated it. GitHub Actions CI #171 completed successfully. This closeout records that evidence, promotes the known-good runtime baseline, and updates the chronological evolution index. No production code is changed by this commit.

## Commit purpose and hypothesis
Record validated evidence after CI rather than claiming success in advance. Hypothesis confirmed: a presentation-only KUPPA presence controller can improve latency perception and accessibility without adding Vayu cognition, network calls, backend contracts, persistence, or approval bypasses.

## Architectural context
KUPPA remains the HEART. `KuppaPresenceController v1` consumes already-emitted browser state/brain-presence events solely for presentation. Vayu remains the BRAIN and retains reasoning, planning, retrieval, provider/tool/agent orchestration, diagnostics and execution strategy.

## Validated implementation
- Runtime implementation: `6be4e77272a3e43ce0f64ba6f8c7f7b2d634dfdd`.
- GitHub Actions: CI #171, completed successfully.
- Pre-change governed head: `e284a05dd5934eb1996bf8a63fc93354a654a9b3`, green on CI #170.

## Detailed validated behavior
- Existing KUPPA interaction states drive presentation modes only.
- Processing states expose `aria-busy=true` on the avatar stage.
- Existing Vayu `pending` presence starts an elapsed local clock; non-pending state immediately clears it.
- A wait longer than four seconds is presented as `still working` without inventing internal Vayu progress or a result.
- Listening/thinking/speaking mic-ring cues are CSS-only.
- Reduced-motion preference removes the new pulse/transition motion while preserving static state visibility.
- Contract test explicitly rejects `fetch(`, `/api/chat`, and `VayuBrainGateway` dependencies from the presence controller.

## Files/components affected by implementation
- `src/main/resources/static/kuppa-presence.js`
- `src/main/resources/static/kuppa-presence.css`
- `src/main/java/ai/kuppa/avatar/AvatarPageController.java`
- `src/test/java/ai/kuppa/ui/AvatarBrainPresenceContractTest.java`
- `docs/evolution/2026/09/2026-09-04-1500-presence-latency-perception.md`
- `CHANGELOG.md`
- `docs/evolution/README.md`

This closeout changes only governance documentation: this file, `docs/evolution/BASELINE.md`, and `docs/evolution/README.md`.

## Behavior before / after
Before: generic Vayu thinking label, no explicit stage busy semantics, no elapsed pending feedback, and no reduced-motion policy for the new pulse behavior.
After: elapsed pending feedback, accessible busy semantics, state-reactive presence cues and reduced-motion handling, with Vayu status remaining authoritative.

## KUPPA/Vayu responsibility impact
KUPPA presentation improved. Vayu responsibility did not change. No reasoning, planning, retrieval, tool selection, specialist-agent coordination, provider routing, execution strategy, cancellation contract, or diagnostics logic moved to KUPPA.

## API/event/schema/config/migration changes
Browser-only `KuppaPresenceController v1` and `kuppa-presence-controller-ready` event were added by the implementation. No backend API, database schema, migration, secret, configuration, runtime dependency or server event contract changed.

## Tests/build/lint/smoke checks and results
- Pre-change governed merge: CI #170 — PASS.
- Implementation commit `6be4e772...`: CI #171 — PASS.
- Repository Maven workflow including UI contract tests — PASS through CI #171.
- Success path covered: pending state starts elapsed feedback and presence/busy mapping exists.
- Failure/fallback path covered: controller has no network/brain dependency; non-pending brain states stop its timer and preserve authoritative fallback/offline/cancelled/completed labels.
- Browser automation screenshots were not available in this run; the UI behavior is contract-tested at resource level.

## Before/after metrics
- Elapsed Vayu wait presentation paths: 0 -> 1.
- Accessible avatar processing busy-state path: 0 -> 1.
- Reduced-motion policy for new state pulse: 0 -> 1.
- Presence controller network calls: 0 -> 0.
- Backend API/schema/dependency changes: 0.
- Vayu cognition changes: 0.
- Personality/memory changes: 0.
- Consequential approval-gate changes: 0.
- Build stability: green CI #170 -> green CI #171.

## Security/privacy/permission implications
No prompts, transcripts, personal memories, credentials or owner secrets are stored or transmitted by the new controller. No permission or consequential-action behavior changed.

## Known limitations
Elapsed time measures browser-observed pending duration, not internal Vayu sub-step progress. It must never be interpreted as chain-of-thought or precise execution progress. Reduced-motion currently applies to the new CSS presence layer, not every existing Three.js idle movement.

## Failures/fallbacks tested
If required DOM elements are absent the module exits safely. Any non-pending Vayu state clears the local elapsed timer. Existing fallback/offline/cancelled labels remain the source of truth. There is no alternative local reasoning path.

## Rollback procedure / known-good reference
Rollback to governed head `e284a05dd5934eb1996bf8a63fc93354a654a9b3`, restoring validated runtime `bae44bab17dc9402fc4abcf195165a51398d82e4`. No schema rollback is required.

## Risks / technical debt introduced or removed
One short-lived 250 ms browser interval exists only while Vayu is pending and is cleared on non-pending status. Frontend events are still DOM events rather than a typed module event bus. The continuity adapter still wraps legacy inline continuity functions.

## Dependencies
None added.

## Screenshots / visual references
No screenshot artifact was produced. Validation relied on repository CI and explicit UI resource contract coverage.

## Follow-up work
- Move continuity implementation behind `KuppaContinuityAdapter` rather than wrapping global inline functions.
- Extend reduced-motion semantics to bounded Three.js avatar movement.
- Add browser-level smoke coverage when a supported runner is available.

## Next evolution target
Heart: typed owner-authenticated memory-change history and safe near-match disambiguation requiring explicit owner selection before deletion.

# 2026-09-04 15:00 — Presence and Latency Perception

## Cycle
Body / UI / Human Interaction.

## Commit purpose and hypothesis
Make KUPPA feel more present during listening, Vayu handoff, thinking and speaking without moving any cognition into KUPPA. Hypothesis: visible elapsed thinking time, a bounded state-reactive mic pulse and accessible busy semantics reduce perceived latency and ambiguity while preserving the existing state engine and Vayu Brain Gateway boundary.

## Architectural context
KUPPA remains the HEART: avatar, voice, relationship-facing interaction, trust, expression and presentation. Vayu remains the BRAIN: reasoning, planning, orchestration, knowledge retrieval, tool/agent selection and execution strategy. The new controller consumes existing `kuppa-state-change` and `kuppa-brain-state-change` presentation events only. It does not call chat APIs, inspect prompts, infer intent, route providers, or make decisions.

## Detailed changes
- Added `kuppa-presence.js`, a frozen `KuppaPresenceController v1` presentation contract.
- Maps the existing interaction state engine to calm/engaged/attentive/processing/expressive presence modes.
- Sets `aria-busy=true` only while KUPPA is understanding, asking Vayu, thinking or preparing a response.
- Starts a local elapsed-time clock when the existing Vayu presence event reports `pending`; the visible label becomes `Vayu · thinking · <seconds>` and after four seconds adds the factual phrase `still working`.
- Stops the clock immediately when Vayu emits any non-pending state so completed/fallback/offline/cancelled labels remain authoritative.
- Added state-reactive microphone ring animation for listening, thinking and speaking.
- Added `prefers-reduced-motion: reduce` handling that removes the new animation/transitions while retaining clear static state cues.
- Added a slow-latency visual treatment after four seconds without fabricating a result or provider status.
- `AvatarPageController` now injects the presence CSS/JS alongside the existing continuity and trusted-device modules.
- Added UI contract coverage proving the presence module has no `fetch`, `/api/chat`, or `VayuBrainGateway` dependency.

## Files/components affected
- `src/main/resources/static/kuppa-presence.js`
- `src/main/resources/static/kuppa-presence.css`
- `src/main/java/ai/kuppa/avatar/AvatarPageController.java`
- `src/test/java/ai/kuppa/ui/AvatarBrainPresenceContractTest.java`
- `CHANGELOG.md`
- `docs/evolution/README.md`
- this record

## Behavior before
The state engine changed aura/mic styling and Vayu showed a generic `thinking…` label. There was no elapsed wait indicator, no explicit accessible busy state on the avatar stage, and no reduced-motion policy for newly introduced state animation.

## Behavior after
KUPPA communicates interaction state more clearly without changing conversation semantics. Long Vayu waits remain visibly active rather than looking frozen; screen readers can observe busy state; motion-sensitive users get static state cues; Vayu completion/fallback/offline/cancelled events still replace the pending presentation immediately.

## KUPPA/Vayu responsibility impact
KUPPA: presentation-only presence, accessibility and latency perception improved.
Vayu: no responsibility change. Brain Gateway v3, reasoning, provider routing, planning, retrieval, tools, specialist agents, diagnostics and execution strategy are untouched.

## API/event/schema/config/migration changes
- New browser-only `KuppaPresenceController v1` object.
- Consumes existing `kuppa-state-change` and `kuppa-brain-state-change` events.
- Emits `kuppa-presence-controller-ready` with version metadata.
- No backend API, event schema consumed by server code, database schema, migration, secret, configuration or runtime dependency added.

## Tests/build/lint/smoke checks
Preflight: governed branch head `e284a05dd5934eb1996bf8a63fc93354a654a9b3` is green on GitHub Actions CI #170 before this change. Added contract assertions for event consumption, accessible busy state, elapsed timing, reduced-motion support and absence of brain/API logic. Full Maven CI is required before promotion; this implementation branch must not be merged if that validation fails.

## Before/after metrics
- Elapsed Vayu wait feedback: 0 -> 1 presentation path.
- Accessible avatar-stage busy semantics: 0 -> 1.
- Reduced-motion policy for new pulse animation: 0 -> 1.
- New browser network calls: 0.
- New backend APIs/schema/runtime dependencies: 0.
- Vayu cognition changes: 0.
- Approval-gate changes: 0.

## Security/privacy/permission implications
The module reads only interaction-state event metadata already present in the page. It stores no personal data, credentials, prompts, transcripts or Vayu results. It introduces no external action and no permission bypass.

## Known limitations
This is presentation-level latency feedback, not streaming reasoning progress. It cannot know internal Vayu sub-steps and deliberately does not pretend to. The avatar's Three.js motion loop is unchanged; reduced-motion currently governs only the new CSS pulse/transition behavior.

## Failures/fallbacks tested
Contract coverage requires the module to contain no network call or Vayu brain implementation dependency. The controller stops elapsed timing on every non-pending brain state, so fallback/offline/cancelled/completed labels remain authoritative. If the stage or brain-status elements are missing, the module exits without affecting conversation.

## Rollback procedure / known-good reference
Return to governed head `e284a05dd5934eb1996bf8a63fc93354a654a9b3`, whose runtime baseline is `bae44bab17dc9402fc4abcf195165a51398d82e4` validated by CI #168 and whose merged head is green on CI #170. No database rollback is required.

## Risks / technical debt introduced or removed
Introduces one small interval timer while Vayu is pending; it is cleared on every terminal/non-pending brain state. The module still observes DOM/browser events rather than a typed frontend event bus. Existing continuity implementation leakage behind `KuppaContinuityAdapter v1` remains unresolved.

## Dependencies
No new dependency.

## Screenshots / visual references
Not attached in this run. The change is intentionally small and state-driven: mic pulse, latency label and slow-wait status treatment.

## Follow-up work
- Move the continuity implementation itself behind the continuity adapter instead of wrapping legacy inline functions.
- Extend reduced-motion handling to the Three.js avatar motion loop with a bounded presentation flag.
- Add browser-level smoke coverage when a browser runner is available.

## Next evolution target
Heart cycle: owner-authenticated typed memory-change history plus safe near-match memory disambiguation with explicit owner selection before deletion.

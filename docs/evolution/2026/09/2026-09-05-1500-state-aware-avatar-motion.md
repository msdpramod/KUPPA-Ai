# 2026-09-05 15:00 — State-aware avatar motion

## Cycle
Body / UI / Human Interaction.

## Commit purpose and hypothesis
Make KUPPA's embodied presence more intentional while closing the known accessibility gap in the Three.js motion loop. Hypothesis: state-scaled autonomous movement improves perceived attentiveness in the normal experience, while a real reduced-motion policy that removes continuous head/bob movement makes the avatar safer and calmer for motion-sensitive users without removing facial/lip-sync state cues.

## Architectural context
KUPPA remains the HEART: avatar, voice, expression, presence, relationship-facing interaction and trust. Vayu remains the BRAIN: reasoning, planning, orchestration, retrieval, tool/skill and specialist-agent selection, execution strategy and self-healing intelligence. This change is presentation-only. It does not inspect prompts, call chat APIs, infer intent, route providers, coordinate agents or alter approval gates.

## Detailed changes
- Added frozen `KuppaAvatarMotionPolicy v1` as a browser presentation contract.
- The policy observes `prefers-reduced-motion: reduce`, including live preference changes.
- Added state-aware autonomous motion energy: noticed/listening/thinking/responding/speaking/waiting states now use bounded motion scales instead of one uniform idle oscillation.
- In reduced-motion mode, autonomous Three.js yaw and vertical bob scale to zero.
- Pointer-driven gaze remains available but is damped to 25% in reduced-motion mode so direct interaction is still legible without large movement.
- Facial morphs, blink/lip-sync hooks, brain fallback/offline expression and static state cues remain intact.
- `AvatarPageController` injects the policy before the inline avatar runtime and patches three explicit animation-loop integration points.
- The server fails fast if those expected animation markers drift, preventing a silent accessibility regression after future index changes.
- Added contract coverage for policy loading, reduced-motion behavior integration, state-aware scaling and absence of network/brain dependencies.

## Files/components affected
- `src/main/resources/static/kuppa-avatar-motion.js`
- `src/main/java/ai/kuppa/avatar/AvatarPageController.java`
- `src/test/java/ai/kuppa/ui/AvatarBrainPresenceContractTest.java`
- `CHANGELOG.md`
- `docs/evolution/README.md`
- this record

## Behavior before
The CSS presence layer honored reduced motion, but the Three.js loop still continuously changed avatar yaw and vertical position. The same autonomous oscillation amplitude ran across interaction states. Reduced-motion preference therefore did not fully govern embodied motion.

## Behavior after
Normal mode keeps KUPPA alive but modulates autonomous movement by interaction state. Reduced-motion mode removes continuous autonomous head/bob motion and strongly damps gaze, while preserving facial expression, lip-sync, state labels and voice interaction. A missing/changed runtime patch point is treated as a server-side error instead of silently falling back to uncontrolled motion.

## KUPPA/Vayu responsibility impact
KUPPA: embodied presentation and accessibility policy improved.
Vayu: no responsibility or implementation change. Vayu Gateway v3, reasoning, planning, provider routing, retrieval, tools, organs/specialist agents, diagnostics and execution remain untouched.

## API/event/schema/config/migration changes
- New browser-only `KuppaAvatarMotionPolicy v1` object.
- Emits `kuppa-motion-preference-change` with version and reduced-motion boolean.
- Adds `data-avatar-motion=full|reduced` to the document root for observable presentation state.
- No backend REST API, database schema, migration, secret, permission, configuration or runtime dependency added.

## Tests/build/lint/smoke checks run with results
Preflight inspected governed head `9ff0ee08587dc20dcc1098127f3ac9512e364e77`, the Constitution, evolution index, latest validation record and known-good baseline `ac082dce...` (CI #180 green). Contract tests were added before publication. Full Maven/GitHub Actions validation is required before merge; this implementation must not be promoted on a failing CI run.

## Relevant before/after metrics
- Three.js autonomous motion obeying reduced-motion preference: 0 -> 1 policy path.
- State-aware autonomous motion scales: 0 -> 9 bounded states.
- Continuous autonomous yaw/bob under reduced motion: present -> removed.
- Reduced-motion pointer gaze scale: 100% -> 25%.
- New browser network calls: 0.
- New backend APIs/schema/runtime dependencies: 0.
- Vayu cognition changes: 0.
- Approval-gate changes: 0.

## Security/privacy/permission implications
The policy reads only a browser accessibility preference and current KUPPA presentation state. It stores no prompt, transcript, personal memory, credential or Vayu result and performs no external action. Consequential actions remain approval gated.

## Known limitations
The inline avatar runtime is still patched by `AvatarPageController`; this keeps the change bounded but is technical debt. The better long-term shape is to move the Three.js avatar runtime into a dedicated versioned frontend module so motion policy is consumed directly rather than via guarded string integration points. Reduced-motion mode still allows facial blink/lip-sync because those are functional communication cues rather than continuous camera/body movement.

## Failures/fallbacks tested
- Contract coverage requires the motion policy to contain no `fetch`, `/api/chat`, or `VayuBrainGateway` dependency.
- Served-page coverage verifies the motion policy is injected and the Three.js yaw/bob/gaze integration is present.
- `AvatarPageController` throws if any expected animation integration marker no longer matches, preventing silent loss of the reduced-motion guarantee.
- If `matchMedia` is unavailable, the policy safely defaults to normal bounded motion.

## Rollback procedure / known-good reference
Return to governed head `9ff0ee08587dc20dcc1098127f3ac9512e364e77`, whose validated runtime baseline is `ac082dce5d68c6908f5c843fded23df11204ce83` from CI #180. No database rollback is required.

## Risks / technical debt introduced or removed
Removed a known accessibility gap where CSS respected reduced motion but the Three.js body loop did not. Introduced a guarded server-side integration seam around the legacy inline avatar loop; this should be removed when the avatar runtime is modularized. No recurring timer or extra render loop was added.

## Dependencies
No new dependency.

## Screenshots or visual references
No screenshot attached in this run. The visual difference is state-dependent movement amplitude and reduced-motion suppression rather than a layout redesign.

## Follow-up work
- Move the inline Three.js avatar runtime into a dedicated `KuppaAvatarRuntime` module and consume `KuppaAvatarMotionPolicy` directly.
- Add browser-level smoke coverage for live `prefers-reduced-motion` changes when a browser runner is available.
- Continue expression/gaze refinement without inventing Vayu internal progress.

## Next evolution target
Heart: safe near-match memory candidate disambiguation with explicit owner selection before deletion. UI: modularize the avatar runtime and then add finer gaze/expression transitions and voice-state micro-interactions on the clean module boundary.

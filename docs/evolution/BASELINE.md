# KUPPA Known-Good Baseline

## Runtime baseline
- **Current validated implementation:** `426132cbd7a9e01e6fbbf55219f2d7e5a60b8ae3` (state-aware avatar motion and Three.js reduced-motion policy), validated by GitHub Actions CI #182.
- **Previous validated implementation:** `ac082dce5d68c6908f5c843fded23df11204ce83` (typed owner memory-change history), validated by CI #180.
- **Pre-change governed branch head:** `9ff0ee08587dc20dcc1098127f3ac9512e364e77`.

## Current evidence
- `KuppaAvatarMotionPolicy v1` is injected before the inline Three.js avatar runtime.
- Normal avatar autonomous movement is bounded by the current KUPPA interaction state rather than using one uniform idle amplitude.
- `prefers-reduced-motion: reduce` removes continuous autonomous yaw/head offset and vertical bob by returning autonomous scale 0.
- Pointer-driven gaze remains available at 25% scale in reduced-motion mode; facial expression, blink and lip-sync cues remain active.
- Live motion-preference changes are observed and exposed through `data-avatar-motion` plus `kuppa-motion-preference-change`.
- Served-page contract coverage verifies policy injection plus Three.js yaw, bob and gaze integration.
- Motion policy contains no chat/network/Vayu Brain Gateway dependency.
- `AvatarPageController` fails fast if the three guarded animation patch points drift, preventing a silent accessibility regression.
- Existing owner memory history, exact-match memory forgetting, Vayu Gateway v3, signed continuity, Trusted Devices, presence/latency controller, voice barge-in and consequential-action approval behavior remain intact.
- No database schema, runtime dependency, secret/configuration, Vayu cognition, memory semantics or approval-gate change was introduced.
- CI #182 passed the repository Maven test workflow.

## Scorecard baseline
| Dimension | Evidence status |
|---|---|
| Build stability | Green on implementation CI #182; previous runtime green on CI #180 |
| Conversation quality | Unchanged |
| Personality consistency | Presentation is more state-coherent; persona/memory rules unchanged |
| Memory accuracy | Unchanged; typed memory history and exact-match forgetting retained |
| Vayu handoff reliability/latency | Unchanged; existing presence latency feedback retained |
| Errors | Guarded avatar integration fails fast on source drift instead of silently dropping reduced-motion behavior |
| Voice reliability | Unchanged; lip-sync/voice path retained |
| UI responsiveness | No extra render loop or network call; existing Three.js loop reuses bounded scalar calculations |
| Accessibility | Improved: Three.js autonomous body motion now honors reduced-motion preference |
| Resource usage | Negligible scalar/state checks inside existing frame loop; no new timers or network polling |
| Security boundary | Unchanged; presentation policy reads only browser accessibility preference/current state |

## Rollback policy
Return to governed head `9ff0ee08587dc20dcc1098127f3ac9512e364e77` to remove this UI evolution, restoring validated runtime `ac082dce5d68c6908f5c843fded23df11204ce83`. No destructive schema rollback is required. Normal evolution must preserve the Constitution, HEART/BRAIN boundary and approval gates.

## Next identified gaps
- Move the inline Three.js avatar runtime into a dedicated versioned `KuppaAvatarRuntime` module so motion policy is consumed directly rather than through guarded server-side string integration points.
- Add browser-level smoke coverage for live reduced-motion preference changes when a browser runner is available.
- Add safe conversational near-match memory disambiguation that presents candidates and requires explicit owner selection before deletion.
- Move owner authentication away from static shared secrets toward passkeys/WebAuthn or OIDC-grade identity.
- Move the continuity implementation itself behind `KuppaContinuityAdapter` rather than bridging legacy inline functions.
- Add explicit owner identity to trust/audit persistence before any multi-owner architecture.
- Move durable device possession credentials away from general browser localStorage when a stronger credential primitive is introduced.

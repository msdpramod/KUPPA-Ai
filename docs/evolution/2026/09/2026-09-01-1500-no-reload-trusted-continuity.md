# 2026-09-01 15:00 — No-reload trusted continuity handoff

## Cycle
Body / UI / Human Interaction.

## Commit purpose and hypothesis
Remove the forced full-page reload after owner-device pairing. Hypothesis: pairing can activate the existing signed continuity session and restore resumable metadata in-place, improving perceived latency and preserving avatar/conversation presence without weakening identity boundaries.

## Architectural context
KUPPA remains the HEART: identity, presence, continuity presentation, trust state and interaction. Vayu remains the BRAIN: reasoning, planning, retrieval, tool/agent orchestration and execution strategy. This change only reuses KUPPA's already-existing owner continuity functions; no semantic reasoning moves into the UI.

## Detailed changes
- Pairing stores the issued device possession credential exactly as before.
- After successful enrollment, the pairing module calls the existing `issueOwnerContinuity()` and `restoreContinuity()` functions instead of reloading the page.
- Pairing reports `Trusted continuity is active.` only after signed continuity activation succeeds.
- Emits non-secret `kuppa-device-pairing-complete` metadata after successful activation.
- Keeps pairing open with a bounded error if secure activation fails; it does not fabricate successful trusted continuity.
- Hides the pair button once activation succeeds.

## Files/components affected
- `src/main/resources/static/trusted-devices.js`
- `src/test/java/ai/kuppa/ui/TrustedDevicesUiContractTest.java`
- this evolution record

## Behavior before
Successful pairing wrote the device credential, emitted `kuppa-device-paired`, then called `location.reload()` after 180 ms. The reload was required for bootstrap to issue signed continuity and restore resumable metadata.

## Behavior after
Successful pairing issues signed owner continuity and restores resumable metadata in the same page. Avatar, current UI state and page presence are retained. A full reload remains only in the older local-forget fallback path.

## KUPPA/Vayu responsibility impact
KUPPA gains no reasoning responsibility. Vayu contracts and cognition are unchanged. The UI only activates an already-authorized continuity credential and restores metadata.

## API/event/schema/config/migration changes
No backend API, schema, configuration or database migration changes. Adds browser event `kuppa-device-pairing-complete` with non-secret `deviceId` and `secure=true` metadata.

## Tests/build/lint/smoke checks
Local static contract coverage added to assert that pairing calls the existing signed-continuity issuance/restoration functions and that the pairing flow contains no `location.reload()`. Full Maven CI must pass before promotion; this record will be updated only by a separate documented validation closeout after CI evidence exists.

## Relevant before/after metrics
- Pairing-triggered full-page reloads: 1 -> 0.
- Signed-continuity activation paths after pairing: reload/bootstrap -> in-place activation.
- New backend requests beyond existing secure continuity flow: 0.
- Vayu cognition changes: 0.
- Approval-gate changes: 0.

## Security/privacy/permission implications
Owner enrollment and device bearer-token behavior are unchanged. Enrollment secret remains ephemeral. Signed continuity is considered active only after the existing owner-device continuity endpoint succeeds. No credentials are added to DOM events.

## Known limitations
The device possession token remains in browser `localStorage`; this remains an interim trust model, not passkey/WebAuthn-grade security. The in-page integration currently depends on classic-script global function bindings from the avatar page and should later become an explicit module interface.

## Failures/fallbacks tested
The contract requires a bounded failure when continuity activation functions are unavailable or signed continuity issuance fails. It deliberately does not claim trusted continuity in those cases.

## Rollback procedure / known-good reference
Rollback to governed branch head `ff09c54691f760f7cacc50dbade84a90e44198bf`, whose validated runtime is `2e3f4c2575bba55af3fedec87db6b78253c309f9` (CI #149).

## Risks / technical debt
Using classic-script globals is a narrow coupling. It removes reload latency but should be replaced by an explicit KUPPA continuity adapter when the inline avatar script is modularized.

## Dependencies
No new runtime or build dependency.

## Screenshots / visual references
Not required: the visible pairing sheet is unchanged; this evolution changes transition behavior after success.

## Follow-up work
Expose a small explicit continuity adapter instead of relying on global function bindings; proceed toward passkeys/WebAuthn before removing local fallback.

## Next evolution target
Typed owner-authenticated trust-history plus stronger owner authentication on the Heart side; explicit modular continuity adapter and pairing-state micro-interactions on the UI side.

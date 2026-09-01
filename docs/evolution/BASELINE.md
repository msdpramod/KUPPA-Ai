# KUPPA Known-Good Baseline

## Runtime baseline
- **Current validated implementation:** `b781e4bd00233dbf7d16a5d34ea686649c330451` (in-place signed continuity activation after owner-device pairing, no full-page reload), validated by GitHub Actions CI #152.
- **Previous validated implementation:** `2e3f4c2575bba55af3fedec87db6b78253c309f9` (in-app owner-device pairing and management unlock forms), validated by CI #149.
- **Pre-change governed branch head:** `ff09c54691f760f7cacc50dbade84a90e44198bf`.

## Current evidence
- Successful pairing activates signed owner continuity and restores resumable metadata in-place.
- Pairing no longer reloads the page, so avatar and interaction presence remain intact.
- The UI reports trusted continuity only after the existing secure issuance path succeeds.
- The pairing flow emits only non-secret completion metadata.
- Existing metadata-only Trusted Devices inventory, local Forget, global Revoke, secure/local continuity fallback, persistent revocation, signing-key rotation and approval gates remain intact.
- VayuBrainGateway v3, cancellation, parent restoration, confidence-aware memory, voice barge-in, state engine and degraded brain presence remain unchanged.
- CI #152 passed checkout, Java setup, full Maven `Test`, cleanup and completion for implementation `b781e4bd...`.

## Scorecard baseline
| Dimension | Evidence status |
|---|---|
| Build stability | Green on CI #152 |
| Conversation quality | Pairing no longer resets the live avatar/conversation page |
| Personality consistency | Unchanged |
| Memory accuracy | Existing confidence-aware memory behavior/tests unchanged |
| Vayu handoff reliability/latency | Vayu Gateway v3 unchanged; continuity activation removes reload delay only |
| Errors | Pairing activation failure is bounded and not presented as trusted success |
| Voice reliability | Existing playback cancellation/barge-in unchanged |
| UI responsiveness | Post-pairing full-page reload removed; trusted state activates in-place |
| Accessibility | Existing labelled pairing dialog/status messaging unchanged |
| Resource usage | Static JS/test change only; no new runtime dependency or DB work |
| Security boundary | No new credential exposure; device bearer token still in localStorage as a known limitation |

## Rollback policy
Return to governed head `ff09c54691f760f7cacc50dbade84a90e44198bf` to remove this UI evolution, restoring validated runtime `2e3f4c2575bba55af3fedec87db6b78253c309f9` (CI #149). No destructive schema rollback is required. Normal evolution must preserve the Constitution, HEART/BRAIN boundary and approval gates.

## Next identified gaps
- Add an owner-authenticated typed/filtered trust-history endpoint.
- Replace static owner enrollment/management shared-secret authentication with passkeys/WebAuthn/OIDC-grade authentication.
- Replace classic-script global continuity bindings with an explicit KUPPA continuity adapter/module.
- Move durable device possession credentials away from general browser localStorage when a stronger credential primitive is introduced.
- Consider tamper-evident integrity verification for high-value trust-management audit events.
- Retire unsigned local continuity only after secure owner identity is universally configured and migration-safe.

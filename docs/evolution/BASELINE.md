# KUPPA Known-Good Baseline

## Runtime baseline
- **Current validated implementation:** `2e3f4c2575bba55af3fedec87db6b78253c309f9` (in-app owner-device pairing and management unlock forms replacing native credential prompts), validated by GitHub Actions CI #149.
- **Previous validated implementation:** `0f57af0525ea869a0fc853e51045f25ea2ab85a1` (avatar-first Trusted Devices sheet with metadata-only inventory, local Forget, global Revoke and repaired UI contract), validated by CI #146.
- **Pre-change governed branch head:** `11cabca8dbc9ff94dda5e5a37386fce82710f724`.

## Current evidence
- `Pair this device` uses a KUPPA-owned accessible dialog rather than the old owner-enrollment browser prompt.
- The enrollment key is sent through the existing `X-KUPPA-Owner-Enroll-Key` contract, cleared from the form after use and never stored by the pairing module.
- Trusted Devices management uses an in-sheet password input; the management key stays page-memory-only while the sheet is open and its input is cleared.
- Device inventory remains metadata-only and still distinguishes `Forget on this browser` from `Revoke everywhere`.
- The issued device possession credential remains in localStorage as an acknowledged interim limitation; no stronger security claim is made.
- Secure continuity bootstrap, local fallback, persistent revocation, signed continuity, audit ledger, VayuBrainGateway v3, cancellation, parent restoration, confidence-aware memory, state engine, voice barge-in, degraded brain presence, signing-key rotation and consequential-action approvals remain intact.
- CI #149 passed checkout, Java setup, the full Maven `Test` step and cleanup for implementation `2e3f4c...`.

## Scorecard baseline
| Dimension | Evidence status |
|---|---|
| Build stability | Green on CI #149 |
| Conversation quality | Avatar/conversation flow unchanged; trust UX remains secondary |
| Personality consistency | Unchanged |
| Memory accuracy | Existing confidence-aware memory behavior/tests unchanged |
| Vayu handoff reliability/latency | Vayu Gateway v3, cancellation and persisted parent lookup unchanged |
| Errors | Pairing/management failures are bounded; secure/local continuity fallback preserved |
| Voice reliability | Existing playback cancellation/barge-in unchanged |
| UI responsiveness | Native credential prompts replaced by two labelled in-app forms; no conversation window |
| Accessibility | Pairing/management dialogs use labelled headings, form labels, password inputs and live pairing status |
| Resource usage | Static CSS/JS + existing controller only; no new runtime dependency or database work |
| Security boundary | Owner secrets remain ephemeral; issued device bearer token still in localStorage and static shared-secret owner auth remains a known limitation |

## Rollback policy
Return to governed head `11cabca8dbc9ff94dda5e5a37386fce82710f724` to remove this pairing evolution, restoring validated runtime `0f57af0525ea869a0fc853e51045f25ea2ab85a1` (CI #146). No destructive schema rollback is required. Normal evolution must preserve the Constitution, HEART/BRAIN boundary and approval gates.

## Next identified gaps
- Add an owner-authenticated typed/filtered trust-history endpoint.
- Replace static owner enrollment/management shared-secret authentication with passkeys/WebAuthn/OIDC-grade authentication.
- Move durable device possession credentials away from general browser localStorage when a stronger credential primitive is introduced.
- Consider a no-reload secure-continuity handoff after pairing once the inline avatar script is modularized.
- Consider tamper-evident integrity verification for high-value trust-management audit events.
- Retire unsigned local continuity only after secure owner identity is universally configured and migration-safe.

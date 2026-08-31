# KUPPA Known-Good Baseline

## Runtime baseline
- **Current validated implementation:** `0f57af0525ea869a0fc853e51045f25ea2ab85a1` (avatar-first Trusted Devices sheet with metadata-only inventory, local Forget, global Revoke and repaired UI contract), validated by GitHub Actions CI #146.
- **Previous governed branch head:** `2c46d39716399206ca9d208626f3f57c8f6d0130` (Spring multi-constructor startup-fix documentation head), validated by CI #144.
- **Previous security/runtime baseline:** `34d762d71b752fcaa88c89b9acc0add6780d7a66` (owner-device trust audit ledger), validated by CI #135.

## Current evidence
- The avatar exposes a `Trusted devices` sheet without restoring a conversation window.
- Inventory is metadata-only: label, token version, enrollment/usage timestamps, continuity count and active/revoked state.
- `Forget on this browser` clears local possession/continuity state only; `Revoke everywhere` uses the owner-management remote-revocation boundary.
- The owner-management key is held only in JS memory while the sheet is open and is not written to localStorage.
- Revoking another device refreshes metadata using the already-entered ephemeral credential without another prompt.
- Missing/rejected management credentials leave normal conversation usable and do not mutate trust.
- Persistent revocation, signed continuity, audit ledger, VayuBrainGateway v3, cancellation, parent restoration, confidence-aware memory, state engine, voice barge-in, degraded brain presence, signing-key rotation and consequential-action approvals remain intact.
- CI #145 rejected an over-broad new contract assertion before promotion; CI #146 passed the repaired runtime.

## Scorecard baseline
| Dimension | Evidence status |
|---|---|
| Build stability | Green on CI #146 after CI #145 regression rejection |
| Conversation quality | Conversation/avatar flow unchanged; Trusted Devices remains secondary UI |
| Personality consistency | Unchanged |
| Memory accuracy | Existing confidence-aware memory behavior/tests unchanged |
| Vayu handoff reliability/latency | Vayu Gateway v3 and cancellation/persisted parent lookup unchanged |
| Errors | Management/auth failures are bounded to the sheet and do not mutate trust |
| Voice reliability | Existing playback cancellation/barge-in unchanged |
| UI responsiveness | One avatar-first trust sheet; no conversation window; repeat revoke prompt removed |
| Accessibility | Dialog has labelled title and close control; existing live interaction states unchanged |
| Resource usage | Static CSS/JS + controller only; no new runtime dependency or database work |
| Security boundary | Metadata-only inventory; management secret memory-only; static secret and localStorage device token remain known limitations |

## Rollback policy
Return to `2c46d39716399206ca9d208626f3f57c8f6d0130` to remove this UI evolution. No destructive schema rollback is required. Normal evolution must preserve the Constitution, HEART/BRAIN boundary and approval gates.

## Next identified gaps
- Add an owner-authenticated typed/filtered trust-history endpoint.
- Replace static owner-management shared-secret authentication with passkeys/WebAuthn/OIDC-grade authentication.
- Replace enrollment/management prompts with a safer pairing and trusted-device flow.
- Move durable device possession credentials away from general browser localStorage when a stronger primitive is introduced.
- Consider tamper-evident integrity verification for high-value trust-management audit events.
- Retire unsigned local continuity only after secure owner identity is universally configured and migration-safe.

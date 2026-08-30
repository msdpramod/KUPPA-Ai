# KUPPA Known-Good Baseline

## Runtime baseline
- **Current validated implementation:** `34d762d71b752fcaa88c89b9acc0add6780d7a66` (owner-device trust audit ledger: sanitized enrollment, migration, continuity issuance and revocation audit events), validated by GitHub Actions CI run #135.
- **Previous validated runtime:** `c726f7fef6f9fccb5709ec7e741d41f11a1264ad` (owner device-management boundary: metadata inventory + owner-management remote revocation, with constructor regression repaired), validated by GitHub Actions CI run #132.
- **Previous UI runtime:** `d938200ea9a70a2cb55b71830663d6decc7a4a5e` (avatar owner-device authorized signed continuity with local fallback; CI #125 green).

## Current evidence
- Owner-device possession tokens must pass cryptographic validation and persistent active-trust authorization before owner continuity is issued.
- Explicit owner enrollment persists a device trust record; revoked devices remain denied even while their signed tokens are otherwise valid.
- Owner continuity issuance records `lastContinuityIssuedAt` and increments `continuityIssueCount` per device.
- Existing `POST /api/chat/owner/device/revoke` preserves self-revocation for a currently valid device credential.
- `KUPPA_OWNER_MANAGEMENT_SECRET` is a distinct minimum-32-byte environment-only management credential; weak/missing configuration fails closed.
- `GET /api/chat/owner/devices` exposes owner-scoped device metadata only and never returns bearer tokens or signing material.
- `POST /api/chat/owner/devices/{deviceId}/revoke` permits remote revocation of a lost device without possession of the target device token.
- Successful device enrollment, legacy migration, owner-continuity issuance, self-revocation and owner-management remote revocation now emit sanitized durable audit events using the existing `audit_events` table.
- New trust audit details carry bounded actor/reason codes only; device/token/signing credentials are never written to audit detail.
- Cross-owner remote-revocation failure emits no misleading success audit event.
- CI #135 passed the full Maven test workflow for implementation `34d762d...`.
- The avatar's signed-continuity/local-fallback behavior, VayuBrainGateway v3, correlation/cancellation, persisted parent restoration, confidence-aware memory, state engine, voice barge-in, degraded brain presence, device signing-key rotation, and consequential-action approval gates remain intact.

## Scorecard baseline
| Dimension | Evidence status |
|---|---|
| Build stability | Green on CI run #135 |
| Conversation quality | Existing Continue/Correct/New topic, refresh recovery, and signed continuity preserved |
| Personality consistency | Unchanged |
| Memory accuracy | Existing behavior/tests unchanged |
| Vayu handoff reliability/latency | v3 correlation/cancellation/persisted parent lookup unchanged |
| Errors | Cross-owner remote revocation still fails closed and now also avoids false success audit entries |
| Voice reliability | Existing playback cancellation/barge-in unchanged |
| UI responsiveness | Avatar-first continuity UI unchanged in this Heart cycle |
| Accessibility | Existing live status semantics unchanged |
| Resource usage | Existing audit table reused; no new runtime dependency |
| Security boundary | Device trust lifecycle now leaves sanitized durable audit evidence; browser token storage and static management secret remain known limitations |

## Rollback policy
For the owner-device trust audit ledger, return to `c726f7fef6f9fccb5709ec7e741d41f11a1264ad`. No destructive schema rollback is required because this evolution reuses the existing `audit_events` table. Normal evolution must preserve the KUPPA Constitution and approval gates.

## Next identified gaps
- Add an avatar-first Trusted Devices management sheet that consumes metadata only and clearly distinguishes local Forget from server-side Revoke.
- Add an owner-authenticated typed/filtered trust-history endpoint instead of relying on the broad generic audit view.
- Consider tamper-evident chaining or integrity verification for high-value trust-management audit events.
- Replace the minimal enrollment prompt with a safer pairing flow.
- Move durable device possession credentials away from general browser localStorage when a stronger credential primitive is introduced.
- Replace static owner-management shared-secret authentication with passkey/WebAuthn/OIDC-grade authentication before treating the boundary as phishing resistant.
- Retire migration-on-first-valid-use after all active devices have durable trust records.
- Retire unsigned local continuity only after secure owner identity is universally configured and migration-safe.

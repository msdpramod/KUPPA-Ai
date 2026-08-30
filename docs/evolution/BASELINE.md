# KUPPA Known-Good Baseline

## Runtime baseline
- **Current validated implementation:** `c726f7fef6f9fccb5709ec7e741d41f11a1264ad` (owner device-management boundary: metadata inventory + owner-management remote revocation, with constructor regression repaired), validated by GitHub Actions CI run #132.
- **Previous validated runtime:** `93e59e784eb4ea0b30a8b0021895975da088f3b5` (persistent owner-device trust, per-device self-revocation, registry migration, and continuity-issuance audit; CI #128 green).
- **Previous UI runtime:** `d938200ea9a70a2cb55b71830663d6decc7a4a5e` (avatar owner-device authorized signed continuity with local fallback; CI #125 green).

## Current evidence
- Owner-device possession tokens must pass cryptographic validation and persistent active-trust authorization before owner continuity is issued.
- Explicit owner enrollment persists a device trust record; revoked devices remain denied even while their signed tokens are otherwise valid.
- Owner continuity issuance records `lastContinuityIssuedAt` and increments `continuityIssueCount` per device.
- Existing `POST /api/chat/owner/device/revoke` preserves self-revocation for a currently valid device credential.
- `KUPPA_OWNER_MANAGEMENT_SECRET` is a distinct minimum-32-byte environment-only management credential; weak/missing configuration fails closed.
- `GET /api/chat/owner/devices` exposes owner-scoped device metadata only and never returns bearer tokens or signing material.
- `POST /api/chat/owner/devices/{deviceId}/revoke` permits remote revocation of a lost device without possession of the target device token.
- CI #131 caught a duplicate-constructor compile regression before promotion; repair commit `c726f7f...` then passed full Maven CI #132.
- The avatar's signed-continuity/local-fallback behavior, VayuBrainGateway v3, correlation/cancellation, persisted parent restoration, confidence-aware memory, state engine, voice barge-in, degraded brain presence, device signing-key rotation, and consequential-action approval gates remain intact.

## Scorecard baseline
| Dimension | Evidence status |
|---|---|
| Build stability | Green on CI run #132 after CI #131 correctly blocked a compile regression |
| Conversation quality | Existing Continue/Correct/New topic, refresh recovery, and signed continuity preserved |
| Personality consistency | Unchanged |
| Memory accuracy | Existing behavior/tests unchanged |
| Vayu handoff reliability/latency | v3 correlation/cancellation/persisted parent lookup unchanged |
| Errors | Weak/incorrect owner-management credentials fail closed; cross-owner remote revocation denied |
| Voice reliability | Existing playback cancellation/barge-in unchanged |
| UI responsiveness | Avatar-first continuity UI unchanged in this Heart cycle |
| Accessibility | Existing live status semantics unchanged |
| Resource usage | No new DB table and no new runtime dependency |
| Security boundary | Lost-device remote revocation added through a credential separate from enrollment/signing; browser token storage remains a known limitation |

## Rollback policy
For the owner device-management boundary, return to `93e59e784eb4ea0b30a8b0021895975da088f3b5`. No destructive schema rollback is required because this evolution adds no table or column. Normal evolution must preserve the KUPPA Constitution and approval gates.

## Next identified gaps
- Add an avatar-first Trusted Devices management sheet that consumes metadata only and clearly distinguishes local Forget from server-side Revoke.
- Add a durable management-action audit ledger with actor/device/reason metadata.
- Replace the minimal enrollment prompt with a safer pairing flow.
- Move durable device possession credentials away from general browser localStorage when a stronger credential primitive is introduced.
- Replace static owner-management shared-secret authentication with passkey/WebAuthn/OIDC-grade authentication before treating the boundary as phishing resistant.
- Retire migration-on-first-valid-use after all active devices have durable trust records.
- Retire unsigned local continuity only after secure owner identity is universally configured and migration-safe.

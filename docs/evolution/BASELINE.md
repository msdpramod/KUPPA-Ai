# KUPPA Known-Good Baseline

## Runtime baseline
- **Current validated implementation:** `93e59e784eb4ea0b30a8b0021895975da088f3b5` (persistent owner-device trust, per-device revocation, backward-compatible registry migration, and continuity-issuance audit), validated by GitHub Actions CI run #128.
- **Previous validated runtime:** `d938200ea9a70a2cb55b71830663d6decc7a4a5e` (avatar owner-device authorized signed continuity with local fallback; CI #125 green).
- **Previous identity runtime:** `b88adffb3bd44985bb38feb40c868050aaba70bf` (device signing-key separation and rotation; CI #121 green).

## Current evidence
- Owner-device possession tokens must pass cryptographic validation and persistent active-trust authorization before owner continuity is issued.
- Explicit owner enrollment persists a device trust record.
- Credentials issued before the trust registry can migrate on first use only after their existing cryptographic token validation succeeds.
- A revoked persistent device record remains denied even while its signed token is otherwise unexpired and valid.
- Owner continuity issuance records `lastContinuityIssuedAt` and increments `continuityIssueCount` per device.
- `POST /api/chat/owner/device/revoke` provides self-revocation for a currently valid device credential.
- The avatar's signed-continuity/local-fallback behavior, VayuBrainGateway v3, correlation/cancellation, persisted parent restoration, confidence-aware memory, state engine, voice barge-in, degraded brain presence, device signing-key rotation, and approval gates remain intact.

## Scorecard baseline
| Dimension | Evidence status |
|---|---|
| Build stability | Green on CI run #128 |
| Conversation quality | Existing explicit Continue/Correct/New topic, refresh recovery, and signed continuity preserved |
| Personality consistency | Unchanged |
| Memory accuracy | Existing automated tests; unchanged |
| Vayu handoff reliability/latency | v3 correlation/cancellation/persisted parent lookup unchanged |
| Errors | Revoked/mismatched devices fail closed; legacy valid-device migration preserves compatibility |
| Voice reliability | Existing playback cancellation/barge-in unchanged |
| UI responsiveness | Avatar-first continuity UI unchanged in this Heart cycle |
| Accessibility | Existing live status semantics unchanged |
| Resource usage | One additive JPA table; no new runtime dependency |
| Security boundary | Per-device persistent revocation now supplements HMAC possession-token validation; browser token storage remains a known limitation |

## Rollback policy
For persistent owner-device revocation, return to `d938200ea9a70a2cb55b71830663d6decc7a4a5e`. The `owner_device_trust` table is additive and may remain present after application rollback; no destructive migration is required. Normal evolution must preserve the KUPPA Constitution and approval gates.

## Next identified gaps
- Add an owner-authenticated device inventory and remote revocation path for lost devices; self-revocation still requires the device token.
- Wire the avatar's Forget trust behavior to distinguish local credential removal from server revocation.
- Replace the minimal enrollment prompt with a safer pairing/device-management flow.
- Move durable device possession credentials away from general browser local storage when a stronger credential primitive is introduced.
- Hardware/passkey/OIDC-grade identity remains intentionally out of scope for the current possession-token model.
- Retire migration-on-first-valid-use after all active devices have durable trust records.
- Retire unsigned local continuity only after secure owner identity is universally configured and migration-safe.

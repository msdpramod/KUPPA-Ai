# 2026-08-29 16:40 Heart — Persistent owner-device revocation validation

## Cycle
Heart / Personality / Relationship validation closeout.

## Purpose and hypothesis
Close validation for the persistent owner-device revocation implementation in `93e59e784eb4ea0b30a8b0021895975da088f3b5`. The hypothesis was that server-side active/revoked state can supplement HMAC possession-token validation without breaking existing enrolled devices or changing Vayu cognition.

## Architectural context
KUPPA remains the HEART and owns owner/device relationship continuity and trust. Vayu remains the BRAIN. The implementation adds authorization state after cryptographic validation; it does not move reasoning, planning, retrieval, tool selection, orchestration, or execution into KUPPA.

## Validated changes
- Persistent `owner_device_trust` registry.
- Individual device revocation before token expiry.
- Migration-on-first-valid-use for credentials issued before the registry existed.
- Per-device continuity issuance timestamp and counter.
- `POST /api/chat/owner/device/revoke`.
- Owner continuity requires both cryptographic validity and active persistent trust.

## Validation/build status
GitHub Actions CI run #128 completed successfully for implementation commit `93e59e784eb4ea0b30a8b0021895975da088f3b5`. The workflow ran checkout, Java 25 setup, and the full Maven `test` step successfully.

Focused unit coverage includes explicit registration, backward-compatible migration followed by revocation, continuity issuance audit updates, and owner mismatch fail-closed behavior. Existing repository tests also remained green under the full Maven run.

## Before/after metrics
- Per-device revocation path: 0 -> 1.
- Persistent device trust registry: 0 -> 1 table.
- Per-device continuity issuance audit values: 0 -> 2.
- New runtime dependencies: 0.
- Vayu cognition changes: 0.
- Approval-gate changes: 0.
- Build stability: green CI #125 baseline -> green CI #128 implementation.

## Security/privacy/permission implications
A copied but otherwise valid device token can now be denied after that device is revoked in persistent state. The token remains a possession credential and is not hardware-bound. No token, enrollment secret, or signing secret is persisted in the trust table.

## Failure/fallback evidence
- Revoked persistent records are denied and are not recreated by the legacy migration path.
- Owner mismatch fails closed.
- Existing pre-registry credentials can migrate only after the caller has already passed cryptographic validation in `ChatController`.
- Existing local continuity fallback and approval behavior are unchanged.

## Known limitations
- Revoking a lost device still needs a stronger owner-authenticated management surface; the new self-revoke endpoint requires the device credential.
- Browser localStorage possession-token exposure remains.
- No WebAuthn/passkey binding exists yet.
- The issuance audit is mutable state, not an immutable event ledger.

## Constitution/regression impact
No Constitution change. KUPPA HEART / Vayu BRAIN separation is preserved. No consequential-action approval gate was weakened. No unrestricted execution or self-modification was introduced.

## Rollback
Previous validated runtime: `d938200ea9a70a2cb55b71830663d6decc7a4a5e`. The new table is additive and can remain present if application code is rolled back.

## Risks / technical debt
The migration-on-first-valid-use compatibility path should be retired after all active devices have durable records. Owner-authenticated device inventory/revocation is still required for lost-device recovery.

## Follow-up work
- Wire avatar/device-management UI to distinguish local forget from server revocation.
- Add owner-authenticated inventory and remote revocation.
- Move toward passkey/WebAuthn-bound credentials.

## Next evolution target
A safer pairing/device-management UI that exposes trust state and server revocation without adding a conversation window or moving semantic reasoning into KUPPA.

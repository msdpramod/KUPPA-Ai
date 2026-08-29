# 2026-08-29 16:30 Heart — Persistent owner-device revocation

## Cycle
Heart / Personality / Relationship.

## Purpose and hypothesis
Add a persistent server-side trust record for owner devices so a still-cryptographically-valid possession token can be revoked before expiry. Hypothesis: separating cryptographic validity from active trust will close the current continuity-security gap without changing Vayu cognition or breaking devices enrolled before the registry exists.

## Architectural context
KUPPA remains the HEART and owns owner/device relationship continuity and trust. Vayu remains the BRAIN and is unchanged. Existing HMAC device credentials prove possession; this change adds server-side authorization state after signature validation.

## Detailed changes
- Added `OwnerDeviceTrust` JPA entity and repository.
- Added `OwnerDeviceTrustService` for registration, migration-on-first-valid-use, revocation, and continuity-issuance auditing.
- Explicit owner enrollment now persists a device trust record.
- Existing pre-registry device credentials are migrated only after `OwnerDeviceIdentityService.validate(...)` has already passed.
- Revoked devices remain denied even when their HMAC token is otherwise valid and unexpired.
- Added `POST /api/chat/owner/device/revoke`.
- Owner continuity issuance now requires both cryptographic token validity and active persistent trust.
- Continuity-session issuance increments a per-device issuance counter and timestamp.

## Files/components affected
- `src/main/java/ai/kuppa/chat/OwnerDeviceTrust.java`
- `src/main/java/ai/kuppa/chat/OwnerDeviceTrustRepository.java`
- `src/main/java/ai/kuppa/chat/OwnerDeviceTrustService.java`
- `src/main/java/ai/kuppa/chat/ChatController.java`
- `src/test/java/ai/kuppa/chat/OwnerDeviceTrustServiceTest.java`
- `docs/adr/0008-persistent-owner-device-revocation.md`

## Behavior before
A valid unexpired device token continued to authorize owner continuity until token expiry or signing/enrollment key rotation. There was no individual device revocation record or per-device continuity issuance audit.

## Behavior after
Cryptographic validity is necessary but no longer sufficient. A device must also be active in the persistent trust registry. Individual devices can be revoked immediately. Legacy devices issued before this schema are admitted to the registry only after their existing credential passes cryptographic validation.

## KUPPA/Vayu responsibility impact
KUPPA gains relationship-trust state only. Vayu reasoning, planning, retrieval, tool selection, agent orchestration, execution strategy, cancellation, and provider routing are untouched.

## API/event/schema/config/migration changes
- New endpoint: `POST /api/chat/owner/device/revoke?deviceId=...` with `X-KUPPA-Device-Token`.
- New JPA table: `owner_device_trust`, created through the existing Hibernate `ddl-auto: update` behavior.
- No new environment variables.
- No new runtime dependency.
- Migration behavior: pre-registry device tokens create an active registry entry on first successfully validated owner-continuity request; a revoked existing record is never auto-reactivated.

## Tests/build/lint/smoke checks
Pre-commit static review completed against current Spring/JPA patterns. Added focused unit coverage for explicit registration, legacy migration then revocation, continuity issuance auditing, and owner mismatch fail-closed behavior. Full Maven/CI validation is intentionally pending until the same-commit implementation is published to a PR; this record will be closed by a separate validation-only commit after CI evidence exists.

## Before/after metrics
- Per-device revocation paths: 0 -> 1.
- Persistent owner-device trust records: 0 -> 1 table.
- Per-device continuity issuance audit fields: 0 -> 2 (`lastContinuityIssuedAt`, `continuityIssueCount`).
- Vayu cognition changes: 0.
- Approval-gate changes: 0.
- New dependencies: 0.

## Security/privacy/permission implications
This materially improves revocation of possession credentials but does not convert them into hardware-bound credentials. Revocation requires the currently valid device token; future owner-console/admin revocation should use a stronger owner-authenticated management boundary. No secret is stored in the trust table.

## Known limitations
- Browser localStorage possession-token exposure still exists.
- Device revocation is not yet exposed through a dedicated pairing/device-management UI.
- There is no passkey/WebAuthn hardware binding.
- Audit state records issuance count/time, not a full immutable audit ledger.

## Failures/fallbacks tested
Unit tests cover owner mismatch fail-closed behavior and ensure a revoked device is denied after legacy migration. Existing local continuity fallback is not changed.

## Rollback
Runtime rollback point: `d938200ea9a70a2cb55b71830663d6decc7a4a5e`. The added table is additive; rolling back application code does not require deleting it.

## Risks / technical debt
The migration-on-first-valid-use path preserves backward compatibility but should eventually be retired once all active devices have persistent records. A future owner-authenticated device-management API should support revoking lost devices without possession of their token.

## Dependencies
No new dependency.

## Follow-up work
- Add an owner-authenticated device inventory/revocation surface.
- Wire the avatar `Forget trust` action to server-side revocation where appropriate.
- Replace localStorage possession credentials with passkey/WebAuthn or another stronger binding.

## Next evolution target
UI pairing/device management that clearly distinguishes local forget from server revocation, without moving reasoning into KUPPA.

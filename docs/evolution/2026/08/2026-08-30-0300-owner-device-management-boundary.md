# 2026-08-30 03:00 Heart — Owner device management boundary

## Cycle
Heart / Personality / Relationship.

## Purpose and hypothesis
Add an owner-authenticated device inventory and remote-revocation surface so a lost device can be revoked without possessing that device's bearer token. Hypothesis: separating owner management authentication from enrollment and device-token signing closes the largest remaining trust-continuity gap while preserving KUPPA's HEART role and leaving Vayu cognition untouched.

## Architectural context
KUPPA owns identity, relationship continuity, trust and device presence. Vayu remains the BRAIN and is unchanged. The previous baseline already persisted active/revoked owner-device trust, but self-revocation required the device token and there was no owner-wide inventory API.

## Detailed changes
- Added `OwnerManagementAuthService` with a dedicated minimum-32-byte owner-management secret.
- Added owner-scoped device inventory retrieval ordered by enrollment time.
- Added metadata-only `DeviceSummary`; device bearer tokens and secrets are never returned.
- Added owner-management remote revocation that is idempotent for already-revoked devices.
- Added `GET /api/chat/owner/devices` guarded by `X-KUPPA-Owner-Management-Key`.
- Added `POST /api/chat/owner/devices/{deviceId}/revoke` guarded by the same owner-management boundary.
- Preserved the existing possession-token self-revocation endpoint for backward compatibility.
- Added focused tests for management-secret fail-closed behavior, inventory metadata, owner scoping and remote revocation.

## Files/components affected
- `src/main/java/ai/kuppa/chat/OwnerManagementAuthService.java`
- `src/main/java/ai/kuppa/chat/OwnerDeviceTrustRepository.java`
- `src/main/java/ai/kuppa/chat/OwnerDeviceTrustService.java`
- `src/main/java/ai/kuppa/chat/ChatController.java`
- `src/main/resources/application.yml`
- `src/test/java/ai/kuppa/chat/OwnerManagementAuthServiceTest.java`
- `src/test/java/ai/kuppa/chat/OwnerDeviceManagementServiceTest.java`
- `docs/adr/0009-owner-device-management-boundary.md`

## Behavior before
A device could be revoked only while its valid device token was available. The owner had no authenticated server-side inventory of trusted devices and therefore no server-side remote revocation path for a lost device.

## Behavior after
When `KUPPA_OWNER_MANAGEMENT_SECRET` is safely configured, the owner can list persisted device metadata and revoke any known owner device by device ID without the target device token. The management secret is separate from the enrollment secret and device signing keys. If the management secret is missing/weak or incorrect, management APIs fail closed.

## KUPPA/Vayu responsibility impact
KUPPA gains a trust-management surface only. Vayu reasoning, planning, retrieval, tool discovery, specialist-agent coordination, execution strategy, provider routing, resumable-turn interpretation and cancellation are untouched.

## API/event/schema/config/migration changes
- New config: `KUPPA_OWNER_MANAGEMENT_SECRET` (minimum 32 bytes).
- New API: `GET /api/chat/owner/devices` with `X-KUPPA-Owner-Management-Key`.
- New API: `POST /api/chat/owner/devices/{deviceId}/revoke` with `X-KUPPA-Owner-Management-Key`.
- No new database table; uses existing `owner_device_trust`.
- No new runtime dependency.
- No event-schema change.

## Tests/build/lint/smoke checks
Pre-commit static review completed against the current Spring/JPA patterns. Added focused unit coverage for strong/weak management credentials, owner-scoped inventory, idempotent remote revocation, and cross-owner denial. Full Maven/CI validation is pending the same-commit PR and will be recorded in a validation closeout only after evidence exists.

## Before/after metrics
- Owner-wide trusted-device inventory paths: 0 -> 1.
- Lost-device remote revocation paths without target bearer token: 0 -> 1.
- Distinct owner-management secrets: 0 -> 1.
- Device token/secret fields exposed by inventory: 0 -> 0.
- New DB tables: 0.
- New runtime dependencies: 0.
- Vayu cognition changes: 0.
- Approval-gate changes: 0.

## Security/privacy/permission implications
The management secret is a high-value credential and must remain environment-only. Reusing the enrollment secret was deliberately avoided so enrollment and fleet/device administration can be rotated independently. Inventory returns identifiers, labels, token version, enrollment time, continuity issuance audit metadata and revocation state only; it never returns bearer tokens or signing material. This is still a shared-secret owner-management model, not phishing-resistant authentication.

## Known limitations
- Owner management currently uses a static bearer-style secret, not passkeys/WebAuthn/OIDC.
- There is no immutable event ledger for management actions.
- Device credentials still live in browser localStorage in the current avatar UI.
- The avatar does not yet expose the new inventory/remote-revocation API.

## Failures/fallbacks tested
Focused tests cover weak/missing management configuration, incorrect management credentials, cross-owner remote-revocation rejection, and repeat revocation. Existing local continuity fallback is unchanged.

## Rollback
Runtime rollback point: `93e59e784eb4ea0b30a8b0021895975da088f3b5`. No destructive schema rollback is required because no new table or column is introduced.

## Risks / technical debt
A static management secret is stronger than target-device possession for lost-device revocation, but it should be replaced by phishing-resistant owner authentication. API request auditing is not yet a full immutable audit trail.

## Dependencies
No new dependency.

## Screenshots / visual references
Not applicable; this is a Heart/backend trust-boundary cycle with no UI change.

## Follow-up work
- Add a trusted-devices UI/pairing surface that consumes metadata only.
- Add explicit management-action audit records with actor/device/reason metadata.
- Move browser possession credentials away from localStorage.
- Introduce passkey/WebAuthn owner authentication before retiring shared-secret management.

## Next evolution target
15:00 UI cycle: a compact avatar-first Trusted Devices sheet that lists metadata, distinguishes local Forget from server Revoke, and never exposes credentials or moves reasoning into KUPPA.

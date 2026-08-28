# 2026-08-29 03:00 Heart — Device signing-key separation and rotation

## Purpose and hypothesis
Separate owner-enrollment authentication from device-token signing, while preserving existing `v1` device credentials and enabling a bounded two-key rotation window. Hypothesis: independent signing keys reduce the blast radius of an enrollment-value exposure and make planned signing-key rotation possible without forcing every enrolled device offline at once.

## Architectural context
KUPPA remains the HEART and owns identity, trust and relationship continuity. Vayu remains the BRAIN and owns reasoning, planning, retrieval, orchestration, tools and execution strategy. Preflight inspected governed branch `6d6c27c012cb6bc6e15f9a062a2ba8db9684aeb7`, `docs/evolution/BASELINE.md`, the 2026-08-28 owner-device evolution record, the evolution index, and the immutable KUPPA Constitution. The validated runtime before this change was `46fd36cdf88e6441e56fc41c63e181ef64dc0d6c` with CI #118 green.

## Detailed changes
- Added environment-only `KUPPA_DEVICE_SIGNING_SECRET` for dedicated `v2` device-token signing.
- Added optional `KUPPA_DEVICE_PREVIOUS_SIGNING_SECRET` so a previous strong signing key can be accepted during planned rotation.
- New enrollments emit `v2` tokens when a dedicated signing key is configured; deployments without it remain on the existing `v1` behavior for backward compatibility.
- `v1` tokens continue to validate against the enrollment value during migration until their normal expiry.
- `v2` validation first checks the active device-signing key and then the optional previous key.
- Weak or structurally unsafe dedicated-signing configuration fails closed rather than silently falling back.
- Device enrollment responses now include additive `tokenVersion` metadata.
- Added regression tests for v2 issuance, legacy v1 compatibility, migration, successful rotation overlap, rejection outside the rotation window, expiry, tampering, weak configuration and label normalization.

## Files/components affected
`OwnerDeviceIdentityService`, `application.yml`, `OwnerDeviceIdentityServiceTest`, ADR 0007, evolution index, changelog, and this evolution record.

## Behavior before
The same owner enrollment value authenticated enrollment requests and signed every device token. Rotating it simultaneously changed both enrollment authentication and token signing, and there was no previous-key overlap mechanism.

## Behavior after
Enrollment authentication can remain stable while device-token signing uses its own strong key. Operators can rotate the device signing key by moving the old key to `KUPPA_DEVICE_PREVIOUS_SIGNING_SECRET` and installing a new `KUPPA_DEVICE_SIGNING_SECRET`, then remove the previous key after the intended overlap window. Existing v1 tokens remain valid until expiry so the migration is not a forced logout.

## KUPPA/Vayu responsibility impact
KUPPA HEART gains only trust-boundary hardening. Vayu BRAIN is untouched. No semantic interpretation, planning, model routing, tool selection, agent orchestration, knowledge retrieval or execution logic moved into KUPPA.

## API/event/schema/config/migration changes
API response change: `DeviceCredential` adds `tokenVersion`. New config: `KUPPA_DEVICE_SIGNING_SECRET` and `KUPPA_DEVICE_PREVIOUS_SIGNING_SECRET`. Database schema changes: none. Runtime dependencies: none. Migration path: blank dedicated signing key preserves v1; setting a strong active key switches new credentials to v2 while old v1 tokens continue to validate.

## Tests/build/lint/smoke checks
A direct local clone/test run was attempted before repository mutation, but the execution environment could not resolve `github.com`; no local Maven result is claimed. The implementation was therefore isolated behind PR #6 and not promoted until repository CI completed. GitHub Actions CI #121 then completed successfully for implementation commit `b88adffb3bd44985bb38feb40c868050aaba70bf`; checkout, Java setup and the full Maven Test step passed. The change was promoted through merge `cbc3a5c8583e142efd2eb5a197b2db3d9983b5a5` only after that green result.

## Before/after metrics
Dedicated device-token signing keys: 0 -> 1 active plus 1 optional previous key. Token protocol versions emitted: v1 only -> v1 legacy or v2 dedicated. Planned signing-key overlap paths: 0 -> 1. Existing v1 migration compatibility: preserved. New runtime dependencies: 0. Database schema changes: 0. Vayu cognition changes: 0. Approval-gate changes: 0. Build stability: green (#118/#119) -> green (#121).

## Security/privacy/permission implications
The owner enrollment and device signing values remain environment-only and are never committed. Dedicated signing reduces credential-purpose coupling but does not create hardware attestation. Keeping v1 validation during migration means the enrollment value can still validate pre-migration tokens until they expire; operators seeking immediate invalidation must rotate that value as well. Consequential external actions remain approval-gated and unchanged.

## Failure/fallbacks tested
Focused tests cover weak active-signing configuration, a previous key without a valid active key, expired tokens, tampered tokens/device identity, an old v2 token rejected when its key is not configured as previous, successful previous-key rotation overlap, and continued v1 validation during migration. The full Maven suite passed with these tests.

## Known limitations
There is still no per-device revocation registry, hardware-bound key, passkey/OIDC layer, multi-owner model, or automatic secret-rotation scheduler. Previous-key acceptance is configuration-driven and should be removed deliberately after the overlap window. The avatar does not yet use the owner/device-authorized path.

## Rollback
Return the runtime to `46fd36cdf88e6441e56fc41c63e181ef64dc0d6c` or governed pre-change head `6d6c27c012cb6bc6e15f9a062a2ba8db9684aeb7`. The change is schema-free and preserves v1 compatibility, so rollback requires no database migration.

## Risks / technical debt
The backward-compatible v1 validation path continues to couple legacy token verification to the enrollment value until old tokens expire. Per-device revocation remains the next missing identity control.

## Dependencies
Only existing JDK/JCA/Spring facilities are used.

## Screenshots / visual references
Not applicable; this Heart cycle changes no avatar/UI surface.

## Follow-up work
Heart: add persistent per-device revocation and renewal/audit semantics. UI: migrate the avatar to server-issued owner/device continuity credentials with graceful fallback.

## Next evolution target
Persistent per-device revocation with auditability, without granting KUPPA any Vayu cognition responsibility.

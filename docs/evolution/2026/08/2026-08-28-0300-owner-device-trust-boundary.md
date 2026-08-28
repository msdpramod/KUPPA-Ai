# 2026-08-28 03:00 Heart — Owner-enrolled device trust boundary

## Purpose and hypothesis
Introduce an explicit owner/device trust boundary before KUPPA is moved to an always-running shared/cloud environment. Hypothesis: requiring owner enrollment before issuing a device credential, then requiring that device credential before issuing a continuity session, materially reduces the chance that browser/session possession is mistaken for owner identity while preserving current clients.

## Architectural context
KUPPA remains the HEART and owns identity, trust, relationship continuity, personal-context presentation, voice/avatar presence, and the continuity interface. Vayu remains the BRAIN and owns reasoning, planning, orchestration, knowledge retrieval/freshness, tool/agent selection, execution strategy, and self-healing. The previous runtime baseline was `a2adb3b89cc1dad11be4ef2f20ccff6fb70494b7`; CI #115 was green on the signed continuity-session implementation. The previous scorecard explicitly identified owner/device identity as the next cloud-continuity gap.

## Detailed changes
- Added `OwnerDeviceIdentityService` with environment-configured owner id, strong enrollment secret requirement, random device IDs, expiring `v1` HMAC-SHA256 device tokens, constant-time secret/signature comparison, and fail-closed validation.
- Added `POST /api/chat/owner/device`, gated by `X-KUPPA-Owner-Enroll-Key`, to issue an owner-scoped device credential.
- Added `POST /api/chat/session/owner`, gated by `deviceId` + `X-KUPPA-Device-Token`, to issue the existing signed continuity credential only after device validation.
- Added identity configuration keys in `application.yml` without committing a secret.
- Added focused unit tests for success, wrong enrollment secret, tampering/wrong device, expiry, weak configuration, and device-label normalization.
- Added ADR 0006 for the trust-boundary decision.

## Files/components affected
`OwnerDeviceIdentityService`, `ChatController`, `application.yml`, `OwnerDeviceIdentityServiceTest`, ADR 0006, evolution index, changelog.

## Behavior before
A caller could obtain a signed continuity session directly from `/api/chat/session`. The credential proved session possession only; there was no owner-enrolled device credential available to gate stronger continuity issuance.

## Behavior after
Deployments that configure `KUPPA_OWNER_ENROLLMENT_SECRET` can enroll a device and use its expiring signed device credential to obtain a signed continuity session from `/api/chat/session/owner`. Existing `/api/chat/session` remains unchanged for backward compatibility and is not relabeled as owner-authenticated.

## KUPPA/Vayu responsibility impact
KUPPA HEART gains only an identity/trust primitive. Vayu BRAIN is untouched. No semantic interpretation, planning, model routing, tool selection, agent orchestration, or execution logic moved into KUPPA.

## API/config/schema changes
New APIs: `POST /api/chat/owner/device`; `POST /api/chat/session/owner`. New headers: `X-KUPPA-Owner-Enroll-Key`; `X-KUPPA-Device-Token`. New config: `KUPPA_OWNER_ID`, `KUPPA_OWNER_ENROLLMENT_SECRET`, `KUPPA_DEVICE_TOKEN_TTL_SECONDS`. Database schema changes: none. Runtime dependencies: none.

## Tests/build/lint/smoke checks
GitHub Actions CI run #118 completed successfully for implementation commit `46fd36cdf88e6441e56fc41c63e181ef64dc0d6c`. Checkout and Java setup succeeded and the full Maven `Test` step succeeded. Focused tests exercise successful enrollment plus wrong secret, wrong device/tampering, expiry, weak configuration, and label normalization. Existing project tests also remained green. No browser/UI code changed, so no separate UI lint/screenshot check was required for this Heart-cycle backend boundary.

## Before/after metrics
Owner-enrolled device credential paths: 0 -> 1. Owner-gated continuity issuance paths: 0 -> 1. Focused device-identity tests: 0 -> 6. New runtime dependencies: 0. Database schema changes: 0. Vayu cognition changes: 0. Approval-gate changes: 0. Build stability: green (#115) -> green (#118).

## Security/privacy/permission implications
The enrollment secret is never committed and must be at least 32 bytes. Device tokens are expiring possession credentials; they are not hardware attestation and should not be described as such. Rotating the owner enrollment secret invalidates all outstanding device tokens. Consequential external actions remain approval-gated and unchanged.

## Failure/fallback paths tested
Weak/missing owner secret disables enrollment. Wrong enrollment secret is rejected. Malformed, tampered, expired, or wrong-device tokens fail validation. If continuity signing is disabled, owner-authenticated continuity issuance is unavailable. Existing continuity APIs remain available exactly as before for backward compatibility.

## Known limitations
No device revocation list, hardware-bound keys, OIDC/OAuth, multi-owner model, or cross-device transcript discovery is added. The same enrollment secret currently signs device tokens; secret separation can be introduced later if operational needs justify it. The avatar does not yet use the owner-authenticated path.

## Rollback
Return the runtime to `a2adb3b89cc1dad11be4ef2f20ccff6fb70494b7`. The change is additive and schema-free, so rollback requires no database migration.

## Risks / technical debt
The backward-compatible direct `/api/chat/session` path remains weaker than owner-gated issuance. It should be retired only after a validated avatar/device migration. Device possession is not equivalent to physical-device attestation.

## Dependencies
JDK/Spring/JCA already present in the repository; no new library dependency.

## Follow-up work / next evolution target
Migrate the avatar to enroll/use owner-authorized device continuity with graceful degradation, then add revocation/rotation and true cross-device owner continuity without exposing transcript text.

# 2026-08-31 03:00 — Owner-device audit ledger

## Cycle
Heart / Personality / Relationship.

## Commit purpose and hypothesis
Add durable, sanitized owner-device trust events to KUPPA's existing audit ledger. The hypothesis is that relationship continuity becomes materially safer when enrollment, legacy migration, owner-continuity issuance, self-revocation, and owner-management remote revocation leave an inspectable server-side record without exposing credentials or moving cognition into KUPPA.

## Architectural context
KUPPA remains the HEART: identity, trust, continuity, memory-facing relationship context, avatar/voice presence and human interaction. Vayu remains the BRAIN: reasoning, planning, retrieval, tool selection, specialist-agent orchestration and execution strategy. This change is strictly within KUPPA's owner/device trust boundary and does not alter VayuBrainGateway v3.

The preflight baseline was `c726f7fef6f9fccb5709ec7e741d41f11a1264ad`, green on CI #132 after CI #131 correctly blocked a compile regression. The previous scorecard identifies durable management-action auditability as a remaining trust gap.

## Detailed changes
- Inject the existing `AuditService` into `OwnerDeviceTrustService` in production while preserving a clock-only package constructor for deterministic unit tests.
- Record sanitized device trust events after successful state-changing operations only.
- Add event types for explicit enrollment, validated legacy migration, owner-continuity issuance, device self-revocation and owner-management remote revocation.
- Use the existing audit `actionId` as the device identifier.
- Store only bounded actor/reason codes in audit detail, for example `actor=OWNER_MANAGEMENT;reason=REMOTE_REVOCATION`.
- Add regression tests proving successful remote revocation records the expected sanitized event and cross-owner failure produces no audit event.

## Files/components affected
- `src/main/java/ai/kuppa/chat/OwnerDeviceTrustService.java`
- `src/test/java/ai/kuppa/chat/OwnerDeviceTrustServiceTest.java`
- `CHANGELOG.md`
- `docs/evolution/README.md`
- this evolution record

## Behavior before
Persistent device trust stored current device state and continuity issuance counters, but the trust lifecycle did not consistently emit durable audit events for enrollment, migration, continuity issuance and revocation operations.

## Behavior after
Successful device trust state changes emit durable audit events through the repository's existing `audit_events` mechanism. Failed cross-owner remote revocation does not emit a misleading success event. No tokens, secrets or signing material are written to the event detail.

## KUPPA/Vayu responsibility impact
KUPPA gains trust-history observability only. Vayu cognition, planning, routing, memory reasoning, provider selection and tool orchestration are unchanged.

## API/event/schema/config/migration changes
- New audit event types: `OWNER_DEVICE_ENROLLED`, `OWNER_DEVICE_MIGRATED`, `OWNER_DEVICE_CONTINUITY_ISSUED`, `OWNER_DEVICE_REVOKED_SELF`, `OWNER_DEVICE_REVOKED_REMOTE`.
- No new HTTP API.
- No database schema migration; the existing `audit_events` table is reused.
- No new configuration or dependency.

## Validation
Pre-publish checks for the feature branch include focused unit coverage in `OwnerDeviceTrustServiceTest`. Full Maven CI must pass on the pull request before this runtime change can be promoted to `agent/avatar-ui`. A validation closeout record will update the known-good baseline and this index with the exact commit/CI evidence after GitHub Actions completes.

Relevant failure path covered: cross-owner remote revocation returns no result and emits no audit success event.

## Before/after metrics
| Metric | Before | After |
|---|---:|---:|
| Device trust lifecycle audit event types | 0 | 5 |
| Remote-revocation actor/reason evidence | 0 | 1 sanitized event path |
| Credential material in new audit details | 0 | 0 |
| New DB tables/columns | 0 | 0 |
| New runtime dependencies | 0 | 0 |
| Vayu cognition changes | 0 | 0 |
| Approval-gate changes | 0 | 0 |

## Security/privacy/permission implications
Audit details contain only stable event type, device ID in the existing action-id field, and bounded actor/reason codes. They intentionally exclude owner enrollment keys, owner management keys, device bearer tokens, continuity tokens and signing secrets. Consequential external-action approval gates are untouched.

## Known limitations
- The generic `/api/audit` surface predates this change and remains broader than an eventual owner-specific trust-history view should be.
- Audit events are append-only by convention, not yet cryptographically tamper-evident.
- A static owner-management shared secret remains an interim authentication mechanism.
- Device possession credentials remain browser bearer credentials until a stronger passkey/WebAuthn-style primitive is introduced.

## Failures/fallbacks tested
- Cross-owner remote revocation does not emit a success audit event.
- Existing clock-only tests remain supported without requiring an audit repository.

## Rollback
Return to runtime `c726f7fef6f9fccb5709ec7e741d41f11a1264ad` (or governed branch merge `6b5582dbd17eff2e1a2b94310bc0d0c2f43c640d`). No schema rollback is required.

## Risks/technical debt
Reusing the generic audit table avoids schema churn but leaves device-trust history mixed with other audit domains. A later owner-authenticated filtered audit API should expose typed metadata rather than raw generic details.

## Dependencies
No new dependencies.

## Follow-up work
- Add an owner-authenticated, metadata-only trusted-device audit/history endpoint.
- Add the avatar-first Trusted Devices management sheet.
- Move owner management toward passkeys/WebAuthn and away from a static shared secret.

## Next evolution target
For the next UI cycle, build an avatar-first Trusted Devices sheet that distinguishes local Forget from global Revoke and consumes metadata only. For the next Heart cycle, make the trust ledger typed and owner-filtered, then consider tamper-evident chaining.

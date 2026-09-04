# ADR 0008 — Persistent owner-device revocation

## Status
Accepted for implementation; CI validation is recorded separately after the implementation commit runs.

## Context
KUPPA already signs owner-device possession credentials and supports signing-key rotation. A valid token could not, however, be invalidated for one specific device before expiry. Rotating a global signing key is too broad and does not provide per-device relationship control.

## Decision
Persist owner-device trust separately from token cryptography.

A device is authorized for owner continuity only when:
1. `OwnerDeviceIdentityService` validates the device token cryptographically; and
2. the persistent owner-device trust record is active for the configured owner.

Explicit enrollments create the trust record immediately. Credentials created before this registry are migrated on first valid use, but only after signature validation. Once a record is revoked, the migration path must never reactivate it.

## Consequences
- Individual devices can be revoked before token expiry.
- KUPPA gains a durable relationship/trust primitive suitable for later device-management UI.
- Continuity issuance can be counted and timestamped per device.
- One additive table is introduced under existing JPA schema management.
- The possession-token model remains weaker than hardware/passkey binding.

## HEART/BRAIN boundary
This ADR concerns KUPPA HEART identity/relationship continuity only. Vayu BRAIN cognition, tool selection, reasoning, planning, retrieval, orchestration, and execution remain unchanged.

## Safety
No secrets are persisted in the trust record. Consequential-action approval gates are unchanged. No arbitrary execution or self-modification capability is introduced.

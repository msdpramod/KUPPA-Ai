# 2026-08-29 03:10 Heart — Device signing-key rotation validation

## Purpose and hypothesis
Close the validation loop for the 03:00 Heart evolution after repository CI. The implementation hypothesis was that separating enrollment authentication from device-token signing, with a single previous-key overlap, would strengthen KUPPA's owner/device trust boundary without breaking legacy v1 credentials or changing Vayu responsibilities.

## Architectural context
KUPPA remains the HEART and owns identity, trust and relationship continuity. Vayu remains the BRAIN and owns reasoning, planning, retrieval, orchestration, tools, specialist agents and execution strategy. The pre-change governed head was `6d6c27c012cb6bc6e15f9a062a2ba8db9684aeb7`; the implementation commit is `b88adffb3bd44985bb38feb40c868050aaba70bf`; governed promotion merge is `cbc3a5c8583e142efd2eb5a197b2db3d9983b5a5`.

## Detailed changes in this documentation-only closeout
- Promoted the implementation to the known-good baseline after CI success.
- Recorded CI #121 as the validation evidence.
- Replaced the pending evolution-index state with the exact implementation commit and green result.
- Marked ADR 0007 as validated/accepted.
- Updated the implementation record so it describes the validated state rather than the pre-CI staging state.

## Files/components affected
Documentation only: `docs/evolution/BASELINE.md`, `docs/evolution/README.md`, the 03:00 implementation record, ADR 0007, and this closeout record.

## Behavior before
The implementation was isolated behind PR #6 and deliberately not promoted because local Maven execution was unavailable in the tool environment.

## Behavior after
PR #6 has been merged only after CI #121 completed successfully. The governed runtime now contains dedicated v2 device-token signing with optional previous-key validation, while preserving legacy v1 migration behavior.

## KUPPA/Vayu responsibility impact
None in this closeout. The implementation remains a KUPPA HEART trust-boundary change only. Vayu BRAIN behavior is unchanged.

## API/event/schema/config/migration changes
No additional API/config/schema changes in this documentation-only commit. The validated implementation adds additive `tokenVersion` metadata and environment-only active/previous device-signing configuration. No database migration or dependency was added.

## Tests/build/lint/smoke checks run with results
GitHub Actions CI run #121 completed successfully for implementation commit `b88adffb3bd44985bb38feb40c868050aaba70bf`. Job `test` completed successfully. Steps `actions/checkout@v4`, `actions/setup-java@v4`, and Maven `Test` all completed successfully. The full project Maven test suite therefore passed before promotion.

A direct local clone had earlier failed because the execution environment could not resolve `github.com`; this is recorded as an environment limitation, not a test failure.

## Before/after metrics
Build stability: green (#118/#119) -> green (#121). Dedicated device signing: 0 -> 1 active key plus one optional previous key. Token issuance modes: v1 only -> v1 legacy or v2 dedicated. Planned rotation overlap paths: 0 -> 1. Vayu cognition changes: 0. Personality/memory changes: 0. UI changes: 0. Approval-gate changes: 0. Runtime dependencies: 0. Database schema changes: 0.

## Security/privacy/permission implications
No secrets were committed. Dedicated signing reduces credential-purpose coupling. Previous-key acceptance is intentionally bounded by operator configuration and should be removed after the rotation window. Legacy v1 validation remains until expiry for backward compatibility. Consequential external actions remain approval-gated.

## Failure/fallbacks validated
The committed focused tests cover legacy v1 mode, dedicated v2 issuance, migration of v1 credentials into dedicated-signing deployments, previous-key overlap acceptance, rejection when the old key is not configured as previous, expiry, tampering, weak dedicated configuration, incomplete rotation configuration, and device-label normalization. The full Maven suite passed around these tests.

## Known limitations
Per-device revocation is still absent. This is still a possession-token model rather than hardware attestation/passkeys/OIDC. Previous-key removal is operational rather than scheduled automatically. The avatar still uses the legacy browser continuity path and has not yet migrated to owner/device-authorized continuity.

## Rollback
Return to validated implementation `46fd36cdf88e6441e56fc41c63e181ef64dc0d6c` or governed pre-change head `6d6c27c012cb6bc6e15f9a062a2ba8db9684aeb7`. No schema rollback is required.

## Risks / technical debt
Legacy v1 verification remains coupled to the enrollment value until those tokens expire. The next Heart-side control should be persistent per-device revocation with auditable renewal/revocation events.

## Dependencies
No new dependencies.

## Screenshots / visual references
Not applicable; no UI changed.

## Follow-up work
Body/UI cycle: migrate the avatar to server-issued owner/device continuity credentials with graceful fallback. Heart cycle: add persistent per-device revocation and renewal/audit semantics.

## Next evolution target
Persistent per-device revocation, followed by owner/device-aware avatar continuity.

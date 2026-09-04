# 2026-09-02 03:00 — Typed owner trust history

## Cycle
Heart / Personality / Relationship.

## Commit purpose and hypothesis
Expose KUPPA's existing owner-device trust ledger through a narrow, typed, owner-authenticated read contract instead of requiring consumers to inspect the generic audit surface. Hypothesis: relationship continuity and owner trust become easier to inspect safely when trust history is explicit, bounded and metadata-only.

## Architectural context
KUPPA remains the HEART: identity, trust, relationship continuity, personal context and human-facing presence. Vayu remains the BRAIN: reasoning, planning, retrieval, tool/skill selection, specialist-agent orchestration and execution strategy. The preflight governed head was `c0794f918d872acecedb035d4c75dd270f56fd71`; runtime baseline `b781e4bd00233dbf7d16a5d34ea686649c330451` was green on CI #152. The baseline identified an owner-authenticated typed/filtered trust-history endpoint as the next Heart gap.

## Detailed changes
- Added `GET /api/chat/owner/trust-history` protected by the existing owner-management credential boundary.
- Added optional `deviceId` filtering and a bounded `limit` (1..100, default 50).
- Added repository queries restricted to the five owner-device trust lifecycle event types.
- Added a typed response containing only event type, device ID, bounded actor/reason metadata and timestamp.
- Raw generic audit detail is not returned by this contract.
- Added tests for device filtering, typed metadata parsing and maximum-result clamping.

## Files/components affected
- `src/main/java/ai/kuppa/audit/AuditEventRepository.java`
- `src/main/java/ai/kuppa/audit/OwnerTrustHistoryService.java`
- `src/main/java/ai/kuppa/audit/OwnerTrustHistoryController.java`
- `src/test/java/ai/kuppa/audit/OwnerTrustHistoryServiceTest.java`
- `CHANGELOG.md`
- `docs/evolution/README.md`
- this record

## Behavior before
Trust lifecycle events existed in the generic audit table, but owner-facing consumers had no narrow trust-history API and would otherwise need broader audit access/parsing.

## Behavior after
An authenticated owner-management caller can retrieve only typed owner-device trust events, optionally scoped to one device, with a bounded response size. The endpoint never returns raw audit detail or credentials.

## KUPPA/Vayu responsibility impact
KUPPA gains trust/relationship observability only. No reasoning, planning, provider routing, retrieval, tool selection, agent coordination or execution behavior moved from Vayu.

## API/event/schema/config/migration changes
- New read-only API: `GET /api/chat/owner/trust-history?deviceId=<optional>&limit=<optional>`.
- Existing header: `X-KUPPA-Owner-Management-Key`.
- No new event types, schema migration, configuration, database table or dependency.

## Tests/build/lint/smoke checks
Added `OwnerTrustHistoryServiceTest` covering typed remote-revocation metadata, device filtering and 100-item clamping. Full Maven/GitHub Actions validation is required before promotion; this record does not claim CI success before it exists.

## Before/after metrics
| Metric | Before | After |
|---|---:|---:|
| Owner-specific typed trust-history APIs | 0 | 1 |
| Trust event types exposed by new API | 0 | 5 allow-listed |
| Raw generic audit detail exposed by new API | 0 | 0 |
| Maximum response items | unbounded/not available | 100 |
| New DB schema/dependencies | 0 | 0 |
| Vayu cognition changes | 0 | 0 |

## Security/privacy/permission implications
The endpoint reuses owner-management authentication, allow-lists trust event types and returns metadata only. It does not expose device bearer tokens, continuity tokens, owner enrollment/management secrets, signing material, or arbitrary audit rows. Consequential-action approval gates are unchanged.

## Known limitations
- Current owner-management authentication is still a static shared secret, not passkey/WebAuthn-grade identity.
- The audit table is single-owner-oriented today; events do not carry an explicit owner ID. This contract is safe under the current single-owner model but must gain owner-scoped persistence before multi-owner support.
- Audit integrity is not yet tamper-evident.

## Failures/fallbacks tested
Unit coverage verifies filtering does not fall back to the generic all-audit query. The controller fails closed when owner management is disabled or the credential is rejected through the existing auth service. CI must verify wiring/startup.

## Rollback procedure / known-good reference
Return to governed head `c0794f918d872acecedb035d4c75dd270f56fd71`, restoring runtime baseline `b781e4bd00233dbf7d16a5d34ea686649c330451`. No schema rollback is needed.

## Risks/technical debt introduced or removed
Removes the need for trust consumers to parse broad raw audit detail. Remaining debt is shared-secret owner authentication and lack of owner ID on audit rows.

## Dependencies
No new dependencies.

## Screenshots / visual references
Not applicable; no UI change.

## Follow-up work
- Validate API wiring and unauthorized/disabled behavior in CI/integration coverage.
- Introduce passkey/WebAuthn or OIDC-grade owner authentication.
- Add owner-scoped audit persistence before any multi-owner model.
- Consider tamper-evident chaining for high-value trust events.

## Next evolution target
Heart: stronger owner identity and tamper-evident trust evidence. UI: consume this typed history inside the Trusted Devices experience without exposing credentials or displacing the avatar-first interaction.

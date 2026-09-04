# 2026-09-05 03:00 — Owner memory-change history contract

## Cycle
Heart / Personality / Relationship.

## Commit purpose and hypothesis
Introduce a narrow owner-authenticated, typed memory-change history contract so KUPPA can make memory behavior observable without exposing raw personal memory text or relying on the generic developer audit surface. Hypothesis: privacy-safe observability improves trust and correction/forget continuity without expanding KUPPA into Vayu cognition.

## Architectural context
KUPPA remains the HEART: identity, relationship continuity, personal memory interface and trust. Vayu remains the BRAIN: reasoning, planning, retrieval, tool/agent orchestration and execution strategy. This change does not alter Vayu Gateway v3, planner behavior, providers, tools, agents or approval gates.

## Detailed changes
- Added `OwnerMemoryHistoryService` with an allow-list of `MEMORY_CAPTURED`, `MEMORY_FORGOTTEN`, and `MEMORY_FORGET_NO_MATCH`.
- Added `GET /api/chat/owner/memory-history?limit=` protected by the existing owner-management credential boundary.
- Typed output contains only bounded memory metadata: event type, category, confidence, source, affected count, affected categories, and timestamp.
- Internal audit `actionId`, correlation IDs, raw audit detail and personal-memory content are not returned.
- Limit defaults to 50 and is clamped to 1..100.
- Parser tolerates malformed/legacy metadata by returning null/empty bounded fields rather than leaking unparsed detail.

## Files/components affected
- `src/main/java/ai/kuppa/audit/OwnerMemoryHistoryService.java`
- `src/main/java/ai/kuppa/audit/OwnerMemoryHistoryController.java`
- `src/test/java/ai/kuppa/audit/OwnerMemoryHistoryServiceTest.java`
- `docs/evolution/README.md`
- `CHANGELOG.md`

## Behavior before
Memory mutation events were privacy-safe in the audit ledger, but owner-facing code had no typed contract and would otherwise need generic audit access.

## Behavior after
An authenticated owner can retrieve a bounded typed history of memory capture/forget outcomes without raw memory text, raw audit detail, internal memory IDs or correlation IDs.

## KUPPA/Vayu responsibility impact
KUPPA gains memory-interface observability only. Vayu cognition and orchestration are unchanged.

## API/event/schema/config/migration changes
- New HTTP API: `GET /api/chat/owner/memory-history`.
- Reuses `X-KUPPA-Owner-Management-Key` and existing `KUPPA_OWNER_MANAGEMENT_SECRET` configuration.
- No new audit event type, database schema, migration, runtime dependency or secret.

## Tests/build/lint/smoke checks
Pre-change governed head `7e47b2e5b527814782d097f8906a4bcaa50c9643` was verified green on CI #173. Added unit coverage for typed privacy-safe mapping, internal identifier non-exposure, limit clamping, and malformed metadata tolerance. Full CI result is pending at commit creation and must pass before promotion.

## Relevant before/after metrics
- Typed owner memory-history APIs: 0 -> 1.
- Allow-listed memory lifecycle event types: 0 -> 3.
- Maximum returned events: unavailable -> 100.
- Raw personal-memory content exposed by new API: 0 -> 0.
- Internal audit action/correlation identifiers exposed by new API: 0 -> 0.
- New schema/dependencies/secrets: 0.
- Vayu cognition changes: 0.

## Security/privacy/permission implications
The endpoint requires the existing owner-management credential. It intentionally does not return raw `AuditEvent.detail` or `actionId`. This preserves the current single-owner trust model but does not strengthen the underlying shared-secret authentication mechanism.

## Known limitations
- Owner authentication is still static shared-secret based; passkeys/WebAuthn/OIDC-grade identity remains future work.
- Historical audit events can only expose metadata that was actually recorded at event time.
- This is observability, not a memory editing endpoint.

## Failures/fallbacks tested
- Oversized requested limits clamp to 100.
- Malformed confidence/count values become null instead of propagating unsafe/unbounded data.
- Duplicate/blank category fragments are bounded and normalized.
- Generic audit listing is not used by the service.

## Rollback procedure / known-good reference
Return to governed branch head `7e47b2e5b527814782d097f8906a4bcaa50c9643`, whose runtime baseline `6be4e77272a3e43ce0f64ba6f8c7f7b2d634dfdd` is green on CI #171 and merge CI #173. No schema rollback is required.

## Risks / technical debt
Adds another owner-management endpoint on top of a shared-secret auth model. The response contract should remain metadata-only even after stronger owner identity is introduced.

## Dependencies
No new dependencies.

## Screenshots / visual references
Not applicable; no UI change.

## Follow-up work
Add safe near-match memory disambiguation that presents candidates and requires explicit owner selection before deletion. Move owner authentication toward passkeys/WebAuthn or OIDC-grade identity.

## Next evolution target
For the next UI cycle, keep avatar presence primary and move the continuity implementation itself behind `KuppaContinuityAdapter` rather than bridging legacy inline functions.

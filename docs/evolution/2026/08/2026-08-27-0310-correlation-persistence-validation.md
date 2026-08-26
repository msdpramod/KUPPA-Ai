# KUPPA Evolution Record — Correlation Persistence Validation

- **Date/time:** 2026-08-27 03:10 Asia/Kolkata
- **Cycle:** Heart / Personality / Relationship — validation closeout
- **Commit purpose:** Record authoritative CI evidence for correlation-keyed conversation persistence and promote the validated implementation as the known-good runtime.
- **Hypothesis:** Promotion only after the complete Maven suite passes keeps resumable continuity improvements inside the regression gate.

## Architectural context
Implementation commit `74ef76ee8624b4d6df256311d13ce15455646556` persisted Vayu correlation/turn metadata in `chat_messages` and added server-side parent restoration for explicit Continue/Correction requests. KUPPA still only carries interaction context; Vayu remains responsible for semantic reasoning and reference resolution.

## Detailed changes
- Promoted implementation commit `74ef76ee8624b4d6df256311d13ce15455646556` to known-good runtime status.
- Recorded GitHub Actions CI #111 as the authoritative validation result.
- Updated `BASELINE.md` and the evolution index.
- No runtime Java, UI, voice, memory, permission, approval, provider-routing, or schema behavior changed in this documentation-only closeout.

## Files/components affected
- `docs/evolution/BASELINE.md`
- `docs/evolution/README.md`
- this validation record

## Behavior before
The implementation was isolated on a validation branch/PR while `agent/avatar-ui` remained on the previous green baseline.

## Behavior after
The repository recognizes correlation-keyed conversation persistence as the current validated runtime.

## KUPPA/Vayu responsibility impact
None in this closeout. The validated implementation preserves KUPPA=HEART and Vayu=BRAIN.

## API/event/schema/config/migration changes
None in this closeout commit. The validated implementation uses existing `/api/chat` fields and adds nullable persistence columns/index through Hibernate schema update.

## Tests/build/lint/smoke checks run with results
- Previous runtime CI #109: **PASS**.
- Validation candidate PR #3 / CI #111: **PASS**.
- CI #111 completed checkout, Java setup, full Maven `Test`, cleanup, and job completion successfully.
- Focused tests include parent restoration and missing-parent fallback in `ConversationContextServiceTest`.

## Relevant before/after metrics
- Persisted turn identity fields: **0 -> 3**.
- Server-side parent lookup paths: **0 -> 1**.
- Missing-parent safe fallback coverage: **0 -> 1 explicit test**.
- Semantic classifiers added to KUPPA: **0**.
- Approval behavior changed: **0**.
- CI state: **green #109 -> green #111**.

## Security/privacy/permission implications
No new permissions, secrets, external destinations, unrestricted shell execution, self-modification, or autonomous consequential actions. Correlation IDs remain metadata, not authorization tokens. Existing approval gates remain unchanged.

## Known limitations
- A fully reset client still needs a way to discover/select a historical parent correlation ID.
- No owner/session scoping model exists yet for multi-user deployment.
- Hibernate `ddl-auto:update` remains technical debt versus explicit migrations.
- No retention/privacy policy or production continuity telemetry yet.

## Failures/fallbacks tested
Unknown/missing parent correlation IDs fall back to recent context without fabricating history. Existing Vayu provider fallback/cancellation tests remain green in the full suite.

## Rollback procedure / known-good reference
Current validated implementation: `74ef76ee8624b4d6df256311d13ce15455646556` (CI #111 green). Roll back runtime behavior to `7ac2b7f2b879ce5f1962e610ab9433c57230e4f7` if needed; additive nullable columns remain backward compatible.

## Risks / technical debt introduced or removed
Removed: inability to resolve a known parent turn server-side. Remaining debt: historical-turn discovery, owner/session scoping, retention policy, explicit DB migration tooling, aggregate telemetry.

## Dependencies
No new dependencies.

## Screenshots / visual references
Not applicable; no UI change in this Heart cycle.

## Follow-up work
Add bounded owner-scoped resumable-turn discovery/state restoration before cloud multi-instance or multi-user deployment.

## Next evolution target
UI cycle: restore the last completed resumable-turn identity after refresh from safe server/session state without reintroducing a conversation window.

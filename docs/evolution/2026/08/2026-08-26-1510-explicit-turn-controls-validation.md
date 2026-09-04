# KUPPA Evolution Record — Explicit Resumable Turn Controls Validation

- **Date/time:** 2026-08-26 15:10 Asia/Kolkata
- **Cycle:** Body / UI / Human Interaction — validation closeout
- **Commit purpose:** Record authoritative CI evidence for the explicit Vayu v3 continuity controls and promote the validated runtime baseline without changing runtime behavior.
- **Hypothesis:** Promoting only after the full CI suite is green preserves the evolution regression gate while keeping the avatar/UI change reviewable and reversible.

## Architectural context
Implementation commit `7ac2b7f2b879ce5f1962e610ab9433c57230e4f7` connected the avatar-first KUPPA HEART UI to existing `VayuBrainGateway v3` continuity metadata through explicit one-shot Continue, Correct, and New topic controls. Natural language remains `AUTO`, so semantic inference still belongs to Vayu.

## Detailed changes
- Promoted implementation commit `7ac2b7f2b879ce5f1962e610ab9433c57230e4f7` to the known-good runtime baseline.
- Recorded GitHub Actions CI #109 as the authoritative validation result.
- Updated the evolution index with the exact implementation commit and this documentation-only closeout.
- No JavaScript, HTML, Java, API, schema, configuration, memory, personality, voice, permission, approval, or provider-routing behavior changed in this closeout commit.

## Files/components affected
- `docs/evolution/BASELINE.md`
- `docs/evolution/README.md`
- this validation record

## Behavior before
The implementation was published and awaiting CI, while the known-good baseline remained `34882775c025ec793decf8846166e700f71a5beb`.

## Behavior after
The repository formally recognizes the explicit v3 avatar continuity wiring as the current known-good runtime.

## KUPPA/Vayu responsibility impact
None in this documentation-only commit. The validated runtime keeps KUPPA as the HEART transporting explicit user-selected interaction intent. Vayu remains the BRAIN responsible for interpreting `AUTO`, resolving references, reasoning, planning, retrieval, orchestration, and execution strategy.

## API/event/schema/config/migration changes
None in this closeout commit. The validated UI uses the existing optional `/api/chat` fields `turnMode` and `parentCorrelationId` and emits browser-only `kuppa-turn-context-change` / `kuppa-turn-completed` events.

## Tests/build/lint/smoke checks run with results
- Pre-change baseline CI #107: **PASS**.
- Implementation CI #109 on `7ac2b7f2b879ce5f1962e610ab9433c57230e4f7`: **PASS**.
- CI #109 completed the full Maven `Test` step successfully.
- Expanded `AvatarBrainPresenceContractTest` preserved existing assertions for degraded/unavailable Vayu status, cancellation, stale-turn suppression, approval rendering, and added v3 continuity assertions.
- Local fresh clone/Maven execution remains unavailable in this environment because direct GitHub DNS resolution is blocked; GitHub Actions is authoritative validation evidence.

## Relevant before/after metrics
- Browser explicit continuity modes: **0 -> 3**.
- Browser turn-context request fields: **0 -> 2**.
- Explicit parent linkage: **0 -> 1 last-completed-turn reference**.
- Semantic turn classifiers added to KUPPA: **0**.
- Conversation windows added: **0**.
- Approval behavior changed: **0**.
- CI state: **green #107 -> green #109**.

## Security/privacy/permission implications
No new permissions, credentials, secrets, network destinations, unrestricted shell execution, self-modification, or autonomous consequential actions. Existing external/high-impact approval gates remain unchanged.

## Known limitations
- Parent linkage is browser-local and only references the last successfully completed turn.
- Persisted conversation messages are not yet keyed by correlation ID.
- Cross-session/device resumability is not available.
- Provider-native cancellation is still unavailable.
- No production telemetry yet measures explicit mode selection accuracy or conversation-quality improvement.

## Failures/fallbacks tested
Existing contract coverage still exercises unavailable/fallback Vayu presentation, cancellation/stale suppression, and approval rendering. Continue/Correct are disabled until a valid last-completed browser turn exists.

## Rollback procedure / known-good reference
Current known-good runtime: `7ac2b7f2b879ce5f1962e610ab9433c57230e4f7` (CI #109 green). Roll back this UI evolution to `34882775c025ec793decf8846166e700f71a5beb` (CI #107 green).

## Risks / technical debt introduced or removed
Removed: the v3 backend/UI continuity wiring gap. Remaining debt: correlation-keyed persistence, cross-session parent lookup, aggregate telemetry, and provider-native cancellation.

## Dependencies
No new dependencies.

## Screenshots / visual references
No screenshot artifact was required for validation. Controls remain intentionally secondary to the avatar.

## Follow-up work
Add correlation-keyed conversation persistence on the Vayu side so resumable parent context can survive browser sessions and multi-instance deployment without moving semantic reasoning into KUPPA.

## Next evolution target
Heart cycle: server-side correlation-keyed turn persistence and Vayu parent-turn lookup with missing/expired-parent fallback behavior.

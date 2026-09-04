# KUPPA Evolution Record — Correlation-Aware Barge-In Validation Closeout

- **Date/time:** 2026-08-25 15:10 Asia/Kolkata
- **Cycle:** Body / UI / Human Interaction — validation closeout
- **Commit purpose:** Record authoritative CI evidence for the correlation-aware avatar interruption implementation and promote its implementation commit to the known-good runtime baseline without changing runtime behavior.
- **Hypothesis:** Baseline promotion only after green CI keeps KUPPA evolution reviewable and prevents an unverified UI change from becoming the rollback reference.

## Architectural context
Implementation commit `2677674a4032ea38b3019ffba04816748793b734` connected KUPPA's avatar-first interaction layer to the already validated `VayuBrainGateway v2` cancellation contract. KUPPA remains HEART; Vayu remains BRAIN. The Constitution is unchanged.

## Detailed changes
- Promoted implementation commit `2677674a4032ea38b3019ffba04816748793b734` to the current validated runtime in `docs/evolution/BASELINE.md`.
- Recorded GitHub Actions CI run #104 as green.
- Replaced the implementation index placeholder with its exact commit hash.
- Added this documentation-only validation record.
- No Java, HTML, JavaScript, API, schema, configuration, memory, personality, voice, permission, or approval behavior changes in this closeout commit.

## Files/components affected
- `docs/evolution/BASELINE.md`
- `docs/evolution/README.md`
- this validation record

## Behavior before
The correlation-aware UI implementation was published but the known-good baseline still pointed to the v2 backend-only runtime pending authoritative CI.

## Behavior after
The repository records a green Maven CI result for the implementation and uses that implementation as the new known-good runtime/rollback reference for subsequent evolutions.

## KUPPA/Vayu responsibility impact
None in this documentation-only commit. The validated implementation lets KUPPA express user interruption and assign caller-visible turn identity; Vayu retains reasoning, provider routing, cancellation semantics, planning, orchestration, and execution strategy.

## API/event/schema/config/migration changes
None in this validation commit. The validated UI consumes the existing `correlationId` field and `POST /api/chat/{correlationId}/cancel`, and emits the browser-only `kuppa-turn-cancelled` event.

## Tests/build/lint/smoke checks run with results
- Pre-publish JavaScript `node --check` on changed turn-management logic: **PASS**.
- Static preflight assertions for correlation propagation/cancellation/stale guards: **PASS**.
- Local fresh Maven clone: **BLOCKED** because the execution container could not resolve `github.com`.
- GitHub Actions CI run #104 on implementation commit `2677674a4032ea38b3019ffba04816748793b734`: **PASS**.
- CI checkout: **PASS**.
- CI Java setup: **PASS**.
- CI Maven `Test`: **PASS**.
- Updated `AvatarBrainPresenceContractTest`: **PASS as part of Maven Test**.

## Relevant before/after metrics
- Build stability: **green (#102) -> green (#104)**.
- Browser correlation IDs: **0 -> 1 per Vayu turn**.
- Browser cancellation integration: **0 -> 1 endpoint path**.
- Typed topic change while busy: **blocked -> supported**.
- Explicit stale-response suppression guards: **0 -> 2**.
- Explicit speech-stop promise settlement: **no -> yes**.
- Approval behavior changed: **0**.

## Security/privacy/permission implications
No changes in this closeout commit. The validated implementation introduced no secrets, new tool capabilities, external destinations, autonomous consequential actions, unrestricted shell execution, or self-modification. Existing approval gates remain intact.

## Known limitations
- Cancellation remains cooperative; provider compute may continue after the user has moved on.
- Active request state remains process-local and needs affinity/distributed state for multi-instance cloud deployment.
- Browser correlation IDs are not authorization tokens.
- Production cancellation acceptance/latency metrics are not yet aggregated.

## Failures/fallbacks tested
The Maven suite validates UI contract preservation; the underlying v2 suite continues to cover active cancellation, unknown cancellation, stale-result suppression, healthy Ollama, degraded fallback, and full Vayu outage. Static UI checks covered the browser interruption/supersession paths.

## Rollback procedure / known-good reference
The new known-good runtime is `2677674a4032ea38b3019ffba04816748793b734`. If this UI behavior regresses in real use, roll back to `7e0df512eeb416a0bd0dfb3d4e8873a16195057c` (CI #102 green). For a deeper avatar rollback, use `1efac9e2485a6181413b30a003a88654c3cd9792`.

## Risks / technical debt introduced or removed
No runtime debt added by this closeout. Remaining debt is provider-native cancellation, distributed lifecycle state, aggregate telemetry, and resumable turn semantics.

## Dependencies
No new dependencies.

## Screenshots / visual references
Not applicable to this documentation-only closeout.

## Follow-up work
Use the validated cancellation path as the foundation for resumable conversations rather than adding more ad-hoc UI state.

## Next evolution target
Heart cycle: define a small versioned resumable-turn context contract for "new topic", "continue previous", and "correct previous" without moving reasoning into KUPPA.

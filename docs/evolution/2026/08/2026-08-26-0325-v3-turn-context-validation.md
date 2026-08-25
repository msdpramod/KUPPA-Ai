# KUPPA Evolution Record — Vayu v3 Turn Context Validation Closeout

- **Date/time:** 2026-08-26 03:25 Asia/Kolkata
- **Cycle:** Heart / Personality / Relationship — validation closeout
- **Commit purpose:** Record authoritative CI evidence for Vayu Brain Gateway v3 resumable-turn context, document the regression caught during validation, and promote the repaired v3 runtime to the known-good baseline without changing runtime behavior.
- **Hypothesis:** Promoting only the repaired state after a full green CI run preserves KUPPA's regression gate and makes the failed intermediate state useful evidence rather than silently hiding it.

## Architectural context
The Heart-cycle implementation `58bc60f202ca70b58ded83df92cda66e732ebed3` introduced `VayuBrainGateway v3` with explicit `AUTO`, `NEW_TOPIC`, `CONTINUE`, and `CORRECTION` turn context plus optional `parentCorrelationId`. CI #106 compiled the code and passed all eight new Vayu gateway tests, but exposed two failures in existing `BrainRouterServiceTest` coverage because the legacy router entrypoint had silently switched provider overloads. The regression-fix commit `34882775c025ec793decf8846166e700f71a5beb` restored legacy routing compatibility while preserving the v3 context-aware path. CI #107 then passed the full Maven suite.

## Detailed changes
- Promoted `34882775c025ec793decf8846166e700f71a5beb` to the current validated runtime in `docs/evolution/BASELINE.md`.
- Recorded CI #106 as the regression-detection run rather than treating the first v3 implementation as validated.
- Recorded CI #107 as the authoritative green validation for the repaired v3 code state.
- Updated the evolution index with exact implementation and regression-fix commit hashes.
- Added this documentation-only validation record.
- No Java, HTML, JavaScript, API, schema, configuration, memory, personality, voice, permission, or approval behavior changes in this closeout commit.

## Files/components affected
- `docs/evolution/BASELINE.md`
- `docs/evolution/README.md`
- this validation record

## Behavior before
The repaired v3 code was published and CI #107 was green, but the repository's known-good baseline still pointed to the prior v2/correlation-aware runtime pending governance closeout.

## Behavior after
The repository formally recognizes the repaired v3 state as known-good and preserves both the failed validation evidence and the successful repair evidence for future evolution decisions.

## KUPPA/Vayu responsibility impact
None in this documentation-only commit. In the validated runtime, KUPPA carries optional continuity intent and parent-turn identity as HEART-level interaction metadata. Vayu remains responsible for interpreting `AUTO`, resolving references, applying continuation/correction semantics, provider routing, reasoning, planning, orchestration, and execution strategy.

## API/event/schema/config/migration changes
None in this closeout commit. The validated v3 API adds optional `turnMode` and `parentCorrelationId` input fields and returns them in brain metadata. No database migration is required.

## Tests/build/lint/smoke checks run with results
- Pre-change branch CI #105: **PASS**.
- v3 implementation CI #106: **FAIL** — compile passed; new `VayuBrainGatewayTest` **8/8 PASS**; full suite found **2 existing BrainRouterServiceTest failures** caused by legacy provider-overload incompatibility.
- Regression fix preserved the existing tests rather than weakening them.
- Repaired v3 CI #107 on `34882775c025ec793decf8846166e700f71a5beb`: **PASS**.
- CI #107 checkout: **PASS**.
- CI #107 Java setup: **PASS**.
- CI #107 Maven `Test`: **PASS**.
- Local fresh clone/Maven run: **BLOCKED** because the execution environment could not resolve `github.com`; GitHub Actions is the authoritative validation evidence.

## Relevant before/after metrics
- Vayu brain contract: **v2 -> v3**.
- Explicit resumable-turn modes: **0 -> 4**.
- Optional parent-turn linkage fields: **0 -> 1**.
- Provider routes receiving normalized continuity guidance: **0 -> 2** (Ollama + OpenAI fallback).
- Validation trajectory: **green #105 -> failed #106 (2 regressions detected) -> green #107**.
- Existing v3 gateway tests in CI #106: **8/8 pass**.
- Approval behavior changed: **0**.
- Personality/memory behavior changed: **0**.
- UI behavior changed: **0**.

## Security/privacy/permission implications
No changes in this closeout. The validated implementation introduced no secrets, new external destinations, tool authority, unrestricted shell execution, self-modification, or autonomous consequential actions. `parentCorrelationId` is context/observability metadata, not authorization. Existing action approval gates remain intact.

## Known limitations
- The current avatar does not yet send explicit v3 turn modes; it continues using backward-compatible `AUTO`.
- `AUTO` still relies on Vayu/model reasoning over recent conversation.
- `parentCorrelationId` is advisory because persisted conversation messages are not yet keyed by correlation ID.
- Cooperative cancellation does not stop provider compute natively.
- Active request lifecycle remains process-local for multi-instance cloud deployment.
- Production metrics for turn-mode accuracy/use and conversation-quality improvement are not yet aggregated.

## Failures/fallbacks tested
CI #106 exercised the regression gate by catching the legacy routing break. Existing healthy Ollama and OpenAI fallback tests failed as expected under that regression and were restored without modification. CI #107 verifies those legacy paths together with v3 context, degraded Vayu behavior, cancellation, stale-result suppression, memory tests, approval tests, and UI contract tests.

## Rollback procedure / known-good reference
The new known-good runtime is `34882775c025ec793decf8846166e700f71a5beb`. If v3 continuity behavior regresses in real use, roll back to `2677674a4032ea38b3019ffba04816748793b734` (CI #104 green). For the earlier backend-only cancellation state, use `7e0df512eeb416a0bd0dfb3d4e8873a16195057c` (CI #102 green).

## Risks / technical debt introduced or removed
Removed: ambiguity at the contract level when an interaction layer explicitly knows a turn is new, continuing, or corrective; and the legacy router compatibility regression caught by CI. Remaining debt: advisory parent linkage, provider-native cancellation, distributed lifecycle state, aggregate telemetry, and some intentional duplication between legacy and context-aware router paths to preserve compatibility.

## Dependencies
No new dependencies.

## Screenshots / visual references
Not applicable; this Heart-cycle closeout has no UI changes.

## Follow-up work
The next UI cycle should use v3 modes only when the interaction state knows the relationship reliably. Natural-language ambiguity should remain `AUTO` so Vayu, not KUPPA, performs semantic inference.

## Next evolution target
Body/UI cycle: wire explicit `NEW_TOPIC`, `CONTINUE`, and `CORRECTION` metadata from reliable avatar interaction events, preserve `AUTO` for ambiguous speech/text, and express continuity subtly without restoring a conversation window.

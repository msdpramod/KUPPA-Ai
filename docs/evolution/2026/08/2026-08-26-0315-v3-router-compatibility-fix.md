# KUPPA Evolution Record — v3 Router Compatibility Fix

- **Date/time:** 2026-08-26 03:15 Asia/Kolkata
- **Cycle:** Heart / Personality / Relationship — regression fix
- **Commit purpose:** Restore the legacy `BrainRouterService.answerDetailed(message, memory)` behavior after CI #106 exposed a compatibility regression introduced by the v3 turn-context overload.
- **Hypothesis:** Keeping the legacy router entrypoint on the legacy provider overloads while using context-aware overloads only for v3 calls will preserve existing behavior/tests without weakening the new Vayu turn-context contract.

## Architectural context
The v3 implementation commit `58bc60f202ca70b58ded83df92cda66e732ebed3` compiled successfully and its new `VayuBrainGatewayTest` cases all passed, but CI #106 failed two existing `BrainRouterServiceTest` cases because the legacy router method delegated into the new provider overloads. The regression was test-visible and represented a real compatibility risk for callers using the old router API.

## Detailed changes
- Restored the pre-v3 execution path for `answerDetailed(String, List<PersonaMemory>)`, using the existing two-argument Ollama/OpenAI provider methods.
- Kept the new three-argument context-aware router path for `VayuBrainGateway v3`.
- Centralized the stable Vayu-unavailable response in a private helper to keep both paths behaviorally aligned.
- Did not change the v3 turn modes, API fields, cancellation behavior, approval gates, memory behavior, or avatar UI.

## Files/components affected
- `src/main/java/ai/kuppa/conversation/BrainRouterService.java`
- `docs/evolution/README.md`
- this evolution record

## Behavior before
Legacy router tests/callers that mock or override the two-argument provider methods could fall through to the Vayu-unavailable response because the router silently switched to the new three-argument provider overloads.

## Behavior after
Legacy router callers remain on the original provider methods. V3 gateway callers use context-aware provider overloads. Both paths retain the same provider/degraded/error semantics.

## KUPPA/Vayu responsibility impact
None. This is a compatibility repair inside the Vayu brain routing layer. KUPPA remains HEART; Vayu remains BRAIN.

## API/event/schema/config/migration changes
None beyond the already-published v3 contract. No schema/config migration.

## Tests/build/lint/smoke checks run with results
- CI #106 compilation: **PASS**.
- CI #106 new `VayuBrainGatewayTest`: **8/8 PASS**.
- CI #106 full Maven suite: **FAIL** — exactly two legacy `BrainRouterServiceTest` assertions failed due to overload compatibility.
- Fix targets the observed failure without altering the existing tests.
- Post-fix GitHub CI: **PENDING** at commit creation; no baseline promotion until green.

## Relevant before/after metrics
- Known CI failures attributable to v3 router compatibility: **2 -> expected 0**.
- New v3 gateway tests changed: **0**.
- Approval behavior changed: **0**.
- External capabilities added: **0**.

## Security/privacy/permission implications
None. No secrets, permissions, tool capabilities, destinations, shell access, or autonomous actions changed.

## Known limitations
The v3 avatar integration remains future work; current UI continues using `AUTO`. Provider-native cancellation and distributed lifecycle state remain unchanged limitations.

## Failures/fallbacks tested
CI #106 proved the failure path and confirmed all new v3 gateway tests passed. The existing legacy healthy Ollama and OpenAI fallback tests are intentionally preserved as the regression gate for this fix.

## Rollback procedure / known-good reference
Until post-fix CI is green, the known-good runtime remains `2677674a4032ea38b3019ffba04816748793b734`. If the fix fails, roll back the branch to that commit rather than promoting v3.

## Risks / technical debt introduced or removed
Removed a backward-compatibility regression. Some duplicated routing code remains intentionally to preserve old and v3 provider entrypoints; it can be refactored later only with explicit tests covering both paths.

## Dependencies
No new dependencies.

## Screenshots / visual references
Not applicable.

## Follow-up work
If CI passes, create a documentation-only validation closeout and promote the v3 implementation/fix state to the known-good baseline.

## Next evolution target
UI cycle: consume v3 turn context only from reliable interaction events; keep natural-language ambiguity in `AUTO` for Vayu to reason about.

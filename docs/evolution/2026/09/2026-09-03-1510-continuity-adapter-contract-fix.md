# 2026-09-03 15:10 — Continuity Adapter Contract Fix

## Cycle
Body / UI / Human Interaction.

## Commit purpose and hypothesis
Repair the new UI contract test after CI #164 exposed that the assertion expected a hard `adapter.forgetDevice()` call while the implementation intentionally uses optional chaining (`adapter?.forgetDevice()`) to preserve graceful fallback when the adapter is unavailable. Hypothesis: align the test with the intended fail-gracefully behavior without weakening the production boundary.

## Architectural context
KUPPA remains the HEART and owns continuity/trust presentation and local interaction behavior. Vayu remains the BRAIN and is untouched. This commit changes only UI contract verification and governance records; runtime behavior from the preceding implementation commit is unchanged.

## Detailed changes
- Updated `TrustedDevicesUiContractTest` to assert the actual optional adapter call used by the fallback path.
- Recorded CI #164 as a caught validation failure rather than promoting the implementation.
- Linked this repair record in the evolution index.

## Files/components affected
- `src/test/java/ai/kuppa/ui/TrustedDevicesUiContractTest.java`
- `docs/evolution/2026/09/2026-09-03-1510-continuity-adapter-contract-fix.md`
- `docs/evolution/README.md`

## Behavior before
The production UI used an optional adapter call for local forgetting, but the new source-contract test searched for the non-optional spelling and failed despite the intended runtime fallback being present.

## Behavior after
The contract test verifies the exact optional adapter call while continuing to reject direct `window.issueOwnerContinuity`, `window.restoreContinuity`, and `window.forgetOwnerDevice` dependencies in Trusted Devices.

## KUPPA/Vayu responsibility impact
None. KUPPA/Vayu responsibilities are unchanged; this is a test-contract correction only.

## API/event/schema/config/migration changes
None.

## Tests/build/lint/smoke checks run with results
CI #164 on implementation commit `b96bf1f08da4d3c0935b93a36b7a647d2db7951d`: Java compilation succeeded; 86 tests ran; 85 passed and one newly added UI contract assertion failed at `TrustedDevicesUiContractTest:57`. Existing Vayu gateway, memory, owner-device trust, continuity, approval and avatar brain-presence tests were green. A fresh full Maven CI run is required for this repair before promotion.

## Relevant before/after metrics
- Runtime production lines changed by this repair: 0.
- CI contract mismatches: 1 -> targeted repair.
- Direct Trusted Devices legacy-global calls: remains 0.
- Vayu cognition changes: 0.
- Approval-gate changes: 0.

## Security/privacy/permission implications
None. The optional fallback is intentionally preserved so missing adapter wiring cannot block local trust cleanup. No credentials, secrets or permission boundaries changed.

## Known limitations
The adapter still bridges to legacy avatar-page continuity functions internally. Browser device bearer-token storage and shared-secret owner management remain unchanged limitations.

## Failures/fallbacks tested
CI #164 exercised the contract suite and exposed the assertion mismatch. The repaired assertion specifically validates the optional local-forget adapter path; a new full CI run will determine promotion eligibility.

## Rollback procedure / known-good reference
No runtime rollback is required for this test-only repair. If the adapter implementation later fails validation, abandon PR #18 and retain governed branch head `d281067dab9759826bfa1ce0d225bf0730f87570` with validated runtime `da8c13b42011360eb63ce30dd14fa0abf1e414a1` (CI #161).

## Risks / technical debt introduced or removed
Removed: false-negative contract expectation. No new runtime debt introduced.

## Dependencies
None.

## Screenshots / visual references
Not applicable; no visual/runtime change.

## Follow-up work
Run the complete Maven suite and only then promote the adapter. If green, close out baseline/index/score evidence in a documentation commit before merge.

## Next evolution target
After successful promotion: move continuity implementation behind the adapter itself, then return focus to avatar micro-interactions and owner-visible memory-change observability.

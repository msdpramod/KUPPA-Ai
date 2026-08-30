# 2026-08-30 03:10 Heart — Owner management constructor regression fix

## Cycle
Heart / Personality / Relationship.

## Purpose and hypothesis
Repair the compile regression caught by GitHub Actions CI #131 before the owner device-management feature is promoted. Hypothesis: removing the duplicate single-String constructor restores compilation without changing the management-auth security contract or any KUPPA/Vayu behavior.

## Architectural context
The prior implementation commit introduced `OwnerManagementAuthService` with both a Spring `@Value` constructor and a package-private test constructor that had the same Java signature. CI correctly rejected the change at compile time. The governed `agent/avatar-ui` runtime was never modified.

## Detailed changes
- Removed the duplicate package-private `OwnerManagementAuthService(String)` constructor.
- Kept the single public Spring constructor with `@Value("${kuppa.identity.management-secret:}")`.
- Tests continue to instantiate that public constructor directly, so no test-only production API is required.
- No API, persistence, authorization, Vayu, memory, voice, UI or approval behavior was changed by this repair.

## Files/components affected
- `src/main/java/ai/kuppa/chat/OwnerManagementAuthService.java`

## Behavior before
CI #131 failed during Maven compilation with `constructor OwnerManagementAuthService(java.lang.String) is already defined`; unit tests never ran.

## Behavior after
The service has one unambiguous constructor. The feature is eligible for a fresh full Maven/CI run; it remains unpromoted until that run is green.

## KUPPA/Vayu responsibility impact
None. KUPPA remains the HEART and owns identity/trust. Vayu remains the BRAIN and is untouched.

## API/event/schema/config/migration changes
None beyond the still-unpromoted owner-management implementation documented in the 03:00 record. This repair introduces no additional API, event, schema, config or migration change.

## Tests/build/lint/smoke checks run with results
- CI #131: **failed at compile**, before tests, because of the duplicate constructor.
- Static repair review: confirmed only one single-String constructor remains.
- Fresh full Maven/CI validation is required after this commit; results will be captured in a validation closeout only if green.

## Relevant before/after metrics
- Compile-blocking duplicate constructors: 1 -> 0.
- Production behavior changes from the intended 03:00 feature: 0.
- Vayu cognition changes: 0.
- Approval-gate changes: 0.
- New dependencies: 0.

## Security/privacy/permission implications
None. The dedicated management secret remains environment-only and minimum-32-byte fail-closed behavior is unchanged. No secret is committed.

## Known limitations
The larger feature limitations remain: static shared-secret management is not passkey/WebAuthn authentication, browser device tokens remain in localStorage, and no immutable management audit ledger exists yet.

## Failures/fallbacks tested
The relevant build failure was observed directly in CI #131. The corrected feature still requires fresh CI coverage of success and failure paths before promotion.

## Rollback procedure / known-good reference
The governed runtime remains unchanged at the previous known-good implementation `93e59e784eb4ea0b30a8b0021895975da088f3b5`. If the repaired feature fails again, do not merge or move `agent/avatar-ui`.

## Risks / technical debt introduced or removed
Removed a compile-time regression. No new technical debt introduced by this repair.

## Dependencies
No new dependency.

## Screenshots / visual references
Not applicable; backend repair only.

## Follow-up work
Run full CI, inspect Maven tests, and only then update the evolution index, changelog and baseline if the feature is validated.

## Next evolution target
Unchanged: after a successful Heart promotion, build the avatar-first Trusted Devices UI that distinguishes local Forget from server Revoke without exposing credentials.

# 2026-09-03 15:00 — Explicit KUPPA Continuity Adapter

## Cycle
Body / UI / Human Interaction.

## Commit purpose and hypothesis
Replace direct Trusted Devices references to classic-script continuity globals with one explicit, versioned UI adapter. Hypothesis: isolating this coupling lowers UI regression risk and makes continuity/trust evolution independently replaceable without changing Vayu cognition or the avatar-first experience.

## Architectural context
KUPPA remains the HEART: identity, trust, continuity, avatar/voice interaction and human-facing presence. Vayu remains the BRAIN: reasoning, planning, retrieval, orchestration, tools, specialist agents and execution strategy. This change is a KUPPA-side UI integration boundary only.

## Detailed changes
- Added `static/kuppa-continuity-adapter.js` with frozen `KuppaContinuityAdapter` contract version `v1`.
- Adapter exposes bounded operations: availability, trusted-continuity activation, continuity restoration and local device forgetting.
- Trusted Devices now calls the adapter rather than `window.issueOwnerContinuity`, `window.restoreContinuity` or `window.forgetOwnerDevice` directly.
- Pairing-complete observability now includes the adapter version.
- `AvatarPageController` injects the adapter before `trusted-devices.js` so the dependency order is explicit.
- Extended UI contract tests to reject regression back to direct classic global bindings.

## Files/components affected
- `src/main/resources/static/kuppa-continuity-adapter.js`
- `src/main/resources/static/trusted-devices.js`
- `src/main/java/ai/kuppa/avatar/AvatarPageController.java`
- `src/test/java/ai/kuppa/ui/TrustedDevicesUiContractTest.java`
- governance documentation/changelog/index in this commit.

## Behavior before
Trusted Devices reached continuity functions directly through classic `window.*` bindings, coupling the trust sheet to implementation details inside the avatar page script.

## Behavior after
Trusted Devices depends on one immutable `KuppaContinuityAdapter v1` surface. If the adapter cannot resolve the existing continuity implementation, pairing fails clearly rather than pretending trusted continuity succeeded. The existing safe local cleanup fallback remains for device forgetting.

## KUPPA/Vayu responsibility impact
KUPPA gains a cleaner internal UI/continuity boundary only. Vayu responsibilities and gateway contracts are unchanged.

## API/event/schema/config/migration changes
- Backend HTTP APIs: none.
- New browser integration contract: `globalThis.KuppaContinuityAdapter`, version `v1`.
- New/extended event metadata: `kuppa-continuity-adapter-ready`; pairing-complete includes `adapterVersion`.
- Database/schema/config/migrations: none.

## Tests/build/lint/smoke checks
Pre-change known-good baseline: `da8c13b42011360eb63ce30dd14fa0abf1e414a1`, CI #161 green. New contract tests cover adapter presence/order-facing behavior and reject direct Trusted Devices access to legacy continuity globals. Full GitHub Actions Maven validation is required before merge; this implementation is not a promoted baseline until green.

## Before/after metrics
- Direct Trusted Devices references to continuity implementation globals: 3 -> 0.
- Explicit versioned KUPPA UI continuity adapters: 0 -> 1.
- Backend APIs/schema/dependencies added: 0.
- Vayu cognition changes: 0.
- Consequential-action approval changes: 0.
- Page reload introduced in pairing success path: 0.

## Security/privacy/permission implications
No secret storage or permission boundary changes. Enrollment/management credentials remain ephemeral as before; device possession token storage limitations remain unchanged. No credentials are added to adapter events.

## Known limitations
The v1 adapter intentionally wraps the current avatar-page continuity implementation; it is an isolation seam, not yet a full ES-module extraction. Device bearer credentials still use browser localStorage. Owner management still uses the existing shared-secret model.

## Failures/fallbacks tested
Contract coverage verifies Trusted Devices no longer depends directly on the three classic global function names. Runtime adapter reports unavailable when any required operation is missing; pairing then stops with a bounded error. Local forgetting retains the previous storage-cleanup/reload fallback if the adapter is unavailable.

## Rollback / known-good reference
Rollback to governed branch head `d281067dab9759826bfa1ce0d225bf0730f87570`, restoring runtime baseline `da8c13b42011360eb63ce30dd14fa0abf1e414a1` (CI #161). No database rollback is required.

## Risks / technical debt
Reduced: direct cross-script coupling in Trusted Devices. Remaining: adapter still bridges to legacy page functions internally; a later refactor can move continuity implementation behind the adapter itself.

## Dependencies
No new runtime or build dependency.

## Screenshots / visual references
Not required: no visual layout change; interaction behavior is preserved.

## Follow-up work
Move the continuity implementation itself behind the adapter contract and add browser-level smoke coverage when available. Continue toward stronger owner authentication/passkeys separately.

## Next evolution target
Owner-visible memory correction/forget observability or safe ambiguous-memory candidate presentation on the next Heart cycle; deeper avatar micro-interaction work only after this adapter remains green.

# 2026-08-31 15:10 — UI / Human Interaction — Trusted Devices Contract-Test Fix

## Purpose and hypothesis
Repair the CI regression detected by run #145 without weakening the credential-safety invariant. The failure came from an over-broad static assertion, not from credential rendering: the implementation legitimately referenced the existing continuity-token storage key only to delete it during local trust cleanup.

## Architectural context
KUPPA remains the HEART and Vayu remains the BRAIN. This repair is limited to UI trust-management behavior and its regression contract.

## Detailed changes
- Replaced the false-positive assertion that rejected any `continuityToken` text with field-specific assertions that reject rendering/access of device credential or secret properties.
- Retained explicit assertions that no owner-management storage key exists and that the management credential is cleared from JS memory.
- Improved the sheet refresh path so revoking a non-current device reuses the in-memory management credential while the sheet remains open instead of prompting a second time.
- Revoking the current browser now clears local trust, closes the sheet and returns immediately.

## Files/components affected
`trusted-devices.js`, `TrustedDevicesUiContractTest`, this evolution record, and the evolution index.

## Behavior before / after
Before: CI #145 failed one new test after all existing tests passed; a successful remote revoke of another device would also cause another management-key prompt. After: the guard targets actual credential-property exposure, and in-sheet refresh reuses the ephemeral key until the sheet closes.

## KUPPA/Vayu responsibility impact
None. KUPPA presents trust state; Vayu cognition is untouched.

## API/event/schema/config/migration changes
None.

## Validation
CI #145 evidence: Maven compiled successfully; 78 tests ran; 77 passed; only `TrustedDevicesUiContractTest` failed on its over-broad negative assertion. A fresh full CI run is required for this repair before promotion.

## Metrics
Pre-existing tests passing in failed run: 77/77. New UI contract tests: 0/1 passing before repair, validation pending after repair. Repeated management-key prompts after non-current-device revoke: 1→0. Credential storage paths added: 0.

## Security/privacy/permission implications
The safety rule is tightened to the intended invariant: device bearer/continuity credentials and enrollment/management/signing secrets must not be read from inventory objects or rendered. The owner-management key remains memory-only and is cleared when the dialog closes or authentication fails.

## Failure/fallbacks tested
CI #145 demonstrated the regression gate. Wrong/missing management credentials remain bounded UI failures and do not mutate trust. Existing local continuity remains independent.

## Known limitations
Static shared-secret owner management and browser localStorage possession tokens remain interim mechanisms. This repair does not address passkeys/WebAuthn.

## Rollback
Return to `2c46d39716399206ca9d208626f3f57c8f6d0130` to remove the complete Trusted Devices UI evolution.

## Risks / technical debt
The UI still uses a browser prompt and transitional runtime asset injection.

## Dependencies
None added.

## Follow-up / next target
Obtain a green full Maven CI run, then record validation/baseline promotion. Next identity step remains stronger owner authentication and typed trust-history access.

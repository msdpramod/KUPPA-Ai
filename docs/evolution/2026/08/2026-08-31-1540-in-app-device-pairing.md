# 2026-08-31 15:40 — UI / Human Interaction — In-app owner-device pairing

## Cycle
UI / Human Interaction.

## Commit purpose and hypothesis
Replace KUPPA's credential browser prompts with explicit in-app pairing and management forms. Hypothesis: keeping trust actions inside the avatar-first experience will reduce accidental credential exposure and make device trust understandable without turning KUPPA into an admin dashboard or moving cognition away from Vayu.

## Architectural context
KUPPA remains the HEART and owns identity/trust presentation, relationship continuity, avatar/voice presence and interaction. Vayu remains the BRAIN and continues to own reasoning, planning, retrieval, orchestration, tool/agent selection and execution strategy. Preflight inspected governed head `11cabca8dbc9ff94dda5e5a37386fce82710f724`, the current known-good baseline `0f57af0525ea869a0fc853e51045f25ea2ab85a1` with CI #146 green, the KUPPA Constitution, the current evolution index and the latest Trusted Devices records. The prior Spring constructor regression is already repaired in the governed history; current `OwnerDeviceIdentityService` has an explicit `@Autowired` production constructor.

## Detailed changes
- Replace the existing `Trust this device` DOM node with a listener-clean clone and present `Pair this device` instead, preventing the old `window.prompt` enrollment handler from firing.
- Add a dedicated accessible pairing dialog with device label and password-style owner-enrollment-key input.
- Send the enrollment key only in the existing `X-KUPPA-Owner-Enroll-Key` request header, clear the input after every attempt, and never store the enrollment key.
- Persist only the already-existing issued device possession credential (`deviceId` + device token), then reload so the existing secure-continuity bootstrap obtains a signed continuity session.
- Replace the Trusted Devices owner-management `window.prompt` with an in-sheet password field.
- Hold the management key only in JavaScript memory while the sheet is open; clear both the variable and input on close/failure.
- Preserve metadata-only inventory, local Forget, global Revoke, remote-revocation confirmation, secure-continuity fallback and current browser events.
- Add a new `kuppa-device-paired` browser event containing only non-secret device metadata.

## Files/components affected
- `src/main/resources/static/trusted-devices.js`
- `src/main/resources/static/trusted-devices.css`
- `src/test/java/ai/kuppa/ui/TrustedDevicesUiContractTest.java`
- `docs/evolution/2026/08/2026-08-31-1540-in-app-device-pairing.md`
- `docs/evolution/README.md`
- `CHANGELOG.md`

## Behavior before
Owner enrollment and owner-management access both used native browser prompts. The enrollment secret was not persisted, but the UX was abrupt, context-poor and difficult to style or explain. Device management itself was already metadata-only and approval/cognition boundaries were intact.

## Behavior after
Pairing and management credentials are entered through KUPPA-owned dialogs with explicit labels, purpose text and bounded status feedback. No `window.prompt` remains in the trusted-device module. Enrollment/management secrets are still ephemeral; only the existing issued device possession credential remains in localStorage as before.

## KUPPA/Vayu responsibility impact
KUPPA gains only identity/trust interaction UX. Vayu cognition is unchanged. No semantic intent classification, reasoning, planning, retrieval, tool selection, agent orchestration or execution logic is added to KUPPA.

## API/event/schema/config/migration changes
No server API, schema, migration, environment variable or runtime dependency changes. Existing APIs remain:
- `POST /api/chat/owner/device`
- `GET /api/chat/owner/devices`
- `POST /api/chat/owner/devices/{deviceId}/revoke`

New browser event: `kuppa-device-paired` with non-secret `deviceId`, token version and expiry metadata.

## Tests/build/lint/smoke checks
Preflight governance and source inspection completed before mutation. `TrustedDevicesUiContractTest` now checks pairing UI presence, owner-enrollment API/header usage, explicit secret-input clearing, management-input clearing, absence of `window.prompt`, and absence of owner enrollment/management secret storage keys. Full Maven GitHub Actions CI is required before promotion to the governed branch.

## Relevant before/after metrics
- Native credential prompts in trusted-device module: 2 -> 0.
- In-app trust credential forms: 0 -> 2 (pairing + management unlock).
- Enrollment/management secrets persisted in browser storage: 0 -> 0.
- Metadata-only device inventory: preserved.
- Conversation windows added: 0.
- Vayu cognition changes: 0.
- Consequential-action approval changes: 0.
- New runtime dependencies/schema changes: 0.

## Security/privacy/permission implications
The owner enrollment and management credentials remain high-value secrets. They are not written to localStorage/sessionStorage and are cleared from form fields after use/close. The issued device token is still a browser-stored bearer credential; this evolution improves secret-entry UX but does not make the existing possession model phishing-resistant or hardware-bound. Consequential external actions remain separately approval gated.

## Known limitations
- Device possession credentials still live in localStorage and remain exposed to browser/XSS compromise.
- Owner authentication still depends on static enrollment/management shared secrets.
- This is not passkeys/WebAuthn, hardware attestation or OIDC-grade owner authentication.
- Pairing currently reloads the page after successful enrollment so the existing secure-continuity bootstrap can take over cleanly.
- Remote revoke still uses a native confirmation dialog; it carries no credential and is retained as a deliberate consequential trust-action confirmation.

## Failures/fallbacks tested
Contract coverage guards missing enrollment key, rejected/config-disabled enrollment, missing management key and credential non-persistence. Existing server-side tests continue to cover weak/incorrect management credentials, revocation boundaries and owner-device trust. Local unsigned continuity remains the graceful fallback when secure identity is unavailable.

## Rollback procedure / known-good reference
Return to governed head `11cabca8dbc9ff94dda5e5a37386fce82710f724` or validated runtime `0f57af0525ea869a0fc853e51045f25ea2ab85a1`. No database rollback is required.

## Risks / technical debt introduced or removed
Removes native browser credential prompts and makes secret lifetime clearer. The transitional runtime asset injection remains technical debt. The larger bearer-token/localStorage and static-shared-secret model remains and should not be treated as final security architecture.

## Dependencies
None added.

## Screenshots / visual references
No binary screenshot is added in this connector run. The pairing dialog reuses the existing Trusted Devices visual language and remains secondary to the avatar/conversation surface.

## Follow-up work
- Replace static owner authentication with passkeys/WebAuthn or another phishing-resistant owner identity mechanism.
- Move durable device possession away from general localStorage once a stronger browser-bound primitive exists.
- Add typed owner-authenticated trust history to the Trusted Devices sheet.
- Consider a no-reload secure-continuity handoff after pairing once the inline avatar script is modularized.

## Next evolution target
Heart: typed owner-authenticated trust-history API and stronger owner authentication. UI: passkey/WebAuthn pairing and credential-bound device presence without browser bearer-token storage.

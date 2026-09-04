# 2026-08-31 15:00 — UI / Human Interaction — Trusted Devices Sheet

## Purpose and hypothesis
Expose the existing owner-device management boundary through an avatar-first, metadata-only sheet. Hypothesis: making trust state visible and revocation understandable reduces accidental persistence of lost devices without turning KUPPA into a security-admin dashboard.

## Architectural context
KUPPA remains the HEART and owns identity, trust presentation, relationship continuity, avatar/voice interaction and permission boundaries. Vayu remains the BRAIN; no reasoning, planning, retrieval, provider routing, tool selection or orchestration moves into the UI.

## Changes
- Added `AvatarPageController` to serve the existing avatar page while injecting isolated trusted-device CSS/JS assets.
- Added an avatar-first `Trusted devices` trigger and modal sheet.
- The sheet consumes metadata-only `GET /api/chat/owner/devices` and owner-management `POST /api/chat/owner/devices/{deviceId}/revoke`.
- Clearly separates `Forget on this browser` from `Revoke everywhere`.
- Owner-management credentials are prompted only while opening the sheet, kept only in JS memory, cleared on close/failure, and never written to localStorage.
- Current-browser revocation also clears local possession/continuity state through the existing forget path.
- Added `kuppa-trusted-devices-loaded` and `kuppa-device-revoked` browser events.

## Files/components affected
`AvatarPageController`, `trusted-devices.css`, `trusted-devices.js`, `TrustedDevicesUiContractTest`, evolution index and changelog.

## Before / after
Before: owner device inventory/revocation existed only as APIs. After: avatar users can inspect device metadata, forget local trust, and remotely revoke an active device without exposing credentials.

## KUPPA/Vayu responsibility impact
KUPPA gains trust-management presentation only. Vayu responsibilities are unchanged.

## API/event/schema/config changes
No new API, schema, environment variable or runtime dependency. Browser events added: `kuppa-trusted-devices-loaded`, `kuppa-device-revoked`.

## Validation
Pre-publish inspection confirmed current branch CI #144 is green, the injected assets use only existing management APIs, and the management key has no browser-storage path. `TrustedDevicesUiContractTest` adds regression assertions for endpoint/header usage, wording separation and credential non-persistence. Full Maven CI is required before promotion.

## Metrics
Trusted-device UI surfaces 0→1; explicit local-vs-global trust actions 0→2; management credentials persisted in browser storage 0→0; conversation windows 0→0; Vayu cognition changes 0; approval-gate changes 0.

## Security/privacy/permissions
No credential values are rendered. No bearer/signing/enrollment tokens are returned by the existing inventory contract. Remote revocation remains gated by the owner-management secret. Consequential external actions remain on their existing approval path.

## Failure/fallback behavior
Missing/cancelled management credential leaves the avatar usable and shows a bounded message. 401/503 management failures do not mutate device trust. Local continuity remains available independently. Revoking the current browser clears local trust.

## Known limitations
The owner-management key is still a static shared secret entered through a browser prompt, and device possession tokens still live in localStorage. The page-injection controller is intentionally isolated but should be retired once the static page is modularized.

## Rollback
Return to `2c46d39716399206ca9d208626f3f57c8f6d0130`; no database rollback is required.

## Risks / technical debt
Runtime HTML injection is a transitional modularization technique. Passkeys/WebAuthn and a first-class component build should replace the prompt/shared-secret and injection pattern.

## Dependencies
None added.

## Follow-up / next evolution target
After CI validation, promote this runtime and update the known-good baseline. Next Heart target: typed owner-authenticated trust history and stronger owner authentication; next UI target: pairing/passkey UX without credential prompts.

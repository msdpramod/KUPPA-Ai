# Changelog

## 2026-08-31
### Changed
- Added an avatar-first Trusted Devices sheet backed by metadata-only owner-device inventory and remote-revocation APIs.
- Added explicit `Forget on this browser` versus `Revoke everywhere` actions.
- Repaired the new UI contract after CI #145 exposed an over-broad assertion; CI #146 is green.
- Replaced native browser prompts for owner-device enrollment and management access with in-app pairing/unlock forms.
- Added `Pair this device` with explicit device label, ephemeral enrollment-key handling, bounded pairing feedback, and a non-secret `kuppa-device-paired` browser event.
- Added durable owner-device trust audit events for enrollment, migration, continuity issuance and revocation.
- Promoted in-app device pairing after implementation `2e3f4c2575bba55af3fedec87db6b78253c309f9` passed full Maven CI #149.

### Safety
- Owner enrollment and management credentials are cleared from form fields after use and are never written to browser storage.
- Owner-management credentials remain held only in page memory while the Trusted Devices sheet is open.
- Inventory displays metadata only; bearer tokens, continuity credentials, enrollment keys, management keys and signing secrets are never rendered.
- The issued device possession token still lives in localStorage as an acknowledged interim limitation; this change does not claim passkey/WebAuthn-grade security.
- KUPPA remains the HEART and Vayu remains the BRAIN; cognition/orchestration is unchanged.
- Consequential external/high-impact action approval behavior is unchanged.
- CI #149 passed checkout, Java setup, full Maven `Test`, and cleanup for the pairing implementation.

## Earlier changes
See `docs/evolution/README.md`, the dated evolution records, and Git history for prior governed changes.

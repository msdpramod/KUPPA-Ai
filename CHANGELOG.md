# Changelog

## 2026-08-31
### Changed
- Added an avatar-first Trusted Devices sheet backed by metadata-only owner-device inventory and remote-revocation APIs.
- Added explicit `Forget on this browser` versus `Revoke everywhere` actions.
- Added isolated trusted-device CSS/JS assets without restoring a conversation window.
- Repaired the new UI contract after CI #145 exposed an over-broad assertion; CI #146 is green.
- Removed the unnecessary second owner-management-key prompt after revoking a non-current device.
- Added durable owner-device trust audit events for enrollment, migration, continuity issuance and revocation.

### Safety
- Owner-management credentials are held only in page memory while the sheet is open and are never written to browser storage.
- Inventory displays metadata only; bearer tokens, continuity credentials, enrollment keys, management keys and signing secrets are never rendered.
- KUPPA remains the HEART and Vayu remains the BRAIN; cognition/orchestration is unchanged.
- Consequential external/high-impact action approval behavior is unchanged.
- CI #145 was rejected; repaired runtime `0f57af05...` passed full Maven CI #146.

## Earlier changes
See `docs/evolution/README.md`, the dated evolution records, and Git history for prior governed changes.

# Changelog

## 2026-08-31
### Changed
- Added an avatar-first Trusted Devices sheet backed by the existing metadata-only owner-device inventory and remote-revocation APIs.
- Added explicit `Forget on this browser` versus `Revoke everywhere` actions so local possession-state cleanup is distinct from server-side device revocation.
- Added isolated trusted-device CSS/JS assets and a page-serving controller that injects them without restoring a conversation window.
- Added durable owner-device trust audit events for explicit enrollment, validated legacy migration, owner-continuity issuance, self-revocation, and owner-management remote revocation.
- Reused the existing `audit_events` table; no new schema or runtime dependency was added.

### Safety
- The owner-management key is held only in page memory while the sheet is open and is never written to browser storage.
- Device inventory displays metadata only; bearer tokens, continuity tokens, enrollment keys, management keys and signing secrets are never rendered.
- KUPPA remains the HEART and Vayu remains the BRAIN; reasoning, planning, retrieval, provider routing, tool/agent orchestration and execution strategy are unchanged.
- Consequential external/high-impact action approval behavior is unchanged.
- Promotion of the Trusted Devices UI requires a green full Maven CI run.

## Earlier changes
See `docs/evolution/README.md` and the Git history for prior governed evolution records.

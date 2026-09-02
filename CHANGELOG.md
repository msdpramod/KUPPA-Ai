# Changelog

## 2026-09-03
### Changed
- Added explicit conversational memory forgetting for `forget that ...` and `please forget that ...` owner instructions.
- Forgetting is exact-match only after case/whitespace/terminal-punctuation normalization; partial and fuzzy matches are intentionally not deleted.
- Forgotten memories are deactivated through the existing reviewed/inactive memory path so stale personal context stops being presented as active memory.

### Safety
- No Vayu reasoning, planning, retrieval, tool/agent orchestration or execution behavior changed.
- No consequential-action approval behavior changed.
- No new database schema, runtime dependency, secret, or configuration was introduced.
- No fuzzy/semantic bulk deletion path was added.

## 2026-09-02
### Changed
- Added an owner-authenticated typed trust-history API at `GET /api/chat/owner/trust-history`.
- Trust history is restricted to KUPPA owner-device lifecycle events, supports optional device filtering, and caps responses at 100 items.
- The new contract returns typed metadata only and does not expose raw audit details or credentials.
- Added a compact `Trust activity` view inside the avatar-first Trusted Devices sheet, with all-device/per-device filtering and bounded refresh using the existing typed trust-history contract.
- Trust activity reuses the existing in-memory owner-management credential and refreshes after remote revocation without introducing another credential prompt or page reload.

### Safety
- Reuses the existing owner-management authentication boundary and fails closed when disabled or unauthorized.
- Trust activity renders typed event metadata only; it does not consume generic audit detail or persist owner-management credentials.
- No Vayu reasoning, planning, retrieval, tool/agent orchestration or execution behavior changed.
- No consequential-action approval behavior changed.
- No new database schema, runtime dependency, secret, or configuration was introduced.

## 2026-09-01
### Changed
- Removed the full-page reload after successful owner-device pairing.
- Pairing now activates the existing signed owner continuity session and restores resumable metadata in-place, preserving avatar and conversation presence.
- Added bounded UI failure handling when secure continuity cannot be activated; KUPPA does not claim trusted continuity unless issuance succeeds.
- Added non-secret `kuppa-device-pairing-complete` observability after successful in-place activation.

### Safety
- No Vayu reasoning, planning, retrieval, tool/agent orchestration or execution behavior changed.
- No consequential-action approval behavior changed.
- Owner enrollment secret handling and device-token storage behavior are unchanged; the localStorage bearer-token limitation remains explicit.

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

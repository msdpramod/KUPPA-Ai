# 2026-09-02 15:00 — Trusted-device trust activity

## Date/time and cycle
2026-09-02 15:00 Asia/Kolkata — Body / UI / Human Interaction cycle.

## Commit purpose and hypothesis
Surface the already-governed typed owner trust-history contract inside the avatar-first Trusted Devices sheet. Hypothesis: KUPPA can make relationship/device trust history understandable without exposing generic audit detail, persisting owner-management credentials, displacing the avatar, or adding cognition to the HEART.

## Architectural context
KUPPA remains the HEART for identity, trust, relationship continuity, avatar/voice presence and interaction. Vayu remains the BRAIN for reasoning, planning, retrieval, orchestration, tools, specialist agents and execution strategy. This change consumes a KUPPA trust API only; it does not alter the Vayu gateway or consequential-action approval flow.

## Detailed changes
- Added `Devices` and `Trust activity` views within the existing Trusted Devices sheet.
- Added bounded trust-history loading from `GET /api/chat/owner/trust-history?limit=30` using the same in-memory owner-management credential already used for device inventory.
- Added optional per-device filtering based on metadata already returned by the trusted-device inventory.
- Rendered only typed fields: event type, device ID, actor, reason and timestamp.
- Added human-readable labels for the five trust lifecycle event types.
- Added refresh behavior and non-secret `kuppa-trust-history-loaded` observability.
- Refreshes trust activity after a remote revoke so the sheet reflects the lifecycle change without a page reload.
- Added responsive/accessibility treatment for tabs, filters and focus states.

## Files/components affected
- `src/main/resources/static/trusted-devices.js`
- `src/main/resources/static/trusted-devices.css`
- `src/test/java/ai/kuppa/ui/TrustedDevicesUiContractTest.java`
- `docs/evolution/2026/09/2026-09-02-1500-trusted-device-trust-activity.md`
- `docs/evolution/README.md`
- `CHANGELOG.md`

## Behavior before
Trusted Devices could list device metadata, pair the current browser, forget local trust and revoke a device globally, but owner trust lifecycle events were not visible in the UI.

## Behavior after
After the owner unlocks Trusted Devices, the same ephemeral management session can view a compact trust timeline for all devices or one selected device. No separate credential is requested or stored. The avatar remains the primary experience and no conversation window is introduced.

## KUPPA/Vayu responsibility impact
KUPPA gains a human-facing trust-history presentation only. Vayu responsibilities and brain-level cognition are unchanged.

## API/event/schema/config/migration changes
No backend API, schema, config or migration change. UI now consumes the existing typed `GET /api/chat/owner/trust-history` endpoint. New browser event: `kuppa-trust-history-loaded` with non-secret count/device-filter metadata.

## Tests/build/lint/smoke checks
A UI contract test was added to require typed trust-history usage, bounded `limit=30`, reuse of `X-KUPPA-Owner-Management-Key`, absence of generic `/api/audit` access, absence of raw `event.detail`, and absence of management-key persistence. Full GitHub Actions validation is required before promotion; this branch commit must not be treated as known-good until CI is green.

## Relevant before/after metrics
| Metric | Before | After candidate |
|---|---:|---:|
| Trust-history UI views | 0 | 1 |
| Trust-history result cap requested by UI | N/A | 30 |
| Device-filter controls | 0 | 1 |
| Extra owner credential prompts | 0 | 0 |
| Management credential persistence | 0 | 0 |
| Generic audit UI access | 0 | 0 |
| Conversation windows added | 0 | 0 |
| Vayu cognition changes | 0 | 0 |
| Approval-gate changes | 0 | 0 |

## Security/privacy/permission implications
The UI reuses the existing owner-management authentication boundary and keeps that credential only in page memory while the sheet is open. It displays typed metadata only and does not render device bearer tokens, continuity tokens, enrollment secrets, management secrets, signing secrets or raw audit detail.

## Known limitations
- Owner management still relies on a static shared secret rather than passkeys/WebAuthn/OIDC-grade identity.
- Device possession tokens remain in browser localStorage under the existing interim design.
- The trust ledger is single-owner and not tamper-evident.
- Device IDs are visible in the owner-only trust sheet because they are necessary for filtering/correlation.

## Failures/fallbacks tested
The contract test guards against accidental generic-audit/raw-detail consumption. Runtime error handling keeps the sheet usable when trust history is unavailable and renders a bounded error instead of fabricating events. Device inventory/auth failure continues to clear the in-memory management key and disables trust-history controls.

## Rollback procedure / known-good reference
Before promotion, rollback is simply to discard this branch. Governed known-good head remains `0769d1ebad7550c21454f279dc0a9b2554bfad9d`, with validated runtime `79b8fa367affc86fa4f63b31244436cf2f7f6628` (CI #155).

## Risks/technical debt introduced or removed
Removes the UI gap between typed trust observability and the owner-facing device experience. It does not solve the static-shared-secret or localStorage bearer-token debt. The trust module remains classic-script based and should later move behind an explicit adapter/module boundary.

## Dependencies
No new dependencies.

## Screenshots / visual references
No screenshot artifact is committed in this candidate; the change extends the existing Trusted Devices sheet rather than changing the avatar layout.

## Follow-up work
If CI is green, promote the implementation and update the known-good baseline. Future UI work should modularize continuity/trust bindings. Future Heart work should prioritize stronger owner identity instead of extending shared-secret authentication.

## Next evolution target
Explicit KUPPA continuity/trust adapter on the UI side; passkeys/WebAuthn/OIDC-grade owner identity on the Heart side.

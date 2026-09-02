# 2026-09-02 15:10 — Trusted-device trust activity validation

## Cycle
Body / UI / Human Interaction validation closeout.

## Why this documentation-only commit exists
Runtime implementation `0006222796a71b1ae0bea070d68d0c8c952b4611` was intentionally held on the evolution branch until CI completed. This commit records the actual validation evidence, promotes the known-good baseline, and updates the evolution index. No runtime source changes are made here.

## Commit purpose and hypothesis result
The UI hypothesis held: KUPPA can surface owner-device trust lifecycle history inside the existing avatar-first Trusted Devices sheet without exposing generic audit detail, persisting management credentials, adding a second credential prompt, reloading the page, or moving cognition out of Vayu.

## Architectural context
KUPPA remains the HEART for identity, trust, relationship continuity, avatar/voice presence and interaction. Vayu remains the BRAIN for reasoning, planning, retrieval, orchestration, tools, specialist agents and execution strategy. The Constitution is unchanged.

## Detailed validated behavior
- `Devices` and `Trust activity` coexist inside the existing Trusted Devices sheet.
- Trust activity consumes only `GET /api/chat/owner/trust-history` and requests `limit=30`.
- All-device and per-device views are available from metadata already loaded by the device inventory.
- Only event type, device ID, actor, reason and timestamp are rendered.
- The existing page-memory-only management key is reused for both inventory and history.
- Remote revocation refreshes both inventory and trust activity.
- Bounded UI errors are shown if history cannot load; KUPPA does not fabricate events.

## Files/components affected by this closeout
- `docs/evolution/BASELINE.md`
- `docs/evolution/README.md`
- `docs/evolution/2026/09/2026-09-02-1510-trusted-device-trust-activity-validation.md`

## Behavior before
Typed trust history existed as a backend contract but was not visible in the owner-facing UI.

## Behavior after
The owner can inspect a compact trust timeline without leaving the avatar-first Trusted Devices experience or exposing raw audit records.

## KUPPA/Vayu responsibility impact
KUPPA gains trust observability presentation only. Vayu responsibilities remain untouched.

## API/event/schema/config/migration changes
No new backend API, schema, config or migration. The validated UI consumes the existing typed trust-history API and emits non-secret `kuppa-trust-history-loaded` metadata.

## Tests/build/lint/smoke checks run with results
GitHub Actions CI #158 completed successfully for `0006222796a71b1ae0bea070d68d0c8c952b4611`. Checkout, Java setup, full Maven `Test`, cleanup and workflow completion all passed. The UI contract specifically requires typed trust-history usage, `limit=30`, reuse of the owner-management header, no `/api/audit` access, no raw `event.detail`, and no management-key persistence.

## Relevant before/after metrics
| Metric | Before | After validated runtime |
|---|---:|---:|
| Trust-history UI views | 0 | 1 |
| Trust-history result cap requested by UI | N/A | 30 |
| Per-device history filter | 0 | 1 |
| Extra owner credential prompts | 0 | 0 |
| Management credential persistence | 0 | 0 |
| Generic audit UI access | 0 | 0 |
| Conversation windows added | 0 | 0 |
| Vayu cognition changes | 0 | 0 |
| Approval-gate changes | 0 | 0 |
| CI state | pending | CI #158 green |

## Security/privacy/permission implications
No secrets are introduced. Owner-management authentication remains fail-closed and page-memory-only in the UI. Device IDs and typed lifecycle metadata are visible only after owner management unlock. Bearer tokens, continuity tokens, enrollment keys, management keys, signing secrets and raw audit detail are not rendered.

## Known limitations
- Static shared-secret owner authentication remains the largest identity weakness.
- Browser device possession credentials remain in localStorage under the existing interim model.
- Audit persistence is single-owner and not tamper-evident.
- The trust/continuity UI remains classic-script based rather than an explicit module boundary.

## Failures/fallbacks tested
The contract test protects against generic/raw audit consumption and credential persistence. Existing UI error handling leaves the Trusted Devices sheet usable when trust history fails and does not claim successful history load. Existing management-auth failure clears the in-memory key and disables trust-history controls.

## Rollback procedure / known-good reference
Rollback to governed head `0769d1ebad7550c21454f279dc0a9b2554bfad9d`, restoring runtime `79b8fa367affc86fa4f63b31244436cf2f7f6628` validated by CI #155. No database rollback is required.

## Risks/technical debt introduced or removed
Removed the gap between typed trust observability and owner-facing device management. No new authentication strength is claimed. Remaining debt is stronger owner identity, owner-scoped/tamper-evident audit persistence, modular continuity/trust UI boundaries, and browser bearer-token storage.

## Dependencies
No new dependencies.

## Screenshots / visual references
No screenshot artifact was required for validation; the change is contained within the existing Trusted Devices sheet.

## Follow-up work
Modularize continuity/trust browser bindings and prioritize passkeys/WebAuthn/OIDC-grade owner identity rather than extending static shared-secret authentication.

## Next evolution target
UI: explicit KUPPA continuity/trust adapter. Heart: stronger owner identity and owner-scoped trust persistence.

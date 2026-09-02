# KUPPA Known-Good Baseline

## Runtime baseline
- **Current validated implementation:** `0006222796a71b1ae0bea070d68d0c8c952b4611` (avatar-first typed trust activity inside Trusted Devices), validated by GitHub Actions CI #158.
- **Previous validated implementation:** `79b8fa367affc86fa4f63b31244436cf2f7f6628` (owner-authenticated typed/filtered trust-history contract), validated by CI #155.
- **Pre-change governed branch head:** `0769d1ebad7550c21454f279dc0a9b2554bfad9d`.

## Current evidence
- Trusted Devices now exposes a compact `Trust activity` view backed only by `GET /api/chat/owner/trust-history`.
- The UI requests at most 30 typed events and supports all-device or per-device filtering.
- The same ephemeral `X-KUPPA-Owner-Management-Key` session is reused; no extra credential prompt or browser persistence is introduced.
- Raw generic audit access is not used and raw `event.detail` is not rendered.
- Remote revocation refreshes trust activity without a page reload.
- No backend API, schema, dependency, secret/configuration, Vayu cognition, voice/avatar engine, memory or approval-gate changes were introduced.
- CI #158 passed checkout, Java setup, full Maven `Test`, cleanup and completion for implementation `00062227...`.

## Scorecard baseline
| Dimension | Evidence status |
|---|---|
| Build stability | Green on CI #158 |
| Conversation quality | Unchanged; no conversation-generation path modified |
| Personality consistency | Unchanged |
| Memory accuracy | Existing confidence-aware memory behavior/tests unchanged |
| Vayu handoff reliability/latency | Vayu Gateway v3 unchanged |
| Errors | Trust-history load failures render bounded UI errors; no fabricated events |
| Voice reliability | Unchanged |
| UI responsiveness | No page reload added; trust activity is bounded to 30 requested events |
| Accessibility | Tab selection exposes `aria-selected`; filter remains keyboard/focus accessible |
| Resource usage | One bounded read on activity view/refresh; no new dependency or schema work |
| Security boundary | Typed owner-only metadata; management key remains page-memory-only; no generic audit/raw detail rendering |

## Rollback policy
Return to governed head `0769d1ebad7550c21454f279dc0a9b2554bfad9d` to remove this UI evolution, restoring validated runtime `79b8fa367affc86fa4f63b31244436cf2f7f6628` (CI #155). No destructive schema rollback is required. Normal evolution must preserve the Constitution, HEART/BRAIN boundary and approval gates.

## Next identified gaps
- Replace static owner enrollment/management shared-secret authentication with passkeys/WebAuthn/OIDC-grade authentication.
- Add explicit owner identity to trust/audit persistence before any multi-owner architecture.
- Consider tamper-evident integrity verification for high-value trust-management audit events.
- Replace classic-script global continuity/trust bindings with an explicit KUPPA adapter/module.
- Move durable device possession credentials away from general browser localStorage when a stronger credential primitive is introduced.
- Retire unsigned local continuity only after secure owner identity is universally configured and migration-safe.

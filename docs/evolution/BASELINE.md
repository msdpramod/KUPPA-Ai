# KUPPA Known-Good Baseline

## Runtime baseline
- **Current validated implementation:** `79b8fa367affc86fa4f63b31244436cf2f7f6628` (owner-authenticated typed/filtered trust-history contract), validated by GitHub Actions CI #155.
- **Previous validated implementation:** `b781e4bd00233dbf7d16a5d34ea686649c330451` (in-place signed continuity activation after owner-device pairing), validated by CI #152.
- **Pre-change governed branch head:** `c0794f918d872acecedb035d4c75dd270f56fd71`.

## Current evidence
- `GET /api/chat/owner/trust-history` exposes only the five allow-listed owner-device trust lifecycle event types.
- Optional `deviceId` filtering is repository-scoped; the new service does not fall back to generic all-audit access.
- Response items contain only event type, device ID, bounded actor/reason metadata and timestamp; raw audit detail is not returned.
- Result count is bounded to 1..100 with a default of 50.
- Owner-management authentication remains fail-closed when disabled or rejected.
- No schema, dependency, secret/configuration, Vayu cognition, voice/avatar, memory or approval-gate changes were introduced.
- CI #155 passed checkout, Java setup, full Maven `Test`, cleanup and completion for implementation `79b8fa36...`.

## Scorecard baseline
| Dimension | Evidence status |
|---|---|
| Build stability | Green on CI #155 |
| Conversation quality | Unchanged; no conversation-generation path modified |
| Personality consistency | Unchanged |
| Memory accuracy | Existing confidence-aware memory behavior/tests unchanged |
| Vayu handoff reliability/latency | Vayu Gateway v3 unchanged |
| Errors | Trust-history access fails closed through existing owner-management auth; bounded response path |
| Voice reliability | Unchanged |
| UI responsiveness | Unchanged; no UI code modified |
| Accessibility | Unchanged |
| Resource usage | Read-only repository query + bounded mapping; no new dependency or schema work |
| Security boundary | Generic audit detail is not exposed; trust events are allow-listed and owner-management authenticated |

## Rollback policy
Return to governed head `c0794f918d872acecedb035d4c75dd270f56fd71` to remove this Heart evolution, restoring validated runtime `b781e4bd00233dbf7d16a5d34ea686649c330451` (CI #152). No destructive schema rollback is required. Normal evolution must preserve the Constitution, HEART/BRAIN boundary and approval gates.

## Next identified gaps
- Replace static owner enrollment/management shared-secret authentication with passkeys/WebAuthn/OIDC-grade authentication.
- Add explicit owner identity to trust/audit persistence before any multi-owner architecture.
- Consider tamper-evident integrity verification for high-value trust-management audit events.
- Surface typed trust history in the avatar-first Trusted Devices experience without exposing credentials.
- Replace classic-script global continuity bindings with an explicit KUPPA continuity adapter/module.
- Move durable device possession credentials away from general browser localStorage when a stronger credential primitive is introduced.
- Retire unsigned local continuity only after secure owner identity is universally configured and migration-safe.

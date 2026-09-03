# KUPPA Known-Good Baseline

## Runtime baseline
- **Current validated implementation:** `b96bf1f08da4d3c0935b93a36b7a647d2db7951d` (explicit KUPPA continuity adapter for Trusted Devices), validated by GitHub Actions CI #165 through repair head `8efd0be0283f29368c5605c5c4a5782d59914e2b`.
- **Previous validated implementation:** `da8c13b42011360eb63ce30dd14fa0abf1e414a1` (explicit exact-match owner memory forgetting), validated by CI #161.
- **Pre-change governed branch head:** `d281067dab9759826bfa1ce0d225bf0730f87570`.

## Current evidence
- Trusted Devices now depends on a frozen, versioned `KuppaContinuityAdapter v1` surface instead of directly calling three avatar-page continuity globals.
- Direct Trusted Devices references to `window.issueOwnerContinuity`, `window.restoreContinuity`, and `window.forgetOwnerDevice` are removed.
- Adapter-unavailable pairing fails clearly; local device forgetting retains bounded storage cleanup/reload fallback.
- Existing signed continuity, no-reload successful pairing, trust-history UI, owner-device management, avatar state engine, voice barge-in and approval behavior remain intact.
- No backend API, database schema, runtime dependency, secret/configuration, Vayu cognition, or approval-gate change was introduced.
- CI #164 compiled and ran 86 tests but caught one new source-contract assertion mismatch; it was not promoted.
- CI #165 passed the full Maven test workflow after the assertion was aligned with the intentional optional fallback.

## Scorecard baseline
| Dimension | Evidence status |
|---|---|
| Build stability | Green on CI #165 after CI #164 caught one test-contract mismatch |
| Conversation quality | Unchanged; continuity transport behavior preserved |
| Personality consistency | Unchanged |
| Memory accuracy | Unchanged from validated exact-forget baseline |
| Vayu handoff reliability/latency | Vayu Gateway v3 unchanged; adapter is KUPPA UI-side continuity only |
| Errors | Improved isolation: missing adapter fails pairing explicitly; local forget retains fallback |
| Voice reliability | Unchanged |
| UI responsiveness | Successful pairing remains in-place with no page reload |
| Accessibility | Unchanged; existing Trusted Devices semantics preserved |
| Resource usage | Negligible one-time adapter object; no dependency or polling added |
| Security boundary | Secret storage/auth unchanged; adapter events contain no credentials; approval gates unchanged |

## Rollback policy
Return to governed head `d281067dab9759826bfa1ce0d225bf0730f87570` to remove this UI evolution, restoring validated runtime `da8c13b42011360eb63ce30dd14fa0abf1e414a1` (CI #161). No destructive schema rollback is required. Normal evolution must preserve the Constitution, HEART/BRAIN boundary and approval gates.

## Next identified gaps
- Move the continuity implementation itself behind the adapter contract rather than bridging to legacy inline functions.
- Add browser-level smoke coverage for pairing/continuity/fallback behavior when practical.
- Add owner-visible observability for memory forget/correction operations without exposing private content unnecessarily.
- Add a safe conversational disambiguation flow for near-match memories; never autonomously erase ambiguous candidates.
- Replace static owner enrollment/management shared-secret authentication with passkeys/WebAuthn/OIDC-grade authentication.
- Add explicit owner identity to trust/audit persistence before any multi-owner architecture.
- Move durable device possession credentials away from general browser localStorage when a stronger credential primitive is introduced.

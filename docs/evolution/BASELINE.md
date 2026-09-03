# KUPPA Known-Good Baseline

## Runtime baseline
- **Current validated implementation:** `bae44bab17dc9402fc4abcf195165a51398d82e4` (privacy-safe memory change observability), validated by GitHub Actions CI #168.
- **Previous validated implementation:** `b96bf1f08da4d3c0935b93a36b7a647d2db7951d` (explicit KUPPA continuity adapter), validated by CI #165 through repair head `8efd0be0283f29368c5605c5c4a5782d59914e2b`.
- **Pre-change governed branch head:** `5f7ceab61c8cafa900c0859f86fe9b24ae951f69`.

## Current evidence
- Explicit owner forget requests now return typed internal mutation outcomes: `FORGOTTEN` or `FORGET_NO_MATCH`.
- Successful forget audit records contain affected count and memory categories only; requested/stored private memory text is not copied into the event detail.
- `MEMORY_CAPTURED` audit detail no longer repeats memory content; it records category, confidence and source.
- Exact-match-only forgetting remains unchanged; partial/near matches are not deleted and perform no repository save.
- Existing Vayu Gateway v3, signed continuity, Trusted Devices, avatar state engine, voice barge-in and consequential-action approval behavior remain intact.
- No backend HTTP API, schema, runtime dependency, secret/configuration, Vayu cognition or approval-gate change was introduced.
- CI #168 passed the full Maven test workflow.

## Scorecard baseline
| Dimension | Evidence status |
|---|---|
| Build stability | Green on CI #168 |
| Conversation quality | Improved transparency for explicit memory-forget intent; response generation path otherwise unchanged |
| Personality consistency | Improved: stale memory withdrawal remains exact and bounded |
| Memory accuracy | Improved observability; successful exact forget vs no-match are distinguishable without fuzzy deletion |
| Vayu handoff reliability/latency | Unchanged; Vayu Gateway v3 untouched |
| Errors | Improved diagnosability for no-match forget requests |
| Voice reliability | Unchanged |
| UI responsiveness | Unchanged |
| Accessibility | Unchanged |
| Resource usage | Negligible in-memory outcome object; no polling/dependency added |
| Security boundary | Improved audit privacy; no raw memory text in new capture/forget audit detail; approvals unchanged |

## Rollback policy
Return to governed head `5f7ceab61c8cafa900c0859f86fe9b24ae951f69` to remove this Heart evolution, restoring validated runtime `b96bf1f08da4d3c0935b93a36b7a647d2db7951d` / repair head `8efd0be...` from CI #165. No destructive schema rollback is required. Normal evolution must preserve the Constitution, HEART/BRAIN boundary and approval gates.

## Next identified gaps
- Add an owner-authenticated typed memory-change history contract instead of relying on the generic developer audit surface.
- Add a safe conversational disambiguation flow for near-match memories; never autonomously erase ambiguous candidates.
- Move the continuity implementation itself behind the adapter contract rather than bridging to legacy inline functions.
- Add browser-level smoke coverage for pairing/continuity/fallback behavior when practical.
- Replace static owner enrollment/management shared-secret authentication with passkeys/WebAuthn/OIDC-grade authentication.
- Add explicit owner identity to trust/audit persistence before any multi-owner architecture.
- Move durable device possession credentials away from general browser localStorage when a stronger credential primitive is introduced.

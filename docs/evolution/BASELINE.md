# KUPPA Known-Good Baseline

## Runtime baseline
- **Current validated implementation:** `6be4e77272a3e43ce0f64ba6f8c7f7b2d634dfdd` (presence and latency perception), validated by GitHub Actions CI #171.
- **Previous validated implementation:** `bae44bab17dc9402fc4abcf195165a51398d82e4` (privacy-safe memory change observability), validated by CI #168.
- **Pre-change governed branch head:** `e284a05dd5934eb1996bf8a63fc93354a654a9b3`, green on CI #170.

## Current evidence
- `KuppaPresenceController v1` consumes existing KUPPA state and Vayu-presence events for presentation only.
- Processing states expose accessible `aria-busy` semantics on the avatar stage.
- Vayu pending state gets elapsed wait feedback; after four seconds the UI says `still working` without fabricating an internal result or reasoning progress.
- Listening/thinking/speaking gain state-reactive mic-ring presence cues.
- `prefers-reduced-motion` disables the new pulse/transition motion while keeping static state cues.
- Contract coverage explicitly forbids presence-controller `fetch(`, `/api/chat`, or `VayuBrainGateway` dependencies.
- Existing Vayu Gateway v3, signed continuity, Trusted Devices, avatar state engine, voice barge-in, personality/memory behavior, and consequential-action approval behavior remain intact.
- No backend HTTP API, database schema, runtime dependency, secret/configuration, Vayu cognition, or approval-gate change was introduced.
- CI #171 passed the repository workflow.

## Scorecard baseline
| Dimension | Evidence status |
|---|---|
| Build stability | Green on CI #171; pre-change merge green on CI #170 |
| Conversation quality | Unchanged semantics; improved perceived responsiveness while waiting for Vayu |
| Personality consistency | Unchanged |
| Memory accuracy | Unchanged; privacy-safe memory observability from previous baseline retained |
| Vayu handoff reliability/latency | Reliability unchanged; latency perception improved through factual elapsed pending duration only |
| Errors | Existing fallback/offline/cancelled states remain authoritative; presence timer clears on every non-pending brain state |
| Voice reliability | Unchanged; speaking/listening presentation cues improved |
| UI responsiveness | Improved state feedback during listening/thinking/speaking and long pending waits |
| Accessibility | Improved via `aria-busy` and reduced-motion handling for new presence animation |
| Resource usage | One 250 ms browser timer only while Vayu is pending; no polling/network/dependency added |
| Security boundary | Unchanged; presence controller has no network, credential, memory, approval, or cognition path |

## Rollback policy
Return to governed head `e284a05dd5934eb1996bf8a63fc93354a654a9b3` to remove this UI evolution, restoring validated runtime `bae44bab17dc9402fc4abcf195165a51398d82e4` from CI #168. No destructive schema rollback is required. Normal evolution must preserve the Constitution, HEART/BRAIN boundary and approval gates.

## Next identified gaps
- Add an owner-authenticated typed memory-change history contract instead of relying on the generic developer audit surface.
- Add a safe conversational disambiguation flow for near-match memories; never autonomously erase ambiguous candidates.
- Move the continuity implementation itself behind the adapter contract rather than bridging to legacy inline functions.
- Extend reduced-motion handling to bounded Three.js avatar movement and add browser-level smoke coverage when practical.
- Replace static owner enrollment/management shared-secret authentication with passkeys/WebAuthn/OIDC-grade authentication.
- Add explicit owner identity to trust/audit persistence before any multi-owner architecture.
- Move durable device possession credentials away from general browser localStorage when a stronger credential primitive is introduced.

# KUPPA Known-Good Baseline

## Runtime baseline
- **Current validated implementation:** `d938200ea9a70a2cb55b71830663d6decc7a4a5e` (avatar owner-device authorized signed continuity with local fallback), promoted through governed merge `6cafbaa9d7360654895756016833c3db620a4029`.
- **CI:** GitHub Actions CI run #125 completed successfully, including the full Maven Test step.
- **Previous validated runtime:** `b88adffb3bd44985bb38feb40c868050aaba70bf` (device signing-key separation and rotation; CI #121 green).
- **Previous UI runtime:** `33ad4d0b1c76bf7886f33d165b5fee1a4da989b3` (CI #113 green; session-scoped continuity recovery).

## Current evidence
- The avatar can explicitly enroll/trust the current owner device through the existing owner-device API without persisting the owner enrollment key.
- A valid owner-device credential can obtain a server-issued signed continuity session and restore resumable-turn metadata through the secure endpoint.
- Expired secure continuity can be renewed once from a still-valid device credential; invalid device credentials clear trusted state and fall back locally.
- The avatar visibly distinguishes `Continuity · trusted device` from `Continuity · local` and provides explicit Trust/Forget controls.
- Legacy browser-local continuity remains available when owner identity or secure continuity is unavailable.
- No conversation window, semantic classifier, database schema, runtime dependency, Vayu cognition authority, or approval-gate change was introduced.
- VayuBrainGateway v3, correlation/cancellation, persisted parent restoration, confidence-aware memory, avatar state engine, voice barge-in, degraded brain presence, owner-device v1/v2 credential validation, and device signing-key rotation remain intact.

## Scorecard baseline
| Dimension | Evidence status |
|---|---|
| Build stability | Green on CI run #125 |
| Conversation quality | Explicit Continue/Correct/New topic plus refresh recovery preserved; secure continuity transport added |
| Personality consistency | Unchanged |
| Memory accuracy | Existing automated tests; unchanged |
| Vayu handoff reliability/latency | v3 correlation/cancellation/persisted parent lookup unchanged |
| Errors | Secure continuity renewal/local fallback plus prior identity/brain fallbacks preserved |
| Voice reliability | Existing playback cancellation/barge-in unchanged |
| UI responsiveness | Avatar-first layout preserved; small trust status/control surface added |
| Accessibility | Existing live status semantics preserved; continuity trust status uses polite live status |
| Resource usage | No new runtime dependency/database schema; only bounded extra identity/session HTTP requests |
| Security boundary | Avatar can use owner-device-authorized signed continuity; enrollment key is not stored; device/continuity tokens remain browser possession credentials |

## Rollback policy
For the secure-continuity UI migration, return to `b88adffb3bd44985bb38feb40c868050aaba70bf` (CI #121 green) or governed pre-UI head `5c209d46a2120424744c16d05da321b608859944`. The change is UI-only and schema-free, so rollback requires no database migration. Normal evolution must preserve the KUPPA Constitution and approval gates.

## Next identified gaps
- Persistent per-device revocation and auditable renewal/re-enrollment semantics.
- Replace the minimal browser enrollment prompt with a safer pairing flow.
- Move durable device possession credentials away from general browser local storage when a stronger credential primitive is introduced.
- Hardware/passkey/OIDC-grade identity remains intentionally out of scope for the current possession-token model.
- Retire unsigned local continuity only after secure owner identity is universally configured and migration-safe.

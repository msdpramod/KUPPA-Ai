# KUPPA Known-Good Baseline

## Runtime baseline
- **Current validated implementation:** `ac082dce5d68c6908f5c843fded23df11204ce83` (typed owner memory-change history), validated by GitHub Actions CI #180.
- **Previous validated implementation:** `6be4e77272a3e43ce0f64ba6f8c7f7b2d634dfdd` (presence and latency perception), validated by CI #171.
- **Pre-change governed branch head:** `7e47b2e5b527814782d097f8906a4bcaa50c9643`, green on merge CI #173.

## Current evidence
- Owner memory-change history is available through `GET /api/chat/owner/memory-history` behind the existing owner-management credential boundary.
- The contract allow-lists only `MEMORY_CAPTURED`, `MEMORY_FORGOTTEN`, and `MEMORY_FORGET_NO_MATCH`.
- Typed output exposes bounded category/confidence/source/count/category-list/timestamp metadata only.
- Raw personal-memory text, raw audit detail, internal memory IDs, correlation IDs, and generic audit listing are not exposed by the new contract.
- Requested limits clamp to 1..100; malformed legacy metadata degrades to null/empty typed fields.
- Existing exact-match-only conversational forgetting remains unchanged; ambiguous/partial wording still does not delete memory.
- Existing Vayu Gateway v3, signed continuity, Trusted Devices, avatar state engine, presence controller, voice barge-in, and consequential-action approval behavior remain intact.
- No database schema, runtime dependency, new secret/configuration, Vayu cognition, UI, or approval-gate change was introduced.
- CI #180 passed the repository workflow.

## Scorecard baseline
| Dimension | Evidence status |
|---|---|
| Build stability | Green on implementation CI #180; pre-change governed head green on CI #173 |
| Conversation quality | Unchanged generation semantics; memory behavior is more transparently observable |
| Personality consistency | Unchanged rules; safer owner visibility into memory lifecycle supports correction continuity |
| Memory accuracy | Improved observability without changing capture/forget semantics; exact-match-only deletion retained |
| Vayu handoff reliability/latency | Unchanged |
| Errors | Malformed history metadata degrades to bounded null/empty fields; no raw-detail fallback |
| Voice reliability | Unchanged |
| UI responsiveness | Unchanged |
| Accessibility | Unchanged |
| Resource usage | One bounded repository read per authenticated history request; response max 100; no polling/dependency added |
| Security boundary | Improved privacy surface: typed allow-list, no raw memory text/detail/internal IDs; auth strength itself unchanged |

## Rollback policy
Return to governed head `7e47b2e5b527814782d097f8906a4bcaa50c9643` to remove this Heart evolution, restoring validated runtime `6be4e77272a3e43ce0f64ba6f8c7f7b2d634dfdd`. No destructive schema rollback is required. Normal evolution must preserve the Constitution, HEART/BRAIN boundary and approval gates.

## Next identified gaps
- Add safe conversational near-match memory disambiguation that presents candidates and requires explicit owner selection before deletion.
- Move owner authentication away from static shared secrets toward passkeys/WebAuthn or OIDC-grade identity.
- Move the continuity implementation itself behind `KuppaContinuityAdapter` rather than bridging legacy inline functions.
- Extend reduced-motion handling to bounded Three.js avatar movement and add browser-level smoke coverage when practical.
- Add explicit owner identity to trust/audit persistence before any multi-owner architecture.
- Move durable device possession credentials away from general browser localStorage when a stronger credential primitive is introduced.

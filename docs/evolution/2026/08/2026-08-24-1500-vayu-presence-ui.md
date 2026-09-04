# KUPPA Evolution Record — Vayu Presence UI

- **Date/time:** 2026-08-24 15:00 Asia/Kolkata
- **Cycle:** Body / UI / Human Interaction
- **Commit purpose:** Make KUPPA's avatar visibly and accessibly reflect the health of its delegated Vayu brain handoff without adding a conversation window or moving cognition into KUPPA.
- **Hypothesis:** Showing healthy, fallback, and unavailable Vayu states through concise presence cues will make latency/failure behavior understandable while preserving one coherent KUPPA identity.

## Architectural context
The previous Heart cycle introduced `VayuBrainGateway v1`, which returns provider, degraded state, latency, error code, correlation ID, and contract version. The UI already had `ASKING_VAYU` and `THINKING` states but discarded this metadata after `/api/chat` returned. KUPPA remains the HEART; Vayu remains the BRAIN.

## Detailed changes
- Added a compact Vayu status chip separate from avatar-runtime status.
- Added `unknown`, `pending`, `healthy`, `fallback`, and `offline` brain-presence modes on the avatar stage.
- Bound `/api/chat` `brain` metadata to the visual presence state.
- Healthy Ollama handoffs show provider and measured latency.
- OpenAI fallback handoffs are explicitly marked degraded/fallback.
- `VAYU_UNAVAILABLE`/`NONE` handoffs show Vayu unavailable rather than implying successful reasoning.
- Added a `kuppa-brain-state-change` browser event carrying the already-returned gateway metadata for future observability hooks.
- Added subtle avatar/aura expression differences for fallback and unavailable states.
- Kept text, voice, barge-in, approval cards, and the nine-state interaction engine intact.
- Added a resource-level regression test that verifies the UI consumes the versioned gateway health fields and retains approval UI.

## Files/components affected
- `src/main/resources/static/index.html`
- `src/test/java/ai/kuppa/ui/AvatarBrainPresenceContractTest.java`
- `CHANGELOG.md`
- `docs/evolution/README.md`
- this evolution record

## Behavior before
KUPPA displayed `Asking Vayu`/`Vayu is thinking`, then always moved to a generic response-ready state. A successful local Ollama response, OpenAI fallback, and complete Vayu outage looked almost identical at the avatar-presence layer.

## Behavior after
KUPPA still presents one identity, but its body now communicates the state of the delegated brain: `Vayu · Ollama · <latency>`, `Vayu fallback · OpenAI fallback · <latency>`, or `Vayu unavailable`. This comes entirely from `VayuBrainGateway v1` metadata.

## KUPPA/Vayu responsibility impact
No responsibility moved. KUPPA only renders gateway state. Vayu continues to own reasoning, provider routing, planning, orchestration, knowledge, tools, and execution strategy. The browser contains no provider-selection or reasoning logic.

## API/event/schema/config/migration changes
- No backend API/schema/config migration.
- New browser event: `kuppa-brain-state-change` with mode plus the gateway metadata already present in `/api/chat`.
- New DOM state attribute: `data-brain`.

## Tests/build/lint/smoke checks run with results
- Preflight confirmed branch `agent/avatar-ui` was identical to `efd238cc8e9a5fdcc53323a3c69008644843b2e6`.
- Previous runtime baseline `930bd83fd3bb64559c4b5ab9da29b7201da9a223` was CI-green on run #98 before this change.
- Added `AvatarBrainPresenceContractTest` for health metadata, browser event, Vayu-unavailable handling, `/api/chat`, and approval UI.
- GitHub Actions CI run #100 for implementation commit `1efac9e2485a6181413b30a003a88654c3cd9792`: **PASS**. Checkout, Java setup, Maven Test, cleanup, and job completion all succeeded.

## Relevant before/after metrics
- Vayu health states visible in avatar UI: **0 -> 5** (`unknown`, `pending`, `healthy`, `fallback`, `offline`).
- Gateway fields consumed by UI: **0 -> 6** (provider, degraded, latency, error code, correlation ID, contract version).
- Explicit Vayu outage presentation: **0 -> 1**.
- Browser Vayu-observability events: **0 -> 1**.
- Conversation windows added: **0**.
- Approval-gate behavior changed: **0**.
- Build stability: **green -> green** (CI #98 baseline -> CI #100 implementation).

## Security/privacy/permission implications
No new credentials, destinations, tool permissions, autonomous actions, or execution paths. Correlation IDs are exposed only in the status element title/event for diagnostics; raw provider exceptions remain excluded by the backend contract. Consequential actions remain approval gated.

## Known limitations
- The UI receives Vayu metadata only when `/api/chat` completes; it does not yet stream intermediate gateway events.
- Barge-in still stops speech playback but does not cancel an in-flight Vayu request.
- This does not create aggregate latency/reliability telemetry.

## Failures/fallbacks tested
The new contract test verifies explicit unavailable/fallback handling at the UI contract level. Backend runtime success/fallback/outage behavior remains covered by the Vayu gateway tests. CI #100 passed the complete Maven test step after this UI change.

## Rollback procedure / known-good reference
For regression, return `agent/avatar-ui` to `efd238cc8e9a5fdcc53323a3c69008644843b2e6`. The newly validated runtime implementation is `1efac9e2485a6181413b30a003a88654c3cd9792` (CI #100 green). The earlier validated gateway baseline remains `930bd83fd3bb64559c4b5ab9da29b7201da9a223`.

## Risks / technical debt introduced or removed
Removes ambiguity between healthy, fallback, and unavailable brain responses. Adds a small UI dependency on the optional `brain` response field; missing metadata degrades safely to `unknown`.

## Dependencies
No new dependencies.

## Screenshots / visual references
Not captured in this execution environment. Visual changes are limited to the existing avatar stage and status chips; no conversation panel/window was introduced.

## Follow-up work
Stream gateway phase events and add cancellable/resumable requests.

## Next evolution target
Introduce cancellable Vayu handoffs with correlation-aware request lifecycle so barge-in can eventually interrupt an in-flight brain request rather than only speech playback.

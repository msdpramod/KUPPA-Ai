# KUPPA Evolution Record — Vayu Brain Gateway v1

- **Date/time:** 2026-08-24 03:00 Asia/Kolkata
- **Cycle:** Heart / Personality / Relationship
- **Commit purpose:** Formalize the KUPPA-to-Vayu cognition boundary as a versioned, observable gateway with correlation IDs, latency/provider metadata, and explicit degraded-mode semantics.
- **Hypothesis:** A stable brain handoff contract will improve trust, debuggability, failure honesty, and future independent evolution of KUPPA and Vayu without moving reasoning into KUPPA.

## Architectural context
KUPPA remains the HEART: identity, relationship continuity, memory presentation, conversation, voice/avatar presence, and trust. Vayu remains the BRAIN: reasoning and future orchestration/tool selection. The existing provider router (Ollama primary, optional OpenAI fallback) is now wrapped by a KUPPA-facing gateway contract rather than being called directly by the planner.

## Detailed changes
- Added `VayuBrainGateway` with contract version `v1`.
- Every brain request receives a UUID correlation ID and measured latency in milliseconds.
- Added provider metadata: `OLLAMA`, `OPENAI_FALLBACK`, or `NONE`.
- Added explicit degraded state and stable error codes (`OLLAMA_UNAVAILABLE`, `VAYU_UNAVAILABLE`).
- Changed `BrainRouterService` to return structured provider/degradation metadata while preserving the legacy string `answer(...)` method for compatibility.
- Changed `LocalPlanner` to use the Vayu gateway for ordinary cognition; approval-gated action detection still short-circuits before brain execution.
- Extended `Plan` and `/api/chat` response metadata with the gateway response while keeping the existing top-level `message` and `proposedAction` contract intact.
- Added `VAYU_HANDOFF` audit events containing contract version, provider, degraded status, latency, and stable error code.
- Removed raw backend exception text from user-facing degraded responses.
- Added focused tests for healthy Ollama, OpenAI fallback, total brain outage, contract version/correlation ID, and degraded propagation.
- Corrected the previous evolution index entry to the actual reactive-state-engine commit hash.

## Files/components affected
- `src/main/java/ai/kuppa/conversation/BrainRouterService.java`
- `src/main/java/ai/kuppa/conversation/VayuBrainGateway.java`
- `src/main/java/ai/kuppa/planner/Plan.java`
- `src/main/java/ai/kuppa/planner/LocalPlanner.java`
- `src/main/java/ai/kuppa/chat/ChatService.java`
- `src/test/java/ai/kuppa/conversation/BrainRouterServiceTest.java`
- `src/test/java/ai/kuppa/conversation/VayuBrainGatewayTest.java`
- `docs/adr/0002-vayu-brain-gateway-v1.md`
- `docs/evolution/README.md`
- `CHANGELOG.md`
- this evolution record

## Behavior before
`LocalPlanner` called `BrainRouterService` directly and received only a response string. KUPPA could not identify a brain handoff, correlate it in logs, distinguish primary/fallback providers, expose degradation programmatically, or measure handoff latency. On total provider failure, raw exception details were included in the user-facing response.

## Behavior after
Ordinary cognitive requests go through `VayuBrainGateway v1`. Each response includes correlation ID, provider, degraded flag, latency, and stable error code. Healthy Ollama responses are marked non-degraded. OpenAI fallback responses are usable but explicitly marked degraded because the primary local brain is unavailable. If no provider is available, KUPPA remains present and clearly says Vayu reasoning is unavailable without fabricating a result or leaking backend exception text.

## KUPPA/Vayu responsibility impact
- **KUPPA:** gains a stable HEART-side handoff contract and truthful degraded-mode presentation.
- **Vayu:** remains the owner of brain-level cognition; no reasoning/planning/tool-selection code moved into KUPPA.
- **Specialist agents:** unchanged; future agent/tool execution remains behind Vayu and approval/policy gates.

## API/event/schema/config/migration changes
- `/api/chat` keeps existing `message` and `proposedAction` fields and adds optional `brain` metadata for cognitive responses.
- Brain metadata fields: `contractVersion`, `correlationId`, `message`, `provider`, `degraded`, `latencyMs`, `errorCode`.
- Internal gateway contract version: `v1`.
- New audit event type: `VAYU_HANDOFF`.
- No database migration or configuration change.

## Tests/build/lint/smoke checks and results
- Preflight verified branch head `5a8357eabed348534484d161d94c7d988c90244b` and previous evolution target.
- Known-good runtime baseline remains `7616e6f344ee57a9a08c0ed55dba01701b4aaf23` with documented green CI run #95; governance successor `b7b937...` remains runtime-identical.
- Local environment check: Java 21 available; Maven unavailable.
- Fresh GitHub clone attempt: blocked because the execution environment cannot resolve `github.com`.
- Added focused JUnit/Mockito tests for gateway and router success/fallback/failure paths.
- Static source checks performed before publication: gateway version constant present, UUID correlation generation present, direct planner dependency on `BrainRouterService` removed, approval action paths remain before gateway invocation, and degraded response does not interpolate provider exception text.
- Post-publish GitHub CI is the required build gate for this evolution because local Maven execution is unavailable. The run result must be checked before treating build stability as green.

## Before/after metrics
- Versioned KUPPA↔Vayu gateway contracts: **0 -> 1**.
- Correlation IDs on brain handoffs: **0% -> 100%** for gateway-routed cognitive requests by construction.
- Provider/degraded metadata on cognitive responses: **0 -> 6 fields** (`contractVersion`, `correlationId`, `provider`, `degraded`, `latencyMs`, `errorCode`; message retained separately).
- Measured handoff latency: **not available -> per-request milliseconds**.
- Stable degraded error codes: **0 -> 2**.
- Raw provider exception detail exposed to user on total brain failure: **yes -> no**.
- Approval-gated consequential action path: **unchanged**.

## Security/privacy/permission implications
- No credentials, new network destinations, shell execution, or autonomous external actions added.
- Correlation IDs are random UUIDs and contain no user content.
- Audit details store provider/status/latency/error code, not the user message or provider exception stack.
- Approval-gated external/high-impact actions continue to bypass brain execution and require explicit approval.

## Known limitations
- Vayu is still represented by local provider routing inside this repository; this gateway prepares a clean boundary but is not yet a remote Vayu service/client.
- Gateway cancellation is not implemented; barge-in still cannot cancel an in-flight reasoning request.
- `brain.message` duplicates the top-level chat `message` in JSON for v1 simplicity.
- No aggregate handoff reliability dashboard yet.

## Failures/fallbacks tested
- Unit test: healthy Ollama path returns `OLLAMA`, non-degraded.
- Unit test: Ollama failure with enabled OpenAI fallback returns `OPENAI_FALLBACK`, degraded with `OLLAMA_UNAVAILABLE`.
- Unit test: total brain outage returns `NONE`, degraded with `VAYU_UNAVAILABLE` and does not expose backend exception detail.
- Unit test: gateway preserves degraded metadata and always produces a version/correlation ID.

## Rollback procedure / known-good reference
Revert this evolution commit to `5a8357eabed348534484d161d94c7d988c90244b` to restore the pre-gateway runtime while keeping the reactive UI evolution. If a deeper rollback is required, use runtime baseline `7616e6f344ee57a9a08c0ed55dba01701b4aaf23`.

## Risks / technical debt introduced or removed
- Removes opaque string-only brain handoffs and raw failure-detail leakage.
- Adds a small contract surface that must be versioned rather than silently changed.
- Audit volume increases by one event per cognitive request.
- Remote Vayu transport, cancellation, retries/circuit breaking, and aggregate metrics remain future work.

## Dependencies
No new dependencies. Existing Spring Boot test support already supplies JUnit, AssertJ, and Mockito.

## Screenshots / visual references
Not applicable; this is a Heart/backend boundary evolution with no visual change.

## Follow-up work
- Add a real Vayu client/transport behind `VayuBrainGateway` while keeping the v1 KUPPA-facing contract stable.
- Add timeout/cancellation and circuit-breaker semantics with correlation-aware events.
- Add handoff metrics to Actuator/Micrometer and expose degraded brain state in the avatar UI without adding a conversation window.
- Remove duplicated `brain.message` in a future v2 contract only with explicit compatibility handling.

## Next evolution target
For the next UI cycle, bind the existing `ASKING_VAYU`/`THINKING` presentation to returned gateway metadata so the avatar can show healthy, fallback, and degraded cognition states without exposing technical noise or adding a separate conversation panel.

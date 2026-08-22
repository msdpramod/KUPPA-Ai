# KUPPA Evolution Record — Reactive State Engine and Barge-In

- **Date/time:** 2026-08-22 15:15 Asia/Kolkata
- **Cycle:** UI / Human Interaction
- **Commit purpose:** Replace the avatar page's four coarse interaction states with an explicit human-interaction state engine and add interruption/barge-in while KUPPA is speaking.
- **Hypothesis:** Making KUPPA's interaction lifecycle explicit will improve perceived responsiveness, reduce ambiguous UI behavior, and create a safer foundation for later resumable conversation and Vayu gateway observability.

## Architectural context
KUPPA remains the HEART. The browser owns presentation, presence, voice interaction, avatar expression, and interaction-state visualization. The existing `/api/chat` boundary is treated as the point where KUPPA asks the BRAIN; the UI now exposes `ASKING_VAYU`/`THINKING` without moving reasoning into KUPPA. Vayu responsibilities are unchanged.

## Detailed changes
- Added explicit states: `IDLE`, `NOTICED_USER`, `LISTENING`, `UNDERSTANDING`, `ASKING_VAYU`, `THINKING`, `RESPONDING`, `SPEAKING`, `WAITING`.
- Added guarded state transitions and a `kuppa-state-change` browser event for future observability/instrumentation.
- Added presence acknowledgement when pointer movement is detected while idle/waiting.
- Added state-specific aura feedback.
- Added barge-in during speech: microphone click or Escape stops playback and returns KUPPA to listening.
- Added cleanup of audio analyser/source nodes on interruption.
- Added dynamic microphone labels/pressed state, `aria-live` status/subtitle regions, focus-visible treatment, and hidden interaction guidance.
- Preserved typed conversation, avatar-offline fallback, speech-recognition fallback, voice synthesis, and approval cards.
- Added governance artifacts: Constitution, known-good baseline, evolution index, changelog, and ADR-0001.

## Files/components affected
- `src/main/resources/static/index.html`
- `docs/evolution/README.md`
- `docs/evolution/BASELINE.md`
- `docs/KUPPA_CONSTITUTION.md`
- `docs/adr/0001-heart-brain-boundary.md`
- `CHANGELOG.md`
- this evolution record

## Behavior before
The avatar page represented only `idle`, `listening`, `thinking`, and `speaking`; movement between them was ad hoc. While audio was playing, the user had no explicit interruption path. The UI did not distinguish understanding, handoff to Vayu, response preparation, or waiting.

## Behavior after
The UI has a deterministic nine-state lifecycle. The KUPPA-to-brain moment is visible as `ASKING_VAYU` followed by `THINKING`. Speech can be interrupted immediately via microphone or Escape, after which KUPPA resumes listening. Invalid non-forced state transitions are blocked and logged instead of silently corrupting state.

## KUPPA/Vayu responsibility impact
- **KUPPA:** richer presence/state expression only.
- **Vayu:** no logic moved into KUPPA; deep cognition remains behind `/api/chat` pending a future versioned Brain Gateway.
- **Specialist agents:** unchanged.

## API/event/schema/config/migration changes
- No server API/schema/config/database migration changes.
- New browser event: `kuppa-state-change` with `{from,to,visual}` detail.
- No external action contract changes.

## Validation and results
- Pre-change runtime baseline: CI run #95 succeeded on `7616e6f344ee57a9a08c0ed55dba01701b4aaf23`.
- `node --check` on the extracted inline JavaScript: **PASS**.
- Static invariant check: all 9 required states present: **9/9 PASS**.
- Static invariant check: `bargeIn` implementation present: **PASS**.
- Static invariant check: `ASKING_VAYU` handoff state present: **PASS**.
- Static invariant check: approval UI still filters `PENDING_APPROVAL` and calls existing approve/reject endpoints: **PASS**.
- Maven was not available in the execution environment, so no local Maven result is claimed. GitHub CI must be used as the post-publish build gate.
- Full browser/device smoke was not available in this execution environment and remains a limitation.

## Before/after metrics
- Explicit interaction states: **4 -> 9**.
- Explicit speech-interruption controls: **0 -> 2** (microphone and Escape).
- Distinct visible brain-handoff phases: **0 -> 2** (`ASKING_VAYU`, `THINKING`).
- Screen-reader live regions for state/conversation: **0 -> 2**.
- Approval-gated external-action path: **unchanged**.

## Security/privacy/permission implications
No new network destinations, credentials, permissions, tool execution, shell capability, or autonomous actions were introduced. Consequential actions still rely on the existing approval endpoints. Barge-in only stops local playback and restarts speech recognition.

## Known limitations
- `/api/chat` is still an unversioned coupling rather than the desired Vayu Brain Gateway.
- Interruption cancels local speech playback but does not yet cancel an in-flight backend/Vayu reasoning request.
- Browser speech recognition support varies by browser.
- No automated visual-regression or accessibility scanner is configured yet.

## Failures/fallbacks tested
- Static verification confirms avatar fallback text path remains present.
- Speech-recognition-unavailable path still falls back to typing.
- Invalid state transitions are rejected unless explicitly forced for recovery.
- Approval gating is preserved by static invariant check.

## Rollback procedure / known-good reference
Revert this evolution commit to `b7b937f8e87af8882619f850078f84173b2d3b85`. That preserves the governance bootstrap while restoring the previous runtime UI. Runtime-only known-good baseline is `7616e6f344ee57a9a08c0ed55dba01701b4aaf23`, CI run #95 green.

## Risks / technical debt introduced or removed
- Removed ad hoc interaction-state ambiguity.
- Added a small client-side state-machine surface that now requires future UI behavior to use valid transitions.
- Backend cancellation/resume semantics remain technical debt.

## Dependencies
No new dependencies. Existing Three.js CDN, browser SpeechRecognition, Web Audio, and current KUPPA backend endpoints remain in use.

## Screenshots / visual references
No screenshot artifact was generated in this environment. State-specific aura behavior is implemented in CSS selectors keyed by `data-state`.

## Follow-up work
- Add a versioned Vayu Brain Gateway with correlation IDs and latency/error events.
- Add cancellable in-flight brain requests so barge-in can interrupt reasoning as well as speech.
- Add browser-level smoke/accessibility tests and basic state-transition telemetry.

## Next evolution target
For the next Heart cycle, formalize a versioned KUPPA<->Vayu gateway/fallback contract so KUPPA can preserve presence and clearly communicate degraded cognition when Vayu is unavailable.

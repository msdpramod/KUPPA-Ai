# Changelog

## 2026-08-24
### Changed
- Added versioned `VayuBrainGateway v1` as the explicit KUPPA HEART -> Vayu BRAIN cognition boundary.
- Added per-request brain correlation IDs, provider/degraded metadata, and latency measurement.
- Added `VAYU_HANDOFF` audit events and optional brain metadata to `/api/chat` responses.
- Added explicit graceful degradation when Ollama/OpenAI brain providers are unavailable without leaking raw provider exception details.

### Safety
- Consequential external/high-impact action approval flow is unchanged and still short-circuits before brain execution.
- No secrets, new network destinations, unrestricted shell execution, self-modification, or autonomous external actions were introduced.

## 2026-08-22
### Changed
- Added a nine-state KUPPA interaction engine: `IDLE -> NOTICED_USER -> LISTENING -> UNDERSTANDING -> ASKING_VAYU -> THINKING -> RESPONDING -> SPEAKING -> WAITING`.
- Added user interruption/barge-in while KUPPA is speaking via the microphone button or Escape key.
- Added state-specific aura feedback and clearer Vayu handoff/response preparation states.
- Improved screen-reader semantics with live status/subtitle regions, microphone pressed state, dynamic labels, and hidden interaction guidance.
- Bootstrapped governed evolution documentation, a known-good baseline, the KUPPA Constitution, and the HEART/BRAIN ADR.

### Safety
- Approval-gated action UI remains unchanged in behavior.
- No secrets, unrestricted shell execution, unrestricted self-modification, or autonomous consequential actions were introduced.

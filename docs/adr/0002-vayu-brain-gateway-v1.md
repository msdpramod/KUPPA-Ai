# ADR-0002: Version the KUPPA-to-Vayu Brain Gateway

- **Status:** Accepted
- **Date:** 2026-08-24

## Context
KUPPA previously called a provider router directly and received only a string. That made the HEART/BRAIN boundary opaque and made graceful degradation, tracing, latency measurement, and independent Vayu evolution harder.

## Decision
KUPPA will delegate brain-level cognition through a versioned `VayuBrainGateway`. Contract v1 carries a correlation ID, provider, degraded state, latency, and stable error code in addition to the response text. KUPPA must not infer or fabricate a Vayu result when the gateway reports degradation/unavailability.

The initial implementation may wrap the existing Ollama/OpenAI provider router, but callers depend on the gateway rather than provider-specific services. Future remote Vayu transport must live behind this boundary.

## Consequences
- KUPPA stays the HEART and does not absorb reasoning/orchestration.
- Vayu/provider implementations can evolve behind a stable KUPPA-facing contract.
- Brain failures become explicit and observable instead of being encoded only as prose.
- Correlation IDs enable later cancellation, distributed tracing, and specialist-agent event chains.
- Contract-breaking changes require a new gateway version rather than silent field/semantic changes.

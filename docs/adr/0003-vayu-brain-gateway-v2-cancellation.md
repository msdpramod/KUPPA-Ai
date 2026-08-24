# ADR 0003 — Vayu Brain Gateway v2 Cooperative Cancellation

## Status
Accepted for the 2026-08-25 Heart evolution cycle.

## Context
KUPPA could interrupt speech, but the HEART had no stable way to identify or cancel an in-flight Vayu brain turn. `VayuBrainGateway v1` generated correlation IDs only after the request entered the gateway, so a client could not know the ID early enough to request cancellation.

## Decision
`VayuBrainGateway v2` accepts an optional caller-provided correlation ID, registers active brain turns in a bounded in-process lifecycle registry, exposes a cancellation operation, and suppresses provider output when a turn has been cancelled before the provider returns. KUPPA remains responsible only for requesting cancellation and presenting state; Vayu owns the brain-request lifecycle.

Cancellation is deliberately cooperative. The current synchronous Ollama/OpenAI router may still finish its underlying provider call after cancellation is requested. v2 guarantees that a cancelled result is not treated as the active Vayu answer; it does not claim provider-level compute cancellation.

## Consequences
- The UI can generate a correlation ID before `/api/chat` and later call `/api/chat/{correlationId}/cancel`.
- Stale brain results can be suppressed deterministically.
- No new external permissions or autonomous actions are introduced.
- A future independent Vayu service can implement provider-native cancellation behind the same conceptual boundary.

# ADR 0004 — Vayu Brain Gateway v3 Resumable Turn Context

- **Status:** Accepted for validation
- **Date:** 2026-08-26

## Context
KUPPA already sends recent conversation to Vayu and supports correlation-aware cancellation, but the cognition boundary does not distinguish a new topic from a continuation or correction. Inferring this relationship inside KUPPA would move reasoning toward the HEART and blur the Constitution boundary.

## Decision
Upgrade the KUPPA -> Vayu contract to `VayuBrainGateway v3` with optional, normalized turn-context metadata:

- `AUTO` — Vayu infers the relationship from recent conversation.
- `NEW_TOPIC` — recent conversation is background; do not force old references.
- `CONTINUE` — continue the prior thought and resolve relevant references.
- `CORRECTION` — prefer the current user statement over conflicting recent conversational context.
- `parentCorrelationId` — optional identity of the related prior turn.

KUPPA transports and exposes this metadata. Vayu/provider prompting interprets it. Missing or invalid modes normalize to `AUTO` for backward compatibility.

## Consequences
- Existing clients remain compatible without sending new fields.
- Future avatar/UI controls can express explicit continuation/correction/new-topic intent without inventing reasoning in KUPPA.
- Ollama and OpenAI fallback receive the same continuity directive.
- The contract version changes from v2 to v3, including cancellation responses.
- Correlation IDs remain observability/lifecycle identifiers, not authorization tokens.

## Safety and boundary
This change adds no tool authority, external action capability, autonomous execution, or new permission. Vayu remains responsible for reasoning and interpretation; KUPPA remains the HEART and does not automatically classify natural-language turns.

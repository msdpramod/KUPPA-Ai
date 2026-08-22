# ADR-0001: Preserve KUPPA Heart / Vayu Brain Boundary

- **Status:** Accepted
- **Date:** 2026-08-22

## Decision
KUPPA is the human-facing HEART and Vayu is the cognitive BRAIN. KUPPA may expose interaction states such as `ASKING_VAYU`, but it must not absorb deep planning, orchestration, retrieval, tool-selection, specialist-agent coordination, or execution strategy.

## Consequences
- UI and personality can evolve independently from Vayu cognition.
- KUPPA must fail visibly and safely when brain-level capability is unavailable rather than fabricating a result.
- Future Vayu integration should move behind a versioned gateway/event contract without changing the coherent KUPPA identity.
- Worker agents remain behind Vayu and do not become separate user-facing personalities.

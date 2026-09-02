# KUPPA Known-Good Baseline

## Runtime baseline
- **Current validated implementation:** `da8c13b42011360eb63ce30dd14fa0abf1e414a1` (explicit exact-match owner memory forgetting), validated by GitHub Actions CI #161.
- **Previous validated implementation:** `0006222796a71b1ae0bea070d68d0c8c952b4611` (avatar-first typed trust activity inside Trusted Devices), validated by CI #158.
- **Pre-change governed branch head:** `e2f92f1f5b0aa311b8a4e7131b50d825c8404de8`.

## Current evidence
- `forget that ...` and `please forget that ...` can deactivate an exact active owner memory through ordinary conversation.
- Matching is deliberately conservative: case, whitespace, and final `.`, `!`, `?` are normalized, but partial/fuzzy/semantic deletion is not performed.
- The forgotten memory uses the existing reviewed/inactive state so it stops participating as active personal context.
- Unrelated active memories remain untouched.
- No API, schema, dependency, secret/configuration, Vayu cognition, voice/avatar engine, or approval-gate changes were introduced.
- CI #161 passed the full Maven test workflow for implementation `da8c13b...`, including exact-forget success and partial-match safety coverage.

## Scorecard baseline
| Dimension | Evidence status |
|---|---|
| Build stability | Green on CI #161 |
| Conversation quality | Improved owner control over stale conversational memory; generation path otherwise unchanged |
| Personality consistency | Improved by allowing stale preferences/facts to be withdrawn explicitly |
| Memory accuracy | Improved: exact owner-directed forgetting deactivates stale active memory; fuzzy deletion remains disabled |
| Vayu handoff reliability/latency | Vayu Gateway v3 unchanged |
| Errors | Near/partial forget wording fails safely by leaving memory active |
| Voice reliability | Unchanged |
| UI responsiveness | Unchanged |
| Accessibility | Unchanged |
| Resource usage | One bounded active-memory read on explicit forget command; no new dependency/schema work |
| Security boundary | Owner-directed local memory control only; no secret, external action, or approval-gate changes |

## Rollback policy
Return to governed head `e2f92f1f5b0aa311b8a4e7131b50d825c8404de8` to remove this Heart evolution, restoring validated runtime `0006222796a71b1ae0bea070d68d0c8c952b4611` (CI #158). No destructive schema rollback is required. Normal evolution must preserve the Constitution, HEART/BRAIN boundary and approval gates.

## Next identified gaps
- Add owner-visible observability for memory forget/correction operations without exposing private content unnecessarily.
- Add a safe conversational disambiguation flow for near-match memories; never autonomously erase ambiguous candidates.
- Replace static owner enrollment/management shared-secret authentication with passkeys/WebAuthn/OIDC-grade authentication.
- Add explicit owner identity to trust/audit persistence before any multi-owner architecture.
- Replace classic-script global continuity/trust bindings with an explicit KUPPA adapter/module.
- Move durable device possession credentials away from general browser localStorage when a stronger credential primitive is introduced.

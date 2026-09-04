# 2026-09-03 03:10 Heart — Explicit owner memory forgetting validation

## Why this documentation-only commit exists
This closeout records post-commit CI evidence, promotes the validated runtime to the known-good baseline, and adds the implementation commit hash to the chronological evolution index. Runtime behavior is unchanged from implementation `da8c13b42011360eb63ce30dd14fa0abf1e414a1`.

## Validation
- GitHub Actions CI #161 completed successfully for implementation `da8c13b42011360eb63ce30dd14fa0abf1e414a1`.
- Full Maven `test` workflow passed.
- Success path covered: an explicit exact `forget that ...` instruction deactivates only the matching active memory.
- Failure/fallback path covered: partial/near-match wording does not deactivate an existing memory.
- Existing memory-capture tests remained green.

## Metrics / regression assessment
- Explicit conversational forget patterns: 0 -> 2.
- Fuzzy/semantic delete paths: 0 -> 0.
- New schema/dependencies/secrets: 0.
- Vayu cognition/handoff changes: 0.
- Approval-gate changes: 0.
- Build stability: green.
- Memory accuracy/control: improved without broad deletion.

## Architectural impact
KUPPA remains the HEART and owns the personal-memory interface. Vayu remains the BRAIN and owns reasoning, planning, orchestration, retrieval, tools, specialist agents and execution strategy. The Constitution is unchanged.

## Security / privacy
The change improves owner control of stored personal context. It introduces no credential handling, external action, shell execution, or unrestricted self-modification.

## Rollback
Rollback to governed head `e2f92f1f5b0aa311b8a4e7131b50d825c8404de8`, restoring validated runtime `0006222796a71b1ae0bea070d68d0c8c952b4611` from CI #158. No database rollback is required.

## Known limitations / next target
Exact text is intentionally required. A future Heart cycle may add safe candidate disambiguation and memory-change observability. The next UI cycle should replace classic-script continuity/trust globals with an explicit KUPPA adapter/module.

# 2026-09-03 03:00 Heart — Explicit owner memory forgetting

## Date / cycle
- 2026-09-03 03:00 Asia/Kolkata
- Cycle: Heart / Personality / Relationship

## Commit purpose and hypothesis
Allow the owner to explicitly tell KUPPA to forget one stale personal memory in ordinary conversation. Hypothesis: a narrow exact-match forget command improves relationship continuity and memory accuracy because owner corrections can remove stale context without broad inference or destructive fuzzy matching.

## Architectural context
KUPPA remains the HEART and owns the confidence-aware personal-memory interface. Vayu remains the BRAIN and continues to own reasoning, planning, orchestration, retrieval, tool/agent selection and execution strategy. This change only affects KUPPA's conversational memory-capture boundary.

## Detailed changes
- Recognize `forget that ...` and `please forget that ...` before normal memory classification.
- Normalize case, surrounding whitespace and terminal `.`, `!`, or `?` only for the forget comparison.
- Deactivate only active memories whose full content exactly matches the requested forgotten content after that normalization.
- Mark a forgotten memory reviewed/inactive using the existing `PersonaMemory.review(false)` behavior and persist it.
- Do not create a new tombstone/persona fact and do not use fuzzy, substring or semantic deletion.
- Add unit coverage for successful exact forgetting and the failure-safe partial-match case.

## Files / components affected
- `src/main/java/ai/kuppa/memory/ConversationMemoryCaptureService.java`
- `src/test/java/ai/kuppa/memory/ConversationMemoryCaptureServiceTest.java`
- `CHANGELOG.md`
- this evolution record

## Behavior before
A conversational owner instruction such as `Forget that I prefer concise technical answers` was ignored by memory capture. Stale active memory therefore remained available to KUPPA's memory presentation unless corrected through the explicit memory API.

## Behavior after
The same explicit instruction deactivates the exact matching active memory. Similar but non-identical text is intentionally left untouched, avoiding broad accidental deletion.

## KUPPA / Vayu responsibility impact
- KUPPA: gains a safer owner-directed memory-forgetting interaction.
- Vayu: no responsibility or cognition change.
- Specialist agents/organs: unchanged.

## API / event / schema / config / migration changes
- API: none.
- Events: none.
- Schema: none.
- Configuration/secrets: none.
- Migration: none.

## Tests / build / lint / smoke checks
- Added unit test: exact forget deactivates the requested memory and preserves unrelated active memory.
- Added unit test: partial text does not deactivate a memory.
- Existing memory-capture tests remain in the same suite.
- Local Maven execution was unavailable in the automation container because direct GitHub network resolution is disabled; the implementation is not promoted until repository CI passes.

## Relevant before / after metrics
- Conversational explicit forget patterns: 0 -> 2 (`forget that`, `please forget that`).
- Fuzzy/substring deletion paths: 0 -> 0.
- New database tables/columns: 0.
- New runtime dependencies: 0.
- Vayu cognition changes: 0.
- Approval-gate changes: 0.

## Security / privacy / permission implications
This is an owner-directed privacy/control improvement, but deletion is deliberately conservative. Only an explicit command with exact full-memory content can deactivate a record. No credential, secret, external action, arbitrary shell path or self-modification capability is introduced.

## Known limitations
- Exact-match forgetting requires the owner to substantially repeat the stored memory text; KUPPA does not yet offer a conversational disambiguation flow for near matches.
- The capture method returns no new memory object after forgetting, so callers observe the side effect only through subsequent memory reads.
- No dedicated memory-forget audit event is added in this increment.

## Failures / fallbacks tested
- Near/partial text must not delete an existing active memory.
- Unrelated active memories remain active when one exact memory is forgotten.
- Ordinary memory classification behavior remains unchanged after the forget pre-check.

## Rollback procedure / known-good reference
Rollback to governed branch head `e2f92f1f5b0aa311b8a4e7131b50d825c8404de8`, restoring validated runtime `0006222796a71b1ae0bea070d68d0c8c952b4611` from CI #158. No schema rollback is required.

## Risks / technical debt introduced or removed
Removed: stale owner memory could not be withdrawn naturally through conversation.
Introduced: exact textual matching is intentionally limited and may miss semantically equivalent wording; broad semantic deletion is deferred because it could erase the wrong memory.

## Dependencies
No new dependencies.

## Screenshots / visual references
Not applicable; no UI changes.

## Follow-up work
- Add an explicit conversational disambiguation flow that can present candidate memories without autonomously deleting near matches.
- Consider an owner-visible memory activity/audit event for forget/correct operations.
- Continue stronger owner authentication work (WebAuthn/passkeys or OIDC-grade identity) independently of this memory change.

## Next evolution target
For the next UI cycle, replace classic-script continuity/trust globals with an explicit KUPPA adapter/module while keeping the avatar primary. For the next Heart cycle, improve correction/forget observability and candidate disambiguation without moving reasoning into KUPPA.

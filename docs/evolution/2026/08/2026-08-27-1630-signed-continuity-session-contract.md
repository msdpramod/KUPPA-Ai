# KUPPA Evolution Record — Signed Continuity Session Contract

- **Date/time:** 2026-08-27 16:30 Asia/Kolkata
- **Cycle:** Heart / Personality / Relationship — manual security hardening follow-up
- **Commit purpose:** Add a server-verifiable continuity-session possession credential without pretending the existing browser session ID is owner authentication.
- **Hypothesis:** A server-issued expiring HMAC credential will create a safe migration path from same-browser metadata recovery toward an authenticated cloud continuity boundary while preserving KUPPA HEART / Vayu BRAIN separation.

## Architectural context
The validated runtime `33ad4d0b1c76bf7886f33d165b5fee1a4da989b3` (CI #113 green) restores resumable-turn metadata after refresh using a high-entropy browser session ID. Its evolution record explicitly states that this identifier is not authentication or authorization and that cloud/multi-user deployment still needs a stronger boundary.

## Detailed changes
- Added `ContinuitySessionService` using HMAC-SHA256 with constant-time signature comparison.
- Added server-generated UUID session IDs, signed expiry binding, and configurable TTL.
- Added `POST /api/chat/session` to issue a secure continuity credential when configured.
- Added `GET /api/chat/resumable/secure`, requiring `X-KUPPA-Continuity-Token` before delegating to the existing metadata-only continuity lookup.
- Added fail-closed behavior when the signing secret is absent/short, the token is malformed/tampered, the session ID differs, or the token is expired.
- Added environment-only configuration for signing secret and TTL.
- Kept the existing `/api/chat/resumable` contract unchanged so the current avatar UI is not broken before its own migration cycle.
- Added focused unit coverage for issue/validate, tampering, expiry, and disabled-secret fallback.

## Files/components affected
- `src/main/java/ai/kuppa/chat/ContinuitySessionService.java`
- `src/main/java/ai/kuppa/chat/ChatController.java`
- `src/main/resources/application.yml`
- `src/test/java/ai/kuppa/chat/ContinuitySessionServiceTest.java`
- `docs/adr/0005-signed-continuity-session-credential.md`
- `CHANGELOG.md`
- `docs/evolution/README.md`
- this evolution record

## Behavior before
Possession of a valid-format browser session ID was enough to call the metadata-only resumable endpoint. This was intentionally low exposure but not a server-verifiable security boundary.

## Behavior after
A new secure path exists in which the server generates the session ID and signs its expiry using an environment-provided secret. Secure lookup requires the matching token. The legacy UI path remains available unchanged until a later UI migration is separately validated.

## KUPPA/Vayu responsibility impact
KUPPA HEART gains only a stronger interaction-continuity possession boundary. Vayu BRAIN remains responsible for interpreting `CONTINUE`/`CORRECTION`, restoring parent meaning, reasoning, planning, retrieval, tool selection, and orchestration. No cognition moved into KUPPA.

## API/event/schema/config/migration changes
- New `POST /api/chat/session`.
- New `GET /api/chat/resumable/secure?clientSessionId=...` requiring `X-KUPPA-Continuity-Token`.
- New `KUPPA_CONTINUITY_SIGNING_SECRET` environment configuration; values shorter than 32 bytes disable the secure contract.
- New `KUPPA_CONTINUITY_SESSION_TTL_SECONDS`, default 2,592,000 seconds (30 days).
- No database schema change and no new dependency.

## Tests/build/lint/smoke checks run with results
- Preflight known-good runtime: CI #113 **PASS** on `33ad4d0b1c76bf7886f33d165b5fee1a4da989b3`.
- Candidate includes focused unit tests for valid issuance, tampered session/signature rejection, expiry rejection, and weak/missing secret fail-closed behavior.
- Full candidate Maven/CI validation is required before runtime promotion; this record must be updated/closed out after CI rather than claiming an unobserved pass.

## Relevant before/after metrics
- Server-verifiable continuity possession contracts: **0 -> 1 additive path**.
- Signed/expiring continuity credentials: **0 -> 1**.
- Secure-path token tamper checks: **0 -> constant-time HMAC validation**.
- Secrets committed to repository: **0 -> 0**.
- Existing avatar API behavior changed: **0**.
- Vayu cognition responsibility moved to KUPPA: **0**.
- Approval behavior changed: **0**.

## Security/privacy/permission implications
The signing secret is environment-only and never returned. The issued token authorizes only continuity metadata lookup for its bound session ID. It does not authenticate an owner, expose transcript text, expose persona memory, grant tool permissions, or bypass consequential-action approval gates. The legacy endpoint remains a known temporary compatibility risk until UI migration.

## Known limitations
- This is possession-based session hardening, not owner authentication.
- The current avatar UI still uses the legacy recovery endpoint until a dedicated UI cycle migrates it.
- Cross-device continuity still requires a real owner/device authentication and enrollment model.
- Token revocation is currently coarse: rotate the signing secret or wait for expiry.
- XSS/CSP hardening remains separate work.

## Failures/fallbacks tested
Focused tests cover weak secret, tampered session, tampered signature, and expiry. When secure continuity is not configured, issuance/secure lookup fail closed; the existing UI path remains usable rather than breaking KUPPA.

## Rollback procedure / known-good reference
Do not promote this candidate unless full CI is green. Current runtime rollback/known-good remains `33ad4d0b1c76bf7886f33d165b5fee1a4da989b3` (CI #113). Because the change is additive and schema-free, rollback requires only returning the runtime branch to that commit.

## Risks / technical debt introduced or removed
Removed: absence of any server-verifiable continuity possession primitive. Remaining: legacy unauthenticated recovery endpoint, lack of owner authentication, no per-token revocation store, and browser-side credential migration.

## Dependencies
No new application dependency; uses JDK/JCA HMAC implementation.

## Screenshots / visual references
None. This Heart-side change intentionally does not alter the avatar UI.

## Follow-up work
Migrate the avatar to request and store the server-issued continuity credential, use only the secure recovery endpoint, then deprecate the legacy endpoint after regression testing. After that, add authenticated owner/device enrollment for cross-device cloud continuity.

## Next evolution target
Body/UI cycle: migrate same-browser continuity recovery from locally generated session IDs to server-issued signed continuity credentials without adding a conversation window or exposing transcripts.

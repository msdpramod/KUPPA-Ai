# 2026-08-29 15:00 UI — Owner-device authorized continuity migration

## Purpose and hypothesis
Migrate KUPPA's avatar-first browser from browser-generated continuity identity toward the existing owner/device-authorized signed continuity contract, while preserving a graceful local fallback. Hypothesis: a visible trusted-device state plus signed recovery will make refresh continuity safer for cloud deployment without moving semantic reasoning into KUPPA or breaking existing local use.

## Architectural context
KUPPA remains the HEART: identity, trust, relationship continuity, voice/avatar presence and interaction. Vayu remains the BRAIN: semantic interpretation, reasoning, planning, retrieval, tool selection, specialist-agent orchestration and execution strategy. Preflight inspected governed branch `5c209d46a2120424744c16d05da321b608859944`, `docs/evolution/BASELINE.md`, the 2026-08-29 03:00 device-signing evolution, the evolution index, and the existing UI contract test. The validated runtime before this change is `b88adffb3bd44985bb38feb40c868050aaba70bf` with CI #121 green.

## Detailed changes
- Added a compact continuity trust indicator to the avatar-first UI: local vs trusted-device signed continuity.
- Added explicit `Trust this device` and `Forget trust` controls without restoring a conversation window.
- Owner enrollment sends the enrollment key once through `X-KUPPA-Owner-Enroll-Key`; the key is never written to local storage by the UI.
- Stores only the issued owner-device possession credential (`deviceId` + device token) locally after successful enrollment.
- Uses `POST /api/chat/session/owner` with `X-KUPPA-Device-Token` to obtain a server-issued continuity session.
- Uses `GET /api/chat/resumable/secure` with `X-KUPPA-Continuity-Token` for signed resumable-turn recovery.
- On an expired secure continuity credential, retries once by issuing a fresh owner-authorized continuity session from the still-valid device credential.
- On an invalid/expired device credential (`401`), removes the stored owner-device credential and falls back to local browser continuity rather than pretending the device is still trusted.
- When owner identity or secure continuity is not configured/reachable, existing opaque local session continuity remains available.
- Added browser events for secure continuity readiness and explicit trust removal.
- Preserved correlation-aware cancellation, Vayu v3 turn modes, voice barge-in, Vayu health presence, and approval-gated external actions.

## Files/components affected
- `src/main/resources/static/index.html`
- `src/test/java/ai/kuppa/ui/AvatarBrainPresenceContractTest.java`
- `docs/evolution/2026/08/2026-08-29-1500-owner-device-continuity-ui.md`
- `docs/evolution/README.md`
- `CHANGELOG.md`

## Behavior before
The avatar generated `kuppa.clientSessionId.v1` in the browser and recovered the latest resumable turn through the unsigned legacy `/api/chat/resumable` endpoint. The owner/device and signed continuity APIs existed but the avatar did not consume them.

## Behavior after
A user can explicitly trust the current browser device using the configured owner enrollment secret. KUPPA stores the resulting device possession credential, exchanges it for a signed continuity session, and uses the secure resumable endpoint. The UI visibly reports `Continuity · trusted device`. Removing trust clears the device and signed-continuity credentials. If secure identity is unavailable, KUPPA continues with `Continuity · local` and the previous browser-local recovery path.

## KUPPA/Vayu responsibility impact
KUPPA HEART gains only interaction-facing identity and continuity transport. It does not inspect message meaning to choose continuation semantics, does not reason over parent turns, and does not select models/tools/agents. Vayu BRAIN responsibilities are unchanged.

## API/event/schema/config/migration changes
No new backend API, schema, configuration or runtime dependency is introduced. The UI begins consuming existing APIs: `POST /api/chat/owner/device`, `POST /api/chat/session/owner`, and `GET /api/chat/resumable/secure`. Existing `/api/chat/resumable` remains the fallback. New browser events: `kuppa-secure-continuity-ready` and `kuppa-device-trust-forgotten`.

## Tests/build/lint/smoke checks
Pre-change CI baseline is green on #121. Local repository cloning is unavailable in this execution environment because `github.com` DNS resolution fails, so no local Maven result is claimed. A focused UI contract regression test was extended to verify the secure endpoints/headers, local fallback, one-shot renewal path, explicit forget path, and preservation of approval UI. The implementation will be isolated on an evolution branch and must not be promoted until GitHub Actions completes successfully.

## Before/after metrics
- Avatar secure continuity paths: 0 -> 1 owner-device-authorized signed path.
- Visible continuity trust states: 0 -> 2 (`local`, `trusted device`).
- Explicit device trust controls: 0 -> 2 (trust / forget).
- Secure resumable endpoint consumption: 0 -> 1.
- Secure continuity renewal retry paths: 0 -> 1 bounded retry.
- Conversation windows: 0 -> 0.
- Semantic classifiers in KUPPA: 0 -> 0.
- Vayu cognition changes: 0.
- Approval-gate changes: 0.
- Backend schema/dependency changes: 0.

## Security/privacy/permission implications
The owner enrollment key is entered only for explicit device enrollment, sent as a request header, and is not persisted by the UI. The issued device token and continuity token are possession credentials stored in browser local storage, which means browser/XSS compromise can steal them; this is not hardware attestation and is not equivalent to passkeys or OIDC. `Forget trust` clears these local credentials. Consequential external actions remain independently approval-gated.

## Failures/fallbacks tested
Contract coverage asserts the secure owner-session and secure-resumable paths, device/continuity headers, local fallback path, expired-continuity renewal hook, explicit trust removal, and unchanged pending-approval surface. Runtime CI is required before promotion. Invalid device credentials are designed to clear trust and fall back locally; unavailable secure configuration leaves local continuity intact.

## Known limitations
Device tokens remain browser-stored possession credentials. There is still no server-side per-device revocation registry, hardware-bound attestation, passkeys/OIDC, or cross-device enrollment ceremony. The browser prompt is intentionally minimal and should later become a proper local-only pairing/enrollment surface. Local fallback remains unsigned by design for backward compatibility and should be retired only after secure owner identity is universally configured.

## Rollback
Return the runtime to `b88adffb3bd44985bb38feb40c868050aaba70bf` or governed pre-change head `5c209d46a2120424744c16d05da321b608859944`. This UI-only migration adds no database schema and can be rolled back without data migration.

## Risks / technical debt
Local storage remains a weak place for long-lived possession credentials. The secure continuity token is refreshed on startup from the owner-device token, but true device revocation is still absent. The legacy unsigned continuity path remains available as a compatibility fallback.

## Dependencies
No new runtime dependency. Uses existing browser APIs and existing KUPPA owner-device/continuity endpoints.

## Screenshots / visual references
No binary screenshot is committed in this connector run. The visual change is deliberately small: a status chip beside the existing continuity controls plus Trust/Forget actions, preserving the avatar-first layout.

## Follow-up work
Heart: persistent device revocation and auditable renewal. UI: replace the temporary enrollment prompt with a safer pairing flow and migrate device credentials away from general local storage when a stronger browser/device credential primitive is introduced.

## Next evolution target
Persistent per-device revocation plus an auditable renewal/re-enrollment path, followed by a safer pairing UX that can eventually retire unsigned local continuity.

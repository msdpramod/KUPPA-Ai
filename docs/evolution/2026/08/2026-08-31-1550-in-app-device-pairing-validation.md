# 2026-08-31 15:50 — UI / Human Interaction — In-app device pairing validation

## Cycle
UI / Human Interaction — documentation-only validation closeout.

## Why this commit exists
Implementation `2e3f4c2575bba55af3fedec87db6b78253c309f9` replaced KUPPA's native owner enrollment/management credential prompts with in-app pairing and unlock forms. It was intentionally held off the governed runtime until GitHub Actions completed. CI #149 is now green, so this commit records the evidence and promotes the implementation as the new known-good candidate. No runtime code changes are made here.

## Architectural context
KUPPA remains the HEART: identity, trust presentation, relationship continuity, avatar/voice presence and human interaction. Vayu remains the BRAIN: reasoning, planning, retrieval, provider/tool/agent orchestration and execution strategy. No cognition boundary changed.

## Validated implementation
- `Pair this device` replaces the old owner-enrollment browser prompt through a listener-clean DOM replacement.
- Pairing uses an accessible KUPPA dialog with device label and password-style enrollment-key input.
- The enrollment key is sent once using the existing `X-KUPPA-Owner-Enroll-Key` header and the field is cleared after attempts.
- Trusted Devices management uses an in-sheet password field instead of `window.prompt`.
- The owner-management key remains page-memory-only while the sheet is open and its field is cleared.
- Existing metadata-only inventory, `Forget on this browser`, `Revoke everywhere`, device revocation confirmation, secure-continuity bootstrap and local fallback are preserved.
- `kuppa-device-paired` emits only non-secret metadata.

## Files/components affected
This closeout changes only governance documentation: this record, `docs/evolution/README.md`, `docs/evolution/BASELINE.md`, and `CHANGELOG.md`. Runtime code remains implementation `2e3f4c2575bba55af3fedec87db6b78253c309f9`.

## Behavior before / after
Before: owner enrollment and device-management unlock used native browser credential prompts. After: both flows use KUPPA-owned, labelled in-app forms with explicit secret lifetime messaging while preserving the same backend trust boundaries.

## KUPPA/Vayu responsibility impact
None beyond KUPPA trust UX. Vayu cognition and orchestration remain unchanged.

## API/event/schema/config/migration changes
No new backend API, schema, configuration, migration or runtime dependency. Existing trust APIs are reused. Browser event added by the validated implementation: `kuppa-device-paired`.

## Tests/build/lint/smoke evidence
- Pre-change known-good runtime `0f57af0525ea869a0fc853e51045f25ea2ab85a1`: CI #146 green.
- Implementation `2e3f4c2575bba55af3fedec87db6b78253c309f9`: GitHub Actions CI #149 completed successfully.
- CI #149 steps: checkout passed; Java setup passed; full Maven `Test` passed; cleanup passed.
- `TrustedDevicesUiContractTest` validates metadata-only management, explicit local/global trust actions, in-app pairing controls, enrollment API/header usage, clearing of enrollment/management inputs, absence of native `window.prompt`, and absence of owner enrollment/management secret storage keys.
- Existing full Maven suite remained green, preserving Vayu gateway, memory, voice, avatar and approval regressions covered elsewhere.

## Before/after metrics
| Metric | Before | Validated after |
|---|---:|---:|
| Native credential prompts in trusted-device module | 2 | 0 |
| In-app trust credential forms | 0 | 2 |
| Enrollment/management secrets persisted in browser storage | 0 | 0 |
| Conversation windows added | 0 | 0 |
| Vayu cognition changes | 0 | 0 |
| Personality/memory changes | 0 | 0 |
| Approval-gate changes | 0 | 0 |
| New runtime dependencies/schema changes | 0 | 0 |
| Build stability | CI #146 green | CI #149 green |

## Security/privacy/permission implications
Secret entry is now explicit and bounded inside KUPPA rather than native prompts. Enrollment and management secrets are not persisted by the UI and are cleared from form fields. The issued owner-device possession token still remains in localStorage and is therefore vulnerable to browser/XSS compromise; this remains an interim bearer-token model, not passkey/WebAuthn-grade security. Consequential external actions retain their existing approval gates.

## Failures/fallbacks validated
Missing enrollment key and missing management key are bounded in-sheet failures. Rejected/unconfigured enrollment and management authentication remain fail-closed through existing backend contracts. Local continuity remains available when secure owner identity is unavailable. Remote revoke retains explicit confirmation.

## Constitution/regression impact
The KUPPA Constitution is unchanged. HEART/BRAIN separation, confidence-aware memory, Vayu Brain Gateway v3, cancellation, degraded brain presence, voice barge-in and consequential-action approvals remain intact. No material regression was observed in CI #149.

## Known limitations
- Device possession credential remains in general browser localStorage.
- Owner authentication still uses static shared secrets.
- Pairing reloads after successful enrollment so the existing secure-continuity bootstrap can establish the signed session.
- Runtime asset injection remains transitional technical debt.
- No passkey/WebAuthn, hardware attestation or OIDC-grade owner identity yet.

## Rollback
Return to governed runtime head `11cabca8dbc9ff94dda5e5a37386fce82710f724`, whose validated runtime is `0f57af0525ea869a0fc853e51045f25ea2ab85a1` on CI #146. No database rollback is required.

## Risks / technical debt
The UX improvement could make the existing shared-secret model feel stronger than it is; documentation and UI deliberately avoid that claim. The real security target remains phishing-resistant owner authentication and browser-bound credentials.

## Dependencies
None added.

## Follow-up / next evolution target
Heart: add a typed owner-authenticated trust-history API and begin replacing static owner-management authentication. UI: move pairing toward passkeys/WebAuthn and remove long-lived bearer-device credentials from localStorage.

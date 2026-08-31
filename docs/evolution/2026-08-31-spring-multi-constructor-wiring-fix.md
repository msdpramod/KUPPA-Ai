# Spring multi-constructor wiring fix — 2026-08-31

## Problem

KUPPA AI compiled successfully but failed during `mvn spring-boot:run` because Spring 7 encountered `@Service` classes with multiple constructors and no explicitly selected injection constructor. Spring then attempted to use a missing no-argument constructor.

The first reported failure was `ContinuitySessionService`; after fixing that bean, startup progressed and exposed the same pattern in `OwnerDeviceIdentityService`.

## Fix

The production constructors for these multi-constructor Spring services are now explicitly annotated with `@Autowired`:

- `ContinuitySessionService`
- `OwnerDeviceIdentityService`
- `OwnerDeviceTrustService`

The secondary constructors remain available for deterministic unit testing with injected `Clock` instances.

## Regression protection

`SpringConstructorWiringTest` verifies that each multi-constructor service keeps an explicit Spring injection constructor. This prevents a future refactor from compiling successfully while silently reintroducing the startup failure.

## Compatibility and safety

This change does not alter HTTP contracts, token formats, HMAC algorithms, owner/device security semantics, TTL defaults, persistence behavior, or approval boundaries. It only makes Spring bean construction explicit.

## Validation

Run locally from the repository root:

```bash
git checkout agent/avatar-ui
git pull
rm -rf target
mvn clean test
mvn spring-boot:run
```

A future improvement should add an ApplicationContext smoke test so CI validates complete Spring startup, not only unit-test execution.

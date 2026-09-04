# Continuity session startup fix — 2026-08-31

## Problem

`mvn clean compile` succeeded, but `mvn spring-boot:run` failed during ApplicationContext startup.

Spring reported that `ContinuitySessionService` could not be instantiated because no default constructor existed.

## Root cause

`ContinuitySessionService` intentionally has two constructors:

1. A public configuration constructor that receives `@Value` properties.
2. A package-private constructor used by deterministic tests with an injected `Clock`.

With multiple constructors and no explicit autowire marker, Spring 7 did not select the public configuration constructor and instead attempted no-arg instantiation.

## Fix

The public configuration constructor is now explicitly annotated with `@Autowired`.

A regression test verifies that the constructor remains explicitly selectable by Spring.

## Compatibility

No API contract, session-token format, TTL default, signing algorithm, or security boundary changed.

## Risk

Low. The change only makes Spring's constructor-selection intent explicit.

## Validation

- Existing unit behavior remains unchanged.
- CI should run the full Maven test suite on the updated branch.
- Local validation should run `mvn clean test` followed by `mvn spring-boot:run`.

## Follow-up

Add a lightweight ApplicationContext smoke test so future bean-wiring failures are caught by CI before runtime.

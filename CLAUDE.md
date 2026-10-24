# Android AI Agents SDK

Open-source Kotlin/Android SDK: a developer-experience layer for building AI agents on Gemini
(and later Google ADK). Multi-module Gradle project, `minSdk 24`, JVM 11 target.

## Read first

- `specs/CONSTITUTION.md` — the non-negotiable rules (dependency rule, core API stability, tests).
- `docs/ARCHITECTURE.md` — module map, dependency graph, squads.
- `docs/MODULE_SPECS.md` — per-module features, target public API, definition of done.

## Spec-driven development

Non-trivial changes go through `specs/` before code: spec → plan → tasks → implement → verify.
Use the project skills: `/spec-new`, `/spec-plan`, `/spec-tasks`, `/spec-implement`,
`/spec-verify`. See `specs/README.md`. Small fixes (typos, one-line bugs) can skip the spec.

## Module rules (short form)

- `:agents-core` depends only on Kotlin + coroutines; its public API changes additively.
- Every other module depends on `:agents-core` only — never a sibling.
- Only `:agents-ready` and `:app` compose several modules.
- Package `com.aimobilecoders.agents.<name>` for module `:agents-<name>`.
- Dependency versions live only in `gradle/libs.versions.toml`.

## Commands

```bash
./gradlew :<module>:testDebugUnitTest   # unit tests for one module
./gradlew test                          # all unit tests
./gradlew :app:assembleDebug            # build the demo app
```

## Conventions

- Coroutines/Flow for async; network on `Dispatchers.IO`.
- KDoc on all public API; keep internals `internal`.
- Unit tests: JUnit4, no real network (fakes or an injected `OkHttpClient` interceptor).
- Conventional Commits (`feat(agents-gemini): …`). Branch from `dev`.
- Never commit API keys; `GEMINI_API_KEY` lives in git-ignored `local.properties`.

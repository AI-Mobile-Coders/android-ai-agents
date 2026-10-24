# Constitution

The non-negotiable rules every spec, plan, and line of code in this repo must respect.
A spec or plan that breaks one of these is rejected at review, no matter how good the idea.
To change a rule, open an issue and get Core-squad approval first.

Background and reasoning live in [ARCHITECTURE.md](../docs/ARCHITECTURE.md),
[MODULE_SPECS.md](../docs/MODULE_SPECS.md) and [VISION.md](../docs/VISION.md).

## I. The golden dependency rule

- `:agents-core` depends on nothing but Kotlin + coroutines.
- Every other module depends on `:agents-core` and **never on a sibling**.
- Only `:agents-ready` and `:app` may compose several modules.
- If two modules need to share something, it becomes an interface or type in `:agents-core`.

## II. `:agents-core` is a contract

- Its public API is treated as frozen. Changes are **additive only** unless the spec
  explicitly calls out a breaking change and the Core squad approves it.
- Every spec that touches core lists the exact public types/functions it adds or changes.

## III. Small public surface

- Public API is the minimum needed for the spec's user stories. Everything else is `internal`.
- Every public class and function has KDoc.

## IV. Provider details stay in the provider

- Only the provider module (e.g. `:agents-gemini`) knows its HTTP/JSON format.
- Provider errors are mapped onto core's typed exceptions (`AgentException` hierarchy).

## V. Tests without network

- Every behaviour in a spec's acceptance criteria has a unit test.
- Unit tests never hit the real network. Use fakes (`FakeLlmClient`, a fake client
  implementing a core interface) or an injected `OkHttpClient` with a canned interceptor.

## VI. Coroutines, never block

- Public async APIs are `suspend` or return `Flow`.
- Network and disk work runs on `Dispatchers.IO`; nothing blocks the main thread.

## VII. Safe by default

- Never commit API keys, tokens, or `local.properties`.
- Raw-key (dev) mode keeps its runtime warning. Never log a key or put one in an exception message.

## VIII. One logical change per PR

- One spec → one feature branch → one PR, scoped to the modules the plan lists.
- Commits follow Conventional Commits: `feat(agents-gemini): ...`, `fix(core): ...`.

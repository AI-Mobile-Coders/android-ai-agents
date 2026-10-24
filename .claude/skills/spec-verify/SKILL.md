---
name: spec-verify
description: Phase 5 of spec-driven development - check the implementation against every acceptance criterion in specs/NNN-name/spec.md, run the tests, and mark the spec done or list the gaps. Use when the user says "verify spec NNN", "is spec NNN done", or before opening a PR for a spec.
---

# spec-verify — Phase 5: Verify

Prove the code does what the spec promised.

## Steps

1. Read `spec.md`, `plan.md`, `tasks.md` for the feature.
2. **Tasks** — every box in `tasks.md` is ticked. List any that aren't.
3. **Tests** — run `./gradlew :<module>:testDebugUnitTest` for every module in the plan.
   Report failures verbatim; never call a failing run green.
4. **Acceptance criteria** — build a table: `AC | evidence | result`. Evidence is a test name
   (`file:line`) or a described manual check. A criterion with no evidence is a gap.
5. **Public API** — compare the actual public declarations in touched modules with the plan's
   Public API section. Flag anything public that the plan didn't list.
6. **Constitution** — re-check rules I (no sibling dependencies — read each touched
   `build.gradle.kts`), V (no network in tests) and VII (no secrets).
7. **Outcome**:
   - All green → set spec status to `done`, and update the module's checklist in
     `docs/MODULE_SPECS.md` if the feature is listed there.
   - Gaps → leave status `in progress` and list exactly what's missing.
8. Report the AC table and the outcome.

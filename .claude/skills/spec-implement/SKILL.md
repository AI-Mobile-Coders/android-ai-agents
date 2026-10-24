---
name: spec-implement
description: Phase 4 of spec-driven development - implement specs/NNN-name/tasks.md task by task, writing tests, ticking checkboxes, and keeping code inside the plan's modules and public API. Use when the user says "implement spec NNN" or after /spec-tasks.
---

# spec-implement — Phase 4: Implement

Write the code `tasks.md` asks for — nothing more.

## Before you start

1. Read `spec.md`, `plan.md`, `tasks.md`, and `specs/CONSTITUTION.md`.
2. Set the spec's status to `in progress`.
3. Run the touched modules' tests once to get a baseline:
   `./gradlew :<module>:testDebugUnitTest` for each module in the plan.

## Loop, for each unchecked task in order

1. Do the task. Match the surrounding code's style, naming, and comment density.
2. Stay inside the plan: only the listed modules, only the listed public API. If the task
   turns out to need something the plan didn't cover, **stop and update `plan.md`** (and tell
   the user) before writing that code.
3. Run that module's unit tests. Fix failures before moving on.
4. Tick the box (`- [x]`) in `tasks.md`.

## Rules

- New public API gets KDoc; everything not in the plan's Public API section is `internal`
  or `private`.
- Unit tests never touch the real network (constitution rule V).
- Never put API keys in code, tests, or logs.
- Don't commit unless the user asks. When they do: a feature branch, Conventional Commits,
  scoped to this spec.

## Finish

Report which tasks are done, test results (with any failures verbatim), and anything that
deviated from the plan. Then suggest `/spec-verify NNN`.

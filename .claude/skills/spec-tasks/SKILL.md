---
name: spec-tasks
description: Phase 3 of spec-driven development - break specs/NNN-name/plan.md into an ordered, checkbox tasks.md where every AC has a test task. Use when the user says "tasks for spec NNN" or after /spec-plan is approved.
---

# spec-tasks — Phase 3: Tasks

Turn `plan.md` into `tasks.md`: small, ordered, checkable steps.

## Steps

1. Read `spec.md` and `plan.md` for the feature (argument: number or folder name).
2. Copy `specs/_templates/tasks.md` → `specs/NNN-name/tasks.md`.
3. Write tasks:
   - IDs `T001`, `T002`, … in execution order, grouped by the template's phases
     (setup → core contracts → implementation → tests → docs).
   - Each task names the **file(s)** it touches and is small enough for one focused commit.
   - Mark tasks that can run in parallel (different files, no dependency) with `[P]`.
   - **Every acceptance criterion gets at least one test task**, labelled with its AC id.
   - Contracts (`:agents-core` types) come before the code that uses them.
   - End with the docs/KDoc task and a "`./gradlew test` green for touched modules" task.
4. Cross-check: every plan item appears in some task; no task does work the plan didn't ask for.
5. Report the task count and the order, then hand off to `/spec-implement`.

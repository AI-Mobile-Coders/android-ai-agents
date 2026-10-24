---
name: spec-plan
description: Phase 2 of spec-driven development - write specs/NNN-name/plan.md (modules, exact public API, internal design, AC-to-test mapping, constitution check) from an approved spec.md. Use when the user says "plan spec NNN", "design this spec", or after /spec-new is approved.
---

# spec-plan — Phase 2: Plan

Turn an approved `spec.md` into `plan.md`: the **how**.

## Steps

1. **Locate the spec** — argument is the number or folder name (e.g. `001`). Read `spec.md`.
   If its status is `draft`, stop and ask the user to approve it first (unless they already
   told you to go end to end).
2. **Read the context** — `specs/CONSTITUTION.md`, `docs/ARCHITECTURE.md`, the relevant
   sections of `docs/MODULE_SPECS.md`, and the **current code** of every module you expect
   to touch. Plans must fit the code as it is, not as the docs wish it were.
3. **Copy** `specs/_templates/plan.md` → `specs/NNN-name/plan.md` and fill it in:
   - *Approach*: the solution shape and the main alternative you rejected, with why.
   - *Constitution check*: go rule by rule. Any ⚠️ needs a justification or a plan change.
   - *Modules touched*: respect the golden rule — new shared types go in `:agents-core`,
     composition only in `:agents-ready` / `:app`.
   - *Public API*: exact Kotlin signatures. Prefer additive changes to `:agents-core`.
   - *Internal design*: wire formats (request/response JSON), parsing, threading, errors.
   - *Testing strategy*: a row for **every** AC → the test that proves it. No network in tests.
   - *Risks* and *Out of scope*.
4. **Verify external facts** — if the plan depends on a provider API shape (field names,
   endpoints), check the provider's docs rather than relying on memory, and link them under
   the spec's References.
5. **Report back**: modules touched, public API summary, any ⚠️ items. Ask for approval before
   `/spec-tasks` unless told to continue.

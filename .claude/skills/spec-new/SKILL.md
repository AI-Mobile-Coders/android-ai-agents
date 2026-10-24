---
name: spec-new
description: Start a new feature the spec-driven way - create specs/NNN-name/ and write spec.md (problem, goals, user stories, acceptance criteria). Use when the user proposes a new feature, agent, or capability for the SDK, or says "write a spec" / "spec this".
---

# spec-new — Phase 1: Specify

Turn an idea into `specs/NNN-short-name/spec.md`. A spec describes **what** and **why**, never
**how**: no class names, file paths, or Kotlin in the spec body.

## Steps

1. **Read the ground rules** — `specs/CONSTITUTION.md` and `specs/README.md`. Skim the module's
   section in `docs/MODULE_SPECS.md` so the spec fits the module's stated purpose.
2. **Pick the number** — list `specs/` and take the next free `NNN` (start at `001`). Choose a
   short kebab-case name: `NNN-<module-or-area>-<feature>`.
3. **Copy the template** — `specs/_templates/spec.md` → `specs/NNN-name/spec.md`.
4. **Fill it in** from the user's idea:
   - *Problem*: what's hard today and for whom.
   - *Goals / Non-goals*: be explicit about scope; push nice-to-haves into non-goals.
   - *User stories*: numbered, "As a … I want … so that …". The SDK's users are app developers;
     their end users are the people using the app.
   - *Acceptance criteria*: `AC1`, `AC2`, … — each observable and testable (Given/When/Then).
     Include error and edge cases (empty input, provider failure, nothing found).
   - *Open questions*: anything you genuinely can't decide. Don't invent answers to them.
5. **Status** stays `draft`. Set *Created* to today's date.
6. **Report back**: the path, a 3-line summary, and the open questions. Ask the user to approve
   (status → `approved`) before running `/spec-plan`. If the user has already said to go
   ahead end to end, mark it `approved` and continue.

## Quality bar

- Every user story is covered by at least one acceptance criterion.
- No criterion says "works well" or "is fast" without a measurable meaning.
- If the idea would break a constitution rule, say so in Open questions rather than silently
  speccing around it.

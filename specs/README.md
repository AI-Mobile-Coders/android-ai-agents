# Specs — spec-driven development

Every non-trivial change starts as a written spec, not as code. The spec says **what** and
**why**; the plan says **how**; the task list says **in what order**. Code comes last and is
checked against the spec.

```
 idea ──▶ spec.md ──▶ plan.md ──▶ tasks.md ──▶ code + tests ──▶ verify ──▶ PR
          (what/why)   (how)      (steps)      (one task at a time)  (vs. spec)
```

## Layout

```
specs/
├── CONSTITUTION.md          # rules every spec and plan must respect
├── README.md                # this file
├── _templates/              # copy these to start a feature
│   ├── spec.md
│   ├── plan.md
│   └── tasks.md
└── NNN-short-name/          # one folder per feature, numbered in order
    ├── spec.md
    ├── plan.md
    └── tasks.md
```

Feature folders are numbered `001`, `002`, … in creation order, followed by a short
kebab-case name, e.g. `001-gemini-search-grounding`.

## The phases

| Phase | Output | Answers | Review gate |
| --- | --- | --- | --- |
| 1. Specify | `spec.md` | What problem, for whom, what does "done" look like? No code or class names. | Product/owner agrees the stories and acceptance criteria are right. |
| 2. Plan | `plan.md` | Which modules, which public API, which files, which risks? | Owning squad agrees; Core squad too if `:agents-core` changes. Passes the constitution check. |
| 3. Tasks | `tasks.md` | Ordered, small, testable steps that implement the plan. | Each task maps to a plan item; each acceptance criterion maps to a test task. |
| 4. Implement | code + tests | One task at a time, ticking boxes as you go. | `./gradlew test` green. |
| 5. Verify | updated `spec.md` status | Does the code meet every acceptance criterion? | Every criterion links to a passing test or a manual check. |

## Status values

Each `spec.md` has a status line: `draft` → `approved` → `in progress` → `done`
(or `dropped`). Update it as the feature moves.

## Using Claude Code

The repo ships project skills in `.claude/skills/` that walk through each phase:

| Skill | Phase |
| --- | --- |
| `/spec-new <idea>` | Create the feature folder and write `spec.md` |
| `/spec-plan <NNN>` | Write `plan.md` from an approved spec |
| `/spec-tasks <NNN>` | Break the plan into `tasks.md` |
| `/spec-implement <NNN>` | Work through `tasks.md`, task by task, with tests |
| `/spec-verify <NNN>` | Check the code against the spec and mark it `done` |

You can also do every phase by hand — copy the templates and fill them in.

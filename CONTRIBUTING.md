# Contributing to Android AI Agents SDK

First off — thank you! 🎉 This is a community project, and it gets better with every
contributor. Whether you fix a typo, build a whole new agent, or help someone in an issue,
you're welcome here.

This guide explains how to get set up, where to start, and the conventions we follow.

---

## Code of Conduct

Be kind, be respectful, assume good intent. We want this to be a friendly place for
developers of every experience level. Harassment or disrespect of any kind is not tolerated.
(A full `CODE_OF_CONDUCT.md` will be added; until then, this paragraph is the rule.)

---

## Ways to contribute

You don't have to write code to help:

- 🐛 **Report bugs** — open an issue with steps to reproduce.
- 💡 **Suggest features or agents** — open a discussion or issue.
- 📖 **Improve docs** — the README, this guide, and code comments.
- 🧪 **Add tests** — coverage is always welcome.
- 🧩 **Build an agent** — the highest-impact contribution (see workstreams below).

---

## Project setup

1. **Fork** this repository and **clone** your fork:

   ```bash
   git clone https://github.com/<your-username>/android-ai-agents.git
   cd android-ai-agents
   ```

2. Open the project in **Android Studio** (latest stable).

3. Add a Gemini API key for local testing. Get one from
   [Google AI Studio](https://aistudio.google.com/apikey), then add it to your
   **`local.properties`** (this file is git-ignored — never commit a key):

   ```properties
   GEMINI_API_KEY=your_key_here
   ```

4. Build and run the `:sample` app to confirm everything works.

**Requirements:** JDK 17, `minSdk 24`, Kotlin 2.2+, Jetpack Compose.

---

## Workstreams — pick one to own

Work is split into parallel, independent workstreams so nobody blocks anyone else. Find one
that fits your skills and comment on its tracking issue to get involved.

| Workstream | Module | Good first issues | Skills |
| --- | --- | --- | --- |
| Core & DSL | `:agents-core` | Add a builder option, improve defaults | Kotlin, coroutines, API design |
| Ready-made agents | `:agents-ready` | Build one new agent end to end | Kotlin, prompt design |
| Compose UI | `:agents-compose` | A message bubble, streaming indicator | Jetpack Compose |
| Persistence | `:agents-persistence` | Room entity, session DAO | Room / DataStore |
| Docs & samples | `README`, `:sample` | Write a how-to, add a demo screen | Writing, Android basics |
| Testing & CI | all modules | Unit-test an agent, set up GitHub Actions | JUnit, Turbine, CI |

New to the project? Look for issues labelled
[`good-first-issue`](../../labels/good-first-issue).

---

## Workflow

1. **Claim** an issue by commenting on it, so we don't duplicate work.
2. **Branch** off `main` with a descriptive name:

   ```bash
   git checkout -b feat/summarizer-agent
   ```

3. **Code**, keeping changes focused — one logical change per pull request.
4. **Test and lint** locally before pushing:

   ```bash
   ./gradlew ktlintCheck detekt test
   ```

5. **Commit** using [Conventional Commits](https://www.conventionalcommits.org):

   ```
   feat(agents-ready): add SummarizerAgent
   fix(core): handle empty prompt in ChatAgent
   docs(readme): clarify API-key modes
   ```

6. **Push** and open a **pull request** against `main`. Fill in the PR template, link the
   issue it closes, and add screenshots/GIFs for UI changes.

---

## Coding standards

- **Kotlin style:** enforced by `ktlint`; static analysis by `detekt`. CI must be green.
- **Public API:** keep it small and stable — `:agents-core` is a contract that other modules
  and user apps depend on. Discuss breaking changes in an issue first.
- **Async:** use coroutines and `Flow`; never block the main thread.
- **Tests:** every new agent and every bug fix ships with tests (JUnit; Turbine for `Flow`).
- **Docs:** public classes and functions get KDoc; update the README when behaviour changes.
- **No secrets:** never commit API keys, tokens, or `local.properties`.

---

## Adding a new ready-made agent

The most valued contribution. A good agent PR includes:

1. The agent class in `:agents-ready`, built on `:agents-core`'s DSL.
2. Sensible default instructions/prompts, with clear extension points for customization.
3. Unit tests.
4. A short usage snippet added to the README's agent catalog.
5. A demo added to the `:sample` app (nice to have).

---

## Developer Certificate of Origin (DCO)

By contributing, you certify that you wrote the code or have the right to submit it under the
project's Apache 2.0 license. Sign off your commits with `-s`:

```bash
git commit -s -m "feat(agents-ready): add TranslatorAgent"
```

This appends a `Signed-off-by` line to your commit message.

---

## Communication

- **Issues & Discussions** on GitHub for anything code-related.
- **[AI Mobile Coders](https://aimobilecoders.com)** community for real-time chat, a dedicated
  SDK channel, and a short weekly async update so everyone stays in sync.

---

## License

By contributing, you agree that your contributions will be licensed under the
[Apache License 2.0](LICENSE).

Happy building! 🚀

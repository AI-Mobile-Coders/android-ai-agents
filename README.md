# Android AI Agents SDK

> The fastest way to ship AI agents in your Android app — powered by **Google ADK + Gemini**.

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2-purple.svg)](https://kotlinlang.org)
[![Platform](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com)
[![Status](https://img.shields.io/badge/status-early--development-orange.svg)]()

**Android AI Agents** is an open-source, batteries-included layer on top of Google's official
[ADK for Kotlin & Android](https://developer.android.com/ai/adk). Drop in a Gemini API key,
pick a ready-made agent, and ship — no boilerplate, no agent internals to learn.

```kotlin
val agent = ChatAgent(apiKey = BuildConfig.GEMINI_API_KEY)   // ready-made
val reply = agent.send("Hello!")                             // done
```

> ⚠️ **Early development.** The API is taking shape and will change before `v1.0`. Star and watch the repo to follow along.

---

## Why this SDK?

Google now ships an excellent — but low-level — [ADK for Kotlin/Android](https://github.com/google/adk-kotlin).
Using it directly means wiring up tools, an agent, a runner, and a session service, then collecting an event
stream — and it ships with no ready-made agents, no UI, and only in-memory sessions.

This SDK is the **developer-experience layer above ADK**. Think of the OkHttp → Retrofit relationship:
ADK is the powerful engine, and this SDK is the clean, opinionated layer that makes it a joy to use.
We never fork or compete with ADK — we build on top of it.

| Concern | Google ADK (the engine) | This SDK (the layer on top) |
| --- | --- | --- |
| Setup | Define tools, agent, runner, session, collect events | One builder call, sensible defaults |
| Ready-made agents | None | Chat, Summarizer, Translator, RAG, Voice, and more |
| UI | None | Jetpack Compose components |
| Sessions | In-memory only | Persistent (Room / DataStore) |
| API key on Android | Restricted; wire Firebase yourself | Dev + production modes behind one interface |
| Observability | Minimal | Logging and tracing hooks |

---

## Features

- **3-line agent DSL** — a clean Kotlin builder that hides ADK's boilerplate behind sensible defaults.
- **Ready-made agents** — import an agent and use it directly, zero configuration.
- **Multi-agent orchestration** — ready presets: a router that delegates, plus sequential and parallel pipelines.
- **Compose-first UI** — drop-in chat components with streaming responses.
- **Persistent sessions** — history survives app restarts, via Room / DataStore.
- **Safe by default** — dev and production API-key modes behind a single interface.

---

## Installation

> Not yet published to Maven Central — coordinates below are the planned artifacts for `v1.0`.

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}
```

```kotlin
// app/build.gradle.kts
dependencies {
    implementation("com.aimobilecoders.agents:agents-core:0.1.0")     // DSL + config
    implementation("com.aimobilecoders.agents:agents-ready:0.1.0")    // ready-made agents
    implementation("com.aimobilecoders.agents:agents-compose:0.1.0")  // optional UI
}
```

Until the first release, add the modules as a local project or Git submodule.

---

## Quick start

```kotlin
import com.aimobilecoders.agents.ready.ChatAgent

// 1. Create a ready-made agent
val agent = ChatAgent(apiKey = BuildConfig.GEMINI_API_KEY)

// 2. Send a message (suspend function)
val reply = agent.send("Summarize the theory of relativity in one line.")

// 3. Or stream tokens as they arrive
agent.stream("Write a haiku about Android").collect { token ->
    print(token)
}
```

Drop a full chat screen into Compose:

```kotlin
setContent {
    AgentChatScreen(agent = ChatAgent(apiKey = BuildConfig.GEMINI_API_KEY))
}
```

---

## Ready-made agents

All agents are used in a single call. This catalog grows over time.

| Agent | What it does | One-liner |
| --- | --- | --- |
| `ChatAgent` | General conversational assistant | `ChatAgent(apiKey).send("Hi")` |
| `SummarizerAgent` | Summarize long text or documents | `SummarizerAgent(apiKey).summarize(text)` |
| `TranslatorAgent` | Translate between languages | `TranslatorAgent(apiKey).translate(text, to = "hi")` |
| `RagAgent` | Answer over your own documents | `RagAgent(apiKey, store).ask(q)` |
| `VisionAgent` | Understand images and screenshots | `VisionAgent(apiKey).describe(bitmap)` |
| `VoiceAgent` | Speech in, respond, speech out | `VoiceAgent(apiKey).listen()` |
| `ToolAgent` | Call your own functions (function-calling) | `ToolAgent(apiKey, tools).run(input)` |
| `RouterAgent` | Multi-agent: route to the right sub-agent | `RouterAgent(listOf(a, b, c)).handle(q)` |

---

## Architecture

A small stack of focused Gradle modules, each a clean layer over `google/adk-kotlin`.
Depend only on what you need.

```
Your app / :sample
      │
      ├── :agents-compose   → Jetpack Compose chat UI
      │        │
      ├── :agents-ready     → ready-made agents
      │        │
      ├── :agents-persistence → Room / DataStore sessions
      │        │
      └── :agents-core      → DSL + config + API-key modes
               │
          google/adk-kotlin → agents, tools, runner, sessions
               │
             Gemini          → cloud + on-device Nano
```

| Module | Responsibility |
| --- | --- |
| `:agents-core` | The agent DSL/builder, configuration, and dev/production key modes. |
| `:agents-ready` | The ready-made agent catalog. |
| `:agents-compose` | Jetpack Compose UI: chat screen, message list, input bar. |
| `:agents-persistence` | Durable sessions and history via Room / DataStore. |
| `:sample` | Demo app showcasing every feature. |

`:agents-core` stays dependency-light and API-stable, because everything else rests on it.

---

## API key: dev vs production

Shipping a raw Gemini API key inside a published APK is a security risk — anyone can decompile
the app and extract it. Google's own Android SDK restricts direct key use for this reason.
This SDK supports both modes behind one interface, so switching is a one-line change.

| Mode | How the key is provided | Use it for |
| --- | --- | --- |
| **Dev / prototype** | Raw Gemini API key, with a runtime warning | Hackathons, demos, learning, internal builds |
| **Production** | Firebase AI Logic or a backend proxy — key stays server-side | Published apps on the Play Store |

```kotlin
// Dev
val agent = ChatAgent(apiKey = "AIza...")

// Production
val agent = ChatAgent(backend = FirebaseAiLogic)
```

---

## Roadmap

| Version | Focus | Key deliverables |
| --- | --- | --- |
| `v0.1` | Skeleton + proof of concept | `:agents-core`, the 3-line DSL, a working `ChatAgent`, `:sample` |
| `v0.2` | Agent catalog | `SummarizerAgent`, `TranslatorAgent`, `ToolAgent`; dev/prod key modes |
| `v0.3` | UI + persistence | `:agents-compose` components, `:agents-persistence` Room sessions |
| `v0.4` | Multi-agent | `RouterAgent`, pipelines; `RagAgent`, `VisionAgent` |
| `v0.5` | Hardening | Observability, error/retry patterns, tests, docs site |
| `v1.0` | Stable release | Frozen public API, `VoiceAgent`, published to Maven Central |

After `v1.0`: Kotlin Multiplatform exploration (iOS/desktop), more agents, community presets.

---

## Contributing

Contributions are very welcome — this is a community project. Work is organized into parallel,
independent workstreams, so you can own a piece without blocking anyone else.

| Workstream | Owns | Good first issues | Skills |
| --- | --- | --- | --- |
| Core & DSL | `:agents-core` | Add a builder option, improve defaults | Kotlin, coroutines, API design |
| Ready-made agents | `:agents-ready` | Build one new agent end to end | Kotlin, prompt design |
| Compose UI | `:agents-compose` | A message bubble, streaming indicator | Jetpack Compose |
| Persistence | `:agents-persistence` | Room entity, session DAO | Room / DataStore |
| Docs & samples | `README`, `:sample` | Write a how-to, add a demo screen | Writing, Android basics |
| Testing & CI | all modules | Unit-test an agent, set up GitHub Actions | JUnit, Turbine, CI |

**How to start**

1. Comment on an open [`good-first-issue`](../../labels/good-first-issue) to claim it.
2. Fork the repo and create a feature branch.
3. Run `ktlint` / `detekt` and add tests for new agents.
4. Open a pull request against `main`.

See `CONTRIBUTING.md` (coming soon) for full conventions.

---

## Tech stack

Kotlin · Jetpack Compose · Coroutines & Flow · `minSdk 24` · JVM 17 · JUnit + Turbine ·
`ktlint` / `detekt` · GitHub Actions · Semantic Versioning · Apache 2.0.

---

## License

Apache 2.0 — see [LICENSE](LICENSE).

---

_Built by the [AI Mobile Coders](https://aimobilecoders.com) community._

# Android AI Agents SDK — Module Specifications (Developer Guide)

Oct 13, 2026 · Maintainer: @Anand

## How to use this guide

This is the build spec for every module. Pick the module your squad owns, read its section, and start coding against the **Target public API** — that is the contract other modules expect from you.

**Each module spec has the same shape:**

- **Purpose** — what the module is for, in one line.
- **Status** — `done`, `in progress`, or `to build`.
- **Features to build** — a checklist of concrete capabilities.
- **Target public API** — the Kotlin types and functions to implement (your contract).
- **Tasks** — bite-sized issues, good first ones first.
- **Definition of Done** — when the work is mergeable.

**The golden rule (never break it):** our module depends on `:agents-core` only — never on a sibling module. If you need something another module has, lift its interface into `:agents-core` and depend on that. Only `:agents-ready` and `:app` compose several modules.

**Definition of Done applies to every module:**

1. Public API implemented as specified (keep internals `internal`).
2. Unit tests using `:agents-testing` fakes — no real network in tests.
3. KDoc on every public class and function.
4. `ktlint` / `detekt` clean, CI green.
5. A short usage snippet added to the README (and a demo in `:app` if user-facing).

> This guide pairs with the [Module Architecture & Team Division](ARCHITECTURE.md) doc (ownership, dependency graph) and [VISION](VISION.md) (the why).

## `:agents-core`

**Status:** in progress (v0.1 shipped) · **Squad:** Core · **Depends on:** nothing but Kotlin + coroutines · **Phase:** v0.1

**Purpose:** the contract every other module builds on — the `Agent` model, the `LlmClient` abstraction, the agent DSL, and the cross-cutting contracts for communication (A2A) and UI (A2UI). This module must stay small and API-stable; everyone depends on it.

Core is where the shared ideas live. It decides what an agent actually is, how one agent can hand work to another, and what kinds of UI an agent is allowed to ask for, but it never calls Gemini and never draws anything on screen. Keeping it this plain is deliberate: every other module is written against core's definitions, so they can be built, tested, and shipped on their own without reaching into each other. Because the whole project depends on these definitions, anything you change here is effectively a change to a shared contract, so it is reviewed with extra care.

**Features to build:**

- [x] `LlmClient` interface (generate + stream)
- [x] `Agent`, `AgentConfig`, and the `agent(client) { }` DSL
- [x] A2A contracts: `AgentMessage`, `AgentBus`, `AgentRegistry`
- [x] A2UI contracts: `UiComponent`, `UiEvent`
- [ ] Typed error model: `AgentException`, `LlmException`, `RateLimitException`
- [ ] `AgentContext` carried through a call (user id, session id, metadata)
- [ ] A `ChatMessage`/`Role` model for multi-turn history
- [ ] Binary-compatibility validator wired on the public API

**Target public API:**

```kotlin
interface LlmClient {
    suspend fun generate(prompt: String, systemInstruction: String? = null): String
    fun stream(prompt: String, systemInstruction: String? = null): Flow<String>
}

open class Agent(client: LlmClient, val config: AgentConfig = AgentConfig()) {
    open suspend fun send(message: String): String
    open fun stream(message: String): Flow<String>
}

interface AgentBus { suspend fun send(message: AgentMessage): String }
interface AgentRegistry { fun register(name: String, agent: Agent); fun find(name: String): Agent? }

sealed interface UiComponent { /* Text, Card, Button, Input, ListView */ }
sealed interface UiEvent     { /* Click, Submit */ }
```

**Tasks:**

1. (good first) Add the typed exception hierarchy and map provider errors onto it.
2. (good first) Add `ChatMessage` + `Role` and a `history: List<ChatMessage>` to `AgentConfig`.
3. Add `AgentContext` and thread it through `Agent.send`/`stream`.
4. Wire the binary-compatibility validator so the public API cannot drift silently.

**Definition of Done:** API above implemented, 90%+ unit-test coverage, KDoc complete, no new dependency added beyond coroutines.

## `:agents-gemini`

**Status:** in progress (v0.1) · **Squad:** Providers · **Depends on:** `:agents-core` · **Phase:** v0.1

**Purpose:** talk to Gemini over the Generative Language REST API with a raw API key (dev mode). This is the default provider for prototypes.

This module is what connects your app to Google's Gemini models. You pass it an API key and it handles the real network call: building the request, sending the prompt, reading the response, and returning plain text to the agent. During development it is the fastest way to get a real answer out of the SDK, since all you need is a key from Google AI Studio. It is intentionally the only place that knows Gemini's HTTP format, which is what lets the rest of the SDK stay provider-independent.

**Features to build:**

- [x] `GeminiClient : LlmClient` — non-streaming `generate`
- [x] `geminiAgent(apiKey)` convenience
- [ ] True token streaming via `streamGenerateContent` (SSE parsing)
- [ ] Generation config: temperature, topP, maxOutputTokens, stop sequences
- [ ] Safety settings passthrough
- [ ] Multimodal input (send images/bitmaps in the request)
- [ ] Map HTTP failures to core's typed exceptions; retry with backoff on 429 / 5xx

**Target public API:**

```kotlin
class GeminiClient(
    apiKey: String,
    model: String = DEFAULT_MODEL,
    config: GeminiConfig = GeminiConfig(),
) : LlmClient

data class GeminiConfig(
    val temperature: Float = 0.7f,
    val topP: Float = 0.95f,
    val maxOutputTokens: Int? = null,
)

fun geminiAgent(apiKey: String, model: String = DEFAULT_MODEL, configure: AgentBuilder.() -> Unit = {}): Agent
```

**Tasks:**

1. (good first) Add `GeminiConfig` and send it in the request body.
2. Implement real SSE streaming in `stream()`.
3. Add retry-with-backoff and typed error mapping.
4. Add multimodal (image) support.

**Definition of Done:** streaming + config working against a live key, errors typed, unit tests use `FakeLlmClient` / a mocked HTTP client (no real network in CI).

## `:agents-firebase`

**Status:** to build · **Squad:** Providers · **Depends on:** `:agents-core` · **Phase:** v0.5

**Purpose:** the production key mode. Routes Gemini calls through Firebase AI Logic so the API key stays server-side and never ships in the APK.

This does the same job as the Gemini module, getting answers from the model, but it is meant for apps you actually ship. The problem with putting a raw API key in a released app is that anyone can unpack the APK and pull it out. Firebase AI Logic avoids that by keeping the key on Google's servers: your app talks to Firebase, and Firebase talks to Gemini. The practical guidance for contributors is to keep the dev-key path for building and demos, and move people to this module once they are ready for the Play Store.

**Features to build:**

- [ ] `FirebaseLlmClient : LlmClient` using the Firebase AI Logic SDK
- [ ] `generate` + real streaming
- [ ] `firebaseAgent()` convenience, drop-in with the same `Agent` surface as `geminiAgent`
- [ ] Setup docs: `google-services.json`, enabling Firebase AI Logic, App Check
- [ ] Graceful errors mapped to core's typed exceptions

**Target public API:**

```kotlin
class FirebaseLlmClient(
    model: String = "gemini-flash-latest",
    config: GenerationConfig = GenerationConfig(),
) : LlmClient

fun firebaseAgent(model: String = "gemini-flash-latest", configure: AgentBuilder.() -> Unit = {}): Agent
```

**Tasks:**

1. Add the Firebase AI Logic dependency and wire `FirebaseLlmClient.generate`.
2. Add streaming.
3. Write the setup guide (Firebase console + App Check).
4. Add a sample flavor in `:app` that uses the Firebase path.

**Definition of Done:** a developer can switch `ChatAgent` from dev key to Firebase with one line, verified in `:app`; docs cover the Firebase setup.

## `:agents-adk`

**Status:** to build · **Squad:** Providers · **Depends on:** `:agents-core` · **Phase:** v0.5

**Purpose:** a bridge to Google's official [`adk-kotlin`](https://github.com/google/adk-kotlin). Lets advanced users tap ADK's `LlmAgent`, tool-calling, multi-agent composition, and on-device Gemini Nano — all behind our clean `Agent` surface.

Google maintains its own agent framework for Kotlin called ADK, and it is genuinely capable: tool calling, multi-agent setups, even running a small model directly on the device. This module's job is to let our agents plug into those features without a developer having to learn ADK first; it sits in between our simple API and Google's lower-level one and translates between them. It is not required to use the SDK. It is here for teams that want to go deeper once the basics are in place.

**Features to build:**

- [ ] `AdkAgent` — wraps an ADK `LlmAgent` and exposes our `Agent` API (`send`/`stream`)
- [ ] `AdkToolBridge` — adapt our `Tool` (`:agents-tools`) to ADK `@Tool`, and vice versa
- [ ] On-device option via ADK's ML Kit / Gemini Nano runner
- [ ] Map our `AgentBus` onto ADK's A2A where it helps

**Target public API:**

```kotlin
class AdkAgent(
    adkAgent: com.google.adk.kt.agents.LlmAgent,
) : Agent(/* adapts ADK runner to LlmClient */)

object AdkToolBridge {
    fun fromCore(tool: com.aimobilecoders.agents.core.Tool): /* ADK tool */
}
```

**Tasks:**

1. Spike: wrap an ADK `InMemoryRunner` as an `LlmClient` so `AdkAgent` works with our `Agent`.
2. Build the tool bridge both directions.
3. Add an on-device (Nano) sample path.

**Definition of Done:** an `AdkAgent` runs end to end in `:app`; the tool bridge round-trips a tool call; limitations documented (e.g. Nano tool-calling gaps).

## `:agents-tools`

**Status:** to build · **Squad:** Core · **Depends on:** `:agents-core` · **Phase:** v0.2

**Purpose:** let an agent call your own Kotlin functions (function-calling). Define a tool, register it, and the model can invoke it mid-conversation.

Out of the box a model can only reply with text. This module is what lets it take real actions, such as running a function you wrote to fetch the weather, querying your database, or starting a booking. You write an ordinary Kotlin function, describe what it does, and during a conversation the model can decide to call it, read the result, and work that into its reply. This is the step that turns a plain chatbot into something that can actually get things done.

**Features to build:**

- [ ] `Tool` abstraction: name, description, parameter schema, `suspend fun call(args)`
- [ ] `@AgentTool` / `@Param` annotations + KSP processor to generate schemas from functions
- [ ] `ToolRegistry` to hold and look up tools
- [ ] JSON-schema generation for the provider's function-calling format
- [ ] A `ToolRunner` that parses a model function-call, runs the tool, feeds the result back

**Target public API:**

```kotlin
interface Tool {
    val name: String
    val description: String
    val parameters: JsonSchema
    suspend fun call(args: Map<String, Any?>): String
}

class ToolRegistry {
    fun register(tool: Tool)
    fun all(): List<Tool>
}

@AgentTool("get_weather", "Current weather for a city")
fun getWeather(@Param("city") city: String): String
```

**Tasks:**

1. (good first) Define `Tool` + `ToolRegistry` and a manual (non-annotation) tool example.
2. Build the KSP processor for `@AgentTool` -> schema.
3. Implement `ToolRunner` and integrate with `:agents-gemini` function-calling.
4. Ship 2–3 built-in tools (time, http GET, calculator) as examples.

**Definition of Done:** an agent calls a registered tool end to end in `:app`; annotation + manual paths both work; tests cover schema generation and tool dispatch.

## `:agents-multiagent`

**Status:** shipped · **Squad:** Core · **Depends on:** `:agents-core` · **Phase:** v0.4

**Purpose:** ready orchestration patterns so multi-agent apps don't start from scratch — a router that delegates, and pipelines that chain or fan out agents.

A single agent handles one job well, but real tasks often need a few of them working together, and this module provides those coordination patterns so nobody has to build them by hand. You can set up a router that reads the user's request and forwards it to whichever agent fits best, chain agents so each one's output feeds the next, or send a request to several at once and combine what comes back. The patterns are written against the core agent interface, so the agents being coordinated stay completely independent of each other.

**Features shipped:**

- [x] `RouterAgent` — classify query intent and delegate to specialized sub-agents or `AgentRegistry`
- [x] `AgentPipeline` — feed each agent's output into the next in sequence
- [x] `ParallelAgentGroup` — run agents concurrently in parallel, with flexible result merging / synthesis
- [x] `multiAgent` — top-level unified DSL builder wrapper
- [x] Integrates with core `AgentBus` and `AgentRegistry` for decoupled routing

**Public API:**

```kotlin
open class AgentPipeline(val agents: List<Agent>, config: AgentConfig) : Agent(...)
fun agentPipeline(configure: AgentPipelineBuilder.() -> Unit): AgentPipeline

open class ParallelAgentGroup(val agents: List<Agent>, val aggregator: AgentAggregator, config: AgentConfig) : Agent(...)
fun parallelAgentGroup(configure: ParallelAgentGroupBuilder.() -> Unit): ParallelAgentGroup

open class RouterAgent(val routes: Map<String, Agent>, val resolver: AgentResolver, val fallbackAgent: Agent?, config: AgentConfig) : Agent(...)
fun routerAgent(configure: RouterAgentBuilder.() -> Unit): RouterAgent

fun multiAgent(configure: MultiAgentBuilder.() -> Unit): Agent
```

**Tasks completed:**

1. [x] Implement `AgentPipeline` + tests with fake `LlmClient`.
2. [x] Implement `ParallelAgentGroup` with coroutines + default and custom synthesis aggregators.
3. [x] Implement `RouterAgent` with route resolution and fallback agent.
4. [x] Implement `multiAgent` DSL wrapper and comprehensive test suite.

**Definition of Done:** All multi-agent primitives tested deterministically with fakes and shipped in `:agents-multiagent`.

## `:agents-comm`

**Status:** in progress (skeleton shipped) · **Squad:** Core · **Depends on:** `:agents-core` · **Phase:** v0.4

**Purpose:** the agent-to-agent (A2A) communication layer. One agent reaches another by name or capability through a bus — no direct reference, so modules stay independent.

This is how agents talk to each other. Instead of one agent holding a direct reference to another, which quickly becomes a tangle nobody can maintain, an agent simply addresses a message to a name such as "translator", and the bus finds the right agent and delivers it. The benefit is independence: an agent that delegates work never needs to import, or even know about, the agent doing that work. That decoupling is the main reason separate teams can build their modules in parallel without colliding.

**Features to build:**

- [x] `InProcessAgentBus` — register agents, send a message, route to the target
- [ ] Capability-based routing (`send(capability = "translate", ...)`, not just by name)
- [ ] Typed request/response payloads (not just `String` text)
- [ ] Timeouts, cancellation, and error propagation
- [ ] Pluggable transport interface so a remote/A2A transport can slot in later

**Target public API:**

```kotlin
class InProcessAgentBus : AgentBus, AgentRegistry {
    override fun register(name: String, agent: Agent)
    override fun find(name: String): Agent?
    override suspend fun send(message: AgentMessage): String
}

// planned
interface AgentTransport { suspend fun deliver(message: AgentMessage): String }
```

**Tasks:**

1. (good first) Add capability tags to `register` and route by capability.
2. Add timeouts + structured errors to `send`.
3. Extract an `AgentTransport` interface and make `InProcessAgentBus` one implementation.

**Definition of Done:** two ready agents talk through the bus in `:app`; routing by name and capability tested; the bus never imports a sibling agent module.

## `:agents-rag`

**Status:** to build · **Squad:** Agents · **Depends on:** `:agents-core` · **Phase:** v0.4

**Purpose:** retrieval-augmented generation — answer questions over the developer's own documents. Chunk, embed, store, retrieve, then ground the agent's answer.

A model knows a lot about the world in general but nothing about your specific files, and RAG closes that gap. You load in your own content, such as notes, a PDF, or a product manual, and when a question comes in, this module finds the passages most relevant to it and passes them to the model alongside the question, so the answer is grounded in your material rather than the model's memory. This is the module behind any feature where a user asks questions about their own documents.

**Features to build:**

- [ ] `Embedder` interface + a Gemini embeddings implementation
- [ ] `DocumentChunker` (split text into overlapping chunks)
- [ ] `VectorStore` interface + an in-memory implementation (Room-backed later)
- [ ] `RagAgent` — retrieve top-k chunks, build a grounded prompt, answer with citations
- [ ] Ingestion helpers for plain text / PDF

**Target public API:**

```kotlin
interface Embedder { suspend fun embed(text: String): FloatArray }

interface VectorStore {
    suspend fun add(id: String, text: String, vector: FloatArray)
    suspend fun search(query: FloatArray, k: Int = 4): List<Chunk>
}

class RagAgent(apiKey: String, store: VectorStore, embedder: Embedder) {
    suspend fun ingest(text: String)
    suspend fun ask(question: String): String
}
```

**Tasks:**

1. (good first) `DocumentChunker` with configurable size + overlap, with tests.
2. In-memory `VectorStore` with cosine similarity.
3. Gemini `Embedder`.
4. `RagAgent` tying it together, with citation formatting.

**Definition of Done:** `RagAgent` answers over a sample document in `:app`; retrieval tested deterministically; Room-backed store tracked as a follow-up with `:agents-persistence`.

## `:agents-voice`

**Status:** to build · **Squad:** Agents · **Depends on:** `:agents-core` · **Phase:** v1.0

**Purpose:** give agents a voice — speech in, speech out. Wrap Android's speech APIs so a developer gets a talking agent with no plumbing.

This gives an agent the ability to listen and speak. It takes what the user says, turns it into text, sends that to the agent, and then reads the reply back out loud. It wraps Android's own speech recognition and text-to-speech so a developer does not have to deal with that plumbing directly. It is the piece you reach for when typing is not practical, like a hands-free assistant or better accessibility.

**Features to build:**

- [ ] `SpeechToText` — wrap Android `SpeechRecognizer`, expose a `Flow<String>` of partial + final transcripts
- [ ] `TextToSpeech` — wrap Android `TextToSpeech`, suspend until speech finishes
- [ ] `VoiceAgent` — listen -> send to agent -> speak the reply, as a loop
- [ ] Runtime permission handling helper (RECORD_AUDIO)
- [ ] Locale / voice selection

**Target public API:**

```kotlin
interface SpeechToText { fun listen(): Flow<Transcript> }
interface TextToSpeech { suspend fun speak(text: String) }

class VoiceAgent(apiKey: String, stt: SpeechToText, tts: TextToSpeech) {
    fun converse(): Flow<Turn>   // emits user + agent turns
}
```

**Tasks:**

1. (good first) `TextToSpeech` wrapper with a suspend `speak`.
2. `SpeechToText` wrapper emitting a transcript `Flow`.
3. `VoiceAgent` loop + permission helper.

**Definition of Done:** a working voice chat screen in `:app`; graceful handling of denied mic permission and unavailable TTS; instrumented tests for the wrappers.

## `:agents-vision`

**Status:** to build · **Squad:** Agents · **Depends on:** `:agents-core` · **Phase:** v0.4

**Purpose:** let agents understand images — describe a photo, read a screenshot, extract text, answer questions about a picture.

This lets an agent work with images, not just words. Hand it a photo or a screenshot and it can tell you what is in it, answer a question about it, or pull out any text it contains. Behind the scenes it prepares the image and sends it to the model's multimodal endpoint. It is what you would build on for features like reading a receipt, understanding a photo of a document, or describing a picture to the user.

**Features to build:**

- [ ] `VisionAgent` with `describe(bitmap)` and `ask(bitmap, question)`
- [ ] Image encoding/resizing helpers for the multimodal request
- [ ] OCR-style text extraction (`extractText(bitmap)`)
- [ ] Support multiple images in one prompt

**Target public API:**

```kotlin
class VisionAgent(apiKey: String, model: String = DEFAULT_VISION_MODEL) {
    suspend fun describe(image: Bitmap): String
    suspend fun ask(image: Bitmap, question: String): String
    suspend fun extractText(image: Bitmap): String
}
```

**Tasks:**

1. (good first) Bitmap -> base64 + resize/compress helper with tests.
2. `describe` / `ask` over Gemini multimodal (needs `:agents-gemini` multimodal support).
3. `extractText` and a multi-image path.

**Definition of Done:** `VisionAgent` describes a sample image and answers a question about it in `:app`; large-image handling is safe (downscale before send).

## `:agents-ready`

**Status:** in progress (v0.1) · **Squad:** Agents · **Depends on:** `:agents-core` + providers/capabilities · **Phase:** v0.1+

**Purpose:** the flagship — drop-in agents a developer uses in one call. Each is a thin, opinionated preset over core + the needed capability module.

This is the module most people will touch first. Everything the other modules build gets wrapped here into agents you can use in a single line: a `ChatAgent` for conversation, a `SummarizerAgent` for condensing text, a `TranslatorAgent` for languages, with more added over time. Someone who just wants a chatbot in their app should not have to wire up clients and configuration; they import one class and they are done. It is also the easiest place for a contributor to make an impact, since each new agent is a small, self-contained piece of work.

**Features to build:**

- [x] `ChatAgent`
- [ ] `SummarizerAgent.summarize(text, style)`
- [ ] `TranslatorAgent.translate(text, to)`
- [ ] `ToolAgent(tools).run(input)` (needs `:agents-tools`)
- [ ] `RagAgent` re-export (needs `:agents-rag`)
- [ ] `VisionAgent` / `VoiceAgent` re-exports
- [ ] `RouterAgent` re-export (needs `:agents-multiagent`)

**Target public API:**

```kotlin
class ChatAgent(apiKey: String, ...) : Agent(...)
class SummarizerAgent(apiKey: String) { suspend fun summarize(text: String, style: Style = Brief): String }
class TranslatorAgent(apiKey: String) { suspend fun translate(text: String, to: String): String }
```

**Tasks:**

1. (good first) `SummarizerAgent` — a preset with a strong system prompt + tests via `FakeLlmClient`.
2. (good first) `TranslatorAgent`.
3. `ToolAgent` once `:agents-tools` lands.
4. Re-export agents from capability modules so users import them all from `:agents-ready`.

**Definition of Done:** each agent is one line to use, has a README snippet + a demo tile in `:app`, and is covered by fake-based unit tests. This is the best module for new contributors — **one agent per PR**.

## `:agents-compose`

**Status:** in progress (skeleton shipped) · **Squad:** UX · **Depends on:** `:agents-core` · **Phase:** v0.3

**Purpose:** drop-in Jetpack Compose chat UI, so a developer gets a working agent screen without building one.

This is a finished chat screen you can drop straight into an app. You give it an agent and it handles showing the conversation, the text box, and the send button, all built with Jetpack Compose. The point is that nobody should have to rebuild a chat interface from scratch just to try the SDK. Over time it grows to cover things like streaming replies, message styling, and loading and error states.

**Features to build:**

- [x] `AgentChatScreen(agent)` — message list + input + send
- [ ] Message bubbles (user vs agent styling), timestamps
- [ ] Streaming rendering (append tokens as they arrive)
- [ ] Typing / loading indicator
- [ ] Markdown rendering in replies
- [ ] Theming hook (`AgentChatTheme`) and a stateful `ChatViewModel`
- [ ] Empty + error states

**Target public API:**

```kotlin
@Composable
fun AgentChatScreen(
    agent: Agent,
    modifier: Modifier = Modifier,
    theme: AgentChatTheme = AgentChatTheme.default(),
)

class ChatViewModel(agent: Agent) : ViewModel() {
    val messages: StateFlow<List<ChatMessage>>
    fun send(text: String)
}
```

**Tasks:**

1. (good first) Message bubble composable with user/agent styles + preview.
2. Streaming rendering using `agent.stream()`.
3. `ChatViewModel` holding history; hoist state out of the composable.
4. Markdown rendering + loading/error states.

**Definition of Done:** a polished chat screen used by `:app`, previews for each state, works in light/dark, no business logic in composables (lives in the ViewModel).

## `:agents-a2ui`

**Status:** in progress (skeleton shipped) · **Squad:** UX · **Depends on:** `:agents-core` · **Phase:** v0.5

**Purpose:** Agent-to-UI (generative UI). An agent returns `UiComponent`s and this module renders them live in Jetpack Compose, routing `UiEvent`s back — so the agent, not the developer, decides the UI.

Usually the developer designs the screen ahead of time. This turns that around and lets the agent decide the interface while it runs. The agent can describe what it wants shown, like a card, a short form, or a button, and this module renders that as real Compose UI on the spot, then sends any taps or input back to the agent. It is often called generative UI. Where the compose module is a fixed chat layout, this one changes based on whatever the agent asks for.

**Features to build:**

- [x] `A2Ui(components, onEvent)` renderer for `Text`, `Card`, `Button`, `Input`, `ListView`
- [ ] More components in core + here: `Image`, `Row`, `Column`, `Checkbox`, `Slider`, `Divider`
- [ ] A stateful binding: observe `agent.uiState: StateFlow<List<UiComponent>>` and re-render
- [ ] A parser that turns an LLM's structured output (JSON) into `UiComponent`s
- [ ] Theming + accessibility on rendered components

**Target public API:**

```kotlin
@Composable
fun A2Ui(components: List<UiComponent>, modifier: Modifier = Modifier, onEvent: (UiEvent) -> Unit = {})

@Composable
fun AgentUi(agent: Agent)   // observes agent.uiState and renders it

object UiComponentParser { fun fromJson(json: String): List<UiComponent> }
```

**Tasks:**

1. (good first) Add `Image`, `Row`, `Divider` components to core + render them here.
2. `UiComponentParser` (JSON -> `UiComponent`) with tests.
3. `agent.uiState` wiring + `AgentUi` composable.
4. Accessibility pass (content descriptions, touch targets).

**Definition of Done:** an agent drives a small form (input + button + card) rendered by `A2Ui` in `:app`; events reach the agent; JSON parsing tested; components render in light/dark.

## `:agents-persistence`

**Status:** to build · **Squad:** Platform · **Depends on:** `:agents-core` · **Phase:** v0.3

**Purpose:** make conversations survive app restarts. Store sessions and message history (Room), plus light preferences (DataStore).

Without this, a conversation only exists while the app is open, and once it closes everything is gone. This module saves chats and their history to the device using Room, so when the user comes back their earlier conversations are still there. It is essentially the SDK's long-term memory, and it is also where features like clearing history or exporting a conversation will live.

**Features to build:**

- [ ] `SessionStore` interface (the contract may live in core) + `RoomSessionStore`
- [ ] Room entities: `SessionEntity`, `MessageEntity`, DAOs
- [ ] CRUD: create session, append message, load history, delete/clear
- [ ] DataStore for small prefs (selected model, last session)
- [ ] Export / import a session (JSON)

**Target public API:**

```kotlin
interface SessionStore {
    suspend fun create(userId: String): String          // returns sessionId
    suspend fun append(sessionId: String, message: ChatMessage)
    suspend fun history(sessionId: String): List<ChatMessage>
    suspend fun clear(sessionId: String)
}

class RoomSessionStore(context: Context) : SessionStore
```

**Tasks:**

1. (good first) Define `SessionStore` + an in-memory implementation + tests.
2. Room schema (entities + DAOs) and `RoomSessionStore`.
3. Wire a persisted `ChatViewModel` in `:agents-compose` / `:app`.
4. JSON export/import.

**Definition of Done:** chat history in `:app` survives a restart; Room migrations set up; DAO tests pass; store swappable (in-memory vs Room) via the interface.

## `:agents-observability`

**Status:** to build · **Squad:** Platform · **Depends on:** `:agents-core` · **Phase:** v0.2

**Purpose:** see what agents are doing — structured logs and traces for each request, with pluggable sinks. Essential for debugging prompts and measuring latency/cost.

When a prompt gives a strange answer or a call feels slow, you need to see what actually happened. This module records each request: the prompt that went out, the reply that came back, how long it took, how many tokens it used, and any errors, then lets you send that wherever you want, starting with Logcat. It is what makes prompts debuggable and lets a team keep an eye on speed and cost as usage grows.

**Features to build:**

- [ ] `AgentLogger` + `AgentEvent` (request, response, token counts, latency, error)
- [ ] A `Tracer` that spans a full agent call (and nested tool/sub-agent calls)
- [ ] Pluggable `Sink` interface: Logcat sink built-in; room for custom (file, remote)
- [ ] An `Agent` decorator that emits events without the agent knowing
- [ ] Redaction of sensitive content (API keys, user text opt-out)

**Target public API:**

```kotlin
interface Sink { fun emit(event: AgentEvent) }

object AgentLog {
    fun install(vararg sinks: Sink)
}

fun Agent.observed(tag: String = config.name): Agent   // wraps and emits events
```

**Tasks:**

1. (good first) Define `AgentEvent` + `Sink` + a `LogcatSink`.
2. The `Agent.observed()` decorator emitting request/response/latency.
3. Tracing spans across tool and sub-agent calls.
4. Redaction controls.

**Definition of Done:** wrapping any agent with `.observed()` logs a full request trace; no sensitive data leaks by default; zero overhead when no sink is installed.

## `:agents-testing`

**Status:** to build · **Squad:** Platform · **Depends on:** `:agents-core` · **Phase:** v0.2

**Purpose:** make every other module testable without a network. Fakes, builders, and assertions so contributors write fast, deterministic tests.

Tests should not call the real model, since it is slow, costs money, and needs a network, which makes tests flaky. This module provides a stand-in model you can script to return whatever a test needs, so every other module can be checked quickly and get the same result on every run. It is the small toolkit that makes the rest of the SDK genuinely testable, and every module is expected to rely on it.

**Features to build:**

- [ ] `FakeLlmClient` — scripted or rule-based responses, records prompts
- [ ] `TestAgent` — an `Agent` backed by `FakeLlmClient`
- [ ] `RecordingAgentBus` — capture messages for A2A tests
- [ ] Assertions / matchers (e.g. `assertPromptContains`)
- [ ] A coroutine test rule / dispatcher helper

**Target public API:**

```kotlin
class FakeLlmClient(
    private val responder: (prompt: String) -> String = { "fake reply" },
) : LlmClient {
    val prompts: List<String>
}

fun testAgent(responder: (String) -> String = { "ok" }): Agent
```

**Tasks:**

1. (good first) `FakeLlmClient` that records prompts and returns scripted replies.
2. `testAgent()` helper + `RecordingAgentBus`.
3. Assertion helpers and a coroutine test dispatcher rule.

**Definition of Done:** other modules depend on this as `testImplementation` and drop any real-network tests; documented usage in CONTRIBUTING. Note: ship it as a normal library so it can be a `testImplementation` dependency elsewhere.

## `:app` (sample / demo)

**Status:** in progress · **Squad:** UX · **Depends on:** `:agents-ready` (and other modules as demos are added) · **Phase:** v0.1+

**Purpose:** the living showcase. Every feature a squad ships gets a screen here, so a new developer sees the SDK working and copies real usage. This is also the acceptance test for the whole SDK.

This is the working app that puts the whole SDK on display. As each squad finishes a feature, it adds a screen here, so a newcomer can install the app, see things running, and copy the exact code that made it work. It serves two purposes at once: a showcase for anyone evaluating the SDK, and the real end-to-end check that all the modules still fit together before a release goes out.

**Features to build:**

- [ ] API-key entry screen (reads `local.properties` / `BuildConfig`, never hard-coded)
- [ ] Home screen listing each demo
- [ ] Chat demo (`ChatAgent` + `AgentChatScreen`)
- [ ] One tile per ready agent (Summarizer, Translator, Vision, Voice…) as they land
- [ ] Multi-agent demo (`RouterAgent`) and an A2A demo (`:agents-comm`)
- [ ] A2UI demo (agent-driven form via `A2Ui`)

**Structure:**

```
:app
 -> HomeScreen (list of demos)
 -> ChatDemoScreen
 -> A2uiDemoScreen
 -> MultiAgentDemoScreen
```

**Tasks:**

1. (good first) Home screen with a demo list + navigation.
2. (good first) Wire the existing `ChatAgent` into a chat demo screen.
3. Add a new demo tile whenever a module ships a user-facing feature (one PR per demo).

**Definition of Done:** `:app` builds and runs; each shipped feature has a working demo; no API key committed; it doubles as the manual end-to-end check before a release.

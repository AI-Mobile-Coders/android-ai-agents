# 🤖 Android AI Agents (Powered by Google ADK Kotlin)

<p align="center">
  <img src="https://img.shields.io/badge/Framework-Google%20ADK%20Kotlin-4285F4?style=for-the-badge&logo=google&logoColor=white" alt="Google ADK Kotlin" />
  <img src="https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Platform: Android" />
  <img src="https://img.shields.io/badge/Language-Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" alt="Language: Kotlin" />
  <img src="https://img.shields.io/badge/License-Apache%202.0-blue?style=for-the-badge&logo=apache" alt="License: Apache 2.0" />
  <img src="https://img.shields.io/badge/PRs-Welcome-brightgreen?style=for-the-badge" alt="PRs Welcome" />
</p>

<p align="center">
  <strong>The definitive open-source collection of production-ready Android AI Agents built exclusively with Google's <a href="https://github.com/google/adk-kotlin">Agent Development Kit (ADK) for Kotlin</a>.</strong>
</p>

---

## 🌟 Overview

**Android AI Agents** is a community-driven repository of specialized, modular, and production-ready AI agents built natively for Android applications using the official **[Google Agent Development Kit (ADK) for Kotlin](https://github.com/google/adk-kotlin)** (`google/adk-kotlin`).

Every agent in this repository leverages the official Google ADK Kotlin DSL, `@Tool` annotations, multi-agent delegation, and Runner infrastructure—enabling seamless execution on **On-Device (Gemini Nano / LiteRT-LM)** as well as **Cloud Gemini** models.

### 🎯 Why Google ADK Kotlin?
- **Android-First & Idiomatic**: Built from the ground up for Kotlin Coroutines, StateFlow, and Jetpack Compose.
- **On-Device & Cloud Hybrid**: Native support for running agents 100% offline via Gemini Nano on Android devices, with cloud fallbacks to Gemini 2.5 Flash / Pro.
- **Type-Safe Tool Calling**: Leverage `@Tool` annotations with KSP code generation for declarative Android OS tool execution.
- **Hierarchical Multi-Agent Systems**: Seamless agent-to-agent delegation and task orchestration.
- **Production Grade**: Standardized session management, memory persistence, structured streaming, and robust error handling.

---

## 📦 Gradle Dependency Setup

Add Google ADK Kotlin dependencies to your app's `build.gradle.kts`:

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp) // Required for ADK @Tool annotation processing
}

dependencies {
    // Google ADK Kotlin Android Core
    implementation("com.google.adk:google-adk-kotlin-core-android:1.0.0")
    ksp("com.google.adk:google-adk-kotlin-processor:1.0.0")

    // Jetpack Compose & Lifecycle
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.kotlinx.coroutines.android)
}
```

---

## 🗂️ Agent Catalog (ADK-Powered)

All agents are structured as reusable, self-contained ADK agent modules:

| Category | Agent Name | Description | ADK Model Runner |
| :--- | :--- | :--- | :--- |
| 👁️ **Vision & Camera** | `VisualInspectorAgent` | Real-time camera & image analyzer for OCR, scene understanding, and inspection using native ADK tools. | Cloud Gemini / On-Device |
| 🎙️ **Voice & Live** | `VoiceActionAgent` | Real-time voice agent executing native Android actions via ADK function calling tools. | Multimodal Live / Gemini Flash |
| ⚡ **On-Device Edge** | `LocalSummaryAgent` | 100% offline text summarization, classification, and drafting with zero network latency. | Gemini Nano (AICore / LiteRT) |
| 🧠 **RAG & Knowledge** | `OfflineDocsRAGAgent` | ADK agent with local vector search & document retrieval tools for in-app knowledge bases. | ADK Retriever + Local SLM |
| 🛠️ **UI & Automation** | `AppNavigatorAgent` | Multi-step agent orchestrating Android Intents, App Navigation, and form automation. | Gemini 2.5 Pro / Flash |
| 🛡️ **Safety & Guardrails** | `ContentGuardAgent` | Client-side input/output validation agent ensuring privacy, PII scrubbing, and safety guardrails. | On-Device LiteRT / Cloud |

---

## 🏗️ Architecture with Google ADK

Each agent is defined using Google ADK Kotlin conventions:

```
┌────────────────────────────────────────────────────────┐
│                   Jetpack Compose UI                   │
│          (Observes StateFlow<AgentUiState>)            │
└───────────────────────────▲────────────────────────────┘
                            │
┌───────────────────────────┴────────────────────────────┐
│                    Android ViewModel                   │
│   ┌────────────────────────────────────────────────┐   │
│   │        Google ADK Agent & Runner Instance       │   │
│   │                                                │   │
│   │   ┌──────────────┐      ┌──────────────────┐   │   │
│   │   │  ADK Session │      │  @Tool Annotated │   │   │
│   │   │   & Memory   │      │  Android Tools   │   │   │
│   │   └──────────────┘      └────────┬─────────┘   │   │
│   └──────────────────────────────────┼─────────────┘   │
└───────────────────────────▲──────────┼─────────────────┘
                            │          │ Executes
            ┌───────────────┴────┐   ┌─▼──────────────────┐
            │   ADK Model Runner │   │ Android System APIs│
            │ (Nano / Gemini Pro)│   │ (Camera, Contacts) │
            └────────────────────┘   └────────────────────┘
```

---

## 🚀 Quick Start Example: Building an ADK Agent

### 1. Define Android Native Tools with `@Tool`

```kotlin
import com.google.adk.annotations.Tool

class AndroidCalendarTools(private val context: Context) {

    @Tool(description = "Schedules an event in the user's Android calendar")
    suspend fun createCalendarEvent(
        title: String,
        startTimeEpochMillis: Long,
        endTimeEpochMillis: Long
    ): String {
        // Safe Android Calendar Provider Intent / ContentResolver invocation
        return "Event '$title' successfully scheduled."
    }
}
```

### 2. Define the Agent with Google ADK DSL

```kotlin
import com.google.adk.agent
import com.google.adk.runner.gemini.geminiModel

val calendarAssistantAgent = agent {
    name = "CalendarAssistant"
    instruction = "You are an intelligent calendar assistant on Android. Help users view and schedule events."
    model = geminiModel(modelName = "gemini-2.5-flash")
    tools = listOf(AndroidCalendarTools(context))
}
```

### 3. Consume in Android ViewModel

```kotlin
@HiltViewModel
class AssistantViewModel @Inject constructor(
    private val calendarAgent: Agent
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    fun sendMessage(prompt: String) {
        viewModelScope.launch {
            calendarAgent.run(prompt).collect { event ->
                when (event) {
                    is AgentEvent.TextChunk -> appendStreamingText(event.text)
                    is AgentEvent.ToolCall -> showToolExecution(event.toolName)
                    is AgentEvent.Complete -> finalizeResponse()
                    is AgentEvent.Error -> handleError(event.throwable)
                }
            }
        }
    }
}
```

---

## 🤝 Contributing to Android AI Agents

We strictly use **Google ADK Kotlin (`google/adk-kotlin`)** for all agent implementations, tool definitions, and multi-agent systems.

### 📌 How to Contribute
1. Read the **[Contributing Guidelines (CONTRIBUTING.md)](CONTRIBUTING.md)**.
2. Review the **[ADK Agent Specification (docs/AGENT_SPECIFICATION.md)](docs/AGENT_SPECIFICATION.md)**.
3. Submit agent proposals via **[Agent Feature Request (.github/ISSUE_TEMPLATE/feature_agent_request.md)](.github/ISSUE_TEMPLATE/feature_agent_request.md)**.
4. Fork the repo, build your ADK agent, and create a Pull Request with our **[PR Template](.github/PULL_REQUEST_TEMPLATE.md)**!

---

## 📂 Repository Structure

```
android-ai-agents/
├── agents/                       # Reusable ADK agent modules
│   ├── vision/                   # ADK Vision & Camera agents
│   ├── voice/                    # ADK Multimodal voice agents
│   ├── ondevice/                 # Gemini Nano / LiteRT ADK agents
│   ├── automation/               # Android OS automation ADK agents
│   └── rag/                      # ADK RAG & Retriever agents
├── core/                         # Shared ADK tools, runners, memory bridges
│   ├── tools/                    # Reusable @Tool Android native tool libraries
│   ├── runners/                  # ADK On-Device (LiteRT/AICore) & Cloud runners
│   └── sessions/                 # ADK Session persistence (Room / DataStore)
├── samples/                      # Jetpack Compose sample apps demonstrating ADK agents
├── docs/                         # Architecture guides & ADK specifications
│   └── AGENT_SPECIFICATION.md    # ADK implementation guidelines
├── .github/                      # Issue & PR templates for ADK contributions
├── CONTRIBUTING.md               # ADK contribution guide
├── CODE_OF_CONDUCT.md            # Contributor Covenant Code of Conduct
└── LICENSE                       # Apache 2.0 License
```

---

## 📄 License

This project is licensed under the **Apache 2.0 License** - see the [LICENSE](LICENSE) file for details.

---

<p align="center">
  Built with ❤️ using <a href="https://github.com/google/adk-kotlin">Google ADK Kotlin</a> for the Android Developer Community.
</p>

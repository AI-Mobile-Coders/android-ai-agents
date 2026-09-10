# 🛠️ Contributing to Android AI Agents (Google ADK Kotlin)

Thank you for contributing to **Android AI Agents**! 🎉 

This project is dedicated to creating the most comprehensive, production-ready library of Android AI Agents built **exclusively using [Google ADK for Kotlin (google/adk-kotlin)](https://github.com/google/adk-kotlin)**.

---

## 📋 Table of Contents

- [Code of Conduct](#-code-of-conduct)
- [ADK Framework Philosophy](#-adk-framework-philosophy)
- [Development Setup](#-development-setup)
- [ADK Agent Standards & Rules](#-adk-agent-standards--rules)
- [Step-by-Step: Adding an ADK Agent](#-step-by-step-adding-an-adk-agent)
- [Writing Android Native Tools with `@Tool`](#-writing-android-native-tools-with-tool)
- [Multi-Agent Delegation with ADK](#-multi-agent-delegation-with-adk)
- [Testing & Quality Requirements](#-testing--quality-requirements)
- [Commit Message Guidelines](#-commit-message-guidelines)
- [Pull Request Process](#-pull-request-process)

---

## 🤝 Code of Conduct

All contributors are expected to uphold our [Code of Conduct](CODE_OF_CONDUCT.md).

---

## ⚡ ADK Framework Philosophy

All agent code in this repository **MUST** adhere to the following principles:

1. **Google ADK Kotlin First**: All agents must use official `com.google.adk` APIs, Kotlin DSLs, and `@Tool` annotations. Do not use non-standard agent abstractions or direct unstructured HTTP wrappers.
2. **Android-First Engineering**: Respect Android lifecycles, memory constraints, network transitions, and battery usage.
3. **On-Device Ready**: Wherever possible, provide configurations that run on **Gemini Nano / LiteRT-LM** via ADK's Android runners, with cloud fallbacks to Gemini Pro/Flash.
4. **Declarative & Modular**: Write self-contained agent modules that Android developers can easily integrate into their own dependency injection (Hilt/Koin) graphs.

---

## 💻 Development Setup

### Prerequisites
- **Android Studio Ladybug (or newer)**
- **JDK 17 or JDK 21**
- **Kotlin 2.0+** with KSP enabled
- **Android SDK** (Min SDK 26, Target SDK 34+)

### Dependencies
Every agent module uses the official Android ADK artifacts:
```kotlin
// build.gradle.kts (Module level)
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
}

dependencies {
    implementation("com.google.adk:google-adk-kotlin-core-android:1.0.0")
    ksp("com.google.adk:google-adk-kotlin-processor:1.0.0")
}
```

---

## 🏛️ ADK Agent Standards & Rules

### 1. Agent Declaration Standard
Use the idiomatic Google ADK `agent { ... }` builder DSL:

```kotlin
package com.android.agents.vision

import com.google.adk.Agent
import com.google.adk.agent
import com.google.adk.runner.gemini.geminiModel

fun createVisualInspectorAgent(
    tools: List<Any>,
    modelName: String = "gemini-2.5-flash"
): Agent = agent {
    name = "VisualInspectorAgent"
    instruction = """
        You are an expert visual inspection agent running on Android.
        Analyze input images, extract structured details, and invoke available tools when actions are required.
    """.trimIndent()
    model = geminiModel(modelName = modelName)
    this.tools = tools
}
```

### 2. ViewModel & Coroutine Integration
Agents should emit reactive streams (`Flow<AgentEvent>`) that map cleanly into Jetpack Compose UI state:

```kotlin
@HiltViewModel
class InspectorViewModel @Inject constructor(
    private val visualAgent: Agent
) : ViewModel() {

    private val _uiState = MutableStateFlow<InspectorUiState>(InspectorUiState.Idle)
    val uiState: StateFlow<InspectorUiState> = _uiState.asStateFlow()

    fun inspectImage(imageBitmap: Bitmap, prompt: String) {
        viewModelScope.launch {
            visualAgent.run(
                prompt = prompt,
                media = listOf(imageBitmap.toAdkPart())
            ).collect { event ->
                // Process ADK streaming events
            }
        }
    }
}
```

---

## 🚀 Step-by-Step: Adding an ADK Agent

### Step 1: Open an Agent Proposal Issue
Create an [Agent Proposal Issue](.github/ISSUE_TEMPLATE/feature_agent_request.md) describing the agent's purpose, ADK tools required, and target models.

### Step 2: Choose the Module Location
Place your agent in the proper directory under `agents/`:
- `agents/vision/` : Camera, multimodal vision, OCR.
- `agents/voice/` : Real-time voice, speech agents.
- `agents/ondevice/` : On-device Gemini Nano / LiteRT agents.
- `agents/automation/` : Android OS intents, notifications, accessibility automation.
- `agents/rag/` : Document search & retrieval agents.

### Step 3: Implement the ADK Module
Each agent module should follow this structure:
```
agents/<category>/<agent-name>/
├── <AgentName>Factory.kt         # ADK agent { ... } builder definition
├── <AgentName>Tools.kt           # @Tool annotated Android tool classes
├── <AgentName>State.kt           # UI State mapping models
├── prompts/                      # System instructions & prompt templates
├── README.md                     # Documentation with usage & Jetpack Compose sample
└── src/test/                     # ADK unit tests (mock runner / test tools)
```

### Step 4: Write Agent Documentation
Include a dedicated `README.md` for the agent specifying:
- Overview and capabilities
- Required Android permissions
- ADK configuration and sample Jetpack Compose code
- Model compatibility (Gemini Nano, Gemini Flash, etc.)

---

## 🔧 Writing Android Native Tools with `@Tool`

Tools in ADK Kotlin use the `@Tool` annotation processed via KSP:

1. **Keep Tool Methods Clean and Focused**:
   ```kotlin
   import com.google.adk.annotations.Tool

   class DeviceBatteryTools(private val context: Context) {

       @Tool(description = "Returns current battery percentage and charging status")
       fun getBatteryStatus(): String {
           val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
           val level = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
           return "Battery level is $level%"
       }
   }
   ```

2. **Handle Android Permissions Gracefully**: If a tool requires a dangerous permission, check permissions and return a descriptive message to the agent if permission is missing.
3. **Idempotency & Destructive Actions**: Destructive operations (sending SMS, deleting data, making calls) must require user confirmation before committing.

---

## 🤝 Multi-Agent Delegation with ADK

Google ADK Kotlin makes sub-agent delegation straightforward. You can add sub-agents directly to the parent agent's `subAgents` list:

```kotlin
val parentAssistant = agent {
    name = "ExecutiveAssistant"
    instruction = "Coordinate tasks across calendar and notes."
    subAgents = listOf(calendarAgent, notesAgent)
}
```

---

## 🧪 Testing & Quality Requirements

All PRs must satisfy:
- **ADK Unit Tests**: Test agent tool execution, instruction following, and error recovery using mock ADK runners.
- **Code Formatting**: Run `./gradlew ktlintCheck` and `./gradlew ktlintFormat`.
- **Detekt Analysis**: Run `./gradlew detekt` with zero critical warnings.
- **KDoc Documentation**: All public classes, functions, and `@Tool` methods must be documented.

---

## 📝 Commit Message Guidelines

Use [Conventional Commits](https://www.conventionalcommits.org/):
- `feat(agent): add VisualInspectorAgent using ADK Kotlin`
- `feat(tools): add Android ContactsLookupTool for ADK`
- `fix(runner): handle network timeout in Gemini runner flow`
- `docs(readme): update ADK setup instructions`

---

## 🔄 Pull Request Process

1. Create a feature branch: `git checkout -b feature/adk-agent-name`.
2. Ensure `./gradlew test` passes.
3. Submit a PR using the [Pull Request Template](.github/PULL_REQUEST_TEMPLATE.md).
4. Maintainers will review your PR and provide feedback.

Happy Agent Building with Google ADK Kotlin! 🚀

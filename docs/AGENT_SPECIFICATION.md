# 📐 Google ADK Kotlin Agent Specification & Blueprint

This document details the architectural specifications and conventions for implementing AI agents using **[Google Agent Development Kit (ADK) for Kotlin](https://github.com/google/adk-kotlin)** (`google/adk-kotlin`) within the **Android AI Agents** repository.

---

## 🎯 Architectural Overview

Agents are built using Google ADK Kotlin's declarative domain-specific language (DSL), `@Tool` annotations, and Kotlin Coroutines/Flows. They connect seamlessly with Jetpack Compose ViewModels while supporting both on-device (Gemini Nano / LiteRT) and cloud (Gemini Pro / Flash) runners.

```
+--------------------------------------------------------------------+
|                         Android Application                        |
|                     (Jetpack Compose / ViewModel)                  |
+---------------------------------+----------------------------------+
                                  |
            agent.run(prompt)     |   Flow<AgentEvent>
                                  v
+--------------------------------------------------------------------+
|                    Google ADK Kotlin Agent Core                    |
|                                                                    |
|  +-------------------+  +--------------------+  +---------------+  |
|  |    ADK Session    |  |  Agent Instruction |  | @Tool Annot.  |  |
|  |     & Memory      |  |    & Sub-Agents    |  | Tool Registry |  |
|  +-------------------+  +--------------------+  +-------+-------+  |
+---------------------------------+-----------------------+----------+
                                  |                       |
                                  v                       v
                    +---------------------------+ +------------------+
                    |      ADK Model Runner     | | Android OS APIs  |
                    | (Gemini Nano / Cloud Pro) | |(Camera, DB, etc.)|
                    +---------------------------+ +------------------+
```

---

## 🧩 ADK Agent Implementation Blueprint

### 1. Agent Factory Definition

Every agent module exposes a clean factory function returning an ADK `Agent`:

```kotlin
package com.android.agents.automation

import android.content.Context
import com.google.adk.Agent
import com.google.adk.agent
import com.google.adk.runner.gemini.geminiModel

object AppNavigatorAgentFactory {

    fun create(
        context: Context,
        modelName: String = "gemini-2.5-flash",
        customTools: List<Any> = emptyList()
    ): Agent {
        val defaultTools = listOf(
            AndroidNavigationTools(context),
            AndroidNotificationTools(context)
        )

        return agent {
            name = "AppNavigatorAgent"
            instruction = """
                You are an autonomous Android navigation and task agent.
                You help users interact with apps, manage alerts, and trigger workflows.
                Always verify tool parameters before execution and provide friendly progress updates.
            """.trimIndent()
            
            model = geminiModel(modelName = modelName)
            tools = defaultTools + customTools
        }
    }
}
```

---

### 2. Native Android Tools with `@Tool`

Tools must be self-contained Kotlin classes with `@Tool` annotated methods processed by KSP:

```kotlin
package com.android.agents.automation.tools

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.google.adk.annotations.Tool

class AndroidNavigationTools(private val context: Context) {

    @Tool(description = "Opens a web link or deep link in the default Android browser/app")
    fun openDeepLink(url: String): String {
        return try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            "Successfully opened link: $url"
        } catch (e: Exception) {
            "Failed to open link: ${e.localizedMessage}"
        }
    }
}
```

---

### 3. Multi-Agent Orchestration with ADK

Google ADK allows agents to delegate specialized sub-tasks to subordinate agents:

```kotlin
fun createOrchestratorAgent(context: Context): Agent {
    val visionAgent = VisualInspectorAgentFactory.create(context)
    val calendarAgent = CalendarAssistantAgentFactory.create(context)

    return agent {
        name = "ChiefAssistant"
        instruction = "Delegate image analysis to the VisionAgent and schedule management to the CalendarAgent."
        subAgents = listOf(visionAgent, calendarAgent)
    }
}
```

---

### 4. Integration with Android `ViewModel`

```kotlin
@HiltViewModel
class AgentChatViewModel @Inject constructor(
    private val agent: Agent
) : ViewModel() {

    private val _uiState = MutableStateFlow<ChatUiState>(ChatUiState.Idle)
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    fun sendUserPrompt(prompt: String) {
        viewModelScope.launch {
            _uiState.value = ChatUiState.Loading

            agent.run(prompt).collect { event ->
                when (event) {
                    is AgentEvent.TextChunk -> {
                        _uiState.update { current ->
                            current.appendStreamingChunk(event.text)
                        }
                    }
                    is AgentEvent.ToolCall -> {
                        _uiState.update { current ->
                            current.setExecutingTool(event.toolName)
                        }
                    }
                    is AgentEvent.Complete -> {
                        _uiState.update { current ->
                            current.markComplete()
                        }
                    }
                    is AgentEvent.Error -> {
                        _uiState.value = ChatUiState.Error(event.throwable.message ?: "Unknown error")
                    }
                }
            }
        }
    }
}
```

---

## 🛡️ Best Practices for ADK on Android

1. **Lifecycle Scope**: Always launch ADK runs inside `viewModelScope` to ensure active LLM streams and coroutines are automatically canceled if the user navigates away.
2. **KSP Tool Processing**: Ensure all `@Tool` classes are properly registered in the Gradle module's KSP configuration.
3. **On-Device Fallback**: Provide an on-device runner option (e.g. Gemini Nano via LiteRT) for privacy-sensitive or offline agents.
4. **Clean Decoupling**: Keep Android `Context` usage isolated inside Tool classes rather than passing `Activity` references into agent builders.

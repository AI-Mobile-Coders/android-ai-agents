---
name: multiagent-orchestration
description: Guide for building multi-agent architectures (Sequential Pipelines, Parallel Fan-Out/Fan-In, Router/Orchestrator, and DSL wrappers) in Android AI Agents using :agents-multiagent.
---

# Multi-Agent Orchestration Skill (`:agents-multiagent`)

This skill provides comprehensive technical guidance for orchestrating multiple AI agents in Android applications using the `:agents-multiagent` module.

---

## Key Architectural Patterns

The `:agents-multiagent` module provides four primary multi-agent orchestration primitives:

1. **Sequential Pipeline (`AgentPipeline`)**: Chains agents together so output of Agent A becomes input to Agent B.
2. **Parallel Fan-Out / Fan-In Group (`ParallelAgentGroup`)**: Executes agents concurrently in parallel and combines responses using a custom aggregator or LLM synthesizer.
3. **Router / Orchestrator (`RouterAgent`)**: Classifies query intent and routes to specialized sub-agents or `AgentRegistry`.
4. **Unified Wrapper (`multiAgent`)**: High-level DSL wrapper providing a single entry point for complex multi-agent workflows.

> [!IMPORTANT]
> All multi-agent primitives extend `com.aimobilecoders.agents.core.Agent` (Composite Pattern). This means a pipeline, parallel group, or router can be passed anywhere a standard `Agent` is accepted!

---

## Usage Code Examples

### 1. Sequential Pipeline (`agentPipeline`)

Use when a task requires multi-stage processing (e.g., Researching -> Drafting -> Reviewing/Translating).

```kotlin
import com.aimobilecoders.agents.multiagent.agentPipeline

val pipeline = agentPipeline {
    name = "research-and-translate"

    +researcherAgent  // Step 1
    +translatorAgent  // Step 2
}

// Executes Step 1 -> passes output to Step 2 -> returns final output
val result = pipeline.send("Analyze latest Android 16 features")

// Streaming streams from the final stage
pipeline.stream("Analyze latest Android 16 features").collect { chunk ->
    print(chunk)
}
```

---

### 2. Parallel Fan-Out / Fan-In (`parallelAgentGroup`)

Use when running multiple specialized agents concurrently (e.g., Sentiment analysis + Keyword extraction + Summarization) and combining their results.

```kotlin
import com.aimobilecoders.agents.multiagent.parallelAgentGroup

val multiPerspectiveGroup = parallelAgentGroup {
    name = "multi-perspective"

    +optimistAgent
    +pessimistAgent

    // Option A: Custom aggregator function
    aggregateWith { results ->
        results.entries.joinToString("\n\n") { (agent, reply) ->
            "--- ${agent.config.name} ---\n$reply"
        }
    }

    // Option B: Synthesize results with a supervisor/synthesizer agent
    // synthesizeWith(summaryAgent)
}

val combinedReply = multiPerspectiveGroup.send("Evaluate risks and opportunities for AI in mobile.")
```

---

### 3. Router / Orchestrator (`routerAgent`)

Use to classify user queries and delegate to the best specialized sub-agent.

```kotlin
import com.aimobilecoders.agents.multiagent.routerAgent

val supportRouter = routerAgent {
    name = "customer-support-router"

    // Keyword or predicate routes
    route("billing", billingSupportAgent)
    route("tech", technicalSupportAgent)

    // Fallback when no route matches
    fallbackAgent = generalSupportAgent
}

val response = supportRouter.send("I have a question about my monthly invoice")
```

---

### 4. Unified DSL Wrapper (`multiAgent`)

Provides a clean top-level builder for setting up multi-agent architectures.

```kotlin
import com.aimobilecoders.agents.multiagent.multiAgent

val workflow = multiAgent {
    pipeline("content-creator") {
        +writerAgent
        +editorAgent
    }
}

val output = workflow.send("Write an article on Jetpack Compose performance.")
```

---

## Unit Testing Multi-Agent Systems

Test multi-agent systems deterministically using fake `LlmClient` instances:

```kotlin
class FakeLlmClient(private val name: String) : LlmClient {
    override suspend fun generate(prompt: String, systemInstruction: String?): String {
        return "[$name: $prompt]"
    }
    override fun stream(prompt: String, systemInstruction: String?): Flow<String> = flow {
        emit("[$name: $prompt]")
    }
}

@Test
fun testPipeline() = runBlocking {
    val agent1 = Agent(FakeLlmClient("A1"))
    val agent2 = Agent(FakeLlmClient("A2"))

    val pipeline = agentPipeline {
        +agent1
        +agent2
    }

    val result = pipeline.send("Hello")
    assertEquals("[A2: [A1: Hello]]", result)
}
```

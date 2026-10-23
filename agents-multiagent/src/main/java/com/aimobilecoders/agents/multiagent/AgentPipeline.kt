package com.aimobilecoders.agents.multiagent

import com.aimobilecoders.agents.core.Agent
import com.aimobilecoders.agents.core.AgentConfig
import com.aimobilecoders.agents.core.LlmClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Executes a sequence of agents in a pipeline where the output of step N
 * becomes the input prompt to step N+1.
 */
open class AgentPipeline(
    val agents: List<Agent>,
    config: AgentConfig = AgentConfig(name = "pipeline")
) : Agent(
    client = CompositeLlmClient,
    config = config
) {
    override suspend fun send(message: String): String {
        require(agents.isNotEmpty()) { "AgentPipeline must contain at least one agent." }
        var currentOutput = message
        for (agent in agents) {
            currentOutput = agent.send(currentOutput)
        }
        return currentOutput
    }

    override fun stream(message: String): Flow<String> = flow {
        require(agents.isNotEmpty()) { "AgentPipeline must contain at least one agent." }
        var currentOutput = message
        // Run all stages except the last one sequentially
        for (i in 0 until agents.size - 1) {
            currentOutput = agents[i].send(currentOutput)
        }
        // Stream the output of the final stage
        agents.last().stream(currentOutput).collect { chunk ->
            emit(chunk)
        }
    }
}

/** A no-op [LlmClient] used for composite agents that override [Agent.send] and [Agent.stream]. */
internal object CompositeLlmClient : LlmClient {
    override suspend fun generate(prompt: String, systemInstruction: String?): String = prompt
    override fun stream(prompt: String, systemInstruction: String?): Flow<String> = flow { emit(prompt) }
}

/** DSL Builder for creating an [AgentPipeline]. */
class AgentPipelineBuilder {
    var name: String = "pipeline"
    private val agentsList = mutableListOf<Agent>()

    fun add(agent: Agent) {
        agentsList.add(agent)
    }

    operator fun Agent.unaryPlus() {
        add(this)
    }

    fun build(): AgentPipeline {
        return AgentPipeline(
            agents = agentsList.toList(),
            config = AgentConfig(name = name)
        )
    }
}

/**
 * Creates an [AgentPipeline] using DSL syntax:
 * ```
 * val pipeline = agentPipeline {
 *     name = "research-writer-pipeline"
 *     +researcherAgent
 *     +writerAgent
 * }
 * ```
 */
fun agentPipeline(configure: AgentPipelineBuilder.() -> Unit): AgentPipeline {
    return AgentPipelineBuilder().apply(configure).build()
}

package com.aimobilecoders.agents.multiagent

import com.aimobilecoders.agents.core.Agent
import com.aimobilecoders.agents.core.AgentConfig
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/** Result map from parallel execution: map of Agent to response String. */
typealias ParallelResults = Map<Agent, String>

/** Aggregator function type that takes responses from all agents and produces a combined output. */
typealias AgentAggregator = suspend (results: ParallelResults) -> String

/**
 * Executes a group of agents concurrently in parallel (Fan-Out) and aggregates
 * their responses into a single output (Fan-In).
 */
open class ParallelAgentGroup(
    val agents: List<Agent>,
    val aggregator: AgentAggregator = { results ->
        results.entries.joinToString("\n\n") { (agent, reply) ->
            "[${agent.config.name}]: $reply"
        }
    },
    config: AgentConfig = AgentConfig(name = "parallelGroup")
) : Agent(
    client = CompositeLlmClient,
    config = config
) {
    override suspend fun send(message: String): String {
        require(agents.isNotEmpty()) { "ParallelAgentGroup must contain at least one agent." }
        val results = coroutineScope {
            agents.map { agent ->
                async { agent to agent.send(message) }
            }.awaitAll().toMap()
        }
        return aggregator(results)
    }

    override fun stream(message: String): Flow<String> = flow {
        val result = send(message)
        emit(result)
    }
}

/** DSL Builder for creating a [ParallelAgentGroup]. */
class ParallelAgentGroupBuilder {
    var name: String = "parallelGroup"
    private val agentsList = mutableListOf<Agent>()
    private var customAggregator: AgentAggregator? = null

    fun add(agent: Agent) {
        agentsList.add(agent)
    }

    operator fun Agent.unaryPlus() {
        add(this)
    }

    /** Set a custom aggregator lambda to merge responses from parallel agents. */
    fun aggregateWith(aggregator: AgentAggregator) {
        this.customAggregator = aggregator
    }

    /** Use a synthesizer [Agent] to summarize/combine the parallel results. */
    fun synthesizeWith(synthesizer: Agent) {
        this.customAggregator = { results ->
            val combinedPrompt = buildString {
                append("Synthesize the following responses from multiple agents into a coherent answer:\n\n")
                results.forEach { (agent, response) ->
                    append("--- Response from ${agent.config.name} ---\n")
                    append(response)
                    append("\n\n")
                }
            }
            synthesizer.send(combinedPrompt)
        }
    }

    fun build(): ParallelAgentGroup {
        val aggregator = customAggregator ?: { results ->
            results.entries.joinToString("\n\n") { (agent, reply) ->
                "[${agent.config.name}]: $reply"
            }
        }
        return ParallelAgentGroup(
            agents = agentsList.toList(),
            aggregator = aggregator,
            config = AgentConfig(name = name)
        )
    }
}

/**
 * Creates a [ParallelAgentGroup] using DSL syntax:
 * ```
 * val parallelGroup = parallelAgentGroup {
 *     name = "multi-perspective-group"
 *     +optimistAgent
 *     +pessimistAgent
 *     synthesizeWith(summaryAgent)
 * }
 * ```
 */
fun parallelAgentGroup(configure: ParallelAgentGroupBuilder.() -> Unit): ParallelAgentGroup {
    return ParallelAgentGroupBuilder().apply(configure).build()
}

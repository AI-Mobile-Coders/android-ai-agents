package com.aimobilecoders.agents.multiagent

import com.aimobilecoders.agents.core.Agent
import com.aimobilecoders.agents.core.AgentConfig
import com.aimobilecoders.agents.core.AgentRegistry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Resolver function that inspects a user message and returns the chosen target [Agent].
 */
typealias AgentResolver = suspend (message: String) -> Agent?

/**
 * Routes user queries to one of several specialized target agents based on a custom
 * classifier, LLM router agent, or [AgentRegistry].
 */
open class RouterAgent(
    val routes: Map<String, Agent> = emptyMap(),
    val resolver: AgentResolver,
    val fallbackAgent: Agent? = null,
    config: AgentConfig = AgentConfig(name = "router")
) : Agent(
    client = CompositeLlmClient,
    config = config
) {
    /** Route message to target agent and generate response. */
    override suspend fun send(message: String): String {
        val target = resolveTarget(message)
        return target.send(message)
    }

    /** Route message to target agent and stream response. */
    override fun stream(message: String): Flow<String> = flow {
        val target = resolveTarget(message)
        target.stream(message).collect { emit(it) }
    }

    private suspend fun resolveTarget(message: String): Agent {
        return resolver(message)
            ?: fallbackAgent
            ?: throw IllegalStateException("RouterAgent '${config.name}' could not route message and no fallback agent was provided.")
    }
}

/** DSL Builder for creating a [RouterAgent]. */
class RouterAgentBuilder {
    var name: String = "router"
    var fallbackAgent: Agent? = null
    var routerLlmAgent: Agent? = null
    private val routesMap = mutableMapOf<String, Agent>()
    private var customResolver: AgentResolver? = null

    fun route(routeName: String, agent: Agent) {
        routesMap[routeName] = agent
    }

    fun registry(registry: AgentRegistry, vararg routeNames: String) {
        routeNames.forEach { routeName ->
            registry.find(routeName)?.let { agent ->
                routesMap[routeName] = agent
            }
        }
    }

    fun resolveWith(resolver: AgentResolver) {
        this.customResolver = resolver
    }

    fun build(): RouterAgent {
        val finalResolver: AgentResolver = customResolver ?: { message ->
            routerLlmAgent?.let { llm ->
                val prompt = buildString {
                    append("You are a router. Select the best agent for this query.\n")
                    append("Available agents:\n")
                    routesMap.forEach { (key, agent) ->
                        append("- $key: ${agent.config.systemInstruction ?: "Specialized agent"}\n")
                    }
                    append("User Query: $message\n")
                    append("Respond ONLY with the name of the selected agent.")
                }
                val selectedName = llm.send(prompt).trim().lowercase()
                routesMap.entries.firstOrNull { it.key.lowercase() == selectedName }?.value
            } ?: run {
                routesMap.entries.firstOrNull { (key, _) ->
                    message.contains(key, ignoreCase = true)
                }?.value
            }
        }

        return RouterAgent(
            routes = routesMap.toMap(),
            resolver = finalResolver,
            fallbackAgent = fallbackAgent,
            config = AgentConfig(name = name)
        )
    }
}

/**
 * Creates a [RouterAgent] using DSL syntax:
 * ```
 * val router = routerAgent {
 *     name = "support-router"
 *     route("billing", billingAgent)
 *     route("tech", techAgent)
 *     fallbackAgent = generalSupportAgent
 * }
 * ```
 */
fun routerAgent(configure: RouterAgentBuilder.() -> Unit): RouterAgent {
    return RouterAgentBuilder().apply(configure).build()
}

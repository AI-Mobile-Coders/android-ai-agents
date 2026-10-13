package com.aimobilecoders.agents.core

import kotlinx.coroutines.flow.Flow

/**
 * Base agent. Wraps an [LlmClient] and applies the agent's [config] to every call.
 * Build one with the [agent] DSL, or subclass it to create a ready-made agent.
 */
open class Agent(
    private val client: LlmClient,
    val config: AgentConfig = AgentConfig(),
) {
    /** Send a single message and get the full reply. */
    open suspend fun send(message: String): String =
        client.generate(message, config.systemInstruction)

    /** Send a message and receive the reply as a stream. */
    open fun stream(message: String): Flow<String> =
        client.stream(message, config.systemInstruction)
}

/** Mutable builder used by the [agent] DSL. */
class AgentBuilder {
    var name: String = "agent"
    var systemInstruction: String? = null

    internal fun build(): AgentConfig = AgentConfig(name, systemInstruction)
}

/**
 * Create an agent from any [LlmClient]:
 *
 * ```
 * val a = agent(GeminiClient(apiKey)) {
 *     systemInstruction = "You are concise."
 * }
 * ```
 *
 * Providers add thin wrappers over this (e.g. `geminiAgent(apiKey)` in :agents-gemini).
 */
fun agent(client: LlmClient, configure: AgentBuilder.() -> Unit = {}): Agent {
    val config = AgentBuilder().apply(configure).build()
    return Agent(client, config)
}

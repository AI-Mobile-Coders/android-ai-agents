package com.aimobilecoders.agents.core

import kotlinx.coroutines.flow.Flow

/**
 * Base agent. Wraps an [LlmClient] and applies the agent's [config]
 * (system instruction, model) to every request.
 *
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
    var model: String = GeminiClient.DEFAULT_MODEL
    var systemInstruction: String? = null

    internal fun build(): AgentConfig = AgentConfig(name, systemInstruction, model)
}

/**
 * Create an agent in a few lines:
 *
 * ```
 * val assistant = agent(apiKey = key) {
 *     name = "assistant"
 *     systemInstruction = "You are concise and helpful."
 * }
 * val reply = assistant.send("Hello!")
 * ```
 */
fun agent(apiKey: String, configure: AgentBuilder.() -> Unit = {}): Agent {
    val config = AgentBuilder().apply(configure).build()
    val client = GeminiClient(apiKey = apiKey, model = config.model)
    return Agent(client, config)
}

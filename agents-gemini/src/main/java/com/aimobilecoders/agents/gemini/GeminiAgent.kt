package com.aimobilecoders.agents.gemini

import com.aimobilecoders.agents.core.Agent
import com.aimobilecoders.agents.core.AgentBuilder
import com.aimobilecoders.agents.core.agent

/**
 * Create a Gemini-backed agent in a few lines:
 *
 * ```
 * val a = geminiAgent(apiKey = key) { systemInstruction = "Be concise." }
 * val reply = a.send("Hello!")
 * ```
 */
fun geminiAgent(
    apiKey: String,
    model: String = GeminiClient.DEFAULT_MODEL,
    configure: AgentBuilder.() -> Unit = {},
): Agent = agent(GeminiClient(apiKey, model), configure)

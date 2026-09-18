package com.aimobilecoders.agents.core

/**
 * Configuration applied to every call an [Agent] makes.
 */
data class AgentConfig(
    val name: String = "agent",
    val systemInstruction: String? = null,
    val model: String = GeminiClient.DEFAULT_MODEL,
)

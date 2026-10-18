package com.aimobilecoders.agents.ready

import com.aimobilecoders.agents.core.Agent
import com.aimobilecoders.agents.core.AgentConfig
import com.aimobilecoders.agents.core.GeminiClient
import com.aimobilecoders.agents.gemini.GeminiClient

/**
 * A ready-made conversational agent. Drop in a Gemini API key and start chatting.
 *
 * ```
 * val chat = ChatAgent(apiKey = BuildConfig.GEMINI_API_KEY)
 * val reply = chat.send("Hi!")
 * ```
 */
class ChatAgent(
    apiKey: String,
    model: String = GeminiClient.DEFAULT_MODEL,
    systemInstruction: String = DEFAULT_INSTRUCTION,
) : Agent(
    client = GeminiClient(apiKey = apiKey, model = model),
    config = AgentConfig(
        name = "chat",
        systemInstruction = systemInstruction,
        model = model,
    ),
    config = AgentConfig(name = "chat", systemInstruction = systemInstruction),
) {
    companion object {
        const val DEFAULT_INSTRUCTION =
            "You are a helpful, friendly assistant. Answer clearly and concisely."
    }
}

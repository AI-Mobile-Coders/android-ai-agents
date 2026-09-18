package com.aimobilecoders.agents.core

import kotlinx.coroutines.flow.Flow

/**
 * Abstraction over an LLM backend. Implementations talk to a specific provider
 * (for example Gemini), so agents stay independent of the transport and can be
 * pointed at a raw API key, a backend proxy, or Firebase AI Logic later.
 */
interface LlmClient {

    /** Send a single prompt and return the full reply. */
    suspend fun generate(prompt: String, systemInstruction: String? = null): String

    /** Send a prompt and receive the reply as a stream of text chunks. */
    fun stream(prompt: String, systemInstruction: String? = null): Flow<String>
}

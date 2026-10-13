package com.aimobilecoders.agents.core.exception

/**
 * Thrown when a call to the underlying LLM provider fails.
 *
 * @property statusCode the provider's HTTP status code, when one is available.
 */
open class LlmException(
    message: String,
    val statusCode: Int? = null,
    cause: Throwable? = null,
) : AgentException(message, cause)

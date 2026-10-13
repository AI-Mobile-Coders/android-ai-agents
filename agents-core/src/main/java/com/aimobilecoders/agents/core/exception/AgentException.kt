package com.aimobilecoders.agents.core.exception

/**
 * Base type for every error thrown by the SDK.
 *
 * Catch [AgentException] to handle any SDK failure in one place, or catch a
 * specific subtype (e.g. [RateLimitException]) to react to a particular case.
 * Providers (such as :agents-gemini) map their raw errors onto these types so
 * callers never have to deal with provider-specific exceptions.
 */
open class AgentException(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause)

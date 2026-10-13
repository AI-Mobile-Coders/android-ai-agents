package com.aimobilecoders.agents.core.exception

/**
 * Thrown when the provider rejects a request because the rate limit was hit
 * (typically HTTP 429). Callers can back off and retry.
 *
 * @property retryAfterSeconds how long to wait before retrying, if the provider
 * supplied a Retry-After hint.
 */
class RateLimitException(
    message: String = "Rate limit exceeded",
    val retryAfterSeconds: Long? = null,
    cause: Throwable? = null,
) : LlmException(message, statusCode = 429, cause = cause)

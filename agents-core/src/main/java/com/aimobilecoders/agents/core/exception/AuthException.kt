package com.aimobilecoders.agents.core.exception

/**
 * Thrown when the provider rejects the request because of an authentication or
 * authorization problem, such as a missing, invalid, or unauthorized API key
 * (typically HTTP 401 or 403).
 */
class AuthException(
    message: String = "Authentication failed - check your API key",
    statusCode: Int? = 401,
    cause: Throwable? = null,
) : LlmException(message, statusCode = statusCode, cause = cause)

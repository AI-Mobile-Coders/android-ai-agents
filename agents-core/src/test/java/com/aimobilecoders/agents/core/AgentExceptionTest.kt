package com.aimobilecoders.agents.core

import com.aimobilecoders.agents.core.exception.AgentException
import com.aimobilecoders.agents.core.exception.AuthException
import com.aimobilecoders.agents.core.exception.LlmException
import com.aimobilecoders.agents.core.exception.RateLimitException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AgentExceptionTest {

    @Test
    fun hierarchy_allErrorsAreAgentExceptions() {
        assertTrue(LlmException("x") is AgentException)
        assertTrue(RateLimitException() is LlmException)
        assertTrue(AuthException() is LlmException)
    }

    @Test
    fun rateLimit_defaultsTo429() {
        val e = RateLimitException(retryAfterSeconds = 30)
        assertEquals(429, e.statusCode)
        assertEquals(30L, e.retryAfterSeconds)
    }

    @Test
    fun llmException_keepsStatusAndCause() {
        val cause = RuntimeException("boom")
        val e = LlmException("failed", statusCode = 500, cause = cause)
        assertEquals(500, e.statusCode)
        assertEquals(cause, e.cause)
    }

    @Test
    fun auth_defaultsTo401() {
        val e = AuthException()
        assertEquals(401, e.statusCode)
    }

    @Test
    fun agentException_statusCodeAbsentOnBase() {
        val e = AgentException("generic")
        assertNull(e.cause)
    }
}

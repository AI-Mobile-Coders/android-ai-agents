package com.aimobilecoders.agents.multiagent

import com.aimobilecoders.agents.core.Agent
import com.aimobilecoders.agents.core.AgentConfig
import com.aimobilecoders.agents.core.LlmClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class RouterAgentTest {

    private class ConstantLlmClient(private val response: String) : LlmClient {
        override suspend fun generate(prompt: String, systemInstruction: String?): String = response
        override fun stream(prompt: String, systemInstruction: String?): Flow<String> = flow { emit(response) }
    }

    @Test
    fun send_routesToMatchedAgentByKeyword() = runBlocking {
        val billingAgent = Agent(ConstantLlmClient("Billing response"), AgentConfig(name = "billing"))
        val techAgent = Agent(ConstantLlmClient("Tech response"), AgentConfig(name = "tech"))

        val router = routerAgent {
            name = "SupportRouter"
            route("billing", billingAgent)
            route("tech", techAgent)
        }

        val billingResult = router.send("I have a billing issue")
        assertEquals("Billing response", billingResult)

        val techResult = router.send("I have a tech question")
        assertEquals("Tech response", techResult)
    }

    @Test
    fun send_usesFallbackAgentWhenNoMatchFound() = runBlocking {
        val billingAgent = Agent(ConstantLlmClient("Billing response"), AgentConfig(name = "billing"))
        val fallbackAgent = Agent(ConstantLlmClient("Fallback response"), AgentConfig(name = "fallback"))

        val router = routerAgent {
            name = "SupportRouter"
            route("billing", billingAgent)
            this.fallbackAgent = fallbackAgent
        }

        val result = router.send("Something unknown")
        assertEquals("Fallback response", result)
    }
}

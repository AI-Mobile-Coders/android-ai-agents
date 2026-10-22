package com.aimobilecoders.agents.multiagent

import com.aimobilecoders.agents.core.Agent
import com.aimobilecoders.agents.core.AgentConfig
import com.aimobilecoders.agents.core.LlmClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ParallelAgentGroupTest {

    private class EchoLlmClient(private val name: String) : LlmClient {
        override suspend fun generate(prompt: String, systemInstruction: String?): String {
            return "Response from $name: $prompt"
        }

        override fun stream(prompt: String, systemInstruction: String?): Flow<String> = flow {
            emit("Response from $name: $prompt")
        }
    }

    @Test
    fun send_runsAgentsConcurrentlyAndAggregatesOutput() = runBlocking {
        val agentA = Agent(EchoLlmClient("AgentA"), AgentConfig(name = "AgentA"))
        val agentB = Agent(EchoLlmClient("AgentB"), AgentConfig(name = "AgentB"))

        val group = parallelAgentGroup {
            name = "TestGroup"
            +agentA
            +agentB
        }

        val result = group.send("Query")
        assertTrue(result.contains("[AgentA]: Response from AgentA: Query"))
        assertTrue(result.contains("[AgentB]: Response from AgentB: Query"))
    }

    @Test
    fun send_usesSynthesizerAgentWhenSpecified() = runBlocking {
        val agentA = Agent(EchoLlmClient("AgentA"), AgentConfig(name = "AgentA"))
        val agentB = Agent(EchoLlmClient("AgentB"), AgentConfig(name = "AgentB"))
        val synthesizer = Agent(
            client = object : LlmClient {
                override suspend fun generate(prompt: String, systemInstruction: String?): String {
                    return "Synthesized output"
                }

                override fun stream(prompt: String, systemInstruction: String?): Flow<String> = flow {
                    emit("Synthesized output")
                }
            },
            config = AgentConfig(name = "Synthesizer")
        )

        val group = parallelAgentGroup {
            name = "TestGroup"
            +agentA
            +agentB
            synthesizeWith(synthesizer)
        }

        val result = group.send("Query")
        assertEquals("Synthesized output", result)
    }
}

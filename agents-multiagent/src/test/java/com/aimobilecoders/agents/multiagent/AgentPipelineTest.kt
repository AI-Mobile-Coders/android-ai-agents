package com.aimobilecoders.agents.multiagent

import com.aimobilecoders.agents.core.Agent
import com.aimobilecoders.agents.core.AgentConfig
import com.aimobilecoders.agents.core.LlmClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class AgentPipelineTest {

    private class TestLlmClient(private val prefix: String) : LlmClient {
        override suspend fun generate(prompt: String, systemInstruction: String?): String {
            return "[$prefix: $prompt]"
        }

        override fun stream(prompt: String, systemInstruction: String?): Flow<String> = flow {
            emit("[$prefix-chunk: $prompt]")
        }
    }

    @Test
    fun send_runsAgentsSequentiallyInPipeline() = runBlocking {
        val agent1 = Agent(TestLlmClient("Step1"), AgentConfig(name = "Agent1"))
        val agent2 = Agent(TestLlmClient("Step2"), AgentConfig(name = "Agent2"))

        val pipeline = agentPipeline {
            name = "TestPipeline"
            +agent1
            +agent2
        }

        val result = pipeline.send("Hello World")
        assertEquals("[Step2: [Step1: Hello World]]", result)
    }

    @Test
    fun stream_streamsFinalStageOfPipeline() = runBlocking {
        val agent1 = Agent(TestLlmClient("Step1"), AgentConfig(name = "Agent1"))
        val agent2 = Agent(TestLlmClient("Step2"), AgentConfig(name = "Agent2"))

        val pipeline = agentPipeline {
            name = "TestPipeline"
            +agent1
            +agent2
        }

        val chunks = pipeline.stream("Hello World").toList()
        assertEquals(listOf("[Step2-chunk: [Step1: Hello World]]"), chunks)
    }
}

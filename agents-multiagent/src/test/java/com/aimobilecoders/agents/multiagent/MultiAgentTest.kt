package com.aimobilecoders.agents.multiagent

import com.aimobilecoders.agents.core.Agent
import com.aimobilecoders.agents.core.AgentConfig
import com.aimobilecoders.agents.core.LlmClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class MultiAgentTest {

    private class StepLlmClient(private val stepName: String) : LlmClient {
        override suspend fun generate(prompt: String, systemInstruction: String?): String {
            return "[$stepName: $prompt]"
        }

        override fun stream(prompt: String, systemInstruction: String?): Flow<String> = flow {
            emit("[$stepName: $prompt]")
        }
    }

    @Test
    fun multiAgent_buildsPipelineWorkflow() = runBlocking {
        val agent1 = Agent(StepLlmClient("Step1"), AgentConfig(name = "Agent1"))
        val agent2 = Agent(StepLlmClient("Step2"), AgentConfig(name = "Agent2"))

        val multiAgentSystem = multiAgent {
            pipeline("myPipeline") {
                +agent1
                +agent2
            }
        }

        val result = multiAgentSystem.send("Input")
        assertEquals("[Step2: [Step1: Input]]", result)
    }

    @Test
    fun multiAgent_buildsRouterWorkflow() = runBlocking {
        val salesAgent = Agent(StepLlmClient("Sales"), AgentConfig(name = "sales"))

        val multiAgentSystem = multiAgent {
            router("myRouter") {
                route("sales", salesAgent)
            }
        }

        val result = multiAgentSystem.send("I need sales help")
        assertEquals("[Sales: I need sales help]", result)
    }
}

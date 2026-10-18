package com.aimobilecoders.agents.comm

import com.aimobilecoders.agents.core.Agent
import com.aimobilecoders.agents.core.AgentBus
import com.aimobilecoders.agents.core.AgentMessage
import com.aimobilecoders.agents.core.AgentRegistry
import java.util.concurrent.ConcurrentHashMap

/**
 * Default in-process [AgentBus] + [AgentRegistry]. Register agents by name, then any
 * agent can reach another through the bus without a direct reference.
 *
 * ```
 * val bus = InProcessAgentBus()
 * bus.register("translator", TranslatorAgent(apiKey))
 * val reply = bus.send(AgentMessage(to = "translator", text = "Translate: Hello"))
 * ```
 */
class InProcessAgentBus : AgentBus, AgentRegistry {

    private val agents = ConcurrentHashMap<String, Agent>()

    override fun register(name: String, agent: Agent) {
        agents[name] = agent
    }

    override fun find(name: String): Agent? = agents[name]

    override suspend fun send(message: AgentMessage): String {
        val target = agents[message.to]
            ?: throw IllegalArgumentException("No agent registered as '${message.to}'")
        return target.send(message.text)
    }
}

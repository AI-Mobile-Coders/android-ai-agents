package com.aimobilecoders.agents.core

/**
 * Routes messages between agents so one agent can use another without holding a
 * direct reference. The default in-process implementation lives in :agents-comm.
 */
interface AgentBus {
    suspend fun send(message: AgentMessage): String
}

/** Registers agents under a name or capability so an [AgentBus] can find them. */
interface AgentRegistry {
    fun register(name: String, agent: Agent)
    fun find(name: String): Agent?
}

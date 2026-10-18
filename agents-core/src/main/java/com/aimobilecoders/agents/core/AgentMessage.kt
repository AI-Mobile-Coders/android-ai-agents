package com.aimobilecoders.agents.core

/** A message routed from one agent to another through an [AgentBus]. */
data class AgentMessage(
    val to: String,
    val text: String,
    val from: String? = null,
)

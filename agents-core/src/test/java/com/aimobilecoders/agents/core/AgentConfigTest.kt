package com.aimobilecoders.agents.core

import org.junit.Assert.assertEquals
import org.junit.Test

class AgentConfigTest {

    @Test
    fun builder_appliesName() {
        val config = AgentBuilder().apply { name = "assistant" }.build()
        assertEquals("assistant", config.name)
    }

    @Test
    fun builder_keepsSystemInstruction() {
        val config = AgentBuilder().apply { systemInstruction = "Be concise." }.build()
        assertEquals("Be concise.", config.systemInstruction)
    }
}

package com.aimobilecoders.agents.core

/** A user interaction coming back from the rendered UI to the agent. */
sealed interface UiEvent {
    data class Click(val id: String) : UiEvent
    data class Submit(val id: String, val value: String) : UiEvent
}

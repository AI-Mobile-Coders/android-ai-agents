package com.aimobilecoders.agents.core

/**
 * UI an agent can emit. A renderer (e.g. :agents-a2ui) turns these into real UI,
 * so agents stay independent of any UI toolkit.
 */
sealed interface UiComponent {
    data class Text(val value: String) : UiComponent
    data class Card(val title: String, val body: String) : UiComponent
    data class Button(val id: String, val label: String) : UiComponent
    data class Input(val id: String, val hint: String = "") : UiComponent
    data class ListView(val items: List<UiComponent>) : UiComponent
}

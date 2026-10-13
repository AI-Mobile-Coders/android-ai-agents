package com.aimobilecoders.agents.a2ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aimobilecoders.agents.core.UiComponent
import com.aimobilecoders.agents.core.UiEvent

/**
 * Renders agent-emitted [UiComponent]s with Jetpack Compose and routes [UiEvent]s back.
 * This is the Agent-to-UI (A2UI) bridge: agents describe UI, Compose draws it.
 */
@Composable
fun A2Ui(
    components: List<UiComponent>,
    modifier: Modifier = Modifier,
    onEvent: (UiEvent) -> Unit = {},
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        components.forEach { RenderComponent(it, onEvent) }
    }
}

@Composable
private fun RenderComponent(component: UiComponent, onEvent: (UiEvent) -> Unit) {
    when (component) {
        is UiComponent.Text -> Text(component.value)
        is UiComponent.Card -> Card {
            Column(Modifier.padding(16.dp)) {
                Text(component.title, style = MaterialTheme.typography.titleMedium)
                Text(component.body)
            }
        }
        is UiComponent.Button -> Button(onClick = { onEvent(UiEvent.Click(component.id)) }) {
            Text(component.label)
        }
        is UiComponent.Input -> {
            var value by remember { mutableStateOf("") }
            OutlinedTextField(
                value = value,
                onValueChange = {
                    value = it
                    onEvent(UiEvent.Submit(component.id, it))
                },
                label = { Text(component.hint) },
            )
        }
        is UiComponent.ListView -> Column {
            component.items.forEach { RenderComponent(it, onEvent) }
        }
    }
}

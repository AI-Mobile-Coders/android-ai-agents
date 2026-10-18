package com.aimobilecoders.agents.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aimobilecoders.agents.core.Agent
import kotlinx.coroutines.launch

/** A simple, ready-to-use chat screen backed by any [Agent]. */
@Composable
fun AgentChatScreen(agent: Agent, modifier: Modifier = Modifier) {
    var input by remember { mutableStateOf("") }
    val messages = remember { mutableStateListOf<ChatLine>() }
    val scope = rememberCoroutineScope()

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        LazyColumn(Modifier.fillMaxSize().padding(bottom = 8.dp)) {
            items(messages) { line ->
                Text("${line.sender}: ${line.text}", Modifier.padding(vertical = 4.dp))
            }
        }
        Row {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.padding(end = 8.dp),
            )
            Button(onClick = {
                val question = input.trim()
                if (question.isNotEmpty()) {
                    input = ""
                    messages.add(ChatLine("You", question))
                    scope.launch {
                        val reply = runCatching { agent.send(question) }
                            .getOrElse { "Error: ${it.message}" }
                        messages.add(ChatLine("Agent", reply))
                    }
                }
            }) {
                Text("Send")
            }
        }
    }
}

private data class ChatLine(val sender: String, val text: String)

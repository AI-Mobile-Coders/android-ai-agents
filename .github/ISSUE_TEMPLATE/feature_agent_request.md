---
name: "🤖 New ADK Agent Proposal / Request"
about: "Propose a new Android AI Agent built using Google ADK Kotlin (google/adk-kotlin)"
title: "[ADK AGENT PROPOSAL]: "
labels: ["enhancement", "adk-agent-proposal"]
assignees: ""
---

### 🌟 ADK Agent Name & Summary
<!-- What should the agent be called and what does it do? -->

### 🎯 Real-world Android Use Case
<!-- Describe how an Android app developer would use this ADK agent in production. -->

### 🛠️ Required `@Tool` Annotated Android Tools
<!-- List the Android APIs or tools this agent needs (e.g., Camera, Location, Contacts, Accessibility, Notifications) -->

### 🧠 Target ADK Model Runners
<!-- Cloud Gemini (gemini-2.5-flash / pro) vs. On-Device Gemini Nano (LiteRT-LM / AICore) -->

### 🏗️ Proposed ADK Agent DSL Definition
```kotlin
val myAdkAgent = agent {
    name = "MyAdkAgent"
    instruction = "..."
    model = geminiModel("gemini-2.5-flash")
    tools = listOf(...)
}
```

### 🙋 Are you interested in implementing this agent?
- [ ] Yes, I'd like to submit a PR using Google ADK Kotlin!
- [ ] No, this is just an idea/request for the community.

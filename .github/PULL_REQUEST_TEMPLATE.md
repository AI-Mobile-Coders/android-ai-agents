## 📋 Description of Changes

<!-- Please provide a concise summary of the ADK agent, tool, or runner being introduced or updated. -->

### 🏷️ Contribution Type
- [ ] 🤖 New Google ADK Agent Implementation
- [ ] 🔧 New `@Tool` Annotated Android Tool
- [ ] ⚡ ADK Runner / On-Device Nano Optimization
- [ ] 🐛 Bug Fix
- [ ] 📚 Documentation & Jetpack Compose Sample
- [ ] 🏗️ Core Architecture / Multi-Agent Delegation

---

## 🤖 ADK Agent Details (If applicable)

- **Agent Name**: `e.g. VisualInspectorAgent`
- **Category**: `e.g. Vision / Automation / Voice / On-Device / RAG`
- **ADK Models Supported**: `e.g. Gemini 2.5 Flash / On-Device Gemini Nano (LiteRT)`
- **ADK Tools Included**: `e.g. @Tool AndroidCalendarTools, @Tool DeviceBatteryTools`
- **Required Android Permissions**: `e.g. android.permission.CAMERA`

---

## 🧪 Testing & Verification

Please describe the tests executed to verify your changes:
- [ ] Unit tests added with mock ADK runners / test tools
- [ ] Tested on Android Emulator / Physical Device (API level: ___)
- [ ] KSP tool code generation verified (`./gradlew kspKotlin`)
- [ ] Cancellation & lifecycle cleanup verified in ViewModel
- [ ] Code formatted with `ktlint` and passes `detekt`

---

## 🔒 Privacy & Safety Checklist

- [ ] Built exclusively with `google/adk-kotlin`
- [ ] No hardcoded API keys or credentials
- [ ] No PII leaked in logs
- [ ] Dangerous tools require explicit permission checks / confirmations

---

## 📸 Screenshots / Demos (Optional)

<!-- Add animated GIF, screenshot, or video link demonstrating the ADK agent in action -->

---

## 🔗 Related Issues

Closes #

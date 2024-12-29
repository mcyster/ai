
AiService
- createConversationBuilder
  - ActiveConverstation start()
- leverages Advisor<CONTEXT> -- curently limited to OpenAi Assistant implementation (perhaps rename to Weaver)

Weaver (formerly Advisor)
- ActiveConvestation createConversationBuilder()
- ActiveConvestation createConversationBuilder(EventContext)
- searchToolBuilder
- codeToolBuilder
- specific implementations for different platforms

AiScenarioService
- ScenarioBuilder(Advisor/Weaver)
  - Scenario
    - createConversationBuilder  <-- pass EventHandler
      - ScenarioConversation start()

ScenarioSet
- set of Scenarios
- future: can execute in context of listener
- supports lazy initalization of scenario
- ScenarioSet works with ScenarioTemplate
- Scenarios must be built in the context of a specific Advisor
- Scenarios must be executed with a WeaveContext

ScenarioTemplate extends ScenarioType
Scenario extends ScenarioType

Weave  // future: if make async this should be safe
- Conversation
  - id()
  - list of Message
    - operation
  - operation


---

ConversationLinkTool
- conversationStore
- execute(parameters, context, weave) {
  weave.id() ...
}

storage implements WeaveOperationHandler

weaver.createConversationBuilder()
  .withContext()
  .withListener(WeaveOperationHandler operationHandler)
  .start()

---
Message
- WeaveOperation operation

Conversation
- List<Message> messages

Weave
- AciveConvestation

---

Move persistance into a Weave

Weave
  - persitance / logging
  - id()
  - child(context?)

 Weaver(WeaveStore)
 - Scenario in the context of a Weave getOrCreateScenario
   - newWeave()

WeaveStore
- addConverstation(Conversation)

 Scenario
 - Converstation createConversatinBuilder.start()
   -> Weave
      - child

---

Struggles
- tool referencing "id" of conversation

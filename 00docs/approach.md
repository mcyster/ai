

ScenarioSetBuilder<Context>
- enable
  - ExtoleSupperContext to use ExtoleAdminContext scenarios
  - ExtoleAdmiNContext to also use void scenarios

Weave -> AdvisorContext
- operations?
WeaveOperation -> AdvisorOperation

Support nesting of Weave Operations between scenarios

ScenarioType where?
- WeaveStore
- ScenarioSet

OpenAi searchTool / codeInterpreterTool are special for OpenAI
- move tool builder to be part of AdvisorBuidler
- need to remove special code from withTool  <<<<<-------- TODO


---

AiService
- createConversationBuilder
  - ActiveConverstation start()
- leverages Advisor<CONTEXT>

Advisor (rename to Weaver?)
- ActiveConvestation createConversationBuilder()
- ActiveConvestation createConversationBuilder(EventContext)
- searchToolBuilder
- codeToolBuilder
- specific implementations for different platforms
- really want typed response, but then we need a RESULT in Conversation and Message and Tool

AiScenarioService
- ScenarioBuilder(Advisor/Weaver)
  - Scenario
    - createConversationBuilder  <-- pass EventHandler
      - ActiveConversation start()

ScenarioSet
- set of Scenarios
- future: can execute in context of listener
- supports lazy initalization of scenario
- ScenarioSet works with ScenarioTemplate
- Scenarios must be built in the context of a specific Advisor
- Scenarios must be executed with a WeaveContext

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

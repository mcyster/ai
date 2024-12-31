

ScenarioSetBuilder<Context>
- currently spring focus
- move to be type focused
- allow multiple types
- support translating context types / having a set of supported type
- enable
  - ExtoleSupperContext to use ExtoleAdminContext scenarios
  - ExtoleAdmiNContext to also use void scenarios

Weave -> AdvisorContext

WeaveOperation -> AdvisorOperation

Support nesting of Weave Operations between scenarios

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

ScenarioTemplate extends ScenarioType
Scenario extends ScenarioType

Weave  // future: if make async this should be safe
- Conversation
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



# Minor Changes

- load vstore lazily, load old version, then update in background
- prehandler use SearchTool
- cleanup vectorstores, files
- Support mention of BeepBoop in jira to have it evaluate ticket, support prompt as commment
- Schedule Continuation of Conversation Tool (perhaps large, unless no context provided)
- Scenario parameters ignored
- Move supportSupportTicket scenario to yaml
- AdvisorService.createToolBuilder so tools can apply to assistantCreateRequest
- change parameters -> data for scenario

# Major Changes

## 3 Concepts necessary?

Do we need 3 concepts
- Scenario
  - Describes an api and context for having a Conversation
  - Use by the rest api to describe the questions it can answer and the context that needs to be provided
- Tool
  - Describe an api for asking a question (One response not a Conversation like a Scenaio)
- Agent
  - Perhaps should be named Assistant (as it sits directly on Assistant)

## Rest API

Need to update to support richer understanding of conversation for prompt development

Response should include all aspects of conversation. If nested conversations, it should be possible to navigate and see them. 
To properly support this, feel I might need to move to an event based architecture.

## Conversation

- Support an ascynronous api
- Support rich conversation logs
- Seperate memory from logs

## Documentation

Mechanism to crawl / upload documentation

QDRant

### Public Code

Upload code directly from github

## Deployment / systemd

Commit basic deployment scripts

Update scripts to work with systemd or perhaps goto Lamdbas?

Leverage secret store for keys


# Modules

Use modules to declare public classes etc rather than separate builds

Testing here
- https://github.com/mcyster/jpms

# Merge Agent and Scenario

public interface Scenario<PARAMETERS, CONTEXT> {
	String getName();
	String getDescription();
  Class<PARAMETERS> getParameterClass();
  Class<CONTEXT> getContextClass();
	Conversation createConversation(PARAMETERS parameters, CONTEXT context);
}

public interface Advisor<C> {
    String getName();
    ConversationBuilder<C> createConversation();
    interface ConversationBuilder<C> {
        ConversationBuilder<C> withContext(C context);
        ConversationBuilder<C> setOverrideInstructions(String instruction);
        ConversationBuilder<C> addMessage(String message);
        Conversation start();
    }
}

public interface AdvisorService { 
    <C> AdvisorBuilder<C> getOrCreateAdvisor(String name);
    <PARAMETERS, CONTEXT> Tool<PARAMETERS, CONTEXT> cachingTool(Tool<PARAMETERS, CONTEXT> tool);
    <CONTEXT> SearchTool.Builder<CONTEXT> searchToolBuilder();
    <CONTEXT> CodeInterpreterTool.Builder<CONTEXT> codeToolBuilder();    
    SimpleDocumentStoreBuilder simpleDocumentStoreBuilder();
    DirectoryDocumentStoreBuilder directoryDocumentStoreBuilder();
}

public interface AdvisorBuilder<C> {
    AdvisorBuilder<C> setInstructions(String instruction);
    <T> AdvisorBuilder<C> withTool(Tool<T, C> tool);
    AdvisorBuilder<C> withFile(Path path);
    Advisor<C> getOrCreate();
}

public interface ScenarioService {
    Set<Scenario<?,?>> getScenarios();
    Scenario<?,?> getScenario(String name) throws ScenarioException;
}


merge concepts

AiWeaveService {
  // move set of Scenarios out to spring
  
  <P, C> AssistantScenarioBuilder<P, C> getOrCreateAssistantScenario(String name)
  
  // Special tools ...
  // <PARAMETERS, CONTEXT> Tool<PARAMETERS, CONTEXT> cachingTool(Tool<PARAMETERS, CONTEXT> tool);
  // <CONTEXT> SearchTool.Builder<CONTEXT> searchToolBuilder();
}

  
public interface Scenario<PARAMETERS, CONTEXT> {
  String getName();
  String getDescription();
  Class<PARAMETERS> getParameterClass();
  Class<CONTEXT> getContextClass();
  
  ConversationBuilder createConversation(PARAMETERS parameters, CONTEXT context);

   interface ConversationBuilder {
        ConversationBuilder setOverrideInstructions(String instruction);
        ConversationBuilder addMessage(String message);
        Conversation start();
    }
}

public interface AssistantScenarioBuilder<P,C> implements Scenario {
    AssistantScenarioBuilder<P,C> setInstructions(String instruction);
    <T> AssistantScenarioBuilder<P, C> withTool(Tool<T, C> tool);
    AssistantScenario<P,C> getOrCreate();
}




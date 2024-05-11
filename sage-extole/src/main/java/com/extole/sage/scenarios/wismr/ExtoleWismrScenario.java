package com.extole.sage.scenarios.wismr;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.theokanning.openai.service.OpenAiService;
import com.cyster.assistant.impl.conversation.TooledChatConversation;
import com.cyster.assistant.service.conversation.Conversation;
import com.cyster.assistant.service.scenario.Scenario;
import com.extole.sage.session.ExtoleSessionContext;

@Component
public class ExtoleWismrScenario implements Scenario<Void, ExtoleSessionContext> {
    private static final String NAME = "extoleWismr";
    
    private OpenAiService openAiService;
    private ExtolePersonFindToolFactory extolePersonFindToolFactory;
    private ExtolePersonRewardsToolFactory extolePersonRewardsToolFactory;
    private ExtolePersonStepsToolFactory extolePersonStepsToolFactory;
    private ExtoleStepsToolFactory extoleStepsToolFactory;

    ExtoleWismrScenario(OpenAiService openAiService,
        ExtolePersonFindToolFactory extolePersonFindToolFactory,
        ExtolePersonRewardsToolFactory extolePersonRewardsToolFactory,
        ExtolePersonStepsToolFactory extolePersonStepsToolFactory,
        ExtoleStepsToolFactory extoleStepsToolFactory) {
        this.openAiService = openAiService;
        this.extolePersonFindToolFactory = extolePersonFindToolFactory;
        this.extolePersonRewardsToolFactory = extolePersonRewardsToolFactory;
        this.extolePersonStepsToolFactory = extolePersonStepsToolFactory;
        this.extoleStepsToolFactory = extoleStepsToolFactory;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "Extole tool to help find the reward of a person";
    }
    
    @Override
    public Class<Void> getParameterClass() {
        return Void.class;
    }
    
    @Override
    public Class<ExtoleSessionContext> getContextClass() {
        return ExtoleSessionContext.class;
    }


    @Override
    public Conversation createConversation(Void parameters, ExtoleSessionContext context) {
            String systemPrompt = """
You are a customer service representative for the Extole SaaS marketing platform.
You specialize in helping people find a reward they expected to receive from the Extole platform.

Step 1: Identify the person and check if they have any rewards. 
The best way to start is to try and load and review the persons profile base on a key we might have, 
such as email, partner_user_id or order_id.

Step 2: Person has rewards
If the person has rewards, show the rewards, we are done.

Step 3: Person has no rewards
Get all the stepNames that have an actionName earnRewards.

Step 4:
Check if the person has have any of the stepNames on their profile that earn rewards.
If there are steps, show the steps and we are done.

""";

        MustacheFactory mostacheFactory = new DefaultMustacheFactory();
        Mustache mustache = mostacheFactory.compile(new StringReader(systemPrompt), "system_prompt");
        var messageWriter = new StringWriter();
        mustache.execute(messageWriter, parameters);
        messageWriter.flush();

        Optional<String> accessToken = Optional.empty();
        accessToken = Optional.of(context.getAccessToken());
        
        var conversation = new TooledChatConversation(openAiService)
            .addSystemMessage(messageWriter.toString())
            .addTool(this.extolePersonFindToolFactory.create(accessToken))
            .addTool(this.extolePersonRewardsToolFactory.create(accessToken))
            .addTool(this.extolePersonStepsToolFactory.create(accessToken))
            .addTool(this.extoleStepsToolFactory.create(accessToken));

        return conversation;
    }
  
}

package com.extole.weave.scenarios.prehandler;

import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.advisor.Advisor;
import com.cyster.ai.weave.service.conversation.Conversation;
import com.cyster.ai.weave.service.scenario.Scenario;
import com.extole.weave.advisors.ExtoleJavascriptPrehandlerActionAdvisor;
import com.extole.weave.advisors.ExtoleJavascriptPrehandlerActionAdvisor.AdminUserToolContext;
import com.extole.weave.session.ExtoleSessionContext;

@Component
public class ExtoleJavascriptPrehandlerActionScenario implements Scenario<Void, ExtoleSessionContext> {
    public static String NAME = "extoleJavascriptPrehandlerAction";

    private Advisor<AdminUserToolContext> advisor;

    ExtoleJavascriptPrehandlerActionScenario(ExtoleJavascriptPrehandlerActionAdvisor advisor) {
        this.advisor = advisor;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "Helps with writing and debugging prehandlers";
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
        return new ConversationBuilder(this.advisor).setContext(context).start();
    }

    public class ConversationBuilder {
        private Advisor<AdminUserToolContext> advisor;
        private ExtoleSessionContext context;

        ConversationBuilder(Advisor<AdminUserToolContext> advisor) {
            this.advisor = advisor;
        }

        public ConversationBuilder setContext(ExtoleSessionContext context) {
            this.context = context;
            return this;
        }

        public Conversation start() {
            var context = new AdminUserToolContext(this.context.getAccessToken());;

            return this.advisor.createConversation().withContext(context).start();
        }
    }

    @Override
    public com.cyster.ai.weave.service.scenario.Scenario.ConversationBuilder createConversationBuilder(Void parameters,
        ExtoleSessionContext context) {
        // TODO Auto-generated method stub
        return null;
    }

}

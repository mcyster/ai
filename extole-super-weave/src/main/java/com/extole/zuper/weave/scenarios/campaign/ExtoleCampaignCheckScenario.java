package com.extole.zuper.weave.scenarios.campaign;

import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.conversation.ActiveConversationBuilder;
import com.cyster.ai.weave.service.scenario.Scenario;
import com.extole.zuper.weave.ExtoleSuperContext;
import com.extole.zuper.weave.scenarios.campaign.ExtoleCampaignCheckScenario.Parameters;
import com.extole.zuper.weave.scenarios.support.ExtoleSupportHelpScenario;
import com.fasterxml.jackson.annotation.JsonProperty;

@Component
public class ExtoleCampaignCheckScenario implements Scenario<Parameters, ExtoleSuperContext> {
    private static final String DESCRIPTION = "Check a campaign given a campaignId";

    private ExtoleSupportHelpScenario helpScenario;

    ExtoleCampaignCheckScenario(ExtoleSupportHelpScenario helpScenario) {
        this.helpScenario = helpScenario;
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName().replace("Scenario", "");
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    @Override
    public Class<Parameters> getParameterClass() {
        return Parameters.class;
    }

    @Override
    public Class<ExtoleSuperContext> getContextClass() {
        return ExtoleSuperContext.class;
    }

    @Override
    public ActiveConversationBuilder createConversationBuilder(Parameters parameters, ExtoleSuperContext context) {
        String instructions = """
                You are a member of the Support team at Extole, a SaaS marketing platform. You are tasked with checking the variables associated with campaigns

                Download the variables associated with the campaignId: %s

                These variables contain multiple languages, the most common being English, Spanish and French.

                These values are used for things like setting the Subject/Body of emails, and messaging on the clients website.

                Please be aware of the different languages and provide language specific suggestions if you think the translation may not be ideal.

                Please review to ensure values have no spelling errors, grammar errors and that there are no unexpected characters.

                If the prompt contains HTML markup it may be a HTML fragment, that is ok.
                The prompt contains curly braces, these are variable replacements in the Mostache scripting language.

                For each variable that has a problem respond in json of the form: { "name": "VARIABLE_NAME", "problems": [ "PROLEM1", PROBLEM2"] }

                """;

        return helpScenario.createConversationBuilder(null, context)
                .setOverrideInstructions(String.format(instructions, parameters.campaignId));
    }

    public static class Parameters {
        @JsonProperty(required = true)
        public String campaignId;
    }

}

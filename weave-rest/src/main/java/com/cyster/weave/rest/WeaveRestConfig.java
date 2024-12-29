package com.cyster.weave.rest;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import com.cyster.ai.weave.impl.AiServiceImpl;
import com.cyster.ai.weave.impl.openai.advisor.assistant.AssistantAiAdvisorServiceImpl;
import com.cyster.ai.weave.impl.scenario.AiScenarioServiceImpl;
import com.cyster.ai.weave.service.AiAdvisorService;
import com.cyster.ai.weave.service.AiScenarioService;
import com.cyster.ai.weave.service.AiService;
import com.cyster.ai.weave.service.scenario.Scenario;
import com.cyster.ai.weave.service.scenario.ScenarioLoader;
import com.cyster.ai.weave.service.scenario.ScenarioSet;
import com.cyster.ai.weave.service.tool.ToolContextFactory;

@Configuration
public class WeaveRestConfig {

    public WeaveRestConfig(ApplicationContext applicationContext) {
    }

    @Bean
    public AiService getAiService(@Value("${OPENAI_API_KEY}") String openAiApiKey,
            ToolContextFactory toolContextFactory) {
        if (!StringUtils.hasText(openAiApiKey)) {
            throw new IllegalArgumentException("OPENAI_API_KEY not defined");
        }

        return new AiServiceImpl();
    }

    @Bean
    public AiAdvisorService getAiAgentService(@Value("${OPENAI_API_KEY}") String openAiApiKey,
            ToolContextFactory toolContextFactory) {
        if (!StringUtils.hasText(openAiApiKey)) {
            throw new IllegalArgumentException("OPENAI_API_KEY not defined");
        }

        return new AssistantAiAdvisorServiceImpl(openAiApiKey, toolContextFactory);
    }

    @Bean
    public AiScenarioService getAiScenarioService(AiAdvisorService advisorService) {
        return new AiScenarioServiceImpl(advisorService);
    }

    @Bean
    public ScenarioSet getScenarioService(AiScenarioService aiScenarioService, List<ScenarioLoader> scenarioLoaders,
            List<Scenario<?, ?>> scenarios) {
        return aiScenarioService.senarioSetBuilder().addScenarioLoaders(scenarioLoaders).addScenarios(scenarios)
                .create();
    }

}

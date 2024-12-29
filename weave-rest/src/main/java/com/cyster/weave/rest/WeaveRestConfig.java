package com.cyster.weave.rest;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import com.cyster.ai.weave.impl.AiWeaveServiceImpl;
import com.cyster.ai.weave.service.AiService;
import com.cyster.ai.weave.service.ToolContextFactory;
import com.cyster.ai.weave.service.scenario.Scenario;
import com.cyster.ai.weave.service.scenario.ScenarioLoader;
import com.cyster.ai.weave.service.scenario.ScenarioSet;

@Configuration
public class WeaveRestConfig {

    public WeaveRestConfig(ApplicationContext applicationContext) {
    }

    @Bean
    public AiService getAiWeaveService(@Value("${OPENAI_API_KEY}") String openAiApiKey,
            ToolContextFactory toolContextFactory) {
        if (!StringUtils.hasText(openAiApiKey)) {
            throw new IllegalArgumentException("OPENAI_API_KEY not defined");
        }

        return new AiWeaveServiceImpl(openAiApiKey, toolContextFactory);
    }

    @Bean
    public ScenarioSet getScenarioService(AiService aiWeaveService, List<ScenarioLoader> scenarioLoaders,
            List<Scenario<?, ?>> scenarios) {
        return aiWeaveService.senarioSetBuilder().addScenarioLoaders(scenarioLoaders).addScenarios(scenarios).create();
    }

}

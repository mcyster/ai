package com.extole.app.jira;

import static org.springframework.ai.autoconfigure.openai.OpenAiProperties.CONFIG_PREFIX;

import java.util.List;

import org.springframework.ai.autoconfigure.openai.OpenAiProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import com.cyster.assistant.impl.advisor.AdvisorServiceImpl;
import com.cyster.assistant.impl.scenario.ScenarioServiceImpl;
import com.cyster.assistant.service.advisor.AdvisorService;
import com.cyster.assistant.service.scenario.Scenario;
import com.cyster.assistant.service.scenario.ScenarioLoader;
import com.cyster.assistant.service.scenario.ScenarioService;


@Configuration
public class JiraAppConfig {
    
    @Bean
    public AdvisorService getAdvisorService(OpenAiProperties openAiProperties) {    
        if (!StringUtils.hasText(openAiProperties.getApiKey())) {
            throw new IllegalArgumentException(
                "No Open API key with the property name " + CONFIG_PREFIX + ".api-key");
        }
    
        return new AdvisorServiceImpl(openAiProperties.getApiKey());
    }
 
    @Bean
    public ScenarioService getScenarioService(List<ScenarioLoader> scenarioLoaders, List<Scenario<?,?>> scenarios) {
        return new ScenarioServiceImpl(scenarioLoaders, scenarios);
    }

}

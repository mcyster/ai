package com.extole.app.jira;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;

import com.cyster.weave.rest.conversation.ScenarioContextException;
import com.cyster.weave.rest.conversation.ScenarioContextFactory;
import com.extole.zuper.weave.ExtoleSuperContext;

@Component
public class JiraScenaioContextFactory implements ScenarioContextFactory<ExtoleSuperContext> {
    private static final Logger logger = LoggerFactory.getLogger(JiraScenaioContextFactory.class);

    private final Optional<String> extoleSuperUserApiKey;

    public JiraScenaioContextFactory(
            @Value("${extoleSuperUserApiKey:#{environment.EXTOLE_SUPER_USER_API_KEY}}") String extoleSuperUserApiKey) {
        if (extoleSuperUserApiKey != null) {
            this.extoleSuperUserApiKey = Optional.of(extoleSuperUserApiKey);
        } else {
            this.extoleSuperUserApiKey = Optional.empty();
            logger.error("extoleSuperUserApiKey not defined or found in environment.EXTOLE_SUPER_USER_API_KEY");
        }
    }

    @Override
    public Class<ExtoleSuperContext> getContextClass() {
        return ExtoleSuperContext.class;
    }

    @Override
    public ExtoleSuperContext createContext(MultiValueMap<String, String> headers) throws ScenarioContextException {
        return createContext();
    }

    public ExtoleSuperContext createContext() throws ScenarioContextException {
        if (extoleSuperUserApiKey.isPresent()) {
            return new ExtoleSuperContext(extoleSuperUserApiKey.get());
        }

        throw new ScenarioContextException("Extole super user key not defined");
    }
}

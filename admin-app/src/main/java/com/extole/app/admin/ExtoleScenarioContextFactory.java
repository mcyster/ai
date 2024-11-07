package com.extole.app.admin;

import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;

import com.cyster.weave.rest.conversation.ScenarioContextException;
import com.cyster.weave.rest.conversation.ScenarioContextFactory;
import com.extole.admin.weave.session.ExtoleSessionContext;

@Component
public class ExtoleScenarioContextFactory implements ScenarioContextFactory<ExtoleSessionContext> {

	@Override
	public Class<ExtoleSessionContext> getContextClass() {
		return ExtoleSessionContext.class;
	}

	@Override
	public ExtoleSessionContext createContext(MultiValueMap<String, String> headers) throws ScenarioContextException {
	   if (headers == null || !headers.containsKey("authorization")) {
	        throw new ScenarioContextException("Unable to create ExtoleSessionContext expected Authorization header");
	    }
	    String authorizationHeader = headers.getFirst("authorization");

	    if (authorizationHeader != null) {
	        var accessToken = authorizationHeader.replace("Bearer ", "");
	        if (accessToken.length() > 0) {
	            return new ExtoleSessionContext(accessToken);
	        }
	    }

	    throw new ScenarioContextException(
	            "Unable to create ExtoleSessionContext, Authorization header exists but not token found");
	}
}

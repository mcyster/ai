package com.cyster.weave.rest.conversation;

import org.springframework.util.MultiValueMap;

public interface ScenarioContextFactory<CONTEXT> {
	
	Class<CONTEXT> getContextClass();
	
	CONTEXT createContext(MultiValueMap<String, String> headers) throws ScenarioContextException;
}

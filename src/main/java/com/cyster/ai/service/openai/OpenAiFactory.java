package com.cyster.ai.service.openai;

import com.theokanning.openai.service.OpenAiService;

public interface OpenAiFactory {
	
	OpenAiService getService();
	
}

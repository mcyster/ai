package com.cyster.app.sage.conversation;

import java.util.ArrayList;
import java.util.List;

import com.cyster.ai.weave.service.conversation.Message;
import com.cyster.ai.weave.service.conversation.Operation;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public record ConversationResponse(
    String id, 
    String scenario, 
    List<MessageResponse> messages) {

	public ConversationResponse {
		if (id == null || id.isBlank()) {
			throw new IllegalArgumentException("Id cannot be null or blank");
		}
		if (scenario == null || scenario.isBlank()) {
			throw new IllegalArgumentException("Scenario cannot be null or blank");
		}
		if (messages == null) {
			throw new IllegalArgumentException("Messages cannot be null");
		}
	}

	@Override
	public String toString() {
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			return objectMapper.writeValueAsString(this);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	public static class Builder {
	    private MessageResponse.Level level;
		private String id;
		private String scenario;
		private List<MessageResponse> messages;

	    public Builder(MessageResponse.Level level) {
	        this.level = level;
	    }
	      
		public Builder setId(String id) {
			this.id = id;
			return this;
		}

		public Builder setScenario(String scenario) {
			this.scenario = scenario;
			return this;
		}

		public Builder setMessages(List<Message> messages) {
		    var response = new ArrayList<MessageResponse>();
			for (var message : messages) {
			    response.add(new MessageResponse.Builder(level)
				    .create(message.getType().toString(), message.getContent(), message.operation()));
			}
			this.messages = response;
			return this;
		}

		public ConversationResponse build() {
			return new ConversationResponse(this.id, this.scenario, this.messages);
		}
	}
}

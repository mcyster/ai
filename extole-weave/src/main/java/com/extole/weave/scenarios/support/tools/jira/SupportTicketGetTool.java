package com.extole.weave.scenarios.support.tools.jira;

import java.util.Iterator;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import com.cyster.adf.reader.MarkDownDocumentMapper;
import com.cyster.ai.weave.service.FatalToolException;
import com.cyster.ai.weave.service.ToolException;
import com.cyster.jira.client.JiraWebClientFactory;
import com.extole.weave.scenarios.support.tools.ExtoleSupportTool;
import com.extole.weave.scenarios.support.tools.jira.SupportTicketGetTool.Request;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Component
public class SupportTicketGetTool implements ExtoleSupportTool<Request> {
    private JiraWebClientFactory jiraWebClientFactory;

    SupportTicketGetTool(JiraWebClientFactory jiraWebClientFactory) {
        this.jiraWebClientFactory = jiraWebClientFactory;
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName().replace("Tool", "");
    }

    @Override
    public String getDescription() {
        return "Retrieve tickets from the Extole Jira support ticket tracking system";
    }

    @Override
    public Class<Request> getParameterClass() {
        return Request.class;
    }

    @Override
    public Object execute(Request request, Void context) throws ToolException {

        if (request.key != null && request.key.isEmpty()) {
            throw new FatalToolException("Attribute ticket key not specified");
        }

        var resultNode =  this.jiraWebClientFactory.getWebClient().get()
            .uri(uriBuilder -> uriBuilder.path("/rest/api/3/issue/" + request.key)
                .queryParam("expand", "renderedFields,comment")
                .build())
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .bodyToMono(JsonNode.class)
            .block();

        if (resultNode == null || !resultNode.has("key") || resultNode.path("key").asText().isEmpty()) {
            throw new ToolException("Jira search failed with unexpected response");
        }
        var issueNode = resultNode;

        // TODO more robust pattern to refine result
        ObjectNode ticket = JsonNodeFactory.instance.objectNode();
        {
            ticket.put("key", issueNode.path("key").asText());
            ticket.put("summary", issueNode.path("fields").path("summary").asText());

            JsonNode assignee = issueNode.path("fields").path("assignee");
            if (!assignee.isMissingNode()) {
                ticket.put("assignee", assignee.path("emailAddress").asText());
            } else {
                ticket.putNull("assignee");
            }

            JsonNode status = issueNode.path("fields").path("status");
            if (!status.isMissingNode()) {
                ticket.put("status", status.path("name").asText());
            } else {
                ticket.putNull("status");
            }

            JsonNode parent = issueNode.path("fields").path("parent");
            if (!parent.isMissingNode()) {
                ticket.put("classification", parent.path("fields").path("summary").asText());
            } else {
                ticket.putNull("classification");
            }

            JsonNode customField11312 = issueNode.path("fields").path("customfield_11312");
            if (!customField11312.isMissingNode()) {
                ticket.put("clientId", customField11312.path("value").asText());
            } else {
                ticket.putNull("clientId");
            }

            ticket.put("createdDate", issueNode.path("fields").path("created").asText());
            ticket.put("updatedDate", issueNode.path("fields").path("updated").asText());

            Iterator<JsonNode> labelsNode = issueNode.path("fields").path("labels").elements();
            ArrayNode labels = ticket.putArray("labels");
            while (labelsNode.hasNext()) {
                JsonNode label = labelsNode.next();
                labels.add(label.asText());
            }

            JsonNode descriptionNode = issueNode.path("fields").path("description");
            if (descriptionNode.isMissingNode()) {
                ticket.put("description", "");
            } else {                
                String content = new MarkDownDocumentMapper().fromAtlassianDocumentFormat(descriptionNode);
                ticket.put("description", content);
            }

            JsonNode renderedFieldsNode = issueNode.path("renderedFields");
            if (renderedFieldsNode.isMissingNode()) {
                ticket.putArray("comments"); 
            } 
            else {
                JsonNode commentContainerNode = renderedFieldsNode.path("comment");
                if (commentContainerNode.isMissingNode()) {
                    ticket.putArray("comments"); 
                } 
                else { 
                    ArrayNode commentsNode = (ArrayNode)commentContainerNode.path("comments");
    
                    if (commentsNode.isMissingNode() || commentsNode.isEmpty()) {
                        ticket.putArray("comments");
                    } 
                    else {
                        var comments = ticket.putArray("comments");
                        
                        for(JsonNode commentNode: commentsNode) {
                            ObjectNode comment = JsonNodeFactory.instance.objectNode();
                            {
                                JsonNode author = commentNode.path("author");
                                if (!author.isMissingNode()) {
                                    comment.put("author", assignee.path("emailAddress").asText());
                                } else {
                                    comment.putNull("author");
                                }
                                JsonNode bodyNode = commentNode.path("body");
                                if (bodyNode.isMissingNode()) {
                                    comment.put("content", "");
                                } else {                
                                    String content = new MarkDownDocumentMapper().fromAtlassianDocumentFormat(bodyNode);
                                    comment.put("description", content);
                                }
             
                                comment.put("createdDate", issueNode.path("created").asText());
                                comment.put("updatedDate", issueNode.path("updated").asText());
                            }
                            comments.add(comment);
                        }
                    } 
                }
            }
        }

        return ticket;
    }

    static class Request {
        @JsonPropertyDescription("ticket key. of the form LETTERS-NUMBER, e.g. SUP-123, ENG-456")
        @JsonProperty(required = true)
        public String key;
    }
}



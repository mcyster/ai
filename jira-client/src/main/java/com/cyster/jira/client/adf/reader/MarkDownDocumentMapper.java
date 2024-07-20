package com.cyster.jira.client.adf.reader;

import java.util.Iterator;

import com.fasterxml.jackson.databind.JsonNode;

public class MarkDownDocumentMapper {

    public String fromAtlassianDocumentFormat(JsonNode document) {
        JsonNode typeNode = document.path("type");
        if (typeNode.isMissingNode() || !typeNode.asText().equals("doc")) {
            throw new IllegalArgumentException("Provided node is not an Atlassian Document");
        }
        
        MarkDownBuilder markDownBuilder = new MarkDownBuilder();
        Iterator<JsonNode> contentNodes = document.path("content").elements();
        
        while (contentNodes.hasNext()) {
            JsonNode contentNode = contentNodes.next();
            markDownBuilder.processContentNode(contentNode);
        }
        
        return markDownBuilder.getMarkDown();
    }
}

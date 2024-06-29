package com.cyster.adf;

import com.fasterxml.jackson.databind.JsonNode;

// https://github.com/vsch/flexmark-java/wiki/Usage
// https://developer.atlassian.com/cloud/jira/platform/apis/document/structure/
// https://unpkg.com/@atlaskit/adf-schema@35.5.1/dist/json-schema/v1/full.json

public class AtlassianDocumentMapper {

    public AtlassianDocumentMapper() {
    }

    public JsonNode fromMarkdown(String markdown) {
        var visitor = new AtlassianDocumentMarkdownVisitor();
  
        JsonNode document = visitor.generate(markdown);
        
        return document;
    }
}

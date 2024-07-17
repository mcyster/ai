package com.cyster.adf.reader;

import com.fasterxml.jackson.databind.JsonNode;

class MarkDownBuilder {
    private final StringBuilder markDown = new StringBuilder();

    public void processContentNode(JsonNode contentNode) {
        switch (contentNode.path("type").asText()) {
            case "paragraph" -> handleParagraph(contentNode);
            case "blockquote" -> handleBlockquote(contentNode);
            case "bulletList" -> handleBulletList(contentNode);
            case "codeBlock" -> handleCodeBlock(contentNode);
            case "heading" -> handleHeading(contentNode);
            case "listItem" -> handleListItem(contentNode);
            case "orderedList" -> handleOrderedList(contentNode);
            case "text" -> handleText(contentNode);
            default -> handleUnsupported(contentNode);
        }
    }

    public String getMarkDown() {
        return markDown.toString();
    }

    private void handleParagraph(JsonNode contentNode) {
        var subContentNodes = contentNode.path("content").elements();
        while (subContentNodes.hasNext()) {
            var subContentNode = subContentNodes.next();
            if (subContentNode.path("type").asText().equals("text")) {
                markDown.append(subContentNode.path("text").asText()).append("\n\n");
            }
        }
    }

    private void handleBlockquote(JsonNode contentNode) {
        markDown.append("> ");
        handleParagraph(contentNode);
    }

    private void handleBulletList(JsonNode contentNode) {
        var listItemNodes = contentNode.path("content").elements();
        while (listItemNodes.hasNext()) {
            var listItemNode = listItemNodes.next();
            markDown.append("* ");
            handleListItem(listItemNode);
        }
    }

    private void handleCodeBlock(JsonNode contentNode) {
        markDown.append("```\n");
        handleText(contentNode);
        markDown.append("```\n");
    }

    private void handleHeading(JsonNode contentNode) {
        int level = contentNode.path("attrs").path("level").asInt();
        markDown.append("#".repeat(level)).append(" ");
        handleText(contentNode);
    }

    private void handleListItem(JsonNode contentNode) {
        handleParagraph(contentNode);
    }

    private void handleOrderedList(JsonNode contentNode) {
        var listItemNodes = contentNode.path("content").elements();
        int counter = 1;
        while (listItemNodes.hasNext()) {
            var listItemNode = listItemNodes.next();
            markDown.append(counter).append(". ");
            handleListItem(listItemNode);
            counter++;
        }
    }

    private void handleText(JsonNode contentNode) {
        String text = contentNode.path("text").asText();
        
        if (contentNode.has("marks")) {
            var marks = contentNode.path("marks").elements();
            while (marks.hasNext()) {
                var mark = marks.next();
                switch (mark.path("type").asText()) {
                    case "code" -> text = "`" + text + "`";
                    case "em" -> text = "*" + text + "*";
                    case "link" -> text = "[" + text + "](" + mark.path("attrs").path("href").asText() + ")";
                    case "strike" -> text = "~~" + text + "~~";
                    case "strong" -> text = "**" + text + "**";
                    case "subsup" -> text = mark.path("attrs").path("type").asText().equals("sub") ? "~" + text + "~" : "^" + text + "^";
                    case "textColor" -> text = "<span style=\"color:" + mark.path("attrs").path("color").asText() + "\">" + text + "</span>";
                    case "underline" -> text = "<u>" + text + "</u>";
                }
            }
        }
        
        markDown.append(text);
    }

    private void handleUnsupported(JsonNode contentNode) {
        var subContentNodes = contentNode.path("content").elements();
        while (subContentNodes.hasNext()) {
            var subContentNode = subContentNodes.next();
            if (subContentNode.path("type").asText().equals("text")) {
                markDown.append(subContentNode.path("text").asText()).append(" ");
            }
        }
    }
}

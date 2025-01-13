package com.extole.zuper.weave.scenarios.support.tools.jira;

import java.util.Objects;

import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.Weave;
import com.cyster.ai.weave.service.tool.ToolException;
import com.cyster.jira.client.ticket.TicketAttachmentBuilder;
import com.cyster.jira.client.ticket.TicketException;
import com.cyster.weave.impl.scenarios.webshot.AssetProvider;
import com.cyster.weave.impl.scenarios.webshot.AssetProvider.Asset;
import com.extole.jira.support.SupportTicketService;
import com.extole.zuper.weave.ExtoleSuperContext;
import com.extole.zuper.weave.scenarios.support.tools.ExtoleSupportTool;
import com.extole.zuper.weave.scenarios.support.tools.jira.SupportTicketAssetAddTool.Request;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Component
class SupportTicketAssetAddTool implements ExtoleSupportTool<Request> {
    private final SupportTicketService supportTicketService;
    private final AssetProvider assetProvider;

    SupportTicketAssetAddTool(SupportTicketService supportTicketService, AssetProvider assetProvider) {
        this.supportTicketService = supportTicketService;
        this.assetProvider = assetProvider;
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName().replace("Tool", "");
    }

    @Override
    public String getDescription() {
        return "Add a binary asset, like an image, to the specified ticket";
    }

    @Override
    public Class<Request> getParameterClass() {
        return Request.class;
    }

    @Override
    public Class<ExtoleSuperContext> getContextClass() {
        return ExtoleSuperContext.class;
    }

    @Override
    public Object execute(Request request, ExtoleSuperContext context, Weave weave) throws ToolException {
        if (request.key == null || request.key.isEmpty()) {
            throw new ToolException("Attribute 'key' not specified");
        }

        if (request.assetName == null || request.assetName.isEmpty()) {
            throw new ToolException("Attribute 'assetName' must be specified");
        }

        if (request.assetType == null || request.assetType.isEmpty()) {
            throw new ToolException("Attribute 'assetType' must be specified");
        }

        com.cyster.jira.client.ticket.TicketAttachmentBuilder.Attachment attachment = null;
        try {
            TicketAttachmentBuilder builder = supportTicketService.ticketAttachmentBuilder(request.key);

            assetProvider.getAsset(new Asset(request.assetName, AssetProvider.Type.fromString(request.assetType)),
                    inputStream -> builder.withAsset(request.assetName, request.assetType, inputStream));

            attachment = builder.post();
        } catch (TicketException exception) {
            throw new ToolException("Error adding comment to ticket: " + request.key, exception);
        }

        ObjectNode response = JsonNodeFactory.instance.objectNode();
        response.put("key", request.key);
        response.put("attachmentUrl", attachment.uri().toString());

        return response;
    }

    public int hash() {
        return Objects.hash(getName(), getDescription(), getParameterClass());
    }

    static record Request(@JsonProperty(required = true) @JsonPropertyDescription("ticket key") String key,
            @JsonProperty(required = true) @JsonPropertyDescription("Name of asset") String assetName,
            @JsonProperty(required = true) @JsonPropertyDescription("Type of asset (e.g. png)") String assetType) {
    }

}

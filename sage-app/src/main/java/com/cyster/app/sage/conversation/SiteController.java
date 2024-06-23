package com.cyster.app.sage.conversation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.cyster.ai.weave.service.conversation.Conversation;
import com.cyster.ai.weave.service.conversation.ConversationException;
import com.cyster.ai.weave.service.conversation.Message;
import com.cyster.ai.weave.service.scenario.Scenario;
import com.cyster.ai.weave.service.scenario.ScenarioException;
import com.cyster.ai.weave.service.scenario.ScenarioService;
import com.cyster.app.sage.site.WebsiteServiceImpl;
import com.cyster.sage.impl.advisors.web.WebsiteService;
import com.cyster.sage.impl.advisors.web.WebsiteService.Website;
import com.cyster.sage.service.scenariosession.ScenarioSession;
import com.cyster.sage.service.scenariosession.ScenarioSessionStore;
import com.extole.sage.session.ExtoleSessionContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.jakarta.JsonSchemaGenerator;


@RestController
public class SiteController {
    private static final Logger logger = LogManager.getLogger(SiteController.class);

    private WebsiteServiceImpl sites;
    
    public SiteController(WebsiteServiceImpl sites) {
        this.sites = sites;
    }

    @GetMapping("/pages")
    public List<Website> index() {
        return sites.getSites();
    }

    @PostMapping("/pages/{id}")
    public Website getSite(
       @PathVariable("id") String id)
        throws ScenarioNameNotSpecifiedRestException, ScenarioNameNotFoundRestException,ScenarioParametersException, ScenarioContextException {
           
        return sites.getSite(id);            
    }
}

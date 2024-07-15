package com.cyster.web.rest.controller;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.cyster.web.developer.advisors.WebsiteService.Website;
import com.cyster.web.rest.WebsiteServiceImpl;

@RestController
public class SiteController {
    private static final Logger logger = LogManager.getLogger(SiteController.class);

    private WebsiteServiceImpl sites;

    public SiteController(WebsiteServiceImpl sites) {
        this.sites = sites;
    }

    @GetMapping("/pages")
    public List<WebsiteResponse> getSites() {
        List<WebsiteResponse> websites = new ArrayList<>();

        for(var site: sites.getSites()) {
            websites.add(new WebsiteResponse(site.getId(), site.getType(), site.getUri()));
        }

        return websites;
    }

    @PostMapping("/pages/{name}")
    public WebsiteResponse getSite(
        @PathVariable("name") String name) {

        var site = sites.getSite(name);

        return new WebsiteResponse(site.getId(), site.getType(), site.getUri());
    }

    @PostMapping("/pages/{name}/copy")
    public WebsiteResponse copySite(
        @PathVariable("name") String name) {

        var site = sites.getSite(name);
        Website newSite = sites.copy(site);

        return new WebsiteResponse(newSite.getId(), newSite.getType(), newSite.getUri());
    }

    @PutMapping("/pages/{name}")
    public WebsiteResponse nameSite(
        @PathVariable("name") String name,
        @RequestBody NameRequest request){

        var site = sites.getSite(name);
        Website newSite = sites.name(site, request.name());

        return new WebsiteResponse(newSite.getId(), newSite.getType(), newSite.getUri());
    }


    static record WebsiteResponse (String name, Website.Type type, URI uri) {};
    static record NameRequest (String name) {};

}

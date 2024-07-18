package com.cyster.web.rest.controller;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cyster.web.rest.LocalWebsiteProvider;
import com.cyster.web.weave.scenarios.ManagedWebsites;
import com.cyster.web.weave.scenarios.ManagedWebsites.ManagedWebsite;
import com.cyster.web.weave.scenarios.WebsiteProvider.Website;

@RestController
public class SiteController {
    private ManagedWebsites managedWebsites;

    public SiteController(LocalWebsiteProvider sites) {
        this.managedWebsites = new ManagedWebsites(sites);
    }

    @GetMapping("/pages")
    public List<WebsiteResponse> getSites(
        @RequestParam(value = "tags", required = false, defaultValue = "") String tags) {
        List<WebsiteResponse> websites = new ArrayList<>();

        List<String> tagList = Arrays.stream(tags.split(","))
            .map(String::trim)
            .filter(tag -> !tag.isEmpty())
            .collect(Collectors.toList());
        
        for(var website: managedWebsites.getSites(tagList)) {
            websites.add(create(website));
        }

        return websites;
    }

    @PostMapping("/pages/{name}")
    public WebsiteResponse getSite(
        @PathVariable("name") String name) {

        var site = managedWebsites.getSite(name);

        return create(site);
    }

    @PostMapping("/pages/{name}/copy")
    public WebsiteResponse copySite(
        @PathVariable("name") String name) {

        var site = managedWebsites.getSite(name);
        var newSite = managedWebsites.copy(site);

        return create(newSite);
    }

    @PutMapping("/pages/{name}")
    public WebsiteResponse nameSite(
        @PathVariable("name") String name,
        @RequestBody NameRequest request){

        var site = managedWebsites.getSite(name);
        var newSite = managedWebsites.name(site, request.name());

        return create(newSite);
    }

    private static WebsiteResponse create(ManagedWebsite website) {
        return new WebsiteResponse(website.site().getId(), website.name(), website.tags(), website.site().getType(), website.site().getUri());
    }
    
    static record WebsiteResponse (String id, String name, List<String> tags, Website.Type type, URI uri) {};
    static record NameRequest (String name) {};

}

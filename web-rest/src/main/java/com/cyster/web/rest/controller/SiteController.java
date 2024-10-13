package com.cyster.web.rest.controller;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cyster.web.rest.LocalWebsiteProvider;
import com.cyster.web.weave.scenarios.ManagedWebsites;
import com.cyster.web.weave.scenarios.ManagedWebsites.ManagedWebsite;
import com.cyster.web.weave.scenarios.WebsiteException;
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
        @PathVariable("name") String name) throws WebsiteException {

        var site = managedWebsites.getSite(name);

        return create(site);
    }

    private static WebsiteResponse create(ManagedWebsite website) {
        return new WebsiteResponse(website.site().getId(), website.name(), website.tags(), website.site().getType(), website.site().getUri());
    }
    
    static record WebsiteResponse (String id, String name, List<String> tags, Website.Type type, URI uri) {};

}

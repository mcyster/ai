package com.cyster.web.weave.scenarios;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.cyster.web.weave.scenarios.WebsiteProvider.Website;
import com.cyster.web.weave.scenarios.WebsiteProvider.Website.Asset;
import com.cyster.web.weave.scenarios.WebsiteProvider.Website.Type;

public class ManagedWebsites {
    private static final List<String> RESERVED_TAGS = Arrays.asList("managed", "unmanaged", "start", "curated");

    private WebsiteProvider websiteProvider;
    private static List<String> curatedSites;

    static {
        curatedSites = new ArrayList<>();
        curatedSites.add("start-here");
        curatedSites.add("blank");
        curatedSites.add("graph-co2");
        curatedSites.add("game-of-life");
    }
    
    public ManagedWebsites(WebsiteProvider websiteProvider) {
        this.websiteProvider = websiteProvider;
    }
    
    public List<ManagedWebsite> getSites(List<String> tags) {
        return websiteProvider.getSites().stream()
            .map(this::create)
            .filter(site -> tags.isEmpty() || tags.stream()
                .allMatch(inputTag -> site.tags().stream()
                    .anyMatch(tag -> inputTag.equalsIgnoreCase(tag))
                )
            )
            .collect(Collectors.toList());
    }
    
    public ManagedWebsite getSite(String id) {
        return create(websiteProvider.getSite(id));
    }
    
    public ManagedWebsite create() {
        return create(websiteProvider.create());
    }
    
    public ManagedWebsite copy(ManagedWebsite website) {
        return create(websiteProvider.copy(website.site));
    }

    private ManagedWebsite create(Website website) {
        String name = website.getId();
        List<String> tags = new ArrayList<>();
        
        Asset indexAsset = website.getAsset("index.html");
        Document document = Jsoup.parse(indexAsset.content());

        name = document.title();

        Element metaTags = document.selectFirst("meta[name=tags]");
        if (metaTags != null) {
            String tagsContent = metaTags.attr("content");
            tags = Arrays.stream(tagsContent.split(","))
                .map(String::trim)
                .filter(tag -> !tag.isEmpty())
                .collect(Collectors.toList());
        }
        
        tags.removeAll(RESERVED_TAGS);
        
        if (website.getType() == Type.Managed) {
            tags.add("managed");
        } else {
            tags.add("unmanaged"); 
        }
        
        if (website.getId().equals("start-here")) {
            tags.add("start");
        }
        if (curatedSites.contains(website.getId())) {
            tags.add("curated");
        }
        
        return new ManagedWebsite(website, name, tags);
    }
        
    public record ManagedWebsite(Website site, String name, List<String> tags) {};
}

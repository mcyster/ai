package com.cyster.app.sage.site;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.cyster.sage.impl.advisors.web.WebsiteService;
import com.cyster.sage.impl.advisors.web.WebsiteService.Website.Type;

public class WebsiteServiceImpl implements WebsiteService {
    private URI baseUri;
    private Path baseDirectory;
    
    public WebsiteServiceImpl(URI baseUri, Path baseDirectory) {
        this.baseUri = baseUri;
        this.baseDirectory = baseDirectory;
    }

    public List<Website> getSites() {
        List<Website> sites = new ArrayList<>();
        
        for(Type type: Website.Type.values()) {
            Path typeRoot = baseDirectory.resolve(type.toString().toLowerCase());
            
            try (Stream<Path> paths = Files.walk(typeRoot, 1)) {
                var typedSites = paths
                    .filter(Files::isDirectory)
                    .filter(path -> !path.equals(typeRoot)) 
                    .map(path -> new WebsiteImpl(this.baseUri, this.baseDirectory, path.getFileName().toString(), type))
                    .collect(Collectors.toList());
                sites.addAll(typedSites);
            } catch (IOException exception) {
                throw new RuntimeException(exception);
            }
        }
        
        return sites;
    }
    
    public Website getSite(String name) {
        Type websiteType = null;
        for(var type: Website.Type.values()) {
            var websiteRoot = baseDirectory.resolve(type.toString().toLowerCase()).resolve(name);
            if (Files.exists(websiteRoot)) {
                websiteType = type;
                break;
            }
        }
        if (websiteType == null) {
            throw new RuntimeException("Wesbite not found: " + name);
        }
        
        return new WebsiteImpl(this.baseUri, baseDirectory, name, websiteType);
    }
    
    @Override
    public Website create() {
        String name = UUID.randomUUID().toString();
        return create(Type.Temporary, name);
    }

    @Override
    public Website name(Website website, String name) {
        Path directory = baseDirectory.resolve(Type.Named.toString().toLowerCase()).resolve(name);
        if (Files.exists(directory)) {
            throw new RuntimeException("Wesbite with that name already exists: " + name);
        }

        var namedWebsite = create(Type.Named, name);
        
        for(var assetName: website.getAssets()) {
            namedWebsite.putAsset(assetName, website.getAsset(assetName).content());
        }
        
        return namedWebsite;
    }

    @Override
    public Website copy(Website website) {
        var newWebsite = create();
               
        clone(website, newWebsite);
        
        return newWebsite;
    }
    
    private Website create(Type type, String name) {
        Path directory = baseDirectory.resolve(type.toString().toLowerCase()).resolve(name);

        try {
            Files.createDirectories(directory);
        } catch (IOException e) {
            throw new RuntimeException("Unable to create site directory: " + directory.toString());
        }
                
        return new WebsiteImpl(baseUri, baseDirectory, name, type);
    }
    
    private Website clone(Website fromWebsite, Website toWebsite) {
        for(var assetName: fromWebsite.getAssets()) {
            toWebsite.putAsset(assetName, fromWebsite.getAsset(assetName).content());
        }   
        return toWebsite;
    }
}

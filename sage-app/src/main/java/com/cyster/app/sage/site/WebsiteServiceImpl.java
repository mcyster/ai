package com.cyster.app.sage.site;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.cyster.sage.impl.advisors.web.WebsiteService;

public class WebsiteServiceImpl implements WebsiteService {
    private URI baseUri;
    private Path baseDirectory;
    
    public WebsiteServiceImpl(URI baseUri, Path baseDirectory) {
        this.baseUri = baseUri;
        this.baseDirectory = baseDirectory;
    }

    public List<Website> getSites() {
        List<Website> sites = new ArrayList<>();
        
        try (Stream<Path> paths = Files.walk(baseDirectory, 1)) {
            sites = paths
                .filter(Files::isDirectory)
                .filter(path -> !path.equals(baseDirectory)) 
                .map(path -> new WebsiteImpl(this.baseUri, path.getFileName()))
                .collect(Collectors.toList()); 
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        
        return sites;
    }
    
    public Website getSite(String name) {
        Path root = baseDirectory.resolve(name);
        if (!Files.exists(root)) {
            throw new RuntimeException("Not Found: " + name);
        }
        return new WebsiteImpl(this.baseUri, root);
    }
    
    @Override
    public Website create() {
        String name = UUID.randomUUID().toString();
        Path directory = baseDirectory.resolve(name);

        try {
            Files.createDirectories(directory);
        } catch (IOException e) {
            throw new RuntimeException("Unable to create site directory: " + directory.toString());
        }
        
        //URI siteUri = this.baseUri.resolve(name).resolve("index.html");
        URI siteUri;
        try {
            siteUri = new URI(baseUri.toString() + "/" + name + "/index.html");
        } catch (URISyntaxException e) {
            throw new RuntimeException("Unable to create site baseUri: " + directory.toString());
        }
        
        return new WebsiteImpl(siteUri, directory);
    }
}

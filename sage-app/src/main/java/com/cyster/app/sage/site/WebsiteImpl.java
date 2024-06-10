package com.cyster.app.sage.site;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import com.extole.sage.advisors.web.WebsiteService.Website;

class WebsiteImpl implements Website {
    private final URI uri; 
    private final Path root;
    
    public WebsiteImpl(URI uri, Path root) {
        this.uri = uri;
        this.root = root;
    }
    
    public String getId() {
        return root.getFileName().toString();
    }
    
    @Override
    public URI getUri() {
        return this.uri;
    }

    @Override
    public Website putAsset(String name, String content) {
        try {
            if (!Files.exists(root)) {
                Files.createDirectories(root);
            }
        } catch (IOException exception) {
            throw new RuntimeException("Unable to create page directory", exception); 
        }
            
        Path file = root.resolve(name);
        if (!Files.exists(file)) {          
            try {
                Files.createFile(file);
            } catch (IOException exception) {
                throw new RuntimeException("Unable to create file: " + name, exception); 
            }
        }
           
        try {
            Files.write(file, content.getBytes(StandardCharsets.UTF_8));
        } catch (IOException exception) {
            throw new RuntimeException("Unable to write content to file", exception); 
        }
        
        return this; 
    }
}

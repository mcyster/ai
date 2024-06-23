package com.cyster.app.sage.site;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.cyster.sage.impl.advisors.web.WebsiteService.Website;

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
    public List<String> getAssets() {
        List<String> assets = new ArrayList<>();
        
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(root)) {
            for (Path entry : stream) {
                assets.add(entry.getFileName().toString());
            }
        } catch (IOException | DirectoryIteratorException exception) {
            throw new RuntimeException("Unable to find website directory: " + root, exception);   
        }

        return assets;
    };
    
    @Override
    public Asset putAsset(String name, String content) {
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
        
        return new TextAsset(name, content); 
    }

    @Override
    public Asset getAsset(String name) {
        if (!Files.exists(root)) {
            throw new RuntimeException("Unable to find website directory: " + root); 
        }
            
        Path file = root.resolve(name);
        if (!Files.exists(file)) {
            throw new RuntimeException("Unable to find file: " + name); 
        }
        
        String content;
        try {
            content = Files.readString(file);
        } catch (IOException exception) {
            throw new RuntimeException("Unable to read file: " + name);    
        }
                  
        return new TextAsset(name, content); 
    }
    
    public static record TextAsset(String filename, String content) implements Asset {}


}

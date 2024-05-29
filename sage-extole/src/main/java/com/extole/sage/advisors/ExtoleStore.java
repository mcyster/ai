package com.extole.sage.advisors;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.advisor.AdvisorService;
import com.cyster.ai.weave.service.advisor.SearchTool;

@Component
public class ExtoleStore {
    private static String remoteJavaApiRepository = "https://github.com/extole/java-api.git";
    private static File localJavaApiRepository = new File("/tmp/extole/java-api");
    private AdvisorService advisorService;
    private String extoleGithubUsername;
    private String extoleGithubToken;
    
    ExtoleStore(AdvisorService advisorService, 
            @Value("${EXTOLE_GITHUB_API_KEY}") String extoleGithubApiKey) {
        this.advisorService = advisorService;
        if (extoleGithubApiKey == null) {
            throw new IllegalArgumentException("EXTOLE_GIT_HUB_API_KEY not found");
        }
        String[] key = extoleGithubApiKey.split(":", 2);
        if (key.length != 2) {
            throw new IllegalArgumentException("EXTOLE_GITHUB_API_KEY needs to be of the form USER:TOKEN");
        }
        this.extoleGithubUsername = key[0];
        this.extoleGithubToken = key[1];
                
        
        load();
    }

    public <CONTEXT> SearchTool<CONTEXT> createStoreTool() {
        load();    
        
        @SuppressWarnings("unchecked")  // TBD
        SearchTool.Builder<CONTEXT> builder = (SearchTool.Builder<CONTEXT>) advisorService.searchToolBuilder()
            .withName("extole-store");
       
        
        try (Stream<Path> paths = Files.walk(Paths.get(localJavaApiRepository.toURI()))) {
            paths
                .filter(Files::isRegularFile)
                .filter(p -> !hasDotInPath(p))
                .forEach(file -> builder.addDocument(file.toFile()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return builder.create();
    }
    
    private static boolean hasDotInPath(Path path) {
        for (Path part : path) {
            if (part.getFileName().toString().startsWith(".")) {
                return true;
            }
        }
        return false;
    }
    
    private void load() {
        if (!localJavaApiRepository.exists()) {
            try {
                Git.cloneRepository()
                    .setURI(remoteJavaApiRepository)
                    .setDirectory(localJavaApiRepository)
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider(extoleGithubUsername, extoleGithubToken))
                    .call();
                System.out.println("Repository cloned successfully!");
            } catch (GitAPIException e) {
                e.printStackTrace();
            }
        }
    }
}

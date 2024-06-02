package com.extole.sage.advisors;

import java.io.File;
import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.HttpTransport;
import org.eclipse.jgit.transport.TransportHttp;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.transport.http.HttpConnection;
import org.eclipse.jgit.transport.http.HttpConnectionFactory;
import org.eclipse.jgit.util.HttpSupport;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import org.eclipse.jgit.transport.ssh.jsch.JschConfigSessionFactory;
import org.eclipse.jgit.transport.ssh.jsch.OpenSshConfig;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.transport.Transport;
import org.eclipse.jgit.util.FS;

import com.cyster.ai.weave.service.advisor.AdvisorService;
import com.cyster.ai.weave.service.advisor.SearchTool;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
 
@Component

public class ExtoleStore {
    // private static String remoteJavaApiRepository = "https://github.com/extole/java-api.git";
    private static String remoteJavaApiRepository = "git@github.com:extole/java-api.git";
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
                
        System.out.println("!!! Github username: " + this.extoleGithubUsername);
        System.out.println("!!! Github token:    " + this.extoleGithubToken);

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
                .filter(path -> !hasDotInPath(path))
                .forEach(file -> builder.addDocument(file.toFile()));
        } catch (IOException exception) {
            exception.printStackTrace();
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
                System.setProperty("org.eclipse.jgit.util.log", "DEBUG");
                
                String sshKeyPath = "/home/mcyster/.ssh/id_rsa";
                
                JschConfigSessionFactory sshSessionFactory = new JschConfigSessionFactory() {
                    @Override
                    protected void configure(OpenSshConfig.Host host, Session session) {
                        // Optional configurations can be added here
                    }

                    @Override
                    protected JSch createDefaultJSch(FS fs) throws JSchException {
                        JSch jsch = super.createDefaultJSch(fs);
                        jsch.addIdentity(sshKeyPath);
                        return jsch;
                    }
                };

                        
                Git.cloneRepository()
                    .setURI(remoteJavaApiRepository)
                    .setDirectory(localJavaApiRepository)
                    .setTransportConfigCallback(transport -> {
                        if (transport instanceof SshTransport) {
                            ((SshTransport) transport).setSshSessionFactory(sshSessionFactory);
                        }
                    })
                    //.setCredentialsProvider(new UsernamePasswordCredentialsProvider(extoleGithubUsername, extoleGithubToken))
                    .call();
                
                System.out.println("Repository cloned successfully!");
            } catch (GitAPIException e) {
                e.printStackTrace();
            }
        }
    }
    
    
}

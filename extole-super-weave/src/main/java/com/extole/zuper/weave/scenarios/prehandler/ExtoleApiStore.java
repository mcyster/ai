package com.extole.zuper.weave.scenarios.prehandler;

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.transport.Transport;
import org.eclipse.jgit.transport.ssh.jsch.JschConfigSessionFactory;
import org.eclipse.jgit.transport.ssh.jsch.OpenSshConfig;
import org.eclipse.jgit.util.FS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.AiWeaveService;
import com.cyster.ai.weave.service.SearchTool;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;

@Component
public class ExtoleApiStore {
    private static final Logger logger = LoggerFactory.getLogger(ExtoleApiStore.class);

    private static String remoteJavaApiRepository = "git@github.com:extole/java-api.git";
    private static File localJavaApiRepository = new File("/tmp/extole/java-api");
    private AiWeaveService aiWeaveService;

    ExtoleApiStore(AiWeaveService aiWeaveService) {
        this.aiWeaveService = aiWeaveService;
    }

    public <CONTEXT> SearchTool<CONTEXT> createStoreTool() {
        String hash = loadOrUpdateLocalRepository();

        var documentStore = aiWeaveService.directoryDocumentStoreBuilder()
                .withDirectory(localJavaApiRepository.toPath()).withHash(hash).create();

        @SuppressWarnings("unchecked") // TBD
        SearchTool.Builder<CONTEXT> builder = (SearchTool.Builder<CONTEXT>) aiWeaveService.searchToolBuilder()
                .withName("extole-store").withDocumentStore(documentStore);

        return builder.create();
    }

    private String loadOrUpdateLocalRepository() {
        logger.debug("loadOrUpdateLocalRepositorty from: " + remoteJavaApiRepository + " to: "
                + localJavaApiRepository.toString());

        // Define a custom SshSessionFactory that specifies the identity file
        SshSessionFactory sshSessionFactory = new JschConfigSessionFactory() {
            @Override
            protected void configure(OpenSshConfig.Host host, com.jcraft.jsch.Session session) {
                // Configure any session parameters if needed
            }

            @Override
            protected JSch createDefaultJSch(FS fs) throws JSchException {
                String idPath = System.getProperty("user.home") + "/.ssh/id_rsa";
                System.out.println("XXXXXX Create default ssh key from: " + idPath);
                JSch jsch = super.createDefaultJSch(fs);
                jsch.addIdentity(System.getProperty(idPath));
                return jsch;
            }
        };

        // Set up the TransportConfigCallback to apply the custom SshSessionFactory
        TransportConfigCallback transportConfigCallback = new TransportConfigCallback() {
            @Override
            public void configure(Transport transport) {
                System.out.println("XXXXXX Transport: " + transport.toString());

                if (transport instanceof org.eclipse.jgit.transport.SshTransport) {
                    ((org.eclipse.jgit.transport.SshTransport) transport).setSshSessionFactory(sshSessionFactory);
                }
            }
        };

        if (!localJavaApiRepository.exists()) {
            logger.debug("creating locale repository as it was not found at: " + localJavaApiRepository.toString());

            try {
                Git.cloneRepository().setURI(remoteJavaApiRepository).setDirectory(localJavaApiRepository)
                        .setTransportConfigCallback(transportConfigCallback).call();
            } catch (GitAPIException exception) {
                logger.error("Unable to clone the java api repository: " + remoteJavaApiRepository, exception);
            }
        } else {
            logger.debug("updating locale repository at: " + localJavaApiRepository.toString());

            try {
                Git git = Git.open(localJavaApiRepository);
                git.pull().call();
            } catch (IOException | GitAPIException exception) {
                logger.error("Unable to update the java api repository: " + localJavaApiRepository, exception);
            }
        }

        String latestCommitHash = null;
        try {
            Git git = Git.open(localJavaApiRepository);

            Iterable<RevCommit> log = git.log().setMaxCount(1).call();
            for (RevCommit commit : log) {
                latestCommitHash = commit.getName();
                break;
            }

        } catch (IOException | GitAPIException exception) {
            logger.error("Unable to update the java api repository: " + localJavaApiRepository, exception);
        }

        return latestCommitHash;
    }
}

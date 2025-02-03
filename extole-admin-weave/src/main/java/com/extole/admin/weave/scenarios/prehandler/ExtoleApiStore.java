package com.extole.admin.weave.scenarios.prehandler;

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.AiService;
import com.cyster.ai.weave.service.tool.SearchTool;

@Component
public class ExtoleApiStore {
    private static final Logger logger = LoggerFactory.getLogger(ExtoleApiStore.class);

    private static final String remoteJavaApiRepository = "git@github.com:extole/java-api.git";
    private static final File localJavaApiRepository = new File("/tmp/extole/java-api");

    private final AiService aiService;

    ExtoleApiStore(AiService aiService) {
        this.aiService = aiService;
    }

    public <CONTEXT> SearchTool<CONTEXT> createStoreTool(SearchTool.Builder<CONTEXT> builder) {
        String hash = loadOrUpdateLocalRepository();

        var documentStore = aiService.directoryDocumentStoreBuilder().withDirectory(localJavaApiRepository.toPath())
                .withHash(hash).create();

        builder.withName("extole-api");
        builder.withDocumentStore(documentStore);

        return builder.create();
    }

    private String loadOrUpdateLocalRepository() {
        logger.debug("loadOrUpdateLocalRepositorty from: " + remoteJavaApiRepository + " to: "
                + localJavaApiRepository.toString());

        if (!localJavaApiRepository.exists()) {
            logger.debug("Creating locale repository as it was not found at: " + localJavaApiRepository.toString());

            try {
                Git.cloneRepository().setURI(remoteJavaApiRepository).setDirectory(localJavaApiRepository).call();
            } catch (GitAPIException exception) {
                logger.error("Unable to clone the java api repository: " + remoteJavaApiRepository, exception);
            }
        } else {
            logger.debug("Updating local repository at: " + localJavaApiRepository.toString());

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

        logger.debug("Updated local repository at: " + localJavaApiRepository.toString());
        return latestCommitHash;
    }
}
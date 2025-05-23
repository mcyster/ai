package com.extole.zuper.weave.scenarios.prehandler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.AiService;
import com.cyster.ai.weave.service.DocumentStore;

@Component
public class ExtoleApiSuperStore {
    private static final Logger logger = LoggerFactory.getLogger(ExtoleApiSuperStore.class);

    public static final String repositoryName = "api-evalutable";

    private static final String remoteJavaApiRepository = "git@github.com:extole/" + repositoryName + ".git";
    private static final File localJavaApiRepository = Path.of("/tmp/extole", repositoryName).toFile();

    private final AiService aiService;

    ExtoleApiSuperStore(AiService aiService) {
        this.aiService = aiService;
    }

    public DocumentStore getDocumentStore() {
        String hash = loadOrUpdateLocalRepository();

        return aiService.directoryDocumentStoreBuilder().withDirectory(localJavaApiRepository.toPath()).withHash(hash)
                .create();
    }

    private String loadOrUpdateLocalRepository() {
        logger.debug("loadOrUpdateLocalRepositorty from: " + remoteJavaApiRepository + " to: "
                + localJavaApiRepository.toString());

        if (!localJavaApiRepository.exists()) {
            logger.debug("creating locale repository as it was not found at: " + localJavaApiRepository.toString());

            try {
                Git.cloneRepository().setURI(remoteJavaApiRepository).setDirectory(localJavaApiRepository).call();
            } catch (GitAPIException exception) {
                logger.error("Unable to clone the java api repository: " + remoteJavaApiRepository, exception);
            }
        } else {
            logger.debug("updating locale repository at: " + localJavaApiRepository.toString());

            try {
                Git git = Git.open(localJavaApiRepository);

                git.reset().setMode(ResetCommand.ResetType.HARD).call();

                git.clean().setCleanDirectories(true).setForce(true).call();

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
            logger.error("Unable to determine commitish of the java api repository: " + localJavaApiRepository,
                    exception);
        }

        return latestCommitHash;
    }
}

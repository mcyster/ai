package com.extole.weave.scenarios.guides;

import java.io.File;
import java.io.IOException;


import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.AiWeaveService;
import com.cyster.ai.weave.service.SearchTool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// @Component
public class ExtoleGuideStore {
    private static final Logger logger = LoggerFactory.getLogger(ExtoleGuideStore.class);

    private static String remoteJavaApiRepository = "git@github.com:extole/guides.git";
    private static File localJavaApiRepository = new File("/tmp/extole/guides");
    private AiWeaveService aiWeaveService;

    ExtoleGuideStore(AiWeaveService aiWeaveService) {
        this.aiWeaveService = aiWeaveService;
    }

    public <CONTEXT> SearchTool<CONTEXT> createStoreTool() {

        String hash = loadOrUpdateLocalRepository();

        var documentStore = aiWeaveService.directoryDocumentStoreBuilder()
          .withDirectory(localJavaApiRepository.toPath())
          .withHash(hash)
          .create();

        @SuppressWarnings("unchecked")  // TBD
        SearchTool.Builder<CONTEXT> builder = (SearchTool.Builder<CONTEXT>) aiWeaveService.searchToolBuilder()
            .withName("extole-guides")
            .withDocumentStore(documentStore);

        return builder.create();
    }

    private String loadOrUpdateLocalRepository() {
        if (!localJavaApiRepository.exists()) {
            try {
                Git.cloneRepository()
                    .setURI(remoteJavaApiRepository)
                    .setDirectory(localJavaApiRepository)
                    .call();
            } catch (GitAPIException exception) {
                logger.error("Unable to clone the java api repository: " + remoteJavaApiRepository, exception);
            }
        } else {
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

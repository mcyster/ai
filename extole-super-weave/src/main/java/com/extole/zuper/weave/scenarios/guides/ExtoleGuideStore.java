package com.extole.zuper.weave.scenarios.guides;

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyster.ai.weave.service.AiAdvisorService;
import com.cyster.ai.weave.service.AiService;
import com.cyster.ai.weave.service.tool.SearchTool;

// @Component
public class ExtoleGuideStore {
    private static final Logger logger = LoggerFactory.getLogger(ExtoleGuideStore.class);

    private static final String remoteJavaApiRepository = "git@github.com:extole/guides.git";
    private static final File localJavaApiRepository = new File("/tmp/extole/guides");

    private final AiService aiService;
    private final AiAdvisorService aiAdvisorService;

    ExtoleGuideStore(AiService aiService, AiAdvisorService aiAdvisorService) {
        this.aiService = aiService;
        this.aiAdvisorService = aiAdvisorService;
    }

    public SearchTool createStoreTool() {

        String hash = loadOrUpdateLocalRepository();

        var documentStore = aiService.directoryDocumentStoreBuilder().withDirectory(localJavaApiRepository.toPath())
                .withHash(hash).create();

        SearchTool.Builder builder = aiAdvisorService.searchToolBuilder().withName("extole-guides")
                .withDocumentStore(documentStore);

        return builder.create();
    }

    private String loadOrUpdateLocalRepository() {
        if (!localJavaApiRepository.exists()) {
            try {
                Git.cloneRepository().setURI(remoteJavaApiRepository).setDirectory(localJavaApiRepository).call();
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

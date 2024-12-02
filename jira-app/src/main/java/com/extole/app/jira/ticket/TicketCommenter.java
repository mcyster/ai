package com.extole.app.jira.ticket;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import com.cyster.ai.weave.service.conversation.ConversationException;
import com.cyster.ai.weave.service.conversation.Message;
import com.cyster.ai.weave.service.conversation.Message.Type;
import com.cyster.weave.rest.conversation.ScenarioContextException;
import com.extole.app.jira.JiraScenaioContextFactory;
import com.extole.zuper.weave.scenarios.client.ExtoleSupportTicketClientScenario;
import com.extole.zuper.weave.scenarios.runbooks.ExtoleSupportTicketScenario;

@Service
@EnableAsync
public class TicketCommenter {
    private static final Logger logger = LoggerFactory.getLogger(TicketCommenter.class);
    private static final Logger ticketLogger = LoggerFactory.getLogger("tickets");

    private final ExtoleSupportTicketScenario supportTicketScenario;
    private final ExtoleSupportTicketClientScenario supportTicketClientScenario;
    private final JiraScenaioContextFactory jiraScenaioContextFactory;

    public TicketCommenter(ExtoleSupportTicketScenario supportTicketScenario,
            ExtoleSupportTicketClientScenario supportTicketClientScenario,
            JiraScenaioContextFactory jiraScenaioContextFactory) {
        this.supportTicketScenario = supportTicketScenario;
        this.supportTicketClientScenario = supportTicketClientScenario;
        this.jiraScenaioContextFactory = jiraScenaioContextFactory;
    }

    @Async("ticketCommentTaskExecutor")
    public void process(String ticketNumber) {
        processMessage(ticketNumber, Optional.empty());
    }

    @Async("ticketCommentTaskExecutor")
    public void process(String ticketNumber, String prompt) {
        processMessage(ticketNumber, Optional.of(prompt));
    }

    void processMessage(String ticketNumber, Optional<String> prompt) {

        clientForTicket(ticketNumber);

        commentOnTicket(ticketNumber, prompt);
    }

    private void clientForTicket(String ticketNumber) {
        Message response;

        var parameters = new com.extole.zuper.weave.scenarios.client.ExtoleSupportTicketClientScenario.Parameters(
                ticketNumber);
        try {
            var conversation = supportTicketClientScenario
                    .createConversationBuilder(parameters, jiraScenaioContextFactory.createContext()).start();

            response = conversation.respond();
        } catch (ConversationException | ScenarioContextException exception) {
            logger.error("Problem processing ticket: " + ticketNumber, exception);
            return;
        }
        logger.info("Ticket - client idenfified " + ticketNumber + " : " + response);
    }

    private void commentOnTicket(String ticketNumber, Optional<String> prompt) {
        Message response;

        logger.info("Ticket - processing " + ticketNumber + " asynchronously on thread "
                + Thread.currentThread().getName());

        var parameters = new com.extole.zuper.weave.scenarios.runbooks.ExtoleSupportTicketScenario.Parameters(
                ticketNumber);

        try {
            var conversation = supportTicketScenario
                    .createConversationBuilder(parameters, jiraScenaioContextFactory.createContext()).start();

            if (prompt.isPresent()) {
                conversation.addMessage(Type.USER, prompt.get());
            }

            response = conversation.respond();
        } catch (ConversationException | ScenarioContextException exception) {
            logger.error("Problem processing ticket: " + ticketNumber, exception);
            return;
        }

        logger.info("Ticket - processed " + ticketNumber + " : " + response);

        ticketLogger.info(ticketNumber + " " + response.toString());
    }

    @Bean(name = "ticketCommentTaskExecutor")
    public ThreadPoolTaskExecutor getTaskExecutor() {
        var taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(100);
        taskExecutor.setMaxPoolSize(110);
        taskExecutor.setQueueCapacity(120);
        taskExecutor.setThreadNamePrefix("TicketCommenter-");
        taskExecutor.initialize();

        return taskExecutor;
    }
}

package com.extole.app.jira.ticket;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import com.cyster.ai.weave.service.conversation.ConversationException;
import com.cyster.ai.weave.service.conversation.Message;
import com.cyster.ai.weave.service.conversation.Message.Type;
import com.extole.weave.scenarios.client.ExtoleSupportTicketClientScenario;
import com.extole.weave.scenarios.runbooks.ExtoleSupportTicketScenario;

@Service
@EnableAsync
public class TicketCommenter {
    private static final Logger logger = LogManager.getLogger(TicketCommenter.class);
    private static final Logger ticketLogger = LogManager.getLogger("tickets");

    private ExtoleSupportTicketScenario supportTicketScenario;
    private ExtoleSupportTicketClientScenario supportTicketClientScenario;

    public TicketCommenter(ExtoleSupportTicketScenario supportTicketScenario, ExtoleSupportTicketClientScenario supportTicketClientScenario) {
        this.supportTicketScenario = supportTicketScenario;
        this.supportTicketClientScenario = supportTicketClientScenario;
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

        var parameters = new com.extole.weave.scenarios.client.ExtoleSupportTicketClientScenario.Parameters(ticketNumber);        
        try {
            var conversation = supportTicketClientScenario.createConversationBuilder(parameters, null).start();

            response = conversation.respond();
        } catch (ConversationException exception) {
            logger.error("Problem processing ticket: " + ticketNumber, exception);
            return;
        }
        logger.info("Ticket - client idenfified " + ticketNumber + " : " + response);
    }

    private void commentOnTicket(String ticketNumber, Optional<String> prompt) {
    	Message response;

        logger.info("Ticket - processing " + ticketNumber + " asynchronously on thread " + Thread.currentThread().getName());
    	
        var parameters = new com.extole.weave.scenarios.runbooks.ExtoleSupportTicketScenario.Parameters(ticketNumber);

        try {
            var conversation = supportTicketScenario.createConversationBuilder(parameters, null).start();

            if (prompt.isPresent()) {
                conversation.addMessage(Type.USER, prompt.get());
            }

            response = conversation.respond();
        } catch (ConversationException exception) {
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

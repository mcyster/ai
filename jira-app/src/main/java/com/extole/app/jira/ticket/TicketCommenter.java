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
import com.extole.sage.scenarios.support.ExtoleSupportTicketScenario;

@Service
@EnableAsync
public class TicketCommenter {
    private static final Logger logger = LogManager.getLogger(TicketCommenter.class);    
    private static final Logger ticketLogger = LogManager.getLogger("tickets");

    private ExtoleSupportTicketScenario supportTicketScenario;

    public TicketCommenter(ExtoleSupportTicketScenario supportTicketScenario) {
        this.supportTicketScenario = supportTicketScenario; 
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
        logger.info("Ticket - processing " + ticketNumber + " asynchronously on thread " + Thread.currentThread().getName());
        
        var parameters = new ExtoleSupportTicketScenario.Parameters(ticketNumber);
        
        Message response;
        try {
            var conversation = supportTicketScenario.createConversation(parameters, null);
                  
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

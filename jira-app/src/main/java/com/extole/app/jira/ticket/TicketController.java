package com.extole.app.jira.ticket;

import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;

// https://developer.atlassian.com/server/jira/platform/webhooks/

// Grok https://dashboard.ngrok.com/
// Command Line:
//   ngrok http http://localhost:8090
//
// Copy url
//
// Setup webhook:
//   https://extole.atlassian.net/plugins/servlet/webhooks
//   - issue create, comment create

@RestController
public class TicketController {
    private static final String JIRA_APP_ACCOUNT_ID = "712020:ac07cb57-f120-4f67-a1d7-06e69eaae834";
    private static Pattern MENTION_PATTERN = Pattern.compile("\\[\\~accountid:[^\\]]+\\]");
    private static final Set<String> SUPPORTED_PROJECTS = Set.of("help", "sup", "launch", "speed");

    private TicketCommenter ticketCommenter;
    private Optional<String> jiraWebhookSecret = Optional.empty();

    private static final Logger logger = LoggerFactory.getLogger(TicketController.class);
    private static final Logger eventLogger = LoggerFactory.getLogger("events");

    public TicketController(TicketCommenter ticketCommenter,  @Value("${JIRA_WEBHOOK_SECRET:}") String jiraWebhookSecret) {
        if (jiraWebhookSecret == null || jiraWebhookSecret.trim().isEmpty()) {
            logger.warn("Environment variable JIRA_WEBHOOK_SECRET not specified");
        }
        
        this.ticketCommenter = ticketCommenter;
        this.jiraWebhookSecret = Optional.of(jiraWebhookSecret);
    }

    @PostMapping("/ticket")
    public ResponseEntity<String> ticketEvent(
    		@RequestParam("secret") String secret, 
    		@RequestBody JsonNode request) throws BadRequestException, FatalException {
    	
    	logger.info("ticket secret: " + secret + " configuredSecret: " + this.jiraWebhookSecret.orElse(""));
    	
    	if (this.jiraWebhookSecret.isPresent() && secret == null) {
    		logger.error("No secret parameter specified in request");
    	     return ResponseEntity
                 .status(HttpStatus.BAD_REQUEST)
                 .body("No secret specified");
    	}
    
    	if (this.jiraWebhookSecret.isPresent() && !secret.equals(this.jiraWebhookSecret.get())) {
    		logger.error("Secret parameter does not match JIRA_WEBHOOK_SECRET");
   	        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body("Secret does not match configured secret");
    	}
    	
        eventLogger.info(request.toString());

        if (!request.has("issue")) {
            throw new BadRequestException("Unexpected jira event, no issue attribute: " + request.toString());
        }
        if (!request.path("issue").has("key")) {
            throw new BadRequestException("Unexpected jira event, issue attribute has no key: " + request.toString());
        }
        var ticketNumber = request.path("issue").path("key").asText();

        if (!request.has("webhookEvent")) {
            throw new BadRequestException("Unexpected jira event, no webhookEvent attribute: " + request.toString());
        }
        var webhookEvent = request.path("webhookEvent").asText().toLowerCase();

        logger.info("Ticket - checking: " + ticketNumber);

        switch (webhookEvent) {
        case "jira:issue_created":
            if (isSupportedProject(ticketNumber)) {
                logger.info("Ticket - " + ticketNumber + " - issue_created - processing");
                ticketCommenter.process(ticketNumber);
            } else {
                logger.info("Ticket - " + ticketNumber + " - issue_created - ignored - not in a supported project");
            }
            break;

        case "comment_created":
            if (!request.has("comment")) {
                logger.info("Ticket - " + ticketNumber + " - comment_created - has no comment - ignoring");
            }
            else if (!request.get("comment").has("body")) {
                logger.info("Ticket - " + ticketNumber + " - comment_created - comment has no body - ignoring");
            }
            var comment = request.get("comment").get("body").asText();
            Matcher matcher = MENTION_PATTERN.matcher(comment);

            boolean mention = false;
            while (matcher.find()) {
                if (matcher.group().equals("[~accountid:" + JIRA_APP_ACCOUNT_ID + "]")) {
                    mention = true;
                    break;
                }
            }
            if (mention) {
               logger.info("Ticket - " + ticketNumber + " - comment_created - ai mention");

               String cleanedComment = MENTION_PATTERN.matcher(comment).replaceAll("");
               try {
	               if (cleanedComment.isBlank()) {
	                   ticketCommenter.process(ticketNumber);
	               } else {
	                   ticketCommenter.process(ticketNumber, cleanedComment);
	               }
               } catch(Exception exception) {
            	   logger.error("Failed to process ticket - " + ticketNumber, exception);
            	   throw exception;
               }
            }
            break;

        default:
            logger.info("Ticket - " + ticketNumber + " - " + webhookEvent + " ignored");
       }

        return ResponseEntity.ok().build();
    }

    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    static public class BadRequestException extends Exception {
        public BadRequestException(String message) {
            super(message);
        }
    }

    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    static public class FatalException extends Exception {
        public FatalException(String message) {
            super(message);
        }

        public FatalException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    private static boolean isSupportedProject(String ticketNumber) {
        String lowerCaseTicket = ticketNumber.toLowerCase();
        return SUPPORTED_PROJECTS.stream().anyMatch(lowerCaseTicket::startsWith);
    }
}

package com.extole.app.jira.ticket;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
    private static final String JIRA_APP_ACCOUNT_ID = "712020:ac07cb57-f120-4f67-a1d7-06e69eaae834";  // TODO: property
    private static Pattern MENTION_PATTERN = Pattern.compile("\\[\\~accountid:[^\\]]+\\]");

    private TicketCommenter ticketCommenter;

    private static final Logger logger = LogManager.getLogger(TicketController.class);
    private static final Logger eventLogger = LogManager.getLogger("events");

    public TicketController(TicketCommenter ticketCommenter) {
        this.ticketCommenter = ticketCommenter;
    }

    @PostMapping("/ticket")
    public ResponseEntity<Void> ticketEvent(@RequestBody JsonNode request) throws BadRequestException, FatalException {
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
            if (ticketNumber.toLowerCase().startsWith("sup")) {
                logger.info("Ticket - " + ticketNumber + " - issue_created - processing");
                ticketCommenter.process(ticketNumber);
            } else {
                logger.info("Ticket - " + ticketNumber + " issue_created - ignored - only processing SUP tickets");
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
               if (cleanedComment.isBlank()) {
                   ticketCommenter.process(ticketNumber);
               } else {
                   ticketCommenter.process(ticketNumber, cleanedComment);
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

}

package com.extole.tickets.rest.support;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cyster.jira.client.ticket.TicketException;
import com.cyster.jira.client.ticket.impl.Ticket;
import com.extole.jira.support.SupportTicket;
import com.extole.jira.support.SupportTicketService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/support")
public class SupportTicketsController {
    private static final Logger logger = LoggerFactory.getLogger(SupportTicketsController.class);

    private SupportTicketService supportTicketService;
    private Path tempDirectory;
    private final ObjectMapper objectMapper;

    public SupportTicketsController(@Value("${AI_HOME}") String aiHome, SupportTicketService supportTicketService,
            ObjectMapper objectMapper) {
        Path directory = Paths.get(aiHome);
        if (!Files.exists(directory)) {
            throw new IllegalArgumentException("AI_HOME (" + aiHome + ") does not exist");
        }
        if (!Files.isDirectory(directory)) {
            throw new IllegalArgumentException("AI_HOME (" + aiHome + ") is not a directory");
        }
        this.tempDirectory = directory.resolve("tmp");
        if (!Files.isDirectory(tempDirectory)) {
            try {
                Files.createDirectories(tempDirectory);
            } catch (IOException e) {
                throw new RuntimeException("Unable to create temp directory: " + tempDirectory.toString());
            }
        }

        this.supportTicketService = supportTicketService;

        this.objectMapper = objectMapper;

    }

    @GetMapping("/tickets")
    public List<SupportTicket> getTickets(@RequestParam Optional<Integer> limit) throws TicketException {
        return loadTickets(limit);
    }

    @GetMapping("/epics")
    public List<Ticket> getEpics(@RequestParam Optional<Integer> limit) throws TicketException {
        return null; // loadEpics(limit);
    }

    @GetMapping("/tickets/{ticketNumber}")
    public SupportTicket getTicket(@PathVariable String ticketNumber) throws TicketException {
        var ticket = supportTicketService.getTicket(ticketNumber);

        if (ticket.isEmpty()) {
            throw new TicketException("Ticket " + ticketNumber + " not found");
        }
        return ticket.get();
    }

    @Scheduled(initialDelayString = "PT30M", fixedRateString = "PT1H")
    public void performScheduledTask() {
        try {
            loadTickets(Optional.empty());
        } catch (TicketException e) {
            logger.error("Unable to load support tickets", e);
        }
    }

    private List<SupportTicket> loadTickets(Optional<Integer> limit) throws TicketException {
        List<SupportTicket> tickets;

        Path cacheFilename = getCacheFilename("support-tickets-", getHash(limit));
        if (Files.exists(cacheFilename)) {
            String json;
            try {
                json = new String(Files.readAllBytes(cacheFilename));
            } catch (IOException exception) {
                throw new RuntimeException(exception);
            }

            try {
                tickets = objectMapper.readValue(json, new TypeReference<List<SupportTicket>>() {
                });
            } catch (JsonProcessingException exception) {
                throw new RuntimeException(exception);
            }
        } else {
            tickets = fetchTickets(limit);

            try (FileWriter file = new FileWriter(cacheFilename.toString())) {
                file.write(objectMapper.writeValueAsString(tickets));
            } catch (IOException exception) {
                throw new RuntimeException(exception);
            }
        }

        return tickets;
    }

    private List<SupportTicket> fetchTickets(Optional<Integer> limit) throws TicketException {
        var ticketQueryBuilder = supportTicketService.ticketQueryBuilder();

        ticketQueryBuilder.addFilter("(created > startOfMonth(\"-7M\") OR resolved > startOfMonth(\"-7M\"))");
        // ticketQueryBuilder.addFilter("created >= -1w");

        if (limit.isPresent()) {
            ticketQueryBuilder.withLimit(limit.get());
        }

        List<SupportTicket> tickets = ticketQueryBuilder.query();

        return tickets;
    }

    private List<SupportTicket> loadEpics(Optional<Integer> limit) throws TicketException {
        List<SupportTicket> tickets;

        Path cacheFilename = getCacheFilename("support-epics-", getHash(limit));
        if (Files.exists(cacheFilename)) {
            String json;
            try {
                json = new String(Files.readAllBytes(cacheFilename));
            } catch (IOException exception) {
                throw new RuntimeException(exception);
            }

            try {
                tickets = objectMapper.readValue(json, new TypeReference<List<SupportTicket>>() {
                });
            } catch (JsonProcessingException exception) {
                throw new RuntimeException(exception);
            }
        } else {
            tickets = fetchFullEpics(limit);

            try (FileWriter file = new FileWriter(cacheFilename.toString())) {
                file.write(objectMapper.writeValueAsString(tickets));
            } catch (IOException exception) {
                throw new RuntimeException(exception);
            }
        }

        return tickets;
    }

    private List<SupportTicket> fetchFullEpics(Optional<Integer> limit) throws TicketException {
        var ticketQueryBuilder = supportTicketService.ticketQueryBuilder();

        // ticketQueryBuilder.withEpicsOnly();

        if (limit.isPresent()) {
            ticketQueryBuilder.withLimit(limit.get());
        }

        List<SupportTicket> tickets = ticketQueryBuilder.query();

        return tickets;
    }

    private Path getCacheFilename(String name, String uniqueHash) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String date = formatter.format(new Date());

        return tempDirectory.resolve(name + uniqueHash + "-" + date + ".json");
    }

    public static String getHash(Object... parameters) {
        StringBuilder concatenated = new StringBuilder();
        for (Object parameter : parameters) {
            concatenated.append(convertToString(parameter));
        }

        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException exception) {
            throw new RuntimeException("Unable to find digest", exception);
        }
        byte[] hashBytes = digest.digest(concatenated.toString().getBytes());

        StringBuilder hexString = new StringBuilder();
        for (byte b : hashBytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }

        return hexString.toString();
    }

    private static String convertToString(Object parameter) {
        if (parameter instanceof Optional) {
            Optional<?> optionalParam = (Optional<?>) parameter;
            return optionalParam.map(Object::toString).orElse("");
        } else {
            return parameter.toString();
        }
    }

}

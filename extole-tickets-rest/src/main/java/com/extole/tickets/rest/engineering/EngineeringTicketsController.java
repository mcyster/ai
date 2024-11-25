package com.extole.tickets.rest.engineering;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
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

import com.cyster.jira.client.ticket.Ticket;
import com.cyster.jira.client.ticket.TicketException;
import com.extole.jira.engineering.EngineeringTicketService;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/engineering")
public class EngineeringTicketsController {
    private static final Logger logger = LoggerFactory.getLogger(EngineeringTicketsController.class);

    private EngineeringTicketService engineeringTicketService;
    private Path tempDirectory;
    private final ObjectMapper objectMapper;

    public EngineeringTicketsController(@Value("${AI_HOME}") String aiHome,
            EngineeringTicketService engineeringTicketService, ObjectMapper objectMapper) {
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

        this.engineeringTicketService = engineeringTicketService;

        ObjectMapper customizedMapper = objectMapper.copy();
        customizedMapper.configOverride(ZonedDateTime.class)
                .setFormat(JsonFormat.Value.forPattern("yyyy-MM-dd'T'HH:mm:ssXXX"));

        this.objectMapper = customizedMapper;
    }

    @GetMapping("/tickets")
    public List<Ticket> getTickets(@RequestParam Optional<Integer> limit) throws TicketException {
        return loadTickets(limit);
    }

    @GetMapping("/tickets/{ticketNumber}")
    public Ticket getTicket(@PathVariable String ticketNumber) throws TicketException {
        var ticket = engineeringTicketService.getTicket(ticketNumber);

        if (ticket.isEmpty()) {
            throw new TicketException("Ticket " + ticketNumber + " not found");
        }
        return ticket.get();
    }

    @Scheduled(initialDelayString = "PT30M", fixedRateString = "PT1H")
    public void performScheduledTask() {
        try {
            loadTickets(Optional.empty());
        } catch (TicketException exception) {
            logger.error("Unable to load tickets", exception);
        }
    }

    private List<Ticket> loadTickets(Optional<Integer> limit) throws TicketException {
        List<Ticket> tickets;

        Path cacheFilename = getCacheFilename(getHash(limit));
        if (Files.exists(cacheFilename)) {
            String json;
            try {
                json = new String(Files.readAllBytes(cacheFilename));
            } catch (IOException exception) {
                throw new RuntimeException(exception);
            }

            try {
                tickets = objectMapper.readValue(json, new TypeReference<List<Ticket>>() {
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

    private List<Ticket> fetchTickets(Optional<Integer> limit) throws TicketException {
        var ticketQueryBuilder = engineeringTicketService.ticketQueryBuilder();

        ticketQueryBuilder.addFilter("(created > startOfMonth(\"-7M\") OR resolved > startOfMonth(\"-7M\"))");
        // ticketQueryBuilder.addFilter("created > -1w");

        if (limit.isPresent()) {
            ticketQueryBuilder.withLimit(limit.get());
        }

        List<Ticket> tickets = ticketQueryBuilder.query();

        return tickets;
    }

    private Path getCacheFilename(String uniqueHash) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String date = formatter.format(new Date());

        return tempDirectory.resolve("engineer-tickets-" + uniqueHash + "-" + date + ".json");
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

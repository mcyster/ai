package com.extole.tickets.rest.engineering;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.extole.jira.engineering.FullEngineeringTicket;
import com.extole.jira.engineering.EngineeringTicket;
import com.extole.jira.engineering.EngineeringTicketService;
import com.extole.jira.engineering.EngineeringTicketComment;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/engineering")
public class EngineeringTicketsController {
    private EngineeringTicketService engineeringTicketService;
    private Path tempDirectory;
    private final ObjectMapper objectMapper;
    
    public EngineeringTicketsController(
        @Value("${AI_HOME}") String aiHome, 
        EngineeringTicketService engineeringTicketService, 
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

        this.engineeringTicketService = engineeringTicketService;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/tickets")
    public List<EngineeringTicketResponse> getTickets(@RequestParam Optional<Integer> limit) {
        return loadTickets(limit);
    }

    @GetMapping("/tickets/full")
    public List<FullEngineeringTicketResponse> getFullTickets(@RequestParam Optional<Integer> limit) {
        return loadFullTickets(limit);
    }

    @GetMapping("/tickets/{ticketNumber}")
    public FullEngineeringTicketResponse getTickets(@PathVariable String ticketNumber) {
        var ticketQueryBuilder = engineeringTicketService.ticketQueryBuilder();
        
        ticketQueryBuilder.withTicket(ticketNumber);
        
        List<FullEngineeringTicket> tickets = ticketQueryBuilder.query();
        if (tickets.size() == 0) {
            return null;  // not found ...
        }
        
        return FullEngineeringTicketResponse.fromFullEngineeringTicket(tickets.get(0));
    }

    
    @Scheduled(initialDelayString = "PT30M", fixedRateString = "PT1H")
    public void performScheduledTask() {
        loadTickets(Optional.empty());
    }

    private List<EngineeringTicketResponse> loadTickets(Optional<Integer> limit) {
        List<FullEngineeringTicketResponse> tickets = loadFullTickets(limit);

        List<EngineeringTicketResponse> shortTickets = new ArrayList<>();
        for(var ticket: tickets) {
            shortTickets.add(ticket.ticket());
        }
        
        return shortTickets;
    }

    private List<FullEngineeringTicketResponse> loadFullTickets(Optional<Integer> limit) {
        List<FullEngineeringTicketResponse> tickets;

        Path cacheFilename = getCacheFilename(getHash(limit));
        if (Files.exists(cacheFilename)) {
            String json;
            try {
                json = new String(Files.readAllBytes(cacheFilename));
            } catch (IOException exception) {
                throw new RuntimeException(exception);
            }

            try {
                tickets = objectMapper.readValue(json, new TypeReference<List<FullEngineeringTicketResponse>>(){});
            } catch (JsonProcessingException exception) {
                throw new RuntimeException(exception);
            }
        } else {
            tickets = fetchFullTickets(limit);

            try (FileWriter file = new FileWriter(cacheFilename.toString())) {
                file.write(objectMapper.writeValueAsString(tickets));
            } catch (IOException exception) {
                throw new RuntimeException(exception);
            }
        }

        return tickets;
    }
    
    private List<FullEngineeringTicketResponse> fetchFullTickets(Optional<Integer> limit) {
        var ticketQueryBuilder = engineeringTicketService.ticketQueryBuilder();
                
        //ticketQueryBuilder.withTrailing7Months();
        ticketQueryBuilder.withTrailingWeek();
        
        if (limit.isPresent()) {
            ticketQueryBuilder.withLimit(limit.get());
        }
        
        List<FullEngineeringTicket> tickets = ticketQueryBuilder.query();
        List<FullEngineeringTicketResponse> response = new ArrayList<>();
        for(var ticket: tickets) {
            response.add(FullEngineeringTicketResponse.fromFullEngineeringTicket(ticket));
        }
        
        return response;
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

    
    public static record FullEngineeringTicketResponse (
            EngineeringTicketResponse ticket, 
            String description, 
            List<TicketCommentResponse> comments
    ) {
        public static FullEngineeringTicketResponse fromFullEngineeringTicket(FullEngineeringTicket ticket) {
            
            EngineeringTicketResponse ticketResponse = EngineeringTicketResponse.fromEngineeringTicket(ticket.ticket());
            List<TicketCommentResponse> comments = new ArrayList<>();
            for(var comment: ticket.comments()) {
               comments.add(TicketCommentResponse.fromTicketComment(comment));
            }
            
            return new FullEngineeringTicketResponse(ticketResponse, ticket.description(), comments); 
        }
    }
            
    public static record EngineeringTicketResponse (
            String key,
            String project,
            String type,
            String status,
            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX") ZonedDateTime statusChanged,
            Optional<String> epic,
            Optional<String> initiative,
            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX") ZonedDateTime created,
            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")  Optional<ZonedDateTime> resolved,
            String priority,
            Optional<String> reporter,
            Optional<String> assignee,
            Optional<String> team,
            List<String> labels
    ) {
        public static EngineeringTicketResponse fromEngineeringTicket(EngineeringTicket ticket) {
            ZoneId utcZone = ZoneId.of("UTC");

            ZonedDateTime created = ticket.created().withZoneSameInstant(utcZone);
            ZonedDateTime statusChanged = ticket.statusChanged().withZoneSameInstant(utcZone);
            Optional<ZonedDateTime> resolved = ticket.resolved().map(date -> date.withZoneSameInstant(utcZone));
            
            return new EngineeringTicketResponse(
                ticket.key(),
                ticket.project(),
                ticket.type(),
                ticket.status(),
                statusChanged,
                ticket.epic(),
                ticket.initiative(),
                created,
                resolved,
                ticket.priority(),
                ticket.reporter(),
                ticket.assignee(),
                ticket.team(),
                ticket.labels()
            );
        }
    }

    public static record TicketCommentResponse (
            String description, 
            String author, 
            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX") ZonedDateTime created
    ) {
        public static TicketCommentResponse fromTicketComment(EngineeringTicketComment comment) {
            ZoneId utcZone = ZoneId.of("UTC");

            return new TicketCommentResponse(comment.description(), comment.author(), comment.created().withZoneSameInstant(utcZone));
        }
    }

}


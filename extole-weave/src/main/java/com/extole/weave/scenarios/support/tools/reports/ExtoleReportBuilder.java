package com.extole.weave.scenarios.support.tools.reports;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import com.cyster.ai.weave.service.ToolException;
import com.extole.weave.scenarios.support.tools.ExtoleWebClientFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ExtoleReportBuilder {
    public static final Duration DEFAULT_MAX_AGE = Duration.ofHours(8);
    ExtoleWebClientFactory webClientFactory;
    Optional<String> clientId = Optional.empty();
    ObjectNode payload = JsonNodeFactory.instance.objectNode();
    String format = "json";
    MediaType mediaType = new MediaType("application", "json");
    Class<?> targetFormat = JsonNode.class;
    private int offset = 0;
    private int limit = 10;
    private boolean waitForResult = true;
    private Duration maxAge = DEFAULT_MAX_AGE;

    public ExtoleReportBuilder(ExtoleWebClientFactory webClientFactory) {
        this.webClientFactory = webClientFactory;
    }

    public ExtoleReportBuilder withClientId(String clientId) {
        this.clientId = Optional.of(clientId);
        return this;
    }

    public ExtoleReportBuilder withName(String name) {
        this.payload.put("name", name);
        return this;
    }

    public ExtoleReportBuilder withDisplayName(String displayName) {
        this.payload.put("display_name", displayName);
        return this;
    }

    public ExtoleReportBuilder withFormat(String format) {
        var formats = this.payload.putArray("formats");
        formats.add(format);
        this.format = format;
        if (format.equals("json")) {
            this.mediaType = new MediaType("applicaton", "json");
            this.targetFormat = JsonNode.class;
        } else if (format.equals("csv")) {
            this.mediaType = new MediaType("text", "csv");
            this.targetFormat = String.class;
        } else {
            throw new RuntimeException("Unsupported download format");
        }

        return this;
    }

    public ExtoleReportBuilder withParameters(ObjectNode parameters) {
        this.payload.set("parameters", parameters);
        return this;
    }

    public ExtoleReportBuilder withOffset(int offset) {
        this.offset = offset;
        return this;
    }

    public ExtoleReportBuilder withLimit(int limit) {
        this.limit = limit;
        return this;
    }

    public ExtoleReportBuilder withWaitForResult(boolean wait) {
        this.waitForResult = wait;
        return this;
    }

    /**
     * Will reuse a report if its age is less than maxAge (default DEFAULT_MAX_AGE) and the parameters are identical
     * (for this feature to find a report, use descriptive periods or periods with a resolution less than maxAge)
     */
    public ExtoleReportBuilder withMaxAge(Duration duration) {
        this.maxAge = duration;
        return this;
    }


    public ObjectNode build() throws ToolException {
        WebClient webClient;
        if (this.clientId.isPresent()) {
            webClient = this.webClientFactory.getWebClient(this.clientId.get());
        } else {
            webClient = this.webClientFactory.getSuperUserWebClient();
        }

        var reportTag = "ai-" + reportHash(payload);
        var tags = this.payload.putArray("tags");
        tags.add(reportTag);

        JsonNode report = getReportByTag(reportTag, maxAge);
        if (report == null) {
            report = webClient.post()
                .uri(uriBuilder -> uriBuilder
                    .path("/v4/reports")
                    .build())
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

            if (report == null || !report.path("report_id").isEmpty()) {
                throw new ToolException("Internal error, failed to generate report");
            }
        }

        ObjectNode enrichedResult = JsonNodeFactory.instance.objectNode();
        enrichedResult.put("report_id", report.path("report_id").asText());
        enrichedResult.put("download_uri", report.path("download_uri").asText());
        enrichedResult.put("view_uri", "https://my.extole.com/reports/view?client_id=" + this.clientId.get() + "#"
            + report.path("report_id").asText());


        if (this.waitForResult) {
            var reportId = report.path("report_id").asText();

            while (!report.path("status").asText().equalsIgnoreCase("DONE")) {
                report = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                        .path("/v4/reports/" + reportId)
                        .build())
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException exception) {
                    throw new RuntimeException("Interrupt while waiting for report to finish", exception);
                }
            }

            var info = webClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path("/v4/reports/" + reportId + "/info")
                    .build())
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();


            enrichedResult.put("total_rows", info.path("total_rows").asInt());

            ObjectNode page = enrichedResult.putObject("page");
            page.put("row_start", this.offset);

            if (this.format.equalsIgnoreCase("json")) {
                ArrayNode result = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                        .path("/v4/reports/" + reportId + "/download" + "." + this.format)
                        .queryParam("offset", this.offset)
                        .queryParam("limit", this.limit)
                        .build())
                    .accept(this.mediaType)
                    .retrieve()
                    .bodyToMono(ArrayNode.class)
                    .block();

                page.put("row_count", result.size());
                enrichedResult.putArray("data").addAll(result);
            } else if (this.format.equalsIgnoreCase("csv")) {
                String csv = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                        .path("/v4/reports/" + reportId + "/download" + "." + this.format)
                        .queryParam("offset", this.offset)
                        .queryParam("limit", this.limit)
                        .build())
                    .accept(this.mediaType)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

                page.put("row_count", csv.split("\r?\n").length);
                enrichedResult.put("data", csv);
            } else {
                throw new RuntimeException("format not supported");
            }
        }

        return enrichedResult;
    }

    private JsonNode getReportByTag(String reportTag, Duration maxAge) throws ToolException {
        if (!this.clientId.isPresent()) {
            return null;
        }

        WebClient webClient = this.webClientFactory.getWebClient(this.clientId.get());

        JsonNode reports = webClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/v4/reports")
                .queryParam("required_tags", reportTag)
                .queryParam("limit", 1)
                .build())
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .bodyToMono(JsonNode.class)
            .block();

        if (!reports.isArray() || reports.size() == 0) {
            return null;
        }
        var report = reports.get(0);

        if (!report.has("created_date") || !report.has("report_id")) {
            return null;
        }

        var createdDate = Instant.parse(report.path("created_date").asText());
        Duration age = Duration.between(createdDate, Instant.now());

        if (age.compareTo(maxAge) > 0) {
            return null;
        }

        return report;
    }

    private static String reportHash(ObjectNode payload) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException exception) {
            throw new RuntimeException("Unable to load message digest", exception);
        }
        digest.update(payload.toString().getBytes());

        byte[] digestBytes = digest.digest();
        StringBuilder hexString = new StringBuilder();
        for (int i = 0; i < digestBytes.length; i++) {
            String hex = Integer.toHexString(0xff & digestBytes[i]);
            if(hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}

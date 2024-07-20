package com.extole.rest.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/support")
public class ReportRunnerController {
    private final ObjectMapper objectMapper;

    public ReportRunnerController(ObjectMapper objectMapper) {
 
        this.objectMapper = objectMapper;
    }

    @GetMapping("/report-runner/{runner}")
    public List<Object> getTickets(@RequestParam Optional<Integer> limit) {
        return null;
    }
}


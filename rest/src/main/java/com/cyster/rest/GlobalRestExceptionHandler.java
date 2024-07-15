package com.cyster.rest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

@ControllerAdvice
public class GlobalRestExceptionHandler {

    private static final Logger logger = LogManager.getLogger(GlobalRestExceptionHandler.class);

    @ExceptionHandler(RestException.class)
    public ResponseEntity<String> handleCustomException(RestException exception) {
        logger.warn("RestException: " + exception.toString(), exception);

        ObjectNode response = JsonNodeFactory.instance.objectNode();
        response.put("status_code", exception.getStatusCode().value());
        response.put("message", exception.getMessage());

        return ResponseEntity
            .status(exception.getStatusCode())
            .body(response.toString());
    }

}
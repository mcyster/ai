package com.cyster.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@ControllerAdvice
@RestController
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<String> handleGeneralException(Exception exception) {
        logger.error("An unexpected error occurred: ", exception);
        return new ResponseEntity<>("Internal Server Error: " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(RestException.class)
    public ResponseEntity<ErrorResponse> handleRestException(RestException exception) {
        logger.error("A RestException occurred: ", exception);
        ErrorResponse errorResponse = new ErrorResponse(
            "RestException",
            exception.getMessage(),
            exception.getStatusCode());
        
        return new ResponseEntity<>(errorResponse, exception.getStatusCode());
    }
    
    public static class ErrorResponse {
        private final String errorType;
        private final String message;
        private final HttpStatus statusCode;

        public ErrorResponse(String errorType, String message, HttpStatus statusCode) {
            this.errorType = errorType;
            this.message = message;
            this.statusCode = statusCode;
        }

        public String getErrorType() {
            return errorType;
        }

        public String getMessage() {
            return message;
        }
        
        public HttpStatus getStatusCode() {
            return statusCode;
        }
    }
}

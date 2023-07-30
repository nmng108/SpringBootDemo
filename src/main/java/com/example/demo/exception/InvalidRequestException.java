package com.example.demo.exception;

import lombok.Getter;
import org.springframework.validation.FieldError;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

//@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidRequestException extends HttpException {
    private static final String errorCode = "E00";

    // field_name => error_message
    @Getter
    private final Map<String, String> details = new HashMap<>();

    public InvalidRequestException(String message) {
        super(400, InvalidRequestException.errorCode, message);
    }

    public InvalidRequestException(List<FieldError> fieldExceptions) {
        super(400, InvalidRequestException.errorCode);

        fieldExceptions.forEach(e -> {
            details.put(e.getField(), e.getDefaultMessage());
        });

        this.getErrorResponse().put("details", this.details);
    }
}

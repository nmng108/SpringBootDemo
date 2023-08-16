package com.example.demo.exception;

import com.example.demo.dto.response.CommonResponse;
import com.example.demo.dto.response.SuccessState;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ResourceNotFoundException extends HttpException {
    private static final int HTTP_STATUS_CODE = 404;
    private static final String ERROR_CODE = "E10";

    public ResourceNotFoundException() {
        super(HTTP_STATUS_CODE);
    }

    public ResourceNotFoundException(String message) {
        super(HTTP_STATUS_CODE, ERROR_CODE, message);
    }
}

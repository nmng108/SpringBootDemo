package com.example.demo.exception;

import com.example.demo.dto.response.CommonResponse;
import com.example.demo.dto.response.SuccessState;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class InvalidRequestException extends HttpException {
    private static final int HTTP_STATUS_CODE = 400;
    private static final String ERROR_CODE = "E00";

    public InvalidRequestException(String message) {
        super(HTTP_STATUS_CODE, ERROR_CODE, message);
    }

    public InvalidRequestException(Map<String, String> details) {
        super(HTTP_STATUS_CODE, ERROR_CODE, details);
    }

//    public InvalidRequestException(List<FieldError> fieldExceptions) {
//        super(HTTP_STATUS_CODE, ERROR_CODE, fieldExceptions.stream().collect(
//                Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage, (a, b) -> b)
//        ));
//    }
//
//    public ResponseEntity<CommonResponse> toResponse() {
//        return ResponseEntity.badRequest().body(
//                CommonResponse.builder().success(SuccessState.FALSE).errors(this.errorMessages).build()
//        );
//    }
}

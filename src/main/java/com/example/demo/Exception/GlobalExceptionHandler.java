package com.example.demo.Exception;

import com.example.demo.Model.DTO.Response.CommonResponse;
import com.example.demo.Model.DTO.Response.SuccessState;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(PropertyReferenceException.class)
    public ResponseEntity<CommonResponse> handleMismatchPropertyName(PropertyReferenceException e) {
        System.out.println("caught PropertyReferenceException");
        return this.handleInvalidRequest(new InvalidRequestException(e.getMessage()));
    }

//    @ExceptionHandler(InvalidDataAccessApiUsageException.class)
//    public ResponseEntity<CommonResponse> handleMismatchPropertyName(InvalidDataAccessApiUsageException e) {
//        System.out.println("caught InvalidDataAccessApiUsageException");
//        return this.handleInvalidRequest(new InvalidRequestException("Wrong field name"));
//    }

    @ExceptionHandler({InvalidRequestException.class})
    public ResponseEntity<CommonResponse> handleInvalidRequest(InvalidRequestException e) {
        System.out.println("caught InvalidRequestException");
        return ResponseEntity.badRequest().body(
                CommonResponse.builder().success(SuccessState.FALSE).errors(e.getErrorResponse()).build()
        );
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<CommonResponse> handleValidationException(BindException ex) {
        System.out.println("caught BindException");
        return ResponseEntity.badRequest().body(
                CommonResponse.builder().success(SuccessState.FALSE).errors(
                        new InvalidRequestException(ex.getFieldErrors()).getErrorResponse()
                ).build()
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<CommonResponse> handleUriValidation(ConstraintViolationException ex) {
        System.out.println("caught ConstraintViolationException");

        HashMap<String, String> details = new HashMap<>();

        ex.getConstraintViolations().forEach(e -> {
            String[] fieldPath = e.getPropertyPath().toString().split("\\.");
            details.put(fieldPath[fieldPath.length - 1], e.getMessage());
        });

        return ResponseEntity.badRequest().body(
                CommonResponse.builder().success(SuccessState.FALSE).errors(
                        details
                ).build()
        );
    }

    @ExceptionHandler(HttpException.class)
    public ResponseEntity<CommonResponse> handleHttpException(HttpException e) {
        System.out.println("caught HttpException");
        return ResponseEntity.status(e.getHttpStatusCode()).body(
                CommonResponse.builder().success(SuccessState.FALSE).errors(e.getErrorResponse()).build()
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<CommonResponse> handleUnreadableRequest(HttpMessageNotReadableException e) {
        System.out.println("caught HttpMessageNotReadableException");
        return ResponseEntity.badRequest().body(CommonResponse.builder().success(SuccessState.FALSE).errors(
                "Wrong request body. Check and try again."
        ).build());
    }

    @ExceptionHandler({Exception.class})
    public ResponseEntity<CommonResponse> handleCommonExceptions(Exception e) {
        // log
        System.out.println("caught Exception");
        e.printStackTrace();

        return ResponseEntity.internalServerError().body(
                CommonResponse.builder().success(SuccessState.FALSE).build()
        );
    }
}

package com.example.demo.exception;


import com.example.demo.dto.response.CommonResponse;
import com.example.demo.dto.response.SuccessState;
import lombok.Getter;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

public class HttpException extends RuntimeException {
    protected int httpStatusCode = 500;
    protected Map<String, Object> errorMessages = null;

    // response without body
    public HttpException() {
        super();
    }

    public HttpException(Throwable throwable) {
        super(throwable);
    }

    public HttpException(String responseMessage, Throwable throwable) {
        super(responseMessage, throwable);
    }

    // response without body
    public HttpException(int httpStatusCode) {
        super();

        if (httpStatusCode < 400 || httpStatusCode >= 600) {
            throw new RuntimeException("Wrong error status code.");
        }

        this.httpStatusCode = httpStatusCode;
    }

    public HttpException(int httpStatusCode, Throwable cause) {
        super(cause);

        if (httpStatusCode < 400 || httpStatusCode >= 600) {
            throw new RuntimeException("Wrong error status code.");
        }

        this.httpStatusCode = httpStatusCode;
    }

    public HttpException(int httpStatusCode, String errorCode) {
        this(httpStatusCode);
        this.errorMessages = new HashMap<>();
        this.errorMessages.put("errorCode", errorCode);
    }

    public HttpException(int httpStatusCode, String errorCode, String message) {
        this(httpStatusCode, errorCode);

        this.errorMessages.put("details", message);
    }

    public HttpException(int httpStatusCode, String errorCode, Map<String, String> messages) {
        this(httpStatusCode, errorCode);
        this.errorMessages.put("details", messages);
    }

    public ResponseEntity<CommonResponse> toResponse() {
        return ResponseEntity.status(this.httpStatusCode).body(
                this.errorMessages == null
                        ? null
                        : CommonResponse.builder().success(SuccessState.FALSE).errors(this.errorMessages).build()
        );
    }
}

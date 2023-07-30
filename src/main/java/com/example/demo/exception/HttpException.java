package com.example.demo.exception;


import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public class HttpException extends RuntimeException {
    @Getter
    protected final int httpStatusCode;

    @Getter
    protected final Map<String, Object> errorResponse = new HashMap<>();

    private HttpException(int httpStatusCode) {
        super();
        if (httpStatusCode < 400) {
            throw new RuntimeException("Wrong status code. Error status code must be larger than 400");
        }
        this.httpStatusCode = httpStatusCode;
    }

    public HttpException(int httpStatusCode, String errorCode) {
        this(httpStatusCode);
        this.errorResponse.put("errorCode", errorCode);
    }

    public HttpException(int httpStatusCode, String errorCode, String message) {
        this(httpStatusCode, errorCode);
        this.errorResponse.put("details", message);
    }

    public HttpException(int httpStatusCode, String errorCode, Map<String, String> messages) {
        this(httpStatusCode, errorCode);
        this.errorResponse.put("details", messages);
    }
}

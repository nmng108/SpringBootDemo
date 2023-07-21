package com.example.demo.Exception;


import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public class HttpException extends RuntimeException {
    @Getter
    protected final int httpStatusCode;

    @Getter
    protected final Map<String, Object> errorResponse;

    public HttpException(int httpStatusCode, String errorCode) {
        super();
        this.httpStatusCode = httpStatusCode;
        this.errorResponse = new HashMap<>();
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

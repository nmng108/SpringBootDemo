package com.example.demo.exception;

import java.util.HashMap;
import java.util.Map;

public class InternalServerException extends HttpException {
    private static final int HTTP_STATUS_CODE = 500;
    private static final String ERROR_CODE = "E20";

    /**
     * Response without body
     */
    public InternalServerException() {
        super(HTTP_STATUS_CODE);
    }

    public InternalServerException(Throwable cause) {
        super(cause);
    }

    public InternalServerException(String message) {
        super(HTTP_STATUS_CODE, ERROR_CODE, message);
    }
}

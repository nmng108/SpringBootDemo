package com.example.demo.exception;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class InternalServerException extends HttpException {
    private static final int HTTP_STATUS_CODE = 500;
    private static final String ERROR_CODE = "E20";

    /**
     * Response without body
     */
    public InternalServerException() {
        super(HTTP_STATUS_CODE);
    }

    public InternalServerException(Throwable throwable) {
        super(throwable);
        log.warn(throwable.getLocalizedMessage());
    }

    public InternalServerException(String message) {
        super(HTTP_STATUS_CODE, ERROR_CODE, message);
    }
}

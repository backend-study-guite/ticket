package com.study.ticket.common.exception;

import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {

    private final ExceptionCode code;
    private final String message;

    public CustomException(ExceptionCode code) {
        this.code = code;
        this.message = code.getMessage();
    }

    public CustomException(ExceptionCode code, Object... args) {
        this.code = code;
        this.message = String.format(code.getMessage(), args);
    }
}

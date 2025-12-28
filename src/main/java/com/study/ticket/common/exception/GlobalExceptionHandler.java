package com.study.ticket.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<String> handleException(CustomException e){
        log.error("{} : {}", e.getCode().toString(), e.getMessage());

        return ResponseEntity
                .status(e.getCode().getCode())
                .body(e.getMessage());
    }
}

package com.study.ticket.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum ExceptionCode {
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),

    NOT_ENOUGH_POINTS(HttpStatus.BAD_REQUEST, "포인트가 부족합니다."),
    ILLEGAL_POINTS(HttpStatus.BAD_REQUEST, "충전하려는 포인트는 0보다 커야합니다."),

    ALREADY_IN_QUEUE(HttpStatus.BAD_REQUEST, "이미 다른 공연의 대기열에 진입해 있습니다."),
    QUEUE_SESSION_EXPIRED(HttpStatus.UNAUTHORIZED, "대기열 세션이 만료되었습니다. 다시 시도해주세요."),
    SESSION_EXTENSION_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "세션 연장 횟수를 초과했습니다."),

    CONCERT_NOT_FOUND(HttpStatus.NOT_FOUND, "콘서트를 찾을 수 없습니다."),

    SEAT_NOT_FOUND(HttpStatus.NOT_FOUND, "좌석을 찾을 수 없습니다."),
    SEAT_ALREADY_RESERVED(HttpStatus.CONFLICT, "이미 예약된 좌석입니다."),
    ;

    private final HttpStatus code;
    private final String message;
}

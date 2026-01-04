package com.study.ticket.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum ExceptionCode {

    SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "처리 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요."),

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),

    NOT_ENOUGH_POINTS(HttpStatus.BAD_REQUEST, "포인트가 부족합니다."),
    ILLEGAL_POINTS(HttpStatus.BAD_REQUEST, "충전하려는 포인트는 0보다 커야합니다."),

    CONCERT_NOT_FOUND(HttpStatus.NOT_FOUND, "콘서트를 찾을 수 없습니다."),

    RESERVATION_NOT_FOUND(HttpStatus.NOT_FOUND, "예약사항을 찾을 수 없습니다"),

    SEAT_NOT_FOUND(HttpStatus.NOT_FOUND, "좌석을 찾을 수 없습니다."),
    SEAT_ALREADY_RESERVED(HttpStatus.CONFLICT, "이미 예약된 좌석입니다."),
    ;

    private final HttpStatus code;
    private final String message;
}

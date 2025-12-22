package com.study.ticket.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum ExceptionCode {
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),

    NOT_ENOUGH_POINTS(HttpStatus.CONFLICT, "포인트가 부족합니다."),
    ILLEGAL_POINTS(HttpStatus.BAD_REQUEST, "충전하려는 포인트는 0보다 커야합니다."),
    POINT_EXCEEDS_SEAT_PRICE(HttpStatus.CONFLICT, "사용 포인트가 좌석 가격을 초과할 수 없습니다."),

    CONCERT_NOT_FOUND(HttpStatus.NOT_FOUND, "콘서트를 찾을 수 없습니다."),

    SEAT_NOT_FOUND(HttpStatus.NOT_FOUND, "좌석을 찾을 수 없습니다."),
    SEAT_ALREADY_RESERVED(HttpStatus.CONFLICT, "이미 예약된 좌석입니다."), // 동시성 이슈의 핵심!

    RESERVATION_NOT_FOUND(HttpStatus.NOT_FOUND, "예약을 찾을 수 없습니다."),
    RESERVATION_NOT_OWNED_BY_USER(HttpStatus.FORBIDDEN, "해당 예약에 대한 권한이 없습니다."),
    RESERVATION_ALREADY_PAID(HttpStatus.CONFLICT, "이미 결제된 예약입니다."),
    ;

    private final HttpStatus code;
    private final String message;
}

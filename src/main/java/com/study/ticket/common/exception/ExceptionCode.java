package com.study.ticket.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum ExceptionCode {
    // ===== User =====
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    NOT_ENOUGH_POINTS(HttpStatus.CONFLICT, "포인트가 부족합니다."),
    ILLEGAL_POINTS(HttpStatus.BAD_REQUEST, "충전/사용 포인트는 0보다 커야합니다."),

    // ===== Concert =====
    CONCERT_NOT_FOUND(HttpStatus.NOT_FOUND, "콘서트를 찾을 수 없습니다."),

    // ===== Seat =====
    SEAT_NOT_FOUND(HttpStatus.NOT_FOUND, "좌석을 찾을 수 없습니다."),
    SEAT_ALREADY_RESERVED(HttpStatus.CONFLICT, "이미 예약된 좌석입니다."),

    // ===== Reservation (추가 필요) =====
    RESERVATION_NOT_FOUND(HttpStatus.NOT_FOUND, "예약을 찾을 수 없습니다."),
    RESERVATION_USER_MISMATCH(HttpStatus.FORBIDDEN, "예약자와 결제 요청 유저가 다릅니다."),
    RESERVATION_ALREADY_PAID(HttpStatus.CONFLICT, "이미 결제된 예약입니다."),

    // ===== Seat (추가 추천) =====
    SEAT_RESERVE_INVALID_STATE(HttpStatus.CONFLICT, "좌석이 예매 가능한 상태가 아닙니다."),
    SEAT_PAY_INVALID_STATE(HttpStatus.CONFLICT, "좌석이 결제 가능한 상태가 아닙니다."),
    SEAT_RELEASE_INVALID_STATE(HttpStatus.CONFLICT, "좌석을 취소/환불할 수 있는 상태가 아닙니다."),

    // ===== Reservation (추가 추천) =====
    RESERVATION_PAY_INVALID_STATE(HttpStatus.CONFLICT, "예약이 결제 가능한 상태가 아닙니다."),
    ;


    private final HttpStatus code;
    private final String message;
}

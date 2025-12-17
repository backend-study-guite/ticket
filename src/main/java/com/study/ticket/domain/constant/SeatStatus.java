package com.study.ticket.domain.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum SeatStatus {
    AVAILABLE("AVAILABLE", "예약 가능"),
    RESERVED("RESERVED", "예약 완료"),
    PAID("PAID", "결제 완료");

    private final String status;
    private final String description;
}

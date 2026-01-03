package com.study.ticket.domain.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum SeatStatus {
    AVAILABLE("AVAILABLE", "에약 가능"),
    RESERVED("RESERVED", "에약 완료");

    private final String status;
    private final String description;
}
